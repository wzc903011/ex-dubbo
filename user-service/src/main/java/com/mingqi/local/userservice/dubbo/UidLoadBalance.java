package com.mingqi.local.userservice.dubbo;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.*;
import com.alibaba.dubbo.rpc.cluster.loadbalance.AbstractLoadBalance;
import com.alibaba.dubbo.rpc.cluster.loadbalance.RandomLoadBalance;
import com.alibaba.dubbo.rpc.support.RpcUtils;
import com.mingqi.local.userservice.dto.UidQueryRequest;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * cas来解决并发问题的代码先注释了
 */
public class UidLoadBalance extends AbstractLoadBalance {

    private static int DEFAULT_REPLICANUMBER = 10000;
    private static int multThreadCostMultiCreation = 0;

    private static RandomLoadBalance randomLoadBalance = new RandomLoadBalance();
    private static AtomicBoolean atomicBoolean = new AtomicBoolean(false);
    private static ExecutorService exec = Executors.newFixedThreadPool(1);

    private final ConcurrentMap<String, ConsistentHashSelector<?>> selectors = new ConcurrentHashMap<String, ConsistentHashSelector<?>>();
    //线程级别，由dubbo控制的消费者实例中的dubbo线程，所以线程数可控；
    private static final ThreadLocal<MessageDigest> LOCAL_MD5 = ThreadLocal.withInitial(() -> {
        try {
            return MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    });

    @SuppressWarnings("unchecked")
    @Override
    protected <T> Invoker<T> doSelect(List<Invoker<T>> invokers, URL url, Invocation invocation) {
        String methodName = RpcUtils.getMethodName(invocation);
        String key = invokers.get(0).getUrl().getServiceKey() + "." + methodName;
        //扩容、缩容后刷新虚拟节点
        int identityHashCode = System.identityHashCode(invokers);
        //method级别的selector
        ConsistentHashSelector<T> selector = (ConsistentHashSelector<T>) selectors.get(key);
        if (selector == null || selector.getIdentityHashCode() != identityHashCode) {
            //通过CAS控制consumer实例里某一个线程去完成初始化，其他线程走random逻辑（解决消费者实例中并发初始化耗时问题）
//            if (atomicBoolean.compareAndSet(false, true)) {
//                //异步初始化method的selector
//                System.out.println("线程： " + Thread.currentThread().getName() + " 获得锁");
//                exec.submit(new FutureTask<>(new Callable<Void>() {
//                    @Override
//                    public Void call() throws InterruptedException {
//                        System.out.println("线程： " + Thread.currentThread().getName() + "-------》开始异步初始化虚拟节点");
//                        long starttime = System.currentTimeMillis();
//                        selectors.put(key, new ConsistentHashSelector<T>(invokers, methodName));
//                        //重置锁 atomicBoolean
//                        atomicBoolean.compareAndSet(true, false);
//                        System.out.println("线程： " + Thread.currentThread().getName() + "-------》结束初始化, 耗时"+ (System.currentTimeMillis() - starttime));
//                        return null;
//                    }
//                }));
//                System.out.println("获得锁的线程： " + Thread.currentThread().getName() + "异步返回=====》 random");
//                return randomLoadBalance.select(invokers, url, invocation);
//            } else {
//                System.out.println("未获得锁的线程返回 random");
//                return randomLoadBalance.select(invokers, url, invocation);
//            }
            selectors.put(key, new ConsistentHashSelector<T>(invokers, methodName));
            selector = (ConsistentHashSelector<T>) selectors.get(key);
        }
        return selector.select(invocation);
    }

    private static final class ConsistentHashSelector<T> {

        private final TreeMap<Long, Invoker<T>> virtualInvokers;

        private final int identityHashCode;

        private final int argumentIndex;

        public ConsistentHashSelector(List<Invoker<T>> invokers, String methodName) {
            System.out.println("初始化" + multThreadCostMultiCreation++ + "次 线程：" + Thread.currentThread().getName());
            this.virtualInvokers = new TreeMap<>();
            this.identityHashCode = System.identityHashCode(invokers);
            URL url = invokers.get(0).getUrl();
            //通过配置得到含uid的参数索引
            String index = url.getMethodParameter(methodName, "uid.argument", "0");
            argumentIndex = Integer.parseInt(index);
            for (Invoker<T> invoker : invokers) {
                // 虚拟节点数使用缺省值，避免误填，不支持自定义
                int replicaNumber = DEFAULT_REPLICANUMBER;
                for (int i = 0; i < replicaNumber / 4; i++) {
                    byte[] digest = md5(removeUnnecessaryParameters(
                            invoker.getUrl(), Constants.PID_KEY, Constants.TIMESTAMP_KEY) + i);
                    for (int h = 0; h < 4; h++) {
                        long m = hash(digest, h);
                        virtualInvokers.put(m, invoker);
                    }
                }
            }
        }

        private String removeUnnecessaryParameters(URL url, String... excludedKeys) {
            Set<String> excludedKeySet = new HashSet<>();
            for (String s : excludedKeys) {
                if (s != null) {
                    excludedKeySet.add(s);
                }
            }
            Set<String> keys = url.getParameters().keySet();
            List<String> includes = new ArrayList<>();
            for (String k : keys) {
                if (k != null && !excludedKeySet.contains(k)) {
                    includes.add(k);
                }
            }
            return url.toFullString(includes.toArray(new String[includes.size()]));
        }

        public int getIdentityHashCode() {
            return identityHashCode;
        }

        public Invoker<T> select(Invocation invocation) {
            String key = toKey(invocation.getArguments());
            byte[] digest = md5(key);
            Invoker<T> invoker = selectForKey(hash(digest, 0));
            return invoker;
        }

        private String toKey(Object[] args) {
            StringBuilder buf = new StringBuilder();

            if (argumentIndex >= 0 && argumentIndex < args.length) {
                Object o = args[argumentIndex];
                //UidRequest，获取uid;
                //不是UidRequest的子类，用原参数(即uid)
                if (UidQueryRequest.class.isAssignableFrom(args[argumentIndex].getClass())) {
                    UidQueryRequest request = (UidQueryRequest) o;
                    o = request.getUid();
                }
                buf.append(o);
            }
            return buf.toString();
        }

        private Invoker<T> selectForKey(long hash) {
            //AscendingSubMap，用firstEntry做，是拿和hash值相关的最小值； 所以hash一定要小于virtualInvokers里最大的key
            Map.Entry<Long, Invoker<T>> entry = virtualInvokers.tailMap(hash, true).firstEntry();
            if (entry == null) {
                entry = virtualInvokers.firstEntry();
            }
            return entry.getValue();
        }

        private long hash(byte[] digest, int number) {
            return (((long) (digest[3 + number * 4] & 0xFF) << 24)
                    | ((long) (digest[2 + number * 4] & 0xFF) << 16)
                    | ((long) (digest[1 + number * 4] & 0xFF) << 8)
                    | (digest[0 + number * 4] & 0xFF))
                    & 0xFFFFFFFFL;
        }

        private byte[] md5(String value) {
            MessageDigest md5 = LOCAL_MD5.get();
            md5.reset();
            byte[] bytes = null;
            try {
                bytes = value.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
            md5.update(bytes);
            return md5.digest();
        }
    }
}

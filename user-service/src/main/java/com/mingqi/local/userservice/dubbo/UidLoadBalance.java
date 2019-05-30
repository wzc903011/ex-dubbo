package com.mingqi.local.userservice.dubbo;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.cluster.loadbalance.AbstractLoadBalance;
import com.alibaba.dubbo.rpc.support.RpcUtils;
import com.mingqi.local.userservice.dto.UidQueryRequest;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 参考  com.alibaba.dubbo.rpc.cluster.loadbalance.ConsistentHashLoadBalance
 * todo 问题：如何保证均匀==即==》预生成的virtualInvokers如何保证和请求的参数生成的key均匀；
 * 待学习：一致性hash算法里 node和真实结点的关系；  考虑缩容 扩容
 * 虚拟节点初始化：
 *     每个method都会初始化virtualInvokers，都是（replicaNumber/4）* 4个key
 */
public class UidLoadBalance extends AbstractLoadBalance {

    private final ConcurrentMap<String, ConsistentHashSelector<?>> selectors = new ConcurrentHashMap<String, ConsistentHashSelector<?>>();
    private static final ThreadLocal<MessageDigest> LOCAL_MD5 = ThreadLocal.withInitial(() -> {
        try {
            return MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    });

    @Override
    protected <T> Invoker<T> doSelect(List<Invoker<T>> invokers, URL url, Invocation invocation) {
        String methodName = RpcUtils.getMethodName(invocation);
        String key = invokers.get(0).getUrl().getServiceKey() + "." + methodName;
        int identityHashCode = System.identityHashCode(invokers);
        //method级别的selector
        ConsistentHashSelector<T> selector = (ConsistentHashSelector<T>) selectors.get(key);
        if (selector == null || selector.getIdentityHashCode() != identityHashCode) {
            //初始化method的selector
            selectors.put(key, new ConsistentHashSelector<T>(invokers, methodName));
            selector = (ConsistentHashSelector<T>) selectors.get(key);
        }
        return selector.select(invocation);
    }

    private static final class ConsistentHashSelector<T> {

        private final TreeMap<Long, Invoker<T>> virtualInvokers;

        private final int replicaNumber;

        private final int identityHashCode;

        private final int[] argumentIndex;

        public ConsistentHashSelector(List<Invoker<T>> invokers, String methodName) {
            this.virtualInvokers = new TreeMap<>();
            this.identityHashCode = System.identityHashCode(invokers);
            URL url = invokers.get(0).getUrl();
            //获取配置的节点数量，作用？
            this.replicaNumber = url.getMethodParameter(methodName, "hash.nodes", 160);
            //定义UidQueryRequest是第几个参数  对象类型可以不用，应该接口就一个对象类型； 基础类型的参数可以设置
            String[] index = Constants.COMMA_SPLIT_PATTERN.split(url.getMethodParameter(methodName, "uid.arguments", "0"));
            //获取配置的参数使用，后面作为hashkey
            argumentIndex = new int[index.length];
            for (int i = 0; i < index.length; i++) {
                argumentIndex[i] = Integer.parseInt(index[i]);
            }
            for (Invoker<T> invoker : invokers) {
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
            String hashKey = RpcContext.getContext()
                    .getAttachments().remove(Constants.LB_HASH_KEY);
            if (hashKey != null) {
                return hashKey;
            }
            StringBuilder buf = new StringBuilder();
            for (int i : argumentIndex) {
                if (i >= 0 && i < args.length) {
                    Object o = args[i];
                    //UidQueryRequest的子类，用uid做key;
                    //不是UidQueryRequest的子类，用原参数做key
                    if (UidQueryRequest.class.isAssignableFrom(args[i].getClass())) {
                        UidQueryRequest request = (UidQueryRequest)o;
                        o = request.getUid();
                    }
                    buf.append(o);
                }
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

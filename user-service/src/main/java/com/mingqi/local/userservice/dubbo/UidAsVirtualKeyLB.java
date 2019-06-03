package com.mingqi.local.userservice.dubbo;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.util.*;

/**
 * 模拟就一个方法，测试平衡性;
 * 测试基础配置：虚拟节点1000， 真实实例200
 * 线上真实uid的数值：最小uid：530000089	最大uid：9999999954769
 * 测试数据区间：（都是5w）
 *     minUid：530000089  maxUid：530050089
 *     minUid：9999999904769L  maxUid：9999999954769L
 * 结果：分布均匀，在0.5%左右浮动  最大不超过0.6%，最小不低于0.4%
 *
 * 参考：https://juejin.im/post/5b8f93576fb9a05d11175b8d
 */
public class UidAsVirtualKeyLB {

    private static final ThreadLocal<MessageDigest> LOCAL_MD5 = ThreadLocal.withInitial(() -> {
        try {
            return MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    });

    private static String[] groups = new String[200];
    static {
        for (int i = 0; i < 200; i++) {
            groups[i] = "192.168.0."+i+":111";
        }
    }

    /**
     * 真实集群地址列表
     */
    private static List<String> realGroups = new LinkedList<>();

    /**
     * 虚拟节点的映射关系
     */
    private static TreeMap<Long, String> virtualNodes = new TreeMap<>();

    private static final Integer virtualNodeCount = 1000;

    static {
        //先添加真实节点列表
        realGroups.addAll(Arrays.asList(groups));
        //初始化虚拟节点
        initVirtualNodes();
    }

    private static void initVirtualNodes() {
        for (String realGroup : realGroups) {
            for (int i = 0; i < virtualNodeCount / 4; i++) {
                byte[] digest = md5(realGroup + i);
                for (int h = 0; h < 4; h++) {
                    long m = hash(digest, h);
                    virtualNodes.put(m, realGroup);
                }
            }
        }
    }

    public static String select(Long uid) {
        String key = toKey(uid);
        byte[] digest = md5(key);
        String realGroup = selectForKey(hash(digest, 0));
        return realGroup;
    }

    private static String toKey(Long uid) {
        StringBuilder buf = new StringBuilder();
        buf.append(uid);
        return buf.toString();
    }

    private static String selectForKey(long hash) {
        //AscendingSubMap，用firstEntry做，是拿和hash值相关的最小值； 所以hash一定要小于virtualInvokers里最大的key
        Map.Entry<Long, String> entry = virtualNodes.tailMap(hash, true).firstEntry();
        if (entry == null) {
            entry = virtualNodes.firstEntry();
        }
        return entry.getValue();
    }

    private static long hash(byte[] digest, int number) {
        return (((long) (digest[3 + number * 4] & 0xFF) << 24)
                | ((long) (digest[2 + number * 4] & 0xFF) << 16)
                | ((long) (digest[1 + number * 4] & 0xFF) << 8)
                | (digest[0 + number * 4] & 0xFF))
                & 0xFFFFFFFFL;
    }

    private static byte[] md5(String value) {
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

    private static void refreshHashCircle() {
        // 当集群变动时，刷新hash环，其余的集群在hash环上的位置不会发生变动
        virtualNodes.clear();
        initVirtualNodes();
    }
    private static void addGroup(String identifier) {
        realGroups.add(identifier);
        refreshHashCircle();
    }

    private static void removeGroup(String identifier) {
        int i = 0;
        for (String group:realGroups) {
            if (group.equals(identifier)) {
                realGroups.remove(i);
            }
            i++;
        }
        refreshHashCircle();
    }

    public static void main(String[] args) {
        testBalance();
        System.out.println("start remove test");
        realGroups.remove("192.168.0.1:111");
        testBalance();
        System.out.println("start add test");
        realGroups.add("192.168.0.1:111");
        realGroups.add("192.168.0.221:111");
        testBalance();
    }

    private static void testBalance() {
        Map<String, Integer> resMap = new HashMap<>();

        Long minUid = 9999999904769L;
        Long maxUid = 9999999954769L;
        for (Long uid = minUid; uid < maxUid; uid++) {
            String realGroup = select(uid);
            if (resMap.containsKey(realGroup)) {
                resMap.put(realGroup, resMap.get(realGroup) + 1);
            } else {
                resMap.put(realGroup, 1);
            }
        }

        resMap.forEach(
                (k, v) -> {
                    System.out.println("realGroup " + k + ":" + v + "（" + 100*(float)v/(float)(maxUid-minUid) +"%)");
                });
    }

}

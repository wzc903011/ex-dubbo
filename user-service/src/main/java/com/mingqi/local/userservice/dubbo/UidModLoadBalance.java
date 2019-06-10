package com.mingqi.local.userservice.dubbo;

import com.google.common.collect.Maps;
import org.assertj.core.util.Lists;

import java.util.*;
import java.util.function.Function;

public class UidModLoadBalance {

    private static int modValue = 150;

    private static int realCount = 150;

    private static int identityHashCode = -1;

    private static Map<Integer, List<String>> map = new HashMap<>();

    private static List<String> realInstances = Lists.newArrayList();
    static {
        for (int i = 0; i < realCount; i++) {
            realInstances.add("realInstance_" + i);
        }
    }

    private static List<String> doSelect(Integer uid) {
        int mod = uid % modValue;
        List<String> targetRealInstances = map.get(mod);
        return targetRealInstances;
    }

    private static void refresh() {
        long starttime = System.currentTimeMillis();
        List<String> copyRealInstances = new ArrayList<>(realInstances);
        if (map == null) {
            map = Maps.newHashMap();
        }
        map.clear();
        System.out.println("inti cost1 :" + (System.currentTimeMillis() - starttime));

        Function<String, String> cmp = instance -> instance;
        copyRealInstances.sort(Comparator.comparing(cmp));
        System.out.println("inti cost2 :" + (System.currentTimeMillis() - starttime));


        //invokers按 mod providerMod  分组
        for (int i = 0; i < realInstances.size(); i++) {
            int mod = i % modValue;
            if (!map.containsKey(mod)) {
                map.put(mod, new ArrayList<>());
            }
            map.get(mod).add(realInstances.get(i));
        }
        System.out.println("inti cost3 :" + (System.currentTimeMillis() - starttime));
        identityHashCode = System.identityHashCode(realInstances);
    }

    public static void main(String[] args) {
        long starttime = System.currentTimeMillis();
        refresh();
        System.out.println("inti cost :" + (System.currentTimeMillis() - starttime));

        Map<String, Integer> realInstance2HitCountMap = new HashMap<>();

        for (int i = 0; i < 10000; i++) {
            String realInstance = doSelect(i).get(0);
            Integer count = realInstance2HitCountMap.get(realInstance);
            if (null == count) {
                realInstance2HitCountMap.put(realInstance, 1);
            } else {
                realInstance2HitCountMap.put(realInstance, realInstance2HitCountMap.get(realInstance) + 1);
            }
        }
        System.out.println(realInstance2HitCountMap);
    }
}

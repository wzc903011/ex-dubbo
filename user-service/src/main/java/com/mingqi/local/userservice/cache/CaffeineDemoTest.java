package com.mingqi.local.userservice.cache;

import com.github.benmanes.caffeine.cache.*;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import com.mingqi.local.userservice.dto.GroupUserInfo;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 按4亿活跃用户：150个实例算，一个实例要存270W数据，按一些非活跃用户，峰值按300W
 * 1w个key：80M
 * 10w个key：120M
 * 100w个key：600M  100w个最大的uid作为key：735M    value加倍：602M
 * 300w个key：1708M   300w个最大的uid作为key：1795M
 *
 * 结论：value大小对内存占用影响很小；  key的个数、key的大小对内存使用影响较大   key个数 > key大小
 *
 */
public class CaffeineDemoTest extends CacheDemoTest{

    static Cache<String, GroupUserInfo> cache = Caffeine.newBuilder()
            .maximumSize(MAX_SIZE)  // 设置最大的key
            .expireAfterWrite(WRITE_EXPIRED_MINUTES, TimeUnit.MINUTES)   //按写入设置过期时间
            .expireAfterAccess(WRITE_EXPIRED_MINUTES, TimeUnit.MINUTES)  // 按被访问后设置过期时间
            //key移除的监听器
            .removalListener((String uid, GroupUserInfo userInfo, RemovalCause cause) ->
                    System.out.println("uid="+ uid + ", value="+ userInfo + " has been removed"))
            .recordStats()
            .build();

    public static void main(String[] args) {
        long initstarttime = System.currentTimeMillis();
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        initCache(MAX_SIZE);
        System.out.println("write cost ====>" + (System.currentTimeMillis() - initstarttime)+ "ms");
        long starttime = System.currentTimeMillis();
        Random random = new Random();
        for (int i = 0; i < 100000; i++) {
            Long uid = 530000089L + random.nextInt(MAX_SIZE);
            getSingle(uid);
        }
        System.out.println("cost ====>" + (System.currentTimeMillis() - starttime)+ "ms");
        System.out.println(cache.stats());

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        cache.cleanUp();
    }

    private static void initCache(int size) {
        for (Long i = 0L; i < size; i++) {
            if (i%10000 == 0) {
                System.out.println("keyCount=" + i);
            }

            GroupUserInfo groupUserInfo = new GroupUserInfo(i+MIN_UID, "name"+i, "avatar"+i, 1);
            putCaffeine(i + MIN_UID, groupUserInfo);
        }
    }

    private static void initCacheWithMaxUid(int size) {
        for (Long i = 0L; i < size; i++) {
            if (i%10000 == 0) {
                System.out.println("keyCount=" + i);
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            GroupUserInfo groupUserInfo = new GroupUserInfo(MAX_UID - i, "name"+i, "avatar"+i, 1);
            putCaffeine(MAX_UID - i, groupUserInfo);
        }
    }




    private static void putCaffeine(Long uid, GroupUserInfo value) {
        cache.put(USER_PORTRAIT+uid, value);
    }

    private static GroupUserInfo getSingle(Long uid) {
        return cache.getIfPresent(USER_PORTRAIT + uid);
    }



    // 批量获取
//    private static Map<Long, GroupUserInfo> getAll(List<Long> uidList) {
//        return cache.getAllPresent(uidList);
//    }
    // get的时候如果不存在，异步的去put;   不使用，因为作为一级缓存，逻辑上和二级、存储分开
//    static AsyncLoadingCache<Long, GroupUserInfo> asyncCache = Caffeine.newBuilder()
//            .maximumSize(100)
//            .expireAfterWrite(1, TimeUnit.MINUTES)
//            .buildAsync(k -> GroupUserInfo.getByUid(k));

    // get的时候如果不存在，同步的去put;   不使用，因为作为一级缓存，逻辑上和二级、存储分开
//    static LoadingCache<Long, GroupUserInfo> syncCache = Caffeine.newBuilder()
//            .maximumSize(100)
//            .expireAfterWrite(1, TimeUnit.MINUTES)
//            .build(k -> GroupUserInfo.getByUid(k));

    // 个性化的缓存过期时间配置
//    static Cache<Long, GroupUserInfo> customCache = Caffeine.newBuilder()
//            .expireAfter(new Expiry<Long, GroupUserInfo>() {
//                @Override
//                public long expireAfterCreate(Long key, GroupUserInfo value, long currentTime) {
//                    return 60*1000;
//                }
//
//                @Override  // 没有更新，不需要关心
//                public long expireAfterUpdate(Long key, GroupUserInfo value, long currentTime, long currentDuration) {
//                    return 60*1000;
//                }
//
//                @Override
//                public long expireAfterRead(Long key, GroupUserInfo value, long currentTime, long currentDuration) {
//                    return 60*1000;
//                }
//            })
//            .build();
}

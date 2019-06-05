package com.mingqi.local.userservice.cache;

import com.google.common.cache.*;
import com.mingqi.local.userservice.dto.GroupUserInfo;

import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * 按4亿活跃用户：150个实例算，一个实例要存270W数据，按一些非活跃用户，峰值按300W
 * 1w个key：53M + 235M
 * 10w个key：140M + 235M
 * 100w个key：621M + 235M    100w个最大的uid作为key：725M
 * 300w个key：1670M + 235M   300w个最大的uid作为key：1702M + 235M
 *
 * 结论：value大小对内存占用影响很小；  key的个数、key的大小对内存使用影响较大   key个数 > key大小
 *
 */
public class GuavaCacheDemoTest extends CacheDemoTest {

    static CacheLoader<String, GroupUserInfo> loader;

    static {
        loader = new CacheLoader<String, GroupUserInfo>() {
            @Override
            public GroupUserInfo load(String key) throws ExecutionException {
                return cache.get(key);
            }
        };
    }

    static RemovalListener<String, GroupUserInfo> listener;
    static {
        listener = new RemovalListener<String, GroupUserInfo>() {
            @Override
            public void onRemoval(RemovalNotification<String, GroupUserInfo> n) {
                if (n.wasEvicted()) {
                    String cause = n.getCause().name();
                    System.out.println("evict key=" + n.getKey());
                }
            }
        };
    }

    static LoadingCache<String, GroupUserInfo> cache;

    static {
        cache = CacheBuilder.newBuilder()
                .maximumSize(MAX_SIZE)
                .expireAfterWrite(WRITE_EXPIRED_MINUTES, TimeUnit.MINUTES)
                .recordStats()
                .removalListener(listener)
                .build(loader);
    }



    public static void main(String[] args) {
        long initstarttime = System.currentTimeMillis();
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
            if (i % 10000 == 0) {
                System.out.println("keyCount=" + i);
            }

            GroupUserInfo groupUserInfo = new GroupUserInfo(i + MIN_UID, "name" + i, "avatar" + i, 1);
            putGuava(i+MIN_UID, groupUserInfo);
        }
    }

    private static void testMaxUidMemoryUse(int size) {
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
            putGuava(MAX_UID - i, groupUserInfo);
        }
    }

    private static void putGuava(Long uid, GroupUserInfo value) {
        cache.put(USER_PORTRAIT+uid, value);
    }

    private static GroupUserInfo getSingle(Long uid) {
        return cache.getIfPresent(USER_PORTRAIT + uid);
    }

}

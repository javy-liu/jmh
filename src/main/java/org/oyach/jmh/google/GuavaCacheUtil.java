package org.oyach.jmh.google;

import com.google.common.base.Function;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.collect.MapMaker;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * @author liuzhenyuan
 * @version Last modified 15/3/4
 * @since 0.0.1
 */
public class GuavaCacheUtil {
    private static final int i = 0;
    public static void CacheBuilder() throws Exception{
        Cache<String, String> cache = CacheBuilder.newBuilder()
                .concurrencyLevel(4)
                .weakKeys()
                .maximumSize(10)
                .expireAfterAccess(3000, TimeUnit.MILLISECONDS)
                .build(new CacheLoader<String, String>() {
                    @Override
                    public String load(String key) throws Exception {

                        return key + i;
                    }
                });

        cache.put("1", "11");
        cache.put("2", "22");
        cache.put("3", "33");
        cache.put("4", "44");
        cache.put("5", "55");
        cache.put("6", "66");
        cache.put("7", "77");
        cache.put("8", "88");
        cache.put("9", "99");
        cache.put("10", "1010");
        cache.put("11", "1111");
        System.out.println(cache.asMap().values());

        Thread.sleep(1000);
        String v1 = cache.getIfPresent("1");
        String v2 = cache.getIfPresent("2");
        String v3 = cache.getIfPresent("3");
        Thread.sleep(1000);

        System.out.println(cache.asMap().values());
        Thread.sleep(4000);
        System.out.println(cache.asMap().values());
        Thread.sleep(1000);
        System.out.println(cache.asMap().values());
        Thread.sleep(1000);
        System.out.println(cache.asMap().values());
    }
}

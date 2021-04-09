package tech.tongyu.bct.common.util;

import java.util.concurrent.ConcurrentHashMap;

public class SystemConfig {
    private static ConcurrentHashMap<String, Object> cache = new ConcurrentHashMap<>();

    public static Object get(String key){
        return cache.get(key);
    }

    public static void put(String key, Object value){
        cache.put(key, value);
    }
}

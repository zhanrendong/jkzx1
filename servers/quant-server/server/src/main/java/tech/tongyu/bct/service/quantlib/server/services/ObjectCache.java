package tech.tongyu.bct.service.quantlib.server.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import tech.tongyu.bct.service.quantlib.common.utils.JsonMapper;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public enum ObjectCache {
    Instance;
    private static final Cache<String, Object> repository = CacheBuilder
            .newBuilder()
            .maximumSize(10000)
            .expireAfterAccess(10, TimeUnit.MINUTES).build();

    public Object get(String key) {
        return repository.getIfPresent(key);
    }

    public void put(String k, Object v) {
        repository.put(k, v);
    }

    public void create(String id, String json) throws Exception {
        JsonNode node = JsonMapper.mapper.readValue(json, JsonNode.class);
        String simple = node.get("class").asText();
        Class clazz = ClassCache.Instance.get(simple);
        if (clazz == null)
            throw new Exception("Cannot locate class " + simple);
        Object o = JsonMapper.mapper.readValue(json, clazz);
        repository.put(id, o);
    }

    public void create(String id, Map<String, Object> json) throws Exception {
        String simple = (String)json.get("class");
        Class clazz = ClassCache.Instance.get(simple);
        if (clazz == null)
            throw new Exception("Cannot locate class " + simple);
        Object o = JsonMapper.mapper.convertValue(json, clazz);
        repository.put(id, o);
    }

    public void delete(String id) {
        repository.invalidate(id);
        return;
    }
}
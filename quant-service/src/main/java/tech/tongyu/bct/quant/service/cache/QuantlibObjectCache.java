package tech.tongyu.bct.quant.service.cache;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.hashids.Hashids;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.exception.ErrorCode;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.quant.library.common.QuantlibSerializableObject;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public enum QuantlibObjectCache {
    Instance;

    private static Hashids hash = new Hashids("quantlib hashid salt");
    private static AtomicLong idCounter = new AtomicLong();

    private static final Cache<String, QuantlibSerializableObject> repository = CacheBuilder
            .newBuilder()
            .maximumSize(10000)
            .expireAfterAccess(10, TimeUnit.MINUTES).build();

    private static String genID(QuantlibSerializableObject o) {
        long n = idCounter.getAndIncrement()+System.currentTimeMillis();
        return o.getClass().getSimpleName() + "~"+ hash.encode(n);
    }

    public Object get(String key) {
        return repository.getIfPresent(key);
    }

    public QuantlibSerializableObject getMayThrow(String key) {
        QuantlibSerializableObject o = repository.getIfPresent(key);
        if (Objects.isNull(o)) {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, String.format("quantlib: 对象 %s 不存在", key));
        }
        return o;
    }

    public <T> T getWithTypeMayThrow(String key, Class<T> c) {
        QuantlibSerializableObject o = getMayThrow(key);
        if (c.isInstance(o)) {
            return (T) o;
        } else {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                    String.format("quantlib: 对象 %s 类型不正确", key));
        }
    }

    public void put(String k, QuantlibSerializableObject v) {
        repository.put(k, v);
    }

    public String put(QuantlibSerializableObject v, String id) {
        String k = id;
        if (Objects.isNull(id)) {
            k = genID(v);
        }
        repository.put(k, v);
        return k;
    }

    public String put(QuantlibSerializableObject v) {
        String id = genID(v);
        repository.put(id, v);
        return id;
    }

    public String create(String id, String json) throws Exception {
        QuantlibSerializableObject o = JsonUtils.mapper.readValue(json, QuantlibSerializableObject.class);
        repository.put(id, o);
        return id;
    }

    public void create(String id, JsonNode jsonNode) {
        QuantlibSerializableObject o = JsonUtils.mapper.convertValue(jsonNode, QuantlibSerializableObject.class);
        repository.put(id, o);
    }

    /*public void create(String id, Map<String, Object> json) throws Exception {
        String simple = (String)json.get("class");
        Class clazz = ClassCache.Instance.get(simple);
        if (clazz == null)
            throw new Exception("Cannot locate class " + simple);
        Object o = JsonMapper.mapper.convertValue(json, clazz);
        repository.put(id, o);
    }*/

    public void delete(String id) {
        repository.invalidate(id);
    }
}

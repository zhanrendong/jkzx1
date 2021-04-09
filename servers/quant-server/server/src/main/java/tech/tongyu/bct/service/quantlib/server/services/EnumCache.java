package tech.tongyu.bct.service.quantlib.server.services;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantEnum;

import java.util.Set;

public enum EnumCache {
    Instance;

    private static final Logger logger = LoggerFactory.getLogger(EnumCache.class);

    static private final Cache<String, Object> repository = CacheBuilder.newBuilder().build();

    public void initialize() {
        Reflections reflections = new Reflections("tech.tongyu.bct.service.quantlib");
        Set<Class<?>> enums = reflections.getTypesAnnotatedWith(BctQuantEnum.class);
        for(Class<?> c : enums) {
            if(c.isEnum()) {
                logger.info("Annotated enum found: " + c.getSimpleName());
                for(Object e : c.getEnumConstants()) {
                    String name = e.toString();
                    repository.put(name, e);
                    logger.info("  registered enum: " + name);
                }
            }
        }
    }

    public Object get(String key) {
        return repository.getIfPresent(key);
    }
}
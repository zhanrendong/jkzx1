package tech.tongyu.bct.service.quantlib.server.services;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantSerializable;

import java.util.Set;

public enum ClassCache {
    Instance;

    private static final Logger logger = LoggerFactory.getLogger(ClassCache.class);

    static private final Cache<String, Class> repository = CacheBuilder.newBuilder().build();

    public Class get(String key) {
        return repository.getIfPresent(key);
    }

    public void initialize() {
        Reflections reflections = new Reflections("tech.tongyu.bct.service.quantlib");
        Set<Class<?>> clazzes = reflections.getTypesAnnotatedWith(BctQuantSerializable.class);
        for (Class<?> clazz : clazzes) {
            logger.info("Serializable class found: " + clazz.getSimpleName());
            String simple = clazz.getSimpleName();
            String full = clazz.getCanonicalName();
            repository.put(simple, clazz);
            logger.info("  Registered simple name {} for class {}", simple, full);
        }
    }
}
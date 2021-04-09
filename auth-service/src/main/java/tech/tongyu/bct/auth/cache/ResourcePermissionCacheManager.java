package tech.tongyu.bct.auth.cache;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import tech.tongyu.bct.auth.enums.ResourcePermissionTypeEnum;
import tech.tongyu.bct.auth.manager.ResourcePermissionManager;

import java.util.List;

/**
 * 查询资源权限的redis缓存层
 * 当前默认关闭redis缓存机制
 */
@Component
@PropertySource("classpath:application.yml")
public class ResourcePermissionCacheManager {

    private ResourcePermissionManager resourcePermissionManager;
    private RedisPublisher redisPublisher;

    @Value("${redis.enabled:false}")
    private Boolean redisEnabled;

    @Autowired
    public ResourcePermissionCacheManager(ResourcePermissionManager resourcePermissionManager,
                                          RedisPublisher redisPublisher) {
        this.resourcePermissionManager = resourcePermissionManager;
        this.redisPublisher = redisPublisher;
    }

    public boolean hasPermission(String userId, String resourceId, ResourcePermissionTypeEnum resourcePermissionType){
        if(redisEnabled) {
            String key = userId.substring(0, 8) + resourceId.substring(0, 8) + resourcePermissionType.name();
            String s = redisPublisher.get(key);
            if (StringUtils.isBlank(s)) {
                Boolean result = resourcePermissionManager.hasPermission(userId, resourceId, resourcePermissionType);
                redisPublisher.set(key, result ? "1" : "0");
                return result;
            } else {
                return "1".equals(s);
            }
        }
        return resourcePermissionManager.hasPermission(userId, resourceId, resourcePermissionType);
    }

    /** 当前实现不查询redis缓存，直接查询数据库 */
    public List<Boolean> hasPermission(String userId, String resourceId, List<ResourcePermissionTypeEnum> resourcePermissionTypeList) {
        return resourcePermissionManager.hasPermission(userId, resourceId, resourcePermissionTypeList);
    }

    /** 当前实现不查询redis缓存，直接查询数据库 */
    public List<Boolean> hasPermission(String userId, List<String> resourceId, ResourcePermissionTypeEnum resourcePermissionType) {
        return resourcePermissionManager.hasPermission(userId, resourceId, resourcePermissionType);
    }

}

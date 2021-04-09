package tech.tongyu.bct.auth.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tech.tongyu.bct.common.exception.CustomException;

import tech.tongyu.bct.common.exception.ErrorCode;
import tech.tongyu.bct.auth.exception.ReturnMessageAndTemplateDef;

@Component
public class RedisPublisher {

    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    public RedisPublisher(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void publish(String channel, Object content) {
        stringRedisTemplate.convertAndSend(channel, content);
    }

    public void set(String key, String value){
        try{
            stringRedisTemplate.opsForValue().set(key, value);
        } catch (Exception e){
            throw new CustomException(
                    ErrorCode.SERVICE_FAILED,
                    String.format(ReturnMessageAndTemplateDef.Errors.REDIS_ERROR_INFO.getMessage(),e.getMessage()));
        }
    }

    public String get(String key){
        try{
            return stringRedisTemplate.opsForValue().get(key);
        } catch (Exception e){
            throw new CustomException(
                    ErrorCode.SERVICE_FAILED,
                    String.format(ReturnMessageAndTemplateDef.Errors.REDIS_ERROR_INFO.getMessage(),e.getMessage()));
        }
    }

    public void delete(String key){
        try{
            stringRedisTemplate.delete(key);
        } catch (Exception e){
            throw new CustomException(
                    ErrorCode.SERVICE_FAILED,
                    String.format(ReturnMessageAndTemplateDef.Errors.REDIS_ERROR_INFO.getMessage(),e.getMessage()));
        }
    }

    public Boolean hasKey(String key){
        try{
            return stringRedisTemplate.hasKey(key);
        } catch (Exception e){
            throw new CustomException(
                    ErrorCode.SERVICE_FAILED,
                    String.format(ReturnMessageAndTemplateDef.Errors.REDIS_ERROR_INFO.getMessage(),e.getMessage()));
        }
    }
}

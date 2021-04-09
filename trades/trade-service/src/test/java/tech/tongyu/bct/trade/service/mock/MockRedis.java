package tech.tongyu.bct.trade.service.mock;

import org.springframework.data.redis.core.RedisTemplate;

public class MockRedis extends RedisTemplate {

    public MockRedis(){

    }

    @Override
    public void convertAndSend(String channel, Object message) {

    }
}

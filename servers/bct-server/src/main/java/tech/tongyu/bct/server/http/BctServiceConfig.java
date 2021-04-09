package tech.tongyu.bct.server.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import tech.tongyu.bct.common.RedisListener;
import java.util.List;

@Configuration
@ComponentScan(basePackages = "tech.tongyu")
public class BctServiceConfig {
    private static final Logger logger = LoggerFactory.getLogger(BctServiceConfig.class);

    List<RedisListener> redisListeners;

    @Autowired
    public BctServiceConfig(List<RedisListener> redisListeners) {
        this.redisListeners = redisListeners;
    }

    @Bean
    @Autowired
    RedisMessageListenerContainer redisContainer(StringRedisTemplate stringRedisTemplate) {
        final RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(stringRedisTemplate.getConnectionFactory());
        redisListeners.forEach(listener->{
            listener.init();
            MessageListenerAdapter listenerAdapter = new MessageListenerAdapter(listener);
            listener.topics().forEach(topic->{
                logger.info("注册redis listener：{}",topic.getTopic());
                container.addMessageListener(listenerAdapter,topic);
            });
        });
        return container;
    }
}

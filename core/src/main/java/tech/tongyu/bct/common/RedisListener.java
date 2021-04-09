package tech.tongyu.bct.common;

import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;

import java.util.List;

public interface RedisListener extends MessageListener {
    List<ChannelTopic> topics();

    void init();
}

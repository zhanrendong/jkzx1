package tech.tongyu.bct.document.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;
import tech.tongyu.bct.common.RedisListener;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.document.dto.PositionDocumentDTO;
import tech.tongyu.bct.document.dto.TradeDocumentDTO;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static tech.tongyu.bct.document.service.TradeDocumentService.*;

@Component
public class TradeDocRedisMessageListener implements RedisListener {

    private static final Logger logger = LoggerFactory.getLogger(TradeDocRedisMessageListener.class);

    private StringRedisTemplate stringRedisTemplate;
    private TradeDocumentService tradeDocumentService;

    @Autowired
    public TradeDocRedisMessageListener(StringRedisTemplate stringRedisTemplate, TradeDocumentService tradeDocumentService) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.tradeDocumentService = tradeDocumentService;
    }

    @Override
    public List<ChannelTopic> topics() {
        return Arrays.asList(
                new ChannelTopic(tradeOpenTopic),
                new ChannelTopic(tradeSettleTopic)
        );
    }

    @Override
    public void init() {
        String context;
        try {
            context = stringRedisTemplate.opsForValue().get(tradeOpenTopic);
            if (context != null) {
                logger.info("初始化{}数据", tradeOpenTopic);
                saveTradeDocument(context);
            }
            context = stringRedisTemplate.opsForValue().get(tradeSettleTopic);
            if (context != null) {
                logger.info("初始化{}数据", tradeSettleTopic);
                savePositionDocument(context);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("解析数据时发生错误");
        }
    }

    @Override
    public void onMessage(final Message message, final byte[] pattern) {
        String context = (String) stringRedisTemplate.getValueSerializer().deserialize(message.getBody());
        String topic = new String(pattern);
        logger.debug("从Redis获得 {} 更新", topic);
        try {
            if (topic.equals(tradeOpenTopic)){
                saveTradeDocument(context);
            } else if (topic.equals(tradeSettleTopic)){
                savePositionDocument(context);
            }else throw new IllegalStateException(String.format("接受到错误的topic:%s", topic));
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException(String.format("解析topic:%s时发生错误", topic));
        }
    }

    private void saveTradeDocument(String context)throws IOException{
        TradeDocumentDTO tradeDocDto = JsonUtils.mapper.readValue(context, TradeDocumentDTO.class);
        tradeDocumentService.createTradeDoc(tradeDocDto);
    }
    private void savePositionDocument(String context)throws IOException{
        PositionDocumentDTO positionDocDto = JsonUtils.mapper.readValue(context, PositionDocumentDTO.class);
        tradeDocumentService.createPositionDoc(positionDocDto);
    }


}
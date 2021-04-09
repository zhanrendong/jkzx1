package tech.tongyu.bct.report.service;

import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import tech.tongyu.bct.common.RedisListener;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.report.dto.IntradayReportNotifyDTO;
import tech.tongyu.bct.report.dto.report.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static tech.tongyu.bct.report.service.IntradayReportService.*;

@Component
public class IntradayReportRedisMessageListener implements RedisListener {

    private static final Logger logger = LoggerFactory.getLogger(IntradayReportRedisMessageListener.class);

    private SimpMessagingTemplate messagingTemplate;

    private StringRedisTemplate stringRedisTemplate;

    private IntradayReportService intradayReportService;

    @Autowired
    public IntradayReportRedisMessageListener(SimpMessagingTemplate messagingTemplate,
                                              StringRedisTemplate stringRedisTemplate,
                                              IntradayReportService intradayReportService) {
        this.messagingTemplate = messagingTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
        this.intradayReportService = intradayReportService;
    }

    @Override
    public void onMessage(final Message message, final byte[] pattern) {
        String context = (String) stringRedisTemplate.getValueSerializer().deserialize(message.getBody());
        String topic = new String(pattern);
        logger.debug("从Redis获得 {} 更新", topic);
        try {
            if (topic.equals(intradayNotifyTopic)){
                notifyIntradayUpdateData(context);
            }else if (topic.equals(intradayTopic)){
                updateIntradayReportCache(context);
            } else if (topic.equals(tradeExpiringTopic)){
                updateTradeExpiringCache(context);
            }else if (topic.equals(tradeTopic)){
                updateTradeCache(context);
            } else if (topic.equals(riskTopic)) {
                updateRiskCache(context);
            } else if (topic.equals(pnlTopic)) {
                updatePnlCache(context);
            } else if (topic.equals(pnlHedgingTopic)){
                updatePnlHedgingCache(context);
            } else if (topic.equals(pnlOptionTopic)){
                updatePnlOptionCache(context);
            } else if (topic.equals(pnlTotalTopic)){
                updatePnlTotalCache(context);
            }
            else if(topic.equals(portfolioRiskTopic)){
                updatePortfolioRiskCache(context);
        }
        else throw new IllegalStateException(String.format("接受到错误的topic:%s", topic));
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException(String.format("解析topic:%s时发生错误", topic));
        }
    }

    @Override
    public List<ChannelTopic> topics() {
        return Arrays.asList(
                new ChannelTopic(intradayNotifyTopic),
                new ChannelTopic(intradayTopic),
                new ChannelTopic(tradeExpiringTopic),
                new ChannelTopic(tradeTopic),
                new ChannelTopic(riskTopic),
                new ChannelTopic(pnlTopic),
                new ChannelTopic(pnlHedgingTopic),
                new ChannelTopic(pnlOptionTopic),
                new ChannelTopic(pnlTotalTopic),
                new ChannelTopic(portfolioRiskTopic)
        );
    }

    @Override
    public void init() {
        String context;
        try {
            context = stringRedisTemplate.opsForValue().get(intradayTopic);
            if (context != null) {
                logger.info("初始化{}数据", intradayTopic);
                updateIntradayReportCache(context);
            }
            context = stringRedisTemplate.opsForValue().get(tradeExpiringTopic);
            if (context != null) {
                logger.info("初始化{}数据", tradeExpiringTopic);
                updateTradeExpiringCache(context);
            }
            context = stringRedisTemplate.opsForValue().get(tradeTopic);
            if (context != null) {
                logger.info("初始化{}数据", tradeTopic);
                updateTradeCache(context);
            }
            context = stringRedisTemplate.opsForValue().get(riskTopic);
            if (context != null) {
                logger.info("初始化{}数据", riskTopic);
                updateRiskCache(context);
            }
            context = stringRedisTemplate.opsForValue().get(pnlTopic);
            if (context != null) {
                logger.info("初始化{}数据", pnlTopic);
                updatePnlCache(context);
            }
            context = stringRedisTemplate.opsForValue().get(pnlHedgingTopic);
            if (context != null) {
                logger.info("初始化{}数据", pnlHedgingTopic);
                updatePnlHedgingCache(context);
            }
            context = stringRedisTemplate.opsForValue().get(pnlOptionTopic);
            if (context != null) {
                logger.info("初始化{}数据", pnlOptionTopic);
                updatePnlOptionCache(context);
            }
            context = stringRedisTemplate.opsForValue().get(pnlTotalTopic);
            if (context != null) {
                logger.info("初始化{}数据", pnlTotalTopic);
                updatePnlTotalCache(context);
            }
            context = stringRedisTemplate.opsForValue().get(portfolioRiskTopic);
            if (context != null) {
                logger.info("初始化{}数据", portfolioRiskTopic);
                updatePortfolioRiskCache(context);
            }


        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("解析数据时发生错误");
        }
    }

    private void notifyIntradayUpdateData(String context)throws IOException{
        logger.debug("报告数据已经更新" + LocalDateTime.now().toString());
        IntradayReportNotifyDTO notify = JsonUtils.mapper.readValue(context, IntradayReportNotifyDTO.class);
        messagingTemplate.convertAndSend(intradayNotifyChannel, notify);
    }

    private void updateIntradayReportCache(String context)throws IOException{
        List<GenericIntradayReportRowDTO> genericReports =
                JsonUtils.mapper.readValue(context, new TypeReference<List<GenericIntradayReportRowDTO>>() {});
        intradayReportService.saveGenericIntradayReport(genericReports);
    }
    private void updateTradeExpiringCache(String context)throws IOException{
        List<PositionExpiringReportRowDTO> expirationPositions =
                JsonUtils.mapper.readValue(context, new TypeReference<List<PositionExpiringReportRowDTO>>() {});
        intradayReportService.saveIntradayTradeExpiringReport(expirationPositions);
    }

    private void updateTradeCache(String context) throws IOException {
        List<PositionReportRowDTO> positions =
                JsonUtils.mapper.readValue(context, new TypeReference<List<PositionReportRowDTO>>() {
                });
        intradayReportService.saveIntradayTradeReport(positions);
    }

    private void updateRiskCache(String context) throws IOException {
        List<RiskReportRowDTO> risks =
                JsonUtils.mapper.readValue(context, new TypeReference<List<RiskReportRowDTO>>() {
                });
        intradayReportService.saveIntradayRiskReport(risks);
    }

    private void updatePnlCache(String context) throws IOException {
        List<PnlReportRowDTO> pnls =
                JsonUtils.mapper.readValue(context, new TypeReference<List<PnlReportRowDTO>>() {
                });
        intradayReportService.saveIntradayPnlReport(pnls);
    }


    /** 之前的场内,场外,总盈亏,保留,暂时不用 */
    private void updatePnlTotalCache(String context) throws IOException {
        List<PnlReportTotalRowDTO> pnlTotals =
                JsonUtils.mapper.readValue(context, new TypeReference<List<PnlReportTotalRowDTO>>() {
                });
        intradayReportService.saveIntradayPnlTotalReport(pnlTotals);
    }

    private void updatePnlOptionCache(String context) throws IOException {
        List<PnlReportOptionRowDTO> pnlOptions =
                JsonUtils.mapper.readValue(context, new TypeReference<List<PnlReportOptionRowDTO>>() {
                });
        intradayReportService.saveIntradayPnlOptionReport(pnlOptions);
    }

    private void updatePnlHedgingCache(String context) throws IOException {
        List<PnlReportHedgingRowDTO> pnlHedgings =
                JsonUtils.mapper.readValue(context, new TypeReference<List<PnlReportHedgingRowDTO>>() {
                });
        intradayReportService.saveIntradayPnlHedgingReport(pnlHedgings);
    }
    private void updatePortfolioRiskCache(String context) throws IOException{
        List<PortfolioRiskReportRowDTO> pflReports=
                JsonUtils.mapper.readValue(context, new TypeReference<List<PortfolioRiskReportRowDTO>>() {
                });
        intradayReportService.saveIntradayPortfolioRiskReport(pflReports);
    }

}
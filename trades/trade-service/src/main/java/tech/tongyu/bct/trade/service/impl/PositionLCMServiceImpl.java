package tech.tongyu.bct.trade.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.tongyu.bct.cm.trade.impl.BctTradePosition;
import tech.tongyu.bct.cm.trade.LCMEventTypeEnum;
import tech.tongyu.bct.trade.dto.lcm.LCMNotificationInfoDTO;
import tech.tongyu.bct.trade.manager.PositionManager;
import tech.tongyu.bct.trade.service.LCMCompositeProcessor;
import tech.tongyu.bct.trade.service.PositionLCMService;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PositionLCMServiceImpl implements PositionLCMService {

    List<LCMCompositeProcessor> assetLCMProcessors;

    PositionManager tradePositionManager;

    @Autowired
    public PositionLCMServiceImpl(List<LCMCompositeProcessor> assetLCMProcessors, PositionManager tradePositionManager) {
        this.assetLCMProcessors = assetLCMProcessors;
        this.tradePositionManager = tradePositionManager;
    }

    @Override
    public List<LCMNotificationInfoDTO> generatePositionLCMEvents(BctTradePosition position) {
        List<LCMNotificationInfoDTO> collect = assetLCMProcessors
                .parallelStream()
                .flatMap(processor ->
                        processor.notifications(position.asset)
                                .stream()
                ).collect(Collectors.toList());
        return collect;
    }


    @Override
    public List<LCMEventTypeEnum> getSupportedPositionLCMEventType(String positionId, OffsetDateTime validTime) {
        OffsetDateTime transactionTime = OffsetDateTime.now();
        BctTradePosition bctPosition = tradePositionManager.getByPositionId(positionId, validTime, transactionTime).get();
        return Arrays.asList(bctPosition)
                .stream()
                .flatMap(pos -> getSupportedPositionLCMEventType(pos).stream())
                .collect(Collectors.toList());
    }

    @Override
    public List<LCMEventTypeEnum> getSupportedPositionLCMEventType(BctTradePosition position) {
        return assetLCMProcessors.stream()
                .filter(p -> p.productTypes().contains(position.asset.instrumentOfValue().productType()))
                .flatMap(p -> p.eventMap().values().stream())
                .flatMap(p -> p.stream())
                .collect(Collectors.toSet())
                .stream()
                .collect(Collectors.toList());
    }


}

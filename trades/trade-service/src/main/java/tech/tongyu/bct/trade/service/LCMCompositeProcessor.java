package tech.tongyu.bct.trade.service;

import tech.tongyu.bct.cm.product.asset.Asset;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValue;
import tech.tongyu.bct.cm.product.iov.ProductTypeEnum;
import tech.tongyu.bct.cm.trade.LCMEventTypeEnum;
import tech.tongyu.bct.cm.trade.impl.BctTradePosition;
import tech.tongyu.bct.trade.dto.lcm.LCMNotificationInfoDTO;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface LCMCompositeProcessor {
    List<ProductTypeEnum> productTypes();

    List<LCMEventTypeEnum> eventTypes();

    List<LCMProcessor> processors();

    default List<LCMProcessor> process(BctTradePosition position, LCMEventTypeEnum eventTypeEnum){
        InstrumentOfValue instrument = position.asset.instrumentOfValue();
        if (productTypes().contains(instrument.productType()) ) {
            return processorsByEvent(eventTypeEnum);
        } else {
            return Collections.emptyList();
        }
    }

    default Map<ProductTypeEnum,List<LCMEventTypeEnum>> eventMap() {
        return productTypes().stream().collect(Collectors.toMap(pt->pt, pt->eventTypes()));
    }

    default List<LCMNotificationInfoDTO> notifications(Asset<InstrumentOfValue> asset) {
        return processors().parallelStream()
                .filter(p -> productTypes().contains(asset.instrumentOfValue().productType()))
                .flatMap(p -> p.notifications(asset).stream()).collect(Collectors.toList());
    }

    default List<LCMProcessor> processorsByEvent(LCMEventTypeEnum eventTypeEnum) {
        return processors().stream()
                .filter(p -> p.eventType().equals(eventTypeEnum)).collect(Collectors.toList());
    }
}
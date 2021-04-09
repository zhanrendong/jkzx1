package tech.tongyu.bct.trade.service.impl.lcm.composite;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.tongyu.bct.cm.product.iov.ProductTypeEnum;
import tech.tongyu.bct.cm.trade.LCMEventTypeEnum;
import tech.tongyu.bct.trade.service.LCMCompositeProcessor;
import tech.tongyu.bct.trade.service.LCMProcessor;
import tech.tongyu.bct.trade.service.impl.lcm.CashFlowExerciseProcessor;
import tech.tongyu.bct.trade.service.impl.lcm.PositionAmendProcessor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CashFlowLCMProcessor implements LCMCompositeProcessor {

    private PositionAmendProcessor positionAmendProcessor;
    private CashFlowExerciseProcessor cashFlowExerciseProcessor;

    @Autowired
    public CashFlowLCMProcessor(CashFlowExerciseProcessor cashFlowExerciseProcessor,
                                PositionAmendProcessor positionAmendProcessor) {
        this.cashFlowExerciseProcessor = cashFlowExerciseProcessor;
        this.positionAmendProcessor = positionAmendProcessor;
    }

    @Override
    public List<ProductTypeEnum> productTypes() {
        return Arrays.asList(ProductTypeEnum.CASH_FLOW);
    }

    @Override
    public List<LCMEventTypeEnum> eventTypes() {
        return processors().stream().map(p -> p.eventType()).collect(Collectors.toList());
    }

    @Override
    public List<LCMProcessor> processors() {
        return Arrays.asList(positionAmendProcessor, cashFlowExerciseProcessor);
    }


}

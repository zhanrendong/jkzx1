package tech.tongyu.bct.trade.service.impl.lcm.composite;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.tongyu.bct.cm.product.iov.ProductTypeEnum;
import tech.tongyu.bct.cm.trade.LCMEventTypeEnum;
import tech.tongyu.bct.trade.service.LCMCompositeProcessor;
import tech.tongyu.bct.trade.service.LCMProcessor;
import tech.tongyu.bct.trade.service.impl.lcm.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class RangeAccrualsOptionLCMProcessor implements LCMCompositeProcessor {

    private UnwindProcessor unwindProcessor;
    private RollOptionProcessor rollOptionProcessor;
    private ExpirationProcessor expirationProcessor;
    private ObservationProcessor observationProcessor;
    private PositionAmendProcessor positionAmendProcessor;
    private PaymentOptionProcessor paymentOptionProcessor;
    private RangeAccrualsExerciseProcessor exerciseProcessor;

    @Autowired
    public RangeAccrualsOptionLCMProcessor(UnwindProcessor unwindProcessor,
                                           RollOptionProcessor rollOptionProcessor,
                                           ExpirationProcessor expirationProcessor,
                                           ObservationProcessor observationProcessor,
                                           PositionAmendProcessor positionAmendProcessor,
                                           PaymentOptionProcessor paymentOptionProcessor,
                                           RangeAccrualsExerciseProcessor exerciseProcessor) {
        this.unwindProcessor = unwindProcessor;
        this.exerciseProcessor = exerciseProcessor;
        this.rollOptionProcessor = rollOptionProcessor;
        this.expirationProcessor = expirationProcessor;
        this.observationProcessor = observationProcessor;
        this.positionAmendProcessor = positionAmendProcessor;
        this.paymentOptionProcessor = paymentOptionProcessor;
    }


    @Override
    public List<ProductTypeEnum> productTypes() {
        return Arrays.asList(ProductTypeEnum.RANGE_ACCRUALS);
    }

    @Override
    public List<LCMEventTypeEnum> eventTypes() {
        return processors().stream().map(p -> p.eventType()).collect(Collectors.toList());
    }

    @Override
    public List<LCMProcessor> processors() {
        return Arrays.asList(
                unwindProcessor,
                exerciseProcessor,
                rollOptionProcessor,
                expirationProcessor,
                observationProcessor,
                paymentOptionProcessor,
                positionAmendProcessor);
    }
}

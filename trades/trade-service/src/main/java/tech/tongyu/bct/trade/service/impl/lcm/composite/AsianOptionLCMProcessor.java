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
public class AsianOptionLCMProcessor implements LCMCompositeProcessor {

    private UnwindProcessor unwindProcessor;
    private RollOptionProcessor rollOptionProcessor;
    private ExpirationProcessor expirationProcessor;
    private ObservationProcessor observationProcessor;
    private AsianExerciseProcessor asianExerciseProcessor;
    private PositionAmendProcessor positionAmendProcessor;
    private PaymentOptionProcessor paymentOptionProcessor;

    @Autowired
    public AsianOptionLCMProcessor(UnwindProcessor unwindProcessor,
                                   RollOptionProcessor rollOptionProcessor,
                                   ExpirationProcessor expirationProcessor,
                                   ObservationProcessor observationProcessor,
                                   AsianExerciseProcessor asianExerciseProcessor,
                                   PositionAmendProcessor positionAmendProcessor,
                                   PaymentOptionProcessor paymentOptionProcessor) {
        this.unwindProcessor = unwindProcessor;
        this.rollOptionProcessor = rollOptionProcessor;
        this.expirationProcessor = expirationProcessor;
        this.observationProcessor = observationProcessor;
        this.asianExerciseProcessor = asianExerciseProcessor;
        this.positionAmendProcessor = positionAmendProcessor;
        this.paymentOptionProcessor = paymentOptionProcessor;
    }

    @Override
    public List<ProductTypeEnum> productTypes() {
        return Arrays.asList(ProductTypeEnum.ASIAN);
    }

    @Override
    public List<LCMEventTypeEnum> eventTypes() {
        return processors().stream().map(p -> p.eventType()).collect(Collectors.toList());
    }

    @Override
    public List<LCMProcessor> processors() {
        return Arrays.asList(
                unwindProcessor,
                rollOptionProcessor,
                expirationProcessor,
                observationProcessor,
                asianExerciseProcessor,
                paymentOptionProcessor,
                positionAmendProcessor);
    }

}

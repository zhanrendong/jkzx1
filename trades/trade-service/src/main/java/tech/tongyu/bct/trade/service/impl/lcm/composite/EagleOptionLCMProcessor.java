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
public class EagleOptionLCMProcessor implements LCMCompositeProcessor {

    private UnwindProcessor unwindProcessor;
    private RollOptionProcessor rollOptionProcessor;
    private ExpirationProcessor expirationProcessor;
    private EagleExerciseProcessor eagleExerciseProcessor;
    private PositionAmendProcessor positionAmendProcessor;
    private PaymentOptionProcessor paymentOptionProcessor;


    @Autowired
    public EagleOptionLCMProcessor(UnwindProcessor unwindProcessor,
                                   RollOptionProcessor rollOptionProcessor,
                                   ExpirationProcessor expirationProcessor,
                                   PositionAmendProcessor positionAmendProcessor,
                                   EagleExerciseProcessor eagleExerciseProcessor,
                                   PaymentOptionProcessor paymentOptionProcessor) {
        this.unwindProcessor = unwindProcessor;
        this.rollOptionProcessor = rollOptionProcessor;
        this.expirationProcessor = expirationProcessor;
        this.positionAmendProcessor = positionAmendProcessor;
        this.paymentOptionProcessor = paymentOptionProcessor;
        this.eagleExerciseProcessor = eagleExerciseProcessor;
    }

    @Override
    public List<ProductTypeEnum> productTypes() {
        return Arrays.asList(ProductTypeEnum.EAGLE);
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
                positionAmendProcessor,
                paymentOptionProcessor,
                eagleExerciseProcessor);
    }

}

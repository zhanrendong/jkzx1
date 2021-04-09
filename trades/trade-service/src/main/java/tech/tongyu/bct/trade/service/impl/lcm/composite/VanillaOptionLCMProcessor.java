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
public class VanillaOptionLCMProcessor implements LCMCompositeProcessor {

    private UnwindProcessor unwindProcessor;
    private RollOptionProcessor rollOptionProcessor;
    private ExpirationProcessor expirationProcessor;
    private VanillaExerciseProcessor exerciseProcessor;
    private PositionAmendProcessor positionAmendProcessor;
    private PaymentOptionProcessor paymentOptionProcessor;

    @Autowired
    public VanillaOptionLCMProcessor(UnwindProcessor unwindProcessor,
                                     RollOptionProcessor rollOptionProcessor,
                                     ExpirationProcessor expirationProcessor,
                                     VanillaExerciseProcessor exerciseProcessor,
                                     PaymentOptionProcessor paymentOptionProcessor,
                                     PositionAmendProcessor positionAmendProcessor) {
        this.unwindProcessor = unwindProcessor;
        this.exerciseProcessor = exerciseProcessor;
        this.rollOptionProcessor = rollOptionProcessor;
        this.expirationProcessor = expirationProcessor;
        this.paymentOptionProcessor = paymentOptionProcessor;
        this.positionAmendProcessor = positionAmendProcessor;
    }

    @Override
    public List<ProductTypeEnum> productTypes() {
        return Arrays.asList(ProductTypeEnum.VANILLA_AMERICAN, ProductTypeEnum.VANILLA_EUROPEAN);
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
                expirationProcessor,
                rollOptionProcessor,
                paymentOptionProcessor,
                positionAmendProcessor);
    }

}

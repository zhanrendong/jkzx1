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
public class TripleDigitalOptionLCMProcessor implements LCMCompositeProcessor {

    private UnwindProcessor unwindProcessor;
    private RollOptionProcessor rollOptionProcessor;
    private ExpirationProcessor expirationProcessor;
    private PositionAmendProcessor positionAmendProcessor;
    private PaymentOptionProcessor paymentOptionProcessor;
    private TripleDigitalExerciseProcessor tripleDigitalExerciseProcessor;

    @Autowired
    public TripleDigitalOptionLCMProcessor(UnwindProcessor unwindProcessor,
                                           RollOptionProcessor rollOptionProcessor,
                                           ExpirationProcessor expirationProcessor,
                                           PositionAmendProcessor positionAmendProcessor,
                                           PaymentOptionProcessor paymentOptionProcessor,
                                           TripleDigitalExerciseProcessor tripleDigitalExerciseProcessor) {
        this.unwindProcessor = unwindProcessor;
        this.rollOptionProcessor = rollOptionProcessor;
        this.expirationProcessor = expirationProcessor;
        this.positionAmendProcessor = positionAmendProcessor;
        this.paymentOptionProcessor = paymentOptionProcessor;
        this.tripleDigitalExerciseProcessor = tripleDigitalExerciseProcessor;
    }

    @Override
    public List<ProductTypeEnum> productTypes() {
        return Arrays.asList(ProductTypeEnum.TRIPLE_DIGITAL);
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
                tripleDigitalExerciseProcessor);
    }
}

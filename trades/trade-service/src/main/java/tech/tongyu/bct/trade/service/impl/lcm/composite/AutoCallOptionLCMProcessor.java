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
public class AutoCallOptionLCMProcessor implements LCMCompositeProcessor {

    private UnwindProcessor unwindProcessor;
    private RollOptionProcessor rollOptionProcessor;
    private AutoCallKnockOutProcessor knockOutProcessor;
    private PositionAmendProcessor positionAmendProcessor;
    private PaymentOptionProcessor paymentOptionProcessor;
    private AutoCallExpirationExerciseProcessor expirationExerciseProcessor;

    @Autowired
    public AutoCallOptionLCMProcessor(UnwindProcessor unwindProcessor,
                                      RollOptionProcessor rollOptionProcessor,
                                      AutoCallKnockOutProcessor knockOutProcessor,
                                      PositionAmendProcessor positionAmendProcessor,
                                      PaymentOptionProcessor paymentOptionProcessor,
                                      AutoCallExpirationExerciseProcessor expirationExerciseProcessor) {
        this.expirationExerciseProcessor = expirationExerciseProcessor;
        this.positionAmendProcessor = positionAmendProcessor;
        this.paymentOptionProcessor = paymentOptionProcessor;
        this.rollOptionProcessor = rollOptionProcessor;
        this.knockOutProcessor = knockOutProcessor;
        this.unwindProcessor = unwindProcessor;
    }

    @Override
    public List<ProductTypeEnum> productTypes() {
        return Arrays.asList(ProductTypeEnum.AUTOCALL);
    }

    @Override
    public List<LCMEventTypeEnum> eventTypes() {
        return processors().stream().map(p -> p.eventType()).collect(Collectors.toList());
    }

    @Override
    public List<LCMProcessor> processors() {
        return Arrays.asList(
                unwindProcessor,
                knockOutProcessor,
                rollOptionProcessor,
                positionAmendProcessor,
                paymentOptionProcessor,
                expirationExerciseProcessor);
    }


}

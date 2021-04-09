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
public class AutoCallPhoenixOptionLCMProcessor implements LCMCompositeProcessor {

    private UnwindProcessor unwindProcessor;
    private KnockInProcessor knockInProcessor;
    private RollOptionProcessor rollOptionProcessor;
    private ObservationProcessor observationProcessor;
    private PositionAmendProcessor positionAmendProcessor;
    private PaymentOptionProcessor paymentOptionProcessor;
    private AutoCallPhoenixKnockOutProcessor phoenixKnockOutProcessor;
    private AutoCallPhoenixExpirationExerciseProcessor expirationExerciseProcessor;

    @Autowired
    public AutoCallPhoenixOptionLCMProcessor(UnwindProcessor unwindProcessor,
                                             KnockInProcessor knockInProcessor,
                                             RollOptionProcessor rollOptionProcessor,
                                             ObservationProcessor observationProcessor,
                                             PositionAmendProcessor positionAmendProcessor,
                                             PaymentOptionProcessor paymentOptionProcessor,
                                             AutoCallPhoenixKnockOutProcessor phoenixKnockOutProcessor,
                                             AutoCallPhoenixExpirationExerciseProcessor expirationExerciseProcessor) {

        this.unwindProcessor = unwindProcessor;
        this.knockInProcessor = knockInProcessor;
        this.rollOptionProcessor = rollOptionProcessor;
        this.observationProcessor = observationProcessor;
        this.positionAmendProcessor = positionAmendProcessor;
        this.paymentOptionProcessor = paymentOptionProcessor;
        this.phoenixKnockOutProcessor = phoenixKnockOutProcessor;
        this.expirationExerciseProcessor = expirationExerciseProcessor;
    }

    @Override
    public List<ProductTypeEnum> productTypes() {
        return Arrays.asList(ProductTypeEnum.AUTOCALL_PHOENIX);
    }

    @Override
    public List<LCMEventTypeEnum> eventTypes() {
        return processors().stream().map(p -> p.eventType()).collect(Collectors.toList());
    }

    @Override
    public List<LCMProcessor> processors() {
        return Arrays.asList(
                unwindProcessor,
                knockInProcessor,
                rollOptionProcessor,
                observationProcessor,
                positionAmendProcessor,
                paymentOptionProcessor,
                phoenixKnockOutProcessor,
                expirationExerciseProcessor);
    }


}

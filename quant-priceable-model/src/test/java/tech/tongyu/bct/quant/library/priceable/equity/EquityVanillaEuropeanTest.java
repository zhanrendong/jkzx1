package tech.tongyu.bct.quant.library.priceable.equity;

import org.junit.Assert;
import org.junit.Test;
import tech.tongyu.bct.quant.library.priceable.common.flag.ExerciseTypeEnum;
import tech.tongyu.bct.quant.library.priceable.common.flag.OptionTypeEnum;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;

public class EquityVanillaEuropeanTest {
    @Test
    public void stockUnderlyerTest() {
        EquityStock stock = new EquityStock("000002.SZ");
        EquityVanillaEuropean<EquityStock> option = new EquityVanillaEuropean<>(stock, 100,
                LocalDateTime.of(2019, 12, 15, 15, 0), OptionTypeEnum.CALL);
        Assert.assertEquals(100., option.getStrike(), 1e-16);
        Assert.assertEquals(LocalDate.of(2019,12,15), option.getExpirationDate());
        Assert.assertEquals(ExerciseTypeEnum.EUROPEAN, option.getExerciseType());
    }
}

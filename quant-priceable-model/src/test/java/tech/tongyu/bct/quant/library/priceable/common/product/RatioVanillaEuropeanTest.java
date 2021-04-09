package tech.tongyu.bct.quant.library.priceable.common.product;

import org.junit.Assert;
import org.junit.Test;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.quant.library.priceable.common.flag.OptionTypeEnum;
import tech.tongyu.bct.quant.library.priceable.common.product.basket.RatioVanillaEuropean;
import tech.tongyu.bct.quant.library.priceable.equity.EquityStock;

import java.time.LocalDateTime;

public class RatioVanillaEuropeanTest {
    @Test
    public void testSerialization() throws Exception {
        EquityStock stock1 = new EquityStock("A");
        EquityStock stock2 = new EquityStock("B");
        LocalDateTime expiry = LocalDateTime.of(2019, 4, 30, 0, 0);
        double strike = 100.;
        RatioVanillaEuropean option = new RatioVanillaEuropean(stock1, stock2, expiry,
                strike, OptionTypeEnum.CALL, expiry.toLocalDate());
        String json = JsonUtils.objectToJsonString(option);
        RatioVanillaEuropean recovered = JsonUtils.mapper.readValue(json, RatioVanillaEuropean.class);
        Assert.assertEquals(option.getExpiry(), recovered.getExpiry());
    }
}

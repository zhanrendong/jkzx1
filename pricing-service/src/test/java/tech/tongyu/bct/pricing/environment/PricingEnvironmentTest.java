package tech.tongyu.bct.pricing.environment;

import org.junit.Test;
import tech.tongyu.bct.common.util.DateTimeUtils;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.pricing.common.config.PricingConfig;
import tech.tongyu.bct.pricing.common.config.impl.OptionOnSpotPricingConfig;
import tech.tongyu.bct.pricing.common.descriptor.PositionDescriptor;
import tech.tongyu.bct.quant.library.priceable.Position;
import tech.tongyu.bct.quant.library.priceable.Priceable;
import tech.tongyu.bct.quant.library.priceable.common.flag.OptionTypeEnum;
import tech.tongyu.bct.quant.library.priceable.equity.EquityStock;
import tech.tongyu.bct.quant.library.priceable.equity.EquityVanillaEuropean;
import tech.tongyu.bct.quant.library.priceable.equity.EquityVerticalSpread;
import tech.tongyu.bct.quant.library.priceable.feature.HasUnderlyer;

import java.time.LocalDateTime;

import static org.junit.Assert.*;

public class PricingEnvironmentTest {

    @Test
    public void plan() throws Exception {
        LocalDateTime expiry  = LocalDateTime.of(2019, 12, 17, 0, 0);
        EquityStock stock = new EquityStock("000002.SZ");
        EquityVanillaEuropean<EquityStock> option = new EquityVanillaEuropean<>(stock, 100,
                expiry, OptionTypeEnum.CALL);
        String json = JsonUtils.objectToJsonString(option);
        Priceable priceable = JsonUtils.mapper.readValue(json, Priceable.class);
        assertEquals(stock.getInstrumentId(),
                ((EquityStock)((HasUnderlyer)priceable).getUnderlyer()).getInstrumentId());

        Position call = new Position("test", 1.0, option);
        PricingConfig config = PricingEnvironment
                .getDefaultPricingEnvironment(null)
                .plan(PositionDescriptor.getPositionDescriptor(call),
                        LocalDateTime.of(2018,12,17,9,0,0),
                        DateTimeUtils.BCT_DEFAULT_TIMEZONE);
        assertTrue(config instanceof OptionOnSpotPricingConfig);
        assertEquals("BlackAnalytic", ((OptionOnSpotPricingConfig)config).getPricer().getPricerName());

        // call spread
        EquityVerticalSpread<EquityStock> spread = new EquityVerticalSpread<>(stock,
                90, 105, expiry, OptionTypeEnum.CALL);
        json = JsonUtils.objectToJsonString(spread);
        Priceable recovered = JsonUtils.mapper.readValue(json, Priceable.class);
        assertEquals(stock.getInstrumentId(),
                ((EquityStock)((HasUnderlyer)recovered).getUnderlyer()).getInstrumentId());
    }
}
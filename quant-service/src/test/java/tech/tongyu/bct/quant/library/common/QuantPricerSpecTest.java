package tech.tongyu.bct.quant.library.common;

import org.junit.Test;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.quant.library.common.impl.*;

import static org.junit.Assert.assertEquals;

public class QuantPricerSpecTest {
    @Test
    public void testBlackAnalytic() throws Exception {
        BlackAnalyticPricerParams params = new BlackAnalyticPricerParams();
        BlackScholesAnalyticPricer pricer = new BlackScholesAnalyticPricer(params);
        String json = JsonUtils.objectToJsonString(pricer);
        QuantPricerSpec recovered = JsonUtils.mapper.readValue(json, QuantPricerSpec.class);
        assertEquals(recovered.getPricerName(), pricer.getPricerName());
        assertEquals(365., ((BlackAnalyticPricerParams)recovered.getPricerParams()).getDaysInYear(),
                DoubleUtils.SMALL_NUMBER);
    }

    @Test
    public void testBlackMc() throws Exception {
        BlackMcPricerParams params = new BlackMcPricerParams(1234L, 10000,
                7./365., false, false, false);
        BlackMcPricer pricer = new BlackMcPricer(params);
        String json = JsonUtils.objectToJsonString(pricer);
        QuantPricerSpec recovered = JsonUtils.mapper.readValue(json, QuantPricerSpec.class);
        assertEquals(recovered.getPricerName(), pricer.getPricerName());
        assertEquals(((BlackMcPricerParams)recovered.getPricerParams()).getNumPaths(), 10000);
    }

    @Test
    public void testModelXY() throws Exception {
        String modelName = "CUSTOME_MODEL_XY";
        ModelXYPricerParams params = new ModelXYPricerParams(modelName);
        ModelXYPricer pricer = new ModelXYPricer(params);
        String json = JsonUtils.objectToJsonString(pricer);
        QuantPricerSpec recovered = JsonUtils.mapper.readValue(json, QuantPricerSpec.class);
        assertEquals(modelName, ((ModelXYPricerParams)recovered.getPricerParams()).getModelId());
    }
}

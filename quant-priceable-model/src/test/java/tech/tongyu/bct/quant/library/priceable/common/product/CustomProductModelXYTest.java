package tech.tongyu.bct.quant.library.priceable.common.product;

import org.junit.Assert;
import org.junit.Test;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.quant.library.priceable.Priceable;

import java.time.LocalDateTime;

public class CustomProductModelXYTest {
    @Test
    public void testSerialization() throws Exception {
        CustomProductModelXY productModelXY = new CustomProductModelXY("test",
                LocalDateTime.of(2119, 3, 25, 15, 0));
        String json = JsonUtils.objectToJsonString(productModelXY);
        System.out.println(json);
        Priceable priceable = JsonUtils.mapper.readValue(json, Priceable.class);
        Assert.assertEquals("test", ((CustomProductModelXY)priceable).getUnderlyerInstrumentId());
    }
}

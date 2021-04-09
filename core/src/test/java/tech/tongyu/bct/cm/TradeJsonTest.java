package tech.tongyu.bct.cm;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.tongyu.bct.cm.core.BusinessCenterEnum;
import tech.tongyu.bct.cm.product.iov.cmd.impl.CommodityFutureInstrument;
import tech.tongyu.bct.cm.product.iov.eqd.impl.ListedEquityBasketInstrument;
import tech.tongyu.bct.cm.product.iov.eqd.impl.ListedEquityInstrument;
import tech.tongyu.bct.cm.reference.impl.CurrencyUnit;
import tech.tongyu.bct.cm.reference.impl.Percent;
import tech.tongyu.bct.cm.reference.impl.UnitOfValue;
import tech.tongyu.bct.cm.trade.impl.BctTrade;
import tech.tongyu.bct.common.util.JsonUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class TradeJsonTest {
    private static final Logger logger = LoggerFactory.getLogger(TradeJsonTest.class);



    @Test
    public void testJsonfyAnnulizedOptionTrade() {
        CurrencyUnit currency = TestTradeFactory.currency;
        BusinessCenterEnum venue = TestTradeFactory.venue;
        ListedEquityInstrument underlyer =
                new ListedEquityInstrument("600519", "贵州茅台", TestTradeFactory.venue);
        UnitOfValue<BigDecimal> strike = new UnitOfValue(currency, BigDecimal.valueOf(120000.0));
        BctTrade trade = TestTradeFactory.createAnnulizedOptionTrade(underlyer, strike.value(),BigDecimal.valueOf(100));
        serializeAndDeserializeAndAssert(trade);

        //test percentage unit
        strike = new UnitOfValue(Percent.get(), BigDecimal.valueOf(120.0));
        trade = TestTradeFactory.createAnnulizedOptionTrade(underlyer, strike.value(),BigDecimal.valueOf(100));
        serializeAndDeserializeAndAssert(trade);

        //test basket instrument
        ListedEquityInstrument underlyer2 =
                new ListedEquityInstrument("123456", "ABCD", TestTradeFactory.venue);
        ListedEquityBasketInstrument basket = new ListedEquityBasketInstrument(
                "basket", currency, TestTradeFactory.venue, Arrays.asList(underlyer, underlyer2));
        trade = TestTradeFactory.createAnnulizedOptionTrade(basket, strike.value(),BigDecimal.valueOf(100));
        serializeAndDeserializeAndAssert(trade);


        //test basket instrument
        CommodityFutureInstrument underlyer3 =
                new CommodityFutureInstrument("123456");
        trade = TestTradeFactory.createAnnulizedOptionTrade(basket, strike.value(),BigDecimal.valueOf(100));
        serializeAndDeserializeAndAssert(trade);

        CommodityFutureInstrument underlyer4 =
                new CommodityFutureInstrument("123456");
        trade = TestTradeFactory.createAnnulizedOptionTrade(basket, strike.value(),BigDecimal.valueOf(100));
        serializeAndDeserializeAndAssert(trade);


        CommodityFutureInstrument underlyer5 =
                new CommodityFutureInstrument("123456");
        trade = TestTradeFactory.createAnnulizedDigitalOptionTrade(basket, strike.value(),BigDecimal.valueOf(100));
        serializeAndDeserializeAndAssert(trade);


    }



    private <T> void serializeAndDeserializeAndAssert(T object) {
        try {
            String json = JsonUtils.mapper.writeValueAsString(object);
            logger.info(String.format("original json:\n%s", json));
            T newObject = (T) JsonUtils.mapper.readValue(json, object.getClass());
            String json2 = JsonUtils.mapper.writeValueAsString(newObject);
            logger.info(String.format("converted json:\n%s", json));
            assertEquals(String.format("serialize/deserialize %s should equal", object.getClass()), json2, json);
        } catch (IOException e) {
            e.printStackTrace();
            assertTrue("errors occurs in serialize/deserialize", false);
        }
    }

    //--------------------------------------------*****---------------------------------------------------

    public void testJsonTrans(){

    }



}

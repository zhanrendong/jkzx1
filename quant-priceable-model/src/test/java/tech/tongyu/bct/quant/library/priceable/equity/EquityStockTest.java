package tech.tongyu.bct.quant.library.priceable.equity;

import org.junit.Test;
import tech.tongyu.bct.quant.library.priceable.Priceable;

import static org.junit.Assert.*;
import static tech.tongyu.bct.quant.library.priceable.feature.ExchangeListed.InstrumentTypeEnum.EQUITY_STOCK;

public class EquityStockTest {
    private final String instrumentId = "000002.SZ";

    @Test
    public void test() {
        EquityStock stock = new EquityStock(this.instrumentId);
        assertEquals(instrumentId, stock.getInstrumentId());
        assertEquals(Priceable.PriceableTypeEnum.EQUITY_STOCK, stock.getPriceableTypeEnum());
        assertEquals(EQUITY_STOCK, stock.getInstrumentType());
    }
}
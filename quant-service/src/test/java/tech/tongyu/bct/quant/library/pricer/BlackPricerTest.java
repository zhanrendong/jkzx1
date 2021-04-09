package tech.tongyu.bct.quant.library.pricer;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import tech.tongyu.bct.common.util.DateTimeUtils;
import tech.tongyu.bct.quant.library.common.CalcTypeEnum;
import tech.tongyu.bct.quant.library.common.DoubleUtils;
import tech.tongyu.bct.quant.library.common.QuantlibCalcResults;
import tech.tongyu.bct.quant.library.common.impl.BlackAnalyticPricerParams;
import tech.tongyu.bct.quant.library.common.impl.BlackResults;
import tech.tongyu.bct.quant.library.common.impl.BlackScholesAnalyticPricer;
import tech.tongyu.bct.quant.library.financial.date.Holidays;
import tech.tongyu.bct.quant.library.market.curve.DiscountingCurve;
import tech.tongyu.bct.quant.library.market.curve.impl.PwlfDiscountingCurve;
import tech.tongyu.bct.quant.library.market.vol.impl.AtmPwcVolSurface;
import tech.tongyu.bct.quant.library.numerics.black.BlackScholes;
import tech.tongyu.bct.quant.library.numerics.black.Digital;
import tech.tongyu.bct.quant.library.priceable.common.flag.OptionTypeEnum;
import tech.tongyu.bct.quant.library.priceable.equity.EquityDigitalCash;
import tech.tongyu.bct.quant.library.priceable.equity.EquityStock;
import tech.tongyu.bct.quant.library.priceable.equity.EquityVanillaEuropean;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class BlackPricerTest {
    private LocalDateTime val;
    private EquityVanillaEuropean<EquityStock> call, callBus252;
    private EquityDigitalCash<EquityStock> digiCall;
    private AtmPwcVolSurface volSurface;
    private DiscountingCurve discountingCurve;
    private DiscountingCurve dividendCurve;

    private final String calendar = "TEST_PRICING_WITH_BUS_252";
    private final List<LocalDate> holidays = Arrays.asList(
            LocalDate.of(2019, 3, 18),
            LocalDate.of(2019, 3, 20)
    );
    private final List<String> calendars = Arrays.asList(calendar);

    @Before
    public void setup() {
        Holidays.Instance.add(calendar, holidays);
        LocalDateTime expiry = LocalDateTime.of(2019, 12, 21, 15, 0, 0);
        val = LocalDateTime.of(2018,12,21,0,0,0);
        EquityStock stock = new EquityStock("000002.SZ");
        call = new EquityVanillaEuropean<>(stock, 100,
                expiry,
                OptionTypeEnum.CALL);
        callBus252 = new EquityVanillaEuropean<>(stock, 100.,
                LocalDateTime.of(2019, 3, 25, 0, 0), OptionTypeEnum.CALL);
        digiCall = new EquityDigitalCash<>(stock, 100, expiry, OptionTypeEnum.CALL, 1.0);
        volSurface = AtmPwcVolSurface.flat(val, 100, 0.2, 365.);
        discountingCurve = PwlfDiscountingCurve.flat(val, 0.05, 365);
        dividendCurve = PwlfDiscountingCurve.flat(val, 0.02, 365);
    }

    @After
    public void tearDown() {
        Holidays.Instance.delete(calendar);
    }

    @Test
    public void testBlackScholes() {
        double tau = DateTimeUtils.days(val, call.getExpiry()) / 365.0;
        double price = BlackScholes.calc(CalcTypeEnum.PRICE,
                100., 100., 0.2, tau, 0.05, 0.02, OptionTypeEnum.CALL);
        QuantlibCalcResults results = GenericPricer.qlSingleAssetOptionCalc(
                Arrays.asList(CalcTypeEnum.PRICE), call, val,100., volSurface,
                discountingCurve, dividendCurve, new BlackScholesAnalyticPricer(new BlackAnalyticPricerParams()));
        assertEquals(price, results.getValue(CalcTypeEnum.PRICE).get(), DoubleUtils.SMALL_NUMBER);

        double T = DateTimeUtils.days(val, digiCall.getExpiry()) / 365.;
        double digiPrice = Digital.calc(CalcTypeEnum.PRICE, 100, 100, 0.2,
                tau, 0.05, 0.02, T,OptionTypeEnum.CALL, true);
        QuantlibCalcResults results2 = GenericPricer.qlSingleAssetOptionCalc(Arrays.asList(CalcTypeEnum.PRICE), digiCall,
                val, 100., volSurface, discountingCurve, dividendCurve,
                new BlackScholesAnalyticPricer(new BlackAnalyticPricerParams()));
        assertEquals(digiPrice, results2.getValue(CalcTypeEnum.PRICE).get(), DoubleUtils.SMALL_NUMBER);
    }

    @Test
    public void testBlackScholesBus252() {
        LocalDate valuationDate = LocalDate.of(2019, 3, 15);
        double tau = 4./252.;
        double price = BlackScholes.calc(CalcTypeEnum.PRICE,
                100., 100., 0.2, tau, 0.05, 0.02, OptionTypeEnum.CALL);
        BlackScholesAnalyticPricer pricer = new BlackScholesAnalyticPricer(
                new BlackAnalyticPricerParams(true, calendars, 252));
        QuantlibCalcResults results = GenericPricer.qlSingleAssetOptionCalc(Arrays.asList(CalcTypeEnum.PRICE),
                callBus252,
                LocalDateTime.of(valuationDate, LocalTime.MIDNIGHT),
                100.,
                volSurface, discountingCurve, dividendCurve, pricer);
        assertEquals(price, results.getValue(CalcTypeEnum.PRICE).get(), 2.0 * DoubleUtils.SMALL_NUMBER);
    }
}

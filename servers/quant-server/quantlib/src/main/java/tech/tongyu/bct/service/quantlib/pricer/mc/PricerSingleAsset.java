package tech.tongyu.bct.service.quantlib.pricer.mc;

import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantApi2;
import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantApiArg;
import tech.tongyu.bct.service.quantlib.common.enums.PricerType;
import tech.tongyu.bct.service.quantlib.common.numerics.mc.McInstrument;
import tech.tongyu.bct.service.quantlib.common.numerics.mc.McPricerSingleAsset;
import tech.tongyu.bct.service.quantlib.common.numerics.mc.lognormal.Pricer;
import tech.tongyu.bct.service.quantlib.common.utils.Constants;
import tech.tongyu.bct.service.quantlib.market.curve.Discount;
import tech.tongyu.bct.service.quantlib.market.curve.Pwlf;
import tech.tongyu.bct.service.quantlib.market.vol.AtmPwc;
import tech.tongyu.bct.service.quantlib.market.vol.ImpliedVolSurface;
import tech.tongyu.bct.service.quantlib.market.vol.LocalVolSurface;

import java.time.LocalDateTime;
import java.util.Map;

public class PricerSingleAsset {
    // uses explict vol, r, q numbers instead of vol surfaces/curves
    // to be deprecated once risk engine is in place
    @BctQuantApi2(
            name = "qlOptionCalcMc",
            description = "Monte Carlo pricer based on Black model",
            args = {
                    @BctQuantApiArg(name = "instrument", type = "Handle", description = "Instrument"),
                    @BctQuantApiArg(name = "valuation_date", type = "DateTime", description = "Valuation date"),
                    @BctQuantApiArg(name = "spot", type = "Double", description = "Underlyer spot"),
                    @BctQuantApiArg(name = "vol", type = "Double", description = "Black volatility"),
                    @BctQuantApiArg(name = "r", type = "Double", description = "Risk free rate"),
                    @BctQuantApiArg(name = "q", type = "Double", description = "Dividend/Borrow rate"),
                    @BctQuantApiArg(name = "pricer_params", type = "Json",
                            description = "Pricer parameters", required = false)
            },
            retName = "request",
            retDescription = "NPV, greeks and Black parameters used",
            retType = "Json"
    )
    public static Map<String, Double> pvBlack(
            Object instrument,
            LocalDateTime val,
            double spot,
            double vol,
            double r,
            double q,
            Map<String, Object> params) {
        AtmPwc vs = AtmPwc.flat(val, spot, vol, Constants.DAYS_IN_YEAR);
        Pwlf discount = Pwlf.flat(val, r, Constants.DAYS_IN_YEAR);
        Pwlf dividend = Pwlf.flat(val, q, Constants.DAYS_IN_YEAR);
        McInstrument mcInstrument = FactoryMcInstrument.from(instrument);
        long seed = 1234L;
        if (params != null && params.containsKey("seed"))
            seed = ((Number)params.get("seed")).longValue();
        int numPaths = 10000;
        if (params != null  && params.containsKey("numPaths"))
            numPaths = ((Number)params.get("numPaths")).intValue();
        double stepSize = 7.0 / Constants.DAYS_IN_YEAR;
        if (params != null && params.containsKey("stepSize"))
                stepSize = ((Number)params.get("stepSize")).doubleValue();
        boolean includeGridDates = false;
        if (params != null && params.containsKey("includeGridDates"))
            includeGridDates = (boolean)params.get("includeGridDates");
        boolean brownianBridgeAdj = false;
        if (params != null && params.containsKey("brownianBridgeAdj"))
            brownianBridgeAdj = (boolean)params.get("brownianBridgeAdj");
        Pricer.McParams mcParams = new Pricer.McParams(seed, numPaths, stepSize, includeGridDates, brownianBridgeAdj);
        return Pricer.pv(mcInstrument, val, spot, discount, dividend, vs, mcParams);
    }

    public static Map<String, Double> pv (
            Object instrument,
            LocalDateTime val,
            double spot,
            ImpliedVolSurface vs,
            Discount discount,
            Discount dividend,
            Map<String, Object> params) throws Exception {
        McInstrument mcInstrument = FactoryMcInstrument.from(instrument);

        PricerType pricerType = PricerType.BLACK_MC;
        if (params != null && params.containsKey("pricer"))
            pricerType = (PricerType)params.get("pricer");

        // check if vol type is consistent with pricer type
        if (pricerType == PricerType.BLACK_MC && !(vs instanceof AtmPwc))
            throw new Exception("black mc requires a piecewise constant atm vol surface (type AtmPwc)");
        if (pricerType == PricerType.LOCAL_VOL_MC && !(vs instanceof LocalVolSurface))
            throw new Exception("local vol mc requires a vol surface that supports local vol calculation. " +
                    "currently only interpolated vol surface or atm pwc vol surfaces supports local vol");

        long seed = 1234L;
        if (params != null && params.containsKey("seed"))
            seed = ((Number)params.get("seed")).longValue();
        int numPaths = 10000;
        if (params != null  && params.containsKey("numPaths"))
            numPaths = ((Number)params.get("numPaths")).intValue();
        double stepSize = 7.0 / Constants.DAYS_IN_YEAR;
        if (params != null && params.containsKey("stepSize"))
            stepSize = ((Number)params.get("stepSize")).doubleValue();
        boolean includeGridDates = false;
        if (params != null && params.containsKey("includeGridDates"))
            includeGridDates = (boolean)params.get("includeGridDates");
        boolean brownianBridgeAdj = false;
        if (params != null && params.containsKey("brownianBridgeAdj"))
            brownianBridgeAdj = (boolean)params.get("brownianBridgeAdj");
        boolean addVolSurfaceDates = false;
        if (params != null && params.containsKey("addVolSurfaceDates"))
            addVolSurfaceDates = (boolean)params.get("addVolSurfaceDates");
        McPricerSingleAsset.Params mcParams = new McPricerSingleAsset.Params(pricerType, seed,
                numPaths, stepSize, includeGridDates, addVolSurfaceDates, brownianBridgeAdj);
        return McPricerSingleAsset.pv(mcInstrument, val, spot, vs, discount, dividend, mcParams);
    }
}

package tech.tongyu.bct.service.quantlib.api;

import org.apache.commons.math3.util.FastMath;
import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantApi2;
import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantApiArg;
import tech.tongyu.bct.service.quantlib.common.enums.CalcType;
import tech.tongyu.bct.service.quantlib.common.enums.PricerType;
import tech.tongyu.bct.service.quantlib.common.utils.Constants;
import tech.tongyu.bct.service.quantlib.common.utils.CustomException;
import tech.tongyu.bct.service.quantlib.financial.instruments.linear.Cash;
import tech.tongyu.bct.service.quantlib.financial.instruments.linear.Spot;
import tech.tongyu.bct.service.quantlib.market.curve.Discount;
import tech.tongyu.bct.service.quantlib.market.curve.Pwlf;
import tech.tongyu.bct.service.quantlib.market.vol.ImpliedVolSurface;
import tech.tongyu.bct.service.quantlib.pricer.PricerBlackWithRisks;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static tech.tongyu.bct.service.quantlib.common.enums.CalcType.*;
import static tech.tongyu.bct.service.quantlib.pricer.PricerBlackWithRisks.mcCalc;

/**
 * A generic pricer
 * This function tries to abstract pricing into instrument and its associated market data
 * Created by lu on 5/3/17.
 */
public class OptionPricerGeneric {
    @BctQuantApi2(
            name = "qlOptionSingleAssetCalcAdv",
            description = "Single asset pricer that outputs price, greeks and Black parameters",
            args = {
                    @BctQuantApiArg(name = "requests", type = "ArrayEnum", description = "Calculation request"),
                    @BctQuantApiArg(name = "instrument", type = "Handle", description = "Instrument"),
                    @BctQuantApiArg(name = "valuation_date", type = "DateTime", description = "Valuation date"),
                    @BctQuantApiArg(name = "spot", type = "Double", description = "Underlyer spot", required = false),
                    @BctQuantApiArg(name = "vol_surface", type = "Handle",
                            description = "Vol surface", required = false),
                    @BctQuantApiArg(name = "discount_curve", type = "Handle",
                            description = "Discounting curve", required = false),
                    @BctQuantApiArg(name = "dividend_curve", type = "Handle",
                            description = "Dividend/Borrow curve", required = false),
                    @BctQuantApiArg(name = "pricer", type = "Enum",
                            description = "Pricer", required = false),
                    @BctQuantApiArg(name = "pricer_params", type = "Json",
                            description = "Pricer parameters", required = false)
            },
            retName = "request",
            retDescription = "NPV, greeks and Black parameters used",
            retType = "Json"
    )
    public static Map<String, Double> singleAssetReport(Object[] requests,
                                                        Object instrument,
                                                        LocalDateTime val,
                                                        double spot,
                                                        ImpliedVolSurface volSurface,
                                                        Discount discountCurve,
                                                        Discount dividendCurve,
                                                        PricerType pricer,
                                                        Map<String, Object> pricerParams) throws Exception {
        if (discountCurve != null) {
            /*if (!discountCurve.getVal().toLocalDate().isEqual(val.toLocalDate()))
                throw new Exception("Discount curve does not start on input valuation date");*/
            if (discountCurve.getVal().isAfter(val))
                throw new Exception("Discount curve's val date is in the future");
            // WARNING: rolling a curve can hide a lot of bugs, esp. market data related
            if (discountCurve.getVal().isBefore(val))
                discountCurve = discountCurve.roll(val);
        }
        if (dividendCurve != null) {
            /*if (!dividendCurve.getVal().toLocalDate().isEqual(val.toLocalDate()))
                throw new Exception("Dividend curve does not start on input valuation date");*/
            // WARNING: rolling a curve can hide a lot of bugs, esp. market data related
            if (dividendCurve.getVal().isAfter(val))
                throw new Exception("Dividend curve's val date is in the future");
            if (dividendCurve.getVal().isBefore(val))
                dividendCurve = dividendCurve.roll(val);
        }
        if (volSurface != null) {
            /*if (!volSurface.getVal().toLocalDate().isEqual(val.toLocalDate()))
                throw new Exception("Vol surface does not start on input valuation date");*/
            if (volSurface.getVal().isAfter(val))
                throw new Exception("Vol surface's val date is in the future");
            if (volSurface.getVal().isBefore(val))
                volSurface = volSurface.roll(val);
        }

        if (instrument instanceof Cash) {
            return PricerBlackWithRisks.calc(requests, (Cash) instrument, discountCurve, 365.0);
        } else if (instrument instanceof Spot) {
            return PricerBlackWithRisks.calc(requests, spot);
        } else {
            switch (pricer) {
                case BLACK_ANALYTIC:
                    return PricerBlackWithRisks.calc(requests,
                            instrument, spot, volSurface, discountCurve, dividendCurve);
                case BLACK_PDE:
                    return PricerBlackWithRisks.pdeCalc(requests, instrument,
                            spot, volSurface, discountCurve, dividendCurve, pricerParams);
                case BLACK_MC:
                case LOCAL_VOL_MC:
                    if (pricerParams != null && !pricerParams.containsKey("pricer"))
                        pricerParams.put("pricer", pricer);
                    return mcCalc(requests, instrument,
                            spot, volSurface, discountCurve, dividendCurve, pricerParams);
                default:
                    throw new Exception(String.format("Pricer %s is not supported", pricer.name()));
            }
        }
    }

    @BctQuantApi2(
            name = "qlOptionSingleAssetOnForwardCalc",
            description = "Single asset Black76 pricer that outputs price, greeks and Black parameters",
            args = {
                    @BctQuantApiArg(name = "requests", type = "ArrayEnum", description = "Calculation request"),
                    @BctQuantApiArg(name = "instrument", type = "Handle", description = "Instrument"),
                    @BctQuantApiArg(name = "valuation_date", type = "DateTime", description = "Valuation date"),
                    @BctQuantApiArg(name = "forward", type = "Double", description = "Underlyer forward", required = false),
                    @BctQuantApiArg(name = "vol_surface", type = "Handle",
                            description = "Vol surface", required = false),
                    @BctQuantApiArg(name = "discount_curve", type = "Handle",
                            description = "Discounting curve", required = false),
                    @BctQuantApiArg(name = "pricer", type = "Enum",
                            description = "Pricer", required = false),
                    @BctQuantApiArg(name = "pricer_params", type = "Json",
                            description = "Pricer parameters", required = false)
            },
            retName = "request",
            retDescription = "NPV, greeks and Black parameters used",
            retType = "Json"
    )
    public static Map<String, Double> singleAssetOnForwardReport(Object[] requests,
                                                        Object instrument,
                                                        LocalDateTime val,
                                                        double forward,
                                                        ImpliedVolSurface volSurface,
                                                        Discount discountCurve,
                                                        PricerType pricer,
                                                        Map<String, Object> pricerParams) throws Exception {
        ArrayList<CalcType> reqs = new ArrayList<>();
        boolean hasRhoR = false;
        boolean hasRhoQ = false;
        for (Object request : requests) {
            if (request == RHO_R)
                hasRhoR = true;
            if (request == CalcType.RHO_Q)
                hasRhoR = true;
            reqs.add((CalcType)request);
        }
        if (hasRhoQ)
            throw new Exception("RHO_Q is not valid for Black76 model.");

        // since we are basing black76 on black, rho_r now has to incorporate rho_q from black
        if (hasRhoR)
            reqs.add(CalcType.RHO_Q);

        PricerType black76Pricer;
        // temporary hack: convert black76_xxx to black_xxx
        switch (pricer) {
            case BLACK76_ANALYTIC:
                black76Pricer = PricerType.BLACK_ANALYTIC;
                break;
            case BLACK76_PDE:
                black76Pricer = PricerType.BLACK_PDE;
                break;
            case BLACK76_BINOMIAL_TREE:
                black76Pricer = PricerType.BLACK_BINOMIAL_TREE;
                break;
            case BLACK76_MC:
                black76Pricer = PricerType.BLACK_MC;
                break;
            case BLACK_MC:
            case BLACK_ANALYTIC:
            case BLACK_PDE:
            case BLACK_BINOMIAL_TREE:
            case LOCAL_VOL_MC:
                // do nothing if the pricer is already black based
                black76Pricer = pricer;
                break;
            default:
                throw new Exception("qlOptionSingleAssetOnForwardCalc supports " +
                            "only black76_analytic/pde/mc pricers");
        }
        Map<String, Double> blackResults = singleAssetReport(reqs.toArray(), instrument, val,
                forward, volSurface, discountCurve, discountCurve, black76Pricer, pricerParams);

        if (blackResults.containsKey("spot")) {
            double spot = blackResults.get("spot");
            blackResults.put("forward", spot);
            //blackResults.remove("spot");
        }
        if (hasRhoR) {
            // we need to combine rho_r and rho_q from black to get black76 rho_r and then delete rho_q
            double rhoR = blackResults.get("rho_r");
            double rhoQ = blackResults.get("rho_q");
            blackResults.put("rho_r", rhoR + rhoQ);
            blackResults.remove("rho_q");
        }
        return blackResults;
    }

    @BctQuantApi2(
            name = "qlOptionSingleAssetCalcAdvWithBaseContract",
            description = "Single asset pricer with base contract iput",
            args = {
                    @BctQuantApiArg(name = "requests", type = "ArrayEnum", description = "Calculation request"),
                    @BctQuantApiArg(name = "instrument", type = "Handle", description = "Instrument"),
                    @BctQuantApiArg(name = "valuation_date", type = "DateTime", description = "Valuation date"),
                    @BctQuantApiArg(name = "spot", type = "Double", description = "Underlyer spot"),
                    @BctQuantApiArg(name = "base_contract_forward", type = "Double",
                            description = "base contract forward"),
                    @BctQuantApiArg(name = "base_contract_maturity", type = "DateTime",
                            description = "Base contract maturity"),
                    @BctQuantApiArg(name = "vol_surface", type = "Handle",
                            description = "Vol surface"),
                    @BctQuantApiArg(name = "discount_curve", type = "Handle",
                            description = "Discounting curve"),
                    @BctQuantApiArg(name = "pricer", type = "Enum",
                            description = "Pricer", required = false),
                    @BctQuantApiArg(name = "pricer_params", type = "Json",
                            description = "Pricer parameters", required = false)
            },
            retName = "request",
            retDescription = "NPV, greeks and Black parameters used",
            retType = "Json"
    )
    public static Map<String, Double> singleAssetCalcWithBaseContract(
            Object[] requests,
            Object instrument,
            LocalDateTime val,
            double spot,
            double baseContractForward,
            LocalDateTime baseContractMaturity,
            ImpliedVolSurface volSurface,
            Discount discountCurve,
            PricerType pricer,
            Map<String, Object> pricerParams
    ) throws Exception {
        LocalDate valDate = val.toLocalDate();
        double dfq = baseContractForward/spot
                * discountCurve.df(baseContractMaturity)
                / discountCurve.df(LocalDateTime.of(valDate, LocalTime.MIDNIGHT));
        long T = valDate.until(baseContractMaturity, ChronoUnit.DAYS);
        if (T == 0)
            throw new CustomException("valuation date cannot be the same as base contract maturity");
        Set<CalcType> reqToUse = new HashSet<>();
        for (Object r : requests) {
            reqToUse.add((CalcType) r);
            if (r == RHO_R || r == THETA)
                reqToUse.add(RHO_Q);
        }
        double q = -FastMath.log(dfq) / T * Constants.DAYS_IN_YEAR;
        Discount dividendCurve = Pwlf.flat(val, q, Constants.DAYS_IN_YEAR);
        Map<String, Double> raw = singleAssetReport(reqToUse.toArray(), instrument, val, spot,
                volSurface, discountCurve, dividendCurve, pricer, pricerParams);
        for (String k : raw.keySet()) {
            Double v = raw.get(k);
            switch (k) {
                case "delta":
                    raw.put(k, spot/baseContractForward*v);
                    break;
                case "gamma":
                    raw.put(k, (spot*spot)/(baseContractForward*baseContractForward)*v);
                    break;
                case "theta":
                    Double rhoq = raw.get("rho_q");
                    raw.put(k, v + rhoq * FastMath.log(baseContractForward/spot)/(T*T)
                            *(Constants.DAYS_IN_YEAR*Constants.DAYS_IN_YEAR));
                    break;
                case "rho_r":
                    Double rhoq2 = raw.get("rho_q");
                    raw.put(k, v + rhoq2);
                    break;
            }
        }
        raw.put("base_contract_spot", baseContractForward);
        return raw;
    }
}

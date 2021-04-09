package tech.tongyu.bct.service.quantlib.api;

import org.apache.commons.math3.util.FastMath;
import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantApi;
import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantApi2;
import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantApiArg;
import tech.tongyu.bct.service.quantlib.common.enums.AtmVolType;
import tech.tongyu.bct.service.quantlib.common.enums.DeltaType;
import tech.tongyu.bct.service.quantlib.common.enums.ExtrapType;
import tech.tongyu.bct.service.quantlib.common.enums.OptionType;
import tech.tongyu.bct.service.quantlib.common.numerics.interp.Interpolator1DCubicSpline;
import tech.tongyu.bct.service.quantlib.common.utils.Constants;
import tech.tongyu.bct.service.quantlib.market.vol.*;
import tech.tongyu.bct.service.quantlib.market.vol.utils.VolCalendar;
import tech.tongyu.bct.service.quantlib.market.vol.utils.VolQuoteToStrikeConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

public class VolSurfaceServices {
    @BctQuantApi(
            name = "qlVolSurfaceAtmPwcCreate",
            description = "Create a vol surface from a list of expiries and ATM vols",
            argNames = {"val", "spot", "expiries", "vols", "daysInYear"},
            argTypes = {"DateTime", "Double", "ArrayDateTime", "ArrayDouble", "Double"},
            argDescriptions = {"Valuation date", "spot", "Expiries", "Implied vols", "Number of days in a year"},
            retName = "vs",
            retDescription = "An ATM PWC vol surface",
            retType = "Handle"
    )
    public static AtmPwc volSurfacAtmPwcCreate(LocalDateTime val, double spot, LocalDateTime[] expiries,
                                               double[] vols, double daysInYear) {
        TreeMap<Double, AtmPwc.Segment> segs = new TreeMap<>();
        ArrayList<Double> taus = new ArrayList<>();
        taus.add(0.0);
        ArrayList<Double> vars = new ArrayList<>();
        vars.add(0.0);
        for (int i = 0; i < expiries.length; ++i) {
            if (!expiries[i].isAfter(val))
                continue;
            double days = val.until(expiries[i], ChronoUnit.NANOS) / Constants.NANOSINDAY;
            taus.add(days);
            double var = vols[i] * vols[i] * days / daysInYear;
            vars.add(var);
        }
        for (int i = 1 ; i < taus.size(); ++i) {
            double tau = taus.get(i) - taus.get(i-1);
            double dailyVar = (vars.get(i) - vars.get(i-1)) / tau;
            AtmPwc.Segment seg = new AtmPwc.Segment();
            seg.localVol = FastMath.sqrt(dailyVar);
            seg.varSoFar = vars.get(i-1);
            segs.put(taus.get(i-1), seg);
        }
        // the last segment represents local volatility after the last expiry.
        // its local volatility is the same as the one before the last expiry.
        AtmPwc.Segment seg = new AtmPwc.Segment();
        seg.localVol = segs.lastEntry().getValue().localVol;
        seg.varSoFar = vars.get(vars.size() - 1);
        segs.put(taus.get(taus.size() - 1), seg);
        return new AtmPwc(val, spot, segs, daysInYear);
    }

    @BctQuantApi(
            name = "qlVolSurfaceImpliedVolGet",
            description = "Get implied vol from a vol surface",
            argNames = {"volSurface", "forward", "strike", "expiry"},
            argDescriptions = {"Vol surface", "Underlying forward", "Strike", "Expiry"},
            argTypes = {"Handle", "Double", "Double", "DateTime"},
            retName = "impliedVol",
            retType = "Double",
            retDescription = "Implied vol"
    )
    public static double getImpliedVol(ImpliedVolSurface volSurface,
                                       double forward, double strike, LocalDateTime expiry) {
        return volSurface.iv(forward, strike, expiry);
    }

    @BctQuantApi2(
            name = "qlVolSurfaceLocalVolGet",
            description = "Get local vol for a given time and (future) spot",
            args = {
                    @BctQuantApiArg(name = "volSurface", type = "Handle", description = "The (local) vol surface"),
                    @BctQuantApiArg(name = "spot", type = "Double", description = "The (future) spot"),
                    @BctQuantApiArg(name = "forward", type = "Double", description = "The underlyer's forward at t"),
                    @BctQuantApiArg(name = "t", type = "DateTime", description = "The time on which local vol is quered")
            },
            retType = "Double",
            retName = "localVol",
            retDescription = "Local vol on a future time"
    )
    public static double getLocalVol(LocalVolSurface volSurface,
                                      double spot, double forward, LocalDateTime t) {
        return volSurface.localVol(spot, forward, t);
    }

    @BctQuantApi2(
            name = "qlVolSurfaceRoll",
            description = "Roll a vol surface to a later date",
            args = {
                    @BctQuantApiArg(name = "oldVol", type = "Handle", description = "The vol surface to roll"),
                    @BctQuantApiArg(name = "newVal", type = "DateTime", description = " The date to roll to")
            },
            retName = "newVol",
            retType = "Handle",
            retDescription = "The rolled vol surface"
    )
    public static ImpliedVolSurface vsRoll(ImpliedVolSurface oldVol, LocalDateTime newVal) throws Exception{
        if (!newVal.isAfter(oldVol.getVal()))
            throw new Exception("The vol surface to be rolled starts after the date to roll to");
        return oldVol.roll(newVal);
    }

    @BctQuantApi2(
            name = "qlVolSurfaceInterpolatedCreate",
            description = "Create an interpolated vol surface from ATM/RR/BF vols",
            args = {
                    @BctQuantApiArg(name = "val", type = "DateTime", description = "Valuation date"),
                    @BctQuantApiArg(name = "spot", type = "Double", description = "Underlyer price"),
                    @BctQuantApiArg(name = "expiries", type = "ArrayDateTime", description = "Option expiries"),
                    @BctQuantApiArg(name = "forwards", type = "ArrayDouble", description = "Forwards on expiries"),
                    @BctQuantApiArg(name = "atmVolType", type = "Enum", description = "ATM vol type"),
                    @BctQuantApiArg(name = "atmVols", type = "ArrayDouble", description = "ATM vols on expiries"),
                    @BctQuantApiArg(name = "deltaType", type = "Enum", description = "Delta types"),
                    @BctQuantApiArg(name = "deltas", type = "ArrayDouble", description = "Deltas"),
                    @BctQuantApiArg(name = "vols", type = "Matrix", description = "RR and BF vols for each expiry"),
                    @BctQuantApiArg(name = "dfDoms", type = "ArrayDouble", description = "Discounting factors")
            },
            retName = "Vol Surface",
            retDescription = "Newly created vol surface",
            retType = "Handle"
    )
    public static ImpliedVolSurface interpolated (
            LocalDateTime val,
            double spot,
            LocalDateTime[] expiries,
            double[] forwards,
            AtmVolType atmVolType,
            double[] atmVols,
            DeltaType deltaType,
            double[] deltas,
            double[][] vols,
            double[] dfDoms
    ) throws Exception {
        // various checks
        if (expiries.length != atmVols.length)
            throw new Exception("Number of expiries does not match that of atm vols");
        if (expiries.length != vols.length)
            throw new Exception("Number of expiries does not match that of vols");
        if (expiries.length != dfDoms.length)
            throw new Exception("Number of expiries does not match that of domestic discounting factors");
        // go through each expiry
        TreeMap<LocalDateTime, Interpolator1DCubicSpline> interp = new TreeMap<>();
        for (int i = 0; i < expiries.length; ++i) {
            if (vols[i].length != deltas.length * 2)
                throw new Exception("For each expiry, there must be both a RR and a BF vol for each delta");
            TreeMap<Double, Double> sorter = new TreeMap<>();
            double atmVol = atmVols[i];
            double timeToExpiry = val.until(expiries[i], ChronoUnit.NANOS)
                    / Constants.NANOSINDAY / Constants.DAYS_IN_YEAR;
            double atmStrike = spot;
            if (atmVolType == AtmVolType.DELTA_NEUTRAL)
                atmStrike = VolQuoteToStrikeConverter.atmDeltaNeutral(
                        forwards[i],
                        atmVols[i],
                        timeToExpiry
                );
            sorter.put(atmStrike, atmVol);
            double dfDom = dfDoms[i];
            double dfFor = forwards[i] / spot * dfDom;
            for (int j = 0; j < deltas.length; ++j) {
                // rr
                double rr = vols[i][2 * j];
                double bf = vols[i][2 * j + 1];
                double callVol = 0.5 * rr + bf + atmVol;
                double callStrike = VolQuoteToStrikeConverter.strike(OptionType.CALL, deltas[j],
                        deltaType, atmVol, rr, bf, timeToExpiry, spot, dfDom, dfFor);
                sorter.put(callStrike, callVol);
                double putVol = callVol - rr;
                double putStrike = VolQuoteToStrikeConverter.strike(OptionType.PUT, -deltas[j],
                        deltaType, atmVol, rr, bf, timeToExpiry, spot, dfDom, dfFor);
                sorter.put(putStrike, putVol);
            }
            double[] xs = new double[1 + 2 * deltas.length];
            double[] ys = new double[1 + 2 * deltas.length];
            ExtrapType extrapLow = ExtrapType.EXTRAP_1D_LINEAR;
            ExtrapType extrapHigh = ExtrapType.EXTRAP_1D_LINEAR;
            int c = 2 * deltas.length;
            for (java.util.Map.Entry<Double, Double> entry : sorter.entrySet()) {
                double k = entry.getKey();
                double m = FastMath.log(forwards[i] / k);
                xs[c] = m;
                double vol = entry.getValue();
                ys[c] = vol * vol * timeToExpiry;
                --c;
            }
            interp.put(expiries[i], new Interpolator1DCubicSpline(xs, ys, extrapLow, extrapHigh));
            // on val date var = 0 for all strikes
            // use the first expiry's values to set up a 0 interpolator
            if (i == 0) {
                Arrays.fill(ys, 0.0);
                interp.put(val, new Interpolator1DCubicSpline(xs, ys,
                        ExtrapType.EXTRAP_1D_FLAT, ExtrapType.EXTRAP_1D_FLAT));
            }
        }
        return new InterpolatedVolSurface(val, interp, VolCalendar.none(), spot);
    }

    @BctQuantApi2(
            name = "qlVolSurfaceBumpParallel",
            description = "Create a new volatility surface whose implied" +
                    " volatility at each expiry increased by a same amount.",
            args = {
                    @BctQuantApiArg(name = "volSurface", type = "Handle",
                    description = "The base volatility surface"),
                    @BctQuantApiArg(name = "amount", type = "Double",
                    description = "The amount to be increased")
            },
            retName = "Vol Surface",
            retDescription = "The new volatility surface",
            retType = "Handle"
    )
    public static ImpliedVolSurface bump(ImpliedVolSurface volSurface,
                                         double amount) {
        return volSurface.bump(amount);
    }

    @BctQuantApi2(
            name = "qlVolSurfaceInterpolatedStrikeCreate",
            description = "Create an interpolated volatility surface based on" +
                    "a matrix of implied volatilities. The 1st index of the matrix" +
                    "runs through expiries and the 2nd index through strikes.",
            args = {
                    @BctQuantApiArg(name = "val", type = "DateTime",
                    description = "valuation date"),
                    @BctQuantApiArg(name = "spot", type = "Double",
                    description = "spot at valuation"),
                    @BctQuantApiArg(name = "expiries", type = "ArrayDateTime",
                    description = "expiries of the implied volatilities"),
                    @BctQuantApiArg(name = "strikes", type = "ArrayDouble",
                    description = "strikes of the implied volatilities"),
                    @BctQuantApiArg(name = "vols", type = "Matrix",
                    description = "a matrix of implied volatilities"),
                    @BctQuantApiArg(name = "daysInYear", type = "Double",
                    description = "number of trading days in year")
            },
            retName = "interpolatedVolSurface",
            retType = "Handle",
            retDescription = "A volatility surface interpolated on the " +
                    "volatility matrix"
    )
    public static InterpolatedStrikeVolSurface interpolatedStrikeVolSurface(
            LocalDateTime val, double spot, LocalDateTime[] expiries,
            double[] strikes, double[][] vols, double daysInYear) {
        if (vols.length != expiries.length)
            throw new RuntimeException("Shape of the volatility matrix does not" +
                    " match size of expiries.");
        if (vols[0].length != strikes.length)
            throw new RuntimeException("Shape of the volatility matrix does not" +
                    " match size of strikes.");
        TreeMap<LocalDateTime, Interpolator1DCubicSpline> varInterp = new TreeMap<>();
        double[] interpStrikes = Arrays.copyOf(strikes, strikes.length);
        for (int i = 0; i < expiries.length; i++) {
            LocalDateTime e = expiries[i];
            double t = val.until(e, ChronoUnit.NANOS)
                    / Constants.NANOSINDAY / daysInYear;
            double[] vars = new double[strikes.length];
            for (int j = 0; j < strikes.length; j++)
                vars[j] = vols[i][j] * vols[i][j] * t;
            varInterp.put(e, new Interpolator1DCubicSpline(interpStrikes, vars,
                    ExtrapType.EXTRAP_1D_FLAT, ExtrapType.EXTRAP_1D_FLAT));
        }
        return new InterpolatedStrikeVolSurface(val, varInterp,
                VolCalendar.none(), spot, daysInYear);
    }

    @BctQuantApi2(
            name = "qlVolCalendarCreate",
            description = "Create a vol calendar.",
            args = {
                    @BctQuantApiArg(name = "weekendWeight", type = "Double", description = "weight on weekends"),
                    @BctQuantApiArg(name = "specialDates", type = "ArrayDateTime",
                            description = "special volatility dates"),
                    @BctQuantApiArg(name = "specialWeights", type = "ArrayDouble",
                            description = "weights on special dates")
            },
            retName = "volCalendar",
            retType = "Handle",
            retDescription = "A vol calendar"
    )
    public static VolCalendar volCalendarCreate(
            double weekendWeight, LocalDateTime[] specialDates, double[] specialWeights) {
        int holidayNum = specialDates.length;
        Map<LocalDate, Double> weights = new TreeMap<>();
        for (int i = 0; i < holidayNum; i++)
            weights.put(specialDates[i].toLocalDate(), specialWeights[i]);
        return new VolCalendar(weekendWeight, weights);
    }

    @BctQuantApi2(
            name = "qlVolSurfaceInterpolatedStrikeVolCalendarCreate",
            description = "Create an interpolated volatility surface based on" +
                    "a matrix of implied volatilities. The 1st index of the matrix" +
                    "runs through expiries and the 2nd index through strikes.",
            args = {
                    @BctQuantApiArg(name = "val", type = "DateTime",
                            description = "valuation date"),
                    @BctQuantApiArg(name = "spot", type = "Double",
                            description = "spot at valuation"),
                    @BctQuantApiArg(name = "expiries", type = "ArrayDateTime",
                            description = "expiries of the implied volatilities"),
                    @BctQuantApiArg(name = "strikes", type = "ArrayDouble",
                            description = "strikes of the implied volatilities"),
                    @BctQuantApiArg(name = "vols", type = "Matrix",
                            description = "a matrix of implied volatilities"),
                    @BctQuantApiArg(name = "volCalendar", type = "Handle",
                            description = "vol calendar"),
                    @BctQuantApiArg(name = "daysInYear", type = "Double",
                            description = "number of days in year")
            },
            retName = "interpolatedVolSurface",
            retType = "Handle",
            retDescription = "A volatility surface interpolated on the " +
                    "volatility matrix"
    )
    public static InterpolatedStrikeVolSurface interpolatedStrikeVolSurface(
            LocalDateTime val, double spot, LocalDateTime[] expiries, double[] strikes,
            double[][] vols, VolCalendar volCalendar, double daysInYear) {
        if (vols.length != expiries.length)
            throw new RuntimeException("Shape of the volatility matrix does not" +
                    " match size of expiries.");
        if (vols[0].length != strikes.length)
            throw new RuntimeException("Shape of the volatility matrix does not" +
                    " match size of strikes.");
        TreeMap<LocalDateTime, Interpolator1DCubicSpline> varInterp = new TreeMap<>();
        double[] interpStrikes = Arrays.copyOf(strikes, strikes.length);
        for (int i = 0; i < expiries.length; i++) {
            LocalDateTime e = expiries[i];
            double t = volCalendar.getEffectiveNumDays(val, e) / daysInYear;
            double[] vars = new double[strikes.length];
            for (int j = 0; j < strikes.length; j++)
                vars[j] = vols[i][j] * vols[i][j] * t;
            varInterp.put(e, new Interpolator1DCubicSpline(interpStrikes, vars,
                    ExtrapType.EXTRAP_1D_FLAT, ExtrapType.EXTRAP_1D_FLAT));
        }
        return new InterpolatedStrikeVolSurface(val, varInterp, volCalendar, spot, daysInYear);
    }
}
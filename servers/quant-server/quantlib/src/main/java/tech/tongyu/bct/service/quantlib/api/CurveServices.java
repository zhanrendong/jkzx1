package tech.tongyu.bct.service.quantlib.api;

import org.apache.commons.math3.util.FastMath;
import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantApi;
import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantApi2;
import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantApiArg;
import tech.tongyu.bct.service.quantlib.common.enums.CompoundingType;
import tech.tongyu.bct.service.quantlib.financial.dateservice.Basis;
import tech.tongyu.bct.service.quantlib.market.curve.Discount;
import tech.tongyu.bct.service.quantlib.market.curve.Pwlf;

import java.time.LocalDateTime;
import java.util.Objects;

public class CurveServices {
    @BctQuantApi(
            name = "qlDf",
            description = "Gets the discounting factor on a given date",
            argNames = {"yc", "t"},
            argDescriptions = {"A discounting curve", "The date on which a discounting factor is requested"},
            argTypes = {"Handle", "DateTime"},
            retDescription = "Discounting factor",
            retName = "df",
            retType = "Double"
    )
    public static double df(Discount yc, LocalDateTime t) {
        return yc.df(t);
    }

    @BctQuantApi(
            name = "qlFwd",
            description = "Calculates the forward rate from a discounting curve between given start and end dates",
            argNames = {"yc", "start", "end", "basis"},
            argDescriptions = {"The yield curve", "Forward rate start date",
                    "Forward rate end date", "Day count basis"},
            argTypes = {"Handle", "DateTime", "DateTime", "Enum"},
            retDescription = "The forward rate",
            retName = "forward",
            retType = "Double"
    )
    public static double fwd(Discount yc, LocalDateTime start, LocalDateTime end, Basis basis) {
        return yc.fwd(start.toLocalDate(), end.toLocalDate(), basis);
    }

    @BctQuantApi2(
            name = "qlCurveFromSpotRates",
            description = "Create a curve from given dates and interest rates",
            retDescription = "A PWLC curve",
            retName = "curve",
            retType = "Handle",
            args = {
                    @BctQuantApiArg(name = "val", type = "DateTime", description = "Curve start date"),
                    @BctQuantApiArg(name = "spotDate", type = "DateTime",description = "Spot date for the rates"),
                    @BctQuantApiArg(name = "ts", type = "ArrayDateTime", description = "Rate end dates"),
                    @BctQuantApiArg(name = "rs", type = "ArrayDouble", description = "Rates"),
                    @BctQuantApiArg(name = "basis", type = "Enum", description = "Basis"),
                    @BctQuantApiArg(name = "compounding", type = "Enum", description = "Compounding type",
                            required = false)
            }
    )
    public static Pwlf fromSpotRates(LocalDateTime val, LocalDateTime spotDate,
                                     LocalDateTime[] ts, double[] rs, Basis basis, CompoundingType compoundingType) {
        double spotDf = 1.0;
        if (spotDate.isAfter(val)) {
            // adjust by dfs from val to spot date
            if (Objects.isNull(compoundingType) || compoundingType == CompoundingType.CONTINUOUS_COMPOUNDING)
                spotDf = FastMath.exp(-basis.daycountFraction(val.toLocalDate(), spotDate.toLocalDate()) * rs[0]);
            else
                spotDf = 1.0 / (1.0 + basis.daycountFraction(val.toLocalDate(), spotDate.toLocalDate()) * rs[0]);
        }
        // compute dfs from spot date
        LocalDateTime[] dfTimes = new LocalDateTime[ts.length+1];
        double dfs[] = new double[ts.length+1];
        dfTimes[0] = spotDate;
        dfs[0] = spotDf;
        for (int i = 1; i < dfs.length; ++i) {
            double r = rs[i-1];
            double dcf = basis.daycountFraction(spotDate.toLocalDate(), ts[i-1].toLocalDate());
            if (Objects.isNull(compoundingType) || compoundingType == CompoundingType.CONTINUOUS_COMPOUNDING)
                dfs[i] = spotDf * FastMath.exp(-dcf*r);
            else
                dfs[i] = spotDf/(1.0 + dcf*r);
            dfTimes[i] = ts[i-1];
        }
        return Pwlf.pwcfFromDfs(val, dfTimes, dfs);
    }

    @BctQuantApi2(
            name = "qlCurveRoll",
            description = "Roll a curve into a future date",
            args = {
                    @BctQuantApiArg(name="oldYc", type="Handle", description = "The curve to roll"),
                    @BctQuantApiArg(name="newVal", type="DateTime", description = "The new date to roll to")
            },
            retName = "newYc",
            retType = "Handle",
            retDescription = "The rolled curve"
    )
    public static Pwlf roll(Pwlf oldYc, LocalDateTime newVal) throws Exception {
        if (!newVal.isAfter(oldYc.getVal()))
            throw new Exception("The new valuation date of the input curve is before the original's");
        return oldYc.roll(newVal);
    }
}

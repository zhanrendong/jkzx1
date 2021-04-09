package tech.tongyu.bct.service.quantlib.api;

import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantApi;
import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantApi2;
import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantApiArg;
import tech.tongyu.bct.service.quantlib.common.enums.BarrierDirection;
import tech.tongyu.bct.service.quantlib.common.enums.ExerciseType;
import tech.tongyu.bct.service.quantlib.common.enums.OptionType;
import tech.tongyu.bct.service.quantlib.common.enums.RebateType;
import tech.tongyu.bct.service.quantlib.financial.dateservice.BusDayAdj;
import tech.tongyu.bct.service.quantlib.financial.dateservice.Roll;
import tech.tongyu.bct.service.quantlib.financial.instruments.linear.Cash;
import tech.tongyu.bct.service.quantlib.financial.instruments.linear.Forward;
import tech.tongyu.bct.service.quantlib.financial.instruments.linear.Spot;
import tech.tongyu.bct.service.quantlib.financial.instruments.options.*;

import java.time.LocalDateTime;
import java.util.*;

public class OptionCreator {
    // cash
    @BctQuantApi(
            name = "qlCashCreate",
            description = "Create a single cash payment",
            argNames = {"amount", "payDate"},
            argDescriptions = {"Payment amount", "Payment date"},
            argTypes = {"Double", "DateTime"},
            retDescription = "NPV of the cash payment",
            retType = "Handle",
            retName = "NPV"
    )
    public static Cash cashCreate(double amount, LocalDateTime paymentDate) {
        return new Cash(amount, paymentDate);
    }
    // spot
    @BctQuantApi(
            name = "qlSpotCreate",
            description = "Create a spot trade",
            argNames = {"underlying"},
            argDescriptions = {"Tikcer/name of the underlying"},
            argTypes = {"String"},
            retName = "Spot",
            retType = "Handle",
            retDescription = "A spot transaction"
    )
    public static Spot spotCreate(String underlying) {
        return new Spot(underlying);
    }

    // forward
    @BctQuantApi2(
            name = "qlForwardCreate",
            description = "Create a forward",
            args = {
                    @BctQuantApiArg(name = "strike", description = "forward's strike", type = "Double"),
                    @BctQuantApiArg(name = "delivery", description = "forward's delivery date", type = "DateTime")
            },
            retName = "forward",
            retType = "Handle",
            retDescription = "A newly created forward"
    )
    public static Forward forwardCreate(double strike, LocalDateTime delivery) {
        return new Forward(delivery, strike);
    }

    //vanilla American option
    @BctQuantApi(
            name = "qlOptionVanillaAmericanCreate",
            description = "Creates an American CALL/PUT option",
            argNames = {"type", "strike", "expiry"},
            argDescriptions = {"Option type: CALL or PUT", "Option strike",
                    "Option expiry date"},
            argTypes = {"Enum", "Double", "DateTime"},
            retName = "",
            retType = "Handle",
            retDescription = "An American Option"
    )
    public static VanillaAmerican optionVanillaAmericanCreate(OptionType type, double strike,
                                                              LocalDateTime expiry) {
        return new VanillaAmerican(strike, type, expiry);
    }

    //vanilla European option
    @BctQuantApi(
            name = "qlOptionVanillaEuropeanCreate",
            description = "Creates a European CALL/PUT option",
            argNames = {"type", "strike", "expiry", "delivery"},
            argDescriptions = {"Option type: CALL or PUT", "Option strike",
                    "Option expiry date", "Option delivery date"},
            argTypes = {"Enum", "Double", "DateTime", "DateTime"},
            retName = "",
            retType = "Handle",
            retDescription = "A European Option"
    )
    public static VanillaEuropean optionVanillaEuropeanCreate(OptionType type, double strike,
                                                              LocalDateTime expiry, LocalDateTime delivery) {
        return new VanillaEuropean(type, strike, expiry, delivery);
    }

    //digital cash option
    @BctQuantApi(
            name = "qlOptionDigitalCashCreate",
            description = "Creates a European cash settled digital CALL/PUT option",
            argNames = {"type", "strike", "payment", "expiry", "delivery"},
            argDescriptions = {"Option type: CALL or PUT", "Option strike", "Option payout",
                    "Option expiry date", "Option delivery date"},
            argTypes = {"Enum", "Double", "Double", "DateTime", "DateTime"},
            retName = "",
            retType = "Handle",
            retDescription = "A European Digital Option"
    )
    public static DigitalCash optionDigitalCashCreate(OptionType type, double strike, double payment,
                                                      LocalDateTime expiry, LocalDateTime delivery) {
        return new DigitalCash(type, strike, payment, expiry, delivery);
    }

    //knock out continuous option
    @BctQuantApi(
            name = "qlOptionKnockOutContinuousCreate",
            description = "Creates a single barrier knock-out option with continuous monitoring",
            argNames = {"type", "strike", "expiry", "delivery", "barrier", "direction",
                    "barrierStart", "barrierEnd", "rebateAmount", "rebateType"},
            argDescriptions = {"Underlying option type: CALL/PUT", "Strike", "Expiry", "Delivery", "Barrier",
                    "Barrier direction(DOWN_AND_OUT/UP_AND_OUT)", "Barrier start date", "Barrier end date",
                    "Rebate amount", "When rebate is paid"},
            argTypes = {"Enum", "Double", "DateTime", "DateTime", "Double", "Enum", "DateTime", "DateTime",
                    "Double", "Enum"},
            retName = "",
            retType = "Handle",
            retDescription = "A knock-out option"
    )
    public static KnockOutContinuous optionKOCreate(OptionType type, double strike, LocalDateTime expiry,
                                                    LocalDateTime delivery, double barrier, BarrierDirection direction,
                                                    LocalDateTime barrierStart, LocalDateTime barrierEnd,
                                                    double rebateAmount, RebateType rebateType) throws Exception {
        if (direction == BarrierDirection.DOWN_AND_IN || direction == BarrierDirection.UP_AND_IN)
            throw new Exception("Barrier direction is not consistent with the instrument.");

        return new KnockOutContinuous(type, strike, expiry, delivery,
                barrier, direction, barrierStart, barrierEnd,
                rebateAmount, rebateType, ExerciseType.EUROPEAN);
    }

    //knock in continuous option
    @BctQuantApi(
            name = "qlOptionKnockInContinuousCreate",
            description = "Creates a single barrier knock-out option with continuous monitoring",
            argNames = {"type", "strike", "expiry", "delivery", "barrier", "direction",
                    "barrierStart", "barrierEnd", "rebateAmount"},
            argDescriptions = {"Underlying option type: CALL/PUT", "Strike", "Expiry", "Delivery", "Barrier",
                    "Barrier direction(DOWN_AND_IN/UP_AND_IN)", "Barrier start date", "Barrier end date",
                    "Rebate amount"},
            argTypes = {"Enum", "Double", "DateTime", "DateTime", "Double", "Enum", "DateTime", "DateTime",
                    "Double"},
            retName = "",
            retType = "Handle",
            retDescription = "A knock-in option"
    )
    public static KnockInContinuous optionKICreate(OptionType type, double strike, LocalDateTime expiry,
                                                   LocalDateTime delivery, double barrier, BarrierDirection direction,
                                                   LocalDateTime barrierStart, LocalDateTime barrierEnd,
                                                   double rebateAmount) throws Exception {
        if (direction == BarrierDirection.DOWN_AND_OUT || direction == BarrierDirection.UP_AND_OUT)
            throw new Exception("Barrier direction is not consistent with the instrument.");
        return new KnockInContinuous(type, strike, expiry, delivery,
                barrier, direction, barrierStart, barrierEnd,
                rebateAmount, ExerciseType.EUROPEAN);
    }

    //knock out terminal
    @BctQuantApi(
            name = "qlOptionKnockOutTerminalCreate",
            description = "Creates a single barrier knock-out option with terminal monitoring",
            argNames = {"type", "strike", "expiry", "delivery", "barrier", "direction", "rebateAmount"},
            argDescriptions = {"Underlying option type: CALL/PUT", "Strike", "Expiry", "Delivery", "Barrier",
                    "Barrier direction", "Rebate amount"},
            argTypes = {"Enum", "Double", "DateTime", "DateTime", "Double", "Enum", "Double"},
            retName = "",
            retType = "Handle",
            retDescription = "A knock-out option"
    )
    public static KnockOutTerminal optionKOTerminalCreate(OptionType type, double strike,
                                                          LocalDateTime expiry,
                                                          LocalDateTime delivery,
                                                          double barrier, BarrierDirection direction,
                                                          double rebateAmount) {
        return new KnockOutTerminal(type, strike, expiry, delivery, barrier, direction, rebateAmount);
    }

    //knock in terminal
    @BctQuantApi(
            name = "qlOptionKnockInTerminalCreate",
            description = "Creates a single barrier knock-in option with terminal monitoring",
            argNames = {"type", "strike", "expiry", "delivery", "barrier", "direction", "rebateAmount"},
            argDescriptions = {"Underlying option type: CALL/PUT", "Strike", "Expiry", "Delivery", "Barrier",
                    "Barrier direction", "Rebate amount"},
            argTypes = {"Enum", "Double", "DateTime", "DateTime", "Double", "Enum", "Double"},
            retName = "",
            retType = "Handle",
            retDescription = "A knock-out option"
    )
    public static KnockInTerminal optionKITerminalCreate(OptionType type, double strike,
                                                         LocalDateTime expiry,
                                                         LocalDateTime delivery,
                                                         double barrier, BarrierDirection direction,
                                                         double rebateAmount) {
        return new KnockInTerminal(type, strike, expiry, delivery, barrier, direction, rebateAmount);
    }

    //Asian
    @BctQuantApi(
            name = "qlOptionAverageRateArithmeticCreate",
            description = "Creates an Asian option on arithmetic average rate",
            argNames = {"type", "strike", "expiry", "delivery", "schedule", "weights", "fixings"},
            argDescriptions = {"Option type: CALL/PUT", "strike", "Expiry", "Delivery", "Averaging schedule",
                    "Averaging weights", "Past fixings"},
            argTypes = {"Enum", "Double", "DateTime", "DateTime", "ArrayDateTime", "ArrayDouble", "ArrayDouble"},
            retName = "",
            retType = "Handle",
            retDescription = "A newly created Asian arithmetic average rate option"
    )
    public static AverageRateArithmetic asianCreate(OptionType type, double strike,
                                                    LocalDateTime expiry, LocalDateTime delivery,
                                                    LocalDateTime[] schedule, double[] weights, double[] fixings) {
        return new AverageRateArithmetic(type, strike, expiry, delivery, schedule, weights, fixings);
    }

    @BctQuantApi2(
            name = "qlOptionAsianCreate",
            description = "Create an Asian option with automatic schedule generation",
            args = {
                    @BctQuantApiArg(name = "type", type = "Enum", description = "Option type: CALL/PUT"),
                    @BctQuantApiArg(name = "strike", type = "Double", description = "Optoin strike"),
                    @BctQuantApiArg(name = "expiry", type = "DateTime", description = "Option expiry"),
                    @BctQuantApiArg(name = "delivery", type = "DateTime", description = "Option delivery date"),
                    @BctQuantApiArg(name = "scheduleStart", type = "DateTime", description = "Schedule start date"),
                    @BctQuantApiArg(name = "scheduleEnd", type = "DateTime", description = "Schedule end date"),
                    @BctQuantApiArg(name = "scheduleFrequency", type = "String", description = "Observation frequency"),
                    @BctQuantApiArg(name = "roll", type = "Enum",
                            description = "Roll from end date (BACKWARD) or start date (FORWARD)"),
                    @BctQuantApiArg(name = "businessDayAdj", type = "Enum",
                            description = "Business day adjustment rule"),
                    @BctQuantApiArg(name = "calendars", type = "ArrayString",
                            description = "Holiday calendars to for business day adjustment")
            },
            retName = "asian",
            retType = "Handle",
            retDescription = "A newly created Asian option"
    )
    public static AverageRateArithmetic asianScheduleCreate(OptionType type, double strike,
                                                            LocalDateTime expiry, LocalDateTime delivery,
                                                            LocalDateTime scheduleStart, LocalDateTime scheduleEnd,
                                                            String scheduleFrequency, Roll roll, BusDayAdj adj,
                                                            String[] calendars) {
        if (calendars == null || calendars.length == 0)
            calendars = new String[] {"NONE"};
        LocalDateTime[] schedule = DateService.generateSchedule(scheduleStart, scheduleEnd, scheduleFrequency, roll,
                adj, calendars);
        int N = schedule.length;
        double[] weights = new double[N];
        for (int i = 0; i < N; i++) {
            weights[i] = 1.0;
        }
        double[] fixings = new double[]{};
        return new AverageRateArithmetic(type, strike, expiry, delivery, schedule, weights, fixings);
    }

    //Double shark Fin continuous create
    @BctQuantApi(
            name = "qlOptionDoubleSharkFinContinuousCreate",
            description = "Creates a double-shark-fin option with continuous monitoring",
            argNames = {"lowerStrike", "lowerBarrier", "lowerRebate",
                    "upperStrike", "upperBarrier", "upperRebate",
                    "barrierStart", "barrierEnd", "expiry", "delivery"},
            argTypes = {"Double", "Double", "Double",
                    "Double", "Double", "Double",
                    "DateTime", "DateTime", "DateTime", "DateTime"
            },
            argDescriptions = {"Put strike", "Lower barrier level", "Lower barrier rebate",
                    "Call strike", "Upper barrier", "Upper barrier rebate",
                    "Barrier start time", "Barrier end time", "Expiry", "Delivery"},
            retName = "",
            retDescription = "A newly created double-shark-fin option",
            retType = "Handle"
    )
    public static DoubleSharkFinContinuous doubleSharkFinContinuousCreate(
            double lowerStrike, double lowerBarrier, double lowerRebate,
            double upperStrike, double upperBarrier, double upperRebate,
            LocalDateTime barrierStart, LocalDateTime barrierEnd,
            LocalDateTime expiry, LocalDateTime delivery) {
        return new DoubleSharkFinContinuous(lowerStrike, lowerBarrier, lowerRebate,
                upperStrike, upperBarrier, upperRebate, barrierStart, barrierEnd, expiry, delivery);
    }

    //double shark fin terminal
    @BctQuantApi(
            name = "qlOptionDoubleSharkFinTerminalCreate",
            description = "Creates a double-shark-fin option with terminal monitoring",
            argNames = {"lowerStrike", "lowerBarrier", "lowerRebate",
                    "upperStrike", "upperBarrier", "upperRebate",
                    "barrierStart", "barrierEnd", "expiry", "delivery"},
            argTypes = {"Double", "Double", "Double",
                    "Double", "Double", "Double",
                    "DateTime", "DateTime", "DateTime", "DateTime"
            },
            argDescriptions = {"Put strike", "Lower barrier level", "Lower barrier rebate",
                    "Call strike", "Upper barrier", "Upper barrier rebate",
                    "Barrier start time", "Barrier end time", "Expiry", "Delivery"},
            retName = "",
            retDescription = "A newly created double-shark-fin option",
            retType = "Handle"
    )
    public static DoubleSharkFinTerminal doubleSharkFinTerminalCreate(
            double lowerStrike, double lowerBarrier, double lowerRebate,
            double upperStrike, double upperBarrier, double upperRebate,
            LocalDateTime barrierStart, LocalDateTime barrierEnd,
            LocalDateTime expiry, LocalDateTime delivery) {
        return new DoubleSharkFinTerminal(lowerStrike, lowerBarrier, lowerRebate,
                upperStrike, upperBarrier, upperRebate, barrierStart, barrierEnd, expiry, delivery);
    }


    //double knock out continuous
    @BctQuantApi(
            name = "qlOptionDoubleKnockOutContinuousCreate",
            description = "Creates a double knock-out option with continuous monitoring",
            argNames = {"type", "strike", "rebateType", "lowerBarrier", "lowerRebate", "upperBarrier", "upperRebate",
                    "barrierStart", "barrierEnd", "expiry", "delivery"},
            argDescriptions = {"option type: CALL/PUT", "strike", "rebate type", "lower barrier",
                    "rebate if lower barrier is hit first", "upper barrier",
                    "rebate if upper barrier is hit first", "barrier start date",
                    "barrier end date", "expiry", "delivery"},
            argTypes = {"Enum", "Double", "Enum", "Double", "Double", "Double", "Double", "DateTime", "DateTime",
                    "DateTime", "DateTime"},
            retName = "",
            retType = "Handle",
            retDescription = "A newly created double knock-out option with continuous monitoring"
    )
    public static DoubleKnockOutContinuous optionDKOCCreate(OptionType type, double strike, RebateType rebateType,
                                                            double lowerBarrier, double lowerRebate, double upperBarrier,
                                                            double upperRebate,
                                                            LocalDateTime barrierStart, LocalDateTime barrierEnd,
                                                            LocalDateTime expiry, LocalDateTime delivery) {
        return new DoubleKnockOutContinuous(type, strike, lowerBarrier, lowerRebate, upperBarrier, upperRebate,
                barrierStart, barrierEnd, expiry, delivery, ExerciseType.EUROPEAN, rebateType);
    }

    //Vertical Spread
    @BctQuantApi(
            name = "qlOptionVerticalSpreadCreate",
            description = "Creates a vertical spread option",
            argNames = {"type", "lowerStrike", "upperStrike", "expiry", "delivery"},
            argDescriptions = {"option type: CALL/PUT", "lower strike", "upper strike", "expiry", "delivery"},
            argTypes = {"Enum", "Double", "Double", "DateTime", "DateTime"},
            retName = "",
            retType = "Handle",
            retDescription = "A newly created spread vertical option"
    )
    public static VerticalSpread verticalSpreadCreate(OptionType type, double lowerStrike, double upperStrike,
                                                      LocalDateTime expiry, LocalDateTime delivery) {
        return new VerticalSpread(lowerStrike, upperStrike, expiry, delivery, type);
    }

    //range accrual
    @BctQuantApi(
            name = "qlOptionRangeAccrualCreate",
            description = "Creates a range accrual option",
            argNames = {"cumulative", "payoff", "expiry", "delivery", "schedule", "fixings", "min", "max"},
            argDescriptions = {"cumulative type", "payoff amount", "expiry", "delivery", "scheduled observation dates",
                    "past fixings", "minimum price", "maximum price"},
            argTypes = {"String", "Double", "DateTime", "DateTime", "ArrayDateTime", "ArrayDouble", "Double", "Double"},
            retName = "",
            retType = "Handle",
            retDescription = "A newly created range accrual option"
    )
    public static RangeAccrual rangeAccrualCreate(String cumulative, double payoff,
                                                  LocalDateTime expiry, LocalDateTime delivery, LocalDateTime[] schedule,
                                                  double[] fixings, double min, double max) {
        return new RangeAccrual(payoff, expiry, delivery, schedule, fixings, min, max, cumulative);
    }

    //alternative range accrual option creator
    @BctQuantApi(
            name = "qlOptionRangeAccrualAltCreate",
            description = "Creates a range accrual option with the scheduling parameters",
            argNames = {"cumulative", "payoff", "expiry", "delivery", "scheduleStart", "scheduleLength",
                    "scheduleFrequency", "roll", "busDayAdj", "fixings", "min", "max"},
            argDescriptions = {"cumulative method (in_range or out_range)", "payoff amount", "expiry date",
                    "delivery date", "schedule start date", "schedule tenor", "schedule frequency",
                    "roll direction (forward or backward)", "business day adjustment ", "past fixings", "min", "max"},
            argTypes = {"String", "Double", "DateTime", "DateTime", "DateTime", "String", "String", "Enum", "Enum",
                    "ArrayDouble", "Double", "Double"},
            retName = "",
            retType = "Handle",
            retDescription = "A newly created range accrual option"
    )
    public static RangeAccrual rangeAccrualCreate(String cumulative, double payoff,
                                                  LocalDateTime expiry, LocalDateTime delivery,
                                                  LocalDateTime scheduleStart, String scheduleLength, String scheduleFrequency,
                                                  Roll roll, BusDayAdj busDayAdj, double[] fixings, double min, double max) {
        String[] calendars = new String[]{"none"};
        LocalDateTime[] schedule = DateService.genSchedule(scheduleStart, scheduleLength, scheduleFrequency, roll, busDayAdj, calendars);
        return new RangeAccrual(payoff, expiry, delivery, schedule, fixings, min, max, cumulative);
    }


    //strangle
    @BctQuantApi(
            name = "qlOptionStrangleCreate",
            description = "Creates a strangle",
            argNames = {"strikeLow", "strikeHigh", "expiry", "delivery"},
            argDescriptions = {"put strike (lower)", "call strike (higher)", "expiry", "delivery"},
            argTypes = {"Double", "Double", "DateTime", "DateTime"},
            retName = "",
            retType = "Handle",
            retDescription = "A newly created strangle"
    )
    public static VanillaStrangle vanillaStrangleCreate(double strikeLow, double strikeHigh,
                                               LocalDateTime expiry, LocalDateTime delivery) throws Exception {
        if (strikeLow > strikeHigh)
            throw new Exception("Strangle's low strike must be less than high strike");
        return new VanillaStrangle(strikeLow, strikeHigh, expiry, delivery);
    }

    //spread option
    @BctQuantApi(
            name = "qlOptionSpreadCreate",
            description = "Creates a spread option",
            argNames = {"strike", "expiry", "delivery"},
            argDescriptions = {"strike", "expiry", "delivery"},
            argTypes = {"Double", "DateTime", "DateTime"},
            retName = "",
            retType = "Handle",
            retDescription = "A newly created spread option"
    )
    public static Spread spreadOptionCreate(double strike, LocalDateTime expiry, LocalDateTime delivery) {
        return new Spread(strike, expiry, delivery);
    }

    // no touch continuous
    @BctQuantApi2(
            name = "qlOptionNoTouchContinuousCreate",
            description = "Creates a no touch with continuous monitoring",
            args = {
                    @BctQuantApiArg(name = "barrierDirection", type = "Enum", description = "barrier direction"),
                    @BctQuantApiArg(name = "barrier", type = "Double", description = "barrier levels"),
                    @BctQuantApiArg(name = "expiry", type = "DateTime", description = "expiry"),
                    @BctQuantApiArg(name = "delivery", type = "DateTime", description = "payment delivery date"),
                    @BctQuantApiArg(name = "payment", type = "Double",
                            description = "payment if barrier is never touched")
            },
            retName = "no touch",
            retType = "Handle",
            retDescription = "The newly created no touch option (continuous monitoring)"
    )
    public static NoTouch noTouchContinuousCreate(BarrierDirection barrierDirection,
                                    double barrier,
                                    LocalDateTime expiry,
                                    LocalDateTime delivery,
                                    double payment) {
        return new NoTouch(barrier, barrierDirection, expiry, delivery,
                LocalDateTime.of(1990, 1, 1, 0, 0), expiry, payment);
    }

    // no touch discrete
    @BctQuantApi2(
            name = "qlOptionNoTouchDiscreteCreate",
            description = "Creates a no touch with discrete observations",
            args = {
                    @BctQuantApiArg(name = "barrierDirection", type = "Enum", description = "barrier direction"),
                    @BctQuantApiArg(name = "barrier", type = "ArrayDouble", description = "barrier levels"),
                    @BctQuantApiArg(name = "observationSchedule", type = "ArrayDateTime",
                            description = "observation dates"),
                    @BctQuantApiArg(name = "rebates", type = "ArrayDouble", description = "rebates"),
                    @BctQuantApiArg(name = "rebatePaymentDates", type = "ArrayDateTime",
                            description = "rebate payment dates"),
                    @BctQuantApiArg(name = "payment", type = "Double", description = "payment if no touch"),
                    @BctQuantApiArg(name = "paymentDate", type = "DateTime", description = "payment date")
            },
            retName = "no touch discrete option",
            retType = "Handle",
            retDescription = "A newly created no touch discrete option"
    )
    public static NoTouchDiscrete noTouchDiscreteCreate(BarrierDirection barrierDirection,
                                                        double[] barriers,
                                                        LocalDateTime[] observationSchedule,
                                                        double[] rebates,
                                                        LocalDateTime[] rebatePaymentDates,
                                                        double payment,
                                                        LocalDateTime paymentDate) {
        return new NoTouchDiscrete(barrierDirection, barriers, observationSchedule,
                rebates, rebatePaymentDates, payment, paymentDate);
    }

    // auto call
    @BctQuantApi2(
            name = "qlOptionAutoCallCreate",
            description = "Creates an auto call",
            args = {@BctQuantApiArg(name = "barrierDirection", type = "Enum",
                    description = "barrier type. (up_and_out or down_and_out"),
                    @BctQuantApiArg(name = "barriers", type = "ArrayDouble",
                            description = "barriers at each observation dates"),
                    @BctQuantApiArg(name = "observationDates", type = "ArrayDateTime",
                            description = "observation dates (earlier first)"),
                    @BctQuantApiArg(name = "inPayments", type = "ArrayDouble",
                            description = "payments if not knocked out"),
                    @BctQuantApiArg(name = "outPayments", type = "ArrayDouble",
                            description = "payments if knocked out"),
                    @BctQuantApiArg(name = "deliveries", type = "ArrayDateTime",
                            description = "delivery dates to each observations"),
                    @BctQuantApiArg(name = "deterministicPayments", type = "ArrayDouble",
                            description = "a series of deterministic payments"),
                    @BctQuantApiArg(name = "deterministicDeliveries", type = "ArrayDateTime",
                            description = "The delivery dates of the deterministic payments")
            },
            retName = "auto call",
            retType = "Handle",
            retDescription = "A newly created auto call"
    )
    public static AutoCall autoCallCreate(
            BarrierDirection barrierDirection, double[] barriers,
            LocalDateTime[] observationDates, double[] inPayments,
            double[] outPayments, LocalDateTime[] deliveries,
            double[] deterministicPayments, LocalDateTime[] deterministicDeliveries) {
        return new AutoCall(barrierDirection, barriers, observationDates,
                inPayments, outPayments, deliveries, deterministicPayments,
                deterministicDeliveries);
    }

    /**
     * Extract key and value from a map<String, double> into two arrays. The key
     * strings are assumed to represent datetime. The resulted key array is
     * sorted in natural order, while the mapping between key and value is
     * preserved, i.e. date[i] and value[i] is a key-value pair in the map for
     * any i.
     */
    private static void extractMap(Map<String, Double> map,
                                   LocalDateTime[] date, double[] value) {
        TreeMap<LocalDateTime, Double> dateMap = new TreeMap<>();
        Set<Map.Entry<String, Double>> set = map.entrySet();
        Iterator<Map.Entry<String, Double>> iter = set.iterator();
        while (iter.hasNext()) {
            Map.Entry<String, Double> e = iter.next();
            dateMap.put(LocalDateTime.parse(e.getKey()), e.getValue());
        }
        dateMap.keySet().toArray(date);
        Arrays.sort(date);
        for (int i = 0; i < date.length; i++)
            value[i] = dateMap.get(date[i]);
    }

    // phoenix option on single asset
    @BctQuantApi2(
            name = "qlOptionAutoCallPhoenixCreate",
            description = "Creates an autocall phoenix option on single asset",
            args = {@BctQuantApiArg(name = "principal", type = "Double",
                    description = "principal"),
                    @BctQuantApiArg(name = "initialSpot", type = "Double",
                    description = "initial underlying spot"),
                    @BctQuantApiArg(name = "expiry", type = "DateTime",
                    description = "expiry"),
                    @BctQuantApiArg(name = "delivery", type = "DateTime",
                    description = "delivery date"),
                    @BctQuantApiArg(name = "outObsDates", type = "ArrayDateTime",
                    description = "knockout observation dates"),
                    @BctQuantApiArg(name = "outBarriers", type = "ArrayDouble",
                    description = "knockout barrier levels"),
                    @BctQuantApiArg(name = "outDeliveries", type = "ArrayDateTime",
                    description = "knockout delivery dates"),
                    @BctQuantApiArg(name = "couponObsDates", type = "ArrayDateTime",
                    description = "coupon barrier observation dates"),
                    @BctQuantApiArg(name = "couponBarriers", type = "ArrayDouble",
                    description = "coupon barrier levels"),
                    @BctQuantApiArg(name = "couponDeliveries", type = "ArrayDateTime",
                    description = "coupon delivery dates"),
                    @BctQuantApiArg(name = "coupons", type = "ArrayDouble",
                    description = "coupon amounts"),
                    @BctQuantApiArg(name = "isSnowball", type = "Boolean",
                    description = "if snowball coupon"),
                    @BctQuantApiArg(name = "inObsDates", type = "ArrayDateTime",
                    description = "knockin observation dates", required = false),
                    @BctQuantApiArg(name = "inBarriers", type = "ArrayDouble",
                    description = "knockin barrier levels", required = false),
                    @BctQuantApiArg(name = "inStrike", type = "Double",
                    description = "knock in put strike", required = false),
                    @BctQuantApiArg(name = "fixingDates", type = "ArrayDateTime",
                    description = "history observation dates", required = false),
                    @BctQuantApiArg(name = "fixings", type = "ArrayDouble",
                    description = "history spots", required = false)
            },
            retName = "autocall phoenix option",
            retType = "Handle",
            retDescription = "A newly created autocall phoenix option on single asset",
            addIdInput = false
    )
    public static AutoCallPhoenix autoCallPhoenixCreate(
            double principal, double initialSpot,
            LocalDateTime expiry, LocalDateTime delivery,
            LocalDateTime[] outObsDates, double[] outBarriers,
            LocalDateTime[] outDeliveries, LocalDateTime[] couponObsDates,
            double[] couponBarriers, LocalDateTime[] couponDeliveries,
            double[] coupons, boolean isSnowball, LocalDateTime[] inObsDates,
            double[] inBarriers, double inStrike, LocalDateTime[] fixingDates,
            double[] fixings) {
        return new AutoCallPhoenix(principal, initialSpot, expiry, delivery,
                outObsDates, outBarriers, outDeliveries, couponObsDates,
                couponBarriers, couponDeliveries, coupons, isSnowball,
                inObsDates, inBarriers, inStrike, fixingDates, fixings);
    }
}
package tech.tongyu.bct.document.dictory;

import java.util.HashMap;
import java.util.Map;

public class Element {

    public static Map<String, Map<String, String>> ELEMENT_MAP = new HashMap<>();

    static {
        Map<String, String> commonT = new HashMap<>();
        commonT.put("tradeId", "${tradeId}");
        commonT.put("tradeDate", "${tradeDate}");
        commonT.put("tradeStatus", "${tradeStatus}");
        commonT.put("trader", "${trader}");
        commonT.put("bookName", "${bookName}");
        commonT.put("margin", "${margin}");
        commonT.put("salesCode", "${salesCode}");
        commonT.put("salesCommission", "${salesCommission}");
        commonT.put("salesName", "${salesName}");
        ELEMENT_MAP.put("commonT", commonT);
        Map<String, String> commonP = new HashMap<>();
        commonP.put("instrumentName", "${instrumentName}");
        commonP.put("counterPartyAccountCode", "${counterPartyAccountCode}");
        commonP.put("counterPartyAccountName", "${counterPartyAccountName}");
        commonP.put("counterPartyCode", "${counterPartyCode}");
        commonP.put("counterPartyName", "${counterPartyName}");
        commonP.put("lcmEventType", "${lcmEventType}");
        commonP.put("positionAccountCode", "${positionAccountCode}");
        commonP.put("positionAccountName", "${positionAccountName}");
        commonP.put("productType", "${productType}");
        ELEMENT_MAP.put("commonP", commonP);

        Map<String, String> settleInfo = new HashMap<>();
        settleInfo.put("underlyerPrice","${underlyerPrice}");
        settleInfo.put("settleAmount","${settleAmount}");
        settleInfo.put("notionalOldValue","${notionalOldValue}");
        settleInfo.put("numOfOption","${numOfOption}");
        settleInfo.put("settlement","${settlement}");
        ELEMENT_MAP.put("settleInfo", settleInfo);

        // 年化公共属性
        Map<String, String> annualizedMap = new HashMap<>();
        annualizedMap.put("daysInYear", "${daysInYear}");
        annualizedMap.put("frontPremium", "${frontPremium}");
        annualizedMap.put("minimumPremium", "${minimumPremium}");
        annualizedMap.put("term", "${term}");

        // 欧式
        Map<String, String> europeanMap = new HashMap<>();
        europeanMap.put("annualized", "${annualized}");
        europeanMap.put("direction", "${direction}");
        europeanMap.put("effectiveDate", "${effectiveDate}");
        europeanMap.put("exerciseType", "${exerciseType}");
        europeanMap.put("expirationDate", "${expirationDate}");
        europeanMap.put("initialSpot", "${initialSpot}");
        europeanMap.put("notionalAmount", "${notionalAmount}");
        europeanMap.put("notionalAmountType", "${notionalAmountType}");
        europeanMap.put("optionType", "${optionType}");
        europeanMap.put("participationRate", "${participationRate}");
        europeanMap.put("premium", "${premium}");
        europeanMap.put("premiumType", "${premiumType}");
        europeanMap.put("settlementDate", "${settlementDate}");
        europeanMap.put("specifiedPrice", "${specifiedPrice}");
        europeanMap.put("strike", "${strike}");
        europeanMap.put("strikeType", "${strikeType}");
        europeanMap.put("underlyerInstrumentId", "${underlyerInstrumentId}");
        europeanMap.put("underlyerMultiplier", "${underlyerMultiplier}");
        ELEMENT_MAP.put("VANILLA_EUROPEAN", europeanMap);
        // 欧式年化
        Map<String, String> europeanAMap =  new HashMap<>();
        europeanAMap.putAll(europeanMap);
        europeanAMap.putAll(annualizedMap);
        ELEMENT_MAP.put("VANILLA_EUROPEAN_ANNUALIZED", europeanAMap);

        // 美式
        Map<String, String> americanMap = new HashMap<>();
        americanMap.putAll(europeanMap);
        ELEMENT_MAP.put("VANILLA_AMERICAN", americanMap);
        // 美式年化
        Map<String, String> americanAMap = new HashMap<>();
        americanAMap.putAll(americanMap);
        americanAMap.putAll(annualizedMap);
        ELEMENT_MAP.put("VANILLA_AMERICAN_ANNUALIZED", americanAMap);

        // 二元
        Map<String, String> digitalMap = new HashMap<>();
        digitalMap.putAll(europeanMap);
        digitalMap.put("observationType", "${observationType}");
        digitalMap.put("payment", "${payment}");
        digitalMap.put("paymentType", "${paymentType}");
        digitalMap.put("rebateType", "${rebateType}");
        ELEMENT_MAP.put("DIGITAL", digitalMap);
        // 二元年化
        Map<String, String> digitalAMap = new HashMap<>();
        digitalAMap.putAll(digitalMap);
        digitalAMap.putAll(annualizedMap);
        ELEMENT_MAP.put("DIGITAL_ANNUALIZED", digitalAMap);

        // 价差
        Map<String, String> spreadMap = new HashMap<>();
        spreadMap.putAll(europeanMap);
        spreadMap.put("highStrike", "${highStrike}");
        spreadMap.put("lowStrike", "${lowStrike}");
        spreadMap.remove("strike", "${strike}");
        ELEMENT_MAP.put("VERTICAL_SPREAD", spreadMap);
        // 价差年化
        Map<String, String> spreadAMap = new HashMap<>();
        spreadAMap.putAll(spreadMap);
        spreadAMap.putAll(annualizedMap);
        spreadAMap.remove("minimumPremium", "${minimumPremium}");
        ELEMENT_MAP.put("VERTICAL_SPREAD_ANNUALIZED", spreadAMap);

        // 单鲨
        Map<String, String> barrierMap = new HashMap<>();
        barrierMap.putAll(europeanMap);
        barrierMap.put("barrier", "${barrier}");
        barrierMap.put("barrierType", "${barrierType}");
        barrierMap.put("knockDirection", "${knockDirection}");
        barrierMap.put("observationType", "${observationType}");
        barrierMap.put("rebate", "${rebate}");
        barrierMap.put("rebateType", "${rebateType}");
        barrierMap.put("rebateUnit", "${rebateUnit}");
        barrierMap.remove("exerciseType", "${exerciseType}");
        ELEMENT_MAP.put("BARRIER", barrierMap);
        // 单鲨年化
        Map<String, String> barrierAMap = new HashMap<>();
        barrierAMap.putAll(barrierMap);
        barrierAMap.putAll(annualizedMap);
        ELEMENT_MAP.put("BARRIER_ANNUALIZED", barrierAMap);

        // 双鲨
        Map<String, String> doubleSharkMap = new HashMap<>();
        doubleSharkMap.putAll(europeanMap);
        doubleSharkMap.put("barrierType", "${barrierType}");
        doubleSharkMap.put("highBarrier", "${highBarrier}");
        doubleSharkMap.put("highParticipationRate", "${highParticipationRate}");
        doubleSharkMap.put("highRebate", "${highRebate}");
        doubleSharkMap.put("highStrike", "${highStrike}");
        doubleSharkMap.put("lowBarrier", "${lowBarrier}");
        doubleSharkMap.put("lowParticipationRate", "${lowParticipationRate}");
        doubleSharkMap.put("lowRebate", "${lowRebate}");
        doubleSharkMap.put("lowStrike", "${lowStrike}");
        doubleSharkMap.put("observationType", "${observationType}");
        doubleSharkMap.put("rebateType", "${rebateType}");
        doubleSharkMap.put("rebateUnit", "${rebateUnit}");
        doubleSharkMap.remove("exerciseType", "${exerciseType}");
        doubleSharkMap.remove("optionType", "${optionType}");
        doubleSharkMap.remove("participationRate", "${participationRate}");
        doubleSharkMap.remove("strike", "${strike}");
        ELEMENT_MAP.put("DOUBLE_SHARK_FIN", doubleSharkMap);
        // 双鲨年化
        Map<String, String> doubleSharkAMap = new HashMap<>();
        doubleSharkAMap.putAll(doubleSharkMap);
        doubleSharkAMap.putAll(annualizedMap);
        ELEMENT_MAP.put("DOUBLE_SHARK_FIN_ANNUALIZED", doubleSharkAMap);

        // 三层
        Map<String, String> doubleDigitalMap = new HashMap<>();
        doubleDigitalMap.putAll(europeanMap);
        doubleDigitalMap.put("highPayment", "${highPayment}");
        doubleDigitalMap.put("highStrike", "${highStrike}");
        doubleDigitalMap.put("lowPayment", "${lowPayment}");
        doubleDigitalMap.put("lowStrike", "${lowStrike}");
        doubleDigitalMap.put("paymentType", "${paymentType}");
        doubleDigitalMap.remove("exerciseType", "${exerciseType}");
        doubleDigitalMap.remove("strike", "${strike}");
        ELEMENT_MAP.put("DOUBLE_DIGITAL", doubleDigitalMap);
        // 三层年化
        Map<String, String> doubleDigitalAMap = new HashMap<>();
        doubleDigitalAMap.putAll(doubleDigitalMap);
        doubleDigitalAMap.putAll(annualizedMap);
        ELEMENT_MAP.put("DOUBLE_DIGITAL_ANNUALIZED", doubleDigitalAMap);

        // 美式双触碰
        Map<String, String> doubleTouchMap = new HashMap<>();
        doubleTouchMap.putAll(europeanMap);
        doubleTouchMap.put("barrierType", "${barrierType}");
        doubleTouchMap.put("highBarrier", "${highBarrier}");
        doubleTouchMap.put("lowBarrier", "${lowBarrier}");
        doubleTouchMap.put("payment", "${payment}");
        doubleTouchMap.put("paymentType", "${paymentType}");
        doubleTouchMap.put("rebateType", "${rebateType}");
        doubleTouchMap.put("touched", "${touched}");
        doubleTouchMap.remove("exerciseType", "${exerciseType}");
        doubleTouchMap.remove("strike", "${strike}");
        doubleTouchMap.remove("strikeType", "${strikeType}");
        ELEMENT_MAP.put("DOUBLE_TOUCH", doubleTouchMap);
        // 美式双触碰 年化
        Map<String, String> doubleTouchAMap = new HashMap<>();
        doubleTouchAMap.putAll(doubleTouchMap);
        doubleTouchAMap.putAll(annualizedMap);
        ELEMENT_MAP.put("DOUBLE_TOUCH_ANNUALIZED", doubleTouchAMap);

        // 美式不双触碰
        Map<String, String> doubleNoTouchMap = new HashMap<>();
        doubleNoTouchMap.putAll(doubleTouchMap);
        ELEMENT_MAP.put("DOUBLE_NO_TOUCH", doubleNoTouchMap);
        // 美式不双触碰 年化
        Map<String, String> doubleNoTouchAMap = new HashMap<>();
        doubleNoTouchAMap.putAll(doubleTouchAMap);
        ELEMENT_MAP.put("DOUBLE_NO_TOUCH_ANNUALIZED", doubleNoTouchAMap);

        // 二元凹式
        Map<String, String> concavaMap = new HashMap<>();
        concavaMap.putAll(europeanMap);
        concavaMap.put("barrierType", "${barrierType}");
        concavaMap.put("concavaed", "${concavaed}");
        concavaMap.put("highBarrier", "${highBarrier}");
        concavaMap.put("lowBarrier", "${lowBarrier}");
        concavaMap.put("payment", "${payment}");
        concavaMap.put("paymentType", "${paymentType}");
        concavaMap.remove("exerciseType", "${exerciseType}");
        concavaMap.remove("optionType", "${optionType}");
        concavaMap.remove("strike", "${strike}");
        concavaMap.remove("strikeType", "${strikeType}");
        ELEMENT_MAP.put("CONCAVA", concavaMap);
        // 二元凹式 年化
        Map<String, String> concavaAMap = new HashMap<>();
        concavaAMap.putAll(concavaMap);
        concavaAMap.putAll(annualizedMap);
        ELEMENT_MAP.put("CONCAVA_ANNUALIZED", concavaAMap);

        // 二元凸式
        Map<String, String> convexMap = new HashMap<>();
        convexMap.putAll(concavaMap);
        ELEMENT_MAP.put("CONVEX", convexMap);
        // 二元凸式 年化
        Map<String, String> convexAMap = new HashMap<>();
        convexAMap.putAll(concavaAMap);
        ELEMENT_MAP.put("CONVEX_ANNUALIZED", convexAMap);

        // 鹰式
        Map<String, String> eagleMap = new HashMap<>();
        eagleMap.putAll(europeanMap);
        eagleMap.put("participationRate1", "${participationRate1}");
        eagleMap.put("participationRate2", "${participationRate2}");
        eagleMap.put("strike1", "${strike1}");
        eagleMap.put("strike2", "${strike2}");
        eagleMap.put("strike3", "${strike3}");
        eagleMap.put("strike4", "${strike4}");
        eagleMap.remove("exerciseType", "${exerciseType}");
        eagleMap.remove("participationRate", "${participationRate}");
        eagleMap.remove("optionType", "${optionType}");
        eagleMap.remove("strike", "${strike}");
        ELEMENT_MAP.put("EAGLE", eagleMap);
        // 鹰式 年化
        Map<String, String> eagleAMap = new HashMap<>();
        eagleAMap.putAll(eagleMap);
        eagleAMap.putAll(annualizedMap);
        ELEMENT_MAP.put("EAGLE_ANNUALIZED", eagleAMap);

        // 区间累积
        Map<String, String> rangeMap = new HashMap<>();
        rangeMap.putAll(europeanMap);
        rangeMap.put("barrierType", "${barrierType}");
        rangeMap.put("fixingObservations", "${fixingObservations}");
        rangeMap.put("highBarrier", "${highBarrier}");
        rangeMap.put("lowBarrier", "${lowBarrier}");
        rangeMap.put("payment", "${payment}");
        rangeMap.put("paymentType", "${paymentType}");
        rangeMap.remove("exerciseType", "${exerciseType}");
        rangeMap.remove("optionType", "${optionType}");
        rangeMap.remove("strike", "${strike}");
        rangeMap.remove("strikeType", "${strikeType}");
        ELEMENT_MAP.put("RANGE_ACCRUALS", rangeMap);
        // 区间累积 年化
        Map<String, String> rangeAMap = new HashMap<>();
        rangeAMap.putAll(rangeMap);
        rangeAMap.putAll(annualizedMap);
        ELEMENT_MAP.put("RANGE_ACCRUALS_ANNUALIZED", rangeAMap);

        //四层
        Map<String, String> tripleMap = new HashMap<>();
        tripleMap.putAll(europeanMap);
        tripleMap.put("payment1", "${payment1}");
        tripleMap.put("payment2", "${payment2}");
        tripleMap.put("payment3", "${payment3}");
        tripleMap.put("paymentType", "${paymentType}");
        tripleMap.put("strike1", "${strike1}");
        tripleMap.put("strike2", "${strike2}");
        tripleMap.put("strike3", "${strike3}");
        tripleMap.remove("exerciseType", "${exerciseType}");
        tripleMap.remove("strike", "${strike}");
        ELEMENT_MAP.put("TRIPLE_DIGITAL", tripleMap);
        //四层年化
        Map<String, String> tripleAMap = new HashMap<>();
        tripleAMap.putAll(tripleMap);
        tripleAMap.putAll(annualizedMap);
        ELEMENT_MAP.put("TRIPLE_DIGITAL_ANNUALIZED", tripleAMap);

        //自定义
        Map<String, String> modelMap = new HashMap<>();
        modelMap.putAll(europeanMap);
        modelMap.put("comment", "${comment}");
        modelMap.remove("exerciseType", "${exerciseType}");
        modelMap.remove("strike", "${strike}");
        modelMap.remove("strikeType", "${strikeType}");
        ELEMENT_MAP.put("MODEL_XY", modelMap);
        //自定义年化
        Map<String, String> modelAMap = new HashMap<>();
        modelAMap.putAll(modelMap);
        modelAMap.putAll(annualizedMap);
        ELEMENT_MAP.put("MODEL_XY_ANNUALIZED", modelAMap);

        // 凤凰
        Map<String, String> phoenixAMap = new HashMap<>();
        phoenixAMap.putAll(europeanMap);
        phoenixAMap.put("barrier", "${barrier}");
        phoenixAMap.put("barrierType", "${barrierType}");
        phoenixAMap.put("couponBarrier", "${couponBarrier}");
        phoenixAMap.put("couponPayment", "${couponPayment}");
        phoenixAMap.put("fixingObservations", "${fixingObservations}");
        phoenixAMap.put("knockDirection", "${knockDirection}");
        phoenixAMap.put("knockInBarrier", "${knockInBarrier}");
        phoenixAMap.put("knockInBarrierType", "${knockInBarrierType}");
        phoenixAMap.put("knockInDate", "${knockInDate}");
        phoenixAMap.put("knockInObservationStep", "${knockInObservationStep}");
        phoenixAMap.put("knockInOptionType", "${knockInOptionType}");
        phoenixAMap.put("knockInStrike", "${knockInStrike}");
        phoenixAMap.put("knockInStrikeType", "${knockInStrikeType}");
        phoenixAMap.put("knockOutObservationStep", "${knockOutObservationStep}");
        phoenixAMap.remove("exerciseType", "${exerciseType}");
        phoenixAMap.remove("optionType", "${optionType}");
        phoenixAMap.remove("strike", "${strike}");
        phoenixAMap.remove("strikeType", "${strikeType}");
        phoenixAMap.putAll(annualizedMap);
        ELEMENT_MAP.put("AUTOCALL_PHOENIX_ANNUALIZED", phoenixAMap);
        // 雪球
        Map<String, String> autocallAMap = new HashMap<>();
        autocallAMap.putAll(europeanMap);
        autocallAMap.put("autoCallPaymentType", "${autoCallPaymentType}");
        autocallAMap.put("barrier", "${barrier}");
        autocallAMap.put("barrierType", "${barrierType}");
        autocallAMap.put("couponPayment", "${couponPayment}");
        autocallAMap.put("fixedPayment", "${fixedPayment}");
        autocallAMap.put("knockDirection", "${knockDirection}");
        autocallAMap.put("knockOutObservationStep", "${knockOutObservationStep}");
        autocallAMap.put("observationDates", "${observationDates}");
        autocallAMap.put("step", "${step}");
        autocallAMap.remove("exerciseType", "${exerciseType}");
        autocallAMap.remove("strike", "${strike}");
        autocallAMap.remove("strikeType", "${strikeType}");
        autocallAMap.remove("optionType", "${optionType}");
        autocallAMap.putAll(annualizedMap);
        ELEMENT_MAP.put("AUTOCALL_ANNUALIZED", autocallAMap);
        // 远期
        Map<String, String> forwardMap = new HashMap<>();
        forwardMap.putAll(europeanMap);
        forwardMap.remove("exerciseType", "${exerciseType}");
        forwardMap.remove("optionType", "${optionType}");
        forwardMap.remove("participationRate", "${participationRate}");
        ELEMENT_MAP.put("FORWARD", forwardMap);

        // 亚式
        Map<String, String> asianMap = new HashMap<>();
        asianMap.putAll(europeanMap);
        asianMap.put("fixingObservations", "${fixingObservations}");
        asianMap.put("fixingWeights", "${fixingWeights}");
        asianMap.put("observationStep", "${observationStep}");
        asianMap.remove("exerciseType", "${exerciseType}");
        ELEMENT_MAP.put("ASIAN", asianMap);
        // 亚式 年化
        Map<String, String> asianAMap = new HashMap<>();
        asianAMap.putAll(asianMap);
        asianAMap.putAll(annualizedMap);
        ELEMENT_MAP.put("ASIAN_ANNUALIZED", asianAMap);

        // 跨式
        Map<String, String> straddleMap = new HashMap<>();
        straddleMap.putAll(europeanMap);
        straddleMap.put("highParticipationRate", "${highParticipationRate}");
        straddleMap.put("highStrike", "${highStrike}");
        straddleMap.put("lowParticipationRate", "${lowParticipationRate}");
        straddleMap.put("lowStrike", "${lowStrike}");
        straddleMap.remove("exerciseType", "${exerciseType}");
        straddleMap.remove("strike", "${strike}");
        straddleMap.remove("optionType", "${optionType}");
        straddleMap.remove("participationRate", "${participationRate}");
        ELEMENT_MAP.put("STRADDLE", straddleMap);
        // 跨式 年化
        Map<String, String> straddleAMap = new HashMap<>();
        straddleAMap.putAll(straddleMap);
        straddleAMap.putAll(annualizedMap);
        ELEMENT_MAP.put("STRADDLE_ANNUALIZED", straddleAMap);
    }
}

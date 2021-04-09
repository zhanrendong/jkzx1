package tech.tongyu.bct.pricing.environment;

import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.exception.ErrorCode;
import tech.tongyu.bct.market.dto.InstanceEnum;
import tech.tongyu.bct.market.dto.QuoteFieldEnum;
import tech.tongyu.bct.model.dto.ModelLocator;
import tech.tongyu.bct.model.dto.ModelTypeEnum;
import tech.tongyu.bct.pricing.common.CorrelationMatrixLocator;
import tech.tongyu.bct.pricing.common.QuoteFieldLocator;
import tech.tongyu.bct.pricing.common.config.PricingConfig;
import tech.tongyu.bct.pricing.common.config.impl.*;
import tech.tongyu.bct.pricing.common.descriptor.PositionDescriptor;
import tech.tongyu.bct.pricing.common.descriptor.impl.*;
import tech.tongyu.bct.pricing.common.rule.PricingRule;
import tech.tongyu.bct.pricing.common.rule.PricingRuleIndex;
import tech.tongyu.bct.pricing.common.rule.impl.*;
import tech.tongyu.bct.pricing.dao.dto.LinearProductRuleDTO;
import tech.tongyu.bct.pricing.dao.dto.PricingEnvironmentDTO;
import tech.tongyu.bct.pricing.dao.dto.SingleAssetOptionRuleDTO;
import tech.tongyu.bct.pricing.dto.BaseContractDTO;
import tech.tongyu.bct.quant.library.common.impl.*;
import tech.tongyu.bct.quant.library.priceable.AssetClass;
import tech.tongyu.bct.quant.library.priceable.Priceable;
import tech.tongyu.bct.quant.library.priceable.feature.ExchangeListed;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

public class PricingEnvironment {
    private final String name;
    private final Map<PricingRuleIndex, PricingRule> ruleMap;
    private final Map<String, BaseContractDTO> baseContractMap;

    public PricingEnvironment(String name,
                              Map<PricingRuleIndex, PricingRule> ruleMap,
                              Map<String, BaseContractDTO> baseContractMap) {
        this.name = name;
        this.ruleMap = ruleMap;
        this.baseContractMap = baseContractMap;
    }

    private  static PricingEnvironment createDefaultPricingEnvironment(
            InstanceEnum instanceEnum,
            Map<String, BaseContractDTO> baseContractMap) {
        String name = "DEFAULT_PRICING_ENVIRONMENT";
        Map<PricingRuleIndex, PricingRule> ruleMap = new HashMap<>();
        BlackScholesAnalyticPricer blackScholesAnalyticPricer =
                new BlackScholesAnalyticPricer(new BlackAnalyticPricerParams());
        Black76AnalyticPricer black76AnalyticPricer =
                new Black76AnalyticPricer(new BlackAnalyticPricerParams());
        BlackMcPricer mcPricer = new BlackMcPricer(
                new BlackMcPricerParams(1234L, 10000, 7./365.,
                        false, false, false));
        QuoteFieldEnum quoteFieldEnum = instanceEnum == InstanceEnum.INTRADAY ?
                QuoteFieldEnum.LAST : QuoteFieldEnum.CLOSE;
        ModelXYPricer modelXYPricer = new ModelXYPricer(new ModelXYPricerParams("PLACE_HOLDER"));
        // custom model xy
        SingleAssetOptionPricingRuleIndex modelXYRuleIndex = new SingleAssetOptionPricingRuleIndex(
                AssetClass.AssetClassEnum.ANY,
                Priceable.PriceableTypeEnum.CUSTOM_MODEL_XY,
                ExchangeListed.InstrumentTypeEnum.ANY, null);
        SingleAssetOptionPricingRule modelXYRule = new SingleAssetOptionPricingRule(instanceEnum,
                quoteFieldEnum,
                "PLACEHOLDER", instanceEnum,
                "PLACEHOLDER", instanceEnum,
                "PLACEHOLDER", instanceEnum,
                modelXYPricer);
        ruleMap.put(modelXYRuleIndex, modelXYRule);
        // option on basket
        SingleAssetOptionPricingRuleIndex basketRuleIndex = new SingleAssetOptionPricingRuleIndex(
                AssetClass.AssetClassEnum.ANY,
                Priceable.PriceableTypeEnum.GENERIC_MULTI_ASSET_OPTION,
                ExchangeListed.InstrumentTypeEnum.ANY, null);
        SingleAssetOptionPricingRule basketRule = new SingleAssetOptionPricingRule(instanceEnum,
                quoteFieldEnum, "TRADER_RISK_FREE_CURVE", instanceEnum,
                "TRADER_VOL", instanceEnum,
                "TRADER_DIVIDEND_CURVE", instanceEnum,
                blackScholesAnalyticPricer);
        ruleMap.put(basketRuleIndex, basketRule);
        // options
        //   equity stock
        SingleAssetOptionPricingRuleIndex equityStock = new SingleAssetOptionPricingRuleIndex(
                AssetClass.AssetClassEnum.EQUITY,
                Priceable.PriceableTypeEnum.GENERIC_SINGLE_ASSET_OPTION,
                ExchangeListed.InstrumentTypeEnum.EQUITY_STOCK, null);
        SingleAssetOptionPricingRule equityStockRule = new SingleAssetOptionPricingRule(instanceEnum,
                quoteFieldEnum, "TRADER_RISK_FREE_CURVE", instanceEnum,
                "TRADER_VOL", instanceEnum,
                "TRADER_DIVIDEND_CURVE", instanceEnum,
                blackScholesAnalyticPricer);
        SingleAssetOptionPricingRuleIndex equityAutocall = new SingleAssetOptionPricingRuleIndex(
                AssetClass.AssetClassEnum.EQUITY,
                Priceable.PriceableTypeEnum.EQUITY_AUTOCALL,
                ExchangeListed.InstrumentTypeEnum.EQUITY_STOCK, null);
        SingleAssetOptionPricingRule equityAutocallRule = new SingleAssetOptionPricingRule(instanceEnum,
                quoteFieldEnum, "TRADER_RISK_FREE_CURVE", instanceEnum,
                "TRADER_VOL", instanceEnum,
                "TRADER_DIVIDEND_CURVE", instanceEnum,
                mcPricer);
        ruleMap.put(equityStock, equityStockRule);
        ruleMap.put(equityAutocall, equityAutocallRule);
        //   equity index
        SingleAssetOptionPricingRuleIndex equityIndex = new SingleAssetOptionPricingRuleIndex(
                AssetClass.AssetClassEnum.EQUITY,
                Priceable.PriceableTypeEnum.GENERIC_SINGLE_ASSET_OPTION,
                ExchangeListed.InstrumentTypeEnum.EQUITY_INDEX, null);
        SingleAssetOptionPricingRule equityIndexRule = new SingleAssetOptionPricingRule(instanceEnum,
                quoteFieldEnum, "TRADER_RISK_FREE_CURVE", instanceEnum,
                "TRADER_VOL", instanceEnum,
                "TRADER_DIVIDEND_CURVE", instanceEnum,
                blackScholesAnalyticPricer);
        SingleAssetOptionPricingRuleIndex equityIndexAutocall = new SingleAssetOptionPricingRuleIndex(
                AssetClass.AssetClassEnum.EQUITY,
                Priceable.PriceableTypeEnum.EQUITY_AUTOCALL,
                ExchangeListed.InstrumentTypeEnum.EQUITY_INDEX, null);
        ruleMap.put(equityIndex, equityIndexRule);
        ruleMap.put(equityIndexAutocall, equityAutocallRule);
        //   equity index futures
        SingleAssetOptionPricingRuleIndex equityIndexFutures = new SingleAssetOptionPricingRuleIndex(
                AssetClass.AssetClassEnum.EQUITY,
                Priceable.PriceableTypeEnum.GENERIC_SINGLE_ASSET_OPTION,
                ExchangeListed.InstrumentTypeEnum.EQUITY_INDEX_FUTURES, null);
        SingleAssetOptionPricingRule equityIndexFuturesRule = new SingleAssetOptionPricingRule(instanceEnum,
                quoteFieldEnum, "TRADER_RISK_FREE_CURVE", instanceEnum,
                "TRADER_VOL", instanceEnum,
                null, null,
                black76AnalyticPricer);
        SingleAssetOptionPricingRuleIndex equityIndexFuturesAutocall = new SingleAssetOptionPricingRuleIndex(
                AssetClass.AssetClassEnum.EQUITY,
                Priceable.PriceableTypeEnum.EQUITY_AUTOCALL,
                ExchangeListed.InstrumentTypeEnum.EQUITY_INDEX_FUTURES, null);
        SingleAssetOptionPricingRule equityIndexFuturesAutocallRule = new SingleAssetOptionPricingRule(instanceEnum,
                quoteFieldEnum, "TRADER_RISK_FREE_CURVE", instanceEnum,
                "TRADER_VOL", instanceEnum,
                null, null,
                mcPricer);
        ruleMap.put(equityIndexFutures, equityIndexFuturesRule);
        ruleMap.put(equityIndexFuturesAutocall, equityIndexFuturesAutocallRule);
        // commodity
        //   commdity spot
        SingleAssetOptionPricingRuleIndex commoditySpot = new SingleAssetOptionPricingRuleIndex(
                AssetClass.AssetClassEnum.COMMODITY,
                Priceable.PriceableTypeEnum.GENERIC_SINGLE_ASSET_OPTION,
                ExchangeListed.InstrumentTypeEnum.COMMODITY_SPOT, null);
        SingleAssetOptionPricingRule commoditySpotRule = new SingleAssetOptionPricingRule(instanceEnum,
                quoteFieldEnum, "TRADER_RISK_FREE_CURVE", instanceEnum,
                "TRADER_VOL", instanceEnum,
                "TRADER_DIVIDEND_CURVE", instanceEnum,
                blackScholesAnalyticPricer);
        ruleMap.put(commoditySpot, commoditySpotRule);
        //    commodity futures
        SingleAssetOptionPricingRuleIndex commodityFutures = new SingleAssetOptionPricingRuleIndex(
                AssetClass.AssetClassEnum.COMMODITY,
                Priceable.PriceableTypeEnum.GENERIC_SINGLE_ASSET_OPTION,
                ExchangeListed.InstrumentTypeEnum.COMMODITY_FUTURES, null);
        SingleAssetOptionPricingRule commdityFuturesRule = new SingleAssetOptionPricingRule(instanceEnum,
                quoteFieldEnum, "TRADER_RISK_FREE_CURVE", instanceEnum,
                "TRADER_VOL", instanceEnum,
                null, null,
                black76AnalyticPricer);
        ruleMap.put(commodityFutures, commdityFuturesRule);
        return new PricingEnvironment(name, ruleMap, baseContractMap);
    }

    private  static PricingEnvironment createDefaultPricingEnvironment(
            Map<String, BaseContractDTO> baseContractMap) {
        return createDefaultPricingEnvironment(InstanceEnum.INTRADAY, baseContractMap);
    }

    public static PricingEnvironment from(PricingEnvironmentDTO dto, Map<String, BaseContractDTO> baseContractMap) {
        String name = dto.getPricingEnvironmentId();
        // cash
        Map<PricingRuleIndex, PricingRule> ruleMap = new HashMap<>();
        ruleMap.put(new CashPricingRuleIndex(),
                new CashPricingRule(
                        dto.getCashRule().getDiscounted(),
                        dto.getCashRule().getCurveName(),
                        dto.getCashRule().getInstance()));
        // linear products
        for (LinearProductRuleDTO l : dto.getLinearProductRules()) {
            ruleMap.put(new LinearProductPricingRuleIndex(l.getAssetClass(), l.getInstrumentType()),
                    new LinearProductPricingRule(l.getInstance(), l.getField()));
        }
        // options (single asset)
        for (SingleAssetOptionRuleDTO s : dto.getSingleAssetOptionRules()) {
            ruleMap.put(new SingleAssetOptionPricingRuleIndex(s.getAssetClass(), s.getProductType(),
                            s.getUnderlyerType(), s.getPositionId()),
                    new SingleAssetOptionPricingRule(s.getUnderlyerInstance(), s.getUnderlyerField(),
                            s.getDiscountingCurveName(), s.getDiscountingCurveInstance(),
                            s.getVolSurfaceName(), s.getVolSurfaceInstance(),
                            s.getDividendCurveName(), s.getDividendCurveInstance(),
                            s.getPricer()));
        }
        return new PricingEnvironment(name, ruleMap, baseContractMap);
    }

    private CashPricingConfig planCashPosition(CashPositionDescriptor cash,
                                               LocalDateTime valuationTime,
                                               ZoneId timezone) {
        PricingRuleIndex ruleIndex = cash.getRuleIndex();
        if (ruleMap.containsKey(ruleIndex)) {
            CashPricingRule rule = (CashPricingRule) ruleMap.get(ruleIndex);
            if (rule.getDiscounted()) {
                return new CashPricingConfig(rule.getDiscounted(),
                        new ModelLocator(
                                ModelTypeEnum.RISK_FREE_CURVE,
                                rule.getCurveName(),
                                null,
                                rule.getInstance(),
                                valuationTime.toLocalDate(),
                                timezone
                        ));
            } else {
                return new CashPricingConfig(false, null);
            }

        } else {
            return new CashPricingConfig(false, null);
        }
    }

    private LinearProductPricingConfig planLinearProductPosition(LinearProductPositionDescriptor linearProduct,
                                                                 LocalDateTime valuationTime,
                                                                 ZoneId timezone) {
        PricingRuleIndex ruleIndex = linearProduct.getRuleIndex();
        if (ruleMap.containsKey(ruleIndex)) {
            LinearProductPricingRule rule = (LinearProductPricingRule) ruleMap.get(ruleIndex);
            return new LinearProductPricingConfig(new QuoteFieldLocator(
                    linearProduct.getInstrumentId(),
                    rule.getInstance(),
                    valuationTime.toLocalDate(),
                    rule.getField(),
                    timezone
            ));
        } else {
            InstanceEnum defaultInstance = InstanceEnum.CLOSE;
            QuoteFieldEnum defaultQuoteField = QuoteFieldEnum.CLOSE;
            ExchangeListed.InstrumentTypeEnum instrumentType = linearProduct.getInstrumentType();
            if (instrumentType == ExchangeListed.InstrumentTypeEnum.COMMODITY_FUTURES ||
                    linearProduct.getInstrumentType() == ExchangeListed.InstrumentTypeEnum.EQUITY_INDEX_FUTURES) {
                defaultQuoteField = QuoteFieldEnum.SETTLE;
            }
            return new LinearProductPricingConfig(new QuoteFieldLocator(
                    linearProduct.getInstrumentId(),
                    defaultInstance,
                    valuationTime.toLocalDate(),
                    defaultQuoteField,
                    timezone
            ));
        }
    }

    private SingleAssetOptionPricingRule getRule(OptionPositionDescriptor option) {
        SingleAssetOptionPricingRuleIndex ruleIndex = (SingleAssetOptionPricingRuleIndex) option.getRuleIndex();
        // precedence:
        //   positionId: if this is known, other characteristics are all known
        //   try (asset class, product type, underlyer type, position id = null)
        //   try (asset class, product type = generic optiontype (place holder for any option), underlyer type, position id = null)
        SingleAssetOptionPricingRule rule;
        if (ruleMap.containsKey(ruleIndex)) {
            rule = (SingleAssetOptionPricingRule) ruleMap.get(ruleIndex);
        } else {
            ruleIndex = ruleIndex.withoutPositionId();
            if (ruleMap.containsKey(ruleIndex)) {
                rule = (SingleAssetOptionPricingRule) ruleMap.get(ruleIndex);
            } else {
                ruleIndex = ruleIndex.toGenericOptionType();
                if (ruleMap.containsKey(ruleIndex)) {
                    rule = (SingleAssetOptionPricingRule) ruleMap.get(ruleIndex);
                } else {
                    throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                            String.format("定价环境(%s)未对产品类型(%s)进行配置", name, option.getProductType()));
                }
            }
        }
        return rule;
    }

    private OptionOnSpotPricingConfig planOptionOnSpot(OptionPositionDescriptor option,
                                                       LocalDateTime valuationTime,
                                                       ZoneId timezone) {
        SingleAssetOptionPricingRule rule = getRule(option);

        // underlyer
        QuoteFieldLocator underlyer = new QuoteFieldLocator(
                option.getUnderlyerInstrumentId(),
                rule.getUnderlyerInstance(),
                valuationTime.toLocalDate(),
                rule.getUnderlyerField(),
                timezone
        );
        // discount curve
        ModelLocator discount = new ModelLocator(
                ModelTypeEnum.RISK_FREE_CURVE,
                rule.getDiscountingCurveName(),
                null,
                rule.getDiscountingCurveInstance(),
                valuationTime.toLocalDate(),
                timezone
        );
        // dividend curve
        ModelLocator dividend = new ModelLocator(
                ModelTypeEnum.DIVIDEND_CURVE,
                rule.getDividendCurveName(),
                option.getUnderlyerInstrumentId(),
                rule.getDividendCurveInstance(),
                valuationTime.toLocalDate(),
                timezone
        );
        // vol surface
        ModelLocator vol = new ModelLocator(
                ModelTypeEnum.VOL_SURFACE,
                rule.getVolSurfaceName(),
                option.getUnderlyerInstrumentId(),
                rule.getVolSurfaceInstance(),
                valuationTime.toLocalDate(),
                timezone
        );
        return new OptionOnSpotPricingConfig(underlyer, discount, dividend, vol, rule.getPricer());
    }

    private OptionOnForwardPricingConfig planOptionOnForward(OptionPositionDescriptor option,
                                                          LocalDateTime valuationTime,
                                                          ZoneId timezone) {
        SingleAssetOptionPricingRule rule = getRule(option);
        // underlyer
        QuoteFieldLocator underlyer = new QuoteFieldLocator(
                option.getUnderlyerInstrumentId(),
                rule.getUnderlyerInstance(),
                valuationTime.toLocalDate(),
                rule.getUnderlyerField(),
                timezone
        );
        // discount curve
        ModelLocator discount = new ModelLocator(
                ModelTypeEnum.RISK_FREE_CURVE,
                rule.getDiscountingCurveName(),
                null,
                rule.getDiscountingCurveInstance(),
                valuationTime.toLocalDate(),
                timezone
        );
        // vol surface
        ModelLocator vol = new ModelLocator(
                ModelTypeEnum.VOL_SURFACE,
                rule.getVolSurfaceName(),
                option.getUnderlyerInstrumentId(),
                rule.getVolSurfaceInstance(),
                valuationTime.toLocalDate(),
                timezone
        );
        return new OptionOnForwardPricingConfig(underlyer, discount, vol, rule.getPricer());
    }

    private OptionWithBaseContractPricingConfig planOptionWithBaseContract(OptionPositionDescriptor option,
                                                                           BaseContractDTO baseContractDTO,
                                                                           LocalDateTime valuationTime,
                                                                           ZoneId timezone) {
        SingleAssetOptionPricingRule rule = getRule(option);
        // underlyer
        QuoteFieldLocator underlyer = new QuoteFieldLocator(
                option.getUnderlyerInstrumentId(),
                rule.getUnderlyerInstance(),
                valuationTime.toLocalDate(),
                rule.getUnderlyerField(),
                timezone
        );
        // base contract
        QuoteFieldLocator baseContract = new QuoteFieldLocator(
                baseContractDTO.getBaseContractId(),
                rule.getUnderlyerInstance(),
                valuationTime.toLocalDate(),
                rule.getUnderlyerField(),
                timezone
        );
        // discount curve
        ModelLocator discount = new ModelLocator(
                ModelTypeEnum.RISK_FREE_CURVE,
                rule.getDiscountingCurveName(),
                null,
                rule.getDiscountingCurveInstance(),
                valuationTime.toLocalDate(),
                timezone
        );
        // vol surface
        ModelLocator vol = new ModelLocator(
                ModelTypeEnum.VOL_SURFACE,
                rule.getVolSurfaceName(),
                baseContractDTO.getBaseContractId(),
                rule.getVolSurfaceInstance(),
                valuationTime.toLocalDate(),
                timezone
        );
        return new OptionWithBaseContractPricingConfig(underlyer, baseContract,
                baseContractDTO.getBaseContractMaturity(), baseContractDTO.getHedgingContractId(),
                discount, vol, rule.getPricer());
    }

    private CustomProductModelXYPricingConfig planCustomProductModelXY(CustomProductModelXYPositionDescriptor option,
                                                              LocalDateTime valuationTime,
                                                              ZoneId timezone) {
        SingleAssetOptionPricingRuleIndex index = option.getRuleIndex();
        if (!ruleMap.containsKey(index)) {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                    String.format("定价环境(%s)未对产品类型 CUSTOM_MODEL_XY 进行配置", name));
        }
        SingleAssetOptionPricingRule rule = (SingleAssetOptionPricingRule) ruleMap.get(index);
        // underlyer
        QuoteFieldLocator underlyer = new QuoteFieldLocator(
                option.getUnderlyerInstrumentId(),
                rule.getUnderlyerInstance(),
                valuationTime.toLocalDate(),
                rule.getUnderlyerField(),
                timezone
        );
        // model name
        // this is a hack: we use trade id as part of model name: MODEL_XY_<TRADE_ID>
        // this is a hack: we rely on position id = trade_id_<digit>
        List<String> splitted = Arrays.asList(option.getPositionId().split("_"));
        String tradeId = String.join("_", splitted.subList(0, splitted.size()-1));
        String modelName = ModelTypeEnum.MODEL_XY.name() + "_" + tradeId;
        // create model locator
        ModelLocator modelLocator = new ModelLocator(ModelTypeEnum.MODEL_XY, modelName,
                option.getUnderlyerInstrumentId(), rule.getUnderlyerInstance(),
                valuationTime.toLocalDate(), timezone);
        return new CustomProductModelXYPricingConfig(underlyer, modelLocator,
                new ModelXYPricer(new ModelXYPricerParams(modelLocator.toString())));
    }

    private OptionOnBasketPricingConfig planOptionOnBasket(OptionOnBasketPositionDescriptor option,
                                                           LocalDateTime valuationTime,
                                                           ZoneId timezone) {
        SingleAssetOptionPricingRuleIndex index = option.getRuleIndex();
        if (!ruleMap.containsKey(index)) {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                    String.format("定价环境(%s)未对产品类型 GENERIC_MULTI_ASSET_OPTION 进行配置", name));
        }
        SingleAssetOptionPricingRule rule = (SingleAssetOptionPricingRule) ruleMap.get(index);
        // we only use instance, model name and pricer from single asset option pricing rule
        // basically we don't distinguish between the basket components, i.e. all the components share the same
        // quote and model specs
        // underlyer
        List<String> underlyerInstrumentIds = option.getUnderlyerInstrumentIds();
        List<QuoteFieldLocator> underlyerPrices = underlyerInstrumentIds.stream()
                .map(s -> new QuoteFieldLocator(s, rule.getUnderlyerInstance(),
                        valuationTime.toLocalDate(), rule.getUnderlyerField(), timezone))
                .collect(Collectors.toList());
        // discount curve
        ModelLocator discount = new ModelLocator(
                ModelTypeEnum.RISK_FREE_CURVE,
                rule.getDiscountingCurveName(),
                null,
                rule.getDiscountingCurveInstance(),
                valuationTime.toLocalDate(),
                timezone
        );
        // dividend curves
        List<ModelLocator> dividendCurves = underlyerInstrumentIds.stream()
                .map(s -> new ModelLocator(ModelTypeEnum.DIVIDEND_CURVE,
                        rule.getDividendCurveName(),
                        s,
                        rule.getDividendCurveInstance(),
                        valuationTime.toLocalDate(),
                        timezone))
                .collect(Collectors.toList());
        // vol surface
        List<ModelLocator> volSurfaces = underlyerInstrumentIds.stream()
                .map(s -> new ModelLocator(
                        ModelTypeEnum.VOL_SURFACE,
                        rule.getVolSurfaceName(),
                        s,
                        rule.getVolSurfaceInstance(),
                        valuationTime.toLocalDate(),
                        timezone))
                .collect(Collectors.toList());
        return new OptionOnBasketPricingConfig(underlyerPrices,
                volSurfaces, discount, dividendCurves, rule.getPricer());
    }

    public PricingConfig plan(PositionDescriptor position,
                              LocalDateTime valuationTime,
                              ZoneId timezone) {
        if (position instanceof CashPositionDescriptor) {
            return planCashPosition((CashPositionDescriptor) position, valuationTime, timezone);
        } else if (position instanceof LinearProductPositionDescriptor) {
            return planLinearProductPosition((LinearProductPositionDescriptor) position, valuationTime, timezone);
        } else if (position instanceof OptionPositionDescriptor) {
            if (!Objects.isNull(baseContractMap) &&
                    baseContractMap.containsKey(((OptionPositionDescriptor) position).getPositionId())) {
                return planOptionWithBaseContract((OptionPositionDescriptor) position,
                        baseContractMap.get(((OptionPositionDescriptor) position).getPositionId()),
                        valuationTime, timezone);
            } else {
                switch (((OptionPositionDescriptor) position).getUnderlyerType()) {
                    case COMMODITY_SPOT:
                    case EQUITY_STOCK:
                    case EQUITY_INDEX:
                        return planOptionOnSpot((OptionPositionDescriptor) position, valuationTime, timezone);
                    case COMMODITY_FUTURES:
                    case EQUITY_INDEX_FUTURES:
                        return planOptionOnForward((OptionPositionDescriptor) position, valuationTime, timezone);
                    default:
                        throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                                String.format("定价环境(%s)未对产品类型(%s), 标的物类型(%s)进行配置",
                                        name,
                                        ((OptionPositionDescriptor) position).getProductType(),
                                        ((OptionPositionDescriptor) position).getUnderlyerType()));
                }
            }
        } else if (position instanceof CustomProductModelXYPositionDescriptor) {
            return planCustomProductModelXY((CustomProductModelXYPositionDescriptor) position,
                    valuationTime, timezone);
        } else if (position instanceof OptionOnBasketPositionDescriptor) {
            return planOptionOnBasket((OptionOnBasketPositionDescriptor)position, valuationTime, timezone);
        } else {
            // unreachable
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "未知类型: " + position.getClass().getName());
        }
    }

    public static PricingEnvironment getDefaultPricingEnvironment(Map<String, BaseContractDTO> baseContractMap) {
        return createDefaultPricingEnvironment(baseContractMap);
    }
}

package tech.tongyu.bct.pricing.convert;


import io.vavr.API;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import tech.tongyu.bct.cm.product.asset.Asset;
import tech.tongyu.bct.cm.product.iov.*;
import tech.tongyu.bct.cm.product.iov.BasketInstrument;
import tech.tongyu.bct.cm.product.iov.BasketInstrumentConstituent;
import tech.tongyu.bct.cm.product.iov.cmd.impl.CommodityFutureInstrument;
import tech.tongyu.bct.cm.product.iov.cmd.impl.CommodityInstrument;
import tech.tongyu.bct.cm.product.iov.eqd.ListedEquityInstrument;
import tech.tongyu.bct.cm.product.iov.eqd.impl.EquityIndexFutureInstrument;
import tech.tongyu.bct.cm.product.iov.eqd.impl.EquityIndexInstrument;
import tech.tongyu.bct.cm.product.iov.feature.OptionTypeEnum;
import tech.tongyu.bct.cm.product.iov.impl.*;
import tech.tongyu.bct.cm.reference.impl.CurrencyUnit;
import tech.tongyu.bct.cm.trade.impl.BctTrade;
import tech.tongyu.bct.cm.trade.impl.BctTradePosition;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.exception.ErrorCode;
import tech.tongyu.bct.common.util.CollectionUtils;
import tech.tongyu.bct.pricing.common.Diagnostic;
import tech.tongyu.bct.quant.library.common.DoubleUtils;
import tech.tongyu.bct.quant.library.priceable.Equity;
import tech.tongyu.bct.quant.library.priceable.Position;
import tech.tongyu.bct.quant.library.priceable.Priceable;
import tech.tongyu.bct.quant.library.priceable.cash.CashPayment;
import tech.tongyu.bct.quant.library.priceable.commodity.*;
import tech.tongyu.bct.quant.library.priceable.common.flag.BarrierDirectionEnum;
import tech.tongyu.bct.quant.library.priceable.common.flag.BarrierTypeEnum;
import tech.tongyu.bct.quant.library.priceable.common.flag.ObservationTypeEnum;
import tech.tongyu.bct.quant.library.priceable.common.product.CustomProductModelXY;
import tech.tongyu.bct.quant.library.priceable.common.product.basket.RatioVanillaEuropean;
import tech.tongyu.bct.quant.library.priceable.common.product.basket.SpreadVanillaEuropean;
import tech.tongyu.bct.quant.library.priceable.equity.*;
import tech.tongyu.bct.quant.library.priceable.feature.ExchangeListed;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.Predicates.instanceOf;
import static tech.tongyu.bct.quant.library.priceable.common.flag.OptionTypeEnum.CALL;
import static tech.tongyu.bct.quant.library.priceable.common.flag.OptionTypeEnum.PUT;

public class BctTradeToQuantModel {

    private static <T extends Equity & ExchangeListed> T fromInstrumentOfValue(InstrumentOfValue iov) {
        return null;
    }

    private static Position from(String positionId, InstrumentOfValuePartyRoleTypeEnum buySell,
                                 CashFlow<InstrumentOfValue> cashflow) {
        LocalDate paymentDate = cashflow.settlementDate();
        double amount = cashflow.paymentAmount().doubleValue();
        double quantity = cashflow.paymentDirection() == CashFlowDirectionEnum.RECEIVE ? 1.0 : -1.0;
        return new Position(positionId, quantity, new CashPayment(paymentDate, amount));
    }

    private static Position from(String positionId, InstrumentOfValuePartyRoleTypeEnum buySell,
                                 Forward<InstrumentOfValue> forward) {
        LocalDateTime expiry = LocalDateTime.of(forward.absoluteExpirationDate(), forward.absoluteExpirationTime());
        LocalDate deliveryDate = forward.settlementDate();
        double strike = forward.strikeValue().doubleValue();
        double quantity = forward.notionalAmountValue().doubleValue() / forward.initialSpot().doubleValue();
        if (buySell == InstrumentOfValuePartyRoleTypeEnum.SELLER) {
            quantity = -quantity;
        }
        String underlyerInstrumentId = forward.underlyer().instrumentId();
        Priceable priceable;
        if (forward.underlyer() instanceof ListedEquityInstrument) {
            EquityStock underlyer = new EquityStock(underlyerInstrumentId);
            priceable = new EquityForward<>(underlyer, strike, expiry, deliveryDate);
        } else if (forward.underlyer() instanceof EquityIndexInstrument) {
            EquityIndex underlyer = new EquityIndex(underlyerInstrumentId);
            priceable = new EquityForward<>(underlyer, strike, expiry, deliveryDate);
        } else if (forward.underlyer() instanceof EquityIndexFutureInstrument) {
            EquityIndexFutures underlyer = new EquityIndexFutures(underlyerInstrumentId);
            priceable = new EquityForward<>(underlyer, strike, expiry, deliveryDate);
        } else if (forward.underlyer() instanceof CommodityFutureInstrument) {
            CommodityFutures underlyer = new CommodityFutures(underlyerInstrumentId);
            priceable = new CommodityForward<>(underlyer, strike, expiry, deliveryDate);
        } else if (forward.underlyer() instanceof CommodityInstrument) {
            CommoditySpot underlyer = new CommoditySpot(underlyerInstrumentId);
            priceable = new CommodityForward<>(underlyer, strike, expiry, deliveryDate);
        } else {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                    String.format("定价：期权标的类型不支持: %s", forward.underlyer().getClass().getSimpleName()));
        }
        return new Position(positionId, quantity, priceable);
    }

    private static Position from(String positionId, InstrumentOfValuePartyRoleTypeEnum buySell,
                                 AnnualizedConcavaConvexOption option) {
        double notional = option.notionalWithParticipation().doubleValue();
        double initialSpot = option.initialSpot().doubleValue();
        double quantity = notional / initialSpot;
        double payment = option.paymentValue().doubleValue() / quantity;
        if (buySell == InstrumentOfValuePartyRoleTypeEnum.SELLER) {
            quantity = -quantity;
        }
        double lowBarrier = option.lowBarrierValue().doubleValue();
        double highBarrier = option.highBarrierValue().doubleValue();

        LocalDate expirationDate = option.absoluteExpirationDate();
        LocalTime expirationTime = option.absoluteExpirationTime();
        // get underlyer depending on types
        Priceable priceable;
        String underlyerInstrumentId = option.underlyer.instrumentId();
        if (option.isConcavaed()) { // 凹式
            if (option.underlyer instanceof ListedEquityInstrument) {
                EquityStock underlyer = new EquityStock(underlyerInstrumentId);
                priceable = new EquityDigitalConcave<>(underlyer, LocalDateTime.of(expirationDate, expirationTime),
                        lowBarrier, highBarrier, payment);
            } else if (option.underlyer instanceof EquityIndexInstrument) {
                EquityIndex underlyer = new EquityIndex(underlyerInstrumentId);
                priceable = new EquityDigitalConcave<>(underlyer, LocalDateTime.of(expirationDate, expirationTime),
                        lowBarrier, highBarrier, payment);
            } else if (option.underlyer() instanceof EquityIndexFutureInstrument) {
                EquityIndexFutures underlyer = new EquityIndexFutures(underlyerInstrumentId);
                priceable = new EquityDigitalConcave<>(underlyer, LocalDateTime.of(expirationDate, expirationTime),
                        lowBarrier, highBarrier, payment);
            } else if (option.underlyer instanceof CommodityFutureInstrument) {
                CommodityFutures futures = new CommodityFutures(underlyerInstrumentId);
                priceable = new CommodityDigitalConcave<>(futures, LocalDateTime.of(expirationDate, expirationTime),
                        lowBarrier, highBarrier, payment);
            } else if (option.underlyer instanceof CommodityInstrument) {
                CommoditySpot underlyer = new CommoditySpot(underlyerInstrumentId);
                priceable = new CommodityDigitalConcave<>(underlyer, LocalDateTime.of(expirationDate, expirationTime),
                        lowBarrier, highBarrier, payment);
            } else {
                throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                        String.format("定价：期权标的类型不支持: %s", option.underlyer().getClass().getSimpleName()));
            }
        } else { // 凸式
            if (option.underlyer instanceof ListedEquityInstrument) {
                EquityStock underlyer = new EquityStock(underlyerInstrumentId);
                priceable = new EquityDigitalConvex<>(underlyer, LocalDateTime.of(expirationDate, expirationTime),
                        lowBarrier, highBarrier, payment);
            } else if (option.underlyer instanceof EquityIndexInstrument) {
                EquityIndex underlyer = new EquityIndex(underlyerInstrumentId);
                priceable = new EquityDigitalConvex<>(underlyer, LocalDateTime.of(expirationDate, expirationTime),
                        lowBarrier, highBarrier, payment);
            } else if (option.underlyer() instanceof EquityIndexFutureInstrument) {
                EquityIndexFutures underlyer = new EquityIndexFutures(underlyerInstrumentId);
                priceable = new EquityDigitalConvex<>(underlyer, LocalDateTime.of(expirationDate, expirationTime),
                        lowBarrier, highBarrier, payment);
            } else if (option.underlyer instanceof CommodityFutureInstrument) {
                CommodityFutures futures = new CommodityFutures(underlyerInstrumentId);
                priceable = new CommodityDigitalConvex<>(futures, LocalDateTime.of(expirationDate, expirationTime),
                        lowBarrier, highBarrier, payment);
            } else if (option.underlyer instanceof CommodityInstrument) {
                CommoditySpot underlyer = new CommoditySpot(underlyerInstrumentId);
                priceable = new CommodityDigitalConvex<>(underlyer, LocalDateTime.of(expirationDate, expirationTime),
                        lowBarrier, highBarrier, payment);
            } else {
                throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                        String.format("定价：期权标的类型不支持: %s", option.underlyer().getClass().getSimpleName()));
            }
        }
        return new Position(positionId, quantity, priceable);
    }

    private static Position from(String positionId, InstrumentOfValuePartyRoleTypeEnum buySell,
                                 AnnualizedDoubleDigitalOption option) {
        double notional = option.notionalWithParticipation().doubleValue();
        double initialSpot = option.initialSpot().doubleValue();
        double quantity = notional / initialSpot;
        double lowStrike = option.lowStrikeValue().doubleValue();
        double highStrike = option.highStrikeValue().doubleValue();
        double lowPayment = option.lowPaymentValue().doubleValue() / quantity;
        double highPayment = option.highPaymentValue().doubleValue() / quantity;
        if (buySell == InstrumentOfValuePartyRoleTypeEnum.SELLER) {
            quantity = -quantity;
        }
        tech.tongyu.bct.quant.library.priceable.common.flag.OptionTypeEnum optionType =
                option.optionType == OptionTypeEnum.CALL ? CALL : PUT;

        LocalDate expirationDate = option.absoluteExpirationDate();
        LocalTime expirationTime = option.absoluteExpirationTime();
        // get underlyer depending on types
        Priceable priceable;
        String underlyerInstrumentId = option.underlyer.instrumentId();
        if (option.underlyer instanceof ListedEquityInstrument) {
            EquityStock underlyer = new EquityStock(underlyerInstrumentId);
            priceable = new EquityDoubleDigital<>(underlyer, optionType, LocalDateTime.of(expirationDate, expirationTime),
                    lowStrike, lowPayment, highStrike, highPayment);
        } else if (option.underlyer instanceof EquityIndexInstrument) {
            EquityIndex underlyer = new EquityIndex(underlyerInstrumentId);
            priceable = new EquityDoubleDigital<>(underlyer, optionType, LocalDateTime.of(expirationDate, expirationTime),
                    lowStrike, lowPayment, highStrike, highPayment);
        } else if (option.underlyer() instanceof EquityIndexFutureInstrument) {
            EquityIndexFutures underlyer = new EquityIndexFutures(underlyerInstrumentId);
            priceable = new EquityDoubleDigital<>(underlyer, optionType, LocalDateTime.of(expirationDate, expirationTime),
                    lowStrike, lowPayment, highStrike, highPayment);
        } else if (option.underlyer instanceof CommodityFutureInstrument) {
            CommodityFutures futures = new CommodityFutures(underlyerInstrumentId);
            priceable = new CommodityDoubleDigital<>(futures, optionType, LocalDateTime.of(expirationDate, expirationTime),
                    lowStrike, lowPayment, highStrike, highPayment);
        } else if (option.underlyer instanceof CommodityInstrument) {
            CommoditySpot underlyer = new CommoditySpot(underlyerInstrumentId);
            priceable = new CommodityDoubleDigital<>(underlyer, optionType, LocalDateTime.of(expirationDate, expirationTime),
                    lowStrike, lowPayment, highStrike, highPayment);
        } else {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                    String.format("定价：期权标的类型不支持: %s", option.underlyer().getClass().getSimpleName()));
        }
        return new Position(positionId, quantity, priceable);
    }

    private static Position from(String positionId, InstrumentOfValuePartyRoleTypeEnum buySell,
                                 AnnualizedTripleDigitalOption option) {
        double notional = option.notionalWithParticipation().doubleValue();
        double initialSpot = option.initialSpot().doubleValue();
        double quantity = notional / initialSpot;
        double strike1 = option.strike1Value().doubleValue();
        double strike2 = option.strike2Value().doubleValue();
        double strike3 = option.strike3Value().doubleValue();
        double payment1 = option.payment1Value().doubleValue() / quantity;
        double payment2 = option.payment2Value().doubleValue() / quantity;
        double payment3 = option.payment3Value().doubleValue() / quantity;
        if (buySell == InstrumentOfValuePartyRoleTypeEnum.SELLER) {
            quantity = -quantity;
        }

        tech.tongyu.bct.quant.library.priceable.common.flag.OptionTypeEnum optionType =
                option.optionType == OptionTypeEnum.CALL ? CALL : PUT;

        LocalDate expirationDate = option.absoluteExpirationDate();
        LocalTime expirationTime = option.absoluteExpirationTime();
        // get underlyer depending on types
        Priceable priceable;
        String underlyerInstrumentId = option.underlyer.instrumentId();
        if (option.underlyer instanceof ListedEquityInstrument) {
            EquityStock underlyer = new EquityStock(underlyerInstrumentId);
            priceable = new EquityTripleDigital<>(underlyer, LocalDateTime.of(expirationDate, expirationTime), optionType,
                    strike1, payment1, strike2, payment2, strike3, payment3);
        } else if (option.underlyer instanceof EquityIndexInstrument) {
            EquityIndex underlyer = new EquityIndex(underlyerInstrumentId);
            priceable = new EquityTripleDigital<>(underlyer, LocalDateTime.of(expirationDate, expirationTime), optionType,
                    strike1, payment1, strike2, payment2, strike3, payment3);
        } else if (option.underlyer() instanceof EquityIndexFutureInstrument) {
            EquityIndexFutures underlyer = new EquityIndexFutures(underlyerInstrumentId);
            priceable = new EquityTripleDigital<>(underlyer, LocalDateTime.of(expirationDate, expirationTime), optionType,
                    strike1, payment1, strike2, payment2, strike3, payment3);
        } else if (option.underlyer instanceof CommodityFutureInstrument) {
            CommodityFutures futures = new CommodityFutures(underlyerInstrumentId);
            priceable = new CommodityTripleDigital<>(futures, LocalDateTime.of(expirationDate, expirationTime), optionType,
                    strike1, payment1, strike2, payment2, strike3, payment3);
        } else if (option.underlyer instanceof CommodityInstrument) {
            CommoditySpot underlyer = new CommoditySpot(underlyerInstrumentId);
            priceable = new CommodityTripleDigital<>(underlyer, LocalDateTime.of(expirationDate, expirationTime), optionType,
                    strike1, payment1, strike2, payment2, strike3, payment3);
        } else {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                    String.format("定价：期权标的类型不支持: %s", option.underlyer().getClass().getSimpleName()));
        }
        return new Position(positionId, quantity, priceable);
    }

    private static Position from(String positionId, InstrumentOfValuePartyRoleTypeEnum buySell,
                                 AnnualizedVanillaOption option) {
        double notional = option.notionalWithParticipation().doubleValue();
        double initialSpot = option.initialSpot().doubleValue();
        double quantity = notional / initialSpot;
        if (buySell == InstrumentOfValuePartyRoleTypeEnum.SELLER) {
            quantity = -quantity;
        }
        double strike = option.strikeValue().doubleValue();
        LocalDate expirationDate = option.absoluteExpirationDate();
        LocalTime expirationTime = option.absoluteExpirationTime();
        tech.tongyu.bct.quant.library.priceable.common.flag.OptionTypeEnum optionType =
                option.optionType == OptionTypeEnum.CALL ? CALL : PUT;
        // get underlyer depending on types
        Priceable priceable;
        String underlyerInstrumentId = option.underlyer.instrumentId();
        if (option.exerciseType == ExerciseTypeEnum.EUROPEAN) {
            if (option.underlyer instanceof ListedEquityInstrument) {
                EquityStock underlyer = new EquityStock(underlyerInstrumentId);
                priceable = new EquityVanillaEuropean<>(underlyer, strike,
                        LocalDateTime.of(expirationDate, expirationTime), optionType);
            } else if (option.underlyer instanceof EquityIndexInstrument) {
                EquityIndex underlyer = new EquityIndex(underlyerInstrumentId);
                priceable = new EquityVanillaEuropean<>(underlyer, strike,
                        LocalDateTime.of(expirationDate, expirationTime), optionType);
            } else if (option.underlyer() instanceof EquityIndexFutureInstrument) {
                EquityIndexFutures underlyer = new EquityIndexFutures(underlyerInstrumentId);
                priceable = new EquityVanillaEuropean<>(underlyer, strike,
                        LocalDateTime.of(expirationDate, expirationTime), optionType);
            } else if (option.underlyer instanceof CommodityFutureInstrument) {
                CommodityFutures futures = new CommodityFutures(underlyerInstrumentId);
                priceable = new CommodityVanillaEuropean<>(futures, strike,
                        LocalDateTime.of(expirationDate, expirationTime), optionType);
            } else if (option.underlyer instanceof CommodityInstrument) {
                CommoditySpot underlyer = new CommoditySpot(underlyerInstrumentId);
                priceable = new CommodityVanillaEuropean<>(underlyer, strike,
                        LocalDateTime.of(expirationDate, expirationTime), optionType);
            } else {
                throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                        String.format("定价：期权标的类型不支持: %s", option.underlyer().getClass().getSimpleName()));
            }
        } else if (option.exerciseType == ExerciseTypeEnum.AMERICAN) {
            if (option.underlyer instanceof ListedEquityInstrument) {
                EquityStock underlyer = new EquityStock(underlyerInstrumentId);
                priceable = new EquityVanillaAmerican<>(underlyer, strike,
                        LocalDateTime.of(expirationDate, expirationTime), optionType);
            } else if (option.underlyer instanceof EquityIndexInstrument) {
                EquityIndex underlyer = new EquityIndex(underlyerInstrumentId);
                priceable = new EquityVanillaAmerican<>(underlyer, strike,
                        LocalDateTime.of(expirationDate, expirationTime), optionType);
            } else if (option.underlyer() instanceof EquityIndexFutureInstrument) {
                EquityIndexFutures underlyer = new EquityIndexFutures(underlyerInstrumentId);
                priceable = new EquityVanillaAmerican<>(underlyer, strike,
                        LocalDateTime.of(expirationDate, expirationTime), optionType);
            } else if (option.underlyer instanceof CommodityFutureInstrument) {
                CommodityFutures futures = new CommodityFutures(underlyerInstrumentId);
                priceable = new CommodityVanillaAmerican<>(futures, strike,
                        LocalDateTime.of(expirationDate, expirationTime), optionType);
            } else if (option.underlyer instanceof CommodityInstrument) {
                CommoditySpot underlyer = new CommoditySpot(underlyerInstrumentId);
                priceable = new CommodityVanillaAmerican<>(underlyer, strike,
                        LocalDateTime.of(expirationDate, expirationTime), optionType);
            } else {
                throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                        String.format("定价：期权标的类型不支持: %s", option.underlyer().getClass().getSimpleName()));
            }
        } else {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                    String.format("定价：期权行权类型不支持: %s", option.exerciseType.toString()));
        }
        return new Position(positionId, quantity, priceable);
    }

    private static Position from(String positionId, InstrumentOfValuePartyRoleTypeEnum buySell,
                                 AnnualizedDigitalOption option) {

        double initialSpot = option.initialSpot.doubleValue();
        double notional = option.notionalWithParticipation().doubleValue();
        double quantity = notional / initialSpot;
        double payment = option.rebateValue().doubleValue() / quantity;
        if (buySell == InstrumentOfValuePartyRoleTypeEnum.SELLER) {
            quantity = -quantity;
        }
        double strike = option.strikeValue().doubleValue();
        LocalDate expirationDate = option.absoluteExpirationDate();
        LocalTime expirationTime = option.absoluteExpirationTime();
        tech.tongyu.bct.quant.library.priceable.common.flag.OptionTypeEnum optionType =
                option.optionType == OptionTypeEnum.CALL ? CALL : PUT;
        // get underlyer depending on types
        Priceable priceable;
        String underlyerInstrumentId = option.underlyer.instrumentId();
        if (Objects.isNull(option.observationType()) || option.observationType() == BarrierObservationTypeEnum.TERMINAL) {
            if (option.underlyer() instanceof EquityIndexInstrument) {
                EquityIndex underlyer = new EquityIndex(underlyerInstrumentId);
                priceable = new EquityDigitalCash<>(underlyer, strike,
                        LocalDateTime.of(expirationDate, expirationTime), optionType, payment);
            } else if (option.underlyer instanceof ListedEquityInstrument) {
                EquityStock underlyer = new EquityStock(underlyerInstrumentId);
                priceable = new EquityDigitalCash<>(underlyer, strike,
                        LocalDateTime.of(expirationDate, expirationTime), optionType, payment);
            } else if (option.underlyer() instanceof EquityIndexFutureInstrument) {
                EquityIndexFutures underlyer = new EquityIndexFutures(underlyerInstrumentId);
                priceable = new EquityDigitalCash<>(underlyer, strike,
                        LocalDateTime.of(expirationDate, expirationTime), optionType, payment);
            } else if (option.underlyer instanceof CommodityInstrument) {
                CommoditySpot underlyer = new CommoditySpot(underlyerInstrumentId);
                priceable = new CommodityDigitalCash<>(underlyer, strike,
                        LocalDateTime.of(expirationDate, expirationTime), optionType, payment);
            } else if (option.underlyer instanceof CommodityFutureInstrument) {
                CommodityFutures futures = new CommodityFutures(underlyerInstrumentId);
                priceable = new CommodityDigitalCash<>(futures, strike,
                        LocalDateTime.of(expirationDate, expirationTime), optionType, payment);
            } else {
                throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                        String.format("定价：未支持的标的物类型 %s", option.underlyer.getClass().getSimpleName()));
            }
        } else if (option.observationType() == BarrierObservationTypeEnum.CONTINUOUS
                || option.observationType() == BarrierObservationTypeEnum.DAILY) {
            BarrierDirectionEnum barrierDirection =
                    optionType == CALL ? BarrierDirectionEnum.UP : BarrierDirectionEnum.DOWN;
            tech.tongyu.bct.quant.library.priceable.common.flag.RebateTypeEnum rebateType;
            if (option.rebateType() == RebateTypeEnum.PAY_AT_EXPIRY) {
                rebateType = tech.tongyu.bct.quant.library.priceable.common.flag.RebateTypeEnum.PAY_AT_EXPIRY;
            } else if (option.rebateType() == RebateTypeEnum.PAY_WHEN_HIT) {
                rebateType = tech.tongyu.bct.quant.library.priceable.common.flag.RebateTypeEnum.PAY_WHEN_HIT;
            } else {
                rebateType = tech.tongyu.bct.quant.library.priceable.common.flag.RebateTypeEnum.PAY_NONE;
            }
            ObservationTypeEnum observationType = option.observationType() == BarrierObservationTypeEnum.DAILY
                    ? ObservationTypeEnum.DAILY : ObservationTypeEnum.CONTINUOUS;
            double daysInYear = option.daysInYear() == null ? 365. : option.daysInYear().doubleValue();
            if (option.underlyer() instanceof EquityIndexInstrument) {
                EquityIndex underlyer = new EquityIndex(underlyerInstrumentId);
                priceable = new EquityOneTouch<>(underlyer, LocalDateTime.of(expirationDate, expirationTime),
                        strike, barrierDirection, payment, rebateType, observationType, daysInYear);
            } else if (option.underlyer instanceof ListedEquityInstrument) {
                EquityStock underlyer = new EquityStock(underlyerInstrumentId);
                priceable = new EquityOneTouch<>(underlyer, LocalDateTime.of(expirationDate, expirationTime),
                        strike, barrierDirection, payment, rebateType, observationType, daysInYear);
            } else if (option.underlyer() instanceof EquityIndexFutureInstrument) {
                EquityIndexFutures underlyer = new EquityIndexFutures(underlyerInstrumentId);
                priceable = new EquityOneTouch<>(underlyer, LocalDateTime.of(expirationDate, expirationTime),
                        strike, barrierDirection, payment, rebateType, observationType, daysInYear);
            } else if (option.underlyer instanceof CommodityInstrument) {
                CommoditySpot underlyer = new CommoditySpot(underlyerInstrumentId);
                priceable = new CommodityOneTouch<>(underlyer, LocalDateTime.of(expirationDate, expirationTime),
                        strike, barrierDirection, payment, rebateType, observationType, daysInYear);
            } else if (option.underlyer instanceof CommodityFutureInstrument) {
                CommodityFutures underlyer = new CommodityFutures(underlyerInstrumentId);
                priceable = new CommodityOneTouch<>(underlyer, LocalDateTime.of(expirationDate, expirationTime),
                        strike, barrierDirection, payment, rebateType, observationType, daysInYear);
            } else {
                throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                        String.format("定价：未支持的标的物类型 %s", option.underlyer.getClass().getSimpleName()));
            }
        } else {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                    String.format("定价：未支持的障碍类型 %s", option.observationType().toString()));
        }
        return new Position(positionId, quantity, priceable);
    }

    private static Position from(String positionId, InstrumentOfValuePartyRoleTypeEnum buySell,
                                 AnnualizedVerticalSpreadOption option) {
        double notional = option.notionalWithParticipation().doubleValue();
        double initSpot = option.initialSpot().doubleValue();
        double quantity = notional / initSpot;
        if (InstrumentOfValuePartyRoleTypeEnum.SELLER == buySell) {
            quantity = -quantity;
        }
        List<Double> strikes = option.strikeValues().stream().map(BigDecimal::doubleValue).collect(Collectors.toList());
        double strikeLow = Collections.min(strikes);
        double strikeHigh = Collections.max(strikes);
        LocalDateTime expiry = LocalDateTime.of(option.absoluteExpirationDate(), option.absoluteExpirationTime());
        tech.tongyu.bct.quant.library.priceable.common.flag.OptionTypeEnum optionType =
                option.optionType() == OptionTypeEnum.CALL ? CALL : PUT;
        Priceable priceable;
        if (option.exerciseType == ExerciseTypeEnum.EUROPEAN) {
            String underlyerInstrumentId = option.underlyer().instrumentId();
            if (option.underlyer() instanceof EquityIndexInstrument) {
                priceable = new EquityVerticalSpread<>(new EquityIndex(underlyerInstrumentId),
                        strikeLow, strikeHigh, expiry, optionType);
            } else if (option.underlyer() instanceof ListedEquityInstrument) {
                priceable = new EquityVerticalSpread<>(new EquityStock(underlyerInstrumentId),
                        strikeLow, strikeHigh, expiry, optionType);
            } else if (option.underlyer() instanceof EquityIndexFutureInstrument) {
                priceable = new EquityVerticalSpread<>(new EquityIndexFutures(underlyerInstrumentId),
                        strikeLow, strikeHigh, expiry, optionType);
            } else if (option.underlyer() instanceof CommodityInstrument) {
                priceable = new CommodityVerticalSpread<>(new CommoditySpot(underlyerInstrumentId),
                        strikeLow, strikeHigh, expiry, optionType);
            } else if (option.underlyer() instanceof CommodityFutureInstrument) {
                priceable = new CommodityVerticalSpread<>(new CommodityFutures(underlyerInstrumentId),
                        strikeLow, strikeHigh, expiry, optionType);
            } else {
                throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                        String.format("定价：未支持的标的物类型 %s", option.underlyer().getClass().getSimpleName()));
            }
        } else {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                    String.format("定价：期权行权类型不支持: %s", option.exerciseType().toString()));
        }
        return new Position(positionId, quantity, priceable);
    }

    private static Position from(String positionId, InstrumentOfValuePartyRoleTypeEnum buySell,
                                 AnnualizedEagleOption option) {
        double notional;
        if (!(option.notionalAmount().unit() instanceof CurrencyUnit)) {
            throw new CustomException("定价：鹰式期权名义本金单位必须为货币");
        }
        // annualized notional, without participation
        notional = option.lowNotionalWithParticipation().divide(option.lowParticipationRate()).doubleValue();
        double initSpot = option.initialSpot().doubleValue();
        double quantity = notional / initSpot;
        if (InstrumentOfValuePartyRoleTypeEnum.SELLER == buySell) {
            quantity = -quantity;
        }
        String underlyerInstrumentId = option.underlyer().instrumentId();
        LocalDateTime expiry = LocalDateTime.of(option.absoluteExpirationDate(), option.absoluteExpirationTime());
        double payoff = option.participationRate1.doubleValue() *
                (option.strike2Value().doubleValue() - option.strike1Value().doubleValue());
        double payoff2 = option.participationRate2.doubleValue() *
                (option.strike4Value().doubleValue() - option.strike3Value().doubleValue());
        if (!DoubleUtils.smallEnough((payoff - payoff2) / payoff, 2e-4)) {
            throw new CustomException("定价：鹰式期权参与率与行权价不一致");
        }
        Priceable priceable;

        if (option.underlyer() instanceof EquityIndexInstrument) {
            priceable = new EquityEagle<>(new EquityIndex(underlyerInstrumentId),
                    option.strike1Value().doubleValue(),
                    option.strike2Value().doubleValue(),
                    option.strike3Value().doubleValue(),
                    option.strike4Value().doubleValue(),
                    payoff,
                    expiry);
        } else if (option.underlyer() instanceof ListedEquityInstrument) {
            priceable = new EquityEagle<>(new EquityStock(underlyerInstrumentId),
                    option.strike1Value().doubleValue(),
                    option.strike2Value().doubleValue(),
                    option.strike3Value().doubleValue(),
                    option.strike4Value().doubleValue(),
                    payoff,
                    expiry);
        } else if (option.underlyer() instanceof EquityIndexFutureInstrument) {
            priceable = new EquityEagle<>(new EquityIndexFutures(underlyerInstrumentId),
                    option.strike1Value().doubleValue(),
                    option.strike2Value().doubleValue(),
                    option.strike3Value().doubleValue(),
                    option.strike4Value().doubleValue(),
                    payoff,
                    expiry);
        } else if (option.underlyer() instanceof CommodityInstrument) {
            priceable = new CommodityEagle<>(new CommoditySpot(underlyerInstrumentId),
                    option.strike1Value().doubleValue(),
                    option.strike2Value().doubleValue(),
                    option.strike3Value().doubleValue(),
                    option.strike4Value().doubleValue(),
                    payoff,
                    expiry);
        } else if (option.underlyer() instanceof CommodityFutureInstrument) {
            priceable = new CommodityEagle<>(new CommodityFutures(underlyerInstrumentId),
                    option.strike1Value().doubleValue(),
                    option.strike2Value().doubleValue(),
                    option.strike3Value().doubleValue(),
                    option.strike4Value().doubleValue(),
                    payoff,
                    expiry);
        } else {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                    String.format("定价：未支持的标的物类型 %s", option.underlyer().getClass().getSimpleName()));
        }

        return new Position(positionId, quantity, priceable);
    }

    private static Position from(String positionId, InstrumentOfValuePartyRoleTypeEnum buySell,
                                 AnnualizedKnockOutOption option) {
        double notional = option.notionalWithParticipation().doubleValue();
        double initSpot = option.initialSpot().doubleValue();
        double quantity = notional / initSpot;
        double strike = option.strikeValue().doubleValue();
        tech.tongyu.bct.quant.library.priceable.common.flag.OptionTypeEnum optionType =
                option.optionType() == OptionTypeEnum.CALL ? CALL : PUT;
        LocalDateTime expiry = LocalDateTime.of(option.absoluteExpirationDate(), option.absoluteExpirationTime());
        BarrierDirectionEnum barrierDirection = option.knockDirection() == KnockDirectionEnum.UP ?
                BarrierDirectionEnum.UP : BarrierDirectionEnum.DOWN;
        double rebate = option.rebateValue().doubleValue() / quantity;
        if (InstrumentOfValuePartyRoleTypeEnum.SELLER == buySell) {
            quantity = -quantity;
        }
        String underlyerInstrumentId = option.underlyer().instrumentId();
        Priceable priceable;
        if (option.observationType() == BarrierObservationTypeEnum.TERMINAL) {
            double barrier = option.barrierValue().doubleValue();
            if (option.underlyer() instanceof ListedEquityInstrument) {
                priceable = new EquityKnockOutTerminal<>(new EquityStock(underlyerInstrumentId),
                        expiry, strike, optionType, barrier, barrierDirection, rebate);
            } else if (option.underlyer() instanceof EquityIndexInstrument) {
                priceable = new EquityKnockOutTerminal<>(new EquityIndex(underlyerInstrumentId),
                        expiry, strike, optionType, barrier, barrierDirection, rebate);
            } else if (option.underlyer() instanceof EquityIndexFutureInstrument) {
                priceable = new EquityKnockOutTerminal<>(new EquityIndexFutures(underlyerInstrumentId),
                        expiry, strike, optionType, barrier, barrierDirection, rebate);
            } else if (option.underlyer() instanceof CommodityInstrument) {
                priceable = new CommodityKnockOutTerminal<>(new CommoditySpot(underlyerInstrumentId),
                        expiry, strike, optionType, barrier, barrierDirection, rebate);
            } else if (option.underlyer() instanceof CommodityFutureInstrument) {
                priceable = new CommodityKnockOutTerminal<>(new CommodityFutures(underlyerInstrumentId),
                        expiry, strike, optionType, barrier, barrierDirection, rebate);
            } else {
                throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                        String.format("定价：未支持的标的物类型 %s", option.underlyer().getClass().getSimpleName()));
            }
        } else {
            double barrier;
            if (Objects.isNull(option.barrierShiftedValue())) {
                barrier = option.barrierValue().doubleValue();
            } else {
                barrier = option.barrierShiftedValue().doubleValue();
            }
            tech.tongyu.bct.quant.library.priceable.common.flag.RebateTypeEnum rebateType;
            if (option.rebateType() == RebateTypeEnum.PAY_WHEN_HIT) {
                rebateType = tech.tongyu.bct.quant.library.priceable.common.flag.RebateTypeEnum.PAY_WHEN_HIT;
            } else if (option.rebateType() == RebateTypeEnum.PAY_AT_EXPIRY) {
                rebateType = tech.tongyu.bct.quant.library.priceable.common.flag.RebateTypeEnum.PAY_AT_EXPIRY;
            } else {
                rebateType = tech.tongyu.bct.quant.library.priceable.common.flag.RebateTypeEnum.PAY_NONE;
            }
            ObservationTypeEnum observationType = option.observationType() == BarrierObservationTypeEnum.DAILY
                    ? ObservationTypeEnum.DAILY : ObservationTypeEnum.CONTINUOUS;
            double daysInYear = option.daysInYear() == null ? 365. : option.daysInYear().doubleValue();
            if (option.underlyer() instanceof ListedEquityInstrument) {
                priceable = new EquityKnockOutContinuous<>(new EquityStock(underlyerInstrumentId),
                        expiry, strike, optionType, barrier, barrierDirection, rebate, rebateType,
                        observationType, daysInYear);
            } else if (option.underlyer() instanceof EquityIndexInstrument) {
                priceable = new EquityKnockOutContinuous<>(new EquityIndex(underlyerInstrumentId),
                        expiry, strike, optionType, barrier, barrierDirection, rebate, rebateType,
                        observationType, daysInYear);
            } else if (option.underlyer() instanceof EquityIndexFutureInstrument) {
                priceable = new EquityKnockOutContinuous<>(new EquityIndexFutures(underlyerInstrumentId),
                        expiry, strike, optionType, barrier, barrierDirection, rebate, rebateType,
                        observationType, daysInYear);
            } else if (option.underlyer() instanceof CommodityInstrument) {
                priceable = new CommodityKnoutOutContinuous<>(new CommoditySpot(underlyerInstrumentId),
                        expiry, strike, optionType, barrier, barrierDirection, rebate, rebateType,
                        observationType, daysInYear);
            } else if (option.underlyer() instanceof CommodityFutureInstrument) {
                priceable = new CommodityKnoutOutContinuous<>(new CommodityFutures(underlyerInstrumentId),
                        expiry, strike, optionType, barrier, barrierDirection, rebate, rebateType,
                        observationType, daysInYear);
            } else {
                throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                        String.format("定价：未支持的标的物类型 %s", option.underlyer().getClass().getSimpleName()));
            }
        }
        return new Position(positionId, quantity, priceable);
    }

    private static Position from(String positionId, InstrumentOfValuePartyRoleTypeEnum buySell,
                                 AnnualizedDoubleSharkFinOption option) {
        double notional = option.lowNotionalWithParticipation().divide(option.lowParticipationRate()).doubleValue();
        double initSpot = option.initialSpot().doubleValue();
        double quantity = notional / initSpot;
        LocalDateTime expiry = LocalDateTime.of(option.absoluteExpirationDate(), option.absoluteExpirationTime());
        double lowStrike = option.lowStrikeValue().doubleValue();
        double lowBarrier = option.lowBarrierValue().doubleValue();
        double lowParticipationRate = option.lowParticipationRate().doubleValue();
        double highStrike = option.highStrikeValue().doubleValue();
        double highBarrier = option.highBarrierValue().doubleValue();
        double highParticipationRate = option.highParticipationRate().doubleValue();
        double lowRabate = option.lowRebateValue().divide(option.lowParticipationRate()).doubleValue() / quantity;
        double highRebate = option.highRebateValue().divide(option.highParticipationRate()).doubleValue() / quantity;
        if (buySell == InstrumentOfValuePartyRoleTypeEnum.SELLER) {
            quantity = -quantity;
        }
        String underlyerInstrumentId = option.underlyer().instrumentId();
        Priceable priceable;
        if (option.observationType() == BarrierObservationTypeEnum.CONTINUOUS) {
            tech.tongyu.bct.quant.library.priceable.common.flag.RebateTypeEnum rebateType =
                    option.rebateType() == RebateTypeEnum.PAY_WHEN_HIT ?
                            tech.tongyu.bct.quant.library.priceable.common.flag.RebateTypeEnum.PAY_WHEN_HIT :
                            (option.rebateType() == RebateTypeEnum.PAY_AT_EXPIRY ?
                                    tech.tongyu.bct.quant.library.priceable.common.flag.RebateTypeEnum.PAY_AT_EXPIRY :
                                    tech.tongyu.bct.quant.library.priceable.common.flag.RebateTypeEnum.PAY_NONE);

            if (option.underlyer() instanceof ListedEquityInstrument) {
                priceable = new EquityDoubleSharkFinConinuous<>(new EquityStock(underlyerInstrumentId),
                        lowStrike, highStrike, expiry, lowBarrier, highBarrier, BarrierTypeEnum.OUT,
                        lowRabate, highRebate, rebateType, lowParticipationRate, highParticipationRate);
            } else if (option.underlyer() instanceof EquityIndexInstrument) {
                priceable = new EquityDoubleSharkFinConinuous<>(new EquityIndex(underlyerInstrumentId),
                        lowStrike, highStrike, expiry, lowBarrier, highBarrier, BarrierTypeEnum.OUT,
                        lowRabate, highRebate, rebateType, lowParticipationRate, highParticipationRate);
            } else if (option.underlyer() instanceof EquityIndexFutureInstrument) {
                priceable = new EquityDoubleSharkFinConinuous<>(new EquityIndexFutures(underlyerInstrumentId),
                        lowStrike, highStrike, expiry, lowBarrier, highBarrier, BarrierTypeEnum.OUT,
                        lowRabate, highRebate, rebateType, lowParticipationRate, highParticipationRate);
            } else if (option.underlyer() instanceof CommodityInstrument) {
                priceable = new CommodityDoubleSharkFinContinuous<>(new CommoditySpot(underlyerInstrumentId),
                        lowStrike, highStrike, expiry, lowBarrier, highBarrier, BarrierTypeEnum.OUT,
                        lowRabate, highRebate, rebateType, lowParticipationRate, highParticipationRate);
            } else if (option.underlyer() instanceof CommodityFutureInstrument) {
                priceable = new CommodityDoubleSharkFinContinuous<>(new CommodityFutures(underlyerInstrumentId),
                        lowStrike, highStrike, expiry, lowBarrier, highBarrier, BarrierTypeEnum.OUT,
                        lowRabate, highRebate, rebateType, lowParticipationRate, highParticipationRate);
            } else {
                throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                        String.format("定价：未支持的标的物类型 %s", option.underlyer().getClass().getSimpleName()));
            }
        } else if (option.observationType() == BarrierObservationTypeEnum.TERMINAL) {
            if (option.underlyer() instanceof ListedEquityInstrument) {
                priceable = new EquityDoubleSharkFinTerminal<>(new EquityStock(underlyerInstrumentId),
                        lowStrike, highStrike, expiry, lowBarrier, highBarrier, BarrierTypeEnum.OUT,
                        lowRabate, highRebate, lowParticipationRate, highParticipationRate);
            } else if (option.underlyer() instanceof EquityIndexInstrument) {
                priceable = new EquityDoubleSharkFinTerminal<>(new EquityIndex(underlyerInstrumentId),
                        lowStrike, highStrike, expiry, lowBarrier, highBarrier, BarrierTypeEnum.OUT,
                        lowRabate, highRebate, lowParticipationRate, highParticipationRate);
            } else if (option.underlyer() instanceof EquityIndexFutureInstrument) {
                priceable = new EquityDoubleSharkFinTerminal<>(new EquityIndexFutures(underlyerInstrumentId),
                        lowStrike, highStrike, expiry, lowBarrier, highBarrier, BarrierTypeEnum.OUT,
                        lowRabate, highRebate, lowParticipationRate, highParticipationRate);
            } else if (option.underlyer() instanceof CommodityInstrument) {
                priceable = new CommodityDoubleSharkFinTerminal<>(new CommoditySpot(underlyerInstrumentId),
                        lowStrike, highStrike, expiry, lowBarrier, highBarrier, BarrierTypeEnum.OUT,
                        lowRabate, highRebate, lowParticipationRate, highParticipationRate);
            } else if (option.underlyer() instanceof CommodityFutureInstrument) {
                priceable = new CommodityDoubleSharkFinTerminal<>(new CommodityFutures(underlyerInstrumentId),
                        lowStrike, highStrike, expiry, lowBarrier, highBarrier, BarrierTypeEnum.OUT,
                        lowRabate, highRebate, lowParticipationRate, highParticipationRate);
            } else {
                throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                        String.format("定价：未支持的标的物类型 %s", option.underlyer().getClass().getSimpleName()));
            }
        } else {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                    String.format("定价：未支持的障碍类型 %s", option.observationType().description()));
        }
        return new Position(positionId, quantity, priceable);
    }

    private static Position from(String positionId, InstrumentOfValuePartyRoleTypeEnum buySell,
                                 AnnualizedDoubleTouchOption option) {
        LocalDateTime expiry = LocalDateTime.of(option.absoluteExpirationDate(), option.absoluteExpirationTime());
        double notional = option.notionalWithParticipation().doubleValue();
        double initSpot = option.initialSpot().doubleValue();
        double quantity = notional / initSpot;
        double lowBarrier = option.lowBarrierValue().doubleValue();
        double highBarrier = option.highBarrierValue().doubleValue();
        double rebate = option.rebateValue().doubleValue() / quantity;
        if (buySell == InstrumentOfValuePartyRoleTypeEnum.SELLER) {
            quantity = -quantity;
        }
        double touchRebate = 0; // same for both barrier
        double noTouchRebate = 0;
        if (option.touched()) {
            touchRebate = rebate;
        } else {
            noTouchRebate = rebate;
        }
        tech.tongyu.bct.quant.library.priceable.common.flag.RebateTypeEnum rebateType =
                option.rebateType() == RebateTypeEnum.PAY_AT_EXPIRY
                        ? tech.tongyu.bct.quant.library.priceable.common.flag.RebateTypeEnum.PAY_AT_EXPIRY
                        : (option.rebateType() == RebateTypeEnum.PAY_WHEN_HIT
                        ? tech.tongyu.bct.quant.library.priceable.common.flag.RebateTypeEnum.PAY_WHEN_HIT
                        : tech.tongyu.bct.quant.library.priceable.common.flag.RebateTypeEnum.PAY_NONE);
        String underlyerInstrumentId = option.underlyer().instrumentId();
        Priceable priceable;
        if (option.underlyer() instanceof ListedEquityInstrument) {
            priceable = new EquityDoubleTouch<>(new EquityStock(underlyerInstrumentId),
                    expiry, rebateType, lowBarrier, highBarrier, touchRebate, touchRebate, noTouchRebate);
        } else if (option.underlyer() instanceof EquityIndexInstrument) {
            priceable = new EquityDoubleTouch<>(new EquityIndex(underlyerInstrumentId),
                    expiry, rebateType, lowBarrier, highBarrier, touchRebate, touchRebate, noTouchRebate);
        } else if (option.underlyer() instanceof EquityIndexFutureInstrument) {
            priceable = new EquityDoubleTouch<>(new EquityIndexFutures(underlyerInstrumentId),
                    expiry, rebateType, lowBarrier, highBarrier, touchRebate, touchRebate, noTouchRebate);
        } else if (option.underlyer() instanceof CommodityInstrument) {
            priceable = new CommodityDoubleTouch<>(new CommoditySpot(underlyerInstrumentId),
                    expiry, rebateType, lowBarrier, highBarrier, touchRebate, touchRebate, noTouchRebate);
        } else if (option.underlyer() instanceof CommodityFutureInstrument) {
            priceable = new CommodityDoubleTouch<>(new CommodityFutures(underlyerInstrumentId),
                    expiry, rebateType, lowBarrier, highBarrier, touchRebate, touchRebate, noTouchRebate);
        } else {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                    String.format("定价：未支持的标的物类型 %s", option.underlyer().getClass().getSimpleName()));
        }
        return new Position(positionId, quantity, priceable);
    }

    private static Position from(String positionId, InstrumentOfValuePartyRoleTypeEnum buySell,
                                 AnnualizedAutoCallOption<InstrumentOfValue> option) {
        LocalDateTime expiry = LocalDateTime.of(option.absoluteExpirationDate(), option.absoluteExpirationTime());
        List<LocalDate> observationDates = option.observationDates();
        BarrierDirectionEnum barrierDirection =
                option.knockDirection() == KnockDirectionEnum.UP ? BarrierDirectionEnum.UP : BarrierDirectionEnum.DOWN;
        List<Double> barriers = option.barrierValues().stream()
                .map(BigDecimal::doubleValue).collect(Collectors.toList());
        double notional = option.notionalWithParticipation().doubleValue();
        double initSpot = option.initialSpot().doubleValue();
        double quantityAbs = notional / initSpot;
        double quantity = buySell == InstrumentOfValuePartyRoleTypeEnum.BUYER ? quantityAbs : -quantityAbs;
        List<Double> payments = option.couponPaymentAmounts().stream()
                .map(c -> c.doubleValue() / quantityAbs).collect(Collectors.toList());
        Map<LocalDate, BigDecimal> observedFixings = option.observedFixings();

        List<LocalDate> paymentDates = observationDates;
        Map<LocalDate, LocalDate> fixingPaymentDates = option.fixingPaymentDates;
        if (CollectionUtils.isNotEmpty(fixingPaymentDates)){
            paymentDates = fixingPaymentDates.entrySet().stream()
                    .sorted(Comparator.comparing(Map.Entry::getKey))
                    .map(entry-> entry.getValue())
                    .collect(Collectors.toList());
        }
        boolean knockedOut = false;
        double knockedOutPayment = 0;
        LocalDate knockedOutPaymentDate = null;
        for (int i = 0; i < observationDates.size(); i++) {
            LocalDate d = observationDates.get(i);
            if (Objects.isNull(observedFixings.get(d))) {
                break;
            }
            if (barrierDirection == BarrierDirectionEnum.UP
                    ? observedFixings.get(d).doubleValue() >= barriers.get(i)
                    : observedFixings.get(d).doubleValue() <= barriers.get(i)) {
                knockedOut = true;
                knockedOutPayment = payments.get(i);
                knockedOutPaymentDate = d;
            }
        }
        boolean finalPayFixed = option.autoCallPaymentType() == AutoCallPaymentTypeEnum.FIXED;
        LocalDate finalPaymentDate = option.settlementDate();
        double finalPayment = 0;
        double finalOptionStrike = 0;
        if (finalPayFixed) {
            finalPayment = option.fixedPaymentAmount().doubleValue() / quantityAbs;
        } else {
            finalOptionStrike = option.autoCallStrikeAmount().doubleValue();
        }
        tech.tongyu.bct.quant.library.priceable.common.flag.OptionTypeEnum finalOptionType =
                option.autoCallPaymentType() == AutoCallPaymentTypeEnum.CALL ? CALL
                        : option.autoCallPaymentType() == AutoCallPaymentTypeEnum.PUT ? PUT
                        : null;
        String underlyerInstrumentId = option.underlyer().instrumentId();
        Priceable priceable;
        if (option.underlyer() instanceof ListedEquityInstrument) {
            priceable = new EquityAutoCall<>(new EquityStock(underlyerInstrumentId),
                    expiry, observationDates, barrierDirection, barriers, paymentDates,
                    payments, finalPayFixed, finalPaymentDate, finalPayment, finalOptionType, finalOptionStrike,
                    knockedOut, knockedOutPayment, knockedOutPaymentDate);
        } else if (option.underlyer() instanceof EquityIndexInstrument) {
            priceable = new EquityAutoCall<>(new EquityIndex(underlyerInstrumentId),
                    expiry, observationDates, barrierDirection, barriers, paymentDates,
                    payments, finalPayFixed, finalPaymentDate, finalPayment, finalOptionType, finalOptionStrike,
                    knockedOut, knockedOutPayment, knockedOutPaymentDate);
        } else if (option.underlyer() instanceof EquityIndexFutureInstrument) {
            priceable = new EquityAutoCall<>(new EquityIndexFutures(underlyerInstrumentId),
                    expiry, observationDates, barrierDirection, barriers, paymentDates,
                    payments, finalPayFixed, finalPaymentDate, finalPayment, finalOptionType, finalOptionStrike,
                    knockedOut, knockedOutPayment, knockedOutPaymentDate);
        } else if (option.underlyer() instanceof CommodityInstrument) {
            priceable = new CommodityAutoCall<>(new CommoditySpot(underlyerInstrumentId),
                    expiry, observationDates, barrierDirection, barriers, paymentDates,
                    payments, finalPayFixed, finalPaymentDate, finalPayment, finalOptionType, finalOptionStrike,
                    knockedOut, knockedOutPayment, knockedOutPaymentDate);
        } else if (option.underlyer() instanceof CommodityFutureInstrument) {
            priceable = new CommodityAutoCall<>(new CommodityFutures(underlyerInstrumentId),
                    expiry, observationDates, barrierDirection, barriers, paymentDates,
                    payments, finalPayFixed, finalPaymentDate, finalPayment, finalOptionType, finalOptionStrike,
                    knockedOut, knockedOutPayment, knockedOutPaymentDate);
        } else {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                    String.format("定价：未支持的标的物类型 %s", option.underlyer().getClass().getSimpleName()));
        }
        return new Position(positionId, quantity, priceable);
    }

    private static Position from(String positionId, InstrumentOfValuePartyRoleTypeEnum buySell,
                                 AnnualizedAutoCallPhoenixOption<InstrumentOfValue> option) {
        LocalDateTime expiry = LocalDateTime.of(option.absoluteExpirationDate(), option.absoluteExpirationTime());
        List<LocalDate> observationDates = new ArrayList<>(option.fixingObservations().keySet());
        Collections.sort(observationDates);
        Map<LocalDate, BigDecimal> fixingObservations = option.fixingObservations();
        List<Double> fixings = new ArrayList<>(observationDates.size());
        for (LocalDate d : observationDates) {
            if (Objects.isNull(fixingObservations.get(d))) {
                break;
            }
            fixings.add(fixingObservations.get(d).doubleValue());
        }
        BarrierDirectionEnum knockOutDirection = option.knockDirection() == KnockDirectionEnum.UP
                ? BarrierDirectionEnum.UP : BarrierDirectionEnum.DOWN;
        double knockOutBarrier = option.barrierValue().doubleValue();
        List<Double> knockOutBarriers = observationDates.stream()
                .map(d -> knockOutBarrier).collect(Collectors.toList());
        double couponBarrier = option.couponBarrierValue().doubleValue();
        List<Double> couponBarriers = observationDates.stream().map(d -> couponBarrier).collect(Collectors.toList());
        double quantityAbs = option.notionalWithParticipation().doubleValue() / option.initialSpot().doubleValue();
        double quantity = buySell == InstrumentOfValuePartyRoleTypeEnum.SELLER ? -quantityAbs : quantityAbs;
        List<Double> coupons = option.couponPaymentValues().stream()
                .map(c -> c.doubleValue() / quantityAbs).collect(Collectors.toList());
        boolean knockedIn = option.knockedIn();
        // TODO (http://jira.tongyu.tech/browse/OTMS-2599) : 这个hack暂时保证可以定价
        List<LocalDate> knockInObservationDates;
        if (option.knockInObservationDates() != null) {
            knockInObservationDates = option.knockInObservationDates();
        } else {
            knockInObservationDates = new ArrayList<>();
            LocalDate observationDate = option.effectiveDate();
            while (observationDate.isBefore(expiry.toLocalDate())) {
                if (!observationDate.equals(option.effectiveDate())) {
                    knockInObservationDates.add(observationDate);
                }
                if (option.knockInObservationStep().equals("1D")) {
                    observationDate = observationDate.plusDays(1);
                } else if (option.knockInObservationStep().equals("1M")) {
                    observationDate = observationDate.plusMonths(1);
                } else if (option.knockInObservationStep().equals("3M")) {
                    observationDate = observationDate.plusMonths(3);
                } else {
                    throw new CustomException(String.format("不支持的敲入观察频率:%s", option.knockInObservationStep()));
                }
            }
        }
        double knockInBarrier = option.knockInBarrierValue().doubleValue();
        List<Double> knockInBarriers = knockInObservationDates.stream()
                .map(d -> knockInBarrier).collect(Collectors.toList());
        tech.tongyu.bct.quant.library.priceable.common.flag.OptionTypeEnum knockedInOptionType =
                option.knockInOptionType() == OptionTypeEnum.CALL ? CALL : PUT;
        double knockedInOptionStrike = option.knockInStrikeValue().doubleValue();
        LocalDate finalPaymentDate = option.settlementDate();
        List<LocalDate> couponPaymentDates = observationDates.stream()
                .map(d -> finalPaymentDate).collect(Collectors.toList());
        String underlyerInstrumentId = option.underlyer().instrumentId();
        Priceable priceable;
        if (option.underlyer() instanceof ListedEquityInstrument) {
            priceable = new EquityAutoCallPhoenix<>(new EquityStock(underlyerInstrumentId),
                    expiry, observationDates, fixings, knockOutDirection, knockOutBarriers, couponBarriers,
                    couponPaymentDates, coupons, knockInObservationDates, knockInBarriers, knockedIn,
                    knockedInOptionType, knockedInOptionStrike, finalPaymentDate);
        } else if (option.underlyer() instanceof EquityIndexInstrument) {
            priceable = new EquityAutoCallPhoenix<>(new EquityIndex(underlyerInstrumentId),
                    expiry, observationDates, fixings, knockOutDirection, knockOutBarriers, couponBarriers,
                    couponPaymentDates, coupons, knockInObservationDates, knockInBarriers, knockedIn,
                    knockedInOptionType, knockedInOptionStrike, finalPaymentDate);
        } else if (option.underlyer() instanceof EquityIndexFutureInstrument) {
            priceable = new EquityAutoCallPhoenix<>(new EquityIndexFutures(underlyerInstrumentId),
                    expiry, observationDates, fixings, knockOutDirection, knockOutBarriers, couponBarriers,
                    couponPaymentDates, coupons, knockInObservationDates, knockInBarriers, knockedIn,
                    knockedInOptionType, knockedInOptionStrike, finalPaymentDate);
        } else if (option.underlyer() instanceof CommodityInstrument) {
            priceable = new CommodityAutoCallPhoenix<>(new CommoditySpot(underlyerInstrumentId),
                    expiry, observationDates, fixings, knockOutDirection, knockOutBarriers, couponBarriers,
                    couponPaymentDates, coupons, knockInObservationDates, knockInBarriers, knockedIn,
                    knockedInOptionType, knockedInOptionStrike, finalPaymentDate);
        } else if (option.underlyer() instanceof CommodityFutureInstrument) {
            priceable = new CommodityAutoCallPhoenix<>(new CommodityFutures(underlyerInstrumentId),
                    expiry, observationDates, fixings, knockOutDirection, knockOutBarriers, couponBarriers,
                    couponPaymentDates, coupons, knockInObservationDates, knockInBarriers, knockedIn,
                    knockedInOptionType, knockedInOptionStrike, finalPaymentDate);
        } else {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                    String.format("定价：未支持的标的物类型 %s", option.underlyer().getClass().getSimpleName()));
        }
        return new Position(positionId, quantity, priceable);
    }

    private static Position from(String positionId, InstrumentOfValuePartyRoleTypeEnum buySell,
                                 AnnualizedAsianOption<InstrumentOfValue> option) {
        LocalDateTime expiry = LocalDateTime.of(option.absoluteExpirationDate(), option.absoluteExpirationTime());
        tech.tongyu.bct.quant.library.priceable.common.flag.OptionTypeEnum optionType =
                option.optionType() == OptionTypeEnum.CALL ? CALL : PUT;
        double strike = option.strikeValue().doubleValue();
        List<LocalDate> observationDates = option.observationDates();
        LocalTime expiryTime = option.absoluteExpirationTime();
        List<LocalDateTime> observationDateTimes = observationDates.stream()
                .map(d -> LocalDateTime.of(d, expiryTime)).collect(Collectors.toList());
        Map<LocalDate, BigDecimal> observationWeights = option.fixingWeights();
        List<Double> weights = observationDates.stream()
                .map(d -> observationWeights.get(d).doubleValue()).collect(Collectors.toList());
        Map<LocalDate, BigDecimal> observedFixings = option.observedFixings();
        List<Double> fixings = new ArrayList<>(0);
        for (int i = 0; i < observedFixings.size(); i++) {
            fixings.add(observedFixings.get(observationDates.get(i)).doubleValue());
        }
        double daysInYear = 365.;
        double notional = option.notionalWithParticipation().doubleValue();
        double initSpot = option.initialSpot().doubleValue();
        double quantity = notional / initSpot;
        if (buySell == InstrumentOfValuePartyRoleTypeEnum.SELLER) {
            quantity = -quantity;
        }
        String underlyerInstrumentId = option.underlyer().instrumentId();
        Priceable priceable;
        if (option.underlyer() instanceof ListedEquityInstrument) {
            priceable = new EquityAsianFixedStrikeArithmetic<>(new EquityStock(underlyerInstrumentId),
                    expiry, optionType, strike, observationDateTimes, weights, fixings, daysInYear);
        } else if (option.underlyer() instanceof EquityIndexInstrument) {
            priceable = new EquityAsianFixedStrikeArithmetic<>(new EquityIndex(underlyerInstrumentId),
                    expiry, optionType, strike, observationDateTimes, weights, fixings, daysInYear);
        } else if (option.underlyer() instanceof EquityIndexFutureInstrument) {
            priceable = new EquityAsianFixedStrikeArithmetic<>(new EquityIndexFutures(underlyerInstrumentId),
                    expiry, optionType, strike, observationDateTimes, weights, fixings, daysInYear);
        } else if (option.underlyer() instanceof CommodityInstrument) {
            priceable = new CommodityAsianFixedStrikeArithmetic<>(new CommoditySpot(underlyerInstrumentId),
                    expiry, optionType, strike, observationDateTimes, weights, fixings, daysInYear);
        } else if (option.underlyer() instanceof CommodityFutureInstrument) {
            priceable = new CommodityAsianFixedStrikeArithmetic<>(new CommodityFutures(underlyerInstrumentId),
                    expiry, optionType, strike, observationDateTimes, weights, fixings, daysInYear);
        } else {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                    String.format("定价：未支持的标的物类型 %s", option.underlyer().getClass().getSimpleName()));
        }
        return new Position(positionId, quantity, priceable);
    }

    private static Position from(String positionId, InstrumentOfValuePartyRoleTypeEnum buySell,
                                 AnnualizedRangeAccrualsOption<InstrumentOfValue> option) {
        LocalDateTime expiry = LocalDateTime.of(option.absoluteExpirationDate(), option.absoluteExpirationTime());
        double lowBarrier = option.lowBarrierValue().doubleValue();
        double highBarrier = option.highBarrierValue().doubleValue();
        double notional = option.notionalWithParticipation().doubleValue();
        double initSpot = option.initialSpot().doubleValue();
        double absQuantity = notional / initSpot;
        double quantity = buySell == InstrumentOfValuePartyRoleTypeEnum.BUYER ? absQuantity : -absQuantity;
        double maxPayment = option.paymentValue().doubleValue() / absQuantity;
        List<LocalDate> dates = new ArrayList<>(option.fixingWeights().keySet());
        Collections.sort(dates);
        Map<LocalDate, BigDecimal> observationWeights = option.fixingWeights();
        LocalTime expiryTime = expiry.toLocalTime();
        List<Tuple2<LocalDateTime, Double>> observationDates = dates.stream()
                .map(d -> new Tuple2<>(LocalDateTime.of(d, expiryTime), observationWeights.get(d).doubleValue()))
                .collect(Collectors.toList());
        Map<LocalDate, BigDecimal> fixingObservations = option.fixingObservations();
        Map<LocalDateTime, Double> fixings = new TreeMap<>();
        observationDates.forEach(t -> {
            LocalDate d = t._1.toLocalDate();
            if (!Objects.isNull(fixingObservations.get(d))) {
                fixings.put(t._1, fixingObservations.get(d).doubleValue());
            }
        });
        String underlyerInstrumentId = option.underlyer().instrumentId();
        Priceable priceable;
        if (option.underlyer() instanceof ListedEquityInstrument) {
            priceable = new EquityRangeAccrual<>(new EquityStock(underlyerInstrumentId),
                    expiry, lowBarrier, highBarrier, maxPayment, observationDates, fixings);
        } else if (option.underlyer() instanceof EquityIndexInstrument) {
            priceable = new EquityRangeAccrual<>(new EquityIndex(underlyerInstrumentId),
                    expiry, lowBarrier, highBarrier, maxPayment, observationDates, fixings);
        } else if (option.underlyer() instanceof EquityIndexFutureInstrument) {
            priceable = new EquityRangeAccrual<>(new EquityIndexFutures(underlyerInstrumentId),
                    expiry, lowBarrier, highBarrier, maxPayment, observationDates, fixings);
        } else if (option.underlyer() instanceof CommodityInstrument) {
            priceable = new CommodityRangeAccrual<>(new CommoditySpot(underlyerInstrumentId),
                    expiry, lowBarrier, highBarrier, maxPayment, observationDates, fixings);
        } else if (option.underlyer() instanceof CommodityFutureInstrument) {
            priceable = new CommodityRangeAccrual<>(new CommodityFutures(underlyerInstrumentId),
                    expiry, lowBarrier, highBarrier, maxPayment, observationDates, fixings);
        } else {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                    String.format("定价：未支持的标的物类型 %s", option.underlyer().getClass().getSimpleName()));
        }
        return new Position(positionId, quantity, priceable);
    }

    private static Position from(String positionId, InstrumentOfValuePartyRoleTypeEnum buySell,
                                 AnnualizedStraddleOption<InstrumentOfValue> option) {
        LocalDateTime expiry = LocalDateTime.of(option.absoluteExpirationDate(), option.absoluteExpirationTime());
        double lowStrike = option.lowStrikeValue().doubleValue();
        double highStrike = option.highStrikeValue().doubleValue();
        double lowNotional = option.lowNotionalWithParticipation().doubleValue();
        double highToLow = option.highNotionalWithParticipation().doubleValue() / lowNotional;
        double initSpot = option.initialSpot().doubleValue();
        double quantity = lowNotional / initSpot;
        if (buySell == InstrumentOfValuePartyRoleTypeEnum.SELLER) {
            quantity = -quantity;
        }
        String underlyerInstrumentId = option.underlyer().instrumentId();
        Priceable priceable;
        if (option.underlyer() instanceof ListedEquityInstrument) {
            priceable = new EquityStraddle<>(new EquityStock(underlyerInstrumentId),
                    expiry, lowStrike, highStrike, 1.0, highToLow);
        } else if (option.underlyer() instanceof EquityIndexInstrument) {
            priceable = new EquityStraddle<>(new EquityIndex(underlyerInstrumentId),
                    expiry, lowStrike, highStrike, 1.0, highToLow);
        } else if (option.underlyer() instanceof EquityIndexFutureInstrument) {
            priceable = new EquityStraddle<>(new EquityIndexFutures(underlyerInstrumentId),
                    expiry, lowStrike, highStrike, 1.0, highToLow);
        } else if (option.underlyer() instanceof CommodityInstrument) {
            priceable = new CommodityStraddle<>(new CommoditySpot(underlyerInstrumentId),
                    expiry, lowStrike, highStrike, 1.0, highToLow);
        } else if (option.underlyer() instanceof CommodityFutureInstrument) {
            priceable = new CommodityStraddle<>(new CommodityFutures(underlyerInstrumentId),
                    expiry, lowStrike, highStrike, 1.0, highToLow);
        } else {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                    String.format("定价：未支持的标的物类型 %s", option.underlyer().getClass().getSimpleName()));
        }
        return new Position(positionId, quantity, priceable);
    }

    private static Position from(String positionId, InstrumentOfValuePartyRoleTypeEnum buySell,
                                 AnnualizedModelXYOption<InstrumentOfValue> option) {
        LocalDateTime expiry = LocalDateTime.of(option.absoluteExpirationDate(), option.absoluteExpirationTime());
        String underlyerInstrumentId = option.underlyer().instrumentId();
        Priceable priceable = new CustomProductModelXY(underlyerInstrumentId, expiry);
        return new Position(positionId, 1.0, priceable);
    }

    private static Position from(String positionId, InstrumentOfValuePartyRoleTypeEnum buySell,
                                 AnnualizedSpreadsOption<InstrumentOfValue> option) {
        LocalDateTime expiry = LocalDateTime.of(option.absoluteExpirationDate(), option.absoluteExpirationTime());
        LocalDate deliveryDate = option.settlementDate();
        double strike = option.strikePercentValue().doubleValue();
        tech.tongyu.bct.quant.library.priceable.common.flag.OptionTypeEnum optionType =
                option.optionType() == OptionTypeEnum.CALL ? CALL : PUT;
        List<BasketInstrumentConstituent> basket = ((BasketInstrument) option.underlyer()).constituents();
        if (basket.size() != 2) {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                    String.format("定价：价差期权标的物数量应当为2，实际为 %s", basket.size()));
        }
        List<ExchangeListed> underlyers = basket.stream().map(i -> {
            if (i.instrument() instanceof ListedEquityInstrument) {
                return new EquityStock(i.instrument().instrumentId());
            } else if (i.instrument() instanceof EquityIndexInstrument) {
                return new EquityIndex(i.instrument().instrumentId());
            } else if (i.instrument() instanceof EquityIndexFutureInstrument) {
                return new EquityIndexFutures(i.instrument().instrumentId());
            } else if (i.instrument() instanceof CommodityInstrument) {
                return new CommoditySpot(i.instrument().instrumentId());
            } else if (i.instrument() instanceof CommodityFutureInstrument) {
                return new CommodityFutures(i.instrument().instrumentId());
            } else {
                throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                        String.format("定价：未支持的标的物类型 %s", i.instrument().getClass().getSimpleName()));
            }
        }).collect(Collectors.toList());
        double quantity = option.notionalWithParticipation().doubleValue();
        if (buySell == InstrumentOfValuePartyRoleTypeEnum.SELLER) {
            quantity = -quantity;
        }
        Priceable priceable;
        if (option.productType() == ProductTypeEnum.SPREAD_EUROPEAN) {
            double weight0 = basket.get(0).weight().doubleValue() / basket.get(0).initialSpot().doubleValue();
            double weight1 = basket.get(1).weight().doubleValue() / basket.get(1).initialSpot().doubleValue();
            priceable = new SpreadVanillaEuropean(
                    underlyers.get(0), weight0, underlyers.get(1), weight1, expiry, strike, optionType, deliveryDate);
            return new Position(positionId, quantity, priceable);
        } else if (option.productType() == ProductTypeEnum.RATIO_SPREAD_EUROPEAN) {
            priceable = new RatioVanillaEuropean(
                    underlyers.get(0), underlyers.get(1), expiry, strike, optionType, deliveryDate);
        } else {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                    String.format("定价：未支持的期权类型 %s", option.productType().toString()));
        }
        return new Position(positionId, quantity, priceable);
    }

    private static Position from(String positionId, InstrumentOfValuePartyRoleTypeEnum buySell,
                                 Asset<InstrumentOfValue> asset) {
        InstrumentOfValue instrumentOfValue = asset.instrumentOfValue();
        return API.Match(instrumentOfValue).of(
                Case($(instanceOf(CashFlow.class)), i -> from(positionId, buySell, i)),
                Case($(instanceOf(Forward.class)), i -> from(positionId, buySell, i)),
                Case($(instanceOf(AnnualizedVanillaOption.class)), i -> from(positionId, buySell, i)),
                Case($(instanceOf(AnnualizedDigitalOption.class)), i -> from(positionId, buySell, i)),
                Case($(instanceOf(AnnualizedVerticalSpreadOption.class)), i -> from(positionId, buySell, i)),
                Case($(instanceOf(AnnualizedStraddleOption.class)), i -> from(positionId, buySell, i)),
                Case($(instanceOf(AnnualizedEagleOption.class)), i -> from(positionId, buySell, i)),
                Case($(instanceOf(AnnualizedKnockOutOption.class)), i -> from(positionId, buySell, i)),
                Case($(instanceOf(AnnualizedConcavaConvexOption.class)), i -> from(positionId, buySell, i)),
                Case($(instanceOf(AnnualizedDoubleDigitalOption.class)), i -> from(positionId, buySell, i)),
                Case($(instanceOf(AnnualizedTripleDigitalOption.class)), i -> from(positionId, buySell, i)),
                Case($(instanceOf(AnnualizedDoubleSharkFinOption.class)), i -> from(positionId, buySell, i)),
                Case($(instanceOf(AnnualizedDoubleTouchOption.class)), i -> from(positionId, buySell, i)),
                Case($(instanceOf(AnnualizedAutoCallOption.class)), i -> from(positionId, buySell, i)),
                Case($(instanceOf(AnnualizedAutoCallPhoenixOption.class)), i -> from(positionId, buySell, i)),
                Case($(instanceOf(AnnualizedAsianOption.class)), i -> from(positionId, buySell, i)),
                Case($(instanceOf(AnnualizedRangeAccrualsOption.class)), i -> from(positionId, buySell, i)),
                Case($(instanceOf(AnnualizedModelXYOption.class)), i -> from(positionId, buySell, i)),
                Case($(instanceOf(AnnualizedSpreadsOption.class)), i -> from(positionId, buySell, i)),
                Case($(), o -> {
                    throw new CustomException(ErrorCode.INPUT_NOT_VALID, String.format("暂时不支持对%s的定价", asset.instrumentOfValue().productType()));
                })
        );
    }

    public static Tuple2<List<Diagnostic>, List<Position>> from(BctTrade bctTrade) {
        return fromPositions(bctTrade.tradeId, bctTrade.positions());
    }

    public static Tuple2<List<Diagnostic>, List<Position>> fromTrades(List<BctTrade> bctTrades) {
        List<Diagnostic> failed = new ArrayList<>();
        List<Position> success = new ArrayList<>();
        List<Tuple2<List<Diagnostic>, List<Position>>> converted = bctTrades.stream()
                .map(BctTradeToQuantModel::from).collect(Collectors.toList());
        for (Tuple2<List<Diagnostic>, List<Position>> tuple2 : converted) {
            failed.addAll(tuple2._1);
            success.addAll(tuple2._2);
        }
        return Tuple.of(failed, success);
    }

    public static Tuple2<List<Diagnostic>, List<Position>> fromPositions(
            String tradeId,
            List<? extends tech.tongyu.bct.cm.trade.Position<Asset<InstrumentOfValue>>> bctTradePositions) {
        List<Diagnostic> failed = new ArrayList<>();
        List<Position> ret = new ArrayList<>();
        bctTradePositions.forEach(tp -> {
            if (!(tp instanceof BctTradePosition)) {
                failed.add(Diagnostic.of(tradeId, Diagnostic.Type.ERROR,
                        String.format("定价：交易 %s 中存在非BctTradePosition类型的position")));
            } else {
                try {
                    InstrumentOfValuePartyRoleTypeEnum buySell = ((BctTradePosition) tp).partyRole();
                    ret.add(from(((BctTradePosition) tp).getPositionId(), buySell, ((BctTradePosition) tp).asset));
                } catch (Exception e) {
                    failed.add(Diagnostic.of(((BctTradePosition) tp).positionId, Diagnostic.Type.ERROR,
                            String.format("定价: 无法解析position %s, 错误信息: %s",
                                    ((BctTradePosition) tp).positionId, e.getMessage())));
                }

            }
        });
        return Tuple.of(failed, ret);
    }
}

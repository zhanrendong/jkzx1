package tech.tongyu.bct.trade.service;

import com.fasterxml.jackson.databind.JsonNode;
import tech.tongyu.bct.cm.core.BusinessCenterEnum;
import tech.tongyu.bct.cm.core.BusinessCenterTimeTypeEnum;
import tech.tongyu.bct.cm.product.iov.*;
import tech.tongyu.bct.cm.product.iov.feature.OptionTypeEnum;
import tech.tongyu.bct.cm.reference.impl.CurrencyUnit;
import tech.tongyu.bct.cm.reference.impl.UnitEnum;
import tech.tongyu.bct.cm.trade.impl.SalesPartyRole;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.trade.dto.trade.TradePositionDTO;
import tech.tongyu.bct.trade.dto.trade.TradeDTO;
import tech.tongyu.bct.trade.dto.trade.product.AnnualizedVerticalSpreadOptionDTO;
import tech.tongyu.bct.trade.dto.trade.product.AnnualizedKnockOutOptionDTO;
import tech.tongyu.bct.trade.dto.trade.product.AnnualizedDigitalOptionDTO;
import tech.tongyu.bct.trade.dto.trade.product.AnnualizedVanillaOptionDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TestPositionFactory {
    public static class Account implements tech.tongyu.bct.cm.reference.elemental.Account {
        public String accountId;
        public String accountCode;

        public Account() {
        }

        public Account(String accountId, String accountCode) {
            this.accountId = accountId;
            this.accountCode = accountCode;
        }

        @Override
        public UUID accountUUID() {
            return null;
        }

        @Override
        public String accountCode() {
            return null;
        }

        @Override
        public String accountName() {
            return null;
        }
    }

    public static class Party implements tech.tongyu.bct.cm.reference.elemental.Party {
        public String partyId;
        public String partyName;

        public Party() {
        }

        public Party(String partyId, String partyName) {
            this.partyId = partyId;
            this.partyName = partyName;
        }

        @Override
        public UUID partyUUID() {
            return null;
        }

        @Override
        public String partyName() {
            return null;
        }

        @Override
        public String partyCode() {
            return null;
        }
    }

    public static LocalDate tradeDate = LocalDate.now();
    public static String bookName = "options";
    public static BusinessCenterEnum venue = BusinessCenterEnum.SSE;
    public static CurrencyUnit currency = CurrencyUnit.CNY;
    public static Party party = new Party("001", "MSCO");
    public static Party counterparty = new Party("002", "aaa");
    public static Account counterpartyAccount = new Account("a1", "a1");
    public static Party branch = new Party("001", "BRANCH_005");
    public static SalesPartyRole sales = new SalesPartyRole(
            branch,
            "sales",
            1.5,
            0.03,
            51.5);
    public static LocalDateTime expirationTime =
            LocalDateTime.of(2018, 11, 30, 13, 25, 49);



    public static TradeDTO createAnnualizedVanillaTradeDTO(String tradeId, String positionId, String instrumentId, Double strike, Double spot, ExerciseTypeEnum exerciseType) {
        AnnualizedVanillaOptionDTO asset = new AnnualizedVanillaOptionDTO(
                exerciseType,
                InstrumentOfValuePartyRoleTypeEnum.SELLER,
                instrumentId,
                BigDecimal.valueOf(spot),
                BigDecimal.valueOf(strike),
                UnitEnum.PERCENT,
                BusinessCenterTimeTypeEnum.CLOSE,
                expirationTime.toLocalDate(),
                BigDecimal.valueOf(100),
                true,
                BigDecimal.valueOf(365),
                BigDecimal.valueOf(99),
                OptionTypeEnum.CALL,
                BigDecimal.valueOf(100000),
                UnitEnum.CNY,
                BigDecimal.valueOf(1),
                expirationTime.toLocalDate(),
                "1",
                tradeDate,
                UnitEnum.PERCENT,
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(10),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.valueOf(100).divide(BigDecimal.valueOf(365), 10, BigDecimal.ROUND_DOWN)
        );
        JsonNode assetJson = JsonUtils.mapper.valueToTree(asset);
        TradePositionDTO position = new TradePositionDTO(
                bookName,
                positionId,
                BigDecimal.valueOf(1.0),
                assetJson,
                ProductTypeEnum.VANILLA_EUROPEAN,
                InstrumentAssetClassTypeEnum.EQUITY,
                "counterparty",
                "counterparty",
                null,
                counterpartyAccount.accountCode
        );
        return new TradeDTO(
                bookName,
                tradeId,
                counterparty.partyCode(),
                sales.salesName,
                sales.salesCommission,
                tradeDate,
                Arrays.asList(position));
    }

    public static TradeDTO createAnnualizedDigitalTradeDTO(String tradeId, String positionId, String instrumentId, Double strike, Double spot, ExerciseTypeEnum exerciseType) {
        AnnualizedDigitalOptionDTO asset = new AnnualizedDigitalOptionDTO(
                InstrumentOfValuePartyRoleTypeEnum.SELLER,
                BusinessCenterTimeTypeEnum.CLOSE,
                exerciseType,
                instrumentId,
                OptionTypeEnum.CALL,
                "1",
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(99),
                BigDecimal.valueOf(spot),
                UnitEnum.CNY,
                BigDecimal.valueOf(100000),
                UnitEnum.PERCENT,
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(30),
                true,
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(365),
                expirationTime.toLocalDate(),
                tradeDate,
                expirationTime.toLocalDate(),
                UnitEnum.PERCENT,
                BigDecimal.valueOf(strike),
                BarrierObservationTypeEnum.TERMINAL,
                RebateTypeEnum.PAY_WHEN_HIT,
                UnitEnum.PERCENT,
                BigDecimal.valueOf(98),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                UnitEnum.PERCENT,
                BigDecimal.valueOf(30).divide(BigDecimal.valueOf(365), 10, BigDecimal.ROUND_DOWN)
                );
        JsonNode assetJson = JsonUtils.mapper.valueToTree(asset);
        TradePositionDTO position = new TradePositionDTO(
                bookName,
                positionId,
                BigDecimal.valueOf(1.0),
                assetJson,
                ProductTypeEnum.DIGITAL,
                InstrumentAssetClassTypeEnum.EQUITY,
                "counterparty",
                "counterparty",
                null,
                counterpartyAccount.accountCode
        );
        return new TradeDTO(
                bookName,
                tradeId,
                counterparty.partyCode(),
                sales.salesName,
                sales.salesCommission,
                tradeDate,
                Arrays.asList(position));
    }


    public static TradeDTO createAnnualizedBarrierTradeDTO(String tradeId, String positionId, String instrumentId, Double strike, Double spot, ExerciseTypeEnum exerciseType) {
        Map<LocalDate, BigDecimal> fixingObservations = new HashMap<>();
        AnnualizedKnockOutOptionDTO asset = new AnnualizedKnockOutOptionDTO(
                InstrumentOfValuePartyRoleTypeEnum.SELLER,
                BusinessCenterTimeTypeEnum.CLOSE,
                OptionTypeEnum.CALL,
                instrumentId,
                "123",
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(99),
                BigDecimal.valueOf(spot),
                UnitEnum.CNY,
                BigDecimal.valueOf(100000),
                UnitEnum.PERCENT,
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(30),
                true,
                BigDecimal.valueOf(365),
                BigDecimal.valueOf(100),
                expirationTime.toLocalDate(),
                expirationTime.toLocalDate(),
                LocalDate.now(),
                UnitEnum.PERCENT,
                BigDecimal.valueOf(strike),
                BarrierObservationTypeEnum.TERMINAL,
                KnockDirectionEnum.UP,
                UnitEnum.PERCENT,
                BigDecimal.valueOf(strike),
                BigDecimal.valueOf(strike),
                "1D",
                fixingObservations,
                RebateTypeEnum.PAY_NONE,
                UnitEnum.PERCENT,
                BigDecimal.valueOf(98),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.valueOf(30).divide(BigDecimal.valueOf(365), 10, BigDecimal.ROUND_DOWN)
        );
        JsonNode assetJson = JsonUtils.mapper.valueToTree(asset);
        TradePositionDTO position = new TradePositionDTO(
                bookName,
                positionId,
                BigDecimal.valueOf(1.0),
                assetJson,
                ProductTypeEnum.BARRIER,
                InstrumentAssetClassTypeEnum.EQUITY,
                "counterparty",
                "counterparty",
                null,
                counterpartyAccount.accountCode
        );
        return new TradeDTO(
                bookName,
                tradeId,
                counterparty.partyCode(),
                sales.salesName,
                sales.salesCommission,
                tradeDate,
                Arrays.asList(position));

    }



    public static TradeDTO createAnnualizedVerticalSpreadTradeDTO(String tradeId, String positionId, String instrumentId, Double strike, Double spot, ExerciseTypeEnum exerciseType) {
        AnnualizedVerticalSpreadOptionDTO asset = new AnnualizedVerticalSpreadOptionDTO(
                ExerciseTypeEnum.EUROPEAN,
                InstrumentOfValuePartyRoleTypeEnum.SELLER,
                instrumentId,
                BigDecimal.valueOf(spot),
                UnitEnum.PERCENT,
                BigDecimal.valueOf(strike),
                BigDecimal.valueOf(123),
                BusinessCenterTimeTypeEnum.CLOSE,
                expirationTime.toLocalDate(),
                BigDecimal.valueOf(100),
                true,
                BigDecimal.valueOf(365),
                BigDecimal.valueOf(99),
                OptionTypeEnum.CALL,
                BigDecimal.valueOf(100000),
                UnitEnum.CNY,
                BigDecimal.valueOf(1),
                expirationTime.toLocalDate(),
                "123",
                LocalDate.now(),
                UnitEnum.PERCENT,
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(30),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.valueOf(100).divide(BigDecimal.valueOf(365), 10, BigDecimal.ROUND_DOWN)
        );
        JsonNode assetJson = JsonUtils.mapper.valueToTree(asset);
        TradePositionDTO position = new TradePositionDTO(
                bookName,
                positionId,
                BigDecimal.valueOf(1.0),
                assetJson,
                ProductTypeEnum.VERTICAL_SPREAD,
                InstrumentAssetClassTypeEnum.EQUITY,
                "counterparty",
                "counterparty",
                null,
                counterpartyAccount.accountCode
        );
        return new TradeDTO(
                bookName,
                tradeId,
                counterparty.partyCode(),
                sales.salesName,
                sales.salesCommission,
                tradeDate,
                Arrays.asList(position));
    }



}

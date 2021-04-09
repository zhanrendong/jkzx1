package tech.tongyu.bct.cm;

import tech.tongyu.bct.cm.core.AbsoluteDate;
import tech.tongyu.bct.cm.core.BusinessCenterEnum;
import tech.tongyu.bct.cm.core.BusinessCenterTimeTypeEnum;
import tech.tongyu.bct.cm.product.asset.AssetImpl;
import tech.tongyu.bct.cm.product.iov.*;
import tech.tongyu.bct.cm.product.iov.cmd.impl.CommodityFutureInstrument;
import tech.tongyu.bct.cm.product.iov.eqd.ListedEquityInstrument;
import tech.tongyu.bct.cm.product.iov.feature.OptionTypeEnum;
import tech.tongyu.bct.cm.product.iov.feature.SettlementTypeEnum;
import tech.tongyu.bct.cm.product.iov.impl.AnnualizedDigitalOption;
import tech.tongyu.bct.cm.product.iov.impl.AnnualizedVanillaOption;
import tech.tongyu.bct.cm.reference.elemental.Account;
import tech.tongyu.bct.cm.reference.elemental.Party;
import tech.tongyu.bct.cm.reference.impl.*;
import tech.tongyu.bct.cm.trade.NonEconomicPartyRole;
import tech.tongyu.bct.cm.trade.impl.BctTrade;
import tech.tongyu.bct.cm.trade.impl.BctTradePosition;
import tech.tongyu.bct.cm.trade.impl.SalesPartyRole;
import tech.tongyu.bct.common.util.JsonUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

import static tech.tongyu.bct.cm.reference.impl.CurrencyUnit.CNY;

public class TestTradeFactory {

    public static BusinessCenterEnum venue = BusinessCenterEnum.SSE;
    public static CurrencyUnit currency = CNY;
    public static Party party = new SimpleParty(UUID.randomUUID(), "001", "MSCO");
    public static Party counterparty = new SimpleParty(UUID.randomUUID(), "002", "aaa");
    public static Account counterpartyAccount = new SimpleAccount(UUID.randomUUID(), "a1", "a1");
    public static Party branch = new SimpleParty(UUID.randomUUID(), "001", "BRANCH_005");
    public static NonEconomicPartyRole sales = new SalesPartyRole(
            branch,
            "sales",
            1.5,
            0.03,
            51.5);

    public static LocalDateTime expirationTime =
            LocalDateTime.of(2018, 11, 30, 13, 25, 49);


    public static BctTrade createAnnulizedOptionTrade(ListedEquityInstrument underlyer, BigDecimal strike, BigDecimal term) {
        LocalDate tradeDate = LocalDate.of(2018, 11, 2);
        LocalDateTime expirationTime = LocalDateTime.of(2018, 11, 30, 13, 25, 49);
        LocalDate effectiveDate = tradeDate;
        UnitOfValue<BigDecimal> p = new UnitOfValue<BigDecimal>(Percent.get(), BigDecimal.valueOf(50.0));
        UnitOfValue<BigDecimal> fp = new UnitOfValue<BigDecimal>(Percent.get(), BigDecimal.valueOf(50.0));
        UnitOfValue<BigDecimal> mp = new UnitOfValue<BigDecimal>(Percent.get(), BigDecimal.valueOf(0));

        LocalDate premiumPaymentDate = tradeDate;
        LocalDate settlementDate = LocalDate.of(2018, 11, 30);
        BigDecimal spotPrice = BigDecimal.valueOf(100.0);
        BigDecimal notional = BigDecimal.valueOf(100000.0);
        UnitOfValue<BigDecimal> notionalAmount = new UnitOfValue<BigDecimal>(CNY, notional);
        BigDecimal participationRate = BigDecimal.valueOf(100);
        BigDecimal annValRatio = term.divide(BigDecimal.valueOf(365), 10, BigDecimal.ROUND_DOWN);

        AnnualizedVanillaOption iov = new AnnualizedVanillaOption(
                underlyer,
                BigDecimal.valueOf(1),
                OptionTypeEnum.CALL,
                ProductTypeEnum.VANILLA_EUROPEAN,
                underlyer.assetClassType(), notionalAmount,
                participationRate,
                effectiveDate,
                expirationTime.toLocalDate(),
                BigDecimal.valueOf(365),
                term,
                true,
                ExerciseTypeEnum.EUROPEAN,
                new AbsoluteDate(expirationTime.toLocalDate()),
                expirationTime.toLocalTime(),
                spotPrice,
                settlementDate,
                SettlementTypeEnum.CASH,
                new UnitOfValue<BigDecimal>(CNY, strike),
                effectiveDate, BusinessCenterTimeTypeEnum.CLOSE, fp, mp, p, annValRatio
        );

        AssetImpl asset = new AssetImpl(
                iov,
                party,
                InstrumentOfValuePartyRoleTypeEnum.SELLER,
                counterparty,
                InstrumentOfValuePartyRoleTypeEnum.BUYER);
        BctTradePosition position = new BctTradePosition(
                "options",
                "a1_1",
                counterparty,
                1.0,
                asset,
                null,
                counterpartyAccount);

        BctTrade bctTrade = new BctTrade(
                "b1", "a1", "trader", Arrays.asList(position), Arrays.asList(sales), tradeDate, "test");

        JsonUtils.objectToJsonString(bctTrade);
        return bctTrade;

    }


    public static BctTrade createAnnulizedDigitalOptionTrade(ListedEquityInstrument underlyer, BigDecimal strike, BigDecimal term) {
        LocalDate tradeDate = LocalDate.of(2018, 11, 2);
        LocalDateTime expirationTime = LocalDateTime.of(2018, 11, 30, 13, 25, 49);
        LocalDate effectiveDate = tradeDate;
        UnitOfValue<BigDecimal> p = new UnitOfValue<BigDecimal>(Percent.get(), BigDecimal.valueOf(50.0));
        UnitOfValue<BigDecimal> fp = new UnitOfValue<BigDecimal>(Percent.get(), BigDecimal.valueOf(50.0));
        UnitOfValue<BigDecimal> mp = new UnitOfValue<BigDecimal>(Percent.get(), BigDecimal.valueOf(0));

        LocalDate premiumPaymentDate = tradeDate;
        LocalDate settlementDate = LocalDate.of(2018, 11, 30);
        BigDecimal spotPrice = BigDecimal.valueOf(100.0);
        BigDecimal notional = BigDecimal.valueOf(100000.0);
        UnitOfValue<BigDecimal> notionalAmount = new UnitOfValue<BigDecimal>(CNY, notional);
        BigDecimal participationRate = BigDecimal.valueOf(100);

        UnitOfValue<BigDecimal> payment = new UnitOfValue<BigDecimal>(Percent.get(), BigDecimal.valueOf(0.1));

        AnnualizedDigitalOption iov = new AnnualizedDigitalOption(
                underlyer,
                OptionTypeEnum.CALL,
                BigDecimal.valueOf(1),
                ProductTypeEnum.DIGITAL,
                underlyer.assetClassType(),
                BusinessCenterTimeTypeEnum.CLOSE,
                ExerciseTypeEnum.EUROPEAN,
                spotPrice,
                participationRate,
                notionalAmount,
                fp,
                mp,
                p,
                true,
                BigDecimal.valueOf(365),
                term,
                SettlementTypeEnum.CASH,
                settlementDate,
                effectiveDate,
                expirationTime.toLocalDate(),
                effectiveDate,
                new AbsoluteDate(expirationTime.toLocalDate()),
                null,
                new UnitOfValue<BigDecimal>(CNY, strike),
                BarrierObservationTypeEnum.TERMINAL,
                RebateTypeEnum.PAY_WHEN_HIT,
                payment,
                term.divide(BigDecimal.valueOf(365), 10, BigDecimal.ROUND_DOWN)
        );

        AssetImpl asset = new AssetImpl(
                iov,
                party,
                InstrumentOfValuePartyRoleTypeEnum.SELLER,
                counterparty,
                InstrumentOfValuePartyRoleTypeEnum.BUYER);
        BctTradePosition position = new BctTradePosition(
                "options",
                "a1_1",
                counterparty,
                1.0,
                asset,
                null,
                counterpartyAccount);

        BctTrade bctTrade = new BctTrade(
                "b1", "a1", "trader", Arrays.asList(position), Arrays.asList(sales), tradeDate, "test");

        JsonUtils.objectToJsonString(bctTrade);
        return bctTrade;

    }



}

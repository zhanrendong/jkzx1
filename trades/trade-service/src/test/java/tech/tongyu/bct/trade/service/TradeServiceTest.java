package tech.tongyu.bct.trade.service;

import org.junit.Test;
import org.springframework.data.redis.core.RedisTemplate;
import tech.tongyu.bct.cm.product.asset.Asset;
import tech.tongyu.bct.cm.product.iov.*;
import tech.tongyu.bct.cm.product.iov.impl.AnnualizedKnockOutOption;
import tech.tongyu.bct.cm.product.iov.impl.AnnualizedDigitalOption;
import tech.tongyu.bct.cm.product.iov.impl.AnnualizedVanillaOption;
import tech.tongyu.bct.cm.product.iov.impl.AnnualizedVerticalSpreadOption;
import tech.tongyu.bct.cm.reference.impl.UnitOfValue;
import tech.tongyu.bct.cm.trade.impl.BctTrade;
import tech.tongyu.bct.cm.trade.impl.BctTradePosition;
import tech.tongyu.bct.market.service.MarketDataService;
import tech.tongyu.bct.trade.dao.repo.*;
import tech.tongyu.bct.trade.dto.trade.TradeDTO;
import tech.tongyu.bct.trade.manager.PositionManager;
import tech.tongyu.bct.trade.manager.TradeManager;
import tech.tongyu.bct.trade.manager.TradePositionManager;
import tech.tongyu.bct.trade.service.impl.BookServiceImpl;
import tech.tongyu.bct.trade.service.impl.PositionServiceImpl;
import tech.tongyu.bct.trade.service.impl.TradeEventComponent;
import tech.tongyu.bct.trade.service.impl.TradeServiceImpl;
import tech.tongyu.bct.trade.service.impl.lcm.*;
import tech.tongyu.bct.trade.service.impl.lcm.composite.VanillaOptionLCMProcessor;
import tech.tongyu.bct.trade.service.impl.transformer.AnnualizedKnockOutOptionTransformer;
import tech.tongyu.bct.trade.service.impl.transformer.AnnualizedDigitalOptionTransformer;
import tech.tongyu.bct.trade.service.impl.transformer.AnnualizedVanillaOptionTransformer;
import tech.tongyu.bct.trade.service.impl.transformer.AnnualizedVerticalSpreadOptionTransformer;
import tech.tongyu.bct.trade.service.mock.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TradeServiceTest extends TradeTestCommon {
    RedisTemplate mockRedis = new MockRedis();
    MarketDataService marketDataService = new MockMarketDataService();
    LCMEventRepo lcmEventRepo = new MockLCMEventRepo();
    TradeEventRepo tradeEventRepo = new MockTradeEventRepo();
    PositionManager positionManager = new MockTradePositionManager();
    PositionBookRepo bookRepo = new MockBookRepo();
    TradePositionIndexRepo indexRepo = new MockIndexRepo();
    BookService bookService = new BookServiceImpl(bookRepo);
    TradeManager tradeManager = new MockTradeManager();
    TradePositionManager tradePositionManager;
    List<AssetTransformer> assetTransformers = Arrays.asList(
            new AnnualizedDigitalOptionTransformer(new MockMarketDataService()),
            new AnnualizedVanillaOptionTransformer(new MockMarketDataService()),
            new AnnualizedKnockOutOptionTransformer(new MockMarketDataService()),
            new AnnualizedVerticalSpreadOptionTransformer(new MockMarketDataService())


    );

    OpenOptionPositionProcessor openProcessor = new OpenOptionPositionProcessor(lcmEventRepo);
    PositionService positionService = new PositionServiceImpl(positionManager, assetTransformers, indexRepo, openProcessor);
    TradeEventComponent tradeEventComponent = new TradeEventComponent();
    TradeService tradeService = new TradeServiceImpl(bookService, tradeManager,lcmEventRepo,mockRedis, tradeEventRepo,
            positionService, tradeEventComponent, tradePositionManager, indexRepo);
    List<LCMCompositeProcessor> lcmProcessors = Arrays.asList(
            new VanillaOptionLCMProcessor(
                    new UnwindProcessor(lcmEventRepo),
                    new RollOptionProcessor(lcmEventRepo),
                    new ExpirationProcessor(lcmEventRepo, marketDataService),
                    new VanillaExerciseProcessor(lcmEventRepo),
                    new PaymentOptionProcessor(lcmEventRepo),
                    new PositionAmendProcessor(lcmEventRepo, assetTransformers)
            )
    );


    @Test
    public void testCreateAnnualVanillaEquityTrade() {
        bookService.createBook("options");
        TradeDTO tradeDTO = TestPositionFactory.createAnnualizedVanillaTradeDTO("t1", "t1_0", "600519.SH", 120.0, 120.0, ExerciseTypeEnum.EUROPEAN);
        tradeService.create(tradeDTO, OffsetDateTime.now());
        Optional<BctTrade> trades = tradeManager.getByTradeId("t1", OffsetDateTime.now(), OffsetDateTime.now());
        assertTrue(trades.isPresent());
        BctTrade trade = trades.get();
        setPositions(trade);
        assertEquals(trade.tradeId, "t1");
        assertEquals(trade.bookName, TestPositionFactory.bookName);
        assertTrue(trade.positions.size() == 1);
        BctTradePosition position = trade.positions.get(0);
        assertEquals(position.positionId, "t1_0");
        assertEquals(position.partyRole(), InstrumentOfValuePartyRoleTypeEnum.SELLER);
        Asset<InstrumentOfValue> asset = position.asset;
        assertTrue(asset.instrumentOfValue() instanceof AnnualizedVanillaOption);
        AnnualizedVanillaOption optionInstrument = (AnnualizedVanillaOption) asset.instrumentOfValue();
        assertEquals(optionInstrument.underlyer.instrumentId(), "600519.SH");
        assertTrue(optionInstrument.exerciseType.equals(ExerciseTypeEnum.EUROPEAN));
        assertEquals(((BigDecimal) (optionInstrument.strike().value())).toPlainString(), "120");
        assertEquals(optionInstrument.productType(), ProductTypeEnum.VANILLA_EUROPEAN);
    }

    @Test
    public void testCreateAnnualDigitalEquityTrade() {

        bookService.createBook("options");
        TradeDTO tradeDTO = TestPositionFactory.createAnnualizedDigitalTradeDTO("t1", "t1_0", "600519.SH", 120.0, 120.0, ExerciseTypeEnum.EUROPEAN);
        tradeService.create(tradeDTO, OffsetDateTime.now());
        Optional<BctTrade> trades = tradeManager.getByTradeId("t1", OffsetDateTime.now(), OffsetDateTime.now());
        assertTrue(trades.isPresent());
        BctTrade trade = trades.get();
        setPositions(trade);
        assertEquals(trade.tradeId, "t1");
        assertEquals(trade.bookName, TestPositionFactory.bookName);
        assertTrue(trade.positions.size() == 1);
        BctTradePosition position = trade.positions.get(0);
        assertEquals(position.positionId, "t1_0");
        assertEquals(position.partyRole(), InstrumentOfValuePartyRoleTypeEnum.SELLER);
        Asset<InstrumentOfValue> asset = position.asset;
        assertTrue(asset.instrumentOfValue() instanceof AnnualizedDigitalOption);
        AnnualizedDigitalOption optionInstrument = (AnnualizedDigitalOption) asset.instrumentOfValue();
        assertEquals(optionInstrument.underlyer.instrumentId(), "600519.SH");
        assertTrue(optionInstrument.exerciseType.equals(ExerciseTypeEnum.EUROPEAN));
        assertEquals(((BigDecimal) (optionInstrument.strike().value())).toPlainString(), "120");
        assertEquals(optionInstrument.productType(), ProductTypeEnum.DIGITAL);
    }


    @Test
    public void testCreateAnnualBarrierEquityTrade() {

        bookService.createBook("options");
        TradeDTO tradeDTO = TestPositionFactory.createAnnualizedBarrierTradeDTO("t1", "t1_0", "600519.SH", 120.0, 120.0, ExerciseTypeEnum.EUROPEAN);
        tradeService.create(tradeDTO, OffsetDateTime.now());
        Optional<BctTrade> trades = tradeManager.getByTradeId("t1", OffsetDateTime.now(), OffsetDateTime.now());
        assertTrue(trades.isPresent());
        BctTrade trade = trades.get();
        setPositions(trade);
        assertEquals(trade.tradeId, "t1");
        assertEquals(trade.bookName, TestPositionFactory.bookName);
        assertTrue(trade.positions.size() == 1);
        BctTradePosition position = trade.positions.get(0);
        assertEquals(position.positionId, "t1_0");
        assertEquals(position.partyRole(), InstrumentOfValuePartyRoleTypeEnum.SELLER);
        Asset<InstrumentOfValue> asset = position.asset;
        assertTrue(asset.instrumentOfValue() instanceof AnnualizedKnockOutOption);
        AnnualizedKnockOutOption optionInstrument = (AnnualizedKnockOutOption) asset.instrumentOfValue();
        assertEquals(optionInstrument.underlyer.instrumentId(), "600519.SH");
        assertTrue(optionInstrument.exerciseType.equals(ExerciseTypeEnum.EUROPEAN));
        assertEquals(((BigDecimal) (optionInstrument.strike().value())).toPlainString(), "120");
        assertEquals(optionInstrument.productType(), ProductTypeEnum.BARRIER);
    }


    @Test
    public void testCreateAnnualVerticalSpreadEquityTrade() {

        bookService.createBook("options");
        TradeDTO tradeDTO = TestPositionFactory.createAnnualizedVerticalSpreadTradeDTO("t1", "t1_0", "600519.SH", 120.0, 120.0, ExerciseTypeEnum.EUROPEAN);
        tradeService.create(tradeDTO, OffsetDateTime.now());
        Optional<BctTrade> trades = tradeManager.getByTradeId("t1", OffsetDateTime.now(), OffsetDateTime.now());
        assertTrue(trades.isPresent());
        BctTrade trade = trades.get();
        setPositions(trade);
        assertEquals(trade.tradeId, "t1");
        assertEquals(trade.bookName, TestPositionFactory.bookName);
        assertTrue(trade.positions.size() == 1);
        BctTradePosition position = trade.positions.get(0);
        assertEquals(position.positionId, "t1_0");
        assertEquals(position.partyRole(), InstrumentOfValuePartyRoleTypeEnum.SELLER);
        Asset<InstrumentOfValue> asset = position.asset;
        assertTrue(asset.instrumentOfValue() instanceof AnnualizedVerticalSpreadOption);
        AnnualizedVerticalSpreadOption optionInstrument = (AnnualizedVerticalSpreadOption) asset.instrumentOfValue();
        assertEquals(optionInstrument.underlyer.instrumentId(), "600519.SH");
        assertTrue(optionInstrument.exerciseType.equals(ExerciseTypeEnum.EUROPEAN));
        assertEquals(((BigDecimal) (((UnitOfValue)(optionInstrument.strikes.get(0))).value())).toPlainString(), "120");
        assertEquals(((BigDecimal) (((UnitOfValue)(optionInstrument.strikes.get(1))).value())).toPlainString(), "123");
        assertEquals(optionInstrument.productType(), ProductTypeEnum.VERTICAL_SPREAD);
    }


    private void setPositions(BctTrade bctTrade) {
        List<BctTradePosition> positions = bctTrade.getPositionIds().stream()
                .map(pid -> positionManager.getByPositionId(pid).get()).collect(Collectors.toList());
        bctTrade.setPositions(positions);
    }

}

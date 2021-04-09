package tech.tongyu.bct.trade.service;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.data.redis.core.RedisTemplate;
import tech.tongyu.bct.cm.core.AbsoluteDate;
import tech.tongyu.bct.cm.core.AdjustedDate;
import tech.tongyu.bct.cm.product.iov.ExerciseTypeEnum;
import tech.tongyu.bct.cm.trade.LCMEventTypeEnum;
import tech.tongyu.bct.cm.trade.impl.BctTrade;
import tech.tongyu.bct.cm.trade.impl.BctTradePosition;
import tech.tongyu.bct.market.service.MarketDataService;
import tech.tongyu.bct.trade.dao.dbo.PositionBook;
import tech.tongyu.bct.trade.dao.repo.*;
import tech.tongyu.bct.trade.dto.trade.TradeDTO;
import tech.tongyu.bct.trade.manager.PositionManager;
import tech.tongyu.bct.trade.manager.TradeManager;
import tech.tongyu.bct.trade.manager.TradePositionManager;
import tech.tongyu.bct.trade.service.impl.*;
import tech.tongyu.bct.trade.service.impl.lcm.*;
import tech.tongyu.bct.trade.service.impl.lcm.composite.VanillaOptionLCMProcessor;
import tech.tongyu.bct.trade.service.impl.transformer.AnnualizedDigitalOptionTransformer;
import tech.tongyu.bct.trade.service.impl.transformer.AnnualizedKnockOutOptionTransformer;
import tech.tongyu.bct.trade.service.impl.transformer.AnnualizedVanillaOptionTransformer;
import tech.tongyu.bct.trade.service.mock.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;

public class TradeLCMTest extends TradeTestCommon {
    RedisTemplate redisTemplate = new MockRedis();
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
            new AnnualizedKnockOutOptionTransformer(marketDataService),
            new AnnualizedDigitalOptionTransformer(marketDataService),
            new AnnualizedVanillaOptionTransformer(marketDataService)
            );

    OpenOptionPositionProcessor openProcessor = new OpenOptionPositionProcessor(lcmEventRepo);
    PositionService positionService = new PositionServiceImpl(positionManager, assetTransformers,
            indexRepo, openProcessor);
    TradeEventComponent tradeEventComponent = new TradeEventComponent();
    TradeService tradeService = new TradeServiceImpl(bookService, tradeManager, lcmEventRepo, redisTemplate,
            tradeEventRepo, positionService, tradeEventComponent, tradePositionManager, indexRepo);
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
    LCMNotificationRepo lcmNotificationRepo = new MockLCMNotificationRepo();
    PositionLCMService positionLCMService = new PositionLCMServiceImpl(lcmProcessors, positionManager);
//    TradeLCMService tradeLCMService = new TradeLCMServiceImpl(tradeManager, positionLCMService, lcmProcessors, lcmNotificationRepo, tradeEventComp);

    {
        PositionBook book = new PositionBook();
        book.setBookName("options");
        bookRepo.save(book);
    }

    @Test
    public void testCalcEuroVanillaLCMEventType() {
        BctTrade trade = createEuroVanillaTrade("t1", "t1_0");
        Set<LCMEventTypeEnum> types = positionLCMService.getSupportedPositionLCMEventType(
                trade.positions.get(0)).stream().collect(Collectors.toSet());
        Set<LCMEventTypeEnum> expectedTypes = Arrays.asList(
                LCMEventTypeEnum.PAYMENT,
                LCMEventTypeEnum.EXERCISE, LCMEventTypeEnum.ROLL, LCMEventTypeEnum.EXPIRATION, LCMEventTypeEnum.UNWIND, LCMEventTypeEnum.AMEND)
                .stream().collect(Collectors.toSet());
        Assert.assertTrue(types.equals(expectedTypes));
    }

    @Test
    public void testCreateEuroVanillaLCMNotification() {
//        BctTrade trade = createEuroVanillaTrade("t1", "p1");
//        VanillaOption optionInstrument = (VanillaOption) trade.positions.get(0).asset.instrumentOfValue();
//        List<LCMNotificationDTO> notifications = tradeLCMService.generateLCMEvents("t1", OffsetDateTime.now());
//        assertFalse(notifications.isEmpty());
//        assertEquals(notifications.size(), 2);
//        notifications.forEach(n -> {
//            assertEquals(n.tradeId, "t1");
//            assertEquals(n.positionId, "p1");
//        });
//        List<LCMNotificationDTO> res;
//        res = notifications.stream()
//                .filter(n -> n.eventInfo.notificationEventType.equals(LCMEventTypeEnum.EXPIRATION))
//                .collect(Collectors.toList());
//        assertTrue(res.size() == 1);
//        assertTrue(res.get(0).eventInfo instanceof LCMExpirationNotificationDTO);
//        LCMExpirationNotificationDTO expirationNotification = (LCMExpirationNotificationDTO) res.get(0).eventInfo;
//        assertEquals(expirationNotification.productType, ProductTypeEnum.VANILLA_EUROPEAN);
//        LocalDate expectedExpirationDate = toLocalDate(optionInstrument.optionExercise().expirationDate());
//        assertEquals(expirationNotification.expirationTime.toLocalDate(), expectedExpirationDate);
//        res = notifications.stream()
//                .filter(n -> n.eventInfo.notificationEventType.equals(LCMEventTypeEnum.EXERCISE))
//                .collect(Collectors.toList());
//        assertTrue(res.get(0).eventInfo instanceof LCMExpirationNotificationDTO);
//        LCMExpirationNotificationDTO exerciseNotification = (LCMExpirationNotificationDTO) res.get(0).eventInfo;
//        assertEquals(exerciseNotification.expirationTime.toLocalDate(), expectedExpirationDate);
    }

    @Test
    public void testGenerateEuroVanillaLCMNotification() {
//        BctTrade trade = createEuroVanillaTrade("t2", "p2");
//        VanillaOption optionInstrument = (VanillaOption) trade.positions.get(0).asset.instrumentOfValue();
//        List<LCMNotificationDTO> notifications = tradeLCMService.reGenerateLCMEvents("t2", OffsetDateTime.now());
//        List<LCMNotification> loadedNotifications = lcmNotificationRepo.findAllByTradeId("t2");
//        assertEquals(notifications.size(), loadedNotifications.size());
//        loadedNotifications.forEach(n -> {
//            assertEquals(n.getTradeId(), "t2");
//            assertEquals(n.getPositionId(), "p2");
//            assertTrue(notifications.stream().anyMatch(n2 -> n2.notificationUUID.equals(n.getUuid().toString())));
//        });
    }

    private BctTrade createEuroVanillaTrade(String tradeId, String positionId) {
        TradeDTO tradeDTO = TestPositionFactory.createAnnualizedVanillaTradeDTO(tradeId, positionId,
                "600519.SH", 120.0, 120.0, ExerciseTypeEnum.EUROPEAN);
        tradeService.create(tradeDTO, OffsetDateTime.now());
        Optional<BctTrade> optional = tradeManager.getByTradeId(tradeId, OffsetDateTime.now(), OffsetDateTime.now());
        assertTrue(optional.isPresent());
        BctTrade bctTrade = optional.get();
        List<BctTradePosition> positions = bctTrade.getPositionIds().stream().map(pid -> positionManager.getByPositionId(pid).get()).collect(Collectors.toList());
        bctTrade.setPositions(positions);
        return bctTrade;
    }

    private LocalDate toLocalDate(AdjustedDate date) {
        if (date instanceof AbsoluteDate) {
            return ((AbsoluteDate) date).unadjustedDate;
        } else {
            throw new IllegalArgumentException(String.format("%s date is not supported", date));
        }
    }
}

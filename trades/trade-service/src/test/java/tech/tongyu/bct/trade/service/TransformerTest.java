package tech.tongyu.bct.trade.service;

import org.junit.Test;

import tech.tongyu.bct.cm.product.iov.ExerciseTypeEnum;
import tech.tongyu.bct.cm.trade.impl.BctTradePosition;
import tech.tongyu.bct.trade.dto.trade.TradeDTO;
import tech.tongyu.bct.trade.dto.trade.TradePositionDTO;
import tech.tongyu.bct.trade.service.impl.PositionServiceImpl;
import tech.tongyu.bct.trade.service.impl.transformer.AnnualizedKnockOutOptionTransformer;
import tech.tongyu.bct.trade.service.impl.transformer.AnnualizedDigitalOptionTransformer;
import tech.tongyu.bct.trade.service.impl.transformer.AnnualizedVanillaOptionTransformer;
import tech.tongyu.bct.trade.service.impl.transformer.AnnualizedVerticalSpreadOptionTransformer;
import tech.tongyu.bct.trade.service.mock.MockMarketDataService;

import java.util.LinkedList;

public class TransformerTest {
    AnnualizedVanillaOptionTransformer transformer = new AnnualizedVanillaOptionTransformer(new MockMarketDataService());
    AnnualizedDigitalOptionTransformer transformer2 = new AnnualizedDigitalOptionTransformer(new MockMarketDataService());
    AnnualizedKnockOutOptionTransformer transformer3 = new AnnualizedKnockOutOptionTransformer(new MockMarketDataService());
    AnnualizedVerticalSpreadOptionTransformer transformer4 = new AnnualizedVerticalSpreadOptionTransformer(new MockMarketDataService());

    LinkedList list = new LinkedList();
    {
        list.add(transformer);
        list.add(transformer2);
        list.add(transformer3);
        list.add(transformer4);

    }

    PositionServiceImpl ps = new PositionServiceImpl(null, list,null, null);

    @Test
    public void VanillaOptionTest() {
        TradeDTO origin = TestPositionFactory.createAnnualizedVanillaTradeDTO("test",
                "test_1", "asdasd", 100.0, 200.0, ExerciseTypeEnum.EUROPEAN);

        BctTradePosition temp = ps.fromDTO(origin.positions.get(0));
        TradePositionDTO converted = ps.toDTO(temp);
        System.out.println(converted);
    }

    @Test
    public void DigitalOptionTest() {
        TradeDTO origin = TestPositionFactory.createAnnualizedDigitalTradeDTO("test",
                "test_1", "asdasd", 100.0, 200.0, ExerciseTypeEnum.AMERICAN);
        BctTradePosition temp = ps.fromDTO(origin.positions.get(0));
        TradePositionDTO converted = ps.toDTO(temp);
        System.out.println(converted);
    }

    @Test
    public void BarrierOptionTest() {
        TradeDTO origin = TestPositionFactory.createAnnualizedBarrierTradeDTO("test",
                "test_1", "asdasd", 100.0, 200.0, ExerciseTypeEnum.AMERICAN);
        BctTradePosition temp = ps.fromDTO(origin.positions.get(0));
        TradePositionDTO converted = ps.toDTO(temp);
        System.out.println(converted);
    }


    @Test
    public void VerticalSpreadOptionTest() {
        TradeDTO origin = TestPositionFactory.createAnnualizedVerticalSpreadTradeDTO("test",
                "test_1", "asdasd", 100.0, 200.0, ExerciseTypeEnum.AMERICAN);
        BctTradePosition temp = ps.fromDTO(origin.positions.get(0));
        TradePositionDTO converted = ps.toDTO(temp);
        System.out.println(converted);
    }

}

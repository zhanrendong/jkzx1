package tech.tongyu.bct.quant.library.priceable.equity;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.Before;
import org.junit.Test;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.quant.library.priceable.Position;
import tech.tongyu.bct.quant.library.priceable.Priceable;
import tech.tongyu.bct.quant.library.priceable.common.flag.OptionTypeEnum;
import tech.tongyu.bct.quant.library.priceable.feature.HasUnderlyer;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class EquityVerticalSpreadComboTest {
    private final String positionId = "test";
    private EquityStock underlyer;
    private EquityVerticalSpreadCombo<EquityStock> spreadCombo;

    @Before
    public void setup() {
        underlyer = new EquityStock("000002.SZ");
        spreadCombo = new EquityVerticalSpreadCombo<>(underlyer, 97, 103,
                LocalDateTime.of(2019, 12, 15, 15, 0),
                OptionTypeEnum.CALL);
    }

    @Test
    public void testDecompose() throws Exception {
        Position position = new Position(positionId, 1.0, spreadCombo);
        List<Position> decomposed = position.decompose();
        assertEquals(2, decomposed.size());
        assertEquals(1.0, decomposed.get(0).getQuantity(), 1e-16);
        assertEquals(-1.0, decomposed.get(1).getQuantity(), 1e-16);

        String json = JsonUtils.objectToJsonString(decomposed);
        List<Position> recovered = JsonUtils.mapper.readValue(json, new TypeReference<List<Position>>() {});
        assertEquals(2, recovered.size());
        assertEquals(1.0, recovered.get(0).getQuantity(), 1e-16);
        assertEquals(-1.0, recovered.get(1).getQuantity(), 1e-16);
    }

    @Test
    public void testSerialization() throws Exception {
        String json = JsonUtils.objectToJsonString(this.spreadCombo);
        Priceable recovered = JsonUtils.mapper.readValue(json, Priceable.class);
        assertEquals(this.underlyer.getInstrumentId(),
                ((EquityStock)((HasUnderlyer)recovered).getUnderlyer()).getInstrumentId());
    }

    @Test
    public void testPositionListSerialization() throws Exception {
        EquityVanillaEuropean<EquityStock> low = new EquityVanillaEuropean<>(underlyer, spreadCombo.getStrikeLow(),
                spreadCombo.getExpiry(), spreadCombo.getOptionType());
        Position positionLow = new Position(positionId, 1., low);
        EquityVanillaEuropean<EquityStock> high = new EquityVanillaEuropean<>(underlyer, spreadCombo.getStrikeHigh(),
                spreadCombo.getExpiry(), spreadCombo.getOptionType());
        Position positionHigh = new Position(positionId, -1.0, high);
        Position positionSpread = new Position(positionId, -1.0, spreadCombo);
        List<Position> ps = Arrays.asList(positionLow, positionHigh, positionSpread);
        String json = JsonUtils.objectToJsonString(ps);
        List<Position> recovered = JsonUtils.mapper.readValue(json, new TypeReference<List<Position>>() {});
        assertEquals(3, recovered.size());
    }
}

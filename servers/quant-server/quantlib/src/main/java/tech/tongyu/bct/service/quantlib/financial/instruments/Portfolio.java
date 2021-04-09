package tech.tongyu.bct.service.quantlib.financial.instruments;

import com.fasterxml.jackson.annotation.JsonProperty;
import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantSerializable;

import java.util.ArrayList;
import java.util.List;

/**
 * A portfolio is a collection of positions
 */
@BctQuantSerializable
public class Portfolio {
    @JsonProperty("class")
    private final String type = Portfolio.class.getSimpleName();

    private List<Position<?>> positions;

    public Portfolio() {
        this.positions = new ArrayList<>();
    }
    public Portfolio(List<Position<?>> positions) {
        this.positions = positions;
    }

    public void add(Position<?> position) {
        this.positions.add(position);
    }

    public List<Position<?>> getPositions() {
        return positions;
    }

    /*public static void main(String[] args) throws Exception {
        Portfolio portfolio = new Portfolio();
        VanillaEuropean optn = VanillaEuropean.call(100.0,
                LocalDateTime.of(2017, 7, 1, 0, 0), LocalDateTime.of(2017, 7, 1, 0, 0));
        DigitalCash digi = new DigitalCash(OptionType.CALL, 100.0, 1.0,
                LocalDateTime.of(2018, 7, 1, 0, 0), LocalDateTime.of(2018, 7, 1, 0, 0));
        portfolio.add(new Position<>(optn, 1.0));
        portfolio.add(new Position<>(digi, -1.0));
        // test serialization
        String s = JsonMapper.mapper.writerWithDefaultPrettyPrinter().writeValueAsString(portfolio);
        System.out.println(s);
        // test restoration
        Portfolio portfolioRestored = JsonMapper.mapper.readValue(s, Portfolio.class);
        System.out.println(portfolioRestored.getPositions().get(0).getQuantity());
    }*/
}
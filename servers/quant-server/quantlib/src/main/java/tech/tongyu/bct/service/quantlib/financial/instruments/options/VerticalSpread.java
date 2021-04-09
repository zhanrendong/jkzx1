package tech.tongyu.bct.service.quantlib.financial.instruments.options;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantSerializable;
import tech.tongyu.bct.service.quantlib.common.enums.OptionType;
import tech.tongyu.bct.service.quantlib.financial.instruments.Portfolio;
import tech.tongyu.bct.service.quantlib.financial.instruments.Position;

import java.time.LocalDateTime;

/**
 * @author Liao Song
 * @since 2016-10-12
 */
@BctQuantSerializable
public class VerticalSpread {
    @JsonProperty("class")
    private final String instrument = VerticalSpread.class.getSimpleName();
    private double lowerStrike;
    private double upperStrike;
    private LocalDateTime expiry;
    private LocalDateTime delivery;
    private OptionType type;

    @JsonCreator
    public VerticalSpread(
            @JsonProperty("lowerStrike") double lowerStrike,
            @JsonProperty("upperStrike") double upperStrike,
            @JsonProperty("expiry") LocalDateTime expiry,
            @JsonProperty("delivery") LocalDateTime delivery,
            @JsonProperty("type") OptionType optionType) {
        this.lowerStrike = lowerStrike;
        this.upperStrike = upperStrike;
        this.expiry = expiry;
        this.delivery = delivery;
        this.type = optionType;
    }

    public Portfolio split() {
        Portfolio combination = new Portfolio();
        if (type == OptionType.CALL) {
            VanillaEuropean call1 = new VanillaEuropean(type, lowerStrike, expiry, delivery);
            combination.add(new Position<>(call1, 1.0));
            VanillaEuropean call2 = new VanillaEuropean(type, upperStrike, expiry, delivery);
            combination.add(new Position<>(call2, -1.0));

            return combination;
        } else {
            VanillaEuropean put1 = new VanillaEuropean(type, upperStrike, expiry, delivery);
            combination.add(new Position<>(put1, 1.0));
            VanillaEuropean put2 = new VanillaEuropean(type, lowerStrike, expiry, delivery);
            combination.add(new Position<>(put2, -1.0));
            return combination;
        }
    }

    public String getInstrument() {
        return instrument;
    }

    public double getLowerStrike() {
        return lowerStrike;
    }

    public double getUpperStrike() {
        return upperStrike;
    }


    public LocalDateTime getExpiry() {
        return expiry;
    }

    public LocalDateTime getDelivery() {
        return delivery;
    }

    public OptionType getType() {
        return type;
    }
}

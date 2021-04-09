package tech.tongyu.bct.service.quantlib.financial.instruments.linear;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Spot {
    @JsonProperty("class")
    private final String instrument = Spot.class.getSimpleName();
    private String underlying;
    @JsonCreator
    public Spot(String underlying) {
        this.underlying = underlying;
    }

    public String getUnderlying() {
        return underlying;
    }

    public void setUnderlying(String underlying) {
        this.underlying = underlying;
    }
}
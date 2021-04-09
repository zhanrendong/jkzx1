package tech.tongyu.bct.quant.library.priceable.common.product;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import tech.tongyu.bct.quant.library.priceable.Priceable;
import tech.tongyu.bct.quant.library.priceable.feature.HasExpiry;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class CustomProductModelXY implements Priceable, HasExpiry {
    private final String underlyerInstrumentId;
    private final LocalDateTime expiry;

    @JsonCreator
    public CustomProductModelXY(String underlyerInstrumentId, LocalDateTime expiry) {
        this.underlyerInstrumentId = underlyerInstrumentId;
        this.expiry = expiry;
    }

    @JsonIgnore
    @Override
    public PriceableTypeEnum getPriceableTypeEnum() {
        return PriceableTypeEnum.CUSTOM_MODEL_XY;
    }

    @JsonIgnore
    @Override
    public LocalDate getExpirationDate() {
        return this.expiry.toLocalDate();
    }

    public String getUnderlyerInstrumentId() {
        return underlyerInstrumentId;
    }

    @Override
    public LocalDateTime getExpiry() {
        return expiry;
    }
}

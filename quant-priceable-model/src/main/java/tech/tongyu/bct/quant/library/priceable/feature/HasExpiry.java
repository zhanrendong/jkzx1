package tech.tongyu.bct.quant.library.priceable.feature;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface HasExpiry {
    LocalDate getExpirationDate();
    LocalDateTime getExpiry();
}

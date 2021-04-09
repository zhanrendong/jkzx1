package tech.tongyu.bct.quant.library.priceable.feature;

import java.time.LocalDateTime;

public interface HasFixings {
    /**
     * Check if all fixings before valuation time are present
     * @param val valuation time
     * @return true if all fixings are present, otherwise false
     */
    boolean hasSufficientFixings(LocalDateTime val);
}

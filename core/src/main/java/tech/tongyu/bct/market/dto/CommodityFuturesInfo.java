package tech.tongyu.bct.market.dto;

import java.time.LocalDate;

/**
 * 商品期货
 *
 * @author Lu Lu
 */
public class CommodityFuturesInfo extends InstrumentCommonInfo implements InstrumentInfo {
    /**
     * 合约乘数
     */
    private int multiplier;
    /**
     * 合约到期日
     */
    private LocalDate maturity;

    @Override
    public Integer multiplier() {
        return multiplier;
    }

    public int getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(int multiplier) {
        this.multiplier = multiplier;
    }

    public LocalDate getMaturity() {
        return maturity;
    }

    public void setMaturity(LocalDate maturity) {
        this.maturity = maturity;
    }
}

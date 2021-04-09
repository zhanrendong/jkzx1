package tech.tongyu.bct.market.dto;

/**
 * 股票
 *
 * @author Lu Lu
 */
public class EquityStockInfo extends InstrumentCommonInfo implements InstrumentInfo {
    /**
     * 合约乘数
     */
    private int multiplier;

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

}

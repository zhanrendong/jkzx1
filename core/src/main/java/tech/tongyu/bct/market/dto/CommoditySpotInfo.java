package tech.tongyu.bct.market.dto;

/**
 * 商品现货
 *
 * @author Lu Lu
 */
public class CommoditySpotInfo extends InstrumentCommonInfo implements InstrumentInfo {
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

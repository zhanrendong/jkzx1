package tech.tongyu.bct.quant.library.priceable;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface Commodity extends AssetClass {
    @JsonIgnore
    default AssetClassEnum getAssetClassEnum() {
        return AssetClassEnum.COMMODITY;
    }
}

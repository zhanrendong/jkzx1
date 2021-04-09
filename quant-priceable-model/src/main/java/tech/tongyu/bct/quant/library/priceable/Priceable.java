package tech.tongyu.bct.quant.library.priceable;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tech.tongyu.bct.quant.library.common.QuantlibSerializableObject;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface Priceable extends QuantlibSerializableObject {
    enum PriceableTypeEnum {
        CASH_PAYMENT,
        CASH_FLOWS,
        EQUITY_STOCK,
        EQUITY_INDEX,
        EQUITY_INDEX_FUTURES,
        EQUITY_FORWARD,
        EQUITY_VANILLA_EUROPEAN,
        EQUITY_VANILLA_AMERICAN,
        EQUITY_VERTICAL_SPREAD,
        EQUITY_VERTICAL_SPREAD_COMBO,
        EQUITY_STRADDLE,
        EQUITY_EAGLE,
        EQUITY_DIGITAL_CASH,
        EQUITY_ONE_TOUCH,
        EQUITY_NO_TOUCH,
        EQUITY_KNOCK_OUT_CONTINUOUS,
        EQUITY_KNOCK_OUT_TERMINAL,
        EQUITY_DOUBLE_KNOCK_OUT_CONTINUOUS,
        EQUITY_DOUBLE_SHARK_FIN_CONTINUOUS,
        EQUITY_DOUBLE_SHARK_FIN_TERMINAL,
        EQUITY_AUTOCALL,
        EQUITY_DIGITAL_CONVEX,
        EQUITY_DIGITAL_CONCAVE,
        EQUITY_DOUBLE_DIGITAL,
        EQUITY_DOUBLE_TOUCH,
        EQUITY_THIRD_DIGITAL,
        EQUITY_ASIAN_FIXED_STRIKE_ARITHMETIC,
        EQUITY_RANGE_ACCRUAL,
        COMMODITY_SPOT,
        COMMODITY_FUTURES,
        COMMODITY_FORWARD,
        COMMODITY_VANILLA_EUROPEAN,
        COMMODITY_VANILLA_AMERICAN,
        COMMODITY_STRADDLE,
        COMMODITY_DIGITAL_CASH,
        COMMODITY_EAGLE,
        COMMODITY_ONE_TOUCH,
        COMMODITY_NO_TOUCH,
        COMMODITY_VERTICAL_SPREAD,
        COMMODITY_KNOCK_OUT_CONTINUOUS,
        COMMODITY_KNOCK_OUT_TERMINAL,
        COMMODITY_DOUBLE_KNOCK_OUT_CONTINUOUS,
        COMMODITY_DOUBLE_SHARK_FIN_CONTINUOUS,
        COMMODITY_DOUBLE_SHARK_FIN_TERMINAL,
        COMMODITY_AUTOCALL,
        COMMODITY_DIGITAL_CONVEX,
        COMMODITY_DIGITAL_CONCAVE,
        COMMODITY_DOUBLE_DIGITAL,
        COMMODITY_DOUBLE_TOUCH,
        COMMODITY_THIRD_DIGITAL,
        COMMODITY_ASIAN_FIXED_STRIKE_ARITHMETIC,
        COMMODITY_RANGE_ACCRUAL,
        // ir
        FIXED_LEG,
        FLOATING_LEG,
        FIXED_FLOATING_SWAP,
        // basket underlyer
        RATIO_VANILLA_EUROPEAN,
        SPREAD_VANILLA_EUROPEAN,
        SPREAD_DIGITAL_CASH,
        SPREAD_KNOCK_OUT_TERMINAL,
        // custom or generic
        CUSTOM_MODEL_XY, // a custom product type to be priced by custom interpolation pricer called model xy
        GENERIC_SINGLE_ASSET_OPTION, // placeholder for generic options (should be used only with pricing rules)
        GENERIC_MULTI_ASSET_OPTION // placeholder for generic options on multi-asset (basket underlyer)
    }
    PriceableTypeEnum getPriceableTypeEnum();
}

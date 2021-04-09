package tech.tongyu.bct.quant.api;

import org.springframework.stereotype.Service;
import tech.tongyu.bct.common.api.annotation.BctApiTagEnum;
import tech.tongyu.bct.common.api.annotation.BctMethodArg;
import tech.tongyu.bct.common.api.annotation.BctMethodInfo;
import tech.tongyu.bct.quant.library.common.EnumUtils;
import tech.tongyu.bct.quant.library.priceable.ir.leg.FixedLeg;
import tech.tongyu.bct.quant.library.priceable.ir.leg.FloatingLeg;
import tech.tongyu.bct.quant.library.priceable.ir.swap.Swap;
import tech.tongyu.bct.quant.library.priceable.ir.swap.impl.FixedFloatingSwap;
import tech.tongyu.bct.quant.service.cache.QuantlibObjectCache;

import java.util.Objects;

@Service
public class SwapApi {
    @BctMethodInfo(tags = {BctApiTagEnum.Excel})
    String qlIrSwapFixedFloatingCreate(
            @BctMethodArg String fixedLeg,
            @BctMethodArg String floatingLeg,
            @BctMethodArg(required = false) String direction,
            @BctMethodArg(required = false) String id
    ) {
        FixedLeg fixed = (FixedLeg) QuantlibObjectCache.Instance.getMayThrow(fixedLeg);
        FloatingLeg floating = (FloatingLeg) QuantlibObjectCache.Instance.getMayThrow(floatingLeg);
        Swap.Direction payOrReceive = Objects.isNull(direction) ?
                Swap.Direction.RECEIVE : EnumUtils.fromString(direction, Swap.Direction.class);
        return QuantlibObjectCache.Instance.put(new FixedFloatingSwap(fixed, floating, payOrReceive), id);
    }
}

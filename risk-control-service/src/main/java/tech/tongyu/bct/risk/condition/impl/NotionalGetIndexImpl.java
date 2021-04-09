package tech.tongyu.bct.risk.condition.impl;

import org.springframework.stereotype.Component;
import tech.tongyu.bct.common.util.DoubleUtils;
import tech.tongyu.bct.risk.dto.RiskTypeEnum;
import tech.tongyu.bct.risk.excepitons.ReturnMessageAndTemplateDef;
import tech.tongyu.bct.risk.excepitons.RiskControlException;
import tech.tongyu.bct.risk.condition.Index;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static tech.tongyu.bct.cm.reference.impl.UnitEnum.CNY;
import static tech.tongyu.bct.risk.condition.TriggerConstants.*;

/**
 * @author yongbin
 * - mailto: wuyongbin@tongyu.tech
 */
@Component
public class NotionalGetIndexImpl implements Index {

    @Override
    public String getIndexName() {
        return "总名义本金(¥)";
    }

    @Override
    public Object execute(Map<String, Object> formData, Map<String, Object> data) {
        if (!formData.containsKey(TRADE)) {
            throw new RiskControlException(ReturnMessageAndTemplateDef.Errors.INDEX_DATA_ERROR);
        }
        Map<String, Object> trade = (Map<String, Object>) formData.get(TRADE);
        List<Map<String, Object>> positions = (List<Map<String, Object>>) trade.get(POSITIONS);

        return positions.stream().map(position -> {
            Map<String, Object> asset = (Map<String, Object>) position.get(ASSET);
            if (Objects.equals(asset.get(NOTIONAL_AMOUNT_TYPE), CNY.name())) {
                if (!asset.containsKey(NOTIONAL_AMOUNT)){
                    throw new RiskControlException(ReturnMessageAndTemplateDef.Errors.INDEX_DATA_ERROR);
                }
                return DoubleUtils.num2Double(asset.get(NOTIONAL_AMOUNT));
            } else {
                if (!asset.containsKey(NOTIONAL_AMOUNT)
                        || !asset.containsKey(UNDERLYER_MULTIPLIER)
                        || !asset.containsKey(INITIAL_SPOT)){
                    throw new RiskControlException(ReturnMessageAndTemplateDef.Errors.INDEX_DATA_ERROR);
                }
                Double notionalAmount = DoubleUtils.num2Double(asset.get(NOTIONAL_AMOUNT));
                Double underlyerMultiplier = DoubleUtils.num2Double(asset.get(UNDERLYER_MULTIPLIER));
                Double initialSpot = DoubleUtils.num2Double(asset.get(INITIAL_SPOT));
                return notionalAmount * underlyerMultiplier * initialSpot;
            }
        }).collect(Collectors.summarizingDouble(value -> value)).getSum();
    }

    @Override
    public Class<?> getClassType() {
        return Double.class;
    }

    @Override
    public RiskTypeEnum getRiskType() {
        return RiskTypeEnum.LIMIT;
    }
}

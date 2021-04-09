package tech.tongyu.bct.workflow.process.trigger.impl;

import org.springframework.stereotype.Component;
import tech.tongyu.bct.workflow.exceptions.ReturnMessageAndTemplateDef;
import tech.tongyu.bct.workflow.exceptions.WorkflowCommonException;
import tech.tongyu.bct.workflow.process.trigger.Index;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static tech.tongyu.bct.cm.reference.impl.UnitEnum.CNY;
import static tech.tongyu.bct.workflow.process.trigger.TriggerConstants.*;

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
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.INDEX_DATA_ERROR);
        }
        Map<String, Object> trade = (Map<String, Object>) formData.get(TRADE);
        List<Map<String, Object>> positions = (List<Map<String, Object>>) trade.get(POSITIONS);

        return positions.stream().map(position -> {
            if (position.containsKey(PRODUCT_TYPE) && position.get(PRODUCT_TYPE).equals(CASH_FLOW)) {
                return BigDecimal.ZERO;
            }
            Map<String, Object> asset = (Map<String, Object>) position.get(ASSET);
            if (Objects.equals(asset.get(NOTIONAL_AMOUNT_TYPE), CNY.name())) {
                if (!asset.containsKey(NOTIONAL_AMOUNT)){
                    throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.INDEX_DATA_ERROR);
                }
                return new BigDecimal(asset.get(NOTIONAL_AMOUNT).toString());
            } else {
                if (!asset.containsKey(NOTIONAL_AMOUNT)
                        || !asset.containsKey(UNDERLYER_MULTIPLIER)
                        || !asset.containsKey(INITIAL_SPOT)){
                    throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.INDEX_DATA_ERROR);
                }
                BigDecimal notionalAmount = new BigDecimal(asset.get(NOTIONAL_AMOUNT).toString());
                BigDecimal underlyerMultiplier = new BigDecimal(asset.get(UNDERLYER_MULTIPLIER).toString());
                BigDecimal initialSpot = new BigDecimal(asset.get(INITIAL_SPOT).toString());
                return notionalAmount.multiply(underlyerMultiplier).multiply(initialSpot);
            }
        }).reduce(BigDecimal.ZERO,BigDecimal::add);
    }

    @Override
    public Class<?> getClassType() {
        return BigDecimal.class;
    }
}

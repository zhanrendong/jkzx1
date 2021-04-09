package tech.tongyu.bct.risk.condition.impl;

import org.springframework.stereotype.Component;
import tech.tongyu.bct.risk.dto.RiskTypeEnum;
import tech.tongyu.bct.risk.excepitons.ReturnMessageAndTemplateDef;
import tech.tongyu.bct.risk.excepitons.RiskControlException;
import tech.tongyu.bct.risk.condition.Index;

import java.util.Map;

import static tech.tongyu.bct.risk.condition.TriggerConstants.NUMBER;

/**
 * @author yongbin
 * - mailto: wuyongbin@tongyu.tech
 */
@Component
public class ReturnNumberIndexImpl implements Index {

    @Override
    public String getIndexName() {
        return "自定义数值";
    }

    @Override
    public Object execute(Map<String, Object> formData, Map<String, Object> data) {
        if (!data.containsKey(NUMBER)){
            throw new RiskControlException(ReturnMessageAndTemplateDef.Errors.INDEX_DATA_ERROR);
        }
        return data.get(NUMBER);
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

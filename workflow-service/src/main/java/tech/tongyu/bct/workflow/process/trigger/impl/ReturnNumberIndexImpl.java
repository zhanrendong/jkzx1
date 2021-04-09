package tech.tongyu.bct.workflow.process.trigger.impl;

import org.springframework.stereotype.Component;
import tech.tongyu.bct.workflow.exceptions.ReturnMessageAndTemplateDef;
import tech.tongyu.bct.workflow.exceptions.WorkflowCommonException;
import tech.tongyu.bct.workflow.process.trigger.Index;

import java.math.BigDecimal;
import java.util.Map;

import static tech.tongyu.bct.workflow.process.trigger.TriggerConstants.NUMBER;

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
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.INDEX_DATA_ERROR);
        }
        return new BigDecimal(data.get(NUMBER).toString());
    }

    @Override
    public Class<?> getClassType() {
        return BigDecimal.class;
    }
}

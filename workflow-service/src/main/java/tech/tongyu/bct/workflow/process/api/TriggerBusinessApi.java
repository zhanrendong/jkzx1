package tech.tongyu.bct.workflow.process.api;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.auth.authaction.intel.ResourcePermissionAuthAction;
import tech.tongyu.bct.auth.enums.ResourceTypeEnum;
import tech.tongyu.bct.auth.exception.AuthorizationException;
import tech.tongyu.bct.common.api.annotation.BctMethodArg;
import tech.tongyu.bct.common.api.annotation.BctMethodInfo;
import tech.tongyu.bct.common.util.CollectionUtils;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.workflow.dto.ConditionBusinessDTO;
import tech.tongyu.bct.workflow.dto.TriggerDTO;
import tech.tongyu.bct.workflow.exceptions.ReturnMessageAndTemplateDef;
import tech.tongyu.bct.workflow.exceptions.WorkflowCommonException;
import tech.tongyu.bct.workflow.process.enums.OperationEnum;
import tech.tongyu.bct.workflow.process.service.TriggerService;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static tech.tongyu.bct.auth.enums.ResourcePermissionTypeEnum.*;
import static tech.tongyu.bct.auth.enums.ResourcePermissionTypeEnum.BIND_PROCESS_TRIGGER;
import static tech.tongyu.bct.auth.enums.ResourceTypeEnum.*;
import static tech.tongyu.bct.auth.enums.ResourceTypeEnum.PROCESS_DEFINITION_INFO;

/**
 * @author yongbin
 * - mailto: wuyongbin@tongyu.tech
 */
@Component
public class TriggerBusinessApi {

    private TriggerService triggerService;
    private ResourcePermissionAuthAction resourcePermissionAuthAction;

    @Autowired
    public TriggerBusinessApi(TriggerService triggerService
            , ResourcePermissionAuthAction resourcePermissionAuthAction) {
        this.triggerService = triggerService;
        this.resourcePermissionAuthAction = resourcePermissionAuthAction;
    }

    @BctMethodInfo
    @Transactional(rollbackFor = Exception.class)
    public Collection<TriggerDTO> wkProcessTriggerBusinessCreate(
            @BctMethodArg(name = "流程定义名称") String processName,
            @BctMethodArg(name = "触发器名称") String triggerName,
            @BctMethodArg(name = "触发器描述", required = false) String description,
            @BctMethodArg(name = "条件运算类型") String operation,
            @BctMethodArg(name = "条件列表") List<Map<String, Object>> conditions) {
        if (StringUtils.isBlank(triggerName)
                || StringUtils.isBlank(operation)) {
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.PARAM_NOT_FOUND);
        }

        if(!resourcePermissionAuthAction
                .hasResourcePermissionForCurrentUser(
                        Lists.newArrayList(TRIGGER.getAlias())
                        , TRIGGER
                        , CREATE_TRIGGER).get(0)){
            throw new AuthorizationException(ResourceTypeEnum.TRIGGER, triggerName, CREATE_TRIGGER);
        }
        if(!resourcePermissionAuthAction
                .hasResourcePermissionForCurrentUser(
                        Lists.newArrayList(processName)
                        , PROCESS_DEFINITION_INFO
                        , BIND_PROCESS_TRIGGER).get(0)){
            throw new AuthorizationException(PROCESS_DEFINITION_INFO, processName, BIND_PROCESS_TRIGGER);
        }

        TriggerDTO trigger = triggerService.createTrigger(triggerName, description, OperationEnum.valueOf(operation));

        if (CollectionUtils.isNotEmpty(conditions)) {
            List<ConditionBusinessDTO> conditionDTOs = conditions.stream()
                    .map(condition -> JsonUtils.mapper.convertValue(condition, ConditionBusinessDTO.class))
                    .collect(Collectors.toList());
            Collection<String> conditionIds = triggerService.createCondition(conditionDTOs);
            triggerService.bindTriggerCondition(trigger.getTriggerId(), conditionIds);
        }

        triggerService.bindProcessTrigger(processName, trigger.getTriggerId());

        return triggerService.listTrigger();
    }

    @BctMethodInfo
    @Transactional(rollbackFor = Exception.class)
    public Collection<TriggerDTO> wkProcessTriggerBusinessModify(
            @BctMethodArg(name = "触发器ID") String triggerId,
            @BctMethodArg(name = "触发器名称") String triggerName,
            @BctMethodArg(name = "触发器描述", required = false) String description,
            @BctMethodArg(name = "条件运算类型") String operation,
            @BctMethodArg(name = "条件列表") List<Map<String, Object>> conditions) {
        if (StringUtils.isBlank(triggerName)
                || StringUtils.isBlank(operation)) {
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.PARAM_NOT_FOUND);
        }
        TriggerDTO trigger = triggerService.getTrigger(triggerId);
        if(!resourcePermissionAuthAction
                .hasResourcePermissionForCurrentUser(
                        Lists.newArrayList(trigger.getTriggerName())
                        , TRIGGER_INFO
                        , UPDATE_TRIGGER).get(0)){
            throw new AuthorizationException(TRIGGER_INFO, triggerName, UPDATE_TRIGGER);
        }

        triggerService.modifyTrigger(triggerId, triggerName, description, OperationEnum.valueOf(operation));

        if (CollectionUtils.isNotEmpty(conditions)) {
            triggerService.deleteConditionByTriggerId(triggerId);

            List<ConditionBusinessDTO> conditionDTOs = conditions.stream()
                    .map(condition -> JsonUtils.mapper.convertValue(condition, ConditionBusinessDTO.class))
                    .collect(Collectors.toList());
            Collection<String> conditionIds = triggerService.createCondition(conditionDTOs);
            triggerService.bindTriggerCondition(triggerId, conditionIds);
        }

        return triggerService.listTrigger();
    }
}

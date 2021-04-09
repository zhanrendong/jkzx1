package tech.tongyu.bct.workflow.process.api;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.tongyu.bct.auth.authaction.intel.ResourcePermissionAuthAction;
import tech.tongyu.bct.auth.exception.AuthorizationException;
import tech.tongyu.bct.common.api.annotation.BctMethodArg;
import tech.tongyu.bct.common.api.annotation.BctMethodInfo;
import tech.tongyu.bct.common.util.CollectionUtils;
import tech.tongyu.bct.workflow.dto.ConditionDTO;
import tech.tongyu.bct.workflow.dto.IndexDTO;
import tech.tongyu.bct.workflow.dto.ProcessDTO;
import tech.tongyu.bct.workflow.dto.TriggerDTO;
import tech.tongyu.bct.workflow.exceptions.ReturnMessageAndTemplateDef;
import tech.tongyu.bct.workflow.exceptions.WorkflowCommonException;
import tech.tongyu.bct.workflow.process.enums.OperationEnum;
import tech.tongyu.bct.workflow.process.enums.SymbolEnum;
import tech.tongyu.bct.workflow.process.service.ProcessService;
import tech.tongyu.bct.workflow.process.service.TriggerService;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static tech.tongyu.bct.auth.enums.ResourcePermissionTypeEnum.BIND_PROCESS_TRIGGER;
import static tech.tongyu.bct.auth.enums.ResourcePermissionTypeEnum.DELETE_TRIGGER;
import static tech.tongyu.bct.auth.enums.ResourceTypeEnum.PROCESS_DEFINITION_INFO;
import static tech.tongyu.bct.auth.enums.ResourceTypeEnum.TRIGGER_INFO;

/**
 * @author yongbin
 * - mailto: wuyongbin@tongyu.tech
 */

@Component
public class TriggerApi {

    private TriggerService triggerService;
    private ProcessService processService;
    private ResourcePermissionAuthAction resourcePermissionAuthAction;

    @Autowired
    public TriggerApi(TriggerService triggerService
            , ProcessService processService
            , ResourcePermissionAuthAction resourcePermissionAuthAction) {
        this.triggerService = triggerService;
        this.processService = processService;
        this.resourcePermissionAuthAction = resourcePermissionAuthAction;
    }

    @BctMethodInfo
    public Collection<TriggerDTO> wkProcessTriggerCreate(
            @BctMethodArg(name = "触发器名称") String triggerName,
            @BctMethodArg(name = "触发器描述", required = false) String description,
            @BctMethodArg(name = "条件运算类型") String operation) {
        if (StringUtils.isBlank(triggerName)
                || StringUtils.isBlank(operation)) {
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.PARAM_NOT_FOUND);
        }
        triggerService.createTrigger(triggerName, description, OperationEnum.valueOf(operation));

        return triggerService.listTrigger();
    }

    @BctMethodInfo
    public Collection<ConditionDTO> wkTriggerConditionCreate(
            @BctMethodArg(name = "左侧指标") String leftIndex,
            @BctMethodArg(name = "右侧指标") String rightIndex,
            @BctMethodArg(name = "条件描述", required = false) String description,
            @BctMethodArg(name = "指标运算类型") String symbol,
            @BctMethodArg(name = "左侧指标参数") Map<String, Object> leftValue,
            @BctMethodArg(name = "右侧指标参数") Map<String, Object> rightValue) {
        if (StringUtils.isBlank(leftIndex)
                || StringUtils.isBlank(rightIndex)
                || StringUtils.isBlank(symbol)) {
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.PARAM_NOT_FOUND);
        }
        triggerService.createCondition(leftIndex, rightIndex, description, SymbolEnum.valueOf(symbol), leftValue, rightValue);

        return triggerService.listCondition();
    }

    @BctMethodInfo
    public Collection<TriggerDTO> wkProcessTriggerModify(
            @BctMethodArg(name = "触发器ID") String triggerId,
            @BctMethodArg(name = "触发器名称") String triggerName,
            @BctMethodArg(name = "触发器描述", required = false) String description,
            @BctMethodArg(name = "条件运算类型") String operation) {
        if (StringUtils.isBlank(triggerId)
                || StringUtils.isBlank(triggerName)
                || StringUtils.isBlank(operation)) {
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.PARAM_NOT_FOUND);
        }
        triggerService.modifyTrigger(triggerId, triggerName, description, OperationEnum.valueOf(operation));

        return triggerService.listTrigger();
    }

    @BctMethodInfo
    public Collection<ConditionDTO> wkTriggerConditionModify(
            @BctMethodArg(name = "条件ID") String conditionId,
            @BctMethodArg(name = "左侧指标") String leftIndex,
            @BctMethodArg(name = "右侧指标") String rightIndex,
            @BctMethodArg(name = "条件描述", required = false) String description,
            @BctMethodArg(name = "指标运算类型") String symbol,
            @BctMethodArg(name = "左侧指标参数") Map<String, Object> leftValue,
            @BctMethodArg(name = "右侧指标参数") Map<String, Object> rightValue) {
        if (StringUtils.isBlank(conditionId)
                || StringUtils.isBlank(leftIndex)
                || StringUtils.isBlank(rightIndex)
                || StringUtils.isBlank(symbol)) {
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.PARAM_NOT_FOUND);
        }
        triggerService.modifyCondition(conditionId, leftIndex, rightIndex, description, SymbolEnum.valueOf(symbol), leftValue, rightValue);

        return triggerService.listCondition();
    }

    @BctMethodInfo
    public String wkProcessTriggerDelete(
            @BctMethodArg(name = "触发器ID") String triggerId) {
        if (StringUtils.isBlank(triggerId)) {
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.PARAM_NOT_FOUND);
        }
        TriggerDTO trigger = triggerService.getTrigger(triggerId);
        if(!resourcePermissionAuthAction
                .hasResourcePermissionForCurrentUser(
                        Lists.newArrayList(trigger.getTriggerName())
                        , TRIGGER_INFO
                        , DELETE_TRIGGER).get(0)){

            throw new AuthorizationException(TRIGGER_INFO, trigger.getTriggerName(), DELETE_TRIGGER);
        }

        triggerService.deleteTrigger(triggerId);
        return "success";
    }

    @BctMethodInfo
    public String wkTriggerConditionDelete(
            @BctMethodArg(name = "条件ID") String conditionId) {
        if (StringUtils.isBlank(conditionId)) {
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.PARAM_NOT_FOUND);
        }
        triggerService.deleteCondition(conditionId);
        return "success";
    }

    @BctMethodInfo
    public Collection<TriggerDTO> wkProcessTriggerList() {
        return triggerService.listTrigger();
    }

    @BctMethodInfo
    public Collection<ConditionDTO> wkTriggerConditionList() {
        return triggerService.listCondition();
    }

    @BctMethodInfo
    public Collection<IndexDTO> wkIndexList() {
        return triggerService.listIndex();
    }

    @BctMethodInfo
    public Collection<TriggerDTO> wkProcessTriggerByProcessName(
            @BctMethodArg(name = "流程定义名称") String processName) {
        return triggerService.listTrigger(processName);
    }

    @BctMethodInfo
    public ProcessDTO wkProcessTriggerBind(
            @BctMethodArg(name = "流程定义名称") String processName,
            @BctMethodArg(name = "触发器ID列表") String triggerId) {
        if (StringUtils.isBlank(processName)
                || StringUtils.isBlank(triggerId)) {
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.PARAM_NOT_FOUND);
        }

        if(!resourcePermissionAuthAction
                .hasResourcePermissionForCurrentUser(
                        Lists.newArrayList(processName)
                        , PROCESS_DEFINITION_INFO
                        , BIND_PROCESS_TRIGGER).get(0)){
            throw new AuthorizationException(PROCESS_DEFINITION_INFO, processName, BIND_PROCESS_TRIGGER);
        }

        triggerService.bindProcessTrigger(processName, triggerId);
        return ProcessDTO.of(processService.getProcessByProcessName(processName));
    }

    @BctMethodInfo
    public ProcessDTO wkProcessTriggerUnbind(
            @BctMethodArg(name = "流程定义名称") String processName) {
        if (StringUtils.isBlank(processName)) {
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.PARAM_NOT_FOUND);
        }

        if(!resourcePermissionAuthAction
                .hasResourcePermissionForCurrentUser(
                        Lists.newArrayList(processName)
                        , PROCESS_DEFINITION_INFO
                        , BIND_PROCESS_TRIGGER).get(0)){
            throw new AuthorizationException(PROCESS_DEFINITION_INFO, processName, BIND_PROCESS_TRIGGER);
        }

        List<String> triggerIds = triggerService.listTrigger(processName).stream().map(triggerDTO -> {
            TriggerDTO trigger = triggerService.getTrigger(triggerDTO.getTriggerId());
            if (!resourcePermissionAuthAction
                    .hasResourcePermissionForCurrentUser(
                            Lists.newArrayList(trigger.getTriggerName())
                            , TRIGGER_INFO
                            , DELETE_TRIGGER).get(0)) {

                throw new AuthorizationException(TRIGGER_INFO, trigger.getTriggerName(), DELETE_TRIGGER);
            }
            return trigger.getTriggerId();
        }).collect(Collectors.toList());

        triggerService.unbindProcessTrigger(processName);

        triggerIds.forEach(triggerId -> triggerService.deleteTrigger(triggerId));
        return ProcessDTO.of(processService.getProcessByProcessName(processName));
    }

    @BctMethodInfo
    public TriggerDTO wkTriggerConditionBind(
            @BctMethodArg(name = "触发器ID列表") String triggerId,
            @BctMethodArg(name = "条件ID列表") Collection<String> conditionIds) {
        if (StringUtils.isBlank(triggerId)
                || CollectionUtils.isEmpty(conditionIds)) {
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.PARAM_NOT_FOUND);
        }
        triggerService.bindTriggerCondition(triggerId, conditionIds);
        return triggerService.getTrigger(triggerId);
    }

    @BctMethodInfo
    public Boolean wkTriggeredProcessValid(
            @BctMethodArg(name = "流程定义名称") String processName,
            @BctMethodArg(name = "触发数据") Map<String, Object> data){
        if (StringUtils.isBlank(processName)) {
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.PARAM_NOT_FOUND);
        }
        return triggerService.validProcessTrigger(processName, data);
    }
}
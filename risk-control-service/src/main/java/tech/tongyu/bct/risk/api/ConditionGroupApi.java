package tech.tongyu.bct.risk.api;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.tongyu.bct.common.api.annotation.BctMethodArg;
import tech.tongyu.bct.common.api.annotation.BctMethodInfo;
import tech.tongyu.bct.common.util.CollectionUtils;
import tech.tongyu.bct.risk.dto.*;
import tech.tongyu.bct.risk.excepitons.ReturnMessageAndTemplateDef;
import tech.tongyu.bct.risk.excepitons.RiskControlException;
import tech.tongyu.bct.risk.service.ConditionGroupService;

import java.util.Collection;
import java.util.Map;

/**
 * @author yongbin
 * - mailto: wuyongbin@tongyu.tech
 */
@Component
public class ConditionGroupApi {

    private ConditionGroupService conditionGroupService;

    @Autowired
    public ConditionGroupApi(ConditionGroupService conditionGroupService) {
        this.conditionGroupService = conditionGroupService;
    }

    @BctMethodInfo
    public Collection<ConditionGroupDTO> wkConditionGroupCreate(
            @BctMethodArg(name = "条件组名称") String conditionGroupName,
            @BctMethodArg(name = "条件组描述", required = false) String description,
            @BctMethodArg(name = "条件运算类型") String operation) {
        if (StringUtils.isBlank(conditionGroupName)
                || StringUtils.isBlank(operation)) {
            throw new RiskControlException(ReturnMessageAndTemplateDef.Errors.PARAM_NOT_FOUND);
        }
        conditionGroupService.createConditionGroup(conditionGroupName, description, OperationEnum.valueOf(operation));

        return conditionGroupService.listConditionGroup();
    }

    @BctMethodInfo
    public Collection<ConditionDTO> wkConditionCreate(
            @BctMethodArg(name = "左侧指标") String leftIndex,
            @BctMethodArg(name = "右侧指标") String rightIndex,
            @BctMethodArg(name = "条件描述", required = false) String description,
            @BctMethodArg(name = "指标运算类型") String symbol,
            @BctMethodArg(name = "左侧指标参数") Map<String, Object> leftValue,
            @BctMethodArg(name = "右侧指标参数") Map<String, Object> rightValue) {
        if (StringUtils.isBlank(leftIndex)
                || StringUtils.isBlank(rightIndex)
                || StringUtils.isBlank(symbol)) {
            throw new RiskControlException(ReturnMessageAndTemplateDef.Errors.PARAM_NOT_FOUND);
        }
        conditionGroupService.createCondition(leftIndex, rightIndex, description, SymbolEnum.valueOf(symbol), leftValue, rightValue);

        return conditionGroupService.listCondition();
    }

    @BctMethodInfo
    public Collection<ConditionGroupDTO> wkConditionGroupModify(
            @BctMethodArg(name = "条件组ID") String conditionGroupId,
            @BctMethodArg(name = "条件组名称") String conditionGroupName,
            @BctMethodArg(name = "条件组描述", required = false) String description,
            @BctMethodArg(name = "条件运算类型") String operation) {
        if (StringUtils.isBlank(conditionGroupId)
                || StringUtils.isBlank(conditionGroupName)
                || StringUtils.isBlank(operation)) {
            throw new RiskControlException(ReturnMessageAndTemplateDef.Errors.PARAM_NOT_FOUND);
        }
        conditionGroupService.modifyConditionGroup(conditionGroupId, conditionGroupName, description, OperationEnum.valueOf(operation));

        return conditionGroupService.listConditionGroup();
    }

    @BctMethodInfo
    public Collection<ConditionDTO> wkConditionModify(
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
            throw new RiskControlException(ReturnMessageAndTemplateDef.Errors.PARAM_NOT_FOUND);
        }
        conditionGroupService.modifyCondition(conditionId, leftIndex, rightIndex, description, SymbolEnum.valueOf(symbol), leftValue, rightValue);

        return conditionGroupService.listCondition();
    }

    @BctMethodInfo
    public String wkConditionGroupDelete(
            @BctMethodArg(name = "条件组ID") String conditionGroupId) {
        if (StringUtils.isBlank(conditionGroupId)) {
            throw new RiskControlException(ReturnMessageAndTemplateDef.Errors.PARAM_NOT_FOUND);
        }

        conditionGroupService.deleteConditionGroup(conditionGroupId);
        return "success";
    }

    @BctMethodInfo
    public String wkConditionDelete(
            @BctMethodArg(name = "条件ID") String conditionId) {
        if (StringUtils.isBlank(conditionId)) {
            throw new RiskControlException(ReturnMessageAndTemplateDef.Errors.PARAM_NOT_FOUND);
        }
        conditionGroupService.deleteCondition(conditionId);
        return "success";
    }

    @BctMethodInfo
    public Collection<ConditionDTO> wkConditionList() {
        return conditionGroupService.listCondition();
    }

    @BctMethodInfo
    public Collection<IndexDTO> wkIndexList() {
        return conditionGroupService.listIndex();
    }

    @BctMethodInfo
    public ConditionGroupDTO wkConditionGroupBind(
            @BctMethodArg(name = "条件组ID列表") String conditionGroupId,
            @BctMethodArg(name = "条件ID列表") Collection<String> conditionIds) {
        if (StringUtils.isBlank(conditionGroupId)
                || CollectionUtils.isEmpty(conditionIds)) {
            throw new RiskControlException(ReturnMessageAndTemplateDef.Errors.PARAM_NOT_FOUND);
        }
        conditionGroupService.bindConditionGroupAndCondition(conditionGroupId, conditionIds);
        return conditionGroupService.getConditionGroup(conditionGroupId);
    }

    @BctMethodInfo
    public Boolean wkCanTriggeredValid(
            @BctMethodArg(name = "条件组ID") String conditionGroupId,
            @BctMethodArg(name = "触发数据") Map<String, Object> data){
        if (StringUtils.isBlank(conditionGroupId)) {
            throw new RiskControlException(ReturnMessageAndTemplateDef.Errors.PARAM_NOT_FOUND);
        }
        return conditionGroupService.validCanTriggered(conditionGroupId, data);
    }
}
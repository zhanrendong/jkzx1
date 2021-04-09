package tech.tongyu.bct.risk.api;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.common.api.annotation.BctMethodArg;
import tech.tongyu.bct.common.api.annotation.BctMethodInfo;
import tech.tongyu.bct.common.util.CollectionUtils;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.risk.dto.ConditionBusinessDTO;
import tech.tongyu.bct.risk.dto.ConditionGroupDTO;
import tech.tongyu.bct.risk.dto.OperationEnum;
import tech.tongyu.bct.risk.excepitons.ReturnMessageAndTemplateDef;
import tech.tongyu.bct.risk.excepitons.RiskControlException;
import tech.tongyu.bct.risk.service.ConditionGroupService;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author yongbin
 * - mailto: wuyongbin@tongyu.tech
 */
@Component
public class ConditionGroupBusinessApi {

    private ConditionGroupService conditionGroupService;

    @Autowired
    public ConditionGroupBusinessApi(ConditionGroupService conditionGroupService) {
        this.conditionGroupService = conditionGroupService;
    }

    @BctMethodInfo
    @Transactional(rollbackFor = Exception.class)
    public Collection<ConditionGroupDTO> wkConditionGroupBusinessCreate(
            @BctMethodArg(name = "条件组名称") String conditionGroupName,
            @BctMethodArg(name = "条件组描述", required = false) String description,
            @BctMethodArg(name = "条件运算类型") String operation,
            @BctMethodArg(name = "条件列表") List<Map<String, Object>> conditions) {
        if (StringUtils.isBlank(conditionGroupName)
                || StringUtils.isBlank(operation)) {
            throw new RiskControlException(ReturnMessageAndTemplateDef.Errors.PARAM_NOT_FOUND);
        }
        ConditionGroupDTO conditionGroupDTO = conditionGroupService.createConditionGroup(conditionGroupName, description, OperationEnum.valueOf(operation));

        if (CollectionUtils.isNotEmpty(conditions)) {
            List<ConditionBusinessDTO> conditionDTOs = conditions.stream()
                    .map(condition -> JsonUtils.mapper.convertValue(condition, ConditionBusinessDTO.class))
                    .collect(Collectors.toList());
            Collection<String> conditionIds = conditionGroupService.createCondition(conditionDTOs);
            conditionGroupService.bindConditionGroupAndCondition(conditionGroupDTO.getConditionGroupId(), conditionIds);
        }

        return conditionGroupService.listConditionGroup();
    }

    @BctMethodInfo
    @Transactional(rollbackFor = Exception.class)
    public Collection<ConditionGroupDTO> wkConditionGroupBusinessModify(
            @BctMethodArg(name = "条件组ID") String conditionGroupId,
            @BctMethodArg(name = "条件组名称") String conditionGroupName,
            @BctMethodArg(name = "条件组描述", required = false) String description,
            @BctMethodArg(name = "条件运算类型") String operation,
            @BctMethodArg(name = "条件列表") List<Map<String, Object>> conditions) {
        if (StringUtils.isBlank(conditionGroupName)
                || StringUtils.isBlank(operation)) {
            throw new RiskControlException(ReturnMessageAndTemplateDef.Errors.PARAM_NOT_FOUND);
        }

        conditionGroupService.modifyConditionGroup(conditionGroupId, conditionGroupName, description, OperationEnum.valueOf(operation));

        if (CollectionUtils.isNotEmpty(conditions)) {
            conditionGroupService.deleteConditionByConditionGroupId(conditionGroupId);

            List<ConditionBusinessDTO> conditionDTOs = conditions.stream()
                    .map(condition -> JsonUtils.mapper.convertValue(condition, ConditionBusinessDTO.class))
                    .collect(Collectors.toList());
            Collection<String> conditionIds = conditionGroupService.createCondition(conditionDTOs);
            conditionGroupService.bindConditionGroupAndCondition(conditionGroupId, conditionIds);
        }

        return conditionGroupService.listConditionGroup();
    }
}

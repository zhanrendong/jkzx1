package tech.tongyu.bct.risk.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.auth.dto.Resource;
import tech.tongyu.bct.common.util.CollectionUtils;
import tech.tongyu.bct.risk.dto.*;
import tech.tongyu.bct.risk.manager.ConditionManager;
import tech.tongyu.bct.risk.manager.IndexManager;
import tech.tongyu.bct.risk.manager.ConditionGroupManager;
import tech.tongyu.bct.risk.service.ConditionGroupService;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author yongbin
 * - mailto: wuyongbin@tongyu.tech
 */

@Service
public class ConditionGroupServiceImpl implements ConditionGroupService {

    private ConditionGroupManager conditionGroupManager;
    private IndexManager indexManager;
    private ConditionManager conditionManager;

    @Autowired
    public ConditionGroupServiceImpl(ConditionGroupManager conditionGroupManager
            , IndexManager indexManager
            , ConditionManager conditionManager) {
        this.conditionGroupManager = conditionGroupManager;
        this.indexManager = indexManager;
        this.conditionManager = conditionManager;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConditionGroupDTO createConditionGroup(String conditionGroupName, String description, OperationEnum operation) {
        return conditionGroupManager.createConditionGroup(conditionGroupName, description, operation);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void modifyConditionGroup(String conditionGroupId, String conditionGroupName, String description, OperationEnum operation) {
        conditionGroupManager.modifyConditionGroup(conditionGroupId, conditionGroupName, description, operation);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteConditionGroup(String conditionGroupId) {
        conditionGroupManager.deleteConditionGroup(conditionGroupId);
    }

    @Override
    public Collection<ConditionGroupDTO> listConditionGroup() {
        return conditionGroupManager.listConditionGroup();
    }

    @Override
    public ConditionGroupDTO getConditionGroup(String conditionGroupId) {
        return conditionGroupManager.getConditionGroup(conditionGroupId);
    }

    @Override
    public Collection<IndexDTO> listIndex() {
        return indexManager.listIndex();
    }

    @Override
    public ConditionDTO createCondition(String leftIndex, String rightIndex, String description, SymbolEnum symbolEnum, Map<String,Object> leftValue, Map<String,Object> rightValue) {
        return conditionManager.createCondition(leftIndex, rightIndex, description, symbolEnum, leftValue, rightValue);
    }

    @Override
    public Collection<String> createCondition(Collection<ConditionBusinessDTO> conditions) {
        return conditionManager.createCondition(conditions);
    }

    @Override
    public void modifyCondition(String conditionId, String leftIndex, String rightIndex, String description, SymbolEnum symbolEnum, Map<String,Object> leftValue, Map<String,Object> rightValue) {
        conditionManager.modifyCondition(conditionId, leftIndex, rightIndex, description, symbolEnum, leftValue, rightValue);
    }

    @Override
    public void deleteCondition(String conditionId) {
        conditionManager.deleteCondition(conditionId);
    }

    @Override
    public void deleteConditionByConditionGroupId(String conditionGroupId) {
        conditionManager.deleteConditionByConditionGroupId(conditionGroupId);
    }

    @Override
    public Collection<ConditionDTO> listCondition() {
        return conditionManager.listCondition();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindConditionGroupAndCondition(String conditionGroupId, Collection<String> conditionIds) {
        getConditionGroup(conditionGroupId);
        conditionIds.forEach(conditionId ->
                conditionGroupManager.bindGroupAndCondition(conditionGroupId, conditionId)
        );
    }

    @Override
    public Boolean validCanTriggered(String conditionGroupId, Map<String, Object> data) {
        ConditionGroupDTO conditionGroup = conditionGroupManager.getConditionGroup(conditionGroupId);
        Collection<ConditionDTO> conditions = conditionGroup.getConditions();
        List<Boolean> calculation = conditions
                    .stream()
                    .map(conditionDTO -> conditionManager.testCondition(conditionDTO, data))
                    .collect(Collectors.toList());
        if (conditionGroup.getOperation().equals(OperationEnum.AND)) {
            return calculation.stream().allMatch(t -> t);
        } else {
            return calculation.stream().anyMatch(t -> t);
        }
    }
}
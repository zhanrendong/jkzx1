package tech.tongyu.bct.risk.manager;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.common.util.CollectionUtils;
import tech.tongyu.bct.risk.dto.ConditionGroupDTO;
import tech.tongyu.bct.risk.dto.OperationEnum;
import tech.tongyu.bct.risk.excepitons.ReturnMessageAndTemplateDef;
import tech.tongyu.bct.risk.excepitons.RiskControlException;
import tech.tongyu.bct.risk.repo.ConditionGroupRepo;
import tech.tongyu.bct.risk.repo.ConditionRepo;
import tech.tongyu.bct.risk.repo.GroupAndConditionRepo;
import tech.tongyu.bct.risk.repo.entities.ConditionDbo;
import tech.tongyu.bct.risk.repo.entities.ConditionGroupDbo;
import tech.tongyu.bct.risk.repo.entities.GroupAndConditionDbo;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yongbin
 * - mailto: wuyongbin@tongyu.tech
 */
@Component
public class ConditionGroupManager {

    private ConditionGroupRepo conditionGroupRepo;
    private GroupAndConditionRepo groupAndConditionRepo;
    private ConditionRepo conditionRepo;
    private ConditionManager conditionManager;

    @Autowired
    public ConditionGroupManager(ConditionGroupRepo conditionGroupRepo
            , GroupAndConditionRepo groupAndConditionRepo
            , ConditionRepo conditionRepo
            , ConditionManager conditionManager) {
        this.conditionGroupRepo = conditionGroupRepo;
        this.groupAndConditionRepo = groupAndConditionRepo;
        this.conditionRepo = conditionRepo;
        this.conditionManager = conditionManager;
    }

    @Transactional(rollbackFor = Exception.class)
    public ConditionGroupDTO createConditionGroup(String conditionGroupName, String description, OperationEnum operation) {
        Optional<ConditionGroupDbo> conditionGroupDbo = conditionGroupRepo.findValidConditionGroupDboByConditionGroupName(conditionGroupName);

        if (conditionGroupDbo.isPresent()){
            throw new RiskControlException(ReturnMessageAndTemplateDef.Errors.GROUP_EXIST_ERROR, conditionGroupName);
        }

        return toConditionGroupDTO(conditionGroupRepo.saveAndFlush(new ConditionGroupDbo(conditionGroupName, description, operation)), null);
    }

    @Transactional(rollbackFor = Exception.class)
    public void modifyConditionGroup(String conditionGroupId, String conditionGroupName, String description, OperationEnum operation) {
        ConditionGroupDbo conditionGroupDbo = conditionGroupRepo.findValidConditionGroupDboByConditionGroupId(conditionGroupId)
                .orElseThrow(() -> new RiskControlException(ReturnMessageAndTemplateDef.Errors.GROUP_ERROR));
        Optional<ConditionGroupDbo> conditionGroup = conditionGroupRepo.findValidConditionGroupDboByConditionGroupName(conditionGroupName);
        if (conditionGroup.isPresent()
                && !conditionGroup.get().getId().equals(conditionGroupId)
                && conditionGroup.get().getConditionGroupName().equals(conditionGroupName)){
            throw new RiskControlException(ReturnMessageAndTemplateDef.Errors.GROUP_EXIST_ERROR, conditionGroupName);
        }
        conditionGroupDbo.setConditionGroupName(conditionGroupName);
        conditionGroupDbo.setDescription(description);
        conditionGroupDbo.setOperation(operation);
        conditionGroupRepo.saveAndFlush(conditionGroupDbo);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteConditionGroup(String conditionGroupId) {
        groupAndConditionRepo.deleteValidGroupAndConditionDboByConditionGroupId(conditionGroupId);
        conditionGroupRepo.deleteValidConditionGroupDboByConditionGroupId(conditionGroupId);
    }

    @Transactional(rollbackFor = Exception.class, readOnly = true)
    public Collection<ConditionGroupDTO> listConditionGroup(){
        Collection<ConditionGroupDbo> conditionGroupDboList = conditionGroupRepo.findValidConditionGroupDbo();
        if (CollectionUtils.isEmpty(conditionGroupDboList)) {
            return Lists.newArrayList();
        }
        Map<String, List<ConditionGroupDbo>> conditionGroupMap = conditionGroupDboList
                .stream()
                .collect(Collectors.groupingBy(ConditionGroupDbo::getId));
        Collection<ConditionDbo> conditionList = conditionRepo.findValidConditionDbo();
        Collection<GroupAndConditionDbo> groupAndConditionDbos = groupAndConditionRepo.findValidGroupAndConditionDboByConditionGroupId(conditionGroupMap.keySet());
        if (CollectionUtils.isEmpty(groupAndConditionDbos)) {
            return conditionGroupDboList.stream()
                    .map(conditionGroupDbo -> toConditionGroupDTO(conditionGroupDbo,null))
                    .collect(Collectors.toList());
        }
        Map<String, List<GroupAndConditionDbo>> groupMap = groupAndConditionDbos
                .stream()
                .collect(Collectors.groupingBy(GroupAndConditionDbo::getConditionGroupId));
        return conditionGroupDboList.stream().map(conditionGroupDbo -> {
            List<GroupAndConditionDbo> groupAndConditionDboList = groupMap.get(conditionGroupDbo.getId());
            return getConditionGroupDTO(conditionGroupDbo, groupAndConditionDboList, conditionList);
        }).collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public ConditionGroupDTO getConditionGroup(String conditionGroupId){
        ConditionGroupDbo conditionGroupDbo = conditionGroupRepo.findValidConditionGroupDboByConditionGroupId(conditionGroupId)
                .orElseThrow(() -> new RiskControlException(ReturnMessageAndTemplateDef.Errors.GROUP_ERROR));
        Set<String> conditionIds = groupAndConditionRepo.findValidGroupAndConditionDboByConditionGroupId(Lists.newArrayList(conditionGroupDbo.getId()))
                .stream()
                .map(GroupAndConditionDbo::getConditionId).collect(Collectors.toSet());

        if (CollectionUtils.isNotEmpty(conditionIds)) {
            Collection<ConditionDbo> conditionDbos = conditionRepo.findValidConditionDboByConditionId(conditionIds);
            return toConditionGroupDTO(conditionGroupDbo, conditionDbos);
        } else {
            return toConditionGroupDTO(conditionGroupDbo, null);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void bindGroupAndCondition(String conditionGroupId, String conditionId){
        conditionRepo.findValidConditionDboByConditionId(conditionId)
                .orElseThrow(() -> new RiskControlException(ReturnMessageAndTemplateDef.Errors.GROUP_ERROR));
        groupAndConditionRepo.saveAndFlush(new GroupAndConditionDbo(conditionGroupId,conditionId));
    }

    private ConditionGroupDTO toConditionGroupDTO(ConditionGroupDbo conditionGroupDbo, Collection<ConditionDbo> conditionDbos){
        ConditionGroupDTO conditionGroupDTO = new ConditionGroupDTO(
                conditionGroupDbo.getId()
                , conditionGroupDbo.getConditionGroupName()
                , conditionGroupDbo.getDescription()
                , conditionGroupDbo.getOperation()
        );
        if (CollectionUtils.isNotEmpty(conditionDbos)){
            conditionGroupDTO.setConditions(conditionManager.toConditionDTO(conditionDbos));
        }
        return conditionGroupDTO;
    }

    private ConditionGroupDTO getConditionGroupDTO(ConditionGroupDbo conditionGroupDbo, List<GroupAndConditionDbo> groupAndConditionDbos, Collection<ConditionDbo> conditionList){
        if (CollectionUtils.isNotEmpty(groupAndConditionDbos)) {
            Set<String> conditionIds = groupAndConditionDbos
                    .stream()
                    .map(GroupAndConditionDbo::getConditionId)
                    .collect(Collectors.toSet());
            List<ConditionDbo> conditionDbos = conditionList
                    .stream()
                    .filter(conditionDbo -> conditionIds.contains(conditionDbo.getId()))
                    .collect(Collectors.toList());
            return toConditionGroupDTO(conditionGroupDbo, conditionDbos);
        } else {
            return toConditionGroupDTO(conditionGroupDbo, null);
        }
    }
}

package tech.tongyu.bct.workflow.process.manager;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.common.util.CollectionUtils;
import tech.tongyu.bct.workflow.dto.TriggerDTO;
import tech.tongyu.bct.workflow.exceptions.ReturnMessageAndTemplateDef;
import tech.tongyu.bct.workflow.exceptions.WorkflowCommonException;
import tech.tongyu.bct.workflow.process.enums.OperationEnum;
import tech.tongyu.bct.workflow.process.repo.*;
import tech.tongyu.bct.workflow.process.repo.entities.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yongbin
 * - mailto: wuyongbin@tongyu.tech
 */
@Component
public class TriggerManager {

    private TriggerRepo triggerRepo;
    private TriggerConditionRepo triggerConditionRepo;
    private ConditionRepo conditionRepo;
    private ProcessTriggerRepo processTriggerRepo;
    private ConditionManager conditionManager;
    private ProcessRepo processRepo;

    @Autowired
    public TriggerManager(TriggerRepo triggerRepo
            , TriggerConditionRepo triggerConditionRepo
            , ConditionRepo conditionRepo
            , ProcessTriggerRepo processTriggerRepo
            , ConditionManager conditionManager
            , ProcessRepo processRepo) {
        this.triggerRepo = triggerRepo;
        this.triggerConditionRepo = triggerConditionRepo;
        this.conditionRepo = conditionRepo;
        this.processTriggerRepo = processTriggerRepo;
        this.conditionManager = conditionManager;
        this.processRepo = processRepo;
    }

    @Transactional(rollbackFor = Exception.class)
    public TriggerDTO createTrigger(String triggerName, String description, OperationEnum operation) {
        Optional<TriggerDbo> triggerDbo = triggerRepo.findValidTriggerDboByTriggerName(triggerName);

        if (triggerDbo.isPresent()){
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.TRIGGER_EXIST_ERROR, triggerName);
        }

        return toTriggerDTO(triggerRepo.saveAndFlush(new TriggerDbo(triggerName, description, operation)), null);
    }

    @Transactional(rollbackFor = Exception.class)
    public void modifyTrigger(String triggerId, String triggerName, String description, OperationEnum operation) {
        TriggerDbo triggerDbo = triggerRepo.findValidTriggerDboByTriggerId(triggerId)
                .orElseThrow(() -> new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.TRIGGER_ERROR));
        Optional<TriggerDbo> trigger = triggerRepo.findValidTriggerDboByTriggerName(triggerName);
        if (trigger.isPresent()
                && !trigger.get().getId().equals(triggerId)
                && trigger.get().getTriggerName().equals(triggerName)){
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.TRIGGER_EXIST_ERROR, triggerName);
        }
        triggerDbo.setTriggerName(triggerName);
        triggerDbo.setDescription(description);
        triggerDbo.setOperation(operation);
        triggerRepo.saveAndFlush(triggerDbo);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteTrigger(String triggerId) {
        Collection<ProcessTriggerDbo> processTriggerDbos = validTriggerBind(triggerId);
        if (CollectionUtils.isNotEmpty(processTriggerDbos)) {
            Set<String> processIds = processTriggerDbos
                    .stream()
                    .map(ProcessTriggerDbo::getProcessId)
                    .collect(Collectors.toSet());
            List<String> processNameList = processRepo.findAllValidProcessByProcessId(processIds)
                    .stream()
                    .map(ProcessDbo::getProcessName)
                    .collect(Collectors.toList());
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.TRIGGER_DELETE_ERROR, processNameList);
        }
        processTriggerRepo.deleteValidProcessTriggerDboByTriggerId(triggerId);
        triggerConditionRepo.deleteValidTriggerConditionDboByTriggerId(triggerId);
        triggerRepo.deleteValidTriggerDboByTriggerId(triggerId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteProcessTriggerBindByProcessId(String processId){
        processTriggerRepo.deleteValidProcessTriggerDboByProcessId(processId);
    }

    @Transactional(rollbackFor = Exception.class, readOnly = true)
    public Collection<TriggerDTO> listTrigger(){
        Collection<TriggerDbo> triggerDboList = triggerRepo.findValidTriggerDbo();
        if (CollectionUtils.isEmpty(triggerDboList)) {
            return Lists.newArrayList();
        }
        Map<String, List<TriggerDbo>> triggerMap = triggerDboList
                .stream()
                .collect(Collectors.groupingBy(TriggerDbo::getId));
        Collection<ConditionDbo> conditionList = conditionRepo.findValidConditionDbo();
        Collection<TriggerConditionDbo> triggerCondition = triggerConditionRepo.findValidTriggerConditionDboByTriggerId(triggerMap.keySet());
        if (CollectionUtils.isEmpty(triggerCondition)) {
            return triggerDboList.stream().map(triggerDbo -> toTriggerDTO(triggerDbo,null)).collect(Collectors.toList());
        }
        Map<String, List<TriggerConditionDbo>> triggerConditionMap = triggerCondition
                .stream()
                .collect(Collectors.groupingBy(TriggerConditionDbo::getTriggerId));
        return triggerDboList.stream().map(triggerDbo -> {
            List<TriggerConditionDbo> triggerConditionDbos = triggerConditionMap.get(triggerDbo.getId());
            return getTriggerDTO(triggerDbo, triggerConditionDbos, conditionList);
        }).collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class, readOnly = true)
    public Collection<TriggerDTO> listTrigger(String processId){
        List<String> triggerIds = processTriggerRepo.findValidProcessTriggerDboByProcessId(processId)
                .stream().map(ProcessTriggerDbo::getTriggerId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(triggerIds)) {
            return Lists.newArrayList();
        }
        Map<String, List<TriggerDbo>> triggerMap = triggerRepo.findValidTriggerDboByTriggerId(triggerIds)
                .stream()
                .collect(Collectors.groupingBy(TriggerDbo::getId));
        Collection<ConditionDbo> conditionList = conditionRepo.findValidConditionDbo();
        Map<String, List<TriggerConditionDbo>> triggerConditionMap = triggerConditionRepo.findValidTriggerConditionDboByTriggerId(triggerMap.keySet())
                .stream()
                .collect(Collectors.groupingBy(TriggerConditionDbo::getTriggerId));

        return triggerIds.stream().map(triggerId -> {
            TriggerDbo triggerDbo = triggerMap.get(triggerId).get(0);
            List<TriggerConditionDbo> triggerConditionDbos = triggerConditionMap.get(triggerId);
            return getTriggerDTO(triggerDbo, triggerConditionDbos, conditionList);
        }).collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public TriggerDTO getTrigger(String triggerId){
        TriggerDbo triggerDbo = triggerRepo.findValidTriggerDboByTriggerId(triggerId)
                .orElseThrow(() -> new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.TRIGGER_ERROR));
        Set<String> conditionIds = triggerConditionRepo.findValidTriggerConditionDboByTriggerId(Lists.newArrayList(triggerDbo.getId()))
                .stream()
                .map(TriggerConditionDbo::getConditionId).collect(Collectors.toSet());

        if (CollectionUtils.isNotEmpty(conditionIds)) {
            Collection<ConditionDbo> conditionDbos = conditionRepo.findValidConditionDboByConditionId(conditionIds);
            return toTriggerDTO(triggerDbo, conditionDbos);
        } else {
            return toTriggerDTO(triggerDbo, null);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void bindTriggerCondition(String triggerId, String conditionId){
        conditionRepo.findValidConditionDboByConditionId(conditionId)
                .orElseThrow(() -> new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.TRIGGER_ERROR));
        triggerConditionRepo.saveAndFlush(new TriggerConditionDbo(triggerId,conditionId));
    }

    @Transactional(rollbackFor = Exception.class)
    public void bindProcessTrigger(String processId, String triggerId){
        triggerRepo.findValidTriggerDboByTriggerId(triggerId)
                .orElseThrow(() -> new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.TRIGGER_ERROR));
        processTriggerRepo.saveAndFlush(new ProcessTriggerDbo(processId, triggerId));
    }

    private TriggerDTO toTriggerDTO(TriggerDbo triggerDbo, Collection<ConditionDbo> conditionDbos){
        TriggerDTO triggerDTO = new TriggerDTO(
                triggerDbo.getId()
                , triggerDbo.getTriggerName()
                , triggerDbo.getDescription()
                , triggerDbo.getOperation()
        );
        if (CollectionUtils.isNotEmpty(conditionDbos)){
            triggerDTO.setConditions(conditionManager.toConditionDTO(conditionDbos));
        }
        return triggerDTO;
    }

    private TriggerDTO getTriggerDTO(TriggerDbo triggerDbo, List<TriggerConditionDbo> triggerConditionDbos, Collection<ConditionDbo> conditionList){
        if (CollectionUtils.isNotEmpty(triggerConditionDbos)) {
            Set<String> conditionIds = triggerConditionDbos
                    .stream()
                    .map(TriggerConditionDbo::getConditionId)
                    .collect(Collectors.toSet());
            List<ConditionDbo> conditionDbos = conditionList
                    .stream()
                    .filter(conditionDbo -> conditionIds.contains(conditionDbo.getId()))
                    .collect(Collectors.toList());
            return toTriggerDTO(triggerDbo, conditionDbos);
        } else {
            return toTriggerDTO(triggerDbo, null);
        }
    }

    private Collection<ProcessTriggerDbo> validTriggerBind(String triggerId){
        return processTriggerRepo.findValidProcessTriggerDboByTriggerId(triggerId);
    }
}

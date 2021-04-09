package tech.tongyu.bct.workflow.process.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.auth.authaction.intel.ResourceAuthAction;
import tech.tongyu.bct.auth.dto.Resource;
import tech.tongyu.bct.auth.dto.ResourceDTO;
import tech.tongyu.bct.auth.dto.UserDTO;
import tech.tongyu.bct.auth.enums.ResourceTypeEnum;
import tech.tongyu.bct.auth.manager.ResourceManager;
import tech.tongyu.bct.auth.manager.ResourcePermissionManager;
import tech.tongyu.bct.auth.manager.UserManager;
import tech.tongyu.bct.common.util.CollectionUtils;
import tech.tongyu.bct.workflow.dto.*;
import tech.tongyu.bct.workflow.exceptions.ReturnMessageAndTemplateDef;
import tech.tongyu.bct.workflow.exceptions.WorkflowCommonException;
import tech.tongyu.bct.workflow.process.enums.OperationEnum;
import tech.tongyu.bct.workflow.process.enums.SymbolEnum;
import tech.tongyu.bct.workflow.process.manager.ConditionManager;
import tech.tongyu.bct.workflow.process.manager.IndexManager;
import tech.tongyu.bct.workflow.process.manager.TriggerManager;
import tech.tongyu.bct.workflow.process.manager.self.ProcessPersistenceManager;
import tech.tongyu.bct.workflow.process.service.TriggerService;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static tech.tongyu.bct.auth.enums.ResourcePermissionTypeEnum.Arrays.WHEN_CREATE_TRIGGER_INFO;
import static tech.tongyu.bct.auth.enums.ResourceTypeEnum.PROCESS_DEFINITION;
import static tech.tongyu.bct.auth.enums.ResourceTypeEnum.TRIGGER;

/**
 * @author yongbin
 * - mailto: wuyongbin@tongyu.tech
 */

@Service
public class TriggerServiceImpl implements TriggerService {

    private TriggerManager triggerManager;
    private IndexManager indexManager;
    private ConditionManager conditionManager;
    private ProcessPersistenceManager processPersistenceManager;
    private ResourceAuthAction resourceAuthAction;
    private ResourceManager resourceManager;
    private ResourcePermissionManager resourcePermissionManager;
    private UserManager userManager;

    @Autowired
    public TriggerServiceImpl(TriggerManager triggerManager
            , IndexManager indexManager
            , ConditionManager conditionManager
            , ProcessPersistenceManager processPersistenceManager
            , ResourceAuthAction resourceAuthAction
            , ResourceManager resourceManager
            , ResourcePermissionManager resourcePermissionManager
            , UserManager userManager) {
        this.triggerManager = triggerManager;
        this.indexManager = indexManager;
        this.conditionManager = conditionManager;
        this.processPersistenceManager = processPersistenceManager;
        this.resourceAuthAction = resourceAuthAction;
        this.resourceManager = resourceManager;
        this.resourcePermissionManager = resourcePermissionManager;
        this.userManager = userManager;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TriggerDTO createTrigger(String triggerName, String description, OperationEnum operation) {
        Resource triggerResource = getTriggerResource();
        ResourceDTO resourceDTO = resourceManager.createResource(
                triggerName
                , ResourceTypeEnum.TRIGGER_INFO
                , triggerResource.getId(), null, 0);

        UserDTO userDTO = userManager.getCurrentUser();
        resourcePermissionManager.createResourcePermissions(
                userDTO.getId()
                , resourceDTO.getId()
                , WHEN_CREATE_TRIGGER_INFO
        );

        return triggerManager.createTrigger(triggerName, description, operation);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void modifyTrigger(String triggerId, String triggerName, String description, OperationEnum operation) {
        TriggerDTO trigger = triggerManager.getTrigger(triggerId);

        if (!Objects.equals(trigger.getTriggerName(), triggerName)){
            Resource resource = getTriggerResource();
            Resource modifyResource = getTriggerByResourceName(resource, trigger.getTriggerName());
            resourceAuthAction.modifyResource(modifyResource.getId(), triggerName);
        }

        triggerManager.modifyTrigger(triggerId, triggerName, description, operation);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTrigger(String triggerId) {
        TriggerDTO trigger = triggerManager.getTrigger(triggerId);
        triggerManager.deleteTrigger(triggerId);

        Resource resource = getTriggerResource();
        Resource deleteResource = getTriggerByResourceName(resource, trigger.getTriggerName());
        resourceManager.deleteResourceByResourceId(deleteResource.getId());
    }

    @Override
    public Collection<TriggerDTO> listTrigger() {
        return triggerManager.listTrigger();
    }

    @Override
    public Collection<TriggerDTO> listTrigger(String processName) {
        ProcessPersistenceDTO persistenceDTO = processPersistenceManager.getProcessPersistenceDTOByProcessName(processName);
        return triggerManager.listTrigger(persistenceDTO.getId());
    }

    @Override
    public TriggerDTO getTrigger(String triggerId) {
        return triggerManager.getTrigger(triggerId);
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
    public void deleteConditionByTriggerId(String triggerId) {
        conditionManager.deleteConditionByTriggerId(triggerId);
    }

    @Override
    public Collection<ConditionDTO> listCondition() {
        return conditionManager.listCondition();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindProcessTrigger(String processName, String triggerId) {
        ProcessPersistenceDTO persistenceDTO = processPersistenceManager.getProcessPersistenceDTOByProcessName(processName);
        triggerManager.deleteProcessTriggerBindByProcessId(persistenceDTO.getId());
        triggerManager.bindProcessTrigger(persistenceDTO.getId(), triggerId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbindProcessTrigger(String processName) {
        ProcessPersistenceDTO persistenceDTO = processPersistenceManager.getProcessPersistenceDTOByProcessName(processName);
        triggerManager.deleteProcessTriggerBindByProcessId(persistenceDTO.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindTriggerCondition(String triggerId, Collection<String> conditionIds) {
        getTrigger(triggerId);
        conditionIds.forEach(conditionId ->
            triggerManager.bindTriggerCondition(triggerId, conditionId)
        );
    }

    @Override
    public Boolean validProcessTrigger(String processName, Map<String, Object> data) {
        return listTrigger(processName).stream().map(triggerDTO -> {
            Collection<ConditionDTO> conditions = triggerDTO.getConditions();
            List<Boolean> calculation = conditions
                    .stream()
                    .map(conditionDTO -> conditionManager.testCondition(conditionDTO, data))
                    .collect(Collectors.toList());
            if (triggerDTO.getOperation().equals(OperationEnum.AND)) {
                return calculation.stream().allMatch(t -> t);
            } else {
                return calculation.stream().anyMatch(t -> t);
            }
        }).allMatch(t -> t);
    }

    public Resource getTriggerResource() {
        List<Resource> resources = resourceAuthAction.getResource().getChildren()
                .stream()
                .filter(children -> Objects.equals(PROCESS_DEFINITION, children.getResourceType()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(resources)){
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.AUTH_PROCESS_DEFINITION);
        }
        List<Resource> resourceList = resources.get(0).getChildren()
                .stream()
                .filter(children -> Objects.equals(TRIGGER, children.getResourceType()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(resourceList)){
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.AUTH_TRIGGER);
        }
        return resourceList.get(0);
    }

    public Resource getTriggerByResourceName(Resource resource, String resourceName) {
        List<Resource> resources = resource.getChildren()
                .stream()
                .filter(children -> Objects.equals(resourceName, children.getResourceName()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(resources)){
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.AUTH_TRIGGER_INFO, resourceName);
        }
        return resources.get(0);
    }
}
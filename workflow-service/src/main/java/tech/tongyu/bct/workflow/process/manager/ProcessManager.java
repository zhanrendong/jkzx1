package tech.tongyu.bct.workflow.process.manager;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.vavr.Tuple;
import io.vavr.Tuple3;
import org.activiti.engine.repository.ProcessDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.common.util.CollectionUtils;
import tech.tongyu.bct.workflow.dto.ProcessConfigDTO;
import tech.tongyu.bct.workflow.dto.ProcessPersistenceDTO;
import tech.tongyu.bct.workflow.dto.TriggerDTO;
import tech.tongyu.bct.workflow.exceptions.ReturnMessageAndTemplateDef;
import tech.tongyu.bct.workflow.exceptions.WorkflowCommonException;
import tech.tongyu.bct.workflow.process.Process;
import tech.tongyu.bct.workflow.process.Task;
import tech.tongyu.bct.workflow.process.enums.FilterTypeEnum;
import tech.tongyu.bct.workflow.process.enums.TaskTypeEnum;
import tech.tongyu.bct.workflow.process.filter.*;
import tech.tongyu.bct.workflow.process.func.TaskAction;
import tech.tongyu.bct.workflow.process.func.TaskActionScanner;
import tech.tongyu.bct.workflow.process.manager.act.ActProcessInstanceManager;
import tech.tongyu.bct.workflow.process.manager.act.ActProcessManager;
import tech.tongyu.bct.workflow.process.repo.*;
import tech.tongyu.bct.workflow.process.repo.entities.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author daivd.yang
 *  - mailto: yangyiwei@tongyu.tech
 */
@Component
public class ProcessManager {

    private static Logger logger = LoggerFactory.getLogger(ProcessManager.class);

    private ProcessRepo processRepo;
    private TaskNodeRepo taskNodeRepo;
    private FilterRepo filterRepo;
    private ProcessFilterRepo processFilterRepo;
    private ProcessConfigRepo processConfigRepo;
    private TaskActionScanner taskActionScanner;
    private FilterScanner filterScanner;
    private ActProcessManager actProcessManager;
    private ActProcessInstanceManager actProcessInstanceManager;
    private TaskNodeManager taskNodeManager;
    private TriggerManager triggerManager;

    @Autowired
    public ProcessManager(
            ProcessRepo processRepo,
            TaskNodeRepo taskNodeRepo,
            FilterRepo filterRepo,
            ProcessFilterRepo processFilterRepo,
            ProcessConfigRepo processConfigRepo,
            TaskActionScanner taskActionScanner,
            TaskNodeManager taskNodeManager,
            FilterScanner filterScanner,
            ActProcessManager actProcessManager,
            ActProcessInstanceManager actProcessInstanceManager,
            TriggerManager triggerManager){
        this.processRepo = processRepo;
        this.taskNodeRepo = taskNodeRepo;
        this.filterRepo = filterRepo;
        this.processConfigRepo = processConfigRepo;
        this.processFilterRepo = processFilterRepo;
        this.taskActionScanner = taskActionScanner;
        this.taskNodeManager = taskNodeManager;
        this.filterScanner = filterScanner;
        this.actProcessManager = actProcessManager;
        this.actProcessInstanceManager = actProcessInstanceManager;
        this.triggerManager = triggerManager;
    }

    public Process getProcessByProcessInstanceId(String processInstanceId){
        return getProcessByProcessName(actProcessInstanceManager.getProcessInstanceByProcessInstanceId(processInstanceId).getProcessDefinitionName());
    }

    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public Collection<Process> listAllProcess(){
        return processRepo.findAllValidProcess()
                .stream()
                .map(processDbo -> this.getProcessByProcessName(processDbo.getProcessName()))
                .collect(Collectors.toSet());
    }

    @Transactional(rollbackFor = Exception.class)
    public void clearAllProcess(){
        actProcessManager.clearAllProcessDefinition();
    }

    @Transactional(rollbackFor = Exception.class)
    public void modifyProcessStatus(String processName, Boolean status){
        processRepo.modifyProcessDboStatusByProcessName(processName, status);
    }

    public Process getProcessByProcessName(String processName){
        return getProcessByProcessDefinition(actProcessManager.getProcessDefinitionByProcessName(processName));
    }

    public Process getProcessByProcessDefinitionId(String processDefinitionId){
        return getProcessByProcessDefinition(actProcessManager.getProcessDefinitionByProcessDefinitionId(processDefinitionId));
    }

    private ProcessDbo getProcessDboByProcessName(String processName){
        return processRepo.findValidProcessDboByProcessName(processName)
                .orElseThrow(() -> new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.UNKNOWN_PROCESS_NAME, processName));
    }

    /**
     * list all valid task node by process id
     * @param processId -> process's id
     * @return list of task node
     */
    private List<TaskNodeDbo> listTaskNodeDboByProcessId(String processId){
        Collection<TaskNodeDbo> taskNodeDbos = taskNodeRepo.findValidTaskNodeDboByProcessId(processId);
        if(CollectionUtils.isNotEmpty(taskNodeDbos)){
            return taskNodeDbos.stream()
                    .sorted(Comparator.comparingInt(TaskNodeDbo::getSequence))
                    .collect(Collectors.toList());
        }
        else{
            return Lists.newArrayList();
        }
    }

    private Collection<ProcessConfigDbo> listAllValidProcessConfigByProcessId(String processId){
        return processConfigRepo.findValidProcessConfigDbosByProcessId(processId);
    }

    private TaskAction getTaskAction(String actionClass){
        return taskActionScanner.getTaskAction(actionClass);
    }



    private static Collection<String> getFilterIdSetByTaskType(Collection<ProcessFilterDbo> processFilterDbos, TaskTypeEnum taskTypeEnum){
        return processFilterDbos.stream()
                .filter(processFilterDbo -> Objects.equals(processFilterDbo.getTaskType(), taskTypeEnum))
                .map(ProcessFilterDbo::getFilterId)
                .collect(Collectors.toSet());
    }

    private <T extends Filter> Map<TaskTypeEnum, Collection<T>> getTaskFilterMap(Class<T> clazz, FilterTypeEnum filterTypeEnum
            , Collection<FilterDbo> filterDbos
            , Collection<String> inputDataFilterIdSet, Collection<String> modifyDataFilterIdSet, Collection<String> reviewDataFilterIdSet){
        Map<TaskTypeEnum, Collection<T>> readableFilterMap = Maps.newHashMap();
        filterDbos.stream()
                .filter(filterDbo -> Objects.equals(filterDbo.getFilterType(), filterTypeEnum))
                .peek(filterDbo -> {
                    T taskReadableFilter = filterScanner.getFilter(clazz, filterDbo.getFilterClass());
                    // input data
                    if(inputDataFilterIdSet.contains(filterDbo.getId())){
                        Collection<T> taskReadableFilters = readableFilterMap.get(TaskTypeEnum.INPUT_DATA);
                        if(CollectionUtils.isEmpty(taskReadableFilters)){
                            taskReadableFilters = Sets.newHashSet();
                            readableFilterMap.put(TaskTypeEnum.INPUT_DATA, taskReadableFilters);
                        }
                        taskReadableFilters.add(taskReadableFilter);
                    }

                    // modify data
                    if(modifyDataFilterIdSet.contains(filterDbo.getId())){
                        Collection<T> taskReadableFilters = readableFilterMap.get(TaskTypeEnum.MODIFY_DATA);
                        if(CollectionUtils.isEmpty(taskReadableFilters)){
                            taskReadableFilters = Sets.newHashSet();
                            readableFilterMap.put(TaskTypeEnum.MODIFY_DATA, taskReadableFilters);
                        }
                        taskReadableFilters.add(taskReadableFilter);
                    }

                    // review data
                    if(reviewDataFilterIdSet.contains(filterDbo.getId())){
                        Collection<T> taskReadableFilters = readableFilterMap.get(TaskTypeEnum.REVIEW_DATA);
                        if(CollectionUtils.isEmpty(taskReadableFilters)){
                            taskReadableFilters = Sets.newHashSet();
                            readableFilterMap.put(TaskTypeEnum.REVIEW_DATA, taskReadableFilters);
                        }
                        taskReadableFilters.add(taskReadableFilter);
                    }
                })
                .collect(Collectors.toSet());

        return readableFilterMap;
    }

    /**
     * be used to get 3 kinds of filter related to one process
     * 1. TaskReadableFilter, and they will be applied to all related tasks with the taskType specified in map's key
     * 2. TaskCompletableFilter, and they will be applied to all related tasks with the taskType specified in map's key
     * 3. ProcessStartableFilter, will be applied to process itself, to test whether it need be started.
     * @param processId -> process's id
     * @return -> tuple of:
     *  map(taskType, list of taskReadableFilters),
     *  map(taskType, list of taskCompletableFilters),
     *  list of processStartableFilters
     */
    private Tuple3<Map<TaskTypeEnum, Collection<TaskReadableFilter>>
                , Map<TaskTypeEnum, Collection<TaskCompletableFilter>>
                , Collection<ProcessStartableFilter>> getFilterByProcessId(String processId){
        Collection<ProcessFilterDbo> processFilterDbos = processFilterRepo.listValidProcessFilterByProcessId(processId);
        Collection<String> filterIdSet = processFilterDbos.stream()
                .map(ProcessFilterDbo::getFilterId)
                .collect(Collectors.toSet());

        Collection<String> inputDataFilters = getFilterIdSetByTaskType(processFilterDbos, TaskTypeEnum.INPUT_DATA);
        Collection<String> modifyDataFilters = getFilterIdSetByTaskType(processFilterDbos, TaskTypeEnum.MODIFY_DATA);
        Collection<String> reviewDataFilters = getFilterIdSetByTaskType(processFilterDbos, TaskTypeEnum.REVIEW_DATA);
        Collection<FilterDbo> filterDbos = filterRepo.findValidFilterDbosByFilterId(filterIdSet);

        Collection<ProcessStartableFilter> processStartableFilters = filterDbos.stream()
                .filter(filterDbo -> Objects.equals(filterDbo.getFilterType(), FilterTypeEnum.PROCESS_STARTABLE))
                .map(filterDbo -> filterScanner.getProcessStartableFilter(filterDbo.getFilterClass()))
                .collect(Collectors.toSet());

        return Tuple.of(
                getTaskFilterMap(TaskReadableFilter.class, FilterTypeEnum.TASK_READABLE, filterDbos, inputDataFilters, modifyDataFilters, reviewDataFilters),
                getTaskFilterMap(TaskCompletableFilter.class, FilterTypeEnum.TASK_COMPLETABLE, filterDbos, inputDataFilters, modifyDataFilters, reviewDataFilters),
                processStartableFilters
        );
    }

    /**
     * get the process by processDefinition
     * @param processDefinition -> process's definition in activiti
     * @return Process
     * @see ProcessDefinition
     * @see Process
     */
    public Process getProcessByProcessDefinition(ProcessDefinition processDefinition){
        String processName = processDefinition.getName();
        ProcessDbo processDbo = getProcessDboByProcessName(processName);
        List<TaskNodeDbo> taskNodeDboList = listTaskNodeDboByProcessId(processDbo.getId());

        Collection<String> taskIdList = taskNodeDboList.stream().map(TaskNodeDbo::getId).collect(Collectors.toSet());
        Map<String, Collection<FilterDbo>> taskFiltersMap = taskNodeManager.listFilterByTaskId(taskIdList);
        Collection<ProcessConfigDTO> processConfigs = Converter.convertProcessConfigDbo2ProcessConfigDto(listAllValidProcessConfigByProcessId(processDbo.getId()));
        Collection<Task> tasks = taskNodeDboList.stream()
                .map(taskNodeDbo -> taskNodeManager.convertTaskNodeDboToTask(taskNodeDbo, taskFiltersMap))
                .collect(Collectors.toSet());

        Tuple3<Map<TaskTypeEnum, Collection<TaskReadableFilter>>
                , Map<TaskTypeEnum, Collection<TaskCompletableFilter>>
                , Collection<ProcessStartableFilter>> filtersTuple
                = getFilterByProcessId(processDbo.getId());

        Collection<TriggerDTO> triggerDTOS = triggerManager.listTrigger(processDbo.getId());

        ProcessPersistenceDTO processPersistenceDTO = new ProcessPersistenceDTO(processDbo.getId(), processName, processDbo.getStatus());
        return new Process(processPersistenceDTO, processDefinition, processConfigs, filtersTuple._3, filtersTuple._1, filtersTuple._2, tasks, triggerDTOS);
    }

}

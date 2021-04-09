package tech.tongyu.bct.workflow.process.manager;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.checkerframework.checker.units.qual.C;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.common.util.CollectionUtils;
import tech.tongyu.bct.workflow.dto.*;
import tech.tongyu.bct.workflow.exceptions.ReturnMessageAndTemplateDef;
import tech.tongyu.bct.workflow.exceptions.WorkflowCommonException;
import tech.tongyu.bct.workflow.process.Task;
import tech.tongyu.bct.workflow.process.enums.FilterTypeEnum;
import tech.tongyu.bct.workflow.process.enums.TaskTypeEnum;
import tech.tongyu.bct.workflow.process.filter.FilterScanner;
import tech.tongyu.bct.workflow.process.func.TaskActionScanner;
import tech.tongyu.bct.workflow.process.manager.act.ActHistoricProcessInstanceManger;
import tech.tongyu.bct.workflow.process.repo.*;
import tech.tongyu.bct.workflow.process.repo.entities.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static tech.tongyu.bct.workflow.initialize.process.ProcessDefinitionConstants.TASK_NAME;
import static tech.tongyu.bct.workflow.process.ProcessConstants.*;

/**
 * @author yongbin
 * @author david.yang
 *  - mailto: yangyiwei@tongyu.tech
 */
@Component
public class TaskNodeManager {

    private TaskApproveGroupRepo taskApproveGroupRepo;
    private ApproveGroupRepo approveGroupRepo;
    private TaskNodeRepo taskNodeRepo;
    private TaskActionScanner taskActionScanner;
    private TaskFilterRepo taskFilterRepo;
    private FilterRepo filterRepo;
    private ProcessRepo processRepo;
    private FilterScanner filterScanner;
    private ApproveGroupManager approveGroupManager;
    private TaskManager taskManager;

    @Autowired
    public TaskNodeManager(
            TaskApproveGroupRepo taskApproveGroupRepo
            , ApproveGroupRepo approveGroupRepo
            , TaskNodeRepo taskNodeRepo
            , ActHistoricProcessInstanceManger actHistoricProcessInstanceManger
            , TaskActionScanner taskActionScanner
            , TaskFilterRepo taskFilterRepo
            , FilterScanner filterScanner
            , FilterRepo filterRepo
            , ProcessRepo processRepo
            , ApproveGroupManager approveGroupManager
            , TaskManager taskManager) {
        this.taskApproveGroupRepo = taskApproveGroupRepo;
        this.approveGroupRepo = approveGroupRepo;
        this.taskNodeRepo = taskNodeRepo;
        this.taskActionScanner = taskActionScanner;
        this.filterScanner = filterScanner;
        this.taskFilterRepo = taskFilterRepo;
        this.filterRepo = filterRepo;
        this.processRepo = processRepo;
        this.approveGroupManager = approveGroupManager;
        this.taskManager = taskManager;
    }

    /**
     * search for the filters for specified task with the given taskId, return the map
     * with the key(task id) & the value (collection of filter)
     * @param taskIdList -> list of tasks' id
     * @return map with the key (task id) and the value (collection of filter dbo)
     */
    public Map<String, Collection<FilterDbo>> listFilterByTaskId(Collection<String> taskIdList){
        Collection<TaskFilterDbo> taskFilterDboList = taskFilterRepo.listValidTaskFilterByTaskId(taskIdList);
        if(CollectionUtils.isEmpty(taskFilterDboList)){
            return Maps.newHashMap();
        }
        Collection<String> filterIdList = taskFilterDboList.stream()
                .map(TaskFilterDbo::getFilterId)
                .collect(Collectors.toSet());
        if(CollectionUtils.isEmpty(filterIdList)){
            return Maps.newHashMap();
        }
        Collection<FilterDbo> filterDboList = filterRepo.findValidFilterDbosByFilterId(filterIdList);
        Map<String, FilterDbo> filterDboMap = filterDboList.stream()
                .collect(Collectors.toMap(FilterDbo::getId, Function.identity()));

        Map<String, Collection<FilterDbo>> returnMap = Maps.newHashMap();
        taskFilterDboList.stream()
                .peek(taskFilterDbo -> {
                    String taskId = taskFilterDbo.getTaskNodeId();
                    Collection<FilterDbo> filters = returnMap.get(taskId);
                    if(CollectionUtils.isEmpty(filters)){
                        filters = Sets.newHashSet();
                        returnMap.put(taskId, filters);
                    }
                    String filterId = taskFilterDbo.getFilterId();
                    FilterDbo filterDbo = filterDboMap.get(filterId);
                    if(Objects.isNull(filterDbo)){
                        throw new RuntimeException("bad data: one record in task - filter relation with no filter persisted");
                    }
                    filters.add(filterDbo);
                })
                .collect(Collectors.toSet());
        return returnMap;
    }

    public Task convertTaskNodeDboToTask(TaskNodeDbo taskNodeDbo, Map<String, Collection<FilterDbo>> taskFiltersMap){
        Task task = new Task();
        task.setTaskId(taskNodeDbo.getId());
        task.setTaskName(taskNodeDbo.getTaskName());
        task.setTaskType(taskNodeDbo.getTaskType());
        task.setTaskAction(taskActionScanner.getTaskAction(taskNodeDbo.getActionClass()));
        Collection<FilterDbo> taskFilters = taskFiltersMap.get(taskNodeDbo.getId());
        Collection<String> taskReadableFilterClassSet = Sets.newHashSet();
        Collection<String> taskCompletableFilterClassSet = Sets.newHashSet();

        if(CollectionUtils.isNotEmpty(taskFilters)){
            taskReadableFilterClassSet = taskFilters.stream()
                    .filter(filterDbo -> Objects.equals(filterDbo.getFilterType(), FilterTypeEnum.TASK_READABLE))
                    .map(FilterDbo::getFilterClass)
                    .collect(Collectors.toSet());
            taskCompletableFilterClassSet = taskFilters.stream()
                    .filter(filterDbo -> Objects.equals(filterDbo.getFilterType(), FilterTypeEnum.TASK_COMPLETABLE))
                    .map(FilterDbo::getFilterClass)
                    .collect(Collectors.toSet());
        }
        task.setTaskReadableFilters(filterScanner.getTaskReadableFilters(taskReadableFilterClassSet));
        task.setTaskCompletableFilters(filterScanner.getTaskCompletableFilters(taskCompletableFilterClassSet));
        task.setSequence(taskNodeDbo.getSequence());

        Collection<String> approveGroupIdList = taskApproveGroupRepo.findValidApproveGroupIdByTaskId(taskNodeDbo.getId())
                .stream()
                .map(TaskApproveGroupDbo::getApproveGroupId)
                .collect(Collectors.toSet());
        Collection<ApproveGroupDbo> approveGroupDbos =
                CollectionUtils.isEmpty(approveGroupIdList)
                        ? Sets.newHashSet()
                        : approveGroupRepo.findValidApproveGroupById(approveGroupIdList);
        task.setApproveGroups(approveGroupManager.toApproveGroupDTO(approveGroupDbos));
        return task;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteTaskApproveGroup(String nodeId){
        taskApproveGroupRepo.deleteAllByTaskNodeId(nodeId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteTaskNodeByProcessId(String processId){
        Collection<TaskNodeDbo> taskNodeDbos = taskNodeRepo.findValidTaskNodeDboByProcessId(processId);
        if(CollectionUtils.isNotEmpty(taskNodeDbos)){
            Collection<String> taskNodeIdList = taskNodeDbos.stream()
                    .map(TaskNodeDbo::getId)
                    .collect(Collectors.toSet());

            taskApproveGroupRepo.deleteAllByTaskNodeIn(taskNodeIdList);
            taskFilterRepo.deleteAllByTaskIdIn(taskNodeIdList);
            taskNodeRepo.deleteAllByProcessId(processId);
        }
    }


    @Transactional(rollbackFor = Exception.class)
    public void deleteTaskApproveGroupByApproveGroupId(String approveGroupId){
        taskApproveGroupRepo.deleteValidTaskApproveGroupDboByApproveGroupId(approveGroupId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void modifyTaskApproveGroupBatch(Collection<TaskApproveGroupDTO> taskList){
        List<TaskApproveGroupDbo> taskApproveGroupDbos = new ArrayList<>();
        taskList.forEach(task -> {
            List<TaskApproveGroupDbo> taskApproveGroupDboByTaskId = taskApproveGroupRepo.findTaskApproveGroupDboByTaskId(task.getTaskId());
            Collection<String> groupIdList = task.getApproveGroupIdList();
            if (CollectionUtils.isEmpty(taskApproveGroupDboByTaskId)) {
                if (CollectionUtils.isNotEmpty(groupIdList)) {
                    Collection<ApproveGroupDbo> group = approveGroupRepo.findValidApproveGroupById(groupIdList);
                    if (group.size() != groupIdList.size()){
                        throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.TASK_FIND_GROUP_ERROR);
                    }
                    taskApproveGroupDbos.addAll(groupIdList
                            .stream()
                            .map(groupId -> new TaskApproveGroupDbo(task.getTaskId(), groupId))
                            .collect(Collectors.toList())
                    );
                }
            } else {
                taskApproveGroupRepo.deleteAllByTaskNodeId(task.getTaskId());
                if (CollectionUtils.isNotEmpty(groupIdList)) {
                    Collection<ApproveGroupDbo> group = approveGroupRepo.findValidApproveGroupById(groupIdList);
                    if (group.size() != groupIdList.size()){
                        throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.TASK_FIND_GROUP_ERROR);
                    }
                    taskApproveGroupDbos.addAll(groupIdList
                            .stream()
                            .map(groupId -> new TaskApproveGroupDbo(task.getTaskId(), groupId))
                            .collect(Collectors.toList())
                    );
                }else{
                    throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.BIND_TASK_ERROR);
                }
            }
        });
        taskApproveGroupRepo.saveAll(taskApproveGroupDbos);
    }

    @Transactional(rollbackFor = Exception.class)
    public void linkTaskNodeAndApproveGroup(String processName, List<Map<String, Object>> taskList){
        List<TaskApproveGroupDbo> taskApproveGroupDbos = new ArrayList<>();
        ProcessDbo processDbo = processRepo.findValidProcessDboByProcessName(processName)
                .orElseThrow(() -> new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.UNKNOWN_PROCESS_NAME, processName));
        Collection<TaskNodeDbo> nodeDbos = taskNodeRepo.findValidTaskNodeDboByProcessId(processDbo.getId());
        if (CollectionUtils.isEmpty(nodeDbos)){
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.TASK_FIND_PROCESS_ERROR);
        }
        Map<String, List<TaskNodeDbo>> map = nodeDbos.stream().collect(Collectors.groupingBy(TaskNodeDbo::getTaskName));
        //fixme: taskList 里的每个taskId, 确认有这样的task存在, 然后修改task - approveGroup的关联表
        taskList.forEach(task -> {
            TaskNodeDbo taskNodeDbos;
            if (Objects.equals(TASK_TYPE$_INPUT_DATA, task.get(TASK_TYPE))
                    || Objects.equals(TASK_TYPE$_MODIFY_DATA, task.get(TASK_TYPE))){
                taskNodeDbos = map.get(task.get(TASK_NAME).toString()).get(0);
            } else {
                List<TaskNodeDbo> reviewTasks = map.get(task.get(TASK_NAME).toString());
                taskNodeDbos = reviewTasks.stream()
                        .filter(review -> review.getSequence() == task.get(SEQUENCE))
                        .collect(Collectors.toList()).get(0);
            }
            List<String> groupIdList = (List<String>) task.get(APPROVE_GROUP_LIST);
            Collection<ApproveGroupDbo> group = approveGroupRepo.findValidApproveGroupById(groupIdList);
            if (group.size() != groupIdList.size()){
                throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.TASK_FIND_GROUP_ERROR);
            }
            taskApproveGroupDbos.addAll(groupIdList
                    .stream()
                    .map(groupId -> new TaskApproveGroupDbo(taskNodeDbos.getId(), groupId))
                    .collect(Collectors.toList())
            );
        });
        taskApproveGroupRepo.saveAll(taskApproveGroupDbos);
    }

    /**
     * persist task nodes
     * @param processId process's id
     * @param nodeList list of task nodes
     */
    @Transactional(rollbackFor = Exception.class)
    public void createTaskNode(String processId, Collection<TaskNode> nodeList){
        if(!isTaskNodeListValid(nodeList)){
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.INVALID_TASKS);
        }

        Collection<TaskNodeDbo> taskNodeDbos = taskNodeRepo.saveAll(
                nodeList.stream()
                .map(taskNode -> new TaskNodeDbo(
                        processId,
                        taskNode.getTaskName(),
                        taskNode.getTaskType(),
                        taskNode.getSequence(),
                        taskNode.getActionClass()
                ))
                .collect(Collectors.toSet())
        );

        Function<TaskNode, String> keyMapper = taskNodeDbo -> String.format("%s%d", taskNodeDbo.getTaskName(), taskNodeDbo.getSequence());

        Map<String, TaskNodeDbo> map = taskNodeDbos.stream()
                .collect(Collectors.toMap(keyMapper, Function.identity()));

        Collection<TaskApproveGroupDTO> taskApproveGroupDTOS = nodeList.stream()
                .map(taskNode -> {
                    TaskNodeDbo taskNodeDbo = map.get(keyMapper.apply(taskNode));
                    TaskApproveGroupDTO taskApproveGroupDTO = new TaskApproveGroupDTO();
                    taskApproveGroupDTO.setApproveGroupIdList(taskNode.getApproveGroupList());
                    taskApproveGroupDTO.setTaskId(taskNodeDbo.getId());
                    return taskApproveGroupDTO;
                })
                .collect(Collectors.toSet());

        this.modifyTaskApproveGroupBatch(taskApproveGroupDTOS);
    }

    public Boolean canDeleteGroup(String groupId){
        Boolean temp = true;
        List<TaskApproveGroupDbo> taskApproveGroupDbos = taskApproveGroupRepo.findValidTaskApproveGroupDboByApproveGroupId(Lists.newArrayList(groupId));
        for (TaskApproveGroupDbo taskApproveGroupDbo : taskApproveGroupDbos) {
            Integer count = taskApproveGroupRepo.countValidTaskApproveGroupDboByTaskNodeId(taskApproveGroupDbo.getTaskNodeId());
            if (count == 1){
                temp = false;
            }
        }
        return temp;
    }

    /**
     * check if the list of task nodes is valid:
     *  1 insertData taskType node
     *  1 updateData taskType node
     *  >=1 reviewData taskType node
     * @param nodeList list of task nodes
     */
    private Boolean isTaskNodeListValid(Collection<TaskNode> nodeList){
        if(CollectionUtils.isEmpty(nodeList) || nodeList.size() < 3){
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.INVALID_TASKS);
        }
        Collection<TaskNode> insertDataNode = Sets.newHashSet();
        Collection<TaskNode> updateDataNode = Sets.newHashSet();
        Collection<TaskNode> reviewDataNode = Sets.newHashSet();

        nodeList.stream()
                .map(taskNodeDTO -> {
                    switch (taskNodeDTO.getTaskType()){
                        case INPUT_DATA:
                            insertDataNode.add(taskNodeDTO);
                            break;
                        case REVIEW_DATA:
                            reviewDataNode.add(taskNodeDTO);
                            break;
                        case MODIFY_DATA:
                            updateDataNode.add(taskNodeDTO);
                            break;
                        case COUNTER_SIGN_DATA:
                            reviewDataNode.add(taskNodeDTO);
                            break;
                        default:
                            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.INVALID_TASK_TYPE, taskNodeDTO.getTaskType().getAlias());
                    }
                    return taskNodeDTO;
                }).collect(Collectors.toList());

        return insertDataNode.size() == 1
                && updateDataNode.size() == 1
                && reviewDataNode.size() >= 1;
    }

    public String getNextCounterSignTaskIdByTaskId(String taskId){
        TaskNodeDbo taskNodeDbo = taskNodeRepo.findValidTaskNodeDboById(taskId)
                .orElseThrow(() -> new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.TASK_FIND_PROCESS_ERROR));
        if (Objects.equals(taskNodeDbo.getTaskType(), TaskTypeEnum.MODIFY_DATA)
                || Objects.equals(taskNodeDbo.getTaskType(), TaskTypeEnum.INPUT_DATA)) {
            Collection<TaskNodeDbo> taskNodeList = taskNodeRepo.findValidTaskNodeDboByProcessId(taskNodeDbo.getProcessId());
            TaskNodeDbo dbo = taskNodeList.stream()
                    .filter(task -> Objects.equals(task.getTaskType(), TaskTypeEnum.REVIEW_DATA)
                            || Objects.equals(task.getTaskType(), TaskTypeEnum.COUNTER_SIGN_DATA))
                    .min(Comparator.comparingInt(TaskNodeDbo::getSequence)).get();
            if (Objects.equals(dbo.getTaskType(), TaskTypeEnum.COUNTER_SIGN_DATA)){
                return dbo.getId();
            }
        } else {
            Optional<TaskNodeDbo> nextTaskNodeDbo = taskNodeRepo.findValidTaskNodeDboByProcessIdAndSequence(taskNodeDbo.getProcessId(), taskNodeDbo.getSequence() + 1);
            if (!nextTaskNodeDbo.isPresent()){
                return null;
            } else {
                if (Objects.equals(nextTaskNodeDbo.get().getTaskType(), TaskTypeEnum.COUNTER_SIGN_DATA)) {
                    return nextTaskNodeDbo.get().getId();
                }
            }
        }
        return null;
    }

    public Boolean hasActiveCounterSignNodeByApproveGroupId(Collection<String> approveGroupIds){
        List<TaskApproveGroupDbo> taskApproveGroups = taskApproveGroupRepo.findValidTaskApproveGroupDboByApproveGroupId(approveGroupIds);
        List<String> taskNodeIds = taskApproveGroups.stream()
                .map(TaskApproveGroupDbo::getTaskNodeId)
                .collect(Collectors.toList());
        if(CollectionUtils.isEmpty(taskNodeIds)) {
            return false;
        }
        Collection<TaskNodeDbo> taskNodeDbos = taskNodeRepo.findValidTaskNodeDboById(taskNodeIds);
        List<TaskNodeDbo> counterSignNodes = taskNodeDbos.stream()
                .filter(taskNodeDbo -> Objects.equals(taskNodeDbo.getTaskType(), TaskTypeEnum.COUNTER_SIGN_DATA))
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(counterSignNodes)){
            List<String> taskDefinitionKeys = counterSignNodes.stream()
                    .map(task -> task.getTaskName() + "_" + task.getSequence())
                    .collect(Collectors.toList());
            Set<String> processId = counterSignNodes.stream().map(TaskNodeDbo::getProcessId).collect(Collectors.toSet());
            Collection<ProcessDbo> processDbos = processRepo.findAllValidProcessByProcessId(processId);
            return processDbos.stream().map(processDbo -> {
                Collection<org.activiti.engine.task.Task> tasks = taskManager.getTaskByProcessName(processDbo.getProcessName());
                if (CollectionUtils.isNotEmpty(tasks)){
                    return tasks.stream().anyMatch(task -> taskDefinitionKeys.contains(task.getTaskDefinitionKey()));
                }
                return false;
            }).anyMatch(t -> t);
        }
        return false;
    }

    public List<ApproveGroupDTO> toApproveGroupDTO(List<ApproveGroupDbo> approveGroupDboList){
        if (Objects.isNull(approveGroupDboList)){
            return Lists.newArrayList();
        }
        return approveGroupDboList.stream().map(v ->
                new ApproveGroupDTO(v.getId(), v.getApproveGroupName())
        ).collect(Collectors.toList());
    }

    public Boolean isInTaskApproveGroup(Task task, String username){
        return task.isInTaskApproveGroup(username);
    }
}

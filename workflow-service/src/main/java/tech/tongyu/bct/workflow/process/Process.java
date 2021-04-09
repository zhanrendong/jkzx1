package tech.tongyu.bct.workflow.process;

import org.activiti.engine.repository.ProcessDefinition;
import tech.tongyu.bct.common.util.CollectionUtils;
import tech.tongyu.bct.workflow.dto.ProcessConfigDTO;
import tech.tongyu.bct.workflow.dto.ProcessPersistenceDTO;
import tech.tongyu.bct.workflow.dto.TriggerDTO;
import tech.tongyu.bct.workflow.exceptions.ReturnMessageAndTemplateDef;
import tech.tongyu.bct.workflow.exceptions.WorkflowCommonException;
import tech.tongyu.bct.workflow.process.enums.TaskTypeEnum;
import tech.tongyu.bct.workflow.process.filter.ProcessStartableFilter;
import tech.tongyu.bct.workflow.process.filter.TaskCompletableFilter;
import tech.tongyu.bct.workflow.process.filter.TaskReadableFilter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author david.yang
 *  - mailto yangyiwei@tongyu.tech
 */
public class Process {

    private ProcessPersistenceDTO process;
    private ProcessDefinition processDefinition;
    private Collection<ProcessStartableFilter> processStartableFilters;
    private Map<TaskTypeEnum, Collection<TaskReadableFilter>> taskTypeReadableFilters;
    private Map<TaskTypeEnum, Collection<TaskCompletableFilter>> taskTypeCompletableFilters;
    private Collection<ProcessConfigDTO> processConfigs;
    private Collection<Task> tasks;
    private Collection<TriggerDTO> triggers;

    public Process(ProcessPersistenceDTO process, ProcessDefinition processDefinition, Collection<ProcessConfigDTO> processConfigs
            , Collection<ProcessStartableFilter> processStartableFilters
            , Map<TaskTypeEnum, Collection<TaskReadableFilter>> taskTypeReadableFilters
            , Map<TaskTypeEnum, Collection<TaskCompletableFilter>> taskTypeCompletableFilters
            , Collection<Task> tasks, Collection<TriggerDTO> triggers){
        this.process = process;
        this.processDefinition = processDefinition;
        this.processConfigs = processConfigs;
        this.processStartableFilters = processStartableFilters;
        this.taskTypeReadableFilters = taskTypeReadableFilters;
        this.taskTypeCompletableFilters = taskTypeCompletableFilters;
        this.tasks = tasks;
        this.triggers = triggers;
    }

    /**
     * get status of process, enabled or disabled(true or false)
     * @return true or false , enabled if true
     */
    public Boolean getStatus(){
        return process.getStatus();
    }

    public String getProcessName(){
        return process.getProcessName();
    }

    public Task getTaskByActTaskId(String actTaskId){
        List<Task> taskList = tasks.stream()
                .filter(task -> Objects.equals(task.getActTaskId(), actTaskId))
                .collect(Collectors.toList());

        // taskList will never be greater than 1
        return taskList.get(0);
    }

    /**
     * get task with the inputData taskType
     * @return inputData task
     */
    public Task getInputTask(){
        if(CollectionUtils.isNotEmpty(this.tasks)){
            List<Task> inputTasks = tasks.stream()
                    .filter(task -> Objects.equals(task.getTaskType(), TaskTypeEnum.INPUT_DATA))
                    .collect(Collectors.toList());
            if(CollectionUtils.isEmpty(inputTasks)){
                throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.MISSING_INPUT_TASK, getProcessName());
            }
            if(inputTasks.size() > 1){
                throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.MULTI_INPUT_TASKS, getProcessName());
            }

            return inputTasks.get(0);
        }
        throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.MISSING_TASKS, getProcessName());
    }

    public Integer getMaxSequence(){
        return this.tasks.stream()
                .filter(task -> Objects.equals(task.getTaskType(), TaskTypeEnum.REVIEW_DATA)
                        || Objects.equals(task.getTaskType(), TaskTypeEnum.COUNTER_SIGN_DATA))
                .max(Comparator.comparingInt(Task::getSequence))
                .map(Task::getSequence).get();
    }

    public Boolean hasCounterSignTask(){
        return this.tasks.stream()
                .anyMatch(task -> Objects.equals(task.getTaskType(), TaskTypeEnum.COUNTER_SIGN_DATA));
    }

    /**
     * get task with the modifyData taskType
     * @return modifyData task
     */
    public Task getModifyTask(){
        if(CollectionUtils.isNotEmpty(this.tasks)){
            List<Task> modifyTasks = tasks.stream()
                    .filter(task -> Objects.equals(task.getTaskType(), TaskTypeEnum.MODIFY_DATA))
                    .collect(Collectors.toList());
            if(CollectionUtils.isEmpty(modifyTasks)){
                throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.MISSING_MODIFY_TASK, getProcessName());
            }
            if(modifyTasks.size() > 1){
                throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.MULTI_MODIFY_TASKS, getProcessName());
            }

            return modifyTasks.get(0);
        }
        throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.MISSING_TASKS, getProcessName());
    }

    /**
     * list tasks of reviewData taskType
     * @return list of review tasks, it has been sorted by task's sequence
     */
    public List<Task> listReviewTask(){
        if(CollectionUtils.isEmpty(tasks)){
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.MISSING_TASKS, getProcessName());
        }
        List<Task> reviewTasks = tasks.stream()
                .filter(task -> Objects.equals(task.getTaskType(), TaskTypeEnum.REVIEW_DATA)
                        || Objects.equals(task.getTaskType(), TaskTypeEnum.COUNTER_SIGN_DATA))
                .sorted(Comparator.comparingInt(Task::getSequence))
                .collect(Collectors.toList());
        if(CollectionUtils.isEmpty(reviewTasks)){
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.MISSING_REVIEW_TASKS, getProcessName());
        }

        return reviewTasks;
    }

    public ProcessPersistenceDTO getProcess() {
        return process;
    }

    public void setProcess(ProcessPersistenceDTO process) {
        this.process = process;
    }

    public Collection<ProcessConfigDTO> getProcessConfigs() {
        return processConfigs;
    }

    public void setProcessConfigs(Collection<ProcessConfigDTO> processConfigs) {
        this.processConfigs = processConfigs;
    }

    public String getProcessId(){
        return process.getId();
    }

    public ProcessDefinition getProcessDefinition() {
        return processDefinition;
    }

    public void setProcessDefinition(ProcessDefinition processDefinition) {
        this.processDefinition = processDefinition;
    }

    public Collection<ProcessStartableFilter> getProcessStartableFilters() {
        return processStartableFilters;
    }

    public void setProcessStartableFilters(Collection<ProcessStartableFilter> processStartableFilters) {
        this.processStartableFilters = processStartableFilters;
    }

    public Map<TaskTypeEnum, Collection<TaskReadableFilter>> getTaskTypeReadableFilters() {
        return taskTypeReadableFilters;
    }

    public void setTaskTypeReadableFilters(Map<TaskTypeEnum, Collection<TaskReadableFilter>> taskTypeReadableFilters) {
        this.taskTypeReadableFilters = taskTypeReadableFilters;
    }

    public Map<TaskTypeEnum, Collection<TaskCompletableFilter>> getTaskTypeCompletableFilters() {
        return taskTypeCompletableFilters;
    }

    public void setTaskTypeCompletableFilters(Map<TaskTypeEnum, Collection<TaskCompletableFilter>> taskTypeCompletableFilters) {
        this.taskTypeCompletableFilters = taskTypeCompletableFilters;
    }

    public Collection<Task> getTasks() {
        return tasks;
    }

    public void setTasks(Collection<Task> tasks) {
        this.tasks = tasks;
    }

    public Collection<TriggerDTO> getTriggers() {
        return triggers;
    }

    public void setTriggers(Collection<TriggerDTO> triggers) {
        this.triggers = triggers;
    }
}

package tech.tongyu.bct.workflow.dto;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import tech.tongyu.bct.common.util.CollectionUtils;
import tech.tongyu.bct.workflow.process.Process;
import tech.tongyu.bct.workflow.process.enums.TaskTypeEnum;
import tech.tongyu.bct.workflow.process.filter.ProcessStartableFilter;
import tech.tongyu.bct.workflow.process.filter.TaskCompletableFilter;
import tech.tongyu.bct.workflow.process.filter.TaskReadableFilter;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author david.yang
 *  - mailto: yangyiwei@tongyu.tech
 */
public class ProcessDTO {

    public static ProcessDTO of(Process process){
        ProcessDTO processDTO = new ProcessDTO();
        processDTO.setProcessId(process.getProcessId());
        processDTO.setProcessName(process.getProcessName());
        Collection<ProcessStartableFilter> processStartableFilterObjects = process.getProcessStartableFilters();
        processDTO.setProcessStartableFilters(
                processStartableFilterObjects.stream()
                        .map(processStartableFilter -> processStartableFilter.getClass().getName())
                        .collect(Collectors.toSet()));

        processDTO.setStatus(process.getStatus());
        processDTO.setTasks(TaskDTO.ofCollection(process.getTasks()));
        processDTO.setProcessConfigs(process.getProcessConfigs());
        processDTO.setTriggers(process.getTriggers());

        Map<TaskTypeEnum, Collection<String>> taskTypeReadableFiltersMap = Maps.newHashMap();
        process.getTaskTypeReadableFilters()
                .entrySet()
                .stream()
                .peek(entry -> {
                    TaskTypeEnum taskType = entry.getKey();
                    Collection<TaskReadableFilter> filters = entry.getValue();
                    if(CollectionUtils.isEmpty(filters)){
                        taskTypeReadableFiltersMap.put(taskType, Sets.newHashSet());
                    } else {
                        taskTypeReadableFiltersMap.put(taskType, filters.stream()
                                .map(filter -> filter.getClass().getName())
                                .collect(Collectors.toSet()));
                    }
                })
                .collect(Collectors.toSet());

        processDTO.setTaskTypeReadableFilters(taskTypeReadableFiltersMap);

        Map<TaskTypeEnum, Collection<String>> taskTypeCompletableFiltersMap = Maps.newHashMap();
        process.getTaskTypeCompletableFilters()
                .entrySet()
                .stream()
                .peek(entry -> {
                    TaskTypeEnum taskType = entry.getKey();
                    Collection<TaskCompletableFilter> filters = entry.getValue();
                    if(CollectionUtils.isEmpty(filters)){
                        taskTypeCompletableFiltersMap.put(taskType, Sets.newHashSet());
                    } else {
                        taskTypeCompletableFiltersMap.put(taskType, filters.stream()
                                .map(filter -> filter.getClass().getName())
                                .collect(Collectors.toSet()));
                    }
                })
                .collect(Collectors.toSet());
        processDTO.setTaskTypeCompletableFilters(taskTypeCompletableFiltersMap);

        return processDTO;
    }

    private String processId;
    private String processName;
    private Collection<String> processStartableFilters;
    private Boolean status;

    private Collection<TaskDTO> tasks;
    private Collection<ProcessConfigDTO> processConfigs;

    private Map<TaskTypeEnum, Collection<String>> taskTypeReadableFilters;
    private Map<TaskTypeEnum, Collection<String>> taskTypeCompletableFilters;

    private Collection<TriggerDTO> triggers;

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public Collection<TaskDTO> getTasks() {
        return tasks;
    }

    public void setTasks(Collection<TaskDTO> tasks) {
        this.tasks = tasks;
    }

    public Collection<ProcessConfigDTO> getProcessConfigs() {
        return processConfigs;
    }

    public void setProcessConfigs(Collection<ProcessConfigDTO> processConfigs) {
        this.processConfigs = processConfigs;
    }

    public Map<TaskTypeEnum, Collection<String>> getTaskTypeReadableFilters() {
        return taskTypeReadableFilters;
    }

    public void setTaskTypeReadableFilters(Map<TaskTypeEnum, Collection<String>> taskTypeReadableFilters) {
        this.taskTypeReadableFilters = taskTypeReadableFilters;
    }

    public Map<TaskTypeEnum, Collection<String>> getTaskTypeCompletableFilters() {
        return taskTypeCompletableFilters;
    }

    public void setTaskTypeCompletableFilters(Map<TaskTypeEnum, Collection<String>> taskTypeCompletableFilters) {
        this.taskTypeCompletableFilters = taskTypeCompletableFilters;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public Collection<String> getProcessStartableFilters() {
        return processStartableFilters;
    }

    public void setProcessStartableFilters(Collection<String> processStartableFilters) {
        this.processStartableFilters = processStartableFilters;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public Collection<TriggerDTO> getTriggers() {
        return triggers;
    }

    public void setTriggers(Collection<TriggerDTO> triggers) {
        this.triggers = triggers;
    }
}

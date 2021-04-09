package tech.tongyu.bct.workflow.dto;

import tech.tongyu.bct.workflow.process.enums.FilterTypeEnum;

public class FilterDTO {

    private String id;
    private String filterName;
    private String filterClass;
    private String processName;
    private String taskName;
    private FilterTypeEnum filterType;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFilterName() {
        return filterName;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    public String getFilterClass() {
        return filterClass;
    }

    public void setFilterClass(String filterClass) {
        this.filterClass = filterClass;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public FilterTypeEnum getFilterType() {
        return filterType;
    }

    public void setFilterType(FilterTypeEnum filterType) {
        this.filterType = filterType;
    }
}

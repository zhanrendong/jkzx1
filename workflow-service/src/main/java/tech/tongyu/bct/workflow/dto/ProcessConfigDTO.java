package tech.tongyu.bct.workflow.dto;

public class ProcessConfigDTO {

    public ProcessConfigDTO(String configId, String configName, String processId, Boolean status){
        this.configId = configId;
        this.configName = configName;
        this.processId = processId;
        this.status = status;
    }

    private String configId;
    private String configName;
    private String processId;
    private Boolean status;

    public String getConfigId() {
        return configId;
    }

    public void setConfigId(String configId) {
        this.configId = configId;
    }

    public String getConfigName() {
        return configName;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }
}

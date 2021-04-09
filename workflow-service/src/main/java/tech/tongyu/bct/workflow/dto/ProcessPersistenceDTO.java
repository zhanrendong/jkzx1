package tech.tongyu.bct.workflow.dto;

/**
 * @author david.yang
 *  - mailto: yangyiwei@tongyu.tech
 */
public class ProcessPersistenceDTO {

    private String id;
    private String processName;
    private Boolean status;

    public ProcessPersistenceDTO(String id, String processName, Boolean status) {
        this.id = id;
        this.processName = processName;
        this.status = status;
    }

    public ProcessPersistenceDTO() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }
}

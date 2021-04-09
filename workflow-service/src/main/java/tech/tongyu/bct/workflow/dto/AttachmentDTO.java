package tech.tongyu.bct.workflow.dto;

public class AttachmentDTO {

    public AttachmentDTO(String attachmentId, String attachmentName, String createdBy,
                         String processInstanceId, String attachmentPath) {
        this.attachmentId = attachmentId;
        this.attachmentName = attachmentName;
        this.createdBy = createdBy;
        this.processInstanceId = processInstanceId;
        this.attachmentPath = attachmentPath;
    }

    private String attachmentId;
    private String attachmentName;
    private String createdBy;
    private String processInstanceId;
    private String attachmentPath;


    public String getAttachmentId() {
        return attachmentId;
    }

    public void setAttachmentId(String attachmentId) {
        this.attachmentId = attachmentId;
    }

    public String getAttachmentName() {
        return attachmentName;
    }

    public void setAttachmentName(String attachmentName) {
        this.attachmentName = attachmentName;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public String getAttachmentPath() {
        return attachmentPath;
    }

    public void setAttachmentPath(String attachmentPath) {
        this.attachmentPath = attachmentPath;
    }
}

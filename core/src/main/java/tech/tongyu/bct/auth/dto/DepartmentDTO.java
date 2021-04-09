package tech.tongyu.bct.auth.dto;

public class DepartmentDTO {

    private String id;
    private String departmentName;
    private String departmentType;
    private String description;
    private String parentId;

    public DepartmentDTO(String id, String departmentName, String departmentType, String description, String parentId) {
        this.id = id;
        this.departmentName = departmentName;
        this.departmentType = departmentType;
        this.description = description;
        this.parentId = parentId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getDepartmentType() {
        return departmentType;
    }

    public void setDepartmentType(String departmentType) {
        this.departmentType = departmentType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }
}

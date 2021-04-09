package tech.tongyu.bct.auth.dto;

import java.util.Objects;

public class PagePermissionDTO {

    private String roleId;
    private String pageComponentId;

    public PagePermissionDTO() {
        super();
    }

    public PagePermissionDTO(String roleId, String pageComponentId) {
        this.roleId = roleId;
        this.pageComponentId = pageComponentId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PagePermissionDTO) {
            PagePermissionDTO other = (PagePermissionDTO) obj;
            return Objects.equals(this.pageComponentId, other.pageComponentId)
                    && Objects.equals(this.roleId, other.roleId);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (this.roleId == null ? 0 : this.roleId.hashCode())
                + (this.pageComponentId == null ? 0 : this.pageComponentId.hashCode());
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getPageComponentId() {
        return pageComponentId;
    }

    public void setPageComponentId(String pageComponentId) {
        this.pageComponentId = pageComponentId;
    }

}

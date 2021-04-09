package tech.tongyu.bct.workflow.dto;
/**
 * @author 勇斌
 */
public class UserApproveGroupDTO {

    public UserApproveGroupDTO(String userApproveGroupId, String username,
                               String departmentId, String nickName) {
        this.userApproveGroupId = userApproveGroupId;
        this.username = username;
        this.departmentId = departmentId;
        this.nickName = nickName;
    }

    private String userApproveGroupId;
    private String username;
    private String departmentId;
    private String nickName;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserApproveGroupId() {
        return userApproveGroupId;
    }

    public void setUserApproveGroupId(String userApproveGroupId) {
        this.userApproveGroupId = userApproveGroupId;
    }

    public String getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }
}

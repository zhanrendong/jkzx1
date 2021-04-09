package tech.tongyu.bct.workflow.dto;


import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static tech.tongyu.bct.workflow.process.ProcessConstants.*;
import static tech.tongyu.bct.workflow.process.utils.MapValueExtractionUtils.*;

/**
 * @author yongbin
 * @author david.yang
 *  - mailto: yangyiwei@tongyu.tech
 */
public class ApproveGroupDTO {

    public static ApproveGroupDTO of(Map<String, Object> map){
        Object description = map.get(DESCRIPTION);
        return new ApproveGroupDTO(
                getNonBlankStringFromMap(map, APPROVE_GROUP_ID, "approveGroupId不能为空"),
                getNonBlankStringFromMap(map, APPROVE_GROUP_NAME, "approveGroupName不能为空"),
                Objects.isNull(description) ? "" : description.toString(),
                null
        );
    }

    public ApproveGroupDTO(String approveGroupId,
                           String approveGroupName,
                           String description,
                           Collection<UserApproveGroupDTO> userList) {
        this.approveGroupId = approveGroupId;
        this.approveGroupName = approveGroupName;
        this.description = description;
        this.userList = userList;
    }

    public ApproveGroupDTO(String approveGroupId, String approveGroupName) {
        this.approveGroupId = approveGroupId;
        this.approveGroupName = approveGroupName;
    }

    private String approveGroupId;
    private String approveGroupName;
    private String description;
    private Collection<UserApproveGroupDTO> userList;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getApproveGroupId() {
        return approveGroupId;
    }

    public void setApproveGroupId(String approveGroupId) {
        this.approveGroupId = approveGroupId;
    }

    public String getApproveGroupName() {
        return approveGroupName;
    }

    public void setApproveGroupName(String approveGroupName) {
        this.approveGroupName = approveGroupName;
    }

    public Collection<UserApproveGroupDTO> getUserList() {
        return userList;
    }

    public void setUserList(List<UserApproveGroupDTO> userList) {
        this.userList = userList;
    }
}

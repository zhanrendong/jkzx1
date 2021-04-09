package tech.tongyu.bct.workflow.process.service;

import tech.tongyu.bct.workflow.dto.ApproveGroupDTO;
import tech.tongyu.bct.workflow.dto.UserApproveGroupDTO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 审批组操作
 * @author 勇斌
 */
public interface ApproveGroupService {

    /**
     * create approve group
     * @param approveGroupName name of approve group
     * @param description description
     * @return approve group DTO
     */
    ApproveGroupDTO createApproveGroup(String approveGroupName, String description);

    /**
     * modify approve group (name or description) by approve group id
     * @param approveGroupId approve group id
     * @param approveGroupName name of approve group
     * @param description description
     */
    void modifyApproveGroup(String approveGroupId,String approveGroupName, String description);

    /**
     * modify approve group's user list
     * @param approveGroupId approve group id
     * @param username collection of username
     */
    void modifyApproveGroupUserList(String approveGroupId, Collection<String> username);

    /**
     * deletion of approve group
     * @param approveGroupId id of approve group
     */
    void deleteApproveGroup(String approveGroupId);

    /**
     * list approve group (with user list)
     * @return list of approve group DTO
     */
    Collection<ApproveGroupDTO> listApproveGroup();

    /**
     * get approve group id
     * @param approveGroupId id of approve group
     * @return approve group DTO
     */
    ApproveGroupDTO getApproveGroup(String approveGroupId);
}

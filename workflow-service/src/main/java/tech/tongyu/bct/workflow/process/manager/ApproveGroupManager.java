package tech.tongyu.bct.workflow.process.manager;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.auth.dto.UserDTO;
import tech.tongyu.bct.auth.exception.manager.ManagerException;
import tech.tongyu.bct.auth.manager.UserManager;
import tech.tongyu.bct.common.util.CollectionUtils;
import tech.tongyu.bct.workflow.dto.ApproveGroupDTO;
import tech.tongyu.bct.workflow.dto.UserApproveGroupDTO;
import tech.tongyu.bct.workflow.exceptions.ReturnMessageAndTemplateDef;
import tech.tongyu.bct.workflow.exceptions.WorkflowCommonException;
import tech.tongyu.bct.workflow.process.repo.ApproveGroupRepo;
import tech.tongyu.bct.workflow.process.repo.TaskApproveGroupRepo;
import tech.tongyu.bct.workflow.process.repo.UserApproveGroupRepo;
import tech.tongyu.bct.workflow.process.repo.entities.ApproveGroupDbo;
import tech.tongyu.bct.workflow.process.repo.entities.TaskApproveGroupDbo;
import tech.tongyu.bct.workflow.process.repo.entities.UserApproveGroupDbo;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author david.yang
 *  - mailto: yangyiwei@tongyu.tech
 * @author yongbin
 */

@Component
public class ApproveGroupManager {

    private ApproveGroupRepo approveGroupRepo;
    private UserApproveGroupRepo userApproveGroupRepo;
    private TaskApproveGroupRepo taskApproveGroupRepo;
    private UserManager userManager;

    @Autowired
    public ApproveGroupManager(ApproveGroupRepo approveGroupRepo
            , UserApproveGroupRepo userApproveGroupRepo
            , TaskApproveGroupRepo taskApproveGroupRepo
            , UserManager userManager) {
        this.approveGroupRepo = approveGroupRepo;
        this.userApproveGroupRepo = userApproveGroupRepo;
        this.taskApproveGroupRepo = taskApproveGroupRepo;
        this.userManager = userManager;
    }

    @Transactional
    public ApproveGroupDTO createApproveGroup(String approveGroupName, String description){
        Optional<ApproveGroupDbo> allByApproveGroupName = approveGroupRepo.findValidApproveGroupDboByApproveGroupName(approveGroupName);

        if (allByApproveGroupName.isPresent()){
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.APPROVE_GROUP_REPEAT, approveGroupName);
        }

        ApproveGroupDbo approveGroupDbo = new ApproveGroupDbo(approveGroupName, description);
        return toApproveGroupDTO(approveGroupRepo.save(approveGroupDbo));
    }

    @Transactional(rollbackFor = Exception.class)
    public void modifyUserApproveGroup(String approveGroupId, Collection<String> username){
        userApproveGroupRepo.deleteValidUserApproveGroupDboByApproveGroupId(approveGroupId);
        if(CollectionUtils.isNotEmpty(username)) {
            Collection<UserApproveGroupDbo> newGroup =
                    username.stream().map(uname ->
                            new UserApproveGroupDbo(approveGroupId, uname))
                            .collect(Collectors.toSet());
            userApproveGroupRepo.saveAll(newGroup);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void modifyApproveGroup(String approveGroupId, String approveGroupName, String description){
        Optional<ApproveGroupDbo> opApproveGroupDbo = approveGroupRepo.findValidApproveGroupDboByApproveGroupId(approveGroupId);
        opApproveGroupDbo.map(approveGroupDbo -> {
                    Optional<ApproveGroupDbo> opDboGetByName = approveGroupRepo.findValidApproveGroupDboByApproveGroupName(approveGroupName);
                    opDboGetByName.ifPresent(dbo -> {
                        if(!Objects.equals(dbo.getId(), approveGroupDbo.getId())){
                            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.APPROVE_GROUP_REPEAT, approveGroupId);
                        }
                    });

                    approveGroupDbo.setApproveGroupName(approveGroupName);
                    approveGroupDbo.setDescription(description);

                    return approveGroupRepo.save(approveGroupDbo);
                })
                .orElseThrow(() -> new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.APPROVE_GROUP_NOT_FOUND, approveGroupId));
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteApproveGroup(String approveGroupId){
        //删除审批组
        approveGroupRepo.deleteValidApproveGroupDboById(approveGroupId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteUserApproveGroup(String approveGroupId){
        //删除审批组关联用户
        userApproveGroupRepo.deleteValidUserApproveGroupDboByApproveGroupId(approveGroupId);
    }

    @Transactional(rollbackFor = Exception.class, readOnly = true)
    public Collection<ApproveGroupDTO> listApproveGroup(){
        Collection<ApproveGroupDbo> approveGroupDbos = approveGroupRepo.findValidApproveGroup();

        return approveGroupDbos.stream().map(approveGroupDbo ->
                new ApproveGroupDTO(
                        approveGroupDbo.getId(),
                        approveGroupDbo.getApproveGroupName(),
                        approveGroupDbo.getDescription(),
                        listUserApproveGroupDtoByApproveGroupId(approveGroupDbo.getId()))
        ).collect(Collectors.toSet());
    }

    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public Boolean hasGroupByUsername(String username){

        Collection<UserApproveGroupDbo> userApproveGroupDbos = userApproveGroupRepo.findValidUserApproveGroupByUsername(username);
        if(CollectionUtils.isEmpty(userApproveGroupDbos)){
            return false;
        }

        Collection<String> idList = userApproveGroupDbos.stream()
                .map(UserApproveGroupDbo::getApproveGroupId)
                .collect(Collectors.toSet());
        //当前审批组是否关联流程任务
        return taskApproveGroupRepo.countValidTaskApproveGroupDboByApproveGroupId(idList) > 0;
    }

    private UserDTO getUserDtoOrNull(String username){
        try {
            return userManager.getUserByUserName(username);
        }catch (ManagerException e){
            return null;
        }
    }

    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public Collection<UserApproveGroupDTO> listUserApproveGroupDtoByApproveGroupId(String approveGroupId){
        Collection<UserApproveGroupDbo> userApproveGroupDbos = userApproveGroupRepo.findValidUserApproveGroupByApproveGroupId(approveGroupId);
        if(CollectionUtils.isEmpty(userApproveGroupDbos)){
            return Sets.newHashSet();
        }
        return userApproveGroupDbos.stream()
                .map(userApproveGroupDbo -> {
                    UserDTO userDTO = getUserDtoOrNull(userApproveGroupDbo.getUsername());
                    if(Objects.isNull(userDTO)) {
                        return null;
                    }
                    return new UserApproveGroupDTO(
                            userApproveGroupDbo.getId(),
                            userApproveGroupDbo.getUsername(),
                            userDTO.getDepartmentId(),
                            userDTO.getNickName()
                    );
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    /**
     * get approve group by approve group id
     * @param approveGroupId id of approve group
     * @return approve group dto
     */
    @Transactional(rollbackFor = Exception.class)
    public ApproveGroupDTO getApproveGroupByApproveGroupId(String approveGroupId){
        Optional<ApproveGroupDbo> group = approveGroupRepo.findValidApproveGroupDboByApproveGroupId(approveGroupId);

        return group
                .map(approveGroupDbo -> new ApproveGroupDTO(
                            approveGroupDbo.getId(),
                            approveGroupDbo.getApproveGroupName(),
                            approveGroupDbo.getDescription(),
                            listUserApproveGroupDtoByApproveGroupId(approveGroupId))
                )
                .orElseThrow(() -> new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.APPROVE_GROUP_NOT_FOUND, approveGroupId));
    }

    public List<String> getUserListByTaskNodeId(String taskId){
        List<String> approvalGroupIdList = taskApproveGroupRepo.findValidApproveGroupIdByTaskId(taskId)
                .stream()
                .map(TaskApproveGroupDbo::getApproveGroupId)
                .collect(Collectors.toList());
        return userApproveGroupRepo.findValidUserApproveGroupByApproveGroupId(approvalGroupIdList)
                .stream()
                .map(UserApproveGroupDbo::getUsername)
                .collect(Collectors.toList());
    }

    public Collection<ApproveGroupDTO> toApproveGroupDTO(Collection<ApproveGroupDbo> approveGroupDbos){
        if(CollectionUtils.isEmpty(approveGroupDbos)){
            return Sets.newHashSet();
        }
        return approveGroupDbos.stream()
                .map(this::toApproveGroupDTO)
                .collect(Collectors.toSet());
    }

    public ApproveGroupDTO toApproveGroupDTO(ApproveGroupDbo approveGroupDbo){
        return new ApproveGroupDTO(
                approveGroupDbo.getId()
                , approveGroupDbo.getApproveGroupName()
                , approveGroupDbo.getDescription()
                , listUserApproveGroupDtoByApproveGroupId(approveGroupDbo.getId())
        );
    }
}

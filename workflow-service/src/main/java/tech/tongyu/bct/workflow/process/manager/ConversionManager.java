package tech.tongyu.bct.workflow.process.manager;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import tech.tongyu.bct.workflow.exceptions.ReturnMessageAndTemplateDef;
import tech.tongyu.bct.workflow.exceptions.WorkflowCommonException;
import tech.tongyu.bct.workflow.process.repo.ProcessRepo;
import tech.tongyu.bct.workflow.process.repo.TaskApproveGroupRepo;
import tech.tongyu.bct.workflow.process.repo.TaskNodeRepo;
import tech.tongyu.bct.workflow.process.repo.UserApproveGroupRepo;
import tech.tongyu.bct.workflow.process.repo.entities.ProcessDbo;
import tech.tongyu.bct.workflow.process.repo.entities.TaskApproveGroupDbo;
import tech.tongyu.bct.workflow.process.repo.entities.TaskNodeDbo;
import tech.tongyu.bct.workflow.process.repo.entities.UserApproveGroupDbo;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author david.yang
 *  - mailto: yangyiwei@tongyu.tech
 */
@Component
public class ConversionManager {

    private TaskApproveGroupRepo taskApproveGroupRepo;
    private UserApproveGroupRepo userApproveGroupRepo;
    private TaskNodeRepo taskNodeRepo;
    private ProcessRepo processRepo;

    @Autowired
    public ConversionManager(
            TaskApproveGroupRepo taskApproveGroupRepo
            , UserApproveGroupRepo userApproveGroupRepo
            , TaskNodeRepo taskNodeRepo
            , ProcessRepo processRepo) {
        this.taskApproveGroupRepo = taskApproveGroupRepo;
        this.userApproveGroupRepo = userApproveGroupRepo;
        this.taskNodeRepo = taskNodeRepo;
        this.processRepo = processRepo;
    }

    public Collection<? extends GrantedAuthority> conversionByUsername(String username){
        //获取全部审批组
        Collection<UserApproveGroupDbo> userApproveGroup =
                userApproveGroupRepo.findValidUserApproveGroupByUsername(username);
        if (Objects.isNull(userApproveGroup)){
            return Lists.newArrayList();
        }
        //获取全部审批组关联任务的group并赋予
        Set<String> approveGroupIdList = userApproveGroup
                .stream().map(UserApproveGroupDbo::getApproveGroupId)
                .collect(Collectors.toSet());
        List<TaskApproveGroupDbo> taskApproveGroupDbo =
                taskApproveGroupRepo.findValidTaskApproveGroupDboByApproveGroupId(approveGroupIdList);
        if (Objects.isNull(taskApproveGroupDbo)){
            return Lists.newArrayList();
        }
        List<String> list = taskApproveGroupDbo.stream().map(TaskApproveGroupDbo::getTaskNodeId).collect(Collectors.toList());
        Collection<TaskNodeDbo> nodeDbos = taskNodeRepo.findValidTaskNodeDboById(list);

        return nodeDbos
                .stream().map(node ->
                    node.getCandidateGroup(
                            processRepo.findById(node.getProcessId())
                            .orElseThrow(() -> new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.UNKNOWN_PROCESS_NAME, node.getProcessId()))
                                    .getProcessName())
                )
                .map(roleName -> (GrantedAuthority) () -> "GROUP_" + roleName)
                .collect(Collectors.toSet());
    }
}

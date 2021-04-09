package tech.tongyu.bct.workflow.process.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.workflow.process.repo.entities.TaskApproveGroupDbo;

import java.util.Collection;
import java.util.List;

/**
 * @author 勇斌
 * @author david.yang
 *  - mailto: yangyiwei@tongyu.tech
 */
public interface TaskApproveGroupRepo extends JpaRepository<TaskApproveGroupDbo, String> {

    /**
     * find all valid task - approve group relations by given approve group id list
     * @param approveGroupId -> list of approve group id
     * @return -> list of task - approve group relations
     */
    @Query("from TaskApproveGroupDbo u where u.revoked = false and u.approveGroupId in ?1")
    List<TaskApproveGroupDbo> findValidTaskApproveGroupDboByApproveGroupId(Collection<String> approveGroupId);

    /**
     * find all task - approve group relations by given task node id list
     * @param taskNodeId -> list of task node id
     * @return -> list of task - approve group relations
     */
    @Query("from TaskApproveGroupDbo u where u.revoked = false and u.taskNodeId in ?1")
    List<TaskApproveGroupDbo> findTaskApproveGroupDboByTaskId(String taskNodeId);

    /**
     * find all valid task - approve group relations by the given list of task nodes' ids
     * @param taskNodeId -> list of task nodes' ids
     * @return -> list of task - approve group relations
     */
    @Query("from TaskApproveGroupDbo u where u.revoked = false and u.taskNodeId in ?1")
    List<TaskApproveGroupDbo> findValidTaskApproveGroupDboByTaskId(Collection<String> taskNodeId);

    /**
     * find all valid task - approve group relations by the given taskNodeId
     * @param taskNodeId task node's id
     * @return collection of approve group id
     */
    @Query("from TaskApproveGroupDbo t where t.revoked = false and t.taskNodeId = ?1")
    Collection<TaskApproveGroupDbo> findValidApproveGroupIdByTaskId(String taskNodeId);

    /**
     * deletion of task - approve group relation by given taskNodeId
     * @param taskNodeId -> id of task node
     */
    @Transactional(rollbackFor = Exception.class)
    @Modifying
    @Query("delete from TaskApproveGroupDbo t where t.taskNodeId = ?1")
    void deleteAllByTaskNodeId(String taskNodeId);

    /**
     * deletion of task - approve group relation by given taskNodeId
     * @param taskNodeId -> list of id of task node
     */
    @Transactional(rollbackFor = Exception.class)
    @Modifying
    @Query("delete from TaskApproveGroupDbo t where t.taskNodeId in (?1)")
    void deleteAllByTaskNodeIn(Collection<String> taskNodeId);

    /**
     * false deletion of task - approve group relation
     * @param approveGroupId -> id of approve group
     */
    @Transactional(rollbackFor = Exception.class)
    @Modifying
    @Query(value = "update TaskApproveGroupDbo u set u.revoked = true where u.approveGroupId = ?1")
    void deleteValidTaskApproveGroupDboByApproveGroupId(String approveGroupId);

    /**
     * count task - approve group relation by id of approve group
     * @param approveGroupId -> id of approve group
     * @return count
     */
    @Query(value = "select count(t) from TaskApproveGroupDbo t where t.revoked = false and t.approveGroupId in ?1")
    Integer countValidTaskApproveGroupDboByApproveGroupId(Collection<String> approveGroupId);

    /**
     * count task - task node relation by id of task node
     * @param taskNodeId -> id of task node
     * @return count
     */
    @Query(value = "select count(t) from TaskApproveGroupDbo t where t.revoked = false and t.taskNodeId = ?1")
    Integer countValidTaskApproveGroupDboByTaskNodeId(String taskNodeId);

}

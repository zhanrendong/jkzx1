package tech.tongyu.bct.workflow.process.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.workflow.process.repo.entities.TaskNodeDbo;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * @author david.yang
 *  - mailto: yangyiwei@tongyu.tech
 */
public interface TaskNodeRepo extends JpaRepository<TaskNodeDbo, String> {

    /**
     * list all valid taskNode by processId
     * @param processId -> process's id
     * @return -> list of taskNode
     */
    @Query("from TaskNodeDbo u where u.revoked = false and u.processId = ?1")
    Collection<TaskNodeDbo> findValidTaskNodeDboByProcessId(String processId);

    /**
     * get valid taskNode by taskNodeId
     * @param id -> task node's id
     * @return -> taskNodeDbo
     */
    @Query("from TaskNodeDbo u where u.revoked = false and u.id = ?1")
    Optional<TaskNodeDbo> findValidTaskNodeDboById(String id);

    /**
     * get valid taskNode by processId and sequence
     * @param processId -> process id
     * @param sequence -> task node sequence
     * @return -> taskNodeDbo
     */
    @Query("from TaskNodeDbo u where u.revoked = false and u.processId = ?1 and u.sequence = ?2")
    Optional<TaskNodeDbo> findValidTaskNodeDboByProcessIdAndSequence(String processId, Integer sequence);

    /**
     * list all valid taskNode by taskNodeId
     * @param taskNodeId -> task node's id
     * @return -> taskNodeDbo
     */
    @Query("from TaskNodeDbo u where u.revoked = false and u.id in (?1)")
    Collection<TaskNodeDbo> findValidTaskNodeDboById(Collection<String> taskNodeId);

    /**
     * delete all taskNode with given processId
     * @param processId -> process's id
     */
    @Transactional(rollbackFor = Exception.class)
    @Modifying
    void deleteAllByProcessId(String processId);
}

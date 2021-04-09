package tech.tongyu.bct.workflow.process.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.workflow.process.repo.entities.ProcessTriggerDbo;

import java.util.Collection;

/**
 * @author yongbin
 * - mailto: wuyongbin@tongyu.tech
 */
public interface ProcessTriggerRepo extends JpaRepository<ProcessTriggerDbo, String> {

    /**
     * false deletion of trigger
     * @param triggerId id of trigger
     */
    @Transactional(rollbackFor = Exception.class)
    @Modifying
    @Query(value = "update ProcessTriggerDbo p set p.revoked = true where p.triggerId = ?1")
    void deleteValidProcessTriggerDboByTriggerId(String triggerId);

    /**
     * false deletion of process trigger
     * @param processId id of process
     */
    @Transactional(rollbackFor = Exception.class)
    @Modifying
    @Query(value = "update ProcessTriggerDbo p set p.revoked = true where p.processId = ?1")
    void deleteValidProcessTriggerDboByProcessId(String processId);

    /**
     * get valid process trigger by process id
     * @param processId id of process
     * @return
     */
    @Query(value = "from ProcessTriggerDbo p where p.revoked = false and p.processId = ?1")
    Collection<ProcessTriggerDbo> findValidProcessTriggerDboByProcessId(String processId);

    /**
     * get valid process trigger by trigger id
     * @param triggerId id of trigger
     * @return
     */
    @Query(value = "from ProcessTriggerDbo p where p.revoked = false and p.triggerId = ?1")
    Collection<ProcessTriggerDbo> findValidProcessTriggerDboByTriggerId(String triggerId);
}

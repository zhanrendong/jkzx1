package tech.tongyu.bct.workflow.process.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.workflow.process.repo.entities.TriggerDbo;

import java.util.Collection;
import java.util.Optional;

/**
 * @author yongbin
 * - mailto: wuyongbin@tongyu.tech
 */

public interface TriggerRepo extends JpaRepository<TriggerDbo, String> {

    /**
     * get valid trigger by trigger id
     * @param triggerId id of trigger
     * @return
     */
    @Query(value = "from TriggerDbo t where t.revoked = false and t.id = ?1")
    Optional<TriggerDbo> findValidTriggerDboByTriggerId(String triggerId);

    /**
     * get valid trigger by trigger name
     * @param triggerName name of trigger
     * @return
     */
    @Query(value = "from TriggerDbo t where t.revoked = false and t.triggerName = ?1")
    Optional<TriggerDbo> findValidTriggerDboByTriggerName(String triggerName);

    /**
     * false deletion of trigger
     * @param triggerId id of trigger
     */
    @Transactional(rollbackFor = Exception.class)
    @Modifying
    @Query(value = "update TriggerDbo t set t.revoked = true where t.id = ?1")
    void deleteValidTriggerDboByTriggerId(String triggerId);

    /**
     * get valid trigger
     * @return
     */
    @Query(value = "from TriggerDbo t where t.revoked = false")
    Collection<TriggerDbo> findValidTriggerDbo();

    /**
     * get valid trigger
     * @return
     */
    @Query(value = "from TriggerDbo t where t.revoked = false and t.id in ?1")
    Collection<TriggerDbo> findValidTriggerDboByTriggerId(Collection<String> triggerIds);
}

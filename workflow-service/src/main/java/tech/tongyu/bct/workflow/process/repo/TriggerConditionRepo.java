package tech.tongyu.bct.workflow.process.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.workflow.process.repo.entities.ConditionDbo;
import tech.tongyu.bct.workflow.process.repo.entities.TriggerConditionDbo;

import java.util.Collection;
import java.util.Optional;

/**
 * @author yongbin
 * - mailto: wuyongbin@tongyu.tech
 */

public interface TriggerConditionRepo extends JpaRepository<TriggerConditionDbo, String> {

    /**
     * false deletion of trigger
     * @param triggerId trigger of id
     */
    @Transactional(rollbackFor = Exception.class)
    @Modifying
    @Query(value = "update TriggerConditionDbo t set t.revoked = true where t.triggerId = ?1")
    void deleteValidTriggerConditionDboByTriggerId(String triggerId);

    /**
     * false deletion of condition
     * @param conditionId condition of id
     */
    @Transactional(rollbackFor = Exception.class)
    @Modifying
    @Query(value = "update TriggerConditionDbo t set t.revoked = true where t.conditionId = ?1")
    void deleteValidTriggerConditionDboByConditionId(String conditionId);

    /**
     * get valid trigger and condition by trigger id
     * @param triggerId id of trigger
     * @return
     */
    @Query(value = "from TriggerConditionDbo t where t.revoked = false and t.triggerId in (?1)")
    Collection<TriggerConditionDbo> findValidTriggerConditionDboByTriggerId(Collection<String> triggerId);
}

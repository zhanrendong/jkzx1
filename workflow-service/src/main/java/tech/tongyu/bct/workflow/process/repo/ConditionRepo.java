package tech.tongyu.bct.workflow.process.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.workflow.process.repo.entities.ConditionDbo;

import java.util.Collection;
import java.util.Optional;

/**
 * @author yongbin
 * - mailto: wuyongbin@tongyu.tech
 */

public interface ConditionRepo extends JpaRepository<ConditionDbo, String> {

    /**
     * get valid condition by condition id
     * @param conditionId id of condition
     * @return
     */
    @Query(value = "from ConditionDbo c where c.revoked = false and c.id = ?1")
    Optional<ConditionDbo> findValidConditionDboByConditionId(String conditionId);

    /**
     * get valid condition by condition id
     * @param conditionIds id of condition
     * @return
     */
    @Query(value = "from ConditionDbo c where c.revoked = false and c.id in ?1")
    Collection<ConditionDbo> findValidConditionDboByConditionId(Collection<String> conditionIds);

    /**
     * get valid condition
     * @return
     */
    @Query(value = "from ConditionDbo c where c.revoked = false")
    Collection<ConditionDbo> findValidConditionDbo();

    /**
     * false deletion of condition
     * @param conditionId id of condition
     */
    @Transactional(rollbackFor = Exception.class)
    @Modifying
    @Query(value = "update ConditionDbo t set t.revoked = true where t.id = ?1")
    void deleteValidConditionDboByConditionId(String conditionId);

    /**
     * false deletion of condition
     * @param conditionIds id of condition
     */
    @Transactional(rollbackFor = Exception.class)
    @Modifying
    @Query(value = "update ConditionDbo t set t.revoked = true where t.id in ?1")
    void deleteValidConditionDboByConditionId(Collection<String> conditionIds);

}

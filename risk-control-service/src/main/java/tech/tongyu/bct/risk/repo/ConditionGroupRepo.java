package tech.tongyu.bct.risk.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.risk.repo.entities.ConditionGroupDbo;

import java.util.Collection;
import java.util.Optional;

/**
 * @author yongbin
 * - mailto: wuyongbin@tongyu.tech
 */
public interface ConditionGroupRepo extends JpaRepository<ConditionGroupDbo, String> {

    /**
     * get valid condition group by condition group id
     * @param conditionGroupId id of conditionGroup
     * @return
     */
    @Query(value = "from ConditionGroupDbo t where t.revoked = false and t.id = ?1")
    Optional<ConditionGroupDbo> findValidConditionGroupDboByConditionGroupId(String conditionGroupId);

    /**
     * get valid condition group by condition group name
     * @param conditionGroupName name of conditionGroup
     * @return
     */
    @Query(value = "from ConditionGroupDbo t where t.revoked = false and t.conditionGroupName = ?1")
    Optional<ConditionGroupDbo> findValidConditionGroupDboByConditionGroupName(String conditionGroupName);

    /**
     * false deletion of condition group
     * @param conditionGroupId id of conditionGroup
     */
    @Transactional(rollbackFor = Exception.class)
    @Modifying
    @Query(value = "update ConditionGroupDbo t set t.revoked = true where t.id = ?1")
    void deleteValidConditionGroupDboByConditionGroupId(String conditionGroupId);

    /**
     * get valid condition group
     * @return
     */
    @Query(value = "from ConditionGroupDbo t where t.revoked = false")
    Collection<ConditionGroupDbo> findValidConditionGroupDbo();

    /**
     * get valid condition group
     * @return
     */
    @Query(value = "from ConditionGroupDbo t where t.revoked = false and t.id in ?1")
    Collection<ConditionGroupDbo> findValidConditionGroupDboByConditionGroupId(Collection<String> conditionGroupIds);
}

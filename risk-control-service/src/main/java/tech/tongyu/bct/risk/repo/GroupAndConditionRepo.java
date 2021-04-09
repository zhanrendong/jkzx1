package tech.tongyu.bct.risk.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.risk.repo.entities.GroupAndConditionDbo;

import java.util.Collection;

/**
 * @author yongbin
 * - mailto: wuyongbin@tongyu.tech
 */

public interface GroupAndConditionRepo extends JpaRepository<GroupAndConditionDbo, String> {

    /**
     * false deletion of condition group
     * @param conditionGroupId condition group of id
     */
    @Transactional(rollbackFor = Exception.class)
    @Modifying
    @Query(value = "update GroupAndConditionDbo t set t.revoked = true where t.conditionGroupId = ?1")
    void deleteValidGroupAndConditionDboByConditionGroupId(String conditionGroupId);

    /**
     * false deletion of condition
     * @param conditionId condition of id
     */
    @Transactional(rollbackFor = Exception.class)
    @Modifying
    @Query(value = "update GroupAndConditionDbo t set t.revoked = true where t.conditionId = ?1")
    void deleteValidGroupAndConditionDboByConditionId(String conditionId);

    /**
     * get valid condition group and condition by condition group id
     * @param conditionGroupIds id of condition group
     * @return
     */
    @Query(value = "from GroupAndConditionDbo t where t.revoked = false and t.conditionGroupId in (?1)")
    Collection<GroupAndConditionDbo> findValidGroupAndConditionDboByConditionGroupId(Collection<String> conditionGroupIds);
}

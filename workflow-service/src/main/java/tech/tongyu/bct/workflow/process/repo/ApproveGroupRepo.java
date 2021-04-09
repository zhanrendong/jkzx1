package tech.tongyu.bct.workflow.process.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.workflow.process.repo.entities.ApproveGroupDbo;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * @author yongbin
 * @author david.yang
 *  - mailto: yangyiwei@tongyu.tech
 */
public interface ApproveGroupRepo extends JpaRepository<ApproveGroupDbo, String> {

    /**
     * get valid approve group by approve group name
     * @param approveGroupName -> name of approve group
     * @return -> optional of approve group
     */
    @Query(value = "from ApproveGroupDbo u where u.revoked = false and u.approveGroupName = ?1")
    Optional<ApproveGroupDbo> findValidApproveGroupDboByApproveGroupName(String approveGroupName);

    /**
     * get valid approve group by approve group id
     * @param approveGroupId -> id of approve group
     * @return -> optional of approve group
     */
    @Query(value = "from ApproveGroupDbo u where u.revoked = false and u.id = ?1")
    Optional<ApproveGroupDbo> findValidApproveGroupDboByApproveGroupId(String approveGroupId);

    /**
     * false deletion of approve group
     * @param id -> id of approve group
     */
    @Transactional(rollbackFor = Exception.class)
    @Modifying
    @Query(value = "update ApproveGroupDbo a set a.revoked = true where a.id = ?1")
    void deleteValidApproveGroupDboById(String id);

    /**
     * get all valid approve group
     * @return list of approve group
     */
    @Query("from ApproveGroupDbo u where u.revoked = false")
    Collection<ApproveGroupDbo> findValidApproveGroup();

    /**
     * list all valid approve group by given list of approve group id
     * @param ids -> list of approve group id
     * @return -> list of approve group
     */
    @Query("from ApproveGroupDbo u where u.revoked = false and u.id in (?1)")
    Collection<ApproveGroupDbo> findValidApproveGroupById(Collection<String> ids);
}

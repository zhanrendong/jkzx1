package tech.tongyu.bct.workflow.process.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.workflow.process.repo.entities.UserApproveGroupDbo;

import java.util.Collection;
import java.util.List;

/**
 * @author 勇斌
 * @author  david.yang
 *  - mailto: yangyiwei@tongyu.tech
 */
public interface UserApproveGroupRepo extends JpaRepository<UserApproveGroupDbo, String> {

    /**
     * false deletion of user - approve group relation
     * @param approveGroupId -> approve group
     */
    @Transactional(rollbackFor = Exception.class)
    @Modifying
    @Query(value = "update UserApproveGroupDbo u set u.revoked = true where u.approveGroupId = ?1")
    void deleteValidUserApproveGroupDboByApproveGroupId(String approveGroupId);

    /**
     * find all valid user - approve group relation
     * @return list user - approve group relation
     */
    @Query("from UserApproveGroupDbo u where u.revoked = false")
    Collection<UserApproveGroupDbo> findValidUserApproveGroup();

    /**
     * find all valid user - approve group relation by specified approveGroupId
     * @param approveGroupIdList -> approve group list
     * @return list user - approve group relation
     */
    @Query("from UserApproveGroupDbo u where u.revoked = false and u.approveGroupId in ?1")
    Collection<UserApproveGroupDbo> findValidUserApproveGroupByApproveGroupId(Collection<String> approveGroupIdList);

    /**
     * find all valid user - approve group relation by specified approveGroupId
     * @param approveGroupId -> approve group
     * @return list user - approve group relation
     */
    @Query("from UserApproveGroupDbo u where u.revoked = false and u.approveGroupId = ?1")
    Collection<UserApproveGroupDbo> findValidUserApproveGroupByApproveGroupId(String approveGroupId);

    /**
     * find all valid user - approve group relation by specified username
     * @param username -> username
     * @return list user - approve group relation
     */
    @Query("from UserApproveGroupDbo u where u.revoked = false and u.username = ?1")
    Collection<UserApproveGroupDbo> findValidUserApproveGroupByUsername(String username);
}

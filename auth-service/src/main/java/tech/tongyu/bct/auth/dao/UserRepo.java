package tech.tongyu.bct.auth.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.auth.dao.entity.UserDbo;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserRepo extends JpaRepository<UserDbo, String> {

    @Query("from UserDbo u where u.username = ?1 and u.revoked = false")
    Optional<UserDbo> findValidUserByUsername(String username);

    @Query(value = "from UserDbo u where u.revoked = false and u.username <> 'admin' order by u.createTime asc")
    List<UserDbo> findAllValidUser();

    @Transactional
    @Modifying
    @Query(value = "update UserDbo user set user.locked = false" +
            " , user.expired = false" +
            " , user.passwordExpiredTimestamp = ?1" +
            " , user.timesOfLoginFailure = 0 " +
            " where user.revoked = false ")
    void resetLockedAndExpiredOfAllValidUser(Timestamp timestamp);

    @Query("from UserDbo u where u.revoked = false and u.id = ?1")
    Optional<UserDbo> findValidUserById(String id);

    @Query("select count(u) from UserDbo u where u.revoked = false and u.username = ?1")
    Integer countValidUserByUsername(String username);

    @Query("select count(u) from UserDbo u join u.departmentDbo d where u.revoked = false and u.id <> ?1 and u.username = ?2 and d.id = ?3")
    Integer countValidUserByOtherIdAndUsernameAndDepartmentId(String userId, String username, String departmentId);

    @Query("select count(u) from UserDbo u where u.revoked = false and u.id = ?1")
    Integer countValidUserByUserId(String userId);

    @Query("select u from UserDbo u join u.departmentDbo d where u.revoked = false and d.id in (?1)")
    Collection<UserDbo> findValidUserByDepartmentId(Collection<String> departmentId);

}

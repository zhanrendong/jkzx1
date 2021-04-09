package tech.tongyu.bct.auth.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tech.tongyu.bct.auth.dao.entity.DepartmentDbo;

import java.util.Collection;
import java.util.Optional;

public interface DepartmentRepo extends JpaRepository<DepartmentDbo, String> {

    @Query("select count(d) from DepartmentDbo d where d.revoked = false and d.id = ?1")
    Integer countValidDepartmentById(String id);

    @Query("select d from DepartmentDbo d where d.revoked = false and d.id = ?1")
    Optional<DepartmentDbo> findValidDepartmentById(String id);

    @Query("select d from DepartmentDbo d where d.revoked = false and d.id in (?1)")
    Collection<DepartmentDbo> findValidDepartmentById(Collection<String> id);

    @Query("select d from DepartmentDbo d where d.revoked = false  and d.parentId = ?1")
    Collection<DepartmentDbo> findValidDepartmentByParentId(String parentId);

    @Query("select d from DepartmentDbo d where d.revoked = false and d.departmentName = ?1 and d.parentId = ?2")
    Optional<DepartmentDbo> findValidDepartmentByDepartmentNameAndParentId(String departmentName, String parentId);

    @Query("select d from DepartmentDbo d where d.revoked = false and d.parentId is null")
    Optional<DepartmentDbo> findValidRootDepartment();

    @Query("select count(d) from DepartmentDbo d where d.revoked = false and d.parentId is null")
    Integer countRootDepartment();

    @Query("select d from DepartmentDbo d where d.revoked = false")
    Collection<DepartmentDbo> findAllValidDepartment();

}

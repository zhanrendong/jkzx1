package tech.tongyu.bct.auth.authaction.intel;

import tech.tongyu.bct.auth.dto.DepartmentDTO;
import tech.tongyu.bct.auth.dto.Department;


public interface DepartmentAuthAction {

    DepartmentDTO createDepartment(String departmentName, String departmentType, String description, String parentId, Integer sort);

    Department getDepartmentById(String departmentId);

    Department updateDepartment(String departmentId, String departmentName, String departmentType, String description);

    Department moveDepartment(String departmentId, String parentId);

}

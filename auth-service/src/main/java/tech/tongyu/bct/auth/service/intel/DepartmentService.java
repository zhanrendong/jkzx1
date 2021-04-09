package tech.tongyu.bct.auth.service.intel;

import tech.tongyu.bct.auth.dto.CompanyInfo;
import tech.tongyu.bct.auth.dto.Department;

public interface DepartmentService {

    /*------------------------ department related api  --------------------------*/
    Department authDepartmentCreate(String departmentName, String departmentType, String description, String parentId, Number sort);

    Department authDepartmentRemove(String departmentId);

    Department authDepartmentModify(String departmentId, String departmentName, String departmentType
            , String description);

    Department authDepartmentMove(String departmentId, String parentId);

    Department authDepartmentGet(String departmentId);

    Department authAllDepartmentGet();
    /*------------------------ department related api  --------------------------*/

    /*------------------------ company related api  --------------------------*/
    CompanyInfo authCompanyGet();
    CompanyInfo authCompanyCreate(String companyName, String companyType, String unifiedSocialCreditCode
            , String legalPerson, String contactEmail, String description);
    /*------------------------ company related api  --------------------------*/

}

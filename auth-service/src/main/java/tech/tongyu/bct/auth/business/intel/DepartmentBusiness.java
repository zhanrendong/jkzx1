package tech.tongyu.bct.auth.business.intel;

import tech.tongyu.bct.auth.dto.CompanyInfo;
import tech.tongyu.bct.auth.dto.Department;


public interface DepartmentBusiness {

    /*------------------------ department related  --------------------------*/

    /**
     * 创建部门
     * @param departmentName 部门名字
     * @param departmentType 部门类型
     * @param description 部门描述信息
     * @param parentId 部门父部门ID
     * @return  Department 返回被创建的部门信息
     * @see tech.tongyu.bct.auth.common.organization.Department
     * @throws tech.tongyu.bct.auth.exception.manager.ManagerException 如果在同一个父部门下存在同名的部门则会抛出异常
     * @throws tech.tongyu.bct.auth.exception.AuthorizationException 如果没有权限创建部门则会抛出异常
     */
    Department createDepartment(String departmentName, String departmentType, String description, String parentId, Integer sort);

    /**
     * 删除部门
     * @param departmentId 部门ID
     * @throws tech.tongyu.bct.auth.exception.AuthorizationException 如果没有权限删除部门则会抛出异常
     */
    void deleteDepartment(String departmentId);

    /**
     * 获取公司所有有权查看的部门信息
     * @return Department 有权限查看的部门树
     */
    Department getDepartment();

    /**
     * 获取某个特定部门ID对应的部门树信息
     * @param departmentId 部门ID
     * @return Department 部门树信息
     * @throws tech.tongyu.bct.auth.exception.AuthorizationException 如果没有该部门的权限会抛出异常
     */
    Department getDepartment(String departmentId);

    /**
     * 更新部门信息
     * @param departmentId 部门ID，用于查找想要改变的部门实体
     * @param departmentName 部门名字
     * @param departmentType 部门类型
     * @param description 部门描述信息
     * @return Department 部门树信息
     * @throws tech.tongyu.bct.auth.exception.AuthorizationException 如果没有该部门的修改权限则会抛出异常
     */
    Department updateDepartment(String departmentId, String departmentName, String departmentType
            , String description);

    /**
     * 将部门移动到部门树的另一个位置
     * @param departmentId 部门ID
     * @param parentId 新的部门父节点
     * @return Department 部门树信息
     * @throws tech.tongyu.bct.auth.exception.AuthorizationException 如果没有该部门的移动权限以及新父部门的移入权限则会抛出异常
     * @throws tech.tongyu.bct.auth.exception.manager.ManagerException 如果移动的是根节点则会抛出异常
     */
    Department moveDepartment(String departmentId, String parentId);

    /*------------------------ department related  --------------------------*/

    /*------------------------ company related  --------------------------*/

    /**
     * 创建公司信息
     * @param companyName 公司名
     * @param companyType 公司类型
     * @param unifiedSocialCreditCode 统一社会信用代码
     * @param legalPerson 法人
     * @param contactEmail 联系email
     * @param description 公司描述信息
     * @return CompanyInfo 公司信息
     * @throws tech.tongyu.bct.auth.exception.manager.ManagerException 如果已经创建公司信息则会报错，公司信息应该只有一条
     * @throws tech.tongyu.bct.auth.exception.AuthorizationException 如果没有创建公司信息的权限
     */
    CompanyInfo createCompanyInfo(String companyName, String companyType, String unifiedSocialCreditCode, String legalPerson, String contactEmail, String description);

    /**
     * 修改公司信息
     * @param companyName 公司名
     * @param companyType 公司类型
     * @param unifiedSocialCreditCode 统一社会信用代码
     * @param legalPerson 法人
     * @param contactEmail 联系email
     * @param description 公司描述信息
     * @return CompanyInfo 公司信息
     * @throws tech.tongyu.bct.auth.exception.AuthorizationException 如果没有修改公司信息的权限
     */
    CompanyInfo updateCompanyInfo(String companyName, String companyType, String unifiedSocialCreditCode, String legalPerson, String contactEmail, String description);

    /**
     * 用于删除公司信息
     * @return 删除公司信息
     * @throws tech.tongyu.bct.auth.exception.AuthorizationException 如果没有删除公司信息的权限
     */
    CompanyInfo deleteCompanyInfo();

    /**
     * 用于获取公司信息
     * @return CompanyInfo 公司信息
     */
    CompanyInfo getCompanyInfo();
    /*------------------------ company related  --------------------------*/

}

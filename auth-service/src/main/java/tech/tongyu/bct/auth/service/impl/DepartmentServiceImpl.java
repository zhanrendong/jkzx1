package tech.tongyu.bct.auth.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.auth.business.intel.DepartmentBusiness;
import tech.tongyu.bct.auth.dto.CompanyInfo;
import tech.tongyu.bct.auth.dto.Department;
import tech.tongyu.bct.auth.service.ApiParamConstants;
import tech.tongyu.bct.auth.service.DepartmentService;
import tech.tongyu.bct.auth.utils.CommonUtils;
import tech.tongyu.bct.common.api.annotation.BctMethodArg;
import tech.tongyu.bct.common.api.annotation.BctMethodInfo;
import tech.tongyu.bct.common.util.IntegerUtils;

import java.util.HashMap;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    private DepartmentBusiness departmentBusiness;

    @Autowired
    public DepartmentServiceImpl(
            DepartmentBusiness departmentBusiness){
        this.departmentBusiness = departmentBusiness;
    }

    /*------------------------ department related api  --------------------------*/
    @Override
    @BctMethodInfo(
            retName = ApiParamConstants.DEPARTMENT
            , retDescription = "部门信息"
            , description = "创建部门"
            , returnClass = Department.class
            , service = "auth-service"
    )
    @Transactional
    public Department authDepartmentCreate(
            @BctMethodArg(name = ApiParamConstants.DEPARTMENT_NAME, description = "部门名称") String departmentName
            , @BctMethodArg(name = ApiParamConstants.DEPARTMENT_TYPE, description = "部门类型") String departmentType
            , @BctMethodArg(required = false, name = ApiParamConstants.DESCRIPTION, description = "描述") String description
            , @BctMethodArg(name = ApiParamConstants.PARENT_ID, description = "父部门ID") String parentId
            , @BctMethodArg(name = ApiParamConstants.SORT, description = "排序") Number sort) {

        CommonUtils.checkBlankParam(new HashMap<String, String>() {{
            put(ApiParamConstants.DEPARTMENT_NAME, departmentName);
            put(ApiParamConstants.DEPARTMENT_TYPE, departmentType);
            put(ApiParamConstants.PARENT_ID, parentId);
        }});

        departmentBusiness.createDepartment(departmentName, departmentType, description, parentId, IntegerUtils.num2Integer(sort));
        return departmentBusiness.getDepartment();
    }

    @Override
    @BctMethodInfo(
            retName = ApiParamConstants.DEPARTMENT
            , retDescription = "部门信息"
            , description = "删除部门，需要删除部门权限(DELETE_DEPARTMENT)"
            , returnClass = Department.class
            , service = "auth-service"
    )
    @Transactional
    public Department authDepartmentRemove(
            @BctMethodArg(name = ApiParamConstants.DEPARTMENT_ID, description = "部门ID") String departmentId) {
        CommonUtils.checkBlankParam(new HashMap<String, String>() {{
            put(ApiParamConstants.DEPARTMENT_ID, departmentId);
        }});

        departmentBusiness.deleteDepartment(departmentId);
        return departmentBusiness.getDepartment();
    }

    @Override
    @BctMethodInfo(
            retName = ApiParamConstants.DEPARTMENT
            , retDescription = "部门信息"
            , description = "更新部门，需要更新部门权限(UPDATE_DEPARTMENT)"
            , returnClass = Department.class
            , service = "auth-service"
    )
    @Transactional
    public Department authDepartmentModify(
            @BctMethodArg(name = ApiParamConstants.DEPARTMENT_ID, description = "部门ID") String departmentId,
            @BctMethodArg(name = ApiParamConstants.DEPARTMENT_NAME, description = "部门名称") String departmentName,
            @BctMethodArg(name = ApiParamConstants.DEPARTMENT_TYPE, description = "部门类型") String departmentType,
            @BctMethodArg(required = false, name = ApiParamConstants.DESCRIPTION, description = "部门描述") String description) {

        CommonUtils.checkBlankParam(new HashMap<String, String>() {{
            put(ApiParamConstants.DEPARTMENT_ID, departmentId);
            put(ApiParamConstants.DEPARTMENT_NAME, departmentName);
            put(ApiParamConstants.DEPARTMENT_TYPE, departmentType);
        }});

        departmentBusiness.updateDepartment(departmentId, departmentName, departmentType, description);
        return departmentBusiness.getDepartment();
    }

    @Override
    @BctMethodInfo(
            retName = ApiParamConstants.DEPARTMENT
            , retDescription = "部门信息"
            , description = "移动部门，需要对应资源的创建与删除权限，如：部门的创建权限(CREATE_NAMESPACE)与删除权限(DELETE_NAMESPACE)"
            , returnClass = Department.class
            , service = "auth-service"
    )
    @Transactional
    public Department authDepartmentMove(
            @BctMethodArg(name = ApiParamConstants.DEPARTMENT_ID, description = "部门ID") String departmentId,
            @BctMethodArg(name = ApiParamConstants.PARENT_ID, description = "父部门ID") String parentId) {

        CommonUtils.checkBlankParam(new HashMap<String, String>() {{
            put(ApiParamConstants.DEPARTMENT_ID, departmentId);
            put(ApiParamConstants.PARENT_ID, parentId);
        }});

        return departmentBusiness.moveDepartment(departmentId, parentId);
    }

    @Override
    @BctMethodInfo(
            retName = ApiParamConstants.DEPARTMENT,
            retDescription = "部门信息",
            description = "根据部门ID获取部门树",
            returnClass = Department.class,
            service = "auth-service"
    )
    @Transactional
    public Department authDepartmentGet(
            @BctMethodArg(name = ApiParamConstants.DEPARTMENT_ID, description = "部门ID") String departmentId) {

        CommonUtils.checkBlankParam(new HashMap<String, String>() {{
            put(ApiParamConstants.DEPARTMENT_ID, departmentId);
        }});

        return departmentBusiness.getDepartment(departmentId);
    }

    @Override
    @BctMethodInfo(
            retName = ApiParamConstants.DEPARTMENT,
            retDescription = "部门信息",
            description = "获取全部部门",
            returnClass = Department.class,
            service = "auth-service"
    )
    @Transactional
    public Department authAllDepartmentGet(){
        return departmentBusiness.getDepartment();
    }

    /*------------------------ department related api  --------------------------*/

    /*------------------------ company related api  --------------------------*/
    @Override
    @BctMethodInfo(
            retDescription = ApiParamConstants.COMPANY_INFO
            , retName = "公司信息"
            , description = "创建公司信息，需要创建部门权限(CREATE_DEPARTMENT)"
            , returnClass = CompanyInfo.class
            , service = "auth-service"
    )
    @Transactional
    public CompanyInfo authCompanyCreate(
            @BctMethodArg(name = ApiParamConstants.COMPANY_NAME, description = "公司名称") String companyName,
            @BctMethodArg(name = ApiParamConstants.COMPANY_TYPE, description = "公司类型") String companyType,
            @BctMethodArg(name = ApiParamConstants.UNIFIED_SOCIAL_CREDIT_CODE, description = "统一社会信用代码") String unifiedSocialCreditCode,
            @BctMethodArg(name = ApiParamConstants.LEGAL_PERSON, description = "法人") String legalPerson,
            @BctMethodArg(name = ApiParamConstants.CONTACT_EMAIL, description = "联系邮箱") String contactEmail,
            @BctMethodArg(name = ApiParamConstants.DESCRIPTION, description = "公司描述信息") String description) {

        CommonUtils.checkBlankParam(new HashMap<String, String>() {{
            put(ApiParamConstants.COMPANY_NAME, companyName);
            put(ApiParamConstants.COMPANY_TYPE, companyType);
            put(ApiParamConstants.UNIFIED_SOCIAL_CREDIT_CODE, unifiedSocialCreditCode);
            put(ApiParamConstants.LEGAL_PERSON, legalPerson);
            put(ApiParamConstants.CONTACT_EMAIL, contactEmail);
        }});

        return departmentBusiness.createCompanyInfo(companyName, companyType, unifiedSocialCreditCode, legalPerson, contactEmail, description);
    }

    @BctMethodInfo(
            retDescription = ApiParamConstants.COMPANY_INFO
            , retName = "公司信息"
            , description = "更新公司信息，需要更新部门权限(UPDATE_DEPARTMENT)"
            , returnClass = CompanyInfo.class
            , service = "auth-service"
    )
    @Transactional
    public CompanyInfo authCompanyUpdate(
            @BctMethodArg(name = ApiParamConstants.COMPANY_NAME, description = "公司名称") String companyName,
            @BctMethodArg(name = ApiParamConstants.COMPANY_TYPE, description = "公司类型") String companyType,
            @BctMethodArg(name = ApiParamConstants.UNIFIED_SOCIAL_CREDIT_CODE, description = "统一社会信用代码") String unifiedSocialCreditCode,
            @BctMethodArg(name = ApiParamConstants.LEGAL_PERSON, description = "法人") String legalPerson,
            @BctMethodArg(name = ApiParamConstants.CONTACT_EMAIL, description = "联系邮箱") String contactEmail,
            @BctMethodArg(name = ApiParamConstants.DESCRIPTION, description = "公司描述信息") String description) {

        CommonUtils.checkBlankParam(new HashMap<String, String>() {{
            put(ApiParamConstants.COMPANY_NAME, companyName);
            put(ApiParamConstants.COMPANY_TYPE, companyType);
            put(ApiParamConstants.UNIFIED_SOCIAL_CREDIT_CODE, unifiedSocialCreditCode);
            put(ApiParamConstants.LEGAL_PERSON, legalPerson);
            put(ApiParamConstants.CONTACT_EMAIL, contactEmail);
        }});

        return departmentBusiness.updateCompanyInfo(companyName, companyType, unifiedSocialCreditCode, legalPerson, contactEmail, description);
    }

    @Override
    @BctMethodInfo(
            retDescription = ApiParamConstants.COMPANY_INFO
            , retName = "公司信息"
            , description = "获取公司信息"
            , returnClass = CompanyInfo.class
            , service = "auth-service"
    )
    @Transactional
    public CompanyInfo authCompanyGet(){
        return departmentBusiness.getCompanyInfo();
    }
    /*------------------------ company related api  --------------------------*/

}

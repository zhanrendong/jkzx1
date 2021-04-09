package tech.tongyu.bct.auth.manager;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.auth.dao.CompanyRepo;
import tech.tongyu.bct.auth.dao.DepartmentRepo;
import tech.tongyu.bct.auth.dao.ResourceRepo;
import tech.tongyu.bct.auth.dao.entity.CompanyDbo;
import tech.tongyu.bct.auth.dao.entity.DepartmentDbo;
import tech.tongyu.bct.auth.dto.CompanyInfo;
import tech.tongyu.bct.auth.dto.Department;
import tech.tongyu.bct.auth.dto.DepartmentDTO;
import tech.tongyu.bct.auth.dto.DepartmentWithResourceDTO;
import tech.tongyu.bct.auth.exception.AuthServiceException;
import tech.tongyu.bct.auth.exception.ReturnMessageAndTemplateDef;
import tech.tongyu.bct.auth.exception.manager.ManagerException;
import tech.tongyu.bct.auth.manager.converter.ConverterUtils;
import tech.tongyu.bct.common.util.CollectionUtils;
import tech.tongyu.bct.common.util.tree.TreeEntity;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class DepartmentManager {

    private DepartmentRepo departmentRepo;
    private CompanyRepo companyRepo;
    private ResourceRepo resourceRepo;

    @Autowired
    public DepartmentManager(
            DepartmentRepo departmentRepo
            , CompanyRepo companyRepo
            , ResourceRepo resourceRepo){
        this.departmentRepo = departmentRepo;
        this.companyRepo = companyRepo;
        this.resourceRepo = resourceRepo;
    }

    public Department getCompanyDepartment(){
        return TreeEntity.fromRecords(departmentRepo.findAllValidDepartment(), (departmentDbo, parent) -> {
            DepartmentDbo dbo = (DepartmentDbo) departmentDbo;
            return new Department(departmentDbo.getId(), departmentDbo.getSort(), parent
                    , dbo.getDepartmentName(), dbo.getDepartmentType(), dbo.getDescription());
        });
    }

    public DepartmentDTO getDepartmentByDepartmentNameAndParentId(String departmentName, String parentId){
        if(Objects.isNull(parentId))
            return departmentRepo.findValidRootDepartment()
                    .map(ConverterUtils::getDepartmentDTO)
                    .orElseThrow(() -> new ManagerException(ReturnMessageAndTemplateDef.Errors.MISSING_DEPARTMENT, String.format("%s:%s", parentId, departmentName)));

        return departmentRepo.findValidDepartmentByDepartmentNameAndParentId(departmentName, parentId)
                .map(ConverterUtils::getDepartmentDTO)
                .orElseThrow(() -> new ManagerException(ReturnMessageAndTemplateDef.Errors.MISSING_DEPARTMENT, String.format("%s:%s", parentId, departmentName)));
    }

    public Optional<String> getCompanyDepartmentId(){
        return departmentRepo.findValidRootDepartment()
                .map(DepartmentDbo::getId);
    }

    public Boolean hasRootDepartment(){
        return departmentRepo.countRootDepartment() > 0;
    }

    public Boolean hasDepartment(String departmentName, String parentId){
        if(Objects.isNull(parentId)){
            return departmentRepo.countRootDepartment() > 0;
        }

        Collection<String> departmentNameSet = departmentRepo.findValidDepartmentByParentId(parentId)
                .stream()
                .map(DepartmentDbo::getDepartmentName)
                .collect(Collectors.toSet());

        return CollectionUtils.contains(departmentNameSet, departmentName);
    }

    private void checkSameDepartmentNameInSameParent(String parentId, String departmentName){
        if(hasDepartment(departmentName, parentId)) {
            throw new ManagerException(ReturnMessageAndTemplateDef.Errors.EXISTING_SAME_NAME_DEPARTMENT, parentId, departmentName);
        }
    }

    @Transactional
    public DepartmentDTO createDepartment(String departmentName, String departmentType, String description, String parentId, Integer sort) {
        DepartmentDbo departmentDbo = new DepartmentDbo(departmentName, departmentType, description, parentId, sort);
        checkSameDepartmentNameInSameParent(parentId, departmentName);
        return ConverterUtils.getDepartmentDTO(departmentRepo.save(departmentDbo));
    }

    @Transactional
    public void deleteDepartment(String departmentId) {
        departmentRepo.findValidDepartmentById(departmentId)
                .map(departmentDbo -> {
                    departmentDbo.setRevoked(true);
                    return departmentRepo.save(departmentDbo);
                })
                .orElseThrow(() -> new ManagerException(ReturnMessageAndTemplateDef.Errors.MISSING_DEPARTMENT, departmentId));
    }

    public Department getDepartment(String departmentId) {
        DepartmentDbo departmentDto = departmentRepo.findValidDepartmentById(departmentId).orElseThrow(
                () -> new AuthServiceException(ReturnMessageAndTemplateDef.Errors.MISSING_DEPARTMENT, departmentId));
        Department department = new Department(departmentDto.getId(), departmentDto.getSort(), departmentDto.getDepartmentName(), departmentDto.getDepartmentType(), departmentDto.getDescription());

        return TreeEntity.fromRecords(department, Lists.newArrayList(department), departmentRepo.findAllValidDepartment(), (departmentDbo, parent) -> {
            DepartmentDbo dbo = (DepartmentDbo) departmentDbo;
            return new Department(departmentDbo.getId(), departmentDbo.getSort(), parent
                    , dbo.getDepartmentName(), dbo.getDepartmentType(), dbo.getDescription());
        });
    }

    public DepartmentDTO getDepartmentDTO(String departmentId) {
        return departmentRepo.findValidDepartmentById(departmentId)
                .map(ConverterUtils::getDepartmentDTO)
                .orElseThrow(() -> new ManagerException(ReturnMessageAndTemplateDef.Errors.MISSING_DEPARTMENT, departmentId));
    }

    public DepartmentWithResourceDTO getDepartmentWithResource(String departmentId){
        return departmentRepo.findValidDepartmentById(departmentId)
                .map(ConverterUtils::getDepartmentWithResourceDto)
                .orElseThrow(() -> new ManagerException(ReturnMessageAndTemplateDef.Errors.MISSING_DEPARTMENT, departmentId));
    }

    @Transactional
    public DepartmentDTO updateDepartment(String departmentId, String departmentName, String departmentType, String description) {
        return departmentRepo.findValidDepartmentById(departmentId)
                .map(departmentDbo -> {
                    departmentDbo.setDepartmentName(departmentName);
                    departmentDbo.setDepartmentType(departmentType);
                    departmentDbo.setDescription(description);
                    return departmentRepo.save(departmentDbo);
                })
                .map(ConverterUtils::getDepartmentDTO)
                .orElseThrow(() -> new ManagerException(ReturnMessageAndTemplateDef.Errors.MISSING_DEPARTMENT, departmentId));
    }

    @Transactional
    public DepartmentDTO moveDepartment(String departmentId, String parentId) {
        return departmentRepo.findValidDepartmentById(departmentId)
                .map(departmentDbo -> {
                    checkSameDepartmentNameInSameParent(parentId, departmentDbo.getDepartmentName());
                    departmentDbo.setParentId(parentId);
                    return departmentRepo.save(departmentDbo);
                })
                .map(ConverterUtils::getDepartmentDTO)
                .orElseThrow(() -> new ManagerException(ReturnMessageAndTemplateDef.Errors.MISSING_DEPARTMENT, departmentId));
    }

    public CompanyInfo createCompanyInfo(String companyName, String companyType, String unifiedSocialCreditCode, String legalPerson, String contactEmail, String description) {
        if(companyRepo.count() == 0){
            return ConverterUtils.getCompanyInfo(companyRepo.save(new CompanyDbo(
                    companyName
                    , companyType
                    , description
                    , unifiedSocialCreditCode
                    , legalPerson
                    , contactEmail
            )));
        }
        throw new ManagerException(ReturnMessageAndTemplateDef.Errors.MULTIPLE_COMPANY_INFO);
    }

    public CompanyInfo deleteCompanyInfo() {
        List<CompanyDbo> companyInfo = companyRepo.findAll();
        if (companyInfo.size() == 0) {
            throw new ManagerException(ReturnMessageAndTemplateDef.Errors.MISSING_COMPANY_INFO);
        }

        return ConverterUtils.getCompanyInfo(companyInfo.get(0));
    }

    public CompanyInfo updateCompanyInfo(String companyName, String companyType, String unifiedSocialCreditCode, String legalPerson, String contactEmail, String description) {
        List<CompanyDbo> companyDbos = companyRepo.findAll();
        if(companyDbos.size() > 1)
            throw new ManagerException(ReturnMessageAndTemplateDef.Errors.MULTIPLE_COMPANY_INFO);

        if(companyDbos.size() == 0)
            throw new ManagerException(ReturnMessageAndTemplateDef.Errors.MISSING_COMPANY_INFO);

        CompanyDbo companyDbo = companyDbos.get(0);
        companyDbo.setCompanyName(companyName);
        companyDbo.setCompanyType(companyType);
        companyDbo.setUnifiedSocialCreditCode(unifiedSocialCreditCode);
        companyDbo.setLegalPerson(legalPerson);
        companyDbo.setContactEmail(contactEmail);
        companyDbo.setDescription(description);
        return ConverterUtils.getCompanyInfo(companyRepo.save(companyDbo));
    }

    public Boolean hasCompanyInfo(){
        return companyRepo.count() > 0;
    }

    public CompanyInfo getCompanyInfo() {
        List<CompanyDbo> companyDbos = companyRepo.findAll();
        if(companyDbos.size() > 1)
            throw new ManagerException(ReturnMessageAndTemplateDef.Errors.MULTIPLE_COMPANY_INFO);
        if(companyDbos.size() == 0)
            throw new ManagerException(ReturnMessageAndTemplateDef.Errors.MISSING_COMPANY_INFO);
        return ConverterUtils.getCompanyInfo(companyDbos.get(0));
    }

    public void linkDepartmentAndResource(String departmentId, String resourceId){
        departmentRepo.findValidDepartmentById(departmentId)
                .map(departmentDbo -> resourceRepo.findValidResourceById(resourceId)
                        .map(resourceDbo -> {
                            departmentDbo.setResource(resourceDbo);
                            resourceDbo.setDepartmentId(departmentDbo.getId());
                            resourceRepo.save(resourceDbo);
                            return departmentRepo.save(departmentDbo);
                        })
                        .orElseThrow(() -> new ManagerException(ReturnMessageAndTemplateDef.Errors.MISSING_RESOURCE, resourceId)))
                .orElseThrow(() -> new ManagerException(ReturnMessageAndTemplateDef.Errors.MISSING_DEPARTMENT, departmentId));
    }

    public Collection<DepartmentDTO> listAllDepartment() {
        return departmentRepo.findAll().stream()
                .map(ConverterUtils::getDepartmentDTO)
                .collect(Collectors.toSet());
    }

    public Collection<DepartmentDTO> listDepartmentByDepartmentId(Collection<String> departmentId){
        if(CollectionUtils.isEmpty(departmentId))
            return Sets.newHashSet();
        return departmentRepo.findValidDepartmentById(departmentId)
                .stream()
                .map(ConverterUtils::getDepartmentDTO)
                .collect(Collectors.toSet());
    }
}

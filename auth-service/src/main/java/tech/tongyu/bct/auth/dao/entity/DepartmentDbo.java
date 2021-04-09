package tech.tongyu.bct.auth.dao.entity;

import tech.tongyu.bct.auth.dao.common.BaseEntity;
import tech.tongyu.bct.common.util.tree.PlainTreeRecord;

import javax.persistence.*;

@Entity
@Table(schema = EntityConstants.AUTH_SERVICE,
        name = EntityConstants.AUTH_DEPARTMENT, indexes = {
        @Index(name = EntityConstants.INDEX_DEPARTMENT__DEPARTMENT_NAME, columnList = EntityConstants.DEPARTMENT_NAME)
        , @Index(name = EntityConstants.INDEX_DEPARTMENT__DEPARTMENT_TYPE, columnList = EntityConstants.DEPARTMENT_TYPE)})
public class DepartmentDbo extends BaseEntity implements PlainTreeRecord {

    @Column(name = EntityConstants.DEPARTMENT_NAME)
    private String departmentName;

    @Column(name = EntityConstants.SORT)
    private Integer sort;

    @Column(name = EntityConstants.DEPARTMENT_TYPE)
    private String departmentType;

    @Column(name = EntityConstants.DESCRIPTION)
    private String description;

    @Column(name = EntityConstants.PARENT_ID)
    private String parentId;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = EntityConstants.RESOURCE_ID, referencedColumnName = EntityConstants.ID)
    private ResourceDbo resource;

    public DepartmentDbo(){ super();}

    public DepartmentDbo(String departmentName, String departmentType, String description
            , String parentId, Integer sort){
        super();
        this.departmentName = departmentName;
        this.departmentType = departmentType;
        this.description = description;
        this.parentId = parentId;
        this.sort = sort;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getDepartmentType() {
        return departmentType;
    }

    public void setDepartmentType(String departmentType) {
        this.departmentType = departmentType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public ResourceDbo getResource() {
        return resource;
    }

    public void setResource(ResourceDbo resource) {
        this.resource = resource;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }
}

package tech.tongyu.bct.auth.dto;

import com.google.common.collect.Sets;
import tech.tongyu.bct.common.api.doc.BctField;
import tech.tongyu.bct.common.util.CollectionUtils;
import tech.tongyu.bct.common.util.tree.TreeEntity;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class Department extends TreeEntity<Department> {

    @BctField(
            name = "departmentName",
            description = "部门名称",
            type = "String",
            order = 1
    )
    private String departmentName;
    @BctField(
            name = "departmentType",
            description = "部门类型",
            type = "String",
            order = 2
    )
    private String departmentType;
    @BctField(
            name = "description",
            description = "描述",
            type = "String",
            order = 3
    )
    private String description;

    public Set<String> getAllChildrenDepartmentId(){
        Set<String> childrenDepartmentId = Sets.newHashSet();
        Set<Department> children = getChildren();
        if(CollectionUtils.isNotEmpty(children)){
            children.stream()
                    .peek(department -> {
                        childrenDepartmentId.add(department.getId());
                        Set<String> grandChildrenDepartmentId = department.getAllChildrenDepartmentId();
                        if(CollectionUtils.isNotEmpty(grandChildrenDepartmentId)) {
                            childrenDepartmentId.addAll(grandChildrenDepartmentId);
                        }
                    })
                    .collect(Collectors.toSet());
        }
        return childrenDepartmentId;
    }

    public Department(String id, Integer sort, String departmentName, String departmentType, String description){
        super(id, sort);
        this.departmentName = departmentName;
        this.departmentType = departmentType;
        this.description = description;
    }

    public Department(String id, Integer sort, Department parent
            , String departmentName, String departmentType, String description){
        this(id, sort, departmentName, departmentType, description);
        super.setParent(parent);
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

    @Override
    public Department getParent(){
        return super.getParent();
    }
}

package tech.tongyu.bct.auth.dto;

import tech.tongyu.bct.common.api.doc.BctField;
import tech.tongyu.bct.common.util.tree.PlainTreeRecord;

public class PageComponentDTO implements PlainTreeRecord {

    @BctField(
            name = "id",
            description = "页面ID",
            type = "String",
            order = 1
    )
    private String id;

    @BctField(
            name = "pageName",
            description = "页面名称",
            type = "String",
            order = 2
    )
    private String pageName;

    @BctField(
            name = "sort",
            description = "排序",
            type = "Integer",
            order = 3
    )
    private Integer sort;

    @BctField(
            name = "parentId",
            description = "父页面ID",
            type = "String",
            order = 4
    )
    private String parentId;

    public PageComponentDTO(String id, String pageName, Integer sort, String parentId) {
        this.id = id;
        this.pageName = pageName;
        this.sort = sort;
        this.parentId = parentId;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public String getPageName() {
        return pageName;
    }

    public void setPageName(String pageName) {
        this.pageName = pageName;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }
}

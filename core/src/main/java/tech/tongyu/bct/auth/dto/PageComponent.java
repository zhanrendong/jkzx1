package tech.tongyu.bct.auth.dto;

import tech.tongyu.bct.common.api.doc.BctField;
import tech.tongyu.bct.common.util.tree.TreeEntity;

public class PageComponent extends TreeEntity<PageComponent> {

    @BctField(
            name = "pageName",
            description = "页面名称",
            type = "String",
            order = 1
    )
    private String pageName;

    public PageComponent(String id, Integer sort, PageComponent parent, String pageName) {
        super(id, sort, parent);
        this.pageName = pageName;
    }

    public PageComponent(String id, Integer sort, String pageName) {
        super(id, sort);
        this.pageName = pageName;
    }

    public String getPageName() {
        return pageName;
    }

    public void setPageName(String pageName) {
        this.pageName = pageName;
    }
}


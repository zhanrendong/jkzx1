package tech.tongyu.bct.auth.dao.entity;

import tech.tongyu.bct.auth.dao.common.BaseEntity;
import tech.tongyu.bct.common.util.tree.PlainTreeRecord;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(schema = EntityConstants.AUTH_SERVICE,
        name = EntityConstants.AUTH_PAGE_COMPONENT)
public class PageComponentDbo extends BaseEntity implements Serializable, PlainTreeRecord {

    @Column(name = EntityConstants.PAGE_NAME, unique = true)
    private String pageName;

    @Column(name = EntityConstants.SORT)
    private Integer sort;

    @Column(name = EntityConstants.PARENT_ID)
    private String parentId;

    public PageComponentDbo() {
        super();
    }

    public PageComponentDbo(String pageName, Integer sort, String parentId) {
        this.pageName = pageName;
        this.sort = sort;
        this.parentId = parentId;
    }

    public String getPageName() {
        return pageName;
    }

    public void setPageName(String pageName) {
        this.pageName = pageName;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }
}

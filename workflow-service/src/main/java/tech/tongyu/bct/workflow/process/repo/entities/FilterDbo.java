package tech.tongyu.bct.workflow.process.repo.entities;

import tech.tongyu.bct.workflow.process.enums.FilterTypeEnum;
import tech.tongyu.bct.workflow.process.enums.TaskTypeEnum;
import tech.tongyu.bct.workflow.process.repo.common.BaseEntity;

import javax.persistence.*;

/**
 * @author david.yang
 *  - mailto: yangyiwei@tongyu.tech
 */
@Entity
@Table(schema = EntityConstants.SCHEMA, name = EntityConstants.TABLE_NAME_$FILTER)
public class FilterDbo extends BaseEntity {

    public FilterDbo() {
    }

    public FilterDbo(String filterName, String filterClass, FilterTypeEnum filterType) {
        this.filterName = filterName;
        this.filterClass = filterClass;
        this.filterType = filterType;
    }

    @Column
    private String filterName;

    @Column
    private String filterClass;

    @Column
    @Enumerated(value = EnumType.ORDINAL)
    private FilterTypeEnum filterType;

    public String getFilterName() {
        return filterName;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    public String getFilterClass() {
        return filterClass;
    }

    public void setFilterClass(String filterClass) {
        this.filterClass = filterClass;
    }

    public FilterTypeEnum getFilterType() {
        return filterType;
    }

    public void setFilterType(FilterTypeEnum filterType) {
        this.filterType = filterType;
    }

}

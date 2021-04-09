package tech.tongyu.bct.risk.repo.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author yongbin
 * - mailto: wuyongbin@tongyu.tech
 */
@Entity
@Table(schema = EntityConstants.SCHEMA, name = EntityConstants.TABlE_NAME_$CONDITION_GROUP_AND_CONDITION)
public class GroupAndConditionDbo extends BaseEntity {

    public GroupAndConditionDbo() {
    }

    public GroupAndConditionDbo(String conditionGroupId, String conditionId) {
        this.conditionGroupId = conditionGroupId;
        this.conditionId = conditionId;
    }

    @Column
    private String conditionGroupId;

    @Column
    private String conditionId;

    public String getConditionGroupId() {
        return conditionGroupId;
    }

    public void setConditionGroupId(String conditionGroupId) {
        this.conditionGroupId = conditionGroupId;
    }

    public String getConditionId() {
        return conditionId;
    }

    public void setConditionId(String conditionId) {
        this.conditionId = conditionId;
    }
}

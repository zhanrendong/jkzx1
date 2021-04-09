package tech.tongyu.bct.workflow.process.repo.entities;

import tech.tongyu.bct.workflow.process.enums.SymbolEnum;
import tech.tongyu.bct.workflow.process.repo.common.BaseEntity;

import javax.persistence.*;

/**
 * @author yongbin
 * - mailto: wuyongbin@tongyu.tech
 */
@Entity
@Table(schema = EntityConstants.SCHEMA, name = EntityConstants.TABlE_NAME_$CONDITION)
public class ConditionDbo extends BaseEntity {

    public ConditionDbo() {
    }

    public ConditionDbo(String description, String leftIndex, String rightIndex,
                        String leftIndexValue, String rightIndexValue, SymbolEnum symbolEnum) {
        this.description = description;
        this.leftIndex = leftIndex;
        this.rightIndex = rightIndex;
        this.leftIndexValue = leftIndexValue;
        this.rightIndexValue = rightIndexValue;
        this.symbolEnum = symbolEnum;
    }

    @Column
    private String description;

    @Column
    private String leftIndex;

    @Column
    private String rightIndex;

    @Column
    private String leftIndexValue;

    @Column
    private String rightIndexValue;

    @Column
    @Enumerated(EnumType.STRING)
    private SymbolEnum symbolEnum;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLeftIndex() {
        return leftIndex;
    }

    public void setLeftIndex(String leftIndex) {
        this.leftIndex = leftIndex;
    }

    public String getRightIndex() {
        return rightIndex;
    }

    public void setRightIndex(String rightIndex) {
        this.rightIndex = rightIndex;
    }

    public SymbolEnum getSymbolEnum() {
        return symbolEnum;
    }

    public void setSymbolEnum(SymbolEnum symbolEnum) {
        this.symbolEnum = symbolEnum;
    }

    public String getLeftIndexValue() {
        return leftIndexValue;
    }

    public void setLeftIndexValue(String leftIndexValue) {
        this.leftIndexValue = leftIndexValue;
    }

    public String getRightIndexValue() {
        return rightIndexValue;
    }

    public void setRightIndexValue(String rightIndexValue) {
        this.rightIndexValue = rightIndexValue;
    }
}

package tech.tongyu.bct.document.dto;

import tech.tongyu.bct.common.api.doc.BctField;

import java.time.Instant;
import java.time.LocalDate;

public class PositionDocumentDTO {

    @BctField(
            name = "uuid",
            description = "交易文档唯一标识",
            type = "String",
            order = 1
    )
    private String uuid;
    @BctField(
            name = "positionId",
            description = "持仓编号",
            type = "String",
            order = 2
    )
    private String positionId;
    @BctField(
            name = "tradeId",
            description = "交易编号",
            type = "String",
            order = 3
    )
    private String tradeId;
    @BctField(
            name = "bookName",
            description = "交易簿名称",
            type = "String",
            order = 4
    )
    private String bookName;
    @BctField(
            name = "partyName",
            description = "交易对手名称",
            type = "String",
            order = 5
    )
    private String partyName;
    @BctField(
            name = "salesName",
            description = "销售名称",
            type = "String",
            order = 6
    )
    private String salesName;
    @BctField(
            name = "tradeEmail",
            description = "交易邮箱地址",
            type = "String",
            order = 7
    )
    private String tradeEmail;
    @BctField(
            name = "expirationDate",
            description = "到期日期",
            type = "LocalDate",
            order = 8
    )
    private LocalDate expirationDate;
    @BctField(
            name = "docProcessStatus",
            description = "通知书处理状态",
            type = "DocProcessStatusEnum",
            componentClass = DocProcessStatusEnum.class,
            order = 9
    )
    private DocProcessStatusEnum docProcessStatus;
    @BctField(
            name = "createdAt",
            description = "创建时间",
            type = "Instant",
            order = 10
    )
    private Instant createdAt;
    @BctField(
            name = "updatedAt",
            description = "更新时间",
            type = "Instant",
            order = 11
    )
    private Instant updatedAt;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getPositionId() {
        return positionId;
    }

    public void setPositionId(String positionId) {
        this.positionId = positionId;
    }

    public String getTradeId() {
        return tradeId;
    }

    public void setTradeId(String tradeId) {
        this.tradeId = tradeId;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getPartyName() {
        return partyName;
    }

    public void setPartyName(String partyName) {
        this.partyName = partyName;
    }

    public String getSalesName() {
        return salesName;
    }

    public void setSalesName(String salesName) {
        this.salesName = salesName;
    }

    public String getTradeEmail() {
        return tradeEmail;
    }

    public void setTradeEmail(String tradeEmail) {
        this.tradeEmail = tradeEmail;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public DocProcessStatusEnum getDocProcessStatus() {
        return docProcessStatus;
    }

    public void setDocProcessStatus(DocProcessStatusEnum docProcessStatus) {
        this.docProcessStatus = docProcessStatus;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}

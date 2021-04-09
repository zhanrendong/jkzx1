package tech.tongyu.bct.document.poi;

import tech.tongyu.bct.common.api.doc.BctField;
import tech.tongyu.bct.document.ext.dto.DocTypeEnum;

import java.time.Instant;
import java.util.UUID;

public class PoiTemplateDTO {

    @BctField(
            name = "uuid",
            description = "模板文档唯一标识",
            type = "UUID",
            order = 1
    )
    private UUID uuid;

    @BctField(
            name = "tradeType",
            description = "交易类型",
            type = "TradeTypeEnum",
            componentClass = TradeTypeEnum.class,
            order = 2
    )
    private TradeTypeEnum tradeType;

    @BctField(
            name = "docType",
            description = "文档类型",
            type = "DocTypeEnum",
            componentClass = DocTypeEnum.class,
            order = 3
    )
    private DocTypeEnum docType;

    @BctField(
            name = "typeSuffix",
            description = "文档类型对应的后缀名",
            type = "String",
            order = 4
    )
    private String typeSuffix;

    @BctField(
            name = "fileName",
            description = "文件名称",
            type = "String",
            order = 5
    )
    private String fileName;

    @BctField(
            name = "updatedAt",
            description = "更新时间",
            type = "Instant",
            order = 6
    )
    private Instant updatedAt;

    @BctField(
            name = "filePath",
            description = "文件路径",
            type = "String",
            order = 7
    )
    private String filePath;

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public PoiTemplateDTO(UUID uuid, TradeTypeEnum tradeType, DocTypeEnum docType, String typeSuffix, String fileName, Instant updatedAt, String filePath) {
        this.uuid = uuid;
        this.tradeType = tradeType;
        this.docType = docType;
        this.typeSuffix = typeSuffix;
        this.fileName = fileName;
        this.updatedAt = updatedAt;
        this.filePath = filePath;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public TradeTypeEnum getTradeType() {
        return tradeType;
    }

    public void setTradeType(TradeTypeEnum tradeType) {
        this.tradeType = tradeType;
    }

    public DocTypeEnum getDocType() {
        return docType;
    }

    public void setDocType(DocTypeEnum docType) {
        this.docType = docType;
    }

    public String getTypeSuffix() {
        return typeSuffix;
    }

    public void setTypeSuffix(String typeSuffix) {
        this.typeSuffix = typeSuffix;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}

package tech.tongyu.bct.document.ext.dto;

import tech.tongyu.bct.common.api.doc.BctField;

import java.time.Instant;
import java.util.UUID;

public class BctTemplateDTO {

    @BctField(
            name = "uuid",
            description = "模板文档唯一标识",
            type = "UUID",
            order = 1
    )
    private UUID uuid;

    @BctField(
            name = "directoryId",
            description = "模板文件夹唯一标识",
            type = "UUID",
            order = 2
    )
    private UUID directoryId;

    @BctField(
            name = "category",
            description = "大类：TRADE_TEMPLATE(交易模板), CLIENT_TEMPLATE(客户模板)",
            type = "CategoryEnum",
            componentClass = CategoryEnum.class,
            order = 3
    )
    private CategoryEnum category;

    @BctField(
            name = "transactType",
            description = "交易类型",
            type = "String",
            order = 4
    )
    private String transactType;

    @BctField(
            name = "docType",
            description = "文档类型",
            type = "DocTypeEnum",
            componentClass = DocTypeEnum.class,
            order = 5
    )
    private DocTypeEnum docType;

    @BctField(
            name = "fileType",
            description = "文件类型",
            type = "String",
            order = 6
    )
    private String fileType;

    @BctField(
            name = "typeSuffix",
            description = "文档类型对应的后缀名",
            type = "String",
            order = 7
    )
    private String typeSuffix;

    @BctField(
            name = "fileName",
            description = "文件名称",
            type = "String",
            order = 8
    )
    private String fileName;

    @BctField(
            name = "groupName",
            description = "字典组名称",
            type = "String",
            order = 9
    )
    private String groupName;

    @BctField(
            name = "description",
            description = "描述",
            type = "String",
            order = 10
    )
    private String description;

    @BctField(
            name = "createdAt",
            description = "创建时间",
            type = "Instant",
            order = 11
    )
    private Instant createdAt;

    @BctField(
            name = "updatedAt",
            description = "更新时间",
            type = "Instant",
            order = 12
    )
    private Instant updatedAt;

    @BctField(
            name = "createdBy",
            description = "创建者",
            type = "String",
            order = 13
    )
    private String createdBy;

    public BctTemplateDTO(UUID uuid) {
        this.uuid = uuid;
    }

    public BctTemplateDTO(UUID directoryId, CategoryEnum category, String transactType, DocTypeEnum docType,
                          String fileType, String typeSuffix, String description, String createdBy) {
        this.directoryId = directoryId;
        this.category = category;
        this.transactType = transactType;
        this.docType = docType;
        this.fileType = fileType;
        this.typeSuffix = typeSuffix;
        this.description = description;
        this.createdBy = createdBy;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getDirectoryId() {
        return directoryId;
    }

    public void setDirectoryId(UUID directoryId) {
        this.directoryId = directoryId;
    }

    public CategoryEnum getCategory() {
        return category;
    }

    public void setCategory(CategoryEnum category) {
        this.category = category;
    }

    public String getTransactType() {
        return transactType;
    }

    public void setTransactType(String transactType) {
        this.transactType = transactType;
    }

    public DocTypeEnum getDocType() {
        return docType;
    }

    public void setDocType(DocTypeEnum docType) {
        this.docType = docType;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}

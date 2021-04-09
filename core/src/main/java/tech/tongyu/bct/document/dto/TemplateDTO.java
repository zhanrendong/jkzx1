package tech.tongyu.bct.document.dto;

import tech.tongyu.bct.common.api.doc.BctField;

import java.time.Instant;
import java.util.UUID;

/**
 * Document template.
 *
 * any template must belong to specific template directory.
 * for specific directory, only one template is enabled.
 * @author hangzhi
 */
public class TemplateDTO {

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
            name = "name",
            description = "文件名",
            type = "String",
            order = 3
    )
    private String name;

    @BctField(
            name = "type",
            description = "文档类型",
            type = "DocTypeEnum",
            componentClass = DocTypeEnum.class,
            order = 4
    )
    private DocTypeEnum type;

    @BctField(
            name = "typeSuffix",
            description = "文档类型对应的后缀名",
            type = "String",
            order = 5
    )
    private String typeSuffix;

    @BctField(
            name = "fileName",
            description = "文件名称",
            type = "String",
            order = 6
    )
    private String fileName;

    @BctField(
            name = "description",
            description = "描述",
            type = "String",
            order = 7
    )
    private String description;

    @BctField(
            name = "enabled",
            description = "是否可用",
            type = "Boolean",
            order = 8
    )
    private Boolean enabled;

    @BctField(
            name = "createdAt",
            description = "创建时间",
            type = "Instant",
            order = 9
    )
    private Instant createdAt;

    @BctField(
            name = "updatedAt",
            description = "更新时间",
            type = "Instant",
            order = 10
    )
    private Instant updatedAt;

    @BctField(
            name = "createdBy",
            description = "创建人",
            type = "String",
            order = 11
    )
    private String createdBy;

    public TemplateDTO() {

    }

    public TemplateDTO(UUID directoryId, String name, DocTypeEnum type, String typeSuffix) {
        this.directoryId = directoryId;
        this.name = name;
        this.type = type;
        this.typeSuffix = typeSuffix;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DocTypeEnum getType() {
        return type;
    }

    public void setType(DocTypeEnum type) {
        this.type = type;
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

    public Boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
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
}

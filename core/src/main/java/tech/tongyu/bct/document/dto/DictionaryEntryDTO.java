package tech.tongyu.bct.document.dto;

import tech.tongyu.bct.common.api.doc.BctField;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * 字典项。
 * @author hangzhi
 */
public class DictionaryEntryDTO {

    @BctField(
            name = "uuid",
            description = "字典项唯一标识",
            type = "UUID",
            order = 1
    )
    private UUID uuid;

    @BctField(
            name = "dicGroup",
            description = "字典组",
            type = "String",
            order = 2
    )
    private String dicGroup;

    @BctField(
            name = "sourcePath",
            description = "源路径",
            type = "String",
            order = 3
    )
    private String sourcePath;

    @BctField(
            name = "destinationPath",
            description = "目的路径",
            type = "String",
            order = 4
    )
    private String destinationPath;

    @BctField(
            name = "type",
            description = "字典类型",
            type = "ValueTypeEnum",
            componentClass = ValueTypeEnum.class,
            order = 5
    )
    private ValueTypeEnum type;

    @BctField(
            name = "tags",
            description = "标识",
            type = "Set<String>",
            isCollection = true,
            order = 6
    )
    private Set<String> tags;

    @BctField(
            name = "description",
            description = "描述",
            type = "String",
            order = 7
    )
    private String description;

    @BctField(
            name = "defaultValue",
            description = "默认值",
            type = "String",
            order = 8
    )
    private String defaultValue;

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

    public DictionaryEntryDTO() {

    }

    public DictionaryEntryDTO(String group,
                              String sourcePath,
                              String destinationPath,
                              ValueTypeEnum type,
                              Set<String> tags,
                              String defaultValue) {
        this.dicGroup = group;
        this.sourcePath = sourcePath;
        this.destinationPath = destinationPath;
        this.type = type;
        this.tags = tags;
        this.defaultValue = defaultValue;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public String getDestinationPath() {
        return destinationPath;
    }

    public void setDestinationPath(String destinationPath) {
        this.destinationPath = destinationPath;
    }

    public ValueTypeEnum getType() {
        return type;
    }

    public void setType(ValueTypeEnum type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
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

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public String getDicGroup() {
        return dicGroup;
    }

    public void setDicGroup(String dicGroup) {
        this.dicGroup = dicGroup;
    }
}

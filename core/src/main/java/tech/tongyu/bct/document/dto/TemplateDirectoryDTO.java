package tech.tongyu.bct.document.dto;

import tech.tongyu.bct.common.api.doc.BctField;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * 模板文件夹（Directory): 用于管理一组模板
 *   １．一个＂模板文件夹＂有多个＂模板＂;
 *   ２．一个＂模板＂关联一个＂模板文件＂;
 *   ３．某一时刻，只有一个＂模板＂是enable = true 状态，
 *      并且该版本所属的模板文件将用于＂文件生成＂．
 */
public class TemplateDirectoryDTO {

    @BctField(
            name = "uuid",
            description = "模板文件夹唯一标识",
            type = "UUID",
            order = 1
    )
    private UUID uuid;

    @BctField(
            name = "name",
            description = "模板文件夹名称",
            type = "String",
            order = 2
    )
    private String name;

    @BctField(
            name = "tags",
            description = "模板文件夹标签，用于分类检索，业务划分等。",
            type = "Set<String>",
            order = 3
    )
    private Set<String> tags;

    @BctField(
            name = "description",
            description = "描述",
            type = "String",
            order = 4
    )
    private String description;

    @BctField(
            name = "templates",
            description = "在该文件夹下的模板列表",
            type = "List<TemplateDTO>",
            isCollection = true,
            componentClass = TemplateDTO.class,
            order = 5
    )
    private List<TemplateDTO> templates;

    @BctField(
            name = "createdAt",
            description = "创建时间",
            type = "Instant",
            order = 6
    )
    private Instant createdAt;

    @BctField(
            name = "updatedAt",
            description = "更新时间",
            type = "Instant",
            order = 7
    )
    private Instant updatedAt;

    @BctField(
            name = "createdBy",
            description = "创建者",
            type = "String",
            order = 8
    )
    private String createdBy;

    public TemplateDirectoryDTO() {
    }

    public TemplateDirectoryDTO(String name, Set<String> tags, String description) {
        this.name = name;
        this.tags = tags;
        this.description = description;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<TemplateDTO> getTemplates() {
        return templates;
    }

    public void setTemplates(List<TemplateDTO> templates) {
        this.templates = templates;
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

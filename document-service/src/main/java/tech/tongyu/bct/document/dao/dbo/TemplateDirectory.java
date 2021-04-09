package tech.tongyu.bct.document.dao.dbo;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import tech.tongyu.bct.document.service.DocumentService;

import javax.persistence.*;
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
 * @author hangzhi
 */
@Entity
@Table(schema = DocumentService.SCHEMA)
public class TemplateDirectory {
    /**
     * 模板文件夹唯一标识
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    /**
     * 模板文件夹名称
     */
    @Column(nullable = false)
    private String name;

    /**
     * 模板文件夹标签，用于分类检索，业务划分等。
     */
    @ElementCollection
    @CollectionTable(name = "DOC_DIR_TAG",
            joinColumns = @JoinColumn(referencedColumnName = "uuid") ,
            schema = DocumentService.SCHEMA)
    private Set<String> tags;

    /**
     * 描述
     */
    @Column
    private String description;

    /**
     * 在该文件夹下的模板列表
     */
    @OneToMany(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "directoryId")
    private List<Template> templates;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column
    private Instant createdAt;

    /**
     * 更新时间
     */
    @UpdateTimestamp
    @Column
    private Instant updatedAt;

    /**
     * 创建者
     */
    @Column
    private String createdBy;

    public TemplateDirectory() {

    }

    public TemplateDirectory(String name, Set<String> tags, String description) {
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

    public List<Template> getTemplates() {
        return templates;
    }

    public void setTemplates(List<Template> templates) {
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

    public Template findEnabledTemplate() {
        if (null == templates || templates.size() < 1) {
            return null;
        }

        return templates.stream().filter(t -> t.isEnabled()).findFirst().orElse(null);
    }
}

package tech.tongyu.bct.document.dao.dbo;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import tech.tongyu.bct.document.dto.DocTypeEnum;
import tech.tongyu.bct.document.service.DocumentService;

import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Document template.
 *
 * any template must belong to specific template directory.
 * for specific directory, only one template is enabled.
 * @author hangzhi
 */
@Entity
@Table(schema = DocumentService.SCHEMA)
public class Template {
    /**
     * template id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    /**
     *  id of directory which the template belongs to.(many to one)
     */
    @Column
    private UUID directoryId;

    /**
     * file content
     */
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private TemplateContent content;

    /**
     * the name of template.
     * this name can be used for showing and document name.
     */
    @Column(nullable = false)
    private String name;

    /**
     * document type.
     * Word2003 / Excel2003 etc.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocTypeEnum type;

    /**
     * the suffix corresponding to document type.
     * e.g. 'doc', 'xls' etc.
     */
    @Column(nullable = false)
    private String typeSuffix;

    /**
     * the file name of template when saving as file.
     */
    @Column
    private String fileName;

    /**
     * description
     */
    @Column
    private String description;

    /**
     * denote whether this template is activated.
     */
    @Column(nullable = false)
    private Boolean enabled;

    /**
     * the time creating this template.
     */
    @CreationTimestamp
    @Column
    private Instant createdAt;

    /**
     * the time updating this template.
     */
    @UpdateTimestamp
    @Column
    private Instant updatedAt;

    /**
     * 创建该模板的用户
     */
    @Column
    private String createdBy;

    public Template() {

    }

    public Template(UUID directoryId, String name, DocTypeEnum type, String typeSuffix, TemplateContent content) {
        this.directoryId = directoryId;
        this.name = name;
        this.type = type;
        this.typeSuffix = typeSuffix;
        this.content = content;
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

    public boolean isEnabled() {
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

    public TemplateContent getContent() {
        return content;
    }

    public void setContent(TemplateContent content) {
        this.content = content;
    }
}

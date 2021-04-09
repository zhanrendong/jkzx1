package tech.tongyu.bct.document.dao.dbo;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;
import tech.tongyu.bct.document.dto.ValueTypeEnum;
import tech.tongyu.bct.document.service.DocumentService;

import javax.persistence.*;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(schema = DocumentService.SCHEMA,
        uniqueConstraints = {@UniqueConstraint(columnNames = {"dicGroup", "destinationPath"})})
public class DictionaryEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column(nullable = false)
    private String dicGroup;

    @Column
    private String sourcePath;

    @Column(nullable = false)
    private String destinationPath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ValueTypeEnum type;

    @ElementCollection
    @CollectionTable(name = "DOC_DIC_TAG",
            joinColumns = @JoinColumn(referencedColumnName = "uuid"),
            schema = DocumentService.SCHEMA)
    private Set<String> tags;

    @Column
    private String description;

    /**
     * 默认值。 该值可能为base64编码后的图片，所以值可能非常大。
     */
    @Column
    @Type(type = "text")
    @Lob
    private String defaultValue;

    @CreationTimestamp
    @Column
    private Instant createdAt;

    @UpdateTimestamp
    @Column
    private Instant updatedAt;

    @Column
    private String createdBy;

    public DictionaryEntry() {

    }

    public DictionaryEntry(String group,
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

package tech.tongyu.bct.document.dao.dbo;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import tech.tongyu.bct.document.ext.dto.CategoryEnum;
import tech.tongyu.bct.document.ext.dto.DocTypeEnum;
import tech.tongyu.bct.document.service.DocumentService;

import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(schema = DocumentService.SCHEMA)
public class BctTemplate {
    /**
     * 模板文档唯一标识
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;
    /**
     * 模板文件夹唯一标识
     */
    @Column(nullable = false)
    private UUID directoryId;
    /**
     * 大类：TRADE_TEMPLATE(交易模板), CLIENT_TEMPLATE(客户模板)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoryEnum category;
    /**
     * 交易类型
     */
    @Column
    private String transactType;
    /**
     * 文档类型
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocTypeEnum docType;
    /**
     * 文件类型
     */
    @Column(nullable = false)
    private String fileType;
    /**
     * 文档类型对应的后缀名
     */
    @Column(nullable = false)
    private String typeSuffix;
    /**
     * 文件名称
     */
    @Column
    private String fileName;
    /**
     * 字典组名称
     */
    @Column
    private String groupName;
    /**
     * 描述
     */
    @Column
    private String description;
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

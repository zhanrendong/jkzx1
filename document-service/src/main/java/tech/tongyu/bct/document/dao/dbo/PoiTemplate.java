package tech.tongyu.bct.document.dao.dbo;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import tech.tongyu.bct.document.poi.TradeTypeEnum;
import tech.tongyu.bct.document.ext.dto.DocTypeEnum;
import tech.tongyu.bct.document.service.DocumentService;

import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(schema = DocumentService.SCHEMA)
public class PoiTemplate {

    /**
     * 模板文档唯一标识
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    /**
     * 交易类型
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TradeTypeEnum tradeType;

    /**
     * 文档类型
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocTypeEnum docType;

    /**
     * 文档类型对应的后缀名
     */
    @Column
    private String typeSuffix;

    /**
     * 文件名称
     */
    @Column
    private String fileName;

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
     * 文件路径
     */
    @Column
    private String filePath;

    public PoiTemplate() {
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public PoiTemplate(UUID uuid, TradeTypeEnum tradeType, DocTypeEnum docType, String typeSuffix, String fileName, String filePath) {
        this.uuid = uuid;
        this.tradeType = tradeType;
        this.docType = docType;
        this.typeSuffix = typeSuffix;
        this.fileName = fileName;
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

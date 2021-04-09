package tech.tongyu.bct.document.dao.dbo;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import tech.tongyu.bct.document.dto.DocProcessStatusEnum;
import tech.tongyu.bct.document.service.DocumentService;

import javax.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(schema = DocumentService.SCHEMA)
public class TradeDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;
    @Column
    private String tradeId;
    @Column
    private String bookName;
    @Column
    private String partyName;
    @Column
    private String salesName;
    @Column
    private String tradeEmail;
    @Column
    private LocalDate tradeDate;
    @Column
    @Enumerated(EnumType.STRING)
    private DocProcessStatusEnum docProcessStatus;
    @Column
    @CreationTimestamp
    private Instant createdAt;
    @Column
    @UpdateTimestamp
    private Instant updatedAt;

    public TradeDocument() {
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getTradeId() {
        return tradeId;
    }

    public void setTradeId(String tradeId) {
        this.tradeId = tradeId;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getPartyName() {
        return partyName;
    }

    public void setPartyName(String partyName) {
        this.partyName = partyName;
    }

    public String getSalesName() {
        return salesName;
    }

    public void setSalesName(String salesName) {
        this.salesName = salesName;
    }

    public String getTradeEmail() {
        return tradeEmail;
    }

    public void setTradeEmail(String tradeEmail) {
        this.tradeEmail = tradeEmail;
    }

    public LocalDate getTradeDate() {
        return tradeDate;
    }

    public void setTradeDate(LocalDate tradeDate) {
        this.tradeDate = tradeDate;
    }

    public DocProcessStatusEnum getDocProcessStatus() {
        return docProcessStatus;
    }

    public void setDocProcessStatus(DocProcessStatusEnum docProcessStatus) {
        this.docProcessStatus = docProcessStatus;
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

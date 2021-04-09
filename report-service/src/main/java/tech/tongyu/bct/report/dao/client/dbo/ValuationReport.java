package tech.tongyu.bct.report.dao.client.dbo;

import com.fasterxml.jackson.databind.JsonNode;
import org.hibernate.annotations.Type;
import tech.tongyu.bct.common.jpa.JsonConverter;
import tech.tongyu.bct.report.service.EodReportService;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(schema = EodReportService.SCHEMA)
public class ValuationReport {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;
    @Column
    private String legalName;
    @Column
    private LocalDate valuationDate;
    @Column(precision=19,scale=4)
    private BigDecimal price;

    @Convert(converter = JsonConverter.class)
    @Column(nullable = false, columnDefinition = "text")
    private JsonNode content;

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getLegalName() {
        return legalName;
    }

    public void setLegalName(String legalName) {
        this.legalName = legalName;
    }

    public LocalDate getValuationDate() {
        return valuationDate;
    }

    public void setValuationDate(LocalDate valuationDate) {
        this.valuationDate = valuationDate;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public JsonNode getContent() {
        return content;
    }

    public void setContent(JsonNode content) {
        this.content = content;
    }
}

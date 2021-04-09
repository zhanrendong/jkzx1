package tech.tongyu.bct.report.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import tech.tongyu.bct.common.api.doc.BctField;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class ValuationReportDTO {
    //UUID
    @BctField(name = "uuid", description = "唯一标识", type = "UUID")
    private UUID uuid;
    //交易对手
    @BctField(name = "legalName", description = "交易对手", type = "String")
    private String legalName;
    //估值日
    @BctField(name = "valuationDate", description = "估值日", type = "LocalDate")
    private LocalDate valuationDate;
    @BctField(name = "masterAgreementId", description = "SAC协议编码", type = "String")
    private String masterAgreementId;
    @BctField(name = "tradeEmail", description = "交易邮箱", type = "String")
    private String tradeEmail;
    //估值
    @BctField(name = "price", description = "估值", type = "String")
    private BigDecimal price;
    //生成文档的参数内容
    @BctField(name = "content", description = "生成文档的参数内容", type = "JsonNode")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private JsonNode content;

    public ValuationReportDTO() {
    }

    public ValuationReportDTO(UUID uuid, String legalName, LocalDate valuationDate, BigDecimal price) {
        this.uuid = uuid;
        this.legalName = legalName;
        this.valuationDate = valuationDate;
        this.price = price;
    }

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

    public String getMasterAgreementId() {
        return masterAgreementId;
    }

    public void setMasterAgreementId(String masterAgreementId) {
        this.masterAgreementId = masterAgreementId;
    }

    public String getTradeEmail() {
        return tradeEmail;
    }

    public void setTradeEmail(String tradeEmail) {
        this.tradeEmail = tradeEmail;
    }

    public JsonNode getContent() {
        return content;
    }

    public void setContent(JsonNode content) {
        this.content = content;
    }
}

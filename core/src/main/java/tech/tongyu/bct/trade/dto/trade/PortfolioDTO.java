package tech.tongyu.bct.trade.dto.trade;

import tech.tongyu.bct.common.api.doc.BctField;

import java.time.Instant;

public class PortfolioDTO {
    @BctField(name = "uuid", description = "唯一标识", type = "String")
    private String uuid;
    @BctField(name = "portfolioName", description = "投资组合名称", type = "String")
    private String portfolioName;
    @BctField(name = "createdAt", description = "创建时间", type = "Instant")
    private Instant createdAt;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getPortfolioName() {
        return portfolioName;
    }

    public void setPortfolioName(String portfolioName) {
        this.portfolioName = portfolioName;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}

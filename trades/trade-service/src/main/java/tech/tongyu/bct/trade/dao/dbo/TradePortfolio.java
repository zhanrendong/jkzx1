package tech.tongyu.bct.trade.dao.dbo;

import org.hibernate.annotations.CreationTimestamp;
import tech.tongyu.bct.trade.service.TradeService;

import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table( schema = TradeService.SCHEMA,
        indexes = {@Index(columnList = "tradeId"),
                   @Index(columnList = "portfolioName")})
public class TradePortfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column
    private String tradeId;

    @Column
    private String portfolioName;

    @Column
    @CreationTimestamp
    private Instant createdAt;

    public TradePortfolio() {
    }

    public TradePortfolio(String tradeId, String portfolioName) {
        this.tradeId = tradeId;
        this.portfolioName = portfolioName;
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

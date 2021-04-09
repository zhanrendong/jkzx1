package tech.tongyu.bct.trade.dao.dbo;

import org.hibernate.annotations.CreationTimestamp;
import tech.tongyu.bct.trade.service.TradeService;

import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(schema = TradeService.SCHEMA)
public class Portfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column(nullable = false, unique = true)
    private String portfolioName;

    @Column
    @CreationTimestamp
    private Instant createdAt;

    public Portfolio() {
    }

    public Portfolio(String portfolioName) {
        this.portfolioName = portfolioName;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
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

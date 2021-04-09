package tech.tongyu.bct.trade.dto.trade;

import tech.tongyu.bct.cm.trade.TradeStatusEnum;
import tech.tongyu.bct.common.api.doc.BctField;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public class TradeDTO {
    @BctField(name = "bookName", description = "交易簿名称", type = "String")
    public String bookName;
    @BctField(name = "tradeId", description = "交易ID", type = "String")
    public String tradeId;
    @BctField(name = "trader", description = "交易员", type = "String")
    public String trader;
    @BctField(name = "comment", description = "备注", type = "String")
    public String comment;
    @BctField(name = "tradeStatus", description = "交易状态", type = "TradeStatusEnum")
    public TradeStatusEnum tradeStatus;
    @BctField(name = "tradeDate", description = "交易日", type = "LocalDate")
    public LocalDate tradeDate;
    @BctField(name = "partyCode", description = "交易对手代码", type = "String")
    public String partyCode;
    @BctField(name = "partyName", description = "交易对手", type = "String")
    public String partyName;
    @BctField(name = "salesCode", description = "销售代码", type = "String")
    public String salesCode;
    @BctField(name = "salesName", description = "销售", type = "String")
    public String salesName;
    @BctField(name = "salesCommission", description = "佣金", type = "BigDecimal")
    public BigDecimal salesCommission;
    @BctField(name = "createdAt", description = "创建时间", type = "Instant")
    public Instant createdAt;
    @BctField(name = "updatedAt", description = "更新时间", type = "Instant")
    public Instant updatedAt;
    @BctField(name = "portfolioNames", description = "投资组合列表", type = "List<String> ")
    public List<String> portfolioNames;
    @BctField(name = "positions", description = "持仓列表", type = "List<TradePositionDTO>",isCollection = true, componentClass = TradePositionDTO.class)
    public List<TradePositionDTO> positions;

    public TradeDTO() {
    }

    public TradeDTO(String bookName, String tradeId, String counterpartyCode, String salesCode, BigDecimal salesCommission,
                    LocalDate tradeDate, List<TradePositionDTO> positions) {
        this.bookName = bookName;
        this.tradeId = tradeId;
        this.salesCode = salesCode;
        this.salesCommission = salesCommission;
        this.tradeDate = tradeDate;
        this.positions = positions;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getTradeId() {
        return tradeId;
    }

    public void setTradeId(String tradeId) {
        this.tradeId = tradeId;
    }

    public String getTrader() {
        return trader;
    }

    public void setTrader(String trader) {
        this.trader = trader;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public TradeStatusEnum getTradeStatus() {
        return tradeStatus;
    }

    public void setTradeStatus(TradeStatusEnum tradeStatus) {
        this.tradeStatus = tradeStatus;
    }

    public LocalDate getTradeDate() {
        return tradeDate;
    }

    public void setTradeDate(LocalDate tradeDate) {
        this.tradeDate = tradeDate;
    }

    public String getPartyCode() {
        return partyCode;
    }

    public void setPartyCode(String partyCode) {
        this.partyCode = partyCode;
    }

    public String getPartyName() {
        return partyName;
    }

    public void setPartyName(String partyName) {
        this.partyName = partyName;
    }

    public String getSalesCode() {
        return salesCode;
    }

    public void setSalesCode(String salesCode) {
        this.salesCode = salesCode;
    }

    public String getSalesName() {
        return salesName;
    }

    public void setSalesName(String salesName) {
        this.salesName = salesName;
    }

    public BigDecimal getSalesCommission() {
        return salesCommission;
    }

    public void setSalesCommission(BigDecimal salesCommission) {
        this.salesCommission = salesCommission;
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

    public List<TradePositionDTO> getPositions() {
        return positions;
    }

    public void setPositions(List<TradePositionDTO> positions) {
        this.positions = positions;
    }

    public List<String> getPortfolioNames() {
        return portfolioNames;
    }

    public void setPortfolioNames(List<String> portfolioNames) {
        this.portfolioNames = portfolioNames;
    }
}

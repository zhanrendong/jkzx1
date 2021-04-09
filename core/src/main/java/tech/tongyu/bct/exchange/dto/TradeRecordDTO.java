package tech.tongyu.bct.exchange.dto;

import tech.tongyu.bct.cm.product.iov.InstrumentOfValuePartyRoleTypeEnum;
import tech.tongyu.bct.common.api.doc.BctField;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

public class TradeRecordDTO {
    @BctField(
            name = "uuid",
            description = "唯一标识",
            type = "String"
    )
    private String uuid;
    @BctField(
            name = "bookId",
            description = "交易簿ID",
            type = "String"
    )
    private String bookId;
    @BctField(
            name = "tradeId",
            description = "交易ID",
            type = "String"
    )
    private String tradeId;
    @BctField(
            name = "tradeAccount",
            description = "交易账户",
            type = "String"
    )
    private String tradeAccount;
    @BctField(
            name = "instrumentId",
            description = "标的物ID",
            type = "String"
    )
    private String instrumentId;
    @BctField(
            name = "multiplier",
            description = "合约乘数",
            type = "BigDecimal"
    )
    private BigDecimal multiplier;
    @BctField(
            name = "dealAmount",
            description = "交易数量",
            type = "BigDecimal"
    )
    private BigDecimal dealAmount;
    @BctField(
            name = "dealPrice",
            description = "交易价格",
            type = "BigDecimal"
    )
    private BigDecimal dealPrice;
    @BctField(
            name = "dealTime",
            description = "交易时间",
            type = "LocalDateTime"
    )
    private LocalDateTime dealTime;
    @BctField(
            name = "portfolioNames",
            description = "投资组合列表",
            type = "List<String>",
            isCollection = true
    )
    private List<String> portfolioNames;
    @BctField(
            name = "openClose",
            description = "开/平",
            type = "OpenCloseEnum",
            componentClass = OpenCloseEnum.class
    )
    private OpenCloseEnum openClose;
    @BctField(
            name = "direction",
            description = "买卖方向",
            type = "InstrumentOfValuePartyRoleTypeEnum",
            componentClass = InstrumentOfValuePartyRoleTypeEnum.class
    )
    private InstrumentOfValuePartyRoleTypeEnum direction;
    @BctField(
            name = "createdAt",
            description = "创建时间",
            type = "Instant"
    )
    private Instant createdAt;

    public TradeRecordDTO() {
    }

    public TradeRecordDTO(String bookId, String tradeId, String tradeAccount, String instrumentId,
                          BigDecimal multiplier, BigDecimal dealAmount, BigDecimal dealPrice, LocalDateTime dealTime,
                          OpenCloseEnum openClose, InstrumentOfValuePartyRoleTypeEnum direction) {
        this.bookId = bookId;
        this.tradeId = tradeId;
        this.tradeAccount = tradeAccount;
        this.instrumentId = instrumentId;
        this.multiplier = multiplier;
        this.dealAmount = dealAmount;
        this.dealPrice = dealPrice;
        this.dealTime = dealTime;
        this.openClose = openClose;
        this.direction = direction;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getTradeId() {
        return tradeId;
    }

    public void setTradeId(String tradeId) {
        this.tradeId = tradeId;
    }

    public String getTradeAccount() {
        return tradeAccount;
    }

    public void setTradeAccount(String tradeAccount) {
        this.tradeAccount = tradeAccount;
    }

    public String getInstrumentId() {
        return instrumentId;
    }

    public void setInstrumentId(String instrumentId) {
        this.instrumentId = instrumentId;
    }

    public BigDecimal getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(BigDecimal multiplier) {
        this.multiplier = multiplier;
    }

    public BigDecimal getDealAmount() {
        return dealAmount;
    }

    public void setDealAmount(BigDecimal dealAmount) {
        this.dealAmount = dealAmount;
    }

    public BigDecimal getDealPrice() {
        return dealPrice;
    }

    public void setDealPrice(BigDecimal dealPrice) {
        this.dealPrice = dealPrice;
    }

    public LocalDateTime getDealTime() {
        return dealTime;
    }

    public void setDealTime(LocalDateTime dealTime) {
        this.dealTime = dealTime;
    }

    public OpenCloseEnum getOpenClose() {
        return openClose;
    }

    public void setOpenClose(OpenCloseEnum openClose) {
        this.openClose = openClose;
    }

    public InstrumentOfValuePartyRoleTypeEnum getDirection() {
        return direction;
    }

    public void setDirection(InstrumentOfValuePartyRoleTypeEnum direction) {
        this.direction = direction;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public List<String> getPortfolioNames() {
        return portfolioNames;
    }

    public void setPortfolioNames(List<String> portfolioNames) {
        this.portfolioNames = portfolioNames;
    }
}

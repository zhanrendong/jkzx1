package tech.tongyu.bct.cm.trade.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tech.tongyu.bct.cm.product.asset.Asset;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValue;
import tech.tongyu.bct.cm.trade.NonEconomicPartyRole;
import tech.tongyu.bct.cm.trade.Position;
import tech.tongyu.bct.cm.trade.Trade;
import tech.tongyu.bct.cm.trade.TradeStatusEnum;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BctTrade implements Trade {
    public static final String nonEconomicPartyRolesFieldName = "nonEconomicPartyRoles";

    public String bookName;

    public String tradeId;

    public String trader;

    public TradeStatusEnum tradeStatus;

    public List<String> positionIds;

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    public List<BctTradePosition> positions;

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    @JsonProperty(nonEconomicPartyRolesFieldName)
    public List<NonEconomicPartyRole> nonEconomicPartyRoles;

    public LocalDate tradeDate;

    public String comment;

    private Instant createdAt;

    private Instant updatedAt;

    public BctTrade() {
    }

    public BctTrade(String bookName, String tradeId, String trader, List<BctTradePosition> positions,
                    List<NonEconomicPartyRole> nonEconomicPartyRoles, LocalDate tradeDate, String comment) {
        this.bookName = bookName;
        this.tradeId = tradeId;
        this.trader = trader;
        this.positions = positions;
        this.nonEconomicPartyRoles = nonEconomicPartyRoles;
        this.tradeDate = tradeDate;
        this.comment = comment;
    }

    public BctTrade(String bookName, String tradeId, String trader, List<BctTradePosition> positions,
                    List<NonEconomicPartyRole> nonEconomicPartyRoles, LocalDate tradeDate) {
        this.bookName = bookName;
        this.tradeId = tradeId;
        this.trader = trader;
        this.positions = positions;
        this.nonEconomicPartyRoles = nonEconomicPartyRoles;
        this.tradeDate = tradeDate;
        this.comment = null;
    }

    @Override
    public List<Position<Asset<InstrumentOfValue>>> positions() {
        return positions.stream().map(p -> (Position<Asset<InstrumentOfValue>>) p).collect(Collectors.toList());
    }

    @Override
    public List<NonEconomicPartyRole> nonEconomicPartyRoles() {
        return nonEconomicPartyRoles;
    }

    @Override
    public LocalDate tradeDate() {
        return tradeDate;
    }

    @Override
    public Optional<String> comment() {
        return Optional.ofNullable(comment);
    }


    // ---------------------Property Generate Get/Set Method-----------------

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

    public TradeStatusEnum getTradeStatus() {
        return tradeStatus;
    }

    public void setTradeStatus(TradeStatusEnum tradeStatus) {
        this.tradeStatus = tradeStatus;
    }

    public List<String> getPositionIds() {
        return positionIds;
    }

    public void setPositionIds(List<String> positionIds) {
        this.positionIds = positionIds;
    }

    public List<BctTradePosition> getPositions() {
        return positions;
    }

    public void setPositions(List<BctTradePosition> positions) {
        this.positions = positions;
    }

    public List<NonEconomicPartyRole> getNonEconomicPartyRoles() {
        return nonEconomicPartyRoles;
    }

    public void setNonEconomicPartyRoles(List<NonEconomicPartyRole> nonEconomicPartyRoles) {
        this.nonEconomicPartyRoles = nonEconomicPartyRoles;
    }

    public LocalDate getTradeDate() {
        return tradeDate;
    }

    public void setTradeDate(LocalDate tradeDate) {
        this.tradeDate = tradeDate;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
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

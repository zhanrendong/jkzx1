package tech.tongyu.bct.pricing.api.response;

import tech.tongyu.bct.common.api.doc.BctField;
import tech.tongyu.bct.exchange.dto.PositionPortfolioRecordDTO;
import tech.tongyu.bct.market.dto.InstrumentDTO;
import tech.tongyu.bct.quant.library.common.impl.BlackResults;

import java.util.Objects;


public class OnExchangeResults {
    @BctField(description = "持仓ID")
    private String positionId; // sythetic position id
    @BctField(description = "持仓合约ID")
    private String instrumentId; // must exist
    @BctField(description = "持仓合约乘数")
    private double instrumentMultiplier;
    @BctField(description = "持仓合约ID")
    private String underlyerInstrumentId; // exists only if it is an exchanged traded option. defaults to instrumentId
    @BctField(description = "持仓合约乘数")
    private double underlyerInstrumentMultiplier;
    // collections
    @BctField(description = "交易簿ID")
    private String bookId;
    @BctField(description = "投资组合ID")
    private String portfolioId;
    // quantity
    @BctField(description = "合约份数")
    private double quantity;
    // pnl related
    @BctField(description = "多头持仓")
    private double longPosition;
    @BctField(description = "空头持仓")
    private double shortPosition;
    @BctField(description = "净持仓")
    private double netPosition;
    @BctField(description = "总卖量")
    private double totalSell;
    @BctField(description = "总买量")
    private double totalBuy;
    @BctField(description = "历史总买金额")
    private double historyBuyAmount;
    @BctField(description = "历史总卖金额")
    private double historySellAmount;
    @BctField(description = "市值")
    private Double marketValue;
    @BctField(description = "市值盈亏")
    private Double totalPnl;
    @BctField(description = "估值盈亏")
    private Double totalValuationPnl;
    // results
    @BctField(description = "持仓估值")
    private Double price;
    @BctField(description = "持仓总delta")
    private Double delta;
    @BctField(description = "持仓总gamma")
    private Double gamma;
    @BctField(description = "持仓总vega")
    private Double vega;
    @BctField(description = "持仓总theta")
    private Double theta;
    @BctField(description = "持仓总rhoR")
    private Double rhoR;
    @BctField(description = "持仓总rhoQ")
    private Double rhoQ;
    // black params
    @BctField(description = "定价使用的标的物价格")
    private Double underlyerPrice;
    @BctField(description = "标的物远期价格")
    private Double underlyerForward;
    @BctField(description = "定价使用的波动率")
    private Double vol;
    @BctField(description = "定价使用的无风险利率")
    private Double r;
    @BctField(description = "定价使用的分红利率")
    private Double q;
    // base contracts
    @BctField(description = "基础合约代码")
    private String baseContractInstrumentId;
    @BctField(description = "基础合约乘数")
    private Double baseContractMultiplier;
    @BctField(description = "基础合约价格")
    private Double baseContractPrice;
    @BctField(description = "基础合约总delta")
    private Double baseContractDelta;
    @BctField(description = "基础合约总gamma")
    private Double baseContractGamma;
    @BctField(description = "基础合约总theta")
    private Double baseContractTheta;
    @BctField(description = "基础合约总rhoR")
    private Double baseContractRhoR;

    public OnExchangeResults(String positionId,
                             InstrumentDTO instrumentDTO,
                             InstrumentDTO underlyerInstrumentDTO,
                             InstrumentDTO baseContractInstrumentDTO,
                             PositionPortfolioRecordDTO positionRecordDTO,
                             BlackResults results) {
        this.positionId = positionId;
        this.instrumentId = instrumentDTO.getInstrumentId();
        this.instrumentMultiplier = instrumentDTO.getInstrumentInfo().multiplier();
        this.underlyerInstrumentId = results.getUnderlyerInstrumentId();
        this.underlyerInstrumentMultiplier = underlyerInstrumentDTO.getInstrumentInfo().multiplier();
        this.bookId = positionRecordDTO.getBookId();
        this.portfolioId = positionRecordDTO.getPortfolioName();
        this.longPosition = positionRecordDTO.getLongPosition().doubleValue();
        this.shortPosition = positionRecordDTO.getShortPosition().doubleValue();
        this.netPosition = positionRecordDTO.getNetPosition().doubleValue();
        this.totalSell = positionRecordDTO.getTotalSell().doubleValue();
        this.totalBuy = positionRecordDTO.getTotalBuy().doubleValue();
        this.historySellAmount = positionRecordDTO.getHistorySellAmount().doubleValue();
        this.historyBuyAmount = positionRecordDTO.getHistoryBuyAmount().doubleValue();
        this.marketValue = null;
        this.price = results.getPrice();
        this.delta = results.getDelta();
        this.gamma = results.getGamma();
        this.vega = results.getVega();
        this.theta = results.getTheta();
        this.rhoR = results.getRhoR();
        this.rhoQ = results.getRhoQ();
        this.underlyerPrice = results.getUnderlyerPrice();
        this.underlyerForward = results.getUnderlyerForward();
        this.vol = results.getVol();
        this.r = results.getR();
        this.q = results.getQ();
        this.baseContractInstrumentId = results.getBaseContractInstrumentId();
        this.baseContractMultiplier = Objects.isNull(this.baseContractInstrumentId) ? null
                : baseContractInstrumentDTO.getInstrumentInfo().multiplier().doubleValue();
        this.baseContractPrice = results.getBaseContractPrice();
        this.baseContractDelta = results.getBaseContractDelta();
        this.baseContractGamma = results.getBaseContractGamma();
        this.baseContractTheta = results.getBaseContractTheta();
        this.baseContractRhoR = results.getBaseContractRhoR();
        this.quantity = results.getQuantity();

        if (!Objects.isNull(this.price) && !this.price.isNaN()) {
            this.totalValuationPnl = this.price + this.historySellAmount - this.historyBuyAmount;
        }
    }

    public OnExchangeResults(String positionId,
                             InstrumentDTO instrumentDTO,
                             InstrumentDTO underlyerInstrumentDTO,
                             PositionPortfolioRecordDTO positionRecordDTO) {
        this.positionId = positionId;
        this.instrumentId = instrumentDTO.getInstrumentId();
        this.instrumentMultiplier = instrumentDTO.getInstrumentInfo().multiplier();
        this.underlyerInstrumentId = underlyerInstrumentDTO.getInstrumentId();
        this.underlyerInstrumentMultiplier = underlyerInstrumentDTO.getInstrumentInfo().multiplier();
        this.bookId = positionRecordDTO.getBookId();
        this.portfolioId = positionRecordDTO.getPortfolioName();
        this.longPosition = positionRecordDTO.getLongPosition().doubleValue();
        this.shortPosition = positionRecordDTO.getShortPosition().doubleValue();
        this.netPosition = positionRecordDTO.getNetPosition().doubleValue();
        this.totalSell = positionRecordDTO.getTotalSell().doubleValue();
        this.totalBuy = positionRecordDTO.getTotalBuy().doubleValue();
        this.historySellAmount = positionRecordDTO.getHistorySellAmount().doubleValue();
        this.historyBuyAmount = positionRecordDTO.getHistoryBuyAmount().doubleValue();
        this.marketValue = null;
        this.price = null;
        this.delta = null;
        this.gamma = null;
        this.vega = null;
        this.theta = null;
        this.rhoR = null;
        this.rhoQ = null;
        this.underlyerPrice = null;
        this.underlyerForward = null;
        this.vol = null;
        this.r = null;
        this.q = null;
        this.baseContractInstrumentId = null;
        this.baseContractMultiplier = null;
        this.baseContractPrice = null;
        this.baseContractDelta = null;
        this.baseContractGamma = null;
        this.baseContractTheta = null;
        this.baseContractRhoR = null;
        this.quantity = this.netPosition * this.instrumentMultiplier;
    }

    public String getPositionId() {
        return positionId;
    }

    public String getInstrumentId() {
        return instrumentId;
    }

    public double getInstrumentMultiplier() {
        return instrumentMultiplier;
    }

    public String getUnderlyerInstrumentId() {
        return underlyerInstrumentId;
    }

    public double getUnderlyerInstrumentMultiplier() {
        return underlyerInstrumentMultiplier;
    }

    public String getBookId() {
        return bookId;
    }

    public String getPortfolioId() {
        return portfolioId;
    }

    public double getLongPosition() {
        return longPosition;
    }

    public double getShortPosition() {
        return shortPosition;
    }

    public double getNetPosition() {
        return netPosition;
    }

    public double getTotalSell() {
        return totalSell;
    }

    public double getTotalBuy() {
        return totalBuy;
    }

    public double getHistoryBuyAmount() {
        return historyBuyAmount;
    }

    public double getHistorySellAmount() {
        return historySellAmount;
    }

    public Double getMarketValue() {
        return marketValue;
    }

    public Double getPrice() {
        return price;
    }

    public Double getDelta() {
        return delta;
    }

    public Double getGamma() {
        return gamma;
    }

    public Double getVega() {
        return vega;
    }

    public Double getTheta() {
        return theta;
    }

    public Double getRhoR() {
        return rhoR;
    }

    public Double getRhoQ() {
        return rhoQ;
    }

    public Double getUnderlyerPrice() {
        return underlyerPrice;
    }

    public Double getUnderlyerForward() {
        return underlyerForward;
    }

    public Double getVol() {
        return vol;
    }

    public Double getR() {
        return r;
    }

    public Double getQ() {
        return q;
    }

    public String getBaseContractInstrumentId() {
        return baseContractInstrumentId;
    }

    public Double getBaseContractMultiplier() {
        return baseContractMultiplier;
    }

    public Double getBaseContractPrice() {
        return baseContractPrice;
    }

    public Double getBaseContractDelta() {
        return baseContractDelta;
    }

    public Double getBaseContractGamma() {
        return baseContractGamma;
    }

    public Double getBaseContractTheta() {
        return baseContractTheta;
    }

    public Double getBaseContractRhoR() {
        return baseContractRhoR;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setMarketValue(Double marketValue) {
        this.marketValue = marketValue;
        if (!Objects.isNull(this.marketValue)) {
            this.totalPnl = this.marketValue + this.historySellAmount - this.historyBuyAmount;
        }
    }

    public Double getTotalPnl() {
        return totalPnl;
    }

    public Double getTotalValuationPnl() {
        return totalValuationPnl;
    }
}

package tech.tongyu.bct.trade.dto.trade;

import java.util.List;

public class TradePositionParamDTO {

    private String tradeId;

    private String positionId;

    public TradePositionParamDTO(String tradeId, String positionId) {
        this.tradeId = tradeId;
        this.positionId = positionId;
    }

    public String getTradeId() {
        return tradeId;
    }

    public void setTradeId(String tradeId) {
        this.tradeId = tradeId;
    }

    public String getPositionId() {
        return positionId;
    }

    public void setPositionId(String positionId) {
        this.positionId = positionId;
    }
}

package tech.tongyu.bct.exchange.dto;

public class CombStockDTO {
    private String occurDate;
    private String stockCode;
    private double buyVolume;
    private double buyAmount;
    private double sellVolume;
    private double sellAmount;
    private double stockVloume;
    private Integer combId;

    public CombStockDTO() {
    }

    public CombStockDTO(String occurDate, String stockCode, double buyVolume,
                        double buyAmount, double sellVolume, double sellAmount,
                        double stockVloume, Integer combId) {
        this.occurDate = occurDate;
        this.stockCode = stockCode;
        this.buyVolume = buyVolume;
        this.buyAmount = buyAmount;
        this.sellVolume = sellVolume;
        this.sellAmount = sellAmount;
        this.stockVloume = stockVloume;
        this.combId = combId;
    }

    public String getOccurDate() {
        return occurDate;
    }

    public void setOccurDate(String occurDate) {
        this.occurDate = occurDate;
    }

    public String getStockCode() {
        return stockCode;
    }

    public void setStockCode(String stockCode) {
        this.stockCode = stockCode;
    }

    public double getBuyVolume() {
        return buyVolume;
    }

    public void setBuyVolume(double buyVolume) {
        this.buyVolume = buyVolume;
    }

    public double getBuyAmount() {
        return buyAmount;
    }

    public void setBuyAmount(double buyAmount) {
        this.buyAmount = buyAmount;
    }

    public double getSellVolume() {
        return sellVolume;
    }

    public void setSellVolume(double sellVolume) {
        this.sellVolume = sellVolume;
    }

    public double getSellAmount() {
        return sellAmount;
    }

    public void setSellAmount(double sellAmount) {
        this.sellAmount = sellAmount;
    }

    public double getStockVloume() {
        return stockVloume;
    }

    public void setStockVloume(double stockVloume) {
        this.stockVloume = stockVloume;
    }

    public Integer getCombId() {
        return combId;
    }

    public void setCombId(Integer combId) {
        this.combId = combId;
    }
}

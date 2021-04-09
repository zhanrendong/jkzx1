package tech.tongyu.bct.model.ao;

import java.time.LocalDate;

/**
 * 创建波动率曲面用到的行情/波动率
 */
public class AtmVolInstrumentAO {
    private LocalDate expiry;
    private String tenor;
    private Double quote;
    private Boolean use;

    public AtmVolInstrumentAO() {
    }

    /**
     * 构造函数
     * @param tenor 期限
     * @param expiry 到期日
     * @param quote 波动率
     * @param use 是否使用
     */
    public AtmVolInstrumentAO(String tenor, LocalDate expiry, Double quote, Boolean use) {
        this.expiry = expiry;
        this.quote = quote;
        this.use = use;
        this.tenor = tenor;
    }

    public LocalDate getExpiry() {
        return expiry;
    }

    public void setExpiry(LocalDate expiry) {
        this.expiry = expiry;
    }

    public Double getQuote() {
        return quote;
    }

    public void setQuote(Double quote) {
        this.quote = quote;
    }

    public Boolean getUse() {
        return use;
    }

    public void setUse(Boolean use) {
        this.use = use;
    }

    public String getTenor() {
        return tenor;
    }

    public void setTenor(String tenor) {
        this.tenor = tenor;
    }
}

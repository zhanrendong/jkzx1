package tech.tongyu.bct.model.ao;

import java.time.LocalDate;

public class CurveInstrumentAO {
    private LocalDate expiry;
    private Double quote;
    private Boolean use;
    private String tenor;

    public CurveInstrumentAO() {
    }

    public CurveInstrumentAO(String tenor, LocalDate expiry, Double quote, Boolean use) {
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

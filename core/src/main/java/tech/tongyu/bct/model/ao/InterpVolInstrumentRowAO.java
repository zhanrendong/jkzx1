package tech.tongyu.bct.model.ao;

import java.time.LocalDate;
import java.util.List;

/**
 * 给定到期日/期限不同行权价上的波动率
 */
public class InterpVolInstrumentRowAO {
    /**
     * 期限
     */
    private String tenor;
    /**
     * 到期日
     */
    private LocalDate expiry;
    /**
     * 不同行权价上的波动率 {@link VolInstrument}
     */
    private List<VolInstrument> vols;

    public static class VolInstrument {
        /**
         * 波动率
         */
        private Double quote;
        /**
         * 行权价
         */
        private Double strike;
        /**
         * 百分比行权价
         */
        private Double percent;
        /**
         * 标识
         */
        private String label;

        public VolInstrument() {
        }

        public VolInstrument(Double quote, Double strike, Double percent, String label) {
            this.quote = quote;
            this.strike = strike;
            this.label = label;
            this.percent = percent;
        }

        public Double getQuote() {
            return quote;
        }

        public void setQuote(Double quote) {
            this.quote = quote;
        }

        public Double getStrike() {
            return strike;
        }

        public void setStrike(Double strike) {
            this.strike = strike;
        }

        public Double getPercent() {
            return percent;
        }

        public void setPercent(Double percent) {
            this.percent = percent;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }
    }


    public InterpVolInstrumentRowAO() {
    }

    public InterpVolInstrumentRowAO(String tenor, LocalDate expiry, List<VolInstrument> vols) {
        this.tenor = tenor;
        this.expiry = expiry;
        this.vols = vols;
    }

    public String getTenor() {
        return tenor;
    }

    public void setTenor(String tenor) {
        this.tenor = tenor;
    }

    public LocalDate getExpiry() {
        return expiry;
    }

    public void setExpiry(LocalDate expiry) {
        this.expiry = expiry;
    }

    public List<VolInstrument> getVols() {
        return vols;
    }

    public void setVols(List<VolInstrument> vols) {
        this.vols = vols;
    }
}

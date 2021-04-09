package tech.tongyu.bct.trade.dto.trade;

import tech.tongyu.bct.common.api.doc.BctField;

import java.util.List;

public class QuotePrcDTO {
    @BctField(name = "userName", description = "用户名", type = "String")
    private String userName;
    @BctField(name = "quotePrcId", description = "试定价结果集ID", type = "String")
    private String quotePrcId;
    @BctField(name = "counterPartyCode", description = "交易对手", type = "String")
    private String counterPartyCode;
    @BctField(name = "pricingEnvironmentId", description = "定价环境ID", type = "String")
    private String pricingEnvironmentId;
    @BctField(name = "quotePositions", description = "试定价结果集", type = "List<QuotePositionDTO>", componentClass = QuotePositionDTO.class)
    private List<QuotePositionDTO> quotePositions;

    private String comment;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getQuotePrcId() {
        return quotePrcId;
    }

    public void setQuotePrcId(String quotePrcId) {
        this.quotePrcId = quotePrcId;
    }

    public String getCounterPartyCode() {
        return counterPartyCode;
    }

    public void setCounterPartyCode(String counterPartyCode) {
        this.counterPartyCode = counterPartyCode;
    }

    public String getPricingEnvironmentId() {
        return pricingEnvironmentId;
    }

    public void setPricingEnvironmentId(String pricingEnvironmentId) {
        this.pricingEnvironmentId = pricingEnvironmentId;
    }

    public List<QuotePositionDTO> getQuotePositions() {
        return quotePositions;
    }

    public void setQuotePositions(List<QuotePositionDTO> quotePositions) {
        this.quotePositions = quotePositions;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}

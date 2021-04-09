package tech.tongyu.bct.reference.dto;

import java.time.LocalDate;

public class AuthorizerDTO {

    public String tradeAuthorizerName;

    public String tradeAuthorizerIdNumber;

    public LocalDate tradeAuthorizerIdExpiryDate;

    public String tradeAuthorizerPhone;

    public AuthorizerDTO() {
    }

    public AuthorizerDTO(String tradeAuthorizerName, String tradeAuthorizerIdNumber, LocalDate tradeAuthorizerIdExpiryDate, String tradeAuthorizerPhone) {
        this.tradeAuthorizerName = tradeAuthorizerName;
        this.tradeAuthorizerIdNumber = tradeAuthorizerIdNumber;
        this.tradeAuthorizerIdExpiryDate = tradeAuthorizerIdExpiryDate;
        this.tradeAuthorizerPhone = tradeAuthorizerPhone;
    }

    public String getTradeAuthorizerName() {
        return tradeAuthorizerName;
    }

    public void setTradeAuthorizerName(String tradeAuthorizerName) {
        this.tradeAuthorizerName = tradeAuthorizerName;
    }

    public String getTradeAuthorizerIdNumber() {
        return tradeAuthorizerIdNumber;
    }

    public void setTradeAuthorizerIdNumber(String tradeAuthorizerIdNumber) {
        this.tradeAuthorizerIdNumber = tradeAuthorizerIdNumber;
    }

    public LocalDate getTradeAuthorizerIdExpiryDate() {
        return tradeAuthorizerIdExpiryDate;
    }

    public void setTradeAuthorizerIdExpiryDate(LocalDate tradeAuthorizerIdExpiryDate) {
        this.tradeAuthorizerIdExpiryDate = tradeAuthorizerIdExpiryDate;
    }

    public String getTradeAuthorizerPhone() {
        return tradeAuthorizerPhone;
    }

    public void setTradeAuthorizerPhone(String tradeAuthorizerPhone) {
        this.tradeAuthorizerPhone = tradeAuthorizerPhone;
    }
}

package tech.tongyu.bct.reference.dao.dbo;

import tech.tongyu.bct.client.service.AccountService;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(schema = AccountService.SCHEMA)
public class Authorizer {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column
    private String tradeAuthorizerName;

    @Column
    private String tradeAuthorizerIdNumber;

    @Column
    private LocalDate tradeAuthorizerIdExpiryDate;

    @Column
    private String tradeAuthorizerPhone;

    @Column String partyLegalName;

    public Authorizer() {
    }

    public Authorizer(String tradeAuthorizerName, String tradeAuthorizerIdNumber, LocalDate tradeAuthorizerIdExpiryDate, String tradeAuthorizerPhone, String partyLegalName) {
        this.tradeAuthorizerName = tradeAuthorizerName;
        this.tradeAuthorizerIdNumber = tradeAuthorizerIdNumber;
        this.tradeAuthorizerIdExpiryDate = tradeAuthorizerIdExpiryDate;
        this.tradeAuthorizerPhone = tradeAuthorizerPhone;
        this.partyLegalName = partyLegalName;
    }

    public String getPartyLegalName() {
        return partyLegalName;
    }

    public void setPartyLegalName(String partyLegalName) {
        this.partyLegalName = partyLegalName;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
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

package tech.tongyu.bct.cm.product.iov.impl;

import tech.tongyu.bct.cm.product.iov.*;
import tech.tongyu.bct.cm.product.iov.feature.CashPaymentFeature;
import tech.tongyu.bct.cm.product.iov.feature.SettlementFeature;
import tech.tongyu.bct.cm.product.iov.feature.SettlementTypeEnum;
import tech.tongyu.bct.cm.reference.impl.CurrencyUnit;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class CashFlow<U extends InstrumentOfValue> implements InstrumentOfValue, CashPaymentFeature, SettlementFeature {
    public CashFlow() {
    }

    public CashFlow(ProductTypeEnum productType, BigDecimal paymentAmount, CashFlowDirectionEnum paymentDirection,
                    LocalDate settlementDate, CurrencyUnit currency, PaymentStateEnum paymentState) {
        this.productType = productType;
        this.settlementDate = settlementDate;
        this.paymentAmount = paymentAmount;
        this.paymentDirection = paymentDirection;
        this.currency = currency;
        this.paymentState = paymentState;
    }

    @Override
    public String instrumentId() {
        return null;
    }

    @Override
    public List<InstrumentOfValuePartyRoleTypeEnum> roles() {
        return Arrays.asList(InstrumentOfValuePartyRoleTypeEnum.BUYER, InstrumentOfValuePartyRoleTypeEnum.SELLER);
    }

    @Override
    public InstrumentAssetClassTypeEnum assetClassType() {
        return InstrumentAssetClassTypeEnum.RATES;
    }

    @Override
    public List<CurrencyUnit> contractCurrencies() {
        return Arrays.asList(currency);
    }

    public CurrencyUnit currency;

    @Override
    public CurrencyUnit currency() {
        return currency;
    }

    public BigDecimal paymentAmount;

    @Override
    public BigDecimal paymentAmount() {
        return paymentAmount;
    }

    public CashFlowDirectionEnum paymentDirection;

    @Override
    public CashFlowDirectionEnum paymentDirection() {
        return paymentDirection;
    }

    public LocalDate settlementDate;

    @Override
    public LocalDate settlementDate() {
        return settlementDate;
    }

    @Override
    public SettlementTypeEnum settlementType() {
        return SettlementTypeEnum.CASH;
    }

    public ProductTypeEnum productType;

    @Override
    public ProductTypeEnum productType() {
        return productType;
    }

    public PaymentStateEnum paymentState;

    @Override
    public PaymentStateEnum paymentState() {
        return paymentState;
    }

}

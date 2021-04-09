package tech.tongyu.bct.reference.dao.dbo;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import tech.tongyu.bct.reference.dto.*;
import tech.tongyu.bct.reference.service.PartyService;

import javax.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(schema = PartyService.SECHEMA)
public class Party {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;
    //客户类型
    @Column
    private ClientTypeEnum clientType;
    //开户名称
    @Column
    private String legalName;
    //开户法人
    @Column
    private String legalRepresentative;
    //注册地址
    @Column
    private String address;
    //联系人
    @Column
    private String contact;
    //担保人
    @Column
    private String warrantor;
    //担保人地址
    @Column
    private String warrantorAddress;
    //交易电话
    @Column
    private String tradePhone;
    //交易指定邮箱
    @Column
    private String tradeEmail;
    //分公司
    @Column
    private String subsidiaryName;
    //营业部
    @Column
    private String branchName;
    //销售
    @Column
    private String salesName;
    //主协议编号
    @Column
    private String masterAgreementId;
    //补充协议编号
    @Column
    private String supplementalAgreementId;
    //主协议
    @Column
    private String masterAgreementDoc;
    //补充协议
    @Column
    private String supplementalAgreementDoc;
    //风险问卷调查
    @Column
    private String riskSurveyDoc;
    //交易授权书
    @Column
    private String tradeAuthDoc;
    //对手尽职调查
    @Column
    private String dueDiligenceDoc;
    //风险承受能力调查问卷
    @Column
    private String riskPreferenceDoc;
    //合规性承诺书
    @Column
    private String complianceDoc;
    //风险揭示书
    @Column
    private String riskRevelationDoc;
    //适当性警示书
    @Column
    private String qualificationWarningDoc;
    //授信协议
    @Column
    private String creditAgreement;
    //履约保障协议
    @Column
    private String performanceGuaranteeDoc;
    //对方授信额度
    @Column
    private Double cptyCreditLimit;
    //我方授信额度
    @Column
    private Double ourCreditLimit;
    //保证金折扣
    @Column
    private Double marginDiscountRate;
    //机构类型分类
    @Column
    private InvestorTypeEnum investorType;
    //交易方向
    @Column
    private TradingDirectionEnum tradingDirection;
    //交易权限
    @Column
    private TradingPermissionEnum tradingPermission;
    //交易权限备注
    @Column
    private String tradingPermissionNote;
    //交易标的
    @Column
    private TradingUnderlyersEnum tradingUnderlyers;
    //托管邮箱
    @Column
    private String trustorEmail;
    //授权到期日
    @Column
    private LocalDate authorizeExpiryDate;
    //协议签署授权人姓名
    @Column
    private String signAuthorizerName;
    //协议签署授权人身份证
    @Column
    private String signAuthorizerIdNumber;
    //协议签署授权人证件有效期
    @Column
    private LocalDate signAuthorizerIdExpiryDate;
    @Column
    private String tradeAuthorizerPhone;
    //产品名称
    @Column
    private String productName;
    //产品代码
    @Column
    private String productCode;
    //产品类型
    @Column
    private String productType;
    //备案编号
    @Column
    private String recordNumber;
    //产品成立日
    @Column
    private LocalDate productFoundDate;
    //产品到期日
    @Column
    private LocalDate productExpiringDate;
    //基金经理
    @Column
    private String fundManager;
    //状态
    @Column
    private PartyStatusEnum partyStatus = PartyStatusEnum.NORMAL;
    //主协议签订日期
    @Column
    private LocalDate masterAgreementSignDate;
    //主协议编号版本
    @Column
    private MasterAgreementNoVersionEnum masterAgreementNoVersion;
    //营业执照
    @Column
    private String businessLicense;


    /**
     * 维持保证金
     */
    @OneToOne(fetch = FetchType.EAGER,cascade = CascadeType.REMOVE)
    @JoinColumn(name = "partyId")
    private Margin maintenanceMargin;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @Column
    @UpdateTimestamp
    private Instant updatedAt;

    public Party() {
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public ClientTypeEnum getClientType() {
        return clientType;
    }

    public void setClientType(ClientTypeEnum clientType) {
        this.clientType = clientType;
    }

    public String getLegalName() {
        return legalName;
    }

    public void setLegalName(String legalName) {
        this.legalName = legalName;
    }

    public String getLegalRepresentative() {
        return legalRepresentative;
    }

    public void setLegalRepresentative(String legalRepresentative) {
        this.legalRepresentative = legalRepresentative;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getWarrantor() {
        return warrantor;
    }

    public void setWarrantor(String warrantor) {
        this.warrantor = warrantor;
    }

    public String getWarrantorAddress() {
        return warrantorAddress;
    }

    public void setWarrantorAddress(String warrantorAddress) {
        this.warrantorAddress = warrantorAddress;
    }

    public String getTradePhone() {
        return tradePhone;
    }

    public void setTradePhone(String tradePhone) {
        this.tradePhone = tradePhone;
    }

    public String getTradeEmail() {
        return tradeEmail;
    }

    public void setTradeEmail(String tradeEmail) {
        this.tradeEmail = tradeEmail;
    }

    public String getSubsidiaryName() {
        return subsidiaryName;
    }

    public void setSubsidiaryName(String subsidiaryName) {
        this.subsidiaryName = subsidiaryName;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getSalesName() {
        return salesName;
    }

    public void setSalesName(String salesName) {
        this.salesName = salesName;
    }

    public String getMasterAgreementId() {
        return masterAgreementId;
    }

    public void setMasterAgreementId(String masterAgreementId) {
        this.masterAgreementId = masterAgreementId;
    }

    public String getSupplementalAgreementId() {
        return supplementalAgreementId;
    }

    public void setSupplementalAgreementId(String supplementalAgreementId) {
        this.supplementalAgreementId = supplementalAgreementId;
    }

    public String getMasterAgreementDoc() {
        return masterAgreementDoc;
    }

    public void setMasterAgreementDoc(String masterAgreementDoc) {
        this.masterAgreementDoc = masterAgreementDoc;
    }

    public String getSupplementalAgreementDoc() {
        return supplementalAgreementDoc;
    }

    public void setSupplementalAgreementDoc(String supplementalAgreementDoc) {
        this.supplementalAgreementDoc = supplementalAgreementDoc;
    }

    public String getRiskSurveyDoc() {
        return riskSurveyDoc;
    }

    public void setRiskSurveyDoc(String riskSurveyDoc) {
        this.riskSurveyDoc = riskSurveyDoc;
    }

    public String getTradeAuthDoc() {
        return tradeAuthDoc;
    }

    public void setTradeAuthDoc(String tradeAuthDoc) {
        this.tradeAuthDoc = tradeAuthDoc;
    }

    public String getDueDiligenceDoc() {
        return dueDiligenceDoc;
    }

    public void setDueDiligenceDoc(String dueDiligenceDoc) {
        this.dueDiligenceDoc = dueDiligenceDoc;
    }

    public String getRiskPreferenceDoc() {
        return riskPreferenceDoc;
    }

    public void setRiskPreferenceDoc(String riskPreferenceDoc) {
        this.riskPreferenceDoc = riskPreferenceDoc;
    }

    public String getComplianceDoc() {
        return complianceDoc;
    }

    public void setComplianceDoc(String complianceDoc) {
        this.complianceDoc = complianceDoc;
    }

    public String getRiskRevelationDoc() {
        return riskRevelationDoc;
    }

    public void setRiskRevelationDoc(String riskRevelationDoc) {
        this.riskRevelationDoc = riskRevelationDoc;
    }

    public String getQualificationWarningDoc() {
        return qualificationWarningDoc;
    }

    public void setQualificationWarningDoc(String qualificationWarningDoc) {
        this.qualificationWarningDoc = qualificationWarningDoc;
    }

    public String getCreditAgreement() {
        return creditAgreement;
    }

    public void setCreditAgreement(String creditAgreement) {
        this.creditAgreement = creditAgreement;
    }

    public String getPerformanceGuaranteeDoc() {
        return performanceGuaranteeDoc;
    }

    public void setPerformanceGuaranteeDoc(String performanceGuaranteeDoc) {
        this.performanceGuaranteeDoc = performanceGuaranteeDoc;
    }

    public Double getCptyCreditLimit() {
        return cptyCreditLimit;
    }

    public void setCptyCreditLimit(Double cptyCreditLimit) {
        this.cptyCreditLimit = cptyCreditLimit;
    }

    public Double getOurCreditLimit() {
        return ourCreditLimit;
    }

    public void setOurCreditLimit(Double ourCreditLimit) {
        this.ourCreditLimit = ourCreditLimit;
    }

    public Double getMarginDiscountRate() {
        return marginDiscountRate;
    }

    public void setMarginDiscountRate(Double marginDiscountRate) {
        this.marginDiscountRate = marginDiscountRate;
    }

    public InvestorTypeEnum getInvestorType() {
        return investorType;
    }

    public void setInvestorType(InvestorTypeEnum investorType) {
        this.investorType = investorType;
    }

    public TradingDirectionEnum getTradingDirection() {
        return tradingDirection;
    }

    public void setTradingDirection(TradingDirectionEnum tradingDirection) {
        this.tradingDirection = tradingDirection;
    }

    public TradingPermissionEnum getTradingPermission() {
        return tradingPermission;
    }

    public void setTradingPermission(TradingPermissionEnum tradingPermission) {
        this.tradingPermission = tradingPermission;
    }

    public String getTradingPermissionNote() {
        return tradingPermissionNote;
    }

    public void setTradingPermissionNote(String tradingPermissionNote) {
        this.tradingPermissionNote = tradingPermissionNote;
    }

    public TradingUnderlyersEnum getTradingUnderlyers() {
        return tradingUnderlyers;
    }

    public void setTradingUnderlyers(TradingUnderlyersEnum tradingUnderlyers) {
        this.tradingUnderlyers = tradingUnderlyers;
    }

    public String getTrustorEmail() {
        return trustorEmail;
    }

    public void setTrustorEmail(String trustorEmail) {
        this.trustorEmail = trustorEmail;
    }

    public LocalDate getAuthorizeExpiryDate() {
        return authorizeExpiryDate;
    }

    public void setAuthorizeExpiryDate(LocalDate authorizeExpiryDate) {
        this.authorizeExpiryDate = authorizeExpiryDate;
    }

    public String getSignAuthorizerName() {
        return signAuthorizerName;
    }

    public void setSignAuthorizerName(String signAuthorizerName) {
        this.signAuthorizerName = signAuthorizerName;
    }

    public String getSignAuthorizerIdNumber() {
        return signAuthorizerIdNumber;
    }

    public void setSignAuthorizerIdNumber(String signAuthorizerIdNumber) {
        this.signAuthorizerIdNumber = signAuthorizerIdNumber;
    }

    public LocalDate getSignAuthorizerIdExpiryDate() {
        return signAuthorizerIdExpiryDate;
    }

    public void setSignAuthorizerIdExpiryDate(LocalDate signAuthorizerIdExpiryDate) {
        this.signAuthorizerIdExpiryDate = signAuthorizerIdExpiryDate;
    }

    public String getTradeAuthorizerPhone() {
        return tradeAuthorizerPhone;
    }

    public void setTradeAuthorizerPhone(String tradeAuthorizerPhone) {
        this.tradeAuthorizerPhone = tradeAuthorizerPhone;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getRecordNumber() {
        return recordNumber;
    }

    public void setRecordNumber(String recordNumber) {
        this.recordNumber = recordNumber;
    }

    public LocalDate getProductFoundDate() {
        return productFoundDate;
    }

    public void setProductFoundDate(LocalDate productFoundDate) {
        this.productFoundDate = productFoundDate;
    }

    public LocalDate getProductExpiringDate() {
        return productExpiringDate;
    }

    public void setProductExpiringDate(LocalDate productExpiringDate) {
        this.productExpiringDate = productExpiringDate;
    }

    public String getFundManager() {
        return fundManager;
    }

    public void setFundManager(String fundManager) {
        this.fundManager = fundManager;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Margin getMaintenanceMargin() {
        return maintenanceMargin;
    }

    public void setMaintenanceMargin(Margin maintenanceMargin) {
        this.maintenanceMargin = maintenanceMargin;
    }

    public PartyStatusEnum getPartyStatus() {
        return partyStatus;
    }

    public void setPartyStatus(PartyStatusEnum partyStatus) {
        this.partyStatus = partyStatus;
    }

    public LocalDate getMasterAgreementSignDate() {
        return masterAgreementSignDate;
    }

    public void setMasterAgreementSignDate(LocalDate masterAgreementSignDate) {
        this.masterAgreementSignDate = masterAgreementSignDate;
    }

    public MasterAgreementNoVersionEnum getMasterAgreementNoVersion() {
        return masterAgreementNoVersion;
    }

    public void setMasterAgreementNoVersion(MasterAgreementNoVersionEnum masterAgreementNoVersion) {
        this.masterAgreementNoVersion = masterAgreementNoVersion;
    }

    public String getBusinessLicense() {
        return businessLicense;
    }

    public void setBusinessLicense(String businessLicense) {
        this.businessLicense = businessLicense;
    }
}

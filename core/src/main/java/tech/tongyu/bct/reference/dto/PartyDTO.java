package tech.tongyu.bct.reference.dto;


import tech.tongyu.bct.common.api.doc.BctField;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;

public class PartyDTO {
    //UUID
    @BctField(description = "唯一标识")
    private String uuid;
    //客户类型
    @BctField(description = "客户类型")
    private ClientTypeEnum clientType;
    //开户名称
    @BctField(description = "开户名称")
    private String legalName;
    //开户法人
    @BctField(description = "开户法人")
    private String legalRepresentative;
    //注册地址
    @BctField(description = "注册地址")
    private String address;
    //联系人
    @BctField(description = "联系人")
    private String contact;
    //担保人
    @BctField(description = "担保人")
    private String warrantor;
    //担保人地址
    @BctField(description = "担保人地址")
    private String warrantorAddress;
    //交易电话
    @BctField(description = "交易电话")
    private String tradePhone;
    //交易指定邮箱
    @BctField(description = "交易指定邮箱")
    private String tradeEmail;
    //分公司
    @BctField(description = "分公司")
    private String subsidiaryName;
    //营业部
    @BctField(description = "营业部")
    private String branchName;
    //销售
    @BctField(description = "销售")
    private String salesName;
    //主协议编号
    @BctField(description = "主协议编号")
    private String masterAgreementId;
    //补充协议编号
    @BctField(description = "补充协议编号")
    private String supplementalAgreementId;
    //主协议
    @BctField(description = "主协议")
    private String masterAgreementDoc;
    //补充协议
    @BctField(description = "补充协议")
    private String supplementalAgreementDoc;
    //风险问卷调查
    @BctField(description = "风险问卷调查")
    private String riskSurveyDoc;

    //交易授权书
    @BctField(description = "交易授权书")
    private String tradeAuthDoc;
    //对手尽职调查
    @BctField(description = "对手尽职调查")
    private String dueDiligenceDoc;
    //风险承受能力调查问卷
    @BctField(description = "风险承受能力调查问卷")
    private String riskPreferenceDoc;
    //合规性承诺书
    @BctField(description = "合规性承诺书")
    private String complianceDoc;
    //风险揭示书
    @BctField(description = "风险揭示书")
    private String riskRevelationDoc;
    //适当性警示书
    @BctField(description = "适当性警示书")
    private String qualificationWarningDoc;
    //授信协议
    @BctField(description = "授信协议")
    private String creditAgreement;
    //履约保障协议
    @BctField(description = "履约保障协议")
    private String performanceGuaranteeDoc;
    //对方授信额度
    @BctField(description = "对方授信额度")
    private Double cptyCreditLimit;
    //我方授信额度
    @BctField(description = "我方授信额度")
    private Double ourCreditLimit;
    //保证金折扣
    @BctField(description = "保证金折扣")
    private Double marginDiscountRate;
    //机构类型分类
    @BctField(description = "机构类型分类")
    private InvestorTypeEnum investorType;
    //交易方向
    @BctField(description = "交易方向")
    private TradingDirectionEnum tradingDirection;
    //交易权限
    @BctField(description = "交易权限")
    private TradingPermissionEnum tradingPermission;
    //交易权限备注
    @BctField(description = "交易权限备注")
    private String tradingPermissionNote;
    //交易标的
    @BctField(description = "交易标的")
    private TradingUnderlyersEnum tradingUnderlyers;
    //托管邮箱
    @BctField(description = "托管邮箱")
    private String trustorEmail;
    //授权到期日v
    @BctField(description = "授权到期日")
    private LocalDate authorizeExpiryDate;
    //协议签署授权人姓名
    @BctField(description = "协议签署授权人姓名")
    private String signAuthorizerName;
    //协议签署授权人身份证
    @BctField(description = "协议签署授权人身份证")
    private String signAuthorizerIdNumber;
    //协议签署授权人证件有效期
    @BctField(description = "协议签署授权人证件有效期")
    private LocalDate signAuthorizerIdExpiryDate;
    //交易授权人信息
    @BctField(description = "交易授权人信息")
    private Collection<AuthorizerDTO> authorizers;
    //产品名称
    @BctField(description = "产品名称")
    private String productName;
    //产品代码
    @BctField(description = "产品代码")
    private String productCode;
    //产品类型
    @BctField(description = "产品类型")
    private String productType;
    //备案编号
    @BctField(description = "备案编号")
    private String recordNumber;
    //产品成立日
    @BctField(description = "产品成立日")
    private LocalDate productFoundDate;
    //产品到期日
    @BctField(description = "产品到期日")
    private LocalDate productExpiringDate;
    //基金经理
    @BctField(description = "基金经理")
    private String fundManager;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private PartyStatusEnum partyStatus;
    //主协议签订日期
    private LocalDate masterAgreementSignDate;
    //主协议编号版本
    private MasterAgreementNoVersionEnum masterAgreementNoVersion;
    //营业执照
    private String businessLicense;

    public PartyDTO() {
    }

    public PartyDTO(String uuid, ClientTypeEnum clientType, String legalName, String legalRepresentative,
                    String address, String contact, String warrantor, String warrantorAddress, String tradePhone,
                    String tradeEmail, String subsidiaryName, String branchName, String salesName,
                    String masterAgreementId, String supplementalAgreementId, String masterAgreementDoc,
                    String supplementalAgreementDoc, String riskSurveyDoc, String tradeAuthDoc, String dueDiligenceDoc,
                    String riskPreferenceDoc, String complianceDoc, String riskRevelationDoc,
                    String qualificationWarningDoc, String creditAgreement, String performanceGuaranteeDoc,
                    Double cptyCreditLimit, Double ourCreditLimit, Double marginDiscountRate,
                    InvestorTypeEnum investorType, TradingDirectionEnum tradingDirection,
                    TradingPermissionEnum tradingPermission, String tradingPermissionNote,
                    TradingUnderlyersEnum tradingUnderlyers, String trustorEmail, LocalDate authorizeExpiryDate,
                    String signAuthorizerName, String signAuthorizerIdNumber, LocalDate signAuthorizerIdExpiryDate,
                    Collection<AuthorizerDTO> authorizers, String productName, String productCode, String productType,
                    String recordNumber, LocalDate productFoundDate, LocalDate productExpiringDate, String fundManager,
                    PartyStatusEnum partyStatus, LocalDate masterAgreementSignDate, MasterAgreementNoVersionEnum masterAgreementNoVersion,
                    String businessLicense) {
        this.uuid = uuid;
        this.clientType = clientType;
        this.legalName = legalName;
        this.legalRepresentative = legalRepresentative;
        this.address = address;
        this.contact = contact;
        this.warrantor = warrantor;
        this.warrantorAddress = warrantorAddress;
        this.tradePhone = tradePhone;
        this.tradeEmail = tradeEmail;
        this.subsidiaryName = subsidiaryName;
        this.branchName = branchName;
        this.salesName = salesName;
        this.masterAgreementId = masterAgreementId;
        this.supplementalAgreementId = supplementalAgreementId;
        this.masterAgreementDoc = masterAgreementDoc;
        this.supplementalAgreementDoc = supplementalAgreementDoc;
        this.riskSurveyDoc = riskSurveyDoc;
        this.tradeAuthDoc = tradeAuthDoc;
        this.dueDiligenceDoc = dueDiligenceDoc;
        this.riskPreferenceDoc = riskPreferenceDoc;
        this.complianceDoc = complianceDoc;
        this.riskRevelationDoc = riskRevelationDoc;
        this.qualificationWarningDoc = qualificationWarningDoc;
        this.creditAgreement = creditAgreement;
        this.performanceGuaranteeDoc = performanceGuaranteeDoc;
        this.cptyCreditLimit = cptyCreditLimit;
        this.ourCreditLimit = ourCreditLimit;
        this.marginDiscountRate = marginDiscountRate;
        this.investorType = investorType;
        this.tradingDirection = tradingDirection;
        this.tradingPermission = tradingPermission;
        this.tradingPermissionNote = tradingPermissionNote;
        this.tradingUnderlyers = tradingUnderlyers;
        this.trustorEmail = trustorEmail;
        this.authorizeExpiryDate = authorizeExpiryDate;
        this.signAuthorizerName = signAuthorizerName;
        this.signAuthorizerIdNumber = signAuthorizerIdNumber;
        this.signAuthorizerIdExpiryDate = signAuthorizerIdExpiryDate;
        this.authorizers = authorizers;
        this.productName = productName;
        this.productCode = productCode;
        this.productType = productType;
        this.recordNumber = recordNumber;
        this.productFoundDate = productFoundDate;
        this.productExpiringDate = productExpiringDate;
        this.fundManager = fundManager;
        this.partyStatus = partyStatus;
        this.masterAgreementSignDate = masterAgreementSignDate;
        this.masterAgreementNoVersion = masterAgreementNoVersion;
        this.businessLicense = businessLicense;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
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

    public Collection<AuthorizerDTO> getAuthorizers() {
        return authorizers;
    }

    public void setAuthorizers(Collection<AuthorizerDTO> authorizers) {
        this.authorizers = authorizers;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
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

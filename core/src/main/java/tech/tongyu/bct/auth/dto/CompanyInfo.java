package tech.tongyu.bct.auth.dto;

import tech.tongyu.bct.common.api.doc.BctField;

public class CompanyInfo {

    @BctField(
            name = "companyName",
            description = "公司名称",
            type = "String",
            order = 1
    )
    private String companyName;
    @BctField(
            name = "description",
            description = "公司描述信息",
            type = "String",
            order = 2
    )
    private String description;
    @BctField(
            name = "companyType",
            description = "公司类型",
            type = "String",
            order = 3
    )
    private String companyType;
    @BctField(
            name = "unifiedSocialCreditCode",
            description = "统一社会信用代码",
            type = "String",
            order = 4
    )
    private String unifiedSocialCreditCode;
    @BctField(
            name = "legalPerson",
            description = "法人",
            type = "String",
            order = 5
    )
    private String legalPerson;
    @BctField(
            name = "contactEmail",
            description = "联系邮箱",
            type = "String",
            order = 6
    )
    private String contactEmail;

    public CompanyInfo(String companyName, String companyType, String unifiedSocialCreditCode, String legalPerson, String contactEmail, String description){
        this.companyName = companyName;
        this.companyType = companyType;
        this.unifiedSocialCreditCode = unifiedSocialCreditCode;
        this.legalPerson = legalPerson;
        this.contactEmail = contactEmail;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyType() {
        return companyType;
    }

    public void setCompanyType(String companyType) {
        this.companyType = companyType;
    }

    public String getUnifiedSocialCreditCode() {
        return unifiedSocialCreditCode;
    }

    public void setUnifiedSocialCreditCode(String unifiedSocialCreditCode) {
        this.unifiedSocialCreditCode = unifiedSocialCreditCode;
    }

    public String getLegalPerson() {
        return legalPerson;
    }

    public void setLegalPerson(String legalPerson) {
        this.legalPerson = legalPerson;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }
}

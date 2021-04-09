package tech.tongyu.bct.auth.dao.entity;

import tech.tongyu.bct.auth.dao.common.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(schema = EntityConstants.AUTH_SERVICE,
        name = EntityConstants.AUTH_COMPANY)
public class CompanyDbo extends BaseEntity {

    @Column(name = EntityConstants.COMPANY_NAME)
    private String companyName;

    @Column(name = EntityConstants.COMPANY_TYPE)
    private String companyType;

    @Column(name = EntityConstants.DESCRIPTION)
    private String description;

    @Column(name = EntityConstants.UNIFIED_SOCIAL_CREDIT_CODE)
    private String unifiedSocialCreditCode;

    @Column(name = EntityConstants.LEGAL_PERSON)
    private String legalPerson;

    @Column(name = EntityConstants.CONTACT_EMAIL)
    private String contactEmail;

    public CompanyDbo(){
        super();
    }

    public CompanyDbo(
            String companyName
            , String companyType
            , String description
            , String unifiedSocialCreditCode
            , String legalPerson
            , String contactEmail){
        super();
        this.companyName = companyName;
        this.companyType = companyType;
        this.description = description;
        this.unifiedSocialCreditCode = unifiedSocialCreditCode;
        this.legalPerson = legalPerson;
        this.contactEmail = contactEmail;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

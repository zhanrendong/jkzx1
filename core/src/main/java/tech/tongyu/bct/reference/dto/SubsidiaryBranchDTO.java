package tech.tongyu.bct.reference.dto;

import tech.tongyu.bct.common.api.doc.BctField;

import java.util.UUID;

public class SubsidiaryBranchDTO {
    @BctField(description = "分公司名称")
    private String subsidiaryName;
    @BctField(description = "分公司唯一标识")
    private UUID subsidiaryId;
    @BctField(description = "营业部名称")
    private String branchName;
    @BctField(description = "营业部唯一标识")
    private UUID branchId;

    public SubsidiaryBranchDTO() {
    }

    public SubsidiaryBranchDTO(String subsidiaryName, UUID subsidiaryId, String branchName, UUID branchId) {
        this.subsidiaryName = subsidiaryName;
        this.subsidiaryId = subsidiaryId;
        this.branchName = branchName;
        this.branchId = branchId;
    }

    public String getSubsidiaryName() {
        return subsidiaryName;
    }

    public void setSubsidiaryName(String subsidiaryName) {
        this.subsidiaryName = subsidiaryName;
    }

    public UUID getSubsidiaryId() {
        return subsidiaryId;
    }

    public void setSubsidiaryId(UUID subsidiaryId) {
        this.subsidiaryId = subsidiaryId;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public UUID getBranchId() {
        return branchId;
    }

    public void setBranchId(UUID branchId) {
        this.branchId = branchId;
    }
}

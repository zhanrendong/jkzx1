package tech.tongyu.bct.reference.dto;

import tech.tongyu.bct.common.api.doc.BctField;

import java.time.Instant;
import java.util.UUID;

public class SalesDTO {
    @BctField(description = "销售唯一标识")
    private UUID uuid;
    @BctField(description = "营业部唯一标识")
    private UUID branchId;
    @BctField(description = "分公司唯一标识")
    private UUID subsidiaryId;
    @BctField(description = "销售名称")
    private String salesName;
    @BctField(description = "营业部名称")
    private String branchName;
    @BctField(description = "分公司名称")
    private String subsidiaryName;
    @BctField(description = "创建时间")
    private Instant createdAt;

    public SalesDTO() {
    }

    public SalesDTO(UUID uuid, String salesName,String subsidiaryName,String branchName,Instant createdAt) {
        this.uuid = uuid;
        this.salesName = salesName;
        this.branchName = branchName;
        this.subsidiaryName = subsidiaryName;
        this.createdAt = createdAt;
    }

    public SalesDTO(UUID uuid, UUID branchId, UUID subsidiaryId, String salesName, String branchName,
                    String subsidiaryName, Instant createdAt) {
        this.uuid = uuid;
        this.branchId = branchId;
        this.subsidiaryId = subsidiaryId;
        this.salesName = salesName;
        this.branchName = branchName;
        this.subsidiaryName = subsidiaryName;
        this.createdAt = createdAt;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }


    public UUID getBranchId() {
        return branchId;
    }

    public void setBranchId(UUID branchId) {
        this.branchId = branchId;
    }

    public UUID getSubsidiaryId() {
        return subsidiaryId;
    }

    public void setSubsidiaryId(UUID subsidiaryId) {
        this.subsidiaryId = subsidiaryId;
    }

    public String getSalesName() {
        return salesName;
    }

    public void setSalesName(String salesName) {
        this.salesName = salesName;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getSubsidiaryName() {
        return subsidiaryName;
    }

    public void setSubsidiaryName(String subsidiaryName) {
        this.subsidiaryName = subsidiaryName;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}

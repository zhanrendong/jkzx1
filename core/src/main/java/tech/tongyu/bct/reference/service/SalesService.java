package tech.tongyu.bct.reference.service;

import tech.tongyu.bct.reference.dto.SalesDTO;
import tech.tongyu.bct.reference.dto.SubsidiaryBranchDTO;

import java.util.List;
import java.util.UUID;

public interface SalesService {
    String SCHEMA = "referenceDataService";

    SubsidiaryBranchDTO createSubsidiary(String subsidiaryName);

    SubsidiaryBranchDTO createBranch(UUID subsidiaryId, String branchName);

    SalesDTO createSales(UUID branchId, String salesName);

    SubsidiaryBranchDTO updateSubsidiary(UUID subsidiaryId, String subsidiaryName);

    SubsidiaryBranchDTO updateBranch(UUID branchId, String branchName);

    SalesDTO updateSales(UUID salesId, UUID branchId, String salesName);

    List<SalesDTO> listSales();

    SubsidiaryBranchDTO deleteSubsidiary(UUID subsidiaryId);

    SubsidiaryBranchDTO deleteBranch(UUID branchId);

    SalesDTO deleteSales(UUID salesId);

    List<SubsidiaryBranchDTO> listSubsidiaryBranches();

    List<String> listBySimilarSalesName(String similarSalesName);
}

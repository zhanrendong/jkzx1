package tech.tongyu.bct.reference.api;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.tongyu.bct.common.api.annotation.BctMethodArg;
import tech.tongyu.bct.common.api.annotation.BctMethodInfo;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.exception.ErrorCode;
import tech.tongyu.bct.reference.dto.SalesDTO;
import tech.tongyu.bct.reference.dto.SubsidiaryBranchDTO;
import tech.tongyu.bct.reference.service.SalesService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SalesApi {
    private SalesService salesService;

    @Autowired
    public SalesApi(SalesService salesService) {
        this.salesService = salesService;
    }

    @BctMethodInfo(
            description = "创建分公司",
            retName = "SubsidiaryBranchDTO",
            retDescription = "分公司信息",
            returnClass = SubsidiaryBranchDTO.class,
            service = "reference-data-service"
    )
    public SubsidiaryBranchDTO refSubsidiaryCreate(
            @BctMethodArg(description = "分公司名称") String subsidiaryName
    ) {
        if (StringUtils.isBlank(subsidiaryName)){
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "分公司名称不可为空");
        }
       return salesService.createSubsidiary(subsidiaryName);
    }

    @BctMethodInfo(
            description = "创建营业部",
            retName = "SubsidiaryBranchDTO",
            retDescription = "分公司信息",
            returnClass = SubsidiaryBranchDTO.class,
            service = "reference-data-service"
    )
    public SubsidiaryBranchDTO refBranchCreate(
            @BctMethodArg(description = "分公司唯一标识") String subsidiaryId,
            @BctMethodArg(description = "营业部名称") String branchName ) {
        if (StringUtils.isBlank(branchName)){
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "营业部名称不可为空");
        }
        return salesService.createBranch(UUID.fromString(subsidiaryId), branchName);
    }

    @BctMethodInfo(
            description = "创建销售",
            retName = "SalesDTO",
            retDescription = "销售信息",
            returnClass = SalesDTO.class,
            service = "reference-data-service"
    )
    public SalesDTO refSalesCreate(
            @BctMethodArg(description = "营业部唯一标识") String branchId,
            @BctMethodArg(description = "销售名称") String salesName
    ) {
        if (StringUtils.isBlank(salesName)){
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "销售姓名不可为空");
        }
        return salesService.createSales(UUID.fromString(branchId), salesName);
    }

    @BctMethodInfo(
            description = "更新分公司",
            retName = "SubsidiaryBranchDTO",
            retDescription = "分公司信息",
            returnClass = SubsidiaryBranchDTO.class,
            service = "reference-data-service"
    )
    public SubsidiaryBranchDTO refSubsidiaryUpdate(
            @BctMethodArg(description = "分公司唯一标识") String subsidiaryId,
            @BctMethodArg(description = "分公司名称") String subsidiaryName) {
        return salesService.updateSubsidiary(UUID.fromString(subsidiaryId),subsidiaryName);
    }

    @BctMethodInfo(
            description = "更新营业部",
            retName = "SubsidiaryBranchDTO",
            retDescription = "分公司信息",
            returnClass = SubsidiaryBranchDTO.class,
            service = "reference-data-service"
    )
    public SubsidiaryBranchDTO refBranchUpdate(
            @BctMethodArg(description = "营业部唯一标识") String branchId,
            @BctMethodArg(description = "营业部名称") String branchName ) {
        return salesService.updateBranch(UUID.fromString(branchId), branchName);
    }

    @BctMethodInfo(
            description = "更新销售",
            retName = "SalesDTO",
            retDescription = "销售信息",
            returnClass = SalesDTO.class,
            service = "reference-data-service"
    )
    public SalesDTO refSalesUpdate(
            @BctMethodArg(description = "销售唯一标识") String salesId,
            @BctMethodArg(description = "营业部唯一标识") String branchId,
            @BctMethodArg(description = "销售名称") String salesName) {
        return salesService.updateSales(UUID.fromString(salesId),UUID.fromString(branchId), salesName);
    }

    @BctMethodInfo(
            description = "查询销售列表",
            retName = "List<SalesDTO>",
            retDescription = "销售信息",
            returnClass = SalesDTO.class,
            service = "reference-data-service"
    )
    public List<SalesDTO> refSalesList(
            @BctMethodArg(required = false, description = "营业部唯一标识") String branchId,
            @BctMethodArg(required = false, description = "分公司唯一标识") String subsidiaryId
    ) {
        List<SalesDTO> sales = salesService.listSales();
        if(!StringUtils.isBlank(branchId)){
            sales = sales.stream().filter(sale -> sale.getBranchId().equals(UUID.fromString(branchId)))
                    .collect(Collectors.toList());
        }
        if(!StringUtils.isBlank(subsidiaryId)){
            sales = sales.stream().filter(sale -> sale.getSubsidiaryId().equals(UUID.fromString(subsidiaryId)))
                    .collect(Collectors.toList());
        }
        return sales;
    }

    @BctMethodInfo(
            description = "删除销售信息",
            retName = "SalesDTO",
            retDescription = "销售信息",
            returnClass = SalesDTO.class,
            service = "reference-data-service"
    )
    public SalesDTO refSalesDelete(
            @BctMethodArg(description = "销售唯一标识") String salesId
    ) {
        return salesService.deleteSales(UUID.fromString(salesId));
    }

    @BctMethodInfo(
            description = "删除营业部信息",
            retName = "SubsidiaryBranchDTO",
            retDescription = "分公司信息",
            returnClass = SubsidiaryBranchDTO.class,
            service = "reference-data-service"
    )
    public SubsidiaryBranchDTO refBranchDelete(
            @BctMethodArg(description = "营业部唯一标识") String branchId
    ) {
        return salesService.deleteBranch(UUID.fromString(branchId));
    }

    @BctMethodInfo(
            description = "删除分公司信息",
            retName = "SubsidiaryBranchDTO",
            retDescription = "分公司信息",
            returnClass = SubsidiaryBranchDTO.class,
            service = "reference-data-service"
    )
    public SubsidiaryBranchDTO refSubsidiaryDelete(
            @BctMethodArg(description = "分公司唯一标识") String subsidiaryId
    ) {
        return salesService.deleteSubsidiary(UUID.fromString(subsidiaryId));
    }

    @BctMethodInfo(
            description = "查询分公司信息",
            retName = "List<SubsidiaryBranchDTO>",
            retDescription = "分公司信息",
            returnClass = SubsidiaryBranchDTO.class,
            service = "reference-data-service"
    )
    public List<SubsidiaryBranchDTO> refSubsidiaryBranchList() {
        return salesService.listSubsidiaryBranches();
    }

    @BctMethodInfo(
            description = "模糊查询销售名称",
            service = "reference-data-service"
    )
    public List<String> refSimilarSalesNameList(
            @BctMethodArg(description = "模糊查询条件") String similarSalesName
    ){
        return salesService.listBySimilarSalesName(similarSalesName);
    }
}

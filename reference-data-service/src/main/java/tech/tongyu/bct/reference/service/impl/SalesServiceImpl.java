package tech.tongyu.bct.reference.service.impl;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.exception.ErrorCode;
import tech.tongyu.bct.reference.dao.dbo.Branch;
import tech.tongyu.bct.reference.dao.dbo.Sales;
import tech.tongyu.bct.reference.dao.dbo.Subsidiary;
import tech.tongyu.bct.reference.dao.repl.intel.BranchRepo;
import tech.tongyu.bct.reference.dao.repl.intel.SalesRepo;
import tech.tongyu.bct.reference.dao.repl.intel.SubsidiaryRepo;
import tech.tongyu.bct.reference.dto.SalesDTO;
import tech.tongyu.bct.reference.dto.SubsidiaryBranchDTO;
import tech.tongyu.bct.reference.service.SalesService;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SalesServiceImpl implements SalesService {
    private SalesRepo salesRepo;
    private BranchRepo branchRepo;
    private SubsidiaryRepo subsidiaryRepo;

    @Autowired
    public SalesServiceImpl(SalesRepo salesRepo, BranchRepo branchRepo, SubsidiaryRepo subsidiaryRepo) {
        this.salesRepo = salesRepo;
        this.branchRepo = branchRepo;
        this.subsidiaryRepo = subsidiaryRepo;
    }

    @Override
    @Transactional
    public SubsidiaryBranchDTO createSubsidiary(String subsidiaryName) {
        if (subsidiaryRepo.existsBySubsidiaryName(subsidiaryName)) {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "已存在同名分公司");
        }
        Subsidiary subsidiary = new Subsidiary(subsidiaryName);
        return convert(subsidiaryRepo.save(subsidiary));
    }

    @Override
    @Transactional
    public SubsidiaryBranchDTO createBranch(UUID subsidiaryId, String branchName) {
        if (branchRepo.existsBySubsidiaryIdAndBranchName(subsidiaryId, branchName)) {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "该分公司下存在同名营业部");
        }
        subsidiaryRepo.findById(subsidiaryId).orElseThrow(() -> new CustomException(ErrorCode.INPUT_NOT_VALID, "分公司不存在"));
        return convert(branchRepo.save(new Branch(subsidiaryId, branchName)));
    }

    @Override
    @Transactional
    public SalesDTO createSales(UUID branchId, String salesName) {
        branchRepo.findById(branchId)
                .orElseThrow(() -> new CustomException(ErrorCode.INPUT_NOT_VALID, "营业部不存在"));
        Optional<Sales> salesOptional = salesRepo.findByBranchIdAndSalesName(branchId, salesName);
        if (salesOptional.isPresent()){
            throw new CustomException(String.format("同营业部下已经存在[%s]同名销售", salesName));
        }
        Sales sales = new Sales(branchId, salesName);
        return convert(salesRepo.save(sales));
    }

    @Override
    @Transactional
    public SubsidiaryBranchDTO updateSubsidiary(UUID subsidiaryId, String subsidiaryName) {
        Subsidiary sub = subsidiaryRepo.findById(subsidiaryId)
                .orElseThrow(() -> new CustomException(ErrorCode.INPUT_NOT_VALID, "分公司不存在"));
        if (subsidiaryRepo.existsBySubsidiaryName(subsidiaryName)) {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "分公司名称已存在");
        }
        sub.setSubsidiaryName(subsidiaryName);
        return convert(subsidiaryRepo.save(sub));

    }

    @Override
    @Transactional
    public SubsidiaryBranchDTO updateBranch(UUID branchId, String branchName) {
        Branch branch = branchRepo.findById(branchId)
                .orElseThrow(() -> new CustomException(ErrorCode.INPUT_NOT_VALID, "营业部不存在"));
        if (branchRepo.existsBySubsidiaryIdAndBranchName(branch.getSubsidiaryId(), branchName)) {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "该分公司下存在同名营业部");
        }
        branch.setBranchName(branchName);
        return convert(branchRepo.save(branch));
    }

    @Override
    public SalesDTO updateSales(UUID salesId, UUID branchId, String salesName) {
        Sales sales = salesRepo.findById(salesId).orElseThrow(() -> new CustomException(ErrorCode.INPUT_NOT_VALID, "销售不存在"));
        sales.setBranchId(branchId);
        sales.setSalesName(salesName);
        try {
            return convert(salesRepo.save(sales));
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "同一营业部下不能存在同名销售");
        } catch (Exception e) {
            throw new RuntimeException("更新交易员信息失败");
        }
    }

    @Override
    @Transactional
    public SubsidiaryBranchDTO deleteSubsidiary(UUID subsidiaryId) {
        subsidiaryRepo.findById(subsidiaryId)
                .orElseThrow(() -> new CustomException(ErrorCode.INPUT_NOT_VALID, "该分公司不存在"));
        if (branchRepo.existsBySubsidiaryId(subsidiaryId)) {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "不能删除分公司,该分公司尚有营业部");
        }
        return convert(subsidiaryRepo.deleteByUuid(subsidiaryId).get(0));
    }

    @Override
    @Transactional
    public SubsidiaryBranchDTO deleteBranch(UUID branchId) {
        branchRepo.findById(branchId).orElseThrow(() -> new CustomException(ErrorCode.INPUT_NOT_VALID, "该营业部不存在"));
        if (salesRepo.existsByBranchId(branchId)) {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "不能删除营业部,该分营业部尚有销售");
        }
        return convert(branchRepo.deleteByUuid(branchId).get(0));
    }


    @Override
    @Transactional
    public SalesDTO deleteSales(UUID salesId) {
        salesRepo.findById(salesId).orElseThrow(() -> new CustomException(ErrorCode.INPUT_NOT_VALID, "该销售不存在"));
        return convert(salesRepo.deleteByUuid(salesId).get(0));
    }

    @Override
    public List<SalesDTO> listSales() {
        return salesRepo.listAll().stream()
                .map(t -> new SalesDTO((UUID) t.get(0), (UUID) t.get(1), (UUID) t.get(2),
                        (String) t.get(3), (String) t.get(4), (String) t.get(5), (Instant) t.get(6)))
                .sorted(Comparator.comparing(SalesDTO::getSubsidiaryName)
                        .thenComparing(SalesDTO::getBranchName)
                        .thenComparing(SalesDTO::getSalesName))
                .collect(Collectors.toList());
    }

    @Override
    public List<SubsidiaryBranchDTO> listSubsidiaryBranches() {
        Iterator<Subsidiary> subsidiaryiterator = subsidiaryRepo.findAll().iterator();
        Map<UUID, List<Branch>> branchMap = branchRepo.findAll().stream().collect(Collectors.groupingBy(Branch::getSubsidiaryId));
        List<SubsidiaryBranchDTO> result = Lists.newArrayList();

        while (subsidiaryiterator.hasNext()) {
            Subsidiary sub = subsidiaryiterator.next();
            if (branchMap.containsKey(sub.getUuid())) {
                result.addAll(branchMap.get(sub.getUuid()).stream().map(branch -> convert(branch)).collect(Collectors.toList()));
            } else {
                result.add(convert(sub));
            }
        }
        return result;
    }

    @Override
    public List<String> listBySimilarSalesName(String similarSalesName) {
        return salesRepo.findBySalesNameContaining(similarSalesName)
                .stream()
                .map(Sales::getSalesName)
                .collect(Collectors.toList());
    }

    private SubsidiaryBranchDTO convert(Branch branch) {
        Subsidiary subsidiary = subsidiaryRepo.findById(branch.getSubsidiaryId()).get();
        return new SubsidiaryBranchDTO(subsidiary.getSubsidiaryName(), subsidiary.getUuid(),
                branch.getBranchName(), branch.getUuid());
    }

    private SubsidiaryBranchDTO convert(Subsidiary subsidiary) {
        return new SubsidiaryBranchDTO(subsidiary.getSubsidiaryName(), subsidiary.getUuid(),
                "", null);
    }

    private SalesDTO convert(Sales sales) {
        Branch branch = branchRepo.findById(sales.getBranchId()).get();
        Subsidiary subsidiary = subsidiaryRepo.findById(branch.getSubsidiaryId()).get();
        return new SalesDTO(sales.getUuid(), sales.getSalesName(), subsidiary.getSubsidiaryName(), branch.getBranchName(), sales.getCreatedAt());
    }
}

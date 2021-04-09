package tech.tongyu.bct.pricing.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.exception.ErrorCode;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.pricing.dao.dbo.CashRule;
import tech.tongyu.bct.pricing.dao.dbo.LinearProductRule;
import tech.tongyu.bct.pricing.dao.dbo.PricingEnvironment;
import tech.tongyu.bct.pricing.dao.dbo.SingleAssetOptionRule;
import tech.tongyu.bct.pricing.dao.dto.CashRuleDTO;
import tech.tongyu.bct.pricing.dao.dto.LinearProductRuleDTO;
import tech.tongyu.bct.pricing.dao.dto.PricingEnvironmentDTO;
import tech.tongyu.bct.pricing.dao.dto.SingleAssetOptionRuleDTO;
import tech.tongyu.bct.pricing.dao.repo.intel.CashRuleRepo;
import tech.tongyu.bct.pricing.dao.repo.intel.LinearProductRuleRepo;
import tech.tongyu.bct.pricing.dao.repo.intel.PricingEnvironmentRepo;
import tech.tongyu.bct.pricing.dao.repo.intel.SingleAssetOptionRuleRepo;
import tech.tongyu.bct.pricing.service.PricingEnvironmentDataService;
import tech.tongyu.bct.quant.library.common.QuantPricerSpec;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PricingEnvironmentDataServiceImpl implements PricingEnvironmentDataService {
    private PricingEnvironmentRepo pricingEnvironmentRepo;
    private CashRuleRepo cashRuleRepo;
    private LinearProductRuleRepo linearProductRuleRepo;
    private SingleAssetOptionRuleRepo singleAssetOptionRuleRepo;

    @Autowired
    public PricingEnvironmentDataServiceImpl(
            PricingEnvironmentRepo pricingEnvironmentRepo,
            CashRuleRepo cashRuleRepo,
            LinearProductRuleRepo linearProductRuleRepo,
            SingleAssetOptionRuleRepo singleAssetOptionRuleRepo) {
        this.pricingEnvironmentRepo = pricingEnvironmentRepo;
        this.cashRuleRepo = cashRuleRepo;
        this.linearProductRuleRepo = linearProductRuleRepo;
        this.singleAssetOptionRuleRepo = singleAssetOptionRuleRepo;
    }

    private CashRuleDTO convert(CashRule rule) {
        return new CashRuleDTO(rule.getDiscounted(), rule.getCurveName(), rule.getCurveInstance());
    }

    private CashRule convert(String pricingEnvironmentId, CashRuleDTO dto) {
        return new CashRule(pricingEnvironmentId, dto.getDiscounted(), dto.getCurveName(), dto.getInstance());
    }

    private LinearProductRuleDTO convert(LinearProductRule rule) {
        return new LinearProductRuleDTO(rule.getAssetClass(), rule.getInstrumentType(),
                rule.getInstance(), rule.getField());
    }

    private LinearProductRule convert(String pricingEnvironmentId, LinearProductRuleDTO dto) {
        return new LinearProductRule(pricingEnvironmentId, dto.getAssetClass(),
                dto.getInstrumentType(), dto.getInstance(), dto.getField());
    }

    private SingleAssetOptionRuleDTO convert(SingleAssetOptionRule rule) {
        QuantPricerSpec pricer = JsonUtils.mapper.convertValue(rule.getPricer(), QuantPricerSpec.class);
        return new SingleAssetOptionRuleDTO(rule.getAssetClass(), rule.getProductType(),
                rule.getUnderlyerType(), rule.getUnderlyerInstance(),
                rule.getUnderlyerField(), rule.getPositionId(),
                rule.getDiscountingCurveName(), rule.getDiscountingCurveInstance(),
                rule.getVolSurfaceName(), rule.getVolSurfaceInstance(),
                rule.getDividendCurveName(), rule.getDividendCurveInstance(),
                pricer);
    }

    private SingleAssetOptionRule convert(String pricingEnvironmentId, SingleAssetOptionRuleDTO dto) {
        return new SingleAssetOptionRule(pricingEnvironmentId, dto.getAssetClass(), dto.getProductType(),
                dto.getUnderlyerType(), dto.getUnderlyerInstance(), dto.getUnderlyerField(),
                dto.getPositionId(), dto.getDiscountingCurveName(), dto.getDiscountingCurveInstance(),
                dto.getDividendCurveName(), dto.getDividendCurveInstance(),
                dto.getVolSurfaceName(), dto.getVolSurfaceInstance(),
                JsonUtils.mapper.valueToTree(dto.getPricer()));
    }

    @Override
    public PricingEnvironmentDTO load(String pricingEnvironmentId) {
        Optional<CashRule> cashRule = cashRuleRepo.findByPricingEnvironmentId(pricingEnvironmentId);
        return new PricingEnvironmentDTO(
                pricingEnvironmentId,
                cashRuleRepo.findByPricingEnvironmentId(pricingEnvironmentId).map(this::convert).orElse(null),
                linearProductRuleRepo.findByPricingEnvironmentId(pricingEnvironmentId).stream()
                        .map(this::convert)
                .collect(Collectors.toList()),
                singleAssetOptionRuleRepo.findByPricingEnvironmentId(pricingEnvironmentId)
                        .stream()
                        .map(this::convert)
                        .collect(Collectors.toList()),
                pricingEnvironmentRepo.findByPricingEnvironmentId(pricingEnvironmentId)
                        .map(PricingEnvironment::getDescription).orElse(""));
    }

    @Override
    @Transactional
    public PricingEnvironmentDTO create(PricingEnvironmentDTO dto) {
        String pricingEnvironmentId = dto.getPricingEnvironmentId();
        Optional<PricingEnvironment> existed = pricingEnvironmentRepo.findByPricingEnvironmentId(pricingEnvironmentId);
        if (existed.isPresent()) {
            throw new CustomException(ErrorCode.DUPLICATE_ENTITY,
                    String.format("定价环境(%s)已存在。请先删除再创建。", pricingEnvironmentId));
        }

        Optional<CashRule> cashRule = cashRuleRepo.findByPricingEnvironmentId(pricingEnvironmentId);
        List<SingleAssetOptionRule> singleAssetOptionRules = singleAssetOptionRuleRepo
                .findByPricingEnvironmentId(pricingEnvironmentId);
        List<LinearProductRule> linearProductRules = linearProductRuleRepo
                .findByPricingEnvironmentId(pricingEnvironmentId);
        if (cashRule.isPresent() || linearProductRules.size() > 0 || singleAssetOptionRules.size() > 0) {
            throw new CustomException(ErrorCode.DUPLICATE_ENTITY,
                    String.format("定价环境(%s)已存在。请先删除再创建。", pricingEnvironmentId));
        }

        CashRuleDTO savedCashRule = convert(cashRuleRepo.save(convert(pricingEnvironmentId, dto.getCashRule())));

        List<LinearProductRuleDTO> savedLinearProductRule = linearProductRuleRepo.saveAll(
                dto.getLinearProductRules().stream()
                        .map(r -> convert(pricingEnvironmentId, r))
                        .collect(Collectors.toList()))
                .stream()
                .map(this::convert)
                .collect(Collectors.toList());

        List<SingleAssetOptionRuleDTO> savedSingleAssetOptionRule = singleAssetOptionRuleRepo.saveAll(
                dto.getSingleAssetOptionRules().stream()
                        .map(r -> convert(pricingEnvironmentId, r))
                        .collect(Collectors.toList()))
                .stream()
                .map(this::convert)
                .collect(Collectors.toList());
        String description = dto.getDescription();

        PricingEnvironment pe = new PricingEnvironment(pricingEnvironmentId, description);
        pricingEnvironmentRepo.save(pe);

        return new PricingEnvironmentDTO(pricingEnvironmentId,
                savedCashRule, savedLinearProductRule,
                savedSingleAssetOptionRule, description);
    }

    @Override
    @Transactional
    public String delete(String pricingEnvironmentId) {
        pricingEnvironmentRepo.deleteByPricingEnvironmentId(pricingEnvironmentId);
        CashRule deletedCashRule = cashRuleRepo.deleteByPricingEnvironmentId(pricingEnvironmentId).get(0);
        List<LinearProductRule> deletedLinearProductRules =
                linearProductRuleRepo.deleteByPricingEnvironmentId(pricingEnvironmentId);
        List<SingleAssetOptionRule> deletedOptionRules =
                singleAssetOptionRuleRepo.deleteByPricingEnvironmentId(pricingEnvironmentId);
        return pricingEnvironmentId;
        /*return new PricingEnvironmentDTO(pricingEnvironmentId,
                convert(deletedCashRule),
                deletedLinearProductRules.stream().map(this::convert).collect(Collectors.toList()),
                deletedOptionRules.stream().map(this::convert).collect(Collectors.toList()));*/
    }

    @Override
    public List<String> list() {
        return pricingEnvironmentRepo.findAllByOrderByPricingEnvironmentId().stream()
                .map(PricingEnvironment::getPricingEnvironmentId)
                .collect(Collectors.toList());
    }
}

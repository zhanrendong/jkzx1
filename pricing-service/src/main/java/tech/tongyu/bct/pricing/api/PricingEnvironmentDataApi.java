package tech.tongyu.bct.pricing.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.tongyu.bct.common.api.annotation.BctApiTagEnum;
import tech.tongyu.bct.common.api.annotation.BctExcelTypeEnum;
import tech.tongyu.bct.common.api.annotation.BctMethodArg;
import tech.tongyu.bct.common.api.annotation.BctMethodInfo;

import tech.tongyu.bct.common.util.DateTimeUtils;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.pricing.common.config.PricingConfig;
import tech.tongyu.bct.pricing.common.descriptor.PositionDescriptor;
import tech.tongyu.bct.pricing.dao.dto.CashRuleDTO;
import tech.tongyu.bct.pricing.dao.dto.LinearProductRuleDTO;
import tech.tongyu.bct.pricing.dao.dto.PricingEnvironmentDTO;
import tech.tongyu.bct.pricing.dao.dto.SingleAssetOptionRuleDTO;
import tech.tongyu.bct.pricing.environment.PricingEnvironment;
import tech.tongyu.bct.pricing.service.BaseContractService;
import tech.tongyu.bct.pricing.service.PricingEnvironmentDataService;
import tech.tongyu.bct.quant.library.priceable.Position;
import tech.tongyu.bct.quant.library.priceable.Priceable;
import tech.tongyu.bct.quant.service.cache.QuantlibObjectCache;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class PricingEnvironmentDataApi {
    private PricingEnvironmentDataService pricingEnvironmentDataService;
    private BaseContractService baseContractService;

    @Autowired
    public PricingEnvironmentDataApi(PricingEnvironmentDataService pricingEnvironmentDataService,
                                     BaseContractService baseContractService) {
        this.pricingEnvironmentDataService = pricingEnvironmentDataService;
        this.baseContractService = baseContractService;
    }

    @BctMethodInfo(
            description = "创建定价环境",
            retName = "PricingEnvironment",
            retDescription = "创建定价环境",
            returnClass = PricingEnvironmentDTO.class,
            service = "pricing-service"
    )
    public PricingEnvironmentDTO prcPricingEnvironmentCreate(
            @BctMethodArg(description = "定价环境名称") String pricingEnvironmentId,
            @BctMethodArg(description = "现金定价规则") Map<String, Object> cashRule,
            @BctMethodArg(description = "现货／期货定价规则") List<Map<String, Object>> linearProductRules,
            @BctMethodArg(description = "期权定价规则") List<Map<String, Object>> optionRules,
            @BctMethodArg(description = "定价环境说明", required = false) String description
    ) {
        PricingEnvironmentDTO dto = new PricingEnvironmentDTO(pricingEnvironmentId,
                JsonUtils.mapper.convertValue(cashRule, CashRuleDTO.class),
                linearProductRules.stream()
                        .map(r -> JsonUtils.mapper.convertValue(r, LinearProductRuleDTO.class))
                        .collect(Collectors.toList()),
                optionRules.stream()
                        .map(r -> JsonUtils.mapper.convertValue(r, SingleAssetOptionRuleDTO.class))
                        .collect(Collectors.toList()),
                Objects.isNull(description) ? pricingEnvironmentId : description);
        return pricingEnvironmentDataService.create(dto);
    }

    @BctMethodInfo(
            description = "查看定价环境",
            retName = "pricingEnvironment",
            retDescription = "返回系统当前可用定价环境",
            returnClass = PricingEnvironmentDTO.class,
            service = "pricing-service"
    )
    public PricingEnvironmentDTO prcPricingEnvironmentGet(
            @BctMethodArg(description = "定价环境名称") String pricingEnvironmentId) {
        return pricingEnvironmentDataService.load(pricingEnvironmentId);
    }

    @BctMethodInfo(
            description = "罗列已有定价环境",
            retName = "pricingEnvironment",
            retDescription = "返回系统当前可用定价环境",
            service = "pricing-service"
    )
    public List<String> prcPricingEnvironmentsList() {
        return pricingEnvironmentDataService.list();
    }

    @BctMethodInfo(
            description = "删除定价环境",
            retName = "pricingEnvironment",
            retDescription = "返回系统当前可用定价环境",
            service = "pricing-service"
    )
    public String prcPricingEnvironmentDelete(
            @BctMethodArg(description = "定价环境名称") String pricingEnvironmentId) {
        return pricingEnvironmentDataService.delete(pricingEnvironmentId);
    }

    @BctMethodInfo(
            description = "根据定价环境获取定价配置",
            retName = "pricingEnvironment",
            retDescription = "返回系统当前可用定价环境",
            returnClass = PricingConfig.class,
            service = "pricing-service",
            excelType = BctExcelTypeEnum.DataDict, tags = {BctApiTagEnum.Excel})
    public PricingConfig prcPlanPriceable(
            @BctMethodArg(description = "priceable") String priceable,
            @BctMethodArg(description = "pricing environment", required = false) String pricingEnvironmentId
    ) {
        ZonedDateTime t = DateTimeUtils.parse(null, null);
        PricingEnvironment pe;
        if ( Objects.isNull(pricingEnvironmentId) || pricingEnvironmentId.isEmpty()) {
            pe = PricingEnvironment.getDefaultPricingEnvironment(null);
        } else {
            pe = PricingEnvironment.from(pricingEnvironmentDataService.load(pricingEnvironmentId), null);
        }
        Priceable p = (Priceable) QuantlibObjectCache.Instance.getMayThrow(priceable);
        Position position = new Position("test", 1.0, p);
        return pe.plan(PositionDescriptor.getPositionDescriptor(position), t.toLocalDateTime(), t.getZone());
    }

    @BctMethodInfo
    public PricingConfig prcPlanPosition(
            @BctMethodArg String positionId,
            @BctMethodArg Number quantity,
            @BctMethodArg Map<String, Object> priceable,
            @BctMethodArg(required = false) String pricingEnvironmentId
    ) {
        ZonedDateTime t = DateTimeUtils.parse(null, null);
        PricingEnvironment pe;
        if ( Objects.isNull(pricingEnvironmentId) || pricingEnvironmentId.isEmpty()) {
            pe = PricingEnvironment.getDefaultPricingEnvironment(null);
        } else {
            pe = PricingEnvironment.from(pricingEnvironmentDataService.load(pricingEnvironmentId), null);
        }
        Priceable p = JsonUtils.mapper.convertValue(priceable, Priceable.class);
        Position position = new Position(positionId, quantity.doubleValue(), p);
        return pe.plan(PositionDescriptor.getPositionDescriptor(position), t.toLocalDateTime(), t.getZone());
    }
}

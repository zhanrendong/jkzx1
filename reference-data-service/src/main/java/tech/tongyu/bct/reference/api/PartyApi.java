package tech.tongyu.bct.reference.api;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.tongyu.bct.auth.dto.ResourceDTO;
import tech.tongyu.bct.auth.dto.ResourceDTO;
import tech.tongyu.bct.auth.enums.ResourcePermissionTypeEnum;
import tech.tongyu.bct.auth.enums.ResourceTypeEnum;
import tech.tongyu.bct.auth.service.ResourcePermissionService;
import tech.tongyu.bct.auth.service.ResourceService;
import tech.tongyu.bct.common.api.annotation.BctMethodArg;
import tech.tongyu.bct.common.api.annotation.BctMethodInfo;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.util.CollectionUtils;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.reference.dto.*;
import tech.tongyu.bct.reference.service.CompanyTypeInfoService;
import tech.tongyu.bct.reference.service.PartyService;
import tech.tongyu.bct.reference.util.Constant;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Collectors;

@Service
public class PartyApi {

    private ResourcePermissionService resourcePermissionService;
    private ResourceService resourceService;
    private PartyService partyService;
    private CompanyTypeInfoService companyTypeInfoService;

    @Autowired
    public PartyApi(PartyService partyService, ResourcePermissionService resourcePermissionService,
                    ResourceService resourceService, CompanyTypeInfoService companyTypeInfoService) {
        this.partyService = partyService;
        this.resourcePermissionService = resourcePermissionService;
        this.resourceService = resourceService;
        this.companyTypeInfoService = companyTypeInfoService;
    }

    @BctMethodInfo(
            description = "创建更新客户信息",
            retDescription = "客户信息",
            retName = "PartyDTO",
            returnClass = PartyDTO.class,
            service = "reference-data-service"
    )
    public PartyDTO refPartySave(@BctMethodArg(description = "UUID", required = false) String uuid,
                                 @BctMethodArg(description = "客户类型") String clientType,
                                 @BctMethodArg(description = "开户名称") String legalName,
                                 @BctMethodArg(description = "开户法人") String legalRepresentative,
                                 @BctMethodArg(description = "注册地址") String address,
                                 @BctMethodArg(description = "联系人") String contact,
                                 @BctMethodArg(description = "担保人") String warrantor,
                                 @BctMethodArg(description = "担保人地址") String warrantorAddress,
                                 @BctMethodArg(description = "交易电话") String tradePhone,
                                 @BctMethodArg(description = "交易指定邮箱") String tradeEmail,
                                 @BctMethodArg(description = "分公司") String subsidiaryName,
                                 @BctMethodArg(description = "营业部") String branchName,
                                 @BctMethodArg(description = "销售") String salesName,
                                 @BctMethodArg(description = "主协议编号") String masterAgreementId,
                                 @BctMethodArg(description = "补充协议编号", required = false) String supplementalAgreementId,
                                 @BctMethodArg(description = "主协议", required = false) String masterAgreementDoc,
                                 @BctMethodArg(description = "补充协议", required = false) String supplementalAgreementDoc,
                                 @BctMethodArg(description = "风险问卷调查", required = false) String riskSurveyDoc,
                                 @BctMethodArg(description = "交易授权书", required = false) String tradeAuthDoc,
                                 @BctMethodArg(description = "对手尽职调查", required = false) String dueDiligenceDoc,
                                 @BctMethodArg(description = "风险承受能力调查问卷", required = false) String riskPreferenceDoc,
                                 @BctMethodArg(description = "合规性承诺书", required = false) String complianceDoc,
                                 @BctMethodArg(description = "风险揭示书", required = false) String riskRevelationDoc,
                                 @BctMethodArg(description = "适当性警示书", required = false) String qualificationWarningDoc,
                                 @BctMethodArg(description = "授信协议", required = false) String creditAgreement,
                                 @BctMethodArg(description = "履约保障协议", required = false) String performanceGuaranteeDoc,
                                 @BctMethodArg(description = "对方授信额度", required = false) Number cptyCreditLimit,
                                 @BctMethodArg(description = "我方授信额度", required = false) Number ourCreditLimit,
                                 @BctMethodArg(description = "保证金折扣", required = false) Number marginDiscountRate,
                                 @BctMethodArg(description = "机构类型分类", required = false) String investorType,
                                 @BctMethodArg(description = "交易方向", required = false) String tradingDirection,
                                 @BctMethodArg(description = "交易权限", required = false) String tradingPermission,
                                 @BctMethodArg(description = "交易权限备注", required = false) String tradingPermissionNote,
                                 @BctMethodArg(description = "交易标的", required = false) String tradingUnderlyers,
                                 @BctMethodArg(description = "托管邮箱", required = false) String trustorEmail,
                                 @BctMethodArg(description = "授权到期日", required = false) String authorizeExpiryDate,
                                 @BctMethodArg(description = "协议签署授权人姓名", required = false) String signAuthorizerName,
                                 @BctMethodArg(description = "协议签署授权人身份证", required = false) String signAuthorizerIdNumber,
                                 @BctMethodArg(description = "协议签署授权人证件有效期", required = false) String signAuthorizerIdExpiryDate,
                                 @BctMethodArg(description = "交易授权人", required = false) Collection<Map<String, Object>> tradeAuthorizer,
                                 @BctMethodArg(description = "产品名称", required = false) String productName,
                                 @BctMethodArg(description = "产品代码", required = false) String productCode,
                                 @BctMethodArg(description = "产品类型", required = false) String productType,
                                 @BctMethodArg(description = "备案编号", required = false) String recordNumber,
                                 @BctMethodArg(description = "产品成立日", required = false) String productFoundDate,
                                 @BctMethodArg(description = "产品到期日", required = false) String productExpiringDate,
                                 @BctMethodArg(description = "基金经理", required = false) String fundManager,
                                 @BctMethodArg(description = "交易对手状态", required = false) String partyStatus,
                                 @BctMethodArg(description = "主协议签订日期", required = false) String masterAgreementSignDate,
                                 @BctMethodArg(description = "主协议编号版本", required = false) String masterAgreementNoVersion,
                                 @BctMethodArg(description = "营业执照", required = false) String businessLicense) {

        if (StringUtils.isEmpty(uuid)) {
            if (!hasPermission(ResourcePermissionTypeEnum.CREATE_CLIENT)) {
                throw new CustomException("没有创建客户的权限");
            }
        } else {
            if (!hasPermission(ResourcePermissionTypeEnum.UPDATE_CLIENT)) {
                throw new CustomException("没有修改客户的权限");
            }
            if (StringUtils.isBlank(partyStatus)) {
                throw new IllegalArgumentException("交易对手状态不能为空");
            }
        }

        if (StringUtils.isBlank(clientType)) {
            throw new IllegalArgumentException("请选择客户类型clientType");
        }
        if (StringUtils.isBlank(legalName)) {
            throw new IllegalArgumentException("请输入开户名称legalName");
        }
        if (StringUtils.isBlank(legalRepresentative)) {
            throw new IllegalArgumentException("请输入开发法人legalRepresentative");
        }
        if (StringUtils.isBlank(address)) {
            throw new IllegalArgumentException("请输入注册地质address");
        }
        if (StringUtils.isBlank(contact)) {
            throw new IllegalArgumentException("请输入联系人contact");
        }
        if (StringUtils.isBlank(warrantor)) {
            throw new IllegalArgumentException("请输入担保人warrantor");
        }
        if (StringUtils.isBlank(warrantorAddress)) {
            throw new IllegalArgumentException("请输入担保人地址warrantorAddress");
        }
        if (StringUtils.isBlank(tradePhone)) {
            throw new IllegalArgumentException("请输入交易电话tradePhone");
        }
        if (StringUtils.isBlank(tradeEmail)) {
            throw new IllegalArgumentException("请输入交易指定邮箱tradeEmail");
        }
        if (StringUtils.isBlank(subsidiaryName)) {
            throw new IllegalArgumentException("请输入分公司subsidiaryName");
        }
        if (StringUtils.isBlank(branchName)) {
            throw new IllegalArgumentException("请输入营业部branchName");
        }
        if (StringUtils.isBlank(salesName)) {
            throw new IllegalArgumentException("请输入销售salesName");
        }
        if (StringUtils.isBlank(masterAgreementId)) {
            throw new IllegalArgumentException("请输入主协议编号masterAgreementId");
        }

        Collection<AuthorizerDTO> authorizerDTOS = new ArrayList<>();
        if (!CollectionUtils.isEmpty(tradeAuthorizer)) {
            tradeAuthorizer.forEach(ta -> {
                AuthorizerDTO authorizerDTO = JsonUtils.mapper.convertValue(ta, AuthorizerDTO.class);
                authorizerDTOS.add(authorizerDTO);
            });
        }
        PartyDTO partyDTO = new PartyDTO(
                uuid,
                ClientTypeEnum.valueOf(clientType.toUpperCase()),
                legalName,
                legalRepresentative,
                address,
                contact,
                warrantor,
                warrantorAddress,
                tradePhone,
                tradeEmail,
                subsidiaryName,
                branchName,
                salesName,
                masterAgreementId,
                supplementalAgreementId,
                masterAgreementDoc,
                supplementalAgreementDoc,
                riskSurveyDoc,
                tradeAuthDoc,
                dueDiligenceDoc,
                riskPreferenceDoc,
                complianceDoc,
                riskRevelationDoc,
                qualificationWarningDoc,
                creditAgreement,
                performanceGuaranteeDoc,
                Objects.isNull(cptyCreditLimit) ? 0D : cptyCreditLimit.doubleValue(),
                Objects.isNull(ourCreditLimit) ? 0D : ourCreditLimit.doubleValue(),
                Objects.isNull(marginDiscountRate) ? 0D : marginDiscountRate.doubleValue(),
                StringUtils.isBlank(investorType) ? null : InvestorTypeEnum.valueOf(investorType.toUpperCase()),
                StringUtils.isBlank(tradingDirection) ? null : TradingDirectionEnum.valueOf(tradingDirection.toUpperCase()),
                StringUtils.isBlank(tradingPermission) ? null : TradingPermissionEnum.valueOf(tradingPermission.toUpperCase()),
                tradingPermissionNote,
                StringUtils.isBlank(tradingUnderlyers) ? null : TradingUnderlyersEnum.valueOf(tradingUnderlyers.toUpperCase()),
                trustorEmail,
                StringUtils.isBlank(authorizeExpiryDate) ? null : LocalDate.parse(authorizeExpiryDate),
                signAuthorizerName,
                signAuthorizerIdNumber,
                StringUtils.isBlank(signAuthorizerIdExpiryDate) ? null : LocalDate.parse(signAuthorizerIdExpiryDate),
                authorizerDTOS,
                productName,
                productCode,
                productType,
                recordNumber,
                StringUtils.isBlank(productFoundDate) ? null : LocalDate.parse(productFoundDate),
                StringUtils.isBlank(productExpiringDate) ? null : LocalDate.parse(productExpiringDate),
                fundManager,
                StringUtils.isBlank(partyStatus) ? null : PartyStatusEnum.valueOf(partyStatus),
                StringUtils.isBlank(masterAgreementSignDate) ? null : LocalDate.parse(masterAgreementSignDate),
                StringUtils.isBlank(masterAgreementNoVersion) ? null : MasterAgreementNoVersionEnum.valueOf(masterAgreementNoVersion.toUpperCase()),
                businessLicense);

        if (StringUtils.isBlank(uuid)) {
            partyDTO.setPartyStatus(PartyStatusEnum.NORMAL);
            return partyService.createParty(partyDTO);
        }
        return partyService.updateParty(partyDTO);
    }

    private Boolean hasPermission(ResourcePermissionTypeEnum resourcePermissionTypeEnum) {
        return resourcePermissionService.authCan(
                ResourceTypeEnum.CLIENT_INFO.toString(),
                Lists.newArrayList("客户信息"),
                resourcePermissionTypeEnum.toString()
        ).get(0);
    }

    @BctMethodInfo(description = "删除交易对手",
            retDescription = "交易对手信息",
            retName = "PartyDTO",
            returnClass = PartyDTO.class
    )
    public PartyDTO refPartyDelete(@BctMethodArg(description = "交易对手唯一标识") String uuid) {
        if (!hasPermission(ResourcePermissionTypeEnum.DELETE_CLIENT)) {
            throw new CustomException("没有删除用户的权限");
        }

        return this.partyService.deleteParty(uuid);
    }

    @BctMethodInfo(description = "根据交易对手获取客户信息",
            retDescription = "交易对手信息",
            retName = "PartyDTO",
            returnClass = PartyDTO.class,
            service = "reference-data-service"
    )
    public PartyDTO refPartyGetByLegalName(@BctMethodArg(description = "交易对手") String legalName) {
        if (StringUtils.isBlank(legalName)) {
            throw new IllegalArgumentException("请输入交易对手legalName");
        }
        return partyService.getByLegalName(legalName);
    }

    @BctMethodInfo(description = "禁用交易对手",
            retDescription = "交易对手信息",
            retName = "PartyDTO",
            returnClass = PartyDTO.class,
            service = "reference-data-service"
    )
    public PartyDTO refDisablePartyByLegalName(@BctMethodArg(description = "交易对手") String legalName) {
        PartyDTO partyDTO = partyService.getByLegalName(legalName);
        partyDTO.setPartyStatus(PartyStatusEnum.DISABLE);
        return partyService.updateParty(partyDTO);
    }

    @BctMethodInfo(description = "启用交易对手",
            retDescription = "交易对手信息",
            retName = "PartyDTO",
            returnClass = PartyDTO.class,
            service = "reference-data-service"
    )
    public PartyDTO refEnablePartyByLegalName(@BctMethodArg(description = "交易对手") String legalName) {
        PartyDTO partyDTO = partyService.getByLegalName(legalName);
        partyDTO.setPartyStatus(PartyStatusEnum.NORMAL);
        return partyService.updateParty(partyDTO);
    }

    @BctMethodInfo(
            description = "根据交易对手获取销售信息",
            retName = "SalesDTO",
            retDescription = "销售信息",
            returnClass = SalesDTO.class,
            service = "reference-data-service"
    )
    public SalesDTO refSalesGetByLegalName(
            @BctMethodArg(description = "交易对手") String legalName) {
        if (StringUtils.isBlank(legalName)) {
            throw new IllegalArgumentException("请输入交易对手legalName");
        }
        return partyService.getSalesByLegalName(legalName);
    }

    @BctMethodInfo(
            description = "客户信息查询",
            retDescription = "客户信息集合",
            retName = "List<PartyDTO>",
            returnClass = PartyDTO.class,
            service = "reference-data-service"
    )
    public List<PartyDTO> refPartyList(
            @BctMethodArg(description = "唯一标识", required = false) String uuid,
            @BctMethodArg(description = "交易对手", required = false) String legalName,
            @BctMethodArg(description = "主协议编号", required = false) String masterAgreementId,
            @BctMethodArg(description = "分公司", required = false) String subsidiaryName,
            @BctMethodArg(description = "营业部", required = false) String branchName,
            @BctMethodArg(description = "销售", required = false) String salesName,
            @BctMethodArg(description = "客户类型", required = false) String clientType) {

        if (!hasPermission(ResourcePermissionTypeEnum.READ_CLIENT)) {
            throw new CustomException("没有读取客户的权限");
        }

        PartyDTO partyDTO = new PartyDTO();
        partyDTO.setMasterAgreementId(killBlank(masterAgreementId));
        partyDTO.setSubsidiaryName(killBlank(subsidiaryName));
        partyDTO.setBranchName(killBlank(branchName));
        partyDTO.setSalesName(killBlank(salesName));
        partyDTO.setLegalName(killBlank(legalName));
        partyDTO.setUuid(killBlank(uuid));
        if (StringUtils.isNotBlank(clientType))
            partyDTO.setClientType(ClientTypeEnum.valueOf(clientType));

        return this.partyService.listParty(partyDTO);
    }

    @BctMethodInfo(
            description = "模糊查询交易对手名称信息",
            service = "reference-data-service"
    )
    public List<String> refSimilarLegalNameList(
            @BctMethodArg(description = "交易对手") String similarLegalName
    ) {
        return partyService.listBySimilarLegalName(similarLegalName, true);
    }

    @BctMethodInfo(description = "模糊查询交易对手名称信息,过滤掉和子公司类型交易对手")
    public List<String> refSimilarLegalNameListWithoutBook(@BctMethodArg(description = "交易对手") String similarLegalName) {
        Collection<String> allSubCompanies = companyTypeInfoService.getAllByLevelTwoType(Constant.COMPANY_TYPE_SUB_COMPANY)
                .stream().map(CompanyTypeInfoDTO::getClientName).collect(Collectors.toSet());
        return partyService.listBySimilarLegalName(similarLegalName, true)
                .stream().filter(v-> !allSubCompanies.contains(v)).collect(Collectors.toList());
    }

    @BctMethodInfo(
            description = "模糊查询可用的交易对手名称信息",
            service = "reference-data-service"
    )
    public List<String> refFuzzyQueryEnabledPartyNames(
            @BctMethodArg(description = "交易对手") String similarLegalName
    ) {
        return partyService.listBySimilarLegalName(similarLegalName, false);
    }

    @BctMethodInfo(
            description = "模糊查询SAC主协议",
            service = "reference-data-service"
    )
    public List<String> refMasterAgreementSearch(
            @BctMethodArg(description = "主协议编号") String masterAgreementId
    ) {
        return partyService.searchMasterAgreementIdByKey(masterAgreementId);
    }

    @BctMethodInfo(
            description = "载入所有交易对手信息",
            service = "reference-data-service"

    )
    public List<PartyDTO> refGetAllParties() {
        return partyService.listAllParties();
    }

    private String killBlank(String s) {
        return StringUtils.isBlank(s) ? null : s;
    }

}

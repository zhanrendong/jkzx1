package tech.tongyu.bct.reference.api;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.auth.enums.ResourcePermissionTypeEnum;
import tech.tongyu.bct.auth.enums.ResourceTypeEnum;
import tech.tongyu.bct.auth.service.ResourcePermissionService;
import tech.tongyu.bct.client.dto.AccountDTO;
import tech.tongyu.bct.common.api.annotation.BctMethodArg;
import tech.tongyu.bct.common.api.annotation.BctMethodInfo;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.common.util.ProfilingUtils;
import tech.tongyu.bct.reference.dto.MarginDTO;
import tech.tongyu.bct.reference.dto.PartyDTO;
import tech.tongyu.bct.reference.service.MarginService;
import tech.tongyu.bct.reference.service.PartyService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static tech.tongyu.bct.auth.enums.ResourcePermissionTypeEnum.UPDATE_MARGIN;
import static tech.tongyu.bct.auth.enums.ResourceTypeEnum.MARGIN;

/**
 * API
 * @author hangzhi
 */
@Service
public class MarginApi {

    private PartyService partyService;
    private final MarginService marginService;
    private ResourcePermissionService resourcePermissionService;


    @Autowired
    public MarginApi(PartyService partyService,
                     MarginService marginService,
                     ResourcePermissionService resourcePermissionService) {
        this.partyService = partyService;
        this.marginService = marginService;
        this.resourcePermissionService = resourcePermissionService;
    }

    @BctMethodInfo(
            description = "创建保证金事件",
            retDescription = "保证金信息",
            retName = "MarginDTO",
            returnClass = MarginDTO.class
    )
    @Transactional
    public MarginDTO mgnMarginCreate(
            @BctMethodArg(description = "交易对手ID") String partyId,
            @BctMethodArg(required = false, description = "维持保证金") Double maintenanceMargin
    ) {
        if (null == partyId) {
            throw new IllegalArgumentException("partyId cannot be null");
        }

        UUID uuid = UUID.fromString(partyId);

        BigDecimal mm = null;
        if (null != maintenanceMargin) {
            mm = BigDecimal.valueOf(maintenanceMargin);
        }

        return marginService.createMargin(uuid, mm);
    }

    /**
     * 依据帐号ID 获取 保证金信息列表。
     * @param accountIds
     * @return
     */
    @BctMethodInfo(
            description = "依据帐号ID 获取 保证金信息列表",
            retDescription = "保证金信息列表",
            retName = "List<MarginDTO>",
            returnClass = MarginDTO.class
    )
    public List<MarginDTO> mgnMarginList(
            @BctMethodArg(description = "账户编号列表") List<String> accountIds
    ) {
        if (null == accountIds || accountIds.size() < 1) {
            return new ArrayList<>();
        }

        return ProfilingUtils.timed("search margin by accounts", () -> marginService.getMargins(accountIds));
    }

    @BctMethodInfo(
            description = "依据帐号ID 获取 保证金信息列表",
            retDescription = "保证金信息列表",
            retName = "List<MarginDTO>",
            returnClass = MarginDTO.class
    )
    public List<MarginDTO> mgnMarginSearch(
            @BctMethodArg(required = false, description = "交易对手名称") String legalName,
            @BctMethodArg(required = false, description = "主协议编号") String masterAgreementId
    ){
        if (StringUtils.isNotBlank(masterAgreementId)){
            PartyDTO party = partyService.getByMasterAgreementId(masterAgreementId);
            if (StringUtils.isNotBlank(legalName)){
                if (!legalName.equals(party.getLegalName())){
                    throw new CustomException(String.format("主协议编号:[%s],交易对手:[%s],客户信息不匹配",
                            masterAgreementId, legalName));
                }
            }
            legalName = party.getLegalName();
        }
        return marginService.marginSearch(legalName);
    }

    /**
     * 更新指定的维持保证金
     * @param uuid
     * @param maintenanceMargin
     * @return
     */
    @Transactional
    @BctMethodInfo(
            description = "更新维持保证金",
            retDescription = "保证金信息",
            retName = "MarginDTO",
            returnClass = MarginDTO.class,
            service = "reference-data-service"
    )
    public MarginDTO mgnMarginUpdate(
            @BctMethodArg(description = "保证金唯一标识") String uuid,
            @BctMethodArg(description = "维持保证金额度") Number maintenanceMargin
    ) {
        if(!resourcePermissionService.authCan(MARGIN.name(), Lists.newArrayList(MARGIN.getAlias()), UPDATE_MARGIN.toString()).get(0)){
            throw new CustomException(String.format("您没有权限对该%s(%s)进行%s操作，请联系管理员",MARGIN.getAlias(), MARGIN, ResourcePermissionTypeEnum.UPDATE_MARGIN.getAlias()));
        }

        if (StringUtils.isBlank(uuid)) {
            throw  new IllegalArgumentException("invalid uuid");
        }

        UUID id = UUID.fromString(uuid);

        BigDecimal mm = new BigDecimal(maintenanceMargin.toString());

        return marginService.updateMaintenanceMargin(id, mm);
    }

    /**
     * 批量更新维持保证金
     * @param margins
     * @return
     */
    @Transactional
    @BctMethodInfo(
            description = "批量更新维持保证金",
            retDescription = "保证金信息列表",
            retName = "List<MarginDTO>",
            returnClass = MarginDTO.class,
            service = "reference-data-service"
    )
    public List<MarginDTO> mgnMarginsUpdate(
            @BctMethodArg(description = "维持保证金数据", argClass = MarginDTO.class) List<Map<String, Object>> margins
    ) {
        if (null == margins || margins.size() < 1) {
            return new ArrayList<>();
        }

        Map<String, BigDecimal> legalNameMtnMargin = margins.stream()
                .map(m -> JsonUtils.mapper.convertValue(m, MarginDTO.class))
                .filter(m -> !StringUtils.isEmpty(m.getLegalName()) && null != m.getMaintenanceMargin())
                .collect(Collectors.toMap(m ->  m.getLegalName(),
                        m -> m.getMaintenanceMargin()));

        return marginService.updateMaintenanceMargins(legalNameMtnMargin);
    } // end mgnMarginsUpdate
}

package tech.tongyu.bct.trade.api;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.tongyu.bct.acl.common.UserInfo;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValuePartyRoleTypeEnum;
import tech.tongyu.bct.cm.product.iov.ProductTypeEnum;
import tech.tongyu.bct.cm.product.iov.feature.OptionTypeEnum;
import tech.tongyu.bct.common.api.annotation.BctMethodArg;
import tech.tongyu.bct.common.api.annotation.BctMethodInfo;
import tech.tongyu.bct.common.api.response.RpcResponseListPaged;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.trade.dto.trade.QuotePositionDTO;
import tech.tongyu.bct.trade.dto.trade.QuotePrcDTO;
import tech.tongyu.bct.trade.service.QuotePositionService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class QuotePrcApi {

    private UserInfo userInfo;
    private QuotePositionService quotePositionService;

    @Autowired
    public QuotePrcApi(UserInfo userInfo, QuotePositionService quotePositionService) {
        this.userInfo = userInfo;
        this.quotePositionService = quotePositionService;
    }

    @BctMethodInfo(
            description = "删除一个试定价结果集",
            retDescription = "删除是否成功",
            retName = "true or false",
            service = "trade-service"
    )
    public Boolean quotePrcDelete(
            @BctMethodArg(description = "试定价唯一标识") String quotePrcId
    ) {
        if (StringUtils.isBlank(quotePrcId)) {
            throw new CustomException("请输入待删除试定价唯一标识quotePrcId");
        }
        quotePositionService.deleteByQuotePrcId(quotePrcId);
        return true;
    }

    @BctMethodInfo(
            description = "保存一个试定价结果集",
            retDescription = "保存是否成功",
            retName = "true or false",
            service = "trade-service"
    )
    public Boolean quotePrcCreate(
            @BctMethodArg(description = "试定价结果集", argClass = QuotePrcDTO.class) Map<String, Object> quotePrc
    ) {
        QuotePrcDTO quotePrcDto = JsonUtils.mapper.convertValue(quotePrc, QuotePrcDTO.class);
        quotePrcDto.setUserName(userInfo.getUserName());
        quotePositionService.save(quotePrcDto);
        return true;
    }

    @BctMethodInfo(
            description = "删除一条试定价结果",
            retDescription = "删除是否成功",
            retName = "true or false",
            service = "trade-service"
    )
    public Boolean quotePrcPositionDelete(
            @BctMethodArg(description = "定价结构唯一标识") String uuid
    ) {
        if (StringUtils.isBlank(uuid)) {
            throw new CustomException("请输入待删除试定价结构唯一标识uuid");
        }
        quotePositionService.deleteById(UUID.fromString(uuid));
        return true;
    }

    @BctMethodInfo(
            description = "根据条件查询试定价结果",
            retDescription = "符合条件的试定价结果",
            retName = "paged QuotePrcDTOs",
            returnClass = QuotePrcDTO.class,
            service = "trade-service"
    )
    public RpcResponseListPaged<QuotePrcDTO> quotePrcSearchPaged(
            @BctMethodArg(description = "页码") Integer page,
            @BctMethodArg(description = "页距") Integer pageSize,
            @BctMethodArg(required = false, description = "买卖方向", argClass = InstrumentOfValuePartyRoleTypeEnum.class) String direction,
            @BctMethodArg(required = false, description = "看涨/跌", argClass = OptionTypeEnum.class) String optionType,
            @BctMethodArg(required = false, description = "期权类型", argClass = ProductTypeEnum.class) String productType,
            @BctMethodArg(required = false, description = "交易对手") String counterPartyCode,
            @BctMethodArg(required = false, description = "标的物ID") String underlyerInstrumentId,
            @BctMethodArg(required = false, description = "到期开始日") String expirationStartDate,
            @BctMethodArg(required = false, description = "到期结束日") String expirationEndDate,
            @BctMethodArg(required = false, description = "期权类型") String comment
    ) {
        QuotePositionDTO quoteSearchDto = new QuotePositionDTO();
        quoteSearchDto.setUserName(userInfo.getUserName());
        quoteSearchDto.setCounterPartyCode(StringUtils.isBlank(counterPartyCode) ? null : counterPartyCode);
        quoteSearchDto.setOptionType(StringUtils.isBlank(optionType) ? null : OptionTypeEnum.valueOf(optionType));
        quoteSearchDto.setProductType(StringUtils.isBlank(productType) ? null : ProductTypeEnum.valueOf(productType));
        quoteSearchDto.setUnderlyerInstrumentId(StringUtils.isBlank(underlyerInstrumentId) ? null : underlyerInstrumentId);
        quoteSearchDto.setDirection(StringUtils.isBlank(direction) ? null : InstrumentOfValuePartyRoleTypeEnum.valueOf(direction));

        List<QuotePrcDTO> quotePrcList = quotePositionService.search(quoteSearchDto, comment,
                StringUtils.isBlank(expirationStartDate) ? null : LocalDate.parse(expirationStartDate),
                StringUtils.isBlank(expirationEndDate) ? null : LocalDate.parse(expirationEndDate));

        int start = page * pageSize;
        int end = Math.min(start + pageSize, quotePrcList.size());
        return new RpcResponseListPaged<>(quotePrcList.subList(start, end), quotePrcList.size());

    }
}

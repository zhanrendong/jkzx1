package tech.tongyu.bct.pricing.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.tongyu.bct.common.api.annotation.BctMethodArg;
import tech.tongyu.bct.common.api.annotation.BctMethodInfo;
import tech.tongyu.bct.common.util.DateTimeUtils;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.pricing.dto.BaseContractDTO;
import tech.tongyu.bct.pricing.service.BaseContractService;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class BaseContractApi {
    private BaseContractService baseContractService;

    @Autowired
    public BaseContractApi(BaseContractService baseContractService) {
        this.baseContractService = baseContractService;
    }

/*    @BctMethodInfo(description = "Save a base contract", retName = "baseContractId", retDescription = "Base contract")
    public BaseContractDTO prcBaseContractSave(
            @BctMethodArg(description = "Trade ID", required= true) String positionId,
            @BctMethodArg(description = "Underlyer instrument ID", required = false) String baseContractId,
            @BctMethodArg(required = false) String hedgingContractId,
            @BctMethodArg(description = "Base contract start date", required = false) String baseContractValidStart
    ) {
        String hedging = Objects.isNull(hedgingContractId) ? baseContractId : hedgingContractId;
        ZonedDateTime t = DateTimeUtils.parse(baseContractValidStart, null);
        return baseContractService.save(positionId, baseContractId, hedging, t.toLocalDate());
    }*/

    @BctMethodInfo
    public List<BaseContractDTO> prcBaseContractsCreateOrUpdate(
            @BctMethodArg List<Map<String, Object>> baseContracts,
            @BctMethodArg(required = false) String valuationDate
    ) {
        List<BaseContractDTO> dtos = baseContracts.stream()
                .map(m -> JsonUtils.mapper.convertValue(m, BaseContractDTO.class))
                .collect(Collectors.toList());
        ZonedDateTime t = DateTimeUtils.parse(valuationDate, null);
        return baseContractService.createOrUpdate(dtos, t.toLocalDate());
    }

    @BctMethodInfo
    public List<BaseContractDTO> prcBaseContractsList(
            @BctMethodArg(required = false) String valuationDate) {
        ZonedDateTime t = DateTimeUtils.parse(valuationDate, null);
        return baseContractService.list(t.toLocalDate());
    }

    @BctMethodInfo
    public List<BaseContractDTO> prcBaseConstractDelete(
            @BctMethodArg String positionId
    ) {
        return baseContractService.deleteByPositionId(positionId);
    }
}

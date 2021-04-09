package tech.tongyu.bct.market.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.tongyu.bct.common.api.annotation.BctApiTagEnum;
import tech.tongyu.bct.common.api.annotation.BctExcelTypeEnum;
import tech.tongyu.bct.common.api.annotation.BctMethodArg;
import tech.tongyu.bct.common.api.annotation.BctMethodInfo;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.market.dto.CorrelationDTO;
import tech.tongyu.bct.market.service.CorrelationService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CorrelationApi {
    private CorrelationService correlationService;

    @Autowired
    public CorrelationApi(CorrelationService correlationService) {
        this.correlationService = correlationService;
    }

    @BctMethodInfo(
            description = "批量保存标的物相关系数",
            retDescription = "保存的标的物相关系数",
            retName = "List of CorrelationDTO",
            returnClass = CorrelationDTO.class,
            service = "market-data-service"
    )
    public List<CorrelationDTO> mktCorrelationsSave(
            @BctMethodArg(description = "标的物相关系数", argClass = CorrelationDTO.class) List<Map<String, Object>> correlations) {
        return correlations.stream()
                .map(c -> JsonUtils.mapper.convertValue(c, CorrelationDTO.class))
                .map(dto -> correlationService.save(dto.getInstrumentId1(),
                        dto.getInstrumentId2(), dto.getCorrelation()))
                .collect(Collectors.toList());
    }

    @BctMethodInfo(
            excelType = BctExcelTypeEnum.Matrix,
            tags = {BctApiTagEnum.Excel},
            description = "获取标的物相关系数",
            retDescription = "标的物相关系数",
            service = "market-data-service"
    )
    public List<List<Double>> mktCorrelationMatrixGet(
            @BctMethodArg(description = "标的物ID列表", excelType = BctExcelTypeEnum.ArrayString) List<String> instrumentIds) {
        return correlationService.correlationMatrix(instrumentIds.stream().distinct().collect(Collectors.toList()));
    }

    @BctMethodInfo(
            description = "删除标的物相关系数",
            retDescription = "删除的标的物相关系数",
            retName = "List of CorrelationDTO",
            returnClass = CorrelationDTO.class,
            service = "market-data-service"
    )
    public List<CorrelationDTO> mktCorrelationDelete(@BctMethodArg(description = "标的物ID1") String instrumentId1,
                                                     @BctMethodArg(description = "标的物ID2") String instrumentId2) {
        return correlationService.delete(instrumentId1, instrumentId2);
    }
}

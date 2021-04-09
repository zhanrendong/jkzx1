package tech.tongyu.bct.document.api;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.tongyu.bct.auth.dto.ResourceDTO;
import tech.tongyu.bct.auth.service.ResourceService;
import tech.tongyu.bct.common.api.annotation.BctMethodArg;
import tech.tongyu.bct.common.api.annotation.BctMethodInfo;
import tech.tongyu.bct.common.api.response.RpcResponseListPaged;
import tech.tongyu.bct.document.dto.DocProcessStatusEnum;
import tech.tongyu.bct.document.dto.PositionDocumentDTO;
import tech.tongyu.bct.document.dto.TradeDocumentDTO;
import tech.tongyu.bct.document.service.TradeDocumentService;
import tech.tongyu.bct.reference.service.PartyService;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TradeDocumentApi {

    TradeDocumentService tradeDocService;

    ResourceService resourceService;

    PartyService partyService;

    @Autowired
    public TradeDocumentApi(TradeDocumentService tradeDocService, ResourceService resourceService, PartyService partyService) {
        this.tradeDocService = tradeDocService;
        this.resourceService = resourceService;
        this.partyService = partyService;
    }

    @BctMethodInfo(
            description = "交易文档状态更新",
            retDescription = "交易文档信息",
            returnClass = TradeDocumentDTO.class,
            retName = "TradeDocumentDTO",
            service = "document-service"
    )
    public TradeDocumentDTO tradeDocStatusUpdate(@BctMethodArg(description = "交易编号") String tradeId,
                                                 @BctMethodArg(description = "文档状态") String docProcessStatus){
        if (StringUtils.isBlank(tradeId)){
            throw new IllegalArgumentException("请输入交易编号tradeId");
        }
        if (StringUtils.isBlank(docProcessStatus)){
            throw new IllegalArgumentException("请输入确认书处理状态docProcessStatus");
        }
        return tradeDocService.updateTradeDocStatus(tradeId, DocProcessStatusEnum.valueOf(docProcessStatus));
    }

    @BctMethodInfo(
            description = "交易文档查询",
            retDescription = "交易文档信息",
            returnClass = TradeDocumentDTO.class,
            retName = "TradeDocumentDTO",
            service = "document-service"
    )
    public RpcResponseListPaged<TradeDocumentDTO> tradeDocSearch(@BctMethodArg(description = "交易编号", required = false) String tradeId,
                                                                 @BctMethodArg(description = "交易簿名称", required = false) String bookName,
                                                                 @BctMethodArg(description = "交易对手名称", required = false) String partyName,
                                                                 @BctMethodArg(description = "通知书处理状态", required = false) String docProcessStatus,
                                                                 @BctMethodArg(description = "开始日期", required = false) String startDate,
                                                                 @BctMethodArg(description = "结束日期", required = false) String endDate,
                                                                 @BctMethodArg(description = "页码") Integer page,
                                                                 @BctMethodArg(description = "页面大小") Integer pageSize){
        TradeDocumentDTO tradeDocDto = new TradeDocumentDTO();
        tradeDocDto.setTradeId(StringUtils.isBlank(tradeId) ? null : tradeId);
        tradeDocDto.setBookName(StringUtils.isBlank(bookName) ? null : bookName);
        tradeDocDto.setPartyName(StringUtils.isBlank(partyName) ? null : partyName);
        tradeDocDto.setDocProcessStatus(StringUtils.isBlank(docProcessStatus) ? null : DocProcessStatusEnum.valueOf(docProcessStatus));

        Set<String> bookNames = resourceService.authBookGetCanRead().stream().map(ResourceDTO::getResourceName).collect(Collectors.toSet());

        List<TradeDocumentDTO> tradeDocs = tradeDocService.tradeDocSearch(tradeDocDto,
                LocalDate.parse(startDate), LocalDate.parse(endDate))
                .stream()
                .map(r -> {
                    r.setTradeEmail(partyService.getByLegalName(r.getPartyName()).getTradeEmail());
                    return r;
                })
                .filter(r -> bookNames.contains(r.getBookName()))
                .collect(Collectors.toList());
        int start = page * pageSize;
        int end = Math.min(start + pageSize, tradeDocs.size());
        return new RpcResponseListPaged<>(tradeDocs.subList(start, end), tradeDocs.size());
    }

    @BctMethodInfo(
            description = "持仓文档状态更新",
            retDescription = "持仓文档信息",
            returnClass = PositionDocumentDTO.class,
            retName = "PositionDocumentDTO",
            service = "document-service"
    )
    public PositionDocumentDTO positionDocStatusUpdate(@BctMethodArg(description = "持仓编号") String positionId,
                                                       @BctMethodArg(description = "通知书处理状态") String docProcessStatus){
        if (StringUtils.isBlank(positionId)){
            throw new IllegalArgumentException("请输入持仓编号positionId");
        }
        if (StringUtils.isBlank(docProcessStatus)){
            throw new IllegalArgumentException("请输入通知书处理状态docProcessStatus");
        }
        return tradeDocService.updatePositionDocStatus(positionId, DocProcessStatusEnum.valueOf(docProcessStatus));
    }

    @BctMethodInfo(
            description = "根据交易编号查询持仓通知书ID",
            retDescription = "持仓通知书ID列表",
            retName = "List<String>",
            service = "document-service"
    )
    public List<String> positionDocPositionIdListByTradeId(@BctMethodArg(description = "交易编号") String tradeId){
        if (StringUtils.isBlank(tradeId)){
            throw new IllegalArgumentException("请输入交易编号tradeId");
        }
        return tradeDocService.findPositionIdByTradeId(tradeId);
    }

    @BctMethodInfo(
            description = "持仓文档查询",
            retDescription = "持仓文档信息",
            returnClass = PositionDocumentDTO.class,
            retName = "PositionDocumentDTO",
            service = "document-service"
    )
    public RpcResponseListPaged<PositionDocumentDTO> positionDocSearch(@BctMethodArg(description = "持仓编号", required = false) String positionId,
                                                                       @BctMethodArg(description = "交易编号", required = false) String tradeId,
                                                                       @BctMethodArg(description = "交易簿名称", required = false) String bookName,
                                                                       @BctMethodArg(description = "交易对手名称", required = false) String partyName,
                                                                       @BctMethodArg(description = "通知书处理状态", required = false) String docProcessStatus,
                                                                       @BctMethodArg(description = "开始日期", required = false) String startDate,
                                                                       @BctMethodArg(description = "结束日期", required = false) String endDate,
                                                                       @BctMethodArg(description = "页码") Integer page,
                                                                       @BctMethodArg(description = "页面大小") Integer pageSize){
        PositionDocumentDTO positionDocDto = new PositionDocumentDTO();
        positionDocDto.setTradeId(StringUtils.isBlank(tradeId) ? null : tradeId);
        positionDocDto.setBookName(StringUtils.isBlank(bookName) ? null : bookName);
        positionDocDto.setPartyName(StringUtils.isBlank(partyName) ? null : partyName);
        positionDocDto.setPositionId(StringUtils.isBlank(positionId) ? null : positionId);
        positionDocDto.setDocProcessStatus(StringUtils.isBlank(docProcessStatus) ? null : DocProcessStatusEnum.valueOf(docProcessStatus));

        Set<String> bookNames = resourceService.authBookGetCanRead().stream().map(ResourceDTO::getResourceName).collect(Collectors.toSet());

        List<PositionDocumentDTO> positionDocs = tradeDocService.positionDocSearch(positionDocDto,
                LocalDate.parse(startDate), LocalDate.parse(endDate))
                .stream()
                .map(r -> {
                    r.setTradeEmail(partyService.getByLegalName(r.getPartyName()).getTradeEmail());
                    return r;
                })
                .filter(r -> bookNames.contains(r.getBookName()))
                .collect(Collectors.toList());
        int start = page * pageSize;
        int end = Math.min(start + pageSize, positionDocs.size());
        return new RpcResponseListPaged<>(positionDocs.subList(start, end), positionDocs.size());
    }

}

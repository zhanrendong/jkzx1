package tech.tongyu.bct.document.service;

import tech.tongyu.bct.document.dto.DocProcessStatusEnum;
import tech.tongyu.bct.document.dto.PositionDocumentDTO;
import tech.tongyu.bct.document.dto.TradeDocumentDTO;

import java.time.LocalDate;
import java.util.List;

public interface TradeDocumentService {

    String tradeOpenTopic = "tradeOpen:queue";
    String tradeSettleTopic = "tradeSettle:queue";

    TradeDocumentDTO createTradeDoc(TradeDocumentDTO tradeDocumentDto);

    TradeDocumentDTO updateTradeDocStatus(String tradeId, DocProcessStatusEnum docProcessStatus);

    List<TradeDocumentDTO> tradeDocSearch(TradeDocumentDTO tradeDocDto, LocalDate startDate, LocalDate endDate);


    List<String> findPositionIdByTradeId(String tradeId);

    PositionDocumentDTO createPositionDoc(PositionDocumentDTO positionDocumentDto);

    PositionDocumentDTO updatePositionDocStatus(String positionId, DocProcessStatusEnum docProcessStatus);

    List<PositionDocumentDTO> positionDocSearch(PositionDocumentDTO positionDocDto, LocalDate startDate, LocalDate endDate);

}

package tech.tongyu.bct.exchange.service;

import tech.tongyu.bct.exchange.dto.PositionPortfolioRecordDTO;
import io.vavr.Tuple4;
import tech.tongyu.bct.exchange.dto.PositionRecordDTO;
import tech.tongyu.bct.exchange.dto.PositionSnapshotDTO;
import tech.tongyu.bct.exchange.dto.TradeRecordDTO;
import tech.tongyu.bct.market.dto.InstrumentDTO;
import tech.tongyu.bct.pricing.common.Diagnostic;
import tech.tongyu.bct.quant.library.priceable.Position;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ExchangeService {

    String SCHEMA = "exchangeService";

    List<TradeRecordDTO> findAllTradeRecord();

    TradeRecordDTO saveTradeRecordWithoutNewTransaction(TradeRecordDTO tradeRecordDto);

    TradeRecordDTO saveTradeRecordWithNewTransaction(TradeRecordDTO tradeRecordDto);

    List<TradeRecordDTO> searchTradeRecord(List<String> instrumentId, LocalDateTime startTime, LocalDateTime endTime);


    List<PositionRecordDTO> searchPositionRecord(LocalDate searchDate);

    Tuple4<List<Diagnostic>, List<Position>, List<InstrumentDTO>, List<PositionPortfolioRecordDTO>> searchPositionRecord(List<String> portfolioNames, List<String> books, LocalDate searchDate);

    Tuple4<List<Diagnostic>, List<Position>, List<InstrumentDTO>, List<PositionPortfolioRecordDTO>> searchPositionRecord(List<PositionPortfolioRecordDTO> records);

    List<PositionRecordDTO> searchPositionRecordGroupByInstrumentId(LocalDate searchDate, Set<String> bookNames);

    List<PositionPortfolioRecordDTO> searchGroupedPositionRecord(List<String> portfolioNames,
                                                                 List<String> bookNames,
                                                                 LocalDate searchDate);

    List<PositionPortfolioRecordDTO> searchGroupedPositionRecord(List<String> portfolioNames,
                                                                 List<String> bookNames,
                                                                 String searchDate,
                                                                 List<String> readableBooks);

    List<PositionSnapshotDTO> findAllPositionSnapshot();

    List<PositionSnapshotDTO> findPositionSnapshotGroupByInstrumentId();

    void savePositionSnapshotByTradeRecords(List<TradeRecordDTO> tradeRecordDtoList);

    Optional<TradeRecordDTO> findTradeRecordByTradeId(String tradeId);

    PositionRecordDTO savePosition(PositionRecordDTO positionRecordDTO);

    List<String> fuzzyQueryInstrumentsInTradeRecords(String criteria);
}

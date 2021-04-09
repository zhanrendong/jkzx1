package tech.tongyu.bct.trade.service;

import tech.tongyu.bct.cm.product.iov.ProductTypeEnum;
import tech.tongyu.bct.cm.trade.LCMEventTypeEnum;
import tech.tongyu.bct.cm.trade.TradeStatusEnum;
import tech.tongyu.bct.cm.trade.impl.BctTrade;
import tech.tongyu.bct.trade.dto.trade.TradeDTO;
import tech.tongyu.bct.trade.dto.trade.TradePositionIndexDTO;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public interface TradeService {
    String SCHEMA = "tradeService";
    String SNAPSHOT_SCHEMA = "tradeSnapshotModel";

    void deleteAll();

    List<BctTrade> findAll();

    List<TradeDTO> findByTradeStatus(TradeStatusEnum tradeStatus);

    void generateHistoryTradeIndex();

    List<BctTrade> listByTradeIds(List<String> tradeIds);

    List<BctTrade> listBySimilarTradeId(String similarTradeId);

    List<String> listTradeIdByCounterPartyName(String counterPartyName);

    List<TradeDTO> search(Map<String, String> searchDetail);

    void create(TradeDTO tradeDto, OffsetDateTime validTime);

    BctTrade fromDTO(TradeDTO tradeDTO, OffsetDateTime validTime);

    void updateTradeStatus(String tradeId, TradeStatusEnum tradeStatus);

    List<TradeDTO> findByTradeIds(List<String> tradeIds);

    List<TradePositionIndexDTO> searchTradeIndexByIndex(TradePositionIndexDTO indexDto, String status);

    List<TradeDTO> searchByProductTypesAndNotInLcmEvents(List<ProductTypeEnum> productTypes,
                                                         List<LCMEventTypeEnum> lcmEventTypes);

    void deleteByTradeId(String tradeId, OffsetDateTime validTime, OffsetDateTime transactionTime);

    TradeDTO getByTradeId(String tradeId, OffsetDateTime validTime, OffsetDateTime transactionTime);

    BctTrade getBctTradeByTradeId(String tradeId, OffsetDateTime validTime, OffsetDateTime transactionTime);

    List<String> listByBookName(String bookName, OffsetDateTime validTime, OffsetDateTime transactionTime);

    List<TradeDTO> getTradeByTradeId(String tradeId, OffsetDateTime validTime, OffsetDateTime transactionTime);

    List<TradeDTO> getTradeByBook(String bookName, OffsetDateTime validTime);

    List<TradeDTO> getTradeByCounterparty(String counterpartyId, OffsetDateTime validTime);

    List<TradeDTO> getTradeBySales(String salesId, OffsetDateTime validTime);

    List<TradeDTO> getTradeByTradeDate(LocalDate tradeDate, OffsetDateTime validTime);

    List<TradeDTO> getExpiringTrades(String trader, OffsetDateTime validTime);

    List<String> listInstrumentsByBookName(String bookName);

    List<TradeDTO> getByTradeIds(List<String> tradeIds);
}

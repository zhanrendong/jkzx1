package tech.tongyu.bct.exchange.dao.repo.intel.local;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tech.tongyu.bct.exchange.dao.dbo.local.TradeRecord;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TradeRecordRepo extends JpaRepository<TradeRecord, UUID> {

    List<TradeRecord> findAllByDealTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    List<TradeRecord> findAllByInstrumentIdInAndDealTimeBetween(List<String> instrumentId,
                                                                LocalDateTime startTime, LocalDateTime endTime);

    List<TradeRecord> findAllByInstrumentIdAndDealTimeBetween(String instrumentId,
                                                              LocalDateTime startTime, LocalDateTime endTime);

    Optional<TradeRecord> findByTradeId(String tradeId);

    @Query("select distinct r.instrumentId from TradeRecord r")
    List<String> findAllInstrumentId();
}

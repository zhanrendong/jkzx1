package tech.tongyu.bct.exchange.dao.repo.intel.local;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.tongyu.bct.exchange.dao.dbo.local.PositionRecord;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface PositionRecordRepo extends JpaRepository<PositionRecord, UUID> {

    Optional<PositionRecord> findByBookIdAndInstrumentIdAndDealDate(String bookId, String instrumentId, LocalDate dealDate);
    
}

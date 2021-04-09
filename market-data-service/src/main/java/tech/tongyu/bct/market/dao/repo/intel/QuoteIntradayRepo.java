package tech.tongyu.bct.market.dao.repo.intel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.market.dao.dbo.QuoteIntraday;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuoteIntradayRepo extends JpaRepository<QuoteIntraday, UUID> {
    Optional<QuoteIntraday> findByInstrumentId(String instrumentId);
    List<QuoteIntraday> findAllByOrderByInstrumentIdAsc();
    List<QuoteIntraday> findAllByInstrumentIdInOrderByInstrumentIdAsc(List<String> instrumentIds);
    @Modifying
    @Transactional
    List<QuoteIntraday> deleteByInstrumentId(String instrumentId);
}

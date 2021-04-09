package tech.tongyu.bct.market.dao.repo.intel;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tech.tongyu.bct.market.dao.dbo.QuoteClose;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Repository
public interface QuoteCloseRepo extends JpaRepository<QuoteClose, UUID> {
    @Query("select q from QuoteClose q " +
            "where q.instrumentId=:instrumentId " +
            "and q.quoteTimezone=:quoteTimezone " +
            "and q.valuationDate<=:valuationDate " +
            "order by q.valuationDate desc")
    List<QuoteClose> findQuotes(String instrumentId, LocalDate valuationDate,
                                ZoneId quoteTimezone, Pageable pageable);
    @Query("select q from QuoteClose  q " +
            "where q.instrumentId in :instrumentIds " +
            "and q.quoteTimezone=:quoteTimezone " +
            "and q.valuationDate<=:valuationDate " +
            "order by q.instrumentId asc, q.valuationDate desc")
    List<QuoteClose> findQuotes(List<String> instrumentIds, LocalDate valuationDate,
                                ZoneId quoteTimezone, Pageable pageable);

    @Query(value = "SELECT x.uuid,x.instrument_id,x.valuation_date,x.close,x.settle,x.quote_timestamp,x.created_at," +
            "x.updated_at,x.high,x.open,x.low,x.quote_timezone " +
            "FROM market_data_service.quote_close x " +
            "JOIN (SELECT p.instrument_id,MAX(valuation_date) AS valuation_date " +
            "FROM market_data_service.quote_close p " +
            "WHERE instrument_id =:instrumentId AND valuation_date <= :valuationDate " +
            "GROUP BY p.instrument_id) y ON y.instrument_id = x.instrument_id " +
            "AND y.valuation_date = x.valuation_date",
            nativeQuery = true)
    List<QuoteClose> findQuotes(String instrumentId, LocalDate valuationDate);

}

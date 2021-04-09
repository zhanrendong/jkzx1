package tech.tongyu.bct.reference.dao.repl.intel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.reference.dao.dbo.TradingHoliday;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface TradingHolidayRepo extends JpaRepository<TradingHoliday, UUID> {
    List<TradingHoliday> findAllByCalendarId(String calendarId);
    @Modifying
    @Transactional
    List<TradingHoliday> deleteByCalendarIdEqualsAndHolidayIn(String calendarId, List<LocalDate> holidays);
}

package tech.tongyu.bct.reference.dao.repl.intel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.tongyu.bct.reference.dao.dbo.TradingCalendar;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TradingCalendarRepo extends JpaRepository<TradingCalendar, UUID> {
    Optional<TradingCalendar> findByCalendarId(String calendarId);
    List<TradingCalendar> findAllByOrderByCalendarId();
}

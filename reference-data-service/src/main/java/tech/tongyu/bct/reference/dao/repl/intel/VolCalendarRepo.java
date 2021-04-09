package tech.tongyu.bct.reference.dao.repl.intel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.tongyu.bct.reference.dao.dbo.VolCalendar;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VolCalendarRepo extends JpaRepository<VolCalendar, UUID> {
    Optional<VolCalendar> findByCalendarId(String calendarId);
    List<VolCalendar> findAllByOrderByCalendarId();
}

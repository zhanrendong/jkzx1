package tech.tongyu.bct.reference.dao.repl.intel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.reference.dao.dbo.VolSpecialDate;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface VolSpecialDateRepo extends JpaRepository<VolSpecialDate, UUID> {
    List<VolSpecialDate> findAllByCalendarId(String calendarId);
    @Modifying
    @Transactional
    List<VolSpecialDate> deleteByCalendarIdEqualsAndSpecialDateIn(String calendarId, List<LocalDate> specialDates);
}

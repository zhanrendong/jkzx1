package tech.tongyu.bct.quant.library.financial.date;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.exception.ErrorCode;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public enum Holidays {
    Instance;
    private static Cache<String, Integer> centers = CacheBuilder.newBuilder().build();
    private static Cache<Integer, HashSet<LocalDate>> holidays = CacheBuilder.newBuilder().build();
    private static int lastCenter = -1;

    private static final Logger logger = LoggerFactory.getLogger(Holidays.class);

    /**
     * Check if a date is a holiday given a list of calendars
     * @param calendars The list of calendars to check against
     * @param day The date to be checked
     * @param includeWknds Whether to include weekends (Saturday and Sunday) in the holiday calendars.
     * @return true if the input date is a holiday. false if not.
     */
    public boolean isHoliday(LocalDate day, List<String> calendars, boolean includeWknds) {
        if(includeWknds) {
            if(day.getDayOfWeek()== DayOfWeek.SATURDAY || day.getDayOfWeek()==DayOfWeek.SUNDAY) {
                return true;
            }
        }
        for(String s : calendars) {
            if(s.equals("NONE")) {
                continue;
            }
            if(s.equals("WEEKENDS")) {
                if(day.getDayOfWeek()==DayOfWeek.SATURDAY || day.getDayOfWeek()==DayOfWeek.SUNDAY) {
                    return true;
                }
            }
            Integer id = centers.getIfPresent(s);
            if(id != null)
                if(holidays.getIfPresent(id).contains(day))
                    return true;
        }
        return false;
    }

    /**
     * Check if a date is a holiday or Saturday or Sunday given a list of calendars
     * @param cities The list of calendars to check against
     * @param day The date to be checked
     * @return true if the input date is a holiday. false if not.
     */
    public boolean isHoliday(LocalDate day, List<String> cities) {
        return isHoliday(day, cities, true);
    }

    /**
     * Add holidays of a city
     * @param city The name of calendar to be added
     * @param holidayList A list of holidays of the city
     */
    public void add(String city, List<LocalDate> holidayList) {
        logger.info("Adding holiday calender {}", city);
        logger.debug("Holiday list: {}", holidayList);
        if(centers.getIfPresent(city) != null)
            throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                    "交易日历 " + city + " 已存在。请删除后再添加。");
        HashSet<LocalDate> list = new HashSet<>(holidayList);
        lastCenter++;
        centers.put(city, lastCenter);
        holidays.put(lastCenter, list);
    }

    /**
     * If the calendar does not exist, create one. Otherwise replace the existing one with provided holidays.
     * WARNING: There may be multiple alias pointing to the same holidays. Thus this function will make all aliases
     * point to the new holidays.
     * @param city Calendar name
     * @param holidayList New set of holidays
     */
    public void replace(String city, List<LocalDate> holidayList) {
        if (centers.getIfPresent(city) == null) {
            add(city, holidayList);
        } else {
            Integer id = centers.getIfPresent(city);
            holidays.put(id, new HashSet<>(holidayList));
        }
    }

    /**
     * Delete a holiday calendar given a name.
     * Note: since we allow aliases, the actual calendar is deleted only if all aliases have been
     * @param city The name/alias of the calendar to delete
     */
    public boolean delete(String city) {
        logger.info("Trying to delete calendar {}", city);
        Integer id = centers.getIfPresent(city);
        if (id == null)
            return false;
        // remove the calendar
        centers.invalidate(city);
        // because we allow aliases we need to find all cities that point to the same id
        // we can break the loop once an alias is found. for now just keep it simple and slow.
        List<String> aliases = new ArrayList<>();
        for (String c : centers.asMap().keySet()) {
            Integer h = centers.getIfPresent(c);
            if (h.equals(id))
                aliases.add(c);
        }
        // delete the actual calendar only if all aliases have been deleted
        if (aliases.size() == 0)
            holidays.invalidate(id);
        return true;
    }

    /**
     * Delete the holidays associated with the given calendar.
     * WARNING: this is a hard delete, meaning the data will be deleted and
     * all alias pointing to this calendar will be invalidated immediately
     * @param city Calendar
     */
    public void deleteAll(String city) {
        Integer id = centers.getIfPresent(city);
        if (Objects.isNull(id)) {
            centers.invalidate(city);
            return;
        }
        List<String> aliases = centers.asMap().keySet().stream()
                .filter(k -> Objects.equals(centers.getIfPresent(k), id))
                .collect(Collectors.toList());
        centers.invalidateAll(aliases);
        holidays.invalidate(id);
    }

    /**
     * Merge given holidays to an existing calendar
     * Note: all aliases will point to the same set of holidays
     * @param city The calendar to merge to
     * @param holidayList The holidays to merge
     */
    public void mergeHolidays(String city, List<LocalDate> holidayList) {
        Integer id = centers.getIfPresent(city);
        if (Objects.isNull(id)) {
            return;
        }
        HashSet<LocalDate> existing = holidays.getIfPresent(id);
        if (Objects.isNull(existing)) {
            holidays.invalidate(id);
            return;
        }
        if (existing.addAll(holidayList)) {
            holidays.put(id, existing);
        }
    }

    /**
     * Remove given holidays from a calendar
     * Note: all aliases will point to the same set of holidays
     * @param city Calendar
     * @param holidayList Holidays to be removed
     */
    public void removeHolidays(String city, List<LocalDate> holidayList) {
        Integer id = centers.getIfPresent(city);
        if (Objects.isNull(id)) {
            return;
        }
        HashSet<LocalDate> existing = holidays.getIfPresent(id);
        if (Objects.isNull(existing)) {
            holidays.invalidate(id);
            return;
        }
        if (existing.removeAll(holidayList)) {
            holidays.put(id, existing);
        }
    }

    /**
     * Add an alias
     * @param alias The alias to a calendar. For example USD as an alias to NYC
     * @param center The holiday calendar the alias points to
     */
    public void add(String alias, String center) {
        Integer id = centers.getIfPresent(center);
        if(id == null) {
            logger.info("Center {} does not exist. Alias {} not added", center, alias);
            return;
        }
        centers.put(alias, id);
        return;
    }

    /**
     * List holiday calendars
     * @return A list of holiday calendars
     */
    public String[] listCalendars() {
        ArrayList<String> calendars = new ArrayList<>();
        calendars.add("WEEKENDS");
        calendars.addAll(centers.asMap().keySet());
        return calendars.toArray(new String[calendars.size()]);
    }

    /**
     * List holidays given a list of holidays
     * @param cities The calendars
     * @return A list of holidays common to all given calendars
     */
    public List<LocalDate> listHolidays(List<String> cities) {
        Set<LocalDate> hs = new HashSet<>();
        for (String c : cities) {
            Integer id = centers.getIfPresent(c);
            if (id != null) {
                HashSet<LocalDate> h = holidays.getIfPresent(id);
                if (h != null)
                    hs.addAll(h);
            }
        }
        return hs.stream().sorted().collect(Collectors.toList());
    }
}

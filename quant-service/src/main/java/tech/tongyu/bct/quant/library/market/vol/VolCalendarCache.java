package tech.tongyu.bct.quant.library.market.vol;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.exception.ErrorCode;
import tech.tongyu.bct.quant.library.financial.date.Holidays;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class VolCalendarCache {
    private static Logger logger = LoggerFactory.getLogger(VolCalendarCache.class);

    private static Cache<String, VolCalendar> cache = CacheBuilder.newBuilder().build();

    public static final String NONE = "NONE";
    public static final String WEEKENDS = "WEEKENDS";

    static {
        cache.put(NONE, VolCalendar.none());
        cache.put(WEEKENDS, VolCalendar.weekendsOnly(0.0));
    }

    /**
     * Create a vol calendar from weekend weight and special dates
     * @param name Name of the vol calendar
     * @param weekendWeight Weekend weight
     * @param specialWeights A map of special dates and their weights
     */
    public static void create(String name, double weekendWeight, Map<LocalDate, Double> specialWeights) {
        logger.info("Creating/Replacing vol calendar " + name);
        cache.put(name, new VolCalendar(weekendWeight, specialWeights));
    }

    /**
     * Create a vol calendar from trading calendars with weights on weekends/holidays set to 0
     * @param name Name of hte vol valendar
     * @param calendars Trading calendars
     */
    public static void create(String name, List<String> calendars) {
        cache.put(name, VolCalendar.fromCalendars(calendars));
    }

    public static VolCalendar get(String name) {
        return cache.getIfPresent(name);
    }

    public static VolCalendar getMayThrow(String name) {
        VolCalendar volCalendar = cache.getIfPresent(name);
        if (Objects.isNull(volCalendar)) {
            throw new CustomException(ErrorCode.MISSING_ENTITY, String.format("quantlib: Vol Calendar %s 不存在", name));
        }
        return volCalendar;
    }

    public static void put(String name, VolCalendar volCalendar) {
        cache.put(name, volCalendar);
    }

    public static void delete(String name) {
        cache.invalidate(name);
    }
}

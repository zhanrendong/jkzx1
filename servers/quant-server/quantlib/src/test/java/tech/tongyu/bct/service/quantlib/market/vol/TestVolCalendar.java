package tech.tongyu.bct.service.quantlib.market.vol;

import org.junit.Assert;
import org.junit.Test;
import tech.tongyu.bct.service.quantlib.common.utils.JsonMapper;
import tech.tongyu.bct.service.quantlib.market.vol.utils.VolCalendar;

import java.time.LocalDateTime;

public class TestVolCalendar {
    @Test
    public void testEffectiveNumDaysWeekendOnly() {
        VolCalendar vc = VolCalendar.weekendsOnly(0.);
        double effectiveNumDays = vc.getEffectiveNumDays(LocalDateTime.of(2017, 10 ,20, 0, 0),
                LocalDateTime.of(2017,10,30,0,0,0));
        Assert.assertEquals(6.0, effectiveNumDays, 1e-15);
    }
    @Test
    public void testSerialization() throws Exception {
        VolCalendar vc = VolCalendar.weekendsOnly(1.0);
        String json = JsonMapper.toJson(vc);
        System.out.println(json);
        VolCalendar vcCopy = (VolCalendar)JsonMapper.fromJson("tech.tongyu.bct.service.quantlib.market.vol.utils.VolCalendar",
                json);
        double effectiveNumDays = vc.getEffectiveNumDays(LocalDateTime.of(2017, 10 ,20, 0, 0),
                LocalDateTime.of(2017,10,30,0,0,0));
        double effectiveNumDaysCopy = vcCopy.getEffectiveNumDays(LocalDateTime.of(2017, 10 ,20, 0, 0),
                LocalDateTime.of(2017,10,30,0,0,0));
        Assert.assertEquals(effectiveNumDays, effectiveNumDaysCopy, 1e-15);
    }
}

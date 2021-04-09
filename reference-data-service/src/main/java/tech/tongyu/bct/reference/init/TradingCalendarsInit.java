package tech.tongyu.bct.reference.init;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import tech.tongyu.bct.reference.dao.dbo.TradingCalendar;
import tech.tongyu.bct.reference.service.TradingCalendarService;
import tech.tongyu.bct.reference.service.VolCalendarService;

@Component
public class TradingCalendarsInit {
    private static Logger logger = LoggerFactory.getLogger(TradingCalendar.class);
    private TradingCalendarService tradingCalendarService;
    private VolCalendarService volCalendarService;

    @Autowired
    public TradingCalendarsInit(TradingCalendarService tradingCalendarService,
                                VolCalendarService volCalendarService) {
        this.tradingCalendarService = tradingCalendarService;
        this.volCalendarService = volCalendarService;
    }

    @EventListener
    public void onApplicationReady(ApplicationReadyEvent event) {
        logger.info("Initializing trading calendars");
        tradingCalendarService.loadIntoQuantlib();
        logger.info("Intializing vol calendars");
        volCalendarService.loadIntoQuantlib();
    }
}

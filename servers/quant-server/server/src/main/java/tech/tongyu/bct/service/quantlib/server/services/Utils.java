package tech.tongyu.bct.service.quantlib.server.services;

import org.hashids.Hashids;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicLong;

public class Utils {
    private static double nanosInYear = 365.25*24*3600*1000000000;
    private static Hashids hash = new Hashids("quantlib hashid salt");
    private static AtomicLong idCounter = new AtomicLong();

    public static double time(LocalDateTime t1, LocalDateTime t2) {
        return ChronoUnit.NANOS.between(t1,t2)/nanosInYear;
    }

    public static String genID(Object o) {
        long n = idCounter.getAndIncrement()+System.currentTimeMillis();
        return o.getClass().getSimpleName() + "~"+ hash.encode(n);
    }
}
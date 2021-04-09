package tech.tongyu.bct.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

public class ProfilingUtils {
    private static Logger logger = LoggerFactory.getLogger(ProfilingUtils.class);

    public static <T> T timed(String msg, Supplier<T> func) {
        long t1 = System.currentTimeMillis();
        T res = func.get();
        long t2 = System.currentTimeMillis();
        double time = (t2 - t1) / 1000.0;
        StringBuilder sb = new StringBuilder();
        sb.append(msg).append(" takes ").append(time).append(" seconds");
        logger.info(sb.toString());
        return res;
    }
}

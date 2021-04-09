package tech.tongyu.bct.service.quantlib.common.numerics.mc;

import java.time.LocalDateTime;

public interface McPathSingleAsset {
    double getSpot(LocalDateTime t);
    double getVariance(LocalDateTime start, LocalDateTime end);
    double df(LocalDateTime t);
    LocalDateTime[] getSimDates();
}

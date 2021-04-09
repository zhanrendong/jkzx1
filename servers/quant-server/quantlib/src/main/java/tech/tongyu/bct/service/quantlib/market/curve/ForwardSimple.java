package tech.tongyu.bct.service.quantlib.market.curve;

import java.time.LocalDateTime;

/**
 * A simple forward curve constructed from two discounting curves.
 * A spot date can be provided, but no date convention is provided. Thus either delviery date
 * should be calculated externally and then provided (when calling onDelivery), or one
 * has to assume delivery date is the same as expiry (when calling onExpiry).
 */
public class ForwardSimple implements Forward {
    private Discount r;
    private Discount q;
    private LocalDateTime val;
    private double spot;
    private LocalDateTime spotDate;
    private double effectiveSpot;

    public ForwardSimple(LocalDateTime val, LocalDateTime spotDate,
                         Discount r, Discount q, double spot) throws Exception{
        if (r.getVal().toLocalDate().isAfter(val.toLocalDate()))
            throw new Exception("Risk free/domestic curve's val date is in the future");
        if (r.getVal().toLocalDate().isBefore(val.toLocalDate()))
            this.r = r.roll(val);
        if (q.getVal().toLocalDate().isAfter(val.toLocalDate()))
            throw new Exception("Dividend/foreign curve's val date is in the future");
        if (q.getVal().toLocalDate().isBefore(val.toLocalDate()))
            this.q = q.roll(val);
        if (spotDate.isBefore(val))
            throw new Exception("Spot date must be after val date");
        this.val = val;
        this.spotDate = spotDate;
        this.effectiveSpot = spot * r.df(spotDate) / q.df(spotDate);

    }

    @Override
    public LocalDateTime getVal() {
        return val;
    }

    @Override
    public double onDelivery(LocalDateTime delivery) {
        return effectiveSpot * q.df(delivery) / r.df(delivery);
    }

    @Override
    public double onExpiry(LocalDateTime expiry) {
        return onDelivery(expiry);
    }
}

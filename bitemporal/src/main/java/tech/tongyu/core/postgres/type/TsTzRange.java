package tech.tongyu.core.postgres.type;

import java.time.OffsetDateTime;

public class TsTzRange {

    public static OffsetDateTime ORIGIN = OffsetDateTime.parse("1970-01-01T08:00:00+08:00");
    public static OffsetDateTime INFINITY = OffsetDateTime.parse("9999-12-31T23:59:59+08:00");

    private OffsetDateTime start;
    private OffsetDateTime end;

    public TsTzRange(OffsetDateTime start, OffsetDateTime end) {
        this.start = start;
        this.end = end;
    }

    public OffsetDateTime getStart() {
        return start;
    }

    public void setStart(OffsetDateTime start) {
        this.start = start;
    }

    public OffsetDateTime getEnd() {
        return end;
    }

    public void setEnd(OffsetDateTime end) {
        this.end = end;
    }

    public static TsTzRange of(OffsetDateTime start, OffsetDateTime end) {
        return new TsTzRange(start, end);
    }

    public static TsTzRange from(String s) {
        String[] ts = s.replace("\"", "").replace(" ", "T").split(",");
        String startStr = ts[0].trim().substring(1) + ":00";
        String endStr = ts[1].trim().substring(0, ts[1].length() - 1) + ":00";
        OffsetDateTime start = OffsetDateTime.parse(startStr);
        OffsetDateTime end = OffsetDateTime.parse(endStr);
        return TsTzRange.of(start, end);
    }

    public TsTzRange deepCopy() {
        return TsTzRange.of(OffsetDateTime.of(start.toLocalDateTime(), start.getOffset()),
                OffsetDateTime.of(end.toLocalDateTime(), end.getOffset()));
    }

    @Override
    public String toString() {
        return String.format("[%s,%s)", start.toString(), end.toString());
    }

    public Boolean contains(OffsetDateTime offsetDateTime) {
        return !start.isAfter(offsetDateTime) && end.isAfter(offsetDateTime);
    }
}

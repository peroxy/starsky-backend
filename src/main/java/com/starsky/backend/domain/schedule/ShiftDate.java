package com.starsky.backend.domain.schedule;

import java.time.Instant;

public class ShiftDate {
    Instant start;
    Instant end;

    public ShiftDate(Instant start, Instant end) {
        this.start = start;
        this.end = end;
    }

    public Instant getStart() {
        return start;
    }

    public Instant getEnd() {
        return end;
    }


    /**
     * Checks if the given shift is the subset of the current shift - if it fits into the time interval of the current shift.
     * e.g. if current shift is from 10:00-16:00 and the given shift is from 12:00-14:00 then that shift is a subset and will return false.
     */
    public boolean isSubsetOfShift(ShiftDate shift) {
        return isAfterOrEquals(shift.getStart(), start) && isBeforeOrEquals(shift.getEnd(), end);
    }

    private boolean isBeforeOrEquals(Instant date, Instant compareTo) {
        return date.equals(compareTo) || date.isBefore(compareTo);
    }

    private boolean isAfterOrEquals(Instant date, Instant compareTo) {
        return date.equals(compareTo) || date.isAfter(compareTo);
    }
}

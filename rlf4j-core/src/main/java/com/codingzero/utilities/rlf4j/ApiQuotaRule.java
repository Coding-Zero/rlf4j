package com.codingzero.utilities.rlf4j;

public class ApiQuotaRule {

    private long capacity;
    private long interval;
    private IntervalUnit intervalUnit;

    public ApiQuotaRule(long capacity, long interval, IntervalUnit intervalUnit) {
        this.capacity = capacity;
        this.interval = interval;
        this.intervalUnit = intervalUnit;
    }

    public long getCapacity() {
        return capacity;
    }

    public long getInterval() {
        return interval;
    }

    public IntervalUnit getIntervalUnit() {
        return intervalUnit;
    }

    public enum IntervalUnit {
        SECONDS,
        MINUTES,
        HOURS,
        DAYS
    }

}

package com.codingzero.utilities.rlf4j;

import java.util.Objects;

public class ApiQuotaRule {

    private String name;
    private long capacity;
    private long interval;
    private IntervalUnit intervalUnit;

    public ApiQuotaRule(String name, long capacity, long interval, IntervalUnit intervalUnit) {
        this.name = name;
        this.capacity = capacity;
        this.interval = interval;
        this.intervalUnit = intervalUnit;
    }

    public String getName() {
        return name;
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

    @Override
    public String toString() {
        return "ApiQuotaRule{" +
                "name='" + name + '\'' +
                ", capacity=" + capacity +
                ", interval=" + interval +
                ", intervalUnit=" + intervalUnit +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApiQuotaRule that = (ApiQuotaRule) o;
        return getCapacity() == that.getCapacity() &&
                getInterval() == that.getInterval() &&
                Objects.equals(getName(), that.getName()) &&
                getIntervalUnit() == that.getIntervalUnit();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getCapacity(), getInterval(), getIntervalUnit());
    }

    public enum IntervalUnit {
        SECONDS,
        MINUTES,
        HOURS,
        DAYS
    }

}

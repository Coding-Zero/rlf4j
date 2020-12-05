package com.codingzero.utilities.rlf4j.quotas;

import com.codingzero.utilities.rlf4j.ApiQuotaRule;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Refill;

import java.time.Duration;

public final class BandwidthConverter {

    public static Bandwidth toClassic(ApiQuotaRule rule) {
        return Bandwidth.classic(
                rule.getCapacity(),
                Refill.intervally(
                        rule.getCapacity(),
                        toDuration(rule.getInterval(), rule.getIntervalUnit())));
    }

    private static Duration toDuration(long interval, ApiQuotaRule.IntervalUnit intervalUnit) {
        Duration duration;
        switch (intervalUnit) {
            case SECONDS:
                duration = Duration.ofSeconds(interval);
                return duration;
            case MINUTES:
                duration = Duration.ofMinutes(interval);
                return duration;
            case HOURS:
                duration = Duration.ofHours(interval);
                return duration;
            case DAYS:
                duration = Duration.ofDays(interval);
                return duration;
            default: throw new IllegalArgumentException("Not support interval unit, " + intervalUnit);
        }
    }

}

package com.codingzero.utilities.rlf4j;

public class ConsumptionReport {

    public static final long UNKNOWN_REMAINING_QUOTA = -1;

    private boolean isConsumed;
    private long remainingQuota;
    private long consumedQuota;

    private ConsumptionReport(boolean isConsumed, long remainingQuota, long consumedQuota) {
        this.isConsumed = isConsumed;
        this.remainingQuota = remainingQuota;
        this.consumedQuota = consumedQuota;
    }

    public boolean isConsumed() {
        return isConsumed;
    }

    public long getRemainingQuota() {
        return remainingQuota;
    }

    public long getConsumedQuota() {
        return consumedQuota;
    }

    public static Builder consumed(long tokens) {
        Builder builder = new Builder(true, tokens);
        return builder;
    }

    public static Builder notConsumed() {
        Builder builder = new Builder(false, 0);
        builder = builder.remainingQuota(0);
        return builder;
    }

    @Override
    public String toString() {
        return "ConsumptionReport{" +
                "isConsumed=" + isConsumed +
                ", remainingQuota=" + remainingQuota +
                ", consumedQuota=" + consumedQuota +
                '}';
    }

    public static class Builder {
        private boolean isConsumed;
        private long remainingQuota;
        private long consumedQuota;

        public Builder(boolean isConsumed, long consumedQuota) {
            this.isConsumed = isConsumed;
            this.consumedQuota = consumedQuota;
        }

        public boolean isConsumed() {
            return isConsumed;
        }

        public long getRemainingQuota() {
            return remainingQuota;
        }

        public long getConsumedQuota() {
            return consumedQuota;
        }

        public Builder remainingQuota(long remainingQuota) {
            this.remainingQuota = remainingQuota;
            return this;
        }

        public ConsumptionReport build() {
            return new ConsumptionReport(isConsumed(), getRemainingQuota(), getConsumedQuota());
        }
    }
}
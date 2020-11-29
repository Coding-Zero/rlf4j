package com.codingzero.utilities.rlf4j;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class ApiIdentity {

    private final static String ID_DELIMITER = "::";

    private Map<String, String> fields;
    private String id;
    private CriticalLevel criticalLevel;
    private ResourceUsage resourceUsage;

    private ApiIdentity(Map<String, String> fields,
                       CriticalLevel criticalLevel,
                       ResourceUsage resourceUsage) {
        this.fields = Collections.unmodifiableMap(fields);
        this.criticalLevel = criticalLevel;
        this.resourceUsage = resourceUsage;
        setId();
    }

    private void setId() {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry: fields.entrySet()) {
            builder.append(entry.getValue());
            builder.append(ID_DELIMITER);
        }
        if (builder.length() > 0) {
            builder = builder.deleteCharAt(builder.lastIndexOf(ID_DELIMITER));
        }
        this.id = builder.toString();
    }

    public Map<String, String> getFields() {
        return fields;
    }

    public String getId() {
        return id;
    }

    public CriticalLevel getCriticalLevel() {
        return criticalLevel;
    }

    public ResourceUsage getResourceUsage() {
        return resourceUsage;
    }

    @Override
    public String toString() {
        return "ApiIdentity{" +
                "fields=" + fields +
                ", id='" + id + '\'' +
                ", criticalLevel=" + criticalLevel +
                ", resourceUsage=" + resourceUsage +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApiIdentity identity = (ApiIdentity) o;
        return Objects.equals(getFields(), identity.getFields()) &&
                Objects.equals(getId(), identity.getId()) &&
                getCriticalLevel() == identity.getCriticalLevel() &&
                getResourceUsage() == identity.getResourceUsage();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFields(), getId(), getCriticalLevel(), getResourceUsage());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Map<String, String> fields;
        private CriticalLevel criticalLevel;
        private ResourceUsage resourceUsage;

        private Builder() {
            fields = new LinkedHashMap<>();
            criticalLevel = CriticalLevel.TO_BE_DECIDED;
            resourceUsage = ResourceUsage.TO_BE_DECIDED;
        }

        public Builder field(String name, String value) {
            fields.put(name, value);
            return this;
        }

        public Builder criticalLevel(CriticalLevel criticalLevel) {
            this.criticalLevel = criticalLevel;
            return this;
        }

        public Builder resourceUsage(ResourceUsage resourceUsage) {
            this.resourceUsage = resourceUsage;
            return this;
        }

        public Map<String, String> getFields() {
            return Collections.unmodifiableMap(fields);
        }

        public CriticalLevel getCriticalLevel() {
            return criticalLevel;
        }

        public ResourceUsage getResourceUsage() {
            return resourceUsage;
        }

        public ApiIdentity build() {
            return new ApiIdentity(getFields(), getCriticalLevel(), getResourceUsage());
        }

    }
}

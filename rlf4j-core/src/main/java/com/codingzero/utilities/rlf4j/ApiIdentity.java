package com.codingzero.utilities.rlf4j;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class ApiIdentity {

    private final static  String ID_DELIMITER = "_";

    private List<String> fields;
    private String id;
    private CriticalLevel criticalLevel;
    private ResourceUsage resourceUsage;

    private ApiIdentity(List<String> fields,
                       CriticalLevel criticalLevel,
                       ResourceUsage resourceUsage) {
        this.fields = Collections.unmodifiableList(fields);
        this.criticalLevel = criticalLevel;
        this.resourceUsage = resourceUsage;
        setId();
    }

    private void setId() {
        StringBuilder builder = new StringBuilder();
        for (String field: fields) {
            builder.append(field);
            builder.append(ID_DELIMITER);
        }
        if (builder.length() > 0) {
            builder = builder.deleteCharAt(builder.lastIndexOf(ID_DELIMITER));
        }
        this.id = builder.toString();
    }

    public List<String> getFields() {
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

        private List<String> fields;
        private CriticalLevel criticalLevel;
        private ResourceUsage resourceUsage;

        private Builder() {
            fields = new LinkedList<>();
            criticalLevel = CriticalLevel.TO_BE_DECIDED;
            resourceUsage = ResourceUsage.TO_BE_DECIDED;
        }

        public Builder field(String field) {
            fields.add(field);
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

        public List<String> getFields() {
            return Collections.unmodifiableList(fields);
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

package com.codingzero.utilities.rlf4j;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ApiIdentity {

    private final static  String ID_DELIMITER = "_";

    private List<String> fields;
    private String id;
    private CriticalLevel criticalLevel;
    private ResourceUsage resourceUsage;

    public ApiIdentity(List<String> fields,
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
}

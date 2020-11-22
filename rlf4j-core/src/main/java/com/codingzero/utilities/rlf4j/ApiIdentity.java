package com.codingzero.utilities.rlf4j;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class ApiIdentity {

    private final static  String ID_DELIMITER = "_";

    private Map<String, String> fields;
    private Set<String> fieldNames;
    private String id;

    public ApiIdentity(Map<String, String> fields) {
        this.fields = Collections.unmodifiableMap(fields);
        setFieldNames();
        setId();
    }

    private void setFieldNames() {
        fieldNames = Collections.unmodifiableSet(fields.keySet());
    }

    private void setId() {
        StringBuilder builder = new StringBuilder();
        for (String name: fieldNames) {
            String value = fields.get(name);
            builder.append(value);
            builder.append(ID_DELIMITER);
        }
        int found = builder.lastIndexOf(ID_DELIMITER);
        if ((builder.length() - 1) == found) {
            builder = builder.deleteCharAt(found);
        }
        this.id = builder.toString();
    }

    public Map<String, String> getFields() {
        return fields;
    }

    public Set<String> getFieldNames() {
        return fieldNames;
    }

    public String getId() {
        return id;
    }
}

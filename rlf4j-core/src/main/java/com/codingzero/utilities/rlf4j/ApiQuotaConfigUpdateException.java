package com.codingzero.utilities.rlf4j;

public class ApiQuotaConfigUpdateException extends Exception {

    private ApiQuotaConfig config;
    private String name;

    public ApiQuotaConfigUpdateException(ApiQuotaConfig config, String name, Throwable throwable) {
        super("Failed to update " + name + " with config " + config + "due to " + throwable.getMessage(), throwable);
        this.config = config;
        this.name = name;
    }

    public ApiQuotaConfig getConfig() {
        return config;
    }

    public String getName() {
        return name;
    }

}

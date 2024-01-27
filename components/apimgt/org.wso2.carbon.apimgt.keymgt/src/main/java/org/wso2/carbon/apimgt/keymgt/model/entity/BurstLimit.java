package org.wso2.carbon.apimgt.keymgt.model.entity;

public class BurstLimit {

    private Integer rateLimitCount;
    private String rateLimitTimeUnit = null;

    public Integer getRateLimitCount() {
        return rateLimitCount;
    }

    public void setRateLimitCount(Integer rateLimitCount) {
        this.rateLimitCount = rateLimitCount;
    }

    public String getRateLimitTimeUnit() {
        return rateLimitTimeUnit;
    }

    public void setRateLimitTimeUnit(String rateLimitTimeUnit) {
        this.rateLimitTimeUnit = rateLimitTimeUnit;
    }

}

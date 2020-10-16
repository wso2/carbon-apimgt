package org.wso2.carbon.apimgt.throttle.policy.deployer;

public class BandwidthLimit {
    private String timeUnit;
    private int unitTime;
    private long dataAmount;
    private String dataUnit;

    public String getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(String timeUnit) {
        this.timeUnit = timeUnit;
    }

    public int getUnitTime() {
        return unitTime;
    }

    public void setUnitTime(int unitTime) {
        this.unitTime = unitTime;
    }

    public long getDataAmount() {
        return dataAmount;
    }

    public void setDataAmount(long dataAmount) {
        this.dataAmount = dataAmount;
    }

    public String getDataUnit() {
        return dataUnit;
    }

    public void setDataUnit(String dataUnit) {
        this.dataUnit = dataUnit;
    }
}

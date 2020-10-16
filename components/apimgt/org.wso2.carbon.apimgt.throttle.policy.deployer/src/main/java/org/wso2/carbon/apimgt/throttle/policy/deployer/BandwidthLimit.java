package org.wso2.carbon.apimgt.throttle.policy.deployer;

public class BandwidthLimit extends Limit {
    private long dataAmount;
    private String dataUnit;

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

package org.wso2.carbon.apimgt.usage.client.bean;

/**
 * Created by rukshan on 9/30/15.
 */
public class FirstAccessValue {
    long first_access_time;

    public long getFirst_access_time() {
        return first_access_time;
    }

    public void setFirst_access_time(long first_access_time) {
        this.first_access_time = first_access_time;
    }

    public FirstAccessValue(long first_access_time) {
        super();
        this.first_access_time = first_access_time;
    }
}

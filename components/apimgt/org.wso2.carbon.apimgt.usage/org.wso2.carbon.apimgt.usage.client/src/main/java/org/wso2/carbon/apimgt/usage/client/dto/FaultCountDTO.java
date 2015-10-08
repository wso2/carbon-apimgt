package org.wso2.carbon.apimgt.usage.client.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rukshan on 10/8/15.
 */
public class FaultCountDTO {
    String appName;
    List<ApiFaultCountArray> apiCountArray=new ArrayList<ApiFaultCountArray>();

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public List<ApiFaultCountArray> getApiCountArray() {
        return apiCountArray;
    }

    public void addToApiFaultCountArray(String apiName, long count) {
        ApiFaultCountArray usage=new ApiFaultCountArray();
        usage.setApiName(apiName);
        usage.setCount(count);
        this.apiCountArray.add(usage);
    }
}

class ApiFaultCountArray{
    String apiName;
    long count;

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}

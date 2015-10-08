package org.wso2.carbon.apimgt.usage.client.dto;


        import java.util.ArrayList;
        import java.util.List;

public class PerAppApiCountDTO {

    private String appName;
    private List<ApiCountArray> apiCountArray=new ArrayList<ApiCountArray>();

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public List<ApiCountArray> getApiCountArray() {
        return apiCountArray;
    }

    public void addToApiCountArray(String apiName,long count) {
        ApiCountArray counts=new ApiCountArray();
        counts.setApiName(apiName);
        counts.setCount(count);
        this.apiCountArray.add(counts);
    }
}

class ApiCountArray{
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

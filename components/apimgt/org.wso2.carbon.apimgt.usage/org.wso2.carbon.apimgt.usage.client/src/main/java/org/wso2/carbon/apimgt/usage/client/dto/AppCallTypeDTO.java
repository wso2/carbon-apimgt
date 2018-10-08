package org.wso2.carbon.apimgt.usage.client.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by asiri on 3/23/14.
 */
public class AppCallTypeDTO {


    private String appName;
    List<ApiCallTypeArray> apiCallTypeArray=new ArrayList<ApiCallTypeArray>();

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public List<ApiCallTypeArray> getApiCallTypeArray() {
        return apiCallTypeArray;
    }

    public void addToApiCallTypeArray(String apiName, List<String> callType, List<Integer> hitCount) {
        ApiCallTypeArray apiCallTypeArray = new ApiCallTypeArray();
        apiCallTypeArray.setApiName(apiName);
        apiCallTypeArray.setCallType(callType);
        apiCallTypeArray.setHitCount(hitCount);
        this.apiCallTypeArray.add(apiCallTypeArray);
    }
}

class ApiCallTypeArray {
    String apiName;
    List<String> callType;
    List<Integer> hitCount;

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public List<String> getCallType() {
        return callType;
    }

    public void setCallType(List<String> callType) {
        this.callType = callType;
    }

    public List<Integer> getHitCount() {
        return hitCount;
    }

    public void setHitCount(List<Integer> hitCount) {
        this.hitCount = hitCount;
    }
}
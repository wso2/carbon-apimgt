package org.wso2.carbon.apimgt.usage.client.dto;

import java.util.List;

/**
 * Created by asiri on 3/23/14.
 */
public class AppCallTypeDTO {


    private String appName;
    private String apiName;
    private String  consumerKey;
    private List<String> methods;


    public String getconsumerKey() {return consumerKey; }

    public void setconsumerKey(String consumerKey) { this.consumerKey = consumerKey;}

    public String getappName() {return appName; }

    public void setappName(String appName) { this.appName = appName;}

    public List<String>  getCallType() {return methods;}

    public void setCallType(List<String>  methods){ this.methods = methods;}

    public String getApiName() {return apiName; }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }


}

package org.wso2.carbon.apimgt.usage.client.dto;

/**
 * Created by asiri on 3/23/14.
 */
public class AppRegisteredUsersDTO {

    private String appName;
    private String  consumerKey;
    private String user;


    public String getconsumerKey() {return consumerKey; }

    public void setconsumerKey(String consumerKey) { this.consumerKey = consumerKey;}

    public String getappName() {return appName; }

    public void setappName(String appName) { this.appName = appName;}

    public String getUser() {return user;}

    public void setUser(String user){ this.user = user;}


}

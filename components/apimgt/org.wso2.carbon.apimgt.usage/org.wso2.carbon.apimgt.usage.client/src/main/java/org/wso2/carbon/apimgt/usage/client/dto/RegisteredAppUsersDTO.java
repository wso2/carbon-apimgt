package org.wso2.carbon.apimgt.usage.client.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rukshan on 10/8/15.
 */
public class RegisteredAppUsersDTO {
    String appName;
    List<String> userArray=new ArrayList<String>();

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public List<String> getUserArray() {
        return userArray;
    }

    public void addToUserArray(String user) {
        this.userArray.add(user);
    }
}


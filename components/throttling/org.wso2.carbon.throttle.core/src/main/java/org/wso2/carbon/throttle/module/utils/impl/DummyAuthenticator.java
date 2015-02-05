/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.throttle.module.utils.impl;


import org.wso2.carbon.throttle.module.utils.AuthenticationFuture;
import org.wso2.carbon.throttle.module.utils.Utils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class DummyAuthenticator implements AuthenticationFuture {
    private String consumerKey;
    private boolean isAuthenticated;
    private List userRoles;

    public DummyAuthenticator(String oAuthHeader){
        this.consumerKey = Utils.extractCustomerKeyFromAuthHeader(oAuthHeader);
        this.userRoles = new ArrayList();
    }

    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    public List getAuthorizedRoles() {
        return userRoles;
    }

    public String getAPIKey() {
        return consumerKey;
    }

    public String getURI() {
        return null;
    }

    public void setAuthenticated(boolean isAuthenticated) {
        this.isAuthenticated = isAuthenticated;
    }

    public void setAuthorizedRoles(List roles) {
        this.userRoles = roles;
    }
}

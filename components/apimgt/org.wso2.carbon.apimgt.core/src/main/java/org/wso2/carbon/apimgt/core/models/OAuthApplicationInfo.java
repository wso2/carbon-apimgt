/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.core.models;

import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Captures common attributes used in an OAuth Application.
 */
public final class OAuthApplicationInfo {

    private String clientId;
    private String clientName;
    private String callBackURL;
    private String grantTypes;
    private String clientSecret;
    private Map<String, Object> parameters = new HashMap<String, Object>();
    private boolean isSaasApplication;
    private String appOwner;

    /**
     * get client Id (consumer id)
     *
     * @return clientId
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * set client Id
     *
     * @param clientId
     */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    /**
     * Set client Name of OAuthApplication.
     *
     * @param clientName
     */
    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    /**
     * Set callback URL of OAuthapplication.
     *
     * @param callBackURL
     */
    public void setCallbackUrl(String callBackURL) {
        this.callBackURL = callBackURL;
    }

    /**
     * Set grant types of OAuth Application.
     *
     * @param grantTypes grant types
     */
    public void setGrantTypes(String grantTypes) {
        this.grantTypes = grantTypes;
    }

    public void addParameter(String name, Object value) {
        parameters.put(name, value);
    }

    public Object getParameter(String name) {
        return parameters.get(name);
    }

    public String getJSONString() {
        return JSONObject.toJSONString(parameters);

    }

    public String getClientName() {
        return clientName;
    }

    public String getCallbackUrl() {
        return callBackURL;
    }

    public String getGrantTypes() {
        return grantTypes;
    }

    public void putAll(Map<String, Object> parameters) {
        this.parameters.putAll(parameters);
    }

    public void removeParameter(String key) {
        this.parameters.remove(key);
    }

    public boolean getIsSaasApplication() {
        return isSaasApplication;
    }

    public void setIsSaasApplication(boolean isSaasApplication) {
        this.isSaasApplication = isSaasApplication;
    }

    public String getAppOwner() {
        return appOwner;
    }

    public void setAppOwner(String appOwner) {
        this.appOwner = appOwner;
    }

}

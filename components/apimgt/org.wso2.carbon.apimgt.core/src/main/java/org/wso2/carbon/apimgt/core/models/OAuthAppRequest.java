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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a request to create an OAuth App.
 */

public final class OAuthAppRequest {

    private String clientName;
    private String callBackURL;
    private String keyType;
    private List<String> grantTypes;
    private Map<String, Object> parameters = new HashMap<>();

    public OAuthAppRequest(String clientName, String callBackURL, String keyType, List<String> grantTypes) {
        this.clientName = clientName;
        this.callBackURL = callBackURL;
        this.keyType = keyType;
        this.grantTypes = grantTypes;
    }

    public String getKeyType() {
        return keyType;
    }

    public void setKeyType(String keyType) {
        this.keyType = keyType;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getCallBackURL() {
        return callBackURL;
    }

    public void setCallBackURL(String callBackURL) {
        this.callBackURL = callBackURL;
    }

    public List<String> getGrantTypes() {
        return grantTypes;
    }

    public void setGrantTypes(List<String> grantTypes) {
        this.grantTypes = grantTypes;
    }

    public void addParameter(String key, String value) {
        parameters.put(key, value);
    }

    public Object getParameter(String key) {
        return parameters.get(key);
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        return "OAuthAppRequest{" +
                "clientName='" + clientName + '\'' +
                ", callBackURL='" + callBackURL + '\'' +
                ", keyType='" + keyType + '\'' +
                ", grantTypes=" + Arrays.toString(grantTypes.toArray()) +
                '}';
    }
}

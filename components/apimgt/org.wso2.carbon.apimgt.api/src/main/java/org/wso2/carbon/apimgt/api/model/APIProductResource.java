/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.api.model;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class APIProductResource {

    private String apiName;
    private String apiId;
    private APIIdentifier apiIdentifier;
    private APIProductIdentifier productIdentifier;
    private URITemplate uriTemplate;
    private String endpointConfig;

    private String inSequenceName = "";
    private String outSequenceName = "";
    private String faultSequenceName = "";

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getApiId() {
        return apiId;
    }

    public void setApiId(String apiId) {
        this.apiId = apiId;
    }

    public void setApiIdentifier(APIIdentifier apiIdentifier) {
        this.apiIdentifier = apiIdentifier;
    }

    public APIIdentifier getApiIdentifier() {
        return apiIdentifier;
    }

    public void setProductIdentifier(APIProductIdentifier productIdentifier) {
        this.productIdentifier = productIdentifier;
    }

    public APIProductIdentifier getProductIdentifier() {
        return productIdentifier;
    }

    @Override
    public String toString() {
        String resources = uriTemplate.getHTTPVerb() + ":" + uriTemplate.getResourceURI() + " " ;
        return "APIProductResource [apiName=" + apiName + ", apiId=" + apiId + ", apiIdentifier=" + apiIdentifier
                + ", resources=" + resources + "]";
    }

    public URITemplate getUriTemplate() {
        return uriTemplate;
    }

    public void setUriTemplate(URITemplate uriTemplate) {
        this.uriTemplate = uriTemplate;
    }

    // Used by velocity template to generate synapse definition
    public String getInSequenceName() {
        return inSequenceName;
    }

    public void setInSequenceName(String inSequenceName) {
        this.inSequenceName = inSequenceName;
    }

    // Used by velocity template to generate synapse definition
    public String getFaultSequenceName() {
        return faultSequenceName;
    }

    public void setFaultSequenceName(String faultSequenceName) {
        this.faultSequenceName = faultSequenceName;
    }

    // Used by velocity template to generate synapse definition
    public String getOutSequenceName() {
        return outSequenceName;
    }

    public void setOutSequenceName(String outSequenceName) {
        this.outSequenceName = outSequenceName;
    }

    public String getEndpointConfig() {
        return endpointConfig;
    }

    // Used by velocity template to generate synapse definition
    public JSONObject getEndpointConfigAsJSON() throws ParseException {
        JSONParser parser = new JSONParser();

        Object config = parser.parse(endpointConfig);

        return (JSONObject) config;
    }

    public void setEndpointConfig(String endpointConfig) {
        this.endpointConfig = endpointConfig;
    }

    // Used by velocity template to generate synapse definition
    public String getEndpointKey() {
        return apiIdentifier.getApiName() + "--v" + apiIdentifier.getVersion();
    }
}

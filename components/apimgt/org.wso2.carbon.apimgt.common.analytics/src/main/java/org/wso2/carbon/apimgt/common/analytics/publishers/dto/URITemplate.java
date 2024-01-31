/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.common.analytics.publishers.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * URITemplate attribute in analytics event.
 */
public class URITemplate {
    private String uriTemplate;
    private String resourceURI;
    private String resourceSandboxURI;
    private String httpVerb;

    private String authScheme;
    private List<OperationPolicy> operationPolicies = new ArrayList<>();

    public void setOperationPolicies(List<OperationPolicy> operationPolicies) {
        this.operationPolicies = new ArrayList(operationPolicies);
    }

    public List<OperationPolicy> getOperationPolicies() {
        return new ArrayList(operationPolicies);
    }

    public void setHttpVerb(String httpVerb) {
        this.httpVerb = httpVerb;
    }

    public String getHttpVerb() {
        return httpVerb;
    }

    public void setUriTemplate(String uriTemplate) {
        this.uriTemplate = uriTemplate;
    }

    public String getUriTemplate() {
        return uriTemplate;
    }

    public void setResourceURI(String resourceURI) {
        this.resourceURI = resourceURI;
    }

    public String getResourceURI() {
        return resourceURI;
    }

    public void setResourceSandboxURI(String resourceSandboxURI) {
        this.resourceSandboxURI = resourceSandboxURI;
    }

    public String getResourceSandboxURI() {
        return resourceSandboxURI;
    }

    public void setAuthScheme(String authScheme) {
        this.authScheme = authScheme;
    }

    public String getAuthScheme() {
        return authScheme;
    }
}

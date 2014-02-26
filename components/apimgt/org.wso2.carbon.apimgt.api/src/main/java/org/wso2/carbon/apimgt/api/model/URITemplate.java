/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.api.model;

import java.util.*;

public class URITemplate {

    private String uriTemplate;
    private String resourceURI;
    private String resourceSandboxURI;
    private String httpVerb;
    private String authType;
    private LinkedHashSet<String> httpVerbs = new LinkedHashSet<String>();
    private List<String> authTypes = new ArrayList<String>();
    private String throttlingTier;
    private List<String> throttlingTiers = new ArrayList<String>();

    public String getThrottlingTier() {
        return throttlingTier;
    }

    public void setThrottlingTier(String throttlingTier) {
        this.throttlingTier = throttlingTier;
    }

    public String getThrottlingTiers(){
        return throttlingTier;
    }

    public void setThrottlingTiers(List<String> throttlingTiers) {
        this.throttlingTiers = throttlingTiers;
    }


    public String getHTTPVerb() {
        return httpVerb;
    }

    public void setHTTPVerb(String httpVerb) {
        this.httpVerb = httpVerb;
    }

    public String getAuthType() {
        return authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;

    }

    public String getResourceURI() {
        return resourceURI;
    }

    public void setResourceURI(String resourceURI) {
        this.resourceURI = resourceURI;
    }

    public boolean isResourceURIExist(){
        return this.resourceURI != null;
    }

    public String getResourceSandboxURI() {
        return resourceSandboxURI;
    }

    public void setResourceSandboxURI(String resourceSandboxURI) {
        this.resourceSandboxURI = resourceSandboxURI;
    }

    public boolean isResourceSandboxURIExist(){
        return this.resourceSandboxURI != null;
    }

    public String getUriTemplate() {
        return uriTemplate;
    }

    public void setUriTemplate(String template) {
        this.uriTemplate = template;
    }

    public void setHttpVerbs(String httpVerb) {

        httpVerbs.add(httpVerb);
    }

    public String getHttpVerbs() {

        return httpVerb;
    }

    public void setAuthTypes(String authType) {

        authTypes.add(authType);
    }

    public String getAuthTypes() {

        return authType;
    }


    public String getMethodsAsString() {
        String str = "";
        for (String method : httpVerbs) {
            str += method + " ";
        }
        return str.trim();
    }

    public String getAuthTypeAsString() {
        String str = "";
        for (String authType : authTypes) {
            str += authType + " ";
        }
        return str.trim();
    }

    public void setThrottlingTiers(String tier) {
        throttlingTiers.add(tier);
    }

    public String getThrottlingTiersAsString() {
        String str = "";
        for (String tier : throttlingTiers) {
            tier = tier.trim();
            str = str + tier + " ";
        }
        return str.trim();
    }

}

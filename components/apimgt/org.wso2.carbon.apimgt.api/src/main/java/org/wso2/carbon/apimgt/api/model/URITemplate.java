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

import org.json.simple.JSONValue;

import java.io.Serializable;
import java.util.*;

public class URITemplate implements Serializable{

    private String uriTemplate;
    private String resourceURI;
    private String resourceSandboxURI;
    private String httpVerb;
    private String authType;
    private LinkedHashSet<String> httpVerbs = new LinkedHashSet<String>();
    private List<String> authTypes = new ArrayList<String>();
    private String throttlingTier;
    private List<String> throttlingTiers = new ArrayList<String>();
    private Scope scope;
    private String mediationScript;
    private List<Scope> scopes = new ArrayList<Scope>();
    private Map<String, String> mediationScripts = new HashMap<String, String>();

    public String getMediationScript() {
        return mediationScript;
    }


    public void setMediationScript(String mediationScript) {
        this.mediationScript = mediationScript;
    }

    /**
     * Set mediation script for a given http method
     * @param method http method name
     * @param mediationScript mediation script content
     */
    public void setMediationScripts(String method, String mediationScript){
        if (mediationScript != null  && !mediationScript.trim().equals("") && !mediationScript.trim().equals("null")){
            mediationScripts.put(method, mediationScript);
        }
    }

    /**
     * Generating the script by aggregating scripts of each http method to form a single script in to be
     * used when generating synapse configuration file.
     *
     * @return aggregated script in the following format,
     * if (http-method = 'GET'){
     *     //script for GET
     * }
     * ....
     * ....
     * if (http-method = 'POST'){
     *     //script for POST
     * }
     */
    public String getAggregatedMediationScript(){
        if (mediationScripts.isEmpty()){
            return "null";
        }else if (mediationScripts.size() == 1 && httpVerbs.size() == 1){
            return mediationScript;
        }else{
            StringBuilder aggregatedScript = new StringBuilder();

            for (Map.Entry<String, String> entry : mediationScripts.entrySet()){
                String httpMethod = entry.getKey();
                String mediationScript = entry.getValue();

                aggregatedScript.append("if (mc.getProperty('REST_METHOD') == '" + httpMethod + "'){");
                aggregatedScript.append(mediationScript);
                aggregatedScript.append("}");

            }

            return aggregatedScript.toString();
        }
    }

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

    public Scope getScope() {
        return scope;
    }
    public Scope getScopes() {
        return scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    public void setScopes(Scope scope){
        this.scopes.add(scope);
    }

    public String getResourceMap(){
        Map verbs = new LinkedHashMap();
        int i = 0;
        for (String method : httpVerbs) {
            Map verb = new LinkedHashMap();
            verb.put("auth_type",authTypes.get(i));
            verb.put("throttling_tier",throttlingTiers.get(i));
            try{
                Scope tmpScope = scopes.get(i);
                if(tmpScope != null){
                    verb.put("scope",tmpScope.getKey());
                }
            }catch(IndexOutOfBoundsException e){
                //todo need to rewrite to prevent this type of exceptions
            }
            verbs.put(method,verb);
            i++;
        }
        //todo this is a hack to make key validation service stub from braking need to rewrite.
        return JSONValue.toJSONString(verbs);
    }
}

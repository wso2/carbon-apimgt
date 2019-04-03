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


import java.util.HashMap;
import java.util.Map;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/*
* Captures common attributes used in an OAuth Application.
* */
public class OAuthApplicationInfo {

    private String clientId;
    private String clientName;
    private String callBackURL;
    private String clientSecret;
    private Map<String,Object> parameters = new HashMap<String, Object>();
    private boolean isSaasApplication;
    private String appOwner;
    private String jsonString;
    private Map<String, String> appAttributes = new HashMap<>();
    private String jsonAppAttribute;

    private String tokenType;



    public void setJsonString(String jsonString) {
        this.jsonString = jsonString;
    }
    /**
     * get client Id (consumer id)
     * @return clientId
     */
    public String getClientId() {
        return clientId;
    }
    /**
     * set client Id
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
     * @param clientName
     */
    public void setClientName(String clientName){
        this.clientName = clientName;
    }

    /**
     * Set callback URL of OAuthapplication.
     * @param callBackURL
     */
    public void setCallBackURL(String callBackURL){
        this.callBackURL = callBackURL;
    }

    public void addParameter(String name,Object value){
        parameters.put(name,value);
    }

    public Object getParameter(String name){
        return parameters.get(name);
    }

    public String getJsonString(){
        if(jsonString != null){
            return jsonString;
        } else {
            return JSONObject.toJSONString(parameters);
        }

    }

    public String getClientName(){
        return clientName;
    }

    public String getCallBackURL(){
        return callBackURL;
    }

    public void putAll(Map<String,Object> parameters){
        this.parameters.putAll(parameters);
    }

    public void removeParameter(String key){
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

    /**
     * Get custom attributes of OAuthApplication.
     * @param key
     *
     * @return appAttribute.get(value)
     */
    public String getAppAttribute(String key) {

        return (String) appAttributes.get(key);
    }

    /**
     * Set all custom attributes of OAuthApplication.
     * @param appAttributes
     */
    public void putAllAppAttributes(Map<String,String> appAttributes) {

        this.appAttributes.putAll(appAttributes);
    }

    /**
     * Set a custom attribute of OAuthApplication.
     * @param key
     * @param value
     */
    public void addAppAttribute(String key, String value) {

        appAttributes.put(key, value);
    }

    public void setJsonAppAttribute (String jsonAppAttribute) {

        this.jsonAppAttribute = jsonAppAttribute;
    }

    public String getJsonAppAttribute() {

        if (jsonAppAttribute != null) {
            return jsonAppAttribute;
        } else {
            return JSONObject.toJSONString(appAttributes);
        }
    }


    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

}
/*
 *
 *   Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * /
 */
package org.wso2.carbon.apimgt.api.model;


import java.util.Map;
import java.util.Set;

public class APISubscription {
    private int appId;
    private String appName;
    private String callbackUrl;
    private String prodKey;
    private String prodKeyScope;
    private String prodKeyScopeValue;
    private String prodConsumerKey;
    private String prodConsumerSecret;
    private String prodJsonString;
    private String prodKeyState;
    private String prodAuthorizedDomains;
    private long prodValidityTime;
    private boolean prodRegenerateOption;
    private String sandKey;
    private String sandKeyScope;
    private String sandKeyScopeValue;
    private String sandConsumerKey;
    private String sandConsumerSecret;
    private String sandJsonString;
    private String sandKeyState;
    private String sandAuthorizedDomains;
    private long sandValidityTime;
    private boolean sandRegenerateOption;
    private Set<Scope> scopes;
    private Set<Map<String,Object>> subscriptions;

    public void setAppId(int id) {
        this.appId = id;
    }

    public int getAppId() {
        return appId;
    }

    public void setAppName(String name) {
        this.appName = name;
    }

    public String getAppName() {
        return appName;
    }

    public void setCallbackUrl(String url) {
        this.callbackUrl = url;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setProdKey(String key) {
        this.prodKey = key;
    }

    public String getProdKey() {
        return prodKey;
    }

    public void setProdKeyScope(String scope) {
        this.prodKeyScope = scope;
    }

    public String getProdKeyScope() {
        return prodKeyScope;
    }

    public void setProdKeyScopeValue(String scopeVal) {
        this.prodKeyScopeValue = scopeVal;
    }

    public String getProdKeyScopeValue() {
        return prodKeyScopeValue;
    }

    public void setProdConsumerKey(String consumerKey) {
        this.prodConsumerKey = consumerKey;
    }

    public String getProdConsumerKey() {
        return prodConsumerKey;
    }

    public void setProdConsumerSecret(String consumerSecret) {
        this.prodConsumerSecret = consumerSecret;
    }

    public String getProdConsumerSecret() {
        return prodConsumerSecret;
    }

    public void setProdJsonString(String jsonString) {
        this.prodJsonString = jsonString;
    }

    public String getProdJsonString() {
        return prodJsonString;
    }

    public void setProdKeyState(String state) {
        this.prodKeyState = state;
    }

    public String getProdKeyState() {
        return prodKeyState;
    }

    public void setProdAuthorizedDomains(String domains) {
        this.prodAuthorizedDomains = domains;
    }

    public String getProdAuthorizedDomains() {
        return prodAuthorizedDomains;
    }

    public void setProdValidityTime(long time) {
        this.prodValidityTime = time;
    }

    public long getProdValidityTime() {
        return prodValidityTime;
    }

    public void setProdRegenerateOption(boolean regenerateOption) {
        this.prodRegenerateOption = regenerateOption;
    }

    public boolean isProdRegenerateOption() {
        return prodRegenerateOption;
    }

    public void setSandKey(String key) {
        this.sandKey = key;
    }

    public String getSandKey() {
        return sandKey;
    }

    public void setSandKeyScope(String scope) {
        this.sandKeyScope = scope;
    }

    public String getSandKeyScope() {
        return sandKeyScope;
    }

    public void setSandKeyScopeValue(String scopeVal) {
        this.sandKeyScopeValue = scopeVal;
    }

    public String getSandKeyScopeValue() {
        return sandKeyScopeValue;
    }

    public void setSandConsumerKey(String consumerKey) {
        this.sandConsumerKey = consumerKey;
    }

    public String getSandConsumerKey() {
        return sandConsumerKey;
    }

    public void setSandConsumerSecret(String consumerSecret) {
        this.sandConsumerSecret = consumerSecret;
    }

    public String getSandConsumerSecret() {
        return sandConsumerSecret;
    }

    public void setSandJsonString(String jsonString) {
        this.sandJsonString = jsonString;
    }

    public String getSandJsonString() {
        return sandJsonString;
    }

    public void setSandKeyState(String state) {
        this.sandKeyState = state;
    }

    public String getSandKeyState() {
        return sandKeyState;
    }

    public void setSandAuthorizedDomains(String domains) {
        this.sandAuthorizedDomains = domains;
    }

    public String getSandAuthorizedDomains() {
        return sandAuthorizedDomains;
    }

    public void setSandRegenerateOption(boolean regenerateOption) {
        this.sandRegenerateOption = regenerateOption;
    }

    public boolean isSandRegenerateOption() {
        return sandRegenerateOption;
    }

    public void setSandValidityTime(long time) {
        this.sandValidityTime = time;
    }

    public long getSandValidityTime() {
        return sandValidityTime;
    }
    public void setScopes(Set<Scope> scopes){
        this.scopes=scopes;
    }
    public Set<Scope> getScopes(){
        return scopes;
    }
    public void setSubscriptions(Set<Map<String,Object>> subs){
        this.subscriptions=subs;
    }
    public Set<Map<String,Object>> getSubscriptions(){
        return subscriptions;
    }
}

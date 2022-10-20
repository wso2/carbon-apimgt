/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
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

package org.wso2.carbon.apimgt.impl.monetization;

import org.json.simple.JSONArray;

public class MonetizationConfigurationDto {

    private String monetizationImpl;
    private String insightAPIEndpoint;
    private String analyticsAccessToken;
    private String choreoTokenEndpoint;
    private String insightAppConsumerKey;
    private String insightAppConsumerSecret;
    private String granularity;
    private String publishTimeDurationInDays;

    private String analyticsHost;

    private int analyticsPort;

    private String analyticsUserName;

    private byte[] analyticsPassword;

    public String getAnalyticsIndexName() {
        return analyticsIndexName;
    }

    public void setAnalyticsIndexName(String analyticsIndexName) {
        this.analyticsIndexName = analyticsIndexName;
    }

    private String analyticsIndexName;
    private JSONArray monetizationAttributes = new JSONArray();

    public String getMonetizationImpl() {

        return monetizationImpl;
    }

    public void setMonetizationImpl(String monetizationImpl) {

        this.monetizationImpl = monetizationImpl;
    }

    public String getChoreoTokenEndpoint() {

        return choreoTokenEndpoint;
    }

    public void setChoreoTokenEndpoint(String choreoTokenEndpoint) {

        this.choreoTokenEndpoint = choreoTokenEndpoint;
    }

    public String getInsightAppConsumerKey() {

        return insightAppConsumerKey;
    }

    public void setInsightAppConsumerKey(String insightAppConsumerKey) {

        this.insightAppConsumerKey = insightAppConsumerKey;
    }

    public String getInsightAppConsumerSecret() {

        return insightAppConsumerSecret;
    }

    public void setInsightAppConsumerSecret(String insightAppConsumerSecret) {

        this.insightAppConsumerSecret = insightAppConsumerSecret;
    }

    public String getInsightAPIEndpoint() {

        return insightAPIEndpoint;
    }

    public void setInsightAPIEndpoint(String insightAPIEndpoint) {

        this.insightAPIEndpoint = insightAPIEndpoint;
    }

    public String getAnalyticsAccessToken() {

        return analyticsAccessToken;
    }

    public void setAnalyticsAccessToken(String analyticsAccessToken) {

        this.analyticsAccessToken = analyticsAccessToken;
    }

    public JSONArray getMonetizationAttributes() {

        return monetizationAttributes;
    }

    public void setMonetizationAttributes(JSONArray monetizationAttributes) {

        this.monetizationAttributes = monetizationAttributes;
    }

    public String getGranularity() {

        return granularity;
    }

    public void setGranularity(String granularity) {

        this.granularity = granularity;
    }

    public String getPublishTimeDurationInDays() {

        return publishTimeDurationInDays;
    }

    public void setPublishTimeDurationInDays(String publishTimeDurationInDays) {

        this.publishTimeDurationInDays = publishTimeDurationInDays;
    }

    public String getAnalyticsHost() {
        return analyticsHost;
    }

    public void setAnalyticsHost(String analyticsHost) {
        this.analyticsHost = analyticsHost;
    }

    public int getAnalyticsPort() {
        return analyticsPort;
    }

    public void setAnalyticsPort(int analyticsPort) {
        this.analyticsPort = analyticsPort;
    }

    public String getAnalyticsUserName() {
        return analyticsUserName;
    }

    public void setAnalyticsUserName(String analyticsUserName) {
        this.analyticsUserName = analyticsUserName;
    }

    public byte[] getAnalyticsPassword() {
        return analyticsPassword;
    }

    public void setAnalyticsPassword(byte[] analyticsPassword) {
        this.analyticsPassword = analyticsPassword;
    }
}

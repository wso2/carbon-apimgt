package org.wso2.carbon.apimgt.impl.monetization;

import org.json.simple.JSONArray;

public class MonetizationConfigurationDto {

    private String monetizationImpl;
    private String insightAPIEndpoint;
    private String analyticsAccessToken;
    private String choreoTokenEndpoint;
    private String insightAppConsumerKey;
    private String insightAppConsumerSecret;
    private JSONArray monetizationAttributes;
    private String granularity;
    private String publishTimeDurationInDays;

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
}

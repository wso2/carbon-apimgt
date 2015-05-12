/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.ganalytics.publisher;

public class GoogleAnalyticsData {
    /** General **/
    private String protocolVersion = "1";
    private String trackingId;
    private Integer anonymizeIP;
    private Integer queueTime;
    private String cacheBuster;

    /** Client **/
    private String clientId;
    private String userId;

    /** Session **/
    private String sessionControl;
    private String IPOverride;
    private String userAgentOverride;

    /** Traffic Sources **/
    private String referrer;
    private String campaignName;
    private String campaignSource;
    private String campaignMedium;
    private String campaignKeyword;
    private String campaignContent;
    private String campaignId;
    private String googleAdwordsId;
    private String googleDisplayAdsId;

    /** System **/
    private String screenResolutoin;
    private String viewPortSize;
    private String documentEncoding;
    private String screenColors;
    private String userLanguage;
    private String javaEnabled;
    private String flashVersion;

    /** Hit **/
    private String hitType;
    private Integer nonInteractionHit;

    /** Content Information **/
    private String documentLocationUrl;
    private String documentHostName;
    private String documentPath;
    private String documentTitle;
    private String screenName;
    private String linkId;

    /** App Tracking **/
    private String appName;
    private String appId;
    private String appVersion;
    private String appInstallerId;

    /** Event Tracking **/
    private String eventCategory;
    private String eventAction;
    private String eventLabel;
    private Integer eventValue;

    /** E-Commerce **/
    private String transactionId;
    private String transactionAffiliation;
    private Double transactionRevenue;
    private Double transactionShipping;
    private Double transactionTax;
    private String itemName;
    private Double itemPrice;
    private Integer itemQty;
    private String itemCode;
    private String itemCategory;
    private String currencyCode;

    /** Social Interactions **/
    private String socialNetwork;
    private String socialAction;
    private String socialActionTarget;

    /** Timing **/
    private String userTimingCategory;
    private String userTimingVariableName;
    private Integer userTimingTime;
    private String userTimingLabel;
    private Integer pageLoadTime;
    private Integer dnsTime;
    private Integer pageDownloadTime;
    private Integer redirectResponseTime;
    private Integer tcpConnectTime;
    private Integer serverResponseTime;

    /** Exceptions **/
    private String exceptionDescription;
    private Integer fatalException;

    /** Custom Dimensions **/
    private String customDimension;
    private String customMetric;

    /** Content Experiments **/
    private String experimentId;
    private String experimentVariant;

    private GoogleAnalyticsData(DataBuilder builder) {
        /** General **/
        this.protocolVersion = builder.protocolVersion;
        this.trackingId = builder.trackingId;
        this.anonymizeIP = builder.anonymizeIP;
        this.queueTime = builder.queueTime;
        this.cacheBuster = builder.cacheBuster;

        /** Client **/
        this.clientId = builder.clientId;
        this.userId = builder.userId;

        /** Session **/
        this.sessionControl = builder.sessionControl;
        this.IPOverride = builder.IPOverride;
        this.userAgentOverride = builder.userAgentOverride;

        /** Traffic Sources **/
        this.referrer = builder.referrer;
        this.campaignName = builder.campaignName;
        this.campaignSource = builder.campaignSource;
        this.campaignMedium = builder.campaignMedium;
        this.campaignKeyword = builder.campaignKeyword;
        this.campaignContent = builder.campaignContent;
        this.campaignId = builder.campaignId;
        this.googleAdwordsId = builder.googleAdwordsId;
        this.googleDisplayAdsId = builder.googleDisplayAdsId;

        /** System **/
        this.screenResolutoin = builder.screenResolutoin;
        this.viewPortSize = builder.viewPortSize;
        this.documentEncoding = builder.documentEncoding;
        this.screenColors = builder.screenColors;
        this.userLanguage = builder.userLanguage;
        this.javaEnabled = builder.javaEnabled;
        this.flashVersion = builder.flashVersion;

        /** Hit **/
        this.hitType = builder.hitType;
        this.nonInteractionHit = builder.nonInteractionHit;

        /** Content Information **/
        this.documentLocationUrl = builder.documentLocationUrl;
        this.documentHostName = builder.documentHostName;
        this.documentPath = builder.documentPath;
        this.documentTitle = builder.documentTitle;
        this.screenName = builder.screenName;
        this.linkId = builder.linkId;

        /** App Tracking **/
        this.appName = builder.appName;
        this.appId = builder.appId;
        this.appVersion = builder.appVersion;
        this.appInstallerId = builder.appInstallerId;

        /** Event Tracking **/
        this.eventCategory = builder.eventCategory;
        this.eventAction = builder.eventAction;
        this.eventLabel = builder.eventLabel;
        this.eventValue = builder.eventValue;

        /** E-Commerce **/
        this.transactionId = builder.transactionId;
        this.transactionAffiliation = builder.transactionAffiliation;
        this.transactionRevenue = builder.transactionRevenue;
        this.transactionShipping = builder.transactionShipping;
        this.transactionTax = builder.transactionTax;
        this.itemName = builder.itemName;
        this.itemPrice = builder.itemPrice;
        this.itemQty = builder.itemQty;
        this.itemCode = builder.itemCode;
        this.itemCategory = builder.itemCategory;
        this.currencyCode = builder.currencyCode;

        /** Social Interactions **/
        this.socialNetwork = builder.socialNetwork;
        this.socialAction = builder.socialAction;
        this.socialActionTarget = builder.socialActionTarget;

        /** Timing **/
        this.userTimingCategory = builder.userTimingCategory;
        this.userTimingVariableName = builder.userTimingVariableName;
        this.userTimingTime = builder.userTimingTime;
        this.userTimingLabel = builder.userTimingLabel;
        this.pageLoadTime = builder.pageLoadTime;
        this.dnsTime = builder.dnsTime;
        this.pageDownloadTime = builder.pageDownloadTime;
        this.redirectResponseTime = builder.redirectResponseTime;
        this.tcpConnectTime = builder.tcpConnectTime;
        this.serverResponseTime = builder.serverResponseTime;

        /** Exceptions **/
        this.exceptionDescription = builder.exceptionDescription;
        this.fatalException = builder.fatalException;

        /** Custom Dimensions **/
        this.customDimension = builder.customDimension;
        this.customMetric = builder.customMetric;

        /** Content Experiments **/
        this.experimentId = builder.experimentId;
        this.experimentVariant = builder.experimentVariant;
    }

    public String getProtocolVersion() {
        return protocolVersion;
    }

    public String getTrackingId() {
        return trackingId;
    }

    public Integer isAnonymizeIP() {
        return anonymizeIP;
    }

    public int getQueueTime() {
        return queueTime;
    }

    public String getCacheBuster() {
        return cacheBuster;
    }

    public String getClientId() {
        return clientId;
    }

    public String getUserId() {
        return userId;
    }

    public String getSessionControl() {
        return sessionControl;
    }

    public String getIPOverride() {
        return IPOverride;
    }

    public String getUserAgentOverride() {
        return userAgentOverride;
    }

    public String getReferrer() {
        return referrer;
    }

    public String getCampaignName() {
        return campaignName;
    }

    public String getCampaignSource() {
        return campaignSource;
    }

    public String getCampaignMedium() {
        return campaignMedium;
    }

    public String getCampaignKeyword() {
        return campaignKeyword;
    }

    public String getCampaignContent() {
        return campaignContent;
    }

    public String getCampaignId() {
        return campaignId;
    }

    public String getGoogleAdwordsId() {
        return googleAdwordsId;
    }

    public String getGoogleDisplayAdsId() {
        return googleDisplayAdsId;
    }

    public String getScreenResolutoin() {
        return screenResolutoin;
    }

    public String getViewPortSize() {
        return viewPortSize;
    }

    public String getDocumentEncoding() {
        return documentEncoding;
    }

    public String getScreenColors() {
        return screenColors;
    }

    public String getUserLanguage() {
        return userLanguage;
    }

    public String getJavaEnabled() {
        return javaEnabled;
    }

    public String getFlashVersion() {
        return flashVersion;
    }

    public String getHitType() {
        return hitType;
    }

    public Integer isNonInteractionHit() {
        return nonInteractionHit;
    }

    public String getDocumentLocationUrl() {
        return documentLocationUrl;
    }

    public String getDocumentHostName() {
        return documentHostName;
    }

    public String getDocumentPath() {
        return documentPath;
    }

    public String getDocumentTitle() {
        return documentTitle;
    }

    public String getScreenName() {
        return screenName;
    }

    public String getLinkId() {
        return linkId;
    }

    public String getAppName() {
        return appName;
    }

    public String getAppId() {
        return appId;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public String getAppInstallerId() {
        return appInstallerId;
    }

    public String getEventCategory() {
        return eventCategory;
    }

    public String getEventAction() {
        return eventAction;
    }

    public String getEventLabel() {
        return eventLabel;
    }

    public Integer getEventValue() {
        return eventValue;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getTransactionAffiliation() {
        return transactionAffiliation;
    }

    public Double getTransactionRevenue() {
        return transactionRevenue;
    }

    public Double getTransactionShipping() {
        return transactionShipping;
    }

    public Double getTransactionTax() {
        return transactionTax;
    }

    public String getItemName() {
        return itemName;
    }

    public Double getItemPrice() {
        return itemPrice;
    }

    public Integer getItemQty() {
        return itemQty;
    }

    public String getItemCode() {
        return itemCode;
    }

    public String getItemCategory() {
        return itemCategory;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public String getSocialNetwork() {
        return socialNetwork;
    }

    public String getSocialAction() {
        return socialAction;
    }

    public String getSocialActionTarget() {
        return socialActionTarget;
    }

    public String getUserTimingCategory() {
        return userTimingCategory;
    }

    public String getUserTimingVariableName() {
        return userTimingVariableName;
    }

    public Integer getUserTimingTime() {
        return userTimingTime;
    }

    public String getUserTimingLabel() {
        return userTimingLabel;
    }

    public Integer getPageLoadTime() {
        return pageLoadTime;
    }

    public Integer getDnsTime() {
        return dnsTime;
    }

    public Integer getPageDownloadTime() {
        return pageDownloadTime;
    }

    public Integer getRedirectResponseTime() {
        return redirectResponseTime;
    }

    public Integer getTcpConnectTime() {
        return tcpConnectTime;
    }

    public Integer getServerResponseTime() {
        return serverResponseTime;
    }

    public String getExceptionDescription() {
        return exceptionDescription;
    }

    public Integer isFatalException() {
        return fatalException;
    }

    public String getCustomDimension() {
        return customDimension;
    }

    public String getCustomMetric() {
        return customMetric;
    }

    public String getExperimentId() {
        return experimentId;
    }

    public String getExperimentVariant() {
        return experimentVariant;
    }


    /**
    * This class can be used to generate a data object adhering to the Google Analytics Measurement Protocol v1
    *                      https://developers.google.com/analytics/devguides/collection/protocol/v1/parameters
    */
    public static class DataBuilder {

        /** General **/
        private String protocolVersion = "1";
        private String trackingId;
        private Integer anonymizeIP;
        private Integer queueTime;
        private String cacheBuster;

        /** Client **/
        private String clientId;
        private String userId;

        /** Session **/
        private String sessionControl;
        private String IPOverride;
        private String userAgentOverride;

        /** Traffic Sources **/
        private String referrer;
        private String campaignName;
        private String campaignSource;
        private String campaignMedium;
        private String campaignKeyword;
        private String campaignContent;
        private String campaignId;
        private String googleAdwordsId;
        private String googleDisplayAdsId;

        /** System **/
        private String screenResolutoin;
        private String viewPortSize;
        private String documentEncoding;
        private String screenColors;
        private String userLanguage;
        private String javaEnabled;
        private String flashVersion;

        /** Hit **/
        private String hitType;
        private Integer nonInteractionHit;

        /** Content Information **/
        private String documentLocationUrl;
        private String documentHostName;
        private String documentPath;
        private String documentTitle;
        private String screenName;
        private String linkId;

        /** App Tracking **/
        private String appName;
        private String appId;
        private String appVersion;
        private String appInstallerId;

        /** Event Tracking **/
        private String eventCategory;
        private String eventAction;
        private String eventLabel;
        private Integer eventValue;

        /** E-Commerce **/
        private String transactionId;
        private String transactionAffiliation;
        private Double transactionRevenue;
        private Double transactionShipping;
        private Double transactionTax;
        private String itemName;
        private Double itemPrice;
        private Integer itemQty;
        private String itemCode;
        private String itemCategory;
        private String currencyCode;

        /** Social Interactions **/
        private String socialNetwork;
        private String socialAction;
        private String socialActionTarget;

        /** Timing **/
        private String userTimingCategory;
        private String userTimingVariableName;
        private Integer userTimingTime;
        private String userTimingLabel;
        private Integer pageLoadTime;
        private Integer dnsTime;
        private Integer pageDownloadTime;
        private Integer redirectResponseTime;
        private Integer tcpConnectTime;
        private Integer serverResponseTime;

        /** Exceptions **/
        private String exceptionDescription;
        private Integer fatalException;

        /** Custom Dimensions **/
        private String customDimension;
        private String customMetric;

        /** Content Experiments **/
        private String experimentId;
        private String experimentVariant;

        /**
         * Builder constructor, use this to build a GoogleAnalyticsData object. Retrieve this object by calling
         * the build() method. The constructor takes the minimum required parameters. NOTE: depending on the hitType,
         * additional parameters maybe required.
         * @param trackingId
         * @param protocolVersion
         * @param clientId
         * @param hitType
         */
        public DataBuilder(String trackingId, String protocolVersion, String clientId, String hitType) {
            this.trackingId = trackingId;
            this.protocolVersion = protocolVersion;
            this.clientId = clientId;
            this.hitType = hitType;
        }

        /**
         * Use this method to retrieve a new GoogleAnalyticsData object.
         * @return GoogleAnalyticsData object
         */
        public GoogleAnalyticsData build() {
            return new GoogleAnalyticsData(this);
        }

        public DataBuilder setProtocolVersion(String protocolVersion) {
            this.protocolVersion = protocolVersion;
            return this;
        }

        public DataBuilder setTrackingId(String trackingId) {
            this.trackingId = trackingId;
            return this;
        }

        public DataBuilder setAnonymizeIP(int anonymizeIP) {
            this.anonymizeIP = anonymizeIP;
            return this;
        }

        public DataBuilder setQueueTime(int queueTime) {
            this.queueTime = queueTime;
            return this;
        }

        public DataBuilder setCacheBuster(String cacheBuster) {
            this.cacheBuster = cacheBuster;
            return this;
        }

        public DataBuilder setClientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public DataBuilder setUserId(String userId) {
            this.userId = userId;
            return this;
        }

        public DataBuilder setSessionControl(String sessionControl) {
            this.sessionControl = sessionControl;
            return this;
        }

        public DataBuilder setIPOverride(String IPOverride) {
            this.IPOverride = IPOverride;
            return this;
        }

        public DataBuilder setUserAgentOverride(String userAgentOverride) {
            this.userAgentOverride = userAgentOverride;
            return this;
        }

        public DataBuilder setReferrer(String referrer) {
            this.referrer = referrer;
            return this;
        }

        public DataBuilder setCampaignName(String campaignName) {
            this.campaignName = campaignName;
            return this;
        }

        public DataBuilder setCampaignSource(String campaignSource) {
            this.campaignSource = campaignSource;
            return this;
        }

        public DataBuilder setCampaignMedium(String campaignMedium) {
            this.campaignMedium = campaignMedium;
            return this;
        }

        public DataBuilder setCampaignKeyword(String campaignKeyword) {
            this.campaignKeyword = campaignKeyword;
            return this;
        }

        public DataBuilder setCampaignContent(String campaignContent) {
            this.campaignContent = campaignContent;
            return this;
        }

        public DataBuilder setCampaignId(String campaignId) {
            this.campaignId = campaignId;
            return this;
        }

        public DataBuilder setGoogleAdwordsId(String googleAdwordsId) {
            this.googleAdwordsId = googleAdwordsId;
            return this;
        }

        public DataBuilder setGoogleDisplayAdsId(String googleDisplayAdsId) {
            this.googleDisplayAdsId = googleDisplayAdsId;
            return this;
        }

        public DataBuilder setScreenResolutoin(String screenResolutoin) {
            this.screenResolutoin = screenResolutoin;
            return this;
        }

        public DataBuilder setViewPortSize(String viewPortSize) {
            this.viewPortSize = viewPortSize;
            return this;
        }

        public DataBuilder setDocumentEncoding(String documentEncoding) {
            this.documentEncoding = documentEncoding;
            return this;
        }

        public DataBuilder setScreenColors(String screenColors) {
            this.screenColors = screenColors;
            return this;
        }

        public DataBuilder setUserLanguage(String userLanguage) {
            this.userLanguage = userLanguage;
            return this;
        }

        public DataBuilder setJavaEnabled(String javaEnabled) {
            this.javaEnabled = javaEnabled;
            return this;
        }

        public DataBuilder setFlashVersion(String flashVersion) {
            this.flashVersion = flashVersion;
            return this;
        }

        public DataBuilder setHitType(String hitType) {
            this.hitType = hitType;
            return this;
        }

        public DataBuilder setNonInteractionHit(int nonInteractionHit) {
            this.nonInteractionHit = nonInteractionHit;
            return this;
        }

        public DataBuilder setDocumentLocationUrl(String documentLocationUrl) {
            this.documentLocationUrl = documentLocationUrl;
            return this;
        }

        public DataBuilder setDocumentHostName(String documentHostName) {
            this.documentHostName = documentHostName;
            return this;
        }

        public DataBuilder setDocumentPath(String documentPath) {
            this.documentPath = documentPath;
            return this;
        }

        public DataBuilder setDocumentTitle(String documentTitle) {
            this.documentTitle = documentTitle;
            return this;
        }

        public DataBuilder setScreenName(String screenName) {
            this.screenName = screenName;
            return this;
        }

        public DataBuilder setLinkId(String linkId) {
            this.linkId = linkId;
            return this;
        }

        public DataBuilder setAppName(String appName) {
            this.appName = appName;
            return this;
        }

        public DataBuilder setAppId(String appId) {
            this.appId = appId;
            return this;
        }

        public DataBuilder setAppVersion(String appVersion) {
            this.appVersion = appVersion;
            return this;
        }

        public DataBuilder setAppInstallerId(String appInstallerId) {
            this.appInstallerId = appInstallerId;
            return this;
        }

        public DataBuilder setEventCategory(String eventCategory) {
            this.eventCategory = eventCategory;
            return this;
        }

        public DataBuilder setEventAction(String eventAction) {
            this.eventAction = eventAction;
            return this;
        }

        public DataBuilder setEventLabel(String eventLabel) {
            this.eventLabel = eventLabel;
            return this;
        }

        public DataBuilder setEventValue(int eventValue) {
            this.eventValue = eventValue;
            return this;
        }

        public DataBuilder setTransactionId(String transactionId) {
            this.transactionId = transactionId;
            return this;
        }

        public DataBuilder setTransactionAffiliation(String transactionAffiliation) {
            this.transactionAffiliation = transactionAffiliation;
            return this;
        }

        public DataBuilder setTransactionRevenue(double transactionRevenue) {
            this.transactionRevenue = transactionRevenue;
            return this;
        }

        public DataBuilder setTransactionShipping(double transactionShipping) {
            this.transactionShipping = transactionShipping;
            return this;
        }

        public DataBuilder setTransactionTax(double transactionTax) {
            this.transactionTax = transactionTax;
            return this;
        }

        public DataBuilder setItemName(String itemName) {
            this.itemName = itemName;
            return this;
        }

        public DataBuilder setItemPrice(double itemPrice) {
            this.itemPrice = itemPrice;
            return this;
        }

        public DataBuilder setItemQty(int itemQty) {
            this.itemQty = itemQty;
            return this;
        }

        public DataBuilder setItemCode(String itemCode) {
            this.itemCode = itemCode;
            return this;
        }

        public DataBuilder setItemCategory(String itemCategory) {
            this.itemCategory = itemCategory;
            return this;
        }

        public DataBuilder setCurrencyCode(String currencyCode) {
            this.currencyCode = currencyCode;
            return this;
        }

        public DataBuilder setSocialNetwork(String socialNetwork) {
            this.socialNetwork = socialNetwork;
            return this;
        }

        public DataBuilder setSocialAction(String socialAction) {
            this.socialAction = socialAction;
            return this;
        }

        public DataBuilder setSocialActionTarget(String socialActionTarget) {
            this.socialActionTarget = socialActionTarget;
            return this;
        }

        public DataBuilder setUserTimingCategory(String userTimingCategory) {
            this.userTimingCategory = userTimingCategory;
            return this;
        }

        public DataBuilder setUserTimingVariableName(String userTimingVariableName) {
            this.userTimingVariableName = userTimingVariableName;
            return this;
        }

        public DataBuilder setUserTimingTime(int userTimingTime) {
            this.userTimingTime = userTimingTime;
            return this;
        }

        public DataBuilder setUserTimingLabel(String userTimingLabel) {
            this.userTimingLabel = userTimingLabel;
            return this;
        }

        public DataBuilder setPageLoadTime(int pageLoadTime) {
            this.pageLoadTime = pageLoadTime;
            return this;
        }

        public DataBuilder setDnsTime(int dnsTime) {
            this.dnsTime = dnsTime;
            return this;
        }

        public DataBuilder setPageDownloadTime(int pageDownloadTime) {
            this.pageDownloadTime = pageDownloadTime;
            return this;
        }

        public DataBuilder setRedirectResponseTime(int redirectResponseTime) {
            this.redirectResponseTime = redirectResponseTime;
            return this;
        }

        public DataBuilder setTcpConnectTime(int tcpConnectTime) {
            this.tcpConnectTime = tcpConnectTime;
            return this;
        }

        public DataBuilder setServerResponseTime(int serverResponseTime) {
            this.serverResponseTime = serverResponseTime;
            return this;
        }

        public DataBuilder setExceptionDescription(String exceptionDescription) {
            this.exceptionDescription = exceptionDescription;
            return this;
        }

        public DataBuilder setFatalException(int fatalException) {
            this.fatalException = fatalException;
            return this;
        }

        public DataBuilder setCustomDimension(String customDimension) {
            this.customDimension = customDimension;
            return this;

        }

        public DataBuilder setCustomMetric(String customMetric) {
            this.customMetric = customMetric;
            return this;
        }

        public DataBuilder setExperimentId(String experimentId) {
            this.experimentId = experimentId;
            return this;
        }

        public DataBuilder setExperimentVariant(String experimentVariant) {
            this.experimentVariant = experimentVariant;
            return this;
        }
    }
}

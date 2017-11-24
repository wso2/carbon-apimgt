/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.wso2.carbon.apimgt.core.configuration.models;

import org.wso2.carbon.kernel.annotations.Configuration;
import org.wso2.carbon.kernel.annotations.Element;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Class to hold APIM configuration parameters and generate yaml file
 */

@Configuration(namespace = "wso2.carbon.apimgt", description = "APIM Configuration Parameters")
public class APIMConfigurations {

    private boolean reverseProxyEnabled = false;
    @Element(description = "hostname")
    private String hostname = "localhost";
    @Element(description = "context for publisher")
    private String publisherContext = "/api/am/publisher/v1.0";
    @Element(description = "context for store")
    private String storeContext = "/api/am/store/v1.0";
    @Element(description = "context for admin")
    private String adminContext = "/api/am/admin/v1.0";

    @Element(description = "package name for gateway configs")
    private String gatewayPackageName = "org.wso2.carbon.apimgt.gateway";

    @Element(description = "package name path for gateway configs")
    private String gatewayPackageNamePath =
            "deployment" + File.separator + "org" + File.separator + "wso2" + File.separator + "apim";

    @Element(description = "label extractor")
    private String labelExtractorImplClass = "org.wso2.carbon.apimgt.core.impl.DefaultLabelExtractorImpl";

    @Element(description = "Key Manager Configurations")
    private KeyMgtConfigurations keyManagerConfigs = new KeyMgtConfigurations();

    @Element(description = "Identity Provider Configurations")
    private IdentityProviderConfigurations identityProviderConfigs = new IdentityProviderConfigurations();

    @Element(description = "Broker Configurations")
    private BrokerConfigurations brokerConfigurations = new BrokerConfigurations();

    @Element(description = "Notificaton Configurations")
    private NotificationConfigurations notificationConfigurations = new NotificationConfigurations();


    @Element(description = "JWT Configurations")
    private JWTConfigurations jwtConfigurations = new JWTConfigurations();

    @Element(description = "Analytics Configurations")
    private AnalyticsConfigurations analyticsConfigurations = new AnalyticsConfigurations();

    @Element(description = "Google Analytics Tracking Configurations")
    private GoogleAnalyticsConfigurations googleAnalyticsConfigurations = new GoogleAnalyticsConfigurations();

    @Element(description = "Throttling Configurations")
    private ThrottlingConfigurations throttlingConfigurations = new ThrottlingConfigurations();

    @Element(description = "comment moderator role")
    private String commentModeratorRole = "comment-moderator";

    @Element(description = "comment text max length")
    private int commentMaxLength = 1000;

    @Element(description = "rating upper limit")
    private int ratingMaxValue = 5;

    @Element(description = "WSDL processor implementations")
    private List<String> wsdlProcessors = Arrays.asList(
            "org.wso2.carbon.apimgt.core.impl.WSDL11ProcessorImpl",
            "org.wso2.carbon.apimgt.core.impl.WSDL20ProcessorImpl");

    @Element(description = "SDK Generation Language Configurations")
    private SdkLanguageConfigurations sdkLanguageConfigurations = new SdkLanguageConfigurations();

    public SdkLanguageConfigurations getSdkLanguageConfigurations() {
        return sdkLanguageConfigurations;
    }

    public void setSdkLanguageConfigurations(SdkLanguageConfigurations sdkLanguageConfigurations) {
        this.sdkLanguageConfigurations = sdkLanguageConfigurations;
    }

    public boolean isReverseProxyEnabled() {
        return reverseProxyEnabled;
    }

    public void setReverseProxyEnabled(boolean reverseProxyEnabled) {
        this.reverseProxyEnabled = reverseProxyEnabled;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getPublisherContext() {
        return publisherContext;
    }

    public void setPublisherContext(String publisherContext) {
        this.publisherContext = publisherContext;
    }

    public String getStoreContext() {
        return storeContext;
    }

    public void setStoreContext(String storeContext) {
        this.storeContext = storeContext;
    }

    public String getAdminContext() {
        return adminContext;
    }

    public void setAdminContext(String adminContext) {
        this.adminContext = adminContext;
    }

    public String getGatewayPackageName() {
        return gatewayPackageName;
    }

    public void setGatewayPackageName(String gatewayPackageName) {
        this.gatewayPackageName = gatewayPackageName;
    }

    public String getGatewayPackageNamePath() {
        return gatewayPackageNamePath;
    }

    public void setGatewayPackageNamePath(String gatewayPackageNamePath) {
        this.gatewayPackageNamePath = gatewayPackageNamePath;
    }

    public NotificationConfigurations getNotificationConfigurations() {
        return notificationConfigurations;
    }

    public void setNotificationConfigurations(NotificationConfigurations notificationConfigurations) {
        this.notificationConfigurations = notificationConfigurations;
    }

    public String getLabelExtractorImplClass() {
        return labelExtractorImplClass;
    }

    public void setLabelExtractorImplClass(String labelExtractorImplClass) {
        this.labelExtractorImplClass = labelExtractorImplClass;
    }

    public KeyMgtConfigurations getKeyManagerConfigs() {
        return keyManagerConfigs;
    }

    public void setKeyManagerConfigs(KeyMgtConfigurations keyManagerConfigs) {
        this.keyManagerConfigs = keyManagerConfigs;
    }

    public IdentityProviderConfigurations getIdentityProviderConfigs() {
        return identityProviderConfigs;
    }

    public void setIdentityProviderConfigs(IdentityProviderConfigurations identityProviderConfigs) {
        this.identityProviderConfigs = identityProviderConfigs;
    }

    public BrokerConfigurations getBrokerConfigurations() {
        return brokerConfigurations;
    }

    public void setBrokerConfigurations(BrokerConfigurations brokerConfigurations) {
        this.brokerConfigurations = brokerConfigurations;
    }

    public JWTConfigurations getJwtConfigurations() {
        return jwtConfigurations;
    }

    public void setJwtConfigurations(JWTConfigurations jwtConfigurations) {
        this.jwtConfigurations = jwtConfigurations;
    }

    public AnalyticsConfigurations getAnalyticsConfigurations() {
        return analyticsConfigurations;
    }

    public void setAnalyticsConfigurations(AnalyticsConfigurations analyticsConfigurations) {
        this.analyticsConfigurations = analyticsConfigurations;
    }

    public GoogleAnalyticsConfigurations getGoogleAnalyticsConfigurations() {
        return googleAnalyticsConfigurations;
    }

    public void setGoogleAnalyticsConfigurations(GoogleAnalyticsConfigurations googleAnalyticsConfigurations) {
        this.googleAnalyticsConfigurations = googleAnalyticsConfigurations;
    }

    public ThrottlingConfigurations getThrottlingConfigurations() {
        return throttlingConfigurations;
    }

    public void setThrottlingConfigurations(ThrottlingConfigurations throttlingConfigurations) {
        this.throttlingConfigurations = throttlingConfigurations;
    }

    public String getCommentModeratorRole() {
        return commentModeratorRole;
    }

    public void setCommentModeratorRole(String commentModeratorRole) {
        this.commentModeratorRole = commentModeratorRole;
    }


    public int getCommentMaxLength() {
        return commentMaxLength;
    }

    public void setCommentMaxLength(int commentMaxLength) {
        this.commentMaxLength = commentMaxLength;
    }

    public int getRatingMaxValue() {
        return ratingMaxValue;
    }

    public void setRatingMaxValue(int ratingMaxValue) {
        this.ratingMaxValue = ratingMaxValue;
    }

    public List<String> getWsdlProcessors() {
        return wsdlProcessors;
    }

    public void setWsdlProcessors(List<String> wsdlProcessors) {
        this.wsdlProcessors = wsdlProcessors;
    }
}

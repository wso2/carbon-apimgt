/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.impl;

import org.apache.axis2.util.JavaUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.util.Map;

public class APIManagerAnalyticsConfiguration {
    private static final Log log = LogFactory.getLog(APIManagerAnalyticsConfiguration.class);
    private String bamServerUrlGroups;
    private String bamServerUser;
    private String bamServerPassword;
    private String dasServerUrl;
    private String dasServerUser;
    private String dasServerPassword;

    private boolean analyticsEnabled;
    private boolean skipEventReceiverConnection;
    private boolean buildMsg;
    private String publisherClass;
    private String requestStreamName;
    private String requestStreamVersion;
    private String responseStreamName;
    private String responseStreamVersion;
    private String faultStreamName;
    private String faultStreamVersion;
    private String throttleStreamName;
    private String throttleStreamVersion;
    private String executionTimeStreamName;
    private String executionTimeStreamVersion;
    private String alertTypeStreamName;
    private String alertTypeStreamVersion;

    private APIManagerAnalyticsConfiguration() {
    }
    private static class APIManagerAnalyticsConfigurationHolder {
        private static final APIManagerAnalyticsConfiguration INSTANCE = new APIManagerAnalyticsConfiguration();

        private APIManagerAnalyticsConfigurationHolder(){}
    }
    public static synchronized APIManagerAnalyticsConfiguration getInstance() {
        return APIManagerAnalyticsConfigurationHolder.INSTANCE;
    }

    public void setAPIManagerConfiguration(APIManagerConfiguration config){
        String usageEnabled = config.getFirstProperty(APIConstants.API_USAGE_ENABLED);
        analyticsEnabled = JavaUtils.isTrueExplicitly(usageEnabled);
        if (analyticsEnabled){
            String skipEventReceiverConnStr = config.getFirstProperty(APIConstants.API_USAGE_SKIP_EVENT_RECEIVER_CONN);
            skipEventReceiverConnection = skipEventReceiverConnStr != null && JavaUtils.isTrueExplicitly
                    (skipEventReceiverConnStr);
            publisherClass = config.getFirstProperty(APIConstants.API_USAGE_PUBLISHER_CLASS);
            requestStreamName = config.getFirstProperty(APIConstants.API_REQUEST_STREAM_NAME);
            requestStreamVersion = config.getFirstProperty(APIConstants.API_REQUEST_STREAM_VERSION);
            if (requestStreamName == null || requestStreamVersion == null) {
                log.error("Request stream name or version is null. Check api-manager.xml");
            }
            responseStreamName = config.getFirstProperty(APIConstants.API_RESPONSE_STREAM_NAME);
            responseStreamVersion = config.getFirstProperty(APIConstants.API_RESPONSE_STREAM_VERSION);
            if (responseStreamName == null || responseStreamVersion == null) {
                log.error("Response stream name or version is null. Check api-manager.xml");
            }
            faultStreamName = config.getFirstProperty(APIConstants.API_FAULT_STREAM_NAME);
            faultStreamVersion = config.getFirstProperty(APIConstants.API_FAULT_STREAM_VERSION);
            if (faultStreamName == null || faultStreamVersion == null) {
                log.error("Fault stream name or version is null. Check api-manager.xml");
            }
            throttleStreamName = config.getFirstProperty(APIConstants.API_THROTTLE_STREAM_NAME);
            throttleStreamVersion = config.getFirstProperty(APIConstants.API_THRORRLE_STREAM_VERSION);
            if (throttleStreamName == null || throttleStreamVersion == null) {
                log.error("Throttle stream name or version is null. Check api-manager.xml");
            }
            executionTimeStreamName = config.getFirstProperty(APIConstants.API_EXECUTION_TIME_STREAM_NAME);
            executionTimeStreamVersion = config.getFirstProperty(APIConstants.API_EXECUTION_TIME_STREAM_VERSION);
            if (executionTimeStreamName == null || executionTimeStreamVersion == null) {
                log.error("Execution Time stream name or version is null. Check api-manager.xml");
            }

            alertTypeStreamName = config.getFirstProperty(APIConstants.API_ALERT_TYPES_STREAM_NAME);
            alertTypeStreamVersion = config.getFirstProperty(APIConstants.API_ALERT_TYPES_STREAM_VERSION);
            if (alertTypeStreamName == null || alertTypeStreamVersion == null) {
                log.error("Execution Time stream name or version is null. Check api-manager.xml");
            }

            bamServerUrlGroups = config.getFirstProperty(APIConstants.API_USAGE_BAM_SERVER_URL_GROUPS);
            bamServerUser = config.getFirstProperty(APIConstants.API_USAGE_BAM_SERVER_USER);
            bamServerPassword = config.getFirstProperty(APIConstants.API_USAGE_BAM_SERVER_PASSWORD);

            dasServerUrl = config.getFirstProperty(APIConstants.API_USAGE_DAS_REST_API_URL);
            dasServerUser = config.getFirstProperty(APIConstants.API_USAGE_DAS_REST_API_USER);
            dasServerPassword = config.getFirstProperty(APIConstants.API_USAGE_DAS_REST_API_PASSWORD);
            String build = config.getFirstProperty(APIConstants.API_USAGE_BUILD_MSG);
            buildMsg = build != null && JavaUtils.isTrueExplicitly(build);
        }
    }

    public String getBamServerPassword() {
        return bamServerPassword;
    }

    public String getBamServerUser() {
        return bamServerUser;
    }

    public String getBamServerUrlGroups() {
        return bamServerUrlGroups;
    }

    public boolean isAnalyticsEnabled() {
        return analyticsEnabled;
    }

    public boolean isSkipEventReceiverConnection() {
        return skipEventReceiverConnection;
    }

    public String getPublisherClass() {
        return publisherClass;
    }

    public String getRequestStreamName() {
        return requestStreamName;
    }

    public String getRequestStreamVersion() {
        return requestStreamVersion;
    }

    public String getResponseStreamName() {
        return responseStreamName;
    }

    public String getResponseStreamVersion() {
        return responseStreamVersion;
    }

    public String getFaultStreamName() {
        return faultStreamName;
    }

    public String getFaultStreamVersion() {
        return faultStreamVersion;
    }

    public String getAlertTypeStreamName() {
        return alertTypeStreamName;
    }

    public String getAlertTypeStreamVersion() {
        return alertTypeStreamVersion;
    }

    public String getThrottleStreamName() {
        return throttleStreamName;
    }

    public String getThrottleStreamVersion() {
        return throttleStreamVersion;
    }

    public void setBamServerUrlGroups(String bamServerUrlGroups) {
        this.bamServerUrlGroups = bamServerUrlGroups;
    }

    public void setBamServerUser(String bamServerUser) {
        this.bamServerUser = bamServerUser;
    }

    public void setBamServerPassword(String bamServerPassword) {
        this.bamServerPassword = bamServerPassword;
    }

    public void setAnalyticsEnabled(boolean analyticsEnabled) {
        this.analyticsEnabled = analyticsEnabled;
    }

    public boolean isBuildMsg() {
        return buildMsg;
    }

    public String getDasServerUrl() {
        return dasServerUrl;
    }

    public void setDasServerUrl(String dasServerUrl) {
        this.dasServerUrl = dasServerUrl;
    }

    public String getDasServerUser() {
        return dasServerUser;
    }

    public void setDasServerUser(String dasServerUser) {
        this.dasServerUser = dasServerUser;
    }

    public String getDasServerPassword() {
        return dasServerPassword;
    }

    public void setDasServerPassword(String dasServerPassword) {
        this.dasServerPassword = dasServerPassword;
    }

    public String getExecutionTimeStreamVersion() {
        return executionTimeStreamVersion;
    }

    public String getExecutionTimeStreamName() {
        return executionTimeStreamName;
    }
}

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

import java.util.Map;

public class APIManagerAnalyticsConfiguration {
    private static final Log log = LogFactory.getLog(APIManagerAnalyticsConfiguration.class);
    private String dasReceiverUrlGroups;
    private String dasReceiverAuthUrlGroups;
    private String dasReceiverServerUser;
    private String dasReceiverServerPassword;
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
    private boolean skipWorkFlowEventReceiverConnection;
    private String datacenterId;
    private String botDataStreamName;
    private String botDataStreamVersion;
    private String reporterClass;
    private String responseSchemaName;
    private String faultSchemaName;
    private Map<String, String> reporterProperties;

    private APIManagerAnalyticsConfiguration() {
    }
    private static class APIManagerAnalyticsConfigurationHolder {
        private static final APIManagerAnalyticsConfiguration INSTANCE = new APIManagerAnalyticsConfiguration();

        private APIManagerAnalyticsConfigurationHolder(){}
    }
    public static APIManagerAnalyticsConfiguration getInstance() {
        return APIManagerAnalyticsConfigurationHolder.INSTANCE;
    }

    public void setAPIManagerConfiguration(APIManagerConfiguration config){
        String usageEnabled = config.getFirstProperty(APIConstants.API_USAGE_ENABLED);
        this.analyticsEnabled = JavaUtils.isTrueExplicitly(usageEnabled);
        if (this.analyticsEnabled) {
            this.reporterClass = config.getFirstProperty(APIConstants.API_ANALYTICS_REPORTER_CLASS);
            this.responseSchemaName = config.getFirstProperty(APIConstants.API_ANALYTICS_RESPONSE_SCHEMA_NAME);
            this.faultSchemaName = config.getFirstProperty(APIConstants.API_ANALYTICS_FAULT_SCHEMA_NAME);
            this.reporterProperties = config.getAnalyticsProperties();
        }
    }

    public String getDasReceiverServerPassword() {
        return dasReceiverServerPassword;
    }

    public String getDasReceiverServerUser() {
        return dasReceiverServerUser;
    }

    public String getDasReceiverUrlGroups() {
        return dasReceiverUrlGroups;
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

    public String getBotDataStreamName(){
        return botDataStreamName;
    }

    public String getBotStreamVersion(){
        return botDataStreamVersion;
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

    public void setDasReceiverUrlGroups(String dasReceiverUrlGroups) {
        this.dasReceiverUrlGroups = dasReceiverUrlGroups;
    }

    public void setDasReceiverServerUser(String dasReceiverServerUser) {
        this.dasReceiverServerUser = dasReceiverServerUser;
    }

    public void setDasReceiverServerPassword(String dasReceiverServerPassword) {
        this.dasReceiverServerPassword = dasReceiverServerPassword;
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

    public String getDasReceiverAuthUrlGroups() {
        return dasReceiverAuthUrlGroups;
    }

    public void setDasReceiverAuthUrlGroups(String dasReceiverAuthUrlGroups) {
        this.dasReceiverAuthUrlGroups = dasReceiverAuthUrlGroups;
    }

    public boolean isSkipWorkFlowEventReceiverConnection() {
        return skipWorkFlowEventReceiverConnection;
    }

    public String getDatacenterId() {
        return datacenterId;
    }

    public void setDatacenterId(String datacenterId) {
        this.datacenterId = datacenterId;
    }

    public String getReporterClass() {
        return reporterClass;
    }

    public void setReporterClass(String reporterClass) {
        this.reporterClass = reporterClass;
    }

    public Map<String, String> getReporterProperties() {
        return reporterProperties;
    }

    public void setReporterProperties(Map<String, String> reporterProperties) {
        this.reporterProperties = reporterProperties;
    }

    public String getResponseSchemaName() {
        return responseSchemaName;
    }

    public String getFaultSchemaName() {
        return faultSchemaName;
    }
}

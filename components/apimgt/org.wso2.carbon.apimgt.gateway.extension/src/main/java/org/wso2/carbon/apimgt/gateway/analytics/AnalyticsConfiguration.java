package org.wso2.carbon.apimgt.gateway.analytics;
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

/**
 * This class is used to keep the Analytics related configurations
 */
public class AnalyticsConfiguration {
    private boolean enabled = true;
    private boolean skipEventReceiverConnection = false;

    private String analyzerUrl = "{tcp://localhost:7612}";
    private String analyzerUser = "admin";
    private String analyzerPass = "admin";

    private String eventPublisher = "org.wso2.carbon.apimgt.gateway.analytics.EventPublisherImpl";
    private String analyticsClient = "";

    private String analyticsEventStreamName = "org.wso2.carbon.apim.event";
    private String analyticsEventStreamVersion = "2.0.0";

    private String workflowEventStreamName = "org.wso2.carbon.apim.workflow";
    private String workflowEventStreamVersion = "2.0.0";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isSkipEventReceiverConnection() {
        return skipEventReceiverConnection;
    }

    public void setSkipEventReceiverConnection(boolean skipEventReceiverConnection) {
        this.skipEventReceiverConnection = skipEventReceiverConnection;
    }

    public String getAnalyzerUrl() {
        return analyzerUrl;
    }

    public void setAnalyzerUrl(String analyzerUrl) {
        this.analyzerUrl = analyzerUrl;
    }

    public String getAnalyzerUser() {
        return analyzerUser;
    }

    public void setAnalyzerUser(String analyzerUser) {
        this.analyzerUser = analyzerUser;
    }

    public String getAnalyzerPass() {
        return analyzerPass;
    }

    public void setAnalyzerPass(String analyzerPass) {
        this.analyzerPass = analyzerPass;
    }

    public String getEventPublisher() {
        return eventPublisher;
    }

    public void setEventPublisher(String eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public String getAnalyticsClient() {
        return analyticsClient;
    }

    public void setAnalyticsClient(String analyticsClient) {
        this.analyticsClient = analyticsClient;
    }

    public String getAnalyticsEventStreamName() {
        return analyticsEventStreamName;
    }

    public void setAnalyticsEventStreamName(String analyticsEventStreamName) {
        this.analyticsEventStreamName = analyticsEventStreamName;
    }

    public String getAnalyticsEventStreamVersion() {
        return analyticsEventStreamVersion;
    }

    public void setAnalyticsEventStreamVersion(String analyticsEventStreamVersion) {
        this.analyticsEventStreamVersion = analyticsEventStreamVersion;
    }

    public String getWorkflowEventStreamName() {
        return workflowEventStreamName;
    }

    public void setWorkflowEventStreamName(String workflowEventStreamName) {
        this.workflowEventStreamName = workflowEventStreamName;
    }

    public String getWorkflowEventStreamVersion() {
        return workflowEventStreamVersion;
    }

    public void setWorkflowEventStreamVersion(String workflowEventStreamVersion) {
        this.workflowEventStreamVersion = workflowEventStreamVersion;
    }
}

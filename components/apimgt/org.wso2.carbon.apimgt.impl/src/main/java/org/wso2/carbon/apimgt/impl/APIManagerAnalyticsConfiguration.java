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
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.util.Map;

public class APIManagerAnalyticsConfiguration {

    private String bamServerUrlGroups;
    private String bamServerUser;
    private String bamServerPassword;
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
    public static APIManagerAnalyticsConfiguration instance = new APIManagerAnalyticsConfiguration();
	private static Log log = LogFactory.getLog(APIManagerAnalyticsConfiguration.class);

    private APIManagerAnalyticsConfiguration() {
        analyticsEnabled = APIUtil.isAnalyticsEnabled();
        if(analyticsEnabled) {
            Map<String, String> analyticsConfigs = APIUtil.getAnalyticsConfigFromRegistry();
            bamServerUrlGroups = analyticsConfigs.get(APIConstants.API_USAGE_BAM_SERVER_URL_GROUPS);
            bamServerUser = analyticsConfigs.get(APIConstants.API_USAGE_BAM_SERVER_USER);
            bamServerPassword = analyticsConfigs.get(APIConstants.API_USAGE_BAM_SERVER_PASSWORD);
        }
    }

    public static synchronized APIManagerAnalyticsConfiguration getInstance(){
        if(instance == null){
            instance = new APIManagerAnalyticsConfiguration();
        }
        return instance;
    }
    public static synchronized APIManagerAnalyticsConfiguration createNewInstance(){
            log.debug("Writing Analytics Configuration to Registry...");
            APIUtil.writeAnalyticsConfigurationToRegistry(ServiceReferenceHolder.getInstance()
                                                                  .getAPIManagerConfigurationService().getAPIManagerConfiguration());
            instance = new APIManagerAnalyticsConfiguration();
        return instance;
    }
    public void setAPIManagerConfiguration(APIManagerConfiguration config){
        String skipEventReceiverConnStr = config.getFirstProperty(APIConstants.API_USAGE_SKIP_EVENT_RECEIVER_CONN);
        skipEventReceiverConnection = skipEventReceiverConnStr != null &&
                JavaUtils.isTrueExplicitly(skipEventReceiverConnStr);
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
        String build = config.getFirstProperty(APIConstants.API_USAGE_BUILD_MSG);
        buildMsg = build != null && JavaUtils.isTrueExplicitly(build);
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
}

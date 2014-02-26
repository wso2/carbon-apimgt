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
package org.wso2.carbon.apimgt.usage.publisher.service;

import org.apache.axis2.util.JavaUtils;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.usage.publisher.APIMgtUsagePublisherConstants;

public class APIMGTConfigReaderService {

    private String bamServerThriftPort;
    private String bamServerURL;
    private String bamServerUser;
    private String bamServerPassword;
    private boolean enabled;
    private String publisherClass;
    private boolean googleAnalyticsTrackingEnabled;
    private String googleAnalyticsTrackingID;

    public APIMGTConfigReaderService(APIManagerConfiguration config) {
        String enabledStr = config.getFirstProperty(APIMgtUsagePublisherConstants.API_USAGE_ENABLED);
        enabled = enabledStr != null && JavaUtils.isTrueExplicitly(enabledStr);
        bamServerThriftPort = config.getFirstProperty(APIMgtUsagePublisherConstants.API_USAGE_THRIFT_PORT);
        bamServerURL = config.getFirstProperty(APIMgtUsagePublisherConstants.API_USAGE_BAM_SERVER_URL);
        bamServerUser = config.getFirstProperty(APIMgtUsagePublisherConstants.API_USAGE_BAM_SERVER_USER);
        bamServerPassword = config.getFirstProperty(APIMgtUsagePublisherConstants.API_USAGE_BAM_SERVER_PASSWORD);
        publisherClass = config.getFirstProperty(APIMgtUsagePublisherConstants.API_USAGE_PUBLISHER_CLASS);
        String googleAnalyticsEnabledStr = config.getFirstProperty(APIMgtUsagePublisherConstants.API_GOOGLE_ANALYTICS_TRACKING_ENABLED);
        googleAnalyticsTrackingEnabled = googleAnalyticsEnabledStr != null && JavaUtils.isTrueExplicitly(googleAnalyticsEnabledStr);
        googleAnalyticsTrackingID = config.getFirstProperty(APIMgtUsagePublisherConstants.API_GOOGLE_ANALYTICS_TRACKING_ID);
    }

    public String getBamServerThriftPort() {
        return bamServerThriftPort;
    }

    public String getBamServerPassword() {
        return bamServerPassword;
    }

    public String getBamServerUser() {
        return bamServerUser;
    }

    public String getBamServerURL() {
        return bamServerURL;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getPublisherClass() {
        return publisherClass;
    }
     
    public String getGoogleAnalyticsTrackingID() {
 		return googleAnalyticsTrackingID;
 	}
    
    public boolean isGoogleAnalyticsTrackingEnabled() {
    	return googleAnalyticsTrackingEnabled;
    }

}

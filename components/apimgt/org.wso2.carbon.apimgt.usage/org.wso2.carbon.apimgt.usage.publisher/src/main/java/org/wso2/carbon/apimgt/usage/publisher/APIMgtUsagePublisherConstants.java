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
package org.wso2.carbon.apimgt.usage.publisher;

public final class APIMgtUsagePublisherConstants {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String _OAUTH_HEADERS_SPLITTER = ",";
    public static final String _OAUTH_CONSUMER_KEY = "Bearer";
    public static final String HEADER_SEGMENT_DELIMITER = " ";
    public static final String  AXIS2_MC_HTTP_METHOD = "HTTP_METHOD";

    public static final String API_USAGE_TRACKING = "APIUsageTracking.";
    public static final String API_USAGE_ENABLED = API_USAGE_TRACKING + "Enabled";
    public static final String API_USAGE_THRIFT_PORT = API_USAGE_TRACKING + "ThriftPort";
    public static final String API_USAGE_BAM_SERVER_URL = API_USAGE_TRACKING + "BAMServerURL";
    public static final String API_USAGE_BAM_SERVER_USER = API_USAGE_TRACKING + "BAMUsername";
    public static final String API_USAGE_BAM_SERVER_PASSWORD = API_USAGE_TRACKING + "BAMPassword";
    public static final String API_USAGE_PUBLISHER_CLASS = API_USAGE_TRACKING + "PublisherClass";

    public static final String CONSUMER_KEY = "api.ut.consumerKey";
    public static final String USER_ID = "api.ut.userId";
    public static final String CONTEXT = "api.ut.context";
    public static final String API_VERSION = "api.ut.api_version";
    public static final String API = "api.ut.api";
    public static final String VERSION = "api.ut.version";
    public static final String REQUEST_TIME = "api.ut.requestTime";
    public static final String RESOURCE = "api.ut.resource";
    public static final String HTTP_METHOD = "api.ut.HTTP_METHOD";
    public static final String HOST_NAME = "api.ut.hostName";
    public static final String API_PUBLISHER = "api.ut.apiPublisher";
    public static final String APPLICATION_NAME = "api.ut.application.name";
    public static final String APPLICATION_ID = "api.ut.application.id";

    public static final String  API_MANAGER_REQUEST_STREAM_NAME = "org.wso2.apimgt.statistics.request";
    public static final String  API_MANAGER_REQUEST_STREAM_VERSION = "1.0.0";
    public static final String  API_MANAGER_RESPONSE_STREAM_NAME = "org.wso2.apimgt.statistics.response";
    public static final String  API_MANAGER_RESPONSE_STREAM_VERSION = "1.0.0";
    public static final String  API_MANAGER_FAULT_STREAM_NAME = "org.wso2.apimgt.statistics.fault";
    public static final String  API_MANAGER_FAULT_STREAM_VERSION = "1.0.0";

    public static final String API_GOOGLE_ANALYTICS_TRACKING = API_USAGE_TRACKING + "GoogleAnalyticsTracking.";
    public static final String API_GOOGLE_ANALYTICS_TRACKING_ENABLED = API_GOOGLE_ANALYTICS_TRACKING + "Enabled";
    public static final String API_GOOGLE_ANALYTICS_TRACKING_ID = API_GOOGLE_ANALYTICS_TRACKING + "TrackingID";
        
}

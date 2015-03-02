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
    public static final String REQUEST_START_TIME = "api.ut.requestTime";
    public static final String BACKEND_REQUEST_START_TIME = "api.ut.backendRequestTime";
    public static final String BACKEND_REQUEST_END_TIME = "api.ut.backendRequestEndTime";
    public static final String REQUEST_END_TIME = "api.ut.requestEndTime";


    public static final String API_GOOGLE_ANALYTICS_TRACKING_ENABLED = "Enabled";
    public static final String API_GOOGLE_ANALYTICS_TRACKING_ID = "TrackingID";


	public static final String API_USAGE_PUBLISHING = "APIUsageTracking.";
	public static final String API_USAGE_STREAMS = API_USAGE_PUBLISHING + "Streams.";
	public static final String API_USAGE_REQUEST_STREAM = API_USAGE_STREAMS + "Request.";
	public static final String API_USAGE_RESPONSE_STREAM = API_USAGE_STREAMS + "Response.";
	public static final String API_USAGE_FAULT_STREAM = API_USAGE_STREAMS + "Fault.";
    public static final String API_USAGE_THROTTLE_STREAM = API_USAGE_STREAMS + "Throttle.";
	public static final String API_REQUEST_STREAM_NAME = API_USAGE_REQUEST_STREAM + "Name";
	public static final String API_REQUEST_STREAM_VERSION = API_USAGE_REQUEST_STREAM + "Version";
	public static final String API_RESPONSE_STREAM_NAME = API_USAGE_RESPONSE_STREAM + "Name";
	public static final String API_RESPONSE_STREAM_VERSION = API_USAGE_RESPONSE_STREAM + "Version";
	public static final String API_FAULT_STREAM_NAME = API_USAGE_FAULT_STREAM + "Name";
	public static final String API_FAULT_STREAM_VERSION = API_USAGE_FAULT_STREAM + "Version";
    public static final String API_THROTTLE_STREAM_NAME = API_USAGE_THROTTLE_STREAM + "Name";
	public static final String API_THRORRLE_STREAM_VERSION = API_USAGE_THROTTLE_STREAM + "Version";


}

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
package org.wso2.carbon.apimgt.usage.client;

import org.wso2.carbon.apimgt.impl.APIConstants;

public final class APIUsageStatisticsClientConstants {

    public static final String API_LAST_ACCESS_TIME_SUMMARY = "API_LAST_ACCESS_TIME_SUMMARY";

    public static final String API_REQUEST_SUMMARY = "API_REQUEST_SUMMARY";

    public static final String API_THROTTLED_OUT_SUMMARY = "API_THROTTLED_OUT_SUMMARY";

    public static final String CONSUMERKEY = "consumerKey";

    public static final String RESOURCE = "resourcepath";

    public static final String API_VERSION_SERVICE_TIME_SUMMARY = "API_RESPONSE_SUMMARY";

    public static final String API_VERSION_SERVICE_TIME_SUMMARY_INDEX = "APIVersionServiceTimeSummaryIndex";

    public static final String API_Resource_Path_USAGE_SUMMARY = "API_Resource_USAGE_SUMMARY";

    public static final String API_VERSION_USAGE_SUMMARY = "API_VERSION_USAGE_SUMMARY";

    public static final String API_VERSION_USAGE_SUMMARY_INDEX = "APIVersionUsageSummaryIndex";

    public static final String API_VERSION_KEY_USAGE_SUMMARY = "APIVersionKeyUsageSummary";

    public static final String API_VERSION_KEY_USAGE_SUMMARY_INDEX = "APIVersionKeyUsageSummaryIndex";

    public static final String API_VERSION_KEY_LAST_ACCESS_SUMMARY = "API_REQUEST_SUMMARY";

    public static final String API_VERSION_KEY_LAST_ACCESS_SUMMARY_INDEX = "APIVersionKeyLastAccessSummaryIndex";

    public static final String KEY_USAGE_SUMMARY = "API_REQUEST_SUMMARY";

    public static final String KEY_USAGE_MONTH_SUMMARY = "API_REQUEST_MONTHLY_SUMMARY";

    public static final String MONTH= "month";

    public static final String  API_FAULT_SUMMARY = "API_FAULT_SUMMARY";

	public static final String  API_REQUEST_TIME_FAULT_SUMMARY = "API_REQUEST_TIME_FAULT_SUMMARY";

    public static final String KEY_USAGE_SUMMARY_INDEX = "KeyUsageSummaryIndex";

    public static final String ROWS = "rows";

    public static final String ROW = "row";

    public static final String TOTAL_REQUEST_COUNT = "total_request_count";

    public static final String API = "api";

    public static final String API_VERSION = "api_version";

    public static final String SERVICE_TIME = "serviceTime";

    public static final String VERSION = "version";

    public static final String METHOD= "method";
    
    public static final String CONTEXT= "context";
    
    public static final String TOTAL_RESPONSE_COUNT = "total_response_count";

	public static final String INVOCATION_TIME = "requesttime";

    public static final String TOTAL_FAULT_COUNT = "total_fault_count";

    public static final String REQUEST_TIME = "max_request_time";

    public static final String CONSUMER_KEY = "CONSUMERKEY";
    
    public static final String USER_ID = "userid";

    public static final String API_PUBLISHER = "apiPublisher";

    public static final String API_PUBLISHER_THROTTLE_TABLE = "apiPublisher";

    public static final String YEAR= "year";

    public static final String DAY= "day";

    public static final String TIME= "time";
    
    public static final String DESTINATION= "destination";

    public static final int DEFAULT_RESULTS_LIMIT = 10;
    
    public static final String ALL_PROVIDERS = "__all_providers__";

    public static final String API_USAGEBY_DESTINATION_SUMMARY = "API_DESTINATION_SUMMARY";

    public static final String APPLICATION_NAME = "applicationName";

    public static final String SUCCESS_REQUEST_COUNT = "success_request_count";

    public static final String THROTTLED_OUT_COUNT = "throttleout_count";

    public static final String FOR_ALL_API_VERSIONS = "FOR_ALL_API_VERSIONS";

    public static final String GROUP_BY_HOUR = "hour";

    public static final String GROUP_BY_DAY = "day";

    public static final String GROUP_BY_WEEK = "week";

    public static final String HOST_NAME = "hostName";
    public static final String MAX_REQUEST_TIME = "max_request_time";

    public static final String APPLICATION_JSON = "application/json";
    public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
    public static final String HTTP_AUTH_HEADER_NAME = "Authorization";
    public static final String HTTP_AUTH_HEADER_TYPE = "Basic";

    public static final String DAS_SEARCH_REST_API_URL = "/analytics/search";
    public static final String DAS_SEARCH_COUNT_REST_API_URL = "/analytics/search_count";
    public static final String DAS_AGGREGATES_SEARCH_REST_API_URL = "/analytics/aggregates";
    public static final String DAS_TABLE_EXIST_REST_API_URL = "/analytics/table_exists";

    public static final String DAS_TABLE_API_UTIL = "API_UTIL";

    public static final String KEY_API_FACET = "key_api_facet";
    public static final String KEY_USERID_FACET = "key_userId_facet";
    public static final String KEY_API_METHOD_PATH_FACET = "key_api_method_path_facet";
    public static final String CONSUMERKEY_API_FACET = "consumerKey_api_facet";
    public static final String API_VERSION_USERID_APIPUBLISHER_FACET = "api_version_userId_apiPublisher_facet";
    public static final String API_VERSION_CONTEXT_FACET = "api_version_context_facet";
    public static final String API_VERSION_USERID_CONTEXT_FACET = "api_version_userId_context_facet";
    public static final String API_VERSION_CONTEXT_METHOD_FACET = "api_version_context_method_facet";
    public static final String API_VERSION_CONTEXT_DEST_FACET = "api_version_context_dest_facet";
    public static final String API_VERSION_APIPUBLISHER_CONTEXT_FACET = "api_version_apiPublisher_context_facet";
    public static final String API_YEAR_MONTH_WEEK_DAY_FACET = "api_year_month_week_day_facet";
    public static final String APPLICATIONNAME_FACET = "applicationName_facet";

    public static final String AGGREGATE_SUM = "SUM";
    public static final String AGGREGATE_MAX = "MAX";

    public static final String ALIAS_COUNT = "count";
    public static final String ALIAS_TOTAL_SERVICE_TIME = "totalServiceTime";
    public static final String ALIAS_TOTAL_RESPONSE_COUNT = "totalResponseCount";
    public static final String ALIAS_LAST_ACCESS_TIME = "lastAccessTime";
    public static final String ALIAS_TOTAL_REQUEST_COUNT = "totalRequestCount";
    public static final String ALIAS_TOTAL_FAULT_COUNT = "totalFaultCount";
    public static final String ALIAS_SUCCESS_REQUEST_COUNT = "success_request_count";
    public static final String ALIAS_THROTTLE_OUT_COUNT = "throttle_out_count";

    public static final String TENANT_DOMAIN = "tenantDomain";
    public static final String API_EXECUTION_TME_DAY_SUMMARY = "API_EXE_TME_DAY_SUMMARY";
    public static final String API_EXECUTION_TIME_HOUR_SUMMARY = "API_EXE_TIME_HOUR_SUMMARY";
    public static final String API_EXECUTION_TIME_MINUTE_SUMMARY = "API_EXE_TIME_MIN_SUMMARY";
    public static final String API_EXECUTION_TIME_SECONDS_SUMMARY = "API_EXE_TIME_SEC_SUMMARY";

    public static final String RDBMS_STATISTICS_CLIENT_TYPE = "RDBMS";
    public static final String REST_STATISTICS_CLIENT_TYPE = "REST";

    public static final String HOUR = "hour";
    public static final String MINUTES = "minutes";
    public static final String SECONDS = "seconds";
    public static final String MEDIATION = "mediationName";
    public static final String EXECUTION_TIME = "executionTime";
    public static final String COUNTRY_CITY_FACET = "key_country_city_facet";
    public static final String API_REQUEST_GEO_LOCATION_SUMMARY = "API_REQ_GEO_LOC_SUMMARY";
    public static final String API_REQUEST_USER_BROWSER_SUMMARY = "API_REQ_USER_BROW_SUMMARY";
    public static String OS_BROWSER_FACET = "key_os_browser_facet";

    public static final String API_RESPONSE_TIME = "apiResponseTime";
    public static final String SECURITY_LATENCY = "securityLatency";
    public static final String THROTTLING_LATENCY = "throttlingLatency";
    public static final String REQ_MEDIATION_LATENCY = "requestMediationLatency";
    public static final String RES_MEDIATION_LATENCY = "responseMediationLatency";
    public static final String BACKEND_LATENCY = "backendLatency";
    public static final String OTHER_LATENCY = "otherLatency";
}

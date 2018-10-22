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

public final class APIUsageStatisticsClientConstants {

    public static final String API_LAST_ACCESS_TIME_SUMMARY = "API_LAST_ACCESS_TIME_SUMMARY";

    public static final String API_REQUEST_SUMMARY = "API_REQUEST_SUMMARY";

    public static final String API_THROTTLED_OUT_SUMMARY = "API_THROTTLED_OUT_SUMMARY";

    public static final String CONSUMERKEY = "consumerKey";

    public static final String RESOURCE = "resourcepath";

    public static final String API_VERSION_SERVICE_TIME_SUMMARY = "API_RESPONSE_SUMMARY";

    public static final String API_Resource_Path_USAGE_SUMMARY = "API_Resource_USAGE_SUMMARY";

    public static final String API_VERSION_USAGE_SUMMARY = "API_VERSION_USAGE_SUMMARY";

    public static final String API_VERSION_KEY_LAST_ACCESS_SUMMARY = "API_REQUEST_SUMMARY";

    public static final String KEY_USAGE_SUMMARY = "API_REQUEST_SUMMARY";

    public static final String MONTH= "month";

    public static final String  API_FAULT_SUMMARY = "API_FAULT_SUMMARY";

    public static final String API = "api";

    public static final String VERSION = "version";

    public static final String METHOD= "method";
    
    public static final String CONTEXT= "context";
    
    public static final String TOTAL_RESPONSE_COUNT = "total_response_count";

    public static final String REQUEST_TIME = "max_request_time";

    public static final String USER_ID = "userid";

    public static final String API_PUBLISHER = "apiPublisher";

    public static final String API_PUBLISHER_THROTTLE_TABLE = "apiPublisher";

    public static final String YEAR= "year";

    public static final String DAY= "day";

    public static final String TIME= "time";

    public static final String ALL_PROVIDERS = "__all_providers__";

    public static final String API_USAGEBY_DESTINATION_SUMMARY = "API_DESTINATION_SUMMARY";

    public static final String SUCCESS_REQUEST_COUNT = "success_request_count";

    public static final String THROTTLED_OUT_COUNT = "throttleout_count";

    public static final String FOR_ALL_API_VERSIONS = "FOR_ALL_API_VERSIONS";

    public static final String GROUP_BY_HOUR = "hour";

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
    public static final String API_REQUEST_GEO_LOCATION_SUMMARY = "API_REQ_GEO_LOC_SUMMARY";
    public static final String API_REQUEST_USER_BROWSER_SUMMARY = "API_REQ_USER_BROW_SUMMARY";

    public static final String API_RESPONSE_TIME = "apiResponseTime";
    public static final String REQ_MEDIATION_LATENCY = "requestMediationLatency";
    public static final String RES_MEDIATION_LATENCY = "responseMediationLatency";

    //-------Aggregation names-----
    public static final String API_USER_PER_APP_AGG = "ApiUserPerAppAgg";
    public static final String API_PER_DESTINATION_AGG = "ApiPerDestinationAgg";
    public static final String API_RESOURCE_PATH_PER_APP_AGG = "ApiResPathPerApp";
    public static final String API_LAST_ACCESS_SUMMARY= "ApiLastAccessSummary";
    public static final String API_VERSION_PER_APP_AGG = "ApiVersionPerAppAgg";
    public static final String API_THROTTLED_OUT_AGG = "ApiThrottledOutAgg";
    public static final String API_FAULTY_INVOCATION_AGG = "ApiFaultyInvocationAgg";
    public static final String API_USER_BROWSER_AGG = "ApiUserBrowserAgg";
    public static final String API_EXECUTION_TIME_AGG = "ApiExeTime";
    public static final String APIM_REQ_COUNT_AGG= "APIM_ReqCountAgg";
    public static final String GEO_LOCATION_AGG = "GeoLocationAgg";

    //------Field names-------
    public static final String TIME_STAMP = "AGG_TIMESTAMP";
    public static final String API_NAME = "apiName";
    public static final String API_VERSION = "apiVersion";
    public static final String API_CREATOR = "apiCreator";
    public static final String API_CONTEXT = "apiContext";
    public static final String DESTINATION = "destination";
    public static final String TOTAL_COUNT = "totalCount";
    public static final String TOTAL_REQUEST_COUNT = "totalRequestCount";
    public static final String THROTTLE_COUNT = "throttleCount";
    public static final String SUCCESS_COUNT = "successCount";
    public static final String TOTAL_FAULT_COUNT = "totalFaultCount";
    public static final String API_METHOD = "apiMethod";
    public static final String API_RESOURCE_TEMPLATE = "apiResourceTemplate";
    public static final String USERNAME = "username";
    public static final String API_CREATOR_TENANT_DOMAIN = "apiCreatorTenantDomain";
    public static final String LAST_ACCESS_TIME = "lastAccessTime";
    public static final String APP_OWNER = "applicationOwner";
    public static final String AGG_COUNT = "AGG_COUNT";
    public static final String APPLICATION_NAME = "applicationName";
    public static final String OPERATING_SYSTEM = "operatingSystem";
    public static final String BROWSER = "browser";
    public static final String AGG_SUM_RESPONSE_TIME = "AGG_SUM_responseTime";
    public static final String AGG_SUM_SECURITY_LATENCY = "AGG_SUM_securityLatency";
    public static final String AGG_SUM_THROTTLING_LATENCY = "AGG_SUM_throttlingLatency";
    public static final String AGG_SUM_REQUEST_MEDIATION_LATENCY = "AGG_SUM_requestMedLat";
    public static final String AGG_SUM_RESPONSE_MEDIATION_LATENCY = "AGG_SUM_responseMedLat";
    public static final String AGG_SUM_BACKEND_LATENCY = "AGG_SUM_backendLatency";
    public static final String AGG_SUM_OTHER_LATENCY = "AGG_SUM_otherLatency";
    public static final String RESPONSE_TIME = "responseTime";
    public static final String SERVICE_TIME = "serviceTime";
    public static final String SECURITY_LATENCY = "securityLatency";
    public static final String THROTTLING_LATENCY = "throttlingLatency";
    public static final String REQUEST_MEDIATION_LATENCY = "requestMedLat";
    public static final String RESPONSE_MEDIATION_LATENCY = "responseMedLat";
    public static final String BACKEND_LATENCY = "backendLatency";
    public static final String OTHER_LATENCY = "otherLatency";
    public static final String APPLICATION_ID = "applicationId";
    public static final String COUNTRY = "country";
    public static final String CITY = "city";

    //------Siddhi app names----
    public static final String APIM_ACCESS_SUMMARY_SIDDHI_APP = "APIM_ACCESS_SUMMARY";
    public static final String APIM_THROTTLED_OUT_SUMMARY_SIDDHI_APP = "APIM_THROTTLED_OUT_SUMMARY";
    public static final String APIM_FAULT_SUMMARY_SIDDHI_APP = "APIM_FAULT_SUMMARY";

    //------Other---------------
    public static final String DAYS_GRANULARITY = "days";
    public static final String MONTHS_GRANULARITY = "months";
    public static final String YEARS_GRANULARITY = "years";
    public static final String HOURS_GRANULARITY = "hours";
    public static final String MINUTES_GRANULARITY = "minutes";
    public static final String SECONDS_GRANULARITY = "seconds";

    public static final String DURATION_SECONDS = "SECONDS";
    public static final String DURATION_MINUTES = "MINUTES";
    public static final String DURATION_HOURS = "HOURS";
    public static final String DURATION_DAYS = "DAYS";
    public static final String DURATION_MONTHS = "MONTHS";
    public static final String DURATION_WEEKS = "WEEKS";
    public static final String DURATION_YEARS = "YEARS";

    public static final String TIMESTAMP_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String RECORDS_DELIMITER = "records";

    public static final String PRODUCTION_KEY_TYPE = "PRODUCTION";
    public static final String SANDBOX_KEY_TYPE = "SANDBOX";
}

/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl.dao.constants;

public class SubscriptionValidationSQLConstants {

    public static final String GET_ALL_APPLICATIONS_SQL =
            " SELECT " +
                    "   APP.APPLICATION_ID AS APP_ID," +
                    "   APP.APPLICATION_TIER AS TIER," +
                    "   APP.TOKEN_TYPE AS TOKEN_TYPE," +
                    "   SUB.USER_ID AS SUB_NAME" +
                    " FROM " +
                    "   AM_APPLICATION AS APP," +
                    "   AM_SUBSCRIBER AS SUB" +
                    " WHERE " +
                    "   APP.SUBSCRIBER_ID = SUB.SUBSCRIBER_ID ";

    public static final String GET_TENANT_APPLICATIONS_SQL =
            " SELECT " +
                    "   APP.APPLICATION_ID AS APP_ID," +
                    "   APP.APPLICATION_TIER AS TIER," +
                    "   APP.TOKEN_TYPE AS TOKEN_TYPE," +
                    "   SUB.USER_ID AS SUB_NAME" +
                    " FROM " +
                    "   AM_APPLICATION AS APP," +
                    "   AM_SUBSCRIBER AS SUB" +
                    " WHERE " +
                    "   APP.SUBSCRIBER_ID = SUB.SUBSCRIBER_ID AND" +
                    "   SUB.TENANT_ID = ? ";

    public static final String GET_APPLICATION_BY_ID_SQL =
            " SELECT " +
                    "   APP.APPLICATION_ID AS APP_ID," +
                    "   APP.APPLICATION_TIER AS TIER," +
                    "   APP.TOKEN_TYPE AS TOKEN_TYPE," +
                    "   SUB.USER_ID AS SUB_NAME" +
                    " FROM " +
                    "   AM_APPLICATION AS APP," +
                    "   AM_SUBSCRIBER AS SUB" +
                    " WHERE " +
                    "   APP.SUBSCRIBER_ID = SUB.SUBSCRIBER_ID AND" +
                    "   APP.APPLICATION_ID = ? ";

    public static final String GET_ALL_SUBSCRIPTIONS_SQL =
            "SELECT " +
                    "   SUBSCRIPTION_ID AS SUB_ID," +
                    "   TIER_ID AS TIER," +
                    "   API_ID AS API_ID," +
                    "   APPLICATION_ID AS APP_ID," +
                    "   SUB_STATUS AS STATUS" +
                    " FROM " +
                    "   AM_SUBSCRIPTION";

    public static final String GET_SUBSCRIPTION_SQL =
            "SELECT " +
                    "   SUBSCRIPTION_ID AS SUB_ID," +
                    "   TIER_ID AS TIER," +
                    "   API_ID AS API_ID," +
                    "   APPLICATION_ID AS APP_ID," +
                    "   SUB_STATUS AS STATUS" +
                    " FROM " +
                    "   AM_SUBSCRIPTION" +
                    " WHERE " +
                    "   API_ID = ? AND " +
                    "   APPLICATION_ID = ? ";

    public static final String GET_ALL_SUBSCRIPTION_POLICIES_SQL =
            "SELECT " +
                    "   APS.POLICY_ID," +
                    "   APS.NAME," +
                    "   APS.RATE_LIMIT_COUNT," +
                    "   APS.RATE_LIMIT_TIME_UNIT," +
                    "   APS.QUOTA_TYPE," +
                    "   APS.STOP_ON_QUOTA_REACH," +
                    "   APS.TENANT_ID," +
                    "   AM_GRAPHQL_QUERY_ANALYSIS.MAX_DEPTH," +
                    "   AM_GRAPHQL_QUERY_ANALYSIS.MAX_COMPLEXITY" +
                    " FROM " +
                    "   AM_POLICY_SUBSCRIPTION APS" +
                    " LEFT OUTER JOIN "+
                    "   AM_GRAPHQL_QUERY_ANALYSIS"+
                    " ON "+
                    "   APS.POLICY_ID = AM_GRAPHQL_QUERY_ANALYSIS.POLICY_ID";

    public static final String GET_ALL_APPLICATION_POLICIES_SQL =
            "SELECT " +
                    "   POLICY_ID," +
                    "   NAME," +
                    "   QUOTA_TYPE," +
                    "   TENANT_ID " +
                    "FROM " +
                    "   AM_POLICY_APPLICATION";

    public static final String GET_ALL_APIS_SQL =
            "SELECT " +
                    "  API.API_ID," +
                    "  API.API_PROVIDER," +
                    "  API.API_NAME," +
                    "  API.API_TIER," +
                    "  API.API_VERSION," +
                    "  API.CONTEXT, " +
                    "  URL.URL_MAPPING_ID," +
                    "  URL.HTTP_METHOD," +
                    "  URL.AUTH_SCHEME," +
                    "  URL.URL_PATTERN," +
                    "  URL.THROTTLING_TIER AS RES_TIER," +
                    "  SCOPE.SCOPE_NAME " +
                    " FROM " +
                    "  AM_API AS API," +
                    "  AM_API_URL_MAPPING AS URL" +
                    " LEFT JOIN " +
                    " AM_API_RESOURCE_SCOPE_MAPPING AS SCOPE " +
                    " ON " +
                    "  URL.URL_MAPPING_ID = SCOPE.URL_MAPPING_ID" +
                    " WHERE " +
                    "  API.API_ID = URL.API_ID";

    public static final String GET_ALL_AM_KEY_MAPPINGS_SQL =
            "SELECT " +
                    "   APPLICATION_ID," +
                    "   CONSUMER_KEY," +
                    "   KEY_TYPE," +
                    "   STATE" +
                    " FROM " +
                    "   AM_APPLICATION_KEY_MAPPING";

    public static final String GET_AM_KEY_MAPPING_SQL =
            "SELECT " +
                    "   APPLICATION_ID," +
                    "   CONSUMER_KEY," +
                    "   KEY_TYPE," +
                    "   STATE " +
                    " FROM " +
                    "   AM_APPLICATION_KEY_MAPPING" +
                    " WHERE " +
                    "APPLICATION_ID = ? " +
                    "AND KEY_TYPE = ? ";

    public static final String GET_AM_KEY_MAPPING_BY_CONSUMER_KEY_SQL =
            "SELECT " +
                    "   APPLICATION_ID," +
                    "   CONSUMER_KEY," +
                    "   KEY_TYPE," +
                    "   STATE " +
                    " FROM " +
                    "   AM_APPLICATION_KEY_MAPPING" +
                    " WHERE " +
                    "CONSUMER_KEY = ? ";

    public static final String GET_TENANT_SUBSCRIPTIONS_SQL =
            "SELECT " +
                    "   SUBS.SUBSCRIPTION_ID AS SUB_ID," +
                    "   SUBS.TIER_ID AS TIER," +
                    "   SUBS.API_ID AS API_ID," +
                    "   APP.APPLICATION_ID AS APP_ID," +
                    "   SUBS.SUB_STATUS AS STATUS," +
                    "   SUB.TENANT_ID AS TENANT_ID" +
                    " FROM " +
                    "   AM_SUBSCRIPTION AS SUBS," +
                    "   AM_APPLICATION AS APP," +
                    "   AM_SUBSCRIBER AS SUB" +
                    " WHERE " +
                    "   SUBS.APPLICATION_ID = APP.APPLICATION_ID AND " +
                    "   APP.SUBSCRIBER_ID = SUB.SUBSCRIBER_ID AND " +
                    "   SUB.TENANT_ID = ? ";

    public static final String GET_TENANT_SUBSCRIPTION_POLICIES_SQL =
            "SELECT " +
                    "   APS.POLICY_ID AS POLICY_ID," +
                    "   APS.NAME AS NAME," +
                    "   APS.RATE_LIMIT_COUNT AS RATE_LIMIT_COUNT," +
                    "   APS.RATE_LIMIT_TIME_UNIT AS RATE_LIMIT_TIME_UNIT," +
                    "   APS.QUOTA_TYPE AS QUOTA_TYPE," +
                    "   APS.STOP_ON_QUOTA_REACH AS STOP_ON_QUOTA_REACH," +
                    "   APS.TENANT_ID AS TENANT_ID," +
                    "   AM_GRAPHQL_QUERY_ANALYSIS.MAX_DEPTH AS MAX_DEPTH,"+
                    "   AM_GRAPHQL_QUERY_ANALYSIS.MAX_COMPLEXITY AS MAX_COMPLEXITY" +
                    " FROM " +
                    "   AM_POLICY_SUBSCRIPTION AS APS" +
                    " LEFT OUTER JOIN "+
                    "   AM_GRAPHQL_QUERY_ANALYSIS" +
                    " ON "+
                    "   APS.POLICY_ID = AM_GRAPHQL_QUERY_ANALYSIS.POLICY_ID " +
                    " WHERE " +
                    "   APS.TENANT_ID = ? ";

    public static final String GET_SUBSCRIPTION_POLICY_SQL =
            "SELECT " +
                    "   APS.POLICY_ID AS POLICY_ID," +
                    "   APS.NAME AS APS_NAME," +
                    "   APS.RATE_LIMIT_COUNT AS RATE_LIMIT_COUNT," +
                    "   APS.RATE_LIMIT_TIME_UNIT AS RATE_LIMIT_TIME_UNIT," +
                    "   APS.QUOTA_TYPE AS QUOTA_TYPE," +
                    "   APS.STOP_ON_QUOTA_REACH AS STOP_ON_QUOTA_REACH, " +
                    "   APS.TENANT_ID AS TENANT_ID, " +
                    "   AM_GRAPHQL_QUERY_ANALYSIS.MAX_DEPTH AS MAX_DEPTH, " +
                    "   AM_GRAPHQL_QUERY_ANALYSIS.MAX_COMPLEXITY AS MAX_COMPLEXITY" +
                    "FROM " +
                    "   AM_POLICY_SUBSCRIPTION AS APS" +
                    "LEFT OUTER JOIN "+
                    "   AM_GRAPHQL_QUERY_ANALYSIS " +
                    " ON "+
                    "APS.POLICY_ID = AM_GRAPHQL_QUERY_ANALYSIS.POLICY_ID " +
                    " WHERE " +
                    "   APS.NAME = ? AND " +
                    "   APS.TENANT_ID = ?";


    public static final String GET_TENANT_APPLICATION_POLICIES_SQL =
            "SELECT " +
                    "   POLICY_ID," +
                    "   NAME," +
                    "   QUOTA_TYPE," +
                    "   TENANT_ID " +
                    "FROM " +
                    "   AM_POLICY_APPLICATION" +
                    " WHERE " +
                    "   TENANT_ID = ? ";

    public static final String GET_TENANT_API_POLICIES_SQL =
            "SELECT" +
                    "   POLICY.POLICY_ID," +
                    "   POLICY.NAME," +
                    "   POLICY.TENANT_ID," +
                    "   POLICY.DEFAULT_QUOTA_TYPE," +
                    "   POLICY.APPLICABLE_LEVEL," +
                    "   COND.CONDITION_GROUP_ID," +
                    "   COND.QUOTA_TYPE" +
                    " FROM" +
                    "   AM_API_THROTTLE_POLICY AS POLICY " +
                    " LEFT JOIN " +
                    "   AM_CONDITION_GROUP AS COND " +
                    " ON " +
                    "   POLICY.POLICY_ID = COND.POLICY_ID" +
                    " WHERE POLICY.TENANT_ID = ?";

    public static final String GET_API_POLICY_BY_NAME =
            "SELECT" +
                    "   POLICY.POLICY_ID," +
                    "   POLICY.NAME," +
                    "   POLICY.TENANT_ID," +
                    "   POLICY.DEFAULT_QUOTA_TYPE," +
                    "   COND.CONDITION_GROUP_ID," +
                    "   COND.QUOTA_TYPE" +
                    " FROM" +
                    "   AM_API_THROTTLE_POLICY AS POLICY " +
                    " LEFT JOIN " +
                    "   AM_CONDITION_GROUP AS COND " +
                    " ON " +
                    "   POLICY.POLICY_ID = COND.POLICY_ID" +
                    " WHERE " +
                    " POLICY.TENANT_ID = ? AND" +
                    " ";

    public static final String GET_APPLICATION_POLICY_SQL =
            "SELECT " +
                    "   POLICY_ID," +
                    "   NAME," +
                    "   QUOTA_TYPE," +
                    "   TENANT_ID " +
                    "FROM " +
                    "   AM_POLICY_APPLICATION" +
                    " WHERE " +
                    "   NAME = ? AND" +
                    "   TENANT_ID = ? ";

    public static final String GET_API_POLICY_SQL =
            "SELECT" +
                    "   POLICY.POLICY_ID," +
                    "   POLICY.NAME," +
                    "   POLICY.TENANT_ID," +
                    "   POLICY.DEFAULT_QUOTA_TYPE," +
                    "   COND.CONDITION_GROUP_ID," +
                    "   COND.QUOTA_TYPE" +
                    " FROM" +
                    "   AM_API_THROTTLE_POLICY AS POLICY " +
                    " LEFT JOIN " +
                    "   AM_CONDITION_GROUP AS COND " +
                    " ON " +
                    "   POLICY.POLICY_ID = COND.POLICY_ID";
    
    public static final String GET_TENANT_API_POLICY_SQL =
            "SELECT" +
                    "   POLICY.POLICY_ID," +
                    "   POLICY.NAME," +
                    "   POLICY.TENANT_ID," +
                    "   POLICY.DEFAULT_QUOTA_TYPE," +
                    "   POLICY.APPLICABLE_LEVEL," +
                    "   COND.CONDITION_GROUP_ID," +
                    "   COND.QUOTA_TYPE" +
                    " FROM" +
                    "   AM_API_THROTTLE_POLICY AS POLICY " +
                    " LEFT JOIN " +
                    "   AM_CONDITION_GROUP AS COND " +
                    " ON " +
                    "   POLICY.POLICY_ID = COND.POLICY_ID" +
                    " WHERE POLICY.TENANT_ID = ? AND POLICY.NAME = ?";

    public static final String GET_TENANT_APIS_SQL =
            "SELECT " +
                    "  API.API_ID," +
                    "  API.API_PROVIDER," +
                    "  API.API_NAME," +
                    "  API.API_TIER," +
                    "  API.API_VERSION," +
                    "  API.CONTEXT, " +
                    "  URL.URL_MAPPING_ID," +
                    "  URL.HTTP_METHOD," +
                    "  URL.AUTH_SCHEME," +
                    "  URL.URL_PATTERN," +
                    "  URL.THROTTLING_TIER AS RES_TIER" +
                    " FROM " +
                    "  AM_API AS API," +
                    "  AM_API_URL_MAPPING AS URL" +
                    " LEFT JOIN " +
                    "  AM_API_RESOURCE_SCOPE_MAPPING AS SCOPE " +
                    " ON " +
                    "  URL.URL_MAPPING_ID = SCOPE.URL_MAPPING_ID" +
                    " WHERE " +
                    "  CONTEXT LIKE ? AND " +
                    "  API.API_ID = URL.API_ID";
    public static final String GET_ST_APIS_SQL =
            "SELECT " +
                    "  API.API_ID," +
                    "  API.API_PROVIDER," +
                    "  API.API_NAME," +
                    "  API.API_TIER," +
                    "  API.API_VERSION," +
                    "  API.CONTEXT, " +
                    "  URL.URL_MAPPING_ID," +
                    "  URL.HTTP_METHOD," +
                    "  URL.AUTH_SCHEME," +
                    "  URL.URL_PATTERN," +
                    "  URL.THROTTLING_TIER AS RES_TIER" +
                    " FROM " +
                    "  AM_API AS API," +
                    "  AM_API_URL_MAPPING AS URL" +
                    " LEFT JOIN " +
                    "  AM_API_RESOURCE_SCOPE_MAPPING AS SCOPE " +
                    " ON " +
                    "  URL.URL_MAPPING_ID = SCOPE.URL_MAPPING_ID " +
                    " WHERE " +
                    "  CONTEXT NOT LIKE ? AND " +
                    "  API.API_ID = URL.API_ID ";

    public static final String GET_API_SQL =
            "SELECT " +
                    "  API.API_ID," +
                    "  API.API_PROVIDER," +
                    "  API.API_NAME," +
                    "  API.API_TIER," +
                    "  API.API_VERSION," +
                    "  API.CONTEXT, " +
                    "  URL.URL_MAPPING_ID," +
                    "  URL.HTTP_METHOD," +
                    "  URL.AUTH_SCHEME," +
                    "  URL.URL_PATTERN," +
                    "  URL.THROTTLING_TIER AS RES_TIER," +
                    "  SCOPE.SCOPE_NAME " +
                    " FROM " +
                    "   AM_API AS API," +
                    "   AM_API_URL_MAPPING AS URL" +
                    " LEFT JOIN " +
                    "  AM_API_RESOURCE_SCOPE_MAPPING AS SCOPE " +
                    " ON " +
                    "  URL.URL_MAPPING_ID = SCOPE.URL_MAPPING_ID " +
                    " WHERE " +
                    "   API.API_ID = URL.API_ID AND " +
                    "   API.API_VERSION = ? AND " +
                    "   API.CONTEXT = ?";

    public static final String GET_TENANT_AM_KEY_MAPPING_SQL =
            "SELECT " +
                    "   MAPPING.APPLICATION_ID," +
                    "   MAPPING.CONSUMER_KEY," +
                    "   MAPPING.KEY_TYPE," +
                    "   MAPPING.STATE" +
                    " FROM " +
                    "   AM_APPLICATION_KEY_MAPPING AS MAPPING," +
                    "   AM_APPLICATION AS APP," +
                    "   AM_SUBSCRIBER AS SUB" +
                    " WHERE " +
                    "   MAPPING.APPLICATION_ID = APP.APPLICATION_ID AND" +
                    "   APP.SUBSCRIBER_ID = SUB.SUBSCRIBER_ID AND" +
                    "   SUB.TENANT_ID = ?";
//todo remove
    public static final String GET_ALL_API_URL_MAPPING_SQL =
            "SELECT " +
                    "   URL_MAPPING_ID," +
                    "   API_ID," +
                    "   HTTP_METHOD," +
                    "   AUTH_SCHEME," +
                    "   URL_PATTERN," +
                    "   THROTTLING_TIER AS POLICY" +
                    " FROM " +
                    "   AM_API_URL_MAPPING";
//todo remove
    public static final String GET_TENANT_API_URL_MAPPING_SQL =
            "SELECT " +
                    "   URL.URL_MAPPING_ID AS URL_MAPPING_ID," +
                    "   URL.API_ID AS API_ID," +
                    "   URL.HTTP_METHOD AS HTTP_METHOD," +
                    "   URL.AUTH_SCHEME AS AUTH_SCHEME," +
                    "   URL.THROTTLING_TIER AS POLICY" +
                    " FROM "+
                    "   AM_API_URL_MAPPING AS URL," +
                    "   AM_API AS API" +
                    " WHERE " +
                    "   URL.API_ID = API.API_ID AND " +
                    "   API.CONTEXT LIKE ? ";

    public static final String GET_ST_API_URL_MAPPING_SQL =
            "SELECT " +
                    "   URL.URL_MAPPING_ID AS URL_MAPPING_ID," +
                    "   URL.API_ID AS API_ID," +
                    "   URL.HTTP_METHOD AS HTTP_METHOD," +
                    "   URL.AUTH_SCHEME AS AUTH_SCHEME," +
                    "   URL.THROTTLING_TIER AS POLICY" +
                    " FROM "+
                    "   AM_API_URL_MAPPING AS URL," +
                    "   AM_API AS API" +
                    " WHERE " +
                    "   URL.API_ID = API.API_ID AND " +
                    "   API.CONTEXT NOT LIKE ? ";

    public static final String GET_API_URL_MAPPING_SQL =
            "SELECT " +
                    "   URL_MAPPING_ID," +
                    "   API_ID," +
                    "   HTTP_METHOD," +
                    "   AUTH_SCHEME," +
                    "   URL_PATTERN," +
                    "   THROTTLING_TIER AS POLICY" +
                    " FROM "+
                    "   AM_API_URL_MAPPING" +
                    " WHERE " +
                    "   URL_MAPPING_ID = ?";

}

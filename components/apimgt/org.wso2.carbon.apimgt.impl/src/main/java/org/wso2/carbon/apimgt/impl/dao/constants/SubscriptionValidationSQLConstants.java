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
                    "   APP.UUID AS APP_UUID," +
                    "   APP.APPLICATION_ID AS APP_ID," +
                    "   APP.APPLICATION_TIER AS TIER," +
                    "   APP.NAME AS APS_NAME," +
                    "   APP.TOKEN_TYPE AS TOKEN_TYPE," +
                    "   SUB.USER_ID AS SUB_NAME," +
                    "   ATTRIBUTES.NAME AS ATTRIBUTE_NAME," +
                    "   ATTRIBUTES.VALUE AS ATTRIBUTE_VALUE" +
                    " FROM " +
                    "   AM_SUBSCRIBER SUB," +
                    "   AM_APPLICATION APP" +
                    "   LEFT OUTER JOIN AM_APPLICATION_ATTRIBUTES ATTRIBUTES  " +
                    "ON APP.APPLICATION_ID = ATTRIBUTES.APPLICATION_ID" +
                    " WHERE " +
                    "   APP.SUBSCRIBER_ID = SUB.SUBSCRIBER_ID ";

    public static final String GET_TENANT_APPLICATIONS_SQL =
            " SELECT " +
                    "   APP.UUID AS APP_UUID," +
                    "   APP.APPLICATION_ID AS APP_ID," +
                    "   APP.NAME AS APS_NAME," +
                    "   APP.APPLICATION_TIER AS TIER," +
                    "   APP.TOKEN_TYPE AS TOKEN_TYPE," +
                    "   SUB.USER_ID AS SUB_NAME," +
                    "   ATTRIBUTES.NAME AS ATTRIBUTE_NAME," +
                    "   ATTRIBUTES.VALUE AS ATTRIBUTE_VALUE"+
                    " FROM " +
                    "   AM_SUBSCRIBER SUB," +
                    "   AM_APPLICATION APP" +
                    "   LEFT OUTER JOIN AM_APPLICATION_ATTRIBUTES ATTRIBUTES" +
                    "  ON APP.APPLICATION_ID = ATTRIBUTES.APPLICATION_ID" +
                    " WHERE " +
                    "   APP.SUBSCRIBER_ID = SUB.SUBSCRIBER_ID AND" +
                    "   SUB.TENANT_ID = ? ";

    public static final String GET_APPLICATION_BY_ID_SQL =
            " SELECT " +
                    "   APP.UUID AS APP_UUID," +
                    "   APP.APPLICATION_ID AS APP_ID," +
                    "   APP.NAME AS APS_NAME," +
                    "   APP.APPLICATION_TIER AS TIER," +
                    "   APP.TOKEN_TYPE AS TOKEN_TYPE," +
                    "   SUB.USER_ID AS SUB_NAME," +
                    "   ATTRIBUTES.NAME AS ATTRIBUTE_NAME," +
                    "   ATTRIBUTES.VALUE AS ATTRIBUTE_VALUE"+
                    " FROM " +
                    "   AM_SUBSCRIBER SUB," +
                    "   AM_APPLICATION APP" +
                    "   LEFT OUTER JOIN AM_APPLICATION_ATTRIBUTES ATTRIBUTES  " +
                    "ON APP.APPLICATION_ID = ATTRIBUTES.APPLICATION_ID" +
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
                    "   APS.POLICY_ID AS POLICY_ID, " +
                    "   APS.NAME AS POLICY_NAME, " +
                    "   APS.RATE_LIMIT_COUNT AS RATE_LIMIT_COUNT, " +
                    "   APS.RATE_LIMIT_TIME_UNIT AS RATE_LIMIT_TIME_UNIT, " +
                    "   APS.QUOTA_TYPE AS QUOTA_TYPE, " +
                    "   APS.STOP_ON_QUOTA_REACH AS STOP_ON_QUOTA_REACH, " +
                    "   APS.TENANT_ID AS TENANT_ID, " +
                    "   APS.MAX_DEPTH AS MAX_DEPTH, " +
                    "   APS.MAX_COMPLEXITY AS MAX_COMPLEXITY, " +
                    "   APS.QUOTA AS QUOTA, " +
                    "   APS.QUOTA_UNIT AS QUOTA_UNIT, " +
                    "   APS.UNIT_TIME AS UNIT_TIME, " +
                    "   APS.TIME_UNIT AS TIME_UNIT " +
                    " FROM " +
                    "   AM_POLICY_SUBSCRIPTION APS";

    public static final String GET_ALL_APPLICATION_POLICIES_SQL =
            "SELECT " +
                    "   POLICY_ID," +
                    "   NAME," +
                    "   QUOTA_TYPE," +
                    "   TENANT_ID, " +
                    "   QUOTA, " +
                    "   QUOTA_UNIT, " +
                    "   UNIT_TIME, " +
                    "   TIME_UNIT " +
                    "FROM " +
                    "   AM_POLICY_APPLICATION";


    public static final String GET_ALL_API_POLICIES_SQL =
            "SELECT" +
                    "   POLICY.POLICY_ID," +
                    "   POLICY.NAME," +
                    "   POLICY.TENANT_ID," +
                    "   POLICY.DEFAULT_QUOTA_TYPE," +
                    "   POLICY.DEFAULT_QUOTA," +
                    "   POLICY.DEFAULT_QUOTA_UNIT," +
                    "   POLICY.DEFAULT_UNIT_TIME," +
                    "   POLICY.DEFAULT_TIME_UNIT," +
                    "   POLICY.APPLICABLE_LEVEL," +
                    "   COND.CONDITION_GROUP_ID," +
                    "   COND.QUOTA_TYPE," +
                    "   COND.QUOTA AS QUOTA," +
                    "   COND.QUOTA_UNIT AS QUOTA_UNIT," +
                    "   COND.UNIT_TIME AS UNIT_TIME," +
                    "   COND.TIME_UNIT AS TIME_UNIT" +
                    " FROM" +
                    "   AM_API_THROTTLE_POLICY POLICY " +
                    " LEFT JOIN " +
                    "   AM_CONDITION_GROUP COND " +
                    " ON " +
                    "   POLICY.POLICY_ID = COND.POLICY_ID";

    public static final String GET_ALL_APIS_SQL =
            "SELECT " +
                    "      APIS.API_ID," +
                    "      APIS.API_PROVIDER," +
                    "      APIS.API_NAME," +
                    "      APIS.API_TIER," +
                    "      APIS.API_VERSION," +
                    "      APIS.CONTEXT," +
                    "      APIS.API_TYPE," +
                    "      APIS.URL_MAPPING_ID," +
                    "      APIS.HTTP_METHOD," +
                    "      APIS.AUTH_SCHEME," +
                    "      APIS.URL_PATTERN," +
                    "      APIS.RES_TIER," +
                    "      APIS.SCOPE_NAME," +
                    "      DEF.PUBLISHED_DEFAULT_API_VERSION" +
                    " FROM " +
                    "  (" +
                    "    SELECT " +
                    "      API.API_ID," +
                    "      API.API_PROVIDER," +
                    "      API.API_NAME," +
                    "      API.API_TIER," +
                    "      API.API_VERSION," +
                    "      API.CONTEXT," +
                    "      API.API_TYPE," +
                    "      URL.URL_MAPPING_ID," +
                    "      URL.HTTP_METHOD," +
                    "      URL.AUTH_SCHEME," +
                    "      URL.URL_PATTERN," +
                    "      URL.THROTTLING_TIER AS RES_TIER," +
                    "      SCOPE.SCOPE_NAME" +
                    "    FROM " +
                    "      AM_API API," +
                    "      AM_API_URL_MAPPING URL" +
                    "      LEFT JOIN AM_API_RESOURCE_SCOPE_MAPPING SCOPE ON" +
                    "      URL.URL_MAPPING_ID = SCOPE.URL_MAPPING_ID" +
                    "    WHERE " +
                    "      API.API_ID = URL.API_ID" +
                    "    UNION " +
                    "    SELECT " +
                    "      API.API_ID," +
                    "      API.API_PROVIDER," +
                    "      API.API_NAME," +
                    "      API.API_TIER," +
                    "      API.API_VERSION," +
                    "      API.CONTEXT," +
                    "      API.API_TYPE," +
                    "      URL.URL_MAPPING_ID," +
                    "      URL.HTTP_METHOD," +
                    "      URL.AUTH_SCHEME," +
                    "      URL.URL_PATTERN," +
                    "      URL.THROTTLING_TIER AS RES_TIER," +
                    "      SCOPE.SCOPE_NAME" +
                    "    FROM " +
                    "      AM_API API," +
                    "      AM_API_PRODUCT_MAPPING PROD," +
                    "      AM_API_URL_MAPPING URL" +
                    "      LEFT JOIN AM_API_RESOURCE_SCOPE_MAPPING SCOPE ON" +
                    "      URL.URL_MAPPING_ID = SCOPE.URL_MAPPING_ID" +
                    "    WHERE " +
                    "      URL.URL_MAPPING_ID = PROD.URL_MAPPING_ID" +
                    "      AND API.API_ID = URL.API_ID" +
                    "      AND URL.API_ID = PROD.API_ID" +
                    "  ) APIS " +
                    "  LEFT JOIN AM_API_DEFAULT_VERSION DEF ON APIS.API_NAME = DEF.API_NAME" +
                    "  AND APIS.API_PROVIDER = DEF.API_PROVIDER AND " +
                    "  APIS.API_VERSION = DEF.PUBLISHED_DEFAULT_API_VERSION";

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
                    "   KEY_MANAGER," +
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
                    "   AM_SUBSCRIPTION SUBS," +
                    "   AM_APPLICATION APP," +
                    "   AM_SUBSCRIBER SUB" +
                    " WHERE " +
                    "   SUBS.APPLICATION_ID = APP.APPLICATION_ID AND " +
                    "   APP.SUBSCRIBER_ID = SUB.SUBSCRIBER_ID AND " +
                    "   SUB.TENANT_ID = ? ";

    public static final String GET_TENANT_SUBSCRIPTION_POLICIES_SQL =
            "SELECT " +
                    "   APS.POLICY_ID AS POLICY_ID," +
                    "   APS.NAME AS POLICY_NAME," +
                    "   APS.RATE_LIMIT_COUNT AS RATE_LIMIT_COUNT," +
                    "   APS.RATE_LIMIT_TIME_UNIT AS RATE_LIMIT_TIME_UNIT," +
                    "   APS.QUOTA_TYPE AS QUOTA_TYPE," +
                    "   APS.STOP_ON_QUOTA_REACH AS STOP_ON_QUOTA_REACH," +
                    "   APS.TENANT_ID AS TENANT_ID," +
                    "   APS.MAX_DEPTH AS MAX_DEPTH,"+
                    "   APS.MAX_COMPLEXITY AS MAX_COMPLEXITY, " +
                    "   APS.QUOTA_TYPE AS QUOTA_TYPE, " +
                    "   APS.QUOTA AS QUOTA, " +
                    "   APS.QUOTA_UNIT AS QUOTA_UNIT, " +
                    "   APS.UNIT_TIME AS UNIT_TIME, " +
                    "   APS.TIME_UNIT AS TIME_UNIT " +
                    " FROM " +
                    "   AM_POLICY_SUBSCRIPTION APS" +
                    " WHERE " +
                    "   APS.TENANT_ID = ? ";

    public static final String GET_SUBSCRIPTION_POLICY_SQL =
            "SELECT " +
                    "   APS.POLICY_ID AS POLICY_ID," +
                    "   APS.NAME AS POLICY_NAME," +
                    "   APS.RATE_LIMIT_COUNT AS RATE_LIMIT_COUNT," +
                    "   APS.RATE_LIMIT_TIME_UNIT AS RATE_LIMIT_TIME_UNIT," +
                    "   APS.QUOTA_TYPE AS QUOTA_TYPE," +
                    "   APS.STOP_ON_QUOTA_REACH AS STOP_ON_QUOTA_REACH, " +
                    "   APS.TENANT_ID AS TENANT_ID, " +
                    "   APS.MAX_DEPTH AS MAX_DEPTH, " +
                    "   APS.MAX_COMPLEXITY AS MAX_COMPLEXITY, " +
                    "   APS.QUOTA_TYPE AS QUOTA_TYPE, " +
                    "   APS.QUOTA AS QUOTA, " +
                    "   APS.QUOTA_UNIT AS QUOTA_UNIT, " +
                    "   APS.UNIT_TIME AS UNIT_TIME, " +
                    "   APS.TIME_UNIT AS TIME_UNIT " +
                    " FROM " +
                    "   AM_POLICY_SUBSCRIPTION APS" +
                    " WHERE " +
                    "   APS.NAME = ? AND " +
                    "   APS.TENANT_ID = ?";


    public static final String GET_TENANT_APPLICATION_POLICIES_SQL =
            "SELECT " +
                    "   POLICY_ID," +
                    "   NAME," +
                    "   QUOTA_TYPE," +
                    "   TENANT_ID, " +
                    "   QUOTA, " +
                    "   QUOTA_UNIT, " +
                    "   UNIT_TIME, " +
                    "   TIME_UNIT " +
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
                    "   POLICY.DEFAULT_QUOTA," +
                    "   POLICY.DEFAULT_QUOTA_UNIT," +
                    "   POLICY.DEFAULT_UNIT_TIME," +
                    "   POLICY.DEFAULT_TIME_UNIT," +
                    "   POLICY.APPLICABLE_LEVEL," +
                    "   COND.CONDITION_GROUP_ID," +
                    "   COND.QUOTA_TYPE," +
                    "   COND.QUOTA AS QUOTA," +
                    "   COND.QUOTA_UNIT AS QUOTA_UNIT," +
                    "   COND.UNIT_TIME AS UNIT_TIME," +
                    "   COND.TIME_UNIT AS TIME_UNIT" +
                    " FROM" +
                    "   AM_API_THROTTLE_POLICY POLICY " +
                    " LEFT JOIN " +
                    "   AM_CONDITION_GROUP COND " +
                    " ON " +
                    "   POLICY.POLICY_ID = COND.POLICY_ID" +
                    " WHERE POLICY.TENANT_ID = ?";

    public static final String GET_API_POLICY_BY_NAME =
            "SELECT" +
                    "   POLICY.POLICY_ID," +
                    "   POLICY.NAME," +
                    "   POLICY.TENANT_ID," +
                    "   POLICY.DEFAULT_QUOTA_TYPE," +
                    "   POLICY.DEFAULT_QUOTA," +
                    "   POLICY.DEFAULT_QUOTA_UNIT," +
                    "   POLICY.DEFAULT_UNIT_TIME," +
                    "   POLICY.DEFAULT_TIME_UNIT," +
                    "   COND.CONDITION_GROUP_ID," +
                    "   COND.QUOTA_TYPE," +
                    "   COND.QUOTA AS QUOTA," +
                    "   COND.QUOTA_UNIT AS QUOTA_UNIT," +
                    "   COND.UNIT_TIME AS UNIT_TIME," +
                    "   COND.TIME_UNIT AS TIME_UNIT" +
                    " FROM" +
                    "   AM_API_THROTTLE_POLICY POLICY " +
                    " LEFT JOIN " +
                    "   AM_CONDITION_GROUP COND " +
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
                    "   TENANT_ID, " +
                    "   QUOTA, " +
                    "   QUOTA_UNIT, " +
                    "   UNIT_TIME, " +
                    "   TIME_UNIT " +
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
                    "   AM_API_THROTTLE_POLICY POLICY " +
                    " LEFT JOIN " +
                    "   AM_CONDITION_GROUP COND " +
                    " ON " +
                    "   POLICY.POLICY_ID = COND.POLICY_ID";
    
    public static final String GET_TENANT_API_POLICY_SQL =
            "SELECT" +
                    "   POLICY.POLICY_ID," +
                    "   POLICY.NAME," +
                    "   POLICY.TENANT_ID," +
                    "   POLICY.DEFAULT_QUOTA_TYPE," +
                    "   POLICY.DEFAULT_QUOTA AS DEFAULT_QUOTA," +
                    "   POLICY.DEFAULT_QUOTA_UNIT AS DEFAULT_QUOTA_UNIT," +
                    "   POLICY.DEFAULT_UNIT_TIME AS DEFAULT_UNIT_TIME," +
                    "   POLICY.DEFAULT_TIME_UNIT AS DEFAULT_TIME_UNIT," +
                    "   POLICY.APPLICABLE_LEVEL," +
                    "   COND.CONDITION_GROUP_ID," +
                    "   COND.QUOTA_TYPE," +
                    "   COND.QUOTA AS QUOTA," +
                    "   COND.QUOTA_UNIT AS QUOTA_UNIT," +
                    "   COND.UNIT_TIME AS UNIT_TIME," +
                    "   COND.TIME_UNIT AS TIME_UNIT" +
                    " FROM" +
                    "   AM_API_THROTTLE_POLICY POLICY " +
                    " LEFT JOIN " +
                    "   AM_CONDITION_GROUP COND " +
                    " ON " +
                    "   POLICY.POLICY_ID = COND.POLICY_ID" +
                    " WHERE POLICY.TENANT_ID = ? AND POLICY.NAME = ?";

    public static final String GET_TENANT_APIS_SQL =
            "SELECT" +
                    "   APIS.API_ID," +
                    "   APIS.API_PROVIDER," +
                    "   APIS.API_NAME," +
                    "   APIS.API_TIER," +
                    "   APIS.API_VERSION," +
                    "   APIS.CONTEXT," +
                    "   APIS.API_TYPE," +
                    "   APIS.URL_MAPPING_ID," +
                    "   APIS.HTTP_METHOD," +
                    "   APIS.AUTH_SCHEME," +
                    "   APIS.URL_PATTERN," +
                    "   APIS.RES_TIER," +
                    "   APIS.SCOPE_NAME," +
                    "   DEF.PUBLISHED_DEFAULT_API_VERSION " +
                    "FROM" +
                    "   (" +
                    "      SELECT" +
                    "         API.API_ID," +
                    "         API.API_PROVIDER," +
                    "         API.API_NAME," +
                    "         API.API_TIER," +
                    "         API.API_VERSION," +
                    "         API.CONTEXT," +
                    "         API.API_TYPE," +
                    "         URL.URL_MAPPING_ID," +
                    "         URL.HTTP_METHOD," +
                    "         URL.AUTH_SCHEME," +
                    "         URL.URL_PATTERN," +
                    "         URL.THROTTLING_TIER AS RES_TIER," +
                    "         SCOPE.SCOPE_NAME " +
                    "      FROM" +
                    "         AM_API API," +
                    "         AM_API_URL_MAPPING URL " +
                    "         LEFT JOIN" +
                    "            AM_API_RESOURCE_SCOPE_MAPPING SCOPE " +
                    "            ON URL.URL_MAPPING_ID = SCOPE.URL_MAPPING_ID " +
                    "      WHERE" +
                    "         API.API_ID = URL.API_ID " +
                    "         AND CONTEXT LIKE ? " +
                    "      UNION ALL" +
                    "      SELECT" +
                    "         API.API_ID," +
                    "         API.API_PROVIDER," +
                    "         API.API_NAME," +
                    "         API.API_TIER," +
                    "         API.API_VERSION," +
                    "         API.CONTEXT," +
                    "         API.API_TYPE," +
                    "         URL.URL_MAPPING_ID," +
                    "         URL.HTTP_METHOD," +
                    "         URL.AUTH_SCHEME," +
                    "         URL.URL_PATTERN," +
                    "         URL.THROTTLING_TIER AS RES_TIER," +
                    "         SCOPE.SCOPE_NAME " +
                    "      FROM" +
                    "         AM_API API," +
                    "         AM_API_PRODUCT_MAPPING PROD," +
                    "         AM_API_URL_MAPPING URL " +
                    "         LEFT JOIN" +
                    "            AM_API_RESOURCE_SCOPE_MAPPING SCOPE " +
                    "            ON URL.URL_MAPPING_ID = SCOPE.URL_MAPPING_ID " +
                    "      WHERE" +
                    "         URL.URL_MAPPING_ID = PROD.URL_MAPPING_ID " +
                    "         AND API.API_ID = PROD.API_ID " +
                    "         AND CONTEXT LIKE ? " +
                    "   )" +
                    "    APIS " +
                    "   LEFT JOIN" +
                    "      AM_API_DEFAULT_VERSION DEF " +
                    "      ON APIS.API_NAME = DEF.API_NAME " +
                    "      AND APIS.API_PROVIDER = DEF.API_PROVIDER " +
                    "      AND APIS.API_VERSION = DEF.PUBLISHED_DEFAULT_API_VERSION";

    public static final String GET_ST_APIS_SQL =
            "SELECT" +
                    "   APIS.API_ID," +
                    "   APIS.API_PROVIDER," +
                    "   APIS.API_NAME," +
                    "   APIS.API_TIER," +
                    "   APIS.API_VERSION," +
                    "   APIS.CONTEXT," +
                    "   APIS.API_TYPE," +
                    "   APIS.URL_MAPPING_ID," +
                    "   APIS.HTTP_METHOD," +
                    "   APIS.AUTH_SCHEME," +
                    "   APIS.URL_PATTERN," +
                    "   APIS.RES_TIER," +
                    "   APIS.SCOPE_NAME," +
                    "   DEF.PUBLISHED_DEFAULT_API_VERSION " +
                    "FROM" +
                    "   (" +
                    "      SELECT" +
                    "         API.API_ID," +
                    "         API.API_PROVIDER," +
                    "         API.API_NAME," +
                    "         API.API_TIER," +
                    "         API.API_VERSION," +
                    "         API.CONTEXT," +
                    "         API.API_TYPE," +
                    "         URL.URL_MAPPING_ID," +
                    "         URL.HTTP_METHOD," +
                    "         URL.AUTH_SCHEME," +
                    "         URL.URL_PATTERN," +
                    "         URL.THROTTLING_TIER AS RES_TIER," +
                    "         SCOPE.SCOPE_NAME " +
                    "      FROM" +
                    "         AM_API API," +
                    "         AM_API_URL_MAPPING URL " +
                    "         LEFT JOIN" +
                    "            AM_API_RESOURCE_SCOPE_MAPPING SCOPE " +
                    "            ON URL.URL_MAPPING_ID = SCOPE.URL_MAPPING_ID " +
                    "      WHERE" +
                    "         API.API_ID = URL.API_ID " +
                    "         AND CONTEXT NOT LIKE ? " +
                    "      UNION" +
                    "      SELECT" +
                    "         API.API_ID," +
                    "         API.API_PROVIDER," +
                    "         API.API_NAME," +
                    "         API.API_TIER," +
                    "         API.API_VERSION," +
                    "         API.CONTEXT," +
                    "         API.API_TYPE," +
                    "         URL.URL_MAPPING_ID," +
                    "         URL.HTTP_METHOD," +
                    "         URL.AUTH_SCHEME," +
                    "         URL.URL_PATTERN," +
                    "         URL.THROTTLING_TIER AS RES_TIER," +
                    "         SCOPE.SCOPE_NAME " +
                    "      FROM" +
                    "         AM_API API," +
                    "         AM_API_PRODUCT_MAPPING PROD," +
                    "         AM_API_URL_MAPPING URL " +
                    "         LEFT JOIN" +
                    "            AM_API_RESOURCE_SCOPE_MAPPING SCOPE " +
                    "            ON URL.URL_MAPPING_ID = SCOPE.URL_MAPPING_ID " +
                    "      WHERE" +
                    "         URL.URL_MAPPING_ID = PROD.URL_MAPPING_ID " +
                    "         AND API.API_ID = PROD.API_ID " +
                    "         AND CONTEXT NOT LIKE ? " +
                    "   )" +
                    "    APIS " +
                    "   LEFT JOIN" +
                    "      AM_API_DEFAULT_VERSION DEF " +
                    "      ON APIS.API_NAME = DEF.API_NAME " +
                    "      AND APIS.API_PROVIDER = DEF.API_PROVIDER " +
                    "      AND APIS.API_VERSION = DEF.PUBLISHED_DEFAULT_API_VERSION";

    public static final String GET_API_SQL =
            "SELECT " +
                    "   APIS.API_ID," +
                    "   APIS.API_PROVIDER," +
                    "   APIS.API_NAME," +
                    "   APIS.API_TIER," +
                    "   APIS.API_VERSION," +
                    "   APIS.CONTEXT," +
                    "   APIS.API_TYPE," +
                    "   APIS.URL_MAPPING_ID," +
                    "   APIS.HTTP_METHOD," +
                    "   APIS.AUTH_SCHEME," +
                    "   APIS.URL_PATTERN," +
                    "   APIS.RES_TIER," +
                    "   APIS.SCOPE_NAME," +
                    "   DEF.PUBLISHED_DEFAULT_API_VERSION " +
                    "FROM " +
                    "   (" +
                    "      SELECT " +
                    "         API.API_ID," +
                    "         API.API_PROVIDER," +
                    "         API.API_NAME," +
                    "         API.API_TIER," +
                    "         API.API_VERSION," +
                    "         API.CONTEXT," +
                    "         API.API_TYPE," +
                    "         URL.URL_MAPPING_ID," +
                    "         URL.HTTP_METHOD," +
                    "         URL.AUTH_SCHEME," +
                    "         URL.URL_PATTERN," +
                    "         URL.THROTTLING_TIER AS RES_TIER," +
                    "         SCOPE.SCOPE_NAME" +
                    "      FROM " +
                    "         AM_API API," +
                    "         AM_API_URL_MAPPING URL" +
                    "         LEFT JOIN" +
                    "            AM_API_RESOURCE_SCOPE_MAPPING SCOPE" +
                    "            ON URL.URL_MAPPING_ID = SCOPE.URL_MAPPING_ID" +
                    "      WHERE" +
                    "         API.API_ID = URL.API_ID" +
                    "         AND API.API_VERSION = ?" +
                    "         AND API.CONTEXT = ?" +
                    "   )" +
                    "   APIS " +
                    "   LEFT JOIN " +
                    "      AM_API_DEFAULT_VERSION DEF" +
                    "      ON APIS.API_NAME = DEF.API_NAME" +
                    "      AND APIS.API_PROVIDER = DEF.API_PROVIDER" +
                    "      AND APIS.API_VERSION = DEF.PUBLISHED_DEFAULT_API_VERSION";

    //todo merge with above DET_API
    public static final String GET_API_PRODUCT_SQL =
            "SELECT " +
                    "   APIS.API_ID," +
                    "   APIS.API_PROVIDER," +
                    "   APIS.API_NAME," +
                    "   APIS.API_TIER," +
                    "   APIS.API_VERSION," +
                    "   APIS.CONTEXT," +
                    "   APIS.API_TYPE," +
                    "   APIS.URL_MAPPING_ID," +
                    "   APIS.HTTP_METHOD," +
                    "   APIS.AUTH_SCHEME," +
                    "   APIS.URL_PATTERN," +
                    "   APIS.RES_TIER," +
                    "   APIS.SCOPE_NAME," +
                    "   DEF.PUBLISHED_DEFAULT_API_VERSION " +
                    "FROM " +
                    "   (" +
                    "      SELECT " +
                    "         API.API_ID," +
                    "         API.API_PROVIDER," +
                    "         API.API_NAME," +
                    "         API.API_TIER," +
                    "         API.API_VERSION," +
                    "         API.CONTEXT," +
                    "         API.API_TYPE," +
                    "         URL.URL_MAPPING_ID," +
                    "         URL.HTTP_METHOD," +
                    "         URL.AUTH_SCHEME," +
                    "         URL.URL_PATTERN," +
                    "         URL.THROTTLING_TIER AS RES_TIER," +
                    "         SCOPE.SCOPE_NAME" +
                    "      FROM " +
                    "         AM_API API," +
                    "         AM_API_PRODUCT_MAPPING PROD," +
                    "         AM_API_URL_MAPPING URL" +
                    "         LEFT JOIN " +
                    "            AM_API_RESOURCE_SCOPE_MAPPING SCOPE" +
                    "            ON URL.URL_MAPPING_ID = SCOPE.URL_MAPPING_ID" +
                    "      WHERE " +
                    "         URL.URL_MAPPING_ID = PROD.URL_MAPPING_ID" +
                    "         AND API.API_ID = PROD.API_ID" +
                    "         AND API.API_VERSION = ?" +
                    "         AND API.CONTEXT = ?" +
                    "   )" +
                    "   APIS " +
                    "   LEFT JOIN " +
                    "      AM_API_DEFAULT_VERSION DEF " +
                    "      ON APIS.API_NAME = DEF.API_NAME" +
                    "      AND APIS.API_PROVIDER = DEF.API_PROVIDER" +
                    "      AND APIS.API_VERSION = DEF.PUBLISHED_DEFAULT_API_VERSION";

    public static final String GET_TENANT_AM_KEY_MAPPING_SQL =
            "SELECT " +
                    "   MAPPING.APPLICATION_ID," +
                    "   MAPPING.CONSUMER_KEY," +
                    "   MAPPING.KEY_TYPE," +
                    "   MAPPING.KEY_MANAGER," +
                    "   MAPPING.STATE" +
                    " FROM " +
                    "   AM_APPLICATION_KEY_MAPPING MAPPING," +
                    "   AM_APPLICATION APP," +
                    "   AM_SUBSCRIBER SUB" +
                    " WHERE " +
                    "   MAPPING.APPLICATION_ID = APP.APPLICATION_ID AND" +
                    "   APP.SUBSCRIBER_ID = SUB.SUBSCRIBER_ID AND" +
                    "   SUB.TENANT_ID = ?";

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

    public static final String GET_TENANT_API_URL_MAPPING_SQL =
            "SELECT " +
                    "   URL.URL_MAPPING_ID AS URL_MAPPING_ID," +
                    "   URL.API_ID AS API_ID," +
                    "   URL.HTTP_METHOD AS HTTP_METHOD," +
                    "   URL.AUTH_SCHEME AS AUTH_SCHEME," +
                    "   URL.THROTTLING_TIER AS POLICY" +
                    " FROM " +
                    "   AM_API_URL_MAPPING URL," +
                    "   AM_API API" +
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
                    " FROM " +
                    "   AM_API_URL_MAPPING URL," +
                    "   AM_API API" +
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
                    " FROM " +
                    "   AM_API_URL_MAPPING" +
                    " WHERE " +
                    "   URL_MAPPING_ID = ?";

    public static final String GET_API_PRODUCT_URL_MAPPING_SQL =
            "SELECT " +
                    "   URL.URL_MAPPING_ID," +
                    "   URL.AUTH_SCHEME," +
                    "   URL.URL_PATTERN," +
                    "   URL.HTTP_METHOD," +
                    "   URL.THROTTLING_TIER AS RES_TIER," +
                    "   SCOPE.SCOPE_NAME" +
                    " FROM " +
                    "   AM_API API," +
                    "   AM_API_PRODUCT_MAPPING PROD," +
                    "   AM_API_URL_MAPPING URL" +
                    " LEFT JOIN " +
                    "   AM_API_RESOURCE_SCOPE_MAPPING SCOPE" +
                    " ON " +
                    "   URL.URL_MAPPING_ID = SCOPE.URL_MAPPING_ID" +
                    " WHERE " +
                    "   URL.URL_MAPPING_ID = PROD.URL_MAPPING_ID AND " +
                    "   API.API_ID = PROD.API_ID AND" +
                    "   API.API_ID = ?";

}

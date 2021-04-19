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
                    "   ATTRIBUTES.VALUE AS ATTRIBUTE_VALUE" +
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
                    "   ATTRIBUTES.VALUE AS ATTRIBUTE_VALUE" +
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
                    "   AM_SUBSCRIPTION.UUID AS SUBSCRIPTION_UUID," +
                    "   AM_SUBSCRIPTION.SUBSCRIPTION_ID AS SUB_ID," +
                    "   AM_SUBSCRIPTION.TIER_ID AS TIER," +
                    "   AM_SUBSCRIPTION.API_ID AS API_ID," +
                    "   AM_SUBSCRIPTION.APPLICATION_ID AS APP_ID," +
                    "   AM_APPLICATION.UUID AS APPLICATION_UUID," +
                    "   AM_API.API_UUID AS API_UUID," +
                    "   AM_SUBSCRIPTION.SUB_STATUS AS STATUS" +
                    " FROM " +
                    "   AM_SUBSCRIPTION," +
                    "   AM_APPLICATION," +
                    "   AM_API" +
                    " WHERE " +
                    "AM_SUBSCRIPTION.APPLICATION_ID = AM_APPLICATION.APPLICATION_ID AND " +
                    "AM_SUBSCRIPTION.API_ID = AM_API.API_ID AND " +
                    "AM_SUBSCRIPTION.API_ID = ? AND AM_SUBSCRIPTION.APPLICATION_ID = ? ";

    public static final String GET_SUBSCRIPTION_APP_UUID_API_UUID_SQL =
            "SELECT " +
                    "   AM_SUBSCRIPTION.UUID AS SUBSCRIPTION_UUID," +
                    "   AM_SUBSCRIPTION.SUBSCRIPTION_ID AS SUB_ID," +
                    "   AM_SUBSCRIPTION.TIER_ID AS TIER," +
                    "   AM_SUBSCRIPTION.API_ID AS API_ID," +
                    "   AM_SUBSCRIPTION.APPLICATION_ID AS APP_ID," +
                    "   AM_APPLICATION.UUID AS APPLICATION_UUID," +
                    "   AM_API.API_UUID AS API_UUID," +
                    "   AM_SUBSCRIPTION.SUB_STATUS AS STATUS" +
                    " FROM " +
                    "   AM_SUBSCRIPTION," +
                    "   AM_APPLICATION," +
                    "   AM_API" +
                    " WHERE " +
                    "AM_SUBSCRIPTION.APPLICATION_ID = AM_APPLICATION.APPLICATION_ID AND " +
                    "AM_SUBSCRIPTION.API_ID = AM_API.API_ID AND " +
                    "AM_API.API_UUID = ? AND AM_APPLICATION.UUID = ? ";

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

    public static final String GET_ALL_AM_KEY_MAPPINGS_SQL =
            "SELECT " +
                    "   APPLICATION_ID," +
                    "   CONSUMER_KEY," +
                    "   KEY_TYPE," +
                    "   STATE" +
                    " FROM " +
                    "   AM_APPLICATION_KEY_MAPPING";

    public static final String GET_AM_KEY_MAPPING_BY_CONSUMER_KEY_SQL = "SELECT AM_APPLICATION.UUID, " +
            "AM_APPLICATION_KEY_MAPPING.APPLICATION_ID,AM_APPLICATION_KEY_MAPPING.CONSUMER_KEY," +
            "AM_APPLICATION_KEY_MAPPING.KEY_TYPE,AM_KEY_MANAGER.NAME AS KEY_MANAGER,AM_APPLICATION_KEY_MAPPING.STATE " +
            "FROM " +
            "AM_APPLICATION_KEY_MAPPING,AM_KEY_MANAGER,AM_APPLICATION WHERE AM_KEY_MANAGER" +
            ".UUID = AM_APPLICATION_KEY_MAPPING.KEY_MANAGER AND AM_APPLICATION_KEY_MAPPING" +
            ".APPLICATION_ID = AM_APPLICATION.APPLICATION_ID AND AM_APPLICATION_KEY_MAPPING.CONSUMER_KEY = ? AND " +
            "AM_KEY_MANAGER.NAME = ?  AND AM_KEY_MANAGER.TENANT_DOMAIN  = ? ";

    public static final String GET_TENANT_SUBSCRIPTIONS_SQL =
            "SELECT " +
                    "   SUBS.UUID AS SUBSCRIPTION_UUID," +
                    "   SUBS.SUBSCRIPTION_ID AS SUB_ID," +
                    "   SUBS.TIER_ID AS TIER," +
                    "   SUBS.API_ID AS API_ID," +
                    "   APP.APPLICATION_ID AS APP_ID," +
                    "   APP.UUID AS APPLICATION_UUID," +
                    "   API.API_UUID AS API_UUID," +
                    "   SUBS.SUB_STATUS AS STATUS," +
                    "   SUB.TENANT_ID AS TENANT_ID" +
                    " FROM " +
                    "   AM_SUBSCRIPTION SUBS," +
                    "   AM_APPLICATION APP," +
                    "   AM_API API," +
                    "   AM_SUBSCRIBER SUB" +
                    " WHERE " +
                    "   SUBS.API_ID = API.API_ID AND " +
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
                    "   APS.MAX_DEPTH AS MAX_DEPTH," +
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

    //todo merge with above DET_API
    public static final String GET_API_PRODUCT_SQL =
            "SELECT " +
                    "   APIS.API_UUID," +
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
                    "         API.API_UUID," +
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
            "SELECT APP.UUID,MAPPING.APPLICATION_ID, MAPPING.CONSUMER_KEY,MAPPING.KEY_TYPE,KEYM.NAME AS KEY_MANAGER," +
                    "MAPPING.STATE" +
                    " FROM " +
                    "   AM_APPLICATION_KEY_MAPPING MAPPING,AM_APPLICATION APP,AM_SUBSCRIBER SUB,AM_KEY_MANAGER KEYM" +
                    " WHERE " +
                    "   MAPPING.APPLICATION_ID = APP.APPLICATION_ID AND APP.SUBSCRIBER_ID = SUB.SUBSCRIBER_ID AND" +
                    "   MAPPING.KEY_MANAGER = KEYM.UUID AND SUB.TENANT_ID = ?";

    public static final String GET_ALL_GLOBAL_POLICIES_SQL =
            " SELECT " +
                    "   POLICY_ID," +
                    "   NAME," +
                    "   TENANT_ID," +
                    "   KEY_TEMPLATE," +
                    "   SIDDHI_QUERY" +
                    " FROM " +
                    "   AM_POLICY_GLOBAL";

    public static final String GET_TENANT_GLOBAL_POLICIES_SQL =
            " SELECT " +
                    "   POLICY_ID," +
                    "   NAME," +
                    "   TENANT_ID," +
                    "   KEY_TEMPLATE," +
                    "   SIDDHI_QUERY" +
                    " FROM " +
                    "   AM_POLICY_GLOBAL" +
                    " WHERE " +
                    "   TENANT_ID = ? ";

    public static final String GET_GLOBAL_POLICY_SQL =
            " SELECT " +
                    "   POLICY_ID," +
                    "   NAME," +
                    "   TENANT_ID," +
                    "   KEY_TEMPLATE," +
                    "   SIDDHI_QUERY" +
                    " FROM " +
                    "   AM_POLICY_GLOBAL" +
                    " WHERE " +
                    "   NAME = ? AND" +
                    "   TENANT_ID = ? ";

    public static final String GET_API_BY_UUID_SQL =
            "SELECT " +
                    "AM_API.API_PROVIDER,AM_API.API_NAME,AM_API.CONTEXT,AM_API.API_UUID,AM_API.API_ID,AM_API" +
                    ".API_TIER,AM_API.API_VERSION,AM_API.API_TYPE,AM_API.STATUS,AM_REVISION.REVISION_UUID AS " +
                    "REVISION_UUID,AM_DEPLOYMENT_REVISION_MAPPING.NAME AS DEPLOYMENT_NAME " +
                    "FROM " +
                    "AM_API LEFT JOIN AM_REVISION ON AM_API.API_UUID = AM_REVISION.API_UUID " +
                    "LEFT JOIN AM_DEPLOYMENT_REVISION_MAPPING " +
                    "ON AM_REVISION.REVISION_UUID=AM_DEPLOYMENT_REVISION_MAPPING.REVISION_UUID " +
                    "WHERE AM_API.API_UUID = ? ";

    public static final String GET_DEFAULT_VERSION_API_SQL = "SELECT PUBLISHED_DEFAULT_API_VERSION FROM " +
            "AM_API_DEFAULT_VERSION WHERE API_NAME = ? AND API_PROVIDER = ? AND PUBLISHED_DEFAULT_API_VERSION = ?";

    public static final String GET_URI_TEMPLATES_BY_API_SQL = "SELECT AM_API_URL_MAPPING.HTTP_METHOD," +
            "AM_API_URL_MAPPING.AUTH_SCHEME,AM_API_URL_MAPPING.URL_PATTERN,AM_API_URL_MAPPING.THROTTLING_TIER," +
            "AM_API_RESOURCE_SCOPE_MAPPING.SCOPE_NAME FROM AM_API_URL_MAPPING LEFT JOIN AM_API_RESOURCE_SCOPE_MAPPING" +
            " ON AM_API_URL_MAPPING.URL_MAPPING_ID=AM_API_RESOURCE_SCOPE_MAPPING.URL_MAPPING_ID WHERE " +
            "AM_API_URL_MAPPING.API_ID = ? AND AM_API_URL_MAPPING.REVISION_UUID = ?";

    public static final String GET_ALL_APIS_BY_ORGANIZATION = "SELECT AM_API.API_PROVIDER,AM_API.API_NAME,AM_API" +
            ".CONTEXT,AM_API.API_UUID,AM_API.API_ID,AM_API.API_VERSION,AM_API.API_TYPE,AM_API.STATUS,AM_REVISION" +
            ".REVISION_UUID AS REVISION_UUID,AM_DEPLOYMENT_REVISION_MAPPING.NAME AS DEPLOYMENT_NAME FROM AM_API LEFT " +
            "JOIN AM_REVISION ON AM_API.API_UUID = AM_REVISION.API_UUID LEFT JOIN  AM_DEPLOYMENT_REVISION_MAPPING " +
            "ON AM_REVISION.REVISION_UUID = AM_DEPLOYMENT_REVISION_MAPPING.REVISION_UUID ";
    public static final String GET_ALL_APIS_BY_ORGANIZATION_AND_DEPLOYMENT_SQL = "SELECT AM_API.API_PROVIDER,AM_API" +
            ".API_NAME,AM_API.CONTEXT,AM_API.API_UUID,AM_API.API_ID,AM_API.API_TIER,AM_API.API_VERSION,AM_API" +
            ".API_TYPE,AM_API.STATUS,AM_REVISION.REVISION_UUID AS REVISION_UUID,AM_DEPLOYMENT_REVISION_MAPPING.NAME " +
            "AS DEPLOYMENT_NAME FROM AM_API LEFT JOIN AM_REVISION ON AM_API.API_UUID=AM_REVISION.API_UUID LEFT JOIN " +
            "AM_DEPLOYMENT_REVISION_MAPPING ON AM_REVISION.REVISION_UUID=AM_DEPLOYMENT_REVISION_MAPPING.REVISION_UUID ";

    public static final String  GET_ALL_API_PRODUCT_URI_TEMPLATES_SQL = "SELECT AM_API_URL_MAPPING.URL_MAPPING_ID," +
            "AM_API_URL_MAPPING.HTTP_METHOD,AM_API_URL_MAPPING.AUTH_SCHEME,AM_API_URL_MAPPING.URL_PATTERN," +
            "AM_API_URL_MAPPING.THROTTLING_TIER,AM_API_RESOURCE_SCOPE_MAPPING.SCOPE_NAME FROM AM_API_URL_MAPPING LEFT" +
            " JOIN AM_API_RESOURCE_SCOPE_MAPPING ON AM_API_URL_MAPPING.URL_MAPPING_ID=AM_API_RESOURCE_SCOPE_MAPPING" +
            ".URL_MAPPING_ID WHERE AM_API_URL_MAPPING.URL_MAPPING_ID IN (SELECT URL_MAPPING_ID FROM " +
            "AM_API_PRODUCT_MAPPING WHERE API_ID = ? )";
    public static final String  GET_API_BY_CONTEXT_AND_VERSION_SQL = "SELECT AM_API.API_PROVIDER,AM_API.API_NAME," +
            "AM_API.CONTEXT,AM_API.API_UUID,AM_API.API_ID,AM_API.API_TIER,AM_API.API_VERSION,AM_API.API_TYPE,AM_API" +
            ".STATUS,AM_REVISION.REVISION_UUID AS REVISION_UUID,AM_DEPLOYMENT_REVISION_MAPPING.NAME AS " +
            "DEPLOYMENT_NAME FROM AM_API LEFT JOIN AM_REVISION ON AM_API.API_UUID=AM_REVISION.API_UUID LEFT JOIN " +
            "AM_DEPLOYMENT_REVISION_MAPPING ON AM_REVISION.REVISION_UUID=AM_DEPLOYMENT_REVISION_MAPPING.REVISION_UUID" +
            " WHERE AM_API.CONTEXT = ? AND AM_API.API_VERSION= ?";
}

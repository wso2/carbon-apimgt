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
package org.wso2.carbon.apimgt.api;

public class InMemorySubscriptionValidationConstants {

    public static final String GET_ALL_APPLICATIONS_SQL =
            " SELECT " +
                    "   APP.APPLICATION_ID AS APP_ID," +
                    "   APP.NAME AS NAME," +
                    "   APP.APPLICATION_TIER AS TIER," +
                    "   APP.TOKEN_TYPE AS TOKEN_TYPE," +
                    "   APP.GROUP_ID AS GROUP_ID," +
                    "   SUB.SUBSCRIBER_ID AS SUB_ID," +
                    "   SUB.TENANT_ID AS TENANT_ID" +
                    " FROM " +
                    "   AM_APPLICATION AS APP," +
                    "   AM_SUBSCRIBER AS SUB" +
                    " WHERE " +
                    "   APP.SUBSCRIBER_ID = SUB.SUBSCRIBER_ID ";

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
                    "   SUBSCRIPTION_ID = ? ";

    public static final String GET_ALL_SUBSCRIPTION_POLICIES_SQL =
            "SELECT " +
                    "   POLICY_ID," +
                    "   NAME," +
                    "   RATE_LIMIT_COUNT," +
                    "   RATE_LIMIT_TIME_UNIT," +
                    "   QUOTA_TYPE," +
                    "   STOP_ON_QUOTA_REACH, " +
                    "   TENANT_ID " +
                    "FROM " +
                    "   AM_POLICY_SUBSCRIPTION";

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
                    " API_ID," +
                    " API_PROVIDER," +
                    " API_NAME," +
                    " API_TIER," +
                    " API_VERSION," +
                    " CONTEXT " +
                    " FROM " +
                    "   AM_API";

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

    public static final String GET_AM_KEY_MAPPING_BY_CONSUMERKEY_SQL =
            "SELECT " +
                    "   APPLICATION_ID," +
                    "   CONSUMER_KEY," +
                    "   KEY_TYPE," +
                    "   STATE " +
                    " FROM " +
                    "   AM_APPLICATION_KEY_MAPPING" +
                    " WHERE " +
                    "CONSUMER_KEY = ? ";

    public static final String GET_TENANT_APPLICATIONS_SQL =
            " SELECT " +
                    "   APP.APPLICATION_ID AS APP_ID," +
                    "   APP.NAME AS NAME," +
                    "   APP.APPLICATION_TIER AS TIER," +
                    "   APP.TOKEN_TYPE AS TOKEN_TYPE," +
                    "   APP.GROUP_ID AS GROUP_ID," +
                    "   SUB.SUBSCRIBER_ID AS SUB_ID," +
                    "   SUB.TENANT_ID AS TENANT_ID" +
                    " FROM " +
                    "   AM_APPLICATION AS APP," +
                    "   AM_SUBSCRIBER AS SUB" +
                    " WHERE " +
                    "   APP.SUBSCRIBER_ID = SUB.SUBSCRIBER_ID " +
                    "   AND SUB.TENANT_ID = ?";

    public static final String GET_APPLICATION_BY_ID_SQL =
            " SELECT " +
                    "   APPLICATION_ID," +
                    "   NAME," +
                    "   APPLICATION_TIER," +
                    "   TOKEN_TYPE," +
                    "   SUBSCRIBER_ID," +
                    "   GROUP_ID" +
                    " FROM " +
                    "   AM_APPLICATION" +
                    " WHERE " +
                    "   APPLICATION_ID = ? ";

    public static final String GET_TENANT_SUBSCRIPTIONS_SQL =
            "SELECT " +
                    "   SUBS.SUBSCRIPTION_ID AS SUB_ID," +
                    "   SUBS.TIER_ID AS TIER," +
                    "   SUBS.API_ID AS API_ID," +
                    "   SUBS.APPLICATION_ID AS APP_ID," +
                    "   SUBS.SUB_STATUS AS STATUS," +
                    "   SUB.TENANT_ID AS TENANT_ID" +
                    " FROM " +
                    "   AM_SUBSCRIPTION AS SUBS," +
                    "   AM_APPLICATION AS APP," +
                    "   AM_SUBSCRIBER AS SUB" +
                    " WHERE " +
                    "   APP.SUBSCRIBER_ID = SUB.SUBSCRIBER_ID AND " +
                    "   SUBS.SUBSCRIPTION_ID = APP.SUBSCRIPTION_ID AND " +
                    "   SUB.TENANT_ID = ? ";

    public static final String GET_TENANT_SUBSCRIPTION_POLICIES_SQL =
            "SELECT " +
                    "   POLICY_ID," +
                    "   NAME," +
                    "   RATE_LIMIT_COUNT," +
                    "   RATE_LIMIT_TIME_UNIT," +
                    "   QUOTA_TYPE," +
                    "   STOP_ON_QUOTA_REACH, " +
                    "   TENANT_ID " +
                    "FROM " +
                    "   AM_POLICY_SUBSCRIPTION" +
                    " WHERE " +
                    "   TENANT_ID = ? ";
    public static final String GET_SUBSCRIPTION_POLICY_BY_ID_SQL =
            "SELECT " +
                    "   POLICY_ID," +
                    "   NAME," +
                    "   RATE_LIMIT_COUNT," +
                    "   RATE_LIMIT_TIME_UNIT," +
                    "   QUOTA_TYPE," +
                    "   STOP_ON_QUOTA_REACH, " +
                    "   TENANT_ID " +
                    "FROM " +
                    "   AM_POLICY_SUBSCRIPTION" +
                    " WHERE " +
                    "   POLICY_ID = ? ";

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

    public static final String GET_APPLICATION_POLICY_SQL =
            "SELECT " +
                    "   POLICY_ID," +
                    "   NAME," +
                    "   QUOTA_TYPE," +
                    "   TENANT_ID " +
                    "FROM " +
                    "   AM_POLICY_APPLICATION" +
                    " WHERE " +
                    "   POLICY_ID = ? ";

    public static final String GET_TENANT_APIS_SQL =
            "SELECT " +
                    " API_ID," +
                    " API_PROVIDER," +
                    " API_NAME," +
                    " API_TIER," +
                    " API_VERSION," +
                    " CONTEXT " +
                    " FROM " +
                    "   AM_API";

    public static final String GET_API_SQL =
            "SELECT " +
                    " API_ID," +
                    " API_PROVIDER," +
                    " API_NAME," +
                    " API_TIER," +
                    " API_VERSION," +
                    " CONTEXT " +
                    " FROM " +
                    "   AM_API" +
                    " WHERE " +
                    "API_ID = ?";

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

    public static final String DELEM_PERIOD = ".";

    public static final int retrievalTimeoutInSeconds = 15;

    public static final int retrievalRetries = 15;

    public static final String UTF8 = "UTF-8";

    public static enum POLICY_TYPE {
        SUBSCRIPTION,
        APPLICATION
    }

}

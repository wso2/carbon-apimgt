/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl.dao.constants;

import org.wso2.carbon.apimgt.api.model.policy.PolicyConstants;
import org.wso2.carbon.apimgt.impl.APIConstants;

public class SQLConstants {
    public static final String GET_API_FOR_CONTEXT_TEMPLATE_SQL =
            " SELECT " +
            "   API.API_NAME," +
            "   API.API_PROVIDER" +
            " FROM " +
            "   AM_API API" +
            " WHERE " +
            "   API.CONTEXT_TEMPLATE = ? ";

    public static final String GET_VERSIONS_MATCHES_API_NAME_SQL=
            "SELECT API_VERSION FROM AM_API WHERE API_NAME = ? AND API_PROVIDER = ?";

    public static final String GET_USER_ID_FROM_CONSUMER_KEY_SQL =
            " SELECT " +
            "   SUBS.USER_ID " +
            " FROM " +
            "   AM_SUBSCRIBER SUBS, " +
            "   AM_APPLICATION APP, " +
            "   AM_APPLICATION_KEY_MAPPING MAP " +
            " WHERE " +
            "   APP.SUBSCRIBER_ID   = SUBS.SUBSCRIBER_ID " +
            "   AND MAP.APPLICATION_ID = APP.APPLICATION_ID " +
            "   AND MAP.CONSUMER_KEY   = ? ";

    public static final String GET_APPLICATION_REGISTRATION_SQL =
            " SELECT REG_ID FROM AM_APPLICATION_REGISTRATION WHERE SUBSCRIBER_ID = ? AND APP_ID = ? AND TOKEN_TYPE = " +
                    "? AND KEY_MANAGER = ?";

    public static final String ADD_APPLICATION_REGISTRATION_SQL =
            " INSERT INTO " +
            "   AM_APPLICATION_REGISTRATION (SUBSCRIBER_ID,WF_REF,APP_ID,TOKEN_TYPE,ALLOWED_DOMAINS," +
            "VALIDITY_PERIOD,TOKEN_SCOPE,INPUTS,KEY_MANAGER) " +
            " VALUES(?,?,?,?,?,?,?,?,?)";

    public static final String ADD_APPLICATION_KEY_MAPPING_SQL =
            " INSERT INTO " +
            "   AM_APPLICATION_KEY_MAPPING (APPLICATION_ID,KEY_TYPE,STATE,KEY_MANAGER,UUID,CREATE_MODE) " +
            " VALUES(?,?,?,?,?,?)";

    public static final String GET_OAUTH_APPLICATION_SQL =
            " SELECT CONSUMER_SECRET, USERNAME, TENANT_ID, APP_NAME, APP_NAME, CALLBACK_URL, GRANT_TYPES " +
            " FROM IDN_OAUTH_CONSUMER_APPS " +
            " WHERE CONSUMER_KEY = ?";

    public static final String GET_OWNER_FOR_CONSUMER_APP_SQL =
            " SELECT USERNAME, USER_DOMAIN, TENANT_ID " +
            " FROM IDN_OAUTH_CONSUMER_APPS " +
            " WHERE CONSUMER_KEY = ?";

    public static final String GET_SUBSCRIBED_APIS_OF_USER_SQL =
            " SELECT " +
            "   API.API_PROVIDER AS API_PROVIDER," +
            "   API.API_NAME AS API_NAME," +
            "   API.CONTEXT AS API_CONTEXT, " +
            "   API.API_VERSION AS API_VERSION, " +
            "   SP.TIER_ID AS SP_TIER_ID " +
            " FROM " +
            "   AM_SUBSCRIPTION SP, " +
            "   AM_API API," +
            "   AM_SUBSCRIBER SB, " +
            "   AM_APPLICATION APP " +
            " WHERE " +
            "   SB.USER_ID = ? " +
            "   AND SB.TENANT_ID = ? " +
            "   AND SB.SUBSCRIBER_ID = APP.SUBSCRIBER_ID " +
            "   AND APP.APPLICATION_ID=SP.APPLICATION_ID " +
            "   AND API.API_ID = SP.API_ID" +
            "   AND SP.SUBS_CREATE_STATE = '" + APIConstants.SubscriptionCreatedStatus.SUBSCRIBE + "'";

    public static final String GET_SUBSCRIBED_APIS_OF_USER_CASE_INSENSITIVE_SQL =
            " SELECT " +
            "   API.API_PROVIDER AS API_PROVIDER," +
            "   API.API_NAME AS API_NAME," +
            "   API.CONTEXT AS API_CONTEXT," +
            "   API.API_VERSION AS API_VERSION, " +
            "   SP.TIER_ID AS SP_TIER_ID " +
            " FROM " +
            "   AM_SUBSCRIPTION SP, " +
            "   AM_API API," +
            "   AM_SUBSCRIBER SB, " +
            "   AM_APPLICATION APP " +
            " WHERE " +
            "   LOWER(SB.USER_ID) = LOWER(?) " +
            "   AND SB.TENANT_ID = ? " +
            "   AND SB.SUBSCRIBER_ID = APP.SUBSCRIBER_ID " +
            "   AND APP.APPLICATION_ID=SP.APPLICATION_ID " +
            "   AND API.API_ID = SP.API_ID" +
            "   AND SP.SUBS_CREATE_STATE = '" +APIConstants.SubscriptionCreatedStatus.SUBSCRIBE + "'";

    public static final String GET_SUBSCRIBED_API_IDs_BY_APP_ID_SQL =
            " SELECT " +
                    "   API.API_ID " +
                    " FROM " +
                    "   AM_SUBSCRIBER SUB," +
                    "   AM_APPLICATION APP, " +
                    "   AM_SUBSCRIPTION SUBS, " +
                    "   AM_API API " +
                    " WHERE " +
                    "   SUB.TENANT_ID = ? " +
                    "   AND SUB.SUBSCRIBER_ID=APP.SUBSCRIBER_ID " +
                    "   AND APP.APPLICATION_ID=SUBS.APPLICATION_ID " +
                    "   AND API.API_ID=SUBS.API_ID" +
                    "   AND APP.APPLICATION_ID= ? " +
                    "   AND SUBS.SUBS_CREATE_STATE = '" + APIConstants.SubscriptionCreatedStatus.SUBSCRIBE + "'";

    public static final String GET_INCLUDED_APIS_IN_PRODUCT_SQL =
            "SELECT "
                    + "DISTINCT API_ID "
                    + "FROM AM_API_URL_MAPPING "
                    + "WHERE URL_MAPPING_ID IN "
                    + "(SELECT URL_MAPPING_ID FROM AM_API_PRODUCT_MAPPING WHERE API_ID = ?)";

    public static final String GET_SUBSCRIBED_APIS_OF_USER_BY_APP_SQL =
            " SELECT " +
                    "   API.API_PROVIDER AS API_PROVIDER," +
                    "   API.API_NAME AS API_NAME," +
                    "   API.CONTEXT AS API_CONTEXT, " +
                    "   API.API_VERSION AS API_VERSION, " +
                    "   SP.TIER_ID AS SP_TIER_ID " +
                    " FROM " +
                    "   AM_SUBSCRIPTION SP, " +
                    "   AM_API API," +
                    "   AM_SUBSCRIBER SB, " +
                    "   AM_APPLICATION APP " +
                    " WHERE " +
                    "   SB.TENANT_ID = ? " +
                    "   AND SB.SUBSCRIBER_ID = APP.SUBSCRIBER_ID " +
                    "   AND APP.APPLICATION_ID=SP.APPLICATION_ID " +
                    "   AND API.API_ID = SP.API_ID" +
                    "   AND (SP.SUB_STATUS = '" + APIConstants.SubscriptionStatus.UNBLOCKED +
                    "' OR SP.SUB_STATUS = '" + APIConstants.SubscriptionStatus.TIER_UPDATE_PENDING +
                    "' OR SP.SUB_STATUS = '" + APIConstants.SubscriptionStatus.PROD_ONLY_BLOCKED + "')" +
                    "   AND SP.SUBS_CREATE_STATE = '" + APIConstants.SubscriptionCreatedStatus.SUBSCRIBE + "'" +
                    "   AND APP.APPLICATION_ID = ?";

    public static final String GET_SUBSCRIBED_APIS_OF_USER_BY_APP_CASE_INSENSITIVE_SQL =
            " SELECT " +
                    "   API.API_PROVIDER AS API_PROVIDER," +
                    "   API.API_NAME AS API_NAME," +
                    "   API.CONTEXT AS API_CONTEXT," +
                    "   API.API_VERSION AS API_VERSION, " +
                    "   SP.TIER_ID AS SP_TIER_ID " +
                    " FROM " +
                    "   AM_SUBSCRIPTION SP, " +
                    "   AM_API API," +
                    "   AM_SUBSCRIBER SB, " +
                    "   AM_APPLICATION APP " +
                    " WHERE " +
                    "   SB.TENANT_ID = ? " +
                    "   AND SB.SUBSCRIBER_ID = APP.SUBSCRIBER_ID " +
                    "   AND APP.APPLICATION_ID=SP.APPLICATION_ID " +
                    "   AND API.API_ID = SP.API_ID" +
                    "   AND (SP.SUB_STATUS = '" + APIConstants.SubscriptionStatus.UNBLOCKED +
                    "' OR SP.SUB_STATUS = '" + APIConstants.SubscriptionStatus.TIER_UPDATE_PENDING +
                    "' OR SP.SUB_STATUS = '" + APIConstants.SubscriptionStatus.PROD_ONLY_BLOCKED + "')" +
                    "   AND SP.SUBS_CREATE_STATE = '" + APIConstants.SubscriptionCreatedStatus.SUBSCRIBE + "'" +
                    "   AND APP.APPLICATION_ID = ?";

    public static final String GET_SUBSCRIBED_USERS_FOR_API_SQL =
            " SELECT " +
            "   SB.USER_ID, " +
            "   SB.TENANT_ID " +
            " FROM " +
            "   AM_SUBSCRIBER SB, " +
            "   AM_APPLICATION APP, " +
            "   AM_SUBSCRIPTION SP, " +
            "   AM_API API " +
            " WHERE " +
            "   API.API_PROVIDER = ? " +
            "   AND API.API_NAME = ?" +
            "   AND API.API_VERSION = ?" +
            "   AND SP.APPLICATION_ID = APP.APPLICATION_ID " +
            "   AND APP.SUBSCRIBER_ID=SB.SUBSCRIBER_ID " +
            "   AND API.API_ID = SP.API_ID" +
            "   AND SP.SUBS_CREATE_STATE = '" + APIConstants.SubscriptionCreatedStatus.SUBSCRIBE + "'";

    public static final String CHANGE_ACCESS_TOKEN_STATUS_PREFIX = "UPDATE ";

    public static final String CHANGE_ACCESS_TOKEN_STATUS_DEFAULT_SUFFIX =
            "   IAT , AM_SUBSCRIBER SB," +
            "   AM_SUBSCRIPTION SP , AM_APPLICATION APP," +
            "   AM_API API" +
            " SET " +
            "   IAT.TOKEN_STATE=? " +
            " WHERE " +
            "   SB.USER_ID=?" +
            "   AND SB.TENANT_ID=?" +
            "   AND API.API_PROVIDER=?" +
            "   AND API.API_NAME=?" +
            "   AND API.API_VERSION=?" +
            "   AND SP.ACCESS_TOKEN=IAT.ACCESS_TOKEN" +
            "   AND SB.SUBSCRIBER_ID=APP.SUBSCRIBER_ID" +
            "   AND APP.APPLICATION_ID = SP.APPLICATION_ID" +
            "   AND API.API_ID = SP.API_ID";

    public static final String CHANGE_ACCESS_TOKEN_STATUS_CASE_INSENSITIVE_SUFFIX =
            "   IAT , AM_SUBSCRIBER SB," +
            "   AM_SUBSCRIPTION SP , " +
            "   AM_APPLICATION APP, AM_API API " +
            " SET" +
            "   IAT.TOKEN_STATE=? " +
            " WHERE " +
            "   LOWER(SB.USER_ID)=LOWER(?)" +
            "   AND SB.TENANT_ID=?" +
            "   AND API.API_PROVIDER=?" +
            "   AND API.API_NAME=?" +
            "   AND API.API_VERSION=?" +
            "   AND SP.ACCESS_TOKEN=IAT.ACCESS_TOKEN" +
            "   AND SB.SUBSCRIBER_ID=APP.SUBSCRIBER_ID" +
            "   AND APP.APPLICATION_ID = SP.APPLICATION_ID" +
            "   AND API.API_ID = SP.API_ID";

    public static final String VALIDATE_KEY_SQL_PREFIX =
            " SELECT " +
            "   IAT.VALIDITY_PERIOD, " +
            "   IAT.TIME_CREATED ," +
            "   IAT.TOKEN_STATE," +
            "   IAT.USER_TYPE," +
            "   IAT.AUTHZ_USER," +
            "   IAT.USER_DOMAIN," +
            "   IAT.TIME_CREATED," +
            "   ISAT.TOKEN_SCOPE," +
            "   SUB.TIER_ID," +
            "   SUBS.USER_ID," +
            "   SUB.SUB_STATUS," +
            "   APP.APPLICATION_ID," +
            "   APP.NAME," +
            "   APP.APPLICATION_TIER," +
            "   AKM.KEY_TYPE," +
            "   API.API_NAME," +
            "   AKM.CONSUMER_KEY," +
            "   API.API_PROVIDER" +
            " FROM ";

    public static final String VALIDATE_SUBSCRIPTION_KEY_DEFAULT_SQL =
            " SELECT " +
            "   SUB.TIER_ID," +
            "   SUBS.USER_ID," +
            "   SUB.SUB_STATUS," +
            "   APP.APPLICATION_ID," +
            "   APP.NAME," +
            "   APP.APPLICATION_TIER," +
            "   APP.TOKEN_TYPE," +
            "   AKM.KEY_TYPE," +
            "   API.API_NAME," +
            "   API.API_PROVIDER " +
            " FROM " +
            "   AM_SUBSCRIPTION SUB," +
            "   AM_SUBSCRIBER SUBS," +
            "   AM_APPLICATION APP," +
            "   AM_APPLICATION_KEY_MAPPING AKM," +
            "   AM_API API " +
            " WHERE " +
            "   API.CONTEXT = ? " +
            "   AND AKM.CONSUMER_KEY = ? " +
            "   AND AKM.KEY_MANAGER = ? " +
            "   AND SUB.APPLICATION_ID = APP.APPLICATION_ID" +
            "   AND APP.SUBSCRIBER_ID = SUBS.SUBSCRIBER_ID" +
            "   AND API.API_ID = SUB.API_ID" +
            "   AND AKM.APPLICATION_ID=APP.APPLICATION_ID";

    public static final String VALIDATE_SUBSCRIPTION_KEY_VERSION_SQL =
            " SELECT " +
            "   SUB.TIER_ID," +
            "   SUBS.USER_ID," +
            "   SUB.SUB_STATUS," +
            "   APP.APPLICATION_ID," +
            "   APP.NAME," +
            "   APP.APPLICATION_TIER," +
            "   APP.TOKEN_TYPE," +
            "   AKM.KEY_TYPE," +
            "   API.API_NAME," +
            "   API.API_PROVIDER" +
            " FROM " +
            "   AM_SUBSCRIPTION SUB," +
            "   AM_SUBSCRIBER SUBS," +
            "   AM_APPLICATION APP," +
            "   AM_APPLICATION_KEY_MAPPING AKM," +
            "   AM_API API" +
            " WHERE " +
            "   API.CONTEXT = ? " +
            "   AND AKM.CONSUMER_KEY = ? " +
            "   AND AKM.KEY_MANAGER = ? " +
            "   AND API.API_VERSION = ? " +
            "   AND SUB.APPLICATION_ID = APP.APPLICATION_ID" +
            "   AND APP.SUBSCRIBER_ID = SUBS.SUBSCRIBER_ID" +
            "   AND API.API_ID = SUB.API_ID" +
            "   AND AKM.APPLICATION_ID=APP.APPLICATION_ID";

    public static final String ADVANCED_VALIDATE_SUBSCRIPTION_KEY_DEFAULT_SQL =
            " SELECT " +
                    "   SUB.TIER_ID," +
                    "   SUBS.USER_ID," +
                    "   SUB.SUB_STATUS," +
                    "   APP.APPLICATION_ID," +
                    "   APP.NAME," +
                    "   APP.APPLICATION_TIER," +
                    "   APP.TOKEN_TYPE," +
                    "   AKM.KEY_TYPE," +
                    "   API.API_NAME," +
                    "   API.API_TIER," +
                    "   API.API_PROVIDER," +
                    "   APS.RATE_LIMIT_COUNT," +
                    "   APS.RATE_LIMIT_TIME_UNIT," +
                    "   APS.STOP_ON_QUOTA_REACH," +
                    "   API.API_ID," +
                    "   APS.MAX_DEPTH,"+
                    "   APS.MAX_COMPLEXITY" +
                    " FROM " +
                    "   AM_SUBSCRIPTION SUB," +
                    "   AM_SUBSCRIBER SUBS," +
                    "   AM_APPLICATION APP," +
                    "   AM_APPLICATION_KEY_MAPPING AKM," +
                    "   AM_API API," +
                    "   AM_POLICY_SUBSCRIPTION APS" +
                    " WHERE " +
                    "   API.CONTEXT = ? " +
                    "   AND AKM.CONSUMER_KEY = ? " +
                    "   AND AKM.KEY_MANAGER = ? " +
                    "   AND SUB.APPLICATION_ID = APP.APPLICATION_ID" +
                    "   AND APP.SUBSCRIBER_ID = SUBS.SUBSCRIBER_ID" +
                    "   AND API.API_ID = SUB.API_ID" +
                    "   AND AKM.APPLICATION_ID=APP.APPLICATION_ID" +
                    "   AND APS.NAME = SUB.TIER_ID" +
                    "   AND APS.TENANT_ID = ? ";

    public static final String ADVANCED_VALIDATE_SUBSCRIPTION_KEY_VERSION_SQL =
            " SELECT " +
                    "   SUB.TIER_ID," +
                    "   SUBS.USER_ID," +
                    "   SUB.SUB_STATUS," +
                    "   APP.APPLICATION_ID," +
                    "   APP.NAME," +
                    "   APP.APPLICATION_TIER," +
                    "   APP.TOKEN_TYPE," +
                    "   AKM.KEY_TYPE," +
                    "   API.API_NAME," +
                    "   API.API_TIER," +
                    "   API.API_PROVIDER," +
                    "   APS.RATE_LIMIT_COUNT," +
                    "   APS.RATE_LIMIT_TIME_UNIT," +
                    "   APS.STOP_ON_QUOTA_REACH," +
                    "   API.API_ID," +
                    "   APS.MAX_DEPTH,"+
                    "   APS.MAX_COMPLEXITY" +
                    " FROM " +
                    "   AM_SUBSCRIPTION SUB," +
                    "   AM_SUBSCRIBER SUBS," +
                    "   AM_APPLICATION APP," +
                    "   AM_APPLICATION_KEY_MAPPING AKM," +
                    "   AM_API API," +
                    "   AM_POLICY_SUBSCRIPTION APS" +
                    " WHERE " +
                    "   API.CONTEXT = ? " +
                    "   AND AKM.CONSUMER_KEY = ? " +
                    "   AND AKM.KEY_MANAGER = ? " +
                    "   AND APS.TENANT_ID = ? " +
                    "   AND API.API_VERSION = ? " +
                    "   AND SUB.APPLICATION_ID = APP.APPLICATION_ID" +
                    "   AND APP.SUBSCRIBER_ID = SUBS.SUBSCRIBER_ID" +
                    "   AND API.API_ID = SUB.API_ID" +
                    "   AND AKM.APPLICATION_ID=APP.APPLICATION_ID" +
                    "   AND APS.NAME = SUB.TIER_ID";

    public static final String ADD_SUBSCRIBER_SQL =
            " INSERT" +
            "   INTO AM_SUBSCRIBER (USER_ID, TENANT_ID, EMAIL_ADDRESS, DATE_SUBSCRIBED, CREATED_BY, CREATED_TIME, " +
                    "UPDATED_TIME) " +
            " VALUES (?,?,?,?,?,?,?)";

    public static final String ADD_MONETIZATION_USAGE_PUBLISH_INFO =
            " INSERT INTO AM_MONETIZATION_USAGE (ID, STATE, STATUS, STARTED_TIME, PUBLISHED_TIME) VALUES (?,?,?,?,?)";

    public static final String UPDATE_MONETIZATION_USAGE_PUBLISH_INFO =
            " UPDATE AM_MONETIZATION_USAGE SET STATE = ?, STATUS = ?, STARTED_TIME = ?, PUBLISHED_TIME = ?" +
                    " WHERE ID = ?";

    public static final String GET_MONETIZATION_USAGE_PUBLISH_INFO =
            " SELECT ID, STATE, STATUS, STARTED_TIME, PUBLISHED_TIME FROM AM_MONETIZATION_USAGE";

    public static final String UPDATE_SUBSCRIBER_SQL =
            " UPDATE AM_SUBSCRIBER " +
            " SET" +
            "   USER_ID=?," +
            "   TENANT_ID=?," +
            "   EMAIL_ADDRESS=?," +
            "   DATE_SUBSCRIBED=?," +
            "   UPDATED_BY=?," +
            "   UPDATED_TIME=? " +
            " WHERE" +
            "   SUBSCRIBER_ID=?";

    public static final String GET_SUBSCRIBER_SQL =
            " SELECT " +
            "   USER_ID, TENANT_ID, EMAIL_ADDRESS, DATE_SUBSCRIBED " +
            " FROM " +
            "   AM_SUBSCRIBER " +
            " WHERE " +
            "   SUBSCRIBER_ID=?";

    public static final String CHECK_EXISTING_SUBSCRIPTION_API_SQL =
            " SELECT " +
            "   SUB_STATUS, SUBS_CREATE_STATE " +
            " FROM " +
            "   AM_SUBSCRIPTION " +
            " WHERE " +
            "   API_ID = ? " +
            "   AND APPLICATION_ID = ?";

    public static final String RETRIEVE_SUBSCRIPTION_ID_SQL =
            " SELECT " +
            "   SUBSCRIPTION_ID " +
            " FROM " +
            "   AM_SUBSCRIPTION " +
            " WHERE " +
            "   UUID = ? ";

    public static final String ADD_SUBSCRIPTION_SQL =
            " INSERT INTO " +
            "   AM_SUBSCRIPTION (TIER_ID,API_ID,APPLICATION_ID,SUB_STATUS,SUBS_CREATE_STATE,CREATED_BY,CREATED_TIME, " +
            "   UPDATED_TIME, UUID, TIER_ID_PENDING) " +
            " VALUES (?,?,?,?,?,?,?,?,?,?)";

    public static final String UPDATE_SINGLE_SUBSCRIPTION_SQL =
            " UPDATE AM_SUBSCRIPTION " +
            " SET TIER_ID_PENDING = ? " +
            " , SUB_STATUS = ? " +
            " WHERE UUID = ?";

    public static final String GET_SUBSCRIPTION_UUID_SQL =
            " SELECT UUID " +
            " FROM AM_SUBSCRIPTION " +
            " WHERE " +
            "   API_ID = ? " +
            "   AND APPLICATION_ID = ?";

    public static final String GET_SUBSCRIBER_ID_BY_SUBSCRIPTION_UUID_SQL =
            " SELECT APPS.SUBSCRIBER_ID  AS SUBSCRIBER_ID " +
            " FROM " +
            " AM_APPLICATION APPS, " +
            " AM_SUBSCRIPTION SUBS" +
            " WHERE " +
            " SUBS.APPLICATION_ID = APPS.APPLICATION_ID " +
            " AND SUBS.UUID = ?";

    public static final String GET_SUBSCRIPTION_STATUS_BY_UUID_SQL =
            " SELECT SUB_STATUS " +
            " FROM AM_SUBSCRIPTION " +
            " WHERE UUID = ?";

    public static final String UPDATE_SUBSCRIPTION_SQL =
            " UPDATE AM_SUBSCRIPTION " +
            " SET SUBS_CREATE_STATE = '" + APIConstants.SubscriptionCreatedStatus.UN_SUBSCRIBE + "' " +
            " WHERE UUID = ?";

    public static final String REMOVE_SUBSCRIPTION_SQL =
            " DELETE FROM AM_SUBSCRIPTION WHERE UUID = ?";

    public static final String REMOVE_SUBSCRIPTION_BY_ID_SQL =
            " DELETE FROM AM_SUBSCRIPTION WHERE SUBSCRIPTION_ID = ?";

    public static final String REMOVE_ALL_SUBSCRIPTIONS_SQL =
            " DELETE FROM AM_SUBSCRIPTION WHERE API_ID = ?";

    public static final String GET_SUBSCRIPTION_STATUS_BY_ID_SQL =
            " SELECT SUB_STATUS FROM AM_SUBSCRIPTION WHERE SUBSCRIPTION_ID = ?";

    public static final String GET_SUBSCRIPTION_BY_ID_SQL =
            " SELECT " +
            "   SUBS.SUBSCRIPTION_ID AS SUBSCRIPTION_ID, " +
            "   API.API_PROVIDER AS API_PROVIDER, " +
            "   API.API_NAME AS API_NAME, " +
            "   API.API_VERSION AS API_VERSION, " +
            "   API.API_TYPE AS API_TYPE, " +
            "   SUBS.APPLICATION_ID AS APPLICATION_ID, " +
            "   SUBS.TIER_ID AS TIER_ID, " +
            "   SUBS.TIER_ID_PENDING AS TIER_ID_PENDING, " +
            "   SUBS.SUB_STATUS AS SUB_STATUS, " +
            "   SUBS.SUBS_CREATE_STATE AS SUBS_CREATE_STATE, " +
            "   SUBS.UUID AS UUID, " +
            "   API.API_ID AS API_ID " +
            " FROM " +
            "   AM_SUBSCRIPTION SUBS," +
            "   AM_API API " +
            " WHERE " +
            "   API.API_ID = SUBS.API_ID " +
            "   AND SUBSCRIPTION_ID = ?";

    public static final String GET_SUBSCRIPTION_BY_UUID_SQL =
            " SELECT " +
            "   SUBS.SUBSCRIPTION_ID AS SUBSCRIPTION_ID, " +
            "   API.API_PROVIDER AS API_PROVIDER, " +
            "   API.API_NAME AS API_NAME, " +
            "   API.API_VERSION AS API_VERSION, " +
            "   API.API_TYPE AS API_TYPE, " +
            "   SUBS.APPLICATION_ID AS APPLICATION_ID, " +
            "   SUBS.TIER_ID AS TIER_ID, " +
            "   SUBS.TIER_ID_PENDING AS TIER_ID_PENDING, " +
            "   SUBS.SUB_STATUS AS SUB_STATUS, " +
            "   SUBS.SUBS_CREATE_STATE AS SUBS_CREATE_STATE, " +
            "   SUBS.UUID AS UUID, " +
            "   SUBS.CREATED_TIME AS CREATED_TIME, " +
            "   SUBS.UPDATED_TIME AS UPDATED_TIME, " +
            "   API.API_ID AS API_ID " +
            " FROM " +
            "   AM_SUBSCRIPTION SUBS," +
            "   AM_API API " +
            " WHERE " +
            "   API.API_ID = SUBS.API_ID " +
            "   AND UUID = ?";

    public static final String GET_TENANT_SUBSCRIBER_SQL =
            " SELECT " +
            "   SUBSCRIBER_ID, " +
            "   USER_ID, " +
            "   TENANT_ID, " +
            "   EMAIL_ADDRESS, " +
            "   DATE_SUBSCRIBED " +
            " FROM " +
            "   AM_SUBSCRIBER " +
            " WHERE " +
            "   USER_ID = ? " +
            "   AND TENANT_ID = ?";

    public static final String GET_TENANT_SUBSCRIBER_CASE_INSENSITIVE_SQL =
            " SELECT " +
            "   SUBSCRIBER_ID, " +
            "   USER_ID, " +
            "   TENANT_ID, " +
            "   EMAIL_ADDRESS, " +
            "   DATE_SUBSCRIBED " +
            " FROM " +
            "   AM_SUBSCRIBER " +
            " WHERE " +
            "   LOWER(USER_ID) = LOWER(?) " +
            "   AND TENANT_ID = ?";

    public static final String GET_API_BY_CONSUMER_KEY_SQL =
            " SELECT" +
            "   API.API_PROVIDER," +
            "   API.API_NAME," +
            "   API.API_VERSION " +
            " FROM" +
            "   AM_SUBSCRIPTION SUB," +
            "   AM_SUBSCRIPTION_KEY_MAPPING SKM, " +
            "   AM_API API " +
            " WHERE" +
            "   SKM.ACCESS_TOKEN=?" +
            "   AND SKM.SUBSCRIPTION_ID=SUB.SUBSCRIPTION_ID" +
            "   AND API.API_ID = SUB.API_ID";

    public static final String GET_SUBSCRIBED_APIS_SQL =
            " SELECT " +
            "   SUBS.SUBSCRIPTION_ID AS SUBS_ID, " +
            "   API.API_PROVIDER AS API_PROVIDER, " +
            "   API.API_NAME AS API_NAME, " +
            "   API.API_VERSION AS API_VERSION, " +
            "   SUBS.TIER_ID AS TIER_ID, " +
            "   APP.APPLICATION_ID AS APP_ID, " +
            "   SUBS.SUB_STATUS AS SUB_STATUS, " +
            "   SUBS.SUBS_CREATE_STATE AS SUBS_CREATE_STATE, " +
            "   APP.NAME AS APP_NAME, " +
            "   APP.CALLBACK_URL AS CALLBACK_URL, " +
            "   SUBS.UUID AS SUB_UUID, " +
            "   APP.UUID AS APP_UUID " +
            " FROM " +
            "   AM_SUBSCRIBER SUB," +
            "   AM_APPLICATION APP, " +
            "   AM_SUBSCRIPTION SUBS, " +
            "   AM_API API " +
            " WHERE " +
            "   SUB.TENANT_ID = ? " +
            "   AND APP.APPLICATION_ID=SUBS.APPLICATION_ID " +
            "   AND SUB.SUBSCRIBER_ID=APP.SUBSCRIBER_ID " +
            "   AND API.API_ID=SUBS.API_ID" +
            "   AND APP.NAME= ? " +
            "   AND SUBS.SUBS_CREATE_STATE = '" + APIConstants.SubscriptionCreatedStatus.SUBSCRIBE + "'";

    public static final String GET_SUBSCRIBED_APIS_BY_ID_SQL =
            " SELECT " +
                    "   SUBS.SUBSCRIPTION_ID AS SUBS_ID, " +
                    "   API.API_PROVIDER AS API_PROVIDER, " +
                    "   API.API_NAME AS API_NAME, " +
                    "   API.API_VERSION AS API_VERSION, " +
                    "   SUBS.TIER_ID AS TIER_ID, " +
                    "   APP.APPLICATION_ID AS APP_ID, " +
                    "   SUBS.SUB_STATUS AS SUB_STATUS, " +
                    "   SUBS.SUBS_CREATE_STATE AS SUBS_CREATE_STATE, " +
                    "   APP.NAME AS APP_NAME, " +
                    "   APP.CALLBACK_URL AS CALLBACK_URL, " +
                    "   SUBS.UUID AS SUB_UUID, " +
                    "   APP.UUID AS APP_UUID, " +
                    "   APP.CREATED_BY AS OWNER" +
                    " FROM " +
                    "   AM_SUBSCRIBER SUB," +
                    "   AM_APPLICATION APP, " +
                    "   AM_SUBSCRIPTION SUBS, " +
                    "   AM_API API " +
                    " WHERE " +
                    "   SUB.TENANT_ID = ? " +
                    "   AND APP.APPLICATION_ID=SUBS.APPLICATION_ID " +
                    "   AND SUB.SUBSCRIBER_ID=APP.SUBSCRIBER_ID " +
                    "   AND API.API_ID=SUBS.API_ID" +
                    "   AND APP.APPLICATION_ID= ? " +
                    "   AND SUBS.SUBS_CREATE_STATE = '" + APIConstants.SubscriptionCreatedStatus.SUBSCRIBE + "'";

    public static final String GET_SUBSCRIPTION_COUNT_SQL =
            " SELECT COUNT(*) AS SUB_COUNT " +
            " FROM " +
            "   AM_SUBSCRIPTION SUBS, AM_APPLICATION APP, AM_SUBSCRIBER SUB " +
            " WHERE SUBS.SUBS_CREATE_STATE ='" + APIConstants.SubscriptionCreatedStatus.SUBSCRIBE + "'" +
            "   AND SUBS.APPLICATION_ID = APP.APPLICATION_ID" +
            "   AND APP.NAME=?" +
            "   AND APP.SUBSCRIBER_ID= SUB.SUBSCRIBER_ID" +
            "   AND SUB.TENANT_ID=?";

    public static final String GET_SUBSCRIPTION_COUNT_BY_APP_ID_SQL =
            " SELECT COUNT(*) AS SUB_COUNT " +
                    " FROM " +
                    "   AM_SUBSCRIPTION SUBS, AM_APPLICATION APP, AM_SUBSCRIBER SUB " +
                    " WHERE SUBS.SUBS_CREATE_STATE ='" + APIConstants.SubscriptionCreatedStatus.SUBSCRIBE + "'" +
                    "   AND SUBS.APPLICATION_ID = APP.APPLICATION_ID" +
                    "   AND APP.APPLICATION_ID=?" +
                    "   AND APP.SUBSCRIBER_ID= SUB.SUBSCRIBER_ID" +
                    "   AND SUB.TENANT_ID=?";

    public static final String GET_SUBSCRIPTION_COUNT_CASE_INSENSITIVE_SQL =
            " SELECT COUNT(*) AS SUB_COUNT " +
            " FROM " +
            "   AM_SUBSCRIPTION SUBS,AM_APPLICATION APP,AM_SUBSCRIBER SUB " +
            " WHERE " +
            "   SUBS.SUBS_CREATE_STATE ='" + APIConstants.SubscriptionCreatedStatus.SUBSCRIBE + "'" +
            "   AND SUBS.APPLICATION_ID = APP.APPLICATION_ID" +
            "   AND APP.NAME=?" +
            "   AND APP.SUBSCRIBER_ID= SUB.SUBSCRIBER_ID" +
            "   AND SUB.TENANT_ID=?";

    public static final String GET_SUBSCRIPTION_COUNT_BY_APP_ID_CASE_INSENSITIVE_SQL =
            " SELECT COUNT(*) AS SUB_COUNT " +
                    " FROM " +
                    "   AM_SUBSCRIPTION SUBS,AM_APPLICATION APP,AM_SUBSCRIBER SUB " +
                    " WHERE " +
                    "   SUBS.SUBS_CREATE_STATE ='" + APIConstants.SubscriptionCreatedStatus.SUBSCRIBE + "'" +
                    "   AND SUBS.APPLICATION_ID = APP.APPLICATION_ID" +
                    "   AND APP.APPLICATION_ID=?" +
                    "   AND APP.SUBSCRIBER_ID= SUB.SUBSCRIBER_ID" +
                    "   AND SUB.TENANT_ID=?";

    public static final String GET_PAGINATED_SUBSCRIBED_APIS_SQL =
            " SELECT " +
            "   API.API_TYPE AS TYPE, " +
            "   SUBS.UUID AS SUB_UUID, " +
            "   SUBS.SUBSCRIPTION_ID, " +
            "   API.API_PROVIDER AS API_PROVIDER, " +
            "   API.API_NAME AS API_NAME, " +
            "   API.API_VERSION AS API_VERSION, " +
            "   SUBS.TIER_ID AS TIER_ID, " +
            "   SUBS.TIER_ID_PENDING AS TIER_ID_PENDING, " +
            "   APP.APPLICATION_ID AS APP_ID, " +
            "   APP.UUID AS APP_UUID, " +
            "   SUBS.SUB_STATUS AS SUB_STATUS, " +
            "   SUBS.SUBS_CREATE_STATE AS SUBS_CREATE_STATE, " +
            "   APP.NAME AS APP_NAME, " +
            "   APP.CALLBACK_URL AS CALLBACK_URL " +
            " FROM " +
            "   AM_SUBSCRIBER SUB," +
            "   AM_APPLICATION APP, " +
            "   AM_SUBSCRIPTION SUBS, " +
            "   AM_API API " +
            " WHERE " +
            "   SUB.TENANT_ID = ? " +
            "   AND SUB.SUBSCRIBER_ID=APP.SUBSCRIBER_ID " +
            "   AND APP.APPLICATION_ID=SUBS.APPLICATION_ID " +
            "   AND API.API_ID=SUBS.API_ID" +
            "   AND APP.NAME= ? " +
            "   AND SUBS.SUBS_CREATE_STATE = '" + APIConstants.SubscriptionCreatedStatus.SUBSCRIBE + "'";

    public static final String GET_PAGINATED_SUBSCRIBED_APIS_BY_APP_ID_SQL =
            " SELECT " +
                    "   SUBS.SUBSCRIPTION_ID, " +
                    "   API.API_PROVIDER AS API_PROVIDER, " +
                    "   API.API_NAME AS API_NAME, " +
                    "   API.API_VERSION AS API_VERSION, " +
                    "   SUBS.TIER_ID AS TIER_ID, " +
                    "   SUBS.TIER_ID_PENDING AS TIER_ID_PENDING, " +
                    "   APP.APPLICATION_ID AS APP_ID, " +
                    "   SUBS.SUB_STATUS AS SUB_STATUS, " +
                    "   SUBS.SUBS_CREATE_STATE AS SUBS_CREATE_STATE, " +
                    "   APP.NAME AS APP_NAME, " +
                    "   APP.CALLBACK_URL AS CALLBACK_URL " +
                    " FROM " +
                    "   AM_SUBSCRIBER SUB," +
                    "   AM_APPLICATION APP, " +
                    "   AM_SUBSCRIPTION SUBS, " +
                    "   AM_API API " +
                    " WHERE " +
                    "   SUB.TENANT_ID = ? " +
                    "   AND SUB.SUBSCRIBER_ID=APP.SUBSCRIBER_ID " +
                    "   AND APP.APPLICATION_ID=SUBS.APPLICATION_ID " +
                    "   AND API.API_ID=SUBS.API_ID " +
                    "   AND APP.APPLICATION_ID= ? " +
                    "   AND SUBS.SUBS_CREATE_STATE = '" + APIConstants.SubscriptionCreatedStatus.SUBSCRIBE + "'";

    public static final String GET_SUBSCRIBED_APIS_OF_SUBSCRIBER_SQL =
            " SELECT " +
            "   API.API_TYPE AS TYPE, " +
            "   SUBS.SUBSCRIPTION_ID AS SUBS_ID, " +
            "   API.API_PROVIDER AS API_PROVIDER, " +
            "   API.API_NAME AS API_NAME, " +
            "   API.API_VERSION AS API_VERSION, " +
            "   SUBS.TIER_ID AS TIER_ID, " +
            "   SUBS.TIER_ID_PENDING AS TIER_ID_PENDING, " +
            "   APP.APPLICATION_ID AS APP_ID, " +
            "   SUBS.SUB_STATUS AS SUB_STATUS, " +
            "   SUBS.SUBS_CREATE_STATE AS SUBS_CREATE_STATE, " +
            "   APP.NAME AS APP_NAME, " +
            "   APP.TOKEN_TYPE AS APP_TOKEN_TYPE, " +
            "   APP.CALLBACK_URL AS CALLBACK_URL, " +
            "   SUBS.UUID AS SUB_UUID, " +
            "   APP.UUID AS APP_UUID, " +
            "   APP.CREATED_BY AS OWNER" +
            " FROM " +
            "   AM_SUBSCRIBER SUB," +
            "   AM_APPLICATION APP, " +
            "   AM_SUBSCRIPTION SUBS, " +
            "   AM_API API " +
            " WHERE " +
            "   SUB.TENANT_ID = ? " +
            "   AND SUB.SUBSCRIBER_ID=APP.SUBSCRIBER_ID " +
            "   AND APP.APPLICATION_ID=SUBS.APPLICATION_ID" +
            "   AND API.API_ID=SUBS.API_ID " +
            "   AND SUBS.SUBS_CREATE_STATE = '" + APIConstants.SubscriptionCreatedStatus.SUBSCRIBE + "'";

    public static final String GET_API_KEY_BY_SUBSCRIPTION_SQL =
            " SELECT " +
            "   SKM.ACCESS_TOKEN AS ACCESS_TOKEN," +
            "   SKM.KEY_TYPE AS TOKEN_TYPE " +
            " FROM" +
            "   AM_SUBSCRIPTION_KEY_MAPPING SKM " +
            " WHERE" +
            "   SKM.SUBSCRIPTION_ID = ?";

    public static final String GET_SCOPE_BY_TOKEN_SQL =
            " SELECT ISAT.TOKEN_SCOPE " +
            " FROM " +
            APIConstants.ACCESS_TOKEN_STORE_TABLE + " IAT," +
            APIConstants.TOKEN_SCOPE_ASSOCIATION_TABLE + " ISAT " +
            " WHERE " +
            "   IAT.ACCESS_TOKEN= ?" +
            "   AND IAT.TOKEN_ID = ISAT.TOKEN_ID";

    public static final String IS_ACCESS_TOKEN_EXISTS_PREFIX = " SELECT ACCESS_TOKEN " + " FROM ";

    public static final String IS_ACCESS_TOKEN_EXISTS_SUFFIX = " WHERE ACCESS_TOKEN= ? ";

    public static final String IS_ACCESS_TOKEN_REVOKED_PREFIX = " SELECT TOKEN_STATE " + " FROM ";

    public static final String IS_ACCESS_TOKE_REVOKED_SUFFIX = " WHERE ACCESS_TOKEN= ? ";

    public static final String GET_ACCESS_TOKEN_DATA_PREFIX =
            " SELECT " +
            "   IAT.ACCESS_TOKEN, " +
            "   IAT.AUTHZ_USER, " +
            "   IAT.USER_DOMAIN, " +
            "   ISAT.TOKEN_SCOPE, " +
            "   ICA.CONSUMER_KEY, " +
            "   IAT.TIME_CREATED, " +
            "   IAT.VALIDITY_PERIOD " +
            " FROM ";

    public static final String GET_ACCESS_TOKEN_DATA_SUFFIX =
            "   IAT, " +
            APIConstants.TOKEN_SCOPE_ASSOCIATION_TABLE + " ISAT, " +
            APIConstants.CONSUMER_KEY_SECRET_TABLE + " ICA " +
            " WHERE IAT.TOKEN_ID = ISAT.TOKEN_ID " +
            "   AND IAT.CONSUMER_KEY_ID = ICA.ID " +
            "   AND IAT.ACCESS_TOKEN= ? " +
            "   AND IAT.TOKEN_STATE='ACTIVE' ";

    public static final String GET_TOKEN_SQL_PREFIX =
            " SELECT " +
            "   IAT.ACCESS_TOKEN, " +
            "   IAT.AUTHZ_USER, " +
            "   IAT.USER_DOMAIN, " +
            "   ISAT.TOKEN_SCOPE, " +
            "   ICA.CONSUMER_KEY, " +
            "   IAT.TIME_CREATED, " +
            "   IAT.VALIDITY_PERIOD " +
            " FROM ";

    public static final String GET_TOKEN_SQL_SUFFIX =
            "   IAT, " +
            APIConstants.TOKEN_SCOPE_ASSOCIATION_TABLE + " ISAT, " +
            APIConstants.CONSUMER_KEY_SECRET_TABLE + " ICA " +
            " WHERE " +
            "   IAT.TOKEN_STATE='ACTIVE' " +
            "   AND IAT.TOKEN_ID = ISAT.TOKEN_ID " +
            "   AND IAT.CONSUMER_KEY_ID = ICA.ID " +
            " ORDER BY IAT.TOKEN_ID";

    public static final String GET_ACCESS_TOKEN_BY_USER_PREFIX =
            " SELECT " +
            "   IAT.ACCESS_TOKEN, " +
            "   IAT.AUTHZ_USER, " +
            "   IAT.USER_DOMAIN, " +
            "   ISAT.TOKEN_SCOPE, " +
            "   ICA.CONSUMER_KEY, " +
            "   IAT.TIME_CREATED, " +
            "   IAT.VALIDITY_PERIOD " +
            " FROM ";

    public static final String GET_ACCESS_TOKEN_BY_USER_SUFFIX =
            "   IAT, " +
            APIConstants.TOKEN_SCOPE_ASSOCIATION_TABLE + " ISAT, " +
            APIConstants.CONSUMER_KEY_SECRET_TABLE + " ICA " +
            " WHERE IAT.AUTHZ_USER= ? " +
            "   AND IAT.TOKEN_STATE='ACTIVE'" +
            "   AND IAT.TOKEN_ID = ISAT.TOKEN_ID " +
            "   AND IAT.CONSUMER_KEY_ID = ICA.ID " +
            " ORDER BY IAT.TOKEN_ID";

    public static final String GET_TOKEN_BY_DATE_PREFIX =
            " SELECT " +
            "   IAT.ACCESS_TOKEN, " +
            "   IAT.AUTHZ_USER, " +
            "   IAT.USER_DOMAIN, " +
            "   ISAT.TOKEN_SCOPE, " +
            "   ICA.CONSUMER_KEY, " +
            "   IAT.TIME_CREATED, " +
            "   IAT.VALIDITY_PERIOD " +
            " FROM ";

    public static final String GET_TOKEN_BY_DATE_AFTER_SUFFIX =
            " IAT, " +
            APIConstants.TOKEN_SCOPE_ASSOCIATION_TABLE + " ISAT, " +
            APIConstants.CONSUMER_KEY_SECRET_TABLE + " ICA " +
            " WHERE " +
            "   IAT.TOKEN_STATE='ACTIVE' " +
            "   AND IAT.TIME_CREATED >= ? " +
            "   AND IAT.TOKEN_ID = ISAT.TOKEN_ID " +
            "   AND IAT.CONSUMER_KEY_ID = ICA.ID " +
            " ORDER BY IAT.TOKEN_ID";

    public static final String GET_TOKEN_BY_DATE_BEFORE_SUFFIX =
            "   IAT, " +
            APIConstants.TOKEN_SCOPE_ASSOCIATION_TABLE + " ISAT, " +
            APIConstants.CONSUMER_KEY_SECRET_TABLE + " ICA " +
            " WHERE " +
            "   IAT.TOKEN_STATE='ACTIVE' " +
            "   AND IAT.TIME_CREATED <= ? " +
            "   AND IAT.TOKEN_ID = ISAT.TOKEN_ID " +
            "   AND IAT.CONSUMER_KEY_ID = ICA.ID " +
            " ORDER BY IAT.TOKEN_ID";

    public static final String GET_CLIENT_OF_APPLICATION_SQL =
            " SELECT  CONSUMER_KEY,KEY_MANAGER,CREATE_MODE " +
            " FROM AM_APPLICATION_KEY_MAPPING " +
            " WHERE APPLICATION_ID = ? AND KEY_TYPE = ?";

    public static final String GET_CONSUMER_KEYS_OF_APPLICATION_SQL =
            " SELECT CONSUMER_KEY " +
            " FROM AM_APPLICATION_KEY_MAPPING " +
            " WHERE APPLICATION_ID = ?";

    public static final String GET_ACCESS_TOKEN_INFO_BY_CONSUMER_KEY_PREFIX =
            "   ICA.CONSUMER_KEY AS CONSUMER_KEY," +
            "   ICA.CONSUMER_SECRET AS CONSUMER_SECRET," +
            "   ICA.GRANT_TYPES AS GRANT_TYPES," +
            "   ICA.CALLBACK_URL AS CALLBACK_URL," +
            "   IAT.ACCESS_TOKEN AS ACCESS_TOKEN," +
            "   IAT.VALIDITY_PERIOD AS VALIDITY_PERIOD," +
            "   ISAT.TOKEN_SCOPE AS TOKEN_SCOPE" +
            " FROM   ";

    public static final String GET_ACCESS_TOKEN_INFO_BY_CONSUMER_KEY_SUFFIX =
            "   IAT, " +
            APIConstants.TOKEN_SCOPE_ASSOCIATION_TABLE + " ISAT," +
            "   IDN_OAUTH_CONSUMER_APPS ICA " +
            " WHERE" +
            "   ICA.CONSUMER_KEY = ? " +
            "   AND IAT.USER_TYPE = ? " +
            "   AND IAT.CONSUMER_KEY_ID = ICA.ID " +
            "   AND IAT.TOKEN_ID = ISAT.TOKEN_ID " +
            "   AND (IAT.TOKEN_STATE = 'ACTIVE' OR IAT.TOKEN_STATE = 'EXPIRED' OR IAT.TOKEN_STATE = 'REVOKED') " +
            " ORDER BY IAT.TIME_CREATED DESC";

    public static final String GET_ACCESS_TOKEN_INFO_BY_CONSUMER_KEY_ORACLE_PREFIX =
            " SELECT " +
            "   CONSUMER_KEY, " +
            "   CONSUMER_SECRET, " +
            "   GRANT_TYPES, " +
            "   CALLBACK_URL, " +
            "   ACCESS_TOKEN, " +
            "   VALIDITY_PERIOD, " +
            "   TOKEN_SCOPE "+
            " FROM (" +
            "   SELECT " +
            "       ICA.CONSUMER_KEY AS CONSUMER_KEY, " +
            "       ICA.CONSUMER_SECRET AS CONSUMER_SECRET, " +
            "       ICA.GRANT_TYPES AS GRANT_TYPES, " +
            "       ICA.CALLBACK_URL AS CALLBACK_URL, " +
            "       IAT.ACCESS_TOKEN AS ACCESS_TOKEN, " +
            "       IAT.VALIDITY_PERIOD AS VALIDITY_PERIOD, " +
            "       ISAT.TOKEN_SCOPE AS TOKEN_SCOPE " +
            "   FROM ";

    public static final String GET_ACCESS_TOKEN_INFO_BY_CONSUMER_KEY_ORACLE_SUFFIX =
            "       IAT, " +
            APIConstants.TOKEN_SCOPE_ASSOCIATION_TABLE + " ISAT," +
            "       IDN_OAUTH_CONSUMER_APPS ICA " +
            "   WHERE " +
            "       ICA.CONSUMER_KEY = ? " +
            "       AND IAT.USER_TYPE = ? " +
            "       AND IAT.CONSUMER_KEY_ID = ICA.ID " +
            "       AND IAT.TOKEN_ID = ISAT.TOKEN_ID " +
            "       AND (IAT.TOKEN_STATE = 'ACTIVE' OR IAT.TOKEN_STATE = 'EXPIRED' OR IAT.TOKEN_STATE = 'REVOKED') " +
            "   ORDER BY IAT.TIME_CREATED DESC) ";

    //--------------------New tier permission management

    public static final String GET_THROTTLE_TIER_PERMISSION_ID_SQL =
            " SELECT THROTTLE_TIER_PERMISSIONS_ID " +
            " FROM AM_THROTTLE_TIER_PERMISSIONS " +
            " WHERE TIER = ? AND " + "TENANT_ID = ?";

    public static final String ADD_THROTTLE_TIER_PERMISSION_SQL =
            " INSERT INTO" +
            "   AM_THROTTLE_TIER_PERMISSIONS (TIER, PERMISSIONS_TYPE, ROLES, TENANT_ID)" +
            " VALUES(?, ?, ?, ?)";

    public static final String UPDATE_THROTTLE_TIER_PERMISSION_SQL =
            " UPDATE" +
            "   AM_THROTTLE_TIER_PERMISSIONS " +
            " SET " +
            "   TIER = ?, " +
            "   PERMISSIONS_TYPE = ?," +
            "   ROLES = ? " +
            " WHERE " +
            "   THROTTLE_TIER_PERMISSIONS_ID = ? " +
            "   AND TENANT_ID = ?";

    public static final String GET_THROTTLE_TIER_PERMISSIONS_SQL =
            " SELECT TIER,PERMISSIONS_TYPE, ROLES " +
            " FROM AM_THROTTLE_TIER_PERMISSIONS " +
            " WHERE TENANT_ID = ?";

    public static final String GET_THROTTLE_TIER_PERMISSION_SQL =
            " SELECT PERMISSIONS_TYPE, ROLES " +
                    " FROM AM_THROTTLE_TIER_PERMISSIONS " +
                    " WHERE TIER = ? AND TENANT_ID = ?";

    public static final String DELETE_THROTTLE_TIER_PERMISSION_SQL =
            "DELETE FROM " +
                    "AM_THROTTLE_TIER_PERMISSIONS " +
                    "WHERE TIER = ? AND TENANT_ID = ?";

  //--------------------

    public static final String GET_TIER_PERMISSION_ID_SQL =
            " SELECT TIER_PERMISSIONS_ID " +
            " FROM AM_TIER_PERMISSIONS " +
            " WHERE TIER = ? AND " + "TENANT_ID = ?";

    public static final String ADD_TIER_PERMISSION_SQL =
            " INSERT INTO" +
            "   AM_TIER_PERMISSIONS (TIER, PERMISSIONS_TYPE, ROLES, TENANT_ID)" +
            " VALUES(?, ?, ?, ?)";

    public static final String UPDATE_TIER_PERMISSION_SQL =
            " UPDATE" +
            "   AM_TIER_PERMISSIONS " +
            " SET " +
            "   TIER = ?, " +
            "   PERMISSIONS_TYPE = ?," +
            "   ROLES = ? " +
            " WHERE " +
            "   TIER_PERMISSIONS_ID = ? " +
            "   AND TENANT_ID = ?";

    public static final String GET_TIER_PERMISSIONS_SQL =
            " SELECT TIER , PERMISSIONS_TYPE , ROLES " +
            " FROM AM_TIER_PERMISSIONS " +
            " WHERE TENANT_ID = ?";

    public static final String GET_PERMISSION_OF_TIER_SQL =
            " SELECT PERMISSIONS_TYPE, ROLES " +
            " FROM AM_TIER_PERMISSIONS " +
            " WHERE TIER = ? AND TENANT_ID = ?";

    public static final String GET_KEY_SQL_PREFIX =
            " SELECT " +
            "   ICA.CONSUMER_KEY AS CONSUMER_KEY," +
            "   ICA.CONSUMER_SECRET AS CONSUMER_SECRET," +
            "   IAT.ACCESS_TOKEN AS ACCESS_TOKEN," +
            "   AKM.KEY_TYPE AS TOKEN_TYPE " +
            " FROM" +
            "   AM_APPLICATION_KEY_MAPPING AKM,";

    public static final String GET_KEY_SQL_SUFFIX =
            "   IAT," +
            "   IDN_OAUTH_CONSUMER_APPS ICA " +
            " WHERE" +
            "   AKM.APPLICATION_ID = ? " +
            "   AND ICA.CONSUMER_KEY = AKM.CONSUMER_KEY " +
            "   AND ICA.ID = IAT.CONSUMER_KEY_ID";

    public static final String GET_KEY_SQL_OF_SUBSCRIPTION_ID_PREFIX =
            " SELECT " +
            "   IAT.ACCESS_TOKEN AS ACCESS_TOKEN," +
            "   IAT.TOKEN_STATE AS TOKEN_STATE " +
            " FROM" +
            "   AM_APPLICATION_KEY_MAPPING AKM," +
            "   AM_SUBSCRIPTION SM,";

    public static final String GET_KEY_SQL_OF_SUBSCRIPTION_ID_SUFFIX =
            "   IAT," +
            "   IDN_OAUTH_CONSUMER_APPS ICA " +
            " WHERE" +
            "   SM.SUBSCRIPTION_ID = ? " +
            "   AND SM.APPLICATION_ID= AKM.APPLICATION_ID " +
            "   AND ICA.CONSUMER_KEY = AKM.CONSUMER_KEY " +
            "   AND ICA.ID = IAT.CONSUMER_KEY_ID";

    public static final String GET_SUBSCRIBERS_OF_PROVIDER_SQL =
            " SELECT " +
            "   SUBS.USER_ID AS USER_ID," +
            "   SUBS.EMAIL_ADDRESS AS EMAIL_ADDRESS, " +
            "   SUBS.DATE_SUBSCRIBED AS DATE_SUBSCRIBED " +
            " FROM " +
            "   AM_SUBSCRIBER  SUBS," +
            "   AM_APPLICATION  APP, " +
            "   AM_SUBSCRIPTION SUB, " +
            "   AM_API API " +
            " WHERE  " +
            "   SUB.APPLICATION_ID = APP.APPLICATION_ID " +
            "   AND SUBS. SUBSCRIBER_ID = APP.SUBSCRIBER_ID " +
            "   AND API.API_ID = SUB.API_ID " +
            "   AND API.API_PROVIDER = ?" +
            "   AND SUB.SUBS_CREATE_STATE = '" + APIConstants.SubscriptionCreatedStatus.SUBSCRIBE + "'";

    public static final String GET_SUBSCRIBERS_OF_API_SQL =
            " SELECT DISTINCT SB.USER_ID, SB.DATE_SUBSCRIBED " +
            " FROM " +
            "   AM_SUBSCRIBER SB, " +
            "   AM_SUBSCRIPTION SP," +
            "   AM_APPLICATION APP," +
            "   AM_API API " +
            " WHERE " +
            "   API.API_PROVIDER=? " +
            "   AND API.API_NAME=? " +
            "   AND API.API_VERSION=? " +
            "   AND SP.APPLICATION_ID=APP.APPLICATION_ID" +
            "   AND APP.SUBSCRIBER_ID=SB.SUBSCRIBER_ID " +
            "   AND API.API_ID = SP.API_ID" +
            "   AND SP.SUB_STATUS != '" + APIConstants.SubscriptionStatus.REJECTED + "'" +
            "   AND SP.SUBS_CREATE_STATE = '" + APIConstants.SubscriptionCreatedStatus.SUBSCRIBE + "'";

    public static final String GET_API_SUBSCRIPTION_COUNT_BY_API_SQL =
            " SELECT" +
            "   COUNT(SUB.SUBSCRIPTION_ID) AS SUB_ID " +
            " FROM " +
            "   AM_SUBSCRIPTION SUB, " +
            "   AM_API API " +
            " WHERE API.API_PROVIDER=? " +
            "   AND API.API_NAME=?" +
            "   AND API.API_VERSION=?" +
            "   AND API.API_ID=SUB.API_ID" +
            "   AND SUB.SUBS_CREATE_STATE = '" + APIConstants.SubscriptionCreatedStatus.SUBSCRIBE + "'";

    public static final String UPDATE_SUBSCRIPTION_OF_APPLICATION_SQL =
            " UPDATE AM_SUBSCRIPTION " +
            " SET " +
            "   SUB_STATUS = ?, " +
            "   UPDATED_BY = ?, " +
            "   UPDATED_TIME = ? " +
            " WHERE " +
            "   API_ID = ? " +
            "   AND APPLICATION_ID = ?";

    public static final String UPDATE_SUBSCRIPTION_OF_UUID_SQL =
            " UPDATE AM_SUBSCRIPTION " +
            " SET " +
            "   SUB_STATUS = ?, " +
            "   UPDATED_BY = ?, " +
            "   UPDATED_TIME = ? " +
            " WHERE " +
            "   UUID = ?";

    public static final String UPDATE_SUBSCRIPTION_STATUS_SQL =
            " UPDATE AM_SUBSCRIPTION " +
            " SET SUB_STATUS = ? " +
            " WHERE SUBSCRIPTION_ID = ?";

    public static final String UPDATE_SUBSCRIPTION_STATUS_AND_TIER_SQL =
            " UPDATE AM_SUBSCRIPTION " +
                    " SET TIER_ID_PENDING = ? " +
                    " , TIER_ID = ? " +
                    " , SUB_STATUS = ? " +
                    " WHERE SUBSCRIPTION_ID = ?";

    public static final String UPDATE_REFRESHED_APPLICATION_ACCESS_TOKEN_PREFIX = "UPDATE ";

    public static final String UPDATE_REFRESHED_APPLICATION_ACCESS_TOKEN_SUFFIX =
            " SET " +
            "   USER_TYPE=?, " +
            "   VALIDITY_PERIOD=? " +
            " WHERE " +
            "   ACCESS_TOKEN=?";

    public static final String DELETE_ACCSS_ALLOWED_DOMAINS_SQL =
            " DELETE FROM AM_APP_KEY_DOMAIN_MAPPING WHERE CONSUMER_KEY=?";

    public static final String GET_REGISTRATION_APPROVAL_STATUS_SQL =
            " SELECT KEY_MANAGER,STATE FROM AM_APPLICATION_KEY_MAPPING WHERE APPLICATION_ID = ? AND KEY_TYPE =?";

    public static final String UPDATE_APPLICAITON_KEY_TYPE_MAPPINGS_SQL =
            " UPDATE AM_APPLICATION_KEY_MAPPING SET CONSUMER_KEY = ? , APP_INFO = ? WHERE APPLICATION_ID = ? AND " +
                    "KEY_TYPE = ? AND KEY_MANAGER = ?";

    public static final String UPDATE_APPLICATION_KEY_TYPE_MAPPINGS_METADATA_SQL =
            " UPDATE AM_APPLICATION_KEY_MAPPING SET APP_INFO = ? WHERE APPLICATION_ID = ? AND " +
                    "KEY_TYPE = ? AND KEY_MANAGER = ?";

    public static final String ADD_APPLICATION_KEY_TYPE_MAPPING_SQL =
            " INSERT INTO " +
            " AM_APPLICATION_KEY_MAPPING (APPLICATION_ID,CONSUMER_KEY,KEY_TYPE,STATE,CREATE_MODE,KEY_MANAGER,UUID) " +
            " VALUES (?,?,?,?,?,?,?)";

    public static final String UPDATE_APPLICATION_KEY_MAPPING_SQL =
            " UPDATE AM_APPLICATION_KEY_MAPPING SET STATE = ? WHERE APPLICATION_ID = ? AND KEY_TYPE = ? AND " +
                    "KEY_MANAGER=?";

    public static final String GET_SUBSCRIPTION_SQL =
            " SELECT " +
            "   SUBS.TIER_ID ," +
            "   API.API_PROVIDER ," +
            "   API.API_NAME ," +
            "   API.API_VERSION ," +
            "   SUBS.APPLICATION_ID " +
            " FROM " +
            "   AM_SUBSCRIPTION SUBS," +
            "   AM_SUBSCRIBER SUB, " +
            "   AM_APPLICATION  APP, " +
            "   AM_API API " +
            " WHERE " +
            "   API.API_PROVIDER  = ?" +
            "   AND API.API_NAME = ?" +
            "   AND API.API_VERSION = ?" +
            "   AND SUB.USER_ID = ?" +
            "   AND SUB.TENANT_ID = ? " +
            "   AND APP.SUBSCRIBER_ID = SUB.SUBSCRIBER_ID" +
            "   AND API.API_ID = SUBS.API_ID" +
            "   AND SUBS.SUBS_CREATE_STATE = '" + APIConstants.SubscriptionCreatedStatus.SUBSCRIBE + "'";

    public static final String GET_SUBSCRIPTION_CASE_INSENSITIVE_SQL =
            " SELECT " +
            "   SUBS.TIER_ID ," +
            "   API.API_PROVIDER ," +
            "   API.API_NAME ," +
            "   API.API_VERSION ," +
            "   SUBS.APPLICATION_ID " +
            " FROM " +
            "   AM_SUBSCRIPTION SUBS," +
            "   AM_SUBSCRIBER SUB, " +
            "   AM_APPLICATION  APP, " +
            "   AM_API API " +
            " WHERE " +
            "   API.API_PROVIDER  = ?" +
            "   AND API.API_NAME = ?" +
            "   AND API.API_VERSION = ?" +
            "   AND LOWER(SUB.USER_ID) = LOWER(?)" +
            "   AND SUB.TENANT_ID = ? " +
            "   AND APP.SUBSCRIBER_ID = SUB.SUBSCRIBER_ID" +
            "   AND API.API_ID = SUBS.API_ID" +
            "   AND SUBS.SUBS_CREATE_STATE = '" + APIConstants.SubscriptionCreatedStatus.SUBSCRIBE + "'";

    public static final String GET_APP_SUBSCRIPTION_TO_API_SQL =
            " SELECT " +
                    "   SUBS.TIER_ID ," +
                    "   API.API_PROVIDER ," +
                    "   API.API_NAME ," +
                    "   API.API_VERSION ," +
                    "   SUBS.APPLICATION_ID " +
                    " FROM " +
                    "   AM_SUBSCRIPTION SUBS," +
                    "   AM_SUBSCRIBER SUB, " +
                    "   AM_APPLICATION  APP, " +
                    "   AM_API API " +
                    " WHERE " +
                    "   API.API_PROVIDER  = ?" +
                    "   AND API.API_NAME = ?" +
                    "   AND API.API_VERSION = ?" +
                    "   AND SUB.USER_ID = ?" +
                    "   AND SUB.TENANT_ID = ? " +
                    "   AND SUBS.APPLICATION_ID = ? " +
                    "   AND APP.SUBSCRIBER_ID = SUB.SUBSCRIBER_ID" +
                    "   AND API.API_ID = SUBS.API_ID" +
                    "   AND SUBS.SUBS_CREATE_STATE = '" + APIConstants.SubscriptionCreatedStatus.SUBSCRIBE + "'";

    public static final String GET_APP_SUBSCRIPTION_TO_API_CASE_INSENSITIVE_SQL =
            " SELECT " +
                    "   SUBS.TIER_ID ," +
                    "   API.API_PROVIDER ," +
                    "   API.API_NAME ," +
                    "   API.API_VERSION ," +
                    "   SUBS.APPLICATION_ID " +
                    " FROM " +
                    "   AM_SUBSCRIPTION SUBS," +
                    "   AM_SUBSCRIBER SUB, " +
                    "   AM_APPLICATION  APP, " +
                    "   AM_API API " +
                    " WHERE " +
                    "   API.API_PROVIDER  = ?" +
                    "   AND API.API_NAME = ?" +
                    "   AND API.API_VERSION = ?" +
                    "   AND LOWER(SUB.USER_ID) = LOWER(?)" +
                    "   AND SUB.TENANT_ID = ? " +
                    "   AND SUBS.APPLICATION_ID = ? " +
                    "   AND APP.SUBSCRIBER_ID = SUB.SUBSCRIBER_ID" +
                    "   AND API.API_ID = SUBS.API_ID" +
                    "   AND SUBS.SUBS_CREATE_STATE = '" + APIConstants.SubscriptionCreatedStatus.SUBSCRIBE + "'";

    public static final String GET_APP_API_USAGE_BY_PROVIDER_SQL =
            " SELECT " +
            "   SUBS.SUBSCRIPTION_ID AS SUBSCRIPTION_ID, " +
            "   SUBS.APPLICATION_ID AS APPLICATION_ID, " +
            "   SUBS.SUB_STATUS AS SUB_STATUS, " +
            "   SUBS.TIER_ID AS TIER_ID, " +
            "   API.API_PROVIDER AS API_PROVIDER, " +
            "   API.API_NAME AS API_NAME, " +
            "   API.API_VERSION AS API_VERSION, " +
            "   SUB.USER_ID AS USER_ID, " +
            "   APP.NAME AS APPNAME, " +
            "   SUBS.UUID AS SUB_UUID, " +
            "   SUBS.TIER_ID AS SUB_TIER_ID, " +
            "   APP.UUID AS APP_UUID, " +
            "   SUBS.SUBS_CREATE_STATE AS SUBS_CREATE_STATE " +
            " FROM " +
            "   AM_SUBSCRIPTION SUBS, " +
            "   AM_APPLICATION APP, " +
            "   AM_SUBSCRIBER SUB, " +
            "   AM_API API " +
            " WHERE " +
            "   SUBS.APPLICATION_ID = APP.APPLICATION_ID " +
            "   AND APP.SUBSCRIBER_ID = SUB.SUBSCRIBER_ID " +
            "   AND API.API_PROVIDER = ? " +
            "   AND API.API_ID = SUBS.API_ID " +
            "   AND SUBS.SUB_STATUS != '" + APIConstants.SubscriptionStatus.REJECTED + "'" +
            " ORDER BY " +
            "   APP.NAME";

    public static final String GET_SUBSCRIPTIONS_OF_API_SQL =
            " SELECT " +
                    "   SUBS.SUBSCRIPTION_ID AS SUBSCRIPTION_ID, " +
                    "   SUBS.APPLICATION_ID AS APPLICATION_ID, " +
                    "   SUBS.SUB_STATUS AS SUB_STATUS, " +
                    "   SUBS.TIER_ID AS TIER_ID, " +
                    "   API.API_PROVIDER AS API_PROVIDER, " +
                    "   SUB.USER_ID AS USER_ID, " +
                    "   APP.NAME AS APPNAME, " +
                    "   SUBS.UUID AS SUB_UUID, " +
                    "   SUBS.CREATED_TIME AS SUB_CREATED_TIME, " +
                    "   SUBS.TIER_ID AS SUB_TIER_ID, " +
                    "   APP.UUID AS APP_UUID, " +
                    "   SUBS.SUBS_CREATE_STATE AS SUBS_CREATE_STATE " +
                    " FROM " +
                    "   AM_SUBSCRIPTION SUBS, " +
                    "   AM_APPLICATION APP, " +
                    "   AM_SUBSCRIBER SUB, " +
                    "   AM_API API " +
                    " WHERE " +
                    "   SUBS.APPLICATION_ID = APP.APPLICATION_ID " +
                    "   AND APP.SUBSCRIBER_ID = SUB.SUBSCRIBER_ID " +
                    "   AND API.API_NAME = ? " +
                    "   AND API.API_VERSION = ? " +
                    "   AND API.API_PROVIDER = ? " +
                    "   AND API.API_ID = SUBS.API_ID " +
                    "   AND SUBS.SUB_STATUS != '" + APIConstants.SubscriptionStatus.REJECTED + "'" +
                    " ORDER BY " +
                    "   APP.NAME";

    public static final String GET_SUBSCRIBER_BY_ID_SQL =
            " SELECT" +
            "   SB.USER_ID, " +
            "   SB.DATE_SUBSCRIBED" +
            " FROM " +
            "   AM_SUBSCRIBER SB , " +
            "   AM_SUBSCRIPTION SP, " +
            "   AM_APPLICATION APP, " +
            "   AM_SUBSCRIPTION_KEY_MAPPING SKM" +
            " WHERE " +
            "   SKM.ACCESS_TOKEN=?" +
            "   AND SP.APPLICATION_ID=APP.APPLICATION_ID" +
            "   AND APP.SUBSCRIBER_ID=SB.SUBSCRIBER_ID" +
            "   AND SP.SUBSCRIPTION_ID=SKM.SUBSCRIPTION_ID";

    public static final String GET_OAUTH_CONSUMER_SQL =
            " SELECT " +
            "   ICA.CONSUMER_KEY AS CONSUMER_KEY," +
            "   ICA.CONSUMER_SECRET AS CONSUMER_SECRET " +
            " FROM " +
            "   AM_SUBSCRIBER SB," +
            "   AM_APPLICATION APP, " +
            "   AM_APPLICATION_KEY_MAPPING AKM," +
            "   IDN_OAUTH_CONSUMER_APPS ICA " +
            " WHERE " +
            "   SB.USER_ID=? " +
            "   AND SB.TENANT_ID=? " +
            "   AND APP.NAME=? " +
            "   AND SB.SUBSCRIBER_ID = APP.SUBSCRIBER_ID " +
            "   AND AKM.APPLICATION_ID = APP.APPLICATION_ID" +
            "   AND ICA.USERNAME = SB.USER_ID" +
            "   AND ICA.TENANT_ID = SB.TENANT_ID" +
            "   AND ICA.APP_NAME = APP.NAME";

    public static final String ADD_OAUTH_CONSUMER_SQL =
            " INSERT INTO IDN_OAUTH_CONSUMER_APPS " +
            " (CONSUMER_KEY, CONSUMER_SECRET, USERNAME, TENANT_ID, OAUTH_VERSION, APP_NAME, CALLBACK_URL) " +
            " VALUES (?,?,?,?,?,?,?) ";

    public static final String UPDATE_OAUTH_CONSUMER_SQL =
            " UPDATE IDN_OAUTH_CONSUMER_APPS SET CALLBACK_URL = ? WHERE APP_NAME = ?";

    public static final String GET_ALL_OAUTH_CONSUMER_APPS_SQL =
            "SELECT * FROM IDN_OAUTH_CONSUMER_APPS WHERE CONSUMER_KEY=?";

    public static final String GET_API_RATING_SQL =
            "SELECT RATING FROM AM_API_RATINGS WHERE API_ID= ? AND SUBSCRIBER_ID=? ";

    public static final String ADD_API_RATING_SQL =
            "INSERT INTO AM_API_RATINGS (RATING_ID, RATING, API_ID, SUBSCRIBER_ID)  VALUES (?,?,?,?)";

    public static final String UPDATE_API_RATING_SQL =
            "UPDATE AM_API_RATINGS SET RATING=? WHERE API_ID= ? AND SUBSCRIBER_ID=?";

    public static final String GET_API_RATING_ID_SQL =
            "SELECT RATING_ID FROM AM_API_RATINGS WHERE API_ID= ? AND SUBSCRIBER_ID=? ";

    public static final String REMOVE_RATING_SQL =
            "DELETE FROM AM_API_RATINGS WHERE RATING_ID =? ";

    public static final String GET_API_RATING_INFO_SQL =
            "SELECT RATING_ID, API_ID, RATING, SUBSCRIBER_ID FROM AM_API_RATINGS WHERE SUBSCRIBER_ID  = ? "
                    + "AND API_ID= ? ";

    public static final String GET_API_ALL_RATINGS_SQL =
            "SELECT RATING_ID, API_ID, RATING, SUBSCRIBER_ID FROM AM_API_RATINGS WHERE API_ID= ? ";

    public static final String GET_SUBSCRIBER_NAME_FROM_ID_SQL =
            "SELECT USER_ID FROM AM_SUBSCRIBER WHERE SUBSCRIBER_ID = ? ";

    public static final String GET_RATING_INFO_BY_ID_SQL =
            "SELECT RATING_ID, API_ID, RATING, SUBSCRIBER_ID FROM AM_API_RATINGS WHERE RATING_ID = ? "
                    + "AND API_ID= ? ";

    public static final String REMOVE_FROM_API_RATING_SQL =
            "DELETE FROM AM_API_RATINGS WHERE API_ID=? ";

    public static final String GET_API_AVERAGE_RATING_SQL =
            " SELECT " +
            "   CAST( SUM(RATING) AS DECIMAL)/COUNT(RATING) AS RATING " +
            " FROM " +
            "   AM_API_RATINGS " +
            " WHERE " +
            "   API_ID =? " +
            " GROUP BY " +
            "   API_ID ";

    public static final String APP_APPLICATION_SQL =
            " INSERT INTO AM_APPLICATION (NAME, SUBSCRIBER_ID, APPLICATION_TIER, " +
            "   CALLBACK_URL, DESCRIPTION, APPLICATION_STATUS, GROUP_ID, CREATED_BY, CREATED_TIME, UPDATED_TIME, " +
                    "UUID, TOKEN_TYPE)" +
            " VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";

    public static final String UPDATE_APPLICATION_SQL =
            " UPDATE " +
            "   AM_APPLICATION" +
            " SET " +
            "   NAME = ?," +
            "   APPLICATION_TIER = ?, " +
            "   CALLBACK_URL = ?, " +
            "   DESCRIPTION = ?, " +
            "   UPDATED_BY = ?, " +
            "   UPDATED_TIME = ?, " +
            "   TOKEN_TYPE = ? " +
            " WHERE" +
            "   APPLICATION_ID = ?";

    public static final String ADD_APPLICATION_ATTRIBUTES_SQL =
            " INSERT INTO AM_APPLICATION_ATTRIBUTES (APPLICATION_ID, NAME, VALUE, TENANT_ID) VALUES (?,?,?,?)";

    public static final String REMOVE_APPLICATION_ATTRIBUTES_SQL =
            " DELETE FROM " +
                    "   AM_APPLICATION_ATTRIBUTES" +
                    " WHERE" +
                    "   APPLICATION_ID = ?";

    public static final String REMOVE_APPLICATION_ATTRIBUTES_BY_ATTRIBUTE_NAME_SQL =
            " DELETE FROM " +
                    "   AM_APPLICATION_ATTRIBUTES" +
                    " WHERE" +
                    "   NAME = ? AND APPLICATION_ID = ?";

    public static final String UPDATE_APPLICATION_STATUS_SQL =
            " UPDATE AM_APPLICATION SET APPLICATION_STATUS = ? WHERE APPLICATION_ID = ?";

    public static final String GET_APPLICATION_STATUS_BY_ID_SQL =
            "SELECT APPLICATION_STATUS FROM AM_APPLICATION WHERE APPLICATION_ID= ?";

    public static final String GET_APPLICATION_ID_PREFIX =
            " SELECT " +
            "   APP.APPLICATION_ID " +
            " FROM " + "   " +
            "   AM_APPLICATION APP," +
            "   AM_SUBSCRIBER SUB " +
            " WHERE " +
            "   LOWER(APP.NAME) = LOWER(?)" + "   " +
            "   AND APP.SUBSCRIBER_ID = SUB.SUBSCRIBER_ID";

    public static final String GET_APPLICATION_ID_SQL =
            "SELECT APPLICATION_ID FROM AM_APPLICATION WHERE  SUBSCRIBER_ID  = ? AND NAME= ?";

    public static final String GET_APPLICATION_NAME_FROM_ID_SQL =
            "SELECT NAME FROM AM_APPLICATION WHERE APPLICATION_ID = ?";

    public static final String GET_BASIC_APPLICATION_DETAILS_PREFIX =
            " SELECT " +
            "   APPLICATION_ID, " +
            "   NAME, " +
            "   APPLICATION_TIER, " +
            "   APP.SUBSCRIBER_ID,  " +
            "   CALLBACK_URL,  " +
            "   DESCRIPTION,  " +
            "   APPLICATION_STATUS,  " +
            "   USER_ID  " +
            " FROM " +
            "   AM_APPLICATION APP, " +
            "   AM_SUBSCRIBER SUB " +
            " WHERE " +
            "   SUB.SUBSCRIBER_ID = APP.SUBSCRIBER_ID ";

    public static final String GET_APPLICATIONS_PREFIX =
            "SELECT " +
            "   APPLICATION_ID, " +
            "   NAME," +
            "   APPLICATION_TIER," +
            "   APP.SUBSCRIBER_ID,  " +
            "   APP.TOKEN_TYPE,  " +
            "   CALLBACK_URL,  " +
            "   DESCRIPTION, " +
            "   APPLICATION_STATUS, " +
            "   USER_ID, " +
            "   GROUP_ID, " +
            "   UUID, " +
            "   APP.CREATED_BY " +
            " FROM" +
            "   AM_APPLICATION APP, " +
            "   AM_SUBSCRIBER SUB  " +
            " WHERE " +
            "   SUB.SUBSCRIBER_ID = APP.SUBSCRIBER_ID ";

    public static final String GET_APPLICATIONS_COUNT =
            "SELECT " +
            "   count(*) count " +
            " FROM" +
            "   AM_APPLICATION APP, " +
            "   AM_SUBSCRIBER SUB  " +
            " WHERE " +
            "   SUB.SUBSCRIBER_ID = APP.SUBSCRIBER_ID " +
            " AND " +
            "   SUB.TENANT_ID=?" +
            " And "+
            "    ( SUB.CREATED_BY like ?" +
            " OR APP.NAME like ? )";

    public static final String GET_APPLICATION_BY_SUBSCRIBERID_AND_NAME_SQL =
            " SELECT " +
                    "   APP.APPLICATION_ID," +
                    "   APP.NAME," +
                    "   APP.SUBSCRIBER_ID," +
                    "   APP.APPLICATION_TIER," +
                    "   APP.CALLBACK_URL," +
                    "   APP.DESCRIPTION, " +
                    "   APP.SUBSCRIBER_ID," +
                    "   APP.APPLICATION_STATUS, " +
                    "   APP.GROUP_ID, " +
                    "   APP.UPDATED_TIME, "+
                    "   APP.CREATED_TIME, "+
                    "   APP.UUID," +
                    "   APP.TOKEN_TYPE," +
                    "   SUB.USER_ID " +
                    " FROM " +
                    "   AM_SUBSCRIBER SUB," +
                    "   AM_APPLICATION APP " +
                    " WHERE " +
                    "    APP.SUBSCRIBER_ID = ? " +
                    "  AND APP.NAME = ? " +
                    "   AND APP.SUBSCRIBER_ID = SUB.SUBSCRIBER_ID";

    public static final String GET_SIMPLE_APPLICATIONS =
            " SELECT " +
            "   APPLICATION_ID, " +
            "   NAME," +
            "   USER_ID, " +
            "   APP.CREATED_BY " +
            " FROM" +
            "   AM_APPLICATION APP, " +
            "   AM_SUBSCRIBER SUB  " +
            " WHERE " +
            "   SUB.SUBSCRIBER_ID = APP.SUBSCRIBER_ID ";

    public static final String GET_APPLICATIONS_BY_OWNER =
            "SELECT " +
            "   UUID, " +
            "   APPLICATION_ID, " +
            "   NAME," +
            "   CREATED_BY, " +
            "   APPLICATION_STATUS, " +
            "   GROUP_ID  " +
            " FROM" +
            "   AM_APPLICATION " +
            " WHERE " +
            "   CREATED_BY = ? ";

    public static final String UPDATE_APPLICATION_OWNER =
            "UPDATE AM_APPLICATION " +
            " SET " +
                "CREATED_BY = ? , " +
                "SUBSCRIBER_ID = ? " +
            " WHERE " +
            "   UUID = ? ";

        public static final String GET_APPLICATIONS_COUNNT_CASESENSITVE_WITHGROUPID = "SELECT " +
                "   count(*) count " +
                " FROM" +
                "   AM_APPLICATION APP, " +
                "   AM_SUBSCRIBER SUB  " +
                " WHERE " +
                "   SUB.SUBSCRIBER_ID = APP.SUBSCRIBER_ID " +
                " AND " +
                "   (GROUP_ID= ?  OR  ((GROUP_ID = '' OR GROUP_ID IS NULL) AND LOWER (SUB.USER_ID) = LOWER(?)))"+
                " And "+
                "    NAME like ?";


        public static final String GET_APPLICATIONS_COUNNT_NONE_CASESENSITVE_WITHGROUPID = "SELECT " +
                "   count(*) count " +
                " FROM" +
                "   AM_APPLICATION APP, " +
                "   AM_SUBSCRIBER SUB  " +
                " WHERE " +
                "   SUB.SUBSCRIBER_ID = APP.SUBSCRIBER_ID " +
                " AND " +
                "   (GROUP_ID= ?  OR ((GROUP_ID = '' OR GROUP_ID IS NULL) AND SUB.USER_ID=?))" +
                " And "+
                "    NAME like ?";

    public static final String GET_APPLICATIONS_COUNNT_CASESENSITVE_WITH_MULTIGROUPID = "SELECT " +
            "   count(*) count " +
            " FROM" +
            "   AM_APPLICATION APP, " +
            "   AM_SUBSCRIBER SUB  " +
            " WHERE " +
            "   SUB.SUBSCRIBER_ID = APP.SUBSCRIBER_ID " +
            " AND (" +
            "   (APPLICATION_ID IN ( SELECT APPLICATION_ID FROM AM_APPLICATION_GROUP_MAPPING WHERE GROUP_ID IN ($params) AND TENANT = ?)) " +
            "           OR   " +
            "   LOWER (SUB.USER_ID) = LOWER(?) )"+
            " And "+
            "    NAME like ?";


    public static final String GET_APPLICATIONS_COUNNT_NONE_CASESENSITVE_WITH_MULTIGROUPID = "SELECT " +
            "   count(*) count " +
            " FROM" +
            "   AM_APPLICATION APP, " +
            "   AM_SUBSCRIBER SUB  " +
            " WHERE " +
            "   SUB.SUBSCRIBER_ID = APP.SUBSCRIBER_ID " +
            " AND (" +
            "    (APPLICATION_ID IN ( SELECT APPLICATION_ID FROM AM_APPLICATION_GROUP_MAPPING WHERE GROUP_ID IN ($params) AND TENANT = ?)) " +
            "        OR " +
            "    SUB.USER_ID = ? )" +
            " And "+
            "    NAME like ?";

        public static final String GET_APPLICATIONS_COUNNT_CASESENSITVE = "SELECT " +
                "   count(*) count " +
                " FROM" +
                "   AM_APPLICATION APP, " +
                "   AM_SUBSCRIBER SUB  " +
                " WHERE " +
                "   SUB.SUBSCRIBER_ID = APP.SUBSCRIBER_ID " +
                " AND " +
                "    LOWER(SUB.USER_ID) = LOWER(?)"+
                " And "+
                "    NAME like ?";

        public static final String GET_APPLICATIONS_COUNNT_NONE_CASESENSITVE = "SELECT " +
                "   count(*) count " +
                " FROM" +
                "   AM_APPLICATION APP, " +
                "   AM_SUBSCRIBER SUB  " +
                " WHERE " +
                "   SUB.SUBSCRIBER_ID = APP.SUBSCRIBER_ID " +
                " AND " +
                "   SUB.USER_ID=?" +
                " And "+
                "    NAME like ?";




        public static final String GET_CONSUMER_KEYS_SQL =
            " SELECT " +
            "   MAP.CONSUMER_KEY " +
            " FROM " +
            "   AM_SUBSCRIPTION SUB, " +
            "   AM_APPLICATION_KEY_MAPPING MAP " +
            " WHERE " +
            "   SUB.APPLICATION_ID = MAP.APPLICATION_ID " +
            "   AND SUB.API_ID = ?";

    public static final String GET_SUBSCRIPTION_ID_OF_APPLICATION_SQL =
            "SELECT SUBSCRIPTION_ID FROM AM_SUBSCRIPTION WHERE APPLICATION_ID = ?";

    public static final String GET_CONSUMER_KEY_OF_APPLICATION_SQL =
            " SELECT" +
            "   CONSUMER_KEY," +
            "   CREATE_MODE," +
            "   KEY_MANAGER" +
                    " FROM" +
            "   AM_APPLICATION_KEY_MAPPING " +
            " WHERE" +
            "   APPLICATION_ID = ?";

    public static final String REMOVE_APPLICATION_FROM_SUBSCRIPTION_KEY_MAPPINGS_SQL =
            "DELETE FROM AM_SUBSCRIPTION_KEY_MAPPING WHERE SUBSCRIPTION_ID = ?";

    public static final String REMOVE_APPLICATION_FROM_SUBSCRIPTIONS_SQL =
            "DELETE FROM AM_SUBSCRIPTION WHERE APPLICATION_ID = ?";

    public static final String REMOVE_APPLICATION_FROM_APPLICATION_KEY_MAPPINGS_SQL =
            "DELETE FROM AM_APPLICATION_KEY_MAPPING WHERE APPLICATION_ID = ?";

    public static final String REMOVE_APPLICATION_FROM_DOMAIN_MAPPINGS_SQL =
            "DELETE FROM AM_APP_KEY_DOMAIN_MAPPING WHERE CONSUMER_KEY = ?";

    public static final String REMOVE_APPLICATION_FROM_APPLICATIONS_SQL =
            "DELETE FROM AM_APPLICATION WHERE APPLICATION_ID = ?";

    public static final String REMOVE_APPLICATION_FROM_APPLICATION_REGISTRATIONS_SQL =
            "DELETE FROM AM_APPLICATION_REGISTRATION WHERE APP_ID = ?";

    public static final String GET_CONSUMER_KEY_WITH_MODE_SLQ =
            " SELECT" +
            "   CONSUMER_KEY, " +
            "   KEY_TYPE" +
            " FROM" +
            "   AM_APPLICATION_KEY_MAPPING " +
            " WHERE" +
            "   APPLICATION_ID = ? AND " +
            "   CREATE_MODE = ?";

    public static final String GET_CONSUMER_KEY_FOR_APPLICATION_KEY_TYPE_SQL =
            " SELECT " +
            "   AKM.CONSUMER_KEY " +
            " FROM " +
            "   AM_APPLICATION APP," +
            "   AM_APPLICATION_KEY_MAPPING AKM," +
            "   AM_SUBSCRIBER SUB " +
            " WHERE " +
            "   SUB.SUBSCRIBER_ID=APP.SUBSCRIBER_ID " +
            "   AND APP.APPLICATION_ID = AKM.APPLICATION_ID " +
            "   AND APP.NAME = ? AND AKM.KEY_TYPE=?  ";

    public static final String GET_CONSUMER_KEY_FOR_APPLICATION_KEY_TYPE_BY_APP_ID_SQL =
            " SELECT " +
                    "   AKM.CONSUMER_KEY " +
                    " FROM " +
                    "   AM_APPLICATION APP," +
                    "   AM_APPLICATION_KEY_MAPPING AKM," +
                    "   AM_SUBSCRIBER SUB " +
                    " WHERE " +
                    "   SUB.SUBSCRIBER_ID=APP.SUBSCRIBER_ID " +
                    "   AND APP.APPLICATION_ID = AKM.APPLICATION_ID " +
                    "   AND APP.APPLICATION_ID = ? AND AKM.KEY_TYPE=?  ";

    public static final String GET_APPLICATION_ID_BY_CONSUMER_KEY_SQL =
            " SELECT " +
            "   MAP.APPLICATION_ID, " +
            "   MAP.KEY_TYPE " +
            " FROM " +
            "   AM_APPLICATION_KEY_MAPPING MAP " +
            " WHERE " +
            "   MAP.CONSUMER_KEY = ? ";

    public static final String DELETE_APPLICATION_KEY_MAPPING_BY_CONSUMER_KEY_SQL =
            "DELETE FROM AM_APPLICATION_KEY_MAPPING WHERE CONSUMER_KEY = ?";

    public static final String DELETE_APPLICATION_KEY_MAPPING_BY_UUID_SQL =
            "DELETE FROM AM_APPLICATION_KEY_MAPPING WHERE UUID = ?";

    public static final String DELETE_APPLICATION_KEY_MAPPING_BY_APPLICATION_ID_SQL =
            "DELETE FROM AM_APPLICATION_KEY_MAPPING WHERE APPLICATION_ID = ? AND KEY_TYPE = ?";

    public static final String REMOVE_FROM_APPLICATION_REGISTRANTS_SQL =
            "DELETE FROM AM_APPLICATION_REGISTRATION WHERE APP_ID = ? AND TOKEN_TYPE = ? AND KEY_MANAGER = ?";

    public static final String GET_SUBSCRIBER_CASE_INSENSITIVE_SQL =
            " SELECT " +
            "   SUB.SUBSCRIBER_ID AS SUBSCRIBER_ID," +
            "   SUB.USER_ID AS USER_ID, " +
            "   SUB.TENANT_ID AS TENANT_ID," +
            "   SUB.EMAIL_ADDRESS AS EMAIL_ADDRESS," +
            "   SUB.DATE_SUBSCRIBED AS DATE_SUBSCRIBED " +
            " FROM " +
            "   AM_SUBSCRIBER SUB " +
            " WHERE " +
            "   LOWER(SUB.USER_ID) = LOWER(?) " +
            "   AND SUB.TENANT_ID = ?";

    public static final String GET_SUBSCRIBER_DETAILS_SQL =
            " SELECT " +
            "   SUB.SUBSCRIBER_ID AS SUBSCRIBER_ID," +
            "   SUB.USER_ID AS USER_ID, " +
            "   SUB.TENANT_ID AS TENANT_ID," +
            "   SUB.EMAIL_ADDRESS AS EMAIL_ADDRESS," +
            "   SUB.DATE_SUBSCRIBED AS DATE_SUBSCRIBED " +
            " FROM " +
            "   AM_SUBSCRIBER SUB " +
            " WHERE " +
            "   SUB.USER_ID = ? " +
            "   AND SUB.TENANT_ID = ?";

    public static final String GET_API_ID_SQL =
            "SELECT API.API_ID FROM AM_API API WHERE API.API_PROVIDER = ? AND API.API_NAME = ? AND API.API_VERSION = ? ";

    public static final String GET_API_PRODUCT_ID_SQL =
            "SELECT API_ID FROM AM_API WHERE API_PROVIDER = ? AND API_NAME = ? "
                    + "AND API_VERSION = ? AND API_TYPE = '" + APIConstants.API_PRODUCT + "'";

    public static final String GET_API_PRODUCT_SQL =
            "SELECT API_ID, API_TIER FROM AM_API WHERE API_PROVIDER = ? " +
                    "AND API_NAME = ? AND API_VERSION = ? AND API_TYPE = '" + APIConstants.API_PRODUCT + "'";

    public static final String GET_AUDIT_UUID_SQL =
            "SELECT MAP.AUDIT_UUID FROM AM_SECURITY_AUDIT_UUID_MAPPING MAP WHERE MAP.API_ID = ?";

    public static final String ADD_SECURITY_AUDIT_MAP_SQL =
            "INSERT INTO AM_SECURITY_AUDIT_UUID_MAPPING (API_ID, AUDIT_UUID) VALUES (?,?)";

    public static final String REMOVE_SECURITY_AUDIT_MAP_SQL =
            "DELETE FROM AM_SECURITY_AUDIT_UUID_MAPPING WHERE API_ID = ?";

    public static final String ADD_CUSTOM_COMPLEXITY_DETAILS_SQL =
            "INSERT INTO AM_GRAPHQL_COMPLEXITY (UUID, API_ID, TYPE, FIELD, COMPLEXITY_VALUE) VALUES (?,?,?,?,?)";

    public static final String GET_CUSTOM_COMPLEXITY_DETAILS_SQL =
            " SELECT" +
            "   TYPE," +
            "   FIELD," +
            "   COMPLEXITY_VALUE" +
            " FROM" +
            "   AM_GRAPHQL_COMPLEXITY " +
            " WHERE" +
            "   API_ID = ?";


    public static final String UPDATE_CUSTOM_COMPLEXITY_DETAILS_SQL =
            " UPDATE AM_GRAPHQL_COMPLEXITY " +
            " SET " +
            "   COMPLEXITY_VALUE = ? " +
            " WHERE " +
            "    API_ID = ?" +
            "    AND TYPE = ? " +
            "    AND FIELD = ?";

    public static final String REMOVE_FROM_GRAPHQL_COMPLEXITY_SQL =
            "DELETE FROM AM_GRAPHQL_COMPLEXITY WHERE API_ID = ?";

    public static final String ADD_API_LIFECYCLE_EVENT_SQL =
            " INSERT INTO AM_API_LC_EVENT (API_ID, PREVIOUS_STATE, NEW_STATE, USER_ID, TENANT_ID, EVENT_DATE)" +
            " VALUES (?,?,?,?,?,?)";

    public static final String GET_LIFECYCLE_EVENT_SQL =
            " SELECT" +
            "   LC.API_ID AS API_ID," +
            "   LC.PREVIOUS_STATE AS PREVIOUS_STATE," +
            "   LC.NEW_STATE AS NEW_STATE," +
            "   LC.USER_ID AS USER_ID," +
            "   LC.EVENT_DATE AS EVENT_DATE " +
            " FROM" +
            "   AM_API_LC_EVENT LC, " +
            "   AM_API API " +
            " WHERE" +
            "   API.API_PROVIDER = ?" +
            "   AND API.API_NAME = ?" +
            "   AND API.API_VERSION = ?" +
            "   AND API.API_ID = LC.API_ID";

    public static final String GET_SUBSCRIPTION_DATA_SQL =
            " SELECT" +
            "   SUB.SUBSCRIPTION_ID AS SUBSCRIPTION_ID," +
            "   SUB.TIER_ID AS TIER_ID," +
            "   SUB.APPLICATION_ID AS APPLICATION_ID," +
            "   SUB.SUB_STATUS AS SUB_STATUS," +
            "   API.CONTEXT AS CONTEXT," +
            "   SKM.ACCESS_TOKEN AS ACCESS_TOKEN," +
            "   SKM.KEY_TYPE AS KEY_TYPE" +
            " FROM" +
            "   AM_SUBSCRIPTION SUB," +
            "   AM_SUBSCRIPTION_KEY_MAPPING SKM, " +
            "   AM_API API " +
            " WHERE" +
            "   API.API_PROVIDER = ?" +
            "   AND API.API_NAME = ?" +
            "   AND API.API_VERSION = ?" +
            "   AND SKM.SUBSCRIPTION_ID = SUB.SUBSCRIPTION_ID" +
            "   AND API.API_ID = SUB.API_ID";

    public static final String ADD_SUBSCRIPTION_KEY_MAPPING_SQL =
            " INSERT INTO AM_SUBSCRIPTION_KEY_MAPPING (SUBSCRIPTION_ID, ACCESS_TOKEN, KEY_TYPE)" +
            " VALUES (?,?,?)";

    public static final String GET_APPLICATION_DATA_SQL =
            " SELECT" +
            "   SUB.SUBSCRIPTION_ID AS SUBSCRIPTION_ID," +
            "   SUB.TIER_ID AS TIER_ID," +
            "   SUB.SUB_STATUS AS SUB_STATUS," +
            "   APP.APPLICATION_ID AS APPLICATION_ID," +
            "   API.CONTEXT AS CONTEXT " +
            " FROM" +
            "   AM_SUBSCRIPTION SUB," +
            "   AM_APPLICATION APP," +
            "   AM_API API " +
            " WHERE" +
            "   API.API_PROVIDER = ?" +
            "   AND API.API_NAME = ?" +
            "   AND API.API_VERSION = ?" +
            "   AND SUB.APPLICATION_ID = APP.APPLICATION_ID" +
            "   AND API.API_ID = SUB.API_ID" +
            "   AND SUB.SUB_STATUS != '" + APIConstants.SubscriptionStatus.ON_HOLD + "'";

    public static final String ADD_API_SQL =
            " INSERT INTO AM_API (API_PROVIDER,API_NAME,API_VERSION,CONTEXT,CONTEXT_TEMPLATE,CREATED_BY,CREATED_TIME, API_TIER, API_TYPE)" +
            " VALUES (?,?,?,?,?,?,?,?,?)";

    public static final String GET_DEFAULT_VERSION_SQL =
            "SELECT DEFAULT_API_VERSION FROM AM_API_DEFAULT_VERSION WHERE API_NAME= ? AND API_PROVIDER= ? ";

    public static final String ADD_WORKFLOW_ENTRY_SQL =
            " INSERT INTO AM_WORKFLOWS (WF_REFERENCE,WF_TYPE,WF_STATUS,WF_CREATED_TIME,WF_STATUS_DESC,TENANT_ID," +
            "TENANT_DOMAIN,WF_EXTERNAL_REFERENCE,WF_METADATA,WF_PROPERTIES)" +
            " VALUES (?,?,?,?,?,?,?,?,?,?)";

    public static final String UPDATE_WORKFLOW_ENTRY_SQL =
            " UPDATE AM_WORKFLOWS " +
            " SET " +
            "   WF_STATUS = ?, " +
            "   WF_STATUS_DESC = ? " +
            " WHERE " +
           "    WF_EXTERNAL_REFERENCE = ?";

    public static final String GET_ALL_WORKFLOW_ENTRY_SQL =
            "SELECT * FROM AM_WORKFLOWS WHERE WF_EXTERNAL_REFERENCE=?";

    public static final String GET_ALL_WORKFLOW_ENTRY_FROM_INTERNAL_REF_SQL =
            "SELECT * FROM AM_WORKFLOWS WHERE WF_REFERENCE=? AND WF_TYPE=?";

    public static final String ADD_PAYLOAD_SQL =
            " UPDATE AM_WORKFLOWS " +
                    " SET " +
                    "   WF_METADATA = ?, " +
                    "   WF_PROPERTIES = ?, " +
                    "   WF_STATUS_DESC = ? " +
                    " WHERE " +
                    "    WF_EXTERNAL_REFERENCE = ?";

    public static final String DELETE_WORKFLOW_REQUEST_SQL=
            " DELETE FROM AM_WORKFLOWS WHERE WF_EXTERNAL_REFERENCE = ?";

    public static final String GET_ALL_WORKFLOW_DETAILS_BY_EXTERNALWORKFLOWREF =
            " SELECT  * FROM AM_WORKFLOWS WHERE WF_EXTERNAL_REFERENCE = ?";

    public static final String GET_ALL_WORKFLOW_DETAILS_BY_WORKFLOW_TYPE =
            " SELECT  * FROM AM_WORKFLOWS WHERE WF_TYPE = ? AND  WF_STATUS = ? AND TENANT_DOMAIN = ?";

    public static final String GET_ALL_WORKFLOW_DETAILS =
            " SELECT  * FROM AM_WORKFLOWS WHERE WF_STATUS = ? AND TENANT_DOMAIN = ?";

    public static final String GET_ALL_WORKFLOW_DETAILS_BY_EXTERNAL_WORKFLOW_REFERENCE =
            " SELECT  * FROM AM_WORKFLOWS " +
            " WHERE WF_EXTERNAL_REFERENCE = ? " +
            " AND WF_STATUS = ? " +
            " AND TENANT_DOMAIN = ?";

    public static final String UPDATE_PUBLISHED_DEFAULT_VERSION_SQL =
            " UPDATE AM_API_DEFAULT_VERSION " +
            " SET " +
            "   PUBLISHED_DEFAULT_API_VERSION = ? " +
            " WHERE" +
            "   API_NAME = ? " +
            "   AND API_PROVIDER = ?";

    public static final String REMOVE_API_DEFAULT_VERSION_SQL =
            "DELETE FROM AM_API_DEFAULT_VERSION WHERE API_NAME = ? AND API_PROVIDER = ?";

    public static final String GET_PUBLISHED_DEFAULT_VERSION_SQL =
            "SELECT PUBLISHED_DEFAULT_API_VERSION FROM AM_API_DEFAULT_VERSION WHERE API_NAME= ? AND API_PROVIDER= ? ";

    public static final String ADD_API_DEFAULT_VERSION_SQL =
            " INSERT INTO " +
            " AM_API_DEFAULT_VERSION(API_NAME,API_PROVIDER,DEFAULT_API_VERSION,PUBLISHED_DEFAULT_API_VERSION)" +
            " VALUES (?,?,?,?)";

    public static final String ADD_URL_MAPPING_SQL =
            " INSERT INTO " +
            " AM_API_URL_MAPPING (API_ID,HTTP_METHOD,AUTH_SCHEME,URL_PATTERN,THROTTLING_TIER,MEDIATION_SCRIPT)" +
            " VALUES (?,?,?,?,?,?)";

    public static final String GET_APPLICATION_BY_NAME_PREFIX =
            " SELECT " +
            "   APP.APPLICATION_ID," +
            "   APP.NAME," +
            "   APP.SUBSCRIBER_ID," +
            "   APP.APPLICATION_TIER," +
            "   APP.CALLBACK_URL," +
            "   APP.DESCRIPTION, " +
            "   APP.SUBSCRIBER_ID," +
            "   APP.APPLICATION_STATUS," +
            "   APP.GROUP_ID," +
            "   APP.UUID," +
            "   APP.CREATED_BY," +
            "   APP.TOKEN_TYPE," +
            "   SUB.USER_ID," +
            "   APP.CREATED_BY" +
            " FROM " +
            "   AM_SUBSCRIBER SUB," +
            "   AM_APPLICATION APP";

    public static final String GET_APPLICATION_ATTRIBUTES_BY_APPLICATION_ID =
            " SELECT " +
                    "   APP.APPLICATION_ID," +
                    "   APP.NAME," +
                    "   APP.VALUE" +
                    " FROM " +
                    "   AM_APPLICATION_ATTRIBUTES APP WHERE APPLICATION_ID = ?";

    public static final String GET_APPLICATION_BY_ID_SQL =
            " SELECT " +
            "   APP.APPLICATION_ID," +
            "   APP.NAME," +
            "   APP.SUBSCRIBER_ID," +
            "   APP.APPLICATION_TIER," +
            "   APP.CALLBACK_URL," +
            "   APP.DESCRIPTION, " +
            "   APP.SUBSCRIBER_ID," +
            "   APP.APPLICATION_STATUS, " +
            "   SUB.USER_ID, " +
            "   APP.GROUP_ID," +
            "   APP.CREATED_BY," +
            "   APP.UUID, " +
            "   APP.TOKEN_TYPE " +
            " FROM " +
            "   AM_SUBSCRIBER SUB," +
            "   AM_APPLICATION APP " +
            " WHERE " +
            "   APPLICATION_ID = ? " +
            "   AND APP.SUBSCRIBER_ID = SUB.SUBSCRIBER_ID";

    public static final String GET_APPLICATION_BY_UUID_SQL =
            " SELECT " +
            "   APP.APPLICATION_ID," +
            "   APP.NAME," +
            "   APP.SUBSCRIBER_ID," +
            "   APP.APPLICATION_TIER," +
            "   APP.CALLBACK_URL," +
            "   APP.DESCRIPTION, " +
            "   APP.SUBSCRIBER_ID," +
            "   APP.APPLICATION_STATUS, " +
            "   APP.GROUP_ID, " +
            "   APP.UPDATED_TIME, "+
            "   APP.CREATED_TIME, "+
            "   APP.UUID," +
            "   APP.TOKEN_TYPE," +
            "   APP.CREATED_BY," +
            "   SUB.USER_ID " +
            " FROM " +
            "   AM_SUBSCRIBER SUB," +
            "   AM_APPLICATION APP " +
            " WHERE " +
            "   APP.UUID = ? " +
            "   AND APP.SUBSCRIBER_ID = SUB.SUBSCRIBER_ID";

    public static final String GET_APPLICATION_BY_CLIENT_ID_SQL =
            " SELECT " +
                    "   APP.APPLICATION_ID," +
                    "   APP.NAME," +
                    "   APP.SUBSCRIBER_ID," +
                    "   APP.APPLICATION_TIER," +
                    "   APP.CALLBACK_URL," +
                    "   APP.DESCRIPTION, " +
                    "   APP.SUBSCRIBER_ID," +
                    "   APP.APPLICATION_STATUS, " +
                    "   APP.GROUP_ID, " +
                    "   APP.UPDATED_TIME, "+
                    "   APP.CREATED_TIME, "+
                    "   APP.UUID," +
                    "   APP.CREATED_BY," +
                    "   APP.TOKEN_TYPE," +
                    "   AM_APP_MAP.KEY_TYPE" +
                    " FROM " +
                    "   AM_APPLICATION_KEY_MAPPING AM_APP_MAP," +
                    "   AM_APPLICATION APP " +
                    " WHERE " +
                    "   AM_APP_MAP.CONSUMER_KEY = ? " +
                    "   AND APP.APPLICATION_ID = AM_APP_MAP.APPLICATION_ID";

    public static final String REMOVE_FROM_URI_TEMPLATES_SQL =
            "DELETE FROM AM_API_URL_MAPPING WHERE API_ID = ?";

    public static final String GET_ALL_URL_TEMPLATES_SQL =
            " SELECT    " +
                    "   AUM.HTTP_METHOD," +
                    "   AUM.AUTH_SCHEME," +
                    "   AUM.URL_PATTERN," +
                    "   AUM.THROTTLING_TIER," +
                    "   AUM.MEDIATION_SCRIPT, " +
                    "   AUM.URL_MAPPING_ID " +
                    " FROM " +
                    "   AM_API_URL_MAPPING AUM, " +
                    "   AM_API API " +
                    " WHERE" +
                    "   API.CONTEXT= ? " +
                    "   AND API.API_VERSION = ? " +
                    "   AND AUM.API_ID = API.API_ID " +
                    " ORDER BY URL_MAPPING_ID";

    public static final String UPDATE_API_SQL =
            "UPDATE AM_API " +
            "SET " +
            "   CONTEXT = ?, " +
            "   CONTEXT_TEMPLATE = ?, " +
            "   UPDATED_BY = ?," +
            "   UPDATED_TIME = ?, " +
            "   API_TIER = ?, " +
            "   API_TYPE = ? " +
            " WHERE " +
            "   API_PROVIDER = ? " +
            "   AND API_NAME = ? " +
            "   AND" + " API_VERSION = ? ";

    public static final String FIX_NULL_THROTTLING_TIERS =
            "UPDATE AM_API_URL_MAPPING SET THROTTLING_TIER = 'Unlimited' WHERE " +
                     " THROTTLING_TIER IS NULL";

    public static final String REMOVE_APPLICATION_MAPPINGS_BY_CONSUMER_KEY_SQL =
            "DELETE FROM AM_APPLICATION_KEY_MAPPING WHERE CONSUMER_KEY = ?";

    public static final String REMOVE_FROM_API_LIFECYCLE_SQL =
            "DELETE FROM AM_API_LC_EVENT WHERE API_ID=? ";

    public static final String REMOVE_FROM_API_COMMENT_SQL =
            "DELETE FROM AM_API_COMMENTS WHERE API_ID=? ";

    public static final String REMOVE_FROM_API_SUBSCRIPTION_SQL =
            "DELETE FROM AM_SUBSCRIPTION WHERE API_ID=?";

    public static final String REMOVE_FROM_EXTERNAL_STORES_SQL =
            "DELETE FROM AM_EXTERNAL_STORES WHERE API_ID=?";

    public static final String REMOVE_FROM_API_SQL =
            "DELETE FROM AM_API WHERE API_PROVIDER=? AND API_NAME=? AND API_VERSION=? ";

    public static final String REMOVE_FROM_API_URL_MAPPINGS_SQL =
            "DELETE FROM AM_API_URL_MAPPING WHERE API_ID = ?";

    public static final String REMOVE_ACCESS_TOKEN_PREFIX = "UPDATE ";

    public static final String REVOKE_ACCESS_TOKEN_SUFFIX =
            " SET TOKEN_STATE" + "='REVOKED' WHERE ACCESS_TOKEN= ? ";

    public static final String GET_API_BY_ACCESS_TOKEN_PREFIX =
            " SELECT AMA.API_ID, API_NAME, API_PROVIDER, API_VERSION " +
            " FROM AM_API AMA, ";

    public static final String GET_API_BY_ACCESS_TOKEN_SUFFIX =
            " IAT, " +
            "   AM_APPLICATION_KEY_MAPPING AKM, " +
            "   AM_SUBSCRIPTION AMS, " +
            "   IDN_OAUTH_CONSUMER_APPS ICA " +
            " WHERE IAT.ACCESS_TOKEN = ? " +
            "   AND ICA.CONSUMER_KEY = AKM.CONSUMER_KEY " +
            "   AND IAT.CONSUMER_KEY_ID = ICA.ID " +
            "   AND AKM.APPLICATION_ID = AMS.APPLICATION_ID " +
            "   AND AMA.API_ID = AMS.API_ID";

    public static final String GET_APPLICATION_BY_TIER_SQL =
            " SELECT DISTINCT AMS.APPLICATION_ID,NAME,SUBSCRIBER_ID " +
            " FROM " +
            "   AM_SUBSCRIPTION AMS," +
            "   AM_APPLICATION AMA " +
            "WHERE " +
            "   AMS.TIER_ID=? " +
            "   AND AMS.APPLICATION_ID=AMA.APPLICATION_ID";

    public static final String GET_URL_TEMPLATES_SQL =
            " SELECT " +
            "   URL_PATTERN," +
            "   HTTP_METHOD," +
            "   AUTH_SCHEME," +
            "   THROTTLING_TIER, " +
            "   MEDIATION_SCRIPT " +
            " FROM " +
            "   AM_API_URL_MAPPING " +
            " WHERE " +
            "   API_ID = ? " +
            " ORDER BY " +
            "   URL_MAPPING_ID ASC ";

    public static final String GET_URL_TEMPLATES_OF_API_SQL =
            " SELECT " +
            "  AUM.URL_MAPPING_ID," +
            "   AUM.URL_PATTERN," +
            "   AUM.HTTP_METHOD," +
            "   AUM.AUTH_SCHEME," +
            "   AUM.THROTTLING_TIER," +
            "   AUM.MEDIATION_SCRIPT," +
            "   ARSM.SCOPE_NAME " +
            " FROM " +
            "   AM_API_URL_MAPPING AUM " +
            " INNER JOIN AM_API API ON AUM.API_ID = API.API_ID " +
            " LEFT OUTER JOIN AM_API_RESOURCE_SCOPE_MAPPING ARSM ON AUM.URL_MAPPING_ID = ARSM.URL_MAPPING_ID" +
            " WHERE " +
            "  API.API_PROVIDER = ? AND " +
            "  API.API_NAME = ? AND " +
            "  API.API_VERSION = ? " +
            " ORDER BY AUM.URL_MAPPING_ID ASC ";

    public static final String GET_API_PRODUCT_URI_TEMPLATE_ASSOCIATION_SQL =
            " SELECT " +
            "  API.API_PROVIDER," +
            "  API.API_NAME," +
            "  API.API_VERSION," +
            "  APM.URL_MAPPING_ID  " +
            "  FROM " +
            "  AM_API API " +
            "  INNER JOIN AM_API_PRODUCT_MAPPING APM ON API.API_ID = APM.API_ID " +
            "  WHERE APM.URL_MAPPING_ID IN " +
            "   (SELECT AUM.URL_MAPPING_ID " +
            "   FROM AM_API_URL_MAPPING AUM " +
            "   INNER JOIN AM_API API ON AUM.API_ID = API.API_ID " +
            "   WHERE API.API_PROVIDER = ? AND " +
            "   API.API_NAME = ? AND API.API_VERSION = ?)";

    public static final String GET_AUTHORIZED_DOMAINS_PREFIX =
            "SELECT AKDM.AUTHZ_DOMAIN FROM AM_APP_KEY_DOMAIN_MAPPING AKDM, ";

    public static final String GET_AUTHORIZED_DOMAINS_SUFFIX =
            "   IOAT, " +
            "   IDN_OAUTH_CONSUMER_APPS IOCA " +
            " WHERE " +
            "   IOAT.ACCESS_TOKEN  = ? " +
            "   AND IOAT.CONSUMER_KEY_ID = IOCA.ID " +
            "   AND IOCA.CONSUMER_KEY = AKDM.CONSUMER_KEY";

    public static final String GET_AUTHORIZED_DOMAINS_BY_ACCESS_KEY_SQL =
            "SELECT AUTHZ_DOMAIN FROM AM_APP_KEY_DOMAIN_MAPPING WHERE CONSUMER_KEY = ? ";

    public static final String GET_CONSUMER_KEY_BY_ACCESS_TOKEN_PREFIX =
            "SELECT ICA.CONSUMER_KEY FROM ";

    public static final String GET_CONSUMER_KEY_BY_ACCESS_TOKEN_SUFFIX =
            "   IAT," +
            "   IDN_OAUTH_CONSUMER_APPS ICA" +
            " WHERE " +
            "   IAT.ACCESS_TOKEN = ? " +
            "   AND ICA.ID = IAT.CONSUMER_KEY_ID";

    public static final String ADD_COMMENT_SQL =
            " INSERT INTO AM_API_COMMENTS (COMMENT_ID,COMMENT_TEXT,COMMENTED_USER,DATE_COMMENTED,API_ID)" +
            " VALUES (?,?,?,?,?)";

    public static final String GET_COMMENT_SQL =
            " SELECT AM_API_COMMENTS.COMMENT_ID AS COMMENT_ID," +
            "   AM_API_COMMENTS.COMMENT_TEXT AS COMMENT_TEXT," +
            "   AM_API_COMMENTS.COMMENTED_USER AS COMMENTED_USER," +
            "   AM_API_COMMENTS.DATE_COMMENTED AS DATE_COMMENTED " +
            " FROM AM_API_COMMENTS, AM_API API " +
            " WHERE API.API_PROVIDER = ? " +
            "   AND API.API_NAME = ? " +
            "   AND API.API_VERSION = ? " +
            "   AND API.API_ID = AM_API_COMMENTS.API_ID " +
            "   AND AM_API_COMMENTS.COMMENT_ID = ?";

    public static final String GET_COMMENTS_SQL =
            " SELECT AM_API_COMMENTS.COMMENT_ID AS COMMENT_ID," +
            "   AM_API_COMMENTS.COMMENT_TEXT AS COMMENT_TEXT," +
            "   AM_API_COMMENTS.COMMENTED_USER AS COMMENTED_USER," +
            "   AM_API_COMMENTS.DATE_COMMENTED AS DATE_COMMENTED " +
            " FROM " +
            "   AM_API_COMMENTS, " +
            "   AM_API API " +
            " WHERE " +
            "   API.API_PROVIDER = ? " +
            "   AND API.API_NAME = ? " +
            "   AND API.API_VERSION  = ? " +
            "   AND API.API_ID = AM_API_COMMENTS.API_ID";

    public static final String DELETE_COMMENT_SQL = "DELETE FROM AM_API_COMMENTS WHERE AM_API_COMMENTS.COMMENT_ID = ?";

    public static final String GET_API_CONTEXT_SQL =
            "SELECT CONTEXT FROM AM_API " + " WHERE CONTEXT= ?";

    public static final String GET_API_CONTEXT_BY_API_NAME_SQL =
            "SELECT CONTEXT FROM AM_API WHERE API_PROVIDER = ? AND API_NAME = ? AND API_VERSION  = ?";

    public static final String GET_ALL_CONTEXT_SQL = "SELECT CONTEXT FROM AM_API ";

    public static final String GET_APPLICATION_REGISTRATION_ENTRY_BY_SUBSCRIBER_SQL =
            "SELECT " +
            "   APP.APPLICATION_ID," +
            "   APP.NAME," +
            "   APP.SUBSCRIBER_ID," +
            "   APP.APPLICATION_TIER," +
            "   REG.TOKEN_TYPE," +
            "   REG.TOKEN_SCOPE," +
            "   APP.CALLBACK_URL," +
            "   APP.DESCRIPTION," +
            "   APP.APPLICATION_STATUS," +
            "   SUB.USER_ID," +
            "   REG.ALLOWED_DOMAINS," +
            "   REG.VALIDITY_PERIOD," +
            "   REG.INPUTS, REG.KEY_MANAGER" +
            " FROM " +
            "   AM_APPLICATION_REGISTRATION REG," +
            "   AM_APPLICATION APP," +
            "   AM_SUBSCRIBER SUB" +
            " WHERE " +
            "   REG.SUBSCRIBER_ID=SUB.SUBSCRIBER_ID " +
            "   AND REG.APP_ID = APP.APPLICATION_ID " +
            "   AND REG.WF_REF=?";

    public static final String GET_APPLICATION_REGISTRATION_ENTRY_SQL =
            "SELECT " +
            "   REG.TOKEN_TYPE," +
            "   REG.ALLOWED_DOMAINS," +
            "   REG.VALIDITY_PERIOD" +
            " FROM " +
            "   AM_APPLICATION_REGISTRATION REG, " +
            "   AM_APPLICATION APP " +
            " WHERE " +
            "   REG.APP_ID = APP.APPLICATION_ID " +
            "   AND APP.APPLICATION_ID=?";

    public static final String GET_APPLICATION_REGISTRATION_ID_SQL =
            "SELECT APP_ID FROM AM_APPLICATION_REGISTRATION WHERE WF_REF=?";

    public static final String GET_WORKFLOW_ENTRY_SQL =
            "SELECT " +
            "   REG.WF_REF" +
            " FROM " +
            "   AM_APPLICATION APP, " +
            "   AM_APPLICATION_REGISTRATION REG, " +
            "   AM_SUBSCRIBER SUB" +
            " WHERE " +
            "   APP.NAME=? " +
            "   AND SUB.USER_ID=? " +
            "   AND SUB.SUBSCRIBER_ID = APP.SUBSCRIBER_ID " +
            "   AND REG.APP_ID=APP.APPLICATION_ID";

    public static final String GET_WORKFLOW_ENTRY_BY_APP_ID_SQL =
            "SELECT " +
                    "   REG.WF_REF" +
                    " FROM " +
                    "   AM_APPLICATION APP, " +
                    "   AM_APPLICATION_REGISTRATION REG, " +
                    "   AM_SUBSCRIBER SUB" +
                    " WHERE " +
                    "   APP.APPLICATION_ID=? " +
                    "   AND SUB.USER_ID=? " +
                    "   AND SUB.SUBSCRIBER_ID = APP.SUBSCRIBER_ID " +
                    "   AND REG.APP_ID=APP.APPLICATION_ID";

    public static final String GET_EXTERNAL_WORKFLOW_REFERENCE_SQL =
            "SELECT WF_EXTERNAL_REFERENCE FROM AM_WORKFLOWS WHERE WF_TYPE=? AND WF_REFERENCE=?";

    public static final String REMOVE_WORKFLOW_ENTRY_SQL =
            "DELETE FROM AM_WORKFLOWS WHERE WF_TYPE=? AND WF_EXTERNAL_REFERENCE=?";

    public static final String GET_EXTERNAL_WORKFLOW_REFERENCE_FOR_SUBSCRIPTION_SQL =
            "SELECT " +
            "   AW.WF_EXTERNAL_REFERENCE " +
            " FROM" +
            "   AM_WORKFLOWS AW, " +
            "   AM_SUBSCRIPTION ASUB " +
            " WHERE" +
            "   ASUB.API_ID=? " +
            "   AND ASUB.APPLICATION_ID=? " +
            "   AND AW.WF_REFERENCE=ASUB.SUBSCRIPTION_ID " +
            "   AND AW.WF_TYPE=?";

    public static final String GET_EXTERNAL_WORKFLOW_REFERENCE_FOR_SUBSCRIPTION_POSTGRE_SQL =
            "SELECT" +
            "   AW.WF_EXTERNAL_REFERENCE" +
            " FROM" +
            "   AM_WORKFLOWS AW, " +
            "   AM_SUBSCRIPTION ASUB  " +
            " WHERE" +
            "   ASUB.API_ID=? " +
            "   AND ASUB.APPLICATION_ID=?" +
            "   AND AW.WF_REFERENCE::integer=ASUB.SUBSCRIPTION_ID " +
            "   AND AW.WF_TYPE=?";

    public static final String GET_EXTERNAL_WORKFLOW_FOR_SUBSCRIPTION_SQL =
            " SELECT " +
            "   WF_EXTERNAL_REFERENCE" +
            " FROM " +
            "   AM_WORKFLOWS" +
            " WHERE " +
            "   WF_REFERENCE=?" +
            "   AND WF_TYPE=?";

    public static final String GET_EXTERNAL_WORKFLOW_FOR_SIGNUP_SQL =
            "SELECT " +
            "   WF_EXTERNAL_REFERENCE" +
            " FROM " +
            "   AM_WORKFLOWS WHERE " +
            "   WF_REFERENCE=? " +
            "   AND WF_TYPE=?";

    public static final String GET_PAGINATED_SUBSCRIPTIONS_BY_APPLICATION_SQL =
            "SELECT" +
            "   SUBSCRIPTION_ID " +
            " FROM " +
            "   AM_SUBSCRIPTION " +
            " WHERE " +
            "   APPLICATION_ID=? " +
            "   AND SUB_STATUS=?";

    public static final String GET_SUBSCRIPTIONS_BY_API_SQL =
            "SELECT" +
                    "   SUBSCRIPTION_ID" +
                    " FROM " +
                    "   AM_SUBSCRIPTION SUBS," +
                    "   AM_API API " +
                    " WHERE " +
                    "   API.API_NAME = ? " +
                    "   AND API.API_VERSION = ? " +
                    "   AND API.API_PROVIDER = ? " +
                    "   AND API.API_ID = SUBS.API_ID " +
                    "   AND SUB_STATUS = ?";

    public static final String GET_REGISTRATION_WORKFLOW_SQL =
            "SELECT WF_REF FROM AM_APPLICATION_REGISTRATION WHERE APP_ID = ? AND TOKEN_TYPE = ? AND KEY_MANAGER = ?";

    public static final String GET_SUBSCRIPTION_STATUS_SQL =
            "SELECT SUB_STATUS FROM AM_SUBSCRIPTION WHERE API_ID = ? AND APPLICATION_ID = ?";

    public static final String GET_SUBSCRIPTION_CREATION_STATUS_SQL =
            "SELECT SUBS_CREATE_STATE FROM AM_SUBSCRIPTION WHERE API_ID = ? AND APPLICATION_ID = ?";

    public static final String ADD_EXTERNAL_API_STORE_SQL =
            " INSERT INTO AM_EXTERNAL_STORES (API_ID,STORE_ID,STORE_DISPLAY_NAME,STORE_ENDPOINT,STORE_TYPE," +
                    "LAST_UPDATED_TIME) VALUES (?,?,?,?,?,?)";

    public static final String REMOVE_EXTERNAL_API_STORE_SQL =
            "DELETE FROM AM_EXTERNAL_STORES WHERE API_ID=? AND STORE_ID=? AND STORE_TYPE=?";

    public static final String UPDATE_EXTERNAL_API_STORE_SQL =
            "UPDATE " +
            "   AM_EXTERNAL_STORES" +
            " SET " +
            "   STORE_ENDPOINT = ?, " +
            "   STORE_TYPE = ?, " +
            "   LAST_UPDATED_TIME = ? " +
            " WHERE " +
            "   API_ID = ? AND STORE_ID = ? ";

    public static final String GET_EXTERNAL_API_STORE_DETAILS_SQL =
            "SELECT " +
            "   ES.STORE_ID, " +
            "   ES.STORE_DISPLAY_NAME, " +
            "   ES.STORE_ENDPOINT, " +
            "   ES.STORE_TYPE, " +
            "   ES.LAST_UPDATED_TIME " +
             "FROM " +
            "   AM_EXTERNAL_STORES ES " +
            " WHERE " +
            "   ES.API_ID = ? ";

    public static final String ADD_PRODUCT_RESOURCE_MAPPING_SQL = "INSERT INTO AM_API_PRODUCT_MAPPING "
            + "(API_ID,URL_MAPPING_ID) " + "VALUES (?, ?)";

    public static final String DELETE_FROM_AM_API_PRODUCT_MAPPING_SQL = "DELETE FROM AM_API_PRODUCT_MAPPING WHERE "
            + "API_ID = ? ";

    public static final String GET_SCOPE_BY_SUBSCRIBED_API_PREFIX =
            "SELECT DISTINCT ARSM.SCOPE_NAME " +
                    "FROM AM_API_RESOURCE_SCOPE_MAPPING ARSM INNER JOIN AM_API_URL_MAPPING AUM " +
                    "ON ARSM.URL_MAPPING_ID = AUM.URL_MAPPING_ID " +
                    "WHERE AUM.API_ID IN (";

    public static final char GET_SCOPE_BY_SUBSCRIBED_ID_SUFFIX = ')';

    public static final String GET_SCOPE_BY_SUBSCRIBED_ID_ORACLE_SQL =
            "SELECT DISTINCT ARSM.SCOPE_NAME " +
                    "FROM AM_API_RESOURCE_SCOPE_MAPPING ARSM INNER JOIN AM_API_URL_MAPPING AUM " +
                    "ON ARSM.URL_MAPPING_ID = AUM.URL_MAPPING_ID " +
                    "WHERE AUM.API_ID IN (";

    public static final String GET_SCOPES_BY_SCOPE_KEY_SQL =
            "SELECT " +
            "   IAS.SCOPE_ID, " +
            "   IAS.NAME, " +
            "   IAS.DISPLAY_NAME, " +
            "   IAS.DESCRIPTION, " +
            "   IAS.TENANT_ID, " +
            "   B.SCOPE_BINDING " +
            " FROM " +
            "   IDN_OAUTH2_SCOPE IAS " +
            " INNER JOIN  IDN_OAUTH2_SCOPE_BINDING B ON IAS.SCOPE_ID = B.SCOPE_ID  " +
            " WHERE" +
            "   NAME = ? AND TENANT_ID = ?";

    public static final String GET_SCOPES_BY_SCOPE_KEYS_PREFIX =
            "SELECT " +
            "   IAS.SCOPE_ID, " +
            "   IAS.NAME, " +
            "   IAS.DISPLAY_NAME, " +
            "   IAS.DESCRIPTION, " +
            "   IAS.TENANT_ID, " +
            "   B.SCOPE_BINDING " +
            " FROM " +
            "   IDN_OAUTH2_SCOPE IAS " +
            " INNER JOIN  IDN_OAUTH2_SCOPE_BINDING B ON IAS.SCOPE_ID = B.SCOPE_ID  " +
            " WHERE" +
            "   NAME IN (";

    public static final String GET_SCOPES_BY_SCOPE_KEYS_PREFIX_ORACLE =
            "SELECT " +
            "   IAS.SCOPE_ID, " +
            "   IAS.NAME, " +
            "   IAS.DISPLAY_NAME, " +
            "   IAS.DESCRIPTION, " +
            "   IAS.TENANT_ID, " +
            "   B.SCOPE_BINDING " +
            " FROM " +
            "   IDN_OAUTH2_SCOPE IAS " +
            " INNER JOIN  IDN_OAUTH2_SCOPE_BINDING B ON IAS.SCOPE_ID = B.SCOPE_ID  " +
            " WHERE" +
            "   NAME IN (";

    public static final String GET_SCOPES_BY_SCOPE_KEYS_SUFFIX = ") AND TENANT_ID = ?";

    public static final String GET_RESOURCE_TO_SCOPE_MAPPING_SQL =
            "SELECT AUM.URL_MAPPING_ID, ARSM.SCOPE_NAME FROM AM_API_URL_MAPPING AUM " +
                    "LEFT JOIN AM_API_RESOURCE_SCOPE_MAPPING ARSM ON AUM.URL_MAPPING_ID = ARSM.URL_MAPPING_ID " +
                    "WHERE AUM.API_ID = ?";

    public static final String GET_SUBSCRIBED_APIS_FROM_CONSUMER_KEY =
        "SELECT SUB.API_ID "
                + "FROM AM_SUBSCRIPTION SUB, AM_APPLICATION_KEY_MAPPING AKM "
                + "WHERE AKM.CONSUMER_KEY = ? AND AKM.APPLICATION_ID = SUB.APPLICATION_ID";

    public static final String GET_SCOPE_ROLES_OF_APPLICATION_SQL =
            "SELECT DISTINCT A.NAME, D.SCOPE_BINDING "
                    + "FROM ("
                    + " (IDN_OAUTH2_SCOPE A "
                    + "     INNER JOIN "
                    + "  AM_API_RESOURCE_SCOPE_MAPPING B1 ON A.TENANT_ID = B1.TENANT_ID "
                    + "     INNER JOIN "
                    + "  AM_API_RESOURCE_SCOPE_MAPPING B2 ON A.NAME = B2.SCOPE_NAME "
                    + "     INNER JOIN "
                    + "  AM_API_URL_MAPPING C ON B1.URL_MAPPING_ID = C.URL_MAPPING_ID"
                    + " ) LEFT JOIN "
                    + " IDN_OAUTH2_SCOPE_BINDING D ON A.SCOPE_ID = D.SCOPE_ID"
                    + ") WHERE C.API_ID IN (";

    public static final String CLOSING_BRACE = ")";

    public static final String GET_SCOPES_FOR_API_LIST = "SELECT "
            + "ARSM.SCOPE_NAME, AUM.API_ID "
            + "FROM AM_API_RESOURCE_SCOPE_MAPPING ARSM "
            + "INNER JOIN AM_API_URL_MAPPING AUM "
            + "ON ARSM.URL_MAPPING_ID = AUM.URL_MAPPING_ID "
            + "WHERE AUM.API_ID IN ( $paramList )";

    public static final String GET_SCOPES_FOR_API_LIST_ORACLE = "SELECT "
            + "ARSM.SCOPE_NAME, AUM.API_ID "
            + "FROM AM_API_RESOURCE_SCOPE_MAPPING ARSM "
            + "INNER JOIN AM_API_URL_MAPPING AUM "
            + "ON ARSM.URL_MAPPING_ID = AUM.URL_MAPPING_ID "
            + "WHERE AUM.API_ID IN ( $paramList )";

    public static final String GET_USERS_FROM_OAUTH_TOKEN_SQL =
            "SELECT " +
            "   DISTINCT AMS.USER_ID, " +
            "   AKM.CONSUMER_KEY " +
            " FROM " +
            "   AM_APPLICATION_KEY_MAPPING AKM, " +
            "   AM_APPLICATION, " +
            "   AM_SUBSCRIBER AMS " +
            " WHERE " +
            "   AKM.CONSUMER_KEY = ? " +
            "   AND AKM.APPLICATION_ID = AA.APPLICATION_ID " +
            "   AND AA.SUBSCRIBER_ID = AMS.SUBSCRIBER_ID";

    public static final String REMOVE_SUBSCRIPTION_BY_APPLICATION_ID_SQL =
            "DELETE FROM AM_SUBSCRIPTION WHERE API_ID = ? AND APPLICATION_ID = ? ";

    public static final String GET_API_NAME_NOT_MATCHING_CONTEXT_SQL =
            "SELECT COUNT(API_ID) AS API_COUNT FROM AM_API WHERE LOWER(API_NAME) = LOWER(?) AND CONTEXT NOT LIKE ?";

    public static final String GET_API_NAME_MATCHING_CONTEXT_SQL =
            "SELECT COUNT(API_ID) AS API_COUNT FROM AM_API WHERE LOWER(API_NAME) = LOWER(?) AND CONTEXT LIKE ?";

    public static final String GET_API_NAME_DIFF_CASE_NOT_MATCHING_CONTEXT_SQL =
            "SELECT COUNT(API_ID) AS API_COUNT FROM AM_API WHERE LOWER(API_NAME) = LOWER(?) AND CONTEXT NOT LIKE ? AND NOT (API_NAME = ?)";

    public static final String GET_API_NAME_DIFF_CASE_MATCHING_CONTEXT_SQL =
            "SELECT COUNT(API_ID) AS API_COUNT FROM AM_API WHERE LOWER(API_NAME) = LOWER(?) AND CONTEXT LIKE ? AND NOT (API_NAME = ?)";

    public static final String GET_ACTIVE_TOKEN_OF_CONSUMER_KEY_SQL =
            " SELECT " +
            "   IOAT.ACCESS_TOKEN" +
            " FROM " +
            "   IDN_OAUTH2_ACCESS_TOKEN IOAT" +
            " INNER JOIN " +
            "   IDN_OAUTH_CONSUMER_APPS IOCA " +
            " ON " +
            "   IOCA.ID = IOAT.CONSUMER_KEY_ID" +
            " WHERE" +
            "   IOCA.CONSUMER_KEY = ?" +
            "   AND IOAT.TOKEN_STATE = 'ACTIVE'";

    public static final String GET_CONTEXT_TEMPLATE_COUNT_SQL =
            "SELECT COUNT(CONTEXT_TEMPLATE) AS CTX_COUNT FROM AM_API WHERE LOWER(CONTEXT_TEMPLATE) = ?";

    public static final String GET_API_NAMES_MATCHES_CONTEXT=
            "SELECT DISTINCT API_NAME FROM AM_API WHERE CONTEXT_TEMPLATE = ?";

    public static final String GET_VERSIONS_MATCHES_CONTEXT=
            "SELECT API_VERSION FROM AM_API WHERE CONTEXT_TEMPLATE = ? AND API_NAME = ?";

    public static final String GET_APPLICATION_MAPPING_FOR_CONSUMER_KEY_SQL =
            "SELECT APPLICATION_ID FROM AM_APPLICATION_KEY_MAPPING WHERE CONSUMER_KEY = ? AND KEY_MANAGER = ?";

    public static final String GET_CONSUMER_KEY_BY_APPLICATION_AND_KEY_SQL =
            " SELECT " +
            "   CONSUMER_KEY,KEY_MANAGER " +
            " FROM " +
            "   AM_APPLICATION_KEY_MAPPING " +
            " WHERE " +
            "   APPLICATION_ID = ? " +
            "   AND KEY_TYPE = ? ";

    public static final String GET_LAST_PUBLISHED_API_VERSION_SQL =
            "SELECT " +
            "   API.API_VERSION " +
            " FROM " +
            "   AM_API API , " +
            "   AM_EXTERNAL_STORES ES " +
            " WHERE " +
            "   ES.API_ID = API.API_ID " +
            "   AND API.API_PROVIDER = ? " +
            "   AND API.API_NAME=? " +
            "   AND ES.STORE_ID =? " +
            " ORDER By API.CREATED_TIME ASC";

    public static final String GET_ACTIVE_TOKENS_OF_USER_PREFIX =
            "SELECT IOAT.ACCESS_TOKEN FROM ";

    public static final String GET_ACTIVE_TOKENS_OF_USER_SUFFIX =
            "   IOAT" +
            " WHERE" +
            "   IOAT.AUTHZ_USER = ?" +
            "   AND IOAT.TENANT_ID = ?" +
            "   AND IOAT.TOKEN_STATE = 'ACTIVE'" +
            "   AND LOWER(IOAT.USER_DOMAIN) = ?";

    public static final String GET_ALL_ALERT_TYPES =
            "SELECT " +
            "   AT.ALERT_TYPE_ID, " +
            "   AT.ALERT_TYPE_NAME " +
            " FROM " +
            "   AM_ALERT_TYPES AT  " +
            " WHERE " +
            "   STAKE_HOLDER   = ?";


    public static final String GET_ALL_ALERT_TYPES_FOR_ADMIN =
            "SELECT DISTINCT" +
            "   AT.ALERT_TYPE_ID, " +
            "   AT.ALERT_TYPE_NAME " +
            " FROM " +
            "   AM_ALERT_TYPES AT  ";

    public static final String GET_SAVED_ALERT_TYPES_BY_USERNAME =
            " SELECT " +
            "   ALERT_TYPE_ID " +
            " FROM " +
            "   AM_ALERT_TYPES_VALUES " +
            " WHERE " +
            "   USER_NAME = ? " +
            "   AND STAKE_HOLDER   = ? ";

    public static final String GET_SAVED_ALERT_EMAILS =

            " SELECT " +
            "   EMAIL " +
            " FROM " +
            "   AM_ALERT_EMAILLIST , " +
            "   AM_ALERT_EMAILLIST_DETAILS  " +
            " WHERE " +
            "   AM_ALERT_EMAILLIST.EMAIL_LIST_ID = AM_ALERT_EMAILLIST_DETAILS.EMAIL_LIST_ID" +
            "   AND USER_NAME = ? " +
            "   AND STAKE_HOLDER  = ? ";


    public static final String ADD_ALERT_TYPES_VALUES =
            " INSERT INTO AM_ALERT_TYPES_VALUES (ALERT_TYPE_ID, USER_NAME , STAKE_HOLDER) " +
            " VALUES(?,?,?)";

    public static final String ADD_ALERT_EMAIL_LIST =
            " INSERT INTO AM_ALERT_EMAILLIST  (USER_NAME, STAKE_HOLDER) " +
            " VALUES(?,?)";

    public static final String DELETE_ALERTTYPES_BY_USERNAME_AND_STAKE_HOLDER  =
            "DELETE FROM AM_ALERT_TYPES_VALUES WHERE USER_NAME = ? AND STAKE_HOLDER = ?";

    public static final String DELETE_EMAILLIST_BY_EMAIL_LIST_ID  =
            "DELETE FROM AM_ALERT_EMAILLIST_DETAILS   WHERE EMAIL_LIST_ID= ? ";

    public static final String GET_EMAILLISTID_BY_USERNAME_AND_STAKEHOLDER =
            " SELECT " +
            "   EMAIL_LIST_ID " +
            " FROM " +
            "   AM_ALERT_EMAILLIST " +
            " WHERE " +
            "   USER_NAME = ? " +
            "   AND STAKE_HOLDER  = ? ";

    public static final String SAVE_EMAIL_LIST_DETAILS_QUERY =
            " INSERT INTO AM_ALERT_EMAILLIST_DETAILS  (EMAIL_LIST_ID, EMAIL) " +
            " VALUES(?,?)";

    public static final String DELETE_ALERTTYPES_EMAILLISTS_BY_USERNAME_AND_STAKE_HOLDER  =
            "DELETE FROM AM_ALERT_EMAILLIST  WHERE USER_NAME = ? AND STAKE_HOLDER = ?";



    public static final String INSERT_APPLICATION_POLICY_SQL =
            "INSERT INTO AM_POLICY_APPLICATION (NAME, DISPLAY_NAME, TENANT_ID, DESCRIPTION, QUOTA_TYPE, QUOTA, \n" +
                    " QUOTA_UNIT, UNIT_TIME, TIME_UNIT, IS_DEPLOYED, UUID) \n" +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?)";

    public static final String INSERT_APPLICATION_POLICY_WITH_CUSTOM_ATTRIB_SQL =
            "INSERT INTO AM_POLICY_APPLICATION (NAME, DISPLAY_NAME, TENANT_ID, DESCRIPTION, QUOTA_TYPE, QUOTA, \n" +
                    " QUOTA_UNIT, UNIT_TIME, TIME_UNIT, IS_DEPLOYED, UUID,CUSTOM_ATTRIBUTES) \n" +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";

    public static final String INSERT_SUBSCRIPTION_POLICY_SQL =
            "INSERT INTO AM_POLICY_SUBSCRIPTION (NAME, DISPLAY_NAME, TENANT_ID, DESCRIPTION, QUOTA_TYPE, QUOTA, \n" +
                    " QUOTA_UNIT, UNIT_TIME, TIME_UNIT, IS_DEPLOYED, UUID, RATE_LIMIT_COUNT, \n" +
                    " RATE_LIMIT_TIME_UNIT,STOP_ON_QUOTA_REACH, MAX_DEPTH, MAX_COMPLEXITY, \n" +
                    " BILLING_PLAN,MONETIZATION_PLAN,FIXED_RATE,BILLING_CYCLE,PRICE_PER_REQUEST,CURRENCY) \n" +
                    " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    public static final String INSERT_SUBSCRIPTION_POLICY_WITH_CUSTOM_ATTRIB_SQL =
            "INSERT INTO AM_POLICY_SUBSCRIPTION (NAME, DISPLAY_NAME, TENANT_ID, DESCRIPTION, QUOTA_TYPE, QUOTA, \n" +
                    " QUOTA_UNIT, UNIT_TIME, TIME_UNIT, IS_DEPLOYED, UUID,  RATE_LIMIT_COUNT, \n" +
                    " RATE_LIMIT_TIME_UNIT, STOP_ON_QUOTA_REACH, MAX_DEPTH, MAX_COMPLEXITY, \n" +
                    " BILLING_PLAN, CUSTOM_ATTRIBUTES, MONETIZATION_PLAN, \n" +
                    " FIXED_RATE, BILLING_CYCLE, PRICE_PER_REQUEST, CURRENCY) \n" +
                    " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";


    public static final String INSERT_GLOBAL_POLICY_SQL =
            "INSERT INTO AM_POLICY_GLOBAL (NAME ,TENANT_ID, KEY_TEMPLATE, DESCRIPTION ,SIDDHI_QUERY, "
                    + "IS_DEPLOYED, UUID) \n" +
            "VALUES (?,?,?,?,?,?,?)";

    public static final String GET_APP_POLICY_NAMES =
            " SELECT " +
                    "   NAME " +
                    "FROM " +
                    "   AM_POLICY_APPLICATION " +
                    " WHERE" +
                    "   TENANT_ID =?";

    public static final String GET_SUB_POLICY_NAMES =
            " SELECT " +
                    "   NAME " +
                    "FROM " +
                    "   AM_POLICY_SUBSCRIPTION " +
                    " WHERE" +
                    "   TENANT_ID =?";

    public static final String GET_GLOBAL_POLICY_NAMES =
            " SELECT " +
                    "   NAME " +
                    "FROM " +
                    "   AM_POLICY_GLOBAL " +
                    " WHERE" +
                    "   TENANT_ID =?";

    public static final String GET_GLOBAL_POLICY_KEY_TEMPLATES =
            " SELECT " +
                    "   KEY_TEMPLATE " +
                    "FROM " +
                    "   AM_POLICY_GLOBAL " +
                    " WHERE" +
                    "   TENANT_ID =?";

    public static final String GET_GLOBAL_POLICY_KEY_TEMPLATE =
            " SELECT " +
                    "   KEY_TEMPLATE " +
                    "FROM " +
                    "   AM_POLICY_GLOBAL " +
                    " WHERE" +
                    "   TENANT_ID =? AND" +
                    "   KEY_TEMPLATE =? AND" +
                    "   NAME =?";

    public static final String GET_APP_POLICIES =
            " SELECT "+
                    "   * " +
                    "FROM " +
                    "   AM_POLICY_APPLICATION " +
                    " WHERE" +
                    "   TENANT_ID =?";
    public static final String GET_SUBSCRIPTION_POLICIES =
            " SELECT " +
                    "   * " +
                    "FROM " +
                    "   AM_POLICY_SUBSCRIPTION " +
                    " WHERE" +
                    "   TENANT_ID =?";

    public static final String GET_SUBSCRIPTION_POLICIES_BY_POLICY_NAMES_PREFIX =
            " SELECT " +
                    "   * " +
                    "FROM " +
                    "   AM_POLICY_SUBSCRIPTION " +
                    " WHERE" +
                    "  NAME IN (";

    public static final String GET_SUBSCRIPTION_POLICIES_BY_POLICY_NAMES_SUFFIX =
            ") AND TENANT_ID =?";

    public static final String GET_GLOBAL_POLICIES =
            " SELECT " +
                    "   * " +
                    "FROM " +
                    "   AM_POLICY_GLOBAL " +
                    " WHERE" +
                    "   TENANT_ID =?";

    public static final String GET_GLOBAL_POLICY =
            " SELECT " +
            "   * " +
            "FROM " +
            "   AM_POLICY_GLOBAL " +
            " WHERE" +
            "   NAME =?";

    public static final String GET_GLOBAL_POLICY_BY_UUID =
            "SELECT " +
            "   * " +
            "FROM " +
            "   AM_POLICY_GLOBAL " +
            "WHERE" +
            "   UUID =?";

    public static final String GET_APPLICATION_POLICY_SQL =
            "SELECT "+
                    "* " +
            "FROM " +
                    "AM_POLICY_APPLICATION " +
            "WHERE " +
                    "NAME = ? AND " +
                    "TENANT_ID =?";

    public static final String GET_APPLICATION_POLICY_BY_UUID_SQL =
            "SELECT " +
                "* " +
            "FROM " +
                "AM_POLICY_APPLICATION " +
            "WHERE " +
                "UUID = ?";

    public static final String GET_SUBSCRIPTION_POLICY_SQL =
            "SELECT "+
                    "* " +
                    "FROM " +
                    "   AM_POLICY_SUBSCRIPTION " +
            "WHERE " +
                    "NAME = ? AND " +
                    "TENANT_ID =?";

    public static final String GET_API_PROVIDER_WITH_NAME_VERSION_FOR_SUPER_TENANT =
            "SELECT API.API_PROVIDER FROM AM_API API WHERE API.API_NAME = ? AND API.API_VERSION = ? AND "
                    + "CONTEXT NOT LIKE '%" + APIConstants.TENANT_PREFIX + "%' ";

    public static final String GET_API_PROVIDER_WITH_NAME_VERSION_FOR_GIVEN_TENANT =
            "SELECT API.API_PROVIDER FROM AM_API API WHERE API.API_NAME = ? AND "
                    + "API.API_VERSION = ? AND API.CONTEXT LIKE ? ";

    public static final String GET_SUBSCRIPTION_POLICY_BY_UUID_SQL =
            "SELECT "+
                    "* " +
                    "FROM " +
                    "   AM_POLICY_SUBSCRIPTION " +
            "WHERE " +
                    "UUID = ?";

    public static final String UPDATE_APPLICATION_POLICY_SQL =
            "UPDATE AM_POLICY_APPLICATION " +
            "SET " +
                    "DISPLAY_NAME = ?, " +
                    "DESCRIPTION = ?, " +
                    "QUOTA_TYPE = ?, " +
                    "QUOTA = ?, " +
                    "QUOTA_UNIT = ?, " +
                    "UNIT_TIME = ?, " +
                    "TIME_UNIT = ? " +
            "WHERE NAME = ? AND TENANT_ID = ?";

    public static final String UPDATE_APPLICATION_POLICY_WITH_CUSTOM_ATTRIBUTES_SQL =
            "UPDATE AM_POLICY_APPLICATION " +
            "SET " +
                    "DISPLAY_NAME = ?, " +
                    "DESCRIPTION = ?, " +
                    "QUOTA_TYPE = ?, " +
                    "QUOTA = ?, " +
                    "QUOTA_UNIT = ?, " +
                    "UNIT_TIME = ?, " +
                    "TIME_UNIT = ?, " +
                    " CUSTOM_ATTRIBUTES = ? "+
            "WHERE NAME = ? AND TENANT_ID = ?";

    public static final String UPDATE_APPLICATION_POLICY_BY_UUID_SQL =
            "UPDATE AM_POLICY_APPLICATION " +
                    "SET " +
                    "DISPLAY_NAME = ?, " +
                    "DESCRIPTION = ?, " +
                    "QUOTA_TYPE = ?, " +
                    "QUOTA = ?, " +
                    "QUOTA_UNIT = ?, " +
                    "UNIT_TIME = ?, " +
                    "TIME_UNIT = ? " +
                    "WHERE UUID = ?";

    public static final String UPDATE_APPLICATION_POLICY_WITH_CUSTOM_ATTRIBUTES_BY_UUID_SQL =
            "UPDATE AM_POLICY_APPLICATION " +
                    "SET " +
                    "DISPLAY_NAME = ?, " +
                    "DESCRIPTION = ?, " +
                    "QUOTA_TYPE = ?, " +
                    "QUOTA = ?, " +
                    "QUOTA_UNIT = ?, " +
                    "UNIT_TIME = ?, " +
                    "TIME_UNIT = ?, " +
                    "CUSTOM_ATTRIBUTES = ? "+
                    "WHERE UUID = ?";

    public static final String UPDATE_SUBSCRIPTION_POLICY_SQL =
            "UPDATE AM_POLICY_SUBSCRIPTION " +
            "SET " +
                    "DISPLAY_NAME = ?, " +
                    "DESCRIPTION = ?, " +
                    "QUOTA_TYPE = ?, " +
                    "QUOTA = ?, " +
                    "QUOTA_UNIT = ?, " +
                    "UNIT_TIME = ?, " +
                    "TIME_UNIT = ?, " +
                    "RATE_LIMIT_COUNT = ?," +
                    "RATE_LIMIT_TIME_UNIT = ?, " +
                    "STOP_ON_QUOTA_REACH = ?, " +
                    "MAX_DEPTH = ?, " +
                    "MAX_COMPLEXITY = ?, " +
                    "BILLING_PLAN = ?, " +
                    "MONETIZATION_PLAN = ?," +
                    "FIXED_RATE = ?," +
                    "BILLING_CYCLE = ?," +
                    "PRICE_PER_REQUEST = ?, " +
                    "CURRENCY = ? " +
            "WHERE NAME = ? AND TENANT_ID = ?";

    public static final String UPDATE_SUBSCRIPTION_POLICY_WITH_CUSTOM_ATTRIBUTES_SQL =
            "UPDATE AM_POLICY_SUBSCRIPTION " +
            "SET " +
                    "DISPLAY_NAME = ?, " +
                    "DESCRIPTION = ?, " +
                    "QUOTA_TYPE = ?, " +
                    "QUOTA = ?, " +
                    "QUOTA_UNIT = ?, " +
                    "UNIT_TIME = ?, " +
                    "TIME_UNIT = ?, " +
                    "RATE_LIMIT_COUNT = ?," +
                    "RATE_LIMIT_TIME_UNIT = ?, " +
                    "STOP_ON_QUOTA_REACH = ?, " +
                    "MAX_DEPTH = ?, " +
                    "MAX_COMPLEXITY = ?, " +
                    "BILLING_PLAN = ?, "+
                    "CUSTOM_ATTRIBUTES = ?, "+
                    "MONETIZATION_PLAN = ?," +
                    "FIXED_RATE = ?," +
                    "BILLING_CYCLE = ?," +
                    "PRICE_PER_REQUEST = ?, " +
                    "CURRENCY = ? " +
            "WHERE NAME = ? AND TENANT_ID = ?";

    public static final String UPDATE_SUBSCRIPTION_POLICY_BY_UUID_SQL =
            "UPDATE AM_POLICY_SUBSCRIPTION " +
                    "SET " +
                    "DISPLAY_NAME = ?, " +
                    "DESCRIPTION = ?, " +
                    "QUOTA_TYPE = ?, " +
                    "QUOTA = ?, " +
                    "QUOTA_UNIT = ?, " +
                    "UNIT_TIME = ?, " +
                    "TIME_UNIT = ?, " +
                    "RATE_LIMIT_COUNT = ?," +
                    "RATE_LIMIT_TIME_UNIT = ?, " +
                    "STOP_ON_QUOTA_REACH = ?, " +
                    "MAX_DEPTH = ?, " +
                    "MAX_COMPLEXITY = ?, " +
                    "BILLING_PLAN = ?, "+
                    "MONETIZATION_PLAN = ?," +
                    "FIXED_RATE = ?," +
                    "BILLING_CYCLE = ?," +
                    "PRICE_PER_REQUEST = ?, " +
                    "CURRENCY = ? " +
                    "WHERE UUID = ?";

    public static final String UPDATE_SUBSCRIPTION_POLICY_WITH_CUSTOM_ATTRIBUTES_BY_UUID_SQL =
            "UPDATE AM_POLICY_SUBSCRIPTION " +
                    "SET " +
                    "DISPLAY_NAME = ?, " +
                    "DESCRIPTION = ?, " +
                    "QUOTA_TYPE = ?, " +
                    "QUOTA = ?, " +
                    "QUOTA_UNIT = ?, " +
                    "UNIT_TIME = ?, " +
                    "TIME_UNIT = ?, " +
                    "RATE_LIMIT_COUNT = ?," +
                    "RATE_LIMIT_TIME_UNIT = ?, " +
                    "STOP_ON_QUOTA_REACH = ?, " +
                    "MAX_DEPTH = ?, " +
                    "MAX_COMPLEXITY = ?, " +
                    "BILLING_PLAN = ?, "+
                    "CUSTOM_ATTRIBUTES = ?, "+
                    "MONETIZATION_PLAN = ?," +
                    "FIXED_RATE = ?," +
                    "BILLING_CYCLE = ?," +
                    "PRICE_PER_REQUEST = ?, " +
                    "CURRENCY = ? " +
                    "WHERE UUID = ?";

    public static final String UPDATE_GLOBAL_POLICY_SQL =
            "UPDATE AM_POLICY_GLOBAL " +
            "SET " +
                    "DESCRIPTION = ?, " +
                    "SIDDHI_QUERY = ?, " +
                    "KEY_TEMPLATE = ? " +
            "WHERE NAME = ? AND TENANT_ID = ?";

    public static final String UPDATE_GLOBAL_POLICY_BY_UUID_SQL =
            "UPDATE AM_POLICY_GLOBAL " +
                    "SET " +
                    "DESCRIPTION = ?, " +
                    "SIDDHI_QUERY = ?, " +
                    "KEY_TEMPLATE = ? " +
                    "WHERE UUID = ?";

    public static final String UPDATE_APPLICATION_POLICY_STATUS_SQL =
            "UPDATE AM_POLICY_APPLICATION SET IS_DEPLOYED = ? WHERE NAME = ? AND TENANT_ID = ?";

    public static final String UPDATE_SUBSCRIPTION_POLICY_STATUS_SQL =
            "UPDATE AM_POLICY_SUBSCRIPTION SET IS_DEPLOYED = ? WHERE NAME = ? AND TENANT_ID = ?";

    public static final String UPDATE_GLOBAL_POLICY_STATUS_SQL =
            "UPDATE AM_POLICY_GLOBAL SET IS_DEPLOYED = ? WHERE NAME = ? AND TENANT_ID = ?";

    public static final String DELETE_APPLICATION_POLICY_SQL =
            "DELETE FROM AM_POLICY_APPLICATION WHERE TENANT_ID = ? AND NAME = ?";

    public static final String DELETE_SUBSCRIPTION_POLICY_SQL =
            "DELETE FROM AM_POLICY_SUBSCRIPTION WHERE TENANT_ID = ? AND NAME = ?";

    public static final String DELETE_GLOBAL_POLICY_SQL =
            "DELETE FROM AM_POLICY_GLOBAL WHERE TENANT_ID = ? AND NAME = ?";

    public static final String GET_API_DETAILS_SQL = "SELECT * FROM AM_API ";

    public static final String GET_ACCESS_TOKENS_BY_USER_SQL = "SELECT AKM.CONSUMER_KEY, CON_APP.CONSUMER_SECRET, TOKEN.ACCESS_TOKEN " +
            "FROM " +
            "IDN_OAUTH_CONSUMER_APPS CON_APP, AM_APPLICATION APP, IDN_OAUTH2_ACCESS_TOKEN  TOKEN, AM_APPLICATION_KEY_MAPPING AKM " +
            "WHERE TOKEN.AUTHZ_USER =? " +
            "AND APP.NAME =? " +
            "AND APP.CREATED_BY =? " +
            "AND TOKEN.TOKEN_STATE = 'ACTIVE' " +
            "AND TOKEN.CONSUMER_KEY_ID = CON_APP.ID " +
            "AND CON_APP.CONSUMER_KEY=AKM.CONSUMER_KEY " +
            "AND AKM.APPLICATION_ID = APP.APPLICATION_ID";


    public static final String REMOVE_GROUP_ID_MAPPING_SQL =
            "DELETE FROM AM_APPLICATION_GROUP_MAPPING WHERE APPLICATION_ID = ? ";

    public static final String ADD_GROUP_ID_MAPPING_SQL =
            "INSERT INTO AM_APPLICATION_GROUP_MAPPING (APPLICATION_ID, GROUP_ID, TENANT) VALUES (?,?,?)";

    public static final String GET_GROUP_ID_SQL =
            "SELECT GROUP_ID  FROM AM_APPLICATION_GROUP_MAPPING WHERE APPLICATION_ID = ?";

    public static final String REMOVE_MIGRATED_GROUP_ID_SQL =
            "UPDATE AM_APPLICATION SET GROUP_ID = '' WHERE APPLICATION_ID = ?";


    /** Label related constants **/

    public static final String GET_LABEL_BY_TENANT = "select * from AM_LABELS where AM_LABELS.TENANT_DOMAIN= ? ";

    public static final String GET_URL_BY_LABEL_ID = "Select * from  AM_LABEL_URLS where LABEL_ID= ? ";

    public static final String ADD_LABEL_SQL = "INSERT INTO AM_LABELS VALUES (?,?,?,?)";

    public static final String ADD_LABEL_URL_MAPPING_SQL = "INSERT INTO AM_LABEL_URLS  VALUES (?,?)";

    public static final String DELETE_LABEL_URL_MAPPING_SQL = "DELETE FROM AM_LABEL_URLS WHERE LABEL_ID = ?";

    public static final String DELETE_LABEL_SQL = "DELETE FROM AM_LABELS WHERE LABEL_ID = ?";

    public static final String UPDATE_LABEL_SQL = "UPDATE AM_LABELS SET NAME = ?, DESCRIPTION = ?  WHERE LABEL_ID = ?";

    public static final String DELETE_API_PRODUCT_SQL =
            "DELETE FROM AM_API WHERE API_PROVIDER = ? AND API_NAME = ? AND API_VERSION = ? AND API_TYPE = '"
                    + APIConstants.API_PRODUCT + "'";

    public static final String UPDATE_PRODUCT_SQL =
            " UPDATE AM_API " +
            " SET" +
            "   API_TIER=?," +
            "   UPDATED_BY=?," +
            "   UPDATED_TIME=?" +
            " WHERE" +
            "   API_NAME=? AND API_PROVIDER=? AND API_VERSION=? AND API_TYPE='" + APIConstants.API_PRODUCT +"'";

    public static final String GET_PRODUCT_ID =
            "SELECT API_ID FROM AM_API WHERE API_NAME = ? AND API_PROVIDER = ? AND "
            + "API_VERSION = ? AND API_TYPE='" + APIConstants.API_PRODUCT +"'";

    public static final String GET_URL_TEMPLATES_FOR_API =
            "SELECT URL_PATTERN , URL_MAPPING_ID, HTTP_METHOD FROM AM_API API , AM_API_URL_MAPPING URL "
            + "WHERE API.API_ID = URL.API_ID AND API.API_NAME =? "
            + "AND API.API_VERSION=? AND API.API_PROVIDER=?";

    public static final String ADD_API_PRODUCT =
            "INSERT INTO "
            + "AM_API(API_PROVIDER, API_NAME, API_VERSION, CONTEXT,"
            + "API_TIER, CREATED_BY, CREATED_TIME, API_TYPE) VALUES (?,?,?,?,?,?,?,?)";

    public static final String GET_RESOURCES_OF_PRODUCT =
            "SELECT API_UM.URL_MAPPING_ID, API_UM.URL_PATTERN, API_UM.HTTP_METHOD, API_UM.AUTH_SCHEME, " +
                "API_UM.THROTTLING_TIER, API.API_PROVIDER, API.API_NAME, API.API_VERSION, API.CONTEXT " +
            "FROM AM_API_URL_MAPPING API_UM " +
            "INNER JOIN AM_API API " +
                "ON API.API_ID = API_UM.API_ID " +
            "INNER JOIN AM_API_PRODUCT_MAPPING PROD_MAP " +
                "ON PROD_MAP.URL_MAPPING_ID = API_UM.URL_MAPPING_ID " +
            "WHERE PROD_MAP.API_ID = ?";

    public static final String GET_SCOPE_KEYS_BY_URL_MAPPING_ID =
            "SELECT SCOPE_NAME FROM AM_API_RESOURCE_SCOPE_MAPPING WHERE URL_MAPPING_ID = ?" ;

    /** API Categories related constants **/

    public static final String ADD_CATEGORY_SQL = "INSERT INTO AM_API_CATEGORIES "
            + "(UUID, NAME, DESCRIPTION, TENANT_ID) VALUES (?,?,?,?)";

    public static final String GET_CATEGORIES_BY_TENANT_ID_SQL = "SELECT * FROM AM_API_CATEGORIES WHERE TENANT_ID = ?";

    public static final String IS_API_CATEGORY_NAME_EXISTS = "SELECT COUNT(UUID) AS API_CATEGORY_COUNT FROM "
            + "AM_API_CATEGORIES WHERE LOWER(NAME) = LOWER(?) AND TENANT_ID = ?";

    public static final String IS_API_CATEGORY_NAME_EXISTS_FOR_ANOTHER_UUID = "SELECT COUNT(UUID) AS API_CATEGORY_COUNT FROM "
            + "AM_API_CATEGORIES WHERE LOWER(NAME) = LOWER(?) AND TENANT_ID = ? AND UUID != ?";

    public static final String GET_API_CATEGORY_BY_ID = "SELECT * FROM AM_API_CATEGORIES WHERE UUID = ?";

    public static final String GET_API_CATEGORY_BY_NAME = "SELECT * FROM AM_API_CATEGORIES WHERE NAME = ? AND TENANT_ID = ?";

    public static final String UPDATE_API_CATEGORY = "UPDATE AM_API_CATEGORIES SET DESCRIPTION = ?, NAME = ? WHERE UUID = ?";

    public static final String DELETE_API_CATEGORY = "DELETE FROM AM_API_CATEGORIES WHERE UUID = ?";

    public static final String GET_USER_ID = "SELECT USER_ID FROM AM_USER WHERE USER_NAME=?";

    public static final String ADD_USER_ID = "INSERT INTO AM_USER (USER_ID, USER_NAME) VALUES (?,?)";
    public static final String GET_KEY_MAPPING_ID_FROM_APPLICATION =
            "SELECT UUID FROM AM_APPLICATION_KEY_MAPPING WHERE APPLICATION_ID = ? AND KEY_TYPE = ? AND KEY_MANAGER = ?";
    public static final String GET_CONSUMER_KEY_FOR_APPLICATION_KEY_TYPE_APP_ID_KEY_MANAGER_SQL =
            "SELECT CONSUMER_KEY FROM AM_APPLICATION_KEY_MAPPING WHERE APPLICATION_ID = ? AND KEY_TYPE = ? AND " +
                    "KEY_MANAGER = ?";
    public static final String GET_KEY_MAPPING_INFO_FROM_APP_ID = "SELECT UUID,CONSUMER_KEY,KEY_MANAGER,KEY_TYPE," +
            "STATE,APP_INFO,CREATE_MODE FROM AM_APPLICATION_KEY_MAPPING WHERE APPLICATION_ID = ?";
    public static final String GET_KEY_MAPPING_INFO_FROM_APP_ID_KEY_MANAGER_KEY_TYPE = "SELECT DISTINCT UUID," +
            "CONSUMER_KEY,KEY_MANAGER, KEY_TYPE, STATE,APP_INFO,CREATE_MODE FROM AM_APPLICATION_KEY_MAPPING WHERE " +
            "APPLICATION_ID = ? AND KEY_TYPE = ? AND KEY_MANAGER IN (?,?)";

    public static final String IS_KEY_MAPPING_EXISTS_FOR_APP_ID_KEY_TYPE_OR_CONSUMER_KEY =
            "SELECT 1 FROM AM_APPLICATION_KEY_MAPPING WHERE " +
                    "((APPLICATION_ID = ? AND KEY_TYPE = ?) OR (CONSUMER_KEY = ?)) AND KEY_MANAGER IN (?,?)";

    public static final String IS_KEY_MAPPING_EXISTS_FOR_APP_ID_KEY_TYPE =
            "SELECT 1 FROM AM_APPLICATION_KEY_MAPPING WHERE APPLICATION_ID = ? AND KEY_TYPE = ? " +
                    "AND KEY_MANAGER IN (?,?)";

    public static final String ADD_GW_PUBLISHED_API_DETAILS = "INSERT INTO AM_GW_PUBLISHED_API_DETAILS (API_ID, " +
            "API_NAME, API_VERSION, TENANT_DOMAIN) VALUES (?,?,?,?)";

    public static final String ADD_GW_API_ARTIFACT = "INSERT INTO AM_GW_API_ARTIFACTS (ARTIFACT, GATEWAY_INSTRUCTION," +
            " TIME_STAMP, API_ID, GATEWAY_LABEL) VALUES (?,?,?,?,?)";


    public static final String UPDATE_API_ARTIFACT = "UPDATE AM_GW_API_ARTIFACTS SET ARTIFACT = ?, " +
            "GATEWAY_INSTRUCTION = ?, TIME_STAMP = ? WHERE (API_ID = ?) AND (GATEWAY_LABEL = ?)";

    public static final String GET_API_ARTIFACT = "SELECT ARTIFACT FROM AM_GW_API_ARTIFACTS WHERE API_ID =? AND " +
            "GATEWAY_LABEL =? AND GATEWAY_INSTRUCTION = ?";

    public static final String GET_API_ARTIFACT_ANY_INSTRUCTION = "SELECT ARTIFACT FROM AM_GW_API_ARTIFACTS WHERE " +
            "API_ID =? AND GATEWAY_LABEL =?";

    public static final String GET_API_ID = "SELECT API_ID  FROM AM_GW_PUBLISHED_API_DETAILS " +
            "WHERE API_NAME =? AND " + "TENANT_DOMAIN =? AND API_VERSION =?";

    public static final String GET_API_LABEL = "SELECT GATEWAY_LABEL  FROM AM_GW_API_ARTIFACTS " +
            "WHERE API_ID =? AND GATEWAY_INSTRUCTION = 'Publish' ";

    public static final String GET_ALL_API_ARTIFACT = "SELECT ARTIFACT FROM AM_GW_API_ARTIFACTS WHERE "
            + "GATEWAY_LABEL =? AND GATEWAY_INSTRUCTION = ?";

    public static final String GET_PUBLISHED_GATEWAYS_FOR_API = "SELECT COUNT(*) AS COUNT FROM AM_GW_API_ARTIFACTS" +
            " WHERE API_ID = ? AND GATEWAY_INSTRUCTION = ?";

    public static final String CHECK_API_EXISTS = "SELECT 1 FROM AM_GW_PUBLISHED_API_DETAILS" +
            " WHERE API_ID = ?";

    public static final String CHECK_ARTIFACT_EXISTS = "SELECT 1 FROM AM_GW_API_ARTIFACTS" +
            " WHERE API_ID = ? AND GATEWAY_LABEL = ?";



    /** Throttle related constants**/

    public static class ThrottleSQLConstants{

    	public static final String QUOTA_TYPE_BANDWIDTH = PolicyConstants.BANDWIDTH_TYPE;

    	public static final String QUOTA_TYPE_REQUESTCOUNT = PolicyConstants.REQUEST_COUNT_TYPE;

		public static final String GET_POLICY_NAMES = " SELECT " + "   NAME " + "FROM " + "   AM_API_THROTTLE_POLICY"
				+ " WHERE" + "   TYPE = ?" + "   AND TENANT_ID =?";

		public static final String GET_EXISTING_POLICY_SQL = "SELECT POLICY_ID FROM AM_API_THROTTLE_POLICY WHERE NAME = ? AND TENANT_ID = ? ";

		public static final String INSERT_API_POLICY_SQL = "INSERT INTO AM_API_THROTTLE_POLICY (NAME, DISPLAY_NAME, TENANT_ID, DESCRIPTION, DEFAULT_QUOTA_TYPE, \n"
				+ "  DEFAULT_QUOTA, DEFAULT_QUOTA_UNIT, DEFAULT_UNIT_TIME, DEFAULT_TIME_UNIT , IS_DEPLOYED, UUID, APPLICABLE_LEVEL) \n"
				+ " VALUES (?,?,?,?,? ,?,?,?,?,? ,?,?)";

		public static final String INSERT_API_POLICY_WITH_ID_SQL = "INSERT INTO AM_API_THROTTLE_POLICY (NAME, DISPLAY_NAME, TENANT_ID, DESCRIPTION, DEFAULT_QUOTA_TYPE, \n"
				+ " DEFAULT_QUOTA, DEFAULT_QUOTA_UNIT, DEFAULT_UNIT_TIME, DEFAULT_TIME_UNIT, \n"
				+ " IS_DEPLOYED, UUID, APPLICABLE_LEVEL, POLICY_ID) \n" + "VALUES (?,?,?,?,?, ?,?,?,?,? ,?,?,?)";


        public static final String UPDATE_API_POLICY_BY_UUID_SQL = "UPDATE AM_API_THROTTLE_POLICY " + "SET "
                + "DISPLAY_NAME = ?,"
                + "DESCRIPTION = ?,"
                + "DEFAULT_QUOTA_TYPE = ?,"
                + "DEFAULT_QUOTA = ?,"
                + "DEFAULT_QUOTA_UNIT = ?,"
                + "DEFAULT_UNIT_TIME = ?,"
                + "DEFAULT_TIME_UNIT = ? "
                + "WHERE UUID = ?";

		public static final String UPDATE_API_POLICY_SQL = "UPDATE AM_API_THROTTLE_POLICY " + "SET "
                + "DISPLAY_NAME = ?,"
                + "DESCRIPTION = ?,"
                + "DEFAULT_QUOTA_TYPE = ?,"
                + "DEFAULT_QUOTA = ?,"
                + "DEFAULT_QUOTA_UNIT = ?,"
                + "DEFAULT_UNIT_TIME = ?,"
                + "DEFAULT_TIME_UNIT = ? "
                + "WHERE NAME = ? AND TENANT_ID = ?";

		public static final String GET_API_POLICY_NAMES = " SELECT " + "   NAME " + "FROM "
				+ "   AM_API_THROTTLE_POLICY " + " WHERE" + "   TENANT_ID =?";

		public static final String GET_API_POLICIES = " SELECT " + "   * " + "FROM " + "   AM_API_THROTTLE_POLICY "
				+ " WHERE" + "   TENANT_ID =? ORDER BY NAME";

		public static final String GET_API_POLICY_ID_SQL = "SELECT " + "POLICY_ID, UUID " + "FROM "
				+ " AM_API_THROTTLE_POLICY " + "WHERE " + "NAME = ? AND " + "TENANT_ID = ?";

        public static final String GET_API_POLICY_ID_BY_UUID_SQL = "SELECT " + "POLICY_ID, UUID " + "FROM "
                + " AM_API_THROTTLE_POLICY " + "WHERE " + "UUID = ?";

		public static final String GET_API_POLICY_SQL = "SELECT " + "* " + "FROM " + "AM_API_THROTTLE_POLICY "
				+ " WHERE " + "NAME = ? AND " + "TENANT_ID =?";

        public static final String GET_API_POLICY_BY_UUID_SQL = "SELECT " + "* " + "FROM " + "AM_API_THROTTLE_POLICY "
                + " WHERE " + "UUID = ?";

		public static final String UPDATE_API_POLICY_STATUS_SQL = "UPDATE AM_API_THROTTLE_POLICY SET IS_DEPLOYED = ? WHERE NAME = ? AND TENANT_ID = ?";

		public static final String DELETE_API_POLICY_SQL = "DELETE FROM AM_API_THROTTLE_POLICY WHERE TENANT_ID = ? AND NAME = ?";



		public static final String INSERT_CONDITION_GROUP_SQL = "INSERT INTO AM_CONDITION_GROUP(POLICY_ID, QUOTA_TYPE,QUOTA,QUOTA_UNIT,UNIT_TIME,TIME_UNIT,DESCRIPTION) \n"
															+ " VALUES (?,?,?,?,?,?,?)";
        public static final String DELETE_CONDITION_GROUP_SQL = "DELETE FROM AM_CONDITION_GROUP WHERE POLICY_ID = ?";

        public static final String UPDATE_CONDITION_GROUP_SQL =  "UPDATE AM_CONDITION_GROUP " + "SET "
                + "QUOTA_TYPE = ?,"
                + "QUOTA = ?,"
                + "QUOTA_UNIT = ?,"
                + "UNIT_TIME = ?,"
                + "TIME_UNIT = ?,"
                + "DESCRIPTION = ? "
                + "WHERE POLICY_ID = ?";


        public static final String GET_PIPELINES_SQL = "SELECT " + "CONDITION_GROUP_ID, " + "QUOTA_TYPE, " + "QUOTA, "
				+ " QUOTA_UNIT, " + "UNIT_TIME, " + "TIME_UNIT, "+ "DESCRIPTION " + "FROM " + "AM_CONDITION_GROUP " + "WHERE " + "POLICY_ID =?";

		public static final String GET_IP_CONDITIONS_SQL = "SELECT " + "STARTING_IP, " + "ENDING_IP, " + "SPECIFIC_IP, "
				+ " WITHIN_IP_RANGE " + "FROM " + "AM_IP_CONDITION " + "WHERE " + "CONDITION_GROUP_ID = ? ";

		public static final String GET_HEADER_CONDITIONS_SQL = "SELECT " + "HEADER_FIELD_NAME, " + "HEADER_FIELD_VALUE , IS_HEADER_FIELD_MAPPING "
				+ " FROM " + "AM_HEADER_FIELD_CONDITION " + "WHERE " + "CONDITION_GROUP_ID =?";

		public static final String GET_JWT_CLAIM_CONDITIONS_SQL = "SELECT " + "CLAIM_URI, " + "CLAIM_ATTRIB , IS_CLAIM_MAPPING " + "FROM "
				+ " AM_JWT_CLAIM_CONDITION " + "WHERE " + "CONDITION_GROUP_ID =?";

		public static final String GET_QUERY_PARAMETER_CONDITIONS_SQL = "SELECT " + "PARAMETER_NAME, "
				+ " PARAMETER_VALUE , IS_PARAM_MAPPING " + "FROM " + "AM_QUERY_PARAMETER_CONDITION " + "WHERE " + "CONDITION_GROUP_ID =?";

		public static final String INSERT_QUERY_PARAMETER_CONDITION_SQL = "INSERT INTO AM_QUERY_PARAMETER_CONDITION(CONDITION_GROUP_ID,PARAMETER_NAME,PARAMETER_VALUE, IS_PARAM_MAPPING) \n"
				+ " VALUES (?,?,?,?)";

		public static final String INSERT_HEADER_FIELD_CONDITION_SQL = "INSERT INTO AM_HEADER_FIELD_CONDITION(CONDITION_GROUP_ID,HEADER_FIELD_NAME,HEADER_FIELD_VALUE, IS_HEADER_FIELD_MAPPING) \n"
				+ " VALUES (?,?,?,?)";

		public static final String INSERT_JWT_CLAIM_CONDITION_SQL = "INSERT INTO AM_JWT_CLAIM_CONDITION(CONDITION_GROUP_ID,CLAIM_URI,CLAIM_ATTRIB,IS_CLAIM_MAPPING) \n"
				+ " VALUES (?,?,?,?)";

		public static final String INSERT_IP_CONDITION_SQL =
	            " INSERT INTO AM_IP_CONDITION(STARTING_IP,ENDING_IP,SPECIFIC_IP,WITHIN_IP_RANGE,CONDITION_GROUP_ID ) \n" +
	            " VALUES (?,?,?,?,?)";

		public static final String IS_ANY_POLICY_CONTENT_AWARE_WITHOUT_API_POLICY_SQL = "SELECT APPPOLICY.TENANT_ID, APPPOLICY.QUOTA_TYPE "
				+ " FROM AM_POLICY_APPLICATION APPPOLICY," + "AM_POLICY_SUBSCRIPTION SUBPOLICY "
				+ " WHERE APPPOLICY.TENANT_ID =? AND " + "APPPOLICY.NAME =? AND " + "SUBPOLICY.NAME=? ";

		public static final String IS_ANY_POLICY_CONTENT_AWARE_SQL = "select sum(c) as c from("
				+ " (SELECT count(*) as c"
				+ " FROM AM_API_THROTTLE_POLICY APIPOLICY where APIPOLICY.NAME =?  AND APIPOLICY.TENANT_ID =? AND APIPOLICY.DEFAULT_QUOTA_TYPE = 'bandwidthVolume')"
				+ " union "
				+ " (SELECT count(*) as c"
				+ " FROM AM_API_THROTTLE_POLICY APIPOLICY , AM_CONDITION_GROUP cg where APIPOLICY.NAME =?  AND APIPOLICY.TENANT_ID =? AND cg.POLICY_ID = APIPOLICY.POLICY_ID AND cg.QUOTA_TYPE = 'bandwidthVolume')"
				+ " union "
				+ " (SELECT count(*) as c"
				+ " FROM AM_API_THROTTLE_POLICY APIPOLICY, AM_API_URL_MAPPING RS, AM_CONDITION_GROUP cg where"
				+ " RS.API_ID = ? AND APIPOLICY.NAME = RS.THROTTLING_TIER AND APIPOLICY.TENANT_ID =? AND cg.POLICY_ID = APIPOLICY.POLICY_ID AND cg.QUOTA_TYPE = 'bandwidthVolume' "
				+ " ) "
				+ " union "
				+ "  (SELECT count(*) as c"
				+ " FROM AM_API_THROTTLE_POLICY APIPOLICY, AM_API_URL_MAPPING RS where "
				+ " RS.API_ID = ? AND APIPOLICY.NAME = RS.THROTTLING_TIER AND APIPOLICY.TENANT_ID =? AND APIPOLICY.DEFAULT_QUOTA_TYPE = 'bandwidthVolume') "
				+ " union "
				+ " (SELECT count(*) as c FROM AM_POLICY_SUBSCRIPTION SUBPOLICY WHERE SUBPOLICY.NAME= ? AND SUBPOLICY.TENANT_ID = ? AND SUBPOLICY.QUOTA_TYPE = 'bandwidthVolume')"
				+ " union "
				+ " (SELECT count(*) as c FROM AM_POLICY_APPLICATION APPPOLICY where APPPOLICY.NAME = ? AND APPPOLICY.TENANT_ID = ? AND APPPOLICY.QUOTA_TYPE = 'bandwidthVolume')"
				+ " ) x";

        public static final String GET_CONDITION_GROUPS_FOR_POLICIES_SQL = "SELECT grp.CONDITION_GROUP_ID ,AUM.HTTP_METHOD,AUM.AUTH_SCHEME, pol.APPLICABLE_LEVEL, "
                + " AUM.URL_PATTERN,AUM.THROTTLING_TIER,AUM.MEDIATION_SCRIPT,AUM.URL_MAPPING_ID, pol.DEFAULT_QUOTA_TYPE  "
                + " FROM AM_API_URL_MAPPING AUM"
                + " INNER JOIN  AM_API API ON AUM.API_ID = API.API_ID"
                + " LEFT OUTER JOIN AM_API_THROTTLE_POLICY pol ON AUM.THROTTLING_TIER = pol.NAME "
                + " LEFT OUTER JOIN AM_CONDITION_GROUP grp ON pol.POLICY_ID  = grp.POLICY_ID"
                + " where API.CONTEXT= ? AND API.API_VERSION = ? AND pol.TENANT_ID = ?"
                /*+ " GROUP BY AUM.HTTP_METHOD,AUM.URL_PATTERN, AUM.URL_MAPPING_ID"*/
                + " ORDER BY AUM.URL_MAPPING_ID";

        public static final String GET_CONDITION_GROUPS_FOR_POLICIES_IN_PRODUCTS_SQL = "SELECT AUM.HTTP_METHOD, AUM.AUTH_SCHEME, AUM.URL_PATTERN, AUM.THROTTLING_TIER, " +
                "AUM.MEDIATION_SCRIPT, AUM.URL_MAPPING_ID, POL.APPLICABLE_LEVEL, GRP.CONDITION_GROUP_ID " +
                "FROM AM_API_URL_MAPPING AUM, AM_API_PRODUCT_MAPPING APM, AM_API API, AM_API_THROTTLE_POLICY POL " +
                "LEFT OUTER JOIN AM_CONDITION_GROUP GRP ON POL.POLICY_ID  = GRP.POLICY_ID " +
                "WHERE APM.API_ID = API.API_ID " +
                "AND API.CONTEXT = ? AND API.API_VERSION = ? AND POL.TENANT_ID = ? " +
                "AND APM.URL_MAPPING_ID = AUM.URL_MAPPING_ID AND AUM.THROTTLING_TIER = POL.NAME " +
                "ORDER BY AUM.URL_MAPPING_ID";

        public static final String ADD_BLOCK_CONDITIONS_SQL =
                "INSERT INTO AM_BLOCK_CONDITIONS (TYPE, VALUE,ENABLED,DOMAIN,UUID) VALUES (?,?,?,?,?)";
        public static final String GET_BLOCK_CONDITIONS_SQL =
                "SELECT CONDITION_ID,TYPE,VALUE,ENABLED,DOMAIN,UUID FROM AM_BLOCK_CONDITIONS WHERE DOMAIN =?";
        public static final String GET_BLOCK_CONDITION_SQL =
                "SELECT TYPE,VALUE,ENABLED,DOMAIN,UUID FROM AM_BLOCK_CONDITIONS WHERE CONDITION_ID =?";
        public static final String GET_BLOCK_CONDITION_BY_UUID_SQL =
                "SELECT CONDITION_ID,TYPE,VALUE,ENABLED,DOMAIN,UUID FROM AM_BLOCK_CONDITIONS WHERE UUID =?";
        public static final String UPDATE_BLOCK_CONDITION_STATE_SQL =
                "UPDATE AM_BLOCK_CONDITIONS SET ENABLED = ? WHERE CONDITION_ID = ?";
        public static final String UPDATE_BLOCK_CONDITION_STATE_BY_UUID_SQL =
                "UPDATE AM_BLOCK_CONDITIONS SET ENABLED = ? WHERE UUID = ?";
        public static final String DELETE_BLOCK_CONDITION_SQL =
                "DELETE FROM AM_BLOCK_CONDITIONS WHERE CONDITION_ID=?";
        public static final String DELETE_BLOCK_CONDITION_BY_UUID_SQL =
                "DELETE FROM AM_BLOCK_CONDITIONS WHERE UUID=?";
        public static final String BLOCK_CONDITION_EXIST_SQL =
                "SELECT CONDITION_ID,TYPE,VALUE,ENABLED,DOMAIN,UUID FROM AM_BLOCK_CONDITIONS WHERE DOMAIN =? AND TYPE =? " +
                        "AND VALUE =?";
        public static final String GET_SUBSCRIPTION_BLOCK_CONDITION_BY_VALUE_AND_DOMAIN_SQL =
                "SELECT CONDITION_ID,TYPE,VALUE,ENABLED,DOMAIN,UUID FROM AM_BLOCK_CONDITIONS WHERE VALUE = ? AND DOMAIN = ? ";

        public static final String TIER_HAS_SUBSCRIPTION = " select count(sub.TIER_ID) as c from AM_SUBSCRIPTION sub, AM_API api "
                + " where sub.TIER_ID = ? and sub.API_ID = api.API_ID ";

        public static final String TIER_ATTACHED_TO_RESOURCES_API = " select sum(c) as c from("
                + " (select count(api.API_TIER) as c from  AM_API api where api.API_TIER = ? )"
                + "		union "
                + " (select count(map.THROTTLING_TIER) as c from AM_API_URL_MAPPING map, AM_API api"
                + "  where map.THROTTLING_TIER = ? and map.API_ID = api.API_ID)) x ";

        public static final String TIER_ATTACHED_TO_APPLICATION = " SELECT count(APPLICATION_TIER) as c FROM AM_APPLICATION where APPLICATION_TIER = ? ";

        public static final String GET_TIERS_WITH_BANDWIDTH_QUOTA_TYPE_SQL = "SELECT NAME "
                + "FROM AM_API_THROTTLE_POLICY LEFT JOIN AM_CONDITION_GROUP "
                + "ON AM_API_THROTTLE_POLICY.POLICY_ID = AM_CONDITION_GROUP.POLICY_ID "
                + "WHERE "
                + "(DEFAULT_QUOTA_TYPE  = '" + QUOTA_TYPE_BANDWIDTH + "' OR QUOTA_TYPE  = '"+ QUOTA_TYPE_BANDWIDTH + "') "
                + "AND TENANT_ID = ?";
        public static final String TIER_HAS_ATTACHED_TO_APPLICATION = "SELECT 1 FROM AM_APPLICATION WHERE " +
                "SUBSCRIBER_ID IN (SELECT SUBSCRIBER_ID FROM AM_SUBSCRIBER WHERE TENANT_ID = ?) " +
                "AND AM_APPLICATION.APPLICATION_TIER = ?";

        public static final String TIER_HAS_ATTACHED_TO_SUBSCRIPTION_SUPER_TENANT = "(SELECT 1 from AM_SUBSCRIPTION " +
                "WHERE API_ID IN (SELECT API_ID FROM AM_API WHERE CONTEXT NOT LIKE '/t/%') AND TIER_ID_PENDING = ?) " +
                "UNION " +
                "(SELECT 1 FROM AM_SUBSCRIPTION WHERE  API_ID IN (SELECT API_ID FROM AM_API WHERE CONTEXT NOT LIKE " +
                "'/t/%') AND TIER_ID = ?)";
        public static final String TIER_HAS_ATTACHED_TO_SUBSCRIPTION_TENANT = "(SELECT 1 from AM_SUBSCRIPTION " +
                "WHERE API_ID IN (SELECT API_ID FROM AM_API WHERE CONTEXT LIKE ?) AND TIER_ID_PENDING = ?) " +
                "UNION " +
                "(SELECT 1 FROM AM_SUBSCRIPTION WHERE  API_ID IN (SELECT API_ID FROM AM_API WHERE CONTEXT LIKE ?) " +
                "AND  TIER_ID = ?)";
        public static final String TIER_HAS_ATTACHED_TO_API_RESOURCE_SUPER_TENANT = "(SELECT 1 FROM AM_API WHERE " +
                "CONTEXT NOT LIKE '/t/%' AND API_TIER = ?) " +
                "UNION " +
                "(SELECT 1 FROM AM_API_URL_MAPPING WHERE API_ID IN (SELECT API_ID FROM AM_API WHERE CONTEXT NOT LIKE " +
                "'/t/%') AND THROTTLING_TIER = ?)";
        public static final String TIER_HAS_ATTACHED_TO_API_RESOURCE_TENANT = "(SELECT 1 FROM AM_API WHERE " +
                "CONTEXT LIKE ? AND API_TIER = ?) UNION (SELECT 1 FROM AM_API_URL_MAPPING WHERE API_ID IN " +
                "(SELECT API_ID FROM AM_API WHERE CONTEXT LIKE ?) AND THROTTLING_TIER = ?)";
    }

    public static class CertificateConstants {
        public static final String INSERT_CERTIFICATE = "INSERT INTO AM_CERTIFICATE_METADATA " +
                "(TENANT_ID, END_POINT, ALIAS) VALUES(?, ?, ?)";

        public static final String GET_CERTIFICATES = "SELECT * FROM AM_CERTIFICATE_METADATA WHERE TENANT_ID=?";

        public static final String GET_CERTIFICATE_ALL_TENANTS = "SELECT * FROM AM_CERTIFICATE_METADATA WHERE " +
                "(ALIAS=?)";
        public static final String GET_CERTIFICATE_TENANT = "SELECT * FROM AM_CERTIFICATE_METADATA WHERE TENANT_ID=? " +
                "AND (ALIAS=? OR END_POINT LIKE ?)";

        public static final String DELETE_CERTIFICATES = "DELETE FROM AM_CERTIFICATE_METADATA WHERE TENANT_ID=? " +
                "AND ALIAS=?";

        public static final String CERTIFICATE_COUNT_QUERY = "SELECT COUNT(*) AS count FROM AM_CERTIFICATE_METADATA " +
                "WHERE TENANT_ID=?";

        public static final String SELECT_CERTIFICATE_FOR_ALIAS = "SELECT * FROM AM_CERTIFICATE_METADATA "
                + "WHERE ALIAS=?";
    }

    public static class ClientCertificateConstants{
        public static final String INSERT_CERTIFICATE = "INSERT INTO AM_API_CLIENT_CERTIFICATE " +
                "(CERTIFICATE, TENANT_ID, ALIAS, API_ID, TIER_NAME) VALUES(?, ?, ?, ?, ?)";

        public static final String GET_CERTIFICATES_FOR_API = "SELECT ALIAS FROM AM_API_CLIENT_CERTIFICATE WHERE "
                + "TENANT_ID=? and API_ID=? and REMOVED=?";

        public static final String DELETE_CERTIFICATES_FOR_API = "DELETE FROM AM_API_CLIENT_CERTIFICATE "
                + "WHERE TENANT_ID=? and API_ID=? and REMOVED=?";

        public static final String SELECT_CERTIFICATE_FOR_ALIAS = "SELECT ALIAS FROM AM_API_CLIENT_CERTIFICATE "
                + "WHERE ALIAS=? AND REMOVED=? AND TENANT_ID =?";

        public static final String SELECT_CERTIFICATE_FOR_TENANT =
                "SELECT AC.CERTIFICATE, AC.ALIAS, AC.TIER_NAME, AA.API_PROVIDER, AA.API_NAME, "
                        + "AA.API_VERSION FROM AM_API_CLIENT_CERTIFICATE AC, AM_API AA "
                        + "WHERE AC.REMOVED=? AND AC.TENANT_ID=? AND AA.API_ID=AC.API_ID";

        public static final String SELECT_CERTIFICATE_FOR_TENANT_ALIAS =
                "SELECT AC.CERTIFICATE, AC.ALIAS, AC.TIER_NAME, AA.API_PROVIDER, AA.API_NAME, AA.API_VERSION "
                        + "FROM AM_API_CLIENT_CERTIFICATE AC, AM_API AA "
                        + "WHERE AC.REMOVED=? AND AC.TENANT_ID=? AND AC.ALIAS=? AND AA.API_ID=AC.API_ID";

        public static final String SELECT_CERTIFICATE_FOR_TENANT_ALIAS_APIID =
                "SELECT AC.CERTIFICATE, AC.ALIAS, AC.TIER_NAME FROM AM_API_CLIENT_CERTIFICATE AC "
                        + "WHERE AC.REMOVED=? AND AC.TENANT_ID=? AND AC.ALIAS=? AND AC.API_ID = ?";

        public static final String SELECT_CERTIFICATE_FOR_TENANT_APIID =
                "SELECT AC.CERTIFICATE, AC.ALIAS, AC.TIER_NAME FROM AM_API_CLIENT_CERTIFICATE AC "
                        + "WHERE AC.REMOVED=? AND AC.TENANT_ID=? AND AC.API_ID=?";

        public static final String PRE_DELETE_CERTIFICATES = "DELETE FROM AM_API_CLIENT_CERTIFICATE "
                + "WHERE TENANT_ID=? AND REMOVED=? ANd ALIAS=? AND API_ID=?";

        public static final String PRE_DELETE_CERTIFICATES_WITHOUT_APIID = "DELETE FROM AM_API_CLIENT_CERTIFICATE "
                + "WHERE TENANT_ID=? AND REMOVED=? and ALIAS=?";

        public static final String DELETE_CERTIFICATES = "UPDATE AM_API_CLIENT_CERTIFICATE SET REMOVED = ? "
                + "WHERE TENANT_ID=? AND ALIAS=? AND API_ID=?";

        public static final String DELETE_CERTIFICATES_WITHOUT_APIID = "UPDATE AM_API_CLIENT_CERTIFICATE SET REMOVED=? "
                + "WHERE TENANT_ID=? AND ALIAS=?";

        public static final String CERTIFICATE_COUNT_QUERY = "SELECT COUNT(*) AS count FROM AM_API_CLIENT_CERTIFICATE " +
                "WHERE TENANT_ID=? AND REMOVED=?";
    }

    /**
     * Static class to hold database queries related to AM_SYSTEM_APPS table
     */
    public static class SystemApplicationConstants {

        public static final String INSERT_SYSTEM_APPLICATION =
                "INSERT INTO AM_SYSTEM_APPS " + "(NAME,CONSUMER_KEY,CONSUMER_SECRET,TENANT_DOMAIN,CREATED_TIME) " +
                        "VALUES (?,?,?,?,?)";

        public static final String GET_APPLICATIONS =
                "SELECT * FROM " + "AM_SYSTEM_APPS WHERE TENANT_DOMAIN = ?";

        public static final String GET_CLIENT_CREDENTIALS_FOR_APPLICATION =
                "SELECT CONSUMER_KEY,CONSUMER_SECRET FROM " + "AM_SYSTEM_APPS WHERE NAME = ? AND TENANT_DOMAIN = ?";

        public static final String DELETE_SYSTEM_APPLICATION = "DELETE FROM AM_SYSTEM_APPS WHERE NAME = ? AND " +
                "TENANT_DOMAIN = ?";

        public static final String CHECK_CLIENT_CREDENTIALS_EXISTS = "SELECT CONSUMER_KEY,CONSUMER_SECRET " +
                "FROM AM_SYSTEM_APPS WHERE NAME = ? AND TENANT_DOMAIN = ?";
    }

    public static class BotDataConstants {

        public static final String ADD_NOTIFICATION = "INSERT INTO AM_NOTIFICATION_SUBSCRIBER (UUID, CATEGORY," +
        "NOTIFICATION_METHOD, SUBSCRIBER_ADDRESS) VALUES(?,?,?,?)";

        public static final String GET_SAVED_ALERT_EMAILS =
                " SELECT UUID, SUBSCRIBER_ADDRESS FROM AM_NOTIFICATION_SUBSCRIBER";

        public static final String DELETE_EMAIL_BY_UUID =
                "DELETE FROM AM_NOTIFICATION_SUBSCRIBER WHERE UUID= ?";

        public static final String GET_ALERT_SUBSCRIPTION_BY_UUID =
                "SELECT UUID, SUBSCRIBER_ADDRESS FROM AM_NOTIFICATION_SUBSCRIBER WHERE UUID = ?";

        public static final String GET_ALERT_SUBSCRIPTION_BY_EMAIL =
                "SELECT UUID, SUBSCRIBER_ADDRESS FROM AM_NOTIFICATION_SUBSCRIBER WHERE SUBSCRIBER_ADDRESS = ?";

        public static final String GET_BOT_DETECTED_DATA =
                "from AM_BOT_DATA SELECT request_time, message_id, http_method, headers, message_body, client_ip";

    }

    public static class RevokedJWTConstants {

        public static final String ADD_JWT_SIGNATURE = "INSERT INTO AM_REVOKED_JWT (UUID, SIGNATURE," +
                "EXPIRY_TIMESTAMP, TENANT_ID, TOKEN_TYPE) VALUES(?,?,?,?,?)";
        public static final String CHECK_REVOKED_TOKEN_EXIST = "SELECT 1 FROM AM_REVOKED_JWT WHERE UUID = ?";
        public static final String DELETE_REVOKED_JWT = "DELETE FROM AM_REVOKED_JWT WHERE EXPIRY_TIMESTAMP < ?";
    }

    //Shared Scopes related constants
    public static final String ADD_SHARED_SCOPE = "INSERT INTO AM_SHARED_SCOPE (NAME, UUID, TENANT_ID) VALUES (?,?,?)";
    public static final String DELETE_SHARED_SCOPE = "DELETE FROM AM_SHARED_SCOPE WHERE NAME = ? AND TENANT_ID = ?";
    public static final String GET_SHARED_SCOPE_BY_UUID = "SELECT NAME FROM AM_SHARED_SCOPE WHERE UUID = ?";
    public static final String GET_ALL_SHARED_SCOPE_KEYS_BY_TENANT = "SELECT NAME FROM AM_SHARED_SCOPE " +
            "WHERE TENANT_ID = ?";
    public static final String IS_SHARED_SCOPE_NAME_EXISTS = "SELECT 1 FROM AM_SHARED_SCOPE " +
            "WHERE TENANT_ID = ? AND NAME = ?";
    public static final String GET_ALL_SHARED_SCOPES_BY_TENANT = "SELECT UUID, NAME FROM AM_SHARED_SCOPE " +
            "WHERE TENANT_ID = ?";
    public static final String GET_SHARED_SCOPE_USAGE_COUNT_BY_TENANT =
            "SELECT SS.NAME, SS.UUID, "
                    + "(SELECT COUNT(*) FROM AM_API_RESOURCE_SCOPE_MAPPING RSM WHERE RSM.SCOPE_NAME=SS.NAME AND "
                    + "RSM.TENANT_ID = ?) usages "
                    + "FROM AM_SHARED_SCOPE SS "
                    + "WHERE SS.TENANT_ID = ?";
    public static final String GET_SHARED_SCOPE_API_USAGE_BY_TENANT =
            "SELECT AA.API_ID, AA.API_NAME, AA.CONTEXT, AA.API_VERSION, AA.API_PROVIDER "
                    + "FROM AM_SHARED_SCOPE ASSC, AM_API_RESOURCE_SCOPE_MAPPING AARSM, "
                    + "AM_API_URL_MAPPING AAUM, AM_API AA "
                    + "WHERE ASSC.NAME=AARSM.SCOPE_NAME AND "
                    + "AARSM.URL_MAPPING_ID=AAUM.URL_MAPPING_ID AND "
                    + "AAUM.API_ID=AA.API_ID AND "
                    + "ASSC.UUID=? AND "
                    + "AARSM.TENANT_ID=? "
                    + "GROUP BY AA.API_ID, AA.API_NAME, AA.CONTEXT, AA.API_VERSION, AA.API_PROVIDER";

    public static final String GET_SHARED_SCOPE_URI_USAGE_BY_TENANT =
            "SELECT AAUM.URL_PATTERN, AAUM.HTTP_METHOD "
                    + "FROM AM_SHARED_SCOPE ASSC, AM_API_RESOURCE_SCOPE_MAPPING AARSM, "
                    + "AM_API_URL_MAPPING AAUM, AM_API AA "
                    + "WHERE ASSC.NAME=AARSM.SCOPE_NAME AND "
                    + "AARSM.URL_MAPPING_ID=AAUM.URL_MAPPING_ID AND "
                    + "AAUM.API_ID=AA.API_ID AND "
                    + "ASSC.UUID=? AND "
                    + "AARSM.TENANT_ID=? AND "
                    + "AA.API_ID=?";

    //Resource Scope related constants
    public static final String ADD_API_RESOURCE_SCOPE_MAPPING =
            "INSERT INTO AM_API_RESOURCE_SCOPE_MAPPING (SCOPE_NAME, URL_MAPPING_ID, TENANT_ID) VALUES (?, ?, ?)";
    public static final String IS_SCOPE_ATTACHED_LOCALLY =
            "SELECT AM_API.API_NAME, AM_API.API_PROVIDER "
                    + "FROM AM_API_RESOURCE_SCOPE_MAPPING ARSM, AM_API_URL_MAPPING AUM, AM_API "
                    + "WHERE ARSM.SCOPE_NAME = ? AND "
                    + "ARSM.TENANT_ID = ? AND "
                    + "ARSM.SCOPE_NAME NOT IN (SELECT GS.NAME FROM AM_SHARED_SCOPE GS WHERE GS.TENANT_ID = ?) AND "
                    + "ARSM.URL_MAPPING_ID = AUM.URL_MAPPING_ID AND "
                    + "AUM.API_ID = AM_API.API_ID";
    public static final String IS_SCOPE_ATTACHED =
            "SELECT 1 FROM AM_API_RESOURCE_SCOPE_MAPPING WHERE SCOPE_NAME = ? AND TENANT_ID = ?";

    public static final String REMOVE_RESOURCE_SCOPE_URL_MAPPING_SQL =
            " DELETE FROM AM_API_RESOURCE_SCOPE_MAPPING "
                    + "WHERE URL_MAPPING_ID IN ( SELECT URL_MAPPING_ID FROM AM_API_URL_MAPPING WHERE API_ID = ? )";

    public static final String GET_UNVERSIONED_LOCAL_SCOPES_FOR_API_SQL =
            "SELECT DISTINCT ARSM.SCOPE_NAME "
                    + "FROM AM_API_RESOURCE_SCOPE_MAPPING ARSM INNER JOIN AM_API_URL_MAPPING AUM "
                    + "ON ARSM.URL_MAPPING_ID = AUM.URL_MAPPING_ID "
                    + "WHERE AUM.API_ID = ? AND ARSM.TENANT_ID = ? AND "
                    + "ARSM.SCOPE_NAME NOT IN (SELECT GS.NAME FROM AM_SHARED_SCOPE GS WHERE GS.TENANT_ID = ?) AND "
                    + "ARSM.SCOPE_NAME NOT IN ( "
                    + "SELECT ARSM2.SCOPE_NAME FROM AM_API_RESOURCE_SCOPE_MAPPING ARSM2 "
                    + "INNER JOIN AM_API_URL_MAPPING AUM2 ON ARSM2.URL_MAPPING_ID = AUM2.URL_MAPPING_ID "
                    + "WHERE AUM2.API_ID != ? AND ARSM2.TENANT_ID = ?)";

    public static final String GET_VERSIONED_LOCAL_SCOPES_FOR_API_SQL =
            "SELECT DISTINCT ARSM.SCOPE_NAME "
                    + "FROM AM_API_RESOURCE_SCOPE_MAPPING ARSM INNER JOIN AM_API_URL_MAPPING AUM "
                    + "ON ARSM.URL_MAPPING_ID = AUM.URL_MAPPING_ID "
                    + "WHERE AUM.API_ID = ? AND ARSM.TENANT_ID = ? AND "
                    + "ARSM.SCOPE_NAME NOT IN (SELECT GS.NAME FROM AM_SHARED_SCOPE GS WHERE GS.TENANT_ID = ?) AND "
                    + "ARSM.SCOPE_NAME IN ( "
                    + "SELECT ARSM2.SCOPE_NAME FROM AM_API_RESOURCE_SCOPE_MAPPING ARSM2 "
                    + "INNER JOIN AM_API_URL_MAPPING AUM2 ON ARSM2.URL_MAPPING_ID = AUM2.URL_MAPPING_ID "
                    + "WHERE AUM2.API_ID != ? AND ARSM2.TENANT_ID = ?)";

    public static final String GET_ALL_LOCAL_SCOPES_FOR_API_SQL =
            "SELECT DISTINCT ARSM.SCOPE_NAME "
                    + "FROM AM_API_RESOURCE_SCOPE_MAPPING ARSM INNER JOIN AM_API_URL_MAPPING AUM "
                    + "ON ARSM.URL_MAPPING_ID = AUM.URL_MAPPING_ID "
                    + "WHERE AUM.API_ID = ? AND ARSM.TENANT_ID = ? AND "
                    + "ARSM.SCOPE_NAME NOT IN (SELECT GS.NAME FROM AM_SHARED_SCOPE GS WHERE GS.TENANT_ID = ?)";

    public static final String GET_URL_TEMPLATES_WITH_SCOPES_FOR_API_SQL =
            " SELECT AUM.URL_MAPPING_ID, "
                    + "AUM.URL_PATTERN, "
                    + "AUM.HTTP_METHOD, "
                    + "AUM.AUTH_SCHEME, "
                    + "AUM.THROTTLING_TIER, "
                    + "AUM.MEDIATION_SCRIPT, "
                    + "ARSM.SCOPE_NAME "
                    + "FROM "
                    + "AM_API_URL_MAPPING AUM "
                    + "INNER JOIN AM_API_RESOURCE_SCOPE_MAPPING ARSM ON AUM.URL_MAPPING_ID = ARSM.URL_MAPPING_ID "
                    + "AND AUM.API_ID = ?";

    public static final String GET_API_SCOPES_SQL =
            " SELECT ARSM.SCOPE_NAME FROM AM_API_RESOURCE_SCOPE_MAPPING ARSM, AM_API_URL_MAPPING AUM "
                    + "WHERE ARSM.URL_MAPPING_ID = AUM.URL_MAPPING_ID AND AUM.API_ID = ?";

    public static final String INSERT_SCOPE_SQL = "INSERT INTO AM_SCOPE (NAME,DISPLAY_NAME,DESCRIPTION,TENANT_ID," +
            "SCOPE_TYPE) VALUES (?,?,?,?,?)";
    public static final String UPDATE_SCOPE_SQL = "UPDATE AM_SCOPE SET DISPLAY_NAME = ?, DESCRIPTION = ? " +
            "WHERE NAME = ? AND TENANT_ID = ?";
    public static final String DELETE_SCOPE_SQL = "DELETE FROM AM_SCOPE WHERE NAME = ? AND TENANT_ID = ?";
    public static final String GET_SCOPE_SQL = "SELECT NAME AS SCOPE_KEY,DISPLAY_NAME AS DISPLAY_NAME,DESCRIPTION AS " +
            "DESCRIPTION,TENANT_ID AS TENANT_ID FROM AM_SCOPE WHERE NAME = ? AND TENANT_ID = ?";
    public static final String ADD_SCOPE_MAPPING = "INSERT INTO AM_SCOPE_BINDING (SCOPE_ID, SCOPE_BINDING, " +
            "BINDING_TYPE) VALUES((SELECT SCOPE_ID FROM AM_SCOPE WHERE NAME = ? AND TENANT_ID = ?),?,?)";
    public static final String DELETE_SCOPE_MAPPING =
            "DELETE FROM AM_SCOPE_BINDING WHERE SCOPE_ID = (SELECT SCOPE_ID FROM AM_SCOPE WHERE NAME = ? " +
                    "AND TENANT_ID = ?)";
    public static final String RETRIEVE_SCOPE_MAPPING =
            "SELECT SCOPE_BINDING FROM AM_SCOPE_BINDING WHERE SCOPE_ID = (SELECT SCOPE_ID FROM AM_SCOPE " +
                    "WHERE NAME = ? AND TENANT_ID = ?) AND BINDING_TYPE = ?";
    public static final String GET_SCOPES_SQL = "SELECT NAME AS SCOPE_KEY,DISPLAY_NAME AS DISPLAY_NAME,DESCRIPTION AS " +
            "DESCRIPTION,TENANT_ID AS TENANT_ID FROM AM_SCOPE WHERE TENANT_ID = ? AND SCOPE_TYPE = ?";

    public static final String SCOPE_EXIST_SQL = "SELECT 1 FROM AM_SCOPE WHERE NAME = ? AND TENANT_ID = ?";

    public static class KeyManagerSqlConstants {
        public static final String ADD_KEY_MANAGER =
                " INSERT INTO AM_KEY_MANAGER (UUID,NAME,DESCRIPTION,TYPE,CONFIGURATION,TENANT_DOMAIN,ENABLED," +
                        "DISPLAY_NAME) VALUES (?,?,?,?,?,?,?,?)";
        public static final String UPDATE_KEY_MANAGER =
                "UPDATE AM_KEY_MANAGER SET NAME = ?,DESCRIPTION = ?,TYPE = ?,CONFIGURATION = ?,TENANT_DOMAIN = ?," +
                        "ENABLED = ?,DISPLAY_NAME = ? WHERE UUID = ?";

        public static final String DELETE_KEY_MANAGER =
                "DELETE FROM AM_KEY_MANAGER WHERE UUID = ? AND TENANT_DOMAIN = ?";
    }

    /**
     * Static class to hold database queries related to AM_TENANT_THEMES table
     */
    public static class TenantThemeConstants {

        public static final String ADD_TENANT_THEME = "INSERT INTO AM_TENANT_THEMES (TENANT_ID, THEME) VALUES (?,?)";
        public static final String UPDATE_TENANT_THEME = "UPDATE AM_TENANT_THEMES SET THEME = ? WHERE TENANT_ID = ?";
        public static final String DELETE_TENANT_THEME = "DELETE FROM AM_TENANT_THEMES WHERE TENANT_ID = ?";
        public static final String GET_TENANT_THEME = "SELECT * FROM AM_TENANT_THEMES WHERE TENANT_ID = ?";
    }
}

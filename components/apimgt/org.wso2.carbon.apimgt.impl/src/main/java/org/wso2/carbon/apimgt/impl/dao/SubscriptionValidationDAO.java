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
package org.wso2.carbon.apimgt.impl.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.model.subscription.API;
import org.wso2.carbon.apimgt.api.model.subscription.Application;
import org.wso2.carbon.apimgt.api.model.subscription.ApplicationKeyMapping;
import org.wso2.carbon.apimgt.api.model.subscription.ApplicationPolicy;
import org.wso2.carbon.apimgt.api.model.subscription.Policy;
import org.wso2.carbon.apimgt.api.model.subscription.Subscription;
import org.wso2.carbon.apimgt.api.model.subscription.SubscriptionPolicy;
import org.wso2.carbon.apimgt.api.model.subscription.URLMapping;
import org.wso2.carbon.apimgt.impl.dao.constants.SubscriptionValidationSQLConstants;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubscriptionValidationDAO {

    private static Log log = LogFactory.getLog(SubscriptionValidationDAO.class);

    /*
     * This method can be used to retrieve all the APIs in the database
     *
     * @return {@link Map<Integer, API>}
     * */
    public static Map<Integer, API> getAllApis() {

        Map<Integer, API> apiMap = new HashMap<>();
        try (
                Connection conn = APIMgtDBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(SubscriptionValidationSQLConstants.GET_ALL_APIS_SQL);
                ResultSet resultSet = ps.executeQuery();
        ) {

            while (resultSet.next()) {
                API api = new API();
                api.setApiId(resultSet.getInt("API_ID"));
                api.setProvider(resultSet.getString("API_PROVIDER"));
                api.setName(resultSet.getString("API_NAME"));
                api.setPolicy(resultSet.getString("API_TIER"));
                api.setVersion(resultSet.getString("API_VERSION"));
                api.setContext(resultSet.getString("CONTEXT"));
                apiMap.put(api.getApiId(), api);
            }

        } catch (SQLException e) {
            log.error("Error in loading Applications : ", e);
        }

        return apiMap;
    }

    /*
     * This method can be used to retrieve all the Subscriptions in the database
     *
     * @return {@link List<Subscription>}
     * */
    public static List<Subscription> getAllSubscriptions() {

        ArrayList<Subscription> subscriptions = new ArrayList<>();
        try (Connection conn = APIMgtDBUtil.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(SubscriptionValidationSQLConstants.GET_ALL_SUBSCRIPTIONS_SQL);
             ResultSet resultSet = ps.executeQuery();) {

            while (resultSet.next()) {
                Subscription subscription = new Subscription();
                subscription.setSubscriptionId(resultSet.getInt("SUB_ID"));
                subscription.setPolicyId(resultSet.getString("TIER"));
                subscription.setApiId(resultSet.getInt("API_ID"));
                subscription.setAppId(resultSet.getInt("APP_ID"));
                subscription.setSubscriptionState(resultSet.getString("STATUS"));
                subscriptions.add(subscription);
            }

        } catch (SQLException e) {
            log.error("Error in loading Subscription : ", e);
        }

        return subscriptions;
    }

    /*
     * This method can be used to retrieve all the Applications in the database
     *
     * @return {@link List<Application>}
     * */
    public static List<Application> getAllApplications() {

        ArrayList<Application> applications = new ArrayList<>();
        try (Connection conn = APIMgtDBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SubscriptionValidationSQLConstants.GET_ALL_APPLICATIONS_SQL);
             ResultSet resultSet = ps.executeQuery();
        ) {

            while (resultSet.next()) {
                Application application = new Application();
                application.setId(resultSet.getInt("APP_ID"));
                application.setName(resultSet.getString("NAME"));
                application.setPolicy(resultSet.getString("TIER"));
                application.setSubId(resultSet.getInt("SUB_ID"));
                application.setTokenType(resultSet.getString("TOKEN_TYPE"));
                application.setGroupId(resultSet.getString("GROUP_ID"));
                applications.add(application);
            }

        } catch (SQLException e) {
            log.error("Error in loading Applications : ", e);
        }

        return applications;
    }

    /*
     * This method can be used to retrieve all the ApplicationKeyMappings in the database
     *
     * @return {@link List<ApplicationKeyMapping>}
     * */
    public static List<ApplicationKeyMapping> getAllApplicationKeyMappings() {

        ArrayList<ApplicationKeyMapping> keyMappings = new ArrayList<>();

        try (
                Connection conn = APIMgtDBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(SubscriptionValidationSQLConstants.GET_ALL_AM_KEY_MAPPINGS_SQL);
                ResultSet resultSet = ps.executeQuery();
        ) {

            while (resultSet.next()) {
                ApplicationKeyMapping keyMapping = new ApplicationKeyMapping();
                keyMapping.setApplicationId(resultSet.getInt("APPLICATION_ID"));
                keyMapping.setConsumerKey(resultSet.getString("CONSUMER_KEY"));
                keyMapping.setKeyType(resultSet.getString("KEY_TYPE"));
                keyMappings.add(keyMapping);
            }

        } catch (SQLException e) {
            log.error("Error in loading Application Key Mappings : ", e);
        }

        return keyMappings;
    }

    /*
     * This method can be used to retrieve all the SubscriptionPolicies in the database
     *
     * @return {@link List<SubscriptionPolicy>}
     * */
    public static List<SubscriptionPolicy> getAllSubscriptionPolicies() {

        ArrayList<SubscriptionPolicy> subscriptionPolicies = new ArrayList<>();
        try (
                Connection conn = APIMgtDBUtil.getConnection();
                PreparedStatement ps =
                        conn.prepareStatement(SubscriptionValidationSQLConstants.GET_ALL_SUBSCRIPTION_POLICIES_SQL);
                ResultSet resultSet = ps.executeQuery();
        ) {

            while (resultSet.next()) {
                Policy policyDTO = new Policy();
                SubscriptionPolicy subscriptionPolicyDTO = new SubscriptionPolicy();

                policyDTO.setId(resultSet.getInt("POLICY_ID"));
                policyDTO.setName(resultSet.getString("NAME"));
                policyDTO.setQuotaType(resultSet.getString("QUOTA_TYPE"));
                policyDTO.setTenantId(resultSet.getInt("TENANT_ID"));

                subscriptionPolicyDTO.setAllOf(policyDTO);
                subscriptionPolicyDTO.setRateLimitCount(resultSet.getInt("RATE_LIMIT_COUNT"));
                subscriptionPolicyDTO.setRateLimitTimeUnit(resultSet.getString("RATE_LIMIT_TIME_UNIT"));
                subscriptionPolicyDTO.setStopOnQuotaReach(resultSet.getBoolean("STOP_ON_QUOTA_REACH"));

                subscriptionPolicies.add(subscriptionPolicyDTO);
            }

        } catch (SQLException e) {
            log.error("Error in loading Subscription policies : ", e);
        }

        return subscriptionPolicies;
    }

    /*
     * This method can be used to retrieve all the ApplicationPolicys in the database
     *
     * @return {@link List<ApplicationPolicy>}
     * */
    public static List<ApplicationPolicy> getAllApplicationPolicies() {

        ArrayList<ApplicationPolicy> applicationPolicie = new ArrayList<>();
        try (
                Connection conn = APIMgtDBUtil.getConnection();
                PreparedStatement ps =
                        conn.prepareStatement(SubscriptionValidationSQLConstants.GET_ALL_APPLICATION_POLICIES_SQL);
                ResultSet resultSet = ps.executeQuery();
        ) {

            while (resultSet.next()) {
                ApplicationPolicy applicationPolicyDTO = new ApplicationPolicy();
                applicationPolicyDTO.setId(resultSet.getInt("POLICY_ID"));
                applicationPolicyDTO.setName(resultSet.getString("NAME"));
                applicationPolicyDTO.setQuotaType(resultSet.getString("QUOTA_TYPE"));
                applicationPolicyDTO.setTenantId(resultSet.getInt("TENANT_ID"));
                applicationPolicie.add(applicationPolicyDTO);
            }

        } catch (SQLException e) {
            log.error("Error in loading application policies : ", e);
        }

        return applicationPolicie;
    }

    /*
     * This method can be used to retrieve all the APIs of a given tenant in the database
     *
     * @param tenantId : unique identifier of tenant
     * @return {@link Map<Integer, API>}
     * */
    public static Map<String, API> getAllApis(int tenantId) {

        Map<String, API> apiMap = new HashMap<>();
        String tenantDomain = APIUtil.getTenantDomainFromTenantId(tenantId);
        try (
                Connection conn = APIMgtDBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(SubscriptionValidationSQLConstants.GET_TENANT_APIS_SQL);) {
            ps.setString(1, "%" + tenantDomain + "%");
            ResultSet resultSet = ps.executeQuery();

            while (resultSet.next()) {
                API api = new API();
                api.setApiId(resultSet.getInt("API_ID"));
                api.setProvider(resultSet.getString("API_PROVIDER"));
                api.setName(resultSet.getString("API_NAME"));
                api.setPolicy(resultSet.getString("API_TIER"));
                api.setVersion(resultSet.getString("API_VERSION"));
                api.setContext(resultSet.getString("CONTEXT"));
                apiMap.put(api.getContext() + ":" + api.getVersion(), api);
            }

        } catch (SQLException e) {
            log.error("Error in loading Apis for tenantId : " + tenantId, e);
        }

        return apiMap;
    }

    /*
     * This method can be used to retrieve an API in the database
     *
     * @param apiId : unique identifier of an API
     * @return {@link API}
     * */
    public static API getApi(int apiId) {
        API api = new API();

        try (
                Connection conn = APIMgtDBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(SubscriptionValidationSQLConstants.GET_API_SQL);) {
            ps.setInt(1, apiId);
            ResultSet resultSet = ps.executeQuery();

            while (resultSet.next()) {
                api.setApiId(resultSet.getInt("API_ID"));
                api.setProvider(resultSet.getString("API_PROVIDER"));
                api.setName(resultSet.getString("API_NAME"));
                api.setPolicy(resultSet.getString("API_TIER"));
                api.setVersion(resultSet.getString("API_VERSION"));
                api.setContext(resultSet.getString("CONTEXT"));
            }

        } catch (SQLException e) {
            log.error("Error in loading API for apiId : " + apiId, e);
        }

        return api;
    }

    /*
     * This method can be used to retrieve all the APIs of a given tesanat in the database
     *
     * @param subscriptionId : unique identifier of a subscription
     * @return {@link Subscription}
     * */
    public static List<Subscription> getAllSubscriptions(int tenantId) {

        ArrayList<Subscription> subscriptions = new ArrayList<>();
        try (Connection conn = APIMgtDBUtil.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(SubscriptionValidationSQLConstants.GET_TENANT_SUBSCRIPTIONS_SQL);
        ) {
            ps.setInt(1, tenantId);

            try (ResultSet resultSet = ps.executeQuery();) {

                while (resultSet.next()) {
                    Subscription subscription = new Subscription();
                    subscription.setSubscriptionId(resultSet.getInt("SUB_ID"));
                    subscription.setPolicyId(resultSet.getString("TIER"));
                    subscription.setApiId(resultSet.getInt("API_ID"));
                    subscription.setAppId(resultSet.getInt("APP_ID"));
                    subscription.setSubscriptionState(resultSet.getString("STATUS"));
                    subscriptions.add(subscription);
                }
            }
        } catch (SQLException e) {
            log.error("Error in loading Subscriptions for tenantId : " + tenantId, e);
        }
        return subscriptions;
    }

    /*
     * This method can be used to retrieve all the Applications of a given tenant in the database
     * @param tenantId : tenant Id
     * @return {@link Subscription}
     * */
    public static List<Application> getAllApplications(int tenantId) {

        ArrayList<Application> applications = new ArrayList<>();
        try (Connection conn = APIMgtDBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SubscriptionValidationSQLConstants.GET_TENANT_APPLICATIONS_SQL);
        ) {
            ps.setInt(1, tenantId);
            ResultSet resultSet = ps.executeQuery();

            while (resultSet.next()) {
                Application application = new Application();
                application.setId(resultSet.getInt("APP_ID"));
                application.setName(resultSet.getString("NAME"));
                application.setPolicy(resultSet.getString("TIER"));
                application.setSubId(resultSet.getInt("SUB_ID"));
                application.setTokenType(resultSet.getString("TOKEN_TYPE"));
                application.setGroupId(resultSet.getString("GROUP_ID"));
                applications.add(application);
            }
        } catch (SQLException e) {
            log.error("Error in loading Applications for tenantId : " + tenantId, e);
        }

        return applications;
    }

    /*
     * @param subscriptionId : unique identifier of a subscription
     * @return {@link Subscription}
     * */
    public static List<ApplicationKeyMapping> getAllApplicationKeyMappings(int tenantId) {

        ArrayList<ApplicationKeyMapping> keyMappings = new ArrayList<>();
        ;
        try (
                Connection conn = APIMgtDBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(SubscriptionValidationSQLConstants.GET_TENANT_AM_KEY_MAPPING_SQL);) {
            ps.setInt(1, tenantId);
            ResultSet resultSet = ps.executeQuery();

            while (resultSet.next()) {
                ApplicationKeyMapping keyMapping = new ApplicationKeyMapping();
                keyMapping.setApplicationId(resultSet.getInt("APPLICATION_ID"));
                keyMapping.setConsumerKey(resultSet.getString("CONSUMER_KEY"));
                keyMapping.setKeyType(resultSet.getString("KEY_TYPE"));
                keyMappings.add(keyMapping);
            }

        } catch (SQLException e) {
            log.error("Error in loading Application key mappings for tenantId : " + tenantId, e);
        }

        return keyMappings;
    }

    /*
     * @param subscriptionId : unique identifier of a subscription
     * @return {@link Subscription}
     * */
    public static List<SubscriptionPolicy> getAllSubscriptionPolicies(int tenantId) {

        ArrayList<SubscriptionPolicy> subscriptionPolicies = new ArrayList<>();
        try (
                Connection conn = APIMgtDBUtil.getConnection();
                PreparedStatement ps =
                        conn.prepareStatement(SubscriptionValidationSQLConstants.GET_TENANT_SUBSCRIPTION_POLICIES_SQL);
        ) {
            ps.setInt(1, tenantId);
            ResultSet resultSet = ps.executeQuery();

            while (resultSet.next()) {
                SubscriptionPolicy subscriptionPolicyDTO = new SubscriptionPolicy();

                subscriptionPolicyDTO.setId(resultSet.getInt("POLICY_ID"));
                subscriptionPolicyDTO.setName(resultSet.getString("NAME"));
                subscriptionPolicyDTO.setQuotaType(resultSet.getString("QUOTA_TYPE"));
                subscriptionPolicyDTO.setTenantId(resultSet.getInt("TENANT_ID"));

                subscriptionPolicyDTO.setRateLimitCount(resultSet.getInt("RATE_LIMIT_COUNT"));
                subscriptionPolicyDTO.setRateLimitTimeUnit(resultSet.getString("RATE_LIMIT_TIME_UNIT"));
                subscriptionPolicyDTO.setStopOnQuotaReach(resultSet.getBoolean("STOP_ON_QUOTA_REACH"));

                subscriptionPolicies.add(subscriptionPolicyDTO);
            }

        } catch (SQLException e) {
            log.error("Error in loading Subscription Policies for tenanatId : " + tenantId, e);
        }

        return subscriptionPolicies;
    }

    /*
     * @param subscriptionId : unique identifier of a subscription
     * @return {@link List<ApplicationPolicy>}
     * */
    public static List<ApplicationPolicy> getAllApplicationPolicies(int tenantId) {

        ArrayList<ApplicationPolicy> applicationPolicies = new ArrayList<>();
        try (
                Connection conn = APIMgtDBUtil.getConnection();
                PreparedStatement ps =
                        conn.prepareStatement(SubscriptionValidationSQLConstants.GET_TENANT_APPLICATION_POLICIES_SQL);) {
            ps.setInt(1, tenantId);
            ResultSet resultSet = ps.executeQuery();

            while (resultSet.next()) {
                ApplicationPolicy applicationPolicyDTO = new ApplicationPolicy();
                applicationPolicyDTO.setId(resultSet.getInt("POLICY_ID"));
                applicationPolicyDTO.setName(resultSet.getString("NAME"));
                applicationPolicyDTO.setQuotaType(resultSet.getString("QUOTA_TYPE"));
                applicationPolicyDTO.setTenantId(resultSet.getInt("TENANT_ID"));
                applicationPolicies.add(applicationPolicyDTO);
            }

        } catch (SQLException e) {
            log.error("Error in loading application policies for tenantId : " + tenantId, e);
        }

        return applicationPolicies;
    }

    /*
     * @param subscriptionId : unique identifier of a subscription
     * @return {@link Subscription}
     * */
    public static Subscription getSubscriptionById(int subscriptionId) {
        try (Connection conn = APIMgtDBUtil.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(SubscriptionValidationSQLConstants.GET_SUBSCRIPTION_SQL);
        ) {
            ps.setInt(1, subscriptionId);

            try (ResultSet resultSet = ps.executeQuery();) {

                while (resultSet.next()) {
                    Subscription subscription = new Subscription();

                    subscription.setSubscriptionId(resultSet.getInt("SUB_ID"));
                    subscription.setPolicyId(resultSet.getString("TIER"));
                    subscription.setApiId(resultSet.getInt("API_ID"));
                    subscription.setAppId(resultSet.getInt("APP_ID"));
                    subscription.setSubscriptionState(resultSet.getString("STATUS"));
                    return subscription;

                }
            }
        } catch (SQLException e) {
            log.error("Error in loading Subscription by subscriptionId : " + subscriptionId, e);
        }
        return null;
    }

    /*
     * @param applicationId : unique identifier of an application
     * @return {@link Application}
     * */
    public static Application getApplicationById(int applicationId) {
        try (Connection conn = APIMgtDBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SubscriptionValidationSQLConstants.GET_APPLICATION_BY_ID_SQL);
        ) {
            ps.setInt(1, applicationId);
            ResultSet resultSet = ps.executeQuery();

            while (resultSet.next()) {
                Application application = new Application();

                application.setId(resultSet.getInt("APPLICATION_ID"));
                application.setName(resultSet.getString("NAME"));
                application.setPolicy(resultSet.getString("APPLICATION_TIER"));
                application.setSubId(resultSet.getInt("SUBSCRIBER_ID"));
                application.setTokenType(resultSet.getString("TOKEN_TYPE"));
                application.setGroupId(resultSet.getString("GROUP_ID"));
                return application;

            }
        } catch (SQLException e) {
            log.error("Error in loading Application by applicationId : " + applicationId, e);
        }
        return null;
    }

    /*
     * @param policyId : unique identifier in an application level throttling policy
     * @return {@link ApplicationPolicy}
     * */
    public static ApplicationPolicy getApplicationPolicyById(int policyId) {

        try (
                Connection conn = APIMgtDBUtil.getConnection();
                PreparedStatement ps =
                        conn.prepareStatement(SubscriptionValidationSQLConstants.GET_APPLICATION_POLICY_SQL);
        ) {
            ps.setInt(1, policyId);
            ResultSet resultSet = ps.executeQuery();

            while (resultSet.next()) {
                ApplicationPolicy applicationPolicy = new ApplicationPolicy();

                applicationPolicy.setId(resultSet.getInt("POLICY_ID"));
                applicationPolicy.setName(resultSet.getString("NAME"));
                applicationPolicy.setQuotaType(resultSet.getString("QUOTA_TYPE"));
                applicationPolicy.setTenantId(resultSet.getInt("TENANT_ID"));
            }

        } catch (SQLException e) {
            log.error("Error in loading application policies by policyId : " + policyId, e);
        }

        return null;
    }

    /*
     * @param policyId : unique identifier in an subscription level throttling policy
     * @return {@link SubscriptionPolicy}
     * */
    public static SubscriptionPolicy getSubscriptionPolicy(int policyId) {
        try (
                Connection conn = APIMgtDBUtil.getConnection();
                PreparedStatement ps =
                        conn.prepareStatement(SubscriptionValidationSQLConstants.GET_SUBSCRIPTION_POLICY_BY_ID_SQL);
        ) {
            ps.setInt(1, policyId);
            ResultSet resultSet = ps.executeQuery();

            while (resultSet.next()) {
                SubscriptionPolicy subscriptionPolicy = new SubscriptionPolicy();

                subscriptionPolicy.setId(resultSet.getInt("POLICY_ID"));
                subscriptionPolicy.setName(resultSet.getString("NAME"));
                subscriptionPolicy.setQuotaType(resultSet.getString("QUOTA_TYPE"));
                subscriptionPolicy.setTenantId(resultSet.getInt("TENANT_ID"));

                subscriptionPolicy.setRateLimitCount(resultSet.getInt("RATE_LIMIT_COUNT"));
                subscriptionPolicy.setRateLimitTimeUnit(resultSet.getString("RATE_LIMIT_TIME_UNIT"));
                subscriptionPolicy.setStopOnQuotaReach(resultSet.getBoolean("STOP_ON_QUOTA_REACH"));
                return subscriptionPolicy;
            }

        } catch (SQLException e) {
            log.error("Error in retrieving Subscription policy by id : " + policyId, e);
        }

        return null;
    }

    /*
     * @param key : unique identifier in the form of <applicationId>.<tokenType>
     * @return {@link ApplicationKeyMapping}
     * */
    public static ApplicationKeyMapping getApplicationKeyMapping(int appId, String keyType) {

        try (
                Connection conn = APIMgtDBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(SubscriptionValidationSQLConstants.GET_AM_KEY_MAPPING_SQL);
        ) {
            ps.setInt(1, appId);
            ps.setString(2, keyType);
            ResultSet resultSet = ps.executeQuery();

            while (resultSet.next()) {
                ApplicationKeyMapping keyMapping = new ApplicationKeyMapping();
                keyMapping.setApplicationId(resultSet.getInt("APPLICATION_ID"));
                keyMapping.setConsumerKey(resultSet.getString("CONSUMER_KEY"));
                keyMapping.setKeyType(resultSet.getString("KEY_TYPE"));
                return keyMapping;
            }

        } catch (SQLException e) {
            log.error("Error in loading  Application Key Mapping for appId : " + appId + " type : " + keyType, e);
        }
        return null;
    }

    /*
     * @param consumerKey : consumer key of an application
     * @return {@link ApplicationKeyMapping}
     * */
    public static ApplicationKeyMapping getApplicationKeyMapping(String consumerKey) {

        try (
                Connection conn = APIMgtDBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        SubscriptionValidationSQLConstants.GET_AM_KEY_MAPPING_BY_CONSUMER_KEY_SQL);
        ) {
            ps.setString(1, consumerKey);
            ResultSet resultSet = ps.executeQuery();

            while (resultSet.next()) {
                ApplicationKeyMapping keyMapping = new ApplicationKeyMapping();
                keyMapping.setApplicationId(resultSet.getInt("APPLICATION_ID"));
                keyMapping.setConsumerKey(resultSet.getString("CONSUMER_KEY"));
                keyMapping.setKeyType(resultSet.getString("KEY_TYPE"));
                return keyMapping;
            }

        } catch (SQLException e) {
            log.error("Error in loading Application Key Mappinghsacfrgtghf54trtjkl;{786754w `13457868789[-876re7w4wertyi875 for consumer key : " + consumerKey, e);
        }
        return null;
    }

    /*
     * This method can be used to retrieve all the URLMappings in the database
     *
     * @return {@link List<URLMapping>}
     * */
    public static List<URLMapping> getAllURLMappings() {

        ArrayList<URLMapping> urlMappings = new ArrayList<>();
        String sql = SubscriptionValidationSQLConstants.GET_ALL_API_URL_MAPPING_SQL;

        try (Connection conn = APIMgtDBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
        ) {
            ResultSet resultSet = ps.executeQuery();

            while (resultSet.next()) {
                URLMapping urlMapping = new URLMapping();
                urlMapping.setId(resultSet.getInt("URL_MAPPING_ID"));
                urlMapping.setAuthScheme(resultSet.getString("AUTH_SCHEME"));
                urlMapping.setHttpMethod(resultSet.getString("HTTP_METHOD"));
                urlMapping.setThrottlingPolicy(resultSet.getString("POLICY"));
                urlMapping.setApiId(resultSet.getInt("API_ID"));
                urlMappings.add(urlMapping);
            }
        } catch (SQLException e) {
            log.error("Error in loading URLMappings : ", e);
        }

        return urlMappings;
    }

    /*
     * This method can be used to retrieve all the URLMappings of a given tenant in the database
     *
     * @param tenantId : tenant Id
     * @return {@link List<URLMapping>}
     * */
    public static List<URLMapping> getAllURLMappings(int tenantId) {

        List<URLMapping> urlMappings = new ArrayList<>();
        String sql = SubscriptionValidationSQLConstants.GET_TENANT_API_URL_MAPPING_SQL;
        String tenantDomain = APIUtil.getTenantDomainFromTenantId(tenantId);
        String contextParam = null;
        if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
            sql = SubscriptionValidationSQLConstants.GET_ST_API_URL_MAPPING_SQL;
            contextParam = "%/t/%";
        } else if (tenantId > 0) {
            contextParam = "%" + tenantDomain + "%";
        } else {
            sql = SubscriptionValidationSQLConstants.GET_ALL_API_URL_MAPPING_SQL;
        }
        try (Connection conn = APIMgtDBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
        ) {
            if (contextParam != null) {
                ps.setString(1, contextParam);
            }
            ResultSet resultSet = ps.executeQuery();

            while (resultSet.next()) {
                URLMapping urlMapping = new URLMapping();
                urlMapping.setId(resultSet.getInt("URL_MAPPING_ID"));
                urlMapping.setAuthScheme(resultSet.getString("AUTH_SCHEME"));
                urlMapping.setHttpMethod(resultSet.getString("HTTP_METHOD"));
                urlMapping.setThrottlingPolicy(resultSet.getString("POLICY"));
                urlMapping.setApiId(resultSet.getInt("API_ID"));
                urlMappings.add(urlMapping);
            }
        } catch (SQLException e) {
            log.error("Error in loading URLMappings for tenantId : " + tenantId, e);
        }

        return urlMappings;
    }

    /*
     * This method can be used to retrieve all the URLMappings of a given tenant in the database
     *
     * @param tenantId : tenant Id
     * @return {@link URLMapping}
     * */
    public static URLMapping getURLMapping(int mappingId) {

        try (Connection conn = APIMgtDBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     SubscriptionValidationSQLConstants.GET_API_URL_MAPPING_SQL);
        ) {
            ps.setInt(1, mappingId);
            ResultSet resultSet = ps.executeQuery();

            while (resultSet.next()) {
                URLMapping urlMapping = new URLMapping();
                urlMapping.setId(resultSet.getInt("URL_MAPPING_ID"));
                urlMapping.setAuthScheme(resultSet.getString("AUTH_SCHEME"));
                urlMapping.setHttpMethod(resultSet.getString("HTTP_METHOD"));
                urlMapping.setThrottlingPolicy(resultSet.getString("POLICY"));
                urlMapping.setApiId(resultSet.getInt("API_ID"));
                return urlMapping;
            }
        } catch (SQLException e) {
            log.error("Error in loading URLMapping for mappingId : " + mappingId, e);
        }
        return null;
    }

}

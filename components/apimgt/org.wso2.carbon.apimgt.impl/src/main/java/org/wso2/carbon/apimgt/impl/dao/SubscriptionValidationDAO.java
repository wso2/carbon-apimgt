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
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.apimgt.api.InMemorySubscriptionValidationConstants;

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
                PreparedStatement ps = conn.prepareStatement(InMemorySubscriptionValidationConstants.APIS_LOAD_SQL);
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
                     conn.prepareStatement(InMemorySubscriptionValidationConstants.SUBSCRIPTION_LOAD_SQL);
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
             PreparedStatement ps = conn.prepareStatement(InMemorySubscriptionValidationConstants.APPLICATION_LOAD_SQL);
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
                PreparedStatement ps = conn.prepareStatement(InMemorySubscriptionValidationConstants.AM_KEY_MAPPINGS);
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
                        conn.prepareStatement(InMemorySubscriptionValidationConstants.SUBSCRIPTION_POLICY_LOAD_SQL);
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
                        conn.prepareStatement(InMemorySubscriptionValidationConstants.APPLICATION_POLICY_LOAD_SQL);
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
    public static Map<Integer, API> getAllApis(int tenantId) {

        Map<Integer, API> apiMap = new HashMap<>();
        try (
                Connection conn = APIMgtDBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(InMemorySubscriptionValidationConstants.TENANT_API_LOAD_SQL);) {
            ps.setInt(1, tenantId);
            ResultSet resultSet = ps.executeQuery();

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

        try (
                Connection conn = APIMgtDBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(InMemorySubscriptionValidationConstants.API_LOAD_SQL);) {
            ps.setInt(1, apiId);
            ResultSet resultSet = ps.executeQuery();

            while (resultSet.next()) {
                API api = new API();
                api.setApiId(resultSet.getInt("API_ID"));
                api.setProvider(resultSet.getString("API_PROVIDER"));
                api.setName(resultSet.getString("API_NAME"));
                api.setPolicy(resultSet.getString("API_TIER"));
                api.setVersion(resultSet.getString("API_VERSION"));
                api.setContext(resultSet.getString("CONTEXT"));
                return api;
            }

        } catch (SQLException e) {
            log.error("Error in loading API for apiId : " + apiId, e);
        }

        return null;
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
                     conn.prepareStatement(InMemorySubscriptionValidationConstants.TENANT_SUBSCRIPTION_LOAD_SQL);
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
             PreparedStatement ps = conn.prepareStatement(InMemorySubscriptionValidationConstants.TENANT_APPLICATION_LOAD_SQL);
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
                PreparedStatement ps = conn.prepareStatement(InMemorySubscriptionValidationConstants.TENANT_AM_KEY_MAPPING);) {
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
                        conn.prepareStatement(InMemorySubscriptionValidationConstants.TENANT_SUBSCRIPTION_POLICY_LOAD_SQL);
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
     * @return {@link Subscription}
     * */
    public static List<ApplicationPolicy> getAllApplicationPolicies(int tenantId) {

        ArrayList<ApplicationPolicy> applicationPolicies = new ArrayList<>();
        try (
                Connection conn = APIMgtDBUtil.getConnection();
                PreparedStatement ps =
                        conn.prepareStatement(InMemorySubscriptionValidationConstants.TENANT_APPLICATION_POLICY_LOAD_SQL);) {
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

        Subscription subscription = new Subscription();
        try (Connection conn = APIMgtDBUtil.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(InMemorySubscriptionValidationConstants.GET_SUBSCRIPTION_SQL);
        ) {
            ps.setInt(1, subscriptionId);

            try (ResultSet resultSet = ps.executeQuery();) {

                while (resultSet.next()) {
                    subscription.setSubscriptionId(resultSet.getInt("SUB_ID"));
                    subscription.setPolicyId(resultSet.getString("TIER"));
                    subscription.setApiId(resultSet.getInt("API_ID"));
                    subscription.setAppId(resultSet.getInt("APP_ID"));
                    subscription.setSubscriptionState(resultSet.getString("STATUS"));
                }
            }
        } catch (SQLException e) {
            log.error("Error in loading Subscription by subscriptionId : " + subscriptionId, e);
        }
        return subscription;
    }

    /*
     * @param applicationId : unique identifier of an application
     * @return {@link Application}
     * */
    public static Application getApplicationById(int applicationId) {

        Application application = new Application();

        try (Connection conn = APIMgtDBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(InMemorySubscriptionValidationConstants.GET_APPLICATION_BY_ID_SQL);
        ) {
            ps.setInt(1, applicationId);
            ResultSet resultSet = ps.executeQuery();

            while (resultSet.next()) {
                application.setId(resultSet.getInt("APPLICATION_ID"));
                application.setName(resultSet.getString("NAME"));
                application.setPolicy(resultSet.getString("APPLICATION_TIER"));
                application.setSubId(resultSet.getInt("SUBSCRIBER_ID"));
                application.setTokenType(resultSet.getString("TOKEN_TYPE"));
                application.setGroupId(resultSet.getString("GROUP_ID"));
            }
        } catch (SQLException e) {
            log.error("Error in loading Application by applicationId : " + application, e);
        }

        return application;
    }

    /*
     * @param policyId : unique identifier in an application level throttling policy
     * @return {@link ApplicationPolicy}
     * */
    public static ApplicationPolicy getApplicationPolicyById(int policyId) {

        ApplicationPolicy applicationPolicyDTO = new ApplicationPolicy();

        try (
                Connection conn = APIMgtDBUtil.getConnection();
                PreparedStatement ps =
                        conn.prepareStatement(InMemorySubscriptionValidationConstants.GET_APPLICATION_POLICY_SQL);
        ) {
            ps.setInt(1, policyId);
            ResultSet resultSet = ps.executeQuery();

            while (resultSet.next()) {

                applicationPolicyDTO.setId(resultSet.getInt("POLICY_ID"));
                applicationPolicyDTO.setName(resultSet.getString("NAME"));
                applicationPolicyDTO.setQuotaType(resultSet.getString("QUOTA_TYPE"));
                applicationPolicyDTO.setTenantId(resultSet.getInt("TENANT_ID"));
            }

        } catch (SQLException e) {
            log.error("Error in loading application policies by policyId : " + policyId, e);
        }

        return applicationPolicyDTO;
    }

    /*
     * @param policyId : unique identifier in an subscription level throttling policy
     * @return {@link SubscriptionPolicy}
     * */
    public static SubscriptionPolicy getSubscriptionPolicy(int policyId) {

        SubscriptionPolicy subscriptionPolicyDTO = new SubscriptionPolicy();
        try (
                Connection conn = APIMgtDBUtil.getConnection();
                PreparedStatement ps =
                        conn.prepareStatement(InMemorySubscriptionValidationConstants.GET_SUBSCRIPTION_POLICY_BY_ID_SQL);
        ) {
            ps.setInt(1, policyId);
            ResultSet resultSet = ps.executeQuery();

            while (resultSet.next()) {

                subscriptionPolicyDTO.setId(resultSet.getInt("POLICY_ID"));
                subscriptionPolicyDTO.setName(resultSet.getString("NAME"));
                subscriptionPolicyDTO.setQuotaType(resultSet.getString("QUOTA_TYPE"));
                subscriptionPolicyDTO.setTenantId(resultSet.getInt("TENANT_ID"));

                subscriptionPolicyDTO.setRateLimitCount(resultSet.getInt("RATE_LIMIT_COUNT"));
                subscriptionPolicyDTO.setRateLimitTimeUnit(resultSet.getString("RATE_LIMIT_TIME_UNIT"));
                subscriptionPolicyDTO.setStopOnQuotaReach(resultSet.getBoolean("STOP_ON_QUOTA_REACH"));
            }

        } catch (SQLException e) {
            log.error("Error in retrieving Subscription policy by id : " + policyId, e);
        }

        return subscriptionPolicyDTO;
    }

    /*
     * @param key : unique identifier in the form of <applicationId>.<tokenType>
     * @return {@link ApplicationKeyMapping}
     * */
    public static ApplicationKeyMapping getApplicationKeyMapping(int appId, String keyType) {

        ApplicationKeyMapping keyMapping = new ApplicationKeyMapping();
        try (
                Connection conn = APIMgtDBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(InMemorySubscriptionValidationConstants.AM_KEY_MAPPING);
        ) {
            ps.setInt(1, appId);
            ps.setString(2, keyType);
            ResultSet resultSet = ps.executeQuery();

            while (resultSet.next()) {
                keyMapping.setApplicationId(resultSet.getInt("APPLICATION_ID"));
                keyMapping.setConsumerKey(resultSet.getString("CONSUMER_KEY"));
                keyMapping.setKeyType(resultSet.getString("KEY_TYPE"));
            }

        } catch (SQLException e) {
            log.error("Error in loading  Applicatio Key Mapping for appId : " + appId + " type : " + keyType, e);
        }
        return keyMapping;
    }

    /*
     * @param consumerKey : consumer key of an application
     * @return {@link ApplicationKeyMapping}
     * */
    public static ApplicationKeyMapping getApplicationKeyMapping(String consumerKey) {

        ApplicationKeyMapping keyMapping = new ApplicationKeyMapping();
        try (
                Connection conn = APIMgtDBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(InMemorySubscriptionValidationConstants.AM_KEY_MAPPING_BY_CONSUMERKAY);
        ) {
            ps.setString(1, consumerKey);
            ResultSet resultSet = ps.executeQuery();

            while (resultSet.next()) {
                keyMapping.setApplicationId(resultSet.getInt("APPLICATION_ID"));
                keyMapping.setConsumerKey(resultSet.getString("CONSUMER_KEY"));
                keyMapping.setKeyType(resultSet.getString("KEY_TYPE"));
            }

        } catch (SQLException e) {
            log.error("Error in loading Application Key Mappinghsacfrgtghf54trtjkl;{786754w `13457868789[-876re7w4wertyi875 for consumer key : " + consumerKey, e);
        }
        return keyMapping;
    }

}

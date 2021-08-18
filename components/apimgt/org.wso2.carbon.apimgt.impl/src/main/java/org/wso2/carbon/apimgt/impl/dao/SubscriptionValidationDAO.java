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

import edu.emory.mathcs.backport.java.util.Arrays;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.dto.ConditionDTO;
import org.wso2.carbon.apimgt.api.dto.ConditionGroupDTO;
import org.wso2.carbon.apimgt.api.dto.KeyManagerConfigurationDTO;
import org.wso2.carbon.apimgt.api.model.subscription.API;
import org.wso2.carbon.apimgt.api.model.subscription.APIPolicy;
import org.wso2.carbon.apimgt.api.model.subscription.APIPolicyConditionGroup;
import org.wso2.carbon.apimgt.api.model.subscription.Application;
import org.wso2.carbon.apimgt.api.model.subscription.ApplicationKeyMapping;
import org.wso2.carbon.apimgt.api.model.subscription.ApplicationPolicy;
import org.wso2.carbon.apimgt.api.model.subscription.Subscription;
import org.wso2.carbon.apimgt.api.model.subscription.SubscriptionPolicy;
import org.wso2.carbon.apimgt.api.model.subscription.URLMapping;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.constants.SubscriptionValidationSQLConstants;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SubscriptionValidationDAO {

    private static Log log = LogFactory.getLog(SubscriptionValidationDAO.class);

    /*
     * This method can be used to retrieve all the APIs in the database
     *
     * @return {@link List<API> List of APIs}
     * */
    public List<API> getAllApis() {

        List<API> apiList = new ArrayList<>();
        try (
                Connection conn = APIMgtDBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(SubscriptionValidationSQLConstants.GET_ALL_APIS_SQL);
                ResultSet resultSet = ps.executeQuery();
        ) {
            populateAPIList(resultSet, apiList);

        } catch (SQLException e) {
            log.error("Error in loading Apis : ", e);
        }

        return apiList;
    }

    /*
     * This method can be used to retrieve all the Subscriptions in the database
     *
     * @return {@link List<Subscription>}
     * */
    public List<Subscription> getAllSubscriptions() {

        List<Subscription> subscriptions = new ArrayList<>();
        try (Connection conn = APIMgtDBUtil.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(SubscriptionValidationSQLConstants.GET_ALL_SUBSCRIPTIONS_SQL);
             ResultSet resultSet = ps.executeQuery();) {
            populateSubscriptionsList(subscriptions, resultSet);

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
    public List<Application> getAllApplications() {

        List<Application> applications = new ArrayList<>();
        try (Connection conn = APIMgtDBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SubscriptionValidationSQLConstants.GET_ALL_APPLICATIONS_SQL);
             ResultSet resultSet = ps.executeQuery();
        ) {
            addToApplicationList(applications, resultSet);

        } catch (SQLException e) {
            log.error("Error in loading Applications : ", e);
        }

        return applications;
    }

    private void addToApplicationList(List<Application> list, ResultSet resultSet) throws SQLException {

        if (list == null) {
            list = new ArrayList<>();
        }
        if (resultSet != null) {
            Map<Integer, Application> temp = new Hashtable<>();
            while (resultSet.next()) {
                int appId = resultSet.getInt("APP_ID");
                Application application = temp.get(appId);
                if (application == null) {
                    application = new Application();
                    application.setId(appId);
                    application.setUuid(resultSet.getString("APP_UUID"));
                    application.setPolicy(resultSet.getString("TIER"));
                    application.setSubName(resultSet.getString("SUB_NAME"));
                    application.setName(resultSet.getString("APS_NAME"));
                    application.setTokenType(resultSet.getString("TOKEN_TYPE"));
                    temp.put(appId, application);
                }
                String attributeName = resultSet.getString("ATTRIBUTE_NAME");
                String attributeValue = resultSet.getString("ATTRIBUTE_VALUE");
                if (StringUtils.isNotEmpty(attributeName) && StringUtils.isNotEmpty(attributeValue)) {
                    application.addAttribute(attributeName, attributeValue);
                }
                // todo read from the aplication_group_mapping table and make it a set
//                application.addGroupId(resultSet.getString("GROUP_ID"));

                list.add(application);
            }
        }
    }

    /*
     * This method can be used to retrieve all the ApplicationKeyMappings in the database
     *
     * @return {@link List<ApplicationKeyMapping>}
     * */
    public List<ApplicationKeyMapping> getAllApplicationKeyMappings() {

        List<ApplicationKeyMapping> keyMappings = new ArrayList<>();

        try (
                Connection conn = APIMgtDBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(SubscriptionValidationSQLConstants.GET_ALL_AM_KEY_MAPPINGS_SQL);
                ResultSet resultSet = ps.executeQuery();
        ) {

            populateApplicationKeyMappingsList(keyMappings, resultSet);

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
    public List<SubscriptionPolicy> getAllSubscriptionPolicies() {

        List<SubscriptionPolicy> subscriptionPolicies = new ArrayList<>();
        try (
                Connection conn = APIMgtDBUtil.getConnection();
                PreparedStatement ps =
                        conn.prepareStatement(SubscriptionValidationSQLConstants.GET_ALL_SUBSCRIPTION_POLICIES_SQL);
                ResultSet resultSet = ps.executeQuery();
        ) {
            populateSubscriptionPolicyList(subscriptionPolicies, resultSet);

        } catch (SQLException e) {
            log.error("Error in loading Subscription policies : ", e);
        }

        return subscriptionPolicies;
    }

    private void populateSubscriptionPolicyList(List<SubscriptionPolicy> subscriptionPolicies, ResultSet resultSet)
            throws SQLException {

        if (subscriptionPolicies != null && resultSet != null) {
            while (resultSet.next()) {
                SubscriptionPolicy subscriptionPolicyDTO = new SubscriptionPolicy();

                subscriptionPolicyDTO.setId(resultSet.getInt("POLICY_ID"));
                subscriptionPolicyDTO.setName(resultSet.getString("POLICY_NAME"));
                subscriptionPolicyDTO.setQuotaType(resultSet.getString("QUOTA_TYPE"));
                subscriptionPolicyDTO.setTenantId(resultSet.getInt("TENANT_ID"));

                subscriptionPolicyDTO.setRateLimitCount(resultSet.getInt("RATE_LIMIT_COUNT"));
                subscriptionPolicyDTO.setRateLimitTimeUnit(resultSet.getString("RATE_LIMIT_TIME_UNIT"));
                subscriptionPolicyDTO.setStopOnQuotaReach(resultSet.getBoolean("STOP_ON_QUOTA_REACH"));
                subscriptionPolicyDTO.setGraphQLMaxDepth(resultSet.getInt("MAX_DEPTH"));
                subscriptionPolicyDTO.setGraphQLMaxComplexity(resultSet.getInt("MAX_COMPLEXITY"));

                subscriptionPolicies.add(subscriptionPolicyDTO);
            }
        }
    }

    /*
     * This method can be used to retrieve all the ApplicationPolicys in the database
     *
     * @return {@link List<ApplicationPolicy>}
     * */
    public List<ApplicationPolicy> getAllApplicationPolicies() {

        List<ApplicationPolicy> applicationPolicies = new ArrayList<>();
        try (
                Connection conn = APIMgtDBUtil.getConnection();
                PreparedStatement ps =
                        conn.prepareStatement(SubscriptionValidationSQLConstants.GET_ALL_APPLICATION_POLICIES_SQL);
                ResultSet resultSet = ps.executeQuery();
        ) {
            populateApplicationPolicyList(applicationPolicies, resultSet);

        } catch (SQLException e) {
            log.error("Error in loading application policies : ", e);
        }

        return applicationPolicies;
    }

    /*
     * This method can be used to retrieve all the ApplicationPolicys in the database
     *
     * @return {@link List<ApplicationPolicy>}
     * */
    public List<APIPolicy> getAllApiPolicies() {

        List<APIPolicy> applicationPolicies = new ArrayList<>();
        try (
                Connection conn = APIMgtDBUtil.getConnection();
                PreparedStatement ps =
                        conn.prepareStatement(SubscriptionValidationSQLConstants.GET_ALL_APPLICATION_POLICIES_SQL);
                ResultSet resultSet = ps.executeQuery();
        ) {
            populateApiPolicyList(applicationPolicies, resultSet);

        } catch (SQLException e) {
            log.error("Error in loading application policies : ", e);
        }

        return applicationPolicies;
    }

    /*
     * This method can be used to retrieve all the APIs of a given tenant in the database
     *
     * @param tenantId : unique identifier of tenant
     * @return {@link List<API>}
     * */
    public List<API> getAllApis(String tenantDomain) {
        String query;
        String contextSearchString;

        if (tenantDomain.equalsIgnoreCase(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            query = SubscriptionValidationSQLConstants.GET_ST_APIS_SQL;
            contextSearchString = APIConstants.TENANT_PREFIX + "%";
        } else {
            query = SubscriptionValidationSQLConstants.GET_TENANT_APIS_SQL;
            contextSearchString = APIConstants.TENANT_PREFIX + tenantDomain + "%";
        }

        List<API> apiList = new ArrayList<>();
        try (Connection conn = APIMgtDBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, contextSearchString);
            ps.setString(2, contextSearchString);

            try (ResultSet resultSet = ps.executeQuery()) {
                populateAPIList(resultSet, apiList);
            }

        } catch (SQLException e) {
            log.error("Error in loading Apis for tenantId : " + tenantDomain, e);
        }

        return apiList;
    }

    /*
     * This method can be used to retrieve an API in the database
     *
     * @param apiId : unique identifier of an API
     * @return {@link API}
     * */
    public API getApi(String version, String context) {

        API api = null;
        try (Connection conn = APIMgtDBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SubscriptionValidationSQLConstants.GET_API_SQL + " UNION "
                        + SubscriptionValidationSQLConstants.GET_API_PRODUCT_SQL)) {
            ps.setString(1, version);
            ps.setString(2, context);
            ps.setString(3, version);
            ps.setString(4, context);

            try (ResultSet resultSet = ps.executeQuery()) {
                Map<Integer, API> temp = new ConcurrentHashMap<>();
                while (resultSet.next()) {
                    int apiId = resultSet.getInt("API_ID");
                    api = temp.get(apiId);
                    if (api == null) {
                        api = new API();
                        api.setApiId(apiId);
                        api.setProvider(resultSet.getString("API_PROVIDER"));
                        api.setName(resultSet.getString("API_NAME"));
                        api.setPolicy(resultSet.getString("API_TIER"));
                        String apiVersionFromDB = resultSet.getString("API_VERSION");
                        api.setVersion(apiVersionFromDB);
                        api.setContext(resultSet.getString("CONTEXT"));
                        api.setApiType(resultSet.getString("API_TYPE"));
                        String publishedDefaultVersion = resultSet.getString("PUBLISHED_DEFAULT_API_VERSION");
                        if (apiVersionFromDB != null) {
                            api.setIsDefaultVersion(apiVersionFromDB.equals(publishedDefaultVersion));
                        }
                        temp.put(apiId, api);
                    }
                    String urlPattern = resultSet.getString("URL_PATTERN");
                    String httpMethod = resultSet.getString("HTTP_METHOD");
                    URLMapping urlMapping = api.getResource(urlPattern, httpMethod);
                    if (urlMapping == null) {
                        urlMapping = new URLMapping();
                        urlMapping.setThrottlingPolicy(resultSet.getString("RES_TIER"));
                        urlMapping.setAuthScheme(resultSet.getString("AUTH_SCHEME"));
                        urlMapping.setHttpMethod(httpMethod);
                        urlMapping.setUrlPattern(urlPattern);
                        api.addResource(urlMapping);
                    }
                    String scopeName = resultSet.getString("SCOPE_NAME");
                    if (StringUtils.isNotEmpty(scopeName)) {
                        urlMapping.addScope(scopeName);
                    }
                }
            }

        } catch (SQLException e) {
            log.error("Error in loading API for api : " + context + " : " + version, e);
        }

        return api;
    }

    private void populateAPIList(ResultSet resultSet, List<API> apiList) throws SQLException {

        Map<Integer, API> temp = new ConcurrentHashMap<>();
        Map<Integer, URLMapping> tempUrls = new ConcurrentHashMap<>();
        while (resultSet.next()) {
            int apiId = resultSet.getInt("API_ID");
            String apiType = resultSet.getString("API_TYPE");
            API api = temp.get(apiId);
            if (api == null) {
                api = new API();
                api.setApiId(apiId);
                api.setProvider(resultSet.getString("API_PROVIDER"));
                api.setName(resultSet.getString("API_NAME"));
                api.setPolicy(resultSet.getString("API_TIER"));
                String apiVersionFromDB = resultSet.getString("API_VERSION");
                api.setVersion(apiVersionFromDB);
                api.setContext(resultSet.getString("CONTEXT"));
                String publishedDefaultVersion = resultSet.getString("PUBLISHED_DEFAULT_API_VERSION");
                if (apiVersionFromDB != null) {
                    api.setIsDefaultVersion(apiVersionFromDB.equals(publishedDefaultVersion));
                }
                api.setApiType(apiType);
                temp.put(apiId, api);
                tempUrls = new ConcurrentHashMap<>();
                apiList.add(api);
            }
            createURLMapping(resultSet, tempUrls, api);
        }
    }

    private void createURLMapping(ResultSet resultSet, Map<Integer, URLMapping> tempUrls, API api) throws SQLException {

        int urlId = resultSet.getInt("URL_MAPPING_ID");
        URLMapping urlMapping;
        urlMapping = tempUrls.get(urlId);
        if (urlMapping == null) {
            urlMapping = new URLMapping();
            urlMapping.setHttpMethod(resultSet.getString("HTTP_METHOD"));
            urlMapping.setAuthScheme(resultSet.getString("AUTH_SCHEME"));
            urlMapping.setThrottlingPolicy(resultSet.getString("RES_TIER"));

            urlMapping.setUrlPattern(resultSet.getString("URL_PATTERN"));
            tempUrls.put(urlId, urlMapping);
            api.addResource(urlMapping);
        }
        String scopeName = resultSet.getString("SCOPE_NAME");
        if (StringUtils.isNotEmpty(scopeName)) {
            urlMapping.addScope(scopeName);
        }

    }
    /*
     * This method can be used to retrieve all the APIs of a given tesanat in the database
     *
     * @param subscriptionId : unique identifier of a subscription
     * @return {@link List<Subscription>}
     * */
    public List<Subscription> getAllSubscriptions(String tenantDomain) {

        List<Subscription> subscriptions = new ArrayList<>();
        try (Connection conn = APIMgtDBUtil.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(SubscriptionValidationSQLConstants.GET_TENANT_SUBSCRIPTIONS_SQL)) {
            int tenantId = 0;
            try {
                tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                        .getTenantId(tenantDomain);
            } catch (UserStoreException e) {
                log.error("Error in getting tenant id for loading Subscriptions for tenant : " + tenantDomain, e);
            }
            ps.setInt(1, tenantId);

            try (ResultSet resultSet = ps.executeQuery()) {
                populateSubscriptionsList(subscriptions, resultSet);
            }
        } catch (SQLException e) {
            log.error("Error in loading Subscriptions for tenantId : " + tenantDomain, e);
        }
        return subscriptions;
    }

    private void populateSubscriptionsList(List<Subscription> subscriptions, ResultSet resultSet) throws SQLException {

        if (resultSet != null && subscriptions != null) {
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
    }

    /*
     * This method can be used to retrieve all the Applications of a given tenant in the database
     * @param tenantId : tenant Id
     * @return {@link Subscription}
     * */
    public List<Application> getAllApplications(String tenantDomain) {

        ArrayList<Application> applications = new ArrayList<>();
        try (Connection conn = APIMgtDBUtil.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(SubscriptionValidationSQLConstants.GET_TENANT_APPLICATIONS_SQL)) {
            try {
                int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                        .getTenantId(tenantDomain);
                ps.setInt(1, tenantId);

                try (ResultSet resultSet = ps.executeQuery()) {
                    addToApplicationList(applications, resultSet);
                }
            } catch (UserStoreException e) {
                log.error("Error in getting tenant id for loading Applications for tenant : " + tenantDomain, e);
            }

        } catch (SQLException e) {
            log.error("Error in loading Applications for tenantDomain : " + tenantDomain, e);
        }

        return applications;
    }

    /*
     * @param subscriptionId : unique identifier of a subscription
     * @return {@link Subscription}
     * */
    public List<ApplicationKeyMapping> getAllApplicationKeyMappings(String tenantDomain) {

        List<ApplicationKeyMapping> keyMappings = new ArrayList<>();

        try (Connection conn = APIMgtDBUtil.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(SubscriptionValidationSQLConstants.GET_TENANT_AM_KEY_MAPPING_SQL)) {
            int tenantId = 0;
            try {
                tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                        .getTenantId(tenantDomain);
            } catch (UserStoreException e) {
                log.error("Error in loading ApplicationKeyMappings for tenantDomain : " + tenantDomain, e);
            }
            ps.setInt(1, tenantId);
            ps.setInt(2, tenantId);

            try (ResultSet resultSet = ps.executeQuery()) {
                populateApplicationKeyMappingsList(keyMappings, resultSet);
            }
        } catch (SQLException e) {
            log.error("Error in loading Application key mappings for tenantId : " + tenantDomain, e);
        }

        return keyMappings;
    }

    private void populateApplicationKeyMappingsList(List<ApplicationKeyMapping> keyMappings, ResultSet resultSet)
            throws SQLException {

        if (keyMappings != null && resultSet != null) {

            while (resultSet.next()) {
                String keyManagerName = resultSet.getString("KEY_MANAGER");
                ApplicationKeyMapping keyMapping = new ApplicationKeyMapping();
                keyMapping.setApplicationId(resultSet.getInt("APPLICATION_ID"));
                keyMapping.setConsumerKey(resultSet.getString("CONSUMER_KEY"));
                keyMapping.setKeyType(resultSet.getString("KEY_TYPE"));
                keyMapping.setKeyManager(keyManagerName);
                keyMappings.add(keyMapping);
            }

        }
    }

    /*
     * @param subscriptionId : unique identifier of a subscription
     * @return {@link Subscription}
     * */
    public List<SubscriptionPolicy> getAllSubscriptionPolicies(String tenantDomain) {

        ArrayList<SubscriptionPolicy> subscriptionPolicies = new ArrayList<>();
        try (Connection conn = APIMgtDBUtil.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(SubscriptionValidationSQLConstants.GET_TENANT_SUBSCRIPTION_POLICIES_SQL)) {
            int tenantId = 0;
            try {
                tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                        .getTenantId(tenantDomain);
            } catch (UserStoreException e) {
                log.error("Error in loading SubscriptionPolicies for tenantDomain : " + tenantDomain, e);
            }
            ps.setInt(1, tenantId);

            try (ResultSet resultSet = ps.executeQuery()) {
                populateSubscriptionPolicyList(subscriptionPolicies, resultSet);
            }

        } catch (SQLException e) {
            log.error("Error in loading Subscription Policies for tenanatId : " + tenantDomain, e);
        }

        return subscriptionPolicies;
    }

    /*
     * @param tenantDomain : tenant domain name
     * @return {@link List<ApplicationPolicy>}
     * */
    public List<ApplicationPolicy> getAllApplicationPolicies(String tenantDomain) {

        ArrayList<ApplicationPolicy> applicationPolicies = new ArrayList<>();
        try (Connection conn = APIMgtDBUtil.getConnection();
             PreparedStatement ps =
                        conn.prepareStatement(SubscriptionValidationSQLConstants.GET_TENANT_APPLICATION_POLICIES_SQL)) {
            int tenantId = 0;
            try {
                tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                        .getTenantId(tenantDomain);
            } catch (UserStoreException e) {
                log.error("Error in loading ApplicationPolicies for tenantDomain : " + tenantDomain, e);
            }
            ps.setInt(1, tenantId);

            try (ResultSet resultSet = ps.executeQuery()) {
                populateApplicationPolicyList(applicationPolicies, resultSet);
            }

        } catch (SQLException e) {
            log.error("Error in loading application policies for tenantId : " + tenantDomain, e);
        }

        return applicationPolicies;
    }

    private void populateApplicationPolicyList(List<ApplicationPolicy> applicationPolicies, ResultSet resultSet)
            throws SQLException {

        if (applicationPolicies != null && resultSet != null) {
            while (resultSet.next()) {
                ApplicationPolicy applicationPolicyDTO = new ApplicationPolicy();
                applicationPolicyDTO.setId(resultSet.getInt("POLICY_ID"));
                applicationPolicyDTO.setName(resultSet.getString("NAME"));
                applicationPolicyDTO.setQuotaType(resultSet.getString("QUOTA_TYPE"));
                applicationPolicyDTO.setTenantId(resultSet.getInt("TENANT_ID"));
                applicationPolicies.add(applicationPolicyDTO);
            }
        }
    }

    /*
     * @param tenantDomain : tenant domain name
     * @return {@link List<APIPolicy>}
     * */
    public List<APIPolicy> getAllApiPolicies(String tenantDomain) {

        ArrayList<APIPolicy> apiPolicies = new ArrayList<>();
        try (Connection conn = APIMgtDBUtil.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(SubscriptionValidationSQLConstants.GET_TENANT_API_POLICIES_SQL)) {
            int tenantId = 0;
            try {
                tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                        .getTenantId(tenantDomain);
            } catch (UserStoreException e) {
                log.error("Error in loading ApplicationPolicies for tenantDomain : " + tenantDomain, e);
            }
            ps.setInt(1, tenantId);

            try (ResultSet resultSet = ps.executeQuery()) {
                populateApiPolicyList(apiPolicies, resultSet);
            }

        } catch (SQLException e) {
            log.error("Error in loading api policies for tenantId : " + tenantDomain, e);
        }

        return apiPolicies;
    }

    private void populateApiPolicyList(List<APIPolicy> apiPolicies, ResultSet resultSet)
            throws SQLException {

        Map<Integer, APIPolicy> temp = new ConcurrentHashMap<>();
        if (apiPolicies != null && resultSet != null) {
            while (resultSet.next()) {
                int policyId = resultSet.getInt("POLICY_ID");
                APIPolicy apiPolicy = temp.get(policyId);
                if (apiPolicy == null) {
                    apiPolicy = new APIPolicy();
                    apiPolicy.setId(policyId);
                    apiPolicy.setName(resultSet.getString("NAME"));
                    apiPolicy.setQuotaType(resultSet.getString("DEFAULT_QUOTA_TYPE"));
                    apiPolicy.setTenantId(resultSet.getInt("TENANT_ID"));
                    apiPolicy.setApplicableLevel(resultSet.getString("APPLICABLE_LEVEL"));
                    apiPolicies.add(apiPolicy);
                }
                APIPolicyConditionGroup apiPolicyConditionGroup = new APIPolicyConditionGroup();
                int conditionGroup = resultSet.getInt("CONDITION_GROUP_ID");
                apiPolicyConditionGroup.setConditionGroupId(conditionGroup);
                apiPolicyConditionGroup.setQuotaType(resultSet.getString("QUOTA_TYPE"));
                apiPolicyConditionGroup.setPolicyId(policyId);
                ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
                ConditionGroupDTO conditionGroupDTO = null;
                try {
                    conditionGroupDTO = apiMgtDAO.createConditionGroupDTO(conditionGroup);
                } catch (APIManagementException e) {
                    log.error("Error while processing api policies for policyId : " + policyId, e);
                }
                ConditionDTO[] conditionDTOS = conditionGroupDTO.getConditions();
                apiPolicyConditionGroup.setConditionDTOS(Arrays.asList(conditionDTOS));
                apiPolicy.addConditionGroup(apiPolicyConditionGroup);
                temp.put(policyId, apiPolicy);
            }
        }
    }

    /*
     * @param subscriptionId : unique identifier of a subscription
     * @return {@link Subscription}
     * */
    public Subscription getSubscription(int apiId, int appId) {

        try (Connection conn = APIMgtDBUtil.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(SubscriptionValidationSQLConstants.GET_SUBSCRIPTION_SQL)) {
            ps.setInt(1, apiId);
            ps.setInt(2, appId);

            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next()) {
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
            log.error("Error in loading Subscription by apiId : " + apiId + " appId: " + appId, e);
        }
        return null;
    }

    /*
     * @param applicationId : unique identifier of an application
     * @return {@link List<Application>} a list with one element
     * */
    public List<Application> getApplicationById(int applicationId) {

        List<Application> applicationList = new ArrayList<>();

        try (Connection conn = APIMgtDBUtil.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(SubscriptionValidationSQLConstants.GET_APPLICATION_BY_ID_SQL)) {
            ps.setInt(1, applicationId);

            try (ResultSet resultSet = ps.executeQuery()) {
                addToApplicationList(applicationList, resultSet);
            }

        } catch (SQLException e) {
            log.error("Error in loading Application by applicationId : " + applicationId, e);
        }
        return applicationList;
    }

    /*
     * @param policyName : name of an application level throttling policy
     * @return {@link ApplicationPolicy}
     * */
    public ApplicationPolicy getApplicationPolicyByNameForTenant(String policyName, String tenantDomain) {

        try (Connection conn = APIMgtDBUtil.getConnection();
             PreparedStatement ps =
                        conn.prepareStatement(SubscriptionValidationSQLConstants.GET_APPLICATION_POLICY_SQL)) {
            int tenantId = 0;
            try {
                tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                        .getTenantId(tenantDomain);
            } catch (UserStoreException e) {
                log.error("Error in loading ApplicationPolicy for tenantDomain : " + tenantDomain, e);
            }
            ps.setString(1, policyName);
            ps.setInt(2, tenantId);

            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next()) {
                    ApplicationPolicy applicationPolicy = new ApplicationPolicy();

                    applicationPolicy.setId(resultSet.getInt("POLICY_ID"));
                    applicationPolicy.setName(resultSet.getString("NAME"));
                    applicationPolicy.setQuotaType(resultSet.getString("QUOTA_TYPE"));
                    applicationPolicy.setTenantId(resultSet.getInt("TENANT_ID"));

                    return applicationPolicy;
                }
            }

        } catch (SQLException e) {
            log.error("Error in loading application policies by policyId : " + policyName + " of " + policyName, e);
        }

        return null;
    }

    /*
     * @param policyName : name of an application level throttling policy
     * @return {@link ApplicationPolicy}
     * */
    public APIPolicy getApiPolicyByNameForTenant(String policyName, String tenantDomain) {
        APIPolicy policy = null;
        try (Connection conn = APIMgtDBUtil.getConnection();
             PreparedStatement ps =
                        conn.prepareStatement(SubscriptionValidationSQLConstants.GET_TENANT_API_POLICY_SQL)) {
            int tenantId = 0;
            try {
                tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                        .getTenantId(tenantDomain);
            } catch (UserStoreException e) {
                log.error("Error in loading ApplicationPolicy for tenantDomain : " + tenantDomain, e);
            }
            ps.setInt(1, tenantId);
            ps.setString(2, policyName);

            try (ResultSet resultSet = ps.executeQuery()) {
                List<APIPolicy> apiPolicies = new ArrayList<APIPolicy>();
                populateApiPolicyList(apiPolicies, resultSet);
                if (!apiPolicies.isEmpty()) {
                    policy = apiPolicies.get(0);
                }
            }

        } catch (SQLException e) {
            log.error("Error in loading application policies by policyId : " + policyName + " of " + policyName, e);
        }

        return policy;
    }

    /*
     * @param policyName : name of the subscription level throttling policy
     * @return {@link SubscriptionPolicy}
     * */
    public SubscriptionPolicy getSubscriptionPolicyByNameForTenant(String policyName, String tenantDomain) {

        if (StringUtils.isNotEmpty(policyName) && StringUtils.isNotEmpty(tenantDomain)) {
            try (Connection conn = APIMgtDBUtil.getConnection();
                 PreparedStatement ps =
                         conn.prepareStatement(SubscriptionValidationSQLConstants.GET_SUBSCRIPTION_POLICY_SQL)) {
                int tenantId = 0;
                try {
                    tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                            .getTenantId(tenantDomain);
                } catch (UserStoreException e) {
                    log.error("Error in loading ApplicationPolicy for tenantDomain : " + tenantDomain, e);
                }
                ps.setString(1, policyName);
                ps.setInt(2, tenantId);

                try (ResultSet resultSet = ps.executeQuery()) {
                    if (resultSet.next()) {
                        SubscriptionPolicy subscriptionPolicy = new SubscriptionPolicy();

                        subscriptionPolicy.setId(resultSet.getInt("POLICY_ID"));
                        subscriptionPolicy.setName(resultSet.getString("POLICY_NAME"));
                        subscriptionPolicy.setQuotaType(resultSet.getString("QUOTA_TYPE"));
                        subscriptionPolicy.setTenantId(resultSet.getInt("TENANT_ID"));

                        subscriptionPolicy.setRateLimitCount(resultSet.getInt("RATE_LIMIT_COUNT"));
                        subscriptionPolicy.setRateLimitTimeUnit(resultSet.getString("RATE_LIMIT_TIME_UNIT"));
                        subscriptionPolicy.setStopOnQuotaReach(resultSet.getBoolean("STOP_ON_QUOTA_REACH"));
                        subscriptionPolicy.setGraphQLMaxDepth(resultSet.getInt("MAX_DEPTH"));
                        subscriptionPolicy.setGraphQLMaxComplexity(resultSet.getInt("MAX_COMPLEXITY"));
                        return subscriptionPolicy;
                    }
                }

            } catch (SQLException e) {
                log.error("Error in retrieving Subscription policy by id : " + policyName + " for " + tenantDomain, e);
            }
        }
        return null;
    }

    /*
     * @param appId : ApplicationId
     * @param keyType : Type of the key ex: PRODUCTION
     * @return {@link ApplicationKeyMapping}
     *
     * */
    public ApplicationKeyMapping getApplicationKeyMapping(int appId, String keyType) {

        try (Connection conn = APIMgtDBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SubscriptionValidationSQLConstants.GET_AM_KEY_MAPPING_SQL)) {
            ps.setInt(1, appId);
            ps.setString(2, keyType);

            try (ResultSet resultSet = ps.executeQuery()) {
                while (resultSet.next()) {
                    ApplicationKeyMapping keyMapping = new ApplicationKeyMapping();
                    keyMapping.setApplicationId(resultSet.getInt("APPLICATION_ID"));
                    keyMapping.setConsumerKey(resultSet.getString("CONSUMER_KEY"));
                    keyMapping.setKeyType(resultSet.getString("KEY_TYPE"));
                    return keyMapping;
                }
            }

        } catch (SQLException e) {
            log.error("Error in loading  Application Key Mapping for appId : " + appId + " type : " + keyType, e);
        }
        return null;
    }

    /*
     * @param consumerKey : consumer key of an application
     * @param keymanager : key manager
     * @param tenantDomain : tenant domain
     * @return {@link ApplicationKeyMapping}
     *
     * */
    public ApplicationKeyMapping getApplicationKeyMapping(String consumerKey, String keymanager, String tenantDomain) {
        ApplicationKeyMapping keyMapping = new ApplicationKeyMapping();
        if (keymanager != null) {
            try (Connection conn = APIMgtDBUtil.getConnection();
                    PreparedStatement ps = conn.prepareStatement(
                            SubscriptionValidationSQLConstants.GET_AM_KEY_MAPPING_BY_CONSUMER_KEY_AND_KM_NAME_SQL)) {
                ps.setString(1, consumerKey);
                ps.setString(2, keymanager);
                ps.setString(3, tenantDomain);
                try (ResultSet resultSet = ps.executeQuery()) {
                    if (resultSet.next()) {
                        keyMapping.setApplicationId(resultSet.getInt("APPLICATION_ID"));
                        keyMapping.setConsumerKey(resultSet.getString("CONSUMER_KEY"));
                        keyMapping.setKeyType(resultSet.getString("KEY_TYPE"));
                        keyMapping.setKeyManager(resultSet.getString("KEY_MANAGER"));
                        return keyMapping;
                    } else {
                        try (PreparedStatement ps1 = conn.prepareStatement(
                                SubscriptionValidationSQLConstants.GET_AM_KEY_MAPPING_BY_CONSUMER_KEY_AND_KM_UUID_SQL)) {
                            ps1.setString(1, consumerKey);
                            ps1.setString(2, keymanager);
                            ps1.setString(3, tenantDomain);
                            try (ResultSet resultSet1 = ps1.executeQuery()) {
                                if (resultSet1.next()) {
                                    keyMapping.setApplicationId(resultSet1.getInt("APPLICATION_ID"));
                                    keyMapping.setConsumerKey(resultSet1.getString("CONSUMER_KEY"));
                                    keyMapping.setKeyType(resultSet1.getString("KEY_TYPE"));
                                    keyMapping.setKeyManager(resultSet1.getString("KEY_MANAGER"));
                                    return keyMapping;
                                }
                            }
                        }
                    }
                }

            } catch (SQLException e) {
                log.error("Error in loading Application Key Mapping for consumer key : " + consumerKey
                        + " and Key Manager " + keymanager + " for tenant domain " + tenantDomain, e);
            }
        } else {
            try (Connection conn = APIMgtDBUtil.getConnection();
                    PreparedStatement ps = conn.prepareStatement(
                            SubscriptionValidationSQLConstants.GET_AM_KEY_MAPPING_BY_CONSUMER_KEY_SQL)) {
                ps.setString(1, consumerKey);
                try (ResultSet resultSet = ps.executeQuery()) {
                    while (resultSet.next()) {
                        String keyManagerName = resultSet.getString("KEY_MANAGER");
                        ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
                        try {
                            KeyManagerConfigurationDTO keyManager = apiMgtDAO.
                                    getKeyManagerConfigurationByUUID(keyManagerName);
                            if (keyManager != null) {
                                keyManagerName = keyManager.getName();
                            }
                        } catch (APIManagementException e) {
                            log.error("Error in fetching Key manager: " + keyManagerName);
                        }
                        keyMapping.setApplicationId(resultSet.getInt("APPLICATION_ID"));
                        keyMapping.setConsumerKey(resultSet.getString("CONSUMER_KEY"));
                        keyMapping.setKeyType(resultSet.getString("KEY_TYPE"));
                        keyMapping.setKeyManager(keyManagerName);
                        return keyMapping;
                    }
                }
            } catch (SQLException e) {
                log.error("Error in loading Application Key Mapping for consumer key : " + consumerKey, e);
            }
        }
        return null;
    }

    /*
     * This method can be used to retrieve all the URLMappings in the database
     *
     * @return {@link List<URLMapping>}
     * */
    public List<URLMapping> getAllURLMappings() {

        List<URLMapping> urlMappings = new ArrayList<>();
        String sql = SubscriptionValidationSQLConstants.GET_ALL_API_URL_MAPPING_SQL;

        try (Connection conn = APIMgtDBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet resultSet = ps.executeQuery()) {

            while (resultSet.next()) {
                URLMapping urlMapping = new URLMapping();
                urlMapping.setAuthScheme(resultSet.getString("AUTH_SCHEME"));
                urlMapping.setHttpMethod(resultSet.getString("HTTP_METHOD"));
                urlMapping.setThrottlingPolicy(resultSet.getString("POLICY"));
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
    public List<URLMapping> getAllURLMappings(int tenantId) {

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

            try (ResultSet resultSet = ps.executeQuery()) {
                while (resultSet.next()) {
                    URLMapping urlMapping = new URLMapping();
                    urlMapping.setAuthScheme(resultSet.getString("AUTH_SCHEME"));
                    urlMapping.setHttpMethod(resultSet.getString("HTTP_METHOD"));
                    urlMapping.setThrottlingPolicy(resultSet.getString("POLICY"));
                    urlMappings.add(urlMapping);
                }
            }
        } catch (SQLException e) {
            log.error("Error in loading URLMappings for tenantId : " + tenantId, e);
        }

        return urlMappings;
    }

}

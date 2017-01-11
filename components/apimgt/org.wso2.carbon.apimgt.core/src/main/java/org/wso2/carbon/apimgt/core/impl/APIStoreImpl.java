/*
 *
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.apimgt.core.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIStore;
import org.wso2.carbon.apimgt.core.api.KeyManager;
import org.wso2.carbon.apimgt.core.dao.APISubscriptionDAO;
import org.wso2.carbon.apimgt.core.dao.ApiDAO;
import org.wso2.carbon.apimgt.core.dao.ApplicationDAO;
import org.wso2.carbon.apimgt.core.dao.PolicyDAO;
import org.wso2.carbon.apimgt.core.dao.TagDAO;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.exception.APIMgtResourceAlreadyExistsException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.exception.KeyManagementException;
import org.wso2.carbon.apimgt.core.factory.KeyManagerHolder;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.APIStatus;
import org.wso2.carbon.apimgt.core.models.AccessTokenInfo;
import org.wso2.carbon.apimgt.core.models.AccessTokenRequest;
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.core.models.OAuthAppRequest;
import org.wso2.carbon.apimgt.core.models.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.core.models.Subscription;
import org.wso2.carbon.apimgt.core.models.Tag;
import org.wso2.carbon.apimgt.core.models.policy.Policy;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.core.util.APIUtils;
import org.wso2.carbon.apimgt.core.util.ApplicationUtils;
import org.wso2.carbon.apimgt.core.util.KeyManagerConstants;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Implementation of API Store operations.
 */
public class APIStoreImpl extends AbstractAPIManager implements APIStore {

    private static final Logger log = LoggerFactory.getLogger(APIStoreImpl.class);
    private TagDAO tagDAO;

    public APIStoreImpl(String username, ApiDAO apiDAO, ApplicationDAO applicationDAO,
            APISubscriptionDAO apiSubscriptionDAO, PolicyDAO policyDAO, TagDAO tagDAO) {
        super(username, apiDAO, applicationDAO, apiSubscriptionDAO, policyDAO, new APILifeCycleManagerImpl());
        this.tagDAO = tagDAO;
    }

    @Override
    public List<API> getAllAPIsByStatus(int offset, int limit, String[] statuses)
            throws APIManagementException {
        List<API> apiResults = null;
        try {
            apiResults = getApiDAO().getAPIsByStatus(new ArrayList<>(Arrays.asList(statuses)));
        } catch (APIMgtDAOException e) {
            String errorMsg =
                    "Error occurred while fetching APIs for the given statuses - " + Arrays.toString(statuses);
            log.error(errorMsg, e);
            throw new APIMgtDAOException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        return apiResults;
    }

    @Override
    public Application getApplicationByName(String applicationName, String ownerId, String groupId)
            throws APIManagementException {
        Application application = null;
        try {
            application = getApplicationDAO().getApplicationByName(applicationName, ownerId);
        } catch (APIMgtDAOException e) {
            String errorMsg =
                    "Error occurred while fetching application for the given applicationName - " + applicationName
                            + " with groupId - " + groupId;
            log.error(errorMsg, e);
            throw new APIMgtDAOException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        return application;
    }

    @Override
    public List<Application> getApplications(String subscriber, String groupId)
            throws APIManagementException {
        List<Application> applicationList = null;
        try {
            applicationList = getApplicationDAO().getApplications(subscriber);
        } catch (APIMgtDAOException e) {
            String errorMsg = "Error occurred while fetching applications for the given subscriber - " + subscriber
                    + " with groupId - " + groupId;
            log.error(errorMsg, e);
            throw new APIMgtDAOException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        return applicationList;
    }

    @Override
    public void updateApplication(String uuid, Application application) throws APIManagementException {
        try {
            application.setId(uuid);
            application.setUpdatedUser(getUsername());
            application.setUpdatedTime(LocalDateTime.now());
            getApplicationDAO().updateApplication(uuid, application);
        } catch (APIMgtDAOException e) {
            String errorMsg = "Error occurred while updating the application - " + uuid;
            log.error(errorMsg, e);
            throw new APIMgtDAOException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
    }

    @Override
    public Map<String, Object> generateApplicationKeys(String userId, String applicationName, String applicationId,
            String tokenType, String callbackUrl, String[] allowedDomains, String validityTime, String tokenScope,
            String groupingId) throws APIManagementException {

        OAuthAppRequest oauthAppRequest = ApplicationUtils
                .createOauthAppRequest(applicationName, userId, callbackUrl, null); //for now tokenSope = null
        oauthAppRequest.getOAuthApplicationInfo().addParameter(KeyManagerConstants.VALIDITY_PERIOD, validityTime);
        oauthAppRequest.getOAuthApplicationInfo().addParameter(KeyManagerConstants.APP_KEY_TYPE, tokenType);
        KeyManager keyManager = KeyManagerHolder.getKeyManagerInstance();
        try {
            OAuthApplicationInfo oauthAppInfo = keyManager.createApplication(oauthAppRequest);
            Map<String, Object> keyDetails = new HashMap<>();
            AccessTokenRequest accessTokenRequest = null;

            if (oauthAppInfo != null) {
                APIUtils.logDebug("Successfully created OAuth application", log);
                keyDetails.put(KeyManagerConstants.KeyDetails.CONSUMER_KEY, oauthAppInfo.getClientId());
                keyDetails.put(KeyManagerConstants.KeyDetails.CONSUMER_SECRET, oauthAppInfo.getClientSecret());
                keyDetails.put(KeyManagerConstants.KeyDetails.SUPPORTED_GRANT_TYPES, oauthAppInfo.getGrantTypes());
                keyDetails.put(KeyManagerConstants.KeyDetails.APP_DETAILS, oauthAppInfo.getJSONString());
            } else {
                throw new KeyManagementException("Error occurred while creating OAuth application");
            }
            accessTokenRequest = ApplicationUtils.createAccessTokenRequest(oauthAppInfo);
            AccessTokenInfo accessTokenInfo = keyManager.getNewApplicationAccessToken(accessTokenRequest);
            // adding access token information with key details
            if (accessTokenInfo != null) {
                APIUtils.logDebug("Successfully created OAuth access token", log);
                keyDetails.put(KeyManagerConstants.KeyDetails.ACCESS_TOKEN, accessTokenInfo.getAccessToken());
                keyDetails.put(KeyManagerConstants.KeyDetails.VALIDITY_TIME, accessTokenInfo.getValidityPeriod());
            } else {
                throw new KeyManagementException("Error occurred while generating access token for OAuth application");
            }

            //todo: temporarily saving to db. later this has to be done via workflow
            try {
                getApplicationDAO().addApplicationKeys(applicationId, oauthAppInfo);
            } catch (APIMgtDAOException e) {
                String errorMsg = "Error occurred while saving key data - " + applicationId;
                log.error(errorMsg, e);
                throw new APIMgtDAOException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
            }
            return keyDetails;
        } catch (KeyManagementException e) {
            String errorMsg = "Error occurred while generating OAuth keys for application - ";
            log.error(errorMsg, e);
            throw new KeyManagementException(errorMsg, e, ExceptionCodes.OAUTH2_APP_CREATION_FAILED);
        }
    }

    @Override
    public Application getApplicationByUuid(String uuid) throws APIManagementException {
        Application application = null;
        try {
            application = getApplicationDAO().getApplication(uuid);
        } catch (APIMgtDAOException e) {
            String errorMsg = "Error occurred while retrieving application - " + uuid;
            log.error(errorMsg, e);
            throw new APIMgtDAOException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }

        return application;
    }

    @Override
    public List<Subscription> getAPISubscriptionsByApplication(Application application)
            throws APIManagementException {
        List<Subscription> subscriptionsList = null;
        try {
            subscriptionsList = getApiSubscriptionDAO().getAPISubscriptionsByApplication(application.getId());
        } catch (APIMgtDAOException e) {
            String errorMsg =
                    "Error occurred while retrieving subscriptions for application - " + application.getName();
            log.error(errorMsg, e);
            throw new APIMgtDAOException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }

        return subscriptionsList;
    }

    @Override
    public String addApiSubscription(String apiId, String applicationId, String tier)
            throws APIManagementException {
        // Generate UUID for application
        String subscriptionId = UUID.randomUUID().toString();
        try {
            getApiSubscriptionDAO().addAPISubscription(subscriptionId, apiId, applicationId, tier,
                    APIMgtConstants.SubscriptionStatus.ACTIVE);
        } catch (APIMgtDAOException e) {
            String errorMsg = "Error occurred while adding api subscription for api - " + apiId;
            log.error(errorMsg, e);
            throw new APIMgtDAOException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        return subscriptionId;
    }

    @Override public void deleteAPISubscription(String subscriptionId) throws APIMgtDAOException {
        try {
            getApiSubscriptionDAO().deleteAPISubscription(subscriptionId);
        } catch (APIMgtDAOException e) {
            String errorMsg = "Error occurred while deleting api subscription - " + subscriptionId;
            log.error(errorMsg, e);
            throw new APIMgtDAOException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
    }

    @Override
    public List<Tag> getAllTags() throws APIManagementException {
        List<Tag> tagList;
        try {
            tagList = getTagDAO().getTags();
        } catch (APIMgtDAOException e) {
            String errorMsg = "Error occurred while retrieving tags";
            log.error(errorMsg, e);
            throw new APIMgtDAOException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        return tagList;
    }

    @Override
    public List<Policy> getPolicies(String policyLevel) throws APIManagementException {
        List<Policy> policyList = null;
        try {
            policyList = getPolicyDAO().getPolicies(policyLevel);
        } catch (APIMgtDAOException e) {
            String errorMsg = "Error occurred while retrieving policies for policy level - " + policyLevel;
            log.error(errorMsg, e);
            throw new APIMgtDAOException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        return policyList;
    }

    @Override
    public Policy getPolicy(String policyLevel, String policyName) throws APIManagementException {
        Policy policy = null;
        try {
            policy = getPolicyDAO().getPolicy(policyLevel, policyName);
        } catch (APIMgtDAOException e) {
            String errorMsg = "Error occurred while retrieving policy - " + policyName;
            log.error(errorMsg, e);
            throw new APIMgtDAOException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        return policy;
    }

    @Override
    public List<API> searchAPIs(String query, int offset, int limit) throws APIManagementException {

        List<API> apiResults = null;
        try {
            //TODO: Need to validate users roles against results returned
            if (query != null && !query.isEmpty()) {
                apiResults = getApiDAO().searchAPIs(query);
            } else {
                List<String> statuses = new ArrayList<>();
                statuses.add(APIStatus.);
                statuses.add(APIMgtConstants.API_PROTOTYPED);
                apiResults = getApiDAO().getAPIsByStatus(statuses);
            }
        } catch (APIMgtDAOException e) {
            String errorMsg = "Error occurred while updating searching APIs - " + query;
            log.error(errorMsg, e);
            throw new APIMgtDAOException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }

        return apiResults;
    }

    @Override
    public void deleteApplication(String appId) throws APIManagementException {
        try {
            getApplicationDAO().deleteApplication(appId);
        } catch (APIMgtDAOException e) {
            String errorMsg = "Error occurred while deleting the application - " + appId;
            log.error(errorMsg, e);
            throw new APIMgtDAOException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
    }

    @Override
    public String addApplication(Application application) throws APIManagementException {
        String applicationUuid = null;
        try {
            if (getApplicationDAO().isApplicationNameExists(application.getName())) {
                String message =  "An application already exists with a duplicate name - " + application.getName();
                log.error(message);
                throw new APIMgtResourceAlreadyExistsException(message, ExceptionCodes.APPLICATION_ALREADY_EXISTS);
            }
            //Tier validation
            String tierName = application.getTier();
            if (tierName == null) {
                String message =  "Tier name cannot be null - " + application.getName();
                log.error(message);
                throw new APIManagementException(message, ExceptionCodes.TIER_CANNOT_BE_NULL);
            } else {
                Policy policy = getPolicyDAO()
                        .getPolicy(APIMgtConstants.ThrottlePolicyConstants.APPLICATION_LEVEL, tierName);
                if (policy == null) {
                    String message = "Specified tier " + tierName + " is invalid";
                    log.error(message);
                    throw new APIManagementException(message, ExceptionCodes.TIER_CANNOT_BE_NULL);
                }
            }
            // Generate UUID for application
            String generatedUuid = UUID.randomUUID().toString();
            application.setId(generatedUuid);

            application.setCreatedTime(LocalDateTime.now());
            getApplicationDAO().addApplication(application);
            APIUtils.logDebug("successfully added application with appId " + application.getId(), log);
            applicationUuid = application.getId();
        } catch (APIMgtDAOException e) {
            String errorMsg = "Error occurred while creating the application - " + application.getName();
            log.error(errorMsg, e);
            throw new APIMgtDAOException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        return applicationUuid;
        //// TODO: 16/11/16 Workflow related implementation has to be done 
    }

    private TagDAO getTagDAO() {
        return tagDAO;
    }
}

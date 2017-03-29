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

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIMObservable;
import org.wso2.carbon.apimgt.core.api.APIStore;
import org.wso2.carbon.apimgt.core.api.EventObserver;
import org.wso2.carbon.apimgt.core.api.KeyManager;
import org.wso2.carbon.apimgt.core.api.WorkflowExecutor;
import org.wso2.carbon.apimgt.core.api.WorkflowResponse;
import org.wso2.carbon.apimgt.core.dao.APISubscriptionDAO;
import org.wso2.carbon.apimgt.core.dao.ApiDAO;
import org.wso2.carbon.apimgt.core.dao.ApplicationDAO;
import org.wso2.carbon.apimgt.core.dao.LabelDAO;
import org.wso2.carbon.apimgt.core.dao.PolicyDAO;
import org.wso2.carbon.apimgt.core.dao.TagDAO;
import org.wso2.carbon.apimgt.core.dao.WorkflowDAO;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.exception.APIMgtResourceAlreadyExistsException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.exception.KeyManagementException;
import org.wso2.carbon.apimgt.core.exception.WorkflowException;
import org.wso2.carbon.apimgt.core.factory.KeyManagerHolder;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.APIStatus;
import org.wso2.carbon.apimgt.core.models.AccessTokenInfo;
import org.wso2.carbon.apimgt.core.models.AccessTokenRequest;
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.core.models.ApplicationCreationResponse;
import org.wso2.carbon.apimgt.core.models.ApplicationCreationWorkflow;
import org.wso2.carbon.apimgt.core.models.Comment;
import org.wso2.carbon.apimgt.core.models.Event;
import org.wso2.carbon.apimgt.core.models.Label;
import org.wso2.carbon.apimgt.core.models.OAuthAppRequest;
import org.wso2.carbon.apimgt.core.models.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.core.models.Rating;
import org.wso2.carbon.apimgt.core.models.Subscription;
import org.wso2.carbon.apimgt.core.models.SubscriptionResponse;
import org.wso2.carbon.apimgt.core.models.SubscriptionWorkflow;
import org.wso2.carbon.apimgt.core.models.Tag;
import org.wso2.carbon.apimgt.core.models.Workflow;
import org.wso2.carbon.apimgt.core.models.WorkflowStatus;
import org.wso2.carbon.apimgt.core.models.policy.Policy;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants.SubscriptionStatus;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants.WorkflowConstants;
import org.wso2.carbon.apimgt.core.util.APIUtils;
import org.wso2.carbon.apimgt.core.util.ApplicationUtils;
import org.wso2.carbon.apimgt.core.util.KeyManagerConstants;
import org.wso2.carbon.apimgt.core.workflow.WorkflowExecutorFactory;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Implementation of API Store operations.
 */
public class APIStoreImpl extends AbstractAPIManager implements APIStore, APIMObservable {

    // Map to store observers, which observe APIStore events
    private Map<String, EventObserver> eventObservers = new HashMap<>();

    private static final Logger log = LoggerFactory.getLogger(APIStoreImpl.class);
    private TagDAO tagDAO;

    /**
     * Constructor.
     *
     * @param username   Logged in user's username
     * @param apiDAO  API Data Access Object
     * @param applicationDAO  Application Data Access Object
     * @param apiSubscriptionDAO   API Subscription Data Access Object
     * @param policyDAO Policy Data Access Object
     * @param tagDAO Tag Data Access Object
     * @param labelDAO Label Data Access Object
     * @param workflowDAO WorkFlow Data Access Object
     */
    public APIStoreImpl(String username, ApiDAO apiDAO, ApplicationDAO applicationDAO,
            APISubscriptionDAO apiSubscriptionDAO, PolicyDAO policyDAO, TagDAO tagDAO, LabelDAO labelDAO,
            WorkflowDAO workflowDAO) {
        super(username, apiDAO, applicationDAO, apiSubscriptionDAO, policyDAO, new APILifeCycleManagerImpl(), labelDAO,
                workflowDAO);
        this.tagDAO = tagDAO;
    }

    @Override
    public List<API> getAllAPIsByStatus(int offset, int limit, String[] statuses) throws APIManagementException {
        List<API> apiResults = null;
        try {
            apiResults = getApiDAO().getAPIsByStatus(new ArrayList<>(Arrays.asList(statuses)));
        } catch (APIMgtDAOException e) {
            String errorMsg = "Error occurred while fetching APIs for the given statuses - "
                    + Arrays.toString(statuses);
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
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
            String errorMsg = "Error occurred while fetching application for the given applicationName - "
                    + applicationName + " with groupId - " + groupId;
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        return application;
    }

    @Override
    public List<Application> getApplications(String subscriber, String groupId) throws APIManagementException {
        List<Application> applicationList = null;
        try {
            applicationList = getApplicationDAO().getApplications(subscriber);
        } catch (APIMgtDAOException e) {
            String errorMsg = "Error occurred while fetching applications for the given subscriber - " + subscriber
                    + " with groupId - " + groupId;
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
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
            throw new APIManagementException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
    }

    @Override
    public Map<String, Object> generateApplicationKeys(String userId, String applicationName, String applicationId,
            String tokenType, String callbackUrl, String[] allowedDomains, String validityTime, String tokenScope,
            String groupingId) throws APIManagementException {

        OAuthAppRequest oauthAppRequest = ApplicationUtils.createOauthAppRequest(applicationName, userId, callbackUrl,
                null); // for now tokenSope = null
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

            // todo: temporarily saving to db. later this has to be done via workflow
            try {
                getApplicationDAO().addApplicationKeys(applicationId, oauthAppInfo);
            } catch (APIMgtDAOException e) {
                String errorMsg = "Error occurred while saving key data - " + applicationId;
                log.error(errorMsg, e);
                throw new APIManagementException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
            }
            return keyDetails;
        } catch (KeyManagementException e) {
            String errorMsg = "Error occurred while generating OAuth keys for application - ";
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, ExceptionCodes.OAUTH2_APP_CREATION_FAILED);
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
            throw new APIManagementException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }

        return application;
    }

    @Override
    public List<Subscription> getAPISubscriptionsByApplication(Application application) throws APIManagementException {
        List<Subscription> subscriptionsList = null;
        try {
            subscriptionsList = getApiSubscriptionDAO().getAPISubscriptionsByApplication(application.getId());
        } catch (APIMgtDAOException e) {
            String errorMsg = "Error occurred while retrieving subscriptions for application - "
                    + application.getName();
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }

        return subscriptionsList;
    }

    @Override
    public SubscriptionResponse addApiSubscription(String apiId, String applicationId, String tier)
            throws APIManagementException {

        SubscriptionResponse subScriptionResponse;
        // Generate UUID for application
        String subscriptionId = UUID.randomUUID().toString();
        try {
            API api = getAPIbyUUID(apiId);
            if (api == null) {
                String errorMsg = "Cannot find an API for given apiId - " + apiId;
                log.error(errorMsg);
                throw new APIManagementException(errorMsg, ExceptionCodes.APIMGT_DAO_EXCEPTION);
            }
            Application application = getApplicationByUuid(applicationId);

            if (application == null) {
                String errorMsg = "Cannot find an application for given applicationId - " + applicationId;
                log.error(errorMsg);
                throw new APIManagementException(errorMsg, ExceptionCodes.APIMGT_DAO_EXCEPTION);
            }
            getApiSubscriptionDAO().addAPISubscription(subscriptionId, apiId, applicationId, tier,
                    APIMgtConstants.SubscriptionStatus.ON_HOLD);

            WorkflowExecutor addSubscriptionWFExecutor = WorkflowExecutorFactory.getInstance()
                    .getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION);

            SubscriptionWorkflow workflow = new SubscriptionWorkflow();

            workflow.setCreatedTime(LocalDateTime.now());
            workflow.setExternalWorkflowReference(UUID.randomUUID().toString());
            workflow.setWorkflowReference(subscriptionId);
            workflow.setWorkflowType(WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION);
            workflow.setApiName(api.getName());
            workflow.setApiContext(api.getContext());
            workflow.setApiVersion(api.getVersion());
            workflow.setApiProvider(api.getProvider());
            workflow.setApiId(apiId);
            workflow.setTierName(tier);
            workflow.setApplicationName(application.getName());
            workflow.setApplicationId(applicationId);
            workflow.setSubscriber(getUsername());

            WorkflowResponse response = addSubscriptionWFExecutor.execute(workflow);
            workflow.setStatus(response.getWorkflowStatus());

            addWorkflowEntries(workflow);

            if (WorkflowStatus.APPROVED == response.getWorkflowStatus()) {
                completeWorkflow(addSubscriptionWFExecutor, workflow);
            }

            subScriptionResponse = new SubscriptionResponse(subscriptionId, response);

        } catch (APIMgtDAOException e) {
            String errorMsg = "Error occurred while adding api subscription for api - " + apiId;
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }

        return subScriptionResponse;
    }

    @Override
    public void deleteAPISubscription(String subscriptionId) throws APIManagementException {
        try {

            WorkflowExecutor createSubscriptionWFExecutor = WorkflowExecutorFactory.getInstance()
                    .getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION);
            WorkflowExecutor removeSubscriptionWFExecutor = WorkflowExecutorFactory.getInstance()
                    .getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_DELETION);

            // check for pending subscription creation
            if (subscriptionId == null) {
                String errorMsg = "Subscription Id is not provided";
                log.error(errorMsg);
                throw new APIManagementException(errorMsg, ExceptionCodes.PARAMETER_NOT_PROVIDED);
            }

            Subscription subscription = getApiSubscriptionDAO().getAPISubscription(subscriptionId);
            if (subscription == null) {
                String errorMsg = "Subscription not found for the id - " + subscriptionId;
                log.error(errorMsg);
                throw new APIManagementException(errorMsg, ExceptionCodes.SUBSCRIPTION_NOT_FOUND);
            } else {
                if (APIMgtConstants.SubscriptionStatus.ON_HOLD == subscription.getStatus()) {
                    String pendingRefForSubscription = getWorkflowDAO()
                            .getExternalWorkflowReferenceForSubscription(subscriptionId);
                    if (pendingRefForSubscription != null) {
                        try {
                            createSubscriptionWFExecutor.cleanUpPendingTask(pendingRefForSubscription);
                        } catch (WorkflowException e) {
                            String warn = "Failed to clean pending subscription approval task for " + subscriptionId;
                            // failed cleanup processes are ignored to prevent failing the deletion process
                            log.warn(warn, e.getLocalizedMessage());
                        }
                    }
                }

                SubscriptionWorkflow workflow = new SubscriptionWorkflow();
                workflow.setWorkflowReference(subscriptionId);
                workflow.setWorkflowType(WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_DELETION);
                workflow.setStatus(WorkflowStatus.CREATED);
                workflow.setCreatedTime(LocalDateTime.now());
                workflow.setExternalWorkflowReference(UUID.randomUUID().toString());

                workflow.setApiName(subscription.getApi().getName());
                workflow.setApiContext(subscription.getApi().getContext());
                workflow.setApiVersion(subscription.getApi().getVersion());
                workflow.setApiProvider(subscription.getApi().getProvider());

                workflow.setApiId(subscription.getApi().getId());
                workflow.setTierName(subscription.getSubscriptionTier());
                workflow.setApplicationName(subscription.getApplication().getName());
                workflow.setApplicationId(subscription.getApplication().getId());

                WorkflowResponse response = removeSubscriptionWFExecutor.execute(workflow);
                workflow.setStatus(response.getWorkflowStatus());

                addWorkflowEntries(workflow);

                if (WorkflowStatus.APPROVED == response.getWorkflowStatus()) {
                    completeWorkflow(removeSubscriptionWFExecutor, workflow);
                }
            }
        } catch (APIMgtDAOException e) {
            String errorMsg = "Error occurred while deleting api subscription - " + subscriptionId;
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
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
            throw new APIManagementException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
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
            throw new APIManagementException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
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
            throw new APIManagementException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        return policy;
    }

    @Override
    public List<Label> getLabelInfo(List<String> labels) throws APIManagementException {

        List<Label> labelList;
        try {
            labelList = getLabelDAO().getLabelsByName(labels);
        } catch (APIMgtDAOException e) {
            String errorMsg = "Error occurred while retrieving label information";
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        return labelList;
    }

    @Override
    public Comment getCommentByUUID(String commentId, String apiId) throws APIManagementException {
        Comment comment;
        try {
            comment = getApiDAO().getCommentByUUID(commentId, apiId);
        } catch (APIMgtDAOException e) {
            String errorMsg = "Error occurred while retrieving Comment + " + commentId + " for API " + apiId;
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
            return comment;
    }

    @Override
    public double getUserRating(String apiId, String username) throws APIManagementException {
        return 0;
    }

    @Override
    public double getAvgRating(String apiId) throws APIManagementException {
        return 0;
    }

    @Override
    public List<Rating> getUserRatingDTOList(String apiId) throws APIManagementException {
        return null;
    }

    @Override
    public List<API> searchAPIs(String query, int offset, int limit) throws APIManagementException {

        List<API> apiResults = null;

        try {

            // TODO: Need to validate users roles against results returned
            if (query != null && !query.isEmpty()) {
                String[] attributes = query.split(",");
                Map<String, String> attributeMap = new HashMap<>();
                List<String> roles = new ArrayList<>();
                String user = "admin";
                // TODO get the logged in user and user roles from key manager.
                boolean isFullTextSearch = false;
                for (String attribute : attributes) {
                    if (attribute.split(":").length > 1) {
                        attributeMap.put(attribute.split(":")[0], attribute.split(":")[1]);
                    } else if (attribute.contains(":") && attribute.split(":").length > 0) {
                        attributeMap.put(attribute.split(":")[0], "");
                    } else {
                        isFullTextSearch = true;
                    }

                }
                if (isFullTextSearch) {
                    apiResults = getApiDAO().searchAPIs(roles, user, query, offset, limit);
                } else {
                    apiResults = getApiDAO().attributeSearchAPIs(roles, user, attributeMap, offset, limit);
                }
            } else {
                List<String> statuses = new ArrayList<>();
                statuses.add(APIStatus.PUBLISHED.getStatus());
                statuses.add(APIStatus.PROTOTYPED.getStatus());
                apiResults = getApiDAO().getAPIsByStatus(statuses);
            }

        } catch (APIMgtDAOException e) {
            String errorMsg = "Error occurred while updating searching APIs - " + query;
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }

        return apiResults;
    }

    @Override
    public void deleteApplication(String appId) throws APIManagementException {
        try {
            if (appId == null) {
                String message = "Application Id is not provided";
                throw new APIManagementException(message, ExceptionCodes.PARAMETER_NOT_PROVIDED);
            }
            // get app info
            Application application = getApplicationDAO().getApplication(appId);
            if (application == null) {
                String message = "Application cannot be found for id :" + appId;
                throw new APIManagementException(message, ExceptionCodes.APPLICATION_NOT_FOUND);
            }           
            
            WorkflowExecutor createApplicationWFExecutor = WorkflowExecutorFactory.getInstance().
                    getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_APPLICATION_CREATION);
            WorkflowExecutor createSubscriptionWFExecutor = WorkflowExecutorFactory.getInstance().
                    getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION);  
            WorkflowExecutor removeApplicationWFExecutor = WorkflowExecutorFactory.getInstance().
                    getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_APPLICATION_DELETION);
            
            // get subscriptions with pending status
            List<Subscription> pendingSubscriptions = getApiSubscriptionDAO()
                    .getPendingAPISubscriptionsByApplication(appId);
            String pendingExtReference;
            if (pendingSubscriptions == null || pendingSubscriptions.isEmpty()) {
                pendingExtReference = getWorkflowDAO().getExternalWorkflowReferenceForApplication(appId);
                try {
                    createApplicationWFExecutor.cleanUpPendingTask(pendingExtReference);
                } catch (WorkflowException e) {
                    String warn = "Failed to clean pending application approval task for " + appId;
                    // failed cleanup processes are ignored to prevent failing the deletion process
                    log.warn(warn, e.getLocalizedMessage());
                }
            } else {

                // this means there are pending subsriptions. It also implies that there cannot be pending application
                // approvals (cannot subscribe to a pending application)
                for (Iterator iterator = pendingSubscriptions.iterator(); iterator.hasNext();) {
                    Subscription pendingSubscription = (Subscription) iterator.next();
                    pendingExtReference = getWorkflowDAO()
                            .getExternalWorkflowReferenceForSubscription(pendingSubscription.getId());
                    createSubscriptionWFExecutor.cleanUpPendingTask(pendingExtReference);

                    try {
                        createSubscriptionWFExecutor.cleanUpPendingTask(pendingExtReference);
                    } catch (WorkflowException e) {
                        String warn = "Failed to clean pending subscription approval task for "
                                + pendingSubscription.getId();
                        // failed cleanup processes are ignored to prevent failing the deletion process
                        log.warn(warn, e.getLocalizedMessage());
                    }
                }
            }
            
            ApplicationCreationWorkflow workflow = new ApplicationCreationWorkflow();
            workflow.setApplication(application);
            workflow.setWorkflowType(APIMgtConstants.WorkflowConstants.WF_TYPE_AM_APPLICATION_DELETION);
            workflow.setWorkflowReference(application.getId());
            workflow.setExternalWorkflowReference(UUID.randomUUID().toString());
            workflow.setCreatedTime(LocalDateTime.now());
            WorkflowResponse response = removeApplicationWFExecutor.execute(workflow);
            workflow.setStatus(response.getWorkflowStatus());
            addWorkflowEntries(workflow);

            if (WorkflowStatus.APPROVED == response.getWorkflowStatus()) {
                completeWorkflow(removeApplicationWFExecutor, workflow);
            }          
         
        } catch (APIMgtDAOException e) {
            String errorMsg = "Error occurred while deleting the application - " + appId;
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
    }

    @Override
    public ApplicationCreationResponse addApplication(Application application) throws APIManagementException {
        ApplicationCreationResponse applicationResponse = null;
        String applicationUuid = null;
        try {
            if (getApplicationDAO().isApplicationNameExists(application.getName())) {
                String message = "An application already exists with a duplicate name - " + application.getName();
                log.error(message);
                throw new APIMgtResourceAlreadyExistsException(message, ExceptionCodes.APPLICATION_ALREADY_EXISTS);
            }
            // Tier validation
            String tierName = application.getTier();
            if (tierName == null) {
                String message = "Tier name cannot be null - " + application.getName();
                log.error(message);
                throw new APIManagementException(message, ExceptionCodes.TIER_CANNOT_BE_NULL);
            } else {
                Policy policy = getPolicyDAO().getPolicy(APIMgtConstants.ThrottlePolicyConstants.APPLICATION_LEVEL,
                        tierName);
                if (policy == null) {
                    String message = "Specified tier " + tierName + " is invalid";
                    log.error(message);
                    throw new APIManagementException(message, ExceptionCodes.TIER_CANNOT_BE_NULL);
                }
            }
            // Generate UUID for application
            String generatedUuid = UUID.randomUUID().toString();
            application.setId(generatedUuid);

            String permissionString = application.getPermissionString();
            if (permissionString != null && !("").equals(permissionString)) {
                HashMap roleNamePermissionList;
                roleNamePermissionList = getAPIPermissionArray(permissionString);
                application.setPermissionMap(roleNamePermissionList);
            }

            application.setCreatedTime(LocalDateTime.now());
            getApplicationDAO().addApplication(application);

            WorkflowExecutor appCreationWFExecutor = WorkflowExecutorFactory.getInstance()
                    .getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_APPLICATION_CREATION);

            ApplicationCreationWorkflow workflow = new ApplicationCreationWorkflow();

            workflow.setApplication(application);
            workflow.setWorkflowReference(application.getId());
            workflow.setExternalWorkflowReference(UUID.randomUUID().toString());
            workflow.setCreatedTime(LocalDateTime.now());
            WorkflowResponse response = appCreationWFExecutor.execute(workflow);
            workflow.setStatus(response.getWorkflowStatus());
            addWorkflowEntries(workflow);

            if (WorkflowStatus.APPROVED == response.getWorkflowStatus()) {
                completeWorkflow(appCreationWFExecutor, workflow);
            } else {
                getApplicationDAO().updateApplicationState(generatedUuid,
                        APIMgtConstants.ApplicationStatus.APPLICATION_ONHOLD);
            }

            APIUtils.logDebug("successfully added application with appId " + application.getId(), log);

            applicationResponse = new ApplicationCreationResponse(application.getId(), response);
        } catch (APIMgtDAOException e) {
            String errorMsg = "Error occurred while creating the application - " + application.getName();
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        } catch (ParseException e) {
            String errorMsg = "Error occurred while parsing the permission json from swagger in application - "
                    + application.getName();
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, ExceptionCodes.SWAGGER_PARSE_EXCEPTION);
        } catch (WorkflowException e) {
            String errorMsg = "Error occurred in workflow";
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, ExceptionCodes.WORKFLOW_EXCEPTION);
        }
        return applicationResponse;
    }

    /**
     * This method will return map with role names and its permission values.
     *
     * @param permissionJsonString
     * @return
     * @throws org.json.simple.parser.ParseException
     */
    private HashMap getAPIPermissionArray(String permissionJsonString) throws ParseException {

        HashMap roleNamePermissionList = new HashMap();
        JSONParser jsonParser = new JSONParser();

        JSONArray baseJsonArray = (JSONArray) jsonParser.parse(permissionJsonString);
        for (int i = 0; i < baseJsonArray.size(); i++) {
            JSONObject jsonObject = (JSONObject) baseJsonArray.get(i);
            String groupId = jsonObject.get(APIMgtConstants.Permission.GROUP_ID).toString();
            JSONArray subJsonArray = (JSONArray) jsonObject.get(APIMgtConstants.Permission.PERMISSION);
            int totalPermissionValue = 0;
            for (int j = 0; j < subJsonArray.size(); j++) {
                if (APIMgtConstants.Permission.READ.equals(subJsonArray.get(j).toString().trim())) {
                    totalPermissionValue += APIMgtConstants.Permission.READ_PERMISSION;
                } else if (APIMgtConstants.Permission.UPDATE.equals(subJsonArray.get(j).toString().trim())) {
                    totalPermissionValue += APIMgtConstants.Permission.UPDATE_PERMISSION;
                } else if (APIMgtConstants.Permission.DELETE.equals(subJsonArray.get(j).toString().trim())) {
                    totalPermissionValue += APIMgtConstants.Permission.DELETE_PERMISSION;
                } else if (APIMgtConstants.Permission.SUBSCRIPTION.equals(subJsonArray.get(j).toString().trim())) {
                    totalPermissionValue += APIMgtConstants.Permission.SUBSCRIBE_PERMISSION;
                }
            }
            roleNamePermissionList.put(groupId, totalPermissionValue);
        }

        return roleNamePermissionList;

    }

    private TagDAO getTagDAO() {
        return tagDAO;
    }

    /**
     * Add {@link org.wso2.carbon.apimgt.core.api.EventObserver} which needs to be registered to a Map.
     * Key should be class name of the observer. This is to prevent registering same observer twice to an
     * observable.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public void registerObserver(EventObserver observer) {
        if (observer != null && !eventObservers.containsKey(observer.getClass().getName())) {
            eventObservers.put(observer.getClass().getName(), observer);
        }
    }

    /**
     * Notify each registered {@link org.wso2.carbon.apimgt.core.api.EventObserver}.
     * This calls
     * {@link org.wso2.carbon.apimgt.core.api.EventObserver#captureEvent(Event, String, ZonedDateTime, Map)}
     * method of that {@link org.wso2.carbon.apimgt.core.api.EventObserver}.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public void notifyObservers(Event event, String username, ZonedDateTime eventTime, Map<String, String> metaData) {

        Set<Map.Entry<String, EventObserver>> eventObserverEntrySet = eventObservers.entrySet();
        eventObserverEntrySet.forEach(
                eventObserverEntry -> eventObserverEntry.getValue().captureEvent(event, username, eventTime, metaData));
    }

    /**
     * Remove {@link org.wso2.carbon.apimgt.core.api.EventObserver} from the Map, which stores observers to be
     * notified.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public void removeObserver(EventObserver observer) {
        if (observer != null) {
            eventObservers.remove(observer.getClass().getName());
        }
    }

    /**
     * To get the Map of all observers, which registered to {@link org.wso2.carbon.apimgt.core.api.APIPublisher}.
     *
     * @return Map of observers.
     */
    public Map<String, EventObserver> getEventObservers() {
        return eventObservers;
    }

    @Override
    public void completeWorkflow(WorkflowExecutor workflowExecutor, Workflow workflow) throws APIManagementException {

        if (workflow.getWorkflowReference() == null) {
            String message = "Error while changing the workflow. Missing reference";
            log.error(message);
            throw new APIManagementException(message, ExceptionCodes.WORKFLOW_EXCEPTION);
        }

        if (workflow instanceof ApplicationCreationWorkflow
                && WorkflowConstants.WF_TYPE_AM_APPLICATION_CREATION.equals(workflow.getWorkflowType())) {
            WorkflowResponse response = workflowExecutor.complete(workflow);

            // setting the workflow status from the one getting from the executor. this gives the executor developer
            // to change the state as well.
            workflow.setStatus(response.getWorkflowStatus());
            updateWorkflowEntries(workflow);
            String applicationState = "";
            if (WorkflowStatus.APPROVED == response.getWorkflowStatus()) {
                if (log.isDebugEnabled()) {
                    log.debug("Application Creation workflow complete: Approved");
                }
                applicationState = APIMgtConstants.ApplicationStatus.APPLICATION_APPROVED;

            } else if (WorkflowStatus.REJECTED == response.getWorkflowStatus()) {
                if (log.isDebugEnabled()) {
                    log.debug("Application Creation workflow complete: Rejected");
                }
                applicationState = APIMgtConstants.ApplicationStatus.APPLICATION_REJECTED;
            }
            getApplicationDAO().updateApplicationState(workflow.getWorkflowReference(), applicationState);

        } else if (workflow instanceof ApplicationCreationWorkflow
                && WorkflowConstants.WF_TYPE_AM_APPLICATION_DELETION.equals(workflow.getWorkflowType())) {
            WorkflowResponse response = workflowExecutor.complete(workflow);

            if (WorkflowStatus.APPROVED == response.getWorkflowStatus()) {
                if (log.isDebugEnabled()) {
                    log.debug("Application Deletion workflow complete: Approved");
                }
                getApplicationDAO().deleteApplication(workflow.getWorkflowReference());

            } else if (WorkflowStatus.REJECTED == response.getWorkflowStatus()) {
                if (log.isDebugEnabled()) {
                    log.debug("Subscription Creation workflow complete: Rejected");
                }
            }

        } else if (workflow instanceof SubscriptionWorkflow
                && WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION.equals(workflow.getWorkflowType())) {
            WorkflowResponse response = workflowExecutor.complete(workflow);
            SubscriptionStatus subscriptionState = null;
            if (WorkflowStatus.APPROVED == response.getWorkflowStatus()) {
                if (log.isDebugEnabled()) {
                    log.debug("Subscription Creation workflow complete: Approved");
                }

                subscriptionState = APIMgtConstants.SubscriptionStatus.ACTIVE;

            } else if (WorkflowStatus.REJECTED == response.getWorkflowStatus()) {
                if (log.isDebugEnabled()) {
                    log.debug("Subscription Creation workflow complete: Rejected");
                }
                subscriptionState = APIMgtConstants.SubscriptionStatus.REJECTED;
            }

            getApiSubscriptionDAO().updateSubscriptionStatus(workflow.getWorkflowReference(), subscriptionState);
        } else if (workflow instanceof SubscriptionWorkflow
                && WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_DELETION.equals(workflow.getWorkflowType())) {
            WorkflowResponse response = workflowExecutor.complete(workflow);
            if (WorkflowStatus.APPROVED == response.getWorkflowStatus()) {
                if (log.isDebugEnabled()) {
                    log.debug("Subscription deletion workflow complete: Approved");
                }
                getApiSubscriptionDAO().deleteAPISubscription(workflow.getWorkflowReference());               

            } else if (WorkflowStatus.REJECTED == response.getWorkflowStatus()) {
                if (log.isDebugEnabled()) {
                    log.debug("Subscription deletion workflow complete: Rejected");
                }
            }

        } else {
            String message = "Invalid workflow type:  " + workflow.getWorkflowType();
            log.error(message);
            throw new APIManagementException(message, ExceptionCodes.WORKFLOW_EXCEPTION);
        }
    }

    
}

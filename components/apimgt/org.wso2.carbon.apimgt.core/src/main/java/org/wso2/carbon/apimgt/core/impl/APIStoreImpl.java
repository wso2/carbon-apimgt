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

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.APIMConfigurations;
import org.wso2.carbon.apimgt.core.api.APIGateway;
import org.wso2.carbon.apimgt.core.api.APIMObservable;
import org.wso2.carbon.apimgt.core.api.APIStore;
import org.wso2.carbon.apimgt.core.api.EventObserver;
import org.wso2.carbon.apimgt.core.api.GatewaySourceGenerator;
import org.wso2.carbon.apimgt.core.api.KeyManager;
import org.wso2.carbon.apimgt.core.api.LabelExtractor;
import org.wso2.carbon.apimgt.core.api.WorkflowExecutor;
import org.wso2.carbon.apimgt.core.api.WorkflowResponse;
import org.wso2.carbon.apimgt.core.dao.APISubscriptionDAO;
import org.wso2.carbon.apimgt.core.dao.ApiDAO;
import org.wso2.carbon.apimgt.core.dao.ApiType;
import org.wso2.carbon.apimgt.core.dao.ApplicationDAO;
import org.wso2.carbon.apimgt.core.dao.LabelDAO;
import org.wso2.carbon.apimgt.core.dao.PolicyDAO;
import org.wso2.carbon.apimgt.core.dao.TagDAO;
import org.wso2.carbon.apimgt.core.dao.WorkflowDAO;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.exception.APIMgtResourceAlreadyExistsException;
import org.wso2.carbon.apimgt.core.exception.APIMgtResourceNotFoundException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.exception.GatewayException;
import org.wso2.carbon.apimgt.core.exception.KeyManagementException;
import org.wso2.carbon.apimgt.core.exception.LabelException;
import org.wso2.carbon.apimgt.core.exception.WorkflowException;
import org.wso2.carbon.apimgt.core.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.APIStatus;
import org.wso2.carbon.apimgt.core.models.AccessTokenInfo;
import org.wso2.carbon.apimgt.core.models.AccessTokenRequest;
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.core.models.ApplicationCreationResponse;
import org.wso2.carbon.apimgt.core.models.ApplicationCreationWorkflow;
import org.wso2.carbon.apimgt.core.models.ApplicationUpdateWorkflow;
import org.wso2.carbon.apimgt.core.models.Comment;
import org.wso2.carbon.apimgt.core.models.CorsConfiguration;
import org.wso2.carbon.apimgt.core.models.Event;
import org.wso2.carbon.apimgt.core.models.Label;
import org.wso2.carbon.apimgt.core.models.OAuthAppRequest;
import org.wso2.carbon.apimgt.core.models.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.core.models.Rating;
import org.wso2.carbon.apimgt.core.models.Subscription;
import org.wso2.carbon.apimgt.core.models.SubscriptionResponse;
import org.wso2.carbon.apimgt.core.models.SubscriptionWorkflow;
import org.wso2.carbon.apimgt.core.models.Tag;
import org.wso2.carbon.apimgt.core.models.UriTemplate;
import org.wso2.carbon.apimgt.core.models.Workflow;
import org.wso2.carbon.apimgt.core.models.WorkflowStatus;
import org.wso2.carbon.apimgt.core.models.policy.Policy;
import org.wso2.carbon.apimgt.core.template.APITemplateException;
import org.wso2.carbon.apimgt.core.template.dto.TemplateBuilderDTO;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants.ApplicationStatus;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants.SubscriptionStatus;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants.WorkflowConstants;
import org.wso2.carbon.apimgt.core.util.APIUtils;
import org.wso2.carbon.apimgt.core.util.ApplicationUtils;
import org.wso2.carbon.apimgt.core.util.KeyManagerConstants;
import org.wso2.carbon.apimgt.core.workflow.WorkflowExecutorFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
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
    private APIMConfigurations config;
    APIGateway gateway = getApiGateway();

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
                        WorkflowDAO workflowDAO, GatewaySourceGenerator
                                gatewaySourceGenerator, APIGateway apiGatewayPublisher) {
        super(username, apiDAO, applicationDAO, apiSubscriptionDAO, policyDAO, new APILifeCycleManagerImpl(), labelDAO,
                workflowDAO, gatewaySourceGenerator, apiGatewayPublisher);
        this.tagDAO = tagDAO;
        config = ServiceReferenceHolder.getInstance().getAPIMConfiguration();
    }

    @Override
    public List<API> getAllAPIsByStatus(int offset, int limit, String[] statuses) throws APIManagementException {
        List<API> apiResults = null;
        try {
            apiResults = getApiDAO().getAPIsByStatus(new ArrayList<>(Arrays.asList(statuses)), ApiType.STANDARD);
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

    /**
     * @see APIStore#updateApplication(String, Application)
     */
    @Override
    public WorkflowResponse updateApplication(String uuid, Application application) throws APIManagementException {
        try {
            //get old app 
            Application existingApplication = getApplicationDAO().getApplication(uuid);
            if (existingApplication != null) {
                WorkflowExecutor executor = WorkflowExecutorFactory.getInstance()
                        .getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_APPLICATION_UPDATE);
                ApplicationUpdateWorkflow workflow = new ApplicationUpdateWorkflow();
                
                
                application.setId(uuid);
                application.setUpdatedUser(getUsername());
                application.setUpdatedTime(LocalDateTime.now());
                
                workflow.setExistingApplication(existingApplication);
                workflow.setUpdatedApplication(application);
                workflow.setCreatedBy(getUsername());
                workflow.setWorkflowReference(application.getId());
                workflow.setExternalWorkflowReference(UUID.randomUUID().toString());
                workflow.setCreatedTime(LocalDateTime.now());
                
                String workflowDescription = "Update application from : " + existingApplication.toString()
                        + " to : " + application.toString();
                workflow.setWorkflowDescription(workflowDescription);
                
                //setting attributes for internal use. These are set to use from outside the executor's method
                //these will be saved in the AM_WORKFLOW table so these can be retrieved later for external wf approval
                //scenarios. this won't get stored for simple wfs
                workflow.setAttribute(WorkflowConstants.ATTRIBUTE_APPLICATION_NAME, application.getName());
                workflow.setAttribute(WorkflowConstants.ATTRIBUTE_APPLICATION_UPDATEDBY, application.getUpdatedUser());
                workflow.setAttribute(WorkflowConstants.ATTRIBUTE_APPLICATION_TIER, application.getTier());
                workflow.setAttribute(WorkflowConstants.ATTRIBUTE_APPLICATION_DESCRIPTION,
                        application.getDescription());
                workflow.setAttribute(WorkflowConstants.ATTRIBUTE_APPLICATION_CALLBACKURL,
                        application.getCallbackUrl());
                workflow.setAttribute(WorkflowConstants.ATTRIBUTE_APPLICATION_GROUPID, application.getGroupId());
                workflow.setAttribute(WorkflowConstants.ATTRIBUTE_APPLICATION_PERMISSION,
                        application.getPermissionString());  
                workflow.setAttribute(WorkflowConstants.ATTRIBUTE_APPLICATION_EXISTIN_APP_STATUS,
                        existingApplication.getStatus());

                WorkflowResponse response = executor.execute(workflow);
                workflow.setStatus(response.getWorkflowStatus());

                if (WorkflowStatus.CREATED != response.getWorkflowStatus()) {
                    completeWorkflow(executor, workflow);
                } else {
                    getApplicationDAO().updateApplicationState(uuid, ApplicationStatus.APPLICATION_ONHOLD);
                    addWorkflowEntries(workflow);
                }             
                return response;
            } else {
                String errorMsg = "Applicaiton does not exist - " + uuid;
                log.error(errorMsg);
                throw new APIManagementException(errorMsg, ExceptionCodes.APPLICATION_NOT_FOUND);
            }
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
        KeyManager keyManager = APIManagerFactory.getInstance().getKeyManager();
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
            //Instead of quering the db, we create same subscription object
            Subscription subscription = new Subscription(subscriptionId, application, api, tier);
            subscription.setStatus(APIMgtConstants.SubscriptionStatus.ON_HOLD);
            
            SubscriptionWorkflow workflow = new SubscriptionWorkflow();

            workflow.setCreatedTime(LocalDateTime.now());
            workflow.setExternalWorkflowReference(UUID.randomUUID().toString());
            workflow.setWorkflowReference(subscriptionId);
            workflow.setWorkflowType(WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION);
            workflow.setSubscription(subscription);
            workflow.setSubscriber(getUsername());

            String workflowDescription = "Subscription creation workflow for the subscription to api "
                    + subscription.getApi().getName() + ":" + subscription.getApi().getVersion() + ":"
                    + subscription.getApi().getProvider() + " using application "
                    + subscription.getApplication().getName() + " with tier " + subscription.getSubscriptionTier()
                    + " by " + getUsername();
            workflow.setWorkflowDescription(workflowDescription);

            WorkflowResponse response = addSubscriptionWFExecutor.execute(workflow);
            workflow.setStatus(response.getWorkflowStatus());            

            if (WorkflowStatus.CREATED != response.getWorkflowStatus()) {
                completeWorkflow(addSubscriptionWFExecutor, workflow);
            } else {
                //only add entry to workflow table if it is a pending task
                addWorkflowEntries(workflow);
            }

            subScriptionResponse = new SubscriptionResponse(subscriptionId, response);

        } catch (APIMgtDAOException e) {
            String errorMsg = "Error occurred while adding api subscription for api - " + apiId;
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }

        return subScriptionResponse;
    }

    /**
     * @see APIStore#deleteAPISubscription(String)
     */
    @Override
    public WorkflowResponse deleteAPISubscription(String subscriptionId) throws APIManagementException {
        try {

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
                //remove pending tasks for subscription creation first 
                cleanupPendingTaskForSubscriptionDeletion(subscription);
                
                SubscriptionWorkflow workflow = new SubscriptionWorkflow();
                workflow.setWorkflowReference(subscriptionId);
                workflow.setSubscription(subscription);
                workflow.setWorkflowType(WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_DELETION);
                workflow.setStatus(WorkflowStatus.CREATED);
                workflow.setCreatedTime(LocalDateTime.now());
                workflow.setExternalWorkflowReference(UUID.randomUUID().toString());        
                workflow.setSubscriber(getUsername());

                String workflowDescription = "Subscription deletion workflow for the subscription to api "
                        + subscription.getApi().getName() + ":" + subscription.getApi().getVersion() + ":"
                        + subscription.getApi().getProvider() + " using application "
                        + subscription.getApplication().getName() + " with tier " + subscription.getSubscriptionTier()
                        + " by " + getUsername();
                workflow.setWorkflowDescription(workflowDescription);
                
                WorkflowResponse response = removeSubscriptionWFExecutor.execute(workflow);
                workflow.setStatus(response.getWorkflowStatus());
                

                if (WorkflowStatus.CREATED != response.getWorkflowStatus()) {
                    completeWorkflow(removeSubscriptionWFExecutor, workflow);
                } else {
                    //add entry to workflow table if it is only in pending state
                    //haven't changed the subscription's state to allow to use it till approval
                    addWorkflowEntries(workflow);
                }
                return response;
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
    public List<Label> getLabelInfo(List<String> labels, String username) throws LabelException {

        List<Label> filteredLabels;
        String labelExtractorClassName = config.getLabelExtractor();
        try {
            List<Label> availableLabels = getLabelDAO().getLabelsByName(labels);
            LabelExtractor labelExtractor = (LabelExtractor) Class.forName(labelExtractorClassName).newInstance();
            filteredLabels = labelExtractor.filterLabels(username, availableLabels);
        } catch (APIMgtDAOException e) {
            String errorMsg = "Error occurred while retrieving label information";
            log.error(errorMsg, e);
            throw new LabelException(errorMsg, e, ExceptionCodes.LABEL_EXCEPTION);
        } catch (ClassNotFoundException e) {
            String errorMsg = "Error occurred while loading the class [class name] " + labelExtractorClassName;
            log.error(errorMsg, e);
            throw new LabelException(errorMsg, e, ExceptionCodes.LABEL_EXCEPTION);
        } catch (IllegalAccessException | InstantiationException e) {
            String errorMsg = "Error occurred while creating an instance of the class [class name] " +
                    labelExtractorClassName;
            log.error(errorMsg, e);
            throw new LabelException(errorMsg, e, ExceptionCodes.LABEL_EXCEPTION);
        }
        return filteredLabels;
    }

    /**
     * @see APIStore#getCommentByUUID(String, String)
     */
    @Override
    public Comment getCommentByUUID(String commentId, String apiId) throws APIManagementException {
        Comment comment;
        try {
            ApiDAO apiDAO = getApiDAO();
            API api = apiDAO.getAPI(apiId);
            if (api == null) {
                String errorMsg = "Couldn't find api with api_id : " + apiId;
                log.error(errorMsg);
                throw new APIMgtResourceNotFoundException(errorMsg, ExceptionCodes.API_NOT_FOUND);
            }
            comment = getApiDAO().getCommentByUUID(commentId, apiId);
            if (comment == null) {
                String errorMsg = "Couldn't find comment with comment_id - " + commentId + " for api_id " + apiId;
                log.error(errorMsg);
                throw new APIMgtResourceNotFoundException(errorMsg, ExceptionCodes.COMMENT_NOT_FOUND);
            }
        } catch (APIMgtDAOException e) {
            String errorMsg =
                    "Error occurred while retrieving comment for comment_id " + commentId + " for api_id " + apiId;
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

    /**
     * @see APIStore#addComment(Comment, String)
     */
    @Override
    public String addComment(Comment comment, String apiId) throws APIManagementException {
        String generatedUuid = UUID.randomUUID().toString();
        comment.setUuid(generatedUuid);
        try {
            ApiDAO apiDAO = getApiDAO();
            API api = apiDAO.getAPI(apiId);
            if (api == null) {
                String errorMsg = "Couldn't find api with api_id : " + apiId;
                log.error(errorMsg);
                throw new APIMgtResourceNotFoundException(errorMsg, ExceptionCodes.API_NOT_FOUND);
            }
            getApiDAO().addComment(comment, apiId);
        } catch (APIMgtDAOException e) {
            String errorMsg = "Error occurred while adding comment for api - " + apiId;
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        return comment.getUuid();
    }

    /**
     * @see APIStore#deleteComment(String, String)
     */
    @Override
    public void deleteComment(String commentId, String apiId) throws APIManagementException {
        try {
            ApiDAO apiDAO = getApiDAO();
            API api = apiDAO.getAPI(apiId);
            if (api == null) {
                String errorMsg = "Couldn't find api with api_id : " + apiId;
                log.error(errorMsg);
                throw new APIMgtResourceNotFoundException(errorMsg, ExceptionCodes.API_NOT_FOUND);
            }
            Comment comment = apiDAO.getCommentByUUID(commentId, apiId);
            if (comment == null) {
                String errorMsg = "Couldn't find comment with comment_id : " + commentId;
                log.error(errorMsg);
                throw new APIMgtResourceNotFoundException(errorMsg, ExceptionCodes.COMMENT_NOT_FOUND);
            } else {
                apiDAO.deleteComment(commentId, apiId);
            }
        } catch (APIMgtDAOException e) {
            String errorMsg = "Error occurred while deleting comment " + commentId;
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
    }

    /**
     * @see APIStore#updateComment(Comment, String, String)
     */
    @Override
    public void updateComment(Comment comment, String commentId, String apiId) throws APIManagementException {
        try {
            ApiDAO apiDAO = getApiDAO();
            API api = apiDAO.getAPI(apiId);
            if (api == null) {
                String errorMsg = "Couldn't find api with api_id : " + apiId;
                log.error(errorMsg);
                throw new APIMgtResourceNotFoundException(errorMsg, ExceptionCodes.API_NOT_FOUND);
            }
            Comment oldComment = apiDAO.getCommentByUUID(commentId, apiId);
            if (oldComment != null) {
                getApiDAO().updateComment(comment, commentId, apiId);
            } else {
                String errorMsg = "Couldn't find comment with comment_id : " + commentId + "and api_id : " + apiId;
                log.error(errorMsg);
                throw new APIMgtResourceNotFoundException(errorMsg, ExceptionCodes.COMMENT_NOT_FOUND);
            }
        } catch (APIMgtDAOException e) {
            String errorMsg = "Error occurred while updating comment " + commentId;
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }

    }

    /**
     * @see APIStore#getCommentsForApi(String)
     */
    @Override
    public List<Comment> getCommentsForApi(String apiId) throws APIManagementException {
        try {
            ApiDAO apiDAO = getApiDAO();
            API api = apiDAO.getAPI(apiId);
            if (api == null) {
                String errorMsg = "api not found for the id : " + apiId;
                log.error(errorMsg);
                throw new APIMgtResourceNotFoundException(errorMsg, ExceptionCodes.API_NOT_FOUND);
            }
            List<Comment> commentList = getApiDAO().getCommentsForApi(apiId);
            return commentList;
        } catch (APIMgtDAOException e) {
            String errorMsg = "Error occurred while retrieving comments for api " + apiId;
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String addCompositeApi(API.APIBuilder apiBuilder) throws APIManagementException {
        apiBuilder.provider(getUsername());
        if (StringUtils.isEmpty(apiBuilder.getId())) {
            apiBuilder.id(UUID.randomUUID().toString());
        }

        LocalDateTime localDateTime = LocalDateTime.now();
        apiBuilder.createdTime(localDateTime);
        apiBuilder.lastUpdatedTime(localDateTime);
        apiBuilder.createdBy(getUsername());
        apiBuilder.updatedBy(getUsername());
        apiBuilder.apiType(ApiType.COMPOSITE);

        if (!isApiNameExist(apiBuilder.getName()) && !isContextExist(apiBuilder.getContext())) {
            setUriTemplates(apiBuilder);
            setGatewayDefinitionSource(apiBuilder);
            setSwaggerDefinition(apiBuilder);
            try {
                setPermission(apiBuilder);

                API createdAPI = apiBuilder.build();
                APIUtils.validate(createdAPI);

                //publishing config to gateway
                gateway.addAPI(createdAPI);

                getApiDAO().addAPI(createdAPI);

                if (log.isDebugEnabled()) {
                    log.debug("API " + createdAPI.getName() + "-" + createdAPI.getVersion() + " was created " +
                            "successfully.", log);
                }

            } catch (ParseException e) {
                String errorMsg = "Unable to update the documentation due to json parse error";
                log.error(errorMsg, e);
                throw new APIManagementException(errorMsg, e, ExceptionCodes.JSON_PARSE_ERROR);
            } catch (APIMgtDAOException e) {
                String errorMsg = "Error occurred while creating the API - " + apiBuilder.getName();
                log.error(errorMsg);
                throw new APIManagementException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
            } catch (GatewayException e) {
                String message = "Error publishing service configuration to Gateway " + apiBuilder.getName();
                log.error(message, e);
                throw new APIManagementException(message, e, ExceptionCodes.GATEWAY_EXCEPTION);
            }

        } else {
            String message = "Duplicate API already Exist with name/Context " + apiBuilder.getName();
            log.error(message);
            throw new APIManagementException(message, ExceptionCodes.API_ALREADY_EXISTS);
        }

        return apiBuilder.getId();
    }

    private void setUriTemplates(API.APIBuilder apiBuilder) {
        Map<String, UriTemplate> uriTemplateMap = new HashMap();
        if (apiBuilder.getUriTemplates().isEmpty()) {
            apiDefinitionFromSwagger20.setDefaultSwaggerDefinition(apiBuilder);
        } else {
            for (UriTemplate uriTemplate : apiBuilder.getUriTemplates().values()) {
                UriTemplate.UriTemplateBuilder uriTemplateBuilder = new UriTemplate.UriTemplateBuilder
                        (uriTemplate);
                if (StringUtils.isEmpty(uriTemplateBuilder.getTemplateId())) {
                    uriTemplateBuilder.templateId(APIUtils.generateOperationIdFromPath(uriTemplate
                            .getUriTemplate(), uriTemplate.getHttpVerb()));
                }
                if (uriTemplate.getEndpoint().isEmpty()) {
                    uriTemplateBuilder.endpoint(apiBuilder.getEndpoint());
                }
                uriTemplateMap.put(uriTemplateBuilder.getTemplateId(), uriTemplateBuilder.build());
            }
            apiBuilder.uriTemplates(uriTemplateMap);
        }

    }

    private void setGatewayDefinitionSource(API.APIBuilder apiBuilder) throws APITemplateException {
        List<UriTemplate> list = new ArrayList<>(apiBuilder.getUriTemplates().values());
        List<TemplateBuilderDTO> resourceList = new ArrayList<>();

        for (UriTemplate uriTemplate : list) {
            TemplateBuilderDTO dto = new TemplateBuilderDTO();
            dto.setTemplateId(uriTemplate.getTemplateId());
            dto.setUriTemplate(uriTemplate.getUriTemplate());
            dto.setHttpVerb(uriTemplate.getHttpVerb());
            dto.setAuthType(uriTemplate.getAuthType());
            dto.setPolicy(uriTemplate.getPolicy());
            resourceList.add(dto);
        }
        GatewaySourceGenerator gatewaySourceGenerator = getGatewaySourceGenerator();
        gatewaySourceGenerator.setAPI(apiBuilder.build());
        String gatewayConfig = gatewaySourceGenerator.getConfigStringFromTemplate(resourceList);
        if (log.isDebugEnabled()) {
            log.debug("API " + apiBuilder.getName() + "gateway config: " + gatewayConfig);
        }
        apiBuilder.gatewayConfig(gatewayConfig);
    }

    private void setSwaggerDefinition(API.APIBuilder apiBuilder) {
        if (StringUtils.isEmpty(apiBuilder.getApiDefinition())) {
            apiBuilder.apiDefinition(apiDefinitionFromSwagger20.generateSwaggerFromResources(apiBuilder));
        }
    }

    private void setPermission(API.APIBuilder apiBuilder) throws ParseException {
        if (apiBuilder.getPermission() != null && !("").equals(apiBuilder.getPermission())) {
            HashMap roleNamePermissionList;
            roleNamePermissionList = APIUtils.getAPIPermissionArray(apiBuilder.getPermission());
            apiBuilder.permissionMap(roleNamePermissionList);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateCompositeApi(API.APIBuilder apiBuilder) throws APIManagementException {
        apiBuilder.provider(getUsername());
        apiBuilder.updatedBy(getUsername());

        API originalAPI = getAPIbyUUID(apiBuilder.getId());
        if (originalAPI != null) {
            apiBuilder.createdTime(originalAPI.getCreatedTime());
            //workflow status is an internal property and shouldn't be allowed to update externally
            apiBuilder.workflowStatus(originalAPI.getWorkflowStatus());

            APIUtils.verifyValidityOfApiUpdate(apiBuilder, originalAPI);

            try {
                setPermission(apiBuilder);

                String updatedSwagger = apiDefinitionFromSwagger20.generateSwaggerFromResources(apiBuilder);
                String gatewayConfig = getApiGatewayConfig(apiBuilder.getId());
                GatewaySourceGenerator gatewaySourceGenerator = getGatewaySourceGenerator();
                gatewaySourceGenerator.setAPI(apiBuilder.build());
                String updatedGatewayConfig = gatewaySourceGenerator
                        .getGatewayConfigFromSwagger(gatewayConfig, updatedSwagger);

                API api = apiBuilder.build();

                if (originalAPI.getContext() != null && !originalAPI.getContext().equals(apiBuilder.getContext())) {
                    if (isContextExist(api.getContext())) {
                        throw new APIManagementException("Context already Exist", ExceptionCodes
                                .API_ALREADY_EXISTS);
                    }
                }

                //publishing config to gateway
                gateway.addAPI(api);

                getApiDAO().updateSwaggerDefinition(api.getId(), updatedSwagger, api.getUpdatedBy());
                getApiDAO().updateGatewayConfig(api.getId(), updatedGatewayConfig, api.getUpdatedBy());

                if (log.isDebugEnabled()) {
                    log.debug("API " + api.getName() + "-" + api.getVersion() + " was updated successfully.");
                }

            } catch (ParseException e) {
                String errorMsg = "Unable to update the documentation due to json parse error";
                log.error(errorMsg, e);
                throw new APIManagementException(errorMsg, e, ExceptionCodes.JSON_PARSE_ERROR);
            } catch (APIMgtDAOException e) {
                String errorMsg = "Error occurred while updating the API - " + apiBuilder.getName();
                log.error(errorMsg, e);
                throw new APIManagementException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
            }
        } else {

            log.error("Couldn't found API with ID " + apiBuilder.getId());
            throw new APIManagementException("Couldn't found API with ID " + apiBuilder.getId(),
                    ExceptionCodes.API_NOT_FOUND);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteCompositeApi(String apiId) throws APIManagementException {
        try {
            API api = getApiDAO().getAPI(apiId);
            if (api != null && api.getApiType() == ApiType.COMPOSITE) {
                //Delete API in gateway
                gateway.deleteAPI(api);
                getApiDAO().deleteAPI(apiId);
            }
        } catch (GatewayException e) {
            String message = "Error occurred while deleting Composite API with id - " + apiId + " from gateway";
            throw new APIManagementException(message, e, ExceptionCodes.GATEWAY_EXCEPTION);
        } catch (APIMgtDAOException e) {
            String errorMsg = "Error occurred while deleting the Composite API with id " + apiId;
            throw new APIManagementException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String createNewCompositeApiVersion(String apiId, String newVersion) throws APIManagementException {
        // validate parameters
        if (StringUtils.isEmpty(newVersion)) {
            String errorMsg = "New API version cannot be empty";
            log.error(errorMsg);
            throw new APIManagementException(errorMsg, ExceptionCodes.PARAMETER_NOT_PROVIDED);
        }
        if (StringUtils.isEmpty(apiId)) {
            String errorMsg = "API ID cannot be empty";
            log.error(errorMsg);
            throw new APIManagementException(errorMsg, ExceptionCodes.PARAMETER_NOT_PROVIDED);
        }

        String newVersionedId;

        try {
            API api = getApiDAO().getAPI(apiId);
            if (api != null) {
                if (api.getVersion().equals(newVersion)) {
                    String errMsg = "New API version " + newVersion + " cannot be same as the previous version for " +
                            "API " + api.getName();
                    log.error(errMsg);
                    throw new APIManagementException(errMsg, ExceptionCodes.API_ALREADY_EXISTS);
                }
                API.APIBuilder apiBuilder = new API.APIBuilder(api);
                apiBuilder.id(UUID.randomUUID().toString());
                apiBuilder.version(newVersion);
                apiBuilder.context(api.getContext().replace(api.getVersion(), newVersion));
                apiBuilder.copiedFromApiId(api.getId());
                if (StringUtils.isEmpty(apiBuilder.getApiDefinition())) {
                    apiBuilder.apiDefinition(apiDefinitionFromSwagger20.generateSwaggerFromResources(apiBuilder));
                }
                getApiDAO().addAPI(apiBuilder.build());
                newVersionedId = apiBuilder.getId();
            } else {
                throw new APIMgtResourceNotFoundException("Requested API on UUID " + apiId + "Couldn't be found");
            }
        } catch (APIMgtDAOException e) {
            String errorMsg = "Couldn't create new API version from " + apiId;
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }

        return newVersionedId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String addCompositeApiFromDefinition(InputStream apiDefinition) throws APIManagementException {
        try {
            String apiDefinitionString = IOUtils.toString(apiDefinition);
            API.APIBuilder apiBuilder = apiDefinitionFromSwagger20.generateApiFromSwaggerResource(getUsername(),
                    apiDefinitionString);
            apiBuilder.corsConfiguration(new CorsConfiguration());
            apiBuilder.apiDefinition(apiDefinitionString);
            addCompositeApi(apiBuilder);
            return apiBuilder.getId();
        } catch (IOException e) {
            throw new APIManagementException("Couldn't Generate ApiDefinition from file", ExceptionCodes
                    .API_DEFINITION_MALFORMED);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String addCompositeApiFromDefinition(String swaggerResourceUrl) throws APIManagementException {
        try {
            URL url = new URL(swaggerResourceUrl);
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setDoOutput(true);
            urlConn.setRequestMethod(APIMgtConstants.HTTP_GET);
            int responseCode = urlConn.getResponseCode();
            if (responseCode == 200) {
                String responseStr = new String(IOUtils.toByteArray(urlConn.getInputStream()), StandardCharsets.UTF_8);
                API.APIBuilder apiBuilder = apiDefinitionFromSwagger20.generateApiFromSwaggerResource(getUsername(),
                        responseStr);
                apiBuilder.corsConfiguration(new CorsConfiguration());
                apiBuilder.apiDefinition(responseStr);
                addCompositeApi(apiBuilder);
                return apiBuilder.getId();
            } else {
                throw new APIManagementException("Error while getting swagger resource from url : " + url,
                        ExceptionCodes.API_DEFINITION_MALFORMED);
            }
        } catch (UnsupportedEncodingException e) {
            String msg = "Unsupported encoding exception while getting the swagger resource from url";
            log.error(msg, e);
            throw new APIManagementException(msg, ExceptionCodes.API_DEFINITION_MALFORMED);
        } catch (ProtocolException e) {
            String msg = "Protocol exception while getting the swagger resource from url";
            log.error(msg, e);
            throw new APIManagementException(msg, ExceptionCodes.API_DEFINITION_MALFORMED);
        } catch (MalformedURLException e) {
            String msg = "Malformed url while getting the swagger resource from url";
            log.error(msg, e);
            throw new APIManagementException(msg, ExceptionCodes.API_DEFINITION_MALFORMED);
        } catch (IOException e) {
            String msg = "Error while getting the swagger resource from url";
            log.error(msg, e);
            throw new APIManagementException(msg, ExceptionCodes.API_DEFINITION_MALFORMED);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<API> searchAPIs(String query, int offset, int limit) throws APIManagementException {

        List<API> apiResults = null;

        try {

            // TODO: Need to validate users roles against results returned
            //this should be current logged in user
            String user = "admin";
            //role list of current user
            Set<String> roles = APIUtils.getAllRolesOfUser(user);
            if (query != null && !query.isEmpty()) {
                String[] attributes = query.split(",");
                Map<String, String> attributeMap = new HashMap<>();
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
                    apiResults = getApiDAO().searchAPIs(roles, user, query, ApiType.STANDARD, offset, limit);
                } else {
                    apiResults = getApiDAO().attributeSearchAPIsStore(new ArrayList<>(roles),
                            attributeMap, offset, limit);
                }
            } else {
                List<String> statuses = new ArrayList<>();
                statuses.add(APIStatus.PUBLISHED.getStatus());
                statuses.add(APIStatus.PROTOTYPED.getStatus());
                apiResults = getApiDAO().getAPIsByStatus(roles, statuses, ApiType.STANDARD);
            }

        } catch (APIMgtDAOException e) {
            String errorMsg = "Error occurred while updating searching APIs - " + query;
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }

        return apiResults;
    }

    /**
     * @see APIStore#deleteApplication(String)
     */
    @Override
    public WorkflowResponse deleteApplication(String appId) throws APIManagementException {
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
            //delete application creation pending tasks
            cleanupPendingTaskForApplicationDeletion(application);
            WorkflowExecutor removeApplicationWFExecutor = WorkflowExecutorFactory.getInstance().
                    getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_APPLICATION_DELETION);
            
            ApplicationCreationWorkflow workflow = new ApplicationCreationWorkflow();
            workflow.setApplication(application);
            workflow.setWorkflowType(APIMgtConstants.WorkflowConstants.WF_TYPE_AM_APPLICATION_DELETION);
            workflow.setWorkflowReference(application.getId());
            workflow.setExternalWorkflowReference(UUID.randomUUID().toString());
            workflow.setCreatedTime(LocalDateTime.now());
            String workflowDescription = "Application deletion workflow for " + application.getName() + " by "
                    + getUsername();
            workflow.setWorkflowDescription(workflowDescription);
            WorkflowResponse response = removeApplicationWFExecutor.execute(workflow);
            workflow.setStatus(response.getWorkflowStatus());

            if (WorkflowStatus.CREATED != response.getWorkflowStatus()) {
                completeWorkflow(removeApplicationWFExecutor, workflow);
            } else {
                //add entry to workflow table if it is only in pending state
                addWorkflowEntries(workflow);
            }
            return response;
        } catch (APIMgtDAOException e) {
            String errorMsg = "Error occurred while deleting the application - " + appId;
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
    }

    private void cleanupPendingTaskForApplicationDeletion(Application application) throws APIManagementException {
        WorkflowExecutor createApplicationWFExecutor = WorkflowExecutorFactory.getInstance()
                .getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_APPLICATION_CREATION);
        WorkflowExecutor createSubscriptionWFExecutor = WorkflowExecutorFactory.getInstance()
                .getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION);
        WorkflowExecutor updateApplicationWFExecutor = WorkflowExecutorFactory.getInstance()
                .getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_APPLICATION_UPDATE);

        String appId = application.getId();
        // get subscriptions with pending status
        List<Subscription> pendingSubscriptions = getApiSubscriptionDAO()
                .getPendingAPISubscriptionsByApplication(appId);
  
        String applicationStatus = application.getStatus();

        if (pendingSubscriptions == null || pendingSubscriptions.isEmpty()) {
            // check whether application is on hold state
            if (ApplicationStatus.APPLICATION_ONHOLD.equals(applicationStatus)) {
                //delete pending tasks for application creation if any
                cleanupPendingTask(createApplicationWFExecutor, appId,
                        WorkflowConstants.WF_TYPE_AM_APPLICATION_CREATION);
            }
        } else {

            // this means there are pending subscriptions. It also implies that there cannot be pending application
            // approvals (cannot subscribe to a pending application)
            for (Iterator iterator = pendingSubscriptions.iterator(); iterator.hasNext();) {
                Subscription pendingSubscription = (Subscription) iterator.next();

                // delete pending tasks for subscripton creation if any
                cleanupPendingTask(createSubscriptionWFExecutor, pendingSubscription.getId(),
                        WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION);
            }
        }
        //delete pending tasks for application update if any
        cleanupPendingTask(updateApplicationWFExecutor, appId, WorkflowConstants.WF_TYPE_AM_APPLICATION_UPDATE);        
    }
    
    private void cleanupPendingTaskForSubscriptionDeletion(Subscription subscription) throws APIManagementException {
        WorkflowExecutor createSubscriptionWFExecutor = WorkflowExecutorFactory.getInstance()
                .getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION);
        if (APIMgtConstants.SubscriptionStatus.ON_HOLD == subscription.getStatus()) {
            cleanupPendingTask(createSubscriptionWFExecutor, subscription.getId(),
                    WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION);
        }
    }

    @Override
    public ApplicationCreationResponse addApplication(Application application) throws APIManagementException {
        ApplicationCreationResponse applicationResponse = null;

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
            workflow.setCreatedBy(getUsername());
            workflow.setWorkflowReference(application.getId());
            workflow.setExternalWorkflowReference(UUID.randomUUID().toString());
            workflow.setCreatedTime(LocalDateTime.now());
            
            String workflowDescription = "Application creation workflow for " + application.getName() + " with tier "
                    + tierName + " by " + getUsername();
            workflow.setWorkflowDescription(workflowDescription);
            WorkflowResponse response = appCreationWFExecutor.execute(workflow);
            workflow.setStatus(response.getWorkflowStatus());

            if (WorkflowStatus.CREATED != response.getWorkflowStatus()) {
                completeWorkflow(appCreationWFExecutor, workflow);
            } else {
                getApplicationDAO().updateApplicationState(generatedUuid,
                        APIMgtConstants.ApplicationStatus.APPLICATION_ONHOLD);
                addWorkflowEntries(workflow);
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

    /**API store related workflow complete tasks
     * {@inheritDoc}
     */
    @Override
    public WorkflowResponse completeWorkflow(WorkflowExecutor workflowExecutor, Workflow workflow)
            throws APIManagementException {
        WorkflowResponse response;
        if (workflow.getWorkflowReference() == null) {
            String message = "Error while changing the workflow. Missing reference";
            log.error(message);
            throw new APIManagementException(message, ExceptionCodes.WORKFLOW_EXCEPTION);
        }

        if (workflow instanceof ApplicationCreationWorkflow
                && WorkflowConstants.WF_TYPE_AM_APPLICATION_CREATION.equals(workflow.getWorkflowType())) {
            ApplicationCreationWorkflow appCreateWorkflow = (ApplicationCreationWorkflow) workflow;
            if (appCreateWorkflow.getApplication() == null) {
                // this is when complete method is executed through workflow rest api
                appCreateWorkflow.setApplication(getApplicationDAO().getApplication(workflow.getWorkflowReference()));
            }      
            response = workflowExecutor.complete(appCreateWorkflow);

            // setting the workflow status from the one getting from the executor. this gives the executor developer
            // to change the state as well.
            appCreateWorkflow.setStatus(response.getWorkflowStatus());

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
            updateWorkflowEntries(appCreateWorkflow);

        } else if (workflow instanceof ApplicationCreationWorkflow
                && WorkflowConstants.WF_TYPE_AM_APPLICATION_DELETION.equals(workflow.getWorkflowType())) {
            ApplicationCreationWorkflow appDeleteWorkflow = (ApplicationCreationWorkflow) workflow;
            if (appDeleteWorkflow.getApplication() == null) {
                // this is when complete method is executed through workflow rest api
                appDeleteWorkflow.setApplication(getApplicationDAO().getApplication(workflow.getWorkflowReference()));
            }    
            response = workflowExecutor.complete(appDeleteWorkflow);
            appDeleteWorkflow.setStatus(response.getWorkflowStatus());
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
            updateWorkflowEntries(appDeleteWorkflow);
        } else if (workflow instanceof SubscriptionWorkflow
                && WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION.equals(workflow.getWorkflowType())) {
            
            SubscriptionWorkflow subsriptionWorkflow = (SubscriptionWorkflow) workflow;
            if (subsriptionWorkflow.getSubscription() == null) {
                // this is when complete method is executed through workflow rest api
                subsriptionWorkflow
                        .setSubscription(getApiSubscriptionDAO().getAPISubscription(workflow.getWorkflowReference()));
            }
            response = workflowExecutor.complete(workflow);
            workflow.setStatus(response.getWorkflowStatus());
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

            //Add subscription to gateway
            gateway.addAPISubscription(((SubscriptionWorkflow) workflow).getSubscription());
            getApiSubscriptionDAO().updateSubscriptionStatus(workflow.getWorkflowReference(), subscriptionState);
            updateWorkflowEntries(workflow);
        } else if (workflow instanceof SubscriptionWorkflow
                && WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_DELETION.equals(workflow.getWorkflowType())) {
            SubscriptionWorkflow subsriptionWorkflow = (SubscriptionWorkflow) workflow;
            if (subsriptionWorkflow.getSubscription() == null) {
                // this is when complete method is executed through workflow rest api
                subsriptionWorkflow
                        .setSubscription(getApiSubscriptionDAO().getAPISubscription(workflow.getWorkflowReference()));
            }
            response = workflowExecutor.complete(subsriptionWorkflow);
            subsriptionWorkflow.setStatus(response.getWorkflowStatus());
            if (WorkflowStatus.APPROVED == response.getWorkflowStatus()) {
                if (log.isDebugEnabled()) {
                    log.debug("Subscription deletion workflow complete: Approved");
                }
                gateway.deleteAPISubscription(((SubscriptionWorkflow) workflow).getSubscription());
                getApiSubscriptionDAO().deleteAPISubscription(workflow.getWorkflowReference());

            } else if (WorkflowStatus.REJECTED == response.getWorkflowStatus()) {
                if (log.isDebugEnabled()) {
                    log.debug("Subscription deletion workflow complete: Rejected");
                }
            }
            updateWorkflowEntries(subsriptionWorkflow);
        } else if (workflow instanceof ApplicationUpdateWorkflow) {
            ApplicationUpdateWorkflow updateWorkflow = (ApplicationUpdateWorkflow) workflow;
            String appId = updateWorkflow.getWorkflowReference();
            String name = updateWorkflow.getAttribute(WorkflowConstants.ATTRIBUTE_APPLICATION_NAME);
            String updatedUser = updateWorkflow.getAttribute(WorkflowConstants.ATTRIBUTE_APPLICATION_UPDATEDBY);
            String applicationId = updateWorkflow.getWorkflowReference();
            String tier = updateWorkflow.getAttribute(WorkflowConstants.ATTRIBUTE_APPLICATION_TIER);
            String description = updateWorkflow.getAttribute(WorkflowConstants.ATTRIBUTE_APPLICATION_DESCRIPTION);
            String callbackUrl = updateWorkflow.getAttribute(WorkflowConstants.ATTRIBUTE_APPLICATION_CALLBACKURL);
            String groupId = updateWorkflow.getAttribute(WorkflowConstants.ATTRIBUTE_APPLICATION_GROUPID);
            String permisson = updateWorkflow.getAttribute(WorkflowConstants.ATTRIBUTE_APPLICATION_PERMISSION);

            Application application = new Application(name, updatedUser); 
            application.setTier(tier);
            application.setCallbackUrl(callbackUrl);
            application.setDescription(description);
            application.setGroupId(groupId);
            application.setId(applicationId);
            application.setUpdatedTime(LocalDateTime.now());
            application.setUpdatedUser(updatedUser);
            application.setPermissionString(permisson);

            if (updateWorkflow.getExistingApplication() == null && updateWorkflow.getUpdatedApplication() == null) {
                // this is when complete method is executed through workflow rest api
                updateWorkflow.setExistingApplication(getApplicationDAO().getApplication(appId));
                updateWorkflow.setUpdatedApplication(application);
            }
            response = workflowExecutor.complete(updateWorkflow);
            
            updateWorkflow.setStatus(response.getWorkflowStatus());
            if (WorkflowStatus.APPROVED == response.getWorkflowStatus()) {
                if (log.isDebugEnabled()) {
                    log.debug("Application update workflow complete: Approved");
                }
                application.setStatus(ApplicationStatus.APPLICATION_APPROVED);                
                getApplicationDAO().updateApplication(appId, application);
            } else if (WorkflowStatus.REJECTED == response.getWorkflowStatus()) {
                if (log.isDebugEnabled()) {
                    log.debug("Application update workflow complete: Rejected");
                }
                String existingAppStatus = updateWorkflow
                        .getAttribute(WorkflowConstants.ATTRIBUTE_APPLICATION_EXISTIN_APP_STATUS);
                getApplicationDAO().updateApplicationState(appId, existingAppStatus);
            }
            updateWorkflowEntries(updateWorkflow);
        } else {
            String message = "Invalid workflow type for store workflows:  " + workflow.getWorkflowType();
            log.error(message);
            throw new APIManagementException(message, ExceptionCodes.WORKFLOW_INV_STORE_WFTYPE);
        }

        return response;
    }


}

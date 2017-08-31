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
import org.wso2.carbon.apimgt.core.api.APIGateway;
import org.wso2.carbon.apimgt.core.api.APIMObservable;
import org.wso2.carbon.apimgt.core.api.APIMgtAdminService;
import org.wso2.carbon.apimgt.core.api.APIStore;
import org.wso2.carbon.apimgt.core.api.EventObserver;
import org.wso2.carbon.apimgt.core.api.GatewaySourceGenerator;
import org.wso2.carbon.apimgt.core.api.IdentityProvider;
import org.wso2.carbon.apimgt.core.api.KeyManager;
import org.wso2.carbon.apimgt.core.api.LabelExtractor;
import org.wso2.carbon.apimgt.core.api.WSDLProcessor;
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
import org.wso2.carbon.apimgt.core.exception.APICommentException;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.exception.APIMgtResourceAlreadyExistsException;
import org.wso2.carbon.apimgt.core.exception.APIMgtResourceNotFoundException;
import org.wso2.carbon.apimgt.core.exception.APIMgtWSDLException;
import org.wso2.carbon.apimgt.core.exception.APINotFoundException;
import org.wso2.carbon.apimgt.core.exception.APIRatingException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.exception.GatewayException;
import org.wso2.carbon.apimgt.core.exception.LabelException;
import org.wso2.carbon.apimgt.core.exception.WorkflowException;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.APIStatus;
import org.wso2.carbon.apimgt.core.models.AccessTokenInfo;
import org.wso2.carbon.apimgt.core.models.AccessTokenRequest;
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.core.models.ApplicationToken;
import org.wso2.carbon.apimgt.core.models.Comment;
import org.wso2.carbon.apimgt.core.models.CompositeAPI;
import org.wso2.carbon.apimgt.core.models.Event;
import org.wso2.carbon.apimgt.core.models.Label;
import org.wso2.carbon.apimgt.core.models.OAuthAppRequest;
import org.wso2.carbon.apimgt.core.models.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.core.models.Rating;
import org.wso2.carbon.apimgt.core.models.Subscription;
import org.wso2.carbon.apimgt.core.models.SubscriptionResponse;
import org.wso2.carbon.apimgt.core.models.SubscriptionValidationData;
import org.wso2.carbon.apimgt.core.models.Tag;
import org.wso2.carbon.apimgt.core.models.UriTemplate;
import org.wso2.carbon.apimgt.core.models.User;
import org.wso2.carbon.apimgt.core.models.WSDLArchiveInfo;
import org.wso2.carbon.apimgt.core.models.WorkflowStatus;
import org.wso2.carbon.apimgt.core.models.policy.Policy;
import org.wso2.carbon.apimgt.core.template.APIConfigContext;
import org.wso2.carbon.apimgt.core.template.dto.CompositeAPIEndpointDTO;
import org.wso2.carbon.apimgt.core.template.dto.TemplateBuilderDTO;
import org.wso2.carbon.apimgt.core.util.APIFileUtils;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants.ApplicationStatus;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants.WorkflowConstants;
import org.wso2.carbon.apimgt.core.util.APIUtils;
import org.wso2.carbon.apimgt.core.util.KeyManagerConstants;
import org.wso2.carbon.apimgt.core.workflow.ApplicationCreationResponse;
import org.wso2.carbon.apimgt.core.workflow.ApplicationCreationWorkflow;
import org.wso2.carbon.apimgt.core.workflow.ApplicationDeletionWorkflow;
import org.wso2.carbon.apimgt.core.workflow.ApplicationUpdateWorkflow;
import org.wso2.carbon.apimgt.core.workflow.SubscriptionCreationWorkflow;
import org.wso2.carbon.apimgt.core.workflow.SubscriptionDeletionWorkflow;
import org.wso2.carbon.apimgt.core.workflow.WorkflowExecutorFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
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
    APIGateway gateway = getApiGateway();

    /**
     * Constructor.
     *
     * @param username   Logged in user's username
     * @param idp Identity Provider Object
     * @param keyManager Key Manager Object
     * @param apiDAO  API Data Access Object
     * @param applicationDAO  Application Data Access Object
     * @param apiSubscriptionDAO   API Subscription Data Access Object
     * @param policyDAO Policy Data Access Object
     * @param tagDAO Tag Data Access Object
     * @param labelDAO Label Data Access Object
     * @param workflowDAO WorkFlow Data Access Object
     * @param gatewaySourceGenerator GatewaySourceGenerator object
     * @param apiGateway APIGateway object
     */
    public APIStoreImpl(String username, IdentityProvider idp, KeyManager keyManager, ApiDAO apiDAO,
                        ApplicationDAO applicationDAO, APISubscriptionDAO apiSubscriptionDAO, PolicyDAO policyDAO,
                        TagDAO tagDAO, LabelDAO labelDAO, WorkflowDAO workflowDAO,
                        GatewaySourceGenerator gatewaySourceGenerator, APIGateway apiGateway) {
        super(username, idp, keyManager, apiDAO, applicationDAO, apiSubscriptionDAO, policyDAO,
                new APILifeCycleManagerImpl(), labelDAO, workflowDAO, tagDAO, gatewaySourceGenerator, apiGateway);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompositeAPI getCompositeAPIbyId(String id) throws APIManagementException {
        CompositeAPI api = null;
        try {
            api = getApiDAO().getCompositeAPI(id);
        } catch (APIMgtDAOException e) {
            String errorMsg = "Error occurred while retrieving API with id " + id;
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, e.getErrorHandler());
        }
        return api;
    }

    @Override
    public List<API> getAllAPIsByStatus(int offset, int limit, String[] statuses) throws APIManagementException {
        List<API> apiResults = null;
        try {
            apiResults = getApiDAO().getAPIsByStatus(new ArrayList<>(Arrays.asList(statuses)));
        } catch (APIMgtDAOException e) {
            String errorMsg = "Error occurred while fetching APIs for the given statuses     - "
                    + Arrays.toString(statuses);
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, e.getErrorHandler());
        }
        return apiResults;
    }

    @Override
    public Application getApplicationByName(String applicationName, String ownerId)
            throws APIManagementException {
        Application application = null;
        try {
            application = getApplicationDAO().getApplicationByName(applicationName, ownerId);
        } catch (APIMgtDAOException e) {
            String errorMsg = "Error occurred while fetching application for the given applicationName - "
                    + applicationName;
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, e.getErrorHandler());
        }
        return application;
    }


    @Override
    public List<Application> getApplications(String subscriber) throws APIManagementException {
        List<Application> applicationList = null;
        try {
            applicationList = getApplicationDAO().getApplications(subscriber);
        } catch (APIMgtDAOException e) {
            String errorMsg = "Error occurred while fetching applications for the given subscriber - " + subscriber;
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, e.getErrorHandler());
        }
        return applicationList;
    }

    @Override
    public WorkflowResponse updateApplication(String uuid, Application application) throws APIManagementException {
        try {
            //get old app
            Application existingApplication = getApplicationDAO().getApplication(uuid);
            if (existingApplication != null) {
                WorkflowExecutor executor = WorkflowExecutorFactory.getInstance()
                        .getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_APPLICATION_UPDATE);
                ApplicationUpdateWorkflow workflow = new ApplicationUpdateWorkflow(getApplicationDAO(),
                        getWorkflowDAO(), getApiGateway());
                application.setId(uuid);
                application.setUpdatedUser(getUsername());
                application.setUpdatedTime(LocalDateTime.now());

                Policy appTier = application.getPolicy();
                if (appTier != null && !appTier.getPolicyName().equals(existingApplication.getPolicy().getPolicyName
                        ())) {
                    Policy policy = getPolicyDAO().getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel
                            .application, appTier.getPolicyName());
                    if (policy == null) {
                        String message = "Specified tier " + appTier + " is invalid";
                        log.error(message);
                        throw new APIManagementException(message, ExceptionCodes.TIER_NAME_INVALID);
                    }
                    application.setPolicy(policy);
                }
                workflow.setExistingApplication(existingApplication);
                workflow.setUpdatedApplication(application);
                workflow.setCreatedBy(getUsername());
                workflow.setWorkflowReference(application.getId());
                workflow.setExternalWorkflowReference(UUID.randomUUID().toString());
                workflow.setCreatedTime(LocalDateTime.now());

                String workflowDescription = "Update application " + existingApplication.getName() + " with tier "
                        + existingApplication.getPolicy().getPolicyName() + " and description \'"
                        + existingApplication.getDescription() + "\' To " + application.getName() + " with tier "
                        + application.getPolicy().getPolicyName() + " and description \'" + application.getDescription()
                        + "\' by " + getUsername();

                workflow.setWorkflowDescription(workflowDescription);

                //setting attributes for internal use. These are set to use from outside the executor's method
                //these will be saved in the AM_WORKFLOW table so these can be retrieved later for external wf approval
                //scenarios. this won't get stored for simple wfs
                workflow.setAttribute(WorkflowConstants.ATTRIBUTE_APPLICATION_NAME, application.getName());
                workflow.setAttribute(WorkflowConstants.ATTRIBUTE_APPLICATION_UPDATEDBY, application.getUpdatedUser());
                workflow.setAttribute(WorkflowConstants.ATTRIBUTE_APPLICATION_TIER, application.getPolicy()
                        .getPolicyName());
                workflow.setAttribute(WorkflowConstants.ATTRIBUTE_APPLICATION_POLICY_ID, application.getPolicy()
                        .getUuid());
                workflow.setAttribute(WorkflowConstants.ATTRIBUTE_APPLICATION_DESCRIPTION,
                        application.getDescription());
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
            throw new APIManagementException(errorMsg, e, e.getErrorHandler());
        }
    }

    @Override
    public OAuthApplicationInfo generateApplicationKeys(String applicationId, String keyType,
                                                        String callbackUrl, List<String> grantTypes)
            throws APIManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Generating application keys for application: " + applicationId);
        }

        Application application = getApplicationByUuid(applicationId);

        OAuthAppRequest oauthAppRequest = new OAuthAppRequest(application.getName(), callbackUrl, keyType,
                grantTypes);

        OAuthApplicationInfo oauthAppInfo = getKeyManager().createApplication(oauthAppRequest);

        if (log.isDebugEnabled()) {
            log.debug("Application key generation was successful for application: " + application.getName()
                    + " Client Id: " + oauthAppInfo.getClientId());
        }

        try {
            getApplicationDAO().addApplicationKeys(applicationId, keyType, oauthAppInfo.getClientId());
        } catch (APIMgtDAOException e) {
            String errorMsg = "Error occurred while saving key data for application: " + application.getName();
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, e.getErrorHandler());
        }

        if (log.isDebugEnabled()) {
            log.debug("Application keys are successfully saved in the database for application: "
                    + application.getName() + " Client Id: " + oauthAppInfo.getClientId());
        }

        List<SubscriptionValidationData> subscriptionValidationData = getApiSubscriptionDAO()
                .getAPISubscriptionsOfAppForValidation(applicationId, keyType);
        if (subscriptionValidationData != null && !subscriptionValidationData.isEmpty()) {
            getApiGateway().addAPISubscription(subscriptionValidationData);
        }

        return oauthAppInfo;
    }

    @Override
    public OAuthApplicationInfo mapApplicationKeys(String applicationId, String keyType, String clientId,
                                                   String clientSecret) throws APIManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Semi-manual client registering for App: " + applicationId + " and Client ID: " + clientId);
        }

        if (StringUtils.isEmpty(applicationId) || StringUtils.isEmpty(clientId) || StringUtils.isEmpty(clientSecret)) {
            String msg = "One of input values is null or empty. Application Id: " + applicationId + " Client Id: "
                    + clientId + (StringUtils.isEmpty(clientSecret) ? " Client Secret: " + clientSecret : "");
            log.error(msg);
            throw new APIManagementException(msg, ExceptionCodes.OAUTH2_APP_MAP_FAILED);
        }

        //Checking whether given consumer key and secret match with an existing OAuth app.
        //If they does not match, throw an exception.
        OAuthApplicationInfo oAuthApp = getKeyManager().retrieveApplication(clientId);
        if (oAuthApp == null || !clientSecret.equals(oAuthApp.getClientSecret())) {
            String msg = "Unable to find OAuth app. The provided Client Id is invalid. Client Id: " + clientId;
            throw new APIManagementException(msg, ExceptionCodes.OAUTH2_APP_MAP_FAILED);
        }

        try {
            getApplicationDAO().addApplicationKeys(applicationId, keyType, clientId);
        } catch (APIMgtDAOException e) {
            String errorMsg = "Error occurred while saving key data.";
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, e.getErrorHandler());
        }

        log.debug("Application keys are successfully saved in the database");

        List<SubscriptionValidationData> subscriptionValidationData = getApiSubscriptionDAO()
                .getAPISubscriptionsOfAppForValidation(applicationId, keyType);
        if (subscriptionValidationData != null && !subscriptionValidationData.isEmpty()) {
            getApiGateway().addAPISubscription(subscriptionValidationData);
        }

        if (log.isDebugEnabled()) {
            log.debug("Semi-manual client registration was successful for application: " + applicationId
                    + " and Client ID: " + clientId);
        }
        return oAuthApp;
    }

    @Override
    public List<OAuthApplicationInfo> getApplicationKeys(String applicationId) throws APIManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Getting keys of App: " + applicationId);
        }

        if (StringUtils.isEmpty(applicationId)) {
            String msg = "Input value is null or empty. Application Id: " + applicationId;
            log.error(msg);
            throw new APIManagementException(msg, ExceptionCodes.OAUTH2_APP_RETRIEVAL_FAILED);
        }

        try {
            List<OAuthApplicationInfo> allKeysFromDB = getApplicationDAO().getApplicationKeys(applicationId);
            for (OAuthApplicationInfo keys : allKeysFromDB) {
                OAuthApplicationInfo oAuthApp = getKeyManager().retrieveApplication(keys.getClientId());
                keys.setClientSecret(oAuthApp.getClientSecret());
                keys.setGrantTypes(oAuthApp.getGrantTypes());
                keys.setCallBackURL(oAuthApp.getCallBackURL());
            }
            if (log.isDebugEnabled()) {
                log.debug("Retrieved all keys of App: " + applicationId);
            }
            return allKeysFromDB;
        } catch (APIMgtDAOException e) {
            String errorMsg = "Error occurred while getting keys of application: " + applicationId;
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, e.getErrorHandler());
        }
    }

    @Override
    public OAuthApplicationInfo getApplicationKeys(String applicationId, String keyType) throws APIManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Getting " + keyType + " keys of App: " + applicationId);
        }

        if (StringUtils.isEmpty(applicationId) || StringUtils.isEmpty(keyType)) {
            String msg = "One of input values is null or empty. Application Id: " + applicationId + " Key Type: "
                    + keyType;
            log.error(msg);
            throw new APIManagementException(msg, ExceptionCodes.OAUTH2_APP_RETRIEVAL_FAILED);
        }

        try {
            OAuthApplicationInfo keysFromDB = getApplicationDAO().getApplicationKeys(applicationId, keyType);
            OAuthApplicationInfo oAuthApp = getKeyManager().retrieveApplication(keysFromDB.getClientId());
            keysFromDB.setClientSecret(oAuthApp.getClientSecret());
            keysFromDB.setGrantTypes(oAuthApp.getGrantTypes());
            keysFromDB.setCallBackURL(oAuthApp.getCallBackURL());
            if (log.isDebugEnabled()) {
                log.debug("Retrieved " + keyType + " keys of App: " + applicationId);
            }
            return keysFromDB;
        } catch (APIMgtDAOException e) {
            String errorMsg = "Error occurred while getting " + keyType + " keys of application: " + applicationId;
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, e.getErrorHandler());
        }
    }

    @Override
    public OAuthApplicationInfo updateGrantTypesAndCallbackURL(String applicationId, String keyType,
                                                               List<String> grantTypes, String callbackURL)
            throws APIManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Updating " + keyType + " grant type/callback of App: " + applicationId);
        }

        if (StringUtils.isEmpty(applicationId) || StringUtils.isEmpty(keyType)) {
            String msg = "One of input values is null or empty. Application Id: " + applicationId + " Key Type: "
                    + keyType;
            log.error(msg);
            throw new APIManagementException(msg, ExceptionCodes.OAUTH2_APP_RETRIEVAL_FAILED);
        }

        if (grantTypes == null || grantTypes.isEmpty() || StringUtils.isEmpty(callbackURL)) {
            String msg = "Both Grant Types list and Callback URL can't be null or empty at once.";
            log.error(msg);
            throw new APIManagementException(msg, ExceptionCodes.OAUTH2_APP_RETRIEVAL_FAILED);
        }

        try {
            OAuthApplicationInfo appFromDB = getApplicationDAO().getApplicationKeys(applicationId, keyType);
            OAuthApplicationInfo oAuthApp = getKeyManager().retrieveApplication(appFromDB.getClientId());
            oAuthApp.setGrantTypes(grantTypes);
            oAuthApp.setCallBackURL(callbackURL);
            oAuthApp = getKeyManager().updateApplication(oAuthApp);
            if (log.isDebugEnabled()) {
                log.debug("Updated " + keyType + " grant type/callback of App: " + applicationId);
            }
            return oAuthApp;
        } catch (APIMgtDAOException e) {
            String errorMsg = "Error occurred while updating " + keyType + " grant type/callback of application: "
                    + applicationId;
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, e.getErrorHandler());
        }
    }

    @Override
    public ApplicationToken generateApplicationToken(String clientId, String clientSecret, String scopes,
                                                     long validityPeriod, String tokenToBeRevoked)
            throws APIManagementException {
        log.debug("Generating a new application access token");
        AccessTokenRequest accessTokenRequest = new AccessTokenRequest();
        accessTokenRequest.setClientId(clientId);
        accessTokenRequest.setClientSecret(clientSecret);
        accessTokenRequest.setGrantType(KeyManagerConstants.CLIENT_CREDENTIALS_GRANT_TYPE);
        if (StringUtils.isEmpty(scopes)) {
            scopes = KeyManagerConstants.OAUTH2_DEFAULT_SCOPE;
        }
        accessTokenRequest.setScopes(scopes);
        accessTokenRequest.setValidityPeriod(validityPeriod);
        accessTokenRequest.setTokenToRevoke(tokenToBeRevoked);

        AccessTokenInfo newToken = getKeyManager().getNewAccessToken(accessTokenRequest);

        ApplicationToken applicationToken = new ApplicationToken();
        applicationToken.setAccessToken(newToken.getAccessToken());
        applicationToken.setValidityPeriod(newToken.getValidityPeriod());
        applicationToken.setScopes(newToken.getScopes());

        log.debug("Successfully created a new application access token.");
        return applicationToken;
    }

    @Override
    public Application getApplicationByUuid(String uuid) throws APIManagementException {
        Application application;
        try {
            application = getApplicationDAO().getApplication(uuid);
        } catch (APIMgtDAOException e) {
            String errorMsg = "Error occurred while retrieving application - " + uuid;
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, e.getErrorHandler());
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
            throw new APIManagementException(errorMsg, e, e.getErrorHandler());
        }

        return subscriptionsList;
    }

    @Override
    public List<Subscription> getAPISubscriptionsByApplication(Application application, ApiType apiType)
            throws APIManagementException {
        List<Subscription> subscriptionsList;
        try {
            subscriptionsList = getApiSubscriptionDAO().getAPISubscriptionsByApplication(application.getId(), apiType);
        } catch (APIMgtDAOException e) {
            String errorMsg = "Error occurred while retrieving subscriptions for application - "
                    + application.getName() + " to API Type " + apiType.toString();
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, e.getErrorHandler());
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
                throw new APIManagementException(errorMsg, ExceptionCodes.API_NOT_FOUND);
            }
            Application application = getApplicationByUuid(applicationId);

            if (application == null) {
                String errorMsg = "Cannot find an application for given applicationId - " + applicationId;
                log.error(errorMsg);
                throw new APIManagementException(errorMsg, ExceptionCodes.APPLICATION_NOT_FOUND);
            }
            Policy policy = getPolicyDAO().getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel
                    .subscription, tier);
            if (policy == null) {
                String errorMsg = "Cannot find an subscription policy for given policy name - " + tier;
                log.error(errorMsg);
                throw new APIManagementException(errorMsg, ExceptionCodes.POLICY_NOT_FOUND);
            }
            getApiSubscriptionDAO().addAPISubscription(subscriptionId, apiId, applicationId, policy.getUuid(),
                    APIMgtConstants.SubscriptionStatus.ON_HOLD);

            WorkflowExecutor addSubscriptionWFExecutor = WorkflowExecutorFactory.getInstance()
                    .getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION);
            //Instead of quering the db, we create same subscription object
            Subscription subscription = new Subscription(subscriptionId, application, api, policy);
            subscription.setStatus(APIMgtConstants.SubscriptionStatus.ON_HOLD);

            SubscriptionCreationWorkflow workflow = new SubscriptionCreationWorkflow(getApiSubscriptionDAO(),
                    getWorkflowDAO(), getApiGateway());

            workflow.setCreatedTime(LocalDateTime.now());
            workflow.setExternalWorkflowReference(UUID.randomUUID().toString());
            workflow.setWorkflowReference(subscriptionId);
            workflow.setWorkflowType(WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION);
            workflow.setSubscription(subscription);
            workflow.setSubscriber(getUsername());

            String workflowDescription = "Subscription creation workflow for the subscription to api "
                    + subscription.getApi().getName() + ":" + subscription.getApi().getVersion() + ":"
                    + subscription.getApi().getProvider() + " using application "
                    + subscription.getApplication().getName() + " with tier " + subscription.getPolicy()
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
            throw new APIManagementException(errorMsg, e, e.getErrorHandler());
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

                SubscriptionDeletionWorkflow workflow = new SubscriptionDeletionWorkflow(getApiSubscriptionDAO(),
                        getWorkflowDAO(), getApiGateway());
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
                        + subscription.getApplication().getName() + " with tier " + subscription.getPolicy()
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
            throw new APIManagementException(errorMsg, e, e.getErrorHandler());
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
            throw new APIManagementException(errorMsg, e, e.getErrorHandler());
        }
        return tagList;
    }

    @Override
    public List<Policy> getPolicies(APIMgtAdminService.PolicyLevel policyLevel) throws APIManagementException {
        List<Policy> policyList = null;
        try {
            policyList = getPolicyDAO().getPoliciesByLevel(policyLevel);
        } catch (APIMgtDAOException e) {
            String errorMsg = "Error occurred while retrieving policies for policy level - " + policyLevel;
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, e.getErrorHandler());
        }
        return policyList;
    }

    @Override
    public Policy getPolicy(APIMgtAdminService.PolicyLevel policyLevel, String policyName)
            throws APIManagementException {
        Policy policy;
        try {
            policy = getPolicyDAO().getPolicyByLevelAndName(policyLevel, policyName);
        } catch (APIMgtDAOException e) {
            String errorMsg = "Error occurred while retrieving policy - " + policyName;
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, e.getErrorHandler());
        }
        return policy;
    }

    @Override
    public List<Label> getLabelInfo(List<String> labels, String username) throws LabelException {

        List<Label> filteredLabels;
        String labelExtractorClassName = getConfig().getLabelExtractorImplClass();
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
     * Check if api exists
     *
     * @param apiId UUID of the api
     * @throws APIMgtResourceNotFoundException if API does not exist
     * @throws APIMgtDAOException if error occurred while accessing data layer
     */
    public void checkIfApiExists(String apiId) throws APIMgtResourceNotFoundException, APIMgtDAOException {
        ApiDAO apiDAO = getApiDAO();
        try {
            API api = apiDAO.getAPI(apiId);
            if (api == null) {
                String errorMsg = "api not found for the id : " + apiId;
                log.error(errorMsg);
                throw new APIMgtResourceNotFoundException(errorMsg, ExceptionCodes.API_NOT_FOUND);
            }
        } catch (APIMgtDAOException e) {
            String errorMsg =
                    "Error occurred while checking if api exists for api_id " + apiId;
            log.error(errorMsg, e);
            throw new APIMgtDAOException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
    }

    @Override
    public String addComment(Comment comment, String apiId) throws APICommentException,
            APIMgtResourceNotFoundException {
        validateCommentMaxCharacterLength(comment.getCommentText());
        String generatedUuid = UUID.randomUUID().toString();
        comment.setUuid(generatedUuid);
        try {
            checkIfApiExists(apiId);
            getApiDAO().addComment(comment, apiId);
        } catch (APIMgtDAOException e) {
            String errorMsg = "Error occurred while adding comment for api - " + apiId;
            log.error(errorMsg, e);
            throw new APICommentException(errorMsg, e, e.getErrorHandler());
        }
        return comment.getUuid();
    }

    @Override
    public void deleteComment(String commentId, String apiId, String username) throws APICommentException,
            APIMgtResourceNotFoundException {
        try {
            ApiDAO apiDAO = getApiDAO();
            checkIfApiExists(apiId);
            Comment comment = apiDAO.getCommentByUUID(commentId, apiId);
            if (comment != null) {
                // if the delete operation is done by a user who isn't the owner of the comment
                if (!comment.getCommentedUser().equals(username)) {
                    checkIfUserIsCommentModerator(username);
                }
                apiDAO.deleteComment(commentId, apiId);
            } else {
                String errorMsg = "Couldn't find comment with comment_id : " + commentId;
                log.error(errorMsg);
                throw new APIMgtResourceNotFoundException(errorMsg, ExceptionCodes.COMMENT_NOT_FOUND);
            }
        } catch (APIMgtDAOException e) {
            String errorMsg = "Error occurred while deleting comment " + commentId;
            log.error(errorMsg, e);
            throw new APICommentException(errorMsg, e, e.getErrorHandler());
        }
    }

    @Override
    public void updateComment(Comment comment, String commentId, String apiId, String username) throws
            APICommentException, APIMgtResourceNotFoundException {
        validateCommentMaxCharacterLength(comment.getCommentText());
        try {
            checkIfApiExists(apiId);
            Comment oldComment = getApiDAO().getCommentByUUID(commentId, apiId);
            if (oldComment != null) {
                // if the update operation is done by a user who isn't the owner of the comment
                if (!oldComment.getCommentedUser().equals(username)) {
                    checkIfUserIsCommentModerator(username);
                }
                getApiDAO().updateComment(comment, commentId, apiId);
            } else {
                String errorMsg = "Couldn't find comment with comment_id : " + commentId + "and api_id : " + apiId;
                log.error(errorMsg);
                throw new APIMgtResourceNotFoundException(errorMsg, ExceptionCodes.COMMENT_NOT_FOUND);
            }
        } catch (APIMgtDAOException e) {
            String errorMsg = "Error occurred while updating comment " + commentId;
            log.error(errorMsg, e);
            throw new APICommentException(errorMsg, e, e.getErrorHandler());
        }

    }

    /**
     * Check whether current user is a comment moderator
     *
     * @param username username of the user
     * @throws APICommentException if user does not have comment moderator role
     */
    private void checkIfUserIsCommentModerator(String username) throws APICommentException {
        Set<String> roles = APIUtils.getAllRolesOfUser(username);
        if (roles.contains(getConfig().getCommentModeratorRole())) {
            return;
        }
        String errorMsg = "comment moderator permission needed";
        log.error(errorMsg);
        throw new APICommentException(errorMsg, ExceptionCodes.NEED_COMMENT_MODERATOR_PERMISSION);
    }

    /**
     * Validate whether the rating value provided by user is positive and less than or equal to the max rating in config
     *
     * @param ratingValue rating value provided by user
     * @throws APIRatingException if rating value is negative or larger than max rating
     */
    private void validateMaxMinRatingValue(int ratingValue) throws APIRatingException {
        if (ratingValue > 0 && ratingValue <= getConfig().getRatingMaxValue()) {
            return;
        }
        String errorMsg = "Provided rating value is invalid";
        log.error(errorMsg);
        throw new APIRatingException(errorMsg, ExceptionCodes.RATING_VALUE_INVALID);
    }

    /**
     * Validates the comment length is less than or equal to max comment length in config
     *
     * @param commentText comment text
     * @throws APICommentException if comment length is larger than max length allowed
     */
    private void validateCommentMaxCharacterLength(String commentText) throws APICommentException {
        if (commentText.length() <= getConfig().getCommentMaxLength()) {
            return;
        }
        String errorMsg = "comment text exceeds allowed maximum length of characters";
        log.error(errorMsg);
        throw new APICommentException(errorMsg, ExceptionCodes.COMMENT_LENGTH_EXCEEDED);
    }



    @Override
    public List<Comment> getCommentsForApi(String apiId) throws APICommentException, APIMgtResourceNotFoundException {
        try {
            checkIfApiExists(apiId);
            List<Comment> commentList = getApiDAO().getCommentsForApi(apiId);
            return commentList;
        } catch (APIMgtDAOException e) {
            String errorMsg = "Error occurred while retrieving comments for api " + apiId;
            log.error(errorMsg, e);
            throw new APICommentException(errorMsg, e, e.getErrorHandler());
        }
    }

    @Override
    public Comment getCommentByUUID(String commentId, String apiId) throws APICommentException,
            APIMgtResourceNotFoundException {
        Comment comment;
        try {
            checkIfApiExists(apiId);
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
            throw new APICommentException(errorMsg, e, e.getErrorHandler());
        }
        return comment;
    }


    @Override
    public String addRating(String apiId, Rating rating) throws APIRatingException, APIMgtResourceNotFoundException {
        try {
            validateMaxMinRatingValue(rating.getRating());
            checkIfApiExists(apiId);
            String generatedUuid = UUID.randomUUID().toString();
            rating.setUuid(generatedUuid);
            getApiDAO().addRating(apiId, rating);
            return rating.getUuid();
        } catch (APIMgtDAOException e) {
            String errorMsg = "Error occurred while adding rating for user " + getUsername() + " for api " + apiId;
            log.error(errorMsg, e);
            throw new APIRatingException(errorMsg, e, e.getErrorHandler());
        }
    }

    @Override
    public void updateRating(String apiId, String ratingId, Rating ratingFromPayload) throws APIRatingException,
            APIMgtResourceNotFoundException {
        try {
            validateMaxMinRatingValue(ratingFromPayload.getRating());
            checkIfApiExists(apiId);
            getApiDAO().updateRating(apiId, ratingId, ratingFromPayload);
        } catch (APIMgtDAOException e) {
            String errorMsg = "Error occurred while updating rating for user " + getUsername() + " for api " + apiId;
            log.error(errorMsg, e);
            throw new APIRatingException(errorMsg, e, e.getErrorHandler());
        }
    }

    @Override
    public Rating getRatingForApiFromUser(String apiId, String userId) throws APIRatingException,
            APIMgtResourceNotFoundException {
        try {
            checkIfApiExists(apiId);
            Rating userRatingForApi = getApiDAO().getUserRatingForApiFromUser(apiId, userId);
            return userRatingForApi;
        } catch (APIMgtDAOException e) {
            String errorMsg = "Error occurred while retrieving ratings for user " + userId + " for api " + apiId;
            log.error(errorMsg, e);
            throw new APIRatingException(errorMsg, e, e.getErrorHandler());
        }
    }

    @Override
    public Rating getRatingByUUID(String apiId, String ratingId) throws APIRatingException,
            APIMgtResourceNotFoundException {
        Rating rating;
        try {
            checkIfApiExists(apiId);
            rating = getApiDAO().getRatingByUUID(apiId, ratingId);
            if (rating == null) {
                String errorMsg = "Couldn't find rating with rating id - " + ratingId + " for api_id " + apiId;
                log.error(errorMsg);
                throw new APIMgtResourceNotFoundException(errorMsg, ExceptionCodes.RATING_NOT_FOUND);
            }
        } catch (APIMgtDAOException e) {
            String errorMsg =
                    "Error occurred while retrieving rating for rating id " + ratingId + " for api_id " + apiId;
            log.error(errorMsg, e);
            throw new APIRatingException(errorMsg, e, e.getErrorHandler());
        }
        return rating;
    }

    @Override
    public double getAvgRating(String apiId) throws APIRatingException, APIMgtResourceNotFoundException {
        try {
            checkIfApiExists(apiId);
            return getApiDAO().getAverageRating(apiId);
        } catch (APIMgtDAOException e) {
            String errorMsg =
                    "Error occurred while retrieving average rating for api_id " + apiId;
            log.error(errorMsg, e);
            throw new APIRatingException(errorMsg, e, e.getErrorHandler());
        }
    }

    @Override
    public List<Rating> getRatingsListForApi(String apiId) throws APIRatingException, APIMgtResourceNotFoundException {
        try {
            checkIfApiExists(apiId);
            List<Rating> ratingsListForApi = getApiDAO().getRatingsListForApi(apiId);
            return ratingsListForApi;
        } catch (APIMgtDAOException e) {
            String errorMsg =
                    "Error occurred while retrieving ratings list for api_id " + apiId;
            log.error(errorMsg, e);
            throw new APIRatingException(errorMsg, e, e.getErrorHandler());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String addCompositeApi(CompositeAPI.Builder apiBuilder) throws APIManagementException {
        apiBuilder.provider(getUsername());
        if (StringUtils.isEmpty(apiBuilder.getId())) {
            apiBuilder.id(UUID.randomUUID().toString());
        }

        LocalDateTime localDateTime = LocalDateTime.now();
        apiBuilder.createdTime(localDateTime);
        apiBuilder.lastUpdatedTime(localDateTime);
        apiBuilder.createdBy(getUsername());
        apiBuilder.updatedBy(getUsername());

        if (!isApiNameExist(apiBuilder.getName()) && !isContextExist(apiBuilder.getContext())) {
            setUriTemplates(apiBuilder);
            setGatewayDefinitionSource(apiBuilder);

            if (StringUtils.isEmpty(apiBuilder.getApiDefinition())) {
                apiBuilder.apiDefinition(apiDefinitionFromSwagger20.generateSwaggerFromResources(apiBuilder));
            }

            try {
                CompositeAPI createdAPI = apiBuilder.build();
                APIUtils.validate(createdAPI);

                //publishing config to gateway
                gateway.addCompositeAPI(createdAPI);

                getApiDAO().addApplicationAssociatedAPI(createdAPI);

                if (log.isDebugEnabled()) {
                    log.debug("API " + createdAPI.getName() + "-" + createdAPI.getVersion() + " was created " +
                            "successfully.", log);
                }

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

    private void setUriTemplates(CompositeAPI.Builder apiBuilder) {
        Map<String, UriTemplate> uriTemplateMap = new HashMap();
        if (apiBuilder.getUriTemplates() == null || apiBuilder.getUriTemplates().isEmpty()) {
            apiBuilder.uriTemplates(APIUtils.getDefaultUriTemplates());
            apiBuilder.apiDefinition(apiDefinitionFromSwagger20.generateSwaggerFromResources(apiBuilder));
        } else {
            for (UriTemplate uriTemplate : apiBuilder.getUriTemplates().values()) {
                UriTemplate.UriTemplateBuilder uriTemplateBuilder = new UriTemplate.UriTemplateBuilder
                        (uriTemplate);
                if (StringUtils.isEmpty(uriTemplateBuilder.getTemplateId())) {
                    uriTemplateBuilder.templateId(APIUtils.generateOperationIdFromPath(uriTemplate
                            .getUriTemplate(), uriTemplate.getHttpVerb()));
                }

                uriTemplateMap.put(uriTemplateBuilder.getTemplateId(), uriTemplateBuilder.build());
            }
            apiBuilder.uriTemplates(uriTemplateMap);
        }

    }

    private void setGatewayDefinitionSource(CompositeAPI.Builder apiBuilder) throws APIManagementException {
        List<UriTemplate> list = new ArrayList<>(apiBuilder.getUriTemplates().values());
        List<TemplateBuilderDTO> resourceList = new ArrayList<>();

        String appId = null;
        List<CompositeAPIEndpointDTO> endpointDTOs = new ArrayList<CompositeAPIEndpointDTO>();
        try {
            appId = apiBuilder.getApplicationId();
            List<Subscription> subscriptions = getApiSubscriptionDAO().getAPISubscriptionsByApplication(
                    apiBuilder.getApplicationId(), ApiType.STANDARD);
            for (Subscription subscription : subscriptions) {
                CompositeAPIEndpointDTO endpointDTO = new CompositeAPIEndpointDTO();
                API api = subscription.getApi();
                endpointDTO.setEndpointName(api.getName());
                // TODO: currently only HTTPS endpoint considered. Websocket APIs and http transport should considered
                endpointDTO.setTransportType(APIMgtConstants.HTTPS);
                // TODO: replace host with gateway domain host
                String endpointUrl = APIMgtConstants.HTTPS + "://" + config.getHostname() + "/" + api.getContext()
                        + "/" + api.getVersion();
                endpointDTO.setEndpointUrl(endpointUrl);
                endpointDTOs.add(endpointDTO);
            }
        } catch (APIMgtDAOException e) {
            String errorMsg = "Error while getting subscriptions of the application " + appId;
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, e.getErrorHandler());
        }

        for (UriTemplate uriTemplate : list) {
            TemplateBuilderDTO dto = new TemplateBuilderDTO();
            dto.setTemplateId(uriTemplate.getTemplateId());
            dto.setUriTemplate(uriTemplate.getUriTemplate());
            dto.setHttpVerb(uriTemplate.getHttpVerb());
            resourceList.add(dto);
        }
        GatewaySourceGenerator gatewaySourceGenerator = getGatewaySourceGenerator();
        APIConfigContext apiConfigContext = new APIConfigContext(apiBuilder.build(), config.getGatewayPackageName());

        gatewaySourceGenerator.setApiConfigContext(apiConfigContext);
        String gatewayConfig = gatewaySourceGenerator.getCompositeAPIConfigStringFromTemplate(resourceList,
                endpointDTOs);
        if (log.isDebugEnabled()) {
            log.debug("API " + apiBuilder.getName() + "gateway config: " + gatewayConfig);
        }
        apiBuilder.gatewayConfig(gatewayConfig);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateCompositeApi(CompositeAPI.Builder apiBuilder) throws APIManagementException {
        apiBuilder.provider(getUsername());
        apiBuilder.updatedBy(getUsername());

        CompositeAPI originalAPI = getApiDAO().getCompositeAPI(apiBuilder.getId());
        if (originalAPI != null) {
            apiBuilder.createdTime(originalAPI.getCreatedTime());
            //workflow status is an internal property and shouldn't be allowed to update externally
            apiBuilder.workflowStatus(originalAPI.getWorkflowStatus());

            APIUtils.verifyValidityOfApiUpdate(apiBuilder, originalAPI);

            try (InputStream gatewayConfig = getApiDAO().getCompositeAPIGatewayConfig(apiBuilder.getId())) {
                String updatedSwagger = apiDefinitionFromSwagger20.generateSwaggerFromResources(apiBuilder);
                GatewaySourceGenerator gatewaySourceGenerator = getGatewaySourceGenerator();
                APIConfigContext apiConfigContext = new APIConfigContext(apiBuilder.build(), config
                        .getGatewayPackageName());

                gatewaySourceGenerator.setApiConfigContext(apiConfigContext);
                String updatedGatewayConfig = gatewaySourceGenerator
                        .getGatewayConfigFromSwagger(IOUtils.toString(gatewayConfig, StandardCharsets.UTF_8),
                                updatedSwagger);

                CompositeAPI api = apiBuilder.build();

                if (originalAPI.getContext() != null && !originalAPI.getContext().equals(apiBuilder.getContext())) {
                    if (isContextExist(api.getContext())) {
                        throw new APIManagementException("Context already Exist", ExceptionCodes
                                .API_ALREADY_EXISTS);
                    }
                }

                //publishing config to gateway
                gateway.addCompositeAPI(api);

                getApiDAO().updateApiDefinition(api.getId(), updatedSwagger, api.getUpdatedBy());
                getApiDAO().updateCompositeAPIGatewayConfig(api.getId(),
                        new ByteArrayInputStream(updatedGatewayConfig.getBytes(StandardCharsets.UTF_8)),
                        api.getUpdatedBy());

                if (log.isDebugEnabled()) {
                    log.debug("API " + api.getName() + "-" + api.getVersion() + " was updated successfully.");
                }

            } catch (APIMgtDAOException e) {
                String errorMsg = "Error occurred while updating the API - " + apiBuilder.getName();
                log.error(errorMsg, e);
                throw new APIManagementException(errorMsg, e, e.getErrorHandler());
            } catch (IOException e) {
                String errorMsg = "Error occurred while reading gateway configuration the API - " +
                        apiBuilder.getName();
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
            CompositeAPI api = getApiDAO().getCompositeAPI(apiId);

            if (api != null) {
                //Delete API in gateway
                gateway.deleteCompositeAPI(api);
                getApiDAO().deleteCompositeApi(apiId);
            }
        } catch (GatewayException e) {
            String message = "Error occurred while deleting Composite API with id - " + apiId + " from gateway";
            throw new APIManagementException(message, e, ExceptionCodes.GATEWAY_EXCEPTION);
        } catch (APIMgtDAOException e) {
            String errorMsg = "Error occurred while deleting the Composite API with id " + apiId;
            throw new APIManagementException(errorMsg, e, e.getErrorHandler());
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
            CompositeAPI api = getApiDAO().getCompositeAPI(apiId);
            if (api != null) {
                if (api.getVersion().equals(newVersion)) {
                    String errMsg = "New API version " + newVersion + " cannot be same as the previous version for " +
                            "API " + api.getName();
                    log.error(errMsg);
                    throw new APIManagementException(errMsg, ExceptionCodes.API_ALREADY_EXISTS);
                }
                CompositeAPI.Builder apiBuilder = new CompositeAPI.Builder(api);
                apiBuilder.id(UUID.randomUUID().toString());
                apiBuilder.version(newVersion);
                apiBuilder.context(api.getContext().replace(api.getVersion(), newVersion));
                apiBuilder.copiedFromApiId(api.getId());
                if (StringUtils.isEmpty(apiBuilder.getApiDefinition())) {
                    apiBuilder.apiDefinition(apiDefinitionFromSwagger20.generateSwaggerFromResources(apiBuilder));
                }
                getApiDAO().addApplicationAssociatedAPI(apiBuilder.build());
                newVersionedId = apiBuilder.getId();
            } else {
                throw new APIMgtResourceNotFoundException("Requested API on UUID " + apiId + "Couldn't be found");
            }
        } catch (APIMgtDAOException e) {
            String errorMsg = "Couldn't create new API version from " + apiId;
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, e.getErrorHandler());
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
            CompositeAPI.Builder apiBuilder = apiDefinitionFromSwagger20.
                    generateCompositeApiFromSwaggerResource(getUsername(), apiDefinitionString);
            apiBuilder.apiDefinition(apiDefinitionString);
            addCompositeApi(apiBuilder);
            return apiBuilder.getId();
        } catch (IOException e) {
            throw new APIManagementException("Couldn't Generate ApiDefinition from file", e, ExceptionCodes
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
                CompositeAPI.Builder apiBuilder = apiDefinitionFromSwagger20.
                        generateCompositeApiFromSwaggerResource(getUsername(), responseStr);
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

    @Override
    public String getAPIWSDL(String apiId, String labelName)
            throws APIMgtDAOException, APIMgtWSDLException, APINotFoundException, LabelException {
        API api = getApiDAO().getAPI(apiId);
        if (api == null) {
            throw new APINotFoundException("API with id " + apiId + " not found.", ExceptionCodes.API_NOT_FOUND);
        }

        //api.getLabels() should not be null and the labels should contain labelName
        if ((api.getLabels() == null || !api.getLabels().contains(labelName))) {
            throw new LabelException("API with id " + apiId + " does not contain label " + labelName,
                    ExceptionCodes.LABEL_NOT_FOUND_IN_API);
        }

        String wsdl = getApiDAO().getWSDL(apiId);
        Label label = getLabelDAO().getLabelByName(labelName);

        if (!StringUtils.isEmpty(wsdl)) {
            WSDLProcessor processor;
            try {
                processor = WSDLProcessFactory.getInstance()
                        .getWSDLProcessor(wsdl.getBytes(APIMgtConstants.ENCODING_UTF_8));
                return new String(processor.getUpdatedWSDL(api, label), APIMgtConstants.ENCODING_UTF_8);
            } catch (UnsupportedEncodingException e) {
                throw new APIMgtWSDLException("WSDL content is not in utf-8 encoding", e,
                        ExceptionCodes.CANNOT_PROCESS_WSDL_CONTENT);
            }
        }
        return null;
    }

    @Override
    public WSDLArchiveInfo getAPIWSDLArchive(String apiId, String labelName)
            throws APIMgtDAOException, APIMgtWSDLException, APINotFoundException, LabelException {
        API api = getApiDAO().getAPI(apiId);
        if (api == null) {
            throw new APINotFoundException("API with id " + apiId + " not found.", ExceptionCodes.API_NOT_FOUND);
        }

        //api.getLabels() should not be null and the labels should contain labelName
        if ((api.getLabels() == null || !api.getLabels().contains(labelName))) {
            throw new LabelException("API with id " + apiId + " does not contain label " + labelName,
                    ExceptionCodes.LABEL_NOT_FOUND_IN_API);
        }

        try (InputStream wsdlZipInputStream = getApiDAO().getWSDLArchive(apiId)) {
            String rootPath = System.getProperty(APIMgtConstants.JAVA_IO_TMPDIR)
                    + File.separator + APIMgtConstants.WSDLConstants.WSDL_ARCHIVES_FOLDERNAME
                    + File.separator + UUID.randomUUID().toString();
            String archivePath = rootPath + File.separator + APIMgtConstants.WSDLConstants.WSDL_ARCHIVE_FILENAME;
            String extractedLocation = APIFileUtils.extractUploadedArchive(wsdlZipInputStream,
                    APIMgtConstants.WSDLConstants.EXTRACTED_WSDL_ARCHIVE_FOLDERNAME, archivePath, rootPath);
            if (log.isDebugEnabled()) {
                log.debug("Successfully extracted WSDL archive in path: " + extractedLocation);
            }
            Label label = getLabelDAO().getLabelByName(labelName);
            WSDLProcessor processor = WSDLProcessFactory.getInstance().getWSDLProcessorForPath(extractedLocation);
            String wsdlPath = processor.getUpdatedWSDLPath(api, label);
            if (log.isDebugEnabled()) {
                log.debug("Successfully updated WSDLs in path [" + extractedLocation + "] with endpoints of label: "
                        + labelName + " and context of API " + api.getContext());
            }
            String wsdlArchiveProcessedFileName =
                    api.getProvider() + "-" + api.getName() + "-" + api.getVersion() + "-" + labelName + "-wsdl";
            APIFileUtils.archiveDirectory(wsdlPath, rootPath, wsdlArchiveProcessedFileName);
            if (log.isDebugEnabled()) {
                log.debug("Successfully archived WSDL files: " + wsdlPath);
            }
            WSDLArchiveInfo archiveInfo = new WSDLArchiveInfo(rootPath, wsdlArchiveProcessedFileName + ".zip");
            archiveInfo.setWsdlInfo(processor.getWsdlInfo());
            return archiveInfo;
        } catch (IOException e) {
            throw new APIMgtWSDLException(e);
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
                String searchAttribute, searchValue;
                if (!query.contains(":")) {
                    isFullTextSearch = true;
                } else {
                    searchAttribute = attributes[0].split(":")[0];
                    searchValue = attributes[0].split(":")[1];
                    attributeMap.put(searchAttribute, searchValue);
                }

                if (isFullTextSearch) {
                    apiResults = getApiDAO().searchAPIs(roles, user, query, offset, limit);
                } else {
                    apiResults = getApiDAO().searchAPIsByAttributeInStore(new ArrayList<>(roles),
                            attributeMap, offset, limit);
                }
            } else {
                List<String> statuses = new ArrayList<>();
                statuses.add(APIStatus.PUBLISHED.getStatus());
                statuses.add(APIStatus.PROTOTYPED.getStatus());
                apiResults = getApiDAO().getAPIsByStatus(roles, statuses);
            }

        } catch (APIMgtDAOException e) {
            String errorMsg = "Error occurred while updating searching APIs - " + query;
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, e.getErrorHandler());
        }

        return apiResults;
    }

    @Override
    public List<CompositeAPI> searchCompositeAPIs(String query, int offset, int limit) throws APIManagementException {
        List<CompositeAPI> apiResults;

        //this should be current logged in user
        String user = getUsername();
        //role list of current user
        Set<String> roles = APIUtils.getAllRolesOfUser(user);
        try {
            if (query != null && !query.isEmpty()) {
                apiResults = getApiDAO().searchCompositeAPIs(roles, user, query, offset, limit);
            } else {
                apiResults = getApiDAO().getCompositeAPIs(roles, user, offset, limit);
            }
        } catch (APIMgtDAOException e) {
            String errorMsg = "Error occurred while updating searching APIs - " + query;
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, e.getErrorHandler());
        }

        return apiResults;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCompositeApiDefinition(String id) throws APIManagementException {
        return getApiDAO().getCompositeApiSwaggerDefinition(id);
    }

    /**
     * {@inheritDoc}
     * <p>Implementation will try to retrieve the summary of API to decide if API exist or not</p>
     */
    @Override
    public boolean isCompositeAPIExist(String apiId) throws APIManagementException {
        boolean status;
        try {
            CompositeAPI compositeAPI = getApiDAO().getCompositeAPISummary(apiId);
            status = compositeAPI != null;
        } catch (APIMgtDAOException e) {
            String errorMsg = "Couldn't get APISummary for Composite API: " + apiId;
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, e.getErrorHandler());
        }
        return status;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateCompositeApiDefinition(String id, String apiDefinition) throws APIManagementException {
        getApiDAO().updateApiDefinition(id, apiDefinition, getUsername());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream getCompositeApiImplementation(String id) throws APIManagementException {
        return getApiDAO().getCompositeAPIGatewayConfig(id);
    }

    @Override
    public void updateCompositeApiImplementation(String id, InputStream implementation) throws APIManagementException {
        getApiDAO().updateCompositeAPIGatewayConfig(id, implementation, getUsername());
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

            ApplicationDeletionWorkflow workflow = new ApplicationDeletionWorkflow(getApplicationDAO(),
                    getWorkflowDAO(), getApiGateway());
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
            throw new APIManagementException(errorMsg, e, e.getErrorHandler());
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
            Policy tier = application.getPolicy();
            if (tier == null) {
                String message = "Tier name cannot be null - " + application.getName();
                log.error(message);
                throw new APIManagementException(message, ExceptionCodes.TIER_CANNOT_BE_NULL);
            } else {
                Policy policy = getPolicyDAO().getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel
                        .application, tier.getPolicyName());
                if (policy == null) {
                    String message = "Specified tier " + tier.getPolicyName() + " is invalid";
                    log.error(message);
                    throw new APIManagementException(message, ExceptionCodes.TIER_CANNOT_BE_NULL);
                }
                application.setPolicy(policy);
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

            ApplicationCreationWorkflow workflow = new ApplicationCreationWorkflow(getApplicationDAO(),
                    getWorkflowDAO(), getApiGateway());

            workflow.setApplication(application);
            workflow.setCreatedBy(getUsername());
            workflow.setWorkflowReference(application.getId());
            workflow.setExternalWorkflowReference(UUID.randomUUID().toString());
            workflow.setCreatedTime(LocalDateTime.now());

            String workflowDescription = "Application creation workflow for " + application.getName() + " with tier "
                    + tier.getPolicyName() + " by " + getUsername();
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
            throw new APIManagementException(errorMsg, e, e.getErrorHandler());
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

    @Override
    public void selfSignUp(User user) throws APIManagementException {
        getIdentityProvider().registerUser(user);
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


}

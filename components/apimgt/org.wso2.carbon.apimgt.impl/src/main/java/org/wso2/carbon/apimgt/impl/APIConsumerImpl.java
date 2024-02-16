/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl;

import org.apache.axis2.util.JavaUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIAdmin;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIMgtAuthorizationFailedException;
import org.wso2.carbon.apimgt.api.APIMgtResourceNotFoundException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.WorkflowResponse;
import org.wso2.carbon.apimgt.api.dto.KeyManagerConfigurationDTO;
import org.wso2.carbon.apimgt.api.dto.KeyManagerPermissionConfigurationDTO;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIKey;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.APIProductIdentifier;
import org.wso2.carbon.apimgt.api.model.APIRating;
import org.wso2.carbon.apimgt.api.model.APIRevisionDeployment;
import org.wso2.carbon.apimgt.api.model.AccessTokenInfo;
import org.wso2.carbon.apimgt.api.model.AccessTokenRequest;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.ApplicationConstants;
import org.wso2.carbon.apimgt.api.model.ApplicationKeysDTO;
import org.wso2.carbon.apimgt.api.model.Comment;
import org.wso2.carbon.apimgt.api.model.CommentList;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.api.model.Documentation.DocumentSourceType;
import org.wso2.carbon.apimgt.api.model.Documentation.DocumentVisibility;
import org.wso2.carbon.apimgt.api.model.DocumentationContent;
import org.wso2.carbon.apimgt.api.model.DocumentationType;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.api.model.Identifier;
import org.wso2.carbon.apimgt.api.model.KeyManager;
import org.wso2.carbon.apimgt.api.model.KeyManagerConfiguration;
import org.wso2.carbon.apimgt.api.model.Label;
import org.wso2.carbon.apimgt.api.model.Monetization;
import org.wso2.carbon.apimgt.api.model.OAuthAppRequest;
import org.wso2.carbon.apimgt.api.model.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.api.model.ResourceFile;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.api.model.SubscriptionResponse;
import org.wso2.carbon.apimgt.api.model.Tag;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.api.model.TierPermission;
import org.wso2.carbon.apimgt.api.model.VHost;
import org.wso2.carbon.apimgt.api.model.policy.PolicyConstants;
import org.wso2.carbon.apimgt.api.model.webhooks.Subscription;
import org.wso2.carbon.apimgt.api.model.webhooks.Topic;
import org.wso2.carbon.apimgt.impl.caching.CacheProvider;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.definitions.OASParserUtil;
import org.wso2.carbon.apimgt.impl.dto.ApplicationDTO;
import org.wso2.carbon.apimgt.impl.dto.ApplicationRegistrationWorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.ApplicationWorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.JwtTokenInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.SubscriptionWorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.TierPermissionDTO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.monetization.DefaultMonetizationImpl;
import org.wso2.carbon.apimgt.impl.notifier.events.ApplicationEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.ApplicationRegistrationEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.SubscriptionEvent;
import org.wso2.carbon.apimgt.impl.publishers.RevocationRequestPublisher;
import org.wso2.carbon.apimgt.impl.recommendationmgt.RecommendationEnvironment;
import org.wso2.carbon.apimgt.impl.recommendationmgt.RecommenderDetailsExtractor;
import org.wso2.carbon.apimgt.impl.recommendationmgt.RecommenderEventPublisher;
import org.wso2.carbon.apimgt.impl.token.ApiKeyGenerator;
import org.wso2.carbon.apimgt.impl.utils.APIAPIProductNameComparator;
import org.wso2.carbon.apimgt.impl.utils.APIMWSDLReader;
import org.wso2.carbon.apimgt.impl.utils.APINameComparator;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.utils.APIVersionComparator;
import org.wso2.carbon.apimgt.impl.utils.ApplicationUtils;
import org.wso2.carbon.apimgt.impl.utils.ContentSearchResultNameComparator;
import org.wso2.carbon.apimgt.impl.utils.VHostUtils;
import org.wso2.carbon.apimgt.impl.workflow.ApplicationDeletionApprovalWorkflowExecutor;
import org.wso2.carbon.apimgt.impl.workflow.GeneralWorkflowResponse;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowConstants;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowException;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowExecutor;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowExecutorFactory;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowStatus;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowUtils;
import org.wso2.carbon.apimgt.impl.wsdl.WSDLProcessor;
import org.wso2.carbon.apimgt.impl.wsdl.model.WSDLValidationResponse;
import org.wso2.carbon.apimgt.persistence.dto.DevPortalAPI;
import org.wso2.carbon.apimgt.persistence.dto.DevPortalAPIInfo;
import org.wso2.carbon.apimgt.persistence.dto.DevPortalAPISearchResult;
import org.wso2.carbon.apimgt.persistence.dto.DevPortalContentSearchResult;
import org.wso2.carbon.apimgt.persistence.dto.DevPortalSearchContent;
import org.wso2.carbon.apimgt.persistence.dto.DocumentSearchContent;
import org.wso2.carbon.apimgt.persistence.dto.Organization;
import org.wso2.carbon.apimgt.persistence.dto.SearchContent;
import org.wso2.carbon.apimgt.persistence.dto.UserContext;
import org.wso2.carbon.apimgt.persistence.exceptions.APIPersistenceException;
import org.wso2.carbon.apimgt.persistence.exceptions.OASPersistenceException;
import org.wso2.carbon.apimgt.persistence.mapper.APIMapper;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.mgt.UserAdmin;
import org.wso2.carbon.user.mgt.common.UserAdminException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.cache.Cache;

/**
 * This class provides the core API store functionality. It is implemented in a very
 * self-contained and 'pure' manner, without taking requirements like security into account,
 * which are subject to frequent change. Due to this 'pure' nature and the significance of
 * the class to the overall API management functionality, the visibility of the class has
 * been reduced to package level. This means we can still use it for internal purposes and
 * possibly even extend it, but it's totally off the limits of the users. Users wishing to
 * programmatically access this functionality should use one of the extensions of this
 * class which is visible to them. These extensions may add additional features like
 * security to this class.
 */
public class APIConsumerImpl extends AbstractAPIManager implements APIConsumer {

    private static final Log log = LogFactory.getLog(APIConsumerImpl.class);
    private RecommendationEnvironment recommendationEnvironment;

    private static final Log audit = CarbonConstants.AUDIT_LOG;
    public static final char COLON_CHAR = ':';
    public static final String EMPTY_STRING = "";

    public static final String ENVIRONMENT_NAME = "environmentName";
    public static final String ENVIRONMENT_TYPE = "environmentType";
    public static final String API_NAME = "apiName";
    public static final String API_VERSION = "apiVersion";
    public static final String API_PROVIDER = "apiProvider";
    private static final String PERMISSION_ALLOW = "ALLOW";
    private static final String PERMISSION_DENY = "DENY";
    private static final String PERMISSION_NOT_RESTRICTED = "PUBLIC";
    private static final String PRESERVED_CASE_SENSITIVE_VARIABLE = "preservedCaseSensitive";

    private static final String GET_SUB_WORKFLOW_REF_FAILED = "Failed to get external workflow reference for " +
            "subscription ";
    public static final String CLEAN_PENDING_SUB_APPROVAL_TASK_FAILED = "Failed to clean pending subscription " +
     "approval task: ";

    /* Map to Store APIs against Tag */
    private ConcurrentMap<String, Set<API>> taggedAPIs = new ConcurrentHashMap<String, Set<API>>();
    private boolean isTenantModeStoreView;
    private String requestedTenant;
    private boolean isTagCacheEnabled;
    private Set<Tag> tagSet;
    private long tagCacheValidityTime;
    private volatile long lastUpdatedTime;
    private final Object tagCacheMutex = new Object();
    protected String userNameWithoutChange;

    public APIConsumerImpl() throws APIManagementException {

        super();
        readTagCacheConfigs();
    }

    public APIConsumerImpl(String username) throws APIManagementException {

        super(username);
        userNameWithoutChange = username;
        readTagCacheConfigs();
        readRecommendationConfigs();
    }

    public APIConsumerImpl(String username, String organization) throws APIManagementException {

        super(username, organization);
        userNameWithoutChange = username;
        readTagCacheConfigs();
        readRecommendationConfigs();
    }

    private void readRecommendationConfigs() {

        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                .getAPIManagerConfiguration();
        recommendationEnvironment = config.getApiRecommendationEnvironment();
    }

    private void readTagCacheConfigs() {

        APIManagerConfiguration config = getAPIManagerConfiguration();
        String enableTagCache = config.getFirstProperty(APIConstants.STORE_TAG_CACHE_DURATION);
        if (enableTagCache == null) {
            isTagCacheEnabled = false;
            tagCacheValidityTime = 0;
        } else {
            isTagCacheEnabled = true;
            tagCacheValidityTime = Long.parseLong(enableTagCache);
        }
    }

    @Override
    public Subscriber getSubscriber(String subscriberId) throws APIManagementException {

        Subscriber subscriber = null;
        try {
            subscriber = apiMgtDAO.getSubscriber(subscriberId);
        } catch (APIManagementException e) {
            handleException("Failed to get Subscriber", e);
        }
        return subscriber;
    }

    protected void setUsernameToThreadLocalCarbonContext(String username) {

        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(username);
    }

    protected int getTenantId(String requestedTenantDomain) throws UserStoreException {

        return ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                .getTenantId(requestedTenantDomain);
    }

    /**
     * Regenerate consumer secret.
     *
     * @param clientId       For which consumer key we need to regenerate consumer secret.
     * @param keyManagerName
     * @return New consumer secret.
     * @throws APIManagementException This is the custom exception class for API management.
     */
    public String renewConsumerSecret(String clientId, String keyManagerName) throws APIManagementException {
        // Create Token Request with parameters provided from UI.
        AccessTokenRequest tokenRequest = new AccessTokenRequest();
        tokenRequest.setClientId(clientId);

        KeyManagerConfigurationDTO keyManagerConfigurationDTO =
                apiMgtDAO.getKeyManagerConfigurationByName(tenantDomain, keyManagerName);
        if (keyManagerConfigurationDTO == null) {
            keyManagerConfigurationDTO = apiMgtDAO.getKeyManagerConfigurationByUUID(keyManagerName);
            if (keyManagerConfigurationDTO != null) {
                keyManagerName = keyManagerConfigurationDTO.getName();
            } else {
                log.error("Key Manager: " + keyManagerName + " not found in database.");
                throw new APIManagementException("Key Manager " + keyManagerName + " not found in database.",
                        ExceptionCodes.KEY_MANAGER_NOT_FOUND);
            }
        }

        KeyManager keyManager = KeyManagerHolder.getKeyManagerInstance(tenantDomain, keyManagerName);
        return keyManager.getNewApplicationConsumerSecret(tokenRequest);
    }

    /**
     * Re-generates the access token.
     *
     * @param oldAccessToken  Token to be revoked
     * @param clientId        Consumer Key for the Application
     * @param clientSecret    Consumer Secret for the Application
     * @param validityTime    Desired Validity time for the token
     * @param requestedScopes Requested Scopes
     * @param jsonInput       Additional parameters if Authorization server needs any.
     * @param keyManagerName  Configured Key Manager
     * @param grantType       Grant Type
     * @return
     * @throws APIManagementException
     */
    @Override
    public AccessTokenInfo renewAccessToken(String oldAccessToken, String clientId, String clientSecret,
                                            String validityTime, String[] requestedScopes, String jsonInput,
                                            String keyManagerName, String grantType) throws APIManagementException {
        // Create Token Request with parameters provided from UI.
        AccessTokenRequest tokenRequest = new AccessTokenRequest();
        tokenRequest.setClientId(clientId);
        tokenRequest.setClientSecret(clientSecret);
        tokenRequest.setValidityPeriod(Long.parseLong(validityTime));
        tokenRequest.setTokenToRevoke(oldAccessToken);
        tokenRequest.setScope(requestedScopes);
        tokenRequest.setGrantType(grantType);

        try {
            // Populating additional parameters.
            KeyManagerConfigurationDTO keyManagerConfiguration =
                    apiMgtDAO.getKeyManagerConfigurationByUUID(keyManagerName);
            String keyManagerTenant = tenantDomain;
            if (keyManagerConfiguration != null) {
                keyManagerName = keyManagerConfiguration.getName();
                keyManagerTenant = keyManagerConfiguration.getOrganization();
            } else {
                //keeping this just in case the name is sent by mistake.
                keyManagerConfiguration =
                        apiMgtDAO.getKeyManagerConfigurationByName(tenantDomain, keyManagerName);
                if (keyManagerConfiguration == null) {
                    throw new APIManagementException("Key Manager " + keyManagerName + " couldn't found.",
                            ExceptionCodes.KEY_MANAGER_NOT_REGISTERED);
                }
            }
            if (keyManagerConfiguration.isEnabled()) {
                Object enableTokenGeneration =
                        keyManagerConfiguration.getProperty(APIConstants.KeyManager.ENABLE_TOKEN_GENERATION);
                if (enableTokenGeneration != null && !(Boolean) enableTokenGeneration) {
                    throw new APIManagementException(
                            "Key Manager didn't support to generate token Generation From portal",
                            ExceptionCodes.KEY_MANAGER_NOT_SUPPORTED_TOKEN_GENERATION);
                }
                KeyManager keyManager = KeyManagerHolder.getKeyManagerInstance(keyManagerTenant, keyManagerName);
                if (keyManager == null) {
                    throw new APIManagementException("Key Manager " + keyManagerName + " not initialized",
                            ExceptionCodes.KEY_MANAGER_INITIALIZATION_FAILED);
                }
                tokenRequest = ApplicationUtils.populateTokenRequest(keyManager, jsonInput, tokenRequest);

                JSONObject appLogObject = new JSONObject();
                appLogObject.put("Re-Generated Keys for application with client Id", clientId);
                APIUtil.logAuditMessage(APIConstants.AuditLogConstants.APPLICATION, appLogObject.toString(),
                        APIConstants.AuditLogConstants.UPDATED, this.username);

                return keyManager.getNewApplicationAccessToken(tokenRequest);
            } else {
                throw new APIManagementException("Key Manager " + keyManagerName + " not enabled",
                        ExceptionCodes.KEY_MANAGER_NOT_ENABLED);
            }
        } catch (APIManagementException e) {
            log.error("Error while re-generating AccessToken", e);
            throw e;
        }
    }

    @Override
    public String generateApiKey(Application application, String userName, long validityPeriod,
                                 String permittedIP, String permittedReferer) throws APIManagementException {

        JwtTokenInfoDTO jwtTokenInfoDTO = APIUtil.getJwtTokenInfoDTO(application, userName,
                MultitenantUtils.getTenantDomain(userName));

        ApplicationDTO applicationDTO = new ApplicationDTO();
        applicationDTO.setId(application.getId());
        applicationDTO.setName(application.getName());
        applicationDTO.setOwner(application.getOwner());
        applicationDTO.setTier(application.getTier());
        applicationDTO.setUuid(application.getUUID());
        jwtTokenInfoDTO.setApplication(applicationDTO);

        jwtTokenInfoDTO.setSubscriber(userName);
        jwtTokenInfoDTO.setExpirationTime(validityPeriod);
        jwtTokenInfoDTO.setKeyType(application.getKeyType());
        jwtTokenInfoDTO.setPermittedIP(permittedIP);
        jwtTokenInfoDTO.setPermittedReferer(permittedReferer);

        ApiKeyGenerator apiKeyGenerator = loadApiKeyGenerator();
        if (apiKeyGenerator != null) {
            return apiKeyGenerator.generateToken(jwtTokenInfoDTO);
        } else {
            throw new APIManagementException("Failed to generate the API key");
        }
    }

    private ApiKeyGenerator loadApiKeyGenerator() throws APIManagementException {

        ApiKeyGenerator apiKeyGenerator;
        String keyGeneratorClassName = APIUtil.getApiKeyGeneratorImpl();

        try {
            Class keyGeneratorClass = APIConsumerImpl.class.getClassLoader().loadClass(keyGeneratorClassName);
            Constructor constructor = keyGeneratorClass.getDeclaredConstructor();
            apiKeyGenerator = (ApiKeyGenerator) constructor.newInstance();
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            throw new APIManagementException("Error while loading the api key generator class: "
                    + keyGeneratorClassName, e);
        }
        return apiKeyGenerator;
    }

    @Override
    public Set<Tag> getAllTags(String organization) throws APIManagementException {

        /* We keep track of the lastUpdatedTime of the TagCache to determine its freshness.
         */
        long lastUpdatedTimeAtStart = lastUpdatedTime;
        long currentTimeAtStart = System.currentTimeMillis();
        if (isTagCacheEnabled && ((currentTimeAtStart - lastUpdatedTimeAtStart) < tagCacheValidityTime)) {
            if (tagSet != null) {
                return tagSet;
            }
        }
        Organization org = new Organization(organization);
        String userName = (userNameWithoutChange != null) ? userNameWithoutChange : username;
        String[] roles = APIUtil.getListOfRoles(userName);
        Map<String, Object> properties = APIUtil.getUserProperties(userName);
        UserContext userCtx = new UserContext(userNameWithoutChange, org, properties, roles);
        try {
            Set<Tag> tempTagSet = apiPersistenceInstance.getAllTags(org, userCtx);
            synchronized (tagCacheMutex) {
                lastUpdatedTime = System.currentTimeMillis();
                this.tagSet = tempTagSet;
            }
        } catch (APIPersistenceException e) {
            String msg = "Failed to get API tags";
            throw new APIManagementException(msg, e);
        }
        return tagSet;
    }

    @Override
    public void rateAPI(String id, APIRating rating, String user) throws APIManagementException {

        apiMgtDAO.addRating(id, rating.getRating(), user);
    }

    @Override
    public void removeAPIRating(String id, String user) throws APIManagementException {

        apiMgtDAO.removeAPIRating(id, user);
    }

    @Override
    public int getUserRating(String uuid, String user) throws APIManagementException {

        return apiMgtDAO.getUserRating(uuid, user);
    }

    @Override
    public JSONObject getUserRatingInfo(String id, String user) throws APIManagementException {

        JSONObject obj = apiMgtDAO.getUserRatingInfo(id, user);
        if (obj == null || obj.isEmpty()) {
            String msg = "Failed to get API ratings for API with UUID " + id + " for user " + user;
            log.error(msg);
            throw new APIMgtResourceNotFoundException(msg);
        }
        return obj;
    }

    @Override
    public JSONArray getAPIRatings(String apiId) throws APIManagementException {

        return apiMgtDAO.getAPIRatings(apiId);
    }

    @Override
    public float getAverageAPIRating(String apiId) throws APIManagementException {

        return apiMgtDAO.getAverageRating(apiId);
    }

    @Override
    public boolean isSubscribedToApp(APIIdentifier apiIdentifier, String userId, int applicationId) throws
            APIManagementException {

        boolean isSubscribed;
        try {
            isSubscribed = apiMgtDAO.isSubscribedToApp(apiIdentifier, userId, applicationId);
        } catch (APIManagementException e) {
            String msg = "Failed to check if user(" + userId + ") with appId " + applicationId + " has subscribed to "
                    + apiIdentifier;
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        }
        return isSubscribed;
    }

    /**
     * This method will delete application key mapping table and application registration table.
     *
     * @param applicationName application Name
     * @param tokenType       Token Type.
     * @param groupId         group id.
     * @param userName        user name.
     * @return
     * @throws APIManagementException
     */
    @Override
    public void cleanUpApplicationRegistration(String applicationName, String tokenType, String groupId, String
            userName) throws APIManagementException {

        Application application = apiMgtDAO.getApplicationByName(applicationName, userName, groupId);
        cleanUpApplicationRegistrationByApplicationId(application.getId(), tokenType);
    }

    /*
     * @see super.cleanUpApplicationRegistrationByApplicationId
     * */
    @Override
    public void cleanUpApplicationRegistrationByApplicationId(int applicationId, String tokenType) throws APIManagementException {

        apiMgtDAO.deleteApplicationRegistration(applicationId, tokenType, APIConstants.KeyManager.DEFAULT_KEY_MANAGER);
        apiMgtDAO.deleteApplicationKeyMappingByApplicationIdAndType(applicationId, tokenType);
        apiMgtDAO.getConsumerkeyByApplicationIdAndKeyType(applicationId, tokenType);
    }

    /**
     * @param jsonString     this string will contain oAuth app details
     * @param userName       user name of logged in user.
     * @param clientId       this is the consumer key of oAuthApplication
     * @param application    the Application Object that represents the Application.
     * @param keyType
     * @param tokenType      this is theApplication Token Type. This can be either default or jwt.
     * @param keyManagerName key Manager name
     * @return
     * @throws APIManagementException
     */
    @Override
    public Map<String, Object> mapExistingOAuthClient(String jsonString, String userName, String clientId,
                                                      Application application, String keyType, String tokenType,
                                                      String keyManagerName, String tenantDomain) throws APIManagementException {

        String callBackURL = null;
        String applicationName = application.getName();
        if (StringUtils.isEmpty(tenantDomain)) {
            tenantDomain = MultitenantUtils.getTenantDomain(userName);
        } else {
            int tenantId = APIUtil.getTenantIdFromTenantDomain(tenantDomain);
            if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
                tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
            }
        }
        String keyManagerId = null;
        KeyManagerConfigurationDTO keyManagerConfiguration = null;
        if (keyManagerName != null) {
            keyManagerConfiguration = apiMgtDAO.getKeyManagerConfigurationByName(tenantDomain, keyManagerName);
            if (keyManagerConfiguration == null) {
                keyManagerConfiguration = apiMgtDAO.getKeyManagerConfigurationByUUID(keyManagerName);
                if (keyManagerConfiguration != null) {
                    keyManagerId = keyManagerName;
                    keyManagerName = keyManagerConfiguration.getName();
                }
            } else {
                keyManagerId = keyManagerConfiguration.getUuid();
            }
        } else {
            keyManagerName = APIConstants.KeyManager.DEFAULT_KEY_MANAGER;
            keyManagerConfiguration = apiMgtDAO.getKeyManagerConfigurationByName(tenantDomain, keyManagerName);
            keyManagerId = keyManagerConfiguration.getUuid();
        }
        if (keyManagerConfiguration == null || !keyManagerConfiguration.isEnabled()) {
            throw new APIManagementException(
                    "Key Manager " + keyManagerName + " doesn't exist in Tenant " + tenantDomain,
                    ExceptionCodes.KEY_MANAGER_NOT_REGISTERED);
        }
        if (KeyManagerConfiguration.TokenType.EXCHANGED.toString().equals(keyManagerConfiguration.getTokenType())) {
            throw new APIManagementException("Key Manager " + keyManagerName + " doesn't support to generate" +
                    " Client Application", ExceptionCodes.KEY_MANAGER_NOT_SUPPORT_OAUTH_APP_CREATION);
        }
        OAuthAppRequest oauthAppRequest = ApplicationUtils
                .createOauthAppRequest(applicationName, clientId, callBackURL, "default", jsonString, tokenType,
                        tenantDomain, keyManagerName);

        // if clientId is null in the argument `ApplicationUtils#createOauthAppRequest` will set it using
        // the props in `jsonString`. Hence we are taking the updated `clientId` here
        clientId = oauthAppRequest.getOAuthApplicationInfo().getClientId();

        KeyManager keyManager = KeyManagerHolder.getKeyManagerInstance(tenantDomain, keyManagerName);
        if (keyManager == null) {
            throw new APIManagementException(
                    "Key Manager " + keyManagerName + "Couldn't initialized in tenant " + tenantDomain + ".",
                    ExceptionCodes.KEY_MANAGER_NOT_REGISTERED);
        }

        //Get application ID
        int applicationId = apiMgtDAO.getApplicationId(applicationName, userName);

        // Checking if clientId is mapped with another application.
        if (apiMgtDAO.isKeyMappingExistsForConsumerKeyOrApplication(applicationId, keyManagerName, keyManagerId,
                keyType, clientId)) {
            throw new APIManagementException("Key Mappings already exists for application " + applicationName
                    + " or consumer key " + clientId, ExceptionCodes.KEY_MAPPING_ALREADY_EXIST);
        }
        if (log.isDebugEnabled()) {
            log.debug("Client ID " + clientId + " not mapped previously with another application. No existing "
                    + "key mappings available for application " + applicationName);
        }
        //createApplication on oAuthorization server.
        OAuthApplicationInfo oAuthApplication = isOauthAppValidation() ?
                keyManager.mapOAuthApplication(oauthAppRequest) : oauthAppRequest.getOAuthApplicationInfo();

        //Do application mapping with consumerKey.
        String keyMappingId = UUID.randomUUID().toString();
        apiMgtDAO.createApplicationKeyTypeMappingForManualClients(keyType, applicationId, clientId, keyManagerId,
                keyMappingId);
        Object enableTokenGeneration =
                keyManager.getKeyManagerConfiguration().getParameter(APIConstants.KeyManager.ENABLE_TOKEN_GENERATION);

        AccessTokenInfo tokenInfo;
        if (enableTokenGeneration != null && (Boolean) enableTokenGeneration &&
                oAuthApplication.getJsonString().contains(APIConstants.GRANT_TYPE_CLIENT_CREDENTIALS)) {
            AccessTokenRequest tokenRequest =
                    ApplicationUtils.createAccessTokenRequest(keyManager, oAuthApplication, null);
            tokenInfo = keyManager.getNewApplicationAccessToken(tokenRequest);
        } else {
            tokenInfo = new AccessTokenInfo();
            tokenInfo.setAccessToken("");
            tokenInfo.setValidityPeriod(0L);
            String[] noScopes = new String[]{"N/A"};
            tokenInfo.setScope(noScopes);
            oAuthApplication.addParameter("tokenScope", Arrays.toString(noScopes));
        }

        Map<String, Object> keyDetails = new HashMap<String, Object>();

        if (tokenInfo != null) {
            keyDetails.put("validityTime", tokenInfo.getValidityPeriod());
            keyDetails.put("accessToken", tokenInfo.getAccessToken());
            keyDetails.put("tokenDetails", tokenInfo.getJSONString());
        }

        keyDetails.put(APIConstants.FrontEndParameterNames.CONSUMER_KEY, oAuthApplication.getClientId());
        keyDetails.put(APIConstants.FrontEndParameterNames.CONSUMER_SECRET, oAuthApplication.getParameter(
                "client_secret"));
        keyDetails.put(APIConstants.FrontEndParameterNames.CLIENT_DETAILS, oAuthApplication.getJsonString());
        keyDetails.put(APIConstants.FrontEndParameterNames.KEY_MAPPING_ID, keyMappingId);
        keyDetails.put(APIConstants.FrontEndParameterNames.MODE, APIConstants.OAuthAppMode.MAPPED.name());

        ApplicationRegistrationEvent applicationRegistrationEvent = new ApplicationRegistrationEvent(
                UUID.randomUUID().toString(), System.currentTimeMillis(),
                APIConstants.EventType.APPLICATION_REGISTRATION_CREATE.name(), tenantId, tenantDomain,
                application.getId(), application.getUUID(), oAuthApplication.getClientId(), keyType,
                keyManagerName);
        APIUtil.sendNotification(applicationRegistrationEvent,
                APIConstants.NotifierType.APPLICATION_REGISTRATION.name());

        return keyDetails;
    }

    /**
     * returns the SubscribedAPI object which is related to the subscriptionId
     *
     * @param subscriptionId subscription id
     * @return
     * @throws APIManagementException
     */
    @Override
    public SubscribedAPI getSubscriptionById(int subscriptionId) throws APIManagementException {

        return apiMgtDAO.getSubscriptionById(subscriptionId);
    }

    public Set<SubscribedAPI> getSubscribedAPIs(String organization, Subscriber subscriber, String groupingId)
            throws APIManagementException {

        Set<SubscribedAPI> originalSubscribedAPIs;
        Set<SubscribedAPI> subscribedAPIs = new HashSet<SubscribedAPI>();
        try {
            originalSubscribedAPIs = apiMgtDAO.getSubscribedAPIs(organization, subscriber, groupingId);
            if (originalSubscribedAPIs != null && !originalSubscribedAPIs.isEmpty()) {
                Map<String, Tier> tiers = APIUtil.getTiers(organization);
                for (SubscribedAPI subscribedApi : originalSubscribedAPIs) {
                    Tier tier = tiers.get(subscribedApi.getTier().getName());
                    subscribedApi.getTier().setDisplayName(tier != null ? tier.getDisplayName() :
                     subscribedApi.getTier().getName());
                    subscribedAPIs.add(subscribedApi);
                }
            }
        } catch (APIManagementException e) {
            handleException("Failed to get APIs of " + subscriber.getName(), e);
        }
        return subscribedAPIs;
    }

    private Set<SubscribedAPI> getLightWeightSubscribedAPIs(String organization, Subscriber subscriber,
     String groupingId) throws
            APIManagementException {

        Set<SubscribedAPI> originalSubscribedAPIs;
        Set<SubscribedAPI> subscribedAPIs = new HashSet<SubscribedAPI>();
        try {
            originalSubscribedAPIs = apiMgtDAO.getSubscribedAPIs(organization, subscriber, groupingId);
            if (originalSubscribedAPIs != null && !originalSubscribedAPIs.isEmpty()) {
                Map<String, Tier> tiers = APIUtil.getTiers(tenantId);
                for (SubscribedAPI subscribedApi : originalSubscribedAPIs) {
                    Application application = subscribedApi.getApplication();
                    if (application != null) {
                        int applicationId = application.getId();
                    }
                    Tier tier = tiers.get(subscribedApi.getTier().getName());
                    subscribedApi.getTier().setDisplayName(tier != null ? tier.getDisplayName() : subscribedApi
                            .getTier().getName());
                    subscribedAPIs.add(subscribedApi);
                }
            }
        } catch (APIManagementException e) {
            handleException("Failed to get APIs of " + subscriber.getName(), e);
        }
        return subscribedAPIs;
    }

    @Override
    public Set<SubscribedAPI> getSubscribedAPIs(Subscriber subscriber, String applicationName, String groupingId)
            throws APIManagementException {

        Set<SubscribedAPI> subscribedAPIs = null;
        try {
            subscribedAPIs = apiMgtDAO.getSubscribedAPIs(subscriber, applicationName, groupingId);
            if (subscribedAPIs != null && !subscribedAPIs.isEmpty()) {
                Map<String, Tier> tiers = APIUtil.getTiers(tenantId);
                for (SubscribedAPI subscribedApi : subscribedAPIs) {
                    Tier tier = tiers.get(subscribedApi.getTier().getName());
                    subscribedApi.getTier().setDisplayName(tier != null ? tier.getDisplayName() : subscribedApi
                            .getTier().getName());
                    // We do not need to add the modified object again.
                }
            }
        } catch (APIManagementException e) {
            handleException("Failed to get APIs of " + subscriber.getName() + " under application " + applicationName
            , e);
        }
        return subscribedAPIs;
    }

    public Set<Scope> getScopesForApplicationSubscription(String username, int applicationId, String organization)
            throws APIManagementException {

        Subscriber subscriber = new Subscriber(username);
        Set<String> scopeKeySet = apiMgtDAO.getScopesForApplicationSubscription(subscriber, applicationId);
        return new LinkedHashSet<>(APIUtil.getScopes(scopeKeySet, organization).values());
    }

    public Integer getSubscriptionCount(Subscriber subscriber, String applicationName, String groupingId)
            throws APIManagementException {

        return apiMgtDAO.getSubscriptionCount(subscriber, applicationName, groupingId);
    }

    @Override
    public boolean isSubscribed(APIIdentifier apiIdentifier, String userId)
            throws APIManagementException {

        boolean isSubscribed;
        try {
            isSubscribed = apiMgtDAO.isSubscribed(apiIdentifier, userId);
        } catch (APIManagementException e) {
            String msg = "Failed to check if user(" + userId + ") has subscribed to " + apiIdentifier;
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        }
        return isSubscribed;
    }

    /**
     * This methods loads the monetization implementation class
     *
     * @return monetization implementation class
     * @throws APIManagementException if failed to load monetization implementation class
     */
    public Monetization getMonetizationImplClass() throws APIManagementException {

        APIManagerConfiguration configuration = getAPIManagerConfiguration();
        Monetization monetizationImpl = null;
        if (configuration == null) {
            log.error("API Manager configuration is not initialized.");
        } else {
            String monetizationImplClass = configuration.getMonetizationConfigurationDto().getMonetizationImpl();
            if (monetizationImplClass == null) {
                monetizationImpl = new DefaultMonetizationImpl();
            } else {
                try {
                    monetizationImpl = (Monetization) APIUtil.getClassInstance(monetizationImplClass);
                } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                    APIUtil.handleException("Failed to load monetization implementation class.", e);
                }
            }
        }
        return monetizationImpl;
    }

    @Override
    public SubscriptionResponse addSubscription(ApiTypeWrapper apiTypeWrapper, String userId, Application application)
            throws APIManagementException {

        API api = null;
        APIProduct product = null;
        Identifier identifier = null;
        int apiId;
        String apiUUID;
        final boolean isApiProduct = apiTypeWrapper.isAPIProduct();
        String state;
        String apiContext;
        String apiOrgId;

        if (isApiProduct) {
            product = apiTypeWrapper.getApiProduct();
            state = product.getState();
            identifier = product.getId();
            apiId = product.getProductId();
            apiUUID = product.getUuid();
            apiContext = product.getContext();
            apiOrgId = product.getOrganization();
        } else {
            api = apiTypeWrapper.getApi();
            state = api.getStatus();
            identifier = api.getId();
            apiId = api.getId().getId();
            apiUUID = api.getUuid();
            apiContext = api.getContext();
            apiOrgId = api.getOrganization();
        }

        WorkflowResponse workflowResponse = null;
        String tenantAwareUsername = MultitenantUtils.getTenantAwareUsername(userId);
        checkSubscriptionAllowed(apiTypeWrapper);
        int subscriptionId;
        if (APIConstants.PUBLISHED.equals(state) || APIConstants.PROTOTYPED.equals(state)) {
            subscriptionId = apiMgtDAO.addSubscription(apiTypeWrapper, application,
                    APIConstants.SubscriptionStatus.ON_HOLD, tenantAwareUsername);

            boolean isTenantFlowStarted = false;
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = startTenantFlowForTenantDomain(tenantDomain);
            }

            String applicationName = application.getName();

            try {
                WorkflowExecutor addSubscriptionWFExecutor =
                        getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION);

                SubscriptionWorkflowDTO workflowDTO = new SubscriptionWorkflowDTO();
                workflowDTO.setStatus(WorkflowStatus.CREATED);
                workflowDTO.setCreatedTime(System.currentTimeMillis());
                workflowDTO.setTenantDomain(tenantDomain);
                workflowDTO.setTenantId(tenantId);
                workflowDTO.setExternalWorkflowReference(addSubscriptionWFExecutor.generateUUID());
                workflowDTO.setWorkflowReference(String.valueOf(subscriptionId));
                workflowDTO.setWorkflowType(WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION);
                workflowDTO.setCallbackUrl(addSubscriptionWFExecutor.getCallbackURL());
                workflowDTO.setApiName(identifier.getName());
                workflowDTO.setApiContext(apiContext);
                workflowDTO.setApiVersion(identifier.getVersion());
                workflowDTO.setApiProvider(identifier.getProviderName());
                workflowDTO.setTierName(identifier.getTier());
                workflowDTO.setRequestedTierName(identifier.getTier());
                workflowDTO.setApplicationName(applicationName);
                workflowDTO.setApplicationId(application.getId());
                workflowDTO.setSubscriber(userId);

                Tier tier = null;
                Set<Tier> policies = Collections.emptySet();
                if (!isApiProduct) {
                    policies = api.getAvailableTiers();
                } else {
                    policies = product.getAvailableTiers();
                }

                for (Tier policy : policies) {
                    if (policy.getName() != null && (policy.getName()).equals(workflowDTO.getTierName())) {
                        tier = policy;
                    }
                }
                boolean isMonetizationEnabled = false;

                if (api != null) {
                    isMonetizationEnabled = api.getMonetizationStatus();
                    //check whether monetization is enabled for API and tier plan is commercial
                    if (isMonetizationEnabled && APIConstants.COMMERCIAL_TIER_PLAN.equals(tier.getTierPlan())) {
                        workflowResponse = addSubscriptionWFExecutor.monetizeSubscription(workflowDTO, api);
                    } else {
                        workflowResponse = addSubscriptionWFExecutor.execute(workflowDTO);
                    }
                } else {
                    isMonetizationEnabled = product.getMonetizationStatus();
                    //check whether monetization is enabled for API and tier plan is commercial
                    if (isMonetizationEnabled && APIConstants.COMMERCIAL_TIER_PLAN.equals(tier.getTierPlan())) {
                        workflowResponse = addSubscriptionWFExecutor.monetizeSubscription(workflowDTO, product);
                    } else {
                        workflowResponse = addSubscriptionWFExecutor.execute(workflowDTO);
                    }
                }
            } catch (WorkflowException e) {
                //If the workflow execution fails, roll back transaction by removing the subscription entry.
                apiMgtDAO.removeSubscriptionById(subscriptionId);
                log.error("Could not execute Workflow", e);
                throw new APIManagementException("Could not execute Workflow", e);
            } finally {
                if (isTenantFlowStarted) {
                    endTenantFlow();
                }
            }

            //to handle on-the-fly subscription rejection (and removal of subscription entry from the database)
            //the response should have {"Status":"REJECTED"} in the json payload for this to work.
            boolean subscriptionRejected = false;
            String subscriptionStatus = null;
            String subscriptionUUID = "";
            SubscribedAPI addedSubscription = getSubscriptionById(subscriptionId);

            if (workflowResponse != null && workflowResponse.getJSONPayload() != null
                    && !workflowResponse.getJSONPayload().isEmpty()) {
                try {
                    JSONObject wfResponseJson = (JSONObject) new JSONParser().parse(workflowResponse.getJSONPayload());
                    if (APIConstants.SubscriptionStatus.REJECTED.equals(wfResponseJson.get("Status"))) {
                        subscriptionRejected = true;
                        subscriptionStatus = APIConstants.SubscriptionStatus.REJECTED;
                    }
                } catch (ParseException e) {
                    log.error('\'' + workflowResponse.getJSONPayload() + "' is not a valid JSON.", e);
                }
            }

            if (!subscriptionRejected) {
                subscriptionStatus = addedSubscription.getSubStatus();
                subscriptionUUID = addedSubscription.getUUID();

                JSONObject subsLogObject = new JSONObject();
                subsLogObject.put(APIConstants.AuditLogConstants.API_NAME, identifier.getName());
                subsLogObject.put(APIConstants.AuditLogConstants.PROVIDER, identifier.getProviderName());
                subsLogObject.put(APIConstants.AuditLogConstants.APPLICATION_ID, application.getId());
                subsLogObject.put(APIConstants.AuditLogConstants.APPLICATION_NAME, applicationName);
                subsLogObject.put(APIConstants.AuditLogConstants.TIER, identifier.getTier());

                APIUtil.logAuditMessage(APIConstants.AuditLogConstants.SUBSCRIPTION, subsLogObject.toString(),
                        APIConstants.AuditLogConstants.CREATED, this.username);

                if (workflowResponse == null) {
                    workflowResponse = new GeneralWorkflowResponse();
                }

            }
            // get the workflow state once the executor is executed.
            WorkflowDTO wfDTO = apiMgtDAO.retrieveWorkflowFromInternalReference(Integer.toString(subscriptionId),
                    WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION);
            // only send the notification if approved
            // wfDTO is null when simple wf executor is used because wf state is not stored in the db and is always
            // approved.
            int tenantId = APIUtil.getTenantId(APIUtil.replaceEmailDomainBack(identifier.getProviderName()));
            String tenantDomain = MultitenantUtils
                    .getTenantDomain(APIUtil.replaceEmailDomainBack(identifier.getProviderName()));
            if (wfDTO != null) {
                if (WorkflowStatus.APPROVED.equals(wfDTO.getStatus())) {
                    SubscriptionEvent subscriptionEvent = new SubscriptionEvent(UUID.randomUUID().toString(),
                            System.currentTimeMillis(), APIConstants.EventType.SUBSCRIPTIONS_CREATE.name(), tenantId,
                            apiOrgId, subscriptionId, addedSubscription.getUUID(), apiId, apiUUID,
                            application.getId(), application.getUUID(), identifier.getTier(), subscriptionStatus,
                            identifier.getName(), identifier.getVersion());
                    APIUtil.sendNotification(subscriptionEvent, APIConstants.NotifierType.SUBSCRIPTIONS.name());
                }
            } else {
                SubscriptionEvent subscriptionEvent = new SubscriptionEvent(UUID.randomUUID().toString(),
                        System.currentTimeMillis(), APIConstants.EventType.SUBSCRIPTIONS_CREATE.name(), tenantId,
                        apiOrgId, subscriptionId, addedSubscription.getUUID(), apiId, apiUUID,
                        application.getId(), application.getUUID(), identifier.getTier(), subscriptionStatus,
                        identifier.getName(), identifier.getVersion());
                APIUtil.sendNotification(subscriptionEvent, APIConstants.NotifierType.SUBSCRIPTIONS.name());
            }

            if (log.isDebugEnabled()) {
                String logMessage = "API Name: " + identifier.getName() + ", API Version " + identifier.getVersion()
                        + ", Subscription Status: " + subscriptionStatus + " subscribe by " + userId + " for app "
                        + applicationName;
                log.debug(logMessage);
            }
            return new SubscriptionResponse(subscriptionStatus, subscriptionUUID, workflowResponse);
        } else {
            throw new APIMgtResourceNotFoundException("Subscriptions not allowed on APIs/API Products in the state: " +
                    state);
        }
    }

    @Override
    public SubscriptionResponse updateSubscription(ApiTypeWrapper apiTypeWrapper, String userId,
                                                   Application application, String inputSubscriptionId,
                                                   String currentThrottlingPolicy, String requestedThrottlingPolicy)
            throws APIManagementException {

        API api = null;
        APIProduct product = null;
        Identifier identifier = null;
        int apiId;
        String apiUUId;
        final boolean isApiProduct = apiTypeWrapper.isAPIProduct();
        String state;
        String apiContext;
        String apiOrgId;

        if (isApiProduct) {
            product = apiTypeWrapper.getApiProduct();
            state = product.getState();
            apiId = product.getProductId();
            apiUUId = product.getUuid();
            identifier = product.getId();
            apiContext = product.getContext();
            apiOrgId = product.getOrganization();
        } else {
            api = apiTypeWrapper.getApi();
            state = api.getStatus();
            identifier = api.getId();
            apiId = identifier.getId();
            apiUUId = api.getUuid();
            apiContext = api.getContext();
            apiOrgId = api.getOrganization();
        }
        checkSubscriptionAllowed(apiTypeWrapper);
        WorkflowResponse workflowResponse = null;
        int subscriptionId;
        if (APIConstants.PUBLISHED.equals(state) || APIConstants.PROTOTYPED.equals(state)) {
            subscriptionId = apiMgtDAO.updateSubscription(apiTypeWrapper, inputSubscriptionId,
                    APIConstants.SubscriptionStatus.TIER_UPDATE_PENDING, requestedThrottlingPolicy);

            boolean isTenantFlowStarted = false;
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = startTenantFlowForTenantDomain(tenantDomain);
            }

            try {
                WorkflowExecutor updateSubscriptionWFExecutor =
                        getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_UPDATE);

                SubscriptionWorkflowDTO workflowDTO = new SubscriptionWorkflowDTO();
                workflowDTO.setStatus(WorkflowStatus.CREATED);
                workflowDTO.setCreatedTime(System.currentTimeMillis());
                workflowDTO.setTenantDomain(tenantDomain);
                workflowDTO.setTenantId(tenantId);
                workflowDTO.setExternalWorkflowReference(updateSubscriptionWFExecutor.generateUUID());
                workflowDTO.setWorkflowReference(String.valueOf(subscriptionId));
                workflowDTO.setWorkflowType(WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_UPDATE);
                workflowDTO.setCallbackUrl(updateSubscriptionWFExecutor.getCallbackURL());
                workflowDTO.setApiName(identifier.getName());
                workflowDTO.setApiContext(apiContext);
                workflowDTO.setApiVersion(identifier.getVersion());
                workflowDTO.setApiProvider(identifier.getProviderName());
                workflowDTO.setTierName(identifier.getTier());
                workflowDTO.setRequestedTierName(requestedThrottlingPolicy);
                workflowDTO.setApplicationName(application.getName());
                workflowDTO.setApplicationId(application.getId());
                workflowDTO.setSubscriber(userId);

                Tier tier = null;
                Set<Tier> policies = Collections.emptySet();
                if (!isApiProduct) {
                    policies = api.getAvailableTiers();
                } else {
                    policies = product.getAvailableTiers();
                }

                for (Tier policy : policies) {
                    if (policy.getName() != null && (policy.getName()).equals(workflowDTO.getTierName())) {
                        tier = policy;
                    }
                }
                boolean isMonetizationEnabled = false;

                if (api != null) {
                    isMonetizationEnabled = api.getMonetizationStatus();
                    //check whether monetization is enabled for API and tier plan is commercial
                    if (isMonetizationEnabled && APIConstants.COMMERCIAL_TIER_PLAN.equals(tier.getTierPlan())) {
                        workflowResponse = updateSubscriptionWFExecutor.monetizeSubscription(workflowDTO, api);
                    } else {
                        workflowResponse = updateSubscriptionWFExecutor.execute(workflowDTO);
                    }
                } else {
                    isMonetizationEnabled = product.getMonetizationStatus();
                    //check whether monetization is enabled for API and tier plan is commercial
                    if (isMonetizationEnabled && APIConstants.COMMERCIAL_TIER_PLAN.equals(tier.getTierPlan())) {
                        workflowResponse = updateSubscriptionWFExecutor.monetizeSubscription(workflowDTO, product);
                    } else {
                        workflowResponse = updateSubscriptionWFExecutor.execute(workflowDTO);
                    }
                }
            } catch (WorkflowException e) {
                throw new APIManagementException("Could not execute Workflow", e);
            } finally {
                if (isTenantFlowStarted) {
                    endTenantFlow();
                }
            }

            //to handle on-the-fly subscription rejection (and removal of subscription entry from the database)
            //the response should have {"Status":"REJECTED"} in the json payload for this to work.
            boolean subscriptionRejected = false;
            String subscriptionStatus = null;
            String subscriptionUUID = "";
            SubscribedAPI updatedSubscription = getSubscriptionById(subscriptionId);

            if (workflowResponse != null && workflowResponse.getJSONPayload() != null
                    && !workflowResponse.getJSONPayload().isEmpty()) {
                try {
                    JSONObject wfResponseJson = (JSONObject) new JSONParser().parse(workflowResponse.getJSONPayload());
                    if (APIConstants.SubscriptionStatus.REJECTED.equals(wfResponseJson.get("Status"))) {
                        subscriptionRejected = true;
                        subscriptionStatus = APIConstants.SubscriptionStatus.REJECTED;
                    }
                } catch (ParseException e) {
                    log.error('\'' + workflowResponse.getJSONPayload() + "' is not a valid JSON.", e);
                }
            }

            if (!subscriptionRejected) {
                subscriptionStatus = updatedSubscription.getSubStatus();
                subscriptionUUID = updatedSubscription.getUUID();

                JSONObject subsLogObject = new JSONObject();
                subsLogObject.put(APIConstants.AuditLogConstants.API_NAME, identifier.getName());
                subsLogObject.put(APIConstants.AuditLogConstants.PROVIDER, identifier.getProviderName());
                subsLogObject.put(APIConstants.AuditLogConstants.APPLICATION_ID, application.getId());
                subsLogObject.put(APIConstants.AuditLogConstants.APPLICATION_NAME, application.getName());
                subsLogObject.put(APIConstants.AuditLogConstants.TIER, identifier.getTier());
                subsLogObject.put(APIConstants.AuditLogConstants.REQUESTED_TIER, requestedThrottlingPolicy);

                APIUtil.logAuditMessage(APIConstants.AuditLogConstants.SUBSCRIPTION, subsLogObject.toString(),
                        APIConstants.AuditLogConstants.UPDATED, this.username);

                if (workflowResponse == null) {
                    workflowResponse = new GeneralWorkflowResponse();
                }

            }

            // get the workflow state once the executor is executed.
            WorkflowDTO wfDTO = apiMgtDAO.retrieveWorkflowFromInternalReference(Integer.toString(subscriptionId),
                    WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_UPDATE);
            // only send the notification if approved
            // wfDTO is null when simple wf executor is used because wf state is not stored in the db and is always
            // approved.
            if (wfDTO != null) {
                if (WorkflowStatus.APPROVED.equals(wfDTO.getStatus())) {
                    SubscriptionEvent subscriptionEvent = new SubscriptionEvent(UUID.randomUUID().toString(),
                            System.currentTimeMillis(), APIConstants.EventType.SUBSCRIPTIONS_UPDATE.name(), tenantId,
                            apiOrgId, subscriptionId, updatedSubscription.getUUID(), apiId, apiUUId,
                            application.getId(), application.getUUID(), requestedThrottlingPolicy, subscriptionStatus
                            , identifier.getName(), identifier.getVersion());
                    APIUtil.sendNotification(subscriptionEvent, APIConstants.NotifierType.SUBSCRIPTIONS.name());
                }
            } else {
                SubscriptionEvent subscriptionEvent = new SubscriptionEvent(UUID.randomUUID().toString(),
                        System.currentTimeMillis(), APIConstants.EventType.SUBSCRIPTIONS_UPDATE.name(), tenantId,
                        apiOrgId, subscriptionId, updatedSubscription.getUUID(), apiId, apiUUId, application.getId(),
                        application.getUUID(), requestedThrottlingPolicy, subscriptionStatus, identifier.getName(),
                        identifier.getVersion());
                APIUtil.sendNotification(subscriptionEvent, APIConstants.NotifierType.SUBSCRIPTIONS.name());
            }

            if (log.isDebugEnabled()) {
                String logMessage = "API Name: " + identifier.getName() + ", API Version " + identifier.getVersion()
                        + ", Subscription Status: " + subscriptionStatus + " subscribe by " + userId + " for app "
                        + application.getName();
                log.debug(logMessage);
            }

            return new SubscriptionResponse(subscriptionStatus, subscriptionUUID, workflowResponse);
        } else {
            throw new APIMgtResourceNotFoundException("Subscriptions not allowed on APIs/API Products in the state: " +
                    state);
        }
    }

    /**
     * Check whether the application is accessible to the specified user
     *
     * @param userId        username
     * @param applicationId application ID
     * @param groupId       GroupId list of the application
     * @return true if the application is accessible by the specified user
     */
    private boolean validateApplication(String userId, int applicationId, String groupId) {

        try {
            return apiMgtDAO.isAppAllowed(applicationId, userId, groupId);
        } catch (APIManagementException e) {
            log.error("Error occurred while getting user group id for user: " + userId, e);
        }
        return false;
    }

    @Override
    public void removeSubscription(Identifier identifier, String userId, int applicationId, String organization)
            throws APIManagementException {

        APIIdentifier apiIdentifier = null;
        APIProductIdentifier apiProdIdentifier = null;
        if (identifier instanceof APIIdentifier) {
            apiIdentifier = (APIIdentifier) identifier;
        }
        if (identifier instanceof APIProductIdentifier) {
            apiProdIdentifier = (APIProductIdentifier) identifier;
        }
        String applicationName = apiMgtDAO.getApplicationNameFromId(applicationId);

        try {
            SubscriptionWorkflowDTO workflowDTO;
            WorkflowExecutor createSubscriptionWFExecutor = getWorkflowExecutor(
                    WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION);
            WorkflowExecutor removeSubscriptionWFExecutor = getWorkflowExecutor(
                    WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_DELETION);
            String workflowExtRef = apiMgtDAO
                    .getExternalWorkflowReferenceForSubscription(identifier, applicationId, organization);

            // in a normal flow workflowExtRef is null when workflows are not enabled
            if (workflowExtRef == null) {
                workflowDTO = new SubscriptionWorkflowDTO();
            } else {
                workflowDTO = (SubscriptionWorkflowDTO) apiMgtDAO.retrieveWorkflow(workflowExtRef);

                // set tiername to the workflowDTO only when workflows are enabled
                SubscribedAPI subscription = apiMgtDAO
                        .getSubscriptionById(Integer.parseInt(workflowDTO.getWorkflowReference()));
                workflowDTO.setTierName(subscription.getTier().getName());
            }
            workflowDTO.setApiProvider(identifier.getProviderName());
            API api = null;
            APIProduct product = null;
            String context = null;
            ApiTypeWrapper wrapper;
            if (apiIdentifier != null) {
                //The API is retrieved without visibility permission check, since the subscribers should be allowed
                //to delete already existing subscriptions made for restricted APIs
                wrapper = getAPIorAPIProductByUUIDWithoutPermissionCheck(apiIdentifier.getUUID(), organization);
                api = wrapper.getApi();
                context = api.getContext();
            } else if (apiProdIdentifier != null) {
                //The API Product is retrieved without visibility permission check, since the subscribers should be
                // allowe to delete already existing subscriptions made for restricted API Products
                wrapper = getAPIorAPIProductByUUIDWithoutPermissionCheck(apiProdIdentifier.getUUID(), organization);
                product = wrapper.getApiProduct();
                context = product.getContext();
            }
            workflowDTO.setApiContext(context);
            workflowDTO.setApiName(identifier.getName());
            workflowDTO.setApiVersion(identifier.getVersion());
            workflowDTO.setApplicationName(applicationName);
            workflowDTO.setTenantDomain(tenantDomain);
            workflowDTO.setTenantId(tenantId);
            workflowDTO.setExternalWorkflowReference(workflowExtRef);
            workflowDTO.setSubscriber(userId);
            workflowDTO.setCallbackUrl(removeSubscriptionWFExecutor.getCallbackURL());
            workflowDTO.setApplicationId(applicationId);
            workflowDTO.setMetadata(WorkflowConstants.PayloadConstants.API_ID, String.valueOf(identifier.getId()));

            String status = null;
            if (apiIdentifier != null) {
                status = apiMgtDAO.getSubscriptionStatus(apiIdentifier.getUUID(), applicationId);
            } else if (apiProdIdentifier != null) {
                status = apiMgtDAO.getSubscriptionStatus(apiProdIdentifier.getUUID(), applicationId);
            }

            String subId = null;
            if (APIConstants.SubscriptionStatus.ON_HOLD.equals(status)) {
                try {
                    createSubscriptionWFExecutor.cleanUpPendingTask(workflowExtRef);
                } catch (WorkflowException ex) {

                    // failed cleanup processes are ignored to prevent failing the deletion process
                    log.warn("Failed to clean pending subscription approval task");
                }
            } else if (APIConstants.SubscriptionStatus.TIER_UPDATE_PENDING.equals(status)) {
                try {
                    if (apiIdentifier != null) {
                        subId = apiMgtDAO.getSubscriptionId(apiIdentifier.getUUID(), applicationId);
                    } else if (apiProdIdentifier != null) {
                        subId = apiMgtDAO.getSubscriptionId(apiProdIdentifier.getUUID(), applicationId);
                    }
                    if (subId != null) {
                        WorkflowDTO wf = apiMgtDAO.retrieveWorkflowFromInternalReference(subId,
                                WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_UPDATE);
                        WorkflowExecutor updateSubscriptionWFExecutor = getWorkflowExecutor(
                                WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_UPDATE);
                        updateSubscriptionWFExecutor.cleanUpPendingTask(wf.getExternalWorkflowReference());
                    }

                } catch (WorkflowException ex) {
                    // failed cleanup processes are ignored to prevent failing the deletion process
                    log.warn("Failed to clean pending subscription update approval task");
                }
            } else if (APIConstants.SubscriptionStatus.UNBLOCKED.equals(status)) {
                try {
                    if (apiIdentifier != null) {
                        subId = apiMgtDAO.getSubscriptionId(apiIdentifier.getUUID(), applicationId);
                    } else if (apiProdIdentifier != null) {
                        subId = apiMgtDAO.getSubscriptionId(apiProdIdentifier.getUUID(), applicationId);
                    }

                } catch (APIManagementException ex) {
                    // failed cleanup processes are ignored to prevent failing the deletion process
                    log.warn("Failed to retrive subscription id");
                }
            }
            if (subId != null) {
                apiMgtDAO.updateSubscriptionStatus(Integer.parseInt(subId),
                 APIConstants.SubscriptionStatus.DELETE_PENDING);
                workflowDTO.setWorkflowReference(subId);
            }

            // update attributes of the new remove workflow to be created
            workflowDTO.setStatus(WorkflowStatus.CREATED);
            workflowDTO.setWorkflowType(WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_DELETION);
            workflowDTO.setCreatedTime(System.currentTimeMillis());
            workflowDTO.setExternalWorkflowReference(removeSubscriptionWFExecutor.generateUUID());

            Tier tier = null;
            if (api != null) {
                Set<Tier> policies = api.getAvailableTiers();
                Iterator<Tier> iterator = policies.iterator();
                boolean isPolicyAllowed = false;
                while (iterator.hasNext()) {
                    Tier policy = iterator.next();
                    if (policy.getName() != null && (policy.getName()).equals(workflowDTO.getTierName())) {
                        tier = policy;
                    }
                }
            } else if (product != null) {
                Set<Tier> policies = product.getAvailableTiers();
                Iterator<Tier> iterator = policies.iterator();
                boolean isPolicyAllowed = false;
                while (iterator.hasNext()) {
                    Tier policy = iterator.next();
                    if (policy.getName() != null && (policy.getName()).equals(workflowDTO.getTierName())) {
                        tier = policy;
                    }
                }
            }
            if (api != null) {
                //check whether monetization is enabled for API and tier plan is commercial
                if (api.getMonetizationStatus() && APIConstants.COMMERCIAL_TIER_PLAN.equals(tier.getTierPlan())) {
                    removeSubscriptionWFExecutor.deleteMonetizedSubscription(workflowDTO, api);
                } else {
                    removeSubscriptionWFExecutor.execute(workflowDTO);
                }
            } else if (product != null) {
                //check whether monetization is enabled for API product and tier plan is commercial
                if (product.getMonetizationStatus() && APIConstants.COMMERCIAL_TIER_PLAN.equals(tier.getTierPlan())) {
                    removeSubscriptionWFExecutor.deleteMonetizedSubscription(workflowDTO, product);
                } else {
                    removeSubscriptionWFExecutor.execute(workflowDTO);
                }
            }
            JSONObject subsLogObject = new JSONObject();
            subsLogObject.put(APIConstants.AuditLogConstants.API_NAME, identifier.getName());
            subsLogObject.put(APIConstants.AuditLogConstants.PROVIDER, identifier.getProviderName());
            subsLogObject.put(APIConstants.AuditLogConstants.APPLICATION_ID, applicationId);
            subsLogObject.put(APIConstants.AuditLogConstants.APPLICATION_NAME, applicationName);

            APIUtil.logAuditMessage(APIConstants.AuditLogConstants.SUBSCRIPTION, subsLogObject.toString(),
                    APIConstants.AuditLogConstants.DELETED, this.username);

        } catch (WorkflowException e) {
            String errorMsg = "Could not execute Workflow, " + WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_DELETION
                    + " for resource " + identifier.toString();
            handleException(errorMsg, e);
        }

        if (log.isDebugEnabled()) {
            String logMessage = "Subscription removed from app " + applicationName + " by " + userId + " For Id: "
                    + identifier.toString();
            log.debug(logMessage);
        }
    }

    @Override
    public void removeSubscription(APIIdentifier identifier, String userId, int applicationId, String groupId,
                                   String organization) throws APIManagementException {
        //check application is viewable to logged user
        boolean isValid = validateApplication(userId, applicationId, groupId);
        if (!isValid) {
            log.error("Application " + applicationId + " is not accessible to user " + userId);
            throw new APIManagementException("Application is not accessible to user " + userId);
        }
        removeSubscription(identifier, userId, applicationId, organization);
    }

    /**
     * Removes a subscription specified by SubscribedAPI object
     *
     * @param subscription SubscribedAPI object
     * @param organization Organization
     * @throws APIManagementException
     */
    @Override
    public void removeSubscription(SubscribedAPI subscription, String organization) throws APIManagementException {

        if (subscription != null) {
            String uuid = subscription.getUUID();
            String deleteWorkflowExtRef = apiMgtDAO
                    .getExternalWorkflowReferenceForSubscriptionAndWFType(subscription.getSubscriptionId(),
                            WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_DELETION);
            if (deleteWorkflowExtRef != null) {
                WorkflowDTO deleteWorkflow = apiMgtDAO.retrieveWorkflow(deleteWorkflowExtRef);
                if (deleteWorkflow != null && WorkflowStatus.CREATED.equals(deleteWorkflow.getStatus())) {
                    subscription.setSubscriptionId(-1);
                    subscription.setSubStatus(APIConstants.SubscriptionStatus.DELETE_PENDING);
                    return;
                }
            }
            Application application = subscription.getApplication();
            Identifier identifier = subscription.getAPIIdentifier() != null ? subscription.getAPIIdentifier()
                    : subscription.getProductId();
            String userId = application.getSubscriber().getName();
            removeSubscription(identifier, userId, application.getId(), organization);
            SubscribedAPI subscriptionAfterDeletion = apiMgtDAO.getSubscriptionById(subscription.getSubscriptionId());
            if (subscriptionAfterDeletion != null
                    && APIConstants.SubscriptionStatus.DELETE_PENDING.equals(subscriptionAfterDeletion.getSubStatus())) {
                subscription.setSubStatus(APIConstants.SubscriptionStatus.DELETE_PENDING);
            } else if (log.isDebugEnabled()) {
                String appName = application.getName();
                String logMessage = "Identifier:  " + identifier.toString() + " subscription (uuid : " + uuid
                        + ") removed from app " + appName;
                log.debug(logMessage);
            }

            // get the workflow state once the executor is executed.
            WorkflowDTO wfDTO = apiMgtDAO.retrieveWorkflowFromInternalReference(Integer.toString(application.getId()),
                    WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_DELETION);
            int tenantId = APIUtil.getTenantId(APIUtil.replaceEmailDomainBack(identifier.getProviderName()));
            String tenantDomain = MultitenantUtils
                    .getTenantDomain(APIUtil.replaceEmailDomainBack(identifier.getProviderName()));
            // only send the notification if approved
            // wfDTO is null when simple wf executor is used because wf state is not stored in the db and is always
            // approved.
            if (wfDTO != null) {
                if (WorkflowStatus.APPROVED.equals(wfDTO.getStatus())) {
                    SubscriptionEvent subscriptionEvent = new SubscriptionEvent(UUID.randomUUID().toString(),
                            System.currentTimeMillis(), APIConstants.EventType.SUBSCRIPTIONS_DELETE.name(), tenantId,
                            organization, subscription.getSubscriptionId(), subscription.getUUID(), identifier.getId(),
                            identifier.getUUID(), application.getId(), application.getUUID(), identifier.getTier(),
                            subscription.getSubStatus(), identifier.getName(), identifier.getVersion());
                    APIUtil.sendNotification(subscriptionEvent, APIConstants.NotifierType.SUBSCRIPTIONS.name());
                }
            } else {
                SubscriptionEvent subscriptionEvent = new SubscriptionEvent(UUID.randomUUID().toString(),
                        System.currentTimeMillis(), APIConstants.EventType.SUBSCRIPTIONS_DELETE.name(), tenantId,
                        organization, subscription.getSubscriptionId(), subscription.getUUID(), identifier.getId(),
                        identifier.getUUID(), application.getId(), application.getUUID(), identifier.getTier(),
                        subscription.getSubStatus(), identifier.getName(), identifier.getVersion());
                APIUtil.sendNotification(subscriptionEvent, APIConstants.NotifierType.SUBSCRIPTIONS.name());
            }
        } else {
            throw new APIManagementException("Subscription does not exists.");
        }
    }

    @Override
    public void removeSubscriber(APIIdentifier identifier, String userId)
            throws APIManagementException {

        throw new UnsupportedOperationException("Unsubscribe operation is not yet implemented");
    }

    /**
     * @deprecated This method needs to be removed once the Jaggery web apps are removed.
     */
    @Override
    public void addComment(APIIdentifier identifier, String commentText, String user) throws APIManagementException {

        apiMgtDAO.addComment(identifier, commentText, user);
    }

    @Override
    public String addComment(String uuid, Comment comment, String user) throws APIManagementException {

        return apiMgtDAO.addComment(uuid, comment, user);
    }

    @Override
    public org.wso2.carbon.apimgt.api.model.Comment[] getComments(String uuid, String parentCommentID)
            throws APIManagementException {

        return apiMgtDAO.getComments(uuid, parentCommentID);
    }

    @Override
    public Comment getComment(ApiTypeWrapper apiTypeWrapper, String commentId, Integer replyLimit,
                              Integer replyOffset) throws
            APIManagementException {

        return apiMgtDAO.getComment(apiTypeWrapper, commentId, replyLimit, replyOffset);
    }

    @Override
    public CommentList getComments(ApiTypeWrapper apiTypeWrapper, String parentCommentID, Integer replyLimit,
                                   Integer replyOffset)
            throws APIManagementException {

        return apiMgtDAO.getComments(apiTypeWrapper, parentCommentID, replyLimit, replyOffset);
    }

    @Override
    public boolean editComment(ApiTypeWrapper apiTypeWrapper, String commentId, Comment comment) throws
            APIManagementException {

        return apiMgtDAO.editComment(apiTypeWrapper, commentId, comment);
    }

    @Override
    public void deleteComment(String uuid, String commentId) throws APIManagementException {

        apiMgtDAO.deleteComment(uuid, commentId);
    }

    @Override
    public boolean deleteComment(ApiTypeWrapper apiTypeWrapper, String commentId) throws APIManagementException {

        return apiMgtDAO.deleteComment(apiTypeWrapper, commentId);
    }

    /**
     * Add a new Application from the store.
     *
     * @param application  - {@link org.wso2.carbon.apimgt.api.model.Application}
     * @param userId       - {@link String}
     * @param organization
     * @return {@link String}
     */
    @Override
    public int addApplication(Application application, String userId, String organization)
            throws APIManagementException {

        if (APIUtil.isOnPremResolver()) {
            organization = tenantDomain;
        }
        if (application.getName() != null && (application.getName().length() != application.getName().trim().length())) {
            handleApplicationNameContainSpacesException("Application name " +
                    "cannot contain leading or trailing white spaces");
        }
        validateApplicationPolicy(application, organization);

        JSONArray applicationAttributesFromConfig = getAppAttributesFromConfig(userId);
        Map<String, String> applicationAttributes = application.getApplicationAttributes();
        if (applicationAttributes == null) {
            /*
             * This empty Hashmap is set to avoid throwing a null pointer exception, in case no application attributes
             * are set when creating an application
             */
            applicationAttributes = new HashMap<String, String>();
        }
        Set<String> configAttributes = new HashSet<>();

        if (applicationAttributesFromConfig != null) {

            for (Object object : applicationAttributesFromConfig) {
                JSONObject attribute = (JSONObject) object;
                Boolean hidden = (Boolean) attribute.get(APIConstants.ApplicationAttributes.HIDDEN);
                Boolean required = (Boolean) attribute.get(APIConstants.ApplicationAttributes.REQUIRED);
                String attributeName = (String) attribute.get(APIConstants.ApplicationAttributes.ATTRIBUTE);
                String defaultValue = (String) attribute.get(APIConstants.ApplicationAttributes.DEFAULT);
                if (BooleanUtils.isTrue(hidden) && BooleanUtils.isTrue(required) && StringUtils.isEmpty(defaultValue)) {
                    /*
                     * In case a default value is not provided for a required hidden attribute, an exception is thrown,
                     * we don't do this validation in server startup to support multi tenancy scenarios
                     */
                    handleException("Default value not provided for hidden required attribute. Please check the " +
                            "configuration");
                }
                configAttributes.add(attributeName);
                if (BooleanUtils.isTrue(required)) {
                    if (BooleanUtils.isTrue(hidden)) {
                        /*
                         * If a required hidden attribute is attempted to be populated, we replace it with
                         * the default value.
                         */
                        String oldValue = applicationAttributes.put(attributeName, defaultValue);
                        if (StringUtils.isNotEmpty(oldValue)) {
                            log.info("Replaced provided value: " + oldValue + " with default the value" +
                                    " for the hidden application attribute: " + attributeName);
                        }
                    } else if (!applicationAttributes.keySet().contains(attributeName)) {
                        if (StringUtils.isNotEmpty(defaultValue)) {
                            /*
                             * If a required attribute is not provided and a default value is given, we replace it with
                             * the default value.
                             */
                            applicationAttributes.put(attributeName, defaultValue);
                            log.info("Added default value: " + defaultValue +
                                    " as required attribute: " + attributeName + "is not provided");
                        } else {
                            /*
                             * If a required attribute is not provided but a default value not given, we throw a bad
                             * request exception.
                             */
                            handleException("Bad Request. Required application attribute not provided");
                        }
                    }
                } else if (BooleanUtils.isTrue(hidden)) {
                    /*
                     * If an optional hidden attribute is provided, we remove it and leave it blank, and leave it for
                     * an extension to populate it.
                     */
                    applicationAttributes.remove(attributeName);
                }
            }
            application.setApplicationAttributes(validateApplicationAttributes(applicationAttributes,
             configAttributes));
        } else {
            application.setApplicationAttributes(null);
        }
        application.setUUID(UUID.randomUUID().toString());
        if (APIUtil.isApplicationExist(userId, application.getName(), application.getGroupId(), organization)) {
            handleResourceAlreadyExistsException(
                    "A duplicate application already exists by the name - " + application.getName());
        }
        //check whether callback url is empty and set null
        if (StringUtils.isBlank(application.getCallbackUrl())) {
            application.setCallbackUrl(null);
        }
        int applicationId = apiMgtDAO.addApplication(application, userId, organization);

        JSONObject appLogObject = new JSONObject();
        appLogObject.put(APIConstants.AuditLogConstants.NAME, application.getName());
        appLogObject.put(APIConstants.AuditLogConstants.TIER, application.getTier());
        appLogObject.put(APIConstants.AuditLogConstants.CALLBACK, application.getCallbackUrl());
        appLogObject.put(APIConstants.AuditLogConstants.GROUPS, application.getGroupId());
        appLogObject.put(APIConstants.AuditLogConstants.OWNER, application.getSubscriber().getName());

        APIUtil.logAuditMessage(APIConstants.AuditLogConstants.APPLICATION, appLogObject.toString(),
                APIConstants.AuditLogConstants.CREATED, this.username);

        boolean isTenantFlowStarted = false;
        if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            isTenantFlowStarted = startTenantFlowForTenantDomain(tenantDomain);
        }
        try {

            WorkflowExecutor appCreationWFExecutor =
             getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_APPLICATION_CREATION);
            ApplicationWorkflowDTO appWFDto = new ApplicationWorkflowDTO();
            appWFDto.setApplication(application);

            appWFDto.setExternalWorkflowReference(appCreationWFExecutor.generateUUID());
            appWFDto.setWorkflowReference(String.valueOf(applicationId));
            appWFDto.setWorkflowType(WorkflowConstants.WF_TYPE_AM_APPLICATION_CREATION);
            appWFDto.setCallbackUrl(appCreationWFExecutor.getCallbackURL());
            appWFDto.setStatus(WorkflowStatus.CREATED);
            appWFDto.setTenantDomain(organization);
            appWFDto.setTenantId(tenantId);
            appWFDto.setUserName(userId);
            appWFDto.setCreatedTime(System.currentTimeMillis());
            appCreationWFExecutor.execute(appWFDto);
        } catch (WorkflowException e) {
            //If the workflow execution fails, roll back transaction by removing the application entry.
            application.setId(applicationId);
            apiMgtDAO.deleteApplication(application);
            log.error("Unable to execute Application Creation Workflow", e);
            handleException("Unable to execute Application Creation Workflow", e);
        } finally {
            if (isTenantFlowStarted) {
                endTenantFlow();
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Application Name: " + application.getName() + " added successfully.");
        }

        // Extracting API details for the recommendation system
        if (recommendationEnvironment != null) {
            RecommenderEventPublisher extractor = new RecommenderDetailsExtractor(application, userId, applicationId,
                    organization);
            Thread recommendationThread = new Thread(extractor);
            recommendationThread.start();
        }
        // get the workflow state once the executor is executed.
        WorkflowDTO wfDTO = apiMgtDAO.retrieveWorkflowFromInternalReference(Integer.toString(applicationId),
                WorkflowConstants.WF_TYPE_AM_APPLICATION_CREATION);
        // only send the notification if approved
        // wfDTO is null when simple wf executor is used because wf state is not stored in the db and is always
        // approved.
        if (wfDTO != null) {
            if (WorkflowStatus.APPROVED.equals(wfDTO.getStatus())) {
                ApplicationEvent applicationEvent = new ApplicationEvent(UUID.randomUUID().toString(),
                        System.currentTimeMillis(), APIConstants.EventType.APPLICATION_CREATE.name(), tenantId,
                        organization, applicationId, application.getUUID(), application.getName(),
                        application.getTokenType(),
                        application.getTier(), application.getGroupId(), application.getApplicationAttributes(),
                         userId);
                APIUtil.sendNotification(applicationEvent, APIConstants.NotifierType.APPLICATION.name());
            }
        } else {
            ApplicationEvent applicationEvent = new ApplicationEvent(UUID.randomUUID().toString(),
                    System.currentTimeMillis(), APIConstants.EventType.APPLICATION_CREATE.name(), tenantId,
                    organization, applicationId, application.getUUID(), application.getName(),
                    application.getTokenType(), application.getTier(), application.getGroupId(),
                    application.getApplicationAttributes(), userId);
            APIUtil.sendNotification(applicationEvent, APIConstants.NotifierType.APPLICATION.name());
        }
        return applicationId;
    }

    private void validateApplicationPolicy(Application application, String organization) throws APIManagementException {

        Map<String, Tier> appTierMap = APIUtil.getTiers(APIConstants.TIER_APPLICATION_TYPE, organization);
        if (APIUtil.findTier(appTierMap.values(), application.getTier()) == null) {
            throw new APIManagementException("Specified tier " + application.getTier() + " is invalid",
                    ExceptionCodes.TIER_NAME_INVALID);
        }
    }

    /**
     * Updates an Application identified by its id
     *
     * @param application Application object to be updated
     * @throws APIManagementException
     */
    @Override
    public void updateApplication(Application application) throws APIManagementException {

        Application existingApp;
        String uuid = application.getUUID();
        if (!StringUtils.isEmpty(uuid)) {
            existingApp = apiMgtDAO.getApplicationByUUID(uuid);
            application.setId(existingApp.getId());
        } else {
            existingApp = apiMgtDAO.getApplicationById(application.getId());
        }

        if (existingApp != null && APIConstants.ApplicationStatus.APPLICATION_CREATED.equals(existingApp.getStatus())) {
            throw new APIManagementException("Cannot update the application while it is INACTIVE");
        }
        boolean isCaseInsensitiveComparisons = Boolean.parseBoolean(getAPIManagerConfiguration().
                getFirstProperty(APIConstants.API_STORE_FORCE_CI_COMPARISIONS));

        boolean isUserAppOwner;
        if (isCaseInsensitiveComparisons) {
            isUserAppOwner = application.getSubscriber().getName().
                    equalsIgnoreCase(existingApp.getSubscriber().getName());
        } else {
            isUserAppOwner = application.getSubscriber().getName().equals(existingApp.getSubscriber().getName());
        }

        if (!isUserAppOwner) {
            throw new APIManagementException("user: " + application.getSubscriber().getName() + ", " +
                    "attempted to update application owned by: " + existingApp.getSubscriber().getName());
        }

        if (application.getName() != null && (application.getName().length() != application.getName().trim().length())) {
            handleApplicationNameContainSpacesException("Application name " +
                    "cannot contain leading or trailing white spaces");
        }

        String processedIds;

        if (!existingApp.getName().equals(application.getName())) {
            processedIds = application.getGroupId();
        } else {
            processedIds = getUpdatedGroupIds(existingApp.getGroupId(), application.getGroupId());
        }

        if (application.getGroupId() != null && APIUtil.isApplicationGroupCombinationExist(
                application.getSubscriber().getName(), application.getName(), processedIds)) {
            handleResourceAlreadyExistsException(
                    "A duplicate application already exists by the name - " + application.getName());
        }

        // Retain the 'DEFAULT' token type of migrated applications unless the token type is changed to 'JWT'.
        if (APIConstants.DEFAULT_TOKEN_TYPE.equals(existingApp.getTokenType()) &&
                APIConstants.TOKEN_TYPE_OAUTH.equals(application.getTokenType())) {
            application.setTokenType(APIConstants.DEFAULT_TOKEN_TYPE);
        }

        // Prevent the change of token type of applications having 'JWT' token type.
        if (APIConstants.TOKEN_TYPE_JWT.equals(existingApp.getTokenType()) &&
                !APIConstants.TOKEN_TYPE_JWT.equals(application.getTokenType())) {
            throw new APIManagementException(
                    "Cannot change application token type from " + APIConstants.TOKEN_TYPE_JWT + " to " +
                            application.getTokenType());
        }

        Subscriber subscriber = application.getSubscriber();

        JSONArray applicationAttributesFromConfig = getAppAttributesFromConfig(subscriber.getName());
        Map<String, String> applicationAttributes = application.getApplicationAttributes();
        Map<String, String> existingApplicationAttributes = existingApp.getApplicationAttributes();
        if (applicationAttributes == null) {
            /*
             * This empty Hashmap is set to avoid throwing a null pointer exception, in case no application attributes
             * are set when updating an application
             */
            applicationAttributes = new HashMap<String, String>();
        }
        Set<String> configAttributes = new HashSet<>();

        if (applicationAttributesFromConfig != null) {

            for (Object object : applicationAttributesFromConfig) {
                boolean isExistingValue = false;
                JSONObject attribute = (JSONObject) object;
                Boolean hidden = (Boolean) attribute.get(APIConstants.ApplicationAttributes.HIDDEN);
                Boolean required = (Boolean) attribute.get(APIConstants.ApplicationAttributes.REQUIRED);
                String attributeName = (String) attribute.get(APIConstants.ApplicationAttributes.ATTRIBUTE);
                String defaultValue = (String) attribute.get(APIConstants.ApplicationAttributes.DEFAULT);
                if (BooleanUtils.isTrue(hidden) && BooleanUtils.isTrue(required) && StringUtils.isEmpty(defaultValue)) {
                    /*
                     * In case a default value is not provided for a required hidden attribute, an exception is thrown,
                     * we don't do this validation in server startup to support multi tenancy scenarios
                     */
                    handleException("Default value not provided for hidden required attribute. Please check the " +
                            "configuration");
                }
                configAttributes.add(attributeName);
                if (existingApplicationAttributes.containsKey(attributeName)) {
                    /*
                     * If a there is an existing attribute value, that is used as the default value.
                     */
                    isExistingValue = true;
                    defaultValue = existingApplicationAttributes.get(attributeName);
                }
                if (BooleanUtils.isTrue(required)) {
                    if (BooleanUtils.isTrue(hidden)) {
                        String oldValue = applicationAttributes.put(attributeName, defaultValue);
                        if (StringUtils.isNotEmpty(oldValue)) {
                            log.info("Replaced provided value: " + oldValue + " with the default/existing value for" +
                                    " the hidden application attribute: " + attributeName);
                        }
                    } else if (!applicationAttributes.keySet().contains(attributeName)) {
                        if (StringUtils.isNotEmpty(defaultValue)) {
                            applicationAttributes.put(attributeName, defaultValue);
                        } else {
                            handleException("Bad Request. Required application attribute not provided");
                        }
                    }
                } else if (BooleanUtils.isTrue(hidden)) {
                    if (isExistingValue) {
                        applicationAttributes.put(attributeName, defaultValue);
                    } else {
                        applicationAttributes.remove(attributeName);
                    }
                }
            }
            application.setApplicationAttributes(validateApplicationAttributes(applicationAttributes,
             configAttributes));
        } else {
            application.setApplicationAttributes(null);
        }
        validateApplicationPolicy(application, existingApp.getOrganization());
        apiMgtDAO.updateApplication(application);
        if (log.isDebugEnabled()) {
            log.debug("Successfully updated the Application: " + application.getId() + " in the database.");
        }

        JSONObject appLogObject = new JSONObject();
        appLogObject.put(APIConstants.AuditLogConstants.NAME, application.getName());
        appLogObject.put(APIConstants.AuditLogConstants.TIER, application.getTier());
        appLogObject.put(APIConstants.AuditLogConstants.STATUS, existingApp != null ? existingApp.getStatus() : "");
        appLogObject.put(APIConstants.AuditLogConstants.CALLBACK, application.getCallbackUrl());
        appLogObject.put(APIConstants.AuditLogConstants.GROUPS, application.getGroupId());
        appLogObject.put(APIConstants.AuditLogConstants.OWNER, application.getSubscriber().getName());

        APIUtil.logAuditMessage(APIConstants.AuditLogConstants.APPLICATION, appLogObject.toString(),
                APIConstants.AuditLogConstants.UPDATED, this.username);

        // Extracting API details for the recommendation system
        if (recommendationEnvironment != null) {
            RecommenderEventPublisher extractor = new RecommenderDetailsExtractor(application, username,
 requestedTenant);
            Thread recommendationThread = new Thread(extractor);
            recommendationThread.start();
        }

        ApplicationEvent applicationEvent = new ApplicationEvent(UUID.randomUUID().toString(),
                System.currentTimeMillis(), APIConstants.EventType.APPLICATION_UPDATE.name(), tenantId,
                existingApp.getOrganization(), application.getId(), application.getUUID(), application.getName(),
                application.getTokenType(), application.getTier(), application.getGroupId(),
                application.getApplicationAttributes(), existingApp.getSubscriber().getName());
        APIUtil.sendNotification(applicationEvent, APIConstants.NotifierType.APPLICATION.name());
    }

    /**
     * Function to find newly added group Ids
     *
     * @param existingGroupIds existing GroupIds
     * @param updatedGroupIds  updated GroupIds
     * @return
     */
    private String getUpdatedGroupIds(String existingGroupIds, String updatedGroupIds) {

        if (StringUtils.isEmpty(updatedGroupIds)) {
            return updatedGroupIds;
        }
        Set<String> existingGroupIdSet = new HashSet<>();
        if (existingGroupIds != null && !existingGroupIds.isEmpty()) {
            existingGroupIdSet.addAll(Arrays.asList(existingGroupIds.split(",")));
        }
        Set<String> updatedGroupIdSet = new HashSet<>();
        updatedGroupIdSet.addAll(Arrays.asList(updatedGroupIds.split(",")));
        updatedGroupIdSet.removeAll(existingGroupIdSet);
        updatedGroupIds = String.join(",", updatedGroupIdSet);
        return updatedGroupIds;
    }

    /**
     * Function to remove an Application from the API Store
     *
     * @param application - The Application Object that represents the Application
     * @param username
     * @throws APIManagementException
     */
    @Override
    public void removeApplication(Application application, String username) throws APIManagementException {

        String uuid = application.getUUID();
        Map<String, Pair<String, String>> consumerKeysOfApplication = null;
        if (application.getId() == 0 && !StringUtils.isEmpty(uuid)) {
            application = apiMgtDAO.getApplicationByUUID(uuid);
        }
        Set<APIKey> keyMappingsFromApplicationId = apiMgtDAO.getKeyMappingsFromApplicationId(application.getId());
        Set<SubscribedAPI> subscribedAPIsByApplication = apiMgtDAO.getSubscribedAPIsByApplication(application);
        boolean isTenantFlowStarted = false;
        int applicationId = application.getId();

        boolean isCaseInsensitiveComparisons =
                Boolean.parseBoolean(getAPIManagerConfiguration().getFirstProperty(APIConstants.API_STORE_FORCE_CI_COMPARISIONS));

        boolean isUserAppOwner;
        if (isCaseInsensitiveComparisons) {
            isUserAppOwner = application.getSubscriber().getName().equalsIgnoreCase(username);
        } else {
            isUserAppOwner = application.getSubscriber().getName().equals(username);
        }

        if (!isUserAppOwner) {
            throw new APIManagementException("user: " + username + ", " + "attempted to remove application owned by: "
                    + application.getSubscriber().getName());
        }
        try {
            String workflowExtRef;
            ApplicationWorkflowDTO workflowDTO;
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                PrivilegedCarbonContext.startTenantFlow();
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }

            String deletePendingWorkflowRef = apiMgtDAO.getExternalWorkflowRefByInternalRefWorkflowType(applicationId
, WorkflowConstants.WF_TYPE_AM_APPLICATION_DELETION);
            if (deletePendingWorkflowRef != null) {
                WorkflowDTO deletePendingWorkflow = apiMgtDAO.retrieveWorkflow(deletePendingWorkflowRef);
                if (deletePendingWorkflow != null && WorkflowStatus.CREATED.equals(deletePendingWorkflow.getStatus())) {
                    application.setId(-1);
                    return;
                }
            }

            WorkflowExecutor removeApplicationWFExecutor =
             getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_APPLICATION_DELETION);

            apiMgtDAO.updateApplicationStatus(applicationId, APIConstants.ApplicationStatus.DELETE_PENDING);

            workflowDTO = new ApplicationWorkflowDTO();
            workflowDTO.setApplication(application);
            workflowDTO.setWorkflowReference(String.valueOf(applicationId));
            workflowDTO.setExternalWorkflowReference(removeApplicationWFExecutor.generateUUID());
            workflowDTO.setCallbackUrl(removeApplicationWFExecutor.getCallbackURL());
            workflowDTO.setUserName(this.username);
            workflowDTO.setTenantDomain(tenantDomain);
            workflowDTO.setTenantId(tenantId);

            // update attributes of the new remove workflow to be created
            workflowDTO.setStatus(WorkflowStatus.CREATED);
            workflowDTO.setCreatedTime(System.currentTimeMillis());
            workflowDTO.setWorkflowType(WorkflowConstants.WF_TYPE_AM_APPLICATION_DELETION);
            workflowDTO.setExternalWorkflowReference(removeApplicationWFExecutor.generateUUID());

            if (!(removeApplicationWFExecutor instanceof ApplicationDeletionApprovalWorkflowExecutor)) {
                cleanupPendingTasksForApplicationDeletion(applicationId);
            }

            removeApplicationWFExecutor.execute(workflowDTO);

            Application applicationAfterDeletion = apiMgtDAO.getApplicationById(applicationId);
            if (applicationAfterDeletion != null) {
                application.setStatus(applicationAfterDeletion.getStatus());
            }

            JSONObject appLogObject = new JSONObject();
            appLogObject.put(APIConstants.AuditLogConstants.NAME, application.getName());
            appLogObject.put(APIConstants.AuditLogConstants.TIER, application.getTier());
            appLogObject.put(APIConstants.AuditLogConstants.CALLBACK, application.getCallbackUrl());
            appLogObject.put(APIConstants.AuditLogConstants.GROUPS, application.getGroupId());
            appLogObject.put(APIConstants.AuditLogConstants.OWNER, application.getSubscriber().getName());

            APIUtil.logAuditMessage(APIConstants.AuditLogConstants.APPLICATION, appLogObject.toString(),
APIConstants.AuditLogConstants.DELETED, this.username);

        } catch (WorkflowException e) {
            String errorMsg = "Could not execute Workflow, " + WorkflowConstants.WF_TYPE_AM_APPLICATION_DELETION + " "
             + "for applicationID " + application.getId();
            handleException(errorMsg, e);
        } finally {
            if (isTenantFlowStarted) {
                endTenantFlow();
            }
        }

        if (log.isDebugEnabled()) {
            String logMessage = "Application Name: " + application.getName() + " successfully removed";
            log.debug(logMessage);
        }

        // Extracting API details for the recommendation system
        if (recommendationEnvironment != null) {
            RecommenderEventPublisher extractor = new RecommenderDetailsExtractor(applicationId, username,
             requestedTenant);
            Thread recommendationThread = new Thread(extractor);
            recommendationThread.start();
        }

        // get the workflow state once the executor is executed.
        WorkflowDTO wfDTO = apiMgtDAO.retrieveWorkflowFromInternalReference(Integer.toString(applicationId),
         WorkflowConstants.WF_TYPE_AM_APPLICATION_DELETION);
        // only send the notification if approved
        // wfDTO is null when simple wf executor is used because wf state is not stored in the db and is always
        // approved.
        if (wfDTO != null) {
            if (WorkflowStatus.APPROVED.equals(wfDTO.getStatus()) || wfDTO.getStatus() == null) {
                sendApplicationDeletionEvent(application, keyMappingsFromApplicationId, subscribedAPIsByApplication);
            }
        } else {
            sendApplicationDeletionEvent(application, keyMappingsFromApplicationId, subscribedAPIsByApplication);
        }
    }

    private void sendApplicationDeletionEvent(Application application, Set<APIKey> apiKeys,
                                              Set<SubscribedAPI> subscribedAPIS) throws APIManagementException {
        if (apiKeys != null && apiKeys.size() > 0) {
            for (APIKey apiKey : apiKeys) {
                String consumerKey = apiKey.getConsumerKey();
                String keyManagerUUID = apiKey.getKeyManager();
                KeyManagerConfigurationDTO keyManagerConfigurationByUUID =
                        apiMgtDAO.getKeyManagerConfigurationByUUID(keyManagerUUID);
                String keyManagerTenantDomain = keyManagerConfigurationByUUID.getOrganization();
                String keyManagerName = keyManagerConfigurationByUUID.getName();
                ApplicationRegistrationEvent removeEntryTrigger = new ApplicationRegistrationEvent(
                        UUID.randomUUID().toString(), System.currentTimeMillis(),
                        APIConstants.EventType.REMOVE_APPLICATION_KEYMAPPING.name(),
                        APIUtil.getTenantIdFromTenantDomain(keyManagerTenantDomain), application.getOrganization(),
                        application.getId(), application.getUUID(), consumerKey, apiKey.getType(),
                        keyManagerName);
                APIUtil.sendNotification(removeEntryTrigger, APIConstants.NotifierType.APPLICATION_REGISTRATION.name());
            }
        }
        if (subscribedAPIS != null && subscribedAPIS.size() > 0) {
            for (SubscribedAPI subscribedAPI : subscribedAPIS) {

                SubscriptionEvent subscriptionEvent = new SubscriptionEvent(UUID.randomUUID().toString(),
                        System.currentTimeMillis(),
                        APIConstants.EventType.SUBSCRIPTIONS_DELETE.name(),
                        APIUtil.getInternalIdFromTenantDomainOrOrganization(subscribedAPI.getOrganization()),
                        subscribedAPI.getOrganization(), subscribedAPI.getSubscriptionId(),
                        subscribedAPI.getUUID(), subscribedAPI.getApiId(), subscribedAPI.getAPIUUId(),
                        subscribedAPI.getApplication().getId(), subscribedAPI.getApplication().getUUID(),
                        subscribedAPI.getTier().getName(), subscribedAPI.getSubCreatedStatus(),
                        subscribedAPI.getAPIIdentifier().getApiName(), subscribedAPI.getAPIIdentifier().getVersion());
                APIUtil.sendNotification(subscriptionEvent, APIConstants.NotifierType.SUBSCRIPTIONS.name());
            }
        }
        ApplicationEvent applicationEvent = new ApplicationEvent(UUID.randomUUID().toString(),
                System.currentTimeMillis(), APIConstants.EventType.APPLICATION_DELETE.name(), tenantId,
                application.getOrganization(), application.getId(), application.getUUID(), application.getName(),
                application.getTokenType(),
                application.getTier(), application.getGroupId(), Collections.EMPTY_MAP, username);
        APIUtil.sendNotification(applicationEvent, APIConstants.NotifierType.APPLICATION.name());
    }

    /**
     * Cleans the pending approval tasks associated with the given application subjected to be deleted
     * Pending approvals for Application creation, Subscription Creation, Subscription Deletion, Subscription Update
     * will be deleted
     *
     * @param applicationId ID of the application which the associated pending tasks should be removed
     * @throws APIManagementException If any issue occurred in retrieving workflow references for the given
     * applicationId
     */
    @Override
    public void cleanupPendingTasksForApplicationDeletion(int applicationId) throws APIManagementException {

        try {
            WorkflowExecutor createApplicationWFExecutor =
 getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_APPLICATION_CREATION);
            WorkflowExecutor createSubscriptionWFExecutor =
            getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION);
            WorkflowExecutor deleteSubscriptionWFExecutor =
            getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_DELETION);
            WorkflowExecutor updateSubscriptionWFExecutor =
            getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_UPDATE);
            WorkflowExecutor createProductionRegistrationWFExecutor =
             getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_APPLICATION_REGISTRATION_PRODUCTION);
            WorkflowExecutor createSandboxRegistrationWFExecutor =
             getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_APPLICATION_REGISTRATION_SANDBOX);

            // clean up pending subscription tasks
            Map<String, Set<Integer>> pendingSubscriptionsByStatus = apiMgtDAO
                    .getPendingSubscriptionsByAppId(applicationId);
            for (int subscription : pendingSubscriptionsByStatus.get(APIConstants.SubscriptionStatus.ON_HOLD)) {
                cleanupPendingSubscriptionTask(subscription, WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION,
                        createSubscriptionWFExecutor);
            }

            for (int subscription : pendingSubscriptionsByStatus.get(APIConstants.SubscriptionStatus.DELETE_PENDING)) {
                cleanupPendingSubscriptionTask(subscription, WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_DELETION,
                        deleteSubscriptionWFExecutor);
            }

            for (int subscription :
            pendingSubscriptionsByStatus.get(APIConstants.SubscriptionStatus.TIER_UPDATE_PENDING)) {
                cleanupPendingSubscriptionTask(subscription, WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_UPDATE,
                        updateSubscriptionWFExecutor);
            }

            // cleanup pending application registration tasks
            Map<String, String> keyManagerWiseProductionKeyStatus = apiMgtDAO
                    .getRegistrationApprovalState(applicationId, APIConstants.API_KEY_TYPE_PRODUCTION);
            Map<String, String> keyManagerWiseSandboxKeyStatus = apiMgtDAO
                    .getRegistrationApprovalState(applicationId, APIConstants.API_KEY_TYPE_SANDBOX);
            keyManagerWiseProductionKeyStatus.forEach((keyManagerName, state) ->
                    cleanupPendingApplicationRegistrationTask(state, applicationId,
                     APIConstants.API_KEY_TYPE_PRODUCTION,
                            keyManagerName, createProductionRegistrationWFExecutor));

            keyManagerWiseSandboxKeyStatus.forEach((keyManagerName, state) ->
                    cleanupPendingApplicationRegistrationTask(state, applicationId, APIConstants.API_KEY_TYPE_SANDBOX,
                            keyManagerName, createSandboxRegistrationWFExecutor));

            //cleanup pending application creation task
            String appCreationWorkflowExtRef = apiMgtDAO.getExternalWorkflowRefByInternalRefWorkflowType(applicationId,
                    WorkflowConstants.WF_TYPE_AM_APPLICATION_CREATION);
            if (appCreationWorkflowExtRef != null) {
                cleanupAppCreationPendingTask(applicationId, createApplicationWFExecutor, appCreationWorkflowExtRef);
            }
        } catch (WorkflowException ex) {
            log.warn("Failed to load workflow executors");
        }
    }

    private void cleanupAppCreationPendingTask(int applicationId, WorkflowExecutor workflowExecutor,
     String workflowRef) {

        try {
            workflowExecutor.cleanUpPendingTask(workflowRef);
        } catch (WorkflowException ex) {

            // failed cleanup processes are ignored to prevent failing the application removal process
            log.warn("Failed to clean pending application approval task of " + applicationId);
        }
    }

    private void cleanupPendingApplicationRegistrationTask(String state, int applicationId, String apiKeyType,
                                                           String keyManagerName,
                                                           WorkflowExecutor applicationRegistrationWFExecutor) {

        final String keyType = apiKeyType.toLowerCase();
        if (WorkflowStatus.CREATED.toString().equals(state)) {
            try {
                String applicationRegistrationExternalRef = apiMgtDAO
                        .getRegistrationWFReference(applicationId, apiKeyType,
                                keyManagerName);
                applicationRegistrationWFExecutor.cleanUpPendingTask(applicationRegistrationExternalRef);
            } catch (APIManagementException ex) {

                // failed cleanup processes are ignored to prevent failing the application removal process
                log.warn("Failed to get external workflow reference for " + keyType + " key of application "
                        + applicationId);
            } catch (WorkflowException ex) {

                // failed cleanup processes are ignored to prevent failing the application removal process
                log.warn("Failed to clean pending " + keyType + " key approval task of " + applicationId);
            }
        }
    }

    private void cleanupPendingSubscriptionTask(int subscriptionId, String wfType,
     WorkflowExecutor subscriptionWFExecutor) {

        try {
            String workflowExtRef = apiMgtDAO.getExternalWorkflowReferenceForSubscriptionAndWFType(subscriptionId,
                    wfType);
            subscriptionWFExecutor.cleanUpPendingTask(workflowExtRef);
        } catch (APIManagementException ex) {
            // failed cleanup processes are ignored to prevent failing the application removal process
            log.warn(GET_SUB_WORKFLOW_REF_FAILED + subscriptionId);
        } catch (WorkflowException ex) {
            // failed cleanup processes are ignored to prevent failing the application removal process
            log.warn(CLEAN_PENDING_SUB_APPROVAL_TASK_FAILED + subscriptionId);
        }
    }

    @Override
    @Deprecated
    public Map<String, Object> requestApprovalForApplicationRegistration(String userId, Application application,
                                                                         String tokenType, String callbackUrl,
                                                                         String[] allowedDomains, String validityTime,
                                                                         String tokenScope, String groupingId,
                                                                         String jsonString,
                                                                         String keyManagerName, String tenantDomain)
            throws APIManagementException {

        return requestApprovalForApplicationRegistration(userId, application, tokenType, callbackUrl,
                allowedDomains, validityTime, tokenScope, jsonString, keyManagerName, tenantDomain, false);
    }

    /**
     * This method specifically implemented for REST API by removing application and data access logic
     * from host object layer. So as per new implementation we need to pass requested scopes to this method
     * as tokenScope. So we will do scope related other logic here in this method.
     * So host object should only pass required 9 parameters.
     */
    @Override
    public Map<String, Object> requestApprovalForApplicationRegistration(String userId, Application application,
                                                                         String tokenType, String callbackUrl,
                                                                         String[] allowedDomains, String validityTime,
                                                                         String tokenScope,
                                                                         String jsonString,
                                                                         String keyManagerName, String tenantDomain,
                                                                         boolean isImportMode)
            throws APIManagementException {

        boolean isTenantFlowStarted = false;
        if (StringUtils.isEmpty(tenantDomain)) {
            tenantDomain = MultitenantUtils.getTenantDomain(userId);
        } else {
            int tenantId = APIUtil.getInternalOrganizationId(tenantDomain);

            // To handle choreo scenario.
            if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
                tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
            }
        }

        String keyManagerId = null;
        if (keyManagerName != null) {
            KeyManagerConfigurationDTO keyManagerConfiguration =
                    apiMgtDAO.getKeyManagerConfigurationByName(tenantDomain, keyManagerName);
            if (keyManagerConfiguration == null) {
                keyManagerConfiguration = apiMgtDAO.getKeyManagerConfigurationByUUID(keyManagerName);
                if (keyManagerConfiguration != null) {
                    keyManagerId = keyManagerName;
                    keyManagerName = keyManagerConfiguration.getName();
                }
            } else {
                keyManagerId = keyManagerConfiguration.getUuid();
            }
            if (keyManagerConfiguration == null || !keyManagerConfiguration.isEnabled()) {
                throw new APIManagementException(
                        "Key Manager " + keyManagerName + " doesn't exist in Tenant " + tenantDomain,
                        ExceptionCodes.KEY_MANAGER_NOT_REGISTERED);
            }
            if (KeyManagerConfiguration.TokenType.EXCHANGED.toString().equals(keyManagerConfiguration.getTokenType())) {
                throw new APIManagementException("Key Manager " + keyManagerName + " doesn't support to generate" +
                        " Client Application", ExceptionCodes.KEY_MANAGER_NOT_SUPPORT_OAUTH_APP_CREATION);
            }
            Object enableOauthAppCreation =
                    keyManagerConfiguration.getProperty(APIConstants.KeyManager.ENABLE_OAUTH_APP_CREATION);
            if (enableOauthAppCreation != null && !(Boolean) enableOauthAppCreation) {
                if (isImportMode) {
                    log.debug("Importing application when KM OAuth App creation is disabled. Trying to map keys");
                    // passing null `clientId` is ok here since the id/secret pair is included
                    // in the `jsonString` and ApplicationUtils#createOauthAppRequest logic handles it.
                    return mapExistingOAuthClient(jsonString, userId, null, application, tokenType,
                            APIConstants.DEFAULT_TOKEN_TYPE, keyManagerName, tenantDomain);
                } else {
                    throw new APIManagementException("Key Manager " + keyManagerName + " doesn't support to generate" +
                            " Client Application", ExceptionCodes.KEY_MANAGER_NOT_SUPPORT_OAUTH_APP_CREATION);
                }
            }
        }
        try {
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = startTenantFlowForTenantDomain(tenantDomain);
            }

            //check if there are any existing key mappings set for the application and the key manager.
            if (apiMgtDAO.isKeyMappingExistsForApplication(application.getId(), keyManagerName, keyManagerId,
                    tokenType)) {
                throw new APIManagementException("Key Mappings already exists for application " + application.getName(),
                        ExceptionCodes.KEY_MAPPING_ALREADY_EXIST);
            }

            // initiate WorkflowExecutor
            WorkflowExecutor appRegistrationWorkflow = null;
            // initiate ApplicationRegistrationWorkflowDTO
            ApplicationRegistrationWorkflowDTO appRegWFDto = null;

            ApplicationKeysDTO appKeysDto = new ApplicationKeysDTO();

            boolean isCaseInsensitiveComparisons = Boolean.parseBoolean(getAPIManagerConfiguration().
                    getFirstProperty(APIConstants.API_STORE_FORCE_CI_COMPARISIONS));

            boolean isUserAppOwner;
            if (isCaseInsensitiveComparisons) {
                isUserAppOwner = application.getSubscriber().getName().equalsIgnoreCase(userId);
            } else {
                isUserAppOwner = application.getSubscriber().getName().equals(userId);
            }

            if (!isUserAppOwner) {
                throw new APIManagementException("user: " + application.getSubscriber().getName() + ", " +
                        "attempted to generate tokens for application owned by: " + userId);
            }

            // if its a PRODUCTION application.
            if (APIConstants.API_KEY_TYPE_PRODUCTION.equals(tokenType)) {
                // initiate workflow type. By default simple work flow will be
                // executed.
                appRegistrationWorkflow =
                        getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_APPLICATION_REGISTRATION_PRODUCTION);
                appRegWFDto =
                        (ApplicationRegistrationWorkflowDTO) WorkflowExecutorFactory.getInstance()
                                .createWorkflowDTO(WorkflowConstants.WF_TYPE_AM_APPLICATION_REGISTRATION_PRODUCTION);

            }// if it is a sandBox application.
            else if (APIConstants.API_KEY_TYPE_SANDBOX.equals(tokenType)) {
                // if its a SANDBOX application.
                appRegistrationWorkflow =
                        getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_APPLICATION_REGISTRATION_SANDBOX);
                appRegWFDto =
                        (ApplicationRegistrationWorkflowDTO) WorkflowExecutorFactory.getInstance()
                                .createWorkflowDTO(WorkflowConstants.WF_TYPE_AM_APPLICATION_REGISTRATION_SANDBOX);
            } else {
                throw new APIManagementException("Invalid Token Type '" + tokenType + "' requested.");
            }

            //check whether callback url is empty and set null
            if (StringUtils.isBlank(callbackUrl)) {
                callbackUrl = null;
            }
            String applicationTokenType = application.getTokenType();
            if (StringUtils.isEmpty(application.getTokenType())) {
                applicationTokenType = APIConstants.DEFAULT_TOKEN_TYPE;
            }
            // Build key manager instance and create oAuthAppRequest by jsonString.
            OAuthAppRequest request =
                    ApplicationUtils
                            .createOauthAppRequest(application.getName(), null, callbackUrl, tokenScope, jsonString,
                                    applicationTokenType, tenantDomain, keyManagerName);
            request.getOAuthApplicationInfo().addParameter(ApplicationConstants.VALIDITY_PERIOD, validityTime);
            request.getOAuthApplicationInfo().addParameter(ApplicationConstants.APP_KEY_TYPE, tokenType);
            request.getOAuthApplicationInfo().addParameter(ApplicationConstants.APP_CALLBACK_URL, callbackUrl);
            request.getOAuthApplicationInfo().setApplicationUUID(application.getUUID());

            // Setting request values in WorkflowDTO - In future we should keep
            // Application/OAuthApplication related
            // information in the respective entities not in the workflowDTO.
            appRegWFDto.setStatus(WorkflowStatus.CREATED);
            appRegWFDto.setCreatedTime(System.currentTimeMillis());
            appRegWFDto.setTenantDomain(tenantDomain);
            appRegWFDto.setTenantId(tenantId);
            appRegWFDto.setExternalWorkflowReference(appRegistrationWorkflow.generateUUID());
            appRegWFDto.setWorkflowReference(appRegWFDto.getExternalWorkflowReference());
            appRegWFDto.setApplication(application);
            appRegWFDto.setKeyManager(keyManagerId);
            request.setMappingId(appRegWFDto.getWorkflowReference());
            if (!application.getSubscriber().getName().equals(userId)) {
                appRegWFDto.setUserName(application.getSubscriber().getName());
            } else {
                appRegWFDto.setUserName(userId);
            }

            appRegWFDto.setCallbackUrl(appRegistrationWorkflow.getCallbackURL());
            appRegWFDto.setAppInfoDTO(request);
            appRegWFDto.setDomainList(allowedDomains);

            appRegWFDto.setKeyDetails(appKeysDto);
            appRegistrationWorkflow.execute(appRegWFDto);

            Map<String, Object> keyDetails = new HashMap<String, Object>();
            keyDetails.put(APIConstants.FrontEndParameterNames.KEY_STATE, appRegWFDto.getStatus().toString());
            OAuthApplicationInfo applicationInfo = appRegWFDto.getApplicationInfo();
            String keyMappingId = apiMgtDAO.getKeyMappingIdFromApplicationIdKeyTypeAndKeyManager(application.getId(),
                    tokenType, keyManagerId);
            keyDetails.put(APIConstants.FrontEndParameterNames.KEY_MAPPING_ID, keyMappingId);
            if (applicationInfo != null) {
                keyDetails.put(APIConstants.FrontEndParameterNames.CONSUMER_KEY, applicationInfo.getClientId());
                keyDetails.put(APIConstants.FrontEndParameterNames.CONSUMER_SECRET, applicationInfo.getClientSecret());
                keyDetails.put(ApplicationConstants.OAUTH_APP_DETAILS, applicationInfo.getJsonString());
                keyDetails.put(APIConstants.FrontEndParameterNames.MODE, APIConstants.OAuthAppMode.CREATED.name());
            }

            // There can be instances where generating the Application Token is
            // not required. In those cases,
            // token info will have nothing.
            AccessTokenInfo tokenInfo = appRegWFDto.getAccessTokenInfo();
            if (tokenInfo != null) {
                keyDetails.put("accessToken", tokenInfo.getAccessToken());
                keyDetails.put("validityTime", tokenInfo.getValidityPeriod());
                keyDetails.put("tokenDetails", tokenInfo.getJSONString());
                keyDetails.put("tokenScope", tokenInfo.getScopes());
            }

            JSONObject appLogObject = new JSONObject();
            appLogObject.put("Generated keys for application", application.getName());
            APIUtil.logAuditMessage(APIConstants.AuditLogConstants.APPLICATION, appLogObject.toString(),
                    APIConstants.AuditLogConstants.UPDATED, this.username);

            String orgId = application.getOrganization();
            // if its a PRODUCTION application.
            if (APIConstants.API_KEY_TYPE_PRODUCTION.equals(tokenType)) {
                // get the workflow state once the executor is executed.
                WorkflowDTO wfDTO =
                 apiMgtDAO.retrieveWorkflowFromInternalReference(appRegWFDto.getExternalWorkflowReference(),
                        WorkflowConstants.WF_TYPE_AM_APPLICATION_REGISTRATION_PRODUCTION);
                // only send the notification if approved
                // wfDTO is null when simple wf executor is used because wf state is not stored in the db and is
                // always approved.
                if (wfDTO != null) {
                    if (WorkflowStatus.APPROVED.equals(wfDTO.getStatus())) {
                        ApplicationRegistrationEvent applicationRegistrationEvent = new ApplicationRegistrationEvent(
                                UUID.randomUUID().toString(), System.currentTimeMillis(),
                                APIConstants.EventType.APPLICATION_REGISTRATION_CREATE.name(), tenantId, orgId,
                                application.getId(), application.getUUID(), applicationInfo.getClientId(), tokenType,
                                keyManagerName);
                        APIUtil.sendNotification(applicationRegistrationEvent,
                                APIConstants.NotifierType.APPLICATION_REGISTRATION.name());
                    }
                } else {
                    ApplicationRegistrationEvent applicationRegistrationEvent = new ApplicationRegistrationEvent(
                            UUID.randomUUID().toString(), System.currentTimeMillis(),
                            APIConstants.EventType.APPLICATION_REGISTRATION_CREATE.name(), tenantId, orgId,
                            application.getId(), application.getUUID(), applicationInfo.getClientId(), tokenType,
                            keyManagerName);
                    APIUtil.sendNotification(applicationRegistrationEvent,
                            APIConstants.NotifierType.APPLICATION_REGISTRATION.name());
                }
            } else if (APIConstants.API_KEY_TYPE_SANDBOX.equals(tokenType)) {
                // get the workflow state once the executor is executed.
                WorkflowDTO wfDTO =
                 apiMgtDAO.retrieveWorkflowFromInternalReference(appRegWFDto.getExternalWorkflowReference(),
                        WorkflowConstants.WF_TYPE_AM_APPLICATION_REGISTRATION_SANDBOX);
                // only send the notification if approved
                // wfDTO is null when simple wf executor is used because wf state is not stored in the db and is
                // always approved.
                if (wfDTO != null) {
                    if (WorkflowStatus.APPROVED.equals(wfDTO.getStatus())) {
                        ApplicationRegistrationEvent applicationRegistrationEvent = new ApplicationRegistrationEvent(
                                UUID.randomUUID().toString(), System.currentTimeMillis(),
                                APIConstants.EventType.APPLICATION_REGISTRATION_CREATE.name(), tenantId, orgId,
                                application.getId(), application.getUUID(), applicationInfo.getClientId(), tokenType,
                                keyManagerName);
                        APIUtil.sendNotification(applicationRegistrationEvent,
                                APIConstants.NotifierType.APPLICATION_REGISTRATION.name());
                    }
                } else {
                    ApplicationRegistrationEvent applicationRegistrationEvent = new ApplicationRegistrationEvent(
                            UUID.randomUUID().toString(), System.currentTimeMillis(),
                            APIConstants.EventType.APPLICATION_REGISTRATION_CREATE.name(), tenantId, orgId,
                            application.getId(), application.getUUID(), applicationInfo.getClientId(), tokenType,
                            keyManagerName);
                    APIUtil.sendNotification(applicationRegistrationEvent,
                            APIConstants.NotifierType.APPLICATION_REGISTRATION.name());
                }
            }
            return keyDetails;
        } catch (WorkflowException e) {
            log.error("Could not execute Workflow", e);
            throw new APIManagementException(e);
        } finally {
            if (isTenantFlowStarted) {
                endTenantFlow();
            }
        }
    }

    private static List<Scope> getAllowedScopesForUserApplication(String username,
                                                                  Set<Scope> reqScopeSet) {

        String[] userRoles = null;
        org.wso2.carbon.user.api.UserStoreManager userStoreManager = null;
        String preservedCaseSensitiveValue = System.getProperty(PRESERVED_CASE_SENSITIVE_VARIABLE);
        boolean preservedCaseSensitive = JavaUtils.isTrueExplicitly(preservedCaseSensitiveValue);

        List<Scope> authorizedScopes = new ArrayList<Scope>();
        try {
            RealmService realmService = ServiceReferenceHolder.getInstance().getRealmService();
            int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                    .getTenantId(MultitenantUtils.getTenantDomain(username));
            userStoreManager = realmService.getTenantUserRealm(tenantId).getUserStoreManager();
            userRoles = userStoreManager.getRoleListOfUser(MultitenantUtils.getTenantAwareUsername(username));
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            // Log and return since we do not want to stop issuing the token in
            // case of scope validation failures.
            log.error("Error when getting the tenant's UserStoreManager or when getting roles of user ", e);
        }

        List<String> userRoleList;
        if (userRoles != null) {
            if (preservedCaseSensitive) {
                userRoleList = Arrays.asList(userRoles);
            } else {
                userRoleList = new ArrayList<String>();
                for (String userRole : userRoles) {
                    userRoleList.add(userRole.toLowerCase());
                }
            }
        } else {
            userRoleList = Collections.emptyList();
        }

        //Iterate the requested scopes list.
        for (Scope scope : reqScopeSet) {
            //Get the set of roles associated with the requested scope.
            String roles = scope.getRoles();

            //If the scope has been defined in the context of the App and if roles have been defined for the scope
            if (roles != null && roles.length() != 0) {
                List<String> roleList = new ArrayList<String>();
                for (String scopeRole : roles.split(",")) {
                    if (preservedCaseSensitive) {
                        roleList.add(scopeRole.trim());
                    } else {
                        roleList.add(scopeRole.trim().toLowerCase());
                    }
                }
                //Check if user has at least one of the roles associated with the scope
                roleList.retainAll(userRoleList);
                if (!roleList.isEmpty()) {
                    authorizedScopes.add(scope);
                }
            }
        }

        return authorizedScopes;
    }

    /**
     * Returns the corresponding application given the Id
     *
     * @param id Id of the Application
     * @return it will return Application corresponds to the id.
     * @throws APIManagementException
     */
    @Override
    public Application getApplicationById(int id) throws APIManagementException {

        Application application = apiMgtDAO.getApplicationById(id);
        if (application != null) {
            Set<APIKey> keys = getApplicationKeys(application.getId());
            for (APIKey key : keys) {
                application.addKey(key);
            }
        }
        return application;
    }

    /*
     * @see super.getApplicationById(int id, String userId, String groupId)
     * */
    @Override
    public Application getApplicationById(int id, String userId, String groupId) throws APIManagementException {

        Application application = apiMgtDAO.getApplicationById(id, userId, groupId);
        if (application != null) {
            checkAppAttributes(application, userId);
            Set<APIKey> keys = getApplicationKeys(application.getId());
            for (APIKey key : keys) {
                application.addKey(key);
            }
        }
        return application;
    }

    @Override
    public Set<SubscribedAPI> getSubscribedIdentifiers(Subscriber subscriber, Identifier identifier, String groupingId,
                                                       String organization) throws APIManagementException {

        Set<SubscribedAPI> subscribedAPISet = new HashSet<>();
        Set<SubscribedAPI> subscribedAPIs = getSubscribedAPIs(organization, subscriber, groupingId);
        for (SubscribedAPI api : subscribedAPIs) {
            if (identifier instanceof APIIdentifier && identifier.equals(api.getAPIIdentifier())) {
                Set<APIKey> keys = getApplicationKeys(api.getApplication().getId());
                for (APIKey key : keys) {
                    api.addKey(key);
                }
                subscribedAPISet.add(api);
            } else if (identifier instanceof APIProductIdentifier && identifier.equals(api.getProductId())) {
                Set<APIKey> keys = getApplicationKeys(api.getApplication().getId());
                for (APIKey key : keys) {
                    api.addKey(key);
                }
                subscribedAPISet.add(api);
            }
        }
        return subscribedAPISet;
    }

    /**
     * Returns a list of tiers denied
     *
     * @return Set<Tier>
     */
    @Override
    public Set<String> getDeniedTiers() throws APIManagementException {
        // '0' is passed as argument whenever tenant id of logged in user is needed
        return getDeniedTiers(0);
    }

    /**
     * Returns a list of tiers denied
     *
     * @param apiProviderTenantId tenant id of API provider
     * @return Set<Tier>
     */
    @Override
    public Set<String> getDeniedTiers(int apiProviderTenantId) throws APIManagementException {

        Set<String> deniedTiers = new HashSet<String>();
        String[] currentUserRoles;
        Set<TierPermissionDTO> tierPermissions = apiMgtDAO.getThrottleTierPermissions(apiProviderTenantId);
        if (apiProviderTenantId == 0) {
            apiProviderTenantId = tenantId;
        }
        if (apiProviderTenantId != 0) {
            if (APIUtil.isOnPremResolver()) {
                if (tenantId != apiProviderTenantId) {
                    // if OnPrem Cross Tenant Scenario we will not able to validate roles allow or deny to policy
                    // therefore any POLICY that have a permission attached marked as deny policy.
                    for (TierPermissionDTO tierPermission : tierPermissions) {
                        deniedTiers.add(tierPermission.getTierName());
                    }
                    return deniedTiers;
                }
            }

            /* Get the roles of the Current User */
            String userName = (userNameWithoutChange != null) ? userNameWithoutChange : username;
            currentUserRoles = APIUtil.getListOfRoles(userName);

            for (TierPermissionDTO tierPermission : tierPermissions) {
                String type = tierPermission.getPermissionType();

                List<String> currentRolesList = new ArrayList<String>(Arrays.asList(currentUserRoles));
                String[] rolesList = tierPermission.getRoles();
                List<String> roles = new ArrayList<>();
                if (rolesList != null) {
                    roles = new ArrayList<>(Arrays.asList(rolesList));
                }
                currentRolesList.retainAll(roles);

                if (APIConstants.TIER_PERMISSION_ALLOW.equals(type)) {
                    /* Current User is not allowed for this Tier*/
                    if (currentRolesList.isEmpty()) {
                        deniedTiers.add(tierPermission.getTierName());
                    }
                } else {
                    /* Current User is denied for this Tier*/
                    if (currentRolesList.size() > 0) {
                        deniedTiers.add(tierPermission.getTierName());
                    }
                }
            }
        }
        return deniedTiers;
    }

    @Override
    public Set<String> getDeniedTiers(String organization) throws APIManagementException {

        int tenantId = APIUtil.getInternalOrganizationId(organization);
        return getDeniedTiers(tenantId);
    }

    @Override
    public Set<TierPermission> getTierPermissions() throws APIManagementException {

        Set<TierPermission> tierPermissions = new HashSet<TierPermission>();
        if (tenantId != 0) {
            Set<TierPermissionDTO> tierPermissionDtos = apiMgtDAO.getThrottleTierPermissions(tenantId);

            for (TierPermissionDTO tierDto : tierPermissionDtos) {
                TierPermission tierPermission = new TierPermission(tierDto.getTierName());
                tierPermission.setRoles(tierDto.getRoles());
                tierPermission.setPermissionType(tierDto.getPermissionType());
                tierPermissions.add(tierPermission);
            }
        }
        return tierPermissions;
    }

    private boolean isTenantDomainNotMatching(String tenantDomain) {

        if (this.tenantDomain != null) {
            return !(this.tenantDomain.equals(tenantDomain));
        }
        return true;
    }

    @Override
    public Set<API> searchAPI(String searchTerm, String searchType, String tenantDomain)
            throws APIManagementException {

        return null;
    }

    public Set<Scope> getScopesBySubscribedAPIs(List<String> uuids)
            throws APIManagementException {

        Set<String> scopeKeySet = apiMgtDAO.getScopesBySubscribedAPIs(uuids);
        return new LinkedHashSet<>(APIUtil.getScopes(scopeKeySet, tenantDomain).values());
    }

    @Override
    public String getGroupId(int appId) throws APIManagementException {

        return apiMgtDAO.getGroupId(appId);
    }

    @Override
    public String[] getGroupIds(String response) throws APIManagementException {

        String groupingExtractorClass = APIUtil.getGroupingExtractorImplementation();
        return APIUtil.getGroupIdsFromExtractor(response, groupingExtractorClass);
    }

    /**
     * Returns all applications associated with given subscriber, groupingId and search criteria.
     *
     * @param subscriber   Subscriber
     * @param groupingId   The groupId to which the applications must belong.
     * @param offset       The offset.
     * @param search       The search string.
     * @param sortColumn   The sort column.
     * @param sortOrder    The sort order.
     * @param organization Identifier of an Organization
     * @return Application[] The Applications.
     * @throws APIManagementException
     */
    @Override
    public Application[] getApplicationsWithPagination(Subscriber subscriber, String groupingId, int start, int offset
            , String search, String sortColumn, String sortOrder, String organization)
            throws APIManagementException {

        if (APIUtil.isOnPremResolver()) {
            organization = tenantDomain;
        }
        return apiMgtDAO.getApplicationsWithPagination(subscriber, groupingId, start, offset,
                search, sortColumn, sortOrder, organization);
    }

    /**
     * Returns a single string containing the provided array of scopes.
     *
     * @param scopes The array of scopes.
     * @return String Single string containing the provided array of scopes.
     */
    private String getScopeString(String[] scopes) {

        return StringUtils.join(scopes, " ");
    }

    /**
     * @param userId         Subscriber name.
     * @param application    The Application.
     * @param tokenType      Token type (PRODUCTION | SANDBOX)
     * @param callbackUrl    callback URL
     * @param allowedDomains allowedDomains for token.
     * @param validityTime   validity time period.
     * @param tokenScope     Scopes for the requested tokens.
     * @param groupingId     APIM application id.
     * @param jsonString     Callback URL for the Application.
     * @param keyManagerID   Key Manager ID of the relevant Key Manager
     * @return
     * @throws APIManagementException
     */
    @Override
    public OAuthApplicationInfo updateAuthClient(String userId, Application application,
                                                 String tokenType,
                                                 String callbackUrl, String[] allowedDomains,
                                                 String validityTime,
                                                 String tokenScope,
                                                 String groupingId,
                                                 String jsonString, String keyManagerID) throws APIManagementException {

        boolean tenantFlowStarted = false;
        try {
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
                tenantFlowStarted = true;
            }

            final String subscriberName = application.getSubscriber().getName();

            boolean isCaseInsensitiveComparisons = Boolean.parseBoolean(getAPIManagerConfiguration().
                    getFirstProperty(APIConstants.API_STORE_FORCE_CI_COMPARISIONS));

            boolean isUserAppOwner;
            if (isCaseInsensitiveComparisons) {
                isUserAppOwner = subscriberName.equalsIgnoreCase(userId);
            } else {
                isUserAppOwner = subscriberName.equals(userId);
            }

            if (!isUserAppOwner) {
                throw new APIManagementException("user: " + userId + ", attempted to update OAuth application " +
                        "owned by: " + subscriberName);
            }
            String keyManagerName;
            KeyManagerConfigurationDTO keyManagerConfiguration =
                    apiMgtDAO.getKeyManagerConfigurationByUUID(keyManagerID);
            String keyManagerTenant;
            if (keyManagerConfiguration != null) {
                keyManagerName = keyManagerConfiguration.getName();
                keyManagerTenant = keyManagerConfiguration.getOrganization();
            } else {
                //keeping this just in case the name is sent by mistake.
                keyManagerConfiguration =
                        apiMgtDAO.getKeyManagerConfigurationByName(tenantDomain, keyManagerID);
                if (keyManagerConfiguration == null) {
                    throw new APIManagementException("Key Manager " + keyManagerID + " couldn't found.",
                            ExceptionCodes.KEY_MANAGER_NOT_REGISTERED);
                } else {
                    keyManagerName = keyManagerID;
                    keyManagerID = keyManagerConfiguration.getUuid();
                    keyManagerTenant = keyManagerConfiguration.getOrganization();
                }
            }

            if (!keyManagerConfiguration.isEnabled()) {
                throw new APIManagementException("Key Manager " + keyManagerName + " not activated in the requested " +
                        "Tenant", ExceptionCodes.KEY_MANAGER_NOT_ENABLED);
            }
            if (KeyManagerConfiguration.TokenType.EXCHANGED.toString().equals(keyManagerConfiguration.getTokenType())) {
                throw new APIManagementException("Key Manager " + keyManagerName + " doesn't support to generate" +
                        " Client Application", ExceptionCodes.KEY_MANAGER_NOT_SUPPORTED_TOKEN_GENERATION);
            }
            //Create OauthAppRequest object by passing json String.
            OAuthAppRequest oauthAppRequest = ApplicationUtils.createOauthAppRequest(application.getName(), null,
                    callbackUrl, tokenScope, jsonString, application.getTokenType(), keyManagerTenant, keyManagerName);

            oauthAppRequest.getOAuthApplicationInfo().addParameter(ApplicationConstants.APP_KEY_TYPE, tokenType);
            String consumerKey = apiMgtDAO
                    .getConsumerKeyByApplicationIdKeyTypeKeyManager(application.getId(), tokenType, keyManagerID);

            oauthAppRequest.getOAuthApplicationInfo().setClientId(consumerKey);
            //get key manager instance.
            KeyManager keyManager = KeyManagerHolder.getKeyManagerInstance(keyManagerTenant, keyManagerName);
            if (keyManager == null) {
                throw new APIManagementException(
                        "Key Manager " + keyManagerName + " not initialized in the requested" + "Tenant",
                        ExceptionCodes.KEY_MANAGER_INITIALIZATION_FAILED);
            }
            // set application attributes
            oauthAppRequest.getOAuthApplicationInfo().putAllAppAttributes(application.getApplicationAttributes());
            oauthAppRequest.getOAuthApplicationInfo().setApplicationUUID(application.getUUID());
            //call update method.
            OAuthApplicationInfo updatedAppInfo = keyManager.updateApplication(oauthAppRequest);
            apiMgtDAO.updateApplicationKeyTypeMetaData(application.getId(), tokenType, keyManagerID, updatedAppInfo);
            JSONObject appLogObject = new JSONObject();
            appLogObject.put(APIConstants.AuditLogConstants.APPLICATION_NAME, updatedAppInfo.getClientName());
            appLogObject.put("Updated Oauth app with Call back URL", callbackUrl);
            appLogObject.put("Updated Oauth app with grant types", jsonString);

            APIUtil.logAuditMessage(APIConstants.AuditLogConstants.APPLICATION, appLogObject.toString(),
                    APIConstants.AuditLogConstants.UPDATED, this.username);
            return updatedAppInfo;
        } finally {
            if (tenantFlowStarted) {
                endTenantFlow();
            }
        }

    }

    public boolean isSubscriberValid(String userId)
            throws APIManagementException {

        boolean isSubscribeValid = false;
        if (apiMgtDAO.getSubscriber(userId) != null) {
            isSubscribeValid = true;
        } else {
            return false;
        }
        return isSubscribeValid;
    }

    public boolean updateApplicationOwner(String userId, String organization, Application application) throws APIManagementException {

        boolean isAppUpdated;
        String consumerKey;
        String oldUserName = application.getSubscriber().getName();
        String oldTenantDomain = MultitenantUtils.getTenantDomain(oldUserName);
        String newTenantDomain = MultitenantUtils.getTenantDomain(userId);
        if (oldTenantDomain.equals(newTenantDomain)) {
            if (!isSubscriberValid(userId)) {
                RealmService realmService = ServiceReferenceHolder.getInstance().getRealmService();
                try {
                    int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                            .getTenantId(newTenantDomain);
                    UserStoreManager userStoreManager = realmService.getTenantUserRealm(tenantId).getUserStoreManager();
                    if (userStoreManager.isExistingUser(userId)) {
                        if (apiMgtDAO.getSubscriber(userId) == null) {
                            addSubscriber(userId, "");
                        }
                    } else {
                        throw new APIManagementException("User " + userId + " doesn't exist in user store");
                    }
                } catch (UserStoreException e) {
                    throw new APIManagementException("Error while adding user " + userId + " as a subscriber");
                }
            }
            String applicationName = application.getName();
            if (!APIUtil.isApplicationOwnedBySubscriber(userId, applicationName, organization)) {
                for (APIKey apiKey : application.getKeys()) {
                    KeyManager keyManager = KeyManagerHolder.getTenantKeyManagerInstance(
                            APIConstants.GLOBAL_KEY_MANAGER_TENANT_DOMAIN, apiKey.getKeyManager());
                    if (keyManager != null) {
                        // Prevent updating the OAuth app owner in the case of Global Key Manager.
                        continue;
                    } else {
                        keyManager =
                                KeyManagerHolder.getTenantKeyManagerInstance(tenantDomain, apiKey.getKeyManager());
                    }
                    /* retrieving OAuth application information for specific consumer key */
                    consumerKey = apiKey.getConsumerKey();
                    OAuthApplicationInfo oAuthApplicationInfo = keyManager.retrieveApplication(consumerKey);
                    if (oAuthApplicationInfo.getParameter(ApplicationConstants.OAUTH_CLIENT_NAME) != null) {
                        OAuthAppRequest oauthAppRequest = ApplicationUtils.createOauthAppRequest(oAuthApplicationInfo.
                                        getParameter(ApplicationConstants.OAUTH_CLIENT_NAME).toString(), null,
                                oAuthApplicationInfo.getCallBackURL(), null,
                                null, application.getTokenType(), this.tenantDomain, apiKey.getKeyManager());
                        oauthAppRequest.getOAuthApplicationInfo().setAppOwner(userId);
                        oauthAppRequest.getOAuthApplicationInfo().setClientId(consumerKey);
                        /* updating the owner of the OAuth application with userId */
                        OAuthApplicationInfo updatedAppInfo = keyManager.updateApplicationOwner(oauthAppRequest,
                                userId);
                        isAppUpdated = true;
                        audit.info("Successfully updated the owner of application " + application.getName() +
                                " from " + oldUserName + " to " + userId + ".");
                    } else {
                        throw new APIManagementException("Unable to retrieve OAuth application information.");
                    }
                }
            } else {
                throw new APIManagementException("Unable to update application owner to " + userId +
                        " as this user has an application with the same name. Update owner to another user.");
            }
        } else {
            throw new APIManagementException("Unable to update application owner to " +
                    userId + " as this user does not belong to " + oldTenantDomain + " domain.");
        }

        isAppUpdated = apiMgtDAO.updateApplicationOwner(userId, application);
        return isAppUpdated;
    }

    public JSONObject resumeWorkflow(Object[] args) {

        JSONObject row = new JSONObject();

        if (args != null && APIUtil.isStringArray(args)) {

            String workflowReference = (String) args[0];
            String status = (String) args[1];
            String description = null;
            if (args.length > 2 && args[2] != null) {
                description = (String) args[2];
            }

            boolean isTenantFlowStarted = false;

            try {
                //                if (workflowReference != null) {
                WorkflowDTO workflowDTO = apiMgtDAO.retrieveWorkflow(workflowReference);
                if (workflowDTO == null) {
                    log.error("Could not find workflow for reference " + workflowReference);

                    row.put("error", Boolean.TRUE);
                    row.put("statusCode", 500);
                    row.put("message", "Could not find workflow for reference " + workflowReference);
                    return row;
                }

                String tenantDomain = workflowDTO.getTenantDomain();
                if (tenantDomain != null && !org.wso2.carbon.utils.multitenancy.MultitenantConstants
                        .SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                    isTenantFlowStarted = startTenantFlowForTenantDomain(tenantDomain);
                }

                workflowDTO.setWorkflowDescription(description);
                workflowDTO.setStatus(WorkflowStatus.valueOf(status));

                String workflowType = workflowDTO.getWorkflowType();
                WorkflowExecutor workflowExecutor;
                try {
                    workflowExecutor = getWorkflowExecutor(workflowType);
                    workflowExecutor.complete(workflowDTO);
                    if (WorkflowStatus.APPROVED.equals(workflowDTO.getStatus())) {
                        WorkflowUtils.sendNotificationAfterWFComplete(workflowDTO, workflowType);
                    }
                } catch (WorkflowException e) {
                    throw new APIManagementException(e);
                }
                row.put("error", Boolean.FALSE);
                row.put("statusCode", 200);
                row.put("message", "Invoked workflow completion successfully.");
                //                }
            } catch (IllegalArgumentException e) {
                String msg = "Illegal argument provided. Valid values for status are APPROVED and REJECTED.";
                log.error(msg, e);

                row.put("error", Boolean.TRUE);
                row.put("statusCode", 500);
                row.put("message", msg);

            } catch (APIManagementException e) {
                String msg = "Error while resuming the workflow. ";
                log.error(msg, e);

                row.put("error", Boolean.TRUE);
                row.put("statusCode", 500);
                row.put("message", msg + e.getMessage());
            } finally {
                if (isTenantFlowStarted) {
                    endTenantFlow();
                }
            }
        }
        return row;
    }

    protected void endTenantFlow() {

        PrivilegedCarbonContext.endTenantFlow();
    }

    protected boolean startTenantFlowForTenantDomain(String tenantDomain) {

        boolean isTenantFlowStarted = true;
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
        return isTenantFlowStarted;
    }

    /**
     * Returns a workflow executor
     *
     * @param workflowType Workflow executor type
     * @return WorkflowExecutor of given type
     * @throws WorkflowException if an error occurred while getting WorkflowExecutor
     */
    protected WorkflowExecutor getWorkflowExecutor(String workflowType) throws WorkflowException {

        return WorkflowExecutorFactory.getInstance().getWorkflowExecutor(workflowType);
    }

    @Override
    public boolean isMonetizationEnabled(String tenantDomain) throws APIManagementException {

        JSONObject apiTenantConfig = APIUtil.getTenantConfig(tenantDomain);
        return getTenantConfigValue(tenantDomain, apiTenantConfig, APIConstants.API_TENANT_CONF_ENABLE_MONITZATION_KEY);
    }

    private boolean getTenantConfigValue(String tenantDomain, JSONObject apiTenantConfig, String configKey) throws APIManagementException {

        if (apiTenantConfig.size() != 0) {
            Object value = apiTenantConfig.get(configKey);

            if (value != null) {
                return Boolean.parseBoolean(value.toString());
            } else {
                throw new APIManagementException(configKey + " config does not exist for tenant " + tenantDomain);
            }
        }
        return false;
    }

    /**
     * To get the query to retrieve user role list query based on current role list.
     *
     * @return the query with user role list.
     * @throws APIManagementException API Management Exception.
     */
    private String getUserRoleListQuery() throws APIManagementException {

        StringBuilder rolesQuery = new StringBuilder();
        rolesQuery.append('(');
        rolesQuery.append(APIConstants.NULL_USER_ROLE_LIST);
        String[] userRoles = APIUtil.getListOfRoles((userNameWithoutChange != null) ? userNameWithoutChange : username);
        String skipRolesByRegex = APIUtil.getSkipRolesByRegex();
        if (StringUtils.isNotEmpty(skipRolesByRegex)) {
            List<String> filteredUserRoles = new ArrayList<>(Arrays.asList(userRoles));
            String[] regexList = skipRolesByRegex.split(",");
            for (int i = 0; i < regexList.length; i++) {
                Pattern p = Pattern.compile(regexList[i]);
                Iterator<String> itr = filteredUserRoles.iterator();
                while (itr.hasNext()) {
                    String role = itr.next();
                    Matcher m = p.matcher(role);
                    if (m.matches()) {
                        itr.remove();
                    }
                }
            }
            userRoles = filteredUserRoles.toArray(new String[0]);
        }
        if (userRoles != null) {
            for (String userRole : userRoles) {
                rolesQuery.append(" OR ");
                rolesQuery.append(ClientUtils.escapeQueryChars(APIUtil.sanitizeUserRole(userRole.toLowerCase())));
            }
        }
        rolesQuery.append(")");
        if (log.isDebugEnabled()) {
            log.debug("User role list solr query " + APIConstants.STORE_VIEW_ROLES + "=" + rolesQuery.toString());
        }
        return APIConstants.STORE_VIEW_ROLES + "=" + rolesQuery.toString();
    }

    /**
     * To get the current user's role list.
     *
     * @return user role list.
     * @throws APIManagementException API Management Exception.
     */
    private List<String> getUserRoleList() throws APIManagementException {

        List<String> userRoleList;
        if (userNameWithoutChange == null) {
            userRoleList = new ArrayList<String>() {{
                add(APIConstants.NULL_USER_ROLE_LIST);
            }};
        } else {
            userRoleList = new ArrayList<String>(Arrays.asList(APIUtil.getListOfRoles(userNameWithoutChange)));
        }
        return userRoleList;
    }

    @Override
    protected String getSearchQuery(String searchQuery) throws APIManagementException {

        if (!isAccessControlRestrictionEnabled || (userNameWithoutChange != null &&
                APIUtil.hasPermission(userNameWithoutChange, APIConstants.Permissions
                        .APIM_ADMIN))) {
            return searchQuery;
        }
        String criteria = getUserRoleListQuery();
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            criteria = criteria + "&" + searchQuery;
        }
        return criteria;
    }

    @Override
    public ResourceFile getWSDL(API api, String environmentName, String environmentType, String organization)
            throws APIManagementException {

        WSDLValidationResponse validationResponse;
        ResourceFile resourceFile = getWSDL(api.getUuid(), organization);
        if (resourceFile.getContentType().contains(APIConstants.APPLICATION_ZIP)) {
            validationResponse = APIMWSDLReader.extractAndValidateWSDLArchive(resourceFile.getContent());
        } else {
            validationResponse = APIMWSDLReader.validateWSDLFile(resourceFile.getContent());
        }
        if (validationResponse.isValid()) {
            WSDLProcessor wsdlProcessor = validationResponse.getWsdlProcessor();
            wsdlProcessor.updateEndpoints(api, environmentName, environmentType);
            InputStream wsdlDataStream = wsdlProcessor.getWSDL();
            return new ResourceFile(wsdlDataStream, resourceFile.getContentType());
        } else {
            throw new APIManagementException(ExceptionCodes.from(ExceptionCodes.CORRUPTED_STORED_WSDL,
                    api.getId().toString()));
        }
    }

    @Override
    public Set<SubscribedAPI> getLightWeightSubscribedIdentifiers(String organization, Subscriber subscriber,
                                                                  APIIdentifier apiIdentifier, String groupingId) throws APIManagementException {

        Set<SubscribedAPI> subscribedAPISet = new HashSet<SubscribedAPI>();
        Set<SubscribedAPI> subscribedAPIs = getLightWeightSubscribedAPIs(organization, subscriber, groupingId);
        for (SubscribedAPI api : subscribedAPIs) {
            if (api.getAPIIdentifier().equals(apiIdentifier)) {
                subscribedAPISet.add(api);
            }
        }
        return subscribedAPISet;
    }

    public Set<APIKey> getApplicationKeysOfApplication(int applicationId) throws APIManagementException {

        Set<APIKey> apikeys = getApplicationKeys(applicationId);
        return apikeys;
    }

    public Set<APIKey> getApplicationKeysOfApplication(int applicationId, String xWso2Tenant)
            throws APIManagementException {

        return getApplicationKeys(applicationId, xWso2Tenant);
    }

    /**
     * This method is used to get keys of custom attributes, configured by user
     *
     * @param userId user name of logged in user
     * @return Array of JSONObject, contains keys of attributes
     * @throws APIManagementException
     */
    public JSONArray getAppAttributesFromConfig(String userId) throws APIManagementException {

        String tenantDomain = MultitenantUtils.getTenantDomain(userId);
        int tenantId = 0;
        try {
            tenantId = getTenantId(tenantDomain);
        } catch (UserStoreException e) {
            handleException("Error in getting tenantId of " + tenantDomain, e);
        }
        JSONArray applicationAttributes = null;
        JSONObject applicationConfig = APIUtil.getAppAttributeKeysFromRegistry(tenantDomain);
        if (applicationConfig != null) {
            applicationAttributes = (JSONArray) applicationConfig.get(APIConstants.ApplicationAttributes.ATTRIBUTES);
        } else {
            APIManagerConfiguration configuration = getAPIManagerConfiguration();
            applicationAttributes = configuration.getApplicationAttributes();
        }
        return applicationAttributes;
    }

    /**
     * This method is used to validate keys of custom attributes, configured by user
     *
     * @param application
     * @param userId      user name of logged in user
     * @throws APIManagementException
     */
    public void checkAppAttributes(Application application, String userId) throws APIManagementException {

        JSONArray applicationAttributesFromConfig = getAppAttributesFromConfig(userId);
        Map<String, String> applicationAttributes = application.getApplicationAttributes();
        List attributeKeys = new ArrayList<String>();
        int applicationId = application.getId();
        int tenantId = 0;
        Map<String, String> newApplicationAttributes = new HashMap<>();
        String tenantDomain = MultitenantUtils.getTenantDomain(userId);
        try {
            tenantId = getTenantId(tenantDomain);
        } catch (UserStoreException e) {
            handleException("Error in getting tenantId of " + tenantDomain, e);
        }

        for (Object object : applicationAttributesFromConfig) {
            JSONObject attribute = (JSONObject) object;
            attributeKeys.add(attribute.get(APIConstants.ApplicationAttributes.ATTRIBUTE));
        }

        for (Object key : applicationAttributes.keySet()) {
            if (!attributeKeys.contains(key)) {
                apiMgtDAO.deleteApplicationAttributes((String) key, applicationId);
                if (log.isDebugEnabled()) {
                    log.debug("Removing " + key + "from application - " + application.getName());
                }
            }
        }

        for (Object key : attributeKeys) {
            if (!applicationAttributes.keySet().contains(key)) {
                newApplicationAttributes.put((String) key, "");
            }
        }
        apiMgtDAO.addApplicationAttributes(newApplicationAttributes, applicationId, tenantId);
    }

    @Override
    public String getOpenAPIDefinition(String apiId, String organization) throws APIManagementException {

        String definition = super.getOpenAPIDefinition(apiId, organization);
        return APIUtil.removeXMediationScriptsFromSwagger(definition);
    }

    @Override
    public String getOpenAPIDefinitionForEnvironment(API api, String environmentName)
            throws APIManagementException {

        return getOpenAPIDefinitionForDeployment(api, environmentName);
    }

    public void revokeAPIKey(String apiKey, long expiryTime, String tenantDomain) throws APIManagementException {

        RevocationRequestPublisher revocationRequestPublisher = RevocationRequestPublisher.getInstance();
        Properties properties = new Properties();
        int tenantId = APIUtil.getTenantIdFromTenantDomain(tenantDomain);
        String eventID = UUID.randomUUID().toString();
        properties.put(APIConstants.NotificationEvent.EVENT_ID, eventID);
        properties.put(APIConstants.NotificationEvent.EVENT_TYPE, APIConstants.API_KEY_AUTH_TYPE);
        properties.put(APIConstants.NotificationEvent.TOKEN_TYPE, APIConstants.API_KEY_AUTH_TYPE);
        properties.put(APIConstants.NotificationEvent.TENANT_ID, tenantId);
        properties.put(APIConstants.NotificationEvent.TENANT_DOMAIN, tenantDomain);
        properties.put(APIConstants.NotificationEvent.STREAM_ID, APIConstants.TOKEN_REVOCATION_STREAM_ID);
        properties.setProperty(APIConstants.NotificationEvent.EXPIRY_TIME, Long.toString(expiryTime));
        ApiMgtDAO.getInstance().addRevokedJWTSignature(eventID,
                apiKey, APIConstants.API_KEY_AUTH_TYPE,
                expiryTime, tenantId);
        revocationRequestPublisher.publishRevocationEvents(apiKey, properties);
    }

    /**
     * Get server URL updated Open API definition for given synapse gateway environment
     *
     * @param environmentName Name of the synapse gateway environment
     * @return Updated Open API definition
     * @throws APIManagementException
     */
    private String getOpenAPIDefinitionForDeployment(API api, String environmentName)
            throws APIManagementException {

        String apiTenantDomain;
        String updatedDefinition = null;
        Map<String, String> hostsWithSchemes;
        String definition;
        if (api.getSwaggerDefinition() != null) {
            definition = api.getSwaggerDefinition();
        } else {
            throw new APIManagementException("Missing API definition in the api " + api.getUuid());
        }
        APIDefinition oasParser = OASParserUtil.getOASParser(definition);
        api.setScopes(oasParser.getScopes(definition));
        api.setUriTemplates(oasParser.getURITemplates(definition));
        apiTenantDomain = MultitenantUtils.getTenantDomain(
                APIUtil.replaceEmailDomainBack(api.getId().getProviderName()));
        hostsWithSchemes = getHostWithSchemeMappingForEnvironment(api, apiTenantDomain, environmentName);
        api.setContext(getBasePath(apiTenantDomain, api.getContext()));
        updatedDefinition = oasParser.getOASDefinitionForStore(api, definition, hostsWithSchemes);
        return updatedDefinition;
    }

    private Map<String, Object> filterMultipleVersionedAPIs(Map<String, Object> searchResults) {

        Object apiObj = searchResults.get("apis");
        ArrayList<Object> apiSet;
        ArrayList<APIProduct> apiProductSet = new ArrayList<>();
        if (apiObj instanceof Set) {
            apiSet = new ArrayList<>(((Set) apiObj));
        } else {
            apiSet = (ArrayList<Object>) apiObj;
        }
        int apiSetLengthWithVersionedApis = apiSet.size(); // Store the length of the APIs list with the versioned APIs
        int totalLength = Integer.parseInt(searchResults.get("length").toString());

        //filter store results if displayMultipleVersions is set to false
        Boolean displayMultipleVersions = APIUtil.isAllowDisplayMultipleVersions();
        if (!displayMultipleVersions) {
            SortedSet<API> resultApis = new TreeSet<API>(new APINameComparator());

            for (Object result : apiSet) {
                if (result instanceof API) {
                    resultApis.add((API) result);
                } else if (result instanceof Map.Entry) {
                    Map.Entry<Documentation, API> entry = (Map.Entry<Documentation, API>) result;
                    resultApis.add(entry.getValue());
                } else if (result instanceof APIProduct) {
                    apiProductSet.add((APIProduct) result);
                }
            }

            Map<String, API> latestPublishedAPIs = new HashMap<String, API>();
            Comparator<API> versionComparator = new APIVersionComparator();
            String key;

            //Run the result api list through API version comparator and filter out multiple versions
            for (API api : resultApis) {
                key = api.getId().getProviderName() + COLON_CHAR + api.getId().getApiName();
                API existingAPI = latestPublishedAPIs.get(key);
                if (existingAPI != null) {
                    // If we have already seen an API with the same name, make sure
                    // this one has a higher version number
                    if (versionComparator.compare(api, existingAPI) > 0) {
                        latestPublishedAPIs.put(key, api);
                    }
                } else {
                    // We haven't seen this API before
                    latestPublishedAPIs.put(key, api);
                }
            }

            //filter apiSet
            ArrayList<Object> tempApiSet = new ArrayList<Object>();
            for (Object result : apiSet) {
                API api = null;
                String mapKey;
                API latestAPI;
                if (result instanceof API) {
                    api = (API) result;
                    mapKey = api.getId().getProviderName() + COLON_CHAR + api.getId().getApiName();
                    if (latestPublishedAPIs.containsKey(mapKey)) {
                        latestAPI = latestPublishedAPIs.get(mapKey);
                        if (latestAPI.getId().equals(api.getId())) {
                            tempApiSet.add(api);
                        }
                    }
                } else if (result instanceof Map.Entry) {
                    Map.Entry<Documentation, API> docEntry = (Map.Entry<Documentation, API>) result;
                    api = docEntry.getValue();
                    mapKey = api.getId().getProviderName() + COLON_CHAR + api.getId().getApiName();
                    if (latestPublishedAPIs.containsKey(mapKey)) {
                        latestAPI = latestPublishedAPIs.get(mapKey);
                        if (latestAPI.getId().equals(api.getId())) {
                            tempApiSet.add(docEntry);
                        }
                    }
                }
            }

            // Store the length of the APIs list without the versioned APIs
            int apiSetLengthWithoutVersionedApis = tempApiSet.size();

            apiSet = tempApiSet;
            ArrayList<Object> resultAPIandProductSet = new ArrayList<>();
            resultAPIandProductSet.addAll(apiSet);
            resultAPIandProductSet.addAll(apiProductSet);
            resultAPIandProductSet.sort(new ContentSearchResultNameComparator());

            if (apiObj instanceof Set) {
                searchResults.put("apis", new LinkedHashSet<>(resultAPIandProductSet));
            } else {
                searchResults.put("apis", resultAPIandProductSet);
            }
            searchResults.put("length",
                    totalLength - (apiSetLengthWithVersionedApis - (apiSetLengthWithoutVersionedApis + apiProductSet
                            .size())));
        }
        return searchResults;
    }

    /**
     * Validate application attributes and remove attributes that does not exist in the config
     *
     * @param applicationAttributes Application attributes provided
     * @param keys                  Application attribute keys in config
     * @return Validated application attributes
     */
    private Map<String, String> validateApplicationAttributes(Map<String, String> applicationAttributes, Set keys) {

        Iterator iterator = applicationAttributes.keySet().iterator();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            if (!keys.contains(key)) {
                iterator.remove();
                applicationAttributes.remove(key);
            }
        }
        return applicationAttributes;
    }

    /**
     * Get host names with transport scheme mapping from Gateway Environments in api-manager.xml or from the tenant
     * custom url config in registry.
     *
     * @param apiTenantDomain Tenant domain
     * @param environmentName Environment name
     * @return Host name to transport scheme mapping
     * @throws APIManagementException if an error occurs when getting host names with schemes
     */
    private Map<String, String> getHostWithSchemeMappingForEnvironment(API api, String apiTenantDomain,
     String environmentName)
            throws APIManagementException {

        Map<String, String> domains = getTenantDomainMappings(apiTenantDomain,
         APIConstants.API_DOMAIN_MAPPINGS_GATEWAY);
        Map<String, String> hostsWithSchemes = new HashMap<>();
        String organization = api.getOrganization();
        if (!domains.isEmpty()) {
            String customUrl = domains.get(APIConstants.CUSTOM_URL);
            if (customUrl.startsWith(APIConstants.HTTP_PROTOCOL_URL_PREFIX)) {
                hostsWithSchemes.put(APIConstants.HTTP_PROTOCOL, customUrl);
            } else {
                hostsWithSchemes.put(APIConstants.HTTPS_PROTOCOL, customUrl);
            }
        } else {
            Map<String, Environment> allEnvironments = APIUtil.getEnvironments(organization);
            Environment environment = allEnvironments.get(environmentName);

            if (environment == null) {
                handleResourceNotFoundException("Could not find provided environment '" + environmentName + "'");
            }

            List<APIRevisionDeployment> deploymentList = getAPIRevisionDeploymentListOfAPI(api.getUuid());
            String host = "";
            for (APIRevisionDeployment deployment : deploymentList) {
                if (!deployment.isDisplayOnDevportal()) {
                    continue;
                }
                if (StringUtils.equals(deployment.getDeployment(), environmentName)) {
                    host = deployment.getVhost();
                }
            }
            if (StringUtils.isEmpty(host)) {
                // returns empty server urls
                hostsWithSchemes.put(APIConstants.HTTP_PROTOCOL, "");
                return hostsWithSchemes;
            }

            VHost vhost = VHostUtils.getVhostFromEnvironment(environment, host);
            if (StringUtils.containsIgnoreCase(api.getTransports(), APIConstants.HTTP_PROTOCOL)
                    && vhost.getHttpPort() != -1) {
                hostsWithSchemes.put(APIConstants.HTTP_PROTOCOL, vhost.getHttpUrl());
            }
            if (StringUtils.containsIgnoreCase(api.getTransports(), APIConstants.HTTPS_PROTOCOL)
                    && vhost.getHttpsPort() != -1) {
                hostsWithSchemes.put(APIConstants.HTTPS_PROTOCOL, vhost.getHttpsUrl());
            }
        }
        return hostsWithSchemes;
    }

    private String getBasePath(String apiTenantDomain, String basePath) throws APIManagementException {

        Map<String, String> domains =
                getTenantDomainMappings(apiTenantDomain, APIConstants.API_DOMAIN_MAPPINGS_GATEWAY);
        if (!domains.isEmpty()) {
            return basePath.replace("/t/" + apiTenantDomain, "");
        }
        return basePath;
    }

    public void publishSearchQuery(String query, String username, String organization) {

        if (recommendationEnvironment != null) {
            RecommenderEventPublisher extractor = new RecommenderDetailsExtractor(query, username, organization);
            Thread recommendationThread = new Thread(extractor);
            recommendationThread.start();
        }
    }

    public void publishClickedAPI(ApiTypeWrapper clickedApi, String username, String organization) {

        if (recommendationEnvironment != null) {
            RecommenderEventPublisher extractor = new RecommenderDetailsExtractor(clickedApi, username, organization);
            Thread recommendationThread = new Thread(extractor);
            recommendationThread.start();
        }
    }

    /**
     * To check whether the API recommendation is enabled. It can be either enabled globally or tenant vice.
     *
     * @param tenantDomain Tenant domain
     * @return whether recommendation is enabled or not
     */

    public boolean isRecommendationEnabled(String tenantDomain) {

        return APIUtil.isRecommendationEnabled(tenantDomain);
    }

    public String getRequestedTenant() {

        return requestedTenant;
    }

    @Override
    public void cleanUpApplicationRegistrationByApplicationIdAndKeyMappingId(int applicationId, String keyMappingId)
            throws APIManagementException {

        APIKey apiKey = apiMgtDAO.getKeyMappingFromApplicationIdAndKeyMappingId(applicationId, keyMappingId);
        if (apiKey != null) {
            apiMgtDAO.deleteApplicationRegistration(applicationId, apiKey.getType(), apiKey.getKeyManager());
            apiMgtDAO.deleteApplicationKeyMappingByMappingId(keyMappingId);
        }
    }

    @Override
    public APIKey getApplicationKeyByAppIDAndKeyMapping(int applicationId, String keyMappingId)
            throws APIManagementException {

        APIKey apiKey = apiMgtDAO.getKeyMappingFromApplicationIdAndKeyMappingId(applicationId, keyMappingId);
        String keyManagerId = apiKey.getKeyManager();
        String consumerKey = apiKey.getConsumerKey();

        KeyManagerConfigurationDTO keyManagerConfigurationDTO =
         apiMgtDAO.getKeyManagerConfigurationByUUID(keyManagerId);
        String keyManagerTenantDomain = keyManagerConfigurationDTO.getOrganization();
        if (keyManagerConfigurationDTO != null) {
            String keyManagerName = keyManagerConfigurationDTO.getName();
            KeyManager keyManager = KeyManagerHolder.getKeyManagerInstance(keyManagerTenantDomain, keyManagerName);
            if (keyManager != null) {
                OAuthApplicationInfo oAuthApplicationInfo = keyManager.retrieveApplication(consumerKey);
                if (oAuthApplicationInfo != null) {
                    apiKey.setConsumerSecret(oAuthApplicationInfo.getClientSecret());
                    apiKey.setGrantTypes((String) oAuthApplicationInfo.getParameter(APIConstants.JSON_GRANT_TYPES));
                    apiKey.setCallbackUrl(oAuthApplicationInfo.getCallBackURL());
                    apiKey.setAdditionalProperties(
                            oAuthApplicationInfo.getParameter(APIConstants.JSON_ADDITIONAL_PROPERTIES));
                }
            }
        }

        return apiKey;
    }

    @Override
    public Set<Topic> getTopics(String apiId) throws APIManagementException {

        return apiMgtDAO.getAPITopics(apiId);
    }

    @Override
    public Set<Subscription> getTopicSubscriptions(String applicationUUID, String apiUUID) throws APIManagementException {

        if (StringUtils.isNotEmpty(apiUUID)) {
            return apiMgtDAO.getTopicSubscriptionsByApiUUID(applicationUUID, apiUUID);
        } else {
            return apiMgtDAO.getTopicSubscriptions(applicationUUID);
        }
    }

    /**
     * Get recommendations for the user from the recommendation cache.
     *
     * @param userName     User's Name
     * @param tenantDomain tenantDomain
     * @return List of APIs recommended for the user
     */
    public String getApiRecommendations(String userName, String tenantDomain) {

        if (tenantDomain != null && userName != null) {
            Cache recommendationsCache = CacheProvider.getRecommendationsCache();
            String cacheName = userName + "_" + tenantDomain;
            if (recommendationsCache.containsKey(cacheName)) {
                org.json.JSONObject cachedObject = (org.json.JSONObject) recommendationsCache.get(cacheName);
                if (cachedObject != null) {
                    return (String) cachedObject.get(APIConstants.RECOMMENDATIONS_CACHE_KEY);
                }
            }
        }
        return null;
    }

    /**
     * Change user's password
     *
     * @param currentPassword Current password of the user
     * @param newPassword     New password of the user
     */
    @Override
    public void changeUserPassword(String currentPassword, String newPassword) throws APIManagementException {
        //check whether EnablePasswordChange configuration is set to 'true'
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
        boolean enableChangePassword =
                Boolean.parseBoolean(config.getFirstProperty(APIConstants.ENABLE_CHANGE_PASSWORD));
        if (!enableChangePassword) {
            throw new APIManagementException("Password change operation is disabled in the system",
                    ExceptionCodes.PASSWORD_CHANGE_DISABLED);
        }

        UserAdmin userAdmin = new UserAdmin();
        try {
            userAdmin.changePasswordByUser(userNameWithoutChange, currentPassword, newPassword);
        } catch (UserAdminException e) {
            String genericErrorMessage = "Error occurred while changing the user password";
            if (log.isDebugEnabled()) {
                log.debug(genericErrorMessage, e);
            }
            //filter the exception message
            String exceptionMessage = e.getMessage();
            if (exceptionMessage.matches("(?i:.*\\b(current)\\b.*\\b(password)\\b.*\\b(incorrect)\\b.*)")) {
                String errorMessage = "The current user password entered is incorrect";
                throw new APIManagementException(errorMessage,
                        ExceptionCodes.CURRENT_PASSWORD_INCORRECT);
            } else if ((exceptionMessage.matches("(?i:.*\\b(password)\\b.*\\b(length)\\b.*)")) ||
                    (ExceptionUtils.getStackTrace(e).contains("PolicyViolationException"))
            ) {
                String errorMessage = "The new password entered is invalid since it doesn't comply with the password " +
                        "pattern/policy configured";
                throw new APIManagementException(errorMessage, ExceptionCodes.PASSWORD_PATTERN_INVALID);
            } else {
                throw new APIManagementException(genericErrorMessage);
            }
        }
    }

    @Override
    public Map<String, Object> searchPaginatedAPIs(String searchQuery, String organization, int start, int end,
                                                   String sortBy, String sortOrder) throws APIManagementException {

        Map<String, Object> result = new HashMap<String, Object>();
        if (log.isDebugEnabled()) {
            log.debug("Original search query received : " + searchQuery);
        }
        Organization org = new Organization(organization);
        String userName = (userNameWithoutChange != null) ? userNameWithoutChange : username;
        String[] roles = APIUtil.getListOfRoles(userName);
        Map<String, Object> properties = APIUtil.getUserProperties(userName);
        UserContext userCtx = new UserContext(userNameWithoutChange, org, properties, roles);
        try {
            DevPortalAPISearchResult searchAPIs = apiPersistenceInstance.searchAPIsForDevPortal(org, searchQuery,
                    start, end, userCtx);
            if (log.isDebugEnabled()) {
                log.debug("searched Devportal APIs for query : " + searchQuery + " :-->: " + searchAPIs.toString());
            }
            SortedSet<Object> apiSet = new TreeSet<>(new APIAPIProductNameComparator());
            if (searchAPIs != null) {
                List<DevPortalAPIInfo> list = searchAPIs.getDevPortalAPIInfoList();
                List<Object> apiList = new ArrayList<>();
                for (DevPortalAPIInfo devPortalAPIInfo : list) {
                    API mappedAPI = APIMapper.INSTANCE.toApi(devPortalAPIInfo);
                    try {
                        mappedAPI.setRating(APIUtil.getAverageRating(mappedAPI.getUuid()));
                        Set<String> tierNameSet = devPortalAPIInfo.getAvailableTierNames();
                        String tiers = null;
                        if (tierNameSet != null) {
                            tiers = String.join("||", tierNameSet);
                        }
                        Map<String, Tier> definedTiers = APIUtil.getTiers(tenantId);
                        Set<Tier> availableTiers =
                                APIUtil.getAvailableTiers(definedTiers, tiers, mappedAPI.getId().getApiName());
                        mappedAPI.removeAllTiers();
                        mappedAPI.setAvailableTiers(availableTiers);
                        apiList.add(mappedAPI);
                    } catch (APIManagementException e) {
                        log.warn("Retrieving API details from DB failed for API: " + mappedAPI.getUuid() + " " + e);
                    }
                }
                apiSet.addAll(apiList);
                result.put("apis", apiSet);
                result.put("length", searchAPIs.getTotalAPIsCount());
                result.put("isMore", true);
            } else {
                result.put("apis", apiSet);
                result.put("length", 0);
                result.put("isMore", false);
            }

        } catch (APIPersistenceException e) {
            throw new APIManagementException("Error while searching the api ", e);
        }
        return result;
    }

    @Override
    public ApiTypeWrapper getAPIorAPIProductByUUID(String uuid, String organization) throws APIManagementException {

        try {
            Organization org = new Organization(organization);
            DevPortalAPI devPortalApi = apiPersistenceInstance.getDevPortalAPI(org,
                    uuid);
            if (devPortalApi != null) {
                checkVisibilityPermission(userNameWithoutChange, devPortalApi.getVisibility(),
                        devPortalApi.getVisibleRoles());
                if (APIConstants.API_PRODUCT.equalsIgnoreCase(devPortalApi.getType())) {
                    APIProduct apiProduct = APIMapper.INSTANCE.toApiProduct(devPortalApi);
                    apiProduct.setID(new APIProductIdentifier(devPortalApi.getProviderName(),
                            devPortalApi.getApiName(), devPortalApi.getVersion()));
                    populateAPIProductInformation(uuid, organization, apiProduct);
                    populateDefaultVersion(apiProduct);
                    populateAPIStatus(apiProduct);
                    apiProduct = addTiersToAPI(apiProduct, organization);
                    return new ApiTypeWrapper(apiProduct);
                } else {
                    API api = APIMapper.INSTANCE.toApi(devPortalApi);
                    populateDevPortalAPIInformation(uuid, organization, api);
                    populateDefaultVersion(api);
                    populateAPIStatus(api);
                    api = addTiersToAPI(api, organization);
                    return new ApiTypeWrapper(api);
                }
            } else {
                String msg = "Failed to get API. API artifact corresponding to artifactId " + uuid + " does not exist";
                throw new APIMgtResourceNotFoundException(msg);
            }
        } catch (APIPersistenceException | OASPersistenceException | ParseException e) {
            String msg = "Failed to get API";
            throw new APIManagementException(msg, e);
        }
    }

    private void populateAPIStatus(API api) throws APIManagementException {

        api.setStatus(apiMgtDAO.getAPIStatusFromAPIUUID(api.getUuid()));
    }

    private void populateAPIStatus(APIProduct apiProduct) throws APIManagementException {

        apiProduct.setState(apiMgtDAO.getAPIStatusFromAPIUUID(apiProduct.getUuid()));
    }

    protected void checkVisibilityPermission(String userNameWithTenantDomain, String visibility, String visibilityRoles)
            throws APIManagementException {

        if (visibility == null || visibility.trim().isEmpty()
                || visibility.equalsIgnoreCase(APIConstants.API_GLOBAL_VISIBILITY)) {
            if (log.isDebugEnabled()) {
                log.debug("API does not have any visibility restriction");
            }
            return;
        }
        if (APIUtil.hasPermission(userNameWithTenantDomain, APIConstants.Permissions.APIM_ADMIN)
                || APIUtil.hasPermission(userNameWithTenantDomain, APIConstants.Permissions.API_CREATE)
                || APIUtil.hasPermission(userNameWithTenantDomain, APIConstants.Permissions.API_PUBLISH)) {
            return;
        }

        if (visibilityRoles != null && !visibilityRoles.trim().isEmpty()) {
            String[] visibilityRolesList = visibilityRoles.replaceAll("\\s+", "").split(",");
            if (log.isDebugEnabled()) {
                log.debug("API has restricted visibility with the roles : "
                        + Arrays.toString(visibilityRolesList));
            }
            String[] userRoleList = APIUtil.getListOfRoles(userNameWithTenantDomain);
            if (log.isDebugEnabled()) {
                log.debug("User " + username + " has roles " + Arrays.toString(userRoleList));
            }
            for (String role : visibilityRolesList) {
                if (!role.equalsIgnoreCase(APIConstants.NULL_USER_ROLE_LIST)
                        && APIUtil.compareRoleList(userRoleList, role)) {
                    return;
                }
            }
            throw new APIMgtResourceNotFoundException("API not found "); // for backword compatibility we send 404
        }

    }

    private API addTiersToAPI(API api, String organization) throws APIManagementException {

        int tenantId = APIUtil.getInternalIdFromTenantDomainOrOrganization(organization);
        Set<Tier> tierNames = api.getAvailableTiers();
        Map<String, Tier> definedTiers = APIUtil.getTiers(tenantId);

        Set<Tier> availableTiers = new HashSet<Tier>();
        Set<String> deniedTiers = getDeniedTiers(tenantId);

        for (Tier tierName : tierNames) {
            Tier definedTier = definedTiers.get(tierName.getName());
            if (definedTier != null && (!deniedTiers.contains(definedTier.getName()))) {
                availableTiers.add(definedTier);
            } else {
                log.warn("Unknown tier: " + tierName + " found on API: ");
            }
        }
        availableTiers.removeIf(tier -> deniedTiers.contains(tier.getName()));
        api.removeAllTiers();
        api.addAvailableTiers(availableTiers);
        return api;
    }

    private APIProduct addTiersToAPI(APIProduct apiProduct, String organization) throws APIManagementException {

        int tenantId = APIUtil.getInternalIdFromTenantDomainOrOrganization(organization);
        Set<Tier> tierNames = apiProduct.getAvailableTiers();
        Map<String, Tier> definedTiers = APIUtil.getTiers(tenantId);

        Set<Tier> availableTiers = new HashSet<>();
        Set<String> deniedTiers = getDeniedTiers(tenantId);

        for (Tier tierName : tierNames) {
            Tier definedTier = definedTiers.get(tierName.getName());
            if (definedTier != null) {
                availableTiers.add(definedTier);
            }
        }
        availableTiers.removeIf(tier -> deniedTiers.contains(tier.getName()));
        apiProduct.removeAllTiers();
        apiProduct.setAvailableTiers(availableTiers);
        return apiProduct;
    }

    /**
     * Get minimal details of API by registry artifact id
     *
     * @param uuid         Registry artifact id
     * @param organization identifier of the organization
     * @return API of the provided artifact id
     * @throws APIManagementException
     */
    @Override
    public API getLightweightAPIByUUID(String uuid, String organization) throws APIManagementException {

        try {
            Organization org = new Organization(organization);
            DevPortalAPI devPortalApi = apiPersistenceInstance.getDevPortalAPI(org, uuid);
            if (devPortalApi != null) {
                checkVisibilityPermission(userNameWithoutChange, devPortalApi.getVisibility(),
                        devPortalApi.getVisibleRoles());
                API api = APIMapper.INSTANCE.toApi(devPortalApi);

                /// populate relavant external info
                // environment
                String environmentString = null;
                if (api.getEnvironments() != null) {
                    environmentString = String.join(",", api.getEnvironments());
                }
                api.setEnvironments(APIUtil.extractEnvironmentsForAPI(environmentString, organization));
                //CORS . if null is returned, set default config from the configuration
                if (api.getCorsConfiguration() == null) {
                    api.setCorsConfiguration(APIUtil.getDefaultCorsConfiguration());
                }
                String tiers = null;
                Set<Tier> apiTiers = api.getAvailableTiers();
                Set<String> tierNameSet = new HashSet<String>();
                for (Tier t : apiTiers) {
                    tierNameSet.add(t.getName());
                }
                if (api.getAvailableTiers() != null) {
                    tiers = String.join("||", tierNameSet);
                }
                Map<String, Tier> definedTiers = APIUtil.getTiers(tenantId);
                Set<Tier> availableTiers = APIUtil.getAvailableTiers(definedTiers, tiers, api.getId().getApiName());
                api.removeAllTiers();
                api.setAvailableTiers(availableTiers);
                api.setOrganization(organization);
                return api;
            } else {
                String msg = "Failed to get API. API artifact corresponding to artifactId " + uuid + " does not exist";
                throw new APIMgtResourceNotFoundException(msg);
            }
        } catch (APIPersistenceException e) {
            String msg = "Failed to get API with uuid " + uuid;
            throw new APIManagementException(msg, e);
        }
    }

    /**
     * Used to retrieve API/API Products without performing the visibility permission checks
     *
     * @param uuid
     * @param organization
     * @return
     * @throws APIManagementException
     */
    private ApiTypeWrapper getAPIorAPIProductByUUIDWithoutPermissionCheck(String uuid, String organization)
            throws APIManagementException {

        try {
            Organization org = new Organization(organization);
            DevPortalAPI devPortalApi = apiPersistenceInstance.getDevPortalAPI(org, uuid);
            if (devPortalApi != null) {
                if (APIConstants.API_PRODUCT.equalsIgnoreCase(devPortalApi.getType())) {
                    APIProduct apiProduct = APIMapper.INSTANCE.toApiProduct(devPortalApi);
                    apiProduct.setID(new APIProductIdentifier(devPortalApi.getProviderName(), devPortalApi.getApiName(),
                            devPortalApi.getVersion()));
                    populateAPIProductInformation(uuid, organization, apiProduct);
                    populateDefaultVersion(apiProduct);
                    return new ApiTypeWrapper(apiProduct);
                } else {
                    API api = APIMapper.INSTANCE.toApi(devPortalApi);
                    populateDevPortalAPIInformation(uuid, organization, api);
                    populateDefaultVersion(api);
                    api = addTiersToAPI(api, organization);
                    return new ApiTypeWrapper(api);
                }
            } else {
                String msg = "Failed to get API. API artifact corresponding to artifactId " + uuid + " does not exist";
                throw new APIMgtResourceNotFoundException(msg);
            }
        } catch (APIPersistenceException | OASPersistenceException | ParseException e) {
            String msg = "Failed to get API";
            throw new APIManagementException(msg, e);
        }
    }

    /**
     * Get minimal details of API by API identifier
     *
     * @param identifier APIIdentifier object
     * @return API of the provided APIIdentifier
     * @throws APIManagementException
     */

    public API getLightweightAPI(APIIdentifier identifier, String orgId) throws APIManagementException {

        String uuid = null;
        try {
            Organization org = new Organization(orgId);
            if (identifier.getUUID() != null) {
                uuid = identifier.getUUID();
            } else {
                uuid = apiMgtDAO.getUUIDFromIdentifier(identifier.getProviderName(), identifier.getApiName(),
                        identifier.getVersion(), orgId);
            }
            DevPortalAPI devPortalApi = apiPersistenceInstance.getDevPortalAPI(org, uuid);
            if (devPortalApi != null) {
                API api = APIMapper.INSTANCE.toApi(devPortalApi);
                api.setOrganization(orgId);
                String tiers = null;
                Set<Tier> apiTiers = api.getAvailableTiers();
                Set<String> tierNameSet = new HashSet<String>();
                for (Tier t : apiTiers) {
                    tierNameSet.add(t.getName());
                }
                if (api.getAvailableTiers() != null) {
                    tiers = String.join("||", tierNameSet);
                }
                Map<String, Tier> definedTiers = APIUtil.getTiers(tenantId);
                Set<Tier> availableTiers = APIUtil.getAvailableTiers(definedTiers, tiers, api.getId().getApiName());
                api.removeAllTiers();
                api.setAvailableTiers(availableTiers);
                return api;
            } else {
                String msg = "Failed to get API. API artifact corresponding to artifactId " + uuid + " does not exist";
                throw new APIMgtResourceNotFoundException(msg);
            }
        } catch (APIPersistenceException e) {
            String msg = "Failed to get API with uuid " + uuid;
            throw new APIManagementException(msg, e);
        }
    }

    @Override
    public Map<String, Object> searchPaginatedContent(String searchQuery, String organization, int start, int end)
            throws APIManagementException {

        ArrayList<Object> compoundResult = new ArrayList<Object>();
        Map<Documentation, API> docMap = new HashMap<Documentation, API>();
        Map<String, Object> result = new HashMap<String, Object>();
        SortedSet<API> apiSet = new TreeSet<API>(new APINameComparator());
        int totalLength = 0;

        String userame = (userNameWithoutChange != null) ? userNameWithoutChange : username;
        Organization org = new Organization(organization);
        Map<String, Object> properties = APIUtil.getUserProperties(userame);
        String[] roles = APIUtil.getFilteredUserRoles(userame);
        ;
        UserContext ctx = new UserContext(userame, org, properties, roles);

        try {
            DevPortalContentSearchResult sResults = apiPersistenceInstance.searchContentForDevPortal(org, searchQuery,
                    start, end, ctx);
            if (sResults != null) {
                List<SearchContent> resultList = sResults.getResults();
                for (SearchContent item : resultList) {
                    if (item instanceof DocumentSearchContent) {
                        // doc item
                        DocumentSearchContent docItem = (DocumentSearchContent) item;
                        Documentation doc = new Documentation(
                                DocumentationType.valueOf(docItem.getDocType().toString()), docItem.getName());
                        doc.setSourceType(DocumentSourceType.valueOf(docItem.getSourceType().toString()));
                        doc.setVisibility(DocumentVisibility.valueOf(docItem.getVisibility().toString()));
                        doc.setId(docItem.getId());
                        API api = new API(new APIIdentifier(docItem.getApiProvider(), docItem.getApiName(),
                                docItem.getApiVersion()));
                        api.setUuid(docItem.getApiUUID());
                        docMap.put(doc, api);
                    } else {
                        DevPortalSearchContent publiserAPI = (DevPortalSearchContent) item;
                        API api = new API(new APIIdentifier(publiserAPI.getProvider(), publiserAPI.getName(),
                                publiserAPI.getVersion()));
                        api.setUuid(publiserAPI.getId());
                        api.setContext(publiserAPI.getContext());
                        api.setContextTemplate(publiserAPI.getContext());
                        api.setStatus(publiserAPI.getStatus());
                        api.setBusinessOwner(publiserAPI.getBusinessOwner());
                        api.setBusinessOwnerEmail(publiserAPI.getBusinessOwnerEmail());
                        api.setTechnicalOwner(publiserAPI.getTechnicalOwner());
                        api.setTechnicalOwnerEmail(publiserAPI.getTechnicalOwnerEmail());
                        api.setMonetizationEnabled(publiserAPI.getMonetizationStatus());
                        api.setAdvertiseOnly(publiserAPI.getAdvertiseOnly());
                        api.setRating(0);// need to retrieve from db
                        apiSet.add(api);
                    }
                }
                compoundResult.addAll(apiSet);
                compoundResult.addAll(docMap.entrySet());
                compoundResult.sort(new ContentSearchResultNameComparator());
                result.put("length", sResults.getTotalCount());
            } else {
                result.put("length", compoundResult.size());
            }
        } catch (APIPersistenceException e) {
            throw new APIManagementException("Error while searching content ", e);
        }
        result.put("apis", compoundResult);
        return result;
    }

    protected void checkAPIVisibilityRestriction(String apiId, String organization) throws APIManagementException {

        try {
            DevPortalAPI api = apiPersistenceInstance.getDevPortalAPI(new Organization(organization), apiId);
            if (api != null) {
                checkVisibilityPermission(userNameWithoutChange, api.getVisibility(), api.getVisibleRoles());
            }
        } catch (APIPersistenceException e) {
            throw new APIManagementException("Error while accessing dev portal API", e);
        }
    }

    @Override
    public void checkAPIVisibility(String uuid, String organization) throws APIManagementException {

        checkAPIVisibilityRestriction(uuid, organization);
    }

    @Override
    public List<Documentation> getAllDocumentation(String uuid, String organization) throws APIManagementException {

        checkAPIVisibilityRestriction(uuid, organization);
        return super.getAllDocumentation(uuid, organization);
    }

    @Override
    public Documentation getDocumentation(String apiId, String docId, String organization)
            throws APIManagementException {

        checkAPIVisibilityRestriction(apiId, organization);
        return super.getDocumentation(apiId, docId, organization);
    }

    @Override
    public DocumentationContent getDocumentationContent(String apiId, String docId, String organization)
            throws APIManagementException {

        checkAPIVisibilityRestriction(apiId, organization);
        return super.getDocumentationContent(apiId, docId, organization);
    }

    @Override
    public String getAsyncAPIDefinitionForLabel(Identifier apiId, String labelName) throws APIManagementException {

        List<Label> gatewayLabels;
        String updatedDefinition = null;
        Map<String, String> hostsWithSchemes;
        // TODO:
//        String definition = super.getOpenAPIDefinition(apiId);
//        AsyncApiParser asyncApiParser = new AsyncApiParser();
//        if (apiId instanceof APIIdentifier) {
//            API api = getLightweightAPI((APIIdentifier) apiId);
//            gatewayLabels = api.getGatewayLabels();
//            hostsWithSchemes = getHostWithSchemeMappingForLabelWS(gatewayLabels, labelName);
//            updatedDefinition = asyncApiParser.getAsyncApiDefinitionForStore(api, definition, hostsWithSchemes);
//        }
//        return updatedDefinition;
        return "";
    }

    @Override
    public List<APIRevisionDeployment> getAPIRevisionDeploymentListOfAPI(String apiUUID) throws APIManagementException {

        return apiMgtDAO.getAPIRevisionDeploymentByApiUUID(apiUUID);
    }

    @Override
    public Set<SubscribedAPI> getPaginatedSubscribedAPIsByApplication(Application application, Integer offset,
                                                                      Integer limit, String organization)
            throws APIManagementException {

        Set<SubscribedAPI> subscribedAPIs = null;
        try {
            subscribedAPIs = apiMgtDAO.getPaginatedSubscribedAPIsByApplication(application, offset,
                    limit, organization);
            if (subscribedAPIs != null && !subscribedAPIs.isEmpty()) {
                Map<String, Tier> tiers = APIUtil.getTiers(tenantId);
                for (SubscribedAPI subscribedApi : subscribedAPIs) {
                    Tier tier = tiers.get(subscribedApi.getTier().getName());
                    subscribedApi.getTier().setDisplayName(tier != null ? tier.getDisplayName() : subscribedApi
                            .getTier().getName());
                }
            }
        } catch (APIManagementException e) {
            handleException("Failed to get subscribed APIs of application " + application.getUUID(), e);
        }
        return subscribedAPIs;
    }

    @Override
    public List<Tier> getThrottlePolicies(int tierLevel, String organization) throws APIManagementException {

        List<Tier> tierList = new ArrayList<>();
        Map<String, Tier> tiers = null;
        if (tierLevel == APIConstants.TIER_API_TYPE) {
            tiers = APIUtil.getTiers(tierLevel, organization);
            Set<TierPermission> tierPermissions = getTierPermissions();
            for (TierPermission tierPermission : tierPermissions) {
                Tier tier = tiers.get(tierPermission.getTierName());
                if (tier != null) {
                    tier.setTierPermission(tierPermission);
                    tiers.put(tierPermission.getTierName(), tier);
                }
            }

            // Removing denied Tiers
            Set<String> deniedTiers = getDeniedTiers();
            for (String tierName : deniedTiers) {
                tiers.remove(tierName);
            }
        } else if (tierLevel == APIConstants.TIER_APPLICATION_TYPE) {
            if (APIUtil.isOnPremResolver()) {
                organization = tenantDomain;
            }
            tiers = APIUtil.getTiers(APIConstants.TIER_APPLICATION_TYPE, organization);
        }
        if (tiers != null) {
            tierList.addAll(tiers.values());
        }
        return tierList;
    }

    @Override
    public Tier getThrottlePolicyByName(String policyId, int policyType, String organization)
            throws APIManagementException {

        Tier tier = null;
        if (policyType == APIConstants.TIER_API_TYPE) {
            tier = APIUtil.getPolicyByName(PolicyConstants.POLICY_LEVEL_SUB, policyId, organization);
        } else if (policyType == APIConstants.TIER_APPLICATION_TYPE) {
            if (APIUtil.isOnPremResolver()) {
                organization = tenantDomain;
            }
            tier = APIUtil.getPolicyByName(PolicyConstants.POLICY_LEVEL_APP, policyId, organization);
        }
        return tier;
    }

    /**
     * Get host names with transport scheme mapping from Gateway Environments in api-manager.xml or from the tenant
     * custom url config in registry. (For WebSockets)
     *
     * @param apiTenantDomain Tenant domain
     * @param environmentName Environment name
     * @return Host name to transport scheme mapping
     * @throws APIManagementException if an error occurs when getting host names with schemes
     */
    private Map<String, String> getHostWithSchemeMappingForEnvironmentWS(String apiTenantDomain, String environmentName,
                                                                         String organization)
            throws APIManagementException {

        Map<String, String> domains = getTenantDomainMappings(apiTenantDomain,
         APIConstants.API_DOMAIN_MAPPINGS_GATEWAY);
        Map<String, String> hostsWithSchemes = new HashMap<>();
        if (!domains.isEmpty()) {
            String customUrl = domains.get(APIConstants.CUSTOM_URL);
            if (customUrl.startsWith(APIConstants.WS_PROTOCOL_URL_PREFIX)) {
                hostsWithSchemes.put(APIConstants.WS_PROTOCOL, customUrl);
            } else {
                hostsWithSchemes.put(APIConstants.WSS_PROTOCOL, customUrl);
            }
        } else {
            Map<String, Environment> allEnvironments = APIUtil.getEnvironments(organization);
            Environment environment = allEnvironments.get(environmentName);

            if (environment == null) {
                handleResourceNotFoundException("Could not find provided environment '" + environmentName + "'");
            }

            assert environment != null;
            String[] hostsWithScheme = environment.getWebsocketGatewayEndpoint().split(",");
            for (String url : hostsWithScheme) {
                if (url.startsWith(APIConstants.WSS_PROTOCOL_URL_PREFIX)) {
                    hostsWithSchemes.put(APIConstants.WSS_PROTOCOL, url);
                }
                if (url.startsWith(APIConstants.WS_PROTOCOL_URL_PREFIX)) {
                    hostsWithSchemes.put(APIConstants.WS_PROTOCOL, url);
                }
            }
        }
        return hostsWithSchemes;
    }

    /**
     * Get gateway host names with transport scheme mapping.
     *
     * @param gatewayLabels gateway label list
     * @param labelName     Label name
     * @return Hostname with transport schemes
     * @throws APIManagementException If an error occurs when getting gateway host names.
     */
    private Map<String, String> getHostWithSchemeMappingForLabelWS(List<Label> gatewayLabels, String labelName)
            throws APIManagementException {

        Map<String, String> hostsWithSchemes = new HashMap<>();
        Label labelObj = null;
        for (Label label : gatewayLabels) {
            if (label.getName().equals(labelName)) {
                labelObj = label;
                break;
            }
        }
        if (labelObj == null) {
            handleException(
                    "Could not find provided label '" + labelName + "'");
            return null;
        }

        List<String> accessUrls = labelObj.getAccessUrls();
        for (String url : accessUrls) {
            if (url.startsWith(APIConstants.WSS_PROTOCOL_URL_PREFIX)) {
                hostsWithSchemes.put(APIConstants.WSS_PROTOCOL, url);
            }
            if (url.startsWith(APIConstants.WS_PROTOCOL_URL_PREFIX)) {
                hostsWithSchemes.put(APIConstants.WS_PROTOCOL, url);
            }
        }
        return hostsWithSchemes;
    }

    /**
     * Check if the specified subscription is allowed for the logged in user
     *
     * @param apiTypeWrapper Api Type wrapper that contains either an API or API Product
     * @throws APIManagementException if the subscription allow check was failed. If the user is not allowed to add the
     *                                subscription, this will throw an instance of APIMgtAuthorizationFailedException
      *                                with the reason as the message
     */
    private void checkSubscriptionAllowed(ApiTypeWrapper apiTypeWrapper)
            throws APIManagementException {

        Set<Tier> tiers;
        String subscriptionAvailability;
        String subscriptionAllowedTenants;

        if (apiTypeWrapper.isAPIProduct()) {
            APIProduct product = apiTypeWrapper.getApiProduct();
            tiers = product.getAvailableTiers();
            subscriptionAvailability = product.getSubscriptionAvailability();
            subscriptionAllowedTenants = product.getSubscriptionAvailableTenants();
        } else {
            API api = apiTypeWrapper.getApi();
            String apiSecurity = api.getApiSecurity();
            if (apiSecurity != null && !apiSecurity.contains(APIConstants.DEFAULT_API_SECURITY_OAUTH2) &&
                    !apiSecurity.contains(APIConstants.API_SECURITY_API_KEY)) {
                String msg = "Subscription is not allowed for API " + apiTypeWrapper.toString() + ". To access the " +
"API, "
                        + "please use the client certificate";
                throw new APIMgtAuthorizationFailedException(msg);
            }
            tiers = api.getAvailableTiers();
            subscriptionAvailability = api.getSubscriptionAvailability();
            subscriptionAllowedTenants = api.getSubscriptionAvailableTenants();
        }

        String apiOrganization = apiTypeWrapper.getOrganization();

        //Tenant based validation for subscription
        boolean subscriptionAllowed = false;
        if (!organization.equals(apiOrganization)) {
            if (APIConstants.SUBSCRIPTION_TO_ALL_TENANTS.equals(subscriptionAvailability)) {
                subscriptionAllowed = true;
            } else if (APIConstants.SUBSCRIPTION_TO_SPECIFIC_TENANTS.equals(subscriptionAvailability)) {
                if (subscriptionAllowedTenants != null) {
                    String[] allowedTenants = subscriptionAllowedTenants.split(",");
                    for (String tenant : allowedTenants) {
                        if (tenant != null && tenantDomain.equals(tenant.trim())) {
                            subscriptionAllowed = true;
                            break;
                        }
                    }
                }
            }
        } else {
            subscriptionAllowed = true;
        }
        if (!subscriptionAllowed) {
            throw new APIMgtAuthorizationFailedException("Subscription is not allowed for " + userNameWithoutChange);
        }

        //check whether the specified tier is within the allowed tiers for the API
        Iterator<Tier> iterator = tiers.iterator();
        boolean isTierAllowed = false;
        List<String> allowedTierList = new ArrayList<>();
        while (iterator.hasNext()) {
            Tier t = iterator.next();
            if (t.getName() != null && (t.getName()).equals(apiTypeWrapper.getTier())) {
                isTierAllowed = true;
            }
            allowedTierList.add(t.getName());
        }
        if (!isTierAllowed) {
            String msg =
 "Tier " + apiTypeWrapper.getTier() + " is not allowed for API/API Product " + apiTypeWrapper + ". Only "
                    + Arrays.toString(allowedTierList.toArray()) + " Tiers are allowed.";
            throw new APIManagementException(msg, ExceptionCodes.from(ExceptionCodes.SUBSCRIPTION_TIER_NOT_ALLOWED,
                    apiTypeWrapper.getTier(), username));
        }
    }

    /**
     * This method is used to retrieve key manager configurations for tenant
     *
     * @param organization organization of the key manager
     * @param username     username of the logged-in user
     * @return KeyManagerConfigurationDTO list
     * @throws APIManagementException if error occurred
     */
    @Override
    public List<KeyManagerConfigurationDTO> getKeyManagerConfigurationsByOrganization(
            String organization, String username) throws APIManagementException {

        APIAdmin apiAdmin = new APIAdminImpl();
        List<KeyManagerConfigurationDTO> keyManagerConfigurations =
                apiAdmin.getKeyManagerConfigurationsByOrganization(organization);
        List<KeyManagerConfigurationDTO> permittedKeyManagerConfigurations = new ArrayList<>();
        if (keyManagerConfigurations.size() > 0) {
            for (KeyManagerConfigurationDTO keyManagerConfiguration : keyManagerConfigurations) {
                if (isKeyManagerAllowedForUser(keyManagerConfiguration.getUuid(), username)) {
                    permittedKeyManagerConfigurations.add(keyManagerConfiguration);
                }
            }
        }
        return permittedKeyManagerConfigurations;
    }

    /**
     * This method is used to check if key manager configuration is allowed for user
     *
     * @param keyManagerId uuid of the key manager
     * @param username     username of the logged in user
     * @return boolean returns if the key manager is allowed for the logged in user
     * @throws APIManagementException if error occurred
     */
    @Override
    public boolean isKeyManagerAllowedForUser(String keyManagerId, String username) throws APIManagementException {

        APIAdmin apiAdmin = new APIAdminImpl();
        KeyManagerPermissionConfigurationDTO permissions = apiAdmin.getKeyManagerPermissions(keyManagerId);
        String permissionType = permissions.getPermissionType();
        if (permissions != null && !permissionType.equals(PERMISSION_NOT_RESTRICTED)) {
            String[] permissionRoles = permissions.getRoles()
                    .stream()
                    .toArray(String[]::new);
            String[] userRoles = APIUtil.getListOfRoles(username);
            boolean roleIsRestricted = hasIntersection(userRoles, permissionRoles);
            if ((PERMISSION_ALLOW.equals(permissionType) && !roleIsRestricted)
                    || (PERMISSION_DENY.equals(permissionType) && roleIsRestricted)) {
                return false;
            }
        }
        return true;
    }

    /**
     * This method is used to check if key manager configuration is allowed for user
     *
     * @param keyManagerName name of the key manager
     * @param organization   organization of the logged in user
     * @param username       username of the logged in user
     * @return boolean returns if the key manager is allowed for the logged in user
     * @throws APIManagementException if error occurred
     */
    @Override
    public boolean isKeyManagerByNameAllowedForUser(String keyManagerName, String organization, String username)
            throws APIManagementException {

        APIAdmin apiAdmin = new APIAdminImpl();
        KeyManagerConfigurationDTO keyManagerConfiguration = apiAdmin
                .getKeyManagerConfigurationByName(organization, keyManagerName);
        KeyManagerPermissionConfigurationDTO permissions = keyManagerConfiguration.getPermissions();
        String permissionType = permissions.getPermissionType();
        //Checks if the keymanager is permission restricted and if the user is in the restricted list
        if (permissions != null && !permissionType.equals(PERMISSION_NOT_RESTRICTED)) {
            String[] permissionRoles = permissions.getRoles()
                    .stream()
                    .toArray(String[]::new);
            String[] userRoles = APIUtil.getListOfRoles(username);
            //list of common roles the user has and the restricted list
            boolean roleIsRestricted = hasIntersection(userRoles, permissionRoles);
            //Checks if the user is allowed to access the key manager
            if ((PERMISSION_ALLOW.equals(permissionType) && !roleIsRestricted)
                    || (PERMISSION_DENY.equals(permissionType) && roleIsRestricted)) {
                return false;
            }
        }
        return true;
    }

    public static boolean hasIntersection(String[] arr1, String[] arr2) {

        Set<String> set = new HashSet<>();

        for (String element : arr1) {
            set.add(element);
        }

        for (String element : arr2) {
            if (set.contains(element)) {
                return true;
            }
        }

        return false;
    }
}

/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl.dao;


import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.BlockConditionAlreadyExistsException;
import org.wso2.carbon.apimgt.api.SubscriptionAlreadyExistingException;
import org.wso2.carbon.apimgt.api.dto.ConditionDTO;
import org.wso2.carbon.apimgt.api.dto.ConditionGroupDTO;
import org.wso2.carbon.apimgt.api.dto.UserApplicationAPIUsage;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIKey;
import org.wso2.carbon.apimgt.api.model.APIStatus;
import org.wso2.carbon.apimgt.api.model.APIStore;
import org.wso2.carbon.apimgt.api.model.AccessTokenInfo;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.ApplicationConstants;
import org.wso2.carbon.apimgt.api.model.BlockConditionsDTO;
import org.wso2.carbon.apimgt.api.model.Comment;
import org.wso2.carbon.apimgt.api.model.KeyManager;
import org.wso2.carbon.apimgt.api.model.LifeCycleEvent;
import org.wso2.carbon.apimgt.api.model.OAuthAppRequest;
import org.wso2.carbon.apimgt.api.model.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.api.model.policy.APIPolicy;
import org.wso2.carbon.apimgt.api.model.policy.ApplicationPolicy;
import org.wso2.carbon.apimgt.api.model.policy.BandwidthLimit;
import org.wso2.carbon.apimgt.api.model.policy.Condition;
import org.wso2.carbon.apimgt.api.model.policy.GlobalPolicy;
import org.wso2.carbon.apimgt.api.model.policy.HeaderCondition;
import org.wso2.carbon.apimgt.api.model.policy.IPCondition;
import org.wso2.carbon.apimgt.api.model.policy.JWTClaimsCondition;
import org.wso2.carbon.apimgt.api.model.policy.Pipeline;
import org.wso2.carbon.apimgt.api.model.policy.Policy;
import org.wso2.carbon.apimgt.api.model.policy.PolicyConstants;
import org.wso2.carbon.apimgt.api.model.policy.QueryParameterCondition;
import org.wso2.carbon.apimgt.api.model.policy.QuotaPolicy;
import org.wso2.carbon.apimgt.api.model.policy.RequestCountLimit;
import org.wso2.carbon.apimgt.api.model.policy.SubscriptionPolicy;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.ThrottlePolicyConstants;
import org.wso2.carbon.apimgt.impl.dao.constants.SQLConstants;
import org.wso2.carbon.apimgt.impl.dto.APIInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.APIKeyInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.ApplicationRegistrationWorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.TierPermissionDTO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.wso2.carbon.apimgt.impl.factory.SQLConstantManagerFactory;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.utils.APIVersionComparator;
import org.wso2.carbon.apimgt.impl.utils.ApplicationUtils;
import org.wso2.carbon.apimgt.impl.utils.LRUCache;
import org.wso2.carbon.apimgt.impl.utils.RemoteUserManagerClient;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowConstants;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowExecutorFactory;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowStatus;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.oauth.IdentityOAuthAdminException;
import org.wso2.carbon.identity.oauth.OAuthUtil;
import org.wso2.carbon.identity.oauth.common.OAuthConstants;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.DBUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import org.wso2.carbon.apimgt.impl.token.JWTGenerator;
//import org.wso2.carbon.apimgt.impl.token.TokenGenerator;

/**
 * This class represent the ApiMgtDAO.
 */
public class ApiMgtDAO {
    private static final Log log = LogFactory.getLog(ApiMgtDAO.class);

    private boolean forceCaseInsensitiveComparisons = false;

    private ApiMgtDAO() {
        APIManagerConfiguration configuration = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfigurationService().getAPIManagerConfiguration();

        String caseSensitiveComparison = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration().getFirstProperty(APIConstants.API_STORE_FORCE_CI_COMPARISIONS);
        if (caseSensitiveComparison != null) {
            forceCaseInsensitiveComparisons = Boolean.parseBoolean(caseSensitiveComparison);
        }
    }

    /**
     * This is an inner class to hold the instance of the ApiMgtDAO.
     * The reason for writing it like this is to guarantee that only one instance would be created.
     * ref: Initialization-on-demand holder idiom
     */
    private static class ApiMgtDAOHolder {
        private static final ApiMgtDAO INSTANCE = new ApiMgtDAO();
    }
    /**
     * Method to get the instance of the ApiMgtDAO.
     *
     * @return {@link ApiMgtDAO} instance
     */
    public static ApiMgtDAO getInstance() {
        return ApiMgtDAOHolder.INSTANCE;
    }

    /**
     * Get access token key for given userId and API Identifier
     *
     * @param userId          id of the user
     * @param applicationName name of the Application
     * @param identifier      APIIdentifier
     * @param keyType         Type of the key required
     * @return Access token
     * @throws APIManagementException if failed to get Access token
     */
    public String getAccessKeyForAPI(String userId, String applicationName, APIInfoDTO identifier, String keyType)
            throws APIManagementException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        String accessKey = null;
        String loginUserName = getLoginUserName(userId);

        //get the tenant id for the corresponding domain
        String tenantAwareUserId = MultitenantUtils.getTenantAwareUsername(loginUserName);
        int tenantId = APIUtil.getTenantId(loginUserName);

        if (log.isDebugEnabled()) {
            log.debug("Searching for: " + identifier.getAPIIdentifier() + ", User: " + tenantAwareUserId +
                      ", ApplicationName: " + applicationName + ", Tenant ID: " + tenantId);
        }

        String sqlQuery = SQLConstants.GET_ACCESS_KEY_FOR_API_SQL;
        if (forceCaseInsensitiveComparisons) {
            sqlQuery = SQLConstants.GET_ACCESS_KEY_FOR_API_CASE_INSENSITIVE_SQL;
        }
        try {
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            ps.setString(1, tenantAwareUserId);
            ps.setInt(2, tenantId);
            ps.setString(3, APIUtil.replaceEmailDomainBack(identifier.getProviderId()));
            ps.setString(4, identifier.getApiName());
            ps.setString(5, identifier.getVersion());
            ps.setString(6, applicationName);
            ps.setString(7, keyType);

            rs = ps.executeQuery();

            while (rs.next()) {
                accessKey = APIUtil.decryptToken(rs.getString(APIConstants.SUBSCRIPTION_FIELD_ACCESS_TOKEN));
            }
        } catch (SQLException e) {
            handleException("Error when executing the SQL query to read the access key for user : " + loginUserName +
                            "of tenant(id) : " + tenantId, e);
        } catch (CryptoException e) {
            handleException("Error when decrypting access key for user : " + loginUserName + "of tenant(id) : " +
                            tenantId, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
        return accessKey;
    }

    /**
     * Persist the details of the token generation request (allowed domains & validity period) to be used back
     * when approval has been granted.
     *
     * @param dto                 DTO related to Application Registration.
     * @param onlyKeyMappingEntry When this flag is enabled, only AM_APPLICATION_KEY_MAPPING will get affected.
     * @throws APIManagementException if failed to create entries in  AM_APPLICATION_REGISTRATION and
     *                                AM_APPLICATION_KEY_MAPPING tables.
     */
    public void createApplicationRegistrationEntry(ApplicationRegistrationWorkflowDTO dto, boolean onlyKeyMappingEntry)
            throws APIManagementException {
        Connection conn = null;
        PreparedStatement ps = null;
        PreparedStatement queryPs = null;
        PreparedStatement appRegPs = null;

        Application application = dto.getApplication();
        Subscriber subscriber = application.getSubscriber();
        String jsonString = dto.getAppInfoDTO().getOAuthApplicationInfo().getJsonString();

        String registrationQuery = SQLConstants.GET_APPLICATION_REGISTRATION_SQL;
        String registrationEntry = SQLConstants.ADD_APPLICATION_REGISTRATION_SQL;
        String keyMappingEntry = SQLConstants.ADD_APPLICATION_KEY_MAPPING_SQL;

        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);

            queryPs = conn.prepareStatement(registrationQuery);
            queryPs.setInt(1, subscriber.getId());
            queryPs.setInt(2, application.getId());
            queryPs.setString(3, dto.getKeyType());
            ResultSet resultSet = queryPs.executeQuery();

            if (resultSet.next()) {
                throw new APIManagementException("Application '" + application.getName() + "' is already registered.");
            }

            if (!onlyKeyMappingEntry) {
                appRegPs = conn.prepareStatement(registrationEntry);
                appRegPs.setInt(1, subscriber.getId());
                appRegPs.setString(2, dto.getWorkflowReference());
                appRegPs.setInt(3, application.getId());
                appRegPs.setString(4, dto.getKeyType());
                appRegPs.setString(5, dto.getDomainList());
                appRegPs.setLong(6, dto.getValidityTime());
                appRegPs.setString(7, (String) dto.getAppInfoDTO().getOAuthApplicationInfo().getParameter("tokenScope"));
                appRegPs.setString(8, jsonString);
                appRegPs.execute();
            }

            ps = conn.prepareStatement(keyMappingEntry);
            ps.setInt(1, application.getId());
            ps.setString(2, dto.getKeyType());
            ps.setString(3, dto.getStatus().toString());
            ps.execute();

            conn.commit();
        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException e1) {
                handleException("Error occurred while Rolling back changes done on Application Registration", e1);
            }
            handleException("Error occurred while creating an " +
                            "Application Registration Entry for Application : " + application.getName(), e);
        } finally {
            APIMgtDBUtil.closeStatement(queryPs);
            APIMgtDBUtil.closeStatement(appRegPs);
            APIMgtDBUtil.closeAllConnections(ps, conn, null);
        }
    }

    public OAuthApplicationInfo getOAuthApplication(String consumerKey) throws APIManagementException {
        OAuthApplicationInfo oAuthApplicationInfo = new OAuthApplicationInfo();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sqlQuery = SQLConstants.GET_OAUTH_APPLICATION_SQL;

        try {
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            ps.setString(1, consumerKey);
            rs = ps.executeQuery();
            while (rs.next()) {
                oAuthApplicationInfo.setClientId(consumerKey);
                oAuthApplicationInfo.setCallBackURL(rs.getString("CALLBACK_URL"));
                oAuthApplicationInfo.setClientSecret(APIUtil.decryptToken(rs.getString("CONSUMER_SECRET")));
                oAuthApplicationInfo.addParameter(ApplicationConstants.OAUTH_REDIRECT_URIS, rs.getString
                        ("CALLBACK_URL"));
                oAuthApplicationInfo.addParameter(ApplicationConstants.OAUTH_CLIENT_NAME, rs.getString("APP_NAME"));
                oAuthApplicationInfo.addParameter(ApplicationConstants.OAUTH_CLIENT_GRANT, rs.getString("GRANT_TYPES"));
            }
        } catch (SQLException e) {
            handleException("Error while executing SQL for getting OAuth application info", e);
        } catch (CryptoException e) {
            handleException("Unable to decrypt consumer secret of consumer key " + consumerKey, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
        return oAuthApplicationInfo;
    }

    /**
     * Get the creator of the OAuth App.
     *
     * @param consumerKey Client ID of the OAuth App
     * @return {@code Subscriber} with name and TenantId set.
     * @throws APIManagementException
     */
    public Subscriber getOwnerForConsumerApp(String consumerKey) throws APIManagementException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String username;
        Subscriber subscriber = null;

        String sqlQuery = SQLConstants.GET_OWNER_FOR_CONSUMER_APP_SQL;
        try {
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            ps.setString(1, consumerKey);
            rs = ps.executeQuery();
            while (rs.next()) {
                username = rs.getString("USERNAME");
                String domainName = rs.getString(APIConstants.IDENTITY_OAUTH2_FIELD_USER_DOMAIN);
                String endUsernameWithDomain = UserCoreUtil.addDomainToName(username, domainName);
                subscriber = new Subscriber(endUsernameWithDomain);
                subscriber.setTenantId(rs.getInt("TENANT_ID"));
            }
        } catch (SQLException e) {
            handleException("Error while executing SQL for getting User Id : SQL " + sqlQuery, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
        return subscriber;
    }

    /**
     * Get Subscribed APIs for given userId
     *
     * @param userId id of the user
     * @return APIInfoDTO[]
     * @throws APIManagementException if failed to get Subscribed APIs
     */
    public APIInfoDTO[] getSubscribedAPIsOfUser(String userId) throws APIManagementException {
        List<APIInfoDTO> apiInfoDTOList = new ArrayList<APIInfoDTO>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        //identify logged in user
        String loginUserName = getLoginUserName(userId);
        String tenantAwareUsername = MultitenantUtils.getTenantAwareUsername(loginUserName);
        int tenantId = APIUtil.getTenantId(loginUserName);

        String sqlQuery = SQLConstants.GET_SUBSCRIBED_APIS_OF_USER_SQL;
        if (forceCaseInsensitiveComparisons) {
            sqlQuery = SQLConstants.GET_SUBSCRIBED_APIS_OF_USER_CASE_INSENSITIVE_SQL;
        }

        try {
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            ps.setString(1, tenantAwareUsername);
            ps.setInt(2, tenantId);
            rs = ps.executeQuery();
            while (rs.next()) {
                APIInfoDTO infoDTO = new APIInfoDTO();
                infoDTO.setProviderId(APIUtil.replaceEmailDomain(rs.getString("API_PROVIDER")));
                infoDTO.setApiName(rs.getString("API_NAME"));
                infoDTO.setVersion(rs.getString("API_VERSION"));
                apiInfoDTOList.add(infoDTO);
            }
        } catch (SQLException e) {
            handleException("Error while executing SQL", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
        return apiInfoDTOList.toArray(new APIInfoDTO[apiInfoDTOList.size()]);
    }

    /**
     * Get API key information for given API
     *
     * @param apiInfoDTO API info
     * @return APIKeyInfoDTO[]
     * @throws APIManagementException if failed to get key info for given API
     */
    public APIKeyInfoDTO[] getSubscribedUsersForAPI(APIInfoDTO apiInfoDTO) throws APIManagementException {
        APIKeyInfoDTO[] apiKeyInfoDTOs = null;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<APIKeyInfoDTO> apiKeyInfoList = new ArrayList<APIKeyInfoDTO>();

        String sqlQuery = SQLConstants.GET_SUBSCRIBED_USERS_FOR_API_SQL;
        try {
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            ps.setString(1, APIUtil.replaceEmailDomainBack(apiInfoDTO.getProviderId()));
            ps.setString(2, apiInfoDTO.getApiName());
            ps.setString(3, apiInfoDTO.getVersion());
            rs = ps.executeQuery();
            while (rs.next()) {
                String userId = rs.getString(APIConstants.SUBSCRIBER_FIELD_USER_ID);
                APIKeyInfoDTO apiKeyInfoDTO = new APIKeyInfoDTO();
                apiKeyInfoDTO.setUserId(userId);
                apiKeyInfoList.add(apiKeyInfoDTO);
            }
            apiKeyInfoDTOs = apiKeyInfoList.toArray(new APIKeyInfoDTO[apiKeyInfoList.size()]);
        } catch (SQLException e) {
            handleException("Error while executing SQL", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
        return apiKeyInfoDTOs;
    }

    /**
     * This method is to update the access token
     *
     * @param userId     id of the user
     * @param apiInfoDTO Api info
     * @param statusEnum Status of the access key
     * @throws APIManagementException if failed to update the access token
     */
    public void changeAccessTokenStatus(String userId, APIInfoDTO apiInfoDTO, String statusEnum)
            throws APIManagementException {
        Connection conn = null;
        PreparedStatement ps = null;

        String tenantAwareUsername = MultitenantUtils.getTenantAwareUsername(userId);
        int tenantId = APIUtil.getTenantId(userId);

        String accessTokenStoreTable = APIConstants.ACCESS_TOKEN_STORE_TABLE;
        accessTokenStoreTable = getAccessTokenStoreTableNameOfUserId(userId, accessTokenStoreTable);

        String sqlQuery = SQLConstants.CHANGE_ACCESS_TOKEN_STATUS_PREFIX +
                          accessTokenStoreTable + SQLConstants.CHANGE_ACCESS_TOKEN_STATUS_DEFAULT_SUFFIX;

        if (forceCaseInsensitiveComparisons) {
            sqlQuery = SQLConstants.CHANGE_ACCESS_TOKEN_STATUS_PREFIX +
                       accessTokenStoreTable + SQLConstants.CHANGE_ACCESS_TOKEN_STATUS_CASE_INSENSITIVE_SUFFIX;
        }

        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);

            ps = conn.prepareStatement(sqlQuery);
            ps.setString(1, statusEnum);
            ps.setString(2, tenantAwareUsername);
            ps.setInt(3, tenantId);
            ps.setString(4, APIUtil.replaceEmailDomainBack(apiInfoDTO.getProviderId()));
            ps.setString(5, apiInfoDTO.getApiName());
            ps.setString(6, apiInfoDTO.getVersion());

            int count = ps.executeUpdate();
            if (log.isDebugEnabled()) {
                log.debug("Number of rows being updated : " + count);
            }
            conn.commit();
        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException e1) {
                log.error("Failed to rollback the changeAccessTokenStatus operation", e1);
            }
            handleException("Error while executing SQL", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, null);
        }
    }

    /**
     * Validate the provided key against the given API. First it will validate the key is valid
     * , ACTIVE and not expired.
     *
     * @param context     Requested Context
     * @param version     version of the API
     * @param accessToken Provided Access Token
     * @return APIKeyValidationInfoDTO instance with authorization status and tier information if
     * authorized.
     * @throws APIManagementException Error when accessing the database or registry.
     */
    public APIKeyValidationInfoDTO validateKey(String context, String version, String accessToken,
                                               String requiredAuthenticationLevel) throws APIManagementException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        String tier;
        String status;
        String type;
        String userType;
        String subscriberName;
        String subscriptionStatus;
        String applicationId;
        String applicationName;
        String applicationTier;
        String endUserName;
        String domainName;
        long validityPeriod;
        long issuedTime;
        long timestampSkew;
        long currentTime;
        String apiName;
        String consumerKey;
        String apiPublisher;

        boolean defaultVersionInvoked = false;

        if (log.isDebugEnabled()) {
            log.debug("A request is received to process the token : " + accessToken + " to access" +
                      " the context URL : " + context);
        }
        APIKeyValidationInfoDTO keyValidationInfoDTO = new APIKeyValidationInfoDTO();
        keyValidationInfoDTO.setAuthorized(false);

        //Check if the api version has been prefixed with _default_
        if (version != null && version.startsWith(APIConstants.DEFAULT_VERSION_PREFIX)) {
            defaultVersionInvoked = true;
            //Remove the prefix from the version.
            version = version.split(APIConstants.DEFAULT_VERSION_PREFIX)[1];
        }

        String accessTokenStoreTable = APIConstants.ACCESS_TOKEN_STORE_TABLE;
        accessTokenStoreTable = getAccessTokenStoreTableFromAccessToken(accessToken, accessTokenStoreTable);

        String applicationSqlQuery;
        if (defaultVersionInvoked) {
            applicationSqlQuery = SQLConstants.VALIDATE_KEY_SQL_PREFIX + accessTokenStoreTable + SQLConstants
                    .VALIDATE_KEY_DEFAULT_SUFFIX;
        } else {
            applicationSqlQuery = SQLConstants.VALIDATE_KEY_SQL_PREFIX + accessTokenStoreTable + SQLConstants
                    .VALIDATE_KEY_VERSION_SUFFIX;
        }

        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);

            ps = conn.prepareStatement(applicationSqlQuery);
            String encryptedAccessToken = APIUtil.encryptToken(accessToken);
            ps.setString(1, encryptedAccessToken);
            ps.setString(2, context);

            //We only do the version check for non-default version API invocations
            if (!defaultVersionInvoked) {
                ps.setString(3, version);
            }
            rs = ps.executeQuery();
            if (rs.next()) {
                status = rs.getString(APIConstants.IDENTITY_OAUTH2_FIELD_TOKEN_STATE);
                tier = rs.getString(APIConstants.SUBSCRIPTION_FIELD_TIER_ID);
                type = rs.getString(APIConstants.SUBSCRIPTION_KEY_TYPE);
                userType = rs.getString(APIConstants.SUBSCRIPTION_USER_TYPE);
                subscriberName = rs.getString(APIConstants.SUBSCRIBER_FIELD_USER_ID);
                applicationId = rs.getString(APIConstants.APPLICATION_ID);
                applicationName = rs.getString(APIConstants.APPLICATION_NAME);
                applicationTier = rs.getString(APIConstants.APPLICATION_TIER);
                endUserName = rs.getString(APIConstants.IDENTITY_OAUTH2_FIELD_AUTHORIZED_USER);
                domainName = rs.getString(APIConstants.IDENTITY_OAUTH2_FIELD_USER_DOMAIN);
                issuedTime = rs.getTimestamp(APIConstants.IDENTITY_OAUTH2_FIELD_TIME_CREATED, Calendar.getInstance
                        (TimeZone.getTimeZone("UTC"))).getTime();
                validityPeriod = rs.getLong(APIConstants.IDENTITY_OAUTH2_FIELD_VALIDITY_PERIOD);
                timestampSkew = OAuthServerConfiguration.getInstance().getTimeStampSkewInSeconds() * 1000;
                currentTime = System.currentTimeMillis();
                subscriptionStatus = rs.getString(APIConstants.SUBSCRIPTION_FIELD_SUB_STATUS);
                apiName = rs.getString(APIConstants.FIELD_API_NAME);
                consumerKey = rs.getString(APIConstants.FIELD_CONSUMER_KEY);
                apiPublisher = rs.getString(APIConstants.FIELD_API_PUBLISHER);

                String endUsernameWithDomain = UserCoreUtil.addDomainToName(endUserName, domainName);

                keyValidationInfoDTO.setApiName(apiName);
                keyValidationInfoDTO.setApiPublisher(apiPublisher);
                keyValidationInfoDTO.setApplicationId(applicationId);
                keyValidationInfoDTO.setApplicationName(applicationName);
                keyValidationInfoDTO.setApplicationTier(applicationTier);
                keyValidationInfoDTO.setConsumerKey(consumerKey);
                keyValidationInfoDTO.setEndUserName(endUsernameWithDomain);
                keyValidationInfoDTO.setIssuedTime(issuedTime);
                keyValidationInfoDTO.setTier(tier);
                keyValidationInfoDTO.setType(type);
                keyValidationInfoDTO.setUserType(userType);
                keyValidationInfoDTO.setValidityPeriod(validityPeriod);
                keyValidationInfoDTO.setSubscriber(subscriberName);

                //keyValidationInfoDTO.setAuthorizedDomains(getAuthorizedDomainList(accessToken));
                keyValidationInfoDTO.setConsumerKey(consumerKey);
                Set<String> scopes = new HashSet<String>();

                do {
                    String scope = rs.getString(APIConstants.IDENTITY_OAUTH2_FIELD_TOKEN_SCOPE);
                    if (scope != null && !scope.isEmpty()) {
                        scopes.add(scope);
                    }
                } while (rs.next());

                keyValidationInfoDTO.setScopes(scopes);
                
                /* If Subscription Status is PROD_ONLY_BLOCKED, block production access only */
                if (APIConstants.SubscriptionStatus.BLOCKED.equals(subscriptionStatus)) {
                    keyValidationInfoDTO.setValidationStatus(APIConstants.KeyValidationStatus.API_BLOCKED);
                    keyValidationInfoDTO.setAuthorized(false);
                    return keyValidationInfoDTO;
                } else if (APIConstants.SubscriptionStatus.ON_HOLD.equals(subscriptionStatus) || APIConstants
                        .SubscriptionStatus.REJECTED.equals(subscriptionStatus)) {
                    keyValidationInfoDTO.setValidationStatus(APIConstants.KeyValidationStatus.SUBSCRIPTION_INACTIVE);
                    keyValidationInfoDTO.setAuthorized(false);
                    return keyValidationInfoDTO;
                } else if (APIConstants.SubscriptionStatus.PROD_ONLY_BLOCKED.equals(subscriptionStatus) &&
                           !APIConstants.API_KEY_TYPE_SANDBOX.equals(type)) {
                    keyValidationInfoDTO.setValidationStatus(APIConstants.KeyValidationStatus.API_BLOCKED);
                    keyValidationInfoDTO.setAuthorized(false);
                    return keyValidationInfoDTO;
                }

                //check if 'requiredAuthenticationLevel' & the one associated with access token matches
                //This check should only be done for 'Application' and 'Application_User' levels
                if (APIConstants.AUTH_APPLICATION_LEVEL_TOKEN.equals(requiredAuthenticationLevel) || APIConstants
                        .AUTH_APPLICATION_USER_LEVEL_TOKEN.equals(requiredAuthenticationLevel)) {
                    if (log.isDebugEnabled()) {
                        log.debug("Access token's userType : " + userType + ".Required type : " +
                                  requiredAuthenticationLevel);
                    }

                    if (!(userType.equalsIgnoreCase(requiredAuthenticationLevel))) {
                        keyValidationInfoDTO.setValidationStatus(APIConstants.KeyValidationStatus
                                                                         .API_AUTH_INCORRECT_ACCESS_TOKEN_TYPE);
                        keyValidationInfoDTO.setAuthorized(false);
                        return keyValidationInfoDTO;
                    }
                }

                // Check whether the token is ACTIVE
                if (APIConstants.TokenStatus.ACTIVE.equals(status)) {
                    if (log.isDebugEnabled()) {
                        log.debug("Checking Access token: " + accessToken + " for validity." +
                                  "((currentTime - timestampSkew) > (issuedTime + validityPeriod)) : " +
                                  "((" + currentTime + '-' + timestampSkew + ')' + " > (" + issuedTime + " + " +
                                  validityPeriod + "))");
                    }
                    if (validityPeriod != Long.MAX_VALUE && (currentTime - timestampSkew) > (issuedTime +
                                                                                             validityPeriod)) {
                        keyValidationInfoDTO.setValidationStatus(APIConstants.KeyValidationStatus
                                                                         .API_AUTH_INVALID_CREDENTIALS);
                        if (log.isDebugEnabled()) {
                            log.debug("Access token: " + accessToken + " has expired. " +
                                      "Reason ((currentTime - timestampSkew) > (issuedTime + validityPeriod)) : " +
                                      "((" + currentTime + '-' + timestampSkew + ')' + " > (" + issuedTime + " + " +
                                      validityPeriod + "))");
                        }
                        //update token status as expired
                        updateTokenState(accessToken, conn, ps);
                        conn.commit();
                    } else {
                        keyValidationInfoDTO.setAuthorized(true);
                    }
                } else {
                    keyValidationInfoDTO.setValidationStatus(APIConstants.KeyValidationStatus
                                                                     .API_AUTH_INVALID_CREDENTIALS);
                    if (log.isDebugEnabled()) {
                        log.debug("Access token: " + accessToken + " is inactive");
                    }
                }
            } else {
                //no record found. Invalid access token received
                keyValidationInfoDTO.setValidationStatus(APIConstants.KeyValidationStatus.API_AUTH_INVALID_CREDENTIALS);
                if (log.isDebugEnabled()) {
                    log.debug("Access token: " + accessToken + " is invalid");
                }
            }
        } catch (SQLException e) {
            handleException("Error when executing the SQL ", e);
        } catch (CryptoException e) {
            handleException("Error when encrypting/decrypting token(s)", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
        return keyValidationInfoDTO;
    }

    @Deprecated
    public Map<String, Object> getSubscriptionDetails(String context, String version, String consumerKey)
            throws APIManagementException {

        String sql = "SELECT " +
                     "   SUB.TIER_ID," +
                     "   SUBS.USER_ID," +
                     "   SUB.SUB_STATUS," +
                     "   APP.APPLICATION_ID," +
                     "   APP.NAME," +
                     "   APP.APPLICATION_TIER," +
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
                     " API.CONTEXT = ? " +
                     " AND API.API_VERSION = ? " +
                     " AND AKM.CONSUMER_KEY = ? " +
                     "   AND SUB.APPLICATION_ID = APP.APPLICATION_ID" +
                     "   AND APP.SUBSCRIBER_ID = SUBS.SUBSCRIBER_ID" +
                     "   AND API.API_ID = SUB.API_ID" +
                     "   AND AKM.APPLICATION_ID=APP.APPLICATION_ID";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Map<String, Object> results = null;
        try {
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, context);
            ps.setString(2, version);
            ps.setString(3, consumerKey);
            rs = ps.executeQuery();
            if (rs.next()) {
                results = new HashMap<String, Object>();

                results.put("tier_id", rs.getString("TIER_ID"));
                results.put("user_id", rs.getString("USER_ID"));
                results.put("subs_status", rs.getString("SUB_STATUS"));
                results.put("app_id", rs.getString("APPLICATION_ID"));
                results.put("key_type", rs.getString("KEY_TYPE"));
                results.put("api_name", rs.getString("API_NAME"));
                results.put("api_provider", rs.getString("API_PROVIDER"));
                results.put("app_name", rs.getString("NAME"));
                results.put("app_tier", rs.getString("APPLICATION_TIER"));
            }
        } catch (SQLException e) {
            handleException("Error occurred while reading subscription details from the database.", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
        return results;
    }

    public boolean validateSubscriptionDetails(String context, String version, String consumerKey,
                                               APIKeyValidationInfoDTO infoDTO) throws APIManagementException {
        boolean defaultVersionInvoked = false;
        String apiTenantDomain = MultitenantUtils.getTenantDomainFromRequestURL(context);
        if(apiTenantDomain == null) {
            apiTenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }
        int apiOwnerTenantId = APIUtil.getTenantIdFromTenantDomain(apiTenantDomain);
        //Check if the api version has been prefixed with _default_
        if (version != null && version.startsWith(APIConstants.DEFAULT_VERSION_PREFIX)) {
            defaultVersionInvoked = true;
            //Remove the prefix from the version.
            version = version.split(APIConstants.DEFAULT_VERSION_PREFIX)[1];
        }
        String sql;
        boolean isAdvancedThrottleEnabled = APIUtil.isAdvanceThrottlingEnabled();
        if(!isAdvancedThrottleEnabled) {
            if (defaultVersionInvoked) {
                sql = SQLConstants.VALIDATE_SUBSCRIPTION_KEY_DEFAULT_SQL;
            } else {
                sql = SQLConstants.VALIDATE_SUBSCRIPTION_KEY_VERSION_SQL;
            }
        } else {
            if (defaultVersionInvoked) {
                sql = SQLConstants.ADVANCED_VALIDATE_SUBSCRIPTION_KEY_DEFAULT_SQL;
            } else {
                sql = SQLConstants.ADVANCED_VALIDATE_SUBSCRIPTION_KEY_VERSION_SQL;
            }
        }

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(true);
            ps = conn.prepareStatement(sql);
            ps.setString(1, context);
            ps.setString(2, consumerKey);
            if(isAdvancedThrottleEnabled) {
                ps.setInt(3, apiOwnerTenantId);
                if (!defaultVersionInvoked) {
                    ps.setString(4, version);
                }
            } else {
                if (!defaultVersionInvoked) {
                    ps.setString(3, version);
                }
            }

            rs = ps.executeQuery();
            if (rs.next()) {
                String subscriptionStatus = rs.getString("SUB_STATUS");
                String type = rs.getString("KEY_TYPE");
                if (APIConstants.SubscriptionStatus.BLOCKED.equals(subscriptionStatus)) {
                    infoDTO.setValidationStatus(APIConstants.KeyValidationStatus.API_BLOCKED);
                    infoDTO.setAuthorized(false);
                    return false;
                } else if (APIConstants.SubscriptionStatus.ON_HOLD.equals(subscriptionStatus) || APIConstants
                        .SubscriptionStatus.REJECTED.equals(subscriptionStatus)) {
                    infoDTO.setValidationStatus(APIConstants.KeyValidationStatus.SUBSCRIPTION_INACTIVE);
                    infoDTO.setAuthorized(false);
                    return false;
                } else if (APIConstants.SubscriptionStatus.PROD_ONLY_BLOCKED.equals(subscriptionStatus) &&
                           !APIConstants.API_KEY_TYPE_SANDBOX.equals(type)) {
                    infoDTO.setValidationStatus(APIConstants.KeyValidationStatus.API_BLOCKED);
                    infoDTO.setType(type);
                    infoDTO.setAuthorized(false);
                    return false;
                }

                String apiProvider = rs.getString("API_PROVIDER");
                String subTier = rs.getString("TIER_ID");
                String appTier = rs.getString("APPLICATION_TIER");
                infoDTO.setTier(subTier);
                infoDTO.setSubscriber(rs.getString("USER_ID"));
                infoDTO.setApplicationId(rs.getString("APPLICATION_ID"));
                infoDTO.setApiName(rs.getString("API_NAME"));
                infoDTO.setApiPublisher(apiProvider);
                infoDTO.setApplicationName(rs.getString("NAME"));
                infoDTO.setApplicationTier(appTier);
                infoDTO.setType(type);

                //Advanced Level Throttling Related Properties
                if(APIUtil.isAdvanceThrottlingEnabled()) {
                    String apiTier = rs.getString("API_TIER");
                    String subscriberUserId = rs.getString("USER_ID");
                    String subscriberTenant = MultitenantUtils.getTenantDomain(subscriberUserId);
                    int apiId = rs.getInt("API_ID");
                    int subscriberTenantId = APIUtil.getTenantId(subscriberUserId);
                    int apiTenantId = APIUtil.getTenantId(apiProvider);
                    //TODO isContentAware
                    boolean isContentAware = isAnyPolicyContentAware(conn, apiTier, appTier, subTier, subscriberTenantId, apiTenantId, apiId);
                    infoDTO.setContentAware(isContentAware);

                    //TODO this must implement as a part of throttling implementation.
                    int spikeArrest = 0;
                    String apiLevelThrottlingKey = "api_level_throttling_key";
                    if (rs.getInt("RATE_LIMIT_COUNT") > 0) {
                        spikeArrest = rs.getInt("RATE_LIMIT_COUNT");
                    }

                    String spikeArrestUnit = null;
                    if (rs.getString("RATE_LIMIT_TIME_UNIT") != null) {
                        spikeArrestUnit = rs.getString("RATE_LIMIT_TIME_UNIT");
                    }
                    boolean stopOnQuotaReach = rs.getBoolean("STOP_ON_QUOTA_REACH");
                    List<String> list = new ArrayList<String>();
                    list.add(apiLevelThrottlingKey);
                    infoDTO.setSpikeArrestLimit(spikeArrest);
                    infoDTO.setSpikeArrestUnit(spikeArrestUnit);
                    infoDTO.setStopOnQuotaReach(stopOnQuotaReach);
                    infoDTO.setSubscriberTenantDomain(subscriberTenant);
                    if (apiTier != null && apiTier.trim().length() > 0) {
                        infoDTO.setApiTier(apiTier);
                    }
                    //We also need to set throttling data list associated with given API. This need to have policy id and
                    // condition id list for all throttling tiers associated with this API.
                    infoDTO.setThrottlingDataList(list);
                }
                return true;
            }
            infoDTO.setAuthorized(false);
            infoDTO.setValidationStatus(APIConstants.KeyValidationStatus.API_AUTH_RESOURCE_FORBIDDEN);
        } catch (SQLException e) {
            handleException("Exception occurred while validating Subscription.", e);
        } finally {
        	try {
				conn.setAutoCommit(false);
			} catch (SQLException e) {
				
			}
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
        return false;
    }

	private boolean isAnyPolicyContentAware(Connection conn, String apiPolicy, String appPolicy,
			String subPolicy, int subscriptionTenantId, int appTenantId, int apiId) throws APIManagementException {
		boolean isAnyContentAware = false;
		// only check if using CEP based throttling.
		ResultSet resultSet = null;
		PreparedStatement ps = null;
		String sqlQuery = SQLConstants.ThrottleSQLConstants.IS_ANY_POLICY_CONTENT_AWARE_SQL;

		try {
			String dbProdName = conn.getMetaData().getDatabaseProductName();
			/*if("oracle".equalsIgnoreCase(dbProdName.toLowerCase()) || conn.getMetaData().getDriverName().toLowerCase().contains("oracle")){
				sqlQuery = sqlQuery.replaceAll("\\+", "union all");
				sqlQuery = sqlQuery.replaceFirst("select", "select sum(c) from ");
			}else if(dbProdName.toLowerCase().contains("microsoft") && dbProdName.toLowerCase().contains("sql")){
				sqlQuery = sqlQuery.replaceAll("\\+", "union all");
				sqlQuery = sqlQuery.replaceFirst("select", "select sum(c) from ");
				sqlQuery = sqlQuery + " x";
            }*/

			ps = conn.prepareStatement(sqlQuery);
			ps.setString(1, apiPolicy);
			ps.setInt(2, subscriptionTenantId);
			ps.setString(3, apiPolicy);
			ps.setInt(4, subscriptionTenantId);
			ps.setInt(5, apiId);
			ps.setInt(6, subscriptionTenantId);
			ps.setInt(7, apiId);
			ps.setInt(8, subscriptionTenantId);
			ps.setString(9, subPolicy);
			ps.setInt(10, subscriptionTenantId);
			ps.setString(11, appPolicy);
			ps.setInt(12, appTenantId);
			resultSet = ps.executeQuery();
			// We only expect one result if all are not content aware.
			if (resultSet == null) {
				throw new APIManagementException(" Result set Null");
			}
			int count = 0;
			if (resultSet.next()) {
				count = resultSet.getInt(1);
				if(count > 0){
					isAnyContentAware = true;
				}
			}
		} catch (SQLException e) {
			handleException("Failed to get content awareness of the policies ", e);
		} finally {
			APIMgtDBUtil.closeAllConnections(ps, null, resultSet);
		}
		return isAnyContentAware;
	}

    private void updateTokenState(String accessToken, Connection conn, PreparedStatement ps)
            throws SQLException, APIManagementException, CryptoException {
        String accessTokenStoreTable = APIConstants.ACCESS_TOKEN_STORE_TABLE;
        accessTokenStoreTable = getAccessTokenStoreTableFromAccessToken(accessToken, accessTokenStoreTable);
        String encryptedAccessToken = APIUtil.encryptToken(accessToken);
        String updateTokeStateSql = SQLConstants.UPDATE_TOKEN_PREFIX + accessTokenStoreTable +
                                    SQLConstants.UPDATE_TOKEN_SUFFIX;

        ps = conn.prepareStatement(updateTokeStateSql);
        ps.setString(1, "EXPIRED");
        ps.setString(2, UUID.randomUUID().toString());
        ps.setString(3, encryptedAccessToken);
        ps.executeUpdate();
    }

    public void addSubscriber(Subscriber subscriber, String groupingId) throws APIManagementException {
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);

            String query = SQLConstants.ADD_SUBSCRIBER_SQL;
            ps = conn.prepareStatement(query, new String[]{"subscriber_id"});

            ps.setString(1, subscriber.getName());
            ps.setInt(2, subscriber.getTenantId());
            ps.setString(3, subscriber.getEmail());
            ps.setTimestamp(4, new Timestamp(subscriber.getSubscribedDate().getTime()));
            ps.setString(5, subscriber.getName());
            ps.setTimestamp(6, new Timestamp(subscriber.getSubscribedDate().getTime()));
            ps.executeUpdate();

            int subscriberId = 0;
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                subscriberId = Integer.parseInt(rs.getString(1));
            }
            subscriber.setId(subscriberId);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error("Error while rolling back the failed operation", e1);
                }
            }
            handleException("Error in adding new subscriber: " + e.getMessage(), e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
    }

    public void updateSubscriber(Subscriber subscriber) throws APIManagementException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);

            String query = SQLConstants.UPDATE_SUBSCRIBER_SQL;

            ps = conn.prepareStatement(query);
            ps.setString(1, subscriber.getName());
            ps.setInt(2, subscriber.getTenantId());
            ps.setString(3, subscriber.getEmail());
            ps.setTimestamp(4, new Timestamp(subscriber.getSubscribedDate().getTime()));
            ps.setString(5, subscriber.getName());
            ps.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
            ps.setInt(7, subscriber.getId());
            ps.executeUpdate();

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error("Error while rolling back the failed operation", e1);
                }
            }
            handleException("Error in updating subscriber: " + e.getMessage(), e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, null);
        }
    }

    public Subscriber getSubscriber(int subscriberId) throws APIManagementException {
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            conn = APIMgtDBUtil.getConnection();
            String query = SQLConstants.GET_SUBSCRIBER_SQL;

            ps = conn.prepareStatement(query);
            ps.setInt(1, subscriberId);
            rs = ps.executeQuery();
            if (rs.next()) {
                Subscriber subscriber = new Subscriber(rs.getString("USER_ID"));
                subscriber.setId(subscriberId);
                subscriber.setTenantId(rs.getInt("TENANT_ID"));
                subscriber.setEmail(rs.getString("EMAIL_ADDRESS"));
                subscriber.setSubscribedDate(new java.util.Date(rs.getTimestamp("DATE_SUBSCRIBED").getTime()));
                return subscriber;
            }
        } catch (SQLException e) {
            handleException("Error while retrieving subscriber: " + e.getMessage(), e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
        return null;
    }

    public int addSubscription(APIIdentifier identifier, String context, int applicationId, String status,
            String subscriber) throws APIManagementException {
        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        PreparedStatement preparedStForInsert = null;
        ResultSet rs = null;
        int subscriptionId = -1;
        int apiId;

        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);
            apiId = getAPIID(identifier, conn);

            //Query to check if this subscription already exists
            String checkDuplicateQuery = SQLConstants.CHECK_EXISTING_SUBSCRIPTION_SQL;
            ps = conn.prepareStatement(checkDuplicateQuery);
            ps.setInt(1, apiId);
            ps.setInt(2, applicationId);

            resultSet = ps.executeQuery();

            //If the subscription already exists
            if (resultSet.next()) {
                String subStatus = resultSet.getString("SUB_STATUS");
                String subCreationStatus = resultSet.getString("SUBS_CREATE_STATE");

                String applicationName = getApplicationNameFromId(applicationId);

                if ((APIConstants.SubscriptionStatus.UNBLOCKED.equals(subStatus) ||
                        APIConstants.SubscriptionStatus.ON_HOLD.equals(subStatus) ||
                        APIConstants.SubscriptionStatus.REJECTED.equals(subStatus)) &&
                        APIConstants.SubscriptionCreatedStatus.SUBSCRIBE.equals(subCreationStatus)) {

                    //Throw error saying subscription already exists.
                    log.error("Subscription already exists for API " + identifier.getApiName() + " in Application " +
                              applicationName);
                    throw new SubscriptionAlreadyExistingException("Subscription already exists for API " +
                                                                   identifier.getApiName() + " in Application " +
                                                                   applicationName);
                } else if (APIConstants.SubscriptionStatus.UNBLOCKED.equals(subStatus) && APIConstants
                        .SubscriptionCreatedStatus.UN_SUBSCRIBE.equals(subCreationStatus)) {
                    deleteSubscriptionByApiIDAndAppID(apiId, applicationId, conn);
                } else if (APIConstants.SubscriptionStatus.BLOCKED.equals(subStatus) || APIConstants
                        .SubscriptionStatus.PROD_ONLY_BLOCKED.equals(subStatus)) {
                    log.error("Subscription to API " + identifier.getApiName() + " through application " +
                              applicationName + " was blocked");
                    throw new APIManagementException("Subscription to API " + identifier.getApiName() + " through " +
                                                     "application " + applicationName + " was blocked");
                }
            }

            //This query to update the AM_SUBSCRIPTION table
            String sqlQuery = SQLConstants.ADD_SUBSCRIPTION_SQL;

            //Adding data to the AM_SUBSCRIPTION table
            //ps = conn.prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS);
            preparedStForInsert = conn.prepareStatement(sqlQuery, new String[]{"SUBSCRIPTION_ID"});
            if (conn.getMetaData().getDriverName().contains("PostgreSQL")) {
                preparedStForInsert = conn.prepareStatement(sqlQuery, new String[]{"subscription_id"});
            }

            preparedStForInsert.setString(1, identifier.getTier());
            preparedStForInsert.setInt(2, apiId);
            preparedStForInsert.setInt(3, applicationId);
            preparedStForInsert.setString(4, status != null ? status : APIConstants.SubscriptionStatus.UNBLOCKED);
            preparedStForInsert.setString(5, APIConstants.SubscriptionCreatedStatus.SUBSCRIBE);
            preparedStForInsert.setString(6, subscriber);
            preparedStForInsert.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
            preparedStForInsert.setString(8, UUID.randomUUID().toString());

            preparedStForInsert.executeUpdate();
            rs = preparedStForInsert.getGeneratedKeys();
            while (rs.next()) {
                //subscriptionId = rs.getInt(1);
                subscriptionId = Integer.parseInt(rs.getString(1));
            }

            // finally commit transaction
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error("Failed to rollback the add subscription ", e1);
                }
            }
            handleException("Failed to add subscriber data ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, resultSet);
            APIMgtDBUtil.closeAllConnections(preparedStForInsert, null, rs);
        }
        return subscriptionId;
    }

    /**
     * Removes the subscription entry from AM_SUBSCRIPTIONS for identifier.
     *
     * @param identifier    APIIdentifier
     * @param applicationId ID of the application which has the subscription
     * @throws APIManagementException
     */
    public void removeSubscription(APIIdentifier identifier, int applicationId)
            throws APIManagementException {
        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        int apiId = -1;
        String uuid;
        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);
            apiId = getAPIID(identifier, conn);

            String subscriptionUUIDQuery = SQLConstants.GET_SUBSCRIPTION_UUID_SQL;

            ps = conn.prepareStatement(subscriptionUUIDQuery);
            ps.setInt(1, apiId);
            ps.setInt(2, applicationId);
            resultSet = ps.executeQuery();

            if (resultSet.next()) {
                uuid = resultSet.getString("UUID");
                SubscribedAPI subscribedAPI = new SubscribedAPI(uuid);
                removeSubscription(subscribedAPI, conn);
            } else {
                throw new APIManagementException("UUID does not exist for the given apiId:" + apiId + " and " +
                                                 "application id:" + applicationId);
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    log.error("Failed to rollback the add subscription ", ex);
                }
            }
            handleException("Failed to add subscriber data ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, null, resultSet);
        }
    }

    /**
     * Removes a subscription specified by SubscribedAPI object
     *
     * @param subscription SubscribedAPI object
     * @param conn         database connection object
     * @throws APIManagementException
     */
    public void removeSubscription(SubscribedAPI subscription, Connection conn) throws APIManagementException {
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        PreparedStatement preparedStForUpdateOrDelete = null;
        String subStatus = null;

        try {
            String subscriptionStatusQuery = SQLConstants.GET_SUBSCRIPTION_STATUS_BY_UUID_SQL;

            ps = conn.prepareStatement(subscriptionStatusQuery);
            ps.setString(1, subscription.getUUID());
            resultSet = ps.executeQuery();

            if (resultSet.next()) {
                subStatus = resultSet.getString("SUB_STATUS");
            }

            // If the user was unblocked, remove the entry from DB, else change the status and keep the entry.
            String updateQuery = SQLConstants.UPDATE_SUBSCRIPTION_SQL;
            String deleteQuery = SQLConstants.REMOVE_SUBSCRIPTION_SQL;

            if (APIConstants.SubscriptionStatus.BLOCKED.equals(subStatus) || APIConstants.SubscriptionStatus
                    .PROD_ONLY_BLOCKED.equals(subStatus)) {
                preparedStForUpdateOrDelete = conn.prepareStatement(updateQuery);
                preparedStForUpdateOrDelete.setString(1, subscription.getUUID());
            } else {
                preparedStForUpdateOrDelete = conn.prepareStatement(deleteQuery);
                preparedStForUpdateOrDelete.setString(1, subscription.getUUID());
            }
            preparedStForUpdateOrDelete.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed to add subscriber data ", e);
            handleException("Failed to add subscriber data ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, null, resultSet);
            APIMgtDBUtil.closeAllConnections(preparedStForUpdateOrDelete, null, null);
        }
    }


    /**
     * Removes a subscription by id by force without considering the subscription blocking state of the user
     *
     * @param subscription_id id of subscription
     * @throws APIManagementException
     */
    public void removeSubscriptionById(int subscription_id) throws APIManagementException {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);

            String sqlQuery = SQLConstants.REMOVE_SUBSCRIPTION_BY_ID_SQL;

            ps = conn.prepareStatement(sqlQuery);
            ps.setInt(1, subscription_id);
            ps.executeUpdate();

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error("Failed to rollback remove subscription ", e1);
                }
            }
            handleException("Failed to remove subscription data ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, null);
        }
    }

    public void removeAllSubscriptions(APIIdentifier apiIdentifier) throws APIManagementException {
        Connection conn = null;
        PreparedStatement ps = null;
        int apiId;

        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);
            apiId = getAPIID(apiIdentifier, conn);

            String sqlQuery = SQLConstants.REMOVE_ALL_SUBSCRIPTIONS_SQL;

            ps = conn.prepareStatement(sqlQuery);
            ps.setInt(1, apiId);
            ps.executeUpdate();

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error("Failed to rollback remove all subscription ", e1);
                }
            }
            handleException("Failed to remove all subscriptions data ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, null);
        }
    }

    public String getSubscriptionStatusById(int subscriptionId) throws APIManagementException {

        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        String subscriptionStatus = null;

        try {
            conn = APIMgtDBUtil.getConnection();
            String getApiQuery = SQLConstants.GET_SUBSCRIPTION_STATUS_BY_ID_SQL;
            ps = conn.prepareStatement(getApiQuery);
            ps.setInt(1, subscriptionId);
            resultSet = ps.executeQuery();
            if (resultSet.next()) {
                subscriptionStatus = resultSet.getString("SUB_STATUS");
            }
            return subscriptionStatus;
        } catch (SQLException e) {
            handleException("Failed to retrieve subscription status", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, resultSet);
        }
        return null;
    }

    /**
     * returns the SubscribedAPI object which is related to the subscriptionId
     *
     * @param subscriptionId subscription id
     * @return {@link SubscribedAPI} Object which contains the subscribed API information.
     * @throws APIManagementException
     */
    public SubscribedAPI getSubscriptionById(int subscriptionId) throws APIManagementException {
        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;

        try {
            conn = APIMgtDBUtil.getConnection();
            String getSubscriptionQuery = SQLConstants.GET_SUBSCRIPTION_BY_ID_SQL;
            ps = conn.prepareStatement(getSubscriptionQuery);
            ps.setInt(1, subscriptionId);
            resultSet = ps.executeQuery();
            SubscribedAPI subscribedAPI = null;
            if (resultSet.next()) {
                APIIdentifier apiIdentifier = new APIIdentifier(APIUtil.replaceEmailDomain(resultSet.getString
                        ("API_PROVIDER")), resultSet.getString("API_NAME"), resultSet.getString("API_VERSION"));

                int applicationId = resultSet.getInt("APPLICATION_ID");
                Application application = getApplicationById(applicationId);
                subscribedAPI = new SubscribedAPI(application.getSubscriber(), apiIdentifier);
                subscribedAPI.setSubscriptionId(resultSet.getInt("SUBSCRIPTION_ID"));
                subscribedAPI.setSubStatus(resultSet.getString("SUB_STATUS"));
                subscribedAPI.setSubCreatedStatus(resultSet.getString("SUBS_CREATE_STATE"));
                subscribedAPI.setTier(new Tier(resultSet.getString("TIER_ID")));
                subscribedAPI.setUUID(resultSet.getString("UUID"));
                subscribedAPI.setApplication(application);
            }
            return subscribedAPI;
        } catch (SQLException e) {
            handleException("Failed to retrieve subscription from subscription id", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, resultSet);
        }
        return null;
    }

    /**
     * returns the SubscribedAPI object which is related to the UUID
     *
     * @param uuid UUID of Application
     * @return {@link SubscribedAPI} Object which contains the subscribed API information.
     * @throws APIManagementException
     */
    public SubscribedAPI getSubscriptionByUUID(String uuid) throws APIManagementException {
        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;

        try {
            conn = APIMgtDBUtil.getConnection();
            String getSubscriptionQuery = SQLConstants.GET_SUBSCRIPTION_BY_UUID_SQL;
            ps = conn.prepareStatement(getSubscriptionQuery);
            ps.setString(1, uuid);
            resultSet = ps.executeQuery();
            SubscribedAPI subscribedAPI = null;
            if (resultSet.next()) {
                APIIdentifier apiIdentifier = new APIIdentifier(APIUtil.replaceEmailDomain(resultSet.getString
                        ("API_PROVIDER")), resultSet.getString("API_NAME"), resultSet.getString("API_VERSION"));

                int applicationId = resultSet.getInt("APPLICATION_ID");
                Application application = getApplicationById(applicationId);
                subscribedAPI = new SubscribedAPI(application.getSubscriber(), apiIdentifier);
                subscribedAPI.setUUID(resultSet.getString("UUID"));
                subscribedAPI.setSubscriptionId(resultSet.getInt("SUBSCRIPTION_ID"));
                subscribedAPI.setSubStatus(resultSet.getString("SUB_STATUS"));
                subscribedAPI.setSubCreatedStatus(resultSet.getString("SUBS_CREATE_STATE"));
                subscribedAPI.setTier(new Tier(resultSet.getString("TIER_ID")));
                subscribedAPI.setApplication(application);
            }
            return subscribedAPI;
        } catch (SQLException e) {
            handleException("Failed to retrieve subscription from subscription id", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, resultSet);
        }
        return null;
    }

    /**
     * This method used tot get Subscriber from subscriberId.
     *
     * @param subscriberName id
     * @return Subscriber
     * @throws APIManagementException if failed to get Subscriber from subscriber id
     */
    public Subscriber getSubscriber(String subscriberName) throws APIManagementException {
        Connection conn = null;
        Subscriber subscriber = null;
        PreparedStatement ps = null;
        ResultSet result = null;

        int tenantId = APIUtil.getTenantId(subscriberName);

        String sqlQuery = SQLConstants.GET_TENANT_SUBSCRIBER_SQL;
        if (forceCaseInsensitiveComparisons) {
            sqlQuery = SQLConstants.GET_TENANT_SUBSCRIBER_CASE_INSENSITIVE_SQL;
        }

        try {
            conn = APIMgtDBUtil.getConnection();

            ps = conn.prepareStatement(sqlQuery);
            ps.setString(1, subscriberName);
            ps.setInt(2, tenantId);
            result = ps.executeQuery();

            if (result.next()) {
                subscriber = new Subscriber(result.getString(APIConstants.SUBSCRIBER_FIELD_EMAIL_ADDRESS));
                subscriber.setEmail(result.getString("EMAIL_ADDRESS"));
                subscriber.setId(result.getInt("SUBSCRIBER_ID"));
                subscriber.setName(subscriberName);
                subscriber.setSubscribedDate(result.getDate(APIConstants.SUBSCRIBER_FIELD_DATE_SUBSCRIBED));
                subscriber.setTenantId(result.getInt("TENANT_ID"));
            }
        } catch (SQLException e) {
            handleException("Failed to get Subscriber for :" + subscriberName, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, result);
        }
        return subscriber;
    }

    public Set<APIIdentifier> getAPIByConsumerKey(String accessToken) throws APIManagementException {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet result = null;

        String getAPISql = SQLConstants.GET_API_BY_CONSUMER_KEY_SQL;

        Set<APIIdentifier> apiSet = new HashSet<APIIdentifier>();
        try {
            connection = APIMgtDBUtil.getConnection();
            ps = connection.prepareStatement(getAPISql);
            String encryptedAccessToken = APIUtil.encryptToken(accessToken);
            ps.setString(1, encryptedAccessToken);
            result = ps.executeQuery();
            while (result.next()) {
                apiSet.add(new APIIdentifier(result.getString("API_PROVIDER"), result.getString("API_NAME"), result
                        .getString("API_VERSION")));
            }
        } catch (SQLException e) {
            handleException("Failed to get API ID for token: " + accessToken, e);
        } catch (CryptoException e) {
            handleException("Failed to get API ID for token: " + accessToken, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, connection, result);
        }
        return apiSet;
    }

    /**
     * This method returns the set of APIs for given subscriber, subscribed under the specified application.
     *
     * @param subscriber      subscriber
     * @param applicationName Application Name
     * @return Set<API>
     * @throws org.wso2.carbon.apimgt.api.APIManagementException if failed to get SubscribedAPIs
     */
    public Set<SubscribedAPI> getSubscribedAPIs(Subscriber subscriber, String applicationName, String groupingId)
            throws APIManagementException {
        Set<SubscribedAPI> subscribedAPIs = new LinkedHashSet<SubscribedAPI>();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet result = null;
        String sqlQuery = SQLConstants.GET_SUBSCRIBED_APIS_SQL;

        String whereClauseWithGroupId = " AND (APP.GROUP_ID = ? OR (APP.GROUP_ID = '' AND SUB.USER_ID = ?))";
        String whereClauseWithGroupIdorceCaseInsensitiveComp = " AND (APP.GROUP_ID = ? OR (APP.GROUP_ID = '' " + "AND" +
                                                               " LOWER(SUB.USER_ID) = LOWER(?)))";
        String whereClause = " AND SUB.USER_ID = ? ";
        String whereClauseCaseSensitive = " AND LOWER(SUB.USER_ID) = LOWER(?) ";

        try {
            connection = APIMgtDBUtil.getConnection();
            if (groupingId != null && !"null".equals(groupingId) && !groupingId.isEmpty()) {
                if (forceCaseInsensitiveComparisons) {
                    sqlQuery += whereClauseWithGroupIdorceCaseInsensitiveComp;
                } else {
                    sqlQuery += whereClauseWithGroupId;
                }
            } else {
                if (forceCaseInsensitiveComparisons) {
                    sqlQuery += whereClauseCaseSensitive;
                } else {
                    sqlQuery += whereClause;
                }
            }

            ps = connection.prepareStatement(sqlQuery);
            int tenantId = APIUtil.getTenantId(subscriber.getName());
            ps.setInt(1, tenantId);
            ps.setString(2, applicationName);

            if (groupingId != null && !"null".equals(groupingId) && !groupingId.isEmpty()) {
                ps.setString(3, groupingId);
                ps.setString(4, subscriber.getName());
            } else {
                ps.setString(3, subscriber.getName());
            }
            result = ps.executeQuery();

            while (result.next()) {
                APIIdentifier apiIdentifier = new APIIdentifier(APIUtil.replaceEmailDomain(result.getString
                        ("API_PROVIDER")), result.getString("API_NAME"), result.getString("API_VERSION"));

                SubscribedAPI subscribedAPI = new SubscribedAPI(subscriber, apiIdentifier);
                subscribedAPI.setSubscriptionId(result.getInt("SUBS_ID"));
                subscribedAPI.setSubStatus(result.getString("SUB_STATUS"));
                subscribedAPI.setSubCreatedStatus(result.getString("SUBS_CREATE_STATE"));
                subscribedAPI.setUUID(result.getString("SUB_UUID"));
                subscribedAPI.setTier(new Tier(result.getString(APIConstants.SUBSCRIPTION_FIELD_TIER_ID)));

                Application application = new Application(result.getString("APP_NAME"), subscriber);
                application.setUUID(result.getString("APP_UUID"));
                subscribedAPI.setApplication(application);
                subscribedAPIs.add(subscribedAPI);
            }
        } catch (SQLException e) {
            handleException("Failed to get SubscribedAPI of :" + subscriber.getName(), e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, connection, result);
        }
        return subscribedAPIs;
    }

    public Integer getSubscriptionCount(Subscriber subscriber, String applicationName, String groupingId)
            throws APIManagementException {
        Integer subscriptionCount = 0;
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet result = null;

        try {
            connection = APIMgtDBUtil.getConnection();

            String sqlQuery = SQLConstants.GET_SUBSCRIPTION_COUNT_SQL;
            if (forceCaseInsensitiveComparisons) {
                sqlQuery = SQLConstants.GET_SUBSCRIPTION_COUNT_CASE_INSENSITIVE_SQL;
            }

            String whereClauseWithGroupId = " AND APP.GROUP_ID = ? ";
            String whereClauseWithUserId = " AND SUB.USER_ID = ? ";
            String whereClauseCaseSensitive = " AND LOWER(SUB.USER_ID) = LOWER(?) ";
            String appIdentifier;

            if (groupingId != null && !"null".equals(groupingId) && !groupingId.isEmpty()) {
                sqlQuery += whereClauseWithGroupId;
                appIdentifier = groupingId;
            } else {
                appIdentifier = subscriber.getName();
                if (forceCaseInsensitiveComparisons) {
                    sqlQuery += whereClauseCaseSensitive;
                } else {
                    sqlQuery += whereClauseWithUserId;
                }
            }
            int tenantId = APIUtil.getTenantId(subscriber.getName());

            ps = connection.prepareStatement(sqlQuery);
            ps.setString(1, applicationName);
            ps.setInt(2, tenantId);
            ps.setString(3, appIdentifier);
            result = ps.executeQuery();

            while (result.next()) {
                subscriptionCount = result.getInt("SUB_COUNT");
            }
        } catch (SQLException e) {
            handleException("Failed to get SubscribedAPI of :" + subscriber.getName(), e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, connection, result);
        }
        return subscriptionCount;
    }


    /**
     * Gets the subscribed API's, by the group for the application.
     *
     * @param subscriber      the subscriber subscribing for the api
     * @param applicationName the application to which the api's are subscribed
     * @param startSubIndex   the start index for pagination
     * @param endSubIndex     end index for pagination
     * @param groupingId      the group id of the application
     * @return the set of subscribed API's.
     * @throws APIManagementException
     */
    public Set<SubscribedAPI> getPaginatedSubscribedAPIs(Subscriber subscriber, String applicationName,
                                                         int startSubIndex, int endSubIndex, String groupingId)
            throws APIManagementException {
        Set<SubscribedAPI> subscribedAPIs = new LinkedHashSet<SubscribedAPI>();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet result = null;

        String sqlQuery = SQLConstants.GET_PAGINATED_SUBSCRIBED_APIS_SQL;

        String whereClause = " AND  SUB.USER_ID = ? ";
        String whereClauseForceCaseInsensitiveComp = " AND LOWER(SUB.USER_ID) = LOWER(?)  ";
        String whereClauseWithGroupId = " AND (APP.GROUP_ID = ? OR (APP.GROUP_ID = '' AND SUB.USER_ID = ?))";
        String whereClauseWithGroupIdorceCaseInsensitiveComp = " AND (APP.GROUP_ID = ? OR (APP.GROUP_ID = '' " + "AND" +
                                                               " LOWER(SUB.USER_ID) = LOWER(?)))";
        try {
            connection = APIMgtDBUtil.getConnection();
            int tenantId = APIUtil.getTenantId(subscriber.getName());
            if (groupingId != null && !"null".equals(groupingId) && !groupingId.isEmpty()) {
                if (forceCaseInsensitiveComparisons) {
                    sqlQuery += whereClauseWithGroupIdorceCaseInsensitiveComp;
                } else {
                    sqlQuery += whereClauseWithGroupId;
                }

                ps = connection.prepareStatement(sqlQuery);
                ps.setInt(1, tenantId);
                ps.setString(2, applicationName);
                ps.setString(3, groupingId);
                ps.setString(4, subscriber.getName());
            } else {
                if (forceCaseInsensitiveComparisons) {
                    sqlQuery += whereClauseForceCaseInsensitiveComp;
                } else {
                    sqlQuery += whereClause;
                }

                ps = connection.prepareStatement(sqlQuery);
                ps.setInt(1, tenantId);
                ps.setString(2, applicationName);
                ps.setString(3, subscriber.getName());
            }
            result = ps.executeQuery();

            int index = 0;
            while (result.next()) {
                if (index >= startSubIndex && index < endSubIndex) {
                    APIIdentifier apiIdentifier = new APIIdentifier(APIUtil.replaceEmailDomain(result.getString
                            ("API_PROVIDER")), result.getString("API_NAME"), result.getString("API_VERSION"));

                    SubscribedAPI subscribedAPI = new SubscribedAPI(subscriber, apiIdentifier);
                    subscribedAPI.setSubStatus(result.getString("SUB_STATUS"));
                    subscribedAPI.setSubCreatedStatus(result.getString("SUBS_CREATE_STATE"));
                    subscribedAPI.setTier(new Tier(result.getString(APIConstants.SUBSCRIPTION_FIELD_TIER_ID)));

                    Application application = new Application(result.getString("APP_NAME"), subscriber);
                    subscribedAPI.setApplication(application);
                    subscribedAPIs.add(subscribedAPI);
                    if (index == endSubIndex - 1) {
                        break;
                    }
                }
                index++;
            }
        } catch (SQLException e) {
            handleException("Failed to get SubscribedAPI of :" + subscriber.getName(), e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, connection, result);
        }
        return subscribedAPIs;
    }

    /**
     * This method returns the set of APIs for given subscriber
     *
     * @param subscriber subscriber
     * @return Set<API>
     * @throws org.wso2.carbon.apimgt.api.APIManagementException if failed to get SubscribedAPIs
     */
    public Set<SubscribedAPI> getSubscribedAPIs(Subscriber subscriber, String groupingId)
            throws APIManagementException {
        Set<SubscribedAPI> subscribedAPIs = new LinkedHashSet<SubscribedAPI>();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet result = null;

        //identify subscribeduser used email/ordinalusername
        String subscribedUserName = getLoginUserName(subscriber.getName());
        subscriber.setName(subscribedUserName);

        String sqlQuery = SQLConstants.GET_SUBSCRIBED_APIS_OF_SUBSCRIBER_SQL;
        String whereClause = " AND  SUB.USER_ID = ? ";
        String whereClauseCaseInSensitive = " AND  LOWER(SUB.USER_ID) = LOWER(?) ";
        String whereClauseWithGroupId = " AND (APP.GROUP_ID = ? OR (APP.GROUP_ID = '' AND SUB.USER_ID = ?))";
        String whereClauseWithGroupIdorceCaseInsensitiveComp = " AND (APP.GROUP_ID = ? OR (APP.GROUP_ID = '' " + "AND" +
                                                               " LOWER(SUB.USER_ID) = LOWER(?)))";
        try {
            connection = APIMgtDBUtil.getConnection();

            if (groupingId != null && !"null".equals(groupingId) && !groupingId.isEmpty()) {
                if (forceCaseInsensitiveComparisons) {
                    sqlQuery += whereClauseWithGroupIdorceCaseInsensitiveComp;
                } else {
                    sqlQuery += whereClauseWithGroupId;
                }
            } else {
                if (forceCaseInsensitiveComparisons) {
                    sqlQuery += whereClauseCaseInSensitive;
                } else {
                    sqlQuery += whereClause;
                }
            }

            ps = connection.prepareStatement(sqlQuery);
            int tenantId = APIUtil.getTenantId(subscriber.getName());
            ps.setInt(1, tenantId);
            if (groupingId != null && !"null".equals(groupingId) && !groupingId.isEmpty()) {
                ps.setString(2, groupingId);
                ps.setString(3, subscriber.getName());
            } else {
                ps.setString(2, subscriber.getName());
            }

            result = ps.executeQuery();

            Map<String, Set<SubscribedAPI>> map = new TreeMap<String, Set<SubscribedAPI>>();
            LRUCache<Integer, Application> applicationCache = new LRUCache<Integer, Application>(100);

            while (result.next()) {
                APIIdentifier apiIdentifier = new APIIdentifier(APIUtil.replaceEmailDomain(result.getString
                        ("API_PROVIDER")), result.getString("API_NAME"), result.getString("API_VERSION"));

                SubscribedAPI subscribedAPI = new SubscribedAPI(subscriber, apiIdentifier);
                subscribedAPI.setSubscriptionId(result.getInt("SUBS_ID"));
                subscribedAPI.setSubStatus(result.getString("SUB_STATUS"));
                subscribedAPI.setSubCreatedStatus(result.getString("SUBS_CREATE_STATE"));
                String tierName = result.getString(APIConstants.SUBSCRIPTION_FIELD_TIER_ID);
                subscribedAPI.setTier(new Tier(tierName));
                subscribedAPI.setUUID(result.getString("SUB_UUID"));
                //setting NULL for subscriber. If needed, Subscriber object should be constructed &
                // passed in
                int applicationId = result.getInt("APP_ID");
                Application application = applicationCache.get(applicationId);
                if (application == null) {
                    application = new Application(result.getString("APP_NAME"), subscriber);
                    application.setId(result.getInt("APP_ID"));
                    application.setCallbackUrl(result.getString("CALLBACK_URL"));
                    application.setUUID(result.getString("APP_UUID"));
                    String tenantAwareUserId = subscriber.getName();
                    Set<APIKey> keys = getApplicationKeys(tenantAwareUserId, applicationId);
                    for (APIKey key : keys) {
                        application.addKey(key);
                    }
                    Map<String, OAuthApplicationInfo> oauthApps = getOAuthApplications(applicationId);

                    for (Map.Entry<String, OAuthApplicationInfo> entry : oauthApps.entrySet()) {
                        application.addOAuthApp(entry.getKey(), entry.getValue());
                    }

                    applicationCache.put(applicationId, application);
                }
                subscribedAPI.setApplication(application);

                int subscriptionId = result.getInt("SUBS_ID");
                Set<APIKey> apiKeys = getAPIKeysBySubscription(subscriptionId);
                for (APIKey key : apiKeys) {
                    subscribedAPI.addKey(key);
                }

                if (!map.containsKey(application.getName())) {
                    map.put(application.getName(), new TreeSet<SubscribedAPI>(new Comparator<SubscribedAPI>() {
                        public int compare(SubscribedAPI o1, SubscribedAPI o2) {
                            int placement = o1.getApiId().getApiName().compareTo(o2.getApiId().getApiName());
                            if (placement == 0) {
                                return new APIVersionComparator().compare(new API(o1.getApiId()), new API(o2.getApiId
                                        ()));
                            }
                            return placement;
                        }
                    }));
                }
                map.get(application.getName()).add(subscribedAPI);
            }

            for (Map.Entry<String, Set<SubscribedAPI>> entry : map.entrySet()) {
                subscribedAPIs.addAll(entry.getValue());
            }
        } catch (SQLException e) {
            handleException("Failed to get SubscribedAPI of :" + subscriber.getName(), e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, connection, result);
        }
        return subscribedAPIs;
    }

    private Set<APIKey> getAPIKeysBySubscription(int subscriptionId) throws APIManagementException {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet result = null;

        String getKeysSql = SQLConstants.GET_API_KEY_BY_SUBSCRIPTION_SQL;
        Set<APIKey> apiKeys = new HashSet<APIKey>();
        try {
            connection = APIMgtDBUtil.getConnection();
            ps = connection.prepareStatement(getKeysSql);
            ps.setInt(1, subscriptionId);
            result = ps.executeQuery();
            while (result.next()) {
                APIKey apiKey = new APIKey();
                String decryptedAccessToken = APIUtil.decryptToken(result.getString("ACCESS_TOKEN"));
                apiKey.setAccessToken(decryptedAccessToken);
                apiKey.setType(result.getString("TOKEN_TYPE"));
                apiKeys.add(apiKey);
            }
        } catch (SQLException e) {
            handleException("Failed to get API keys for subscription: " + subscriptionId, e);
        } catch (CryptoException e) {
            handleException("Failed to get API keys for subscription: " + subscriptionId, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, connection, result);
        }
        return apiKeys;
    }

    public String getTokenScope(String consumerKey) throws APIManagementException {
        String tokenScope = null;

        if (APIUtil.checkAccessTokenPartitioningEnabled() && APIUtil.checkUserNameAssertionEnabled()) {
            String[] keyStoreTables = APIUtil.getAvailableKeyStoreTables();
            if (keyStoreTables != null) {
                for (String keyStoreTable : keyStoreTables) {
                    tokenScope = getTokenScope(consumerKey, getScopeSql(keyStoreTable));
                    if (tokenScope != null) {
                        break;
                    }
                }
            }
        } else {
            tokenScope = getTokenScope(consumerKey, getScopeSql(null));
        }
        return tokenScope;
    }

    private String getTokenScope(String consumerKey, String getScopeSql) throws APIManagementException {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet result = null;
        String tokenScope = null;

        try {

            connection = APIMgtDBUtil.getConnection();
            ps = connection.prepareStatement(getScopeSql);
            ps.setString(1, consumerKey);
            result = ps.executeQuery();
            ArrayList<String> scopes = new ArrayList<String>();
            while (result.next()) {
                scopes.add(result.getString(APIConstants.IDENTITY_OAUTH2_FIELD_TOKEN_SCOPE));
            }
            tokenScope = getScopeString(scopes);
        } catch (SQLException e) {
            handleException("Failed to get token scope from consumer key: " + consumerKey, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, connection, result);
        }
        return tokenScope;
    }

    private String getScopeSql(String accessTokenStoreTable) {
        String tokenStoreTable = APIConstants.ACCESS_TOKEN_STORE_TABLE;
        String scopeAssociationTable = APIConstants.TOKEN_SCOPE_ASSOCIATION_TABLE;
        if (accessTokenStoreTable != null) {
            tokenStoreTable = accessTokenStoreTable;
        }

        //TODO : this wont work due to 'IAT.AUTHZ_USER = ICA.USERNAME' changes
        return "SELECT" +
               " ISAT.TOKEN_SCOPE AS TOKEN_SCOPE " +
               "FROM " +
               tokenStoreTable + " IAT, " +
               scopeAssociationTable + " ISAT, " +
               " IDN_OAUTH_CONSUMER_APPS ICA " +
               "WHERE" +
               " ICA.CONSUMER_KEY = ?" +
               " AND IAT.CONSUMER_KEY_ID = ICA.ID" +
               " AND IAT.TOKEN_ID = ISAT.TOKEN_ID " +
               " AND IAT.AUTHZ_USER = ICA.USERNAME " +
               " AND IAT.USER_DOMAIN = ICA.USER_DOMAIN";
    }

    public String getScopesByToken(String accessToken) throws APIManagementException {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet result = null;
        String tokenScope = null;

        String getScopeSql = SQLConstants.GET_SCOPE_BY_TOKEN_SQL;
        try {
            connection = APIMgtDBUtil.getConnection();
            ps = connection.prepareStatement(getScopeSql);
            ps.setString(1, accessToken);
            result = ps.executeQuery();
            List<String> scopes = new ArrayList<String>();
            while (result.next()) {
                scopes.add(result.getString(APIConstants.IDENTITY_OAUTH2_FIELD_TOKEN_SCOPE));
            }
            tokenScope = getScopeString(scopes);
        } catch (SQLException e) {
            handleException("Failed to get token scope from access token : " + accessToken, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, connection, result);
        }
        return tokenScope;
    }

    public boolean isAccessTokenExists(String accessToken) throws APIManagementException {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet result = null;

        String accessTokenStoreTable = APIConstants.ACCESS_TOKEN_STORE_TABLE;
        accessTokenStoreTable = getAccessTokenStoreTableFromAccessToken(accessToken, accessTokenStoreTable);

        String getTokenSql = SQLConstants.IS_ACCESS_TOKEN_EXISTS_PREFIX + accessTokenStoreTable +
                             SQLConstants.IS_ACCESS_TOKEN_EXISTS_SUFFIX;
        boolean tokenExists = false;
        try {
            connection = APIMgtDBUtil.getConnection();
            ps = connection.prepareStatement(getTokenSql);
            String encryptedAccessToken = APIUtil.encryptToken(accessToken);
            ps.setString(1, encryptedAccessToken);
            result = ps.executeQuery();
            while (result.next()) {
                tokenExists = true;
            }
        } catch (SQLException e) {
            handleException("Failed to check availability of the access token. ", e);
        } catch (CryptoException e) {
            handleException("Failed to check availability of the access token. ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, connection, result);
        }
        return tokenExists;
    }

    public boolean isAccessTokenRevoked(String accessToken) throws APIManagementException {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet result = null;

        String accessTokenStoreTable = APIConstants.ACCESS_TOKEN_STORE_TABLE;
        accessTokenStoreTable = getAccessTokenStoreTableFromAccessToken(accessToken, accessTokenStoreTable);

        String getTokenSql = SQLConstants.IS_ACCESS_TOKEN_REVOKED_PREFIX + accessTokenStoreTable +
                             SQLConstants.IS_ACCESS_TOKE_REVOKED_SUFFIX;
        boolean tokenExists = false;
        try {
            connection = APIMgtDBUtil.getConnection();
            ps = connection.prepareStatement(getTokenSql);
            String encryptedAccessToken = APIUtil.encryptToken(accessToken);
            ps.setString(1, encryptedAccessToken);
            result = ps.executeQuery();
            while (result.next()) {
                if (!"REVOKED".equals(result.getString("TOKEN_STATE"))) {
                    tokenExists = true;
                }
            }
        } catch (SQLException e) {
            handleException("Failed to check availability of the access token. ", e);
        } catch (CryptoException e) {
            handleException("Failed to check availability of the access token. ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, connection, result);
        }
        return tokenExists;
    }

    public APIKey getAccessTokenData(String accessToken) throws APIManagementException {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet result = null;
        APIKey apiKey = new APIKey();

        String accessTokenStoreTable = APIConstants.ACCESS_TOKEN_STORE_TABLE;
        accessTokenStoreTable = getAccessTokenStoreTableFromAccessToken(accessToken, accessTokenStoreTable);

        String getTokenSql = SQLConstants.GET_ACCESS_TOKEN_DATA_PREFIX + accessTokenStoreTable + SQLConstants
                .GET_ACCESS_TOKEN_DATA_SUFFIX;
        try {
            connection = APIMgtDBUtil.getConnection();
            ps = connection.prepareStatement(getTokenSql);
            ps.setString(1, APIUtil.encryptToken(accessToken));
            result = ps.executeQuery();
            if (result.next()) {
                String decryptedAccessToken = APIUtil.decryptToken(result.getString("ACCESS_TOKEN")); // todo - check

                String endUserName = result.getString(APIConstants.IDENTITY_OAUTH2_FIELD_AUTHORIZED_USER);
                String domainName = result.getString(APIConstants.IDENTITY_OAUTH2_FIELD_USER_DOMAIN);
                String endUsernameWithDomain = UserCoreUtil.addDomainToName(endUserName, domainName);
                apiKey.setAuthUser(endUsernameWithDomain);

                apiKey.setAccessToken(decryptedAccessToken);
                apiKey.setCreatedDate(result.getTimestamp("TIME_CREATED").toString().split("\\.")[0]);
                String consumerKey = result.getString("CONSUMER_KEY");
                apiKey.setConsumerKey(consumerKey);
                apiKey.setValidityPeriod(result.getLong("VALIDITY_PERIOD"));
                List<String> scopes = new ArrayList<String>();
                do {
                    scopes.add(result.getString(APIConstants.IDENTITY_OAUTH2_FIELD_TOKEN_SCOPE));
                } while (result.next());
                apiKey.setTokenScope(getScopeString(scopes));
            }
        } catch (SQLException e) {
            handleException("Failed to get the access token data. ", e);
        } catch (CryptoException e) {
            handleException("Failed to get the access token data. ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, connection, result);
        }
        return apiKey;
    }

    public Map<Integer, APIKey> getAccessTokens(String query) throws APIManagementException {
        Map<Integer, APIKey> tokenDataMap = new HashMap<Integer, APIKey>();
        if (APIUtil.checkAccessTokenPartitioningEnabled() && APIUtil.checkUserNameAssertionEnabled()) {
            String[] keyStoreTables = APIUtil.getAvailableKeyStoreTables();
            if (keyStoreTables != null) {
                for (String keyStoreTable : keyStoreTables) {
                    Map<Integer, APIKey> tokenDataMapTmp = getAccessTokens(query, getTokenSql(keyStoreTable));
                    tokenDataMap.putAll(tokenDataMapTmp);
                }
            }
        } else {
            tokenDataMap = getAccessTokens(query, getTokenSql(null));
        }
        return tokenDataMap;
    }

    private Map<Integer, APIKey> getAccessTokens(String query, String getTokenSql) throws APIManagementException {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet result = null;
        Map<Integer, APIKey> tokenDataMap = new HashMap<Integer, APIKey>();

        try {
            connection = APIMgtDBUtil.getConnection();
            ps = connection.prepareStatement(getTokenSql);
            result = ps.executeQuery();
            boolean accessTokenRowBreaker = false;

            Integer i = 0;
            while (accessTokenRowBreaker || result.next()) {
                accessTokenRowBreaker = false;
                String accessToken = APIUtil.decryptToken(result.getString("ACCESS_TOKEN"));
                String regex = "(?i)[a-zA-Z0-9_.-|]*" + query.trim() + "(?i)[a-zA-Z0-9_.-|]*";
                Pattern pattern;
                Matcher matcher;
                pattern = Pattern.compile(regex);
                matcher = pattern.matcher(accessToken);
                if (matcher.matches()) {
                    APIKey apiKey = new APIKey();
                    apiKey.setAccessToken(accessToken);

                    String username = result.getString(APIConstants.IDENTITY_OAUTH2_FIELD_AUTHORIZED_USER);
                    String domainName = result.getString(APIConstants.IDENTITY_OAUTH2_FIELD_USER_DOMAIN);
                    String endUsernameWithDomain = UserCoreUtil.addDomainToName(username, domainName);
                    apiKey.setAuthUser(endUsernameWithDomain);

                    apiKey.setCreatedDate(result.getTimestamp("TIME_CREATED").toString().split("\\.")[0]);
                    String consumerKey = result.getString("CONSUMER_KEY");
                    apiKey.setConsumerKey(consumerKey);
                    apiKey.setValidityPeriod(result.getLong("VALIDITY_PERIOD"));
                    // Load all the rows to in memory and build the scope string
                    List<String> scopes = new ArrayList<String>();
                    String tokenString = result.getString("ACCESS_TOKEN");
                    do {
                        String currentRowTokenString = result.getString("ACCESS_TOKEN");
                        if (tokenString.equals(currentRowTokenString)) {
                            scopes.add(result.getString(APIConstants.IDENTITY_OAUTH2_FIELD_TOKEN_SCOPE));
                        } else {
                            accessTokenRowBreaker = true;
                            break;
                        }
                    } while (result.next());
                    apiKey.setTokenScope(getScopeString(scopes));
                    tokenDataMap.put(i, apiKey);
                    i++;
                }
            }
        } catch (SQLException e) {
            handleException("Failed to get access token data. ", e);
        } catch (CryptoException e) {
            handleException("Failed to get access token data. ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, connection, result);
        }
        return tokenDataMap;
    }

    private String getTokenSql(String accessTokenStoreTable) {
        String tokenStoreTable = APIConstants.ACCESS_TOKEN_STORE_TABLE;
        if (accessTokenStoreTable != null) {
            tokenStoreTable = accessTokenStoreTable;
        }
        return SQLConstants.GET_TOKEN_SQL_PREFIX + tokenStoreTable + SQLConstants.GET_TOKEN_SQL_SUFFIX;
    }

    public Map<Integer, APIKey> getAccessTokensByUser(String user, String loggedInUser) throws APIManagementException {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet result = null;
        Map<Integer, APIKey> tokenDataMap = new HashMap<Integer, APIKey>();

        String accessTokenStoreTable = APIConstants.ACCESS_TOKEN_STORE_TABLE;
        accessTokenStoreTable = getAccessTokenStoreTableNameOfUserId(user, accessTokenStoreTable);

        String getTokenSql = SQLConstants.GET_ACCESS_TOKEN_BY_USER_PREFIX + accessTokenStoreTable + SQLConstants
                .GET_ACCESS_TOKEN_BY_USER_SUFFIX;
        try {
            connection = APIMgtDBUtil.getConnection();
            ps = connection.prepareStatement(getTokenSql);
            ps.setString(1, user);
            result = ps.executeQuery();
            Integer i = 0;
            boolean accessTokenRowBreaker = false;
            while (accessTokenRowBreaker || result.next()) {
                accessTokenRowBreaker = false;
                String username = result.getString(APIConstants.IDENTITY_OAUTH2_FIELD_AUTHORIZED_USER);
                String domainName = result.getString(APIConstants.IDENTITY_OAUTH2_FIELD_USER_DOMAIN);
                String authorizedUserWithDomain = UserCoreUtil.addDomainToName(username, domainName);

                if (APIUtil.isLoggedInUserAuthorizedToRevokeToken(loggedInUser, authorizedUserWithDomain)) {
                    String accessToken = APIUtil.decryptToken(result.getString("ACCESS_TOKEN"));
                    APIKey apiKey = new APIKey();
                    apiKey.setAccessToken(accessToken);
                    apiKey.setAuthUser(authorizedUserWithDomain);
                    apiKey.setCreatedDate(result.getTimestamp("TIME_CREATED").toString().split("\\.")[0]);
                    String consumerKey = result.getString("CONSUMER_KEY");
                    apiKey.setConsumerKey(consumerKey);
                    apiKey.setValidityPeriod(result.getLong("VALIDITY_PERIOD"));
                    // Load all the rows to in memory and build the scope string
                    List<String> scopes = new ArrayList<String>();
                    String tokenString = result.getString("ACCESS_TOKEN");
                    do {
                        String currentRowTokenString = result.getString("ACCESS_TOKEN");
                        if (tokenString.equals(currentRowTokenString)) {
                            scopes.add(result.getString(APIConstants.IDENTITY_OAUTH2_FIELD_TOKEN_SCOPE));
                        } else {
                            accessTokenRowBreaker = true;
                            break;
                        }
                    } while (result.next());
                    apiKey.setTokenScope(getScopeString(scopes));
                    tokenDataMap.put(i, apiKey);
                    i++;
                }
            }
        } catch (SQLException e) {
            handleException("Failed to get access token data. ", e);
        } catch (CryptoException e) {
            handleException("Failed to get access token data. ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, connection, result);
        }
        return tokenDataMap;
    }

    public Map<Integer, APIKey> getAccessTokensByDate(String date, boolean latest, String loggedInUser)
            throws APIManagementException {
        Map<Integer, APIKey> tokenDataMap = new HashMap<Integer, APIKey>();

        if (APIUtil.checkAccessTokenPartitioningEnabled() && APIUtil.checkUserNameAssertionEnabled()) {
            String[] keyStoreTables = APIUtil.getAvailableKeyStoreTables();
            if (keyStoreTables != null) {
                for (String keyStoreTable : keyStoreTables) {
                    Map<Integer, APIKey> tokenDataMapTmp = getAccessTokensByDate(date, latest, getTokenByDateSqls
                            (keyStoreTable), loggedInUser);
                    tokenDataMap.putAll(tokenDataMapTmp);
                }
            }
        } else {
            tokenDataMap = getAccessTokensByDate(date, latest, getTokenByDateSqls(null), loggedInUser);
        }
        return tokenDataMap;
    }

    public Map<Integer, APIKey> getAccessTokensByDate(String date, boolean latest, String[] querySql,
                                                      String loggedInUser) throws APIManagementException {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet result = null;
        Map<Integer, APIKey> tokenDataMap = new HashMap<Integer, APIKey>();

        try {
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            java.util.Date searchDate = fmt.parse(date);
            Date sqlDate = new Date(searchDate.getTime());
            connection = APIMgtDBUtil.getConnection();
            if (latest) {
                ps = connection.prepareStatement(querySql[0]);
            } else {
                ps = connection.prepareStatement(querySql[1]);
            }
            ps.setDate(1, sqlDate);

            result = ps.executeQuery();
            Integer i = 0;
            boolean accessTokenRowBreaker = false;
            while (accessTokenRowBreaker || result.next()) {
                accessTokenRowBreaker = true;

                String username = result.getString(APIConstants.IDENTITY_OAUTH2_FIELD_AUTHORIZED_USER);
                String domainName = result.getString(APIConstants.IDENTITY_OAUTH2_FIELD_USER_DOMAIN);
                String authorizedUserWithDomain = UserCoreUtil.addDomainToName(username, domainName);

                if (APIUtil.isLoggedInUserAuthorizedToRevokeToken(loggedInUser, authorizedUserWithDomain)) {
                    String accessToken = APIUtil.decryptToken(result.getString("ACCESS_TOKEN"));
                    APIKey apiKey = new APIKey();
                    apiKey.setAccessToken(accessToken);
                    apiKey.setAuthUser(authorizedUserWithDomain);
                    apiKey.setCreatedDate(result.getTimestamp("TIME_CREATED").toString().split("\\.")[0]);
                    String consumerKey = result.getString("CONSUMER_KEY");
                    apiKey.setConsumerKey(consumerKey);
                    apiKey.setValidityPeriod(result.getLong("VALIDITY_PERIOD"));
                    // Load all the rows to in memory and build the scope string
                    List<String> scopes = new ArrayList<String>();
                    String tokenString = result.getString("ACCESS_TOKEN");
                    do {
                        String currentRowTokenString = result.getString("ACCESS_TOKEN");
                        if (tokenString.equals(currentRowTokenString)) {
                            scopes.add(result.getString(APIConstants.IDENTITY_OAUTH2_FIELD_TOKEN_SCOPE));
                        } else {
                            accessTokenRowBreaker = true;
                            break;
                        }
                    } while (result.next());
                    apiKey.setTokenScope(getScopeString(scopes));
                    tokenDataMap.put(i, apiKey);
                    i++;
                }
            }
        } catch (SQLException e) {
            handleException("Failed to get access token data. ", e);
        } catch (ParseException e) {
            handleException("Failed to get access token data. ", e);
        } catch (CryptoException e) {
            handleException("Failed to get access token data. ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, connection, result);
        }
        return tokenDataMap;
    }

    public String[] getTokenByDateSqls(String accessTokenStoreTable) {
        String[] querySqlArr = new String[2];
        String tokenStoreTable = APIConstants.ACCESS_TOKEN_STORE_TABLE;
        if (accessTokenStoreTable != null) {
            tokenStoreTable = accessTokenStoreTable;
        }

        querySqlArr[0] = SQLConstants.GET_TOKEN_BY_DATE_PREFIX + tokenStoreTable + SQLConstants
                .GET_TOKEN_BY_DATE_AFTER_SUFFIX;

        querySqlArr[1] = SQLConstants.GET_TOKEN_BY_DATE_PREFIX + tokenStoreTable + SQLConstants
                .GET_TOKEN_BY_DATE_BEFORE_SUFFIX;
        return querySqlArr;
    }

    private Set<APIKey> getApplicationKeys(String username, int applicationId) throws APIManagementException {

        String accessTokenStoreTable = APIConstants.ACCESS_TOKEN_STORE_TABLE;
        accessTokenStoreTable = getAccessTokenStoreTableNameOfUserId(username, accessTokenStoreTable);

        Set<APIKey> apiKeys = new HashSet<APIKey>();

        try {
            APIKey productionKey = getProductionKeyOfApplication(applicationId, accessTokenStoreTable);
            if (productionKey != null) {
                apiKeys.add(productionKey);
            } else {
                productionKey = getKeyStatusOfApplication(APIConstants.API_KEY_TYPE_PRODUCTION, applicationId);
                if (productionKey != null) {
                    productionKey.setType(APIConstants.API_KEY_TYPE_PRODUCTION);
                    apiKeys.add(productionKey);
                }
            }

            APIKey sandboxKey = getSandboxKeyOfApplication(applicationId, accessTokenStoreTable);
            if (sandboxKey != null) {
                apiKeys.add(sandboxKey);
            } else {
                sandboxKey = getKeyStatusOfApplication(APIConstants.API_KEY_TYPE_SANDBOX, applicationId);
                if (sandboxKey != null) {
                    sandboxKey.setType(APIConstants.API_KEY_TYPE_SANDBOX);
                    apiKeys.add(sandboxKey);
                }
            }
        } catch (SQLException e) {
            handleException("Failed to get keys for application: " + applicationId, e);
        } catch (CryptoException e) {
            handleException("Failed to get keys for application: " + applicationId, e);
        }
        return apiKeys;
    }

    private Map<String, OAuthApplicationInfo> getOAuthApplications(int applicationId) throws APIManagementException {
        Map<String, OAuthApplicationInfo> map = new HashMap<String, OAuthApplicationInfo>();
        OAuthApplicationInfo prodApp = getClientOfApplication(applicationId, "PRODUCTION");
        if (prodApp != null) {
            map.put("PRODUCTION", prodApp);
        }

        OAuthApplicationInfo sandboxApp = getClientOfApplication(applicationId, "SANDBOX");
        if (sandboxApp != null) {
            map.put("SANDBOX", sandboxApp);
        }

        return map;
    }

    public OAuthApplicationInfo getClientOfApplication(int applicationID, String keyType)
            throws APIManagementException {
        String sqlQuery = SQLConstants.GET_CLIENT_OF_APPLICATION_SQL;

        KeyManager keyManager = null;
        OAuthApplicationInfo oAuthApplication = null;
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String consumerKey = null;
        try {
            connection = APIMgtDBUtil.getConnection();
            ps = connection.prepareStatement(sqlQuery);
            ps.setInt(1, applicationID);
            ps.setString(2, keyType);
            rs = ps.executeQuery();

            while (rs.next()) {
                consumerKey = rs.getString(1);
            }

            if (consumerKey != null) {
                keyManager = KeyManagerHolder.getKeyManagerInstance();
                oAuthApplication = keyManager.retrieveApplication(consumerKey);
            }
        } catch (SQLException e) {
            handleException("Failed to get  client of application. SQL error", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, connection, rs);
        }

        return oAuthApplication;
    }

    private APIKey getKeyStatusOfApplication(String keyType, int applicationId) throws APIManagementException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        APIKey key = null;

        String sqlQuery = SQLConstants.GET_KEY_STATUS_OF_APPLICATION_SQL;
        try {
            connection = APIMgtDBUtil.getConnection();
            preparedStatement = connection.prepareStatement(sqlQuery);
            preparedStatement.setInt(1, applicationId);
            preparedStatement.setString(2, keyType);

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                key = new APIKey();
                key.setState(resultSet.getString("STATE"));
            }
        } catch (SQLException e) {
            handleException("Error occurred while getting the State of Access Token", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(preparedStatement, connection, resultSet);
        }
        return key;
    }

    /**
     * Gets ConsumerKeys when given the Application ID.
     *
     * @param applicationId
     * @return {@link java.util.Set} containing ConsumerKeys
     * @throws APIManagementException
     */
    public Set<String> getConsumerKeysOfApplication(int applicationId) throws APIManagementException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        Set<String> consumerKeys = new HashSet<String>();

        String sqlQuery = SQLConstants.GET_CONSUMER_KEYS_OF_APPLICATION_SQL;
        try {
            connection = APIMgtDBUtil.getConnection();
            preparedStatement = connection.prepareStatement(sqlQuery);
            preparedStatement.setInt(1, applicationId);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String consumerKey = resultSet.getString("CONSUMER_KEY");
                if (consumerKey != null) {
                    consumerKeys.add(consumerKey);
                }
            }
        } catch (SQLException e) {
            handleException("Error occurred while getting the State of Access Token", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(preparedStatement, connection, resultSet);
        }

        return consumerKeys;
    }

    private APIKey getProductionKeyOfApplication(int applicationId, String accessTokenStoreTable)
            throws SQLException, CryptoException, APIManagementException {

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        //The part of the sql query that remain common across databases.
        String statement = SQLConstants.GET_PRODUCTION_KEYS_OF_APPLICATION_PREFIX +
                           accessTokenStoreTable + SQLConstants.GET_PRODUCTION_KEYS_OF_APPLICATION_SUFFIX;


        String oracleSQL = SQLConstants.GET_PRODUCTION_KEYS_OF_APPLICATION_ORACLE_PREFIX +
                           accessTokenStoreTable + SQLConstants.GET_PRODUCTION_KEYS_OF_APPLICATION_ORACLE_SUFFIX;

        String mySQL = "SELECT" + statement;//+ " LIMIT 1";
        String db2SQL = "SELECT" + statement; //+ " FETCH FIRST 1 ROWS ONLY";
        String msSQL = "SELECT " + statement;
        String postgreSQL = "SELECT * FROM (SELECT" + statement + ") AS TOKEN";

        String authorizedDomains;
        String accessToken;
        String sql;

        try {
            connection = APIMgtDBUtil.getConnection();

            if (connection.getMetaData().getDriverName().contains("MySQL") || connection.getMetaData().getDriverName
                    ().contains("H2")) {
                sql = mySQL;
            } else if (connection.getMetaData().getDatabaseProductName().contains("DB2")) {
                sql = db2SQL;
            } else if (connection.getMetaData().getDriverName().contains("MS SQL")) {
                sql = msSQL;
            } else if (connection.getMetaData().getDriverName().contains("Microsoft")) {
                sql = msSQL;
            } else if (connection.getMetaData().getDriverName().contains("PostgreSQL")) {
                sql = postgreSQL;
            } else {
                sql = oracleSQL;
            }

            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, applicationId);
            preparedStatement.setString(2, APIConstants.ACCESS_TOKEN_USER_TYPE_APPLICATION);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                APIKey apiKey = new APIKey();
                accessToken = APIUtil.decryptToken(resultSet.getString("ACCESS_TOKEN"));
                String consumerKey = resultSet.getString("CONSUMER_KEY");
                apiKey.setConsumerKey(consumerKey);
                String consumerSecret = resultSet.getString("CONSUMER_SECRET");
                apiKey.setConsumerSecret(APIUtil.decryptToken(consumerSecret));
                apiKey.setAccessToken(accessToken);

                //authorizedDomains = getAuthorizedDomains(accessToken);
                apiKey.setType(resultSet.getString("TOKEN_TYPE"));
                //apiKey.setAuthorizedDomains(authorizedDomains);
                apiKey.setValidityPeriod(resultSet.getLong("VALIDITY_PERIOD") / 1000);
                apiKey.setState(resultSet.getString("STATE"));

                // Load all the rows to in memory and build the scope string
                List<String> scopes = new ArrayList<String>();
                String tokenString = resultSet.getString("ACCESS_TOKEN");

                do {
                    String currentRowTokenString = resultSet.getString("ACCESS_TOKEN");
                    if (tokenString.equals(currentRowTokenString)) {
                        scopes.add(resultSet.getString(APIConstants.IDENTITY_OAUTH2_FIELD_TOKEN_SCOPE));
                    }
                } while (resultSet.next());
                apiKey.setTokenScope(getScopeString(scopes));
                return apiKey;
            }
            return null;
        } finally {
            APIMgtDBUtil.closeAllConnections(preparedStatement, connection, resultSet);
        }
    }

    private APIKey getSandboxKeyOfApplication(int applicationId, String accessTokenStoreTable)
            throws SQLException, CryptoException, APIManagementException {

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        //The part of the sql query that remain common across databases.
        String statement = SQLConstants.GET_SANDBOX_KEYS_OF_APPLICATION_PREFIX +
                           accessTokenStoreTable + SQLConstants.GET_SANDBOX_KEYS_OF_APPLICATION_SUFFIX;

        //Construct database specific sql statements.
        String oracleSQL = SQLConstants.GET_SANDBOX_KEYS_OF_APPLICATION_ORACLE_PREFIX +
                           accessTokenStoreTable + SQLConstants.GET_SANDBOX_KEYS_OF_APPLICATION_ORACLE_SUFFIX;

        String mySQL = "SELECT" + statement;// + " LIMIT 1";
        String db2SQL = "SELECT" + statement;// + " FETCH FIRST 1 ROWS ONLY";
        String msSQL = "SELECT " + statement;
        String postgreSQL = "SELECT * FROM (SELECT" + statement + ") AS TOKEN";

        String authorizedDomains;
        String accessToken;
        String sql;
        try {
            connection = APIMgtDBUtil.getConnection();

            if (connection.getMetaData().getDriverName().contains("MySQL") || connection.getMetaData().getDriverName
                    ().contains("H2")) {
                sql = mySQL;
            } else if (connection.getMetaData().getDatabaseProductName().contains("DB2")) {
                sql = db2SQL;
            } else if (connection.getMetaData().getDriverName().contains("MS SQL")) {
                sql = msSQL;
            } else if (connection.getMetaData().getDriverName().contains("Microsoft")) {
                sql = msSQL;
            } else if (connection.getMetaData().getDriverName().contains("PostgreSQL")) {
                sql = postgreSQL;
            } else {
                sql = oracleSQL;
            }

            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, applicationId);
            preparedStatement.setString(2, APIConstants.ACCESS_TOKEN_USER_TYPE_APPLICATION);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                APIKey apiKey = new APIKey();
                accessToken = APIUtil.decryptToken(resultSet.getString("ACCESS_TOKEN"));
                String consumerKey = resultSet.getString("CONSUMER_KEY");
                apiKey.setConsumerKey(consumerKey);
                String consumerSecret = resultSet.getString("CONSUMER_SECRET");
                apiKey.setConsumerSecret(APIUtil.decryptToken(consumerSecret));
                apiKey.setAccessToken(accessToken);
                apiKey.setType(resultSet.getString("TOKEN_TYPE"));
                apiKey.setValidityPeriod(resultSet.getLong("VALIDITY_PERIOD") / 1000);

                // Load all the rows to in memory and build the scope string
                List<String> scopes = new ArrayList<String>();
                String tokenString = resultSet.getString("ACCESS_TOKEN");

                do {
                    String currentRowTokenString = resultSet.getString("ACCESS_TOKEN");
                    if (tokenString.equals(currentRowTokenString)) {
                        scopes.add(resultSet.getString(APIConstants.IDENTITY_OAUTH2_FIELD_TOKEN_SCOPE));
                    }
                } while (resultSet.next());
                apiKey.setTokenScope(getScopeString(scopes));
                return apiKey;
            }
            return null;
        } finally {
            APIMgtDBUtil.closeAllConnections(preparedStatement, connection, resultSet);
        }
    }

    public Set<String> getApplicationKeys(int applicationId) throws APIManagementException {
        Set<String> apiKeys = new HashSet<String>();
        if (APIUtil.checkAccessTokenPartitioningEnabled() && APIUtil.checkUserNameAssertionEnabled()) {
            String[] keyStoreTables = APIUtil.getAvailableKeyStoreTables();
            if (keyStoreTables != null) {
                for (String keyStoreTable : keyStoreTables) {
                    apiKeys = getApplicationKeys(applicationId, getKeysSql(keyStoreTable));
                    if (apiKeys.size() > 0) {
                        break;
                    }
                }
            }
        } else {
            apiKeys = getApplicationKeys(applicationId, getKeysSql(null));
        }
        return apiKeys;
    }

    public void updateTierPermissions(String tierName, String permissionType, String roles, int tenantId)
            throws APIManagementException {
        Connection conn = null;
        PreparedStatement ps = null;
        PreparedStatement insertOrUpdatePS = null;
        ResultSet resultSet = null;
        int tierPermissionId = -1;

        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);

            String getTierPermissionQuery = SQLConstants.GET_TIER_PERMISSION_ID_SQL;
            ps = conn.prepareStatement(getTierPermissionQuery);
            ps.setString(1, tierName);
            ps.setInt(2, tenantId);
            resultSet = ps.executeQuery();
            if (resultSet.next()) {
                tierPermissionId = resultSet.getInt("TIER_PERMISSIONS_ID");
            }

            if (tierPermissionId == -1) {
                String query = SQLConstants.ADD_TIER_PERMISSION_SQL;
                insertOrUpdatePS = conn.prepareStatement(query);
                insertOrUpdatePS.setString(1, tierName);
                insertOrUpdatePS.setString(2, permissionType);
                insertOrUpdatePS.setString(3, roles);
                insertOrUpdatePS.setInt(4, tenantId);
                insertOrUpdatePS.execute();
            } else {
                String query = SQLConstants.UPDATE_TIER_PERMISSION_SQL;
                insertOrUpdatePS = conn.prepareStatement(query);
                insertOrUpdatePS.setString(1, tierName);
                insertOrUpdatePS.setString(2, permissionType);
                insertOrUpdatePS.setString(3, roles);
                insertOrUpdatePS.setInt(4, tierPermissionId);
                insertOrUpdatePS.setInt(5, tenantId);
                insertOrUpdatePS.executeUpdate();
            }
            conn.commit();
        } catch (SQLException e) {
            handleException("Error in updating tier permissions: " + e.getMessage(), e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, resultSet);
            APIMgtDBUtil.closeAllConnections(insertOrUpdatePS, null, null);
        }
    }

    public Set<TierPermissionDTO> getTierPermissions(int tenantId) throws APIManagementException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet resultSet = null;

        Set<TierPermissionDTO> tierPermissions = new HashSet<TierPermissionDTO>();

        try {
            String getTierPermissionQuery = SQLConstants.GET_TIER_PERMISSIONS_SQL;

            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(getTierPermissionQuery);
            ps.setInt(1, tenantId);

            resultSet = ps.executeQuery();
            while (resultSet.next()) {
                TierPermissionDTO tierPermission = new TierPermissionDTO();
                tierPermission.setTierName(resultSet.getString("TIER"));
                tierPermission.setPermissionType(resultSet.getString("PERMISSIONS_TYPE"));
                String roles = resultSet.getString("ROLES");
                if (roles != null && !roles.isEmpty()) {
                    String roleList[] = roles.split(",");
                    tierPermission.setRoles(roleList);
                }
                tierPermissions.add(tierPermission);
            }
        } catch (SQLException e) {
            handleException("Failed to get Tier permission information ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, resultSet);
        }
        return tierPermissions;
    }

    public TierPermissionDTO getTierPermission(String tierName, int tenantId) throws APIManagementException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet resultSet = null;

        TierPermissionDTO tierPermission = null;
        try {
            String getTierPermissionQuery = SQLConstants.GET_PERMISSION_OF_TIER_SQL;
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(getTierPermissionQuery);

            ps.setString(1, tierName);
            ps.setInt(2, tenantId);

            resultSet = ps.executeQuery();
            while (resultSet.next()) {
                tierPermission = new TierPermissionDTO();
                tierPermission.setTierName(tierName);
                tierPermission.setPermissionType(resultSet.getString("PERMISSIONS_TYPE"));
                String roles = resultSet.getString("ROLES");
                if (roles != null) {
                    String roleList[] = roles.split(",");
                    tierPermission.setRoles(roleList);
                }
            }
        } catch (SQLException e) {
            handleException("Failed to get Tier permission information for Tier " + tierName, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, resultSet);
        }
        return tierPermission;
    }

    public TierPermissionDTO getThrottleTierPermission(String tierName, int tenantId) throws APIManagementException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet resultSet = null;

        TierPermissionDTO tierPermission = null;
        try {
            String getTierPermissionQuery = SQLConstants.GET_THROTTLE_TIER_PERMISSION_SQL;
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(getTierPermissionQuery);

            ps.setString(1, tierName);
            ps.setInt(2, tenantId);

            resultSet = ps.executeQuery();
            while (resultSet.next()) {
                tierPermission = new TierPermissionDTO();
                tierPermission.setTierName(tierName);
                tierPermission.setPermissionType(resultSet.getString("PERMISSIONS_TYPE"));
                String roles = resultSet.getString("ROLES");
                if (roles != null) {
                    String roleList[] = roles.split(",");
                    tierPermission.setRoles(roleList);
                }
            }
        } catch (SQLException e) {
            handleException("Failed to get Tier permission information for Tier " + tierName, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, resultSet);
        }
        return tierPermission;
    }


    public void updateThrottleTierPermissions(String tierName, String permissionType, String roles, int tenantId)
            throws APIManagementException {
        Connection conn = null;
        PreparedStatement ps = null;
        PreparedStatement insertOrUpdatePS = null;
        ResultSet resultSet = null;
        int tierPermissionId = -1;

        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);

            String getTierPermissionQuery = SQLConstants.GET_THROTTLE_TIER_PERMISSION_ID_SQL;
            ps = conn.prepareStatement(getTierPermissionQuery);
            ps.setString(1, tierName);
            ps.setInt(2, tenantId);
            resultSet = ps.executeQuery();
            if (resultSet.next()) {
                tierPermissionId = resultSet.getInt("THROTTLE_TIER_PERMISSIONS_ID");
            }

            if (tierPermissionId == -1) {
                String query = SQLConstants.ADD_THROTTLE_TIER_PERMISSION_SQL;
                insertOrUpdatePS = conn.prepareStatement(query);
                insertOrUpdatePS.setString(1, tierName);
                insertOrUpdatePS.setString(2, permissionType);
                insertOrUpdatePS.setString(3, roles);
                insertOrUpdatePS.setInt(4, tenantId);
                insertOrUpdatePS.execute();
            } else {
                String query = SQLConstants.UPDATE_THROTTLE_TIER_PERMISSION_SQL;
                insertOrUpdatePS = conn.prepareStatement(query);
                insertOrUpdatePS.setString(1, tierName);
                insertOrUpdatePS.setString(2, permissionType);
                insertOrUpdatePS.setString(3, roles);
                insertOrUpdatePS.setInt(4, tierPermissionId);
                insertOrUpdatePS.setInt(5, tenantId);
                insertOrUpdatePS.executeUpdate();
            }
            conn.commit();
        } catch (SQLException e) {
            handleException("Error in updating tier permissions: " + e.getMessage(), e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, resultSet);
            APIMgtDBUtil.closeAllConnections(insertOrUpdatePS, null, null);
        }
    }

    public Set<TierPermissionDTO> getThrottleTierPermissions(int tenantId) throws APIManagementException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet resultSet = null;

        Set<TierPermissionDTO> tierPermissions = new HashSet<TierPermissionDTO>();

        try {
            String getTierPermissionQuery = SQLConstants.GET_THROTTLE_TIER_PERMISSIONS_SQL;

            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(getTierPermissionQuery);
            ps.setInt(1, tenantId);

            resultSet = ps.executeQuery();
            while (resultSet.next()) {
                TierPermissionDTO tierPermission = new TierPermissionDTO();
                tierPermission.setTierName(resultSet.getString("TIER"));
                tierPermission.setPermissionType(resultSet.getString("PERMISSIONS_TYPE"));
                String roles = resultSet.getString("ROLES");
                if (roles != null && !roles.isEmpty()) {
                    String roleList[] = roles.split(",");
                    tierPermission.setRoles(roleList);
                }
                tierPermissions.add(tierPermission);
            }
        } catch (SQLException e) {
            handleException("Failed to get Tier permission information ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, resultSet);
        }
        return tierPermissions;
    }

    private Set<String> getApplicationKeys(int applicationId, String getKeysSql) throws APIManagementException {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet result = null;
        Set<String> apiKeys = new HashSet<String>();
        try {
            connection = APIMgtDBUtil.getConnection();
            ps = connection.prepareStatement(getKeysSql);
            ps.setInt(1, applicationId);
            result = ps.executeQuery();
            while (result.next()) {
                apiKeys.add(APIUtil.decryptToken(result.getString("ACCESS_TOKEN")));
            }
        } catch (SQLException e) {
            handleException("Failed to get keys for application: " + applicationId, e);
        } catch (CryptoException e) {
            handleException("Failed to get keys for application: " + applicationId, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, connection, result);
        }
        return apiKeys;
    }

    private String getKeysSql(String accessTokenStoreTable) {
        String tokenStoreTable = APIConstants.ACCESS_TOKEN_STORE_TABLE;
        if (accessTokenStoreTable != null) {
            tokenStoreTable = accessTokenStoreTable;
        }

        return SQLConstants.GET_KEY_SQL_PREFIX + tokenStoreTable + SQLConstants.GET_KEY_SQL_SUFFIX;
    }

    /**
     * Get access token data based on application ID
     *
     * @param subscriptionId Subscription Id
     * @return access token data
     * @throws APIManagementException
     */
    public Map<String, String> getAccessTokenData(int subscriptionId) throws APIManagementException {
        Map<String, String> apiKeys = new HashMap<String, String>();

        if (APIUtil.checkAccessTokenPartitioningEnabled() && APIUtil.checkUserNameAssertionEnabled()) {
            String[] keyStoreTables = APIUtil.getAvailableKeyStoreTables();
            if (keyStoreTables != null) {
                for (String keyStoreTable : keyStoreTables) {
                    apiKeys = getAccessTokenData(subscriptionId, getKeysSqlUsingSubscriptionId(keyStoreTable));
                    if (apiKeys.size() > 0) {
                        break;
                    }
                }
            }
        } else {
            apiKeys = getAccessTokenData(subscriptionId, getKeysSqlUsingSubscriptionId(null));
        }
        return apiKeys;
    }

    private Map<String, String> getAccessTokenData(int subscriptionId, String getKeysSql)
            throws APIManagementException {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet result = null;
        Map<String, String> apiKeys = new HashMap<String, String>();
        try {
            connection = APIMgtDBUtil.getConnection();
            ps = connection.prepareStatement(getKeysSql);
            ps.setInt(1, subscriptionId);
            result = ps.executeQuery();
            while (result.next()) {
                apiKeys.put("token", APIUtil.decryptToken(result.getString("ACCESS_TOKEN")));
                apiKeys.put("status", result.getString("TOKEN_STATE"));
            }
        } catch (SQLException e) {
            handleException("Failed to get keys for application: " + subscriptionId, e);
        } catch (CryptoException e) {
            handleException("Failed to get keys for application: " + subscriptionId, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, connection, result);
        }
        return apiKeys;
    }

    private String getKeysSqlUsingSubscriptionId(String accessTokenStoreTable) {
        String tokenStoreTable = APIConstants.ACCESS_TOKEN_STORE_TABLE;
        if (accessTokenStoreTable != null) {
            tokenStoreTable = accessTokenStoreTable;
        }

        return SQLConstants.GET_KEY_SQL_OF_SUBSCRIPTION_ID_PREFIX +
               tokenStoreTable + SQLConstants.GET_KEY_SQL_OF_SUBSCRIPTION_ID_SUFFIX;
    }

    /**
     * This method returns the set of Subscribers for given provider
     *
     * @param providerName name of the provider
     * @return Set<Subscriber>
     * @throws APIManagementException if failed to get subscribers for given provider
     */
    public Set<Subscriber> getSubscribersOfProvider(String providerName) throws APIManagementException {

        Set<Subscriber> subscribers = new HashSet<Subscriber>();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet result = null;

        try {
            String sqlQuery = SQLConstants.GET_SUBSCRIBERS_OF_PROVIDER_SQL;
            connection = APIMgtDBUtil.getConnection();

            ps = connection.prepareStatement(sqlQuery);
            ps.setString(1, APIUtil.replaceEmailDomainBack(providerName));
            result = ps.executeQuery();

            while (result.next()) {
                // Subscription table should have API_VERSION AND API_PROVIDER
                Subscriber subscriber = new Subscriber(result.getString(APIConstants.SUBSCRIBER_FIELD_EMAIL_ADDRESS));
                subscriber.setName(result.getString(APIConstants.SUBSCRIBER_FIELD_USER_ID));
                subscriber.setSubscribedDate(result.getDate(APIConstants.SUBSCRIBER_FIELD_DATE_SUBSCRIBED));
                subscribers.add(subscriber);
            }

        } catch (SQLException e) {
            handleException("Failed to subscribers for :" + providerName, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, connection, result);
        }
        return subscribers;
    }

    public Set<Subscriber> getSubscribersOfAPI(APIIdentifier identifier) throws APIManagementException {

        Set<Subscriber> subscribers = new HashSet<Subscriber>();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet result = null;

        try {
            String sqlQuery = SQLConstants.GET_SUBSCRIBERS_OF_API_SQL;
            connection = APIMgtDBUtil.getConnection();

            ps = connection.prepareStatement(sqlQuery);
            ps.setString(1, APIUtil.replaceEmailDomainBack(identifier.getProviderName()));
            ps.setString(2, identifier.getApiName());
            ps.setString(3, identifier.getVersion());
            result = ps.executeQuery();
            while (result.next()) {
                Subscriber subscriber = new Subscriber(result.getString(APIConstants.SUBSCRIBER_FIELD_USER_ID));
                subscriber.setSubscribedDate(result.getTimestamp(APIConstants.SUBSCRIBER_FIELD_DATE_SUBSCRIBED));
                subscribers.add(subscriber);
            }
        } catch (SQLException e) {
            handleException("Failed to get subscribers for :" + identifier.getApiName(), e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, connection, result);
        }
        return subscribers;
    }

    public long getAPISubscriptionCountByAPI(APIIdentifier identifier) throws APIManagementException {

        String sqlQuery = SQLConstants.GET_API_SUBSCRIPTION_COUNT_BY_API_SQL;
        long subscriptions = 0;

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet result = null;

        try {
            connection = APIMgtDBUtil.getConnection();

            ps = connection.prepareStatement(sqlQuery);
            ps.setString(1, APIUtil.replaceEmailDomainBack(identifier.getProviderName()));
            ps.setString(2, identifier.getApiName());
            ps.setString(3, identifier.getVersion());
            result = ps.executeQuery();
            while (result.next()) {
                subscriptions = result.getLong("SUB_ID");
            }
        } catch (SQLException e) {
            handleException("Failed to get subscription count for API", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, connection, result);
        }
        return subscriptions;
    }

    /**
     * This method is used to update the subscriber
     *
     * @param identifier    APIIdentifier
     * @param context       Context of the API
     * @param applicationId Application id
     * @throws org.wso2.carbon.apimgt.api.APIManagementException if failed to update subscriber
     */
    public void updateSubscriptions(APIIdentifier identifier, String context, int applicationId, String subscriber)
            throws APIManagementException {
        addSubscription(identifier, context, applicationId, APIConstants.SubscriptionStatus.UNBLOCKED, subscriber);
    }

    /**
     * This method is used to update the subscription
     *
     * @param identifier    APIIdentifier
     * @param subStatus     Subscription Status[BLOCKED/UNBLOCKED]
     * @param applicationId Application id
     * @throws org.wso2.carbon.apimgt.api.APIManagementException if failed to update subscriber
     */
    public void updateSubscription(APIIdentifier identifier, String subStatus, int applicationId)
            throws APIManagementException {

        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        PreparedStatement updatePs = null;
        int apiId = -1;

        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);

            String getApiQuery = SQLConstants.GET_API_ID_SQL;
            ps = conn.prepareStatement(getApiQuery);
            ps.setString(1, APIUtil.replaceEmailDomainBack(identifier.getProviderName()));
            ps.setString(2, identifier.getApiName());
            ps.setString(3, identifier.getVersion());
            resultSet = ps.executeQuery();
            if (resultSet.next()) {
                apiId = resultSet.getInt("API_ID");
            }

            if (apiId == -1) {
                String msg = "Unable to get the API ID for: " + identifier;
                log.error(msg);
                throw new APIManagementException(msg);
            }

            String subsCreateStatus = getSubscriptionCreaeteStatus(identifier, applicationId, conn);

            if (APIConstants.SubscriptionCreatedStatus.UN_SUBSCRIBE.equals(subsCreateStatus)) {
                deleteSubscriptionByApiIDAndAppID(apiId, applicationId, conn);
            }


            //This query to update the AM_SUBSCRIPTION table
            String sqlQuery = SQLConstants.UPDATE_SUBSCRIPTION_OF_APPLICATION_SQL;

            //Updating data to the AM_SUBSCRIPTION table
            updatePs = conn.prepareStatement(sqlQuery);
            updatePs.setString(1, subStatus);
            updatePs.setString(2, identifier.getProviderName());
            updatePs.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            updatePs.setInt(4, apiId);
            updatePs.setInt(5, applicationId);
            updatePs.execute();

            // finally commit transaction
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error("Failed to rollback the add subscription ", e1);
                }
            }
            handleException("Failed to update subscription data ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, resultSet);
            APIMgtDBUtil.closeAllConnections(updatePs, null, null);
        }
    }

    /**
     * This method is used to update the subscription
     *
     * @param subscribedAPI subscribedAPI object that represents the new subscription detals
     * @throws APIManagementException if failed to update subscription
     */
    public void updateSubscription(SubscribedAPI subscribedAPI) throws APIManagementException {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);

            //This query to update the AM_SUBSCRIPTION table
            String sqlQuery = SQLConstants.UPDATE_SUBSCRIPTION_OF_UUID_SQL;

            //Updating data to the AM_SUBSCRIPTION table
            ps = conn.prepareStatement(sqlQuery);
            ps.setString(1, subscribedAPI.getSubStatus());
            //TODO Need to find logged in user who does this update.
            ps.setString(2, null);
            ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            ps.setString(4, subscribedAPI.getUUID());
            ps.execute();

            // finally commit transaction
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error("Failed to rollback the update subscription ", e1);
                }
            }
            handleException("Failed to update subscription data ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, null);
        }
    }

    public void updateSubscriptionStatus(int subscriptionId, String status) throws APIManagementException {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);

            //This query is to update the AM_SUBSCRIPTION table
            String sqlQuery = SQLConstants.UPDATE_SUBSCRIPTION_STATUS_SQL;

            ps = conn.prepareStatement(sqlQuery);
            ps.setString(1, status);
            ps.setInt(2, subscriptionId);
            ps.execute();

            //Commit transaction
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error("Failed to rollback subscription status update ", e1);
                }
            }
            handleException("Failed to update subscription status ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, null);
        }
    }

    /**
     * Update refreshed ApplicationAccesstoken's usertype
     *
     * @param keyType
     * @param newAccessToken
     * @param validityPeriod
     * @return
     * @throws APIManagementException
     */
    public void updateRefreshedApplicationAccessToken(String keyType, String newAccessToken, long validityPeriod)
            throws APIManagementException {

        String accessTokenStoreTable = APIConstants.ACCESS_TOKEN_STORE_TABLE;
        accessTokenStoreTable = getAccessTokenStoreTableFromAccessToken(newAccessToken, accessTokenStoreTable);
        // Update Access Token
        String sqlUpdateNewAccessToken = SQLConstants.UPDATE_REFRESHED_APPLICATION_ACCESS_TOKEN_PREFIX +
                                         accessTokenStoreTable +
                                         SQLConstants.UPDATE_REFRESHED_APPLICATION_ACCESS_TOKEN_SUFFIX;

        Connection connection = null;
        PreparedStatement prepStmt = null;
        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);

            prepStmt = connection.prepareStatement(sqlUpdateNewAccessToken);
            prepStmt.setString(1, APIConstants.ACCESS_TOKEN_USER_TYPE_APPLICATION);
            if (validityPeriod < 0) {
                prepStmt.setLong(2, Long.MAX_VALUE);
            } else {
                prepStmt.setLong(2, validityPeriod * 1000);
            }
            prepStmt.setString(3, APIUtil.encryptToken(newAccessToken));
            // prepStmt.setString(4, keyType);

            prepStmt.execute();
            connection.commit();

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException e1) {
                    log.error("Failed to rollback the add access token ", e1);
                }
            }
        } catch (CryptoException e) {
            log.error(e.getMessage(), e);
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException e1) {
                    log.error("Failed to rollback the add access token ", e1);
                }
            }
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, null);
        }

    }

    public String getRegistrationApprovalState(int appId, String keyType) throws APIManagementException {
        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        String state = null;

        try {
            conn = APIMgtDBUtil.getConnection();
            String sqlQuery = SQLConstants.GET_REGISTRATION_APPROVAL_STATUS_SQL;

            ps = conn.prepareStatement(sqlQuery);
            ps.setInt(1, appId);
            ps.setString(2, keyType);
            resultSet = ps.executeQuery();

            while (resultSet.next()) {
                state = resultSet.getString("STATE");
            }
        } catch (SQLException e) {
            handleException("Error while getting Application Registration State.", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, resultSet);
        }
        return state;
    }

    /**
     * Update the consumer key and application status for the given key type and application.
     *
     * @param application
     * @param keyType
     */
    public void updateApplicationKeyTypeMapping(Application application, String keyType) throws APIManagementException {
        OAuthApplicationInfo app = application.getOAuthApp(keyType);
        String consumerKey = null;
        if (app != null) {
            consumerKey = app.getClientId();
        }

        if (consumerKey != null && application.getId() != -1) {
            String addApplicationKeyMapping = SQLConstants.UPDATE_APPLICAITON_KEY_TYPE_MAPPINGS_SQL;

            Connection connection = null;
            PreparedStatement ps = null;
            try {
                connection = APIMgtDBUtil.getConnection();
                connection.setAutoCommit(false);
                ps = connection.prepareStatement(addApplicationKeyMapping);
                ps.setString(1, consumerKey);
                ps.setInt(2, application.getId());
                ps.setString(3, keyType);
                ps.executeUpdate();
                connection.commit();
            } catch (SQLException e) {
                handleException("Error updating the CONSUMER KEY of the AM_APPLICATION_KEY_MAPPING table where " +
                                "APPLICATION_ID = " + application.getId() + " and KEY_TYPE = " + keyType, e);
            } finally {
                APIMgtDBUtil.closeAllConnections(ps, connection, null);
            }
        }
    }

    /**
     * This method will create a new client at key-manager side.further it will add new record to
     * the AM_APPLICATION_KEY_MAPPING table
     *
     * @param keyType
     * @param applicationName apim application name.
     * @param userName        apim user name
     * @param clientId        this is the consumner key.
     * @throws APIManagementException
     */
    public void createApplicationKeyTypeMappingForManualClients(String keyType, String applicationName, String userName,
                                                                String clientId) throws APIManagementException {
        String consumerKey = null;
        if (clientId != null) {
            consumerKey = clientId;
        }
        Connection connection = null;
        PreparedStatement ps = null;

        //APIM application id.
        int applicationId = getApplicationId(applicationName, userName);

        if (consumerKey != null) {
            String addApplicationKeyMapping = SQLConstants.ADD_APPLICATION_KEY_TYPE_MAPPING_SQL;
            try {
                connection = APIMgtDBUtil.getConnection();
                connection.setAutoCommit(false);
                ps = connection.prepareStatement(addApplicationKeyMapping);
                ps.setInt(1, applicationId);
                ps.setString(2, consumerKey);
                ps.setString(3, keyType);
                ps.setString(4, APIConstants.AppRegistrationStatus.REGISTRATION_COMPLETED);
                // If the CK/CS pair is pasted on the screen set this to MAPPED
                ps.setString(5, "MAPPED");
                ps.execute();
                connection.commit();

            } catch (SQLException e) {
                handleException("Error while inserting record to the AM_APPLICATION_KEY_MAPPING table,  " +
                                "error is =  " + e.getMessage(), e);
            } finally {
                APIMgtDBUtil.closeAllConnections(ps, connection, null);
            }
        }
    }


    /**
     * Updates the state of the Application Registration.
     *
     * @param state   State of the registration.
     * @param keyType PRODUCTION | SANDBOX
     * @param appId   ID of the Application.
     * @throws APIManagementException if updating fails.
     */
    public void updateApplicationRegistration(String state, String keyType, int appId) throws APIManagementException {
        Connection conn = null;
        PreparedStatement ps = null;

        String sqlStmt = SQLConstants.UPDATE_APPLICATION_KEY_MAPPING_SQL;
        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);

            ps = conn.prepareStatement(sqlStmt);
            ps.setString(1, state);
            ps.setInt(2, appId);
            ps.setString(3, keyType);
            ps.execute();

            conn.commit();
        } catch (SQLException e) {
            handleException("Error while updating registration entry.", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, null);
        }
    }


    /**
     * @param apiIdentifier APIIdentifier
     * @param userId        User Id
     * @return true if user subscribed for given APIIdentifier
     * @throws APIManagementException if failed to check subscribed or not
     */
    public boolean isSubscribed(APIIdentifier apiIdentifier, String userId) throws APIManagementException {
        boolean isSubscribed = false;
        String loginUserName = getLoginUserName(userId);

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sqlQuery = SQLConstants.GET_SUBSCRIPTION_SQL;

        if (forceCaseInsensitiveComparisons) {
            sqlQuery = SQLConstants.GET_SUBSCRIPTION_CASE_INSENSITIVE_SQL;
        }

        try {
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            ps.setString(1, APIUtil.replaceEmailDomainBack(apiIdentifier.getProviderName()));
            ps.setString(2, apiIdentifier.getApiName());
            ps.setString(3, apiIdentifier.getVersion());
            ps.setString(4, loginUserName);
            int tenantId;
            tenantId = APIUtil.getTenantId(loginUserName);
            ps.setInt(5, tenantId);

            rs = ps.executeQuery();

            if (rs.next()) {
                isSubscribed = true;
            }
        } catch (SQLException e) {
            handleException("Error while checking if user has subscribed to the API ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
        return isSubscribed;
    }

    /**
     * @param providerName Name of the provider
     * @return UserApplicationAPIUsage of given provider
     * @throws org.wso2.carbon.apimgt.api.APIManagementException if failed to get
     *                                                           UserApplicationAPIUsage for given provider
     */
    public UserApplicationAPIUsage[] getAllAPIUsageByProvider(String providerName) throws APIManagementException {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet result = null;

        try {
            String sqlQuery = SQLConstants.GET_APP_API_USAGE_BY_PROVIDER_SQL;
            connection = APIMgtDBUtil.getConnection();

            ps = connection.prepareStatement(sqlQuery);
            ps.setString(1, APIUtil.replaceEmailDomainBack(providerName));
            result = ps.executeQuery();

            Map<String, UserApplicationAPIUsage> userApplicationUsages = new TreeMap<String, UserApplicationAPIUsage>();
            while (result.next()) {
                int subId = result.getInt("SUBSCRIPTION_ID");
                Map<String, String> keyData = getAccessTokenData(subId);
                String accessToken = keyData.get("token");
                String tokenStatus = keyData.get("status");
                String userId = result.getString("USER_ID");
                String application = result.getString("APPNAME");
                int appId = result.getInt("APPLICATION_ID");
                String subStatus = result.getString("SUB_STATUS");
                String subsCreateState = result.getString("SUBS_CREATE_STATE");
                String key = userId + "::" + application;
                UserApplicationAPIUsage usage = userApplicationUsages.get(key);
                if (usage == null) {
                    usage = new UserApplicationAPIUsage();
                    usage.setUserId(userId);
                    usage.setApplicationName(application);
                    usage.setAppId(appId);
                    usage.setAccessToken(accessToken);
                    usage.setAccessTokenStatus(tokenStatus);
                    userApplicationUsages.put(key, usage);
                }
                APIIdentifier apiId = new APIIdentifier(result.getString("API_PROVIDER"), result.getString
                        ("API_NAME"), result.getString("API_VERSION"));
                SubscribedAPI apiSubscription = new SubscribedAPI(new Subscriber(userId), apiId);
                apiSubscription.setSubStatus(subStatus);
                apiSubscription.setSubCreatedStatus(subsCreateState);
                apiSubscription.setUUID(result.getString("SUB_UUID"));
                apiSubscription.setTier(new Tier(result.getString("SUB_TIER_ID")));
                Application applicationObj = new Application(result.getString("APP_UUID"));
                apiSubscription.setApplication(applicationObj);
                usage.addApiSubscriptions(apiSubscription);
            }
            return userApplicationUsages.values().toArray(new UserApplicationAPIUsage[userApplicationUsages.size()]);
        } catch (SQLException e) {
            handleException("Failed to find API Usage for :" + providerName, e);
            return null;
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, connection, result);
        }
    }

    /**
     * return the subscriber for given access token
     *
     * @param accessToken AccessToken
     * @return Subscriber
     * @throws APIManagementException if failed to get subscriber for given access token
     */
    public Subscriber getSubscriberById(String accessToken) throws APIManagementException {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet result = null;
        Subscriber subscriber = null;
        String query = SQLConstants.GET_SUBSCRIBER_BY_ID_SQL;

        try {
            connection = APIMgtDBUtil.getConnection();
            ps = connection.prepareStatement(query);
            ps.setString(1, APIUtil.encryptToken(accessToken));

            result = ps.executeQuery();
            while (result.next()) {
                subscriber = new Subscriber(result.getString(APIConstants.SUBSCRIBER_FIELD_USER_ID));
                subscriber.setSubscribedDate(result.getDate(APIConstants.SUBSCRIBER_FIELD_DATE_SUBSCRIBED));
            }
        } catch (SQLException e) {
            handleException("Failed to get Subscriber for accessToken", e);
        } catch (CryptoException e) {
            handleException("Failed to get Subscriber for accessToken", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, connection, result);
        }
        return subscriber;
    }

    @Deprecated
    public String[] addOAuthConsumer(String username, int tenantId, String appName, String callbackUrl)
            throws IdentityOAuthAdminException, APIManagementException {

        Connection connection = null;
        PreparedStatement prepStmt = null;
        String loginUserName = getLoginUserName(username);

        ResultSet rs = null;
        String consumerKey = null;
        String consumerSecret = null;

        String sqlCheckStmt = SQLConstants.GET_OAUTH_CONSUMER_SQL;
        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);

            prepStmt = connection.prepareStatement(sqlCheckStmt);
            prepStmt.setString(1, loginUserName.toLowerCase());
            prepStmt.setInt(2, tenantId);
            prepStmt.setString(3, appName);

            rs = prepStmt.executeQuery();

            if (rs.next()) {
                consumerKey = rs.getString("CONSUMER_KEY");
                consumerSecret = rs.getString("CONSUMER_SECRET");
                consumerSecret = APIUtil.encryptToken(consumerSecret);
            } else {
                String sqlStmt = SQLConstants.ADD_OAUTH_CONSUMER_SQL;
                consumerSecret = OAuthUtil.getRandomNumber();
                do {
                    consumerKey = OAuthUtil.getRandomNumber();
                } while (isDuplicateConsumer(consumerKey));

                consumerSecret = APIUtil.encryptToken(consumerSecret);

                prepStmt = connection.prepareStatement(sqlStmt);
                prepStmt.setString(1, consumerKey);
                prepStmt.setString(2, consumerSecret);
                prepStmt.setString(3, loginUserName.toLowerCase());
                prepStmt.setInt(4, tenantId);
                prepStmt.setString(5, OAuthConstants.OAuthVersions.VERSION_2);
                prepStmt.setString(6, appName);
                prepStmt.setString(7, callbackUrl);
                prepStmt.execute();
            }
            connection.commit();
        } catch (SQLException e) {
            handleException("Error when adding a new OAuth consumer.", e);
        } catch (CryptoException e) {
            handleException("Error while attempting to encrypt consumer-secret.", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
        }
        try {
            return new String[]{consumerKey, APIUtil.decryptToken(consumerSecret)};
        } catch (CryptoException e) {
            handleException("Error while decrypting consumer-secret", e);
        }
        return null;
    }


    private void updateOAuthConsumerApp(String appName, String callbackUrl)
            throws IdentityOAuthAdminException, APIManagementException {
        Connection connection = null;
        PreparedStatement prepStmt = null;
        String sqlStmt = SQLConstants.UPDATE_OAUTH_CONSUMER_SQL;
        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);

            prepStmt = connection.prepareStatement(sqlStmt);
            prepStmt.setString(1, callbackUrl);
            prepStmt.setString(2, appName);
            prepStmt.execute();

            connection.commit();
        } catch (SQLException e) {
            handleException("Error when updating OAuth consumer App for " + appName, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, null);
        }
    }

    private boolean isDuplicateConsumer(String consumerKey) throws APIManagementException {
        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rSet = null;
        String sqlQuery = SQLConstants.GET_ALL_OAUTH_CONSUMER_APPS_SQL;

        boolean isDuplicateConsumer = false;
        try {
            connection = APIMgtDBUtil.getConnection();
            prepStmt = connection.prepareStatement(sqlQuery);
            prepStmt.setString(1, consumerKey);

            rSet = prepStmt.executeQuery();
            if (rSet.next()) {
                isDuplicateConsumer = true;
            }
        } catch (SQLException e) {
            handleException("Error when reading the application information from" + " the persistence store.", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, rSet);
        }
        return isDuplicateConsumer;
    }

    public int addApplication(Application application, String userId) throws APIManagementException {
        Connection conn = null;
        int applicationId = 0;
        String loginUserName = getLoginUserName(userId);
        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);
            applicationId = addApplication(application, loginUserName, conn);

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error("Failed to rollback the add Application ", e1);
                }
            }
            handleException("Failed to add Application", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(null, conn, null);
        }
        return applicationId;
    }

    public void addRating(APIIdentifier apiId, int rating, String user) throws APIManagementException {
        Connection conn = null;
        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);
            addRating(apiId, rating, user, conn);

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error("Failed to rollback the add Application ", e1);
                }
            }
            handleException("Failed to add Application", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(null, conn, null);
        }
    }

    /**
     * @param apiIdentifier API Identifier
     * @param userId        User Id
     * @throws APIManagementException if failed to add Application
     */
    public void addRating(APIIdentifier apiIdentifier, int rating, String userId, Connection conn)
            throws APIManagementException, SQLException {
        PreparedStatement ps = null;
        PreparedStatement psSelect = null;
        ResultSet rs = null;

        try {
            int tenantId;
            tenantId = APIUtil.getTenantId(userId);
            //Get subscriber Id
            Subscriber subscriber = getSubscriber(userId, tenantId, conn);
            if (subscriber == null) {
                String msg = "Could not load Subscriber records for: " + userId;
                log.error(msg);
                throw new APIManagementException(msg);
            }
            int apiId;
            apiId = getAPIID(apiIdentifier, conn);
            if (apiId == -1) {
                String msg = "Could not load API record for: " + apiIdentifier.getApiName();
                log.error(msg);
                throw new APIManagementException(msg);
            }
            boolean userRatingExists = false;
            //This query to check the ratings already exists for the user in the AM_API_RATINGS table
            String sqlQuery = SQLConstants.GET_API_RATING_SQL;

            psSelect = conn.prepareStatement(sqlQuery);
            psSelect.setInt(1, apiId);
            psSelect.setInt(2, subscriber.getId());
            rs = psSelect.executeQuery();

            while (rs.next()) {
                userRatingExists = true;
            }

            String sqlAddQuery;
            if (!userRatingExists) {
                //This query to update the AM_API_RATINGS table
                sqlAddQuery = SQLConstants.APP_API_RATING_SQL;
            } else {
                //This query to insert into the AM_API_RATINGS table
                sqlAddQuery = SQLConstants.UPDATE_API_RATING_SQL;
            }
            // Adding data to the AM_API_RATINGS  table
            ps = conn.prepareStatement(sqlAddQuery);
            ps.setInt(1, rating);
            ps.setInt(2, apiId);
            ps.setInt(3, subscriber.getId());
            ps.executeUpdate();

        } catch (SQLException e) {
            handleException("Failed to add API rating of the user:" + userId, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, null, rs);
            APIMgtDBUtil.closeAllConnections(psSelect, null, null);
        }
    }

    public void removeAPIRating(APIIdentifier apiId, String user) throws APIManagementException {
        Connection conn = null;
        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);

            removeAPIRating(apiId, user, conn);

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error("Failed to rollback the add Application ", e1);
                }
            }
            handleException("Failed to add Application", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(null, conn, null);
        }
    }

    /**
     * @param apiIdentifier API Identifier
     * @param userId        User Id
     * @throws APIManagementException if failed to add Application
     */
    public void removeAPIRating(APIIdentifier apiIdentifier, String userId, Connection conn)
            throws APIManagementException, SQLException {
        PreparedStatement ps = null;
        PreparedStatement psSelect = null;
        ResultSet rs = null;

        try {
            int tenantId;
            int rateId = -1;
            tenantId = APIUtil.getTenantId(userId);
            //Get subscriber Id
            Subscriber subscriber = getSubscriber(userId, tenantId, conn);
            if (subscriber == null) {
                String msg = "Could not load Subscriber records for: " + userId;
                log.error(msg);
                throw new APIManagementException(msg);
            }
            //Get API Id
            int apiId = -1;
            apiId = getAPIID(apiIdentifier, conn);
            if (apiId == -1) {
                String msg = "Could not load API record for: " + apiIdentifier.getApiName();
                log.error(msg);
                throw new APIManagementException(msg);
            }

            //This query to check the ratings already exists for the user in the AM_API_RATINGS table
            String sqlQuery = SQLConstants.GET_RATING_ID_SQL;

            psSelect = conn.prepareStatement(sqlQuery);
            psSelect.setInt(1, apiId);
            psSelect.setInt(2, subscriber.getId());
            rs = psSelect.executeQuery();

            while (rs.next()) {
                rateId = rs.getInt("RATING_ID");
            }
            String sqlAddQuery;
            if (rateId != -1) {
                //This query to delete the specific rate row from the AM_API_RATINGS table
                sqlAddQuery = SQLConstants.REMOVE_RATING_SQL;
                // Adding data to the AM_API_RATINGS  table
                ps = conn.prepareStatement(sqlAddQuery);
                ps.setInt(1, rateId);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            handleException("Failed to delete API rating", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, null, null);
            APIMgtDBUtil.closeAllConnections(psSelect, null, rs);
        }
    }

    public int getUserRating(APIIdentifier apiId, String user) throws APIManagementException {
        Connection conn = null;
        int userRating = 0;
        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);

            userRating = getUserRating(apiId, user, conn);

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error("Failed to rollback getting user ratings ", e1);
                }
            }
            handleException("Failed to get user ratings", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(null, conn, null);
        }
        return userRating;
    }

    /**
     * @param apiIdentifier API Identifier
     * @param userId        User Id
     * @throws APIManagementException if failed to add Application
     */
    public int getUserRating(APIIdentifier apiIdentifier, String userId, Connection conn)
            throws APIManagementException, SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        int userRating = 0;
        try {
            int tenantId;
            tenantId = APIUtil.getTenantId(userId);
            //Get subscriber Id
            Subscriber subscriber = getSubscriber(userId, tenantId, conn);
            if (subscriber == null) {
                String msg = "Could not load Subscriber records for: " + userId;
                log.error(msg);
                throw new APIManagementException(msg);
            }
            //Get API Id
            int apiId = -1;
            apiId = getAPIID(apiIdentifier, conn);
            if (apiId == -1) {
                String msg = "Could not load API record for: " + apiIdentifier.getApiName();
                log.error(msg);
                throw new APIManagementException(msg);
            }
            //This query to update the AM_API_RATINGS table
            String sqlQuery = SQLConstants.GET_RATING_SQL;
            // Adding data to the AM_API_RATINGS  table
            ps = conn.prepareStatement(sqlQuery);
            ps.setInt(1, subscriber.getId());
            ps.setInt(2, apiId);
            rs = ps.executeQuery();

            while (rs.next()) {
                userRating = rs.getInt("RATING");
            }

        } catch (SQLException e) {
            handleException("Failed to add Application", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, null, rs);
        }
        return userRating;
    }

    public float getAverageRating(APIIdentifier apiId) throws APIManagementException {
        Connection conn = null;
        float avrRating = 0;
        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);

            avrRating = getAverageRating(apiId, conn);
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error("Failed to rollback getting user ratings ", e1);
                }
            }
            handleException("Failed to get user ratings", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(null, conn, null);
        }
        return avrRating;
    }

    public float getAverageRating(int apiId) throws APIManagementException {
        Connection conn = null;
        float avrRating = 0;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);

            if (apiId == -1) {
                String msg = "Invalid APIId : " + apiId;
                log.error(msg);
                return Float.NEGATIVE_INFINITY;
            }
            //This query to update the AM_API_RATINGS table
            String sqlQuery = SQLConstants.GET_AVERAGE_RATING_SQL;

            ps = conn.prepareStatement(sqlQuery);
            ps.setInt(1, apiId);
            rs = ps.executeQuery();

            while (rs.next()) {
                avrRating = rs.getFloat("RATING");
            }
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error("Failed to rollback getting user ratings ", e1);
                }
            }
            handleException("Failed to get user ratings", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
        return avrRating;
    }

    /**
     * @param apiIdentifier API Identifier
     * @throws APIManagementException if failed to add Application
     */
    public float getAverageRating(APIIdentifier apiIdentifier, Connection conn)
            throws APIManagementException, SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        float avrRating = 0;
        try {
            //Get API Id
            int apiId;
            apiId = getAPIID(apiIdentifier, conn);
            if (apiId == -1) {
                String msg = "Could not load API record for: " + apiIdentifier.getApiName();
                log.error(msg);
                return Float.NEGATIVE_INFINITY;
            }
            //This query to update the AM_API_RATINGS table
            String sqlQuery = SQLConstants.GET_AVERAGE_RATING_SQL;

            ps = conn.prepareStatement(sqlQuery);
            ps.setInt(1, apiId);
            rs = ps.executeQuery();

            while (rs.next()) {
                avrRating = rs.getFloat("RATING");
            }

        } catch (SQLException e) {
            handleException("Failed to add Application", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, null, rs);
        }

        BigDecimal decimal = new BigDecimal(avrRating);
        return Float.parseFloat(decimal.setScale(1, BigDecimal.ROUND_UP).toString());
    }

    /**
     * @param application Application
     * @param userId      User Id
     * @throws APIManagementException if failed to add Application
     */
    public int addApplication(Application application, String userId, Connection conn)
            throws APIManagementException, SQLException {
        PreparedStatement ps = null;
        conn.setAutoCommit(false);
        ResultSet rs = null;

        int applicationId = 0;
        try {
            int tenantId;
            tenantId = APIUtil.getTenantId(userId);

            //Get subscriber Id
            Subscriber subscriber = getSubscriber(userId, tenantId, conn);
            if (subscriber == null) {
                String msg = "Could not load Subscriber records for: " + userId;
                log.error(msg);
                throw new APIManagementException(msg);
            }
            //This query to update the AM_APPLICATION table
            String sqlQuery = SQLConstants.APP_APPLICATION_SQL;
            // Adding data to the AM_APPLICATION  table
            //ps = conn.prepareStatement(sqlQuery);
            ps = conn.prepareStatement(sqlQuery, new String[]{"APPLICATION_ID"});
            if (conn.getMetaData().getDriverName().contains("PostgreSQL")) {
                ps = conn.prepareStatement(sqlQuery, new String[]{"application_id"});
            }

            ps.setString(1, application.getName());
            ps.setInt(2, subscriber.getId());
            ps.setString(3, application.getTier());
            ps.setString(4, application.getCallbackUrl());
            ps.setString(5, application.getDescription());

            if (APIConstants.DEFAULT_APPLICATION_NAME.equals(application.getName())) {
                ps.setString(6, APIConstants.ApplicationStatus.APPLICATION_APPROVED);
            } else {
                ps.setString(6, APIConstants.ApplicationStatus.APPLICATION_CREATED);
            }
            ps.setString(7, application.getGroupId());
            ps.setString(8, subscriber.getName());
            ps.setTimestamp(9, new Timestamp(System.currentTimeMillis()));
            ps.setString(10, UUID.randomUUID().toString());
            ps.executeUpdate();

            rs = ps.getGeneratedKeys();
            while (rs.next()) {
                applicationId = Integer.parseInt(rs.getString(1));
            }

            conn.commit();
        } catch (SQLException e) {
            handleException("Failed to add Application", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, null, rs);
        }
        return applicationId;
    }

    public void updateApplication(Application application) throws APIManagementException {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);

            //This query to update the AM_APPLICATION table
            String sqlQuery = SQLConstants.UPDATE_APPLICATION_SQL;
            // Adding data to the AM_APPLICATION  table
            ps = conn.prepareStatement(sqlQuery);
            ps.setString(1, application.getName());
            ps.setString(2, application.getTier());
            ps.setString(3, application.getCallbackUrl());
            ps.setString(4, application.getDescription());
            //TODO need to find the proper user who updates this application.
            ps.setString(5, null);
            ps.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
            ps.setInt(7, application.getId());

            ps.executeUpdate();
            conn.commit();

            updateOAuthConsumerApp(application.getName(), application.getCallbackUrl());
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error("Failed to rollback the update Application ", e1);
                }
            }
            handleException("Failed to update Application", e);
        } catch (IdentityOAuthAdminException e) {
            handleException("Failed to update OAuth Consumer Application", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, null);
        }
    }

    /**
     * Update the status of the Application creation process
     *
     * @param applicationId
     * @param status
     * @throws APIManagementException
     */
    public void updateApplicationStatus(int applicationId, String status) throws APIManagementException {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);

            String updateSqlQuery = SQLConstants.UPDATE_APPLICATION_STATUS_SQL;

            ps = conn.prepareStatement(updateSqlQuery);
            ps.setString(1, status);
            ps.setInt(2, applicationId);

            ps.executeUpdate();

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error("Failed to rollback the update Application ", e1);
                }
            }
            handleException("Failed to update Application", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, null);
        }
    }

    /**
     * get the status of the Application creation process
     *
     * @param appName
     * @return
     * @throws APIManagementException
     */
    public String getApplicationStatus(String appName, String userId) throws APIManagementException {
        int applicationId = getApplicationId(appName, userId);
        return getApplicationStatusById(applicationId);
    }

    /**
     * get the status of the Application creation process given the application Id
     *
     * @param applicationId Id of the Application
     * @return
     * @throws APIManagementException
     */
    public String getApplicationStatusById(int applicationId) throws APIManagementException {
        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        String status = null;
        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);

            String sqlQuery = SQLConstants.GET_APPLICATION_STATUS_BY_ID_SQL;

            ps = conn.prepareStatement(sqlQuery);
            ps.setInt(1, applicationId);
            resultSet = ps.executeQuery();
            while (resultSet.next()) {
                status = resultSet.getString("APPLICATION_STATUS");
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error("Failed to rollback the update Application ", e1);
                }
            }
            handleException("Failed to update Application", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, resultSet);
        }
        return status;
    }

    /**
     * Check whether given application name is available under current subscriber or group
     *
     * @param appName  application name
     * @param username subscriber
     * @param groupId  group of the subscriber
     * @return true if application is available for the subscriber
     * @throws APIManagementException if failed to get applications for given subscriber
     */
    public boolean isApplicationExist(String appName, String username, String groupId) throws APIManagementException {
        if (username == null) {
            return false;
        }
        Subscriber subscriber = getSubscriber(username);

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        int appId = 0;

        String sqlQuery = SQLConstants.GET_APPLICATION_ID_PREFIX;

        String whereClauseWithGroupId = " AND (APP.GROUP_ID = ? OR (APP.GROUP_ID = '' AND SUB.USER_ID = ?))";
        String whereClauseWithGroupIdCaseInsensitive = " AND (APP.GROUP_ID = ? OR (APP.GROUP_ID = '' " + "AND LOWER" +
                                                       "(SUB.USER_ID) = LOWER(?)))";
        String whereClause = " AND SUB.USER_ID = ? ";
        String whereClauseCaseInsensitive = " AND LOWER(SUB.USER_ID) = LOWER(?) ";

        try {
            connection = APIMgtDBUtil.getConnection();

            if (!StringUtils.isEmpty(groupId)) {
                if (forceCaseInsensitiveComparisons) {
                    sqlQuery += whereClauseWithGroupIdCaseInsensitive;
                } else {
                    sqlQuery += whereClauseWithGroupId;
                }
            } else {
                if (forceCaseInsensitiveComparisons) {
                    sqlQuery += whereClauseCaseInsensitive;
                } else {
                    sqlQuery += whereClause;
                }
            }

            preparedStatement = connection.prepareStatement(sqlQuery);
            preparedStatement.setString(1, appName);

            if (!StringUtils.isEmpty(groupId)) {
                preparedStatement.setString(2, groupId);
                preparedStatement.setString(3, subscriber.getName());
            } else {
                preparedStatement.setString(2, subscriber.getName());
            }

            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                appId = resultSet.getInt("APPLICATION_ID");
            }

            if (appId > 0) {
                return true;
            }

        } catch (SQLException e) {
            handleException("Error while getting the id  of " + appName + " from the persistence store.", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(preparedStatement, connection, resultSet);
        }
        return false;
    }


    /**
     * @param username Subscriber
     * @return ApplicationId for given appname.
     * @throws APIManagementException if failed to get Applications for given subscriber.
     */
    public int getApplicationId(String appName, String username) throws APIManagementException {
        if (username == null) {
            return 0;
        }
        Subscriber subscriber = getSubscriber(username);

        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        int appId = 0;

        String sqlQuery = SQLConstants.GET_APPLICATION_ID_SQL;
        try {
            connection = APIMgtDBUtil.getConnection();
            prepStmt = connection.prepareStatement(sqlQuery);
            prepStmt.setInt(1, subscriber.getId());
            prepStmt.setString(2, appName);
            rs = prepStmt.executeQuery();


            while (rs.next()) {
                appId = rs.getInt("APPLICATION_ID");
            }

        } catch (SQLException e) {
            handleException("Error when getting the application id from" + " the persistence store.", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
        }
        return appId;
    }

    /**
     * Find the name of the application by Id
     *
     * @param applicationId - applicatoin id
     * @return - application name
     * @throws APIManagementException
     */
    public String getApplicationNameFromId(int applicationId) throws APIManagementException {

        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        String appName = null;

        String sqlQuery = SQLConstants.GET_APPLICATION_NAME_FROM_ID_SQL;

        try {
            connection = APIMgtDBUtil.getConnection();
            prepStmt = connection.prepareStatement(sqlQuery);
            prepStmt.setInt(1, applicationId);
            rs = prepStmt.executeQuery();

            while (rs.next()) {
                appName = rs.getString("NAME");
            }

        } catch (SQLException e) {
            handleException("Error when getting the application name for id " + applicationId, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
        }
        return appName;
    }

    /**
     * This method will give basic Application Details like name, Application tier, call back url and description.
     * Applications returned by this method will not have Access Tokens populated. This method can be used to check
     * existency of an app.
     *
     * @param subscriberName Name of the Application Owner
     * @param groupingId     Grouping ID
     * @return List of {@code Application}s having basic details populated.
     * @throws APIManagementException
     */
    @Deprecated
    public List<Application> getBasicApplicationDetails(String subscriberName, String groupingId)
            throws APIManagementException {
        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        ArrayList<Application> applications = null;
        String sqlQuery = SQLConstants.GET_BASIC_APPLICATION_DETAILS_PREFIX;
        String whereClauseWithGroupId = "   AND " + "     (GROUP_ID= ? " + "      OR " + "     (GROUP_ID='' AND SUB" +
                                        ".USER_ID=?))";
        String whereClause = "   AND " + " SUB.USER_ID=?";

        if (groupingId != null && !"null".equals(groupingId) && !groupingId.isEmpty()) {
            sqlQuery += whereClauseWithGroupId;
        } else {
            sqlQuery += whereClause;
        }
        try {
            connection = APIMgtDBUtil.getConnection();
            prepStmt = connection.prepareStatement(sqlQuery);
            if (groupingId != null && !"null".equals(groupingId) && !groupingId.isEmpty()) {
                prepStmt.setString(1, groupingId);
                prepStmt.setString(2, subscriberName);
            } else {
                prepStmt.setString(1, subscriberName);
            }
            rs = prepStmt.executeQuery();

            Application application = null;
            while (rs.next()) {
                application = new Application(rs.getString("NAME"), new Subscriber(subscriberName));
                application.setId(rs.getInt("APPLICATION_ID"));
                application.setTier(rs.getString("APPLICATION_TIER"));
                application.setCallbackUrl(rs.getString("CALLBACK_URL"));
                application.setDescription(rs.getString("DESCRIPTION"));
                application.setStatus(rs.getString("APPLICATION_STATUS"));
            }

            if (application != null) {
                applications = new ArrayList<Application>();
                applications.add(application);
            }
        } catch (SQLException e) {
            handleException("Error when reading the application information from" + " the persistence store.", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
        }
        return applications;
    }

    public int getAllApplicationCount(Subscriber subscriber, String groupingId, String search) throws APIManagementException {

        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet resultSet = null;
        String sqlQuery = null;

        if (groupingId != null && !"null".equals(groupingId) && !groupingId.isEmpty()) {

            if (forceCaseInsensitiveComparisons) {
                sqlQuery = SQLConstants.GET_APPLICATIONS_COUNNT_CASESENSITVE_WITHGROUPID;
            } else {
                sqlQuery = SQLConstants.GET_APPLICATIONS_COUNNT_NONE_CASESENSITVE_WITHGROUPID;
            }

        } else {

            if (forceCaseInsensitiveComparisons) {
                sqlQuery = SQLConstants.GET_APPLICATIONS_COUNNT_CASESENSITVE;
            } else {
                sqlQuery = SQLConstants.GET_APPLICATIONS_COUNNT_NONE_CASESENSITVE;
            }

        }

        try {
            connection = APIMgtDBUtil.getConnection();

            prepStmt = connection.prepareStatement(sqlQuery);
            if (groupingId != null && !"null".equals(groupingId) && !groupingId.isEmpty()) {
                prepStmt.setString(1, groupingId);
                prepStmt.setString(2, subscriber.getName());
                prepStmt.setString(3, "%" + search + "%");

            } else {
                prepStmt.setString(1, subscriber.getName());
                prepStmt.setString(2, "%" + search + "%");
            }

            resultSet = prepStmt.executeQuery();

            int applicationCount = 0;
            if (resultSet != null) {
                while (resultSet.next()) {
                    applicationCount = resultSet.getInt("count");
                }
            }
            if (applicationCount > 0) {
                return applicationCount;
            }
        } catch (SQLException e) {
            handleException("Failed to get applicaiton count : " , e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, resultSet);
        }

        return 0;
    }

    /**
     * #TODO later we might need to use only this method.
     * @param subscriber
     * @param groupingId
     * @return
     * @throws APIManagementException
     */
    public Application[] getApplicationsWithPagination(Subscriber subscriber, String groupingId,int start ,
            int offset , String search, String sortColumn, String sortOrder) throws APIManagementException {

        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        Application[] applications = null;
        String sqlQuery = null;

        if (groupingId != null && !"null".equals(groupingId) && !groupingId.isEmpty()) {

            if (forceCaseInsensitiveComparisons) {
                sqlQuery = SQLConstantManagerFactory.getSQlString("GET_APPLICATIONS_PREFIX_CASESENSITVE_WITHGROUPID");
            } else {
                sqlQuery = SQLConstantManagerFactory.getSQlString("GET_APPLICATIONS_PREFIX_NONE_CASESENSITVE_WITHGROUPID");
            }

        } else {

            if (forceCaseInsensitiveComparisons) {
                sqlQuery = SQLConstantManagerFactory.getSQlString("GET_APPLICATIONS_PREFIX_CASESENSITVE");
            } else {
                sqlQuery = SQLConstantManagerFactory.getSQlString("GET_APPLICATIONS_PREFIX_NONE_CASESENSITVE");
            }

        }

        try {
            connection = APIMgtDBUtil.getConnection();

            sqlQuery = sqlQuery.replace("$1", sortColumn);
            sqlQuery = sqlQuery.replace("$2", sortOrder);

            prepStmt = connection.prepareStatement(sqlQuery);

            if (groupingId != null && !"null".equals(groupingId) && !groupingId.isEmpty()) {
                prepStmt.setString(1, groupingId);
                prepStmt.setString(2, subscriber.getName());
                prepStmt.setString(3, "%"+search+"%");
                //prepStmt.setString(4, sortColumn + " " + sortOrder);
                prepStmt.setInt(4, start);
                prepStmt.setInt(5, offset);

            } else {
                prepStmt.setString(1, subscriber.getName());
                prepStmt.setString(2, "%"+search+"%");
                //prepStmt.setString(3, sortColumn + " " + sortOrder);
                prepStmt.setInt(3, start);
                prepStmt.setInt(4, offset);
            }
            rs = prepStmt.executeQuery();
            ArrayList<Application> applicationsList = new ArrayList<Application>();
            Application application;
            while (rs.next()) {
                application = new Application(rs.getString("NAME"), subscriber);
                application.setId(rs.getInt("APPLICATION_ID"));
                application.setTier(rs.getString("APPLICATION_TIER"));
                application.setCallbackUrl(rs.getString("CALLBACK_URL"));
                application.setDescription(rs.getString("DESCRIPTION"));
                application.setStatus(rs.getString("APPLICATION_STATUS"));
                application.setGroupId(rs.getString("GROUP_ID"));
                application.setUUID(rs.getString("UUID"));
                application.setIsBlackListed(rs.getBoolean("ENABLED"));

                Set<APIKey> keys = getApplicationKeys(subscriber.getName(), application.getId());
                Map<String, OAuthApplicationInfo> keyMap = getOAuthApplications(application.getId());

                for (Map.Entry<String, OAuthApplicationInfo> entry : keyMap.entrySet()) {
                    application.addOAuthApp(entry.getKey(), entry.getValue());
                }

                for (APIKey key : keys) {
                    application.addKey(key);
                }
                applicationsList.add(application);

            }

            applications = applicationsList.toArray(new Application[applicationsList.size()]);
        } catch (SQLException e) {
            handleException("Error when reading the application information from" + " the persistence store.", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
        }
        return applications;
    }

    public Application[] getApplications(Subscriber subscriber, String groupingId) throws APIManagementException {

        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        Application[] applications = null;
        String sqlQuery = SQLConstants.GET_APPLICATIONS_PREFIX;

        String whereClauseWithGroupId;

        if (forceCaseInsensitiveComparisons) {
            whereClauseWithGroupId = "   AND " + "     (GROUP_ID= ? " + "      OR " + "     (GROUP_ID='' AND LOWER" +
                                     "(SUB.USER_ID) = LOWER(?)))";
        } else {
            whereClauseWithGroupId = "   AND " + "     (GROUP_ID= ? " + "      OR " + "     (GROUP_ID='' AND SUB" +
                                     ".USER_ID=?))";
        }

        String whereClause;
        if (forceCaseInsensitiveComparisons) {
            whereClause = "   AND " + " LOWER(SUB.USER_ID) = LOWER(?)";
        } else {
            whereClause = "   AND " + " SUB.USER_ID = ?";
        }

        if (groupingId != null && !"null".equals(groupingId) && !groupingId.isEmpty()) {
            sqlQuery += whereClauseWithGroupId;
        } else {
            sqlQuery += whereClause;
        }
        try {
            connection = APIMgtDBUtil.getConnection();
            String blockingFilerSql = null;
            if (connection.getMetaData().getDriverName().contains("MS SQL") ||
                connection.getMetaData().getDriverName().contains("Microsoft")) {
                sqlQuery = sqlQuery.replaceAll("NAME", "cast(NAME as varchar(100)) collate SQL_Latin1_General_CP1_CI_AS "
                        + "as NAME");
                blockingFilerSql = " select distinct x.*,bl.ENABLED from ( "+sqlQuery+" )x left join AM_BLOCK_CONDITIONS bl "
                        + "on  ( bl.TYPE = 'APPLICATION' AND bl.VALUE = (x.USER_ID + ':') + x.name)";
            }else {
                blockingFilerSql = " select distinct x.*,bl.ENABLED from ( " + sqlQuery
                        + " )x left join AM_BLOCK_CONDITIONS bl on  ( bl.TYPE = 'APPLICATION' AND bl.VALUE = "
                        + "concat(concat(x.USER_ID,':'),x.name))";
            }
            prepStmt = connection.prepareStatement(blockingFilerSql);
            
            if (groupingId != null && !"null".equals(groupingId) && !groupingId.isEmpty()) {
                prepStmt.setString(1, groupingId);
                prepStmt.setString(2, subscriber.getName());
            } else {
                prepStmt.setString(1, subscriber.getName());
            }
            rs = prepStmt.executeQuery();
            ArrayList<Application> applicationsList = new ArrayList<Application>();
            Application application;
            while (rs.next()) {
                application = new Application(rs.getString("NAME"), subscriber);
                application.setId(rs.getInt("APPLICATION_ID"));
                application.setTier(rs.getString("APPLICATION_TIER"));
                application.setCallbackUrl(rs.getString("CALLBACK_URL"));
                application.setDescription(rs.getString("DESCRIPTION"));
                application.setStatus(rs.getString("APPLICATION_STATUS"));
                application.setGroupId(rs.getString("GROUP_ID"));
                application.setUUID(rs.getString("UUID"));
                application.setIsBlackListed(rs.getBoolean("ENABLED"));

                Set<APIKey> keys = getApplicationKeys(subscriber.getName(), application.getId());
                Map<String, OAuthApplicationInfo> keyMap = getOAuthApplications(application.getId());

                for (Map.Entry<String, OAuthApplicationInfo> entry : keyMap.entrySet()) {
                    application.addOAuthApp(entry.getKey(), entry.getValue());
                }

                for (APIKey key : keys) {
                    application.addKey(key);
                }
                applicationsList.add(application);

            }
            Collections.sort(applicationsList, new Comparator<Application>() {
                public int compare(Application o1, Application o2) {
                    return o1.getName().compareToIgnoreCase(o2.getName());
                }
            });
            applications = applicationsList.toArray(new Application[applicationsList.size()]);
        } catch (SQLException e) {
            handleException("Error when reading the application information from" + " the persistence store.", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
        }
        return applications;
    }

    /**
     * Returns all the consumerkeys of application which are subscribed for the given api
     *
     * @param identifier APIIdentifier
     * @return Consumerkeys
     * @throws org.wso2.carbon.apimgt.api.APIManagementException if failed to get Applications for given subscriber.
     */
    public String[] getConsumerKeys(APIIdentifier identifier) throws APIManagementException {

        Set<String> consumerKeys = new HashSet<String>();

        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;

        int apiId;
        String sqlQuery = SQLConstants.GET_CONSUMER_KEYS_SQL;

        try {

            connection = APIMgtDBUtil.getConnection();
            apiId = getAPIID(identifier, connection);

            prepStmt = connection.prepareStatement(sqlQuery);
            prepStmt.setInt(1, apiId);
            rs = prepStmt.executeQuery();

            while (rs.next()) {
                consumerKeys.add(rs.getString("CONSUMER_KEY"));
            }

        } catch (SQLException e) {
            handleException("Error when reading application subscription information", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
        }

        return consumerKeys.toArray(new String[consumerKeys.size()]);
    }

    /**
     * Deletes an Application along with subscriptions, keys and registration data
     *
     * @param application Application object to be deleted from the database which has the application Id
     * @throws APIManagementException
     */
    public void deleteApplication(Application application) throws APIManagementException {
        Connection connection = null;
        PreparedStatement deleteMappingQuery = null;
        PreparedStatement prepStmt = null;
        PreparedStatement prepStmtGetConsumerKey = null;
        PreparedStatement deleteRegistrationQuery = null;
        PreparedStatement deleteSubscription = null;
        PreparedStatement deleteDomainApp = null;
        PreparedStatement deleteAppKey = null;
        PreparedStatement deleteApp = null;
        ResultSet rs = null;

        String getSubscriptionsQuery = SQLConstants.GET_SUBSCRIPTION_ID_OF_APPLICATION_SQL;

        String getConsumerKeyQuery = SQLConstants.GET_CONSUMER_KEY_OF_APPLICATION_SQL;

        String deleteKeyMappingQuery = SQLConstants.REMOVE_APPLICATION_FROM_SUBSCRIPTION_KEY_MAPPINGS_SQL;
        String deleteSubscriptionsQuery = SQLConstants.REMOVE_APPLICATION_FROM_SUBSCRIPTIONS_SQL;
        String deleteApplicationKeyQuery = SQLConstants.REMOVE_APPLICATION_FROM_APPLICATION_KEY_MAPPINGS_SQL;
        String deleteDomainAppQuery = SQLConstants.REMOVE_APPLICATION_FROM_DOMAIN_MAPPINGS_SQL;
        String deleteApplicationQuery = SQLConstants.REMOVE_APPLICATION_FROM_APPLICATIONS_SQL;
        String deleteRegistrationEntry = SQLConstants.REMOVE_APPLICATION_FROM_APPLICATION_REGISTRATIONS_SQL;

        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            prepStmt = connection.prepareStatement(getSubscriptionsQuery);
            prepStmt.setInt(1, application.getId());
            rs = prepStmt.executeQuery();

            List<Integer> subscriptions = new ArrayList<Integer>();
            while (rs.next()) {
                subscriptions.add(rs.getInt("SUBSCRIPTION_ID"));
            }

            deleteMappingQuery = connection.prepareStatement(deleteKeyMappingQuery);
            for (Integer subscriptionId : subscriptions) {
                deleteMappingQuery.setInt(1, subscriptionId);
                deleteMappingQuery.addBatch();
            }
            deleteMappingQuery.executeBatch();

            if (log.isDebugEnabled()) {
                log.debug("Subscription Key mapping details are deleted successfully for Application - " +
                          application.getName());
            }

            deleteRegistrationQuery = connection.prepareStatement(deleteRegistrationEntry);
            deleteRegistrationQuery.setInt(1, application.getId());
            deleteRegistrationQuery.execute();

            if (log.isDebugEnabled()) {
                log.debug("Application Registration details are deleted successfully for Application - " +
                          application.getName());
            }

            deleteSubscription = connection.prepareStatement(deleteSubscriptionsQuery);
            deleteSubscription.setInt(1, application.getId());
            deleteSubscription.execute();

            if (log.isDebugEnabled()) {
                log.debug("Subscription details are deleted successfully for Application - " + application.getName());
            }

            prepStmtGetConsumerKey = connection.prepareStatement(getConsumerKeyQuery);
            prepStmtGetConsumerKey.setInt(1, application.getId());
            rs = prepStmtGetConsumerKey.executeQuery();
            ArrayList<String> consumerKeys = new ArrayList<String>();

            deleteDomainApp = connection.prepareStatement(deleteDomainAppQuery);
            while (rs.next()) {
                String consumerKey = rs.getString("CONSUMER_KEY");

                // This is true when OAuth app has been created by pasting consumer key/secret in the screen.
                String mode = rs.getString("CREATE_MODE");
                if (consumerKey != null) {
                    deleteDomainApp.setString(1, consumerKey);
                    deleteDomainApp.addBatch();

                    KeyManagerHolder.getKeyManagerInstance().deleteMappedApplication(consumerKey);
                    // OAuth app is deleted if only it has been created from API Store. For mapped clients we don't
                    // call delete.
                    if (!"MAPPED".equals(mode)) {
                        // Adding clients to be deleted.
                        consumerKeys.add(consumerKey);
                    }

                }
            }
            deleteDomainApp.executeBatch();

            deleteAppKey = connection.prepareStatement(deleteApplicationKeyQuery);
            deleteAppKey.setInt(1, application.getId());
            deleteAppKey.execute();

            if (log.isDebugEnabled()) {
                log.debug("Application Key Mapping details are deleted successfully for Application - " + application
                        .getName());
            }

            deleteApp = connection.prepareStatement(deleteApplicationQuery);
            deleteApp.setInt(1, application.getId());
            deleteApp.execute();

            if (log.isDebugEnabled()) {
                log.debug("Application " + application.getName() + " is deleted successfully.");
            }

            connection.commit();

            for (String consumerKey : consumerKeys) {
                //delete on oAuthorization server.
                KeyManagerHolder.getKeyManagerInstance().deleteApplication(consumerKey);
            }
        } catch (SQLException e) {
            handleException("Error while removing application details from the database", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmtGetConsumerKey, connection, rs);
            APIMgtDBUtil.closeAllConnections(prepStmt, null, rs);
            APIMgtDBUtil.closeAllConnections(deleteApp, null, null);
            APIMgtDBUtil.closeAllConnections(deleteAppKey, null, null);
            APIMgtDBUtil.closeAllConnections(deleteMappingQuery, null, null);
            APIMgtDBUtil.closeAllConnections(deleteRegistrationQuery, null, null);
            APIMgtDBUtil.closeAllConnections(deleteSubscription, null, null);
            APIMgtDBUtil.closeAllConnections(deleteDomainApp, null, null);
            APIMgtDBUtil.closeAllConnections(deleteAppKey, null, null);
            APIMgtDBUtil.closeAllConnections(deleteApp, null, null);

        }
    }

    public APIKey[] getConsumerKeysWithMode(int appId, String mode) throws APIManagementException {

        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        ArrayList<APIKey> consumerKeys = new ArrayList<APIKey>();

        String getConsumerKeyQuery = SQLConstants.GET_CONSUMER_KEY_WITH_MODE_SLQ;

        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            prepStmt = connection.prepareStatement(getConsumerKeyQuery);
            prepStmt.setInt(1, appId);
            prepStmt.setString(2, mode);
            rs = prepStmt.executeQuery();
            while (rs.next()) {
                String consumerKey = rs.getString("CONSUMER_KEY");

                if (consumerKey != null && !consumerKey.isEmpty()) {
                    APIKey apiKey = new APIKey();
                    apiKey.setConsumerKey(consumerKey);
                    apiKey.setType(rs.getString("KEY_TYPE"));
                    consumerKeys.add(apiKey);
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while getting consumer keys";
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
        }
        return consumerKeys.toArray(new APIKey[consumerKeys.size()]);
    }

    /**
     * Returns the consumer Key for a given Application Name, Subscriber Name, Key Type, Grouping Id combination.
     *
     * @param applicationName Name of the Application.
     * @param subscriberId    Name of Subscriber.
     * @param keyType         PRODUCTION | SANDBOX.
     * @param groupingId      Grouping ID. When set to null query will be performed using the other three values.
     * @return Consumer Key matching the provided combination.
     * @throws APIManagementException
     */
    public String getConsumerKeyForApplicationKeyType(String applicationName, String subscriberId, String keyType,
                                                      String groupingId) throws APIManagementException {

        String consumerKey = null;
        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        String sqlQuery = SQLConstants.GET_CONSUMER_KEY_FOR_APPLICATION_KEY_TYPE_SQL;
        String whereClauseWithGroupId = "   AND " + "     (APP.GROUP_ID= ? " + "      OR " + "     (APP.GROUP_ID='' AND SUB" +
                                        ".USER_ID=?))";
        String whereClause = "   AND " + " SUB.USER_ID=?";

        if (groupingId != null && !"null".equals(groupingId) && !groupingId.isEmpty()) {
            sqlQuery += whereClauseWithGroupId;
        } else {
            sqlQuery += whereClause;
        }
        try {
            connection = APIMgtDBUtil.getConnection();
            prepStmt = connection.prepareStatement(sqlQuery);
            prepStmt.setString(1, applicationName);
            prepStmt.setString(2, keyType);
            if (groupingId != null && !"null".equals(groupingId) && !groupingId.isEmpty()) {
                prepStmt.setString(3, groupingId);
                prepStmt.setString(4, subscriberId);

            } else {
                prepStmt.setString(3, subscriberId);
            }
            rs = prepStmt.executeQuery();

            while (rs.next()) {
                consumerKey = rs.getString("CONSUMER_KEY");
            }
        } catch (SQLException e) {
            handleException("Error when reading the application information from" + " the persistence store.", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
        }
        return consumerKey;
    }

    /**
     * This method will return a java Map that contains application ID and token type.
     *
     * @param consumerKey consumer key of the oAuth application.
     * @return Map.
     * @throws APIManagementException
     */
    public Map<String, String> getApplicationIdAndTokenTypeByConsumerKey(String consumerKey)
            throws APIManagementException {
        Map<String, String> appIdAndConsumerKey = new HashMap<String, String>();

        if (log.isDebugEnabled()) {
            log.debug("fetching application id and token type by consumer key " + consumerKey);
        }

        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;

        String sqlQuery = SQLConstants.GET_APPLICATION_ID_BY_CONSUMER_KEY_SQL;
        try {
            connection = APIMgtDBUtil.getConnection();

            prepStmt = connection.prepareStatement(sqlQuery);
            prepStmt.setString(1, consumerKey);
            rs = prepStmt.executeQuery();
            while (rs.next()) {
                appIdAndConsumerKey.put("application_id", rs.getString("APPLICATION_ID"));
                appIdAndConsumerKey.put("token_type", rs.getString("KEY_TYPE"));
            }
        } catch (SQLException e) {
            handleException("Error when reading application subscription information", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
        }
        return appIdAndConsumerKey;
    }

    /*
        Delete mapping record by given consumer key
     */
    public void deleteApplicationKeyMappingByConsumerKey(String consumerKey) throws APIManagementException {

        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            String deleteKeyMappingQuery = SQLConstants.DELETE_APPLICATION_KEY_MAPPING_BY_CONSUMER_KEY_SQL;
            if (log.isDebugEnabled()) {
                log.debug("trying to delete key mapping for consumer id " + consumerKey);
            }
            ps = connection.prepareStatement(deleteKeyMappingQuery);
            ps.setString(1, consumerKey);
            ps.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            handleException("Error while removing application mapping table", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, connection, null);
        }
    }

    /**
     * This method will delete a record from AM_APPLICATION_REGISTRATION
     *
     * @param applicationId
     * @param tokenType
     */
    public void deleteApplicationKeyMappingByApplicationIdAndType(String applicationId, String tokenType)
            throws APIManagementException {

        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            String deleteRegistrationEntry = SQLConstants.DELETE_APPLICATION_KEY_MAPPING_BY_APPLICATION_ID_SQL;

            if (log.isDebugEnabled()) {
                log.debug("trying to delete a record from AM_APPLICATION_KEY_MAPPING table by application ID " +
                          applicationId + " and Token type" + tokenType);
            }
            ps = connection.prepareStatement(deleteRegistrationEntry);
            ps.setInt(1, Integer.parseInt(applicationId));
            ps.setString(2, tokenType);
            ps.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            handleException("Error while removing AM_APPLICATION_KEY_MAPPING table", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, connection, null);
        }
    }

    /**
     * Delete a record from AM_APPLICATION_REGISTRATION table by application ID and token type.
     *
     * @param applicationId APIM application ID.
     * @param tokenType     Token type (PRODUCTION || SANDBOX)
     * @throws APIManagementException if failed to delete the record.
     */
    public void deleteApplicationRegistration(String applicationId, String tokenType) throws APIManagementException {

        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = APIMgtDBUtil.getConnection();
            String deleteRegistrationEntry = SQLConstants.REMOVE_FROM_APPLICATION_REGISTRANTS_SQL;

            if (log.isDebugEnabled()) {
                log.debug("trying to delete a record from AM_APPLICATION_REGISTRATION table by application ID " +
                          applicationId + " and Token type" + tokenType);
            }
            ps = connection.prepareStatement(deleteRegistrationEntry);
            ps.setInt(1, Integer.parseInt(applicationId));
            ps.setString(2, tokenType);
            ps.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            handleException("Error while removing AM_APPLICATION_REGISTRATION table", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, connection, null);
        }
    }

    /**
     * returns a subscriber record for given username,tenant Id
     *
     * @param username   UserName
     * @param tenantId   Tenant Id
     * @param connection
     * @return Subscriber
     * @throws APIManagementException if failed to get subscriber
     */
    private Subscriber getSubscriber(String username, int tenantId, Connection connection)
            throws APIManagementException {
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        Subscriber subscriber = null;
        String sqlQuery;

        if (forceCaseInsensitiveComparisons) {
            sqlQuery = SQLConstants.GET_SUBSCRIBER_CASE_INSENSITIVE_SQL;
        } else {
            sqlQuery = SQLConstants.GET_SUBSCRIBER_DETAILS_SQL;
        }

        try {
            prepStmt = connection.prepareStatement(sqlQuery);
            prepStmt.setString(1, username);
            prepStmt.setInt(2, tenantId);
            rs = prepStmt.executeQuery();

            if (rs.next()) {
                subscriber = new Subscriber(rs.getString("USER_ID"));
                subscriber.setEmail(rs.getString("EMAIL_ADDRESS"));
                subscriber.setId(rs.getInt("SUBSCRIBER_ID"));
                subscriber.setSubscribedDate(rs.getDate("DATE_SUBSCRIBED"));
                subscriber.setTenantId(rs.getInt("TENANT_ID"));
                return subscriber;
            }
        } catch (SQLException e) {
            handleException("Error when reading the application information from" + " the persistence store.", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, null, rs);
        }
        return subscriber;
    }

    public void recordAPILifeCycleEvent(APIIdentifier identifier, APIStatus oldStatus, APIStatus newStatus,
            String userId, int tenantId) throws APIManagementException {
        Connection conn = null;
        try {
            conn = APIMgtDBUtil.getConnection();
            recordAPILifeCycleEvent(identifier, oldStatus.toString(), newStatus.toString(), userId, tenantId, conn);
        } catch (SQLException e) {
            handleException("Failed to record API state change", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(null, conn, null);
        }
    }

    public void recordAPILifeCycleEvent(APIIdentifier identifier, String oldStatus, String newStatus, String userId,
            int tenantId) throws APIManagementException {
        Connection conn = null;
        try {
            conn = APIMgtDBUtil.getConnection();
            recordAPILifeCycleEvent(identifier, oldStatus, newStatus, userId, tenantId, conn);
        } catch (SQLException e) {
            handleException("Failed to record API state change", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(null, conn, null);
        }
    }

    public void recordAPILifeCycleEvent(APIIdentifier identifier, String oldStatus, String newStatus, String userId,
            int tenantId, Connection conn) throws APIManagementException {
        //Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        PreparedStatement selectQuerySt = null;

        int apiId = -1;

        if (oldStatus == null && !newStatus.equals(APIStatus.CREATED.toString())) {
            String msg = "Invalid old and new state combination";
            log.error(msg);
            throw new APIManagementException(msg);
        } else if (oldStatus != null && oldStatus.equals(newStatus)) {
            String msg = "No measurable differences in API state";
            log.error(msg);
            throw new APIManagementException(msg);
        }

        String getAPIQuery = SQLConstants.GET_API_ID_SQL;
        String sqlQuery = SQLConstants.ADD_API_LIFECYCLE_EVENT_SQL;
        try {
            conn.setAutoCommit(false);

            selectQuerySt = conn.prepareStatement(getAPIQuery);
            selectQuerySt.setString(1, APIUtil.replaceEmailDomainBack(identifier.getProviderName()));
            selectQuerySt.setString(2, identifier.getApiName());
            selectQuerySt.setString(3, identifier.getVersion());
            resultSet = selectQuerySt.executeQuery();
            if (resultSet.next()) {
                apiId = resultSet.getInt("API_ID");
            }

            if (apiId == -1) {
                String msg = "Unable to find the API: " + identifier + " in the database";
                log.error(msg);
                throw new APIManagementException(msg);
            }

            ps = conn.prepareStatement(sqlQuery);
            ps.setInt(1, apiId);
            if (oldStatus != null) {
                ps.setString(2, oldStatus);
            } else {
                ps.setNull(2, Types.VARCHAR);
            }
            ps.setString(3, newStatus);
            ps.setString(4, userId);
            ps.setInt(5, tenantId);
            ps.setTimestamp(6, new Timestamp(System.currentTimeMillis()));

            ps.executeUpdate();

            // finally commit transaction
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error("Failed to rollback the API state change record", e1);
                }
            }
            handleException("Failed to record API state change", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(selectQuerySt, null, null);
            APIMgtDBUtil.closeAllConnections(ps, null, resultSet);
        }
    }

    public void updateDefaultAPIPublishedVersion(APIIdentifier identifier, APIStatus oldStatus, APIStatus newStatus)
            throws APIManagementException {

        Connection conn = null;
        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);

            if (!oldStatus.equals(newStatus)) {
                if ((newStatus.equals(APIStatus.CREATED) || newStatus.equals(APIStatus.RETIRED)) && (oldStatus.equals
                        (APIStatus.PUBLISHED) || oldStatus.equals(APIStatus.DEPRECATED) || oldStatus.equals(APIStatus.BLOCKED))) {
                    setPublishedDefVersion(identifier, conn, null);
                } else if (newStatus.equals(APIStatus.PUBLISHED) || newStatus.equals(APIStatus.DEPRECATED) ||
                           newStatus.equals(APIStatus.BLOCKED)) {
                    setPublishedDefVersion(identifier, conn, identifier.getVersion());
                }
            }

            conn.commit();

        } catch (SQLException e) {
            handleException("Failed to update published default API state change", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(null, conn, null);
        }
    }

    public List<LifeCycleEvent> getLifeCycleEvents(APIIdentifier apiId) throws APIManagementException {
        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        String sqlQuery = SQLConstants.GET_LIFECYCLE_EVENT_SQL;

        List<LifeCycleEvent> events = new ArrayList<LifeCycleEvent>();
        try {
            connection = APIMgtDBUtil.getConnection();
            prepStmt = connection.prepareStatement(sqlQuery);
            prepStmt.setString(1, APIUtil.replaceEmailDomainBack(apiId.getProviderName()));
            prepStmt.setString(2, apiId.getApiName());
            prepStmt.setString(3, apiId.getVersion());
            rs = prepStmt.executeQuery();

            while (rs.next()) {
                LifeCycleEvent event = new LifeCycleEvent();
                event.setApi(apiId);
                String oldState = rs.getString("PREVIOUS_STATE");
                //event.setOldStatus(oldState != null ? APIStatus.valueOf(oldState) : null);
                event.setOldStatus(oldState != null ? oldState : null);
                //event.setNewStatus(APIStatus.valueOf(rs.getString("NEW_STATE")));
                event.setNewStatus(rs.getString("NEW_STATE"));
                event.setUserId(rs.getString("USER_ID"));
                event.setDate(rs.getTimestamp("EVENT_DATE"));
                events.add(event);
            }

            Collections.sort(events, new Comparator<LifeCycleEvent>() {
                public int compare(LifeCycleEvent o1, LifeCycleEvent o2) {
                    return o1.getDate().compareTo(o2.getDate());
                }
            });
        } catch (SQLException e) {
            handleException("Error when executing the SQL : " + sqlQuery, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
        }
        return events;
    }

    public void makeKeysForwardCompatible(String provider, String apiName, String oldVersion, String newVersion,
                                          String context) throws APIManagementException {

        Connection connection = null;
        PreparedStatement prepStmt = null;
        PreparedStatement addSubKeySt = null;
        PreparedStatement getAppSt = null;
        ResultSet rs = null;

        String getSubscriptionDataQuery = SQLConstants.GET_SUBSCRIPTION_DATA_SQL;
        String addSubKeyMapping = SQLConstants.ADD_SUBSCRIPTION_KEY_MAPPING_SQL;
        String getApplicationDataQuery = SQLConstants.GET_APPLICATION_DATA_SQL;

        try {
            // Retrieve all the existing subscription for the old version
            connection = APIMgtDBUtil.getConnection();

            prepStmt = connection.prepareStatement(getSubscriptionDataQuery);
            prepStmt.setString(1, APIUtil.replaceEmailDomainBack(provider));
            prepStmt.setString(2, apiName);
            prepStmt.setString(3, oldVersion);
            rs = prepStmt.executeQuery();

            List<SubscriptionInfo> subscriptionData = new ArrayList<SubscriptionInfo>();
            Set<Integer> subscribedApplications = new HashSet<Integer>();
            while (rs.next() && !(APIConstants.SubscriptionStatus.ON_HOLD.equals(rs.getString("SUB_STATUS")))) {
                SubscriptionInfo info = new SubscriptionInfo();
                info.subscriptionId = rs.getInt("SUBSCRIPTION_ID");
                info.tierId = rs.getString("TIER_ID");
                info.applicationId = rs.getInt("APPLICATION_ID");
                info.accessToken = rs.getString("ACCESS_TOKEN");  // no decryption needed.
                info.tokenType = rs.getString("KEY_TYPE");
                subscriptionData.add(info);
            }

            Map<Integer, Integer> subscriptionIdMap = new HashMap<Integer, Integer>();
            APIIdentifier apiId = new APIIdentifier(provider, apiName, newVersion);

            for (SubscriptionInfo info : subscriptionData) {
                try {
                    if (!subscriptionIdMap.containsKey(info.subscriptionId)) {
                        apiId.setTier(info.tierId);
                        int subscriptionId = addSubscription(apiId, context, info.applicationId, APIConstants
                                .SubscriptionStatus.UNBLOCKED, provider);
                        if (subscriptionId == -1) {
                            String msg = "Unable to add a new subscription for the API: " + apiName +
                                         ":v" + newVersion;
                            log.error(msg);
                            throw new APIManagementException(msg);
                        }
                        subscriptionIdMap.put(info.subscriptionId, subscriptionId);
                    }
                    int subscriptionId = subscriptionIdMap.get(info.subscriptionId);
                    connection.setAutoCommit(false);

                    addSubKeySt = connection.prepareStatement(addSubKeyMapping);
                    addSubKeySt.setInt(1, subscriptionId);
                    addSubKeySt.setString(2, info.accessToken);
                    addSubKeySt.setString(3, info.tokenType);
                    addSubKeySt.execute();
                    connection.commit();

                    subscribedApplications.add(info.applicationId);
                    // catching the exception because when copy the api without the option "require re-subscription"
                    // need to go forward rather throwing the exception
                } catch (SubscriptionAlreadyExistingException e) {
                    log.error("Error while adding subscription " + e.getMessage(), e);
                }
            }

            getAppSt = connection.prepareStatement(getApplicationDataQuery);
            getAppSt.setString(1, APIUtil.replaceEmailDomainBack(provider));
            getAppSt.setString(2, apiName);
            getAppSt.setString(3, oldVersion);
            rs = getAppSt.executeQuery();
            while (rs.next() && !(APIConstants.SubscriptionStatus.ON_HOLD.equals(rs.getString("SUB_STATUS")))) {
                int applicationId = rs.getInt("APPLICATION_ID");
                if (!subscribedApplications.contains(applicationId)) {
                    apiId.setTier(rs.getString("TIER_ID"));
                    try {
                        addSubscription(apiId, rs.getString("CONTEXT"), applicationId, APIConstants
                                .SubscriptionStatus.UNBLOCKED, provider);
                        // catching the exception because when copy the api without the option "require re-subscription"
                        // need to go forward rather throwing the exception
                    } catch (SubscriptionAlreadyExistingException e) {
                        //Not handled as an error because same subscription can be there in many previous versions. 
                        //Ex: if previous version was created by another older version and if the subscriptions are
                        //Forwarded, then the third one will get same subscription from previous two versions.
                        log.info("Subscription already exists: " + e.getMessage());
                    }
                }
            }
        } catch (SQLException e) {
            handleException("Error when executing the SQL queries", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(getAppSt, null, null);
            APIMgtDBUtil.closeAllConnections(addSubKeySt, null, null);
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
        }
    }

    public void addAPI(API api, int tenantId) throws APIManagementException {
        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;

        String query = SQLConstants.ADD_API_SQL;
        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);

            prepStmt = connection.prepareStatement(query, new String[]{"api_id"});
            prepStmt.setString(1, APIUtil.replaceEmailDomainBack(api.getId().getProviderName()));
            prepStmt.setString(2, api.getId().getApiName());
            prepStmt.setString(3, api.getId().getVersion());
            prepStmt.setString(4, api.getContext());
            String contextTemplate = api.getContextTemplate();
            //Validate if the API has an unsupported context before executing the query
            String invalidContext = "/" + APIConstants.VERSION_PLACEHOLDER;
            if (invalidContext.equals(contextTemplate)) {
                throw new APIManagementException("Cannot add API : " + api.getId() + " with unsupported context : "
                        + contextTemplate);
            }
            //If the context template ends with {version} this means that the version will be at the end of the context.
            if (contextTemplate.endsWith("/" + APIConstants.VERSION_PLACEHOLDER)) {
                //Remove the {version} part from the context template.
                contextTemplate = contextTemplate.split(Pattern.quote("/" + APIConstants.VERSION_PLACEHOLDER))[0];
            }
            prepStmt.setString(5, contextTemplate);
            prepStmt.setString(6, APIUtil.replaceEmailDomainBack(api.getId().getProviderName()));
            prepStmt.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
            prepStmt.setString(8, api.getApiLevelPolicy());
            prepStmt.execute();

            rs = prepStmt.getGeneratedKeys();
            int applicationId = -1;
            if (rs.next()) {
                applicationId = rs.getInt(1);
            }

            connection.commit();

            if (api.getScopes() != null) {
                addScopes(api.getScopes(), applicationId, tenantId);
            }
            addURLTemplates(applicationId, api, connection);
            APIIdentifier apiIdentifier =
                    new APIIdentifier(APIUtil.replaceEmailDomainBack(api.getId().getProviderName()),
                            api.getId().getApiName(), api.getId().getVersion());
            //add new APIGatewayUrls
            addAPIEnvironments(apiIdentifier, api);
            String tenantUserName = MultitenantUtils
                    .getTenantAwareUsername(APIUtil.replaceEmailDomainBack(api.getId().getProviderName()));
            recordAPILifeCycleEvent(api.getId(), null, APIStatus.CREATED.toString(), tenantUserName, tenantId,
                    connection);
            //If the api is selected as default version, it is added/replaced into AM_API_DEFAULT_VERSION table
            if (api.isDefaultVersion()) {
                addUpdateAPIAsDefaultVersion(api, connection);
            }
            connection.commit();
        } catch (SQLException e) {
            handleException("Error while adding the API: " + api.getId() + " to the database", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
        }
    }
    /**
     * Persists Environment details of the API to the Database     *
     * @param apiIdentifier API Identifier
     * @param api API Object
     * @throws APIManagementException
     */
    public void addAPIEnvironments(APIIdentifier apiIdentifier, API api) throws APIManagementException {

        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        String query = SQLConstants.ADD_API_ENVIRONMENTS_SQL;
        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);

            if(api.getEnvironments() != null) {
                Set<String> environments =
                        new HashSet<String>(api.getEnvironments());
                environments.remove("none");

                if (api.getGatewayUrls() != null) {
                    String gatewayUrls = api.getGatewayUrls();
                    JSONParser parser = new JSONParser();
                    Object object  = parser.parse(gatewayUrls);
                    JSONObject gatewayUrlsJson = (JSONObject) object;

                    for (String environmentName : environments) {
                        prepStmt = connection.prepareStatement(query);
                        //set environment name
                        prepStmt.setString(1, environmentName);
                        //set API Id
                        int apiId;
                        apiId = getAPIID(apiIdentifier, connection);
                        if (apiId == -1) {
                            String msg = "Could not load API record for: " + apiIdentifier.getApiName();
                            log.error(msg);
                            throw new APIManagementException(msg);
                        }
                        prepStmt.setInt(2, apiId);
                        //extract URLs
                        String urlsFromPublisher = (String) gatewayUrlsJson.get(environmentName);
                        JSONObject urlsFromPublisherJson = (JSONObject) parser.parse(urlsFromPublisher);

                        //both http and https are default
                        if (urlsFromPublisherJson.get("https").equals("default") && urlsFromPublisherJson.get("http")
                                .equals("default")) {
                            prepStmt.setString(3, "default");
                            prepStmt.setString(4, "default");
                        } else{ //else it should be  : both are not default
                            prepStmt.setString(3, (String) urlsFromPublisherJson.get("http"));
                            prepStmt.setString(4, (String) urlsFromPublisherJson.get("https"));
                        }
                        //set UseDefaultContext
                        if(urlsFromPublisherJson.get("useDefaultContext").equals("true")){
                            prepStmt.setBoolean(5, true);
                        } else {
                            prepStmt.setBoolean(5, false);
                        }
                        prepStmt.execute();
                    }
                }
            }
            connection.commit();

        } catch (SQLException e) {
            handleException("Error while adding the environments of the API: " + api.getId() + " to the database", e);
        } catch (org.json.simple.parser.ParseException e) {
            handleException("Cannot Parse the environments JSON retrieved for API :" + api.getId(), e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
        }
    }
    /**
     * Remove persisted Environments of the API     *
     * @param apiIdentifier API Identifier
     * @throws APIManagementException
     */
    public void RemoveAPIEnvironments(APIIdentifier apiIdentifier) throws APIManagementException {
        Connection connection = null;
        PreparedStatement prepStmt = null;
        int apiId;

        String deleteEnvironments = SQLConstants.REMOVE_API_ENVIRONMENTS_SQL;
        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            prepStmt = connection.prepareStatement(deleteEnvironments);

            apiId = getAPIID(apiIdentifier, connection);
            if (apiId == -1) {
                String msg = "Could not load API record for: " + apiIdentifier.getApiName();
                log.error(msg);
                throw new APIManagementException(msg);
            }
            prepStmt.setInt(1, apiId);
            prepStmt.execute();
            connection.commit();
        } catch (SQLException e) {
            handleException("Error while deleting environments for API : " + apiIdentifier.getApiName(), e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, null);
        }
    }
    /**
     * Retrive Environments of the API     *
     * @param apiIdentifier
     * @param apiId
     * @throws APIManagementException
     */
    public String getAPIEnvironmentUrls(APIIdentifier apiIdentifier, int apiId) throws APIManagementException {

        Connection connection = null;
        PreparedStatement prepStmt = null;
        String environmentConfig = null;
        ResultSet rs = null;
        String query = SQLConstants.GET_API_ENVIRONMENTS_SQL;
        JSONObject environmentsObject = new JSONObject();

        try {
            connection = APIMgtDBUtil.getConnection();
            prepStmt = connection.prepareStatement(query);
            prepStmt.setInt(1, apiId);
            rs = prepStmt.executeQuery();

            while (rs.next()) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("https", rs.getString("HTTPS_URL"));
                jsonObject.put("http", rs.getString("HTTP_URL"));
                jsonObject.put("useDefaultContext", rs.getBoolean("APPEND_CONTEXT"));
                environmentsObject.put(rs.getString("ENVIRONMENT_NAME"),jsonObject);
            }

        } catch (SQLException e) {
            handleException("Error while getting the Environment Config URLs for " + apiIdentifier.getApiName(), e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
        }
        if(environmentsObject.size()!= 0){
            environmentConfig = environmentsObject.toString();
        }
        return environmentConfig;
    }

    public String getDefaultVersion(APIIdentifier apiId) throws APIManagementException {
        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        String oldDefaultVersion = null;

        String query = SQLConstants.GET_DEFAULT_VERSION_SQL;
        try {

            connection = APIMgtDBUtil.getConnection();
            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, apiId.getApiName());
            prepStmt.setString(2, APIUtil.replaceEmailDomainBack(apiId.getProviderName()));

            rs = prepStmt.executeQuery();

            if (rs.next()) {
                oldDefaultVersion = rs.getString("DEFAULT_API_VERSION");
            }
        } catch (SQLException e) {
            handleException("Error while getting default version for " + apiId.getApiName(), e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
        }
        return oldDefaultVersion;
    }

    /**
     * Persists WorkflowDTO to Database
     *
     * @param workflow
     * @throws APIManagementException
     */
    public void addWorkflowEntry(WorkflowDTO workflow) throws APIManagementException {
        Connection connection = null;
        PreparedStatement prepStmt = null;

        String query = SQLConstants.ADD_WORKFLOW_ENTRY_SQL;
        try {
            Timestamp cratedDateStamp = new Timestamp(workflow.getCreatedTime());

            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);

            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, workflow.getWorkflowReference());
            prepStmt.setString(2, workflow.getWorkflowType());
            prepStmt.setString(3, workflow.getStatus().toString());
            prepStmt.setTimestamp(4, cratedDateStamp);
            prepStmt.setString(5, workflow.getWorkflowDescription());
            prepStmt.setInt(6, workflow.getTenantId());
            prepStmt.setString(7, workflow.getTenantDomain());
            prepStmt.setString(8, workflow.getExternalWorkflowReference());

            prepStmt.execute();

            connection.commit();
        } catch (SQLException e) {
            handleException("Error while adding Workflow : " + workflow.getExternalWorkflowReference() + " to the " +
                            "database", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, null);
        }
    }

    public void updateWorkflowStatus(WorkflowDTO workflowDTO) throws APIManagementException {
        Connection connection = null;
        PreparedStatement prepStmt = null;

        String query = SQLConstants.UPDATE_WORKFLOW_ENTRY_SQL;
        try {
            Timestamp updatedTimeStamp = new Timestamp(workflowDTO.getUpdatedTime());

            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);

            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, workflowDTO.getStatus().toString());
            prepStmt.setString(2, workflowDTO.getWorkflowDescription());
            prepStmt.setTimestamp(3, updatedTimeStamp);
            prepStmt.setString(4, workflowDTO.getExternalWorkflowReference());

            prepStmt.execute();

            connection.commit();
        } catch (SQLException e) {
            handleException("Error while updating Workflow Status of workflow " + workflowDTO
                    .getExternalWorkflowReference(), e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, null);
        }
    }

    /**
     * Returns a workflow object for a given external workflow reference.
     *
     * @param workflowReference
     * @return
     * @throws APIManagementException
     */
    public WorkflowDTO retrieveWorkflow(String workflowReference) throws APIManagementException {
        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        WorkflowDTO workflowDTO = null;

        String query = SQLConstants.GET_ALL_WORKFLOW_ENTRY_SQL;
        try {
            connection = APIMgtDBUtil.getConnection();
            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, workflowReference);

            rs = prepStmt.executeQuery();
            while (rs.next()) {
                workflowDTO = WorkflowExecutorFactory.getInstance().createWorkflowDTO(rs.getString("WF_TYPE"));
                workflowDTO.setStatus(WorkflowStatus.valueOf(rs.getString("WF_STATUS")));
                workflowDTO.setExternalWorkflowReference(rs.getString("WF_EXTERNAL_REFERENCE"));
                workflowDTO.setCreatedTime(rs.getTimestamp("WF_CREATED_TIME").getTime());
                workflowDTO.setWorkflowReference(rs.getString("WF_REFERENCE"));
                workflowDTO.setTenantDomain(rs.getString("TENANT_DOMAIN"));
                workflowDTO.setTenantId(rs.getInt("TENANT_ID"));
                workflowDTO.setWorkflowDescription(rs.getString("WF_STATUS_DESC"));
            }
        } catch (SQLException e) {
            handleException("Error while retrieving workflow details for " + workflowReference, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
        }
        return workflowDTO;
    }

    private void setPublishedDefVersion(APIIdentifier apiId, Connection connection, String value)
            throws APIManagementException {
        String queryDefaultVersionUpdate = SQLConstants.UPDATE_PUBLISHED_DEFAULT_VERSION_SQL;

        PreparedStatement prepStmtDefVersionUpdate = null;
        try {
            prepStmtDefVersionUpdate = connection.prepareStatement(queryDefaultVersionUpdate);
            prepStmtDefVersionUpdate.setString(1, value);
            prepStmtDefVersionUpdate.setString(2, apiId.getApiName());
            prepStmtDefVersionUpdate.setString(3, APIUtil.replaceEmailDomainBack(apiId.getProviderName()));
            prepStmtDefVersionUpdate.execute();
        } catch (SQLException e) {
            handleException("Error while deleting the API default version entry: " + apiId.getApiName() + " from the " +
                            "database", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmtDefVersionUpdate, null, null);
        }
    }

    /**
     * Sets/removes default api entry such that api will not represent as default api further.
     * If the api's version is the same as the published version, then the whole entry will be removed.
     * Otherwise only the default version attribute is set to null.
     *
     * @param apiId
     * @param connection
     * @return
     * @throws APIManagementException
     */
    public void removeAPIFromDefaultVersion(APIIdentifier apiId, Connection connection) throws APIManagementException {

        String queryDefaultVersionDelete = SQLConstants.REMOVE_API_DEFAULT_VERSION_SQL;

        PreparedStatement prepStmtDefVersionDelete = null;
        try {
            prepStmtDefVersionDelete = connection.prepareStatement(queryDefaultVersionDelete);
            prepStmtDefVersionDelete.setString(1, apiId.getApiName());
            prepStmtDefVersionDelete.setString(2, APIUtil.replaceEmailDomainBack(apiId.getProviderName()));
            prepStmtDefVersionDelete.execute();
        } catch (SQLException e) {
            handleException("Error while deleting the API default version entry: " + apiId.getApiName() + " from the " +
                            "database", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmtDefVersionDelete, null, null);
        }
    }

    public String getPublishedDefaultVersion(APIIdentifier apiId) throws APIManagementException {
        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        String publishedDefaultVersion = null;

        String query = SQLConstants.GET_PUBLISHED_DEFAULT_VERSION_SQL;
        try {
            connection = APIMgtDBUtil.getConnection();
            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, apiId.getApiName());
            prepStmt.setString(2, APIUtil.replaceEmailDomainBack(apiId.getProviderName()));

            rs = prepStmt.executeQuery();

            while (rs.next()) {
                publishedDefaultVersion = rs.getString("PUBLISHED_DEFAULT_API_VERSION");
            }
        } catch (SQLException e) {
            handleException("Error while getting default version for " + apiId.getApiName(), e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
        }
        return publishedDefaultVersion;
    }

    public void addUpdateAPIAsDefaultVersion(API api, Connection connection) throws APIManagementException {
        String publishedDefaultVersion = getPublishedDefaultVersion(api.getId());
        removeAPIFromDefaultVersion(api.getId(), connection);

        PreparedStatement prepStmtDefVersionAdd = null;
        String queryDefaultVersionAdd = SQLConstants.ADD_API_DEFAULT_VERSION_SQL;
        try {
            prepStmtDefVersionAdd = connection.prepareStatement(queryDefaultVersionAdd);
            prepStmtDefVersionAdd.setString(1, api.getId().getApiName());
            prepStmtDefVersionAdd.setString(2, APIUtil.replaceEmailDomainBack(api.getId().getProviderName()));
            prepStmtDefVersionAdd.setString(3, api.getId().getVersion());
            APIStatus apistatus = api.getStatus();
            if (apistatus.equals(APIStatus.PUBLISHED) || apistatus.equals(APIStatus.DEPRECATED) || apistatus.equals
                    (APIStatus.BLOCKED)) {
                prepStmtDefVersionAdd.setString(4, api.getId().getVersion());
            } else {
                prepStmtDefVersionAdd.setString(4, publishedDefaultVersion);
            }

            prepStmtDefVersionAdd.execute();
        } catch (SQLException e) {
            handleException("Error while adding the API default version entry: " + api.getId().getApiName() + " to " +
                            "the database", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmtDefVersionAdd, null, null);
        }
    }

    /**
     * Adds URI templates define for an API
     *
     * @param apiId
     * @param api
     * @param connection
     * @throws APIManagementException
     */
    public void addURLTemplates(int apiId, API api, Connection connection) throws APIManagementException {
        if (apiId == -1) {
            //application addition has failed
            return;
        }
        PreparedStatement prepStmt = null;
        PreparedStatement scopePrepStmt = null;

        String query = SQLConstants.ADD_URL_MAPPING_SQL;
        String scopeQuery = SQLConstants.ADD_OAUTH2_RESOURCE_SCOPE_SQL;
        try {
            //connection = APIMgtDBUtil.getConnection();
            prepStmt = connection.prepareStatement(query);
            scopePrepStmt = connection.prepareStatement(scopeQuery);

            Iterator<URITemplate> uriTemplateIterator = api.getUriTemplates().iterator();
            URITemplate uriTemplate;
            for (; uriTemplateIterator.hasNext(); ) {
                uriTemplate = uriTemplateIterator.next();

                prepStmt.setInt(1, apiId);
                prepStmt.setString(2, uriTemplate.getHTTPVerb());
                prepStmt.setString(3, uriTemplate.getAuthType());
                prepStmt.setString(4, uriTemplate.getUriTemplate());
                //If API policy is available then set it for all the resources
                if(StringUtils.isEmpty(api.getApiLevelPolicy())) {
                    prepStmt.setString(5, uriTemplate.getThrottlingTier());
                } else {
                    prepStmt.setString(5, api.getApiLevelPolicy());
                }
                InputStream is;
                if (uriTemplate.getMediationScript() != null) {
                    is = new ByteArrayInputStream(uriTemplate.getMediationScript().getBytes(Charset.defaultCharset()));
                } else {
                    is = null;
                }
                if (connection.getMetaData().getDriverName().contains("PostgreSQL") || connection.getMetaData()
                        .getDatabaseProductName().contains("DB2")) {
                    if (uriTemplate.getMediationScript() != null) {
                        prepStmt.setBinaryStream(6, is, uriTemplate.getMediationScript().getBytes(Charset.defaultCharset()).length);
                    } else {
                        prepStmt.setBinaryStream(6, is, 0);
                    }
                } else {
                    prepStmt.setBinaryStream(6, is);
                }
                prepStmt.addBatch();
                if (uriTemplate.getScope() != null) {
                    scopePrepStmt.setString(1, APIUtil.getResourceKey(api, uriTemplate));

                    if (uriTemplate.getScope().getId() == 0) {
                        String scopeKey = uriTemplate.getScope().getKey();
                        Scope scopeByKey = APIUtil.findScopeByKey(api.getScopes(), scopeKey);
                        if (scopeByKey != null) {
                            if (scopeByKey.getId() > 0) {
                                uriTemplate.getScopes().setId(scopeByKey.getId());
                            }
                        }
                    }

                    scopePrepStmt.setInt(2, uriTemplate.getScope().getId());
                    scopePrepStmt.addBatch();
                }
            }
            prepStmt.executeBatch();
            prepStmt.clearBatch();
            scopePrepStmt.executeBatch();
            scopePrepStmt.clearBatch();
        } catch (SQLException e) {
            handleException("Error while adding URL template(s) to the database for API : " + api.getId(), e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, null, null);
            APIMgtDBUtil.closeAllConnections(scopePrepStmt, null, null);
        }
    }

    /**
     * Fetches an Application by name.
     *
     * @param applicationName Name of the Application
     * @param userId          Name of the User.
     * @throws APIManagementException
     */
    public Application getApplicationByName(String applicationName, String userId, String groupId)
            throws APIManagementException {
        //mysql> select APP.APPLICATION_ID, APP.NAME, APP.SUBSCRIBER_ID,APP.APPLICATION_TIER,APP.CALLBACK_URL,APP
        // .DESCRIPTION,
        // APP.APPLICATION_STATUS from AM_SUBSCRIBER as SUB,AM_APPLICATION as APP
        // where SUB.user_id='admin' AND APP.name='DefaultApplication' AND SUB.SUBSCRIBER_ID=APP.SUBSCRIBER_ID;
        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;

        Application application = null;
        try {
            connection = APIMgtDBUtil.getConnection();

            String query = SQLConstants.GET_APPLICATION_BY_NAME_PREFIX;
            String whereClause = "  WHERE SUB.USER_ID =? AND APP.NAME=? AND SUB.SUBSCRIBER_ID=APP.SUBSCRIBER_ID";
            String whereClauseCaseInSensitive = "  WHERE LOWER(SUB.USER_ID) =LOWER(?) AND APP.NAME=? AND SUB" + "" +
                                                ".SUBSCRIBER_ID=APP.SUBSCRIBER_ID";
            String whereClauseWithGroupId = "  WHERE  (APP.GROUP_ID = ? OR (APP.GROUP_ID = '' AND SUB.USER_ID = ?)) " +
                                            "AND " + "APP.NAME = ? AND SUB.SUBSCRIBER_ID = APP.SUBSCRIBER_ID";

            if (groupId != null && !"null".equals(groupId) && !groupId.isEmpty()) {
                query += whereClauseWithGroupId;
            } else {
                if (forceCaseInsensitiveComparisons) {
                    query = query + whereClauseCaseInSensitive;
                } else {
                    query = query + whereClause;
                }
            }

            prepStmt = connection.prepareStatement(query);

            if (groupId != null && !"null".equals(groupId) && !groupId.isEmpty()) {
                prepStmt.setString(1, groupId);
                prepStmt.setString(2, userId);
                prepStmt.setString(3, applicationName);
            } else {
                prepStmt.setString(1, userId);
                prepStmt.setString(2, applicationName);
            }

            rs = prepStmt.executeQuery();
            while (rs.next()) {
                String subscriberId = rs.getString("SUBSCRIBER_ID");
                String subscriberName = rs.getString("USER_ID");

                Subscriber subscriber = new Subscriber(subscriberName);
                subscriber.setId(Integer.parseInt(subscriberId));
                application = new Application(applicationName, subscriber);

                application.setDescription(rs.getString("DESCRIPTION"));
                application.setStatus(rs.getString("APPLICATION_STATUS"));
                application.setCallbackUrl(rs.getString("CALLBACK_URL"));
                application.setId(rs.getInt("APPLICATION_ID"));
                application.setTier(rs.getString("APPLICATION_TIER"));
                application.setUUID(rs.getString("UUID"));
                application.setGroupId(rs.getString("GROUP_ID"));
            }
        } catch (SQLException e) {
            handleException("Error while obtaining details of the Application : " + applicationName, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
        }
        return application;
    }

    public Application getApplicationById(int applicationId) throws APIManagementException {
        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;

        Application application = null;
        try {
            connection = APIMgtDBUtil.getConnection();

            String query = SQLConstants.GET_APPLICATION_BY_ID_SQL;
            prepStmt = connection.prepareStatement(query);
            prepStmt.setInt(1, applicationId);

            rs = prepStmt.executeQuery();
            if (rs.next()) {
                String applicationName = rs.getString("NAME");
                String subscriberId = rs.getString("SUBSCRIBER_ID");
                String subscriberName = rs.getString("USER_ID");

                Subscriber subscriber = new Subscriber(subscriberName);
                subscriber.setId(Integer.parseInt(subscriberId));
                application = new Application(applicationName, subscriber);

                application.setDescription(rs.getString("DESCRIPTION"));
                application.setStatus(rs.getString("APPLICATION_STATUS"));
                application.setCallbackUrl(rs.getString("CALLBACK_URL"));
                application.setId(rs.getInt("APPLICATION_ID"));
                application.setGroupId(rs.getString("GROUP_ID"));
                application.setUUID(rs.getString("UUID"));
                application.setTier(rs.getString("APPLICATION_TIER"));
                subscriber.setId(rs.getInt("SUBSCRIBER_ID"));
            }
        } catch (SQLException e) {
            handleException("Error while obtaining details of the Application : " + applicationId, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
        }
        return application;
    }

    /**
     * Retrieves the Application which is corresponding to the given UUID String
     *
     * @param uuid UUID of Application
     * @return
     * @throws APIManagementException
     */
    public Application getApplicationByUUID(String uuid) throws APIManagementException {
        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;

        Application application = null;
        try {
            connection = APIMgtDBUtil.getConnection();
            String query = SQLConstants.GET_APPLICATION_BY_UUID_SQL;

            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, uuid);

            rs = prepStmt.executeQuery();
            if (rs.next()) {
                String applicationName = rs.getString("NAME");
                String subscriberId = rs.getString("SUBSCRIBER_ID");
                String subscriberName = rs.getString("USER_ID");

                Subscriber subscriber = new Subscriber(subscriberName);
                subscriber.setId(Integer.parseInt(subscriberId));
                application = new Application(applicationName, subscriber);

                application.setDescription(rs.getString("DESCRIPTION"));
                application.setStatus(rs.getString("APPLICATION_STATUS"));
                application.setCallbackUrl(rs.getString("CALLBACK_URL"));
                application.setId(rs.getInt("APPLICATION_ID"));
                application.setGroupId(rs.getString("GROUP_ID"));
                application.setUUID(rs.getString("UUID"));
                application.setTier(rs.getString("APPLICATION_TIER"));
                subscriber.setId(rs.getInt("SUBSCRIBER_ID"));

                Set<APIKey> keys = getApplicationKeys(subscriber.getName(), application.getId());
                for (APIKey key : keys) {
                    application.addKey(key);
                }
            }
        } catch (SQLException e) {
            handleException("Error while obtaining details of the Application : " + uuid, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
        }
        return application;
    }

    /**
     * update URI templates define for an API
     *
     * @param api
     * @throws APIManagementException
     */
    public void updateURLTemplates(API api) throws APIManagementException {

        Connection connection = null;
        PreparedStatement prepStmt = null;
        int apiId;

        String deleteOldMappingsQuery = SQLConstants.REMOVE_FROM_URI_TEMPLATES_SQL;
        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);

            apiId = getAPIID(api.getId(), connection);
            if (apiId == -1) {
                //application addition has failed
                return;
            }
            prepStmt = connection.prepareStatement(deleteOldMappingsQuery);
            prepStmt.setInt(1, apiId);
            prepStmt.execute();

            addURLTemplates(apiId, api, connection);

            connection.commit();
        } catch (SQLException e) {
            handleException("Error while deleting URL template(s) for API : " + api.getId(), e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, null);
        }
    }

    /**
     * returns all URL templates define for all active(PUBLISHED) APIs.
     */
    public ArrayList<URITemplate> getAllURITemplates(String apiContext, String version) throws APIManagementException {
        if(APIUtil.isAdvanceThrottlingEnabled()) {
            return getAllURITemplatesAdvancedThrottle(apiContext, version);
        } else {
            return getAllURITemplatesOldThrottle(apiContext, version);
        }
    }

    public ArrayList<URITemplate> getAllURITemplatesOldThrottle(String apiContext, String version) throws APIManagementException {
        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        ArrayList<URITemplate> uriTemplates = new ArrayList<URITemplate>();

        //TODO : FILTER RESULTS ONLY FOR ACTIVE APIs
        String query = SQLConstants.GET_ALL_URL_TEMPLATES_SQL;
        try {
            connection = APIMgtDBUtil.getConnection();
            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, apiContext);
            prepStmt.setString(2, version);

            rs = prepStmt.executeQuery();

            URITemplate uriTemplate;
            while (rs.next()) {
                uriTemplate = new URITemplate();
                String script = null;
                uriTemplate.setHTTPVerb(rs.getString("HTTP_METHOD"));
                uriTemplate.setAuthType(rs.getString("AUTH_SCHEME"));
                uriTemplate.setUriTemplate(rs.getString("URL_PATTERN"));
                uriTemplate.setThrottlingTier(rs.getString("THROTTLING_TIER"));
                InputStream mediationScriptBlob = rs.getBinaryStream("MEDIATION_SCRIPT");
                if (mediationScriptBlob != null) {
                    script = APIMgtDBUtil.getStringFromInputStream(mediationScriptBlob);
                }
                uriTemplate.setMediationScript(script);
                uriTemplate.getThrottlingConditions().add("_default");
                uriTemplates.add(uriTemplate);
            }
        } catch (SQLException e) {
            handleException("Error while fetching all URL Templates", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
        }
        return uriTemplates;
    }

	public ArrayList<URITemplate> getAllURITemplatesAdvancedThrottle(String apiContext, String version) throws APIManagementException {
		Connection connection = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		ArrayList<URITemplate> uriTemplates = new ArrayList<URITemplate>();

		// TODO : FILTER RESULTS ONLY FOR ACTIVE APIs
		String query = SQLConstants.ThrottleSQLConstants.GET_CONDITION_GROUPS_FOR_POLICIES_SQL;
		try {
			connection = APIMgtDBUtil.getConnection();
			prepStmt = connection.prepareStatement(query);
			prepStmt.setString(1, apiContext);
			prepStmt.setString(2, version);

			rs = prepStmt.executeQuery();
			Map<String, Set<ConditionGroupDTO>> mapByHttpVerbURLPatternToId = new HashMap<String, Set<ConditionGroupDTO>>();
			while (rs != null && rs.next()) {

				String httpVerb = rs.getString("HTTP_METHOD");
				String authType = rs.getString("AUTH_SCHEME");
				String urlPattern = rs.getString("URL_PATTERN");
				String policyName = rs.getString("THROTTLING_TIER");
				String conditionGroupId = rs.getString("CONDITION_GROUP_ID");
				String applicableLevel = rs.getString("APPLICABLE_LEVEL");
				String policyConditionGroupId  = "_condition_" + conditionGroupId;

				String key = httpVerb + ":" + urlPattern;
				if (mapByHttpVerbURLPatternToId.containsKey(key)) {
                    if (StringUtils.isEmpty(conditionGroupId)) {
                        continue;
					}

                    // Converting ConditionGroup to a lightweight ConditionGroupDTO.
                    ConditionGroupDTO groupDTO = createConditionGroupDTO(Integer.parseInt(conditionGroupId));
                    groupDTO.setConditionGroupId(policyConditionGroupId);
//					mapByHttpVerbURLPatternToId.get(key).add(policyConditionGroupId);
                    mapByHttpVerbURLPatternToId.get(key).add(groupDTO);

				} else {
					String script = null;
					URITemplate uriTemplate = new URITemplate();
					uriTemplate.setThrottlingTier(policyName);
					uriTemplate.setAuthType(authType);
					uriTemplate.setHTTPVerb(httpVerb);
					uriTemplate.setUriTemplate(urlPattern);
                    uriTemplate.setApplicableLevel(applicableLevel);
					InputStream mediationScriptBlob = rs.getBinaryStream("MEDIATION_SCRIPT");
					if (mediationScriptBlob != null) {
						script = APIMgtDBUtil.getStringFromInputStream(mediationScriptBlob);
					}

					uriTemplate.setMediationScript(script);
					Set<ConditionGroupDTO> conditionGroupIdSet = new HashSet<ConditionGroupDTO>();
					mapByHttpVerbURLPatternToId.put(key, conditionGroupIdSet);
					uriTemplates.add(uriTemplate);
					if (StringUtils.isEmpty(conditionGroupId)) {
						continue;
					}
                    ConditionGroupDTO groupDTO = createConditionGroupDTO(Integer.parseInt(conditionGroupId));
                    groupDTO.setConditionGroupId(policyConditionGroupId);
					conditionGroupIdSet.add(groupDTO);

				}

			}

			for (URITemplate uriTemplate : uriTemplates) {
				String key = uriTemplate.getHTTPVerb() + ":" + uriTemplate.getUriTemplate();
				if (mapByHttpVerbURLPatternToId.containsKey(key)) {
					if (!mapByHttpVerbURLPatternToId.get(key).isEmpty()) {
                        Set<ConditionGroupDTO> conditionGroupDTOs = mapByHttpVerbURLPatternToId.get(key);
                        ConditionGroupDTO defaultGroup = new ConditionGroupDTO();
                        defaultGroup.setConditionGroupId(APIConstants.THROTTLE_POLICY_DEFAULT);
                        conditionGroupDTOs.add(defaultGroup);
//						uriTemplate.getThrottlingConditions().addAll(mapByHttpVerbURLPatternToId.get(key));
                      uriTemplate.getThrottlingConditions().add(APIConstants.THROTTLE_POLICY_DEFAULT);
                        uriTemplate.setConditionGroups(conditionGroupDTOs.toArray(new ConditionGroupDTO[]{}));
					}

				}

				if (uriTemplate.getThrottlingConditions().isEmpty()) {
					uriTemplate.getThrottlingConditions().add(APIConstants.THROTTLE_POLICY_DEFAULT);
                    ConditionGroupDTO defaultGroup = new ConditionGroupDTO();
                    defaultGroup.setConditionGroupId(APIConstants.THROTTLE_POLICY_DEFAULT);
                    uriTemplate.setConditionGroups(new ConditionGroupDTO[]{defaultGroup});
				}

			}
		} catch (SQLException e) {
			handleException("Error while fetching all URL Templates", e);
		} finally {
			APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
		}
		return uriTemplates;
	}

    /**
     * Converts an {@code Pipeline} object into a {@code ConditionGroupDTO}.{@code ConditionGroupDTO} class tries to
     * contain the same information held by  {@code Pipeline}, but in a much lightweight fashion.
     * @param conditionGroup Id of the condition group ({@code Pipeline}) to be converted
     * @return An object of {@code ConditionGroupDTO} type.
     * @throws APIManagementException
     */
    private ConditionGroupDTO createConditionGroupDTO(int conditionGroup) throws APIManagementException {
        List<Condition> conditions = getConditions(conditionGroup);
        ArrayList<ConditionDTO> conditionDTOs = new ArrayList<ConditionDTO>(conditions.size());
        for(Condition condition:conditions){
            ConditionDTO conditionDTO = new ConditionDTO();
            conditionDTO.setConditionType(condition.getType());

            conditionDTO.isInverted(condition.isInvertCondition());
            if(PolicyConstants.IP_RANGE_TYPE.equals(condition.getType())){
                IPCondition ipRangeCondition = (IPCondition) condition;
                conditionDTO.setConditionName(ipRangeCondition.getStartingIP());
                conditionDTO.setConditionValue(ipRangeCondition.getEndingIP());

            }else if(PolicyConstants.IP_SPECIFIC_TYPE.equals(condition.getType())){
                IPCondition ipCondition = (IPCondition) condition;
                conditionDTO.setConditionName(PolicyConstants.IP_SPECIFIC_TYPE);
                conditionDTO.setConditionValue(ipCondition.getSpecificIP());

            }else if(PolicyConstants.HEADER_TYPE.equals(condition.getType())){
                HeaderCondition headerCondition = (HeaderCondition) condition;
                conditionDTO.setConditionName(headerCondition.getHeaderName());
                conditionDTO.setConditionValue(headerCondition.getValue());

            }else if(PolicyConstants.JWT_CLAIMS_TYPE.equals(condition.getType())){
                JWTClaimsCondition jwtClaimsCondition = (JWTClaimsCondition) condition;
                conditionDTO.setConditionName(jwtClaimsCondition.getClaimUrl());
                conditionDTO.setConditionValue(jwtClaimsCondition.getAttribute());

            }else if(PolicyConstants.QUERY_PARAMETER_TYPE.equals(condition.getType())){
                QueryParameterCondition parameterCondition = (QueryParameterCondition) condition;
                conditionDTO.setConditionName(parameterCondition.getParameter());
                conditionDTO.setConditionValue(parameterCondition.getValue());
            }
            conditionDTOs.add(conditionDTO);
        }

        ConditionGroupDTO conditionGroupDTO = new ConditionGroupDTO();
        conditionGroupDTO.setConditions(conditionDTOs.toArray(new ConditionDTO[]{}));

        return conditionGroupDTO;
    }


    public void updateAPI(API api, int tenantId) throws APIManagementException {
        Connection connection = null;
        PreparedStatement prepStmt = null;

        String previousDefaultVersion = getDefaultVersion(api.getId());

        String query = SQLConstants.UPDATE_API_SQL;
        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            //Header change check not required here as we update API level throttling tier
            //from same call.
            //TODO review and run tier update as separate query if need.
            //if (api.isApiHeaderChanged()) {
                prepStmt = connection.prepareStatement(query);
                prepStmt.setString(1, api.getContext());
                String contextTemplate = api.getContextTemplate();
                //If the context template ends with {version} this means that the version will be at the end of the
                // context.
                if (contextTemplate.endsWith("/" + APIConstants.VERSION_PLACEHOLDER)) {
                    //Remove the {version} part from the context template.
                    contextTemplate = contextTemplate.split(Pattern.quote("/" + APIConstants.VERSION_PLACEHOLDER))[0];
                }
                prepStmt.setString(2, contextTemplate);
                //TODO Need to find who exactly does this update.
                prepStmt.setString(3, null);
                prepStmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
                prepStmt.setString(5, api.getApiLevelPolicy());
                prepStmt.setString(6, APIUtil.replaceEmailDomainBack(api.getId().getProviderName()));
                prepStmt.setString(7, api.getId().getApiName());
                prepStmt.setString(8, api.getId().getVersion());
                prepStmt.execute();
            //}

            if (api.isDefaultVersion() ^ api.getId().getVersion().equals(previousDefaultVersion)) { //A change has
                // happen
                //If the api is selected as default version, it is added/replaced into AM_API_DEFAULT_VERSION table
                if (api.isDefaultVersion()) {
                    addUpdateAPIAsDefaultVersion(api, connection);
                } else { //tick is removed
                    removeAPIFromDefaultVersion(api.getId(), connection);
                }
            }
            connection.commit();

            updateScopes(api, tenantId);
            updateURLTemplates(api);
            APIIdentifier apiIdentifier =
                    new APIIdentifier(APIUtil.replaceEmailDomainBack(api.getId().getProviderName()),
                            api.getId().getApiName(), api.getId().getVersion());
            //delete the existing environment details
            RemoveAPIEnvironments(apiIdentifier);
            // add new API Environment details
            addAPIEnvironments(apiIdentifier, api);

        } catch (SQLException e) {
            handleException("Error while updating the API: " + api.getId() + " in the database", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, null);
        }
    }

    public int getAPIID(APIIdentifier apiId, Connection connection) throws APIManagementException {
        boolean created = false;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;

        int id = -1;
        String getAPIQuery = SQLConstants.GET_API_ID_SQL;

        try {
            if (connection == null) {

                // If connection is not provided a new one will be created.
                connection = APIMgtDBUtil.getConnection();
                created = true;
            }

            prepStmt = connection.prepareStatement(getAPIQuery);
            prepStmt.setString(1, APIUtil.replaceEmailDomainBack(apiId.getProviderName()));
            prepStmt.setString(2, apiId.getApiName());
            prepStmt.setString(3, apiId.getVersion());
            rs = prepStmt.executeQuery();
            if (rs.next()) {
                id = rs.getInt("API_ID");
            }
            if (id == -1) {
                String msg = "Unable to find the API: " + apiId + " in the database";
                log.error(msg);
                throw new APIManagementException(msg);
            }
        } catch (SQLException e) {
            handleException("Error while locating API: " + apiId + " from the database", e);
        } finally {
            if (created) {
                APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
            } else {
                APIMgtDBUtil.closeAllConnections(prepStmt, null, rs);
            }
        }
        return id;
    }

    /**
     * Delete a record from AM_APPLICATION_KEY_MAPPING table
     *
     * @param consumerKey
     * @throws APIManagementException
     */
    public void deleteApplicationMappingByConsumerKey(String consumerKey) throws APIManagementException {
        Connection connection = null;
        PreparedStatement prepStmt = null;

        String deleteApplicationKeyQuery = SQLConstants.REMOVE_APPLICATION_MAPPINGS_BY_CONSUMER_KEY_SQL;
        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            prepStmt = connection.prepareStatement(deleteApplicationKeyQuery);
            prepStmt.setString(1, consumerKey);
            prepStmt.execute();
            connection.commit();
        } catch (SQLException e) {
            handleException("Error while deleting mapping: consumer key " + consumerKey + " from the database", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, null);
        }
    }

    public void deleteAPI(APIIdentifier apiId) throws APIManagementException {
        Connection connection = null;
        PreparedStatement prepStmt = null;
        int id;

        String deleteLCEventQuery = SQLConstants.REMOVE_FROM_API_LIFECYCLE_SQL;
        String deleteCommentQuery = SQLConstants.REMOVE_FROM_API_COMMENT_SQL;
        String deleteRatingsQuery = SQLConstants.REMOVE_FROM_API_RATING_SQL;
        String deleteSubscriptionQuery = SQLConstants.REMOVE_FROM_API_SUBSCRIPTION_SQL;
        String deleteExternalAPIStoresQuery = SQLConstants.REMOVE_FROM_EXTERNAL_STORES_SQL;
        String deleteAPIQuery = SQLConstants.REMOVE_FROM_API_SQL;
        String deleteURLTemplateQuery = SQLConstants.REMOVE_FROM_API_URL_MAPPINGS_SQL;

        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);

            id = getAPIID(apiId, connection);

            removeAPIScope(apiId);

            prepStmt = connection.prepareStatement(deleteSubscriptionQuery);
            prepStmt.setInt(1, id);
            prepStmt.execute();
            prepStmt.close();//If exception occurs at execute, this statement will close in finally else here

            //Delete all comments associated with given API
            prepStmt = connection.prepareStatement(deleteCommentQuery);
            prepStmt.setInt(1, id);
            prepStmt.execute();
            prepStmt.close();//If exception occurs at execute, this statement will close in finally else here

            prepStmt = connection.prepareStatement(deleteRatingsQuery);
            prepStmt.setInt(1, id);
            prepStmt.execute();
            prepStmt.close();//If exception occurs at execute, this statement will close in finally else here

            prepStmt = connection.prepareStatement(deleteLCEventQuery);
            prepStmt.setInt(1, id);
            prepStmt.execute();
            prepStmt.close();//If exception occurs at execute, this statement will close in finally else here

            //Delete all external APIStore details associated with a given API
            prepStmt = connection.prepareStatement(deleteExternalAPIStoresQuery);
            prepStmt.setInt(1, id);
            prepStmt.execute();
            prepStmt.close();//If exception occurs at execute, this statement will close in finally else here

            prepStmt = connection.prepareStatement(deleteAPIQuery);
            prepStmt.setString(1, APIUtil.replaceEmailDomainBack(apiId.getProviderName()));
            prepStmt.setString(2, apiId.getApiName());
            prepStmt.setString(3, apiId.getVersion());
            prepStmt.execute();
            prepStmt.close();//If exception occurs at execute, this statement will close in finally else here

            prepStmt = connection.prepareStatement(deleteURLTemplateQuery);
            prepStmt.setInt(1, id);
            prepStmt.execute();

            String curDefaultVersion = getDefaultVersion(apiId);
            String pubDefaultVersion = getPublishedDefaultVersion(apiId);
            if (apiId.getVersion().equals(curDefaultVersion)) {
                removeAPIFromDefaultVersion(apiId, connection);
            } else if (apiId.getVersion().equals(pubDefaultVersion)) {
                setPublishedDefVersion(apiId, connection, null);
            }

            connection.commit();
        } catch (SQLException e) {
            handleException("Error while removing the API: " + apiId + " from the database", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, null);
        }
    }

    /**
     * Change access token status in to revoked in database level.
     *
     * @param key API Key to be revoked
     * @throws APIManagementException on error in revoking access token
     */
    public void revokeAccessToken(String key) throws APIManagementException {
        String accessTokenStoreTable = APIConstants.ACCESS_TOKEN_STORE_TABLE;
        accessTokenStoreTable = getAccessTokenStoreTableFromAccessToken(key, accessTokenStoreTable);
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);

            String query = SQLConstants.REMOVE_ACCESS_TOKEN_PREFIX + accessTokenStoreTable + SQLConstants
                    .REVOKE_ACCESS_TOKEN_SUFFIX;
            ps = conn.prepareStatement(query);
            ps.setString(1, APIUtil.encryptToken(key));
            ps.execute();

            conn.commit();
        } catch (SQLException e) {
            handleException("Error in revoking access token: " + e.getMessage(), e);
        } catch (CryptoException e) {
            handleException("Error in revoking access token: " + e.getMessage(), e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, null);
        }
    }

    /**
     * Get APIIdentifiers Associated with access token - access token associated with application
     * which has multiple APIs. so this returns all APIs associated with a access token
     *
     * @param accessToken String access token
     * @return APIIdentifier set for all API's associated with given access token
     * @throws APIManagementException error in getting APIIdentifiers
     */
    public Set<APIIdentifier> getAPIByAccessToken(String accessToken) throws APIManagementException {
        String accessTokenStoreTable = APIConstants.ACCESS_TOKEN_STORE_TABLE;
        accessTokenStoreTable = getAccessTokenStoreTableFromAccessToken(accessToken, accessTokenStoreTable);
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet result = null;
        String getAPISql = SQLConstants.GET_API_BY_ACCESS_TOKEN_PREFIX +
                           accessTokenStoreTable + SQLConstants.GET_API_BY_ACCESS_TOKEN_SUFFIX;

        Set<APIIdentifier> apiSet = new HashSet<APIIdentifier>();
        try {
            connection = APIMgtDBUtil.getConnection();
            ps = connection.prepareStatement(getAPISql);
            ps.setString(1, APIUtil.encryptToken(accessToken));
            result = ps.executeQuery();
            while (result.next()) {
                apiSet.add(new APIIdentifier(APIUtil.replaceEmailDomain(result.getString("API_PROVIDER")), result
                        .getString("API_NAME"), result.getString("API_VERSION")));
            }
        } catch (SQLException e) {
            handleException("Failed to get API ID for token: " + accessToken, e);
        } catch (CryptoException e) {
            handleException("Failed to get API ID for token: " + accessToken, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, connection, result);
        }
        return apiSet;
    }


    /**
     * Get all applications associated with given tier
     *
     * @param tier String tier name
     * @return Application object array associated with tier
     * @throws APIManagementException on error in getting applications array
     */
    public Application[] getApplicationsByTier(String tier) throws APIManagementException {
        if (tier == null) {
            return null;
        }
        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        Application[] applications = null;

        String sqlQuery = SQLConstants.GET_APPLICATION_BY_TIER_SQL;
        try {
            connection = APIMgtDBUtil.getConnection();
            prepStmt = connection.prepareStatement(sqlQuery);
            prepStmt.setString(1, tier);
            rs = prepStmt.executeQuery();
            ArrayList<Application> applicationsList = new ArrayList<Application>();
            Application application;
            while (rs.next()) {
                application = new Application(rs.getString("NAME"), getSubscriber(rs.getString("SUBSCRIBER_ID")));
                application.setId(rs.getInt("APPLICATION_ID"));
            }
            Collections.sort(applicationsList, new Comparator<Application>() {
                public int compare(Application o1, Application o2) {
                    return o1.getName().compareToIgnoreCase(o2.getName());
                }
            });
            applications = applicationsList.toArray(new Application[applicationsList.size()]);

        } catch (SQLException e) {
            handleException("Error when reading the application information from" + " the persistence store.", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
        }
        return applications;
    }

    private void handleException(String msg, Throwable t) throws APIManagementException {
        log.error(msg, t);
        throw new APIManagementException(msg, t);
    }


    public HashMap<String, String> getURITemplatesPerAPIAsString(APIIdentifier identifier)
            throws APIManagementException {
        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        int apiId;
        HashMap<String, String> urlMappings = new LinkedHashMap<String, String>();
        try {
            conn = APIMgtDBUtil.getConnection();
            apiId = getAPIID(identifier, conn);

            String sqlQuery = SQLConstants.GET_URL_TEMPLATES_SQL;

            ps = conn.prepareStatement(sqlQuery);
            ps.setInt(1, apiId);
            resultSet = ps.executeQuery();
            while (resultSet.next()) {
                String script = null;
                String uriPattern = resultSet.getString("URL_PATTERN");
                String httpMethod = resultSet.getString("HTTP_METHOD");
                String authScheme = resultSet.getString("AUTH_SCHEME");
                String throttlingTier = resultSet.getString("THROTTLING_TIER");
                InputStream mediationScriptBlob = resultSet.getBinaryStream("MEDIATION_SCRIPT");
                if (mediationScriptBlob != null) {
                    script = APIMgtDBUtil.getStringFromInputStream(mediationScriptBlob);
                    // set null if the script is empty. Otherwise ArrayIndexOutOfBoundsException occurs when trying
                    // to split by ::
                    if (script.isEmpty()) {
                        script = null;
                    }
                }
                urlMappings.put(uriPattern + "::" + httpMethod + "::" + authScheme + "::" + throttlingTier + "::" +
                                script, null);
            }
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error("Failed to rollback the add subscription ", e1);
                }
            }
            handleException("Failed to add subscriber data ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, resultSet);
        }
        return urlMappings;
    }

    // This should be only used only when Token Partitioning is enabled.
    public String getConsumerKeyForTokenWhenTokenPartitioningEnabled(String accessToken) throws APIManagementException {

        if (APIUtil.checkAccessTokenPartitioningEnabled() && APIUtil.checkUserNameAssertionEnabled()) {
            String accessTokenStoreTable = APIUtil.getAccessTokenStoreTableFromAccessToken(accessToken);
            StringBuilder authorizedDomains = new StringBuilder();
            String getCKFromTokenSQL = "SELECT CONSUMER_KEY " +
                                           " FROM " + accessTokenStoreTable +
                                           " WHERE ACCESS_TOKEN = ? ";

            Connection connection = null;
            PreparedStatement prepStmt = null;
            ResultSet rs = null;
            try {
                connection = APIMgtDBUtil.getConnection();
                prepStmt = connection.prepareStatement(getCKFromTokenSQL);
                prepStmt.setString(1, APIUtil.encryptToken(accessToken));
                rs = prepStmt.executeQuery();
                boolean first = true;
                while (rs.next()) {
                    String domain = rs.getString(1);
                    if (first) {
                        authorizedDomains.append(domain);
                        first = false;
                    } else {
                        authorizedDomains.append(',').append(domain);
                    }
                }
            } catch (SQLException e) {
                throw new APIManagementException("Error in retrieving access allowing domain list from table.", e);
            } catch (CryptoException e) {
                throw new APIManagementException("Error in retrieving access allowing domain list from table.", e);
            } finally {
                APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
            }
            return authorizedDomains.toString();
        }
        return null;
    }

    public String findConsumerKeyFromAccessToken(String accessToken) throws APIManagementException {
        String accessTokenStoreTable = APIConstants.ACCESS_TOKEN_STORE_TABLE;
        accessTokenStoreTable = getAccessTokenStoreTableFromAccessToken(accessToken, accessTokenStoreTable);
        Connection connection = null;
        PreparedStatement smt = null;
        ResultSet rs = null;
        String consumerKey = null;
        try {
            String getConsumerKeySql = SQLConstants.GET_CONSUMER_KEY_BY_ACCESS_TOKEN_PREFIX + accessTokenStoreTable +
                                       SQLConstants.GET_CONSUMER_KEY_BY_ACCESS_TOKEN_SUFFIX;
            connection = APIMgtDBUtil.getConnection();
            smt = connection.prepareStatement(getConsumerKeySql);
            smt.setString(1, APIUtil.encryptToken(accessToken));
            rs = smt.executeQuery();
            while (rs.next()) {
                consumerKey = rs.getString(1);
            }
        } catch (SQLException e) {
            handleException("Error while getting authorized domians.", e);
        } catch (CryptoException e) {
            handleException("Error while getting authorized domians.", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(smt, connection, rs);
        }
        return consumerKey;
    }

    /**
     * Adds a comment for an API
     *
     * @param identifier  API Identifier
     * @param commentText Commented Text
     * @param user        User who did the comment
     * @return Comment ID
     */
    public int addComment(APIIdentifier identifier, String commentText, String user) throws APIManagementException {

        Connection connection = null;
        ResultSet resultSet = null;
        ResultSet insertSet = null;
        PreparedStatement getPrepStmt = null;
        PreparedStatement insertPrepStmt = null;
        int commentId = -1;
        int apiId = -1;

        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);

            String getApiQuery = SQLConstants.GET_API_ID_SQL;
            getPrepStmt = connection.prepareStatement(getApiQuery);
            getPrepStmt.setString(1, APIUtil.replaceEmailDomainBack(identifier.getProviderName()));
            getPrepStmt.setString(2, identifier.getApiName());
            getPrepStmt.setString(3, identifier.getVersion());
            resultSet = getPrepStmt.executeQuery();
            if (resultSet.next()) {
                apiId = resultSet.getInt("API_ID");
            }

            if (apiId == -1) {
                String msg = "Unable to get the API ID for: " + identifier;
                log.error(msg);
                throw new APIManagementException(msg);
            }

            /*This query to update the AM_API_COMMENTS table */
            String addCommentQuery = SQLConstants.ADD_COMMENT_SQL;

            /*Adding data to the AM_API_COMMENTS table*/
            String dbProductName = connection.getMetaData().getDatabaseProductName();
            insertPrepStmt = connection.prepareStatement(addCommentQuery,
                new String[]{DBUtils.getConvertedAutoGeneratedColumnName(dbProductName, "comment_id")});

            insertPrepStmt.setString(1, commentText);
            insertPrepStmt.setString(2, user);
            insertPrepStmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()), Calendar.getInstance());
            insertPrepStmt.setInt(4, apiId);

            insertPrepStmt.executeUpdate();
            insertSet = insertPrepStmt.getGeneratedKeys();
            while (insertSet.next()) {
                commentId = Integer.parseInt(insertSet.getString(1));
            }
            connection.commit();
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException e1) {
                    log.error("Failed to rollback the add comment ", e1);
                }
            }
            handleException("Failed to add comment data, for  " + identifier.getApiName() + '-' + identifier
                    .getVersion(), e);
        } finally {
            APIMgtDBUtil.closeAllConnections(getPrepStmt, connection, resultSet);
            APIMgtDBUtil.closeAllConnections(insertPrepStmt, null, insertSet);
        }
        return commentId;
    }

    /**
     * Returns all the Comments on an API
     *
     * @param identifier API Identifier
     * @return Comment Array
     * @throws APIManagementException
     */
    public Comment[] getComments(APIIdentifier identifier) throws APIManagementException {
        List<Comment> commentList = new ArrayList<Comment>();
        Connection connection = null;
        ResultSet resultSet = null;
        PreparedStatement prepStmt = null;

        String sqlQuery = SQLConstants.GET_COMMENTS_SQL;
        try {
            connection = APIMgtDBUtil.getConnection();
            prepStmt = connection.prepareStatement(sqlQuery);
            prepStmt.setString(1, APIUtil.replaceEmailDomainBack(identifier.getProviderName()));
            prepStmt.setString(2, identifier.getApiName());
            prepStmt.setString(3, identifier.getVersion());
            resultSet = prepStmt.executeQuery();
            while (resultSet.next()) {
                Comment comment = new Comment();
                comment.setText(resultSet.getString("COMMENT_TEXT"));
                comment.setUser(resultSet.getString("COMMENTED_USER"));
                comment.setCreatedTime(new java.util.Date(resultSet.getTimestamp("DATE_COMMENTED").getTime()));
                commentList.add(comment);
            }
        } catch (SQLException e) {
            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException e1) {
                log.error("Failed to retrieve comments ", e1);
            }
            handleException("Failed to retrieve comments for  " + identifier.getApiName() + '-' + identifier
                    .getVersion(), e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, resultSet);
        }
        return commentList.toArray(new Comment[commentList.size()]);
    }

    public boolean isContextExist(String context) {
        Connection connection = null;
        ResultSet resultSet = null;
        PreparedStatement prepStmt = null;

        String sql = SQLConstants.GET_API_CONTEXT_SQL;
        try {
            connection = APIMgtDBUtil.getConnection();
            prepStmt = connection.prepareStatement(sql);
            prepStmt.setString(1, context);
            resultSet = prepStmt.executeQuery();

            while (resultSet.next()) {
                if (resultSet.getString(1) != null) {
                    return true;
                }
            }
        } catch (SQLException e) {
            log.error("Failed to retrieve the API Context ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, resultSet);
        }
        return false;
    }

    public String getAPIContext(APIIdentifier identifier) throws APIManagementException {
        Connection connection = null;
        ResultSet resultSet = null;
        PreparedStatement prepStmt = null;

        String context = null;

        String sql = SQLConstants.GET_API_CONTEXT_BY_API_NAME_SQL;
        try {
            connection = APIMgtDBUtil.getConnection();
            prepStmt = connection.prepareStatement(sql);
            prepStmt.setString(1, APIUtil.replaceEmailDomainBack(identifier.getProviderName()));
            prepStmt.setString(2, identifier.getApiName());
            prepStmt.setString(3, identifier.getVersion());
            resultSet = prepStmt.executeQuery();

            while (resultSet.next()) {
                context = resultSet.getString(1);
            }
        } catch (SQLException e) {
            log.error("Failed to retrieve the API Context", e);

            handleException("Failed to retrieve the API Context for " +
                            identifier.getProviderName() + '-' + identifier.getApiName() + '-' + identifier
                                    .getVersion(), e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, resultSet);
        }
        return context;
    }

    public List<String> getAllAvailableContexts() {
        List<String> contexts = new ArrayList<String>();
        Connection connection = null;
        ResultSet resultSet = null;
        PreparedStatement prepStmt = null;

        String sql = SQLConstants.GET_ALL_CONTEXT_SQL;
        try {
            connection = APIMgtDBUtil.getConnection();
            prepStmt = connection.prepareStatement(sql);
            resultSet = prepStmt.executeQuery();

            while (resultSet.next()) {
                contexts.add(resultSet.getString("CONTEXT"));
            }
        } catch (SQLException e) {
            log.error("Failed to retrieve the API Context ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, resultSet);
        }
        return contexts;
    }

    public void populateAppRegistrationWorkflowDTO(ApplicationRegistrationWorkflowDTO workflowDTO)
            throws APIManagementException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Application application = null;
        Subscriber subscriber = null;

        String registrationEntry = SQLConstants.GET_APPLICATION_REGISTRATION_ENTRY_BY_SUBSCRIBER_SQL;
        try {
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(registrationEntry);
            ps.setString(1, workflowDTO.getExternalWorkflowReference());
            rs = ps.executeQuery();

            while (rs.next()) {
                subscriber = new Subscriber(rs.getString("USER_ID"));
                subscriber.setId(rs.getInt("SUBSCRIBER_ID"));
                application = new Application(rs.getString("NAME"), subscriber);
                application.setId(rs.getInt("APPLICATION_ID"));
                application.setApplicationWorkFlowStatus(rs.getString("APPLICATION_STATUS"));
                application.setCallbackUrl(rs.getString("CALLBACK_URL"));
                application.setDescription(rs.getString("DESCRIPTION"));
                application.setTier(rs.getString("APPLICATION_TIER"));
                workflowDTO.setApplication(application);
                workflowDTO.setKeyType(rs.getString("TOKEN_TYPE"));
                workflowDTO.setUserName(subscriber.getName());
                workflowDTO.setDomainList(rs.getString("ALLOWED_DOMAINS"));
                workflowDTO.setValidityTime(rs.getLong("VALIDITY_PERIOD"));
                OAuthAppRequest request = ApplicationUtils.createOauthAppRequest(application.getName(), null,
                                                                                 application.getCallbackUrl(), rs
                                                                                         .getString("TOKEN_SCOPE"),
                                                                                 rs.getString("INPUTS"));
                workflowDTO.setAppInfoDTO(request);
            }
        } catch (SQLException e) {
            handleException("Error occurred while retrieving an " +
                            "Application Registration Entry for Workflow : " + workflowDTO
                                    .getExternalWorkflowReference(), e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
    }

    @Deprecated
    public ApplicationRegistrationWorkflowDTO populateAppRegistrationWorkflowDTO(int appId)
            throws APIManagementException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        ApplicationRegistrationWorkflowDTO workflowDTO = null;
        //TODO: Need to create a different Entity for holding Registration Info.
        String registrationEntry = SQLConstants.GET_APPLICATION_REGISTRATION_ENTRY_SQL;

        try {
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(registrationEntry);
            ps.setInt(1, appId);
            rs = ps.executeQuery();

            while (rs.next()) {
                workflowDTO = (ApplicationRegistrationWorkflowDTO) WorkflowExecutorFactory.getInstance()
                        .createWorkflowDTO(WorkflowConstants.WF_TYPE_AM_APPLICATION_REGISTRATION_PRODUCTION);
                workflowDTO.setKeyType(rs.getString("TOKEN_TYPE"));
                workflowDTO.setDomainList(rs.getString("ALLOWED_DOMAINS"));
                workflowDTO.setValidityTime(rs.getLong("VALIDITY_PERIOD"));
            }
        } catch (SQLException e) {
            handleException("Error occurred while retrieving an " +
                            "Application Registration Entry for Application ID : " + appId, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
        return workflowDTO;
    }

    public int getApplicationIdForAppRegistration(String workflowReference) throws APIManagementException {

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int appId = -1;

        String registrationEntry = SQLConstants.GET_APPLICATION_REGISTRATION_ID_SQL;
        try {
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(registrationEntry);
            ps.setString(1, workflowReference);
            rs = ps.executeQuery();

            while (rs.next()) {
                appId = rs.getInt("APP_ID");
            }
        } catch (SQLException e) {
            handleException("Error occurred while retrieving an " +
                            "Application Registration Entry for Workflow : " + workflowReference, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
        return appId;
    }

    /**
     * Fetches WorkflowReference when given Application Name and UserId.
     *
     * @param applicationName
     * @param userId
     * @return WorkflowReference
     * @throws APIManagementException
     */
    public String getWorkflowReference(String applicationName, String userId) throws APIManagementException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String workflowReference = null;

        String sqlQuery = SQLConstants.GET_WORKFLOW_ENTRY_SQL;
        try {
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            ps.setString(1, applicationName);
            ps.setString(2, userId);
            rs = ps.executeQuery();

            while (rs.next()) {
                workflowReference = rs.getString("WF_REF");
            }
        } catch (SQLException e) {
            handleException("Error occurred while getting workflow entry for " +
                            "Application : " + applicationName + " created by " + userId, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
        return workflowReference;
    }

    /**
     * Retries the WorkflowExternalReference for a application.
     *
     * @param appID ID of the application
     * @return External workflow reference for the application identified
     * @throws APIManagementException
     */
    public String getExternalWorkflowReferenceByApplicationID(int appID) throws APIManagementException {
        String workflowExtRef = null;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        String sqlQuery = SQLConstants.GET_EXTERNAL_WORKFLOW_REFERENCE_SQL;
        try {
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            ps.setString(1, WorkflowConstants.WF_TYPE_AM_APPLICATION_CREATION);
            ps.setString(2, String.valueOf(appID));
            rs = ps.executeQuery();

            // returns only one row
            while (rs.next()) {
                workflowExtRef = rs.getString("WF_EXTERNAL_REFERENCE");
            }
        } catch (SQLException e) {
            handleException("Error occurred while getting workflow entry for " +
                            "Application ID : " + appID, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }

        return workflowExtRef;
    }

    /**
     * Retries the WorkflowExternalReference for a subscription.
     *
     * @param identifier APIIdentifier to find the subscribed api
     * @param appID      ID of the application which has the subscription
     * @return External workflow reference for the subscription identified
     * @throws APIManagementException
     */
    public String getExternalWorkflowReferenceForSubscription(APIIdentifier identifier, int appID)
            throws APIManagementException {
        String workflowExtRef = null;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int apiID;
        int subscriptionID = -1;

        String sqlQuery = SQLConstants.GET_EXTERNAL_WORKFLOW_REFERENCE_FOR_SUBSCRIPTION_SQL;
        String postgreSQL = SQLConstants.GET_EXTERNAL_WORKFLOW_REFERENCE_FOR_SUBSCRIPTION_POSTGRE_SQL;
        try {
            apiID = getAPIID(identifier, conn);
            conn = APIMgtDBUtil.getConnection();
            if (conn.getMetaData().getDriverName().contains("PostgreSQL")) {
                sqlQuery = postgreSQL;
            }
            ps = conn.prepareStatement(sqlQuery);
            ps.setInt(1, apiID);
            ps.setInt(2, appID);
            ps.setString(3, WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION);
            rs = ps.executeQuery();

            // returns only one row
            while (rs.next()) {
                workflowExtRef = rs.getString("WF_EXTERNAL_REFERENCE");
            }

        } catch (SQLException e) {
            handleException("Error occurred while getting workflow entry for " +
                            "Subscription : " + subscriptionID, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }

        return workflowExtRef;
    }

    /**
     * Retries the WorkflowExternalReference for a subscription.
     *
     * @param subscriptionId ID of the subscription
     * @return External workflow reference for the subscription <code>subscriptionId</code>
     * @throws APIManagementException
     */
    public String getExternalWorkflowReferenceForSubscription(int subscriptionId) throws APIManagementException {
        String workflowExtRef = null;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        String sqlQuery = SQLConstants.GET_EXTERNAL_WORKFLOW_FOR_SUBSCRIPTION_SQL;
        try {
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            // setting subscriptionId as string to prevent error when db finds string type IDs for
            // ApplicationRegistration workflows
            ps.setString(1, String.valueOf(subscriptionId));
            ps.setString(2, WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION);
            rs = ps.executeQuery();

            // returns only one row
            while (rs.next()) {
                workflowExtRef = rs.getString("WF_EXTERNAL_REFERENCE");
            }
        } catch (SQLException e) {
            handleException("Error occurred while getting workflow entry for " +
                            "Subscription : " + subscriptionId, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
        return workflowExtRef;
    }

    /**
     * Retries the WorkflowExternalReference for an user signup by DOMAIN/username.
     *
     * @param usernameWithDomain username of the signed up user inthe format of DOMAIN/username
     * @return External workflow reference for the signup workflow entry
     * @throws APIManagementException
     */
    public String getExternalWorkflowReferenceForUserSignup(String usernameWithDomain) throws APIManagementException {
        String workflowExtRef = null;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        String sqlQuery = SQLConstants.GET_EXTERNAL_WORKFLOW_FOR_SIGNUP_SQL;
        try {
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            ps.setString(1, usernameWithDomain);
            ps.setString(2, WorkflowConstants.WF_TYPE_AM_USER_SIGNUP);
            rs = ps.executeQuery();

            // returns only one row
            while (rs.next()) {
                workflowExtRef = rs.getString("WF_EXTERNAL_REFERENCE");
            }
        } catch (SQLException e) {
            handleException("Error occurred while getting workflow entry for " +
                            "User signup : " + usernameWithDomain, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
        return workflowExtRef;
    }

    /**
     * Retrieves IDs of pending subscriptions for a given application
     *
     * @param applicationId application id of the application
     * @return Set containing subscription id list
     * @throws APIManagementException
     */
    public Set<Integer> getPendingSubscriptionsByApplicationId(int applicationId) throws APIManagementException {
        Set<Integer> pendingSubscriptions = new HashSet<Integer>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        String sqlQuery = SQLConstants.GET_PAGINATED_SUBSCRIPTIONS_BY_APPLICATION_SQL;
        try {
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            ps.setInt(1, applicationId);
            ps.setString(2, APIConstants.SubscriptionStatus.ON_HOLD);
            rs = ps.executeQuery();

            while (rs.next()) {
                pendingSubscriptions.add(rs.getInt("SUBSCRIPTION_ID"));
            }
        } catch (SQLException e) {
            handleException("Error occurred while getting subscription entries for " +
                            "Application : " + applicationId, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
        return pendingSubscriptions;
    }

    /**
     * Retrieves registration workflow reference for applicationId and key type
     *
     * @param applicationId id of the application with registration
     * @param keyType       key type of the registration
     * @return workflow reference of the registration
     * @throws APIManagementException
     */
    public String getRegistrationWFReference(int applicationId, String keyType) throws APIManagementException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String reference = null;

        String sqlQuery = SQLConstants.GET_REGISTRATION_WORKFLOW_SQL;
        try {
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            ps.setInt(1, applicationId);
            ps.setString(2, keyType);
            rs = ps.executeQuery();

            // returns only one row
            while (rs.next()) {
                reference = rs.getString("WF_REF");
            }
        } catch (SQLException e) {
            handleException("Error occurred while getting registration entry for " +
                            "Application : " + applicationId, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
        return reference;
    }

    /**
     * Retrives subscription status for APIIdentifier and applicationId
     *
     * @param identifier    api identifier subscribed
     * @param applicationId application with subscription
     * @return subscription status
     * @throws APIManagementException
     */
    public String getSubscriptionStatus(APIIdentifier identifier, int applicationId) throws APIManagementException {
        String status = null;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        String sqlQuery = SQLConstants.GET_SUBSCRIPTION_STATUS_SQL;
        try {
            conn = APIMgtDBUtil.getConnection();
            int apiId = getAPIID(identifier, conn);
            ps = conn.prepareStatement(sqlQuery);
            ps.setInt(1, apiId);
            ps.setInt(2, applicationId);
            rs = ps.executeQuery();

            // returns only one row
            while (rs.next()) {
                status = rs.getString("SUB_STATUS");
            }
        } catch (SQLException e) {
            handleException("Error occurred while getting subscription entry for " +
                            "Application : " + applicationId + ", API: " + identifier, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
        return status;
    }

    /**
     * Retrieve subscription create state for APIIdentifier and applicationID
     *
     * @param identifier    - api identifier which is subscribed
     * @param applicationId - application used to subscribed
     * @param connection
     * @return subscription create status
     * @throws APIManagementException
     */
    public String getSubscriptionCreaeteStatus(APIIdentifier identifier, int applicationId, Connection connection)
            throws APIManagementException {
        String status = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        String sqlQuery = SQLConstants.GET_SUBSCRIPTION_CREATION_STATUS_SQL;
        try {
            int apiId = getAPIID(identifier, connection);
            ps = connection.prepareStatement(sqlQuery);
            ps.setInt(1, apiId);
            ps.setInt(2, applicationId);
            rs = ps.executeQuery();

            // returns only one row
            while (rs.next()) {
                status = rs.getString("SUBS_CREATE_STATE");
            }
        } catch (SQLException e) {
            handleException("Error occurred while getting subscription entry for " +
                            "Application : " + applicationId + ", API: " + identifier, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, null, rs);
        }
        return status;
    }

    private class SubscriptionInfo {
        private int subscriptionId;
        private String tierId;
        private int applicationId;
        private String accessToken;
        private String tokenType;
    }

    /**
     * Identify whether the loggedin user used his ordinal username or email
     *
     * @param userId
     * @return
     */
    private boolean isUserLoggedInEmail(String userId) {
        return userId.contains("@");
    }

    /**
     * Identify whether the loggedin user used his Primary Login name or Secondary login name
     *
     * @param userId
     * @return
     */
    private boolean isSecondaryLogin(String userId) {
        Map<String, Map<String, String>> loginConfiguration = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfigurationService().getAPIManagerConfiguration().getLoginConfiguration();
        if (loginConfiguration.get(APIConstants.EMAIL_LOGIN) != null) {
            Map<String, String> emailConf = loginConfiguration.get(APIConstants.EMAIL_LOGIN);
            if ("true".equalsIgnoreCase(emailConf.get(APIConstants.PRIMARY_LOGIN))) {
                return !isUserLoggedInEmail(userId);
            }
            if ("false".equalsIgnoreCase(emailConf.get(APIConstants.PRIMARY_LOGIN))) {
                return isUserLoggedInEmail(userId);
            }
        }
        if (loginConfiguration.get(APIConstants.USERID_LOGIN) != null) {
            Map<String, String> userIdConf = loginConfiguration.get(APIConstants.USERID_LOGIN);
            if ("true".equalsIgnoreCase(userIdConf.get(APIConstants.PRIMARY_LOGIN))) {
                return isUserLoggedInEmail(userId);
            }
            if ("false".equalsIgnoreCase(userIdConf.get(APIConstants.PRIMARY_LOGIN))) {
                return !isUserLoggedInEmail(userId);
            }
        }
        return false;
    }

    /**
     * Get the primaryLogin name using secondary login name. Primary secondary
     * Configuration is provided in the identitiy.xml. In the userstore, it is
     * users responsibility TO MAINTAIN THE SECONDARY LOGIN NAME AS UNIQUE for
     * each and every users. If it is not unique, we will pick the very first
     * entry from the userlist.
     *
     * @param login
     * @return
     * @throws APIManagementException
     */
    private String getPrimaryLoginFromSecondary(String login) throws APIManagementException {
        Map<String, Map<String, String>> loginConfiguration = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfigurationService().getAPIManagerConfiguration().getLoginConfiguration();
        String claimURI, username = null;
        if (isUserLoggedInEmail(login)) {
            Map<String, String> emailConf = loginConfiguration.get(APIConstants.EMAIL_LOGIN);
            claimURI = emailConf.get(APIConstants.CLAIM_URI);
        } else {
            Map<String, String> userIdConf = loginConfiguration.get(APIConstants.USERID_LOGIN);
            claimURI = userIdConf.get(APIConstants.CLAIM_URI);
        }

        try {
            RemoteUserManagerClient rmUserClient = new RemoteUserManagerClient(login);
            String[] user = rmUserClient.getUserList(claimURI, login);
            if (user.length > 0) {
                username = user[0];
            }
        } catch (Exception e) {

            handleException("Error while retrieving the primaryLogin name using secondary loginName : " + login, e);
        }
        return username;
    }

    /**
     * identify the login username is primary or secondary
     *
     * @param userID
     * @return
     * @throws APIManagementException
     */
    private String getLoginUserName(String userID) throws APIManagementException {
        String primaryLogin = userID;
        if (isSecondaryLogin(userID)) {
            primaryLogin = getPrimaryLoginFromSecondary(userID);
        }
        return primaryLogin;
    }


    /**
     * Store external APIStore details to which APIs successfully published
     *
     * @param apiId       APIIdentifier
     * @param apiStoreSet APIStores set
     * @return added/failed
     * @throws APIManagementException
     */
    public boolean addExternalAPIStoresDetails(APIIdentifier apiId, Set<APIStore> apiStoreSet)
            throws APIManagementException {
        Connection conn = null;
        PreparedStatement ps = null;
        boolean state = false;
        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);

            //This query to add external APIStores to database table
            String sqlQuery = SQLConstants.ADD_EXTERNAL_API_STORE_SQL;

            //Get API Id
            int apiIdentifier;
            apiIdentifier = getAPIID(apiId, conn);
            if (apiIdentifier == -1) {
                String msg = "Could not load API record for: " + apiId.getApiName();
                log.error(msg);
            }
            ps = conn.prepareStatement(sqlQuery);
            for (Object storeObject : apiStoreSet) {
                APIStore store = (APIStore) storeObject;
                ps.setInt(1, apiIdentifier);
                ps.setString(2, store.getName());
                ps.setString(3, store.getDisplayName());
                ps.setString(4, store.getEndpoint());
                ps.setString(5, store.getType());
                ps.addBatch();
            }

            ps.executeBatch();
            conn.commit();
            state = true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error("Failed to rollback storing external apistore details ", e1);
                }
            }
            log.error("Failed to store external apistore details", e);
            state = false;
        } catch (APIManagementException e) {
            log.error("Failed to store external apistore details", e);
            state = false;
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, null);
        }
        return state;
    }

    /**
     * Delete the records of external APIStore details.
     *
     * @param apiId       APIIdentifier
     * @param apiStoreSet APIStores set
     * @return added/failed
     * @throws APIManagementException
     */
    public boolean deleteExternalAPIStoresDetails(APIIdentifier apiId, Set<APIStore> apiStoreSet)
            throws APIManagementException {
        Connection conn = null;
        PreparedStatement ps = null;
        boolean state = false;
        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);

            String sqlQuery = SQLConstants.REMOVE_EXTERNAL_API_STORE_SQL;

            //Get API Id
            int apiIdentifier;
            apiIdentifier = getAPIID(apiId, conn);
            if (apiIdentifier == -1) {
                String msg = "Could not load API record for: " + apiId.getApiName();
                log.error(msg);
            }
            ps = conn.prepareStatement(sqlQuery);
            for (Object storeObject : apiStoreSet) {
                APIStore store = (APIStore) storeObject;
                ps.setInt(1, apiIdentifier);
                ps.setString(2, store.getName());
                ps.setString(3, store.getType());
                ps.addBatch();
            }
            ps.executeBatch();

            conn.commit();
            state = true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error("Failed to rollback deleting external apistore details ", e1);
                }
            }
            log.error("Failed to delete external apistore details", e);
            state = false;
        } catch (APIManagementException e) {
            log.error("Failed to delete external apistore details", e);
            state = false;
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, null);
        }
        return state;
    }

    public void updateExternalAPIStoresDetails(APIIdentifier apiId, Set<APIStore> apiStoreSet)
            throws APIManagementException {
        Connection conn = null;
        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);
            updateExternalAPIStoresDetails(apiId, apiStoreSet, conn);
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error("Failed to rollback updating external apistore details ", e1);
                }
            }
            log.error("Failed to update external apistore details", e);
        } catch (APIManagementException e) {
            log.error("Failed to updating external apistore details", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(null, conn, null);
        }
    }

    /**
     * Updateexternal APIStores details to which APIs published
     *
     * @param apiIdentifier API Identifier
     * @throws APIManagementException if failed to add Application
     */
    public void updateExternalAPIStoresDetails(APIIdentifier apiIdentifier, Set<APIStore> apiStoreSet, Connection conn)
            throws APIManagementException, SQLException {
        PreparedStatement ps = null;

        try {
            conn.setAutoCommit(false);
            //This query to add external APIStores to database table
            String sqlQuery = SQLConstants.UPDATE_EXTERNAL_API_STORE_SQL;

            ps = conn.prepareStatement(sqlQuery);
            //Get API Id
            int apiId;
            apiId = getAPIID(apiIdentifier, conn);
            if (apiId == -1) {
                String msg = "Could not load API record for: " + apiIdentifier.getApiName();
                log.error(msg);
            }

            for (Object storeObject : apiStoreSet) {
                APIStore store = (APIStore) storeObject;
                ps.setString(1, store.getEndpoint());
                ps.setString(2, store.getType());
                ps.setInt(3, apiId);
                ps.setString(4, store.getName());
                ps.addBatch();
            }

            ps.executeBatch();
            ps.clearBatch();

            conn.commit();
        } catch (SQLException e) {
            log.error("Error while updating External APIStore details to the database for API : ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, null, null);
        }
    }

    /**
     * Return external APIStore details on successfully APIs published
     *
     * @param apiId APIIdentifier
     * @return Set of APIStore
     * @throws APIManagementException
     */
    public Set<APIStore> getExternalAPIStoresDetails(APIIdentifier apiId) throws APIManagementException {
        Connection conn = null;
        Set<APIStore> storesSet = new HashSet<APIStore>();
        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);

            storesSet = getExternalAPIStoresDetails(apiId, conn);

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error("Failed to rollback getting external apistore details ", e1);
                }
            }
            log.error("Failed to get external apistore details", e);
        } catch (APIManagementException e) {
            log.error("Failed to get external apistore details", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(null, conn, null);
        }
        return storesSet;
    }

    /**
     * Get external APIStores details which are stored in database
     *
     * @param apiIdentifier API Identifier
     * @throws APIManagementException if failed to get external APIStores
     */
    public Set<APIStore> getExternalAPIStoresDetails(APIIdentifier apiIdentifier, Connection conn)
            throws APIManagementException, SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        Set<APIStore> storesSet = new HashSet<APIStore>();
        try {
            conn = APIMgtDBUtil.getConnection();
            //This query to add external APIStores to database table
            String sqlQuery = SQLConstants.GET_EXTERNAL_API_STORE_DETAILS_SQL;

            ps = conn.prepareStatement(sqlQuery);
            int apiId;
            apiId = getAPIID(apiIdentifier, conn);
            if (apiId == -1) {
                String msg = "Could not load API record for: " + apiIdentifier.getApiName();
                log.error(msg);
                throw new APIManagementException(msg);
            }
            ps.setInt(1, apiId);
            rs = ps.executeQuery();
            while (rs.next()) {
                APIStore store = new APIStore();
                store.setName(rs.getString("STORE_ID"));
                store.setDisplayName(rs.getString("STORE_DISPLAY_NAME"));
                store.setEndpoint(rs.getString("STORE_ENDPOINT"));
                store.setType(rs.getString("STORE_TYPE"));
                store.setPublished(true);
                storesSet.add(store);
            }
        } catch (SQLException e) {
            handleException("Error while getting External APIStore details from the database for  the API : " +
                            apiIdentifier.getApiName() + '-' + apiIdentifier.getVersion(), e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
        return storesSet;
    }

    public void addScopes(Set<?> objects, int api_id, int tenantID) throws APIManagementException {

        Connection conn = null;
        PreparedStatement ps = null, ps2 = null;
        ResultSet rs = null;

        String scopeEntry = SQLConstants.ADD_SCOPE_ENTRY_SQL;
        String scopeLink = SQLConstants.ADD_SCOPE_LINK_SQL;
        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);

            String scopeId = "SCOPE_ID";
            if (conn.getMetaData().getDriverName().contains("PostgreSQL")) {
                scopeId = "scope_id";
            }

            if (objects != null) {
                for (Object object : objects) {
                    ps = conn.prepareStatement(scopeEntry, new String[]{scopeId});
                    ps2 = conn.prepareStatement(scopeLink);

                    if (object instanceof URITemplate) {
                        URITemplate uriTemplate = (URITemplate) object;

                        if (uriTemplate.getScope() == null) {
                            continue;
                        }
                        ps.setString(1, uriTemplate.getScope().getKey());
                        ps.setString(2, uriTemplate.getScope().getName());
                        ps.setString(3, uriTemplate.getScope().getDescription());
                        ps.setInt(4, tenantID);
                        ps.setString(5, uriTemplate.getScope().getRoles());
                        ps.execute();
                        rs = ps.getGeneratedKeys();
                        if (rs.next()) {
                            uriTemplate.getScope().setId(rs.getInt(1));
                        }

                        ps2.setInt(1, api_id);
                        ps2.setInt(2, uriTemplate.getScope().getId());
                        ps2.execute();
                        conn.commit();
                    } else if (object instanceof Scope) {
                        Scope scope = (Scope) object;
                        ps.setString(1, scope.getKey());
                        ps.setString(2, scope.getName());
                        ps.setString(3, scope.getDescription());
                        ps.setInt(4, tenantID);
                        ps.setString(5, scope.getRoles());
                        ps.execute();
                        rs = ps.getGeneratedKeys();
                        if (rs.next()) {
                            scope.setId(rs.getInt(1));
                        }
                        ps2.setInt(1, api_id);
                        ps2.setInt(2, scope.getId());
                        ps2.execute();
                        conn.commit();
                    }
                }
            }
        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException e1) {
                handleException("Error occurred while Rolling back changes done on Scopes Creation", e1);
            }
            handleException("Error occurred while creating scopes ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
            APIMgtDBUtil.closeAllConnections(ps2, null, null);
        }
    }

    public Set<Scope> getAPIScopes(APIIdentifier identifier) throws APIManagementException {
        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        Set<Scope> scopes = new LinkedHashSet<Scope>();
        int apiId;
        try {
            conn = APIMgtDBUtil.getConnection();
            apiId = getAPIID(identifier, conn);

            String sqlQuery = SQLConstants.GET_API_SCOPES_SQL;
            if (conn.getMetaData().getDriverName().contains("Oracle")) {
                sqlQuery = SQLConstants.GET_API_SCOPES_ORACLE_SQL;
            }

            ps = conn.prepareStatement(sqlQuery);
            ps.setInt(1, apiId);
            resultSet = ps.executeQuery();
            while (resultSet.next()) {
                Scope scope = new Scope();
                scope.setId(resultSet.getInt(1));
                scope.setKey(resultSet.getString(2));
                scope.setName(resultSet.getString(3));
                scope.setDescription(resultSet.getString(4));
                scope.setRoles(resultSet.getString(5));
                scopes.add(scope);
            }
        } catch (SQLException e) {
            handleException("Failed to retrieve api scopes ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, resultSet);
        }
        return scopes;
    }

    public Set<Scope> getScopesBySubscribedAPIs(List<APIIdentifier> identifiers) throws APIManagementException {
        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        Set<Scope> scopes = new LinkedHashSet<Scope>();
        List<Integer> apiIds = new ArrayList<Integer>();

        try {
            conn = APIMgtDBUtil.getConnection();
            for (APIIdentifier identifier : identifiers) {
                apiIds.add(getAPIID(identifier, conn));
            }

            String commaSeparatedIds = StringUtils.join(apiIds.iterator(), ',');
            String sqlQuery = SQLConstants.GET_SCOPE_BY_SUBSCRIBED_API_PREFIX + commaSeparatedIds + SQLConstants
                    .GET_SCOPE_BY_SUBSCRIBED_ID_SUFFIX;

            if (conn.getMetaData().getDriverName().contains("Oracle")) {
                sqlQuery = SQLConstants.GET_SCOPE_BY_SUBSCRIBED_ID_ORACLE_SQL + commaSeparatedIds +
                           SQLConstants.GET_SCOPE_BY_SUBSCRIBED_ID_SUFFIX;
            }

            ps = conn.prepareStatement(sqlQuery);
            resultSet = ps.executeQuery();
            while (resultSet.next()) {
                Scope scope = new Scope();
                scope.setKey(resultSet.getString(1));
                scope.setName(resultSet.getString(2));
                scope.setDescription(resultSet.getString(3));
                scope.setRoles(resultSet.getString(4));
                scopes.add(scope);
            }
        } catch (SQLException e) {
            handleException("Failed to retrieve api scopes ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, resultSet);
        }
        return scopes;
    }

    public Set<Scope> getAPIScopesByScopeKey(String scopeKey, int tenantId) throws APIManagementException {
        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        Set<Scope> scopes = new LinkedHashSet<Scope>();
        try {
            String sqlQuery = SQLConstants.GET_SCOPES_BY_SCOPE_KEY_SQL;
            conn = APIMgtDBUtil.getConnection();

            ps = conn.prepareStatement(sqlQuery);
            ps.setString(1, scopeKey);
            ps.setInt(2, tenantId);
            resultSet = ps.executeQuery();
            while (resultSet.next()) {
                Scope scope = new Scope();
                scope.setId(resultSet.getInt("SCOPE_ID"));
                scope.setKey(resultSet.getString("SCOPE_KEY"));
                scope.setName(resultSet.getString("NAME"));
                scope.setDescription(resultSet.getString("DESCRIPTION"));
                scope.setRoles(resultSet.getString("ROLES"));
                scopes.add(scope);
            }
        } catch (SQLException e) {
            handleException("Failed to retrieve api scopes ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, resultSet);
        }
        return scopes;
    }

    public Set<Scope> getScopesByScopeKeys(String scopeKeys, int tenantId) throws APIManagementException {
        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        Set<Scope> scopes = new LinkedHashSet<Scope>();
        List<String> inputScopeList = Arrays.asList(scopeKeys.split(" "));
        StringBuilder scopeStrBuilder = new StringBuilder();
        for (String inputScope : inputScopeList) {
            scopeStrBuilder.append('\'').append(inputScope).append("',");
        }
        String scopesString = scopeStrBuilder.toString();
        scopesString = scopesString.substring(0, scopesString.length() - 1);
        try {
            conn = APIMgtDBUtil.getConnection();

            String sqlQuery = SQLConstants.GET_SCOPES_BY_SCOPE_KEYS_PREFIX + scopesString + SQLConstants
                    .GET_SCOPES_BY_SCOPE_KEYS_SUFFIX;

            ps = conn.prepareStatement(sqlQuery);
            ps.setInt(1, tenantId);
            resultSet = ps.executeQuery();
            while (resultSet.next()) {
                Scope scope = new Scope();
                scope.setId(resultSet.getInt("SCOPE_ID"));
                scope.setKey(resultSet.getString("SCOPE_KEY"));
                scope.setName(resultSet.getString("NAME"));
                scope.setDescription(resultSet.getString("DESCRIPTION"));
                scope.setRoles(resultSet.getString("ROLES"));
                scopes.add(scope);
            }
        } catch (SQLException e) {
            handleException("Failed to retrieve api scopes ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, resultSet);
        }
        return scopes;
    }

    /**
     * update URI templates define for an API
     *
     * @param api
     * @throws APIManagementException
     */
    public void updateScopes(API api, int tenantId) throws APIManagementException {

        Connection connection = null;
        PreparedStatement prepStmt = null;
        int apiId = -1;

        String deleteScopes = SQLConstants.REMOVE_SCOPE_SQL;
        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);

            apiId = getAPIID(api.getId(), connection);
            if (apiId == -1) {
                //application addition has failed
                return;
            }

            prepStmt = connection.prepareStatement(deleteScopes);
            prepStmt.setInt(1, apiId);
            prepStmt.execute();

            connection.commit();
        } catch (SQLException e) {
            handleException("Error while deleting Scopes for API : " + api.getId(), e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, null);
        }
        addScopes(api.getUriTemplates(), apiId, tenantId);
    }

    public HashMap<String, String> getResourceToScopeMapping(APIIdentifier identifier) throws APIManagementException {
        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        HashMap<String, String> map = new HashMap<String, String>();
        int apiId;
        try {
            String sqlQuery = SQLConstants.GET_RESOURCE_TO_SCOPE_MAPPING_SQL;
            apiId = getAPIID(identifier, conn);

            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            ps.setInt(1, apiId);
            resultSet = ps.executeQuery();
            while (resultSet.next()) {
                map.put(resultSet.getString(1), resultSet.getString(2));
            }
        } catch (SQLException e) {
            handleException("Failed to retrieve api scopes ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, resultSet);
        }
        return map;
    }

    public Map<String, String> getScopeRolesOfApplication(String consumerKey) throws APIManagementException {
        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;

        try {
            conn = APIMgtDBUtil.getConnection();

            String sqlQuery = SQLConstants.GET_SCOPE_ROLES_OF_APPLICATION_SQL;

            ps = conn.prepareStatement(sqlQuery);
            ps.setString(1, consumerKey);
            resultSet = ps.executeQuery();
            Map<String, String> scopes = new HashMap<String, String>();
            while (resultSet.next()) {
                scopes.put(resultSet.getString("SCOPE_KEY"), resultSet.getString("ROLES"));
            }
            return scopes;
        } catch (SQLException e) {
            handleException("Failed to retrieve scopes of application" + consumerKey, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, resultSet);
        }
        return null;
    }

    @Deprecated
    public String getUserFromOauthToken(String oauthToken) throws APIManagementException {
        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        String tokenOwner = null;

        try {
            String getUserQuery = SQLConstants.GET_USERS_FROM_OAUTH_TOKEN_SQL;

            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(getUserQuery);
            ps.setString(1, oauthToken);
            resultSet = ps.executeQuery();
            if (resultSet.next()) {
                tokenOwner = resultSet.getString("USER_ID");
            }
            return tokenOwner;
        } catch (SQLException e) {
            handleException("Failed to retrieve user ID for given OAuth token", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, resultSet);
        }
        return null;
    }


    /**
     * Remove scope entries from DB, when delete APIs
     *
     * @param apiIdentifier The {@link APIIdentifier} of the API
     */
    private void removeAPIScope(APIIdentifier apiIdentifier) throws APIManagementException {
        Set<Scope> scopes = getAPIScopes(apiIdentifier);

        Connection connection = null;
        PreparedStatement prepStmt = null;
        PreparedStatement deleteOauth2ResourceScopePrepStmt = null;
        PreparedStatement deleteOauth2ScopePrepStmt = null;
        int scopeId;
        int apiId = -1;

        String deleteAPIScopeQuery = SQLConstants.REMOVE_FROM_API_SCOPES_SQL;
        String deleteOauth2ScopeQuery = SQLConstants.REMOVE_FROM_OAUTH_SCOPE_SQL;
        String deleteOauth2ResourceScopeQuery = SQLConstants.REMOVE_FROM_OAUTH_RESOURCE_SQL;

        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);

            prepStmt = connection.prepareStatement(deleteAPIScopeQuery);
            prepStmt.setInt(1, apiId);
            prepStmt.execute();

            if (!scopes.isEmpty()) {
                deleteOauth2ResourceScopePrepStmt = connection.prepareStatement(deleteOauth2ResourceScopeQuery);
                deleteOauth2ScopePrepStmt = connection.prepareStatement(deleteOauth2ScopeQuery);
                for (Scope scope : scopes) {
                    scopeId = scope.getId();

                    deleteOauth2ResourceScopePrepStmt.setInt(1, scopeId);
                    deleteOauth2ResourceScopePrepStmt.addBatch();

                    deleteOauth2ScopePrepStmt.setInt(1, scopeId);
                    deleteOauth2ScopePrepStmt.addBatch();
                }
                deleteOauth2ResourceScopePrepStmt.executeBatch();
                deleteOauth2ScopePrepStmt.executeBatch();
            }

            connection.commit();
        } catch (SQLException e) {
            handleException("Error while removing the scopes for the API: " +
                            apiIdentifier.getApiName() + " from the database", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(deleteOauth2ResourceScopePrepStmt, null, null);
            APIMgtDBUtil.closeAllConnections(deleteOauth2ScopePrepStmt, null, null);
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, null);
        }
    }

    /**
     * Delete a user subscription based on API_ID, APP_ID, TIER_ID
     *
     * @param apiId - subscriber API ID
     * @param appId - application ID used to subscribe
     * @throws java.sql.SQLException - Letting the caller to handle the roll back
     */
    private void deleteSubscriptionByApiIDAndAppID(int apiId, int appId, Connection conn) throws SQLException {
        String deleteQuery = SQLConstants.REMOVE_SUBSCRIPTION_BY_APPLICATION_ID_SQL;
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(deleteQuery);
            ps.setInt(1, apiId);
            ps.setInt(2, appId);

            ps.executeUpdate();
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, null, null);
        }
    }

    /**
     * Check the given api name is already available in the api table under given tenant domain
     *
     * @param apiName      candidate api name
     * @param tenantDomain tenant domain name
     * @return true if the name is already available
     * @throws APIManagementException
     */
    public boolean isApiNameExist(String apiName, String tenantDomain) throws APIManagementException {
        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet resultSet = null;
        String contextParam = "/t/";

        String query = SQLConstants.GET_API_NAME_NOT_MATCHING_CONTEXT_SQL;
        if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            query = SQLConstants.GET_API_NAME_MATCHING_CONTEXT_SQL;
            contextParam += tenantDomain + '/';
        }

        try {
            connection = APIMgtDBUtil.getConnection();

            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, apiName);
            prepStmt.setString(2, contextParam + '%');
            resultSet = prepStmt.executeQuery();

            int apiCount = 0;
            if (resultSet != null) {
                while (resultSet.next()) {
                    apiCount = resultSet.getInt("API_COUNT");
                }
            }
            if (apiCount > 0) {
                return true;
            }
        } catch (SQLException e) {
            handleException("Failed to check api Name availability : " + apiName, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, resultSet);
        }
        return false;
    }

    public Set<String> getActiveTokensOfConsumerKey(String consumerKey) throws APIManagementException {
        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;

        Set<String> tokens = null;
        try {
            conn = APIMgtDBUtil.getConnection();
            String sqlQuery = SQLConstants.GET_ACTIVE_TOKEN_OF_CONSUMER_KEY_SQL;

            ps = conn.prepareStatement(sqlQuery);
            ps.setString(1, consumerKey);
            resultSet = ps.executeQuery();
            tokens = new HashSet<String>();
            while (resultSet.next()) {
                tokens.add(APIUtil.decryptToken(resultSet.getString("ACCESS_TOKEN")));
            }
        } catch (SQLException e) {
            handleException("Failed to get active access tokens for consumerKey " + consumerKey, e);
        } catch (CryptoException e) {
            handleException("Token decryption failed of an active access token of consumerKey " + consumerKey, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, resultSet);
        }
        return tokens;
    }

    /**
     * Check the given scope key is already available under given tenant
     *
     * @param scopeKey candidate scope key
     * @param tenantId tenant id
     * @return true if the scope key is already available
     * @throws APIManagementException
     */
    public boolean isScopeKeyExist(String scopeKey, int tenantId) throws APIManagementException {
        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet resultSet = null;

        String query = SQLConstants.GET_SCOPE_KEY_SQL;
        try {
            connection = APIMgtDBUtil.getConnection();

            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, scopeKey);
            prepStmt.setInt(2, tenantId);
            resultSet = prepStmt.executeQuery();

            int scopeCount = 0;
            if (resultSet != null) {
                while (resultSet.next()) {
                    scopeCount = resultSet.getInt("SCOPE_COUNT");
                }
            }
            if (scopeCount > 0) {
                return true;
            }
        } catch (SQLException e) {
            handleException("Failed to check Scope Key availability : " + scopeKey, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, resultSet);
        }
        return false;
    }

    /**
     * Check whether the given scope key is already assigned to another API than given under given tenant
     *
     * @param identifier API Identifier
     * @param scopeKey   candidate scope key
     * @param tenantId   tenant id
     * @return true if the scope key is already available
     * @throws APIManagementException if failed to check the context availability
     */
    public boolean isScopeKeyAssigned(APIIdentifier identifier, String scopeKey, int tenantId)
            throws APIManagementException {
        Connection connection = null;
        PreparedStatement prepStmt = null;
        PreparedStatement prepStmt2 = null;
        ResultSet resultSet = null;
        ResultSet resultSet2 = null;

        String apiScopeQuery = SQLConstants.GET_API_SCOPE_SQL;
        String getApiQuery = SQLConstants.GET_API_ID_SQL;

        try {
            connection = APIMgtDBUtil.getConnection();

            prepStmt = connection.prepareStatement(apiScopeQuery);
            prepStmt.setString(1, scopeKey);
            prepStmt.setInt(2, tenantId);
            resultSet = prepStmt.executeQuery();

            if (resultSet != null && resultSet.next()) {
                int apiID = resultSet.getInt("API_ID");
                String provider = resultSet.getString("API_PROVIDER");
                String apiName = resultSet.getString("API_NAME");

                prepStmt2 = connection.prepareStatement(getApiQuery);
                prepStmt2.setString(1, APIUtil.replaceEmailDomainBack(identifier.getProviderName()));
                prepStmt2.setString(2, identifier.getApiName());
                prepStmt2.setString(3, identifier.getVersion());
                resultSet2 = prepStmt2.executeQuery();

                if (resultSet2 != null && resultSet2.next()) {
                    //If the API ID is different from the one being saved
                    if (apiID != resultSet2.getInt("API_ID")) {
                        //Check if the provider name and api name is same.
                        if (provider.equals(APIUtil.replaceEmailDomainBack(identifier.getProviderName())) && apiName
                                .equals(identifier.getApiName())) {

                            //Return false since this means we're attaching the scope to another version of the API.
                            return false;
                        }
                        return true;
                    } else {
                        return false;
                    }
                }
            }
        } catch (SQLException e) {
            handleException("Failed to check Scope Key availability : " + scopeKey, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt2, null, resultSet2);
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, resultSet);
        }
        return false;
    }

    public boolean isDuplicateContextTemplate(String contextTemplate) throws APIManagementException {
        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;

        String sqlQuery = SQLConstants.GET_CONTEXT_TEMPLATE_COUNT_SQL;
        try {
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            ps.setString(1, contextTemplate);

            resultSet = ps.executeQuery();
            if (resultSet.next()) {
                int count = resultSet.getInt("CTX_COUNT");
                return count > 0;
            }
        } catch (SQLException e) {
            handleException("Failed to count contexts which match " + contextTemplate, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, resultSet);
        }
        return false;
    }

    /**
     * Retrieve list of API versions that matches given API name
     *
     * @param apiName name of the api
     * @return list of api versions
     * @throws APIManagementException
     */
    public List<String> getAPIVersionsMatchingApiName(String apiName)
            throws APIManagementException {
        Connection conn = null;
        PreparedStatement ps = null;
        List<String> versionList = new ArrayList<String>();
        ResultSet resultSet = null;

        String sqlQuery = SQLConstants.GET_VERSIONS_MATCHES_API_NAME_SQL;
        try {
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            ps.setString(1, apiName);
            resultSet = ps.executeQuery();
            while (resultSet.next()) {
                versionList.add(resultSet.getString("API_VERSION"));
            }
        } catch (SQLException e) {
            handleException("Failed to get API versions matches API name" + apiName, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, resultSet);
        }
        return versionList;
    }


    /**
     * @param consumerKey
     * @return
     */
    public boolean isMappingExistsforConsumerKey(String consumerKey) throws APIManagementException {
        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;

        String sqlQuery = SQLConstants.GET_APPLICATION_MAPPING_FOR_CONSUMER_KEY_SQL;
        try {
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            ps.setString(1, consumerKey);

            resultSet = ps.executeQuery();
            // We only expect one result.
            if (resultSet.next()) {
                String applicationId = resultSet.getString("APPLICATION_ID");
                return (applicationId != null && !applicationId.isEmpty());
            }
        } catch (SQLException e) {
            handleException("Failed to get Application ID by consumerKey ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, resultSet);
        }
        return false;
    }

    /**
     * @param applicationId
     * @param keyType
     * @return
     */
    public String getConsumerkeyByApplicationIdAndKeyType(String applicationId, String keyType)
            throws APIManagementException {
        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        String consumerKey = null;
        try {
            conn = APIMgtDBUtil.getConnection();

            String sqlQuery = SQLConstants.GET_CONSUMER_KEY_BY_APPLICATION_AND_KEY_SQL;

            ps = conn.prepareStatement(sqlQuery);
            ps.setInt(1, Integer.parseInt(applicationId));
            ps.setString(2, keyType);
            resultSet = ps.executeQuery();

            while (resultSet.next()) {
                consumerKey = resultSet.getString("CONSUMER_KEY");
            }
        } catch (SQLException e) {
            handleException("Failed to get consumer key by applicationId " + applicationId + "and keyType " +
                            keyType, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, resultSet);
        }
        return consumerKey;
    }

    /**
     * Get external APIStores details which are stored in database
     *
     * @param apiIdentifier API Identifier
     * @throws APIManagementException if failed to get external APIStores
     */
    public String getLastPublishedAPIVersionFromAPIStore(APIIdentifier apiIdentifier, String storeName)
            throws APIManagementException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection conn = null;
        String version = null;
        try {
            conn = APIMgtDBUtil.getConnection();
            String sqlQuery = SQLConstants.GET_LAST_PUBLISHED_API_VERSION_SQL;
            ps = conn.prepareStatement(sqlQuery);
            ps.setString(1, apiIdentifier.getProviderName());
            ps.setString(2, apiIdentifier.getApiName());
            ps.setString(3, storeName);
            rs = ps.executeQuery();
            while (rs.next()) {
                version = rs.getString("API_VERSION");
            }
        } catch (SQLException e) {
            handleException("Error while getting External APIStore details from the database for  the API : " +
                            apiIdentifier.getApiName() + '-' + apiIdentifier.getVersion(), e);

        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
        return version;
    }

    private String getScopeString(List<String> scopes) {
        return StringUtils.join(scopes, " ");
    }

    /**
     * Find all active access tokens of a given user.
     *
     * @param username - Username of the user
     * @return - The set of active access tokens of the user.
     */
    public Set<String> getActiveAccessTokensOfUser(String username) throws APIManagementException {

        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;

        Set<String> tokens = null;

        String accessTokenStoreTable = APIConstants.ACCESS_TOKEN_STORE_TABLE;
        accessTokenStoreTable = getAccessTokenStoreTableNameOfUserId(username, accessTokenStoreTable);

        int tenantId = IdentityTenantUtil.getTenantIdOfUser(username);
        String userStoreDomain = IdentityUtil.extractDomainFromName(username).toUpperCase();
        if (StringUtils.isEmpty(userStoreDomain)) {
            userStoreDomain = IdentityUtil.getPrimaryDomainName();
        } else {
            //IdentityUtil doesn't have a function to remove the domain name from the username. Using the UserCoreUtil.
            username = UserCoreUtil.removeDomainFromName(username);
        }

        try {
            conn = APIMgtDBUtil.getConnection();
            String sqlQuery = SQLConstants.GET_ACTIVE_TOKENS_OF_USER_PREFIX + accessTokenStoreTable + SQLConstants
                    .GET_ACTIVE_TOKENS_OF_USER_SUFFIX;

            ps = conn.prepareStatement(sqlQuery);
            ps.setString(1, MultitenantUtils.getTenantAwareUsername(username));
            ps.setInt(2, tenantId);
            ps.setString(3, userStoreDomain.toLowerCase());
            resultSet = ps.executeQuery();
            tokens = new HashSet<String>();
            while (resultSet.next()) {
                tokens.add(APIUtil.decryptToken(resultSet.getString("ACCESS_TOKEN")));
            }
        } catch (SQLException e) {
            handleException("Failed to get active access tokens of user " + username, e);
        } catch (CryptoException e) {
            handleException("Token decryption failed of an active access token of user " + username, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, resultSet);
        }
        return tokens;
    }

//    public TokenGenerator getTokenGenerator() {
//        return tokenGenerator;
//    }

    private String getAccessTokenStoreTableNameOfUserId(String userId, String accessTokenStoreTable)
            throws APIManagementException {
        if (APIUtil.checkAccessTokenPartitioningEnabled() && APIUtil.checkUserNameAssertionEnabled()) {
            return APIUtil.getAccessTokenStoreTableFromUserId(userId);
        }
        return accessTokenStoreTable;
    }

    private String getAccessTokenStoreTableFromAccessToken(String accessToken, String accessTokenStoreTable)
            throws APIManagementException {
        if (APIUtil.checkAccessTokenPartitioningEnabled() && APIUtil.checkUserNameAssertionEnabled()) {
            return APIUtil.getAccessTokenStoreTableFromAccessToken(accessToken);
        }
        return accessTokenStoreTable;
    }

    /**
     * This method will fetch all alerts type that is available in AM_ALERT_TYPES.
     * @param stakeHolder the name of the stakeholder. whether its "subscriber", "publisher" or
     * "admin-dashboard"
     * @return List of alert types
     * @throws APIManagementException
     */
    public HashMap<Integer,String> getAllAlertTypesByStakeHolder(String stakeHolder) throws APIManagementException {
        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        HashMap<Integer, String> map = new HashMap<Integer, String>();

        try {
            conn = APIMgtDBUtil.getConnection();
            String sqlQuery;
            if(stakeHolder.equals("admin-dashboard")){
                sqlQuery = SQLConstants.GET_ALL_ALERT_TYPES_FOR_ADMIN;
                ps = conn.prepareStatement(sqlQuery);
            }else {
                sqlQuery = SQLConstants.GET_ALL_ALERT_TYPES;
                ps = conn.prepareStatement(sqlQuery);
                ps.setString(1, stakeHolder);
            }

            resultSet = ps.executeQuery();
            while (resultSet.next()) {
                map.put(resultSet.getInt(1),resultSet.getString(2));
            }
        } catch (SQLException e) {
            handleException("Failed to retrieve alert types ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, resultSet);
        }
        return map;
    }

    /**
     *
     * @param userName user name with tenant domain ex: admin@carbon.super
     * @param stakeHolder value "p" for publisher value "s" for subscriber value "a" for admin
     * @return map of saved values of alert types.
     * @throws APIManagementException
     */
    public List<Integer> getSavedAlertTypesIdsByUserNameAndStakeHolder(String userName,String stakeHolder) throws APIManagementException{
        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        List<Integer> list = new ArrayList<Integer>();

        try {
            String sqlQuery;
            conn = APIMgtDBUtil.getConnection();
            sqlQuery = SQLConstants.GET_SAVED_ALERT_TYPES_BY_USERNAME;
            ps = conn.prepareStatement(sqlQuery);
            ps.setString(1, userName);
            ps.setString(2,stakeHolder);
            resultSet = ps.executeQuery();
            while (resultSet.next()) {
                list.add(resultSet.getInt(1));
            }
        } catch (SQLException e) {
            handleException("Failed to retrieve saved alert types by user name. ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, resultSet);
        }
        return list;
    }

    /**
     * This method will retrieve saved emails list by user name and stakeholder.
     * @param userName user name.
     * @param stakeHolder "publisher" , "subscriber" or "admin-dashboard"
     * @return
     * @throws APIManagementException
     */
    public List<String> retrieveSavedEmailList(String userName, String stakeHolder) throws APIManagementException {

        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        List<String> list = new ArrayList<String>();

        try {
            String sqlQuery;
            conn = APIMgtDBUtil.getConnection();
            sqlQuery = SQLConstants.GET_SAVED_ALERT_EMAILS;
            ps = conn.prepareStatement(sqlQuery);
            ps.setString(1, userName);
            ps.setString(2,stakeHolder);
            resultSet = ps.executeQuery();
            while (resultSet.next()) {
                list.add(resultSet.getString(1));
            }
        } catch (SQLException e) {
            handleException("Failed to retrieve saved alert types by user name. ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, resultSet);
        }
        return list;

    }

    /**
     * This method will delete all email alert subscriptions details from tables
     * @param userName
     * @param agent whether its publisher or store or admin dash board.
     */
    public void unSubscribeAlerts(String userName, String agent) throws APIManagementException, SQLException {

        Connection connection;
        PreparedStatement ps = null;
        ResultSet rs = null;
        connection = APIMgtDBUtil.getConnection();
        connection.setAutoCommit(false);

        try {
            connection.setAutoCommit(false);
            String alertTypesQuery = SQLConstants.ADD_ALERT_TYPES_VALUES;

            String deleteAlertTypesByUserNameAndStakeHolderQuery = SQLConstants.DELETE_ALERTTYPES_BY_USERNAME_AND_STAKE_HOLDER;

            ps = connection.prepareStatement(deleteAlertTypesByUserNameAndStakeHolderQuery);
            ps.setString(1, userName);
            ps.setString(2, agent);
            ps.executeUpdate();

            String getEmailListIdByUserNameAndStakeHolderQuery = SQLConstants.GET_EMAILLISTID_BY_USERNAME_AND_STAKEHOLDER;
            ps = connection.prepareStatement(getEmailListIdByUserNameAndStakeHolderQuery);
            ps.setString(1, userName);
            ps.setString(2,agent);
            rs = ps.executeQuery();
            int emailListId = 0;
            while (rs.next()) {
                emailListId = rs.getInt(1);
            }
            if(emailListId != 0) {
                String deleteEmailListDetailsByEmailListId = SQLConstants.DELETE_EMAILLIST_BY_EMAIL_LIST_ID;
                ps = connection.prepareStatement(deleteEmailListDetailsByEmailListId);
                ps.setInt(1, emailListId);
                ps.executeUpdate();

            }

            connection.commit();

        } catch (SQLException e) {
            handleException("Failed to delete alert email data.", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, connection, rs);

        }


    }

    /**
     *
     * @param userName User name.
     * @param emailList Comma separated email list.
     * @param alertTypesIDList Comma separated alert types list.
     * @param stakeHolder if pram value = p we assume those changes from publisher if param value = s those data belongs to
     * subscriber.
     * @throws APIManagementException
     * @throws SQLException
     */
    public void addAlertTypesConfigInfo(String userName, String emailList, String alertTypesIDList, String  stakeHolder)
            throws APIManagementException, SQLException {

        Connection connection;
        PreparedStatement ps = null;
        ResultSet rs = null;
        connection = APIMgtDBUtil.getConnection();
        connection.setAutoCommit(false);
        try {

            String alertTypesQuery = SQLConstants.ADD_ALERT_TYPES_VALUES;

            String deleteAlertTypesByUserNameAndStakeHolderQuery = SQLConstants.DELETE_ALERTTYPES_BY_USERNAME_AND_STAKE_HOLDER;

            ps = connection.prepareStatement(deleteAlertTypesByUserNameAndStakeHolderQuery);
            ps.setString(1, userName);
            ps.setString(2, stakeHolder);
            ps.executeUpdate();

            if(!StringUtils.isEmpty(alertTypesIDList)){

                List<String> alertTypeIdList = Arrays.asList(alertTypesIDList.split(","));

                for (String alertTypeId : alertTypeIdList) {
                    PreparedStatement psAlertTypeId = null;
                    try {
                        psAlertTypeId = connection.prepareStatement(alertTypesQuery);
                        psAlertTypeId.setInt(1, Integer.parseInt(alertTypeId));
                        psAlertTypeId.setString(2, userName);
                        psAlertTypeId.setString(3, stakeHolder);
                        psAlertTypeId.execute();
                    }catch (SQLException e){
                        handleException("Error while adding alert types" ,e);
                    }finally {
                        APIMgtDBUtil.closeAllConnections(psAlertTypeId, null, null);
                    }
                }


            }

            String getEmailListIdByUserNameAndStakeHolderQuery = SQLConstants.GET_EMAILLISTID_BY_USERNAME_AND_STAKEHOLDER;
            ps = connection.prepareStatement(getEmailListIdByUserNameAndStakeHolderQuery);
            ps.setString(1, userName);
            ps.setString(2,stakeHolder);
            rs = ps.executeQuery();
            int emailListId = 0;
            while (rs.next()) {
                emailListId = rs.getInt(1);
            }
            if(emailListId != 0){
                String deleteEmailListDetailsByEmailListId = SQLConstants.DELETE_EMAILLIST_BY_EMAIL_LIST_ID;
                ps = connection.prepareStatement(deleteEmailListDetailsByEmailListId);
                ps.setInt(1, emailListId);
                ps.executeUpdate();

                if(!StringUtils.isEmpty(emailList)){

                    List<String> extractedEmailList = Arrays.asList(emailList.split(","));

                    String saveEmailListDetailsQuery = SQLConstants.SAVE_EMAIL_LIST_DETAILS_QUERY;

                    for (String email : extractedEmailList) {
                        PreparedStatement extractedEmailListPs = null;
                        try {
                            extractedEmailListPs = connection.prepareStatement(saveEmailListDetailsQuery);
                            extractedEmailListPs.setInt(1, emailListId);
                            extractedEmailListPs.setString(2, email);
                            extractedEmailListPs.execute();
                        }catch (SQLException e){
                            handleException("Error while save email list.", e);
                        }finally {
                            APIMgtDBUtil.closeAllConnections(extractedEmailListPs, null, null);
                        }
                    }

                }

            } else {

                String emailListSaveQuery = SQLConstants.ADD_ALERT_EMAIL_LIST;

                String dbProductName = connection.getMetaData().getDatabaseProductName();

                ps = connection.prepareStatement(emailListSaveQuery,new String[]{DBUtils.
                        getConvertedAutoGeneratedColumnName(dbProductName, "EMAIL_LIST_ID")});

                ps.setString(1, userName);
                ps.setString(2, stakeHolder);
                ps.execute();

                rs = ps.getGeneratedKeys();

                if (rs.next()) {
                    int generatedEmailIdList = rs.getInt(1);
                    if(!StringUtils.isEmpty(emailList)){

                        List<String> extractedEmailList = Arrays.asList(emailList.split(","));

                        String saveEmailListDetailsQuery = SQLConstants.SAVE_EMAIL_LIST_DETAILS_QUERY;

                        for (String email : extractedEmailList) {
                            PreparedStatement elseExtractedEmailListPS = null;
                            try {
                                elseExtractedEmailListPS = connection.prepareStatement(saveEmailListDetailsQuery);
                                elseExtractedEmailListPS.setInt(1, generatedEmailIdList);
                                elseExtractedEmailListPS.setString(2, email);
                                elseExtractedEmailListPS.execute();
                            }catch (SQLException e){
                                handleException("Error while save email list.", e);
                            }finally {
                                APIMgtDBUtil.closeAllConnections(elseExtractedEmailListPS, null, null);
                            }
                        }

                    }
                }

            }
            connection.commit();

        } catch (SQLException e) {
            handleException("Failed to save alert preferences", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, connection, rs);
        }
    }

    /**
     * Add a Application level throttling policy to database
     *
     * @param policy policy object defining the throttle policy
     * @throws APIManagementException
     */
    public void addApplicationPolicy(ApplicationPolicy policy) throws APIManagementException {
        Connection conn = null;
        PreparedStatement policyStatement = null;
        boolean hasCustomAttrib = false;
        try {
        	 if(policy.getCustomAttributes() != null){
        		 hasCustomAttrib = true;
             }
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);
            String addQuery = SQLConstants.INSERT_APPLICATION_POLICY_SQL;
            if(hasCustomAttrib){
            	addQuery = SQLConstants.INSERT_APPLICATION_POLICY_WITH_CUSTOM_ATTRIB_SQL;
            }
            policyStatement = conn.prepareStatement(addQuery);
            setCommonParametersForPolicy(policyStatement, policy);
            if(hasCustomAttrib){
            	policyStatement.setBlob(12, new ByteArrayInputStream(policy.getCustomAttributes()));
            }
            policyStatement.executeUpdate();

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {

                    // Rollback failed. Exception will be thrown later for upper exception
                    log.error("Failed to rollback the add Application Policy: " + policy.toString(), ex);
                }
            }
            handleException("Failed to add Application Policy: " + policy, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(policyStatement, conn, null);
        }
    }

    /**
     * Add a Subscription level throttling policy to database
     *
     * @param policy policy object defining the throttle policy
     * @throws APIManagementException
     */
    public void addSubscriptionPolicy(SubscriptionPolicy policy) throws APIManagementException {
        Connection conn = null;
        PreparedStatement policyStatement = null;
        boolean hasCustomAttrib = false;

        try {
        	if(policy.getCustomAttributes() != null){
       		 hasCustomAttrib = true;
            }
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);
            String addQuery = SQLConstants.INSERT_SUBSCRIPTION_POLICY_SQL;
            if(hasCustomAttrib){
            	addQuery = SQLConstants.INSERT_SUBSCRIPTION_POLICY_WITH_CUSTOM_ATTRIB_SQL;
            }
            policyStatement = conn.prepareStatement(addQuery);
            setCommonParametersForPolicy(policyStatement, policy);
            policyStatement.setInt(12, policy.getRateLimitCount());
            policyStatement.setString(13, policy.getRateLimitTimeUnit());
            policyStatement.setBoolean(14, policy.isStopOnQuotaReach());
            policyStatement.setString(15, policy.getBillingPlan());
            if(hasCustomAttrib){
            	policyStatement.setBytes(16, policy.getCustomAttributes());
            }
            policyStatement.executeUpdate();

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {

                    // Rollback failed. Exception will be thrown later for upper exception
                    log.error("Failed to rollback the add Subscription Policy: " + policy.toString(), ex);
                }
            }
            handleException("Failed to add Subscription Policy: " + policy, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(policyStatement, conn, null);
        }
    }

    /**
     * Wrapper method for {@link #addAPIPolicy(APIPolicy, Connection)} to add
     * API Policy without managing the database connection manually.
     *
     * @param policy policy object to add
     * @throws APIManagementException
     */
    public APIPolicy addAPIPolicy(APIPolicy policy) throws APIManagementException {
        Connection connection = null;

        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            addAPIPolicy(policy, connection);
            connection.commit();
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {

                    // Rollback failed. Exception will be thrown later for upper exception
                    log.error("Failed to rollback the add Api Policy: " + policy.toString(), ex);
                }
            }
            handleException("Failed to add Api Policy: " + policy, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(null, connection, null);
        }
        return policy;
    }

    /**
     * Add a API level throttling policy to database.
     * <p>
     * If valid policy Id (not -1) is present in the <code>policy</code> object,
     * policy will be inserted with that policy Id.
     * Otherwise policy Id will be auto incremented.
     * </p>
     *
     * @param policy policy object defining the throttle policy
     * @throws SQLException
     */
    private void addAPIPolicy(APIPolicy policy, Connection conn) throws SQLException {
        ResultSet resultSet = null;
        PreparedStatement policyStatement = null;
        String addQuery;
        int policyId = policy.getPolicyId();

        try {

            // Valid policyId is available means policy should be inserted with 'policyId'. (Policy update request)
            if (policyId == -1) {
                addQuery = SQLConstants.ThrottleSQLConstants.INSERT_API_POLICY_SQL;
            } else {
                addQuery = SQLConstants.ThrottleSQLConstants.INSERT_API_POLICY_WITH_ID_SQL;
            }
            String dbProductName = conn.getMetaData().getDatabaseProductName();
            policyStatement = conn.prepareStatement(addQuery, new String[] { DBUtils
                    .getConvertedAutoGeneratedColumnName(dbProductName, "POLICY_ID") });
            setCommonParametersForPolicy(policyStatement, policy);
            policyStatement.setString(12, policy.getUserLevel());
            if (policyId != -1) {

                // Assume policy is deployed if update request is recieved
                policyStatement.setBoolean(10, true);
                policyStatement.setInt(13, policyId);
            }
            policyStatement.executeUpdate();
            resultSet = policyStatement.getGeneratedKeys(); // Get the inserted POLICY_ID (auto incremented value)

            // Returns only single row
            if (resultSet.next()) {

                /*Not sure about below comment :-) (Dhanuka)
                 *  H2 doesn't return generated keys when key is provided (not generated).
                   Therefore policyId should be policy parameter's policyId when it is provided.
                 */
                if (policyId == -1) {
                    policyId = resultSet.getInt(1);
                }
                List<Pipeline> pipelines = policy.getPipelines();
                if(pipelines != null) {
                    for (Pipeline pipeline : pipelines) { // add each pipeline data to AM_CONDITION_GROUP table
                        addPipeline(pipeline, policyId, conn);
                    }
                }
            }
        } finally {
            APIMgtDBUtil.closeAllConnections(policyStatement, null, resultSet);
        }
    }

    /**
     * Add throttling policy pipeline to database
     *
     * @param pipeline condition pipeline
     * @param policyID id of the policy to add pipeline
     * @param conn     database connection. This should be provided inorder to rollback transaction
     * @throws SQLException
     */
	private void addPipeline(Pipeline pipeline, int policyID, Connection conn) throws SQLException {
		PreparedStatement conditionStatement = null;
		ResultSet rs = null;

		try {
			String sqlAddQuery = SQLConstants.ThrottleSQLConstants.INSERT_CONDITION_GROUP_SQL;
			List<Condition> conditionList = pipeline.getConditions();

			// Add data to the AM_CONDITION table
			String dbProductName = conn.getMetaData().getDatabaseProductName();
			conditionStatement = conn.prepareStatement(sqlAddQuery, new String[] { DBUtils
                    .getConvertedAutoGeneratedColumnName(dbProductName, "CONDITION_GROUP_ID") });
			conditionStatement.
                    setInt(1, policyID);
			conditionStatement.setString(2, pipeline.getQuotaPolicy().getType());

			if (PolicyConstants.REQUEST_COUNT_TYPE.equals(pipeline.getQuotaPolicy().getType())) {
				conditionStatement.setLong(3,
						((RequestCountLimit) pipeline.getQuotaPolicy().getLimit()).getRequestCount());
				conditionStatement.setString(4, null);
			} else if (PolicyConstants.BANDWIDTH_TYPE.equals(pipeline.getQuotaPolicy().getType())) {
				BandwidthLimit limit = (BandwidthLimit) pipeline.getQuotaPolicy().getLimit();
				conditionStatement.setLong(3, limit.getDataAmount());
				conditionStatement.setString(4, limit.getDataUnit());
			}

			conditionStatement.setLong(5, pipeline.getQuotaPolicy().getLimit().getUnitTime());
			conditionStatement.setString(6, pipeline.getQuotaPolicy().getLimit().getTimeUnit());
			conditionStatement.setString(7, pipeline.getDescription());
			conditionStatement.executeUpdate();
			rs = conditionStatement.getGeneratedKeys();

			// Add Throttling parameters which have multiple entries
			if (rs != null && rs.next()) {
				int pipelineId = rs.getInt(1); // Get the inserted
												// CONDITION_GROUP_ID (auto
												// incremented value)
                pipeline.setId(pipelineId);
				for (Condition condition : conditionList) {
					if (condition == null) {
						continue;
					}
					String type = condition.getType();
					if (PolicyConstants.IP_RANGE_TYPE.equals(type) || PolicyConstants.IP_SPECIFIC_TYPE.equals(type)) {
						IPCondition ipCondition = (IPCondition) condition;
						addIPCondition(ipCondition, pipelineId, conn);
					}

					if (PolicyConstants.HEADER_TYPE.equals(type)) {
						addHeaderCondition((HeaderCondition) condition, pipelineId, conn);
					} else if (PolicyConstants.QUERY_PARAMETER_TYPE.equals(type)) {
						addQueryParameterCondition((QueryParameterCondition) condition, pipelineId, conn);
					} else if (PolicyConstants.JWT_CLAIMS_TYPE.equals(type)) {
						addJWTClaimsCondition((JWTClaimsCondition) condition, pipelineId, conn);
					}
				}
			}
		} finally {
			APIMgtDBUtil.closeAllConnections(conditionStatement, null, rs);
		}
	}

    /**
     * Add HEADER throttling condition to AM_HEADER_FIELD_CONDITION table
     *
     * @param headerCondition {@link HeaderCondition} with header fieled and value
     * @param pipelineId      id of the pipeline which this condition belongs to
     * @param conn            database connection. This should be provided inorder to rollback transaction
     * @throws SQLException
     */
    private void addHeaderCondition(HeaderCondition headerCondition, int pipelineId, Connection conn)
            throws SQLException {
        PreparedStatement psHeaderCondition = null;

        try {
            String sqlQuery = SQLConstants.ThrottleSQLConstants.INSERT_HEADER_FIELD_CONDITION_SQL;
            psHeaderCondition = conn.prepareStatement(sqlQuery);
            psHeaderCondition.setInt(1, pipelineId);
            psHeaderCondition.setString(2, headerCondition.getHeaderName());
            psHeaderCondition.setString(3, headerCondition.getValue());
            psHeaderCondition.setBoolean(4, headerCondition.isInvertCondition());
            psHeaderCondition.executeUpdate();
        } finally {
            APIMgtDBUtil.closeAllConnections(psHeaderCondition, null, null);
        }
    }

    /**
     * Add QUERY throttling condition to AM_QUERY_PARAMETER_CONDITION table
     *
     * @param queryParameterCondition {@link QueryParameterCondition} with parameter name and value
     * @param pipelineId              id of the pipeline which this condition belongs to
     * @param conn                    database connection. This should be provided inorder to rollback transaction
     * @throws SQLException
     */
    private void addQueryParameterCondition(QueryParameterCondition queryParameterCondition, int pipelineId,
            Connection conn) throws SQLException {
        PreparedStatement psQueryParameterCondition = null;

        try {
            String sqlQuery = SQLConstants.ThrottleSQLConstants.INSERT_QUERY_PARAMETER_CONDITION_SQL;
            psQueryParameterCondition = conn.prepareStatement(sqlQuery);
            psQueryParameterCondition.setInt(1, pipelineId);
            psQueryParameterCondition.setString(2, queryParameterCondition.getParameter());
            psQueryParameterCondition.setString(3, queryParameterCondition.getValue());
            psQueryParameterCondition.setBoolean(4, queryParameterCondition.isInvertCondition());
            psQueryParameterCondition.executeUpdate();
        } finally {
            APIMgtDBUtil.closeAllConnections(psQueryParameterCondition, null, null);
        }
    }

	private void addIPCondition(IPCondition ipCondition, int pipelineId, Connection conn) throws SQLException {
		PreparedStatement statementIPCondition = null;

		try {
			String sqlQuery = SQLConstants.ThrottleSQLConstants.INSERT_IP_CONDITION_SQL;

			statementIPCondition = conn.prepareStatement(sqlQuery);
			String startingIP = ipCondition.getStartingIP();
			String endingIP = ipCondition.getEndingIP();
			String specificIP = ipCondition.getSpecificIP();

			statementIPCondition.setString(1, startingIP);
			statementIPCondition.setString(2, endingIP);
			statementIPCondition.setString(3, specificIP);
			statementIPCondition.setBoolean(4, ipCondition.isInvertCondition());
			statementIPCondition.setInt(5, pipelineId);
			statementIPCondition.executeUpdate();
		} finally {
			APIMgtDBUtil.closeAllConnections(statementIPCondition, null, null);
		}
	}

    /**
     * Add JWTCLAIMS throttling condition to AM_JWT_CLAIM_CONDITION table
     *
     * @param jwtClaimsCondition {@link JWTClaimsCondition} with claim url and claim attribute
     * @param pipelineId         id of the pipeline which this condition belongs to
     * @param conn               database connection. This should be provided inorder to rollback transaction
     * @throws SQLException
     */
    private void addJWTClaimsCondition(JWTClaimsCondition jwtClaimsCondition, int pipelineId, Connection conn)
            throws SQLException {
        PreparedStatement psJWTClaimsCondition = null;

        try {
            String sqlQuery = SQLConstants.ThrottleSQLConstants.INSERT_JWT_CLAIM_CONDITION_SQL;
            psJWTClaimsCondition = conn.prepareStatement(sqlQuery);
            psJWTClaimsCondition.setInt(1, pipelineId);
            psJWTClaimsCondition.setString(2, jwtClaimsCondition.getClaimUrl());
            psJWTClaimsCondition.setString(3, jwtClaimsCondition.getAttribute());
            psJWTClaimsCondition.setBoolean(4, jwtClaimsCondition.isInvertCondition());
            psJWTClaimsCondition.executeUpdate();
        } finally {
            APIMgtDBUtil.closeAllConnections(psJWTClaimsCondition, null, null);
        }
    }

    /**
     * Add a Global level throttling policy to database
     *
     * @param policy Global Policy
     * @throws APIManagementException
     */
    public void addGlobalPolicy(GlobalPolicy policy) throws APIManagementException {
        Connection conn = null;
        PreparedStatement policyStatement = null;
        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);
            String addQuery = SQLConstants.INSERT_GLOBAL_POLICY_SQL;
            policyStatement = conn.prepareStatement(addQuery);
            policyStatement.setString(1, policy.getPolicyName());
            policyStatement.setInt(2, policy.getTenantId());
            policyStatement.setString(3, policy.getKeyTemplate());
            policyStatement.setString(4, policy.getDescription());

            InputStream siddhiQueryInputStream;
            byte[] byteArray = policy.getSiddhiQuery().getBytes(Charset.defaultCharset());
            int lengthOfBytes = byteArray.length;
            siddhiQueryInputStream = new ByteArrayInputStream(byteArray);
            policyStatement.setBinaryStream(5, siddhiQueryInputStream,lengthOfBytes);
            policyStatement.setBoolean(6, false);
            policyStatement.setString(7, UUID.randomUUID().toString());
            policyStatement.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {

                    // rollback failed. exception will be thrown later for upper exception
                    log.error("Failed to rollback the add Global Policy: " + policy.toString(), ex);
                }
            }
            handleException("Failed to add Global Policy: " + policy, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(policyStatement, conn, null);
        }
    }

    /**
     * Retrieves global policy key templates for the given tenantID
     *
     * @param tenantID tenant id
     * @return list of KeyTemplates
     * @throws APIManagementException
     */
    public List<String> getGlobalPolicyKeyTemplates(int tenantID) throws APIManagementException {

        List<String> keyTemplates = new ArrayList<String>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sqlQuery = null;

        try {
            conn = APIMgtDBUtil.getConnection();

            sqlQuery = SQLConstants.GET_GLOBAL_POLICY_KEY_TEMPLATES;

            ps = conn.prepareStatement(sqlQuery);
            ps.setInt(1, tenantID);
            rs = ps.executeQuery();
            while (rs.next()) {
                keyTemplates.add(rs.getString(ThrottlePolicyConstants.COLUMN_KEY_TEMPLATE));
            }

        } catch (SQLException e) {
            handleException("Error while executing SQL to get GLOBAL_POLICY_KEY_TEMPLATES", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
        return keyTemplates;
    }

    /**
     * Returns true if the key template exist in DB
     *
     * @param policy Global Policy
     * @return true if key template already exists
     * @throws APIManagementException
     */
    public boolean isKeyTemplatesExist(GlobalPolicy policy) throws APIManagementException {

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sqlQuery = null;

        try {
            conn = APIMgtDBUtil.getConnection();

            sqlQuery = SQLConstants.GET_GLOBAL_POLICY_KEY_TEMPLATE;

            ps = conn.prepareStatement(sqlQuery);
            ps.setInt(1, policy.getTenantId());
            ps.setString(2, policy.getKeyTemplate());
            ps.setString(3, policy.getPolicyName());
            rs = ps.executeQuery();
            if (rs.next()) {
                return true;
            }

        } catch (SQLException e) {
            handleException("Error while executing SQL to get GLOBAL_POLICY_KEY_TEMPLATE", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
        return false;
    }

    /**
     * Removes a throttling policy from the database
     *
     * @param policyLevel level of the policy to be deleted
     * @param policyName  name of the policy
     * @param tenantId    used to get the tenant id
     * @throws APIManagementException
     */
    public void removeThrottlePolicy(String policyLevel, String policyName, int tenantId)
            throws APIManagementException {
        Connection connection = null;
        PreparedStatement deleteStatement = null;
        String query = null;

        if (PolicyConstants.POLICY_LEVEL_APP.equals(policyLevel)) {
            query = SQLConstants.DELETE_APPLICATION_POLICY_SQL;
        } else if (PolicyConstants.POLICY_LEVEL_SUB.equals(policyLevel)) {
            query = SQLConstants.DELETE_SUBSCRIPTION_POLICY_SQL;
        } else if (PolicyConstants.POLICY_LEVEL_API.equals(policyLevel)) {
            query = SQLConstants.ThrottleSQLConstants.DELETE_API_POLICY_SQL;
        } else if (PolicyConstants.POLICY_LEVEL_GLOBAL.equals(policyLevel)) {
            query = SQLConstants.DELETE_GLOBAL_POLICY_SQL;
        }

        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            deleteStatement = connection.prepareStatement(query);
            deleteStatement.setInt(1, tenantId);
            deleteStatement.setString(2, policyName);
            deleteStatement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            handleException("Failed to remove policy " + policyLevel + '-' + policyName + '-' + tenantId, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(deleteStatement, connection, null);
        }
    }

    /**
     * Get API level policies. Result only contains basic details of the policy,
     * it doesn't contain pipeline information.
     *
     * @param tenantID policies are selected using tenantID
     * @return APIPolicy ArrayList
     * @throws APIManagementException
     */
    public APIPolicy[] getAPIPolicies(int tenantID) throws APIManagementException {
        List<APIPolicy> policies = new ArrayList<APIPolicy>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sqlQuery = SQLConstants.ThrottleSQLConstants.GET_API_POLICIES;

        if (forceCaseInsensitiveComparisons) {
            sqlQuery = SQLConstants.ThrottleSQLConstants.GET_API_POLICIES;
        }

        try {
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            ps.setInt(1, tenantID);
            rs = ps.executeQuery();
            while (rs.next()) {
                APIPolicy apiPolicy = new APIPolicy(rs.getString(ThrottlePolicyConstants.COLUMN_NAME));
                setCommonPolicyDetails(apiPolicy, rs);
                apiPolicy.setUserLevel(rs.getString(ThrottlePolicyConstants.COLUMN_APPLICABLE_LEVEL));

                policies.add(apiPolicy);
            }
        } catch (SQLException e) {
            handleException("Error while executing SQL", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
        return policies.toArray(new APIPolicy[policies.size()]);
    }

    /**
     * Get application level polices
     *
     * @param tenantID polices are selected only belong to specific tenantID
     * @return AppilicationPolicy array list
     */
    public ApplicationPolicy[] getApplicationPolicies(int tenantID) throws APIManagementException {
        List<ApplicationPolicy> policies = new ArrayList<ApplicationPolicy>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        String sqlQuery = SQLConstants.GET_APP_POLICIES;
        if (forceCaseInsensitiveComparisons) {
            sqlQuery = SQLConstants.GET_APP_POLICIES;
        }

        try {
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            ps.setInt(1, tenantID);
            rs = ps.executeQuery();
            while (rs.next()) {
                ApplicationPolicy appPolicy = new ApplicationPolicy(rs.getString(ThrottlePolicyConstants.COLUMN_NAME));
                setCommonPolicyDetails(appPolicy, rs);
                policies.add(appPolicy);
            }
        } catch (SQLException e) {
            handleException("Error while executing SQL", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
        return policies.toArray(new ApplicationPolicy[policies.size()]);
    }

    /**
     * Get all subscription level policeis belongs to specific tenant
     *
     * @param tenantID tenantID filters the polices belongs to specific tenant
     * @return subscriptionPolicy array list
     */
    public SubscriptionPolicy[] getSubscriptionPolicies(int tenantID) throws APIManagementException {
        List<SubscriptionPolicy> policies = new ArrayList<SubscriptionPolicy>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        String sqlQuery = SQLConstants.GET_SUBSCRIPTION_POLICIES;
        if (forceCaseInsensitiveComparisons) {
            sqlQuery = SQLConstants.GET_SUBSCRIPTION_POLICIES;
        }

        try {
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            ps.setInt(1, tenantID);
            rs = ps.executeQuery();
            while (rs.next()) {
                SubscriptionPolicy subPolicy = new SubscriptionPolicy(
                        rs.getString(ThrottlePolicyConstants.COLUMN_NAME));
                setCommonPolicyDetails(subPolicy, rs);
                subPolicy.setRateLimitCount(rs.getInt(ThrottlePolicyConstants.COLUMN_RATE_LIMIT_COUNT));
                subPolicy.setRateLimitTimeUnit(rs.getString(ThrottlePolicyConstants.COLUMN_RATE_LIMIT_TIME_UNIT));
                subPolicy.setStopOnQuotaReach(rs.getBoolean(ThrottlePolicyConstants.COLUMN_STOP_ON_QUOTA_REACH));
                subPolicy.setBillingPlan(rs.getString(ThrottlePolicyConstants.COLUMN_BILLING_PLAN));
                InputStream binary = rs.getBinaryStream(ThrottlePolicyConstants.COLUMN_CUSTOM_ATTRIB);
                if(binary != null){
                	byte[] customAttrib = APIUtil.toByteArray(binary);
                	subPolicy.setCustomAttributes(customAttrib);
                }
                policies.add(subPolicy);
            }
        } catch (SQLException e) {
            handleException("Error while executing SQL", e);
        } catch (IOException e) {
        	handleException("Error while converting input stream to byte array", e);
		} finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
        return policies.toArray(new SubscriptionPolicy[policies.size()]);
    }

    /**
     * Get all Global level policeis belongs to specific tenant
     *
     * @param tenantID
     * @return
     * @throws APIManagementException
     */
    public GlobalPolicy[] getGlobalPolicies(int tenantID) throws APIManagementException {
        List<GlobalPolicy> policies = new ArrayList<GlobalPolicy>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        String sqlQuery = SQLConstants.GET_GLOBAL_POLICIES;
        if (forceCaseInsensitiveComparisons) {
            sqlQuery = SQLConstants.GET_GLOBAL_POLICIES;
        }

        try {
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            ps.setInt(1, tenantID);
            rs = ps.executeQuery();
            while (rs.next()) {
                String siddhiQuery = null;
                GlobalPolicy globalPolicy = new GlobalPolicy(rs.getString(ThrottlePolicyConstants.COLUMN_NAME));
                globalPolicy.setDescription(rs.getString(ThrottlePolicyConstants.COLUMN_DESCRIPTION));
                globalPolicy.setPolicyId(rs.getInt(ThrottlePolicyConstants.COLUMN_POLICY_ID));
                globalPolicy.setUUID(rs.getString(ThrottlePolicyConstants.COLUMN_UUID));
                globalPolicy.setTenantId(rs.getShort(ThrottlePolicyConstants.COLUMN_TENANT_ID));
                globalPolicy.setKeyTemplate(rs.getString(ThrottlePolicyConstants.COLUMN_KEY_TEMPLATE));
                globalPolicy.setDeployed(rs.getBoolean(ThrottlePolicyConstants.COLUMN_DEPLOYED));
                InputStream siddhiQueryBlob = rs.getBinaryStream(ThrottlePolicyConstants.COLUMN_SIDDHI_QUERY);
                if (siddhiQueryBlob != null) {
                    siddhiQuery = APIMgtDBUtil.getStringFromInputStream(siddhiQueryBlob);
                }
                globalPolicy.setSiddhiQuery(siddhiQuery);
                policies.add(globalPolicy);
            }
        } catch (SQLException e) {
            handleException("Error while executing SQL", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
        return policies.toArray(new GlobalPolicy[policies.size()]);
    }


    /**
     * Get a particular Global level policy.
     *
     * @param policyName name of the global polixy
     * @return {@link GlobalPolicy}
     * @throws APIManagementException
     */
    public GlobalPolicy getGlobalPolicy(String policyName) throws APIManagementException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        String sqlQuery = SQLConstants.GET_GLOBAL_POLICY;

        GlobalPolicy globalPolicy = null;
        try {
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            ps.setString(1, policyName);
            rs = ps.executeQuery();

            if (rs.next()) {
                String siddhiQuery = null;
                globalPolicy = new GlobalPolicy(rs.getString(ThrottlePolicyConstants.COLUMN_NAME));
                globalPolicy.setDescription(rs.getString(ThrottlePolicyConstants.COLUMN_DESCRIPTION));
                globalPolicy.setPolicyId(rs.getInt(ThrottlePolicyConstants.COLUMN_POLICY_ID));
                globalPolicy.setUUID(rs.getString(ThrottlePolicyConstants.COLUMN_UUID));
                globalPolicy.setTenantId(rs.getShort(ThrottlePolicyConstants.COLUMN_TENANT_ID));
                globalPolicy.setKeyTemplate(rs.getString(ThrottlePolicyConstants.COLUMN_KEY_TEMPLATE));
                globalPolicy.setDeployed(rs.getBoolean(ThrottlePolicyConstants.COLUMN_DEPLOYED));
                InputStream siddhiQueryBlob = rs.getBinaryStream(ThrottlePolicyConstants.COLUMN_SIDDHI_QUERY);
                if (siddhiQueryBlob != null) {
                    siddhiQuery = APIMgtDBUtil.getStringFromInputStream(siddhiQueryBlob);
                }
                globalPolicy.setSiddhiQuery(siddhiQuery);
            }
        } catch (SQLException e) {
            handleException("Error while executing SQL", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
        return globalPolicy;
    }

    /**
     * Get a particular Global level policy given UUID.
     *
     * @param uuid name of the global polixy
     * @return {@link GlobalPolicy}
     * @throws APIManagementException
     */
    public GlobalPolicy getGlobalPolicyByUUID(String uuid) throws APIManagementException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        String sqlQuery = SQLConstants.GET_GLOBAL_POLICY_BY_UUID;

        GlobalPolicy globalPolicy = null;
        try {
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            ps.setString(1, uuid);
            rs = ps.executeQuery();

            if (rs.next()) {
                String siddhiQuery = null;
                globalPolicy = new GlobalPolicy(rs.getString(ThrottlePolicyConstants.COLUMN_NAME));
                globalPolicy.setDescription(rs.getString(ThrottlePolicyConstants.COLUMN_DESCRIPTION));
                globalPolicy.setPolicyId(rs.getInt(ThrottlePolicyConstants.COLUMN_POLICY_ID));
                globalPolicy.setUUID(rs.getString(ThrottlePolicyConstants.COLUMN_UUID));
                globalPolicy.setTenantId(rs.getShort(ThrottlePolicyConstants.COLUMN_TENANT_ID));
                globalPolicy.setKeyTemplate(rs.getString(ThrottlePolicyConstants.COLUMN_KEY_TEMPLATE));
                globalPolicy.setDeployed(rs.getBoolean(ThrottlePolicyConstants.COLUMN_DEPLOYED));
                InputStream siddhiQueryBlob = rs.getBinaryStream(ThrottlePolicyConstants.COLUMN_SIDDHI_QUERY);
                if (siddhiQueryBlob != null) {
                    siddhiQuery = APIMgtDBUtil.getStringFromInputStream(siddhiQueryBlob);
                }
                globalPolicy.setSiddhiQuery(siddhiQuery);
            }
        } catch (SQLException e) {
            handleException("Error while retrieving global policy by uuid " + uuid, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
        return globalPolicy;
    }

    /**
     * Retrieves {@link APIPolicy} with name <code>policyName</code> and tenant Id <code>tenantNId</code>
     * <p>This will retrieve complete details about the APIPolicy with all pipelins and conditions.</p>
     *
     * @param policyName name of the policy to retrieve from the database
     * @param tenantId   tenantId of the policy
     * @return {@link APIPolicy}
     * @throws APIManagementException
     */
    public APIPolicy getAPIPolicy(String policyName, int tenantId) throws APIManagementException {
        APIPolicy policy = null;
        Connection connection = null;
        PreparedStatement selectStatement = null;
        ResultSet resultSet = null;

        String sqlQuery = SQLConstants.ThrottleSQLConstants.GET_API_POLICY_SQL;
        if (forceCaseInsensitiveComparisons) {
            sqlQuery = SQLConstants.ThrottleSQLConstants.GET_API_POLICY_SQL;
        }

        try {
            connection = APIMgtDBUtil.getConnection();
            selectStatement = connection.prepareStatement(sqlQuery);
            selectStatement.setString(1, policyName);
            selectStatement.setInt(2, tenantId);

            // Should return only single result
            resultSet = selectStatement.executeQuery();
            if (resultSet.next()) {
                policy = new APIPolicy(resultSet.getString(ThrottlePolicyConstants.COLUMN_NAME));
                setCommonPolicyDetails(policy, resultSet);
                policy.setUserLevel(resultSet.getString(ThrottlePolicyConstants.COLUMN_APPLICABLE_LEVEL));
                policy.setPipelines(getPipelines(policy.getPolicyId()));
            }
        } catch (SQLException e) {
            handleException("Failed to get api policy: " + policyName + '-' + tenantId, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(selectStatement, connection, resultSet);
        }
        return policy;
    }

    /**
     * Retrieves {@link APIPolicy} with name <code>uuid</code>
     * <p>This will retrieve complete details about the APIPolicy with all pipelines and conditions.</p>
     *
     * @param uuid uuid of the policy to retrieve from the database
     * @return {@link APIPolicy}
     * @throws APIManagementException
     */
    public APIPolicy getAPIPolicyByUUID(String uuid) throws APIManagementException {
        APIPolicy policy = null;
        Connection connection = null;
        PreparedStatement selectStatement = null;
        ResultSet resultSet = null;

        String sqlQuery = SQLConstants.ThrottleSQLConstants.GET_API_POLICY_BY_UUID_SQL;
        if (forceCaseInsensitiveComparisons) {
            sqlQuery = SQLConstants.ThrottleSQLConstants.GET_API_POLICY_BY_UUID_SQL;
        }

        try {
            connection = APIMgtDBUtil.getConnection();
            selectStatement = connection.prepareStatement(sqlQuery);
            selectStatement.setString(1, uuid);

            // Should return only single result
            resultSet = selectStatement.executeQuery();
            if (resultSet.next()) {
                policy = new APIPolicy(resultSet.getString(ThrottlePolicyConstants.COLUMN_NAME));
                setCommonPolicyDetails(policy, resultSet);
                policy.setUserLevel(resultSet.getString(ThrottlePolicyConstants.COLUMN_APPLICABLE_LEVEL));
                policy.setPipelines(getPipelines(policy.getPolicyId()));
            }
        } catch (SQLException e) {
            handleException("Failed to get api policy: " + uuid, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(selectStatement, connection, resultSet);
        }
        return policy;
    }

    /**
     * Retrieves {@link ApplicationPolicy} with name <code>policyName</code> and tenant Id <code>tenantNId</code>
     *
     * @param policyName name of the policy to retrieve from the database
     * @param tenantId   tenantId of the policy
     * @return {@link ApplicationPolicy}
     * @throws APIManagementException
     */
    public ApplicationPolicy getApplicationPolicy(String policyName, int tenantId) throws APIManagementException {
        ApplicationPolicy policy = null;
        Connection connection = null;
        PreparedStatement selectStatement = null;
        ResultSet resultSet = null;

        String sqlQuery = SQLConstants.GET_APPLICATION_POLICY_SQL;
        if (forceCaseInsensitiveComparisons) {
            sqlQuery = SQLConstants.GET_APPLICATION_POLICY_SQL;
        }

        try {
            connection = APIMgtDBUtil.getConnection();
            selectStatement = connection.prepareStatement(sqlQuery);
            selectStatement.setString(1, policyName);
            selectStatement.setInt(2, tenantId);

            // Should return only single row
            resultSet = selectStatement.executeQuery();
            if (resultSet.next()) {
                policy = new ApplicationPolicy(resultSet.getString(ThrottlePolicyConstants.COLUMN_NAME));
                setCommonPolicyDetails(policy, resultSet);
            }
        } catch (SQLException e) {
            handleException("Failed to get application policy: " + policyName + '-' + tenantId, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(selectStatement, connection, resultSet);
        }
        return policy;
    }

    /**
     * Retrieves {@link ApplicationPolicy} with name <code>uuid</code>
     *
     * @param uuid uuid of the policy to retrieve from the database
     * @return {@link ApplicationPolicy}
     * @throws APIManagementException
     */
    public ApplicationPolicy getApplicationPolicyByUUID(String uuid) throws APIManagementException {
        ApplicationPolicy policy = null;
        Connection connection = null;
        PreparedStatement selectStatement = null;
        ResultSet resultSet = null;

        String sqlQuery = SQLConstants.GET_APPLICATION_POLICY_BY_UUID_SQL;
        if (forceCaseInsensitiveComparisons) {
            sqlQuery = SQLConstants.GET_APPLICATION_POLICY_BY_UUID_SQL;
        }

        try {
            connection = APIMgtDBUtil.getConnection();
            selectStatement = connection.prepareStatement(sqlQuery);
            selectStatement.setString(1, uuid);

            // Should return only single row
            resultSet = selectStatement.executeQuery();
            if (resultSet.next()) {
                policy = new ApplicationPolicy(resultSet.getString(ThrottlePolicyConstants.COLUMN_NAME));
                setCommonPolicyDetails(policy, resultSet);
            }
        } catch (SQLException e) {
            handleException("Failed to get application policy: " + uuid, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(selectStatement, connection, resultSet);
        }
        return policy;
    }

    /**
     * Retrieves {@link SubscriptionPolicy} with name <code>policyName</code> and tenant Id <code>tenantNId</code>
     *
     * @param policyName name of the policy to retrieve from the database
     * @param tenantId   tenantId of the policy
     * @return {@link SubscriptionPolicy}
     * @throws APIManagementException
     */
    public SubscriptionPolicy getSubscriptionPolicy(String policyName, int tenantId) throws APIManagementException {
        SubscriptionPolicy policy = null;
        Connection connection = null;
        PreparedStatement selectStatement = null;
        ResultSet resultSet = null;

        String sqlQuery = SQLConstants.GET_SUBSCRIPTION_POLICY_SQL;
        if (forceCaseInsensitiveComparisons) {
            sqlQuery = SQLConstants.GET_SUBSCRIPTION_POLICY_SQL;
        }

        try {
            connection = APIMgtDBUtil.getConnection();
            selectStatement = connection.prepareStatement(sqlQuery);
            selectStatement.setString(1, policyName);
            selectStatement.setInt(2, tenantId);

            // Should return only single row
            resultSet = selectStatement.executeQuery();
            if (resultSet.next()) {
                policy = new SubscriptionPolicy(resultSet.getString(ThrottlePolicyConstants.COLUMN_NAME));
                setCommonPolicyDetails(policy, resultSet);
                policy.setRateLimitCount(resultSet.getInt(ThrottlePolicyConstants.COLUMN_RATE_LIMIT_COUNT));
                policy.setRateLimitTimeUnit(resultSet.getString(ThrottlePolicyConstants.COLUMN_RATE_LIMIT_TIME_UNIT));
                policy.setStopOnQuotaReach(resultSet.getBoolean(ThrottlePolicyConstants.COLUMN_STOP_ON_QUOTA_REACH));
                policy.setBillingPlan(resultSet.getString(ThrottlePolicyConstants.COLUMN_BILLING_PLAN));
                InputStream binary = resultSet.getBinaryStream(ThrottlePolicyConstants.COLUMN_CUSTOM_ATTRIB);
                if(binary != null){
                    byte[] customAttrib = APIUtil.toByteArray(binary);
                    policy.setCustomAttributes(customAttrib);
                }
            }
        } catch (SQLException e) {
            handleException("Failed to get subscription policy: " + policyName + '-' + tenantId, e);
        } catch (IOException e) {
            handleException("Error while converting input stream to byte array", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(selectStatement, connection, resultSet);
        }
        return policy;
    }

    /**
     * Retrieves {@link SubscriptionPolicy} with name <code>uuid</code>
     *
     * @param uuid name of the policy to retrieve from the database
     * @return {@link SubscriptionPolicy}
     * @throws APIManagementException
     */
    public SubscriptionPolicy getSubscriptionPolicyByUUID(String uuid) throws APIManagementException {
        SubscriptionPolicy policy = null;
        Connection connection = null;
        PreparedStatement selectStatement = null;
        ResultSet resultSet = null;

        String sqlQuery = SQLConstants.GET_SUBSCRIPTION_POLICY_BY_UUID_SQL;
        if (forceCaseInsensitiveComparisons) {
            sqlQuery = SQLConstants.GET_SUBSCRIPTION_POLICY_BY_UUID_SQL;
        }

        try {
            connection = APIMgtDBUtil.getConnection();
            selectStatement = connection.prepareStatement(sqlQuery);
            selectStatement.setString(1, uuid);

            // Should return only single row
            resultSet = selectStatement.executeQuery();
            if (resultSet.next()) {
                policy = new SubscriptionPolicy(resultSet.getString(ThrottlePolicyConstants.COLUMN_NAME));
                setCommonPolicyDetails(policy, resultSet);
                policy.setRateLimitCount(resultSet.getInt(ThrottlePolicyConstants.COLUMN_RATE_LIMIT_COUNT));
                policy.setRateLimitTimeUnit(resultSet.getString(ThrottlePolicyConstants.COLUMN_RATE_LIMIT_TIME_UNIT));
                policy.setStopOnQuotaReach(resultSet.getBoolean(ThrottlePolicyConstants.COLUMN_STOP_ON_QUOTA_REACH));
                policy.setBillingPlan(resultSet.getString(ThrottlePolicyConstants.COLUMN_BILLING_PLAN));
                InputStream binary = resultSet.getBinaryStream(ThrottlePolicyConstants.COLUMN_CUSTOM_ATTRIB);
                if(binary != null){
                    byte[] customAttrib = APIUtil.toByteArray(binary);
                    policy.setCustomAttributes(customAttrib);
                }
            }
        } catch (SQLException e) {
            handleException("Failed to get subscription policy: " + uuid, e);
        } catch (IOException e) {
            handleException("Error while converting input stream to byte array", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(selectStatement, connection, resultSet);
        }
        return policy;
    }

    /**
     * Retrieves list of pipelines for the policy with policy Id: <code>policyId</code>
     *
     * @param policyId policy id of the pipelines
     * @return list of pipelines
     * @throws APIManagementException
     */
    private ArrayList<Pipeline> getPipelines(int policyId) throws APIManagementException {
        Connection connection = null;
        PreparedStatement pipelinesStatement = null;
        ResultSet resultSet = null;
        ArrayList<Pipeline> pipelines = new ArrayList<Pipeline>();

        try {
            connection = APIMgtDBUtil.getConnection();
            pipelinesStatement = connection.prepareStatement(SQLConstants.ThrottleSQLConstants.GET_PIPELINES_SQL);
            int unitTime = 0;
            int quota = 0;
            int pipelineId = -1;
            String timeUnit = null;
            String quotaUnit = null;
            String description;
            pipelinesStatement.setInt(1, policyId);
            resultSet = pipelinesStatement.executeQuery();

            while (resultSet.next()) {
                Pipeline pipeline = new Pipeline();
                ArrayList<Condition> conditions = null;
                QuotaPolicy quotaPolicy = new QuotaPolicy();
                quotaPolicy.setType(resultSet.getString(ThrottlePolicyConstants.COLUMN_QUOTA_POLICY_TYPE));
                timeUnit = resultSet.getString(ThrottlePolicyConstants.COLUMN_TIME_UNIT);
                quotaUnit = resultSet.getString(ThrottlePolicyConstants.COLUMN_QUOTA_UNIT);
                unitTime = resultSet.getInt(ThrottlePolicyConstants.COLUMN_UNIT_TIME);
                quota = resultSet.getInt(ThrottlePolicyConstants.COLUMN_QUOTA);
                pipelineId = resultSet.getInt(ThrottlePolicyConstants.COLUMN_CONDITION_ID);
                description = resultSet.getString(ThrottlePolicyConstants.COLUMN_DESCRIPTION);
                if (PolicyConstants.REQUEST_COUNT_TYPE.equals(quotaPolicy.getType())) {
                    RequestCountLimit requestCountLimit = new RequestCountLimit();
                    requestCountLimit.setUnitTime(unitTime);
                    requestCountLimit.setTimeUnit(timeUnit);
                    requestCountLimit.setRequestCount(quota);
                    quotaPolicy.setLimit(requestCountLimit);
                } else if (PolicyConstants.BANDWIDTH_TYPE.equals(quotaPolicy.getType())) {
                    BandwidthLimit bandwidthLimit = new BandwidthLimit();
                    bandwidthLimit.setUnitTime(unitTime);
                    bandwidthLimit.setTimeUnit(timeUnit);
                    bandwidthLimit.setDataUnit(quotaUnit);
                    bandwidthLimit.setDataAmount(quota);
                    quotaPolicy.setLimit(bandwidthLimit);
                }

                conditions = getConditions(pipelineId);
                pipeline.setConditions(conditions);
                pipeline.setQuotaPolicy(quotaPolicy);
                pipeline.setId(pipelineId);
                pipeline.setDescription(description);
                pipelines.add(pipeline);
            }
        } catch (SQLException e) {
            handleException("Failed to get pipelines for policyId: " + policyId, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(pipelinesStatement, connection, resultSet);
        }
        return pipelines;
    }

    /**
     * Retrieves list of Conditions for a pipeline specified by <code>pipelineId</code>
     *
     * @param pipelineId pipeline Id with conditions to retrieve
     * @return list of Conditions for a pipeline
     * @throws APIManagementException
     */
    private ArrayList<Condition> getConditions(int pipelineId) throws APIManagementException {
        Connection connection = null;
        PreparedStatement conditionsStatement = null;
        ResultSet resultSet = null;
        ArrayList<Condition> conditions = new ArrayList<Condition>();
        String startingIP = null;
        String endingIP = null;
        String specificIP = null;
        boolean invert;
        try {
            connection = APIMgtDBUtil.getConnection();
            conditionsStatement = connection.prepareStatement(SQLConstants.ThrottleSQLConstants.GET_IP_CONDITIONS_SQL);
            conditionsStatement.setInt(1, pipelineId);
            resultSet = conditionsStatement.executeQuery();

            while (resultSet.next()) {
                startingIP = resultSet.getString(ThrottlePolicyConstants.COLUMN_STARTING_IP);
                endingIP = resultSet.getString(ThrottlePolicyConstants.COLUMN_ENDING_IP);
                specificIP = resultSet.getString(ThrottlePolicyConstants.COLUMN_SPECIFIC_IP);
                invert = resultSet.getBoolean(ThrottlePolicyConstants.COLUMN_WITHIN_IP_RANGE);

                if (specificIP != null && !"".equals(specificIP)) {
                    IPCondition ipCondition = new IPCondition(PolicyConstants.IP_SPECIFIC_TYPE);
                    ipCondition.setSpecificIP(specificIP);
                    ipCondition.setInvertCondition(invert);
                    conditions.add(ipCondition);
                } else if (startingIP != null && !"".equals(startingIP)) {

                     /*
                     Assumes availability of starting ip means ip range is enforced.
                     Therefore availability of ending ip is not checked.
                    */
                    IPCondition ipRangeCondition = new IPCondition(PolicyConstants.IP_RANGE_TYPE);
                    ipRangeCondition.setStartingIP(startingIP);
                    ipRangeCondition.setEndingIP(endingIP);
                    ipRangeCondition.setInvertCondition(invert);
                    conditions.add(ipRangeCondition);
                }
            }
            setHeaderConditions(pipelineId, conditions);
            setQueryParameterConditions(pipelineId, conditions);
            setJWTClaimConditions(pipelineId, conditions);
        } catch (SQLException e) {
            handleException("Failed to get conditions for pipelineId: " + pipelineId, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(conditionsStatement, connection, resultSet);
        }
        return conditions;
    }

    /**
     * Add Header conditions of pipeline with pipeline Id: <code>pipelineId</code> to a
     * provided {@link Condition} array
     *
     * @param pipelineId Id of the pipeline
     * @param conditions condition array to populate
     * @throws APIManagementException
     */
    private void setHeaderConditions(int pipelineId, ArrayList<Condition> conditions) throws APIManagementException {
        Connection connection = null;
        PreparedStatement conditionsStatement = null;
        ResultSet resultSet = null;

        try {
            connection = APIMgtDBUtil.getConnection();
            conditionsStatement = connection.prepareStatement(SQLConstants.ThrottleSQLConstants.GET_HEADER_CONDITIONS_SQL);
            conditionsStatement.setInt(1, pipelineId);
            resultSet = conditionsStatement.executeQuery();

            while (resultSet.next()) {
                HeaderCondition headerCondition = new HeaderCondition();
                headerCondition.setHeader(resultSet.getString(ThrottlePolicyConstants.COLUMN_HEADER_FIELD_NAME));
                headerCondition.setValue(resultSet.getString(ThrottlePolicyConstants.COLUMN_HEADER_FIELD_VALUE));
                headerCondition.setInvertCondition(resultSet.getBoolean(ThrottlePolicyConstants.COLUMN_IS_HEADER_FIELD_MAPPING));
                conditions.add(headerCondition);
            }
        } catch (SQLException e) {
            handleException("Failed to get header conditions for pipelineId: " + pipelineId, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(conditionsStatement, connection, resultSet);
        }
    }

    /**
     * Add Query parameter conditions of pipeline with pipeline Id: <code>pipelineId</code> to a
     * provided {@link Condition} array
     *
     * @param pipelineId Id of the pipeline
     * @param conditions condition array to populate
     * @throws APIManagementException
     */
    private void setQueryParameterConditions(int pipelineId, ArrayList<Condition> conditions)
            throws APIManagementException {
        Connection connection = null;
        PreparedStatement conditionsStatement = null;
        ResultSet resultSet = null;

        try {
            connection = APIMgtDBUtil.getConnection();
            conditionsStatement = connection.prepareStatement(SQLConstants.ThrottleSQLConstants.GET_QUERY_PARAMETER_CONDITIONS_SQL);
            conditionsStatement.setInt(1, pipelineId);
            resultSet = conditionsStatement.executeQuery();

            while (resultSet.next()) {
                QueryParameterCondition queryParameterCondition = new QueryParameterCondition();
                queryParameterCondition
                        .setParameter(resultSet.getString(ThrottlePolicyConstants.COLUMN_PARAMETER_NAME));
                queryParameterCondition.setValue(resultSet.getString(ThrottlePolicyConstants.COLUMN_PARAMETER_VALUE));
                queryParameterCondition.setInvertCondition(resultSet.getBoolean(ThrottlePolicyConstants.COLUMN_IS_PARAM_MAPPING));
                conditions.add(queryParameterCondition);
            }
        } catch (SQLException e) {
            handleException("Failed to get query parameter conditions for pipelineId: " + pipelineId, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(conditionsStatement, connection, resultSet);
        }
    }

    /**
     * Add JWT claim conditions of pipeline with pipeline Id: <code>pipelineId</code> to a
     * provided {@link Condition} array
     *
     * @param pipelineId Id of the pipeline
     * @param conditions condition array to populate
     * @throws APIManagementException
     */
    private void setJWTClaimConditions(int pipelineId, ArrayList<Condition> conditions) throws APIManagementException {
        Connection connection = null;
        PreparedStatement conditionsStatement = null;
        ResultSet resultSet = null;

        try {
            connection = APIMgtDBUtil.getConnection();
            conditionsStatement = connection.prepareStatement(SQLConstants.ThrottleSQLConstants.GET_JWT_CLAIM_CONDITIONS_SQL);
            conditionsStatement.setInt(1, pipelineId);
            resultSet = conditionsStatement.executeQuery();

            while (resultSet.next()) {
                JWTClaimsCondition jwtClaimsCondition = new JWTClaimsCondition();
                jwtClaimsCondition.setClaimUrl(resultSet.getString(ThrottlePolicyConstants.COLUMN_CLAIM_URI));
                jwtClaimsCondition.setAttribute(resultSet.getString(ThrottlePolicyConstants.COLUMN_CLAIM_ATTRIBUTE));
                jwtClaimsCondition.setInvertCondition(resultSet.getBoolean(ThrottlePolicyConstants.COLUMN_IS_CLAIM_MAPPING));
                conditions.add(jwtClaimsCondition);
            }
        } catch (SQLException e) {
            handleException("Failed to get jwt claim conditions for pipelineId: " + pipelineId, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(conditionsStatement, connection, resultSet);
        }
    }

    /**
     * Updates API level policy.
     * <p>policy name and tenant id should be specified in <code>policy</code></p>
     * <p>
     * Exsisting policy will be deleted and new policy will be inserted to the database
     * with old POLICY_ID. Uses {@link #addAPIPolicy(APIPolicy) addAPIPolicy}
     * to create new policy.
     * </p>
     *
     * @param policy updated policy object
     * @throws APIManagementException
     */
    public APIPolicy updateAPIPolicy(APIPolicy policy) throws APIManagementException {
        Connection connection = null;
        PreparedStatement selectStatement = null;
        PreparedStatement deleteStatement = null;
        ResultSet resultSet = null;
        int oldPolicyId = 0;
        String oldPolicyUUID = null;

        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            if (policy != null) {
                if (policy.getPolicyName() != null && policy.getTenantId() != -1) {
                    selectStatement = connection
                            .prepareStatement(SQLConstants.ThrottleSQLConstants.GET_API_POLICY_ID_SQL);
                    selectStatement.setString(1, policy.getPolicyName());
                    selectStatement.setInt(2, policy.getTenantId());
                } else if (policy.getUUID() != null) {
                    selectStatement = connection
                            .prepareStatement(SQLConstants.ThrottleSQLConstants.GET_API_POLICY_ID_BY_UUID_SQL);
                    selectStatement.setString(1, policy.getUUID());
                } else {
                    String errorMsg =
                            "Policy object doesn't contain mandatory parameters. At least UUID or Name,Tenant Id"
                                    + " should be provided. Name: " + policy.getPolicyName()
                                    + ", Tenant Id: " + policy.getTenantId() + ", UUID: " + policy.getUUID();
                    log.error(errorMsg);
                    throw new APIManagementException(errorMsg);
                }
            } else {
                String errorMsg = "Provided Policy to add is null";
                log.error(errorMsg);
                throw new APIManagementException(errorMsg);
            }

            // Should return only single row
            resultSet = selectStatement.executeQuery();
            if (resultSet.next()) {
                oldPolicyId = resultSet.getInt(ThrottlePolicyConstants.COLUMN_POLICY_ID);
                oldPolicyUUID = resultSet.getString(ThrottlePolicyConstants.COLUMN_UUID);
            }

            deleteStatement = connection.prepareStatement(SQLConstants.ThrottleSQLConstants.DELETE_API_POLICY_SQL);
            deleteStatement.setInt(1, policy.getTenantId());
            deleteStatement.setString(2, policy.getPolicyName());
            deleteStatement.executeUpdate();
            policy.setPolicyId(oldPolicyId);
            if (!StringUtils.isBlank(oldPolicyUUID)) {
                policy.setUUID(oldPolicyUUID);
            }
            addAPIPolicy(policy, connection);
            connection.commit();
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {

                    // Rollback failed. Exception will be thrown later for upper exception
                    log.error("Failed to rollback the add Api Policy: " + policy.toString(), ex);
                }
            }
            handleException("Failed to update api policy: " + policy.getPolicyName() + '-' + policy.getTenantId(), e);
        } finally {
            APIMgtDBUtil.closeAllConnections(selectStatement, connection, resultSet);
            APIMgtDBUtil.closeAllConnections(deleteStatement, null, null);
        }
        return policy;
    }

    /**
     * Updates Application level policy.
     * <p>policy name and tenant id should be specified in <code>policy</code></p>
     *
     * @param policy updated policy object
     * @throws APIManagementException
     */
    public void updateApplicationPolicy(ApplicationPolicy policy) throws APIManagementException {
        Connection connection = null;
        PreparedStatement updateStatement = null;
        boolean hasCustomAttrib = false;
        String updateQuery;

        if (policy.getTenantId() == -1 || StringUtils.isEmpty(policy.getPolicyName())) {
            String errorMsg = "Policy object doesn't contain mandatory parameters. Name: " + policy.getPolicyName() +
                    ", Tenant Id: " + policy.getTenantId();
            log.error(errorMsg);
            throw new APIManagementException(errorMsg);
        }

        try {
        	if(policy.getCustomAttributes() != null){
       		 hasCustomAttrib = true;
            }
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            if (!StringUtils.isBlank(policy.getPolicyName()) && policy.getTenantId() != -1) {
                updateQuery = SQLConstants.UPDATE_APPLICATION_POLICY_SQL;
                if (hasCustomAttrib) {
                    updateQuery = SQLConstants.UPDATE_APPLICATION_POLICY_WITH_CUSTOM_ATTRIBUTES_SQL;
                }
            } else if (!StringUtils.isBlank(policy.getUUID())) {
                updateQuery = SQLConstants.UPDATE_APPLICATION_POLICY_BY_UUID_SQL;
                if (hasCustomAttrib) {
                    updateQuery = SQLConstants.UPDATE_APPLICATION_POLICY_WITH_CUSTOM_ATTRIBUTES_BY_UUID_SQL;
                }
            } else {
                String errorMsg =
                        "Policy object doesn't contain mandatory parameters. At least UUID or Name,Tenant Id"
                                + " should be provided. Name: " + policy.getPolicyName()
                                + ", Tenant Id: " + policy.getTenantId() + ", UUID: " + policy.getUUID();
                log.error(errorMsg);
                throw new APIManagementException(errorMsg);
            }

            updateStatement = connection.prepareStatement(updateQuery);
            if(!StringUtils.isEmpty(policy.getDisplayName())) {
                updateStatement.setString(1, policy.getDisplayName());
            } else {
                updateStatement.setString(1, policy.getPolicyName());
            }
            updateStatement.setString(2, policy.getDescription());
            updateStatement.setString(3, policy.getDefaultQuotaPolicy().getType());

            if (PolicyConstants.REQUEST_COUNT_TYPE.equalsIgnoreCase(policy.getDefaultQuotaPolicy().getType())) {
                RequestCountLimit limit = (RequestCountLimit) policy.getDefaultQuotaPolicy().getLimit();
                updateStatement.setLong(4, limit.getRequestCount());
                updateStatement.setString(5, null);
            } else if (PolicyConstants.BANDWIDTH_TYPE.equalsIgnoreCase(policy.getDefaultQuotaPolicy().getType())) {
                BandwidthLimit limit = (BandwidthLimit) policy.getDefaultQuotaPolicy().getLimit();
                updateStatement.setLong(4, limit.getDataAmount());
                updateStatement.setString(5, limit.getDataUnit());
            }
            updateStatement.setLong(6, policy.getDefaultQuotaPolicy().getLimit().getUnitTime());
            updateStatement.setString(7, policy.getDefaultQuotaPolicy().getLimit().getTimeUnit());

            if(hasCustomAttrib){
            	updateStatement.setBlob(8, new ByteArrayInputStream(policy.getCustomAttributes()));
                if (!StringUtils.isBlank(policy.getPolicyName()) && policy.getTenantId() != -1) {
                    updateStatement.setString(9, policy.getPolicyName());
                    updateStatement.setInt(10, policy.getTenantId());
                } else if (!StringUtils.isBlank(policy.getUUID())) {
                    updateStatement.setString(9, policy.getUUID());
                }
            } else {
                if (!StringUtils.isBlank(policy.getPolicyName()) && policy.getTenantId() != -1) {
                    updateStatement.setString(8, policy.getPolicyName());
                    updateStatement.setInt(9, policy.getTenantId());
                } else if (!StringUtils.isBlank(policy.getUUID())) {
                    updateStatement.setString(8, policy.getUUID());
                }
            }
            updateStatement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {

                    // Rollback failed. Exception will be thrown later for upper exception
                    log.error("Failed to rollback the update Application Policy: " + policy.toString(), ex);
                }
            }
            handleException(
                    "Failed to update application policy: " + policy.getPolicyName() + '-' + policy.getTenantId(), e);
        } finally {
            APIMgtDBUtil.closeAllConnections(updateStatement, connection, null);
        }
    }

    /**
     * Updates Subscription level policy.
     * <p>policy name and tenant id should be specified in <code>policy</code></p>
     *
     * @param policy updated policy object
     * @throws APIManagementException
     */
    public void updateSubscriptionPolicy(SubscriptionPolicy policy) throws APIManagementException {
        Connection connection = null;
        PreparedStatement updateStatement = null;
        boolean hasCustomAttrib = false;
        String updateQuery;

        try {
        	if(policy.getCustomAttributes() != null){
       		 hasCustomAttrib = true;
            }
            if (!StringUtils.isBlank(policy.getPolicyName()) && policy.getTenantId() != -1) {
                updateQuery = SQLConstants.UPDATE_SUBSCRIPTION_POLICY_SQL;
                if (hasCustomAttrib) {
                    updateQuery = SQLConstants.UPDATE_SUBSCRIPTION_POLICY_WITH_CUSTOM_ATTRIBUTES_SQL;
                }
            } else if (!StringUtils.isBlank(policy.getUUID())) {
                updateQuery = SQLConstants.UPDATE_SUBSCRIPTION_POLICY_BY_UUID_SQL;
                if (hasCustomAttrib) {
                    updateQuery = SQLConstants.UPDATE_SUBSCRIPTION_POLICY_WITH_CUSTOM_ATTRIBUTES_BY_UUID_SQL;
                }
            } else {
                String errorMsg =
                        "Policy object doesn't contain mandatory parameters. At least UUID or Name,Tenant Id"
                                + " should be provided. Name: " + policy.getPolicyName()
                                + ", Tenant Id: " + policy.getTenantId() + ", UUID: " + policy.getUUID();
                log.error(errorMsg);
                throw new APIManagementException(errorMsg);
            }

            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            updateStatement = connection.prepareStatement(updateQuery);
            if(!StringUtils.isEmpty(policy.getDisplayName())) {
                updateStatement.setString(1, policy.getDisplayName());
            } else {
                updateStatement.setString(1, policy.getPolicyName());
            }
            updateStatement.setString(2, policy.getDescription());
            updateStatement.setString(3, policy.getDefaultQuotaPolicy().getType());

            if (PolicyConstants.REQUEST_COUNT_TYPE.equalsIgnoreCase(policy.getDefaultQuotaPolicy().getType())) {
                RequestCountLimit limit = (RequestCountLimit) policy.getDefaultQuotaPolicy().getLimit();
                updateStatement.setLong(4, limit.getRequestCount());
                updateStatement.setString(5, null);
            } else if (PolicyConstants.BANDWIDTH_TYPE.equalsIgnoreCase(policy.getDefaultQuotaPolicy().getType())) {
                BandwidthLimit limit = (BandwidthLimit) policy.getDefaultQuotaPolicy().getLimit();
                updateStatement.setLong(4, limit.getDataAmount());
                updateStatement.setString(5, limit.getDataUnit());
            }

            updateStatement.setLong(6, policy.getDefaultQuotaPolicy().getLimit().getUnitTime());
            updateStatement.setString(7, policy.getDefaultQuotaPolicy().getLimit().getTimeUnit());
            updateStatement.setInt(8, policy.getRateLimitCount());
            updateStatement.setString(9, policy.getRateLimitTimeUnit());
            updateStatement.setBoolean(10, policy.isStopOnQuotaReach());
            updateStatement.setString(11, policy.getBillingPlan());

            if (hasCustomAttrib) {
                long lengthOfStream = policy.getCustomAttributes().length;
                updateStatement.setBinaryStream(12, new ByteArrayInputStream(policy.getCustomAttributes()),
                        lengthOfStream);
                if (!StringUtils.isBlank(policy.getPolicyName()) && policy.getTenantId() != -1) {
                    updateStatement.setString(13, policy.getPolicyName());
                    updateStatement.setInt(14, policy.getTenantId());
                } else if (!StringUtils.isBlank(policy.getUUID())) {
                    updateStatement.setString(13, policy.getUUID());
                }
            } else {
                if (!StringUtils.isBlank(policy.getPolicyName()) && policy.getTenantId() != -1) {
                    updateStatement.setString(12, policy.getPolicyName());
                    updateStatement.setInt(13, policy.getTenantId());
                } else if (!StringUtils.isBlank(policy.getUUID())) {
                    updateStatement.setString(12, policy.getUUID());
                }
            }
            updateStatement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {

                    // Rollback failed. Exception will be thrown later for upper exception
                    log.error("Failed to rollback the update Subscription Policy: " + policy.toString(), ex);
                }
            }
            handleException(
                    "Failed to update subscription policy: " + policy.getPolicyName() + '-' + policy.getTenantId(), e);
        } finally {
            APIMgtDBUtil.closeAllConnections(updateStatement, connection, null);
        }
    }

    /**
     * Updates global throttle policy in database
     *
     * @param policy updated policy obejct
     * @throws APIManagementException
     */
    public void updateGlobalPolicy(GlobalPolicy policy) throws APIManagementException {
        Connection connection = null;
        PreparedStatement updateStatement = null;
        InputStream siddhiQueryInputStream;

        try {
        	byte[] byteArray = policy.getSiddhiQuery().getBytes(Charset.defaultCharset());
        	int lengthOfBytes = byteArray.length;
            siddhiQueryInputStream = new ByteArrayInputStream(byteArray);
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            if (!StringUtils.isBlank(policy.getPolicyName()) && policy.getTenantId() != -1) {
                updateStatement = connection.prepareStatement(SQLConstants.UPDATE_GLOBAL_POLICY_SQL);
            } else if (!StringUtils.isBlank(policy.getUUID())) {
                updateStatement = connection.prepareStatement(SQLConstants.UPDATE_GLOBAL_POLICY_BY_UUID_SQL);
            } else {
                String errorMsg =
                        "Policy object doesn't contain mandatory parameters. At least UUID or Name,Tenant Id"
                                + " should be provided. Name: " + policy.getPolicyName()
                                + ", Tenant Id: " + policy.getTenantId() + ", UUID: " + policy.getUUID();
                log.error(errorMsg);
                throw new APIManagementException(errorMsg);
            }

            updateStatement.setString(1, policy.getDescription());
            updateStatement.setBinaryStream(2, siddhiQueryInputStream, lengthOfBytes);
            updateStatement.setString(3, policy.getKeyTemplate());
            if (!StringUtils.isBlank(policy.getPolicyName()) && policy.getTenantId() != -1) {
                updateStatement.setString(4, policy.getPolicyName());
                updateStatement.setInt(5, policy.getTenantId());
            } else if (!StringUtils.isBlank(policy.getUUID())) {
                updateStatement.setString(4, policy.getUUID());
            }
            updateStatement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {

                    // Rollback failed. Exception will be thrown later for upper exception
                    log.error("Failed to rollback the update Global Policy: " + policy.toString(), ex);
                }
            }
            handleException("Failed to update global policy: " + policy.getPolicyName() + '-' + policy.getTenantId(),
                    e);
        } finally {
            APIMgtDBUtil.closeAllConnections(updateStatement, connection, null);
        }
    }

    /**
     * Retrieves list of available policy names under <code>policyLevel</code>
     * and user <code>username</code>'s tenant
     *
     * @param policyLevel policY level to filter policies
     * @param username    username will be used to get the tenant
     * @return array of policy names
     * @throws APIManagementException
     */
    public String[] getPolicyNames(String policyLevel, String username) throws APIManagementException {

        List<String> names = new ArrayList<String>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sqlQuery = null;

        int tenantID = APIUtil.getTenantId(username);

        try {
            conn = APIMgtDBUtil.getConnection();
            if (PolicyConstants.POLICY_LEVEL_API.equals(policyLevel)) {
                sqlQuery = SQLConstants.ThrottleSQLConstants.GET_API_POLICY_NAMES;
            } else if (PolicyConstants.POLICY_LEVEL_APP.equals(policyLevel)) {
                sqlQuery = SQLConstants.GET_APP_POLICY_NAMES;
            } else if (PolicyConstants.POLICY_LEVEL_SUB.equals(policyLevel)) {
                sqlQuery = SQLConstants.GET_SUB_POLICY_NAMES;
            } else if (PolicyConstants.POLICY_LEVEL_GLOBAL.equals(policyLevel)) {
                sqlQuery = SQLConstants.GET_GLOBAL_POLICY_NAMES;
            }
            ps = conn.prepareStatement(sqlQuery);
            ps.setInt(1, tenantID);
            rs = ps.executeQuery();
            while (rs.next()) {
                names.add(rs.getString(ThrottlePolicyConstants.COLUMN_NAME));
            }

        } catch (SQLException e) {
            handleException("Error while executing SQL", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
        return names.toArray(new String[names.size()]);
    }

    /**
     * Sets deployment status vaule of a policy in database.
     *
     * @param policyLevel policy level
     * @param policyName  name of the policy
     * @param tenantId    tenant id of the policy
     * @param isDeployed  deployment status. <code>true</code> if deployment successful, <code>false</code> if not
     * @throws APIManagementException
     */
    public void setPolicyDeploymentStatus(String policyLevel, String policyName, int tenantId, boolean isDeployed)
            throws APIManagementException {
        Connection connection = null;
        PreparedStatement statusStatement = null;
        String query = null;

        if (PolicyConstants.POLICY_LEVEL_APP.equals(policyLevel)) {
            query = SQLConstants.UPDATE_APPLICATION_POLICY_STATUS_SQL;
        } else if (PolicyConstants.POLICY_LEVEL_SUB.equals(policyLevel)) {
            query = SQLConstants.UPDATE_SUBSCRIPTION_POLICY_STATUS_SQL;
        } else if (PolicyConstants.POLICY_LEVEL_API.equals(policyLevel)) {
            query = SQLConstants.ThrottleSQLConstants.UPDATE_API_POLICY_STATUS_SQL;
        } else if (PolicyConstants.POLICY_LEVEL_GLOBAL.equals(policyLevel)) {
            query = SQLConstants.UPDATE_GLOBAL_POLICY_STATUS_SQL;
        }

        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            statusStatement = connection.prepareStatement(query);
            statusStatement.setBoolean(1, isDeployed);
            statusStatement.setString(2, policyName);
            statusStatement.setInt(3, tenantId);
            statusStatement.executeUpdate();

            connection.commit();
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {

                    // Rollback failed. Exception will be thrown later for upper exception
                    log.error("Failed to rollback setting isDeployed flag: " + policyName + '-' + tenantId, ex);
                }
            }
            handleException("Failed to set deployment status to the policy: " + policyName + '-' + tenantId, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(statusStatement, connection, null);
        }
    }

    /**
     * Populates common attribute data of the <code>policy</code> to <code>policyStatement</code>
     *
     * @param policyStatement prepared statement initialized of policy operation
     * @param policy          <code>Policy</code> object with data
     * @throws SQLException
     */
    private void setCommonParametersForPolicy(PreparedStatement policyStatement, Policy policy) throws SQLException {
        policyStatement.setString(1, policy.getPolicyName());
        if(!StringUtils.isEmpty(policy.getDisplayName())) {
            policyStatement.setString(2, policy.getDisplayName());
        } else {
            policyStatement.setString(2, policy.getPolicyName());
        }
        policyStatement.setInt(3, policy.getTenantId());
        policyStatement.setString(4, policy.getDescription());
        policyStatement.setString(5, policy.getDefaultQuotaPolicy().getType());

        //TODO use requestCount in same format in all places
        if (PolicyConstants.REQUEST_COUNT_TYPE.equalsIgnoreCase(policy.getDefaultQuotaPolicy().getType())) {
            RequestCountLimit limit = (RequestCountLimit) policy.getDefaultQuotaPolicy().getLimit();
            policyStatement.setLong(6, limit.getRequestCount());
            policyStatement.setString(7, null);
        } else if (PolicyConstants.BANDWIDTH_TYPE.equalsIgnoreCase(policy.getDefaultQuotaPolicy().getType())) {
            BandwidthLimit limit = (BandwidthLimit) policy.getDefaultQuotaPolicy().getLimit();
            policyStatement.setLong(6, limit.getDataAmount());
            policyStatement.setString(7, limit.getDataUnit());
        }

        policyStatement.setLong(8, policy.getDefaultQuotaPolicy().getLimit().getUnitTime());
        policyStatement.setString(9, policy.getDefaultQuotaPolicy().getLimit().getTimeUnit());
        //policyStatement.setBoolean(9, APIUtil.isContentAwarePolicy(policy));
        policyStatement.setBoolean(10, policy.isDeployed());
        if (!StringUtils.isBlank(policy.getUUID())) {
            policyStatement.setString(11, policy.getUUID());
        } else {
            policyStatement.setString(11, UUID.randomUUID().toString());
        }
    }

    /**
     * Populated common attributes of policy type objects to <code>policy</code>
     * from <code>resultSet</code>
     *
     * @param policy    initiallized {@link Policy} object to populate
     * @param resultSet {@link ResultSet} with data to populate <code>policy</code>
     * @throws SQLException
     */
    private void setCommonPolicyDetails(Policy policy, ResultSet resultSet) throws SQLException {
        QuotaPolicy quotaPolicy = new QuotaPolicy();
        String prefix = "";

        if (policy instanceof APIPolicy) {
            prefix = "DEFAULT_";
        }

        quotaPolicy.setType(resultSet.getString(prefix + ThrottlePolicyConstants.COLUMN_QUOTA_POLICY_TYPE));
        if (resultSet.getString(prefix + ThrottlePolicyConstants.COLUMN_QUOTA_POLICY_TYPE)
                .equalsIgnoreCase(PolicyConstants.REQUEST_COUNT_TYPE)) {
            RequestCountLimit reqLimit = new RequestCountLimit();
            reqLimit.setUnitTime(resultSet.getInt(prefix + ThrottlePolicyConstants.COLUMN_UNIT_TIME));
            reqLimit.setTimeUnit(resultSet.getString(prefix + ThrottlePolicyConstants.COLUMN_TIME_UNIT));
            reqLimit.setRequestCount(resultSet.getInt(prefix + ThrottlePolicyConstants.COLUMN_QUOTA));
            quotaPolicy.setLimit(reqLimit);
        } else if (resultSet.getString(prefix + ThrottlePolicyConstants.COLUMN_QUOTA_POLICY_TYPE)
                .equalsIgnoreCase(PolicyConstants.BANDWIDTH_TYPE)) {
            BandwidthLimit bandLimit = new BandwidthLimit();
            bandLimit.setUnitTime(resultSet.getInt(prefix + ThrottlePolicyConstants.COLUMN_UNIT_TIME));
            bandLimit.setTimeUnit(resultSet.getString(prefix + ThrottlePolicyConstants.COLUMN_TIME_UNIT));
            bandLimit.setDataAmount(resultSet.getInt(prefix + ThrottlePolicyConstants.COLUMN_QUOTA));
            bandLimit.setDataUnit(resultSet.getString(prefix + ThrottlePolicyConstants.COLUMN_QUOTA_UNIT));
            quotaPolicy.setLimit(bandLimit);
        }

        policy.setUUID(resultSet.getString(ThrottlePolicyConstants.COLUMN_UUID));
        policy.setDescription(resultSet.getString(ThrottlePolicyConstants.COLUMN_DESCRIPTION));
        policy.setDisplayName(resultSet.getString(ThrottlePolicyConstants.COLUMN_DISPLAY_NAME));
        policy.setPolicyId(resultSet.getInt(ThrottlePolicyConstants.COLUMN_POLICY_ID));
        policy.setTenantId(resultSet.getShort(ThrottlePolicyConstants.COLUMN_TENANT_ID));
        policy.setTenantDomain(IdentityTenantUtil.getTenantDomain(policy.getTenantId()));
        policy.setDefaultQuotaPolicy(quotaPolicy);
        policy.setDeployed(resultSet.getBoolean(ThrottlePolicyConstants.COLUMN_DEPLOYED));
    }

    public boolean isPolicyExist(String policyType,int tenantId, String policyName ) throws APIManagementException{
    	Connection connection = null;
        PreparedStatement isExistStatement = null;

    	boolean isExist = false;
    	String policyTable = null;
    	if(PolicyConstants.POLICY_LEVEL_API.equalsIgnoreCase(policyType)){
    		policyTable = PolicyConstants.API_THROTTLE_POLICY_TABLE;
    	}else if(PolicyConstants.POLICY_LEVEL_APP.equalsIgnoreCase(policyType)){
    		policyTable = PolicyConstants.POLICY_APPLICATION_TABLE;
    	}else if(PolicyConstants.POLICY_LEVEL_GLOBAL.equalsIgnoreCase(policyType)){
    		policyTable = PolicyConstants.POLICY_GLOBAL_TABLE;
    	}else if(PolicyConstants.POLICY_LEVEL_SUB.equalsIgnoreCase(policyType)){
    		policyTable = PolicyConstants.POLICY_SUBSCRIPTION_TABLE;
    	}
    	try{
    		String query = "SELECT " +PolicyConstants.POLICY_ID + " FROM "+policyTable + " WHERE TENANT_ID =? AND NAME = ? ";
        	connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(true);
            isExistStatement = connection.prepareStatement(query);
            isExistStatement.setInt(1, tenantId);
            isExistStatement.setString(2, policyName);
            ResultSet result = isExistStatement.executeQuery();
            if(result != null && result.next()){
            	isExist = true;
            }
    	} catch (SQLException e) {
            handleException("Failed to check is exist: " + policyName + '-' + tenantId, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(isExistStatement, connection, null);
        }
    	return isExist;
    }

    public boolean isPolicyDeployed(String policyType,int tenantId, String policyName ) throws APIManagementException{
        Connection connection = null;
        PreparedStatement isExistStatement = null;

        boolean isDeployed = false;
        String policyTable = null;
        if(PolicyConstants.POLICY_LEVEL_API.equalsIgnoreCase(policyType)){
            policyTable = PolicyConstants.API_THROTTLE_POLICY_TABLE;
        }else if(PolicyConstants.POLICY_LEVEL_APP.equalsIgnoreCase(policyType)){
            policyTable = PolicyConstants.POLICY_APPLICATION_TABLE;
        }else if(PolicyConstants.POLICY_LEVEL_GLOBAL.equalsIgnoreCase(policyType)){
            policyTable = PolicyConstants.POLICY_GLOBAL_TABLE;
        }else if(PolicyConstants.POLICY_LEVEL_SUB.equalsIgnoreCase(policyType)){
            policyTable = PolicyConstants.POLICY_SUBSCRIPTION_TABLE;
        }
        try{
            String query = "SELECT " +PolicyConstants.POLICY_IS_DEPLOYED + " FROM "+policyTable + " WHERE TENANT_ID =? AND NAME = ? ";
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(true);
            isExistStatement = connection.prepareStatement(query);
            isExistStatement.setInt(1, tenantId);
            isExistStatement.setString(2, policyName);
            ResultSet result = isExistStatement.executeQuery();
            if(result != null && result.next()){
                isDeployed = result.getBoolean(PolicyConstants.POLICY_IS_DEPLOYED);
            }
        }catch (SQLException e) {
            handleException("Failed to check is exist: " + policyName + '-' + tenantId, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(isExistStatement, connection, null);
        }
        return isDeployed;
    }

    /**
     * Add a block condition
     * 
     * @param conditionType Type of the block condition
     * @param conditionValue value related to the type
     * @param tenantDomain tenant domain the block condition should be effective
     * @return uuid of the block condition if successfully added
     * @throws APIManagementException
     */
    public String addBlockConditions(String conditionType, String conditionValue, String tenantDomain) throws
            APIManagementException {
        Connection connection = null;
        PreparedStatement insertPreparedStatement = null;
        boolean status = false;
        boolean valid = false;
        ResultSet rs = null;
        String uuid = null;
        try {
            String query = SQLConstants.ThrottleSQLConstants.ADD_BLOCK_CONDITIONS_SQL;
            if (APIConstants.BLOCKING_CONDITIONS_API.equals(conditionType)) {
                String extractedTenantDomain = MultitenantUtils.getTenantDomainFromRequestURL(conditionValue);
                if (extractedTenantDomain == null) {
                    extractedTenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
                }
                if (tenantDomain.equals(extractedTenantDomain) && isValidContext(conditionValue)) {
                    valid = true;
                } else {
                    throw new APIManagementException("Couldn't Save Block Condition Due to Invalid API Context " +
                            conditionValue);
                }
            } else if (APIConstants.BLOCKING_CONDITIONS_APPLICATION.equals(conditionType)) {
                String appArray[] = conditionValue.split(":");
                if (appArray.length > 1) {
                    String appOwner = appArray[0];
                    String appName = appArray[1];

                    if ((MultitenantUtils.getTenantDomain(appOwner).equals(tenantDomain)) &&
                                                                                isValidApplication(appOwner, appName)) {
                        valid = true;
                    } else {
                        throw new APIManagementException("Couldn't Save Block Condition Due to Invalid Application " +
                                "name " + appName + "from Application " +
                                "Owner " + appOwner);
                    }
                }
            } else if (APIConstants.BLOCKING_CONDITIONS_USER.equals(conditionType)) {
                if (MultitenantUtils.getTenantDomain(conditionValue).equals(tenantDomain)) {
                    valid = true;
                } else {
                    throw new APIManagementException("Invalid User in Tenant Domain " + tenantDomain);
                }
            } else if (APIConstants.BLOCKING_CONDITIONS_IP.equals(conditionType)) {
                valid = true;
            }
            if (valid) {
                connection = APIMgtDBUtil.getConnection();
                connection.setAutoCommit(false);
                if(!isBlockConditionExist(conditionType, conditionValue, tenantDomain, connection)){
                    uuid = UUID.randomUUID().toString();
                    insertPreparedStatement = connection.prepareStatement(query);
                    insertPreparedStatement.setString(1, conditionType);
                    insertPreparedStatement.setString(2, conditionValue);
                    insertPreparedStatement.setString(3, "TRUE");
                    insertPreparedStatement.setString(4, tenantDomain);
                    insertPreparedStatement.setString(5, uuid);
                    status = insertPreparedStatement.execute();
                    connection.commit();
                    status = true;
                } else {
                    throw new BlockConditionAlreadyExistsException(
                            "Condition with type: " + conditionType + ", value: " + conditionValue + " already exists");
                }
            }
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    handleException(
                            "Failed to rollback adding Block condition : " + conditionType + " and " + conditionValue,
                            ex);
                }
            }
            handleException("Failed to add Block condition : " + conditionType + " and " + conditionValue, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(insertPreparedStatement, connection, null);
        }
        if (status) {
            return uuid;
        } else {
            return null;
        }
    }

    /**
     * Get details of a block condition by Id
     *
     * @param conditionId id of the condition
     * @return Block conditoin represented by the UUID
     * @throws APIManagementException
     */
    public BlockConditionsDTO getBlockCondition(int conditionId) throws APIManagementException {
        Connection connection = null;
        PreparedStatement selectPreparedStatement = null;
        ResultSet resultSet = null;
        BlockConditionsDTO blockCondition = null;
        try {
            String query = SQLConstants.ThrottleSQLConstants.GET_BLOCK_CONDITION_SQL;
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(true);
            selectPreparedStatement = connection.prepareStatement(query);
            selectPreparedStatement.setInt(1, conditionId);
            resultSet = selectPreparedStatement.executeQuery();
            if (resultSet.next()) {
                blockCondition = new BlockConditionsDTO();
                blockCondition.setEnabled(resultSet.getBoolean("ENABLED"));
                blockCondition.setConditionType(resultSet.getString("TYPE"));
                blockCondition.setConditionValue(resultSet.getString("VALUE"));
                blockCondition.setConditionId(conditionId);
                blockCondition.setTenantDomain(resultSet.getString("DOMAIN"));
                blockCondition.setUUID(resultSet.getString("UUID"));
            }
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    handleException("Failed to rollback getting Block condition with id " + conditionId, ex);
                }
            }
            handleException("Failed to get Block condition with id " + conditionId, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(selectPreparedStatement, connection, resultSet);
        }
        return blockCondition;
    }

    /**
     * Get details of a block condition by UUID
     * 
     * @param uuid uuid of the block condition
     * @return Block conditoin represented by the UUID
     * @throws APIManagementException
     */
    public BlockConditionsDTO getBlockConditionByUUID(String uuid) throws APIManagementException {
        Connection connection = null;
        PreparedStatement selectPreparedStatement = null;
        ResultSet resultSet = null;
        BlockConditionsDTO blockCondition = null;
        try {
            String query = SQLConstants.ThrottleSQLConstants.GET_BLOCK_CONDITION_BY_UUID_SQL;
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(true);
            selectPreparedStatement = connection.prepareStatement(query);
            selectPreparedStatement.setString(1, uuid);
            resultSet = selectPreparedStatement.executeQuery();
            if (resultSet.next()) {
                blockCondition = new BlockConditionsDTO();
                blockCondition.setEnabled(resultSet.getBoolean("ENABLED"));
                blockCondition.setConditionType(resultSet.getString("TYPE"));
                blockCondition.setConditionValue(resultSet.getString("VALUE"));
                blockCondition.setConditionId(resultSet.getInt("CONDITION_ID"));
                blockCondition.setTenantDomain(resultSet.getString("DOMAIN"));
                blockCondition.setUUID(resultSet.getString("UUID"));
            }
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    handleException("Failed to rollback getting Block condition by uuid " + uuid, ex);
                }
            }
            handleException("Failed to get Block condition by uuid " + uuid, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(selectPreparedStatement, connection, resultSet);
        }
        return blockCondition;
    }

    public List<BlockConditionsDTO> getBlockConditions(String tenantDomain) throws APIManagementException {
        Connection connection = null;
        PreparedStatement selectPreparedStatement = null;
        ResultSet resultSet = null;
        List<BlockConditionsDTO> blockConditionsDTOList = new ArrayList<BlockConditionsDTO>();
        try {
            String query = SQLConstants.ThrottleSQLConstants.GET_BLOCK_CONDITIONS_SQL;
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(true);
            selectPreparedStatement = connection.prepareStatement(query);
            selectPreparedStatement.setString(1, tenantDomain);
            resultSet = selectPreparedStatement.executeQuery();
            while (resultSet.next()) {
                BlockConditionsDTO blockConditionsDTO = new BlockConditionsDTO();
                blockConditionsDTO.setEnabled(resultSet.getBoolean("ENABLED"));
                blockConditionsDTO.setConditionType(resultSet.getString("TYPE"));
                blockConditionsDTO.setConditionValue(resultSet.getString("VALUE"));
                blockConditionsDTO.setConditionId(resultSet.getInt("CONDITION_ID"));
                blockConditionsDTO.setUUID(resultSet.getString("UUID"));
                blockConditionsDTO.setTenantDomain(resultSet.getString("DOMAIN"));
                blockConditionsDTOList.add(blockConditionsDTO);
            }
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    handleException("Failed to rollback getting Block conditions ", ex);
                }
            }
            handleException("Failed to get Block conditions", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(selectPreparedStatement, connection, resultSet);
        }
        return blockConditionsDTOList;
    }

    /**
     * Update the block condition state true (Enabled) /false (Disabled) given the UUID
     * 
     * @param conditionId id of the block condition
     * @param state blocking state
     * @return true if the operation was success
     * @throws APIManagementException
     */
    public boolean updateBlockConditionState(int conditionId,String state) throws APIManagementException {
        Connection connection = null;
        PreparedStatement updateBlockConditionPreparedStatement = null;
        boolean status = false;
        try {
            String query = SQLConstants.ThrottleSQLConstants.UPDATE_BLOCK_CONDITION_STATE_SQL;
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            updateBlockConditionPreparedStatement = connection.prepareStatement(query);
            updateBlockConditionPreparedStatement.setString(1,state.toUpperCase());
            updateBlockConditionPreparedStatement.setInt(2, conditionId);
            updateBlockConditionPreparedStatement.executeUpdate();
            connection.commit();
            status = true;
        } catch (SQLException e) {
            if (connection != null) {
                try {
                     connection.rollback();
                } catch (SQLException ex) {
                    handleException("Failed to rollback updating Block condition with condition id " + conditionId, ex);
                }
            }
            handleException("Failed to update Block condition with condition id " + conditionId, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(updateBlockConditionPreparedStatement, connection, null);
        }
        return status;
    }

    /**
     * Update the block condition state true (Enabled) /false (Disabled) given the UUID
     * 
     * @param uuid UUID of the block condition
     * @param state blocking state
     * @return true if the operation was success
     * @throws APIManagementException
     */
    public boolean updateBlockConditionStateByUUID(String uuid, String state) throws APIManagementException {
        Connection connection = null;
        PreparedStatement updateBlockConditionPreparedStatement = null;
        boolean status = false;
        try {
            String query = SQLConstants.ThrottleSQLConstants.UPDATE_BLOCK_CONDITION_STATE_BY_UUID_SQL;
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            updateBlockConditionPreparedStatement = connection.prepareStatement(query);
            updateBlockConditionPreparedStatement.setString(1, state.toUpperCase());
            updateBlockConditionPreparedStatement.setString(2, uuid);
            updateBlockConditionPreparedStatement.executeUpdate();
            connection.commit();
            status = true;
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    handleException("Failed to rollback updating Block condition with condition UUID " + uuid, ex);
                }
            }
            handleException("Failed to update Block condition with condition UUID " + uuid, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(updateBlockConditionPreparedStatement, connection, null);
        }
        return status;
    }

    /**
     * Delete the block condition given the id
     * 
     * @param conditionId id of the condition
     * @return true if successfully deleted
     * @throws APIManagementException
     */
    public boolean deleteBlockCondition(int conditionId) throws APIManagementException {
        Connection connection = null;
        PreparedStatement deleteBlockConditionPreparedStatement = null;
        boolean status = false;
        try {
            String query = SQLConstants.ThrottleSQLConstants.DELETE_BLOCK_CONDITION_SQL;
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            deleteBlockConditionPreparedStatement = connection.prepareStatement(query);
            deleteBlockConditionPreparedStatement.setInt(1, conditionId);
            status = deleteBlockConditionPreparedStatement.execute();
            connection.commit();
            status = true;
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    handleException("Failed to rollback deleting Block condition with condition id " + conditionId, ex);
                }
            }
            handleException("Failed to delete Block condition with condition id " + conditionId, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(deleteBlockConditionPreparedStatement, connection, null);
        }
        return status;
    }

    /**
     * Delete the block condition given the id
     *
     * @param uuid UUID of the block condition
     * @return true if successfully deleted
     * @throws APIManagementException
     */
    public boolean deleteBlockConditionByUUID(String uuid) throws APIManagementException {
        Connection connection = null;
        PreparedStatement deleteBlockConditionPreparedStatement = null;
        boolean status = false;
        try {
            String query = SQLConstants.ThrottleSQLConstants.DELETE_BLOCK_CONDITION_BY_UUID_SQL;
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            deleteBlockConditionPreparedStatement = connection.prepareStatement(query);
            deleteBlockConditionPreparedStatement.setString(1, uuid);
            status = deleteBlockConditionPreparedStatement.execute();
            connection.commit();
            status = true;
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    handleException("Failed to rollback deleting Block condition with condition UUID " + uuid, ex);
                }
            }
            handleException("Failed to delete Block condition with condition UUID " + uuid, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(deleteBlockConditionPreparedStatement, connection, null);
        }
        return status;
    }

    private boolean isValidContext(String context) throws APIManagementException {
        Connection connection = null;
        PreparedStatement validateContextPreparedStatement = null;
        ResultSet resultSet = null;
        boolean status = false;
        try {
            String query = "select count(*) COUNT from AM_API where CONTEXT=?";
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            validateContextPreparedStatement = connection.prepareStatement(query);
            validateContextPreparedStatement.setString(1, context);
            resultSet = validateContextPreparedStatement.executeQuery();
            connection.commit();
            if (resultSet.next() && resultSet.getInt("COUNT") > 0) {
                status = true;
            }
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    handleException("Failed to rollback checking Block condition with context " + context, ex);
                }
            }
            handleException("Failed to check Block condition with context " + context, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(validateContextPreparedStatement, connection, resultSet);
        }
        return status;
    }
    private boolean isValidApplication(String appOwner, String appName) throws APIManagementException {
        Connection connection = null;
        PreparedStatement validateContextPreparedStatement = null;
        ResultSet resultSet = null;
        boolean status = false;
        try {
            String query = "SELECT * FROM AM_APPLICATION App,AM_SUBSCRIBER SUB  WHERE App.NAME=? AND App" +
                    ".SUBSCRIBER_ID=SUB.SUBSCRIBER_ID AND SUB.USER_ID=?";
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            validateContextPreparedStatement = connection.prepareStatement(query);
            validateContextPreparedStatement.setString(1,appName);
            validateContextPreparedStatement.setString(2, appOwner);
            resultSet = validateContextPreparedStatement.executeQuery();
            connection.commit();
            if (resultSet.next()){
                status = true;
            }
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    handleException(
                            "Failed to rollback checking Block condition with Application Name " + appName + " with "
                                    + "Application Owner" + appOwner, ex);
                }
            }
            handleException("Failed to check Block condition with Application Name " + appName + " with " +
                    "Application Owner" + appOwner, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(validateContextPreparedStatement, connection, resultSet);
        }
        return status;
    }

    public String getAPILevelTier(int id) throws APIManagementException{
    	 Connection connection = null;
         PreparedStatement selectPreparedStatement = null;
         ResultSet resultSet = null;
         String apiLevelTier = null;
         try {
             String query = SQLConstants.GET_API_DETAILS_SQL;
             connection = APIMgtDBUtil.getConnection();
             connection.setAutoCommit(true);
             selectPreparedStatement = connection.prepareStatement(query + " WHERE API_ID = ?");
             selectPreparedStatement.setInt(1, id);
             resultSet = selectPreparedStatement.executeQuery();
             while (resultSet.next()) {
            	 apiLevelTier = resultSet.getString("API_TIER");
             }
         } catch (SQLException e) {
             if (connection != null) {
                 try {
                     connection.rollback();
                 } catch (SQLException ex) {
                     handleException("Failed to rollback getting API Details", ex);
                 }
             }
             handleException("Failed to get API Details", e);
         } finally {
             APIMgtDBUtil.closeAllConnections(selectPreparedStatement, connection, resultSet);
         }
         return apiLevelTier;
    }

    private boolean isBlockConditionExist(String conditionType, String conditionValue, String tenantDomain, Connection
            connection) throws APIManagementException {
        PreparedStatement checkIsExistPreparedStatement = null;
        ResultSet checkIsResultSet = null;
        boolean status = false;
        try {
            String isExistQuery = SQLConstants.ThrottleSQLConstants.BLOCK_CONDITION_EXIST_SQL;
            checkIsExistPreparedStatement = connection.prepareStatement(isExistQuery);
            checkIsExistPreparedStatement.setString(1, tenantDomain);
            checkIsExistPreparedStatement.setString(2, conditionType);
            checkIsExistPreparedStatement.setString(3, conditionValue);
            checkIsResultSet = checkIsExistPreparedStatement.executeQuery();
            connection.commit();
            if (checkIsResultSet.next()) {
                status = true;
            }
        } catch (SQLException e) {
            String msg = "Couldn't check the Block Condition Exist";
            log.error(msg, e);
            handleException(msg, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(checkIsExistPreparedStatement, null, checkIsResultSet);
        }
        return status;
    }
    
    public boolean hasSubscription(String tierId, String tenantDomainWithAt, String policyLevel) throws APIManagementException{
    	 PreparedStatement checkIsExistPreparedStatement = null;
    	 Connection connection = null;
         ResultSet checkIsResultSet = null;
         boolean status = false;
         try {
        	 /*String apiProvider = tenantId;*/
        	 connection = APIMgtDBUtil.getConnection();
        	 connection.setAutoCommit(true);
        	 String isExistQuery = SQLConstants.ThrottleSQLConstants.TIER_HAS_SUBSCRIPTION;
        	 if (PolicyConstants.POLICY_LEVEL_API.equals(policyLevel)) {
        		 isExistQuery = SQLConstants.ThrottleSQLConstants.TIER_ATTACHED_TO_RESOURCES_API;
             } else if (PolicyConstants.POLICY_LEVEL_APP.equals(policyLevel)) {
            	 isExistQuery = SQLConstants.ThrottleSQLConstants.TIER_ATTACHED_TO_APPLICATION;
             } else if (PolicyConstants.POLICY_LEVEL_SUB.equals(policyLevel)) {
            	 isExistQuery = SQLConstants.ThrottleSQLConstants.TIER_HAS_SUBSCRIPTION;
             } 
        	 
             checkIsExistPreparedStatement = connection.prepareStatement(isExistQuery);
             checkIsExistPreparedStatement.setString(1, tierId);
             checkIsExistPreparedStatement.setString(2, "%"+tenantDomainWithAt);
             if (PolicyConstants.POLICY_LEVEL_API.equals(policyLevel)) {
            	 checkIsExistPreparedStatement.setString(3, tierId);
                 checkIsExistPreparedStatement.setString(4, "%"+tenantDomainWithAt);
             }
             checkIsResultSet = checkIsExistPreparedStatement.executeQuery();
             if (checkIsResultSet != null && checkIsResultSet.next()) {
            	 int count = checkIsResultSet.getInt(1);
            	 if(count > 0){
            		 status = true;
            	 }
                 
             }
             
             connection.setAutoCommit(true);
         } catch (SQLException e) {
             String msg = "Couldn't check Subscription Exist";
             log.error(msg, e);
             handleException(msg, e);
         } finally {
             APIMgtDBUtil.closeAllConnections(checkIsExistPreparedStatement, null, checkIsResultSet);
         }
         return status;
    	
    }


    /**
     * Get a list of access tokens issued for given user under the given app. Returned object carries consumer key
     * and secret information related to the access token
     *
     * @param userName end user name
     * @param appName application name
     * @return list of tokens
     * @throws SQLException in case of a DB issue
     */
    public static List<AccessTokenInfo> getAccessTokenListForUser(String userName, String appName) throws SQLException {

        List<AccessTokenInfo> accessTokens = new ArrayList<AccessTokenInfo>(5);
        Connection connection = APIMgtDBUtil.getConnection();
        PreparedStatement consumerSecretIDPS = connection.prepareStatement(SQLConstants.GET_ACCESS_TOKENS_BY_USER_SQL);
        consumerSecretIDPS.setString(1, userName);
        consumerSecretIDPS.setString(2, appName);

        ResultSet consumerSecretIDResult = consumerSecretIDPS.executeQuery();

        while (consumerSecretIDResult.next()) {
            String consumerKey = consumerSecretIDResult.getString(1);
            String consumerSecret = consumerSecretIDResult.getString(2);
            String accessToken = consumerSecretIDResult.getString(3);

            AccessTokenInfo accessTokenInfo = new AccessTokenInfo();
            accessTokenInfo.setConsumerKey(consumerKey);
            accessTokenInfo.setConsumerSecret(consumerSecret);
            accessTokenInfo.setAccessToken(accessToken);

            accessTokens.add(accessTokenInfo);
        }

        return accessTokens;
    }
}

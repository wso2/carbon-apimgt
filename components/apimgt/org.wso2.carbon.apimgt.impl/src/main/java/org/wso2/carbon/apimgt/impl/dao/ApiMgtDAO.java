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


import org.apache.axis2.util.JavaUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.SubscriptionAlreadyExistingException;
import org.wso2.carbon.apimgt.api.dto.UserApplicationAPIUsage;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIKey;
import org.wso2.carbon.apimgt.api.model.APIStatus;
import org.wso2.carbon.apimgt.api.model.APIStore;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.ApplicationConstants;
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
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.APIInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.APIKeyInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.ApplicationRegistrationWorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.TierPermissionDTO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.token.JWTGenerator;
import org.wso2.carbon.apimgt.impl.token.TokenGenerator;
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
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.cache.Cache;
import javax.cache.Caching;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
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

/**
 * This class represent the ApiMgtDAO.
 */
public class ApiMgtDAO {

    private static final Log log = LogFactory.getLog(ApiMgtDAO.class);

    public static TokenGenerator tokenGenerator;
    public static Boolean removeUserNameInJWTForAppToken;


    private static final String ENABLE_JWT_CACHE = "APIKeyManager.EnableJWTCache";
    private static boolean forceCaseInsensitiveComparisons = false;


    public ApiMgtDAO() {
        APIManagerConfiguration configuration = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfigurationService().getAPIManagerConfiguration();

		if (configuration == null) {
            log.error("API Manager configuration is not initialized");
		        } else {
            String enableJWTGeneration = configuration.getFirstProperty(APIConstants.ENABLE_JWT_GENERATION);
            removeUserNameInJWTForAppToken = Boolean.parseBoolean(configuration.getFirstProperty(
                    APIConstants.API_KEY_VALIDATOR_REMOVE_USERNAME_TO_JWT_FOR_APP_TOKEN));
            if (enableJWTGeneration != null && JavaUtils.isTrueExplicitly(enableJWTGeneration)) {
                String clazz = configuration.getFirstProperty(APIConstants.TOKEN_GENERATOR_IMPL);
                if (clazz == null) {
                    tokenGenerator = new JWTGenerator();
                } else {
                    try {
                        tokenGenerator = (TokenGenerator) APIUtil.getClassForName(clazz).newInstance();
                    } catch (InstantiationException e) {
                        log.error("Error while instantiating class " + clazz, e);
                    } catch (IllegalAccessException e) {
                        log.error(e);
                    } catch (ClassNotFoundException e) {
                        log.error("Cannot find the class " + clazz + e);
                    }
                }

            }
        }

        String caseSensitiveComparison = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration().getFirstProperty(APIConstants.API_STORE_FORCE_CI_COMPARISIONS);
        if (caseSensitiveComparison != null) {
            forceCaseInsensitiveComparisons = Boolean.parseBoolean(caseSensitiveComparison);
        }
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
    public String getAccessKeyForAPI(String userId, String applicationName, APIInfoDTO identifier,
                                     String keyType)
            throws APIManagementException {

        String accessKey = null;

        String loginUserName = getLoginUserName(userId);

        //get the tenant id for the corresponding domain
        String tenantAwareUserId = MultitenantUtils.getTenantAwareUsername(loginUserName);
        int tenantId = APIUtil.getTenantId(loginUserName);

        if (log.isDebugEnabled()) {
            log.debug("Searching for: " + identifier.getAPIIdentifier() + ", User: " + tenantAwareUserId +
                      ", ApplicationName: " + applicationName + ", Tenant ID: " + tenantId);
        }

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sqlQuery =
                "SELECT " +
                "   SKM.ACCESS_TOKEN AS ACCESS_TOKEN " +
                "FROM " +
                "   AM_SUBSCRIPTION SP," +
                "   AM_API API," +
                "   AM_SUBSCRIBER SB," +
                "   AM_APPLICATION APP, " +
                "   AM_SUBSCRIPTION_KEY_MAPPING SKM " +
                "WHERE " +
                "   SB.USER_ID=? " +
                "   AND SB.TENANT_ID=? " +
                "   AND API.API_PROVIDER=? " +
                "   AND API.API_NAME=?" +
                "   AND API.API_VERSION=?" +
                "   AND APP.NAME=? " +
                "   AND SKM.KEY_TYPE=? " +
                "   AND API.API_ID = SP.API_ID" +
                "   AND SB.SUBSCRIBER_ID = APP.SUBSCRIBER_ID " +
                "   AND APP.APPLICATION_ID = SP.APPLICATION_ID " +
                "   AND SP.SUBSCRIPTION_ID = SKM.SUBSCRIPTION_ID ";

        if (forceCaseInsensitiveComparisons) {
            sqlQuery =
                    "SELECT " +
                            "   SKM.ACCESS_TOKEN AS ACCESS_TOKEN " +
                            "FROM " +
                            "   AM_SUBSCRIPTION SP," +
                            "   AM_API API," +
                            "   AM_SUBSCRIBER SB," +
                            "   AM_APPLICATION APP, " +
                            "   AM_SUBSCRIPTION_KEY_MAPPING SKM " +
                            "WHERE " +
                            "   LOWER(SB.USER_ID)=LOWER(?) " +
                            "   AND SB.TENANT_ID=? " +
                            "   AND API.API_PROVIDER=? " +
                            "   AND API.API_NAME=?" +
                            "   AND API.API_VERSION=?" +
                            "   AND APP.NAME=? " +
                            "   AND SKM.KEY_TYPE=? " +
                            "   AND API.API_ID = SP.API_ID" +
                            "   AND SB.SUBSCRIBER_ID = APP.SUBSCRIBER_ID " +
                            "   AND APP.APPLICATION_ID = SP.APPLICATION_ID " +
                            "   AND SP.SUBSCRIPTION_ID = SKM.SUBSCRIPTION_ID ";
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
            handleException("Error when executing the SQL query to read the access key for user : "
                            + loginUserName + "of tenant(id) : " + tenantId, e);
        } catch (CryptoException e) {
            handleException("Error when decrypting access key for user : "
                            + loginUserName + "of tenant(id) : " + tenantId, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
        return accessKey;
    }

    /**
     * Persist the details of the token generation request (allowed domains & validity period) to be used back
     * when approval has been granted.
     *
     * @param dto  DTO related to Application Registration.
     * @param onlyKeyMappingEntry When this flag is enabled, only AM_APPLICATION_KEY_MAPPING will get affected.
     * @throws APIManagementException if failed to create entries in  AM_APPLICATION_REGISTRATION and
     * AM_APPLICATION_KEY_MAPPING tables.
     */

    public void createApplicationRegistrationEntry(ApplicationRegistrationWorkflowDTO dto, boolean onlyKeyMappingEntry)
            throws APIManagementException {

        Connection conn = null;
        PreparedStatement ps = null;
        Application application = dto.getApplication();
        Subscriber subscriber = application.getSubscriber();
        String jsonString = dto.getAppInfoDTO().getOAuthApplicationInfo().getJsonString();


        String registrationEntry = "INSERT INTO " +
                " AM_APPLICATION_REGISTRATION (SUBSCRIBER_ID,WF_REF,APP_ID,TOKEN_TYPE,ALLOWED_DOMAINS,VALIDITY_PERIOD,TOKEN_SCOPE,INPUTS) " +
                "  VALUES(?,?,?,?,?,?,?,?)";

        String keyMappingEntry = "INSERT INTO " +
                "AM_APPLICATION_KEY_MAPPING (APPLICATION_ID,KEY_TYPE,STATE) " +
                "VALUES(?,?,?)";

        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);

            if (!onlyKeyMappingEntry) {
                ps = conn.prepareStatement(registrationEntry);
                ps.setInt(1, subscriber.getId());
                ps.setString(2, dto.getWorkflowReference());
                ps.setInt(3, application.getId());
                ps.setString(4, dto.getKeyType());
                ps.setString(5, dto.getDomainList());
                ps.setLong(6, dto.getValidityTime());
		        ps.setString(7,(String) dto.getAppInfoDTO().getOAuthApplicationInfo().getParameter("tokenScope"));
	            ps.setString(8,jsonString);
                ps.execute();
                ps.close();
            }

            ps = conn.prepareStatement(keyMappingEntry);
            ps.setInt(1, application.getId());
            ps.setString(2, dto.getKeyType());
            ps.setString(3, dto.getStatus().toString());
            ps.execute();

            conn.commit();
        } catch (SQLException e) {
            try {
                if (conn != null)   {
                    conn.rollback();
                }
            } catch (SQLException e1) {
                handleException("Error occurred while Roling back changes done on Application Registration", e1);
            }
            handleException("Error occurred while creating an " +
                    "Application Registration Entry for Application : " + application.getName(), e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, null);
        }

    }

    public OAuthApplicationInfo getOAuthApplication(String consumerKey) throws APIManagementException {
        OAuthApplicationInfo oAuthApplicationInfo = new OAuthApplicationInfo();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sqlQuery =
                "SELECT CONSUMER_SECRET, USERNAME, TENANT_ID, APP_NAME, APP_NAME, CALLBACK_URL, GRANT_TYPES " +
                        "FROM IDN_OAUTH_CONSUMER_APPS " +
                        "WHERE CONSUMER_KEY = ?";

        try {
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            ps.setString(1, consumerKey);
            rs = ps.executeQuery();
            while (rs.next()) {
                oAuthApplicationInfo.setClientId(consumerKey);
                oAuthApplicationInfo.setCallBackURL(rs.getString("CALLBACK_URL"));
                oAuthApplicationInfo.setClientSecret(APIUtil.decryptToken(rs.getString("CONSUMER_SECRET")));
                oAuthApplicationInfo.addParameter(ApplicationConstants.OAUTH_REDIRECT_URIS, rs.getString("CALLBACK_URL"));
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
     * @param consumerKey Client ID of the OAuth App
     * @return {@code Subscriber} with name and TenantId set.
     * @throws APIManagementException
     */
    public static Subscriber getOwnerForConsumerApp(String consumerKey) throws APIManagementException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String username;
        Subscriber subscriber = null;

        String sqlQuery =
                "SELECT USERNAME, USER_DOMAIN, TENANT_ID FROM " +
                " IDN_OAUTH_CONSUMER_APPS " +
                " WHERE " +
                " CONSUMER_KEY = ?";

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
            handleException("Error while executing SQL for getting User Id : SQL "+sqlQuery, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }

        return subscriber;

    }


    public static void deleteOAuthApplication(String consumerKey) throws APIManagementException {
        Connection conn = null;
        PreparedStatement ps = null;
        String sqlQuery = "DELETE FROM IDN_OAUTH_CONSUMER_APPS WHERE CONSUMER_KEY = ?";

        try {
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            ps.setString(1, consumerKey);
            ps.executeUpdate();

        } catch (SQLException e) {
            handleException("Error while executing SQL for deleting OAuth application", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, null);
        }
    }

    /**
     * Get Subscribed APIs for given userId
     *
     * @param userId id of the user
     * @return APIInfoDTO[]
     * @throws APIManagementException if failed to get Subscribed APIs
     */
    public APIInfoDTO[] getSubscribedAPIsOfUser(String userId) throws APIManagementException {

        //identify logged in user
        String loginUserName = getLoginUserName(userId);

        String tenantAwareUsername = MultitenantUtils.getTenantAwareUsername(loginUserName);
        int tenantId = APIUtil.getTenantId(loginUserName);
        List<APIInfoDTO> apiInfoDTOList = new ArrayList<APIInfoDTO>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sqlQuery = "SELECT " +
                          "   API.API_PROVIDER AS API_PROVIDER," +
                          "   API.API_NAME AS API_NAME," +
                          "   API.API_VERSION AS API_VERSION " +
                          "FROM " +
                          "   AM_SUBSCRIPTION SP, " +
                          "   AM_API API," +
                          "   AM_SUBSCRIBER SB, " +
                          "   AM_APPLICATION APP " +
                          "WHERE " +
                          "   SB.USER_ID = ? " +
                          "   AND SB.TENANT_ID = ? " +
                          "   AND SB.SUBSCRIBER_ID = APP.SUBSCRIBER_ID " +
                          "   AND APP.APPLICATION_ID=SP.APPLICATION_ID " +
                          "   AND API.API_ID = SP.API_ID" +
                          "   AND SP.SUBS_CREATE_STATE = '" + APIConstants.SubscriptionCreatedStatus.SUBSCRIBE + "'";

        if (forceCaseInsensitiveComparisons) {
            sqlQuery = "SELECT " +
                    "   API.API_PROVIDER AS API_PROVIDER," +
                    "   API.API_NAME AS API_NAME," +
                    "   API.API_VERSION AS API_VERSION " +
                    "FROM " +
                    "   AM_SUBSCRIPTION SP, " +
                    "   AM_API API," +
                    "   AM_SUBSCRIBER SB, " +
                    "   AM_APPLICATION APP " +
                    "WHERE " +
                    "   LOWER(SB.USER_ID) = LOWER(?) " +
                    "   AND SB.TENANT_ID = ? " +
                    "   AND SB.SUBSCRIBER_ID = APP.SUBSCRIBER_ID " +
                    "   AND APP.APPLICATION_ID=SP.APPLICATION_ID " +
                    "   AND API.API_ID = SP.API_ID" +
                    "   AND SP.SUBS_CREATE_STATE = '" + APIConstants.SubscriptionCreatedStatus.SUBSCRIBE + "'";
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
        // api_id store as "providerName_apiName_apiVersion" in AM_SUBSCRIPTION table
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sqlQuery = "SELECT " +
                          "   SB.USER_ID, " +
                          "   SB.TENANT_ID " +
                          "FROM " +
                          "   AM_SUBSCRIBER SB, " +
                          "   AM_APPLICATION APP, " +
                          "   AM_SUBSCRIPTION SP, " +
                          "   AM_API API " +
                          "WHERE " +
                          "   API.API_PROVIDER = ? " +
                          "   AND API.API_NAME = ?" +
                          "   AND API.API_VERSION = ?" +
                          "   AND SP.APPLICATION_ID = APP.APPLICATION_ID " +
                          "   AND APP.SUBSCRIBER_ID=SB.SUBSCRIBER_ID " +
                          "   AND API.API_ID = SP.API_ID" +
                          "   AND SP.SUBS_CREATE_STATE = '" + APIConstants.SubscriptionCreatedStatus.SUBSCRIBE + "'";
        try {
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            ps.setString(1, APIUtil.replaceEmailDomainBack(apiInfoDTO.getProviderId()));
            ps.setString(2, apiInfoDTO.getApiName());
            ps.setString(3, apiInfoDTO.getVersion());
            rs = ps.executeQuery();
            List<APIKeyInfoDTO> apiKeyInfoList = new ArrayList<APIKeyInfoDTO>();
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
    public void changeAccessTokenStatus(String userId, APIInfoDTO apiInfoDTO,
                                        String statusEnum)
            throws APIManagementException {
        String tenantAwareUsername = MultitenantUtils.getTenantAwareUsername(userId);
        int tenantId = APIUtil.getTenantId(userId);

        String accessTokenStoreTable = APIConstants.ACCESS_TOKEN_STORE_TABLE;
        if (APIUtil.checkAccessTokenPartitioningEnabled() &&
            APIUtil.checkUserNameAssertionEnabled()) {
            accessTokenStoreTable = APIUtil.getAccessTokenStoreTableFromUserId(userId);
        }

        Connection conn = null;
        PreparedStatement ps = null;
        String sqlQuery = "UPDATE " +
                          accessTokenStoreTable + " IAT , AM_SUBSCRIBER SB," +
                          " AM_SUBSCRIPTION SP , AM_APPLICATION APP, AM_API API" +
                          " SET IAT.TOKEN_STATE=?" +
                          " WHERE SB.USER_ID=?" +
                          " AND SB.TENANT_ID=?" +
                          " AND API.API_PROVIDER=?" +
                          " AND API.API_NAME=?" +
                          " AND API.API_VERSION=?" +
                          " AND SP.ACCESS_TOKEN=IAT.ACCESS_TOKEN" +
                          " AND SB.SUBSCRIBER_ID=APP.SUBSCRIBER_ID" +
                          " AND APP.APPLICATION_ID = SP.APPLICATION_ID" +
                          " AND API.API_ID = SP.API_ID";

        if (forceCaseInsensitiveComparisons) {
            sqlQuery = "UPDATE " +
                    accessTokenStoreTable + " IAT , AM_SUBSCRIBER SB," +
                    " AM_SUBSCRIPTION SP , AM_APPLICATION APP, AM_API API" +
                    " SET IAT.TOKEN_STATE=?" +
                    " WHERE LOWER(SB.USER_ID)=LOWER(?)" +
                    " AND SB.TENANT_ID=?" +
                    " AND API.API_PROVIDER=?" +
                    " AND API.API_NAME=?" +
                    " AND API.API_VERSION=?" +
                    " AND SP.ACCESS_TOKEN=IAT.ACCESS_TOKEN" +
                    " AND SB.SUBSCRIBER_ID=APP.SUBSCRIBER_ID" +
                    " AND APP.APPLICATION_ID = SP.APPLICATION_ID" +
                    " AND API.API_ID = SP.API_ID";
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
                log.error("Failed to rollback the changeAccessTokenStatus operation", e);
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
     *         authorized.
     * @throws APIManagementException Error when accessing the database or registry.
     */
    public APIKeyValidationInfoDTO validateKey(String context, String version, String accessToken, String requiredAuthenticationLevel)
            throws APIManagementException {

        if (log.isDebugEnabled()) {
            log.debug("A request is received to process the token : " + accessToken + " to access" +
                      " the context URL : " + context);
        }
        APIKeyValidationInfoDTO keyValidationInfoDTO = new APIKeyValidationInfoDTO();
        keyValidationInfoDTO.setAuthorized(false);

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

        //Check if the api version has been prefixed with _default_
        if(version != null && version.startsWith(APIConstants.DEFAULT_VERSION_PREFIX)){
            defaultVersionInvoked = true;
            //Remove the prefix from the version.
            version = version.split(APIConstants.DEFAULT_VERSION_PREFIX)[1];
        }

        String accessTokenStoreTable = APIConstants.ACCESS_TOKEN_STORE_TABLE;
        String tokenScopeAssociationTable= APIConstants.TOKEN_SCOPE_ASSOCIATION_TABLE;
        if (APIUtil.checkAccessTokenPartitioningEnabled() &&
            APIUtil.checkUserNameAssertionEnabled()) {
            accessTokenStoreTable = APIUtil.getAccessTokenStoreTableFromAccessToken(accessToken);
        }

        // First check whether the token is valid, active and not expired.
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        String applicationSqlQuery = "SELECT " +
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
                                    " FROM " + accessTokenStoreTable + " IAT,"
                                    + tokenScopeAssociationTable + " ISAT," +
                                    "   AM_SUBSCRIPTION SUB," +
                                    "   AM_SUBSCRIBER SUBS," +
                                    "   AM_APPLICATION APP," +
                                    "   AM_APPLICATION_KEY_MAPPING AKM," +
                                    "   AM_API API," +
                                    "   IDN_OAUTH_CONSUMER_APPS ICA" +
                                    " WHERE " +
                                    "   IAT.ACCESS_TOKEN = ? " +
                                    "   AND API.CONTEXT = ? " +
                                    "   AND IAT.TOKEN_ID = ISAT.TOKEN_ID " +
                                    (defaultVersionInvoked ? "" : " AND API.API_VERSION = ? ") +
                                    "   ICA.CONSUMER_KEY = AKM.CONSUMER_KEY AND" +
                                    "   ICA.ID = IAT.CONSUMER_KEY_ID" +
                                    "   AND SUB.APPLICATION_ID = APP.APPLICATION_ID" +
                                    "   AND APP.SUBSCRIBER_ID = SUBS.SUBSCRIBER_ID" +
                                    "   AND API.API_ID = SUB.API_ID" +
                                    "   AND AKM.APPLICATION_ID=APP.APPLICATION_ID";

        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);

            ps = conn.prepareStatement(applicationSqlQuery);
            String encryptedAccessToken = APIUtil.encryptToken(accessToken);
            ps.setString(1, encryptedAccessToken);
            ps.setString(2, context);

            //We only do the version check for non-default version API invocations
            if(!defaultVersionInvoked){
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
                issuedTime = rs.getTimestamp(APIConstants.IDENTITY_OAUTH2_FIELD_TIME_CREATED,
                                             Calendar.getInstance(TimeZone.getTimeZone("UTC"))).getTime();
                validityPeriod = rs.getLong(APIConstants.IDENTITY_OAUTH2_FIELD_VALIDITY_PERIOD);
                timestampSkew = OAuthServerConfiguration.getInstance().getTimeStampSkewInSeconds() * 1000;
                currentTime = System.currentTimeMillis();
                subscriptionStatus=rs.getString(APIConstants.SUBSCRIPTION_FIELD_SUB_STATUS);
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

                keyValidationInfoDTO.setAuthorizedDomains(ApiMgtDAO.getAuthorizedDomainList(accessToken));
                keyValidationInfoDTO.setConsumerKey(consumerKey);
                Set<String> scopes = new HashSet<String>();

                do {
                    String scope = rs.getString(APIConstants.IDENTITY_OAUTH2_FIELD_TOKEN_SCOPE);
                    if(scope != null && !scope.isEmpty()){
                        scopes.add(scope);
                    }
                }while(rs.next());

                keyValidationInfoDTO.setScopes(scopes);
                
                /* If Subscription Status is PROD_ONLY_BLOCKED, block production access only */
                if (subscriptionStatus.equals(APIConstants.SubscriptionStatus.BLOCKED)) {
                    keyValidationInfoDTO.setValidationStatus(APIConstants.KeyValidationStatus.API_BLOCKED);
                    keyValidationInfoDTO.setAuthorized(false);
                    return keyValidationInfoDTO;
                }
                else if(APIConstants.SubscriptionStatus.ON_HOLD.equals(subscriptionStatus) ||
                        APIConstants.SubscriptionStatus.REJECTED.equals(subscriptionStatus)){
                    keyValidationInfoDTO.setValidationStatus(APIConstants.KeyValidationStatus.SUBSCRIPTION_INACTIVE);
                    keyValidationInfoDTO.setAuthorized(false);
                    return keyValidationInfoDTO;
                }
                else if (subscriptionStatus.equals(APIConstants.SubscriptionStatus.PROD_ONLY_BLOCKED) &&
                           !APIConstants.API_KEY_TYPE_SANDBOX.equals(type)) {
                    keyValidationInfoDTO.setValidationStatus(APIConstants.KeyValidationStatus.API_BLOCKED);
                    keyValidationInfoDTO.setAuthorized(false);
                    return keyValidationInfoDTO;
                }

                //check if 'requiredAuthenticationLevel' & the one associated with access token matches
                //This check should only be done for 'Application' and 'Application_User' levels
                if(requiredAuthenticationLevel.equals(APIConstants.AUTH_APPLICATION_LEVEL_TOKEN)
                   || requiredAuthenticationLevel.equals(APIConstants.AUTH_APPLICATION_USER_LEVEL_TOKEN)){
                    if(log.isDebugEnabled()){
                        log.debug("Access token's userType : "+userType + ".Required type : "+requiredAuthenticationLevel);
                    }

                    if (!(userType.equalsIgnoreCase(requiredAuthenticationLevel))){
                        keyValidationInfoDTO.setValidationStatus(
                                APIConstants.KeyValidationStatus.API_AUTH_INCORRECT_ACCESS_TOKEN_TYPE);
                        keyValidationInfoDTO.setAuthorized(false);
                        return keyValidationInfoDTO;
                    }
                }

                // Check whether the token is ACTIVE
                if (APIConstants.TokenStatus.ACTIVE.equals(status)) {
                    if (log.isDebugEnabled()) {
                        log.debug("Checking Access token: " + accessToken + " for validity." +
                                  "((currentTime - timestampSkew) > (issuedTime + validityPeriod)) : " +
                                  "((" + currentTime + "-" + timestampSkew + ")" + " > (" + issuedTime + " + " + validityPeriod + "))");
                    }
                    if (validityPeriod!=Long.MAX_VALUE && (currentTime - timestampSkew) > (issuedTime + validityPeriod)) {
                        keyValidationInfoDTO.setValidationStatus(
                                APIConstants.KeyValidationStatus.API_AUTH_INVALID_CREDENTIALS);
                        if (log.isDebugEnabled()) {
                            log.debug("Access token: " + accessToken + " has expired. " +
                                      "Reason ((currentTime - timestampSkew) > (issuedTime + validityPeriod)) : " +
                                      "((" + currentTime + "-" + timestampSkew + ")" + " > (" + issuedTime + " + " + validityPeriod + "))");
                        }
                        //update token status as expired
                        updateTokenState(accessToken, conn, ps);

                        conn.commit();
                    } else {
                        keyValidationInfoDTO.setAuthorized(true);

                        if (tokenGenerator != null) {
                            String jwtToken = generateJWTToken(keyValidationInfoDTO, context, version,accessToken);
                            keyValidationInfoDTO.setEndUserToken(jwtToken);
                        }
                    }
				} else {
					keyValidationInfoDTO.setValidationStatus(APIConstants.KeyValidationStatus.API_AUTH_INVALID_CREDENTIALS);
					if (log.isDebugEnabled()) {
						log.debug("Access token: " + accessToken + " is inactive");
					}
                }
            } else {
                //no record found. Invalid access token received
                keyValidationInfoDTO.setValidationStatus(
                        APIConstants.KeyValidationStatus.API_AUTH_INVALID_CREDENTIALS);
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

    public Map<String,Object> getSubscriptionDetails(String context,String version, String consumerKey) throws APIManagementException {

        String sql = "SELECT "+
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
        Map<String,Object> results = null;
        try {
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, context);
            ps.setString(2, version);
            ps.setString(3, consumerKey);
            rs = ps.executeQuery();
            if(rs.next()){
             results = new HashMap<String,Object>();

                results.put("tier_id",rs.getString("TIER_ID"));
                results.put("user_id",rs.getString("USER_ID"));
                results.put("subs_status",rs.getString("SUB_STATUS"));
                results.put("app_id",rs.getString("APPLICATION_ID"));
                results.put("key_type",rs.getString("KEY_TYPE"));
                results.put("api_name",rs.getString("API_NAME"));
                results.put("api_provider",rs.getString("API_PROVIDER"));
                results.put("app_name",rs.getString("NAME"));
                results.put("app_tier",rs.getString("APPLICATION_TIER"));
            }

        } catch (SQLException e) {
            handleException("Error occurred while reading subscription details from the database.", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps,conn,rs);
        }
       return results;
    }


    public boolean validateSubscriptionDetails(String context, String version, String consumerKey,
                                               APIKeyValidationInfoDTO infoDTO) throws APIManagementException {


        boolean defaultVersionInvoked = false;

        //Check if the api version has been prefixed with _default_
        if (version != null && version.startsWith(APIConstants.DEFAULT_VERSION_PREFIX)) {
            defaultVersionInvoked = true;
            //Remove the prefix from the version.
            version = version.split(APIConstants.DEFAULT_VERSION_PREFIX)[1];
        }

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
                     " AND AKM.CONSUMER_KEY = ? " +
                     (defaultVersionInvoked ? "" : " AND API.API_VERSION = ? ") +
                     "   AND SUB.APPLICATION_ID = APP.APPLICATION_ID" +
                     "   AND APP.SUBSCRIBER_ID = SUBS.SUBSCRIBER_ID" +
                     "   AND API.API_ID = SUB.API_ID" +
                     "   AND AKM.APPLICATION_ID=APP.APPLICATION_ID" +
                     "   AND AKM.APPLICATION_ID=APP.APPLICATION_ID";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, context);
            ps.setString(2, consumerKey);
            if (!defaultVersionInvoked) {
                ps.setString(3, version);
            }
            rs = ps.executeQuery();
            if (rs.next()) {
                String subscriptionStatus = rs.getString("SUB_STATUS");
                String type = rs.getString("KEY_TYPE");
                if (subscriptionStatus.equals(APIConstants.SubscriptionStatus.BLOCKED)) {
                    infoDTO.setValidationStatus(APIConstants.KeyValidationStatus.API_BLOCKED);
                    infoDTO.setAuthorized(false);
                    return false;
                } else if (APIConstants.SubscriptionStatus.ON_HOLD.equals(subscriptionStatus) ||
                           APIConstants.SubscriptionStatus.REJECTED.equals(subscriptionStatus)) {
                    infoDTO.setValidationStatus(APIConstants.KeyValidationStatus.SUBSCRIPTION_INACTIVE);
                    infoDTO.setAuthorized(false);
                    return false;
                } else if (subscriptionStatus.equals(APIConstants.SubscriptionStatus.PROD_ONLY_BLOCKED) &&
                           !APIConstants.API_KEY_TYPE_SANDBOX.equals(type)) {
                    infoDTO.setValidationStatus(APIConstants.KeyValidationStatus.API_BLOCKED);
                    infoDTO.setType(type);
                    infoDTO.setAuthorized(false);
                    return false;
                }

                infoDTO.setTier(rs.getString("TIER_ID"));
                infoDTO.setSubscriber(rs.getString("USER_ID"));
                infoDTO.setApplicationId(rs.getString("APPLICATION_ID"));
                infoDTO.setApiName(rs.getString("API_NAME"));
                infoDTO.setApiPublisher(rs.getString("API_PROVIDER"));
                infoDTO.setApplicationName(rs.getString("NAME"));
                infoDTO.setApplicationTier(rs.getString("APPLICATION_TIER"));
                infoDTO.setType(type);
                return true;
            }
            infoDTO.setAuthorized(false);
            infoDTO.setValidationStatus(APIConstants.KeyValidationStatus.API_AUTH_RESOURCE_FORBIDDEN);

        } catch (SQLException e) {
            handleException("Exception occurred while validating Subscription.", e);
        }
        finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
        return false;
    }

    private String generateJWTToken(APIKeyValidationInfoDTO keyValidationInfoDTO,
                                    String context, String version) throws APIManagementException {

        return tokenGenerator.generateToken(keyValidationInfoDTO,context,version);
    }


    private String generateJWTToken(APIKeyValidationInfoDTO keyValidationInfoDTO,
                                    String context, String version, String accessToken) throws APIManagementException {

        return tokenGenerator.generateToken(keyValidationInfoDTO, context, version, accessToken);
    }


    //This returns the authorized client domains into a List
    public static List<String> getAuthorizedDomainList(String apiKey) throws APIManagementException {
        return Arrays.asList(getAuthorizedDomains(apiKey).split(","));
    }

    private void updateTokenState(String accessToken, Connection conn, PreparedStatement ps)
            throws SQLException, APIManagementException, CryptoException{

        String accessTokenStoreTable = APIConstants.ACCESS_TOKEN_STORE_TABLE;
        if (APIUtil.checkAccessTokenPartitioningEnabled() &&
            APIUtil.checkUserNameAssertionEnabled()) {
            accessTokenStoreTable = APIUtil.getAccessTokenStoreTableFromAccessToken(accessToken);
        }
        String encryptedAccessToken = APIUtil.encryptToken(accessToken);
        String UPDATE_TOKE_STATE_SQL =
                "UPDATE " +
                accessTokenStoreTable +
                " SET " +
                "   TOKEN_STATE = ? " +
                "   ,TOKEN_STATE_ID = ? " +
                "WHERE " +
                "   ACCESS_TOKEN = ?";
        ps = conn.prepareStatement(UPDATE_TOKE_STATE_SQL);
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

            String query = "INSERT" +
                           " INTO AM_SUBSCRIBER (USER_ID, TENANT_ID, EMAIL_ADDRESS, DATE_SUBSCRIBED, " +
                           "CREATED_BY, CREATED_TIME)" +
                           " VALUES (?,?,?,?,?,?)";

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
                subscriberId = Integer.valueOf(rs.getString(1));
            }
            subscriber.setId(subscriberId);


            conn.commit();

            //Add default application.
            //It will not be shared within in the group 
            Application defaultApp = new Application(APIConstants.DEFAULT_APPLICATION_NAME, subscriber);
            defaultApp.setTier(APIConstants.UNLIMITED_TIER);
            defaultApp.setGroupId("");
            addApplication(defaultApp, subscriber.getName(), conn);

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error("Error while rolling back the failed operation", e);
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

            String query = "UPDATE" +
                           " AM_SUBSCRIBER SET USER_ID=?, TENANT_ID=?, EMAIL_ADDRESS=?, DATE_SUBSCRIBED=?, " +
                           "UPDATED_BY=?, UPDATED_TIME=? WHERE SUBSCRIBER_ID=?";
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
                    log.error("Error while rolling back the failed operation", e);
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
            String query =
                    "SELECT" +
                    " USER_ID, TENANT_ID, EMAIL_ADDRESS, DATE_SUBSCRIBED " +
                    "FROM " +
                    "AM_SUBSCRIBER" +
                    " WHERE " +
                    "SUBSCRIBER_ID=?";
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

    public int addSubscription(APIIdentifier identifier, String context, int applicationId, String status)
            throws APIManagementException {

        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        PreparedStatement preparedStforInsert = null;
        ResultSet rs = null;
        int subscriptionId = -1;
        int apiId;

        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);
            apiId = getAPIID(identifier, conn);

            //Query to check if this subscription already exists
            String checkDuplicateQuery = "SELECT " +
                                         "SUB_STATUS, SUBS_CREATE_STATE FROM AM_SUBSCRIPTION" +
                                         " WHERE " +
                                         "API_ID = ? " +
                                         "AND APPLICATION_ID = ? " +
                                         "AND TIER_ID = ?";
            ps = conn.prepareStatement(checkDuplicateQuery);
            ps.setInt(1, apiId);
            ps.setInt(2, applicationId);
            ps.setString(3, identifier.getTier());

            resultSet = ps.executeQuery();

            //If the subscription already exists
            if (resultSet.next()) {

                String subStatus = resultSet.getString("SUB_STATUS");
                String subCreationStatus = resultSet.getString("SUBS_CREATE_STATE");

                String applicationName = getApplicationNameFromId(applicationId);

                if(APIConstants.SubscriptionStatus.UNBLOCKED.equals(subStatus)
                        && APIConstants.SubscriptionCreatedStatus.SUBSCRIBE.equals(subCreationStatus))  {

                	//Throw error saying subscription already exists.
                    log.error("Subscription already exists for API " + identifier.getApiName() + " in Application "
                            + applicationName);
                    throw new SubscriptionAlreadyExistingException("Subscription already exists for API "
                            + identifier.getApiName() + " in Application " + applicationName);
                } else if (APIConstants.SubscriptionStatus.UNBLOCKED.equals(subStatus)
                        && APIConstants.SubscriptionCreatedStatus.UN_SUBSCRIBE.equals(subCreationStatus))    {
                    deleteSubscriptionByApiIDAndAppID(apiId, applicationId, conn);
                } else if (APIConstants.SubscriptionStatus.BLOCKED.equals(subStatus)
                        || APIConstants.SubscriptionStatus.PROD_ONLY_BLOCKED.equals(subStatus))  {
                    log.error("Subscription to API " + identifier.getApiName() + " through application "
                              + applicationName + " was blocked");
                    throw new APIManagementException("Subscription to API " + identifier.getApiName()
                                                     + " through application " + applicationName + " was blocked");
                }


            }

            //This query to update the AM_SUBSCRIPTION table
            String sqlQuery = "INSERT " +
                              "INTO AM_SUBSCRIPTION (TIER_ID,API_ID,APPLICATION_ID,SUB_STATUS, SUBS_CREATE_STATE, " +
                              "CREATED_BY, CREATED_TIME, UUID) VALUES (?,?,?,?,?,?,?,?)";

            //Adding data to the AM_SUBSCRIPTION table
            //ps = conn.prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS);
            preparedStforInsert = conn.prepareStatement(sqlQuery, new String[]{"SUBSCRIPTION_ID"});
            if (conn.getMetaData().getDriverName().contains("PostgreSQL")) {
                preparedStforInsert = conn.prepareStatement(sqlQuery, new String[]{"subscription_id"});
            }

            preparedStforInsert.setString(1, identifier.getTier());
            preparedStforInsert.setInt(2, apiId);
            preparedStforInsert.setInt(3, applicationId);
            preparedStforInsert.setString(4, status != null ? status : APIConstants.SubscriptionStatus.UNBLOCKED);
            preparedStforInsert.setString(5, APIConstants.SubscriptionCreatedStatus.SUBSCRIBE);
            //TODO Need to find logged in user who perform this subscription
            preparedStforInsert.setString(6, null);
            preparedStforInsert.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
            preparedStforInsert.setString(8, UUID.randomUUID().toString());

            preparedStforInsert.executeUpdate();
            rs = preparedStforInsert.getGeneratedKeys();
            while (rs.next()) {
                //subscriptionId = rs.getInt(1);
                subscriptionId = Integer.valueOf(rs.getString(1));
            }

            // finally commit transaction
            conn.commit();

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error("Failed to rollback the add subscription ", e);
                }
            }
            handleException("Failed to add subscriber data ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, resultSet);
            APIMgtDBUtil.closeAllConnections(preparedStforInsert, null, rs);
        }
        return subscriptionId;
    }

    /**
     * Removes the subscription entry from AM_SUBSCRIPTIONS for identifier. Providing and managing
     * the conn object is a responsibility of the third party that uses this method.
     *
     * @param identifier APIIdentifier
     * @param applicationId ID of the application which has the subscription
     * @param conn Connection object to use for database operations.
     * @throws APIManagementException
     */
    public void removeSubscription(APIIdentifier identifier, int applicationId, Connection conn)
            throws APIManagementException {
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        int apiId = -1;
        String uuid;
        try {
            apiId = getAPIID(identifier, conn);

            String subscriptionUUIDQuery = "SELECT " +
                    " UUID FROM AM_SUBSCRIPTION" +
                    " WHERE " +
                    "API_ID = ? " +
                    "AND APPLICATION_ID = ?";

            ps = conn.prepareStatement(subscriptionUUIDQuery);
            ps.setInt(1, apiId);
            ps.setInt(2, applicationId);
            resultSet = ps.executeQuery();

            if (resultSet.next())   {
                uuid = resultSet.getString("UUID");
                SubscribedAPI subscribedAPI = new SubscribedAPI(uuid);
                removeSubscription(subscribedAPI, conn);
            } else {
                throw new APIManagementException(
                        "UUID does not exist for the given apiId:" + apiId + " and application id:" + applicationId);
            }

        } catch (SQLException e) {
            handleException("Failed to add subscriber data ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, null, resultSet);
        }
    }

    /** Removes a subscription specified by SubscribedAPI object
     *
     * @param subscription SubscribedAPI object
     * @throws APIManagementException
     */
    public void removeSubscription(SubscribedAPI subscription) throws APIManagementException {
        Connection conn = null;
        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);
            removeSubscription(subscription, conn);
            conn.commit();
        } catch (SQLException e) {
            handleException("Failed to add subscriber data ", e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                log.error("Couldn't close database connection for removing subscription", e);
            }
        }
    }

    /** Removes a subscription specified by SubscribedAPI object
     * 
     * @param subscription SubscribedAPI object
     * @param conn database connection object
     * @throws APIManagementException
     */
    public void removeSubscription(SubscribedAPI subscription, Connection conn) throws APIManagementException {
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        PreparedStatement preparedStForUpdateOrDelete = null;
        String subStatus = null;

        try {
            String subscriptionStatusQuery = "SELECT " +
                    "SUB_STATUS FROM AM_SUBSCRIPTION" +
                    " WHERE" +
                    " UUID = ?";

            ps = conn.prepareStatement(subscriptionStatusQuery);
            ps.setString(1, subscription.getUUID());
            resultSet = ps.executeQuery();

            if (resultSet.next())   {
                subStatus = resultSet.getString("SUB_STATUS");
            }

            // If the user was unblocked, remove the entry from DB, else change the status and keep the entry.
            String updateQuery = "UPDATE AM_SUBSCRIPTION " +
                    " SET " +
                    "SUBS_CREATE_STATE = '" + APIConstants.SubscriptionCreatedStatus.UN_SUBSCRIBE + "'" +
                    " WHERE" +
                    " UUID = ?";

            String deleteQuery = "DELETE FROM AM_SUBSCRIPTION" + 
                    " WHERE" + 
                    " UUID = ?";

            if(APIConstants.SubscriptionStatus.BLOCKED.equals(subStatus)
                    || APIConstants.SubscriptionStatus.PROD_ONLY_BLOCKED.equals(subStatus)) {
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


    /** Removes a subscription by id by force without considering the subscription blocking state of the user
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

            String sqlQuery = "DELETE FROM AM_SUBSCRIPTION WHERE SUBSCRIPTION_ID = ?";

            ps = conn.prepareStatement(sqlQuery);
            ps.setInt(1, subscription_id);
            ps.executeUpdate();

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error("Failed to rollback remove subscription ", e);
                }
            }
            handleException("Failed to remove subscription data ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, null);
        }
    }

    public void removeAllSubscriptions(APIIdentifier apiIdentifier) throws APIManagementException   {
        Connection conn = null;
        PreparedStatement ps = null;
        int apiId;

        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);
            apiId = getAPIID(apiIdentifier, conn);

            String sqlQuery = "DELETE FROM AM_SUBSCRIPTION WHERE API_ID = ?";

            ps = conn.prepareStatement(sqlQuery);
            ps.setInt(1, apiId);
            ps.executeUpdate();

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error("Failed to rollback remove all subscription ", e);
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
                String getApiQuery = "SELECT SUB_STATUS FROM AM_SUBSCRIPTION WHERE SUBSCRIPTION_ID = ?";
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

    /** returns the SubscribedAPI object which is related to the subscriptionId
     * 
     * @param subscriptionId subscription id
     * @return
     * @throws APIManagementException 
     */
    public SubscribedAPI getSubscriptionById(int subscriptionId) throws APIManagementException {

        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;

        try {
            conn = APIMgtDBUtil.getConnection();
            String getSubscriptionQuery = 
                    "SELECT " +
                    "SUBS.SUBSCRIPTION_ID AS SUBSCRIPTION_ID, " +
                    "API.API_PROVIDER AS API_PROVIDER, " +
                    "API.API_NAME AS API_NAME, " +
                    "API.API_VERSION AS API_VERSION, " +
                    "SUBS.APPLICATION_ID AS APPLICATION_ID, " +
                    "SUBS.TIER_ID AS TIER_ID, " +
                    "SUBS.SUB_STATUS AS SUB_STATUS, " +
                    "SUBS.SUBS_CREATE_STATE AS SUBS_CREATE_STATE, " +
                    "SUBS.LAST_ACCESSED AS LAST_ACCESSED, " +
                    "SUBS.UUID AS UUID " +
                    "FROM " +
                    "AM_SUBSCRIPTION SUBS," +
                    "AM_API API " +
                    "WHERE " +
                    "API.API_ID = SUBS.API_ID AND " +
                    "SUBSCRIPTION_ID = ?";
            ps = conn.prepareStatement(getSubscriptionQuery);
            ps.setInt(1, subscriptionId);
            resultSet = ps.executeQuery();
            SubscribedAPI subscribedAPI = null;
            if (resultSet.next()) {
                APIIdentifier apiIdentifier = new APIIdentifier(APIUtil.replaceEmailDomain(resultSet.getString("API_PROVIDER")),
                        resultSet.getString("API_NAME"), resultSet.getString("API_VERSION"));
                
                int applicationId = resultSet.getInt("APPLICATION_ID");
                Application application = getApplicationById(applicationId);               
                subscribedAPI = new SubscribedAPI(application.getSubscriber(), apiIdentifier);
                subscribedAPI.setSubscriptionId(resultSet.getInt("SUBSCRIPTION_ID"));
                subscribedAPI.setSubStatus(resultSet.getString("SUB_STATUS"));
                subscribedAPI.setSubCreatedStatus(resultSet.getString("SUBS_CREATE_STATE"));
                subscribedAPI.setTier(new Tier(resultSet.getString("TIER_ID")));
                subscribedAPI.setLastAccessed(resultSet.getDate("LAST_ACCESSED"));
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

    /** returns the SubscribedAPI object which is related to the UUID
     *
     * @param uuid UUID of Application
     * @return
     * @throws APIManagementException
     */
    public SubscribedAPI getSubscriptionByUUID(String uuid) throws APIManagementException {

        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;

        try {
            conn = APIMgtDBUtil.getConnection();
            String getSubscriptionQuery =
                    "SELECT " +
                            "SUBS.SUBSCRIPTION_ID AS SUBSCRIPTION_ID, " +
                            "API.API_PROVIDER AS API_PROVIDER, " +
                            "API.API_NAME AS API_NAME, " +
                            "API.API_VERSION AS API_VERSION, " +
                            "SUBS.APPLICATION_ID AS APPLICATION_ID, " +
                            "SUBS.TIER_ID AS TIER_ID, " +
                            "SUBS.SUB_STATUS AS SUB_STATUS, " +
                            "SUBS.SUBS_CREATE_STATE AS SUBS_CREATE_STATE, " +
                            "SUBS.LAST_ACCESSED AS LAST_ACCESSED, " +
                            "SUBS.UUID AS UUID " +
                            "FROM " +
                            "AM_SUBSCRIPTION SUBS," +
                            "AM_API API " +
                            "WHERE " +
                            "API.API_ID = SUBS.API_ID AND " +
                            "UUID = ?";
            ps = conn.prepareStatement(getSubscriptionQuery);
            ps.setString(1, uuid);
            resultSet = ps.executeQuery();
            SubscribedAPI subscribedAPI = null;
            if (resultSet.next()) {
                APIIdentifier apiIdentifier = new APIIdentifier(APIUtil.replaceEmailDomain(resultSet.getString("API_PROVIDER")),
                        resultSet.getString("API_NAME"), resultSet.getString("API_VERSION"));

                int applicationId = resultSet.getInt("APPLICATION_ID");
                Application application = getApplicationById(applicationId);
                subscribedAPI = new SubscribedAPI(application.getSubscriber(), apiIdentifier);
                subscribedAPI.setUUID(resultSet.getString("UUID"));
                subscribedAPI.setSubscriptionId(resultSet.getInt("SUBSCRIPTION_ID"));
                subscribedAPI.setSubStatus(resultSet.getString("SUB_STATUS"));
                subscribedAPI.setSubCreatedStatus(resultSet.getString("SUBS_CREATE_STATE"));
                subscribedAPI.setTier(new Tier(resultSet.getString("TIER_ID")));
                subscribedAPI.setLastAccessed(resultSet.getDate("LAST_ACCESSED"));
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
    public static Subscriber getSubscriber(String subscriberName) throws APIManagementException {

        Connection conn = null;
        Subscriber subscriber = null;
        PreparedStatement ps = null;
        ResultSet result = null;

        int tenantId;
        tenantId = APIUtil.getTenantId(subscriberName);

        String sqlQuery = "SELECT " +
                          "   SUBSCRIBER_ID, " +
                          "   USER_ID, " +
                          "   TENANT_ID, " +
                          "   EMAIL_ADDRESS, " +
                          "   DATE_SUBSCRIBED " +
                          "FROM " +
                          "   AM_SUBSCRIBER " +
                          "WHERE " +
                          "   USER_ID = ? " +
                          "   AND TENANT_ID = ?";

        if (forceCaseInsensitiveComparisons) {

            sqlQuery = "SELECT " +
                    "   SUBSCRIBER_ID, " +
                    "   USER_ID, " +
                    "   TENANT_ID, " +
                    "   EMAIL_ADDRESS, " +
                    "   DATE_SUBSCRIBED " +
                    "FROM " +
                    "   AM_SUBSCRIBER " +
                    "WHERE " +
                    "   LOWER(USER_ID) = LOWER(?) " +
                    "   AND TENANT_ID = ?";
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

        String getAPISql = "SELECT" +
                           " API.API_PROVIDER," +
                           " API.API_NAME," +
                           " API.API_VERSION " +
                           "FROM" +
                           " AM_SUBSCRIPTION SUB," +
                           " AM_SUBSCRIPTION_KEY_MAPPING SKM, " +
                           " AM_API API " +
                           "WHERE" +
                           " SKM.ACCESS_TOKEN=?" +
                           " AND SKM.SUBSCRIPTION_ID=SUB.SUBSCRIPTION_ID" +
                           " AND API.API_ID = SUB.API_ID";

        Set<APIIdentifier> apiList = new HashSet<APIIdentifier>();
        try {
            connection = APIMgtDBUtil.getConnection();
            ps = connection.prepareStatement(getAPISql);
            String encryptedAccessToken = APIUtil.encryptToken(accessToken);
            ps.setString(1, encryptedAccessToken);
            result = ps.executeQuery();
            while (result.next()) {
                apiList.add(new APIIdentifier(result.getString("API_PROVIDER"), result.getString("API_NAME"),
                                              result.getString("API_VERSION")));
            }
        } catch (SQLException e) {
            handleException("Failed to get API ID for token: " + accessToken, e);
        } catch (CryptoException e) {
            handleException("Failed to get API ID for token: " + accessToken, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, connection, result);
        }
        return apiList;
    }

    /**
     * This method returns the set of APIs for given subscriber, subscribed under the specified application.
     *
     * @param subscriber subscriber
     * @param applicationName Application Name
     * @return Set<API>
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to get SubscribedAPIs
     */
    public Set<SubscribedAPI> getSubscribedAPIs(Subscriber subscriber, String applicationName, String groupingId)
            throws APIManagementException {
        Set<SubscribedAPI> subscribedAPIs = new LinkedHashSet<SubscribedAPI>();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet result = null;
        String sqlQuery = "SELECT " + 
                "   SUBS.SUBSCRIPTION_ID AS SUBS_ID" +
                "   ,API.API_PROVIDER AS API_PROVIDER" +
                "   ,API.API_NAME AS API_NAME" + 
                "   ,API.API_VERSION AS API_VERSION" +
                "   ,SUBS.TIER_ID AS TIER_ID" +
                "   ,APP.APPLICATION_ID AS APP_ID" +
                "   ,SUBS.LAST_ACCESSED AS LAST_ACCESSED" +
                "   ,SUBS.SUB_STATUS AS SUB_STATUS" +
                "   ,SUBS.SUBS_CREATE_STATE AS SUBS_CREATE_STATE" +
                "   ,APP.NAME AS APP_NAME " +
                "   ,APP.CALLBACK_URL AS CALLBACK_URL " +
                "   ,SUBS.UUID AS SUB_UUID " +
                "   ,APP.UUID AS APP_UUID " +
                "FROM " + 
                "   AM_SUBSCRIBER SUB," +
                "   AM_APPLICATION APP, " +
                "   AM_SUBSCRIPTION SUBS, " +
                "   AM_API API " +
                "WHERE "+ 
                "   SUB.TENANT_ID = ? " +
                "   AND APP.APPLICATION_ID=SUBS.APPLICATION_ID " +
                "   AND API.API_ID=SUBS.API_ID" +
                "   AND APP.NAME= ? " +
                "   AND SUBS.SUBS_CREATE_STATE = '" +
                APIConstants.SubscriptionCreatedStatus.SUBSCRIBE + "'";

        String whereClausewithGroupId = " AND (APP.GROUP_ID = ? OR (APP.GROUP_ID = '' AND SUB.USER_ID = ?))" ;
        String whereClausewithGroupIdorceCaseInsensitiveComp = " AND (APP.GROUP_ID = ? OR (APP.GROUP_ID = '' " +
                "AND LOWER(SUB.USER_ID) = LOWER(?)))" ;
        String whereClause = " AND SUB.USER_ID = ? ";
        String whereClauseCaseSensitive =" AND LOWER(SUB.USER_ID) = LOWER(?) ";

        try {
            connection = APIMgtDBUtil.getConnection();
             if (groupingId != null && !groupingId.equals("null") && !groupingId.isEmpty()) {
                 if (forceCaseInsensitiveComparisons) {
                     sqlQuery += whereClausewithGroupIdorceCaseInsensitiveComp;
                 } else {
                     sqlQuery += whereClausewithGroupId;
                 }
             } else {
    		    if (forceCaseInsensitiveComparisons) {
                    sqlQuery += whereClauseCaseSensitive;
               }else{
                   sqlQuery += whereClause ;
               }
            }

            ps = connection.prepareStatement(sqlQuery);
            int tenantId = APIUtil.getTenantId(subscriber.getName());
            ps.setInt(1, tenantId);
            ps.setString(2, applicationName);

            if (groupingId != null && !groupingId.equals("null") && !groupingId.isEmpty()) {
                ps.setString(3, groupingId);
                ps.setString(4, subscriber.getName());
            } else {
                ps.setString(3, subscriber.getName());
            }
            result = ps.executeQuery();

            if (result == null) {
                return subscribedAPIs;
            }

            while (result.next()) {
                APIIdentifier apiIdentifier = new APIIdentifier(APIUtil.replaceEmailDomain(result.getString("API_PROVIDER")),
                                                                result.getString("API_NAME"), result.getString("API_VERSION"));

                SubscribedAPI subscribedAPI = new SubscribedAPI(subscriber, apiIdentifier);
                subscribedAPI.setSubscriptionId(result.getInt("SUBS_ID"));
                subscribedAPI.setSubStatus(result.getString("SUB_STATUS"));
                subscribedAPI.setSubCreatedStatus(result.getString("SUBS_CREATE_STATE"));
                subscribedAPI.setUUID(result.getString("SUB_UUID"));
                subscribedAPI.setTier(new Tier(
                        result.getString(APIConstants.SUBSCRIPTION_FIELD_TIER_ID)));
                subscribedAPI.setLastAccessed(result.getDate(
                        APIConstants.SUBSCRIPTION_FIELD_LAST_ACCESS));

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

    public Integer getSubscriptionCount(Subscriber subscriber,String applicationName,String groupingId)
            throws APIManagementException {
        Integer subscriptionCount = 0;
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet result = null;

        try {
            connection = APIMgtDBUtil.getConnection();

            String sqlQuery = "SELECT COUNT(*) AS SUB_COUNT " +
                              " FROM AM_SUBSCRIPTION SUBS"+
                              " ,AM_APPLICATION APP"+
                              " ,AM_SUBSCRIBER SUB "+
                              " WHERE SUBS.SUBS_CREATE_STATE ='" + APIConstants.SubscriptionCreatedStatus.SUBSCRIBE + "'"+
                              " AND SUBS.APPLICATION_ID = APP.APPLICATION_ID"+
                              " AND APP.NAME=?"+
                              " AND APP.SUBSCRIBER_ID= SUB.SUBSCRIBER_ID"+
                              " AND SUB.TENANT_ID=?";

            if (forceCaseInsensitiveComparisons) {
                sqlQuery = "SELECT COUNT(*) AS SUB_COUNT " +
                            " FROM AM_SUBSCRIPTION SUBS"+
                            " ,AM_APPLICATION APP"+
                            " ,AM_SUBSCRIBER SUB "+
                            " WHERE SUBS.SUBS_CREATE_STATE ='" + APIConstants.SubscriptionCreatedStatus.SUBSCRIBE + "'"+
                            " AND SUBS.APPLICATION_ID = APP.APPLICATION_ID"+
                            " AND APP.NAME=?"+
                            " AND APP.SUBSCRIBER_ID= SUB.SUBSCRIBER_ID"+
                            " AND SUB.TENANT_ID=?";
            }

            String whereClauseWithGroupId = " AND APP.GROUP_ID = ? ";
            String whereClauseWithUserId = " AND SUB.USER_ID = ? ";
            String whereClauseCaseSensitive = " AND LOWER(SUB.USER_ID) = LOWER(?) ";
            String appIdentifier;

            if (groupingId != null && !groupingId.equals("null") && !groupingId.isEmpty()) {
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

            ps = connection.prepareStatement(sqlQuery);
            ps.setString(1, applicationName);
            int tenantId = APIUtil.getTenantId(subscriber.getName());
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
     * @param subscriber the subscriber subscribing for the api
     * @param applicationName the application to which the api's are subscribed
     * @param startSubIndex the start index for pagination
     * @param endSubIndex end index for pagination
     * @param groupingId the group id of the application
     * @return the set of subscribed API's.
     * @throws APIManagementException
     */
    public Set<SubscribedAPI> getPaginatedSubscribedAPIs(Subscriber subscriber,String applicationName, int startSubIndex,
                                                         int endSubIndex, String groupingId)
            throws APIManagementException {
        Set<SubscribedAPI> subscribedAPIs = new LinkedHashSet<SubscribedAPI>();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet result = null;

        String sqlQuery = "SELECT " +
                    "   SUBS.SUBSCRIPTION_ID" +
                    "   ,API.API_PROVIDER AS API_PROVIDER" +
                    "   ,API.API_NAME AS API_NAME" +
                    "   ,API.API_VERSION AS API_VERSION" +
                    "   ,SUBS.TIER_ID AS TIER_ID" +
                    "   ,APP.APPLICATION_ID AS APP_ID" +
                    "   ,SUBS.LAST_ACCESSED AS LAST_ACCESSED" +
                    "   ,SUBS.SUB_STATUS AS SUB_STATUS" +
                    "   ,SUBS.SUBS_CREATE_STATE AS SUBS_CREATE_STATE" +
                    "   ,APP.NAME AS APP_NAME " +
                    "   ,APP.CALLBACK_URL AS CALLBACK_URL " +
                    "FROM " +
                    "   AM_SUBSCRIBER SUB," +
                    "   AM_APPLICATION APP, " +
                    "   AM_SUBSCRIPTION SUBS, " +
                    "   AM_API API " +
                    "WHERE " +
                    "   SUB.TENANT_ID = ? " +
                    "   AND SUB.SUBSCRIBER_ID=APP.SUBSCRIBER_ID " +
                    "   AND APP.APPLICATION_ID=SUBS.APPLICATION_ID " +
                    "   AND API.API_ID=SUBS.API_ID" +
                    "   AND APP.NAME= ? " +
                    "   AND SUBS.SUBS_CREATE_STATE = '" + APIConstants.SubscriptionCreatedStatus.SUBSCRIBE + "'";

            String whereClause = " AND  SUB.USER_ID = ? " ;
            String whereClauseForceCaseInsensitiveComp = " AND LOWER(SUB.USER_ID) = LOWER(?)  ";
            String whereClauseWithGroupId = " AND (APP.GROUP_ID = ? OR (APP.GROUP_ID = '' AND SUB.USER_ID = ?))" ;
            String whereClauseWithGroupIdorceCaseInsensitiveComp = " AND (APP.GROUP_ID = ? OR (APP.GROUP_ID = '' " +
                    "AND LOWER(SUB.USER_ID) = LOWER(?)))" ;
        try {
            connection = APIMgtDBUtil.getConnection();
            int tenantId = APIUtil.getTenantId(subscriber.getName());
            if (groupingId != null && !groupingId.equals("null") && !groupingId.equals("")) {
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

            if (result == null) {
                return subscribedAPIs;
            }

            int index = 0;

            while (result.next()) {
                if(index >= startSubIndex && index < endSubIndex) {
                    APIIdentifier apiIdentifier = new APIIdentifier(APIUtil.replaceEmailDomain(result.getString("API_PROVIDER")),
                            result.getString("API_NAME"), result.getString("API_VERSION"));

                    SubscribedAPI subscribedAPI = new SubscribedAPI(subscriber, apiIdentifier);
                    subscribedAPI.setSubStatus(result.getString("SUB_STATUS"));
                    subscribedAPI.setSubCreatedStatus(result.getString("SUBS_CREATE_STATE"));
                    subscribedAPI.setTier(new Tier(result.getString(APIConstants.SUBSCRIPTION_FIELD_TIER_ID)));
                    subscribedAPI.setLastAccessed(result.getDate(APIConstants.SUBSCRIPTION_FIELD_LAST_ACCESS));

                    Application application = new Application(result.getString("APP_NAME"), subscriber);
                    subscribedAPI.setApplication(application);
                    subscribedAPIs.add(subscribedAPI);
                    if(index == endSubIndex-1){
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
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to get SubscribedAPIs
     */
    public Set<SubscribedAPI> getSubscribedAPIs(Subscriber subscriber, String groupingId) throws APIManagementException {
        Set<SubscribedAPI> subscribedAPIs = new LinkedHashSet<SubscribedAPI>();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet result = null;

        //identify subscribeduser used email/ordinalusername
        String subscribedUserName = getLoginUserName(subscriber.getName());
        subscriber.setName(subscribedUserName);

        String sqlQuery = "SELECT " +
                "   SUBS.SUBSCRIPTION_ID AS SUBS_ID" +
                "   ,API.API_PROVIDER AS API_PROVIDER" +
                "   ,API.API_NAME AS API_NAME" +
                "   ,API.API_VERSION AS API_VERSION" +
                "   ,SUBS.TIER_ID AS TIER_ID" +
                "   ,APP.APPLICATION_ID AS APP_ID" +
                "   ,SUBS.LAST_ACCESSED AS LAST_ACCESSED" +
                "   ,SUBS.SUB_STATUS AS SUB_STATUS" +
                "   ,SUBS.SUBS_CREATE_STATE AS SUBS_CREATE_STATE" +
                "   ,APP.NAME AS APP_NAME " +
                "   ,APP.CALLBACK_URL AS CALLBACK_URL " +
                "   ,SUBS.UUID AS SUB_UUID " +
                "   ,APP.UUID AS APP_UUID " +
                "FROM " +
                "   AM_SUBSCRIBER SUB," +
                "   AM_APPLICATION APP, " +
                "   AM_SUBSCRIPTION SUBS, " +
                "   AM_API API " +
                "WHERE " +
                "   SUB.TENANT_ID = ? " +
                "   AND SUB.SUBSCRIBER_ID=APP.SUBSCRIBER_ID " +
                "   AND APP.APPLICATION_ID=SUBS.APPLICATION_ID " +
                "   AND API.API_ID=SUBS.API_ID" +
                "   AND SUBS.SUBS_CREATE_STATE = '" + APIConstants.SubscriptionCreatedStatus.SUBSCRIBE + "'";
        String whereClause =  " AND  SUB.USER_ID = ? " ;
        String whereClauseCaseInSensitive = " AND  LOWER(SUB.USER_ID) = LOWER(?) ";
        String whereClauseWithGroupId = " AND (APP.GROUP_ID = ? OR (APP.GROUP_ID = '' AND SUB.USER_ID = ?))" ;
        String whereClauseWithGroupIdorceCaseInsensitiveComp = " AND (APP.GROUP_ID = ? OR (APP.GROUP_ID = '' " +
                "AND LOWER(SUB.USER_ID) = LOWER(?)))" ;
        try {
            connection = APIMgtDBUtil.getConnection();

            if (groupingId != null && !groupingId.equals("null") && !groupingId.isEmpty()) {
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
            if(groupingId != null && !groupingId.equals("null") && !groupingId.isEmpty()){
                ps.setString(2, groupingId);
                ps.setString(3, subscriber.getName());
            }else{
                ps.setString(2, subscriber.getName());
            }

            result = ps.executeQuery();

            if (result == null) {
                return subscribedAPIs;
            }

            Map<String, Set<SubscribedAPI>> map = new TreeMap<String, Set<SubscribedAPI>>();
            LRUCache<Integer, Application> applicationCache = new LRUCache<Integer, Application>(100);

            while (result.next()) {
                APIIdentifier apiIdentifier = new APIIdentifier(APIUtil.replaceEmailDomain(result.getString("API_PROVIDER")),
                                                                result.getString("API_NAME"), result.getString("API_VERSION"));

                SubscribedAPI subscribedAPI = new SubscribedAPI(subscriber, apiIdentifier);
                subscribedAPI.setSubscriptionId(result.getInt("SUBS_ID"));
                subscribedAPI.setSubStatus(result.getString("SUB_STATUS"));
                subscribedAPI.setSubCreatedStatus(result.getString("SUBS_CREATE_STATE"));
                String tierName=result.getString(APIConstants.SUBSCRIPTION_FIELD_TIER_ID);
                subscribedAPI.setTier(new Tier(tierName));
                subscribedAPI.setLastAccessed(result.getDate(
                        APIConstants.SUBSCRIPTION_FIELD_LAST_ACCESS));
                subscribedAPI.setUUID(result.getString("SUB_UUID"));
                subscribedAPI.setLastAccessed(result.getDate(APIConstants.SUBSCRIPTION_FIELD_LAST_ACCESS));
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
                    Map<String,OAuthApplicationInfo> oauthApps = getOAuthApplications(applicationId);
                    for(String keyType : oauthApps.keySet()){
                        application.addOAuthApp(keyType,oauthApps.get(keyType));
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
                                return new APIVersionComparator().compare(new API(o1.getApiId()),
                                                                          new API(o2.getApiId()));
                            }
                            return placement;
                        }
                    }));
                }
                map.get(application.getName()).add(subscribedAPI);
            }

            for (String application : map.keySet()) {
                Set<SubscribedAPI> apis = map.get(application);
                for (SubscribedAPI api : apis) {
                    subscribedAPIs.add(api);
                }
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

        String getKeysSql = "SELECT " +
                            " SKM.ACCESS_TOKEN AS ACCESS_TOKEN," +
                            " SKM.KEY_TYPE AS TOKEN_TYPE " +
                            "FROM" +
                            " AM_SUBSCRIPTION_KEY_MAPPING SKM " +
                            "WHERE" +
                            " SKM.SUBSCRIPTION_ID = ?";

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

        if (APIUtil.checkAccessTokenPartitioningEnabled() &&
            APIUtil.checkUserNameAssertionEnabled()) {
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

    private String getTokenScope(String consumerKey, String getScopeSql)
            throws APIManagementException {
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
		String tokenStoreTable = APIConstants.ACCESS_TOKEN_STORE_TABLE;
        String scopeAssociationTable = APIConstants.TOKEN_SCOPE_ASSOCIATION_TABLE;
        String getScopeSql = "SELECT ISAT.TOKEN_SCOPE " +
                "FROM " + tokenStoreTable + " IAT,"+scopeAssociationTable + " ISAT " +
                " WHERE IAT.ACCESS_TOKEN= ?  AND IAT.TOKEN_ID = ISAT.TOKEN_ID";

		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet result = null;
		String tokenScope = null;

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

    public Boolean isAccessTokenExists(String accessToken) throws APIManagementException {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet result = null;

        String accessTokenStoreTable = APIConstants.ACCESS_TOKEN_STORE_TABLE;
        if (APIUtil.checkAccessTokenPartitioningEnabled() &&
            APIUtil.checkUserNameAssertionEnabled()) {
            accessTokenStoreTable = APIUtil.getAccessTokenStoreTableFromAccessToken(accessToken);
        }

        String getTokenSql = "SELECT ACCESS_TOKEN " +
                             "FROM " + accessTokenStoreTable +
                             " WHERE ACCESS_TOKEN= ? ";
        Boolean tokenExists = false;
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

    public Boolean isAccessTokenRevoked(String accessToken) throws APIManagementException {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet result = null;

        String accessTokenStoreTable = APIConstants.ACCESS_TOKEN_STORE_TABLE;
        if (APIUtil.checkAccessTokenPartitioningEnabled() &&
            APIUtil.checkUserNameAssertionEnabled()) {
            accessTokenStoreTable = APIUtil.getAccessTokenStoreTableFromAccessToken(accessToken);
        }

        String getTokenSql = "SELECT TOKEN_STATE " +
                             "FROM " + accessTokenStoreTable +
                             " WHERE ACCESS_TOKEN= ? ";
        Boolean tokenExists = false;
        try {
            connection = APIMgtDBUtil.getConnection();
            ps = connection.prepareStatement(getTokenSql);
            String encryptedAccessToken = APIUtil.encryptToken(accessToken);
            ps.setString(1, encryptedAccessToken);
            result = ps.executeQuery();
            while (result.next()) {
                if (!result.getString("TOKEN_STATE").equals("REVOKED")) {
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
        APIKey apiKey=new APIKey();

        String accessTokenStoreTable = APIConstants.ACCESS_TOKEN_STORE_TABLE;
        String tokenScopeAssociationTable = APIConstants.TOKEN_SCOPE_ASSOCIATION_TABLE;
        String consumerKeyTable = APIConstants.CONSUMER_KEY_SECRET_TABLE;
        if (APIUtil.checkAccessTokenPartitioningEnabled() &&
            APIUtil.checkUserNameAssertionEnabled()) {
            accessTokenStoreTable = APIUtil.getAccessTokenStoreTableFromAccessToken(accessToken);
        }

        String getTokenSql = "SELECT IAT.ACCESS_TOKEN,IAT.AUTHZ_USER, IAT.DOMAIN_NAME, ISAT.TOKEN_SCOPE,ICA.CONSUMER_KEY," +
                            "IAT.TIME_CREATED,IAT.VALIDITY_PERIOD " +
                            "FROM " + accessTokenStoreTable  + " IAT, "+
                            tokenScopeAssociationTable + " ISAT, " +
                            consumerKeyTable + " ICA" +
                            " WHERE IAT.TOKEN_ID = ISAT.TOKEN_ID AND IAT.CONSUMER_KEY_ID = ICA.ID AND" +
                            " IAT.ACCESS_TOKEN= ? AND IAT.TOKEN_STATE='ACTIVE' ";
        try {
            connection = APIMgtDBUtil.getConnection();
            ps = connection.prepareStatement(getTokenSql);
            ps.setString(1, APIUtil.encryptToken(accessToken));
            result = ps.executeQuery();
            if (result.next()) {

                String decryptedAccessToken = APIUtil.decryptToken(result.getString("ACCESS_TOKEN")); // todo - check redundant decryption
                
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
                do{
                    scopes.add(result.getString(APIConstants.IDENTITY_OAUTH2_FIELD_TOKEN_SCOPE));
                } while(result.next());
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
        if (APIUtil.checkAccessTokenPartitioningEnabled()
            && APIUtil.checkUserNameAssertionEnabled()) {
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
            ps  = connection.prepareStatement(getTokenSql);
            result = ps.executeQuery();
            boolean accessTokenRowBreaker=false;

            while (accessTokenRowBreaker || result.next()) {
                accessTokenRowBreaker=false;
                String accessToken = APIUtil.decryptToken(result.getString("ACCESS_TOKEN"));
                String regex = "(?i)[a-zA-Z0-9_.-|]*" + query.trim() + "(?i)[a-zA-Z0-9_.-|]*";
                Pattern pattern;
                Matcher matcher;
                pattern = Pattern.compile(regex);
                matcher = pattern.matcher(accessToken);
                Integer i = 0;
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
                    do{
                        String currentRowTokenString = result.getString("ACCESS_TOKEN");
                        if(tokenString.equals(currentRowTokenString)){
                            scopes.add(result.getString(APIConstants.IDENTITY_OAUTH2_FIELD_TOKEN_SCOPE));
                        } else {
                            accessTokenRowBreaker =true;
                            break;
                        }
                    } while(result.next());
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

    private String getTokenSql (String accessTokenStoreTable) {
        String tokenStoreTable = APIConstants.ACCESS_TOKEN_STORE_TABLE;
        String scopeAssociationTable = APIConstants.TOKEN_SCOPE_ASSOCIATION_TABLE;
        String consumerKeyTable = APIConstants.CONSUMER_KEY_SECRET_TABLE;
        if (accessTokenStoreTable != null) {
            tokenStoreTable = accessTokenStoreTable;
        }

        return "SELECT IAT.ACCESS_TOKEN,IAT.AUTHZ_USER, IAT.USER_DOMAIN, ISAT.TOKEN_SCOPE,ICA.CONSUMER_KEY," +
                "IAT.TIME_CREATED,IAT.VALIDITY_PERIOD " +
                "FROM " + tokenStoreTable + " IAT, " + scopeAssociationTable + " ISAT, " + consumerKeyTable + " ICA" +
                " WHERE IAT.TOKEN_STATE='ACTIVE' AND IAT.TOKEN_ID = ISAT.TOKEN_ID AND IAT.CONSUMER_KEY_ID = ICA.ID" +
                " ORDER BY IAT.TOKEN_ID";

    }

    public Map<Integer, APIKey> getAccessTokensByUser(String user, String loggedInUser) throws APIManagementException {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet result = null;
        Map<Integer, APIKey> tokenDataMap = new HashMap<Integer, APIKey>();

        String accessTokenStoreTable = APIConstants.ACCESS_TOKEN_STORE_TABLE;
        String scopeAssociationTable = APIConstants.TOKEN_SCOPE_ASSOCIATION_TABLE;
        String consumerKeyTable = APIConstants.CONSUMER_KEY_SECRET_TABLE;
        if (APIUtil.checkAccessTokenPartitioningEnabled() &&
            APIUtil.checkUserNameAssertionEnabled()) {
            accessTokenStoreTable = APIUtil.getAccessTokenStoreTableFromUserId(user);
        }

        String getTokenSql = "SELECT IAT.ACCESS_TOKEN,IAT.AUTHZ_USER, IAT.USER_DOMAIN, ISAT.TOKEN_SCOPE,ICA.CONSUMER_KEY," +
                             "IAT.TIME_CREATED,IAT.VALIDITY_PERIOD " +
                             "FROM " + accessTokenStoreTable + " IAT, " + scopeAssociationTable +" ISAT, " + consumerKeyTable + " ICA" +
                             " WHERE IAT.AUTHZ_USER= ? AND IAT.TOKEN_STATE='ACTIVE' AND IAT.TOKEN_ID = ISAT" +
                             ".TOKEN_ID AND IAT.CONSUMER_KEY_ID = ICA.ID ORDER BY IAT.TOKEN_ID";
        try {
            connection = APIMgtDBUtil.getConnection();
            ps = connection.prepareStatement(getTokenSql);
            ps.setString(1, user);
            result = ps.executeQuery();
            Integer i = 0;
            boolean accessTokenRowBreaker=false;
            while (accessTokenRowBreaker || result.next()) {
                accessTokenRowBreaker=false;
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
                    do{
                        String currentRowTokenString = result.getString("ACCESS_TOKEN");
                        if(tokenString.equals(currentRowTokenString)){
                            scopes.add(result.getString(APIConstants.IDENTITY_OAUTH2_FIELD_TOKEN_SCOPE));
                        } else  {
                            accessTokenRowBreaker =true;
                            break;
                        }
                    } while(result.next());
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

        if (APIUtil.checkAccessTokenPartitioningEnabled() &&
            APIUtil.checkUserNameAssertionEnabled()) {
            String[] keyStoreTables = APIUtil.getAvailableKeyStoreTables();
            if (keyStoreTables != null) {
                for (String keyStoreTable : keyStoreTables) {
                    Map<Integer, APIKey> tokenDataMapTmp = getAccessTokensByDate
                            (date, latest, getTokenByDateSqls(keyStoreTable), loggedInUser);
                    tokenDataMap.putAll(tokenDataMapTmp);
                }
            }
        } else {
            tokenDataMap = getAccessTokensByDate(date, latest, getTokenByDateSqls(null), loggedInUser);
        }

        return tokenDataMap;
    }

    public Map<Integer, APIKey> getAccessTokensByDate(String date, boolean latest, String[] querySql, String loggedInUser)
            throws APIManagementException {
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
                    do{
                        String currentRowTokenString = result.getString("ACCESS_TOKEN");
                        if(tokenString.equals(currentRowTokenString)){
                            scopes.add(result.getString(APIConstants.IDENTITY_OAUTH2_FIELD_TOKEN_SCOPE));
                        } else {
                            accessTokenRowBreaker =true;
                            break;
                        }
                    } while(result.next());
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

    public String[] getTokenByDateSqls (String accessTokenStoreTable) {
        String[] querySqlArr = new String[2];
        String tokenStoreTable = APIConstants.ACCESS_TOKEN_STORE_TABLE;
        String scopeAssociationTable = APIConstants.TOKEN_SCOPE_ASSOCIATION_TABLE;
        String consumerKeyTable = APIConstants.CONSUMER_KEY_SECRET_TABLE;
        if (accessTokenStoreTable != null) {
            tokenStoreTable = accessTokenStoreTable;
        }

        querySqlArr[0] = "SELECT IAT.ACCESS_TOKEN,IAT.AUTHZ_USER, IAT.USER_DOMAIN, ISAT.TOKEN_SCOPE,ICA.CONSUMER_KEY," +
                         "IAT.TIME_CREATED,IAT.VALIDITY_PERIOD " +
                         "FROM " + tokenStoreTable  + " IAT, "+ scopeAssociationTable + " ISAT, "+ consumerKeyTable + " ICA" +
                         " WHERE IAT.TOKEN_STATE='ACTIVE' AND IAT.TIME_CREATED >= ? AND IAT.TOKEN_ID" +
                         " = ISAT.TOKEN_ID AND IAT.CONSUMER_KEY_ID = ICA.ID ORDER BY IAT.TOKEN_ID";

        querySqlArr[1] = "SELECT IAT.ACCESS_TOKEN,IAT.AUTHZ_USER, IAT.USER_DOMAIN, ISAT.TOKEN_SCOPE,ICA.CONSUMER_KEY," +
                        "IAT.TIME_CREATED,IAT.VALIDITY_PERIOD " +
                        "FROM " + tokenStoreTable  + " IAT, "+ scopeAssociationTable + " ISAT, "+ consumerKeyTable + " ICA" +
                        " WHERE IAT.TOKEN_STATE='ACTIVE' AND IAT.TIME_CREATED <= ? AND IAT.TOKEN_ID" +
                        " = ISAT.TOKEN_ID AND IAT.CONSUMER_KEY_ID = ICA.ID ORDER BY IAT.TOKEN_ID";

        return querySqlArr;
    }

    private Set<APIKey> getApplicationKeys(String username, int applicationId) throws APIManagementException {

        String accessTokenStoreTable = APIConstants.ACCESS_TOKEN_STORE_TABLE;
        if (APIUtil.checkAccessTokenPartitioningEnabled() &&
            APIUtil.checkUserNameAssertionEnabled()) {
            accessTokenStoreTable = APIUtil.getAccessTokenStoreTableFromUserId(username);
        }

        Set<APIKey> apiKeys = new HashSet<APIKey>();

        try{
            APIKey productionKey = getProductionKeyOfApplication(applicationId, accessTokenStoreTable);
            if(productionKey != null){
                apiKeys.add(productionKey);
            } else {
                productionKey = getKeyStatusOfApplication(APIConstants.API_KEY_TYPE_PRODUCTION,applicationId);
                if(productionKey != null){
                    productionKey.setType(APIConstants.API_KEY_TYPE_PRODUCTION);
                    apiKeys.add(productionKey);
                }
            }

            APIKey sandboxKey = getSandboxKeyOfApplication(applicationId, accessTokenStoreTable);
            if(sandboxKey != null){
                apiKeys.add(sandboxKey);
            }  else {
                sandboxKey = getKeyStatusOfApplication(APIConstants.API_KEY_TYPE_SANDBOX,applicationId);
                if(sandboxKey != null){
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

    private Map<String,OAuthApplicationInfo> getOAuthApplications(int applicationId) throws APIManagementException {
        Map<String,OAuthApplicationInfo> map = new HashMap<String,OAuthApplicationInfo>();
        OAuthApplicationInfo prodApp = getClientOfApplication(applicationId, "PRODUCTION");
        if(prodApp != null){
           map.put("PRODUCTION",prodApp);
        }

        OAuthApplicationInfo sandboxApp = getClientOfApplication(applicationId, "SANDBOX");
        if(sandboxApp != null){
            map.put("SANDBOX",sandboxApp);
        }

        return map;
    }

    public OAuthApplicationInfo getClientOfApplication(int applicationID,String keyType) throws APIManagementException {
        String sqlQuery = "SELECT " +
                "CONSUMER_KEY " +
                "FROM AM_APPLICATION_KEY_MAPPING WHERE APPLICATION_ID = ? AND KEY_TYPE = ?";

        KeyManager keyManager = null;
        OAuthApplicationInfo oAuthApplication = null;
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String consumerKey = null;
        try {
            connection = APIMgtDBUtil.getConnection();
            ps = connection.prepareStatement(sqlQuery);
            ps.setInt(1,applicationID);
            ps.setString(2,keyType);
            rs = ps.executeQuery();

            while (rs.next()){
                consumerKey = rs.getString(1);
            }

            if(consumerKey != null){
                keyManager = KeyManagerHolder.getKeyManagerInstance();
                oAuthApplication = keyManager.retrieveApplication(consumerKey);
                // oAuthApplication.setJsonString(jsonString);
            }
        } catch (SQLException e) {
            handleException("Failed to get  client of application. SQL error", e);
        } finally {
             APIMgtDBUtil.closeAllConnections(ps,connection,rs);
        }

        return oAuthApplication;
    }

    private APIKey getKeyStatusOfApplication(String keyType, int applicationId) throws APIManagementException{
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        APIKey key = null;

        String sqlQuery = "SELECT STATE FROM AM_APPLICATION_KEY_MAPPING WHERE APPLICATION_ID = ? AND KEY_TYPE = ?";

        try {
        connection = APIMgtDBUtil.getConnection();
        preparedStatement = connection.prepareStatement(sqlQuery);
        preparedStatement.setInt(1,applicationId);
        preparedStatement.setString(2, keyType);

        resultSet = preparedStatement.executeQuery();
        while (resultSet.next()){
            key = new APIKey();
            key.setState(resultSet.getString("STATE"));
        }
        } catch (SQLException e) {
            handleException("Error occurred while getting the State of Access Token",e);
        } finally {
            APIMgtDBUtil.closeAllConnections(preparedStatement, connection, resultSet);
        }

        return key;
    }

    /**
     * Gets ConsumerKeys when given the Application ID.
     * @param applicationId
     * @return {@link java.util.Set} containing ConsumerKeys
     * @throws APIManagementException
     */
    public Set<String> getConsumerKeysOfApplication(int applicationId) throws APIManagementException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        Set<String> consumerKeys = new HashSet<String>();

        String sqlQuery = "SELECT CONSUMER_KEY FROM AM_APPLICATION_KEY_MAPPING WHERE APPLICATION_ID = ?";

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
        String scopeAssociationTable = APIConstants.TOKEN_SCOPE_ASSOCIATION_TABLE;
        //The part of the sql query that remain common across databases.
        String statement =
                " ICA.CONSUMER_KEY AS CONSUMER_KEY," +
                        " ICA.CONSUMER_SECRET AS CONSUMER_SECRET," +
                        " IAT.ACCESS_TOKEN AS ACCESS_TOKEN," +
                        " IAT.VALIDITY_PERIOD AS VALIDITY_PERIOD," +
                        " ISAT.TOKEN_SCOPE AS TOKEN_SCOPE," +
                        " AKM.KEY_TYPE AS TOKEN_TYPE, " +
                        " AKM.STATE AS STATE "+
                        "FROM" +
                        " AM_APPLICATION_KEY_MAPPING AKM," +
                        accessTokenStoreTable + " IAT, " + scopeAssociationTable+" ISAT,"+
                        " IDN_OAUTH_CONSUMER_APPS ICA " +
                        "WHERE" +
                        " AKM.APPLICATION_ID = ? AND" +
                        " IAT.USER_TYPE = ? AND" +
                        " ICA.CONSUMER_KEY = AKM.CONSUMER_KEY AND" +
                        " IAT.CONSUMER_KEY_ID = ICA.ID AND" +
                        " IAT.TOKEN_ID = ISAT.TOKEN_ID AND" +
                        " AKM.KEY_TYPE = 'PRODUCTION' AND" +
                        " (IAT.TOKEN_STATE = 'ACTIVE' OR" +
                        " IAT.TOKEN_STATE = 'EXPIRED' OR" +
                        " IAT.TOKEN_STATE = 'REVOKED')" +
                        " ORDER BY IAT.TIME_CREATED DESC";


        String oracleSQL = "SELECT CONSUMER_KEY, " +
                "CONSUMER_SECRET, " +
                "ACCESS_TOKEN, " +
                "VALIDITY_PERIOD, " +
                "TOKEN_SCOPE, " +
                "TOKEN_TYPE, " +
                "STATE " +
                "FROM (" +
                        "SELECT " +
                        "ICA.CONSUMER_KEY AS CONSUMER_KEY, " +
                        "ICA.CONSUMER_SECRET AS CONSUMER_SECRET, " +
                        "IAT.ACCESS_TOKEN AS ACCESS_TOKEN, " +
                        "IAT.VALIDITY_PERIOD AS VALIDITY_PERIOD, " +
                        "ISAT.TOKEN_SCOPE AS TOKEN_SCOPE, " +
                        "AKM.KEY_TYPE AS TOKEN_TYPE, " +
                        "AKM.STATE AS STATE " +
                        "FROM " +
                        "AM_APPLICATION_KEY_MAPPING AKM, " +
                        accessTokenStoreTable + " IAT, " + scopeAssociationTable+" ISAT,"+
                        "IDN_OAUTH_CONSUMER_APPS ICA " +
                        "WHERE " +
                        "AKM.APPLICATION_ID = ? AND " +
                        "IAT.USER_TYPE = ? AND " +
                        "ICA.CONSUMER_KEY = AKM.CONSUMER_KEY AND " +
                        "IAT.CONSUMER_KEY_ID = ICA.ID AND " +
                        "IAT.TOKEN_ID = ISAT.TOKEN_ID AND " +
                        "AKM.KEY_TYPE = 'PRODUCTION' AND " +
                        "(IAT.TOKEN_STATE = 'ACTIVE' OR " +
                        "IAT.TOKEN_STATE = 'EXPIRED' OR " +
                        "IAT.TOKEN_STATE = 'REVOKED') " +
                        "ORDER BY IAT.TIME_CREATED DESC) ";

        String mySQL = "SELECT" + statement ;//+ " LIMIT 1";

        String db2SQL = "SELECT" + statement; //+ " FETCH FIRST 1 ROWS ONLY";

        String msSQL = "SELECT " + statement;

        String postgreSQL = "SELECT * FROM (SELECT" + statement + ") AS TOKEN";

        String authorizedDomains;
        String accessToken;
        String sql;

        try{
            connection = APIMgtDBUtil.getConnection();

            if (connection.getMetaData().getDriverName().contains("MySQL")
                    || connection.getMetaData().getDriverName().contains("H2")) {
                sql = mySQL;
            } else if(connection.getMetaData().getDatabaseProductName().contains("DB2")){
                sql = db2SQL;
            } else if(connection.getMetaData().getDriverName().contains("MS SQL")){
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

                authorizedDomains = getAuthorizedDomains(accessToken);
                apiKey.setType(resultSet.getString("TOKEN_TYPE"));
                apiKey.setAuthorizedDomains(authorizedDomains);
                apiKey.setValidityPeriod(resultSet.getLong("VALIDITY_PERIOD") / 1000);
                apiKey.setState(resultSet.getString("STATE"));

                // Load all the rows to in memory and build the scope string
                List<String> scopes = new ArrayList<String>();
                String tokenString = resultSet.getString("ACCESS_TOKEN");

                do{
                    String currentRowTokenString = resultSet.getString("ACCESS_TOKEN");
                    if(tokenString.equals(currentRowTokenString)){
                        scopes.add(resultSet.getString(APIConstants.IDENTITY_OAUTH2_FIELD_TOKEN_SCOPE));
                    }
                }while(resultSet.next());
                apiKey.setTokenScope(getScopeString(scopes));
                return apiKey;
            }
            return null;
        }finally {
            APIMgtDBUtil.closeAllConnections(preparedStatement, connection, resultSet);
        }
    }

    private APIKey getSandboxKeyOfApplication(int applicationId, String accessTokenStoreTable)
            throws SQLException, CryptoException, APIManagementException {

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        String scopeAssociationTable = APIConstants.TOKEN_SCOPE_ASSOCIATION_TABLE;
        //The part of the sql query that remain common across databases.
        String statement =
                " ICA.CONSUMER_KEY AS CONSUMER_KEY," +
                        " ICA.CONSUMER_SECRET AS CONSUMER_SECRET," +
                        " IAT.ACCESS_TOKEN AS ACCESS_TOKEN," +
                        " IAT.VALIDITY_PERIOD AS VALIDITY_PERIOD," +
                        " ISAT.TOKEN_SCOPE AS TOKEN_SCOPE," +
                        " AKM.KEY_TYPE AS TOKEN_TYPE " +
                        "FROM" +
                        " AM_APPLICATION_KEY_MAPPING AKM," +
                        accessTokenStoreTable + " IAT, " + scopeAssociationTable+" ISAT,"+
                        " IDN_OAUTH_CONSUMER_APPS ICA " +
                        "WHERE" +
                        " AKM.APPLICATION_ID = ? AND" +
                        " IAT.USER_TYPE = ? AND" +
                        " ICA.CONSUMER_KEY = AKM.CONSUMER_KEY AND" +
                        " IAT.CONSUMER_KEY_ID = ICA.ID AND" +
                        " IAT.TOKEN_ID = ISAT.TOKEN_ID AND" +
                        " AKM.KEY_TYPE = 'SANDBOX' AND" +
                        " (IAT.TOKEN_STATE = 'ACTIVE' OR" +
                        " IAT.TOKEN_STATE = 'EXPIRED' OR" +
                        " IAT.TOKEN_STATE = 'REVOKED')" +
                        " ORDER BY IAT.TIME_CREATED DESC";

        String sql;

        //Construct database specific sql statements.
        String oracleSQL =  "SELECT ICA.CONSUMER_KEY AS CONSUMER_KEY," +
                        " ICA.CONSUMER_SECRET AS CONSUMER_SECRET," +
                        " IAT.ACCESS_TOKEN AS ACCESS_TOKEN," +
                        " IAT.VALIDITY_PERIOD AS VALIDITY_PERIOD," +
                        " ISAT.TOKEN_SCOPE AS TOKEN_SCOPE," +
                        " AKM.KEY_TYPE AS TOKEN_TYPE " +
                        "FROM" +
                        " AM_APPLICATION_KEY_MAPPING AKM," +
                        accessTokenStoreTable + " IAT, " + scopeAssociationTable+" ISAT,"+
                        " IDN_OAUTH_CONSUMER_APPS ICA " +
                        "WHERE" +
                        " AKM.APPLICATION_ID = ? AND" +
                        " IAT.USER_TYPE = ? AND" +
                        " ICA.CONSUMER_KEY = AKM.CONSUMER_KEY AND" +
                        " IAT.CONSUMER_KEY_ID = ICA.ID AND" +
                        " IAT.TOKEN_ID = ISAT.TOKEN_ID AND " +
                        " AKM.KEY_TYPE = 'SANDBOX' AND" +
                        " (IAT.TOKEN_STATE = 'ACTIVE' OR" +
                        " IAT.TOKEN_STATE = 'EXPIRED' OR" +
                        " IAT.TOKEN_STATE = 'REVOKED')" +
                        //" AND ROWNUM < 2 " +
                        " ORDER BY IAT.TIME_CREATED DESC ";

        String mySQL = "SELECT" + statement;// + " LIMIT 1";

        String db2SQL = "SELECT" + statement;// + " FETCH FIRST 1 ROWS ONLY";

        String msSQL = "SELECT " + statement;

        String postgreSQL = "SELECT * FROM (SELECT" + statement + ") AS TOKEN";

        String authorizedDomains;
        String accessToken;

        try{
            connection = APIMgtDBUtil.getConnection();

            if (connection.getMetaData().getDriverName().contains("MySQL")
                    || connection.getMetaData().getDriverName().contains("H2")) {
                sql = mySQL;
            } else if(connection.getMetaData().getDatabaseProductName().contains("DB2")){
                sql = db2SQL;
            } else if(connection.getMetaData().getDriverName().contains("MS SQL")){
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
                authorizedDomains = getAuthorizedDomains(accessToken);
                apiKey.setType(resultSet.getString("TOKEN_TYPE"));
                apiKey.setAuthorizedDomains(authorizedDomains);
                apiKey.setValidityPeriod(resultSet.getLong("VALIDITY_PERIOD") / 1000);

                // Load all the rows to in memory and build the scope string
                List<String> scopes = new ArrayList<String>();
                String tokenString = resultSet.getString("ACCESS_TOKEN");

                do{
                    String currentRowTokenString = resultSet.getString("ACCESS_TOKEN");
                    if(tokenString.equals(currentRowTokenString)){
                        scopes.add(resultSet.getString(APIConstants.IDENTITY_OAUTH2_FIELD_TOKEN_SCOPE));
                    }
                } while(resultSet.next());
                apiKey.setTokenScope(getScopeString(scopes));
                return apiKey;
            }
            return null;
        }finally {
            APIMgtDBUtil.closeAllConnections(preparedStatement, connection, resultSet);
        }
    }

    public Set<String> getApplicationKeys(int applicationId) throws APIManagementException {
        Set<String> apiKeys = new HashSet<String>();
        if (APIUtil.checkAccessTokenPartitioningEnabled() &&
            APIUtil.checkUserNameAssertionEnabled()) {
            String[] keyStoreTables = APIUtil.getAvailableKeyStoreTables();
            if (keyStoreTables != null) {
                for (String keyStoreTable : keyStoreTables) {
                    apiKeys = getApplicationKeys(applicationId, getKeysSql(keyStoreTable));
                    if (apiKeys != null) {
                        break;
                    }
                }
            }
        } else {
            apiKeys = getApplicationKeys(applicationId, getKeysSql(null));
        }
        return apiKeys;
    }

    public void updateTierPermissions(String tierName, String permissionType, String roles, int tenantId) throws APIManagementException {
        Connection conn = null;
        PreparedStatement ps = null;
        PreparedStatement insertOrUpdatePS = null;
        ResultSet resultSet = null;
        int tierPermissionId = -1;

        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);

            String getTierPermissionQuery = "SELECT TIER_PERMISSIONS_ID FROM AM_TIER_PERMISSIONS WHERE TIER = ? AND TENANT_ID = ?";
            ps = conn.prepareStatement(getTierPermissionQuery);
            ps.setString(1, tierName);
            ps.setInt(2, tenantId);
            resultSet = ps.executeQuery();
            if (resultSet.next()) {
                tierPermissionId = resultSet.getInt("TIER_PERMISSIONS_ID");
            }


            if (tierPermissionId == -1) {
                String query = "INSERT INTO" +
                               " AM_TIER_PERMISSIONS (TIER, PERMISSIONS_TYPE, ROLES, TENANT_ID)" +
                               " VALUES(?, ?, ?, ?)";
                insertOrUpdatePS = conn.prepareStatement(query);
                insertOrUpdatePS.setString(1, tierName);
                insertOrUpdatePS.setString(2, permissionType);
                insertOrUpdatePS.setString(3, roles);
                insertOrUpdatePS.setInt(4, tenantId);
                insertOrUpdatePS.execute();
            } else {
                String query = "UPDATE" +
                               " AM_TIER_PERMISSIONS SET TIER = ?, PERMISSIONS_TYPE = ?, ROLES = ?" +
                               " WHERE TIER_PERMISSIONS_ID = ? AND TENANT_ID = ?";
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
            conn = APIMgtDBUtil.getConnection();
            String getTierPermissionQuery = "SELECT TIER , PERMISSIONS_TYPE , ROLES  " +
                                            "FROM" +
                                            " AM_TIER_PERMISSIONS " +
                                            "WHERE" +
                                            " TENANT_ID = ?";
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
            handleException("Failed to get Tier permission information " , e);
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
            conn = APIMgtDBUtil.getConnection();
            String getTierPermissionQuery = "SELECT PERMISSIONS_TYPE , ROLES  FROM AM_TIER_PERMISSIONS" +
                                            " WHERE TIER = ? AND TENANT_ID = ?";
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
            handleException("Failed to get Tier permission information for Tier " + tierName , e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, resultSet);
        }
        return tierPermission;
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
        String tokenStoreTable = "IDN_OAUTH2_ACCESS_TOKEN";
        if (accessTokenStoreTable != null) {
            tokenStoreTable = accessTokenStoreTable;
        }

        return "SELECT " +
               " ICA.CONSUMER_KEY AS CONSUMER_KEY," +
               " ICA.CONSUMER_SECRET AS CONSUMER_SECRET," +
               " IAT.ACCESS_TOKEN AS ACCESS_TOKEN," +
               " AKM.KEY_TYPE AS TOKEN_TYPE " +
               "FROM" +
               " AM_APPLICATION_KEY_MAPPING AKM," +
               tokenStoreTable + " IAT," +
               " IDN_OAUTH_CONSUMER_APPS ICA " +
               "WHERE" +
               " AKM.APPLICATION_ID = ? AND" +
               " ICA.CONSUMER_KEY = AKM.CONSUMER_KEY AND" +
               " ICA.ID = IAT.CONSUMER_KEY_ID";
    }

    /**
     * Get access token data based on application ID
     *
     * @param subscriptionId Subscription Id
     * @return access token data
     * @throws APIManagementException
     */
    public Map<String, String> getAccessTokenData(int subscriptionId)
            throws APIManagementException {
        Map<String, String> apiKeys = new HashMap<String, String>();

        if (APIUtil.checkAccessTokenPartitioningEnabled() &&
            APIUtil.checkUserNameAssertionEnabled()) {
            String[] keyStoreTables = APIUtil.getAvailableKeyStoreTables();
            if (keyStoreTables != null) {
                for (String keyStoreTable : keyStoreTables) {
                    apiKeys = getAccessTokenData(subscriptionId,
                                                 getKeysSqlUsingSubscriptionId(keyStoreTable));
                    if (apiKeys != null) {
                        break;
                    }
                }
            }
        } else {
            apiKeys = getAccessTokenData(subscriptionId, getKeysSqlUsingSubscriptionId(null));
        }
        return apiKeys;
    }

    private Map<String, String> getAccessTokenData(int subscriptionId, String getKeysSql) throws APIManagementException {
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
        String tokenStoreTable = "IDN_OAUTH2_ACCESS_TOKEN";
        if (accessTokenStoreTable != null) {
            tokenStoreTable = accessTokenStoreTable;
        }

        return "SELECT " +
               " IAT.ACCESS_TOKEN AS ACCESS_TOKEN," +
               " IAT.TOKEN_STATE AS TOKEN_STATE" +
               " FROM" +
               " AM_APPLICATION_KEY_MAPPING AKM," +
               " AM_SUBSCRIPTION SM," +
               tokenStoreTable + " IAT," +
               " IDN_OAUTH_CONSUMER_APPS ICA " +
               "WHERE" +
               " SM.SUBSCRIPTION_ID = ? AND" +
               " SM.APPLICATION_ID= AKM.APPLICATION_ID AND" +
               " ICA.CONSUMER_KEY = AKM.CONSUMER_KEY AND" +
               " ICA.ID = IAT.CONSUMER_KEY_ID";
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
            connection = APIMgtDBUtil.getConnection();

            String sqlQuery = "SELECT " +
                              "   SUBS.USER_ID AS USER_ID," +
                              "   SUBS.EMAIL_ADDRESS AS EMAIL_ADDRESS, " +
                              "   SUBS.DATE_SUBSCRIBED AS DATE_SUBSCRIBED " +
                              "FROM " +
                              "   AM_SUBSCRIBER  SUBS," +
                              "   AM_APPLICATION  APP, " +
                              "   AM_SUBSCRIPTION SUB, " +
                              "   AM_API API " +
                              "WHERE  " +
                              "   SUB.APPLICATION_ID = APP.APPLICATION_ID " +
                              "   AND SUBS. SUBSCRIBER_ID = APP.SUBSCRIBER_ID " +
                              "   AND API.API_ID = SUB.API_ID " +
                              "   AND API.API_PROVIDER = ?" +
                              " AND SUB.SUBS_CREATE_STATE = '" + APIConstants.SubscriptionCreatedStatus.SUBSCRIBE + "'";


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
            connection = APIMgtDBUtil.getConnection();
            String sqlQuery = "SELECT DISTINCT " +
                              "SB.USER_ID, SB.DATE_SUBSCRIBED " +
                              "FROM AM_SUBSCRIBER SB, AM_SUBSCRIPTION SP,AM_APPLICATION APP,AM_API API" +
                              " WHERE API.API_PROVIDER=? " +
                              "AND API.API_NAME=? " +
                              "AND API.API_VERSION=? " +
                              "AND SP.APPLICATION_ID=APP.APPLICATION_ID" +
                              " AND APP.SUBSCRIBER_ID=SB.SUBSCRIBER_ID " +
                              " AND API.API_ID = SP.API_ID" +
                              " AND SP.SUBS_CREATE_STATE = '" + APIConstants.SubscriptionCreatedStatus.SUBSCRIBE + "'";

            ps = connection.prepareStatement(sqlQuery);
            ps.setString(1, APIUtil.replaceEmailDomainBack(identifier.getProviderName()));
            ps.setString(2, identifier.getApiName());
            ps.setString(3, identifier.getVersion());
            result = ps.executeQuery();
            if (result == null) {
                return subscribers;
            }
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

        String sqlQuery = "SELECT" +
                          " COUNT(SUB.SUBSCRIPTION_ID) AS SUB_ID" +
                          " FROM AM_SUBSCRIPTION SUB, AM_API API " +
                          " WHERE API.API_PROVIDER=? " +
                          " AND API.API_NAME=?" +
                          " AND API.API_VERSION=?" +
                          " AND API.API_ID=SUB.API_ID" +
                          " AND SUB.SUBS_CREATE_STATE = '" + APIConstants.SubscriptionCreatedStatus.SUBSCRIBE + "'";
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
            if (result == null) {
                return subscriptions;
            }
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
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to update subscriber
     */
    public void updateSubscriptions(APIIdentifier identifier, String context, int applicationId)
            throws APIManagementException {
        addSubscription(identifier, context, applicationId, APIConstants.SubscriptionStatus.UNBLOCKED);
    }
    /**
     * This method is used to update the subscription
     *
     * @param identifier    APIIdentifier
     * @param subStatus    Subscription Status[BLOCKED/UNBLOCKED]
     * @param applicationId Application id
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to update subscriber
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

            String getApiQuery = "SELECT API_ID FROM AM_API API WHERE API_PROVIDER = ? AND " +
                                 "API_NAME = ? AND API_VERSION = ?";
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

            if (APIConstants.SubscriptionCreatedStatus.UN_SUBSCRIBE.equals(subsCreateStatus))   {
                deleteSubscriptionByApiIDAndAppID(apiId, applicationId, conn);
            }


            //This query to update the AM_SUBSCRIPTION table
            String sqlQuery ="UPDATE AM_SUBSCRIPTION SET SUB_STATUS = ?, UPDATED_BY = ?, UPDATED_TIME = ? " +
                             "WHERE API_ID = ? AND APPLICATION_ID = ?";

            //Updating data to the AM_SUBSCRIPTION table
            updatePs = conn.prepareStatement(sqlQuery);
            updatePs.setString(1, subStatus);
            //TODO Need to find logged in user who does this update.
            updatePs.setString(2, null);
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
                    log.error("Failed to rollback the add subscription ", e);
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
            String sqlQuery ="UPDATE AM_SUBSCRIPTION SET SUB_STATUS = ?, UPDATED_BY = ?, UPDATED_TIME = ? " +
                    "WHERE UUID = ?";

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
                    log.error("Failed to rollback the update subscription ", e);
                }
            }
            handleException("Failed to update subscription data ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, null);
        }
    }

    public void updateSubscriptionStatus(int subscriptionId, String status) throws APIManagementException{

        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);

            //This query is to update the AM_SUBSCRIPTION table
            String sqlQuery ="UPDATE AM_SUBSCRIPTION SET SUB_STATUS = ? WHERE SUBSCRIPTION_ID = ?";

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
                    log.error("Failed to rollback subscription status update ", e);
                }
            }
            handleException("Failed to update subscription status ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, null);
        }
    }

    /**
     * Update refreshed ApplicationAccesstoken's usertype
     * @param keyType
     * @param newAccessToken
     * @param validityPeriod
     * @return
     * @throws APIManagementException
     */
    public void updateRefreshedApplicationAccessToken(String keyType, String newAccessToken,
                                                      long validityPeriod) throws APIManagementException {

        String accessTokenStoreTable = APIConstants.ACCESS_TOKEN_STORE_TABLE;
        if (APIUtil.checkAccessTokenPartitioningEnabled() &&
            APIUtil.checkUserNameAssertionEnabled()) {
            accessTokenStoreTable = APIUtil.getAccessTokenStoreTableFromAccessToken(newAccessToken);
        }
        // Update Access Token
        String sqlUpdateNewAccessToken = "UPDATE " + accessTokenStoreTable +
                                         " SET USER_TYPE=?, VALIDITY_PERIOD=? " +
                                         " WHERE ACCESS_TOKEN=?";

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
                    log.error("Failed to rollback the add access token ", e);
                }
            }
        } catch (CryptoException e) {
            log.error(e.getMessage(), e);
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException e1) {
                    log.error("Failed to rollback the add access token ", e);
                }
            }
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, null);
        }

    }

    /**
     * This method will delete allow domains record by given consumer key
     * @param consumerKey
     */
    public static void deleteAccessAllowDomains(String consumerKey) throws APIManagementException {

        String sqlDeleteAccessAllowDomains = "DELETE " +
                " FROM AM_APP_KEY_DOMAIN_MAPPING " +
                " WHERE CONSUMER_KEY=?";

        Connection connection = null;
        PreparedStatement prepStmt = null;
        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            prepStmt = connection.prepareStatement(sqlDeleteAccessAllowDomains);
            prepStmt.setString(1, consumerKey);
            prepStmt.execute();

            connection.commit();

        } catch (SQLException e) {
            handleException("Error while deleting allowed domains for application identified " +
                    "by consumer key :" + consumerKey, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, null);
        }
    }

    public static void addAccessAllowDomains(String oAuthConsumerKey, String[] accessAllowDomains) throws
            APIManagementException {

        String sqlAddAccessAllowDomains = "INSERT" +
                " INTO AM_APP_KEY_DOMAIN_MAPPING (CONSUMER_KEY, AUTHZ_DOMAIN) " +
                " VALUES (?,?)";

        Connection connection = null;
        PreparedStatement prepStmt = null;

        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);

            if (accessAllowDomains != null && !accessAllowDomains[0].trim().equals("")) {
                for (String domain : accessAllowDomains)    {
                    prepStmt = connection.prepareStatement(sqlAddAccessAllowDomains);
                    prepStmt.setString(1, oAuthConsumerKey);
                    prepStmt.setString(2, domain.trim());
                    prepStmt.execute();
                    prepStmt.close();

                }
            } else {
                prepStmt = connection.prepareStatement(sqlAddAccessAllowDomains);
                prepStmt.setString(1, oAuthConsumerKey);
                prepStmt.setString(2, "ALL");
                prepStmt.execute();
                prepStmt.close();
            }
            connection.commit();
        } catch (SQLException e) {
            handleException("Error while adding allowed domains for application identified " +
                    "by consumer key :" + oAuthConsumerKey, e);
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException e1) {
                    log.error("Failed to rollback the add access token ", e);
                }
            }
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, null);
        }

    }


    public void updateAccessAllowDomains(String accessToken, String[] accessAllowDomains)
            throws APIManagementException {
        String consumerKey = findConsumerKeyFromAccessToken(accessToken);
        String sqlDeleteAccessAllowDomains = "DELETE " +
                                             " FROM AM_APP_KEY_DOMAIN_MAPPING " +
                                             " WHERE CONSUMER_KEY=?";

        String sqlAddAccessAllowDomains = "INSERT" +
                                          " INTO AM_APP_KEY_DOMAIN_MAPPING (CONSUMER_KEY, AUTHZ_DOMAIN) " +
                                          " VALUES (?,?)";

        Connection connection = null ;
        PreparedStatement prepStmt = null;
        try {

            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);

            //first delete the existing domain list for access token
            prepStmt = connection.prepareStatement(sqlDeleteAccessAllowDomains);
            prepStmt.setString(1, consumerKey);
            prepStmt.execute();
            prepStmt.close();

            //add the new domain list for access token
            if (accessAllowDomains != null && !accessAllowDomains[0].trim().isEmpty()) {
                for (String domain : accessAllowDomains)    {
                    prepStmt = connection.prepareStatement(sqlAddAccessAllowDomains);
                    prepStmt.setString(1, consumerKey);
                    prepStmt.setString(2, domain.trim());
                    prepStmt.execute();
                    prepStmt.close();
                }
            } else {
                prepStmt = connection.prepareStatement(sqlAddAccessAllowDomains);
                prepStmt.setString(1, consumerKey);
                prepStmt.setString(2, "ALL");
                prepStmt.execute();
                prepStmt.close();
            }

            connection.commit();
        } catch (SQLException e) {
            handleException("Failed to update the access allow domains.", e);
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
            String sqlQuery = "SELECT STATE FROM AM_APPLICATION_KEY_MAPPING WHERE APPLICATION_ID = ? AND KEY_TYPE =?";

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
     * @param application
     * @param keyType
     */
    public void updateApplicationKeyTypeMapping(Application application, String keyType) throws APIManagementException {

        OAuthApplicationInfo app = application.getOAuthApp(keyType);
        String consumerKey = null;
        if(app != null){
            consumerKey = app.getClientId();
        }

        if(consumerKey != null && application.getId() != -1){
        String addApplicationKeyMapping = "UPDATE " +
                "AM_APPLICATION_KEY_MAPPING SET " +
                "CONSUMER_KEY = ? " +
                "WHERE APPLICATION_ID = ? AND KEY_TYPE = ?";

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
                APIMgtDBUtil.closeAllConnections(ps,connection,null);
            }

        }

    }

    /**
     * This method will create a new client at key-manager side.further it will add new record to
     * the AM_APPLICATION_KEY_MAPPING table
     *
     * @param keyType
     * @param applicationName apim application name.
     * @param userName apim user name
     * @param clientId this is the consumner key.
     * @throws APIManagementException
     */
    public void createApplicationKeyTypeMappingForManualClients(String keyType, String applicationName,
                                                                String userName, String clientId) throws APIManagementException {

        String consumerKey = null;
        if (clientId != null) {
            consumerKey = clientId;
        }
        Connection connection = null;
        PreparedStatement ps = null;

        //APIM application id.
        int applicationId = getApplicationId(applicationName, userName);

        if (consumerKey != null) {
            String addApplicationKeyMapping = "INSERT INTO " +
                    "AM_APPLICATION_KEY_MAPPING (APPLICATION_ID,CONSUMER_KEY,KEY_TYPE,STATE,CREATE_MODE) " +
                    "VALUES (?,?,?,?,?)";
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
     * @param state State of the registration.
     * @param keyType PRODUCTION | SANDBOX
     * @param appId ID of the Application.
     * @throws APIManagementException if updating fails.
     */
    public void updateApplicationRegistration(String state, String keyType, int appId) throws APIManagementException {
        Connection conn = null;
        PreparedStatement ps = null;

        String sqlStmt = "UPDATE AM_APPLICATION_KEY_MAPPING " +
                "SET STATE = ? WHERE APPLICATION_ID = ? AND KEY_TYPE = ?";

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
        String sqlQuery = "SELECT " +
                          "   SUBS.TIER_ID ," +
                          "   API.API_PROVIDER ," +
                          "   API.API_NAME ," +
                          "   API.API_VERSION ," +
                          "   SUBS.LAST_ACCESSED ," +
                          "   SUBS.APPLICATION_ID " +
                          "FROM " +
                          "   AM_SUBSCRIPTION SUBS," +
                          "   AM_SUBSCRIBER SUB, " +
                          "   AM_APPLICATION  APP, " +
                          "   AM_API API " +
                          "WHERE " +
                          "   API.API_PROVIDER  = ?" +
                          "   AND API.API_NAME = ?" +
                          "   AND API.API_VERSION = ?" +
                          "   AND SUB.USER_ID = ?" +
                          "   AND SUB.TENANT_ID = ? " +
                          "   AND APP.SUBSCRIBER_ID = SUB.SUBSCRIBER_ID" +
                          "   AND API.API_ID = SUBS.API_ID" +
                          "   AND SUBS.SUBS_CREATE_STATE = '" + APIConstants.SubscriptionCreatedStatus.SUBSCRIBE + "'";

        if (forceCaseInsensitiveComparisons) {
            sqlQuery = "SELECT " +
                    "   SUBS.TIER_ID ," +
                    "   API.API_PROVIDER ," +
                    "   API.API_NAME ," +
                    "   API.API_VERSION ," +
                    "   SUBS.LAST_ACCESSED ," +
                    "   SUBS.APPLICATION_ID " +
                    "FROM " +
                    "   AM_SUBSCRIPTION SUBS," +
                    "   AM_SUBSCRIBER SUB, " +
                    "   AM_APPLICATION  APP, " +
                    "   AM_API API " +
                    "WHERE " +
                    "   API.API_PROVIDER  = ?" +
                    "   AND API.API_NAME = ?" +
                    "   AND API.API_VERSION = ?" +
                    "   AND LOWER(SUB.USER_ID) = LOWER(?)" +
                    "   AND SUB.TENANT_ID = ? " +
                    "   AND APP.SUBSCRIBER_ID = SUB.SUBSCRIBER_ID" +
                    "   AND API.API_ID = SUBS.API_ID" +
                    "   AND SUBS.SUBS_CREATE_STATE = '" + APIConstants.SubscriptionCreatedStatus.SUBSCRIBE + "'";
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
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to get
     *          UserApplicationAPIUsage for given provider
     */
    public UserApplicationAPIUsage[] getAllAPIUsageByProvider(String providerName) throws APIManagementException {

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet result = null;


        try {
            connection = APIMgtDBUtil.getConnection();

            String sqlQuery = "SELECT " +
                              "   SUBS.SUBSCRIPTION_ID AS SUBSCRIPTION_ID, " +
                              "   SUBS.APPLICATION_ID AS APPLICATION_ID, " +
                              "   SUBS.SUB_STATUS AS SUB_STATUS, " +
                              "   SUBS.TIER_ID AS TIER_ID, " +
                              "   API.API_PROVIDER AS API_PROVIDER, " +
                              "   API.API_NAME AS API_NAME, " +
                              "   API.API_VERSION AS API_VERSION, " +
                              "   SUBS.LAST_ACCESSED AS LAST_ACCESSED, " +
                              "   SUB.USER_ID AS USER_ID, " +
                              "   APP.NAME AS APPNAME, " +
                              "   SUBS.UUID AS SUB_UUID, " +
                              "   SUBS.TIER_ID AS SUB_TIER_ID, " +
                              "   APP.UUID AS APP_UUID, " +
                              "   SUBS.SUBS_CREATE_STATE AS SUBS_CREATE_STATE " +
                              "FROM " +
                              "   AM_SUBSCRIPTION SUBS, " +
                              "   AM_APPLICATION APP, " +
                              "   AM_SUBSCRIBER SUB, " +
                              "   AM_API API " +
                              "WHERE " +
                              "   SUBS.APPLICATION_ID = APP.APPLICATION_ID " +
                              "   AND APP.SUBSCRIBER_ID = SUB.SUBSCRIBER_ID " +
                              "   AND API.API_PROVIDER = ? " +
                              "   AND API.API_ID = SUBS.API_ID " +
                              "   AND SUBS.SUB_STATUS != '" + APIConstants.SubscriptionStatus.REJECTED + "'" +
                              "ORDER BY " +
                              "   APP.NAME";

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
                APIIdentifier apiId=new APIIdentifier(result.getString("API_PROVIDER"),
                                                      result.getString("API_NAME"), result.getString("API_VERSION"));
                SubscribedAPI apiSubscription=new SubscribedAPI(new Subscriber(userId),apiId);
                apiSubscription.setSubStatus(subStatus);
                apiSubscription.setSubCreatedStatus(subsCreateState);
                apiSubscription.setUUID(result.getString("SUB_UUID"));
                apiSubscription.setTier(new Tier(result.getString("SUB_TIER_ID")));
                Application applicationObj = new Application(result.getString("APP_UUID"));
                apiSubscription.setApplication(applicationObj);
                usage.addApiSubscriptions(apiSubscription);

            }
            return userApplicationUsages.values().toArray(
                    new UserApplicationAPIUsage[userApplicationUsages.size()]);

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
        String query = " SELECT" +
                       " SB.USER_ID, SB.DATE_SUBSCRIBED" +
                       " FROM AM_SUBSCRIBER SB , AM_SUBSCRIPTION SP, AM_APPLICATION APP, AM_SUBSCRIPTION_KEY_MAPPING SKM" +
                       " WHERE SKM.ACCESS_TOKEN=?" +
                       " AND SP.APPLICATION_ID=APP.APPLICATION_ID" +
                       " AND APP.SUBSCRIBER_ID=SB.SUBSCRIBER_ID" +
                       " AND SP.SUBSCRIPTION_ID=SKM.SUBSCRIPTION_ID";

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

    public String[] addOAuthConsumer(String username, int tenantId, String appName, String callbackUrl)
            throws IdentityOAuthAdminException, APIManagementException {

        Connection connection = null;
        PreparedStatement prepStmt = null;
        String loginUserName = getLoginUserName(username);

        ResultSet rs = null;
        String consumerKey = null;
        String consumerSecret = null;

        String sqlCheckStmt =   "SELECT " +
                                "   ICA.CONSUMER_KEY AS CONSUMER_KEY," +
                                "   ICA.CONSUMER_SECRET AS CONSUMER_SECRET " +
                                "FROM " +
                                "   AM_SUBSCRIBER SB," +
                                "   AM_APPLICATION APP, " +
                                "   AM_APPLICATION_KEY_MAPPING AKM," +
                                "   IDN_OAUTH_CONSUMER_APPS ICA " +
                                "WHERE " +
                                "   SB.USER_ID=? " +
                                "   AND SB.TENANT_ID=? " +
                                "   AND APP.NAME=? " +
                                "   AND SB.SUBSCRIBER_ID = APP.SUBSCRIBER_ID " +
                                "   AND AKM.APPLICATION_ID = APP.APPLICATION_ID" +
                                "   AND ICA.USERNAME = SB.USER_ID" +
                                "   AND ICA.TENANT_ID = SB.TENANT_ID" +
                                "   AND ICA.APP_NAME = APP.NAME";

        try{
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
            }
            else {

                String sqlStmt = "INSERT INTO IDN_OAUTH_CONSUMER_APPS " +
                                 "(CONSUMER_KEY, CONSUMER_SECRET, USERNAME, TENANT_ID, OAUTH_VERSION, APP_NAME, CALLBACK_URL) VALUES (?,?,?,?,?,?, ?) ";
                consumerSecret = OAuthUtil.getRandomNumber();

                do {
                    consumerKey = OAuthUtil.getRandomNumber();
                }
                while (isDuplicateConsumer(consumerKey));

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
        String sqlStmt = "UPDATE IDN_OAUTH_CONSUMER_APPS " +
                         "SET CALLBACK_URL = ? WHERE APP_NAME = ?";
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
        String sqlQuery = "SELECT * FROM IDN_OAUTH_CONSUMER_APPS " +
                          "WHERE CONSUMER_KEY=?";

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

            applicationId = addApplication(application,loginUserName, conn);

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error("Failed to rollback the add Application ", e);
                }
            }
            handleException("Failed to add Application", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(null, conn, null);
        }
		return applicationId;
    }

    public void addRating(APIIdentifier apiId, int rating,String user) throws APIManagementException {
        Connection conn = null;
        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);

            addRating(apiId,rating,user, conn);

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error("Failed to rollback the add Application ", e);
                }
            }
            handleException("Failed to add Application", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(null, conn, null);
        }
    }

    /**
     * @param apiIdentifier API Identifier
     * @param userId      User Id
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
            boolean userRatingExists=false;
            //This query to check the ratings already exists for the user in the AM_API_RATINGS table
            String sqlQuery = "SELECT " +
                              "RATING FROM AM_API_RATINGS " +
                              " WHERE API_ID= ? AND SUBSCRIBER_ID=? ";

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
                sqlAddQuery = "INSERT " +
                              "INTO AM_API_RATINGS (RATING,API_ID, SUBSCRIBER_ID)" +
                              " VALUES (?,?,?)";

            } else {
                //This query to insert into the AM_API_RATINGS table
                sqlAddQuery = "UPDATE " +
                              "AM_API_RATINGS SET RATING=? " +
                              "WHERE API_ID= ? AND SUBSCRIBER_ID=?";
            }
            // Adding data to the AM_API_RATINGS  table
            ps = conn.prepareStatement(sqlAddQuery);
            ps.setInt(1, rating);
            ps.setInt(2, apiId);
            ps.setInt(3, subscriber.getId());
            ps.executeUpdate();

        } catch (SQLException e) {
            handleException("Failed to add API rating of the user:"+userId, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, null, rs);
            APIMgtDBUtil.closeAllConnections(psSelect, null, null);
        }
    }

    public void removeAPIRating(APIIdentifier apiId, String user)
            throws APIManagementException {
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
                    log.error("Failed to rollback the add Application ", e);
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
            String sqlQuery = "SELECT " +
                              "RATING_ID FROM AM_API_RATINGS " +
                              " WHERE API_ID= ? AND SUBSCRIBER_ID=? ";

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
                sqlAddQuery = "DELETE " +
                              "FROM AM_API_RATINGS" +
                              " WHERE RATING_ID =? ";
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
        int userRating=0;
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
                    log.error("Failed to rollback getting user ratings ", e);
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
        int userRating=0;
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
            String sqlQuery = "SELECT RATING" +
                              " FROM AM_API_RATINGS " +
                              " WHERE SUBSCRIBER_ID  = ? AND API_ID= ? ";
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

    public static float getAverageRating(APIIdentifier apiId) throws APIManagementException {
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
                    log.error("Failed to rollback getting user ratings ", e);
                }
            }
            handleException("Failed to get user ratings", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(null, conn, null);
        }
        return avrRating;
    }


    public static float getAverageRating(int apiId) throws APIManagementException {
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
            String sqlQuery = "SELECT CAST( SUM(RATING) AS DECIMAL)/COUNT(RATING) AS RATING " +
                              " FROM AM_API_RATINGS" +
                              " WHERE API_ID =? GROUP BY API_ID ";

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
                    log.error("Failed to rollback getting user ratings ", e);
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
    public static float getAverageRating(APIIdentifier apiIdentifier, Connection conn)
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
            String sqlQuery = "SELECT CAST( SUM(RATING) AS DECIMAL)/COUNT(RATING) AS RATING " +
                              " FROM AM_API_RATINGS" +
                              " WHERE API_ID =? GROUP BY API_ID ";

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
        return Float.valueOf(decimal.setScale(1, BigDecimal.ROUND_UP).toString());
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
            String sqlQuery = "INSERT " +
                              "INTO AM_APPLICATION (NAME, SUBSCRIBER_ID, APPLICATION_TIER, CALLBACK_URL, DESCRIPTION, "
                              + "APPLICATION_STATUS, GROUP_ID, CREATED_BY, CREATED_TIME, UUID)" +
                              " VALUES (?,?,?,?,?,?,?,?,?,?)";
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

            if (application.getName() == APIConstants.DEFAULT_APPLICATION_NAME) {
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
                applicationId = Integer.valueOf(rs.getString(1));
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
            String sqlQuery = "UPDATE " +
                              "AM_APPLICATION" +
                              " SET NAME = ? " +
                              ", APPLICATION_TIER = ? " +
                              ", CALLBACK_URL = ? " +
                              ", DESCRIPTION = ? " +
                              ", UPDATED_BY = ? " +
                              ", UPDATED_TIME = ? " +
                              "WHERE" +
                              " APPLICATION_ID = ?";
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
                    log.error("Failed to rollback the update Application ", e);
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

            String updateSqlQuery = "UPDATE " +
                              " AM_APPLICATION" +
                              " SET APPLICATION_STATUS = ? " +
                              "WHERE" +
                              " APPLICATION_ID = ?";

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
                    log.error("Failed to rollback the update Application ", e);
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

    /** get the status of the Application creation process given the application Id
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

            String sqlQuery = "SELECT APPLICATION_STATUS FROM AM_APPLICATION " + "WHERE "
                    + "   APPLICATION_ID= ?";

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
                    log.error("Failed to rollback the update Application ", e);
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
    public static boolean isApplicationExist(String appName, String username, String groupId)
            throws APIManagementException {
        if (username == null) {
            return false;
        }
        Subscriber subscriber = getSubscriber(username);

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        int appId = 0;

        String sqlQuery = "SELECT "
                          + "   APP.APPLICATION_ID "
                          + "FROM "
                          + "   AM_APPLICATION APP,"
                          + "   AM_SUBSCRIBER SUB "
                          + "WHERE "
                          + "   APP.NAME = ?"
                          + "   AND APP.SUBSCRIBER_ID = SUB.SUBSCRIBER_ID";

        String whereClauseWithGroupId = " AND (APP.GROUP_ID = ? OR (APP.GROUP_ID = '' AND SUB.USER_ID = ?))";
        String whereClauseWithGroupIdCaseInsensitive =
                " AND (APP.GROUP_ID = ? OR (APP.GROUP_ID = '' " + "AND LOWER(SUB.USER_ID) = LOWER(?)))";
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

        String sqlQuery = "SELECT " +
                          "   APPLICATION_ID " +

                          "FROM " +
                          "   AM_APPLICATION " +
                          "WHERE " +
                          "   SUBSCRIBER_ID  = ? AND  NAME= ?";

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
     * @param applicationId - applicatoin id
     * @return - application name
     * @throws APIManagementException
     */
    public String getApplicationNameFromId(int applicationId) throws APIManagementException{

        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        String appName = null;

        String sqlQuery = "SELECT NAME " +
                          "FROM AM_APPLICATION " +
                          "WHERE " +
                          "APPLICATION_ID = ?";

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
    public static List<Application> getBasicApplicationDetails(String subscriberName, String groupingId)
            throws APIManagementException {
        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        ArrayList<Application> applications = null;
        String sqlQuery = "SELECT "
                          + "   APPLICATION_ID "
                          + "   ,NAME"
                          + "   ,APPLICATION_TIER"
                          + "   ,APP.SUBSCRIBER_ID  "
                          + "   ,CALLBACK_URL  "
                          + "   ,DESCRIPTION  "
                          + "   ,APPLICATION_STATUS  "
                          + "   ,USER_ID  "
                          + "FROM "
                          + "   AM_APPLICATION APP, "
                          + "   AM_SUBSCRIBER SUB  "
                          + "WHERE "
                          + "   SUB.SUBSCRIBER_ID = APP.SUBSCRIBER_ID ";
        String whereClauseWithGroupId = "   AND "
                                        + "     (GROUP_ID= ? "
                                        + "      OR "
                                        + "     (GROUP_ID='' AND SUB.USER_ID=?))";
        String whereClause = "   AND "
                             + " SUB.USER_ID=?";

        if (groupingId != null && !groupingId.equals("null") && !groupingId.isEmpty()) {
            sqlQuery += whereClauseWithGroupId;
        } else {
            sqlQuery += whereClause;
        }
        try {
            connection = APIMgtDBUtil.getConnection();
            prepStmt = connection.prepareStatement(sqlQuery);
            if (groupingId != null && !groupingId.equals("null") && !groupingId.isEmpty()) {
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
            handleException("Error when reading the application information from" +
                            " the persistence store.", e);
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
        String sqlQuery = "SELECT "
                + "   APPLICATION_ID "
                + "   ,NAME"
                + "   ,APPLICATION_TIER"
                + "   ,APP.SUBSCRIBER_ID  "
                + "   ,CALLBACK_URL  "
                + "   ,DESCRIPTION  "
                + "   ,APPLICATION_STATUS  "
                + "   ,USER_ID  "
                + "   ,GROUP_ID  "
                + "   ,UUID "
                + "FROM "
                + "   AM_APPLICATION APP, "
                + "   AM_SUBSCRIBER SUB  "
                + "WHERE "
                + "   SUB.SUBSCRIBER_ID = APP.SUBSCRIBER_ID ";

        String whereClauseWithGroupId;

        if(forceCaseInsensitiveComparisons){
           whereClauseWithGroupId = "   AND "
                    +"     (GROUP_ID= ? "
                    + "      OR "
                    + "     (GROUP_ID='' AND LOWER(SUB.USER_ID) = LOWER(?)))";
        } else  {
            whereClauseWithGroupId = "   AND "
                    +"     (GROUP_ID= ? "
                    + "      OR "
                    + "     (GROUP_ID='' AND SUB.USER_ID=?))";
        }

        String whereClause;
        if(forceCaseInsensitiveComparisons){
            whereClause = "   AND "
                    + " LOWER(SUB.USER_ID) = LOWER(?)";
        } else  {
            whereClause=  "   AND "
                    + " SUB.USER_ID = ?";
        }

       if(groupingId != null && !groupingId.equals("null") && !groupingId.isEmpty())    {
           sqlQuery += whereClauseWithGroupId;
       } else   {
           sqlQuery += whereClause;
       }
       try {
           connection = APIMgtDBUtil.getConnection();
           prepStmt = connection.prepareStatement(sqlQuery);
           if(groupingId != null && !groupingId.equals("null") && !groupingId.isEmpty()){
              prepStmt.setString(1, groupingId);
              prepStmt.setString(2, subscriber.getName());
           }else{
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

                Set<APIKey> keys = getApplicationKeys(subscriber.getName() , application.getId());
                Map<String,OAuthApplicationInfo> keyMap = getOAuthApplications(application.getId());
                    for (String keyType : keyMap.keySet()){
                            application.addOAuthApp(keyType,keyMap.get(keyType));
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
            handleException("Error when reading the application information from" +
                            " the persistence store.", e);
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
        String sqlQuery = "SELECT " +
                          "   MAP.CONSUMER_KEY " +
                          "FROM " +
                          "   AM_SUBSCRIPTION SUB, " +
                          "   AM_APPLICATION_KEY_MAPPING MAP " +
                          "WHERE " +
                          "   SUB.APPLICATION_ID = MAP.APPLICATION_ID " +
                          "   AND SUB.API_ID = ?";

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

    /** Deletes an Application along with subscriptions, keys and registration data 
     *
     * @param application Application object to be deleted from the database which consists of Id
     * @throws APIManagementException
     */
    public void deleteApplication(Application application) throws APIManagementException {
        Connection con = null;
        try {
            con = APIMgtDBUtil.getConnection();
            con.setAutoCommit(false);
            deleteApplication(application, con);
            con.commit();
        } catch (SQLException e) {
            handleException("Error while removing application details from the database", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(null, con, null);
        }
    }
    public void deleteApplication(Application application, Connection connection) throws APIManagementException {
        PreparedStatement deleteMappingQuery = null;
        PreparedStatement prepStmt = null;
        PreparedStatement prepStmtGetConsumerKey = null;
        PreparedStatement deleteRegistrationQuery = null;
        PreparedStatement deleteSubscription = null;
        PreparedStatement deleteDomainApp = null;
        PreparedStatement deleteAppKey = null;
        PreparedStatement deleteApp = null;
        ResultSet rs = null;

        String getSubscriptionsQuery = "SELECT" +
                                       " SUBSCRIPTION_ID " +
                                       "FROM" +
                                       " AM_SUBSCRIPTION " +
                                       "WHERE" +
                                       " APPLICATION_ID = ?";

        String getConsumerKeyQuery = "SELECT" +
                                       " CONSUMER_KEY , CREATE_MODE" +
                                       " FROM" +
                                       " AM_APPLICATION_KEY_MAPPING " +
                                       " WHERE" +
                                       " APPLICATION_ID = ?";

        String deleteKeyMappingQuery = "DELETE FROM AM_SUBSCRIPTION_KEY_MAPPING WHERE SUBSCRIPTION_ID = ?";
        String deleteSubscriptionsQuery = "DELETE FROM AM_SUBSCRIPTION WHERE APPLICATION_ID = ?";
        String deleteApplicationKeyQuery = "DELETE FROM AM_APPLICATION_KEY_MAPPING WHERE APPLICATION_ID = ?";
        String deleteDomainAppQuery = "DELETE FROM AM_APP_KEY_DOMAIN_MAPPING WHERE CONSUMER_KEY = ?";
        String deleteApplicationQuery = "DELETE FROM AM_APPLICATION WHERE APPLICATION_ID = ?";
        String deleteRegistrationEntry = "DELETE FROM AM_APPLICATION_REGISTRATION WHERE APP_ID = ?";

        try {
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
                deleteMappingQuery.execute();
            }

            if (log.isDebugEnabled()) {
                log.debug("Subscription Key mapping details are deleted successfully for Application - " + application
                        .getName());
            }

            deleteRegistrationQuery = connection.prepareStatement(deleteRegistrationEntry);
            deleteRegistrationQuery.setInt(1, application.getId());
            deleteRegistrationQuery.execute();

            if (log.isDebugEnabled()) {
                log.debug("Application Registration details are deleted successfully for Application - " + application
                        .getName());
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

            while (rs.next()) {
                String consumerKey=rs.getString("CONSUMER_KEY");

                // This is true when OAuth app has been created by pasting consumer key/secret in the screen.
                String mode = rs.getString("CREATE_MODE");
                if (consumerKey != null) {
                    deleteDomainApp = connection.prepareStatement(deleteDomainAppQuery);
                    deleteDomainApp.setString(1, consumerKey);
                    deleteDomainApp.execute();

                    KeyManagerHolder.getKeyManagerInstance().deleteMappedApplication(consumerKey);
                    // OAuth app is deleted if only it has been created from API Store. For mapped clients we don't
                    // call delete.
                    if(!"MAPPED".equals(mode)) {
                        // Adding clients to be deleted.
                        consumerKeys.add(consumerKey);
                    }

                }
            }

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

            for (String consumerKey : consumerKeys){
                //delete on oAuthorization server.
                KeyManagerHolder.getKeyManagerInstance().deleteApplication(consumerKey);
            }
        } catch (SQLException e) {
            handleException("Error while removing application details from the database", e);
        } finally {
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

        String getConsumerKeyQuery = "SELECT" +
                                     " CONSUMER_KEY, KEY_TYPE" +
                                     " FROM" +
                                     " AM_APPLICATION_KEY_MAPPING " +
                                     " WHERE" +
                                     " APPLICATION_ID = ? AND " +
                                     " CREATE_MODE = ?";

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
            log.error(msg);
            throw new APIManagementException(msg);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
        }

        return consumerKeys.toArray(new APIKey[consumerKeys.size()]);
    }

    /**
     * Returns the consumer Key for a given Application Name, Subscriber Name, Key Type, Grouping Id combination.
     * @param applicationName Name of the Application.
     * @param subscriberId Name of Subscriber.
     * @param keyType PRODUCTION | SANDBOX.
     * @param groupingId Grouping ID. When set to null query will be performed using the other three values.
     * @return Consumer Key matching the provided combination.
     * @throws APIManagementException
     */
    public String getConsumerKeyForApplicationKeyType(String applicationName, String subscriberId, String keyType,
                                                      String groupingId) throws APIManagementException {

        String consumerKey = null;
        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        String sqlQuery = "SELECT AKM.CONSUMER_KEY " +
                          "FROM " +
                          "AM_APPLICATION as APP," +
                          "AM_APPLICATION_KEY_MAPPING as AKM," +
                          "AM_SUBSCRIBER as SUB " +
                          "WHERE " +
                          "SUB.SUBSCRIBER_ID=APP.SUBSCRIBER_ID AND APP.APPLICATION_ID = AKM.APPLICATION_ID " +
                          "AND APP.NAME = ? AND AKM.KEY_TYPE=?  ";
        String whereClauseWithGroupId = "   AND "
                                        + "     (GROUP_ID= ? "
                                        + "      OR "
                                        + "     (GROUP_ID='' AND SUB.USER_ID=?))";
        String whereClause = "   AND "
                             + " SUB.USER_ID=?";

        if (groupingId != null && !groupingId.equals("null") && !groupingId.isEmpty()) {
            sqlQuery += whereClauseWithGroupId;
        } else {
            sqlQuery += whereClause;
        }
        try {
            connection = APIMgtDBUtil.getConnection();
            prepStmt = connection.prepareStatement(sqlQuery);
            prepStmt.setString(1, applicationName);
            prepStmt.setString(2, keyType);
            if (groupingId != null && !groupingId.equals("null") && !groupingId.isEmpty()) {
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
            handleException("Error when reading the application information from" +
                    " the persistence store.", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
        }

        return consumerKey;
    }

    /**
     * This method will return a java Map that contains application ID and token type.
     * @param consumerKey consumer key of the oAuth application.
     * @return Map.
     * @throws APIManagementException
     */
    public Map<String,String>  getApplicationIdAndTokenTypeByConsumerKey(String consumerKey) throws APIManagementException {



        Map<String,String> appIdAndConsumerKey = new HashMap<String, String>();

        if (log.isDebugEnabled()) {
            log.debug("fetching application id and token type by consumer key " + consumerKey);
        }

        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;

        String sqlQuery = "SELECT " +
                "   MAP.APPLICATION_ID, " +
                "   MAP.KEY_TYPE " +
                "FROM " +
                "   AM_APPLICATION_KEY_MAPPING MAP " +
                "WHERE " +
                "   MAP.CONSUMER_KEY = ? ";

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
            String deleteKeyMappingQuery = "DELETE " +
                    "FROM" +
                    "   AM_APPLICATION_KEY_MAPPING " +
                    "WHERE" +
                    "   CONSUMER_KEY = ?";
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
            APIMgtDBUtil.closeAllConnections(ps,connection,null);
        }
    }

    /**
     * This method will delete a record from AM_APPLICATION_REGISTRATION
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
            String deleteRegistrationEntry = "DELETE " +
                    "FROM" +
                    "   AM_APPLICATION_KEY_MAPPING  " +
                    "WHERE" +
                    "   APPLICATION_ID = ?" +
                    " AND" +
                    "   KEY_TYPE = ?";

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
            APIMgtDBUtil.closeAllConnections(ps,connection,null);
        }

    }
    /**
     * Delete a record from AM_APPLICATION_REGISTRATION table by application ID and token type.
     * @param applicationId APIM application ID.
     * @param tokenType Token type (PRODUCTION || SANDBOX)
     * @throws APIManagementException if failed to delete the record.
     */
    public void deleteApplicationRegistration(String applicationId, String tokenType) throws APIManagementException {

        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = APIMgtDBUtil.getConnection();
            String deleteRegistrationEntry = "DELETE " +
                    "FROM" +
                    "   AM_APPLICATION_REGISTRATION " +
                    "WHERE" +
                    "   APP_ID = ?" +
                    " AND" +
                    "   TOKEN_TYPE = ?";

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
            APIMgtDBUtil.closeAllConnections(ps,connection,null);
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
            sqlQuery = "SELECT " +
                    "   SUB.SUBSCRIBER_ID AS SUBSCRIBER_ID" +
                    "   ,SUB.USER_ID AS USER_ID " +
                    "   ,SUB.TENANT_ID AS TENANT_ID" +
                    "   ,SUB.EMAIL_ADDRESS AS EMAIL_ADDRESS" +
                    "   ,SUB.DATE_SUBSCRIBED AS DATE_SUBSCRIBED " +
                    "FROM " +
                    "   AM_SUBSCRIBER SUB " +
                    "WHERE " +
                    "LOWER(SUB.USER_ID) = LOWER(?) " +
                    "AND SUB.TENANT_ID = ?";
        } else {
            sqlQuery = "SELECT " +
                    "   SUB.SUBSCRIBER_ID AS SUBSCRIBER_ID" +
                    "   ,SUB.USER_ID AS USER_ID " +
                    "   ,SUB.TENANT_ID AS TENANT_ID" +
                    "   ,SUB.EMAIL_ADDRESS AS EMAIL_ADDRESS" +
                    "   ,SUB.DATE_SUBSCRIBED AS DATE_SUBSCRIBED " +
                    "FROM " +
                    "   AM_SUBSCRIBER SUB " +
                    "WHERE " +
                    "SUB.USER_ID = ? " +
                    "AND SUB.TENANT_ID = ?";
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
            handleException("Error when reading the application information from" +
                            " the persistence store.", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, null, rs);
        }
        return subscriber;
    }

    public void recordAPILifeCycleEvent(APIIdentifier identifier, APIStatus oldStatus,
                                        APIStatus newStatus, String userId)
            throws APIManagementException {
        Connection conn = null;
        try {
            conn = APIMgtDBUtil.getConnection();
            recordAPILifeCycleEvent(identifier, oldStatus.toString(), newStatus.toString(), userId, conn);
        } catch (SQLException e) {
            handleException("Failed to record API state change", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(null, conn, null);
        }
    }
    
    public void recordAPILifeCycleEvent(APIIdentifier identifier, String oldStatus, String newStatus,
            String userId) throws APIManagementException {
        Connection conn = null;
        try {
            conn = APIMgtDBUtil.getConnection();
            recordAPILifeCycleEvent(identifier, oldStatus, newStatus, userId, conn);
        } catch (SQLException e) {
            handleException("Failed to record API state change", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(null, conn, null);
        }
    }

    public void recordAPILifeCycleEvent(APIIdentifier identifier, String oldStatus,
                                        String newStatus, String userId, Connection conn)
            throws APIManagementException {
        //Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        PreparedStatement selectQuerySt = null;

        int tenantId;
        int apiId = -1;
        tenantId = APIUtil.getTenantId(userId);

        if (oldStatus == null && !newStatus.equals(APIStatus.CREATED.toString())) {
            String msg = "Invalid old and new state combination";
            log.error(msg);
            throw new APIManagementException(msg);
        } else if (oldStatus != null && oldStatus.equals(newStatus)) {
            String msg = "No measurable differences in API state";
            log.error(msg);
            throw new APIManagementException(msg);
        }

        String getAPIQuery = "SELECT " +
                             "API.API_ID FROM AM_API API" +
                             " WHERE " +
                             "API.API_PROVIDER = ?" +
                             " AND API.API_NAME = ?" +
                             " AND API.API_VERSION = ?";

        String sqlQuery = "INSERT " +
                          "INTO AM_API_LC_EVENT (API_ID, PREVIOUS_STATE, NEW_STATE, USER_ID, TENANT_ID, EVENT_DATE)" +
                          " VALUES (?,?,?,?,?,?)";

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
                    log.error("Failed to rollback the API state change record", e);
                }
            }
            handleException("Failed to record API state change", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(selectQuerySt, null, null);
           APIMgtDBUtil.closeAllConnections(ps, null, resultSet);
        }
    }

    public void updateDefaultAPIPublishedVersion(APIIdentifier identifier, APIStatus oldStatus,
                                                 APIStatus newStatus) throws APIManagementException {

        Connection conn = null;
        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);

            if(!oldStatus.equals(newStatus)){
                if((newStatus.equals(APIStatus.CREATED) || newStatus.equals(APIStatus.RETIRED)) && (oldStatus.equals(APIStatus.PUBLISHED)
                        || oldStatus.equals(APIStatus.DEPRECATED) || oldStatus.equals(APIStatus.BLOCKED))){
                    setPublishedDefVersion(identifier,conn,null);
                }else if(newStatus.equals(APIStatus.PUBLISHED) || newStatus.equals(APIStatus.DEPRECATED) || newStatus.equals(APIStatus.BLOCKED) ){
                    setPublishedDefVersion(identifier,conn,identifier.getVersion());
                }
            }

            conn.commit();

        } catch (SQLException e) {
            handleException("Failed to update published default API state change", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(null, conn, null);
        }

    }

    public List<LifeCycleEvent> getLifeCycleEvents(APIIdentifier apiId)
            throws APIManagementException {
        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        String sqlQuery = "SELECT" +
                          " LC.API_ID AS API_ID," +
                          " LC.PREVIOUS_STATE AS PREVIOUS_STATE," +
                          " LC.NEW_STATE AS NEW_STATE," +
                          " LC.USER_ID AS USER_ID," +
                          " LC.EVENT_DATE AS EVENT_DATE " +
                          "FROM" +
                          " AM_API_LC_EVENT LC, " +
                          " AM_API API " +
                          "WHERE" +
                          " API.API_PROVIDER = ?" +
                          " AND API.API_NAME = ?" +
                          " AND API.API_VERSION = ?" +
                          " AND API.API_ID = LC.API_ID";

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

    public void makeKeysForwardCompatible(String provider, String apiName, String oldVersion,
                                          String newVersion, String context)
            throws APIManagementException {

        Connection connection = null;
        PreparedStatement prepStmt = null;
        PreparedStatement addSubKeySt = null;
        PreparedStatement getAppSt = null;
        ResultSet rs = null;
        String getSubscriptionDataQuery = "SELECT" +
                                          " SUB.SUBSCRIPTION_ID AS SUBSCRIPTION_ID," +
                                          " SUB.TIER_ID AS TIER_ID," +
                                          " SUB.APPLICATION_ID AS APPLICATION_ID," +
                                          " SUB.SUB_STATUS AS SUB_STATUS," +
                                          " API.CONTEXT AS CONTEXT," +
                                          " SKM.ACCESS_TOKEN AS ACCESS_TOKEN," +
                                          " SKM.KEY_TYPE AS KEY_TYPE" +
                                          " FROM" +
                                          " AM_SUBSCRIPTION SUB," +
                                          " AM_SUBSCRIPTION_KEY_MAPPING SKM, " +
                                          " AM_API API " +
                                          "WHERE" +
                                          " API.API_PROVIDER = ?" +
                                          " AND API.API_NAME = ?" +
                                          " AND API.API_VERSION = ?" +
                                          " AND SKM.SUBSCRIPTION_ID = SUB.SUBSCRIPTION_ID" +
                                          " AND API.API_ID = SUB.API_ID";

        String addSubKeyMapping = "INSERT INTO" +
                                  " AM_SUBSCRIPTION_KEY_MAPPING (SUBSCRIPTION_ID, ACCESS_TOKEN, KEY_TYPE)" +
                                  " VALUES (?,?,?)";

        String getApplicationDataQuery = "SELECT" +
                                         " SUB.SUBSCRIPTION_ID AS SUBSCRIPTION_ID," +
                                         " SUB.TIER_ID AS TIER_ID," +
                                         " SUB.SUB_STATUS AS SUB_STATUS," +
                                         " APP.APPLICATION_ID AS APPLICATION_ID," +
                                         " API.CONTEXT AS CONTEXT " +
                                         "FROM" +
                                         " AM_SUBSCRIPTION SUB," +
                                         " AM_APPLICATION APP," +
                                         " AM_API API " +
                                         "WHERE" +
                                         " API.API_PROVIDER = ?" +
                                         " AND API.API_NAME = ?" +
                                         " AND API.API_VERSION = ?" +
                                         " AND SUB.APPLICATION_ID = APP.APPLICATION_ID" +
                                         " AND API.API_ID = SUB.API_ID";

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
                info.context = rs.getString("CONTEXT");
                info.applicationId = rs.getInt("APPLICATION_ID");
                info.accessToken = rs.getString("ACCESS_TOKEN");  // no decryption needed.
                info.tokenType = rs.getString("KEY_TYPE");
                subscriptionData.add(info);
            }


            Map<Integer, Integer> subscriptionIdMap = new HashMap<Integer, Integer>();
            APIIdentifier apiId = new APIIdentifier(provider, apiName, newVersion);

            for (SubscriptionInfo info : subscriptionData) {
            	try{
            		if (!subscriptionIdMap.containsKey(info.subscriptionId)) {
            			apiId.setTier(info.tierId);
            			int subscriptionId = addSubscription(apiId, context, info.applicationId,
                                APIConstants.SubscriptionStatus.UNBLOCKED);
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
                        addSubscription(apiId, rs.getString("CONTEXT"), applicationId, APIConstants.SubscriptionStatus
                                .UNBLOCKED);
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
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
            APIMgtDBUtil.closeAllConnections(getAppSt, null, null);
            APIMgtDBUtil.closeAllConnections(addSubKeySt, null, null);
        }
    }

    public void addAPI(API api,int tenantId) throws APIManagementException {
        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;

        String query = "INSERT INTO AM_API (API_PROVIDER, API_NAME, API_VERSION, CONTEXT, CONTEXT_TEMPLATE, " +
                       "CREATED_BY, CREATED_TIME) VALUES (?,?,?,?,?,?,?)";

        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);

            prepStmt = connection.prepareStatement(query, new String[]{"api_id"});
            prepStmt.setString(1, APIUtil.replaceEmailDomainBack(api.getId().getProviderName()));
            prepStmt.setString(2, api.getId().getApiName());
            prepStmt.setString(3, api.getId().getVersion());
            prepStmt.setString(4, api.getContext());
            String contextTemplate = api.getContextTemplate();
            //If the context template ends with {version} this means that the version will be at the end of the context.
            if(contextTemplate.endsWith("/" + APIConstants.VERSION_PLACEHOLDER)){
                //Remove the {version} part from the context template.
                contextTemplate = contextTemplate.split(Pattern.quote("/" + APIConstants.VERSION_PLACEHOLDER))[0];
            }
            prepStmt.setString(5, contextTemplate);
            prepStmt.setString(6, APIUtil.replaceEmailDomainBack(api.getId().getProviderName()));
            prepStmt.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
            prepStmt.execute();

            rs = prepStmt.getGeneratedKeys();
            int applicationId = -1;
            if (rs.next()) {
                applicationId = rs.getInt(1);
            }

            connection.commit();

            if(api.getScopes()!= null){
                addScopes(api.getScopes(),applicationId, tenantId);
            }
            addURLTemplates(applicationId, api, connection);
            recordAPILifeCycleEvent(api.getId(), null, APIStatus.CREATED.toString(), APIUtil.replaceEmailDomainBack(api.getId()
                    .getProviderName()), connection);
            //If the api is selected as default version, it is added/replaced into AM_API_DEFAULT_VERSION table
            if(api.isDefaultVersion()){
                addUpdateAPIAsDefaultVersion(api, connection);
            }
            connection.commit();
        } catch (SQLException e) {
            handleException("Error while adding the API: " + api.getId() + " to the database", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
        }

    }

    public String getDefaultVersion(APIIdentifier apiId) throws APIManagementException{
        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        String oldDefaultVersion=null;

        String query =
                "SELECT DEFAULT_API_VERSION FROM AM_API_DEFAULT_VERSION " +
                        "WHERE API_NAME= ? " +
                        "AND API_PROVIDER= ? ";

        try {

            connection = APIMgtDBUtil.getConnection();
            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, apiId.getApiName());
            prepStmt.setString(2, APIUtil.replaceEmailDomainBack(apiId.getProviderName()));

            rs = prepStmt.executeQuery();

            if (rs.next()) {
                oldDefaultVersion= rs.getString("DEFAULT_API_VERSION");
            }

        } catch (SQLException e) {
            handleException("Error while getting default version for "+ apiId.getApiName(), e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
        }

        return oldDefaultVersion;

    }



    /**
     * Persists WorkflowDTO to Database
     * @param workflow
     * @throws APIManagementException
     */
    public void addWorkflowEntry(WorkflowDTO workflow) throws APIManagementException {
        Connection connection = null;
        PreparedStatement prepStmt = null;

        String query = "INSERT INTO AM_WORKFLOWS (WF_REFERENCE, WF_TYPE, WF_STATUS, WF_CREATED_TIME, " +
                "WF_STATUS_DESC, TENANT_ID, TENANT_DOMAIN, WF_EXTERNAL_REFERENCE ) VALUES (?,?,?,?,?,?,?,?)";
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
            handleException("Error while adding Workflow : " + workflow.getExternalWorkflowReference() + " to the database", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, null);
        }
    }

    public void updateWorkflowStatus(WorkflowDTO workflowDTO) throws APIManagementException {
        Connection connection = null;
        PreparedStatement prepStmt = null;

        String query = "UPDATE AM_WORKFLOWS SET WF_STATUS = ?, WF_STATUS_DESC = ?, WF_UPDATED_TIME = ? " +
                       "WHERE WF_EXTERNAL_REFERENCE = ?";
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
        }catch (SQLException e) {
            handleException("Error while updating Workflow Status of workflow " +
                    workflowDTO.getExternalWorkflowReference(), e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, null);
        }
    }

    /**
     * Returns a workflow object for a given external workflow reference.
     * @param workflowReference
     * @return
     * @throws APIManagementException
     */
    public WorkflowDTO retrieveWorkflow(String workflowReference) throws APIManagementException {
        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        WorkflowDTO workflowDTO = null;

        String query = "SELECT * FROM AM_WORKFLOWS WHERE WF_EXTERNAL_REFERENCE=?";
        try {

            connection = APIMgtDBUtil.getConnection();
            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, workflowReference);

            rs = prepStmt.executeQuery();

            while (rs.next()) {
                workflowDTO =  WorkflowExecutorFactory.getInstance().createWorkflowDTO(rs.getString("WF_TYPE"));
                workflowDTO.setStatus(WorkflowStatus.valueOf(rs.getString("WF_STATUS")));
                workflowDTO.setExternalWorkflowReference(rs.getString("WF_EXTERNAL_REFERENCE"));
                workflowDTO.setCreatedTime(rs.getTimestamp("WF_CREATED_TIME").getTime());
                workflowDTO.setWorkflowReference(rs.getString("WF_REFERENCE"));
                workflowDTO.setTenantDomain(rs.getString("TENANT_DOMAIN"));
                workflowDTO.setTenantId(rs.getInt("TENANT_ID"));
                workflowDTO.setWorkflowDescription(rs.getString("WF_STATUS_DESC"));
            }

        }catch (SQLException e) {
            handleException("Error while retrieving workflow details for " +
                    workflowReference, e);
        }finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
        }

        return workflowDTO;
    }


    private void setPublishedDefVersion(APIIdentifier apiId, Connection connection,String value) throws APIManagementException{
        String queryDefaultVersionUpdate = "UPDATE AM_API_DEFAULT_VERSION SET PUBLISHED_DEFAULT_API_VERSION = ? " +
                "WHERE API_NAME = ? " +
                "AND API_PROVIDER = ?" ;


        PreparedStatement prepStmtDefVersionUpdate = null;

        try {
            prepStmtDefVersionUpdate=connection.prepareStatement(queryDefaultVersionUpdate);
            prepStmtDefVersionUpdate.setString(1, value);
            prepStmtDefVersionUpdate.setString(2, apiId.getApiName());
            prepStmtDefVersionUpdate.setString(3,  APIUtil.replaceEmailDomainBack(apiId.getProviderName()));
            prepStmtDefVersionUpdate.execute();
        } catch (SQLException e) {
            handleException("Error while deleting the API default version entry: " + apiId.getApiName() + " from the database", e);
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


    public void removeAPIFromDefaultVersion(APIIdentifier apiId, Connection connection) throws APIManagementException{

        String queryDefaultVersionDelete = "DELETE FROM AM_API_DEFAULT_VERSION " +
                "WHERE API_NAME = ? " +
                "AND API_PROVIDER = ?" ;

        PreparedStatement prepStmtDefVersionDelete = null;

        try {
            prepStmtDefVersionDelete=connection.prepareStatement(queryDefaultVersionDelete);
            prepStmtDefVersionDelete.setString(1, apiId.getApiName());
            prepStmtDefVersionDelete.setString(2, APIUtil.replaceEmailDomainBack(apiId.getProviderName()));
            prepStmtDefVersionDelete.execute();
        } catch (SQLException e) {
            handleException("Error while deleting the API default version entry: " + apiId.getApiName() + " from the database", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmtDefVersionDelete, null, null);
        }

    }


    public String getPublishedDefaultVersion(APIIdentifier apiId) throws APIManagementException{
        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        String publishedDefaultVersion=null;

        String query =
                "SELECT PUBLISHED_DEFAULT_API_VERSION FROM AM_API_DEFAULT_VERSION " +
                        "WHERE API_NAME= ? " +
                        "AND API_PROVIDER= ? ";

        try {

            connection = APIMgtDBUtil.getConnection();
            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, apiId.getApiName());
            prepStmt.setString(2, APIUtil.replaceEmailDomainBack(apiId.getProviderName()));

            rs = prepStmt.executeQuery();

            while (rs.next()) {
              publishedDefaultVersion= rs.getString("PUBLISHED_DEFAULT_API_VERSION");
            }

        } catch (SQLException e) {
            handleException("Error while getting default version for "+ apiId.getApiName(), e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
        }

        return publishedDefaultVersion;
    }

public void addUpdateAPIAsDefaultVersion(API api, Connection connection) throws APIManagementException{

        String publishedDefaultVersion=getPublishedDefaultVersion(api.getId());
        removeAPIFromDefaultVersion(api.getId(), connection);

        PreparedStatement prepStmtDefVersionAdd = null;
        String queryDefaultVersionAdd = "INSERT INTO AM_API_DEFAULT_VERSION (" +
                " API_NAME , API_PROVIDER , DEFAULT_API_VERSION , PUBLISHED_DEFAULT_API_VERSION ) " +
                " VALUES (?,?,?,?)";

        try {
            prepStmtDefVersionAdd=connection.prepareStatement(queryDefaultVersionAdd);
            prepStmtDefVersionAdd.setString(1, api.getId().getApiName());
            prepStmtDefVersionAdd.setString(2,  APIUtil.replaceEmailDomainBack(api.getId().getProviderName()));
            prepStmtDefVersionAdd.setString(3, api.getId().getVersion());
            APIStatus apistatus = api.getStatus();
            if(apistatus.equals(APIStatus.PUBLISHED) || apistatus.equals(APIStatus.DEPRECATED)
                    || apistatus.equals(APIStatus.BLOCKED)){
                prepStmtDefVersionAdd.setString(4,api.getId().getVersion());
            } else {
                prepStmtDefVersionAdd.setString(4,publishedDefaultVersion);
            }

            prepStmtDefVersionAdd.execute();
        } catch (SQLException e) {
            handleException("Error while adding the API default version entry: " + api.getId().getApiName() + " to the database", e);
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

        String query = "INSERT INTO AM_API_URL_MAPPING (API_ID,HTTP_METHOD,AUTH_SCHEME,URL_PATTERN,THROTTLING_TIER,MEDIATION_SCRIPT) VALUES (?,?,?,?,?,?)";
        String scopeQuery = "INSERT INTO IDN_OAUTH2_RESOURCE_SCOPE (RESOURCE_PATH, SCOPE_ID) VALUES (?,?)";
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
                prepStmt.setString(5, uriTemplate.getThrottlingTier());
                InputStream is;
                if(uriTemplate.getMediationScript() != null){
                    is = new ByteArrayInputStream(uriTemplate.getMediationScript().getBytes());
                } else {
                    is = null;
                }
                if (connection.getMetaData().getDriverName().contains("PostgreSQL")
                        || connection.getMetaData().getDatabaseProductName().contains("DB2")) {
                    if(uriTemplate.getMediationScript() != null) {
                        prepStmt.setBinaryStream(6, is, uriTemplate.getMediationScript().getBytes().length);
                    } else  {
                        prepStmt.setBinaryStream(6, is, 0);
                    }
                } else  {
                    prepStmt.setBinaryStream(6, is);
                }
                prepStmt.addBatch();
                if(uriTemplate.getScope()!=null){
                    scopePrepStmt.setString(1, APIUtil.getResourceKey(api,uriTemplate));
                    scopePrepStmt.setInt(2,uriTemplate.getScope().getId());
                    scopePrepStmt.addBatch();
                }
            }
            prepStmt.executeBatch();
            prepStmt.clearBatch();
            scopePrepStmt.executeBatch();
            scopePrepStmt.clearBatch();

        } catch (SQLException e) {
            handleException("Error while adding URL template(s) to the database for API : " + api.getId().toString(), e);
        }
        finally {
            APIMgtDBUtil.closeAllConnections(prepStmt,null,null);
            APIMgtDBUtil.closeAllConnections(scopePrepStmt, null, null);
        }
    }

    /**
     * Fetches an Application by name.
     *
     * @param applicationName Name of the Application
     * @param userId Name of the User.
     * @throws APIManagementException
     */
    public Application getApplicationByName(String applicationName, String userId, String groupId) throws APIManagementException {
        //mysql> select APP.APPLICATION_ID, APP.NAME, APP.SUBSCRIBER_ID,APP.APPLICATION_TIER,APP.CALLBACK_URL,APP.DESCRIPTION,
        // APP.APPLICATION_STATUS from AM_SUBSCRIBER as SUB,AM_APPLICATION as APP
        // where SUB.user_id='admin' AND APP.name='DefaultApplication' AND SUB.SUBSCRIBER_ID=APP.SUBSCRIBER_ID;
        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;

        Application application = null;
        try {
        	connection = APIMgtDBUtil.getConnection();

        	String query = "SELECT " +
    				"APP.APPLICATION_ID," +
    				"APP.NAME," +
    				"APP.SUBSCRIBER_ID," +
    				"APP.APPLICATION_TIER," +
    				"APP.CALLBACK_URL," +
    				"APP.DESCRIPTION, " +
    				"APP.SUBSCRIBER_ID,"+
    				"APP.APPLICATION_STATUS," +
                    "APP.GROUP_ID," +
                    "APP.UUID," +
    				"SUB.USER_ID" +
    				" FROM " +
    				"AM_SUBSCRIBER SUB," +
    				"AM_APPLICATION APP";


        	String whereClause = "  WHERE SUB.USER_ID =? AND APP.NAME=? AND SUB.SUBSCRIBER_ID=APP.SUBSCRIBER_ID";
        	String whereClauseCaseInSensitive = "  WHERE LOWER(SUB.USER_ID) =LOWER(?) AND APP.NAME=? AND SUB.SUBSCRIBER_ID=APP.SUBSCRIBER_ID";
        	String whereClauseWithGroupId = "  WHERE  (APP.GROUP_ID = ? OR (APP.GROUP_ID = '' AND SUB.USER_ID = ?)) AND " +
        	        "APP.NAME = ? AND SUB.SUBSCRIBER_ID = APP.SUBSCRIBER_ID";


            if(groupId != null && !groupId.equals("null") && !groupId.isEmpty()){
                  query += whereClauseWithGroupId;
            } else  {
            	if (forceCaseInsensitiveComparisons) {
    	            query = query + whereClauseCaseInSensitive;
    	        } else {
    	        	query = query + whereClause;
    	        }
            }

            prepStmt = connection.prepareStatement(query);

            if(groupId != null && !groupId.equals("null") && !groupId.isEmpty()){
                prepStmt.setString(1, groupId);
                prepStmt.setString(2, userId);
                prepStmt.setString(3, applicationName);
            }else{
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

            String query = "SELECT " +
                    "APP.APPLICATION_ID," +
                    "APP.NAME," +
                    "APP.SUBSCRIBER_ID," +
                    "APP.APPLICATION_TIER," +
                    "APP.CALLBACK_URL," +
                    "APP.DESCRIPTION, " +
                    "APP.SUBSCRIBER_ID,"+
                    "APP.APPLICATION_STATUS, " +
                    "SUB.USER_ID, " +
                    "APP.GROUP_ID," +
                    "APP.UUID " +
                    "FROM " +
                    "AM_SUBSCRIBER SUB," +
                    "AM_APPLICATION APP " +
                    "WHERE APPLICATION_ID = ? " +
                    "AND APP.SUBSCRIBER_ID = SUB.SUBSCRIBER_ID";

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

    /** Retrieves the Application which is corresponding to the given UUID String
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

            String query = "SELECT " +
                    "APP.APPLICATION_ID," +
                    "APP.NAME," +
                    "APP.SUBSCRIBER_ID," +
                    "APP.APPLICATION_TIER," +
                    "APP.CALLBACK_URL," +
                    "APP.DESCRIPTION, " +
                    "APP.SUBSCRIBER_ID,"+
                    "APP.APPLICATION_STATUS, " +
                    "APP.GROUP_ID, " +
                    "APP.UUID," +
                    "SUB.USER_ID " +
                    "FROM " +
                    "AM_SUBSCRIBER SUB," +
                    "AM_APPLICATION APP " +
                    "WHERE APP.UUID = ? " +
                    "AND APP.SUBSCRIBER_ID = SUB.SUBSCRIBER_ID";

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

                Set<APIKey> keys = getApplicationKeys(subscriber.getName() , application.getId());
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

        String deleteOldMappingsQuery = "DELETE FROM AM_API_URL_MAPPING WHERE API_ID = ?";
        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);

            apiId = getAPIID(api.getId(),connection);
            if (apiId == -1) {
                //application addition has failed
                return;
            }
            prepStmt = connection.prepareStatement(deleteOldMappingsQuery);
            prepStmt.setInt(1,apiId);
            prepStmt.execute();

            addURLTemplates(apiId, api, connection);

            connection.commit();
        } catch (SQLException e) {
            handleException("Error while deleting URL template(s) for API : " + api.getId().toString(), e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, null);
        }

    }

    /**
     * returns all URL templates define for all active(PUBLISHED) APIs.
     */
    public static ArrayList<URITemplate> getAllURITemplates(String apiContext, String version)
            throws APIManagementException {
        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        ArrayList<URITemplate> uriTemplates = new ArrayList<URITemplate>();

        //TODO : FILTER RESULTS ONLY FOR ACTIVE APIs
        String query =
                "SELECT AUM.HTTP_METHOD,AUTH_SCHEME,URL_PATTERN,THROTTLING_TIER,MEDIATION_SCRIPT FROM AM_API_URL_MAPPING AUM, AM_API API " +
                "WHERE API.CONTEXT= ? " +
                "AND API.API_VERSION = ? " +
                "AND AUM.API_ID = API.API_ID " +
                "ORDER BY " +
                "URL_MAPPING_ID";
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
                if(mediationScriptBlob!=null){
                    script = APIMgtDBUtil.getStringFromInputStream(mediationScriptBlob);
                }
                uriTemplate.setMediationScript(script);
                uriTemplates.add(uriTemplate);
            }
        } catch (SQLException e) {
            handleException("Error while fetching all URL Templates", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
        }
        return uriTemplates;
    }


    public void updateAPI(API api,int tenantId) throws APIManagementException {
        Connection connection = null;
        PreparedStatement prepStmt = null;

        String previousDefaultVersion = getDefaultVersion(api.getId());

        String query = "UPDATE AM_API SET CONTEXT = ?, CONTEXT_TEMPLATE = ?, UPDATED_BY = ?, UPDATED_TIME = ? WHERE API_PROVIDER = ? AND API_NAME = ? AND"
                        + " API_VERSION = ? ";
        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);

            if(api.isApiHeaderChanged()){
                prepStmt = connection.prepareStatement(query);
                prepStmt.setString(1, api.getContext());
                String contextTemplate = api.getContextTemplate();
                //If the context template ends with {version} this means that the version will be at the end of the context.
                if(contextTemplate.endsWith("/" + APIConstants.VERSION_PLACEHOLDER)){
                    //Remove the {version} part from the context template.
                    contextTemplate = contextTemplate.split(Pattern.quote("/" + APIConstants.VERSION_PLACEHOLDER))[0];
                }
                prepStmt.setString(2, contextTemplate);
                //TODO Need to find who exactly does this update.
                prepStmt.setString(3, null);
                prepStmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
                prepStmt.setString(5, APIUtil.replaceEmailDomainBack(api.getId().getProviderName()));
                prepStmt.setString(6, api.getId().getApiName());
                prepStmt.setString(7, api.getId().getVersion());
                prepStmt.execute();
            }

            if(api.isDefaultVersion() ^ api.getId().getVersion().equals(previousDefaultVersion)){ //A change has happen
                //If the api is selected as default version, it is added/replaced into AM_API_DEFAULT_VERSION table
                if(api.isDefaultVersion()){
                    addUpdateAPIAsDefaultVersion(api, connection);
                }else{ //tick is removed
                    removeAPIFromDefaultVersion(api.getId(), connection);
                }
            }

            connection.commit();

            updateScopes(api, tenantId);
            updateURLTemplates(api);
        } catch (SQLException e) {
            handleException("Error while updating the API: " + api.getId() + " in the database", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, null);
        }
    }

    public static int getAPIID(APIIdentifier apiId, Connection connection) throws APIManagementException {
        boolean created = false;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;

        int id = -1;
        String getAPIQuery = "SELECT " +
                             "API.API_ID FROM AM_API API" +
                             " WHERE " +
                             "API.API_PROVIDER = ?" +
                             " AND API.API_NAME = ?" +
                             " AND API.API_VERSION = ?";

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
     * @param consumerKey
     * @throws APIManagementException
     */
    public static void deleteApplicationMappingByConsumerKey(String consumerKey)
            throws APIManagementException{
        Connection connection = null;
        PreparedStatement prepStmt = null;

        String deleteApplicationKeyQuery = "DELETE " +
                "FROM " +
                "   AM_APPLICATION_KEY_MAPPING " +
                "WHERE " +
                "   CONSUMER_KEY = ?";

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

        String deleteLCEventQuery = "DELETE FROM AM_API_LC_EVENT WHERE API_ID=? ";
        String deleteCommentQuery = "DELETE FROM AM_API_COMMENTS WHERE API_ID=? ";
        String deleteRatingsQuery = "DELETE FROM AM_API_RATINGS WHERE API_ID=? ";
        String deleteSubscriptionQuery = "DELETE FROM AM_SUBSCRIPTION WHERE API_ID=?";
        String deleteExternalAPIStoresQuery = "DELETE FROM AM_EXTERNAL_STORES WHERE API_ID=?";
        String deleteAPIQuery = "DELETE FROM AM_API WHERE API_PROVIDER=? AND API_NAME=? AND API_VERSION=? ";
        String deleteURLTemplateQuery = "DELETE FROM AM_API_URL_MAPPING WHERE API_ID = ?";

        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);

            id = getAPIID(apiId,connection);

            removeAPIScope(apiId);

            prepStmt = connection.prepareStatement(deleteSubscriptionQuery);
            prepStmt.setInt(1, id);
            prepStmt.execute();
            //Delete all comments associated with given API
            prepStmt = connection.prepareStatement(deleteCommentQuery);
            prepStmt.setInt(1, id);
            prepStmt.execute();

            prepStmt = connection.prepareStatement(deleteRatingsQuery);
            prepStmt.setInt(1, id);
            prepStmt.execute();

            prepStmt = connection.prepareStatement(deleteLCEventQuery);
            prepStmt.setInt(1, id);
            prepStmt.execute();
            //Delete all external APIStore details associated with a given API
            prepStmt = connection.prepareStatement(deleteExternalAPIStoresQuery);
            prepStmt.setInt(1, id);
            prepStmt.execute();

            prepStmt = connection.prepareStatement(deleteAPIQuery);
            prepStmt.setString(1, APIUtil.replaceEmailDomainBack(apiId.getProviderName()));
            prepStmt.setString(2, apiId.getApiName());
            prepStmt.setString(3, apiId.getVersion());
            prepStmt.execute();

            prepStmt = connection.prepareStatement(deleteURLTemplateQuery);
            prepStmt.setInt(1, id);
            prepStmt.execute();

            String curDefaultVersion = getDefaultVersion(apiId);
            String pubDefaultVersion = getPublishedDefaultVersion(apiId);
            if(apiId.getVersion().equals(curDefaultVersion)){
                removeAPIFromDefaultVersion(apiId, connection);
            }else if(apiId.getVersion().equals(pubDefaultVersion)){
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
        if (APIUtil.checkAccessTokenPartitioningEnabled() &&
            APIUtil.checkUserNameAssertionEnabled()) {
            accessTokenStoreTable = APIUtil.getAccessTokenStoreTableFromAccessToken(key);
        }
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);

            String query = "UPDATE " + accessTokenStoreTable + " SET TOKEN_STATE='REVOKED' WHERE ACCESS_TOKEN= ? ";
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
        if (APIUtil.checkAccessTokenPartitioningEnabled() &&
            APIUtil.checkUserNameAssertionEnabled()) {
            accessTokenStoreTable = APIUtil.getAccessTokenStoreTableFromAccessToken(accessToken);
        }
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet result = null;
        String getAPISql =  "SELECT AMA.API_ID, API_NAME, API_PROVIDER, API_VERSION " +
                            "FROM AM_API AMA, " +
                                accessTokenStoreTable + " IAT, " +
                                "AM_APPLICATION_KEY_MAPPING AKM, " +
                                "AM_SUBSCRIPTION AMS, " +
                                "IDN_OAUTH_CONSUMER_APPS ICA " +
                            "WHERE IAT.ACCESS_TOKEN = ? " +
                                "AND ICA.CONSUMER_KEY = AKM.CONSUMER_KEY " +
                                "AND IAT.CONSUMER_KEY_ID = ICA.ID " +
                                "AND AKM.APPLICATION_ID = AMS.APPLICATION_ID " +
                                "AND AMA.API_ID = AMS.API_ID";

        Set<APIIdentifier> apiList = new HashSet<APIIdentifier>();
        try {
            connection = APIMgtDBUtil.getConnection();
            ps = connection.prepareStatement(getAPISql);
            ps.setString(1, APIUtil.encryptToken(accessToken));
            result = ps.executeQuery();
            while (result.next()) {
                apiList.add(new APIIdentifier(APIUtil.replaceEmailDomain(result.getString("API_PROVIDER")),
                                              result.getString("API_NAME"),
                                              result.getString("API_VERSION")));
            }
        } catch (SQLException e) {
            handleException("Failed to get API ID for token: " + accessToken, e);
        } catch (CryptoException e) {
            handleException("Failed to get API ID for token: " + accessToken, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, connection, result);
        }
        return apiList;
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

        String sqlQuery = "SELECT DISTINCT AMS.APPLICATION_ID,NAME,SUBSCRIBER_ID FROM AM_SUBSCRIPTION AMS,AM_APPLICATION AMA " +
                          "WHERE TIER_ID=? " +
                          "AND AMS.APPLICATION_ID=AMA.APPLICATION_ID";

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
            handleException("Error when reading the application information from" +
                            " the persistence store.", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
        }
        return applications;
    }

    private static void handleException(String msg, Throwable t) throws APIManagementException {
        log.error(msg, t);
        throw new APIManagementException(msg, t);
    }

    /**
     * Generates fresh JWT token for given information of validation information
     *
     * @param context         String context for API
     * @param version         version of API
     * @param keyValidationInfoDTO   APIKeyValidationInfoDTO
     * @return signed JWT token string
     * @throws APIManagementException error in generating token
     */
    public String createJWTTokenString(String context, String version, APIKeyValidationInfoDTO keyValidationInfoDTO)
            throws APIManagementException {
        String calleeToken = null;

        if (tokenGenerator != null) {
            calleeToken = generateJWTToken(keyValidationInfoDTO, context, version);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("JWT generator not properly initialized. JWT token will not present in validation info");
            }
        }
        return calleeToken;
    }

    /**
     * Generates fresh JWT token for given information of validation information
     *
     * @param context         String context for API
     * @param version         version of API
     * @param keyValidationInfoDTO   APIKeyValidationInfoDTO
     * @return signed JWT token string
     * @throws APIManagementException error in generating token
     */
    public String createJWTTokenString(String context, String version, APIKeyValidationInfoDTO keyValidationInfoDTO, String accessToken)
            throws APIManagementException {
        String calleeToken = null;

        if (tokenGenerator != null) {
            calleeToken = generateJWTToken(keyValidationInfoDTO, context, version, accessToken);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("JWT generator not properly initialized. JWT token will not present in validation info");
            }
        }
        return calleeToken;
    }

    public static HashMap<String, String> getURITemplatesPerAPIAsString(APIIdentifier identifier)
            throws APIManagementException {
        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        int apiId;
        HashMap<String, String> urlMappings = new LinkedHashMap<String, String>();
        try {
            conn = APIMgtDBUtil.getConnection();
            apiId = getAPIID(identifier, conn);

            String sqlQuery =
                    "SELECT " +
                    "URL_PATTERN" +
                    ",HTTP_METHOD" +
                    ",AUTH_SCHEME" +
                    ",THROTTLING_TIER " +
                    ",MEDIATION_SCRIPT " +
                    "FROM " +
                    "AM_API_URL_MAPPING " +
                    "WHERE " +
                    "API_ID = ? " +
                    "ORDER BY " +
                    "URL_MAPPING_ID ASC ";


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
                    // set null if the script is empty. Otherwise ArrayIndexOutOfBoundsException occurs when trying to split by ::
                    if (script.isEmpty()) {
                        script = null;
                    }
                }
                urlMappings.put(uriPattern + "::" + httpMethod + "::" + authScheme + "::" + throttlingTier + "::" + script, null);
            }
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error("Failed to rollback the add subscription ", e);
                }
            }
            handleException("Failed to add subscriber data ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, resultSet);
        }
        return urlMappings;
    }


    public static boolean isDomainRestricted(String apiKey, String clientDomain) throws APIManagementException {
        boolean restricted = true;
        if (clientDomain != null) {
            clientDomain = clientDomain.trim();
        }
        List<String> authorizedDomains = Arrays.asList(getAuthorizedDomains(apiKey).split(","));
        if (authorizedDomains.contains("ALL") || authorizedDomains.contains(clientDomain)) {
            restricted = false;
        }
        return restricted;
    }

    public static String getAuthorizedDomains(String accessToken) throws APIManagementException {
        String accessTokenStoreTable = APIConstants.ACCESS_TOKEN_STORE_TABLE;
        if (APIUtil.checkAccessTokenPartitioningEnabled() &&
            APIUtil.checkUserNameAssertionEnabled()) {
            accessTokenStoreTable = APIUtil.getAccessTokenStoreTableFromAccessToken(accessToken);
        }
        String authorizedDomains = "";
        String accessAllowDomainsSql = "SELECT AKDM.AUTHZ_DOMAIN " +
                                       "FROM AM_APP_KEY_DOMAIN_MAPPING AKDM, " +
                                             accessTokenStoreTable + " IOAT, " +
                                             "IDN_OAUTH_CONSUMER_APPS IOCA " +
                                       "WHERE IOAT.ACCESS_TOKEN  = ? " +
                                              "AND IOAT.CONSUMER_KEY_ID = IOCA.ID " +
                                              "AND IOCA.CONSUMER_KEY = AKDM.CONSUMER_KEY";

        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        try {
            connection = APIMgtDBUtil.getConnection();
            prepStmt = connection.prepareStatement(accessAllowDomainsSql);
            prepStmt.setString(1, APIUtil.encryptToken(accessToken));
            rs = prepStmt.executeQuery();
            boolean first = true;
            while (rs.next()) {  //if(rs.next==true) -> domain != null
                String domain = rs.getString(1);
                if (first) {
                    authorizedDomains = domain;
                    first = false;
                } else {
                    authorizedDomains = authorizedDomains + "," + domain;
                }
            }
        } catch (SQLException e) {
            throw new APIManagementException
                    ("Error in retrieving access allowing domain list from table.", e);
        } catch (CryptoException e) {
            throw new APIManagementException
                    ("Error in retrieving access allowing domain list from table.", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
        }
        return authorizedDomains;
    }

    // This should be only used only when Token Partitioning is enabled.
    public static  String getConsumerKeyForTokenWhenTokenPartitioningEnabled(String accessToken) throws APIManagementException {

        if (APIUtil.checkAccessTokenPartitioningEnabled() &&
            APIUtil.checkUserNameAssertionEnabled()) {
            String accessTokenStoreTable = APIUtil.getAccessTokenStoreTableFromAccessToken(accessToken);
            String authorizedDomains = "";
            String accessAllowDomainsSql = "SELECT CONSUMER_KEY " +
                                           " FROM " +accessTokenStoreTable+
                                           " WHERE ACCESS_TOKEN = ? ";

            Connection connection = null;
            PreparedStatement prepStmt = null;
            ResultSet rs = null;
            try {
                connection = APIMgtDBUtil.getConnection();
                prepStmt = connection.prepareStatement(accessAllowDomainsSql);
                prepStmt.setString(1, APIUtil.encryptToken(accessToken));
                rs = prepStmt.executeQuery();
                boolean first = true;
                while (rs.next()) {  //if(rs.next==true) -> domain != null
                    String domain = rs.getString(1);
                    if (first) {
                        authorizedDomains = domain;
                        first = false;
                    } else {
                        authorizedDomains = authorizedDomains + "," + domain;
                    }
                }
            } catch (SQLException e) {
                throw new APIManagementException
                        ("Error in retrieving access allowing domain list from table.", e);
            } catch (CryptoException e) {
                throw new APIManagementException
                        ("Error in retrieving access allowing domain list from table.", e);
            } finally {
                APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
            }
            return authorizedDomains;
        }

        return null;
    }

    public static String getAuthorizedDomainsByConsumerKey(String consumerKey) throws APIManagementException {

        String authorizedDomains = "";
        String accessAllowDomainsSql = "SELECT AUTHZ_DOMAIN " +
                                       " FROM AM_APP_KEY_DOMAIN_MAPPING" +
                                       " WHERE CONSUMER_KEY = ? ";

        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        try {
            connection = APIMgtDBUtil.getConnection();
            prepStmt = connection.prepareStatement(accessAllowDomainsSql);
            prepStmt.setString(1, consumerKey);
            rs = prepStmt.executeQuery();
            boolean first = true;
            while (rs.next()) {
                String domain = rs.getString(1);
                if (first) {
                    authorizedDomains = domain;
                    first = false;
                } else {
                    authorizedDomains = authorizedDomains + "," + domain;
                }
            }
        } catch (SQLException e) {
            throw new APIManagementException
                    ("Error in retrieving access allowing domain list from table.", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
        }
        return authorizedDomains;
    }

    public static String findConsumerKeyFromAccessToken(String accessToken) throws APIManagementException {
        String accessTokenStoreTable = APIConstants.ACCESS_TOKEN_STORE_TABLE;
        if (APIUtil.checkAccessTokenPartitioningEnabled() &&
            APIUtil.checkUserNameAssertionEnabled()) {
            accessTokenStoreTable = APIUtil.getAccessTokenStoreTableFromAccessToken(accessToken);
        }
        Connection connection = null;
        PreparedStatement smt = null;
        ResultSet rs = null;
        String consumerKey = null;
        try {
            String getConsumerKeySql = "SELECT ICA.CONSUMER_KEY " +
                                       " FROM " + accessTokenStoreTable + " IAT," +
                                       " IDN_OAUTH_CONSUMER_APPS ICA" +
                                       " WHERE IAT.ACCESS_TOKEN = ? " +
                                       " AND ICA.ID = IAT.CONSUMER_KEY_ID";
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
     * @param identifier	API Identifier
     * @param commentText	Commented Text
     * @param user			User who did the comment
     * @return 				Comment ID
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

            String getApiQuery = "SELECT API_ID FROM AM_API API WHERE API_PROVIDER = ? AND " +
                                 "API_NAME = ? AND API_VERSION = ?";
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
            String addCommentQuery = "INSERT " +
                                     "INTO AM_API_COMMENTS (COMMENT_TEXT,COMMENTED_USER,DATE_COMMENTED,API_ID)" +
                                     " VALUES (?,?,?,?)";

            /*Adding data to the AM_API_COMMENTS table*/
            insertPrepStmt = connection.prepareStatement(addCommentQuery, new String[]{"comment_id"});

            insertPrepStmt.setString(1, commentText);
            insertPrepStmt.setString(2, user);
            insertPrepStmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()),
                                  Calendar.getInstance());
            insertPrepStmt.setInt(4, apiId);

            insertPrepStmt.executeUpdate();
            insertSet = getPrepStmt.getGeneratedKeys();
            while (insertSet.next()) {
                commentId = Integer.valueOf(insertSet.getString(1));
            }

            connection.commit();

        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException e1) {
                    log.error("Failed to rollback the add comment ", e);
                }
            }
            handleException("Failed to add comment data, for  " + identifier.getApiName() + "-"
                            +identifier.getVersion(), e);
        } finally {
            APIMgtDBUtil.closeAllConnections(getPrepStmt, connection, resultSet);
            APIMgtDBUtil.closeAllConnections(insertPrepStmt, null, insertSet);
        }
        return commentId;
    }

    /**
     * Returns all the Comments on an API
     * @param identifier	API Identifier
     * @return				Comment Array
     * @throws APIManagementException
     */
    public Comment[] getComments(APIIdentifier identifier) throws APIManagementException {
        List<Comment> commentList = new ArrayList<Comment>();
        Connection connection = null;
        ResultSet resultSet = null;
        PreparedStatement prepStmt = null;

        String sqlQuery = "SELECT " +
                          "   AM_API_COMMENTS.COMMENT_TEXT AS COMMENT_TEXT," +
                          "   AM_API_COMMENTS.COMMENTED_USER AS COMMENTED_USER," +
                          "   AM_API_COMMENTS.DATE_COMMENTED AS DATE_COMMENTED " +
                          "FROM " +
                          "   AM_API_COMMENTS, " +
                          "   AM_API API " +
                          "WHERE " +
                          "   API.API_PROVIDER = ? " +
                          "   AND API.API_NAME = ? " +
                          "   AND API.API_VERSION  = ? " +
                          "   AND API.API_ID = AM_API_COMMENTS.API_ID";
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
                log.error("Failed to retrieve comments ", e);
            }
            handleException("Failed to retrieve comments for  " + identifier.getApiName() + "-"
                    + identifier.getVersion(), e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, resultSet);
        }
        return commentList.toArray(new Comment[commentList.size()]);
    }

    public static boolean isContextExist(String context) {
        Connection connection = null;
        ResultSet resultSet = null;
        PreparedStatement prepStmt = null;

        String sql = "SELECT CONTEXT FROM AM_API " +
                     " WHERE CONTEXT= ?";
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

    public static String getAPIContext(APIIdentifier identifier) throws APIManagementException {
        Connection connection = null;
        ResultSet resultSet = null;
        PreparedStatement prepStmt = null;

        String  context = null;

        String sql = "SELECT CONTEXT FROM AM_API WHERE " +
                        "API_PROVIDER = ? " +
                        "  AND API_NAME = ? " +
                        "  AND API_VERSION  = ?";
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
                    identifier.getProviderName() + "-" + identifier.getApiName() + "-"
                    + identifier.getVersion(), e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, resultSet);
        }
        return context;
    }

    public static List<String> getAllAvailableContexts () {
        List<String> contexts = new ArrayList<String> ();
        Connection connection = null;
        ResultSet resultSet = null;
        PreparedStatement prepStmt = null;

        String sql = "SELECT CONTEXT FROM AM_API ";
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

    public void populateAppRegistrationWorkflowDTO(ApplicationRegistrationWorkflowDTO workflowDTO) throws APIManagementException {

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Application application = null;
        Subscriber subscriber = null;


        String registrationEntry = "SELECT " +
                "APP.APPLICATION_ID," +
                "APP.NAME," +
                "APP.SUBSCRIBER_ID," +
                "APP.APPLICATION_TIER," +
                "REG.TOKEN_TYPE," +
                "REG.TOKEN_SCOPE," +
                "APP.CALLBACK_URL," +
                "APP.DESCRIPTION," +
                "APP.APPLICATION_STATUS," +
                "SUB.USER_ID," +
                "REG.ALLOWED_DOMAINS," +
                "REG.VALIDITY_PERIOD," +
                "REG.INPUTS" +
                " FROM " +
                "AM_APPLICATION_REGISTRATION REG," +
                "AM_APPLICATION APP," +
                "AM_SUBSCRIBER SUB" +
                " WHERE " +
                "REG.SUBSCRIBER_ID=SUB.SUBSCRIBER_ID AND REG.APP_ID = APP.APPLICATION_ID AND REG.WF_REF=?";


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
                                                                                 application.getCallbackUrl(), rs.getString("TOKEN_SCOPE"), rs.getString("INPUTS"));
                workflowDTO.setAppInfoDTO(request);

            }

        } catch (SQLException e) {
            handleException("Error occurred while retrieving an " +
                    "Application Registration Entry for Workflow : " + workflowDTO.getExternalWorkflowReference(), e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
    }


    public ApplicationRegistrationWorkflowDTO populateAppRegistrationWorkflowDTO(int appId) throws APIManagementException {

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        ApplicationRegistrationWorkflowDTO workflowDTO = null;


        //TODO: Need to create a different Entity for holding Registration Info.
        String registrationEntry = "SELECT " +
                "REG.TOKEN_TYPE," +
                "REG.ALLOWED_DOMAINS," +
                "REG.VALIDITY_PERIOD," +
                " FROM " +
                "AM_APPLICATION_REGISTRATION REG, " +
                "AM_APPLICATION APP " +
                " WHERE " +
                "REG.APP_ID = APP.APPLICATION_ID AND APP.APPLICATION_ID=?";


        try {
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(registrationEntry);
            ps.setInt(1, appId);
            rs = ps.executeQuery();

            while (rs.next()) {
                workflowDTO = (ApplicationRegistrationWorkflowDTO)
                        WorkflowExecutorFactory.getInstance().createWorkflowDTO(WorkflowConstants.WF_TYPE_AM_APPLICATION_REGISTRATION_PRODUCTION);
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

        String registrationEntry = "SELECT " +
                "APP_ID " +
                " FROM " +
                " AM_APPLICATION_REGISTRATION" +
                " WHERE " +
                " WF_REF=?";

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

        String sqlQuery = "SELECT REG.WF_REF FROM " +
                          "AM_APPLICATION APP, " +
                          "AM_APPLICATION_REGISTRATION REG, " +
                          "AM_SUBSCRIBER SUB WHERE " +
                          "APP.NAME=? AND " +
                          "SUB.USER_ID=? AND " +
                          "SUB.SUBSCRIBER_ID = APP.SUBSCRIBER_ID AND " +
                          "REG.APP_ID=APP.APPLICATION_ID";

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

        String sqlQuery = "SELECT WF_EXTERNAL_REFERENCE FROM " +
                "AM_WORKFLOWS WHERE " +
                "WF_TYPE=? AND " +
                "WF_REFERENCE=?";

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

        String sqlQuery = "SELECT AW.WF_EXTERNAL_REFERENCE FROM" +
                " AM_WORKFLOWS AW, AM_SUBSCRIPTION ASUB  WHERE" +
                " ASUB.API_ID=? AND" +
                " ASUB.APPLICATION_ID=? AND" +
                " AW.WF_REFERENCE=ASUB.SUBSCRIPTION_ID AND" +
                " AW.WF_TYPE=?";
        
        String postgreSQL =  "SELECT AW.WF_EXTERNAL_REFERENCE FROM" +
                " AM_WORKFLOWS AW, AM_SUBSCRIPTION ASUB  WHERE" +
                " ASUB.API_ID=? AND" +
                " ASUB.APPLICATION_ID=? AND" +
                " AW.WF_REFERENCE::integer=ASUB.SUBSCRIPTION_ID AND" +
                " AW.WF_TYPE=?";

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
     * @return External workflow reference for the subscription with subscriptionId
     * @throws APIManagementException
     */
    public String getExternalWorkflowReferenceForSubscription(int subscriptionId)
            throws APIManagementException {
        String workflowExtRef = null;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        String sqlQuery = "SELECT WF_EXTERNAL_REFERENCE FROM " +
                "AM_WORKFLOWS WHERE " +
                "WF_REFERENCE=? AND " +
                "WF_TYPE=?";

        try {
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            // setting subscriptionId as string to prevent error when db finds string type IDs for
            // ApplicationRegistration workflows
            ps.setString(1, String.valueOf(subscriptionId));
            ps.setString(2, WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION);
            rs = ps.executeQuery();

            // returns only one row
            while(rs.next()) {
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

        String sqlQuery = "SELECT SUBSCRIPTION_ID FROM " +
                "AM_SUBSCRIPTION WHERE " +
                "APPLICATION_ID=? AND " +
                "SUB_STATUS=?";

        try {
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            ps.setInt(1, applicationId);
            ps.setString(2, APIConstants.SubscriptionStatus.ON_HOLD);
            rs = ps.executeQuery();

            while(rs.next()) {
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
     * @param keyType key type of the registration
     * @return workflow reference of the registration
     * @throws APIManagementException
     */
    public String getRegistrationWFReference(int applicationId, String keyType)
            throws APIManagementException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String reference = null;

        String sqlQuery = "SELECT WF_REF FROM" +
                " AM_APPLICATION_REGISTRATION WHERE" +
                " APP_ID = ? AND TOKEN_TYPE = ?";

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

        String sqlQuery = "SELECT SUB_STATUS FROM" +
                " AM_SUBSCRIPTION WHERE" +
                " API_ID = ? AND" +
                " APPLICATION_ID = ?";

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
     * @param identifier - api identifier which is subscribed
     * @param applicationId - application used to subscribed
     * @param connection
     * @return subscription create status
     * @throws APIManagementException
     */

    public String getSubscriptionCreaeteStatus(APIIdentifier identifier, int applicationId, Connection connection)
            throws APIManagementException   {

        String status = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        String sqlQuery = "SELECT SUBS_CREATE_STATE FROM" +
                          " AM_SUBSCRIPTION WHERE" +
                          " API_ID = ? AND" +
                          " APPLICATION_ID = ?";

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

    private static class SubscriptionInfo {
        private int subscriptionId;
        private String tierId;
        private String context;
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
        return  userId.contains("@");
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
            Map<String, String> userIdConf = loginConfiguration
                    .get(APIConstants.USERID_LOGIN);
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
    private String getPrimaryloginFromSecondary(String login) throws APIManagementException {
        Map<String, Map<String, String>> loginConfiguration = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfigurationService().getAPIManagerConfiguration().getLoginConfiguration();
        String claimURI, username = null;
        if (isUserLoggedInEmail(login)) {
            Map<String, String> emailConf = loginConfiguration.get(APIConstants.EMAIL_LOGIN);
            claimURI = emailConf.get(APIConstants.CLAIM_URI);
        } else {
            Map<String, String> userIdConf = loginConfiguration
                    .get(APIConstants.USERID_LOGIN);
            claimURI = userIdConf.get(APIConstants.CLAIM_URI);
        }

        try {
            RemoteUserManagerClient rmUserClient = new RemoteUserManagerClient(login);
            String[] user = rmUserClient.getUserList(claimURI, login);
            if (user.length > 0) {
                username = user[0];
            }
        } catch (Exception e) {

            handleException("Error while retrieving the primaryLogin name using secondary loginName : "+login, e);
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
            primaryLogin = getPrimaryloginFromSecondary(userID);
        }
        return primaryLogin;
    }


    /**
     * Store external APIStore details to which APIs successfully published
     * @param apiId APIIdentifier
     * @param apiStoreSet APIStores set
     * @return   added/failed
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
            String sqlQuery = "INSERT" +
                    " INTO AM_EXTERNAL_STORES (API_ID, STORE_ID,STORE_DISPLAY_NAME, STORE_ENDPOINT,STORE_TYPE)" +
                    " VALUES (?,?,?,?,?)";

            //Get API Id
            int apiIdentifier;
            apiIdentifier = getAPIID(apiId, conn);
            if (apiIdentifier == -1) {
                String msg = "Could not load API record for: " + apiId.getApiName();
                log.error(msg);
            }
            ps = conn.prepareStatement(sqlQuery);
            Iterator it = apiStoreSet.iterator();
            while (it.hasNext()) {
                Object storeObject = it.next();
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
                    log.error("Failed to rollback storing external apistore details ", e);
                }
            }
            log.error("Failed to store external apistore details", e);
            state = false;
        } catch (APIManagementException e) {
            log.error("Failed to store external apistore details", e);
            state = false;
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, null);

            return state;
        }
    }

    /**
     * Delete the records of external APIStore details.
     * @param apiId APIIdentifier
     * @param apiStoreSet APIStores set
     * @return   added/failed
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

            String sqlQuery = "DELETE" +
                    " FROM AM_EXTERNAL_STORES WHERE API_ID=? AND STORE_ID=? AND STORE_TYPE=?";

            //Get API Id
            int apiIdentifier;
            apiIdentifier = getAPIID(apiId, conn);
            if (apiIdentifier == -1) {
                String msg = "Could not load API record for: " + apiId.getApiName();
                log.error(msg);
            }
            ps = conn.prepareStatement(sqlQuery);
            Iterator it = apiStoreSet.iterator();
            while (it.hasNext()) {
                Object storeObject = it.next();
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
                    log.error("Failed to rollback deleting external apistore details ", e);
                }
            }
            log.error("Failed to delete external apistore details", e);
            state = false;
        } catch (APIManagementException e) {
            log.error("Failed to delete external apistore details", e);
            state = false;
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, null);

            return state;
        }
    }

    public void updateExternalAPIStoresDetails(APIIdentifier apiId,Set<APIStore> apiStoreSet)
            throws APIManagementException {
        Connection conn = null;
        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);

            updateExternalAPIStoresDetails(apiId,apiStoreSet, conn);

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error("Failed to rollback updating external apistore details ", e);
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
     * @param apiIdentifier API Identifier
     * @throws APIManagementException if failed to add Application
     */
    public void updateExternalAPIStoresDetails(APIIdentifier apiIdentifier, Set<APIStore> apiStoreSet, Connection conn)
            throws APIManagementException, SQLException {
        PreparedStatement ps;

        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);

            //This query to add external APIStores to database table
            String sqlQuery = "UPDATE " +
                              "AM_EXTERNAL_STORES"  +
                              " SET " +
                              "   STORE_ENDPOINT = ? " +
                              "   ,STORE_TYPE = ? " +
                              "WHERE " +
                              "   API_ID = ? AND STORE_ID=?";



            ps = conn.prepareStatement(sqlQuery);
            //Get API Id
            int apiId;
            apiId = getAPIID(apiIdentifier, conn);
            if (apiId==-1) {
                String msg = "Could not load API record for: " + apiIdentifier.getApiName();
                log.error(msg);
            }

            Iterator it = apiStoreSet.iterator();
            while (it.hasNext()) {
                Object storeObject = it.next();
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

        }

    }

    /**
     * Return external APIStore details on successfully APIs published
     * @param apiId  APIIdentifier
     * @return  Set of APIStore
     * @throws APIManagementException
     */
    public Set<APIStore> getExternalAPIStoresDetails(APIIdentifier apiId)
            throws APIManagementException {
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
                    log.error("Failed to rollback getting external apistore details ", e);
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
            String sqlQuery = "SELECT " +
                              "   ES.STORE_ID, " +
                              "   ES.STORE_DISPLAY_NAME, " +
                              "   ES.STORE_ENDPOINT, " +
                              "   ES.STORE_TYPE " +
                              "FROM " +
                              "   AM_EXTERNAL_STORES ES " +
                              "WHERE " +
                              "   ES.API_ID = ? ";


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
            handleException("Error while getting External APIStore details from the database for  the API : " + apiIdentifier.getApiName() + "-" + apiIdentifier.getVersion(), e);

        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
        return storesSet;
    }

    public void addScopes(Set<?> objects,int api_id,int tenantID) throws APIManagementException {

        Connection conn = null;
        PreparedStatement ps = null,ps2=null;
        ResultSet rs = null;

        String scopeEntry = "INSERT INTO " +
                " IDN_OAUTH2_SCOPE (SCOPE_KEY, NAME , DESCRIPTION, TENANT_ID, ROLES) " +
                " VALUES(?,?,?,?,?)";

        String scopeLink = "INSERT INTO " +
                " AM_API_SCOPES (API_ID, SCOPE_ID) " +
                " VALUES(?,?)";

        try {
            conn = APIMgtDBUtil.getConnection();

            conn.setAutoCommit(false);

            String scopeId = "SCOPE_ID";
            if (conn.getMetaData().getDriverName().contains("PostgreSQL")) {
            	scopeId = scopeId.toLowerCase();
            }

            if(objects != null){
                for(Object object : objects){
                    ps = conn.prepareStatement(scopeEntry, new String[]{scopeId});
                    ps2 = conn.prepareStatement(scopeLink);

                    if(object instanceof URITemplate){
                        URITemplate uriTemplate = (URITemplate)object;

                        if(uriTemplate.getScope() == null){
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

                        ps2.setInt(1,api_id);
                        ps2.setInt(2,uriTemplate.getScope().getId());
                        ps2.execute();
                        conn.commit();
                    } else if(object instanceof Scope){
                        Scope scope = (Scope)object;
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
                        ps2.setInt(1,api_id);
                        ps2.setInt(2,scope.getId());
                        ps2.execute();
                        conn.commit();
                    }
                }
            }
        } catch (SQLException e) {
            try {
                if(conn != null)
                    conn.rollback();
            } catch (SQLException e1) {
                handleException("Error occurred while Roling back changes done on Scopes Creation", e1);
            }
            handleException("Error occurred while creating scopes " , e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
            APIMgtDBUtil.closeAllConnections(ps2, null, null);
        }
    }


    public static Set<Scope> getAPIScopes(APIIdentifier identifier) throws APIManagementException {
        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        Set<Scope> scopes= new LinkedHashSet<Scope>();
        int apiId;
        try {
            conn = APIMgtDBUtil.getConnection();
            apiId = getAPIID(identifier, conn);

            String sqlQuery ="SELECT "
                    +"A.SCOPE_ID, A.SCOPE_KEY, A.NAME, A.DESCRIPTION, A.ROLES "
                    +"FROM IDN_OAUTH2_SCOPE AS A "
                    +"INNER JOIN AM_API_SCOPES AS B "
                    +"ON A.SCOPE_ID = B.SCOPE_ID WHERE B.API_ID = ?";

            if (conn.getMetaData().getDriverName().contains("Oracle")) {
            	sqlQuery ="SELECT "
                        +"A.SCOPE_ID, A.SCOPE_KEY, A.NAME, A.DESCRIPTION, A.ROLES "
                        +"FROM IDN_OAUTH2_SCOPE A "
                        +"INNER JOIN AM_API_SCOPES B "
                        +"ON A.SCOPE_ID = B.SCOPE_ID WHERE B.API_ID = ?";
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

			String commaSeperatedIds = StringUtils.join(apiIds.iterator(), ",");

			String sqlQuery =
					"SELECT DISTINCT A.SCOPE_KEY, A.NAME, A.DESCRIPTION, A.ROLES " +
					"FROM IDN_OAUTH2_SCOPE AS A INNER JOIN AM_API_SCOPES AS B " +
					"ON A.SCOPE_ID = B.SCOPE_ID WHERE B.API_ID IN (" + commaSeperatedIds + ")";

			if (conn.getMetaData().getDriverName().contains("Oracle")) {
				sqlQuery = "SELECT DISTINCT A.SCOPE_KEY, A.NAME, A.DESCRIPTION, A.ROLES " +
				           "FROM IDN_OAUTH2_SCOPE A INNER JOIN AM_API_SCOPES B " +
				           "ON A.SCOPE_ID = B.SCOPE_ID WHERE B.API_ID IN (" + commaSeperatedIds +
				           ")";
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

    public static Set<Scope> getAPIScopesByScopeKey(String scopeKey, int tenantId)
            throws APIManagementException {
        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        Set<Scope> scopes= new LinkedHashSet<Scope>();
        try {
            conn = APIMgtDBUtil.getConnection();

            String sqlQuery ="SELECT IAS.SCOPE_ID, IAS.SCOPE_KEY, IAS.NAME, IAS.DESCRIPTION, IAS.TENANT_ID, IAS.ROLES FROM " +
                                "IDN_OAUTH2_SCOPE IAS " +
                                "WHERE SCOPE_KEY = ? AND TENANT_ID = ?";

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
		StringBuilder scopeStrBuilder = new StringBuilder("");
		for (String inputScope : inputScopeList) {
			scopeStrBuilder.append("'").append(inputScope).append("',");
		}
		String scopesString = scopeStrBuilder.toString();
		scopesString = scopesString.substring(0, scopesString.length() - 1);
		try {
			conn = APIMgtDBUtil.getConnection();

			String sqlQuery =
					"SELECT IAS.SCOPE_ID, IAS.SCOPE_KEY, IAS.NAME, IAS.DESCRIPTION, IAS.TENANT_ID, IAS.ROLES " +
					"FROM IDN_OAUTH2_SCOPE IAS " +
					"WHERE SCOPE_KEY IN (" + scopesString + ") AND TENANT_ID = ?";

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

        String deleteScopes = "DELETE FROM IDN_OAUTH2_SCOPE WHERE SCOPE_ID IN ( SELECT SCOPE_ID FROM AM_API_SCOPES " +
                "WHERE API_ID = ? )";
        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);

            apiId = getAPIID(api.getId(),connection);
            if (apiId == -1) {
                //application addition has failed
                return;
            }

            prepStmt = connection.prepareStatement(deleteScopes);
            prepStmt.setInt(1,apiId);
            prepStmt.execute();

            connection.commit();
        } catch (SQLException e) {
            handleException("Error while deleting Scopes for API : " + api.getId().toString(), e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, null);
        }
        addScopes(api.getUriTemplates(),apiId,tenantId);
    }

    public static HashMap<String,String> getResourceToScopeMapping(APIIdentifier identifier) throws APIManagementException{
        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        HashMap<String,String> map = new HashMap<String, String>();
        int apiId;
        try {
            conn = APIMgtDBUtil.getConnection();
            apiId = getAPIID(identifier, conn);

            String sqlQuery ="SELECT "
                    +"RS.RESOURCE_PATH, S.SCOPE_KEY "
                    +"FROM IDN_OAUTH2_RESOURCE_SCOPE RS "
                    +"INNER JOIN IDN_OAUTH2_SCOPE S ON S.SCOPE_ID = RS.SCOPE_ID "
                    +"INNER JOIN AM_API_SCOPES A ON A.SCOPE_ID = RS.SCOPE_ID "
                    +"WHERE A.API_ID = ? ";


            ps = conn.prepareStatement(sqlQuery);
            ps.setInt(1, apiId);
            resultSet = ps.executeQuery();
            while (resultSet.next()) {
                map.put(resultSet.getString(1),resultSet.getString(2));
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

            String sqlQuery = "SELECT IOS.SCOPE_KEY, IOS.ROLES " +
                    "FROM IDN_OAUTH2_SCOPE IOS, " +
                    "AM_APPLICATION_KEY_MAPPING AKM, " +
                    "AM_SUBSCRIPTION SUB, " +
                    "AM_API_SCOPES SCOPE " +
                    "WHERE AKM.CONSUMER_KEY = ? " +
                    "AND AKM.APPLICATION_ID = SUB.APPLICATION_ID " +
                    "AND SUB.API_ID = SCOPE.API_ID " +
                    "AND SCOPE.SCOPE_ID = IOS.SCOPE_ID";

            ps = conn.prepareStatement(sqlQuery);
            ps.setString(1, consumerKey);
            resultSet = ps.executeQuery();
            Map<String, String> scopes = new HashMap<String, String>();
            while (resultSet.next()) {
                scopes.put(resultSet.getString("SCOPE_KEY"), resultSet.getString("ROLES"));
            }
            return scopes;
        } catch (SQLException e) {
            handleException("Failed to retrieve scopes of applicaltion " + consumerKey, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, resultSet);
        }
        return null;
    }

    public static String getUserFromOauthToken(String oauthToken) throws APIManagementException {

        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        String tokenOwner = null;

        try {
            conn = APIMgtDBUtil.getConnection();
            String getUserQuery = "SELECT DISTINCT AMS.USER_ID  ,AKM.CONSUMER_KEY " +
                    "FROM AM_APPLICATION_KEY_MAPPING AKM, AM_APPLICATION AA, AM_SUBSCRIBER AMS " +
                    "WHERE AKM.CONSUMER_KEY = ? AND " +
                    "AKM.APPLICATION_ID = AA.APPLICATION_ID AND AA.SUBSCRIBER_ID = AMS.SUBSCRIBER_ID";
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
	 * @param apiIdentifier
	 */
	private void removeAPIScope(APIIdentifier apiIdentifier) throws APIManagementException {
		Set<Scope> scopes = getAPIScopes(apiIdentifier);

		Connection connection = null;
		PreparedStatement prepStmt = null;
		int scopeId;
		int apiId = -1;

		String deleteAPIScopeQuery = "DELETE FROM AM_API_SCOPES WHERE API_ID = ?";
		String deleteOauth2ScopeQuery = "DELETE FROM IDN_OAUTH2_SCOPE  WHERE SCOPE_ID = ?";
		String deleteOauth2ResourceScopeQuery =
		                                        "DELETE FROM IDN_OAUTH2_RESOURCE_SCOPE  WHERE SCOPE_ID = ?";

		try {
			connection = APIMgtDBUtil.getConnection();
			connection.setAutoCommit(false);

			prepStmt = connection.prepareStatement(deleteAPIScopeQuery);
			prepStmt.setInt(1, apiId);
			prepStmt.execute();

			if (!scopes.isEmpty()) {
				Iterator<Scope> scopeItr = scopes.iterator();
				while (scopeItr.hasNext()) {
					scopeId = scopeItr.next().getId();

					prepStmt = connection.prepareStatement(deleteOauth2ResourceScopeQuery);
					prepStmt.setInt(1, scopeId);
					prepStmt.execute();

					prepStmt = connection.prepareStatement(deleteOauth2ScopeQuery);
					prepStmt.setInt(1, scopeId);
					prepStmt.execute();
				}
			}

            connection.commit();

		} catch (SQLException e) {
			handleException("Error while removing the scopes for the API: " +
			                        apiIdentifier.getApiName() + " from the database", e);
		} finally {
			APIMgtDBUtil.closeAllConnections(prepStmt, connection, null);
		}
	}

    /**
     * Delete a user subscription based on API_ID, APP_ID, TIER_ID
     *
     * @param apiId - subscriber API ID
     * @param appId - application ID used to subscribe
     * @param tier - subscribed TIER ID
     * @throws java.sql.SQLException - Letting the caller to handle the roll back
     */
    private void deleteSubscriptionByApiIDAndAppID(int apiId, int appId, Connection conn)
            throws SQLException {
        String deleteQuery = "DELETE FROM AM_SUBSCRIPTION " +
                             " WHERE " +
                             "API_ID = ? " +
                             "AND APPLICATION_ID = ? ";
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
     * @param apiName candidate api name
     * @param tenantDomain tenant domain name
     * @return true if the name is already available
     * @throws APIManagementException
     */
    public boolean isApiNameExist(String apiName, String tenantDomain) throws APIManagementException {
        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet resultSet = null;
        String contextParam = "/t/";

        String query = "SELECT COUNT(API_ID) AS API_COUNT FROM AM_API WHERE API_NAME = ? AND CONTEXT NOT LIKE ?";
        if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            query = "SELECT COUNT(API_ID) AS API_COUNT FROM AM_API WHERE API_NAME = ? AND CONTEXT LIKE ?";
            contextParam += tenantDomain + "/";
        }

        try {
            connection = APIMgtDBUtil.getConnection();

            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, apiName);
            prepStmt.setString(2, contextParam + "%");
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
            String sqlQuery = "SELECT IOAT.ACCESS_TOKEN" +
                    " FROM IDN_OAUTH2_ACCESS_TOKEN IOAT" +
                    " INNER JOIN IDN_OAUTH_CONSUMER_APPS IOCA ON IOCA.ID = IOAT.CONSUMER_KEY_ID" +
                    " WHERE IOCA.CONSUMER_KEY = ?" +
                    " AND IOAT.TOKEN_STATE = 'ACTIVE'";

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

        String query = "SELECT COUNT(SCOPE_ID) AS SCOPE_COUNT FROM IDN_OAUTH2_SCOPE WHERE SCOPE_KEY = ? AND TENANT_ID = ?";

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
     * @param scopeKey candidate scope key
     * @param tenantId tenant id
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

        String apiScopeQuery = "SELECT API.API_ID, API.API_NAME, API.API_PROVIDER "
                                   + "FROM AM_API API, IDN_OAUTH2_SCOPE IDN, AM_API_SCOPES AMS "
                                   + "WHERE IDN.SCOPE_ID=AMS.SCOPE_ID AND "
                                   + "AMS.API_ID=API.API_ID AND "
                                   + "IDN.SCOPE_KEY = ? AND "
                                   + "IDN.tenant_id = ?";
        String getApiQuery =
                             "SELECT API_ID FROM AM_API API WHERE API_PROVIDER = ? AND "
                                     + "API_NAME = ? AND API_VERSION = ?";

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
                    if(apiID != resultSet2.getInt("API_ID")){
                        //Check if the provider name and api name is same.
                        if(provider.equals(APIUtil.replaceEmailDomainBack(identifier.getProviderName()))
                                && apiName.equals(identifier.getApiName())){

                            //Return false since this means we're attaching the scope to another version of the API.
                            return false;
                        }
                        return true;
                    }
                    else{
                        return false;
                    }
                }

            }

        } catch (SQLException e) {
            handleException("Failed to check Scope Key availability : " + scopeKey, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, resultSet);
            APIMgtDBUtil.closeAllConnections(prepStmt2, null, resultSet2);
        }
        return false;
    }

    public boolean isDuplicateContextTemplate(String contextTemplate) throws APIManagementException {
        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        try {
            conn = APIMgtDBUtil.getConnection();

            String sqlQuery = "SELECT COUNT(CONTEXT_TEMPLATE) AS CTX_COUNT FROM AM_API WHERE CONTEXT_TEMPLATE = ?";

            ps = conn.prepareStatement(sqlQuery);
            ps.setString(1, contextTemplate);
            resultSet = ps.executeQuery();
            resultSet.next();
            int count = resultSet.getInt("CTX_COUNT");
            return count > 0;
        } catch (SQLException e) {
            handleException("Failed to count contexts which match " + contextTemplate, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, resultSet);
        }
        return false;
    }

    /**
     * @param consumerKey
     * @return
     */
    public static boolean isMappingExistsforConsumerKey(String consumerKey) throws APIManagementException {
        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;

        try {
            conn = APIMgtDBUtil.getConnection();

            String sqlQuery = "SELECT APPLICATION_ID " +
                              " FROM AM_APPLICATION_KEY_MAPPING " +
                              " WHERE CONSUMER_KEY   = ?";

            ps = conn.prepareStatement(sqlQuery);
            ps.setString(1, consumerKey);

            resultSet = ps.executeQuery();

            while (resultSet.next()) {
                String applicationId = resultSet.getString("APPLICATION_ID");
                return (applicationId != null && !applicationId.isEmpty());
            }

        } catch (SQLException e) {
            handleException("Failed to get Application ID by consumerKey "
                    , e);
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
    public static String getConsumerkeyByApplicationIdAndKeyType(String applicationId, String keyType)
            throws APIManagementException {
        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        String consumerKey = null;
        try {
            conn = APIMgtDBUtil.getConnection();

            String sqlQuery = "SELECT CONSUMER_KEY " +
                    " FROM AM_APPLICATION_KEY_MAPPING " +
                    " WHERE APPLICATION_ID   = ? AND KEY_TYPE = ? ";

            ps = conn.prepareStatement(sqlQuery);
            ps.setString(1, applicationId);
            ps.setString(2, keyType);
            resultSet = ps.executeQuery();

            while (resultSet.next()) {
                consumerKey = resultSet.getString("CONSUMER_KEY");
            }

        } catch (SQLException e) {
            handleException("Failed to get consumer key by applicationId " + applicationId + "and keyType " + keyType
                    , e);
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
    public static String getLastPublishedAPIVersionFromAPIStore(APIIdentifier apiIdentifier,String storeName)
            throws APIManagementException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection conn = null;
        String version = null;
        try {
            conn = APIMgtDBUtil.getConnection();
            String sqlQuery =
                    "SELECT API.API_VERSION FROM AM_API API , AM_EXTERNAL_STORES  ES WHERE ES.API_ID = " +
                    "API.API_ID and API.API_PROVIDER = ? and API.API_NAME=? and ES.STORE_ID =? ORDER By API.CREATED_TIME ASC";
            ps = conn.prepareStatement(sqlQuery);
            ps.setString(1, apiIdentifier.getProviderName());
            ps.setString(2,apiIdentifier.getApiName());
            ps.setString(3,storeName);
            rs = ps.executeQuery();
            while (rs.next()) {
                version = rs.getString("API_VERSION");
            }
        } catch (SQLException e) {
            handleException("Error while getting External APIStore details from the database for  the API : " +
                            apiIdentifier.getApiName() + "-" + apiIdentifier.getVersion(), e);

        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
        return version;
    }

    private String getScopeString(List<String> scopes){
        return StringUtils.join(scopes," ");
    }

    /**
     * Find all active access tokens of a given user.
     * @param username - Username of the user
     * @return - The set of active access tokens of the user.
     */
    public Set<String> getActiveAccessTokensOfUser(String username) throws APIManagementException {

        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;

        Set<String> tokens = null;

        String accessTokenStoreTable = APIConstants.ACCESS_TOKEN_STORE_TABLE;
        if (APIUtil.checkAccessTokenPartitioningEnabled() &&
                APIUtil.checkUserNameAssertionEnabled()) {
            accessTokenStoreTable = APIUtil.getAccessTokenStoreTableFromUserId(username);
        }

        int tenantId = IdentityTenantUtil.getTenantIdOfUser(username);

        String userStoreDomain = IdentityUtil.extractDomainFromName(username).toUpperCase();
        if(StringUtils.isEmpty(userStoreDomain)){
            userStoreDomain = IdentityUtil.getPrimaryDomainName();
        }
        else{
            //IdentityUtil doesn't have a function to remove the domain name from the username. Using the UserCoreUtil.
            username = UserCoreUtil.removeDomainFromName(username);
        }

        try {
            conn = APIMgtDBUtil.getConnection();
            String sqlQuery = "SELECT IOAT.ACCESS_TOKEN" +
                    " FROM " + accessTokenStoreTable + " IOAT" +
                    " WHERE IOAT.AUTHZ_USER = ?" +
                        " AND IOAT.TENANT_ID = ?" +
                        " AND IOAT.TOKEN_STATE = 'ACTIVE'" +
                        " AND LOWER(IOAT.USER_DOMAIN) = ?";

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


}

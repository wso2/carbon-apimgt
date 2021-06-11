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

import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIMgtResourceNotFoundException;
import org.wso2.carbon.apimgt.api.BlockConditionAlreadyExistsException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.SubscriptionAlreadyExistingException;
import org.wso2.carbon.apimgt.api.SubscriptionBlockedException;
import org.wso2.carbon.apimgt.api.dto.ConditionDTO;
import org.wso2.carbon.apimgt.api.dto.ConditionGroupDTO;
import org.wso2.carbon.apimgt.api.dto.KeyManagerConfigurationDTO;
import org.wso2.carbon.apimgt.api.dto.UserApplicationAPIUsage;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APICategory;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIKey;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.APIProductIdentifier;
import org.wso2.carbon.apimgt.api.model.APIProductResource;
import org.wso2.carbon.apimgt.api.model.APIStatus;
import org.wso2.carbon.apimgt.api.model.APIStore;
import org.wso2.carbon.apimgt.api.model.AccessTokenInfo;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.BlockConditionsDTO;
import org.wso2.carbon.apimgt.api.model.Comment;
import org.wso2.carbon.apimgt.api.model.Identifier;
import org.wso2.carbon.apimgt.api.model.KeyManager;
import org.wso2.carbon.apimgt.api.model.Label;
import org.wso2.carbon.apimgt.api.model.LifeCycleEvent;
import org.wso2.carbon.apimgt.api.model.MonetizationUsagePublishInfo;
import org.wso2.carbon.apimgt.api.model.OAuthAppRequest;
import org.wso2.carbon.apimgt.api.model.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.api.model.ResourcePath;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.SharedScopeUsage;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.api.model.Workflow;
import org.wso2.carbon.apimgt.api.model.botDataAPI.BotDetectionData;
import org.wso2.carbon.apimgt.api.model.graphql.queryanalysis.CustomComplexityDetails;
import org.wso2.carbon.apimgt.api.model.graphql.queryanalysis.GraphqlComplexityInfo;
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
import org.wso2.carbon.apimgt.impl.alertmgt.AlertMgtConstants;
import org.wso2.carbon.apimgt.impl.dao.constants.SQLConstants;
import org.wso2.carbon.apimgt.impl.dao.constants.SQLConstants.ThrottleSQLConstants;
import org.wso2.carbon.apimgt.impl.dto.APIInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.APIKeyInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.APISubscriptionInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.ApplicationRegistrationWorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.TierPermissionDTO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.wso2.carbon.apimgt.impl.factory.SQLConstantManagerFactory;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.notifier.events.SubscriptionEvent;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.utils.ApplicationUtils;
import org.wso2.carbon.apimgt.impl.utils.RemoteUserManagerClient;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowConstants;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowExecutorFactory;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowStatus;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.DBUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class represent the ApiMgtDAO.
 */
public class ApiMgtDAO {
    private static final Log log = LogFactory.getLog(ApiMgtDAO.class);
    private static ApiMgtDAO INSTANCE = null;

    private boolean forceCaseInsensitiveComparisons = false;
    private boolean multiGroupAppSharingEnabled = false;
    private static boolean initialAutoCommit = false;

    private final Object scopeMutex = new Object();

    private ApiMgtDAO() {
        APIManagerConfiguration configuration = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfigurationService().getAPIManagerConfiguration();

        String caseSensitiveComparison = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration().getFirstProperty(APIConstants.API_STORE_FORCE_CI_COMPARISIONS);
        if (caseSensitiveComparison != null) {
            forceCaseInsensitiveComparisons = Boolean.parseBoolean(caseSensitiveComparison);
        }

        multiGroupAppSharingEnabled = APIUtil.isMultiGroupAppSharingEnabled();
    }

    public List<String> getAPIVersionsMatchingApiName(String apiName, String username) throws APIManagementException {
        Connection conn = null;
        PreparedStatement ps = null;
        List<String> versionList = new ArrayList<String>();
        ResultSet resultSet = null;

        String sqlQuery = SQLConstants.GET_VERSIONS_MATCHES_API_NAME_SQL;
        try {
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            ps.setString(1, apiName);
            ps.setString(2, username);
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
     * Method to get the instance of the ApiMgtDAO.
     *
     * @return {@link ApiMgtDAO} instance
     */
    public static ApiMgtDAO getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ApiMgtDAO();
        }

        return INSTANCE;
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
        ResultSet resultSet = null;

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
            queryPs.setString(4, dto.getKeyManager());
            resultSet = queryPs.executeQuery();

            if (resultSet.next()) {
                throw new APIManagementException("Application '" + application.getName() + "' is already registered.",
                        ExceptionCodes.APPLICATION_ALREADY_REGISTERED);
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
                appRegPs.setString(9, dto.getKeyManager());
                appRegPs.execute();
            }

            ps = conn.prepareStatement(keyMappingEntry);
            ps.setInt(1, application.getId());
            ps.setString(2, dto.getKeyType());
            ps.setString(3, dto.getStatus().toString());
            ps.setString(4, dto.getKeyManager());
            ps.setString(5,UUID.randomUUID().toString());
            ps.setString(6, APIConstants.OAuthAppMode.CREATED.name());
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
            APIMgtDBUtil.closeAllConnections(ps, conn, resultSet);
        }
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




    private boolean isAnyPolicyContentAware(Connection conn, String apiPolicy, String appPolicy,
                                            String subPolicy, int subscriptionTenantId, int appTenantId, int apiId) throws APIManagementException {
        boolean isAnyContentAware = false;
        // only check if using CEP based throttling.
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        String sqlQuery = SQLConstants.ThrottleSQLConstants.IS_ANY_POLICY_CONTENT_AWARE_SQL;

        try {
            String dbProdName = conn.getMetaData().getDatabaseProductName();
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
                if (count > 0) {
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

            Timestamp timestamp = new Timestamp(subscriber.getSubscribedDate().getTime());
            ps.setTimestamp(4, timestamp);
            ps.setString(5, subscriber.getName());
            ps.setTimestamp(6, timestamp);
            ps.setTimestamp(7, timestamp);
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

    /**
     * Get subscriber name using subscription ID
     *
     * @param subscriptionId
     * @return subscriber name
     * @throws APIManagementException
     */
    public String getSubscriberName(String subscriptionId) throws APIManagementException {
        int subscriberId = getSubscriberIdBySubscriptionUUID(subscriptionId);
        Subscriber subscriber = getSubscriber(subscriberId);
        if (subscriber != null) {
            return subscriber.getName();
        }
        return StringUtils.EMPTY;
    }

    /**
     * Get subscriber ID using subscription ID
     *
     * @param subscriptionId
     * @return subscriber ID
     * @throws APIManagementException
     */
    private int getSubscriberIdBySubscriptionUUID(String subscriptionId) throws APIManagementException {
        int subscirberId = 0;
        String query = SQLConstants.GET_SUBSCRIBER_ID_BY_SUBSCRIPTION_UUID_SQL;
        try (Connection connection = APIMgtDBUtil.getConnection();
            PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, subscriptionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    subscirberId = rs.getInt(APIConstants.APPLICATION_SUBSCRIBER_ID);
                }
            }
        } catch (SQLException e) {
            handleException("Error while retrieving Subscriber ID: ", e);
        }
        return subscirberId;
    }

    /**
     * Derives info about monetization usage publish job
     *
     * @return ifno about the monetization usage publish job
     * @throws APIManagementException
     */
    public MonetizationUsagePublishInfo getMonetizationUsagePublishInfo() throws APIManagementException {

        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            conn = APIMgtDBUtil.getConnection();
            String query = SQLConstants.GET_MONETIZATION_USAGE_PUBLISH_INFO;
            ps = conn.prepareStatement(query);
            rs = ps.executeQuery();
            if (rs.next()) {
                MonetizationUsagePublishInfo monetizationUsagePublishInfo = new MonetizationUsagePublishInfo();
                monetizationUsagePublishInfo.setId(rs.getString("ID"));
                monetizationUsagePublishInfo.setState(rs.getString("STATE"));
                monetizationUsagePublishInfo.setStatus(rs.getString("STATUS"));
                monetizationUsagePublishInfo.setStartedTime(rs.getLong("STARTED_TIME"));
                monetizationUsagePublishInfo.setLastPublishTime(rs.getLong("PUBLISHED_TIME"));
                return monetizationUsagePublishInfo;
            }
        } catch (SQLException e) {
            handleException("Error while retrieving Monetization Usage Publish Info: ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
        return null;
    }

    /**
     * Add info about monetization usage publish job
     *
     * @throws APIManagementException
     */
    public void addMonetizationUsagePublishInfo(MonetizationUsagePublishInfo monetizationUsagePublishInfo)
            throws APIManagementException {

        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);
            String query = SQLConstants.ADD_MONETIZATION_USAGE_PUBLISH_INFO;
            ps = conn.prepareStatement(query);

            ps.setString(1, monetizationUsagePublishInfo.getId());
            ps.setString(2, monetizationUsagePublishInfo.getState());
            ps.setString(3, monetizationUsagePublishInfo.getStatus());
            ps.setString(4, Long.toString(monetizationUsagePublishInfo.getStartedTime()));
            ps.setString(5, Long.toString(monetizationUsagePublishInfo.getLastPublishTime()));
            ps.executeUpdate();

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    log.error("Error while rolling back the failed operation", ex);
                }
            }
            handleException("Error while adding monetization usage publish Info: ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
    }

    /**
     * Updates info about monetization usage publish job
     *
     * @throws APIManagementException
     */
    public void updateUsagePublishInfo(MonetizationUsagePublishInfo monetizationUsagePublishInfo)
                throws APIManagementException {

        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);
            String query = SQLConstants.UPDATE_MONETIZATION_USAGE_PUBLISH_INFO;
            ps = conn.prepareStatement(query);

            ps.setString(1, monetizationUsagePublishInfo.getState());
            ps.setString(2, monetizationUsagePublishInfo.getStatus());
            ps.setLong(3, monetizationUsagePublishInfo.getStartedTime());
            ps.setLong(4, monetizationUsagePublishInfo.getLastPublishTime());
            ps.setString(5, monetizationUsagePublishInfo.getId());
            ps.executeUpdate();

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    log.error("Error while rolling back the failed operation", ex);
                }
            }
            handleException("Error while updating monetization usage publish Info: " + e.getMessage(), e);
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

    public int addSubscription(ApiTypeWrapper apiTypeWrapper, int applicationId, String status, String subscriber)
            throws APIManagementException {
        Connection conn = null;
        final boolean isProduct = apiTypeWrapper.isAPIProduct();
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        PreparedStatement preparedStForInsert = null;
        ResultSet rs = null;
        int subscriptionId = -1;
        int id = -1;

        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);

            Identifier identifier;

            //Query to check if this subscription already exists
            String checkDuplicateQuery = SQLConstants.CHECK_EXISTING_SUBSCRIPTION_API_SQL;
            if (!isProduct) {
                identifier = apiTypeWrapper.getApi().getId();
                id = getAPIID(apiTypeWrapper.getApi().getId(), conn);
            } else  {
                identifier = apiTypeWrapper.getApiProduct().getId();
                id = apiTypeWrapper.getApiProduct().getProductId();
            }
            ps = conn.prepareStatement(checkDuplicateQuery);
            ps.setInt(1, id);
            ps.setInt(2, applicationId);

            resultSet = ps.executeQuery();
            int tenantId = APIUtil.getTenantId(APIUtil.replaceEmailDomainBack(identifier.getProviderName()));
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
                    log.error("Subscription already exists for API/API Prouct " + apiTypeWrapper.getName() + " in Application " +
                            applicationName);
                    throw new SubscriptionAlreadyExistingException("Subscription already exists for API/API Prouct " +
                            apiTypeWrapper.getName() + " in Application " +
                            applicationName);

                } else if (APIConstants.SubscriptionStatus.UNBLOCKED.equals(subStatus) && APIConstants
                        .SubscriptionCreatedStatus.UN_SUBSCRIBE.equals(subCreationStatus)) {
                    deleteSubscriptionByApiIDAndAppID(id, applicationId, conn);
                } else if (APIConstants.SubscriptionStatus.BLOCKED.equals(subStatus) || APIConstants
                        .SubscriptionStatus.PROD_ONLY_BLOCKED.equals(subStatus)) {
                    log.error("Subscription to API/API Prouct " + apiTypeWrapper.getName() + " through application " +
                            applicationName + " was blocked");
                    throw new SubscriptionBlockedException("Subscription to API/API Product " + apiTypeWrapper.getName() + " through " +
                            "application " + applicationName + " was blocked");
                } else if (APIConstants.SubscriptionStatus.REJECTED.equals(subStatus)) {
                    throw new SubscriptionBlockedException("Subscription to API " + apiTypeWrapper.getName()
                            + " through application " + applicationName + " was rejected");
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
            String tier;
            if (!isProduct) {
                tier = apiTypeWrapper.getApi().getId().getTier();
                preparedStForInsert.setString(1, tier);
                preparedStForInsert.setString(10, tier);
            } else {
                tier = apiTypeWrapper.getApiProduct().getId().getTier();
                preparedStForInsert.setString(1, tier);
                preparedStForInsert.setString(10, tier);
            }
            preparedStForInsert.setInt(2, id);
            preparedStForInsert.setInt(3, applicationId);
            preparedStForInsert.setString(4, status != null ? status : APIConstants.SubscriptionStatus.UNBLOCKED);
            preparedStForInsert.setString(5, APIConstants.SubscriptionCreatedStatus.SUBSCRIBE);
            preparedStForInsert.setString(6, subscriber);

            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            preparedStForInsert.setTimestamp(7, timestamp);
            preparedStForInsert.setTimestamp(8, timestamp);
            preparedStForInsert.setString(9, UUID.randomUUID().toString());

            preparedStForInsert.executeUpdate();
            rs = preparedStForInsert.getGeneratedKeys();
            while (rs.next()) {
                //subscriptionId = rs.getInt(1);
                subscriptionId = Integer.parseInt(rs.getString(1));
            }

            // finally commit transaction
            conn.commit();
            String tenantDomain = MultitenantUtils
                    .getTenantDomain(APIUtil.replaceEmailDomainBack(identifier.getProviderName()));
            SubscriptionEvent subscriptionEvent = new SubscriptionEvent(UUID.randomUUID().toString(),
                    System.currentTimeMillis(), APIConstants.EventType.SUBSCRIPTIONS_CREATE.name(),
                    tenantId, tenantDomain , subscriptionId,id, applicationId, tier,
                    (status != null ? status : APIConstants.SubscriptionStatus.UNBLOCKED));
            APIUtil.sendNotification(subscriptionEvent, APIConstants.NotifierType.SUBSCRIPTIONS.name());
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

    public int updateSubscription(ApiTypeWrapper apiTypeWrapper, String inputSubscriptionUUId, String status,
                                  String requestedThrottlingTier) throws APIManagementException {
        Connection conn = null;
        final boolean isProduct = apiTypeWrapper.isAPIProduct();
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        PreparedStatement preparedStForUpdate = null;
        int subscriptionId = -1;

        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);

            //Query to retrieve subscription id
            String retrieveSubscriptionIDQuery = SQLConstants.RETRIEVE_SUBSCRIPTION_ID_SQL;
            ps = conn.prepareStatement(retrieveSubscriptionIDQuery);
            ps.setString(1, inputSubscriptionUUId);
            resultSet = ps.executeQuery();
            if (resultSet.next()) {
                subscriptionId = resultSet.getInt(1);
            }

            //This query to update the AM_SUBSCRIPTION table
            String sqlQuery = SQLConstants.UPDATE_SINGLE_SUBSCRIPTION_SQL;
            preparedStForUpdate = conn.prepareStatement(sqlQuery);
            preparedStForUpdate.setString(1, requestedThrottlingTier);
            preparedStForUpdate.setString(2, status);
            preparedStForUpdate.setString(3, inputSubscriptionUUId);
            preparedStForUpdate.executeUpdate();

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
            APIMgtDBUtil.closeAllConnections(ps, conn, resultSet);
            APIMgtDBUtil.closeAllConnections(preparedStForUpdate, null, null);
        }
        return subscriptionId;
    }

    /**
     * Removes the subscription entry from AM_SUBSCRIPTIONS for identifier.
     *
     * @param identifier    Identifier
     * @param applicationId ID of the application which has the subscription
     * @throws APIManagementException
     */
    public void removeSubscription(Identifier identifier, int applicationId)
            throws APIManagementException {
        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        int id = -1;
        String uuid;
        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);
            String subscriptionUUIDQuery = SQLConstants.GET_SUBSCRIPTION_UUID_SQL;
            if(identifier instanceof APIIdentifier) {
                id = getAPIID((APIIdentifier) identifier, conn);
            } else if (identifier instanceof APIProductIdentifier) {
                id = ((APIProductIdentifier) identifier).getProductId();
            }

            ps = conn.prepareStatement(subscriptionUUIDQuery);
            ps.setInt(1, id);
            ps.setInt(2, applicationId);
            resultSet = ps.executeQuery();

            if (resultSet.next()) {
                uuid = resultSet.getString("UUID");
                SubscribedAPI subscribedAPI = new SubscribedAPI(uuid);
                removeSubscription(subscribedAPI, conn);
            } else {
                throw new APIManagementException("UUID does not exist for the given apiId:" + id + " and " +
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
            APIMgtDBUtil.closeAllConnections(ps, conn, resultSet);
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
                int applicationId = resultSet.getInt("APPLICATION_ID");
                Application application = getLightweightApplicationById(applicationId);
                if (APIConstants.API_PRODUCT.equals(resultSet.getString("API_TYPE"))) {
                    APIProductIdentifier apiProductIdentifier = new APIProductIdentifier(
                            APIUtil.replaceEmailDomain(resultSet.getString("API_PROVIDER")),
                            resultSet.getString("API_NAME"), resultSet.getString("API_VERSION"));
                    apiProductIdentifier.setProductId(resultSet.getInt("API_ID"));
                    subscribedAPI = new SubscribedAPI(application.getSubscriber(), apiProductIdentifier);
                } else {
                    APIIdentifier apiIdentifier = new APIIdentifier(
                            APIUtil.replaceEmailDomain(resultSet.getString("API_PROVIDER")),
                            resultSet.getString("API_NAME"), resultSet.getString("API_VERSION"));
                    apiIdentifier.setId(resultSet.getInt("API_ID"));
                    subscribedAPI = new SubscribedAPI(application.getSubscriber(), apiIdentifier);
                }
                subscribedAPI.setSubscriptionId(resultSet.getInt("SUBSCRIPTION_ID"));
                subscribedAPI.setSubStatus(resultSet.getString("SUB_STATUS"));
                subscribedAPI.setSubCreatedStatus(resultSet.getString("SUBS_CREATE_STATE"));
                subscribedAPI.setTier(new Tier(resultSet.getString("TIER_ID")));
                subscribedAPI.setRequestedTier(new Tier(resultSet.getString("TIER_ID_PENDING")));
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

                int applicationId = resultSet.getInt("APPLICATION_ID");
                Application application = getApplicationById(applicationId);

                if (APIConstants.API_PRODUCT.equals(resultSet.getString("API_TYPE"))) {
                    APIProductIdentifier apiProductIdentifier = new APIProductIdentifier(
                            APIUtil.replaceEmailDomain(resultSet.getString("API_PROVIDER")),
                            resultSet.getString("API_NAME"), resultSet.getString("API_VERSION"));
                    apiProductIdentifier.setProductId(resultSet.getInt("API_ID"));
                    subscribedAPI = new SubscribedAPI(application.getSubscriber(), apiProductIdentifier);
                } else {
                    APIIdentifier apiIdentifier = new APIIdentifier(
                            APIUtil.replaceEmailDomain(resultSet.getString("API_PROVIDER")),
                            resultSet.getString("API_NAME"), resultSet.getString("API_VERSION"));
                    apiIdentifier.setId(resultSet.getInt("API_ID"));
                    subscribedAPI = new SubscribedAPI(application.getSubscriber(), apiIdentifier);
                }

                subscribedAPI.setUUID(resultSet.getString("UUID"));
                subscribedAPI.setSubscriptionId(resultSet.getInt("SUBSCRIPTION_ID"));
                subscribedAPI.setSubStatus(resultSet.getString("SUB_STATUS"));
                subscribedAPI.setSubCreatedStatus(resultSet.getString("SUBS_CREATE_STATE"));
                subscribedAPI.setTier(new Tier(resultSet.getString("TIER_ID")));
                subscribedAPI.setRequestedTier(new Tier(resultSet.getString("TIER_ID_PENDING")));

                Timestamp createdTime = resultSet.getTimestamp("CREATED_TIME");
                subscribedAPI.setCreatedTime(createdTime == null ? null : String.valueOf(createdTime.getTime()));
                try {
                    Timestamp updated_time = resultSet.getTimestamp("UPDATED_TIME");
                    subscribedAPI.setUpdatedTime(
                            updated_time == null ? null : String.valueOf(updated_time.getTime()));
                } catch (SQLException e) {
                    // fixing Timestamp issue with default value '0000-00-00 00:00:00'for existing applications created
                    subscribedAPI.setUpdatedTime(subscribedAPI.getCreatedTime());
                }
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

        String whereClauseWithGroupId = " AND (APP.GROUP_ID = ? OR ((APP.GROUP_ID='' OR APP.GROUP_ID IS NULL)"
                + " AND SUB.USER_ID = ?))";
        String whereClauseWithGroupIdorceCaseInsensitiveComp = " AND (APP.GROUP_ID = ?"
                + " OR ((APP.GROUP_ID='' OR APP.GROUP_ID IS NULL) AND LOWER(SUB.USER_ID) = LOWER(?)))";
        String whereClause = " AND SUB.USER_ID = ? ";
        String whereClauseCaseSensitive = " AND LOWER(SUB.USER_ID) = LOWER(?) ";

        String whereClauseWithMultiGroupId = " AND  ( (APP.APPLICATION_ID IN (SELECT APPLICATION_ID FROM " +
                "AM_APPLICATION_GROUP_MAPPING WHERE GROUP_ID IN ($params)  AND TENANT = ?))  OR  ( SUB.USER_ID = ? ))";
        String whereClauseWithMultiGroupIdCaseInsensitive = " AND  ( (APP.APPLICATION_ID IN  (SELECT APPLICATION_ID " +
                "FROM AM_APPLICATION_GROUP_MAPPING  WHERE GROUP_ID IN ($params) AND TENANT = ?))  OR  ( LOWER(SUB.USER_ID) = LOWER" +
                "(?) ))";

        try {
            connection = APIMgtDBUtil.getConnection();
            if (groupingId != null && !"null".equals(groupingId) && !groupingId.isEmpty()) {

                if (multiGroupAppSharingEnabled) {
                    if (forceCaseInsensitiveComparisons) {
                        sqlQuery += whereClauseWithMultiGroupIdCaseInsensitive;
                    } else {
                        sqlQuery += whereClauseWithMultiGroupId;
                    }
                    String tenantDomain = MultitenantUtils.getTenantDomain(subscriber.getName());
                    String groupIdArr[] = groupingId.split(",");

                    ps = fillQueryParams(connection, sqlQuery, groupIdArr, 3);
                    int tenantId = APIUtil.getTenantId(subscriber.getName());
                    ps.setInt(1, tenantId);
                    ps.setString(2, applicationName);
                    int paramIndex = groupIdArr.length + 2;
                    ps.setString(++paramIndex, tenantDomain);
                    ps.setString(++paramIndex, subscriber.getName());
                } else {
                    if (forceCaseInsensitiveComparisons) {
                        sqlQuery += whereClauseWithGroupIdorceCaseInsensitiveComp;
                    } else {
                        sqlQuery += whereClauseWithGroupId;
                    }
                    ps = connection.prepareStatement(sqlQuery);
                    int tenantId = APIUtil.getTenantId(subscriber.getName());
                    ps.setInt(1, tenantId);
                    ps.setString(2, applicationName);
                    ps.setString(3, groupingId);
                    ps.setString(4, subscriber.getName());
                }
            } else {
                if (forceCaseInsensitiveComparisons) {
                    sqlQuery += whereClauseCaseSensitive;
                } else {
                    sqlQuery += whereClause;
                }
                ps = connection.prepareStatement(sqlQuery);
                int tenantId = APIUtil.getTenantId(subscriber.getName());
                ps.setInt(1, tenantId);
                ps.setString(2, applicationName);
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

    /**
     * This method returns the set of APIs for given subscriber, subscribed under the specified application.
     *
     * @param subscriber    subscriber
     * @param applicationId Application Id
     * @return Set<API>
     * @throws org.wso2.carbon.apimgt.api.APIManagementException if failed to get SubscribedAPIs
     */
    public Set<SubscribedAPI> getSubscribedAPIsByApplicationId(Subscriber subscriber, int applicationId, String groupingId)
            throws APIManagementException {
        Set<SubscribedAPI> subscribedAPIs = new LinkedHashSet<SubscribedAPI>();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet result = null;
        String sqlQuery = SQLConstants.GET_SUBSCRIBED_APIS_BY_ID_SQL;
        String whereClauseWithGroupId = " AND (APP.GROUP_ID = ? OR ((APP.GROUP_ID='' OR APP.GROUP_ID IS NULL)"
                + " AND SUB.USER_ID = ?))";
        String whereClauseWithGroupIdorceCaseInsensitiveComp = " AND (APP.GROUP_ID = ?"
                + " OR ((APP.GROUP_ID='' OR APP.GROUP_ID IS NULL) AND LOWER(SUB.USER_ID) = LOWER(?)))";
        String whereClause = " AND SUB.USER_ID = ? ";
        String whereClauseCaseSensitive = " AND LOWER(SUB.USER_ID) = LOWER(?) ";
        String whereClauseWithMultiGroupId = " AND  ( (APP.APPLICATION_ID IN (SELECT APPLICATION_ID FROM " +
                "AM_APPLICATION_GROUP_MAPPING WHERE GROUP_ID IN ($params)  AND TENANT = ?))  OR  ( SUB.USER_ID = ? ))";
        String whereClauseWithMultiGroupIdCaseInsensitive = " AND  ( (APP.APPLICATION_ID IN  (SELECT APPLICATION_ID " +
                "FROM AM_APPLICATION_GROUP_MAPPING  WHERE GROUP_ID IN ($params) AND TENANT = ?))  OR  ( LOWER(SUB.USER_ID) = LOWER" +
                "(?) ))";
        try {
            connection = APIMgtDBUtil.getConnection();
            if (groupingId != null && !"null".equals(groupingId) && !groupingId.isEmpty()) {
                if (multiGroupAppSharingEnabled) {
                    if (forceCaseInsensitiveComparisons) {
                        sqlQuery += whereClauseWithMultiGroupIdCaseInsensitive;
                    } else {
                        sqlQuery += whereClauseWithMultiGroupId;
                    }
                    String tenantDomain = MultitenantUtils.getTenantDomain(subscriber.getName());
                    String groupIdArr[] = groupingId.split(",");
                    ps = fillQueryParams(connection, sqlQuery, groupIdArr, 3);
                    int tenantId = APIUtil.getTenantId(subscriber.getName());
                    ps.setInt(1, tenantId);
                    ps.setInt(2, applicationId);
                    int paramIndex = groupIdArr.length + 2;
                    ps.setString(++paramIndex, tenantDomain);
                    ps.setString(++paramIndex, subscriber.getName());
                } else {
                    if (forceCaseInsensitiveComparisons) {
                        sqlQuery += whereClauseWithGroupIdorceCaseInsensitiveComp;
                    } else {
                        sqlQuery += whereClauseWithGroupId;
                    }
                    ps = connection.prepareStatement(sqlQuery);
                    int tenantId = APIUtil.getTenantId(subscriber.getName());
                    ps.setInt(1, tenantId);
                    ps.setInt(2, applicationId);
                    ps.setString(3, groupingId);
                    ps.setString(4, subscriber.getName());
                }
            } else {
                if (forceCaseInsensitiveComparisons) {
                    sqlQuery += whereClauseCaseSensitive;
                } else {
                    sqlQuery += whereClause;
                }
                ps = connection.prepareStatement(sqlQuery);
                int tenantId = APIUtil.getTenantId(subscriber.getName());
                ps.setInt(1, tenantId);
                ps.setInt(2, applicationId);
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
                application.setId(result.getInt("APP_ID"));
                application.setOwner(result.getString("OWNER"));
                application.setCallbackUrl(result.getString("CALLBACK_URL"));
                application.setUUID(result.getString("APP_UUID"));
                if (multiGroupAppSharingEnabled) {
                    application.setGroupId(getGroupId(application.getId()));
                }
                int subscriptionId = result.getInt("SUBS_ID");
                Set<APIKey> apiKeys = getAPIKeysBySubscription(subscriptionId);
                for (APIKey key : apiKeys) {
                    subscribedAPI.addKey(key);
                }
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

    public Set<String> getScopesForApplicationSubscription(Subscriber subscriber, int applicationId)
            throws APIManagementException {
        PreparedStatement getIncludedApisInProduct = null;
        PreparedStatement getSubscribedApisAndProducts = null;
        ResultSet resultSet = null;
        ResultSet resultSet1 = null;
        Set<String> scopeKeysSet = new HashSet<>();
        Set<Integer> apiIdSet = new HashSet<>();
        int tenantId = APIUtil.getTenantId(subscriber.getName());

        try (Connection conn = APIMgtDBUtil.getConnection()) {
            String sqlQueryForGetSubscribedApis = SQLConstants.GET_SUBSCRIBED_API_IDs_BY_APP_ID_SQL;
            getSubscribedApisAndProducts = conn.prepareStatement(sqlQueryForGetSubscribedApis);
            getSubscribedApisAndProducts.setInt(1, tenantId);
            getSubscribedApisAndProducts.setInt(2, applicationId);
            resultSet = getSubscribedApisAndProducts.executeQuery();
            while (resultSet.next()) {
                int apiId = resultSet.getInt("API_ID");
                String getIncludedApisInProductQuery = SQLConstants.GET_INCLUDED_APIS_IN_PRODUCT_SQL;
                getIncludedApisInProduct = conn.prepareStatement(getIncludedApisInProductQuery);
                getIncludedApisInProduct.setInt(1, apiId);
                resultSet1 = getIncludedApisInProduct.executeQuery();
                while (resultSet1.next()) {
                    int includedApiId = resultSet1.getInt("API_ID");
                    apiIdSet.add(includedApiId);
                }
                apiIdSet.add(apiId);
            }
            if (!apiIdSet.isEmpty()) {
                String apiIdList = StringUtils.join(apiIdSet, ", ");
                String sqlQuery = SQLConstants.GET_SCOPE_BY_SUBSCRIBED_API_PREFIX + apiIdList
                        + SQLConstants.GET_SCOPE_BY_SUBSCRIBED_ID_SUFFIX;

                if (conn.getMetaData().getDriverName().contains("Oracle")) {
                    sqlQuery = SQLConstants.GET_SCOPE_BY_SUBSCRIBED_ID_ORACLE_SQL + apiIdList
                            + SQLConstants.GET_SCOPE_BY_SUBSCRIBED_ID_SUFFIX;
                }
                try (PreparedStatement statement = conn.prepareStatement(sqlQuery)) {
                    try (ResultSet finalResultSet = statement.executeQuery()) {
                        while (finalResultSet.next()) {
                            scopeKeysSet.add(finalResultSet.getString(1));
                        }
                    }
                }
            }
        } catch (SQLException e) {
            handleException("Failed to retrieve scopes for application subscription ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(getSubscribedApisAndProducts, null, resultSet);
            APIMgtDBUtil.closeAllConnections(getIncludedApisInProduct, null, resultSet1);
        }
        return scopeKeysSet;
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

    public Integer getSubscriptionCount(Subscriber subscriber, String applicationName, String groupingId)
            throws APIManagementException {
        Integer subscriptionCount = 0;
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet result = null;
        int tenantId = APIUtil.getTenantId(subscriber.getName());

        try {
            connection = APIMgtDBUtil.getConnection();

            String sqlQuery = SQLConstants.GET_SUBSCRIPTION_COUNT_SQL;
            if (forceCaseInsensitiveComparisons) {
                sqlQuery = SQLConstants.GET_SUBSCRIPTION_COUNT_CASE_INSENSITIVE_SQL;
            }

            String whereClauseWithGroupId = " AND (APP.GROUP_ID = ? OR "
                    + "((APP.GROUP_ID = '' OR APP.GROUP_ID IS NULL) AND SUB.USER_ID = ?)) ";
            String whereClauseWithMultiGroupId = " AND  ( (APP.APPLICATION_ID IN (SELECT APPLICATION_ID  FROM " +
                    "AM_APPLICATION_GROUP_MAPPING WHERE GROUP_ID IN ($params) AND TENANT = ?))  OR  ( SUB.USER_ID = ? ))";
            String whereClauseWithUserId = " AND SUB.USER_ID = ? ";
            String whereClauseCaseSensitive = " AND LOWER(SUB.USER_ID) = LOWER(?) ";
            String appIdentifier;

            boolean hasGrouping = false;
            if (groupingId != null && !"null".equals(groupingId) && !groupingId.isEmpty()) {
                if (multiGroupAppSharingEnabled) {
                    String tenantDomain = MultitenantUtils.getTenantDomain(subscriber.getName());
                    sqlQuery += whereClauseWithMultiGroupId;
                    String[] groupIdArr = groupingId.split(",");

                    ps = fillQueryParams(connection, sqlQuery, groupIdArr, 3);
                    ps.setString(1, applicationName);
                    ps.setInt(2, tenantId);
                    int paramIndex = groupIdArr.length + 2;
                    ps.setString(++paramIndex, tenantDomain);
                    ps.setString(++paramIndex, subscriber.getName());
                } else {
                    sqlQuery += whereClauseWithGroupId;
                    ps = connection.prepareStatement(sqlQuery);
                    ps.setString(1, applicationName);
                    ps.setInt(2, tenantId);
                    ps.setString(3, groupingId);
                    ps.setString(4, subscriber.getName());
                }
            } else {
                if (forceCaseInsensitiveComparisons) {
                    sqlQuery += whereClauseCaseSensitive;
                } else {
                    sqlQuery += whereClauseWithUserId;
                }
                ps = connection.prepareStatement(sqlQuery);
                ps.setString(1, applicationName);
                ps.setInt(2, tenantId);
                ps.setString(3, subscriber.getName());
            }
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

    public Integer getSubscriptionCountByApplicationId(Subscriber subscriber, int applicationId, String groupingId)
            throws APIManagementException {
        Integer subscriptionCount = 0;
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet result = null;
        int tenantId = APIUtil.getTenantId(subscriber.getName());
        try {
            connection = APIMgtDBUtil.getConnection();
            String sqlQuery = SQLConstants.GET_SUBSCRIPTION_COUNT_BY_APP_ID_SQL;
            if (forceCaseInsensitiveComparisons) {
                sqlQuery = SQLConstants.GET_SUBSCRIPTION_COUNT_BY_APP_ID_CASE_INSENSITIVE_SQL;
            }
            String whereClauseWithGroupId = " AND (APP.GROUP_ID = ? OR "
                    + "((APP.GROUP_ID = '' OR APP.GROUP_ID IS NULL) AND SUB.USER_ID = ?)) ";
            String whereClauseWithMultiGroupId = " AND  ( (APP.APPLICATION_ID IN (SELECT APPLICATION_ID  FROM " +
                    "AM_APPLICATION_GROUP_MAPPING WHERE GROUP_ID IN ($params) AND TENANT = ?))  OR  ( SUB.USER_ID = ? ))";
            String whereClauseWithUserId = " AND SUB.USER_ID = ? ";
            String whereClauseCaseSensitive = " AND LOWER(SUB.USER_ID) = LOWER(?) ";
            String appIdentifier;
            boolean hasGrouping = false;
            if (groupingId != null && !"null".equals(groupingId) && !groupingId.isEmpty()) {
                if (multiGroupAppSharingEnabled) {
                    String tenantDomain = MultitenantUtils.getTenantDomain(subscriber.getName());
                    sqlQuery += whereClauseWithMultiGroupId;
                    String[] groupIdArr = groupingId.split(",");
                    ps = fillQueryParams(connection, sqlQuery, groupIdArr, 3);
                    ps.setInt(1, applicationId);
                    ps.setInt(2, tenantId);
                    int paramIndex = groupIdArr.length + 2;
                    ps.setString(++paramIndex, tenantDomain);
                    ps.setString(++paramIndex, subscriber.getName());
                } else {
                    sqlQuery += whereClauseWithGroupId;
                    ps = connection.prepareStatement(sqlQuery);
                    ps.setInt(1, applicationId);
                    ps.setInt(2, tenantId);
                    ps.setString(3, groupingId);
                    ps.setString(4, subscriber.getName());
                }
            } else {
                if (forceCaseInsensitiveComparisons) {
                    sqlQuery += whereClauseCaseSensitive;
                } else {
                    sqlQuery += whereClauseWithUserId;
                }
                ps = connection.prepareStatement(sqlQuery);
                ps.setInt(1, applicationId);
                ps.setInt(2, tenantId);
                ps.setString(3, subscriber.getName());
            }
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
        Set<SubscribedAPI> subscribedAPIs = new LinkedHashSet<>();
        String sqlQuery =
                appendSubscriptionQueryWhereClause(groupingId, SQLConstants.GET_PAGINATED_SUBSCRIBED_APIS_SQL);

        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(sqlQuery);
             ResultSet result = getSubscriptionResultSet(groupingId, subscriber, applicationName, ps)) {
            int index = 0;
            while (result.next()) {
                if (index >= startSubIndex && index < endSubIndex) {
                    String apiType = result.getString("TYPE");

                    if (APIConstants.API_PRODUCT.toString().equals(apiType)) {
                        APIProductIdentifier identifier = new APIProductIdentifier(
                                APIUtil.replaceEmailDomain(result.getString("API_PROVIDER")),
                                result.getString("API_NAME"), result.getString("API_VERSION"));

                        SubscribedAPI subscribedAPI = new SubscribedAPI(subscriber, identifier);
                        initSubscribedAPI(subscribedAPI, subscriber, result);
                        subscribedAPIs.add(subscribedAPI);
                    } else {
                        APIIdentifier identifier = new APIIdentifier(APIUtil.replaceEmailDomain(result.getString
                                ("API_PROVIDER")), result.getString("API_NAME"),
                                result.getString("API_VERSION"));

                        SubscribedAPI subscribedAPI = new SubscribedAPI(subscriber, identifier);
                        initSubscribedAPI(subscribedAPI, subscriber, result);
                        subscribedAPIs.add(subscribedAPI);
                    }

                    if (index == endSubIndex - 1) {
                        break;
                    }
                }
                index++;
            }
        } catch (SQLException e) {
            handleException("Failed to get SubscribedAPI of :" + subscriber.getName(), e);
        }

        return subscribedAPIs;
    }

    private String appendSubscriptionQueryWhereClause(final String groupingId, String sqlQuery) {
        if (groupingId != null && !"null".equals(groupingId) && !groupingId.isEmpty()) {
            if (multiGroupAppSharingEnabled) {
                String[] groupIDArray = groupingId.split(",");
                List<String> questionMarks = new ArrayList<>(Collections.nCopies(groupIDArray.length, "?"));
                final String paramString = String.join(",", questionMarks);

                if (forceCaseInsensitiveComparisons) {
                    sqlQuery += " AND  ( (APP.APPLICATION_ID IN  (SELECT APPLICATION_ID " +
                            " FROM AM_APPLICATION_GROUP_MAPPING  " +
                            " WHERE GROUP_ID IN (" + paramString + ") AND TENANT = ?))" +
                            "  OR  ( LOWER(SUB.USER_ID) = LOWER(?) ))";
                } else {
                    sqlQuery += " AND  ( (APP.APPLICATION_ID IN (SELECT APPLICATION_ID FROM " +
                            "AM_APPLICATION_GROUP_MAPPING WHERE GROUP_ID IN (" + paramString + ") AND TENANT = ?))  " +
                            "OR  ( SUB.USER_ID = ? ))";
                }
            } else {
                if (forceCaseInsensitiveComparisons) {
                    sqlQuery += " AND (APP.GROUP_ID = ? OR ((APP.GROUP_ID='' OR APP.GROUP_ID IS NULL)" +
                            " AND LOWER(SUB.USER_ID) = LOWER(?)))";
                } else {
                    sqlQuery += " AND (APP.GROUP_ID = ? OR ((APP.GROUP_ID='' OR APP.GROUP_ID IS NULL)" +
                            " AND SUB.USER_ID = ?))";
                }
            }
        } else {
            if (forceCaseInsensitiveComparisons) {
                sqlQuery += " AND LOWER(SUB.USER_ID) = LOWER(?)  ";
            } else {
                sqlQuery += " AND  SUB.USER_ID = ? ";
            }
        }

        return sqlQuery;
    }

    private ResultSet getSubscriptionResultSet(String groupingId, Subscriber subscriber, String applicationName,
                                          PreparedStatement statement) throws SQLException {
        int tenantId = APIUtil.getTenantId(subscriber.getName());
        int paramIndex = 0;

        if (groupingId != null && !"null".equals(groupingId) && !groupingId.isEmpty()) {
            if (multiGroupAppSharingEnabled) {
                String tenantDomain = MultitenantUtils.getTenantDomain(subscriber.getName());

                String[] groupIDArray = groupingId.split(",");

                statement.setInt(++paramIndex, tenantId);
                statement.setString(++paramIndex, applicationName);
                for (String groupId : groupIDArray) {
                    statement.setString(++paramIndex, groupId);
                }
                statement.setString(++paramIndex, tenantDomain);
                statement.setString(++paramIndex, subscriber.getName());
            } else {
                statement.setInt(++paramIndex, tenantId);
                statement.setString(++paramIndex, applicationName);
                statement.setString(++paramIndex, groupingId);
                statement.setString(++paramIndex, subscriber.getName());
            }
        } else {
            statement.setInt(++paramIndex, tenantId);
            statement.setString(++paramIndex, applicationName);
            statement.setString(++paramIndex, subscriber.getName());
        }

        return statement.executeQuery();
    }

    private void initSubscribedAPI(SubscribedAPI subscribedAPI, Subscriber subscriber, ResultSet resultSet)
            throws SQLException {
        subscribedAPI.setUUID(resultSet.getString("SUB_UUID"));
        subscribedAPI.setSubStatus(resultSet.getString("SUB_STATUS"));
        subscribedAPI.setSubCreatedStatus(resultSet.getString("SUBS_CREATE_STATE"));
        subscribedAPI.setTier(new Tier(resultSet.getString(APIConstants.SUBSCRIPTION_FIELD_TIER_ID)));
        subscribedAPI.setRequestedTier(new Tier(resultSet.getString("TIER_ID_PENDING")));

        Application application = new Application(resultSet.getString("APP_NAME"), subscriber);
        application.setUUID(resultSet.getString("APP_UUID"));
        subscribedAPI.setApplication(application);
    }

    /**
     * Gets the subscribed API's, by the group for the application.
     *
     * @param subscriber      the subscriber subscribing for the api
     * @param applicationId the application to which the api's are subscribed
     * @param startSubIndex   the start index for pagination
     * @param endSubIndex     end index for pagination
     * @param groupingId      the group id of the application
     * @return the set of subscribed API's.
     * @throws APIManagementException
     */
    public Set<SubscribedAPI> getPaginatedSubscribedAPIs(Subscriber subscriber, int applicationId,
            int startSubIndex, int endSubIndex, String groupingId)
            throws APIManagementException {
        Set<SubscribedAPI> subscribedAPIs = new LinkedHashSet<SubscribedAPI>();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet result = null;
        String sqlQuery = SQLConstants.GET_PAGINATED_SUBSCRIBED_APIS_BY_APP_ID_SQL;
        String whereClause = " AND  SUB.USER_ID = ? ";
        String whereClauseForceCaseInsensitiveComp = " AND LOWER(SUB.USER_ID) = LOWER(?)  ";
        String whereClauseWithGroupId = " AND (APP.GROUP_ID = ? OR ((APP.GROUP_ID='' OR APP.GROUP_ID IS NULL)"
                + " AND SUB.USER_ID = ?))";
        String whereClauseWithGroupIdorceCaseInsensitiveComp = " AND (APP.GROUP_ID = ?"
                + " OR ((APP.GROUP_ID='' OR APP.GROUP_ID IS NULL) AND LOWER(SUB.USER_ID) = LOWER(?)))";
        String whereClauseWithMultiGroupId = " AND  ( (APP.APPLICATION_ID IN (SELECT APPLICATION_ID FROM " +
                "AM_APPLICATION_GROUP_MAPPING WHERE GROUP_ID IN ($params) AND TENANT = ?))  OR  ( SUB.USER_ID = ? ))";
        String whereClauseWithMultiGroupIdCaseInsensitive = " AND  ( (APP.APPLICATION_ID IN  (SELECT APPLICATION_ID " +
                "FROM AM_APPLICATION_GROUP_MAPPING  WHERE GROUP_ID IN ($params) AND TENANT = ?))  OR  ( LOWER(SUB.USER_ID) = LOWER" +
                "(?) ))";
        try {
            connection = APIMgtDBUtil.getConnection();
            int tenantId = APIUtil.getTenantId(subscriber.getName());
            if (groupingId != null && !"null".equals(groupingId) && !groupingId.isEmpty()) {
                if (multiGroupAppSharingEnabled) {
                    String tenantDomain = MultitenantUtils.getTenantDomain(subscriber.getName());
                    if (forceCaseInsensitiveComparisons) {
                        sqlQuery += whereClauseWithMultiGroupIdCaseInsensitive;
                    } else {
                        sqlQuery += whereClauseWithMultiGroupId;
                    }
                    String groupIDArray[] = groupingId.split(",");
                    ps = fillQueryParams(connection, sqlQuery, groupIDArray, 3);
                    ps.setInt(1, tenantId);
                    ps.setInt(2, applicationId);
                    // dynamically seeting the parameter index
                    int paramIndex = groupIDArray.length + 2;
                    ps.setString(++paramIndex, tenantDomain);
                    ps.setString(++paramIndex, subscriber.getName());
                } else {
                    if (forceCaseInsensitiveComparisons) {
                        sqlQuery += whereClauseWithGroupIdorceCaseInsensitiveComp;
                    } else {
                        sqlQuery += whereClauseWithGroupId;
                    }
                    ps = connection.prepareStatement(sqlQuery);
                    ps.setInt(1, tenantId);
                    ps.setInt(2, applicationId);
                    ps.setString(3, groupingId);
                    ps.setString(4, subscriber.getName());
                }
            } else {
                if (forceCaseInsensitiveComparisons) {
                    sqlQuery += whereClauseForceCaseInsensitiveComp;
                } else {
                    sqlQuery += whereClause;
                }
                ps = connection.prepareStatement(sqlQuery);
                ps.setInt(1, tenantId);
                ps.setInt(2, applicationId);
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
        Set<SubscribedAPI> subscribedAPIs = new LinkedHashSet<>();

        //identify subscribeduser used email/ordinalusername
        String subscribedUserName = getLoginUserName(subscriber.getName());
        subscriber.setName(subscribedUserName);

        String sqlQuery =
                        appendSubscriptionQueryWhereClause(groupingId,
                                SQLConstants.GET_SUBSCRIBED_APIS_OF_SUBSCRIBER_SQL);

        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(sqlQuery);
             ResultSet result = getSubscriptionResultSet(groupingId, subscriber, ps)) {
            while (result.next()) {
                String apiType = result.getString("TYPE");

                if (APIConstants.API_PRODUCT.toString().equals(apiType)) {
                    APIProductIdentifier identifier =
                            new APIProductIdentifier(APIUtil.replaceEmailDomain(result.getString("API_PROVIDER")),
                                    result.getString("API_NAME"), result.getString("API_VERSION"));

                    SubscribedAPI subscribedAPI = new SubscribedAPI(subscriber, identifier);

                    initSubscribedAPIDetailed(subscribedAPI, subscriber, result);
                    subscribedAPIs.add(subscribedAPI);
                } else {
                    APIIdentifier identifier = new APIIdentifier(APIUtil.replaceEmailDomain(result.getString
                            ("API_PROVIDER")), result.getString("API_NAME"),
                            result.getString("API_VERSION"));
                    SubscribedAPI subscribedAPI = new SubscribedAPI(subscriber, identifier);

                    initSubscribedAPIDetailed(subscribedAPI, subscriber, result);
                    subscribedAPIs.add(subscribedAPI);
                }
            }
        } catch (SQLException e) {
            handleException("Failed to get SubscribedAPI of :" + subscriber.getName(), e);
        }

        return subscribedAPIs;
    }

    private ResultSet getSubscriptionResultSet(String groupingId, Subscriber subscriber,
                                               PreparedStatement statement) throws SQLException {
        int tenantId = APIUtil.getTenantId(subscriber.getName());
        int paramIndex = 0;

        if (groupingId != null && !"null".equals(groupingId) && !groupingId.isEmpty()) {
            if (multiGroupAppSharingEnabled) {
                String tenantDomain = MultitenantUtils.getTenantDomain(subscriber.getName());
                String[] groupIDArray = groupingId.split(",");

                statement.setInt(++paramIndex, tenantId);
                for (String groupId : groupIDArray) {
                    statement.setString(++paramIndex, groupId);
                }
                statement.setString(++paramIndex, tenantDomain);
                statement.setString(++paramIndex, subscriber.getName());

            } else {
                statement.setInt(++paramIndex, tenantId);
                statement.setString(++paramIndex, groupingId);
                statement.setString(++paramIndex, subscriber.getName());
            }
        } else {
            statement.setInt(++paramIndex, tenantId);
            statement.setString(++paramIndex, subscriber.getName());
        }

        return statement.executeQuery();
    }

    private void initSubscribedAPIDetailed(SubscribedAPI subscribedAPI, Subscriber subscriber, ResultSet result)
            throws SQLException, APIManagementException {
        subscribedAPI.setSubscriptionId(result.getInt("SUBS_ID"));
        subscribedAPI.setSubStatus(result.getString("SUB_STATUS"));
        subscribedAPI.setSubCreatedStatus(result.getString("SUBS_CREATE_STATE"));
        String tierName = result.getString(APIConstants.SUBSCRIPTION_FIELD_TIER_ID);
        String requestedTierName = result.getString(APIConstants.SUBSCRIPTION_FIELD_TIER_ID_PENDING);
        subscribedAPI.setTier(new Tier(tierName));
        subscribedAPI.setRequestedTier(new Tier(requestedTierName));
        subscribedAPI.setUUID(result.getString("SUB_UUID"));
        //setting NULL for subscriber. If needed, Subscriber object should be constructed &
        // passed in
        int applicationId = result.getInt("APP_ID");

        Application application = new Application(result.getString("APP_NAME"), subscriber);
        application.setId(result.getInt("APP_ID"));
        application.setTokenType(result.getString("APP_TOKEN_TYPE"));
        application.setCallbackUrl(result.getString("CALLBACK_URL"));
        application.setUUID(result.getString("APP_UUID"));

        if (multiGroupAppSharingEnabled) {
            application.setGroupId(getGroupId(application.getId()));
            application.setOwner(result.getString("OWNER"));
        }

        subscribedAPI.setApplication(application);
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


    private Map<String, Map<String, OAuthApplicationInfo>> getOAuthApplications(
            String tenantDomain, int applicationId) throws APIManagementException {
        Map<String, Map<String, OAuthApplicationInfo>> map = new HashMap<>();
        Map<String, OAuthApplicationInfo> prodApp = getClientOfApplication(tenantDomain, applicationId, "PRODUCTION");
        map.put("PRODUCTION", prodApp);

        Map<String, OAuthApplicationInfo> sandboxApp = getClientOfApplication(tenantDomain, applicationId, "SANDBOX");
        map.put("SANDBOX", sandboxApp);

        return map;
    }

    private Map<String, OAuthApplicationInfo> getClientOfApplication(String tenntDomain,
                                                                     int applicationID, String keyType)
            throws APIManagementException {
        String sqlQuery = SQLConstants.GET_CLIENT_OF_APPLICATION_SQL;
        Map<String,OAuthApplicationInfo> keyTypeWiseOAuthApps = new HashMap<>();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = APIMgtDBUtil.getConnection();
            ps = connection.prepareStatement(sqlQuery);
            ps.setInt(1, applicationID);
            ps.setString(2, keyType);
            rs = ps.executeQuery();

            while (rs.next()) {
                String consumerKey = rs.getString("CONSUMER_KEY");
                String keyManagerName = rs.getString("KEY_MANAGER");
                String createMode = rs.getString("CREATE_MODE");
                if (consumerKey != null) {
                    KeyManagerConfigurationDTO keyManagerConfiguration = getKeyManagerConfigurationByName(
                            tenntDomain, keyManagerName);
                    if (keyManagerConfiguration == null) {
                        keyManagerConfiguration = getKeyManagerConfigurationByUUID(keyManagerName);
                        if (keyManagerConfiguration != null) {
                            keyManagerName = keyManagerConfiguration.getName();
                        }
                    }
                    KeyManager keyManager = KeyManagerHolder.getKeyManagerInstance(tenntDomain,keyManagerName);
                    boolean canRetrieveOauthApp =
                            isOauthAppValidationEnabled() || APIConstants.OAuthAppMode.CREATED.name()
                                    .equals(createMode);
                    if (keyManager != null && canRetrieveOauthApp) {
                        OAuthApplicationInfo oAuthApplication = keyManager.retrieveApplication(consumerKey);
                        keyTypeWiseOAuthApps.put(keyManagerName,oAuthApplication);
                    }
                }
            }


        } catch (SQLException e) {
            handleException("Failed to get  client of application. SQL error", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, connection, rs);
        }

        return keyTypeWiseOAuthApps;
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

    public long getAPISubscriptionCountByAPI(Identifier identifier) throws APIManagementException {
        String sqlQuery = SQLConstants.GET_API_SUBSCRIPTION_COUNT_BY_API_SQL;
        String artifactType = APIConstants.API_IDENTIFIER_TYPE;
        if (identifier instanceof APIProductIdentifier) {
            artifactType = APIConstants.API_PRODUCT_IDENTIFIER_TYPE;
        }

        long subscriptions = 0;

        try (Connection connection = APIMgtDBUtil.getConnection();
            PreparedStatement ps = connection.prepareStatement(sqlQuery)) {
            ps.setString(1, APIUtil.replaceEmailDomainBack(identifier.getProviderName()));
            ps.setString(2, identifier.getName());
            ps.setString(3, identifier.getVersion());
            try (ResultSet result = ps.executeQuery()) {
                while (result.next()) {
                    subscriptions = result.getLong("SUB_ID");
                }
            }
        } catch (SQLException e) {
            handleException("Failed to get subscription count for " + artifactType , e);
        }

        return subscriptions;
    }

    /**
     * This method is used to update the subscriber
     *
     * @param apiTypeWrapper    APIIdentifier
     * @param applicationId Application id
     * @throws org.wso2.carbon.apimgt.api.APIManagementException if failed to update subscriber
     */
    public void updateSubscriptions(ApiTypeWrapper apiTypeWrapper, int applicationId,String subscriber)
            throws APIManagementException {
        addSubscription(apiTypeWrapper, applicationId, APIConstants.SubscriptionStatus.UNBLOCKED, subscriber);
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

    public void updateSubscriptionStatusAndTier(int subscriptionId, String status) throws APIManagementException {
        Connection conn = null;
        PreparedStatement ps = null;
        SubscribedAPI subscribedAPI = getSubscriptionById(subscriptionId);
        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);

            //This query is to update the AM_SUBSCRIPTION table
            String sqlQuery = SQLConstants.UPDATE_SUBSCRIPTION_STATUS_AND_TIER_SQL;

            ps = conn.prepareStatement(sqlQuery);
            ps.setString(1, null);
            if (subscribedAPI.getRequestedTier().getName() == null ) {
                ps.setString(2, subscribedAPI.getTier().getName());
            } else {
                ps.setString(2, subscribedAPI.getRequestedTier().getName());
            }
            ps.setString(3, status);
            ps.setInt(4, subscriptionId);
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


    public Map<String, String> getRegistrationApprovalState(int appId, String keyType) throws APIManagementException {
        Map<String,String> keyManagerWiseApprovalState = new HashMap<>();
        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;

        try {
            conn = APIMgtDBUtil.getConnection();
            String sqlQuery = SQLConstants.GET_REGISTRATION_APPROVAL_STATUS_SQL;

            ps = conn.prepareStatement(sqlQuery);
            ps.setInt(1, appId);
            ps.setString(2, keyType);
            resultSet = ps.executeQuery();

            while (resultSet.next()) {
                String state = resultSet.getString("STATE");
                String keyManagerName = resultSet.getString("KEY_MANAGER");
                keyManagerWiseApprovalState.put(keyManagerName, state);
            }
        } catch (SQLException e) {
            handleException("Error while getting Application Registration State.", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, resultSet);
        }
        return keyManagerWiseApprovalState;
    }

    /**
     * Update the consumer key and application status for the given key type and application.
     *  @param application
     * @param keyType
     * @param keyManagerId
     */
    public void updateApplicationKeyTypeMapping(Application application, String keyType,
                                                String keyManagerId) throws APIManagementException {
        OAuthApplicationInfo app = application.getOAuthApp(keyType,keyManagerId);
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
                OAuthApplicationInfo oAuthApp = application.getOAuthApp(keyType, keyManagerId);
                String content = new Gson().toJson(oAuthApp);
                ps.setBinaryStream(2, new ByteArrayInputStream(content.getBytes()));
                ps.setInt(3, application.getId());
                ps.setString(4, keyType);
                ps.setString(5, keyManagerId);
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
     * @param applicationId   apim application ID.
     * @param userName        apim user name
     * @param clientId        this is the consumner key.
     * @param keyMappingId
     * @throws APIManagementException
     */
    public void createApplicationKeyTypeMappingForManualClients(String keyType, int applicationId, String clientId,
                                                                String keyManagerId, String keyMappingId)
            throws APIManagementException {
        String consumerKey = null;
        if (clientId != null) {
            consumerKey = clientId;
        }
        Connection connection = null;
        PreparedStatement ps = null;

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
                ps.setString(5, APIConstants.OAuthAppMode.MAPPED.name());
                ps.setString(6, keyManagerId);
                ps.setString(7, keyMappingId);
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
    public void updateApplicationRegistration(String state, String keyType, int appId, String keyManager)
            throws APIManagementException {
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
            ps.setString(4, keyManager);
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
     *
     * @param apiIdentifier
     * @param userId
     * @param applicationId
     * @return true if user app subscribed for given APIIdentifier
     * @throws APIManagementException if failed to check subscribed or not
     */
    public boolean isSubscribedToApp(APIIdentifier apiIdentifier, String userId, int applicationId)
            throws APIManagementException {
        boolean isSubscribed = false;
        String loginUserName = getLoginUserName(userId);

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sqlQuery = SQLConstants.GET_APP_SUBSCRIPTION_TO_API_SQL;

        if (forceCaseInsensitiveComparisons) {
            sqlQuery = SQLConstants.GET_APP_SUBSCRIPTION_TO_API_CASE_INSENSITIVE_SQL;
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
            ps.setInt(6, applicationId);

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
                applicationObj.setId(appId);
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
     * @param providerName Name of the provider
     * @return UserApplicationAPIUsage of given provider
     * @throws org.wso2.carbon.apimgt.api.APIManagementException if failed to get
     *                                                           UserApplicationAPIUsage for given provider
     */
    public UserApplicationAPIUsage[] getAllAPIProductUsageByProvider(String providerName) throws APIManagementException {
        try (Connection connection = APIMgtDBUtil.getConnection();
            PreparedStatement ps =
                    connection.prepareStatement(SQLConstants.GET_APP_API_USAGE_BY_PROVIDER_SQL)) {
            ps.setString(1, APIUtil.replaceEmailDomainBack(providerName));
            try (ResultSet result = ps.executeQuery()) {
                Map<String, UserApplicationAPIUsage> userApplicationUsages = new TreeMap<String, UserApplicationAPIUsage>();
                while (result.next()) {
                    int subId = result.getInt("SUBSCRIPTION_ID");
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
                        userApplicationUsages.put(key, usage);
                    }
                    APIProductIdentifier apiProductId = new APIProductIdentifier(result.getString("API_PROVIDER"), result.getString
                            ("API_NAME"), result.getString("API_VERSION"));
                    SubscribedAPI apiSubscription = new SubscribedAPI(new Subscriber(userId), apiProductId);
                    apiSubscription.setSubStatus(subStatus);
                    apiSubscription.setSubCreatedStatus(subsCreateState);
                    apiSubscription.setUUID(result.getString("SUB_UUID"));
                    apiSubscription.setTier(new Tier(result.getString("SUB_TIER_ID")));
                    Application applicationObj = new Application(result.getString("APP_UUID"));
                    apiSubscription.setApplication(applicationObj);
                    usage.addApiSubscriptions(apiSubscription);
                }
                return userApplicationUsages.values().toArray(new UserApplicationAPIUsage[userApplicationUsages.size()]);
            }
        } catch (SQLException e) {
            handleException("Failed to find API Product Usage for :" + providerName, e);
        }

        return new UserApplicationAPIUsage[]{};
    }

    /**
     * @param apiName    Name of the API
     * @param apiVersion Version of the API
     * @param provider Name of API creator
     * @return All subscriptions of a given API
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     */
    public List<SubscribedAPI> getSubscriptionsOfAPI(String apiName, String apiVersion, String provider)
            throws APIManagementException {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet result = null;
        List<SubscribedAPI> subscriptions = new ArrayList<>();

        try {
            String sqlQuery = SQLConstants.GET_SUBSCRIPTIONS_OF_API_SQL;
            connection = APIMgtDBUtil.getConnection();

            ps = connection.prepareStatement(sqlQuery);
            ps.setString(1, apiName);
            ps.setString(2, apiVersion);
            ps.setString(3, provider);
            result = ps.executeQuery();

            while (result.next()) {
                APIIdentifier apiId = new APIIdentifier(result.getString("API_PROVIDER"), apiName, apiVersion);
                Subscriber subscriber = new Subscriber(result.getString("USER_ID"));
                SubscribedAPI subscription = new SubscribedAPI(subscriber, apiId);
                subscription.setUUID(result.getString("SUB_UUID"));
                subscription.setSubStatus(result.getString("SUB_STATUS"));
                subscription.setSubCreatedStatus(result.getString("SUBS_CREATE_STATE"));
                subscription.setTier(new Tier(result.getString("SUB_TIER_ID")));
                subscription.setCreatedTime(result.getString("SUB_CREATED_TIME"));

                Application application = new Application(result.getInt("APPLICATION_ID"));
                application.setName(result.getString("APPNAME"));
                subscription.setApplication(application);

                subscriptions.add(subscription);
            }
        } catch (SQLException e) {
            handleException("Error occurred while reading subscriptions of API: " + apiName + ':' + apiVersion, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, connection, result);
        }
        return subscriptions;
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
            Subscriber subscriber = getSubscriber(userId);
            String tenantDomain = MultitenantUtils.getTenantDomain(subscriber.getName());

            if (multiGroupAppSharingEnabled) {
                updateGroupIDMappings(conn, applicationId, application.getGroupId(), tenantDomain);
            }
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

    public void addRating(Identifier id, int rating, String user) throws APIManagementException {
        Connection conn = null;
        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);
            addOrUpdateRating(id, rating, user, conn);

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
     * @param identifier    Identifier
     * @param rating        Rating
     * @param userId        User Id
     * @throws APIManagementException if failed to add Rating
     */
    public void addOrUpdateRating(Identifier identifier, int rating, String userId, Connection conn)
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
            int id;
            id = getAPIID(identifier, conn);
            if (id == -1) {
                String msg = "Could not load API record for: " + identifier.getName();
                log.error(msg);
                throw new APIManagementException(msg);
            }
            boolean userRatingExists = false;
            //This query to check the ratings already exists for the user in the AM_API_RATINGS table
            String sqlQuery = SQLConstants.GET_API_RATING_SQL;

            psSelect = conn.prepareStatement(sqlQuery);
            psSelect.setInt(1, id);
            psSelect.setInt(2, subscriber.getId());
            rs = psSelect.executeQuery();

            while (rs.next()) {
                userRatingExists = true;
            }

            String sqlAddQuery;
            String ratingId = UUID.randomUUID().toString();
            if (!userRatingExists) {
                //This query to insert into the AM_API_RATINGS table
                sqlAddQuery = SQLConstants.ADD_API_RATING_SQL;
                ps = conn.prepareStatement(sqlAddQuery);
                ps.setString(1, ratingId);
                ps.setInt(2, rating);
                ps.setInt(3, id);
                ps.setInt(4, subscriber.getId());
            } else {
                // This query to update the AM_API_RATINGS table
                sqlAddQuery = SQLConstants.UPDATE_API_RATING_SQL;
                ps = conn.prepareStatement(sqlAddQuery);
                // Adding data to the AM_API_RATINGS table
                ps.setInt(1, rating);
                ps.setInt(2, id);
                ps.setInt(3, subscriber.getId());
            }


            ps.executeUpdate();

        } catch (SQLException e) {
            handleException("Failed to add API rating of the user:" + userId, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, null, rs);
            APIMgtDBUtil.closeAllConnections(psSelect, null, null);
        }
    }

    public void removeAPIRating(Identifier id, String user) throws APIManagementException {
        Connection conn = null;
        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);

            removeAPIRating(id, user, conn);

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
     * @param identifier    Identifier
     * @param userId        User Id
     * @throws APIManagementException if failed to remove API user Rating
     */
    public void removeAPIRating(Identifier identifier, String userId, Connection conn)
            throws APIManagementException, SQLException {
        PreparedStatement ps = null;
        PreparedStatement psSelect = null;
        ResultSet rs = null;

        try {
            int tenantId;
            String rateId = null;
            tenantId = APIUtil.getTenantId(userId);
            //Get subscriber Id
            Subscriber subscriber = getSubscriber(userId, tenantId, conn);
            if (subscriber == null) {
                String msg = "Could not load Subscriber records for: " + userId;
                log.error(msg);
                throw new APIManagementException(msg);
            }
            //Get API Id
            int id = -1;
            id = getAPIID(identifier, conn);
            if (id == -1) {
                String msg = "Could not load API record for: " + identifier.getName();
                log.error(msg);
                throw new APIManagementException(msg);
            }

            //This query to check the ratings already exists for the user in the AM_API_RATINGS table
            String sqlQuery = SQLConstants.GET_API_RATING_ID_SQL;
            psSelect = conn.prepareStatement(sqlQuery);
            psSelect.setInt(1, id);
            psSelect.setInt(2, subscriber.getId());
            rs = psSelect.executeQuery();

            while (rs.next()) {
                rateId = rs.getString("RATING_ID");
            }
            String sqlDeleteQuery;
            if (rateId != null) {
                //This query to delete the specific rate row from the AM_API_RATINGS table
                sqlDeleteQuery = SQLConstants.REMOVE_RATING_SQL;
                // Adding data to the AM_API_RATINGS  table
                ps = conn.prepareStatement(sqlDeleteQuery);
                ps.setString(1, rateId);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            handleException("Failed to delete API rating", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, null, null);
            APIMgtDBUtil.closeAllConnections(psSelect, null, rs);
        }
    }

    public int getUserRating(Identifier id, String user) throws APIManagementException {
        Connection conn = null;
        int userRating = 0;
        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);

            userRating = getUserRating(id, user, conn);

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
     * @param identifier    Identifier
     * @param userId        User Id
     * @throws APIManagementException if failed to get User API Rating
     */
    public int getUserRating(Identifier identifier, String userId, Connection conn)
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
            int id = -1;
            id = getAPIID(identifier, conn);
            if (id == -1) {
                String msg = "Could not load API record for: " + identifier.getName();
                log.error(msg);
                throw new APIManagementException(msg);
            }
            //This query to update the AM_API_RATINGS table
            String sqlQuery = SQLConstants.GET_API_RATING_SQL;

            // Adding data to the AM_API_RATINGS  table
            ps = conn.prepareStatement(sqlQuery);
            ps.setInt(1, id);
            ps.setInt(2, subscriber.getId());
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

    /**
     * @param identifier          Identifier
     * @param user        User name
     * @throws APIManagementException if failed to get user API Ratings
     */
    public JSONObject getUserRatingInfo(Identifier identifier, String user) throws APIManagementException {
        Connection conn = null;
        JSONObject userRating = null;
        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);

            userRating = getUserRatingInfo(identifier, user, conn);

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error("Failed to rollback getting user ratings info ", e1);
                }
            }
            handleException("Failed to get user ratings info", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(null, conn, null);
        }
        return userRating;
    }

    /**
     * @param identifier    Identifier
     * @param userId        User Id
     * @param conn          Database connection
     * @throws APIManagementException if failed to get user API Ratings
     */
    private JSONObject getUserRatingInfo(Identifier identifier, String userId, Connection conn)
            throws APIManagementException, SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        JSONObject ratingObj = new JSONObject();
        int userRating = 0;
        int id = -1;
        String ratingId = null;
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
            id = getAPIID(identifier, conn);

            String sqlQuery = SQLConstants.GET_API_RATING_INFO_SQL;
            if (id == -1) {
                String msg = "Could not load API record for: " + identifier.getName();
                log.error(msg);
                throw new APIManagementException(msg);
            }
            //This query to get rating information from the AM_API_RATINGS table
            ps = conn.prepareStatement(sqlQuery);
            ps.setInt(1, subscriber.getId());
            ps.setInt(2, id);
            rs = ps.executeQuery();

            while (rs.next()) {
                ratingId = rs.getString("RATING_ID");
                userRating = rs.getInt("RATING");
            }
            if (ratingId != null) {
                // A rating record exists
                ratingObj.put(APIConstants.RATING_ID, ratingId);
                ratingObj.put(APIConstants.USER_NAME, userId);
                ratingObj.put(APIConstants.RATING, userRating);
            }
        } catch (SQLException e) {
            handleException("Failed to retrieve API ratings ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, null, rs);
        }
        return ratingObj;
    }

    /**
     * @param apiId API Identifier
     * @throws APIManagementException if failed to get API Ratings
     */
    public JSONArray getAPIRatings(Identifier apiId) throws APIManagementException {
        Connection conn = null;
        JSONArray apiRatings = null;
        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);

            apiRatings = getAPIRatings(apiId, conn);

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error("Failed to rollback getting user ratings info ", e1);
                }
            }
            handleException("Failed to get user ratings info", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(null, conn, null);
        }
        return apiRatings;
    }

    /**
     * @param identifier    Identifier
     * @param conn          Database connection
     * @throws APIManagementException if failed to get API Ratings
     */
    private JSONArray getAPIRatings(Identifier identifier, Connection conn)
            throws APIManagementException, SQLException {
        PreparedStatement ps = null;
        PreparedStatement psSubscriber = null;
        ResultSet rs = null;
        ResultSet rsSubscriber = null;
        JSONArray ratingArray = new JSONArray();
        int userRating = 0;
        String ratingId = null;
        int id = -1;
        int subscriberId = -1;
        try {
            //Get API Id
            id = getAPIID(identifier, conn);
            if (id == -1) {
                String msg = "Could not load API record for: " + identifier.getName();
                log.error(msg);
                throw new APIManagementException(msg);
            }
            //This query to get rating information from the AM_API_RATINGS table
            String sqlQuery = SQLConstants.GET_API_ALL_RATINGS_SQL;
            ps = conn.prepareStatement(sqlQuery);
            ps.setInt(1, id);
            rs = ps.executeQuery();

            while (rs.next()) {
                JSONObject ratingObj = new JSONObject();
                String subscriberName = null;
                ratingId = rs.getString("RATING_ID");
                subscriberId = rs.getInt("SUBSCRIBER_ID");
                userRating = rs.getInt("RATING");
                ratingObj.put(APIConstants.RATING_ID, ratingId);
                // SQL Query to get subscriber name
                String sqlSubscriberQuery = SQLConstants.GET_SUBSCRIBER_NAME_FROM_ID_SQL;

                psSubscriber = conn.prepareStatement(sqlSubscriberQuery);
                psSubscriber.setInt(1, subscriberId);
                rsSubscriber = psSubscriber.executeQuery();

                while (rsSubscriber.next()) {
                    subscriberName = rsSubscriber.getString("USER_ID");
                }

                ratingObj.put(APIConstants.USER_NAME, subscriberName);
                ratingObj.put(APIConstants.RATING,userRating);
                ratingArray.add(ratingObj);
            }
        } catch (SQLException e) {
            handleException("Failed to retrieve API ratings ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, null, rs);
            APIMgtDBUtil.closeAllConnections(psSubscriber, null, rsSubscriber);
        }
        return ratingArray;
    }

    public float getAverageRating(Identifier apiId) throws APIManagementException {
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
            String sqlQuery = SQLConstants.GET_API_AVERAGE_RATING_SQL;

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
     * @param identifier Identifier
     * @throws APIManagementException if failed to add Application
     */
    public float getAverageRating(Identifier identifier, Connection conn)
            throws APIManagementException, SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        float avrRating = 0;
        try {
            //Get API Id
            int apiId;
            apiId = getAPIID(identifier, conn);
            if (apiId == -1) {
                String msg = "Could not load API record for: " + identifier.getName();
                log.error(msg);
                return Float.NEGATIVE_INFINITY;
            }
            //This query to update the AM_API_RATINGS table
            String sqlQuery = SQLConstants.GET_API_AVERAGE_RATING_SQL;

            ps = conn.prepareStatement(sqlQuery);
            ps.setInt(1, apiId);
            rs = ps.executeQuery();

            while (rs.next()) {
                avrRating = rs.getFloat("RATING");
            }

        } catch (SQLException e) {
            handleException("Failed to get average rating ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, null, rs);
        }

        BigDecimal decimal = new BigDecimal(avrRating);
        return Float.parseFloat(decimal.setScale(1, BigDecimal.ROUND_UP).toString());
    }

    /**
     * Get details of the subscription block condition by condition value and tenant domain
     *
     * @param conditionValue condition value of the block condition
     * @param tenantDomain tenant domain of the block condition
     * @return Block condition
     * @throws APIManagementException
     */
    public BlockConditionsDTO getSubscriptionBlockCondition(String conditionValue, String tenantDomain)
            throws APIManagementException {
        Connection connection = null;
        PreparedStatement selectPreparedStatement = null;
        ResultSet resultSet = null;
        BlockConditionsDTO blockCondition = null;
        try {
            String query = SQLConstants.ThrottleSQLConstants.GET_SUBSCRIPTION_BLOCK_CONDITION_BY_VALUE_AND_DOMAIN_SQL;
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(true);
            selectPreparedStatement = connection.prepareStatement(query);
            selectPreparedStatement.setString(1, conditionValue);
            selectPreparedStatement.setString(2, tenantDomain);
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
                    handleException("Failed to rollback getting Subscription Block condition with condition value "
                            + conditionValue + " of tenant " + tenantDomain, ex);
                }
            }
            handleException("Failed to get Subscription Block condition with condition value " + conditionValue
                    + " of tenant " + tenantDomain, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(selectPreparedStatement, connection, resultSet);
        }
        return blockCondition;
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
            int tenantId = APIUtil.getTenantId(userId);

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

            String groupId = application.getGroupId();
            if (multiGroupAppSharingEnabled) {
                // setting an empty groupId since groupid's should be saved in groupId mapping table
                groupId = "";
            }
            ps.setString(7, groupId);
            ps.setString(8, subscriber.getName());

            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            ps.setTimestamp(9, timestamp);
            ps.setTimestamp(10, timestamp);
            ps.setString(11, application.getUUID());
            ps.setString(12, String.valueOf(application.getTokenType()));
            ps.executeUpdate();

            rs = ps.getGeneratedKeys();
            while (rs.next()) {
                applicationId = Integer.parseInt(rs.getString(1));
            }

            //Adding data to AM_APPLICATION_ATTRIBUTES table
            if (application.getApplicationAttributes() != null) {
                addApplicationAttributes(conn, application.getApplicationAttributes(), applicationId, tenantId);
            }
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
        PreparedStatement preparedStatement = null;
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
            ps.setString(7, application.getTokenType());
            ps.setInt(8, application.getId());

            ps.executeUpdate();

            if (multiGroupAppSharingEnabled) {
                Subscriber subscriber = application.getSubscriber();
                String tenantDomain = MultitenantUtils.getTenantDomain(subscriber.getName());
                updateGroupIDMappings(conn, application.getId(), application.getGroupId(),
                        tenantDomain);
            }
            Subscriber subscriber = application.getSubscriber();
            String domain = MultitenantUtils.getTenantDomain(subscriber.getName());
            int tenantId = IdentityTenantUtil.getTenantId(domain);

            preparedStatement = conn.prepareStatement(SQLConstants.REMOVE_APPLICATION_ATTRIBUTES_SQL);
            preparedStatement.setInt(1,application.getId());
            preparedStatement.execute();

            if (log.isDebugEnabled()) {
                log.debug("Old attributes of application - " + application.getName() + " are removed");
            }

            if (application.getApplicationAttributes() != null && !application.getApplicationAttributes().isEmpty()) {
                addApplicationAttributes(conn, application.getApplicationAttributes(), application.getId(), tenantId);
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
        }  finally {
            APIMgtDBUtil.closeAllConnections(ps, null, null);
            APIMgtDBUtil.closeAllConnections(preparedStatement, conn, null);
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

        String whereClauseWithGroupId = " AND (APP.GROUP_ID = ? OR ((APP.GROUP_ID='' OR APP.GROUP_ID IS NULL)"
                + " AND SUB.USER_ID = ?))";
        String whereClauseWithGroupIdCaseInsensitive = " AND (APP.GROUP_ID = ? "
                + "OR ((APP.GROUP_ID='' OR APP.GROUP_ID IS NULL) AND LOWER(SUB.USER_ID) = LOWER(?)))";

        String whereClauseWithMultiGroupId = " AND  ( (APP.APPLICATION_ID IN (SELECT APPLICATION_ID  FROM " +
                "AM_APPLICATION_GROUP_MAPPING WHERE GROUP_ID IN ($params) AND TENANT = ?))  OR  ( SUB.USER_ID = ? ) " +
                "OR (APP.APPLICATION_ID IN (SELECT APPLICATION_ID FROM AM_APPLICATION WHERE GROUP_ID = ?)))";

        String whereClauseWithMultiGroupIdCaseInsensitive = " AND  ( (APP.APPLICATION_ID IN  (SELECT APPLICATION_ID " +
                "FROM AM_APPLICATION_GROUP_MAPPING WHERE GROUP_ID IN ($params) AND TENANT = ?)) " +
                "OR (LOWER(SUB.USER_ID) = LOWER(?))" +
                "OR (APP.APPLICATION_ID IN (SELECT APPLICATION_ID FROM AM_APPLICATION WHERE GROUP_ID = ?)))";


        String whereClause = " AND SUB.USER_ID = ? ";
        String whereClauseCaseInsensitive = " AND LOWER(SUB.USER_ID) = LOWER(?) ";

        try {
            connection = APIMgtDBUtil.getConnection();

            if (!StringUtils.isEmpty(groupId)) {
                if (multiGroupAppSharingEnabled) {
                    if (forceCaseInsensitiveComparisons) {
                        sqlQuery += whereClauseWithMultiGroupIdCaseInsensitive;
                    } else {
                        sqlQuery += whereClauseWithMultiGroupId;
                    }
                    String tenantDomain = MultitenantUtils.getTenantDomain(subscriber.getName());
                    String[] grpIdArray = groupId.split(",");
                    int noOfParams = grpIdArray.length;
                    preparedStatement = fillQueryParams(connection, sqlQuery, grpIdArray, 2);
                    preparedStatement.setString(1, appName);
                    int paramIndex = noOfParams + 1;
                    preparedStatement.setString(++paramIndex, tenantDomain);
                    preparedStatement.setString(++paramIndex, subscriber.getName());
                    preparedStatement.setString(++paramIndex, tenantDomain + '/' + groupId);
                } else {
                    if (forceCaseInsensitiveComparisons) {
                        sqlQuery += whereClauseWithGroupIdCaseInsensitive;
                    } else {
                        sqlQuery += whereClauseWithGroupId;
                    }
                    preparedStatement = connection.prepareStatement(sqlQuery);
                    preparedStatement.setString(1, appName);
                    preparedStatement.setString(2, groupId);
                    preparedStatement.setString(3, subscriber.getName());
                }
            } else {
                if (forceCaseInsensitiveComparisons) {
                    sqlQuery += whereClauseCaseInsensitive;
                } else {
                    sqlQuery += whereClause;
                }
                preparedStatement = connection.prepareStatement(sqlQuery);
                preparedStatement.setString(1, appName);
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
     * Check whether the new user has an application
     *
     * @param appName  application name
     * @param username subscriber
     * @return true if application is available for the subscriber
     * @throws APIManagementException if failed to get applications for given subscriber
     */
    public boolean isApplicationOwnedBySubscriber(String appName, String username) throws APIManagementException {
        if (username == null) {
            return false;
        }
        Subscriber subscriber = getSubscriber(username);
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        int appId = 0;
        String sqlQuery = SQLConstants.GET_APPLICATION_ID_PREFIX;
        String whereClause = " AND SUB.USER_ID = ? ";
        String whereClauseCaseInsensitive = " AND LOWER(SUB.USER_ID) = LOWER(?) ";
        try {
            connection = APIMgtDBUtil.getConnection();
            if (forceCaseInsensitiveComparisons) {
                sqlQuery += whereClauseCaseInsensitive;
            } else {
                sqlQuery += whereClause;
            }
            preparedStatement = connection.prepareStatement(sqlQuery);
            preparedStatement.setString(1, appName);
            preparedStatement.setString(2, subscriber.getName());
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

    public int getAllApplicationCount(Subscriber subscriber, String groupingId, String search) throws APIManagementException {

        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet resultSet = null;
        String sqlQuery = null;

        try {
            connection = APIMgtDBUtil.getConnection();
            if (groupingId != null && !"null".equals(groupingId) && !groupingId.isEmpty()) {

                if (multiGroupAppSharingEnabled) {
                    if (forceCaseInsensitiveComparisons) {
                        sqlQuery = SQLConstants.GET_APPLICATIONS_COUNNT_CASESENSITVE_WITH_MULTIGROUPID;
                    } else {
                        sqlQuery = SQLConstants.GET_APPLICATIONS_COUNNT_NONE_CASESENSITVE_WITH_MULTIGROUPID;
                    }
                    String tenantDomain = MultitenantUtils.getTenantDomain(subscriber.getName());
                    String[] grpIdArray = groupingId.split(",");
                    int noOfParams = grpIdArray.length;
                    prepStmt = fillQueryParams(connection, sqlQuery, grpIdArray, 1);
                    prepStmt.setString(++noOfParams, tenantDomain);
                    prepStmt.setString(++noOfParams, subscriber.getName());
                    prepStmt.setString(++noOfParams, "%" + search + "%");
                } else {
                    if (forceCaseInsensitiveComparisons) {
                        sqlQuery = SQLConstants.GET_APPLICATIONS_COUNNT_CASESENSITVE_WITHGROUPID;
                    } else {
                        sqlQuery = SQLConstants.GET_APPLICATIONS_COUNNT_NONE_CASESENSITVE_WITHGROUPID;
                    }
                    prepStmt = connection.prepareStatement(sqlQuery);
                    prepStmt.setString(1, groupingId);
                    prepStmt.setString(2, subscriber.getName());
                    prepStmt.setString(3, "%" + search + "%");
                }

            } else {

                if (forceCaseInsensitiveComparisons) {
                    sqlQuery = SQLConstants.GET_APPLICATIONS_COUNNT_CASESENSITVE;
                } else {
                    sqlQuery = SQLConstants.GET_APPLICATIONS_COUNNT_NONE_CASESENSITVE;
                }
                prepStmt = connection.prepareStatement(sqlQuery);
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
            handleException("Failed to get applicaiton count : ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, resultSet);
        }

        return 0;
    }

    /**
     * Returns all applications created by given user Id
     *
     * @param userId
     * @return
     * @throws APIManagementException
     */
    public Application[] getApplicationsByOwner(String userId) throws APIManagementException {

        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        Application[] applications = null;

        String sqlQuery = SQLConstants.GET_APPLICATIONS_BY_OWNER;

        try {
            connection = APIMgtDBUtil.getConnection();
            prepStmt = connection.prepareStatement(sqlQuery);
            prepStmt.setString(1, userId);
            rs = prepStmt.executeQuery();

            ArrayList<Application> applicationsList = new ArrayList<Application>();
            Application application;
            while (rs.next()) {
                application = new Application(rs.getString("UUID"));
                application.setName(rs.getString("NAME"));
                application.setOwner(rs.getString("CREATED_BY"));
                application.setStatus(rs.getString("APPLICATION_STATUS"));
                application.setGroupId(rs.getString("GROUP_ID"));

                if (multiGroupAppSharingEnabled) {
                    application.setGroupId(getGroupId(rs.getInt("APPLICATION_ID")));
                }
                applicationsList.add(application);
            }
            applications = applicationsList.toArray(new Application[applicationsList.size()]);
        } catch (SQLException e) {
            handleException("Error when getting the application name for id " + userId, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
        }
        return applications;
    }

    /**
     * Returns all applications created by given user Id
     *
     * @param userName
     * @return
     * @throws APIManagementException
     */
    public boolean updateApplicationOwner(String userName, Application application) throws
            APIManagementException {

        boolean isAppUpdated = false;
        Connection connection = null;
        PreparedStatement prepStmt = null;

        String sqlQuery = SQLConstants.UPDATE_APPLICATION_OWNER;

        try {
            Subscriber subscriber = getSubscriber(userName);
            if (subscriber != null) {
                int subscriberId = getSubscriber(userName).getId();
                connection = APIMgtDBUtil.getConnection();
                connection.setAutoCommit(false);
                prepStmt = connection.prepareStatement(sqlQuery);
                prepStmt.setString(1, userName);
                prepStmt.setInt(2, subscriberId);
                prepStmt.setString(3, application.getUUID());
                prepStmt.executeUpdate();
                connection.commit();
                isAppUpdated = true;
            } else {
                String errorMessage = "Error when retrieving subscriber details for user " + userName;
                handleException(errorMessage, new APIManagementException(errorMessage));
            }
        } catch (SQLException e) {
            handleException("Error when updating application owner for user " + userName, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, null);
        }
        return isAppUpdated;
    }

    /**
     * #TODO later we might need to use only this method.
     *
     * @param subscriber The subscriber.
     * @param groupingId The groupId to which the applications must belong.
     * @param start      The start index.
     * @param offset     The offset.
     * @param search     The search string.
     * @param sortOrder  The sort order.
     * @param sortColumn The sort column.
     * @return Application[] The array of applications.
     * @throws APIManagementException
     */
    public Application[] getApplicationsWithPagination(Subscriber subscriber, String groupingId, int start,
                                                       int offset, String search, String sortColumn, String sortOrder)
            throws APIManagementException {

        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        Application[] applications = null;
        String sqlQuery = null;

        if (groupingId != null && !"null".equals(groupingId) && !groupingId.isEmpty()) {
            if (multiGroupAppSharingEnabled) {
                if (forceCaseInsensitiveComparisons) {
                    sqlQuery = SQLConstantManagerFactory.
                            getSQlString("GET_APPLICATIONS_PREFIX_NONE_CASESENSITVE_WITH_MULTIGROUPID");
                } else {
                    sqlQuery = SQLConstantManagerFactory.
                            getSQlString("GET_APPLICATIONS_PREFIX_CASESENSITVE_WITH_MULTIGROUPID");
                }
            } else {
                if (forceCaseInsensitiveComparisons) {
                    sqlQuery = SQLConstantManagerFactory.
                            getSQlString("GET_APPLICATIONS_PREFIX_NONE_CASESENSITVE_WITHGROUPID");
                } else {
                    sqlQuery = SQLConstantManagerFactory.
                            getSQlString("GET_APPLICATIONS_PREFIX_CASESENSITVE_WITHGROUPID");
                }
            }
        } else {
            if (forceCaseInsensitiveComparisons) {
                sqlQuery = SQLConstantManagerFactory.getSQlString("GET_APPLICATIONS_PREFIX_NONE_CASESENSITVE");
            } else {
                sqlQuery = SQLConstantManagerFactory.getSQlString("GET_APPLICATIONS_PREFIX_CASESENSITVE");
            }
        }

        try {
            connection = APIMgtDBUtil.getConnection();
            String driverName = connection.getMetaData().getDriverName();
            if (driverName.contains("Oracle")) {
                offset = start + offset;
            }
            // sortColumn, sortOrder variable values has sanitized in jaggery level (applications-list.jag)for security.
            sqlQuery = sqlQuery.replace("$1", sortColumn);
            if ("acs".equalsIgnoreCase(sortOrder) || "desc".equalsIgnoreCase(sortOrder)) {
                sqlQuery = sqlQuery.replace("$2", sortOrder);
            } else {
                sqlQuery = sqlQuery.replace("$2", "asc");
            }

            if (driverName.contains("Oracle") && "CREATED_BY".equals(sortColumn)) {
                sqlQuery = sqlQuery.replace("$3", "APP.CREATED_BY");
            } else {
                sqlQuery = sqlQuery.replace("$3", sortColumn);
            }
            
            if (groupingId != null && !"null".equals(groupingId) && !groupingId.isEmpty()) {
                if (multiGroupAppSharingEnabled) {
                    String tenantDomain = MultitenantUtils.getTenantDomain(subscriber.getName());
                    String[] grpIdArray = groupingId.split(",");
                    int noOfParams = grpIdArray.length;
                    prepStmt = fillQueryParams(connection, sqlQuery, grpIdArray, 1);
                    prepStmt.setString(++noOfParams, tenantDomain);
                    prepStmt.setString(++noOfParams, subscriber.getName());
                    prepStmt.setString(++noOfParams, tenantDomain + '/' + groupingId);
                    prepStmt.setString(++noOfParams, "%" + search + "%");
                    prepStmt.setInt(++noOfParams, start);
                    prepStmt.setInt(++noOfParams, offset);
                } else {
                    prepStmt = connection.prepareStatement(sqlQuery);
                    prepStmt.setString(1, groupingId);
                    prepStmt.setString(2, subscriber.getName());
                    prepStmt.setString(3, "%" + search + "%");
                    prepStmt.setInt(4, start);
                    prepStmt.setInt(5, offset);
                }
            } else {
                prepStmt = connection.prepareStatement(sqlQuery);
                prepStmt.setString(1, subscriber.getName());
                prepStmt.setString(2, "%" + search + "%");
                prepStmt.setInt(3, start);
                prepStmt.setInt(4, offset);
            }
            if (log.isDebugEnabled()) {
                log.debug("Query: " + sqlQuery);
                log.debug("Param: " + "Sub:" + subscriber.getName() + " GroupId: " + groupingId + " Search:%" + search
                        + "% " + "Start:" + start + " Offset:" + offset + " SortColumn:" + sortColumn + " SortOrder:"
                        + sortOrder);
            }
            rs = prepStmt.executeQuery();
            ArrayList<Application> applicationsList = new ArrayList<Application>();
            Application application;
            while (rs.next()) {
                application = new Application(rs.getString("NAME"), subscriber);
                int applicationId = rs.getInt("APPLICATION_ID");
                application.setId(applicationId);
                application.setTier(rs.getString("APPLICATION_TIER"));
                application.setDescription(rs.getString("DESCRIPTION"));
                application.setStatus(rs.getString("APPLICATION_STATUS"));
                application.setGroupId(rs.getString("GROUP_ID"));
                application.setUUID(rs.getString("UUID"));
                application.setIsBlackListed(rs.getBoolean("ENABLED"));
                application.setOwner(rs.getString("CREATED_BY"));

                if (multiGroupAppSharingEnabled) {
                    setGroupIdInApplication(application);
                }

                //setting subscription count
                int subscriptionCount = getSubscriptionCountByApplicationId(subscriber, applicationId, groupingId);
                application.setSubscriptionCount(subscriptionCount);

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

    /**
     * Returns all the applications associated with given subscriber and group id, without their keys.
     *
     * @param subscriber The subscriber.
     * @param groupingId The groupId to which the applications must belong.
     * @return Application[] Array of applications.
     * @throws APIManagementException
     */
    public Application[] getLightWeightApplications(Subscriber subscriber, String groupingId) throws
            APIManagementException {

        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        Application[] applications = null;
        String sqlQuery = SQLConstants.GET_APPLICATIONS_PREFIX;

        String whereClauseWithGroupId;
        String whereClauseWithMultiGroupId;

        if (forceCaseInsensitiveComparisons) {
            if (multiGroupAppSharingEnabled) {
                whereClauseWithGroupId = " AND ( (APP.APPLICATION_ID IN (SELECT APPLICATION_ID  FROM " +
                        "AM_APPLICATION_GROUP_MAPPING WHERE GROUP_ID IN ($params) AND TENANT = ?)) " +
                        "OR (LOWER(SUB.USER_ID) = LOWER(?))" +
                        "OR (APP.APPLICATION_ID IN (SELECT APPLICATION_ID FROM AM_APPLICATION WHERE GROUP_ID = ?)))";
            } else {
                whereClauseWithGroupId = "   AND " + "     (GROUP_ID= ? " + "      OR "
                        + "     ((GROUP_ID='' OR GROUP_ID IS NULL) AND LOWER(SUB.USER_ID) = LOWER(?))) ";
            }
        } else {
            if (multiGroupAppSharingEnabled) {
                whereClauseWithGroupId = " AND ( (APP.APPLICATION_ID IN (SELECT APPLICATION_ID " +
                        "FROM AM_APPLICATION_GROUP_MAPPING WHERE GROUP_ID IN ($params) AND TENANT = ?))  " +
                        "OR  ( SUB.USER_ID = ? )" +
                        "OR (APP.APPLICATION_ID IN (SELECT APPLICATION_ID FROM AM_APPLICATION WHERE GROUP_ID = ?))) ";
            } else {
                whereClauseWithGroupId = "   AND " + "     (GROUP_ID= ? " + "      OR "
                        + "     ((GROUP_ID='' OR GROUP_ID IS NULL) AND SUB.USER_ID=?))";
            }
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
                sqlQuery = sqlQuery.replaceAll("NAME", "cast(NAME as varchar(100)) collate " +
                        "SQL_Latin1_General_CP1_CI_AS as NAME");
                blockingFilerSql = " select distinct x.*,bl.ENABLED from ( " + sqlQuery + " )x left join " +
                        "AM_BLOCK_CONDITIONS bl on  ( bl.TYPE = 'APPLICATION' AND bl.VALUE = (x.USER_ID + ':') + x" +
                        ".name)";
            } else {
                blockingFilerSql = " select distinct x.*,bl.ENABLED from ( " + sqlQuery
                        + " )x left join AM_BLOCK_CONDITIONS bl on  ( bl.TYPE = 'APPLICATION' AND bl.VALUE = "
                        + "concat(concat(x.USER_ID,':'),x.name))";
            }

            if (groupingId != null && !"null".equals(groupingId) && !groupingId.isEmpty()) {
                if (multiGroupAppSharingEnabled) {
                    String tenantDomain = MultitenantUtils.getTenantDomain(subscriber.getName());
                    String groupIDArray[] = groupingId.split(",");
                    int paramIndex = groupIDArray.length;
                    prepStmt = fillQueryParams(connection, blockingFilerSql, groupIDArray, 1);
                    prepStmt.setString(++paramIndex, tenantDomain);
                    prepStmt.setString(++paramIndex, subscriber.getName());
                    prepStmt.setString(++paramIndex, tenantDomain + '/' + groupingId);
                } else {
                    prepStmt = connection.prepareStatement(blockingFilerSql);
                    prepStmt.setString(1, groupingId);
                    prepStmt.setString(2, subscriber.getName());
                }
            } else {
                prepStmt = connection.prepareStatement(blockingFilerSql);
                prepStmt.setString(1, subscriber.getName());
            }
            rs = prepStmt.executeQuery();
            ArrayList<Application> applicationsList = new ArrayList<Application>();
            Application application;
            Map<String,String> applicationAttributes;
            int applicationId = 0;
            while (rs.next()) {
                applicationId = rs.getInt("APPLICATION_ID");
                application = new Application(rs.getString("NAME"), subscriber);
                application.setId(applicationId);
                application.setTier(rs.getString("APPLICATION_TIER"));
                application.setCallbackUrl(rs.getString("CALLBACK_URL"));
                application.setDescription(rs.getString("DESCRIPTION"));
                application.setStatus(rs.getString("APPLICATION_STATUS"));
                application.setGroupId(rs.getString("GROUP_ID"));
                application.setUUID(rs.getString("UUID"));
                application.setIsBlackListed(rs.getBoolean("ENABLED"));
                application.setOwner(rs.getString("CREATED_BY"));
                application.setTokenType(rs.getString("TOKEN_TYPE"));
                if (multiGroupAppSharingEnabled) {
                    setGroupIdInApplication(application);
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
     * Returns applications within a tenant domain with pagination
     * @param tenantId   The tenantId.
     * @param start      The start index.
     * @param offset     The offset.
     * @param searchOwner     The search string.
     * @param searchApplication     The search string.
     * @param sortOrder  The sort order.
     * @param sortColumn The sort column.
     * @return Application[] The array of applications.
     * @throws APIManagementException
     */
    public List<Application> getApplicationsByTenantIdWithPagination(int tenantId, int start, int offset,
                                                                     String searchOwner, String searchApplication,
                                                                     String sortColumn, String sortOrder)
            throws APIManagementException {
        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        String sqlQuery = null;
        List<Application> applicationList = new ArrayList<>();
        sqlQuery = SQLConstantManagerFactory.getSQlString("GET_APPLICATIONS_BY_TENANT_ID");
        try {
            connection = APIMgtDBUtil.getConnection();
            if (connection.getMetaData().getDriverName().contains("Oracle")) {
                offset = start + offset;
            }
            sqlQuery = sqlQuery.replace("$1", sortColumn);
            sqlQuery = sqlQuery.replace("$2", sortOrder);
            prepStmt = connection.prepareStatement(sqlQuery);
            prepStmt.setInt(1, tenantId);
            prepStmt.setString(2, "%" + searchOwner + "%");
            prepStmt.setString(3, "%" + searchApplication + "%");
            prepStmt.setInt(4, start);
            prepStmt.setInt(5, offset);
            rs = prepStmt.executeQuery();
            Application application;
            while (rs.next()) {
                String applicationName = rs.getString("NAME");
                String subscriberName = rs.getString("CREATED_BY");
                Subscriber subscriber = new Subscriber(subscriberName);
                application = new Application(applicationName, subscriber);
                application.setName(applicationName);
                application.setId(rs.getInt("APPLICATION_ID"));
                application.setUUID(rs.getString("UUID"));
                application.setGroupId(rs.getString("GROUP_ID"));
                subscriber.setTenantId(rs.getInt("TENANT_ID"));
                subscriber.setId(rs.getInt("SUBSCRIBER_ID"));
                application.setStatus(rs.getString("APPLICATION_STATUS"));
                application.setOwner(subscriberName);
                applicationList.add(application);
            }
        } catch (SQLException e) {
            handleException("Error while obtaining details of the Application for tenant id : " + tenantId, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
        }
        return applicationList;
    }

    /**
     * Returns applications within a tenant domain with pagination
     * @param tenantId   The tenantId.
     * @param start      The start index.
     * @param offset     The offset.
     * @param searchOwner     The search string.
     * @param searchApplication     The search string.
     * @param sortOrder  The sort order.
     * @param sortColumn The sort column.
     * @return Application[] The array of applications.
     * @throws APIManagementException
     */
    public List<Application> getApplicationsByNameWithPagination(int tenantId, int start, int offset,
                                                                     String searchApplication,
                                                                     String sortColumn, String sortOrder)
            throws APIManagementException {
        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        String sqlQuery = null;
        List<Application> applicationList = new ArrayList<>();
        sqlQuery = SQLConstantManagerFactory.getSQlString("GET_APPLICATIONS_BY_NAME");
        try {
            connection = APIMgtDBUtil.getConnection();
            if (connection.getMetaData().getDriverName().contains("Oracle")) {
                offset = start + offset;
            }
            sqlQuery = sqlQuery.replace("$1", sortColumn);
            sqlQuery = sqlQuery.replace("$2", sortOrder);
            prepStmt = connection.prepareStatement(sqlQuery);
            prepStmt.setInt(1, tenantId);
            prepStmt.setString(2, "%" + searchApplication + "%");
            prepStmt.setInt(3, start);
            prepStmt.setInt(4, offset);
            rs = prepStmt.executeQuery();
            Application application;
            while (rs.next()) {
                String applicationName = rs.getString("NAME");
                String subscriberName = rs.getString("CREATED_BY");
                Subscriber subscriber = new Subscriber(subscriberName);
                application = new Application(applicationName, subscriber);
                application.setName(applicationName);
                application.setId(rs.getInt("APPLICATION_ID"));
                application.setUUID(rs.getString("UUID"));
                application.setGroupId(rs.getString("GROUP_ID"));
                subscriber.setTenantId(rs.getInt("TENANT_ID"));
                subscriber.setId(rs.getInt("SUBSCRIBER_ID"));
                application.setStatus(rs.getString("APPLICATION_STATUS"));
                application.setOwner(subscriberName);
                applicationList.add(application);
            }
        } catch (SQLException e) {
            handleException("Error while obtaining details of the Application for tenant id : " + tenantId, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
        }
        return applicationList;
    }

    public int getApplicationsCount(int tenantId, String searchOwner, String searchApplication) throws
            APIManagementException {
        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet resultSet = null;
        String sqlQuery = null;
        try {
            connection = APIMgtDBUtil.getConnection();
            sqlQuery = SQLConstants.GET_APPLICATIONS_COUNT;
            prepStmt = connection.prepareStatement(sqlQuery);
            prepStmt.setInt(1, tenantId);
            prepStmt.setString(2, "%" + searchOwner + "%");
            prepStmt.setString(3, "%" + searchApplication + "%");
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
            handleException("Failed to get application count of tenant id : " + tenantId, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, resultSet);
        }
        return 0;
    }

    public Application[] getAllApplicationsOfTenantForMigration(String appTenantDomain) throws
            APIManagementException {

        Connection connection;
        PreparedStatement prepStmt = null;
        ResultSet rs;
        Application[] applications = null;
        String sqlQuery = SQLConstants.GET_SIMPLE_APPLICATIONS;

        String tenantFilter = "AND SUB.TENANT_ID=?";
        sqlQuery += tenantFilter ;
        try {
            connection = APIMgtDBUtil.getConnection();

            int appTenantId = APIUtil.getTenantIdFromTenantDomain(appTenantDomain);
            prepStmt = connection.prepareStatement(sqlQuery);
            prepStmt.setInt(1, appTenantId);
            rs = prepStmt.executeQuery();

            ArrayList<Application> applicationsList = new ArrayList<Application>();
            Application application;
            while (rs.next()) {
                application = new Application(Integer.parseInt(rs.getString("APPLICATION_ID")));
                application.setName(rs.getString("NAME"));
                application.setOwner(rs.getString("CREATED_BY"));
                applicationsList.add(application);
            }
            applications = applicationsList.toArray(new Application[applicationsList.size()]);
        } catch (SQLException e) {
            handleException("Error when reading the application information from the persistence store.", e);
        } finally {
            if (prepStmt != null) {
                try {
                    prepStmt.close();
                } catch (SQLException e) {
                    log.warn("Database error. Could not close Statement. Continuing with others." + e.getMessage(), e);
                }
            }
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
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
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

        boolean transactionCompleted = true;
        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            prepStmt = connection.prepareStatement(getSubscriptionsQuery);
            prepStmt.setInt(1, application.getId());
            rs = prepStmt.executeQuery();

            if (multiGroupAppSharingEnabled) {
                transactionCompleted = updateGroupIDMappings(connection, application.getId(), null, null);
            }

            List<Integer> subscriptions = new ArrayList<Integer>();
            while (rs.next()) {
                subscriptions.add(rs.getInt("SUBSCRIPTION_ID"));
            }

            prepStmtGetConsumerKey = connection.prepareStatement(getConsumerKeyQuery);
            prepStmtGetConsumerKey.setInt(1, application.getId());
            rs = prepStmtGetConsumerKey.executeQuery();

            deleteDomainApp = connection.prepareStatement(deleteDomainAppQuery);
            while (rs.next()) {
                String consumerKey = rs.getString(APIConstants.FIELD_CONSUMER_KEY);
                String keyManagerName = rs.getString("KEY_MANAGER");
                // This is true when OAuth app has been created by pasting consumer key/secret in the screen.
                String mode = rs.getString("CREATE_MODE");
                if (consumerKey != null) {
                    deleteDomainApp.setString(1, consumerKey);
                    deleteDomainApp.addBatch();
                    KeyManager keyManager = KeyManagerHolder.getKeyManagerInstance(tenantDomain, keyManagerName);
                    if (keyManager != null) {
                        try {
                            keyManager.deleteMappedApplication(consumerKey);
                        } catch (APIManagementException e) {
                            log.error("Error while Deleting Client Application", e);
                        }
                    } else {
                        KeyManagerConfigurationDTO config = getKeyManagerConfigurationByUUID(keyManagerName);
                        if (config != null) {
                            keyManagerName = config.getName();
                            keyManager = KeyManagerHolder.getKeyManagerInstance(tenantDomain, keyManagerName);
                            try {
                                keyManager.deleteMappedApplication(consumerKey);
                            } catch (APIManagementException e) {
                                log.error("Error while Deleting Client Application", e);
                            }
                        }
                    }
                    // OAuth app is deleted if only it has been created from API Store. For mapped clients we don't
                    // call delete.
                    if (!APIConstants.OAuthAppMode.MAPPED.name().equals(mode)) {
                        //delete on oAuthorization server.
                        if (log.isDebugEnabled()) {
                            log.debug("Deleting Oauth application with consumer key " + consumerKey + " from the Oauth server");
                        }
                        if (keyManager != null){
                            try {
                                keyManager.deleteApplication(consumerKey);
                            } catch (APIManagementException e) {
                                log.error("Error while Deleting Client Application", e);
                            }

                        }
                    }
                }
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

            if (transactionCompleted) {
                connection.commit();
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

    /**
     * Retrieves the consumer keys and keymanager in a given application
     * @param appId application id
     * @return HashMap<ConsumerKey, keyManager>
     * @throws APIManagementException
     */
    public HashMap<String, String> getConsumerKeysForApplication(int appId) throws APIManagementException {

        HashMap<String, String> consumerKeysOfApplication = new HashMap<>();
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement preparedStatement = connection
                     .prepareStatement(SQLConstants.GET_CONSUMER_KEY_OF_APPLICATION_SQL)) {
            preparedStatement.setInt(1, appId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String consumerKey = resultSet.getString("CONSUMER_KEY");
                    String keyManager = resultSet.getString("KEY_MANAGER");

                    KeyManagerConfigurationDTO keyManagerConfiguration = getKeyManagerConfigurationByUUID(keyManager);
                    if (keyManagerConfiguration != null) {
                        keyManager = keyManagerConfiguration.getName();
                    }
                    consumerKeysOfApplication.put(consumerKey, keyManager);
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while getting consumer keys for application " + appId;
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        }
        return consumerKeysOfApplication;
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

    public String getConsumerKeyByApplicationIdKeyTypeKeyManager(int applicationId, String keyType, String keyManager)
            throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement preparedStatement = connection
                     .prepareStatement(SQLConstants.GET_CONSUMER_KEY_FOR_APPLICATION_KEY_TYPE_APP_ID_KEY_MANAGER_SQL)) {
            preparedStatement.setInt(1, applicationId);
            preparedStatement.setString(2, keyType);
            preparedStatement.setString(3, keyManager);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("CONSUMER_KEY");
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while retreving consumer key for application" + applicationId + " keyType " +
                    keyType + " Key Manager " + keyManager;
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        }
        return null;
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
     *  @param applicationId
     * @param tokenType
     */
    public void deleteApplicationKeyMappingByApplicationIdAndType(int applicationId, String tokenType)
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
            ps.setInt(1, applicationId);
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
     * @param keyManagerName
     * @throws APIManagementException if failed to delete the record.
     */
    public void deleteApplicationRegistration(int applicationId, String tokenType, String keyManagerName) throws APIManagementException {

        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            String deleteRegistrationEntry = SQLConstants.REMOVE_FROM_APPLICATION_REGISTRANTS_SQL;

            if (log.isDebugEnabled()) {
                log.debug("trying to delete a record from AM_APPLICATION_REGISTRATION table by application ID " +
                        applicationId + " and Token type" + tokenType);
            }
            ps = connection.prepareStatement(deleteRegistrationEntry);
            ps.setInt(1, applicationId);
            ps.setString(2, tokenType);
            ps.setString(3,keyManagerName);
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

        if (oldStatus == null && !newStatus.equals(APIConstants.CREATED)) {
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

    public void updateDefaultAPIPublishedVersion(APIIdentifier identifier, String oldStatus, String newStatus)
            throws APIManagementException {

        Connection conn = null;
        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);

            if (!oldStatus.equals(newStatus)) {
                if ((APIConstants.CREATED.equals(newStatus) || APIConstants.RETIRED.equals(newStatus)) && (
                        APIConstants.PUBLISHED.equals(oldStatus) || APIConstants.DEPRECATED.equals(oldStatus)
                                || APIConstants.BLOCKED.equals(oldStatus))) {
                    setPublishedDefVersion(identifier, conn, null);
                } else if (APIConstants.PUBLISHED.equals(newStatus) || APIConstants.DEPRECATED.equals(newStatus)
                        || APIConstants.BLOCKED.equals(newStatus)) {
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

    public void makeKeysForwardCompatible(ApiTypeWrapper apiTypeWrapper, String oldVersion) throws APIManagementException {
        Connection connection = null;
        PreparedStatement prepStmt = null;
        PreparedStatement addSubKeySt = null;
        PreparedStatement getAppSt = null;
        ResultSet rs = null;

        String getSubscriptionDataQuery = SQLConstants.GET_SUBSCRIPTION_DATA_SQL;
        String addSubKeyMapping = SQLConstants.ADD_SUBSCRIPTION_KEY_MAPPING_SQL;
        String getApplicationDataQuery = SQLConstants.GET_APPLICATION_DATA_SQL;

        APIIdentifier apiIdentifier = apiTypeWrapper.getApi().getId();
        int tenantId = APIUtil.getTenantId(APIUtil.replaceEmailDomainBack(apiIdentifier.getProviderName()));
        try {
            // Retrieve all the existing subscription for the old version
            connection = APIMgtDBUtil.getConnection();

            prepStmt = connection.prepareStatement(getSubscriptionDataQuery);
            prepStmt.setString(1, APIUtil.replaceEmailDomainBack(apiIdentifier.getProviderName()));
            prepStmt.setString(2, apiIdentifier.getApiName());
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
                info.subscriptionStatus = rs.getString("SUB_STATUS");
                subscriptionData.add(info);
            }

            Map<Integer, Integer> subscriptionIdMap = new HashMap<Integer, Integer>();

            for (SubscriptionInfo info : subscriptionData) {
                try {
                    if (!subscriptionIdMap.containsKey(info.subscriptionId)) {
                        String subscriptionStatus;
                        if (APIConstants.SubscriptionStatus.BLOCKED.equalsIgnoreCase(info.subscriptionStatus)) {
                            subscriptionStatus = APIConstants.SubscriptionStatus.BLOCKED;
                        } else if (APIConstants.SubscriptionStatus.UNBLOCKED.equalsIgnoreCase(info.subscriptionStatus)) {
                            subscriptionStatus = APIConstants.SubscriptionStatus.UNBLOCKED;
                        } else if (APIConstants.SubscriptionStatus.PROD_ONLY_BLOCKED.equalsIgnoreCase(info.subscriptionStatus)) {
                            subscriptionStatus = APIConstants.SubscriptionStatus.PROD_ONLY_BLOCKED;
                        } else if (APIConstants.SubscriptionStatus.REJECTED.equalsIgnoreCase(info.subscriptionStatus)) {
                            subscriptionStatus = APIConstants.SubscriptionStatus.REJECTED;
                        } else {
                            subscriptionStatus = APIConstants.SubscriptionStatus.ON_HOLD;
                        }
                        apiTypeWrapper.setTier(info.tierId);
                        int subscriptionId = addSubscription(apiTypeWrapper, info.applicationId, subscriptionStatus,
                                apiIdentifier.getProviderName());
                        if (subscriptionId == -1) {
                            String msg = "Unable to add a new subscription for the API: " + apiIdentifier.getName() +
                                    ":v" + apiIdentifier.getVersion();
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
                } catch (SubscriptionBlockedException e) {
                    log.info("Subscription is blocked: " + e.getMessage());
                }
            }

            getAppSt = connection.prepareStatement(getApplicationDataQuery);
            getAppSt.setString(1, APIUtil.replaceEmailDomainBack(apiIdentifier.getProviderName()));
            getAppSt.setString(2, apiIdentifier.getName());
            getAppSt.setString(3, oldVersion);
            rs = getAppSt.executeQuery();

            while (rs.next()) {
                int applicationId = rs.getInt("APPLICATION_ID");
                if (!subscribedApplications.contains(applicationId)) {
                    try {
                        String subscriptionStatus;
                        if (APIConstants.SubscriptionStatus.BLOCKED.equalsIgnoreCase(rs.getString("SUB_STATUS"))) {
                            subscriptionStatus = APIConstants.SubscriptionStatus.BLOCKED;
                        } else if (APIConstants.SubscriptionStatus.UNBLOCKED.equalsIgnoreCase(rs.getString("SUB_STATUS"))) {
                            subscriptionStatus = APIConstants.SubscriptionStatus.UNBLOCKED;
                        } else if (APIConstants.SubscriptionStatus.PROD_ONLY_BLOCKED.equalsIgnoreCase(rs.getString("SUB_STATUS"))) {
                            subscriptionStatus = APIConstants.SubscriptionStatus.PROD_ONLY_BLOCKED;
                        } else if (APIConstants.SubscriptionStatus.REJECTED.equalsIgnoreCase(rs.getString("SUB_STATUS"))) {
                            subscriptionStatus = APIConstants.SubscriptionStatus.REJECTED;
                        } else {
                            subscriptionStatus = APIConstants.SubscriptionStatus.ON_HOLD;
                        }
                        apiTypeWrapper.setTier(rs.getString("TIER_ID"));
                        int subscriptionId = addSubscription(apiTypeWrapper, applicationId, subscriptionStatus, apiIdentifier.getProviderName());
                        // catching the exception because when copy the api without the option "require re-subscription"
                        // need to go forward rather throwing the exception
                    } catch (SubscriptionAlreadyExistingException e) {
                        //Not handled as an error because same subscription can be there in many previous versions.
                        //Ex: if previous version was created by another older version and if the subscriptions are
                        //Forwarded, then the third one will get same subscription from previous two versions.
                        log.info("Subscription already exists: " + e.getMessage());
                    } catch (SubscriptionBlockedException e) {
                        //Not handled as an error because we cannot update subscriptions for an API with blocked subscriptions
                        //If previous version was created by another older version and if the subscriptions are
                        //Forwarded, by catching the exception we will continue checking the other subscriptions
                        log.info("Subscription is blocked: " + e.getMessage());
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

    /**
     * Add API metadata.
     *
     * @param api      API to add
     * @param tenantId tenant id
     * @return API Id of the successfully added API
     * @throws APIManagementException if fails to add API
     */
    public int addAPI(API api, int tenantId) throws APIManagementException {

        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        int apiId = -1;

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
            prepStmt.setString(9, api.getType());
            prepStmt.execute();

            rs = prepStmt.getGeneratedKeys();
            if (rs.next()) {
                apiId = rs.getInt(1);
            }

            connection.commit();

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
            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException ex) {
                // Rollback failed. Exception will be thrown later for upper exception
                log.error("Failed to rollback the add API: " + api.getId(), ex);
            }
            handleException("Error while adding the API: " + api.getId() + " to the database", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
        }
        return apiId;
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

            if (workflow.getMetadata() != null) {
                byte[] metadataByte = workflow.getMetadata().toJSONString().getBytes("UTF-8");
                prepStmt.setBinaryStream(9, new ByteArrayInputStream(metadataByte));
            } else {
                prepStmt.setNull(9, java.sql.Types.BLOB);
            }

            if (workflow.getProperties() != null) {
                byte[] propertiesByte = workflow.getProperties().toJSONString().getBytes("UTF-8");
                prepStmt.setBinaryStream(10, new ByteArrayInputStream(propertiesByte));
            } else {
                prepStmt.setNull(10, java.sql.Types.BLOB);
            }
            prepStmt.execute();
            connection.commit();
        } catch (SQLException | UnsupportedEncodingException e) {
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
            prepStmt.setString(3, workflowDTO.getExternalWorkflowReference());

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

    /**
     * Returns a workflow object for a given internal workflow reference and the workflow type.
     *
     * @param workflowReference
     * @param workflowType
     * @return
     * @throws APIManagementException
     */
    public WorkflowDTO retrieveWorkflowFromInternalReference(String workflowReference, String workflowType)
            throws APIManagementException {
        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        WorkflowDTO workflowDTO = null;

        String query = SQLConstants.GET_ALL_WORKFLOW_ENTRY_FROM_INTERNAL_REF_SQL;
        try {
            connection = APIMgtDBUtil.getConnection();
            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, workflowReference);
            prepStmt.setString(2, workflowType);

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
            String apistatus = api.getStatus();
            if (APIConstants.PUBLISHED.equals(apistatus) || APIConstants.DEPRECATED.equals(apistatus) || APIConstants
                    .BLOCKED.equals(apistatus)) {
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
     * Add URI Templates to database with resource scope mappings.
     *
     * @param apiId    API Id
     * @param api      API to add URI templates of
     * @param tenantId Tenant ID
     * @throws APIManagementException If an error occurs while adding URI templates.
     */
    public void addURITemplates(int apiId, API api, int tenantId) throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            connection.setAutoCommit(false);
            try {
                addURITemplates(apiId, api, tenantId, connection);
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                handleException("Error while adding URL template(s) to the database for API : " + api.getId(), e);
            }
        } catch (SQLException e) {
            handleException("Error while adding URL template(s) to the database for API : " + api.getId(), e);
        }
    }

    /**
     * Add URI Templates to database with resource scope mappings by passing the DB connection.
     *
     * @param apiId      API Id
     * @param api        API
     * @param tenantId   tenant Id
     * @param connection Existing DB Connection
     * @throws SQLException If a SQL error occurs while adding URI Templates
     */
    public void addURITemplates(int apiId, API api, int tenantId, Connection connection) throws SQLException {

        String dbProductName = connection.getMetaData().getDatabaseProductName();
        try (PreparedStatement uriMappingPrepStmt = connection.prepareStatement(SQLConstants.ADD_URL_MAPPING_SQL,
                new String[]{
                        DBUtils.getConvertedAutoGeneratedColumnName(dbProductName, "URL_MAPPING_ID")});
             PreparedStatement uriScopeMappingPrepStmt =
                     connection.prepareStatement(SQLConstants.ADD_API_RESOURCE_SCOPE_MAPPING)) {

            for (URITemplate uriTemplate : api.getUriTemplates()) {
                uriMappingPrepStmt.setInt(1, apiId);
                uriMappingPrepStmt.setString(2, uriTemplate.getHTTPVerb());
                uriMappingPrepStmt.setString(3, uriTemplate.getAuthType());
                uriMappingPrepStmt.setString(4, uriTemplate.getUriTemplate());
                //If API policy is available then set it for all the resources.
                if (StringUtils.isEmpty(api.getApiLevelPolicy())) {
                    uriMappingPrepStmt.setString(5, (StringUtils.isEmpty(uriTemplate.getThrottlingTier())) ?
                            APIConstants.UNLIMITED_TIER :
                            uriTemplate.getThrottlingTier());
                } else {
                    uriMappingPrepStmt.setString(5, (StringUtils.isEmpty(
                            api.getApiLevelPolicy())) ? APIConstants.UNLIMITED_TIER : api.getApiLevelPolicy());
                }
                InputStream is = null;
                if (uriTemplate.getMediationScript() != null) {
                    is = new ByteArrayInputStream(
                            uriTemplate.getMediationScript().getBytes(Charset.defaultCharset()));
                }
                if (connection.getMetaData().getDriverName().contains("PostgreSQL") || connection.getMetaData()
                        .getDatabaseProductName().contains("DB2")) {
                    if (uriTemplate.getMediationScript() != null) {
                        uriMappingPrepStmt.setBinaryStream(6, is, uriTemplate.getMediationScript()
                                .getBytes(Charset.defaultCharset()).length);
                    } else {
                        uriMappingPrepStmt.setBinaryStream(6, is, 0);
                    }
                } else {
                    uriMappingPrepStmt.setBinaryStream(6, is);
                }
                uriMappingPrepStmt.execute();
                int uriMappingId = -1;
                try (ResultSet resultIdSet = uriMappingPrepStmt.getGeneratedKeys()) {
                    while (resultIdSet.next()) {
                        uriMappingId = resultIdSet.getInt(1);
                    }
                }
                if (uriMappingId != -1) {
                    for (Scope uriTemplateScope : uriTemplate.retrieveAllScopes()) {
                        String scopeKey = uriTemplateScope.getKey();
                        if (log.isDebugEnabled()) {
                            log.debug("Adding scope to resource mapping for scope key: " + scopeKey +
                                    " and URL mapping Id: " + uriMappingId);
                        }
                        uriScopeMappingPrepStmt.setString(1, scopeKey);
                        uriScopeMappingPrepStmt.setInt(2, uriMappingId);
                        uriScopeMappingPrepStmt.setInt(3, tenantId);
                        uriScopeMappingPrepStmt.addBatch();
                    }
                }
            } // end URITemplate list iteration
            uriScopeMappingPrepStmt.executeBatch();
        }
    }

    /**
     * Checks whether application is accessible to the specified user
     *
     * @param applicationID ID of the Application
     * @param userId        Name of the User.
     * @param groupId       Group IDs
     * @throws APIManagementException
     */
    public boolean isAppAllowed(int applicationID, String userId, String groupId)
            throws APIManagementException {
        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;

        try {
            connection = APIMgtDBUtil.getConnection();

            String query = "SELECT APP.APPLICATION_ID FROM AM_SUBSCRIBER SUB, AM_APPLICATION APP";
            String whereClause = "  WHERE SUB.USER_ID =? AND APP.APPLICATION_ID=? AND " +
                    "SUB.SUBSCRIBER_ID=APP.SUBSCRIBER_ID";
            String whereClauseCaseInSensitive = "  WHERE LOWER(SUB.USER_ID) =LOWER(?) AND APP.APPLICATION_ID=? AND SUB"
                    + ".SUBSCRIBER_ID=APP.SUBSCRIBER_ID";
            String whereClauseWithGroupId = "  WHERE  (APP.GROUP_ID = ? OR ((APP.GROUP_ID='' OR APP.GROUP_ID IS NULL)"
                    + " AND SUB.USER_ID = ?)) AND " + "APP.APPLICATION_ID = ? AND SUB.SUBSCRIBER_ID = APP.SUBSCRIBER_ID";

            String whereClauseWithMultiGroupId = "  WHERE  ((APP.APPLICATION_ID IN (SELECT APPLICATION_ID  FROM " +
                    "AM_APPLICATION_GROUP_MAPPING WHERE GROUP_ID IN ($params) AND TENANT = ?))  OR   SUB.USER_ID = ? " +
                    "OR (APP.APPLICATION_ID IN (SELECT APPLICATION_ID FROM AM_APPLICATION WHERE GROUP_ID = ?))) " +
                    "AND APP.APPLICATION_ID = ? AND SUB.SUBSCRIBER_ID = APP.SUBSCRIBER_ID";

            if (!StringUtils.isEmpty(groupId) && !APIConstants.NULL_GROUPID_LIST.equals(groupId)) {
                if (multiGroupAppSharingEnabled) {
                    Subscriber subscriber = getSubscriber(userId);
                    String tenantDomain = MultitenantUtils.getTenantDomain(subscriber.getName());
                    query += whereClauseWithMultiGroupId;
                    String[] groupIds = groupId.split(",");
                    int parameterIndex = groupIds.length;

                    prepStmt = fillQueryParams(connection, query, groupIds, 1);
                    prepStmt.setString(++parameterIndex, tenantDomain);
                    prepStmt.setString(++parameterIndex, userId);
                    prepStmt.setString(++parameterIndex, tenantDomain + '/' + groupId);
                    prepStmt.setInt(++parameterIndex, applicationID);
                } else {
                    query += whereClauseWithGroupId;
                    prepStmt = connection.prepareStatement(query);
                    prepStmt.setString(1, groupId);
                    prepStmt.setString(2, userId);
                    prepStmt.setInt(3, applicationID);
                }
            } else {
                if (forceCaseInsensitiveComparisons) {
                    query += whereClauseCaseInSensitive;
                } else {
                    query += whereClause;
                }
                prepStmt = connection.prepareStatement(query);
                prepStmt.setString(1, userId);
                prepStmt.setInt(2, applicationID);
            }

            rs = prepStmt.executeQuery();
            while (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            handleException("Error while checking whether the application : " + applicationID + " is accessible " +
                    "to user " + userId, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
        }
        return false;
    }

    /**
     * Fetches an Application by name.
     *
     * @param applicationName Name of the Application
     * @param userId          Name of the User.
     * @param groupId         Group ID
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

        int applicationId = 0;
        Application application = null;
        try {
            connection = APIMgtDBUtil.getConnection();

            String query = SQLConstants.GET_APPLICATION_BY_NAME_PREFIX;
            String whereClause = "  WHERE SUB.USER_ID =? AND APP.NAME=? AND SUB.SUBSCRIBER_ID=APP.SUBSCRIBER_ID";
            String whereClauseCaseInSensitive = "  WHERE LOWER(SUB.USER_ID) =LOWER(?) AND APP.NAME=? AND SUB" + "" +
                    ".SUBSCRIBER_ID=APP.SUBSCRIBER_ID";
            String whereClauseWithGroupId = "  WHERE  (APP.GROUP_ID = ? OR ((APP.GROUP_ID='' OR APP.GROUP_ID IS NULL)"
                    + " AND SUB.USER_ID = ?)) AND " + "APP.NAME = ? AND SUB.SUBSCRIBER_ID = APP.SUBSCRIBER_ID";
            String whereClauseWithGroupIdCaseInSensitive =
                    "  WHERE  (APP.GROUP_ID = ? OR ((APP.GROUP_ID='' OR APP.GROUP_ID IS NULL)"
                            + " AND LOWER(SUB.USER_ID) = LOWER(?))) AND "
                            + "APP.NAME = ? AND SUB.SUBSCRIBER_ID = APP.SUBSCRIBER_ID";

            String whereClauseWithMultiGroupId = "  WHERE  ((APP.APPLICATION_ID IN (SELECT APPLICATION_ID  FROM " +
                    "AM_APPLICATION_GROUP_MAPPING WHERE GROUP_ID IN ($params) AND TENANT = ?))  OR   SUB.USER_ID = ? " +
                    "OR (APP.APPLICATION_ID IN (SELECT APPLICATION_ID FROM AM_APPLICATION WHERE GROUP_ID = ?))) " +
                    "AND APP.NAME = ? AND SUB.SUBSCRIBER_ID = APP.SUBSCRIBER_ID";
            String whereClauseWithMultiGroupIdCaseInSensitive =
                    "  WHERE  ((APP.APPLICATION_ID IN (SELECT APPLICATION_ID  FROM "
                    + "AM_APPLICATION_GROUP_MAPPING WHERE GROUP_ID IN ($params) AND TENANT = ?))  "
                    + "OR   LOWER(SUB.USER_ID) = LOWER(?)  "
                    + "OR (APP.APPLICATION_ID IN (SELECT APPLICATION_ID FROM AM_APPLICATION WHERE GROUP_ID = ?))) "
                    + "AND APP.NAME = ? AND SUB.SUBSCRIBER_ID = APP.SUBSCRIBER_ID";

            if (groupId != null && !"null".equals(groupId) && !groupId.isEmpty()) {
                if (multiGroupAppSharingEnabled) {
                    Subscriber subscriber = getSubscriber(userId);
                    String tenantDomain = MultitenantUtils.getTenantDomain(subscriber.getName());
                    if (forceCaseInsensitiveComparisons) {
                        query = query + whereClauseWithMultiGroupIdCaseInSensitive;
                    } else {
                        query = query + whereClauseWithMultiGroupId;
                    }
                    String[] groupIds = groupId.split(",");
                    int parameterIndex = groupIds.length;

                    prepStmt = fillQueryParams(connection, query, groupIds, 1);
                    prepStmt.setString(++parameterIndex, tenantDomain);
                    prepStmt.setString(++parameterIndex, userId);
                    prepStmt.setString(++parameterIndex, tenantDomain + '/' + groupId);
                    prepStmt.setString(++parameterIndex, applicationName);
                } else {
                    if (forceCaseInsensitiveComparisons) {
                        query = query + whereClauseWithGroupIdCaseInSensitive;
                    } else {
                        query = query + whereClauseWithGroupId;
                    }
                    prepStmt = connection.prepareStatement(query);
                    prepStmt.setString(1, groupId);
                    prepStmt.setString(2, userId);
                    prepStmt.setString(3, applicationName);
                }
            } else {
                if (forceCaseInsensitiveComparisons) {
                    query = query + whereClauseCaseInSensitive;
                } else {
                    query = query + whereClause;
                }
                prepStmt = connection.prepareStatement(query);
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

                application.setOwner(rs.getString("CREATED_BY"));
                application.setDescription(rs.getString("DESCRIPTION"));
                application.setStatus(rs.getString("APPLICATION_STATUS"));
                application.setCallbackUrl(rs.getString("CALLBACK_URL"));
                applicationId = rs.getInt("APPLICATION_ID");
                application.setId(applicationId);
                application.setTier(rs.getString("APPLICATION_TIER"));
                application.setUUID(rs.getString("UUID"));
                application.setGroupId(rs.getString("GROUP_ID"));
                application.setOwner(rs.getString("CREATED_BY"));
                application.setTokenType(rs.getString("TOKEN_TYPE"));

                if (multiGroupAppSharingEnabled) {
                    setGroupIdInApplication(application);
                }
                if (application != null) {
                    Map<String, String> applicationAttributes = getApplicationAttributes(connection, applicationId);
                    application.setApplicationAttributes(applicationAttributes);
                }
            }
        } catch (SQLException e) {
            handleException("Error while obtaining details of the Application : " + applicationName, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
        }
        return application;
    }

    private void setGroupIdInApplication(Application application) throws APIManagementException {
        String applicationGroupId = application.getGroupId();
        if (StringUtils.isEmpty(applicationGroupId)) { // No migrated App groupId
            application.setGroupId(getGroupId(application.getId()));
        } else {
            // Migrated data exists where Group ID for this App has been stored in AM_APPLICATION table
            // in the format 'tenant/groupId', so extract groupId value and store it in the App object
            String[] split = applicationGroupId.split("/");
            if (split.length == 2) {
                application.setGroupId(split[1]);
            } else {
                log.error("Migrated Group ID: " + applicationGroupId +
                        "does not follow the expected format 'tenant/groupId'");
            }
        }
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

                application.setOwner(rs.getString("CREATED_BY"));
                application.setDescription(rs.getString("DESCRIPTION"));
                application.setStatus(rs.getString("APPLICATION_STATUS"));
                application.setCallbackUrl(rs.getString("CALLBACK_URL"));
                application.setId(rs.getInt("APPLICATION_ID"));
                application.setGroupId(rs.getString("GROUP_ID"));
                application.setUUID(rs.getString("UUID"));
                application.setTier(rs.getString("APPLICATION_TIER"));
                application.setTokenType(rs.getString("TOKEN_TYPE"));
                subscriber.setId(rs.getInt("SUBSCRIBER_ID"));

                String tenantDomain = MultitenantUtils.getTenantDomain(subscriberName);
                Map<String, Map<String, OAuthApplicationInfo>>
                        keyMap = getOAuthApplications(tenantDomain, application.getId());
                application.getKeyManagerWiseOAuthApp().putAll(keyMap);

                if (multiGroupAppSharingEnabled) {
                    if (application.getGroupId() == null || application.getGroupId().isEmpty()) {
                        application.setGroupId(getGroupId(applicationId));
                    }
                }
            }
            if (application != null) {
                Map<String,String> applicationAttributes = getApplicationAttributes(connection, applicationId);
                application.setApplicationAttributes(applicationAttributes);
            }
        } catch (SQLException e) {
            handleException("Error while obtaining details of the Application : " + applicationId, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
        }
        return application;
    }

    public Application getLightweightApplicationById(int applicationId) throws APIManagementException {
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

                application.setOwner(rs.getString("CREATED_BY"));
                application.setDescription(rs.getString("DESCRIPTION"));
                application.setStatus(rs.getString("APPLICATION_STATUS"));
                application.setCallbackUrl(rs.getString("CALLBACK_URL"));
                application.setId(rs.getInt("APPLICATION_ID"));
                application.setGroupId(rs.getString("GROUP_ID"));
                application.setUUID(rs.getString("UUID"));
                application.setTier(rs.getString("APPLICATION_TIER"));
                application.setTokenType(rs.getString("TOKEN_TYPE"));
                subscriber.setId(rs.getInt("SUBSCRIBER_ID"));

                if (multiGroupAppSharingEnabled) {
                    if (application.getGroupId() == null || application.getGroupId().isEmpty()) {
                        application.setGroupId(getGroupId(applicationId));
                    }
                }
            }

        } catch (SQLException e) {
            handleException("Error while obtaining details of the Application : " + applicationId, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
        }
        return application;
    }

    public Application getApplicationById(int applicationId, String userId, String groupId) throws APIManagementException {

        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        Application application = null;
        try {
            connection = APIMgtDBUtil.getConnection();
            String query = SQLConstants.GET_APPLICATION_BY_ID_SQL;

            String whereClause = "  AND SUB.USER_ID =?";
            String whereClauseCaseInSensitive = "  AND LOWER(SUB.USER_ID) =LOWER(?)";
            String whereClauseWithGroupId = "  AND  (APP.GROUP_ID = ? OR ((APP.GROUP_ID='' OR APP.GROUP_ID IS NULL)"
                    + " AND SUB.USER_ID = ?))";
            String whereClauseWithGroupIdCaseInSensitive = "  AND  (APP.GROUP_ID = ? OR ((APP.GROUP_ID='' OR APP.GROUP_ID IS NULL)"
                    + " AND LOWER(SUB.USER_ID) = LOWER(?)))";

            String whereClauseWithMultiGroupId = "  AND  ((APP.APPLICATION_ID IN (SELECT APPLICATION_ID  FROM " +
                    "AM_APPLICATION_GROUP_MAPPING WHERE GROUP_ID IN ($params) AND TENANT = ?))  OR   SUB.USER_ID = ? )";
            String whereClauseWithMultiGroupIdCaseInSensitive = "  AND  ((APP.APPLICATION_ID IN (SELECT APPLICATION_ID  FROM " +
                    "AM_APPLICATION_GROUP_MAPPING WHERE GROUP_ID IN ($params) AND TENANT = ?))  OR   LOWER(SUB.USER_ID) = LOWER(?) )";

            if (groupId != null && !"null".equals(groupId) && !groupId.isEmpty()) {
                if (multiGroupAppSharingEnabled) {
                    Subscriber subscriber = getSubscriber(userId);
                    String tenantDomain = MultitenantUtils.getTenantDomain(subscriber.getName());
                    if (forceCaseInsensitiveComparisons) {
                        query = query + whereClauseWithMultiGroupIdCaseInSensitive;
                    } else {
                        query = query + whereClauseWithMultiGroupId;
                    }
                    String[] groupIds = groupId.split(",");
                    int parameterIndex = groupIds.length + 1; //since index 1 is applicationId
                    // query params will fil from 2
                    prepStmt = fillQueryParams(connection, query, groupIds, 2);
                    prepStmt.setString(++parameterIndex, tenantDomain);
                    prepStmt.setInt(1, applicationId);
                    prepStmt.setString(++parameterIndex, userId);
                } else {
                    if (forceCaseInsensitiveComparisons) {
                        query = query + whereClauseWithGroupIdCaseInSensitive;
                    } else {
                        query = query + whereClauseWithGroupId;
                    }
                    prepStmt = connection.prepareStatement(query);
                    prepStmt.setInt(1, applicationId);
                    prepStmt.setString(2, groupId);
                    prepStmt.setString(3, userId);
                }
            } else {
                if (forceCaseInsensitiveComparisons) {
                    query = query + whereClauseCaseInSensitive;
                } else {
                    query = query + whereClause;
                }
                prepStmt = connection.prepareStatement(query);
                prepStmt.setInt(1, applicationId);
                prepStmt.setString(2, userId);
            }
            rs = prepStmt.executeQuery();
            if (rs.next()) {
                String applicationName = rs.getString("NAME");
                String subscriberId = rs.getString("SUBSCRIBER_ID");
                String subscriberName = rs.getString("USER_ID");

                Subscriber subscriber = new Subscriber(subscriberName);
                subscriber.setId(Integer.parseInt(subscriberId));
                application = new Application(applicationName, subscriber);

                application.setOwner(rs.getString("CREATED_BY"));
                application.setDescription(rs.getString("DESCRIPTION"));
                application.setStatus(rs.getString("APPLICATION_STATUS"));
                application.setCallbackUrl(rs.getString("CALLBACK_URL"));
                application.setId(rs.getInt("APPLICATION_ID"));
                application.setGroupId(rs.getString("GROUP_ID"));
                application.setUUID(rs.getString("UUID"));
                application.setTier(rs.getString("APPLICATION_TIER"));
                subscriber.setId(rs.getInt("SUBSCRIBER_ID"));

                String tenantDomain = MultitenantUtils.getTenantDomain(subscriberName);
                Map<String, Map<String, OAuthApplicationInfo>>
                        keyMap = getOAuthApplications(tenantDomain, application.getId());
                application.getKeyManagerWiseOAuthApp().putAll(keyMap);

                if (multiGroupAppSharingEnabled) {
                    if (application.getGroupId() == null || application.getGroupId().isEmpty()) {
                        application.setGroupId(getGroupId(applicationId));
                    }
                }
            }

            if (application != null) {
                Map<String,String> applicationAttributes = getApplicationAttributes(connection, applicationId);
                application.setApplicationAttributes(applicationAttributes);
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
        int applicationId = 0;

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
                applicationId = rs.getInt("APPLICATION_ID");
                application.setId(applicationId);
                application.setGroupId(rs.getString("GROUP_ID"));
                application.setUUID(rs.getString("UUID"));
                application.setTier(rs.getString("APPLICATION_TIER"));
                application.setTokenType(rs.getString("TOKEN_TYPE"));
                application.setOwner(rs.getString("CREATED_BY"));
                subscriber.setId(rs.getInt("SUBSCRIBER_ID"));

                if (multiGroupAppSharingEnabled) {
                    if (application.getGroupId() == null || application.getGroupId().isEmpty()) {
                        application.setGroupId(getGroupId(application.getId()));
                    }
                }

                int subscriptionCount = getSubscriptionCountByApplicationId(subscriber, applicationId,
                        application.getGroupId());
                application.setSubscriptionCount(subscriptionCount);

                Timestamp createdTime = rs.getTimestamp("CREATED_TIME");
                application.setCreatedTime(createdTime == null ? null : String.valueOf(createdTime.getTime()));
                try {
                    Timestamp updated_time = rs.getTimestamp("UPDATED_TIME");
                    application.setLastUpdatedTime(
                            updated_time == null ? null : String.valueOf(updated_time.getTime()));
                } catch (SQLException e) {
                    // fixing Timestamp issue with default value '0000-00-00 00:00:00'for existing applications created
                    application.setLastUpdatedTime(application.getCreatedTime());
                }
            }
            // Get custom attributes of application
            if (application != null) {
                Map<String, String> applicationAttributes = getApplicationAttributes(connection, applicationId);
                application.setApplicationAttributes(applicationAttributes);
            }
        } catch (SQLException e) {
            handleException("Error while obtaining details of the Application : " + uuid, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
        }
        return application;
    }

    /**
     * Update URI templates define for an API.
     *
     * @param api      API to update
     * @param tenantId tenant Id
     * @throws APIManagementException if fails to update URI template of the API.
     */
    public void updateURITemplates(API api, int tenantId) throws APIManagementException {

        int apiId;
        String deleteOldMappingsQuery = SQLConstants.REMOVE_FROM_URI_TEMPLATES_SQL;
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(deleteOldMappingsQuery)) {
            connection.setAutoCommit(false);
            apiId = getAPIID(api.getId(), connection);
            prepStmt.setInt(1, apiId);
            try {
                prepStmt.execute();
                addURITemplates(apiId, api, tenantId, connection);
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                handleException("Error while deleting URL template(s) for API : " + api.getId(), e);
            }
        } catch (SQLException e) {
            handleException("Error while deleting URL template(s) for API : " + api.getId(), e);
        }
    }

    /**
     * Get resource (URI Template) to scope mappings of the given API.
     *
     * @param identifier API Identifier
     * @return Map of URI template ID to Scope Keys
     * @throws APIManagementException if an error occurs while getting resource to scope mapping of the API
     */
    public HashMap<Integer, Set<String>> getResourceToScopeMapping(APIIdentifier identifier)
            throws APIManagementException {

        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        HashMap<Integer, Set<String>> scopeToResourceMap = new HashMap<>();
        int apiId;
        try {
            String sqlQuery = SQLConstants.GET_RESOURCE_TO_SCOPE_MAPPING_SQL;
            apiId = getAPIID(identifier, conn);

            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            ps.setInt(1, apiId);
            resultSet = ps.executeQuery();
            while (resultSet.next()) {
                int urlMappingId = resultSet.getInt(1);
                String scopeKey = resultSet.getString(2);
                if (scopeToResourceMap.containsKey(urlMappingId)) {
                    if (!StringUtils.isEmpty(scopeKey)) {
                        scopeToResourceMap.get(urlMappingId).add(scopeKey);
                    }
                } else {
                    Set<String> scopeSet = new HashSet<>();
                    if (!StringUtils.isEmpty(scopeKey)) {
                        scopeSet.add(scopeKey);
                    }
                    scopeToResourceMap.put(urlMappingId, scopeSet);
                }
            }
        } catch (SQLException e) {
            handleException("Failed to retrieve api resource scope mappings ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, resultSet);
        }
        return scopeToResourceMap;
    }

    /**
     * returns all URL templates define for all active(PUBLISHED) APIs.
     */
    public ArrayList<URITemplate> getAllURITemplates(String apiContext, String version) throws APIManagementException {

        return getAllURITemplatesAdvancedThrottle(apiContext, version);

    }

    public ArrayList<URITemplate> getAPIProductURITemplates(String apiContext, String version)
                                                                    throws APIManagementException {
        return getAPIProductURITemplatesAdvancedThrottle(apiContext, version);
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
                uriTemplate.setId(rs.getInt("URL_MAPPING_ID"));
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
        int tenantId;
        ArrayList<URITemplate> uriTemplates = new ArrayList<>();

        String apiTenantDomain = MultitenantUtils.getTenantDomainFromRequestURL(apiContext);
        if (apiTenantDomain != null) {
            tenantId = APIUtil.getTenantIdFromTenantDomain(apiTenantDomain);
        } else {
            tenantId = MultitenantConstants.SUPER_TENANT_ID;
        }

        // TODO : FILTER RESULTS ONLY FOR ACTIVE APIs
        String query = SQLConstants.ThrottleSQLConstants.GET_CONDITION_GROUPS_FOR_POLICIES_SQL;
        try {
            connection = APIMgtDBUtil.getConnection();
            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, apiContext);
            prepStmt.setString(2, version);
            prepStmt.setInt(3, tenantId);

            rs = prepStmt.executeQuery();

            uriTemplates = extractURITemplates(rs);
        } catch (SQLException e) {
            handleException("Error while fetching all URL Templates", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
        }

        return uriTemplates;
    }


    public ArrayList<URITemplate> getAPIProductURITemplatesAdvancedThrottle(String apiContext, String version)
            throws APIManagementException {
        int tenantId;
        ArrayList<URITemplate> uriTemplates = new ArrayList<>();

        String apiTenantDomain = MultitenantUtils.getTenantDomainFromRequestURL(apiContext);
        if (apiTenantDomain != null) {
            tenantId = APIUtil.getTenantIdFromTenantDomain(apiTenantDomain);
        } else {
            tenantId = MultitenantConstants.SUPER_TENANT_ID;
        }

        // TODO : FILTER RESULTS ONLY FOR ACTIVE APIs
        String query = SQLConstants.ThrottleSQLConstants.GET_CONDITION_GROUPS_FOR_POLICIES_IN_PRODUCTS_SQL;
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            prepStmt.setString(1, apiContext);
            prepStmt.setString(2, version);
            prepStmt.setInt(3, tenantId);

            try (ResultSet rs = prepStmt.executeQuery()) {
                uriTemplates = extractURITemplates(rs);
            }
        } catch (SQLException e) {
            handleException("Error while fetching all URL Templates", e);
        }

        return uriTemplates;
    }

    private ArrayList<URITemplate> extractURITemplates(ResultSet rs) throws SQLException, APIManagementException {
        Map<String, Set<ConditionGroupDTO>> mapByHttpVerbURLPatternToId = new HashMap<String, Set<ConditionGroupDTO>>();
        ArrayList<URITemplate> uriTemplates = new ArrayList<URITemplate>();

        while (rs != null && rs.next()) {
            int uriTemplateId = rs.getInt("URL_MAPPING_ID");
            String httpVerb = rs.getString("HTTP_METHOD");
            String authType = rs.getString("AUTH_SCHEME");
            String urlPattern = rs.getString("URL_PATTERN");
            String policyName = rs.getString("THROTTLING_TIER");
            String conditionGroupId = rs.getString("CONDITION_GROUP_ID");
            String applicableLevel = rs.getString("APPLICABLE_LEVEL");
            String policyConditionGroupId = "_condition_" + conditionGroupId;
            boolean isContentAware = PolicyConstants.BANDWIDTH_TYPE.equals(
                    rs.getString(ThrottlePolicyConstants.COLUMN_DEFAULT_QUOTA_POLICY_TYPE));

            String key = httpVerb + ":" + urlPattern;
            if (mapByHttpVerbURLPatternToId.containsKey(key)) {

                if (StringUtils.isEmpty(conditionGroupId)) {
                    continue;
                }

                // Converting ConditionGroup to a lightweight ConditionGroupDTO.
                ConditionGroupDTO groupDTO = createConditionGroupDTO(Integer.parseInt(conditionGroupId));
                groupDTO.setConditionGroupId(policyConditionGroupId);
                mapByHttpVerbURLPatternToId.get(key).add(groupDTO);
            } else {
                String script = null;
                URITemplate uriTemplate = new URITemplate();
                uriTemplate.setId(uriTemplateId);
                uriTemplate.setThrottlingTier(policyName);
                uriTemplate.setThrottlingTiers(
                        policyName + PolicyConstants.THROTTLING_TIER_CONTENT_AWARE_SEPERATOR + isContentAware);
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

        return uriTemplates;
    }

    /**
     * This method is used to get the API provider by giving API name, API version and tenant domain
     *
     * @param apiName    API name
     * @param apiVersion API version
     * @param tenant     tenant domain
     * @return API provider
     * @throws APIManagementException if failed to get the API provider by giving API name, API version, tenant domain
     */
    public String getAPIProviderByNameAndVersion(String apiName, String apiVersion, String tenant)
            throws APIManagementException {
        if (StringUtils.isBlank(apiName) || StringUtils.isBlank(apiVersion) || StringUtils.isBlank(tenant)) {
            String msg = "API name, version, tenant cannot be null when fetching provider";
            log.error(msg);
            throw new APIManagementException(msg);
        }

        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        String apiProvider = null;
        String getAPIProviderQuery = null;

        try(Connection connection = APIMgtDBUtil.getConnection()) {
            if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equalsIgnoreCase(tenant)) {
                //in this case, the API should be fetched from super tenant
                getAPIProviderQuery = SQLConstants.GET_API_PROVIDER_WITH_NAME_VERSION_FOR_SUPER_TENANT;
                prepStmt = connection.prepareStatement(getAPIProviderQuery);
            } else {
                //in this case, the API should be fetched from the respective tenant
                getAPIProviderQuery = SQLConstants.GET_API_PROVIDER_WITH_NAME_VERSION_FOR_GIVEN_TENANT;
                prepStmt = connection.prepareStatement(getAPIProviderQuery);
                prepStmt.setString(3, "%" + tenant + "%");
            }
            prepStmt.setString(1, apiName);
            prepStmt.setString(2, apiVersion);
            rs = prepStmt.executeQuery();
            if (rs.next()) {
                apiProvider = rs.getString("API_PROVIDER");
            }
            if (StringUtils.isBlank(apiProvider)) {
                String msg = "Unable to find provider for API: " + apiName + " in the database";
                log.warn(msg);
            }
        } catch (SQLException e) {
            handleException("Error while locating API: " + apiName + " from the database", e);
        }
        return apiProvider;
    }

    /**
     * Converts an {@code Pipeline} object into a {@code ConditionGroupDTO}.{@code ConditionGroupDTO} class tries to
     * contain the same information held by  {@code Pipeline}, but in a much lightweight fashion.
     *
     * @param conditionGroup Id of the condition group ({@code Pipeline}) to be converted
     * @return An object of {@code ConditionGroupDTO} type.
     * @throws APIManagementException
     */
    public ConditionGroupDTO createConditionGroupDTO(int conditionGroup) throws APIManagementException {
        List<Condition> conditions = getConditions(conditionGroup);
        ArrayList<ConditionDTO> conditionDTOs = new ArrayList<ConditionDTO>(conditions.size());
        for (Condition condition : conditions) {
            ConditionDTO conditionDTO = new ConditionDTO();
            conditionDTO.setConditionType(condition.getType());

            conditionDTO.isInverted(condition.isInvertCondition());
            if (PolicyConstants.IP_RANGE_TYPE.equals(condition.getType())) {
                IPCondition ipRangeCondition = (IPCondition) condition;
                conditionDTO.setConditionName(ipRangeCondition.getStartingIP());
                conditionDTO.setConditionValue(ipRangeCondition.getEndingIP());

            } else if (PolicyConstants.IP_SPECIFIC_TYPE.equals(condition.getType())) {
                IPCondition ipCondition = (IPCondition) condition;
                conditionDTO.setConditionName(PolicyConstants.IP_SPECIFIC_TYPE);
                conditionDTO.setConditionValue(ipCondition.getSpecificIP());

            } else if (PolicyConstants.HEADER_TYPE.equals(condition.getType())) {
                HeaderCondition headerCondition = (HeaderCondition) condition;
                conditionDTO.setConditionName(headerCondition.getHeaderName());
                conditionDTO.setConditionValue(headerCondition.getValue());

            } else if (PolicyConstants.JWT_CLAIMS_TYPE.equals(condition.getType())) {
                JWTClaimsCondition jwtClaimsCondition = (JWTClaimsCondition) condition;
                conditionDTO.setConditionName(jwtClaimsCondition.getClaimUrl());
                conditionDTO.setConditionValue(jwtClaimsCondition.getAttribute());

            } else if (PolicyConstants.QUERY_PARAMETER_TYPE.equals(condition.getType())) {
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

    public void updateAPI(API api) throws APIManagementException {

        updateAPI(api, null);
    }

    public void updateAPI(API api, String username) throws APIManagementException {

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
            prepStmt.setString(3, username);
            prepStmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            prepStmt.setString(5, api.getApiLevelPolicy());
            prepStmt.setString(6, api.getType());
            prepStmt.setString(7, APIUtil.replaceEmailDomainBack(api.getId().getProviderName()));
            prepStmt.setString(8, api.getId().getApiName());
            prepStmt.setString(9, api.getId().getVersion());
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
        } catch (SQLException e) {
            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException ex) {
                // Rollback failed. Exception will be thrown later for upper exception
                log.error("Failed to rollback the update API: " + api.getId(), ex);
            }
            handleException("Error while updating the API: " + api.getId() + " in the database", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, null);
        }
    }

    public int getAPIID(Identifier apiId, Connection connection) throws APIManagementException {
        boolean created = false;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;

        int id = -1;
        String getAPIQuery = SQLConstants.GET_API_ID_SQL;

        if (apiId instanceof APIProductIdentifier) {
            getAPIQuery = SQLConstants.GET_API_PRODUCT_ID_SQL;
        }

        try {
            if (connection == null) {

                // If connection is not provided a new one will be created.
                connection = APIMgtDBUtil.getConnection();
                created = true;
            }

            prepStmt = connection.prepareStatement(getAPIQuery);
            prepStmt.setString(1, APIUtil.replaceEmailDomainBack(apiId.getProviderName()));
            prepStmt.setString(2, apiId.getName());
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
     * Get product Id from the product name and the provider.
     * @param product product identifier
     * @throws APIManagementException exception
     */
    public void setAPIProductFromDB(APIProduct product)
            throws APIManagementException {
        APIProductIdentifier apiProductIdentifier = product.getId();

        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(SQLConstants.GET_API_PRODUCT_SQL)) {
            prepStmt.setString(1, APIUtil.replaceEmailDomainBack(apiProductIdentifier.getProviderName()));
            prepStmt.setString(2, apiProductIdentifier.getName());
            prepStmt.setString(3, apiProductIdentifier.getVersion());
            try (ResultSet rs = prepStmt.executeQuery()) {
                if (rs.next()) {
                    product.setProductId(rs.getInt("API_ID"));
                    product.setProductLevelPolicy(rs.getString("API_TIER"));
                } else {
                    String msg = "Unable to find the API Product : " + apiProductIdentifier.getName() + "-" +
                            APIUtil.replaceEmailDomainBack(apiProductIdentifier.getProviderName()) + "-" +
                            apiProductIdentifier.getVersion() + " in the database";
                    throw new APIManagementException(msg);
                }
            }
        } catch (SQLException e) {
            handleException("Error while locating API Product: " + apiProductIdentifier.getName() + "-" +
                    APIUtil.replaceEmailDomainBack(apiProductIdentifier.getProviderName())
                    + "-" + apiProductIdentifier.getVersion() + " from the database", e);
        }
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
        String deleteAuditAPIMapping = SQLConstants.REMOVE_SECURITY_AUDIT_MAP_SQL;
        String deleteCommentQuery = SQLConstants.REMOVE_FROM_API_COMMENT_SQL;
        String deleteRatingsQuery = SQLConstants.REMOVE_FROM_API_RATING_SQL;
        String deleteSubscriptionQuery = SQLConstants.REMOVE_FROM_API_SUBSCRIPTION_SQL;
        String deleteExternalAPIStoresQuery = SQLConstants.REMOVE_FROM_EXTERNAL_STORES_SQL;
        String deleteAPIQuery = SQLConstants.REMOVE_FROM_API_SQL;
        String deleteResourceScopeMappingsQuery = SQLConstants.REMOVE_RESOURCE_SCOPE_URL_MAPPING_SQL;
        String deleteURLTemplateQuery = SQLConstants.REMOVE_FROM_API_URL_MAPPINGS_SQL;
        String deleteGraphqlComplexityQuery = SQLConstants.REMOVE_FROM_GRAPHQL_COMPLEXITY_SQL;


        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);

            id = getAPIID(apiId, connection);

            prepStmt = connection.prepareStatement(deleteAuditAPIMapping);
            prepStmt.setInt(1, id);
            prepStmt.execute();
            prepStmt.close();//If exception occurs at execute, this statement will close in finally else here

            prepStmt = connection.prepareStatement(deleteGraphqlComplexityQuery);
            prepStmt.setInt(1, id);
            prepStmt.execute();
            prepStmt.close();//If exception occurs at execute, this statement will close in finally else here

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

            //Delete resource scope mappings of the API
            prepStmt = connection.prepareStatement(deleteResourceScopeMappingsQuery);
            prepStmt.setInt(1, id);
            prepStmt.execute();
            prepStmt.close();//If exception occurs at execute, this statement will close in finally else here

            // Delete URL Templates (delete the resource scope mappings on delete cascade)
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

    public Set<URITemplate> getURITemplatesOfAPI(APIIdentifier identifier)
            throws APIManagementException {
        Map<Integer, URITemplate> uriTemplates = new LinkedHashMap<>();
        Map<Integer, Set<String>> scopeToURITemplateId = new HashMap<>();

        try (Connection conn = APIMgtDBUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(SQLConstants.GET_URL_TEMPLATES_OF_API_SQL)) {
            ps.setString(1, APIUtil.replaceEmailDomainBack(identifier.getProviderName()));
            ps.setString(2, identifier.getName());
            ps.setString(3, identifier.getVersion());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Integer uriTemplateId = rs.getInt("URL_MAPPING_ID");
                    String scopeName = rs.getString("SCOPE_NAME");

                    if (scopeToURITemplateId.containsKey(uriTemplateId) && !StringUtils.isEmpty(scopeName)
                            && !scopeToURITemplateId.get(uriTemplateId).contains(scopeName)
                            && uriTemplates.containsKey(uriTemplateId)) {
                        Scope scope = new Scope();
                        scope.setKey(scopeName);
                        scopeToURITemplateId.get(uriTemplateId).add(scopeName);
                        uriTemplates.get(uriTemplateId).setScopes(scope);
                        continue;
                    }
                    String urlPattern = rs.getString("URL_PATTERN");
                    String verb = rs.getString("HTTP_METHOD");

                    URITemplate uriTemplate = new URITemplate();
                    uriTemplate.setUriTemplate(urlPattern);
                    uriTemplate.setHTTPVerb(verb);
                    uriTemplate.setHttpVerbs(verb);
                    String authType = rs.getString("AUTH_SCHEME");
                    String throttlingTier = rs.getString("THROTTLING_TIER");
                    if (StringUtils.isNotEmpty(scopeName)) {
                        Scope scope = new Scope();
                        scope.setKey(scopeName);
                        uriTemplate.setScope(scope);
                        uriTemplate.setScopes(scope);
                        Set<String> templateScopes = new HashSet<>();
                        templateScopes.add(scopeName);
                        scopeToURITemplateId.put(uriTemplateId, templateScopes);
                    }
                    uriTemplate.setAuthType(authType);
                    uriTemplate.setAuthTypes(authType);
                    uriTemplate.setThrottlingTier(throttlingTier);
                    uriTemplate.setThrottlingTiers(throttlingTier);

                    InputStream mediationScriptBlob = rs.getBinaryStream("MEDIATION_SCRIPT");
                    if (mediationScriptBlob != null) {
                        String script = APIMgtDBUtil.getStringFromInputStream(mediationScriptBlob);
                        uriTemplate.setMediationScript(script);
                        uriTemplate.setMediationScripts(verb, script);
                    }

                    uriTemplates.put(uriTemplateId, uriTemplate);
                }
            }

            setAssociatedAPIProducts(identifier, uriTemplates);
        } catch (SQLException e) {
            handleException("Failed to get URI Templates of API" + identifier, e);
        }

        return new LinkedHashSet<>(uriTemplates.values());
    }

    private void setAssociatedAPIProducts(APIIdentifier identifier, Map<Integer, URITemplate> uriTemplates)
            throws SQLException {
        try (Connection conn = APIMgtDBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQLConstants.GET_API_PRODUCT_URI_TEMPLATE_ASSOCIATION_SQL)) {
            ps.setString(1, APIUtil.replaceEmailDomainBack(identifier.getProviderName()));
            ps.setString(2, identifier.getName());
            ps.setString(3, identifier.getVersion());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String productName = rs.getString("API_NAME");
                    String productVersion = rs.getString("API_VERSION");
                    String productProvider = rs.getString("API_PROVIDER");
                    int uriTemplateId  = rs.getInt("URL_MAPPING_ID");

                    URITemplate uriTemplate = uriTemplates.get(uriTemplateId);
                    if (uriTemplate != null) {
                        APIProductIdentifier productIdentifier = new APIProductIdentifier
                                (productProvider, productName, productVersion);
                        uriTemplate.addUsedByProduct(productIdentifier);
                    }
                }
            }
        }
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
     *
     * @deprecated
     * This method needs to be removed once the Jaggery web apps are removed.
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
     * Adds a comment for an API
     *
     * @param identifier API identifier
     * @param comment    Commented Text
     * @param user       User who did the comment
     * @return Comment ID
     */
    public String addComment(Identifier identifier, Comment comment, String user) throws APIManagementException {
        Connection connection = null;
        ResultSet insertSet = null;
        PreparedStatement insertPrepStmt = null;
        String commentId = null;
        int id = -1;

        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            //Get API Id
            id = getAPIID(identifier, connection);
            if (id == -1) {
                String msg = "Could not load API record for: " + identifier.getName();
                log.error(msg);
                throw new APIManagementException(msg);
            }

            /*This query is to update the AM_API_COMMENTS table */
            String addCommentQuery = SQLConstants.ADD_COMMENT_SQL;
            commentId = UUID.randomUUID().toString();

            /*Adding data to the AM_API_COMMENTS table*/
            String dbProductName = connection.getMetaData().getDatabaseProductName();
            insertPrepStmt = connection.prepareStatement(addCommentQuery);

            insertPrepStmt.setString(1, commentId);
            insertPrepStmt.setString(2, comment.getText());
            insertPrepStmt.setString(3, user);
            insertPrepStmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()), Calendar.getInstance());
            insertPrepStmt.setInt(5, id);

            insertPrepStmt.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException e1) {
                    log.error("Failed to rollback the add comment ", e1);
                }
            }
            handleException("Failed to add comment data, for  " + identifier.getName() + "-" + identifier.getVersion(),
                    e);
        } finally {
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
                comment.setId(resultSet.getString("COMMENT_ID"));
                comment.setText(resultSet.getString("COMMENT_TEXT"));
                comment.setUser(resultSet.getString("COMMENTED_USER"));
                comment.setCreatedTime(resultSet.getTimestamp("DATE_COMMENTED"));
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

    /**
     * Returns a specific comment of an API
     *
     * @param commentId  Comment ID
     * @param identifier API identifier
     * @return Comment Array
     * @throws APIManagementException
     */
    public Comment getComment(Identifier identifier, String commentId) throws APIManagementException {

        Comment comment = new Comment();
        Connection connection = null;
        ResultSet resultSet = null;
        PreparedStatement prepStmt = null;
        int id = -1;

        String getCommentQuery = SQLConstants.GET_COMMENT_SQL;
        try {
            connection = APIMgtDBUtil.getConnection();
            id = getAPIID(identifier, connection);
            if (id == -1) {
                String msg = "Could not load API record for: " + identifier.getName();
                log.error(msg);
                throw new APIManagementException(msg);
            }
            prepStmt = connection.prepareStatement(getCommentQuery);
            prepStmt.setString(1, APIUtil.replaceEmailDomainBack(identifier.getProviderName()));
            prepStmt.setString(2, identifier.getName());
            prepStmt.setString(3, identifier.getVersion());
            prepStmt.setString(4, commentId);
            resultSet = prepStmt.executeQuery();

            if (resultSet.next()) {
                comment.setId(resultSet.getString("COMMENT_ID"));
                comment.setText(resultSet.getString("COMMENT_TEXT"));
                comment.setUser(resultSet.getString("COMMENTED_USER"));
                comment.setCreatedTime(resultSet.getTimestamp("DATE_COMMENTED"));
                return comment;
            }
        } catch (SQLException e) {
            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException e1) {
                log.error("Failed to retrieve comment ", e1);
            }
            handleException("Failed to retrieve comment for API " + identifier.getName() + "with comment ID " +
                    commentId, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, resultSet);
        }
        return null;
    }

    /**
     * Returns all the Comments on an API
     *
     * @param apiTypeWrapper API type wrapper
     * @return Comment Array
     * @throws APIManagementException
     */
    public Comment[] getComments(ApiTypeWrapper apiTypeWrapper) throws APIManagementException {
        List<Comment> commentList = new ArrayList<Comment>();
        Connection connection = null;
        ResultSet resultSet = null;
        PreparedStatement prepStmt = null;
        boolean isProduct = apiTypeWrapper.isAPIProduct();
        int id = -1;
        String sqlQuery;
        sqlQuery  = SQLConstants.GET_COMMENTS_SQL;
        Identifier identifier;

        try {
            connection = APIMgtDBUtil.getConnection();
            if (!isProduct) {
                identifier = apiTypeWrapper.getApi().getId();
            } else  {
                identifier = apiTypeWrapper.getApiProduct().getId();
            }
            id = getAPIID(identifier, connection);
            if (id == -1) {
                String msg = "Could not load API record for: " + identifier.getName();
                log.error(msg);
                throw new APIManagementException(msg);
            }
            prepStmt = connection.prepareStatement(sqlQuery);
            prepStmt.setString(1, APIUtil.replaceEmailDomainBack(identifier.getProviderName()));
            prepStmt.setString(2, identifier.getName());
            prepStmt.setString(3, identifier.getVersion());
            resultSet = prepStmt.executeQuery();
            while (resultSet.next()) {
                Comment comment = new Comment();
                comment.setId(resultSet.getString("COMMENT_ID"));
                comment.setText(resultSet.getString("COMMENT_TEXT"));
                comment.setUser(resultSet.getString("COMMENTED_USER"));
                comment.setCreatedTime(resultSet.getTimestamp("DATE_COMMENTED"));
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
            handleException("Failed to retrieve comments for  " + apiTypeWrapper.getName(), e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, resultSet);
        }
        return commentList.toArray(new Comment[commentList.size()]);
    }

    /**
     * Delete a comment
     *
     * @param identifier API Identifier
     * @param commentId Comment ID
     * @throws APIManagementException
     */
    public void deleteComment(APIIdentifier identifier, String commentId) throws APIManagementException {

        Connection connection = null;
        PreparedStatement prepStmt = null;

        String deleteCommentQuery = SQLConstants.DELETE_COMMENT_SQL;
        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            prepStmt = connection.prepareStatement(deleteCommentQuery);
            prepStmt.setString(1, commentId);
            prepStmt.execute();
            connection.commit();
        } catch (SQLException e) {
            handleException("Error while deleting comment " + commentId + " from the database", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, null);
        }
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

    /**
     * Get API Context using a new DB connection.
     *
     * @param identifier API Identifier
     * @return API Context
     * @throws APIManagementException if an error occurs
     */
    public String getAPIContext(APIIdentifier identifier) throws APIManagementException {

        String context = null;
        try (Connection connection = APIMgtDBUtil.getConnection()) {
            context = getAPIContext(identifier, connection);
        } catch (SQLException e) {
            log.error("Failed to retrieve the API Context", e);
            handleException("Failed to retrieve connection while getting the API Context for "
                    + identifier.getProviderName() + '-' + identifier.getApiName() + '-' + identifier.getVersion(), e);
        }
        return context;
    }

    /**
     * Get API Context by passing an existing DB connection.
     *
     * @param identifier API Identifier
     * @param connection DB Connection
     * @return API Context
     * @throws APIManagementException if an error occurs
     */
    public String getAPIContext(APIIdentifier identifier, Connection connection) throws APIManagementException {

        String context = null;
        String sql = SQLConstants.GET_API_CONTEXT_BY_API_NAME_SQL;
        try (PreparedStatement prepStmt = connection.prepareStatement(sql)) {
            prepStmt.setString(1, APIUtil.replaceEmailDomainBack(identifier.getProviderName()));
            prepStmt.setString(2, identifier.getApiName());
            prepStmt.setString(3, identifier.getVersion());
            try (ResultSet resultSet = prepStmt.executeQuery()) {
                while (resultSet.next()) {
                    context = resultSet.getString(1);
                }
            }
        } catch (SQLException e) {
            log.error("Failed to retrieve the API Context", e);
            handleException("Failed to retrieve the API Context for " + identifier.getProviderName() + '-'
                    + identifier.getApiName() + '-' + identifier.getVersion(), e);
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
                String tenantDomain = MultitenantUtils.getTenantDomain(subscriber.getName());
                String keyManagerName = rs.getString("KEY_MANAGER");
                workflowDTO.setKeyManager(keyManagerName);
                OAuthAppRequest request = ApplicationUtils.createOauthAppRequest(application.getName(), null,
                        application.getCallbackUrl(), rs
                                .getString("TOKEN_SCOPE"),
                        rs.getString("INPUTS"), application.getTokenType(),tenantDomain, keyManagerName);
                if (request.getOAuthApplicationInfo().getParameter("username") == null) {
                    KeyManagerConfigurationDTO config = getKeyManagerConfigurationByUUID(keyManagerName);
                    if (config != null) {
                        request = ApplicationUtils.createOauthAppRequest(application.getName(), null,
                                application.getCallbackUrl(), rs
                                        .getString("TOKEN_SCOPE"),
                                rs.getString("INPUTS"), application.getTokenType(),tenantDomain,
                                config.getName());
                    }
                }
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
     * Fetches WorkflowReference when given Application Name and UserId.
     *
     * @param applicationId
     * @param userId
     * @return WorkflowReference
     * @throws APIManagementException
     */
    public String getWorkflowReferenceByApplicationId(int applicationId, String userId) throws APIManagementException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String workflowReference = null;
        String sqlQuery = SQLConstants.GET_WORKFLOW_ENTRY_BY_APP_ID_SQL;
        try {
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            ps.setInt(1, applicationId);
            ps.setString(2, userId);
            rs = ps.executeQuery();
            while (rs.next()) {
                workflowReference = rs.getString("WF_REF");
            }
        } catch (SQLException e) {
            handleException("Error occurred while getting workflow entry for " +
                    "Application : " + applicationId + " created by " + userId, e);
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
     * Remove workflow entry
     *
     * @param workflowReference
     * @param workflowType
     * @throws APIManagementException
     */
    public void removeWorkflowEntry(String workflowReference, String workflowType) throws APIManagementException {

        Connection connection = null;
        PreparedStatement prepStmt = null;

        String queryWorkflowDelete = SQLConstants.REMOVE_WORKFLOW_ENTRY_SQL;
        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            prepStmt = connection.prepareStatement(queryWorkflowDelete);
            prepStmt.setString(1, workflowType);
            prepStmt.setString(2, workflowReference);
            prepStmt.execute();
            connection.commit();
        } catch (SQLException e) {
            handleException("Error while deleting workflow entry " + workflowReference + " from the database", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, null);
        }
    }

    /**
     * Retries the WorkflowExternalReference for a subscription.
     *
     * @param identifier Identifier to find the subscribed api
     * @param appID      ID of the application which has the subscription
     * @return External workflow reference for the subscription identified
     * @throws APIManagementException
     */
    public String getExternalWorkflowReferenceForSubscription(Identifier identifier, int appID)
            throws APIManagementException {
        String workflowExtRef = null;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int id = -1;
        int subscriptionID = -1;

        String sqlQuery = SQLConstants.GET_EXTERNAL_WORKFLOW_REFERENCE_FOR_SUBSCRIPTION_SQL;
        String postgreSQL = SQLConstants.GET_EXTERNAL_WORKFLOW_REFERENCE_FOR_SUBSCRIPTION_POSTGRE_SQL;
        try {
            if (identifier instanceof APIIdentifier) {
                id = getAPIID((APIIdentifier) identifier, conn);

            } else if (identifier instanceof APIProductIdentifier) {
                id = ((APIProductIdentifier) identifier).getProductId();
            }

            conn = APIMgtDBUtil.getConnection();
            if (conn.getMetaData().getDriverName().contains("PostgreSQL")) {
                sqlQuery = postgreSQL;
            }
            ps = conn.prepareStatement(sqlQuery);
            ps.setInt(1, id);
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
     * Retrieves the IDs of pending subscriptions of a given API
     * @param apiId API Identifier
     * @return set of subscriptions ids
     * @throws APIManagementException
     */
    public Set<Integer> getPendingSubscriptionsByAPIId(APIIdentifier apiId) throws APIManagementException {
        Set<Integer> pendingSubscriptions = new HashSet<Integer>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sqlQuery = SQLConstants.GET_SUBSCRIPTIONS_BY_API_SQL;
        try {
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            ps.setString(1, apiId.getApiName());
            ps.setString(2, apiId.getVersion());
            ps.setString(3, apiId.getProviderName());
            ps.setString(4, APIConstants.SubscriptionStatus.ON_HOLD);
            rs = ps.executeQuery();

            while (rs.next()) {
                pendingSubscriptions.add(rs.getInt("SUBSCRIPTION_ID"));
            }
        } catch (SQLException e) {
            handleException("Error occurred while retrieving subscription entries for API : " + apiId, e);
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
     * @param keyManagerName
     * @return workflow reference of the registration
     * @throws APIManagementException
     */
    public String getRegistrationWFReference(int applicationId, String keyType, String keyManagerName) throws APIManagementException {
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
            ps.setString(3,keyManagerName);
            rs = ps.executeQuery();

            // returns only one row
            if (rs.next()) {
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
     * @param identifier    identifier subscribed
     * @param applicationId application with subscription
     * @return subscription status
     * @throws APIManagementException
     */
    public String getSubscriptionStatus(Identifier identifier, int applicationId) throws APIManagementException {
        String status = null;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int id = -1;

        String sqlQuery = SQLConstants.GET_SUBSCRIPTION_STATUS_SQL;
        try {
            conn = APIMgtDBUtil.getConnection();
            if (identifier instanceof APIIdentifier) {
                id = getAPIID((APIIdentifier) identifier, conn);
            } else if (identifier instanceof APIProductIdentifier) {
                id = ((APIProductIdentifier) identifier).getProductId();
            }
            ps = conn.prepareStatement(sqlQuery);
            ps.setInt(1, id);
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

    public List<KeyManagerConfigurationDTO> getKeyManagerConfigurationsByTenant(String tenantDomain)
            throws APIManagementException {
        List<KeyManagerConfigurationDTO> keyManagerConfigurationDTOS = new ArrayList<>();
        final String query = "SELECT * FROM AM_KEY_MANAGER WHERE TENANT_DOMAIN = ? ";
        try (Connection conn = APIMgtDBUtil.getConnection();
        PreparedStatement preparedStatement = conn.prepareStatement(query)) {
            preparedStatement.setString(1, tenantDomain);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()){
                    KeyManagerConfigurationDTO keyManagerConfigurationDTO = new KeyManagerConfigurationDTO();
                    String uuid = resultSet.getString("UUID");
                    keyManagerConfigurationDTO.setUuid(uuid);
                    keyManagerConfigurationDTO.setName(resultSet.getString("NAME"));
                    keyManagerConfigurationDTO.setDisplayName(resultSet.getString("DISPLAY_NAME"));
                    keyManagerConfigurationDTO.setDescription(resultSet.getString("DESCRIPTION"));
                    keyManagerConfigurationDTO.setType(resultSet.getString("TYPE"));
                    keyManagerConfigurationDTO.setEnabled(resultSet.getBoolean("ENABLED"));
                    keyManagerConfigurationDTO.setTenantDomain(tenantDomain);
                    try (InputStream configuration = resultSet.getBinaryStream("CONFIGURATION")) {
                        String configurationContent = IOUtils.toString(configuration);
                        Map map = new Gson().fromJson(configurationContent, Map.class);
                        keyManagerConfigurationDTO.setAdditionalProperties(map);
                    } catch (IOException e) {
                        log.error("Error while converting configurations in " + uuid, e);
                    }
                    keyManagerConfigurationDTOS.add(keyManagerConfigurationDTO);
                }
            }
        } catch (SQLException e) {
            throw new APIManagementException(
                    "Error while retrieving key manager configurations for tenant " + tenantDomain, e);
        }

        return keyManagerConfigurationDTOS;
    }

    public KeyManagerConfigurationDTO getKeyManagerConfigurationByID(String tenantDomain, String id)
            throws APIManagementException {

        final String query = "SELECT * FROM AM_KEY_MANAGER WHERE UUID = ? AND TENANT_DOMAIN = ?";
        try (Connection conn = APIMgtDBUtil.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(query)) {
            preparedStatement.setString(1, id);
            preparedStatement.setString(2,tenantDomain);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()){
                    KeyManagerConfigurationDTO keyManagerConfigurationDTO = new KeyManagerConfigurationDTO();
                    String uuid = resultSet.getString("UUID");
                    keyManagerConfigurationDTO.setUuid(uuid);
                    keyManagerConfigurationDTO.setName(resultSet.getString("NAME"));
                    keyManagerConfigurationDTO.setDescription(resultSet.getString("DESCRIPTION"));
                    keyManagerConfigurationDTO.setType(resultSet.getString("TYPE"));
                    keyManagerConfigurationDTO.setEnabled(resultSet.getBoolean("ENABLED"));
                    keyManagerConfigurationDTO.setTenantDomain(tenantDomain);
                    keyManagerConfigurationDTO.setDisplayName(resultSet.getString("DISPLAY_NAME"));
                    try (InputStream configuration = resultSet.getBinaryStream("CONFIGURATION")) {
                        String configurationContent = IOUtils.toString(configuration);
                        Map map = new Gson().fromJson(configurationContent, Map.class);
                        keyManagerConfigurationDTO.setAdditionalProperties(map);
                    }
                    return keyManagerConfigurationDTO;
                }
            }
        } catch (SQLException | IOException e) {
            throw new APIManagementException(
                    "Error while retrieving key manager configuration for " + id + " in tenant " + tenantDomain, e);
        }
        return null;

    }

    public KeyManagerConfigurationDTO getKeyManagerConfigurationByName(String tenantDomain, String name)
            throws APIManagementException {
        final String query = "SELECT * FROM AM_KEY_MANAGER WHERE NAME = ? AND TENANT_DOMAIN = ?";
        try (Connection conn = APIMgtDBUtil.getConnection()) {
            return  getKeyManagerConfigurationByName(conn,tenantDomain,name);
        } catch (SQLException | IOException e) {
            throw new APIManagementException(
                    "Error while retrieving key manager configuration for " + name + " in tenant " + tenantDomain, e);
        }
    }

    private KeyManagerConfigurationDTO getKeyManagerConfigurationByName(Connection connection ,String tenantDomain,
                                                                        String name)
            throws SQLException, IOException {
        final String query = "SELECT * FROM AM_KEY_MANAGER WHERE NAME = ? AND TENANT_DOMAIN = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, name);
            preparedStatement.setString(2,tenantDomain);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()){
                    KeyManagerConfigurationDTO keyManagerConfigurationDTO = new KeyManagerConfigurationDTO();
                    String uuid = resultSet.getString("UUID");
                    keyManagerConfigurationDTO.setUuid(uuid);
                    keyManagerConfigurationDTO.setName(resultSet.getString("NAME"));
                    keyManagerConfigurationDTO.setDisplayName(resultSet.getString("DISPLAY_NAME"));
                    keyManagerConfigurationDTO.setDescription(resultSet.getString("DESCRIPTION"));
                    keyManagerConfigurationDTO.setType(resultSet.getString("TYPE"));
                    keyManagerConfigurationDTO.setEnabled(resultSet.getBoolean("ENABLED"));
                    keyManagerConfigurationDTO.setTenantDomain(tenantDomain);
                    try (InputStream configuration = resultSet.getBinaryStream("CONFIGURATION")) {
                        String configurationContent = IOUtils.toString(configuration);
                        Map map = new Gson().fromJson(configurationContent, Map.class);
                        keyManagerConfigurationDTO.setAdditionalProperties(map);
                    }
                    return keyManagerConfigurationDTO;
                }
            }
        }
        return null;
    }

    public KeyManagerConfigurationDTO getKeyManagerConfigurationByUUID(String uuid)
            throws APIManagementException {
        try (Connection conn = APIMgtDBUtil.getConnection()) {
            return  getKeyManagerConfigurationByUUID(conn, uuid);
        } catch (SQLException | IOException e) {
            throw new APIManagementException(
                    "Error while retrieving key manager configuration for key manager uuid: " + uuid, e);
        }
    }

    private KeyManagerConfigurationDTO getKeyManagerConfigurationByUUID(Connection connection ,String uuid)
            throws SQLException, IOException {
        final String query = "SELECT * FROM AM_KEY_MANAGER WHERE UUID = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, uuid);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()){
                    KeyManagerConfigurationDTO keyManagerConfigurationDTO = new KeyManagerConfigurationDTO();
                    keyManagerConfigurationDTO.setUuid(resultSet.getString("UUID"));
                    keyManagerConfigurationDTO.setName(resultSet.getString("NAME"));
                    keyManagerConfigurationDTO.setDisplayName(resultSet.getString("DISPLAY_NAME"));
                    keyManagerConfigurationDTO.setDescription(resultSet.getString("DESCRIPTION"));
                    keyManagerConfigurationDTO.setType(resultSet.getString("TYPE"));
                    keyManagerConfigurationDTO.setEnabled(resultSet.getBoolean("ENABLED"));
                    keyManagerConfigurationDTO.setTenantDomain(resultSet.getString("TENANT_DOMAIN"));
                    try (InputStream configuration = resultSet.getBinaryStream("CONFIGURATION")) {
                        String configurationContent = IOUtils.toString(configuration);
                        Map map = new Gson().fromJson(configurationContent, Map.class);
                        keyManagerConfigurationDTO.setAdditionalProperties(map);
                    }
                    return keyManagerConfigurationDTO;
                }
            }
        }
        return null;
    }

    public void addKeyManagerConfiguration(KeyManagerConfigurationDTO keyManagerConfigurationDTO)
            throws APIManagementException {

        try (Connection conn = APIMgtDBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement preparedStatement = conn
                    .prepareStatement(SQLConstants.KeyManagerSqlConstants.ADD_KEY_MANAGER)) {
                preparedStatement.setString(1, keyManagerConfigurationDTO.getUuid());
                preparedStatement.setString(2, keyManagerConfigurationDTO.getName());
                preparedStatement.setString(3, keyManagerConfigurationDTO.getDescription());
                preparedStatement.setString(4, keyManagerConfigurationDTO.getType());
                String configurationJson = new Gson().toJson(keyManagerConfigurationDTO.getAdditionalProperties());
                preparedStatement.setBinaryStream(5, new ByteArrayInputStream(configurationJson.getBytes()));
                preparedStatement.setString(6, keyManagerConfigurationDTO.getTenantDomain());
                preparedStatement.setBoolean(7, keyManagerConfigurationDTO.isEnabled());
                preparedStatement.setString(8, keyManagerConfigurationDTO.getDisplayName());
                preparedStatement.executeUpdate();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                if (e instanceof SQLIntegrityConstraintViolationException) {
                    if (getKeyManagerConfigurationByName(conn, keyManagerConfigurationDTO.getTenantDomain(),
                            keyManagerConfigurationDTO.getName()) != null) {
                        log.warn(keyManagerConfigurationDTO.getName() + " Key Manager Already Registered in tenant" +
                                keyManagerConfigurationDTO.getTenantDomain());
                    } else {
                        throw new APIManagementException("Error while Storing key manager configuration with name " +
                                keyManagerConfigurationDTO.getName() + " in tenant " +
                                keyManagerConfigurationDTO.getTenantDomain(), e);
                    }
                }
            }
        } catch (SQLException | IOException e) {
            throw new APIManagementException(
                    "Error while Storing key manager configuration with name " + keyManagerConfigurationDTO.getName() +
                            " in tenant " + keyManagerConfigurationDTO.getTenantDomain(), e);
        }
    }

    public boolean isKeyManagerConfigurationExistById(String tenantDomain, String id) throws APIManagementException {
        final String query = "SELECT 1 FROM AM_KEY_MANAGER WHERE UUID = ? AND TENANT_DOMAIN = ?";
        try (Connection conn = APIMgtDBUtil.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(query)) {
            preparedStatement.setString(1, id);
            preparedStatement.setString(2, tenantDomain);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return true;
                }
            }
        } catch (SQLException e) {
            throw new APIManagementException(
                    "Error while retrieving key manager configuration for " + id + " in tenant " + tenantDomain, e);
        }
        return false;

    }

    public void updateKeyManagerConfiguration(KeyManagerConfigurationDTO keyManagerConfigurationDTO)
            throws APIManagementException {
        try (Connection conn = APIMgtDBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement preparedStatement = conn
                    .prepareStatement(SQLConstants.KeyManagerSqlConstants.UPDATE_KEY_MANAGER)) {
                preparedStatement.setString(1, keyManagerConfigurationDTO.getName());
                preparedStatement.setString(2, keyManagerConfigurationDTO.getDescription());
                preparedStatement.setString(3, keyManagerConfigurationDTO.getType());
                String configurationJson = new Gson().toJson(keyManagerConfigurationDTO.getAdditionalProperties());
                preparedStatement.setBinaryStream(4, new ByteArrayInputStream(configurationJson.getBytes()));
                preparedStatement.setString(5, keyManagerConfigurationDTO.getTenantDomain());
                preparedStatement.setBoolean(6,keyManagerConfigurationDTO.isEnabled());
                preparedStatement.setString(7, keyManagerConfigurationDTO.getDisplayName());
                preparedStatement.setString(8, keyManagerConfigurationDTO.getUuid());
                preparedStatement.executeUpdate();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new APIManagementException(
                    "Error while Updating key manager configuration with name " + keyManagerConfigurationDTO.getName() +
                            " in tenant " + keyManagerConfigurationDTO.getTenantDomain(), e);
        }
    }

    public void deleteKeyManagerConfigurationById(String id, String tenantDomain) throws APIManagementException {
        try (Connection conn = APIMgtDBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement preparedStatement = conn
                    .prepareStatement(SQLConstants.KeyManagerSqlConstants.DELETE_KEY_MANAGER)) {
                preparedStatement.setString(1, id);
                preparedStatement.setString(2, tenantDomain);
                preparedStatement.execute();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new APIManagementException(
                    "Error while deleting key manager configuration with id " + id + " in tenant " + tenantDomain, e);
        }

    }


    public List<KeyManagerConfigurationDTO> getKeyManagerConfigurations() throws APIManagementException {
        List<KeyManagerConfigurationDTO> keyManagerConfigurationDTOS = new ArrayList<>();
        final String query = "SELECT * FROM AM_KEY_MANAGER";
        try (Connection conn = APIMgtDBUtil.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(query)) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()){
                    KeyManagerConfigurationDTO keyManagerConfigurationDTO = new KeyManagerConfigurationDTO();
                    String uuid = resultSet.getString("UUID");
                    keyManagerConfigurationDTO.setUuid(uuid);
                    keyManagerConfigurationDTO.setName(resultSet.getString("NAME"));
                    keyManagerConfigurationDTO.setDisplayName(resultSet.getString("DISPLAY_NAME"));
                    keyManagerConfigurationDTO.setDescription(resultSet.getString("DESCRIPTION"));
                    keyManagerConfigurationDTO.setType(resultSet.getString("TYPE"));
                    keyManagerConfigurationDTO.setEnabled(resultSet.getBoolean("ENABLED"));
                    keyManagerConfigurationDTO.setTenantDomain(resultSet.getString("TENANT_DOMAIN"));
                    try (InputStream configuration = resultSet.getBinaryStream("CONFIGURATION")) {
                        String configurationContent = IOUtils.toString(configuration);
                        Map map = new Gson().fromJson(configurationContent, Map.class);
                        keyManagerConfigurationDTO.setAdditionalProperties(map);
                    } catch (IOException e) {
                        log.error("Error while converting configurations in " + uuid, e);
                    }
                    keyManagerConfigurationDTOS.add(keyManagerConfigurationDTO);
                }
            }
        } catch (SQLException e) {
            throw new APIManagementException("Error while retrieving all key manager configurations", e);
        }

        return keyManagerConfigurationDTOS;
    }

    public boolean isKeyManagerConfigurationExistByName(String name, String tenantDomain)
            throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            final String query = "SELECT 1 FROM AM_KEY_MANAGER WHERE NAME = ? AND TENANT_DOMAIN = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, name);
                preparedStatement.setString(2, tenantDomain);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()){
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            throw new APIManagementException("Error while retriving key manager existence",e);
        }
        return false;
    }

    public Set<APIKey> getKeyMappingsFromApplicationId(int applicationId) throws APIManagementException {

        Set<APIKey> apiKeyList = new HashSet<>();
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement preparedStatement = connection
                     .prepareStatement(SQLConstants.GET_KEY_MAPPING_INFO_FROM_APP_ID)) {
            preparedStatement.setInt(1, applicationId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    APIKey apiKey = new APIKey();
                    apiKey.setMappingId(resultSet.getString("UUID"));
                    apiKey.setConsumerKey(resultSet.getString("CONSUMER_KEY"));
                    apiKey.setKeyManager(resultSet.getString("KEY_MANAGER"));
                    apiKey.setType(resultSet.getString("KEY_TYPE"));
                    apiKey.setState(resultSet.getString("STATE"));
                    String createMode = resultSet.getString("CREATE_MODE");
                    if (StringUtils.isEmpty(createMode)) {
                        createMode = APIConstants.OAuthAppMode.CREATED.name();
                    }
                    apiKey.setCreateMode(createMode);
                    try (InputStream appInfo = resultSet.getBinaryStream("APP_INFO")) {
                        if (appInfo != null) {
                            apiKey.setAppMetaData(IOUtils.toString(appInfo));
                        }
                    } catch (IOException e) {
                        log.error("Error while retrieving metadata", e);
                    }
                    apiKeyList.add(apiKey);
                }
            }
        } catch (SQLException e) {
            throw new APIManagementException("Error while Retrieving Key Mappings ", e);
        }
        return apiKeyList;
    }

    public APIKey getKeyMappingsFromApplicationIdKeyManagerAndKeyType(int applicationId, String keyManagerName,
                                                                      String keyManagerId, String keyType)
            throws APIManagementException {
        try (Connection connection = APIMgtDBUtil.getConnection();
                PreparedStatement preparedStatement = connection
                        .prepareStatement(SQLConstants.GET_KEY_MAPPING_INFO_FROM_APP_ID_KEY_MANAGER_KEY_TYPE)) {
            preparedStatement.setInt(1, applicationId);
            preparedStatement.setString(2, keyType);
            preparedStatement.setString(3, keyManagerName);
            preparedStatement.setString(4, keyManagerId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    APIKey apiKey = new APIKey();
                    apiKey.setMappingId(resultSet.getString("UUID"));
                    apiKey.setConsumerKey(resultSet.getString("CONSUMER_KEY"));
                    apiKey.setKeyManager(resultSet.getString("KEY_MANAGER"));
                    apiKey.setType(resultSet.getString("KEY_TYPE"));
                    apiKey.setState(resultSet.getString("STATE"));
                    String createMode = resultSet.getString("CREATE_MODE");
                    if (StringUtils.isEmpty(createMode)) {
                        createMode = APIConstants.OAuthAppMode.CREATED.name();
                    }
                    apiKey.setCreateMode(createMode);
                    try (InputStream appInfo = resultSet.getBinaryStream("APP_INFO")) {
                        if (appInfo != null) {
                            apiKey.setAppMetaData(IOUtils.toString(appInfo));
                        }
                    } catch (IOException e) {
                        log.error("Error while retrieving metadata", e);
                    }
                    return apiKey;
                }
            }
        } catch (SQLException e) {
            throw new APIManagementException("Error while Retrieving Key Mappings ", e);
        }
        return null;
    }

    public APIKey getKeyMappingFromApplicationIdAndKeyMappingId(int applicationId, String keyMappingId)
            throws APIManagementException {

        final String query = "SELECT UUID,CONSUMER_KEY,KEY_MANAGER,KEY_TYPE,STATE FROM AM_APPLICATION_KEY_MAPPING " +
                "WHERE APPLICATION_ID=? AND UUID = ?";
        Set<APIKey> apiKeyList  = new HashSet<>();
        try(Connection connection = APIMgtDBUtil.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query)){
            preparedStatement.setInt(1,applicationId);
            preparedStatement.setString(2,keyMappingId);
            try(ResultSet resultSet = preparedStatement.executeQuery()){
                if (resultSet.next()){
                    APIKey apiKey = new APIKey() ;
                    apiKey.setMappingId(resultSet.getString("UUID"));
                    apiKey.setConsumerKey(resultSet.getString("CONSUMER_KEY"));
                    apiKey.setKeyManager(resultSet.getString("KEY_MANAGER"));
                    apiKey.setType(resultSet.getString("KEY_TYPE"));
                    apiKey.setState(resultSet.getString("STATE"));
                    return  apiKey;
                }
            }
        } catch (SQLException e) {
            throw new APIManagementException("Error while Retrieving Key Mapping ",e);
        }
        return null;
    }

    public void deleteApplicationKeyMappingByMappingId(String keyMappingId) throws APIManagementException {
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            String deleteKeyMappingQuery = SQLConstants.DELETE_APPLICATION_KEY_MAPPING_BY_UUID_SQL;
            if (log.isDebugEnabled()) {
                log.debug("trying to delete key mapping for UUID " + keyMappingId);
            }
            ps = connection.prepareStatement(deleteKeyMappingQuery);
            ps.setString(1, keyMappingId);
            ps.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            handleException("Error while removing application mapping table", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, connection, null);
        }
    }

    public String getKeyMappingIdFromApplicationIdKeyTypeAndKeyManager(int applicationId, String tokenType,
                                                                       String keyManagerName)
            throws APIManagementException {
        try(Connection connection = APIMgtDBUtil.getConnection();
        PreparedStatement preparedStatement =
                connection.prepareStatement(SQLConstants.GET_KEY_MAPPING_ID_FROM_APPLICATION)) {
            preparedStatement.setInt(1,applicationId);
            preparedStatement.setString(2,tokenType);
            preparedStatement.setString(3,keyManagerName);
            try(ResultSet resultSet = preparedStatement.executeQuery()){
                if (resultSet.next()){
                    return resultSet.getString("UUID");
                }
            }
        } catch (SQLException e) {
            throw new APIManagementException("Error while retrieving the Key Mapping id", e);
        }
        return null;
    }

    /**
     * This method used to update Application metadata according to oauth app info
     * @param applicationId
     * @param keyType
     * @param keyManagerName
     * @param keymanagerUUID
     * @param updatedAppInfo
     * @throws APIManagementException
     */
    public void updateApplicationKeyTypeMetaData(int applicationId, String keyType, String keyManagerName,
                                                 String keymanagerUUID, OAuthApplicationInfo updatedAppInfo)
            throws APIManagementException {

        if (applicationId > 0 && updatedAppInfo != null) {
            String addApplicationKeyMapping = SQLConstants.UPDATE_APPLICATION_KEY_TYPE_MAPPINGS_METADATA_SQL;
            try (Connection connection = APIMgtDBUtil.getConnection()) {
                connection.setAutoCommit(false);
                try {
                    try (PreparedStatement ps = connection.prepareStatement(addApplicationKeyMapping)) {
                        String content = new Gson().toJson(updatedAppInfo);
                        ps.setBinaryStream(1, new ByteArrayInputStream(content.getBytes()));
                        ps.setInt(2, applicationId);
                        ps.setString(3, keyType);
                        ps.setString(4, keyManagerName);
                        int res = ps.executeUpdate();
                        if (res == 0) {
                            ps.setBinaryStream(1, new ByteArrayInputStream(content.getBytes()));
                            ps.setInt(2, applicationId);
                            ps.setString(3, keyType);
                            ps.setString(4, keymanagerUUID);
                            ps.executeUpdate();
                        }
                    }
                    connection.commit();
                } catch (SQLException e) {
                    connection.rollback();
                }
            } catch (SQLException e) {
                handleException("Error updating the Application Metadata of the AM_APPLICATION_KEY_MAPPING table " +
                        "where " +
                        "APPLICATION_ID = " + applicationId + " and KEY_TYPE = " + keyType, e);
            }
        }
    }

    private class SubscriptionInfo {
        private int subscriptionId;
        private String tierId;
        private int applicationId;
        private String accessToken;
        private String tokenType;
        private String subscriptionStatus;
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
                ps.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
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
                ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
                ps.setInt(4, apiId);
                ps.setString(5, store.getName());
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
                store.setLastUpdated(rs.getTimestamp("LAST_UPDATED_TIME"));
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

    /**
     * Get Scope keys attached to the given API.
     *
     * @param identifier API Identifier
     * @return set of scope key attached to the API
     * @throws APIManagementException if fails get API scope keys
     */
    public Set<String> getAPIScopeKeys(APIIdentifier identifier) throws APIManagementException {

        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        Set<String> scopeKeySet = new LinkedHashSet<>();
        int apiId;
        try {
            conn = APIMgtDBUtil.getConnection();
            apiId = getAPIID(identifier, conn);

            String sqlQuery = SQLConstants.GET_API_SCOPES_SQL;

            ps = conn.prepareStatement(sqlQuery);
            ps.setInt(1, apiId);
            resultSet = ps.executeQuery();
            while (resultSet.next()) {
                scopeKeySet.add(resultSet.getString(1));
            }
        } catch (SQLException e) {
            handleException("Failed to retrieve api scope keys ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, resultSet);
        }
        return scopeKeySet;
    }

    /**
     * Generate Set<Scope> from HashMap
     *
     * @return Set of Scopes populated with roles.
     */
    private Set<Scope> populateScopeSet(HashMap<?, Scope> scopeHashMap) {
        Set<Scope> scopes = new LinkedHashSet<Scope>();
        for (Scope scope : scopeHashMap.values()) {
            scopes.add(scope);
        }
        return scopes;
    }

    /**
     * Returns all the scopes assigned for given apis
     *
     * @param apiIdsString list of api ids separated by commas
     * @return Map<String, Set<String>> set of scope keys for each apiId
     * @throws APIManagementException
     */
    public Map<String, Set<String>> getScopesForAPIS(String apiIdsString) throws APIManagementException {

        Map<String, Set<String>> apiScopeSet = new HashMap();

        try (Connection conn = APIMgtDBUtil.getConnection()) {

            String sqlQuery = SQLConstants.GET_SCOPES_FOR_API_LIST;

            if (conn.getMetaData().getDriverName().contains("Oracle")) {
                sqlQuery = SQLConstants.GET_SCOPES_FOR_API_LIST_ORACLE;
            }

            // apids are retrieved from the db so no need to protect for sql injection
            sqlQuery = sqlQuery.replace("$paramList", apiIdsString);

            try (PreparedStatement ps = conn.prepareStatement(sqlQuery);
                 ResultSet resultSet = ps.executeQuery()) {
                while (resultSet.next()) {
                    String scopeKey = resultSet.getString(1);
                    String apiId = resultSet.getString(2);
                    Set<String> scopeList = apiScopeSet.get(apiId);
                    if (scopeList == null) {
                        scopeList = new LinkedHashSet<>();
                        scopeList.add(scopeKey);
                        apiScopeSet.put(apiId, scopeList);
                    } else {
                        scopeList.add(scopeKey);
                        apiScopeSet.put(apiId, scopeList);
                    }
                }
            }
        } catch (SQLException e) {
            handleException("Failed to retrieve api scopes ", e);
        }
        return apiScopeSet;
    }

    public Set<String> getScopesBySubscribedAPIs(List<APIIdentifier> identifiers) throws APIManagementException {

        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        List<Integer> apiIds = new ArrayList<Integer>();
        Set<String> scopes = new HashSet<>();

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
                scopes.add(resultSet.getString(1));
            }
        } catch (SQLException e) {
            handleException("Failed to retrieve api scope keys ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, resultSet);
        }
        return scopes;
    }

    public Set<Scope> getAPIScopesByScopeKey(String scopeKey, int tenantId) throws APIManagementException {
        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        HashMap<String, Scope> scopeHashMap = new HashMap<>();
        try {
            String sqlQuery = SQLConstants.GET_SCOPES_BY_SCOPE_KEY_SQL;
            conn = APIMgtDBUtil.getConnection();

            ps = conn.prepareStatement(sqlQuery);
            ps.setString(1, scopeKey);
            ps.setInt(2, tenantId);
            resultSet = ps.executeQuery();
            while (resultSet.next()) {
                Scope scope;
                String scopeId = String.valueOf(resultSet.getInt(1));
                if (scopeHashMap.containsKey(scopeId)) {
                    // scope already exists append roles.
                    scope = scopeHashMap.get(scopeId);
                    String roles = resultSet.getString(5);
                    if (StringUtils.isNotEmpty(roles)) {
                        scope.setRoles(scope.getRoles().concat("," + roles.trim()));
                    }
                } else {
                    scope = new Scope();
                    scope.setId(scopeId);
                    scope.setKey(resultSet.getString(2));
                    scope.setName(resultSet.getString(3));
                    scope.setDescription(resultSet.getString(4));
                    String roles = resultSet.getString(5);
                    if (StringUtils.isNotEmpty(roles)) {
                        scope.setRoles(roles.trim());
                    }
                }
                scopeHashMap.put(scopeId, scope);
            }
        } catch (SQLException e) {
            handleException("Failed to retrieve api scopes ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, resultSet);
        }
        return populateScopeSet(scopeHashMap);
    }

    public Set<Scope> getScopesByScopeKeys(String scopeKeys, int tenantId) throws APIManagementException {
        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        List<String> inputScopeList = Arrays.asList(scopeKeys.split(" "));
        StringBuilder placeHolderBuilder = new StringBuilder();
        HashMap<String, Scope> scopeHashMap = new HashMap<>();
        for (int i = 0; i < inputScopeList.size(); i++) {
            placeHolderBuilder.append("?, ");
        }

        String placeHolderStr = placeHolderBuilder.deleteCharAt(placeHolderBuilder.length() - 2).toString();
        try {
            conn = APIMgtDBUtil.getConnection();
            String sqlQuery = SQLConstants.GET_SCOPES_BY_SCOPE_KEYS_PREFIX + placeHolderStr + SQLConstants
                    .GET_SCOPES_BY_SCOPE_KEYS_SUFFIX;
            if (conn.getMetaData().getDriverName().contains("Oracle")) {
                sqlQuery = SQLConstants.GET_SCOPES_BY_SCOPE_KEYS_PREFIX_ORACLE + placeHolderStr
                        + SQLConstants.GET_SCOPES_BY_SCOPE_KEYS_SUFFIX;
            }
            ps = conn.prepareStatement(sqlQuery);

            for (int i = 0; i < inputScopeList.size(); i++) {
                ps.setString(i + 1, inputScopeList.get(i));
            }

            ps.setInt(inputScopeList.size() + 1, tenantId);

            resultSet = ps.executeQuery();
            while (resultSet.next()) {
                Scope scope;
                String scopeId = String.valueOf(resultSet.getInt(1));
                if (scopeHashMap.containsKey(scopeId)) {
                    // scope already exists append roles.
                    scope = scopeHashMap.get(scopeId);
                    String roles = resultSet.getString(6);
                    if (StringUtils.isNotEmpty(roles)) {
                        scope.setRoles(scope.getRoles().concat("," + roles.trim()));
                    }
                } else {
                    scope = new Scope();
                    scope.setId(scopeId);
                    scope.setKey(resultSet.getString(2));
                    scope.setName(resultSet.getString(3));
                    scope.setDescription(resultSet.getString(4));
                    String roles = resultSet.getString(6);
                    if (StringUtils.isNotEmpty(roles)) {
                        scope.setRoles(roles.trim());
                    }
                }
                scopeHashMap.put(scopeId, scope);
            }
        } catch (SQLException e) {
            handleException("Failed to retrieve api scopes ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, resultSet);
        }
        return populateScopeSet(scopeHashMap);
    }

    /**
     * Get the unversioned local scope keys set of the API.
     *
     * @param apiIdentifier API Identifier
     * @param tenantId      Tenant Id
     * @return Local Scope keys set
     * @throws APIManagementException if fails to get local scope keys for API
     */
    public Set<String> getUnversionedLocalScopeKeysForAPI(APIIdentifier apiIdentifier, int tenantId)
            throws APIManagementException {

        int apiId;
        Set<String> localScopes = new HashSet<>();
        String getUnVersionedLocalScopes = SQLConstants.GET_UNVERSIONED_LOCAL_SCOPES_FOR_API_SQL;
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(getUnVersionedLocalScopes)) {
            apiId = getAPIID(apiIdentifier, connection);
            preparedStatement.setInt(1, apiId);
            preparedStatement.setInt(2, tenantId);
            preparedStatement.setInt(3, tenantId);
            preparedStatement.setInt(4, apiId);
            preparedStatement.setInt(5, tenantId);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    localScopes.add(rs.getString("SCOPE_NAME"));
                }
            }
        } catch (SQLException e) {
            handleException("Failed while getting unversioned local scopes for API:" + apiIdentifier + " tenant: "
                    + tenantId, e);
        }
        return localScopes;
    }

    /**
     * Get the versioned local scope keys set of the API.
     *
     * @param apiIdentifier API Identifier
     * @param tenantId      Tenant Id
     * @return Local Scope keys set
     * @throws APIManagementException if fails to get local scope keys for API
     */
    public Set<String> getVersionedLocalScopeKeysForAPI(APIIdentifier apiIdentifier, int tenantId)
            throws APIManagementException {

        int apiId;
        Set<String> localScopes = new HashSet<>();
        String getUnVersionedLocalScopes = SQLConstants.GET_VERSIONED_LOCAL_SCOPES_FOR_API_SQL;
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(getUnVersionedLocalScopes)) {
            apiId = getAPIID(apiIdentifier, connection);
            preparedStatement.setInt(1, apiId);
            preparedStatement.setInt(2, tenantId);
            preparedStatement.setInt(3, tenantId);
            preparedStatement.setInt(4, apiId);
            preparedStatement.setInt(5, tenantId);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    localScopes.add(rs.getString("SCOPE_NAME"));
                }
            }
        } catch (SQLException e) {
            handleException("Failed while getting versioned local scopes for API:" + apiIdentifier + " tenant: "
                    + tenantId, e);
        }
        return localScopes;
    }

    /**
     * Get the local scope keys set of the API.
     *
     * @param apiIdentifier API Identifier
     * @param tenantId      Tenant Id
     * @return Local Scope keys set
     * @throws APIManagementException if fails to get local scope keys for API
     */
    public Set<String> getAllLocalScopeKeysForAPI(APIIdentifier apiIdentifier, int tenantId)
            throws APIManagementException {

        int apiId;
        Set<String> localScopes = new HashSet<>();
        String getAllLocalScopesStmt = SQLConstants.GET_ALL_LOCAL_SCOPES_FOR_API_SQL;
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(getAllLocalScopesStmt)) {
            apiId = getAPIID(apiIdentifier, connection);
            preparedStatement.setInt(1, apiId);
            preparedStatement.setInt(2, tenantId);
            preparedStatement.setInt(3, tenantId);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    localScopes.add(rs.getString("SCOPE_NAME"));
                }
            }
        } catch (SQLException e) {
            handleException("Failed while getting local scopes for API:" + apiIdentifier + " tenant: " + tenantId, e);
        }
        return localScopes;
    }

    public Map<String, String> getScopeRolesOfApplication(String consumerKey) throws APIManagementException {
        Connection conn = null;
        ResultSet resultSet = null;
        ResultSet resultSet1 = null;
        ResultSet resultSet2 = null;
        PreparedStatement ps = null;
        PreparedStatement getSubscribedApisAndProducts = null;
        PreparedStatement getIncludedApisInProduct = null;

        Set<Integer> apiIdSet = new HashSet<>();

        try {
            conn = APIMgtDBUtil.getConnection();
            String sqlQueryForGetSubscribedApis = SQLConstants.GET_SUBSCRIBED_APIS_FROM_CONSUMER_KEY;
            getSubscribedApisAndProducts = conn.prepareStatement(sqlQueryForGetSubscribedApis);
            getSubscribedApisAndProducts.setString(1, consumerKey);
            resultSet1 = getSubscribedApisAndProducts.executeQuery();
            while (resultSet1.next()) {
                int apiId = resultSet1.getInt("API_ID");
                String getIncludedApisInProductQuery = SQLConstants.GET_INCLUDED_APIS_IN_PRODUCT_SQL;
                getIncludedApisInProduct = conn.prepareStatement(getIncludedApisInProductQuery);
                getIncludedApisInProduct.setInt(1, apiId);
                resultSet2 = getIncludedApisInProduct.executeQuery();
                while (resultSet2.next()) {
                    int includedApiId = resultSet2.getInt("API_ID");
                    apiIdSet.add(includedApiId);
                }
                apiIdSet.add(apiId);
            }
            Map<String, String> scopes = new HashMap<String, String>();
            if (!apiIdSet.isEmpty()) {
                String apiIdList = StringUtils.join(apiIdSet, ", ");
                String sqlQuery =
                        SQLConstants.GET_SCOPE_ROLES_OF_APPLICATION_SQL + apiIdList + SQLConstants.CLOSING_BRACE;
                ps = conn.prepareStatement(sqlQuery);
                resultSet = ps.executeQuery();
                while (resultSet.next()) {
                    if (scopes.containsKey(resultSet.getString(1))) {
                        // Role for the scope exists. Append the new role.
                        String roles = scopes.get(resultSet.getString(1));
                        roles += "," + resultSet.getString(2);
                        scopes.put(resultSet.getString(1), roles);
                    } else {
                        scopes.put(resultSet.getString(1), resultSet.getString(2));
                    }
                }
            }
            return scopes;
        } catch (SQLException e) {
            handleException("Failed to retrieve scopes of application" + consumerKey, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, resultSet);
            APIMgtDBUtil.closeAllConnections(getSubscribedApisAndProducts, null, resultSet1);
            APIMgtDBUtil.closeAllConnections(getIncludedApisInProduct, null, resultSet2);
        }
        return null;
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

    /**
     * Check whether another API with a different letter case of the given api name is already available in the api
     * table under the given tenant domain
     *
     * @param apiName      candidate api name
     * @param tenantDomain tenant domain name
     * @return true if a different letter case name is already available
     * @throws APIManagementException If failed to check different letter case api name availability
     */
    public boolean isApiNameWithDifferentCaseExist(String apiName, String tenantDomain) throws APIManagementException {
        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet resultSet = null;
        String contextParam = "/t/";

        String query = SQLConstants.GET_API_NAME_DIFF_CASE_NOT_MATCHING_CONTEXT_SQL;
        if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            query = SQLConstants.GET_API_NAME_DIFF_CASE_MATCHING_CONTEXT_SQL;
            contextParam += tenantDomain + '/';
        }

        try {
            connection = APIMgtDBUtil.getConnection();

            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, apiName);
            prepStmt.setString(2, contextParam + '%');
            prepStmt.setString(3, apiName);
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
            handleException("Failed to check different letter case api name availability : " + apiName, e);
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
     * Check whether the given scope key is already assigned locally to another API which are different from the given
     * API or its versioned APIs under given tenant.
     *
     * @param apiIdentifier API Identifier
     * @param scopeKey      candidate scope key
     * @param tenantId      tenant id
     * @return true if the scope key is already available
     * @throws APIManagementException if failed to check the context availability
     */
    public boolean isScopeKeyAssignedLocally(APIIdentifier apiIdentifier, String scopeKey, int tenantId)
            throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQLConstants.IS_SCOPE_ATTACHED_LOCALLY)) {
            statement.setString(1, scopeKey);
            statement.setInt(2, tenantId);
            statement.setInt(3, tenantId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    String provider = rs.getString("API_PROVIDER");
                    String apiName = rs.getString("API_NAME");
                    // Check if the provider name and api name is same.
                    // Return false if we're attaching the scope to another version of the API.
                    return !(provider.equals(APIUtil.replaceEmailDomainBack(apiIdentifier.getProviderName()))
                            && apiName.equals(apiIdentifier.getApiName()));
                }
            }
        } catch (SQLException e) {
            handleException("Failed to check scope key availability for: " + scopeKey, e);
        }
        return false;
    }

    /**
     * Check the given scopeKey assigned to any API resource in the given tenant.
     *
     * @param scopeKey Scope Key
     * @param tenantId Tenant Id
     * @return Whether scope assigned or not
     * @throws APIManagementException If an error occurs while checking scope assignment
     */
    public boolean isScopeKeyAssigned(String scopeKey, int tenantId) throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQLConstants.IS_SCOPE_ATTACHED)) {
            statement.setString(1, scopeKey);
            statement.setInt(2, tenantId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return true;
                }
            }
        } catch (SQLException e) {
            handleException("Failed to check scope key to API assignment for scope: " + scopeKey, e);
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
            ps.setString(1, contextTemplate.toLowerCase());

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
     * retrieve list of API names which matches given context
     *
     * @param contextTemplate context template
     * @return list of API names
     * @throws APIManagementException
     */
    public List<String> getAPINamesMatchingContext(String contextTemplate) throws APIManagementException {
        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        List<String> nameList = new ArrayList<String>();

        String sqlQuery = SQLConstants.GET_API_NAMES_MATCHES_CONTEXT;
        try {
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            ps.setString(1, contextTemplate);

            resultSet = ps.executeQuery();
            while (resultSet.next()) {
                nameList.add(resultSet.getString("API_NAME"));
            }
        } catch (SQLException e) {
            handleException("Failed to get API names matches context " + contextTemplate, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, resultSet);
        }
        return nameList;
    }

    /**
     * Check if key mappings already exists for app ID, key manager name or ID and key type.
     *
     * @param applicationId  app ID
     * @param keyManagerName key manager name
     * @param keyManagerId   key manager ID
     * @param keyType        key type
     * @return true if key mapping exists
     * @throws APIManagementException if an error occurs
     */
    public boolean isKeyMappingExistsForApplication(int applicationId, String keyManagerName,
                                                    String keyManagerId, String keyType)
            throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement preparedStatement = connection
                     .prepareStatement(SQLConstants.IS_KEY_MAPPING_EXISTS_FOR_APP_ID_KEY_TYPE)) {
            preparedStatement.setInt(1, applicationId);
            preparedStatement.setString(2, keyType);
            preparedStatement.setString(3, keyManagerName);
            preparedStatement.setString(4, keyManagerId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            handleException("Error while checking Key Mapping existence", e);
        }
        return false;
    }

    /**
     * Check if key mapping exists for (app ID, key type and key manager) or (consumer key and key manager) values.
     *
     * @param applicationId AppID
     * @param keyManagerName KeyManager Name
     * @param keyManagerId KeyManager Id
     * @param keyType KeyType
     * @param consumerKey   Consumer Key
     * @return true if key mapping exists
     * @throws APIManagementException if an error occurs.
     */
    public boolean isKeyMappingExistsForConsumerKeyOrApplication(int applicationId, String keyManagerName,
                                                                 String keyManagerId, String keyType,
                                                                 String consumerKey) throws APIManagementException {
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement preparedStatement = connection
                     .prepareStatement(SQLConstants.IS_KEY_MAPPING_EXISTS_FOR_APP_ID_KEY_TYPE_OR_CONSUMER_KEY)) {
            preparedStatement.setInt(1, applicationId);
            preparedStatement.setString(2, keyType);
            preparedStatement.setString(3, consumerKey);
            preparedStatement.setString(4, keyManagerName);
            preparedStatement.setString(5, keyManagerId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            handleException("Error while checking Key Mapping existence for AppId, KeyType or Consumer Key", e);
        }
        return false;
    }

    /**
     * @param applicationId
     * @param keyType
     * @return
     */
    public Map<String,String> getConsumerkeyByApplicationIdAndKeyType(int applicationId, String keyType)
            throws APIManagementException {
        Map<String,String> keyManagerConsumerKeyMap = new HashMap<>();
        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        try {
            conn = APIMgtDBUtil.getConnection();

            String sqlQuery = SQLConstants.GET_CONSUMER_KEY_BY_APPLICATION_AND_KEY_SQL;

            ps = conn.prepareStatement(sqlQuery);
            ps.setInt(1, applicationId);
            ps.setString(2, keyType);
            resultSet = ps.executeQuery();

            while (resultSet.next()) {
                String consumerKey = resultSet.getString("CONSUMER_KEY");
                String keyManager = resultSet.getString("KEY_MANAGER");
                keyManagerConsumerKeyMap.put(keyManager, consumerKey);
            }
        } catch (SQLException e) {
            handleException("Failed to get consumer key by applicationId " + applicationId + "and keyType " +
                    keyType, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, resultSet);
        }
        return keyManagerConsumerKeyMap;
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
     *
     * @param stakeHolder the name of the stakeholder. whether its "subscriber", "publisher" or
     *                    "admin-dashboard"
     * @return List of alert types
     * @throws APIManagementException
     */
    public HashMap<Integer, String> getAllAlertTypesByStakeHolder(String stakeHolder) throws APIManagementException {
        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        HashMap<Integer, String> map = new HashMap<Integer, String>();

        try {
            conn = APIMgtDBUtil.getConnection();
            String sqlQuery;
            if (stakeHolder.equals("admin-dashboard")) {
                sqlQuery = SQLConstants.GET_ALL_ALERT_TYPES_FOR_ADMIN;
                ps = conn.prepareStatement(sqlQuery);
            } else {
                sqlQuery = SQLConstants.GET_ALL_ALERT_TYPES;
                ps = conn.prepareStatement(sqlQuery);
                ps.setString(1, stakeHolder);
            }

            resultSet = ps.executeQuery();
            while (resultSet.next()) {
                map.put(resultSet.getInt(1), resultSet.getString(2));
            }
        } catch (SQLException e) {
            handleException("Failed to retrieve alert types ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, resultSet);
        }
        return map;
    }

    /**
     * @param userName    user name with tenant domain ex: admin@carbon.super
     * @param stakeHolder value "p" for publisher value "s" for subscriber value "a" for admin
     * @return map of saved values of alert types.
     * @throws APIManagementException
     */
    public List<Integer> getSavedAlertTypesIdsByUserNameAndStakeHolder(String userName, String stakeHolder) throws APIManagementException {
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
            ps.setString(2, stakeHolder);
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
     *
     * @param userName    user name.
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
            ps.setString(2, stakeHolder);
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
     *
     * @param userName
     * @param agent    whether its publisher or store or admin dash board.
     */
    public void unSubscribeAlerts(String userName, String agent) throws APIManagementException {

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
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
            ps.setString(2, agent);
            rs = ps.executeQuery();
            int emailListId = 0;
            while (rs.next()) {
                emailListId = rs.getInt(1);
            }
            if (emailListId != 0) {
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
     * @param userName         User name.
     * @param emailList        Comma separated email list.
     * @param alertTypesIDList Comma separated alert types list.
     * @param stakeHolder      if pram value = p we assume those changes from publisher if param value = s those data belongs to
     *                         subscriber.
     * @throws APIManagementException
     * @throws SQLException
     */
    public void addAlertTypesConfigInfo(String userName, String emailList, String alertTypesIDList, String stakeHolder)
            throws APIManagementException {

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);

            String alertTypesQuery = SQLConstants.ADD_ALERT_TYPES_VALUES;

            String deleteAlertTypesByUserNameAndStakeHolderQuery = SQLConstants.DELETE_ALERTTYPES_BY_USERNAME_AND_STAKE_HOLDER;

            ps = connection.prepareStatement(deleteAlertTypesByUserNameAndStakeHolderQuery);
            ps.setString(1, userName);
            ps.setString(2, stakeHolder);
            ps.executeUpdate();

            if (!StringUtils.isEmpty(alertTypesIDList)) {

                List<String> alertTypeIdList = Arrays.asList(alertTypesIDList.split(","));

                for (String alertTypeId : alertTypeIdList) {
                    PreparedStatement psAlertTypeId = null;
                    try {
                        psAlertTypeId = connection.prepareStatement(alertTypesQuery);
                        psAlertTypeId.setInt(1, Integer.parseInt(alertTypeId));
                        psAlertTypeId.setString(2, userName);
                        psAlertTypeId.setString(3, stakeHolder);
                        psAlertTypeId.execute();
                    } catch (SQLException e) {
                        handleException("Error while adding alert types", e);
                    } finally {
                        APIMgtDBUtil.closeAllConnections(psAlertTypeId, null, null);
                    }
                }


            }

            String getEmailListIdByUserNameAndStakeHolderQuery = SQLConstants.GET_EMAILLISTID_BY_USERNAME_AND_STAKEHOLDER;
            ps = connection.prepareStatement(getEmailListIdByUserNameAndStakeHolderQuery);
            ps.setString(1, userName);
            ps.setString(2, stakeHolder);
            rs = ps.executeQuery();
            int emailListId = 0;
            while (rs.next()) {
                emailListId = rs.getInt(1);
            }
            if (emailListId != 0) {
                String deleteEmailListDetailsByEmailListId = SQLConstants.DELETE_EMAILLIST_BY_EMAIL_LIST_ID;
                ps = connection.prepareStatement(deleteEmailListDetailsByEmailListId);
                ps.setInt(1, emailListId);
                ps.executeUpdate();

                if (!StringUtils.isEmpty(emailList)) {

                    List<String> extractedEmailList = Arrays.asList(emailList.split(","));

                    String saveEmailListDetailsQuery = SQLConstants.SAVE_EMAIL_LIST_DETAILS_QUERY;

                    for (String email : extractedEmailList) {
                        PreparedStatement extractedEmailListPs = null;
                        try {
                            extractedEmailListPs = connection.prepareStatement(saveEmailListDetailsQuery);
                            extractedEmailListPs.setInt(1, emailListId);
                            extractedEmailListPs.setString(2, email);
                            extractedEmailListPs.execute();
                        } catch (SQLException e) {
                            handleException("Error while save email list.", e);
                        } finally {
                            APIMgtDBUtil.closeAllConnections(extractedEmailListPs, null, null);
                        }
                    }

                }

            } else {

                String emailListSaveQuery = SQLConstants.ADD_ALERT_EMAIL_LIST;

                String dbProductName = connection.getMetaData().getDatabaseProductName();

                ps = connection.prepareStatement(emailListSaveQuery, new String[]{DBUtils.
                        getConvertedAutoGeneratedColumnName(dbProductName, "EMAIL_LIST_ID")});

                ps.setString(1, userName);
                ps.setString(2, stakeHolder);
                ps.execute();

                rs = ps.getGeneratedKeys();

                if (rs.next()) {
                    int generatedEmailIdList = rs.getInt(1);
                    if (!StringUtils.isEmpty(emailList)) {

                        List<String> extractedEmailList = Arrays.asList(emailList.split(","));

                        String saveEmailListDetailsQuery = SQLConstants.SAVE_EMAIL_LIST_DETAILS_QUERY;

                        for (String email : extractedEmailList) {
                            PreparedStatement elseExtractedEmailListPS = null;
                            try {
                                elseExtractedEmailListPS = connection.prepareStatement(saveEmailListDetailsQuery);
                                elseExtractedEmailListPS.setInt(1, generatedEmailIdList);
                                elseExtractedEmailListPS.setString(2, email);
                                elseExtractedEmailListPS.execute();
                            } catch (SQLException e) {
                                handleException("Error while save email list.", e);
                            } finally {
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
            if (policy.getCustomAttributes() != null) {
                hasCustomAttrib = true;
            }
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);
            String addQuery = SQLConstants.INSERT_APPLICATION_POLICY_SQL;
            if (hasCustomAttrib) {
                addQuery = SQLConstants.INSERT_APPLICATION_POLICY_WITH_CUSTOM_ATTRIB_SQL;
            }
            policyStatement = conn.prepareStatement(addQuery);
            setCommonParametersForPolicy(policyStatement, policy);
            if (hasCustomAttrib) {
                policyStatement.setBlob(12, new ByteArrayInputStream(policy.getCustomAttributes()));
            }
            policyStatement.executeUpdate();

            conn.commit();
        } catch (SQLIntegrityConstraintViolationException e){
            boolean isAppPolicyExists = isPolicyExist(conn, PolicyConstants.POLICY_LEVEL_APP, policy.getTenantId(),
                    policy.getPolicyName());

            if (isAppPolicyExists) {
                log.warn(
                        "Application Policy " + policy.getPolicyName() + " in tenant domain " + policy.getTenantId()
                                + " is already persisted");
            } else {
                handleException("Failed to add Application Policy: " + policy, e);
            }
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {

                    // Rollback failed. Exception will be thrown later for upper exception
                    log.error("Failed to rollback the add Application Policy: " + policy.toString(), ex);
                }
            }
            if (StringUtils.containsIgnoreCase(e.getMessage(), "Violation of UNIQUE KEY constraint")) {
                boolean isAppPolicyExists = isPolicyExist(conn, PolicyConstants.POLICY_LEVEL_APP, policy.getTenantId(),
                        policy.getPolicyName());

                if (isAppPolicyExists) {
                    log.warn("Application Policy " + policy.getPolicyName() + " in tenant domain " + policy.getTenantId()
                            + " is already persisted");
                }
            } else {
                handleException("Failed to add Application Policy: " + policy, e);
            }
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
            if (policy.getCustomAttributes() != null) {
                hasCustomAttrib = true;
            }
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);
            String addQuery = SQLConstants.INSERT_SUBSCRIPTION_POLICY_SQL;
            if (hasCustomAttrib) {
                addQuery = SQLConstants.INSERT_SUBSCRIPTION_POLICY_WITH_CUSTOM_ATTRIB_SQL;
            }
            policyStatement = conn.prepareStatement(addQuery);
            setCommonParametersForPolicy(policyStatement, policy);
            policyStatement.setInt(12, policy.getRateLimitCount());
            policyStatement.setString(13, policy.getRateLimitTimeUnit());
            policyStatement.setBoolean(14, policy.isStopOnQuotaReach());
            policyStatement.setInt(15, policy.getGraphQLMaxDepth());
            policyStatement.setInt(16, policy.getGraphQLMaxComplexity());
            policyStatement.setString(17, policy.getBillingPlan());
            if (hasCustomAttrib) {
                policyStatement.setBytes(18, policy.getCustomAttributes());
                policyStatement.setString(19, policy.getMonetizationPlan());
                policyStatement.setString(20, policy.getMonetizationPlanProperties().get(APIConstants.Monetization.FIXED_PRICE));
                policyStatement.setString(21, policy.getMonetizationPlanProperties().get(APIConstants.Monetization.BILLING_CYCLE));
                policyStatement.setString(22, policy.getMonetizationPlanProperties().get(APIConstants.Monetization.PRICE_PER_REQUEST));
                policyStatement.setString(23, policy.getMonetizationPlanProperties().get(APIConstants.Monetization.CURRENCY));
            } else {
                policyStatement.setString(18, policy.getMonetizationPlan());
                policyStatement.setString(19, policy.getMonetizationPlanProperties().get(APIConstants.Monetization.FIXED_PRICE));
                policyStatement.setString(20, policy.getMonetizationPlanProperties().get(APIConstants.Monetization.BILLING_CYCLE));
                policyStatement.setString(21, policy.getMonetizationPlanProperties().get(APIConstants.Monetization.PRICE_PER_REQUEST));
                policyStatement.setString(22, policy.getMonetizationPlanProperties().get(APIConstants.Monetization.CURRENCY));
            }
            policyStatement.executeUpdate();
            conn.commit();
        } catch (SQLIntegrityConstraintViolationException e) {
            boolean isSubscriptionPolicyExists = isPolicyExist(conn, PolicyConstants.POLICY_LEVEL_SUB, policy.getTenantId(),
                    policy.getPolicyName());

            if (isSubscriptionPolicyExists) {
                log.warn(
                        "Subscription Policy " + policy.getPolicyName() + " in tenant domain " + policy.getTenantId()
                                + " is already persisted");
            } else {
                handleException("Failed to add Subscription Policy: " + policy, e);
            }
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {

                    // Rollback failed. Exception will be thrown later for upper exception
                    log.error("Failed to rollback the add Subscription Policy: " + policy.toString(), ex);
                }
            }
            if (StringUtils.containsIgnoreCase(e.getMessage(), "Violation of UNIQUE KEY constraint")) {
                boolean isSubscriptionPolicyExists = isPolicyExist(conn, PolicyConstants.POLICY_LEVEL_SUB, policy.getTenantId(),
                        policy.getPolicyName());

                if (isSubscriptionPolicyExists) {
                    log.warn("Subscription Policy " + policy.getPolicyName() + " in tenant domain " + policy.getTenantId()
                            + " is already persisted");
                }
            } else {
                handleException("Failed to add Subscription Policy: " + policy, e);
            }
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
        } catch (SQLIntegrityConstraintViolationException e) {
            boolean isAPIPolicyExists = isPolicyExist(connection, PolicyConstants.POLICY_LEVEL_API, policy.getTenantId(),
                    policy.getPolicyName());

            if (isAPIPolicyExists) {
                log.warn(
                        "API Policy " + policy.getPolicyName() + " in tenant domain " + policy.getTenantId()
                                + " is already persisted");
            } else {
                handleException("Failed to add API Policy: " + policy, e);
            }
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {

                    // Rollback failed. Exception will be thrown later for upper exception
                    log.error("Failed to rollback the add Api Policy: " + policy.toString(), ex);
                }
            }
            if (StringUtils.containsIgnoreCase(e.getMessage(), "Violation of UNIQUE KEY constraint")) {
                boolean isAPIPolicyExists = isPolicyExist(connection, PolicyConstants.POLICY_LEVEL_API, policy.getTenantId(),
                        policy.getPolicyName());

                if (isAPIPolicyExists) {
                    log.warn("API Policy " + policy.getPolicyName() + " in tenant domain " + policy.getTenantId()
                            + " is already persisted");
                }
            } else {
                handleException("Failed to add Api Policy: " + policy, e);
            }
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
        String addQuery = SQLConstants.ThrottleSQLConstants.INSERT_API_POLICY_SQL;
        int policyId;

        try {
            String dbProductName = conn.getMetaData().getDatabaseProductName();
            policyStatement = conn.prepareStatement(addQuery,
                    new String[]{DBUtils.getConvertedAutoGeneratedColumnName(dbProductName, "POLICY_ID")});
            setCommonParametersForPolicy(policyStatement, policy);
            policyStatement.setString(12, policy.getUserLevel());
            policyStatement.executeUpdate();
            resultSet = policyStatement.getGeneratedKeys(); // Get the inserted POLICY_ID (auto incremented value)

            // Returns only single row
            if (resultSet.next()) {

                /*
                 *  H2 doesn't return generated keys when key is provided (not generated).
                   Therefore policyId should be policy parameter's policyId when it is provided.
                 */
                policyId = resultSet.getInt(1);
                List<Pipeline> pipelines = policy.getPipelines();
                if (pipelines != null) {
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
     * Update a API level throttling policy to database.
     * <p>
     * If condition group already exists for the policy, that condition Group will be deleted and condition Group will
     * be inserted to the database with old POLICY_ID.
     * </p>
     *
     * @param policy policy object defining the throttle policy
     * @throws APIManagementException
     */
    public APIPolicy updateAPIPolicy(APIPolicy policy) throws APIManagementException {
        String updateQuery;
        int policyId = 0;
        String selectQuery;
        if (policy != null) {
            if (!StringUtils.isBlank(policy.getPolicyName()) && policy.getTenantId() != -1) {
                selectQuery = SQLConstants.ThrottleSQLConstants.GET_API_POLICY_ID_SQL;
                updateQuery = SQLConstants.ThrottleSQLConstants.UPDATE_API_POLICY_SQL;
            } else if (!StringUtils.isBlank(policy.getUUID())) {
                selectQuery = SQLConstants.ThrottleSQLConstants.GET_API_POLICY_ID_BY_UUID_SQL;
                updateQuery = ThrottleSQLConstants.UPDATE_API_POLICY_BY_UUID_SQL;
            } else {
                String errorMsg = "Policy object doesn't contain mandatory parameters. At least UUID or Name,Tenant Id"
                                + " should be provided. Name: " + policy.getPolicyName()
                                + ", Tenant Id: " + policy.getTenantId() + ", UUID: " + policy.getUUID();
                log.error(errorMsg);
                throw new APIManagementException(errorMsg);
            }
        } else {
            String errorMsg = "Provided Policy to update is null";
            log.error(errorMsg);
            throw new APIManagementException(errorMsg);
        }

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            connection.setAutoCommit(false);
            try ( PreparedStatement selectStatement = connection.prepareStatement(selectQuery);
                  PreparedStatement deleteStatement = connection.prepareStatement(SQLConstants
                          .ThrottleSQLConstants.DELETE_CONDITION_GROUP_SQL);
                  PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
                if (selectQuery.equals(SQLConstants.ThrottleSQLConstants.GET_API_POLICY_ID_SQL)) {
                    selectStatement .setString(1, policy.getPolicyName());
                    selectStatement .setInt(2, policy.getTenantId());
                } else {
                    selectStatement .setString(1, policy.getUUID());
                }
                try (ResultSet resultSet = selectStatement.executeQuery()) {
                    if (resultSet.next()) {
                        policyId = resultSet.getInt(ThrottlePolicyConstants.COLUMN_POLICY_ID);
                    }
                }
                deleteStatement.setInt(1, policyId);
                deleteStatement.executeUpdate();
                if (!StringUtils.isEmpty(policy.getDisplayName())) {
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

                if (!StringUtils.isBlank(policy.getPolicyName()) && policy.getTenantId() != -1) {
                    updateStatement.setString(8, policy.getPolicyName());
                    updateStatement.setInt(9, policy.getTenantId());
                } else if (!StringUtils.isBlank(policy.getUUID())) {
                    updateStatement.setString(8, policy.getUUID());
                }
                int updatedRawCount = updateStatement.executeUpdate();
                if (updatedRawCount > 0) {
                    List<Pipeline> pipelines = policy.getPipelines();
                    if (pipelines != null) {
                        for (Pipeline pipeline : pipelines) { // add each pipeline data to AM_CONDITION_GROUP table
                            addPipeline(pipeline, policyId, connection);
                        }
                    }
                }
                connection.commit();
            } catch (SQLException e) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    // rollback failed. exception will be thrown later for upper exception
                    log.error("Failed to rollback the add Global Policy: " + policy.toString(), ex);
                }
                handleException("Failed to update API policy: " + policy.getPolicyName() + '-' + policy.getTenantId(), e);
            }
        } catch (SQLException e) {
            handleException("Failed to update API policy: " + policy.getPolicyName() + '-' + policy.getTenantId(), e);
        }
        return policy;
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
            conditionStatement = conn.prepareStatement(sqlAddQuery, new String[]{DBUtils
                    .getConvertedAutoGeneratedColumnName(dbProductName, "CONDITION_GROUP_ID")});
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
            policyStatement.setBinaryStream(5, siddhiQueryInputStream, lengthOfBytes);
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
                subPolicy.setGraphQLMaxDepth(rs.getInt(ThrottlePolicyConstants.COLUMN_MAX_DEPTH));
                subPolicy.setGraphQLMaxComplexity(rs.getInt(ThrottlePolicyConstants.COLUMN_MAX_COMPLEXITY));
                subPolicy.setMonetizationPlan(rs.getString(ThrottlePolicyConstants.COLUMN_MONETIZATION_PLAN));
                Map<String, String> monetizationPlanProperties = subPolicy.getMonetizationPlanProperties();
                monetizationPlanProperties.put(APIConstants.Monetization.FIXED_PRICE,
                        rs.getString(ThrottlePolicyConstants.COLUMN_FIXED_RATE));
                monetizationPlanProperties.put(APIConstants.Monetization.BILLING_CYCLE,
                        rs.getString(ThrottlePolicyConstants.COLUMN_BILLING_CYCLE));
                monetizationPlanProperties.put(APIConstants.Monetization.PRICE_PER_REQUEST,
                        rs.getString(ThrottlePolicyConstants.COLUMN_PRICE_PER_REQUEST));
                monetizationPlanProperties.put(APIConstants.Monetization.CURRENCY,
                        rs.getString(ThrottlePolicyConstants.COLUMN_CURRENCY));
                subPolicy.setMonetizationPlanProperties(monetizationPlanProperties);
                InputStream binary = rs.getBinaryStream(ThrottlePolicyConstants.COLUMN_CUSTOM_ATTRIB);
                if (binary != null) {
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
     * Get subscription level policies specified by tier names belonging to a specific tenant
     *
     * @param subscriptionTiers subscription tiers
     * @param tenantID          tenantID filters the polices belongs to specific tenant
     * @return subscriptionPolicy array list
     */
    public SubscriptionPolicy[] getSubscriptionPolicies(String[] subscriptionTiers, int tenantID) throws APIManagementException {
        List<SubscriptionPolicy> policies = new ArrayList<SubscriptionPolicy>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        List<String> questionMarks = new ArrayList<>(Collections.nCopies(subscriptionTiers.length, "?"));
        String parameterString = String.join(",", questionMarks);

        String sqlQuery = SQLConstants.GET_SUBSCRIPTION_POLICIES_BY_POLICY_NAMES_PREFIX +
                parameterString + SQLConstants.GET_SUBSCRIPTION_POLICIES_BY_POLICY_NAMES_SUFFIX;

        try {
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            int i = 1;
            for (String subscriptionTier : subscriptionTiers) {
                ps.setString(i, subscriptionTier);
                i++;
            }
            ps.setInt(i, tenantID);
            rs = ps.executeQuery();
            while (rs.next()) {
                SubscriptionPolicy subPolicy = new SubscriptionPolicy(
                        rs.getString(ThrottlePolicyConstants.COLUMN_NAME));
                setCommonPolicyDetails(subPolicy, rs);
                subPolicy.setRateLimitCount(rs.getInt(ThrottlePolicyConstants.COLUMN_RATE_LIMIT_COUNT));
                subPolicy.setRateLimitTimeUnit(rs.getString(ThrottlePolicyConstants.COLUMN_RATE_LIMIT_TIME_UNIT));
                subPolicy.setStopOnQuotaReach(rs.getBoolean(ThrottlePolicyConstants.COLUMN_STOP_ON_QUOTA_REACH));
                subPolicy.setBillingPlan(rs.getString(ThrottlePolicyConstants.COLUMN_BILLING_PLAN));
                subPolicy.setGraphQLMaxDepth(rs.getInt(ThrottlePolicyConstants.COLUMN_MAX_DEPTH));
                subPolicy.setGraphQLMaxComplexity(rs.getInt(ThrottlePolicyConstants.COLUMN_MAX_COMPLEXITY));
                subPolicy.setMonetizationPlan(rs.getString(ThrottlePolicyConstants.COLUMN_MONETIZATION_PLAN));
                subPolicy.setTierQuotaType(rs.getString(ThrottlePolicyConstants.COLUMN_QUOTA_POLICY_TYPE));
                Map<String, String> monetizationPlanProperties = subPolicy.getMonetizationPlanProperties();
                monetizationPlanProperties.put(APIConstants.Monetization.FIXED_PRICE,
                        rs.getString(ThrottlePolicyConstants.COLUMN_FIXED_RATE));
                monetizationPlanProperties.put(APIConstants.Monetization.BILLING_CYCLE,
                        rs.getString(ThrottlePolicyConstants.COLUMN_BILLING_CYCLE));
                monetizationPlanProperties.put(APIConstants.Monetization.PRICE_PER_REQUEST,
                        rs.getString(ThrottlePolicyConstants.COLUMN_PRICE_PER_REQUEST));
                monetizationPlanProperties.put(APIConstants.Monetization.CURRENCY,
                        rs.getString(ThrottlePolicyConstants.COLUMN_CURRENCY));
                subPolicy.setMonetizationPlanProperties(monetizationPlanProperties);
                InputStream binary = rs.getBinaryStream(ThrottlePolicyConstants.COLUMN_CUSTOM_ATTRIB);
                if (binary != null) {
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
                globalPolicy.setTenantId(rs.getInt(ThrottlePolicyConstants.COLUMN_TENANT_ID));
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
                globalPolicy.setTenantId(rs.getInt(ThrottlePolicyConstants.COLUMN_TENANT_ID));
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
                globalPolicy.setTenantId(rs.getInt(ThrottlePolicyConstants.COLUMN_TENANT_ID));
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
                policy.setGraphQLMaxDepth(resultSet.getInt(ThrottlePolicyConstants.COLUMN_MAX_DEPTH));
                policy.setGraphQLMaxComplexity(resultSet.getInt(ThrottlePolicyConstants.COLUMN_MAX_COMPLEXITY));
                InputStream binary = resultSet.getBinaryStream(ThrottlePolicyConstants.COLUMN_CUSTOM_ATTRIB);
                if (binary != null) {
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
                policy.setGraphQLMaxDepth(resultSet.getInt(ThrottlePolicyConstants.COLUMN_MAX_DEPTH));
                policy.setGraphQLMaxComplexity(resultSet.getInt(ThrottlePolicyConstants.COLUMN_MAX_COMPLEXITY));
                InputStream binary = resultSet.getBinaryStream(ThrottlePolicyConstants.COLUMN_CUSTOM_ATTRIB);
                if (binary != null) {
                    byte[] customAttrib = APIUtil.toByteArray(binary);
                    policy.setCustomAttributes(customAttrib);
                }
                if (APIConstants.COMMERCIAL_TIER_PLAN.equals(policy.getBillingPlan())) {
                    policy.setMonetizationPlan(resultSet.getString(ThrottlePolicyConstants.COLUMN_MONETIZATION_PLAN));
                    Map<String, String> tierMonetizationProperties = new HashMap<>();
                    tierMonetizationProperties.put(APIConstants.Monetization.CURRENCY, resultSet
                            .getString(ThrottlePolicyConstants.COLUMN_CURRENCY));
                    tierMonetizationProperties.put(APIConstants.Monetization.BILLING_CYCLE, resultSet
                            .getString(ThrottlePolicyConstants.COLUMN_BILLING_CYCLE));
                    tierMonetizationProperties.put(APIConstants.Monetization.FIXED_PRICE, resultSet
                            .getString(ThrottlePolicyConstants.COLUMN_FIXED_RATE));
                    tierMonetizationProperties.put(APIConstants.Monetization.PRICE_PER_REQUEST, resultSet
                            .getString(ThrottlePolicyConstants.COLUMN_PRICE_PER_REQUEST));
                    policy.setMonetizationPlanProperties(tierMonetizationProperties);
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
            if (policy.getCustomAttributes() != null) {
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
            if (!StringUtils.isEmpty(policy.getDisplayName())) {
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

            if (hasCustomAttrib) {
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
            if (policy.getCustomAttributes() != null) {
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
            if (!StringUtils.isEmpty(policy.getDisplayName())) {
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
            updateStatement.setInt(11, policy.getGraphQLMaxDepth());
            updateStatement.setInt(12, policy.getGraphQLMaxComplexity());
            updateStatement.setString(13, policy.getBillingPlan());
            if (hasCustomAttrib) {
                long lengthOfStream = policy.getCustomAttributes().length;
                updateStatement.setBinaryStream(14, new ByteArrayInputStream(policy.getCustomAttributes()),
                        lengthOfStream);
                if (!StringUtils.isBlank(policy.getPolicyName()) && policy.getTenantId() != -1) {
                    updateStatement.setString(15, policy.getMonetizationPlan());
                    updateStatement.setString(16, policy.getMonetizationPlanProperties().get(APIConstants.Monetization.FIXED_PRICE));
                    updateStatement.setString(17, policy.getMonetizationPlanProperties().get(APIConstants.Monetization.BILLING_CYCLE));
                    updateStatement.setString(18, policy.getMonetizationPlanProperties().get(APIConstants.Monetization.PRICE_PER_REQUEST));
                    updateStatement.setString(19, policy.getMonetizationPlanProperties().get(APIConstants.Monetization.CURRENCY));
                    updateStatement.setString(20, policy.getPolicyName());
                    updateStatement.setInt(21, policy.getTenantId());
                } else if (!StringUtils.isBlank(policy.getUUID())) {
                    updateStatement.setString(15, policy.getMonetizationPlan());
                    updateStatement.setString(16, policy.getMonetizationPlanProperties().get(APIConstants.Monetization.FIXED_PRICE));
                    updateStatement.setString(17, policy.getMonetizationPlanProperties().get(APIConstants.Monetization.BILLING_CYCLE));
                    updateStatement.setString(18, policy.getMonetizationPlanProperties().get(APIConstants.Monetization.PRICE_PER_REQUEST));
                    updateStatement.setString(19, policy.getMonetizationPlanProperties().get(APIConstants.Monetization.CURRENCY));
                    updateStatement.setString(20, policy.getUUID());
                }
            } else {
                if (!StringUtils.isBlank(policy.getPolicyName()) && policy.getTenantId() != -1) {
                    updateStatement.setString(14, policy.getMonetizationPlan());
                    updateStatement.setString(15, policy.getMonetizationPlanProperties().get(APIConstants.Monetization.FIXED_PRICE));
                    updateStatement.setString(16, policy.getMonetizationPlanProperties().get(APIConstants.Monetization.BILLING_CYCLE));
                    updateStatement.setString(17, policy.getMonetizationPlanProperties().get(APIConstants.Monetization.PRICE_PER_REQUEST));
                    updateStatement.setString(18, policy.getMonetizationPlanProperties().get(APIConstants.Monetization.CURRENCY));
                    updateStatement.setString(19, policy.getPolicyName());
                    updateStatement.setInt(20, policy.getTenantId());

                } else if (!StringUtils.isBlank(policy.getUUID())) {
                    updateStatement.setString(14, policy.getMonetizationPlan());
                    updateStatement.setString(15, policy.getMonetizationPlanProperties().get(APIConstants.Monetization.FIXED_PRICE));
                    updateStatement.setString(16, policy.getMonetizationPlanProperties().get(APIConstants.Monetization.BILLING_CYCLE));
                    updateStatement.setString(17, policy.getMonetizationPlanProperties().get(APIConstants.Monetization.PRICE_PER_REQUEST));
                    updateStatement.setString(18, policy.getMonetizationPlanProperties().get(APIConstants.Monetization.CURRENCY));
                    updateStatement.setString(19, policy.getUUID());
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
        if (!StringUtils.isEmpty(policy.getDisplayName())) {
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
        policy.setTenantId(resultSet.getInt(ThrottlePolicyConstants.COLUMN_TENANT_ID));
        policy.setTenantDomain(IdentityTenantUtil.getTenantDomain(policy.getTenantId()));
        policy.setDefaultQuotaPolicy(quotaPolicy);
        policy.setDeployed(resultSet.getBoolean(ThrottlePolicyConstants.COLUMN_DEPLOYED));
    }

    public boolean isPolicyExist(String policyType, int tenantId, String policyName) throws APIManagementException {
        try (Connection connection = APIMgtDBUtil.getConnection();) {
            return isPolicyExist(connection, policyType, tenantId, policyName);
        } catch (SQLException e) {
            handleException("Error while checking policy existence " + policyName + "-" + tenantId, e);
        }
        return false;
    }

    public boolean isPolicyExist(Connection connection, String policyType, int tenantId, String policyName)
            throws APIManagementException {
        PreparedStatement isExistStatement = null;

        boolean isExist = false;
        String policyTable = null;
        if (PolicyConstants.POLICY_LEVEL_API.equalsIgnoreCase(policyType)) {
            policyTable = PolicyConstants.API_THROTTLE_POLICY_TABLE;
        } else if (PolicyConstants.POLICY_LEVEL_APP.equalsIgnoreCase(policyType)) {
            policyTable = PolicyConstants.POLICY_APPLICATION_TABLE;
        } else if (PolicyConstants.POLICY_LEVEL_GLOBAL.equalsIgnoreCase(policyType)) {
            policyTable = PolicyConstants.POLICY_GLOBAL_TABLE;
        } else if (PolicyConstants.POLICY_LEVEL_SUB.equalsIgnoreCase(policyType)) {
            policyTable = PolicyConstants.POLICY_SUBSCRIPTION_TABLE;
        }
        try {
            String query = "SELECT " + PolicyConstants.POLICY_ID + " FROM " + policyTable
                    + " WHERE TENANT_ID =? AND NAME = ? ";
            connection.setAutoCommit(true);
            isExistStatement = connection.prepareStatement(query);
            isExistStatement.setInt(1, tenantId);
            isExistStatement.setString(2, policyName);
            ResultSet result = isExistStatement.executeQuery();
            if (result != null && result.next()) {
                isExist = true;
            }
        } catch (SQLException e) {
            handleException("Failed to check is exist: " + policyName + '-' + tenantId, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(isExistStatement, connection, null);
        }
        return isExist;
    }

    public boolean isPolicyDeployed(String policyType, int tenantId, String policyName) throws APIManagementException {
        Connection connection = null;
        PreparedStatement isExistStatement = null;

        boolean isDeployed = false;
        String policyTable = null;
        if (PolicyConstants.POLICY_LEVEL_API.equalsIgnoreCase(policyType)) {
            policyTable = PolicyConstants.API_THROTTLE_POLICY_TABLE;
        } else if (PolicyConstants.POLICY_LEVEL_APP.equalsIgnoreCase(policyType)) {
            policyTable = PolicyConstants.POLICY_APPLICATION_TABLE;
        } else if (PolicyConstants.POLICY_LEVEL_GLOBAL.equalsIgnoreCase(policyType)) {
            policyTable = PolicyConstants.POLICY_GLOBAL_TABLE;
        } else if (PolicyConstants.POLICY_LEVEL_SUB.equalsIgnoreCase(policyType)) {
            policyTable = PolicyConstants.POLICY_SUBSCRIPTION_TABLE;
        }
        try {
            String query = "SELECT " + PolicyConstants.POLICY_IS_DEPLOYED + " FROM " + policyTable + " WHERE TENANT_ID =? AND NAME = ? ";
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(true);
            isExistStatement = connection.prepareStatement(query);
            isExistStatement.setInt(1, tenantId);
            isExistStatement.setString(2, policyName);
            ResultSet result = isExistStatement.executeQuery();
            if (result != null && result.next()) {
                isDeployed = result.getBoolean(PolicyConstants.POLICY_IS_DEPLOYED);
            }
        } catch (SQLException e) {
            handleException("Failed to check is exist: " + policyName + '-' + tenantId, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(isExistStatement, connection, null);
        }
        return isDeployed;
    }

    /**
     * Add a block condition
     *
     * @return uuid of the block condition if successfully added
     * @throws APIManagementException
     */
    public BlockConditionsDTO addBlockConditions(BlockConditionsDTO blockConditionsDTO) throws
            APIManagementException {
        Connection connection = null;
        PreparedStatement insertPreparedStatement = null;
        boolean status = false;
        boolean valid = false;
        ResultSet rs = null;
        String uuid = blockConditionsDTO.getUUID();
        String conditionType  = blockConditionsDTO.getConditionType();
        String conditionValue = blockConditionsDTO.getConditionValue();
        String tenantDomain = blockConditionsDTO.getTenantDomain();
        String conditionStatus = String.valueOf(blockConditionsDTO.isEnabled());
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
                                "name " + appName + " from Application " +
                                "Owner " + appOwner);
                    }
                }
            } else if (APIConstants.BLOCKING_CONDITIONS_USER.equals(conditionType)) {
                if (MultitenantUtils.getTenantDomain(conditionValue).equals(tenantDomain)) {
                    valid = true;
                } else {
                    throw new APIManagementException("Invalid User in Tenant Domain " + tenantDomain);
                }
            } else if (APIConstants.BLOCKING_CONDITIONS_IP.equals(conditionType) ||
                    APIConstants.BLOCK_CONDITION_IP_RANGE.equals(conditionType)) {
                valid = true;
            } else if (APIConstants.BLOCKING_CONDITIONS_SUBSCRIPTION.equals(conditionType)) {
                /* ATM this condition type will be used internally to handle subscription blockings for JWT type access
                   tokens.
                */
                String[] conditionsArray = conditionValue.split(":");
                if (conditionsArray.length > 0) {
                    String apiContext = conditionsArray[0];
                    String applicationIdentifier = conditionsArray[2];

                    String[] app = applicationIdentifier.split("-");
                    String appOwner = app[0];
                    String appName = app[1];

                    // Check whether the given api context exists in tenant
                    String extractedTenantDomain = MultitenantUtils.getTenantDomainFromRequestURL(apiContext);
                    if (extractedTenantDomain == null) {
                        extractedTenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
                    }
                    if (tenantDomain.equals(extractedTenantDomain) && isValidContext(apiContext)) {
                        valid = true;
                    } else {
                        throw new APIManagementException(
                                "Couldn't Save Subscription Block Condition Due to Invalid API Context "
                                        + apiContext);
                    }

                    // Check whether the given application is valid
                    if ((MultitenantUtils.getTenantDomain(appOwner).equals(tenantDomain)) &&
                            isValidApplication(appOwner, appName)) {
                        valid = true;
                    } else {
                        throw new APIManagementException(
                                "Couldn't Save Subscription Block Condition Due to Invalid Application " + "name "
                                        + appName + " from Application " + "Owner " + appOwner);
                    }
                } else {
                    throw new APIManagementException(
                            "Invalid subscription block condition with insufficient data : " + conditionValue);
                }
            }
            if (valid) {
                connection = APIMgtDBUtil.getConnection();
                connection.setAutoCommit(false);
                if (!isBlockConditionExist(conditionType, conditionValue, tenantDomain, connection)) {
                    String dbProductName = connection.getMetaData().getDatabaseProductName();
                    insertPreparedStatement = connection.prepareStatement(query,
                            new String[]{DBUtils.getConvertedAutoGeneratedColumnName(dbProductName, "CONDITION_ID")});
                    insertPreparedStatement.setString(1, conditionType);
                    insertPreparedStatement.setString(2, conditionValue);
                    insertPreparedStatement.setString(3, conditionStatus);
                    insertPreparedStatement.setString(4, tenantDomain);
                    insertPreparedStatement.setString(5, uuid);
                    insertPreparedStatement.execute();
                    ResultSet generatedKeys = insertPreparedStatement.getGeneratedKeys();
                    if (generatedKeys != null && generatedKeys.next()){
                        blockConditionsDTO.setConditionId(generatedKeys.getInt(1));
                    }
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
            return blockConditionsDTO;
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
     * @return Block condition represented by the UUID
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
     * @param state       blocking state
     * @return true if the operation was success
     * @throws APIManagementException
     */
    public boolean updateBlockConditionState(int conditionId, String state) throws APIManagementException {
        Connection connection = null;
        PreparedStatement updateBlockConditionPreparedStatement = null;
        boolean status = false;
        try {
            String query = SQLConstants.ThrottleSQLConstants.UPDATE_BLOCK_CONDITION_STATE_SQL;
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            updateBlockConditionPreparedStatement = connection.prepareStatement(query);
            updateBlockConditionPreparedStatement.setString(1, state.toUpperCase());
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
     * @param uuid  UUID of the block condition
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
            validateContextPreparedStatement.setString(1, appName);
            validateContextPreparedStatement.setString(2, appOwner);
            resultSet = validateContextPreparedStatement.executeQuery();
            connection.commit();
            if (resultSet.next()) {
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

    public String getAPILevelTier(int id) throws APIManagementException {
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

    public boolean hasSubscription(String tierId, String tenantDomainWithAt, String policyLevel) throws APIManagementException {
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

            if (PolicyConstants.POLICY_LEVEL_API.equals(policyLevel)) {
                checkIsExistPreparedStatement.setString(2, tierId);
            }
            checkIsResultSet = checkIsExistPreparedStatement.executeQuery();
            if (checkIsResultSet != null && checkIsResultSet.next()) {
                int count = checkIsResultSet.getInt(1);
                if (count > 0) {
                    status = true;
                }

            }

            connection.setAutoCommit(true);
        } catch (SQLException e) {
            String msg = "Couldn't check Subscription Exist";
            log.error(msg, e);
            handleException(msg, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(checkIsExistPreparedStatement, connection, checkIsResultSet);
        }
        return status;

    }


    /**
     * Get a list of access tokens issued for given user under the given app of given owner. Returned object carries
     * consumer key and secret information related to the access token
     *
     * @param userName end user name
     * @param appName  application name
     * @param appOwner application owner user name
     * @return list of tokens
     * @throws SQLException in case of a DB issue
     */
    public static List<AccessTokenInfo> getAccessTokenListForUser(String userName, String appName, String appOwner)
            throws SQLException {
        List<AccessTokenInfo> accessTokens = new ArrayList<AccessTokenInfo>(5);
        Connection connection = APIMgtDBUtil.getConnection();
        PreparedStatement consumerSecretIDPS = connection.prepareStatement(SQLConstants.GET_ACCESS_TOKENS_BY_USER_SQL);
        consumerSecretIDPS.setString(1, userName);
        consumerSecretIDPS.setString(2, appName);
        consumerSecretIDPS.setString(3, appOwner);

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

    public String[] getAPIDetailsByContext(String context) {
        String apiName = "";
        String apiProvider = "";
        String sql = SQLConstants.GET_API_FOR_CONTEXT_TEMPLATE_SQL;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(true);
            ps = conn.prepareStatement(sql);
            ps.setString(1, context);

            rs = ps.executeQuery();
            if (rs.next()) {
                apiName = rs.getString("API_NAME");
                apiProvider = rs.getString("API_PROVIDER");
            }
        } catch (SQLException e) {
            log.error("Error occurred while fetching data: " + e.getMessage(), e);
        } finally {
            try {
                conn.setAutoCommit(false);
            } catch (SQLException e) {
                log.error("Error occurred while fetching data: " + e.getMessage(), e);
            }
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
        return new String[]{apiName, apiProvider};
    }


    /**
     * Returns a Prepared statement after setting all the dynamic parameters. Dynamic parameters will be added in
     * the place of $params in query string
     *
     * @param conn               connection which will be used to create a prepared statement
     * @param query              dynamic query string which will be modified.
     * @param params             list of parameters
     * @param startingParamIndex index from which the parameter numbering will start.
     * @return
     * @throws SQLException
     */
    public PreparedStatement fillQueryParams(Connection conn, String query, String params[], int startingParamIndex)
            throws SQLException {

        String paramString = "";

        for (int i = 1; i <= params.length; i++) {
            if (i == params.length) {
                paramString = paramString + "?";
            } else {
                paramString = paramString + "?,";
            }
        }

        query = query.replace("$params", paramString);

        if (log.isDebugEnabled()) {
            log.info("Prepared statement query :" + query);
        }

        PreparedStatement preparedStatement = conn.prepareStatement(query);
        for (int i = 0; i < params.length; i++) {
            preparedStatement.setString(startingParamIndex, params[i]);
            startingParamIndex++;
        }
        return preparedStatement;
    }

    /**
     * Returns True if AM_APPLICATION_GROUP_MAPPING table exist in AM DB
     *
     * @return
     */
    public boolean isGrpIdMappingTableExist() {

        String sql = "SELECT * FROM AM_APPLICATION_GROUP_MAPPING";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(sql);

            rs = ps.executeQuery();

        } catch (SQLException e) {
            log.info("AM_APPLICATION_GROUP_MAPPING :- " + e.getMessage(), e);
            return false;
        } finally {

            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
        return true;
    }

    /**
     * Adds a new record in AM_APPLICATION_GROUP_MAPPING for each group
     *
     * @param conn
     * @param applicationId
     * @param groupIdString group id values separated by commas
     * @return
     * @throws APIManagementException
     */
    private boolean updateGroupIDMappings(Connection conn, int applicationId, String groupIdString, String tenant)
            throws APIManagementException {

        boolean updateSuccessful = false;

        PreparedStatement removeMigratedGroupIdsStatement = null;
        PreparedStatement deleteStatement = null;
        PreparedStatement insertStatement = null;
        String deleteQuery = SQLConstants.REMOVE_GROUP_ID_MAPPING_SQL;
        String insertQuery = SQLConstants.ADD_GROUP_ID_MAPPING_SQL;

        try {
            // Remove migrated Group ID information so that it can be replaced by updated Group ID's that are now
            // being saved. This is done to ensure that there is no conflicting migrated Group ID data remaining
            removeMigratedGroupIdsStatement = conn.prepareStatement(SQLConstants.REMOVE_MIGRATED_GROUP_ID_SQL);
            removeMigratedGroupIdsStatement.setInt(1, applicationId);
            removeMigratedGroupIdsStatement.executeUpdate();

            deleteStatement = conn.prepareStatement(deleteQuery);
            deleteStatement.setInt(1, applicationId);
            deleteStatement.executeUpdate();

            if (!StringUtils.isEmpty(groupIdString)) {

                String[] groupIdArray = groupIdString.split(",");

                insertStatement = conn.prepareStatement(insertQuery);
                for (String group : groupIdArray) {
                    insertStatement.setInt(1, applicationId);
                    insertStatement.setString(2, group);
                    insertStatement.setString(3, tenant);
                    insertStatement.addBatch();
                }
                insertStatement.executeBatch();
            }
            updateSuccessful = true;
        } catch (SQLException e) {
            updateSuccessful = false;
            handleException("Failed to update GroupId mappings ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(removeMigratedGroupIdsStatement, null, null);
            APIMgtDBUtil.closeAllConnections(deleteStatement, null, null);
            APIMgtDBUtil.closeAllConnections(insertStatement, null, null);
        }
        return updateSuccessful;
    }

    /**
     * Fetches all the groups for a given application and creates a single string separated by comma
     *
     * @param applicationId
     * @return comma separated group Id String
     * @throws APIManagementException
     */
    public String getGroupId(int applicationId) throws APIManagementException {

        String grpId = "";
        ArrayList<String> grpIdList = new ArrayList<String>();
        PreparedStatement preparedStatement = null;
        Connection conn = null;
        ResultSet resultSet = null;
        String sqlQuery = SQLConstants.GET_GROUP_ID_SQL;

        try {
            conn = APIMgtDBUtil.getConnection();
            preparedStatement = conn.prepareStatement(sqlQuery);
            preparedStatement.setInt(1, applicationId);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                grpIdList.add(resultSet.getString("GROUP_ID"));
            }

            for (int i = 0; i < grpIdList.size(); i++) {
                if (i == grpIdList.size() - 1) {
                    grpId = grpId + grpIdList.get(i);
                } else {
                    grpId = grpId + grpIdList.get(i) + ",";
                }
            }

        } catch (SQLException e) {
            handleException("Failed to Retrieve GroupId for application " + applicationId, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(preparedStatement, conn, resultSet);
        }
        return grpId;
    }

    /**
     * Get access token information associated with the given consumer key.
     *
     * @param consumerKey The consumer key.
     * @return APIKey The access token information.
     * @throws SQLException
     * @throws CryptoException
     */
    public APIKey getAccessTokenInfoByConsumerKey(String consumerKey) throws SQLException, CryptoException,
            APIManagementException {

        String accessTokenStoreTable = APIConstants.ACCESS_TOKEN_STORE_TABLE;
        String username = getUserIdFromConsumerKey(consumerKey);
        accessTokenStoreTable = getAccessTokenStoreTableNameOfUserId(username, accessTokenStoreTable);

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        String statement = SQLConstants.GET_ACCESS_TOKEN_INFO_BY_CONSUMER_KEY_PREFIX +
                accessTokenStoreTable + SQLConstants.GET_ACCESS_TOKEN_INFO_BY_CONSUMER_KEY_SUFFIX;

        String oracleSQL = SQLConstants.GET_ACCESS_TOKEN_INFO_BY_CONSUMER_KEY_ORACLE_PREFIX +
                accessTokenStoreTable + SQLConstants.GET_ACCESS_TOKEN_INFO_BY_CONSUMER_KEY_ORACLE_SUFFIX;

        String mySQL = "SELECT" + statement;
        String db2SQL = "SELECT" + statement;
        String msSQL = "SELECT " + statement;
        String postgreSQL = "SELECT * FROM (SELECT" + statement + ") AS TOKEN";

        String accessToken;
        String sql;

        try {
            connection = APIMgtDBUtil.getConnection();

            if (connection.getMetaData().getDriverName().contains("MySQL") || connection.getMetaData().getDriverName
                    ().contains("H2")) {
                sql = mySQL;
            } else if (connection.getMetaData().getDatabaseProductName().contains("DB2")) {
                sql = db2SQL;
            } else if (connection.getMetaData().getDriverName().contains("MS SQL") || connection.getMetaData()
                    .getDriverName().contains("Microsoft")) {
                sql = msSQL;
            } else if (connection.getMetaData().getDriverName().contains("PostgreSQL")) {
                sql = postgreSQL;
            } else {
                sql = oracleSQL;
            }

            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, consumerKey);
            preparedStatement.setString(2, APIConstants.ACCESS_TOKEN_USER_TYPE_APPLICATION);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                APIKey apiKey = new APIKey();
                accessToken = APIUtil.decryptToken(resultSet.getString("ACCESS_TOKEN"));
                apiKey.setConsumerKey(consumerKey);
                String consumerSecret = resultSet.getString("CONSUMER_SECRET");
                apiKey.setConsumerSecret(APIUtil.decryptToken(consumerSecret));
                apiKey.setAccessToken(accessToken);
                apiKey.setValidityPeriod(resultSet.getLong("VALIDITY_PERIOD") / 1000);
                apiKey.setGrantTypes(resultSet.getString("GRANT_TYPES"));
                apiKey.setCallbackUrl(resultSet.getString("CALLBACK_URL"));

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

    /**
     * Returns the user id for the consumer key.
     *
     * @param consumerKey The consumer key.
     * @return String The user id.
     */
    private String getUserIdFromConsumerKey(String consumerKey) throws APIManagementException {
        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        String userId = null;

        String sqlQuery = SQLConstants.GET_USER_ID_FROM_CONSUMER_KEY_SQL;

        try {
            connection = APIMgtDBUtil.getConnection();
            prepStmt = connection.prepareStatement(sqlQuery);
            prepStmt.setString(1, consumerKey);
            rs = prepStmt.executeQuery();

            while (rs.next()) {
                userId = rs.getString("USER_ID");
            }

        } catch (SQLException e) {
            handleException("Error when getting the user id for Consumer Key" + consumerKey, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
        }
        return userId;
    }

    /**
     * Get Subscribed APIs for an App.
     *
     * @param applicationID id of the application name
     * @return APISubscriptionInfoDTO[]
     * @throws APIManagementException if failed to get Subscribed APIs
     */
    public APISubscriptionInfoDTO[] getSubscribedAPIsForAnApp(String userId, int applicationID) throws
            APIManagementException {
        List<APISubscriptionInfoDTO> apiSubscriptionInfoDTOS = new ArrayList<APISubscriptionInfoDTO>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        //identify logged in user
        String loginUserName = getLoginUserName(userId);
        int tenantId = APIUtil.getTenantId(loginUserName);

        String sqlQuery = SQLConstants.GET_SUBSCRIBED_APIS_OF_USER_BY_APP_SQL;
        if (forceCaseInsensitiveComparisons) {
            sqlQuery = SQLConstants.GET_SUBSCRIBED_APIS_OF_USER_BY_APP_CASE_INSENSITIVE_SQL;
        }

        try {
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            ps.setInt(1, tenantId);
            ps.setInt(2, applicationID);
            rs = ps.executeQuery();
            while (rs.next()) {
                APISubscriptionInfoDTO infoDTO = new APISubscriptionInfoDTO();
                infoDTO.setProviderId(APIUtil.replaceEmailDomain(rs.getString("API_PROVIDER")));
                infoDTO.setApiName(rs.getString("API_NAME"));
                infoDTO.setContext(rs.getString("API_CONTEXT"));
                infoDTO.setVersion(rs.getString("API_VERSION"));
                infoDTO.setSubscriptionTier(rs.getString("SP_TIER_ID"));
                apiSubscriptionInfoDTOS.add(infoDTO);
            }
        } catch (SQLException e) {
            handleException("Error while executing SQL", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
        return apiSubscriptionInfoDTOS.toArray(new APISubscriptionInfoDTO[apiSubscriptionInfoDTOS.size()]);
    }


    public Application getApplicationByClientId(String clientId) throws APIManagementException {
        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;

        Application application = null;
        try {
            connection = APIMgtDBUtil.getConnection();
            String query = SQLConstants.GET_APPLICATION_BY_CLIENT_ID_SQL;

            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, clientId);

            rs = prepStmt.executeQuery();
            if (rs.next()) {
                String applicationId = rs.getString("APPLICATION_ID");
                String applicationName = rs.getString("NAME");
                String applicationOwner = rs.getString("CREATED_BY");

                application = new Application(applicationId);
                application.setName(applicationName);
                application.setOwner(applicationOwner);
                application.setDescription(rs.getString("DESCRIPTION"));
                application.setStatus(rs.getString("APPLICATION_STATUS"));
                application.setCallbackUrl(rs.getString("CALLBACK_URL"));
                application.setId(rs.getInt("APPLICATION_ID"));
                application.setGroupId(rs.getString("GROUP_ID"));
                application.setUUID(rs.getString("UUID"));
                application.setTier(rs.getString("APPLICATION_TIER"));
                application.setTokenType(rs.getString("TOKEN_TYPE"));
                application.setKeyType(rs.getString("KEY_TYPE"));

                if (multiGroupAppSharingEnabled) {
                    if (application.getGroupId() == null || application.getGroupId().isEmpty()) {
                        application.setGroupId(getGroupId(application.getId()));
                    }
                }
            }
        } catch (SQLException e) {
            handleException("Error while obtaining details of the Application foe client id " + clientId, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
        }
        return application;
    }

    /**
     * Returns the Label List for the TenantId.
     *
     * @param tenantDomain The tenant domain.
     * @return List of labels.
     */
    public List<Label> getAllLabels(String tenantDomain) throws APIManagementException {
        List<Label> labelList = new ArrayList<>();

        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQLConstants.GET_LABEL_BY_TENANT)) {
            try {
                connection.setAutoCommit(false);
                statement.setString(1, tenantDomain);
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        String labelId = rs.getString("LABEL_ID");
                        String labelName = rs.getString("NAME");
                        String description = rs.getString("DESCRIPTION");

                        Label label = new Label();
                        label.setLabelId(labelId);
                        label.setName(labelName);
                        label.setDescription(description);
                        label.setAccessUrls(getAccessUrlList(connection, labelId));
                        labelList.add(label);
                    }
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                handleException("Failed to get Labels of " + tenantDomain, e);
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            handleException("Failed to get Labels of " + tenantDomain, e);
        }
        return labelList;
    }

    /**
     * Returns the URL list for label id.
     *
     * @param labelId label id.
     * @return List of string.
     */
    private List<String> getAccessUrlList(Connection connection, String labelId) throws APIManagementException {
        List<String> hostList = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(SQLConstants.GET_URL_BY_LABEL_ID)) {
            statement.setString(1, labelId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    String host = rs.getString("ACCESS_URL");
                    hostList.add(host);
                }
            }
        } catch (SQLException e) {
            handleException("Failed to get label list: " , e);
        }
        return hostList;
    }

    /**
     * Returns the Label.
     *
     * @param tenantDomain The tenant domain.
     * @param label        label object.
     * @return label.
     */
    public Label addLabel(String tenantDomain, Label label) throws APIManagementException {
        String uuid = UUID.randomUUID().toString();
        label.setLabelId(uuid);
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQLConstants.ADD_LABEL_SQL)) {
            try {
                initialAutoCommit = connection.getAutoCommit();
                connection.setAutoCommit(false);

                statement.setString(1, uuid);
                statement.setString(2, label.getName());
                statement.setString(3, label.getDescription());
                statement.setString(4, tenantDomain);
                statement.executeUpdate();
                if (!label.getAccessUrls().isEmpty()) {
                    insertAccessUrlMappings(connection, uuid, label.getAccessUrls());
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                handleException("Failed to add label: " + uuid, e);
            } finally {
                APIMgtDBUtil.setAutoCommit(connection, initialAutoCommit);
            }
        } catch (SQLException e) {
            handleException("Failed to add label: " + uuid, e);
        }
        return label;
    }

    /**
     * Insert URL to the URL table
     *
     * @param uuid    label id.
     * @param urlList The list of url.
     * @throws APIManagementException
     */
    private void insertAccessUrlMappings(Connection connection, String uuid, List<String> urlList) throws
            APIManagementException {
        try (PreparedStatement statement = connection.prepareStatement(SQLConstants.ADD_LABEL_URL_MAPPING_SQL)) {
            for (String accessUrl : urlList) {
                statement.setString(1, uuid);
                statement.setString(2, accessUrl);
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException e) {
            handleException("Failed to add label url : " + uuid, e);
        }
    }

    /**
     * Delete  label.
     *
     * @param labelUUID label id.
     * @throws APIManagementException
     */
    public void deleteLabel(String labelUUID) throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQLConstants.DELETE_LABEL_SQL)) {
            try {
                initialAutoCommit = connection.getAutoCommit();
                connection.setAutoCommit(false);
                statement.setString(1, labelUUID);
                statement.executeUpdate();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                handleException("Failed to delete label : " + labelUUID, e);
            } finally {
                APIMgtDBUtil.setAutoCommit(connection, initialAutoCommit);
            }
        } catch (SQLException e) {
            handleException("Failed to delete label : " + labelUUID, e);
        }
    }

    /**
     * Delete label URL
     *
     * @param labelUUID label id.
     * @throws APIManagementException
     */
    private void deleteAccessUrlMappings(Connection connection, String labelUUID) throws APIManagementException {
        try (PreparedStatement statement = connection.prepareStatement(SQLConstants.DELETE_LABEL_URL_MAPPING_SQL)) {
            statement.setString(1, labelUUID);
            statement.executeUpdate();
        } catch (SQLException e) {
            handleException("Failed to delete label url : ", e);
        }
    }

    /**
     * Update the label.
     *
     * @param label label object.
     * @return labels.
     */
    public Label updateLabel(Label label) throws APIManagementException {
        List<String> accessURLs = label.getAccessUrls();
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQLConstants.UPDATE_LABEL_SQL)) {
            try {
                initialAutoCommit = connection.getAutoCommit();
                connection.setAutoCommit(false);
                statement.setString(1, label.getName());
                statement.setString(2, label.getDescription());
                statement.setString(3, label.getLabelId());

                deleteAccessUrlMappings(connection, label.getLabelId());
                insertAccessUrlMappings(connection, label.getLabelId(), accessURLs);
                statement.executeUpdate();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                handleException("Failed to update label : ", e);
            } finally {
                APIMgtDBUtil.setAutoCommit(connection, initialAutoCommit);
            }
        } catch (SQLException e) {
            handleException("Failed to update label : ", e);
        }
        return label;
    }

    private void addApplicationAttributes(Connection conn, Map<String, String> attributes, int applicationId, int tenantId)
            throws APIManagementException {

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            if (attributes != null) {
                ps = conn.prepareStatement(SQLConstants.ADD_APPLICATION_ATTRIBUTES_SQL);
                for (Map.Entry<String, String> attribute : attributes.entrySet()) {
                    if (StringUtils.isNotEmpty(attribute.getKey()) && StringUtils.isNotEmpty(attribute.getValue())) {
                        ps.setInt(1, applicationId);
                        ps.setString(2, attribute.getKey());
                        ps.setString(3, attribute.getValue());
                        ps.setInt(4, tenantId);
                        ps.addBatch();
                    }
                }
                int[] update = ps.executeBatch();
            }
        } catch (SQLException e) {
            handleException("Error in adding attributes of application with id: " + applicationId , e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, null, rs);
        }
    }

    /**
     * Get all attributes stored against an Application
     *
     * @param conn Database connection
     * @param applicationId
     * @throws APIManagementException
     */
    public Map<String, String> getApplicationAttributes(Connection conn, int applicationId) throws APIManagementException {

        PreparedStatement ps = null;
        ResultSet rs = null;
        Map<String, String> applicationAttributes = new HashMap<>();
        try {
            ps = conn.prepareStatement(SQLConstants.GET_APPLICATION_ATTRIBUTES_BY_APPLICATION_ID);
            ps.setInt(1, applicationId);
            rs = ps.executeQuery();
            while (rs.next()) {
                applicationAttributes.put(rs.getString("NAME"),
                        rs.getString("VALUE"));
            }

        } catch (SQLException e) {
            handleException("Error when reading attributes of application with id: " + applicationId, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, null, rs);
        }
        return applicationAttributes;
    }

    /**
     * Delete certain attribute stored against an Application
     *
     * @param attributeKey User defined key of attribute
     * @param applicationId
     * @throws APIManagementException
     */
    public void deleteApplicationAttributes(String attributeKey, int applicationId) throws APIManagementException {

        Connection connection = null;
        PreparedStatement ps = null;

        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            ps = connection.prepareStatement(SQLConstants.REMOVE_APPLICATION_ATTRIBUTES_BY_ATTRIBUTE_NAME_SQL);
            ps.setString(1, attributeKey);
            ps.setInt(2, applicationId);
            ps.execute();
            connection.commit();
        } catch (SQLException e) {
            handleException("Error in establishing SQL connection ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, connection, null);
        }
    }

    /**
     * Add new attributes against an Application in API Store
     *
     * @param applicationAttributes Map of key, value pair of attributes
     * @param applicationId Id of Application against which attributes are getting stored
     * @param tenantId Id of tenant
     * @throws APIManagementException
     */
    public void addApplicationAttributes(Map<String, String> applicationAttributes, int applicationId, int tenantId)
            throws APIManagementException {

        Connection connection = null;
        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            addApplicationAttributes(connection, applicationAttributes, applicationId, tenantId);
            connection.commit();
        } catch (SQLException sqlException) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException e) {
                    log.error("Failed to rollback add application attributes ", e);
                }
            }
            handleException("Failed to add Application", sqlException);
        } finally {
            APIMgtDBUtil.closeAllConnections(null, connection, null);
        }
    }

    /**
     * Converts all null values for THROTTLING_TIER in AM_API_URL_MAPPING table, to Unlimited.
     * This will be executed only during startup of the server.
     *
     * @throws APIManagementException
     */
    public void convertNullThrottlingTiers() throws APIManagementException {
        Connection connection = null;
        PreparedStatement prepStmt = null;

        String query = SQLConstants.FIX_NULL_THROTTLING_TIERS;
        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            prepStmt = connection.prepareStatement(query);
            prepStmt.execute();
            connection.commit();
        } catch (SQLException e) {
            handleException(
                    "Error occurred while converting NULL throttling tiers to Unlimited in AM_API_URL_MAPPING table",
                    e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, null);
        }
    }

    /**
     * Retrieves the Application which is corresponding to the given UUID String
     *
     * @param subscriberId subscriberId of the Application
     * @param applicationName name of the Application
     * @return
     * @throws APIManagementException
     */
    public Application getApplicationBySubscriberIdAndName(int subscriberId, String applicationName) throws
            APIManagementException {
        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        int applicationId = 0;
        Application application = null;
        try {
            connection = APIMgtDBUtil.getConnection();
            String query = SQLConstants.GET_APPLICATION_BY_SUBSCRIBERID_AND_NAME_SQL;
            prepStmt = connection.prepareStatement(query);
            prepStmt.setInt(1, subscriberId);
            prepStmt.setString(2, applicationName);
            rs = prepStmt.executeQuery();
            if (rs.next()) {
                String subscriberName = rs.getString("USER_ID");
                Subscriber subscriber = new Subscriber(subscriberName);
                subscriber.setId(subscriberId);
                application = new Application(applicationName, subscriber);
                application.setDescription(rs.getString("DESCRIPTION"));
                application.setStatus(rs.getString("APPLICATION_STATUS"));
                application.setCallbackUrl(rs.getString("CALLBACK_URL"));
                applicationId = rs.getInt("APPLICATION_ID");
                application.setId(applicationId);
                application.setGroupId(rs.getString("GROUP_ID"));
                application.setUUID(rs.getString("UUID"));
                application.setTier(rs.getString("APPLICATION_TIER"));
                application.setTokenType(rs.getString("TOKEN_TYPE"));
                subscriber.setId(rs.getInt("SUBSCRIBER_ID"));
                if (multiGroupAppSharingEnabled) {
                    if (StringUtils.isEmpty(application.getGroupId())) {
                        application.setGroupId(getGroupId(application.getId()));
                    }
                }
                Timestamp createdTime = rs.getTimestamp("CREATED_TIME");
                application.setCreatedTime(createdTime == null ? null : String.valueOf(createdTime.getTime()));
                try {
                    Timestamp updated_time = rs.getTimestamp("UPDATED_TIME");
                    application.setLastUpdatedTime(
                            updated_time == null ? null : String.valueOf(updated_time.getTime()));
                } catch (SQLException e) {
                    application.setLastUpdatedTime(application.getCreatedTime());
                }
            }
            if (application != null) {
                Map<String, String> applicationAttributes = getApplicationAttributes(connection, applicationId);
                application.setApplicationAttributes(applicationAttributes);
            }
        } catch (SQLException e) {
            handleException("Error while obtaining details of the Application : " + applicationName + " of " +
                    subscriberId, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
        }
        return application;
    }

    /**
     * Retrieve URI Templates for the given API
     * @param api API
     * @return Map of URITemplate with key as Method:resourcepath
     * @throws APIManagementException exception
     */
    public Map<String, URITemplate> getURITemplatesForAPI(API api) throws APIManagementException {
        Map<String, URITemplate> templatesMap = new HashMap<String, URITemplate>();
        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        try {
            connection = APIMgtDBUtil.getConnection();
            String query = SQLConstants.GET_URL_TEMPLATES_FOR_API;

            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, api.getId().getApiName());
            prepStmt.setString(2, api.getId().getVersion());
            prepStmt.setString(3, APIUtil.replaceEmailDomainBack(api.getId().getProviderName()));
            rs = prepStmt.executeQuery();
            while (rs.next()) {
                URITemplate template = new URITemplate();
                String urlPattern = rs.getString("URL_PATTERN");
                String httpMethod = rs.getString("HTTP_METHOD");

                template.setHTTPVerb(httpMethod);
                template.setResourceURI(urlPattern);
                template.setId(rs.getInt("URL_MAPPING_ID"));

                //TODO populate others if needed

                templatesMap.put(httpMethod + ":" + urlPattern, template);
            }

        } catch (SQLException e) {
            handleException("Error while obtaining details of the URI Template for api " + api.getId() , e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
        }

        return templatesMap;
    }

    public List<ResourcePath> getResourcePathsOfAPI(APIIdentifier apiId) throws APIManagementException{
        List<ResourcePath> resourcePathList = new ArrayList<ResourcePath>();

        try (Connection conn = APIMgtDBUtil.getConnection()) {
            String sql = SQLConstants.GET_URL_TEMPLATES_FOR_API;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, apiId.getApiName());
                ps.setString(2, apiId.getVersion());
                ps.setString(3, APIUtil.replaceEmailDomainBack(apiId.getProviderName()));

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ResourcePath resourcePath = new ResourcePath();
                        resourcePath.setId(rs.getInt("URL_MAPPING_ID"));
                        //Set the URL pattern as the resource path
                        resourcePath.setResourcePath(rs.getString("URL_PATTERN"));
                        //Set the HTTP method as the HTTPVerb
                        resourcePath.setHttpVerb(rs.getString("HTTP_METHOD"));
                        resourcePathList.add(resourcePath);
                    }
                }
            }
        } catch (SQLException e) {
            handleException("Error while obtaining Resource Paths of api " + apiId , e);
        }
        return resourcePathList;
    }

    public void addAPIProduct(APIProduct apiProduct, String tenantDomain) throws APIManagementException {
        Connection connection = null;
        PreparedStatement prepStmtAddAPIProduct = null;
        PreparedStatement prepStmtAddScopeEntry = null;

        if(log.isDebugEnabled()) {
            log.debug("addAPIProduct() : " + apiProduct.toString() + " for tenant " + tenantDomain);
        }
        APIProductIdentifier identifier = apiProduct.getId();
        ResultSet rs = null;
        int productId = 0;
        int scopeId = 0;
        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            String queryAddAPIProduct = SQLConstants.ADD_API_PRODUCT;
            prepStmtAddAPIProduct = connection.prepareStatement(queryAddAPIProduct, new String[]{"api_id"});
            prepStmtAddAPIProduct.setString(1, APIUtil.replaceEmailDomainBack(identifier.getProviderName()));
            prepStmtAddAPIProduct.setString(2, identifier.getName());
            prepStmtAddAPIProduct.setString(3, identifier.getVersion());
            prepStmtAddAPIProduct.setString(4, apiProduct.getContext());
            prepStmtAddAPIProduct.setString(5, apiProduct.getProductLevelPolicy());
            prepStmtAddAPIProduct.setString(6, APIUtil.replaceEmailDomainBack(identifier.getProviderName()));
            prepStmtAddAPIProduct.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
            prepStmtAddAPIProduct.setString(8, APIConstants.API_PRODUCT);

            prepStmtAddAPIProduct.execute();

            rs = prepStmtAddAPIProduct.getGeneratedKeys();

            if (rs.next()) {
                productId = rs.getInt(1);
            }
            //breaks the flow if product is not added to the db correctly
            if(productId == 0) {
                throw new APIManagementException("Error while adding API product " + apiProduct.getUuid());
            }

            addAPIProductResourceMappings(apiProduct.getProductResources(), connection);
            connection.commit();
        } catch (SQLException e) {
            handleException("Error while adding API product " + identifier.getName() + " of provider "
                    + APIUtil.replaceEmailDomainBack(identifier.getProviderName()), e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmtAddAPIProduct, null, null);
            APIMgtDBUtil.closeAllConnections(prepStmtAddScopeEntry, connection, null);
        }
    }

    /**
     * Add api product url mappings to DB
     *    - url templeates to product mappings (resource bundling) - AM_API_PRODUCT_MAPPING
     * @param productResources
     * @throws APIManagementException
     */
    public void addAPIProductResourceMappings(List<APIProductResource> productResources, Connection connection)
            throws APIManagementException {
        //add product-api resource mappings
        PreparedStatement prepStmtAddResourceMapping = null;

        String addProductResourceMappingSql = SQLConstants.ADD_PRODUCT_RESOURCE_MAPPING_SQL;

        boolean isNewConnection = false;
        try {
            if (connection == null) {
                connection = APIMgtDBUtil.getConnection();
                isNewConnection = true;
            }

            prepStmtAddResourceMapping = connection.prepareStatement(addProductResourceMappingSql);

            //add the resources in each API in the API product.
            for (APIProductResource apiProductResource : productResources) {
                APIProductIdentifier productIdentifier = apiProductResource.getProductIdentifier();
                int productId = getAPIID(productIdentifier, connection);
                URITemplate uriTemplate = apiProductResource.getUriTemplate();
                prepStmtAddResourceMapping.setInt(1, productId);
                prepStmtAddResourceMapping.setInt(2, uriTemplate.getId());
                prepStmtAddResourceMapping.addBatch();
            }

            prepStmtAddResourceMapping.executeBatch();
            prepStmtAddResourceMapping.clearBatch();
        } catch (SQLException e) {
            handleException("Error while adding API product Resources" , e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmtAddResourceMapping, null, null);
            if (isNewConnection) {
                APIMgtDBUtil.closeAllConnections(null, connection, null);
            }
        }
    }

    /**
     * Update Product scope and resource mappings
     * @param apiProduct
     * @param productId
     * @throws APIManagementException
     */
    public void updateAPIProductResourceMappings(APIProduct apiProduct, int productId, Connection connection) throws APIManagementException {

        PreparedStatement prepStmtRemoveResourceToProductMappings = null;

        String removeResourceToProductMappingsSql = SQLConstants.DELETE_FROM_AM_API_PRODUCT_MAPPING_SQL;

        try {
            prepStmtRemoveResourceToProductMappings = connection.prepareStatement(removeResourceToProductMappingsSql);
            prepStmtRemoveResourceToProductMappings.setInt(1, productId);
            prepStmtRemoveResourceToProductMappings.execute();

            addAPIProductResourceMappings(apiProduct.getProductResources(), connection);
        } catch (SQLException e) {
            handleException("Error while updating API-Product Resources.", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmtRemoveResourceToProductMappings, null, null);
        }
    }

    /**
     * Delete API product and its related scopes
     * @param productIdentifier product ID
     * @throws APIManagementException
     */
    public void deleteAPIProduct(APIProductIdentifier productIdentifier) throws APIManagementException {
        String deleteQuery = SQLConstants.DELETE_API_PRODUCT_SQL;
        String deleteRatingsQuery = SQLConstants.REMOVE_FROM_API_RATING_SQL;
        PreparedStatement ps = null;
        Connection connection = null;
        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            //  delete product ratings
            int id = getAPIProductId(productIdentifier);
            ps = connection.prepareStatement(deleteRatingsQuery);
            ps.setInt(1, id);
            ps.execute();
            ps.close();//If exception occurs at execute, this statement will close in finally else here
            //delete product
            ps = connection.prepareStatement(deleteQuery);
            ps.setString(1, APIUtil.replaceEmailDomainBack(productIdentifier.getProviderName()));
            ps.setString(2, productIdentifier.getName());
            ps.setString(3, productIdentifier.getVersion());
            ps.executeUpdate();
            ps.close();

            connection.commit();
        } catch (SQLException e) {
            handleException("Error while deleting api product " + productIdentifier , e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, connection, null);
        }
    }

    public List<APIProductResource> getProductMappingsForAPI(API api) throws APIManagementException {
        List<APIProductResource> productMappings = new ArrayList<>();
        APIIdentifier apiIdentifier = api.getId();

        Set<URITemplate> uriTemplatesOfAPI = getURITemplatesOfAPI(apiIdentifier);

        for (URITemplate uriTemplate : uriTemplatesOfAPI) {
            Set<APIProductIdentifier> apiProductIdentifiers = uriTemplate.retrieveUsedByProducts();

            for (APIProductIdentifier apiProductIdentifier : apiProductIdentifiers) {
                APIProductResource productMapping = new APIProductResource();
                productMapping.setProductIdentifier(apiProductIdentifier);
                productMapping.setUriTemplate(uriTemplate);

                productMappings.add(productMapping);
            }
        }

        return productMappings;
    }

    public int getAPIProductId(APIProductIdentifier identifier) throws APIManagementException {
        Connection conn = null;
        String queryGetProductId = SQLConstants.GET_PRODUCT_ID;
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        int productId = -1;

        try {
            conn = APIMgtDBUtil.getConnection();
            preparedStatement = conn.prepareStatement(queryGetProductId);
            preparedStatement.setString(1, identifier.getName());
            preparedStatement.setString(2, APIUtil.replaceEmailDomainBack(identifier.getProviderName()));
            preparedStatement.setString(3, APIConstants.API_PRODUCT_VERSION); //versioning is not supported atm

            rs = preparedStatement.executeQuery();

            if (rs.next()) {
                productId = rs.getInt("API_ID");
            }

            if (productId == -1) {
                String msg = "Unable to find the API Product : " + productId + " in the database";
                log.error(msg);
                throw new APIManagementException(msg);
            }
        } catch (SQLException e) {
            handleException("Error while retrieving api product id for product " + identifier.getName() + " by " +
                    APIUtil.replaceEmailDomainBack(identifier.getProviderName()), e);
        } finally {
            APIMgtDBUtil.closeAllConnections(preparedStatement, conn, rs);
        }
        return productId;
    }

    public void updateAPIProduct(APIProduct product, String username) throws APIManagementException {
        Connection conn = null;
        PreparedStatement ps = null;
        if(log.isDebugEnabled()) {
            log.debug("updateAPIProduct() : product- " + product.toString());
        }
        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);

            String query = SQLConstants.UPDATE_PRODUCT_SQL;

            ps = conn.prepareStatement(query);

            ps.setString(1, product.getProductLevelPolicy());
            ps.setString(2, username);
            ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            APIProductIdentifier identifier = product.getId();
            ps.setString(4, identifier.getName());
            ps.setString(5, APIUtil.replaceEmailDomainBack(identifier.getProviderName()));
            ps.setString(6, identifier.getVersion());
            ps.executeUpdate();

            int productId = getAPIID(product.getId(), conn);
            updateAPIProductResourceMappings(product, productId, conn);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error("Error while rolling back the failed operation", e1);
                }
            }
            handleException("Error in updating API Product: " + e.getMessage(), e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, null);
        }
    }

    /**
     * get resource mapping of the api product
     * TODO://Get resource scopes from AM_API_RESOURCE_SCOPE table and retrieve scope meta data and bindings from KM.
     * @param productIdentifier api product identifier
     * @throws APIManagementException
     */
    public List<APIProductResource> getAPIProductResourceMappings(APIProductIdentifier productIdentifier)
            throws APIManagementException {
        int productId = getAPIProductId(productIdentifier);
        List<APIProductResource> productResourceList = new ArrayList<>();
        try (Connection connection = APIMgtDBUtil.getConnection()) {
            String sql = SQLConstants.GET_RESOURCES_OF_PRODUCT;
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, productId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        APIProductResource resource = new APIProductResource();
                        APIIdentifier apiId = new APIIdentifier(rs.getString("API_PROVIDER"), rs.getString("API_NAME"),
                                rs.getString("API_VERSION"));
                        resource.setProductIdentifier(productIdentifier);
                        resource.setApiIdentifier(apiId);
                        resource.setApiName(rs.getString("API_NAME"));
                        URITemplate uriTemplate = new URITemplate();
                        uriTemplate.setUriTemplate(rs.getString("URL_PATTERN"));
                        uriTemplate.setResourceURI(rs.getString("URL_PATTERN"));
                        uriTemplate.setHTTPVerb(rs.getString("HTTP_METHOD"));
                        int uriTemplateId = rs.getInt("URL_MAPPING_ID");
                        uriTemplate.setId(uriTemplateId);
                        uriTemplate.setAuthType(rs.getString("AUTH_SCHEME"));
                        uriTemplate.setThrottlingTier(rs.getString("THROTTLING_TIER"));

                        try (PreparedStatement scopesStatement = connection.
                                prepareStatement(SQLConstants.GET_SCOPE_KEYS_BY_URL_MAPPING_ID)) {
                            scopesStatement.setInt(1, uriTemplateId);
                            try (ResultSet scopesResult = scopesStatement.executeQuery()) {
                                while (scopesResult.next()) {
                                    Scope scope = new Scope();
                                    scope.setKey(scopesResult.getString("SCOPE_NAME"));
                                    uriTemplate.setScopes(scope);
                                }
                            }
                        }

                        resource.setUriTemplate(uriTemplate);
                        productResourceList.add(resource);
                    }
                }
            }
        } catch (SQLException e) {
            handleException("Failed to get product resources of api product : " + productIdentifier, e);
        }
        return productResourceList;
    }

    /**
     * Add new Audit API ID
     *
     * @param apiIdentifier APIIdentifier object to retrieve API ID
     * @param uuid Audit API ID
     * @throws APIManagementException
     */
    public void addAuditApiMapping(APIIdentifier apiIdentifier, String uuid) throws APIManagementException {
        Connection connection = null;
        String query = SQLConstants.ADD_SECURITY_AUDIT_MAP_SQL;
        int apiId = getAPIID(apiIdentifier, connection);
        try (Connection conn = APIMgtDBUtil.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, apiId);
                ps.setString(2, uuid);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            handleException("Error while adding new audit api id: ", e);
        }
    }

    /**
     * Get Audit API ID
     *
     * @param apiIdentifier APIIdentifier object to retrieve API ID
     * @throws APIManagementException
     */
    public String getAuditApiId(APIIdentifier apiIdentifier) throws APIManagementException {
        Connection connection = null;
        String query = SQLConstants.GET_AUDIT_UUID_SQL;
        int apiId = getAPIID(apiIdentifier, connection);
        String auditUuid = null;
        try (Connection conn = APIMgtDBUtil.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, apiId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        auditUuid = rs.getString("AUDIT_UUID");
                    }
                }
            }
        } catch (SQLException e) {
            handleException("Error while getting audit api id: ", e);
        }
        return auditUuid;
    }

    /**
     * Add custom complexity details for a particular API
     *
     * @param apiIdentifier         APIIdentifier object to retrieve API ID
     * @param graphqlComplexityInfo GraphqlComplexityInfo object
     * @throws APIManagementException
     */
    public void addComplexityDetails(APIIdentifier apiIdentifier, GraphqlComplexityInfo graphqlComplexityInfo)
            throws APIManagementException {
        String addCustomComplexityDetails = SQLConstants.ADD_CUSTOM_COMPLEXITY_DETAILS_SQL;
        try (Connection conn = APIMgtDBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(addCustomComplexityDetails)) {
            conn.setAutoCommit(false);
            int apiId = getAPIID(apiIdentifier, conn);
            for (CustomComplexityDetails customComplexity : graphqlComplexityInfo.getList()) {
                UUID uuid = UUID.randomUUID();
                String randomUUIDString = uuid.toString();
                ps.setString(1, randomUUIDString);
                ps.setInt(2, apiId);
                ps.setString(3, customComplexity.getType());
                ps.setString(4, customComplexity.getField());
                ps.setInt(5, customComplexity.getComplexityValue());
                ps.executeUpdate();
            }
            conn.commit();
        } catch (SQLException ex) {
            handleException("Error while adding custom complexity details: ", ex);
        }
    }

    /**
     * Update custom complexity details for a particular API
     *
     * @param apiIdentifier         APIIdentifier object to retrieve API ID
     * @param graphqlComplexityInfo GraphqlComplexityInfo object
     * @throws APIManagementException
     */
    public void updateComplexityDetails(APIIdentifier apiIdentifier, GraphqlComplexityInfo graphqlComplexityInfo)
            throws APIManagementException {
        String updateCustomComplexityDetails = SQLConstants.UPDATE_CUSTOM_COMPLEXITY_DETAILS_SQL;
        try (Connection conn = APIMgtDBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(updateCustomComplexityDetails)) {
            conn.setAutoCommit(false);
            int apiId = getAPIID(apiIdentifier, conn);
            // Entries already exists for this API_ID. Hence an update is performed.
            for (CustomComplexityDetails customComplexity : graphqlComplexityInfo.getList()) {
                ps.setInt(1, customComplexity.getComplexityValue());
                ps.setInt(2, apiId);
                ps.setString(3, customComplexity.getType());
                ps.setString(4, customComplexity.getField());
                ps.executeUpdate();
            }
            conn.commit();
        } catch (SQLException e) {
            handleException("Error while updating custom complexity details: ", e);
        }
    }
    /**
     * Add or Update complexity details
     *
     * @param apiIdentifier         APIIdentifier object to retrieve API ID
     * @param graphqlComplexityInfo GraphqlComplexityDetails object
     * @throws APIManagementException
     */
    public void addOrUpdateComplexityDetails(APIIdentifier apiIdentifier, GraphqlComplexityInfo graphqlComplexityInfo)
            throws APIManagementException {
        String getCustomComplexityDetailsQuery = SQLConstants.GET_CUSTOM_COMPLEXITY_DETAILS_SQL;
        try (Connection conn = APIMgtDBUtil.getConnection();
             PreparedStatement getCustomComplexityDetails = conn.prepareStatement(getCustomComplexityDetailsQuery)) {
            int apiId = getAPIID(apiIdentifier, conn);
            getCustomComplexityDetails.setInt(1, apiId);
            try (ResultSet rs1 = getCustomComplexityDetails.executeQuery()) {
                if (rs1.next()) {
                    updateComplexityDetails(apiIdentifier, graphqlComplexityInfo);
                } else {
                    addComplexityDetails(apiIdentifier, graphqlComplexityInfo);
                }
            }
        } catch (SQLException ex) {
            handleException("Error while updating custom complexity details: ", ex);
        }
    }

    /**
     * Get custom complexity details for a particular API
     *
     * @param apiIdentifier APIIdentifier object to retrieve API ID
     * @return info about the complexity details
     * @throws APIManagementException
     */
    public GraphqlComplexityInfo getComplexityDetails(APIIdentifier apiIdentifier) throws APIManagementException {
        GraphqlComplexityInfo graphqlComplexityInfo = new GraphqlComplexityInfo();
        String getCustomComplexityDetailsQuery = SQLConstants.GET_CUSTOM_COMPLEXITY_DETAILS_SQL;
        List<CustomComplexityDetails> customComplexityDetailsList = new ArrayList<CustomComplexityDetails>();
        try (Connection conn = APIMgtDBUtil.getConnection();
             PreparedStatement getCustomComplexityDetails = conn.prepareStatement(getCustomComplexityDetailsQuery)) {
            int apiId = getAPIID(apiIdentifier, conn);
            getCustomComplexityDetails.setInt(1, apiId);
            try (ResultSet rs1 = getCustomComplexityDetails.executeQuery()) {
                while (rs1.next()) {
                    CustomComplexityDetails customComplexityDetails = new CustomComplexityDetails();
                    customComplexityDetails.setType(rs1.getString("TYPE"));
                    customComplexityDetails.setField(rs1.getString("FIELD"));
                    customComplexityDetails.setComplexityValue(rs1.getInt("COMPLEXITY_VALUE"));
                    customComplexityDetailsList.add(customComplexityDetails);
                }
            }
            graphqlComplexityInfo.setList(customComplexityDetailsList);
        } catch (SQLException ex) {
            handleException("Error while retrieving custom complexity details: ", ex);
        }
        return graphqlComplexityInfo;
    }

    /**
     * Add a bot detection alert subscription
     *
     * @param email email to be registered for the subscription
     * @throws APIManagementException if an error occurs when adding a bot detection alert subscription
     */
    public void addBotDetectionAlertSubscription(String email) throws APIManagementException {

        String query = SQLConstants.BotDataConstants.ADD_NOTIFICATION;
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            UUID uuid = UUID.randomUUID();
            String randomUUIDString = uuid.toString();
            String category = "Bot-Detection";
            String notificationType = "email";
            ps.setString(1, randomUUIDString);
            ps.setString(2, category);
            ps.setString(3, notificationType);
            ps.setString(4, email);
            ps.execute();
            connection.commit();
        } catch (SQLException e) {
            handleException("Error while adding bot detection alert subscription", e);
        }
    }

    /**
     * Retrieve all bot detection alert subscriptions
     *
     * @throws APIManagementException if an error occurs when retrieving bot detection alert subscriptions
     */
    public List<BotDetectionData> getBotDetectionAlertSubscriptions() throws APIManagementException {

        List<BotDetectionData> list = new ArrayList<>();
        String query = SQLConstants.BotDataConstants.GET_SAVED_ALERT_EMAILS;
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {
            ResultSet resultSet = ps.executeQuery();
            while (resultSet.next()) {
                BotDetectionData botDetectedData = new BotDetectionData();
                botDetectedData.setUuid(resultSet.getString("UUID"));
                botDetectedData.setEmail(resultSet.getString("SUBSCRIBER_ADDRESS"));
                list.add(botDetectedData);
            }
        } catch (SQLException e) {
            handleException("Error while retrieving bot detection alert subscriptions", e);
        }
        return list;
    }

    /**
     * Delete a bot detection alert subscription
     *
     * @param uuid uuid of the subscription
     * @throws APIManagementException if an error occurs when deleting a bot detection alert subscription
     */
    public void deleteBotDetectionAlertSubscription(String uuid) throws APIManagementException {

        String query = SQLConstants.BotDataConstants.DELETE_EMAIL_BY_UUID;
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            ps.setString(1, uuid);
            ps.execute();
            connection.commit();
        } catch (SQLException e) {
            handleException("Error while deleting bot detection alert subscription", e);
        }
    }

    /**
     * Retrieve a bot detection alert subscription by querying a particular field (uuid or email)
     *
     * @param field field to be queried to obtain the bot detection alert subscription. Can be uuid or email
     * @param value value corresponding to the field (uuid or email value)
     * @return if subscription exist, returns the bot detection alert subscription, else returns a null object
     * @throws APIManagementException
     */
    public BotDetectionData getBotDetectionAlertSubscription(String field, String value)
            throws APIManagementException {

        BotDetectionData alertSubscription = null;
        String query = "";
        if (AlertMgtConstants.BOT_DETECTION_UUID_FIELD.equals(field)) {
            query = SQLConstants.BotDataConstants.GET_ALERT_SUBSCRIPTION_BY_UUID;
        }
        if (AlertMgtConstants.BOT_DETECTION_EMAIL_FIELD.equals(field)) {
            query = SQLConstants.BotDataConstants.GET_ALERT_SUBSCRIPTION_BY_EMAIL;
        }
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, value);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                alertSubscription = new BotDetectionData();
                alertSubscription.setUuid(resultSet.getString("UUID"));
                alertSubscription.setEmail(resultSet.getString("SUBSCRIBER_ADDRESS"));
            }
        } catch (SQLException e) {
            handleException("Failed to retrieve bot detection alert subscription of " + field + ": " + value, e);
        }
        return alertSubscription;
    }

    /**
     * Persist revoked jwt signatures to database.
     *
     * @param eventId
     * @param jwtSignature signature of jwt token.
     * @param expiryTime   expiry time of the token.
     * @param tenantId tenant id of the jwt subject.
     */
    public void addRevokedJWTSignature(String eventId, String jwtSignature, String type,
                                       Long expiryTime, int tenantId) throws APIManagementException {

        if (StringUtils.isEmpty(type)) {
            type = APIConstants.DEFAULT;
        }
        String addJwtSignature = SQLConstants.RevokedJWTConstants.ADD_JWT_SIGNATURE;
        try (Connection conn = APIMgtDBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(addJwtSignature)) {
                ps.setString(1, eventId);
                ps.setString(2, jwtSignature);
                ps.setLong(3, expiryTime);
                ps.setInt(4, tenantId);
                ps.setString(5, type);
                ps.execute();
                conn.commit();
            } catch (SQLIntegrityConstraintViolationException e) {
                boolean isRevokedTokenExist = isRevokedJWTSignatureExist(conn, eventId);

                if (isRevokedTokenExist) {
                    log.warn("Revoked Token already persisted");
                } else {
                    handleException("Failed to add Revoked Token Event" + APIUtil.getMaskedToken(jwtSignature), e);
                }
            }catch (SQLException e){
                conn.rollback();
            }
        } catch (SQLException e) {
            handleException("Error in adding revoked jwt signature to database : " + e.getMessage(), e);
        }
    }

    /**
     * Check revoked Token Identifier exist
     *
     * @param eventId

     */
    private boolean isRevokedJWTSignatureExist(Connection conn, String eventId) throws SQLException {

        String checkRevokedTokenExist = SQLConstants.RevokedJWTConstants.CHECK_REVOKED_TOKEN_EXIST;
        try (PreparedStatement ps = conn.prepareStatement(checkRevokedTokenExist)) {
            ps.setString(1, eventId);
            try (ResultSet resultSet = ps.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    /**
     * Removes expired JWTs from revoke table.
     * @throws APIManagementException
     */
    public void removeExpiredJWTs() throws APIManagementException {

        String deleteQuery = SQLConstants.RevokedJWTConstants.DELETE_REVOKED_JWT;
        try (Connection connection = APIMgtDBUtil.getConnection(); PreparedStatement ps =
                connection.prepareStatement(deleteQuery)) {
            connection.setAutoCommit(false);
            ps.setLong(1, System.currentTimeMillis() / 1000);
            ps.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            handleException("Error while deleting expired JWTs from revoke table.", e);
        }
    }

    /**
     * Adds an API category
     *
     * @param tenantID     Logged in user's tenant ID
     * @param category     Category
     * @return Category
     */
    public APICategory addCategory(int tenantID, APICategory category) throws APIManagementException {
        String uuid = UUID.randomUUID().toString();
        category.setId(uuid);
        try (Connection connection = APIMgtDBUtil.getConnection();
            PreparedStatement statement = connection.prepareStatement(SQLConstants.ADD_CATEGORY_SQL)) {
            statement.setString(1, uuid);
            statement.setString(2, category.getName());
            statement.setString(3, category.getDescription());
            statement.setInt(4, tenantID);
            statement.executeUpdate();
        } catch (SQLException e) {
            handleException("Failed to add Category: " + uuid, e);
        }
        return category;
    }

    /**
     * Update API Category
     *
     * @param apiCategory API category object with updated details
     * @throws APIManagementException
     */
    public void updateCategory(APICategory apiCategory) throws APIManagementException {
        try (Connection connection = APIMgtDBUtil.getConnection();
                PreparedStatement statement = connection.prepareStatement(SQLConstants.UPDATE_API_CATEGORY)) {
            statement.setString(1, apiCategory.getDescription());
            statement.setString(2, apiCategory.getName());
            statement.setString(3, apiCategory.getId());
            statement.execute();
        } catch (SQLException e) {
            handleException("Failed to update API Category : " + apiCategory.getName() + " of tenant " +
                    APIUtil.getTenantDomainFromTenantId(apiCategory.getTenantID()), e);
        }
    }

    /**
     * Get all available API categories of the tenant
     *
     * @param tenantID
     * @return API Categories List
     */
    public List<APICategory> getAllCategories(int tenantID) throws APIManagementException {
        List<APICategory> categoriesList = new ArrayList<>();
        try (Connection connection = APIMgtDBUtil.getConnection();
            PreparedStatement statement = connection.prepareStatement(SQLConstants.GET_CATEGORIES_BY_TENANT_ID_SQL)) {
            statement.setInt(1, tenantID);

            ResultSet rs = statement.executeQuery();
            while(rs.next()) {
                String id = rs.getString("UUID");
                String name = rs.getString("NAME");
                String description = rs.getString("DESCRIPTION");

                APICategory category = new APICategory();
                category.setId(id);
                category.setName(name);
                category.setDescription(description);
                category.setTenantID(tenantID);

                categoriesList.add(category);
            }
        } catch (SQLException e) {
            handleException("Failed to retrieve API categories for tenant " + tenantID, e);
        }
        return categoriesList;
    }

    /**
     * Checks whether the given category name is already available under given tenant domain with any UUID other than the given UUID
     *
     * @param categoryName
     * @param uuid
     * @param tenantID
     * @return
     */
    public boolean isAPICategoryNameExists(String categoryName, String uuid, int tenantID) throws APIManagementException {
        String sql = SQLConstants.IS_API_CATEGORY_NAME_EXISTS;
        if (uuid != null) {
            sql = SQLConstants.IS_API_CATEGORY_NAME_EXISTS_FOR_ANOTHER_UUID;
        }
        try (Connection connection = APIMgtDBUtil.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, categoryName);
            statement.setInt(2, tenantID);
            if (uuid != null) {
                statement.setString(3, uuid);
            }

            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                int count = rs.getInt("API_CATEGORY_COUNT");
                if (count > 0) {
                    return true;
                }
            }
        } catch (SQLException e) {
            handleException("Failed to check whether API category name : " + categoryName + " exists", e);
        }
        return false;
    }


    public APICategory getAPICategoryByID(String apiCategoryID) throws APIManagementException {
        APICategory apiCategory = null;
        try (Connection connection = APIMgtDBUtil.getConnection();
            PreparedStatement statement = connection.prepareStatement(SQLConstants.GET_API_CATEGORY_BY_ID)) {
            statement.setString(1, apiCategoryID);

            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                apiCategory = new APICategory();
                apiCategory.setName(rs.getString("NAME"));
                apiCategory.setDescription(rs.getString("DESCRIPTION"));
                apiCategory.setTenantID(rs.getInt("TENANT_ID"));
                apiCategory.setId(apiCategoryID);
            }
        } catch (SQLException e) {
            handleException("Failed to fetch API category : " + apiCategoryID, e);
        }
        return apiCategory;
    }

    public APICategory getAPICategoryByName(String apiCategoryName, String tenantDomain) throws APIManagementException {
        APICategory apiCategory = null;
        int tenantID = APIUtil.getTenantIdFromTenantDomain(tenantDomain);
        try (Connection connection = APIMgtDBUtil.getConnection();
                PreparedStatement statement = connection.prepareStatement(SQLConstants.GET_API_CATEGORY_BY_NAME)) {
            statement.setString(1, apiCategoryName);
            statement.setInt(2, tenantID);

            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                apiCategory = new APICategory();
                apiCategory.setName(rs.getString("NAME"));
                apiCategory.setDescription(rs.getString("DESCRIPTION"));
                apiCategory.setTenantID(rs.getInt("TENANT_ID"));
                apiCategory.setId(rs.getString("UUID"));
            }
        } catch (SQLException e) {
            handleException("Failed to fetch API category : " + apiCategoryName + " of tenant " + tenantDomain, e);
        }
        return apiCategory;
    }

    public void deleteCategory(String categoryID) throws APIManagementException {
        try (Connection connection = APIMgtDBUtil.getConnection();
                PreparedStatement statement = connection.prepareStatement(SQLConstants.DELETE_API_CATEGORY)) {
            statement.setString(1, categoryID);
            statement.executeUpdate();
        } catch (SQLException e) {
            handleException("Failed to delete API category : " + categoryID, e);
        }
    }

    public String addUserID(String userID, String userName) throws APIManagementException {
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQLConstants.ADD_USER_ID)) {
            statement.setString(1, userID);
            statement.setString(2, userName);
            statement.executeUpdate();
        } catch (SQLException e) {
            handleException("Failed to add userID for " + userName , e);
        }
        return userID;
    }

    public String getUserID(String userName) throws APIManagementException {
        String userID = null;
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQLConstants.GET_USER_ID)) {
            statement.setString(1, userName);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                userID = rs.getString("USER_ID");
            }
            if (userID == null) {
                userID = UUID.randomUUID().toString();
                addUserID(userID, userName);
            }
        } catch (SQLException e) {
            handleException("Failed to fetch user ID for " + userName , e);
        }
        return userID;
    }

    /**
     * Get names of the tiers which has bandwidth as the quota type
     * @param tenantId id of the tenant
     * @return list of names
     * @throws APIManagementException
     */
    public List<String> getNamesOfTierWithBandwidthQuotaType(int tenantId) throws APIManagementException {
        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        List<String> list = new ArrayList<String>();
        try {
            String sqlQuery = ThrottleSQLConstants.GET_TIERS_WITH_BANDWIDTH_QUOTA_TYPE_SQL;
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            ps.setInt(1, tenantId);
            resultSet = ps.executeQuery();
            while (resultSet.next()) {
                list.add(resultSet.getString(1));
            }
        } catch (SQLException e) {
            handleException("Failed to retrieve tiers with bandwidth QuotaType ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, resultSet);
        }
        return list;
    }

    /**
     * Remove the Pending workflow Requests using ExternalWorkflowReference
     *
     * @param workflowExtRef External Workflow Reference of Workflow Pending Request
     * @throws APIManagementException
     */
    public void deleteWorkflowRequest(String workflowExtRef) throws APIManagementException {

        String query = SQLConstants.DELETE_WORKFLOW_REQUEST_SQL;
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            try {
                connection.setAutoCommit(false);
                prepStmt.setString(1, workflowExtRef);
                prepStmt.executeUpdate();
                connection.commit();
            } catch (SQLException e) {
                handleException("Failed to delete the workflow request. ", e);
            } finally {
                APIMgtDBUtil.closeAllConnections(prepStmt, connection, null);
            }
        } catch (SQLException e) {
            handleException("Failed to delete the workflow request. ", e);
        }
    }

    /**
     * Get the Pending workflow Request using ExternalWorkflowReference
     *
     * @param externalWorkflowRef
     * @return workflow pending request
     * @throws APIManagementException
     */
    public Workflow getworkflowReferenceByExternalWorkflowReference(String externalWorkflowRef) throws APIManagementException {

        ResultSet rs = null;
        Workflow workflow = new Workflow();
        String sqlQuery = SQLConstants.GET_ALL_WORKFLOW_DETAILS_BY_EXTERNALWORKFLOWREF;
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(sqlQuery)) {
            try {
                prepStmt.setString(1, externalWorkflowRef);
                rs = prepStmt.executeQuery();

                while (rs.next()) {
                    workflow.setWorkflowId(rs.getInt("WF_ID"));
                    workflow.setWorkflowReference(rs.getString("WF_REFERENCE"));
                    workflow.setWorkflowType(rs.getString("WF_TYPE"));
                    String workflowstatus = rs.getString("WF_STATUS");
                    workflow.setStatus(org.wso2.carbon.apimgt.api.WorkflowStatus.valueOf(workflowstatus));
                    workflow.setCreatedTime(rs.getTimestamp("WF_CREATED_TIME").toString());
                    workflow.setUpdatedTime(rs.getTimestamp("WF_UPDATED_TIME").toString());
                    workflow.setWorkflowStatusDesc(rs.getString("WF_STATUS_DESC"));
                    workflow.setTenantId(rs.getInt("TENANT_ID"));
                    workflow.setTenantDomain(rs.getString("TENANT_DOMAIN"));
                    workflow.setExternalWorkflowReference(rs.getString("WF_EXTERNAL_REFERENCE"));
                    InputStream metadatablob = rs.getBinaryStream("WF_METADATA");

                    byte[] metadataByte;
                    if (metadatablob != null) {
                        String metadata = APIMgtDBUtil.getStringFromInputStream(metadatablob);
                        Gson metadataGson = new Gson();
                        JSONObject metadataJson = metadataGson.fromJson(metadata, JSONObject.class);
                        workflow.setMetadata(metadataJson);
                    } else {
                        JSONObject metadataJson = new JSONObject();
                        workflow.setMetadata(metadataJson);
                    }
                }
            } catch (SQLException e) {
                handleException("Error when retriving the workflow details. ", e);
            } finally {
                APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
            }
        } catch (SQLException e) {
            handleException("Error when retriving the workflow details. ", e);
        }
        return workflow;
    }

    /**
     * Get the Pending workflow Requests using WorkflowType for a particular tenant
     *
     * @param workflowType Type of the workflow pending request
     * @param status       workflow status of workflow pending request
     * @param tenantDomain tenantDomain of the user
     * @return List of workflow pending request
     * @throws APIManagementException
     */
    public Workflow[] getworkflows(String workflowType, String status, String tenantDomain) throws APIManagementException {

        ResultSet rs = null;
        Workflow[] workflows = null;
        String sqlQuery;
        if (workflowType != null) {
            sqlQuery = SQLConstants.GET_ALL_WORKFLOW_DETAILS_BY_WORKFLOW_TYPE;
        } else {
            sqlQuery = SQLConstants.GET_ALL_WORKFLOW_DETAILS;
        }
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(sqlQuery)) {
            try {
                if (workflowType != null) {
                    prepStmt.setString(1, workflowType);
                    prepStmt.setString(2, status);
                    prepStmt.setString(3, tenantDomain);
                } else {
                    prepStmt.setString(1, status);
                    prepStmt.setString(2, tenantDomain);
                }
                rs = prepStmt.executeQuery();

                ArrayList<Workflow> workflowsList = new ArrayList<Workflow>();
                Workflow workflow;
                while (rs.next()) {
                    workflow = new Workflow();
                    workflow.setWorkflowId(rs.getInt("WF_ID"));
                    workflow.setWorkflowReference(rs.getString("WF_REFERENCE"));
                    workflow.setWorkflowType(rs.getString("WF_TYPE"));
                    String workflowstatus = rs.getString("WF_STATUS");
                    workflow.setStatus(org.wso2.carbon.apimgt.api.WorkflowStatus.valueOf(workflowstatus));
                    workflow.setCreatedTime(rs.getTimestamp("WF_CREATED_TIME").toString());
                    workflow.setUpdatedTime(rs.getTimestamp("WF_UPDATED_TIME").toString());
                    workflow.setWorkflowStatusDesc(rs.getString("WF_STATUS_DESC"));
                    workflow.setTenantId(rs.getInt("TENANT_ID"));
                    workflow.setTenantDomain(rs.getString("TENANT_DOMAIN"));
                    workflow.setExternalWorkflowReference(rs.getString("WF_EXTERNAL_REFERENCE"));
                    workflow.setWorkflowDescription(rs.getString("WF_STATUS_DESC"));
                    InputStream metadataBlob = rs.getBinaryStream("WF_METADATA");
                    InputStream propertiesBlob = rs.getBinaryStream("WF_PROPERTIES");

                    if (metadataBlob != null) {
                        String metadata = APIMgtDBUtil.getStringFromInputStream(metadataBlob);
                        Gson metadataGson = new Gson();
                        JSONObject metadataJson = metadataGson.fromJson(metadata, JSONObject.class);
                        workflow.setMetadata(metadataJson);
                    } else {
                        JSONObject metadataJson = new JSONObject();
                        workflow.setMetadata(metadataJson);
                    }

                    if (propertiesBlob != null) {
                        String properties = APIMgtDBUtil.getStringFromInputStream(propertiesBlob);
                        Gson propertiesGson = new Gson();
                        JSONObject propertiesJson = propertiesGson.fromJson(properties, JSONObject.class);
                        workflow.setProperties(propertiesJson);
                    } else {
                        JSONObject propertiesJson = new JSONObject();
                        workflow.setProperties(propertiesJson);
                    }
                    workflowsList.add(workflow);
                }
                workflows = workflowsList.toArray(new Workflow[workflowsList.size()]);
            } catch (SQLException e) {
                handleException("Error when retrieve all the workflow details. ", e);
            } finally {
                APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
            }
        } catch (SQLException e) {
            handleException("Error when retrieve all the workflow details. ", e);
        }
        return workflows;
    }

    /**
     * Get the Pending workflow Request using ExternalWorkflowReference for a particular tenant
     *
     * @param externelWorkflowRef of pending workflow request
     * @param status              workflow status of workflow pending process
     * @param tenantDomain        tenant domain of user
     * @return workflow pending request
     */
    public Workflow getworkflowReferenceByExternalWorkflowReferenceID(String externelWorkflowRef, String status,
                                                                      String tenantDomain) throws APIManagementException {
        ResultSet rs = null;
        Workflow workflow = new Workflow();
        String sqlQuery = SQLConstants.GET_ALL_WORKFLOW_DETAILS_BY_EXTERNAL_WORKFLOW_REFERENCE;
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(sqlQuery)) {
            try {
                prepStmt.setString(1, externelWorkflowRef);
                prepStmt.setString(2, status);
                prepStmt.setString(3, tenantDomain);
                rs = prepStmt.executeQuery();

                while (rs.next()) {
                    workflow.setWorkflowId(rs.getInt("WF_ID"));
                    workflow.setWorkflowReference(rs.getString("WF_REFERENCE"));
                    workflow.setWorkflowType(rs.getString("WF_TYPE"));
                    String workflowstatus = rs.getString("WF_STATUS");
                    workflow.setStatus(org.wso2.carbon.apimgt.api.WorkflowStatus.valueOf(workflowstatus));
                    workflow.setCreatedTime(rs.getTimestamp("WF_CREATED_TIME").toString());
                    workflow.setUpdatedTime(rs.getTimestamp("WF_UPDATED_TIME").toString());
                    workflow.setWorkflowDescription(rs.getString("WF_STATUS_DESC"));
                    workflow.setTenantId(rs.getInt("TENANT_ID"));
                    workflow.setTenantDomain(rs.getString("TENANT_DOMAIN"));
                    workflow.setExternalWorkflowReference(rs.getString("WF_EXTERNAL_REFERENCE"));
                    Blob metadataBlob = rs.getBlob("WF_METADATA");
                    Blob propertiesBlob = rs.getBlob("WF_PROPERTIES");

                    byte[] metadataByte;
                    if (metadataBlob != null) {
                        metadataByte = metadataBlob.getBytes(1L, (int) metadataBlob.length());
                        InputStream targetStream = new ByteArrayInputStream(metadataByte);
                        String metadata = APIMgtDBUtil.getStringFromInputStream(targetStream);
                        Gson metadataGson = new Gson();
                        JSONObject metadataJson = metadataGson.fromJson(metadata, JSONObject.class);
                        workflow.setMetadata(metadataJson);
                    } else {
                        JSONObject metadataJson = new JSONObject();
                        workflow.setMetadata(metadataJson);
                    }

                    byte[] propertiesByte;
                    if (propertiesBlob != null) {
                        propertiesByte = propertiesBlob.getBytes(1L, (int) propertiesBlob.length());
                        InputStream propertiesTargetStream = new ByteArrayInputStream(propertiesByte);
                        String properties = APIMgtDBUtil.getStringFromInputStream(propertiesTargetStream);
                        Gson propertiesGson = new Gson();
                        JSONObject propertiesJson = propertiesGson.fromJson(properties, JSONObject.class);
                        workflow.setProperties(propertiesJson);
                    } else {
                        JSONObject propertiesJson = new JSONObject();
                        workflow.setProperties(propertiesJson);
                    }
                }
            } catch (SQLException e) {
                handleException("Error when retriving the workflow details. ", e);
            } finally {
                APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
            }
        } catch (SQLException e) {
            handleException("Error when retriving the workflow details. ", e);
        }
        return workflow;
    }

    /**
     * Add shared scope.
     *
     * @param scope        Scope Object to add
     * @param tenantDomain Tenant domain
     * @return UUID of the shared scope
     * @throws APIManagementException if an error occurs while adding shared scope
     */
    public String addSharedScope(Scope scope, String tenantDomain) throws APIManagementException {

        String uuid = UUID.randomUUID().toString();
        String scopeName = scope.getKey();
        int tenantId = APIUtil.getTenantIdFromTenantDomain(tenantDomain);
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQLConstants.ADD_SHARED_SCOPE)) {
            try {
                connection.setAutoCommit(false);
                statement.setString(1, scopeName);
                statement.setString(2, uuid);
                statement.setInt(3, tenantId);
                statement.executeUpdate();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                handleException("Failed to add Shared Scope : " + scopeName, e);
            }
        } catch (SQLException e) {
            handleException("Failed to add Shared Scope: " + scopeName, e);
        }
        return uuid;
    }

    /**
     * Delete shared scope.
     *
     * @param scopeName    shared scope name
     * @param tenantDomain tenant domain
     * @throws APIManagementException if an error occurs while removing shared scope
     */
    public void deleteSharedScope(String scopeName, String tenantDomain) throws APIManagementException {

        int tenantId = APIUtil.getTenantIdFromTenantDomain(tenantDomain);
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQLConstants.DELETE_SHARED_SCOPE)) {
            try {
                connection.setAutoCommit(false);
                statement.setString(1, scopeName);
                statement.setInt(2, tenantId);
                statement.executeUpdate();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                handleException("Failed to delete Shared Scope : " + scopeName + " from tenant: " + tenantDomain, e);
            }
        } catch (SQLException e) {
            handleException("Failed to delete Shared Scope : " + scopeName + " from tenant: " + tenantDomain, e);
        }
    }

    /**
     * Get shared scope key by uuid.
     *
     * @param uuid UUID of shared scope
     * @return Shared scope key
     * @throws APIManagementException if an error occurs while getting shared scope
     */
    public String getSharedScopeKeyByUUID(String uuid) throws APIManagementException {

        String scopeKey = null;
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQLConstants.GET_SHARED_SCOPE_BY_UUID)) {
            statement.setString(1, uuid);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    scopeKey = rs.getString("NAME");
                }
            }
        } catch (SQLException e) {
            handleException("Failed to get Shared Scope : " + uuid, e);
        }
        return scopeKey;
    }

    /***
     * Get the API and URI usages of the given shared scope
     *
     * @param uuid Id of the shared scope
     * @param tenantId tenant Id
     * @return usgaes ofr the shaerd scope
     * @throws APIManagementException If an error occurs while getting the usage details
     */
    public SharedScopeUsage getSharedScopeUsage(String uuid, int tenantId) throws APIManagementException {
        SharedScopeUsage sharedScopeUsage;
        List<API> usedApiList = new ArrayList<>();
        String sharedScopeName = getSharedScopeKeyByUUID(uuid);

        if (sharedScopeName != null) {
            sharedScopeUsage = new SharedScopeUsage();
            sharedScopeUsage.setId(uuid);
            sharedScopeUsage.setName(sharedScopeName);
        } else {
            throw new APIMgtResourceNotFoundException("Shared Scope not found for scope ID: " + uuid,
                    ExceptionCodes.from(ExceptionCodes.SHARED_SCOPE_NOT_FOUND, uuid));
        }

        try (Connection connection = APIMgtDBUtil.getConnection();
                PreparedStatement psForApiUsage = connection
                        .prepareStatement(SQLConstants.GET_SHARED_SCOPE_API_USAGE_BY_TENANT)) {
            psForApiUsage.setString(1, uuid);
            psForApiUsage.setInt(2, tenantId);
            try (ResultSet apiUsageResultSet = psForApiUsage.executeQuery()) {
                while (apiUsageResultSet.next()) {
                    String provider = apiUsageResultSet.getString("API_PROVIDER");
                    String apiName = apiUsageResultSet.getString("API_NAME");
                    String version = apiUsageResultSet.getString("API_VERSION");
                    APIIdentifier apiIdentifier = new APIIdentifier(provider, apiName, version);
                    API usedApi = new API(apiIdentifier);
                    usedApi.setContext(apiUsageResultSet.getString("CONTEXT"));

                    try (PreparedStatement psForUriUsage = connection
                                    .prepareStatement(SQLConstants.GET_SHARED_SCOPE_URI_USAGE_BY_TENANT)) {
                        int apiId = apiUsageResultSet.getInt("API_ID");
                        Set<URITemplate> usedUriTemplates = new LinkedHashSet<>();
                        psForUriUsage.setString(1, uuid);
                        psForUriUsage.setInt(2, tenantId);
                        psForUriUsage.setInt(3, apiId);
                        try (ResultSet uriUsageResultSet = psForUriUsage.executeQuery()) {
                            while (uriUsageResultSet.next()) {
                                URITemplate usedUriTemplate = new URITemplate();
                                usedUriTemplate.setUriTemplate(uriUsageResultSet.getString("URL_PATTERN"));
                                usedUriTemplate.setHTTPVerb(uriUsageResultSet.getString("HTTP_METHOD"));
                                usedUriTemplates.add(usedUriTemplate);
                            }
                        }
                        usedApi.setUriTemplates(usedUriTemplates);
                        usedApiList.add(usedApi);
                    } catch (SQLException e) {
                        handleException("Failed to retrieve Resource usages of shared scope with scope ID " + uuid, e);
                    }
                }
            }

            if (sharedScopeUsage != null) {
                sharedScopeUsage.setApis(usedApiList);
            }

            return sharedScopeUsage;
        } catch (SQLException e) {
            handleException("Failed to retrieve API usages of shared scope with scope ID" + uuid, e);
        }
        return null;
    }

    /**
     * Checks whether the given shared scope name is already available under given tenant domain.
     *
     * @param scopeName Scope Name
     * @param tenantId  Tenant ID
     * @return scope name availability
     * @throws APIManagementException If an error occurs while checking the availability
     */
    public boolean isSharedScopeExists(String scopeName, int tenantId) throws APIManagementException {

        boolean isExist = false;
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQLConstants.IS_SHARED_SCOPE_NAME_EXISTS)) {
            statement.setInt(1, tenantId);
            statement.setString(2, scopeName);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    isExist = true;
                }
            }
        } catch (SQLException e) {
            handleException("Failed to check is exists Shared Scope : " + scopeName + "-" + tenantId, e);
        }
        return isExist;
    }

    /**
     * Get all shared scope keys for tenant.
     *
     * @param tenantDomain Tenant Domain
     * @return shared scope list
     * @throws APIManagementException if an error occurs while getting all shared scopes for tenant
     */
    public Set<String> getAllSharedScopeKeys(String tenantDomain) throws APIManagementException {

        Set<String> scopeKeys = null;
        int tenantId = APIUtil.getTenantIdFromTenantDomain(tenantDomain);
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection
                     .prepareStatement(SQLConstants.GET_ALL_SHARED_SCOPE_KEYS_BY_TENANT)) {
            statement.setInt(1, tenantId);
            try (ResultSet rs = statement.executeQuery()) {
                scopeKeys = new HashSet<>();
                while (rs.next()) {
                    scopeKeys.add(rs.getString("NAME"));
                }
            }
        } catch (SQLException e) {
            handleException("Failed to get all Shared Scope Keys for tenant: " + tenantDomain, e);
        }
        return scopeKeys;
    }

    /**
     * Get all shared scopes for tenant.
     *
     * @param tenantDomain Tenant Domain
     * @return shared scope list
     * @throws APIManagementException if an error occurs while getting all shared scopes for tenant
     */
    public List<Scope> getAllSharedScopes(String tenantDomain) throws APIManagementException {

        List<Scope> scopeList = null;
        int tenantId = APIUtil.getTenantIdFromTenantDomain(tenantDomain);
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection
                     .prepareStatement(SQLConstants.GET_SHARED_SCOPE_USAGE_COUNT_BY_TENANT)) {
            statement.setInt(1, tenantId);
            statement.setInt(2, tenantId);
            try (ResultSet rs = statement.executeQuery()) {
                scopeList = new ArrayList<>();
                while (rs.next()) {
                    Scope scope = new Scope();
                    scope.setId(rs.getString("UUID"));
                    scope.setKey(rs.getString("NAME"));
                    scope.setUsageCount(rs.getInt("usages"));
                    scopeList.add(scope);
                }
            }
        } catch (SQLException e) {
            handleException("Failed to get all Shared Scopes for tenant: " + tenantDomain, e);
        }
        return scopeList;
    }

    /**
     * Adds a tenant theme to the database
     *
     * @param tenantId     tenant ID of user
     * @param themeContent content of the tenant theme
     * @throws APIManagementException if an error occurs when adding a tenant theme to the database
     */
    public void addTenantTheme(int tenantId, InputStream themeContent) throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection
                     .prepareStatement(SQLConstants.TenantThemeConstants.ADD_TENANT_THEME)) {
            statement.setInt(1, tenantId);
            statement.setBinaryStream(2, themeContent);
            statement.executeUpdate();
        } catch (SQLException e) {
            handleException("Failed to add tenant theme of tenant "
                    + APIUtil.getTenantDomainFromTenantId(tenantId), e);
        }
    }

    /**
     * Updates an existing tenant theme in the database
     *
     * @param tenantId     tenant ID of user
     * @param themeContent content of the tenant theme
     * @throws APIManagementException if an error occurs when updating an existing tenant theme in the database
     */
    public void updateTenantTheme(int tenantId, InputStream themeContent) throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(SQLConstants.TenantThemeConstants.UPDATE_TENANT_THEME)) {
            statement.setBinaryStream(1, themeContent);
            statement.setInt(2, tenantId);
            statement.executeUpdate();
        } catch (SQLException e) {
            handleException("Failed to update tenant theme of tenant "
                    + APIUtil.getTenantDomainFromTenantId(tenantId), e);
        }
    }

    /**
     * Retrieves a tenant theme from the database
     *
     * @param tenantId tenant ID of user
     * @return content of the tenant theme
     * @throws APIManagementException if an error occurs when retrieving a tenant theme from the database
     */
    public InputStream getTenantTheme(int tenantId) throws APIManagementException {

        InputStream tenantThemeContent = null;
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection
                     .prepareStatement(SQLConstants.TenantThemeConstants.GET_TENANT_THEME)) {
            statement.setInt(1, tenantId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                tenantThemeContent = resultSet.getBinaryStream("THEME");
            }
        } catch (SQLException e) {
            handleException("Failed to fetch tenant theme of tenant "
                    + APIUtil.getTenantDomainFromTenantId(tenantId), e);
        }
        return tenantThemeContent;
    }

    /**
     * Checks whether a tenant theme exist for a particular tenant
     *
     * @param tenantId tenant ID of user
     * @return true if a tenant theme exist for a particular tenant ID, false otherwise
     * @throws APIManagementException if an error occurs when determining whether a tenant theme exists for a given
     *                                tenant ID
     */
    public boolean isTenantThemeExist(int tenantId) throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection
                     .prepareStatement(SQLConstants.TenantThemeConstants.GET_TENANT_THEME)) {
            statement.setInt(1, tenantId);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            handleException("Failed to check whether tenant theme exist for tenant "
                    + APIUtil.getTenantDomainFromTenantId(tenantId), e);
        }
        return false;
    }

    /**
     * Deletes a tenant theme from the database
     *
     * @param tenantId tenant ID of user
     * @throws APIManagementException if an error occurs when deleting a tenant theme from the database
     */
    public void deleteTenantTheme(int tenantId) throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection
                     .prepareStatement(SQLConstants.TenantThemeConstants.DELETE_TENANT_THEME)) {
            statement.setInt(1, tenantId);
            statement.executeUpdate();
        } catch (SQLException e) {
            handleException("Failed to delete tenant theme of tenant "
                    + APIUtil.getTenantDomainFromTenantId(tenantId), e);
        }
    }

    protected boolean isOauthAppValidationEnabled() {
        APIManagerConfiguration configuration = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                .getAPIManagerConfiguration();

        String isOauthAppValidationEnabled = configuration
                .getFirstProperty(APIConstants.API_KEY_VALIDATOR_ENABLE_PROVISION_APP_VALIDATION);
        if (StringUtils.isNotEmpty(isOauthAppValidationEnabled)) {
            return Boolean.parseBoolean(isOauthAppValidationEnabled);
        }
        return true;
    }
}

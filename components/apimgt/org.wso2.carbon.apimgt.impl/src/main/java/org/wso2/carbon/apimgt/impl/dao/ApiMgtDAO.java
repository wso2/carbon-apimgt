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
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
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
import org.wso2.carbon.apimgt.api.dto.ClientCertificateDTO;
import org.wso2.carbon.apimgt.api.dto.ClonePolicyMetadataDTO;
import org.wso2.carbon.apimgt.api.dto.ConditionDTO;
import org.wso2.carbon.apimgt.api.dto.ConditionGroupDTO;
import org.wso2.carbon.apimgt.api.dto.KeyManagerConfigurationDTO;
import org.wso2.carbon.apimgt.api.dto.UserApplicationAPIUsage;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APICategory;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIInfo;
import org.wso2.carbon.apimgt.api.model.APIKey;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.APIProductIdentifier;
import org.wso2.carbon.apimgt.api.model.APIProductResource;
import org.wso2.carbon.apimgt.api.model.APIRevision;
import org.wso2.carbon.apimgt.api.model.APIRevisionDeployment;
import org.wso2.carbon.apimgt.api.model.APIStatus;
import org.wso2.carbon.apimgt.api.model.APIStore;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.ApplicationInfo;
import org.wso2.carbon.apimgt.api.model.BlockConditionsDTO;
import org.wso2.carbon.apimgt.api.model.Comment;
import org.wso2.carbon.apimgt.api.model.CommentList;
import org.wso2.carbon.apimgt.api.model.DeployedAPIRevision;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.api.model.Identifier;
import org.wso2.carbon.apimgt.api.model.KeyManager;
import org.wso2.carbon.apimgt.api.model.LifeCycleEvent;
import org.wso2.carbon.apimgt.api.model.MonetizationUsagePublishInfo;
import org.wso2.carbon.apimgt.api.model.OAuthAppRequest;
import org.wso2.carbon.apimgt.api.model.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.api.model.OperationPolicy;
import org.wso2.carbon.apimgt.api.model.OperationPolicyData;
import org.wso2.carbon.apimgt.api.model.OperationPolicyDefinition;
import org.wso2.carbon.apimgt.api.model.OperationPolicySpecAttribute;
import org.wso2.carbon.apimgt.api.model.OperationPolicySpecification;
import org.wso2.carbon.apimgt.api.model.Pagination;
import org.wso2.carbon.apimgt.api.model.ResourcePath;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.SharedScopeUsage;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.api.model.VHost;
import org.wso2.carbon.apimgt.api.model.Workflow;
import org.wso2.carbon.apimgt.api.model.botDataAPI.BotDetectionData;
import org.wso2.carbon.apimgt.api.model.graphql.queryanalysis.CustomComplexityDetails;
import org.wso2.carbon.apimgt.api.model.graphql.queryanalysis.GraphqlComplexityInfo;
import org.wso2.carbon.apimgt.api.model.policy.APIPolicy;
import org.wso2.carbon.apimgt.api.model.policy.ApplicationPolicy;
import org.wso2.carbon.apimgt.api.model.policy.BandwidthLimit;
import org.wso2.carbon.apimgt.api.model.policy.Condition;
import org.wso2.carbon.apimgt.api.model.policy.EventCountLimit;
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
import org.wso2.carbon.apimgt.api.model.webhooks.Subscription;
import org.wso2.carbon.apimgt.api.model.webhooks.Topic;
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
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.utils.ApplicationUtils;
import org.wso2.carbon.apimgt.impl.utils.RemoteUserManagerClient;
import org.wso2.carbon.apimgt.impl.utils.VHostUtils;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowConstants;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowExecutorFactory;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowStatus;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.utils.DBUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This class represent the ApiMgtDAO.
 */
public class ApiMgtDAO {

    private static final Log log = LogFactory.getLog(ApiMgtDAO.class);
    private static ApiMgtDAO INSTANCE = null;
    private final Object scopeMutex = new Object();
    private boolean forceCaseInsensitiveComparisons = false;
    private boolean multiGroupAppSharingEnabled = false;
    String migrationEnabled = System.getProperty(APIConstants.MIGRATE);

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
                throw new APIManagementException("Application '" + application.getName() + "' is already registered."
                        , ExceptionCodes.APPLICATION_ALREADY_REGISTERED);
            }

            if (!onlyKeyMappingEntry) {
                appRegPs = conn.prepareStatement(registrationEntry);
                appRegPs.setInt(1, subscriber.getId());
                appRegPs.setString(2, dto.getWorkflowReference());
                appRegPs.setInt(3, application.getId());
                appRegPs.setString(4, dto.getKeyType());
                appRegPs.setString(5, dto.getDomainList());
                appRegPs.setLong(6, dto.getValidityTime());
                appRegPs.setString(7, (String) dto.getAppInfoDTO().getOAuthApplicationInfo().getParameter("tokenScope"
                ));
                appRegPs.setString(8, jsonString);
                appRegPs.setString(9, dto.getKeyManager());
                appRegPs.execute();
            }

            ps = conn.prepareStatement(keyMappingEntry);
            ps.setInt(1, application.getId());
            ps.setString(2, dto.getKeyType());
            ps.setString(3, dto.getStatus().toString());
            ps.setString(4, dto.getKeyManager());
            ps.setString(5, UUID.randomUUID().toString());
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

    public int addSubscription(ApiTypeWrapper apiTypeWrapper, Application application, String status, String subscriber)
            throws APIManagementException {
        int subscriptionId = -1;

        try (Connection conn = APIMgtDBUtil.getConnection()) {
            try {
                conn.setAutoCommit(false);
                subscriptionId = addSubscription(conn, apiTypeWrapper, application, status, subscriber);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            handleException("Failed to add subscriber data ", e);
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
            if (identifier.getId() > 0) {
                id = identifier.getId();
            } else if (identifier instanceof APIIdentifier) {
                String apiUuid;
                if (identifier.getUUID() != null) {
                    apiUuid = identifier.getUUID();
                } else {
                    apiUuid = getUUIDFromIdentifier((APIIdentifier) identifier);
                }
                id = getAPIID(apiUuid, conn);
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

    public void removeAllSubscriptions(String uuid) throws APIManagementException {

        Connection conn = null;
        PreparedStatement ps = null;
        int apiId;

        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);
            apiId = getAPIID(uuid, conn);

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
                Application application = getLightweightApplicationById(conn, applicationId);
                if (APIConstants.API_PRODUCT.equals(resultSet.getString("API_TYPE"))) {
                    APIProductIdentifier apiProductIdentifier = new APIProductIdentifier(
                            APIUtil.replaceEmailDomain(resultSet.getString("API_PROVIDER")),
                            resultSet.getString("API_NAME"), resultSet.getString("API_VERSION"));
                    apiProductIdentifier.setProductId(resultSet.getInt("API_ID"));
                    apiProductIdentifier.setUuid(resultSet.getString("API_UUID"));
                    subscribedAPI = new SubscribedAPI(application.getSubscriber(), apiProductIdentifier);
                } else {
                    APIIdentifier apiIdentifier = new APIIdentifier(
                            APIUtil.replaceEmailDomain(resultSet.getString("API_PROVIDER")),
                            resultSet.getString("API_NAME"), resultSet.getString("API_VERSION"));
                    apiIdentifier.setId(resultSet.getInt("API_ID"));
                    apiIdentifier.setUuid(resultSet.getString("API_UUID"));
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
                Identifier identifier;

                if (APIConstants.API_PRODUCT.equals(resultSet.getString("API_TYPE"))) {
                    identifier = new APIProductIdentifier(
                            APIUtil.replaceEmailDomain(resultSet.getString("API_PROVIDER")),
                            resultSet.getString("API_NAME"), resultSet.getString("API_VERSION"));
                } else {
                    identifier = new APIIdentifier(
                            APIUtil.replaceEmailDomain(resultSet.getString("API_PROVIDER")),
                            resultSet.getString("API_NAME"), resultSet.getString("API_VERSION"));
                }
                identifier.setId(resultSet.getInt("API_ID"));
                identifier.setUuid(resultSet.getString("API_UUID"));
                identifier.setOrganization(resultSet.getString("ORGANIZATION"));
                int applicationId = resultSet.getInt("APPLICATION_ID");
                Application application = getLightweightApplicationById(conn, applicationId);
                application.setSubscriptionCount(getSubscriptionCountByApplicationId(conn, application,
                        identifier.getOrganization()));
                subscribedAPI = new SubscribedAPI(application.getSubscriber(), identifier);

                subscribedAPI.setUUID(resultSet.getString("UUID"));
                subscribedAPI.setSubscriptionId(resultSet.getInt("SUBSCRIPTION_ID"));
                subscribedAPI.setSubStatus(resultSet.getString("SUB_STATUS"));
                subscribedAPI.setSubCreatedStatus(resultSet.getString("SUBS_CREATE_STATE"));
                subscribedAPI.setTier(new Tier(resultSet.getString("TIER_ID")));
                subscribedAPI.setRequestedTier(new Tier(resultSet.getString("TIER_ID_PENDING")));

                Timestamp createdTime = resultSet.getTimestamp("CREATED_TIME");
                subscribedAPI.setCreatedTime(createdTime == null ? null : String.valueOf(createdTime.getTime()));
                try {
                    Timestamp updatedTime = resultSet.getTimestamp("UPDATED_TIME");
                    subscribedAPI.setUpdatedTime(
                            updatedTime == null ? null : String.valueOf(updatedTime.getTime()));
                } catch (SQLException e) {
                    // fixing Timestamp issue with default value '0000-00-00 00:00:00'for existing applications created
                    subscribedAPI.setUpdatedTime(subscribedAPI.getCreatedTime());
                }
                subscribedAPI.setApplication(application);
                subscribedAPI.setOrganization(resultSet.getString("ORGANIZATION"));
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

    /**
     * Retrieves the Topic for a specified async API.
     *
     * @param apiId API UUID
     * @return Set of Topic objects
     * @throws APIManagementException if failed to retrieve topics of the web hook API
     */
    public Set<Topic> getAPITopics(String apiId) throws APIManagementException {

        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        String getTopicsQuery = SQLConstants.GET_ALL_TOPICS_BY_API_ID;
        Set<Topic> topicSet = new HashSet();

        try {
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(getTopicsQuery);
            ps.setString(1, apiId);
            resultSet = ps.executeQuery();
            while (resultSet.next()) {
                Topic topic = new Topic();
                topic.setName(resultSet.getString("URL_PATTERN"));
                topic.setApiId(resultSet.getString("API_ID"));
                topic.setType(resultSet.getString("HTTP_METHOD"));
                topicSet.add(topic);
            }
            return topicSet;
        } catch (SQLException e) {
            handleException("Failed to retrieve topics available in api " + apiId, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, resultSet);
        }
        return null;
    }

    /**
     * Retrieves set of web hook topic subscriptions for a application.
     *
     * @param applicationId application UUID
     * @return set of web hook subscriptions.
     * @throws APIManagementException if failed to retrieve web hook topc subscriptions
     */
    public Set<Subscription> getTopicSubscriptions(String applicationId) throws APIManagementException {

        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        String getTopicSubscriptionsQuery = SQLConstants.GET_WH_TOPIC_SUBSCRIPTIONS;
        Set<Subscription> subscriptionSet = new HashSet();
        try {
            conn = APIMgtDBUtil.getConnection();
            if (conn.getMetaData().getDriverName().contains("PostgreSQL")) {
                getTopicSubscriptionsQuery = SQLConstants.GET_WH_TOPIC_SUBSCRIPTIONS_POSTGRE_SQL;
            }
            ps = conn.prepareStatement(getTopicSubscriptionsQuery);
            ps.setString(1, applicationId);
            resultSet = ps.executeQuery();
            while (resultSet.next()) {
                Subscription subscription = new Subscription();
                subscription.setApiUuid(resultSet.getString("API_UUID"));
                subscription.setCallback(resultSet.getString("HUB_CALLBACK_URL"));
                Timestamp deliveryTime = resultSet.getTimestamp("DELIVERED_AT");
                if (deliveryTime != null) {
                    subscription.setLastDelivery(new Date(deliveryTime.getTime()));
                }
                subscription.setLastDeliveryState(resultSet.getInt("DELIVERY_STATE"));
                subscription.setTopic(resultSet.getString("HUB_TOPIC"));
                subscription.setAppID(resultSet.getString("APPLICATION_ID"));
                subscriptionSet.add(subscription);
            }
        } catch (SQLException e) {
            handleException("Failed to retrieve topic subscriptions for application  " + applicationId, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, resultSet);
        }

        return null;
    }

    /**
     * Retrieves the web hook topc subscriptions from an application to a given api.
     *
     * @param applicationId application uuid
     * @param apiId         api uuid
     * @return set of web hook topic subscriptions
     * @throws APIManagementException
     */
    public Set<Subscription> getTopicSubscriptionsByApiUUID(String applicationId, String apiId) throws APIManagementException {

        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        String getTopicSubscriptionsByApiIdQuery = SQLConstants.GET_WH_TOPIC_SUBSCRIPTIONS_BY_API_KEY;
        Set<Subscription> subscriptionSet = new HashSet();
        try {
            conn = APIMgtDBUtil.getConnection();
            if (conn.getMetaData().getDriverName().contains("PostgreSQL")) {
                getTopicSubscriptionsByApiIdQuery = SQLConstants.GET_WH_TOPIC_SUBSCRIPTIONS_BY_API_KEY_POSTGRE_SQL;
            }
            ps = conn.prepareStatement(getTopicSubscriptionsByApiIdQuery);
            ps.setString(1, applicationId);
            ps.setString(2, apiId);
            resultSet = ps.executeQuery();
            while (resultSet.next()) {
                Subscription subscription = new Subscription();
                subscription.setApiUuid(resultSet.getString("API_UUID"));
                subscription.setCallback(resultSet.getString("HUB_CALLBACK_URL"));
                Timestamp deliveryTime = resultSet.getTimestamp("DELIVERED_AT");
                if (deliveryTime != null) {
                    subscription.setLastDelivery(new Date(deliveryTime.getTime()));
                }
                subscription.setLastDeliveryState(resultSet.getInt("DELIVERY_STATE"));
                subscription.setTopic(resultSet.getString("HUB_TOPIC"));
                subscription.setAppID(resultSet.getString("APPLICATION_ID"));
                subscriptionSet.add(subscription);
            }
            return subscriptionSet;
        } catch (SQLException e) {
            handleException("Failed to retrieve topic subscriptions for application  " + applicationId, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, resultSet);
        }

        return null;
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
                "FROM AM_APPLICATION_GROUP_MAPPING  WHERE GROUP_ID IN ($params) AND TENANT = ?))  OR  ( LOWER(SUB" +
                ".USER_ID) = LOWER" +
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
                apiIdentifier.setUuid(result.getString("API_UUID"));

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

    public Set<String> getScopesForApplicationSubscription(Subscriber subscriber, int applicationId)
            throws APIManagementException {

        PreparedStatement getIncludedApisInProduct = null;
        PreparedStatement getSubscribedApisAndProducts = null;
        ResultSet resultSet = null;
        Set<String> scopeKeysSet = new HashSet<>();
        Set<Integer> apiIdSet = new HashSet<>();
        int tenantId = APIUtil.getTenantId(subscriber.getName());

        try (Connection conn = APIMgtDBUtil.getConnection()) {
            String sqlQueryForGetSubscribedApis = SQLConstants.GET_SUBSCRIBED_API_IDs_BY_APP_ID_SQL;
            getSubscribedApisAndProducts = conn.prepareStatement(sqlQueryForGetSubscribedApis);
            getSubscribedApisAndProducts.setInt(1, tenantId);
            getSubscribedApisAndProducts.setInt(2, applicationId);
            resultSet = getSubscribedApisAndProducts.executeQuery();
            String getIncludedApisInProductQuery = SQLConstants.GET_INCLUDED_APIS_IN_PRODUCT_SQL;
            getIncludedApisInProduct = conn.prepareStatement(getIncludedApisInProductQuery);
            while (resultSet.next()) {
                int apiId = resultSet.getInt("API_ID");
                getIncludedApisInProduct.setInt(1, apiId);
                try (ResultSet resultSet1 = getIncludedApisInProduct.executeQuery()) {
                    while (resultSet1.next()) {
                        int includedApiId = resultSet1.getInt("API_ID");
                        apiIdSet.add(includedApiId);
                    }
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
            APIMgtDBUtil.closeAllConnections(getIncludedApisInProduct, null, null);
        }
        return scopeKeysSet;
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
                    "AM_APPLICATION_GROUP_MAPPING WHERE GROUP_ID IN ($params) AND TENANT = ?))  OR  ( SUB.USER_ID = ?" +
                    " ))";
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

    public Integer getSubscriptionCountByApplicationId(Application application, String organization)
            throws APIManagementException {

        int subscriptionCount = 0;

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            return getSubscriptionCountByApplicationId(connection, application, organization);
        } catch (SQLException e) {
            handleException("Failed to get SubscribedAPI of : application " + application.getName() + " in " +
                    "organization " + organization, e);
        }
        return subscriptionCount;
    }

    private Integer getSubscriptionCountByApplicationId(Connection connection, Application application,
                                                        String organization) throws SQLException {

        int subscriptionCount = 0;
        String sqlQuery = SQLConstants.GET_SUBSCRIPTION_COUNT_BY_APP_ID_SQL;
        try (PreparedStatement ps = connection.prepareStatement(sqlQuery)) {
            ps.setInt(1, application.getId());
            ps.setString(2, organization);
            try (ResultSet result = ps.executeQuery()) {
                while (result.next()) {
                    subscriptionCount = result.getInt("SUB_COUNT");
                }
            }
        }

        return subscriptionCount;
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
                                               String organization, PreparedStatement statement) throws SQLException {

        int tenantId = APIUtil.getTenantId(subscriber.getName());
        int paramIndex = 0;

        if (groupingId != null && !"null".equals(groupingId) && !groupingId.isEmpty()) {
            if (multiGroupAppSharingEnabled) {
                String tenantDomain = MultitenantUtils.getTenantDomain(subscriber.getName());

                String[] groupIDArray = groupingId.split(",");

                statement.setInt(++paramIndex, tenantId);
                statement.setString(++paramIndex, applicationName);
                statement.setString(++paramIndex, organization);
                for (String groupId : groupIDArray) {
                    statement.setString(++paramIndex, groupId);
                }
                statement.setString(++paramIndex, tenantDomain);
                statement.setString(++paramIndex, subscriber.getName());
            } else {
                statement.setInt(++paramIndex, tenantId);
                statement.setString(++paramIndex, applicationName);
                statement.setString(++paramIndex, organization);
                statement.setString(++paramIndex, groupingId);
                statement.setString(++paramIndex, subscriber.getName());
            }
        } else {
            statement.setInt(++paramIndex, tenantId);
            statement.setString(++paramIndex, applicationName);
            statement.setString(++paramIndex, organization);
            statement.setString(++paramIndex, subscriber.getName());
        }

        return statement.executeQuery();
    }

    private void initSubscribedAPI(SubscribedAPI subscribedAPI, ResultSet resultSet)
            throws SQLException {

        subscribedAPI.setUUID(resultSet.getString("SUB_UUID"));
        subscribedAPI.setSubStatus(resultSet.getString("SUB_STATUS"));
        subscribedAPI.setSubCreatedStatus(resultSet.getString("SUBS_CREATE_STATE"));
        subscribedAPI.setTier(new Tier(resultSet.getString(APIConstants.SUBSCRIPTION_FIELD_TIER_ID)));
        subscribedAPI.setRequestedTier(new Tier(resultSet.getString("TIER_ID_PENDING")));
    }

    /**
     * This method returns the set of APIs for given subscriber
     *
     * @param organization identifier of the organization
     * @param subscriber subscriber
     * @return Set<API>
     * @throws org.wso2.carbon.apimgt.api.APIManagementException if failed to get SubscribedAPIs
     */
    public Set<SubscribedAPI> getSubscribedAPIs(String organization, Subscriber subscriber, String groupingId)
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
             ResultSet result = getSubscriptionResultSet(groupingId, subscriber, ps, organization)) {
            while (result.next()) {
                String apiType = result.getString("TYPE");

                if (APIConstants.API_PRODUCT.toString().equals(apiType)) {
                    APIProductIdentifier identifier =
                            new APIProductIdentifier(APIUtil.replaceEmailDomain(result.getString("API_PROVIDER")),
                                    result.getString("API_NAME"), result.getString("API_VERSION"));
                    identifier.setUuid(result.getString("API_UUID"));
                    SubscribedAPI subscribedAPI = new SubscribedAPI(subscriber, identifier);

                    initSubscribedAPIDetailed(connection, subscribedAPI, subscriber, result);
                    subscribedAPIs.add(subscribedAPI);
                } else {
                    APIIdentifier identifier = new APIIdentifier(APIUtil.replaceEmailDomain(result.getString
                            ("API_PROVIDER")), result.getString("API_NAME"),
                            result.getString("API_VERSION"));
                    identifier.setUuid(result.getString("API_UUID"));
                    SubscribedAPI subscribedAPI = new SubscribedAPI(subscriber, identifier);

                    initSubscribedAPIDetailed(connection,subscribedAPI, subscriber, result);
                    subscribedAPIs.add(subscribedAPI);
                }
            }
        } catch (SQLException e) {
            handleException("Failed to get SubscribedAPI of :" + subscriber.getName(), e);
        }

        return subscribedAPIs;
    }

    private ResultSet getSubscriptionResultSet(String groupingId, Subscriber subscriber,
                                               PreparedStatement statement, String organization) throws SQLException {

        int tenantId = APIUtil.getTenantId(subscriber.getName());
        int paramIndex = 0;

        if (groupingId != null && !"null".equals(groupingId) && !groupingId.isEmpty()) {
            if (multiGroupAppSharingEnabled) {
                String tenantDomain = MultitenantUtils.getTenantDomain(subscriber.getName());
                String[] groupIDArray = groupingId.split(",");

                statement.setInt(++paramIndex, tenantId);
                statement.setString(++paramIndex, organization);
                for (String groupId : groupIDArray) {
                    statement.setString(++paramIndex, groupId);
                }
                statement.setString(++paramIndex, tenantDomain);
                statement.setString(++paramIndex, subscriber.getName());
            } else {
                statement.setInt(++paramIndex, tenantId);
                statement.setString(++paramIndex, organization);
                statement.setString(++paramIndex, groupingId);
                statement.setString(++paramIndex, subscriber.getName());
            }
        } else {
            statement.setInt(++paramIndex, tenantId);
            statement.setString(++paramIndex, organization);
            statement.setString(++paramIndex, subscriber.getName());
        }

        return statement.executeQuery();
    }

    private void initSubscribedAPIDetailed(Connection connection, SubscribedAPI subscribedAPI, Subscriber subscriber, ResultSet result)
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
            application.setGroupId(getGroupId(connection, application.getId()));
            application.setOwner(result.getString("OWNER"));
        }

        subscribedAPI.setApplication(application);
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
        Map<String, OAuthApplicationInfo> keyTypeWiseOAuthApps = new HashMap<>();
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
                if (consumerKey != null) {
                    KeyManager keyManager = KeyManagerHolder.getKeyManagerInstance(tenntDomain, keyManagerName);
                    if (keyManager != null) {
                        OAuthApplicationInfo oAuthApplication = keyManager.retrieveApplication(consumerKey);
                        keyTypeWiseOAuthApps.put(keyManagerName, oAuthApplication);
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

    public void deleteThrottlingPermissions(String tierName, int tenantId) throws APIManagementException {
        int tierPermissionId = -1;
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(SQLConstants.GET_THROTTLE_TIER_PERMISSION_ID_SQL)) {
            ps.setString(1, tierName);
            ps.setInt(2, tenantId);
            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next()) {
                    tierPermissionId = resultSet.getInt("THROTTLE_TIER_PERMISSIONS_ID");
                }
            }
            if (tierPermissionId != -1) {
                try (PreparedStatement preparedStatement = connection.prepareStatement(SQLConstants
                        .DELETE_THROTTLE_TIER_PERMISSION_SQL)) {
                    preparedStatement.setInt(1, tierPermissionId);
                    preparedStatement.setInt(2, tenantId);
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            handleException("Error in deleting tier permissions: " + e.getMessage(), e);
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

    /**
     * This method returns the set of Subscribers for given provider
     *
     * @param providerName name of the provider
     * @return Set<Subscriber>
     * @throws APIManagementException if failed to get subscribers for given provider
     */
    @Deprecated
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

    /**
     * Function returns the set of Subscribers by eliminating replicated subscribers
     *
     * @param identifier    API Identifier
     * @param subscriberMap Map of subscriber id and an integer value
     * @return Set of subscribers
     * @throws APIManagementException if failed to get subscribers for given provider
     */
    public Set<Subscriber> getSubscribersOfAPIWithoutDuplicates(APIIdentifier identifier,
            Map<Integer, Integer> subscriberMap) throws APIManagementException {

        Set<Subscriber> subscribers = new HashSet<Subscriber>();

        try (Connection connection = APIMgtDBUtil.getConnection();
                PreparedStatement ps = connection.prepareStatement(SQLConstants.GET_SUBSCRIBERS_OF_API_SQL);) {

            ps.setString(1, APIUtil.replaceEmailDomainBack(identifier.getProviderName()));
            ps.setString(2, identifier.getApiName());
            ps.setString(3, identifier.getVersion());

            try (ResultSet resultSet = ps.executeQuery()) {
                while (resultSet.next()) {
                    Subscriber subscriber = new Subscriber(resultSet.getString(APIConstants.SUBSCRIBER_FIELD_USER_ID));
                    subscriber.setSubscribedDate(resultSet.getTimestamp(APIConstants.SUBSCRIBER_FIELD_DATE_SUBSCRIBED));

                    if (!subscriberMap.containsKey(subscriber.getId())) {
                        subscribers.add(subscriber);
                        subscriberMap.put(subscriber.getId(), 1);
                    }
                }
            }
        } catch (SQLException e) {
            handleException("Failed to get subscribers for :" + identifier.getApiName(), e);
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
            handleException("Failed to get subscription count for " + artifactType, e);
        }

        return subscriptions;
    }

    /**
     * This method is used to update the subscription
     *
     * @param identifier    APIIdentifier
     * @param subStatus     Subscription Status[BLOCKED/UNBLOCKED]
     * @param applicationId Application id
     * @param organization  Organization
     * @throws org.wso2.carbon.apimgt.api.APIManagementException if failed to update subscriber
     */
    public void updateSubscription(APIIdentifier identifier, String subStatus, int applicationId, String organization)
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

            String subsCreateStatus = getSubscriptionCreaeteStatus(identifier, applicationId, organization, conn);

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
            if (subscribedAPI.getRequestedTier().getName() == null) {
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

        Map<String, String> keyManagerWiseApprovalState = new HashMap<>();
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
     *
     * @param application
     * @param keyType
     * @param keyManagerId
     */
    public void updateApplicationKeyTypeMapping(Application application, String keyType,
                                                String keyManagerId) throws APIManagementException {

        OAuthApplicationInfo app = application.getOAuthApp(keyType, keyManagerId);
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
     * @param keyType         key type.
     * @param applicationId   apim application id.
     * @param clientId        consumer key.
     * @param keyMappingId    key mapping id.
     * @throws APIManagementException   if an error occurs while creation key mappings.
     */
    public void createApplicationKeyTypeMappingForManualClients(String keyType, int applicationId,
                                                                String clientId, String keyManagerId,
                                                                String keyMappingId) throws APIManagementException {

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
     * @param uuid API uuid
     * @param organization Organization of the API
     * @return UserApplicationAPIUsage of given provider
     * @throws org.wso2.carbon.apimgt.api.APIManagementException if failed to get
     *                                                           UserApplicationAPIUsage for given provider
     */
    public UserApplicationAPIUsage[] getAllAPIUsageByProviderAndApiId(String uuid, String organization)
            throws APIManagementException {

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet result = null;

        try {
            String sqlQuery = SQLConstants.GET_APP_API_USAGE_BY_UUID_SQL;
            connection = APIMgtDBUtil.getConnection();

            ps = connection.prepareStatement(sqlQuery);
            ps.setString(1, uuid);
            ps.setString(2, organization);
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
                apiSubscription.setApplication(applicationObj);
                usage.addApiSubscriptions(apiSubscription);
            }
            return userApplicationUsages.values().toArray(new UserApplicationAPIUsage[userApplicationUsages.size()]);
        } catch (SQLException e) {
            handleException("Failed to find API Usage for API with UUID :" + uuid, e);
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
                Map<String, UserApplicationAPIUsage> userApplicationUsages = new TreeMap<String,
                        UserApplicationAPIUsage>();
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
                    APIProductIdentifier apiProductId = new APIProductIdentifier(result.getString("API_PROVIDER"),
                            result.getString
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
     * @param provider   Name of API creator
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


    public int addApplication(Application application, String userId, String organization)
            throws APIManagementException {
        Connection conn = null;
        int applicationId = 0;
        String loginUserName = getLoginUserName(userId);
        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);
            applicationId = addApplication(application, loginUserName, conn, organization);
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

    public void addRating(String id, int rating, String user) throws APIManagementException {

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
     * @param uuid API uuid
     * @param rating     Rating
     * @param userId     User Id
     * @throws APIManagementException if failed to add Rating
     */
    public void addOrUpdateRating(String uuid, int rating, String userId, Connection conn)
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
            id = getAPIID(uuid, conn);
            if (id == -1) {
                String msg = "Could not load API record for API with UUID : " + uuid;
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

    public void removeAPIRating(String uuid, String user) throws APIManagementException {

        Connection conn = null;
        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);

            removeAPIRating(uuid, user, conn);

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
     * @param uuid API uuid
     * @param userId     User Id
     * @throws APIManagementException if failed to remove API user Rating
     */
    public void removeAPIRating(String uuid, String userId, Connection conn)
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
            id = getAPIID(uuid, conn);
            if (id == -1) {
                String msg = "Could not load API record for API with UUID: " + uuid;
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

    public int getUserRating(String uuid, String user) throws APIManagementException {

        Connection conn = null;
        int userRating = 0;
        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);

            userRating = getUserRating(uuid, user, conn);

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
     * @param uuid API uuid
     * @param userId     User Id
     * @throws APIManagementException if failed to get User API Rating
     */
    public int getUserRating(String uuid, String userId, Connection conn)
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
            id = getAPIID(uuid, conn);
            if (id == -1) {
                String msg = "Could not load API record for API with UUID : " + uuid;
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
     * @param uuid API uuid
     * @param user       User name
     * @throws APIManagementException if failed to get user API Ratings
     */
    public JSONObject getUserRatingInfo(String uuid, String user) throws APIManagementException {

        Connection conn = null;
        JSONObject userRating = null;
        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);

            userRating = getUserRatingInfo(uuid, user, conn);

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
     * @param uuid API uuid
     * @param userId     User Id
     * @param conn       Database connection
     * @throws APIManagementException if failed to get user API Ratings
     */
    private JSONObject getUserRatingInfo(String uuid, String userId, Connection conn)
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
            id = getAPIID(uuid, conn);

            String sqlQuery = SQLConstants.GET_API_RATING_INFO_SQL;
            if (id == -1) {
                String msg = "Could not load API record for API with UUID: " + uuid;
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
     * @param apiId API uuid
     * @throws APIManagementException if failed to get API Ratings
     */
    public JSONArray getAPIRatings(String apiId) throws APIManagementException {

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
     * @param uuid API uuid
     * @param conn       Database connection
     * @throws APIManagementException if failed to get API Ratings
     */
    private JSONArray getAPIRatings(String uuid, Connection conn)
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
            id = getAPIID(uuid, conn);
            if (id == -1) {
                String msg = "Could not load API record for API with UUID: " + uuid;
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
                ratingObj.put(APIConstants.RATING, userRating);
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

    public float getAverageRating(String apiId) throws APIManagementException {

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
     * @param uuid API uuid
     * @throws APIManagementException if failed to add Application
     */
    public float getAverageRating(String uuid, Connection conn)
            throws APIManagementException, SQLException {

        PreparedStatement ps = null;
        ResultSet rs = null;
        float avrRating = 0;
        try {
            //Get API Id
            int apiId;
            apiId = getAPIID(uuid, conn);
            if (apiId == -1) {
                String msg = "Could not load API record for API with UUID: " + uuid;
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
     * @param tenantDomain   tenant domain of the block condition
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
                blockCondition.setConditionValue(resultSet.getString("BLOCK_CONDITION"));
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
     * @param application  Application
     * @param userId       User Id
     * @param organization Identifier of an organization
     * @throws APIManagementException if failed to add Application
     */
    public int addApplication(Application application, String userId, Connection conn, String organization)
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
            ps.setString(13, organization);
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
            preparedStatement.setInt(1, application.getId());
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
        } finally {
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
     * @param organization identifier of the organization
     * @return true if application is available for the subscriber
     * @throws APIManagementException if failed to get applications for given subscriber
     */
    public boolean isApplicationExist(String appName, String username, String groupId,
                                      String organization) throws APIManagementException {

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
                    preparedStatement = fillQueryParams(connection, sqlQuery, grpIdArray, 3);
                    preparedStatement.setString(1, appName);
                    preparedStatement.setString(2, organization);
                    int paramIndex = noOfParams + 2;
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
                    preparedStatement.setString(2, organization);
                    preparedStatement.setString(3, groupId);
                    preparedStatement.setString(4, subscriber.getName());
                }
            } else {
                if (forceCaseInsensitiveComparisons) {
                    sqlQuery += whereClauseCaseInsensitive;
                } else {
                    sqlQuery += whereClause;
                }
                preparedStatement = connection.prepareStatement(sqlQuery);
                preparedStatement.setString(1, appName);
                preparedStatement.setString(2, organization);
                preparedStatement.setString(3, subscriber.getName());
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
     *
     * @param applicationName application name
     * @param username username
     * @param groupId group id
     * @return whether a certain application group combination exists or not
     * @throws APIManagementException if failed to assess whether a certain application group combination exists or not
     */
    public boolean isApplicationGroupCombinationExists(String applicationName, String username, String groupId)
            throws APIManagementException {
        if (username == null) {
            return false;
        }

        Subscriber subscriber = getSubscriber(username);

        int appId = 0;

        String sqlQuery = SQLConstants.GET_APPLICATION_ID_PREFIX_FOR_GROUP_COMPARISON;
        String whereClauseWithGroupId = " AND APP.GROUP_ID = ?";
        String whereClauseWithMultiGroupId = " AND (APP.APPLICATION_ID IN (SELECT APPLICATION_ID  FROM "
                + "AM_APPLICATION_GROUP_MAPPING WHERE GROUP_ID IN ($params) AND TENANT = ?))";

        try (Connection connection = APIMgtDBUtil.getConnection();) {
            if (!StringUtils.isEmpty(groupId)) {
                if (multiGroupAppSharingEnabled) {
                    sqlQuery += whereClauseWithMultiGroupId;
                    String tenantDomain = MultitenantUtils.getTenantDomain(subscriber.getName());
                    String[] grpIdArray = groupId.split(",");

                    int noOfParams = grpIdArray.length;

                    try (PreparedStatement preparedStatement = fillQueryParams(connection, sqlQuery,
                            grpIdArray, 2)) {

                        preparedStatement.setString(1, applicationName);
                        int paramIndex = noOfParams + 1;
                        preparedStatement.setString(++paramIndex, tenantDomain);

                        try (ResultSet resultSet = preparedStatement.executeQuery();) {
                            if (resultSet.next()) {
                                appId = resultSet.getInt("APPLICATION_ID");
                            }

                            if (appId > 0) {
                                return true;
                            }
                        }
                    }
                } else {
                    sqlQuery += whereClauseWithGroupId;
                    try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);) {
                        preparedStatement.setString(1, applicationName);
                        preparedStatement.setString(2, groupId);

                        try (ResultSet resultSet = preparedStatement.executeQuery();) {
                            if (resultSet.next()) {
                                appId = resultSet.getInt("APPLICATION_ID");
                            }

                            if (appId > 0) {
                                return true;
                            }
                        }
                    }
                }
            }

        } catch (SQLException e) {
            handleException("Error while getting application group combination data for application: " + applicationName, e);
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
    public boolean isApplicationOwnedBySubscriber(String appName, String username, String organization) throws APIManagementException {

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
            preparedStatement.setString(2, organization);
            preparedStatement.setString(3, subscriber.getName());
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

    public String getApplicationUUID(String appName, String username) throws APIManagementException {

        if (username == null) {
            return null;
        }
        Subscriber subscriber = getSubscriber(username);
        String applicationUUID = null;

        String sql = "SELECT UUID FROM AM_APPLICATION WHERE NAME = ? AND SUBSCRIBER_ID  = ?";

        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(sql)) {
            prepStmt.setString(1, appName);
            prepStmt.setInt(2, subscriber.getId());
            try (ResultSet rs = prepStmt.executeQuery()) {
                if (rs.next()) {
                    applicationUUID = rs.getString("UUID");
                }
            }
        } catch (SQLException e) {
            handleException("Error when getting the application id from" + " the persistence store.", e);
        }
        return applicationUUID;
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
     * @param subscriber   The subscriber.
     * @param groupingId   The groupId to which the applications must belong.
     * @param start        The start index.
     * @param offset       The offset.
     * @param search       The search string.
     * @param sortOrder    The sort order.
     * @param sortColumn   The sort column.
     * @param organization Identifier of an organization
     * @return Application[] The array of applications.
     * @throws APIManagementException
     */
    public Application[] getApplicationsWithPagination(Subscriber subscriber, String groupingId, int start,
                                                       int offset, String search, String sortColumn, String sortOrder, String organization)
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
                    prepStmt.setString(++noOfParams, organization);
                    prepStmt.setString(++noOfParams, "%" + search + "%");
                    prepStmt.setInt(++noOfParams, start);
                    prepStmt.setInt(++noOfParams, offset);
                } else {
                    prepStmt = connection.prepareStatement(sqlQuery);
                    prepStmt.setString(1, groupingId);
                    prepStmt.setString(2, subscriber.getName());
                    prepStmt.setString(3, organization);
                    prepStmt.setString(4, "%" + search + "%");
                    prepStmt.setInt(5, start);
                    prepStmt.setInt(6, offset);
                }
            } else {
                prepStmt = connection.prepareStatement(sqlQuery);
                prepStmt.setString(1, subscriber.getName());
                prepStmt.setString(2, organization);
                prepStmt.setString(3, "%" + search + "%");
                prepStmt.setInt(4, start);
                prepStmt.setInt(5, offset);
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
                application.setTokenType(rs.getString("TOKEN_TYPE"));
                application.setLastUpdatedTime(String.valueOf(rs.getTimestamp("APP_UPDATED_TIME").getTime()));
                application.setCreatedTime(String.valueOf(rs.getTimestamp("APP_CREATED_TIME").getTime()));

                if (multiGroupAppSharingEnabled) {
                    setGroupIdInApplication(connection,application);
                }

                //setting subscription count
                int subscriptionCount = getSubscriptionCountByApplicationId(connection,application, organization);
                application.setSubscriptionCount(subscriptionCount);

                // Get custom attributes of application
                Map<String, String> applicationAttributes = getApplicationAttributes(connection, applicationId);
                application.setApplicationAttributes(applicationAttributes);

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
                        "AM_BLOCK_CONDITIONS bl on  ( bl.TYPE = 'APPLICATION' AND bl.BLOCK_CONDITION = (x.USER_ID + ':') + x" +
                        ".name)";
            } else {
                blockingFilerSql = " select distinct x.*,bl.ENABLED from ( " + sqlQuery
                        + " )x left join AM_BLOCK_CONDITIONS bl on  ( bl.TYPE = 'APPLICATION' AND bl.BLOCK_CONDITION = "
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
            Map<String, String> applicationAttributes;
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
                    setGroupIdInApplication(connection, application);
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
     * Retrieve the applications by user/application name
     *
     * @param user
     * @param owner
     * @param tenantId
     * @param limit
     * @param offset
     * @param sortBy
     * @param sortOrder
     * @param appName
     * @return
     * @throws APIManagementException
     */
    public Application[] getApplicationsWithPagination(String user, String owner, int tenantId, int limit,
                                                       int offset, String sortBy, String sortOrder, String appName)
            throws APIManagementException {

        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        String sqlQuery = null;
        List<Application> applicationList = new ArrayList<>();
        sqlQuery = SQLConstantManagerFactory.getSQlString("GET_APPLICATIONS_BY_TENANT_ID");
        Application[] applications = null;
        try {
            connection = APIMgtDBUtil.getConnection();
            String driverName = connection.getMetaData().getDriverName();
            if (driverName.contains("Oracle")) {
                limit = offset + limit;
            }
            sqlQuery = sqlQuery.replace("$1", sortBy);
            sqlQuery = sqlQuery.replace("$2", sortOrder);
            prepStmt = connection.prepareStatement(sqlQuery);
            prepStmt.setInt(1, tenantId);
            prepStmt.setString(2, "%" + owner + "%");
            prepStmt.setString(3, "%" + appName + "%");
            prepStmt.setInt(4, offset);
            prepStmt.setInt(5, limit);
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
            applications = applicationList.toArray(new Application[applicationList.size()]);
        } catch (SQLException e) {
            handleException("Error while obtaining details of the Application for tenant id : " + tenantId, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
        }
        return applications;
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
        sqlQuery += tenantFilter;
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
     * @param organization Organization
     * @return Consumerkeys
     * @throws org.wso2.carbon.apimgt.api.APIManagementException if failed to get Applications for given subscriber.
     */
    public String[] getConsumerKeys(APIIdentifier identifier, String organization) throws APIManagementException {

        Set<String> consumerKeys = new HashSet<String>();

        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;

        String uuid;
        if (identifier.getUUID() != null) {
            uuid = identifier.getUUID();
        } else {
            uuid = getUUIDFromIdentifier(identifier, organization);
        }
        int apiId;
        String sqlQuery = SQLConstants.GET_CONSUMER_KEYS_SQL;

        try {

            connection = APIMgtDBUtil.getConnection();
            apiId = getAPIID(uuid, connection);

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
                String keyManagerName = rs.getString("NAME");
                String keyManagerOrganization = rs.getString("ORGANIZATION");
                // This is true when OAuth App has been created by pasting consumer key/secret in the screen.
                String mode = rs.getString("CREATE_MODE");
                if (consumerKey != null) {
                    deleteDomainApp.setString(1, consumerKey);
                    deleteDomainApp.addBatch();
                    KeyManager keyManager =
                            KeyManagerHolder.getKeyManagerInstance(keyManagerOrganization, keyManagerName);
                    if (keyManager != null) {
                        try {
                            keyManager.deleteMappedApplication(consumerKey);
                        } catch (APIManagementException e) {
                            log.error("Error while Deleting Client Application", e);
                        }
                    }
                    // OAuth app is deleted if only it has been created from API Store. For mapped clients we don't
                    // call delete.
                    if (!APIConstants.OAuthAppMode.MAPPED.name().equals(mode)) {
                        //delete on oAuthorization server.
                        if (log.isDebugEnabled()) {
                            log.debug("Deleting Oauth application with consumer key " + consumerKey + " from the " +
                                    "Oauth server");
                        }
                        if (keyManager != null) {
                            try {
                                keyManager.deleteApplication(consumerKey);
                            } catch (APIManagementException e) {
                                log.error("Error while Deleting Client Application", e);
                            }

                        }
                    }
                }
            }

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
     *
     * @param appId application id
     * @return Map<ConsumerKey, Pair<keyManagerName, keyManagerTenantDomain>
     * @throws APIManagementException
     */
    public Map<String, Pair<String, String>> getConsumerKeysForApplication(int appId) throws APIManagementException {

        Map<String, Pair<String, String>> consumerKeysOfApplication = new HashMap<>();
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement preparedStatement = connection
                     .prepareStatement(SQLConstants.GET_CONSUMER_KEY_OF_APPLICATION_SQL)) {
            preparedStatement.setInt(1, appId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String consumerKey = resultSet.getString("CONSUMER_KEY");
                    String keyManagerName = resultSet.getString("NAME");
                    String keyManagerOrganization = resultSet.getString("ORGANIZATION");
                    consumerKeysOfApplication.put(consumerKey, Pair.of(keyManagerName, keyManagerOrganization));
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
     *
     * @param applicationId
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
     * @param applicationId  APIM application ID.
     * @param tokenType      Token type (PRODUCTION || SANDBOX)
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
            ps.setString(3, keyManagerName);
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

    public void recordAPILifeCycleEvent(String uuid, String oldStatus, String newStatus, String userId,
                                        int tenantId) throws APIManagementException {

        try (Connection conn = APIMgtDBUtil.getConnection()) {
            int apiId = getAPIID(uuid, conn);
            conn.setAutoCommit(false);
            try {
                recordAPILifeCycleEvent(apiId, oldStatus, newStatus, userId, tenantId, conn);
                changeAPILifeCycleStatus(conn, apiId, newStatus);
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            handleException("Failed to record API state change", e);
        }
    }

    public void recordAPILifeCycleEvent(int apiId, String oldStatus, String newStatus, String userId,
                                        int tenantId) throws APIManagementException {

        try (Connection conn = APIMgtDBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                recordAPILifeCycleEvent(apiId, oldStatus, newStatus, userId, tenantId, conn);
                changeAPILifeCycleStatus(conn, apiId, newStatus);
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            handleException("Failed to record API state change", e);
        }
    }

    private void recordAPILifeCycleEvent(int apiId, String oldStatus, String newStatus, String userId,
                                         int tenantId, Connection conn) throws APIManagementException, SQLException {

        if (oldStatus == null && !newStatus.equals(APIConstants.CREATED)) {
            String msg = "Invalid old and new state combination";
            log.error(msg);
            throw new APIManagementException(msg);
        } else if (oldStatus != null && oldStatus.equals(newStatus)) {
            String msg = "No measurable differences in API state";
            log.error(msg);
            throw new APIManagementException(msg);
        }

        String sqlQuery = SQLConstants.ADD_API_LIFECYCLE_EVENT_SQL;

        try (PreparedStatement ps = conn.prepareStatement(sqlQuery)) {
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
            conn.commit();
        }
        // finally commit transaction
    }

    private void changeAPILifeCycleStatus(Connection connection, int apiId, String updatedStatus) throws SQLException {

        try (PreparedStatement preparedStatement = connection.prepareStatement(SQLConstants.UPDATE_API_STATUS)) {
            preparedStatement.setString(1, updatedStatus);
            preparedStatement.setInt(2, apiId);
            preparedStatement.executeUpdate();
            connection.commit();
        }
    }

    public void updateDefaultAPIPublishedVersion(APIIdentifier identifier)
            throws APIManagementException {

        try (Connection conn = APIMgtDBUtil.getConnection()) {
            try {
                conn.setAutoCommit(false);
                String defaultVersion = getDefaultVersion(conn, identifier);
                if (identifier.getVersion().equals(defaultVersion)) {
                    setPublishedDefVersion(identifier, conn, identifier.getVersion());
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }

        } catch (SQLException e) {
            handleException("Failed to update published default API state change", e);
        }
    }

    public List<LifeCycleEvent> getLifeCycleEvents(String uuid) throws APIManagementException {

        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        String sqlQuery = SQLConstants.GET_LIFECYCLE_EVENT_SQL;
        int apiOrApiProductId = getAPIID(uuid);

        List<LifeCycleEvent> events = new ArrayList<LifeCycleEvent>();
        try {
            connection = APIMgtDBUtil.getConnection();
            prepStmt = connection.prepareStatement(sqlQuery);
            prepStmt.setInt(1, apiOrApiProductId);
            rs = prepStmt.executeQuery();

            while (rs.next()) {
                LifeCycleEvent event = new LifeCycleEvent();
                String oldState = rs.getString("PREVIOUS_STATE");
                //event.setOldStatus(oldState != null ? APIStatus.valueOf(oldState) : null);
                event.setOldStatus(oldState);
                //event.setNewStatus(APIStatus.valueOf(rs.getString("NEW_STATE")));
                event.setNewStatus(rs.getString("NEW_STATE"));
                event.setUserId(rs.getString("USER_ID"));
                event.setDate(rs.getTimestamp("EVENT_DATE"));
                events.add(event);
            }

            Collections.sort(events, (o1, o2) -> o1.getDate().compareTo(o2.getDate()));
        } catch (SQLException e) {
            handleException("Error when executing the SQL : " + sqlQuery, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
        }
        return events;
    }

    public List<SubscribedAPI> makeKeysForwardCompatible(ApiTypeWrapper apiTypeWrapper, List<API> oldAPIVersions) throws APIManagementException {
        List<SubscribedAPI> subscribedAPISet = new ArrayList<>();
        //if there are no previous versions, there is no need to copy subscriptions
        if (oldAPIVersions == null || oldAPIVersions.isEmpty()) {
            return subscribedAPISet;
        }
        String getSubscriptionDataQuery = SQLConstants.GET_SUBSCRIPTION_DATA_SQL.replaceAll("_API_VERSION_LIST_",
                String.join(",", Collections.nCopies(oldAPIVersions.size(), "?")));
        APIIdentifier apiIdentifier = apiTypeWrapper.getApi().getId();
        try {
            // Retrieve all the existing subscription for the old version
            try (Connection connection = APIMgtDBUtil.getConnection()) {
                connection.setAutoCommit(false);
                try (PreparedStatement prepStmt = connection.prepareStatement(getSubscriptionDataQuery)) {
                    prepStmt.setString(1, APIUtil.replaceEmailDomainBack(apiIdentifier.getProviderName()));
                    prepStmt.setString(2, apiIdentifier.getApiName());
                    int index = 3;
                    for (API oldAPI : oldAPIVersions) {
                        prepStmt.setString(index++, oldAPI.getId().getVersion());
                    }
                    try (ResultSet rs = prepStmt.executeQuery()) {
                        List<SubscriptionInfo> subscriptionData = new ArrayList<SubscriptionInfo>();
                        while (rs.next() && !(APIConstants.SubscriptionStatus.ON_HOLD.equals(rs.getString("SUB_STATUS"
                        )))) {
                            int subscriptionId = rs.getInt("SUBSCRIPTION_ID");
                            String tierId = rs.getString("TIER_ID");
                            int applicationId = rs.getInt("APPLICATION_ID");
                            String apiVersion = rs.getString("VERSION");
                            String subscriptionStatus = rs.getString("SUB_STATUS");
                            SubscriptionInfo info = new SubscriptionInfo(subscriptionId, tierId, applicationId,
                                    apiVersion, subscriptionStatus);
                            subscriptionData.add(info);
                        }
                        // To keep track of already added subscriptions (apps)
                        List<Integer> addedApplications = new ArrayList<>();
                        for (int i = oldAPIVersions.size() - 1; i >= 0; i--) {
                            API oldAPI = oldAPIVersions.get(i);
                            for (SubscriptionInfo info : subscriptionData) {
                                try {
                                    if (info.getApiVersion().equals(oldAPI.getId().getVersion()) &&
                                            !addedApplications.contains(info.getApplicationId())) {
                                        String subscriptionStatus;
                                        if (APIConstants.SubscriptionStatus.BLOCKED.equalsIgnoreCase(info.getSubscriptionStatus())) {
                                            subscriptionStatus = APIConstants.SubscriptionStatus.BLOCKED;
                                        } else if (APIConstants.SubscriptionStatus.UNBLOCKED.equalsIgnoreCase(info.getSubscriptionStatus())) {
                                            subscriptionStatus = APIConstants.SubscriptionStatus.UNBLOCKED;
                                        } else if (APIConstants.SubscriptionStatus.PROD_ONLY_BLOCKED.equalsIgnoreCase(info.getSubscriptionStatus())) {
                                            subscriptionStatus = APIConstants.SubscriptionStatus.PROD_ONLY_BLOCKED;
                                        } else if (APIConstants.SubscriptionStatus.REJECTED.equalsIgnoreCase(info.getSubscriptionStatus())) {
                                            subscriptionStatus = APIConstants.SubscriptionStatus.REJECTED;
                                        } else {
                                            subscriptionStatus = APIConstants.SubscriptionStatus.ON_HOLD;
                                        }
                                        apiTypeWrapper.setTier(info.getTierId());
                                        Application application = getLightweightApplicationById(connection,
                                                info.getApplicationId());
                                        String subscriptionUUID = UUID.randomUUID().toString();
                                        int subscriptionId = addSubscription(connection, apiTypeWrapper, application,
                                                subscriptionStatus, apiIdentifier.getProviderName(), subscriptionUUID);
                                        if (subscriptionId == -1) {
                                            String msg =
                                                    "Unable to add a new subscription for the API: " + apiIdentifier.getName() +
                                                            ":v" + apiIdentifier.getVersion();
                                            log.error(msg);
                                            throw new APIManagementException(msg);
                                        }
                                        SubscribedAPI subscribedAPI = new SubscribedAPI(subscriptionUUID);
                                        subscribedAPI.setApplication(application);
                                        subscribedAPI.setTier(new Tier(info.getTierId()));
                                        subscribedAPI.setOrganization(apiTypeWrapper.getOrganization());
                                        subscribedAPI.setIdentifier(apiTypeWrapper);
                                        subscribedAPI.setSubStatus(subscriptionStatus);
                                        subscribedAPI.setSubscriptionId(subscriptionId);
                                        addedApplications.add(info.getApplicationId());
                                        subscribedAPISet.add(subscribedAPI);
                                    }
                                    // catching the exception because when copy the api without the option "require
                                    // re-subscription"
                                    // need to go forward rather throwing the exception
                                } catch (SubscriptionAlreadyExistingException e) {
                                    log.error("Error while adding subscription " + e.getMessage(), e);
                                } catch (SubscriptionBlockedException e) {
                                    log.info("Subscription is blocked: " + e.getMessage());
                                }
                            }
                        }
                    }
                    connection.commit();
                } catch (SQLException e) {
                    connection.rollback();
                    throw e;
                }
            }
        } catch (SQLException e) {
            handleException("Error when executing the SQL queries", e);
        }
        return subscribedAPISet;
    }

    private int addSubscription(Connection connection, ApiTypeWrapper apiTypeWrapper, Application application,
                                String subscriptionStatus, String subscriber) throws APIManagementException,
            SQLException {
        return addSubscription(connection, apiTypeWrapper, application, subscriptionStatus, subscriber, UUID.randomUUID().toString());
    }
    private int addSubscription(Connection connection, ApiTypeWrapper apiTypeWrapper, Application application,
                                String subscriptionStatus, String subscriber, String subscriptionUUID)
            throws APIManagementException, SQLException {

        final boolean isProduct = apiTypeWrapper.isAPIProduct();
        int subscriptionId = -1;
        int id = -1;
        String apiUUID;
        Identifier identifier;
        String tier;

        //Query to check if this subscription already exists
        String checkDuplicateQuery = SQLConstants.CHECK_EXISTING_SUBSCRIPTION_API_SQL;
        if (!isProduct) {
            identifier = apiTypeWrapper.getApi().getId();
            apiUUID = apiTypeWrapper.getApi().getUuid();
            if (apiUUID != null) {
                id = getAPIID(apiUUID);
            }
            if (id == -1){
                id = identifier.getId();
            }
        } else {
            identifier = apiTypeWrapper.getApiProduct().getId();
            id = apiTypeWrapper.getApiProduct().getProductId();
            apiUUID = apiTypeWrapper.getApiProduct().getUuid();
        }
        int tenantId = APIUtil.getTenantId(APIUtil.replaceEmailDomainBack(identifier.getProviderName()));

        try (PreparedStatement ps = connection.prepareStatement(checkDuplicateQuery)) {
            ps.setInt(1, id);
            ps.setInt(2, application.getId());

            try (ResultSet resultSet = ps.executeQuery()) {
                //If the subscription already exists
                if (resultSet.next()) {
                    String subStatus = resultSet.getString("SUB_STATUS");
                    String subCreationStatus = resultSet.getString("SUBS_CREATE_STATE");

                    if ((APIConstants.SubscriptionStatus.UNBLOCKED.equals(subStatus) ||
                            APIConstants.SubscriptionStatus.ON_HOLD.equals(subStatus) ||
                            APIConstants.SubscriptionStatus.REJECTED.equals(subStatus)) &&
                            APIConstants.SubscriptionCreatedStatus.SUBSCRIBE.equals(subCreationStatus)) {

                        //Throw error saying subscription already exists.
                        log.error(String.format("Subscription already exists for API/API Prouct %s in Application %s"
                                , apiTypeWrapper.getName(), application.getName()));
                        throw new SubscriptionAlreadyExistingException(String.format("Subscription already exists for" +
                                " API/API Prouct %s in Application %s", apiTypeWrapper.getName(), application.getName()));

                    } else if (APIConstants.SubscriptionStatus.UNBLOCKED.equals(subStatus) && APIConstants
                            .SubscriptionCreatedStatus.UN_SUBSCRIBE.equals(subCreationStatus)) {
                        deleteSubscriptionByApiIDAndAppID(id, application.getId(), connection);
                    } else if (APIConstants.SubscriptionStatus.BLOCKED.equals(subStatus) || APIConstants
                            .SubscriptionStatus.PROD_ONLY_BLOCKED.equals(subStatus)) {
                        log.error(String.format(String.format("Subscription to API/API Prouct %%s through application" +
                                " %%s was blocked"), apiTypeWrapper.getName(), application.getName()));
                        throw new SubscriptionBlockedException(String.format("Subscription to API/API Product %s " +
                                "through application %s was blocked", apiTypeWrapper.getName(), application.getName()));
                    } else if (APIConstants.SubscriptionStatus.REJECTED.equals(subStatus)) {
                        throw new SubscriptionBlockedException("Subscription to API " + apiTypeWrapper.getName()
                                + " through application " + application.getName() + " was rejected");
                    }
                }

            }
        }

        //This query to update the AM_SUBSCRIPTION table
        String sqlQuery = SQLConstants.ADD_SUBSCRIPTION_SQL;

        //Adding data to the AM_SUBSCRIPTION table
        //ps = conn.prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS);
        String subscriptionIDColumn = "SUBSCRIPTION_ID";
        if (connection.getMetaData().getDriverName().contains("PostgreSQL")) {
            subscriptionIDColumn = "subscription_id";
        }
            try (PreparedStatement preparedStForInsert = connection.prepareStatement(sqlQuery,
                    new String[]{subscriptionIDColumn})) {
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
                preparedStForInsert.setInt(3, application.getId());
                preparedStForInsert.setString(4, subscriptionStatus != null ? subscriptionStatus :
                        APIConstants.SubscriptionStatus.UNBLOCKED);
                preparedStForInsert.setString(5, APIConstants.SubscriptionCreatedStatus.SUBSCRIBE);
                preparedStForInsert.setString(6, subscriber);

                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                preparedStForInsert.setTimestamp(7, timestamp);
                preparedStForInsert.setTimestamp(8, timestamp);
                preparedStForInsert.setString(9, subscriptionUUID);

                preparedStForInsert.executeUpdate();
                try (ResultSet rs = preparedStForInsert.getGeneratedKeys()) {
                    while (rs.next()) {
                        //subscriptionId = rs.getInt(1);
                        subscriptionId = Integer.parseInt(rs.getString(1));
                    }
                }
            }

        return subscriptionId;
    }

    /**
     * Returns whether a given API Name already exists
     *
     * @param apiName      Name of the API
     * @param username     Username
     * @param organization Identifier of an Organization
     * @return true/false
     * @throws APIManagementException if failed to get API Names
     */
    public List<String> getAPIVersionsMatchingApiNameAndOrganization(String apiName, String username,
            String organization) throws APIManagementException {

        List<String> versionList = new ArrayList<String>();
        try (Connection connection = APIMgtDBUtil.getConnection();
                PreparedStatement ps = connection
                        .prepareStatement(SQLConstants.GET_VERSIONS_MATCHES_API_NAME_AND_ORGANIZATION_SQL)) {
            boolean initialAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            ps.setString(1, apiName);
            ps.setString(2, username);
            ps.setString(3, organization);
            try (ResultSet resultSet = ps.executeQuery()) {
                while (resultSet.next()) {
                    versionList.add(resultSet.getString("API_VERSION"));
                }
                connection.commit();
            } catch (SQLException e) {
                APIMgtDBUtil.rollbackConnection(connection,
                        "Failed to rollback get API versions matches API name " + apiName, e);
            } finally {
                APIMgtDBUtil.setAutoCommit(connection, initialAutoCommit);
            }
        } catch (SQLException e) {
            handleException("Failed to get API versions matches API name" + apiName, e);
        }
        return versionList;
    }

    /**
     * Returns whether a given API Context already exists
     *
     * @param contextTemplate Requested context template
     * @param organization    Identifier of an Organization
     * @return true/false
     * @throws APIManagementException if failed to get API Contexts
     */
    public boolean isDuplicateContextTemplateMatchesOrganization(String contextTemplate, String organization)
            throws APIManagementException {
        try (Connection connection = APIMgtDBUtil.getConnection();
                PreparedStatement ps = connection
                        .prepareStatement(SQLConstants.GET_CONTEXT_TEMPLATE_COUNT_SQL_MATCHES_ORGANIZATION)) {
            boolean initialAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            ps.setString(1, contextTemplate.toLowerCase());
            ps.setString(2, organization);
            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt("CTX_COUNT");
                    return count > 0;
                }
                connection.commit();
            } catch (SQLException e) {
                APIMgtDBUtil.rollbackConnection(connection,
                        "Failed to rollback in getting count matches context and organization", e);
            } finally {
                APIMgtDBUtil.setAutoCommit(connection, initialAutoCommit);
            }
        } catch (SQLException e) {
            handleException("Failed to count contexts which match " + contextTemplate + " for the organization : "
                    + organization, e);
        }
        return false;
    }

    /**
     * Add API metadata.
     *
     * @param api      API to add
     * @param tenantId tenant id
     * @param organization identifier of the organization
     * @return API Id of the successfully added API
     * @throws APIManagementException if fails to add API
     */
    public int addAPI(API api, int tenantId, String organization) throws APIManagementException {

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

            // For Choreo-Connect gateway, gateway vendor type in the DB will be "wso2/choreo-connect".
            // This value is determined considering the gateway type comes with the request.
            api.setGatewayVendor(APIUtil.setGatewayVendorBeforeInsertion(
                    api.getGatewayVendor(), api.getGatewayType()));

            prepStmt.setString(5, contextTemplate);
            prepStmt.setString(6, APIUtil.replaceEmailDomainBack(api.getId().getProviderName()));
            prepStmt.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
            prepStmt.setString(8, api.getApiLevelPolicy());
            prepStmt.setString(9, api.getType());
            prepStmt.setString(10, api.getUUID());
            prepStmt.setString(11, APIConstants.CREATED);
            prepStmt.setString(12, organization);
            prepStmt.setString(13, api.getGatewayVendor());
            prepStmt.setString(14, api.getVersionTimestamp());
            prepStmt.execute();

            rs = prepStmt.getGeneratedKeys();
            if (rs.next()) {
                apiId = rs.getInt(1);
            }

            connection.commit();

            String tenantUserName = MultitenantUtils
                    .getTenantAwareUsername(APIUtil.replaceEmailDomainBack(api.getId().getProviderName()));
            recordAPILifeCycleEvent(apiId, null, APIStatus.CREATED.toString(), tenantUserName, tenantId,
                    connection);
            //If the api is selected as default version, it is added/replaced into AM_API_DEFAULT_VERSION table
            if (api.isDefaultVersion()) {
                addUpdateAPIAsDefaultVersion(api, connection);
            }
            String serviceKey = api.getServiceInfo("key");
            if (StringUtils.isNotEmpty(serviceKey)) {
                addAPIServiceMapping(apiId, serviceKey, api.getServiceInfo("md5"), tenantId, connection);
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

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            return getDefaultVersion(connection, apiId);
        } catch (SQLException e) {
            handleException("Error while getting default version for " + apiId.getApiName(), e);
        }
        return null;
    }

    private String getDefaultVersion(Connection connection, APIIdentifier apiId) throws SQLException {

        String oldDefaultVersion = null;

        String query = SQLConstants.GET_DEFAULT_VERSION_SQL;
        try (PreparedStatement prepStmt = connection.prepareStatement(query)) {
            prepStmt.setString(1, apiId.getApiName());
            prepStmt.setString(2, APIUtil.replaceEmailDomainBack(apiId.getProviderName()));
            try (ResultSet rs = prepStmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("DEFAULT_API_VERSION");
                }
            }
        }
        return null;
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
                InputStream metadataBlob = rs.getBinaryStream("WF_METADATA");

                if (metadataBlob != null) {
                    String metadata = APIMgtDBUtil.getStringFromInputStream(metadataBlob);
                    Gson metadataGson = new Gson();
                    JSONObject metadataJson = metadataGson.fromJson(metadata, JSONObject.class);
                    workflowDTO.setMetadata(metadataJson);
                } else {
                    JSONObject metadataJson = new JSONObject();
                    workflowDTO.setMetadata(metadataJson);
                }
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
     * @param apiIdList
     * @param connection
     * @return
     * @throws APIManagementException
     */
    private void removeAPIFromDefaultVersion(List<APIIdentifier> apiIdList, Connection connection) throws
            APIManagementException {
        // TODO: check list empty
        try (PreparedStatement prepStmtDefVersionDelete =
                     connection.prepareStatement(SQLConstants.REMOVE_API_DEFAULT_VERSION_SQL)) {

            for (APIIdentifier apiId : apiIdList) {
                prepStmtDefVersionDelete.setString(1, apiId.getApiName());
                prepStmtDefVersionDelete.setString(2, APIUtil.
                        replaceEmailDomainBack(apiId.getProviderName()));
                prepStmtDefVersionDelete.addBatch();
            }
            prepStmtDefVersionDelete.executeBatch();
        } catch (SQLException e) {
                try {
                    connection.rollback();
                } catch (SQLException e1) {
                    log.error("Error while rolling back the failed operation", e1);
                }
            handleException("Error while deleting the API default version entry: " + apiIdList.stream().
                    map(APIIdentifier::getApiName).collect(Collectors.joining(",")) + " from the " +
                    "database", e);
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
        boolean deploymentAvailable = isDeploymentAvailableByAPIUUID(connection, api.getUuid());
        ArrayList<APIIdentifier> apiIdList = new ArrayList<APIIdentifier>() {{
            add(api.getId());
        }};
        removeAPIFromDefaultVersion(apiIdList, connection);

        PreparedStatement prepStmtDefVersionAdd = null;
        String queryDefaultVersionAdd = SQLConstants.ADD_API_DEFAULT_VERSION_SQL;
        try {
            prepStmtDefVersionAdd = connection.prepareStatement(queryDefaultVersionAdd);
            prepStmtDefVersionAdd.setString(1, api.getId().getApiName());
            prepStmtDefVersionAdd.setString(2, APIUtil.replaceEmailDomainBack(api.getId().getProviderName()));
            prepStmtDefVersionAdd.setString(3, api.getId().getVersion());

            if (deploymentAvailable) {
                prepStmtDefVersionAdd.setString(4, api.getId().getVersion());
                api.setAsPublishedDefaultVersion(true);
            } else {
                prepStmtDefVersionAdd.setString(4, publishedDefaultVersion);
            }
            prepStmtDefVersionAdd.setString(5, api.getOrganization());
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
    private void addURITemplates(int apiId, API api, int tenantId, Connection connection)
            throws SQLException, APIManagementException {

        String dbProductName = connection.getMetaData().getDatabaseProductName();
        String tenantDomain = APIUtil.getTenantDomainFromTenantId(tenantId);
        try (PreparedStatement uriMappingPrepStmt = connection.prepareStatement(SQLConstants.ADD_URL_MAPPING_SQL,
                new String[]{
                        DBUtils.getConvertedAutoGeneratedColumnName(dbProductName, "URL_MAPPING_ID")});
             PreparedStatement uriScopeMappingPrepStmt =
                     connection.prepareStatement(SQLConstants.ADD_API_RESOURCE_SCOPE_MAPPING);
             PreparedStatement operationPolicyMappingPrepStmt =
                     connection.prepareStatement(SQLConstants.OperationPolicyConstants.ADD_API_OPERATION_POLICY_MAPPING)) {
            Map<String, String> updatedPoliciesMap = new HashMap<>();
            Set<String> usedClonedPolicies = new HashSet<String>();
            List<ClonePolicyMetadataDTO> toBeClonedPolicyDetails = new ArrayList<>();
            String clonedPolicyId;
            for (URITemplate uriTemplate : api.getUriTemplates()) {
                uriMappingPrepStmt.setInt(1, apiId);
                uriMappingPrepStmt.setString(2, uriTemplate.getHTTPVerb());
                uriMappingPrepStmt.setString(3, uriTemplate.getAuthType());
                uriMappingPrepStmt.setString(4, uriTemplate.getUriTemplate());
                uriMappingPrepStmt.setString(5, uriTemplate.getThrottlingTier());
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

                    if (migrationEnabled == null) {
                        if (uriTemplate.getOperationPolicies() != null) {
                            for (OperationPolicy policy : uriTemplate.getOperationPolicies()) {
                                if (!updatedPoliciesMap.keySet().contains(policy.getPolicyId())) {
                                    //Check whether API Specific policies available
                                    OperationPolicyData existingPolicy =
                                            getAPISpecificOperationPolicyByPolicyID(connection, policy.getPolicyId(),
                                                    api.getUuid(), tenantDomain, false);
                                    clonedPolicyId = policy.getPolicyId();
                                    if (existingPolicy != null) {
                                        if (existingPolicy.isClonedPolicy()) {
                                            usedClonedPolicies.add(clonedPolicyId);
                                        }
                                    } else {
                                        // Even though the policy ID attached is not in the API specific policy list,
                                        // it can be a common policy and we need to verify that it has not been previously cloned
                                        // for the API before cloning again.
                                        clonedPolicyId = getClonedPolicyIdForCommonPolicyId(connection,
                                                policy.getPolicyId(), api.getUuid());
                                        if (clonedPolicyId == null) {
                                            clonedPolicyId = UUID.randomUUID().toString();
                                            ClonePolicyMetadataDTO toBeClonedSinglePolicyData = new ClonePolicyMetadataDTO();
                                            toBeClonedSinglePolicyData.setClonedPolicyUUID(clonedPolicyId);
                                            toBeClonedSinglePolicyData.setCurrentPolicyUUID(policy.getPolicyId());
                                            toBeClonedPolicyDetails.add(toBeClonedSinglePolicyData);
                                        }
                                        usedClonedPolicies.add(clonedPolicyId);
                                        //usedClonedPolicies set will not contain used API specific policies that are not cloned.
                                    }

                                    // Updated policies map will record the updated policy ID for the used policy ID.
                                    // If the policy has been cloned to the API specific policy list, we need to use the
                                    // updated policy Id.
                                    updatedPoliciesMap.put(policy.getPolicyId(), clonedPolicyId);
                                }

                                Gson gson = new Gson();
                                String paramJSON = gson.toJson(policy.getParameters());
                                if (log.isDebugEnabled()) {
                                    log.debug("Adding operation policy " + policy.getPolicyName() + " for API "
                                            + api.getId().getApiName() + " to URL mapping Id " + uriMappingId);
                                }

                                operationPolicyMappingPrepStmt.setInt(1, uriMappingId);
                                operationPolicyMappingPrepStmt.setString(2, updatedPoliciesMap.get(policy.getPolicyId()));
                                operationPolicyMappingPrepStmt.setString(3, policy.getDirection());
                                operationPolicyMappingPrepStmt.setString(4, paramJSON);
                                operationPolicyMappingPrepStmt.setInt(5, policy.getOrder());
                                operationPolicyMappingPrepStmt.addBatch();
                            }
                        }
                        uriTemplate.setId(uriMappingId);
                    }
                }
            } // end URITemplate list iteration
            uriScopeMappingPrepStmt.executeBatch();
            if (migrationEnabled == null) {
                for (ClonePolicyMetadataDTO toBeClonedPolicyData : toBeClonedPolicyDetails) {
                    cloneCommonPolicyToAPI(connection, toBeClonedPolicyData.getCurrentPolicyUUID(),
                            toBeClonedPolicyData.getClonedPolicyUUID(), api.getUuid());
                }
                operationPolicyMappingPrepStmt.executeBatch();
                cleanUnusedClonedOperationPolicies(connection, usedClonedPolicies, api.getUuid());
            }
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
                    + " AND SUB.USER_ID = ?)) AND " + "APP.APPLICATION_ID = ? AND SUB.SUBSCRIBER_ID = APP" +
                    ".SUBSCRIBER_ID";

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
                            + "OR (APP.APPLICATION_ID IN (SELECT APPLICATION_ID FROM AM_APPLICATION WHERE GROUP_ID = " +
                            "?))) "
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
                application.setLastUpdatedTime(String.valueOf(rs.getTimestamp("UPDATED_TIME").getTime()));
                application.setCreatedTime(String.valueOf(rs.getTimestamp("CREATED_TIME").getTime()));

                if (multiGroupAppSharingEnabled) {
                    setGroupIdInApplication(connection, application);
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

    private void setGroupIdInApplication(Connection connection, Application application) throws SQLException {

        String applicationGroupId = application.getGroupId();
        if (StringUtils.isEmpty(applicationGroupId)) { // No migrated App groupId
            application.setGroupId(getGroupId(connection, application.getId()));
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
                application.setOrganization(rs.getString("ORGANIZATION"));
                subscriber.setId(rs.getInt("SUBSCRIBER_ID"));
                application.setLastUpdatedTime(String.valueOf(rs.getTimestamp("UPDATED_TIME").getTime()));
                application.setCreatedTime(String.valueOf(rs.getTimestamp("CREATED_TIME").getTime()));

                String tenantDomain = MultitenantUtils.getTenantDomain(subscriberName);
                Map<String, Map<String, OAuthApplicationInfo>>
                        keyMap = getOAuthApplications(tenantDomain, application.getId());
                application.getKeyManagerWiseOAuthApp().putAll(keyMap);

                if (multiGroupAppSharingEnabled) {
                    if (application.getGroupId() == null || application.getGroupId().isEmpty()) {
                        application.setGroupId(getGroupId(connection, applicationId));
                    }
                }
            }
            if (application != null) {
                Map<String, String> applicationAttributes = getApplicationAttributes(connection, applicationId);
                application.setApplicationAttributes(applicationAttributes);
            }
        } catch (SQLException e) {
            handleException("Error while obtaining details of the Application : " + applicationId, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
        }
        return application;
    }

    private Application getLightweightApplicationById(Connection conn, int applicationId) throws SQLException {

        Application application = null;
        String query = SQLConstants.GET_APPLICATION_BY_ID_SQL;
        try (PreparedStatement prepStmt = conn.prepareStatement(query)) {
            prepStmt.setInt(1, applicationId);

            try (ResultSet rs = prepStmt.executeQuery()) {
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
                    if (rs.getTimestamp("CREATED_TIME") != null) {
                        application.setCreatedTime(String.valueOf(rs.getTimestamp("CREATED_TIME")
                                .getTime()));
                    }
                    if (rs.getTimestamp("UPDATED_TIME") != null) {
                        application.setLastUpdatedTime(String.valueOf(rs.getTimestamp("UPDATED_TIME")
                                .getTime()));
                    }

                    if (multiGroupAppSharingEnabled) {
                        if (application.getGroupId() == null || application.getGroupId().isEmpty()) {
                            application.setGroupId(getGroupId(conn, applicationId));
                        }
                    }
                }

            }
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
            String whereClauseWithGroupIdCaseInSensitive = "  AND  (APP.GROUP_ID = ? OR ((APP.GROUP_ID='' OR APP" +
                    ".GROUP_ID IS NULL)"
                    + " AND LOWER(SUB.USER_ID) = LOWER(?)))";

            String whereClauseWithMultiGroupId = "  AND  ((APP.APPLICATION_ID IN (SELECT APPLICATION_ID  FROM " +
                    "AM_APPLICATION_GROUP_MAPPING WHERE GROUP_ID IN ($params) AND TENANT = ?))  OR   SUB.USER_ID = ? )";
            String whereClauseWithMultiGroupIdCaseInSensitive = "  AND  ((APP.APPLICATION_ID IN (SELECT " +
                    "APPLICATION_ID  FROM " +
                    "AM_APPLICATION_GROUP_MAPPING WHERE GROUP_ID IN ($params) AND TENANT = ?))  OR   LOWER(SUB" +
                    ".USER_ID) = LOWER(?) )";

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
                application.setLastUpdatedTime(String.valueOf(rs.getTimestamp("UPDATED_TIME").getTime()));
                application.setCreatedTime(String.valueOf(rs.getTimestamp("CREATED_TIME").getTime()));

                String tenantDomain = MultitenantUtils.getTenantDomain(subscriberName);
                Map<String, Map<String, OAuthApplicationInfo>>
                        keyMap = getOAuthApplications(tenantDomain, application.getId());
                application.getKeyManagerWiseOAuthApp().putAll(keyMap);

                if (multiGroupAppSharingEnabled) {
                    if (application.getGroupId() == null || application.getGroupId().isEmpty()) {
                        application.setGroupId(getGroupId(connection, applicationId));
                    }
                }
            }

            if (application != null) {
                Map<String, String> applicationAttributes = getApplicationAttributes(connection, applicationId);
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
                application.setOrganization(rs.getString("ORGANIZATION"));
                subscriber.setId(rs.getInt("SUBSCRIBER_ID"));
                application.setLastUpdatedTime(String.valueOf(rs.getTimestamp("UPDATED_TIME").getTime()));
                application.setCreatedTime(String.valueOf(rs.getTimestamp("CREATED_TIME").getTime()));
                if (multiGroupAppSharingEnabled) {
                    if (application.getGroupId() == null || application.getGroupId().isEmpty()) {
                        application.setGroupId(getGroupId(connection, application.getId()));
                    }
                }

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
            apiId = getAPIID(api.getUuid(), connection);
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
     * @param uuid API uuid
     * @return Map of URI template ID to Scope Keys
     * @throws APIManagementException if an error occurs while getting resource to scope mapping of the API
     */
    public HashMap<Integer, Set<String>> getResourceToScopeMapping(String uuid)
            throws APIManagementException {

        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        HashMap<Integer, Set<String>> scopeToResourceMap = new HashMap<>();
        int apiId;
        try {
            String sqlQuery = SQLConstants.GET_RESOURCE_TO_SCOPE_MAPPING_SQL;
            apiId = getAPIID(uuid, conn);

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

        try (Connection connection = APIMgtDBUtil.getConnection()) {
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

        boolean isServiceInfoAvailable = false;
        int apiId = 0;
        int tenantID = -1234;
        String serviceKey = api.getServiceInfo("key");
        if (StringUtils.isNotEmpty(serviceKey)) {
            apiId = getAPIID(api.getUuid());
            tenantID = APIUtil.getTenantId(username);
            isServiceInfoAvailable = true;
        }

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

            // For Choreo-Connect gateway, gateway vendor type in the DB will be "wso2/choreo-connect".
            // This value is determined considering the gateway type comes with the request.
            api.setGatewayVendor(APIUtil.setGatewayVendorBeforeInsertion(
                    api.getGatewayVendor(), api.getGatewayType()));

            prepStmt.setString(2, api.getId().getApiName());
            prepStmt.setString(3, contextTemplate);
            prepStmt.setString(4, username);
            prepStmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            prepStmt.setString(6, api.getApiLevelPolicy());
            prepStmt.setString(7, api.getType());
            prepStmt.setString(8, api.getGatewayVendor());
            prepStmt.setString(9, api.getUuid());
            prepStmt.execute();

            if (api.isDefaultVersion() ^ api.getId().getVersion().equals(previousDefaultVersion)) { //A change has
                // happen
                //If the api is selected as default version, it is added/replaced into AM_API_DEFAULT_VERSION table
                if (api.isDefaultVersion()) {
                    addUpdateAPIAsDefaultVersion(api, connection);
                } else { //tick is removed
                    ArrayList<APIIdentifier> apiIdList = new ArrayList<APIIdentifier>() {{
                        add(api.getId());
                    }};

                    removeAPIFromDefaultVersion(apiIdList, connection);
                }
            }
            if (isServiceInfoAvailable) {
                updateAPIServiceMapping(apiId, serviceKey, api.getServiceInfo("md5"), tenantID, connection);
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

    public int getAPIID(String uuid) throws APIManagementException {
        int id = -1;
        try {
            try (Connection connection = APIMgtDBUtil.getConnection()) {
                return getAPIID(uuid, connection);
            }
        } catch (SQLException e) {
            handleException("Error while locating API with UUID : " + uuid + " from the database", e);
        }
        return id;
    }

    public int getAPIID(String uuid, Connection connection) throws APIManagementException, SQLException {

        int id = -1;
        String getAPIQuery = SQLConstants.GET_API_ID_SQL_BY_UUID;

        try (PreparedStatement prepStmt = connection.prepareStatement(getAPIQuery)) {
            prepStmt.setString(1, uuid);
            try (ResultSet rs = prepStmt.executeQuery()) {
                if (rs.next()) {
                    id = rs.getInt("API_ID");
                }
                if (id == -1) {
                    String msg = "Unable to find the API with UUID : " + uuid + " in the database";
                    log.error(msg);
                    throw new APIManagementException(msg);
                }
            }
        }
        return id;
    }

    /**
     * Get product Id from the product name and the provider.
     *
     * @param product product identifier
     * @throws APIManagementException exception
     */
    public void setAPIProductFromDB(APIProduct product)
            throws APIManagementException {

        APIProductIdentifier apiProductIdentifier = product.getId();
        String currentApiUuid;
        APIRevision apiRevision = ApiMgtDAO.getInstance().checkAPIUUIDIsARevisionUUID(product.getUuid());
        if (apiRevision != null && apiRevision.getApiUUID() != null) {
            currentApiUuid = apiRevision.getApiUUID();
        } else {
            currentApiUuid = product.getUuid();
        }

        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(SQLConstants.GET_API_PRODUCT_SQL)) {
            prepStmt.setString(1, currentApiUuid);
            try (ResultSet rs = prepStmt.executeQuery()) {
                if (rs.next()) {
                    product.setProductId(rs.getInt("API_ID"));
                    product.setProductLevelPolicy(rs.getString("API_TIER"));
                } else {
                    String msg = "Unable to find the API Product : " + apiProductIdentifier.getName() + "-" +
                            APIUtil.replaceEmailDomainBack(apiProductIdentifier.getProviderName()) + "-" +
                            apiProductIdentifier.getVersion() + " in the database";
                    throw new APIMgtResourceNotFoundException(msg);
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

    public void deleteAPI(String uuid) throws APIManagementException {

        Connection connection = null;
        PreparedStatement prepStmt = null;
        int id;
        String deleteLCEventQuery = SQLConstants.REMOVE_FROM_API_LIFECYCLE_SQL;
        String deleteAuditAPIMapping = SQLConstants.REMOVE_SECURITY_AUDIT_MAP_SQL;
        String deleteCommentQuery = SQLConstants.REMOVE_FROM_API_COMMENT_SQL;
        String deleteRatingsQuery = SQLConstants.REMOVE_FROM_API_RATING_SQL;
        String deleteSubscriptionQuery = SQLConstants.REMOVE_FROM_API_SUBSCRIPTION_SQL;
        String deleteExternalAPIStoresQuery = SQLConstants.REMOVE_FROM_EXTERNAL_STORES_SQL;
        String deleteAPIQuery = SQLConstants.REMOVE_FROM_API_SQL_BY_UUID;
        String deleteResourceScopeMappingsQuery = SQLConstants.REMOVE_RESOURCE_SCOPE_URL_MAPPING_SQL;
        String deleteURLTemplateQuery = SQLConstants.REMOVE_FROM_API_URL_MAPPINGS_SQL;
        String deleteGraphqlComplexityQuery = SQLConstants.REMOVE_FROM_GRAPHQL_COMPLEXITY_SQL;
        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            APIIdentifier identifier = ApiMgtDAO.getInstance().getAPIIdentifierFromUUID(uuid);
            id = getAPIID(uuid, connection);

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
            deleteAPIComments(id, uuid, connection);

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

            //Delete resource scope mappings of the API
            prepStmt = connection.prepareStatement(deleteResourceScopeMappingsQuery);
            prepStmt.setInt(1, id);
            prepStmt.execute();
            prepStmt.close();//If exception occurs at execute, this statement will close in finally else here

            // Delete URL Templates (delete the resource scope mappings on delete cascade)
            prepStmt = connection.prepareStatement(deleteURLTemplateQuery);
            prepStmt.setInt(1, id);
            prepStmt.execute();

            deleteAllAPISpecificOperationPoliciesByAPIUUID(connection, uuid, null);

            prepStmt = connection.prepareStatement(deleteAPIQuery);
            prepStmt.setString(1, uuid);
            prepStmt.execute();
            prepStmt.close();//If exception occurs at execute, this statement will close in finally else here

            String curDefaultVersion = getDefaultVersion(identifier);
            String pubDefaultVersion = getPublishedDefaultVersion(identifier);
            if (identifier.getVersion().equals(curDefaultVersion)) {
                ArrayList<APIIdentifier> apiIdList = new ArrayList<APIIdentifier>() {{
                    add(identifier);
                }};
                removeAPIFromDefaultVersion(apiIdList, connection);
            } else if (identifier.getVersion().equals(pubDefaultVersion)) {
                setPublishedDefVersion(identifier, connection, null);
            }

            connection.commit();
        } catch (SQLException e) {
            handleException("Error while removing the API with UUID: " + uuid + " from the database", e);
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

    public HashMap<String, String> getURITemplatesPerAPIAsString(String uuid)
            throws APIManagementException {

        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        int apiId;
        HashMap<String, String> urlMappings = new LinkedHashMap<String, String>();
        try {
            conn = APIMgtDBUtil.getConnection();
            apiId = getAPIID(uuid, conn);

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

    public Set<URITemplate> getURITemplatesOfAPI(String uuid)
            throws APIManagementException {

        String currentApiUuid;
        APIRevision apiRevision = checkAPIUUIDIsARevisionUUID(uuid);
        if (apiRevision != null && apiRevision.getApiUUID() != null) {
            currentApiUuid = apiRevision.getApiUUID();
        } else {
            currentApiUuid = uuid;
        }
        Map<Integer, URITemplate> uriTemplates = new LinkedHashMap<>();
        Map<Integer, Set<String>> scopeToURITemplateId = new HashMap<>();
        //Check If the API is a Revision
        if (apiRevision != null) {
            try (Connection conn = APIMgtDBUtil.getConnection();
                 PreparedStatement ps = conn.prepareStatement(SQLConstants.GET_URL_TEMPLATES_OF_API_REVISION_SQL)) {
                ps.setString(1, currentApiUuid);
                ps.setString(2, uuid);
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
                        uriTemplate.setId(uriTemplateId);
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
                        uriTemplate.setId(uriTemplateId);

                        InputStream mediationScriptBlob = rs.getBinaryStream("MEDIATION_SCRIPT");
                        if (mediationScriptBlob != null) {
                            String script = APIMgtDBUtil.getStringFromInputStream(mediationScriptBlob);
                            uriTemplate.setMediationScript(script);
                            uriTemplate.setMediationScripts(verb, script);
                        }

                        uriTemplates.put(uriTemplateId, uriTemplate);
                    }
                }

                setAssociatedAPIProducts(currentApiUuid, uriTemplates);
                if (migrationEnabled == null) {
                    setOperationPolicies(conn, apiRevision.getRevisionUUID(), uriTemplates);
                }
            } catch (SQLException e) {
                handleException("Failed to get URI Templates of API with UUID " + uuid, e);
            }
        } else {
            try (Connection conn = APIMgtDBUtil.getConnection();
                 PreparedStatement ps = conn.prepareStatement(SQLConstants.GET_URL_TEMPLATES_OF_API_SQL)) {
                ps.setString(1, currentApiUuid);
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
                        uriTemplate.setId(uriTemplateId);

                        InputStream mediationScriptBlob = rs.getBinaryStream("MEDIATION_SCRIPT");
                        if (mediationScriptBlob != null) {
                            String script = APIMgtDBUtil.getStringFromInputStream(mediationScriptBlob);
                            uriTemplate.setMediationScript(script);
                            uriTemplate.setMediationScripts(verb, script);
                        }

                        uriTemplates.put(uriTemplateId, uriTemplate);
                    }
                }

                setAssociatedAPIProducts(currentApiUuid, uriTemplates);
                if (migrationEnabled == null) {
                    setOperationPolicies(conn, currentApiUuid, uriTemplates);
                }
            } catch (SQLException e) {
                handleException("Failed to get URI Templates of API with UUID " + currentApiUuid, e);
            }
        }
        return new LinkedHashSet<>(uriTemplates.values());
    }

    public Map<Integer, URITemplate> getURITemplatesOfAPIWithProductMapping(String uuid) throws APIManagementException {

        Map<Integer, URITemplate> uriTemplates = new LinkedHashMap<>();
        Map<Integer, Set<String>> scopeToURITemplateId = new HashMap<>();
        try (Connection conn = APIMgtDBUtil.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(SQLConstants.GET_URL_TEMPLATES_OF_API_WITH_PRODUCT_MAPPINGS_SQL)) {
            ps.setString(1, uuid);
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

            setAssociatedAPIProductsURLMappings(uuid, uriTemplates);
        } catch (SQLException e) {
            handleException("Failed to get URI Templates of API with UUID " + uuid, e);
        }
        return uriTemplates;
    }

    private void setAssociatedAPIProducts(String uuid, Map<Integer, URITemplate> uriTemplates)
            throws SQLException {

        try (Connection conn = APIMgtDBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQLConstants.GET_API_PRODUCT_URI_TEMPLATE_ASSOCIATION_SQL)) {
            ps.setString(1, uuid);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String productName = rs.getString("API_NAME");
                    String productVersion = rs.getString("API_VERSION");
                    String productProvider = rs.getString("API_PROVIDER");
                    int uriTemplateId = rs.getInt("URL_MAPPING_ID");

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


    private void setAssociatedAPIProductsURLMappings(String uuid, Map<Integer, URITemplate> uriTemplates)
            throws SQLException {

        try (Connection conn = APIMgtDBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQLConstants.GET_ASSOCIATED_API_PRODUCT_URL_TEMPLATES_SQL)) {
            ps.setString(1, uuid);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String productName = rs.getString("API_NAME");
                    String productVersion = rs.getString("API_VERSION");
                    String productProvider = rs.getString("API_PROVIDER");
                    int uriTemplateId = rs.getInt("URL_MAPPING_ID");

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

    /**
     * Adds a comment for an API
     *
     * @param identifier  API Identifier
     * @param commentText Commented Text
     * @param user        User who did the comment
     * @return Comment ID
     * @deprecated This method needs to be removed once the Jaggery web apps are removed.
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

    /******************************
     * Adds a comment for an API
     *
     * @param uuid API uuid
     * @param comment Commented Text
     * @param user User who did the comment
     * @return Comment ID
     */
    public String addComment(String uuid, Comment comment, String user) throws APIManagementException {

        String commentId = null;
        try (Connection connection = APIMgtDBUtil.getConnection()) {
            int id = -1;
            connection.setAutoCommit(false);
            id = getAPIID(uuid, connection);
            if (id == -1) {
                String msg = "Could not load API record for API with UUID: " + uuid;
                throw new APIManagementException(msg);
            }
            String addCommentQuery = SQLConstants.ADD_COMMENT_SQL;
            commentId = UUID.randomUUID().toString();
            try (PreparedStatement insertPrepStmt = connection.prepareStatement(addCommentQuery)) {
                insertPrepStmt.setString(1, commentId);
                insertPrepStmt.setString(2, comment.getText());
                insertPrepStmt.setString(3, user);
                insertPrepStmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()), Calendar.getInstance());
                insertPrepStmt.setInt(5, id);
                insertPrepStmt.setString(6, comment.getParentCommentID());
                insertPrepStmt.setString(7, comment.getEntryPoint());
                insertPrepStmt.setString(8, comment.getCategory());
                insertPrepStmt.executeUpdate();
                connection.commit();
            }
        } catch (SQLException e) {
            handleException("Failed to add comment data, for API with UUID " + uuid,
                    e);
        }
        return commentId;
    }

    /**************************
     * Returns a specific comment of an API
     *
     * @param commentId  Comment ID
     * @param apiTypeWrapper Api Type Wrapper
     * @return Comment Array
     * @throws APIManagementException
     */
    public Comment getComment(ApiTypeWrapper apiTypeWrapper, String commentId, Integer replyLimit,
                              Integer replyOffset) throws
            APIManagementException {

        String uuid;
        Identifier identifier;
        if (apiTypeWrapper.isAPIProduct()) {
            identifier = apiTypeWrapper.getApiProduct().getId();
            uuid = apiTypeWrapper.getApiProduct().getUuid();
        } else {
            identifier = apiTypeWrapper.getApi().getId();
            uuid = apiTypeWrapper.getApi().getUuid();
        }

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            Comment comment = new Comment();
            int id = -1;
            String getCommentQuery = SQLConstants.GET_COMMENT_SQL;
            id = getAPIID(uuid, connection);
            if (id == -1) {
                String msg = "Could not load API record for: " + identifier.getName();
                throw new APIManagementException(msg);
            }
            try (PreparedStatement prepStmt = connection.prepareStatement(getCommentQuery)) {
                prepStmt.setString(1, uuid);
                prepStmt.setString(2, commentId);
                try (ResultSet resultSet = prepStmt.executeQuery()) {
                    if (resultSet.next()) {
                        comment.setId(resultSet.getString("COMMENT_ID"));
                        comment.setText(resultSet.getString("COMMENT_TEXT"));
                        comment.setUser(resultSet.getString("CREATED_BY"));
                        comment.setCreatedTime(resultSet.getTimestamp("CREATED_TIME"));
                        comment.setUpdatedTime(resultSet.getTimestamp("UPDATED_TIME"));
                        comment.setApiId(resultSet.getString("API_ID"));
                        comment.setParentCommentID(resultSet.getString("PARENT_COMMENT_ID"));
                        comment.setEntryPoint(resultSet.getString("ENTRY_POINT"));
                        comment.setCategory(resultSet.getString("CATEGORY"));
                        comment.setReplies(getComments(uuid, commentId, replyLimit, replyOffset, connection));
                        return comment;
                    }
                }
            }
        } catch (SQLException e) {
            handleException("Failed to retrieve comment for API " + identifier.getName() + "with comment ID " +
                    commentId, e);
        }
        return null;
    }

    /****************************************
     * Returns all the Comments on an API
     *
     * @param apiTypeWrapper API type Wrapper
     * @param parentCommentID Parent Comment ID
     * @return Comment Array
     * @throws APIManagementException
     */
    public CommentList getComments(ApiTypeWrapper apiTypeWrapper, String parentCommentID, Integer limit,
     Integer offset) throws APIManagementException {

        CommentList commentList = null;
        try (Connection connection = APIMgtDBUtil.getConnection()) {
            int id = -1;
            String uuid;
            Identifier identifier;
            String currentApiUuid;
            if (apiTypeWrapper.isAPIProduct()) {
                identifier = apiTypeWrapper.getApiProduct().getId();
                uuid = apiTypeWrapper.getApiProduct().getUuid();
                APIRevision apiRevision = checkAPIUUIDIsARevisionUUID(uuid);
                if (apiRevision != null && apiRevision.getApiUUID() != null) {
                    currentApiUuid = apiRevision.getApiUUID();
                } else {
                    currentApiUuid = uuid;
                }
            } else {
                identifier = apiTypeWrapper.getApi().getId();
                uuid = apiTypeWrapper.getApi().getUuid();
                APIRevision apiRevision = checkAPIUUIDIsARevisionUUID(uuid);
                if (apiRevision != null && apiRevision.getApiUUID() != null) {
                    currentApiUuid = apiRevision.getApiUUID();
                } else {
                    currentApiUuid = uuid;
                }
            }
            id = getAPIID(currentApiUuid, connection);
            if (id == -1) {
                String msg = "Could not load API record for: " + identifier.getName();
                throw new APIManagementException(msg);
            }
            commentList = getComments(currentApiUuid, parentCommentID, limit, offset, connection);
        } catch (SQLException e) {
            handleException("Failed to retrieve comments for  " + apiTypeWrapper.getName(), e);
        }
        return commentList;
    }

    /****************************************
     * Returns all the Comments on an API
     *
     * @param uuid API UUID
     * @param parentCommentID Parent Comment ID
     * @param limit           The limit
     * @param offset          The offset
     * @param connection Database connection
     * @return Comment Array
     * @throws APIManagementException
     */
    private CommentList getComments(String uuid, String parentCommentID, Integer limit, Integer offset,
     Connection connection) throws
            APIManagementException {

        List<Comment> list = new ArrayList<Comment>();
        CommentList commentList = new CommentList();
        Pagination pagination = new Pagination();
        commentList.setPagination(pagination);
        int total = 0;
        String sqlQuery;
        String sqlQueryForCount;
        if (parentCommentID == null) {
            sqlQueryForCount = SQLConstants.GET_ROOT_COMMENTS_COUNT_SQL;
        } else {
            sqlQueryForCount = SQLConstants.GET_REPLIES_COUNT_SQL;
        }
        try (PreparedStatement prepStmtForCount = connection.prepareStatement(sqlQueryForCount)) {
            prepStmtForCount.setString(1, uuid);
            if (parentCommentID != null) {
                prepStmtForCount.setString(2, parentCommentID);
            }
            try (ResultSet resultSetForCount = prepStmtForCount.executeQuery()) {
                while (resultSetForCount.next()) {
                    total = resultSetForCount.getInt("COMMENT_COUNT");
                }
                if (total > 0 && limit > 0) {
                    if (parentCommentID == null) {
                        sqlQuery = SQLConstantManagerFactory.getSQlString("GET_ROOT_COMMENTS_SQL");
                    } else {
                        sqlQuery = SQLConstantManagerFactory.getSQlString("GET_REPLIES_SQL");
                    }
                    try (PreparedStatement prepStmt = connection.prepareStatement(sqlQuery)) {
                        prepStmt.setString(1, uuid);
                        if (parentCommentID != null) {
                            prepStmt.setString(2, parentCommentID);
                            prepStmt.setInt(3, offset);
                            prepStmt.setInt(4, limit);
                        } else {
                            prepStmt.setInt(2, offset);
                            prepStmt.setInt(3, limit);
                        }
                        try (ResultSet resultSet = prepStmt.executeQuery()) {
                            while (resultSet.next()) {
                                Comment comment = new Comment();
                                comment.setId(resultSet.getString("COMMENT_ID"));
                                comment.setText(resultSet.getString("COMMENT_TEXT"));
                                comment.setUser(resultSet.getString("CREATED_BY"));
                                comment.setCreatedTime(resultSet.getTimestamp("CREATED_TIME"));
                                comment.setUpdatedTime(resultSet.getTimestamp("UPDATED_TIME"));
                                comment.setApiId(resultSet.getString("API_ID"));
                                comment.setParentCommentID(resultSet.getString("PARENT_COMMENT_ID"));
                                comment.setEntryPoint(resultSet.getString("ENTRY_POINT"));
                                comment.setCategory(resultSet.getString("CATEGORY"));
                                if (parentCommentID == null) {
                                    comment.setReplies(getComments(uuid, resultSet.getString("COMMENT_ID")
                                            , APIConstants.REPLYLIMIT, APIConstants.REPLYOFFSET, connection));
                                } else {
                                    CommentList emptyCommentList = new CommentList();
                                    Pagination emptyPagination = new Pagination();
                                    emptyCommentList.setPagination(emptyPagination);
                                    emptyCommentList.getPagination().setTotal(0);
                                    emptyCommentList.setCount(0);
                                    comment.setReplies(emptyCommentList);
                                }
                                list.add(comment);
                            }
                        }
                    }
                } else {
                    commentList.getPagination().setTotal(total);
                    commentList.setCount(total);
                    return commentList;
                }
            }
        } catch (SQLException e) {
            handleException("Failed to retrieve comments for API with UUID " + uuid, e);
        }
        pagination.setLimit(limit);
        pagination.setOffset(offset);
        commentList.getPagination().setTotal(total);
        commentList.setList(list);
        commentList.setCount(list.size());
        return commentList;
    }

    /**
     * Returns all the Comments on an API
     *
     * @param uuid      API uuid
     * @param parentCommentID Parent Comment ID
     * @return Comment Array
     * @throws APIManagementException
     */
    public Comment[] getComments(String uuid, String parentCommentID) throws APIManagementException {

        List<Comment> commentList = new ArrayList<Comment>();
        Connection connection = null;
        ResultSet resultSet = null;
        PreparedStatement prepStmt = null;
        int id = -1;
        String sqlQuery;
        if (parentCommentID == null) {
            sqlQuery = SQLConstantManagerFactory.getSQlString("GET_ROOT_COMMENTS_SQL");
        } else {
            sqlQuery = SQLConstantManagerFactory.getSQlString("GET_REPLIES_SQL");
        }
        try {
            connection = APIMgtDBUtil.getConnection();
            id = getAPIID(uuid, connection);
            if (id == -1) {
                String msg = "Could not load API record for API with UUID: " + uuid;
                throw new APIManagementException(msg);
            }
            prepStmt = connection.prepareStatement(sqlQuery);
            prepStmt.setString(1, uuid);
            if (parentCommentID != null) {
                prepStmt.setString(2, parentCommentID);
            }
            resultSet = prepStmt.executeQuery();
            while (resultSet.next()) {
                Comment comment = new Comment();
                comment.setId(resultSet.getString("COMMENT_ID"));
                comment.setText(resultSet.getString("COMMENT_TEXT"));
                comment.setUser(resultSet.getString("CREATED_BY"));
                comment.setCreatedTime(resultSet.getTimestamp("CREATED_TIME"));
                comment.setUpdatedTime(resultSet.getTimestamp("UPDATED_TIME"));
                comment.setApiId(resultSet.getString("API_ID"));
                comment.setParentCommentID(resultSet.getString("PARENT_COMMENT_ID"));
                comment.setEntryPoint(resultSet.getString("ENTRY_POINT"));
                comment.setCategory(resultSet.getString("CATEGORY"));
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
            handleException("Failed to retrieve comments for API with UUID " + uuid, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, resultSet);
        }
        return commentList.toArray(new Comment[commentList.size()]);
    }

    /***********
     * Edit a comment
     *
     * @param apiTypeWrapper API Type Wrapper
     * @param commentId Comment ID
     * @param comment Comment object
     * @throws APIManagementException
     */
    public boolean editComment(ApiTypeWrapper apiTypeWrapper, String commentId, Comment comment) throws
            APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            int id = -1;
            String editCommentQuery = SQLConstants.EDIT_COMMENT;
            Identifier identifier;
            String uuid;
            if (apiTypeWrapper.isAPIProduct()) {
                identifier = apiTypeWrapper.getApiProduct().getId();
                uuid = apiTypeWrapper.getApiProduct().getUuid();
            } else {
                identifier = apiTypeWrapper.getApi().getId();
                uuid = apiTypeWrapper.getApi().getUuid();
            }
            id = getAPIID(uuid, connection);
            if (id == -1) {
                String msg = "Could not load API record for: " + identifier.getName();
                throw new APIManagementException(msg);
            }
            connection.setAutoCommit(false);
            try (PreparedStatement prepStmt = connection.prepareStatement(editCommentQuery)) {
                prepStmt.setString(1, comment.getText());
                prepStmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()), Calendar.getInstance());
                prepStmt.setString(3, comment.getCategory());
                prepStmt.setInt(4, id);
                prepStmt.setString(5, commentId);
                prepStmt.execute();
                connection.commit();
                return true;
            }
        } catch (SQLException e) {
            handleException("Error while editing comment " + commentId + " from the database", e);
        }
        return false;
    }

    /**************************************
     * Delete a comment
     *
     * @param apiTypeWrapper API Type Wrapper
     * @param commentId Comment ID
     * @throws APIManagementException
     */
    public boolean deleteComment(ApiTypeWrapper apiTypeWrapper, String commentId) throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            String uuid;
            if (apiTypeWrapper.isAPIProduct()) {
                uuid = apiTypeWrapper.getApiProduct().getUuid();
            } else {
                uuid = apiTypeWrapper.getApi().getUuid();
            }
            return deleteComment(uuid, commentId, connection);
        } catch (SQLException e) {
            handleException("Error while deleting comment " + commentId + " from the database", e);
        }
        return false;
    }

    private boolean deleteComment(String uuid, String commentId, Connection connection) throws
            APIManagementException {

        int id = -1;
        String deleteCommentQuery = SQLConstants.DELETE_COMMENT_SQL;
        String getCommentIDsOfReplies = SQLConstants.GET_IDS_OF_REPLIES_SQL;
        ResultSet resultSet = null;
        try {
            id = getAPIID(uuid, connection);
            if (id == -1) {
                String msg = "Could not load API record for API with UUID: " + uuid;
                throw new APIManagementException(msg, ExceptionCodes.from(ExceptionCodes.API_NOT_FOUND, uuid));
            }
            connection.setAutoCommit(false);
            try (PreparedStatement prepStmtGetReplies = connection.prepareStatement(getCommentIDsOfReplies)) {
                prepStmtGetReplies.setString(1, uuid);
                prepStmtGetReplies.setString(2, commentId);
                resultSet = prepStmtGetReplies.executeQuery();
                while (resultSet.next()) {
                    deleteComment(uuid, resultSet.getString("COMMENT_ID"), connection);
                }
                try (PreparedStatement prepStmt = connection.prepareStatement(deleteCommentQuery)) {
                    prepStmt.setInt(1, id);
                    prepStmt.setString(2, commentId);
                    prepStmt.execute();
                    connection.commit();
                    return true;
                }
            }
        } catch (SQLException e) {
            handleException("Error while deleting comment " + commentId + " from the database", e);
        }
        return false;
    }

    private void deleteAPIComments(int apiId, String uuid, Connection connection) throws APIManagementException {
        try {
            connection.setAutoCommit(false);
            String deleteChildComments = SQLConstants.DELETE_API_CHILD_COMMENTS;
            String deleteParentComments = SQLConstants.DELETE_API_PARENT_COMMENTS;
            try (PreparedStatement childCommentPreparedStmt = connection.prepareStatement(deleteChildComments);
                    PreparedStatement parentCommentPreparedStmt = connection.prepareStatement(deleteParentComments)) {
                childCommentPreparedStmt.setInt(1, apiId);
                childCommentPreparedStmt.execute();

                parentCommentPreparedStmt.setInt(1, apiId);
                parentCommentPreparedStmt.execute();
            }
        } catch (SQLException e) {
            handleException("Error while deleting comments for API " + uuid, e);
        }
    }

    /**
     * Delete a comment
     *
     * @param uuid API uuid
     * @param commentId  Comment ID
     * @throws APIManagementException
     */
    public void deleteComment(String uuid, String commentId) throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            int id = -1;
            id = getAPIID(uuid, connection);
            if (id == -1) {
                String msg = "Could not load API record for API with UUID: " + uuid;
                throw new APIManagementException(msg);
            }
            String deleteCommentQuery = SQLConstants.DELETE_COMMENT_SQL;
            connection.setAutoCommit(false);
            try (PreparedStatement prepStmt = connection.prepareStatement(deleteCommentQuery)) {
                prepStmt.setInt(1, id);
                prepStmt.setString(2, commentId);
                prepStmt.execute();
                connection.commit();
            }
        } catch (SQLException e) {
            handleException("Error while deleting comment " + commentId + " from the database", e);
        }
    }

    public boolean isContextExist(String context, String organization) {

        Connection connection = null;
        ResultSet resultSet = null;
        PreparedStatement prepStmt = null;

        String sql = SQLConstants.GET_API_CONTEXT_SQL;
        try {
            connection = APIMgtDBUtil.getConnection();
            prepStmt = connection.prepareStatement(sql);
            prepStmt.setString(1, context);
            prepStmt.setString(2, organization);
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
     * @param uuid API uuid
     * @return API Context
     * @throws APIManagementException if an error occurs
     */
    public String getAPIContext(String uuid) throws APIManagementException {

        String context = null;
        try (Connection connection = APIMgtDBUtil.getConnection()) {
            context = getAPIContext(uuid, connection);
        } catch (SQLException e) {
            log.error("Failed to retrieve the API Context", e);
            handleException("Failed to retrieve connection while getting the API Context for API with UUID " + uuid, e);
        }
        return context;
    }

    /**
     * Get API Context by passing an existing DB connection.
     *
     * @param uuid API uuid
     * @param connection DB Connection
     * @return API Context
     * @throws APIManagementException if an error occurs
     */
    public String getAPIContext(String uuid, Connection connection) throws APIManagementException {

        String context = null;
        String sql = SQLConstants.GET_API_CONTEXT_BY_API_UUID_SQL;
        try (PreparedStatement prepStmt = connection.prepareStatement(sql)) {
            prepStmt.setString(1, uuid);
            try (ResultSet resultSet = prepStmt.executeQuery()) {
                while (resultSet.next()) {
                    context = resultSet.getString(1);
                }
            }
        } catch (SQLException e) {
            log.error("Failed to retrieve the API Context", e);
            handleException("Failed to retrieve the API Context for API with UUID " + uuid, e);
        }
        return context;
    }

    /**
     * Get API Identifier by the the API's UUID.
     *
     * @param uuid uuid of the API
     * @return API Identifier
     * @throws APIManagementException if an error occurs
     */
    public APIIdentifier getAPIIdentifierFromUUID(String uuid) throws APIManagementException {

        APIIdentifier identifier = null;
        String sql = SQLConstants.GET_API_IDENTIFIER_BY_UUID_SQL;
        try (Connection connection = APIMgtDBUtil.getConnection()) {
            PreparedStatement prepStmt = connection.prepareStatement(sql);
            prepStmt.setString(1, uuid);
            try (ResultSet resultSet = prepStmt.executeQuery()) {
                while (resultSet.next()) {
                    String provider = resultSet.getString(1);
                    String name = resultSet.getString(2);
                    String version = resultSet.getString(3);
                    identifier = new APIIdentifier(APIUtil.replaceEmailDomain(provider), name, version, uuid);
                }
            }
        } catch (SQLException e) {
            handleException("Failed to retrieve the API Identifier details for UUID : " + uuid, e);
        }
        return identifier;
    }

    /**
     * Get API Product Identifier by the product's UUID.
     *
     * @param uuid uuid of the API
     * @return API Identifier
     * @throws APIManagementException if an error occurs
     */
    public APIProductIdentifier getAPIProductIdentifierFromUUID(String uuid) throws APIManagementException {

        APIProductIdentifier identifier = null;
        String sql = SQLConstants.GET_API_IDENTIFIER_BY_UUID_SQL;
        try (Connection connection = APIMgtDBUtil.getConnection()) {
            PreparedStatement prepStmt = connection.prepareStatement(sql);
            prepStmt.setString(1, uuid);
            try (ResultSet resultSet = prepStmt.executeQuery()) {
                while (resultSet.next()) {
                    String provider = resultSet.getString(1);
                    String name = resultSet.getString(2);
                    String version = resultSet.getString(3);
                    identifier = new APIProductIdentifier(APIUtil.replaceEmailDomain(provider), name, version, uuid);
                }
            }
        } catch (SQLException e) {
            handleException("Failed to retrieve the API Product Identifier details for UUID : " + uuid, e);
        }
        return identifier;
    }

    /**
     * @param apiId UUID of the API
     * @return organization of the API
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     */
    public String getOrganizationByAPIUUID(String apiId) throws APIManagementException {
        String organization = null;
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(SQLConstants.GET_ORGANIZATION_BY_API_ID)) {
            boolean initialAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            ps.setString(1, apiId);
            try (ResultSet result = ps.executeQuery()){
                while (result.next()) {
                    organization = result.getString("ORGANIZATION");
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                APIMgtDBUtil.rollbackConnection(connection, "Failed to rollback while fetching organization", e);
            } finally {
                APIMgtDBUtil.setAutoCommit(connection, initialAutoCommit);
            }
        } catch (SQLException e) {
            handleException("Error occurred while fetching organization", e);
        }
        return organization;
    }

    /**
     * Retrieve the gateway vendor of an API by providing the UUID
     *
     * @param apiId UUID of the API
     * @return gatewayVendor of the API
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     */
    public String getGatewayVendorByAPIUUID(String apiId) throws APIManagementException {
        String gatewayVendor = null;
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(SQLConstants.GET_GATEWAY_VENDOR_BY_API_ID)) {
            ResultSet result = null;
            try {
                connection.setAutoCommit(false);
                ps.setString(1, apiId);
                result = ps.executeQuery();

                while (result.next()) {
                    gatewayVendor = result.getString("GATEWAY_VENDOR");
                }
                connection.commit();
            } catch (SQLException e) {
                APIMgtDBUtil.rollbackConnection(connection, "Failed to rollback while fetching gateway vendor" +
                        " of the API", e);
            }
        } catch (SQLException e) {
            handleException("Error occurred while fetching gateway vendor of the API with ID " + apiId, e);
        }
        gatewayVendor = APIUtil.handleGatewayVendorRetrieval(gatewayVendor);
        return gatewayVendor;
    }

    public String getUUIDFromIdentifier(Identifier apiIdentifier, String organization) throws APIManagementException {
        if (apiIdentifier instanceof APIProductIdentifier) {
            return getUUIDFromIdentifier((APIProductIdentifier) apiIdentifier, organization);
        } else {
            if (organization != null) {
                return getUUIDFromIdentifier((APIIdentifier) apiIdentifier, organization);
            } else {
                return getUUIDFromIdentifier((APIIdentifier) apiIdentifier);
            }
        }
    }

    /**
     * Get API UUID by the API Identifier.
     *
     * @param identifier API Identifier
     * @return String UUID
     * @throws APIManagementException if an error occurs
     */
    public String getUUIDFromIdentifier(APIIdentifier identifier) throws APIManagementException {

        String uuid = null;
        String sql = SQLConstants.GET_UUID_BY_IDENTIFIER_SQL;
        try (Connection connection = APIMgtDBUtil.getConnection();
                PreparedStatement prepStmt = connection.prepareStatement(sql)) {
            prepStmt.setString(1, APIUtil.replaceEmailDomainBack(identifier.getProviderName()));
            prepStmt.setString(2, identifier.getApiName());
            prepStmt.setString(3, identifier.getVersion());
            try (ResultSet resultSet = prepStmt.executeQuery()) {
                while (resultSet.next()) {
                    uuid = resultSet.getString(1);
                }
            }
        } catch (SQLException e) {
            handleException(
                    "Failed to get the UUID for API : " + identifier.getApiName() + '-' + identifier.getVersion(), e);
        }
        return uuid;
    }

    /**
     * Get API Product UUID by the API Product Identifier.
     *
     * @param identifier API Product Identifier
     * @return String UUID
     * @throws APIManagementException if an error occurs
     */
    public String getUUIDFromIdentifier(APIProductIdentifier identifier) throws APIManagementException {

        String uuid = null;
        String sql = SQLConstants.GET_UUID_BY_IDENTIFIER_SQL;
        try (Connection connection = APIMgtDBUtil.getConnection()) {
            PreparedStatement prepStmt = connection.prepareStatement(sql);
            prepStmt.setString(1, APIUtil.replaceEmailDomainBack(identifier.getProviderName()));
            prepStmt.setString(2, identifier.getName());
            prepStmt.setString(3, identifier.getVersion());
            try (ResultSet resultSet = prepStmt.executeQuery()) {
                while (resultSet.next()) {
                    uuid = resultSet.getString(1);
                }
            }
        } catch (SQLException e) {
            handleException("Failed to retrieve the UUID for the API Product : " + identifier.getName() + '-'
                    + identifier.getVersion(), e);
        }
        return uuid;
    }

    /**
     * Get API UUID by the API Identifier.
     *
     * @param identifier API Identifier
     * @param organization identifier of the organization
     * @return String UUID
     * @throws APIManagementException if an error occurs
     */
    public String getUUIDFromIdentifier(APIIdentifier identifier, String organization) throws APIManagementException {

        String uuid = null;
        String sql = SQLConstants.GET_UUID_BY_IDENTIFIER_AND_ORGANIZATION_SQL;
        try (Connection connection = APIMgtDBUtil.getConnection();
                PreparedStatement prepStmt = connection.prepareStatement(sql)) {
            prepStmt.setString(1, identifier.getApiName());
            prepStmt.setString(2, identifier.getVersion());
            prepStmt.setString(3, organization);
            try (ResultSet resultSet = prepStmt.executeQuery()) {
                while (resultSet.next()) {
                    uuid = resultSet.getString(1);
                }
            }
        } catch (SQLException e) {
            handleException(
                    "Failed to get the UUID for API : " + identifier.getApiName() + '-' + identifier.getVersion(), e);
        }
        return uuid;
    }

    /**
     * Get API UUID by passed parameters.
     *
     * @param provider Provider of the API
     * @param apiName  Name of the API
     * @param version  Version of the API
     * @param organization identifier of the organization
     * @return String UUID
     * @throws APIManagementException if an error occurs
     */
    public String getUUIDFromIdentifier(String provider, String apiName, String version, String organization)
            throws APIManagementException {

        String uuid = null;
        String sql = SQLConstants.GET_UUID_BY_IDENTIFIER_AND_ORGANIZATION_SQL;
        try (Connection connection = APIMgtDBUtil.getConnection();
                PreparedStatement prepStmt = connection.prepareStatement(sql)) {
            prepStmt.setString(1, apiName);
            prepStmt.setString(2, version);
            prepStmt.setString(3, organization);
            try (ResultSet resultSet = prepStmt.executeQuery()) {
                while (resultSet.next()) {
                    uuid = resultSet.getString(1);
                }
            }
        } catch (SQLException e) {
            handleException("Failed to get the UUID for API : ", e);
        }
        return uuid;
    }


    /**
     * Get API Product UUID by the API Product Identifier and organization.
     *
     * @param identifier API Product Identifier
     * @param organization
     * @return String UUID
     * @throws APIManagementException if an error occurs
     */
    public String getUUIDFromIdentifier(APIProductIdentifier identifier, String organization)
            throws APIManagementException {
        if (organization != null) {
            return getUUIDFromIdentifier(identifier, organization, null);
        } else {
            String apiTenantDomain = MultitenantUtils.getTenantDomain(
                    APIUtil.replaceEmailDomainBack(identifier.getProviderName()));
            return getUUIDFromIdentifier(identifier, apiTenantDomain, null);
        }
    }

    /**
     * Get API UUID by passed parameters.
     *
     * @param provider Provider of the API
     * @param apiName  Name of the API
     * @param version  Version of the API
     * @return String UUID
     * @throws APIManagementException if an error occurs
     */
    public String getUUIDFromIdentifier(String provider, String apiName, String version) throws APIManagementException {

        String uuid = null;
        String sql = SQLConstants.GET_UUID_BY_IDENTIFIER_SQL;
        try (Connection connection = APIMgtDBUtil.getConnection()) {
            PreparedStatement prepStmt = connection.prepareStatement(sql);
            prepStmt.setString(1, APIUtil.replaceEmailDomainBack(provider));
            prepStmt.setString(2, apiName);
            prepStmt.setString(3, version);
            try (ResultSet resultSet = prepStmt.executeQuery()) {
                while (resultSet.next()) {
                    uuid = resultSet.getString(1);
                }
            }
        } catch (SQLException e) {
            handleException("Failed to get the UUID for API : ", e);
        }
        return uuid;
    }

    /**
     * Get API Product UUID by the API Product Identifier and organization.
     *
     * @param identifier API Product Identifier
     * @param organization
     * @param connection
     * @return String UUID
     * @throws APIManagementException if an error occurs
     */
    private String getUUIDFromIdentifier(APIProductIdentifier identifier, String organization, Connection connection)
            throws APIManagementException {
        boolean isNewConnection = false;
        String uuid = null;
        PreparedStatement prepStmt = null;
        String sql = SQLConstants.GET_UUID_BY_IDENTIFIER_AND_ORGANIZATION_SQL;
        try {
            if (connection == null) {
                connection = APIMgtDBUtil.getConnection();
                isNewConnection = true;
            }
            prepStmt = connection.prepareStatement(sql);
            prepStmt.setString(1, identifier.getName());
            prepStmt.setString(2, identifier.getVersion());
            prepStmt.setString(3, organization);
            try (ResultSet resultSet = prepStmt.executeQuery()) {
                while (resultSet.next()) {
                    uuid = resultSet.getString(1);
                }
            }
        } catch (SQLException e) {
            handleException("Failed to retrieve the UUID for the API Product : " + identifier.getName() + '-'
                    + identifier.getVersion(), e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, null, null);
            if (isNewConnection) {
                APIMgtDBUtil.closeAllConnections(null, connection, null);
            }
        }
        return uuid;
    }

    /**
     * Get API TYPE by the uuid.
     *
     * @param uuid UUID of API
     * @return String API Type
     * @throws APIManagementException if an error occurs
     */
    public String getAPITypeFromUUID(String uuid) throws APIManagementException {

        String apiType = null;
        String sql = SQLConstants.GET_API_TYPE_BY_UUID;
        try (Connection connection = APIMgtDBUtil.getConnection()) {
            PreparedStatement prepStmt = connection.prepareStatement(sql);
            prepStmt.setString(1, uuid);
            try (ResultSet resultSet = prepStmt.executeQuery()) {
                while (resultSet.next()) {
                    apiType = resultSet.getString(1);
                }
            }
        } catch (SQLException e) {
            handleException("Failed to retrieve the API TYPE for UUID " + uuid, e);
        }
        return apiType;
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
                application.setUUID(rs.getString("UUID"));
                application.setTokenType(rs.getString("APP_TYPE"));
                application.setApplicationWorkFlowStatus(rs.getString("APPLICATION_STATUS"));
                application.setCallbackUrl(rs.getString("CALLBACK_URL"));
                application.setDescription(rs.getString("DESCRIPTION"));
                application.setTier(rs.getString("APPLICATION_TIER"));
                application.setTokenType(rs.getString("APP_TOKEN_TYPE"));
                workflowDTO.setApplication(application);
                workflowDTO.setKeyType(rs.getString("TOKEN_TYPE"));
                workflowDTO.setUserName(subscriber.getName());
                workflowDTO.setDomainList(rs.getString("ALLOWED_DOMAINS"));
                workflowDTO.setValidityTime(rs.getLong("VALIDITY_PERIOD"));
                String tenantDomain = MultitenantUtils.getTenantDomain(subscriber.getName());
                String keyManagerUUID = rs.getString("KEY_MANAGER");
                workflowDTO.setKeyManager(keyManagerUUID);
                KeyManagerConfigurationDTO keyManagerConfigurationByUUID = getKeyManagerConfigurationByUUID(conn,
                        keyManagerUUID);
                if (keyManagerConfigurationByUUID != null) {
                    OAuthAppRequest request = ApplicationUtils.createOauthAppRequest(application.getName(), null,
                            application.getCallbackUrl(), rs
                                    .getString("TOKEN_SCOPE"),
                            rs.getString("INPUTS"), application.getTokenType(),
                            keyManagerConfigurationByUUID.getOrganization(), keyManagerConfigurationByUUID.getName());
                    request.setMappingId(workflowDTO.getWorkflowReference());
                    request.getOAuthApplicationInfo().setApplicationUUID(application.getUUID());
                    workflowDTO.setAppInfoDTO(request);
                } else {
                    throw new APIManagementException("Error occured while finding the KeyManager from uuid "
                            + keyManagerUUID + ".", ExceptionCodes.KEY_MANAGER_NOT_REGISTERED);
                }
            }
        } catch (SQLException | IOException e) {
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
     * Get external workflow reference by internal workflow reference and workflow type
     *
     * @param internalRef  Internal reference of the workflow
     * @param workflowType Workflow type of the workflow
     * @return External workflow reference for the given internal reference and workflow type if present. Null otherwise
     * @throws APIManagementException If an SQL exception occurs in database interactions
     */
    public String getExternalWorkflowRefByInternalRefWorkflowType(int internalRef, String workflowType) throws APIManagementException {

        String workflowExtRef = null;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        String sqlQuery = SQLConstants.GET_EXTERNAL_WORKFLOW_REFERENCE_SQL;
        try {
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            ps.setString(1, workflowType);
            ps.setString(2, String.valueOf(internalRef));
            rs = ps.executeQuery();

            // returns only one row
            while (rs.next()) {
                workflowExtRef = rs.getString("WF_EXTERNAL_REFERENCE");
            }
        } catch (SQLException e) {
            handleException("Error occurred while getting workflow entry for " +
                    "Internal Ref : " + internalRef, e);
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
     * @param organization organization
     * @return External workflow reference for the subscription identified
     * @throws APIManagementException
     */
    public String getExternalWorkflowReferenceForSubscription(Identifier identifier, int appID, String organization)
            throws APIManagementException {

        String workflowExtRef = null;
        int id = -1;
        int subscriptionID = -1;

        String sqlQuery = SQLConstants.GET_EXTERNAL_WORKFLOW_REFERENCE_FOR_SUBSCRIPTION_SQL;
        String postgreSQL = SQLConstants.GET_EXTERNAL_WORKFLOW_REFERENCE_FOR_SUBSCRIPTION_POSTGRE_SQL;
        try (Connection conn = APIMgtDBUtil.getConnection()) {
            if (identifier instanceof APIIdentifier) {
                String apiUuid;
                if (identifier.getUUID() != null) {
                    apiUuid = identifier.getUUID();
                } else {
                    apiUuid = getUUIDFromIdentifier((APIIdentifier) identifier, organization);
                }
                id = getAPIID(apiUuid, conn);

            } else if (identifier instanceof APIProductIdentifier) {
                id = ((APIProductIdentifier) identifier).getProductId();
            }
            if (conn.getMetaData().getDriverName().contains("PostgreSQL")) {
                sqlQuery = postgreSQL;
            }
            try (PreparedStatement ps = conn.prepareStatement(sqlQuery)) {
                ps.setInt(1, id);
                ps.setInt(2, appID);
                ps.setString(3, WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION);
                try (ResultSet rs = ps.executeQuery()) {
                    // returns only one row
                    if (rs.next()) {
                        workflowExtRef = rs.getString("WF_EXTERNAL_REFERENCE");
                    }
                }

            }
        } catch (SQLException e) {
            handleException("Error occurred while getting workflow entry for " +
                    "Subscription : " + subscriptionID, e);
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

    public String getExternalWorkflowReferenceForSubscriptionAndWFType(int subscriptionId, String wfType) throws APIManagementException {

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
            ps.setString(2, wfType);
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

    public Map<String, Set<Integer>> getPendingSubscriptionsByAppId(int applicationId) throws APIManagementException {

        Set<Integer> pendingCreateSubscriptionIds = new HashSet<>();
        Set<Integer> pendingDeleteSubscriptionIds = new HashSet<>();
        Set<Integer> pendingUpdateSubscriptionIds = new HashSet<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        String sqlQuery = SQLConstants.GET_SUBSCRIPTION_ID_STATUS_BY_APPLICATION_SQL;
        try {
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            ps.setInt(1, applicationId);
            rs = ps.executeQuery();

            while (rs.next()) {
                String subStatus = rs.getString("SUB_STATUS");
                if(APIConstants.SubscriptionStatus.ON_HOLD.equals(subStatus)) {
                    pendingCreateSubscriptionIds.add(rs.getInt("SUBSCRIPTION_ID"));
                }
                else if(APIConstants.SubscriptionStatus.DELETE_PENDING.equals(subStatus)){
                    pendingDeleteSubscriptionIds.add(rs.getInt("SUBSCRIPTION_ID"));
                }
                else if(APIConstants.SubscriptionStatus.TIER_UPDATE_PENDING.equals(subStatus)){
                    pendingUpdateSubscriptionIds.add(rs.getInt("SUBSCRIPTION_ID"));
                }
            }
        } catch (SQLException e) {
            handleException("Error occurred while getting subscription entries for " +
                    "Application : " + applicationId, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
        Map<String,Set<Integer>> map = new HashMap<>();
        map.put(APIConstants.SubscriptionStatus.ON_HOLD, pendingCreateSubscriptionIds);
        map.put(APIConstants.SubscriptionStatus.DELETE_PENDING, pendingDeleteSubscriptionIds);
        map.put(APIConstants.SubscriptionStatus.TIER_UPDATE_PENDING, pendingUpdateSubscriptionIds);
        return map;
    }

    /**
     * Retrieves the IDs of pending subscriptions of a given API
     *
     * @param uuid API uuid
     * @return set of subscriptions ids
     * @throws APIManagementException
     */
    public Set<Integer> getPendingSubscriptionsByAPIId(String uuid) throws APIManagementException {

        Set<Integer> pendingSubscriptions = new HashSet<Integer>();
        String sqlQuery = SQLConstants.GET_SUBSCRIPTIONS_BY_API_SQL;
        try (Connection connection = APIMgtDBUtil.getConnection();
                PreparedStatement ps = connection.prepareStatement(sqlQuery);) {

            ps.setString(1, uuid);
            ps.setString(2, APIConstants.SubscriptionStatus.ON_HOLD);
            try (ResultSet rs = ps.executeQuery();) {
                while (rs.next()) {
                    pendingSubscriptions.add(rs.getInt("SUBSCRIPTION_ID"));
                }
            }

        } catch (SQLException e) {
            handleException("Error occurred while retrieving subscription entries for API with UUID: " + uuid, e);
        }
        return pendingSubscriptions;
    }

    /**
     * Retrieves registration workflow reference for applicationId and key type
     *
     * @param applicationId  id of the application with registration
     * @param keyType        key type of the registration
     * @param keyManagerName
     * @return workflow reference of the registration
     * @throws APIManagementException
     */
    public String getRegistrationWFReference(int applicationId, String keyType, String keyManagerName)
            throws APIManagementException {

        String reference = null;

        String sqlQuery = SQLConstants.GET_REGISTRATION_WORKFLOW_SQL;
        try (Connection conn = APIMgtDBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sqlQuery)) {
            ps.setInt(1, applicationId);
            ps.setString(2, keyType);
            ps.setString(3, keyManagerName);

            try (ResultSet rs = ps.executeQuery()) {
                // returns only one row
                if (rs.next()) {
                    reference = rs.getString("WF_REF");
                }
            }

        } catch (SQLException e) {
            handleException("Error occurred while getting registration entry for " + "Application : " + applicationId,
                    e);
        }
        return reference;
    }

    /**
     * Retrives subscription status for APIIdentifier and applicationId
     *
     * @param uuid    API subscribed
     * @param applicationId application with subscription
     * @return subscription status
     * @throws APIManagementException
     */
    public String getSubscriptionStatus(String uuid, int applicationId) throws APIManagementException {

        String status = null;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int id = -1;

        String sqlQuery = SQLConstants.GET_SUBSCRIPTION_STATUS_SQL;
        try {
            conn = APIMgtDBUtil.getConnection();
            id = getAPIID(uuid, conn);
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
                    "Application : " + applicationId + ", API with UUID: " + uuid, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
        return status;
    }

    /**
     * Retrieves subscription Id for APIIdentifier and applicationId
     *
     * @param uuid    API subscribed
     * @param applicationId application with subscription
     * @return subscription id
     * @throws APIManagementException
     */
    public String getSubscriptionId(String uuid, int applicationId) throws APIManagementException {

        String subId = null;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int id = -1;

        String sqlQuery = SQLConstants.GET_SUBSCRIPTION_ID_SQL;
        try {
            conn = APIMgtDBUtil.getConnection();
            id = getAPIID(uuid, conn);
            ps = conn.prepareStatement(sqlQuery);
            ps.setInt(1, id);
            ps.setInt(2, applicationId);
            rs = ps.executeQuery();

            // returns only one row
            while (rs.next()) {
                subId = rs.getString("SUBSCRIPTION_ID");
            }
        } catch (SQLException e) {
            handleException("Error occurred while getting subscription id for " +
                    "Application : " + applicationId + ", API with UUID: " + uuid, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
        return subId;
    }
    /**
     * Retrieve subscription create state for APIIdentifier and applicationID
     *
     * @param identifier    - api identifier which is subscribed
     * @param applicationId - application used to subscribed
     * @param organization identifier of the organization
     * @param connection
     * @return subscription create status
     * @throws APIManagementException
     */
    public String getSubscriptionCreaeteStatus(APIIdentifier identifier, int applicationId, String organization,
            Connection connection) throws APIManagementException {

        String status = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        String sqlQuery = SQLConstants.GET_SUBSCRIPTION_CREATION_STATUS_SQL;
        try {
            String uuid;
            if (identifier.getUUID() != null) {
                uuid = identifier.getUUID();
            } else {
                uuid = getUUIDFromIdentifier(identifier, organization);
            }
            int apiId = getAPIID(uuid, connection);
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

    public List<KeyManagerConfigurationDTO> getKeyManagerConfigurationsByOrganization(String organization)
            throws APIManagementException {

        List<KeyManagerConfigurationDTO> keyManagerConfigurationDTOS = new ArrayList<>();
        final String query = "SELECT * FROM AM_KEY_MANAGER WHERE ORGANIZATION = ? ";
        try (Connection conn = APIMgtDBUtil.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(query)) {
            preparedStatement.setString(1, organization);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    KeyManagerConfigurationDTO keyManagerConfigurationDTO = new KeyManagerConfigurationDTO();
                    String uuid = resultSet.getString("UUID");
                    keyManagerConfigurationDTO.setUuid(uuid);
                    keyManagerConfigurationDTO.setName(resultSet.getString("NAME"));
                    keyManagerConfigurationDTO.setDisplayName(resultSet.getString("DISPLAY_NAME"));
                    keyManagerConfigurationDTO.setDescription(resultSet.getString("DESCRIPTION"));
                    keyManagerConfigurationDTO.setType(resultSet.getString("TYPE"));
                    keyManagerConfigurationDTO.setEnabled(resultSet.getBoolean("ENABLED"));
                    keyManagerConfigurationDTO.setOrganization(organization);
                    keyManagerConfigurationDTO.setTokenType(resultSet.getString("TOKEN_TYPE"));
                    keyManagerConfigurationDTO.setExternalReferenceId(resultSet.getString("EXTERNAL_REFERENCE_ID"));
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
                    "Error while retrieving key manager configurations for organization " + organization, e);
        }

        return keyManagerConfigurationDTOS;
    }

    public KeyManagerConfigurationDTO getKeyManagerConfigurationByID(String organization, String id)
            throws APIManagementException {

        final String query = "SELECT * FROM AM_KEY_MANAGER WHERE UUID = ? AND ORGANIZATION = ?";
        try (Connection conn = APIMgtDBUtil.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(query)) {
            preparedStatement.setString(1, id);
            preparedStatement.setString(2, organization);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    KeyManagerConfigurationDTO keyManagerConfigurationDTO = new KeyManagerConfigurationDTO();
                    String uuid = resultSet.getString("UUID");
                    keyManagerConfigurationDTO.setUuid(uuid);
                    keyManagerConfigurationDTO.setName(resultSet.getString("NAME"));
                    keyManagerConfigurationDTO.setDescription(resultSet.getString("DESCRIPTION"));
                    keyManagerConfigurationDTO.setType(resultSet.getString("TYPE"));
                    keyManagerConfigurationDTO.setEnabled(resultSet.getBoolean("ENABLED"));
                    keyManagerConfigurationDTO.setOrganization(organization);
                    keyManagerConfigurationDTO.setDisplayName(resultSet.getString("DISPLAY_NAME"));
                    keyManagerConfigurationDTO.setTokenType(resultSet.getString("TOKEN_TYPE"));
                    keyManagerConfigurationDTO.setExternalReferenceId(resultSet.getString("EXTERNAL_REFERENCE_ID"));
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
                    "Error while retrieving key manager configuration for " + id + " in organization " + organization,
                    e);
        }
        return null;

    }

    public boolean isIDPExistInOrg(String organization, String resourceId) throws APIManagementException {

        final String query = "SELECT 1 FROM AM_KEY_MANAGER WHERE EXTERNAL_REFERENCE_ID  = ? AND ORGANIZATION = ?";
        try (Connection conn = APIMgtDBUtil.getConnection();
                PreparedStatement preparedStatement = conn.prepareStatement(query)) {
            preparedStatement.setString(1, resourceId);
            preparedStatement.setString(2, organization);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return true;
                }
            }
        } catch (SQLException e) {
            throw new APIManagementException(
                    "Error while checking key manager for " + resourceId + " in organization " + organization, e);
        }
        return false;
    }

    public KeyManagerConfigurationDTO getKeyManagerConfigurationByName(String organization, String name)
            throws APIManagementException {

        final String query = "SELECT * FROM AM_KEY_MANAGER WHERE NAME = ? AND ORGANIZATION = ?";
        try (Connection conn = APIMgtDBUtil.getConnection()) {
            return getKeyManagerConfigurationByName(conn, organization, name);
        } catch (SQLException | IOException e) {
            throw new APIManagementException(
                    "Error while retrieving key manager configuration for " + name + " in organization " + organization,
                    e);
        }
    }

    private KeyManagerConfigurationDTO getKeyManagerConfigurationByName(Connection connection, String organization,
                                                                        String name)
            throws SQLException, IOException {

        final String query = "SELECT * FROM AM_KEY_MANAGER WHERE NAME = ? AND ORGANIZATION = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, organization);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    KeyManagerConfigurationDTO keyManagerConfigurationDTO = new KeyManagerConfigurationDTO();
                    String uuid = resultSet.getString("UUID");
                    keyManagerConfigurationDTO.setUuid(uuid);
                    keyManagerConfigurationDTO.setName(resultSet.getString("NAME"));
                    keyManagerConfigurationDTO.setDisplayName(resultSet.getString("DISPLAY_NAME"));
                    keyManagerConfigurationDTO.setDescription(resultSet.getString("DESCRIPTION"));
                    keyManagerConfigurationDTO.setType(resultSet.getString("TYPE"));
                    keyManagerConfigurationDTO.setEnabled(resultSet.getBoolean("ENABLED"));
                    keyManagerConfigurationDTO.setOrganization(organization);
                    keyManagerConfigurationDTO.setTokenType(resultSet.getString("TOKEN_TYPE"));
                    keyManagerConfigurationDTO.setExternalReferenceId(resultSet.getString("EXTERNAL_REFERENCE_ID"));
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
            return getKeyManagerConfigurationByUUID(conn, uuid);
        } catch (SQLException | IOException e) {
            throw new APIManagementException(
                    "Error while retrieving key manager configuration for key manager uuid: " + uuid, e);
        }
    }

    private KeyManagerConfigurationDTO getKeyManagerConfigurationByUUID(Connection connection, String uuid)
            throws SQLException, IOException {

        final String query = "SELECT * FROM AM_KEY_MANAGER WHERE UUID = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, uuid);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    KeyManagerConfigurationDTO keyManagerConfigurationDTO = new KeyManagerConfigurationDTO();
                    keyManagerConfigurationDTO.setUuid(resultSet.getString("UUID"));
                    keyManagerConfigurationDTO.setName(resultSet.getString("NAME"));
                    keyManagerConfigurationDTO.setDisplayName(resultSet.getString("DISPLAY_NAME"));
                    keyManagerConfigurationDTO.setDescription(resultSet.getString("DESCRIPTION"));
                    keyManagerConfigurationDTO.setType(resultSet.getString("TYPE"));
                    keyManagerConfigurationDTO.setEnabled(resultSet.getBoolean("ENABLED"));
                    keyManagerConfigurationDTO.setOrganization(resultSet.getString("ORGANIZATION"));
                    keyManagerConfigurationDTO.setTokenType(resultSet.getString("TOKEN_TYPE"));
                    keyManagerConfigurationDTO.setExternalReferenceId(resultSet.getString("EXTERNAL_REFERENCE_ID"));
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
                preparedStatement.setString(6, keyManagerConfigurationDTO.getOrganization());
                preparedStatement.setBoolean(7, keyManagerConfigurationDTO.isEnabled());
                preparedStatement.setString(8, keyManagerConfigurationDTO.getDisplayName());
                preparedStatement.setString(9, keyManagerConfigurationDTO.getTokenType());
                preparedStatement.setString(10, keyManagerConfigurationDTO.getExternalReferenceId());
                preparedStatement.executeUpdate();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                if (e instanceof SQLIntegrityConstraintViolationException) {
                    if (getKeyManagerConfigurationByName(conn, keyManagerConfigurationDTO.getOrganization(),
                            keyManagerConfigurationDTO.getName()) != null) {
                        log.warn(keyManagerConfigurationDTO.getName() + " Key Manager Already Registered in tenant" +
                                keyManagerConfigurationDTO.getOrganization());
                    } else {
                        throw new APIManagementException("Error while Storing key manager configuration with name " +
                                keyManagerConfigurationDTO.getName() + " in tenant " +
                                keyManagerConfigurationDTO.getOrganization(), e);
                    }
                }
            }
        } catch (SQLException | IOException e) {
            throw new APIManagementException(
                    "Error while Storing key manager configuration with name " + keyManagerConfigurationDTO.getName() +
                            " in tenant " + keyManagerConfigurationDTO.getOrganization(), e);
        }
    }

    public boolean isKeyManagerConfigurationExistById(String organization, String id) throws APIManagementException {

        final String query = "SELECT 1 FROM AM_KEY_MANAGER WHERE UUID = ? AND ORGANIZATION = ?";
        try (Connection conn = APIMgtDBUtil.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(query)) {
            preparedStatement.setString(1, id);
            preparedStatement.setString(2, organization);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return true;
                }
            }
        } catch (SQLException e) {
            throw new APIManagementException(
                    "Error while retrieving key manager configuration for " + id + " in organization " + organization,
                    e);
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
                preparedStatement.setString(5, keyManagerConfigurationDTO.getOrganization());
                preparedStatement.setBoolean(6, keyManagerConfigurationDTO.isEnabled());
                preparedStatement.setString(7, keyManagerConfigurationDTO.getDisplayName());
                preparedStatement.setString(8, keyManagerConfigurationDTO.getTokenType());
                preparedStatement.setString(9, keyManagerConfigurationDTO.getExternalReferenceId());
                preparedStatement.setString(10, keyManagerConfigurationDTO.getUuid());
                preparedStatement.executeUpdate();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new APIManagementException(
                    "Error while Updating key manager configuration with name " + keyManagerConfigurationDTO.getName() +
                            " in tenant " + keyManagerConfigurationDTO.getOrganization(), e);
        }
    }

    public void deleteKeyManagerConfigurationById(String id, String organization) throws APIManagementException {

        try (Connection conn = APIMgtDBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement preparedStatement = conn
                    .prepareStatement(SQLConstants.KeyManagerSqlConstants.DELETE_KEY_MANAGER)) {
                preparedStatement.setString(1, id);
                preparedStatement.setString(2, organization);
                preparedStatement.execute();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new APIManagementException(
                    "Error while deleting key manager configuration with id " + id + " in organization " + organization,
                    e);
        }

    }

    public List<KeyManagerConfigurationDTO> getKeyManagerConfigurations() throws APIManagementException {

        List<KeyManagerConfigurationDTO> keyManagerConfigurationDTOS = new ArrayList<>();
        final String query = "SELECT * FROM AM_KEY_MANAGER";
        try (Connection conn = APIMgtDBUtil.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(query)) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    KeyManagerConfigurationDTO keyManagerConfigurationDTO = new KeyManagerConfigurationDTO();
                    String uuid = resultSet.getString("UUID");
                    keyManagerConfigurationDTO.setUuid(uuid);
                    keyManagerConfigurationDTO.setName(resultSet.getString("NAME"));
                    keyManagerConfigurationDTO.setDisplayName(resultSet.getString("DISPLAY_NAME"));
                    keyManagerConfigurationDTO.setDescription(resultSet.getString("DESCRIPTION"));
                    keyManagerConfigurationDTO.setType(resultSet.getString("TYPE"));
                    keyManagerConfigurationDTO.setEnabled(resultSet.getBoolean("ENABLED"));
                    keyManagerConfigurationDTO.setOrganization(resultSet.getString("ORGANIZATION"));
                    keyManagerConfigurationDTO.setTokenType(resultSet.getString("TOKEN_TYPE"));
                    keyManagerConfigurationDTO.setExternalReferenceId(resultSet.getString("EXTERNAL_REFERENCE_ID"));
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

    public boolean isKeyManagerConfigurationExistByName(String name, String organization)
            throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            final String query = "SELECT 1 FROM AM_KEY_MANAGER WHERE NAME = ? AND ORGANIZATION = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, name);
                preparedStatement.setString(2, organization);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            throw new APIManagementException("Error while retrieving key manager existence", e);
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

    public APIKey getKeyMappingFromApplicationIdAndKeyMappingId(int applicationId, String keyMappingId)
            throws APIManagementException {

        final String query = "SELECT UUID,CONSUMER_KEY,KEY_MANAGER,KEY_TYPE,STATE,CREATE_MODE FROM " +
                "AM_APPLICATION_KEY_MAPPING WHERE APPLICATION_ID=? AND UUID = ?";
        Set<APIKey> apiKeyList = new HashSet<>();
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, applicationId);
            preparedStatement.setString(2, keyMappingId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
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
                    return apiKey;
                }
            }
        } catch (SQLException e) {
            throw new APIManagementException("Error while Retrieving Key Mapping ", e);
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

        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(SQLConstants.GET_KEY_MAPPING_ID_FROM_APPLICATION)) {
            preparedStatement.setInt(1, applicationId);
            preparedStatement.setString(2, tokenType);
            preparedStatement.setString(3, keyManagerName);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
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
     *
     * @param applicationId
     * @param keyType
     * @param keyManagerName
     * @param updatedAppInfo
     * @throws APIManagementException
     */
    public void updateApplicationKeyTypeMetaData(int applicationId, String keyType, String keyManagerName,
                                                 OAuthApplicationInfo updatedAppInfo) throws APIManagementException {

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
                        ps.executeUpdate();
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

    /**
     * Retrieve basic information about the given API by the UUID quering only from AM_API
     *
     * @param apiId UUID of the API
     * @return basic information about the API
     * @throws APIManagementException error while getting the API information from AM_API
     */
    public APIInfo getAPIInfoByUUID(String apiId) throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            APIRevision apiRevision = getRevisionByRevisionUUID(connection, apiId);
            String sql = SQLConstants.RETRIEVE_API_INFO_FROM_UUID;
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                if (apiRevision != null) {
                    preparedStatement.setString(1, apiRevision.getApiUUID());
                } else {
                    preparedStatement.setString(1, apiId);
                }
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        APIInfo.Builder apiInfoBuilder = new APIInfo.Builder();
                        apiInfoBuilder = apiInfoBuilder.id(resultSet.getString("API_UUID"))
                                .name(resultSet.getString("API_NAME"))
                                .version(resultSet.getString("API_VERSION"))
                                .provider(resultSet.getString("API_PROVIDER"))
                                .context(resultSet.getString("CONTEXT"))
                                .contextTemplate(resultSet.getString("CONTEXT_TEMPLATE"))
                                .status(APIUtil.getApiStatus(resultSet.getString("STATUS")))
                                .apiType(resultSet.getString("API_TYPE"))
                                .createdBy(resultSet.getString("CREATED_BY"))
                                .createdTime(resultSet.getString("CREATED_TIME"))
                                .updatedBy(resultSet.getString("UPDATED_BY"))
                                .updatedTime(resultSet.getString("UPDATED_TIME"))
                                .revisionsCreated(resultSet.getInt("REVISIONS_CREATED"))
                                .organization(resultSet.getString("ORGANIZATION"))
                                .isRevision(apiRevision != null).organization(resultSet.getString("ORGANIZATION"));
                        if (apiRevision != null) {
                            apiInfoBuilder = apiInfoBuilder.apiTier(getAPILevelTier(connection,
                                    apiRevision.getApiUUID(), apiId));
                        } else {
                            apiInfoBuilder = apiInfoBuilder.apiTier(resultSet.getString("API_TIER"));
                        }
                        return apiInfoBuilder.build();
                    }
                }
            }
        } catch (SQLException e) {
            throw new APIManagementException("Error while retrieving apimgt connection", e,
                    ExceptionCodes.INTERNAL_ERROR);
        }
        return null;
    }

    private APIRevision getRevisionByRevisionUUID(Connection connection, String revisionUUID) throws SQLException {

        try (PreparedStatement statement = connection
                .prepareStatement(SQLConstants.APIRevisionSqlConstants.GET_REVISION_BY_REVISION_UUID)) {
            statement.setString(1, revisionUUID);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    APIRevision apiRevision = new APIRevision();
                    apiRevision.setId(rs.getInt("ID"));
                    apiRevision.setApiUUID(rs.getString("API_UUID"));
                    apiRevision.setRevisionUUID(rs.getString("REVISION_UUID"));
                    apiRevision.setDescription(rs.getString("DESCRIPTION"));
                    apiRevision.setCreatedTime(rs.getString("CREATED_TIME"));
                    apiRevision.setCreatedBy(rs.getString("CREATED_BY"));
                    return apiRevision;
                }
            }
        }
        return null;
    }

    public String getAPIStatusFromAPIUUID(String uuid) throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            try (PreparedStatement preparedStatement =
                         connection.prepareStatement(SQLConstants.RETRIEVE_API_STATUS_FROM_UUID)) {
                preparedStatement.setString(1, uuid);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getString("STATUS");
                    }
                }
            }
        } catch (SQLException e) {
            throw new APIManagementException("Error while retrieving apimgt connection", e,
                    ExceptionCodes.INTERNAL_ERROR);
        }
        return null;
    }

    public void setDefaultVersion(API api) throws APIManagementException {

        APIIdentifier apiId = api.getId();
        try (Connection connection = APIMgtDBUtil.getConnection()) {
            try (PreparedStatement preparedStatement =
                         connection.prepareStatement(SQLConstants.RETRIEVE_DEFAULT_VERSION)) {
                preparedStatement.setString(1, apiId.getApiName());
                preparedStatement.setString(2, APIUtil.replaceEmailDomainBack(apiId.getProviderName()));
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        api.setDefaultVersion(apiId.getVersion().equals(resultSet.getString("DEFAULT_API_VERSION")));
                        api.setAsPublishedDefaultVersion(apiId.getVersion().equals(resultSet.getString(
                                "PUBLISHED_DEFAULT_API_VERSION")));
                    }
                }
            }

        } catch (SQLException e) {
            throw new APIManagementException("Error while retrieving apimgt connection", e,
                    ExceptionCodes.INTERNAL_ERROR);
        }
    }

    public API getLightWeightAPIInfoByAPIIdentifier(APIIdentifier apiIdentifier, String organization)
            throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            try (PreparedStatement preparedStatement =
                         connection.prepareStatement(SQLConstants.GET_LIGHT_WEIGHT_API_INFO_BY_API_IDENTIFIER)) {
                preparedStatement.setString(1, APIUtil.replaceEmailDomainBack(apiIdentifier.getProviderName()));
                preparedStatement.setString(2, apiIdentifier.getName());
                preparedStatement.setString(3, apiIdentifier.getVersion());
                preparedStatement.setString(4, organization);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        apiIdentifier.setId(resultSet.getInt("API_ID"));
                        API api = new API(apiIdentifier);
                        api.setUuid(resultSet.getString("API_UUID"));
                        api.setContext(resultSet.getString("CONTEXT"));
                        api.setType(resultSet.getString("API_TYPE"));
                        api.setStatus(resultSet.getString("STATUS"));
                        return api;
                    }
                }

            }
        } catch (SQLException e) {
            throw new APIManagementException("Error while retrieving apimgt connection", e,
                    ExceptionCodes.INTERNAL_ERROR);
        }
        return null;
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
            String[] user = RemoteUserManagerClient.getInstance().getUserList(claimURI, login);
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
     * @param uuid       API uuid
     * @param apiStoreSet APIStores set
     * @return added/failed
     * @throws APIManagementException
     */
    public boolean addExternalAPIStoresDetails(String uuid, Set<APIStore> apiStoreSet)
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
            apiIdentifier = getAPIID(uuid, conn);
            if (apiIdentifier == -1) {
                String msg = "Could not load API record for API with uuid: " + uuid;
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
     * @param uuid       API uuid
     * @param apiStoreSet APIStores set
     * @return added/failed
     * @throws APIManagementException
     */
    public boolean deleteExternalAPIStoresDetails(String uuid, Set<APIStore> apiStoreSet)
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
            apiIdentifier = getAPIID(uuid, conn);
            if (apiIdentifier == -1) {
                String msg = "Could not load API record for API with UUID: " + uuid;
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

    public void updateExternalAPIStoresDetails(String uuid, Set<APIStore> apiStoreSet)
            throws APIManagementException {

        Connection conn = null;
        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);
            updateExternalAPIStoresDetails(uuid, apiStoreSet, conn);
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
     * @param uuid API uuid
     * @throws APIManagementException if failed to add Application
     */
    public void updateExternalAPIStoresDetails(String uuid, Set<APIStore> apiStoreSet, Connection conn)
            throws APIManagementException, SQLException {

        PreparedStatement ps = null;

        try {
            conn.setAutoCommit(false);
            //This query to add external APIStores to database table
            String sqlQuery = SQLConstants.UPDATE_EXTERNAL_API_STORE_SQL;

            ps = conn.prepareStatement(sqlQuery);
            //Get API Id
            int apiId;
            apiId = getAPIID(uuid, conn);
            if (apiId == -1) {
                String msg = "Could not load API record for API with UUID: " + uuid;
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
     * @param uuid API uuid
     * @return Set of APIStore
     * @throws APIManagementException
     */
    public Set<APIStore> getExternalAPIStoresDetails(String uuid) throws APIManagementException {

        Connection conn = null;
        Set<APIStore> storesSet = new HashSet<APIStore>();
        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);

            storesSet = getExternalAPIStoresDetails(uuid, conn);

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
     * @param uuid API uuid
     * @throws APIManagementException if failed to get external APIStores
     */
    public Set<APIStore> getExternalAPIStoresDetails(String uuid, Connection conn)
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
            apiId = getAPIID(uuid, conn);
            if (apiId == -1) {
                String msg = "Could not load API record for API with UUID: " + uuid;
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
            handleException(
                    "Error while getting External APIStore details from the database for the API with UUID: " + uuid,
                    e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
        return storesSet;
    }

    /**
     * Get Scope keys attached to the given API.
     *
     * @param uuid API uuid
     * @return set of scope key attached to the API
     * @throws APIManagementException if fails get API scope keys
     */
    public Set<String> getAPIScopeKeys(String uuid) throws APIManagementException {

        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        Set<String> scopeKeySet = new LinkedHashSet<>();
        int apiId;
        try {
            conn = APIMgtDBUtil.getConnection();
            apiId = getAPIID(uuid, conn);

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

    public Set<String> getScopesBySubscribedAPIs(List<String> identifiers) throws APIManagementException {

        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        List<Integer> apiIds = new ArrayList<Integer>();
        Set<String> scopes = new HashSet<>();

        try {
            conn = APIMgtDBUtil.getConnection();
            for (String identifier : identifiers) {
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

    /**
     * Get the unversioned local scope keys set of the API.
     *
     * @param uuid API uuid
     * @param tenantId      Tenant Id
     * @return Local Scope keys set
     * @throws APIManagementException if fails to get local scope keys for API
     */
    public Set<String> getUnversionedLocalScopeKeysForAPI(String uuid, int tenantId)
            throws APIManagementException {

        int apiId;
        Set<String> localScopes = new HashSet<>();
        String getUnVersionedLocalScopes = SQLConstants.GET_UNVERSIONED_LOCAL_SCOPES_FOR_API_SQL;
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(getUnVersionedLocalScopes)) {
            apiId = getAPIID(uuid, connection);
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
            handleException("Failed while getting unversioned local scopes for API with UUID:" + uuid + " tenant: "
                    + tenantId, e);
        }
        return localScopes;
    }

    /**
     * Get the versioned local scope keys set of the API.
     *
     * @param uuid API uuid
     * @param tenantId      Tenant Id
     * @return Local Scope keys set
     * @throws APIManagementException if fails to get local scope keys for API
     */
    public Set<String> getVersionedLocalScopeKeysForAPI(String uuid, int tenantId)
            throws APIManagementException {

        int apiId;
        Set<String> localScopes = new HashSet<>();
        String getUnVersionedLocalScopes = SQLConstants.GET_VERSIONED_LOCAL_SCOPES_FOR_API_SQL;
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(getUnVersionedLocalScopes)) {
            apiId = getAPIID(uuid, connection);
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
            handleException("Failed while getting versioned local scopes for API with UUID:" + uuid + " tenant: "
                    + tenantId, e);
        }
        return localScopes;
    }

    /**
     * Get the local scope keys set of the API.
     *
     * @param uuid API uuid
     * @param tenantId      Tenant Id
     * @return Local Scope keys set
     * @throws APIManagementException if fails to get local scope keys for API
     */
    public Set<String> getAllLocalScopeKeysForAPI(String uuid, int tenantId)
            throws APIManagementException {

        int apiId;
        Set<String> localScopes = new HashSet<>();
        String getAllLocalScopesStmt = SQLConstants.GET_ALL_LOCAL_SCOPES_FOR_API_SQL;
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(getAllLocalScopesStmt)) {
            apiId = getAPIID(uuid, connection);
            preparedStatement.setInt(1, apiId);
            preparedStatement.setInt(2, tenantId);
            preparedStatement.setInt(3, tenantId);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    localScopes.add(rs.getString("SCOPE_NAME"));
                }
            }
        } catch (SQLException e) {
            handleException("Failed while getting local scopes for API:" + uuid + " tenant: " + tenantId, e);
        }
        return localScopes;
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
    public boolean isApiNameExist(String apiName, String tenantDomain, String organization)
            throws APIManagementException {

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
            prepStmt.setString(2, organization);
            prepStmt.setString(3, contextParam + '%');
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
    public boolean isApiNameWithDifferentCaseExist(String apiName, String tenantDomain, String organization)
            throws APIManagementException {

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
            prepStmt.setString(4, organization);
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

    /**
     * Check whether the given scope key is already assigned locally to another API which are different from the given
     * API or its versioned APIs under given tenant.
     *
     * @param apiName       API Name
     * @param scopeKey      candidate scope key
     * @param tenantId      tenant id
     * @param organization identifier of the organization
     * @return true if the scope key is already available
     * @throws APIManagementException if failed to check the context availability
     */
    public boolean isScopeKeyAssignedLocally(String apiName, String scopeKey, int tenantId, String organization)
            throws APIManagementException {
        try (Connection connection = APIMgtDBUtil.getConnection();
                PreparedStatement statement = connection.prepareStatement(SQLConstants.IS_SCOPE_ATTACHED_LOCALLY)) {
            statement.setString(1, scopeKey);
            statement.setString(2, organization);
            statement.setInt(3, tenantId);
            statement.setInt(4, tenantId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    String provider = rs.getString("API_PROVIDER");
                    String existingApiName = rs.getString("API_NAME");
                    // Check if the api name is same.
                    // Return false if we're attaching the scope to another version of the API.
                    return !(existingApiName.equals(apiName));
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
                                                    String keyManagerId, String keyType) throws APIManagementException {

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
    public Map<String, String> getConsumerkeyByApplicationIdAndKeyType(int applicationId, String keyType)
            throws APIManagementException {

        Map<String, String> keyManagerConsumerKeyMap = new HashMap<>();
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

    //    public TokenGenerator getTokenGenerator() {
//        return tokenGenerator;
//    }

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

            String deleteAlertTypesByUserNameAndStakeHolderQuery =
                    SQLConstants.DELETE_ALERTTYPES_BY_USERNAME_AND_STAKE_HOLDER;

            ps = connection.prepareStatement(deleteAlertTypesByUserNameAndStakeHolderQuery);
            ps.setString(1, userName);
            ps.setString(2, agent);
            ps.executeUpdate();

            String getEmailListIdByUserNameAndStakeHolderQuery =
                    SQLConstants.GET_EMAILLISTID_BY_USERNAME_AND_STAKEHOLDER;
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
     * @param stakeHolder      if pram value = p we assume those changes from publisher if param value = s those data
     *                         belongs to
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

            String deleteAlertTypesByUserNameAndStakeHolderQuery =
                    SQLConstants.DELETE_ALERTTYPES_BY_USERNAME_AND_STAKE_HOLDER;

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

            String getEmailListIdByUserNameAndStakeHolderQuery =
                    SQLConstants.GET_EMAILLISTID_BY_USERNAME_AND_STAKEHOLDER;
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
        } catch (SQLIntegrityConstraintViolationException e) {
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
                policyStatement.setString(20,
                        policy.getMonetizationPlanProperties().get(APIConstants.Monetization.FIXED_PRICE));
                policyStatement.setString(21,
                        policy.getMonetizationPlanProperties().get(APIConstants.Monetization.BILLING_CYCLE));
                policyStatement.setString(22,
                        policy.getMonetizationPlanProperties().get(APIConstants.Monetization.PRICE_PER_REQUEST));
                policyStatement.setString(23,
                        policy.getMonetizationPlanProperties().get(APIConstants.Monetization.CURRENCY));
                policyStatement.setInt(24, policy.getSubscriberCount());
            } else {
                policyStatement.setString(18, policy.getMonetizationPlan());
                policyStatement.setString(19,
                        policy.getMonetizationPlanProperties().get(APIConstants.Monetization.FIXED_PRICE));
                policyStatement.setString(20,
                        policy.getMonetizationPlanProperties().get(APIConstants.Monetization.BILLING_CYCLE));
                policyStatement.setString(21,
                        policy.getMonetizationPlanProperties().get(APIConstants.Monetization.PRICE_PER_REQUEST));
                policyStatement.setString(22,
                        policy.getMonetizationPlanProperties().get(APIConstants.Monetization.CURRENCY));
                policyStatement.setInt(23, policy.getSubscriberCount());
            }
            policyStatement.executeUpdate();
            conn.commit();
        } catch (SQLIntegrityConstraintViolationException e) {
            boolean isSubscriptionPolicyExists = isPolicyExist(conn, PolicyConstants.POLICY_LEVEL_SUB,
                    policy.getTenantId(),
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
                boolean isSubscriptionPolicyExists = isPolicyExist(conn, PolicyConstants.POLICY_LEVEL_SUB,
                        policy.getTenantId(),
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
            boolean isAPIPolicyExists = isPolicyExist(connection, PolicyConstants.POLICY_LEVEL_API,
                    policy.getTenantId(),
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
                boolean isAPIPolicyExists = isPolicyExist(connection, PolicyConstants.POLICY_LEVEL_API,
                        policy.getTenantId(),
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
            try (PreparedStatement selectStatement = connection.prepareStatement(selectQuery);
                 PreparedStatement deleteStatement = connection.prepareStatement(SQLConstants
                         .ThrottleSQLConstants.DELETE_CONDITION_GROUP_SQL);
                 PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
                if (selectQuery.equals(SQLConstants.ThrottleSQLConstants.GET_API_POLICY_ID_SQL)) {
                    selectStatement.setString(1, policy.getPolicyName());
                    selectStatement.setInt(2, policy.getTenantId());
                } else {
                    selectStatement.setString(1, policy.getUUID());
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
                } else if (PolicyConstants.EVENT_COUNT_TYPE.equalsIgnoreCase(policy.getDefaultQuotaPolicy().getType())) {
                    EventCountLimit limit = (EventCountLimit) policy.getDefaultQuotaPolicy().getLimit();
                    updateStatement.setLong(4, limit.getEventCount());
                    updateStatement.setString(5, null);
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
                handleException("Failed to update API policy: " + policy.getPolicyName() + '-' + policy.getTenantId()
                        , e);
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
        String deleteTierPermissionsQuery = null;

        if (PolicyConstants.POLICY_LEVEL_APP.equals(policyLevel)) {
            query = SQLConstants.DELETE_APPLICATION_POLICY_SQL;
        } else if (PolicyConstants.POLICY_LEVEL_SUB.equals(policyLevel)) {
            //in case of a subscription policy delete we have to remove throttle tier permissions as well
            query = SQLConstants.DELETE_SUBSCRIPTION_POLICY_SQL;
            deleteTierPermissionsQuery = SQLConstants.DELETE_THROTTLE_TIER_BY_NAME_PERMISSION_SQL;
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
            if (deleteTierPermissionsQuery != null) {
                deleteStatement = connection.prepareStatement(deleteTierPermissionsQuery);
                deleteStatement.setString(1, policyName);
                deleteStatement.setInt(2, tenantId);
                deleteStatement.executeUpdate();
            }
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
                subPolicy.setSubscriberCount(rs.getInt(ThrottlePolicyConstants.COLUMN_CONNECTION_COUNT));
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
                subPolicy.setSubscriberCount(rs.getInt(ThrottlePolicyConstants.COLUMN_CONNECTION_COUNT));
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
                policy.setSubscriberCount(resultSet.getInt(ThrottlePolicyConstants.COLUMN_CONNECTION_COUNT));
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
                policy.setSubscriberCount(resultSet.getInt(ThrottlePolicyConstants.COLUMN_CONNECTION_COUNT));
                InputStream binary = resultSet.getBinaryStream(ThrottlePolicyConstants.COLUMN_CUSTOM_ATTRIB);
                if (binary != null) {
                    byte[] customAttrib = APIUtil.toByteArray(binary);
                    policy.setCustomAttributes(customAttrib);
                }
                if (APIConstants.COMMERCIAL_TIER_PLAN.equals(resultSet.getString(
                        ThrottlePolicyConstants.COLUMN_BILLING_PLAN))) {
                    policy.setMonetizationPlan(resultSet.getString(ThrottlePolicyConstants.COLUMN_MONETIZATION_PLAN));
                    Map<String, String> monetizationPlanProperties = new HashMap<String, String>();
                    monetizationPlanProperties.put(APIConstants.Monetization.FIXED_PRICE,
                            resultSet.getString(ThrottlePolicyConstants.COLUMN_FIXED_RATE));
                    monetizationPlanProperties.put(APIConstants.Monetization.BILLING_CYCLE,
                            resultSet.getString(ThrottlePolicyConstants.COLUMN_BILLING_CYCLE));
                    monetizationPlanProperties.put(APIConstants.Monetization.PRICE_PER_REQUEST,
                            resultSet.getString(ThrottlePolicyConstants.COLUMN_PRICE_PER_REQUEST));
                    monetizationPlanProperties.put(APIConstants.Monetization.CURRENCY,
                            resultSet.getString(ThrottlePolicyConstants.COLUMN_CURRENCY));
                    policy.setMonetizationPlanProperties(monetizationPlanProperties);
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
            conditionsStatement =
                    connection.prepareStatement(SQLConstants.ThrottleSQLConstants.GET_HEADER_CONDITIONS_SQL);
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
            conditionsStatement =
                    connection.prepareStatement(SQLConstants.ThrottleSQLConstants.GET_QUERY_PARAMETER_CONDITIONS_SQL);
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
            conditionsStatement =
                    connection.prepareStatement(SQLConstants.ThrottleSQLConstants.GET_JWT_CLAIM_CONDITIONS_SQL);
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
            } else if (PolicyConstants.EVENT_COUNT_TYPE.equalsIgnoreCase(policy.getDefaultQuotaPolicy().getType())) {
                EventCountLimit limit = (EventCountLimit) policy.getDefaultQuotaPolicy().getLimit();
                updateStatement.setLong(4, limit.getEventCount());
                updateStatement.setString(5, null);
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
                    updateStatement.setString(16,
                            policy.getMonetizationPlanProperties().get(APIConstants.Monetization.FIXED_PRICE));
                    updateStatement.setString(17,
                            policy.getMonetizationPlanProperties().get(APIConstants.Monetization.BILLING_CYCLE));
                    updateStatement.setString(18,
                            policy.getMonetizationPlanProperties().get(APIConstants.Monetization.PRICE_PER_REQUEST));
                    updateStatement.setString(19,
                            policy.getMonetizationPlanProperties().get(APIConstants.Monetization.CURRENCY));
                    updateStatement.setInt(20, policy.getSubscriberCount());
                    updateStatement.setString(21, policy.getPolicyName());
                    updateStatement.setInt(22, policy.getTenantId());
                } else if (!StringUtils.isBlank(policy.getUUID())) {
                    updateStatement.setString(15, policy.getMonetizationPlan());
                    updateStatement.setString(16,
                            policy.getMonetizationPlanProperties().get(APIConstants.Monetization.FIXED_PRICE));
                    updateStatement.setString(17,
                            policy.getMonetizationPlanProperties().get(APIConstants.Monetization.BILLING_CYCLE));
                    updateStatement.setString(18,
                            policy.getMonetizationPlanProperties().get(APIConstants.Monetization.PRICE_PER_REQUEST));
                    updateStatement.setString(19,
                            policy.getMonetizationPlanProperties().get(APIConstants.Monetization.CURRENCY));
                    updateStatement.setInt(20, policy.getSubscriberCount());
                    updateStatement.setString(21, policy.getUUID());
                }
            } else {
                if (!StringUtils.isBlank(policy.getPolicyName()) && policy.getTenantId() != -1) {
                    updateStatement.setString(14, policy.getMonetizationPlan());
                    updateStatement.setString(15,
                            policy.getMonetizationPlanProperties().get(APIConstants.Monetization.FIXED_PRICE));
                    updateStatement.setString(16,
                            policy.getMonetizationPlanProperties().get(APIConstants.Monetization.BILLING_CYCLE));
                    updateStatement.setString(17,
                            policy.getMonetizationPlanProperties().get(APIConstants.Monetization.PRICE_PER_REQUEST));
                    updateStatement.setString(18,
                            policy.getMonetizationPlanProperties().get(APIConstants.Monetization.CURRENCY));
                    updateStatement.setInt(19, policy.getSubscriberCount());
                    updateStatement.setString(20, policy.getPolicyName());
                    updateStatement.setInt(21, policy.getTenantId());

                } else if (!StringUtils.isBlank(policy.getUUID())) {
                    updateStatement.setString(14, policy.getMonetizationPlan());
                    updateStatement.setString(15,
                            policy.getMonetizationPlanProperties().get(APIConstants.Monetization.FIXED_PRICE));
                    updateStatement.setString(16,
                            policy.getMonetizationPlanProperties().get(APIConstants.Monetization.BILLING_CYCLE));
                    updateStatement.setString(17,
                            policy.getMonetizationPlanProperties().get(APIConstants.Monetization.PRICE_PER_REQUEST));
                    updateStatement.setString(18,
                            policy.getMonetizationPlanProperties().get(APIConstants.Monetization.CURRENCY));
                    updateStatement.setInt(19, policy.getSubscriberCount());
                    updateStatement.setString(20, policy.getUUID());
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
        } else if (PolicyConstants.EVENT_COUNT_TYPE.equalsIgnoreCase(policy.getDefaultQuotaPolicy().getType())) {
            EventCountLimit limit = (EventCountLimit) policy.getDefaultQuotaPolicy().getLimit();
            policyStatement.setLong(6, limit.getEventCount());
            policyStatement.setString(7, null);
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
        } else if (resultSet.getString(prefix + ThrottlePolicyConstants.COLUMN_QUOTA_POLICY_TYPE)
                .equalsIgnoreCase(PolicyConstants.EVENT_COUNT_TYPE)) {
            EventCountLimit eventCountLimit = new EventCountLimit();
            eventCountLimit.setUnitTime(resultSet.getInt(prefix + ThrottlePolicyConstants.COLUMN_UNIT_TIME));
            eventCountLimit.setTimeUnit(resultSet.getString(prefix + ThrottlePolicyConstants.COLUMN_TIME_UNIT));
            eventCountLimit.setEventCount(resultSet.getInt(prefix + ThrottlePolicyConstants.COLUMN_QUOTA));
            quotaPolicy.setLimit(eventCountLimit);
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
            String query = "SELECT " + PolicyConstants.POLICY_IS_DEPLOYED + " FROM " + policyTable + " WHERE " +
                    "TENANT_ID =? AND NAME = ? ";
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
        String conditionType = blockConditionsDTO.getConditionType();
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

                    String[] app = applicationIdentifier.split("-", 2);
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
                    if (generatedKeys != null && generatedKeys.next()) {
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
                blockCondition.setConditionValue(resultSet.getString("BLOCK_CONDITION"));
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
                blockCondition.setConditionValue(resultSet.getString("BLOCK_CONDITION"));
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
                blockConditionsDTO.setConditionValue(resultSet.getString("BLOCK_CONDITION"));
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

    private String getAPILevelTier(Connection connection, String apiUUID, String revisionUUID) throws SQLException {

        try (PreparedStatement preparedStatement =
                     connection.prepareStatement(SQLConstants.GET_REVISIONED_API_TIER_SQL)) {
            preparedStatement.setString(1, apiUUID);
            preparedStatement.setString(2, revisionUUID);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("API_TIER");
                }
            }
        }
        return null;
    }

    public String getAPILevelTier(String apiUUID, String revisionUUID) throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            return getAPILevelTier(connection, apiUUID, revisionUUID);
        } catch (SQLException e) {
            handleException("Failed to retrieve Connection", e);
        }
        return null;
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

    public String getGroupId(int applicationId) throws APIManagementException {

        try (Connection conn = APIMgtDBUtil.getConnection()) {
            return getGroupId(conn, applicationId);
        } catch (SQLException e) {
            handleException("Failed to Retrieve GroupId for application " + applicationId, e);
        }
        return null;
    }

    /**
     * Fetches all the groups for a given application and creates a single string separated by comma
     *
     * @param applicationId
     * @return comma separated group Id String
     * @throws APIManagementException
     */
    private String getGroupId(Connection connection, int applicationId) throws SQLException {

        ArrayList<String> grpIdList = new ArrayList<String>();
        String sqlQuery = SQLConstants.GET_GROUP_ID_SQL;
        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
            preparedStatement.setInt(1, applicationId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    grpIdList.add(resultSet.getString("GROUP_ID"));
                }
            }
        }
        return String.join(",", grpIdList);
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
                application.setOrganization(rs.getString("ORGANIZATION"));
                application.setLastUpdatedTime(String.valueOf(rs.getTimestamp("UPDATED_TIME").getTime()));
                application.setCreatedTime(String.valueOf(rs.getTimestamp("CREATED_TIME").getTime()));

                if (multiGroupAppSharingEnabled) {
                    if (application.getGroupId() == null || application.getGroupId().isEmpty()) {
                        application.setGroupId(getGroupId(connection, application.getId()));
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
     * Returns the Environments List for the TenantId.
     *
     * @param tenantDomain The tenant domain.
     * @return List of Environments.
     */
    public List<Environment> getAllEnvironments(String tenantDomain) throws APIManagementException {

        List<Environment> envList = new ArrayList<>();
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(SQLConstants.GET_ENVIRONMENT_BY_ORGANIZATION_SQL)) {
            prepStmt.setString(1, tenantDomain);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    Integer id = rs.getInt("ID");
                    String uuid = rs.getString("UUID");
                    String name = rs.getString("NAME");
                    String displayName = rs.getString("DISPLAY_NAME");
                    String description = rs.getString("DESCRIPTION");
                    String provider = rs.getString("PROVIDER");

                    Environment env = new Environment();
                    env.setId(id);
                    env.setUuid(uuid);
                    env.setName(name);
                    env.setDisplayName(displayName);
                    env.setDescription(description);
                    env.setProvider(provider);
                    env.setVhosts(getVhostGatewayEnvironments(connection, id));
                    envList.add(env);
                }
            }
        } catch (SQLException e) {
            handleException("Failed to get Environments in tenant domain: " + tenantDomain, e);
        }
        return envList;
    }

    /**
     * Returns the Environment for the uuid in the tenant domain.
     *
     * @param tenantDomain the tenant domain to look environment
     * @param uuid         UUID of the environment
     * @return Gateway environment with given UUID
     */
    public Environment getEnvironment(String tenantDomain, String uuid) throws APIManagementException {

        Environment env = null;
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement prepStmt =
                     connection.prepareStatement(SQLConstants.GET_ENVIRONMENT_BY_ORGANIZATION_AND_UUID_SQL)) {
            prepStmt.setString(1, tenantDomain);
            prepStmt.setString(2, uuid);
            try (ResultSet rs = prepStmt.executeQuery()) {
                if (rs.next()) {
                    Integer id = rs.getInt("ID");
                    String name = rs.getString("NAME");
                    String displayName = rs.getString("DISPLAY_NAME");
                    String description = rs.getString("DESCRIPTION");
                    String provider = rs.getString("PROVIDER");

                    env = new Environment();
                    env.setId(id);
                    env.setUuid(uuid);
                    env.setName(name);
                    env.setDisplayName(displayName);
                    env.setDescription(description);
                    env.setProvider(provider);
                    env.setVhosts(getVhostGatewayEnvironments(connection, id));
                }
            }
        } catch (SQLException e) {
            handleException("Failed to get Environment in tenant domain:" + tenantDomain, e);
        }
        return env;
    }

    /**
     * Add an Environment
     *
     * @param tenantDomain tenant domain
     * @param environment  Environment
     * @return added Environment
     * @throws APIManagementException if failed to add environment
     */
    public Environment addEnvironment(String tenantDomain, Environment environment) throws APIManagementException {

        String uuid = UUID.randomUUID().toString();
        environment.setUuid(uuid);

        try (Connection conn = APIMgtDBUtil.getConnection()) {
            conn.setAutoCommit(false);
            String dbProductName = conn.getMetaData().getDatabaseProductName();
            try (PreparedStatement prepStmt = conn.prepareStatement(SQLConstants.INSERT_ENVIRONMENT_SQL,
                    new String[]{DBUtils.getConvertedAutoGeneratedColumnName(dbProductName, "ID")})) {
                prepStmt.setString(1, uuid);
                prepStmt.setString(2, environment.getName());
                prepStmt.setString(3, environment.getDisplayName());
                prepStmt.setString(4, environment.getDescription());
                prepStmt.setString(5, environment.getProvider());
                prepStmt.setString(6, tenantDomain);
                prepStmt.executeUpdate();

                ResultSet rs = prepStmt.getGeneratedKeys();
                int id = -1;
                if (rs.next()) {
                    id = rs.getInt(1);
                }
                addGatewayVhosts(conn, id, environment.getVhosts());
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                handleException("Failed to add VHost: " + uuid, e);
            }
        } catch (SQLException e) {
            handleException("Failed to add VHost: " + uuid, e);
        }
        return environment;
    }

    /**
     * Add VHost assigned to gateway environment
     *
     * @param connection connection
     * @param id         Environment ID in the databse
     * @param vhosts     list of VHosts assigned to the environment
     * @throws APIManagementException if falied to add VHosts
     */
    private void addGatewayVhosts(Connection connection, int id, List<VHost> vhosts) throws
            APIManagementException {

        try (PreparedStatement prepStmt = connection.prepareStatement(SQLConstants.INSERT_GATEWAY_VHOSTS_SQL)) {
            for (VHost vhost : vhosts) {
                prepStmt.setInt(1, id);
                prepStmt.setString(2, vhost.getHost());
                prepStmt.setString(3, vhost.getHttpContext());
                prepStmt.setString(4, vhost.getHttpPort().toString());
                prepStmt.setString(5, vhost.getHttpsPort().toString());
                prepStmt.setString(6, vhost.getWsPort().toString());
                prepStmt.setString(7, vhost.getWssPort().toString());
                prepStmt.addBatch();
            }
            prepStmt.executeBatch();
        } catch (SQLException e) {
            handleException("Failed to add VHosts for environment ID: " + id, e);
        }
    }

    /**
     * Delete all VHosts assigned to gateway environment
     *
     * @param connection connection
     * @param id         Environment ID in the databse
     * @throws APIManagementException if falied to delete VHosts
     */
    private void deleteGatewayVhosts(Connection connection, int id) throws
            APIManagementException {

        try (PreparedStatement prepStmt = connection.prepareStatement(SQLConstants.DELETE_GATEWAY_VHOSTS_SQL)) {
            prepStmt.setInt(1, id);
            prepStmt.executeUpdate();
        } catch (SQLException e) {
            handleException("Failed to delete VHosts for environment ID: " + id, e);
        }
    }

    /**
     * Returns a list of vhosts belongs to the gateway environments
     *
     * @param connection DB connection
     * @param envId      Environment id.
     * @return list of vhosts belongs to the gateway environments.
     */
    private List<VHost> getVhostGatewayEnvironments(Connection connection, Integer envId) throws APIManagementException {

        List<VHost> vhosts = new ArrayList<>();
        try (PreparedStatement prepStmt = connection.prepareStatement(SQLConstants.GET_ENVIRONMENT_VHOSTS_BY_ID_SQL)) {
            prepStmt.setInt(1, envId);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    String host = rs.getString("HOST");
                    String httpContext = rs.getString("HTTP_CONTEXT");
                    Integer httpPort = rs.getInt("HTTP_PORT");
                    Integer httpsPort = rs.getInt("HTTPS_PORT");
                    Integer wsPort = rs.getInt("WS_PORT");
                    Integer wssPort = rs.getInt("WSS_PORT");

                    VHost vhost = new VHost();
                    vhost.setHost(host);
                    vhost.setHttpContext(httpContext == null ? "" : httpContext);
                    vhost.setHttpPort(httpPort);
                    vhost.setHttpsPort(httpsPort);
                    vhost.setWsPort(wsPort);
                    vhost.setWssPort(wssPort);
                    // Since DB does not contain columns for wsHost and wssHost, host is used
                    vhost.setWsHost(host);
                    vhost.setWssHost(host);
                    vhosts.add(vhost);
                }
            }
        } catch (SQLException e) {
            handleException("Failed to get gateway environments list of VHost: ", e);
        }
        return vhosts;
    }

    /**
     * Delete an Environment
     *
     * @param uuid UUID of the environment
     * @throws APIManagementException if failed to delete environment
     */
    public void deleteEnvironment(String uuid) throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement prepStmt = connection.prepareStatement(SQLConstants.DELETE_ENVIRONMENT_SQL)) {
                prepStmt.setString(1, uuid);
                prepStmt.executeUpdate();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                handleException("Failed to delete Environment", e);
            }
        } catch (SQLException e) {
            handleException("Failed to delete Environment", e);
        }
    }

    /**
     * Update Gateway Environment
     *
     * @param environment Environment to be updated
     * @return Updated Environment
     * @throws APIManagementException if failed to updated Environment
     */
    public Environment updateEnvironment(Environment environment) throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement prepStmt = connection.prepareStatement(SQLConstants.UPDATE_ENVIRONMENT_SQL)) {
                prepStmt.setString(1, environment.getDisplayName());
                prepStmt.setString(2, environment.getDescription());
                prepStmt.setString(3, environment.getUuid());
                prepStmt.executeUpdate();
                deleteGatewayVhosts(connection, environment.getId());
                addGatewayVhosts(connection, environment.getId(), environment.getVhosts());
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                handleException("Failed to update Environment", e);
            }
        } catch (SQLException e) {
            handleException("Failed to update Environment", e);
        }
        return environment;
    }

    private void addApplicationAttributes(Connection conn, Map<String, String> attributes, int applicationId,
                                          int tenantId)
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
            handleException("Error in adding attributes of application with id: " + applicationId, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, null, rs);
        }
    }

    /**
     * Get all attributes stored against an Application
     *
     * @param conn          Database connection
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
                        rs.getString("APP_ATTRIBUTE"));
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
     * @param attributeKey  User defined key of attribute
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
     * @param applicationId         Id of Application against which attributes are getting stored
     * @param tenantId              Id of tenant
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
     * @param subscriberId    subscriberId of the Application
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
                        application.setGroupId(getGroupId(connection,application.getId()));
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
     *
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
            String query = SQLConstants.GET_URL_TEMPLATES_FOR_API_WITH_UUID;

            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, api.getUuid());
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
            handleException("Error while obtaining details of the URI Template for api " + api.getId(), e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
        }

        return templatesMap;
    }

    public List<ResourcePath> getResourcePathsOfAPI(APIIdentifier apiId) throws APIManagementException {

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
                        resourcePath.setResourcePath(rs.getString("URL_PATTERN"));
                        resourcePath.setHttpVerb(rs.getString("HTTP_METHOD"));
                        resourcePathList.add(resourcePath);
                    }
                }
            }
        } catch (SQLException e) {
            handleException("Error while obtaining Resource Paths of api " + apiId, e);
        }
        return resourcePathList;
    }

    public void addAPIProduct(APIProduct apiProduct, String organization) throws APIManagementException {

        Connection connection = null;
        PreparedStatement prepStmtAddAPIProduct = null;
        PreparedStatement prepStmtAddScopeEntry = null;

        if (log.isDebugEnabled()) {
            log.debug("addAPIProduct() : " + apiProduct.toString() + " for organization " + organization);
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
            prepStmtAddAPIProduct.setString(9, apiProduct.getUuid());
            prepStmtAddAPIProduct.setString(10, apiProduct.getState());
            prepStmtAddAPIProduct.setString(11, organization);
            prepStmtAddAPIProduct.setString(12, apiProduct.getGatewayVendor());
            prepStmtAddAPIProduct.setString(13, apiProduct.getVersionTimestamp());
            prepStmtAddAPIProduct.execute();

            rs = prepStmtAddAPIProduct.getGeneratedKeys();

            if (rs.next()) {
                productId = rs.getInt(1);
            }
            //breaks the flow if product is not added to the db correctly
            if (productId == 0) {
                throw new APIManagementException("Error while adding API product " + apiProduct.getUuid());
            }

            addAPIProductResourceMappings(apiProduct.getProductResources(), apiProduct.getOrganization(), connection);
            String tenantUserName = MultitenantUtils
                    .getTenantAwareUsername(APIUtil.replaceEmailDomainBack(identifier.getProviderName()));
            int tenantId = APIUtil.getTenantId(APIUtil.replaceEmailDomainBack(identifier.getProviderName()));
            recordAPILifeCycleEvent(productId, null, APIStatus.CREATED.toString(), tenantUserName, tenantId,
                    connection);
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
     * - url templeates to product mappings (resource bundling) - AM_API_PRODUCT_MAPPING
     *
     * @param productResources
     * @param organization
     * @param connection
     * @throws APIManagementException
     */
    public void addAPIProductResourceMappings(List<APIProductResource> productResources, String organization,
            Connection connection) throws APIManagementException {
        String addProductResourceMappingSql = SQLConstants.ADD_PRODUCT_RESOURCE_MAPPING_SQL;

        boolean isNewConnection = false;
        String uuid = null;
        try {
            if (connection == null) {
                connection = APIMgtDBUtil.getConnection();
                isNewConnection = true;
            }

            Set<String> usedClonedPolicies = new HashSet<>();
            Map<String, String> clonedPoliciesMap = new HashMap<>();

            //add the duplicate resources in each API in the API product.
            for (APIProductResource apiProductResource : productResources) {
                APIProductIdentifier productIdentifier = apiProductResource.getProductIdentifier();
                if (productIdentifier.getUUID() != null) {
                    uuid = productIdentifier.getUUID();
                } else {
                    uuid = getUUIDFromIdentifier(productIdentifier, organization, connection);
                }
                int productId = getAPIID(uuid, connection);
                int tenantId = APIUtil.getTenantId(APIUtil.replaceEmailDomainBack(productIdentifier.getProviderName()));
                String tenantDomain = APIUtil.getTenantDomainFromTenantId(tenantId);
                URITemplate uriTemplateOriginal = apiProductResource.getUriTemplate();
                int urlMappingId = uriTemplateOriginal.getId();
                // Adding to AM_API_URL_MAPPING table
                PreparedStatement getURLMappingsStatement = connection
                        .prepareStatement(SQLConstants.APIRevisionSqlConstants.
                                GET_URL_MAPPINGS_WITH_SCOPE_BY_URL_MAPPING_ID);
                getURLMappingsStatement.setInt(1, urlMappingId);
                List<URITemplate> urlMappingList = new ArrayList<>();
                try (ResultSet rs = getURLMappingsStatement.executeQuery()) {
                    while (rs.next()) {
                        URITemplate uriTemplate = new URITemplate();
                        uriTemplate.setHTTPVerb(rs.getString("HTTP_METHOD"));
                        uriTemplate.setAuthType(rs.getString("AUTH_SCHEME"));
                        uriTemplate.setUriTemplate(rs.getString("URL_PATTERN"));
                        uriTemplate.setThrottlingTier(rs.getString("THROTTLING_TIER"));
                        String script = null;
                        InputStream mediationScriptBlob = rs.getBinaryStream("MEDIATION_SCRIPT");
                        if (mediationScriptBlob != null) {
                            script = APIMgtDBUtil.getStringFromInputStream(mediationScriptBlob);
                        }
                        uriTemplate.setMediationScript(script);
                        if (!StringUtils.isEmpty(rs.getString("SCOPE_NAME"))) {
                            Scope scope = new Scope();
                            scope.setKey(rs.getString("SCOPE_NAME"));
                            uriTemplate.setScope(scope);
                        }
                        if (rs.getInt("API_ID") != 0) {
                            // Adding api id to uri template id just to store value
                            uriTemplate.setId(rs.getInt("API_ID"));
                        }
                        List<OperationPolicy> operationPolicies = getOperationPoliciesOfURITemplate(connection,
                                urlMappingId);
                        uriTemplate.setOperationPolicies(operationPolicies);
                        urlMappingList.add(uriTemplate);
                    }
                }

                Map<String, URITemplate> uriTemplateMap = new HashMap<>();
                for (URITemplate urlMapping : urlMappingList) {
                    if (urlMapping.getScope() != null) {
                        URITemplate urlMappingNew = urlMapping;
                        URITemplate urlMappingExisting = uriTemplateMap.get(urlMapping.getUriTemplate()
                                + urlMapping.getHTTPVerb());
                        if (urlMappingExisting != null && urlMappingExisting.getScopes() != null) {
                            if (!urlMappingExisting.getScopes().contains(urlMapping.getScope())) {
                                urlMappingExisting.setScopes(urlMapping.getScope());
                                uriTemplateMap.put(urlMappingExisting.getUriTemplate() + urlMappingExisting.getHTTPVerb(),
                                        urlMappingExisting);
                            }
                        } else {
                            urlMappingNew.setScopes(urlMapping.getScope());
                            uriTemplateMap.put(urlMappingNew.getUriTemplate() + urlMappingNew.getHTTPVerb(),
                                    urlMappingNew);
                        }
                    } else if (urlMapping.getId() != 0) {
                        URITemplate urlMappingExisting = uriTemplateMap.get(urlMapping.getUriTemplate()
                                + urlMapping.getHTTPVerb());
                        if (urlMappingExisting == null) {
                            uriTemplateMap.put(urlMapping.getUriTemplate() + urlMapping.getHTTPVerb(), urlMapping);
                        }
                    } else {
                        uriTemplateMap.put(urlMapping.getUriTemplate() + urlMapping.getHTTPVerb(), urlMapping);
                    }
                }

                PreparedStatement insertURLMappingsStatement = connection
                        .prepareStatement(SQLConstants.APIRevisionSqlConstants.INSERT_URL_MAPPINGS);
                for (URITemplate urlMapping : uriTemplateMap.values()) {
                    insertURLMappingsStatement.setInt(1, urlMapping.getId());
                    insertURLMappingsStatement.setString(2, urlMapping.getHTTPVerb());
                    insertURLMappingsStatement.setString(3, urlMapping.getAuthType());
                    insertURLMappingsStatement.setString(4, urlMapping.getUriTemplate());
                    insertURLMappingsStatement.setString(5, urlMapping.getThrottlingTier());
                    insertURLMappingsStatement.setString(6, String.valueOf(productId));
                    insertURLMappingsStatement.addBatch();
                }
                insertURLMappingsStatement.executeBatch();

                // Add to AM_API_RESOURCE_SCOPE_MAPPING table and to AM_API_PRODUCT_MAPPING
                PreparedStatement getRevisionedURLMappingsStatement = connection
                        .prepareStatement(SQLConstants.APIRevisionSqlConstants.GET_URL_MAPPINGS_ID);
                PreparedStatement insertScopeResourceMappingStatement = connection
                        .prepareStatement(SQLConstants.APIRevisionSqlConstants.INSERT_SCOPE_RESOURCE_MAPPING);
                PreparedStatement insertProductResourceMappingStatement = connection
                        .prepareStatement(addProductResourceMappingSql);
                String dbProductName = connection.getMetaData().getDatabaseProductName();
                PreparedStatement insertOperationPolicyMappingStatement = connection
                        .prepareStatement(SQLConstants.OperationPolicyConstants.ADD_API_OPERATION_POLICY_MAPPING, new String[]{
                                DBUtils.getConvertedAutoGeneratedColumnName(dbProductName, "OPERATION_POLICY_MAPPING_ID")});
                List<ClonePolicyMetadataDTO> toBeClonedPolicyDetails = new ArrayList<>();
                for (URITemplate urlMapping : uriTemplateMap.values()) {
                    getRevisionedURLMappingsStatement.setInt(1, urlMapping.getId());
                    getRevisionedURLMappingsStatement.setString(2, urlMapping.getHTTPVerb());
                    getRevisionedURLMappingsStatement.setString(3, urlMapping.getAuthType());
                    getRevisionedURLMappingsStatement.setString(4, urlMapping.getUriTemplate());
                    getRevisionedURLMappingsStatement.setString(5, urlMapping.getThrottlingTier());
                    getRevisionedURLMappingsStatement.setString(6, String.valueOf(productId));
                    if (!urlMapping.getScopes().isEmpty()) {
                        try (ResultSet rs = getRevisionedURLMappingsStatement.executeQuery()) {
                            while (rs.next()) {
                                for (Scope scope : urlMapping.getScopes()) {
                                    insertScopeResourceMappingStatement.setString(1, scope.getKey());
                                    insertScopeResourceMappingStatement.setInt(2, rs.getInt(1));
                                    insertScopeResourceMappingStatement.setInt(3, tenantId);
                                    insertScopeResourceMappingStatement.addBatch();
                                }
                            }
                        }
                    }
                    try (ResultSet rs = getRevisionedURLMappingsStatement.executeQuery()) {
                        while (rs.next()) {
                            insertProductResourceMappingStatement.setInt(1, productId);
                            insertProductResourceMappingStatement.setInt(2, rs.getInt(1));
                            insertProductResourceMappingStatement.setString(3, "Current API");
                            insertProductResourceMappingStatement.addBatch();
                        }
                    }
                    try (ResultSet rs = getRevisionedURLMappingsStatement.executeQuery()) {
                        while (rs.next()) {
                            for (OperationPolicy policy : urlMapping.getOperationPolicies()) {
                                if (!clonedPoliciesMap.keySet().contains(policy.getPolicyId())) {
                                    //Check whether API Specific policies available
                                    OperationPolicyData existingPolicy =
                                            getAPISpecificOperationPolicyByPolicyID(connection,
                                                    policy.getPolicyId(), uuid, tenantDomain, false);
                                    String clonedPolicyId = policy.getPolicyId();
                                    if (existingPolicy != null) {
                                        if (existingPolicy.isClonedPolicy()) {
                                            usedClonedPolicies.add(clonedPolicyId);
                                        }
                                    } else {
                                        // Even though the policy ID attached is not in the API specific policy list for the product uuid,
                                        // it can be from the dependent API and we need to verify that it has not been previously cloned
                                        // for the product before cloning again.
                                        clonedPolicyId = getClonedPolicyIdForCommonPolicyId(connection,
                                                policy.getPolicyId(), uuid);
                                        if (clonedPolicyId == null) {
                                            clonedPolicyId = UUID.randomUUID().toString();
                                            ClonePolicyMetadataDTO toBeClonedSinglePolicyData = new ClonePolicyMetadataDTO();
                                            toBeClonedSinglePolicyData.setClonedPolicyUUID(clonedPolicyId);
                                            // In here the cloned policy ID will be not the common policy Id, but the
                                            // API specific policy Id from the dependent API
                                            toBeClonedSinglePolicyData.setCurrentPolicyUUID(policy.getPolicyId());
                                            toBeClonedSinglePolicyData.setApiUUID(uuid);
                                            toBeClonedPolicyDetails.add(toBeClonedSinglePolicyData);

                                        }
                                        usedClonedPolicies.add(clonedPolicyId);
                                        //usedClonedPolicies set will not contain used API specific policies that are not cloned.
                                    }

                                    // Updated policies map will record the updated policy ID for the used policy ID.
                                    // If the policy has been cloned to the API specific policy list, we need to use the
                                    // updated policy Id.
                                    clonedPoliciesMap.put(policy.getPolicyId(), clonedPolicyId);
                                }

                                Gson gson = new Gson();
                                String paramJSON = gson.toJson(policy.getParameters());

                                insertOperationPolicyMappingStatement.setInt(1, rs.getInt(1));
                                insertOperationPolicyMappingStatement
                                        .setString(2, clonedPoliciesMap.get(policy.getPolicyId()));
                                insertOperationPolicyMappingStatement.setString(3, policy.getDirection());
                                insertOperationPolicyMappingStatement.setString(4, paramJSON);
                                insertOperationPolicyMappingStatement.setInt(5, policy.getOrder());
                                insertOperationPolicyMappingStatement.addBatch();
                            }
                        }
                    }
                }
                insertScopeResourceMappingStatement.executeBatch();
                insertProductResourceMappingStatement.executeBatch();
                for (ClonePolicyMetadataDTO toBeClonedPolicyData : toBeClonedPolicyDetails) {
                    cloneCommonPolicyToAPI(connection, toBeClonedPolicyData.getCurrentPolicyUUID(),
                            toBeClonedPolicyData.getClonedPolicyUUID(), toBeClonedPolicyData.getApiUUID());
                }
                insertOperationPolicyMappingStatement.executeBatch();
            }
            cleanUnusedClonedOperationPolicies(connection, usedClonedPolicies, uuid);
        } catch (SQLException e) {
            handleException("Error while adding API product Resources", e);
        } finally {
            if (isNewConnection) {
                APIMgtDBUtil.closeAllConnections(null, connection, null);
            }
        }
    }

    /**
     * Update Product scope and resource mappings
     *
     * @param apiProduct
     * @param productId
     * @param connection
     * @throws APIManagementException
     */
    public void updateAPIProductResourceMappings(APIProduct apiProduct, int productId, Connection connection)
            throws APIManagementException {

        PreparedStatement removeURLMappingsStatement = null;
        try {
            // Retrieve Product Resources
            PreparedStatement getProductMappingsStatement = connection.prepareStatement(SQLConstants.
                    APIRevisionSqlConstants.GET_CUURENT_API_PRODUCT_RESOURCES);
            getProductMappingsStatement.setInt(1, productId);
            List<Integer> urlMappingIds = new ArrayList<>();
            try (ResultSet rs = getProductMappingsStatement.executeQuery()) {
                while (rs.next()) {
                    urlMappingIds.add(rs.getInt(1));
                }
            }
            // Removing related revision entries from AM_API_URL_MAPPING table
            // This will cascade remove entries from AM_API_RESOURCE_SCOPE_MAPPING and AM_API_PRODUCT_MAPPING tables
            removeURLMappingsStatement = connection.prepareStatement(SQLConstants
                    .APIRevisionSqlConstants.REMOVE_PRODUCT_ENTRIES_IN_AM_API_URL_MAPPING_BY_URL_MAPPING_ID);
            for (int id : urlMappingIds) {
                removeURLMappingsStatement.setInt(1, id);
                removeURLMappingsStatement.addBatch();
            }
            removeURLMappingsStatement.executeBatch();
            //Add new resources
            addAPIProductResourceMappings(apiProduct.getProductResources(), apiProduct.getOrganization(), connection);
        } catch (SQLException e) {
            handleException("Error while updating API-Product Resources.", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(removeURLMappingsStatement, null, null);
        }
    }

    /**
     * Delete API product and its related scopes
     *
     * @param productIdentifier product ID
     * @throws APIManagementException
     */
    public void deleteAPIProduct(APIProductIdentifier productIdentifier) throws APIManagementException {

        String deleteQuery = SQLConstants.DELETE_API_PRODUCT_SQL;
        String deleteRatingsQuery = SQLConstants.REMOVE_FROM_API_RATING_SQL;
        String urlMappingQuery = SQLConstants.REMOVE_FROM_URI_TEMPLATES__FOR_PRODUCTS_SQL;
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

            ps = connection.prepareStatement(urlMappingQuery);
            ps.setString(1, Integer.toString(id));
            ps.execute();
            ps.close();

            deleteAllAPISpecificOperationPoliciesByAPIUUID(connection, productIdentifier.getUUID(), null);

            connection.commit();
        } catch (SQLException e) {
            handleException("Error while deleting api product " + productIdentifier, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, connection, null);
        }
    }

    public List<APIProductResource> getProductMappingsForAPI(API api) throws APIManagementException {

        List<APIProductResource> productMappings = new ArrayList<>();

        Set<URITemplate> uriTemplatesOfAPI = getURITemplatesOfAPI(api.getUuid());

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
        if (log.isDebugEnabled()) {
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
            ps.setString(4, product.getGatewayVendor());
            APIProductIdentifier identifier = product.getId();
            ps.setString(5, identifier.getName());
            ps.setString(6, APIUtil.replaceEmailDomainBack(identifier.getProviderName()));
            ps.setString(7, identifier.getVersion());
            ps.executeUpdate();

            int productId = getAPIID(product.getUuid(), conn);
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
     *
     * @param productIdentifier api product identifier
     * @throws APIManagementException
     */
    public List<APIProductResource> getAPIProductResourceMappings(APIProductIdentifier productIdentifier)
            throws APIManagementException {

        int productId = getAPIProductId(productIdentifier);
        List<APIProductResource> productResourceList = new ArrayList<>();
        try (Connection connection = APIMgtDBUtil.getConnection()) {
            if (checkAPIUUIDIsARevisionUUID(productIdentifier.getUUID()) == null) {
                String sql = SQLConstants.GET_RESOURCES_OF_PRODUCT;
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setInt(1, productId);
                    ps.setString(2, String.valueOf(productId));
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            APIProductResource resource = new APIProductResource();
                            APIIdentifier apiId = new APIIdentifier(rs.getString("API_PROVIDER"), rs.getString(
                                    "API_NAME"),
                                    rs.getString("API_VERSION"));
                            apiId.setUuid(rs.getString("API_UUID"));
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

                            if (migrationEnabled == null) {
                                try (PreparedStatement policiesStatement = connection.
                                        prepareStatement(
                                                SQLConstants.OperationPolicyConstants.GET_OPERATION_POLICIES_BY_URI_TEMPLATE_ID)) {
                                    policiesStatement.setInt(1, uriTemplateId);
                                    try (ResultSet policiesResult = policiesStatement.executeQuery()) {
                                        List<OperationPolicy> operationPolicies = new ArrayList<>();
                                        while (policiesResult.next()) {
                                            OperationPolicy policy = populateOperationPolicyWithRS(policiesResult);
                                            operationPolicies.add(policy);
                                        }
                                        uriTemplate.setOperationPolicies(operationPolicies);
                                    }
                                }
                            }

                            resource.setUriTemplate(uriTemplate);
                            productResourceList.add(resource);
                        }
                    }
                }
            } else {
                String sql = SQLConstants.GET_RESOURCES_OF_PRODUCT_REVISION;
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setInt(1, productId);
                    ps.setString(2, productIdentifier.getUUID());
                    ps.setString(3, productIdentifier.getUUID());
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            APIProductResource resource = new APIProductResource();
                            APIIdentifier apiId = new APIIdentifier(rs.getString("API_PROVIDER"), rs.getString(
                                    "API_NAME"),
                                    rs.getString("API_VERSION"));
                            apiId.setUuid(rs.getString("API_UUID"));
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

                            if (migrationEnabled == null) {
                                try (PreparedStatement policiesStatement = connection.
                                        prepareStatement(
                                                SQLConstants.OperationPolicyConstants.GET_OPERATION_POLICIES_BY_URI_TEMPLATE_ID)) {
                                    policiesStatement.setInt(1, uriTemplateId);
                                    try (ResultSet policiesResult = policiesStatement.executeQuery()) {
                                        List<OperationPolicy> operationPolicies = new ArrayList<>();
                                        while (policiesResult.next()) {
                                            OperationPolicy policy = populateOperationPolicyWithRS(policiesResult);
                                            operationPolicies.add(policy);
                                        }
                                        uriTemplate.setOperationPolicies(operationPolicies);
                                    }
                                }
                            }

                            resource.setUriTemplate(uriTemplate);
                            productResourceList.add(resource);
                        }
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
     * @param uuid          Audit API ID
     * @param organization  Organization
     * @throws APIManagementException
     */
    public void addAuditApiMapping(APIIdentifier apiIdentifier, String uuid, String organization)
            throws APIManagementException {

        String query = SQLConstants.ADD_SECURITY_AUDIT_MAP_SQL;
        String apiUuid;
        if (apiIdentifier.getUUID() != null) {
            apiUuid = apiIdentifier.getUUID();
        } else {
            apiUuid = getUUIDFromIdentifier(apiIdentifier, organization);
        }
        try (Connection conn = APIMgtDBUtil.getConnection()) {
            int apiId = getAPIID(apiUuid, conn);
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
     * @param uuid API uuid to retrieve API ID
     * @throws APIManagementException
     */
    public String getAuditApiId(String uuid) throws APIManagementException {

        String query = SQLConstants.GET_AUDIT_UUID_SQL;
        String auditUuid = null;
        try (Connection conn = APIMgtDBUtil.getConnection()) {
            int apiId = getAPIID(uuid, conn);
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
     * @param apiUuid         API uuid to retrieve API ID
     * @param graphqlComplexityInfo GraphqlComplexityInfo object
     * @throws APIManagementException
     */
    public void addComplexityDetails(String apiUuid, GraphqlComplexityInfo graphqlComplexityInfo)
            throws APIManagementException {

        String addCustomComplexityDetails = SQLConstants.ADD_CUSTOM_COMPLEXITY_DETAILS_SQL;
        try (Connection conn = APIMgtDBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(addCustomComplexityDetails)) {
            conn.setAutoCommit(false);
            int apiId = getAPIID(apiUuid, conn);
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
     * @param uuid         API uuid object to retrieve API ID
     * @param graphqlComplexityInfo GraphqlComplexityInfo object
     * @throws APIManagementException
     */
    public void updateComplexityDetails(String uuid, GraphqlComplexityInfo graphqlComplexityInfo)
            throws APIManagementException {

        String updateCustomComplexityDetails = SQLConstants.UPDATE_CUSTOM_COMPLEXITY_DETAILS_SQL;
        try (Connection conn = APIMgtDBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(updateCustomComplexityDetails)) {
            conn.setAutoCommit(false);
            int apiId = getAPIID(uuid, conn);
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
     * @param uuid         API uuid to retrieve API ID
     * @param graphqlComplexityInfo GraphqlComplexityDetails object
     * @throws APIManagementException
     */
    public void addOrUpdateComplexityDetails(String uuid, GraphqlComplexityInfo graphqlComplexityInfo)
            throws APIManagementException {

        String getCustomComplexityDetailsQuery = SQLConstants.GET_CUSTOM_COMPLEXITY_DETAILS_SQL;
        try (Connection conn = APIMgtDBUtil.getConnection();
             PreparedStatement getCustomComplexityDetails = conn.prepareStatement(getCustomComplexityDetailsQuery)) {
            int apiId = getAPIID(uuid, conn);
            getCustomComplexityDetails.setInt(1, apiId);
            try (ResultSet rs1 = getCustomComplexityDetails.executeQuery()) {
                if (rs1.next()) {
                    updateComplexityDetails(uuid, graphqlComplexityInfo);
                } else {
                    addComplexityDetails(uuid, graphqlComplexityInfo);
                }
            }
        } catch (SQLException ex) {
            handleException("Error while updating custom complexity details: ", ex);
        }
    }

    /**
     * Get custom complexity details for a particular API
     *
     * @param uuid API UUID to retrieve API ID
     * @return info about the complexity details
     * @throws APIManagementException
     */
    public GraphqlComplexityInfo getComplexityDetails(String uuid) throws APIManagementException {

        GraphqlComplexityInfo graphqlComplexityInfo = new GraphqlComplexityInfo();
        String getCustomComplexityDetailsQuery = SQLConstants.GET_CUSTOM_COMPLEXITY_DETAILS_SQL;
        List<CustomComplexityDetails> customComplexityDetailsList = new ArrayList<CustomComplexityDetails>();
        try (Connection conn = APIMgtDBUtil.getConnection();
             PreparedStatement getCustomComplexityDetails = conn.prepareStatement(getCustomComplexityDetailsQuery)) {
            int apiId = getAPIID(uuid, conn);
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
     * @throws APIManagementException if an error occurs when retrieving a bot detection alert subscription
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
     * @param tenantId     tenant id of the jwt subject.
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
            } catch (SQLException e) {
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
     *
     * @throws APIManagementException
     */
    public void removeExpiredJWTs() throws APIManagementException {

        String deleteQuery = SQLConstants.RevokedJWTConstants.DELETE_REVOKED_JWT;
        try (Connection connection = APIMgtDBUtil.getConnection(); PreparedStatement ps =
                connection.prepareStatement(deleteQuery)) {
            connection.setAutoCommit(false);
            ps.setLong(1, System.currentTimeMillis());
            ps.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            handleException("Error while deleting expired JWTs from revoke table.", e);
        }
    }

    /**
     * Adds an API category
     *
     * @param category      Category
     * @param organization  Organization
     * @return Category
     */
    public APICategory addCategory(APICategory category, String organization) throws APIManagementException {

        String uuid = UUID.randomUUID().toString();
        category.setId(uuid);
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQLConstants.ADD_CATEGORY_SQL)) {
            statement.setString(1, uuid);
            statement.setString(2, category.getName());
            statement.setString(3, category.getDescription());
            statement.setString(4, organization);
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
            statement.setString(3, apiCategory.getOrganization());
            statement.setString(4, apiCategory.getId());
            statement.execute();
        } catch (SQLException e) {
            handleException("Failed to update API Category : " + apiCategory.getName() + " of tenant " +
                    APIUtil.getTenantDomainFromTenantId(apiCategory.getTenantID()), e);
        }
    }

    /**
     * Get all available API categories of the organization
     * @param organization
     * @return
     * @throws APIManagementException
     */
    public List<APICategory> getAllCategories(String organization) throws APIManagementException {

        List<APICategory> categoriesList = new ArrayList<>();
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(SQLConstants.GET_CATEGORIES_BY_ORGANIZATION_SQL)) {
            statement.setString(1, organization);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    String id = rs.getString("UUID");
                    String name = rs.getString("NAME");
                    String description = rs.getString("DESCRIPTION");

                    APICategory category = new APICategory();
                    category.setId(id);
                    category.setName(name);
                    category.setDescription(description);
                    category.setOrganization(organization);

                    categoriesList.add(category);
                }
            }
        } catch (SQLException e) {
            handleException("Failed to retrieve API categories for organization " + organization, e);
        }
        return categoriesList;
    }

    /**
     * Checks whether the given category name is already available under given tenant domain with any UUID other than
     * the given UUID
     *
     * @param categoryName
     * @param uuid
     * @param organization
     * @return
     */
    public boolean isAPICategoryNameExists(String categoryName, String uuid, String organization) throws APIManagementException {

        String sql = SQLConstants.IS_API_CATEGORY_NAME_EXISTS;
        if (uuid != null) {
            sql = SQLConstants.IS_API_CATEGORY_NAME_EXISTS_FOR_ANOTHER_UUID;
        }
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, categoryName);
            statement.setString(2, organization);
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
                apiCategory.setId(apiCategoryID);
            }
        } catch (SQLException e) {
            handleException("Failed to fetch API category : " + apiCategoryID, e);
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
            handleException("Failed to add userID for " + userName, e);
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
            handleException("Failed to fetch user ID for " + userName, e);
        }
        return userID;
    }

    /**
     * Get names of the tiers which has bandwidth as the quota type
     *
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
                    InputStream targetStream = rs.getBinaryStream("WF_METADATA");
                    InputStream propertiesTargetStream = rs.getBinaryStream("WF_PROPERTIES");

                    if (targetStream != null) {
                        String metadata = APIMgtDBUtil.getStringFromInputStream(targetStream);
                        Gson metadataGson = new Gson();
                        JSONObject metadataJson = metadataGson.fromJson(metadata, JSONObject.class);
                        workflow.setMetadata(metadataJson);
                    } else {
                        JSONObject metadataJson = new JSONObject();
                        workflow.setMetadata(metadataJson);
                    }

                    if (propertiesTargetStream != null) {
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

    /**
     * Return the existing versions for the given api name for the provider
     *
     * @param apiName     api name
     * @param apiProvider provider
     * @param organization identifier of the organization
     * @return set version
     * @throws APIManagementException
     */
    public Set<String> getAPIVersions(String apiName, String apiProvider, String organization) throws APIManagementException {

        Set<String> versions = new HashSet<String>();

        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQLConstants.GET_API_VERSIONS)) {
            statement.setString(1, APIUtil.replaceEmailDomainBack(apiProvider));
            statement.setString(2, apiName);
            statement.setString(3, organization);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                versions.add(resultSet.getString("API_VERSION"));
            }
        } catch (SQLException e) {
            handleException("Error while retrieving versions for api " + apiName + " for the provider " + apiProvider,
                    e);
        }
        return versions;
    }

    /**
     * Return ids of the versions for the given name for the given provider
     *
     * @param apiName     api name
     * @param apiProvider provider
     * @return set ids
     * @throws APIManagementException
     */
    public List<API> getAllAPIVersions(String apiName, String apiProvider) throws APIManagementException {

        List<API> apiVersions = new ArrayList<API>();

        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQLConstants.GET_API_VERSIONS_UUID)) {
            statement.setString(1, APIUtil.replaceEmailDomainBack(apiProvider));
            statement.setString(2, apiName);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String version = resultSet.getString("API_VERSION");
                String status = resultSet.getString("STATUS");
                String versionTimestamp = resultSet.getString("VERSION_COMPARABLE");
                String context = resultSet.getString("CONTEXT");
                String contextTemplate = resultSet.getString("CONTEXT_TEMPLATE");

                String uuid = resultSet.getString("API_UUID");
                if (APIConstants.API_PRODUCT.equals(resultSet.getString("API_TYPE"))) {
                    // skip api products
                    continue;
                }
                API api = new API(new APIIdentifier(apiProvider, apiName,
                        version, uuid));
                api.setUuid(uuid);
                api.setStatus(status);
                api.setVersionTimestamp(versionTimestamp);
                api.setContext(context);
                api.setContextTemplate(contextTemplate);
                apiVersions.add(api);
            }
        } catch (SQLException e) {
            handleException("Error while retrieving versions for api " + apiName + " for the provider " + apiProvider,
                    e);
        }
        return apiVersions;
    }

    /**
     * Get count of the revisions created for a particular API.
     *
     * @return revision count
     * @throws APIManagementException if an error occurs while retrieving revision count
     */
    public int getRevisionCountByAPI(String apiUUID) throws APIManagementException {

        int count = 0;
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection
                     .prepareStatement(SQLConstants.APIRevisionSqlConstants.GET_REVISION_COUNT_BY_API_UUID)) {
            statement.setString(1, apiUUID);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    count = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            handleException("Failed to get revisions count for API UUID: " + apiUUID, e);
        }
        return count;
    }

    /**
     * Get most recent revision id of the revisions created for a particular API.
     *
     * @return revision id
     * @throws APIManagementException if an error occurs while retrieving revision id
     */
    public int getMostRecentRevisionId(String apiUUID) throws APIManagementException {

        int revisionId = 0;
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection
                     .prepareStatement(SQLConstants.APIRevisionSqlConstants.GET_MOST_RECENT_REVISION_ID)) {
            statement.setString(1, apiUUID);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    revisionId = rs.getInt("REVISIONS_CREATED");
                }
            }
        } catch (SQLException e) {
            handleException("Failed to get most recent revision ID for API UUID: " + apiUUID, e);
        }
        return revisionId;
    }

    /**
     * Get the latest revision UUID from the revision list for a given API
     *
     * @param apiUUID UUID of the API
     * @return UUID of the revision
     * @throws APIManagementException if an error occurs while retrieving revision details
     */
    public String getLatestRevisionUUID(String apiUUID) throws APIManagementException {

        String revisionUUID = null;
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = (connection.getMetaData().getDriverName().contains("MS SQL") ||
                     connection.getMetaData().getDriverName().contains("Microsoft") ?
                     connection.prepareStatement(SQLConstants.APIRevisionSqlConstants.GET_MOST_RECENT_REVISION_UUID_MSSQL) :
                     (connection.getMetaData().getDriverName().contains("MySQL") || connection.getMetaData().getDriverName()
                             .contains("H2")) ?
                             connection.prepareStatement(SQLConstants.APIRevisionSqlConstants.GET_MOST_RECENT_REVISION_UUID_MYSQL) :
                             connection.prepareStatement(SQLConstants.APIRevisionSqlConstants.GET_MOST_RECENT_REVISION_UUID))) {
            statement.setString(1, apiUUID);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    revisionUUID = rs.getString(1);
                }
            }
        } catch (SQLException e) {
            handleException("Failed to get the latest revision for api ID: " + apiUUID, e);
        }
        return revisionUUID;
    }

    /**
     * Adds an API revision record to the database
     *
     * @param apiRevision content of the revision
     * @throws APIManagementException if an error occurs when adding a new API revision
     */
    public void addAPIRevision(APIRevision apiRevision) throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            try {
                connection.setAutoCommit(false);
                // Adding to AM_REVISION table
                PreparedStatement statement = connection
                        .prepareStatement(SQLConstants.APIRevisionSqlConstants.ADD_API_REVISION);
                statement.setInt(1, apiRevision.getId());
                statement.setString(2, apiRevision.getApiUUID());
                statement.setString(3, apiRevision.getRevisionUUID());
                statement.setString(4, apiRevision.getDescription());
                statement.setString(5, apiRevision.getCreatedBy());
                statement.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
                statement.executeUpdate();

                // Retrieve API ID
                APIIdentifier apiIdentifier = APIUtil.getAPIIdentifierFromUUID(apiRevision.getApiUUID());
                int apiId = getAPIID(apiRevision.getApiUUID(), connection);
                int tenantId = APIUtil.getTenantId(APIUtil.replaceEmailDomainBack(apiIdentifier.getProviderName()));
                String tenantDomain = APIUtil.getTenantDomainFromTenantId(tenantId);

                // Adding to AM_API_URL_MAPPING table
                PreparedStatement getURLMappingsStatement = connection
                        .prepareStatement(SQLConstants.APIRevisionSqlConstants.GET_URL_MAPPINGS_WITH_SCOPE_AND_PRODUCT_ID);
                getURLMappingsStatement.setInt(1, apiId);
                List<URITemplate> urlMappingList = new ArrayList<>();
                try (ResultSet rs = getURLMappingsStatement.executeQuery()) {
                    while (rs.next()) {
                        String script = null;
                        URITemplate uriTemplate = new URITemplate();
                        uriTemplate.setHTTPVerb(rs.getString(1));
                        uriTemplate.setAuthType(rs.getString(2));
                        uriTemplate.setUriTemplate(rs.getString(3));
                        uriTemplate.setThrottlingTier(rs.getString(4));
                        InputStream mediationScriptBlob = rs.getBinaryStream(5);
                        if (mediationScriptBlob != null) {
                            script = APIMgtDBUtil.getStringFromInputStream(mediationScriptBlob);
                        }
                        uriTemplate.setMediationScript(script);
                        if (!StringUtils.isEmpty(rs.getString(6))) {
                            Scope scope = new Scope();
                            scope.setKey(rs.getString(6));
                            uriTemplate.setScope(scope);
                        }
                        if (rs.getInt(7) != 0) {
                            // Adding product id to uri template id just to store value
                            uriTemplate.setId(rs.getInt(7));
                        }
                        urlMappingList.add(uriTemplate);
                    }
                }

                Map<String, URITemplate> uriTemplateMap = new HashMap<>();
                for (URITemplate urlMapping : urlMappingList) {
                    if (urlMapping.getScope() != null) {
                        URITemplate urlMappingNew = urlMapping;
                        URITemplate urlMappingExisting = uriTemplateMap.get(urlMapping.getUriTemplate()
                                + urlMapping.getHTTPVerb());
                        if (urlMappingExisting != null && urlMappingExisting.getScopes() != null) {
                            if (!urlMappingExisting.getScopes().contains(urlMapping.getScope())) {
                                urlMappingExisting.setScopes(urlMapping.getScope());
                                uriTemplateMap.put(urlMappingExisting.getUriTemplate() + urlMappingExisting.getHTTPVerb(),
                                        urlMappingExisting);
                            }
                        } else {
                            urlMappingNew.setScopes(urlMapping.getScope());
                            uriTemplateMap.put(urlMappingNew.getUriTemplate() + urlMappingNew.getHTTPVerb(),
                                    urlMappingNew);
                        }
                    } else if (urlMapping.getId() != 0) {
                        URITemplate urlMappingExisting = uriTemplateMap.get(urlMapping.getUriTemplate()
                                + urlMapping.getHTTPVerb());
                        if (urlMappingExisting == null) {
                            uriTemplateMap.put(urlMapping.getUriTemplate() + urlMapping.getHTTPVerb(), urlMapping);
                        }
                    } else {
                        uriTemplateMap.put(urlMapping.getUriTemplate() + urlMapping.getHTTPVerb(), urlMapping);
                    }
                }

                String migrate =  System.getProperty(APIConstants.MIGRATE);
                if (migrate == null) {
                    setOperationPoliciesToURITemplatesMap(connection, apiRevision.getApiUUID(), uriTemplateMap);
                }

                PreparedStatement insertURLMappingsStatement = connection
                        .prepareStatement(SQLConstants.APIRevisionSqlConstants.INSERT_URL_MAPPINGS);
                for (URITemplate urlMapping : uriTemplateMap.values()) {
                    insertURLMappingsStatement.setInt(1, apiId);
                    insertURLMappingsStatement.setString(2, urlMapping.getHTTPVerb());
                    insertURLMappingsStatement.setString(3, urlMapping.getAuthType());
                    insertURLMappingsStatement.setString(4, urlMapping.getUriTemplate());
                    insertURLMappingsStatement.setString(5, urlMapping.getThrottlingTier());
                    insertURLMappingsStatement.setString(6, apiRevision.getRevisionUUID());
                    insertURLMappingsStatement.addBatch();
                }
                insertURLMappingsStatement.executeBatch();

                // Add to AM_API_RESOURCE_SCOPE_MAPPING table and to AM_API_PRODUCT_MAPPING
                PreparedStatement getRevisionedURLMappingsStatement = connection
                        .prepareStatement(SQLConstants.APIRevisionSqlConstants.GET_REVISIONED_URL_MAPPINGS_ID);
                PreparedStatement insertScopeResourceMappingStatement = connection
                        .prepareStatement(SQLConstants.APIRevisionSqlConstants.INSERT_SCOPE_RESOURCE_MAPPING);
                PreparedStatement insertProductResourceMappingStatement = connection
                        .prepareStatement(SQLConstants.APIRevisionSqlConstants.INSERT_PRODUCT_RESOURCE_MAPPING);
                PreparedStatement insertOperationPolicyMappingStatement = connection
                        .prepareStatement(SQLConstants.OperationPolicyConstants.ADD_API_OPERATION_POLICY_MAPPING);

                Map<String, String> clonedPolicyMap = new HashMap<>();
                List<ClonePolicyMetadataDTO> toBeClonedPolicyDetails = new ArrayList<>();
                for (URITemplate urlMapping : uriTemplateMap.values()) {
                    getRevisionedURLMappingsStatement.setInt(1, apiId);
                    getRevisionedURLMappingsStatement.setString(2, apiRevision.getRevisionUUID());
                    getRevisionedURLMappingsStatement.setString(3, urlMapping.getHTTPVerb());
                    getRevisionedURLMappingsStatement.setString(4, urlMapping.getAuthType());
                    getRevisionedURLMappingsStatement.setString(5, urlMapping.getUriTemplate());
                    getRevisionedURLMappingsStatement.setString(6, urlMapping.getThrottlingTier());
                    try (ResultSet rs = getRevisionedURLMappingsStatement.executeQuery()) {
                        while (rs.next()) {
                            if (urlMapping.getScopes() != null) {
                                for (Scope scope : urlMapping.getScopes()) {
                                    insertScopeResourceMappingStatement.setString(1, scope.getKey());
                                    insertScopeResourceMappingStatement.setInt(2, rs.getInt(1));
                                    insertScopeResourceMappingStatement.setInt(3, tenantId);
                                    insertScopeResourceMappingStatement.addBatch();
                                }
                            }

                            if (urlMapping.getId() != 0) {
                                insertProductResourceMappingStatement.setInt(1, urlMapping.getId());
                                insertProductResourceMappingStatement.setInt(2, rs.getInt(1));
                                insertProductResourceMappingStatement.addBatch();
                            }

                            if (urlMapping.getOperationPolicies().size() > 0) {
                                for (OperationPolicy policy : urlMapping.getOperationPolicies()) {
                                    if (!clonedPolicyMap.keySet().contains(policy.getPolicyId())) {
                                        // Since we are creating a new revision, if the policy is not found in the policy map,
                                        // we have to clone the policy.
                                        String clonedPolicyId = UUID.randomUUID().toString();
                                        ClonePolicyMetadataDTO toBeClonedSinglePolicyData = new ClonePolicyMetadataDTO();
                                        toBeClonedSinglePolicyData.setClonedPolicyUUID(clonedPolicyId);
                                        toBeClonedSinglePolicyData.setCurrentPolicyUUID(policy.getPolicyId());
                                        toBeClonedSinglePolicyData.setApiUUID(apiRevision.getApiUUID());
                                        toBeClonedSinglePolicyData.setRevisionUUID(apiRevision.getRevisionUUID());
                                        toBeClonedPolicyDetails.add(toBeClonedSinglePolicyData);

                                        // policy ID is stored in a map as same policy can be applied to multiple operations
                                        // and we only need to create the policy once.
                                        clonedPolicyMap.put(policy.getPolicyId(), clonedPolicyId);
                                    }

                                    Gson gson = new Gson();
                                    String paramJSON = gson.toJson(policy.getParameters());

                                    insertOperationPolicyMappingStatement.setInt(1, rs.getInt(1));
                                    insertOperationPolicyMappingStatement.setString(2, clonedPolicyMap.get(policy.getPolicyId()));
                                    insertOperationPolicyMappingStatement.setString(3, policy.getDirection());
                                    insertOperationPolicyMappingStatement.setString(4, paramJSON);
                                    insertOperationPolicyMappingStatement.setInt(5, policy.getOrder());
                                    insertOperationPolicyMappingStatement.addBatch();
                                }
                            }
                        }
                    }
                }
                insertScopeResourceMappingStatement.executeBatch();
                insertProductResourceMappingStatement.executeBatch();

                for (ClonePolicyMetadataDTO toBeClonedPolicyData : toBeClonedPolicyDetails) {
                    cloneAPISpecificPoliciesForRevisioning(connection, toBeClonedPolicyData.getCurrentPolicyUUID(),
                            toBeClonedPolicyData.getClonedPolicyUUID(), toBeClonedPolicyData.getApiUUID(),
                            toBeClonedPolicyData.getRevisionUUID(), tenantDomain);
                }
                insertOperationPolicyMappingStatement.executeBatch();

                // Adding to AM_API_CLIENT_CERTIFICATE
                String getClientCertificatesQuery = SQLConstants.APIRevisionSqlConstants.GET_CLIENT_CERTIFICATES;
                String driverName = connection.getMetaData().getDriverName();
                if (driverName.contains("Oracle")) {
                    getClientCertificatesQuery = SQLConstants.APIRevisionSqlConstants.GET_CLIENT_CERTIFICATES_ORACLE_SQL;
                } else if (driverName.contains("MS SQL") || driverName.contains("Microsoft")) {
                    getClientCertificatesQuery = SQLConstants.APIRevisionSqlConstants.GET_CLIENT_CERTIFICATES_MSSQL;
                }

                PreparedStatement getClientCertificatesStatement = connection.prepareStatement(getClientCertificatesQuery);
                getClientCertificatesStatement.setInt(1, apiId);
                List<ClientCertificateDTO> clientCertificateDTOS = new ArrayList<>();
                try (ResultSet rs = getClientCertificatesStatement.executeQuery()) {
                    while (rs.next()) {
                        ClientCertificateDTO clientCertificateDTO = new ClientCertificateDTO();
                        clientCertificateDTO.setAlias(rs.getString(1));
                        clientCertificateDTO.setCertificate(APIMgtDBUtil.getStringFromInputStream(rs.getBinaryStream(2)));
                        clientCertificateDTO.setTierName(rs.getString(3));
                        clientCertificateDTOS.add(clientCertificateDTO);
                    }
                }
                PreparedStatement insertClientCertificateStatement = connection
                        .prepareStatement(SQLConstants.APIRevisionSqlConstants.INSERT_CLIENT_CERTIFICATES);
                for (ClientCertificateDTO clientCertificateDTO : clientCertificateDTOS) {
                    insertClientCertificateStatement.setInt(1, tenantId);
                    insertClientCertificateStatement.setString(2, clientCertificateDTO.getAlias());
                    insertClientCertificateStatement.setInt(3, apiId);
                    insertClientCertificateStatement.setBinaryStream(4,
                            getInputStream(clientCertificateDTO.getCertificate()));
                    insertClientCertificateStatement.setBoolean(5, false);
                    insertClientCertificateStatement.setString(6, clientCertificateDTO.getTierName());
                    insertClientCertificateStatement.setString(7, apiRevision.getRevisionUUID());
                    insertClientCertificateStatement.addBatch();
                }
                insertClientCertificateStatement.executeBatch();

                // Adding to AM_GRAPHQL_COMPLEXITY table
                PreparedStatement getGraphQLComplexityStatement = connection
                        .prepareStatement(SQLConstants.APIRevisionSqlConstants.GET_GRAPHQL_COMPLEXITY);
                List<CustomComplexityDetails> customComplexityDetailsList = new ArrayList<>();
                getGraphQLComplexityStatement.setInt(1, apiId);
                try (ResultSet rs1 = getGraphQLComplexityStatement.executeQuery()) {
                    while (rs1.next()) {
                        CustomComplexityDetails customComplexityDetails = new CustomComplexityDetails();
                        customComplexityDetails.setType(rs1.getString("TYPE"));
                        customComplexityDetails.setField(rs1.getString("FIELD"));
                        customComplexityDetails.setComplexityValue(rs1.getInt("COMPLEXITY_VALUE"));
                        customComplexityDetailsList.add(customComplexityDetails);
                    }
                }

                PreparedStatement insertGraphQLComplexityStatement = connection
                        .prepareStatement(SQLConstants.APIRevisionSqlConstants.INSERT_GRAPHQL_COMPLEXITY);
                for (CustomComplexityDetails customComplexityDetails : customComplexityDetailsList) {
                    insertGraphQLComplexityStatement.setString(1, UUID.randomUUID().toString());
                    insertGraphQLComplexityStatement.setInt(2, apiId);
                    insertGraphQLComplexityStatement.setString(3, customComplexityDetails.getType());
                    insertGraphQLComplexityStatement.setString(4, customComplexityDetails.getField());
                    insertGraphQLComplexityStatement.setInt(5, customComplexityDetails.getComplexityValue());
                    insertGraphQLComplexityStatement.setString(6, apiRevision.getRevisionUUID());
                    insertGraphQLComplexityStatement.addBatch();
                }
                insertGraphQLComplexityStatement.executeBatch();
                updateLatestRevisionNumber(connection, apiRevision.getApiUUID(), apiRevision.getId());
                addAPIRevisionMetaData(connection, apiRevision.getApiUUID(), apiRevision.getRevisionUUID());
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                handleException("Failed to add API Revision entry of API UUID "
                        + apiRevision.getApiUUID(), e);
            }
        } catch (SQLException e) {
            handleException("Failed to add API Revision entry of API UUID "
                    + apiRevision.getApiUUID(), e);
        }
    }

    /**
     * To get the input stream from string.
     *
     * @param value : Relevant string that need to be converted to input stream.
     * @return input stream.
     */
    private InputStream getInputStream(String value) {

        byte[] cert = value.getBytes(StandardCharsets.UTF_8);
        return new ByteArrayInputStream(cert);
    }

    /**
     * Get revision details by providing revision UUID
     *
     * @return revision object
     * @throws APIManagementException if an error occurs while retrieving revision details
     */
    public APIRevision getRevisionByRevisionUUID(String revisionUUID) throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            return getRevisionByRevisionUUID(connection, revisionUUID);
        } catch (SQLException e) {
            handleException("Failed to get revision details for revision UUID: " + revisionUUID, e);
        }
        return null;
    }

    /**
     * Get revision UUID providing revision number
     *
     * @param revisionNum Revision number
     * @param apiUUID     UUID of the API
     * @return UUID of the revision
     * @throws APIManagementException if an error occurs while retrieving revision details
     */
    public String getRevisionUUID(String revisionNum, String apiUUID) throws APIManagementException {

        String revisionUUID = null;
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection
                     .prepareStatement(SQLConstants.APIRevisionSqlConstants.GET_REVISION_UUID)) {
            statement.setString(1, apiUUID);
            statement.setInt(2, Integer.parseInt(revisionNum));
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    revisionUUID = rs.getString(1);
                }
            }
        } catch (SQLException e) {
            handleException("Failed to get revision UUID for Revision " + revisionNum, e);
        }
        return revisionUUID;
    }

    /**
     * Get revision UUID providing revision number and organization
     *
     * @param revisionNum   Revision number
     * @param apiUUID       UUID of the API
     * @param organization  organization ID of the API
     * @return UUID of the revision
     * @throws APIManagementException if an error occurs while retrieving revision details
     */
    public String getRevisionUUIDByOrganization(String revisionNum, String apiUUID, String organization) throws APIManagementException {

        String revisionUUID = null;
        String sql = SQLConstants.APIRevisionSqlConstants.GET_REVISION_UUID_BY_ORGANIZATION;
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection
                     .prepareStatement(sql)) {
            statement.setString(1, apiUUID);
            statement.setInt(2, Integer.parseInt(revisionNum));
            statement.setString(3, organization);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    revisionUUID = rs.getString(1);
                }
            }
        } catch (SQLException e) {
            handleException("Failed to get revision UUID for Revision " + revisionNum, e);
        }
        return revisionUUID;
    }

    /**
     * Get the earliest revision UUID from the revision list for a given API
     *
     * @param apiUUID UUID of the API
     * @return UUID of the revision
     * @throws APIManagementException if an error occurs while retrieving revision details
     */
    public String getEarliestRevision(String apiUUID) throws APIManagementException {
        String revisionUUID = null;
        try (Connection connection = APIMgtDBUtil.getConnection();
                PreparedStatement statement = (
                        connection.getMetaData().getDriverName().contains("MS SQL") || connection.getMetaData()
                                .getDriverName().contains("Microsoft") ?
                                connection.prepareStatement(
                                        SQLConstants.APIRevisionSqlConstants.GET_EARLIEST_REVISION_ID_MSSQL) :
                                (connection.getMetaData().getDriverName().contains("MySQL") || connection.getMetaData()
                                        .getDriverName().contains("H2")) ?
                                        connection.prepareStatement(
                                                SQLConstants.APIRevisionSqlConstants.GET_EARLIEST_REVISION_ID_MYSQL) :
                                        connection.prepareStatement(
                                                SQLConstants.APIRevisionSqlConstants.GET_EARLIEST_REVISION_ID))) {
            statement.setString(1, apiUUID);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    revisionUUID = rs.getString(1);
                }
            }
        } catch (SQLException e) {
            handleException("Failed to get the earliest revision for api ID: " + apiUUID, e);
        }
        return revisionUUID;
    }

    /**
     * Get revision details by providing revision UUID
     *
     * @return revisions List object
     * @throws APIManagementException if an error occurs while retrieving revision details
     */
    public List<APIRevision> getRevisionsListByAPIUUID(String apiUUID) throws APIManagementException {

        List<APIRevision> revisionList = new ArrayList<>();
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection
                     .prepareStatement(SQLConstants.APIRevisionSqlConstants.GET_REVISIONS_BY_API_UUID)) {
            statement.setString(1, apiUUID);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    APIRevision apiRevision = new APIRevision();
                    apiRevision.setId(rs.getInt("ID"));
                    apiRevision.setApiUUID(apiUUID);
                    apiRevision.setRevisionUUID(rs.getString("REVISION_UUID"));
                    apiRevision.setDescription(rs.getString("DESCRIPTION"));
                    apiRevision.setCreatedTime(rs.getString("CREATED_TIME"));
                    apiRevision.setCreatedBy(rs.getString("CREATED_BY"));
                    apiRevision.setApiRevisionDeploymentList(new ArrayList<>());
                    revisionList.add(apiRevision);
                }
            }
        } catch (SQLException e) {
            handleException("Failed to get revision details for API UUID: " + apiUUID, e);
        }

        // adding deployment info to revision objects
        List<APIRevisionDeployment> allAPIRevisionDeploymentList = getAPIRevisionDeploymentByApiUUID(apiUUID);

        for(APIRevisionDeployment apiRevisionDeployment : allAPIRevisionDeploymentList) {
            for (APIRevision apiRevision : revisionList) {
                if (apiRevision.getRevisionUUID().equals(apiRevisionDeployment.getRevisionUUID())) {
                    apiRevision.getApiRevisionDeploymentList().add(apiRevisionDeployment);
                    break;
                }
            }
        }

        return revisionList;
    }

    /**
     * Get a provided api uuid is in the revision db table
     *
     * @return String apiUUID
     * @throws APIManagementException if an error occurs while checking revision table
     */
    public APIRevision checkAPIUUIDIsARevisionUUID(String apiUUID) throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection
                     .prepareStatement(SQLConstants.APIRevisionSqlConstants.GET_REVISION_APIID_BY_REVISION_UUID)) {
            statement.setString(1, apiUUID);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    APIRevision apiRevision = new APIRevision();
                    apiRevision.setApiUUID(rs.getString("API_UUID"));
                    apiRevision.setId(rs.getInt("ID"));
                    apiRevision.setRevisionUUID(apiUUID);
                    return apiRevision;
                }
            }
        } catch (SQLException e) {
            handleException("Failed to search UUID: " + apiUUID + " in the revision db table", e);
        }
        return null;
    }

    /**
     * Adds an API revision Deployment mapping record to the database
     *
     * @param apiRevisionId          uuid of the revision
     * @param apiRevisionDeployments content of the revision deployment mapping objects
     * @throws APIManagementException if an error occurs when adding a new API revision
     */
    public void addAPIRevisionDeployment(String apiRevisionId, List<APIRevisionDeployment> apiRevisionDeployments)
            throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            try {
                connection.setAutoCommit(false);
                // Adding to AM_DEPLOYMENT_REVISION_MAPPING table
                PreparedStatement statement = connection
                        .prepareStatement(SQLConstants.APIRevisionSqlConstants.ADD_API_REVISION_DEPLOYMENT_MAPPING);
                for (APIRevisionDeployment apiRevisionDeployment : apiRevisionDeployments) {
                    String envName = apiRevisionDeployment.getDeployment();
                    String vhost = apiRevisionDeployment.getVhost();
                    // set VHost as null, if it is the default vhost of the read only environment
                    statement.setString(1, apiRevisionDeployment.getDeployment());
                    statement.setString(2, VHostUtils.resolveIfDefaultVhostToNull(envName, vhost));
                    statement.setString(3, apiRevisionId);
                    statement.setBoolean(4, apiRevisionDeployment.isDisplayOnDevportal());
                    statement.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
                    statement.addBatch();
                }
                statement.executeBatch();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                handleException("Failed to add API Revision Deployment Mapping entry for Revision UUID "
                        + apiRevisionId, e);
            }
        } catch (SQLException e) {
            handleException("Failed to add API Revision Deployment Mapping entry for Revision UUID " + apiRevisionId,
                    e);
        }
    }

    /**
     * Adds an deployed API revision to the database
     *
     * @param deployedAPIRevisionList content of the revision deployment mapping objects
     * @throws APIManagementException if an error occurs when adding a new API revision
     */
    public void addDeployedAPIRevision(String apiRevisionId, List<DeployedAPIRevision> deployedAPIRevisionList)
            throws APIManagementException {
        if (deployedAPIRevisionList.size() > 0) {
            try (Connection connection = APIMgtDBUtil.getConnection()) {
                connection.setAutoCommit(false);
                // Adding to AM_DEPLOYED_REVISION table
                try (PreparedStatement statement = connection
                            .prepareStatement(SQLConstants.APIRevisionSqlConstants.ADD_DEPLOYED_API_REVISION)) {
                    for (DeployedAPIRevision deployedAPIRevision : deployedAPIRevisionList) {
                        String envName = deployedAPIRevision.getDeployment();
                        String vhost = deployedAPIRevision.getVhost();
                        // set VHost as null, if it is the default vhost of the read only environment
                        statement.setString(1, deployedAPIRevision.getDeployment());
                        statement.setString(2, VHostUtils.resolveIfDefaultVhostToNull(envName, vhost));
                        statement.setString(3, apiRevisionId);
                        statement.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
                        statement.addBatch();
                    }
                    statement.executeBatch();
                    connection.commit();
                } catch (SQLException e) {
                    connection.rollback();
                    // handle concurrent db entry update. Fix duplicate primary key issue.
                    if (e.getMessage().toLowerCase().contains("primary key violation") ||
                            e.getMessage().toLowerCase().contains("duplicate entry") ||
                            e.getMessage().contains("Violation of PRIMARY KEY constraint")) {
                        log.warn("Duplicate entries detected for Revision UUID " + apiRevisionId +
                                " while adding deployed API revisions", e);
                        throw new APIManagementException("Failed to add deployed API Revision for Revision UUID "
                                + apiRevisionId,  e, ExceptionCodes.REVISION_ALREADY_DEPLOYED);
                    } else {
                        handleException("Failed to add deployed API Revision for Revision UUID "
                                + apiRevisionId, e);
                    }
                }
            } catch (SQLException e) {
                handleException("Failed to add deployed API Revision for Revision UUID " + apiRevisionId,
                        e);
            }
        }
    }

    /**
     * Get APIRevisionDeployment details by providing deployment name and revision uuid
     *
     * @return APIRevisionDeployment object
     * @throws APIManagementException if an error occurs while retrieving revision details
     */
    public APIRevisionDeployment getAPIRevisionDeploymentByNameAndRevsionID(String name, String revisionId) throws APIManagementException {

        APIRevisionDeployment apiRevisionDeployment = new APIRevisionDeployment();
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection
                     .prepareStatement(SQLConstants.
                             APIRevisionSqlConstants.GET_API_REVISION_DEPLOYMENT_MAPPING_BY_NAME_AND_REVISION_UUID)) {
            statement.setString(1, name);
            statement.setString(2, revisionId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    String environmentName = rs.getString("NAME");
                    String vhost = rs.getString("VHOST");
                    apiRevisionDeployment.setDeployment(environmentName);
                    apiRevisionDeployment.setVhost(VHostUtils.resolveIfNullToDefaultVhost(environmentName, vhost));
                    apiRevisionDeployment.setRevisionUUID(rs.getString("REVISION_UUID"));
                    apiRevisionDeployment.setDisplayOnDevportal(rs.getBoolean("DISPLAY_ON_DEVPORTAL"));
                    apiRevisionDeployment.setDeployedTime(rs.getString("DEPLOYED_TIME"));
                }
            }
        } catch (SQLException e) {
            handleException("Failed to get API Revision deployment mapping details for deployment name: " + name, e);
        }
        return apiRevisionDeployment;
    }

    /**
     * Get APIRevisionDeployment details by providing revision uuid
     *
     * @return List<APIRevisionDeployment> object
     * @throws APIManagementException if an error occurs while retrieving revision deployment mapping details
     */
    public List<APIRevisionDeployment> getAPIRevisionDeploymentByRevisionUUID(String revisionUUID) throws APIManagementException {

        List<APIRevisionDeployment> apiRevisionDeploymentList = new ArrayList<>();
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection
                     .prepareStatement(SQLConstants.
                             APIRevisionSqlConstants.GET_API_REVISION_DEPLOYMENT_MAPPING_BY_REVISION_UUID)) {
            statement.setString(1, revisionUUID);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    APIRevisionDeployment apiRevisionDeployment = new APIRevisionDeployment();
                    String environmentName = rs.getString("NAME");
                    String vhost = rs.getString("VHOST");
                    apiRevisionDeployment.setDeployment(environmentName);
                    apiRevisionDeployment.setVhost(VHostUtils.resolveIfNullToDefaultVhost(environmentName, vhost));
                    apiRevisionDeployment.setRevisionUUID(rs.getString("REVISION_UUID"));
                    apiRevisionDeployment.setDisplayOnDevportal(rs.getBoolean("DISPLAY_ON_DEVPORTAL"));
                    apiRevisionDeployment.setDeployedTime(rs.getString("DEPLOYED_TIME"));
                    apiRevisionDeploymentList.add(apiRevisionDeployment);
                }
            }
        } catch (SQLException e) {
            handleException("Failed to get API Revision deployment mapping details for revision uuid: " +
                    revisionUUID, e);
        }
        return apiRevisionDeploymentList;
    }

    /**
     * Get APIRevisionDeployment details by providing API uuid
     *
     * @return List<APIRevisionDeployment> object
     * @throws APIManagementException if an error occurs while retrieving revision deployment mapping details
     */
    public List<APIRevisionDeployment> getAPIRevisionDeploymentByApiUUID(String apiUUID) throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            PreparedStatement statement;
            if (connection.getMetaData().getDriverName().contains("PostgreSQL")) {
                statement = connection
                        .prepareStatement(SQLConstants.
                                APIRevisionSqlConstants.GET_API_REVISION_DEPLOYMENTS_BY_API_UUID_POSTGRES);
            } else {
                statement = connection
                        .prepareStatement(SQLConstants.
                                APIRevisionSqlConstants.GET_API_REVISION_DEPLOYMENTS_BY_API_UUID);
            }
            statement.setString(1, apiUUID);
            try (ResultSet rs = statement.executeQuery()) {
                return APIMgtDBUtil.mergeRevisionDeploymentDTOs(rs);
            }
        } catch (SQLException e) {
            handleException("Failed to get API Revision deployment mapping details for api uuid: " +
                    apiUUID, e);
        }
        return new ArrayList<>();
    }

    /**
     * Get APIRevisionDeployment details by providing API uuid
     *
     * @return List<APIRevisionDeployment> object
     * @throws APIManagementException if an error occurs while retrieving revision deployment mapping details
     */
    private boolean isDeploymentAvailableByAPIUUID(Connection connection, String apiUUID) throws APIManagementException {

        try (PreparedStatement statement =
                     connection.prepareStatement(SQLConstants.APIRevisionSqlConstants.CHECK_API_REVISION_DEPLOYMENT_AVAILABILITY_BY_API_UUID)) {
            statement.setString(1, apiUUID);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return true;
                }
            }
        } catch (SQLException e) {
            handleException("Failed to get API Revision deployment mapping details for api uuid: " +
                    apiUUID, e);
        }
        return false;
    }

    /**
     * Get APIRevisionDeployment details by providing ApiUUID
     *
     * @return List<APIRevisionDeployment> object
     * @throws APIManagementException if an error occurs while retrieving revision deployment mapping details
     */
    public List<APIRevisionDeployment> getAPIRevisionDeploymentsByApiUUID(String apiUUID) throws APIManagementException {

        List<APIRevisionDeployment> apiRevisionDeploymentList = new ArrayList<>();
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection
                     .prepareStatement(SQLConstants.
                             APIRevisionSqlConstants.GET_API_REVISION_DEPLOYMENT_MAPPING_BY_API_UUID)) {
            statement.setString(1, apiUUID);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    APIRevisionDeployment apiRevisionDeployment = new APIRevisionDeployment();
                    String environmentName = rs.getString("NAME");
                    String vhost = rs.getString("VHOST");
                    apiRevisionDeployment.setDeployment(environmentName);
                    apiRevisionDeployment.setVhost(VHostUtils.resolveIfNullToDefaultVhost(environmentName, vhost));
                    apiRevisionDeployment.setRevisionUUID(rs.getString("REVISION_UUID"));
                    apiRevisionDeployment.setDisplayOnDevportal(rs.getBoolean("DISPLAY_ON_DEVPORTAL"));
                    apiRevisionDeployment.setDeployedTime(rs.getString("DEPLOYED_TIME"));
                    apiRevisionDeploymentList.add(apiRevisionDeployment);
                }
            }
        } catch (SQLException e) {
            handleException("Failed to get API Revision deployment mapping details for api uuid: " +
                    apiUUID, e);
        }
        return apiRevisionDeploymentList;
    }

    /**
     * Get DeployedAPIRevision details by providing ApiUUID
     *
     * @return List<DeployedAPIRevision> object
     * @throws APIManagementException if an error occurs while retrieving revision deployment mapping details
     */
    public List<DeployedAPIRevision> getDeployedAPIRevisionByApiUUID(String apiUUID) throws APIManagementException {

        List<DeployedAPIRevision> deployedAPIRevisionList = new ArrayList<>();
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection
                     .prepareStatement(SQLConstants.
                             APIRevisionSqlConstants.GET_DEPLOYED_REVISION_BY_API_UUID)) {
            statement.setString(1, apiUUID);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    DeployedAPIRevision deployedAPIRevision = new DeployedAPIRevision();
                    String environmentName = rs.getString("NAME");
                    String vhost = rs.getString("VHOST");
                    deployedAPIRevision.setDeployment(environmentName);
                    deployedAPIRevision.setVhost(VHostUtils.resolveIfNullToDefaultVhost(environmentName, vhost));
                    deployedAPIRevision.setRevisionUUID(rs.getString("REVISION_UUID"));
                    deployedAPIRevision.setDeployedTime(rs.getString("DEPLOYED_TIME"));
                    deployedAPIRevisionList.add(deployedAPIRevision);
                }
            }
        } catch (SQLException e) {
            handleException("Failed to get deployed API Revision details for api uuid: " +
                    apiUUID, e);
        }
        return deployedAPIRevisionList;
    }

    /**
     * Remove an API revision Deployment mapping record to the database
     *
     * @param apiRevisionId          uuid of the revision
     * @param apiRevisionDeployments content of the revision deployment mapping objects
     * @throws APIManagementException if an error occurs when adding a new API revision
     */
    public void removeAPIRevisionDeployment(String apiRevisionId, List<APIRevisionDeployment> apiRevisionDeployments)
            throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            try {
                connection.setAutoCommit(false);
                // Remove an entry from AM_DEPLOYMENT_REVISION_MAPPING table
                try (PreparedStatement statement = connection
                        .prepareStatement(SQLConstants.APIRevisionSqlConstants.REMOVE_API_REVISION_DEPLOYMENT_MAPPING)) {
                    for (APIRevisionDeployment apiRevisionDeployment : apiRevisionDeployments) {
                        statement.setString(1, apiRevisionDeployment.getDeployment());
                        statement.setString(2, apiRevisionId);
                        statement.addBatch();
                    }
                    statement.executeBatch();
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                handleException("Failed to remove API Revision Deployment Mapping entry for Revision UUID "
                        + apiRevisionId, e);
            }
        } catch (SQLException e) {
            handleException("Failed to remove API Revision Deployment Mapping entry for Revision UUID "
                    + apiRevisionId, e);
        }
    }

    /**
     * Remove an API revision Deployment mapping record to the database
     *
     * @param apiUUID          uuid of the revision
     * @param deployments content of the revision deployment mapping objects
     * @throws APIManagementException if an error occurs when adding a new API revision
     */
    public void removeAPIRevisionDeployment(String apiUUID, Set<APIRevisionDeployment> deployments)
            throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            try {
                connection.setAutoCommit(false);
                // Remove an entry from AM_DEPLOYMENT_REVISION_MAPPING table
                PreparedStatement statement = connection
                        .prepareStatement(SQLConstants.APIRevisionSqlConstants.REMOVE_API_REVISION_DEPLOYMENT_MAPPING);
                for (APIRevisionDeployment deployment : deployments) {
                    statement.setString(1, deployment.getDeployment());
                    statement.setString(2, deployment.getRevisionUUID());
                    statement.addBatch();
                }
                statement.executeBatch();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            handleException("Failed to remove API Revision Deployment Mapping entry for API UUID "
                    + apiUUID, e);
        }
    }

    /**
     * Remove an deployed API revision in the database
     *
     * @param apiUUID     uuid of the revision
     * @param deployments content of the revision deployment mapping objects
     * @throws APIManagementException if an error occurs when adding a new API revision
     */
    public void removeDeployedAPIRevision(String apiUUID, Set<DeployedAPIRevision> deployments)
            throws APIManagementException {
        if (deployments.size() > 0) {
            try (Connection connection = APIMgtDBUtil.getConnection()) {
                connection.setAutoCommit(false);
                // Remove an entry from AM_DEPLOYED_REVISION table
                try (PreparedStatement statement = connection
                        .prepareStatement(SQLConstants.APIRevisionSqlConstants.REMOVE_DEPLOYED_API_REVISION)) {
                    for (DeployedAPIRevision deployment : deployments) {
                        statement.setString(1, deployment.getDeployment());
                        statement.setString(2, deployment.getRevisionUUID());
                        statement.addBatch();
                    }
                    statement.executeBatch();
                    connection.commit();
                } catch (SQLException e) {
                    connection.rollback();
                    handleException("Failed to remove deployed API Revision entry for API UUID "
                            + apiUUID, e);
                }
            } catch (SQLException e) {
                handleException("Failed to remove deployed API Revision entry for API UUID "
                        + apiUUID, e);
            }
        }
    }

    /**
     * Set the deployed time of the un-deployed revision entry as NULL
     *
     * @param apiUUID     uuid of the revision
     * @param deployments content of the revision deployment mapping objects
     * @throws APIManagementException if an error occurs when adding a new API revision
     */
    public void setUnDeployedAPIRevision(String apiUUID, Set<DeployedAPIRevision> deployments)
            throws APIManagementException {
        if (deployments.size() > 0) {
            try (Connection connection = APIMgtDBUtil.getConnection()) {
                connection.setAutoCommit(false);
                // Remove an entry from AM_DEPLOYED_REVISION table
                try (PreparedStatement statement = connection
                        .prepareStatement(SQLConstants.APIRevisionSqlConstants.SET_UN_DEPLOYED_API_REVISION)) {
                    for (DeployedAPIRevision deployment : deployments) {
                        statement.setString(1, deployment.getDeployment());
                        statement.setString(2, deployment.getRevisionUUID());
                        statement.addBatch();
                    }
                    statement.executeBatch();
                    connection.commit();
                } catch (SQLException e) {
                    connection.rollback();
                    handleException("Failed to set un-deployed API Revision entry for API UUID "
                            + apiUUID, e);
                }
            } catch (SQLException e) {
                handleException("Failed to set un-deployed API Revision entry for API UUID "
                        + apiUUID, e);
            }
        }
    }

    /**
     * Update API revision Deployment mapping record
     *
     * @param apiUUID     API UUID
     * @param deployments content of the revision deployment mapping objects
     * @throws APIManagementException if an error occurs when adding a new API revision
     */
    public void updateAPIRevisionDeployment(String apiUUID, Set<APIRevisionDeployment> deployments)
            throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            connection.setAutoCommit(false);
            // Update an entry from AM_DEPLOYMENT_REVISION_MAPPING table
            try (PreparedStatement statement = connection
                    .prepareStatement(SQLConstants.APIRevisionSqlConstants.UPDATE_API_REVISION_DEPLOYMENT_MAPPING)) {
                for (APIRevisionDeployment deployment : deployments) {
                    statement.setBoolean(1, deployment.isDisplayOnDevportal());
                    statement.setString(2, deployment.getDeployment());
                    statement.setString(3, deployment.getRevisionUUID());
                    statement.addBatch();
                }
                statement.executeBatch();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            handleException("Failed to update Deployment Mapping entry for API UUID "
                    + apiUUID, e);
        }
    }

    /**
     * Restore API revision database records as the Current API of an API
     *
     * @param apiRevision content of the revision
     * @throws APIManagementException if an error occurs when restoring an API revision
     */
    public void restoreAPIRevision(APIRevision apiRevision) throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            try {
                connection.setAutoCommit(false);
                // Retrieve API ID
                APIIdentifier apiIdentifier = APIUtil.getAPIIdentifierFromUUID(apiRevision.getApiUUID());
                int apiId = getAPIID(apiRevision.getApiUUID(), connection);
                int tenantId = APIUtil.getTenantId(APIUtil.replaceEmailDomainBack(apiIdentifier.getProviderName()));
                String tenantDomain = APIUtil.getTenantDomainFromTenantId(tenantId);
                // Removing related Current API entries from AM_API_URL_MAPPING table
                PreparedStatement removeURLMappingsStatement = connection.prepareStatement(SQLConstants
                        .APIRevisionSqlConstants.REMOVE_CURRENT_API_ENTRIES_IN_AM_API_URL_MAPPING_BY_API_ID);
                removeURLMappingsStatement.setInt(1, apiId);
                removeURLMappingsStatement.executeUpdate();

                // Restoring to AM_API_URL_MAPPING table
                PreparedStatement getURLMappingsStatement = connection.prepareStatement(SQLConstants
                        .APIRevisionSqlConstants.GET_URL_MAPPINGS_WITH_SCOPE_AND_PRODUCT_ID_BY_REVISION_UUID);
                getURLMappingsStatement.setInt(1, apiId);
                getURLMappingsStatement.setString(2, apiRevision.getRevisionUUID());
                List<URITemplate> urlMappingList = new ArrayList<>();
                try (ResultSet rs = getURLMappingsStatement.executeQuery()) {
                    while (rs.next()) {
                        String script = null;
                        URITemplate uriTemplate = new URITemplate();
                        uriTemplate.setHTTPVerb(rs.getString(1));
                        uriTemplate.setAuthType(rs.getString(2));
                        uriTemplate.setUriTemplate(rs.getString(3));
                        uriTemplate.setThrottlingTier(rs.getString(4));
                        InputStream mediationScriptBlob = rs.getBinaryStream(5);
                        if (mediationScriptBlob != null) {
                            script = APIMgtDBUtil.getStringFromInputStream(mediationScriptBlob);
                        }
                        uriTemplate.setMediationScript(script);
                        if (!StringUtils.isEmpty(rs.getString(6))) {
                            Scope scope = new Scope();
                            scope.setKey(rs.getString(6));
                            uriTemplate.setScope(scope);
                        }
                        if (rs.getInt(7) != 0) {
                            // Adding product id to uri template id just to store value
                            uriTemplate.setId(rs.getInt(7));
                        }
                        urlMappingList.add(uriTemplate);
                    }
                }

                Map<String, URITemplate> uriTemplateMap = new HashMap<>();
                for (URITemplate urlMapping : urlMappingList) {
                    if (urlMapping.getScope() != null) {
                        URITemplate urlMappingNew = urlMapping;
                        URITemplate urlMappingExisting = uriTemplateMap.get(urlMapping.getUriTemplate()
                                + urlMapping.getHTTPVerb());
                        if (urlMappingExisting != null && urlMappingExisting.getScopes() != null) {
                            if (!urlMappingExisting.getScopes().contains(urlMapping.getScope())) {
                                urlMappingExisting.setScopes(urlMapping.getScope());
                                uriTemplateMap.put(urlMappingExisting.getUriTemplate() + urlMappingExisting.getHTTPVerb(),
                                        urlMappingExisting);
                            }
                        } else {
                            urlMappingNew.setScopes(urlMapping.getScope());
                            uriTemplateMap.put(urlMappingNew.getUriTemplate() + urlMappingNew.getHTTPVerb(),
                                    urlMappingNew);
                        }
                    } else {
                        uriTemplateMap.put(urlMapping.getUriTemplate() + urlMapping.getHTTPVerb(), urlMapping);
                    }
                }

                setOperationPoliciesToURITemplatesMap(connection, apiRevision.getRevisionUUID(), uriTemplateMap);

                PreparedStatement insertURLMappingsStatement = connection
                        .prepareStatement(SQLConstants.APIRevisionSqlConstants.INSERT_URL_MAPPINGS_CURRENT_API);
                for (URITemplate urlMapping : uriTemplateMap.values()) {
                    insertURLMappingsStatement.setInt(1, apiId);
                    insertURLMappingsStatement.setString(2, urlMapping.getHTTPVerb());
                    insertURLMappingsStatement.setString(3, urlMapping.getAuthType());
                    insertURLMappingsStatement.setString(4, urlMapping.getUriTemplate());
                    insertURLMappingsStatement.setString(5, urlMapping.getThrottlingTier());
                    insertURLMappingsStatement.addBatch();
                }
                insertURLMappingsStatement.executeBatch();

                // Add to AM_API_RESOURCE_SCOPE_MAPPING table and to AM_API_PRODUCT_MAPPING
                PreparedStatement getCurrentAPIURLMappingsStatement = connection
                        .prepareStatement(SQLConstants.APIRevisionSqlConstants.GET_CURRENT_API_URL_MAPPINGS_ID);
                PreparedStatement insertScopeResourceMappingStatement = connection
                        .prepareStatement(SQLConstants.APIRevisionSqlConstants.INSERT_SCOPE_RESOURCE_MAPPING);
                PreparedStatement insertProductResourceMappingStatement = connection
                        .prepareStatement(SQLConstants.APIRevisionSqlConstants.INSERT_PRODUCT_RESOURCE_MAPPING);
                PreparedStatement insertOperationPolicyMappingStatement = connection
                        .prepareStatement(SQLConstants.OperationPolicyConstants.ADD_API_OPERATION_POLICY_MAPPING);
                PreparedStatement deleteOutdatedOperationPolicyStatement = connection
                        .prepareStatement(SQLConstants.OperationPolicyConstants.DELETE_OPERATION_POLICY_BY_POLICY_ID);

                Map<String, String> restoredPolicyMap = new HashMap<>();
                Set<String> usedClonedPolicies = new HashSet<String>();
                for (URITemplate urlMapping : uriTemplateMap.values()) {
                    if (urlMapping.getScopes() != null) {
                        getCurrentAPIURLMappingsStatement.setInt(1, apiId);
                        getCurrentAPIURLMappingsStatement.setString(2, urlMapping.getHTTPVerb());
                        getCurrentAPIURLMappingsStatement.setString(3, urlMapping.getAuthType());
                        getCurrentAPIURLMappingsStatement.setString(4, urlMapping.getUriTemplate());
                        getCurrentAPIURLMappingsStatement.setString(5, urlMapping.getThrottlingTier());
                        try (ResultSet rs = getCurrentAPIURLMappingsStatement.executeQuery()) {
                            while (rs.next()) {
                                for (Scope scope : urlMapping.getScopes()) {
                                    insertScopeResourceMappingStatement.setString(1, scope.getKey());
                                    insertScopeResourceMappingStatement.setInt(2, rs.getInt(1));
                                    insertScopeResourceMappingStatement.setInt(3, tenantId);
                                    insertScopeResourceMappingStatement.addBatch();
                                }
                            }
                        }
                    }
                    if (urlMapping.getId() != 0) {
                        getCurrentAPIURLMappingsStatement.setInt(1, apiId);
                        getCurrentAPIURLMappingsStatement.setString(2, urlMapping.getHTTPVerb());
                        getCurrentAPIURLMappingsStatement.setString(3, urlMapping.getAuthType());
                        getCurrentAPIURLMappingsStatement.setString(4, urlMapping.getUriTemplate());
                        getCurrentAPIURLMappingsStatement.setString(5, urlMapping.getThrottlingTier());
                        try (ResultSet rs = getCurrentAPIURLMappingsStatement.executeQuery()) {
                            while (rs.next()) {
                                insertProductResourceMappingStatement.setInt(1, urlMapping.getId());
                                insertProductResourceMappingStatement.setInt(2, rs.getInt(1));
                                insertProductResourceMappingStatement.addBatch();
                            }
                        }
                    }
                    if (!urlMapping.getOperationPolicies().isEmpty()) {
                        getCurrentAPIURLMappingsStatement.setInt(1, apiId);
                        getCurrentAPIURLMappingsStatement.setString(2, urlMapping.getHTTPVerb());
                        getCurrentAPIURLMappingsStatement.setString(3, urlMapping.getAuthType());
                        getCurrentAPIURLMappingsStatement.setString(4, urlMapping.getUriTemplate());
                        getCurrentAPIURLMappingsStatement.setString(5, urlMapping.getThrottlingTier());
                        try (ResultSet rs = getCurrentAPIURLMappingsStatement.executeQuery()) {
                            while (rs.next()) {
                                for (OperationPolicy policy : urlMapping.getOperationPolicies()) {
                                    if (!restoredPolicyMap.keySet().contains(policy.getPolicyName())) {
                                        String restoredPolicyId = restoreOperationPolicyRevision(connection,
                                                apiRevision.getApiUUID(), policy.getPolicyId(), apiRevision.getId(),
                                                tenantDomain, false);
                                        // policy ID is stored in a map as same policy can be applied to multiple operations
                                        // and we only need to create the policy once.
                                        restoredPolicyMap.put(policy.getPolicyName(), restoredPolicyId);
                                        usedClonedPolicies.add(restoredPolicyId);
                                    }

                                    Gson gson = new Gson();
                                    String paramJSON = gson.toJson(policy.getParameters());
                                    insertOperationPolicyMappingStatement.setInt(1, rs.getInt(1));
                                    insertOperationPolicyMappingStatement.setString(2, restoredPolicyMap.get(policy.getPolicyName()));
                                    insertOperationPolicyMappingStatement.setString(3, policy.getDirection());
                                    insertOperationPolicyMappingStatement.setString(4, paramJSON);
                                    insertOperationPolicyMappingStatement.setInt(5, policy.getOrder());
                                    insertOperationPolicyMappingStatement.addBatch();
                                }
                            }
                        }
                    }
                }
                insertScopeResourceMappingStatement.executeBatch();
                insertProductResourceMappingStatement.executeBatch();
                insertOperationPolicyMappingStatement.executeBatch();
                deleteOutdatedOperationPolicyStatement.executeBatch();
                cleanUnusedClonedOperationPolicies(connection, usedClonedPolicies, apiRevision.getApiUUID());

                // Restoring AM_API_CLIENT_CERTIFICATE table entries
                PreparedStatement removeClientCertificatesStatement = connection.prepareStatement(SQLConstants
                        .APIRevisionSqlConstants.REMOVE_CURRENT_API_ENTRIES_IN_AM_API_CLIENT_CERTIFICATE_BY_API_ID);
                removeClientCertificatesStatement.setInt(1, apiId);
                removeClientCertificatesStatement.executeUpdate();

                PreparedStatement getClientCertificatesStatement = connection
                        .prepareStatement(SQLConstants.APIRevisionSqlConstants.GET_CLIENT_CERTIFICATES_BY_REVISION_UUID);
                getClientCertificatesStatement.setInt(1, apiId);
                getClientCertificatesStatement.setString(2, apiRevision.getRevisionUUID());
                List<ClientCertificateDTO> clientCertificateDTOS = new ArrayList<>();
                try (ResultSet rs = getClientCertificatesStatement.executeQuery()) {
                    while (rs.next()) {
                        ClientCertificateDTO clientCertificateDTO = new ClientCertificateDTO();
                        clientCertificateDTO.setAlias(rs.getString(1));
                        clientCertificateDTO.setCertificate(APIMgtDBUtil.getStringFromInputStream(rs.getBinaryStream(2)));
                        clientCertificateDTO.setTierName(rs.getString(3));
                        clientCertificateDTOS.add(clientCertificateDTO);
                    }
                }
                PreparedStatement insertClientCertificateStatement = connection
                        .prepareStatement(SQLConstants.APIRevisionSqlConstants.INSERT_CLIENT_CERTIFICATES_AS_CURRENT_API);
                for (ClientCertificateDTO clientCertificateDTO : clientCertificateDTOS) {
                    insertClientCertificateStatement.setInt(1, tenantId);
                    insertClientCertificateStatement.setString(2, clientCertificateDTO.getAlias());
                    insertClientCertificateStatement.setInt(3, apiId);
                    insertClientCertificateStatement.setBinaryStream(4,
                            getInputStream(clientCertificateDTO.getCertificate()));
                    insertClientCertificateStatement.setBoolean(5, false);
                    insertClientCertificateStatement.setString(6, clientCertificateDTO.getTierName());
                    insertClientCertificateStatement.setString(7, "Current API");
                    insertClientCertificateStatement.addBatch();
                }
                insertClientCertificateStatement.executeBatch();

                // Restoring AM_GRAPHQL_COMPLEXITY table
                PreparedStatement removeGraphQLComplexityStatement = connection.prepareStatement(SQLConstants
                        .APIRevisionSqlConstants.REMOVE_CURRENT_API_ENTRIES_IN_AM_GRAPHQL_COMPLEXITY_BY_API_ID);
                removeGraphQLComplexityStatement.setInt(1, apiId);
                removeGraphQLComplexityStatement.executeUpdate();

                PreparedStatement getGraphQLComplexityStatement = connection
                        .prepareStatement(SQLConstants.APIRevisionSqlConstants.GET_GRAPHQL_COMPLEXITY_BY_REVISION_UUID);
                List<CustomComplexityDetails> customComplexityDetailsList = new ArrayList<>();
                getGraphQLComplexityStatement.setInt(1, apiId);
                getGraphQLComplexityStatement.setString(2, apiRevision.getRevisionUUID());
                try (ResultSet rs1 = getGraphQLComplexityStatement.executeQuery()) {
                    while (rs1.next()) {
                        CustomComplexityDetails customComplexityDetails = new CustomComplexityDetails();
                        customComplexityDetails.setType(rs1.getString("TYPE"));
                        customComplexityDetails.setField(rs1.getString("FIELD"));
                        customComplexityDetails.setComplexityValue(rs1.getInt("COMPLEXITY_VALUE"));
                        customComplexityDetailsList.add(customComplexityDetails);
                    }
                }

                PreparedStatement insertGraphQLComplexityStatement = connection
                        .prepareStatement(SQLConstants.APIRevisionSqlConstants.INSERT_GRAPHQL_COMPLEXITY_AS_CURRENT_API);
                for (CustomComplexityDetails customComplexityDetails : customComplexityDetailsList) {
                    insertGraphQLComplexityStatement.setString(1, UUID.randomUUID().toString());
                    insertGraphQLComplexityStatement.setInt(2, apiId);
                    insertGraphQLComplexityStatement.setString(3, customComplexityDetails.getType());
                    insertGraphQLComplexityStatement.setString(4, customComplexityDetails.getField());
                    insertGraphQLComplexityStatement.setInt(5, customComplexityDetails.getComplexityValue());
                    insertGraphQLComplexityStatement.addBatch();
                }
                insertGraphQLComplexityStatement.executeBatch();
                restoreAPIRevisionMetaDataToWorkingCopy(connection, apiRevision.getApiUUID(),
                        apiRevision.getRevisionUUID());
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                handleException("Failed to restore API Revision entry of API UUID "
                        + apiRevision.getApiUUID(), e);
            }
        } catch (SQLException e) {
            handleException("Failed to restore API Revision entry of API UUID "
                    + apiRevision.getApiUUID(), e);
        }
    }



    /**
     * Restore API revision database records as the Current API of an API
     *
     * @param apiRevision content of the revision
     * @throws APIManagementException if an error occurs when restoring an API revision
     */
    public void deleteAPIRevision(APIRevision apiRevision) throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            try {
                connection.setAutoCommit(false);
                // Retrieve API ID
                int apiId = getAPIID(apiRevision.getApiUUID(), connection);

                // Removing related revision entries from AM_REVISION table
                PreparedStatement removeAMRevisionStatement = connection.prepareStatement(SQLConstants
                        .APIRevisionSqlConstants.DELETE_API_REVISION);
                removeAMRevisionStatement.setString(1, apiRevision.getRevisionUUID());
                removeAMRevisionStatement.executeUpdate();

                // Removing related revision entries from AM_API_URL_MAPPING table
                // This will cascade remove entries from AM_API_RESOURCE_SCOPE_MAPPING and AM_API_PRODUCT_MAPPING tables
                PreparedStatement removeURLMappingsStatement = connection.prepareStatement(SQLConstants
                        .APIRevisionSqlConstants.REMOVE_REVISION_ENTRIES_IN_AM_API_URL_MAPPING_BY_REVISION_UUID);
                removeURLMappingsStatement.setInt(1, apiId);
                removeURLMappingsStatement.setString(2, apiRevision.getRevisionUUID());
                removeURLMappingsStatement.executeUpdate();

                // Removing related revision entries from AM_API_CLIENT_CERTIFICATE table
                PreparedStatement removeClientCertificatesStatement = connection.prepareStatement(SQLConstants
                        .APIRevisionSqlConstants.REMOVE_REVISION_ENTRIES_IN_AM_API_CLIENT_CERTIFICATE_BY_REVISION_UUID);
                removeClientCertificatesStatement.setInt(1, apiId);
                removeClientCertificatesStatement.setString(2, apiRevision.getRevisionUUID());
                removeClientCertificatesStatement.executeUpdate();

                // Removing related revision entries from AM_GRAPHQL_COMPLEXITY table
                PreparedStatement removeGraphQLComplexityStatement = connection.prepareStatement(SQLConstants
                        .APIRevisionSqlConstants.REMOVE_REVISION_ENTRIES_IN_AM_GRAPHQL_COMPLEXITY_BY_REVISION_UUID);
                removeGraphQLComplexityStatement.setInt(1, apiId);
                removeGraphQLComplexityStatement.setString(2, apiRevision.getRevisionUUID());
                removeGraphQLComplexityStatement.executeUpdate();
                deleteAPIRevisionMetaData(connection, apiRevision.getApiUUID(), apiRevision.getRevisionUUID());

                // Removing related revision entries from operation policies
                deleteAllAPISpecificOperationPoliciesByAPIUUID(connection, apiRevision.getApiUUID(), apiRevision.getRevisionUUID());

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                handleException("Failed to delete API Revision entry of API UUID "
                        + apiRevision.getApiUUID(), e);
            }
        } catch (SQLException e) {
            handleException("Failed to delete API Revision entry of API UUID "
                    + apiRevision.getApiUUID(), e);
        }
    }

    /**
     * Adds an API Product revision record to the database
     *
     * @param apiRevision content of the revision
     * @throws APIManagementException if an error occurs when adding a new API revision
     */
    public void addAPIProductRevision(APIRevision apiRevision) throws APIManagementException {
        try (Connection connection = APIMgtDBUtil.getConnection()) {
            try {
                connection.setAutoCommit(false);
                // Adding to AM_REVISION table
                PreparedStatement statement = connection
                        .prepareStatement(SQLConstants.APIRevisionSqlConstants.ADD_API_REVISION);
                statement.setInt(1, apiRevision.getId());
                statement.setString(2, apiRevision.getApiUUID());
                statement.setString(3, apiRevision.getRevisionUUID());
                statement.setString(4, apiRevision.getDescription());
                statement.setString(5, apiRevision.getCreatedBy());
                statement.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
                statement.executeUpdate();

                // Retrieve API Product ID
                APIProductIdentifier apiProductIdentifier =
                        APIUtil.getAPIProductIdentifierFromUUID(apiRevision.getApiUUID());
                int apiId = getAPIID(apiRevision.getApiUUID(), connection);
                int tenantId =
                        APIUtil.getTenantId(APIUtil.replaceEmailDomainBack(apiProductIdentifier.getProviderName()));
                String tenantDomain = APIUtil.getTenantDomainFromTenantId(tenantId);

                // Adding to AM_API_URL_MAPPING table
                PreparedStatement getURLMappingsStatement = connection
                        .prepareStatement(SQLConstants.APIRevisionSqlConstants.
                                GET_URL_MAPPINGS_WITH_SCOPE_AND_PRODUCT_ID_BY_PRODUCT_ID);
                getURLMappingsStatement.setInt(1, apiId);
                List<URITemplate> urlMappingList = new ArrayList<>();
                try (ResultSet rs = getURLMappingsStatement.executeQuery()) {
                    while (rs.next()) {
                        String script = null;
                        URITemplate uriTemplate = new URITemplate();
                        uriTemplate.setHTTPVerb(rs.getString(1));
                        uriTemplate.setAuthType(rs.getString(2));
                        uriTemplate.setUriTemplate(rs.getString(3));
                        uriTemplate.setThrottlingTier(rs.getString(4));
                        InputStream mediationScriptBlob = rs.getBinaryStream(5);
                        if (mediationScriptBlob != null) {
                            script = APIMgtDBUtil.getStringFromInputStream(mediationScriptBlob);
                        }
                        uriTemplate.setMediationScript(script);
                        if (!StringUtils.isEmpty(rs.getString(6))) {
                            Scope scope = new Scope();
                            scope.setKey(rs.getString(6));
                            uriTemplate.setScope(scope);
                        }
                        if (rs.getInt(7) != 0) {
                            // Adding api id to uri template id just to store value
                            uriTemplate.setId(rs.getInt(7));
                        }
                        urlMappingList.add(uriTemplate);
                    }
                }

                Map<String, URITemplate> uriTemplateMap = new HashMap<>();
                for (URITemplate urlMapping : urlMappingList) {
                    if (urlMapping.getScope() != null) {
                        URITemplate urlMappingNew = urlMapping;
                        URITemplate urlMappingExisting = uriTemplateMap.get(urlMapping.getUriTemplate()
                                + urlMapping.getHTTPVerb());
                        if (urlMappingExisting != null && urlMappingExisting.getScopes() != null) {
                            if (!urlMappingExisting.getScopes().contains(urlMapping.getScope())) {
                                urlMappingExisting.setScopes(urlMapping.getScope());
                                uriTemplateMap.put(urlMappingExisting.getUriTemplate() + urlMappingExisting.getHTTPVerb(),
                                        urlMappingExisting);
                            }
                        } else {
                            urlMappingNew.setScopes(urlMapping.getScope());
                            uriTemplateMap.put(urlMappingNew.getUriTemplate() + urlMappingNew.getHTTPVerb(),
                                    urlMappingNew);
                        }
                    } else if (urlMapping.getId() != 0) {
                        URITemplate urlMappingExisting = uriTemplateMap.get(urlMapping.getUriTemplate()
                                + urlMapping.getHTTPVerb());
                        if (urlMappingExisting == null) {
                            uriTemplateMap.put(urlMapping.getUriTemplate() + urlMapping.getHTTPVerb(), urlMapping);
                        }
                    } else {
                        uriTemplateMap.put(urlMapping.getUriTemplate() + urlMapping.getHTTPVerb(), urlMapping);
                    }
                }

                setAPIProductOperationPoliciesToURITemplatesMap(connection, new Integer(apiId).toString(), uriTemplateMap);

                PreparedStatement insertURLMappingsStatement = connection
                        .prepareStatement(SQLConstants.APIRevisionSqlConstants.INSERT_URL_MAPPINGS);
                for (URITemplate urlMapping : uriTemplateMap.values()) {
                    insertURLMappingsStatement.setInt(1, urlMapping.getId());
                    insertURLMappingsStatement.setString(2, urlMapping.getHTTPVerb());
                    insertURLMappingsStatement.setString(3, urlMapping.getAuthType());
                    insertURLMappingsStatement.setString(4, urlMapping.getUriTemplate());
                    insertURLMappingsStatement.setString(5, urlMapping.getThrottlingTier());
                    insertURLMappingsStatement.setString(6, apiRevision.getRevisionUUID());
                    insertURLMappingsStatement.addBatch();
                }
                insertURLMappingsStatement.executeBatch();

                // Add to AM_API_RESOURCE_SCOPE_MAPPING table and to AM_API_PRODUCT_MAPPING
                PreparedStatement getRevisionedURLMappingsStatement = connection
                        .prepareStatement(SQLConstants.APIRevisionSqlConstants.GET_REVISIONED_URL_MAPPINGS_ID);
                PreparedStatement insertScopeResourceMappingStatement = connection
                        .prepareStatement(SQLConstants.APIRevisionSqlConstants.INSERT_SCOPE_RESOURCE_MAPPING);
                PreparedStatement insertProductResourceMappingStatement = connection
                        .prepareStatement(SQLConstants.APIRevisionSqlConstants.INSERT_PRODUCT_REVISION_RESOURCE_MAPPING);
                String dbProductName = connection.getMetaData().getDatabaseProductName();
                PreparedStatement insertOperationPolicyMappingStatement = connection
                        .prepareStatement(SQLConstants.OperationPolicyConstants.ADD_API_OPERATION_POLICY_MAPPING, new String[]{
                                DBUtils.getConvertedAutoGeneratedColumnName(dbProductName, "OPERATION_POLICY_MAPPING_ID")});
                Map<String, String> clonedPoliciesMap = new HashMap<>();
                List<ClonePolicyMetadataDTO> toBeClonedPolicyDetails = new ArrayList<>();
                for (URITemplate urlMapping : uriTemplateMap.values()) {
                    getRevisionedURLMappingsStatement.setInt(1, urlMapping.getId());
                    getRevisionedURLMappingsStatement.setString(2, apiRevision.getRevisionUUID());
                    getRevisionedURLMappingsStatement.setString(3, urlMapping.getHTTPVerb());
                    getRevisionedURLMappingsStatement.setString(4, urlMapping.getAuthType());
                    getRevisionedURLMappingsStatement.setString(5, urlMapping.getUriTemplate());
                    getRevisionedURLMappingsStatement.setString(6, urlMapping.getThrottlingTier());
                    if (urlMapping.getScopes() != null) {
                        try (ResultSet rs = getRevisionedURLMappingsStatement.executeQuery()) {
                            while (rs.next()) {
                                for (Scope scope : urlMapping.getScopes()) {
                                    insertScopeResourceMappingStatement.setString(1, scope.getKey());
                                    insertScopeResourceMappingStatement.setInt(2, rs.getInt(1));
                                    insertScopeResourceMappingStatement.setInt(3, tenantId);
                                    insertScopeResourceMappingStatement.addBatch();
                                }
                            }
                        }
                    }
                    try (ResultSet rs = getRevisionedURLMappingsStatement.executeQuery()) {
                        while (rs.next()) {
                            insertProductResourceMappingStatement.setInt(1, apiId);
                            insertProductResourceMappingStatement.setInt(2, rs.getInt(1));
                            insertProductResourceMappingStatement.setString(3, apiRevision.getRevisionUUID());
                            insertProductResourceMappingStatement.addBatch();
                        }
                    }
                    try (ResultSet rs = getRevisionedURLMappingsStatement.executeQuery()) {
                        while (rs.next()) {
                            for (OperationPolicy policy : urlMapping.getOperationPolicies()) {
                                String clonedPolicyId;
                                if (!clonedPoliciesMap.keySet().contains(policy.getPolicyId())) {
                                    // Since we are creating a new revision, we need to clone all the policies from current status.
                                    // If the policy is not cloned from a previous policy, we have to clone.
                                    clonedPolicyId = UUID.randomUUID().toString();
                                    ClonePolicyMetadataDTO toBeClonedSinglePolicyData = new ClonePolicyMetadataDTO();
                                    toBeClonedSinglePolicyData.setClonedPolicyUUID(clonedPolicyId);
                                    toBeClonedSinglePolicyData.setCurrentPolicyUUID(policy.getPolicyId());
                                    toBeClonedSinglePolicyData.setApiUUID(apiRevision.getApiUUID());
                                    toBeClonedSinglePolicyData.setRevisionUUID(apiRevision.getRevisionUUID());
                                    toBeClonedPolicyDetails.add(toBeClonedSinglePolicyData);

                                    clonedPoliciesMap.put(policy.getPolicyId(), clonedPolicyId);
                                }

                                Gson gson = new Gson();
                                String paramJSON = gson.toJson(policy.getParameters());

                                insertOperationPolicyMappingStatement.setInt(1, rs.getInt(1));
                                insertOperationPolicyMappingStatement.setString(2, clonedPoliciesMap.get(policy.getPolicyId()));
                                insertOperationPolicyMappingStatement.setString(3, policy.getDirection());
                                insertOperationPolicyMappingStatement.setString(4, paramJSON);
                                insertOperationPolicyMappingStatement.setInt(5, policy.getOrder());
                                insertOperationPolicyMappingStatement.addBatch();
                            }
                        }
                    }
                }
                insertScopeResourceMappingStatement.executeBatch();
                insertProductResourceMappingStatement.executeBatch();

                for (ClonePolicyMetadataDTO toBeClonedPolicyData : toBeClonedPolicyDetails) {
                    cloneAPISpecificPoliciesForRevisioning(connection, toBeClonedPolicyData.getCurrentPolicyUUID(),
                            toBeClonedPolicyData.getClonedPolicyUUID(), toBeClonedPolicyData.getApiUUID(),
                            toBeClonedPolicyData.getRevisionUUID(), tenantDomain);
                }
                insertOperationPolicyMappingStatement.executeBatch();

                // Adding to AM_API_CLIENT_CERTIFICATE
                String getClientCertificatesQuery = SQLConstants.APIRevisionSqlConstants.GET_CLIENT_CERTIFICATES;
                String driverName = connection.getMetaData().getDriverName();
                if (driverName.contains("Oracle")) {
                    getClientCertificatesQuery = SQLConstants.APIRevisionSqlConstants.GET_CLIENT_CERTIFICATES_ORACLE_SQL;
                } else if (driverName.contains("MS SQL") || driverName.contains("Microsoft")) {
                    getClientCertificatesQuery = SQLConstants.APIRevisionSqlConstants.GET_CLIENT_CERTIFICATES_MSSQL;
                }
                
                PreparedStatement getClientCertificatesStatement = connection.prepareStatement(getClientCertificatesQuery);
                getClientCertificatesStatement.setInt(1, apiId);
                List<ClientCertificateDTO> clientCertificateDTOS = new ArrayList<>();
                try (ResultSet rs = getClientCertificatesStatement.executeQuery()) {
                    while (rs.next()) {
                        ClientCertificateDTO clientCertificateDTO = new ClientCertificateDTO();
                        clientCertificateDTO.setAlias(rs.getString(1));
                        clientCertificateDTO.setCertificate(APIMgtDBUtil.getStringFromInputStream(rs.getBinaryStream(2)));
                        clientCertificateDTO.setTierName(rs.getString(3));
                        clientCertificateDTOS.add(clientCertificateDTO);
                    }
                }
                PreparedStatement insertClientCertificateStatement = connection
                        .prepareStatement(SQLConstants.APIRevisionSqlConstants.INSERT_CLIENT_CERTIFICATES);
                for (ClientCertificateDTO clientCertificateDTO : clientCertificateDTOS) {
                    insertClientCertificateStatement.setInt(1, tenantId);
                    insertClientCertificateStatement.setString(2, clientCertificateDTO.getAlias());
                    insertClientCertificateStatement.setInt(3, apiId);
                    insertClientCertificateStatement.setBinaryStream(4,
                            getInputStream(clientCertificateDTO.getCertificate()));
                    insertClientCertificateStatement.setBoolean(5, false);
                    insertClientCertificateStatement.setString(6, clientCertificateDTO.getTierName());
                    insertClientCertificateStatement.setString(7, apiRevision.getRevisionUUID());
                    insertClientCertificateStatement.addBatch();
                }
                insertClientCertificateStatement.executeBatch();

                // Adding to AM_GRAPHQL_COMPLEXITY table
                PreparedStatement getGraphQLComplexityStatement = connection
                        .prepareStatement(SQLConstants.APIRevisionSqlConstants.GET_GRAPHQL_COMPLEXITY);
                List<CustomComplexityDetails> customComplexityDetailsList = new ArrayList<>();
                getGraphQLComplexityStatement.setInt(1, apiId);
                try (ResultSet rs1 = getGraphQLComplexityStatement.executeQuery()) {
                    while (rs1.next()) {
                        CustomComplexityDetails customComplexityDetails = new CustomComplexityDetails();
                        customComplexityDetails.setType(rs1.getString("TYPE"));
                        customComplexityDetails.setField(rs1.getString("FIELD"));
                        customComplexityDetails.setComplexityValue(rs1.getInt("COMPLEXITY_VALUE"));
                        customComplexityDetailsList.add(customComplexityDetails);
                    }
                }

                PreparedStatement insertGraphQLComplexityStatement = connection
                        .prepareStatement(SQLConstants.APIRevisionSqlConstants.INSERT_GRAPHQL_COMPLEXITY);
                for (CustomComplexityDetails customComplexityDetails : customComplexityDetailsList) {
                    insertGraphQLComplexityStatement.setString(1, UUID.randomUUID().toString());
                    insertGraphQLComplexityStatement.setInt(2, apiId);
                    insertGraphQLComplexityStatement.setString(3, customComplexityDetails.getType());
                    insertGraphQLComplexityStatement.setString(4, customComplexityDetails.getField());
                    insertGraphQLComplexityStatement.setInt(5, customComplexityDetails.getComplexityValue());
                    insertGraphQLComplexityStatement.setString(6, apiRevision.getRevisionUUID());
                    insertGraphQLComplexityStatement.addBatch();
                }
                insertGraphQLComplexityStatement.executeBatch();
                updateLatestRevisionNumber(connection, apiRevision.getApiUUID(), apiRevision.getId());
                addAPIRevisionMetaData(connection, apiRevision.getApiUUID(), apiRevision.getRevisionUUID());
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                handleException("Failed to add API Revision entry of API Product UUID "
                        + apiRevision.getApiUUID(), e);
            }
        } catch (SQLException e) {
            handleException("Failed to add API Revision entry of API Product UUID "
                    + apiRevision.getApiUUID(), e);
        }
    }

    /**
     * Restore API Product revision database records as the Current API Product of an API Product
     *
     * @param apiRevision content of the revision
     * @throws APIManagementException if an error occurs when restoring an API revision
     */
    public void restoreAPIProductRevision(APIRevision apiRevision) throws APIManagementException {
        try (Connection connection = APIMgtDBUtil.getConnection()) {
            try {
                connection.setAutoCommit(false);
                // Retrieve API ID
                APIProductIdentifier apiProductIdentifier =
                        APIUtil.getAPIProductIdentifierFromUUID(apiRevision.getApiUUID());
                int apiId = getAPIID(apiRevision.getApiUUID(), connection);
                int tenantId =
                        APIUtil.getTenantId(APIUtil.replaceEmailDomainBack(apiProductIdentifier.getProviderName()));
                String tenantDomain = APIUtil.getTenantDomainFromTenantId(tenantId);

                //Remove Current API Product entries from AM_API_URL_MAPPING table
                PreparedStatement removeURLMappingsFromCurrentAPIProduct = connection.prepareStatement(
                        SQLConstants.APIRevisionSqlConstants.REMOVE_CURRENT_API_PRODUCT_ENTRIES_IN_AM_API_URL_MAPPING);
                removeURLMappingsFromCurrentAPIProduct.setString(1, Integer.toString(apiId));
                removeURLMappingsFromCurrentAPIProduct.executeUpdate();

                //Copy Revision resources
                PreparedStatement getURLMappingsFromRevisionedAPIProduct = connection.prepareStatement(
                        SQLConstants.APIRevisionSqlConstants.GET_API_PRODUCT_REVISION_URL_MAPPINGS_BY_REVISION_UUID);
                getURLMappingsFromRevisionedAPIProduct.setString(1, apiRevision.getRevisionUUID());
                Map<String, URITemplate> urlMappingList = new HashMap<>();
                try (ResultSet rs = getURLMappingsFromRevisionedAPIProduct.executeQuery()) {
                    String key, httpMethod, urlPattern;
                    while (rs.next()) {
                        String script = null;
                        URITemplate uriTemplate = new URITemplate();
                        httpMethod = rs.getString("HTTP_METHOD");
                        urlPattern = rs.getString("URL_PATTERN");
                        uriTemplate.setHTTPVerb(httpMethod);
                        uriTemplate.setAuthType(rs.getString("AUTH_SCHEME"));
                        uriTemplate.setUriTemplate(rs.getString("URL_PATTERN"));
                        uriTemplate.setThrottlingTier(rs.getString("THROTTLING_TIER"));
                        InputStream mediationScriptBlob = rs.getBinaryStream("MEDIATION_SCRIPT");
                        if (mediationScriptBlob != null) {
                            script = APIMgtDBUtil.getStringFromInputStream(mediationScriptBlob);
                        }
                        uriTemplate.setMediationScript(script);
                        if (rs.getInt("API_ID") != 0) {
                            // Adding product id to uri template id just to store value
                            uriTemplate.setId(rs.getInt("API_ID"));
                        }
                        key = urlPattern + httpMethod;
                        urlMappingList.put(key, uriTemplate);
                    }
                }

                //Populate Scope Mappings
                PreparedStatement getScopeMappingsFromRevisionedAPIProduct = connection.prepareStatement(
                        SQLConstants.APIRevisionSqlConstants.GET_API_PRODUCT_REVISION_SCOPE_MAPPINGS_BY_REVISION_UUID);
                getScopeMappingsFromRevisionedAPIProduct.setString(1, apiRevision.getRevisionUUID());
                try (ResultSet rs = getScopeMappingsFromRevisionedAPIProduct.executeQuery()) {
                    while (rs.next()) {
                        String key = rs.getString("URL_PATTERN") + rs.getString("HTTP_METHOD");
                        if (urlMappingList.containsKey(key)) {
                            URITemplate uriTemplate = urlMappingList.get(key);

                            Scope scope = new Scope();
                            scope.setKey(rs.getString("SCOPE_NAME"));
                            uriTemplate.setScope(scope);

                            uriTemplate.setScopes(scope);
                        }
                    }
                }

                setAPIProductOperationPoliciesToURITemplatesMap(connection, apiRevision.getRevisionUUID(), urlMappingList);

                PreparedStatement insertURLMappingsStatement = connection
                        .prepareStatement(SQLConstants.APIRevisionSqlConstants.INSERT_URL_MAPPINGS);
                for (URITemplate urlMapping : urlMappingList.values()) {
                    insertURLMappingsStatement.setInt(1, urlMapping.getId());
                    insertURLMappingsStatement.setString(2, urlMapping.getHTTPVerb());
                    insertURLMappingsStatement.setString(3, urlMapping.getAuthType());
                    insertURLMappingsStatement.setString(4, urlMapping.getUriTemplate());
                    insertURLMappingsStatement.setString(5, urlMapping.getThrottlingTier());
                    insertURLMappingsStatement.setString(6, Integer.toString(apiId));
                    insertURLMappingsStatement.addBatch();
                }
                insertURLMappingsStatement.executeBatch();

                //Insert Scope Mappings and operation policy mappings
                PreparedStatement getRevisionedURLMappingsStatement = connection
                        .prepareStatement(SQLConstants.APIRevisionSqlConstants.GET_REVISIONED_URL_MAPPINGS_ID);
                PreparedStatement addResourceScopeMapping = connection.prepareStatement(
                        SQLConstants.ADD_API_RESOURCE_SCOPE_MAPPING);
                PreparedStatement addOperationPolicyStatement = connection
                        .prepareStatement(SQLConstants.OperationPolicyConstants.ADD_API_OPERATION_POLICY_MAPPING);

                Map<String, String> clonedPoliciesMap = new HashMap<>();
                Set<String> usedClonedPolicies = new HashSet<String>();
                for (URITemplate urlMapping : urlMappingList.values()) {
                    getRevisionedURLMappingsStatement.setInt(1, urlMapping.getId());
                    getRevisionedURLMappingsStatement.setString(2, Integer.toString(apiId));
                    getRevisionedURLMappingsStatement.setString(3, urlMapping.getHTTPVerb());
                    getRevisionedURLMappingsStatement.setString(4, urlMapping.getAuthType());
                    getRevisionedURLMappingsStatement.setString(5, urlMapping.getUriTemplate());
                    getRevisionedURLMappingsStatement.setString(6, urlMapping.getThrottlingTier());
                    try (ResultSet rs = getRevisionedURLMappingsStatement.executeQuery()) {
                        if (rs.next()) {
                            int newURLMappingId = rs.getInt("URL_MAPPING_ID");
                            if (urlMapping.getScopes() != null && urlMapping.getScopes().size() > 0) {
                                for (Scope scope : urlMapping.getScopes()) {
                                    addResourceScopeMapping.setString(1, scope.getKey());
                                    addResourceScopeMapping.setInt(2, newURLMappingId);
                                    addResourceScopeMapping.setInt(3, tenantId);
                                    addResourceScopeMapping.addBatch();
                                }
                            }

                            if (urlMapping.getOperationPolicies().size() > 0) {
                                for (OperationPolicy policy : urlMapping.getOperationPolicies()) {
                                    if (!clonedPoliciesMap.keySet().contains(policy.getPolicyName())) {
                                        String policyId = restoreOperationPolicyRevision(connection,
                                                apiRevision.getApiUUID(), policy.getPolicyId(), apiRevision.getId(),
                                                tenantDomain, true);
                                        clonedPoliciesMap.put(policy.getPolicyName(), policyId);
                                        usedClonedPolicies.add(policyId);
                                    }

                                    Gson gson = new Gson();
                                    String paramJSON = gson.toJson(policy.getParameters());

                                    addOperationPolicyStatement.setInt(1, rs.getInt(1));
                                    addOperationPolicyStatement.setString(2, clonedPoliciesMap.get(policy.getPolicyName()));
                                    addOperationPolicyStatement.setString(3, policy.getDirection());
                                    addOperationPolicyStatement.setString(4, paramJSON);
                                    addOperationPolicyStatement.setInt(5, policy.getOrder());
                                    addOperationPolicyStatement.executeUpdate();
                                }
                            }
                        }
                    }
                }
                addResourceScopeMapping.executeBatch();
                cleanUnusedClonedOperationPolicies(connection, usedClonedPolicies, apiRevision.getApiUUID());

                //Get URL_MAPPING_IDs from table and add records to product mapping table
                PreparedStatement getURLMappingOfAPIProduct = connection.prepareStatement(
                        SQLConstants.GET_URL_MAPPING_IDS_OF_API_PRODUCT_SQL);
                PreparedStatement insertProductResourceMappingStatement = connection
                        .prepareStatement(SQLConstants.APIRevisionSqlConstants.INSERT_PRODUCT_REVISION_RESOURCE_MAPPING);
                getURLMappingOfAPIProduct.setString(1, Integer.toString(apiId));
                try (ResultSet rs = getURLMappingOfAPIProduct.executeQuery()) {
                    while (rs.next()) {
                        insertProductResourceMappingStatement.setInt(1, apiId);
                        insertProductResourceMappingStatement.setInt(2, rs.getInt("URL_MAPPING_ID"));
                        insertProductResourceMappingStatement.setString(3, "Current API");
                        insertProductResourceMappingStatement.addBatch();
                    }
                    insertProductResourceMappingStatement.executeBatch();
                }

                // Restoring AM_API_CLIENT_CERTIFICATE table entries
                PreparedStatement removeClientCertificatesStatement = connection.prepareStatement(SQLConstants
                        .APIRevisionSqlConstants.REMOVE_CURRENT_API_ENTRIES_IN_AM_API_CLIENT_CERTIFICATE_BY_API_ID);
                removeClientCertificatesStatement.setInt(1, apiId);
                removeClientCertificatesStatement.executeUpdate();

                PreparedStatement getClientCertificatesStatement = connection
                        .prepareStatement(SQLConstants.APIRevisionSqlConstants.GET_CLIENT_CERTIFICATES_BY_REVISION_UUID);
                getClientCertificatesStatement.setInt(1, apiId);
                getClientCertificatesStatement.setString(2, apiRevision.getRevisionUUID());
                List<ClientCertificateDTO> clientCertificateDTOS = new ArrayList<>();
                try (ResultSet rs = getClientCertificatesStatement.executeQuery()) {
                    while (rs.next()) {
                        ClientCertificateDTO clientCertificateDTO = new ClientCertificateDTO();
                        clientCertificateDTO.setAlias(rs.getString(1));
                        clientCertificateDTO.setCertificate(APIMgtDBUtil.getStringFromInputStream(rs.getBinaryStream(2)));
                        clientCertificateDTO.setTierName(rs.getString(3));
                        clientCertificateDTOS.add(clientCertificateDTO);
                    }
                }
                PreparedStatement insertClientCertificateStatement = connection
                        .prepareStatement(SQLConstants.APIRevisionSqlConstants.INSERT_CLIENT_CERTIFICATES_AS_CURRENT_API);
                for (ClientCertificateDTO clientCertificateDTO : clientCertificateDTOS) {
                    insertClientCertificateStatement.setInt(1, tenantId);
                    insertClientCertificateStatement.setString(2, clientCertificateDTO.getAlias());
                    insertClientCertificateStatement.setInt(3, apiId);
                    insertClientCertificateStatement.setBinaryStream(4,
                            getInputStream(clientCertificateDTO.getCertificate()));
                    insertClientCertificateStatement.setBoolean(5, false);
                    insertClientCertificateStatement.setString(6, clientCertificateDTO.getTierName());
                    insertClientCertificateStatement.setString(7, "Current API");
                    insertClientCertificateStatement.addBatch();
                }
                insertClientCertificateStatement.executeBatch();

                // Restoring AM_GRAPHQL_COMPLEXITY table
                PreparedStatement removeGraphQLComplexityStatement = connection.prepareStatement(SQLConstants
                        .APIRevisionSqlConstants.REMOVE_CURRENT_API_ENTRIES_IN_AM_GRAPHQL_COMPLEXITY_BY_API_ID);
                removeGraphQLComplexityStatement.setInt(1, apiId);
                removeGraphQLComplexityStatement.executeUpdate();

                PreparedStatement getGraphQLComplexityStatement = connection
                        .prepareStatement(SQLConstants.APIRevisionSqlConstants.GET_GRAPHQL_COMPLEXITY_BY_REVISION_UUID);
                List<CustomComplexityDetails> customComplexityDetailsList = new ArrayList<>();
                getGraphQLComplexityStatement.setInt(1, apiId);
                getGraphQLComplexityStatement.setString(2, apiRevision.getRevisionUUID());
                try (ResultSet rs1 = getGraphQLComplexityStatement.executeQuery()) {
                    while (rs1.next()) {
                        CustomComplexityDetails customComplexityDetails = new CustomComplexityDetails();
                        customComplexityDetails.setType(rs1.getString("TYPE"));
                        customComplexityDetails.setField(rs1.getString("FIELD"));
                        customComplexityDetails.setComplexityValue(rs1.getInt("COMPLEXITY_VALUE"));
                        customComplexityDetailsList.add(customComplexityDetails);
                    }
                }

                PreparedStatement insertGraphQLComplexityStatement = connection
                        .prepareStatement(SQLConstants.APIRevisionSqlConstants.INSERT_GRAPHQL_COMPLEXITY_AS_CURRENT_API);
                for (CustomComplexityDetails customComplexityDetails : customComplexityDetailsList) {
                    insertGraphQLComplexityStatement.setString(1, UUID.randomUUID().toString());
                    insertGraphQLComplexityStatement.setInt(2, apiId);
                    insertGraphQLComplexityStatement.setString(3, customComplexityDetails.getType());
                    insertGraphQLComplexityStatement.setString(4, customComplexityDetails.getField());
                    insertGraphQLComplexityStatement.setInt(5, customComplexityDetails.getComplexityValue());
                    insertGraphQLComplexityStatement.addBatch();
                }
                insertGraphQLComplexityStatement.executeBatch();

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                handleException("Failed to restore API Revision entry of API UUID "
                        + apiRevision.getApiUUID(), e);
            }
        } catch (SQLException e) {
            handleException("Failed to restore API Revision entry of API UUID "
                    + apiRevision.getApiUUID(), e);
        }
    }

    /**
     * Delete API Product revision database records
     *
     * @param apiRevision content of the revision
     * @throws APIManagementException if an error occurs when restoring an API revision
     */
    public void deleteAPIProductRevision(APIRevision apiRevision) throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            try {
                connection.setAutoCommit(false);
                // Retrieve API ID
                APIProductIdentifier apiProductIdentifier =
                        APIUtil.getAPIProductIdentifierFromUUID(apiRevision.getApiUUID());
                int apiId = getAPIID(apiRevision.getApiUUID(), connection);
                int tenantId =
                        APIUtil.getTenantId(APIUtil.replaceEmailDomainBack(apiProductIdentifier.getProviderName()));

                // Removing related revision entries from AM_REVISION table
                PreparedStatement removeAMRevisionStatement = connection.prepareStatement(SQLConstants
                        .APIRevisionSqlConstants.DELETE_API_REVISION);
                removeAMRevisionStatement.setString(1, apiRevision.getRevisionUUID());
                removeAMRevisionStatement.executeUpdate();

                // Removing related revision entries from AM_API_PRODUCT_MAPPING table
                PreparedStatement removeProductMappingsStatement = connection.prepareStatement(SQLConstants
                        .APIRevisionSqlConstants.REMOVE_REVISION_ENTRIES_IN_AM_API_PRODUCT_MAPPING_BY_REVISION_UUID);
                removeProductMappingsStatement.setInt(1, apiId);
                removeProductMappingsStatement.setString(2, apiRevision.getRevisionUUID());
                removeProductMappingsStatement.executeUpdate();

                // Removing related revision entries from AM_API_URL_MAPPING table
                PreparedStatement removeURLMappingsStatement = connection.prepareStatement(SQLConstants
                        .APIRevisionSqlConstants.REMOVE_PRODUCT_REVISION_ENTRIES_IN_AM_API_URL_MAPPING_BY_REVISION_UUID);
                removeURLMappingsStatement.setString(1, apiRevision.getRevisionUUID());
                removeURLMappingsStatement.executeUpdate();

                // Removing related revision entries from AM_API_CLIENT_CERTIFICATE table
                PreparedStatement removeClientCertificatesStatement = connection.prepareStatement(SQLConstants
                        .APIRevisionSqlConstants.REMOVE_REVISION_ENTRIES_IN_AM_API_CLIENT_CERTIFICATE_BY_REVISION_UUID);
                removeClientCertificatesStatement.setInt(1, apiId);
                removeClientCertificatesStatement.setString(2, apiRevision.getRevisionUUID());
                removeClientCertificatesStatement.executeUpdate();

                // Removing related revision entries from AM_GRAPHQL_COMPLEXITY table
                PreparedStatement removeGraphQLComplexityStatement = connection.prepareStatement(SQLConstants
                        .APIRevisionSqlConstants.REMOVE_REVISION_ENTRIES_IN_AM_GRAPHQL_COMPLEXITY_BY_REVISION_UUID);
                removeGraphQLComplexityStatement.setInt(1, apiId);
                removeGraphQLComplexityStatement.setString(2, apiRevision.getRevisionUUID());
                removeGraphQLComplexityStatement.executeUpdate();

                // Removing related revision entries for operation policies
                deleteAllAPISpecificOperationPoliciesByAPIUUID(connection, apiRevision.getApiUUID(), apiRevision.getRevisionUUID());

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                handleException("Failed to delete API Revision entry of API Product UUID "
                        + apiRevision.getApiUUID(), e);
            }
        } catch (SQLException e) {
            handleException("Failed to delete API Revision entry of API Product UUID "
                    + apiRevision.getApiUUID(), e);
        }
    }

    /**
     * Retrieve Service Info and Set it to API
     *
     * @param api   API Object
     * @param apiId Internal Unique API Id
     * @throws APIManagementException
     */
    public void setServiceStatusInfoToAPI(API api, int apiId) throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SQLConstants
                     .GET_MD5_VALUE_OF_SERVICE_BY_API_ID_SQL)) {
            preparedStatement.setInt(1, apiId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    JSONObject serviceInfo = new JSONObject();
                    serviceInfo.put("key", resultSet.getString(APIConstants.ServiceCatalogConstants.SERVICE_KEY));
                    serviceInfo.put("name", resultSet.getString(APIConstants.ServiceCatalogConstants
                            .SERVICE_NAME));
                    serviceInfo.put("version", resultSet.getString(APIConstants.ServiceCatalogConstants
                            .SERVICE_VERSION));
                    serviceInfo.put("md5", resultSet.getString("API_SERVICE_MD5"));
                    if (resultSet.getString("SERVICE_MD5").equals(resultSet
                            .getString("API_SERVICE_MD5"))) {
                        serviceInfo.put("outdated", false);
                    } else {
                        serviceInfo.put("outdated", true);
                    }
                    api.setServiceInfo(serviceInfo);
                }
            }
        } catch (SQLException e) {
            handleException("Error while retrieving the service status associated with the API - "
                    + api.getId().getApiName() + "-" + api.getId().getVersion(), e);
        }
    }

    private void addAPIServiceMapping(int apiId, String serviceKey, String md5sum, int tenantId,
                                      Connection connection) throws SQLException {

        String addAPIServiceMappingSQL = SQLConstants.ADD_API_SERVICE_MAPPING_SQL;
        try (PreparedStatement preparedStatement = connection.prepareStatement(addAPIServiceMappingSQL)) {
            preparedStatement.setInt(1, apiId);
            preparedStatement.setString(2, serviceKey);
            preparedStatement.setString(3, md5sum);
            preparedStatement.setInt(4, tenantId);
            preparedStatement.executeUpdate();
        }
    }

    /**
     * Retrieve the Unique Identifier of the Service used in API
     *
     * @param apiId    Unique Identifier of API
     * @param tenantId Tenant ID
     * @return Service Key
     * @throws APIManagementException
     */
    public String retrieveServiceKeyByApiId(int apiId, int tenantId) throws APIManagementException {

        String retrieveServiceKeySQL = SQLConstants.GET_SERVICE_KEY_BY_API_ID_SQL;
        String serviceKey = StringUtils.EMPTY;
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(retrieveServiceKeySQL)) {
            preparedStatement.setInt(1, apiId);
            preparedStatement.setInt(2, tenantId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    serviceKey = resultSet.getString(APIConstants.ServiceCatalogConstants.SERVICE_KEY);
                }
            }
        } catch (SQLException e) {
            handleException("Error while retrieving the Service Key associated with API " + apiId, e);
        }
        return serviceKey;
    }

    /**
     * Retrieve the Unique Identifier of the Service used in API
     *
     * @param apiId    Unique Identifier of API
     * @return Service Key
     * @throws APIManagementException
     */
    private String retrieveServiceKeyByApiId(int apiId, Connection connection) throws APIManagementException {

        String retrieveServiceKeySQL = SQLConstants.GET_SERVICE_KEY_BY_API_ID_SQL_WITHOUT_TENANT_ID;
        String serviceKey = StringUtils.EMPTY;
        try (PreparedStatement preparedStatement = connection.prepareStatement(retrieveServiceKeySQL)) {
            preparedStatement.setInt(1, apiId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    serviceKey = resultSet.getString(APIConstants.ServiceCatalogConstants.SERVICE_KEY);
                }
            }
        } catch (SQLException e) {
            handleException("Error while retrieving the Service Key associated with API " + apiId, e);
        }
        return serviceKey;
    }

    /**
     * Update API Service Mapping entry in AM_API_SERVICE_MAPPING
     *
     * @param apiId      Unique Identifier of API
     * @param serviceKey Unique key of the Service
     * @param md5        MD5 value of the Service
     * @param tenantID   tenantID of API
     * @throws SQLException
     */
    private void updateAPIServiceMapping(int apiId, String serviceKey, String md5, int tenantID, Connection connection)
            throws APIManagementException {
        try {
            if (!retrieveServiceKeyByApiId(apiId, connection).isEmpty()) {
                try (PreparedStatement statement = connection.prepareStatement(SQLConstants.UPDATE_API_SERVICE_MAPPING_SQL)) {
                    statement.setString(1, serviceKey);
                    statement.setString(2, md5);
                    statement.setInt(3, apiId);
                    statement.executeUpdate();
                }
            } else {
                addAPIServiceMapping(apiId, serviceKey, md5, tenantID, connection);
            }
        } catch (SQLException e) {
            handleException("Error while updating the Service info associated with API " + apiId, e);
        }
    }

    private void updateLatestRevisionNumber(Connection connection, String apiUUID, int revisionId) throws SQLException {

        try (PreparedStatement preparedStatement =
                     connection.prepareStatement(SQLConstants.UPDATE_REVISION_CREATED_BY_API_SQL)) {
            preparedStatement.setInt(1, revisionId);
            preparedStatement.setString(2, apiUUID);
            preparedStatement.executeUpdate();
        }
    }

    private void addAPIRevisionMetaData(Connection connection, String apiUUID, String revisionUUID) throws SQLException {

        try (PreparedStatement preparedStatement =
                     connection.prepareStatement(SQLConstants.ADD_API_REVISION_METADATA)) {
            preparedStatement.setString(1, apiUUID);
            preparedStatement.setString(2, revisionUUID);
            preparedStatement.setString(3, apiUUID);
            preparedStatement.executeUpdate();
        }
    }

    private void deleteAPIRevisionMetaData(Connection connection, String apiUUID, String revisionUUID) throws SQLException {

        try (PreparedStatement preparedStatement =
                     connection.prepareStatement(SQLConstants.DELETE_API_REVISION_METADATA)) {
            preparedStatement.setString(1, apiUUID);
            preparedStatement.setString(2, revisionUUID);
            preparedStatement.executeUpdate();
        }
    }

    private void restoreAPIRevisionMetaDataToWorkingCopy(Connection connection, String apiUUID, String revisionUUID) throws SQLException {

        try (PreparedStatement preparedStatement =
                     connection.prepareStatement(SQLConstants.RESTORE_API_REVISION_METADATA)) {
            preparedStatement.setString(1, apiUUID);
            preparedStatement.setString(2, revisionUUID);
            preparedStatement.setString(3, apiUUID);
            preparedStatement.executeUpdate();
        }
    }

    public ApplicationInfo getLightweightApplicationByConsumerKey(String consumerKey) throws APIManagementException {
        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;

        try {
            connection = APIMgtDBUtil.getConnection();
            String query = SQLConstants.GET_APPLICATION_INFO_BY_CK;

            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, consumerKey);

            rs = prepStmt.executeQuery();
            if (rs.next()) {
                ApplicationInfo applicationInfo = new ApplicationInfo();
                applicationInfo.setName(rs.getString("NAME"));
                applicationInfo.setUuid(rs.getString("UUID"));
                applicationInfo.setOrganizationId(rs.getString("ORGANIZATION"));
                applicationInfo.setOwner(rs.getString("OWNER"));
                return applicationInfo;
            }
        } catch (SQLException e) {
            handleException("Error while obtaining organisation of the application for client id " + consumerKey, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
        }
        return null;
    }

    public Set<SubscribedAPI> getSubscribedAPIsByApplication(Application application)
            throws APIManagementException {
        Set<SubscribedAPI> subscribedAPIs = new LinkedHashSet<>();

        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement ps =
                     connection.prepareStatement(SQLConstants.GET_SUBSCRIBED_APIS_BY_APP_ID_SQL)) {
            ps.setInt(1, application.getId());
            try (ResultSet result = ps.executeQuery()) {
                while (result.next()) {
                    String apiType = result.getString("TYPE");
                    if (!APIConstants.API_PRODUCT.toString().equals(apiType)) {
                        APIIdentifier identifier = new APIIdentifier(APIUtil.replaceEmailDomain(result.getString
                                ("API_PROVIDER")), result.getString("API_NAME"),
                                result.getString("API_VERSION"));
                        identifier.setUuid(result.getString("API_UUID"));
                        SubscribedAPI subscribedAPI = new SubscribedAPI(application.getSubscriber(), identifier);
                        subscribedAPI.setApplication(application);
                        initSubscribedAPI(subscribedAPI, result);
                        subscribedAPIs.add(subscribedAPI);
                    }
                }
            }
        } catch (SQLException e) {
            handleException("Failed to get SubscribedAPI of application :" + application.getName(), e);
        }
        return subscribedAPIs;
    }

    public Set<SubscribedAPI> getPaginatedSubscribedAPIsByApplication(Application application, Integer offset,
                                                                      Integer limit, String organization)
            throws APIManagementException {
        Set<SubscribedAPI> subscribedAPIs = new LinkedHashSet<>();

        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement ps =
                     connection.prepareStatement(SQLConstants.GET_PAGINATED_SUBSCRIBED_APIS_BY_APP_ID_SQL)) {
            ps.setInt(1, application.getId());
            ps.setString(2, organization);
            try (ResultSet result = ps.executeQuery()) {
                int index = 0;
                while (result.next()) {
                    if (index >= offset && index < limit) {
                        String apiType = result.getString("TYPE");

                        if (APIConstants.API_PRODUCT.toString().equals(apiType)) {
                            APIProductIdentifier identifier = new APIProductIdentifier(
                                    APIUtil.replaceEmailDomain(result.getString("API_PROVIDER")),
                                    result.getString("API_NAME"), result.getString("API_VERSION"));
                            identifier.setUuid(result.getString("API_UUID"));
                            SubscribedAPI subscribedAPI = new SubscribedAPI(application.getSubscriber(), identifier);
                            subscribedAPI.setApplication(application);
                            initSubscribedAPI(subscribedAPI, result);
                            subscribedAPIs.add(subscribedAPI);
                        } else {
                            APIIdentifier identifier = new APIIdentifier(APIUtil.replaceEmailDomain(result.getString
                                    ("API_PROVIDER")), result.getString("API_NAME"),
                                    result.getString("API_VERSION"));
                            identifier.setUuid(result.getString("API_UUID"));
                            SubscribedAPI subscribedAPI = new SubscribedAPI(application.getSubscriber(), identifier);
                            subscribedAPI.setApplication(application);
                            initSubscribedAPI(subscribedAPI, result);
                            subscribedAPIs.add(subscribedAPI);
                        }

                        if (index == limit - 1) {
                            break;
                        }
                    }
                    index++;

            }
        }

        } catch (SQLException e) {
            handleException("Failed to get SubscribedAPI of application :" + application.getName(), e);
        }

        return subscribedAPIs;

    }

    public void addOperationPolicyMapping(Set<URITemplate> uriTemplates) throws APIManagementException {
        if (uriTemplates != null && !uriTemplates.isEmpty()) {
            try (Connection connection = APIMgtDBUtil.getConnection()) {
                connection.setAutoCommit(false);
                try (PreparedStatement preparedStatement =
                             connection.prepareStatement(SQLConstants.OperationPolicyConstants.ADD_API_OPERATION_POLICY_MAPPING)) {
                    for (URITemplate uriTemplate : uriTemplates){
                        List<OperationPolicy> operationPolicies = uriTemplate.getOperationPolicies();
                        if (operationPolicies != null && !operationPolicies.isEmpty()){
                            for (OperationPolicy operationPolicy : operationPolicies){
                                Gson gson = new Gson();
                                String paramJSON = gson.toJson(operationPolicy.getParameters());
                                preparedStatement.setInt(1, uriTemplate.getId());
                                preparedStatement.setString(2,operationPolicy.getPolicyId());
                                preparedStatement.setString(3, operationPolicy.getDirection());
                                preparedStatement.setString(4, paramJSON);
                                preparedStatement.setInt(5, operationPolicy.getOrder());
                                preparedStatement.addBatch();
                            }
                        }
                    }
                    preparedStatement.executeBatch();
                    connection.commit();
                }catch(SQLException e){
                    connection.rollback();
                    throw e;
                }
            } catch (SQLException e) {
                throw new APIManagementException("Error while updating operation Policy mapping for API", e);
            }
        }
    }

    public boolean hasApplicationPolicyAttachedToApplication(String policyName, String organization) throws APIManagementException {
        try (Connection connection = APIMgtDBUtil.getConnection()) {
            try (PreparedStatement preparedStatement =
                         connection.prepareStatement(ThrottleSQLConstants.TIER_HAS_ATTACHED_TO_APPLICATION)) {
                preparedStatement.setString(1, organization);
                preparedStatement.setString(2,policyName);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    return resultSet.next();
                }
            }
        } catch (SQLException e) {
            handleException("Error while checking existence of Policy " + policyName + "Attached to Application.", e);
        }

        return false;
    }

    public boolean hasSubscriptionPolicyAttached(String policyName, String organization) throws APIManagementException {

        String sql = ThrottleSQLConstants.TIER_HAS_ATTACHED_TO_SUBSCRIPTION_SUPER_TENANT;
        try (Connection connection = APIMgtDBUtil.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setString(1, organization);
                    preparedStatement.setString(2, policyName);
                    preparedStatement.setString(3, organization);
                    preparedStatement.setString(4, policyName);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    return resultSet.next();
                }
            }
        } catch (SQLException e) {
            handleException("Error while checking existence of Policy " + policyName + "Attached to Subscription.", e);
        }

        return false;
    }
    public boolean hasAPIPolicyAttached(String policyName, String organization) throws APIManagementException {

        String sql = ThrottleSQLConstants.TIER_HAS_ATTACHED_TO_API_RESOURCE_TENANT;
        try (Connection connection = APIMgtDBUtil.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setString(1, organization);
                    preparedStatement.setString(2, policyName);
                    preparedStatement.setString(3, organization);
                    preparedStatement.setString(4, policyName);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    return resultSet.next();
                }
            }
        } catch (SQLException e) {
            handleException("Error while checking existence of Policy " + policyName + "Attached to API/Resouce.", e);
        }

        return false;
    }

    private class SubscriptionInfo {

        private int subscriptionId;
        private String tierId;
        private int applicationId;
        private String subscriptionStatus;
        private String apiVersion;

        public SubscriptionInfo(int subscriptionId, String tierId, int applicationId,
                                String apiVersion, String subscriptionStatus) {
            this.subscriptionId = subscriptionId;
            this.tierId = tierId;
            this.applicationId = applicationId;
            this.subscriptionStatus = subscriptionStatus;
            this.apiVersion = apiVersion;
        }

        public String getApiVersion() {
            return apiVersion;
        }

        public int getSubscriptionId() {

            return subscriptionId;
        }

        public void setSubscriptionId(int subscriptionId) {

            this.subscriptionId = subscriptionId;
        }

        public String getTierId() {

            return tierId;
        }

        public void setTierId(String tierId) {

            this.tierId = tierId;
        }

        public int getApplicationId() {

            return applicationId;
        }

        public void setApplicationId(int applicationId) {

            this.applicationId = applicationId;
        }

        public String getSubscriptionStatus() {

            return subscriptionStatus;
        }

        public void setSubscriptionStatus(String subscriptionStatus) {

            this.subscriptionStatus = subscriptionStatus;
        }
    }

    /**
     * Operation policy implementation
     *
     */

    /**
     * Add a new common operation policy to the database. This will first add the operation policy content to the
     * AM_OPERATION_POLICY table and another entry to AM_COMMON_OPERATION_POLICY table.
     *
     * @param policyData Operation policy data.
     * @return UUID of the newly created shared policy
     * @throws APIManagementException
     */
    public String addCommonOperationPolicy(OperationPolicyData policyData) throws APIManagementException {

        String policyUUID = null;
        OperationPolicySpecification policySpecification = policyData.getSpecification();
        try (Connection connection = APIMgtDBUtil.getConnection()) {
            try {
                connection.setAutoCommit(false);
                policyUUID = addOperationPolicyContent(connection, policyData);
                String dbQuery = SQLConstants.OperationPolicyConstants.ADD_COMMON_OPERATION_POLICY;
                try (PreparedStatement statement = connection.prepareStatement(dbQuery)) {
                    statement.setString(1, policyUUID);
                    statement.executeUpdate();
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                handleException("Failed to add common operation policy " + policySpecification.getName(), e);
            }
        } catch (SQLException e) {
            handleException("Failed to add common operation policy " + policySpecification.getName(), e);
        }
        return policyUUID;
    }

    /**
     * Add a new API specific operation policy to the database
     *
     * @param apiUUID      Unique Identifier of API
     * @param revisionUUID Unique Identifier of API revision
     * @param policyData   Unique Identifier of API
     * @return UUID of the newly created shared policy
     * @throws APIManagementException
     */
    public String addAPISpecificOperationPolicy(String apiUUID, String revisionUUID,
                                                OperationPolicyData policyData)
            throws APIManagementException {

        OperationPolicySpecification policySpecification = policyData.getSpecification();
        try (Connection connection = APIMgtDBUtil.getConnection()) {
            try {
                connection.setAutoCommit(false);
                String policyID = addAPISpecificOperationPolicy(connection, policyData, apiUUID, revisionUUID,
                        null, null);
                connection.commit();
                return policyID;
            } catch (SQLException e) {
                connection.rollback();
                handleException("Failed to add API specific operation policy " + policySpecification.getName()
                        + " for API " + apiUUID, e);
            }
        } catch (SQLException e) {
            handleException("Failed to add API specific operation policy " + policySpecification.getName()
                    + " for API " + apiUUID, e);
        }
        return null;
    }

    private String addAPISpecificOperationPolicy(Connection connection, OperationPolicyData policyData,
                                                 String apiUUID, String revisionUUID, String policyUUID,
                                                 String clonedPolicyUUID)
            throws SQLException {

        policyData.setPolicyId(policyUUID);
        policyUUID = addOperationPolicyContent(connection, policyData);

        String dbQuery;
        if (revisionUUID != null) {
            dbQuery = SQLConstants.OperationPolicyConstants.ADD_API_SPECIFIC_OPERATION_POLICY_WITH_REVISION;
        } else {
            dbQuery = SQLConstants.OperationPolicyConstants.ADD_API_SPECIFIC_OPERATION_POLICY;
        }

        try (PreparedStatement statement = connection.prepareStatement(dbQuery)) {
            statement.setString(1, policyUUID);
            statement.setString(2, apiUUID);
            statement.setString(3, clonedPolicyUUID);
            if (revisionUUID != null) {
                statement.setString(4, revisionUUID);
            }
            statement.executeUpdate();
        }
        return policyUUID;
    }

    /**
     * This method is used to populate AM_OPERATION_POLICY table. This will return the policy ID.
     *
     * @param connection DB connection
     * @param policyData Unique Identifier of API
     * @return UUID of the newly created policy
     * @throws SQLException
     */
    private String addOperationPolicyContent(Connection connection, OperationPolicyData policyData)
            throws SQLException {

        OperationPolicySpecification policySpecification = policyData.getSpecification();
        String dbQuery = SQLConstants.OperationPolicyConstants.ADD_OPERATION_POLICY;
        String policyUUID = policyData.getPolicyId();;
        if (policyUUID == null) {
            policyUUID = UUID.randomUUID().toString();
        }

        try (PreparedStatement statement = connection.prepareStatement(dbQuery)) {
            statement.setString(1, policyUUID);
            statement.setString(2, policySpecification.getName());
            statement.setString(3, policySpecification.getVersion());
            statement.setString(4, policySpecification.getDisplayName());
            statement.setString(5, policySpecification.getDescription());
            statement.setString(6, policySpecification.getApplicableFlows().toString());
            statement.setString(7, policySpecification.getSupportedGateways().toString());
            statement.setString(8, policySpecification.getSupportedApiTypes().toString());
            statement.setBinaryStream(9,
                    new ByteArrayInputStream(APIUtil.getPolicyAttributesAsString(policySpecification).getBytes()));
            statement.setString(10, policyData.getOrganization());
            statement.setString(11, policySpecification.getCategory().toString());
            statement.setString(12, policyData.getMd5Hash());
            statement.executeUpdate();

            if (policyData.getSynapsePolicyDefinition() != null) {
                addOperationPolicyDefinition(connection, policyUUID, policyData.getSynapsePolicyDefinition());
            }
            if (policyData.getCcPolicyDefinition() != null) {
                addOperationPolicyDefinition(connection, policyUUID, policyData.getCcPolicyDefinition());
            }
        }
        return policyUUID;
    }

    /**
     * Update an existing operation policy
     *
     * @param policyId   Shared policy UUID
     * @param policyData Updated policy definition
     * @throws APIManagementException
     */
    public void updateOperationPolicy(String policyId, OperationPolicyData policyData)
            throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            connection.setAutoCommit(false);
            updateOperationPolicy(connection, policyId, policyData);
            connection.commit();
        } catch (SQLException e) {
            handleException("Failed to update the operation policy with ID " + policyId, e);
        }
    }

    /**
     * Update an existing operation policy
     *
     * @param connection DB connection
     * @param policyId   Shared policy UUID
     * @param policyData Updated policy definition
     * @throws SQLException
     */
    private void updateOperationPolicy(Connection connection, String policyId, OperationPolicyData policyData)
            throws SQLException {

        OperationPolicySpecification policySpecification = policyData.getSpecification();
        try (PreparedStatement statement = connection.prepareStatement(
                SQLConstants.OperationPolicyConstants.UPDATE_OPERATION_POLICY_CONTENT)) {

            statement.setString(1, policySpecification.getName());
            statement.setString(2, policySpecification.getVersion());
            statement.setString(3, policySpecification.getDisplayName());
            statement.setString(4, policySpecification.getDescription());
            statement.setString(5, policySpecification.getApplicableFlows().toString());
            statement.setString(6, policySpecification.getSupportedGateways().toString());
            statement.setString(7, policySpecification.getSupportedApiTypes().toString());
            statement.setBinaryStream(8,
                    new ByteArrayInputStream(APIUtil.getPolicyAttributesAsString(policySpecification).getBytes()));
            statement.setString(9, policyData.getOrganization());
            statement.setString(10, policySpecification.getCategory().toString());
            statement.setString(11, policyData.getMd5Hash());
            statement.setString(12, policyId);
            statement.executeUpdate();

            if (policyData.getSynapsePolicyDefinition() != null) {
                updateOperationPolicyDefinition(connection, policyId, policyData.getSynapsePolicyDefinition());
            }
            if (policyData.getCcPolicyDefinition() != null) {
                updateOperationPolicyDefinition(connection, policyId, policyData.getCcPolicyDefinition());
            }
        }
    }

    /**
     * Delete an operation policy by providing the policy UUID
     *
     * @param policyId UUID of the policy to be deleted
     * @return True if deleted successfully
     * @throws APIManagementException
     */
    public void deleteOperationPolicyByPolicyId(String policyId) throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            connection.setAutoCommit(false);
            if (!getPolicyUsageByPolicyId(connection, policyId)) {
                deleteOperationPolicyByPolicyId(connection, policyId);
                connection.commit();
            } else {
                throw new APIManagementException("Cannot delete operation policy with id " + policyId
                        + " as policy usages exists");
            }
        } catch (SQLException e) {
            handleException("Failed to delete operation policy " + policyId, e);
        }
    }

    private void deleteOperationPolicyByPolicyId(Connection connection, String policyId) throws SQLException {

        String dbQuery = SQLConstants.OperationPolicyConstants.DELETE_OPERATION_POLICY_BY_ID;
        try (PreparedStatement statement = connection.prepareStatement(dbQuery)) {
            statement.setString(1, policyId);
            statement.execute();
        }
    }

    private boolean getPolicyUsageByPolicyId(Connection connection, String policyId) throws SQLException {

        boolean result = false;
        String dbQuery = SQLConstants.OperationPolicyConstants.GET_EXISTING_POLICY_USAGES_BY_POLICY_UUID;
        try (PreparedStatement statement = connection.prepareStatement(dbQuery)) {
            statement.setString(1, policyId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    result = rs.getInt("POLICY_COUNT") != 0;
                }
            }
        }
        return result;
    }

    /**
     * Get the set of URI templates that have Operation policies
     *
     * @param apiUUID Unique Identifier of API
     * @return URITemplate set
     * @throws APIManagementException
     */
    public Set<URITemplate> getURITemplatesWithOperationPolicies(String apiUUID) throws APIManagementException {

        String query;
        APIRevision apiRevision = checkAPIUUIDIsARevisionUUID(apiUUID);

        if (apiRevision == null) {
            query = SQLConstants.OperationPolicyConstants.GET_OPERATION_POLICIES_OF_API_SQL;
        } else {
            query = SQLConstants.OperationPolicyConstants.GET_OPERATION_POLICIES_FOR_API_REVISION_SQL;
        }

        Map<String, URITemplate> uriTemplates = new HashMap<>();
        Set<URITemplate> uriTemplateList = new HashSet<>();
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            if (apiRevision == null) {
                int apiId = getAPIID(apiUUID, connection);
                prepStmt.setInt(1, apiId);
            } else {
                int apiId = getAPIID(apiRevision.getApiUUID(), connection);
                prepStmt.setInt(1, apiId);
                prepStmt.setString(2, apiRevision.getRevisionUUID());
            }
            try (ResultSet rs = prepStmt.executeQuery()) {
                URITemplate uriTemplate;
                while (rs.next()) {
                    String httpMethod = rs.getString("HTTP_METHOD");
                    String urlPattern = rs.getString("URL_PATTERN");
                    String urlTemplateKey = httpMethod + ":" + urlPattern;
                    if (!uriTemplates.containsKey(urlTemplateKey)) {
                        uriTemplate = new URITemplate();
                    } else {
                        uriTemplate = uriTemplates.get(urlTemplateKey);
                    }
                    OperationPolicy operationPolicy = populateOperationPolicyWithRS(rs);
                    uriTemplate.addOperationPolicy(operationPolicy);
                    uriTemplate.setHTTPVerb(httpMethod);
                    uriTemplate.setUriTemplate(urlPattern);
                    uriTemplate.setId(rs.getInt("URL_MAPPING_ID"));
                    uriTemplates.put(urlTemplateKey, uriTemplate);
                }
            }
            uriTemplateList.addAll(uriTemplates.values());
        } catch (SQLException e) {
            handleException("Error while fetching URI templates with operation policies for " + apiUUID, e);
        }
        return uriTemplateList;
    }

    /**
     * Get operation polycies attached to the resource identified by the url mapping ID
     *
     * @param urlMappingId URL Mapping ID of the resource
     * @return
     * @throws SQLException
     * @throws APIManagementException
     */
    private List<OperationPolicy> getOperationPoliciesOfURITemplate(Connection connection, int urlMappingId)
            throws SQLException, APIManagementException {

        if (connection == null) {
            connection = APIMgtDBUtil.getConnection();
        }
        List<OperationPolicy> operationPolicies = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(
                     SQLConstants.OperationPolicyConstants.GET_OPERATION_POLICIES_BY_URI_TEMPLATE_ID)) {
            ps.setInt(1, urlMappingId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OperationPolicy policy = populateOperationPolicyWithRS(rs);
                    operationPolicies.add(policy);
                }
            }
        }
        return operationPolicies;
    }

    /**
     * Sets operation policies to uriTemplates map
     *
     * @param uuid         UUID of API or API Revision
     * @param uriTemplates URI Templates map with 'URL_PATTERN + HTTP_METHOD' as the map key
     * @throws SQLException
     * @throws APIManagementException
     */
    private void setOperationPoliciesToURITemplatesMap(Connection connection, String uuid, Map<String,
            URITemplate> uriTemplates) throws SQLException, APIManagementException {

        String currentApiUuid;
        String query;
        boolean isRevision = false;
        APIRevision apiRevision = checkAPIUUIDIsARevisionUUID(uuid);
        if (apiRevision != null && apiRevision.getApiUUID() != null) {
            currentApiUuid = apiRevision.getApiUUID();
            query = SQLConstants.OperationPolicyConstants.GET_OPERATION_POLICIES_FOR_API_REVISION_SQL;
            isRevision = true;
        } else {
            query = SQLConstants.OperationPolicyConstants.GET_OPERATION_POLICIES_OF_API_SQL;
            currentApiUuid = uuid;
        }

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            int apiId = getAPIID(currentApiUuid);
            ps.setInt(1, apiId);
            if (isRevision) {
                ps.setString(2, uuid);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String key = rs.getString("URL_PATTERN") + rs.getString("HTTP_METHOD");

                    URITemplate uriTemplate = uriTemplates.get(key);
                    if (uriTemplate != null) {
                        OperationPolicy operationPolicy = populateOperationPolicyWithRS(rs);
                        uriTemplate.addOperationPolicy(operationPolicy);
                    }
                }
            }
        }
    }

    /**
     * Sets operation policies to uriTemplates map
     *
     * @param uuid         UUID of API or API Revision
     * @param uriTemplates URI Templates map with URL_MAPPING_ID as the map key
     * @throws SQLException
     * @throws APIManagementException
     */
    private void setOperationPolicies(Connection connection, String uuid, Map<Integer, URITemplate> uriTemplates)
            throws SQLException, APIManagementException {

        String currentApiUuid;
        String query;
        boolean isRevision = false;
        APIRevision apiRevision = checkAPIUUIDIsARevisionUUID(uuid);
        if (apiRevision != null && apiRevision.getApiUUID() != null) {
            currentApiUuid = apiRevision.getApiUUID();
            query = SQLConstants.OperationPolicyConstants.GET_OPERATION_POLICIES_FOR_API_REVISION_SQL;
            isRevision = true;
        } else {
            query = SQLConstants.OperationPolicyConstants.GET_OPERATION_POLICIES_OF_API_SQL;
            currentApiUuid = uuid;
        }
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            int apiId = getAPIID(currentApiUuid);
            ps.setInt(1, apiId);
            if (isRevision) {
                ps.setString(2, uuid);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int uriTemplateId = rs.getInt("URL_MAPPING_ID");

                    URITemplate uriTemplate = uriTemplates.get(uriTemplateId);
                    if (uriTemplate != null) {
                        OperationPolicy operationPolicy = populateOperationPolicyWithRS(rs);
                        uriTemplate.addOperationPolicy(operationPolicy);
                    }
                }
            }
        }
    }

    /**
     * Populates operation policy mappings in the API Product URITemplate map
     *
     * @param productRevisionId Product Revision ID
     * @param uriTemplates      Map of URI Templates
     * @throws SQLException
     * @throws APIManagementException
     */
    private void setAPIProductOperationPoliciesToURITemplatesMap(Connection connection, String productRevisionId,
                                                                 Map<String, URITemplate> uriTemplates)
            throws SQLException, APIManagementException {

        try (PreparedStatement ps = connection.prepareStatement(
                     SQLConstants.OperationPolicyConstants.GET_OPERATION_POLICIES_PER_API_PRODUCT_SQL)) {
            ps.setString(1, productRevisionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String key = rs.getString("URL_PATTERN") + rs.getString("HTTP_METHOD");

                    URITemplate uriTemplate = uriTemplates.get(key);
                    if (uriTemplate != null) {
                        OperationPolicy operationPolicy = populateOperationPolicyWithRS(rs);
                        uriTemplate.addOperationPolicy(operationPolicy);
                    }
                }
            }
        }
    }

    /**
     * Clone a common policy to the API
     *
     * @param commonPolicyId The policy ID that needs to be cloned
     * @param clonedPolicyId If needed, we can assign the policyId for the coloned policy. This will be an
     *                       API specific policy and if not provided, a new policy UUID will be generated
     * @param apiUUID        The API uuid which the cloned policy will be assigned to
     * @return cloned policyID
     * @throws APIManagementException
     **/
    private String cloneCommonPolicyToAPI(Connection connection, String commonPolicyId, String clonedPolicyId,
                                          String apiUUID) throws APIManagementException, SQLException {
        OperationPolicyData policyData = getOperationPolicyByPolicyID(connection, commonPolicyId, true);
        if (policyData != null) {
            // If we are taking a clone from common policy, common policy's Id is used as the CLONED_POLICY_ID.
            return addAPISpecificOperationPolicy(connection, policyData, apiUUID, null, clonedPolicyId, commonPolicyId);
        } else {
            throw new APIManagementException("Cannot clone common policy with ID " + commonPolicyId
                    + " as it does not exists.");
        }
    }

    /**
     * A clone of API specific policies will be taken and stored against the revision. This can be considered as
     * revisioning the policy. This revisions will be used when restoring and exporting.
     *
     * @param connection          DB connection
     * @param workingCopyPolicyId The policy Id used in the working copy
     * @param revisionedPolicyId  If needed, we can assign the policyId for the revisioned policy. This will be an
     *                            API specific policy and if not provided, a new policy UUID will be generated
     * @param apiUUID             The API uuid which the cloned policy will be assigned to
     * @param revisionUUID        The revision UUID of the API
     * @param organization        Organization name
     * @return cloned policyID
     * @throws APIManagementException
     * @throws SQLException
     **/
    private String cloneAPISpecificPoliciesForRevisioning(Connection connection, String workingCopyPolicyId,
                                                          String revisionedPolicyId, String apiUUID, String revisionUUID,
                                                          String organization)
            throws APIManagementException, SQLException {

        OperationPolicyData policyData = getAPISpecificOperationPolicyByPolicyID(connection, workingCopyPolicyId,
                apiUUID, organization, true);
        // Since we import all the policies to API at API update, getting the policy from API specific policy list is enough.
        // Cloned common policy UUID is mandatory if it's a clone as without it restore will be broken.
        if (policyData != null) {
            return addAPISpecificOperationPolicy(connection, policyData, apiUUID, revisionUUID, revisionedPolicyId,
                    policyData.getClonedCommonPolicyId());
        } else {
            throw new APIManagementException("Cannot create a revision of policy with ID " + workingCopyPolicyId
                    + " as it does not exists.");
        }
    }

    /**
     * A clone of API specific will be taken and cloned when a new version is created.
     *
     * @param previousAPIVersionUUID The UUID of the original API
     * @param newAPIVersionUUID      The UUID of the newly created API version
     * @param organization           The API uuid which the cloned policy will be assigned to
     * @return cloned policyID
     * @throws APIManagementException
     * @throws SQLException
     **/
    public boolean cloneAPISpecificPoliciesForVersioning(String previousAPIVersionUUID, String newAPIVersionUUID,
                                                         String organization, List<ClonePolicyMetadataDTO> clonePolicyMetadata)
            throws APIManagementException {
        try (Connection connection = APIMgtDBUtil.getConnection()) {
            try {
                connection.setAutoCommit(false);

                for (ClonePolicyMetadataDTO toBeClonedPolicyData : clonePolicyMetadata) {
                    cloneAPISpecificPoliciesForVersioning(connection, previousAPIVersionUUID,
                            toBeClonedPolicyData.getCurrentPolicyUUID(), newAPIVersionUUID,
                            toBeClonedPolicyData.getClonedPolicyUUID(), organization);
                }
                connection.commit();
                return true;
            } catch (SQLException | APIManagementException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException | APIManagementException e) {
            throw new APIManagementException("Error while cloning Operation policies", e);
        }
    }

    private String cloneAPISpecificPoliciesForVersioning(Connection connection, String previousAPIVersionUUID,
                                                         String previousPolicyUUID, String newAPIVersionUUID,
                                                         String newPolicyUUID, String organization)
            throws APIManagementException, SQLException {
        OperationPolicyData policyData = getAPISpecificOperationPolicyByPolicyID(connection, previousPolicyUUID,
                previousAPIVersionUUID, organization, true);
        if (policyData != null) {
            return addAPISpecificOperationPolicy(connection, policyData, newAPIVersionUUID, null, newPolicyUUID,
                    policyData.getClonedCommonPolicyId());
        } else {
            throw new APIManagementException("Cannot copy policy with ID " + previousPolicyUUID
                    + " as it does not exists.");
        }
    }

    /**
     * This method is used the restore flow. At the restore, apart from the policy details, CLONED_POLICY_ID column
     * too can change and that needs to be updated.
     *
     * @param connection DB connection
     * @param policyId   Original policy's ID that needs to be cloned
     * @param policyData Updated policy data
     * @throws APIManagementException
     * @throws SQLException
     **/
    private void updateAPISpecificOperationPolicyWithClonedPolicyId(Connection connection, String policyId,
                                                                    OperationPolicyData policyData)
            throws SQLException {

        if (policyData.getClonedCommonPolicyId() != null) {
            try (PreparedStatement statement = connection.prepareStatement(
                    SQLConstants.OperationPolicyConstants.UPDATE_API_OPERATION_POLICY_BY_POLICY_ID)) {
                statement.setString(1, policyData.getClonedCommonPolicyId());
                statement.executeUpdate();
            }
        }
        updateOperationPolicy(connection, policyId, policyData);
    }

    /**
     * This method is used to restore an API specific operation policy revision.
     *
     * @param connection   DB connection
     * @param apiUUID      UUID of the API
     * @param policyId     Original policy's ID that needs to be cloned
     * @param revisionId   The revision number
     * @param organization Organization name
     * @throws SQLException
     * @throws APIManagementException
     **/
    private String restoreOperationPolicyRevision(Connection connection, String apiUUID, String policyId,
                                                  int revisionId, String organization, boolean isProduct)
            throws SQLException, APIManagementException {

        OperationPolicyData revisionedPolicy = getAPISpecificOperationPolicyByPolicyID(connection, policyId,
                apiUUID, organization, true);
        String restoredPolicyId = null;
        if (revisionedPolicy != null) {
            // First check whether there exists a API specific policy for same policy name with revision uuid null
            // This is the state where we record the policies applied in the working copy.
            OperationPolicyData apiSpecificPolicy = getAPISpecificOperationPolicyByPolicyName(connection,
                    revisionedPolicy.getSpecification().getName(), revisionedPolicy.getSpecification().getVersion(),
                    revisionedPolicy.getApiUUID(), null, organization, false);
            if (apiSpecificPolicy != null) {
                if (apiSpecificPolicy.getMd5Hash().equals(revisionedPolicy.getMd5Hash())) {
                    if (log.isDebugEnabled()) {
                        log.debug("Matching API specific operation policy found for the revisioned policy and " +
                                "MD5 hashes match");
                    }

                } else {
                    updateAPISpecificOperationPolicyWithClonedPolicyId(connection, apiSpecificPolicy.getPolicyId(),
                            revisionedPolicy);
                    if (log.isDebugEnabled()) {
                        log.debug("Even though a matching API specific operation policy found for name,"
                                + " MD5 hashes does not match. Policy " + apiSpecificPolicy.getPolicyId()
                                + " has been updated from the revision.");
                    }
                }
                restoredPolicyId = apiSpecificPolicy.getPolicyId();
            } else {
                if (revisionedPolicy.isClonedPolicy()) {
                    // Check for a common operation policy only if it is a cloned policy.
                    OperationPolicyData commonPolicy = getCommonOperationPolicyByPolicyID(connection,
                            revisionedPolicy.getClonedCommonPolicyId(), organization, false);
                    if (commonPolicy != null) {
                        if (commonPolicy.getMd5Hash().equals(revisionedPolicy.getMd5Hash())) {
                            if (log.isDebugEnabled()) {
                                log.debug("Matching common operation policy found. MD5 hash match");
                            }
                            //This means the common policy is same with our revision. A clone is created and original
                            // common policy ID is referenced as the ClonedCommonPolicyId
                            restoredPolicyId = addAPISpecificOperationPolicy(connection, revisionedPolicy, apiUUID,
                                    null, null, revisionedPolicy.getClonedCommonPolicyId());
                        } else {
                            // This means the common policy is updated since we created the revision.
                            // we have to create a clone and since policy is different, we can't refer the original common
                            // policy as ClonedCommonPolicyId. This should be a new API specific policy
                            revisionedPolicy.getSpecification().setName(revisionedPolicy.getSpecification().getName()
                                    + "_restored-" + revisionId);
                            revisionedPolicy.getSpecification()
                                    .setDisplayName(revisionedPolicy.getSpecification().getDisplayName()
                                            + " Restored from revision " + revisionId);
                            revisionedPolicy.setMd5Hash(APIUtil.getMd5OfOperationPolicy(revisionedPolicy));
                            revisionedPolicy.setRevisionUUID(null);
                            restoredPolicyId = addAPISpecificOperationPolicy(connection, revisionedPolicy, apiUUID, null,
                                    null, null);
                            if (log.isDebugEnabled()) {
                                log.debug(
                                        "An updated matching common operation policy found. A new API specific operation " +
                                                "policy created by the display name " +
                                                revisionedPolicy.getSpecification().getName());
                            }
                        }
                    } else {
                        // This means this is a clone of a deleted common policy. A new API specific policy will be created.
                        if (!isProduct) {
                            revisionedPolicy.getSpecification().setName(revisionedPolicy.getSpecification().getName()
                                    + "_restored-" + revisionId);
                            revisionedPolicy.getSpecification()
                                    .setDisplayName(revisionedPolicy.getSpecification().getDisplayName()
                                            + " Restored from revision " + revisionId);
                            revisionedPolicy.setMd5Hash(APIUtil.getMd5OfOperationPolicy(revisionedPolicy));
                            revisionedPolicy.setRevisionUUID(null);
                            restoredPolicyId = addAPISpecificOperationPolicy(connection, revisionedPolicy, apiUUID, null,
                                    null, null);
                            if (log.isDebugEnabled()) {
                                log.debug("No matching operation policy found. A new API specific operation " +
                                        "policy created by the name " + revisionedPolicy.getSpecification().getName());
                            }
                        } else {
                            // API products will have cloned policy Id as API.
                            restoredPolicyId = addAPISpecificOperationPolicy(connection, revisionedPolicy, apiUUID, null,
                                    null, revisionedPolicy.getClonedCommonPolicyId());
                        }
                    }
                } else {
                    // This means this is a completely new policy and we don't have any reference of a previous state in
                    // working copy. A new API specific policy will be created.
                    revisionedPolicy.setRevisionUUID(null);
                    restoredPolicyId = addAPISpecificOperationPolicy(connection, revisionedPolicy, apiUUID,
                            null, null, null);
                    if (log.isDebugEnabled()) {
                        log.debug("No matching operation policy found. A new API specific operation " +
                                "policy created by the name " + revisionedPolicy.getSpecification().getName());
                    }
                }
            }
        } else {
            throw new APIManagementException("A revisioned operation policy not found for " + policyId);
        }
        return restoredPolicyId;
    }

    /**
     * Retrieve an operation policy by providing the policy uuid
     *
     * @param connection             DB connection
     * @param policyId               Policy UUID
     * @param isWithPolicyDefinition Include the policy definition to the output or not
     * @return operation policy
     * @throws SQLException
     */
    private OperationPolicyData getOperationPolicyByPolicyID(Connection connection, String policyId,
                                                             boolean isWithPolicyDefinition) throws SQLException {

        String dbQuery = SQLConstants.OperationPolicyConstants.GET_OPERATION_POLICY_FROM_POLICY_ID;
        OperationPolicyData policyData = null;
        try (PreparedStatement statement = connection.prepareStatement(dbQuery)) {
            statement.setString(1, policyId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    policyData = new OperationPolicyData();
                    policyData.setPolicyId(policyId);
                    policyData.setOrganization(rs.getString("ORGANIZATION"));
                    policyData.setMd5Hash(rs.getString("POLICY_MD5"));
                    policyData.setSpecification(populatePolicySpecificationFromRS(rs));

                    if (isWithPolicyDefinition && policyData != null) {
                        if (isWithPolicyDefinition && policyData != null) {
                            populatePolicyDefinitions(connection, policyId, policyData);
                        }
                    }
                }
            }
        }
        return policyData;
    }

    /**
     * Get the API specific operation policy from the policy ID if exists. This method will take the intersection of AM_OPERATION_POLICY
     * table and AM_API_OPERATION_POLICY table from API UUID. Policy id might be available, but if it is not referenced in the
     * APIS table, this will return null.
     * The returned policy data can be either an API only policy, cloned common policy to API or a revisioned API specific policy
     *
     * @param policyId               Policy UUID
     * @param apiUUID                UUID of the API
     * @param organization           Organization name
     * @param isWithPolicyDefinition Include the policy definition to the output or not
     * @return operation policy
     * @throws APIManagementException
     */
    public OperationPolicyData getAPISpecificOperationPolicyByPolicyID(String policyId, String apiUUID,
                                                                       String organization,
                                                                       boolean isWithPolicyDefinition)
            throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            return getAPISpecificOperationPolicyByPolicyID(connection, policyId, apiUUID, organization,
                    isWithPolicyDefinition);
        } catch (SQLException e) {
            handleException("Failed to get the API specific operation policy for id " + policyId + " from API "
                    + apiUUID, e);
        }
        return null;
    }

    private OperationPolicyData getAPISpecificOperationPolicyByPolicyID(Connection connection, String policyId,
                                                                        String apiUUID,
                                                                        String organization,
                                                                        boolean isWithPolicyDefinition)
            throws SQLException {

        String dbQuery =
                SQLConstants.OperationPolicyConstants.GET_API_SPECIFIC_OPERATION_POLICY_FROM_POLICY_ID;
        OperationPolicyData policyData = null;
        try (PreparedStatement statement = connection.prepareStatement(dbQuery)) {
            statement.setString(1, policyId);
            statement.setString(2, organization);
            statement.setString(3, apiUUID);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    policyData = new OperationPolicyData();
                    policyData.setPolicyId(policyId);
                    policyData.setApiUUID(apiUUID);
                    policyData.setOrganization(organization);
                    policyData.setMd5Hash(rs.getString("POLICY_MD5"));
                    policyData.setRevisionUUID(rs.getString("REVISION_UUID"));
                    policyData.setClonedCommonPolicyId(rs.getString("CLONED_POLICY_UUID"));
                    policyData.setSpecification(populatePolicySpecificationFromRS(rs));

                    if (isWithPolicyDefinition && policyData != null) {
                        if (isWithPolicyDefinition && policyData != null) {
                            populatePolicyDefinitions(connection, policyId, policyData);
                        }
                    }
                }
            }
        }
        return policyData;
    }

    private List<OperationPolicyDefinition> getPolicyDefinitionForPolicyId(Connection connection, String policyId)
            throws SQLException {

        List<OperationPolicyDefinition> operationPolicyDefinitions = new ArrayList<>();

        String dbQuery = SQLConstants.OperationPolicyConstants.GET_OPERATION_POLICY_DEFINITION_FROM_POLICY_ID;
        try (PreparedStatement statement = connection.prepareStatement(dbQuery)) {
            statement.setString(1, policyId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    String policyDefinitionString;
                    OperationPolicyDefinition policyDefinition = new OperationPolicyDefinition();
                    try (InputStream policyDefinitionStream = rs.getBinaryStream("POLICY_DEFINITION")) {
                        policyDefinitionString = IOUtils.toString(policyDefinitionStream);
                        policyDefinition.setContent(policyDefinitionString);
                        policyDefinition.setGatewayType(
                                OperationPolicyDefinition.GatewayType.valueOf(rs.getString("GATEWAY_TYPE")));
                        policyDefinition.setMd5Hash(rs.getString("DEFINITION_MD5"));

                        operationPolicyDefinitions.add(policyDefinition);
                    } catch (IOException e) {
                        log.error("Error while converting policy definition for the policy", e);
                    }

                }
            }
        }
        return operationPolicyDefinitions;
    }


    public void populatePolicyDefinitions(Connection connection, String policyId, OperationPolicyData policyData)
            throws SQLException {
        if (policyId != null && !policyId.isEmpty()) {
            List<OperationPolicyDefinition> policyDefinitions = getPolicyDefinitionForPolicyId(connection, policyId);
            for (OperationPolicyDefinition policyDefinition : policyDefinitions) {
                if (OperationPolicyDefinition.GatewayType.Synapse.equals(policyDefinition.getGatewayType())) {
                    policyData.setSynapsePolicyDefinition(policyDefinition);
                } else if (OperationPolicyDefinition.GatewayType.ChoreoConnect.equals(policyDefinition.getGatewayType())) {
                    policyData.setCcPolicyDefinition(policyDefinition);
                }
            }
        }
    }


    private void addOperationPolicyDefinition(Connection connection, String policyId,
                                               OperationPolicyDefinition policyDefinition) throws SQLException {

        String dbQuery = SQLConstants.OperationPolicyConstants.ADD_OPERATION_POLICY_DEFINITION;
        try (PreparedStatement statement = connection.prepareStatement(dbQuery)) {
            statement.setString(1, policyId);
            statement.setString(2, policyDefinition.getGatewayType().toString());
            statement.setString(3, policyDefinition.getMd5Hash());
            statement.setBinaryStream(4, new ByteArrayInputStream(policyDefinition.getContent().getBytes()));
            statement.executeUpdate();
        }
    }


    private void updateOperationPolicyDefinition(Connection connection, String policyId,
                                               OperationPolicyDefinition policyDefinition) throws SQLException {

        String dbQuery = SQLConstants.OperationPolicyConstants.UPDATE_OPERATION_POLICY_DEFINITION;
        try (PreparedStatement statement = connection.prepareStatement(dbQuery)) {
            statement.setString(1, policyDefinition.getMd5Hash());
            statement.setBinaryStream(2, new ByteArrayInputStream(policyDefinition.getContent().getBytes()));
            statement.setString(3, policyId);
            statement.setString(4, policyDefinition.getGatewayType().toString());
            statement.executeUpdate();
        }
    }

    /**
     * Get the common operation policy from the policy ID if exists. This method will take the intersection of AM_OPERATION_POLICY
     * table and AM_COMMON_OPERATION_POLICY table. Policy id might be available, but if it is not referenced in the
     * common policies table, this will return null.
     *
     * @param policyId               Policy UUID
     * @param organization           Organization name
     * @param isWithPolicyDefinition Include the policy definition to the output or not
     * @return operation policy
     * @throws APIManagementException
     */
    public OperationPolicyData getCommonOperationPolicyByPolicyID(String policyId, String organization,
                                                                  boolean isWithPolicyDefinition)
            throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            return getCommonOperationPolicyByPolicyID(connection, policyId, organization, isWithPolicyDefinition);
        } catch (SQLException e) {
            handleException("Failed to get the operation policy for id " + policyId, e);
        }
        return null;
    }

    private OperationPolicyData getCommonOperationPolicyByPolicyID(Connection connection, String policyId,
                                                                   String organization,
                                                                   boolean isWithPolicyDefinition)
            throws SQLException {

        String dbQuery =
                SQLConstants.OperationPolicyConstants.GET_COMMON_OPERATION_POLICY_WITH_OUT_DEFINITION_FROM_POLICY_ID;
        OperationPolicyData policyData = null;
        try (PreparedStatement statement = connection.prepareStatement(dbQuery)) {
            statement.setString(1, policyId);
            statement.setString(2, organization);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    policyData = new OperationPolicyData();
                    policyData.setPolicyId(policyId);
                    policyData.setOrganization(organization);
                    policyData.setMd5Hash(rs.getString("POLICY_MD5"));
                    policyData.setSpecification(populatePolicySpecificationFromRS(rs));

                    if (isWithPolicyDefinition && policyData != null) {
                        populatePolicyDefinitions(connection, policyId, policyData);
                    }
                }
            }
        }

        return policyData;
    }

    /**
     * Retrieve a common operation policy by providing the policy name and organization
     *
     * @param policyName             Policy name
     * @param policyVersion          Policy version
     * @param organization           Organization name
     * @param isWithPolicyDefinition Include the policy definition to the output or not
     * @return operation policy
     * @throws APIManagementException
     */
    public OperationPolicyData getCommonOperationPolicyByPolicyName(String policyName, String policyVersion,
                                                                    String organization, boolean isWithPolicyDefinition)
            throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            return getCommonOperationPolicyByPolicyName(connection, policyName, policyVersion, organization,
                    isWithPolicyDefinition);
        } catch (SQLException e) {
            handleException("Failed to get common operation policy for name " + policyName + "for organization "
                    + organization, e);
        }
        return null;
    }

    private OperationPolicyData getCommonOperationPolicyByPolicyName(Connection connection, String policyName,
                                                                     String policyVersion, String tenantDomain,
                                                                     boolean isWithPolicyDefinition)
            throws SQLException {

        String dbQuery =
                SQLConstants.OperationPolicyConstants.GET_COMMON_OPERATION_POLICY_FROM_POLICY_NAME;

        OperationPolicyData policyData = null;
        try (PreparedStatement statement = connection.prepareStatement(dbQuery)) {
            statement.setString(1, policyName);
            statement.setString(2, policyVersion);
            statement.setString(3, tenantDomain);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    policyData = new OperationPolicyData();
                    policyData.setOrganization(tenantDomain);
                    policyData.setPolicyId(rs.getString("POLICY_UUID"));
                    policyData.setMd5Hash(rs.getString("POLICY_MD5"));
                    policyData.setSpecification(populatePolicySpecificationFromRS(rs));

                    if (isWithPolicyDefinition && policyData != null) {
                        if (isWithPolicyDefinition && policyData != null) {
                            populatePolicyDefinitions(connection, policyData.getPolicyId(), policyData);
                        }
                    }
                }
            }
        }
        return policyData;
    }

    /**
     * Retrieve an API Specific operation policy by providing the policy name. In order to narrow down the specific policy
     * this needs policy name, apiUUID, api revision UUID (if exists) and organization. If revision UUID is not provided,
     * that means the policy is not a revisioned policy.
     *
     * @param policyName             Policy name
     * @param apiUUID                UUID of API
     * @param revisionUUID           UUID of API revision
     * @param organization           Organization name
     * @param isWithPolicyDefinition Include the policy definition to the output or not
     * @return operation policy
     * @throws APIManagementException
     */
    public OperationPolicyData getAPISpecificOperationPolicyByPolicyName(String policyName, String policyVersion,
                                                                         String apiUUID, String revisionUUID,
                                                                         String organization, boolean isWithPolicyDefinition)
            throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            return getAPISpecificOperationPolicyByPolicyName(connection, policyName, policyVersion, apiUUID,
                    revisionUUID, organization, isWithPolicyDefinition);
        } catch (SQLException e) {
            handleException("Failed to get API specific operation policy for name " + policyName + " with API UUID "
                    + apiUUID + " revision UUID " + revisionUUID, e);
        }
        return null;
    }

    private OperationPolicyData getAPISpecificOperationPolicyByPolicyName(Connection connection,
                                                                          String policyName, String policyVersion,
                                                                          String apiUUID, String revisionUUID,
                                                                          String tenantDomain,
                                                                          boolean isWithPolicyDefinition)
            throws SQLException {

        String dbQuery = SQLConstants.OperationPolicyConstants.GET_API_SPECIFIC_OPERATION_POLICY_FROM_POLICY_NAME;
        if (revisionUUID != null) {
            dbQuery += " AND AOP.REVISION_UUID = ?";
        } else {
            dbQuery += " AND AOP.REVISION_UUID IS NULL";
        }
        OperationPolicyData policyData = null;
        try (PreparedStatement statement = connection.prepareStatement(dbQuery)) {
            statement.setString(1, policyName);
            statement.setString(2, policyVersion);
            statement.setString(3, tenantDomain);
            statement.setString(4, apiUUID);
            if (revisionUUID != null) {
                statement.setString(5, revisionUUID);
            }
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    policyData = new OperationPolicyData();
                    policyData.setOrganization(tenantDomain);
                    policyData.setPolicyId(rs.getString("POLICY_UUID"));
                    policyData.setApiUUID(rs.getString("API_UUID"));
                    policyData.setRevisionUUID(rs.getString("REVISION_UUID"));
                    policyData.setMd5Hash(rs.getString("POLICY_MD5"));
                    policyData.setClonedCommonPolicyId(rs.getString("CLONED_POLICY_UUID"));
                    policyData.setSpecification(populatePolicySpecificationFromRS(rs));
                }

                if (isWithPolicyDefinition && policyData != null) {
                    if (isWithPolicyDefinition && policyData != null) {
                        populatePolicyDefinitions(connection, policyData.getPolicyId(), policyData);
                    }
                }
            }
        }
        return policyData;
    }

    /**
     * Get the list of all operation policies. If the API UUID is provided, this will return all the operation policies
     * for that API. If not, it will return the common operation policies which are not bound to any API.
     * This list will include policy specification of each policy and policy ID. It will not contain the
     * policy definition as it is not useful for the operation.
     *
     * @param apiUUID      UUID of the API if exists. Null for common operation policies
     * @param organization Organization name
     * @return List of Operation Policies
     * @throws APIManagementException
     */
    public List<OperationPolicyData> getLightWeightVersionOfAllOperationPolicies(String apiUUID,
                                                                                 String organization)
            throws APIManagementException {

        String dbQuery;
        if (apiUUID != null) {
            dbQuery =
                    SQLConstants.OperationPolicyConstants.GET_ALL_API_SPECIFIC_OPERATION_POLICIES_WITHOUT_CLONED_POLICIES;
        } else {
            dbQuery = SQLConstants.OperationPolicyConstants.GET_ALL_COMMON_OPERATION_POLICIES;
        }
        List<OperationPolicyData> policyDataList = new ArrayList<>();
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(dbQuery)) {
            statement.setString(1, organization);
            if (apiUUID != null) {
                statement.setString(2, apiUUID);
            }
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    OperationPolicyData policyData = new OperationPolicyData();
                    policyData.setOrganization(organization);
                    policyData.setPolicyId(rs.getString("POLICY_UUID"));
                    policyData.setMd5Hash(rs.getString("POLICY_MD5"));
                    policyData.setSpecification(populatePolicySpecificationFromRS(rs));
                    if (apiUUID != null) {
                        policyData.setApiUUID(apiUUID);
                    }
                    policyDataList.add(policyData);
                }
            }
        } catch (SQLException e) {
            handleException("Failed to get all the operation policy for tenant " + organization, e);
        }
        return policyDataList;
    }

    public Set<String> getCommonOperationPolicyNames(String organization) throws APIManagementException {

        String dbQuery = SQLConstants.OperationPolicyConstants.GET_COMMON_OPERATION_POLICY_NAMES_FOR_ORGANIZATION;
        Set<String> policyNames = new HashSet<>();
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(dbQuery)) {
            statement.setString(1, organization);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    String policyName = rs.getString("POLICY_NAME");
                    String policyVersion = rs.getString("POLICY_VERSION");
                    policyNames.add(APIUtil.getOperationPolicyFileName(policyName, policyVersion));
                }
            }
        } catch (SQLException e) {
            handleException("Failed to get the count of operation policies for organization " + organization, e);
        }
        return policyNames;
    }



    /**
     * This method will return a list of all cloned policies for an API.
     *
     * @param connection DB connection
     * @param apiUUID    UUID of the API if exists. Null for common operation policies
     * @return List of Operation Policies
     * @throws APIManagementException
     */
    private Set<String> getAllClonedPolicyIdsForAPI(Connection connection, String apiUUID)
            throws SQLException {

        String dbQuery = SQLConstants.OperationPolicyConstants.GET_ALL_CLONED_POLICIES_FOR_API;
        Set<String> policyIds = new HashSet<>();
        try (PreparedStatement statement = connection.prepareStatement(dbQuery)) {
            statement.setString(1, apiUUID);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    policyIds.add(rs.getString("POLICY_UUID"));
                }
            }
        }
        return policyIds;
    }

    /**
     * This method will query AM_API_OPERATION_POLICY table from CLONED_POLICY_ID row for a matching policy ID
     * for the required API. This is useful to find the cloned API specific policy ID from a common policy.
     *
     * @param connection     DB connection
     * @param commonPolicyId Common policy ID
     * @param apiUUID        UUID of API
     * @return List of Operation Policies
     * @throws APIManagementException
     */
    private String getClonedPolicyIdForCommonPolicyId(Connection connection, String commonPolicyId, String apiUUID)
            throws SQLException {

        String policyId = null;
        String dbQuery = SQLConstants.OperationPolicyConstants.GET_CLONED_POLICY_ID_FOR_COMMON_POLICY_ID;
        try (PreparedStatement statement = connection.prepareStatement(dbQuery)) {
            statement.setString(1, commonPolicyId);
            statement.setString(2, apiUUID);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    policyId = rs.getString("POLICY_UUID");
                }
            }
        }
        return policyId;
    }

    /**
     * Delete all the API specific policies for a given API UUID. If revision UUID is provided, only the policies that
     * are revisioned will be deleted. This is used when we delete an API revision.
     * If revision id is null, all the API specific policies will be deleted. This is used in API deleting flow.
     *
     * @param connection   DB connection
     * @param apiUUID      UUID of API
     * @param revisionUUID Revision UUID
     * @return List of Operation Policies
     * @throws APIManagementException
     */
    private void deleteAllAPISpecificOperationPoliciesByAPIUUID(Connection connection, String apiUUID,
                                                                String revisionUUID)
            throws SQLException {

        String dbQuery;
        if (revisionUUID != null) {
            dbQuery = SQLConstants.OperationPolicyConstants.GET_ALL_API_SPECIFIC_POLICIES_FOR_REVISION_UUID;
        } else {
            dbQuery = SQLConstants.OperationPolicyConstants.GET_ALL_API_SPECIFIC_POLICIES_FOR_API_ID;
        }
        try (PreparedStatement statement = connection.prepareStatement(dbQuery)) {
            statement.setString(1, apiUUID);
            if (revisionUUID != null) {
                statement.setString(2, revisionUUID);
            }
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    String deleteQuery = SQLConstants.OperationPolicyConstants.DELETE_OPERATION_POLICY_BY_ID;
                    try (PreparedStatement deleteStatement = connection.prepareStatement(deleteQuery)) {
                        deleteStatement.setString(1, rs.getString("POLICY_UUID"));
                        deleteStatement.execute();
                    }
                }
            }
        }
    }

    /**
     * When we apply a common policy to an operation, this policy will be cloned to the API at the backend. However, if
     * that policy is removed from the UI, this method is used to clean such unused policies that are imported,
     * but not used
     *
     * @param connection            DB connection
     * @param usedClonedPoliciesSet Currently used imported API specific policies set
     * @param apiUUID               UUID of the API
     * @throws SQLException
     */
    private void cleanUnusedClonedOperationPolicies(Connection connection, Set<String> usedClonedPoliciesSet,
                                                    String apiUUID)
            throws SQLException {

        Set<String> allClonedPoliciesForAPI = getAllClonedPolicyIdsForAPI(connection, apiUUID);
        Set<String> policiesToDelete = allClonedPoliciesForAPI;
        policiesToDelete.removeAll(usedClonedPoliciesSet);
        for (String policyId : allClonedPoliciesForAPI) {
            deleteOperationPolicyByPolicyId(connection, policyId);
        }
    }

    /**
     * This method will read the result set and populate OperationPolicy object, which later will be set to the URI template.
     * This object has the information regarding the policy allocation
     *
     * @param rs Result set
     * @return OperationPolicy object
     * @throws APIManagementException
     * @throws SQLException
     */
    private OperationPolicy populateOperationPolicyWithRS(ResultSet rs) throws SQLException, APIManagementException {

        OperationPolicy operationPolicy = new OperationPolicy();
        operationPolicy.setPolicyName(rs.getString("POLICY_NAME"));
        operationPolicy.setPolicyVersion(rs.getString("POLICY_VERSION"));
        operationPolicy.setPolicyId(rs.getString("POLICY_UUID"));
        operationPolicy.setOrder(rs.getInt("POLICY_ORDER"));
        operationPolicy.setDirection(rs.getString("DIRECTION"));
        operationPolicy.setParameters(APIMgtDBUtil.convertJSONStringToMap(rs.getString("PARAMETERS")));
        return operationPolicy;
    }

    /**
     * This method will read the result set and populate OperationPolicySpecification object.
     *
     * @param rs Result set
     * @return OperationPolicySpecification object
     * @throws APIManagementException
     * @throws SQLException
     */
    private OperationPolicySpecification populatePolicySpecificationFromRS(ResultSet rs) throws SQLException {

        OperationPolicySpecification policySpecification = new OperationPolicySpecification();
        policySpecification.setName(rs.getString("POLICY_NAME"));
        policySpecification.setVersion(rs.getString("POLICY_VERSION"));
        policySpecification.setDisplayName(rs.getString("DISPLAY_NAME"));
        policySpecification.setDescription(rs.getString("POLICY_DESCRIPTION"));
        policySpecification.setApplicableFlows(getListFromString(rs.getString("APPLICABLE_FLOWS")));
        policySpecification.setSupportedApiTypes(getListFromString(rs.getString("API_TYPES")));
        policySpecification.setSupportedGateways(getListFromString(rs.getString("GATEWAY_TYPES")));
        policySpecification.setCategory(OperationPolicySpecification.PolicyCategory
                .valueOf(rs.getString("POLICY_CATEGORY")));
        List<OperationPolicySpecAttribute> policySpecAttributes = null;

        try (InputStream policyParametersStream = rs.getBinaryStream("POLICY_PARAMETERS")) {
            String policyParametersString = IOUtils.toString(policyParametersStream);
            policySpecAttributes = new Gson().fromJson(policyParametersString,
                    new TypeToken<List<OperationPolicySpecAttribute>>() {
                    }.getType());
        } catch (IOException e) {
            log.error("Error while converting policy specification attributes for the policy "
                    + policySpecification.getName(), e);
        }
        policySpecification.setPolicyAttributes(policySpecAttributes);
        return policySpecification;
    }


    /**
     * Create a string list from a single string element by splitting from the comma
     *
     * @param stringElement String element
     * @return list of strings
     */
    private List<String> getListFromString(String stringElement) {

        List<String> list = null;
        if (!stringElement.isEmpty()) {
            list = Arrays.asList(
                    stringElement.substring(1, stringElement.length() - 1).replaceAll("\\s", "").split(","));
        }
        return list;
    }

}

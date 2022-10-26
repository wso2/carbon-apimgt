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

package org.wso2.apk.apimgt.impl.dao;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.wso2.apk.apimgt.api.APIManagementException;
import org.wso2.apk.apimgt.api.APIMgtResourceNotFoundException;
import org.wso2.apk.apimgt.api.ErrorHandler;
import org.wso2.apk.apimgt.api.ExceptionCodes;
import org.wso2.apk.apimgt.api.SubscriptionAlreadyExistingException;
import org.wso2.apk.apimgt.api.SubscriptionBlockedException;
import org.wso2.apk.apimgt.api.dto.ClientCertificateDTO;
import org.wso2.apk.apimgt.api.dto.ConditionDTO;
import org.wso2.apk.apimgt.api.dto.ConditionGroupDTO;
import org.wso2.apk.apimgt.api.dto.KeyManagerConfigurationDTO;
import org.wso2.apk.apimgt.api.dto.UserApplicationAPIUsage;
import org.wso2.apk.apimgt.api.model.API;
import org.wso2.apk.apimgt.api.model.APICategory;
import org.wso2.apk.apimgt.api.model.APIIdentifier;
import org.wso2.apk.apimgt.api.model.APIInfo;
import org.wso2.apk.apimgt.api.model.APIKey;
import org.wso2.apk.apimgt.api.model.APIProduct;
import org.wso2.apk.apimgt.api.model.APIProductIdentifier;
import org.wso2.apk.apimgt.api.model.APIProductResource;
import org.wso2.apk.apimgt.api.model.APIRevision;
import org.wso2.apk.apimgt.api.model.APIRevisionDeployment;
import org.wso2.apk.apimgt.api.model.APIStatus;
import org.wso2.apk.apimgt.api.model.APIStore;
import org.wso2.apk.apimgt.api.model.ApiTypeWrapper;
import org.wso2.apk.apimgt.api.model.Application;
import org.wso2.apk.apimgt.api.model.ApplicationInfo;
import org.wso2.apk.apimgt.api.model.BlockConditionsDTO;
import org.wso2.apk.apimgt.api.model.Comment;
import org.wso2.apk.apimgt.api.model.CommentList;
import org.wso2.apk.apimgt.api.model.DeployedAPIRevision;
import org.wso2.apk.apimgt.api.model.Environment;
import org.wso2.apk.apimgt.api.model.Identifier;
import org.wso2.apk.apimgt.api.model.KeyManager;
import org.wso2.apk.apimgt.api.model.LifeCycleEvent;
import org.wso2.apk.apimgt.api.model.MonetizationUsagePublishInfo;
import org.wso2.apk.apimgt.api.model.OAuthApplicationInfo;
import org.wso2.apk.apimgt.api.model.OperationPolicy;
import org.wso2.apk.apimgt.api.model.OperationPolicyData;
import org.wso2.apk.apimgt.api.model.OperationPolicyDefinition;
import org.wso2.apk.apimgt.api.model.OperationPolicySpecAttribute;
import org.wso2.apk.apimgt.api.model.OperationPolicySpecification;
import org.wso2.apk.apimgt.api.model.Pagination;
import org.wso2.apk.apimgt.api.model.ResourcePath;
import org.wso2.apk.apimgt.api.model.Scope;
import org.wso2.apk.apimgt.api.model.SharedScopeUsage;
import org.wso2.apk.apimgt.api.model.SubscribedAPI;
import org.wso2.apk.apimgt.api.model.Subscriber;
import org.wso2.apk.apimgt.api.model.Tier;
import org.wso2.apk.apimgt.api.model.URITemplate;
import org.wso2.apk.apimgt.api.model.VHost;
import org.wso2.apk.apimgt.api.model.botDataAPI.BotDetectionData;
import org.wso2.apk.apimgt.api.model.graphql.queryanalysis.CustomComplexityDetails;
import org.wso2.apk.apimgt.api.model.graphql.queryanalysis.GraphqlComplexityInfo;
import org.wso2.apk.apimgt.api.model.policy.APIPolicy;
import org.wso2.apk.apimgt.api.model.policy.ApplicationPolicy;
import org.wso2.apk.apimgt.api.model.policy.BandwidthLimit;
import org.wso2.apk.apimgt.api.model.policy.Condition;
import org.wso2.apk.apimgt.api.model.policy.EventCountLimit;
import org.wso2.apk.apimgt.api.model.policy.GlobalPolicy;
import org.wso2.apk.apimgt.api.model.policy.HeaderCondition;
import org.wso2.apk.apimgt.api.model.policy.IPCondition;
import org.wso2.apk.apimgt.api.model.policy.JWTClaimsCondition;
import org.wso2.apk.apimgt.api.model.policy.Pipeline;
import org.wso2.apk.apimgt.api.model.policy.Policy;
import org.wso2.apk.apimgt.api.model.policy.PolicyConstants;
import org.wso2.apk.apimgt.api.model.policy.QueryParameterCondition;
import org.wso2.apk.apimgt.api.model.policy.QuotaPolicy;
import org.wso2.apk.apimgt.api.model.policy.RequestCountLimit;
import org.wso2.apk.apimgt.api.model.policy.SubscriptionPolicy;
import org.wso2.apk.apimgt.api.model.webhooks.Subscription;
import org.wso2.apk.apimgt.api.model.webhooks.Topic;
import org.wso2.apk.apimgt.impl.APIConstants;
import org.wso2.apk.apimgt.impl.ThrottlePolicyConstants;
import org.wso2.apk.apimgt.impl.alertmgt.AlertMgtConstants;
import org.wso2.apk.apimgt.impl.dao.constants.SQLConstants;
import org.wso2.apk.apimgt.impl.dao.constants.SQLConstants.ThrottleSQLConstants;
import org.wso2.apk.apimgt.impl.dao.util.DBUtils;
import org.wso2.apk.apimgt.impl.dto.APIInfoDTO;
import org.wso2.apk.apimgt.impl.dto.APIKeyInfoDTO;
import org.wso2.apk.apimgt.impl.dto.APISubscriptionInfoDTO;
import org.wso2.apk.apimgt.impl.dto.TierPermissionDTO;
import org.wso2.apk.apimgt.impl.factory.SQLConstantManagerFactory;
import org.wso2.apk.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.apk.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.apk.apimgt.impl.utils.APIUtil;
import org.wso2.apk.apimgt.impl.utils.VHostUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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

    private ApiMgtDAO() {

        String caseSensitiveComparison = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration()
                .getFirstProperty(APIConstants.API_STORE_FORCE_CI_COMPARISIONS);

        forceCaseInsensitiveComparisons = Boolean.parseBoolean(caseSensitiveComparison);
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
                                            String subPolicy, int subscriptionTenantId, int appTenantId, int apiId)
            throws APIManagementException {

        boolean isAnyContentAware = false;
        // only check if using CEP based throttling.
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        String sqlQuery = ThrottleSQLConstants.IS_ANY_POLICY_CONTENT_AWARE_SQL;

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
            ps.setString(2, subscriber.getOrganization());
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
            handleExceptionWithCode("Error while retrieving Monetization Usage Publish Info: ", e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
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
            handleExceptionWithCode("Error while adding monetization usage publish Info: ", e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
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
            ps.setString(2, subscriber.getOrganization());
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
                subscriber.setOrganization(rs.getString("ORGANIZATION"));
                subscriber.setEmail(rs.getString("EMAIL_ADDRESS"));
                subscriber.setSubscribedDate(new Date(rs.getTimestamp("DATE_SUBSCRIBED").getTime()));
                return subscriber;
            }
        } catch (SQLException e) {
            handleException("Error while retrieving subscriber: " + e.getMessage(), e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
        return null;
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
                        "application id:" + applicationId,
                        ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_MESSAGE,
                                "Failed to remove subscription data"));
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
            handleExceptionWithCode("Failed to remove subscriber data " + subscription.getUUID(), e,
                    ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_MESSAGE,
                            "Failed to remove subscription data"));
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, null, resultSet);
            APIMgtDBUtil.closeAllConnections(preparedStForUpdateOrDelete, null, null);
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
            handleExceptionWithCode("Failed to remove all subscriptions data ", e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
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
            String errorMessage = "Failed to retrieve subscription status";
            handleExceptionWithCode(errorMessage, e,
                    ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_MESSAGE, errorMessage));

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
     * @throws APIManagementException if failed to get SubscribedAPIs
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
                    String tenantDomain = APIUtil.getTenantDomain(subscriber.getName());
                    String groupIdArr[] = groupingId.split(",");

                    ps = fillQueryParams(connection, sqlQuery, groupIdArr, 3);
                    String organization = APIUtil.getTenantDomain(subscriber.getName());
                    ps.setString(1, organization);
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
                    String organization = APIUtil.getTenantDomain(subscriber.getName());
                    ps.setString(1, organization);
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
                String organization = APIUtil.getTenantDomain(subscriber.getName());
                ps.setString(1, organization);
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
            handleExceptionWithCode("Failed to get SubscribedAPI of :" + subscriber.getName(), e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, connection, result);
        }
        return subscribedAPIs;
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
                                               String organization, PreparedStatement statement)
            throws SQLException, APIManagementException {

        int tenantId = APIUtil.getTenantId(subscriber.getName());
        int paramIndex = 0;

        if (groupingId != null && !"null".equals(groupingId) && !groupingId.isEmpty()) {
            if (multiGroupAppSharingEnabled) {
                String tenantDomain = APIUtil.getTenantDomain(subscriber.getName());

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

    private ResultSet getSubscriptionResultSet(String groupingId, Subscriber subscriber,
                                               PreparedStatement statement, String organization)
            throws SQLException, APIManagementException {

        String subOrganization = APIUtil.getTenantDomain(subscriber.getName());
        int paramIndex = 0;

        if (groupingId != null && !"null".equals(groupingId) && !groupingId.isEmpty()) {
            if (multiGroupAppSharingEnabled) {
                String tenantDomain = APIUtil.getTenantDomain(subscriber.getName());
                String[] groupIDArray = groupingId.split(",");

                statement.setString(++paramIndex, subOrganization);
                statement.setString(++paramIndex, organization);
                for (String groupId : groupIDArray) {
                    statement.setString(++paramIndex, groupId);
                }
                statement.setString(++paramIndex, tenantDomain);
                statement.setString(++paramIndex, subscriber.getName());
            } else {
                statement.setString(++paramIndex, subOrganization);
                statement.setString(++paramIndex, organization);
                statement.setString(++paramIndex, groupingId);
                statement.setString(++paramIndex, subscriber.getName());
            }
        } else {
            statement.setString(++paramIndex, subOrganization);
            statement.setString(++paramIndex, organization);
            statement.setString(++paramIndex, subscriber.getName());
        }

        return statement.executeQuery();
    }

    private void initSubscribedAPIDetailed(Connection connection, SubscribedAPI subscribedAPI, Subscriber subscriber, ResultSet result)
            throws SQLException {

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
                    KeyManager keyManager = null;
                    // TODO: getKM instance
                            // KeyManagerHolder.getKeyManagerInstance(tenntDomain, keyManagerName);
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
     * @return {@link Set} containing ConsumerKeys
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
            String organization = APIUtil.getTenantDomain(userId);
            //Get subscriber Id
            Subscriber subscriber = getSubscriber(userId, organization, conn);
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
            String organization = APIUtil.getTenantDomain(userId);
            String rateId = null;
            //Get subscriber Id
            Subscriber subscriber = getSubscriber(userId, organization, conn);
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
            String organization = APIUtil.getTenantDomain(userId);
            //Get subscriber Id
            Subscriber subscriber = getSubscriber(userId, organization, conn);
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
            String organization = APIUtil.getTenantDomain(userId);
            //Get subscriber Id
            Subscriber subscriber = getSubscriber(userId, organization, conn);
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
                    String tenantDomain = APIUtil.getTenantDomain(subscriber.getName());
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
                    String tenantDomain = APIUtil.getTenantDomain(subscriber.getName());
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
     * Returns all the consumerkeys of application which are subscribed for the given api
     *
     * @param identifier APIIdentifier
     * @param organization Organization
     * @return Consumerkeys
     * @throws APIManagementException if failed to get Applications for given subscriber.
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
     * returns a subscriber record for given username,tenant Id
     *
     * @param username   UserName
     * @param organization
     * @param connection
     * @return Subscriber
     * @throws APIManagementException if failed to get subscriber
     */
    private Subscriber getSubscriber(String username, String organization, Connection connection)
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
            prepStmt.setString(2, organization);
            rs = prepStmt.executeQuery();

            if (rs.next()) {
                subscriber = new Subscriber(rs.getString("USER_ID"));
                subscriber.setEmail(rs.getString("EMAIL_ADDRESS"));
                subscriber.setId(rs.getInt("SUBSCRIBER_ID"));
                subscriber.setSubscribedDate(rs.getDate("DATE_SUBSCRIBED"));
                subscriber.setOrganization(rs.getString("ORGANIZATION"));
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
            handleExceptionWithCode("Failed to record API state change", e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
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
            throw new APIManagementException(msg, ExceptionCodes.INTERNAL_ERROR);
        } else if (oldStatus != null && oldStatus.equals(newStatus)) {
            String msg = "No measurable differences in API state";
            log.error(msg);
            throw new APIManagementException(msg, ExceptionCodes.INTERNAL_ERROR);
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
                                            throw new APIManagementException(msg,
                                                    ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_MESSAGE, msg));
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
            handleExceptionWithCode("Error when executing the SQL queries", e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
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
            handleExceptionWithCode("Failed to get API versions matches API name" + apiName, e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
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
            handleExceptionWithCode("Failed to count contexts which match " + contextTemplate + " for the organization : "
                    + organization, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        return false;
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
            handleExceptionWithCode("Error while deleting the API default version entry: " + apiIdList.stream().
                    map(APIIdentifier::getApiName).collect(Collectors.joining(",")) + " from the " +
                    "database", e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
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
            handleExceptionWithCode("Error while getting default version for " + apiId.getApiName(), e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
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
            handleExceptionWithCode("Error while adding the API default version entry: " + api.getId().getApiName()
                    + " to " + "the database", e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmtDefVersionAdd, null, null);
        }
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
            if (APIConstants.SUPER_TENANT_DOMAIN.equalsIgnoreCase(tenant)) {
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

    public int getAPIID(String uuid) throws APIManagementException {
        int id = -1;
        try {
            try (Connection connection = APIMgtDBUtil.getConnection()) {
                return getAPIID(uuid, connection);
            }
        } catch (SQLException e) {
            handleExceptionWithCode("Error while locating API with UUID : " + uuid + " from the database", e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
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
                    throw new APIManagementException(msg, ExceptionCodes.API_NOT_FOUND);
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
            handleExceptionWithCode("Error while locating API Product: " + apiProductIdentifier.getName() + "-" +
                            APIUtil.replaceEmailDomainBack(apiProductIdentifier.getProviderName())
                            + "-" + apiProductIdentifier.getVersion() + " from the database", e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
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

    private void handleException(String msg, Throwable t) throws APIManagementException {

        log.error(msg, t);
        throw new APIManagementException(msg, t);
    }

    private void handleExceptionWithCode(String msg, Throwable t, ErrorHandler code) throws APIManagementException {
        log.error(msg, t);
        throw new APIManagementException(msg, code);
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
            handleExceptionWithCode("Failed to add comment data, for API with UUID " + uuid,
                    e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
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
                throw new APIManagementException(msg, ExceptionCodes.API_NOT_FOUND);
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
            handleExceptionWithCode("Failed to retrieve comment for API " + identifier.getName() + "with comment ID " +
                    commentId, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
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
                throw new APIManagementException(msg, ExceptionCodes.API_NOT_FOUND);
            }
            commentList = getComments(currentApiUuid, parentCommentID, limit, offset, connection);
        } catch (SQLException e) {
            handleExceptionWithCode("Failed to retrieve comments for  " + apiTypeWrapper.getName(), e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
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
                throw new APIManagementException(msg, ExceptionCodes.API_NOT_FOUND);
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
            handleExceptionWithCode("Error while editing comment " + commentId + " from the database", e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
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
            handleExceptionWithCode("Error while deleting comment " + commentId + " from the database", e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
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
            handleExceptionWithCode("Error while deleting comment " + commentId + " from the database", e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
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
     * @throws APIManagementException
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
            handleExceptionWithCode("Failed to get the UUID for API : ", e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
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
        return getUUIDFromIdentifier(identifier, organization, null);
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
            handleExceptionWithCode("Failed to retrieve the UUID for the API Product : " + identifier.getName() + '-'
                    + identifier.getVersion(), e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
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

        Map<String, Map<String, String>> loginConfiguration = null;
        // TODO: Read from LoginConfiguration
//                ServiceReferenceHolder.getInstance()
//                .getAPIManagerConfigurationService().getAPIManagerConfiguration().getLoginConfiguration();
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

        Map<String, Map<String, String>> loginConfiguration = null;
          // TODO:  read from LoginConfiguration
//        ServiceReferenceHolder.getInstance()
//                .getAPIManagerConfigurationService().getAPIManagerConfiguration().getLoginConfiguration();
        String claimURI, username = null;
        if (isUserLoggedInEmail(login)) {
            Map<String, String> emailConf = loginConfiguration.get(APIConstants.EMAIL_LOGIN);
            claimURI = emailConf.get(APIConstants.CLAIM_URI);
        } else {
            Map<String, String> userIdConf = loginConfiguration.get(APIConstants.USERID_LOGIN);
            claimURI = userIdConf.get(APIConstants.CLAIM_URI);
        }

        try {
            // TODO:  read from RemoteUserManagerClient
            String[] user = new String[0];
                    // RemoteUserManagerClient.getInstance().getUserList(claimURI, login);
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
            handleExceptionWithCode("Failed to retrieve api scope keys ", e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, resultSet);
        }
        return scopeKeySet;
    }

    /**
     * Delete a user subscription based on API_ID, APP_ID, TIER_ID
     *
     * @param apiId - subscriber API ID
     * @param appId - application ID used to subscribe
     * @throws SQLException - Letting the caller to handle the roll back
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
        if (!APIConstants.SUPER_TENANT_DOMAIN.equals(tenantDomain)) {
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
            handleExceptionWithCode("Failed to check api Name availability : " + apiName, e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
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
        if (!APIConstants.SUPER_TENANT_DOMAIN.equals(tenantDomain)) {
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
            handleExceptionWithCode("Failed to check different letter case api name availability : " + apiName, e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
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
            handleExceptionWithCode("Failed to check scope key availability for: " + scopeKey, e,
                    ExceptionCodes.from(ExceptionCodes.FAILED_CHECKING_SCOPE_KEY_AVAILABILITY, scopeKey));
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
            handleExceptionWithCode("Failed to retrieve alert types ", e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
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
            handleExceptionWithCode("Failed to retrieve saved alert types by user name. ", e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
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
            handleExceptionWithCode("Failed to retrieve saved alert types by user name. ", e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
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
            handleExceptionWithCode("Failed to delete alert email data.", e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
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
            query = ThrottleSQLConstants.UPDATE_API_POLICY_STATUS_SQL;
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
            handleExceptionWithCode("Failed to check is exist: " + policyName + '-' + tenantId, e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
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
                    handleExceptionWithCode("Failed to rollback checking Block condition with context " + context, ex,
                            ExceptionCodes.APIMGT_DAO_EXCEPTION);
                }
            }
            handleExceptionWithCode("Failed to check Block condition with context " + context, e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
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
                    handleExceptionWithCode(
                            "Failed to rollback checking Block condition with Application Name " + appName + " with "
                                    + "Application Owner" + appOwner, ex, ExceptionCodes.APIMGT_DAO_EXCEPTION);
                }
            }
            handleExceptionWithCode("Failed to check Block condition with Application Name " + appName + " with " +
                    "Application Owner" + appOwner, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        } finally {
            APIMgtDBUtil.closeAllConnections(validateContextPreparedStatement, connection, resultSet);
        }
        return status;
    }

    private boolean isBlockConditionExist(String conditionType, String conditionValue, String tenantDomain, Connection
            connection) throws APIManagementException {

        PreparedStatement checkIsExistPreparedStatement = null;
        ResultSet checkIsResultSet = null;
        boolean status = false;
        try {
            String isExistQuery = ThrottleSQLConstants.BLOCK_CONDITION_EXIST_SQL;
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
            handleExceptionWithCode(msg, e,ExceptionCodes.APIMGT_DAO_EXCEPTION);
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
        String organization = APIUtil.getTenantDomain(loginUserName);

        String sqlQuery = SQLConstants.GET_SUBSCRIBED_APIS_OF_USER_BY_APP_SQL;
        if (forceCaseInsensitiveComparisons) {
            sqlQuery = SQLConstants.GET_SUBSCRIBED_APIS_OF_USER_BY_APP_CASE_INSENSITIVE_SQL;
        }

        try {
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            ps.setString(1, organization);
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
            handleExceptionWithCode("Failed to add VHosts for environment ID: " + id, e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
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
                    vhosts.add(vhost);
                }
            }
        } catch (SQLException e) {
            handleExceptionWithCode("Failed to get gateway environments list of VHost: ", e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        return vhosts;
    }

    private void addApplicationAttributes(Connection conn, Map<String, String> attributes, int applicationId,
                                          String organization)
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
                        ps.setString(4, organization);
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
            handleExceptionWithCode("Error while retrieving api product id for product " + identifier.getName() + " by " +
                    APIUtil.replaceEmailDomainBack(identifier.getProviderName()), e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        } finally {
            APIMgtDBUtil.closeAllConnections(preparedStatement, conn, rs);
        }
        return productId;
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

                            resource.setUriTemplate(uriTemplate);
                            productResourceList.add(resource);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            handleExceptionWithCode("Failed to get product resources of api product : " + productIdentifier, e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
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
            handleExceptionWithCode("Error while adding new audit api id: ", e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
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
            handleExceptionWithCode("Error while getting audit api id: ", e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
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
            handleExceptionWithCode("Error while adding custom complexity details: ", ex,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
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
            handleExceptionWithCode("Error while updating custom complexity details: ", e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
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
            handleExceptionWithCode("Error while updating custom complexity details: ", ex,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
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
            handleExceptionWithCode("Error while retrieving custom complexity details: ", ex,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
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
            handleExceptionWithCode("Error while adding bot detection alert subscription", e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
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
            handleExceptionWithCode("Error while retrieving bot detection alert subscriptions", e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
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
            handleExceptionWithCode("Error while deleting bot detection alert subscription", e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
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
            handleExceptionWithCode("Failed to retrieve bot detection alert subscription of " + field + ": " + value,
                    e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        return alertSubscription;
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
            handleExceptionWithCode("Failed to add Category: " + uuid, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
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
            handleExceptionWithCode("Failed to update API Category : " + apiCategory.getName() + " of tenant " +
                            APIUtil.getTenantDomainFromTenantId(apiCategory.getTenantID()), e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
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
            handleExceptionWithCode("Failed to retrieve API categories for organization " + organization,
                    e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
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
            handleExceptionWithCode("Failed to check whether API category name : " + categoryName + " exists",
                    e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
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
            handleExceptionWithCode("Failed to fetch API category : " + apiCategoryID, e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        return apiCategory;
    }

    public void deleteCategory(String categoryID) throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQLConstants.DELETE_API_CATEGORY)) {
            statement.setString(1, categoryID);
            statement.executeUpdate();
        } catch (SQLException e) {
            handleExceptionWithCode("Failed to delete API category : " + categoryID,
                    e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
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
     * Adds a tenant theme to the database
     *
     * @param organization     tenant ID of user
     * @param themeContent content of the tenant theme
     * @throws APIManagementException if an error occurs when adding a tenant theme to the database
     */
    public void addTenantTheme(String organization, InputStream themeContent) throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection
                     .prepareStatement(SQLConstants.TenantThemeConstants.ADD_TENANT_THEME)) {
            statement.setString(1, organization);
            statement.setBinaryStream(2, themeContent);
            statement.executeUpdate();
        } catch (SQLException e) {
            handleExceptionWithCode("Failed to add tenant theme of tenant "
                    + organization, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
    }

    /**
     * Updates an existing tenant theme in the database
     *
     * @param organization     tenant ID of user
     * @param themeContent content of the tenant theme
     * @throws APIManagementException if an error occurs when updating an existing tenant theme in the database
     */
    public void updateTenantTheme(String organization, InputStream themeContent) throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(SQLConstants.TenantThemeConstants.UPDATE_TENANT_THEME)) {
            statement.setBinaryStream(1, themeContent);
            statement.setString(2, organization);
            statement.executeUpdate();
        } catch (SQLException e) {
            handleExceptionWithCode("Failed to update tenant theme of tenant "
                    + organization, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
    }

    /**
     * Retrieves a tenant theme from the database
     *
     * @param organization tenant ID of user
     * @return content of the tenant theme
     * @throws APIManagementException if an error occurs when retrieving a tenant theme from the database
     */
    public InputStream getTenantTheme(String organization) throws APIManagementException {

        InputStream tenantThemeContent = null;
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection
                     .prepareStatement(SQLConstants.TenantThemeConstants.GET_TENANT_THEME)) {
            statement.setString(1, organization);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                tenantThemeContent = resultSet.getBinaryStream("THEME");
            }
        } catch (SQLException e) {
            handleExceptionWithCode("Failed to fetch tenant theme of tenant "
                    + organization, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        return tenantThemeContent;
    }

    /**
     * Checks whether a tenant theme exist for a particular tenant
     *
     * @param organization tenant ID of user
     * @return true if a tenant theme exist for a particular tenant ID, false otherwise
     * @throws APIManagementException if an error occurs when determining whether a tenant theme exists for a given
     *                                tenant ID
     */
    public boolean isTenantThemeExist(String organization) throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection
                     .prepareStatement(SQLConstants.TenantThemeConstants.GET_TENANT_THEME)) {
            statement.setString(1, organization);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            handleExceptionWithCode("Failed to check whether tenant theme exist for tenant "
                    + organization, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        return false;
    }

    /**
     * Deletes a tenant theme from the database
     *
     * @param organization tenant ID of user
     * @throws APIManagementException if an error occurs when deleting a tenant theme from the database
     */
    public void deleteTenantTheme(String organization) throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection
                     .prepareStatement(SQLConstants.TenantThemeConstants.DELETE_TENANT_THEME)) {
            statement.setString(1, organization);
            statement.executeUpdate();
        } catch (SQLException e) {
            handleExceptionWithCode("Failed to delete tenant theme of tenant "
                    + organization, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
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
            handleExceptionWithCode("Error while retrieving versions for api " + apiName + " for the provider "
                    + apiProvider, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        return versions;
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
            handleExceptionWithCode("Failed to get revision UUID for Revision " + revisionNum, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        return revisionUUID;
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
            handleExceptionWithCode("Failed to search UUID: " + apiUUID + " in the revision db table", e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        return null;
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
            handleExceptionWithCode("Failed to get API Revision deployment mapping details for api uuid: " +
                    apiUUID, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        return false;
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
            handleExceptionWithCode("Error while retrieving the service status associated with the API - "
                    + api.getId().getApiName() + "-" + api.getId().getVersion(), e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
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

    public String getUUIDFromIdentifier(Identifier apiIdentifier, String organization) throws APIManagementException {
        if (apiIdentifier instanceof APIProductIdentifier) {
            return getUUIDFromIdentifier((APIProductIdentifier) apiIdentifier, organization);
        } else {
            return getUUIDFromIdentifier((APIIdentifier) apiIdentifier, organization);
        }
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

    public Set<String> getCommonOperationPolicyNames(String organization) throws APIManagementException {

        String dbQuery = SQLConstants.OperationPolicyConstants.GET_COMMON_OPERATION_POLICY_NAMES_FOR_ORGANIZATION;
        Set<String> policyNames = new HashSet<>();
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(dbQuery)) {
            statement.setString(1, organization);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                String policyName = rs.getString("POLICY_NAME");
                String policyVersion = rs.getString("POLICY_VERSION");
                policyNames.add(APIUtil.getOperationPolicyFileName(policyName, policyVersion));
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
        PreparedStatement statement = connection.prepareStatement(dbQuery);
        statement.setString(1, apiUUID);
        ResultSet rs = statement.executeQuery();
        while (rs.next()) {
            policyIds.add(rs.getString("POLICY_UUID"));
        }
        rs.close();
        statement.close();
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

        String dbQuery = SQLConstants.OperationPolicyConstants.GET_CLONED_POLICY_ID_FOR_COMMON_POLICY_ID;
        PreparedStatement statement = connection.prepareStatement(dbQuery);
        statement.setString(1, commonPolicyId);
        statement.setString(2, apiUUID);
        ResultSet rs = statement.executeQuery();
        String policyId = null;
        if (rs.next()) {
            policyId = rs.getString("POLICY_UUID");
        }
        rs.close();
        statement.close();
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
        PreparedStatement statement = connection.prepareStatement(dbQuery);
        statement.setString(1, apiUUID);
        if (revisionUUID != null) {
            statement.setString(2, revisionUUID);
        }
        ResultSet rs = statement.executeQuery();
        while (rs.next()) {
            String deleteQuery = SQLConstants.OperationPolicyConstants.DELETE_OPERATION_POLICY_BY_ID;
            PreparedStatement deleteStatement = connection.prepareStatement(deleteQuery);
            deleteStatement.setString(1, rs.getString("POLICY_UUID"));
            deleteStatement.execute();
            deleteStatement.close();
        }
        rs.close();
        statement.close();
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
        policy.setTenantDomain(APIUtil.getTenantDomainFromTenantId(policy.getTenantId()));
        policy.setDefaultQuotaPolicy(quotaPolicy);
        policy.setDeployed(resultSet.getBoolean(ThrottlePolicyConstants.COLUMN_DEPLOYED));
    }

}

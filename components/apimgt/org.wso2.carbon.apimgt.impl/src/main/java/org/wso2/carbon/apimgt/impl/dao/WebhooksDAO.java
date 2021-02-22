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
package org.wso2.carbon.apimgt.impl.dao;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.webhooks.Subscription;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.constants.SQLConstants;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * This class represent the WebhooksSubscriptionDAO.
 */

public class WebhooksDAO {

    private static final Log log = LogFactory.getLog(WebhooksDAO.class);
    private static WebhooksDAO subscriptionsDAO = null;
    /**
     * Private constructor
     */
    private WebhooksDAO() {
    }

    /**
     * Method to get the instance of the WebhooksSubscriptionDAO.
     *
     * @return {@link WebhooksDAO} instance
     */
    public static WebhooksDAO getInstance() {
        if (subscriptionsDAO == null) {
            subscriptionsDAO = new WebhooksDAO();
        }
        return subscriptionsDAO;
    }


    /*
     * This method can be used to insert webhooks subscriptions to the database
     *
     * @param properties Subscription request properties
     * */
    public boolean addSubscription(Properties properties) throws APIManagementException {

        try (Connection conn = APIMgtDBUtil.getConnection()) {
            try {
                conn.setAutoCommit(false);
                int id = findSubscription(conn, properties);
                if (id == 0) {
                    int throttleLimit = getAllowedConnectionsCount(conn, properties);
                    int currentLimit = getCurrentConnectionsCount(conn, properties);
                    if (currentLimit >= throttleLimit) {
                        return false;
                    }
                    addSubscription(conn, properties);
                } else {
                    updateSubscription(conn, properties, id);
                }
                conn.commit();
            } catch (SQLException e) {
                handleConnectionRollBack(conn);
                throw new APIManagementException("Error while storing webhooks unsubscription request for callback" +
                        properties.getProperty(APIConstants.Webhooks.CALLBACK) + " for the API " +
                        properties.getProperty(APIConstants.Webhooks.API_UUID), e);
            }
        } catch (SQLException e) {
            throw new APIManagementException("Error while storing subscription with callback " +
                    properties.getProperty(APIConstants.Webhooks.CALLBACK) + " for the API " +
                    properties.getProperty(APIConstants.Webhooks.API_UUID), e);
        }
        return true;
    }

    /*
     * This method can be used to check whether the  webhooks subscriptions is throttled out or not
     *
     * @param properties Subscription request properties
     * */
    public boolean isThrottled(Properties properties) throws APIManagementException {

        try (Connection conn = APIMgtDBUtil.getConnection()) {
            int throttleLimit = getAllowedConnectionsCount(conn, properties);
            int currentLimit = getCurrentConnectionsCount(conn, properties);
            return currentLimit > throttleLimit;
        } catch (SQLException e) {
            throw new APIManagementException("Error while storing subscription with callback " +
                    properties.getProperty(APIConstants.Webhooks.CALLBACK) + " for the API " +
                    properties.getProperty(APIConstants.Webhooks.API_UUID), e);
        }
    }

    private int getAllowedConnectionsCount(Connection conn, Properties properties) throws APIManagementException {
        try (PreparedStatement preparedStatement = conn
                .prepareStatement(SQLConstants.WebhooksSqlConstants.GET_THROTTLE_LIMIT)) {
            preparedStatement.setString(1, properties.getProperty(APIConstants.Webhooks.TIER));
            preparedStatement.setInt(2, (Integer) properties.get(APIConstants.Webhooks.TENANT_ID));
            int id = 0;
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    id = rs.getInt(APIConstants.Webhooks.CONNECTIONS_COUNT_COLUMN);
                }
            }
            return id;
        } catch (SQLException e) {
            throw new APIManagementException("Error while select existing subscriptions request for callback" +
                    properties.getProperty(APIConstants.Webhooks.CALLBACK) + " for the API " +
                    properties.getProperty(APIConstants.Webhooks.API_UUID), e);
        }
    }

    private int getCurrentConnectionsCount(Connection conn, Properties properties) throws APIManagementException {
        try (PreparedStatement preparedStatement = conn
                .prepareStatement(SQLConstants.WebhooksSqlConstants.GET_CURRENT_CONNECTIONS_COUNT)) {
            preparedStatement.setString(1, properties.getProperty(APIConstants.Webhooks.API_UUID));
            preparedStatement.setString(2, properties.getProperty(APIConstants.Webhooks.APP_ID));
            preparedStatement.setString(3, properties.getProperty(APIConstants.Webhooks.TENANT_DOMAIN));
            int id = 0;
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    id = rs.getInt(APIConstants.Webhooks.SUB_COUNT_COLUMN);
                }
            }
            return id;
        } catch (SQLException e) {
            throw new APIManagementException("Error while select existing subscriptions request for callback" +
                    properties.getProperty(APIConstants.Webhooks.CALLBACK) + " for the API " +
                    properties.getProperty(APIConstants.Webhooks.API_UUID), e);
        }
    }

    private void addSubscription(Connection conn, Properties properties) throws APIManagementException {
        try (PreparedStatement prepareStmt = conn
                .prepareStatement(SQLConstants.WebhooksSqlConstants.ADD_SUBSCRIPTION)) {
            prepareStmt.setString(1, properties.getProperty(APIConstants.Webhooks.API_UUID));
            prepareStmt.setString(2, properties.getProperty(APIConstants.Webhooks.APP_ID));
            prepareStmt.setString(3, properties.getProperty(APIConstants.Webhooks.TENANT_DOMAIN));
            prepareStmt.setString(4, properties.getProperty(APIConstants.Webhooks.CALLBACK));
            prepareStmt.setString(5, properties.getProperty(APIConstants.Webhooks.TOPIC));
            String secret = properties.getProperty(APIConstants.Webhooks.SECRET);
            String encryptedSecret = null;
            if (!StringUtils.isEmpty(secret)) {
                encryptedSecret = encryptSecret(properties.getProperty(APIConstants.Webhooks.SECRET));
            }
            prepareStmt.setString(6, encryptedSecret);
            prepareStmt.setString(7, properties.getProperty(APIConstants.Webhooks.LEASE_SECONDS));
            Timestamp updatedTime = (Timestamp) properties.get(APIConstants.Webhooks.UPDATED_AT);
            prepareStmt.setTimestamp(8, updatedTime);
            long expiryTime = Long.parseLong(properties.getProperty(APIConstants.Webhooks.EXPIRY_AT));
            prepareStmt.setLong(9, expiryTime);
            prepareStmt.setString(10, null);
            prepareStmt.setInt(11, 0);
            prepareStmt.executeUpdate();
        } catch (SQLException | CryptoException e) {
            throw new APIManagementException("Error while adding subscriptions request for callback" +
                    properties.getProperty(APIConstants.Webhooks.CALLBACK) + " for the API " +
                    properties.getProperty(APIConstants.Webhooks.API_UUID), e);
        }
    }

    private void updateSubscription(Connection conn, Properties properties, int id) throws APIManagementException {
        try (PreparedStatement prepareStmt = conn
                .prepareStatement(SQLConstants.WebhooksSqlConstants.UPDATE_EXISTING_SUBSCRIPTION)) {
            String secret = properties.getProperty(APIConstants.Webhooks.SECRET);
            String encryptedSecret = null;
            if (!StringUtils.isEmpty(secret)) {
                encryptedSecret = encryptSecret(properties.getProperty(APIConstants.Webhooks.SECRET));
            }
            prepareStmt.setString(1, encryptedSecret);
            prepareStmt.setString(2, properties.getProperty(APIConstants.Webhooks.LEASE_SECONDS));
            Timestamp updatedTime = (Timestamp) properties.get(APIConstants.Webhooks.UPDATED_AT);
            prepareStmt.setTimestamp(3, updatedTime);
            long expiryTime = Long.parseLong(properties.getProperty(APIConstants.Webhooks.EXPIRY_AT));
            prepareStmt.setLong(4, expiryTime);
            prepareStmt.setInt(5, id);
            prepareStmt.executeUpdate();
        } catch (SQLException | CryptoException e) {
            throw new APIManagementException("Error while deleting existing subscriptions request for callback" +
                    properties.getProperty(APIConstants.Webhooks.CALLBACK) + " for the API " +
                    properties.getProperty(APIConstants.Webhooks.API_UUID), e);
        }
    }

    private int findSubscription(Connection conn, Properties properties) throws APIManagementException {
        try (PreparedStatement preparedStatement = conn
                .prepareStatement(SQLConstants.WebhooksSqlConstants.FIND_SUBSCRIPTION)) {
            preparedStatement.setString(1, properties.getProperty(APIConstants.Webhooks.API_UUID));
            preparedStatement.setString(2, properties.getProperty(APIConstants.Webhooks.APP_ID));
            preparedStatement.setString(3, properties.getProperty(APIConstants.Webhooks.TENANT_DOMAIN));
            preparedStatement.setString(4, properties.getProperty(APIConstants.Webhooks.CALLBACK));
            preparedStatement.setString(5, properties.getProperty(APIConstants.Webhooks.TOPIC));
            int id = 0;
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    id = rs.getInt(APIConstants.Webhooks.WH_SUBSCRIPTION_ID_COLUMN);
                }
            }
            return id;
        } catch (SQLException e) {
            throw new APIManagementException("Error while select existing subscriptions request for callback" +
                    properties.getProperty(APIConstants.Webhooks.CALLBACK) + " for the API " +
                    properties.getProperty(APIConstants.Webhooks.API_UUID), e);
        }
    }

    /*
     * This method can be used to add webhooks unsubscription request to the database
     *
     * @param properties
     * */
    public void updateUnSubscription(Properties properties) throws APIManagementException {
        try (Connection conn = APIMgtDBUtil.getConnection()) {
            try {
                conn.setAutoCommit(false);
                deleteSubscription(conn, properties);
                addUnsubscription(conn, properties);
                conn.commit();
            } catch (SQLException e) {
                handleConnectionRollBack(conn);
                throw new APIManagementException("Error while storing webhooks unsubscription request for callback" +
                        properties.getProperty(APIConstants.Webhooks.CALLBACK) + " for the API " +
                        properties.getProperty(APIConstants.Webhooks.API_UUID), e);
            }
        } catch (SQLException | CryptoException e) {
            throw new APIManagementException("Error while storing webhooks unsubscription request for callback" +
                    properties.getProperty(APIConstants.Webhooks.CALLBACK) + " for the API " +
                    properties.getProperty(APIConstants.Webhooks.API_UUID), e);
        }
    }

    /*
     * This method can be used to add webhooks unsubscription request to the database
     *
     * @param properties
     * */
    public List<Subscription> getSubscriptionsList(String tenantDomain) throws APIManagementException {

        List<Subscription> subscriptionsList = new ArrayList<>();

        try (Connection conn = APIMgtDBUtil.getConnection()) {
            try (PreparedStatement preparedStatement = conn
                    .prepareStatement(SQLConstants.WebhooksSqlConstants.GET_ALL_VALID_SUBSCRIPTIONS)) {
                long currentTime = Instant.now().toEpochMilli();
                preparedStatement.setLong(1, currentTime);
                preparedStatement.setString(2, tenantDomain);
                try (ResultSet rs = preparedStatement.executeQuery()) {
                    while (rs.next()) {
                        Subscription subscription = new Subscription();
                        subscription.setApiUuid(rs.getString(APIConstants.Webhooks.API_UUID_COLUMN));
                        subscription.setAppID(rs.getString(APIConstants.Webhooks.APPLICATION_ID_COLUMN));
                        subscription.setCallback(rs.getString(APIConstants.Webhooks.CALLBACK_COLUMN));
                        subscription.setTopic(rs.getString(APIConstants.Webhooks.TOPIC_COLUMN));
                        String secret = rs.getString(APIConstants.Webhooks.SECRET_COLUMN);
                        String decryptedSecret = null;
                        if (!StringUtils.isEmpty(secret)) {
                            decryptedSecret = decryptSecret(rs.getString(APIConstants.Webhooks.SECRET_COLUMN));
                        }
                        subscription.setSecret(decryptedSecret);
                        subscription.setExpiryTime(rs.getLong(APIConstants.Webhooks.EXPIRY_AT_COLUMN));
                        subscription.setApiContext(rs.getString(APIConstants.Webhooks.API_CONTEXT_COLUMN));
                        subscription.setApiVersion(rs.getString(APIConstants.Webhooks.API_VERSION_COLUMN));
                        subscription.setTenantId(rs.getInt(APIConstants.Webhooks.TENANT_ID_COLUMN));
                        subscription.setTier(rs.getString(APIConstants.Webhooks.SUB_TIER_COLUMN));
                        subscription.setApiTier(rs.getString(APIConstants.Webhooks.API_TIER_COLUMN));
                        subscription.setApplicationTier(rs.getString(APIConstants.Webhooks.APPLICATION_TIER_COLUMN));
                        subscription.setSubscriberName(rs.getString(APIConstants.Webhooks.SUBSCRIBER_COLUMN));
                        subscriptionsList.add(subscription);
                    }
                }
            } catch (SQLException | CryptoException e) {
                throw new APIManagementException("Error while retrieving webhooks subscription request", e);
            }
        } catch (SQLException e) {
            throw new APIManagementException("Error while retrieving webhooks subscription request", e);
        }
        return subscriptionsList;
    }

    /*
     * This method can be used to update webhooks callback url delivery data to the database
     *
     * @param properties Subscription request properties
     * */
    public void updateDeliveryStatus(String apiUUID, String appID, String tenantDomain, String callback, String topic,
                                     int state) throws APIManagementException {

        try (Connection conn = APIMgtDBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement prepareStmt = conn
                    .prepareStatement(SQLConstants.WebhooksSqlConstants.UPDATE_DELIVERY_STATE)) {
                Date currentTime = new Date();
                Timestamp updatedTimestamp = new Timestamp(currentTime.getTime());
                prepareStmt.setTimestamp(1, updatedTimestamp);
                prepareStmt.setInt(2, state);
                prepareStmt.setString(3, apiUUID);
                prepareStmt.setString(4, appID);
                prepareStmt.setString(5, tenantDomain);
                prepareStmt.setString(6, callback);
                prepareStmt.setString(7, topic);
                prepareStmt.executeUpdate();
                conn.commit();
            } catch (SQLException e) {
                handleConnectionRollBack(conn);
                throw new APIManagementException("Error while storing webhooks delivery status data for callback" +
                        callback + " for the API " + apiUUID, e);
            }
        } catch (SQLException e) {
            throw new APIManagementException("Error while storing webhooks delivery status data for callback " +
                    callback + " for the API " + apiUUID, e);
        }
    }

    private void deleteSubscription(Connection conn, Properties properties) throws APIManagementException {
        try (PreparedStatement preparedStatement = conn
                .prepareStatement(SQLConstants.WebhooksSqlConstants.DELETE_IF_EXISTS_SUBSCRIBER)) {
            preparedStatement.setString(1, properties.getProperty(APIConstants.Webhooks.API_UUID));
            preparedStatement.setString(2, properties.getProperty(APIConstants.Webhooks.APP_ID));
            preparedStatement.setString(3, properties.getProperty(APIConstants.Webhooks.TENANT_DOMAIN));
            preparedStatement.setString(4, properties.getProperty(APIConstants.Webhooks.CALLBACK));
            preparedStatement.setString(5, properties.getProperty(APIConstants.Webhooks.TOPIC));
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new APIManagementException("Error while deleting existing subscriptions request for callback" +
                    properties.getProperty(APIConstants.Webhooks.CALLBACK) + " for the API " +
                    properties.getProperty(APIConstants.Webhooks.API_UUID), e);
        }
    }

    private void addUnsubscription(Connection conn, Properties properties) throws SQLException, CryptoException {
        try (PreparedStatement preparedStatement = conn
                .prepareStatement(SQLConstants.WebhooksSqlConstants.ADD_UNSUBSCRIPTION)) {
            preparedStatement.setString(1, properties.getProperty(APIConstants.Webhooks.API_UUID));
            preparedStatement.setString(2, properties.getProperty(APIConstants.Webhooks.APP_ID));
            preparedStatement.setString(3, properties.getProperty(APIConstants.Webhooks.TENANT_DOMAIN));
            preparedStatement.setString(4, properties.getProperty(APIConstants.Webhooks.CALLBACK));
            preparedStatement.setString(5, properties.getProperty(APIConstants.Webhooks.TOPIC));
            String secret = properties.getProperty(APIConstants.Webhooks.SECRET);
            String encryptedSecret = null;
            if (!StringUtils.isEmpty(secret)) {
                encryptedSecret = encryptSecret(properties.getProperty(APIConstants.Webhooks.SECRET));
            }
            preparedStatement.setString(6, encryptedSecret);
            preparedStatement.setString(7, properties.getProperty(APIConstants.Webhooks.LEASE_SECONDS));
            Timestamp updatedTime = (Timestamp)properties.get(APIConstants.Webhooks.UPDATED_AT);
            preparedStatement.setTimestamp(8, updatedTime);
            preparedStatement.executeUpdate();
        }
    }

    /**
     * This method handles the connection roll back.
     *
     * @param connection Relevant database connection that need to be rolled back.
     */
    private void handleConnectionRollBack(Connection connection) {

        try {
            if (connection != null) {
                connection.rollback();
            } else {
                log.warn("Could not perform rollback since the connection is null.");
            }
        } catch (SQLException e1) {
            log.error("Error while rolling back the transaction.", e1);
        }
    }

    private String encryptSecret(String secret) throws CryptoException {
        CryptoUtil cryptoUtil = CryptoUtil.getDefaultCryptoUtil();
        return cryptoUtil.encryptAndBase64Encode(secret.getBytes());
    }

    private String decryptSecret(String cipherText) throws CryptoException {
        CryptoUtil cryptoUtil = CryptoUtil.getDefaultCryptoUtil();
        return new String(cryptoUtil.base64DecodeAndDecrypt(cipherText));
    }
}

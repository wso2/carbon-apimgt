package org.wso2.carbon.apimgt.gatewaybridge.dao;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.gatewaybridge.dto.WebhookSubscriptionDTO;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.constants.SQLConstants;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represent the WebhooksSubscriptionDAO.
 */

public class WebhooksDAO {

    private static final Log log = LogFactory.getLog(WebhooksDAO.class);



    /**
     * Manage adding Webhook Subscription to database.
     *
     * @param webhookSubscription the DTO of webhook subscription data
     * @return a status of the operation
     */
    public boolean addSubscription(WebhookSubscriptionDTO webhookSubscription) throws APIManagementException {
        try (Connection conn = APIMgtDBUtil.getConnection()) {
            try {
               if (webhookSubscription != null) {
                   conn.setAutoCommit(false);
                   int id = findSubscription(conn, webhookSubscription);
                   if (id == 0) {
                       addSubscription(conn, webhookSubscription);
                   } else {
                       updateSubscription(conn, webhookSubscription, id);
                   }
               } else {
                   log.debug("WebhookDTO is empty!");
               }

            } catch (SQLException e) {
                handleConnectionRollBack(conn);
                throw new APIManagementException("Error while storing webhooks unsubscription request for callback" +
                        webhookSubscription.getCallback(), e);
            }
        } catch (SQLException e) {
            throw new APIManagementException("Error while storing subscription with callback " +
                    webhookSubscription.getCallback(), e);
        }
        return true;
    }

    /**
     * Check whether the subscription is available in the database.
     *
     * @param conn          the connection of database
     * @param webhookSubscription  the DTO object with values.
     * @return an integer with no of rows available
     */
    private int findSubscription(Connection conn, WebhookSubscriptionDTO webhookSubscription)
            throws APIManagementException {
        int id = 0;

        try (PreparedStatement preparedStatement = conn
                .prepareStatement(SQLConstants.ExternalGatewayWebhooksSqlConstants.FIND_SUBSCRIPTION)) {
            preparedStatement.setString(1, webhookSubscription.getSubscriberName());
            preparedStatement.setString(2, webhookSubscription.getCallback());
            preparedStatement.setString(3, webhookSubscription.getTopic());

            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    id = rs.getInt(APIConstants.Webhooks.WH_SUBSCRIPTION_ID_COLUMN);
                }
            }

        } catch (SQLException e) {
            throw new APIManagementException("Error while select existing subscriptions request for callback" +
                    webhookSubscription.getCallback() + " for the Subscriber " +
                    webhookSubscription.getSubscriberName() , e);
        }
        return id;
    }

    /**
     * Insert external gateway subscription to database.
     *
     * @param conn the connection for the database
     * @param webhookSubscription  the DTO object with values.
     */
    private void addSubscription(Connection conn, WebhookSubscriptionDTO webhookSubscription)
            throws APIManagementException {
        try (PreparedStatement prepareStmt = conn
                .prepareStatement(SQLConstants.ExternalGatewayWebhooksSqlConstants.ADD_SUBSCRIPTION)) {
            prepareStmt.setString(1, webhookSubscription.getSubscriberName());
            prepareStmt.setString(2, webhookSubscription.getCallback());
            prepareStmt.setString(3, webhookSubscription.getTopic());
            prepareStmt.setLong(4, webhookSubscription.getExpiryTime());
            prepareStmt.executeUpdate();
        } catch (SQLException e) {
            throw new APIManagementException("Error while adding subscriptions request for callback" +
                    webhookSubscription.getCallback() + " for the Subscriber " +
                    webhookSubscription.getSubscriberName(), e);
        }
    }

    /**
     * Update the available gateway subscription.
     *
     * @param conn   the connection for the database
     * @param webhookSubscription the DTO object with values.
     * @param id the id of the specific subscriber.
     */
    private void updateSubscription(Connection conn, WebhookSubscriptionDTO webhookSubscription, int id)
            throws APIManagementException {

        try (PreparedStatement prepareStmt = conn
                .prepareStatement(SQLConstants.ExternalGatewayWebhooksSqlConstants.UPDATE_EXISTING_SUBSCRIPTION)) {
            prepareStmt.setString(1, webhookSubscription.getCallback());
            prepareStmt.setString(2, webhookSubscription.getTopic());
            prepareStmt.setLong(3, webhookSubscription.getExpiryTime());
            prepareStmt.setInt(4, id);
            prepareStmt.executeUpdate();
        } catch (SQLException e) {
            throw new APIManagementException("Error while deleting existing subscriptions request for callback" +
                    webhookSubscription.getCallback() + " for the Subscriber " +
                    webhookSubscription.getSubscriberName(), e);
        }
    }
    /**
     * Handles the connection roll back.
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

    /**
     * Retrieving the List of subscribers to a topic.
     *
     * @param topic the topic subscribed
     * @return a list of subscriber DTOs.
     */
    public List<WebhookSubscriptionDTO> getSubscriptionsList(String topic) throws APIManagementException {
        List<WebhookSubscriptionDTO> subscriptionsList = new ArrayList<>();
        WebhookSubscriptionDTO webhookSubscriptionDTO = new WebhookSubscriptionDTO();
        try (Connection conn = APIMgtDBUtil.getConnection()) {
            try (PreparedStatement preparedStatement = conn
                    .prepareStatement(SQLConstants.ExternalGatewayWebhooksSqlConstants.GET_SUBSCRIPTIONS_FOR_TOPIC)) {
                preparedStatement.setString(1, topic);
                try (ResultSet rs = preparedStatement.executeQuery()) {
                    if (rs.next()) {
                        webhookSubscriptionDTO.setSubscriberName(rs.getString("WH_SUBSCRIBER_NAME"));
                        webhookSubscriptionDTO.setCallback(rs.getString("WH_CALLBACK_URL"));
                        subscriptionsList.add(webhookSubscriptionDTO);
                    }
                } catch (SQLException e) {
                    throw new APIManagementException("Error while processing webhooks subscription list", e);
                }
            } catch (SQLException e) {
                throw new APIManagementException("Error while retrieving webhooks subscription list", e);
            }
        } catch (SQLException e) {
            throw new APIManagementException("Error while retrieving webhooks subscription list request", e);
        }
        return subscriptionsList;
    }

    }

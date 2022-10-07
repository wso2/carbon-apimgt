package org.wso2.carbon.apimgt.impl.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ErrorHandler;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApplicationDAO;
import org.wso2.carbon.apimgt.impl.dao.constants.SQLConstants;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.sql.*;
import java.util.ArrayList;

public class ApplicationDAOImpl implements ApplicationDAO {
    private static final Log log = LogFactory.getLog(ApplicationDAOImpl.class);
    private static ApplicationDAOImpl INSTANCE = new ApplicationDAOImpl();

    private boolean multiGroupAppSharingEnabled = false;

    private ApplicationDAOImpl() {

    }

    public static ApplicationDAOImpl getInstance() {
        return INSTANCE;
    }

    private void handleExceptionWithCode(String msg, Throwable t, ErrorHandler code) throws APIManagementException {
        log.error(msg, t);
        throw new APIManagementException(msg, code);
    }

    @Override
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
            handleExceptionWithCode("Failed to retrieve subscription from subscription id", e,
                    ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_MESSAGE,
                            "Failed to retrieve subscription from subscription id"));
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, resultSet);
        }
        return null;
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
}

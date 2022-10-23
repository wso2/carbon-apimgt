package org.wso2.apk.apimgt.impl.dao.impl;

import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.apk.apimgt.api.APIManagementException;
import org.wso2.apk.apimgt.api.ErrorHandler;
import org.wso2.apk.apimgt.api.ExceptionCodes;
import org.wso2.apk.apimgt.api.model.*;
import org.wso2.apk.apimgt.api.model.webhooks.Subscription;
import org.wso2.apk.apimgt.impl.APIConstants;
import org.wso2.apk.apimgt.impl.dao.ApplicationDAO;
import org.wso2.apk.apimgt.impl.dao.constants.SQLConstants;
import org.wso2.apk.apimgt.impl.factory.SQLConstantManagerFactory;
import org.wso2.apk.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.apk.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

public class ApplicationDAOImpl implements ApplicationDAO {
    private static final Log log = LogFactory.getLog(ApplicationDAOImpl.class);
    private static ApplicationDAOImpl INSTANCE = new ApplicationDAOImpl();

    private boolean multiGroupAppSharingEnabled = false;
    private boolean forceCaseInsensitiveComparisons = false;

    private ApplicationDAOImpl() {

    }

    public static ApplicationDAOImpl getInstance() {
        return INSTANCE;
    }

    private void handleExceptionWithCode(String msg, Throwable t, ErrorHandler code) throws APIManagementException {
        log.error(msg, t);
        throw new APIManagementException(msg, code);
    }

    private void handleException(String msg, Throwable t) throws APIManagementException {

        log.error(msg, t);
        throw new APIManagementException(msg, t);
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

    @Override
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
            handleExceptionWithCode("Error when reading the application information from the persistence store.", e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
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

    @Override
    public Application[] getApplicationsWithPagination(String user, String owner, String organization, int limit,
                                                       int offset, String sortBy, String sortOrder, String appName)
            throws APIManagementException {

        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        String sqlQuery = null;
        List<Application> applicationList = new ArrayList<>();
        sqlQuery = SQLConstantManagerFactory.getSQlString("GET_APPLICATIONS_BY_ORGANIZATION");
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
            prepStmt.setString(1, organization);
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
                subscriber.setOrganization(rs.getString("ORGANIZATION"));
                subscriber.setId(rs.getInt("SUBSCRIBER_ID"));
                application.setStatus(rs.getString("APPLICATION_STATUS"));
                application.setOwner(subscriberName);
                applicationList.add(application);
            }
            applications = applicationList.toArray(new Application[applicationList.size()]);
        } catch (SQLException e) {
            handleExceptionWithCode("Error while obtaining details of the Application for organization : "
                            + organization, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
        }
        return applications;
    }

    @Override
    public int getApplicationsCount(String organization, String searchOwner, String searchApplication) throws
            APIManagementException {

        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet resultSet = null;
        String sqlQuery = null;
        try {
            connection = APIMgtDBUtil.getConnection();
            sqlQuery = SQLConstants.GET_APPLICATIONS_COUNT;
            prepStmt = connection.prepareStatement(sqlQuery);
            prepStmt.setString(1, organization);
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
            handleExceptionWithCode("Failed to get application count of organization: " + organization, e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, resultSet);
        }
        return 0;
    }

    @Override
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

    @Override
    public Subscriber getSubscriber(String subscriberName) throws APIManagementException {

        Connection conn = null;
        Subscriber subscriber = null;
        PreparedStatement ps = null;
        ResultSet result = null;

        String organization = APIUtil.getTenantDomain(subscriberName);

        String sqlQuery = SQLConstants.GET_TENANT_SUBSCRIBER_SQL;
        if (forceCaseInsensitiveComparisons) {
            sqlQuery = SQLConstants.GET_TENANT_SUBSCRIBER_CASE_INSENSITIVE_SQL;
        }

        try {
            conn = APIMgtDBUtil.getConnection();

            ps = conn.prepareStatement(sqlQuery);
            ps.setString(1, subscriberName);
            ps.setString(2, organization);
            result = ps.executeQuery();

            if (result.next()) {
                subscriber = new Subscriber(result.getString(APIConstants.SUBSCRIBER_FIELD_EMAIL_ADDRESS));
                subscriber.setEmail(result.getString("EMAIL_ADDRESS"));
                subscriber.setId(result.getInt("SUBSCRIBER_ID"));
                subscriber.setName(subscriberName);
                subscriber.setSubscribedDate(result.getDate(APIConstants.SUBSCRIBER_FIELD_DATE_SUBSCRIBED));
                subscriber.setOrganization(result.getString("ORGANIZATION"));
            }
        } catch (SQLException e) {
            handleException("Failed to get Subscriber for :" + subscriberName, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, result);
        }
        return subscriber;
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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
    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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
     * Identify whether the loggedin user used his Primary Login name or Secondary login name
     *
     * @param userId
     * @return
     */
    private boolean isSecondaryLogin(String userId) {

        Map<String, Map<String, String>> loginConfiguration = new HashMap<>();
        //Todo need to fix retrieving login user configuration
        //= ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration().getLoginConfiguration();
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
     * Identify whether the loggedin user used his ordinal username or email
     *
     * @param userId
     * @return
     */
    private boolean isUserLoggedInEmail(String userId) {

        return userId.contains("@");
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

        //Todo need to fix retrieving login user configuration
        Map<String, Map<String, String>> loginConfiguration = new HashMap<>();
        //ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration().getLoginConfiguration();
        String claimURI, username = null;
        if (isUserLoggedInEmail(login)) {
            Map<String, String> emailConf = loginConfiguration.get(APIConstants.EMAIL_LOGIN);
            claimURI = emailConf.get(APIConstants.CLAIM_URI);
        } else {
            Map<String, String> userIdConf = loginConfiguration.get(APIConstants.USERID_LOGIN);
            claimURI = userIdConf.get(APIConstants.CLAIM_URI);
        }

        try {
            //Todo need to fix retrieving login user configuration
            String[] user = {""};
            //RemoteUserManagerClient.getInstance().getUserList(claimURI, login);
            if (user.length > 0) {
                username = user[0];
            }
        } catch (Exception e) {

            handleException("Error while retrieving the primaryLogin name using secondary loginName : " + login, e);
        }
        return username;
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
            String userOrganization = APIUtil.getTenantDomain(userId);

            //Get subscriber Id
            Subscriber subscriber = getSubscriber(userId, userOrganization, conn);
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
                addApplicationAttributes(conn, application.getApplicationAttributes(), applicationId, userOrganization);
            }
        } catch (SQLException e) {
            handleException("Failed to add Application", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, null, rs);
        }
        return applicationId;
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
     * returns a subscriber record for given username,tenant Id
     *
     * @param username   UserName
     * @param organization   Tenant Id
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
                    //Todo: Need to implement removing from KeyManager in a different way, not in DAO level
//                    KeyManager keyManager =
//                            KeyManagerHolder.getKeyManagerInstance(keyManagerOrganization, keyManagerName);
//                    if (keyManager != null) {
//                        try {
//                            keyManager.deleteMappedApplication(consumerKey);
//                        } catch (APIManagementException e) {
//                            log.error("Error while Deleting Client Application", e);
//                        }
//                    }
                    // OAuth app is deleted if only it has been created from API Store. For mapped clients we don't
                    // call delete.
                    if (!APIConstants.OAuthAppMode.MAPPED.name().equals(mode)) {
                        //delete on oAuthorization server.
                        if (log.isDebugEnabled()) {
                            log.debug("Deleting Oauth application with consumer key " + consumerKey + " from the " +
                                    "Oauth server");
                        }
                        //Todo: Need to implement removing from KeyManager in a different way, not in DAO level
//                        if (keyManager != null) {
//                            try {
//                                keyManager.deleteApplication(consumerKey);
//                            } catch (APIManagementException e) {
//                                log.error("Error while Deleting Client Application", e);
//                            }
//
//                        }
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

    @Override
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
            handleExceptionWithCode("Error while obtaining details of the Application : " + uuid, e,
                    ExceptionCodes.from(ExceptionCodes.ERROR_RETRIEVE_APPLICATION_DETAILS, uuid));
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
        }
        return application;
    }

    @Override
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
                    //Todo: Need to implement this outside DAO
//                    KeyManager keyManager = KeyManagerHolder.getKeyManagerInstance(tenntDomain, keyManagerName);
//                    if (keyManager != null) {
//                        OAuthApplicationInfo oAuthApplication = keyManager.retrieveApplication(consumerKey);
//                        keyTypeWiseOAuthApps.put(keyManagerName, oAuthApplication);
//                    }
                }
            }

        } catch (SQLException e) {
            handleException("Failed to get  client of application. SQL error", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, connection, rs);
        }

        return keyTypeWiseOAuthApps;
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

            preparedStatement = conn.prepareStatement(SQLConstants.REMOVE_APPLICATION_ATTRIBUTES_SQL);
            preparedStatement.setInt(1, application.getId());
            preparedStatement.execute();

            if (log.isDebugEnabled()) {
                log.debug("Old attributes of application - " + application.getName() + " are removed");
            }

            if (application.getApplicationAttributes() != null && !application.getApplicationAttributes().isEmpty()) {
                addApplicationAttributes(conn, application.getApplicationAttributes(), application.getId(), domain);
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

    @Override
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

    @Override
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

    @Override
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
            handleExceptionWithCode("Error occurred while getting subscription entries for " +
                    "Application : " + applicationId, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
        Map<String,Set<Integer>> map = new HashMap<>();
        map.put(APIConstants.SubscriptionStatus.ON_HOLD, pendingCreateSubscriptionIds);
        map.put(APIConstants.SubscriptionStatus.DELETE_PENDING, pendingDeleteSubscriptionIds);
        map.put(APIConstants.SubscriptionStatus.TIER_UPDATE_PENDING, pendingUpdateSubscriptionIds);
        return map;
    }

    @Override
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
            handleExceptionWithCode("Error while getting Application Registration State.", e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, resultSet);
        }
        return keyManagerWiseApprovalState;
    }

    @Override
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

    @Override
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

    @Override
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

    @Override
    public String getGroupId(int applicationId) throws APIManagementException {

        try (Connection conn = APIMgtDBUtil.getConnection()) {
            return getGroupId(conn, applicationId);
        } catch (SQLException e) {
            handleException("Failed to Retrieve GroupId for application " + applicationId, e);
        }
        return null;
    }

    @Override
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

    @Override
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

    @Override
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
            handleExceptionWithCode("Error when updating application owner for user " + userName, e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, null);
        }
        return isAppUpdated;
    }

    @Override
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

    @Override
    public void addApplicationAttributes(Map<String, String> applicationAttributes, int applicationId,
                                         String organization) throws APIManagementException {

        Connection connection = null;
        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            addApplicationAttributes(connection, applicationAttributes, applicationId, organization);
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

    @Override
    public APIKey getAPIKeyFromApplicationIdAndKeyMappingId(int applicationId, String keyMappingId)
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

    @Override
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

    @Override
    public Set<Subscription> getTopicSubscriptions(String applicationId) throws APIManagementException {

        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        String getTopicSubscriptionsQuery = SQLConstants.GET_WH_TOPIC_SUBSCRIPTIONS;
        Set<Subscription> subscriptionSet = new HashSet();
        try {
            conn = APIMgtDBUtil.getConnection();
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
            handleExceptionWithCode("Failed to retrieve topic subscriptions for application  " + applicationId, e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, resultSet);
        }

        return null;
    }

    @Override
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
            String errorMessage =  "Failed to get subscribed APIs of application: " +  application.getName();
            handleExceptionWithCode(errorMessage, e,
                    ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_MESSAGE, errorMessage));
        }

        return subscribedAPIs;

    }

    private void initSubscribedAPI(SubscribedAPI subscribedAPI, ResultSet resultSet)
            throws SQLException {

        subscribedAPI.setUUID(resultSet.getString("SUB_UUID"));
        subscribedAPI.setSubStatus(resultSet.getString("SUB_STATUS"));
        subscribedAPI.setSubCreatedStatus(resultSet.getString("SUBS_CREATE_STATE"));
        subscribedAPI.setTier(new Tier(resultSet.getString(APIConstants.SUBSCRIPTION_FIELD_TIER_ID)));
        subscribedAPI.setRequestedTier(new Tier(resultSet.getString("TIER_ID_PENDING")));
    }

    @Override
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

}

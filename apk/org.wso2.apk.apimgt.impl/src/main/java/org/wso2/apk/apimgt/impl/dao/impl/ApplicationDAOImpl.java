package org.wso2.apk.apimgt.impl.dao.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.apk.apimgt.api.APIManagementException;
import org.wso2.apk.apimgt.api.ErrorHandler;
import org.wso2.apk.apimgt.api.ExceptionCodes;
import org.wso2.apk.apimgt.api.model.APIIdentifier;
import org.wso2.apk.apimgt.api.model.APIProductIdentifier;
import org.wso2.apk.apimgt.api.model.Application;
import org.wso2.apk.apimgt.api.model.ApplicationInfo;
import org.wso2.apk.apimgt.api.model.Identifier;
import org.wso2.apk.apimgt.api.model.SubscribedAPI;
import org.wso2.apk.apimgt.api.model.Subscriber;
import org.wso2.apk.apimgt.api.model.Tier;
import org.wso2.apk.apimgt.impl.APIConstants;
import org.wso2.apk.apimgt.impl.dao.ApplicationDAO;
import org.wso2.apk.apimgt.impl.dao.constants.SQLConstants;
import org.wso2.apk.apimgt.impl.factory.SQLConstantManagerFactory;
import org.wso2.apk.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.apk.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            handleExceptionWithCode("Error while obtaining details of the Application for tenant id : " + tenantId, e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
        }
        return applications;
    }

    @Override
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
            handleExceptionWithCode("Failed to get application count of tenant id : " + tenantId, e,
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

    /**
     * This method used tot get Subscriber from subscriberId.
     *
     * @param subscriberName id
     * @return Subscriber
     * @throws APIManagementException if failed to get Subscriber from subscriber id
     */
    private Subscriber getSubscriber(String subscriberName) throws APIManagementException {

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



}

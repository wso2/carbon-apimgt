package org.wso2.apk.apimgt.impl.dao.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.wso2.apk.apimgt.api.*;
import org.wso2.apk.apimgt.api.model.*;
import org.wso2.apk.apimgt.impl.dao.ConsumerDAO;
import org.wso2.apk.apimgt.impl.APIConstants;
import org.wso2.apk.apimgt.impl.dao.constants.SQLConstants;
import org.wso2.apk.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.apk.apimgt.impl.utils.APIUtil;
import org.wso2.apk.apimgt.api.model.APIIdentifier;
import org.wso2.apk.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

public class ConsumerDAOImpl implements ConsumerDAO {
    private static final Log log = LogFactory.getLog(ConsumerDAOImpl.class);
    private static ConsumerDAOImpl INSTANCE = new ConsumerDAOImpl();

    private boolean forceCaseInsensitiveComparisons = false;
    private boolean multiGroupAppSharingEnabled = false;

    private ConsumerDAOImpl() {

    }

    public static ConsumerDAOImpl getInstance() {
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

    @Override
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
    private void addOrUpdateRating(String uuid, int rating, String userId, Connection conn)
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

    private int getAPIID(String uuid, Connection connection) throws APIManagementException, SQLException {

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

    @Override
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
    private void removeAPIRating(String uuid, String userId, Connection conn)
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

    @Override
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
    private int getUserRating(String uuid, String userId, Connection conn)
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

    @Override
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

    @Override
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

    @Override
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
//            String[] user = RemoteUserManagerClient.getInstance().getUserList(claimURI, login);
            String[] user = {};
            if (user.length > 0) {
                username = user[0];
            }
        } catch (Exception e) {

            handleException("Error while retrieving the primaryLogin name using secondary loginName : " + login, e);
        }
        return username;
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
     * Identify whether the loggedin user used his ordinal username or email
     *
     * @param userId
     * @return
     */
    private boolean isUserLoggedInEmail(String userId) {

        return userId.contains("@");
    }

    @Override
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
            String errorMessage = "Failed to retrieve subscription from subscription id";
            handleExceptionWithCode(errorMessage, e,
                    ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_MESSAGE, errorMessage));
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

    @Override
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

    private ResultSet getSubscriptionResultSet(String groupingId, Subscriber subscriber,
                                               PreparedStatement statement, String organization)
            throws SQLException, APIManagementException {

        String subOrganization = APIUtil.getTenantDomain(subscriber.getName());
        int paramIndex = 0;

        if (groupingId != null && !"null".equals(groupingId) && !groupingId.isEmpty()) {
            if (multiGroupAppSharingEnabled) {
                String tenantDomain = MultitenantUtils.getTenantDomain(subscriber.getName());
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

    @Override
    public Set<String> getScopesForApplicationSubscription(Subscriber subscriber, int applicationId)
            throws APIManagementException {

        PreparedStatement getIncludedApisInProduct = null;
        PreparedStatement getSubscribedApisAndProducts = null;
        ResultSet resultSet = null;
        Set<String> scopeKeysSet = new HashSet<>();
        Set<Integer> apiIdSet = new HashSet<>();
        String organization = APIUtil.getTenantDomain(subscriber.getName());

        try (Connection conn = APIMgtDBUtil.getConnection()) {
            String sqlQueryForGetSubscribedApis = SQLConstants.GET_SUBSCRIBED_API_IDs_BY_APP_ID_SQL;
            getSubscribedApisAndProducts = conn.prepareStatement(sqlQueryForGetSubscribedApis);
            getSubscribedApisAndProducts.setString(1, organization);
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
            handleExceptionWithCode("Failed to retrieve scopes for application subscription ", e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
        } finally {
            APIMgtDBUtil.closeAllConnections(getSubscribedApisAndProducts, null, resultSet);
            APIMgtDBUtil.closeAllConnections(getIncludedApisInProduct, null, null);
        }
        return scopeKeysSet;
    }

    @Override
    public Integer getSubscriptionCount(Subscriber subscriber, String applicationName, String groupingId)
            throws APIManagementException {

        Integer subscriptionCount = 0;
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet result = null;
        String organization = APIUtil.getTenantDomain(subscriber.getName());

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
                    ps.setString(2, organization);
                    int paramIndex = groupIdArr.length + 2;
                    ps.setString(++paramIndex, tenantDomain);
                    ps.setString(++paramIndex, subscriber.getName());
                } else {
                    sqlQuery += whereClauseWithGroupId;
                    ps = connection.prepareStatement(sqlQuery);
                    ps.setString(1, applicationName);
                    ps.setString(2, organization);
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
                ps.setString(2, organization);
                ps.setString(3, subscriber.getName());
            }
            result = ps.executeQuery();

            while (result.next()) {
                subscriptionCount = result.getInt("SUB_COUNT");
            }
        } catch (SQLException e) {
            handleExceptionWithCode("Failed to get SubscribedAPI of :" + subscriber.getName(), e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, connection, result);
        }
        return subscriptionCount;
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
    private PreparedStatement fillQueryParams(Connection conn, String query, String params[], int startingParamIndex)
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
            String organization = APIUtil.getTenantDomain(loginUserName);
            ps.setString(5, organization);

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

    @Override
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
            handleExceptionWithCode("Failed to add subscriber data", e,
                    ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_MESSAGE,
                            "Failed to add subscriber data"));
        }
        return subscriptionId;
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

    private int getAPIID(String uuid) throws APIManagementException {
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

    @Override
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
            handleExceptionWithCode("Failed to remove subscriber data of " + subscription_id, e,
                    ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_MESSAGE,
                            "Failed to remove subscription data"));
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, null);
        }
    }

    @Override
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
                    log.error("Failed to rollback the update subscription", e1);
                }
            }
            handleExceptionWithCode("Failed to update subscription data", e,
                    ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_MESSAGE,
                            "Failed to update subscription data"));
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, resultSet);
            APIMgtDBUtil.closeAllConnections(preparedStForUpdate, null, null);
        }
        return subscriptionId;
    }

    @Override
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

    @Override
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

    @Override
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

    @Override
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


}

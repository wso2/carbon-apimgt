package org.wso2.apk.apimgt.impl.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.apk.apimgt.api.APIManagementException;
import org.wso2.apk.apimgt.api.ErrorHandler;
import org.wso2.apk.apimgt.api.ExceptionCodes;
import org.wso2.apk.apimgt.api.model.APICategory;
import org.wso2.apk.apimgt.api.model.MonetizationUsagePublishInfo;
import org.wso2.apk.apimgt.api.model.botDataAPI.BotDetectionData;
import org.wso2.apk.apimgt.impl.alertmgt.AlertMgtConstants;
import org.wso2.apk.apimgt.impl.dao.AdminDAO;
import org.wso2.apk.apimgt.impl.dao.constants.SQLConstants;
import org.wso2.apk.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.apk.apimgt.impl.utils.APIUtil;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AdminDAOImpl implements AdminDAO {
    private static final Log log = LogFactory.getLog(AdminDAOImpl.class);
    private static AdminDAOImpl INSTANCE = new AdminDAOImpl();

    private AdminDAOImpl() {

    }

    public static AdminDAOImpl getInstance() {
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
    public void addTenantTheme(int tenantId, InputStream themeContent) throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection
                     .prepareStatement(SQLConstants.TenantThemeConstants.ADD_TENANT_THEME)) {
            statement.setInt(1, tenantId);
            statement.setBinaryStream(2, themeContent);
            statement.executeUpdate();
        } catch (SQLException e) {
            handleExceptionWithCode("Failed to add tenant theme of tenant "
                    + APIUtil.getTenantDomainFromTenantId(tenantId), e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
    }

    @Override
    public void updateTenantTheme(int tenantId, InputStream themeContent) throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(SQLConstants.TenantThemeConstants.UPDATE_TENANT_THEME)) {
            statement.setBinaryStream(1, themeContent);
            statement.setInt(2, tenantId);
            statement.executeUpdate();
        } catch (SQLException e) {
            handleExceptionWithCode("Failed to update tenant theme of tenant "
                    + APIUtil.getTenantDomainFromTenantId(tenantId), e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
    }

    @Override
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
            handleExceptionWithCode("Failed to fetch tenant theme of tenant "
                    + APIUtil.getTenantDomainFromTenantId(tenantId), e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        return tenantThemeContent;
    }

    @Override
    public boolean isTenantThemeExist(int tenantId) throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection
                     .prepareStatement(SQLConstants.TenantThemeConstants.GET_TENANT_THEME)) {
            statement.setInt(1, tenantId);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            handleExceptionWithCode("Failed to check whether tenant theme exist for tenant "
                    + APIUtil.getTenantDomainFromTenantId(tenantId), e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        return false;
    }

    @Override
    public void deleteTenantTheme(int tenantId) throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection
                     .prepareStatement(SQLConstants.TenantThemeConstants.DELETE_TENANT_THEME)) {
            statement.setInt(1, tenantId);
            statement.executeUpdate();
        } catch (SQLException e) {
            handleExceptionWithCode("Failed to delete tenant theme of tenant "
                    + APIUtil.getTenantDomainFromTenantId(tenantId), e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
    }


}

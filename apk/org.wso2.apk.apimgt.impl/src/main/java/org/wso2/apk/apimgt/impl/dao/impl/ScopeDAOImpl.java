package org.wso2.apk.apimgt.impl.dao.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.apk.apimgt.api.APIManagementException;
import org.wso2.apk.apimgt.api.APIMgtResourceNotFoundException;
import org.wso2.apk.apimgt.api.ErrorHandler;
import org.wso2.apk.apimgt.api.ExceptionCodes;
import org.wso2.apk.apimgt.api.model.API;
import org.wso2.apk.apimgt.api.model.APIIdentifier;
import org.wso2.apk.apimgt.api.model.Scope;
import org.wso2.apk.apimgt.api.model.SharedScopeUsage;
import org.wso2.apk.apimgt.api.model.URITemplate;
import org.wso2.apk.apimgt.impl.APIConstants;
import org.wso2.apk.apimgt.impl.dao.ScopeDAO;
import org.wso2.apk.apimgt.impl.dao.constants.SQLConstants;
import org.wso2.apk.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.apk.apimgt.impl.utils.APIUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ScopeDAOImpl implements ScopeDAO {

    private static final Log log = LogFactory.getLog(ScopeDAOImpl.class);
    private static ScopeDAOImpl INSTANCE = new ScopeDAOImpl();

    private ScopeDAOImpl() {

    }

    public static ScopeDAOImpl getInstance() {
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
            handleExceptionWithCode("Failed to check is exists Shared Scope : " + scopeName + "-" + tenantId, e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        return isExist;
    }

    @Override
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

    @Override
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

    @Override
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

    @Override
    public List<Scope> getScopes(int tenantId) throws APIManagementException {

        List<Scope> scopeList = new ArrayList<>();
        try (Connection connection = APIMgtDBUtil.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(SQLConstants.GET_SCOPES_SQL)) {
                preparedStatement.setInt(1, tenantId);
                preparedStatement.setString(2, APIConstants.DEFAULT_SCOPE_TYPE);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        String name = resultSet.getString("SCOPE_KEY");
                        String displayName = resultSet.getString("DISPLAY_NAME");
                        String description = resultSet.getString("DESCRIPTION");
                        Scope scope = new Scope();
                        scope.setName(displayName);
                        scope.setDescription(description);
                        scope.setKey(name);
                        scope.setRoles(String.join(",", getScopeBindings(connection, name, tenantId)));
                        scopeList.add(scope);
                    }
                }
            } catch (SQLException e) {
                throw new APIManagementException("Error while retrieving scopes from db", e,
                        ExceptionCodes.INTERNAL_ERROR);
            }
        } catch (SQLException e) {
            throw new APIManagementException("Error while retrieving database connection", e,
                    ExceptionCodes.INTERNAL_ERROR);
        }

        return scopeList;
    }

    private List<String> getScopeBindings(Connection connection, String scopeKey, int tenantId)
            throws SQLException {

        List<String> bindingList = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(SQLConstants.RETRIEVE_SCOPE_MAPPING)) {
            preparedStatement.setString(1, scopeKey);
            preparedStatement.setInt(2, tenantId);
            preparedStatement.setString(3, APIConstants.DEFAULT_BINDING_TYPE);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String binding = resultSet.getString("SCOPE_BINDING");
                    if (StringUtils.isNotEmpty(binding)) {
                        bindingList.add(binding);
                    }
                }
            }
        }
        return bindingList;
    }

    @Override
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
            handleExceptionWithCode("Failed to get all Shared Scope Keys for tenant: " + tenantDomain, e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        return scopeKeys;
    }

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
            handleExceptionWithCode("Failed to get Shared Scope : " + uuid, e,
                    ExceptionCodes.FAILED_RETRIEVE_SHARED_SCOPE);
        }
        return scopeKey;
    }

    @Override
    public Scope getScope(String name, int tenantId) throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(SQLConstants.GET_SCOPE_SQL)) {
                preparedStatement.setString(1, name);
                preparedStatement.setInt(2, tenantId);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        String displayName = resultSet.getString("DISPLAY_NAME");
                        String description = resultSet.getString("DESCRIPTION");
                        Scope scope = new Scope();
                        scope.setName(displayName);
                        scope.setDescription(description);
                        scope.setKey(name);
                        scope.setRoles(String.join(",", getScopeBindings(connection, name, tenantId)));
                        return scope;
                    }
                }
            } catch (SQLException e) {
                String msg = String.format("Error while retrieving scope %s from db", name);
                log.error(msg, e);
                throw new APIManagementException(msg, e, ExceptionCodes.INTERNAL_ERROR);
            }
        } catch (SQLException e) {
            throw new APIManagementException("Error while retrieving database connection", e,
                    ExceptionCodes.INTERNAL_ERROR);
        }
        return null;
    }

    @Override
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

    @Override
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



}

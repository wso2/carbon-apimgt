package org.wso2.carbon.apimgt.impl.migrator;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ScopesDAO;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.identity.core.util.IdentityIOStreamUtils;
import org.wso2.carbon.identity.oauth.common.OAuthConstants;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 *  This class used to migrate scopes from IDN_OAUTH2_SCOPE table to AM_SCOPE table at server startup
 */
public class ScopesMigrator {

    private static final String DATABASE_LOCK_INSERT_QUERY = "INSERT INTO AM_USER (USER_ID,USER_NAME) VALUES(?,?)";
    private static final String DATABASE_LOCK_DELETE_QUERY = "DELETE FROM AM_USER WHERE USER_ID = ?";
    private static final Log log = LogFactory.getLog(ScopesMigrator.class);
    private static final String SELECT_SCOPES_QUERY_LEFT = " SELECT SCOPE_ID AS SCOPE_ID,NAME AS SCOPE_KEY," +
            "DISPLAY_NAME AS DISPLAY_NAME,DESCRIPTION AS DESCRIPTION,TENANT_ID AS TENANT_ID,SCOPE_TYPE AS SCOPE_TYPE " +
            "FROM IDN_OAUTH2_SCOPE WHERE NAME NOT IN (";
    private static final String SELECT_SCOPES_QUERY_RIGHT = ") AND SCOPE_TYPE = 'OAUTH2'";
    public static final String SELECT_SCOPE_MAPPING =
            "SELECT SCOPE_BINDING FROM IDN_OAUTH2_SCOPE_BINDING WHERE SCOPE_ID = ? AND BINDING_TYPE = ?";
    private static final String IDENTITY_PATH = "identity";
    private static final String NAME = "name";

    public void migrateScopes() throws APIManagementException {

        Map<Integer, Set<Scope>> scopesMap = new HashMap<>();
        boolean scopesMigrated = isScopesMigrated();
        if (!scopesMigrated) {
            boolean acquired = acquireDatabaseLock();
            if (acquired) {
                String query = SELECT_SCOPES_QUERY_LEFT;
                List<String> identityScopes = retrieveIdentityScopes();
                query = query.concat(StringUtils.repeat("?", ",", identityScopes.size()))
                        .concat(SELECT_SCOPES_QUERY_RIGHT);
                try (Connection connection = APIMgtDBUtil.getConnection()) {
                    try (PreparedStatement preparedStatement = connection
                            .prepareStatement(query)) {
                        for (int i = 0; i < identityScopes.size(); i++) {
                            preparedStatement.setString(i + 1, identityScopes.get(i));
                        }
                        try (ResultSet resultSet = preparedStatement.executeQuery()) {
                            while (resultSet.next()) {
                                int scopeId = resultSet.getInt("SCOPE_ID");
                                String scopeKey = resultSet.getString("SCOPE_KEY");
                                String displayName = resultSet.getString("DISPLAY_NAME");
                                String description = resultSet.getString("DESCRIPTION");
                                int tenantId = resultSet.getInt("TENANT_ID");
                                Scope scope = new Scope();
                                scope.setKey(scopeKey);
                                scope.setName(displayName);
                                scope.setDescription(description);
                                List<String> bindings = retrieveScopeBindings(connection, scopeId);
                                scope.setRoles(String.join(",", bindings));
                                Set<Scope> scopeList = scopesMap.get(tenantId);
                                if (scopeList != null) {
                                    scopeList.add(scope);
                                } else {
                                    scopeList = new HashSet<>();
                                    scopeList.add(scope);
                                    scopesMap.put(tenantId, scopeList);
                                }
                            }
                        }
                    }
                    for (Map.Entry<Integer, Set<Scope>> scopesMapEntry : scopesMap.entrySet()) {
                        ScopesDAO scopesDAO = ScopesDAO.getInstance();
                        scopesDAO.addScopes(scopesMapEntry.getValue(), scopesMapEntry.getKey());

                    }
                } catch (SQLException e) {
                    throw new APIManagementException("Error while retrieving database connection", e);
                } finally {
                    releaseDatabaseLock();
                }
            }
        }
    }

    private boolean acquireDatabaseLock() throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement preparedStatement = connection.prepareStatement(DATABASE_LOCK_INSERT_QUERY)) {
                String driverName = connection.getMetaData().getDriverName();
                preparedStatement.setString(1, "am_lock_table");
                preparedStatement.setString(2, "am_lock_table");
                preparedStatement.executeUpdate();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                if (e instanceof SQLIntegrityConstraintViolationException || "23505".equals(e.getSQLState())) {
                    log.debug("Lock already acquired to update");
                    return false;
                } else {
                    throw new APIManagementException("Error while inserting Lock Entry to table", e);
                }
            }

        } catch (SQLException e) {
            throw new APIManagementException("Error while retrieving database connection", e);
        }
        return true;
    }

    private boolean releaseDatabaseLock() throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement preparedStatement = connection.prepareStatement(DATABASE_LOCK_DELETE_QUERY)) {
                preparedStatement.setString(1, "am_lock_table");
                preparedStatement.executeUpdate();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
            }
        } catch (SQLException e) {
            throw new APIManagementException("Error while retrieving database connection", e);
        }
        return true;
    }

    private boolean isScopesMigrated() throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            try (PreparedStatement preparedStatement = connection
                    .prepareStatement("SELECT 1 FROM AM_SCOPE WHERE SCOPE_TYPE = ?")) {
                preparedStatement.setString(1, APIConstants.DEFAULT_SCOPE_TYPE);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    return true;
                }
            }
        } catch (SQLException e) {
            throw new APIManagementException("Error while retrieving database connection", e);
        }
        return false;
    }

    private static List<String> retrieveIdentityScopes() {

        List<String> scopes = new ArrayList<>();
        String configDirPath = CarbonUtils.getCarbonConfigDirPath();
        String confXml = Paths.get(configDirPath, IDENTITY_PATH, OAuthConstants.OAUTH_SCOPE_BINDING_PATH)
                .toString();
        File configFile = new File(confXml);
        if (!configFile.exists()) {
            log.warn("OAuth scope binding File is not present at: " + confXml);
            return new ArrayList<>();
        }

        XMLStreamReader parser = null;
        InputStream stream = null;

        try {
            stream = new FileInputStream(configFile);
            parser = XMLInputFactory.newInstance()
                    .createXMLStreamReader(stream);
            StAXOMBuilder builder = new StAXOMBuilder(parser);
            OMElement documentElement = builder.getDocumentElement();
            Iterator iterator = documentElement.getChildElements();
            while (iterator.hasNext()) {
                OMElement omElement = (OMElement) iterator.next();
                String scopeName = omElement.getAttributeValue(new QName(
                        NAME));
                scopes.add(scopeName);
            }
        } catch (XMLStreamException e) {
            log.warn("Error while loading scope config.", e);
        } catch (FileNotFoundException e) {
            log.warn("Error while loading email config.", e);
        } finally {
            try {
                if (parser != null) {
                    parser.close();
                }
                if (stream != null) {
                    IdentityIOStreamUtils.closeInputStream(stream);
                }
            } catch (XMLStreamException e) {
                log.error("Error while closing XML stream", e);
            }
        }
        return scopes;
    }

    private List<String> retrieveScopeBindings(Connection connection, int scopeId) throws SQLException {

        List<String> bindings = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(SELECT_SCOPE_MAPPING)) {
            preparedStatement.setInt(1, scopeId);
            preparedStatement.setString(2, APIConstants.DEFAULT_BINDING_TYPE);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    bindings.add(resultSet.getString("SCOPE_BINDING"));
                }
            }
        }
        return bindings;
    }
}

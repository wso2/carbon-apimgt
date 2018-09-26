/*
 *
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.apimgt.core.dao.impl;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.dao.APISubscriptionDAO;
import org.wso2.carbon.apimgt.core.dao.ApiDAO;
import org.wso2.carbon.apimgt.core.dao.ApiType;
import org.wso2.carbon.apimgt.core.dao.SearchType;
import org.wso2.carbon.apimgt.core.dao.SecondarySearchType;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.APIStatus;
import org.wso2.carbon.apimgt.core.models.AdditionalProperties;
import org.wso2.carbon.apimgt.core.models.BusinessInformation;
import org.wso2.carbon.apimgt.core.models.Comment;
import org.wso2.carbon.apimgt.core.models.CompositeAPI;
import org.wso2.carbon.apimgt.core.models.CorsConfiguration;
import org.wso2.carbon.apimgt.core.models.DedicatedGateway;
import org.wso2.carbon.apimgt.core.models.DocumentInfo;
import org.wso2.carbon.apimgt.core.models.Endpoint;
import org.wso2.carbon.apimgt.core.models.Rating;
import org.wso2.carbon.apimgt.core.models.ResourceCategory;
import org.wso2.carbon.apimgt.core.models.Subscription;
import org.wso2.carbon.apimgt.core.models.UriTemplate;
import org.wso2.carbon.apimgt.core.models.policy.APIPolicy;
import org.wso2.carbon.apimgt.core.models.policy.Policy;
import org.wso2.carbon.apimgt.core.models.policy.SubscriptionPolicy;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants.APILCWorkflowStatus;
import org.wso2.carbon.apimgt.core.util.APIUtils;
import org.wso2.carbon.apimgt.core.util.ContainerBasedGatewayConstants;
import org.wso2.carbon.apimgt.core.util.ThrottleConstants;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.CheckForNull;
import javax.ws.rs.core.MediaType;

/**
 * Default implementation of the ApiDAO interface. Uses SQL syntax that is common to H2 and MySQL DBs.
 * Hence is considered as the default due to its re-usability.
 */
public class ApiDAOImpl implements ApiDAO {

    private final ApiDAOVendorSpecificStatements sqlStatements;

    private static final String API_SUMMARY_SELECT = "SELECT DISTINCT UUID, PROVIDER, NAME, CONTEXT, VERSION, " +
            "DESCRIPTION, CURRENT_LC_STATUS, LIFECYCLE_INSTANCE_ID, LC_WORKFLOW_STATUS, SECURITY_SCHEME FROM AM_API";

    private static final String API_SELECT = "SELECT UUID, PROVIDER, NAME, CONTEXT, VERSION, IS_DEFAULT_VERSION, " +
            "DESCRIPTION, VISIBILITY, IS_RESPONSE_CACHED, HAS_OWN_GATEWAY, CACHE_TIMEOUT, TECHNICAL_OWNER, " +
            "TECHNICAL_EMAIL, BUSINESS_OWNER, BUSINESS_EMAIL, LIFECYCLE_INSTANCE_ID, CURRENT_LC_STATUS, " +
            "CORS_ENABLED, CORS_ALLOW_ORIGINS, CORS_ALLOW_CREDENTIALS, CORS_ALLOW_HEADERS, CORS_ALLOW_METHODS, " +
            "CREATED_BY, CREATED_TIME, LAST_UPDATED_TIME, COPIED_FROM_API, UPDATED_BY, LC_WORKFLOW_STATUS, " +
            "SECURITY_SCHEME FROM AM_API";

    private static final String COMPOSITE_API_SUMMARY_SELECT = "SELECT UUID, PROVIDER, NAME, CONTEXT, VERSION, " +
            "DESCRIPTION, LC_WORKFLOW_STATUS FROM AM_API";

    private static final String API_INSERT = "INSERT INTO AM_API (PROVIDER, NAME, CONTEXT, VERSION, " +
            "IS_DEFAULT_VERSION, DESCRIPTION, VISIBILITY, IS_RESPONSE_CACHED, HAS_OWN_GATEWAY, CACHE_TIMEOUT, " +
            "UUID, TECHNICAL_OWNER, TECHNICAL_EMAIL, BUSINESS_OWNER, BUSINESS_EMAIL, LIFECYCLE_INSTANCE_ID, " +
            "CURRENT_LC_STATUS, CORS_ENABLED, CORS_ALLOW_ORIGINS, CORS_ALLOW_CREDENTIALS, CORS_ALLOW_HEADERS, " +
            "CORS_ALLOW_METHODS, API_TYPE_ID, CREATED_BY, CREATED_TIME, LAST_UPDATED_TIME, COPIED_FROM_API, " +
            "UPDATED_BY, LC_WORKFLOW_STATUS, SECURITY_SCHEME) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?," +
            "?,?,?,?,?,?,?,?,?)";

    private static final String API_DELETE = "DELETE FROM AM_API WHERE UUID = ?";

    private static final String AM_API_TABLE_NAME = "AM_API";
    private static final String AM_TAGS_TABLE_NAME = "AM_TAGS";
    private static final String AM_API_OPERATION_MAPPING_TABLE_NAME = "AM_API_OPERATION_MAPPING";
    private static final String AM_API_COMMENTS_TABLE_NAME = "AM_API_COMMENTS";
    private static final String AM_ENDPOINT_TABLE_NAME = "AM_ENDPOINT";
    private static final Logger log = LoggerFactory.getLogger(ApiDAOImpl.class);

    ApiDAOImpl(ApiDAOVendorSpecificStatements sqlStatements) {
        this.sqlStatements = sqlStatements;
    }

    @Override
    public boolean isAPIExists(String apiID) throws APIMgtDAOException {
        final String query = "SELECT 1 FROM AM_API WHERE UUID = ?";

        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, apiID);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return true;
                }
            }

        } catch (SQLException e) {
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + "checking if API: " + apiID + " exists", e);
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public API getAPI(String apiID) throws APIMgtDAOException {
        final String query = API_SELECT + " WHERE UUID = ? AND API_TYPE_ID = " +
                "(SELECT TYPE_ID FROM AM_API_TYPES WHERE TYPE_NAME = ?)";

        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, apiID);
            statement.setString(2, ApiType.STANDARD.toString());

            API api = constructAPIFromResultSet(connection, statement);

            if (api == null) {
                throw new APIMgtDAOException("API with ID " + apiID + " does not exist", ExceptionCodes.API_NOT_FOUND);
            }

            return api;
        } catch (SQLException | IOException e) {
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + "getting API: " + apiID, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public API getAPISummary(String apiID) throws APIMgtDAOException {
        final String query = API_SUMMARY_SELECT + " WHERE UUID = ? AND API_TYPE_ID = " +
                "(SELECT TYPE_ID FROM AM_API_TYPES WHERE TYPE_NAME = ?)";

        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, apiID);
            statement.setString(2, ApiType.STANDARD.toString());

            List<API> apiResults = constructAPISummaryList(connection, statement);
            if (apiResults.isEmpty()) {
                throw new APIMgtDAOException("API with ID " + apiID + " does not exist", ExceptionCodes.API_NOT_FOUND);
            }

            return apiResults.get(0);
        } catch (SQLException e) {
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + "getting API: " + apiID, e);
        }
    }

    @Override
    public CompositeAPI getCompositeAPISummary(String apiID) throws APIMgtDAOException {
        final String query = COMPOSITE_API_SUMMARY_SELECT + " WHERE UUID = ? AND API_TYPE_ID = " +
                "(SELECT TYPE_ID FROM AM_API_TYPES WHERE TYPE_NAME = ?)";

        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, apiID);
            statement.setString(2, ApiType.COMPOSITE.toString());

            List<CompositeAPI> apiResults = getCompositeAPISummaryList(connection, statement);
            if (apiResults.isEmpty()) {
                throw new APIMgtDAOException("API with ID " + apiID + " does not exist", ExceptionCodes.API_NOT_FOUND);
            }

            // there should be only 1 result from the database
            return apiResults.get(0);
        } catch (SQLException e) {
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + "getting Composite API: " + apiID, e);
        }
    }

    @Override
    public CompositeAPI getCompositeAPI(String apiID) throws APIMgtDAOException {
        final String query = "SELECT UUID, PROVIDER, NAME, CONTEXT, VERSION, DESCRIPTION, CREATED_BY, CREATED_TIME, " +
                "LAST_UPDATED_TIME, COPIED_FROM_API, UPDATED_BY, LC_WORKFLOW_STATUS " +
                "FROM AM_API WHERE UUID = ? AND API_TYPE_ID = (SELECT TYPE_ID FROM AM_API_TYPES WHERE TYPE_NAME = ?)";

        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, apiID);
            statement.setString(2, ApiType.COMPOSITE.toString());

            CompositeAPI api = getCompositeAPIFromResultSet(connection, statement);

            if (api == null) {
                throw new APIMgtDAOException("Composite API: " + apiID + " does not exist",
                        ExceptionCodes.API_NOT_FOUND);
            }

            return api;
        } catch (SQLException | IOException e) {
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + "getting Composite API: " + apiID, e);
        }
    }

    /**
     * @see ApiDAO#getLastUpdatedTimeOfAPI(java.lang.String)
     */
    @Override
    public String getLastUpdatedTimeOfAPI(String apiId) throws APIMgtDAOException {
        String lastUpdatedTime = EntityDAO.getLastUpdatedTimeOfResourceByUUID(AM_API_TABLE_NAME, apiId);

        if (lastUpdatedTime == null) {
            throw new APIMgtDAOException("API: " + apiId + ", does not exist", ExceptionCodes.API_NOT_FOUND);
        }

        return lastUpdatedTime;
    }

    /**
     * @see ApiDAO#getLastUpdatedTimeOfSwaggerDefinition(String)
     */
    @Override
    public String getLastUpdatedTimeOfSwaggerDefinition(String apiId) throws APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection()) {
            String lastUpdatedTime = ApiResourceDAO.getAPIUniqueResourceLastUpdatedTime(connection, apiId,
                    ResourceCategory.SWAGGER);

            if (lastUpdatedTime == null) {
                throw new APIMgtDAOException("Swagger Definition of API: " + apiId + ", does not exist",
                        ExceptionCodes.SWAGGER_NOT_FOUND);
            }

            return lastUpdatedTime;
        } catch (SQLException e) {
            String error = "getting last updated time of swagger definition for API: " + apiId;
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + error, e);
        }
    }

    /**
     * @see ApiDAO#getLastUpdatedTimeOfGatewayConfig(String)
     */
    @Override
    @CheckForNull
    public String getLastUpdatedTimeOfGatewayConfig(String apiId) throws APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection()) {
            String lastUpdatedTime = ApiResourceDAO
                    .getAPIUniqueResourceLastUpdatedTime(connection, apiId, ResourceCategory.GATEWAY_CONFIG);

            if (lastUpdatedTime == null) {
                throw new APIMgtDAOException("API Definition of API: " + apiId + ", does not exist",
                        ExceptionCodes.API_DEFINITION_NOT_FOUND);
            }

            return lastUpdatedTime;
        } catch (SQLException e) {
            String errorMessage = "getting last updated time of gateway config for API: " + apiId;
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + errorMessage, e);
        }
    }

    @Override
    @SuppressFBWarnings("SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING")
    public List<API> getAPIs(Set<String> roles, String user) throws APIMgtDAOException {
        int roleCount = roles.size();
        final String query;
        if (roleCount > 0) {
            query = API_SUMMARY_SELECT + " LEFT JOIN AM_API_GROUP_PERMISSION PERMISSION ON UUID = API_ID WHERE" +
                    " API_TYPE_ID = (SELECT TYPE_ID FROM AM_API_TYPES WHERE TYPE_NAME = '" + ApiType.STANDARD.toString()
                    + "') AND (((PERMISSION.GROUP_ID IN (" + DAOUtil.getParameterString(roles.size()) + "))" +
                    " AND PERMISSION.PERMISSION >= " + APIMgtConstants.Permission.READ_PERMISSION + ") OR " +
                    "(PROVIDER = ?) OR (PERMISSION.GROUP_ID IS NULL))";
        } else {
            query = API_SUMMARY_SELECT + " LEFT JOIN AM_API_GROUP_PERMISSION PERMISSION ON UUID = API_ID WHERE" +
                    " API_TYPE_ID = (SELECT TYPE_ID FROM AM_API_TYPES WHERE TYPE_NAME = '" + ApiType.STANDARD.toString()
                    + "') AND ((PROVIDER = ?) OR (PERMISSION.GROUP_ID IS NULL))";
        }
        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            int index = 0;
            if (roleCount > 0) {
                for (String role : roles) {
                    statement.setString(++index, role);
                }
            }
            statement.setString(++index, user);
            return constructAPISummaryList(connection, statement);
        } catch (SQLException e) {
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + "getting APIs", e);
        }
    }

    @Override
    @SuppressFBWarnings("SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING")
    public List<CompositeAPI> getCompositeAPIs(Set<String> roles, String user, int offset, int limit)
            throws APIMgtDAOException {

        // TODO: 6/5/17 Implement pagination support when implementing pagination support for
        // other list operations.
        final String query = COMPOSITE_API_SUMMARY_SELECT + " WHERE API_TYPE_ID = " +
                "(SELECT TYPE_ID FROM AM_API_TYPES WHERE TYPE_NAME = ?) AND PROVIDER = ?";

        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, ApiType.COMPOSITE.toString());
            statement.setString(2, user);

            return getCompositeAPISummaryList(connection, statement);
        } catch (SQLException e) {
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + "getting Composite APIs", e);
        }
    }

    /**
     * @see ApiDAO#getAPIsByStatus(Set)
     * @param statuses
     */
    @Override
    @SuppressFBWarnings("SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING")
    public List<API> getAPIsByStatus(Set<APIStatus> statuses) throws APIMgtDAOException {
        final String query = API_SUMMARY_SELECT + " WHERE CURRENT_LC_STATUS IN (" +
                DAOUtil.getParameterString(statuses.size()) + ") AND " +
                "API_TYPE_ID = (SELECT TYPE_ID FROM AM_API_TYPES WHERE TYPE_NAME = ?)";

        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            int i = 0;
            for (APIStatus status : statuses) {
                statement.setString(++i, status.getStatus());
            }

            statement.setString(++i, ApiType.STANDARD.toString());

            return constructAPISummaryList(connection, statement);
        } catch (SQLException e) {
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + "getting APIs by Status", e);
        }
    }

    /**
     * @see ApiDAO#getAPIsByStatus(Set, Set, Set)
     */
    @Override
    @SuppressFBWarnings("SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING")
    public List<API> getAPIsByStatus(Set<String> roles, Set<APIStatus> statuses, Set<String> labels)
            throws APIMgtDAOException {
        //check for null at the beginning before constructing the query to retrieve APIs from database
        if (roles == null || statuses == null) {
            String errorMessage = "Role list or API status list should not be null to retrieve APIs.";
            log.error(errorMessage);
            throw new APIMgtDAOException(errorMessage);
        }
        //the below query will be used to retrieve the union of,
        //published/prototyped APIs (statuses) with public visibility and
        //published/prototyped APIs with restricted visibility where APIs are restricted based on roles of the user
        String labelQuery = null;
        if (labels.isEmpty()) {
            labelQuery = "SELECT LABEL_ID FROM  AM_LABELS WHERE TYPE_NAME='STORE'";
        } else {
            labelQuery = "SELECT LABEL_ID FROM  AM_LABELS WHERE NAME IN ( " +
                    DAOUtil.getParameterString(labels.size()) + ") AND TYPE_NAME='STORE'";
        }
        final String query = "Select UUID, PROVIDER, NAME, CONTEXT, VERSION, DESCRIPTION, CURRENT_LC_STATUS, " +
                "LIFECYCLE_INSTANCE_ID, LC_WORKFLOW_STATUS, SECURITY_SCHEME  FROM (" + API_SUMMARY_SELECT + " WHERE " +
                "VISIBILITY = '" + API.Visibility.PUBLIC + "' " +
                "AND " +
                "CURRENT_LC_STATUS  IN (" + DAOUtil.getParameterString(statuses.size()) + ") AND " +
                "API_TYPE_ID = (SELECT TYPE_ID FROM AM_API_TYPES WHERE TYPE_NAME = ?)" +
                "UNION " +
                API_SUMMARY_SELECT +
                " WHERE " +
                "VISIBILITY = '" + API.Visibility.RESTRICTED + "' " +
                "AND " +
                "UUID IN (SELECT API_ID FROM AM_API_VISIBLE_ROLES WHERE ROLE IN " +
                "(" + DAOUtil.getParameterString(roles.size()) + ")) " +
                " AND CURRENT_LC_STATUS  IN (" + DAOUtil.getParameterString(statuses.size()) + ") AND " +
                " API_TYPE_ID = (SELECT TYPE_ID FROM AM_API_TYPES WHERE TYPE_NAME = ?)) A" +
                " JOIN AM_API_LABEL_MAPPING LM ON A.UUID=LM.API_ID WHERE LM.LABEL_ID IN (" + labelQuery + ")";

        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            int i = 0;
            //put desired API status into the query (to get APIs with public visibility)
            for (APIStatus status : statuses) {
                statement.setString(++i, status.getStatus());
            }

            statement.setString(++i, ApiType.STANDARD.toString());

            //put desired roles into the query
            for (String role : roles) {
                statement.setString(++i, role);
            }
            //put desired API status into the query (to get APIs with restricted visibility)
            for (APIStatus status : statuses) {
                statement.setString(++i, status.getStatus());
            }

            statement.setString(++i, ApiType.STANDARD.toString());
            //Set the label names in the query
            for (String label : labels) {
                statement.setString(++i, label);
            }

            return constructAPISummaryList(connection, statement);
        } catch (SQLException e) {
            String errorMessage = "Error while retrieving API list in store.";
            throw new APIMgtDAOException(errorMessage, e);
        }
    }

    /**
     * @see ApiDAO#searchAPIs(Set, String, String, int, int)
     */
    @Override
    @SuppressFBWarnings("SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING")
    public List<API> searchAPIs(Set<String> roles, String user, String searchString,
                                int offset, int limit) throws APIMgtDAOException {
        final String query = sqlStatements.getPermissionBasedApiFullTextSearchQuery(roles.size());
        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            sqlStatements.setPermissionBasedApiFullTextSearchStatement(statement, roles, user, searchString,
                    ApiType.STANDARD, offset, limit);

            return constructAPISummaryList(connection, statement);
        } catch (SQLException e) {
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + "searching APIs", e);
        }
    }

    @Override
    @SuppressFBWarnings("SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING")
    public List<CompositeAPI> searchCompositeAPIs(Set<String> roles, String user, String searchString,
                                                  int offset, int limit) throws APIMgtDAOException {
        final String query = sqlStatements.getPermissionBasedApiFullTextSearchQuery(roles.size());
        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            sqlStatements.setPermissionBasedApiFullTextSearchStatement(statement, roles, user, searchString,
                    ApiType.COMPOSITE, offset, limit);

            return getCompositeAPISummaryList(connection, statement);
        } catch (SQLException e) {
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + "searching Composite APIs", e);
        }
    }


    /**
     * @see ApiDAO#searchAPIsByAttributeInStore(Set, Set, Map, int, int)
     */
    @Override
    @SuppressFBWarnings("SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING")
    public List<API> searchAPIsByAttributeInStore(Set<String> roles, Set<String> labels, Map<SearchType, String>
            attributeMap, int offset, int limit) throws APIMgtDAOException {

        final String query = sqlStatements.getVisibilityBasedApiAttributeSearchQuery(roles.size(), labels.size(),
                attributeMap);

        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            DatabaseMetaData md = connection.getMetaData();
            Iterator<Map.Entry<SearchType, String>> entries = attributeMap.entrySet().iterator();

            while (entries.hasNext()) {
                Map.Entry<SearchType, String> entry = entries.next();

                DBTableMetaData metaData = sqlStatements.mapSearchAttributesToDB(entry.getKey());

                if (!checkTableColumnExists(md, metaData.getTableName(), metaData.getColumnName())) {
                    throw new APIMgtDAOException("Attribute does not exist with name: " + entry.getKey(),
                            ExceptionCodes.API_ATTRIBUTE_NOT_FOUND);
                }
            }

            sqlStatements.setVisibilityBasedApiAttributeSearchStatement(statement, roles, labels, attributeMap,
                    offset, limit);
            return constructAPISummaryList(connection, statement);
        } catch (SQLException e) {
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + "searching APIs by attribute", e);
        }
    }

    /**
     * @see ApiDAO#searchAPIsByStoreLabel(Set, String, String, int, int, Set)
     */
    @Override
    @SuppressFBWarnings("SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING")
    public List<API> searchAPIsByStoreLabel(Set<String> roles, String user, String searchString,
                                            int offset, int limit, Set<String> labels) throws APIMgtDAOException {
        final String query = sqlStatements.getVisibilityBasedApiFullTextSearchQuery(roles.size(), labels.size());
        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            sqlStatements.setVisibilityBasedApiFullTextSearchStatement(statement, roles, labels, searchString,
                    offset, limit);

            return constructAPISummaryList(connection, statement);
        } catch (SQLException e) {
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + "searching APIs", e);
        }
    }

    /**
     * @see ApiDAO#attributeSearchAPIs(Set, String, Map, Map, int, int, boolean)
     */
    @Override
    @SuppressFBWarnings("SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING")
    public List<API> attributeSearchAPIs(
            Set<String> roles, String user, Map<SearchType, String> attributeMap,
            Map<SecondarySearchType, String> secondaryAttributeMap, int offset, int limit, boolean expand)
            throws APIMgtDAOException {
        final String query = sqlStatements
                .getPermissionBasedApiAttributeSearchQuery(attributeMap, secondaryAttributeMap, roles.size());
        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            DatabaseMetaData md = connection.getMetaData();
            Iterator<Map.Entry<SearchType, String>> entries = attributeMap.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry<SearchType, String> entry = entries.next();
                String tableName = connection.getMetaData().getDriverName().contains(DAOUtil.DB_NAME_POSTGRESQL) ?
                        AM_API_TABLE_NAME.toLowerCase(Locale.ENGLISH) :
                        AM_API_TABLE_NAME;
                String columnName = connection.getMetaData().getDriverName().contains(DAOUtil.DB_NAME_POSTGRESQL) ?
                        entry.getKey().toString().toLowerCase(Locale.ENGLISH) :
                        entry.getKey().toString().toUpperCase(Locale.ENGLISH);
                if (!checkTableColumnExists(md, tableName, columnName)) {
                    throw new APIMgtDAOException("Attribute does not exist with name: " + entry.getKey(),
                            ExceptionCodes.API_ATTRIBUTE_NOT_FOUND);
                }
            }
            sqlStatements.setPermissionBasedApiAttributeSearchStatement(statement, roles, user, attributeMap,
                    secondaryAttributeMap, ApiType.STANDARD, offset, limit);

            if (expand) {
                return constructDetailAPIList(connection, statement);
            }
            return constructAPISummaryList(connection, statement);

        } catch (SQLException e) {
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + "searching APIs by attribute", e);
        }
    }

    /**
     * @see ApiDAO#isAPINameExists(String, String)
     */
    @Override
    public boolean isAPINameExists(String apiName, String providerName) throws APIMgtDAOException {
        final String apiExistsQuery = "SELECT 1 FROM AM_API WHERE LOWER(NAME) = ? AND PROVIDER = ? AND " +
                "API_TYPE_ID = (SELECT TYPE_ID FROM AM_API_TYPES WHERE TYPE_NAME = ?)";

        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(apiExistsQuery)) {
            statement.setString(1, apiName.toLowerCase(Locale.ENGLISH));
            statement.setString(2, providerName);
            statement.setString(3, ApiType.STANDARD.toString());

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return true;
                }
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + "checking if API: " + apiName +
                    "exists for provider " + providerName, e);
        }

        return false;
    }

    /**
     * @see org.wso2.carbon.apimgt.core.dao.ApiDAO#isAPIContextExists(String)
     */
    @Override
    public boolean isAPIContextExists(String contextName) throws APIMgtDAOException {
        final String apiExistsQuery = "SELECT 1 FROM AM_API WHERE CONTEXT = ?";

        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(apiExistsQuery)) {
            statement.setString(1, contextName);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return true;
                }
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + "checking if API Context: " +
                    contextName + " exists", e);
        }

        return false;
    }

    /**
     * Add a new instance of an API
     *
     * @param api The {@link API} object to be added
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    @Override
    public void addAPI(final API api) throws APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(API_INSERT)) {
            try {
                connection.setAutoCommit(false);

                addAPIRelatedInformation(connection, statement, api);
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new APIMgtDAOException("adding API: " + api.getProvider() + "-" +
                        api.getName() + "-" + api.getVersion(), e);
            } finally {
                connection.setAutoCommit(DAOUtil.isAutoCommit());
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + "adding API: " + api.getProvider() + " - " +
                    api.getName() + " - " + api.getVersion(), e);
        }
    }

    @Override
    public void addApplicationAssociatedAPI(CompositeAPI api) throws APIMgtDAOException {
        final String query = "INSERT INTO AM_API (PROVIDER, NAME, CONTEXT, VERSION, " +
                "DESCRIPTION, UUID, API_TYPE_ID, CREATED_BY, CREATED_TIME, LAST_UPDATED_TIME, COPIED_FROM_API, " +
                "UPDATED_BY, LC_WORKFLOW_STATUS) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            try {
                connection.setAutoCommit(false);

                addCompositeAPIRelatedInformation(connection, statement, api);
                String policyUuid = new DAOFactory().getPolicyDAO()
                        .getSubscriptionPolicy(ThrottleConstants.DEFAULT_SUB_POLICY_UNLIMITED).getUuid();
                APISubscriptionDAOImpl apiSubscriptionDAO = (APISubscriptionDAOImpl) new DAOFactory()
                        .getAPISubscriptionDAO();
                apiSubscriptionDAO.createSubscription(api.getId(), api.getApplicationId(), UUID.randomUUID().toString(),
                        policyUuid, APIMgtConstants.SubscriptionStatus.ACTIVE, connection);

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + "adding Composite API: " + api.getProvider() +
                        " - " + api.getName() + " - " + api.getVersion(), e);
            } finally {
                connection.setAutoCommit(DAOUtil.isAutoCommit());
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + "adding Composite API: " + api.getProvider() +
                    " - " + api.getName() + " - " + api.getVersion(), e);
        }
    }

    /**
     * Method for adding API related information
     *
     * @param connection DB Connection
     * @param statement  PreparedStatement
     * @param api        API object
     * @throws SQLException if error occurs while accessing data layer
     */
    private void addAPIRelatedInformation(Connection connection, PreparedStatement statement, final API api)
            throws SQLException, org.wso2.carbon.apimgt.core.exception.APIMgtDAOException {
        String apiPrimaryKey = api.getId();
        statement.setString(1, api.getProvider());
        statement.setString(2, api.getName());
        statement.setString(3, api.getContext());
        statement.setString(4, api.getVersion());
        statement.setBoolean(5, api.isDefaultVersion());
        statement.setString(6, api.getDescription());
        statement.setString(7, api.getVisibility().toString());
        statement.setBoolean(8, api.isResponseCachingEnabled());
        statement.setBoolean(9, api.hasOwnGateway());
        statement.setInt(10, api.getCacheTimeout());
        statement.setString(11, apiPrimaryKey);

        BusinessInformation businessInformation = api.getBusinessInformation();
        statement.setString(12, businessInformation.getTechnicalOwner());
        statement.setString(13, businessInformation.getTechnicalOwnerEmail());
        statement.setString(14, businessInformation.getBusinessOwner());
        statement.setString(15, businessInformation.getBusinessOwnerEmail());

        statement.setString(16, api.getLifecycleInstanceId());
        statement.setString(17, api.getLifeCycleStatus());

        CorsConfiguration corsConfiguration = api.getCorsConfiguration();
        statement.setBoolean(18, corsConfiguration.isEnabled());
        statement.setString(19, String.join(",", corsConfiguration.getAllowOrigins()));
        statement.setBoolean(20, corsConfiguration.isAllowCredentials());
        statement.setString(21, String.join(",", corsConfiguration.getAllowHeaders()));
        statement.setString(22, String.join(",", corsConfiguration.getAllowMethods()));

        statement.setInt(23, getApiTypeId(connection, ApiType.STANDARD));
        statement.setString(24, api.getCreatedBy());
        statement.setTimestamp(25, Timestamp.valueOf(LocalDateTime.now()));
        statement.setTimestamp(26, Timestamp.valueOf(LocalDateTime.now()));
        statement.setString(27, api.getCopiedFromApiId());
        statement.setString(28, api.getUpdatedBy());
        statement.setString(29, APILCWorkflowStatus.APPROVED.toString());
        statement.setInt(30, api.getSecurityScheme());
        statement.execute();

        if (API.Visibility.RESTRICTED == api.getVisibility()) {
            addVisibleRole(connection, apiPrimaryKey, api.getVisibleRoles());
        }

        addTagsMapping(connection, apiPrimaryKey, api.getTags());
        addLabelMapping(connection, apiPrimaryKey, api.getGatewayLabels(), APIMgtConstants.LABEL_TYPE_GATEWAY);
        addLabelMapping(connection, apiPrimaryKey, api.getStoreLabels(), APIMgtConstants.LABEL_TYPE_STORE);
        addGatewayConfig(connection, apiPrimaryKey, api.getGatewayConfig(), api.getCreatedBy());
        addTransports(connection, apiPrimaryKey, api.getTransport());
        addUrlMappings(connection, api.getUriTemplates().values(), apiPrimaryKey);
        addSubscriptionPolicies(connection, api.getPolicies(), apiPrimaryKey);
        addEndPointsForApi(connection, apiPrimaryKey, api.getEndpoint());
        addAPIDefinition(connection, apiPrimaryKey, api.getApiDefinition(), api.getCreatedBy());
        addAPIPermission(connection, api.getPermissionMap(), apiPrimaryKey);

        if (api.getAdditionalProperties() != null) {
            addAdditionalProperties(connection, api.getAdditionalProperties(), apiPrimaryKey);
        }

        if (api.getThreatProtectionPolicies() != null) {
            addThreatProtectionPolicies(connection, apiPrimaryKey, api.getThreatProtectionPolicies());
        }

        if (api.getApiPolicy() != null) {
            addApiPolicy(connection, api.getApiPolicy().getUuid(), apiPrimaryKey);
        }


    }

    /**
     * Method for adding Composite API related information
     *
     * @param connection DB Connection
     * @param statement  PreparedStatement
     * @param api        Composite API object
     * @throws SQLException if error occurs while accessing data layer
     */
    private void addCompositeAPIRelatedInformation(Connection connection, PreparedStatement statement,
                                                   final CompositeAPI api)
            throws SQLException, org.wso2.carbon.apimgt.core.exception.APIMgtDAOException {
        String apiPrimaryKey = api.getId();
        statement.setString(1, api.getProvider());
        statement.setString(2, api.getName());
        statement.setString(3, api.getContext());
        statement.setString(4, api.getVersion());
        statement.setString(5, api.getDescription());
        statement.setString(6, apiPrimaryKey);

        statement.setInt(7, getApiTypeId(connection, ApiType.COMPOSITE));
        statement.setString(8, api.getCreatedBy());
        statement.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
        statement.setTimestamp(10, Timestamp.valueOf(LocalDateTime.now()));
        statement.setString(11, api.getCopiedFromApiId());
        statement.setString(12, api.getUpdatedBy());
        statement.setString(13, APILCWorkflowStatus.APPROVED.toString());
        statement.execute();

        addLabelMapping(connection, apiPrimaryKey, api.getGatewayLabels(), APIMgtConstants.LABEL_TYPE_GATEWAY);
        addLabelMapping(connection, apiPrimaryKey, api.getStoreLabels(), APIMgtConstants.LABEL_TYPE_STORE);
        addGatewayConfig(connection, apiPrimaryKey, api.getGatewayConfig(), api.getCreatedBy());
        addTransports(connection, apiPrimaryKey, api.getTransport());
        addUrlMappings(connection, api.getUriTemplates().values(), apiPrimaryKey);
        addAPIDefinition(connection, apiPrimaryKey, api.getApiDefinition(), api.getCreatedBy());
        addAPIPermission(connection, api.getPermissionMap(), apiPrimaryKey);

        if (api.getThreatProtectionPolicies() != null) {
            addThreatProtectionPolicies(connection, apiPrimaryKey, api.getThreatProtectionPolicies());
        }
    }

    /**
     * Update an existing API
     *
     * @param apiID         The {@link String} of the API that needs to be updated
     * @param substituteAPI Substitute {@link API} object that will replace the existing API
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    @Override
    public void updateAPI(String apiID, API substituteAPI) throws APIMgtDAOException {
        final String query = "UPDATE AM_API SET CONTEXT = ?, IS_DEFAULT_VERSION = ?, DESCRIPTION = ?, VISIBILITY = ?, "
                + "IS_RESPONSE_CACHED = ?, CACHE_TIMEOUT = ?, TECHNICAL_OWNER = ?, TECHNICAL_EMAIL = ?, " +
                "BUSINESS_OWNER = ?, BUSINESS_EMAIL = ?, CORS_ENABLED = ?, CORS_ALLOW_ORIGINS = ?, " +
                "CORS_ALLOW_CREDENTIALS = ?, CORS_ALLOW_HEADERS = ?, CORS_ALLOW_METHODS = ?, LAST_UPDATED_TIME = ?," +
                "UPDATED_BY = ?, LC_WORKFLOW_STATUS = ?, SECURITY_SCHEME = ? WHERE UUID = ?";

        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            try {
                connection.setAutoCommit(false);
                statement.setString(1, substituteAPI.getContext());
                statement.setBoolean(2, substituteAPI.isDefaultVersion());
                statement.setString(3, substituteAPI.getDescription());
                statement.setString(4, substituteAPI.getVisibility().toString());
                statement.setBoolean(5, substituteAPI.isResponseCachingEnabled());
                statement.setInt(6, substituteAPI.getCacheTimeout());

                BusinessInformation businessInformation = substituteAPI.getBusinessInformation();
                statement.setString(7, businessInformation.getTechnicalOwner());
                statement.setString(8, businessInformation.getTechnicalOwnerEmail());
                statement.setString(9, businessInformation.getBusinessOwner());
                statement.setString(10, businessInformation.getBusinessOwnerEmail());

                CorsConfiguration corsConfiguration = substituteAPI.getCorsConfiguration();
                statement.setBoolean(11, corsConfiguration.isEnabled());
                statement.setString(12, String.join(",", corsConfiguration.getAllowOrigins()));
                statement.setBoolean(13, corsConfiguration.isAllowCredentials());
                statement.setString(14, String.join(",", corsConfiguration.getAllowHeaders()));
                statement.setString(15, String.join(",", corsConfiguration.getAllowMethods()));

                statement.setTimestamp(16, Timestamp.valueOf(LocalDateTime.now()));
                statement.setString(17, substituteAPI.getUpdatedBy());
                statement.setString(18, substituteAPI.getWorkflowStatus());
                statement.setInt(19, substituteAPI.getSecurityScheme());
                statement.setString(20, apiID);

                statement.execute();

                deleteVisibleRoles(connection, apiID); // Delete current visible roles if they exist

                if (API.Visibility.RESTRICTED == substituteAPI.getVisibility()) {
                    addVisibleRole(connection, apiID, substituteAPI.getVisibleRoles());
                }

                deleteAPIPermission(connection, apiID);
                updateApiPermission(connection, substituteAPI.getPermissionMap(), apiID);

                deleteTransports(connection, apiID);
                addTransports(connection, apiID, substituteAPI.getTransport());

                deleteThreatProtectionPolicies(connection, apiID);
                if (substituteAPI.getThreatProtectionPolicies() != null) {
                    addThreatProtectionPolicies(connection, apiID, substituteAPI.getThreatProtectionPolicies());
                }

                deleteTagsMapping(connection, apiID); // Delete current tag mappings if they exist
                addTagsMapping(connection, apiID, substituteAPI.getTags());
                deleteLabelsMapping(connection, apiID);
                addLabelMapping(connection, apiID, substituteAPI.getGatewayLabels(),
                        APIMgtConstants.LABEL_TYPE_GATEWAY);
                addLabelMapping(connection, apiID, substituteAPI.getStoreLabels(), APIMgtConstants.LABEL_TYPE_STORE);
                deleteSubscriptionPolicies(connection, apiID);
                addSubscriptionPolicies(connection, substituteAPI.getPolicies(), apiID);
                deleteEndPointsForApi(connection, apiID);
                addEndPointsForApi(connection, apiID, substituteAPI.getEndpoint());
                deleteEndPointsForOperation(connection, apiID);
                deleteUrlMappings(connection, apiID);
                addUrlMappings(connection, substituteAPI.getUriTemplates().values(), apiID);
                deleteApiPolicy(connection, apiID);
                if (substituteAPI.getApiPolicy() != null) {
                    addApiPolicy(connection, substituteAPI.getApiPolicy().getUuid(), apiID);
                }
                connection.commit();
            } catch (SQLException | IOException e) {
                connection.rollback();
                throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + "updating API: " + substituteAPI.getProvider() +
                        " - " + substituteAPI.getName() + " - " + substituteAPI.getVersion(), e);
            } finally {
                connection.setAutoCommit(DAOUtil.isAutoCommit());
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + "updating API: " + substituteAPI.getProvider() +
                    " - " + substituteAPI.getName() + " - " + substituteAPI.getVersion(), e);
        }
    }

    /**
     * Remove an existing API
     *
     * @param apiID The {@link String} of the API that needs to be deleted
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    @Override
    public void deleteAPI(String apiID) throws APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(API_DELETE)) {
            persistAPIDelete(connection, statement, apiID);
        } catch (SQLException | IOException e) {
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + "deleting API: " + apiID, e);
        }
    }

    @Override
    public void deleteCompositeApi(String apiId) throws APIMgtDAOException {
        APISubscriptionDAO apiSubscriptionDAO = new DAOFactory().getAPISubscriptionDAO();
        List<Subscription> subscriptions = apiSubscriptionDAO.getAPISubscriptionsByAPI(apiId);

        for (Subscription subscription : subscriptions) {
            apiSubscriptionDAO.deleteAPISubscription(subscription.getId());
        }


        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(API_DELETE)) {
            persistAPIDelete(connection, statement, apiId);
        } catch (SQLException | IOException e) {
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + "deleting Composite API: " + apiId, e);
        }
    }

    private void persistAPIDelete(Connection connection, PreparedStatement statement, String apiId)
            throws IOException, SQLException {
        try {
            connection.setAutoCommit(false);

            deleteAPIRelatedInformation(connection, statement, apiId);

            connection.commit();
        } catch (SQLException | IOException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(DAOUtil.isAutoCommit());
        }
    }

    private void deleteAPIRelatedInformation(Connection connection, PreparedStatement statement, String apiID)
            throws IOException, SQLException {
        deleteEndPointsForOperation(connection, apiID);
        deleteLabelsMapping(connection, apiID);
        deleteUrlMappings(connection, apiID);
        deleteEndPointsForApi(connection, apiID);
        deleteThreatProtectionPolicies(connection, apiID);
        statement.setString(1, apiID);
        statement.execute();
    }


    /**
     * Get swagger definition of a given API
     *
     * @param apiID The UUID of the respective API
     * @return Swagger definition String
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    @Override
    public String getApiSwaggerDefinition(String apiID) throws APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection()) {
            String apiDefinition = getAPIDefinition(connection, apiID);

            if (apiDefinition == null) {
                throw new APIMgtDAOException("Swagger Definition of API: " + apiID + ", does not exist",
                        ExceptionCodes.SWAGGER_NOT_FOUND);
            }

            return apiDefinition;
        } catch (SQLException | IOException e) {
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + "getting swagger definition of API: " + apiID, e);
        }
    }

    @Override
    public String getCompositeApiSwaggerDefinition(String apiID) throws APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection()) {
            InputStream apiDefinition = ApiResourceDAO.getBinaryValueForCategory(connection, apiID,
                    ResourceCategory.SWAGGER, ApiType.COMPOSITE);

            if (apiDefinition == null) {
                throw new APIMgtDAOException("Composite Swagger Definition of API: " + apiID + ", does not exist",
                        ExceptionCodes.SWAGGER_NOT_FOUND);
            }
            return IOUtils.toString(apiDefinition, StandardCharsets.UTF_8);
        } catch (SQLException | IOException e) {
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + "getting swagger definition of Composite API: " +
                    apiID, e);
        }
    }

    @Override
    public void updateApiDefinition(String apiID, String apiDefinition, String updatedBy)
            throws APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection()) {
            try {
                connection.setAutoCommit(false);
                updateAPIDefinition(connection, apiID, apiDefinition, updatedBy);
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + "updating API definition of API: " + apiID, e);
            } finally {
                connection.setAutoCommit(DAOUtil.isAutoCommit());
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + "updating API definition of API: " + apiID, e);
        }
    }

    @Override
    public boolean isWSDLArchiveExists(String apiId) throws APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection()) {
            return ApiResourceDAO
                    .isResourceExistsForCategory(connection, apiId, ResourceCategory.WSDL_ZIP);
        } catch (SQLException e) {
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX +
                    "checking if WSDL archive exists for API(api: " + apiId + ")", e);
        }
    }

    @Override
    public boolean isWSDLExists(String apiId) throws APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection()) {
            return ApiResourceDAO.isResourceExistsForCategory(connection, apiId, ResourceCategory.WSDL_ZIP)
                    || ApiResourceDAO.isResourceExistsForCategory(connection, apiId, ResourceCategory.WSDL_TEXT);
        } catch (SQLException e) {
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX +
                    "checking if WSDL exists for API(api: " + apiId + ")", e);
        }
    }

    @Override
    public String getWSDL(String apiId) throws APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection()) {

            InputStream wsdlContent = ApiResourceDAO
                    .getBinaryValueForCategory(connection, apiId, ResourceCategory.WSDL_TEXT, ApiType.STANDARD);

            if (wsdlContent != null) {
                return IOUtils.toString(wsdlContent, StandardCharsets.UTF_8);
            }
        } catch (SQLException | IOException e) {
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + "getting WSDL for API(api: " + apiId + ")", e);
        }
        return null;
    }

    @Override
    public InputStream getWSDLArchive(String apiId) throws APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection()) {
            return ApiResourceDAO.getBinaryValueForCategory(connection, apiId, ResourceCategory.WSDL_ZIP,
                    ApiType.STANDARD);
        } catch (SQLException | IOException e) {
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX +
                    "getting WSDL archive for API(api: " + apiId + ")", e);
        }
    }

    @Override
    public void addOrUpdateWSDL(String apiId, byte[] wsdlContent, String updatedBy) throws APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection()) {
            try {
                connection.setAutoCommit(false);
                if (!ApiResourceDAO.isResourceExistsForCategory(connection, apiId,
                        ResourceCategory.WSDL_TEXT)) {
                    if (ApiResourceDAO.isResourceExistsForCategory(connection, apiId,
                            ResourceCategory.WSDL_ZIP)) {
                        removeWSDLArchiveOfAPI(apiId);
                    }
                    ApiResourceDAO.addBinaryResource(connection, apiId, UUID.randomUUID().toString(),
                            ResourceCategory.WSDL_TEXT, MediaType.TEXT_XML,
                            new ByteArrayInputStream(wsdlContent), updatedBy);
                } else {
                    ApiResourceDAO.updateBinaryResourceForCategory(connection, apiId,
                            ResourceCategory.WSDL_TEXT, new ByteArrayInputStream(wsdlContent), updatedBy);
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX +
                        "adding or updating WSDL for API(api: " + apiId + ")", e);
            } finally {
                connection.setAutoCommit(DAOUtil.isAutoCommit());
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX +
                    "adding or updating WSDL for API(api: " + apiId + ")", e);
        }
    }

    @Override
    public void addOrUpdateWSDLArchive(String apiID, InputStream inputStream, String updatedBy)
            throws APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection()) {
            try {
                connection.setAutoCommit(false);
                if (!ApiResourceDAO.isResourceExistsForCategory(connection, apiID,
                        ResourceCategory.WSDL_ZIP)) {
                    if (ApiResourceDAO.isResourceExistsForCategory(connection, apiID,
                            ResourceCategory.WSDL_TEXT)) {
                        removeWSDL(apiID);
                    }
                    ApiResourceDAO.addBinaryResource(connection, apiID, UUID.randomUUID().toString(),
                            ResourceCategory.WSDL_ZIP, MediaType.APPLICATION_OCTET_STREAM, inputStream, updatedBy);
                } else {
                    ApiResourceDAO.updateBinaryResourceForCategory(connection, apiID,
                            ResourceCategory.WSDL_ZIP, inputStream, updatedBy);
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX +
                        "adding or updating WSDL archive for API(api: " + apiID + ")", e);
            } finally {
                connection.setAutoCommit(DAOUtil.isAutoCommit());
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX +
                    "adding or updating WSDL archive for API(api: " + apiID + ")", e);
        }
    }

    @Override
    public void removeWSDL(String apiId) throws APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection()) {
            ApiResourceDAO.deleteUniqueResourceForCategory(connection, apiId, ResourceCategory.WSDL_TEXT);
        } catch (SQLException e) {
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + "removing WSDL for API(api: " + apiId + ")", e);
        }
    }

    @Override
    public void removeWSDLArchiveOfAPI(String apiId) throws APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection()) {
            ApiResourceDAO.deleteUniqueResourceForCategory(connection, apiId, ResourceCategory.WSDL_ZIP);
        } catch (SQLException e) {
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX +
                    "removing WSDL archive for API(api: " + apiId + ")", e);
        }
    }

    /**
     * Get gateway configuration of a given API
     *
     * @param apiID The UUID of the respective API
     * @return gateway configuration String
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    public String getGatewayConfigOfAPI(String apiID) throws APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection()) {
            String gatewayConfig = getGatewayConfig(connection, apiID);

            if (gatewayConfig == null) {
                throw new APIMgtDAOException("Gateway config of API: " + apiID + " does not exist",
                        ExceptionCodes.API_DEFINITION_NOT_FOUND);
            }

            return gatewayConfig;
        } catch (SQLException | IOException e) {
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + "getting Gateway Config of API: " + apiID, e);
        }
    }

    @Override
    public InputStream getCompositeAPIGatewayConfig(String apiID) throws APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection()) {
            InputStream gatewayConfig = ApiResourceDAO
                    .getBinaryValueForCategory(connection, apiID, ResourceCategory.GATEWAY_CONFIG, ApiType.COMPOSITE);

            if (gatewayConfig == null) {
                throw new APIMgtDAOException("Gateway config of Composite API: " + apiID + ", does not exist",
                        ExceptionCodes.API_DEFINITION_NOT_FOUND);
            }

            return gatewayConfig;
        } catch (SQLException | IOException e) {
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX +
                    "getting Gateway Config for Composite API: " + apiID, e);
        }
    }

    @Override
    public void updateGatewayConfig(String apiID, String gatewayConfig, String updatedBy) throws APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection()) {
            updateGatewayConfig(connection, apiID, gatewayConfig, updatedBy);
        } catch (SQLException e) {
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + "updating Gateway Config for API: " + apiID, e);
        }
    }

    @Override
    public void updateCompositeAPIGatewayConfig(String apiID, InputStream gatewayConfig, String updatedBy)
            throws APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection()) {
            ApiResourceDAO.updateBinaryResourceForCategory(connection, apiID, ResourceCategory.GATEWAY_CONFIG,
                    gatewayConfig, updatedBy);
        } catch (SQLException e) {
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX +
                    "updating Gateway Config for Composite API: " + apiID, e);
        }
    }

    @Override
    public String getLastUpdatedTimeOfDocument(String documentId) throws APIMgtDAOException {
        return DocMetaDataDAO.getLastUpdatedTimeOfDocument(documentId);
    }

    @Override
    public String getLastUpdatedTimeOfDocumentContent(String apiId, String documentId) throws APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection()) {
            return ApiResourceDAO
                    .getResourceLastUpdatedTime(connection, apiId, documentId, ResourceCategory.DOC);
        } catch (SQLException e) {
            String errorMessage =
                    "getting last updated time of document for API: " + apiId + ", Doc: " + documentId;
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + errorMessage, e);
        }
    }

    /**
     * @see ApiDAO#getLastUpdatedTimeOfAPIThumbnailImage(String)
     */
    @Override
    public String getLastUpdatedTimeOfAPIThumbnailImage(String apiId) throws APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection()) {
            return ApiResourceDAO
                    .getAPIUniqueResourceLastUpdatedTime(connection, apiId, ResourceCategory.IMAGE);
        } catch (SQLException e) {
            String errorMessage = "getting last updated time of thumbnail image for API: " + apiId;
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + errorMessage, e);
        }
    }

    /**
     * @see ApiDAO#getLastUpdatedTimeOfEndpoint(String)
     */
    @Override
    public String getLastUpdatedTimeOfEndpoint(String endpointId) throws APIMgtDAOException {
        return EntityDAO.getLastUpdatedTimeOfResourceByUUID(AM_ENDPOINT_TABLE_NAME, endpointId);
    }

    @Override
    public Comment getCommentByUUID(String commentId, String apiId) throws APIMgtDAOException {
        final String query = "SELECT UUID, COMMENT_TEXT, USER_IDENTIFIER, API_ID, "
                + "CREATED_BY, CREATED_TIME, UPDATED_BY, LAST_UPDATED_TIME "
                + "FROM AM_API_COMMENTS WHERE UUID = ? AND API_ID = ?";

        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            try {
                statement.setString(1, commentId);
                statement.setString(2, apiId);
                statement.execute();
                try (ResultSet rs = statement.getResultSet()) {
                    if (rs.next()) {
                        return constructCommentFromResultSet(rs);
                    }
                }
            } catch (SQLException e) {
                String errorMessage =
                        "getting comment for API: " + apiId + ", Comment: " + commentId;
                throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + errorMessage, e);
            }
        } catch (SQLException e) {
            String errorMessage = "getting comment for API: " + apiId + ", Comment: " + commentId;
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + errorMessage, e);
        }
        return null;
    }

    @Override
    public boolean isEndpointExist(String name) throws APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection()) {
            return isEndpointExist(connection, name);
        } catch (SQLException e) {
            String errorMessage = "checking existence of Endpoint: " + name;
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + errorMessage, e);
        }
    }

    /**
     * Constructs a comment object from a resulset object
     *
     * @param rs result set object
     * @return
     * @throws SQLException
     */
    private Comment constructCommentFromResultSet(ResultSet rs) throws SQLException {
        Comment comment = new Comment();

        comment.setUuid(rs.getString("UUID"));
        comment.setCommentText(rs.getString("COMMENT_TEXT"));
        comment.setCommentedUser(rs.getString("USER_IDENTIFIER"));
        comment.setApiId(rs.getString("API_ID"));
        comment.setCreatedUser(rs.getString("CREATED_BY"));
        comment.setCreatedTime(rs.getTimestamp("CREATED_TIME").toInstant());
        comment.setUpdatedUser(rs.getString("UPDATED_BY"));
        comment.setUpdatedTime(rs.getTimestamp("LAST_UPDATED_TIME").toInstant());

        return comment;
    }


    @Override
    public void addComment(Comment comment, String apiId) throws APIMgtDAOException {
        final String addCommentQuery =
                "INSERT INTO AM_API_COMMENTS (UUID, COMMENT_TEXT, USER_IDENTIFIER, API_ID, " +
                        "CREATED_BY, CREATED_TIME, UPDATED_BY, LAST_UPDATED_TIME" + ") VALUES (?,?,?,?,?,?,?,?)";
        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(addCommentQuery)) {
            try {
                connection.setAutoCommit(false);
                statement.setString(1, comment.getUuid());
                statement.setString(2, comment.getCommentText());
                statement.setString(3, comment.getCommentedUser());
                statement.setString(4, apiId);
                statement.setString(5, comment.getCreatedUser());
                statement.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
                statement.setString(7, comment.getUpdatedUser());
                statement.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));
                statement.execute();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                String errorMessage = "adding comment for API " + apiId;
                throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + errorMessage, e);
            } finally {
                connection.setAutoCommit(DAOUtil.isAutoCommit());
            }
        } catch (SQLException e) {
            String errorMessage = "adding comment for API " + apiId;
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + errorMessage, e);
        }
    }


    @Override
    public void deleteComment(String commentId, String apiId) throws APIMgtDAOException {
        final String deleteCommentQuery = "DELETE FROM AM_API_COMMENTS WHERE UUID = ? AND API_ID = ?";
        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(deleteCommentQuery)) {
            try {
                connection.setAutoCommit(false);
                statement.setString(1, commentId);
                statement.setString(2, apiId);
                statement.execute();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                String errorMessage =
                        "deleting comment for API " + apiId + ", Comment " + commentId;
                throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + errorMessage, e);
            } finally {
                connection.setAutoCommit(DAOUtil.isAutoCommit());
            }
        } catch (SQLException e) {
            String errorMessage = "deleting comment for API " + apiId + ", Comment " + commentId;
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + errorMessage, e);
        }
    }


    @Override
    public void updateComment(Comment comment, String commentId, String apiId) throws APIMgtDAOException {
        final String updateCommentQuery = "UPDATE AM_API_COMMENTS SET COMMENT_TEXT = ? "
                + ", UPDATED_BY = ? , LAST_UPDATED_TIME = ?"
                + " WHERE UUID = ? AND API_ID = ?";
        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(updateCommentQuery)) {
            try {
                connection.setAutoCommit(false);
                statement.setString(1, comment.getCommentText());
                statement.setString(2, comment.getUpdatedUser());
                statement.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                statement.setString(4, commentId);
                statement.setString(5, apiId);
                statement.execute();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                String errorMessage = "updating comment for API " + apiId + ", Comment " + commentId;
                throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + errorMessage, e);
            } finally {
                connection.setAutoCommit(DAOUtil.isAutoCommit());
            }
        } catch (SQLException e) {
            String errorMessage = "updating comment for API " + apiId + ", Comment " + commentId;
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + errorMessage, e);
        }

    }


    @Override
    public List<Comment> getCommentsForApi(String apiId) throws APIMgtDAOException {
        List<Comment> commentList = new ArrayList<>();
        final String getCommentsQuery = "SELECT UUID, COMMENT_TEXT, USER_IDENTIFIER, API_ID, "
                + "CREATED_BY, CREATED_TIME, UPDATED_BY, LAST_UPDATED_TIME "
                + "FROM AM_API_COMMENTS WHERE API_ID = ?";
        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(getCommentsQuery)) {
            try {
                statement.setString(1, apiId);
                statement.execute();
                try (ResultSet rs = statement.getResultSet()) {
                    while (rs.next()) {
                        commentList.add(constructCommentFromResultSet(rs));
                    }
                }
            } catch (SQLException e) {
                connection.rollback();
                String errorMessage = "getting all comments for API " + apiId;
                throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + errorMessage, e);
            }
        } catch (SQLException e) {
            String errorMessage = "getting all comments for API " + apiId;
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + errorMessage, e);
        }
        return commentList;
    }

    @Override
    public String getLastUpdatedTimeOfComment(String commentId) throws APIMgtDAOException {
        return EntityDAO.getLastUpdatedTimeOfResourceByUUID(AM_API_COMMENTS_TABLE_NAME, commentId);
    }

    @Override
    public void addRating(String apiId, Rating rating) throws APIMgtDAOException {
        final String addRatingQuery =
                "INSERT INTO AM_API_RATINGS (UUID, API_ID, RATING, USER_IDENTIFIER, " +
                        "CREATED_BY, CREATED_TIME, UPDATED_BY, LAST_UPDATED_TIME" + ") VALUES (?,?,?,?,?,?,?,?)";
        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(addRatingQuery)) {
            try {
                connection.setAutoCommit(false);
                statement.setString(1, rating.getUuid());
                statement.setString(2, apiId);
                statement.setInt(3, rating.getRating());
                statement.setString(4, rating.getUsername());
                statement.setString(5, rating.getCreatedUser());
                statement.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
                statement.setString(7, rating.getLastUpdatedUser());
                statement.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));
                statement.execute();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                String errorMessage = "adding rating for API: " + apiId;
                throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + errorMessage, e);
            } finally {
                connection.setAutoCommit(DAOUtil.isAutoCommit());
            }
        } catch (SQLException e) {
            String errorMessage = "adding rating for API: " + apiId;
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + errorMessage, e);
        }
    }

    @Override
    public Rating getRatingByUUID(String apiId, String ratingId) throws APIMgtDAOException {
        final String query = "SELECT UUID, API_ID, RATING, USER_IDENTIFIER, " +
                "CREATED_BY, CREATED_TIME, UPDATED_BY, LAST_UPDATED_TIME "
                + "FROM AM_API_RATINGS WHERE UUID = ? AND API_ID = ?";
        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            try {
                statement.setString(1, ratingId);
                statement.setString(2, apiId);
                statement.execute();
                try (ResultSet rs = statement.getResultSet()) {
                    if (rs.next()) {
                        return constructRatingFromResultSet(rs);
                    }
                }
            } catch (SQLException e) {
                String errorMessage = "getting Rating: " + ratingId + " for API: " + apiId;
                throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + errorMessage, e);
            }
        } catch (SQLException e) {
            String errorMessage = "getting Rating: " + ratingId + " for API: " + apiId;
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + errorMessage, e);
        }
        return null;
    }

    @Override
    public Rating getUserRatingForApiFromUser(String apiId, String userId) throws APIMgtDAOException {
        final String query = "SELECT UUID, API_ID, RATING, USER_IDENTIFIER, " +
                "CREATED_BY, CREATED_TIME, UPDATED_BY, LAST_UPDATED_TIME "
                + "FROM AM_API_RATINGS WHERE USER_IDENTIFIER = ? AND API_ID = ?";
        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            try {
                statement.setString(1, userId);
                statement.setString(2, apiId);
                statement.execute();
                try (ResultSet rs = statement.getResultSet()) {
                    if (rs.next()) {
                        return constructRatingFromResultSet(rs);
                    }
                }
            } catch (SQLException e) {
                String errorMessage = "getting User Rating for API: " + apiId + ", User: " + userId;
                throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + errorMessage, e);
            }
        } catch (SQLException e) {
            String errorMessage = "getting User Rating for API: " + apiId + ", User: " + userId;
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + errorMessage, e);
        }
        return null;
    }

    /**
     * Constructs a Rating object from a result set
     *
     * @param rs the result set retrieved from db
     * @return Rating constructed from result set
     * @throws APIMgtDAOException if result set access fails
     */
    private Rating constructRatingFromResultSet(ResultSet rs) throws APIMgtDAOException {
        Rating rating = new Rating();
        try {
            rating.setUuid(rs.getString("UUID"));
            rating.setRating((int) Double.parseDouble(rs.getString("RATING")));
            rating.setApiId(rs.getString("API_ID"));
            rating.setUsername(rs.getString("USER_IDENTIFIER"));
            rating.setCreatedUser(rs.getString("CREATED_BY"));
            rating.setCreatedTime(rs.getTimestamp("CREATED_TIME").toInstant());
            rating.setLastUpdatedUser(rs.getString("UPDATED_BY"));
            rating.setLastUpdatedTime(rs.getTimestamp("LAST_UPDATED_TIME").toInstant());
        } catch (SQLException e) {
            String errorMessage = "constructing Rating from ResultSet";
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + errorMessage, e);
        }
        return rating;
    }

    @Override
    public void updateRating(String apiId, String ratingId, Rating rating) throws APIMgtDAOException {
        final String updateRatingQuery = "UPDATE AM_API_RATINGS SET RATING = ? , UPDATED_BY = ? , LAST_UPDATED_TIME = ?"
                + " WHERE API_ID = ? AND UUID = ?";
        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(updateRatingQuery)) {
            try {
                connection.setAutoCommit(false);
                statement.setInt(1, rating.getRating());
                statement.setString(2, rating.getUsername());
                statement.setTimestamp(3, Timestamp.from(rating.getLastUpdatedTime()));
                statement.setString(4, apiId);
                statement.setString(5, ratingId);
                statement.execute();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                String errorMessage = "updating Rating for API: " + apiId + ", Rating: " + rating.getUuid();
                throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + errorMessage, e);
            } finally {
                connection.setAutoCommit(DAOUtil.isAutoCommit());
            }
        } catch (SQLException e) {
            String errorMessage = "updating Rating for API: " + apiId + ", Rating: " + rating.getUuid();
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + errorMessage, e);
        }

    }

    @Override
    public List<Rating> getRatingsListForApi(String apiId) throws APIMgtDAOException {
        final String query = "SELECT UUID, API_ID, RATING, USER_IDENTIFIER, " +
                "CREATED_BY, CREATED_TIME, UPDATED_BY, LAST_UPDATED_TIME "
                + "FROM AM_API_RATINGS WHERE API_ID = ?";
        List<Rating> ratingsList = new ArrayList<>();
        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            try {
                statement.setString(1, apiId);
                statement.execute();
                try (ResultSet rs = statement.getResultSet()) {
                    while (rs.next()) {
                        ratingsList.add(constructRatingFromResultSet(rs));
                    }
                }
            } catch (SQLException e) {
                String errorMessage = "getting rating for API: " + apiId;
                throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + errorMessage, e);
            }
        } catch (SQLException e) {
            String errorMessage = "getting rating for API: " + apiId;
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + errorMessage, e);
        }
        return ratingsList;
    }

    @Override
    public List<String> getUUIDsOfGlobalEndpoints() throws APIMgtDAOException {
        final String query = "SELECT UUID FROM AM_ENDPOINT WHERE APPLICABLE_LEVEL='" + APIMgtConstants
                .GLOBAL_ENDPOINT + "'";
        List<String> endpointList = new ArrayList<>();
        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                endpointList.add(resultSet.getString(APIMgtConstants.ThrottlePolicyConstants.COLUMN_UUID));
            }
        } catch (SQLException e) {
            String msg = "getting UUIDs of Global Endpoints";
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + msg, e);
        }
        return endpointList;
    }

    @Override
    public String getEndpointConfig(String endpointId) throws APIMgtDAOException {
        final String query = "SELECT GATEWAY_CONFIG FROM AM_ENDPOINT WHERE UUID = ?";
        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, endpointId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    InputStream inputStream = resultSet.getBinaryStream(APIMgtConstants.GATEWAY_CONFIG);
                    if (inputStream != null) {
                        return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                    } else {
                        throw new APIMgtDAOException("Couldn't Find Endpoint Config for Endpoint: " + endpointId,
                                ExceptionCodes.ENDPOINT_CONFIG_NOT_FOUND);
                    }
                } else {
                    throw new APIMgtDAOException("Couldn't Find Endpoint Config for Endpoint: " + endpointId,
                            ExceptionCodes.ENDPOINT_CONFIG_NOT_FOUND);
                }
            }
        } catch (SQLException | IOException e) {
            String msg = "getting Endpoints Gateway Config for Endpoint: " + endpointId;
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + msg, e);
        }
    }

    /**
     * @see ApiDAO#updateDedicatedGateway(DedicatedGateway, List)
     */
    @Override
    public void updateDedicatedGateway(DedicatedGateway dedicatedGateway, List<String> labels)
            throws APIMgtDAOException {

        // labels will come in 2 ways.
        // 1. auto-generated label - Update from dedicateGateway false to true
        // 2. default label - Update from dedicatedGateway true to false

        String apiId = dedicatedGateway.getApiId();
        final String query = "UPDATE AM_API SET HAS_OWN_GATEWAY = ?, LAST_UPDATED_TIME = ?, UPDATED_BY = ? " +
                "WHERE UUID = ?";
        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            try {
                connection.setAutoCommit(false);
                statement.setBoolean(1, dedicatedGateway.isEnabled());
                statement.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                statement.setString(3, dedicatedGateway.getUpdatedBy());
                statement.setString(4, apiId);

                // if the labels are not null or not empty
                if (labels != null && !labels.isEmpty()) {
                    deleteLabelsMapping(connection, apiId);
                    addLabelMapping(connection, apiId, labels, APIMgtConstants.LABEL_TYPE_GATEWAY);
                }
                statement.execute();
                connection.commit();
            } catch (SQLException e) {
                String msg = "Couldn't update dedicated Gateway details of API : " + apiId;
                connection.rollback();
                throw new APIMgtDAOException(msg, e);
            } finally {
                connection.setAutoCommit(DAOUtil.isAutoCommit());
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException("Error Executing query for updating Container Based Gateway", e);
        }
    }

    /**
     * @see ApiDAO#getDedicatedGateway(String)
     */
    @Override
    public DedicatedGateway getDedicatedGateway(String apiId) throws APIMgtDAOException {
        final String query = "SELECT HAS_OWN_GATEWAY FROM AM_API WHERE UUID = ?";
        DedicatedGateway dedicatedGateway;

        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            try {
                statement.setString(1, apiId);
                statement.execute();
                try (ResultSet rs = statement.getResultSet()) {
                    if (rs.next()) {
                        dedicatedGateway = new DedicatedGateway();
                        dedicatedGateway.setEnabled(rs.getBoolean(
                                ContainerBasedGatewayConstants.IS_DEDICATED_GATEWAY_ENABLED));
                        return dedicatedGateway;
                    } else {
                        throw new APIMgtDAOException("Couldn't Find Dedicated Gateway details ", ExceptionCodes
                                .DEDICATED_GATEWAY_DETAILS_NOT_FOUND);
                    }
                }
            } catch (SQLException e) {
                String errorMessage = "Error while retrieving dedicated gateway details of API : " + apiId;
                throw new APIMgtDAOException(errorMessage, e);
            }
        } catch (SQLException e) {
            String message = "Error while creating database connection/prepared-statement";
            throw new APIMgtDAOException(message, e);
        }
    }

    @Override
    public boolean isAPIVersionsExist(String apiName) throws APIMgtDAOException {
        final String query = "SELECT COUNT (NAME) FROM AM_API WHERE NAME = ?";
        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, apiName);
            statement.execute();
            try (ResultSet rs = statement.getResultSet()) {
                if (rs.next() && rs.getInt(1) > 1) {
                    return true;
                } else {
                    return false;
                }
            }
        } catch (SQLException e) {
            String errorMessage = "getting existence of versioned API: " + apiName;
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + errorMessage, e);
        }
    }

    @Override
    public double getAverageRating(String apiId) throws APIMgtDAOException {
        final String query = "SELECT AVG(RATING) FROM AM_API_RATINGS WHERE API_ID = ?";
        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            try {
                statement.setString(1, apiId);
                statement.execute();
                try (ResultSet rs = statement.getResultSet()) {
                    if (rs.next()) {
                        return rs.getDouble(1);
                    }
                }
            } catch (SQLException e) {
                String errorMessage = "getting Average Rating for API: " + apiId;
                throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + errorMessage, e);
            }
        } catch (SQLException e) {
            String errorMessage = "getting Average Rating for API: " + apiId;
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + errorMessage, e);
        }
        return 0;
    }

    /**
     * @see org.wso2.carbon.apimgt.core.dao.ApiDAO#getResourcesOfApi(String, String)
     */
    @Override
    public List<UriTemplate> getResourcesOfApi(String apiContext, String apiVersion) throws APIMgtDAOException {
        final String query = "SELECT operationMapping.OPERATION_ID AS OPERATION_ID,operationMapping.HTTP_METHOD AS " +
                "HTTP_METHOD,operationMapping.URL_PATTERN AS URL_PATTERN,operationMapping.AUTH_SCHEME AS AUTH_SCHEME," +
                "operationMapping.API_POLICY_ID AS API_POLICY_ID FROM AM_API_OPERATION_MAPPING operationMapping," +
                "AM_API api WHERE operationMapping.API_ID = api.UUID AND api.CONTEXT = ? AND api.VERSION = ?";
        List<UriTemplate> uriTemplates = new ArrayList<>();
        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, apiContext);
            preparedStatement.setString(2, apiVersion);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    UriTemplate uriTemplate = new UriTemplate.UriTemplateBuilder()
                            .uriTemplate(resultSet.getString("URL_PATTERN")).authType(resultSet.getString
                                    ("AUTH_SCHEME"))
                            .httpVerb(resultSet.getString("HTTP_METHOD"))
                            .policy(new APIPolicy(resultSet.getString("API_POLICY_ID"), "")).templateId
                                    (resultSet.getString("OPERATION_ID")).build();
                    uriTemplates.add(uriTemplate);
                }
            }
        } catch (SQLException e) {
            String msg = "getting API resources for Context: " + apiContext + ", Version: " + apiVersion;
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + msg, e);
        }
        return uriTemplates;
    }

    /**
     * Get image of a given API
     *
     * @param apiID The UUID of the respective API
     * @return Image stream
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    @Override
    public InputStream getImage(String apiID) throws APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection()) {
            return ApiResourceDAO.getBinaryValueForCategory(connection, apiID,
                    ResourceCategory.IMAGE, ApiType.STANDARD);
        } catch (SQLException | IOException e) {
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + "getting Image for API: " + apiID, e);
        }
    }

    /**
     * @see ApiDAO#updateImage(String, InputStream, String, String)
     */
    @Override
    public void updateImage(String apiID, InputStream image, String dataType, String updatedBy)
            throws APIMgtDAOException {
        if (image != null) {
            try (Connection connection = DAOUtil.getConnection()) {
                try {
                    connection.setAutoCommit(false);
                    if (!ApiResourceDAO.isResourceExistsForCategory(connection, apiID,
                            ResourceCategory.IMAGE)) {
                        ApiResourceDAO.addBinaryResource(connection, apiID, UUID.randomUUID().toString(),
                                ResourceCategory.IMAGE, dataType, image, updatedBy);
                    } else {
                        ApiResourceDAO.updateBinaryResourceForCategory(connection, apiID,
                                ResourceCategory.IMAGE, image, updatedBy);
                    }
                    connection.commit();
                } catch (SQLException e) {
                    connection.rollback();
                    String msg = "updating Image for API: " + apiID + ", Data Type: " + dataType +
                            ", Updated By: " + updatedBy;
                    throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + msg, e);
                } finally {
                    connection.setAutoCommit(DAOUtil.isAutoCommit());
                }
            } catch (SQLException e) {
                String msg = "updating Image for API: " + apiID + ", Data Type: " + dataType +
                        ", Updated By: " + updatedBy;
                throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + msg, e);
            }
        }
    }

    /**
     * Change the lifecycle status of a given API
     *
     * @param apiID  The UUID of the respective API
     * @param status The lifecycle status that the API must be set to
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    @Override
    public void changeLifeCycleStatus(String apiID, String status) throws APIMgtDAOException {
        final String query = "UPDATE AM_API SET CURRENT_LC_STATUS = ?, LAST_UPDATED_TIME = ? WHERE UUID = ?";
        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            try {
                connection.setAutoCommit(false);
                statement.setString(1, status);
                statement.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                statement.setString(3, apiID);
                statement.execute();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                String msg = "changing Life Cycle Status for API: " + apiID + " to Status: " + status;
                throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + msg, e);
            } finally {
                connection.setAutoCommit(DAOUtil.isAutoCommit());
            }
        } catch (SQLException e) {
            String msg = "changing Life Cycle Status for API: " + apiID + " to Status: " + status;
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + msg, e);
        }
    }

    /**
     * Return list of all Document info belonging to a given API.
     *
     * @param apiID The UUID of the respective API
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    @Override
    public List<DocumentInfo> getDocumentsInfoList(String apiID) throws APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection()) {
            return DocMetaDataDAO.getDocumentInfoList(connection, apiID);
        } catch (SQLException e) {
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + "getting Documents Info list for API: " + apiID, e);
        }
    }

    /**
     * Return Document info object
     *
     * @param resourceID The UUID of the respective Document
     * @return {@link DocumentInfo} DocumentInfo meta data
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    @Override
    @CheckForNull
    public DocumentInfo getDocumentInfo(String resourceID) throws APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection()) {
            return DocMetaDataDAO.getDocumentInfo(connection, resourceID);
        } catch (SQLException e) {
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX +
                    "getting Document Info for Resource: " + resourceID, e);
        }
    }

    /**
     * @param resourceID The UUID of the respective Document
     * @return {@link InputStream} Document Info object
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    @Override
    @CheckForNull
    public InputStream getDocumentFileContent(String resourceID) throws APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection()) {
            return ApiResourceDAO.getBinaryResource(connection, resourceID);
        } catch (SQLException | IOException e) {
            String msg = "getting Document File Content for Resource: " + resourceID;
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + msg, e);
        }
    }

    /**
     * @param resourceID The UUID of the respective resource
     * @return {@link String} Document inline content
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    @Override
    public String getDocumentInlineContent(String resourceID) throws APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection()) {
            return ApiResourceDAO.getTextResource(connection, resourceID);
        } catch (SQLException e) {
            String msg = "getting Document Inline Content for Resource: " + resourceID;
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + msg, e);
        }
    }

    /**
     * Add artifact resource meta data to an API
     *
     * @param apiId        UUID of API
     * @param documentInfo {@link DocumentInfo}
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    @Override
    public void addDocumentInfo(String apiId, DocumentInfo documentInfo) throws APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection()) {
            try {
                connection.setAutoCommit(false);
                ApiResourceDAO.addResourceWithoutValue(connection, apiId, documentInfo.getId(), ResourceCategory.DOC);
                DocMetaDataDAO.addDocumentInfo(connection, documentInfo);

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                String msg = "adding Document Info for API: " + apiId + " , Document Name: " + documentInfo.getName();
                throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + msg, e);
            } finally {
                connection.setAutoCommit(DAOUtil.isAutoCommit());
            }
        } catch (SQLException e) {
            String msg = "adding Document Info for API: " + apiId + " , Document Name: " + documentInfo.getName();
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + msg, e);
        }
    }

    /**
     * Add artifact resource meta data to an API
     *
     * @param apiId        UUID of API
     * @param documentInfo {@link DocumentInfo}
     * @param updatedBy    user who performs the action
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    @Override
    public void updateDocumentInfo(String apiId, DocumentInfo documentInfo, String updatedBy)
            throws APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection()) {
            try {
                connection.setAutoCommit(false);
                DocMetaDataDAO.updateDocInfo(connection, documentInfo, updatedBy);

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                String msg = "updating Document Info for API: " + apiId + " , Document Name: "
                        + documentInfo.getName() + ", updated by: " + updatedBy;
                throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + msg, e);
            } finally {
                connection.setAutoCommit(DAOUtil.isAutoCommit());
            }
        } catch (SQLException e) {
            String msg = "updating Document Info for API: " + apiId + " , Document Name: "
                    + documentInfo.getName() + ", updated by: " + updatedBy;
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + msg, e);
        }
    }

    /**
     * @see ApiDAO#addDocumentFileContent(String, InputStream, String, String)
     */
    @Override
    public void addDocumentFileContent(String resourceID, InputStream content, String dataType, String updatedBy) throws
            APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection()) {
            try {
                connection.setAutoCommit(false);
                if (ApiResourceDAO.updateBinaryResource(connection, resourceID, content, dataType, updatedBy) == 0) {
                    String msg = "Cannot add file content for non existing document: " + resourceID + ", updated by: "
                            + updatedBy;
                    throw new APIMgtDAOException(msg, ExceptionCodes.DOCUMENT_NOT_FOUND);
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                String msg = "adding document file content for document: " + resourceID + ", updatedBy: " + updatedBy;
                throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + msg, e);
            } finally {
                connection.setAutoCommit(DAOUtil.isAutoCommit());
            }
        } catch (SQLException e) {
            String msg = "adding document file content for document: " + resourceID + ", updatedBy: " + updatedBy;
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + msg, e);
        }
    }

    /**
     * @see ApiDAO#addDocumentInlineContent(String, String, String)
     */
    @Override
    public void addDocumentInlineContent(String resourceID, String content, String updatedBy)
            throws APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection()) {
            try {
                connection.setAutoCommit(false);
                if (ApiResourceDAO.updateTextResource(connection, resourceID, content, updatedBy) == 0) {
                    throw new APIMgtDAOException("Cannot add inline content for non existing document: " + resourceID +
                            "",
                            ExceptionCodes.DOCUMENT_NOT_FOUND);
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                String msg = "adding document inline content for document: " + resourceID + ", updatedBy: " + updatedBy;
                throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + msg, e);
            } finally {
                connection.setAutoCommit(DAOUtil.isAutoCommit());
            }
        } catch (SQLException e) {
            String msg = "adding document inline content for document: " + resourceID + ", updatedBy: " + updatedBy;
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + msg, e);
        }
    }

    /**
     * Delete a resource
     *
     * @param resourceID UUID of resource
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    @Override
    public void deleteDocument(String resourceID) throws APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection()) {
            try {
                connection.setAutoCommit(false);
                ApiResourceDAO.deleteResource(connection, resourceID);
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + "deleting document: " + resourceID, e);
            } finally {
                connection.setAutoCommit(DAOUtil.isAutoCommit());
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + "deleting document: " + resourceID, e);
        }
    }

    /**
     * Used to deprecate older versions of the api
     *
     * @param identifier UUID of the API.
     */
    @Override
    public void deprecateOlderVersions(String identifier) {
        /**
         * todo:
         */
    }

    @Override
    public boolean isDocumentExist(String apiId, DocumentInfo documentInfo) throws APIMgtDAOException {
        final String query = "SELECT 1 FROM AM_API_DOC_META_DATA INNER JOIN AM_API_RESOURCES " +
                "ON AM_API_DOC_META_DATA.UUID=AM_API_RESOURCES.UUID WHERE AM_API_RESOURCES.API_ID = ? AND " +
                "AM_API_DOC_META_DATA.NAME=?";
        boolean exist = false;
        try (Connection connection = DAOUtil.getConnection(); PreparedStatement preparedStatement = connection
                .prepareStatement(query)) {
            preparedStatement.setString(1, apiId);
            preparedStatement.setString(2, documentInfo.getName());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            String msg = "checking Document exists for API: " + apiId + " , Document Name: " + documentInfo.getName();
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + msg, e);
        }
    }

    private API constructAPIFromResultSet(Connection connection, PreparedStatement statement) throws SQLException,
            IOException, APIMgtDAOException {
        try (ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                BusinessInformation businessInformation = new BusinessInformation();
                businessInformation.setTechnicalOwner(rs.getString("TECHNICAL_OWNER"));
                businessInformation.setTechnicalOwnerEmail(rs.getString("TECHNICAL_EMAIL"));
                businessInformation.setBusinessOwner(rs.getString("BUSINESS_OWNER"));
                businessInformation.setBusinessOwnerEmail(rs.getString("BUSINESS_EMAIL"));

                CorsConfiguration corsConfiguration = new CorsConfiguration();
                corsConfiguration.setEnabled(rs.getBoolean("CORS_ENABLED"));

                String allowOrigins = rs.getString("CORS_ALLOW_ORIGINS");
                corsConfiguration.setAllowOrigins(DAOUtil.commaSeperatedStringToList(allowOrigins));

                corsConfiguration.setAllowCredentials(rs.getBoolean("CORS_ALLOW_CREDENTIALS"));

                String allowHeaders = rs.getString("CORS_ALLOW_HEADERS");
                corsConfiguration.setAllowHeaders(DAOUtil.commaSeperatedStringToList(allowHeaders));

                String allowMethods = rs.getString("CORS_ALLOW_METHODS");
                corsConfiguration.setAllowMethods(DAOUtil.commaSeperatedStringToList(allowMethods));

                String apiPrimaryKey = rs.getString("UUID");

                return new API.APIBuilder(rs.getString("PROVIDER"), rs.getString("NAME"), rs.getString("VERSION")).
                        id(apiPrimaryKey).
                        context(rs.getString("CONTEXT")).
                        isDefaultVersion(rs.getBoolean("IS_DEFAULT_VERSION")).
                        description(rs.getString("DESCRIPTION")).
                        visibility(API.Visibility.valueOf(rs.getString("VISIBILITY"))).
                        visibleRoles(getVisibleRoles(connection, apiPrimaryKey)).
                        isResponseCachingEnabled(rs.getBoolean("IS_RESPONSE_CACHED")).
                        cacheTimeout(rs.getInt("CACHE_TIMEOUT")).
                        hasOwnGateway(rs.getBoolean("HAS_OWN_GATEWAY")).
                        tags(getTags(connection, apiPrimaryKey)).
                        gatewayLabels(getLabelNamesForAPI(connection, apiPrimaryKey, APIMgtConstants
                                .LABEL_TYPE_GATEWAY)).
                        storeLabels(getLabelNamesForAPI(connection, apiPrimaryKey, APIMgtConstants.LABEL_TYPE_STORE)).
                        wsdlUri(ApiResourceDAO.
                                getTextValueForCategory(connection, apiPrimaryKey,
                                        ResourceCategory.WSDL_TEXT)).
                        transport(getTransports(connection, apiPrimaryKey)).
                        endpoint(getEndPointsForApi(connection, apiPrimaryKey)).
                        apiPermission(getPermissionsStringForApi(connection, apiPrimaryKey)).
                        permissionMap(getPermissionMapForApi(connection, apiPrimaryKey)).
                        businessInformation(businessInformation).
                        lifecycleInstanceId(rs.getString("LIFECYCLE_INSTANCE_ID")).
                        lifeCycleStatus(rs.getString("CURRENT_LC_STATUS")).
                        corsConfiguration(corsConfiguration).
                        createdBy(rs.getString("CREATED_BY")).
                        updatedBy(rs.getString("UPDATED_BY")).
                        createdTime(rs.getTimestamp("CREATED_TIME").toInstant()).
                        lastUpdatedTime(rs.getTimestamp("LAST_UPDATED_TIME").toInstant()).
                        uriTemplates(getUriTemplates(connection, apiPrimaryKey)).
                        policies(getSubscripitonPolciesByAPIId(connection, apiPrimaryKey)).copiedFromApiId(rs.getString
                        ("COPIED_FROM_API")).
                        workflowStatus(rs.getString("LC_WORKFLOW_STATUS")).
                        securityScheme(rs.getInt("SECURITY_SCHEME")).
                        apiPolicy(getApiPolicyByAPIId(connection, apiPrimaryKey)).
                        threatProtectionPolicies(getThreatProtectionPolicies(connection, apiPrimaryKey)).
                        additionalProperties(getAdditionalProperties(connection, apiPrimaryKey)).build();
            }
        }

        return null;
    }

    private List<API> constructAPISummaryList(Connection connection, PreparedStatement statement) throws SQLException {
        List<API> apiList = new ArrayList<>();
        try (ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                String apiPrimaryKey = rs.getString("UUID");
                API apiSummary = new API.APIBuilder(rs.getString("PROVIDER"), rs.getString("NAME"),
                        rs.getString("VERSION")).
                        id(apiPrimaryKey).
                        context(rs.getString("CONTEXT")).
                        description(rs.getString("DESCRIPTION")).
                        lifeCycleStatus(rs.getString("CURRENT_LC_STATUS")).
                        lifecycleInstanceId(rs.getString("LIFECYCLE_INSTANCE_ID")).
                        workflowStatus(rs.getString("LC_WORKFLOW_STATUS")).
                        securityScheme(rs.getInt("SECURITY_SCHEME")).
                        threatProtectionPolicies(getThreatProtectionPolicies(connection, apiPrimaryKey)).build();

                apiList.add(apiSummary);
            }
        }

        return apiList;
    }

    private List<API> constructDetailAPIList(Connection connection, PreparedStatement statement)
            throws SQLException, APIMgtDAOException {
        List<API> apiList = new ArrayList<>();
        try (ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                String apiPrimaryKey = rs.getString("UUID");
                final String query = API_SELECT + " WHERE UUID = ? AND API_TYPE_ID = "
                        + "(SELECT TYPE_ID FROM AM_API_TYPES WHERE TYPE_NAME = ?)";

                try (PreparedStatement statement1 = connection.prepareStatement(query)) {
                    statement1.setString(1, apiPrimaryKey);
                    statement1.setString(2, ApiType.STANDARD.toString());

                    API api = constructAPIFromResultSet(connection, statement1);
                    apiList.add(api);
                } catch (SQLException | IOException e) {
                    //skip throwing error to make sure next api is fetched even one api fetching is failed.
                    log.error(DAOUtil.DAO_ERROR_PREFIX + "getting API with id: " + apiPrimaryKey, e);
                }
            }
        }
        return apiList;
    }

    private CompositeAPI getCompositeAPIFromResultSet(Connection connection, PreparedStatement statement)
            throws SQLException, IOException, APIMgtDAOException {
        try (ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {

                String apiPrimaryKey = rs.getString("UUID");

                return new CompositeAPI.Builder().
                        id(apiPrimaryKey).
                        provider(rs.getString("PROVIDER")).
                        name(rs.getString("NAME")).
                        version(rs.getString("VERSION")).
                        context(rs.getString("CONTEXT")).
                        description(rs.getString("DESCRIPTION")).
                        gatewayLabels(getLabelNamesForAPI(connection, apiPrimaryKey, APIMgtConstants
                                .LABEL_TYPE_GATEWAY)).
                        storeLabels(getLabelNamesForAPI(connection, apiPrimaryKey, APIMgtConstants.LABEL_TYPE_STORE)).
                        transport(getTransports(connection, apiPrimaryKey)).
                        applicationId(getCompositeAPIApplicationId(connection, apiPrimaryKey)).
                        createdBy(rs.getString("CREATED_BY")).
                        updatedBy(rs.getString("UPDATED_BY")).
                        createdTime(rs.getTimestamp("CREATED_TIME").toInstant()).
                        lastUpdatedTime(rs.getTimestamp("LAST_UPDATED_TIME").toInstant()).
                        uriTemplates(getUriTemplates(connection, apiPrimaryKey)).
                        copiedFromApiId(rs.getString("COPIED_FROM_API")).
                        workflowStatus(rs.getString("LC_WORKFLOW_STATUS")).
                        threatProtectionPolicies(getThreatProtectionPolicies(connection, apiPrimaryKey)).build();
            }
        }

        return null;
    }

    private List<CompositeAPI> getCompositeAPISummaryList(Connection connection, PreparedStatement statement)
            throws SQLException, APIMgtDAOException {
        List<CompositeAPI> apiList = new ArrayList<>();
        try (ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                String apiPrimaryKey = rs.getString("UUID");
                CompositeAPI apiSummary = new CompositeAPI.Builder().
                        id(apiPrimaryKey).
                        provider(rs.getString("PROVIDER")).
                        name(rs.getString("NAME")).
                        version(rs.getString("VERSION")).
                        context(rs.getString("CONTEXT")).
                        description(rs.getString("DESCRIPTION")).
                        applicationId(getCompositeAPIApplicationId(connection, apiPrimaryKey)).
                        workflowStatus(rs.getString("LC_WORKFLOW_STATUS")).
                        threatProtectionPolicies(getThreatProtectionPolicies(connection, apiPrimaryKey)).build();

                apiList.add(apiSummary);
            }
        }

        return apiList;
    }

    private String getCompositeAPIApplicationId(Connection connection, String apiId) throws APIMgtDAOException {
        APISubscriptionDAO apiSubscriptionDAO = new DAOFactory().getAPISubscriptionDAO();

        List<Subscription> subscriptions = apiSubscriptionDAO.getAPISubscriptionsByAPI(apiId);

        if (!subscriptions.isEmpty()) {
            return subscriptions.get(0).getApplication().getId();
        }

        throw new IllegalStateException("Composite API ID " + apiId + " has no associated Application subscription");
    }

    private void addTagsMapping(Connection connection, String apiID, Set<String> tags) throws SQLException {
        if (!tags.isEmpty()) {
            List<String> tagIDs = TagDAOImpl.addTagsIfNotExist(connection, tags);

            final String query = "INSERT INTO AM_API_TAG_MAPPING (API_ID, TAG_ID) VALUES (?, ?)";

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                for (String tagID : tagIDs) {
                    statement.setString(1, apiID);
                    statement.setString(2, tagID);
                    statement.addBatch();
                }

                statement.executeBatch();
            }
        }
    }

    private void deleteTagsMapping(Connection connection, String apiID) throws SQLException {
        final String query = "DELETE FROM AM_API_TAG_MAPPING WHERE API_ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, apiID);
            statement.execute();
        }
    }


    private Set<String> getTags(Connection connection, String apiID) throws SQLException {
        Set<String> tags = new HashSet<>();

        final String query = "SELECT TAG_ID FROM AM_API_TAG_MAPPING WHERE API_ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, apiID);
            statement.execute();

            try (ResultSet rs = statement.getResultSet()) {
                List<String> tagIDs = new ArrayList<>();

                while (rs.next()) {
                    tagIDs.add(rs.getString("TAG_ID"));
                }

                if (!tagIDs.isEmpty()) {
                    tags = TagDAOImpl.getTagsByIDs(connection, tagIDs);
                }
            }
        }

        return tags;
    }

    private void addVisibleRole(Connection connection, String apiID, Set<String> roles) throws SQLException {
        final String query = "INSERT INTO AM_API_VISIBLE_ROLES (API_ID, ROLE) VALUES (?,?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            for (String role : roles) {
                statement.setString(1, apiID);
                statement.setString(2, role);
                statement.addBatch();
            }

            statement.executeBatch();
        }
    }

    private void deleteVisibleRoles(Connection connection, String apiID) throws SQLException {
        final String query = "DELETE FROM AM_API_VISIBLE_ROLES WHERE API_ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, apiID);
            statement.execute();
        }
    }

    private Set<String> getVisibleRoles(Connection connection, String apiID) throws SQLException {
        Set<String> roles = new HashSet<>();

        final String query = "SELECT ROLE FROM AM_API_VISIBLE_ROLES WHERE API_ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, apiID);
            statement.execute();

            try (ResultSet rs = statement.getResultSet()) {
                while (rs.next()) {
                    roles.add(rs.getString("ROLE"));
                }
            }
        }

        return roles;
    }

    private void addAPIDefinition(Connection connection, String apiID, String apiDefinition, String addedBy)
            throws SQLException {
        if (!apiDefinition.isEmpty()) {
            ApiResourceDAO.addBinaryResource(connection, apiID, UUID.randomUUID().toString(), ResourceCategory.SWAGGER,
                    MediaType.APPLICATION_JSON,
                    new ByteArrayInputStream(apiDefinition.getBytes(StandardCharsets.UTF_8)), addedBy);
        }
    }

    private void updateAPIDefinition(Connection connection, String apiID, String apiDefinition, String updatedBy)
            throws SQLException {
        ApiResourceDAO.updateBinaryResourceForCategory(connection, apiID, ResourceCategory.SWAGGER,
                new ByteArrayInputStream(apiDefinition.getBytes(StandardCharsets.UTF_8)), updatedBy);
    }

    private String getAPIDefinition(Connection connection, String apiID) throws SQLException, IOException {
        InputStream apiDefinition = ApiResourceDAO.getBinaryValueForCategory(connection, apiID,
                ResourceCategory.SWAGGER, ApiType.STANDARD);

        return IOUtils.toString(apiDefinition, StandardCharsets.UTF_8);
    }

    private void addGatewayConfig(Connection connection, String apiID, String gatewayConfig, String addedBy)
            throws SQLException {
        if (gatewayConfig != null && !gatewayConfig.isEmpty()) {
            ApiResourceDAO
                    .addBinaryResource(connection, apiID, UUID.randomUUID().toString(), ResourceCategory.GATEWAY_CONFIG,
                            MediaType.APPLICATION_JSON,
                            new ByteArrayInputStream(gatewayConfig.getBytes(StandardCharsets.UTF_8)), addedBy);
        }
    }

    private String getGatewayConfig(Connection connection, String apiID) throws SQLException, IOException {
        InputStream gatewayConfig = ApiResourceDAO
                .getBinaryValueForCategory(connection, apiID, ResourceCategory.GATEWAY_CONFIG, ApiType.STANDARD);

        if (gatewayConfig != null) {
            return IOUtils.toString(gatewayConfig, StandardCharsets.UTF_8);
        }
        return null;
    }

    private void updateGatewayConfig(Connection connection, String apiID, String gatewayConfig, String updatedBy)
            throws SQLException {
        if (gatewayConfig != null && !gatewayConfig.isEmpty()) {
            ApiResourceDAO.updateBinaryResourceForCategory(connection, apiID, ResourceCategory.GATEWAY_CONFIG,
                    new ByteArrayInputStream(gatewayConfig.getBytes(StandardCharsets.UTF_8)), updatedBy);
        }
    }

    private void addTransports(Connection connection, String apiID, Set<String> transports) throws SQLException {
        final String query = "INSERT INTO AM_API_TRANSPORTS (API_ID, TRANSPORT) VALUES (?,?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            for (String transport : transports) {
                statement.setString(1, apiID);
                statement.setString(2, transport);

                statement.addBatch();
            }
            statement.executeBatch();

        }
    }

    /**
     * Associate a list of threat protection policy ids with an API
     *
     * @param connection SQL Connection
     * @param apiId      ApiId of the API
     * @param policies   Set of threat protection policies
     * @throws SQLException If failed to associate policies
     */
    private void addThreatProtectionPolicies(Connection connection, String apiId, Set<String> policies)
            throws SQLException {
        final String query = "INSERT INTO AM_THREAT_PROTECTION_MAPPING (API_ID, POLICY_ID) VALUES(?,?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            for (String policy : policies) {
                statement.setString(1, apiId);
                statement.setString(2, policy);
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }


    private void deleteAPIPermission(Connection connection, String apiID) throws SQLException {
        final String query = "DELETE FROM AM_API_GROUP_PERMISSION WHERE API_ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, apiID);
            statement.execute();
        }
    }

    /**
     * Adding API permission to database
     *
     * @param connection    connection to database
     * @param permissionMap permission map
     * @param apiId         id of the API
     * @throws SQLException if error occurred when adding API permission to database
     */
    private void addAPIPermission(Connection connection, Map permissionMap, String apiId) throws SQLException {
        final String query = "INSERT INTO AM_API_GROUP_PERMISSION (API_ID, GROUP_ID, PERMISSION) VALUES (?, ?, ?)";
        Map<String, Integer> map = permissionMap;
        if (permissionMap != null) {
            if (permissionMap.size() > 0) {
                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    for (Map.Entry<String, Integer> entry : map.entrySet()) {
                        statement.setString(1, apiId);
                        statement.setString(2, entry.getKey());
                        Integer permissionValue = entry.getValue();
                        //if permission value is UPDATE, DELETE or MANAGE_SUBSCRIPTION_PERMISSION we by default give
                        // them read permission also. Have used the bitwise AND operation to check whether the
                        // permission value passed by the user contains the read permission.
                        if (permissionValue > APIMgtConstants.Permission.READ_PERMISSION
                                && (permissionValue & APIMgtConstants.Permission.READ_PERMISSION) == 0) {
                            statement.setInt(3, permissionValue + APIMgtConstants.Permission.READ_PERMISSION);
                        } else {
                            statement.setInt(3, permissionValue);
                        }
                        statement.addBatch();
                    }
                    statement.executeBatch();
                }
            }
        }
    }

    /**
     * Update API permission
     *
     * @param connection    connection to database
     * @param permissionMap updated permission map
     * @param apiId         id of API to be updated permission
     * @throws SQLException if error occurred when updating api permission
     */
    private void updateApiPermission(Connection connection, Map permissionMap, String apiId) throws SQLException {
        final String query = "INSERT INTO AM_API_GROUP_PERMISSION (API_ID, GROUP_ID, PERMISSION) VALUES (?, ?, ?)";
        Map<String, Integer> map = permissionMap;
        if (permissionMap != null) {
            if (permissionMap.size() > 0) {
                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    for (Map.Entry<String, Integer> entry : map.entrySet()) {
                        statement.setString(1, apiId);
                        statement.setString(2, entry.getKey());
                        Integer permissionValue = entry.getValue();
                        //if permission value is UPDATE, DELETE or MANAGE_SUBSCRIPTION_PERMISSION we by default give
                        // them read permission also. Have used the bitwise AND operation to check whether the
                        // permission value passed by the user contains the read permission.
                        if (permissionValue > APIMgtConstants.Permission.READ_PERMISSION
                                && (permissionValue & APIMgtConstants.Permission.READ_PERMISSION) == 0) {
                            statement.setInt(3, permissionValue + APIMgtConstants.Permission.READ_PERMISSION);
                        } else {
                            statement.setInt(3, permissionValue);
                        }
                        statement.addBatch();
                    }
                    statement.executeBatch();
                }
            }
        }
    }

    private void deleteTransports(Connection connection, String apiID) throws SQLException {
        final String query = "DELETE FROM AM_API_TRANSPORTS WHERE API_ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, apiID);
            statement.execute();
        }
    }

    /**
     * Delete threat protection policies from an API
     *
     * @param connection SQL Connection
     * @param apiId      ApiId of the API
     * @throws SQLException If failed to delete policies
     */
    private void deleteThreatProtectionPolicies(Connection connection, String apiId) throws SQLException {
        final String query = "DELETE FROM AM_THREAT_PROTECTION_MAPPING WHERE API_ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, apiId);
            statement.execute();
        }
    }


    private void addUrlMappings(Connection connection, Collection<UriTemplate> uriTemplates, String apiID)
            throws SQLException {
        final String query = "INSERT INTO AM_API_OPERATION_MAPPING (OPERATION_ID,API_ID, HTTP_METHOD, URL_PATTERN, "
                + "AUTH_SCHEME, API_POLICY_ID) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            for (UriTemplate uriTemplate : uriTemplates) {
                statement.setString(1, uriTemplate.getTemplateId());
                statement.setString(2, apiID);
                statement.setString(3, uriTemplate.getHttpVerb());
                statement.setString(4, uriTemplate.getUriTemplate());
                statement.setString(5, uriTemplate.getAuthType());
                statement.setString(6, uriTemplate.getPolicy().getUuid());
                statement.addBatch();
            }
            statement.executeBatch();
            for (UriTemplate uriTemplate : uriTemplates) {
                addEndPointsForOperation(connection, apiID, uriTemplate.getTemplateId(), uriTemplate.getEndpoint());
            }
        }
    }

    private void deleteUrlMappings(Connection connection, String apiID) throws
            SQLException {
        final String query = "DELETE FROM AM_API_OPERATION_MAPPING WHERE API_ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, apiID);
            statement.execute();
        }
    }

    private Map<String, UriTemplate> getUriTemplates(Connection connection, String apiId) throws SQLException,
            IOException {
        final String query = "SELECT operationMapping.OPERATION_ID AS OPERATION_ID,operationMapping.API_ID AS API_ID," +
                "operationMapping.HTTP_METHOD AS HTTP_METHOD,operationMapping.URL_PATTERN AS URL_PATTERN," +
                "operationMapping.AUTH_SCHEME AS AUTH_SCHEME,operationMapping.API_POLICY_ID AS API_POLICY_ID FROM " +
                "AM_API_OPERATION_MAPPING operationMapping WHERE operationMapping.API_ID = ?";
        Map<String, UriTemplate> uriTemplateSet = new HashMap();
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, apiId);
            statement.execute();
            try (ResultSet rs = statement.getResultSet()) {
                while (rs.next()) {
                    UriTemplate.UriTemplateBuilder uriTemplateBuilder = new UriTemplate.UriTemplateBuilder()
                            .uriTemplate(rs.getString("URL_PATTERN")).authType(rs.getString("AUTH_SCHEME"))
                            .httpVerb(rs.getString("HTTP_METHOD")).templateId(rs.getString("OPERATION_ID")).
                                    endpoint(getEndPointsForOperation(connection, apiId, rs.getString("OPERATION_ID")
                                    ));
                    String apiPolicyId = rs.getString("API_POLICY_ID");
                    if (StringUtils.isNotEmpty(apiPolicyId)) {
                        uriTemplateBuilder.policy(getApiPolicyByUuid(connection, apiPolicyId));
                    }
                    uriTemplateSet.put(uriTemplateBuilder.build().getTemplateId(), uriTemplateBuilder.build());
                }
            }
        }
        return uriTemplateSet;
    }

    private Policy getApiPolicyByUuid(Connection connection, String uuid) throws SQLException {

        String sqlQuery = "SELECT NAME from AM_API_POLICY WHERE UUID = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
            preparedStatement.setString(1, uuid);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    APIPolicy apiPolicy = new APIPolicy(resultSet
                            .getString(APIMgtConstants.ThrottlePolicyConstants.COLUMN_NAME));
                    return apiPolicy;
                }
            }
        }
        return null;
    }

    private void addSubscriptionPolicies(Connection connection, Set<Policy> policies, String apiID)
            throws SQLException {
        final String query =
                "INSERT INTO AM_API_SUBS_POLICY_MAPPING (API_ID, SUBSCRIPTION_POLICY_ID) " + "VALUES (?,?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            for (Policy policy : policies) {
                statement.setString(1, apiID);

                statement.setString(2, getSubscriptionPolicyUUIDByName(connection, policy.getPolicyName()));
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private String getSubscriptionPolicyUUIDByName(Connection connection, String policyName) throws SQLException {
        final String query = "SELECT UUID FROM AM_SUBSCRIPTION_POLICY WHERE NAME = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, policyName);
            statement.execute();
            try (ResultSet rs = statement.getResultSet()) {
                if (rs.next()) {
                    return rs.getString("UUID");
                } else {
                    return null;
                }
            }
        }
    }

    private void addApiPolicy(Connection connection, String apiPolicy, String apiID)
            throws SQLException {
        final String query =
                "INSERT INTO AM_API_POLICY_MAPPING (API_ID, API_POLICY_ID) " + "VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, apiID);
            statement.setString(2, apiPolicy);
            statement.execute();
        }
    }

    private void deleteApiPolicy(Connection connection, String apiID) throws SQLException {
        final String query = "DELETE FROM AM_API_POLICY_MAPPING WHERE API_ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, apiID);
            statement.execute();
        }
    }

    private Policy getApiPolicyByAPIId(Connection connection, String apiId) throws SQLException {
        final String query = "SELECT amapipolicy.NAME AS POLICY_NAME FROM AM_API_POLICY_MAPPING apimpolicymapping," +
                "AM_API_POLICY amapipolicy WHERE apimpolicymapping.API_POLICY_ID=amapipolicy.UUID " +
                "AND apimpolicymapping.API_ID = ?";
        Policy apiPolicy = null;
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, apiId);
            statement.execute();

            try (ResultSet rs = statement.getResultSet()) {
                if (rs.next()) {
                    apiPolicy = new APIPolicy(rs.getString("POLICY_NAME"));
                }
            }
        }
        return apiPolicy;
    }

    private void deleteSubscriptionPolicies(Connection connection, String apiID) throws SQLException {
        final String query = "DELETE FROM AM_API_SUBS_POLICY_MAPPING WHERE API_ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, apiID);
            statement.execute();
        }
    }

    private Set<Policy> getSubscripitonPolciesByAPIId(Connection connection, String apiId) throws SQLException {
        final String query = "SELECT amPolcySub.NAME FROM AM_API_SUBS_POLICY_MAPPING apimsubmapping," +
                "AM_SUBSCRIPTION_POLICY amPolcySub where apimsubmapping.SUBSCRIPTION_POLICY_ID=amPolcySub.UUID " +
                "AND apimsubmapping.API_ID = ?";
        Set<Policy> policies = new HashSet<>();
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, apiId);
            statement.execute();

            try (ResultSet rs = statement.getResultSet()) {
                while (rs.next()) {
                    policies.add(new SubscriptionPolicy(rs.getString("NAME")));
                }
            }
        }

        return policies;
    }

    private void addAdditionalProperties(Connection connection, List<AdditionalProperties> additionalProperty,
                                         String apiID) throws SQLException {
        final String query =
                "INSERT INTO AM_API_ADDITIONAL_PROPERTIES(UUID, PROPERTY_NAME, PROPERTY_VALUE) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {

            for (int i = 0; i < additionalProperty.size(); i++) {
                statement.setString(1, apiID);
                statement.setString(2, (additionalProperty.get(i)).getPropertyName());
                statement.setString(3, (additionalProperty.get(i)).getPropertyValue());
                statement.execute();
            }

        }
    }

    private List<AdditionalProperties> getAdditionalProperties(Connection connection, String apiID)
            throws SQLException {

        final String query = "SELECT PROPERTY_NAME,PROPERTY_VALUE  FROM AM_API_ADDITIONAL_PROPERTIES" +
                             " WHERE UUID = ?";
        List<AdditionalProperties> additionalProperties = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, apiID);
            statement.execute();
            try (ResultSet rs = statement.getResultSet()) {
                while (rs.next()) {
                    additionalProperties.add(new AdditionalProperties(rs.getString("PROPERTY_NAME"),
                            rs.getString("PROPERTY_VALUE")));
                }
            }
        }
        return  additionalProperties;
    }

    private boolean checkTableColumnExists(DatabaseMetaData databaseMetaData, String tableName, String columnName)
            throws SQLException {
        try (ResultSet rs = databaseMetaData.getColumns(null, null, tableName, columnName)) {
            return rs.next();
        }
    }

    private int getApiTypeId(Connection connection, ApiType apiType) throws SQLException {
        final String query = "SELECT TYPE_ID FROM AM_API_TYPES WHERE TYPE_NAME = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, apiType.toString());
            statement.execute();

            try (ResultSet rs = statement.getResultSet()) {
                if (rs.next()) {
                    return rs.getInt("TYPE_ID");
                }

                throw new IllegalStateException("API Type " + apiType.toString() + " does not exist");
            }
        }
    }

    static void initResourceCategories() throws APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection()) {
            try {
                if (!ResourceCategoryDAO.isStandardResourceCategoriesExist(connection)) {
                    connection.setAutoCommit(false);
                    ResourceCategoryDAO.addResourceCategories(connection);
                    connection.commit();
                }
            } catch (SQLException e) {
                connection.rollback();
                throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + "adding API resource categories", e);
            } finally {
                connection.setAutoCommit(DAOUtil.isAutoCommit());
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + "adding API resource categories", e);
        }
    }

    static void initApiTypes() throws APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection()) {
            try {
                if (!isApiTypesExist(connection)) {
                    connection.setAutoCommit(false);
                    addApiTypes(connection);
                    connection.commit();
                }
            } catch (SQLException e) {
                connection.rollback();
                throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + "adding API types", e);
            } finally {
                connection.setAutoCommit(DAOUtil.isAutoCommit());
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + "adding API types", e);
        }
    }

    /**
     * Add an Endpoint
     *
     * @param endpoint Endpoint object.
     * @throws APIMgtDAOException If failed to add endpoint.
     */
    @Override
    public void addEndpoint(Endpoint endpoint) throws APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection()) {
            connection.setAutoCommit(false);
            try {
                addEndpoint(connection, endpoint);
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + "adding Endpoint: " + endpoint.getName(), e);
            } finally {
                connection.setAutoCommit(DAOUtil.isAutoCommit());
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + "adding Endpoint: " + endpoint.getName(), e);
        }
    }

    private void addEndpoint(Connection connection, Endpoint endpoint) throws SQLException {
        final String query = "INSERT INTO AM_ENDPOINT (UUID,NAME,ENDPOINT_CONFIGURATION,"
                + "TPS,TYPE,SECURITY_CONFIGURATION,APPLICABLE_LEVEL,GATEWAY_CONFIG) VALUES (?,?,?,?,?,?,?,?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, endpoint.getId());
            statement.setString(2, endpoint.getName());
            InputStream byteArrayInputStream = IOUtils.toInputStream(endpoint.getEndpointConfig());
            statement.setBinaryStream(3, byteArrayInputStream);
            if (endpoint.getMaxTps() != null) {
                statement.setLong(4, endpoint.getMaxTps());
            } else {
                statement.setNull(4, Types.INTEGER);
            }
            statement.setString(5, endpoint.getType());
            statement.setBinaryStream(6, IOUtils.toInputStream(endpoint.getSecurity()));
            statement.setString(7, endpoint.getApplicableLevel());
            statement.setBinaryStream(8, IOUtils.toInputStream(endpoint.getConfig()));
            statement.execute();
        }
    }

    private boolean isEndpointExist(Connection connection, String endpointName) throws SQLException {
        final String query = "SELECT 1 FROM AM_ENDPOINT WHERE NAME = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, endpointName);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    /**
     * Delete an Endpoint
     *
     * @param endpointId UUID of the endpoint.
     * @return Success of the delete operation.
     * @throws APIMgtDAOException If failed to delete endpoint.
     */
    @Override
    public boolean deleteEndpoint(String endpointId) throws APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection()) {
            try {
                connection.setAutoCommit(false);
                deleteEndpoint(connection, endpointId);
                connection.commit();
                return true;
            } catch (SQLException e) {
                connection.rollback();
                throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + "deleting Endpoint: " + endpointId, e);
            } finally {
                connection.setAutoCommit(DAOUtil.isAutoCommit());
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + "deleting Endpoint: " + endpointId, e);
        }
    }

    private void deleteEndpoint(Connection connection, String endpointId) throws SQLException {
        final String query = "DELETE FROM AM_ENDPOINT WHERE UUID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, endpointId);
            statement.execute();
        }
    }

    /**
     * @see org.wso2.carbon.apimgt.core.dao.ApiDAO#isEndpointAssociated(String)
     */
    @Override
    public boolean isEndpointAssociated(String endpointId) throws APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection()) {
            return isEndpointAssociated(connection, endpointId);
        } catch (SQLException e) {
            String msg = "checking existence of endpoint usage for " + endpointId;
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + msg, e);
        }
    }

    private boolean isEndpointAssociated(Connection connection, String endpointId) throws SQLException {
        final String apiLevelQuery = "SELECT 1 FROM AM_API_ENDPOINT_MAPPING WHERE ENDPOINT_ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(apiLevelQuery)) {
            statement.setString(1, endpointId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() || isEndpointAssociatedToOperation(connection, endpointId);
            }
        }
    }

    @Override
    @SuppressFBWarnings("SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING")
    public List<API> getAPIsByStatus(List<String> gatewayLabels, String status) throws APIMgtDAOException {
        final String query = "SELECT DISTINCT UUID, PROVIDER, A.NAME, CONTEXT, VERSION, DESCRIPTION, CURRENT_LC_STATUS,"
                + " LIFECYCLE_INSTANCE_ID, LC_WORKFLOW_STATUS, SECURITY_SCHEME FROM AM_API A INNER JOIN " +
                " AM_API_LABEL_MAPPING M ON A.UUID"
                + " = M.API_ID INNER JOIN AM_LABELS L ON L.LABEL_ID = M.LABEL_ID  WHERE L.TYPE_NAME='GATEWAY' " +
                "AND L.NAME" + " " + "IN" + " " + "(" + DAOUtil.getParameterString(gatewayLabels.size()) + ") AND A" +
                ".CURRENT_LC_STATUS=?";

        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            int i = 0;
            for (String label : gatewayLabels) {
                statement.setString(++i, label);
            }
            statement.setString(++i, status);
            return constructAPISummaryList(connection, statement);
        } catch (SQLException e) {
            String msg = "getting APIs for given gateway labels: " + gatewayLabels.toString() +
                    " with status: " + status;
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + msg, e);
        }
    }

    @Override
    @SuppressFBWarnings("SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING")
    public List<API> getAPIsByGatewayLabel(List<String> gatewayLabels) throws APIMgtDAOException {
        final String query = "SELECT DISTINCT UUID, PROVIDER, A.NAME, CONTEXT, VERSION, DESCRIPTION, CURRENT_LC_STATUS,"
                + " LIFECYCLE_INSTANCE_ID, LC_WORKFLOW_STATUS, SECURITY_SCHEME FROM AM_API A INNER JOIN " +
                "AM_API_LABEL_MAPPING M ON A.UUID"
                + " = M.API_ID INNER JOIN AM_LABELS L ON L.LABEL_ID = M.LABEL_ID WHERE L.TYPE_NAME = 'GATEWAY' AND" +
                " L.NAME IN (" + DAOUtil.getParameterString(gatewayLabels.size()) + ")";

        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            int i = 0;
            for (String label : gatewayLabels) {
                statement.setString(++i, label);
            }
            return constructAPISummaryList(connection, statement);
        } catch (SQLException e) {
            String msg = "searching APIs getting APIs for given gateway labels: " + gatewayLabels.toString();
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + msg, e);
        }
    }

    private boolean isEndpointAssociatedToOperation(Connection connection, String endpointId) throws SQLException {
        final String query = "Select 1 FROM AM_API_RESOURCE_ENDPOINT WHERE ENDPOINT_ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, endpointId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    /**
     * Update an Endpoint
     *
     * @param endpoint Endpoint Object.
     * @return Success of the update operation.
     * @throws APIMgtDAOException If failed to update endpoint.
     */
    @Override
    public boolean updateEndpoint(Endpoint endpoint) throws APIMgtDAOException {
        final String query = "UPDATE AM_ENDPOINT SET ENDPOINT_CONFIGURATION = ?,TPS = ?,TYPE = " +
                "?,SECURITY_CONFIGURATION =?, LAST_UPDATED_TIME = ?, GATEWAY_CONFIG = ? WHERE UUID = ?";
        try (Connection connection = DAOUtil.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                InputStream byteArrayInputStream = IOUtils.toInputStream(endpoint.getEndpointConfig());
                statement.setBinaryStream(1, byteArrayInputStream);
                if (endpoint.getMaxTps() != null) {
                    statement.setLong(2, endpoint.getMaxTps());
                } else {
                    statement.setNull(2, Types.INTEGER);
                }
                statement.setString(3, endpoint.getType());
                statement.setBinaryStream(4, IOUtils.toInputStream(endpoint.getSecurity()));
                statement.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
                statement.setBinaryStream(6, IOUtils.toInputStream(endpoint.getConfig()));
                statement.setString(7, endpoint.getId());
                statement.execute();
                connection.commit();
                return true;
            } catch (SQLException e) {
                connection.rollback();
                throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + "updating Endpoint: " + endpoint.getName(), e);
            } finally {
                connection.setAutoCommit(DAOUtil.isAutoCommit());
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + "updating Endpoint: " + endpoint.getName(), e);
        }
    }

    /**
     * Get an Endpoint
     *
     * @param endpointId uuid of endpoint
     * @return Endpoint object.
     * @throws APIMgtDAOException If failed to retrieve endpoint.
     */
    @Override
    public Endpoint getEndpoint(String endpointId) throws APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection()) {
            return getEndpoint(connection, endpointId);
        } catch (SQLException | IOException e) {
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + "getting Endpoint: " + endpointId, e);
        }
    }


    private Endpoint getEndpoint(Connection connection, String endpointId) throws SQLException, IOException,
            APIMgtDAOException {
        final String query = "SELECT UUID,NAME,ENDPOINT_CONFIGURATION,TPS,TYPE,"
                + "SECURITY_CONFIGURATION,APPLICABLE_LEVEL FROM AM_ENDPOINT WHERE UUID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, endpointId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return constructEndPointDetails(resultSet);
                }
            }
        }

        return null;
    }

    private Endpoint constructEndPointDetails(ResultSet resultSet) throws SQLException, IOException {
        Endpoint.Builder endpointBuilder = new Endpoint.Builder();
        endpointBuilder.id(resultSet.getString("UUID"));
        endpointBuilder.name(resultSet.getString("NAME"));
        endpointBuilder.endpointConfig(IOUtils.toString(resultSet.getBinaryStream
                ("ENDPOINT_CONFIGURATION")));
        endpointBuilder.maxTps(resultSet.getLong("TPS"));
        endpointBuilder.type(resultSet.getString("TYPE"));
        endpointBuilder.security(IOUtils.toString(resultSet.getBinaryStream("SECURITY_CONFIGURATION")));
        endpointBuilder.applicableLevel(resultSet.getString("APPLICABLE_LEVEL"));
        return endpointBuilder.build();
    }

    /**
     * Get an Endpoint
     *
     * @param name name of endpoint
     * @return Endpoint object.
     * @throws APIMgtDAOException If failed to retrieve endpoint.
     */
    @Override
    public Endpoint getEndpointByName(String name) throws APIMgtDAOException {
        final String query = "SELECT UUID,NAME,ENDPOINT_CONFIGURATION,TPS,TYPE,"
                + "SECURITY_CONFIGURATION,APPLICABLE_LEVEL FROM AM_ENDPOINT WHERE name = ?";
        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, name);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Endpoint.Builder endpointBuilder = new Endpoint.Builder();
                    endpointBuilder.id(resultSet.getString("UUID"));
                    endpointBuilder.name(resultSet.getString("NAME"));
                    endpointBuilder
                            .endpointConfig(IOUtils.toString(resultSet.getBinaryStream("ENDPOINT_CONFIGURATION")));
                    endpointBuilder.maxTps(resultSet.getLong("TPS"));
                    endpointBuilder.type(resultSet.getString("TYPE"));
                    endpointBuilder.security(IOUtils.toString(resultSet.getBinaryStream("SECURITY_CONFIGURATION")));
                    endpointBuilder.applicableLevel(resultSet.getString("APPLICABLE_LEVEL"));
                    return endpointBuilder.build();
                } else {
                    return null;
                }
            }
        } catch (SQLException | IOException e) {
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + "getting Endpoint: " + name, e);
        }
    }

    /**
     * get all Endpoints
     *
     * @return List of endpoints.
     * @throws APIMgtDAOException If failed to retrieve endpoints.
     */
    @Override
    public List<Endpoint> getEndpoints() throws APIMgtDAOException {
        final String query = "SELECT UUID,NAME,ENDPOINT_CONFIGURATION,TPS,TYPE,SECURITY_CONFIGURATION," +
                "APPLICABLE_LEVEL FROM AM_ENDPOINT WHERE APPLICABLE_LEVEL='" + APIMgtConstants.GLOBAL_ENDPOINT + "'";
        List<Endpoint> endpointList = new ArrayList<>();
        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                endpointList.add(constructEndPointDetails(resultSet));
            }
        } catch (SQLException | IOException e) {
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + "getting Endpoints", e);
        }
        return endpointList;
    }

    /**
     * This returns the json string containing the role permissions for a given API
     *
     * @param connection - DB connection
     * @param apiId      - apiId of the API
     * @return permission string
     * @throws SQLException - if error occurred while getting permissionMap of API from DB
     */
    private String getPermissionsStringForApi(Connection connection, String apiId) throws SQLException {
        JSONArray permissionArray = new JSONArray();
        Map<String, Integer> permissionMap = getPermissionMapForApi(connection, apiId);
        for (Map.Entry<String, Integer> entry : permissionMap.entrySet()) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(APIMgtConstants.Permission.GROUP_ID, entry.getKey());
            jsonObject.put(APIMgtConstants.Permission.PERMISSION,
                    APIUtils.constructApiPermissionsListForValue(entry.getValue()));
            permissionArray.add(jsonObject);
        }
        if (!permissionArray.isEmpty()) {
            return permissionArray.toString();
        } else {
            return "";
        }
    }

    /**
     * This constructs and returns the API permissions map from the DB
     *
     * @param connection - DB connection
     * @param apiId      - apiId of the API
     * @return permission map for the API
     * @throws SQLException - if error occurred while getting permissionMap of API from DB
     */
    private Map<String, Integer> getPermissionMapForApi(Connection connection, String apiId) throws SQLException {
        Map<String, Integer> permissionMap = new HashMap();
        final String query = "SELECT GROUP_ID,PERMISSION FROM AM_API_GROUP_PERMISSION WHERE API_ID=?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, apiId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    permissionMap.put(resultSet.getString("GROUP_ID"), resultSet.getInt("PERMISSION"));
                }
            }
        }
        return permissionMap;
    }

    private Map<String, Endpoint> getEndPointsForApi(Connection connection, String apiId) throws SQLException,
            IOException {
        Map<String, Endpoint> endpointMap = new HashMap();
        final String query = "SELECT AM_ENDPOINT.UUID,AM_ENDPOINT.NAME,AM_ENDPOINT.SECURITY_CONFIGURATION,AM_ENDPOINT" +
                ".APPLICABLE_LEVEL,AM_ENDPOINT.ENDPOINT_CONFIGURATION,AM_ENDPOINT.TPS,AM_ENDPOINT.TYPE," +
                "AM_API_ENDPOINT_MAPPING.TYPE AS ENDPOINT_LEVEL FROM AM_API_ENDPOINT_MAPPING INNER JOIN AM_ENDPOINT " +
                "ON AM_API_ENDPOINT_MAPPING.ENDPOINT_ID=AM_ENDPOINT.UUID WHERE AM_API_ENDPOINT_MAPPING.API_ID = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, apiId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Endpoint endpoint = constructEndPointDetails(resultSet);
                    if (APIMgtConstants.GLOBAL_ENDPOINT.equals(endpoint.getApplicableLevel())) {
                        endpointMap.put(resultSet.getString("ENDPOINT_LEVEL"), new Endpoint.Builder().
                                id(endpoint.getId()).applicableLevel(APIMgtConstants.GLOBAL_ENDPOINT).build());
                    } else {
                        endpointMap.put(resultSet.getString("ENDPOINT_LEVEL"), endpoint);
                    }
                }
            }
        }
        return endpointMap;
    }

    private void addEndPointsForApi(Connection connection, String apiId, Map<String, Endpoint> endpointMap) throws
            SQLException {
        final String query = "INSERT INTO AM_API_ENDPOINT_MAPPING (API_ID,TYPE,ENDPOINT_ID) VALUES (?,?,?)";
        if (endpointMap != null && !endpointMap.isEmpty()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                for (Map.Entry<String, Endpoint> entry : endpointMap.entrySet()) {
                    String endpointId;
                    Endpoint endpoint = entry.getValue();
                    if (APIMgtConstants.API_SPECIFIC_ENDPOINT.equals(endpoint.getApplicableLevel())) {
                        if (!isEndpointExist(connection, endpoint.getName())) {
                            addEndpoint(connection, endpoint);
                        }
                    }
                    endpointId = endpoint.getId();
                    preparedStatement.setString(1, apiId);
                    preparedStatement.setString(2, entry.getKey());
                    preparedStatement.setString(3, endpointId);
                    preparedStatement.addBatch();
                }
                preparedStatement.executeBatch();
            }
        }
    }

    private void deleteEndPointsForApi(Connection connection, String apiId) throws SQLException, IOException {
        final String query = "DELETE FROM AM_API_ENDPOINT_MAPPING WHERE API_ID = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, apiId);
            preparedStatement.execute();
        }

        Map<String, Endpoint> apiEndPoints = getEndPointsForApi(connection, apiId);
        for (Map.Entry<String, Endpoint> apiEndpoint : apiEndPoints.entrySet()) {
            if (APIMgtConstants.API_SPECIFIC_ENDPOINT.equals(apiEndpoint.getValue().getApplicableLevel())) {
                if (!isEndpointAssociated(connection, apiEndpoint.getKey())) {
                    deleteEndpoint(connection, apiEndpoint.getValue().getId());
                }
            }
        }
    }

    private void deleteEndPointsForOperation(Connection connection, String apiId) throws SQLException, IOException {
        final String query = "DELETE FROM AM_API_RESOURCE_ENDPOINT WHERE API_ID = ?";
        Set<String> endpoints = new HashSet<>();

        Map<String, UriTemplate> uriTemplates = getUriTemplates(connection, apiId);

        for (Map.Entry<String, UriTemplate> uriTemplate : uriTemplates.entrySet()) {
            Map<String, Endpoint> apiEndPoints = getEndPointsForOperation(connection, apiId,
                    uriTemplate.getValue().getTemplateId());

            for (Map.Entry<String, Endpoint> apiEndpoint : apiEndPoints.entrySet()) {
                Endpoint endpoint = apiEndpoint.getValue();
                if (APIMgtConstants.API_SPECIFIC_ENDPOINT.equals(endpoint.getApplicableLevel())) {
                    endpoints.add(endpoint.getId());
                }
            }

        }

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, apiId);
            preparedStatement.execute();

            for (String endpointId : endpoints) {
                if (!isEndpointAssociated(connection, endpointId)) {
                    deleteEndpoint(connection, endpointId);
                }
            }
        }
    }

    private Map<String, Endpoint> getEndPointsForOperation(Connection connection, String apiId, String operationId)
            throws SQLException, IOException {
        Map<String, Endpoint> endpointMap = new HashMap();
        final String query = "SELECT AM_ENDPOINT.UUID,AM_ENDPOINT.NAME,AM_ENDPOINT.SECURITY_CONFIGURATION,AM_ENDPOINT" +
                ".APPLICABLE_LEVEL,AM_ENDPOINT.ENDPOINT_CONFIGURATION,AM_ENDPOINT.TPS,AM_ENDPOINT.TYPE," +
                "AM_API_RESOURCE_ENDPOINT.TYPE AS ENDPOINT_LEVEL FROM AM_API_RESOURCE_ENDPOINT INNER JOIN AM_ENDPOINT" +
                " ON AM_API_RESOURCE_ENDPOINT.ENDPOINT_ID=AM_ENDPOINT.UUID WHERE AM_API_RESOURCE_ENDPOINT.API_ID = ? " +
                "AND OPERATION_ID = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, apiId);
            preparedStatement.setString(2, operationId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Endpoint endpoint = constructEndPointDetails(resultSet);
                    if (APIMgtConstants.API_SPECIFIC_ENDPOINT.equals(endpoint.getApplicableLevel())) {
                        endpointMap.put(resultSet.getString("ENDPOINT_LEVEL"), new Endpoint.Builder().
                                id(endpoint.getId()).applicableLevel(endpoint.getApplicableLevel()).build());
                    } else {
                        endpointMap.put(resultSet.getString("ENDPOINT_LEVEL"), endpoint);
                    }
                }
            }
        }
        return endpointMap;
    }

    private void addEndPointsForOperation(Connection connection, String apiId, String operationId, Map<String,
            Endpoint> endpointMap) throws SQLException {
        final String query = "INSERT INTO AM_API_RESOURCE_ENDPOINT (API_ID,OPERATION_ID,TYPE,ENDPOINT_ID) " +
                "VALUES (?,?,?,?)";
        if (endpointMap != null && !endpointMap.isEmpty()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                for (Map.Entry<String, Endpoint> entry : endpointMap.entrySet()) {
                    String endpointId;
                    Endpoint endpoint = entry.getValue();
                    if (APIMgtConstants.API_SPECIFIC_ENDPOINT.equals(endpoint.getApplicableLevel())) {
                        if (!isEndpointExist(connection, endpoint.getName())) {
                            addEndpoint(connection, endpoint);
                        }
                    }
                    endpointId = endpoint.getId();
                    preparedStatement.setString(1, apiId);
                    preparedStatement.setString(2, operationId);
                    preparedStatement.setString(3, entry.getKey());
                    preparedStatement.setString(4, endpointId);
                    preparedStatement.addBatch();
                }
                preparedStatement.executeBatch();
            }
        }
    }

    private void addLabelMapping(Connection connection, String apiID, List<String> labels, String labelType)
            throws SQLException, org.wso2.carbon.apimgt.core.exception.APIMgtDAOException {
        final String query = "INSERT INTO AM_API_LABEL_MAPPING (API_ID, LABEL_ID) VALUES (?,?)";

        if (labels != null && !labels.isEmpty()) {
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                for (String labelName : labels) {
                    String labelID = getLabelIdByNameAndType(connection, labelName, labelType);
                    statement.setString(1, apiID);
                    statement.setString(2, labelID);
                    statement.addBatch();
                }
                statement.executeBatch();
            }
        } else {
            try (PreparedStatement statement2 = connection.prepareStatement(query)) {
                String defaultLabel = getLabelIdByNameAndType(connection, APIMgtConstants.DEFAULT_LABEL_NAME,
                        labelType);
                statement2.setString(1, apiID);
                statement2.setString(2, defaultLabel);
                statement2.addBatch();
                statement2.executeBatch();
            }
        }

    }

    private void deleteLabelsMapping(Connection connection, String apiID) throws SQLException {
        final String query = "DELETE FROM AM_API_LABEL_MAPPING WHERE API_ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, apiID);
            statement.execute();
        }
    }

    private List<String> getLabelNamesForAPI(Connection connection, String apiID, String type) throws SQLException {
        List<String> labelNames = new ArrayList<>();

        final String query = "SELECT AM_LABELS.NAME FROM AM_API_LABEL_MAPPING INNER JOIN AM_LABELS ON " +
                "AM_API_LABEL_MAPPING.LABEL_ID = AM_LABELS.LABEL_ID AND AM_API_LABEL_MAPPING.API_ID = ? " +
                "AND AM_LABELS.TYPE_NAME = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, apiID);
            statement.setString(2, type);
            statement.execute();

            try (ResultSet rs = statement.getResultSet()) {

                while (rs.next()) {
                    labelNames.add(rs.getString("NAME"));
                }
            }
        }
        Collections.sort(labelNames);
        return labelNames;
    }

    private Set<String> getTransports(Connection connection, String apiID) throws SQLException {
        Set<String> transports = new HashSet<>();

        final String query = "SELECT TRANSPORT FROM AM_API_TRANSPORTS WHERE API_ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, apiID);
            statement.execute();

            try (ResultSet rs = statement.getResultSet()) {
                while (rs.next()) {
                    transports.add(rs.getString("TRANSPORT"));
                }
            }
        }

        return transports;
    }

    private String getLabelIdByNameAndType(Connection connection, String name, String type) throws APIMgtDAOException {

        final String query = "SELECT LABEL_ID FROM AM_LABELS WHERE NAME = ? AND TYPE_NAME = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, name);
            statement.setString(2, type);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("LABEL_ID");
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            String message = "Error while retrieving label ID of label [label Name] " + name;
            throw new APIMgtDAOException(message, e, ExceptionCodes.LABEL_NOT_FOUND);
        }

    }

    /**
     * Get a list threat protection policy ids associated with an API
     *
     * @param connection SQL Connection
     * @param apiId      ApiId of the API
     * @return Set of threat protection policy ids
     * @throws SQLException If failed to retrieve the set of ids
     */
    private Set<String> getThreatProtectionPolicies(Connection connection, String apiId) throws SQLException {
        Set<String> policies = new HashSet<>();
        final String query = "SELECT POLICY_ID FROM AM_THREAT_PROTECTION_MAPPING WHERE API_ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, apiId);
            statement.execute();

            try (ResultSet rs = statement.getResultSet()) {
                while (rs.next()) {
                    policies.add(rs.getString("POLICY_ID"));
                }
            }
        }

        return policies;
    }

    private static boolean isApiTypesExist(Connection connection) throws SQLException {
        final String query = "SELECT 1 FROM AM_API_TYPES";

        try (PreparedStatement statement = connection.prepareStatement(query)) {

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return true;
                }
            }
        }

        return false;
    }

    private static void addApiTypes(Connection connection) throws SQLException {
        final String query = "INSERT INTO AM_API_TYPES (TYPE_NAME) VALUES (?)";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            for (ApiType apiType : ApiType.values()) {
                statement.setString(1, apiType.toString());
                statement.addBatch();
            }

            statement.executeBatch();
        }
    }

    /**
     * Update an existing API workflow state
     *
     * @param apiID          The {@link String} of the API that needs to be updated
     * @param workflowStatus workflow status
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    @Override
    public void updateAPIWorkflowStatus(String apiID, APILCWorkflowStatus workflowStatus) throws APIMgtDAOException {
        final String query = "UPDATE AM_API SET LAST_UPDATED_TIME = ?, LC_WORKFLOW_STATUS=? WHERE UUID = ?";

        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            try {
                connection.setAutoCommit(false);
                statement.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                statement.setString(2, workflowStatus.toString());
                statement.setString(3, apiID);
                statement.execute();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                String msg = "updating workflow status for API: " + apiID + " to Status: " + workflowStatus.name();
                throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + msg, e);
            } finally {
                connection.setAutoCommit(DAOUtil.isAutoCommit());
            }
        } catch (SQLException e) {
            String msg = "updating workflow status for API: " + apiID + " to Status: " + workflowStatus.name();
            throw new APIMgtDAOException(DAOUtil.DAO_ERROR_PREFIX + msg, e);
        }
    }

}

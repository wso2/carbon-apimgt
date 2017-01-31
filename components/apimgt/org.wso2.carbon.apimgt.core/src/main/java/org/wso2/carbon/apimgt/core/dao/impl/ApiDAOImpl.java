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
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.io.IOUtils;
import org.wso2.carbon.apimgt.core.dao.ApiDAO;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.BusinessInformation;
import org.wso2.carbon.apimgt.core.models.CorsConfiguration;
import org.wso2.carbon.apimgt.core.models.DocumentInfo;
import org.wso2.carbon.apimgt.core.models.Endpoint;
import org.wso2.carbon.apimgt.core.models.ResourceCategory;
import org.wso2.carbon.apimgt.core.models.UriTemplate;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import javax.annotation.CheckForNull;
import javax.ws.rs.core.MediaType;

/**
 * Default implementation of the ApiDAO interface. Uses SQL syntax that is common to H2 and MySQL DBs.
 * Hence is considered as the default due to its re-usability.
 */
public class ApiDAOImpl implements ApiDAO {

    private final ApiDAOVendorSpecificStatements sqlStatements;
    private static final String API_SUMMARY_SELECT = "SELECT UUID, PROVIDER, NAME, CONTEXT, VERSION, DESCRIPTION, " +
            "CURRENT_LC_STATUS, LIFECYCLE_INSTANCE_ID FROM AM_API";
    private static final String AM_API_TABLE_NAME = "AM_API";

    ApiDAOImpl(ApiDAOVendorSpecificStatements sqlStatements) {
        this.sqlStatements = sqlStatements;
    }

    /**
     * Retrieve a given instance of an API
     *
     * @param apiID The {@link String} that uniquely identifies an API
     * @return valid {@link API} object or null
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    @Override
    @CheckForNull
    public API getAPI(String apiID) throws APIMgtDAOException {
        final String query = "SELECT UUID, PROVIDER, NAME, CONTEXT, VERSION, IS_DEFAULT_VERSION, DESCRIPTION, " +
                "VISIBILITY, IS_RESPONSE_CACHED, CACHE_TIMEOUT, TECHNICAL_OWNER, TECHNICAL_EMAIL, " +
                "BUSINESS_OWNER, BUSINESS_EMAIL, LIFECYCLE_INSTANCE_ID, CURRENT_LC_STATUS, " +
                "CORS_ENABLED, CORS_ALLOW_ORIGINS, CORS_ALLOW_CREDENTIALS, CORS_ALLOW_HEADERS, CORS_ALLOW_METHODS, " +
                "CREATED_BY, CREATED_TIME, LAST_UPDATED_TIME, COPIED_FROM_API FROM AM_API WHERE UUID = ?";

        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, apiID);

            return constructAPIFromResultSet(connection, statement);
        } catch (SQLException | IOException e) {
            throw new APIMgtDAOException(e);
        }
    }

    /**
     * Retrieve a given instance of an APISummary object
     *
     * @param apiID The UUID that uniquely identifies an API
     * @return valid {@link API} object or null
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    @Override
    @CheckForNull
    public API getAPISummary(String apiID) throws APIMgtDAOException {
        final String query = API_SUMMARY_SELECT + " WHERE UUID = ?";

        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, apiID);

            List<API> apiResults = constructAPISummaryList(statement);
            if (apiResults.isEmpty()) {
                return null;
            }

            return apiResults.get(0);
        } catch (SQLException e) {
            throw new APIMgtDAOException(e);
        }
    }

    /**
     * Retrieves summary data of all available APIs.
     *
     * @return {@link List<API>} matching results
     * @throws SQLException if error occurs while accessing data layer
     */
    @Override
    @SuppressFBWarnings("SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING")
    public List<API> getAPIs() throws APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(API_SUMMARY_SELECT)) {

            return constructAPISummaryList(statement);
        } catch (SQLException e) {
            throw new APIMgtDAOException(e);
        }
    }

    /**
     * Retrieves summary data of all available APIs of a given provider.
     *
     * @param providerName A given API Provider
     * @return {@link List<API>} matching results
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    @Override
    @SuppressFBWarnings("SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING")
    public List<API> getAPIsForProvider(String providerName) throws APIMgtDAOException {
        final String query = API_SUMMARY_SELECT + " WHERE PROVIDER = ?";

        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, providerName);

            return constructAPISummaryList(statement);
        } catch (SQLException e) {
            throw new APIMgtDAOException(e);
        }
    }

    /**
     * Retrieves summary data of all available APIs with life cycle status that matches the status list provided
     *
     * @param statuses A list of matching life cycle statuses
     * @return {@link List<API>} matching results
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    @Override
    @SuppressFBWarnings("SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING")
    public List<API> getAPIsByStatus(List<String> statuses) throws APIMgtDAOException {
        final String query = API_SUMMARY_SELECT + " WHERE CURRENT_LC_STATUS IN (" +
                DAOUtil.getParameterString(statuses.size()) + ")";

        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            for (int i = 0; i < statuses.size(); ++i) {
                statement.setString(i + 1, statuses.get(i));
            }

            return constructAPISummaryList(statement);
        } catch (SQLException e) {
            throw new APIMgtDAOException(e);
        }
    }

    /**
     * Retrieves summary of paginated data of all available APIs that match the given search criteria. This will use
     * the full text search for API table
     * @param searchString The search string provided
     * @param offset  The starting point of the search results.
     * @param limit   Number of search results that will be returned.
     * @return {@link List<API>} matching results
     * @throws APIMgtDAOException if error occurs while accessing data layer
     *
     */
    @Override
    @SuppressFBWarnings("SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING")
    public List<API> searchAPIs(List<String> roles, String user, String searchString, int offset, int limit) throws
            APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection();
                PreparedStatement statement = sqlStatements.search(connection, roles, user, searchString, offset,
                        limit)) {
            return constructAPISummaryList(statement);
        } catch (SQLException e) {
            throw new APIMgtDAOException(e);
        }
    }

    /**
     * Retrieves summary of paginated data of all available APIs that match the given search criteria.
     * @param attributeMap Map containing the attributes and search queries for those attributes
     * @param offset  The starting point of the search results.
     * @param limit   Number of search results that will be returned.
     * @return {@link List<API>} matching results
     * @throws APIMgtDAOException if error occurs while accessing data layer
     *
     */
    @Override
    @SuppressFBWarnings ("SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING")
    public List<API> attributeSearchAPIs(List<String> roles, String user,
            Map<String, String> attributeMap, int offset, int limit) throws APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection();
                PreparedStatement statement = sqlStatements.attributeSearch(connection, roles, user, attributeMap,
                        offset,
                        limit)) {
            DatabaseMetaData md = connection.getMetaData();
            Iterator<Map.Entry<String , String>> entries = attributeMap.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry<String, String> entry = entries.next();
                String tableName = connection.getMetaData().getDriverName().contains("PostgreSQL") ?
                        AM_API_TABLE_NAME.toLowerCase(Locale.ENGLISH) :
                        AM_API_TABLE_NAME;
                String columnName = connection.getMetaData().getDriverName().contains("PostgreSQL") ?
                        entry.getKey().toLowerCase(Locale.ENGLISH) :
                        entry.getKey().toUpperCase(Locale.ENGLISH);
                if (!checkTableColumnExists(md, tableName, columnName)) {
                    throw new APIMgtDAOException(
                            "Wrong search attribute. Attribute does not exist with name : " + entry.getKey());
                }
            }
            return constructAPISummaryList(statement);

        } catch (SQLException e) {
            throw new APIMgtDAOException(e);
        }
    }

    /**
     * Retrieves summary data of all available APIs with life cycle status that matches the status list provided
     * and matches the given search criteria.
     *
     * @param searchString The search string provided
     * @param statuses     A list of matching life cycle statuses
     * @return {@link List < API >} matching results
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    @Override
    @SuppressFBWarnings("SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING")
    public List<API> searchAPIsByStatus(String searchString, List<String> statuses) throws APIMgtDAOException {
        final String query = API_SUMMARY_SELECT + " WHERE LOWER(NAME) LIKE ? AND CURRENT_LC_STATUS IN (" +
                DAOUtil.getParameterString(statuses.size()) + ")";

        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, '%' + searchString.toLowerCase(Locale.ENGLISH) + '%');

            for (int i = 0; i < statuses.size(); ++i) {
                statement.setString(i + 2, statuses.get(i));
            }

            return constructAPISummaryList(statement);
        } catch (SQLException e) {
            throw new APIMgtDAOException(e);
        }
    }

    /**
     * Checks if a given API which is uniquely identified by the API Name  already
     * exists
     *
     * @param apiName Name of API
     * @return true if  apiName combination already exists else false
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    @Override
    public boolean isAPINameExists(String apiName, String providerName) throws APIMgtDAOException {
        final String apiExistsQuery = "SELECT UUID FROM AM_API WHERE LOWER(NAME) = ? AND PROVIDER = ?";

        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(apiExistsQuery)) {
            statement.setString(1, apiName.toLowerCase(Locale.ENGLISH));
            statement.setString(2, providerName);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return true;
                }
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException(e);
        }

        return false;
    }

    /**
     * Checks if a given API Context already exists
     *
     * @param contextName Name of API Context
     * @return true if contextName already exists else false
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    @Override
    public boolean isAPIContextExists(String contextName) throws APIMgtDAOException {
        final String apiExistsQuery = "SELECT UUID FROM AM_API WHERE CONTEXT = ?";

        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(apiExistsQuery)) {
            statement.setString(1, contextName);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return true;
                }
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException(e);
        }

        return false;
    }

    /**
     * Add a new instance of an API
     *
     * @param api The {@link API} object to be added
     * @return true if addition is successful else false
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    @Override
    public void addAPI(final API api) throws APIMgtDAOException {
        final String addAPIQuery = "INSERT INTO AM_API (PROVIDER, NAME, CONTEXT, VERSION, " +
                "IS_DEFAULT_VERSION, DESCRIPTION, VISIBILITY, IS_RESPONSE_CACHED, CACHE_TIMEOUT, " +
                "UUID, TECHNICAL_OWNER, TECHNICAL_EMAIL, BUSINESS_OWNER, BUSINESS_EMAIL, LIFECYCLE_INSTANCE_ID, " +
                "CURRENT_LC_STATUS, CORS_ENABLED, CORS_ALLOW_ORIGINS, CORS_ALLOW_CREDENTIALS, CORS_ALLOW_HEADERS, " +
                "CORS_ALLOW_METHODS,CREATED_BY, CREATED_TIME, LAST_UPDATED_TIME, COPIED_FROM_API) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(addAPIQuery)) {
            try {
                connection.setAutoCommit(false);

                String apiPrimaryKey = api.getId();
                statement.setString(1, api.getProvider());
                statement.setString(2, api.getName());
                statement.setString(3, api.getContext());
                statement.setString(4, api.getVersion());
                statement.setBoolean(5, api.isDefaultVersion());
                statement.setString(6, api.getDescription());
                statement.setString(7, api.getVisibility().toString());
                statement.setBoolean(8, api.isResponseCachingEnabled());
                statement.setInt(9, api.getCacheTimeout());
                statement.setString(10, apiPrimaryKey);

                BusinessInformation businessInformation = api.getBusinessInformation();
                statement.setString(11, businessInformation.getTechnicalOwner());
                statement.setString(12, businessInformation.getTechnicalOwnerEmail());
                statement.setString(13, businessInformation.getBusinessOwner());
                statement.setString(14, businessInformation.getBusinessOwnerEmail());

                statement.setString(15, api.getLifecycleInstanceId());
                statement.setString(16, api.getLifeCycleStatus());

                CorsConfiguration corsConfiguration = api.getCorsConfiguration();
                statement.setBoolean(17, corsConfiguration.isEnabled());
                statement.setString(18, String.join(",", corsConfiguration.getAllowOrigins()));
                statement.setBoolean(19, corsConfiguration.isAllowCredentials());
                statement.setString(20, String.join(",", corsConfiguration.getAllowHeaders()));
                statement.setString(21, String.join(",", corsConfiguration.getAllowMethods()));

                statement.setString(22, api.getCreatedBy());
                statement.setTimestamp(23, Timestamp.valueOf(api.getCreatedTime()));
                statement.setTimestamp(24, Timestamp.valueOf(api.getLastUpdatedTime()));
                statement.setString(25, api.getCopiedFromApiId());
                statement.execute();

                if (API.Visibility.RESTRICTED == api.getVisibility()) {
                    addVisibleRole(connection, apiPrimaryKey, api.getVisibleRoles());
                }

                String wsdlUri = api.getWsdlUri();

                if (wsdlUri != null) {
                    ApiResourceDAO.addTextResource(connection, apiPrimaryKey, UUID.randomUUID().toString(),
                            ResourceCategory.WSDL_URI, MediaType.TEXT_PLAIN, wsdlUri);
                }
                addTagsMapping(connection, apiPrimaryKey, api.getTags());
                addGatewayConfig(connection, apiPrimaryKey, api.getGatewayConfig());
                addTransports(connection, apiPrimaryKey, api.getTransport());
                addUrlMappings(connection, api.getUriTemplates().values(), apiPrimaryKey);
                addSubscriptionPolicies(connection, api.getPolicies(), apiPrimaryKey);
                addEndPointsForApi(connection, apiPrimaryKey, api.getEndpoint());
                addAPIDefinition(connection, apiPrimaryKey, api.getApiDefinition());
                addAPIPermission(connection,  api.getPermissionMap(), apiPrimaryKey);
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new APIMgtDAOException(e);
            } finally {
                connection.setAutoCommit(DAOUtil.isAutoCommit());
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException(e);
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
                "CORS_ALLOW_CREDENTIALS = ?, CORS_ALLOW_HEADERS = ?, CORS_ALLOW_METHODS = ?, LAST_UPDATED_TIME = ?" +
                " WHERE UUID = ?";

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

                statement.setTimestamp(16, Timestamp.valueOf(substituteAPI.getLastUpdatedTime()));
                statement.setString(17, apiID);

                statement.execute();

                deleteVisibleRoles(connection, apiID); // Delete current visible roles if they exist

                if (API.Visibility.RESTRICTED == substituteAPI.getVisibility()) {
                    addVisibleRole(connection, apiID, substituteAPI.getVisibleRoles());
                }

                String wsdlUri = substituteAPI.getWsdlUri();
                if (wsdlUri.isEmpty()) {
                    ApiResourceDAO.deleteUniqueResourceForCategory(connection, apiID, ResourceCategory.WSDL_URI);
                } else {
                    if (!ApiResourceDAO.isResourceExistsForCategory(connection, apiID, ResourceCategory.WSDL_URI)) {
                        ApiResourceDAO.addTextResource(connection, apiID, UUID.randomUUID().toString(),
                                ResourceCategory.WSDL_URI, MediaType.TEXT_PLAIN, wsdlUri);
                    } else {
                        ApiResourceDAO.updateTextValueForCategory(connection, apiID,
                                ResourceCategory.WSDL_URI, wsdlUri);
                    }
                }

                deleteAPIPermission(connection, apiID);
                updateApiPermission(connection, substituteAPI.getPermissionMap(), apiID);

                deleteTransports(connection, apiID);
                addTransports(connection, apiID, substituteAPI.getTransport());

                deleteTagsMapping(connection, apiID); // Delete current tag mappings if they exist
                addTagsMapping(connection, apiID, substituteAPI.getTags());
                deleteSubscriptionPolicies(connection, apiID);
                addSubscriptionPolicies(connection, substituteAPI.getPolicies(), apiID);
                deleteUrlMappings(connection, apiID);
                addUrlMappings(connection, substituteAPI.getUriTemplates().values(), apiID);
                deleteEndPointsForApi(connection, apiID);
                addEndPointsForApi(connection, apiID, substituteAPI.getEndpoint());
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new APIMgtDAOException(e);
            } finally {
                connection.setAutoCommit(DAOUtil.isAutoCommit());
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException(e);
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
        final String query = "DELETE FROM AM_API WHERE UUID = ?";
        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            try {
                connection.setAutoCommit(false);
                statement.setString(1, apiID);
                statement.execute();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new APIMgtDAOException(e);
            } finally {
                connection.setAutoCommit(DAOUtil.isAutoCommit());
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException(e);
        }
    }

    /**
     * Get swagger definition of a given API
     *
     * @param apiID The UUID of the respective API
     * @return Swagger definition String
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    @Override
    public String getSwaggerDefinition(String apiID) throws APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection()) {
            return getAPIDefinition(connection, apiID);
        } catch (SQLException | IOException e) {
            throw new APIMgtDAOException(e);
        }
    }

    /**
     * Update swagger definition of a given API
     *
     * @param apiID             The UUID of the respective API
     * @param swaggerDefinition Swagger definition String
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    @Override
    public void updateSwaggerDefinition(String apiID, String swaggerDefinition) throws APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection()) {
            try {
                connection.setAutoCommit(false);
                updateAPIDefinition(connection, apiID, swaggerDefinition);
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new APIMgtDAOException(e);
            } finally {
                connection.setAutoCommit(DAOUtil.isAutoCommit());
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException("Data access error when updating API definition", e);
        }
    }

    /**
     * Get gateway configuration of a given API
     *
     * @param apiID The UUID of the respective API
     * @return gateway configuration String
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    public String getGatewayConfig(String apiID) throws APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection()) {
            return getGatewayConfig(connection, apiID);
        } catch (SQLException | IOException e) {
            throw new APIMgtDAOException(e);
        }
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
            return ApiResourceDAO.getBinaryValueForCategory(connection, apiID, ResourceCategory.IMAGE);
        } catch (SQLException | IOException e) {
            throw new APIMgtDAOException("Couldn't retrieve api thumbnail for api " + apiID, e);
        }
    }

    /**
     * Update image of a given API
     *
     * @param apiID    The UUID of the respective API
     * @param image    Image stream
     * @param dataType Data Type of image
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    @Override
    public void updateImage(String apiID, InputStream image, String dataType) throws APIMgtDAOException {
        if (image != null) {
            try (Connection connection = DAOUtil.getConnection()) {
                try {
                    connection.setAutoCommit(false);
                    if (!ApiResourceDAO.isResourceExistsForCategory(connection, apiID,
                            ResourceCategory.WSDL_URI)) {
                        ApiResourceDAO.addBinaryResource(connection, apiID, UUID.randomUUID().toString(),
                                ResourceCategory.IMAGE, dataType, image);
                    } else {
                        ApiResourceDAO.updateBinaryResourceForCategory(connection, apiID,
                                ResourceCategory.IMAGE, image);
                    }
                    connection.commit();
                } catch (SQLException e) {
                    connection.rollback();
                    throw new APIMgtDAOException(e);
                } finally {
                    connection.setAutoCommit(DAOUtil.isAutoCommit());
                }
            } catch (SQLException e) {
                throw new APIMgtDAOException(e);
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
        final String query = "UPDATE AM_API SET CURRENT_LC_STATUS = ? WHERE UUID = ?";
        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            try {
                connection.setAutoCommit(false);
                statement.setString(1, status);
                statement.setString(2, apiID);
                statement.execute();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new APIMgtDAOException(e);
            } finally {
                connection.setAutoCommit(DAOUtil.isAutoCommit());
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException(e);
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
            throw new APIMgtDAOException(e);
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
            throw new APIMgtDAOException(e);
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
        } catch (SQLException e) {
            throw new APIMgtDAOException(e);
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
            throw new APIMgtDAOException(e);
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
                throw new APIMgtDAOException(e);
            } finally {
                connection.setAutoCommit(DAOUtil.isAutoCommit());
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException(e);
        }
    }

    /**
     * Add Document File content
     *
     * @param resourceID UUID of resource
     * @param content    File content as an InputStream
     * @param fileName
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    @Override
    public void addDocumentFileContent(String resourceID, InputStream content, String fileName) throws
            APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection()) {
            try {
                connection.setAutoCommit(false);
                if (ApiResourceDAO.updateBinaryResource(connection, resourceID, content, fileName) == 0) {
                    throw new APIMgtDAOException("Cannot add file content for a document that does not exist");
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new APIMgtDAOException(e);
            } finally {
                connection.setAutoCommit(DAOUtil.isAutoCommit());
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException(e);
        }
    }

    /**
     * Add Document Inline content
     *
     * @param resourceID UUID of resource
     * @param content    Inline content as a String
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    @Override
    public void addDocumentInlineContent(String resourceID, String content) throws APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection()) {
            try {
                connection.setAutoCommit(false);
                if (ApiResourceDAO.updateTextResource(connection, resourceID, content) == 0) {
                    throw new APIMgtDAOException("Cannot add inline content for a document that does not exist");
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new APIMgtDAOException(e);
            } finally {
                connection.setAutoCommit(DAOUtil.isAutoCommit());
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException(e);
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
                throw new APIMgtDAOException(e);
            } finally {
                connection.setAutoCommit(DAOUtil.isAutoCommit());
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException(e);
        }
    }

    /**
     * Used to deprecate older versions of the api
     *
     * @param identifier
     */
    @Override
    public void deprecateOlderVersions(String identifier) {
        /**
         * todo:
         */
    }

    @Override
    public boolean isDocumentExist(String apiId, DocumentInfo documentInfo) throws APIMgtDAOException {
        final String query = "SELECT AM_API_DOC_META_DATA.UUID FROM AM_API_DOC_META_DATA INNER JOIN AM_API_RESOURCES " +
                "ON AM_API_DOC_META_DATA.UUID=AM_API_RESOURCES.UUID WHERE AM_API_RESOURCES.API_ID = ? AND " +
                "AM_API_DOC_META_DATA.NAME=? AND AM_API_DOC_META_DATA.TYPE= ? AND AM_API_DOC_META_DATA.SOURCE_TYPE= ?";
        boolean exist = false;
        try (Connection connection = DAOUtil.getConnection(); PreparedStatement preparedStatement = connection
                .prepareStatement(query)) {
            preparedStatement.setString(1, apiId);
            preparedStatement.setString(2, documentInfo.getName());
            preparedStatement.setString(3, documentInfo.getType().getType());
            preparedStatement.setString(4, documentInfo.getSourceType().getType());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    exist = true;
                }
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException(e);
        }
        return exist;
    }

    private API constructAPIFromResultSet(Connection connection, PreparedStatement statement) throws SQLException,
            IOException {
        try (ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                BusinessInformation businessInformation = new BusinessInformation();
                businessInformation.setTechnicalOwner(rs.getString("TECHNICAL_OWNER"));
                businessInformation.setTechnicalOwnerEmail(rs.getString("TECHNICAL_EMAIL"));
                businessInformation.setBusinessOwner(rs.getString("BUSINESS_OWNER"));
                businessInformation.setBusinessOwnerEmail(rs.getString("BUSINESS_EMAIL"));

                CorsConfiguration corsConfiguration = new CorsConfiguration();
                corsConfiguration.setEnabled(rs.getBoolean("CORS_ENABLED"));
                corsConfiguration.setAllowOrigins(commaSeperatedStringToList(rs.getString("CORS_ALLOW_ORIGINS")));
                corsConfiguration.setAllowCredentials(rs.getBoolean("CORS_ALLOW_CREDENTIALS"));
                corsConfiguration.setAllowHeaders(commaSeperatedStringToList(rs.getString("CORS_ALLOW_HEADERS")));
                corsConfiguration.setAllowMethods(commaSeperatedStringToList(rs.getString("CORS_ALLOW_METHODS")));

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
                        tags(getTags(connection, apiPrimaryKey)).
                        wsdlUri(ApiResourceDAO.
                                getTextValueForCategory(connection, apiPrimaryKey,
                                        ResourceCategory.WSDL_URI)).
                        transport(getTransports(connection, apiPrimaryKey)).
                        endpoint(getEndPointsForApi(connection, apiPrimaryKey)).
                        businessInformation(businessInformation).
                        lifecycleInstanceId(rs.getString("LIFECYCLE_INSTANCE_ID")).
                        lifeCycleStatus(rs.getString("CURRENT_LC_STATUS")).
                        corsConfiguration(corsConfiguration).
                        createdBy(rs.getString("CREATED_BY")).
                        createdTime(rs.getTimestamp("CREATED_TIME").toLocalDateTime()).
                        lastUpdatedTime(rs.getTimestamp("LAST_UPDATED_TIME").toLocalDateTime()).
                        uriTemplates(getUriTemplates(connection, apiPrimaryKey)).
                        policies(getSubscripitonPolciesByAPIId(connection, apiPrimaryKey)).copiedFromApiId(rs.getString
                        ("COPIED_FROM_API")).build();
            }
        }

        return null;
    }

    private List<API> constructAPISummaryList(PreparedStatement statement) throws SQLException {
        List<API> apiList = new ArrayList<>();
        try (ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                API apiSummary = new API.APIBuilder(rs.getString("PROVIDER"), rs.getString("NAME"),
                        rs.getString("VERSION")).
                        id(rs.getString("UUID")).
                        context(rs.getString("CONTEXT")).
                        description(rs.getString("DESCRIPTION")).
                        lifeCycleStatus(rs.getString("CURRENT_LC_STATUS")).
                        lifecycleInstanceId(rs.getString("LIFECYCLE_INSTANCE_ID")).build();

                apiList.add(apiSummary);
            }
        }

        return apiList;
    }

    private void addTagsMapping(Connection connection, String apiID, List<String> tags) throws SQLException {
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

    private List<String> getTags(Connection connection, String apiID) throws SQLException {
        List<String> tags = new ArrayList<>();

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

    private void addVisibleRole(Connection connection, String apiID, List<String> roles) throws SQLException {
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

    private List<String> getVisibleRoles(Connection connection, String apiID) throws SQLException {
        List<String> roles = new ArrayList<>();

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

    private void addAPIDefinition(Connection connection, String apiID, String apiDefinition) throws SQLException {
        if (!apiDefinition.isEmpty()) {
            ApiResourceDAO.addBinaryResource(connection, apiID, UUID.randomUUID().toString(), ResourceCategory.SWAGGER,
                    MediaType.APPLICATION_JSON,
                    new ByteArrayInputStream(apiDefinition.getBytes(StandardCharsets.UTF_8)));
        }
    }

    private void updateAPIDefinition(Connection connection, String apiID, String apiDefinition) throws SQLException {
        ApiResourceDAO.updateBinaryResourceForCategory(connection, apiID, ResourceCategory.SWAGGER,
                new ByteArrayInputStream(apiDefinition.getBytes(StandardCharsets.UTF_8)));
    }

    private String getAPIDefinition(Connection connection, String apiID) throws SQLException, IOException {
        InputStream apiDefinition = ApiResourceDAO.getBinaryValueForCategory(connection, apiID,
                ResourceCategory.SWAGGER);

        return IOUtils.toString(apiDefinition, StandardCharsets.UTF_8);
    }

    private void addGatewayConfig(Connection connection, String apiID, String gatewayConfig) throws SQLException {
        if (!gatewayConfig.isEmpty()) {
            ApiResourceDAO
                    .addBinaryResource(connection, apiID, UUID.randomUUID().toString(), ResourceCategory.GATEWAY_CONFIG,
                            MediaType.APPLICATION_JSON,
                            new ByteArrayInputStream(gatewayConfig.getBytes(StandardCharsets.UTF_8)));
        }
    }

    private String getGatewayConfig(Connection connection, String apiID) throws SQLException, IOException {
        InputStream gatewayConfig = ApiResourceDAO
                .getBinaryValueForCategory(connection, apiID, ResourceCategory.GATEWAY_CONFIG);

        return IOUtils.toString(gatewayConfig, StandardCharsets.UTF_8);
    }

    private String getAPIThrottlePolicyID(Connection connection, String policyName) throws SQLException {
        final String query = "SELECT UUID FROM AM_API_POLICY WHERE NAME = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, policyName);
            statement.execute();

            try (ResultSet rs = statement.getResultSet()) {
                if (rs.next()) {
                    return rs.getString("UUID");
                }
            }
        }

        throw new SQLException("API Policy " + policyName + ", does not exist");
    }

    private String getAPIThrottlePolicyName(Connection connection, String policyID) throws SQLException {
        final String query = "SELECT NAME FROM AM_API_POLICY WHERE UUID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, policyID);
            statement.execute();

            try (ResultSet rs = statement.getResultSet()) {
                if (rs.next()) {
                    return rs.getString("NAME");
                }
            }
        }

        throw new SQLException("API Policy ID " + policyID + ", does not exist");
    }

    private void addTransports(Connection connection, String apiID, List<String> transports) throws SQLException {
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


    private void deleteAPIPermission(Connection connection, String apiID) throws SQLException {
        final String query = "DELETE FROM AM_API_GROUP_PERMISSION WHERE API_ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, apiID);
            statement.execute();
        }
    }

    private void addAPIPermission(Connection connection, HashMap permissionMap, String apiId) throws SQLException {
        final String query = "INSERT INTO AM_API_GROUP_PERMISSION (API_ID, GROUP_ID, PERMISSION) VALUES (?, ?, ?)";
        Map<String, Integer> map = permissionMap;
        if (permissionMap != null) {
            if (permissionMap.size() > 0) {
                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    for (Map.Entry<String, Integer> entry : map.entrySet()) {
                        statement.setString(1, apiId);
                        statement.setString(2, entry.getKey());
                        //if permission value is UPDATE or DELETE we by default give them read permission also.
                        if (entry.getValue() < APIMgtConstants.Permission.READ_PERMISSION && entry.getValue() != 0) {
                            statement.setInt(3, entry.getValue() + APIMgtConstants.Permission.READ_PERMISSION);
                        } else {
                            statement.setInt(3, entry.getValue());
                        }
                        statement.addBatch();
                    }
                    statement.executeBatch();
                }
            }
        } else {
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, apiId);
                statement.setString(2, APIMgtConstants.Permission.EVERYONE_GROUP);
                statement.setInt(3, 7);
                statement.execute();
            }
        }

    }

    private void updateApiPermission(Connection connection, HashMap permissionMap, String apiId) throws SQLException {
        final String query = "INSERT INTO AM_API_GROUP_PERMISSION (API_ID, GROUP_ID, PERMISSION) VALUES (?, ?, ?)";
        Map<String, Integer> map = permissionMap;
        if (permissionMap != null) {
            if (permissionMap.size() > 0) {
                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    for (Map.Entry<String, Integer> entry : map.entrySet()) {
                        statement.setString(1, apiId);
                        statement.setString(2, entry.getKey());
                        //if permission value is UPDATE or DELETE we by default give them read permission also.
                        if (entry.getValue() < APIMgtConstants.Permission.READ_PERMISSION && entry.getValue() != 0) {
                            statement.setInt(3, entry.getValue() + APIMgtConstants.Permission.READ_PERMISSION);
                        } else {
                            statement.setInt(3, entry.getValue());
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

    private List<String> getTransports(Connection connection, String apiID) throws SQLException {
        List<String> transports = new ArrayList<>();

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

    private List<String> commaSeperatedStringToList(String strValue) {
        if (strValue != null && !strValue.isEmpty()) {
            return Arrays.asList(strValue.split("\\s*,\\s*"));
        }

        return new ArrayList<>();
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
                statement.setString(6, getAPIThrottlePolicyID(connection, uriTemplate.getPolicy()));
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

    private Map<String, UriTemplate> getUriTemplates(Connection connection, String apiId) throws SQLException {
        final String query = "SELECT OPERATION_ID,API_ID,HTTP_METHOD,URL_PATTERN,AUTH_SCHEME,API_POLICY_ID FROM " +
                "AM_API_OPERATION_MAPPING WHERE API_ID = ?";
        Map<String, UriTemplate> uriTemplateSet = new HashMap();
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, apiId);
            statement.execute();
            try (ResultSet rs = statement.getResultSet()) {
                while (rs.next()) {
                    UriTemplate uriTemplate = new UriTemplate.UriTemplateBuilder()
                            .uriTemplate(rs.getString("URL_PATTERN")).authType(rs.getString("AUTH_SCHEME"))
                            .httpVerb(rs.getString("HTTP_METHOD"))
                            .policy(getAPIThrottlePolicyName(connection, rs.getString("API_POLICY_ID"))).templateId
                                    (rs.getString("OPERATION_ID")).endpoint(getEndPointsForOperation(connection,
                                    apiId, rs.getString("OPERATION_ID"))).build();
                    uriTemplateSet.put(uriTemplate.getTemplateId(), uriTemplate);
                }
            }
        }
        return uriTemplateSet;
    }

    private void addSubscriptionPolicies(Connection connection, List<String> policies, String apiID)
            throws SQLException {
        final String query =
                "INSERT INTO AM_API_SUBS_POLICY_MAPPING (API_ID, SUBSCRIPTION_POLICY_ID) " + "VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            for (String policy : policies) {
                statement.setString(1, apiID);
                statement.setString(2, getSubscriptionThrottlePolicyID(connection, policy));
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void deleteSubscriptionPolicies(Connection connection, String apiID) throws SQLException {
        final String query = "DELETE FROM AM_API_SUBS_POLICY_MAPPING WHERE API_ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, apiID);
            statement.execute();
        }
    }

    private String getSubscriptionThrottlePolicyID(Connection connection, String policyName) throws SQLException {
        final String query = "SELECT UUID from AM_SUBSCRIPTION_POLICY where NAME=?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, policyName);
            statement.execute();

            try (ResultSet rs = statement.getResultSet()) {
                if (rs.next()) {
                    return rs.getString("UUID");
                }
            }
        }

        throw new SQLException("Subscription Policy " + policyName + ", does not exist");
    }

    private List<String> getSubscripitonPolciesByAPIId(Connection connection, String apiId) throws SQLException {
        final String query = "SELECT amPolcySub.NAME FROM AM_API_SUBS_POLICY_MAPPING apimsubmapping," +
                "AM_SUBSCRIPTION_POLICY amPolcySub where apimsubmapping.SUBSCRIPTION_POLICY_ID=amPolcySub.UUID " +
                "AND apimsubmapping.API_ID = ?";
        List<String> policies = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, apiId);
            statement.execute();

            try (ResultSet rs = statement.getResultSet()) {
                while (rs.next()) {
                    policies.add(rs.getString("NAME"));
                }
            }
        }

        return policies;
    }

    private boolean checkTableColumnExists (DatabaseMetaData databaseMetaData, String tableName, String columnName)
            throws
            APIMgtDAOException {
        try (ResultSet rs = databaseMetaData.getColumns(null, null, tableName, columnName)) {
            return rs.next();
        } catch (SQLException e) {
            throw new APIMgtDAOException(e);
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
                throw new APIMgtDAOException(e);
            } finally {
                connection.setAutoCommit(DAOUtil.isAutoCommit());
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException(e);
        }
    }

    /**
     * Add an Endpoint
     *
     * @param endpoint
     * @return
     * @throws APIMgtDAOException
     */
    @Override
    public void addEndpoint(Endpoint endpoint) throws APIMgtDAOException {
        final String query = "INSERT INTO AM_ENDPOINT (UUID,NAME,ENDPOINT_CONFIGURATION," +
                "TPS_SANDBOX,TPS_PRODUCTION,SECURITY_CONFIGURATION) VALUES (?,?,?,?,?,?)";
        try (Connection connection = DAOUtil.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, endpoint.getId());
                statement.setString(2, endpoint.getName());
                InputStream byteArrayInputStream = IOUtils.toInputStream(endpoint.getEndpointConfig());
                statement.setBinaryStream(3, byteArrayInputStream);
                statement.setLong(4, endpoint.getMaxTps().getSandbox());
                statement.setLong(5, endpoint.getMaxTps().getProduction());
                statement.setBinaryStream(6, IOUtils.toInputStream(endpoint.getSecurity()));
                statement.execute();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new APIMgtDAOException(e);
            } finally {
                connection.setAutoCommit(DAOUtil.isAutoCommit());
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException(e);
        }
    }

    /**
     * Delete an Endpoint
     *
     * @param endpointId
     * @return
     * @throws APIMgtDAOException
     */
    @Override
    public boolean deleteEndpoint(String endpointId) throws APIMgtDAOException {
        final String query = "DELETE FROM AM_ENDPOINT WHERE UUID = ?";
        try (Connection connection = DAOUtil.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, endpointId);
                statement.execute();
                connection.commit();
                return true;
            } catch (SQLException e) {
                connection.rollback();
                throw new APIMgtDAOException(e);
            } finally {
                connection.setAutoCommit(DAOUtil.isAutoCommit());
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException(e);
        }
    }

    /**
     * Update an Endpoint
     *
     * @param endpoint
     * @return
     * @throws APIMgtDAOException
     */
    @Override
    public boolean updateEndpoint(Endpoint endpoint) throws APIMgtDAOException {
        final String query = "UPDATE AM_ENDPOINT SET ENDPOINT_CONFIGURATION = ?,TPS_SANDBOX = ?,TPS_PRODUCTION = " +
                "?,SECURITY_CONFIGURATION =? WHERE UUID = ?";
        try (Connection connection = DAOUtil.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                InputStream byteArrayInputStream = IOUtils.toInputStream(endpoint.getEndpointConfig());
                statement.setBinaryStream(1, byteArrayInputStream);
                statement.setLong(2, endpoint.getMaxTps().getSandbox());
                statement.setLong(3, endpoint.getMaxTps().getProduction());
                statement.setBinaryStream(4, IOUtils.toInputStream(endpoint.getSecurity()));
                statement.setString(5, endpoint.getId());
                statement.execute();
                connection.commit();
                return true;
            } catch (SQLException e) {
                connection.rollback();
                throw new APIMgtDAOException(e);
            } finally {
                connection.setAutoCommit(DAOUtil.isAutoCommit());
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException(e);
        }
    }

    /**
     * Get an Endpoint
     *
     * @param endpointId uuid of endpoint
     * @return
     * @throws APIMgtDAOException
     */
    @Override
    public Endpoint getEndpoint(String endpointId) throws APIMgtDAOException {
        final String query = "SELECT UUID,NAME,ENDPOINT_CONFIGURATION,TPS_SANDBOX,TPS_PRODUCTION," +
                "SECURITY_CONFIGURATION FROM AM_ENDPOINT WHERE UUID = ?";
        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, endpointId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Endpoint.Builder endpointBuilder = new Endpoint.Builder();
                    endpointBuilder.id(endpointId);
                    endpointBuilder.name(resultSet.getString("NAME"));
                    endpointBuilder.endpointConfig(IOUtils.toString(resultSet.getBinaryStream
                            ("ENDPOINT_CONFIGURATION")));
                    endpointBuilder.maxTps(new Endpoint.MaxTps(resultSet.getLong("TPS_PRODUCTION"), resultSet.getLong
                            ("TPS_SANDBOX")));
                    endpointBuilder.security(IOUtils.toString(resultSet.getBinaryStream("SECURITY_CONFIGURATION")));
                    return endpointBuilder.build();
                } else {
                    return null;
                }
            }
        } catch (SQLException | IOException e) {
            throw new APIMgtDAOException(e);
        }
    }

    /**
     * get all Endpoints
     *
     * @return
     * @throws APIMgtDAOException
     */
    @Override
    public List<Endpoint> getEndpoints() throws APIMgtDAOException {
        final String query = "SELECT UUID,NAME,ENDPOINT_CONFIGURATION,TPS_SANDBOX,TPS_PRODUCTION," +
                "SECURITY_CONFIGURATION FROM AM_ENDPOINT";
        List<Endpoint> endpointList = new ArrayList<>();
        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                Endpoint.Builder endpointBuilder = new Endpoint.Builder();
                endpointBuilder.id(resultSet.getString("UUID"));
                endpointBuilder.name(resultSet.getString("NAME"));
                endpointBuilder.endpointConfig(IOUtils.toString(resultSet.getBinaryStream
                        ("ENDPOINT_CONFIGURATION")));
                endpointBuilder.maxTps(new Endpoint.MaxTps(resultSet.getLong("TPS_PRODUCTION"), resultSet.getLong
                        ("TPS_SANDBOX")));
                endpointBuilder.security(IOUtils.toString(resultSet.getBinaryStream("SECURITY_CONFIGURATION")));
                endpointList.add(endpointBuilder.build());
            }
        } catch (SQLException | IOException e) {
            throw new APIMgtDAOException(e);
        }
        return endpointList;
    }

    private Map<String, String> getEndPointsForApi(Connection connection, String apiId) throws SQLException {
        Map<String, String> endpointMap = new HashedMap();
        final String query = "SELECT ENDPOINT_ID,TYPE FROM AM_API_ENDPOINT_MAPPING WHERE API_ID=?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, apiId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    endpointMap.put(resultSet.getString("TYPE"), resultSet.getString("ENDPOINT_ID"));
                }
            }
        }
        return endpointMap;
    }

    private void addEndPointsForApi(Connection connection, String apiId, Map<String, String> endpointMap) throws
            SQLException {
        final String query = "INSERT INTO AM_API_ENDPOINT_MAPPING (API_ID,TYPE,ENDPOINT_ID) VALUES (?,?,?)";
        if (endpointMap != null && !endpointMap.isEmpty()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                for (Map.Entry<String, String> entry : endpointMap.entrySet()) {
                    preparedStatement.setString(1, apiId);
                    preparedStatement.setString(2, entry.getKey());
                    preparedStatement.setString(3, entry.getValue());
                    preparedStatement.addBatch();
                }
                preparedStatement.executeBatch();
            }
        }
    }

    private void deleteEndPointsForApi(Connection connection, String apiId) throws SQLException {
        final String query = "DELETE FROM AM_API_ENDPOINT_MAPPING WHERE API_ID = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, apiId);
            preparedStatement.execute();
        }
    }

    private Map<String, String> getEndPointsForOperation(Connection connection, String apiId, String operationId)
            throws SQLException {
        Map<String, String> endpointMap = new HashedMap();
        final String query = "SELECT ENDPOINT_ID,TYPE FROM AM_API_RESOURCE_ENDPOINT WHERE API_ID=? AND " +
                "OPERATION_ID = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, apiId);
            preparedStatement.setString(2, operationId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    endpointMap.put(resultSet.getString("TYPE"), resultSet.getString("ENDPOINT_ID"));
                }
            }
        }
        return endpointMap;
    }

    private void addEndPointsForOperation(Connection connection, String apiId, String operationId, Map<String,
            String> endpointMap) throws SQLException {
        final String query = "INSERT INTO AM_API_RESOURCE_ENDPOINT (API_ID,OPERATION_ID,TYPE,ENDPOINT_ID) " +
                "VALUES (?,?,?,?)";
        if (endpointMap != null && !endpointMap.isEmpty()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                    for (Map.Entry<String, String> entry : endpointMap.entrySet()) {
                        preparedStatement.setString(1, apiId);
                        preparedStatement.setString(2, operationId);
                        preparedStatement.setString(3, entry.getKey());
                        preparedStatement.setString(4, entry.getValue());
                        preparedStatement.addBatch();
                    }
                    preparedStatement.executeBatch();
            }
        }
    }
}

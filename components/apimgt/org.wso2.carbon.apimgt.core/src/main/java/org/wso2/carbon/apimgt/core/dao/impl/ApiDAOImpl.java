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
import org.wso2.carbon.apimgt.core.dao.ApiDAO;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.ArtifactResource;
import org.wso2.carbon.apimgt.core.models.BusinessInformation;
import org.wso2.carbon.apimgt.core.models.CorsConfiguration;
import org.wso2.carbon.apimgt.core.models.ResourceCategory;
import org.wso2.carbon.apimgt.core.models.ResourceVisibility;
import org.wso2.carbon.apimgt.core.models.UriTemplate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.annotation.CheckForNull;
import javax.ws.rs.core.MediaType;

/**
 * Default implementation of the ApiDAO interface. Uses SQL syntax that is common to H2 and MySQL DBs.
 * Hence is considered as the default due to its re-usability.
 */
public class ApiDAOImpl implements ApiDAO {

    //private final ApiDAOVendorSpecificStatements sqlStatements;
    private static final String API_SUMMARY_SELECT = "SELECT UUID, PROVIDER, NAME, CONTEXT, VERSION, DESCRIPTION, " +
            "CURRENT_LC_STATUS, LIFECYCLE_INSTANCE_ID FROM AM_API";

    ApiDAOImpl(ApiDAOVendorSpecificStatements sqlStatements) {
        //this.sqlStatements = sqlStatements;
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
                "CREATED_BY, CREATED_TIME, LAST_UPDATED_TIME FROM AM_API WHERE UUID = ?";

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

            return constructAPISummary(statement);
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
     * Retrieves summary data of all available APIs that match the given search criteria.
     *
     * @param searchString The search string provided
     * @return {@link List<API>} matching results
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    @Override
    @SuppressFBWarnings("SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING")
    public List<API> searchAPIs(String searchString) throws APIMgtDAOException {
        final String query = API_SUMMARY_SELECT + " WHERE NAME LIKE ?";

        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, '%' + searchString + '%');
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
    public boolean isAPINameExists(String apiName) throws APIMgtDAOException {
        final String apiExistsQuery = "SELECT UUID FROM AM_API WHERE NAME = ?";

        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(apiExistsQuery)) {
            statement.setString(1, apiName);

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
                "CURRENT_LC_STATUS, CORS_ENABLED, CORS_ALLOW_ORIGINS, CORS_ALLOW_CREDENTIALS, " +
                "CORS_ALLOW_HEADERS, CORS_ALLOW_METHODS,CREATED_BY, CREATED_TIME, LAST_UPDATED_TIME) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(addAPIQuery)) {
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

            statement.execute();

            if (API.Visibility.RESTRICTED == api.getVisibility()) {
                addVisibleRole(connection, apiPrimaryKey, api.getVisibleRoles());
            }

            String wsdlUri = api.getWsdlUri();

            if (wsdlUri != null) {
                ArtifactResource resource = new ArtifactResource.Builder().
                                            id(UUID.randomUUID().toString()).
                                            name(ResourceCategory.WSDL_URI.toString()).
                                            category(ResourceCategory.WSDL_URI).
                                            dataType(MediaType.TEXT_PLAIN).
                                            textValue(wsdlUri).
                                            visibility(ResourceVisibility.RESOURCE_LEVEL).build();

                ApiResourceDAO.addResource(connection, apiPrimaryKey, resource);
            }
            addTagsMapping(connection, apiPrimaryKey, api.getTags());
            addAPIDefinition(connection, apiPrimaryKey, api.getApiDefinition(),
                                                                    ResourceVisibility.RESOURCE_LEVEL.toString());
            addTransports(connection, apiPrimaryKey, api.getTransport());
            addUrlMappings(connection, api.getUriTemplates(), apiPrimaryKey);
            addSubscriptionPolicies(connection, api.getPolicies(), apiPrimaryKey);

            connection.commit();
        } catch (SQLException e) {
            throw new APIMgtDAOException(e);
        }
    }

    /**
     * Update an existing API
     *
     * @param apiID      The {@link String} of the API that needs to be updated
     * @param substituteAPI Substitute {@link API} object that will replace the existing API
     * @return true if update is successful else false
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    @Override
    public API updateAPI(String apiID, API substituteAPI) throws APIMgtDAOException {
        final String query = "UPDATE AM_API SET IS_DEFAULT_VERSION = ?, DESCRIPTION = ?, VISIBILITY = ?, " +
                "IS_RESPONSE_CACHED = ?, CACHE_TIMEOUT = ?, TECHNICAL_OWNER = ?, TECHNICAL_EMAIL = ?, " +
                "BUSINESS_OWNER = ?, BUSINESS_EMAIL = ?, CORS_ENABLED = ?, CORS_ALLOW_ORIGINS = ?, " +
                "CORS_ALLOW_CREDENTIALS = ?, CORS_ALLOW_HEADERS = ?, CORS_ALLOW_METHODS = ?, LAST_UPDATED_TIME = ? " +
                "WHERE UUID = ?";

        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setBoolean(1, substituteAPI.isDefaultVersion());
            statement.setString(2, substituteAPI.getDescription());
            statement.setString(3, substituteAPI.getVisibility().toString());
            statement.setBoolean(4, substituteAPI.isResponseCachingEnabled());
            statement.setInt(5, substituteAPI.getCacheTimeout());

            BusinessInformation businessInformation = substituteAPI.getBusinessInformation();
            statement.setString(6, businessInformation.getTechnicalOwner());
            statement.setString(7, businessInformation.getTechnicalOwnerEmail());
            statement.setString(8, businessInformation.getBusinessOwner());
            statement.setString(9, businessInformation.getBusinessOwnerEmail());

            CorsConfiguration corsConfiguration = substituteAPI.getCorsConfiguration();
            statement.setBoolean(10, corsConfiguration.isEnabled());
            statement.setString(11, String.join(",", corsConfiguration.getAllowOrigins()));
            statement.setBoolean(12, corsConfiguration.isAllowCredentials());
            statement.setString(13, String.join(",", corsConfiguration.getAllowHeaders()));
            statement.setString(14, String.join(",", corsConfiguration.getAllowMethods()));

            statement.setTimestamp(15, Timestamp.valueOf(substituteAPI.getLastUpdatedTime()));
            statement.setString(16, apiID);

            statement.execute();

            deleteVisibleRoles(connection, apiID); // Delete current visible roles if they exist

            if (API.Visibility.RESTRICTED == substituteAPI.getVisibility()) {
                addVisibleRole(connection, apiID, substituteAPI.getVisibleRoles());
            }

            String wsdlUri = substituteAPI.getWsdlUri();
            if (wsdlUri.isEmpty()) {
                ApiResourceDAO.deleteUniqueResourceForCategory(connection, apiID, ResourceCategory.WSDL_URI.toString());
            } else {
                if (!ApiResourceDAO.isResourceExistsForCategory(connection, apiID,
                                                                    ResourceCategory.WSDL_URI.toString())) {
                    ArtifactResource resource = new ArtifactResource.Builder().
                            id(UUID.randomUUID().toString()).
                            name(ResourceCategory.WSDL_URI.toString()).
                            category(ResourceCategory.WSDL_URI).
                            dataType(MediaType.TEXT_PLAIN).
                            textValue(wsdlUri).
                            visibility(ResourceVisibility.RESOURCE_LEVEL).build();

                    ApiResourceDAO.addResource(connection, apiID, resource);
                } else {
                    ApiResourceDAO.updateTextValueForCategory(connection, apiID,
                                                                        ResourceCategory.WSDL_URI.toString(), wsdlUri);
                }
            }

            deleteTransports(connection, apiID);
            addTransports(connection, apiID, substituteAPI.getTransport());

            deleteTagsMapping(connection, apiID); // Delete current tag mappings if they exist
            addTagsMapping(connection, apiID, substituteAPI.getTags());

            updateAPIDefinition(connection, apiID, substituteAPI.getApiDefinition());
            deleteSubscriptionPolicies(connection, apiID);
            addSubscriptionPolicies(connection, substituteAPI.getPolicies(), apiID);
            deleteUrlMappings(connection, apiID);
            addUrlMappings(connection, substituteAPI.getUriTemplates(), apiID);
            connection.commit();
        } catch (SQLException e) {
            throw new APIMgtDAOException(e);
        }

        return null;
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
            statement.setString(1, apiID);
            statement.execute();
            connection.commit();
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
    @Override public String getSwaggerDefinition(String apiID) throws APIMgtDAOException {
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
    @Override public void updateSwaggerDefinition(String apiID, String swaggerDefinition) throws APIMgtDAOException {
        try {
            Connection connection = DAOUtil.getConnection();
            updateAPIDefinition(connection, apiID, swaggerDefinition);
        } catch (SQLException e) {
            throw new APIMgtDAOException("Data access error when updating API definition", e);
        }
    }

    /**
     * Get image of a given API
     *
     * @param apiID The UUID of the respective API
     * @return Image stream
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    @Override public InputStream getImage(String apiID) throws APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection()) {
            return ApiResourceDAO.getBinaryValueForCategory(connection, apiID, ResourceCategory.IMAGE.toString());
        } catch (SQLException e) {
            throw new APIMgtDAOException("Couldn't retrieve api thumbnail for api " + apiID, e);
        }
    }

    /**
     * Update image of a given API
     *
     * @param apiID The UUID of the respective API
     * @param image Image stream
     * @param dataType Data Type of image
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    @Override
    public void updateImage(String apiID, InputStream image, String dataType) throws APIMgtDAOException {
        if (image != null) {
            try (Connection connection = DAOUtil.getConnection()) {

                if (!ApiResourceDAO.isResourceExistsForCategory(connection, apiID,
                                                                    ResourceCategory.WSDL_URI.toString())) {
                    ArtifactResource resource = new ArtifactResource.Builder().
                            id(UUID.randomUUID().toString()).
                            name(ResourceCategory.IMAGE.toString()).
                            category(ResourceCategory.IMAGE).
                            dataType(dataType).
                            visibility(ResourceVisibility.RESOURCE_LEVEL).build();

                    ApiResourceDAO.addUniqueBinaryResourceForCategory(connection, apiID, resource, image);
                } else {
                    ApiResourceDAO.updateUniqueBinaryResourceForCategory(connection, apiID,
                                                                    ResourceCategory.IMAGE.toString(), image);
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
            statement.setString(1, status);
            statement.setString(2, apiID);
            statement.execute();
        } catch (SQLException e) {
            throw new APIMgtDAOException(e);
        }
    }

    /**
     * Return list of all Document info belonging to a given API.
     *
     * @param apiID  The UUID of the respective API
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    @Override
    public List<ArtifactResource> getDocumentsInfoList(String apiID) throws APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection()) {
            return ApiResourceDAO.getDocResourceMetaDataList(connection, apiID);
        } catch (SQLException e) {
            throw new APIMgtDAOException(e);
        }
    }

    /**
     * Return Document info object
     *
     * @param resourceID The UUID of the respective Document
     * @return {@link ArtifactResource} Resource meta data
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    @Override
    @CheckForNull
    public ArtifactResource getResource(String resourceID) throws APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection()) {
            return ApiResourceDAO.getResource(connection, resourceID);
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
    public InputStream getBinaryResourceContent(String resourceID) throws APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection()) {
            return ApiResourceDAO.getBinaryResource(connection, resourceID);
        } catch (SQLException e) {
            throw new APIMgtDAOException(e);
        }
    }

    /**
     * Add artifact resource meta data to an API
     *
     * @param apiId    UUID of API
     * @param resource {@link ArtifactResource}
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    @Override
    public void addArtifactResource(String apiId, ArtifactResource resource) throws APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection()) {
            ApiResourceDAO.addResource(connection, apiId, resource);
        } catch (SQLException e) {
            throw new APIMgtDAOException(e);
        }
    }

    /**
     * Update binary resource
     *
     * @param resourceID         UUID of resource
     * @param content                Binary content as an Input Stream
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    @Override
    public void updateBinaryResourceContent(String resourceID, InputStream content) throws APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection()) {
            ApiResourceDAO.updateBinaryResource(connection, resourceID, content);
        } catch (SQLException e) {
            throw new APIMgtDAOException(e);
        }
    }

    /**
     * Update text resource
     *
     * @param resourceID UUID of resource
     * @param resource {@link ArtifactResource}
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    @Override
    public void updateArtifactResource(String resourceID, ArtifactResource resource) throws APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection()) {
            ApiResourceDAO.updateResource(connection, resourceID, resource);
        } catch (SQLException e) {
            throw new APIMgtDAOException(e);
        }
    }

    /**
     * Delete a resource
     *
     * @param resourceID   UUID of resource
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    @Override
    public void deleteResource(String resourceID) throws APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection()) {
            ApiResourceDAO.deleteResource(connection, resourceID);
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
                                        ResourceCategory.WSDL_URI.toString())).
                        transport(getTransports(connection, apiPrimaryKey)).
                        apiDefinition(getAPIDefinition(connection, apiPrimaryKey)).
                        businessInformation(businessInformation).
                        lifecycleInstanceId(rs.getString("LIFECYCLE_INSTANCE_ID")).
                        lifeCycleStatus(rs.getString("CURRENT_LC_STATUS")).
                        corsConfiguration(corsConfiguration).
                        createdBy(rs.getString("CREATED_BY")).
                        createdTime(rs.getTimestamp("CREATED_TIME").toLocalDateTime()).
                        lastUpdatedTime(rs.getTimestamp("LAST_UPDATED_TIME").toLocalDateTime()).
                        uriTemplates(getUriTemplates(connection, apiPrimaryKey)).
                        policies(getSubscripitonPolciesByAPIId(connection, apiPrimaryKey)).
                        build();
            }
        }

        return null;
    }

    private API constructAPISummary(PreparedStatement statement) throws SQLException {
        try (ResultSet rs = statement.executeQuery()) {
            if (rs.next()) {
                return new API.APIBuilder(rs.getString("PROVIDER"), rs.getString("NAME"),
                        rs.getString("VERSION")).
                        id(rs.getString("UUID")).
                        context(rs.getString("CONTEXT")).
                        description(rs.getString("DESCRIPTION")).
                        lifeCycleStatus(rs.getString("CURRENT_LC_STATUS")).lifecycleInstanceId(rs.getString
                        ("LIFECYCLE_INSTANCE_ID")).build();
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
                        lifeCycleStatus(rs.getString("CURRENT_LC_STATUS")).build();

                apiList.add(apiSummary);
            }
        }

        return apiList;
    }

    private void addTagsMapping(Connection connection, String apiID, List<String> tags) throws SQLException {
        if (!tags.isEmpty()) {
            List<Integer> tagIDs = TagDAO.addTagsIfNotExist(connection, tags);

            final String query = "INSERT INTO AM_API_TAG_MAPPING (API_ID, TAG_ID) VALUES (?, ?)";

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                for (Integer tagID : tagIDs) {
                    statement.setString(1, apiID);
                    statement.setInt(2, tagID);
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
                List<Integer> tagIDs = new ArrayList<>();

                while (rs.next()) {
                    tagIDs.add(rs.getInt("TAG_ID"));
                }

                if (!tagIDs.isEmpty()) {
                    tags = TagDAO.getTagsByIDs(connection, tagIDs);
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

    private void addAPIDefinition(Connection connection, String apiID, String apiDefinition, String visibility)
                                                                                                throws SQLException {
        if (!apiDefinition.isEmpty()) {
            ArtifactResource resource = new ArtifactResource.Builder().
                    id(UUID.randomUUID().toString()).
                    name(ResourceCategory.SWAGGER.toString()).
                    category(ResourceCategory.SWAGGER).
                    dataType(MediaType.APPLICATION_JSON).
                    visibility(ResourceVisibility.RESOURCE_LEVEL).build();

            ApiResourceDAO.addUniqueBinaryResourceForCategory(connection, apiID, resource,
                    new ByteArrayInputStream(apiDefinition.getBytes(StandardCharsets.UTF_8)));
        }
    }

    private void updateAPIDefinition(Connection connection, String apiID, String apiDefinition) throws SQLException {
        ApiResourceDAO.updateUniqueBinaryResourceForCategory(connection, apiID, ResourceCategory.SWAGGER.toString(),
                                        new ByteArrayInputStream(apiDefinition.getBytes(StandardCharsets.UTF_8)));
    }

    private String getAPIDefinition(Connection connection, String apiID) throws SQLException, IOException {
        InputStream apiDefinition = ApiResourceDAO.getBinaryValueForCategory(connection, apiID,
                                                                ResourceCategory.SWAGGER.toString());

        return IOUtils.toString(apiDefinition, StandardCharsets.UTF_8);
    }

    private int getAPIThrottlePolicyID(Connection connection, String policyName) throws SQLException {
        final String query = "SELECT UUID FROM AM_API_POLICY WHERE NAME = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, policyName);
            statement.execute();

            try (ResultSet rs = statement.getResultSet()) {
                if (rs.next()) {
                    return rs.getInt("UUID");
                }
            }
        }

        return -1;
    }

    private String getAPIThrottlePolicyName(Connection connection, int policyID) throws SQLException {
        final String query = "SELECT NAME FROM AM_API_POLICY WHERE UUID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, policyID);
            statement.execute();

            try (ResultSet rs = statement.getResultSet()) {
                if (rs.next()) {
                    return rs.getString("NAME");
                }
            }
        }

        return "";
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

    private void addUrlMappings(Connection connection, Set<UriTemplate> uriTemplates, String apiID)
            throws SQLException {
        final String query = "INSERT INTO AM_API_URL_MAPPING (API_ID, HTTP_METHOD, URL_PATTERN, "
                + "AUTH_SCHEME, API_POLICY_ID) VALUES (?,?,?,?,?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            for (UriTemplate uriTemplate : uriTemplates) {
                statement.setString(1, apiID);
                statement.setString(2, uriTemplate.getHttpVerb());
                statement.setString(3, uriTemplate.getUriTemplate());
                statement.setString(4, uriTemplate.getAuthType());
                statement.setInt(5, getAPIThrottlePolicyID(connection, uriTemplate.getPolicy()));
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }
    private void deleteUrlMappings(Connection connection, String apiID) throws
            SQLException {
        final String query = "DELETE FROM AM_API_URL_MAPPING WHERE API_ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, apiID);
            statement.execute();
        }
    }

    private Set<UriTemplate> getUriTemplates(Connection connection, String apiId) throws SQLException {
        String query = "SELECT API_ID,HTTP_METHOD,URL_PATTERN,AUTH_SCHEME,API_POLICY_ID FROM AM_API_URL_MAPPING WHERE"
                + " API_ID = ?";
        Set<UriTemplate> uriTemplateSet = new HashSet<>();
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, apiId);
            statement.execute();
            try (ResultSet rs = statement.getResultSet()) {
                while (rs.next()) {
                    UriTemplate uriTemplate = new UriTemplate.UriTemplateBuilder()
                            .uriTemplate(rs.getString("URL_PATTERN")).authType(rs.getString("AUTH_SCHEME"))
                            .httpVerb(rs.getString("HTTP_METHOD"))
                            .policy(getAPIThrottlePolicyName(connection, rs.getInt("API_POLICY_ID"))).build();
                    uriTemplateSet.add(uriTemplate);
                }
            }
        }
        return uriTemplateSet;
    }

    private void addSubscriptionPolicies(Connection connection, List<String> policies, String apiID)
            throws SQLException {
        final String query =
                "INSERT INTO AM_API_SUBSCRIPTION_POLICY_MAPPING (API_ID, SUBSCRIPTION_POLICY_ID) " + "VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            for (String policy : policies) {
                statement.setString(1, apiID);
                statement.setInt(2, getSubscriptionThrottlePolicyID(connection, policy));
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void deleteSubscriptionPolicies(Connection connection, String apiID) throws SQLException {
        final String query = "DELETE FROM AM_API_SUBSCRIPTION_POLICY_MAPPING WHERE API_ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, apiID);
            statement.execute();
        }
    }

    private int getSubscriptionThrottlePolicyID(Connection connection, String policyName) throws SQLException {
        final String query = "SELECT UUID from AM_SUBSCRIPTION_POLICY where NAME=?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, policyName);
            statement.execute();

            try (ResultSet rs = statement.getResultSet()) {
                if (rs.next()) {
                    return rs.getInt("UUID");
                }
            }
        }

        return -1;
    }

    private List<String> getSubscripitonPolciesByAPIId(Connection connection, String apiId) throws SQLException {
        final String query = "SELECT amPolcySub.NAME FROM AM_API_SUBSCRIPTION_POLICY_MAPPING as apimsubmapping," +
                "AM_SUBSCRIPTION_POLICY as amPolcySub where apimsubmapping.SUBSCRIPTION_POLICY_ID=amPolcySub.UUID " +
                "AND apimsubmapping.API_ID = ?";
        List<String> policies = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, apiId);
            statement.execute();

            try (ResultSet rs = statement.getResultSet()) {
                if (rs.next()) {
                    policies.add(rs.getString("NAME"));
                }
            }
        }

        return policies;
    }

    void initResourceCategories() throws APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection()) {
            if (!ResourceCategoryDAO.isStandardResourceCategoriesExist(connection)) {
                ResourceCategoryDAO.addResourceCategories(connection);
                connection.commit();
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException(e);
        }
    }
}

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
import org.wso2.carbon.apimgt.core.models.*;

import javax.annotation.CheckForNull;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

/**
 * Default implementation of the ApiDAO interface. Uses SQL syntax that is common to H2 and MySQL DBs.
 * Hence is considered as the default due to its re-usability.
 */
public class ApiDAOImpl implements ApiDAO {

    //private final ApiDAOVendorSpecificStatements sqlStatements;
    private static final String API_SUMMARY_SELECT = "SELECT API_ID, PROVIDER, NAME, CONTEXT, VERSION, DESCRIPTION, " +
            "UUID, CURRENT_LC_STATUS, LIFECYCLE_INSTANCE_ID FROM AM_API";

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
        final String query = "SELECT API_ID, PROVIDER, NAME, CONTEXT, VERSION, IS_DEFAULT_VERSION, DESCRIPTION, " +
                "VISIBILITY, IS_RESPONSE_CACHED, CACHE_TIMEOUT, UUID, TECHNICAL_OWNER, TECHNICAL_EMAIL, " +
                "BUSINESS_OWNER, BUSINESS_EMAIL, LIFECYCLE_INSTANCE_ID, CURRENT_LC_STATUS, " +
                "CORS_ENABLED, CORS_ALLOW_ORIGINS, CORS_ALLOW_CREDENTIALS, CORS_ALLOW_HEADERS, CORS_ALLOW_METHODS, " +
                "CREATED_BY, CREATED_TIME, LAST_UPDATED_TIME FROM AM_API WHERE UUID = ?";

        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, apiID);

            return constructAPIFromResultSet(connection, statement);
        } catch (SQLException e) {
            throw new APIMgtDAOException("Data access error when getting API", e);
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
            throw new APIMgtDAOException("Data access error when getting API", e);
        }
    }

    /**
     * Retrieves summary data of all available APIs.
     * @return {@link List<API>} matching results
     * @throws SQLException if error occurs while accessing data layer
     *
     */
    @Override
    @SuppressFBWarnings("SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING")
    public List<API> getAPIs() throws APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection();
            PreparedStatement statement = connection.prepareStatement(API_SUMMARY_SELECT)) {

            return constructAPISummaryList(statement);
        } catch (SQLException e) {
            throw new APIMgtDAOException("Data access error when getting APIs", e);
        }
    }

    /**
     * Retrieves summary data of all available APIs of a given provider.
     * @param providerName A given API Provider
     * @return {@link List<API>} matching results
     * @throws APIMgtDAOException if error occurs while accessing data layer
     *
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
            throw new APIMgtDAOException("Data access error when getting APIs", e);
        }
    }

    /**
     * Retrieves summary data of all available APIs with life cycle status that matches the status list provided
     * @param statuses A list of matching life cycle statuses
     * @return {@link List<API>} matching results
     * @throws APIMgtDAOException if error occurs while accessing data layer
     *
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
            throw new APIMgtDAOException("Data access error when getting APIs", e);
        }
    }

    /**
     * Retrieves summary data of all available APIs that match the given search criteria.
     * @param searchString The search string provided
     * @return {@link List<API>} matching results
     * @throws APIMgtDAOException if error occurs while accessing data layer
     *
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
            throw new APIMgtDAOException("Data access error when searching APIs", e);
        }
    }

    /**
     * Checks if a given API which is uniquely identified by the API Name  already
     * exists
     *
     * @param apiName      Name of API
     * @return true if  apiName combination already exists else false
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    @Override
    public boolean isAPINameExists(String apiName) throws APIMgtDAOException {
        final String apiExistsQuery = "SELECT API_ID FROM AM_API WHERE NAME = ?";

        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(apiExistsQuery)) {
            statement.setString(1, apiName);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return true;
                }
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException("Data access error when checking if API name exists", e);
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
        final String apiExistsQuery = "SELECT API_ID FROM AM_API WHERE CONTEXT = ?";

        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(apiExistsQuery)) {
            statement.setString(1, contextName);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return true;
                }
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException("Data access error when checking if API context exists", e);
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
             PreparedStatement statement = connection.prepareStatement(addAPIQuery, new String[]{"api_id"})) {
            statement.setString(1, api.getProvider());
            statement.setString(2, api.getName());
            statement.setString(3, api.getContext());
            statement.setString(4, api.getVersion());
            statement.setBoolean(5, api.isDefaultVersion());
            statement.setString(6, api.getDescription());
            statement.setString(7, api.getVisibility().toString());
            statement.setBoolean(8, api.isResponseCachingEnabled());
            statement.setInt(9, api.getCacheTimeout());
            statement.setString(10, api.getId());

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

            try (ResultSet rs = statement.getGeneratedKeys()) {
                if (rs.next()) {
                    int apiPrimaryKey = rs.getInt(1);

                    if (API.Visibility.RESTRICTED == api.getVisibility()) {
                        addVisibleRole(connection, apiPrimaryKey, api.getVisibleRoles());
                    }

                    addWsdlURI(connection, apiPrimaryKey, api.getWsdlUri());
                    addTagsMapping(connection, apiPrimaryKey, api.getTags());
                    addAPIDefinition(connection, apiPrimaryKey, api.getApiDefinition());
                    addTransports(connection, apiPrimaryKey, api.getTransport());
                    addUrlMappings(connection,api.getUriTemplates(),apiPrimaryKey);
                    addSubscriptionPolicies(connection,api.getPolicies(),apiPrimaryKey);
                }
            }

            connection.commit();
        } catch (SQLException e) {
            throw new APIMgtDAOException("Data access error when adding API", e);
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
                "IS_RESPONSE_CACHED = ?, CACHE_TIMEOUT = ?, UUID = ?, TECHNICAL_OWNER = ?, TECHNICAL_EMAIL = ?, " +
                "BUSINESS_OWNER = ?, BUSINESS_EMAIL = ?, CORS_ENABLED = ?, CORS_ALLOW_ORIGINS = ?, " +
                "CORS_ALLOW_CREDENTIALS = ?, CORS_ALLOW_HEADERS = ?, CORS_ALLOW_METHODS = ?, LAST_UPDATED_TIME = ? " +
                "WHERE UUID = ?";

        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            int apiPrimaryKey = getAPIPrimaryKey(connection, apiID);

            statement.setBoolean(1, substituteAPI.isDefaultVersion());
            statement.setString(2, substituteAPI.getDescription());
            statement.setString(3, substituteAPI.getVisibility().toString());
            statement.setBoolean(4, substituteAPI.isResponseCachingEnabled());
            statement.setInt(5, substituteAPI.getCacheTimeout());
            statement.setString(6, substituteAPI.getId());

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

            deleteVisibleRoles(connection, apiPrimaryKey); // Delete current visible roles if they exist

            if (API.Visibility.RESTRICTED == substituteAPI.getVisibility()) {
                addVisibleRole(connection, apiPrimaryKey, substituteAPI.getVisibleRoles());
            }

            String wsdlUri = substituteAPI.getWsdlUri();
            if (wsdlUri.isEmpty()) {
                deleteWsdlURI(connection, apiPrimaryKey);
            }
            else {
                if (getWsdlURI(connection, apiPrimaryKey).isEmpty()) {
                    addWsdlURI(connection, apiPrimaryKey, wsdlUri);
                }
                else {
                    updateWsdlURI(connection, apiPrimaryKey, wsdlUri);
                }
            }

            deleteTransports(connection, apiPrimaryKey);
            addTransports(connection, apiPrimaryKey, substituteAPI.getTransport());

            deleteTagsMapping(connection, apiPrimaryKey); // Delete current tag mappings if they exist
            addTagsMapping(connection, apiPrimaryKey, substituteAPI.getTags());

            updateAPIDefinition(connection, apiPrimaryKey, substituteAPI.getApiDefinition());
            deleteSubscriptionPolicies(connection, apiPrimaryKey);
            addSubscriptionPolicies(connection, substituteAPI.getPolicies(), apiPrimaryKey);
            deleteUrlMappings(connection, apiPrimaryKey);
            addUrlMappings(connection, substituteAPI.getUriTemplates(), apiPrimaryKey);
            connection.commit();
        } catch (SQLException e) {
            throw new APIMgtDAOException("Data access error when updating API", e);
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
            throw new APIMgtDAOException("Data access error when deleting API", e);
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
        return null;
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
        return null;
    }

    /**
     * Update swagger definition of a given API
     *
     * @param apiID The UUID of the respective API
     * @param image Image stream
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    @Override
    public void updateImage(String apiID, OutputStream image) throws APIMgtDAOException {

    }

    /**
     * Change the lifecycle status of a given API
     *
     * @param apiID                     The UUID of the respective API
     * @param status                    The lifecycle status that the API must be set to
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    @Override
    public void changeLifeCycleStatus(String apiID, String status) throws APIMgtDAOException {
    final String query = "UPDATE AM_API SET CURRENT_LC_STATUS = ? WHERE UUID = ?";
        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1,status);
            statement.setString(2,apiID);
            statement.execute();
        } catch (SQLException e) {
            throw new APIMgtDAOException("Data access error when changing life cycle status", e);
        }
    }

    /**
     * Return list of all Document info belonging to a given API. This method supports result pagination
     *
     * @param apiID  The UUID of the respective API
     * @param offset The number of results from the beginning that is to be ignored
     * @param limit  The maximum number of results to be returned after the offset
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    @Override
    public DocumentInfoResults getDocumentsInfoList(String apiID, int offset, int limit)
                                                                    throws APIMgtDAOException {
        return null;
    }

    /**
     * Return Document info object
     * @param docID The UUID of the respective Document
     * @return {@link DocumentInfo} Document Info object
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    @Override
    public DocumentInfo getDocumentInfo(String docID) throws APIMgtDAOException {
        //todo:implement
        return null;
    }

    /**
     * @param docID The UUID of the respective Document
     * @return {@link InputStream} Document Info object
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    @Override
    public InputStream getDocumentContent(String docID) throws APIMgtDAOException {
        return null;
    }

    /**
     * Attach Documentation (without content) to an API
     *
     * @param apiId         UUID of API
     * @param documentation Documentat Summary
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    @Override
    public void addDocumentationInfo(String apiId, DocumentInfo documentation) throws APIMgtDAOException {

    }

    /**
     * Add a document (of source type FILE) with a file
     *
     * @param apiId         UUID of API
     * @param documentation Document Summary
     * @param filename      name of the file
     * @param content       content of the file as an Input Stream
     * @param contentType   content type of the file
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    @Override
    public void addDocumentationWithFile(String apiId, DocumentInfo documentation, String filename, InputStream content, String contentType) throws APIMgtDAOException {

    }

    /**
     * Removes a given documentation
     *
     * @param id Document Id
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    @Override
    public void removeDocumentation(String id) throws APIMgtDAOException {

    }

    /**
     * Used to deprecate older versions of the api
     * @param identifier
     */
    @Override
    public void deprecateOlderVersions(String identifier) {
        /**
         * todo:
         */
    }

    private API constructAPIFromResultSet(Connection connection, PreparedStatement statement) throws SQLException {
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

                int apiPrimaryKey = rs.getInt("API_ID");

                return new API.APIBuilder(rs.getString("PROVIDER"), rs.getString("NAME"), rs.getString("VERSION")).
                        id(rs.getString("UUID")).
                        context(rs.getString("CONTEXT")).
                        isDefaultVersion(rs.getBoolean("IS_DEFAULT_VERSION")).
                        description(rs.getString("DESCRIPTION")).
                        visibility(API.Visibility.valueOf(rs.getString("VISIBILITY"))).
                        visibleRoles(getVisibleRoles(connection, apiPrimaryKey)).
                        isResponseCachingEnabled(rs.getBoolean("IS_RESPONSE_CACHED")).
                        cacheTimeout(rs.getInt("CACHE_TIMEOUT")).
                        tags(getTags(connection, apiPrimaryKey)).
                        wsdlUri(getWsdlURI(connection, apiPrimaryKey)).
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

    private void addTagsMapping(Connection connection, int apiID, List<String> tags) throws SQLException {
        if (!tags.isEmpty()) {
            List<Integer> tagIDs = TagDAO.addTagsIfNotExist(connection, tags);

            final String query = "INSERT INTO AM_API_TAG_MAPPING (API_ID, TAG_ID) VALUES (?, ?)";

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                for (Integer tagID : tagIDs) {
                    statement.setInt(1, apiID);
                    statement.setInt(2, tagID);
                    statement.addBatch();
                }

                statement.executeBatch();
            }
        }
    }

    private void deleteTagsMapping(Connection connection, int apiID) throws SQLException {
        final String query = "DELETE FROM AM_API_TAG_MAPPING WHERE API_ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, apiID);
            statement.execute();
        }
    }

    private List<String> getTags(Connection connection, int apiID) throws SQLException {
        List<String> tags = new ArrayList<>();

        final String query = "SELECT TAG_ID FROM AM_API_TAG_MAPPING WHERE API_ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, apiID);
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

    private void addVisibleRole(Connection connection, int apiID, List<String> roles) throws SQLException {
        final String query = "INSERT INTO AM_API_VISIBLE_ROLES (API_ID, ROLE) VALUES (?,?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            for (String role : roles) {
                statement.setInt(1, apiID);
                statement.setString(2, role);
                statement.addBatch();
            }

            statement.executeBatch();
        }
    }

    private void deleteVisibleRoles(Connection connection, int apiID) throws SQLException {
        final String query =  "DELETE FROM AM_API_VISIBLE_ROLES WHERE API_ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, apiID);
            statement.execute();
        }
    }

    private List<String> getVisibleRoles(Connection connection, int apiID) throws SQLException {
        List<String> roles =  new ArrayList<>();

        final String query = "SELECT ROLE FROM AM_API_VISIBLE_ROLES WHERE API_ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, apiID);
            statement.execute();

            try (ResultSet rs = statement.getResultSet()) {
                while (rs.next()) {
                    roles.add(rs.getString("ROLE"));
                }
            }
        }

        return roles;
    }

    private void addAPIDefinition(Connection connection, int apiID, String apiDefinition) throws SQLException {
        if (!apiDefinition.isEmpty()) {
            int resourceTypeID = getSwaggerResourceTypeID(connection);

            final String query = "INSERT INTO AM_API_RESOURCES (API_ID, RESOURCE_TYPE_ID, DATA_TYPE, " +
                    "RESOURCE_BINARY_VALUE) VALUES (?,?,?,?)";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, apiID);
                statement.setInt(2, resourceTypeID);
                statement.setString(3, MediaType.APPLICATION_JSON);
                statement.setBlob(4, new ByteArrayInputStream(apiDefinition.getBytes(StandardCharsets.UTF_8)));

                statement.execute();
            }
        }
    }

    private void updateAPIDefinition(Connection connection, int apiID, String apiDefinition) throws SQLException {
        int resourceTypeID = getSwaggerResourceTypeID(connection);

        final String query = "UPDATE AM_API_RESOURCES SET RESOURCE_BINARY_VALUE = ? WHERE " +
                "API_ID = ? AND RESOURCE_TYPE_ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setBlob(1, new ByteArrayInputStream(apiDefinition.getBytes(StandardCharsets.UTF_8)));
            statement.setInt(2, apiID);
            statement.setInt(3, resourceTypeID);

            statement.execute();
        }
    }

    private String getAPIDefinition(Connection connection, int apiID) throws SQLException {
        int resourceTypeID = getSwaggerResourceTypeID(connection);

        final String query = "SELECT RESOURCE_BINARY_VALUE FROM AM_API_RESOURCES WHERE API_ID = ? AND " +
                "RESOURCE_TYPE_ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, apiID);
            statement.setInt(2, resourceTypeID);
            statement.execute();

            try (ResultSet rs =  statement.getResultSet()) {
                if (rs.next()) {
                    try {
                        return IOUtils.toString(rs.getBlob("RESOURCE_BINARY_VALUE").
                                getBinaryStream(), StandardCharsets.UTF_8);
                    } catch (IOException e) {
                        throw new SQLException("IO Error while reading API definition", e);
                    }
                }
            }
        }

        return "";
    }

    private void addWsdlURI(Connection connection, int apiID, String wsdlURI) throws SQLException {
        if (!wsdlURI.isEmpty()) {
            int resourceTypeID = getWSDLResourceTypeID(connection);

            final String query = "INSERT INTO AM_API_RESOURCES (API_ID, RESOURCE_TYPE_ID, DATA_TYPE, " +
                    "RESOURCE_TEXT_VALUE) VALUES (?,?,?,?)";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, apiID);
                statement.setInt(2, resourceTypeID);
                statement.setString(3, MediaType.TEXT_PLAIN);
                statement.setString(4, wsdlURI);

                statement.execute();
            }
        }
    }

    private void updateWsdlURI(Connection connection, int apiID, String wsdlURI) throws SQLException {
        int resourceTypeID = getWSDLResourceTypeID(connection);

        final String query = "UPDATE AM_API_RESOURCES SET RESOURCE_TEXT_VALUE = ? WHERE " +
                "API_ID = ? AND RESOURCE_TYPE_ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, wsdlURI);
            statement.setInt(2, apiID);
            statement.setInt(3, resourceTypeID);

            statement.execute();
        }
    }

    private void deleteWsdlURI(Connection connection, int apiID) throws SQLException {
        int resourceTypeID = getWSDLResourceTypeID(connection);

        final String query = "DELETE FROM AM_API_RESOURCES WHERE API_ID = ? AND RESOURCE_TYPE_ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, apiID);
            statement.setInt(2, resourceTypeID);

            statement.execute();
        }
    }

    private String getWsdlURI(Connection connection, int apiID) throws SQLException {
        int resourceTypeID = getWSDLResourceTypeID(connection);

        final String query = "SELECT RESOURCE_TEXT_VALUE FROM AM_API_RESOURCES WHERE API_ID = ? AND " +
                "RESOURCE_TYPE_ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, apiID);
            statement.setInt(2, resourceTypeID);
            statement.execute();

            try (ResultSet rs =  statement.getResultSet()) {
                if (rs.next()) {
                    return rs.getString("RESOURCE_TEXT_VALUE");
                }
            }
        }

        return "";
    }

    private int getSwaggerResourceTypeID(Connection connection) throws SQLException {
        int resourceTypeID = ResourceTypeDAO.getResourceTypeID(connection,
                ResourceConstants.ResourceType.SWAGGER.toString());

        if (resourceTypeID == -1) { // If resource type does not already exist
            ResourceTypeDAO.addResourceType(connection, ResourceConstants.ResourceType.SWAGGER.toString());
            resourceTypeID = ResourceTypeDAO.getResourceTypeID(connection,
                    ResourceConstants.ResourceType.SWAGGER.toString());
        }

        return resourceTypeID;
    }

    private int getWSDLResourceTypeID(Connection connection) throws SQLException {
        int resourceTypeID = ResourceTypeDAO.getResourceTypeID(connection,
                ResourceConstants.ResourceType.WSDL_URI.toString());

        if (resourceTypeID == -1) { // If resource type does not already exist
            ResourceTypeDAO.addResourceType(connection, ResourceConstants.ResourceType.WSDL_URI.toString());
            resourceTypeID = ResourceTypeDAO.getResourceTypeID(connection,
                    ResourceConstants.ResourceType.WSDL_URI.toString());
        }

        return resourceTypeID;
    }

    private int getAPIThrottlePolicyID(Connection connection, String policyName) throws SQLException {
        final String query = "SELECT POLICY_ID FROM AM_API_THROTTLE_POLICY WHERE NAME = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, policyName);
            statement.execute();

            try (ResultSet rs = statement.getResultSet()) {
                if (rs.next()) {
                    return rs.getInt("POLICY_ID");
                }
            }
        }

        return -1;
    }

    private String getAPIThrottlePolicyName(Connection connection, int policyID) throws SQLException {
        final String query = "SELECT NAME FROM AM_API_THROTTLE_POLICY WHERE POLICY_ID = ?";
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

    private void addTransports(Connection connection, int apiID, List<String> transports) throws SQLException {
        final String query = "INSERT INTO AM_API_TRANSPORTS (API_ID, TRANSPORT) VALUES (?,?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            for (String transport : transports) {
                statement.setInt(1, apiID);
                statement.setString(2, transport);

                statement.addBatch();
            }
            statement.executeBatch();

        }
    }

    private void deleteTransports(Connection connection, int apiID) throws SQLException {
        final String query = "DELETE FROM AM_API_TRANSPORTS WHERE API_ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, apiID);
            statement.execute();
        }
    }

    private List<String> getTransports(Connection connection, int apiID) throws SQLException {
        List<String> transports = new ArrayList<>();

        final String query = "SELECT TRANSPORT FROM AM_API_TRANSPORTS WHERE API_ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, apiID);
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

    private int getAPIPrimaryKey(Connection connection, String apiID) throws SQLException {
        final String query = "SELECT API_ID FROM AM_API WHERE UUID = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, apiID);
            statement.execute();

            try (ResultSet rs = statement.getResultSet()) {
                if (rs.next()) {
                    return rs.getInt("API_ID");
                }
            }
        }

        return -1;
    }

    private void addUrlMappings(Connection connection, Set<URITemplate> uriTemplates, int apiID) throws
            SQLException {
        final String query = "INSERT INTO AM_API_URL_MAPPING (API_ID, HTTP_METHOD, URL_PATTERN, " +
                "AUTH_SCHEME, API_POLICY_ID) VALUES (?,?,?,?,?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            for (URITemplate uriTemplate : uriTemplates) {
                statement.setInt(1, apiID);
                statement.setString(2, uriTemplate.getHttpVerb());
                statement.setString(3, uriTemplate.getUriTemplate());
                statement.setString(4, uriTemplate.getAuthType());
                statement.setInt(5, getAPIThrottlePolicyID(connection,uriTemplate.getPolicy()));
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }
    private void deleteUrlMappings(Connection connection, int apiID) throws
            SQLException {
        final String query = "DELETE FROM AM_API_URL_MAPPING WHERE API_ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, apiID);
            statement.execute();
        }
    }

    private Set<URITemplate> getUriTemplates(Connection connection, int apiId) throws SQLException {
        String query = "SELECT API_ID,HTTP_METHOD,URL_PATTERN,AUTH_SCHEME,API_POLICY_ID FROM AM_API_URL_MAPPING WHERE" +
                " API_ID = ?";
        Set<URITemplate> uriTemplateSet = new HashSet<>();
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, apiId);
            statement.execute();
            try (ResultSet rs = statement.getResultSet()) {
                while (rs.next()) {
                    URITemplate uriTemplate = new URITemplate.URITemplateBuilder()
                            .uriTemplate(rs.getString("URL_PATTERN")).authType(rs.getString("AUTH_SCHEME")).httpVerb
                                    (rs.getString("HTTP_METHOD")).policy(getAPIThrottlePolicyName(connection, rs
                                    .getInt("API_POLICY_ID"))).build();
                    uriTemplateSet.add(uriTemplate);
                }
            }
        }
        return uriTemplateSet;
    }
    private void addSubscriptionPolicies(Connection connection, List<String> policies, int apiID) throws
            SQLException {
        final String query = "INSERT INTO AM_API_SUBSCRIPTION_POLICY_MAPPING (API_ID, POLICY_ID) " +
                "VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            for (String policy : policies) {
                statement.setInt(1, apiID);
                statement.setInt(2, getSubscriptionThrottlePolicyID(connection,policy));
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void deleteSubscriptionPolicies(Connection connection, int apiID) throws
            SQLException {
        final String query = "DELETE FROM AM_API_SUBSCRIPTION_POLICY_MAPPING WHERE API_ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1,apiID);
            statement.execute();
        }
    }

    private int getSubscriptionThrottlePolicyID(Connection connection, String policyName) throws SQLException {
        final String query = "SELECT POLICY_ID from AM_POLICY_SUBSCRIPTION where NAME=?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, policyName);
            statement.execute();

            try (ResultSet rs = statement.getResultSet()) {
                if (rs.next()) {
                    return rs.getInt("POLICY_ID");
                }
            }
        }

        return -1;
    }
    private List<String> getSubscripitonPolciesByAPIId(Connection connection, int apiId) throws SQLException {
        final String query = "SELECT amPolcySub.NAME FROM AM_API_SUBSCRIPTION_POLICY_MAPPING as apimsubmapping," +
                "AM_POLICY_SUBSCRIPTION as amPolcySub where apimsubmapping.POLICY_ID=amPolcySub.POLICY_ID AND " +
                "apimsubmapping.API_ID = ?";
        List<String> policies = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, apiId);
            statement.execute();

            try (ResultSet rs = statement.getResultSet()) {
                if (rs.next()) {
                    policies.add(rs.getString("NAME"));
                }
            }
        }

        return policies;
    }
}

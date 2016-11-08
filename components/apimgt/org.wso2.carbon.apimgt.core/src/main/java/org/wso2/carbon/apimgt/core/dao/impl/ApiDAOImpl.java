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
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.APISummary;
import org.wso2.carbon.apimgt.core.models.APISummaryResults;
import org.wso2.carbon.apimgt.core.models.BusinessInformation;
import org.wso2.carbon.apimgt.core.models.CorsConfiguration;
import org.wso2.carbon.apimgt.core.models.DocumentInfo;
import org.wso2.carbon.apimgt.core.models.DocumentInfoResults;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Default implementation of the ApiDAO interface. Uses SQL syntax that is common to H2 and MySQL DBs.
 * Hence is considered as the default due to its re-usability.
 */
public class ApiDAOImpl implements ApiDAO {

    private final ApiDAOVendorSpecificStatements sqlStatements;

    ApiDAOImpl(ApiDAOVendorSpecificStatements sqlStatements) {
        this.sqlStatements = sqlStatements;
    }

    /**
     * Retrieve a given instance of an API
     *
     * @param apiID The {@link String} that uniquely identifies an API
     * @return valid {@link API} object or null
     * @throws SQLException if error occurs while accessing data layer
     */
    @Override
    @CheckForNull
    public API getAPI(String apiID) throws SQLException {
        final String query = "SELECT API_ID, PROVIDER, NAME, CONTEXT, VERSION, IS_DEFAULT_VERSION, DESCRIPTION, " +
                "VISIBILITY, IS_RESPONSE_CACHED, CACHE_TIMEOUT, UUID, TECHNICAL_OWNER, TECHNICAL_EMAIL, " +
                "BUSINESS_OWNER, BUSINESS_EMAIL, LIFECYCLE_INSTANCE_ID, CURRENT_LC_STATUS, API_POLICY_ID, " +
                "CORS_ENABLED, CORS_ALLOW_ORIGINS, CORS_ALLOW_CREDENTIALS, CORS_ALLOW_HEADERS, CORS_ALLOW_METHODS, " +
                "CREATED_BY, CREATED_TIME, LAST_UPDATED_TIME FROM AM_API WHERE UUID = ?";

        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, apiID);

            return constructAPIFromResultSet(connection, statement);
        }
    }

    /**
     * Retrieve a given instance of an APISummary object
     *
     * @param apiID The UUID that uniquely identifies an API
     * @return valid {@link APISummary} object or null
     * @throws SQLException if error occurs while accessing data layer
     */
    @Override
    public APISummary.Builder getAPISummary(String apiID) throws SQLException {
        return null;
    }

    /**
     * Retrieves summary data of all available APIs. This method supports result pagination as well as
     * doing a permission check to ensure results returned are only those that match the list of roles provided
     *
     * @param offset        The number of results from the beginning that is to be ignored
     * @param limit         The maximum number of results to be returned after the offset
     * @param roles The list of roles of the user making the query
     * @return {@link APISummaryResults} matching results
     * @throws SQLException if error occurs while accessing data layer
     */
    @Override
    @SuppressFBWarnings("SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING")
    public APISummaryResults getAPIsForRoles(int offset, int limit, List<String> roles)
                                                                            throws SQLException {
        final String query = sqlStatements.getAPIsForRoles(roles.size());

        try (Connection connection = DAOUtil.getConnection();
            PreparedStatement statement = connection.prepareStatement(query)) {

            int numberOfParams = roles.size();

            for (int i = 0; i < numberOfParams; ++i) {
                statement.setString(i + 1, roles.get(i));
            }

            int rowCount = limit + 1;  // We ask for an additional row to check if more results are available

            statement.setInt(++numberOfParams, offset);
            statement.setInt(++numberOfParams, rowCount);

            List<APISummary> apiSummaryList = constructAPISummaryFromResultSet(connection, statement);

            boolean isMoreResultsExist = false;

            if (apiSummaryList.size() == rowCount) { // More results exist
                apiSummaryList.remove(rowCount - 1); // Remove additional result that was not asked for
                isMoreResultsExist = true;
            }

            return new APISummaryResults.Builder(apiSummaryList, isMoreResultsExist,
                                                    offset + apiSummaryList.size()).build();
        }
    }

    /**
     * Retrieves summary data of all available APIs. This method supports result pagination as well as
     * doing a permission check to ensure results returned are only those that match the list of roles provided
     *
     * @param offset       The number of results from the beginning that is to be ignored
     * @param limit        The maximum number of results to be returned after the offset
     * @param providerName A given API Provider
     * @return {@link APISummaryResults} matching results
     * @throws SQLException if error occurs while accessing data layer
     */
    @Override
    public APISummaryResults getAPIsForProvider(int offset, int limit, String providerName) throws SQLException {
        return null;
    }

    /**
     * Retrieves summary data of all available APIs that match the given search criteria. This method supports result
     * pagination as well as doing a permission check to ensure results returned are only those that match
     * the list of roles provided
     *
     * @param searchString    The search string provided
     * @param offset          The number of results from the beginning that is to be ignored
     * @param limit           The maximum number of results to be returned after the offset
     * @param roles   The list of roles of the user making the query
     * @return {@link APISummaryResults} matching results
     * @throws SQLException if error occurs while accessing data layer
     */
    @Override
    public APISummaryResults searchAPIsForRoles(String searchString, int offset, int limit,
                                                List<String> roles) throws SQLException {
        return null;
    }

    /**
     * Checks if a given API which is uniquely identified by the Provider, API Name and Version combination already
     * exists
     *
     * @param providerName Name of API provider/publisher
     * @param apiName      Name of API
     * @param version      version of the API
     * @return true if providerName, apiName, version combination already exists else false
     * @throws SQLException if error occurs while accessing data layer
     */
    @Override
    public boolean isAPIExists(String providerName, String apiName, String version) throws SQLException {
        final String apiExistsQuery = "SELECT API_ID FROM AM_API WHERE PROVIDER = ? AND NAME = ? AND VERSION = ?";

        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(apiExistsQuery)) {
            statement.setString(1, providerName);
            statement.setString(2, apiName);
            statement.setString(3, version);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Checks if a given API Context already exists
     *
     * @param contextName Name of API Context
     * @return true if contextName already exists else false
     * @throws SQLException if error occurs while accessing data layer
     */
    @Override
    public boolean isAPIContextExists(String contextName) throws SQLException {
        final String apiExistsQuery = "SELECT API_ID FROM AM_API WHERE CONTEXT = ?";

        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(apiExistsQuery)) {
            statement.setString(1, contextName);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Add a new instance of an API
     *
     * @param api The {@link API} object to be added
     * @return true if addition is successful else false
     * @throws SQLException if error occurs while accessing data layer
     */
    @Override
    public void addAPI(final API api) throws SQLException {
        final String addAPIQuery = "INSERT INTO AM_API (PROVIDER, NAME, CONTEXT, VERSION, " +
                "IS_DEFAULT_VERSION, DESCRIPTION, VISIBILITY, IS_RESPONSE_CACHED, CACHE_TIMEOUT, " +
                "UUID, TECHNICAL_OWNER, TECHNICAL_EMAIL, BUSINESS_OWNER, BUSINESS_EMAIL, LIFECYCLE_INSTANCE_ID, " +
                "CURRENT_LC_STATUS, API_POLICY_ID, CORS_ENABLED, CORS_ALLOW_ORIGINS, CORS_ALLOW_CREDENTIALS, " +
                "CORS_ALLOW_HEADERS, CORS_ALLOW_METHODS,CREATED_BY, CREATED_TIME, LAST_UPDATED_TIME) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

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

            statement.setString(15, api.getLifeCycleInstanceMap().get("API_LIFECYCLE"));
            statement.setString(16, api.getLifeCycleStatus());
            statement.setInt(17, getAPIThrottlePolicyID(connection, api.getApiPolicy()));

            CorsConfiguration corsConfiguration = api.getCorsConfiguration();
            statement.setBoolean(18, corsConfiguration.isEnabled());
            statement.setString(19, String.join(",", corsConfiguration.getAllowOrigins()));
            statement.setBoolean(20, corsConfiguration.isAllowCredentials());
            statement.setString(21, String.join(",", corsConfiguration.getAllowHeaders()));
            statement.setString(22, String.join(",", corsConfiguration.getAllowMethods()));

            statement.setString(23, api.getCreatedBy());
            statement.setTimestamp(24, new java.sql.Timestamp(api.getCreatedTime().getTime()));
            statement.setTimestamp(25, new java.sql.Timestamp(api.getLastUpdatedTime().getTime()));

            statement.execute();

            try (ResultSet rs = statement.getGeneratedKeys()) {
                if (rs.next()) {
                    int apiPrimaryKey = rs.getInt(1);

                    if (API.Visibility.RESTRICTED == api.getVisibility()) {
                        addVisibleRole(connection, apiPrimaryKey, api.getVisibleRoles());
                    }

                    addTags(connection, apiPrimaryKey, api.getTags());
                    addAPIDefinition(connection, apiPrimaryKey, api.getApiDefinition());
                }
            }

            connection.commit();
        }

    }

    /**
     * Update an existing API
     *
     * @param apiID      The {@link String} of the API that needs to be updated
     * @param substituteAPI Substitute {@link API} object that will replace the existing API
     * @return true if update is successful else false
     * @throws SQLException if error occurs while accessing data layer
     */
    @Override
    public API updateAPI(String apiID, API substituteAPI) throws SQLException {
        final String addAPIQuery = "INSERT INTO AM_API (PROVIDER, NAME, CONTEXT, VERSION, " +
                "IS_DEFAULT_VERSION, DESCRIPTION, VISIBILITY, IS_RESPONSE_CACHED, CACHE_TIMEOUT, " +
                "UUID, TECHNICAL_OWNER, TECHNICAL_EMAIL, BUSINESS_OWNER, BUSINESS_EMAIL, LIFECYCLE_INSTANCE_ID, " +
                "CURRENT_LC_STATUS, API_POLICY_ID, CORS_ENABLED, CORS_ALLOW_ORIGINS, CORS_ALLOW_CREDENTIALS, " +
                "CORS_ALLOW_HEADERS, CORS_ALLOW_METHODS,CREATED_BY, CREATED_TIME, LAST_UPDATED_TIME) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        return null;
    }

    /**
     * Remove an existing API
     *
     * @param apiID The {@link String} of the API that needs to be deleted
     * @throws SQLException if error occurs while accessing data layer
     */
    @Override
    public void deleteAPI(String apiID) throws SQLException {
        final String deleteAPIQuery = "DELETE FROM AM_API WHERE UUID = ?";
        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(deleteAPIQuery)) {
            statement.setString(1, apiID);
            statement.execute();
            connection.commit();
        }
    }

    /**
     * Get swagger definition of a given API
     *
     * @param apiID The UUID of the respective API
     * @return Swagger definition String
     * @throws SQLException if error occurs while accessing data layer
     */
    @Override
    public String getSwaggerDefinition(String apiID) throws SQLException {
        return null;
    }

    /**
     * Update swagger definition of a given API
     *
     * @param apiID             The UUID of the respective API
     * @param swaggerDefinition Swagger definition String
     * @throws SQLException if error occurs while accessing data layer
     */
    @Override
    public void updateSwaggerDefinition(String apiID, String swaggerDefinition) throws SQLException {

    }

    /**
     * Get image of a given API
     *
     * @param apiID The UUID of the respective API
     * @return Image stream
     * @throws SQLException if error occurs while accessing data layer
     */
    @Override
    public InputStream getImage(String apiID) throws SQLException {
        return null;
    }

    /**
     * Update swagger definition of a given API
     *
     * @param apiID The UUID of the respective API
     * @param image Image stream
     * @throws SQLException if error occurs while accessing data layer
     */
    @Override
    public void updateImage(String apiID, OutputStream image) throws SQLException {

    }

    /**
     * Change the lifecycle status of a given API
     *
     * @param apiID                     The UUID of the respective API
     * @param status                    The lifecycle status that the API must be set to
     * @param deprecateOldVersions      if true for deprecate older versions
     * @param makeKeysForwardCompatible if true for make subscriptions get forward
     * @throws SQLException if error occurs while accessing data layer
     */
    @Override
    public void changeLifeCycleStatus(String apiID, String status, boolean deprecateOldVersions, boolean
            makeKeysForwardCompatible) throws SQLException {

    }

    /**
     * Create a new version of an existing API
     *
     * @param apiID   The UUID of the respective API
     * @param version The new version of the API
     * @return The new version {@link API} object
     * @throws SQLException if error occurs while accessing data layer
     */
    @Override
    public API.APIBuilder createNewAPIVersion(String apiID, String version) throws SQLException {
        return null;
    }

    /**
     * Return list of all Document info belonging to a given API. This method supports result pagination
     *
     * @param apiID  The UUID of the respective API
     * @param offset The number of results from the beginning that is to be ignored
     * @param limit  The maximum number of results to be returned after the offset
     * @throws SQLException if error occurs while accessing data layer
     */
    @Override
    public DocumentInfoResults getDocumentsInfoList(String apiID, int offset, int limit)
                                                                    throws SQLException {
        return null;
    }

    /**
     * @param docID The UUID of the respective Document
     * @return {@link DocumentInfo} Document Info object
     * @throws SQLException if error occurs while accessing data layer
     */
    @Override
    public DocumentInfo getDocumentInfo(String docID) throws SQLException {
        //todo:implement
        return null;
    }

    /**
     * @param docID The UUID of the respective Document
     * @return {@link InputStream} Document Info object
     * @throws SQLException if error occurs while accessing data layer
     */
    @Override
    public InputStream getDocumentContent(String docID) throws SQLException {
        //todo:implement
        return null;
    }

    /**
     * Attach Documentation (without content) to an API
     *
     * @param apiId         UUID of API
     * @param documentation Documentat Summary
     * @throws SQLException if error occurs while accessing data layer
     */
    @Override
    public void addDocumentationInfo(String apiId, DocumentInfo documentation) throws SQLException {
        //todo:implement
    }

    /**
     * Add a document (of source type FILE) with a file
     *
     * @param apiId         UUID of API
     * @param documentation Document Summary
     * @param filename      name of the file
     * @param content       content of the file as an Input Stream
     * @param contentType   content type of the file
     * @throws SQLException if error occurs while accessing data layer
     */
    @Override
    public void addDocumentationWithFile(String apiId, DocumentInfo documentation, String filename, InputStream content,
                                         String contentType) throws SQLException {
        //todo:implement
    }

    /**
     * Removes a given documentation
     *
     * @param id Document Id
     * @throws SQLException if error occurs while accessing data layer
     */
    @Override
    public void removeDocumentation(String id) throws SQLException {
        //todo:implement
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
                corsConfiguration.setAllowOrigins(Arrays.asList(rs.getString("CORS_ALLOW_ORIGINS").
                        split("\\s*,\\s*")));
                corsConfiguration.setAllowCredentials(rs.getBoolean("CORS_ALLOW_CREDENTIALS"));
                corsConfiguration.setAllowHeaders(Arrays.asList(rs.getString("CORS_ALLOW_HEADERS").
                        split("\\s*,\\s*")));
                corsConfiguration.setAllowMethods(Arrays.asList(rs.getString("CORS_ALLOW_METHODS").
                        split("\\s*,\\s*")));

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
                        apiDefinition(getAPIDefinition(connection, apiPrimaryKey)).
                        businessInformation(businessInformation).
                        lifeCycleInstanceMap(Collections.emptyMap()).
                        lifeCycleStatus(rs.getString("CURRENT_LC_STATUS")).
                        apiPolicy(getAPIThrottlePolicyName(connection, rs.getInt("API_POLICY_ID"))).
                        corsConfiguration(corsConfiguration).
                        createdBy(rs.getString("CREATED_BY")).
                        createdTime(rs.getTimestamp("CREATED_TIME")).
                        lastUpdatedTime(rs.getTimestamp("LAST_UPDATED_TIME")).
                        build();
            }
        }

        return null;
    }

    private List<APISummary> constructAPISummaryFromResultSet(Connection connection, PreparedStatement statement)
                                                                                                throws SQLException {
        List<APISummary> apiSummaryList = new ArrayList<>();
        try (ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                APISummary apiSummary = new APISummary.Builder(rs.getString("PROVIDER"), rs.getString("NAME"),
                        rs.getString("VERSION")).
                        id(rs.getString("UUID")).
                        context(rs.getString("CONTEXT")).
                        description(rs.getString("DESCRIPTION")).
                        status(rs.getString("CURRENT_LC_STATUS")).build();

                apiSummaryList.add(apiSummary);
            }
        }

        return apiSummaryList;
    }

    private void addTags(Connection connection, int apiID, List<String> tags) throws SQLException {
        if (!tags.isEmpty()) {
            List<Integer> tagIDs = TagDAO.addTagsIfNotExist(connection, tags);

            final String query = "INSERT INTO AM_API_TAG_MAPPING (API_ID, TAG_ID) VALUES (?, ?)";

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                for (Integer tagID : tagIDs) {
                    statement.setInt(1, apiID);
                    statement.setInt(2, tagID);
                    statement.addBatch();
                }

                statement.execute();
            }
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
            int resourceTypeID = ResourceTypeDAO.getResourceTypeID(connection,
                                                            ResourceConstants.ResourceType.SWAGGER.toString());

            if (resourceTypeID == -1) { // If resource type does not already exist
                ResourceTypeDAO.addResourceType(connection, ResourceConstants.ResourceType.SWAGGER.toString());
                resourceTypeID = ResourceTypeDAO.getResourceTypeID(connection,
                                                            ResourceConstants.ResourceType.SWAGGER.toString());
            }

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

    private String getAPIDefinition(Connection connection, int apiID) throws SQLException {
        int resourceTypeID = ResourceTypeDAO.getResourceTypeID(connection,
                                                            ResourceConstants.ResourceType.SWAGGER.toString());
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
}

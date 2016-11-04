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

import org.slf4j.Logger;
import org.wso2.carbon.apimgt.core.dao.ApiDAO;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.APISummaryResults;
import org.wso2.carbon.apimgt.core.models.BusinessInformation;
import org.wso2.carbon.apimgt.core.models.CorsConfiguration;
import org.wso2.carbon.apimgt.core.models.DocumentInfo;
import org.wso2.carbon.apimgt.core.models.DocumentInfoResults;

import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.annotation.CheckForNull;

/**
 * Default implementation of the ApiDAO interface. Uses SQL syntax that is common to H2 and MySQL DBs.
 * Hence is considered as the default due to its re-usability.
 */
public class ApiDAOImpl implements ApiDAO {

    private final SQLStatements sqlStatements;
    private final Logger log;

    ApiDAOImpl(SQLStatements sqlStatements, Logger log) {
        this.sqlStatements = sqlStatements;
        this.log = log;
    }

    /**
     * Retrieve a given instance of an API
     *
     * @param apiID The {@link String} that uniquely identifies an API
     * @return valid {@link API} object or null
     * @throws SQLException
     */
    @Override
    @CheckForNull
    public API getAPI(String apiID) throws SQLException {
        API api = null;

        final String getAPIQuery = "SELECT PROVIDER, NAME, CONTEXT, VERSION, IS_DEFAULT_VERSION, DESCRIPTION, " +
                "VISIBILITY, IS_RESPONSE_CACHED, CACHE_TIMEOUT, UUID, TECHNICAL_OWNER, TECHNICAL_EMAIL, " +
                "BUSINESS_OWNER, BUSINESS_EMAIL, LIFECYCLE_INSTANCE_ID, CURRENT_LC_STATUS, API_POLICY_ID, " +
                "CORS_ENABLED, CORS_ALLOW_ORIGINS, CORS_ALLOW_CREDENTIALS, CORS_ALLOW_HEADERS, CORS_ALLOW_METHODS, " +
                "CREATED_BY, CREATED_TIME, LAST_UPDATED_TIME FROM AM_API WHERE UUID = ?";

        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(getAPIQuery)) {
            statement.setString(1, apiID);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    api = new API(rs.getString("PROVIDER"), rs.getString("VERSION"), rs.getString("NAME"));
                    api.setId(rs.getString("UUID"));
                    api.setContext(rs.getString("CONTEXT"));
                    api.setDefaultVersion(rs.getBoolean("IS_DEFAULT_VERSION"));
                    api.setDescription(rs.getString("DESCRIPTION"));
                    api.setVisibility(API.Visibility.valueOf(rs.getString("VISIBILITY")));
                    api.setResponseCachingEnabled(rs.getBoolean("IS_RESPONSE_CACHED"));
                    api.setCacheTimeout(rs.getInt("CACHE_TIMEOUT"));
                    api.setId(rs.getString("UUID"));

                    BusinessInformation businessInformation = new BusinessInformation();
                    businessInformation.setTechnicalOwner(rs.getString("TECHNICAL_OWNER"));
                    businessInformation.setTechnicalOwnerEmail(rs.getString("TECHNICAL_EMAIL"));
                    businessInformation.setBusinessOwner(rs.getString("BUSINESS_OWNER"));
                    businessInformation.setBusinessOwnerEmail(rs.getString("BUSINESS_EMAIL"));
                    api.setBusinessInformation(businessInformation);

                    api.setLifeCycleInstanceID(rs.getString("LIFECYCLE_INSTANCE_ID"));
                    api.setLifeCycleStatus(rs.getString("CURRENT_LC_STATUS"));
                    api.setApiPolicyID(rs.getInt("API_POLICY_ID"));

                    CorsConfiguration corsConfiguration = new CorsConfiguration();
                    corsConfiguration.setEnabled(rs.getBoolean("CORS_ENABLED"));
                    corsConfiguration.setAllowOrigins(Arrays.asList(rs.getString("CORS_ALLOW_ORIGINS").
                                                                                                split("\\s*,\\s*")));
                    corsConfiguration.setAllowCredentials(rs.getBoolean("CORS_ALLOW_CREDENTIALS"));
                    corsConfiguration.setAllowHeaders(Arrays.asList(rs.getString("CORS_ALLOW_HEADERS").
                                                                                                split("\\s*,\\s*")));
                    corsConfiguration.setAllowMethods(Arrays.asList(rs.getString("CORS_ALLOW_METHODS").
                                                                                                split("\\s*,\\s*")));
                    api.setCorsConfiguration(corsConfiguration);

                    api.setCreatedBy(rs.getString("CREATED_BY"));
                    api.setCreatedTime(rs.getTimestamp("CREATED_TIME"));
                    api.setLastUpdatedTime(rs.getTimestamp("LAST_UPDATED_TIME"));
                }

            }
        }

        return api;
    }

    /**
     * Retrieves summary data of all available APIs. This method supports result pagination as well as
     * doing a permission check to ensure results returned are only those that match the list of roles provided
     *
     * @param offset        The number of results from the beginning that is to be ignored
     * @param limit         The maximum number of results to be returned after the offset
     * @param roles The list of roles of the user making the query
     * @return {@link APISummaryResults} matching results
     * @throws SQLException
     */
    @Override
    public APISummaryResults getAPIsForRoles(int offset, int limit, List<String> roles)
                                                                            throws SQLException {
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
     * @throws SQLException
     */
    @Override
    public APISummaryResults searchAPIsForRoles(String searchString, int offset, int limit,
                                                List<String> roles) throws SQLException {
        return null;
    }

    /**
     * Add a new instance of an API
     *
     * @param api The {@link API} object to be added
     * @return true if addition is successful else false
     * @throws SQLException
     */
    @Override
    public API addAPI(API api) throws SQLException {
        final String addAPIQuery = "INSERT INTO AM_API (PROVIDER, NAME, CONTEXT, VERSION, " +
                "IS_DEFAULT_VERSION, DESCRIPTION, VISIBILITY, IS_RESPONSE_CACHED, CACHE_TIMEOUT, " +
                "UUID, TECHNICAL_OWNER, TECHNICAL_EMAIL, BUSINESS_OWNER, BUSINESS_EMAIL, LIFECYCLE_INSTANCE_ID, " +
                "CURRENT_LC_STATUS, API_POLICY_ID, CORS_ENABLED, CORS_ALLOW_ORIGINS, CORS_ALLOW_CREDENTIALS, " +
                "CORS_ALLOW_HEADERS, CORS_ALLOW_METHODS,CREATED_BY, CREATED_TIME, LAST_UPDATED_TIME) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(addAPIQuery)) {
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

            statement.setString(15, api.getLifeCycleInstanceID());
            statement.setString(16, api.getLifeCycleStatus());
            statement.setInt(17, api.getApiPolicyID());

            CorsConfiguration corsConfiguration = api.getCorsConfiguration();
            statement.setBoolean(18, corsConfiguration.isEnabled());
            statement.setString(19, String.join(",", corsConfiguration.getAllowOrigins()));
            statement.setBoolean(20, corsConfiguration.isAllowCredentials());
            statement.setString(21, String.join(",", corsConfiguration.getAllowHeaders()));
            statement.setString(22, String.join(",", corsConfiguration.getAllowMethods()));

            statement.setString(23, api.getCreatedBy());
            Date date = new Date();
            api.setCreatedTime(date);
            api.setLastUpdatedTime(date);
            statement.setTimestamp(24, new java.sql.Timestamp(date.getTime()));
            statement.setTimestamp(25, new java.sql.Timestamp(date.getTime()));

            statement.execute();
            connection.commit();
        }

        return api;
    }

    /**
     * Update an existing API
     *
     * @param apiID      The {@link String} of the API that needs to be updated
     * @param substituteAPI Substitute {@link API} object that will replace the existing API
     * @return true if update is successful else false
     * @throws SQLException
     */
    @Override
    public API updateAPI(String apiID, API substituteAPI) throws SQLException {
        return null;
    }

    /**
     * Remove an existing API
     *
     * @param apiID The {@link String} of the API that needs to be deleted
     * @return true if update is successful else false
     * @throws SQLException
     */
    @Override
    public void deleteAPI(String apiID) throws SQLException {
        final String deleteAPIQuery = "DELETE FROM AM_API WHERE UUID = ?";
        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(deleteAPIQuery)) {
            statement.setString(1, apiID);
            statement.execute();
        }
    }

    /**
     * Get swagger definition of a given API
     *
     * @param apiID The UUID of the respective API
     * @return Swagger definition stream
     * @throws SQLException
     */
    @Override
    public OutputStream getSwaggerDefinition(String apiID) throws SQLException {
        return null;
    }

    /**
     * Update swagger definition of a given API
     *
     * @param apiID             The UUID of the respective API
     * @param swaggerDefinition Swagger definition stream
     * @throws SQLException
     */
    @Override
    public void updateSwaggerDefinition(String apiID, InputStream swaggerDefinition) throws SQLException {

    }

    /**
     * Get image of a given API
     *
     * @param apiID The UUID of the respective API
     * @return Image stream
     * @throws SQLException
     */
    @Override
    public OutputStream getImage(String apiID) throws SQLException {
        return null;
    }

    /**
     * Update swagger definition of a given API
     *
     * @param apiID The UUID of the respective API
     * @param image Image stream
     * @throws SQLException
     */
    @Override
    public void updateImage(String apiID, InputStream image) throws SQLException {

    }

    /**
     * Change the lifecycle status of a given API
     *
     * @param apiID                     The UUID of the respective API
     * @param status                    The lifecycle status that the API must be set to
     * @param deprecateOldVersions      if true for deprecate older versions
     * @param makeKeysForwardCompatible if true for make subscriptions get forward
     * @throws SQLException
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
     * @throws SQLException
     */
    @Override
    public API createNewAPIVersion(String apiID, String version) throws SQLException {
        return null;
    }

    /**
     * Return list of all Document info belonging to a given API. This method supports result pagination
     *
     * @param apiID  The UUID of the respective API
     * @param offset The number of results from the beginning that is to be ignored
     * @param limit  The maximum number of results to be returned after the offset
     * @throws SQLException
     */
    @Override
    public DocumentInfoResults getDocumentsInfoList(String apiID, int offset, int limit)
                                                                    throws SQLException {
        return null;
    }

    /**
     * @param apiID The UUID of the respective API
     * @param docID The UUID of the respective Document
     * @return {@link DocumentInfo} Document Info object
     * @throws SQLException if error occurs while accessing data layer
     */
    @Override
    public DocumentInfo getDocumentInfo(String apiID, String docID) throws SQLException {
        return null;
    }

    private boolean isAPIExists(API api) throws SQLException {
        final String apiExistsQuery = "SELECT API_ID FROM AM_API WHERE " +
                "PROVIDER = ? AND NAME = ? AND VERSION = ?";

        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(apiExistsQuery)) {
            statement.setString(1, api.getProvider());
            statement.setString(2, api.getName());
            statement.setString(3, api.getVersion());

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return true;
                }
            }
        }

        return false;
    }

}

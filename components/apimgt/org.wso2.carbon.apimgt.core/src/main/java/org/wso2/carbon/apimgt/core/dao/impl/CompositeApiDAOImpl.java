/*
 *
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.wso2.carbon.apimgt.core.dao.CompositeApiDAO;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.models.BusinessInformation;
import org.wso2.carbon.apimgt.core.models.CompositeAPI;
import org.wso2.carbon.apimgt.core.models.CorsConfiguration;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Implementation of the CompositeApiDAO interface.
 */
public class CompositeApiDAOImpl implements CompositeApiDAO {

    /**
     * @see CompositeApiDAO#getCompositeAPI(java.lang.String)
     */
    @Override
    public CompositeAPI getCompositeAPI(String apiID) throws APIMgtDAOException {
        final String query = CommonQueryConstants.API_SELECT + " WHERE UUID = ? AND API_TYPE_ID = (" +
                "SELECT TYPE_ID FROM AM_API_TYPE WHERE TYPE_NAME = " + CommonQueryConstants.COMPOSITE_API_TYPE;

        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, apiID);

            return constructAPIFromResultSet(connection, statement);
        } catch (SQLException | IOException e) {
            throw new APIMgtDAOException(e);
        }
    }

    /**
     * @see CompositeApiDAO#getCompositeAPISummary(java.lang.String)
     */
    @Override
    public CompositeAPI getCompositeAPISummary(String apiID) throws APIMgtDAOException {
        return null;
    }

    /**
     * @see CompositeApiDAO#getCompositeAPIsForProvider(java.lang.String)
     */
    @Override
    public List<CompositeAPI> getCompositeAPIsForProvider(String providerName) throws APIMgtDAOException {
        return null;
    }

    /**
     * @see CompositeApiDAO#searchCompositeAPIs(java.lang.String, java.lang.String, int, int)
     */
    @Override
    public List<CompositeAPI> searchCompositeAPIs(String user, String searchString, int offset, int limit)
            throws APIMgtDAOException {
        return null;
    }

    /**
     * @see CompositeApiDAO#attributeSearchCompositeAPIs(java.util.List, java.lang.String, java.util.Map, int, int)
     */
    @Override
    public List<CompositeAPI> attributeSearchCompositeAPIs(List<String> roles, String user,
                                                           Map<String, String> attributeMap,
                                                           int offset, int limit) throws APIMgtDAOException {
        return null;
    }

    /**
     * @see CompositeApiDAO#isCompositeAPINameExists(java.lang.String, java.lang.String)
     */
    @Override
    public boolean isCompositeAPINameExists(String apiName, String providerName) throws APIMgtDAOException {
        return false;
    }

    /**
     * @see CompositeApiDAO#isCompositeAPIContextExists(java.lang.String)
     */
    @Override
    public boolean isCompositeAPIContextExists(String contextName) throws APIMgtDAOException {
        return false;
    }

    /**
     * @see CompositeApiDAO#addCompositeAPI(CompositeAPI)
     */
    @Override
    public void addCompositeAPI(CompositeAPI api) throws APIMgtDAOException {

    }

    /**
     * @see CompositeApiDAO#updateCompositeAPI(java.lang.String, CompositeAPI)
     */
    @Override
    public void updateCompositeAPI(String apiID, CompositeAPI substituteAPI) throws APIMgtDAOException {

    }

    /**
     * @see CompositeApiDAO#deleteCompositeAPI(java.lang.String)
     */
    @Override
    public void deleteCompositeAPI(String apiID) throws APIMgtDAOException {

    }

    /**
     * @see CompositeApiDAO#getSwaggerDefinition(java.lang.String)
     */
    @Override
    public String getSwaggerDefinition(String apiID) throws APIMgtDAOException {
        return null;
    }

    /**
     * @see CompositeApiDAO#updateSwaggerDefinition(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void updateSwaggerDefinition(String apiID, String swaggerDefinition, String updatedBy)
            throws APIMgtDAOException {

    }

    private CompositeAPI constructAPIFromResultSet(Connection connection, PreparedStatement statement)
            throws SQLException, IOException {
        try (ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                BusinessInformation businessInformation = new BusinessInformation();
                businessInformation.setTechnicalOwner(rs.getString("TECHNICAL_OWNER"));
                businessInformation.setTechnicalOwnerEmail(rs.getString("TECHNICAL_EMAIL"));
                businessInformation.setBusinessOwner(rs.getString("BUSINESS_OWNER"));
                businessInformation.setBusinessOwnerEmail(rs.getString("BUSINESS_EMAIL"));

                CorsConfiguration corsConfiguration = new CorsConfiguration();
                corsConfiguration.setEnabled(rs.getBoolean("CORS_ENABLED"));

                String allowOrigins =  rs.getString("CORS_ALLOW_ORIGINS");
                corsConfiguration.setAllowOrigins(DAOUtil.commaSeperatedStringToList(allowOrigins));

                corsConfiguration.setAllowCredentials(rs.getBoolean("CORS_ALLOW_CREDENTIALS"));

                String allowHeaders = rs.getString("CORS_ALLOW_HEADERS");
                corsConfiguration.setAllowHeaders(DAOUtil.commaSeperatedStringToList(allowHeaders));

                String allowMethods = rs.getString("CORS_ALLOW_METHODS");
                corsConfiguration.setAllowMethods(DAOUtil.commaSeperatedStringToList(allowMethods));

                String apiPrimaryKey = rs.getString("UUID");

                return new CompositeAPI.APIBuilder(rs.getString("PROVIDER"),
                        rs.getString("NAME"), rs.getString("VERSION")).
                        id(apiPrimaryKey).
                        context(rs.getString("CONTEXT")).
                        isDefaultVersion(rs.getBoolean("IS_DEFAULT_VERSION")).
                        description(rs.getString("DESCRIPTION")).
                        isResponseCachingEnabled(rs.getBoolean("IS_RESPONSE_CACHED")).
                        cacheTimeout(rs.getInt("CACHE_TIMEOUT")).
                        labels(ApiMetaInfoDAO.getLabelNames(connection, apiPrimaryKey)).
                        transport(ApiMetaInfoDAO.getTransports(connection, apiPrimaryKey)).
                        businessInformation(businessInformation).
                        lifecycleInstanceId(rs.getString("LIFECYCLE_INSTANCE_ID")).
                        lifeCycleStatus(rs.getString("CURRENT_LC_STATUS")).
                        corsConfiguration(corsConfiguration).
                        createdBy(rs.getString("CREATED_BY")).
                        updatedBy(rs.getString("UPDATED_BY")).
                        createdTime(rs.getTimestamp("CREATED_TIME").toLocalDateTime()).
                        lastUpdatedTime(rs.getTimestamp("LAST_UPDATED_TIME").toLocalDateTime()).
                        uriTemplates(ApiMetaInfoDAO.getUriTemplates(connection, apiPrimaryKey)).
                        copiedFromApiId(rs.getString
                        ("COPIED_FROM_API")).
                        workflowStatus(rs.getString("LC_WORKFLOW_STATUS")).build();
            }
        }

        return null;
    }
}

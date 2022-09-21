/*
 *   Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl.dao.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIStatus;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dao.ApiDAO;
import org.wso2.carbon.apimgt.impl.dao.constants.SQLConstants;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.persistence.dto.*;
import org.wso2.carbon.apimgt.persistence.exceptions.APIPersistenceException;
import org.wso2.carbon.apimgt.persistence.mapper.APIMapper;
import org.wso2.carbon.apimgt.persistence.utils.PublisherAPISearchResultComparator;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ApiDAOImpl implements ApiDAO {

    private static final Log log = LogFactory.getLog(ApiDAOImpl.class);
    private static ApiDAOImpl INSTANCE = null;
    private boolean forceCaseInsensitiveComparisons = false;
    private boolean multiGroupAppSharingEnabled = false;

    private ApiDAOImpl() {
        APIManagerConfiguration configuration = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfigurationService().getAPIManagerConfiguration();
        String caseSensitiveComparison = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration().getFirstProperty(APIConstants.API_STORE_FORCE_CI_COMPARISIONS);
        if (caseSensitiveComparison != null) {
            forceCaseInsensitiveComparisons = Boolean.parseBoolean(caseSensitiveComparison);
        }
        multiGroupAppSharingEnabled = APIUtil.isMultiGroupAppSharingEnabled();
    }

    /**
     * Method to get the instance of the ApiDAOImpl.
     *
     * @return {@link ApiDAOImpl} instance
     */
    public static ApiDAOImpl getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ApiDAOImpl();
        }
        return INSTANCE;
    }

    private void handleException(String msg, Throwable t) throws APIManagementException {
        log.error(msg, t);
        throw new APIManagementException(msg, t);
    }

    @Override
    public int addAPI(API api, int tenantId, String organization) throws APIManagementException {
        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        int apiId = -1;
        String query = SQLConstants.ADD_API_SQL;

        String uuid = UUID.nameUUIDFromBytes(api.getId().getApiName().getBytes()).toString();
        api.setUuid(uuid);
        PublisherAPI artefact = APIMapper.INSTANCE.toPublisherApi(api);
        String jsonArtifact = "";
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        try {
            jsonArtifact = ow.writeValueAsString(artefact);
        } catch (IOException e) {
            throw new APIManagementException(e);
        }
        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);

            prepStmt = connection.prepareStatement(query, new String[]{"api_id"});
            prepStmt.setString(1, APIUtil.replaceEmailDomainBack(api.getId().getProviderName()));
            prepStmt.setString(2, api.getId().getApiName());
            prepStmt.setString(3, api.getId().getVersion());
            prepStmt.setString(4, api.getContext());
            String contextTemplate = api.getContextTemplate();
            //Validate if the API has an unsupported context before executing the query
            String invalidContext = "/" + APIConstants.VERSION_PLACEHOLDER;
            if (invalidContext.equals(contextTemplate)) {
                throw new APIManagementException("Cannot add API : " + api.getId() + " with unsupported context : "
                        + contextTemplate);
            }
            //If the context template ends with {version} this means that the version will be at the end of the context.
            if (contextTemplate.endsWith("/" + APIConstants.VERSION_PLACEHOLDER)) {
                //Remove the {version} part from the context template.
                contextTemplate = contextTemplate.split(Pattern.quote("/" + APIConstants.VERSION_PLACEHOLDER))[0];
            }

            // For Choreo-Connect gateway, gateway vendor type in the DB will be "wso2/choreo-connect".
            // This value is determined considering the gateway type comes with the request.
            api.setGatewayVendor(APIUtil.setGatewayVendorBeforeInsertion(
                    api.getGatewayVendor(), api.getGatewayType()));

            prepStmt.setString(5, contextTemplate);
            prepStmt.setString(6, APIUtil.replaceEmailDomainBack(api.getId().getProviderName()));
            prepStmt.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
            prepStmt.setString(8, api.getApiLevelPolicy());
            prepStmt.setString(9, api.getType());
            prepStmt.setString(10, api.getUuid());
            prepStmt.setString(11, APIConstants.CREATED);
            prepStmt.setString(12, organization);
            prepStmt.setString(13, api.getGatewayVendor());
            prepStmt.setString(14, api.getVersionTimestamp());
            prepStmt.setString(15, jsonArtifact);
            prepStmt.execute();

            rs = prepStmt.getGeneratedKeys();
            if (rs.next()) {
                apiId = rs.getInt(1);
            }

            connection.commit();

            String tenantUserName = MultitenantUtils
                    .getTenantAwareUsername(APIUtil.replaceEmailDomainBack(api.getId().getProviderName()));
            recordAPILifeCycleEvent(apiId, null, APIStatus.CREATED.toString(), tenantUserName, tenantId,
                    connection);
            //If the api is selected as default version, it is added/replaced into AM_API_DEFAULT_VERSION table
            if (api.isDefaultVersion()) {
                addUpdateAPIAsDefaultVersion(api, connection);
            }
            String serviceKey = api.getServiceInfo("key");
            if (StringUtils.isNotEmpty(serviceKey)) {
                addAPIServiceMapping(apiId, serviceKey, api.getServiceInfo("md5"), tenantId, connection);
            }
            addAPIDefinition(organization, api,connection);
            connection.commit();
        } catch (SQLException e) {
            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException ex) {
                // Rollback failed. Exception will be thrown later for upper exception
                log.error("Failed to rollback the add API: " + api.getId(), ex);
            }
            handleException("Error while adding the API: " + api.getId() + " to the database", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
        }
        return apiId;
    }

    public void recordAPILifeCycleEvent(String uuid, String oldStatus, String newStatus, String userId,
                                        int tenantId) throws APIManagementException {

        try (Connection conn = APIMgtDBUtil.getConnection()) {
            int apiId = getAPIID(uuid, conn);
            conn.setAutoCommit(false);
            try {
                recordAPILifeCycleEvent(apiId, oldStatus, newStatus, userId, tenantId, conn);
                changeAPILifeCycleStatus(conn, apiId, newStatus);
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            handleException("Failed to record API state change", e);
        }
    }

    private void recordAPILifeCycleEvent(int apiId, String oldStatus, String newStatus, String userId,
                                         int tenantId, Connection conn) throws APIManagementException, SQLException {

        if (oldStatus == null && !newStatus.equals(APIConstants.CREATED)) {
            String msg = "Invalid old and new state combination";
            log.error(msg);
            throw new APIManagementException(msg);
        } else if (oldStatus != null && oldStatus.equals(newStatus)) {
            String msg = "No measurable differences in API state";
            log.error(msg);
            throw new APIManagementException(msg);
        }

        String sqlQuery = SQLConstants.ADD_API_LIFECYCLE_EVENT_SQL;

        try (PreparedStatement ps = conn.prepareStatement(sqlQuery)) {
            ps.setInt(1, apiId);
            if (oldStatus != null) {
                ps.setString(2, oldStatus);
            } else {
                ps.setNull(2, Types.VARCHAR);
            }
            ps.setString(3, newStatus);
            ps.setString(4, userId);
            ps.setInt(5, tenantId);
            ps.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
            ps.executeUpdate();
            conn.commit();
        }
        // finally commit transaction
    }

    private void changeAPILifeCycleStatus(Connection connection, int apiId, String updatedStatus) throws SQLException {

        try (PreparedStatement preparedStatement = connection.prepareStatement(SQLConstants.UPDATE_API_STATUS)) {
            preparedStatement.setString(1, updatedStatus);
            preparedStatement.setInt(2, apiId);
            preparedStatement.executeUpdate();
            connection.commit();
        }
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
                    throw new APIManagementException(msg);
                }
            }
        }
        return id;
    }


    public void addUpdateAPIAsDefaultVersion(API api, Connection connection) throws APIManagementException {

        String publishedDefaultVersion = getPublishedDefaultVersion(api.getId());
        boolean deploymentAvailable = isDeploymentAvailableByAPIUUID(connection, api.getUuid());
        ArrayList<APIIdentifier> apiIdList = new ArrayList<APIIdentifier>() {{
            add(api.getId());
        }};
        removeAPIFromDefaultVersion(apiIdList, connection);

        PreparedStatement prepStmtDefVersionAdd = null;
        String queryDefaultVersionAdd = SQLConstants.ADD_API_DEFAULT_VERSION_SQL;
        try {
            prepStmtDefVersionAdd = connection.prepareStatement(queryDefaultVersionAdd);
            prepStmtDefVersionAdd.setString(1, api.getId().getApiName());
            prepStmtDefVersionAdd.setString(2, APIUtil.replaceEmailDomainBack(api.getId().getProviderName()));
            prepStmtDefVersionAdd.setString(3, api.getId().getVersion());

            if (deploymentAvailable) {
                prepStmtDefVersionAdd.setString(4, api.getId().getVersion());
                api.setAsPublishedDefaultVersion(true);
            } else {
                prepStmtDefVersionAdd.setString(4, publishedDefaultVersion);
            }
            prepStmtDefVersionAdd.setString(5, api.getOrganization());
            prepStmtDefVersionAdd.execute();
        } catch (SQLException e) {
            handleException("Error while adding the API default version entry: " + api.getId().getApiName() + " to " +
                    "the database", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmtDefVersionAdd, null, null);
        }
    }

    public String getPublishedDefaultVersion(APIIdentifier apiId) throws APIManagementException {

        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        String publishedDefaultVersion = null;

        String query = SQLConstants.GET_PUBLISHED_DEFAULT_VERSION_SQL;
        try {
            connection = APIMgtDBUtil.getConnection();
            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, apiId.getApiName());
            prepStmt.setString(2, APIUtil.replaceEmailDomainBack(apiId.getProviderName()));

            rs = prepStmt.executeQuery();

            while (rs.next()) {
                publishedDefaultVersion = rs.getString("PUBLISHED_DEFAULT_API_VERSION");
            }
        } catch (SQLException e) {
            handleException("Error while getting default version for " + apiId.getApiName(), e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
        }
        return publishedDefaultVersion;
    }

    /**
     * Get APIRevisionDeployment details by providing API uuid
     *
     * @return List<APIRevisionDeployment> object
     * @throws APIManagementException if an error occurs while retrieving revision deployment mapping details
     */
    private boolean isDeploymentAvailableByAPIUUID(Connection connection, String apiUUID) throws APIManagementException {

        try (PreparedStatement statement =
                     connection.prepareStatement(SQLConstants.APIRevisionSqlConstants.CHECK_API_REVISION_DEPLOYMENT_AVAILABILITY_BY_API_UUID)) {
            statement.setString(1, apiUUID);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return true;
                }
            }
        } catch (SQLException e) {
            handleException("Failed to get API Revision deployment mapping details for api uuid: " +
                    apiUUID, e);
        }
        return false;
    }

    /**
     * Sets/removes default api entry such that api will not represent as default api further.
     * If the api's version is the same as the published version, then the whole entry will be removed.
     * Otherwise only the default version attribute is set to null.
     *
     * @param apiIdList
     * @param connection
     * @return
     * @throws APIManagementException
     */
    private void removeAPIFromDefaultVersion(List<APIIdentifier> apiIdList, Connection connection) throws
            APIManagementException {
        // TODO: check list empty
        try (PreparedStatement prepStmtDefVersionDelete =
                     connection.prepareStatement(SQLConstants.REMOVE_API_DEFAULT_VERSION_SQL)) {

            for (APIIdentifier apiId : apiIdList) {
                prepStmtDefVersionDelete.setString(1, apiId.getApiName());
                prepStmtDefVersionDelete.setString(2, APIUtil.
                        replaceEmailDomainBack(apiId.getProviderName()));
                prepStmtDefVersionDelete.addBatch();
            }
            prepStmtDefVersionDelete.executeBatch();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException e1) {
                log.error("Error while rolling back the failed operation", e1);
            }
            handleException("Error while deleting the API default version entry: " + apiIdList.stream().
                    map(APIIdentifier::getApiName).collect(Collectors.joining(",")) + " from the " +
                    "database", e);
        }
    }

    private void addAPIServiceMapping(int apiId, String serviceKey, String md5sum, int tenantId,
                                      Connection connection) throws SQLException {

        String addAPIServiceMappingSQL = SQLConstants.ADD_API_SERVICE_MAPPING_SQL;
        try (PreparedStatement preparedStatement = connection.prepareStatement(addAPIServiceMappingSQL)) {
            preparedStatement.setInt(1, apiId);
            preparedStatement.setString(2, serviceKey);
            preparedStatement.setString(3, md5sum);
            preparedStatement.setInt(4, tenantId);
            preparedStatement.executeUpdate();
        }
    }

    private void addAPIDefinition(String org, API api,
                                      Connection connection) throws SQLException {
        String addAPIDefinition = SQLConstants.ADD_API_DEFINITION_SQL;
        try (PreparedStatement preparedStatement = connection.prepareStatement(addAPIDefinition)) {
            preparedStatement.setString(1, org);
            preparedStatement.setString(2, api.getUuid());
            if (api.getSwaggerDefinition() != null) {
                byte[] apiDefinitionBytes = api.getSwaggerDefinition().getBytes();
                preparedStatement.setBinaryStream(3, new ByteArrayInputStream(apiDefinitionBytes));
                preparedStatement.setString(4, "swagger.json");
            } else if (api.getAsyncApiDefinition() != null) {
                byte[] apiDefinitionBytes = api.getAsyncApiDefinition().getBytes();
                preparedStatement.setBinaryStream(3, new ByteArrayInputStream(apiDefinitionBytes));
                preparedStatement.setString(4, "asyncapi.json");
            } else if (api.getGraphQLSchema() != null) {
                byte[] apiDefinitionBytes = api.getGraphQLSchema().getBytes();
                preparedStatement.setBinaryStream(3, new ByteArrayInputStream(apiDefinitionBytes));
                preparedStatement.setString(4, "graphqlapi.graphql");
            } else {
                preparedStatement.setBinaryStream(3, null);
                preparedStatement.setString(4, null);
            }
            preparedStatement.executeUpdate();
        }
    }

    @Override
    public PublisherAPI getPublisherAPI(Organization organization, String apiUUID) throws APIManagementException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        PublisherAPI publisherAPI = null;
        String getAPIArtefactQuery = SQLConstants.GET_API_ARTIFACT_SQL;
        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement(getAPIArtefactQuery);
            preparedStatement.setString(1, apiUUID);
            preparedStatement.setString(2, organization.getName());
            resultSet = preparedStatement.executeQuery();
            connection.commit();
            while (resultSet.next()) {
                String json = resultSet.getString("ARTIFACT");
                ObjectMapper mapper = new ObjectMapper();
                JsonNode tree = mapper.readTree(json);
                publisherAPI = mapper.treeToValue(tree, PublisherAPI.class);
                String mediaType = resultSet.getString("MEDIA_TYPE");
                InputStream apiDefinitionBlob = resultSet.getBinaryStream("API_DEFINITION");
                if (apiDefinitionBlob != null) {
                    if (StringUtils.equals("swagger.json",mediaType)) {
                        publisherAPI.setSwaggerDefinition(APIMgtDBUtil.getStringFromInputStream(apiDefinitionBlob));
                    } else if (StringUtils.equals("asyncapi.json",mediaType)) {
                        publisherAPI.setAsyncApiDefinition(APIMgtDBUtil.getStringFromInputStream(apiDefinitionBlob));
                    }
                }
            }
        } catch (SQLException e) {
            if (log.isDebugEnabled()) {
                log.debug("Error while retrieving api artefact for API uuid: " + apiUUID);
            }
            handleException("Error while retrieving api artefact for API uuid: " + apiUUID, e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            APIMgtDBUtil.closeAllConnections(preparedStatement, connection, resultSet);
        }
        return publisherAPI;
    }


    @Override
    public PublisherAPISearchResult searchAPIsForPublisher(Organization organization, String searchQuery, int start,
                                                           int offset, UserContext ctx, String sortBy, String sortOrder)
            throws APIManagementException {
        PublisherAPISearchResult result = null;
        String searchAllQuery = SQLConstants.SEARCH_ALL_APIS_SQL;

        if (StringUtils.isEmpty(searchQuery)) {
            result = searchPaginatedPublisherAPIs(organization.getName(), searchAllQuery, start, offset);
        }
        return result;
    }

    private PublisherAPISearchResult searchPaginatedPublisherAPIs(String org, String searchQuery, int start,
                                                                  int offset) throws APIManagementException {

        int totalLength = 0;
        PublisherAPISearchResult searchResults = new PublisherAPISearchResult();
        PublisherAPI publisherAPI;
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement(searchQuery);
            preparedStatement.setString(1, org);
            resultSet = preparedStatement.executeQuery();
            connection.commit();
            List<PublisherAPIInfo> publisherAPIInfoList = new ArrayList<>();
            while (resultSet.next()) {
                String json = resultSet.getString(1);
                ObjectMapper mapper = new ObjectMapper();
                JsonNode tree = mapper.readTree(json);
                publisherAPI = mapper.treeToValue(tree, PublisherAPI.class);
                PublisherAPIInfo apiInfo = new PublisherAPIInfo();
                apiInfo.setType(publisherAPI.getType());
                apiInfo.setId(resultSet.getString(2));
                apiInfo.setApiName(publisherAPI.getApiName());
                apiInfo.setDescription(publisherAPI.getDescription());
                apiInfo.setContext(publisherAPI.getContext());
                apiInfo.setProviderName(publisherAPI.getProviderName());
                apiInfo.setStatus(publisherAPI.getStatus());
                apiInfo.setThumbnail(publisherAPI.getThumbnail());
                apiInfo.setVersion(publisherAPI.getVersion());
                apiInfo.setAudience(publisherAPI.getAudience());
                apiInfo.setCreatedTime(publisherAPI.getCreatedTime());
                apiInfo.setUpdatedTime(publisherAPI.getUpdatedTime());
                publisherAPIInfoList.add(apiInfo);
                totalLength ++;
            }
            Collections.sort(publisherAPIInfoList, new PublisherAPISearchResultComparator());
            searchResults.setPublisherAPIInfoList(publisherAPIInfoList);
            searchResults.setReturnedAPIsCount(publisherAPIInfoList.size());
            searchResults.setTotalAPIsCount(totalLength);
        } catch (SQLException e) {
            if (log.isDebugEnabled()) {
                log.debug("Error while retrieving api artefacts");
            }
            handleException("Error while retrieving api artefacts", e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            APIMgtDBUtil.closeAllConnections(preparedStatement, connection, resultSet);
        }
        return searchResults;
    }



}

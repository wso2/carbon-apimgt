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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.dto.ClientCertificateDTO;
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.api.model.graphql.queryanalysis.CustomComplexityDetails;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dao.ApiDAO;
import org.wso2.carbon.apimgt.impl.dao.constants.SQLConstants;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.persistence.dto.*;
import org.wso2.carbon.apimgt.persistence.dto.ResourceFile;
import org.wso2.carbon.apimgt.persistence.exceptions.*;
import org.wso2.carbon.apimgt.persistence.mapper.APIMapper;
import org.wso2.carbon.apimgt.persistence.utils.PublisherAPISearchResultComparator;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;
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
        String getAPIArtefactQuery;
        APIRevision apiRevision = checkAPIUUIDIsARevisionUUID(apiUUID);
        if (apiRevision != null && apiRevision.getApiUUID() != null) {
            getAPIArtefactQuery = SQLConstants.GET_REVISION_API_ARTIFACT_SQL;
        } else {
            getAPIArtefactQuery = SQLConstants.GET_API_ARTIFACT_SQL;
        }
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

    @Override
    public void saveWSDL(Organization organization, String apiId, ResourceFile wsdlResourceFile) throws WSDLPersistenceException {
//        String mediaType;
//        if (APIConstants.APPLICATION_ZIP.equals(wsdlResourceFile.getContentType())) {
//            mediaType = wsdlResourceFile.getName() + APIConstants.ZIP_FILE_EXTENSION;
//        } else {
//            mediaType = wsdlResourceFile.getName() + ".wsdl";
//        }
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        PreparedStatement preparedStatementUpdate = null;
        String saveWSDLQuery = SQLConstants.UPDATE_API_DEFINITION_SQL;
        String updateArtifactQuery = "UPDATE AM_API SET ARTIFACT[?] = TO_JSONB(?) WHERE ORGANIZATION=? AND API_UUID=?;";
        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement(saveWSDLQuery);
            if (wsdlResourceFile.getContent() != null) {
                preparedStatement.setBinaryStream(1, wsdlResourceFile.getContent());
                preparedStatement.setString(2, wsdlResourceFile.getContentType());
            }
            preparedStatement.setString(3, organization.getName());
            preparedStatement.setString(4, apiId);
            preparedStatement.executeUpdate();

            preparedStatementUpdate = connection.prepareStatement(updateArtifactQuery);
            if (wsdlResourceFile.getContent() != null) {
                preparedStatementUpdate.setString(1,"wsdlUrl");
                preparedStatementUpdate.setString(2,"wsdl");
            }
            preparedStatementUpdate.setString(3, organization.getName());
            preparedStatementUpdate.setString(4, apiId);
            preparedStatementUpdate.executeUpdate();

            connection.commit();
        } catch (SQLException e) {
            APIMgtDBUtil.rollbackConnection(connection,"Save WSDL definition");
            if (log.isDebugEnabled()) {
                log.debug("Error occurred while updating entry in AM_API_ARTIFACT table ", e);
            }
            throw new WSDLPersistenceException("Error while updating entry in AM_API_ARTIFACT table ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(preparedStatement, connection, null);
        }
    }

    @Override
    public ResourceFile getWSDL(Organization organization, String apiId) throws WSDLPersistenceException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        ResourceFile returnResource = null;
        String getWSDLQuery = SQLConstants.GET_API_DEFINITION_SQL;
        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement(getWSDLQuery);
            preparedStatement.setString(1, organization.getName());
            preparedStatement.setString(2, apiId);
            resultSet = preparedStatement.executeQuery();
            connection.commit();
            while (resultSet.next()) {
                String mediaType = resultSet.getString("MEDIA_TYPE");
                InputStream apiDefinitionBlob = resultSet.getBinaryStream("API_DEFINITION");
                if (apiDefinitionBlob != null) {
                    byte[] artifactByte = APIMgtDBUtil.getBytesFromInputStream(apiDefinitionBlob);
                    try (InputStream newArtifact = new ByteArrayInputStream(artifactByte)) {
                        returnResource = new ResourceFile(newArtifact, mediaType);
                        //returnResource.setName(resourceFileName);
                    } catch (IOException e) {
                        throw new WSDLPersistenceException("Error occurred retrieving input stream from byte array.", e);
                    }
                }
            }
        } catch (SQLException e) {
            if (log.isDebugEnabled()) {
                log.debug("Error while retrieving WSDL definition for api uuid: " + apiId);
            }
            throw new WSDLPersistenceException("Error while retrieving WSDL definition for api uuid: " + apiId, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(preparedStatement, connection, resultSet);
        }
        return returnResource;
    }

    @Override
    public void saveOASDefinition(Organization organization, String apiId, String apiDefinition) throws OASPersistenceException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        String saveOASQuery = SQLConstants.UPDATE_API_DEFINITION_SQL;
        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement(saveOASQuery);
            if (apiDefinition != null) {
                byte[] apiDefinitionBytes = apiDefinition.getBytes();
                preparedStatement.setBinaryStream(1, new ByteArrayInputStream(apiDefinitionBytes));
                preparedStatement.setString(2, org.wso2.carbon.apimgt.persistence.APIConstants.API_OAS_DEFINITION_RESOURCE_NAME);
            }
            preparedStatement.setString(3, organization.getName());
            preparedStatement.setString(4, apiId);
            preparedStatement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            APIMgtDBUtil.rollbackConnection(connection,"Save OAS definition");
            if (log.isDebugEnabled()) {
                log.debug("Error occurred while updating entry in AM_API_ARTIFACT table ", e);
            }
            throw new OASPersistenceException("Error while updating entry in AM_API_ARTIFACT table ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(preparedStatement, connection, null);
        }
    }

    @Override
    public String getOASDefinition(Organization organization, String apiId) throws OASPersistenceException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String oasDefinition = null;
        String getOASDefinitionQuery = SQLConstants.GET_API_DEFINITION_SQL;
        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement(getOASDefinitionQuery);
            preparedStatement.setString(1, organization.getName());
            preparedStatement.setString(2, apiId);
            resultSet = preparedStatement.executeQuery();
            connection.commit();
            while (resultSet.next()) {
                String mediaType = resultSet.getString("MEDIA_TYPE");
                InputStream apiDefinitionBlob = resultSet.getBinaryStream("API_DEFINITION");
                if (apiDefinitionBlob != null) {
                    oasDefinition = APIMgtDBUtil.getStringFromInputStream(apiDefinitionBlob);
                }
            }
        } catch (SQLException e) {
            if (log.isDebugEnabled()) {
                log.debug("Error while retrieving oas definition for api uuid: " + apiId);
            }
            throw new OASPersistenceException("Error while retrieving oas definition for api uuid: " + apiId, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(preparedStatement, connection, resultSet);
        }
        return oasDefinition;
    }

    @Override
    public void saveAsyncDefinition(Organization organization, String apiId, String apiDefinition) throws AsyncSpecPersistenceException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        String saveOASQuery = SQLConstants.UPDATE_API_DEFINITION_SQL;
        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement(saveOASQuery);
            if (apiDefinition != null) {
                byte[] apiDefinitionBytes = apiDefinition.getBytes();
                preparedStatement.setBinaryStream(1, new ByteArrayInputStream(apiDefinitionBytes));
                preparedStatement.setString(2, org.wso2.carbon.apimgt.persistence.APIConstants.API_ASYNC_API_DEFINITION_RESOURCE_NAME);
            }
            preparedStatement.setString(3, organization.getName());
            preparedStatement.setString(4, apiId);
            preparedStatement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            APIMgtDBUtil.rollbackConnection(connection,"Save Async API definition");
            if (log.isDebugEnabled()) {
                log.debug("Error occurred while updating entry in API_ARTIFACTS table ", e);
            }
            throw new AsyncSpecPersistenceException("Error while updating entry in API_ARTIFACTS table ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(preparedStatement, connection, null);
        }
    }

    @Override
    public String getAsyncDefinition(Organization organization, String apiId) throws AsyncSpecPersistenceException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String asyncDefinition = null;
        String getAsyncDefinitionQuery = SQLConstants.GET_API_DEFINITION_SQL;
        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement(getAsyncDefinitionQuery);
            preparedStatement.setString(1, organization.getName());
            preparedStatement.setString(2, apiId);
            resultSet = preparedStatement.executeQuery();
            connection.commit();
            while (resultSet.next()) {
                String mediaType = resultSet.getString("MEDIA_TYPE");
                InputStream apiDefinitionBlob = resultSet.getBinaryStream("API_DEFINITION");
                if (apiDefinitionBlob != null) {
                    asyncDefinition = APIMgtDBUtil.getStringFromInputStream(apiDefinitionBlob);
                }
            }
        } catch (SQLException e) {
            if (log.isDebugEnabled()) {
                log.debug("Error while retrieving async definition for api uuid: " + apiId);
            }
            throw new AsyncSpecPersistenceException("Error while retrieving async definition for api uuid: " + apiId, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(preparedStatement, connection, resultSet);
        }
        return asyncDefinition;
    }

    @Override
    public void saveGraphQLSchemaDefinition(Organization organization, String apiId, String schemaDefinition) throws GraphQLPersistenceException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        String saveOASQuery = SQLConstants.UPDATE_API_DEFINITION_SQL;
        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement(saveOASQuery);
            if (schemaDefinition != null) {
                byte[] apiDefinitionBytes = schemaDefinition.getBytes();
                preparedStatement.setBinaryStream(1, new ByteArrayInputStream(apiDefinitionBytes));
                preparedStatement.setString(2, "graphqlapi" + org.wso2.carbon.apimgt.persistence.APIConstants.GRAPHQL_SCHEMA_FILE_EXTENSION);
            }
            preparedStatement.setString(3, organization.getName());
            preparedStatement.setString(4, apiId);
            preparedStatement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            APIMgtDBUtil.rollbackConnection(connection,"Save GraphQL Schema Definition");
            if (log.isDebugEnabled()) {
                log.debug("Error occurred while updating entry in AM_API_ARTIFACT table ", e);
            }
            throw new GraphQLPersistenceException("Error while updating entry in AM_API_ARTIFACT table ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(preparedStatement, connection, null);
        }
    }

    @Override
    public String getGraphQLSchema(Organization organization, String apiId) throws GraphQLPersistenceException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String graphQLDefinition = null;
        String getGraphQLDefinitionQuery = SQLConstants.GET_API_DEFINITION_SQL;
        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement(getGraphQLDefinitionQuery);
            preparedStatement.setString(1, organization.getName());
            preparedStatement.setString(2, apiId);
            resultSet = preparedStatement.executeQuery();
            connection.commit();
            while (resultSet.next()) {
                String mediaType = resultSet.getString("MEDIA_TYPE");
                InputStream apiDefinitionBlob = resultSet.getBinaryStream("API_DEFINITION");
                if (apiDefinitionBlob != null) {
                    graphQLDefinition = APIMgtDBUtil.getStringFromInputStream(apiDefinitionBlob);
                }
            }
        } catch (SQLException e) {
            if (log.isDebugEnabled()) {
                log.debug("Error while retrieving graphQL definition for api uuid: " + apiId);
            }
            throw new GraphQLPersistenceException("Error while retrieving graphQL definition for api uuid: " + apiId, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(preparedStatement, connection, resultSet);
        }
        return graphQLDefinition;
    }

    /**
     * Adds an API revision record to the database
     *
     * @param apiRevision content of the revision
     * @throws APIManagementException if an error occurs when adding a new API revision
     */
    @Override
    public void addAPIRevision(APIRevision apiRevision) throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            try {
                connection.setAutoCommit(false);
                // Adding to AM_REVISION table
                PreparedStatement statement = connection
                        .prepareStatement(SQLConstants.APIRevisionSqlConstants.ADD_API_REVISION);
                statement.setInt(1, apiRevision.getId());
                statement.setString(2, apiRevision.getApiUUID());
                statement.setString(3, apiRevision.getRevisionUUID());
                statement.setString(4, apiRevision.getDescription());
                statement.setString(5, apiRevision.getCreatedBy());
                statement.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
                statement.executeUpdate();

                // Retrieve API ID
                APIIdentifier apiIdentifier = APIUtil.getAPIIdentifierFromUUID(apiRevision.getApiUUID());
                int apiId = getAPIID(apiRevision.getApiUUID(), connection);
                int tenantId = APIUtil.getTenantId(APIUtil.replaceEmailDomainBack(apiIdentifier.getProviderName()));
                String tenantDomain = APIUtil.getTenantDomainFromTenantId(tenantId);

                // Adding to AM_API_URL_MAPPING table
                PreparedStatement getURLMappingsStatement = connection
                        .prepareStatement(SQLConstants.APIRevisionSqlConstants.GET_URL_MAPPINGS_WITH_SCOPE_AND_PRODUCT_ID);
                getURLMappingsStatement.setInt(1, apiId);
                List<URITemplate> urlMappingList = new ArrayList<>();
                try (ResultSet rs = getURLMappingsStatement.executeQuery()) {
                    while (rs.next()) {
                        String script = null;
                        URITemplate uriTemplate = new URITemplate();
                        uriTemplate.setHTTPVerb(rs.getString(1));
                        uriTemplate.setAuthType(rs.getString(2));
                        uriTemplate.setUriTemplate(rs.getString(3));
                        uriTemplate.setThrottlingTier(rs.getString(4));
                        InputStream mediationScriptBlob = rs.getBinaryStream(5);
                        if (mediationScriptBlob != null) {
                            script = APIMgtDBUtil.getStringFromInputStream(mediationScriptBlob);
                        }
                        uriTemplate.setMediationScript(script);
                        if (!StringUtils.isEmpty(rs.getString(6))) {
                            Scope scope = new Scope();
                            scope.setKey(rs.getString(6));
                            uriTemplate.setScope(scope);
                        }
                        if (rs.getInt(7) != 0) {
                            // Adding product id to uri template id just to store value
                            uriTemplate.setId(rs.getInt(7));
                        }
                        urlMappingList.add(uriTemplate);
                    }
                }

                Map<String, URITemplate> uriTemplateMap = new HashMap<>();
                for (URITemplate urlMapping : urlMappingList) {
                    if (urlMapping.getScope() != null) {
                        URITemplate urlMappingNew = urlMapping;
                        URITemplate urlMappingExisting = uriTemplateMap.get(urlMapping.getUriTemplate()
                                + urlMapping.getHTTPVerb());
                        if (urlMappingExisting != null && urlMappingExisting.getScopes() != null) {
                            if (!urlMappingExisting.getScopes().contains(urlMapping.getScope())) {
                                urlMappingExisting.setScopes(urlMapping.getScope());
                                uriTemplateMap.put(urlMappingExisting.getUriTemplate() + urlMappingExisting.getHTTPVerb(),
                                        urlMappingExisting);
                            }
                        } else {
                            urlMappingNew.setScopes(urlMapping.getScope());
                            uriTemplateMap.put(urlMappingNew.getUriTemplate() + urlMappingNew.getHTTPVerb(),
                                    urlMappingNew);
                        }
                    } else if (urlMapping.getId() != 0) {
                        URITemplate urlMappingExisting = uriTemplateMap.get(urlMapping.getUriTemplate()
                                + urlMapping.getHTTPVerb());
                        if (urlMappingExisting == null) {
                            uriTemplateMap.put(urlMapping.getUriTemplate() + urlMapping.getHTTPVerb(), urlMapping);
                        }
                    } else {
                        uriTemplateMap.put(urlMapping.getUriTemplate() + urlMapping.getHTTPVerb(), urlMapping);
                    }
                }

                setOperationPoliciesToURITemplatesMap(apiRevision.getApiUUID(), uriTemplateMap);

                PreparedStatement insertURLMappingsStatement = connection
                        .prepareStatement(SQLConstants.APIRevisionSqlConstants.INSERT_URL_MAPPINGS);
                for (URITemplate urlMapping : uriTemplateMap.values()) {
                    insertURLMappingsStatement.setInt(1, apiId);
                    insertURLMappingsStatement.setString(2, urlMapping.getHTTPVerb());
                    insertURLMappingsStatement.setString(3, urlMapping.getAuthType());
                    insertURLMappingsStatement.setString(4, urlMapping.getUriTemplate());
                    insertURLMappingsStatement.setString(5, urlMapping.getThrottlingTier());
                    insertURLMappingsStatement.setString(6, apiRevision.getRevisionUUID());
                    insertURLMappingsStatement.addBatch();
                }
                insertURLMappingsStatement.executeBatch();

                // Add to AM_API_RESOURCE_SCOPE_MAPPING table and to AM_API_PRODUCT_MAPPING
                PreparedStatement getRevisionedURLMappingsStatement = connection
                        .prepareStatement(SQLConstants.APIRevisionSqlConstants.GET_REVISIONED_URL_MAPPINGS_ID);
                PreparedStatement insertScopeResourceMappingStatement = connection
                        .prepareStatement(SQLConstants.APIRevisionSqlConstants.INSERT_SCOPE_RESOURCE_MAPPING);
                PreparedStatement insertProductResourceMappingStatement = connection
                        .prepareStatement(SQLConstants.APIRevisionSqlConstants.INSERT_PRODUCT_RESOURCE_MAPPING);
                PreparedStatement insertOperationPolicyMappingStatement = connection
                        .prepareStatement(SQLConstants.OperationPolicyConstants.ADD_API_OPERATION_POLICY_MAPPING);

                Map<String, String> clonedPolicyMap = new HashMap<>();
                for (URITemplate urlMapping : uriTemplateMap.values()) {
                    getRevisionedURLMappingsStatement.setInt(1, apiId);
                    getRevisionedURLMappingsStatement.setString(2, apiRevision.getRevisionUUID());
                    getRevisionedURLMappingsStatement.setString(3, urlMapping.getHTTPVerb());
                    getRevisionedURLMappingsStatement.setString(4, urlMapping.getAuthType());
                    getRevisionedURLMappingsStatement.setString(5, urlMapping.getUriTemplate());
                    getRevisionedURLMappingsStatement.setString(6, urlMapping.getThrottlingTier());
                    try (ResultSet rs = getRevisionedURLMappingsStatement.executeQuery()) {
                        while (rs.next()) {
                            if (urlMapping.getScopes() != null) {
                                for (Scope scope : urlMapping.getScopes()) {
                                    insertScopeResourceMappingStatement.setString(1, scope.getKey());
                                    insertScopeResourceMappingStatement.setInt(2, rs.getInt(1));
                                    insertScopeResourceMappingStatement.setInt(3, tenantId);
                                    insertScopeResourceMappingStatement.addBatch();
                                }
                            }

                            if (urlMapping.getId() != 0) {
                                insertProductResourceMappingStatement.setInt(1, urlMapping.getId());
                                insertProductResourceMappingStatement.setInt(2, rs.getInt(1));
                                insertProductResourceMappingStatement.addBatch();
                            }

                            if (urlMapping.getOperationPolicies().size() > 0) {
                                for (OperationPolicy policy : urlMapping.getOperationPolicies()) {
                                    if (!clonedPolicyMap.keySet().contains(policy.getPolicyId())) {
                                        // Since we are creating a new revision, if the policy is not found in the policy map,
                                        // we have to clone the policy.
                                        String clonedPolicyId = revisionOperationPolicy(connection, policy.getPolicyId(),
                                                apiRevision.getApiUUID(), apiRevision.getRevisionUUID(), tenantDomain);

                                        // policy ID is stored in a map as same policy can be applied to multiple operations
                                        // and we only need to create the policy once.
                                        clonedPolicyMap.put(policy.getPolicyId(), clonedPolicyId);
                                    }

                                    Gson gson = new Gson();
                                    String paramJSON = gson.toJson(policy.getParameters());

                                    insertOperationPolicyMappingStatement.setInt(1, rs.getInt(1));
                                    insertOperationPolicyMappingStatement.setString(2, clonedPolicyMap.get(policy.getPolicyId()));
                                    insertOperationPolicyMappingStatement.setString(3, policy.getDirection());
                                    insertOperationPolicyMappingStatement.setString(4, paramJSON);
                                    insertOperationPolicyMappingStatement.setInt(5, policy.getOrder());
                                    insertOperationPolicyMappingStatement.addBatch();
                                }
                            }
                        }
                    }
                }
                insertScopeResourceMappingStatement.executeBatch();
                insertProductResourceMappingStatement.executeBatch();
                insertOperationPolicyMappingStatement.executeBatch();

                // Adding to AM_API_CLIENT_CERTIFICATE
                PreparedStatement getClientCertificatesStatement = connection
                        .prepareStatement(SQLConstants.APIRevisionSqlConstants.GET_CLIENT_CERTIFICATES);
                getClientCertificatesStatement.setInt(1, apiId);
                List<ClientCertificateDTO> clientCertificateDTOS = new ArrayList<>();
                try (ResultSet rs = getClientCertificatesStatement.executeQuery()) {
                    while (rs.next()) {
                        ClientCertificateDTO clientCertificateDTO = new ClientCertificateDTO();
                        clientCertificateDTO.setAlias(rs.getString(1));
                        clientCertificateDTO.setCertificate(APIMgtDBUtil.getStringFromInputStream(rs.getBinaryStream(2)));
                        clientCertificateDTO.setTierName(rs.getString(3));
                        clientCertificateDTOS.add(clientCertificateDTO);
                    }
                }
                PreparedStatement insertClientCertificateStatement = connection
                        .prepareStatement(SQLConstants.APIRevisionSqlConstants.INSERT_CLIENT_CERTIFICATES);
                for (ClientCertificateDTO clientCertificateDTO : clientCertificateDTOS) {
                    insertClientCertificateStatement.setInt(1, tenantId);
                    insertClientCertificateStatement.setString(2, clientCertificateDTO.getAlias());
                    insertClientCertificateStatement.setInt(3, apiId);
                    insertClientCertificateStatement.setBinaryStream(4,
                            getInputStream(clientCertificateDTO.getCertificate()));
                    insertClientCertificateStatement.setBoolean(5, false);
                    insertClientCertificateStatement.setString(6, clientCertificateDTO.getTierName());
                    insertClientCertificateStatement.setString(7, apiRevision.getRevisionUUID());
                    insertClientCertificateStatement.addBatch();
                }
                insertClientCertificateStatement.executeBatch();

                // Adding to AM_GRAPHQL_COMPLEXITY table
                PreparedStatement getGraphQLComplexityStatement = connection
                        .prepareStatement(SQLConstants.APIRevisionSqlConstants.GET_GRAPHQL_COMPLEXITY);
                List<CustomComplexityDetails> customComplexityDetailsList = new ArrayList<>();
                getGraphQLComplexityStatement.setInt(1, apiId);
                try (ResultSet rs1 = getGraphQLComplexityStatement.executeQuery()) {
                    while (rs1.next()) {
                        CustomComplexityDetails customComplexityDetails = new CustomComplexityDetails();
                        customComplexityDetails.setType(rs1.getString("TYPE"));
                        customComplexityDetails.setField(rs1.getString("FIELD"));
                        customComplexityDetails.setComplexityValue(rs1.getInt("COMPLEXITY_VALUE"));
                        customComplexityDetailsList.add(customComplexityDetails);
                    }
                }

                PreparedStatement insertGraphQLComplexityStatement = connection
                        .prepareStatement(SQLConstants.APIRevisionSqlConstants.INSERT_GRAPHQL_COMPLEXITY);
                for (CustomComplexityDetails customComplexityDetails : customComplexityDetailsList) {
                    insertGraphQLComplexityStatement.setString(1, UUID.randomUUID().toString());
                    insertGraphQLComplexityStatement.setInt(2, apiId);
                    insertGraphQLComplexityStatement.setString(3, customComplexityDetails.getType());
                    insertGraphQLComplexityStatement.setString(4, customComplexityDetails.getField());
                    insertGraphQLComplexityStatement.setInt(5, customComplexityDetails.getComplexityValue());
                    insertGraphQLComplexityStatement.setString(6, apiRevision.getRevisionUUID());
                    insertGraphQLComplexityStatement.addBatch();
                }
                insertGraphQLComplexityStatement.executeBatch();
                updateLatestRevisionNumber(connection, apiRevision.getApiUUID(), apiRevision.getId());
                addAPIRevisionMetaData(connection, apiRevision.getApiUUID(), apiRevision.getRevisionUUID());
                addAPIRevisionDefinition(connection, apiRevision.getApiUUID(), apiRevision.getRevisionUUID());
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                handleException("Failed to add API Revision entry of API UUID "
                        + apiRevision.getApiUUID(), e);
            }
        } catch (SQLException e) {
            handleException("Failed to add API Revision entry of API UUID "
                    + apiRevision.getApiUUID(), e);
        }
    }


    private void updateLatestRevisionNumber(Connection connection, String apiUUID, int revisionId) throws SQLException {

        try (PreparedStatement preparedStatement =
                     connection.prepareStatement(SQLConstants.UPDATE_REVISION_CREATED_BY_API_SQL)) {
            preparedStatement.setInt(1, revisionId);
            preparedStatement.setString(2, apiUUID);
            preparedStatement.executeUpdate();
        }
    }

    private void addAPIRevisionMetaData(Connection connection, String apiUUID, String revisionUUID) throws SQLException {

        try (PreparedStatement preparedStatement =
                     connection.prepareStatement(SQLConstants.ADD_API_REVISION_METADATA)) {
            preparedStatement.setString(1, apiUUID);
            preparedStatement.setString(2, revisionUUID);
            preparedStatement.setString(3, apiUUID);
            preparedStatement.executeUpdate();
        }
    }

    private void addAPIRevisionDefinition(Connection connection, String apiUUID, String revisionUUID) throws SQLException {

        try (PreparedStatement preparedStatement =
                     connection.prepareStatement(SQLConstants.ADD_REVISION_API_ARTIFACTS)) {
            preparedStatement.setString(1, revisionUUID);
            preparedStatement.setString(2, apiUUID);
            preparedStatement.executeUpdate();
        }
    }

    /**
     * To get the input stream from string.
     *
     * @param value : Relevant string that need to be converted to input stream.
     * @return input stream.
     */
    private InputStream getInputStream(String value) {

        byte[] cert = value.getBytes(StandardCharsets.UTF_8);
        return new ByteArrayInputStream(cert);
    }


    /**
     * Sets operation policies to uriTemplates map
     *
     * @param uuid         UUID of API or API Revision
     * @param uriTemplates URI Templates map with 'URL_PATTERN + HTTP_METHOD' as the map key
     * @throws SQLException
     * @throws APIManagementException
     */
    private void setOperationPoliciesToURITemplatesMap(String uuid, Map<String, URITemplate> uriTemplates)
            throws SQLException, APIManagementException {

        String currentApiUuid;
        String query;
        boolean isRevision = false;
        APIRevision apiRevision = checkAPIUUIDIsARevisionUUID(uuid);
        if (apiRevision != null && apiRevision.getApiUUID() != null) {
            currentApiUuid = apiRevision.getApiUUID();
            query = SQLConstants.OperationPolicyConstants.GET_OPERATION_POLICIES_FOR_API_REVISION_SQL;
            isRevision = true;
        } else {
            query = SQLConstants.OperationPolicyConstants.GET_OPERATION_POLICIES_OF_API_SQL;
            currentApiUuid = uuid;
        }

        try (Connection conn = APIMgtDBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            int apiId = getAPIID(currentApiUuid);
            ps.setInt(1, apiId);
            if (isRevision) {
                ps.setString(2, uuid);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String key = rs.getString("URL_PATTERN") + rs.getString("HTTP_METHOD");

                    URITemplate uriTemplate = uriTemplates.get(key);
                    if (uriTemplate != null) {
                        OperationPolicy operationPolicy = populateOperationPolicyWithRS(rs);
                        uriTemplate.addOperationPolicy(operationPolicy);
                    }
                }
            }
        }
    }

    /**
     * This method is used in the creating a revision for API and API product. This will create a new API specific policy
     * with API UUID and revision UUID.
     *
     * @param connection   DB connection
     * @param policyId     Original policy's ID that needs to be cloned
     * @param apiUUID      UUID of the API
     * @param revisionUUID UUID of the revision
     * @return cloned policyID
     * @throws APIManagementException
     * @throws SQLException
     **/
    private String revisionOperationPolicy(Connection connection, String policyId, String apiUUID, String revisionUUID,
                                           String organization)
            throws APIManagementException, SQLException {

        OperationPolicyData policyData = getAPISpecificOperationPolicyByPolicyID(connection, policyId, apiUUID,
                organization, true);
        // Since we import all the policies to API at API update, getting the policy from API specific policy list is enough
        if (policyData != null) {
            return addAPISpecificOperationPolicy(connection, apiUUID, revisionUUID, policyData,
                    policyData.getClonedCommonPolicyId());
        } else {
            throw new APIManagementException("Cannot create a revision of policy with ID " + policyId
                    + " as it does not exists.");
        }
    }

    /**
     * This method is used to populate AM_OPERATION_POLICY table. This will return the policy ID.
     *
     * @param connection DB connection
     * @param policyData Unique Identifier of API
     * @return UUID of the newly created policy
     * @throws SQLException
     */
    private String addOperationPolicyContent(Connection connection, OperationPolicyData policyData)
            throws SQLException {

        OperationPolicySpecification policySpecification = policyData.getSpecification();
        String dbQuery = SQLConstants.OperationPolicyConstants.ADD_OPERATION_POLICY;
        String policyUUID = UUID.randomUUID().toString();

        PreparedStatement statement = connection.prepareStatement(dbQuery);
        statement.setString(1, policyUUID);
        statement.setString(2, policySpecification.getName());
        statement.setString(3, policySpecification.getVersion());
        statement.setString(4, policySpecification.getDisplayName());
        statement.setString(5, policySpecification.getDescription());
        statement.setString(6, policySpecification.getApplicableFlows().toString());
        statement.setString(7, policySpecification.getSupportedGateways().toString());
        statement.setString(8, policySpecification.getSupportedApiTypes().toString());
        statement.setBinaryStream(9,
                new ByteArrayInputStream(APIUtil.getPolicyAttributesAsString(policySpecification).getBytes()));
        statement.setString(10, policyData.getOrganization());
        statement.setString(11, policySpecification.getCategory().toString());
        statement.setString(12, policyData.getMd5Hash());
        statement.executeUpdate();
        statement.close();

        if (policyData.getSynapsePolicyDefinition() != null) {
            addOperationPolicyDefinition(connection, policyUUID, policyData.getSynapsePolicyDefinition());
        }
        if (policyData.getCcPolicyDefinition() != null) {
            addOperationPolicyDefinition(connection, policyUUID, policyData.getCcPolicyDefinition());
        }

        return policyUUID;
    }


    private void addOperationPolicyDefinition (Connection connection, String policyId,
                                               OperationPolicyDefinition policyDefinition) throws SQLException {

        String dbQuery = SQLConstants.OperationPolicyConstants.ADD_OPERATION_POLICY_DEFINITION;
        PreparedStatement statement = connection.prepareStatement(dbQuery);
        statement.setString(1, policyId);
        statement.setString(2, policyDefinition.getGatewayType().toString());
        statement.setString(3, policyDefinition.getMd5Hash());
        statement.setBinaryStream(4, new ByteArrayInputStream(policyDefinition.getContent().getBytes()));
        statement.executeUpdate();
        statement.close();
    }

    private String addAPISpecificOperationPolicy(Connection connection, String apiUUID, String revisionUUID,
                                                 OperationPolicyData policyData, String clonedPolicyId)
            throws SQLException {

        String policyUUID = addOperationPolicyContent(connection, policyData);

        String dbQuery;
        if (revisionUUID != null) {
            dbQuery = SQLConstants.OperationPolicyConstants.ADD_API_SPECIFIC_OPERATION_POLICY_WITH_REVISION;
        } else {
            dbQuery = SQLConstants.OperationPolicyConstants.ADD_API_SPECIFIC_OPERATION_POLICY;
        }

        PreparedStatement statement = connection.prepareStatement(dbQuery);
        statement.setString(1, policyUUID);
        statement.setString(2, apiUUID);
        statement.setString(3, clonedPolicyId);
        if (revisionUUID != null) {
            statement.setString(4, revisionUUID);
        }
        statement.executeUpdate();
        statement.close();
        return policyUUID;
    }



    private OperationPolicyData getAPISpecificOperationPolicyByPolicyID(Connection connection, String policyId,
                                                                        String apiUUID,
                                                                        String organization,
                                                                        boolean isWithPolicyDefinition)
            throws SQLException {

        String dbQuery =
                SQLConstants.OperationPolicyConstants.GET_API_SPECIFIC_OPERATION_POLICY_FROM_POLICY_ID;
        OperationPolicyData policyData = null;
        PreparedStatement statement = connection.prepareStatement(dbQuery);
        statement.setString(1, policyId);
        statement.setString(2, organization);
        statement.setString(3, apiUUID);
        ResultSet rs = statement.executeQuery();
        if (rs.next()) {
            policyData = new OperationPolicyData();
            policyData.setPolicyId(policyId);
            policyData.setApiUUID(apiUUID);
            policyData.setOrganization(organization);
            policyData.setMd5Hash(rs.getString("POLICY_MD5"));
            policyData.setRevisionUUID(rs.getString("REVISION_UUID"));
            policyData.setClonedCommonPolicyId(rs.getString("CLONED_POLICY_UUID"));
            policyData.setSpecification(populatePolicySpecificationFromRS(rs));
        }
        rs.close();
        statement.close();

        if (isWithPolicyDefinition && policyData != null) {
            if (isWithPolicyDefinition && policyData != null) {
                populatePolicyDefinitions(connection, policyId, policyData);
            }
        }
        return policyData;
    }


    private List<OperationPolicyDefinition> getPolicyDefinitionForPolicyId(Connection connection, String policyId)
            throws SQLException {

        List<OperationPolicyDefinition> operationPolicyDefinitions = new ArrayList<>();

        String dbQuery = SQLConstants.OperationPolicyConstants.GET_OPERATION_POLICY_DEFINITION_FROM_POLICY_ID;
        PreparedStatement statement = connection.prepareStatement(dbQuery);
        statement.setString(1, policyId);
        ResultSet rs = statement.executeQuery();
        while (rs.next()) {
            String policyDefinitionString;
            OperationPolicyDefinition policyDefinition = new OperationPolicyDefinition();

            try (InputStream policyDefinitionStream = rs.getBinaryStream("POLICY_DEFINITION")) {
                policyDefinitionString = IOUtils.toString(policyDefinitionStream);
                policyDefinition.setContent(policyDefinitionString);
                policyDefinition.setGatewayType(
                        OperationPolicyDefinition.GatewayType.valueOf(rs.getString("GATEWAY_TYPE")));
                policyDefinition.setMd5Hash(rs.getString("DEFINITION_MD5"));

                operationPolicyDefinitions.add(policyDefinition);
            } catch (IOException e) {
                log.error("Error while converting policy definition for the policy", e);
            }

        }
        rs.close();
        statement.close();
        return operationPolicyDefinitions;
    }

    public void populatePolicyDefinitions(Connection connection, String policyId, OperationPolicyData policyData)
            throws SQLException {
        if (policyId != null && !policyId.isEmpty()) {
            List<OperationPolicyDefinition> policyDefinitions = getPolicyDefinitionForPolicyId(connection, policyId);
            for (OperationPolicyDefinition policyDefinition : policyDefinitions) {
                if (OperationPolicyDefinition.GatewayType.Synapse.equals(policyDefinition.getGatewayType())) {
                    policyData.setSynapsePolicyDefinition(policyDefinition);
                } else if (OperationPolicyDefinition.GatewayType.ChoreoConnect.equals(policyDefinition.getGatewayType())) {
                    policyData.setCcPolicyDefinition(policyDefinition);
                }
            }
        }
    }


    /**
     * This method will read the result set and populate OperationPolicySpecification object.
     *
     * @param rs Result set
     * @return OperationPolicySpecification object
     * @throws APIManagementException
     * @throws SQLException
     */
    private OperationPolicySpecification populatePolicySpecificationFromRS(ResultSet rs) throws SQLException {

        OperationPolicySpecification policySpecification = new OperationPolicySpecification();
        policySpecification.setName(rs.getString("POLICY_NAME"));
        policySpecification.setVersion(rs.getString("POLICY_VERSION"));
        policySpecification.setDisplayName(rs.getString("DISPLAY_NAME"));
        policySpecification.setDescription(rs.getString("POLICY_DESCRIPTION"));
        policySpecification.setApplicableFlows(getListFromString(rs.getString("APPLICABLE_FLOWS")));
        policySpecification.setSupportedApiTypes(getListFromString(rs.getString("API_TYPES")));
        policySpecification.setSupportedGateways(getListFromString(rs.getString("GATEWAY_TYPES")));
        policySpecification.setCategory(OperationPolicySpecification.PolicyCategory
                .valueOf(rs.getString("POLICY_CATEGORY")));
        List<OperationPolicySpecAttribute> policySpecAttributes = null;

        try (InputStream policyParametersStream = rs.getBinaryStream("POLICY_PARAMETERS")) {
            String policyParametersString = IOUtils.toString(policyParametersStream);
            policySpecAttributes = new Gson().fromJson(policyParametersString,
                    new TypeToken<List<OperationPolicySpecAttribute>>() {
                    }.getType());
        } catch (IOException e) {
            log.error("Error while converting policy specification attributes for the policy "
                    + policySpecification.getName(), e);
        }
        policySpecification.setPolicyAttributes(policySpecAttributes);
        return policySpecification;
    }

    /**
     * Create a string list from a single string element by splitting from the comma
     *
     * @param stringElement String element
     * @return list of strings
     */
    private List<String> getListFromString(String stringElement) {

        List<String> list = null;
        if (!stringElement.isEmpty()) {
            list = Arrays.asList(
                    stringElement.substring(1, stringElement.length() - 1).replaceAll("\\s", "").split(","));
        }
        return list;
    }


    /**
     * Get a provided api uuid is in the revision db table
     *
     * @return String apiUUID
     * @throws APIManagementException if an error occurs while checking revision table
     */
    @Override
    public APIRevision checkAPIUUIDIsARevisionUUID(String apiUUID) throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection
                     .prepareStatement(SQLConstants.APIRevisionSqlConstants.GET_REVISION_APIID_BY_REVISION_UUID)) {
            statement.setString(1, apiUUID);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    APIRevision apiRevision = new APIRevision();
                    apiRevision.setApiUUID(rs.getString("API_UUID"));
                    apiRevision.setId(rs.getInt("ID"));
                    apiRevision.setRevisionUUID(apiUUID);
                    return apiRevision;
                }
            }
        } catch (SQLException e) {
            handleException("Failed to search UUID: " + apiUUID + " in the revision db table", e);
        }
        return null;
    }

    public int getAPIID(String uuid) throws APIManagementException {
        int id = -1;
        try {
            try (Connection connection = APIMgtDBUtil.getConnection()) {
                return getAPIID(uuid, connection);
            }
        } catch (SQLException e) {
            handleException("Error while locating API with UUID : " + uuid + " from the database", e);
        }
        return id;
    }

    /**
     * This method will read the result set and populate OperationPolicy object, which later will be set to the URI template.
     * This object has the information regarding the policy allocation
     *
     * @param rs Result set
     * @return OperationPolicy object
     * @throws APIManagementException
     * @throws SQLException
     */
    private OperationPolicy populateOperationPolicyWithRS(ResultSet rs) throws SQLException, APIManagementException {

        OperationPolicy operationPolicy = new OperationPolicy();
        operationPolicy.setPolicyName(rs.getString("POLICY_NAME"));
        operationPolicy.setPolicyVersion(rs.getString("POLICY_VERSION"));
        operationPolicy.setPolicyId(rs.getString("POLICY_UUID"));
        operationPolicy.setOrder(rs.getInt("POLICY_ORDER"));
        operationPolicy.setDirection(rs.getString("DIRECTION"));
        operationPolicy.setParameters(APIMgtDBUtil.convertJSONStringToMap(rs.getString("PARAMETERS")));
        return operationPolicy;
    }



}

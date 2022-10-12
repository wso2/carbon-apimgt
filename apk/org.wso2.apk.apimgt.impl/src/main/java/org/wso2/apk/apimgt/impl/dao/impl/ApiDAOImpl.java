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

package org.wso2.apk.apimgt.impl.dao.impl;

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
import org.wso2.apk.apimgt.impl.dao.dto.DevPortalAPI;
import org.wso2.apk.apimgt.impl.dao.dto.DevPortalAPIInfo;
import org.wso2.apk.apimgt.impl.dao.dto.DevPortalAPISearchResult;
import org.wso2.apk.apimgt.impl.dao.dto.DevPortalContentSearchResult;
import org.wso2.apk.apimgt.impl.dao.dto.DevPortalSearchContent;
import org.wso2.apk.apimgt.impl.dao.dto.DocumentSearchContent;
import org.wso2.apk.apimgt.impl.dao.dto.Documentation;
import org.wso2.apk.apimgt.impl.dao.dto.DocumentationType;
import org.wso2.apk.apimgt.impl.dao.dto.DocumentContent;
import org.wso2.apk.apimgt.impl.dao.dto.DocumentSearchResult;
import org.wso2.apk.apimgt.impl.dao.dto.Organization;
import org.wso2.apk.apimgt.impl.dao.dto.PublisherAPI;
import org.wso2.apk.apimgt.impl.dao.dto.PublisherAPIInfo;
import org.wso2.apk.apimgt.impl.dao.dto.PublisherAPISearchResult;
import org.wso2.apk.apimgt.impl.dao.dto.PublisherContentSearchResult;
import org.wso2.apk.apimgt.impl.dao.dto.PublisherSearchContent;
import org.wso2.apk.apimgt.impl.dao.dto.SearchContent;
import org.wso2.apk.apimgt.impl.dao.dto.UserContext;
import org.wso2.apk.apimgt.impl.dao.exceptions.APIPersistenceException;
import org.wso2.apk.apimgt.impl.dao.exceptions.AsyncSpecPersistenceException;
import org.wso2.apk.apimgt.impl.dao.exceptions.DocumentationPersistenceException;
import org.wso2.apk.apimgt.impl.dao.exceptions.GraphQLPersistenceException;
import org.wso2.apk.apimgt.impl.dao.exceptions.OASPersistenceException;
import org.wso2.apk.apimgt.impl.dao.exceptions.ThumbnailPersistenceException;
import org.wso2.apk.apimgt.impl.dao.exceptions.WSDLPersistenceException;
import org.wso2.apk.apimgt.api.APIManagementException;
import org.wso2.apk.apimgt.api.ErrorHandler;
import org.wso2.apk.apimgt.api.ExceptionCodes;
import org.wso2.apk.apimgt.api.dto.ClientCertificateDTO;
import org.wso2.apk.apimgt.api.dto.KeyManagerConfigurationDTO;
import org.wso2.apk.apimgt.api.dto.UserApplicationAPIUsage;
import org.wso2.apk.apimgt.api.model.API;
import org.wso2.apk.apimgt.api.model.APIIdentifier;
import org.wso2.apk.apimgt.api.model.APIInfo;
import org.wso2.apk.apimgt.api.model.APIProduct;
import org.wso2.apk.apimgt.api.model.APIProductIdentifier;
import org.wso2.apk.apimgt.api.model.APIProductResource;
import org.wso2.apk.apimgt.api.model.APIRevision;
import org.wso2.apk.apimgt.api.model.APIRevisionDeployment;
import org.wso2.apk.apimgt.api.model.APIStatus;
import org.wso2.apk.apimgt.api.model.APIStore;
import org.wso2.apk.apimgt.api.model.Application;
import org.wso2.apk.apimgt.api.model.BlockConditionsDTO;
import org.wso2.apk.apimgt.api.model.DeployedAPIRevision;
import org.wso2.apk.apimgt.api.model.Identifier;
import org.wso2.apk.apimgt.api.model.LifeCycleEvent;
import org.wso2.apk.apimgt.api.model.OperationPolicy;
import org.wso2.apk.apimgt.api.model.OperationPolicyData;
import org.wso2.apk.apimgt.api.model.OperationPolicyDefinition;
import org.wso2.apk.apimgt.api.model.OperationPolicySpecAttribute;
import org.wso2.apk.apimgt.api.model.OperationPolicySpecification;
import org.wso2.apk.apimgt.impl.dao.dto.ResourceFile;
import org.wso2.apk.apimgt.api.model.ResourcePath;
import org.wso2.apk.apimgt.api.model.Scope;
import org.wso2.apk.apimgt.api.model.SubscribedAPI;
import org.wso2.apk.apimgt.api.model.Subscriber;
import org.wso2.apk.apimgt.api.model.Tier;
import org.wso2.apk.apimgt.api.model.URITemplate;
import org.wso2.apk.apimgt.api.model.graphql.queryanalysis.CustomComplexityDetails;
import org.wso2.apk.apimgt.impl.APIConstants;
import org.wso2.apk.apimgt.impl.APIConstants.ResourceCategory;
import org.wso2.apk.apimgt.impl.dao.ApiDAO;
import org.wso2.apk.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.apk.apimgt.impl.dao.ResourceCategoryDAO;
import org.wso2.apk.apimgt.impl.dao.constants.PostgreSQLConstants;
import org.wso2.apk.apimgt.impl.dao.constants.SQLConstants;
import org.wso2.apk.apimgt.impl.dao.mapper.APIMapper;
import org.wso2.apk.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.apk.apimgt.impl.utils.APIUtil;
import org.wso2.apk.apimgt.impl.utils.PublisherAPISearchResultComparator;
import org.wso2.apk.apimgt.impl.utils.VHostUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ApiDAOImpl implements ApiDAO {

    private static final Log log = LogFactory.getLog(ApiDAOImpl.class);
    private static ApiDAOImpl INSTANCE = null;
    private boolean forceCaseInsensitiveComparisons = false;
    private boolean multiGroupAppSharingEnabled = false;

    private ApiDAOImpl() throws APIManagementException {
        String caseSensitiveComparison = null;
        // TODO: Read caseSensitiveComparison from config
//       APIManagerConfiguration configuration = ServiceReferenceHolder.getInstance()
//                .getAPIManagerConfigurationService().getAPIManagerConfiguration();
//        String caseSensitiveComparison = ServiceReferenceHolder.getInstance().
//                getAPIManagerConfigurationService().getAPIManagerConfiguration().getFirstProperty(APIConstants.API_STORE_FORCE_CI_COMPARISIONS);
        if (caseSensitiveComparison != null) {
            forceCaseInsensitiveComparisons = Boolean.parseBoolean(caseSensitiveComparison);
        }
        multiGroupAppSharingEnabled = APIUtil.isMultiGroupAppSharingEnabled();
        initResourceCategories();
    }

    /**
     * Method to get the instance of the ApiDAOImpl.
     *
     * @return {@link ApiDAOImpl} instance
     */
    public static ApiDAOImpl getInstance() {
        if (INSTANCE == null) {
            try {
                INSTANCE = new ApiDAOImpl();
            } catch (APIManagementException e) {
                throw new RuntimeException(e);
            }
        }
        return INSTANCE;
    }

    private void handleException(String msg, Throwable t) throws APIManagementException {
        log.error(msg, t);
        throw new APIManagementException(msg, t);
    }

    @Override
    public int addAPI(Organization organization,API api) throws APIManagementException {
        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        int apiId = -1;
        String query = PostgreSQLConstants.ADD_API_SQL;

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
            prepStmt.setString(12, organization.getName());
            prepStmt.setString(13, api.getGatewayVendor());
            prepStmt.setString(14, api.getVersionTimestamp());
            prepStmt.setString(15, jsonArtifact);
            prepStmt.execute();

            rs = prepStmt.getGeneratedKeys();
            if (rs.next()) {
                apiId = rs.getInt(1);
            }

            connection.commit();

            // TODO:// tenantUsername
//            String tenantUserName = MultitenantUtils
//                    .getTenantAwareUsername(APIUtil.replaceEmailDomainBack(api.getId().getProviderName()));

            String tenantUserName = api.getId().getProviderName();
            int tenantId = APIUtil.getInternalOrganizationId(organization.getName());
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
            addAPIDefinition(organization.getName(), api,connection);
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

        String sqlQuery = PostgreSQLConstants.ADD_API_LIFECYCLE_EVENT_SQL;

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

    private int getAPIID(String uuid, Connection connection) throws APIManagementException, SQLException {

        int id = -1;
        String getAPIQuery = PostgreSQLConstants.GET_API_ID_SQL_BY_UUID;

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

    private void addUpdateAPIAsDefaultVersion(API api, Connection connection) throws APIManagementException {

        String publishedDefaultVersion = getPublishedDefaultVersion(api.getId());
        boolean deploymentAvailable = isDeploymentAvailableByAPIUUID(connection, api.getUuid());
        ArrayList<APIIdentifier> apiIdList = new ArrayList<APIIdentifier>() {{
            add(api.getId());
        }};
        removeAPIFromDefaultVersion(apiIdList, connection);

        PreparedStatement prepStmtDefVersionAdd = null;
        String queryDefaultVersionAdd = PostgreSQLConstants.ADD_API_DEFAULT_VERSION_SQL;
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

        String query = PostgreSQLConstants.GET_PUBLISHED_DEFAULT_VERSION_SQL;
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
                     connection.prepareStatement(PostgreSQLConstants.APIRevisionSqlConstants.CHECK_API_REVISION_DEPLOYMENT_AVAILABILITY_BY_API_UUID)) {
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
                     connection.prepareStatement(PostgreSQLConstants.REMOVE_API_DEFAULT_VERSION_SQL)) {

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

        String addAPIServiceMappingSQL = PostgreSQLConstants.ADD_API_SERVICE_MAPPING_SQL;
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
        String addAPIDefinition = PostgreSQLConstants.ADD_API_DEFINITION_SQL;
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
            getAPIArtefactQuery = PostgreSQLConstants.GET_REVISION_API_ARTIFACT_SQL;
        } else {
            getAPIArtefactQuery = PostgreSQLConstants.GET_API_ARTIFACT_SQL;
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
        String searchAllQuery = PostgreSQLConstants.SEARCH_ALL_APIS_SQL;

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
    public void saveWSDL(Organization organization, String apiId, ResourceFile wsdlResourceFile)
            throws WSDLPersistenceException {
//        String mediaType;
//        if (APIConstants.APPLICATION_ZIP.equals(wsdlResourceFile.getContentType())) {
//            mediaType = wsdlResourceFile.getName() + APIConstants.ZIP_FILE_EXTENSION;
//        } else {
//            mediaType = wsdlResourceFile.getName() + ".wsdl";
//        }
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        PreparedStatement preparedStatementUpdate = null;
        String saveWSDLQuery = PostgreSQLConstants.UPDATE_API_DEFINITION_SQL;
        String updateArtifactQuery = PostgreSQLConstants.UPDATE_API_ARTIFACT_SQL;
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
        String getWSDLQuery = PostgreSQLConstants.GET_API_DEFINITION_SQL;
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
        String saveOASQuery = PostgreSQLConstants.UPDATE_API_DEFINITION_SQL;
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
        String getOASDefinitionQuery = PostgreSQLConstants.GET_API_DEFINITION_SQL;
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
        String saveOASQuery = PostgreSQLConstants.UPDATE_API_DEFINITION_SQL;
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
        String getAsyncDefinitionQuery = PostgreSQLConstants.GET_API_DEFINITION_SQL;
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
        String saveOASQuery = PostgreSQLConstants.UPDATE_API_DEFINITION_SQL;
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
        String getGraphQLDefinitionQuery = PostgreSQLConstants.GET_API_DEFINITION_SQL;
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
                        .prepareStatement(PostgreSQLConstants.APIRevisionSqlConstants.ADD_API_REVISION);
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
                        .prepareStatement(PostgreSQLConstants.APIRevisionSqlConstants.GET_URL_MAPPINGS_WITH_SCOPE_AND_PRODUCT_ID);
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
                        .prepareStatement(PostgreSQLConstants.APIRevisionSqlConstants.INSERT_URL_MAPPINGS);
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
                        .prepareStatement(PostgreSQLConstants.APIRevisionSqlConstants.GET_REVISIONED_URL_MAPPINGS_ID);
                PreparedStatement insertScopeResourceMappingStatement = connection
                        .prepareStatement(PostgreSQLConstants.APIRevisionSqlConstants.INSERT_SCOPE_RESOURCE_MAPPING);
                PreparedStatement insertProductResourceMappingStatement = connection
                        .prepareStatement(PostgreSQLConstants.APIRevisionSqlConstants.INSERT_PRODUCT_RESOURCE_MAPPING);
                PreparedStatement insertOperationPolicyMappingStatement = connection
                        .prepareStatement(PostgreSQLConstants.OperationPolicyConstants.ADD_API_OPERATION_POLICY_MAPPING);

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
                        .prepareStatement(PostgreSQLConstants.APIRevisionSqlConstants.GET_CLIENT_CERTIFICATES);
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
                        .prepareStatement(PostgreSQLConstants.APIRevisionSqlConstants.INSERT_CLIENT_CERTIFICATES);
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
                        .prepareStatement(PostgreSQLConstants.APIRevisionSqlConstants.GET_GRAPHQL_COMPLEXITY);
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
                        .prepareStatement(PostgreSQLConstants.APIRevisionSqlConstants.INSERT_GRAPHQL_COMPLEXITY);
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
                     connection.prepareStatement(PostgreSQLConstants.UPDATE_REVISION_CREATED_BY_API_SQL)) {
            preparedStatement.setInt(1, revisionId);
            preparedStatement.setString(2, apiUUID);
            preparedStatement.executeUpdate();
        }
    }

    private void addAPIRevisionMetaData(Connection connection, String apiUUID, String revisionUUID) throws SQLException {

        try (PreparedStatement preparedStatement =
                     connection.prepareStatement(PostgreSQLConstants.ADD_API_REVISION_METADATA)) {
            preparedStatement.setString(1, apiUUID);
            preparedStatement.setString(2, revisionUUID);
            preparedStatement.setString(3, apiUUID);
            preparedStatement.executeUpdate();
        }
    }

    private void addAPIRevisionDefinition(Connection connection, String apiUUID, String revisionUUID) throws SQLException {

        try (PreparedStatement preparedStatement =
                     connection.prepareStatement(PostgreSQLConstants.ADD_REVISION_API_ARTIFACTS)) {
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
            query = PostgreSQLConstants.OperationPolicyConstants.GET_OPERATION_POLICIES_FOR_API_REVISION_SQL;
            isRevision = true;
        } else {
            query = PostgreSQLConstants.OperationPolicyConstants.GET_OPERATION_POLICIES_OF_API_SQL;
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
        String dbQuery = PostgreSQLConstants.OperationPolicyConstants.ADD_OPERATION_POLICY;
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

        String dbQuery = PostgreSQLConstants.OperationPolicyConstants.ADD_OPERATION_POLICY_DEFINITION;
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
            dbQuery = PostgreSQLConstants.OperationPolicyConstants.ADD_API_SPECIFIC_OPERATION_POLICY_WITH_REVISION;
        } else {
            dbQuery = PostgreSQLConstants.OperationPolicyConstants.ADD_API_SPECIFIC_OPERATION_POLICY;
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
                PostgreSQLConstants.OperationPolicyConstants.GET_API_SPECIFIC_OPERATION_POLICY_FROM_POLICY_ID;
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

        String dbQuery = PostgreSQLConstants.OperationPolicyConstants.GET_OPERATION_POLICY_DEFINITION_FROM_POLICY_ID;
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

    @Override
    public APIRevision checkAPIUUIDIsARevisionUUID(String apiUUID) throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection
                     .prepareStatement(PostgreSQLConstants.APIRevisionSqlConstants.GET_REVISION_APIID_BY_REVISION_UUID)) {
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

    @Override
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


    /**
     * Restore API revision database records as the Current API of an API
     *
     * @param apiRevision content of the revision
     * @throws APIManagementException if an error occurs when restoring an API revision
     */
    @Override
    public void restoreAPIRevision(APIRevision apiRevision) throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            try {
                connection.setAutoCommit(false);
                // Retrieve API ID
                APIIdentifier apiIdentifier = APIUtil.getAPIIdentifierFromUUID(apiRevision.getApiUUID());
                int apiId = getAPIID(apiRevision.getApiUUID(), connection);
                int tenantId = APIUtil.getTenantId(APIUtil.replaceEmailDomainBack(apiIdentifier.getProviderName()));
                String tenantDomain = APIUtil.getTenantDomainFromTenantId(tenantId);
                // Removing related Current API entries from AM_API_URL_MAPPING table
                PreparedStatement removeURLMappingsStatement = connection.prepareStatement(PostgreSQLConstants
                        .APIRevisionSqlConstants.REMOVE_CURRENT_API_ENTRIES_IN_AM_API_URL_MAPPING_BY_API_ID);
                removeURLMappingsStatement.setInt(1, apiId);
                removeURLMappingsStatement.executeUpdate();

                // Restoring to AM_API_URL_MAPPING table
                PreparedStatement getURLMappingsStatement = connection.prepareStatement(PostgreSQLConstants
                        .APIRevisionSqlConstants.GET_URL_MAPPINGS_WITH_SCOPE_AND_PRODUCT_ID_BY_REVISION_UUID);
                getURLMappingsStatement.setInt(1, apiId);
                getURLMappingsStatement.setString(2, apiRevision.getRevisionUUID());
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
                    } else {
                        uriTemplateMap.put(urlMapping.getUriTemplate() + urlMapping.getHTTPVerb(), urlMapping);
                    }
                }

                setOperationPoliciesToURITemplatesMap(apiRevision.getRevisionUUID(), uriTemplateMap);

                PreparedStatement insertURLMappingsStatement = connection
                        .prepareStatement(PostgreSQLConstants.APIRevisionSqlConstants.INSERT_URL_MAPPINGS_CURRENT_API);
                for (URITemplate urlMapping : uriTemplateMap.values()) {
                    insertURLMappingsStatement.setInt(1, apiId);
                    insertURLMappingsStatement.setString(2, urlMapping.getHTTPVerb());
                    insertURLMappingsStatement.setString(3, urlMapping.getAuthType());
                    insertURLMappingsStatement.setString(4, urlMapping.getUriTemplate());
                    insertURLMappingsStatement.setString(5, urlMapping.getThrottlingTier());
                    insertURLMappingsStatement.addBatch();
                }
                insertURLMappingsStatement.executeBatch();

                // Add to AM_API_RESOURCE_SCOPE_MAPPING table and to AM_API_PRODUCT_MAPPING
                PreparedStatement getCurrentAPIURLMappingsStatement = connection
                        .prepareStatement(PostgreSQLConstants.APIRevisionSqlConstants.GET_CURRENT_API_URL_MAPPINGS_ID);
                PreparedStatement insertScopeResourceMappingStatement = connection
                        .prepareStatement(PostgreSQLConstants.APIRevisionSqlConstants.INSERT_SCOPE_RESOURCE_MAPPING);
                PreparedStatement insertProductResourceMappingStatement = connection
                        .prepareStatement(PostgreSQLConstants.APIRevisionSqlConstants.INSERT_PRODUCT_RESOURCE_MAPPING);
                PreparedStatement insertOperationPolicyMappingStatement = connection
                        .prepareStatement(PostgreSQLConstants.OperationPolicyConstants.ADD_API_OPERATION_POLICY_MAPPING);
                PreparedStatement deleteOutdatedOperationPolicyStatement = connection
                        .prepareStatement(PostgreSQLConstants.OperationPolicyConstants.DELETE_OPERATION_POLICY_BY_POLICY_ID);

                Map<String, String> restoredPolicyMap = new HashMap<>();
                Set<String> usedClonedPolicies = new HashSet<String>();
                for (URITemplate urlMapping : uriTemplateMap.values()) {
                    if (urlMapping.getScopes() != null) {
                        getCurrentAPIURLMappingsStatement.setInt(1, apiId);
                        getCurrentAPIURLMappingsStatement.setString(2, urlMapping.getHTTPVerb());
                        getCurrentAPIURLMappingsStatement.setString(3, urlMapping.getAuthType());
                        getCurrentAPIURLMappingsStatement.setString(4, urlMapping.getUriTemplate());
                        getCurrentAPIURLMappingsStatement.setString(5, urlMapping.getThrottlingTier());
                        try (ResultSet rs = getCurrentAPIURLMappingsStatement.executeQuery()) {
                            while (rs.next()) {
                                for (Scope scope : urlMapping.getScopes()) {
                                    insertScopeResourceMappingStatement.setString(1, scope.getKey());
                                    insertScopeResourceMappingStatement.setInt(2, rs.getInt(1));
                                    insertScopeResourceMappingStatement.setInt(3, tenantId);
                                    insertScopeResourceMappingStatement.addBatch();
                                }
                            }
                        }
                    }
                    if (urlMapping.getId() != 0) {
                        getCurrentAPIURLMappingsStatement.setInt(1, apiId);
                        getCurrentAPIURLMappingsStatement.setString(2, urlMapping.getHTTPVerb());
                        getCurrentAPIURLMappingsStatement.setString(3, urlMapping.getAuthType());
                        getCurrentAPIURLMappingsStatement.setString(4, urlMapping.getUriTemplate());
                        getCurrentAPIURLMappingsStatement.setString(5, urlMapping.getThrottlingTier());
                        try (ResultSet rs = getCurrentAPIURLMappingsStatement.executeQuery()) {
                            while (rs.next()) {
                                insertProductResourceMappingStatement.setInt(1, urlMapping.getId());
                                insertProductResourceMappingStatement.setInt(2, rs.getInt(1));
                                insertProductResourceMappingStatement.addBatch();
                            }
                        }
                    }
                    if (!urlMapping.getOperationPolicies().isEmpty()) {
                        getCurrentAPIURLMappingsStatement.setInt(1, apiId);
                        getCurrentAPIURLMappingsStatement.setString(2, urlMapping.getHTTPVerb());
                        getCurrentAPIURLMappingsStatement.setString(3, urlMapping.getAuthType());
                        getCurrentAPIURLMappingsStatement.setString(4, urlMapping.getUriTemplate());
                        getCurrentAPIURLMappingsStatement.setString(5, urlMapping.getThrottlingTier());
                        try (ResultSet rs = getCurrentAPIURLMappingsStatement.executeQuery()) {
                            while (rs.next()) {
                                for (OperationPolicy policy : urlMapping.getOperationPolicies()) {
                                    if (!restoredPolicyMap.keySet().contains(policy.getPolicyName())) {
                                        String restoredPolicyId = restoreOperationPolicyRevision(connection,
                                                apiRevision.getApiUUID(), policy.getPolicyId(), apiRevision.getId(),
                                                tenantDomain);
                                        // policy ID is stored in a map as same policy can be applied to multiple operations
                                        // and we only need to create the policy once.
                                        restoredPolicyMap.put(policy.getPolicyName(), restoredPolicyId);
                                        usedClonedPolicies.add(restoredPolicyId);
                                    }

                                    Gson gson = new Gson();
                                    String paramJSON = gson.toJson(policy.getParameters());
                                    insertOperationPolicyMappingStatement.setInt(1, rs.getInt(1));
                                    insertOperationPolicyMappingStatement.setString(2, restoredPolicyMap.get(policy.getPolicyName()));
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
                deleteOutdatedOperationPolicyStatement.executeBatch();
                cleanUnusedClonedOperationPolicies(connection, usedClonedPolicies, apiRevision.getApiUUID());

                // Restoring AM_API_CLIENT_CERTIFICATE table entries
                PreparedStatement removeClientCertificatesStatement = connection.prepareStatement(PostgreSQLConstants
                        .APIRevisionSqlConstants.REMOVE_CURRENT_API_ENTRIES_IN_AM_API_CLIENT_CERTIFICATE_BY_API_ID);
                removeClientCertificatesStatement.setInt(1, apiId);
                removeClientCertificatesStatement.executeUpdate();

                PreparedStatement getClientCertificatesStatement = connection
                        .prepareStatement(PostgreSQLConstants.APIRevisionSqlConstants.GET_CLIENT_CERTIFICATES_BY_REVISION_UUID);
                getClientCertificatesStatement.setInt(1, apiId);
                getClientCertificatesStatement.setString(2, apiRevision.getRevisionUUID());
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
                        .prepareStatement(PostgreSQLConstants.APIRevisionSqlConstants.INSERT_CLIENT_CERTIFICATES_AS_CURRENT_API);
                for (ClientCertificateDTO clientCertificateDTO : clientCertificateDTOS) {
                    insertClientCertificateStatement.setInt(1, tenantId);
                    insertClientCertificateStatement.setString(2, clientCertificateDTO.getAlias());
                    insertClientCertificateStatement.setInt(3, apiId);
                    insertClientCertificateStatement.setBinaryStream(4,
                            getInputStream(clientCertificateDTO.getCertificate()));
                    insertClientCertificateStatement.setBoolean(5, false);
                    insertClientCertificateStatement.setString(6, clientCertificateDTO.getTierName());
                    insertClientCertificateStatement.setString(7, "Current API");
                    insertClientCertificateStatement.addBatch();
                }
                insertClientCertificateStatement.executeBatch();

                // Restoring AM_GRAPHQL_COMPLEXITY table
                PreparedStatement removeGraphQLComplexityStatement = connection.prepareStatement(PostgreSQLConstants
                        .APIRevisionSqlConstants.REMOVE_CURRENT_API_ENTRIES_IN_AM_GRAPHQL_COMPLEXITY_BY_API_ID);
                removeGraphQLComplexityStatement.setInt(1, apiId);
                removeGraphQLComplexityStatement.executeUpdate();

                PreparedStatement getGraphQLComplexityStatement = connection
                        .prepareStatement(PostgreSQLConstants.APIRevisionSqlConstants.GET_GRAPHQL_COMPLEXITY_BY_REVISION_UUID);
                List<CustomComplexityDetails> customComplexityDetailsList = new ArrayList<>();
                getGraphQLComplexityStatement.setInt(1, apiId);
                getGraphQLComplexityStatement.setString(2, apiRevision.getRevisionUUID());
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
                        .prepareStatement(PostgreSQLConstants.APIRevisionSqlConstants.INSERT_GRAPHQL_COMPLEXITY_AS_CURRENT_API);
                for (CustomComplexityDetails customComplexityDetails : customComplexityDetailsList) {
                    insertGraphQLComplexityStatement.setString(1, UUID.randomUUID().toString());
                    insertGraphQLComplexityStatement.setInt(2, apiId);
                    insertGraphQLComplexityStatement.setString(3, customComplexityDetails.getType());
                    insertGraphQLComplexityStatement.setString(4, customComplexityDetails.getField());
                    insertGraphQLComplexityStatement.setInt(5, customComplexityDetails.getComplexityValue());
                    insertGraphQLComplexityStatement.addBatch();
                }
                insertGraphQLComplexityStatement.executeBatch();
                restoreAPIRevisionMetaDataToWorkingCopy(connection, apiRevision.getApiUUID(),
                        apiRevision.getRevisionUUID());
                restoreAPIDefinition(connection, apiRevision.getApiUUID(),
                        apiRevision.getRevisionUUID());
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                handleException("Failed to restore API Revision entry of API UUID "
                        + apiRevision.getApiUUID(), e);
            }
        } catch (SQLException e) {
            handleException("Failed to restore API Revision entry of API UUID "
                    + apiRevision.getApiUUID(), e);
        }
    }

    /**
     * When we apply a common policy to an operation, this policy will be cloned to the API at the backend. However, if
     * that policy is removed from the UI, this method is used to clean such unused policies that are imported,
     * but not used
     *
     * @param connection            DB connection
     * @param usedClonedPoliciesSet Currently used imported API specific policies set
     * @param apiUUID               UUID of the API
     * @throws SQLException
     */
    private void cleanUnusedClonedOperationPolicies(Connection connection, Set<String> usedClonedPoliciesSet,
                                                    String apiUUID)
            throws SQLException {

        Set<String> allClonedPoliciesForAPI = getAllClonedPolicyIdsForAPI(connection, apiUUID);
        Set<String> policiesToDelete = allClonedPoliciesForAPI;
        policiesToDelete.removeAll(usedClonedPoliciesSet);
        for (String policyId : allClonedPoliciesForAPI) {
            deleteOperationPolicyByPolicyId(connection, policyId);
        }
    }

    private void deleteOperationPolicyByPolicyId(Connection connection, String policyId) throws SQLException {

        String dbQuery = PostgreSQLConstants.OperationPolicyConstants.DELETE_OPERATION_POLICY_BY_ID;
        PreparedStatement statement = connection.prepareStatement(dbQuery);
        statement.setString(1, policyId);
        statement.execute();
        statement.close();
    }

    /**
     * This method will return a list of all cloned policies for an API.
     *
     * @param connection DB connection
     * @param apiUUID    UUID of the API if exists. Null for common operation policies
     * @return List of Operation Policies
     * @throws APIManagementException
     */
    private Set<String> getAllClonedPolicyIdsForAPI(Connection connection, String apiUUID)
            throws SQLException {

        String dbQuery = PostgreSQLConstants.OperationPolicyConstants.GET_ALL_CLONED_POLICIES_FOR_API;
        Set<String> policyIds = new HashSet<>();
        PreparedStatement statement = connection.prepareStatement(dbQuery);
        statement.setString(1, apiUUID);
        ResultSet rs = statement.executeQuery();
        while (rs.next()) {
            policyIds.add(rs.getString("POLICY_UUID"));
        }
        rs.close();
        statement.close();
        return policyIds;
    }

    /**
     * This method is used to restore an API specific operation policy revision.
     *
     * @param connection   DB connection
     * @param apiUUID      UUID of the API
     * @param policyId     Original policy's ID that needs to be cloned
     * @param revisionId   The revision number
     * @param organization Organization name
     * @throws SQLException
     * @throws APIManagementException
     **/
    private String restoreOperationPolicyRevision(Connection connection, String apiUUID, String policyId,
                                                  int revisionId,
                                                  String organization) throws SQLException, APIManagementException {

        OperationPolicyData revisionedPolicy = getAPISpecificOperationPolicyByPolicyID(connection, policyId,
                apiUUID, organization, true);
        String restoredPolicyId = null;
        if (revisionedPolicy != null) {
            // First check whether there exists a API specific policy for same policy name with revision uuid null
            // This is the state where we record the policies applied in the working copy.
            OperationPolicyData apiSpecificPolicy = getAPISpecificOperationPolicyByPolicyName(connection,
                    revisionedPolicy.getSpecification().getName(), revisionedPolicy.getSpecification().getVersion(),
                    revisionedPolicy.getApiUUID(), null, organization, false);
            if (apiSpecificPolicy != null) {
                if (apiSpecificPolicy.getMd5Hash().equals(revisionedPolicy.getMd5Hash())) {
                    if (log.isDebugEnabled()) {
                        log.debug("Matching API specific operation policy found for the revisioned policy and " +
                                "MD5 hashes match");
                    }

                } else {
                    updateAPISpecificOperationPolicyWithClonedPolicyId(connection, apiSpecificPolicy.getPolicyId(),
                            revisionedPolicy);
                    if (log.isDebugEnabled()) {
                        log.debug("Even though a matching API specific operation policy found for name,"
                                + " MD5 hashes does not match. Policy " + apiSpecificPolicy.getPolicyId()
                                + " has been updated from the revision.");
                    }
                }
                restoredPolicyId = apiSpecificPolicy.getPolicyId();
            } else {
                if (revisionedPolicy.isClonedPolicy()) {
                    // Check for a common operation policy only if it is a cloned policy.
                    OperationPolicyData commonPolicy = getCommonOperationPolicyByPolicyID(connection,
                            revisionedPolicy.getClonedCommonPolicyId(), organization, false);
                    if (commonPolicy != null) {
                        if (commonPolicy.getMd5Hash().equals(revisionedPolicy.getMd5Hash())) {
                            if (log.isDebugEnabled()) {
                                log.debug("Matching common operation policy found. MD5 hash match");
                            }
                            //This means the common policy is same with our revision. A clone is created and original
                            // common policy ID is referenced as the ClonedCommonPolicyId
                            restoredPolicyId = addAPISpecificOperationPolicy(connection, apiUUID, null,
                                    revisionedPolicy, revisionedPolicy.getClonedCommonPolicyId());
                        } else {
                            // This means the common policy is updated since we created the revision.
                            // we have to create a clone and since policy is different, we can't refer the original common
                            // policy as ClonedCommonPolicyId. This should be a new API specific policy
                            revisionedPolicy.getSpecification().setName(revisionedPolicy.getSpecification().getName()
                                    + "_restored-" + revisionId);
                            revisionedPolicy.getSpecification()
                                    .setDisplayName(revisionedPolicy.getSpecification().getDisplayName()
                                            + " Restored from revision " + revisionId);
                            revisionedPolicy.setMd5Hash(APIUtil.getMd5OfOperationPolicy(revisionedPolicy));
                            revisionedPolicy.setRevisionUUID(null);
                            restoredPolicyId = addAPISpecificOperationPolicy(connection, apiUUID, null,
                                    revisionedPolicy, null);
                            if (log.isDebugEnabled()) {
                                log.debug(
                                        "An updated matching common operation policy found. A new API specific operation " +
                                                "policy created by the display name " +
                                                revisionedPolicy.getSpecification().getName());
                            }
                        }
                    } else {
                        // This means this is a clone of a deleted common policy. A new API specific policy will be created.
                        revisionedPolicy.getSpecification().setName(revisionedPolicy.getSpecification().getName()
                                + "_restored-" + revisionId);
                        revisionedPolicy.getSpecification()
                                .setDisplayName(revisionedPolicy.getSpecification().getDisplayName()
                                        + " Restored from revision " + revisionId);
                        revisionedPolicy.setMd5Hash(APIUtil.getMd5OfOperationPolicy(revisionedPolicy));
                        revisionedPolicy.setRevisionUUID(null);
                        restoredPolicyId = addAPISpecificOperationPolicy(connection, apiUUID, null, revisionedPolicy, null);
                        if (log.isDebugEnabled()) {
                            log.debug("No matching operation policy found. A new API specific operation " +
                                    "policy created by the name " + revisionedPolicy.getSpecification().getName());
                        }
                    }
                } else {
                    // This means this is a completely new policy and we don't have any reference of a previous state in
                    // working copy. A new API specific policy will be created.
                    revisionedPolicy.setRevisionUUID(null);
                    restoredPolicyId = addAPISpecificOperationPolicy(connection, apiUUID, null, revisionedPolicy, null);
                    if (log.isDebugEnabled()) {
                        log.debug("No matching operation policy found. A new API specific operation " +
                                "policy created by the name " + revisionedPolicy.getSpecification().getName());
                    }
                }
            }
        } else {
            throw new APIManagementException("A revisioned operation policy not found for " + policyId);
        }
        return restoredPolicyId;
    }

    public OperationPolicyData getCommonOperationPolicyByPolicyID(String policyId, String organization,
                                                                  boolean isWithPolicyDefinition)
            throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            return getCommonOperationPolicyByPolicyID(connection, policyId, organization, isWithPolicyDefinition);
        } catch (SQLException e) {
            handleException("Failed to get the operation policy for id " + policyId, e);
        }
        return null;
    }

    private OperationPolicyData getCommonOperationPolicyByPolicyID(Connection connection, String policyId,
                                                                   String organization,
                                                                   boolean isWithPolicyDefinition)
            throws SQLException {

        String dbQuery =
                PostgreSQLConstants.OperationPolicyConstants.GET_COMMON_OPERATION_POLICY_WITH_OUT_DEFINITION_FROM_POLICY_ID;
        PreparedStatement statement = connection.prepareStatement(dbQuery);
        statement.setString(1, policyId);
        statement.setString(2, organization);
        ResultSet rs = statement.executeQuery();
        OperationPolicyData policyData = null;
        if (rs.next()) {
            policyData = new OperationPolicyData();
            policyData.setPolicyId(policyId);
            policyData.setOrganization(organization);
            policyData.setMd5Hash(rs.getString("POLICY_MD5"));
            policyData.setSpecification(populatePolicySpecificationFromRS(rs));
        }
        rs.close();
        statement.close();

        if (isWithPolicyDefinition && policyData != null) {
            populatePolicyDefinitions(connection, policyId, policyData);
        }

        return policyData;
    }

    /**
     * This method is used the restore flow. At the restore, apart from the policy details, CLONED_POLICY_ID column
     * too can change and that needs to be updated.
     *
     * @param connection DB connection
     * @param policyId   Original policy's ID that needs to be cloned
     * @param policyData Updated policy data
     * @throws APIManagementException
     * @throws SQLException
     **/
    private void updateAPISpecificOperationPolicyWithClonedPolicyId(Connection connection, String policyId,
                                                                    OperationPolicyData policyData)
            throws SQLException {

        if (policyData.getClonedCommonPolicyId() != null) {
            PreparedStatement statement = connection.prepareStatement(
                    PostgreSQLConstants.OperationPolicyConstants.UPDATE_API_OPERATION_POLICY_BY_POLICY_ID);
            statement.setString(1, policyData.getClonedCommonPolicyId());
            statement.executeUpdate();
            statement.close();
        }
        updateOperationPolicy(connection, policyId, policyData);
    }

    /**
     * Update an existing operation policy
     *
     * @param connection DB connection
     * @param policyId   Shared policy UUID
     * @param policyData Updated policy definition
     * @throws SQLException
     */
    private void updateOperationPolicy(Connection connection, String policyId, OperationPolicyData policyData)
            throws SQLException {

        OperationPolicySpecification policySpecification = policyData.getSpecification();
        PreparedStatement statement = connection.prepareStatement(
                PostgreSQLConstants.OperationPolicyConstants.UPDATE_OPERATION_POLICY_CONTENT);

        statement.setString(1, policySpecification.getName());
        statement.setString(2, policySpecification.getVersion());
        statement.setString(3, policySpecification.getDisplayName());
        statement.setString(4, policySpecification.getDescription());
        statement.setString(5, policySpecification.getApplicableFlows().toString());
        statement.setString(6, policySpecification.getSupportedGateways().toString());
        statement.setString(7, policySpecification.getSupportedApiTypes().toString());
        statement.setBinaryStream(8,
                new ByteArrayInputStream(APIUtil.getPolicyAttributesAsString(policySpecification).getBytes()));
        statement.setString(9, policyData.getOrganization());
        statement.setString(10, policySpecification.getCategory().toString());
        statement.setString(11, policyData.getMd5Hash());
        statement.setString(12, policyId);
        statement.executeUpdate();
        statement.close();

        if (policyData.getSynapsePolicyDefinition() != null) {
            updateOperationPolicyDefinition(connection, policyId, policyData.getSynapsePolicyDefinition());
        }
        if (policyData.getCcPolicyDefinition() != null) {
            updateOperationPolicyDefinition(connection, policyId, policyData.getCcPolicyDefinition());
        }

    }

    private void updateOperationPolicyDefinition(Connection connection, String policyId,
                                                 OperationPolicyDefinition policyDefinition) throws SQLException {

        String dbQuery = PostgreSQLConstants.OperationPolicyConstants.UPDATE_OPERATION_POLICY_DEFINITION;
        PreparedStatement statement = connection.prepareStatement(dbQuery);
        statement.setString(1, policyDefinition.getMd5Hash());
        statement.setBinaryStream(2, new ByteArrayInputStream(policyDefinition.getContent().getBytes()));
        statement.setString(3, policyId);
        statement.setString(4, policyDefinition.getGatewayType().toString());
        statement.executeUpdate();
        statement.close();
    }

    private OperationPolicyData getAPISpecificOperationPolicyByPolicyName(Connection connection,
                                                                          String policyName, String policyVersion,
                                                                          String apiUUID, String revisionUUID,
                                                                          String tenantDomain,
                                                                          boolean isWithPolicyDefinition)
            throws SQLException {

        String dbQuery = PostgreSQLConstants.OperationPolicyConstants.GET_API_SPECIFIC_OPERATION_POLICY_FROM_POLICY_NAME;
        if (revisionUUID != null) {
            dbQuery += " AND AOP.REVISION_UUID = ?";
        } else {
            dbQuery += " AND AOP.REVISION_UUID IS NULL";
        }

        PreparedStatement statement = connection.prepareStatement(dbQuery);
        statement.setString(1, policyName);
        statement.setString(2, policyVersion);
        statement.setString(3, tenantDomain);
        statement.setString(4, apiUUID);
        if (revisionUUID != null) {
            statement.setString(5, revisionUUID);
        }
        ResultSet rs = statement.executeQuery();
        OperationPolicyData policyData = null;
        if (rs.next()) {
            policyData = new OperationPolicyData();
            policyData.setOrganization(tenantDomain);
            policyData.setPolicyId(rs.getString("POLICY_UUID"));
            policyData.setApiUUID(rs.getString("API_UUID"));
            policyData.setRevisionUUID(rs.getString("REVISION_UUID"));
            policyData.setMd5Hash(rs.getString("POLICY_MD5"));
            policyData.setClonedCommonPolicyId(rs.getString("CLONED_POLICY_UUID"));
            policyData.setSpecification(populatePolicySpecificationFromRS(rs));
        }

        if (isWithPolicyDefinition && policyData != null) {
            if (isWithPolicyDefinition && policyData != null) {
                populatePolicyDefinitions(connection, policyData.getPolicyId(), policyData);
            }
        }
        return policyData;
    }

    /**
     * This method will query AM_API_OPERATION_POLICY table from CLONED_POLICY_ID row for a matching policy ID
     * for the required API. This is useful to find the cloned API specific policy ID from a common policy.
     *
     * @param connection     DB connection
     * @param commonPolicyId Common policy ID
     * @param apiUUID        UUID of API
     * @return List of Operation Policies
     * @throws APIManagementException
     */
    private String getClonedPolicyIdForCommonPolicyId(Connection connection, String commonPolicyId, String apiUUID)
            throws SQLException {

        String dbQuery = PostgreSQLConstants.OperationPolicyConstants.GET_CLONED_POLICY_ID_FOR_COMMON_POLICY_ID;
        PreparedStatement statement = connection.prepareStatement(dbQuery);
        statement.setString(1, commonPolicyId);
        statement.setString(2, apiUUID);
        ResultSet rs = statement.executeQuery();
        String policyId = null;
        if (rs.next()) {
            policyId = rs.getString("POLICY_UUID");
        }
        rs.close();
        statement.close();
        return policyId;
    }

    /**
     * Restore API revision database records as the Current API of an API
     *
     * @param apiRevision content of the revision
     * @throws APIManagementException if an error occurs when restoring an API revision
     */
    @Override
    public void deleteAPIRevision(APIRevision apiRevision) throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            try {
                connection.setAutoCommit(false);
                // Retrieve API ID
                int apiId = getAPIID(apiRevision.getApiUUID(), connection);

                // Removing related revision entries from AM_REVISION table
                PreparedStatement removeAMRevisionStatement = connection.prepareStatement(PostgreSQLConstants
                        .APIRevisionSqlConstants.DELETE_API_REVISION);
                removeAMRevisionStatement.setString(1, apiRevision.getRevisionUUID());
                removeAMRevisionStatement.executeUpdate();

                // Removing related revision entries from AM_API_URL_MAPPING table
                // This will cascade remove entries from AM_API_RESOURCE_SCOPE_MAPPING and AM_API_PRODUCT_MAPPING tables
                PreparedStatement removeURLMappingsStatement = connection.prepareStatement(PostgreSQLConstants
                        .APIRevisionSqlConstants.REMOVE_REVISION_ENTRIES_IN_AM_API_URL_MAPPING_BY_REVISION_UUID);
                removeURLMappingsStatement.setInt(1, apiId);
                removeURLMappingsStatement.setString(2, apiRevision.getRevisionUUID());
                removeURLMappingsStatement.executeUpdate();

                // Removing related revision entries from AM_API_CLIENT_CERTIFICATE table
                PreparedStatement removeClientCertificatesStatement = connection.prepareStatement(PostgreSQLConstants
                        .APIRevisionSqlConstants.REMOVE_REVISION_ENTRIES_IN_AM_API_CLIENT_CERTIFICATE_BY_REVISION_UUID);
                removeClientCertificatesStatement.setInt(1, apiId);
                removeClientCertificatesStatement.setString(2, apiRevision.getRevisionUUID());
                removeClientCertificatesStatement.executeUpdate();

                // Removing related revision entries from AM_GRAPHQL_COMPLEXITY table
                PreparedStatement removeGraphQLComplexityStatement = connection.prepareStatement(PostgreSQLConstants
                        .APIRevisionSqlConstants.REMOVE_REVISION_ENTRIES_IN_AM_GRAPHQL_COMPLEXITY_BY_REVISION_UUID);
                removeGraphQLComplexityStatement.setInt(1, apiId);
                removeGraphQLComplexityStatement.setString(2, apiRevision.getRevisionUUID());
                removeGraphQLComplexityStatement.executeUpdate();
                deleteAPIRevisionMetaData(connection, apiRevision.getApiUUID(), apiRevision.getRevisionUUID());

                // Removing related revision entries from operation policies
                deleteAllAPISpecificOperationPoliciesByAPIUUID(connection, apiRevision.getApiUUID(), apiRevision.getRevisionUUID());
                deleteAPIRevisionArtifacts(connection, apiRevision.getRevisionUUID());

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                handleException("Failed to delete API Revision entry of API UUID "
                        + apiRevision.getApiUUID(), e);
            }
        } catch (SQLException e) {
            handleException("Failed to delete API Revision entry of API UUID "
                    + apiRevision.getApiUUID(), e);
        }
    }


    private void restoreAPIDefinition(Connection connection, String apiUUID, String revisionUUID)
            throws APIManagementException {;
        PreparedStatement preparedStatement = null;
        String restoreAPIRevisionQuery = PostgreSQLConstants.APIRevisionSqlConstants.RESTORE_API_REVISION_DEFINITION;
        try {
            preparedStatement = connection.prepareStatement(restoreAPIRevisionQuery);
            preparedStatement.setString(1, revisionUUID);
            preparedStatement.setString(2, apiUUID);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            APIMgtDBUtil.rollbackConnection(connection,"restore api revision");
            if (log.isDebugEnabled()) {
                log.debug("Error occurred while updating entry in AM_API_ARTIFACT table ", e);
            }
            throw new APIManagementException("Error while updating entry in AM_API_ARTIFACT table ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(preparedStatement, connection, null);
        }
    }

    private void deleteAPIRevisionArtifacts(Connection connection, String revisionUUID)
            throws APIManagementException {
        PreparedStatement preparedStatement = null;
        String deleteAPIRevisionQuery = PostgreSQLConstants.APIRevisionSqlConstants.DELETE_API_REVISION_DEFINITION;
        try {
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement(deleteAPIRevisionQuery);
            preparedStatement.setString(1, revisionUUID);
            preparedStatement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            APIMgtDBUtil.rollbackConnection(connection,"delete api revision");
            if (log.isDebugEnabled()) {
                log.debug("Error occurred while deleting entry from AM_API_ARTIFACT table ", e);
            }
            throw new APIManagementException("Error occurred while deleting entry from AM_API_ARTIFACT table ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(preparedStatement, connection, null);
        }
    }

    private void deleteAPIRevisionMetaData(Connection connection, String apiUUID, String revisionUUID) throws SQLException {

        try (PreparedStatement preparedStatement =
                     connection.prepareStatement(PostgreSQLConstants.DELETE_API_REVISION_METADATA)) {
            preparedStatement.setString(1, apiUUID);
            preparedStatement.setString(2, revisionUUID);
            preparedStatement.executeUpdate();
        }
    }

    private void restoreAPIRevisionMetaDataToWorkingCopy(Connection connection, String apiUUID, String revisionUUID) throws SQLException {

        try (PreparedStatement preparedStatement =
                     connection.prepareStatement(PostgreSQLConstants.RESTORE_API_REVISION_METADATA)) {
            preparedStatement.setString(1, apiUUID);
            preparedStatement.setString(2, revisionUUID);
            preparedStatement.setString(3, apiUUID);
            preparedStatement.executeUpdate();
        }
    }


    /**
     * Delete all the API specific policies for a given API UUID. If revision UUID is provided, only the policies that
     * are revisioned will be deleted. This is used when we delete an API revision.
     * If revision id is null, all the API specific policies will be deleted. This is used in API deleting flow.
     *
     * @param connection   DB connection
     * @param apiUUID      UUID of API
     * @param revisionUUID Revision UUID
     * @return List of Operation Policies
     * @throws APIManagementException
     */
    private void deleteAllAPISpecificOperationPoliciesByAPIUUID(Connection connection, String apiUUID,
                                                                String revisionUUID)
            throws SQLException {

        String dbQuery;
        if (revisionUUID != null) {
            dbQuery = PostgreSQLConstants.OperationPolicyConstants.GET_ALL_API_SPECIFIC_POLICIES_FOR_REVISION_UUID;
        } else {
            dbQuery = PostgreSQLConstants.OperationPolicyConstants.GET_ALL_API_SPECIFIC_POLICIES_FOR_API_ID;
        }
        PreparedStatement statement = connection.prepareStatement(dbQuery);
        statement.setString(1, apiUUID);
        if (revisionUUID != null) {
            statement.setString(2, revisionUUID);
        }
        ResultSet rs = statement.executeQuery();
        while (rs.next()) {
            String deleteQuery = PostgreSQLConstants.OperationPolicyConstants.DELETE_OPERATION_POLICY_BY_ID;
            PreparedStatement deleteStatement = connection.prepareStatement(deleteQuery);
            deleteStatement.setString(1, rs.getString("POLICY_UUID"));
            deleteStatement.execute();
            deleteStatement.close();
        }
        rs.close();
        statement.close();
    }


    @Override
    public PublisherAPI updateAPIArtifact(Organization organization, PublisherAPI publisherAPI) throws APIManagementException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        String updateAPIQuery = PostgreSQLConstants.UPDATE_FULL_API_ARTIFACT_SQL;
        API api = APIMapper.INSTANCE.toApi(publisherAPI);
        String uuid = api.getUuid();
        String json = "";
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        try {
            json = ow.writeValueAsString(publisherAPI);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement(updateAPIQuery);
            preparedStatement.setString(1, json);
            preparedStatement.setString(2, organization.getName());
            preparedStatement.setString(3, uuid);
            preparedStatement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            APIMgtDBUtil.rollbackConnection(connection,"update api");
            if (log.isDebugEnabled()) {
                log.debug("Error occurred while updating entry in AM_API table ", e);
            }
            handleException("Error while updating entry in AM_API table ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(preparedStatement, connection, null);
        }
        PublisherAPI returnAPI = APIMapper.INSTANCE.toPublisherApi(api);
        return returnAPI;
    }

    @Override
    public void deleteAPI(Organization organization, String apiUUID) throws APIManagementException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        String deleteAPIQuery = PostgreSQLConstants.DELETE_API_DEFINITION_SQL;
        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement(deleteAPIQuery);
            preparedStatement.setString(1, organization.getName());
            preparedStatement.setString(2, apiUUID);
            preparedStatement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            APIMgtDBUtil.rollbackConnection(connection,"delete api");
            if (log.isDebugEnabled()) {
                log.debug("Error occurred while deleting entry from AM_API_ARTIFACT table ", e);
            }
            handleException("Error occurred while deleting entry from AM_API_ARTIFACT table ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(preparedStatement, connection, null);
        }
    }

    @Override
    public Documentation addDocumentation(Organization organization, String apiUUID, Documentation documentation)
            throws DocumentationPersistenceException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        final String addDocumentQuery = PostgreSQLConstants.APIDocumentConstants.ADD_DOCUMENT_SQL;
        String docUUID = UUID.nameUUIDFromBytes((documentation.getName() + apiUUID).getBytes()).toString();
        String resourceUUID = UUID.randomUUID().toString();
        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            addResourceWithoutValue(connection, apiUUID, resourceUUID, ResourceCategory.DOC);

            preparedStatement = connection.prepareStatement(addDocumentQuery);
            preparedStatement.setString(1, docUUID);
            preparedStatement.setString(2, resourceUUID);
            preparedStatement.setString(3, documentation.getName());
            preparedStatement.setString(4, documentation.getSummary());
            preparedStatement.setString(5, documentation.getType().getType());
            preparedStatement.setString(6, documentation.getOtherTypeName());
            preparedStatement.setString(7, documentation.getSourceUrl());
            preparedStatement.setString(8, documentation.getFilePath());
            preparedStatement.setString(9, documentation.getSourceType().name());
            preparedStatement.setString(10, documentation.getVisibility().toString());
            preparedStatement.setString(11, null);
            preparedStatement.setString(12, null);
            preparedStatement.executeUpdate();
            connection.commit();

        } catch (SQLException e) {
            APIMgtDBUtil.rollbackConnection(connection,"add document");
            if (log.isDebugEnabled()) {
                log.debug("Error occurred while adding entry to AM_API_DOC_META_DATA table ", e);
            }
            throw new DocumentationPersistenceException("Error while persisting entry to AM_API_DOC_META_DATA table ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(preparedStatement, connection, null);
        }
        documentation.setId(docUUID);
        return documentation;
    }

    private void addResourceWithoutValue(Connection connection, String apiID, String resourceID,
                                        ResourceCategory category) throws SQLException {
        final String query = PostgreSQLConstants.APIResourceConstants.ADD_API_RESOURCES_SQL;
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, resourceID);
            statement.setString(2, apiID);
            statement.setInt(3, ResourceCategoryDAO.getResourceCategoryID(connection, category));
            statement.execute();
        }
    }

    @Override
    public Documentation updateDocumentation(Organization organization, String apiId, Documentation documentation)
            throws DocumentationPersistenceException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        final String updateDocumentQuery = PostgreSQLConstants.APIDocumentConstants.UPDATE_DOCUMENT_SQL;
        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement(updateDocumentQuery);
            preparedStatement.setString(1, documentation.getName());
            preparedStatement.setString(2, documentation.getSummary());
            preparedStatement.setString(3, documentation.getType().getType());
            preparedStatement.setString(4, documentation.getOtherTypeName());
            preparedStatement.setString(5, documentation.getSourceUrl());
            preparedStatement.setString(6, documentation.getFilePath());
            preparedStatement.setString(7, documentation.getSourceType().name());
            preparedStatement.setString(8, documentation.getVisibility().toString());
            preparedStatement.setString(9, null);
            preparedStatement.setString(10, null);
            preparedStatement.setString(11, documentation.getId());
            preparedStatement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            APIMgtDBUtil.rollbackConnection(connection,"add document");
            if (log.isDebugEnabled()) {
                log.debug("Error occurred while adding entry to AM_API_DOC_META_DATA table ", e);
            }
            throw new DocumentationPersistenceException("Error while persisting entry to AM_API_DOC_META_DATA table ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(preparedStatement, connection, null);
        }
        return documentation;
    }

    @Override
    public Documentation getDocumentation(Organization organization, String apiUUID, String docUUID)
            throws DocumentationPersistenceException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        Documentation documentation = null;
        final String getDocumentQuery = PostgreSQLConstants.APIDocumentConstants.GET_DOCUMENT_SQL;
        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement(getDocumentQuery);
            preparedStatement.setString(1, docUUID);
            resultSet = preparedStatement.executeQuery();
            connection.commit();
            while (resultSet.next()) {
                String docType = resultSet.getString("TYPE");
                DocumentationType type;
                if (docType.equalsIgnoreCase(DocumentationType.HOWTO.getType())) {
                    type = DocumentationType.HOWTO;
                } else if (docType.equalsIgnoreCase(DocumentationType.PUBLIC_FORUM.getType())) {
                    type = DocumentationType.PUBLIC_FORUM;
                } else if (docType.equalsIgnoreCase(DocumentationType.SUPPORT_FORUM.getType())) {
                    type = DocumentationType.SUPPORT_FORUM;
                } else if (docType.equalsIgnoreCase(DocumentationType.API_MESSAGE_FORMAT.getType())) {
                    type = DocumentationType.API_MESSAGE_FORMAT;
                } else if (docType.equalsIgnoreCase(DocumentationType.SAMPLES.getType())) {
                    type = DocumentationType.SAMPLES;
                } else {
                    type = DocumentationType.OTHER;
                }
                String docName = resultSet.getString("NAME");
                documentation = new Documentation(type, docName);
                documentation.setId(docUUID);
                documentation.setSummary(resultSet.getString("SUMMARY"));
                documentation.setSourceUrl(resultSet.getString("SOURCE_URL"));
                String visibilityAttr = resultSet.getString("VISIBILITY");
                Documentation.DocumentVisibility documentVisibility = Documentation.DocumentVisibility.API_LEVEL;

                if (visibilityAttr != null) {
                    if (visibilityAttr.equals(Documentation.DocumentVisibility.API_LEVEL.name())) {
                        documentVisibility = Documentation.DocumentVisibility.API_LEVEL;
                    } else if (visibilityAttr.equals(Documentation.DocumentVisibility.PRIVATE.name())) {
                        documentVisibility = Documentation.DocumentVisibility.PRIVATE;
                    } else if (visibilityAttr.equals(Documentation.DocumentVisibility.OWNER_ONLY.name())) {
                        documentVisibility = Documentation.DocumentVisibility.OWNER_ONLY;
                    }
                }
                documentation.setVisibility(documentVisibility);

                Documentation.DocumentSourceType docSourceType = Documentation.DocumentSourceType.INLINE;
                String artifactAttribute = resultSet.getString("SOURCE_TYPE");

                if (Documentation.DocumentSourceType.URL.name().equals(artifactAttribute)) {
                    docSourceType = Documentation.DocumentSourceType.URL;
                } else if (Documentation.DocumentSourceType.FILE.name().equals(artifactAttribute)) {
                    docSourceType = Documentation.DocumentSourceType.FILE;
                } else if (Documentation.DocumentSourceType.MARKDOWN.name().equals(artifactAttribute)) {
                    docSourceType = Documentation.DocumentSourceType.MARKDOWN;
                }
                documentation.setSourceType(docSourceType);
                documentation.setOtherTypeName(resultSet.getString("OTHER_TYPE_NAME"));
                documentation.setCreatedDate(parseStringToDate(resultSet.getString("CREATED_TIME")));
                documentation.setLastUpdated(parseStringToDate(resultSet.getString("LAST_UPDATED_TIME")));
                documentation.setFilePath(resultSet.getString("FILE_NAME"));
            }
        } catch (SQLException e) {
            if (log.isDebugEnabled()) {
                log.debug("Error while retrieving document for doc uuid: " + docUUID);
            }
            throw new DocumentationPersistenceException("Error while retrieving document for doc uuid: " + docUUID, e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        } finally {
            APIMgtDBUtil.closeAllConnections(preparedStatement, connection, resultSet);
        }
        return documentation;
    }

    private static Date parseStringToDate(String time) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.parse(time);
    }

    @Override
    public DocumentContent getDocumentationContent(Organization organization, String apiUUID, String docUUID) throws
            DocumentationPersistenceException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        DocumentContent documentContent = null;
        String getDocumentContentQuery = PostgreSQLConstants.APIDocumentConstants.GET_DOCUMENT_CONTENT_SQL;
        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement(getDocumentContentQuery);
            preparedStatement.setString(1, apiUUID);
            preparedStatement.setString(2, docUUID);
            resultSet = preparedStatement.executeQuery();
            connection.commit();
            while (resultSet.next()) {
                String docSourceType = resultSet.getString("DATA_TYPE");
                InputStream docBlob = resultSet.getBinaryStream("RESOURCE_BINARY_VALUE");
                String doc = null;
                if (docBlob != null) {
                    doc = APIMgtDBUtil.getStringFromInputStream(docBlob);
                }
                documentContent = new DocumentContent();
                if (StringUtils.equals(docSourceType,Documentation.DocumentSourceType.FILE.toString())) {
                    if (doc != null) {
                        ResourceFile resourceFile = new ResourceFile(docBlob, "PDF");
                        resourceFile.setName(resultSet.getString("FILE_NAME"));
                        documentContent.setResourceFile(resourceFile);
                        documentContent
                                .setSourceType(DocumentContent.ContentSourceType.valueOf(docSourceType));
                    }
                } else if (StringUtils.equals(docSourceType,Documentation.DocumentSourceType.INLINE.toString())
                        || StringUtils.equals(docSourceType,Documentation.DocumentSourceType.MARKDOWN.toString())) {
                    if (doc != null) {
                        documentContent.setTextContent(doc);
                        documentContent
                                .setSourceType(DocumentContent.ContentSourceType.valueOf(docSourceType));
                    }

                }
                else if (StringUtils.equals(docSourceType,Documentation.DocumentSourceType.URL.toString())) {

                    String sourceUrl = resultSet.getString("SOURCE_URL");
                    documentContent.setTextContent(sourceUrl);
                    documentContent
                            .setSourceType(DocumentContent.ContentSourceType.valueOf(docSourceType));
                }
            }
        } catch (SQLException e) {
            if (log.isDebugEnabled()) {
                log.debug("Error while retrieving document content for doc uuid: " + docUUID);
            }
            throw new DocumentationPersistenceException("Error while retrieving document content for doc uuid: " + docUUID, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(preparedStatement, connection, resultSet);
        }
        return documentContent;
    }

    @Override
    public DocumentContent addDocumentationContent(Organization organization, String apiUUID, String docUUID, DocumentContent documentContent) throws DocumentationPersistenceException {

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        final String addDocumentContentQuery = PostgreSQLConstants.APIDocumentConstants.ADD_DOCUMENT_CONTENT_SQL;
        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);

            preparedStatement = connection.prepareStatement(addDocumentContentQuery);
            if (documentContent.getResourceFile() != null && documentContent.getResourceFile().getContent() != null) {
                byte[] docByte = documentContent.getResourceFile().getContent().toString().getBytes();
                String stringContent = APIMgtDBUtil.getStringFromInputStream(documentContent.getResourceFile().getContent());
                stringContent.replaceAll("\u0000", "");
                preparedStatement.setBinaryStream(1, new ByteArrayInputStream(docByte));
                preparedStatement.setString(2, documentContent.getResourceFile().getContent().toString());
                preparedStatement.setString(3, documentContent.getSourceType().toString());

            } else {
                preparedStatement.setBinaryStream(1, new ByteArrayInputStream(documentContent.getTextContent().getBytes()));
                preparedStatement.setString(2, documentContent.getTextContent());
                preparedStatement.setString(3, Documentation.DocumentSourceType.INLINE.toString());
            }
            preparedStatement.setString(4, null);
            preparedStatement.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            preparedStatement.setString(6, docUUID);
            preparedStatement.executeUpdate();
            connection.commit();

        } catch (SQLException e) {
            APIMgtDBUtil.rollbackConnection(connection,"add document content");
            if (log.isDebugEnabled()) {
                log.debug("Error occurred while adding entry to AM_API_DOCUMENT table ", e);
            }
            throw new DocumentationPersistenceException("Error while persisting entry to AM_API_DOCUMENT table ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(preparedStatement, connection, null);
        }
        return documentContent;
    }

    @Override
    public DocumentSearchResult searchDocumentation(Organization org, String apiUUID, int start, int offset,
                                                    String searchQuery, UserContext ctx) throws DocumentationPersistenceException {
        DocumentSearchResult result = new DocumentSearchResult();

        int totalLength = 0;
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        String searchAllQuery = PostgreSQLConstants.APIDocumentConstants.SEARCH_ALL_DOC_SQL;

        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement(searchAllQuery);
            preparedStatement.setString(1, apiUUID);
            resultSet = preparedStatement.executeQuery();
            connection.commit();
            List<Documentation> documentationList = new ArrayList<Documentation>();
            Documentation documentation = null;
            while (resultSet.next()) {
                String docType = resultSet.getString("TYPE");
                DocumentationType type;
                if (docType.equalsIgnoreCase(DocumentationType.HOWTO.getType())) {
                    type = DocumentationType.HOWTO;
                } else if (docType.equalsIgnoreCase(DocumentationType.PUBLIC_FORUM.getType())) {
                    type = DocumentationType.PUBLIC_FORUM;
                } else if (docType.equalsIgnoreCase(DocumentationType.SUPPORT_FORUM.getType())) {
                    type = DocumentationType.SUPPORT_FORUM;
                } else if (docType.equalsIgnoreCase(DocumentationType.API_MESSAGE_FORMAT.getType())) {
                    type = DocumentationType.API_MESSAGE_FORMAT;
                } else if (docType.equalsIgnoreCase(DocumentationType.SAMPLES.getType())) {
                    type = DocumentationType.SAMPLES;
                } else {
                    type = DocumentationType.OTHER;
                }
                String docName = resultSet.getString("NAME");
                documentation = new Documentation(type, docName);
                documentation.setId(resultSet.getString("UUID"));
                documentation.setSummary(resultSet.getString("SUMMARY"));
                documentation.setSourceUrl(resultSet.getString("SOURCE_URL"));
                String visibilityAttr = resultSet.getString("VISIBILITY");
                Documentation.DocumentVisibility documentVisibility = Documentation.DocumentVisibility.API_LEVEL;

                if (visibilityAttr != null) {
                    if (visibilityAttr.equals(Documentation.DocumentVisibility.API_LEVEL.name())) {
                        documentVisibility = Documentation.DocumentVisibility.API_LEVEL;
                    } else if (visibilityAttr.equals(Documentation.DocumentVisibility.PRIVATE.name())) {
                        documentVisibility = Documentation.DocumentVisibility.PRIVATE;
                    } else if (visibilityAttr.equals(Documentation.DocumentVisibility.OWNER_ONLY.name())) {
                        documentVisibility = Documentation.DocumentVisibility.OWNER_ONLY;
                    }
                }
                documentation.setVisibility(documentVisibility);

                Documentation.DocumentSourceType docSourceType = Documentation.DocumentSourceType.INLINE;
                String artifactAttribute = resultSet.getString("SOURCE_TYPE");

                if (Documentation.DocumentSourceType.URL.name().equals(artifactAttribute)) {
                    docSourceType = Documentation.DocumentSourceType.URL;
                } else if (Documentation.DocumentSourceType.FILE.name().equals(artifactAttribute)) {
                    docSourceType = Documentation.DocumentSourceType.FILE;
                } else if (Documentation.DocumentSourceType.MARKDOWN.name().equals(artifactAttribute)) {
                    docSourceType = Documentation.DocumentSourceType.MARKDOWN;
                }
                documentation.setSourceType(docSourceType);
                documentation.setOtherTypeName(resultSet.getString("OTHER_TYPE_NAME"));
                documentation.setCreatedDate(parseStringToDate(resultSet.getString("CREATED_TIME")));
                documentation.setLastUpdated(parseStringToDate(resultSet.getString("LAST_UPDATED_TIME")));
                documentation.setFilePath(resultSet.getString("FILE_NAME"));
                if (searchQuery != null) {
                    if (searchQuery.toLowerCase().startsWith("name:")) {
                        String requestedDocName = searchQuery.split(":")[1];
                        if (documentation.getName().equalsIgnoreCase(requestedDocName)) {
                            documentationList.add(documentation);
                        }
                    } else {
                        log.warn("Document search not implemented for the query " + searchQuery);
                    }
                } else {
                    documentationList.add(documentation);
                }
                totalLength ++;
            }
            result.setDocumentationList(documentationList);
            result.setTotalDocsCount(totalLength);
            result.setReturnedDocsCount(totalLength);
        } catch (SQLException e) {
            if (log.isDebugEnabled()) {
                log.debug("Error while retrieving documents");
            }
            throw new DocumentationPersistenceException("Error while retrieving documents", e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        } finally {
            APIMgtDBUtil.closeAllConnections(preparedStatement, connection, resultSet);
        }
        return result;
    }

    @Override
    public void deleteDocumentation(Organization organization, String apiId, String docId) throws DocumentationPersistenceException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        PreparedStatement preparedStatement2 = null;
        final String deleteMetaDataQuery = PostgreSQLConstants.APIDocumentConstants.DELETE_DOC_META_DATA_SQL;
        final String deleteResourceQuery = PostgreSQLConstants.APIDocumentConstants.DELETE_DOC_RESOURCE_SQL;
        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement(deleteResourceQuery);
            preparedStatement.setString(1, apiId);
            preparedStatement.setString(2, docId);
            preparedStatement.executeUpdate();

            preparedStatement2 = connection.prepareStatement(deleteMetaDataQuery);
            preparedStatement2.setString(1, docId);
            preparedStatement2.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            APIMgtDBUtil.rollbackConnection(connection,"delete document");
            if (log.isDebugEnabled()) {
                log.debug("Error occurred while deleting entry in AM_API_DOC_META_DATA table ", e);
            }
            throw new DocumentationPersistenceException("Error while while deleting entry in AM_API_DOC_META_DATA table ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(preparedStatement, connection, null);
        }
    }

    private void initResourceCategories() throws APIManagementException {
        try (Connection connection = APIMgtDBUtil.getConnection()) {
            try {
                if (!ResourceCategoryDAO.isStandardResourceCategoriesExist(connection)) {
                    connection.setAutoCommit(false);
                    ResourceCategoryDAO.addResourceCategories(connection);
                    connection.commit();
                }
            } catch (SQLException e) {
                connection.rollback();
                throw new APIManagementException("Error while adding API resource categories", e);
            }
        } catch (SQLException e) {
            throw new APIManagementException( "Error while adding API resource categories", e);
        }
    }

    @Override
    public PublisherContentSearchResult searchContentForPublisher(Organization org, String searchQuery, int i, int i1, UserContext userContext) throws APIPersistenceException {
        int totalLength = 0;
        PublisherContentSearchResult searchResults = new PublisherContentSearchResult();
        PublisherAPI publisherAPI;
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        PreparedStatement preparedStatementDoc = null;
        ResultSet resultSet = null;
        ResultSet resultSetDoc = null;

        String searchContentQuery = PostgreSQLConstants.SEARCH_API_CONTENT_SQL;

        String modifiedSearchQuery = "%" + searchQuery.substring(8) +"%";

        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement(searchContentQuery);
            preparedStatement.setString(1, org.getName());
            preparedStatement.setString(2, modifiedSearchQuery);
            resultSet = preparedStatement.executeQuery();
            connection.commit();
            List<SearchContent> contentData = new ArrayList<>();
            while (resultSet.next()) {
                String json = resultSet.getString(1);
                ObjectMapper mapper = new ObjectMapper();
                JsonNode tree = mapper.readTree(json);
                publisherAPI = mapper.treeToValue(tree, PublisherAPI.class);
                PublisherSearchContent content = new PublisherSearchContent();
                content.setContext(publisherAPI.getContext());
                content.setDescription(publisherAPI.getDescription());
                content.setId(resultSet.getString(2));
                content.setName(publisherAPI.getApiName());
                content.setProvider(publisherAPI.getProviderName());
                content.setType("API");
                content.setVersion(publisherAPI.getVersion());
                content.setStatus(publisherAPI.getStatus());
                contentData.add(content);
                totalLength ++;
            }

            // Adding doc search
            String docSearchQuery = PostgreSQLConstants.SEARCH_DOC_CONTENT_SQL;
            String modifiedDocQuery = "";
            if (searchQuery.substring(8).split(" ").length <= 1) {
                modifiedDocQuery = searchQuery.substring(8);
            } else {
                modifiedDocQuery = searchQuery.substring(8).replace(" "," & ");
            }
            preparedStatementDoc = connection.prepareStatement(docSearchQuery);
            preparedStatementDoc.setString(1, org.getName());
            preparedStatementDoc.setString(2, modifiedDocQuery);
            resultSetDoc = preparedStatementDoc.executeQuery();
            connection.commit();
            while (resultSetDoc.next()) {
                DocumentSearchContent docSearch = new DocumentSearchContent();
                String apiUUID = resultSetDoc.getString("API_ID");
                String docUUID = resultSetDoc.getString("UUID");
                String apiType = resultSetDoc.getString("API_TYPE");
                String accociatedType;
                if (apiType.
                        equals("APIProduct")) {
                    accociatedType = "APIProduct";
                } else {
                    accociatedType = "API";
                }
                String docType = resultSetDoc.getString("TYPE");
                DocumentationType type;
                if (docType.equalsIgnoreCase(DocumentationType.HOWTO.getType())) {
                    type = DocumentationType.HOWTO;
                } else if (docType.equalsIgnoreCase(DocumentationType.PUBLIC_FORUM.getType())) {
                    type = DocumentationType.PUBLIC_FORUM;
                } else if (docType.equalsIgnoreCase(DocumentationType.SUPPORT_FORUM.getType())) {
                    type = DocumentationType.SUPPORT_FORUM;
                } else if (docType.equalsIgnoreCase(DocumentationType.API_MESSAGE_FORMAT.getType())) {
                    type = DocumentationType.API_MESSAGE_FORMAT;
                } else if (docType.equalsIgnoreCase(DocumentationType.SAMPLES.getType())) {
                    type = DocumentationType.SAMPLES;
                } else {
                    type = DocumentationType.OTHER;
                }

                String visibilityAttr = resultSetDoc.getString("VISIBILITY");
                Documentation.DocumentVisibility documentVisibility = Documentation.DocumentVisibility.API_LEVEL;

                if (visibilityAttr != null) {
                    if (visibilityAttr.equals(Documentation.DocumentVisibility.API_LEVEL.name())) {
                        documentVisibility = Documentation.DocumentVisibility.API_LEVEL;
                    } else if (visibilityAttr.equals(Documentation.DocumentVisibility.PRIVATE.name())) {
                        documentVisibility = Documentation.DocumentVisibility.PRIVATE;
                    } else if (visibilityAttr.equals(Documentation.DocumentVisibility.OWNER_ONLY.name())) {
                        documentVisibility = Documentation.DocumentVisibility.OWNER_ONLY;
                    }
                }

                Documentation.DocumentSourceType docSourceType = Documentation.DocumentSourceType.INLINE;
                String artifactAttribute = resultSetDoc.getString("SOURCE_TYPE");

                if (Documentation.DocumentSourceType.URL.name().equals(artifactAttribute)) {
                    docSourceType = Documentation.DocumentSourceType.URL;
                } else if (Documentation.DocumentSourceType.FILE.name().equals(artifactAttribute)) {
                    docSourceType = Documentation.DocumentSourceType.FILE;
                } else if (Documentation.DocumentSourceType.MARKDOWN.name().equals(artifactAttribute)) {
                    docSourceType = Documentation.DocumentSourceType.MARKDOWN;
                }
                docSearch.setApiName(resultSetDoc.getString("API_NAME"));
                docSearch.setApiProvider(resultSetDoc.getString("API_PROVIDER"));
                docSearch.setApiVersion(resultSetDoc.getString("API_VERSION"));
                docSearch.setApiUUID(apiUUID);
                docSearch.setAssociatedType(accociatedType);
                docSearch.setDocType(type);
                docSearch.setId(docUUID);
                docSearch.setSourceType(docSourceType);
                docSearch.setVisibility(documentVisibility);
                docSearch.setName(resultSetDoc.getString("NAME"));
                contentData.add(docSearch);
                totalLength ++;
            }
            searchResults.setResults(contentData);
            searchResults.setReturnedCount(contentData.size());
            searchResults.setTotalCount(totalLength);

        } catch (SQLException e) {
            if (log.isDebugEnabled()) {
                log.debug("Error while content searching api artefacts");
            }
            throw new APIPersistenceException("Error while content searching api artefacts", e);
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
    public void saveThumbnail(Organization organization, String apiId, ResourceFile resourceFile) throws ThumbnailPersistenceException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        String saveThumbnailQuery = PostgreSQLConstants.UPDATE_API_ARTIFACT_SQL;
        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            if (isResourceExistsForCategory(connection, apiId, ResourceCategory.IMAGE)) {
                updateBinaryResourceForCategory(connection, apiId, ResourceCategory.IMAGE, resourceFile.getContent(), null);
            } else {
                addBinaryResource(connection, apiId, UUID.randomUUID().toString(),
                        ResourceCategory.IMAGE, resourceFile.getContentType(), resourceFile.getContent(), null);
            }
            preparedStatement = connection.prepareStatement(saveThumbnailQuery);
            if (resourceFile.getContent() != null) {
                preparedStatement.setString(1, "thumbnail");
                preparedStatement.setString(2, org.wso2.carbon.apimgt.persistence.APIConstants.API_ICON_IMAGE + resourceFile.getContentType());
                preparedStatement.setString(3, organization.getName());
                preparedStatement.setString(4, apiId);
            }
            preparedStatement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            APIMgtDBUtil.rollbackConnection(connection,"Save API Thumbnail");
            if (log.isDebugEnabled()) {
                log.debug("Error occurred while updating entry in AM_API_RESOURCES table ", e);
            }
            throw new ThumbnailPersistenceException("Error while updating entry in AM_API_RESOURCES table ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(preparedStatement, connection, null);
        }
    }

    private void addBinaryResource(Connection connection, String apiID, String resourceID, ResourceCategory category,
                                  String dataType, InputStream binaryValue, String createdBy) throws SQLException {
        final String query = PostgreSQLConstants.APIResourceConstants.ADD_BINARY_RESOURCES_SQL;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, resourceID);
            statement.setString(2, apiID);
            statement.setInt(3, ResourceCategoryDAO.getResourceCategoryID(connection, category));
            statement.setString(4, dataType);
            statement.setBinaryStream(5, binaryValue);
            statement.setString(6, createdBy);
            statement.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
            statement.setString(8, createdBy);
            statement.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
            statement.execute();
        }
    }

    @Override
    public ResourceFile getThumbnail(Organization organization, String apiId) throws ThumbnailPersistenceException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        ResourceFile returnResource = null;
        final String getThumbnailQuery = PostgreSQLConstants.APIResourceConstants.GET_THUMBNAIL_SQL;
        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement(getThumbnailQuery);
            preparedStatement.setString(1, apiId);
            preparedStatement.setInt(2, ResourceCategoryDAO.getResourceCategoryID(connection, ResourceCategory.IMAGE));
            resultSet = preparedStatement.executeQuery();
            connection.commit();
            while (resultSet.next()) {
                String mediaType = resultSet.getString("DATA_TYPE");
                InputStream thumbnailBlob = resultSet.getBinaryStream("RESOURCE_BINARY_VALUE");
                if (thumbnailBlob != null) {
                    byte[] artifactByte = APIMgtDBUtil.getBytesFromInputStream(thumbnailBlob);
                    try (InputStream newArtifact = new ByteArrayInputStream(artifactByte)) {
                        returnResource = new ResourceFile(newArtifact, mediaType);
                    } catch (IOException e) {
                        throw new ThumbnailPersistenceException("Error occurred retrieving input stream from byte array.", e);
                    }
                }
            }
        } catch (SQLException e) {
            if (log.isDebugEnabled()) {
                log.debug("Error while retrieving Thumbnail for api uuid: " + apiId);
            }
            throw new ThumbnailPersistenceException("Error while retrieving Thumbnail for api uuid: " + apiId, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(preparedStatement, connection, resultSet);
        }
        return returnResource;
    }

    private InputStream getBinaryValueForCategory(Connection connection, String apiID,
                                                 ResourceCategory category)
            throws SQLException, IOException {
        final String query = PostgreSQLConstants.APIResourceConstants.GET_BINARY_VALUE_BY_CATEGORY_SQL;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, apiID);
            statement.setInt(2, ResourceCategoryDAO.getResourceCategoryID(connection, category));
            statement.execute();

            try (ResultSet rs =  statement.getResultSet()) {
                if (rs.next()) {
                    InputStream inputStream = new ByteArrayInputStream(IOUtils.toByteArray(rs.getBinaryStream
                            ("RESOURCE_BINARY_VALUE")));
                    return inputStream;
                }
            }
        }
        return null;
    }

    @Override
    public void deleteThumbnail(Organization organization, String apiId) throws ThumbnailPersistenceException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        String deleteThumbnailQuery = PostgreSQLConstants.UPDATE_API_ARTIFACT_SQL;
        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            deleteUniqueResourceForCategory(connection,apiId,ResourceCategory.IMAGE);
            preparedStatement = connection.prepareStatement(deleteThumbnailQuery);
            preparedStatement.setString(1, "thumbnail");
            preparedStatement.setString(2, null);
            preparedStatement.setString(3, organization.getName());
            preparedStatement.setString(4, apiId);
            preparedStatement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            APIMgtDBUtil.rollbackConnection(connection,"Delete API Thumbnail");
            if (log.isDebugEnabled()) {
                log.debug("Error occurred while updating entry in AM_API_RESOURCES table ", e);
            }
            throw new ThumbnailPersistenceException("Error while updating entry in AM_API_RESOURCES table ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(preparedStatement, connection, null);
        }
    }

    private boolean isResourceExistsForCategory(Connection connection, String apiID,
                                               ResourceCategory category) throws SQLException {
        final String query = PostgreSQLConstants.APIResourceConstants.RESOURCE_EXIST_CHECK_SQL;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, apiID);
            statement.setInt(2, ResourceCategoryDAO.getResourceCategoryID(connection, category));
            statement.execute();

            try (ResultSet rs =  statement.getResultSet()) {
                if (rs.next()) {
                    return true;
                }
            }
        }

        return false;
    }

    private void updateBinaryResourceForCategory(Connection connection, String apiID, ResourceCategory category,
                                                InputStream resourceValue, String updatedBy)
            throws SQLException {
        final String query = PostgreSQLConstants.APIResourceConstants.UPDATE_BINARY_RESOURCE_SQL;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setBinaryStream(1, resourceValue);
            statement.setString(2, updatedBy);
            statement.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            statement.setString(4, apiID);
            statement.setInt(5, ResourceCategoryDAO.getResourceCategoryID(connection, category));

            statement.execute();
        }
    }

    static void deleteUniqueResourceForCategory(Connection connection, String apiID, ResourceCategory resourceCategory)
            throws SQLException {
        final String query = PostgreSQLConstants.APIResourceConstants.DELETE_UNIQUE_RESOURCE_BY_CATEGORY_SQL;
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, apiID);
            statement.setInt(2, ResourceCategoryDAO.getResourceCategoryID(connection, resourceCategory));

            statement.execute();
        }
    }

    @Override
    public DevPortalAPI getDevPortalAPI(Organization organization, String apiId) throws APIPersistenceException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        PublisherAPI api = null;
        DevPortalAPI devPortalAPI = null;
        String getAPIArtefactQuery = PostgreSQLConstants.GET_DEVPORTAL_API_ARTIFACT_SQL;
        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement(getAPIArtefactQuery);
            preparedStatement.setString(1, apiId);
            preparedStatement.setString(2, organization.getName());
            resultSet = preparedStatement.executeQuery();
            connection.commit();
            while (resultSet.next()) {
                String json = resultSet.getString("ARTIFACT");
                ObjectMapper mapper = new ObjectMapper();
                JsonNode tree = mapper.readTree(json);
                api = mapper.treeToValue(tree, PublisherAPI.class);
                String mediaType = resultSet.getString("MEDIA_TYPE");
                InputStream apiDefinitionBlob = resultSet.getBinaryStream("API_DEFINITION");
                if (apiDefinitionBlob != null) {
                    if (StringUtils.equals("swagger.json",mediaType)) {
                        api.setSwaggerDefinition(APIMgtDBUtil.getStringFromInputStream(apiDefinitionBlob));
                    } else if (StringUtils.equals("asyncapi.json",mediaType)) {
                        api.setAsyncApiDefinition(APIMgtDBUtil.getStringFromInputStream(apiDefinitionBlob));
                    }
                }
                API apiObject = APIMapper.INSTANCE.toApi(api);
                devPortalAPI = APIMapper.INSTANCE.toDevPortalApi(apiObject);
            }
        } catch (SQLException e) {
            if (log.isDebugEnabled()) {
                log.debug("Error while retrieving api artefact for API uuid: " + apiId);
                throw new APIPersistenceException("Error while retrieving api artefact for API uuid: " + apiId, e);
            } else {
                throw new APIPersistenceException("Error while retrieving api artefact for API uuid: " + apiId, e);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } finally {
            APIMgtDBUtil.closeAllConnections(preparedStatement, connection, resultSet);
        }
        return devPortalAPI;
    }

    @Override
    public DevPortalAPISearchResult searchAPIsForDevPortal(Organization organization, String searchQuery, int start,
                                                           int offset, UserContext ctx) throws APIPersistenceException {
        DevPortalAPISearchResult result = null;
        String searchAllQuery = PostgreSQLConstants.SEARCH_ALL_DEVPORTAL_SQL;
        if (StringUtils.isEmpty(searchQuery)) {
            result = searchPaginatedDevportalAPIs(organization.getName(), searchAllQuery, start, offset);
        }
        return result;
    }


    private DevPortalAPISearchResult searchPaginatedDevportalAPIs(String org, String searchQuery, int start,
                                                                  int offset) throws APIPersistenceException {
        int totalLength = 0;
        DevPortalAPISearchResult searchResults = new DevPortalAPISearchResult();
        PublisherAPI devPortalAPI;
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
            List<DevPortalAPIInfo> devportalAPIInfoList = new ArrayList<>();
            while (resultSet.next()) {
                String json = resultSet.getString(1);
                ObjectMapper mapper = new ObjectMapper();
                JsonNode tree = mapper.readTree(json);
                devPortalAPI = mapper.treeToValue(tree, PublisherAPI.class);
                DevPortalAPIInfo apiInfo = new DevPortalAPIInfo();
                apiInfo.setType(devPortalAPI.getType());
                apiInfo.setId(resultSet.getString(2));
                apiInfo.setApiName(devPortalAPI.getApiName());
                apiInfo.setDescription(devPortalAPI.getDescription());
                apiInfo.setContext(devPortalAPI.getContext());
                apiInfo.setProviderName(devPortalAPI.getProviderName());
                apiInfo.setStatus(devPortalAPI.getStatus());
                apiInfo.setThumbnail(devPortalAPI.getThumbnail());
                apiInfo.setVersion(devPortalAPI.getVersion());
                apiInfo.setCreatedTime(devPortalAPI.getCreatedTime());
                apiInfo.setBusinessOwner(devPortalAPI.getBusinessOwner());
                apiInfo.setAvailableTierNames(devPortalAPI.getAvailableTierNames());
                apiInfo.setSubscriptionAvailability(devPortalAPI.getSubscriptionAvailability());
                apiInfo.setSubscriptionAvailableOrgs(devPortalAPI.getSubscriptionAvailableOrgs());
                //apiInfo.setGatewayVendor(devPortalAPI.getAPIVendor());
                devportalAPIInfoList.add(apiInfo);
                totalLength ++;
            }
            searchResults.setDevPortalAPIInfoList(devportalAPIInfoList);
            searchResults.setReturnedAPIsCount(devportalAPIInfoList.size());
            searchResults.setTotalAPIsCount(totalLength);
        } catch (SQLException e) {
            if (log.isDebugEnabled()) {
                log.debug("Error while retrieving api artefacts");
            }
            throw new APIPersistenceException("Error while retrieving api artefacts", e);
        } catch (JsonProcessingException e) {
            throw new APIPersistenceException("Error while retrieving api artefacts", e);
        } catch (IOException e) {
            throw new APIPersistenceException("Error while retrieving api artefacts", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(preparedStatement, connection, resultSet);
        }
        return searchResults;
    }

    @Override
    public DevPortalContentSearchResult searchContentForDevPortal(Organization org, String searchQuery, int start, int offset, UserContext ctx) throws APIPersistenceException {
        int totalLength = 0;
        DevPortalContentSearchResult searchResults = new DevPortalContentSearchResult();
        PublisherAPI publisherAPI;
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        PreparedStatement preparedStatementDoc = null;
        ResultSet resultSet = null;
        ResultSet resultSetDoc = null;

        String searchContentQuery = PostgreSQLConstants.SEARCH_ALL_CONTENT_DEVPORTAL_SQL;

        String modifiedSearchQuery = "%" + searchQuery.substring(8) +"%";

        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement(searchContentQuery);
            preparedStatement.setString(1, org.getName());
            preparedStatement.setString(2, modifiedSearchQuery);
            resultSet = preparedStatement.executeQuery();
            connection.commit();
            List<SearchContent> contentData = new ArrayList<>();
            while (resultSet.next()) {
                String json = resultSet.getString(1);
                ObjectMapper mapper = new ObjectMapper();
                JsonNode tree = mapper.readTree(json);
                publisherAPI = mapper.treeToValue(tree, PublisherAPI.class);
                DevPortalSearchContent content = new DevPortalSearchContent();
                content.setContext(publisherAPI.getContext());
                content.setDescription(publisherAPI.getDescription());
                content.setId(resultSet.getString(2));
                content.setName(publisherAPI.getApiName());
                content.setProvider(publisherAPI.getProviderName());
                content.setType("API");
                content.setVersion(publisherAPI.getVersion());
                content.setStatus(publisherAPI.getStatus());
                contentData.add(content);
                totalLength ++;
            }

            // Adding doc search
            String docSearchQuery = PostgreSQLConstants.SEARCH_ALL_DOC_DEVPORTAL_SQL;
            String modifiedDocQuery = "";
            if (searchQuery.substring(8).split(" ").length <= 1) {
                modifiedDocQuery = searchQuery.substring(8);
            } else {
                modifiedDocQuery = searchQuery.substring(8).replace(" "," & ");
            }
            preparedStatementDoc = connection.prepareStatement(docSearchQuery);
            preparedStatementDoc.setString(1, org.getName());
            preparedStatementDoc.setString(2, modifiedDocQuery);
            resultSetDoc = preparedStatementDoc.executeQuery();
            connection.commit();
            while (resultSetDoc.next()) {
                DocumentSearchContent docSearch = new DocumentSearchContent();
                String apiUUID = resultSetDoc.getString("API_ID");
                String docUUID = resultSetDoc.getString("UUID");
                String apiType = resultSetDoc.getString("API_TYPE");
                String accociatedType;
                if (apiType.
                        equals("APIProduct")) {
                    accociatedType = "APIProduct";
                } else {
                    accociatedType = "API";
                }
                String docType = resultSetDoc.getString("TYPE");
                DocumentationType type;
                if (docType.equalsIgnoreCase(DocumentationType.HOWTO.getType())) {
                    type = DocumentationType.HOWTO;
                } else if (docType.equalsIgnoreCase(DocumentationType.PUBLIC_FORUM.getType())) {
                    type = DocumentationType.PUBLIC_FORUM;
                } else if (docType.equalsIgnoreCase(DocumentationType.SUPPORT_FORUM.getType())) {
                    type = DocumentationType.SUPPORT_FORUM;
                } else if (docType.equalsIgnoreCase(DocumentationType.API_MESSAGE_FORMAT.getType())) {
                    type = DocumentationType.API_MESSAGE_FORMAT;
                } else if (docType.equalsIgnoreCase(DocumentationType.SAMPLES.getType())) {
                    type = DocumentationType.SAMPLES;
                } else {
                    type = DocumentationType.OTHER;
                }

                String visibilityAttr = resultSetDoc.getString("VISIBILITY");
                Documentation.DocumentVisibility documentVisibility = Documentation.DocumentVisibility.API_LEVEL;

                if (visibilityAttr != null) {
                    if (visibilityAttr.equals(Documentation.DocumentVisibility.API_LEVEL.name())) {
                        documentVisibility = Documentation.DocumentVisibility.API_LEVEL;
                    } else if (visibilityAttr.equals(Documentation.DocumentVisibility.PRIVATE.name())) {
                        documentVisibility = Documentation.DocumentVisibility.PRIVATE;
                    } else if (visibilityAttr.equals(Documentation.DocumentVisibility.OWNER_ONLY.name())) {
                        documentVisibility = Documentation.DocumentVisibility.OWNER_ONLY;
                    }
                }

                Documentation.DocumentSourceType docSourceType = Documentation.DocumentSourceType.INLINE;
                String artifactAttribute = resultSetDoc.getString("SOURCE_TYPE");

                if (Documentation.DocumentSourceType.URL.name().equals(artifactAttribute)) {
                    docSourceType = Documentation.DocumentSourceType.URL;
                } else if (Documentation.DocumentSourceType.FILE.name().equals(artifactAttribute)) {
                    docSourceType = Documentation.DocumentSourceType.FILE;
                } else if (Documentation.DocumentSourceType.MARKDOWN.name().equals(artifactAttribute)) {
                    docSourceType = Documentation.DocumentSourceType.MARKDOWN;
                }
                docSearch.setApiName(resultSetDoc.getString("API_NAME"));
                docSearch.setApiProvider(resultSetDoc.getString("API_PROVIDER"));
                docSearch.setApiVersion(resultSetDoc.getString("API_VERSION"));
                docSearch.setApiUUID(apiUUID);
                docSearch.setAssociatedType(accociatedType);
                docSearch.setDocType(type);
                docSearch.setId(docUUID);
                docSearch.setSourceType(docSourceType);
                docSearch.setVisibility(documentVisibility);
                docSearch.setName(resultSetDoc.getString("NAME"));
                contentData.add(docSearch);
                totalLength ++;
            }
            searchResults.setResults(contentData);
            searchResults.setReturnedCount(contentData.size());
            searchResults.setTotalCount(totalLength);

        } catch (SQLException e) {
            if (log.isDebugEnabled()) {
                log.debug("Error while content searching api artefacts");
            }
            throw new APIPersistenceException("Error while content searching api artefacts", e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            APIMgtDBUtil.closeAllConnections(preparedStatement, connection, resultSet);
        }
        return searchResults;
    }

    /**
     * Add URI Templates to database with resource scope mappings.
     *
     * @param tenantId    Tenant ID of an API
     * @param apiId API Id
     * @param api      API to add URI templates of

     * @throws APIManagementException If an error occurs while adding URI templates.
     */
    @Override
    public void addURITemplates(int tenantId, int apiId, API api) throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            connection.setAutoCommit(false);
            try {
                addURITemplates(apiId, api, tenantId, connection);
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                handleException("Error while adding URL template(s) to the database for API : " + api.getId(), e);
            }
        } catch (SQLException e) {
            handleException("Error while adding URL template(s) to the database for API : " + api.getId(), e);
        }
    }

    /**
     * Add URI Templates to database with resource scope mappings by passing the DB connection.
     *
     * @param apiId      API Id
     * @param api        API
     * @param tenantId   Organization name
     * @param connection Existing DB Connection
     * @throws SQLException If a SQL error occurs while adding URI Templates
     */
    private void addURITemplates(int apiId, API api, int tenantId, Connection connection)
            throws SQLException, APIManagementException {

        String dbProductName = connection.getMetaData().getDatabaseProductName();
        String tenantDomain = api.getOrganization();
        try (PreparedStatement uriMappingPrepStmt = connection.prepareStatement(PostgreSQLConstants.ADD_URL_MAPPING_SQL,
                new String[]{ "URL_MAPPING_ID".toLowerCase() });
             PreparedStatement uriScopeMappingPrepStmt =
                     connection.prepareStatement(PostgreSQLConstants.ADD_API_RESOURCE_SCOPE_MAPPING);
             PreparedStatement operationPolicyMappingPrepStmt =
                     connection.prepareStatement(PostgreSQLConstants.OperationPolicyConstants.ADD_API_OPERATION_POLICY_MAPPING)) {
            Map<String, String> updatedPoliciesMap = new HashMap<>();
            Set<String> usedClonedPolicies = new HashSet<String>();
            for (URITemplate uriTemplate : api.getUriTemplates()) {
                uriMappingPrepStmt.setInt(1, apiId);
                uriMappingPrepStmt.setString(2, uriTemplate.getHTTPVerb());
                uriMappingPrepStmt.setString(3, uriTemplate.getAuthType());
                uriMappingPrepStmt.setString(4, uriTemplate.getUriTemplate());
                //If API policy is available then set it for all the resources.
                if (StringUtils.isEmpty(api.getApiLevelPolicy())) {
                    uriMappingPrepStmt.setString(5, (StringUtils.isEmpty(uriTemplate.getThrottlingTier())) ?
                            APIConstants.UNLIMITED_TIER :
                            uriTemplate.getThrottlingTier());
                } else {
                    uriMappingPrepStmt.setString(5, (StringUtils.isEmpty(
                            api.getApiLevelPolicy())) ? APIConstants.UNLIMITED_TIER : api.getApiLevelPolicy());
                }
                InputStream is = null;
                if (uriTemplate.getMediationScript() != null) {
                    is = new ByteArrayInputStream(
                            uriTemplate.getMediationScript().getBytes(Charset.defaultCharset()));
                }
                if (connection.getMetaData().getDriverName().contains("PostgreSQL") || connection.getMetaData()
                        .getDatabaseProductName().contains("DB2")) {
                    if (uriTemplate.getMediationScript() != null) {
                        uriMappingPrepStmt.setBinaryStream(6, is, uriTemplate.getMediationScript()
                                .getBytes(Charset.defaultCharset()).length);
                    } else {
                        uriMappingPrepStmt.setBinaryStream(6, is, 0);
                    }
                } else {
                    uriMappingPrepStmt.setBinaryStream(6, is);
                }
                uriMappingPrepStmt.execute();
                int uriMappingId = -1;
                try (ResultSet resultIdSet = uriMappingPrepStmt.getGeneratedKeys()) {
                    while (resultIdSet.next()) {
                        uriMappingId = resultIdSet.getInt(1);
                    }
                }
                if (uriMappingId != -1) {
                    for (Scope uriTemplateScope : uriTemplate.retrieveAllScopes()) {
                        String scopeKey = uriTemplateScope.getKey();
                        if (log.isDebugEnabled()) {
                            log.debug("Adding scope to resource mapping for scope key: " + scopeKey +
                                    " and URL mapping Id: " + uriMappingId);
                        }
                        uriScopeMappingPrepStmt.setString(1, scopeKey);
                        uriScopeMappingPrepStmt.setInt(2, uriMappingId);
                        uriScopeMappingPrepStmt.setInt(3, tenantId);
                        uriScopeMappingPrepStmt.addBatch();
                    }

                    if (uriTemplate.getOperationPolicies() != null) {
                        for (OperationPolicy policy : uriTemplate.getOperationPolicies()) {
                            if (!updatedPoliciesMap.keySet().contains(policy.getPolicyId())) {
                                OperationPolicyData existingPolicy =
                                        getAPISpecificOperationPolicyByPolicyID(policy.getPolicyId(), api.getUuid(),
                                                tenantDomain, false);
                                String clonedPolicyId = policy.getPolicyId();
                                if (existingPolicy != null) {
                                    if (existingPolicy.isClonedPolicy()) {
                                        usedClonedPolicies.add(clonedPolicyId);
                                    }
                                } else {
                                    // Even though the policy ID attached is not in the API specific policy list,
                                    // it can be a common policy and we need to verify that it has not been previously cloned
                                    // for the API before cloning again.
                                    clonedPolicyId = getClonedPolicyIdForCommonPolicyId(connection,
                                            policy.getPolicyId(), api.getUuid());
                                    if (clonedPolicyId == null) {
                                        clonedPolicyId = cloneOperationPolicy(connection, policy.getPolicyId(),
                                                api.getUuid(), null);
                                    }
                                    usedClonedPolicies.add(clonedPolicyId);
                                    //usedClonedPolicies set will not contain used API specific policies that are not cloned.
                                    //TODO: discuss whether we need to clone API specific policies as well
                                }

                                // Updated policies map will record the updated policy ID for the used policy ID.
                                // If the policy has been cloned to the API specific policy list, we need to use the
                                // updated policy Id.
                                updatedPoliciesMap.put(policy.getPolicyId(), clonedPolicyId);
                            }

                            Gson gson = new Gson();
                            String paramJSON = gson.toJson(policy.getParameters());
                            if (log.isDebugEnabled()) {
                                log.debug("Adding operation policy " + policy.getPolicyName() + " for API "
                                        + api.getId().getApiName() + " to URL mapping Id " + uriMappingId);
                            }

                            operationPolicyMappingPrepStmt.setInt(1, uriMappingId);
                            operationPolicyMappingPrepStmt.setString(2, updatedPoliciesMap.get(policy.getPolicyId()));
                            operationPolicyMappingPrepStmt.setString(3, policy.getDirection());
                            operationPolicyMappingPrepStmt.setString(4, paramJSON);
                            operationPolicyMappingPrepStmt.setInt(5, policy.getOrder());
                            operationPolicyMappingPrepStmt.addBatch();
                        }
                    }
                }
                uriTemplate.setId(uriMappingId);
            } // end URITemplate list iteration
            uriScopeMappingPrepStmt.executeBatch();
            operationPolicyMappingPrepStmt.executeBatch();
            cleanUnusedClonedOperationPolicies(connection, usedClonedPolicies, api.getUuid());
        }
    }

    /**
     * Get the API specific operation policy from the policy ID if exists. This method will take the intersection of AM_OPERATION_POLICY
     * table and AM_API_OPERATION_POLICY table from API UUID. Policy id might be available, but if it is not referenced in the
     * APIS table, this will return null.
     * The returned policy data can be either an API only policy, cloned common policy to API or a revisioned API specific policy
     *
     * @param policyId               Policy UUID
     * @param apiUUID                UUID of the API
     * @param organization           Organization name
     * @param isWithPolicyDefinition Include the policy definition to the output or not
     * @return operation policy
     * @throws APIManagementException
     */
    public OperationPolicyData getAPISpecificOperationPolicyByPolicyID(String policyId, String apiUUID,
                                                                       String organization,
                                                                       boolean isWithPolicyDefinition)
            throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            return getAPISpecificOperationPolicyByPolicyID(connection, policyId, apiUUID, organization,
                    isWithPolicyDefinition);
        } catch (SQLException e) {
            handleExceptionWithCode("Failed to get the API specific operation policy for id " + policyId + " from API "
                    + apiUUID, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        return null;
    }

    private void handleExceptionWithCode(String msg, Throwable t, ErrorHandler code) throws APIManagementException {
        log.error(msg, t);
        throw new APIManagementException(msg, code);
    }

    /**
     * Clone an operation policy to the API. This method is used in two flows.
     * Cloning a common policy to API.
     * Cloning a dependent policy of a product
     * Each of these scenarios, original APIs' policy ID will be recorded as the cloned policy ID.
     *
     * @param connection   DB connection
     * @param policyId     Original policy's ID that needs to be cloned
     * @param apiUUID      UUID of the API
     * @param revisionUUID UUID of the revision
     * @return cloned policyID
     * @throws APIManagementException
     * @throws SQLException
     **/
    private String cloneOperationPolicy(Connection connection, String policyId, String apiUUID, String revisionUUID)
            throws APIManagementException, SQLException {

        OperationPolicyData policyData = getOperationPolicyByPolicyID(connection, policyId, true);
        if (policyData != null) {
            // If we are taking a clone from common policy, common policy's Id is used as the CLONED_POLICY_ID.
            // If we are cloning for an API Product, dependent APIs' id is used.
            return addAPISpecificOperationPolicy(connection, apiUUID, revisionUUID, policyData, policyId);
        } else {
            throw new APIManagementException("Cannot clone policy with ID " + policyId + " as it does not exists.",
                    ExceptionCodes.from(ExceptionCodes.OPERATION_POLICY_NOT_FOUND, policyId));
        }
    }


    /**
     * Retrieve an operation policy by providing the policy uuid
     *
     * @param connection             DB connection
     * @param policyId               Policy UUID
     * @param isWithPolicyDefinition Include the policy definition to the output or not
     * @return operation policy
     * @throws SQLException
     */
    private OperationPolicyData getOperationPolicyByPolicyID(Connection connection, String policyId,
                                                             boolean isWithPolicyDefinition) throws SQLException {

        String dbQuery = PostgreSQLConstants.OperationPolicyConstants.GET_OPERATION_POLICY_FROM_POLICY_ID;

        PreparedStatement statement = connection.prepareStatement(dbQuery);
        statement.setString(1, policyId);
        ResultSet rs = statement.executeQuery();
        OperationPolicyData policyData = null;
        if (rs.next()) {
            policyData = new OperationPolicyData();
            policyData.setPolicyId(policyId);
            policyData.setOrganization(rs.getString("ORGANIZATION"));
            policyData.setMd5Hash(rs.getString("POLICY_MD5"));
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

    public String getAPIContext(String uuid) throws APIManagementException {

        String context = null;
        try (Connection connection = APIMgtDBUtil.getConnection()) {
            context = getAPIContext(uuid, connection);
        } catch (SQLException e) {
            log.error("Failed to retrieve the API Context", e);
            handleException("Failed to retrieve connection while getting the API Context for API with UUID " + uuid, e);
        }
        return context;
    }

    /**
     * Get API Context by passing an existing DB connection.
     *
     * @param uuid API uuid
     * @param connection DB Connection
     * @return API Context
     * @throws APIManagementException if an error occurs
     */
    public String getAPIContext(String uuid, Connection connection) throws APIManagementException {

        String context = null;
        String sql = PostgreSQLConstants.GET_API_CONTEXT_BY_API_UUID_SQL;
        try (PreparedStatement prepStmt = connection.prepareStatement(sql)) {
            prepStmt.setString(1, uuid);
            try (ResultSet resultSet = prepStmt.executeQuery()) {
                while (resultSet.next()) {
                    context = resultSet.getString(1);
                }
            }
        } catch (SQLException e) {
            log.error("Failed to retrieve the API Context", e);
            handleException("Failed to retrieve the API Context for API with UUID " + uuid, e);
        }
        return context;
    }

    public String getDefaultVersion(APIIdentifier apiId) throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            return getDefaultVersion(connection, apiId);
        } catch (SQLException e) {
            handleExceptionWithCode("Error while getting default version for " + apiId.getApiName(), e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        return null;
    }

    private String getDefaultVersion(Connection connection, APIIdentifier apiId) throws SQLException {

        String oldDefaultVersion = null;

        String query = PostgreSQLConstants.GET_DEFAULT_VERSION_SQL;
        try (PreparedStatement prepStmt = connection.prepareStatement(query)) {
            prepStmt.setString(1, apiId.getApiName());
            prepStmt.setString(2, APIUtil.replaceEmailDomainBack(apiId.getProviderName()));
            try (ResultSet rs = prepStmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("DEFAULT_API_VERSION");
                }
            }
        }
        return null;
    }

    @Override
    public API getLightWeightAPIInfoByAPIIdentifier(String organization, APIIdentifier apiIdentifier)
            throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            try (PreparedStatement preparedStatement =
                         connection.prepareStatement(PostgreSQLConstants.GET_LIGHT_WEIGHT_API_INFO_BY_API_IDENTIFIER)) {
                preparedStatement.setString(1, APIUtil.replaceEmailDomainBack(apiIdentifier.getProviderName()));
                preparedStatement.setString(2, apiIdentifier.getName());
                preparedStatement.setString(3, apiIdentifier.getVersion());
                preparedStatement.setString(4, organization);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        apiIdentifier.setId(resultSet.getInt("API_ID"));
                        API api = new API(apiIdentifier);
                        api.setUuid(resultSet.getString("API_UUID"));
                        api.setContext(resultSet.getString("CONTEXT"));
                        api.setType(resultSet.getString("API_TYPE"));
                        api.setStatus(resultSet.getString("STATUS"));
                        return api;
                    }
                }

            }
        } catch (SQLException e) {
            throw new APIManagementException("Error while retrieving apimgt connection", e,
                    ExceptionCodes.INTERNAL_ERROR);
        }
        return null;
    }

    @Override
    public void updateAPI(API api, String username) throws APIManagementException {

        Connection connection = null;
        PreparedStatement prepStmt = null;

        String previousDefaultVersion = getDefaultVersion(api.getId());

        String query = PostgreSQLConstants.UPDATE_API_SQL;
        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            //Header change check not required here as we update API level throttling tier
            //from same call.
            //TODO review and run tier update as separate query if need.
            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, api.getContext());
            String contextTemplate = api.getContextTemplate();
            //If the context template ends with {version} this means that the version will be at the end of the
            // context.
            if (contextTemplate.endsWith("/" + APIConstants.VERSION_PLACEHOLDER)) {
                //Remove the {version} part from the context template.
                contextTemplate = contextTemplate.split(Pattern.quote("/" + APIConstants.VERSION_PLACEHOLDER))[0];
            }

            // For Choreo-Connect gateway, gateway vendor type in the DB will be "wso2/choreo-connect".
            // This value is determined considering the gateway type comes with the request.
            api.setGatewayVendor(APIUtil.setGatewayVendorBeforeInsertion(
                    api.getGatewayVendor(), api.getGatewayType()));

            prepStmt.setString(2, api.getId().getApiName());
            prepStmt.setString(3, contextTemplate);
            prepStmt.setString(4, username);
            prepStmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            prepStmt.setString(6, api.getApiLevelPolicy());
            prepStmt.setString(7, api.getType());
            prepStmt.setString(8, api.getGatewayVendor());
            prepStmt.setString(9, api.getUuid());
            prepStmt.execute();

            if (api.isDefaultVersion() ^ api.getId().getVersion().equals(previousDefaultVersion)) { //A change has
                // happen
                //If the api is selected as default version, it is added/replaced into AM_API_DEFAULT_VERSION table
                if (api.isDefaultVersion()) {
                    addUpdateAPIAsDefaultVersion(api, connection);
                } else { //tick is removed
                    ArrayList<APIIdentifier> apiIdList = new ArrayList<APIIdentifier>() {{
                        add(api.getId());
                    }};

                    removeAPIFromDefaultVersion(apiIdList, connection);
                }
            }
            String serviceKey = api.getServiceInfo("key");
            if (StringUtils.isNotEmpty(serviceKey)) {
                int apiId = getAPIID(api.getUuid());
                int tenantID = APIUtil.getTenantId(username);
                updateAPIServiceMapping(apiId, serviceKey, api.getServiceInfo("md5"), tenantID, connection);
            }
            connection.commit();
        } catch (SQLException e) {
            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException ex) {
                // Rollback failed. Exception will be thrown later for upper exception
                log.error("Failed to rollback the update API: " + api.getId(), ex);
            }
            handleExceptionWithCode("Error while updating the API: " + api.getId() + " in the database", e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, null);
        }
    }

    /**
     * Update API Service Mapping entry in AM_API_SERVICE_MAPPING
     *
     * @param apiId      Unique Identifier of API
     * @param serviceKey Unique key of the Service
     * @param md5        MD5 value of the Service
     * @param tenantID   tenantID of API
     * @throws SQLException
     */
    private void updateAPIServiceMapping(int apiId, String serviceKey, String md5, int tenantID, Connection connection)
            throws APIManagementException {
        try {
            if (!retrieveServiceKeyByApiId(apiId, connection).isEmpty()) {
                try (PreparedStatement statement = connection.prepareStatement(PostgreSQLConstants.UPDATE_API_SERVICE_MAPPING_SQL)) {
                    statement.setString(1, serviceKey);
                    statement.setString(2, md5);
                    statement.setInt(3, apiId);
                    statement.executeUpdate();
                }
            } else {
                addAPIServiceMapping(apiId, serviceKey, md5, tenantID, connection);
            }
        } catch (SQLException e) {
            handleExceptionWithCode("Error while updating the Service info associated with API " + apiId, e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
    }

    /**
     * Retrieve the Unique Identifier of the Service used in API
     *
     * @param apiId    Unique Identifier of API
     * @return Service Key
     * @throws APIManagementException
     */
    private String retrieveServiceKeyByApiId(int apiId, Connection connection) throws APIManagementException {

        String retrieveServiceKeySQL = PostgreSQLConstants.GET_SERVICE_KEY_BY_API_ID_SQL_WITHOUT_TENANT_ID;
        String serviceKey = StringUtils.EMPTY;
        try (PreparedStatement preparedStatement = connection.prepareStatement(retrieveServiceKeySQL)) {
            preparedStatement.setInt(1, apiId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    serviceKey = resultSet.getString(APIConstants.ServiceCatalogConstants.SERVICE_KEY);
                }
            }
        } catch (SQLException e) {
            handleExceptionWithCode("Error while retrieving the Service Key associated with API " + apiId, e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        return serviceKey;
    }

    @Override
    public int getRevisionCountByAPI(String apiUUID) throws APIManagementException {

        int count = 0;
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection
                     .prepareStatement(PostgreSQLConstants.APIRevisionSqlConstants.GET_REVISION_COUNT_BY_API_UUID)) {
            statement.setString(1, apiUUID);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    count = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            handleException("Failed to get revisions count for API UUID: " + apiUUID, e);
        }
        return count;
    }

    @Override
    public int getMostRecentRevisionId(String apiUUID) throws APIManagementException {

        int revisionId = 0;
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection
                     .prepareStatement(PostgreSQLConstants.APIRevisionSqlConstants.GET_MOST_RECENT_REVISION_ID)) {
            statement.setString(1, apiUUID);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    revisionId = rs.getInt("REVISIONS_CREATED");
                }
            }
        } catch (SQLException e) {
            handleException("Failed to get most recent revision ID for API UUID: " + apiUUID, e);
        }
        return revisionId;
    }

    @Override
    public APIRevision getRevisionByRevisionUUID(String revisionUUID) throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            return getRevisionByRevisionUUID(connection, revisionUUID);
        } catch (SQLException e) {
            handleExceptionWithCode("Failed to get revision details for revision UUID: " + revisionUUID, e,
                    ExceptionCodes.from(ExceptionCodes.ERROR_RETRIEVING_REVISION_FOR_UUID, revisionUUID));

        }
        return null;
    }

    private APIRevision getRevisionByRevisionUUID(Connection connection, String revisionUUID) throws SQLException {

        try (PreparedStatement statement = connection
                .prepareStatement(PostgreSQLConstants.APIRevisionSqlConstants.GET_REVISION_BY_REVISION_UUID)) {
            statement.setString(1, revisionUUID);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    APIRevision apiRevision = new APIRevision();
                    apiRevision.setId(rs.getInt("ID"));
                    apiRevision.setApiUUID(rs.getString("API_UUID"));
                    apiRevision.setRevisionUUID(rs.getString("REVISION_UUID"));
                    apiRevision.setDescription(rs.getString("DESCRIPTION"));
                    apiRevision.setCreatedTime(rs.getString("CREATED_TIME"));
                    apiRevision.setCreatedBy(rs.getString("CREATED_BY"));
                    return apiRevision;
                }
            }
        }
        return null;
    }

    @Override
    public String getRevisionUUID(String revisionNum, String apiUUID) throws APIManagementException {

        String revisionUUID = null;
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection
                     .prepareStatement(PostgreSQLConstants.APIRevisionSqlConstants.GET_REVISION_UUID)) {
            statement.setString(1, apiUUID);
            statement.setInt(2, Integer.parseInt(revisionNum));
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    revisionUUID = rs.getString(1);
                }
            }
        } catch (SQLException e) {
            handleExceptionWithCode("Failed to get revision UUID for Revision " + revisionNum, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        return revisionUUID;
    }

    @Override
    public String getRevisionUUIDByOrganization(String revisionNum, String apiUUID, String organization) throws APIManagementException {

        String revisionUUID = null;
        String sql = PostgreSQLConstants.APIRevisionSqlConstants.GET_REVISION_UUID_BY_ORGANIZATION;
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection
                     .prepareStatement(sql)) {
            statement.setString(1, apiUUID);
            statement.setInt(2, Integer.parseInt(revisionNum));
            statement.setString(3, organization);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    revisionUUID = rs.getString(1);
                }
            }
        } catch (SQLException e) {
            handleException("Failed to get revision UUID for Revision " + revisionNum, e);
        }
        return revisionUUID;
    }

    @Override
    public String getEarliestRevision(String apiUUID) throws APIManagementException {
        String revisionUUID = null;
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = (
                     connection.getMetaData().getDriverName().contains("MS SQL") || connection.getMetaData()
                             .getDriverName().contains("Microsoft") ?
                             connection.prepareStatement(
                                     SQLConstants.APIRevisionSqlConstants.GET_EARLIEST_REVISION_ID_MSSQL) :
                             (connection.getMetaData().getDriverName().contains("MySQL") || connection.getMetaData()
                                     .getDriverName().contains("H2")) ?
                                     connection.prepareStatement(
                                             SQLConstants.APIRevisionSqlConstants.GET_EARLIEST_REVISION_ID_MYSQL) :
                                     connection.prepareStatement(
                                             SQLConstants.APIRevisionSqlConstants.GET_EARLIEST_REVISION_ID))) {
            statement.setString(1, apiUUID);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    revisionUUID = rs.getString(1);
                }
            }
        } catch (SQLException e) {
            handleException("Failed to get the earliest revision for api ID: " + apiUUID, e);
        }
        return revisionUUID;
    }

    @Override
    public String getLatestRevisionUUID(String apiUUID) throws APIManagementException {

        String revisionUUID = null;
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = (connection.getMetaData().getDriverName().contains("MS SQL") ||
                     connection.getMetaData().getDriverName().contains("Microsoft") ?
                     connection.prepareStatement(SQLConstants.APIRevisionSqlConstants.GET_MOST_RECENT_REVISION_UUID_MSSQL) :
                     (connection.getMetaData().getDriverName().contains("MySQL") || connection.getMetaData().getDriverName()
                             .contains("H2")) ?
                             connection.prepareStatement(SQLConstants.APIRevisionSqlConstants.GET_MOST_RECENT_REVISION_UUID_MYSQL) :
                             connection.prepareStatement(SQLConstants.APIRevisionSqlConstants.GET_MOST_RECENT_REVISION_UUID))) {
            statement.setString(1, apiUUID);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    revisionUUID = rs.getString(1);
                }
            }
        } catch (SQLException e) {
            handleException("Failed to get the latest revision for api ID: " + apiUUID, e);
        }
        return revisionUUID;
    }

    @Override
    public List<APIRevision> getRevisionsListByAPIUUID(String apiUUID) throws APIManagementException {

        List<APIRevision> revisionList = new ArrayList<>();
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection
                     .prepareStatement(SQLConstants.APIRevisionSqlConstants.GET_REVISIONS_BY_API_UUID)) {
            statement.setString(1, apiUUID);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    APIRevision apiRevision = new APIRevision();
                    apiRevision.setId(rs.getInt("ID"));
                    apiRevision.setApiUUID(apiUUID);
                    apiRevision.setRevisionUUID(rs.getString("REVISION_UUID"));
                    apiRevision.setDescription(rs.getString("DESCRIPTION"));
                    apiRevision.setCreatedTime(rs.getString("CREATED_TIME"));
                    apiRevision.setCreatedBy(rs.getString("CREATED_BY"));
                    apiRevision.setApiRevisionDeploymentList(new ArrayList<>());
                    revisionList.add(apiRevision);
                }
            }
        } catch (SQLException e) {
            handleExceptionWithCode("Failed to get revision details for API UUID: " + apiUUID, e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }

        // adding deployment info to revision objects
        List<APIRevisionDeployment> allAPIRevisionDeploymentList = getAPIRevisionDeploymentByApiUUID(apiUUID);

        for(APIRevisionDeployment apiRevisionDeployment : allAPIRevisionDeploymentList) {
            for (APIRevision apiRevision : revisionList) {
                if (apiRevision.getRevisionUUID().equals(apiRevisionDeployment.getRevisionUUID())) {
                    apiRevision.getApiRevisionDeploymentList().add(apiRevisionDeployment);
                    break;
                }
            }
        }

        return revisionList;
    }

    @Override
    public List<APIRevisionDeployment> getAPIRevisionDeploymentByApiUUID(String apiUUID) throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            PreparedStatement statement;
            if (connection.getMetaData().getDriverName().contains("PostgreSQL")) {
                statement = connection
                        .prepareStatement(SQLConstants.
                                APIRevisionSqlConstants.GET_API_REVISION_DEPLOYMENTS_BY_API_UUID_POSTGRES);
            } else {
                statement = connection
                        .prepareStatement(SQLConstants.
                                APIRevisionSqlConstants.GET_API_REVISION_DEPLOYMENTS_BY_API_UUID);
            }
            statement.setString(1, apiUUID);
            try (ResultSet rs = statement.executeQuery()) {
                return APIMgtDBUtil.mergeRevisionDeploymentDTOs(rs);
            }
        } catch (SQLException e) {
            handleExceptionWithCode("Failed to get API Revision deployment mapping details for api uuid: " +
                    apiUUID, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        return new ArrayList<>();
    }

    @Override
    public List<APIRevisionDeployment> getAPIRevisionDeploymentsByApiUUID(String apiUUID) throws APIManagementException {

        List<APIRevisionDeployment> apiRevisionDeploymentList = new ArrayList<>();
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection
                     .prepareStatement(SQLConstants.
                             APIRevisionSqlConstants.GET_API_REVISION_DEPLOYMENT_MAPPING_BY_API_UUID)) {
            statement.setString(1, apiUUID);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    APIRevisionDeployment apiRevisionDeployment = new APIRevisionDeployment();
                    String environmentName = rs.getString("NAME");
                    String vhost = rs.getString("VHOST");
                    apiRevisionDeployment.setDeployment(environmentName);
                    apiRevisionDeployment.setVhost(VHostUtils.resolveIfNullToDefaultVhost(environmentName, vhost));
                    apiRevisionDeployment.setRevisionUUID(rs.getString("REVISION_UUID"));
                    apiRevisionDeployment.setDisplayOnDevportal(rs.getBoolean("DISPLAY_ON_DEVPORTAL"));
                    apiRevisionDeployment.setDeployedTime(rs.getString("DEPLOYED_TIME"));
                    apiRevisionDeploymentList.add(apiRevisionDeployment);
                }
            }
        } catch (SQLException e) {
            handleExceptionWithCode("Failed to get API Revision deployment mapping details for api uuid: " +
                    apiUUID, e, ExceptionCodes.from(ExceptionCodes.ERROR_RETRIEVING_REVISION_DEPLOYMENT_MAPPING,
                    "API UUID", apiUUID));
        }
        return apiRevisionDeploymentList;
    }

    @Override
    public void removeAPIRevisionDeployment(String apiUUID, Set<APIRevisionDeployment> deployments)
            throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            try {
                connection.setAutoCommit(false);
                // Remove an entry from AM_DEPLOYMENT_REVISION_MAPPING table
                PreparedStatement statement = connection
                        .prepareStatement(SQLConstants.APIRevisionSqlConstants.REMOVE_API_REVISION_DEPLOYMENT_MAPPING);
                for (APIRevisionDeployment deployment : deployments) {
                    statement.setString(1, deployment.getDeployment());
                    statement.setString(2, deployment.getRevisionUUID());
                    statement.addBatch();
                }
                statement.executeBatch();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            handleExceptionWithCode("Failed to remove API Revision Deployment Mapping entry for API UUID "
                    + apiUUID, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
    }

    @Override
    public void addAPIRevisionDeployment(String apiRevisionId, List<APIRevisionDeployment> apiRevisionDeployments)
            throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            try {
                connection.setAutoCommit(false);
                // Adding to AM_DEPLOYMENT_REVISION_MAPPING table
                PreparedStatement statement = connection
                        .prepareStatement(PostgreSQLConstants.APIRevisionSqlConstants.ADD_API_REVISION_DEPLOYMENT_MAPPING);
                for (APIRevisionDeployment apiRevisionDeployment : apiRevisionDeployments) {
                    String envName = apiRevisionDeployment.getDeployment();
                    String vhost = apiRevisionDeployment.getVhost();
                    // set VHost as null, if it is the default vhost of the read only environment
                    statement.setString(1, apiRevisionDeployment.getDeployment());
                    statement.setString(2, VHostUtils.resolveIfDefaultVhostToNull(envName, vhost));
                    statement.setString(3, apiRevisionId);
                    statement.setBoolean(4, apiRevisionDeployment.isDisplayOnDevportal());
                    statement.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
                    statement.addBatch();
                }
                statement.executeBatch();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                handleExceptionWithCode("Failed to add API Revision Deployment Mapping entry for Revision UUID "
                        + apiRevisionId, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
            }
        } catch (SQLException e) {
            handleExceptionWithCode("Failed to add API Revision Deployment Mapping entry for Revision UUID " +
                    apiRevisionId, e, ExceptionCodes.API_IMPORT_ERROR);
        }
    }

    @Override
    public void updateDefaultAPIPublishedVersion(APIIdentifier identifier)
            throws APIManagementException {

        try (Connection conn = APIMgtDBUtil.getConnection()) {
            try {
                conn.setAutoCommit(false);
                String defaultVersion = getDefaultVersion(conn, identifier);
                if (identifier.getVersion().equals(defaultVersion)) {
                    setPublishedDefVersion(identifier, conn, identifier.getVersion());
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }

        } catch (SQLException e) {
            handleExceptionWithCode("Failed to update published default API state change", e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
    }

    private void setPublishedDefVersion(APIIdentifier apiId, Connection connection, String value)
            throws APIManagementException {

        String queryDefaultVersionUpdate = PostgreSQLConstants.UPDATE_PUBLISHED_DEFAULT_VERSION_SQL;

        PreparedStatement prepStmtDefVersionUpdate = null;
        try {
            prepStmtDefVersionUpdate = connection.prepareStatement(queryDefaultVersionUpdate);
            prepStmtDefVersionUpdate.setString(1, value);
            prepStmtDefVersionUpdate.setString(2, apiId.getApiName());
            prepStmtDefVersionUpdate.setString(3, APIUtil.replaceEmailDomainBack(apiId.getProviderName()));
            prepStmtDefVersionUpdate.execute();
        } catch (SQLException e) {
            handleException("Error while deleting the API default version entry: " + apiId.getApiName() + " from the " +
                    "database", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmtDefVersionUpdate, null, null);
        }
    }

    @Override
    public List<DeployedAPIRevision> getDeployedAPIRevisionByApiUUID(String apiUUID) throws APIManagementException {

        List<DeployedAPIRevision> deployedAPIRevisionList = new ArrayList<>();
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection
                     .prepareStatement(PostgreSQLConstants.
                             APIRevisionSqlConstants.GET_DEPLOYED_REVISION_BY_API_UUID)) {
            statement.setString(1, apiUUID);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    DeployedAPIRevision deployedAPIRevision = new DeployedAPIRevision();
                    String environmentName = rs.getString("NAME");
                    String vhost = rs.getString("VHOST");
                    deployedAPIRevision.setDeployment(environmentName);
                    deployedAPIRevision.setVhost(VHostUtils.resolveIfNullToDefaultVhost(environmentName, vhost));
                    deployedAPIRevision.setRevisionUUID(rs.getString("REVISION_UUID"));
                    deployedAPIRevision.setDeployedTime(rs.getString("DEPLOYED_TIME"));
                    deployedAPIRevisionList.add(deployedAPIRevision);
                }
            }
        } catch (SQLException e) {
            handleException("Failed to get deployed API Revision details for api uuid: " +
                    apiUUID, e);
        }
        return deployedAPIRevisionList;
    }

    @Override
    public void removeDeployedAPIRevision(String apiUUID, Set<DeployedAPIRevision> deployments)
            throws APIManagementException {
        if (deployments.size() > 0) {
            try (Connection connection = APIMgtDBUtil.getConnection()) {
                connection.setAutoCommit(false);
                // Remove an entry from AM_DEPLOYED_REVISION table
                try (PreparedStatement statement = connection
                        .prepareStatement(PostgreSQLConstants.APIRevisionSqlConstants.REMOVE_DEPLOYED_API_REVISION)) {
                    for (DeployedAPIRevision deployment : deployments) {
                        statement.setString(1, deployment.getDeployment());
                        statement.setString(2, deployment.getRevisionUUID());
                        statement.addBatch();
                    }
                    statement.executeBatch();
                    connection.commit();
                } catch (SQLException e) {
                    connection.rollback();
                    handleException("Failed to remove deployed API Revision entry for API UUID "
                            + apiUUID, e);
                }
            } catch (SQLException e) {
                handleException("Failed to remove deployed API Revision entry for API UUID "
                        + apiUUID, e);
            }
        }
    }

    @Override
    public void addDeployedAPIRevision(String apiRevisionId, List<DeployedAPIRevision> deployedAPIRevisionList)
            throws APIManagementException {
        if (deployedAPIRevisionList.size() > 0) {
            try (Connection connection = APIMgtDBUtil.getConnection()) {
                connection.setAutoCommit(false);
                // Adding to AM_DEPLOYED_REVISION table
                try (PreparedStatement statement = connection
                        .prepareStatement(PostgreSQLConstants.APIRevisionSqlConstants.ADD_DEPLOYED_API_REVISION)) {
                    for (DeployedAPIRevision deployedAPIRevision : deployedAPIRevisionList) {
                        String envName = deployedAPIRevision.getDeployment();
                        String vhost = deployedAPIRevision.getVhost();
                        // set VHost as null, if it is the default vhost of the read only environment
                        statement.setString(1, deployedAPIRevision.getDeployment());
                        statement.setString(2, VHostUtils.resolveIfDefaultVhostToNull(envName, vhost));
                        statement.setString(3, apiRevisionId);
                        statement.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
                        statement.addBatch();
                    }
                    statement.executeBatch();
                    connection.commit();
                } catch (SQLException e) {
                    connection.rollback();
                    // handle concurrent db entry update. Fix duplicate primary key issue.
                    if (e.getMessage().toLowerCase().contains("primary key violation") ||
                            e.getMessage().toLowerCase().contains("duplicate entry") ||
                            e.getMessage().contains("Violation of PRIMARY KEY constraint")) {
                        log.warn("Duplicate entries detected for Revision UUID " + apiRevisionId +
                                " while adding deployed API revisions", e);
                        throw new APIManagementException("Failed to add deployed API Revision for Revision UUID "
                                + apiRevisionId,  e, ExceptionCodes.REVISION_ALREADY_DEPLOYED);
                    } else {
                        handleException("Failed to add deployed API Revision for Revision UUID "
                                + apiRevisionId, e);
                    }
                }
            } catch (SQLException e) {
                handleException("Failed to add deployed API Revision for Revision UUID " + apiRevisionId,
                        e);
            }
        }
    }

    @Override
    public void updateAPIRevisionDeployment(String apiUUID, Set<APIRevisionDeployment> deployments)
            throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            connection.setAutoCommit(false);
            // Update an entry from AM_DEPLOYMENT_REVISION_MAPPING table
            try (PreparedStatement statement = connection
                    .prepareStatement(PostgreSQLConstants.APIRevisionSqlConstants.UPDATE_API_REVISION_DEPLOYMENT_MAPPING)) {
                for (APIRevisionDeployment deployment : deployments) {
                    statement.setBoolean(1, deployment.isDisplayOnDevportal());
                    statement.setString(2, deployment.getDeployment());
                    statement.setString(3, deployment.getRevisionUUID());
                    statement.addBatch();
                }
                statement.executeBatch();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            handleExceptionWithCode("Failed to update Deployment Mapping entry for API UUID "
                    + apiUUID, e, ExceptionCodes.from(ExceptionCodes.ERROR_UPDATING_REVISION_DEPLOYMENT_MAPPING, apiUUID));
        }
    }

    @Override
    public APIRevisionDeployment getAPIRevisionDeploymentByNameAndRevsionID(String name, String revisionId) throws APIManagementException {

        APIRevisionDeployment apiRevisionDeployment = new APIRevisionDeployment();
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection
                     .prepareStatement(PostgreSQLConstants.
                             APIRevisionSqlConstants.GET_API_REVISION_DEPLOYMENT_MAPPING_BY_NAME_AND_REVISION_UUID)) {
            statement.setString(1, name);
            statement.setString(2, revisionId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    String environmentName = rs.getString("NAME");
                    String vhost = rs.getString("VHOST");
                    apiRevisionDeployment.setDeployment(environmentName);
                    apiRevisionDeployment.setVhost(VHostUtils.resolveIfNullToDefaultVhost(environmentName, vhost));
                    apiRevisionDeployment.setRevisionUUID(rs.getString("REVISION_UUID"));
                    apiRevisionDeployment.setDisplayOnDevportal(rs.getBoolean("DISPLAY_ON_DEVPORTAL"));
                    apiRevisionDeployment.setDeployedTime(rs.getString("DEPLOYED_TIME"));
                }
            }
        } catch (SQLException e) {
            handleExceptionWithCode("Failed to get API Revision deployment mapping details for deployment name: " +
                    name, e, ExceptionCodes.from(ExceptionCodes.ERROR_RETRIEVING_REVISION_DEPLOYMENT_MAPPING,
                    "deployment name", name));
        }
        return apiRevisionDeployment;
    }

    public List<APIRevisionDeployment> getAPIRevisionDeploymentByRevisionUUID(String revisionUUID) throws APIManagementException {

        List<APIRevisionDeployment> apiRevisionDeploymentList = new ArrayList<>();
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection
                     .prepareStatement(PostgreSQLConstants.
                             APIRevisionSqlConstants.GET_API_REVISION_DEPLOYMENT_MAPPING_BY_REVISION_UUID)) {
            statement.setString(1, revisionUUID);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    APIRevisionDeployment apiRevisionDeployment = new APIRevisionDeployment();
                    String environmentName = rs.getString("NAME");
                    String vhost = rs.getString("VHOST");
                    apiRevisionDeployment.setDeployment(environmentName);
                    apiRevisionDeployment.setVhost(VHostUtils.resolveIfNullToDefaultVhost(environmentName, vhost));
                    apiRevisionDeployment.setRevisionUUID(rs.getString("REVISION_UUID"));
                    apiRevisionDeployment.setDisplayOnDevportal(rs.getBoolean("DISPLAY_ON_DEVPORTAL"));
                    apiRevisionDeployment.setDeployedTime(rs.getString("DEPLOYED_TIME"));
                    apiRevisionDeploymentList.add(apiRevisionDeployment);
                }
            }
        } catch (SQLException e) {
            handleExceptionWithCode("Failed to get API Revision deployment mapping details for revision uuid: " +
                    revisionUUID, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        return apiRevisionDeploymentList;
    }

    @Override
    public void removeAPIRevisionDeployment(String apiRevisionId, List<APIRevisionDeployment> apiRevisionDeployments)
            throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            try {
                connection.setAutoCommit(false);
                // Remove an entry from AM_DEPLOYMENT_REVISION_MAPPING table
                try (PreparedStatement statement = connection
                        .prepareStatement(PostgreSQLConstants.APIRevisionSqlConstants.REMOVE_API_REVISION_DEPLOYMENT_MAPPING)) {
                    for (APIRevisionDeployment apiRevisionDeployment : apiRevisionDeployments) {
                        statement.setString(1, apiRevisionDeployment.getDeployment());
                        statement.setString(2, apiRevisionId);
                        statement.addBatch();
                    }
                    statement.executeBatch();
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                handleExceptionWithCode("Failed to remove API Revision Deployment Mapping entry for Revision UUID "
                        + apiRevisionId, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
            }
        } catch (SQLException e) {
            handleExceptionWithCode("Failed to remove API Revision Deployment Mapping entry for Revision UUID "
                    + apiRevisionId, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
    }

    @Override
    public APIInfo getAPIInfoByUUID(String apiId) throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            APIRevision apiRevision = getRevisionByRevisionUUID(connection, apiId);
            String sql = SQLConstants.RETRIEVE_API_INFO_FROM_UUID;
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                if (apiRevision != null) {
                    preparedStatement.setString(1, apiRevision.getApiUUID());
                } else {
                    preparedStatement.setString(1, apiId);
                }
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        APIInfo.Builder apiInfoBuilder = new APIInfo.Builder();
                        apiInfoBuilder = apiInfoBuilder.id(resultSet.getString("API_UUID"))
                                .name(resultSet.getString("API_NAME"))
                                .version(resultSet.getString("API_VERSION"))
                                .provider(resultSet.getString("API_PROVIDER"))
                                .context(resultSet.getString("CONTEXT"))
                                .contextTemplate(resultSet.getString("CONTEXT_TEMPLATE"))
                                .status(APIUtil.getApiStatus(resultSet.getString("STATUS")))
                                .apiType(resultSet.getString("API_TYPE"))
                                .createdBy(resultSet.getString("CREATED_BY"))
                                .createdTime(resultSet.getString("CREATED_TIME"))
                                .updatedBy(resultSet.getString("UPDATED_BY"))
                                .updatedTime(resultSet.getString("UPDATED_TIME"))
                                .revisionsCreated(resultSet.getInt("REVISIONS_CREATED"))
                                .organization(resultSet.getString("ORGANIZATION"))
                                .isRevision(apiRevision != null).organization(resultSet.getString("ORGANIZATION"));
                        if (apiRevision != null) {
                            apiInfoBuilder = apiInfoBuilder.apiTier(getAPILevelTier(connection,
                                    apiRevision.getApiUUID(), apiId));
                        } else {
                            apiInfoBuilder = apiInfoBuilder.apiTier(resultSet.getString("API_TIER"));
                        }
                        return apiInfoBuilder.build();
                    }
                }
            }
        } catch (SQLException e) {
            throw new APIManagementException("Error while retrieving apimgt connection", e,
                    ExceptionCodes.INTERNAL_ERROR);
        }
        return null;
    }

    private String getAPILevelTier(Connection connection, String apiUUID, String revisionUUID) throws SQLException {

        try (PreparedStatement preparedStatement =
                     connection.prepareStatement(SQLConstants.GET_REVISIONED_API_TIER_SQL)) {
            preparedStatement.setString(1, apiUUID);
            preparedStatement.setString(2, revisionUUID);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("API_TIER");
                }
            }
        }
        return null;
    }

    @Override
    public Set<URITemplate> getURITemplatesWithOperationPolicies(String apiUUID) throws APIManagementException {

        String query;
        APIRevision apiRevision = checkAPIUUIDIsARevisionUUID(apiUUID);

        if (apiRevision == null) {
            query = SQLConstants.OperationPolicyConstants.GET_OPERATION_POLICIES_OF_API_SQL;
        } else {
            query = SQLConstants.OperationPolicyConstants.GET_OPERATION_POLICIES_FOR_API_REVISION_SQL;
        }

        Map<String, URITemplate> uriTemplates = new HashMap<>();
        Set<URITemplate> uriTemplateList = new HashSet<>();
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            if (apiRevision == null) {
                int apiId = getAPIID(apiUUID, connection);
                prepStmt.setInt(1, apiId);
            } else {
                int apiId = getAPIID(apiRevision.getApiUUID(), connection);
                prepStmt.setInt(1, apiId);
                prepStmt.setString(2, apiRevision.getRevisionUUID());
            }
            try (ResultSet rs = prepStmt.executeQuery()) {
                URITemplate uriTemplate;
                while (rs.next()) {
                    String httpMethod = rs.getString("HTTP_METHOD");
                    String urlPattern = rs.getString("URL_PATTERN");
                    String urlTemplateKey = httpMethod + ":" + urlPattern;
                    if (!uriTemplates.containsKey(urlTemplateKey)) {
                        uriTemplate = new URITemplate();
                    } else {
                        uriTemplate = uriTemplates.get(urlTemplateKey);
                    }
                    OperationPolicy operationPolicy = populateOperationPolicyWithRS(rs);
                    uriTemplate.addOperationPolicy(operationPolicy);
                    uriTemplate.setHTTPVerb(httpMethod);
                    uriTemplate.setUriTemplate(urlPattern);
                    uriTemplate.setId(rs.getInt("URL_MAPPING_ID"));
                    uriTemplates.put(urlTemplateKey, uriTemplate);
                }
            }
            uriTemplateList.addAll(uriTemplates.values());
        } catch (SQLException e) {
            handleExceptionWithCode("Error while fetching URI templates with operation policies for " + apiUUID, e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        return uriTemplateList;
    }

    @Override
    public String addAPISpecificOperationPolicy(String apiUUID, String revisionUUID,
                                                OperationPolicyData policyData)
            throws APIManagementException {

        OperationPolicySpecification policySpecification = policyData.getSpecification();
        try (Connection connection = APIMgtDBUtil.getConnection()) {
            try {
                connection.setAutoCommit(false);
                String policyID = addAPISpecificOperationPolicy(connection, apiUUID, revisionUUID, policyData, null);
                connection.commit();
                return policyID;
            } catch (SQLException e) {
                connection.rollback();
                handleExceptionWithCode("Failed to add API specific operation policy " + policySpecification.getName()
                        + " for API " + apiUUID, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
            }
        } catch (SQLException e) {
            handleExceptionWithCode("Failed to add API specific operation policy " + policySpecification.getName()
                    + " for API " + apiUUID, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        return null;
    }

    @Override
    public String addCommonOperationPolicy(OperationPolicyData policyData) throws APIManagementException {

        String policyUUID = null;
        OperationPolicySpecification policySpecification = policyData.getSpecification();
        try (Connection connection = APIMgtDBUtil.getConnection()) {
            try {
                connection.setAutoCommit(false);
                policyUUID = addOperationPolicyContent(connection, policyData);

                String dbQuery = SQLConstants.OperationPolicyConstants.ADD_COMMON_OPERATION_POLICY;
                PreparedStatement statement = connection.prepareStatement(dbQuery);
                statement.setString(1, policyUUID);
                statement.executeUpdate();
                statement.close();

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                handleExceptionWithCode("Failed to add common operation policy " + policySpecification.getName(), e,
                        ExceptionCodes.APIMGT_DAO_EXCEPTION);
            }
        } catch (SQLException e) {
            handleExceptionWithCode("Failed to add common operation policy " + policySpecification.getName(), e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        return policyUUID;
    }

    @Override
    public OperationPolicyData getAPISpecificOperationPolicyByPolicyName(String policyName, String policyVersion,
                                                                         String apiUUID, String revisionUUID,
                                                                         String organization, boolean isWithPolicyDefinition)
            throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            return getAPISpecificOperationPolicyByPolicyName(connection, policyName, policyVersion, apiUUID,
                    revisionUUID, organization, isWithPolicyDefinition);
        } catch (SQLException e) {
            handleExceptionWithCode("Failed to get API specific operation policy for name " + policyName + " with API UUID "
                    + apiUUID + " revision UUID " + revisionUUID, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        return null;
    }

    public OperationPolicyData getCommonOperationPolicyByPolicyName(String policyName, String policyVersion,
                                                                    String organization, boolean isWithPolicyDefinition)
            throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            return getCommonOperationPolicyByPolicyName(connection, policyName, policyVersion, organization,
                    isWithPolicyDefinition);
        } catch (SQLException e) {
            handleExceptionWithCode("Failed to get common operation policy for name " + policyName + "for organization "
                            + organization, e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        return null;
    }

    private OperationPolicyData getCommonOperationPolicyByPolicyName(Connection connection, String policyName,
                                                                     String policyVersion, String tenantDomain,
                                                                     boolean isWithPolicyDefinition)
            throws SQLException {

        String dbQuery =
                SQLConstants.OperationPolicyConstants.GET_COMMON_OPERATION_POLICY_FROM_POLICY_NAME;

        PreparedStatement statement = connection.prepareStatement(dbQuery);
        statement.setString(1, policyName);
        statement.setString(2, policyVersion);
        statement.setString(3, tenantDomain);
        ResultSet rs = statement.executeQuery();
        OperationPolicyData policyData = null;
        if (rs.next()) {
            policyData = new OperationPolicyData();
            policyData.setOrganization(tenantDomain);
            policyData.setPolicyId(rs.getString("POLICY_UUID"));
            policyData.setMd5Hash(rs.getString("POLICY_MD5"));
            policyData.setSpecification(populatePolicySpecificationFromRS(rs));
        }
        rs.close();
        statement.close();

        if (isWithPolicyDefinition && policyData != null) {
            populatePolicyDefinitions(connection, policyData.getPolicyId(), policyData);
        }
        return policyData;
    }

    @Override
    public void updateOperationPolicy(String policyId, OperationPolicyData policyData)
            throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            connection.setAutoCommit(false);
            updateOperationPolicy(connection, policyId, policyData);
            connection.commit();
        } catch (SQLException e) {
            handleException("Failed to update the operation policy with ID " + policyId, e);
        }
    }

    @Override
    public List<OperationPolicyData> getLightWeightVersionOfAllOperationPolicies(String apiUUID,
                                                                                 String organization)
            throws APIManagementException {

        String dbQuery;
        if (apiUUID != null) {
            dbQuery =
                    SQLConstants.OperationPolicyConstants.GET_ALL_API_SPECIFIC_OPERATION_POLICIES_WITHOUT_CLONED_POLICIES;
        } else {
            dbQuery = SQLConstants.OperationPolicyConstants.GET_ALL_COMMON_OPERATION_POLICIES;
        }
        List<OperationPolicyData> policyDataList = new ArrayList<>();
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(dbQuery)) {
            statement.setString(1, organization);
            if (apiUUID != null) {
                statement.setString(2, apiUUID);
            }
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                OperationPolicyData policyData = new OperationPolicyData();
                policyData.setOrganization(organization);
                policyData.setPolicyId(rs.getString("POLICY_UUID"));
                policyData.setMd5Hash(rs.getString("POLICY_MD5"));
                policyData.setSpecification(populatePolicySpecificationFromRS(rs));
                if (apiUUID != null) {
                    policyData.setApiUUID(apiUUID);
                }
                policyDataList.add(policyData);
            }
        } catch (SQLException e) {
            handleExceptionWithCode("Failed to get all the operation policy for tenant " + organization, e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        return policyDataList;
    }

    @Override
    public void deleteOperationPolicyByPolicyId(String policyId) throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            connection.setAutoCommit(false);
            if (!getPolicyUsageByPolicyId(connection, policyId)) {
                deleteOperationPolicyByPolicyId(connection, policyId);
                connection.commit();
            } else {
                throw new APIManagementException("Cannot delete operation policy with id " + policyId
                        + " as policy usages exists",
                        ExceptionCodes.from(ExceptionCodes.OPERATION_POLICY_USAGE_EXISTS, policyId));
            }
        } catch (SQLException e) {
            handleExceptionWithCode("Failed to delete operation policy " + policyId, e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
    }

    private boolean getPolicyUsageByPolicyId(Connection connection, String policyId) throws SQLException {

        boolean result = false;
        String dbQuery = SQLConstants.OperationPolicyConstants.GET_EXISTING_POLICY_USAGES_BY_POLICY_UUID;
        PreparedStatement statement = connection.prepareStatement(dbQuery);
        statement.setString(1, policyId);
        ResultSet rs = statement.executeQuery();

        if (rs.next()) {
            result = rs.getInt("POLICY_COUNT") != 0;
        }
        rs.close();
        statement.close();
        return result;
    }

    @Override
    public UserApplicationAPIUsage[] getAllAPIUsageByProvider(String providerName) throws APIManagementException {

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet result = null;

        try {
            String sqlQuery = SQLConstants.GET_APP_API_USAGE_BY_PROVIDER_SQL;
            connection = APIMgtDBUtil.getConnection();

            ps = connection.prepareStatement(sqlQuery);
            ps.setString(1, APIUtil.replaceEmailDomainBack(providerName));
            result = ps.executeQuery();

            Map<String, UserApplicationAPIUsage> userApplicationUsages = new TreeMap<String, UserApplicationAPIUsage>();
            while (result.next()) {
                int subId = result.getInt("SUBSCRIPTION_ID");
                String userId = result.getString("USER_ID");
                String application = result.getString("APPNAME");
                int appId = result.getInt("APPLICATION_ID");
                String subStatus = result.getString("SUB_STATUS");
                String subsCreateState = result.getString("SUBS_CREATE_STATE");
                String key = userId + "::" + application;
                UserApplicationAPIUsage usage = userApplicationUsages.get(key);
                if (usage == null) {
                    usage = new UserApplicationAPIUsage();
                    usage.setUserId(userId);
                    usage.setApplicationName(application);
                    usage.setAppId(appId);
                    userApplicationUsages.put(key, usage);
                }
                APIIdentifier apiId = new APIIdentifier(result.getString("API_PROVIDER"), result.getString
                        ("API_NAME"), result.getString("API_VERSION"));
                SubscribedAPI apiSubscription = new SubscribedAPI(new Subscriber(userId), apiId);
                apiSubscription.setSubStatus(subStatus);
                apiSubscription.setSubCreatedStatus(subsCreateState);
                apiSubscription.setUUID(result.getString("SUB_UUID"));
                apiSubscription.setTier(new Tier(result.getString("SUB_TIER_ID")));
                Application applicationObj = new Application(result.getString("APP_UUID"));
                apiSubscription.setApplication(applicationObj);
                usage.addApiSubscriptions(apiSubscription);
            }
            return userApplicationUsages.values().toArray(new UserApplicationAPIUsage[userApplicationUsages.size()]);
        } catch (SQLException e) {
            handleExceptionWithCode("Failed to find API Usage for :" + providerName, e,
                    ExceptionCodes.from(ExceptionCodes.FAILED_FIND_API_USAGE, providerName));
            return null;
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, connection, result);
        }
    }

    @Override
    public APIIdentifier getAPIIdentifierFromUUID(String uuid) throws APIManagementException {

        APIIdentifier identifier = null;
        String sql = SQLConstants.GET_API_IDENTIFIER_BY_UUID_SQL;
        try (Connection connection = APIMgtDBUtil.getConnection()) {
            PreparedStatement prepStmt = connection.prepareStatement(sql);
            prepStmt.setString(1, uuid);
            try (ResultSet resultSet = prepStmt.executeQuery()) {
                while (resultSet.next()) {
                    String provider = resultSet.getString(1);
                    String name = resultSet.getString(2);
                    String version = resultSet.getString(3);
                    identifier = new APIIdentifier(APIUtil.replaceEmailDomain(provider), name, version, uuid);
                }
            }
        } catch (SQLException e) {
            handleExceptionWithCode("Failed to retrieve the API Identifier details for UUID : " + uuid, e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        return identifier;
    }

    @Override
    public UserApplicationAPIUsage[] getAllAPIUsageByProviderAndApiId(String uuid, String organization)
            throws APIManagementException {

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet result = null;

        try {
            String sqlQuery = SQLConstants.GET_APP_API_USAGE_BY_UUID_SQL;
            connection = APIMgtDBUtil.getConnection();

            ps = connection.prepareStatement(sqlQuery);
            ps.setString(1, uuid);
            ps.setString(2, organization);
            result = ps.executeQuery();

            Map<String, UserApplicationAPIUsage> userApplicationUsages = new TreeMap<String, UserApplicationAPIUsage>();
            while (result.next()) {
                int subId = result.getInt("SUBSCRIPTION_ID");
                String userId = result.getString("USER_ID");
                String application = result.getString("APPNAME");
                int appId = result.getInt("APPLICATION_ID");
                String subStatus = result.getString("SUB_STATUS");
                String subsCreateState = result.getString("SUBS_CREATE_STATE");
                String key = userId + "::" + application;
                UserApplicationAPIUsage usage = userApplicationUsages.get(key);
                if (usage == null) {
                    usage = new UserApplicationAPIUsage();
                    usage.setUserId(userId);
                    usage.setApplicationName(application);
                    usage.setAppId(appId);
                    userApplicationUsages.put(key, usage);
                }
                APIIdentifier apiId = new APIIdentifier(result.getString("API_PROVIDER"), result.getString
                        ("API_NAME"), result.getString("API_VERSION"));
                SubscribedAPI apiSubscription = new SubscribedAPI(new Subscriber(userId), apiId);
                apiSubscription.setSubStatus(subStatus);
                apiSubscription.setSubCreatedStatus(subsCreateState);
                apiSubscription.setUUID(result.getString("SUB_UUID"));
                apiSubscription.setTier(new Tier(result.getString("SUB_TIER_ID")));
                Application applicationObj = new Application(result.getString("APP_UUID"));
                apiSubscription.setApplication(applicationObj);
                usage.addApiSubscriptions(apiSubscription);
            }
            return userApplicationUsages.values().toArray(new UserApplicationAPIUsage[userApplicationUsages.size()]);
        } catch (SQLException e) {
            handleExceptionWithCode("Failed to find API Usage for API with UUID :" + uuid, e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
            return null;
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, connection, result);
        }
    }

    @Override
    public UserApplicationAPIUsage[] getAllAPIProductUsageByProvider(String providerName) throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement ps =
                     connection.prepareStatement(SQLConstants.GET_APP_API_USAGE_BY_PROVIDER_SQL)) {
            ps.setString(1, APIUtil.replaceEmailDomainBack(providerName));
            try (ResultSet result = ps.executeQuery()) {
                Map<String, UserApplicationAPIUsage> userApplicationUsages = new TreeMap<String,
                        UserApplicationAPIUsage>();
                while (result.next()) {
                    int subId = result.getInt("SUBSCRIPTION_ID");
                    String userId = result.getString("USER_ID");
                    String application = result.getString("APPNAME");
                    int appId = result.getInt("APPLICATION_ID");
                    String subStatus = result.getString("SUB_STATUS");
                    String subsCreateState = result.getString("SUBS_CREATE_STATE");
                    String key = userId + "::" + application;
                    UserApplicationAPIUsage usage = userApplicationUsages.get(key);
                    if (usage == null) {
                        usage = new UserApplicationAPIUsage();
                        usage.setUserId(userId);
                        usage.setApplicationName(application);
                        usage.setAppId(appId);
                        userApplicationUsages.put(key, usage);
                    }
                    APIProductIdentifier apiProductId = new APIProductIdentifier(result.getString("API_PROVIDER"),
                            result.getString
                                    ("API_NAME"), result.getString("API_VERSION"));
                    SubscribedAPI apiSubscription = new SubscribedAPI(new Subscriber(userId), apiProductId);
                    apiSubscription.setSubStatus(subStatus);
                    apiSubscription.setSubCreatedStatus(subsCreateState);
                    apiSubscription.setUUID(result.getString("SUB_UUID"));
                    apiSubscription.setTier(new Tier(result.getString("SUB_TIER_ID")));
                    Application applicationObj = new Application(result.getString("APP_UUID"));
                    apiSubscription.setApplication(applicationObj);
                    usage.addApiSubscriptions(apiSubscription);
                }
                return userApplicationUsages.values().toArray(new UserApplicationAPIUsage[userApplicationUsages.size()]);
            }
        } catch (SQLException e) {
            handleException("Failed to find API Product Usage for :" + providerName, e);
        }

        return new UserApplicationAPIUsage[]{};
    }

    @Override
    public Set<Subscriber> getSubscribersOfAPI(APIIdentifier identifier) throws APIManagementException {

        Set<Subscriber> subscribers = new HashSet<Subscriber>();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet result = null;

        try {
            String sqlQuery = SQLConstants.GET_SUBSCRIBERS_OF_API_SQL;
            connection = APIMgtDBUtil.getConnection();

            ps = connection.prepareStatement(sqlQuery);
            ps.setString(1, APIUtil.replaceEmailDomainBack(identifier.getProviderName()));
            ps.setString(2, identifier.getApiName());
            ps.setString(3, identifier.getVersion());
            result = ps.executeQuery();
            while (result.next()) {
                Subscriber subscriber = new Subscriber(result.getString(APIConstants.SUBSCRIBER_FIELD_USER_ID));
                subscriber.setSubscribedDate(result.getTimestamp(APIConstants.SUBSCRIBER_FIELD_DATE_SUBSCRIBED));
                subscribers.add(subscriber);
            }
        } catch (SQLException e) {
            handleException("Failed to get subscribers for :" + identifier.getApiName(), e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, connection, result);
        }
        return subscribers;
    }

    @Override
    public List<SubscribedAPI> getSubscriptionsOfAPI(String apiName, String apiVersion, String provider)
            throws APIManagementException {

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet result = null;
        List<SubscribedAPI> subscriptions = new ArrayList<>();

        try {
            String sqlQuery = SQLConstants.GET_SUBSCRIPTIONS_OF_API_SQL;
            connection = APIMgtDBUtil.getConnection();

            ps = connection.prepareStatement(sqlQuery);
            ps.setString(1, apiName);
            ps.setString(2, apiVersion);
            ps.setString(3, provider);
            result = ps.executeQuery();

            while (result.next()) {
                APIIdentifier apiId = new APIIdentifier(result.getString("API_PROVIDER"), apiName, apiVersion);
                Subscriber subscriber = new Subscriber(result.getString("USER_ID"));
                SubscribedAPI subscription = new SubscribedAPI(subscriber, apiId);
                subscription.setUUID(result.getString("SUB_UUID"));
                subscription.setSubStatus(result.getString("SUB_STATUS"));
                subscription.setSubCreatedStatus(result.getString("SUBS_CREATE_STATE"));
                subscription.setTier(new Tier(result.getString("SUB_TIER_ID")));
                subscription.setCreatedTime(result.getString("SUB_CREATED_TIME"));

                Application application = new Application(result.getInt("APPLICATION_ID"));
                application.setName(result.getString("APPNAME"));
                subscription.setApplication(application);

                subscriptions.add(subscription);
            }
        } catch (SQLException e) {
            handleException("Error occurred while reading subscriptions of API: " + apiName + ':' + apiVersion, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, connection, result);
        }
        return subscriptions;
    }

    @Override
    public long getAPISubscriptionCountByAPI(Identifier identifier) throws APIManagementException {

        String sqlQuery = SQLConstants.GET_API_SUBSCRIPTION_COUNT_BY_API_SQL;
        String artifactType = APIConstants.API_IDENTIFIER_TYPE;
        if (identifier instanceof APIProductIdentifier) {
            artifactType = APIConstants.API_PRODUCT_IDENTIFIER_TYPE;
        }

        long subscriptions = 0;

        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(sqlQuery)) {
            ps.setString(1, APIUtil.replaceEmailDomainBack(identifier.getProviderName()));
            ps.setString(2, identifier.getName());
            ps.setString(3, identifier.getVersion());
            try (ResultSet result = ps.executeQuery()) {
                while (result.next()) {
                    subscriptions = result.getLong("SUB_ID");
                }
            }
        } catch (SQLException e) {
            handleException("Failed to get subscription count for " + artifactType, e);
        }

        return subscriptions;
    }

    @Override
    public BlockConditionsDTO getSubscriptionBlockCondition(String conditionValue, String tenantDomain)
            throws APIManagementException {

        Connection connection = null;
        PreparedStatement selectPreparedStatement = null;
        ResultSet resultSet = null;
        BlockConditionsDTO blockCondition = null;
        try {
            String query = SQLConstants.ThrottleSQLConstants.GET_SUBSCRIPTION_BLOCK_CONDITION_BY_VALUE_AND_DOMAIN_SQL;
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(true);
            selectPreparedStatement = connection.prepareStatement(query);
            selectPreparedStatement.setString(1, conditionValue);
            selectPreparedStatement.setString(2, tenantDomain);
            resultSet = selectPreparedStatement.executeQuery();
            if (resultSet.next()) {
                blockCondition = new BlockConditionsDTO();
                blockCondition.setEnabled(resultSet.getBoolean("ENABLED"));
                blockCondition.setConditionType(resultSet.getString("TYPE"));
                blockCondition.setConditionValue(resultSet.getString("BLOCK_CONDITION"));
                blockCondition.setConditionId(resultSet.getInt("CONDITION_ID"));
                blockCondition.setTenantDomain(resultSet.getString("DOMAIN"));
                blockCondition.setUUID(resultSet.getString("UUID"));
            }
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    handleException("Failed to rollback getting Subscription Block condition with condition value "
                            + conditionValue + " of tenant " + tenantDomain, ex);
                }
            }
            handleException("Failed to get Subscription Block condition with condition value " + conditionValue
                    + " of tenant " + tenantDomain, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(selectPreparedStatement, connection, resultSet);
        }
        return blockCondition;
    }

    @Override
    public List<APIProductResource> getProductMappingsForAPI(API api) throws APIManagementException {

        List<APIProductResource> productMappings = new ArrayList<>();

        Set<URITemplate> uriTemplatesOfAPI = getURITemplatesOfAPI(api.getUuid());

        for (URITemplate uriTemplate : uriTemplatesOfAPI) {
            Set<APIProductIdentifier> apiProductIdentifiers = uriTemplate.retrieveUsedByProducts();

            for (APIProductIdentifier apiProductIdentifier : apiProductIdentifiers) {
                APIProductResource productMapping = new APIProductResource();
                productMapping.setProductIdentifier(apiProductIdentifier);
                productMapping.setUriTemplate(uriTemplate);

                productMappings.add(productMapping);
            }
        }

        return productMappings;
    }

    public Set<URITemplate> getURITemplatesOfAPI(String uuid)
            throws APIManagementException {

        String currentApiUuid;
        APIRevision apiRevision = checkAPIUUIDIsARevisionUUID(uuid);
        if (apiRevision != null && apiRevision.getApiUUID() != null) {
            currentApiUuid = apiRevision.getApiUUID();
        } else {
            currentApiUuid = uuid;
        }
        Map<Integer, URITemplate> uriTemplates = new LinkedHashMap<>();
        Map<Integer, Set<String>> scopeToURITemplateId = new HashMap<>();
        //Check If the API is a Revision
        if (apiRevision != null) {
            try (Connection conn = APIMgtDBUtil.getConnection();
                 PreparedStatement ps = conn.prepareStatement(SQLConstants.GET_URL_TEMPLATES_OF_API_REVISION_SQL)) {
                ps.setString(1, currentApiUuid);
                ps.setString(2, uuid);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Integer uriTemplateId = rs.getInt("URL_MAPPING_ID");
                        String scopeName = rs.getString("SCOPE_NAME");

                        if (scopeToURITemplateId.containsKey(uriTemplateId) && !StringUtils.isEmpty(scopeName)
                                && !scopeToURITemplateId.get(uriTemplateId).contains(scopeName)
                                && uriTemplates.containsKey(uriTemplateId)) {
                            Scope scope = new Scope();
                            scope.setKey(scopeName);
                            scopeToURITemplateId.get(uriTemplateId).add(scopeName);
                            uriTemplates.get(uriTemplateId).setScopes(scope);
                            continue;
                        }
                        String urlPattern = rs.getString("URL_PATTERN");
                        String verb = rs.getString("HTTP_METHOD");

                        URITemplate uriTemplate = new URITemplate();
                        uriTemplate.setUriTemplate(urlPattern);
                        uriTemplate.setHTTPVerb(verb);
                        uriTemplate.setHttpVerbs(verb);
                        uriTemplate.setId(uriTemplateId);
                        String authType = rs.getString("AUTH_SCHEME");
                        String throttlingTier = rs.getString("THROTTLING_TIER");
                        if (StringUtils.isNotEmpty(scopeName)) {
                            Scope scope = new Scope();
                            scope.setKey(scopeName);
                            uriTemplate.setScope(scope);
                            uriTemplate.setScopes(scope);
                            Set<String> templateScopes = new HashSet<>();
                            templateScopes.add(scopeName);
                            scopeToURITemplateId.put(uriTemplateId, templateScopes);
                        }
                        uriTemplate.setAuthType(authType);
                        uriTemplate.setAuthTypes(authType);
                        uriTemplate.setThrottlingTier(throttlingTier);
                        uriTemplate.setThrottlingTiers(throttlingTier);
                        uriTemplate.setId(uriTemplateId);

                        InputStream mediationScriptBlob = rs.getBinaryStream("MEDIATION_SCRIPT");
                        if (mediationScriptBlob != null) {
                            String script = APIMgtDBUtil.getStringFromInputStream(mediationScriptBlob);
                            uriTemplate.setMediationScript(script);
                            uriTemplate.setMediationScripts(verb, script);
                        }

                        uriTemplates.put(uriTemplateId, uriTemplate);
                    }
                }

                setAssociatedAPIProducts(currentApiUuid, uriTemplates);
                setOperationPolicies(apiRevision.getRevisionUUID(), uriTemplates);
            } catch (SQLException e) {
                handleExceptionWithCode("Failed to get URI Templates of API with UUID " + uuid, e,
                        ExceptionCodes.APIMGT_DAO_EXCEPTION);
            }
        } else {
            try (Connection conn = APIMgtDBUtil.getConnection();
                 PreparedStatement ps = conn.prepareStatement(SQLConstants.GET_URL_TEMPLATES_OF_API_SQL)) {
                ps.setString(1, currentApiUuid);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Integer uriTemplateId = rs.getInt("URL_MAPPING_ID");
                        String scopeName = rs.getString("SCOPE_NAME");

                        if (scopeToURITemplateId.containsKey(uriTemplateId) && !StringUtils.isEmpty(scopeName)
                                && !scopeToURITemplateId.get(uriTemplateId).contains(scopeName)
                                && uriTemplates.containsKey(uriTemplateId)) {
                            Scope scope = new Scope();
                            scope.setKey(scopeName);
                            scopeToURITemplateId.get(uriTemplateId).add(scopeName);
                            uriTemplates.get(uriTemplateId).setScopes(scope);
                            continue;
                        }
                        String urlPattern = rs.getString("URL_PATTERN");
                        String verb = rs.getString("HTTP_METHOD");

                        URITemplate uriTemplate = new URITemplate();
                        uriTemplate.setUriTemplate(urlPattern);
                        uriTemplate.setHTTPVerb(verb);
                        uriTemplate.setHttpVerbs(verb);
                        String authType = rs.getString("AUTH_SCHEME");
                        String throttlingTier = rs.getString("THROTTLING_TIER");
                        if (StringUtils.isNotEmpty(scopeName)) {
                            Scope scope = new Scope();
                            scope.setKey(scopeName);
                            uriTemplate.setScope(scope);
                            uriTemplate.setScopes(scope);
                            Set<String> templateScopes = new HashSet<>();
                            templateScopes.add(scopeName);
                            scopeToURITemplateId.put(uriTemplateId, templateScopes);
                        }
                        uriTemplate.setAuthType(authType);
                        uriTemplate.setAuthTypes(authType);
                        uriTemplate.setThrottlingTier(throttlingTier);
                        uriTemplate.setThrottlingTiers(throttlingTier);
                        uriTemplate.setId(uriTemplateId);

                        InputStream mediationScriptBlob = rs.getBinaryStream("MEDIATION_SCRIPT");
                        if (mediationScriptBlob != null) {
                            String script = APIMgtDBUtil.getStringFromInputStream(mediationScriptBlob);
                            uriTemplate.setMediationScript(script);
                            uriTemplate.setMediationScripts(verb, script);
                        }

                        uriTemplates.put(uriTemplateId, uriTemplate);
                    }
                }

                setAssociatedAPIProducts(currentApiUuid, uriTemplates);
                setOperationPolicies(currentApiUuid, uriTemplates);
            } catch (SQLException e) {
                handleExceptionWithCode("Failed to get URI Templates of API with UUID " + currentApiUuid, e,
                        ExceptionCodes.APIMGT_DAO_EXCEPTION);
            }
        }
        return new LinkedHashSet<>(uriTemplates.values());
    }

    private void setAssociatedAPIProducts(String uuid, Map<Integer, URITemplate> uriTemplates)
            throws SQLException {

        try (Connection conn = APIMgtDBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQLConstants.GET_API_PRODUCT_URI_TEMPLATE_ASSOCIATION_SQL)) {
            ps.setString(1, uuid);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String productName = rs.getString("API_NAME");
                    String productVersion = rs.getString("API_VERSION");
                    String productProvider = rs.getString("API_PROVIDER");
                    int uriTemplateId = rs.getInt("URL_MAPPING_ID");

                    URITemplate uriTemplate = uriTemplates.get(uriTemplateId);
                    if (uriTemplate != null) {
                        APIProductIdentifier productIdentifier = new APIProductIdentifier
                                (productProvider, productName, productVersion);
                        uriTemplate.addUsedByProduct(productIdentifier);
                    }
                }
            }
        }
    }

    /**
     * Sets operation policies to uriTemplates map
     *
     * @param uuid         UUID of API or API Revision
     * @param uriTemplates URI Templates map with URL_MAPPING_ID as the map key
     * @throws SQLException
     * @throws APIManagementException
     */
    private void setOperationPolicies(String uuid, Map<Integer, URITemplate> uriTemplates)
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
                    int uriTemplateId = rs.getInt("URL_MAPPING_ID");

                    URITemplate uriTemplate = uriTemplates.get(uriTemplateId);
                    if (uriTemplate != null) {
                        OperationPolicy operationPolicy = populateOperationPolicyWithRS(rs);
                        uriTemplate.addOperationPolicy(operationPolicy);
                    }
                }
            }
        }
    }

    @Override
    public List<KeyManagerConfigurationDTO> getKeyManagerConfigurationsByOrganization(String organization)
            throws APIManagementException {

        List<KeyManagerConfigurationDTO> keyManagerConfigurationDTOS = new ArrayList<>();
        final String query = "SELECT * FROM AM_KEY_MANAGER WHERE ORGANIZATION = ? ";
        try (Connection conn = APIMgtDBUtil.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(query)) {
            preparedStatement.setString(1, organization);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    KeyManagerConfigurationDTO keyManagerConfigurationDTO = new KeyManagerConfigurationDTO();
                    String uuid = resultSet.getString("UUID");
                    keyManagerConfigurationDTO.setUuid(uuid);
                    keyManagerConfigurationDTO.setName(resultSet.getString("NAME"));
                    keyManagerConfigurationDTO.setDisplayName(resultSet.getString("DISPLAY_NAME"));
                    keyManagerConfigurationDTO.setDescription(resultSet.getString("DESCRIPTION"));
                    keyManagerConfigurationDTO.setType(resultSet.getString("TYPE"));
                    keyManagerConfigurationDTO.setEnabled(resultSet.getBoolean("ENABLED"));
                    keyManagerConfigurationDTO.setOrganization(organization);
                    keyManagerConfigurationDTO.setTokenType(resultSet.getString("TOKEN_TYPE"));
                    keyManagerConfigurationDTO.setExternalReferenceId(resultSet.getString("EXTERNAL_REFERENCE_ID"));
                    try (InputStream configuration = resultSet.getBinaryStream("CONFIGURATION")) {
                        String configurationContent = IOUtils.toString(configuration);
                        Map map = new Gson().fromJson(configurationContent, Map.class);
                        keyManagerConfigurationDTO.setAdditionalProperties(map);
                    } catch (IOException e) {
                        log.error("Error while converting configurations in " + uuid, e);
                    }
                    keyManagerConfigurationDTOS.add(keyManagerConfigurationDTO);
                }
            }
        } catch (SQLException e) {
            handleExceptionWithCode("Error while retrieving key manager configurations for organization "
                    + organization, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        return keyManagerConfigurationDTOS;
    }

    @Override
    public Set<String> getAllLocalScopeKeysForAPI(String uuid, int tenantId)
            throws APIManagementException {

        int apiId;
        Set<String> localScopes = new HashSet<>();
        String getAllLocalScopesStmt = SQLConstants.GET_ALL_LOCAL_SCOPES_FOR_API_SQL;
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(getAllLocalScopesStmt)) {
            apiId = getAPIID(uuid, connection);
            preparedStatement.setInt(1, apiId);
            preparedStatement.setInt(2, tenantId);
            preparedStatement.setInt(3, tenantId);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    localScopes.add(rs.getString("SCOPE_NAME"));
                }
            }
        } catch (SQLException e) {
            handleExceptionWithCode("Failed while getting local scopes for API:" + uuid + " tenant: " + tenantId, e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        return localScopes;
    }

    public Set<String> getVersionedLocalScopeKeysForAPI(String uuid, int tenantId)
            throws APIManagementException {

        int apiId;
        Set<String> localScopes = new HashSet<>();
        String getUnVersionedLocalScopes = SQLConstants.GET_VERSIONED_LOCAL_SCOPES_FOR_API_SQL;
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(getUnVersionedLocalScopes)) {
            apiId = getAPIID(uuid, connection);
            preparedStatement.setInt(1, apiId);
            preparedStatement.setInt(2, tenantId);
            preparedStatement.setInt(3, tenantId);
            preparedStatement.setInt(4, apiId);
            preparedStatement.setInt(5, tenantId);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    localScopes.add(rs.getString("SCOPE_NAME"));
                }
            }
        } catch (SQLException e) {
            handleExceptionWithCode("Failed while getting versioned local scopes for API with UUID:" + uuid + " tenant: "
                    + tenantId, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        return localScopes;
    }

    @Override
    public void updateURITemplates(API api, int tenantId) throws APIManagementException {

        int apiId;
        String deleteOldMappingsQuery = SQLConstants.REMOVE_FROM_URI_TEMPLATES_SQL;
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(deleteOldMappingsQuery)) {
            connection.setAutoCommit(false);
            apiId = getAPIID(api.getUuid(), connection);
            prepStmt.setInt(1, apiId);
            try {
                prepStmt.execute();
                addURITemplates(apiId, api, tenantId, connection);
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                handleExceptionWithCode("Error while deleting URL template(s) for API : " + api.getId(), e,
                        ExceptionCodes.APIMGT_DAO_EXCEPTION);
            }
        } catch (SQLException e) {
            handleExceptionWithCode("Error while deleting URL template(s) for API : " + api.getId(), e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
    }

    @Override
    public void addOperationPolicyMapping(Set<URITemplate> uriTemplates) throws APIManagementException {
        if (uriTemplates != null && !uriTemplates.isEmpty()) {
            try (Connection connection = APIMgtDBUtil.getConnection()) {
                connection.setAutoCommit(false);
                try (PreparedStatement preparedStatement =
                             connection.prepareStatement(SQLConstants.OperationPolicyConstants.ADD_API_OPERATION_POLICY_MAPPING)) {
                    for (URITemplate uriTemplate : uriTemplates){
                        List<OperationPolicy> operationPolicies = uriTemplate.getOperationPolicies();
                        if (operationPolicies != null && !operationPolicies.isEmpty()){
                            for (OperationPolicy operationPolicy : operationPolicies){
                                Gson gson = new Gson();
                                String paramJSON = gson.toJson(operationPolicy.getParameters());
                                preparedStatement.setInt(1, uriTemplate.getId());
                                preparedStatement.setString(2,operationPolicy.getPolicyId());
                                preparedStatement.setString(3, operationPolicy.getDirection());
                                preparedStatement.setString(4, paramJSON);
                                preparedStatement.setInt(5, operationPolicy.getOrder());
                                preparedStatement.addBatch();
                            }
                        }
                    }
                    preparedStatement.executeBatch();
                    connection.commit();
                }catch(SQLException e){
                    connection.rollback();
                    throw e;
                }
            } catch (SQLException e) {
                throw new APIManagementException("Error while updating operation Policy mapping for API", e,
                        ExceptionCodes.APIMGT_DAO_EXCEPTION);
            }
        }
    }

    @Override
    public String getSubscriberName(String subscriptionId) throws APIManagementException {

        int subscriberId = getSubscriberIdBySubscriptionUUID(subscriptionId);
        Subscriber subscriber = getSubscriber(subscriberId);
        if (subscriber != null) {
            return subscriber.getName();
        }
        return StringUtils.EMPTY;
    }

    /**
     * Get subscriber ID using subscription ID
     *
     * @param subscriptionId
     * @return subscriber ID
     * @throws APIManagementException
     */
    private int getSubscriberIdBySubscriptionUUID(String subscriptionId) throws APIManagementException {

        int subscirberId = 0;
        String query = SQLConstants.GET_SUBSCRIBER_ID_BY_SUBSCRIPTION_UUID_SQL;
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, subscriptionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    subscirberId = rs.getInt(APIConstants.APPLICATION_SUBSCRIBER_ID);
                }
            }
        } catch (SQLException e) {
            handleException("Error while retrieving Subscriber ID: ", e);
        }
        return subscirberId;
    }

    public Subscriber getSubscriber(int subscriberId) throws APIManagementException {

        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            conn = APIMgtDBUtil.getConnection();
            String query = SQLConstants.GET_SUBSCRIBER_SQL;

            ps = conn.prepareStatement(query);
            ps.setInt(1, subscriberId);
            rs = ps.executeQuery();
            if (rs.next()) {
                Subscriber subscriber = new Subscriber(rs.getString("USER_ID"));
                subscriber.setId(subscriberId);
                subscriber.setTenantId(rs.getInt("TENANT_ID"));
                subscriber.setEmail(rs.getString("EMAIL_ADDRESS"));
                subscriber.setSubscribedDate(new Date(rs.getTimestamp("DATE_SUBSCRIBED").getTime()));
                return subscriber;
            }
        } catch (SQLException e) {
            handleException("Error while retrieving subscriber: " + e.getMessage(), e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
        return null;
    }

    @Override
    public String cloneOperationPolicy(String apiUUID, OperationPolicyData operationPolicyData)
            throws APIManagementException {
        try (Connection connection = APIMgtDBUtil.getConnection()) {
            try {
                connection.setAutoCommit(false);
                String policyId = addAPISpecificOperationPolicy(connection, apiUUID, null, operationPolicyData, operationPolicyData.getClonedCommonPolicyId());
                connection.commit();
                return policyId;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new APIManagementException("Error while cloning Operation policies", e);
        }
    }

    @Override
    public String retrieveServiceKeyByApiId(int apiId, int tenantId) throws APIManagementException {

        String retrieveServiceKeySQL = SQLConstants.GET_SERVICE_KEY_BY_API_ID_SQL;
        String serviceKey = StringUtils.EMPTY;
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(retrieveServiceKeySQL)) {
            preparedStatement.setInt(1, apiId);
            preparedStatement.setInt(2, tenantId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    serviceKey = resultSet.getString(APIConstants.ServiceCatalogConstants.SERVICE_KEY);
                }
            }
        } catch (SQLException e) {
            handleExceptionWithCode("Error while retrieving the Service Key associated with API " + apiId, e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        return serviceKey;
    }

    @Override
    public List<LifeCycleEvent> getLifeCycleEvents(String uuid) throws APIManagementException {

        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        String sqlQuery = SQLConstants.GET_LIFECYCLE_EVENT_SQL;
        int apiOrApiProductId = getAPIID(uuid);

        List<LifeCycleEvent> events = new ArrayList<LifeCycleEvent>();
        try {
            connection = APIMgtDBUtil.getConnection();
            prepStmt = connection.prepareStatement(sqlQuery);
            prepStmt.setInt(1, apiOrApiProductId);
            rs = prepStmt.executeQuery();

            while (rs.next()) {
                LifeCycleEvent event = new LifeCycleEvent();
                String oldState = rs.getString("PREVIOUS_STATE");
                //event.setOldStatus(oldState != null ? APIStatus.valueOf(oldState) : null);
                event.setOldStatus(oldState);
                //event.setNewStatus(APIStatus.valueOf(rs.getString("NEW_STATE")));
                event.setNewStatus(rs.getString("NEW_STATE"));
                event.setUserId(rs.getString("USER_ID"));
                event.setDate(rs.getTimestamp("EVENT_DATE"));
                events.add(event);
            }

            Collections.sort(events, (o1, o2) -> o1.getDate().compareTo(o2.getDate()));
        } catch (SQLException e) {
            handleExceptionWithCode("Error while getting the lifecycle events", e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
        }
        return events;
    }

    @Override
    public void updateSubscription(APIIdentifier identifier, String subStatus, int applicationId, String organization)
            throws APIManagementException {

        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        PreparedStatement updatePs = null;
        int apiId = -1;

        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);

            String getApiQuery = SQLConstants.GET_API_ID_SQL;
            ps = conn.prepareStatement(getApiQuery);
            ps.setString(1, APIUtil.replaceEmailDomainBack(identifier.getProviderName()));
            ps.setString(2, identifier.getApiName());
            ps.setString(3, identifier.getVersion());
            resultSet = ps.executeQuery();
            if (resultSet.next()) {
                apiId = resultSet.getInt("API_ID");
            }

            if (apiId == -1) {
                String msg = "Unable to get the API ID for: " + identifier;
                log.error(msg);
                throw new APIManagementException(msg);
            }

            String subsCreateStatus = getSubscriptionCreaeteStatus(identifier, applicationId, organization, conn);

            if (APIConstants.SubscriptionCreatedStatus.UN_SUBSCRIBE.equals(subsCreateStatus)) {
                deleteSubscriptionByApiIDAndAppID(apiId, applicationId, conn);
            }

            //This query to update the AM_SUBSCRIPTION table
            String sqlQuery = SQLConstants.UPDATE_SUBSCRIPTION_OF_APPLICATION_SQL;

            //Updating data to the AM_SUBSCRIPTION table
            updatePs = conn.prepareStatement(sqlQuery);
            updatePs.setString(1, subStatus);
            updatePs.setString(2, identifier.getProviderName());
            updatePs.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            updatePs.setInt(4, apiId);
            updatePs.setInt(5, applicationId);
            updatePs.execute();

            // finally commit transaction
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error("Failed to rollback the add subscription ", e1);
                }
            }
            handleException("Failed to update subscription data ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, resultSet);
            APIMgtDBUtil.closeAllConnections(updatePs, null, null);
        }
    }

    /**
     * Delete a user subscription based on API_ID, APP_ID, TIER_ID
     *
     * @param apiId - subscriber API ID
     * @param appId - application ID used to subscribe
     * @throws SQLException - Letting the caller to handle the roll back
     */
    private void deleteSubscriptionByApiIDAndAppID(int apiId, int appId, Connection conn) throws SQLException {

        String deleteQuery = SQLConstants.REMOVE_SUBSCRIPTION_BY_APPLICATION_ID_SQL;
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(deleteQuery);
            ps.setInt(1, apiId);
            ps.setInt(2, appId);

            ps.executeUpdate();
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, null, null);
        }
    }

    /**
     * Retrieve subscription create state for APIIdentifier and applicationID
     *
     * @param identifier    - api identifier which is subscribed
     * @param applicationId - application used to subscribed
     * @param organization identifier of the organization
     * @param connection
     * @return subscription create status
     * @throws APIManagementException
     */
    private String getSubscriptionCreaeteStatus(APIIdentifier identifier, int applicationId, String organization,
                                               Connection connection) throws APIManagementException {

        String status = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        String sqlQuery = SQLConstants.GET_SUBSCRIPTION_CREATION_STATUS_SQL;
        try {
            String uuid;
            if (identifier.getUUID() != null) {
                uuid = identifier.getUUID();
            } else {
                uuid = getUUIDFromIdentifier(identifier, organization);
            }
            int apiId = getAPIID(uuid, connection);
            ps = connection.prepareStatement(sqlQuery);
            ps.setInt(1, apiId);
            ps.setInt(2, applicationId);
            rs = ps.executeQuery();

            // returns only one row
            while (rs.next()) {
                status = rs.getString("SUBS_CREATE_STATE");
            }
        } catch (SQLException e) {
            handleException("Error occurred while getting subscription entry for " +
                    "Application : " + applicationId + ", API: " + identifier, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, null, rs);
        }
        return status;
    }

    public String getUUIDFromIdentifier(APIIdentifier identifier, String organization) throws APIManagementException {

        String uuid = null;
        String sql = SQLConstants.GET_UUID_BY_IDENTIFIER_AND_ORGANIZATION_SQL;
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(sql)) {
            prepStmt.setString(1, identifier.getApiName());
            prepStmt.setString(2, identifier.getVersion());
            prepStmt.setString(3, organization);
            try (ResultSet resultSet = prepStmt.executeQuery()) {
                while (resultSet.next()) {
                    uuid = resultSet.getString(1);
                }
            }
        } catch (SQLException e) {
            handleException(
                    "Failed to get the UUID for API : " + identifier.getApiName() + '-' + identifier.getVersion(), e);
        }
        return uuid;
    }

    @Override
    public void updateSubscription(SubscribedAPI subscribedAPI) throws APIManagementException {

        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);

            //This query to update the AM_SUBSCRIPTION table
            String sqlQuery = SQLConstants.UPDATE_SUBSCRIPTION_OF_UUID_SQL;

            //Updating data to the AM_SUBSCRIPTION table
            ps = conn.prepareStatement(sqlQuery);
            ps.setString(1, subscribedAPI.getSubStatus());
            //TODO Need to find logged in user who does this update.
            ps.setString(2, null);
            ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            ps.setString(4, subscribedAPI.getUUID());
            ps.execute();

            // finally commit transaction
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error("Failed to rollback the update subscription ", e1);
                }
            }
            handleExceptionWithCode("Failed to update subscription data ", e,
                    ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_MESSAGE, "Subscription update failed"));
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, null);
        }
    }

    @Override
    public Set<String> getUnversionedLocalScopeKeysForAPI(String uuid, int tenantId)
            throws APIManagementException {

        int apiId;
        Set<String> localScopes = new HashSet<>();
        String getUnVersionedLocalScopes = SQLConstants.GET_UNVERSIONED_LOCAL_SCOPES_FOR_API_SQL;
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(getUnVersionedLocalScopes)) {
            apiId = getAPIID(uuid, connection);
            preparedStatement.setInt(1, apiId);
            preparedStatement.setInt(2, tenantId);
            preparedStatement.setInt(3, tenantId);
            preparedStatement.setInt(4, apiId);
            preparedStatement.setInt(5, tenantId);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    localScopes.add(rs.getString("SCOPE_NAME"));
                }
            }
        } catch (SQLException e) {
            handleException("Failed while getting unversioned local scopes for API with UUID:" + uuid + " tenant: "
                    + tenantId, e);
        }
        return localScopes;
    }

    @Override
    public void deleteAPI(String uuid) throws APIManagementException {

        Connection connection = null;
        PreparedStatement prepStmt = null;
        int id;
        String deleteLCEventQuery = SQLConstants.REMOVE_FROM_API_LIFECYCLE_SQL;
        String deleteAuditAPIMapping = SQLConstants.REMOVE_SECURITY_AUDIT_MAP_SQL;
        String deleteCommentQuery = SQLConstants.REMOVE_FROM_API_COMMENT_SQL;
        String deleteRatingsQuery = SQLConstants.REMOVE_FROM_API_RATING_SQL;
        String deleteSubscriptionQuery = SQLConstants.REMOVE_FROM_API_SUBSCRIPTION_SQL;
        String deleteExternalAPIStoresQuery = SQLConstants.REMOVE_FROM_EXTERNAL_STORES_SQL;
        String deleteAPIQuery = SQLConstants.REMOVE_FROM_API_SQL_BY_UUID;
        String deleteResourceScopeMappingsQuery = SQLConstants.REMOVE_RESOURCE_SCOPE_URL_MAPPING_SQL;
        String deleteURLTemplateQuery = SQLConstants.REMOVE_FROM_API_URL_MAPPINGS_SQL;
        String deleteGraphqlComplexityQuery = SQLConstants.REMOVE_FROM_GRAPHQL_COMPLEXITY_SQL;
        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            APIIdentifier identifier = ApiMgtDAO.getInstance().getAPIIdentifierFromUUID(uuid);
            id = getAPIID(uuid, connection);

            prepStmt = connection.prepareStatement(deleteAuditAPIMapping);
            prepStmt.setInt(1, id);
            prepStmt.execute();
            prepStmt.close();//If exception occurs at execute, this statement will close in finally else here

            prepStmt = connection.prepareStatement(deleteGraphqlComplexityQuery);
            prepStmt.setInt(1, id);
            prepStmt.execute();
            prepStmt.close();//If exception occurs at execute, this statement will close in finally else here

            prepStmt = connection.prepareStatement(deleteSubscriptionQuery);
            prepStmt.setInt(1, id);
            prepStmt.execute();
            prepStmt.close();//If exception occurs at execute, this statement will close in finally else here

            //Delete all comments associated with given API
            deleteAPIComments(id, uuid, connection);

            prepStmt = connection.prepareStatement(deleteRatingsQuery);
            prepStmt.setInt(1, id);
            prepStmt.execute();
            prepStmt.close();//If exception occurs at execute, this statement will close in finally else here

            prepStmt = connection.prepareStatement(deleteLCEventQuery);
            prepStmt.setInt(1, id);
            prepStmt.execute();
            prepStmt.close();//If exception occurs at execute, this statement will close in finally else here

            //Delete all external APIStore details associated with a given API
            prepStmt = connection.prepareStatement(deleteExternalAPIStoresQuery);
            prepStmt.setInt(1, id);
            prepStmt.execute();
            prepStmt.close();//If exception occurs at execute, this statement will close in finally else here

            //Delete resource scope mappings of the API
            prepStmt = connection.prepareStatement(deleteResourceScopeMappingsQuery);
            prepStmt.setInt(1, id);
            prepStmt.execute();
            prepStmt.close();//If exception occurs at execute, this statement will close in finally else here

            // Delete URL Templates (delete the resource scope mappings on delete cascade)
            prepStmt = connection.prepareStatement(deleteURLTemplateQuery);
            prepStmt.setInt(1, id);
            prepStmt.execute();

            deleteAllAPISpecificOperationPoliciesByAPIUUID(connection, uuid, null);

            prepStmt = connection.prepareStatement(deleteAPIQuery);
            prepStmt.setString(1, uuid);
            prepStmt.execute();
            prepStmt.close();//If exception occurs at execute, this statement will close in finally else here

            String curDefaultVersion = getDefaultVersion(identifier);
            String pubDefaultVersion = getPublishedDefaultVersion(identifier);
            if (identifier.getVersion().equals(curDefaultVersion)) {
                ArrayList<APIIdentifier> apiIdList = new ArrayList<APIIdentifier>() {{
                    add(identifier);
                }};
                removeAPIFromDefaultVersion(apiIdList, connection);
            } else if (identifier.getVersion().equals(pubDefaultVersion)) {
                setPublishedDefVersion(identifier, connection, null);
            }

            connection.commit();
        } catch (SQLException e) {
            handleException("Error while removing the API with UUID: " + uuid + " from the database", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, null);
        }
    }

    private void deleteAPIComments(int apiId, String uuid, Connection connection) throws APIManagementException {
        try {
            connection.setAutoCommit(false);
            String deleteChildComments = SQLConstants.DELETE_API_CHILD_COMMENTS;
            String deleteParentComments = SQLConstants.DELETE_API_PARENT_COMMENTS;
            try (PreparedStatement childCommentPreparedStmt = connection.prepareStatement(deleteChildComments);
                 PreparedStatement parentCommentPreparedStmt = connection.prepareStatement(deleteParentComments)) {
                childCommentPreparedStmt.setInt(1, apiId);
                childCommentPreparedStmt.execute();

                parentCommentPreparedStmt.setInt(1, apiId);
                parentCommentPreparedStmt.execute();
            }
        } catch (SQLException e) {
            handleException("Error while deleting comments for API " + uuid, e);
        }
    }

    @Override
    public String getLastPublishedAPIVersionFromAPIStore(APIIdentifier apiIdentifier, String storeName)
            throws APIManagementException {

        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection conn = null;
        String version = null;
        try {
            conn = APIMgtDBUtil.getConnection();
            String sqlQuery = SQLConstants.GET_LAST_PUBLISHED_API_VERSION_SQL;
            ps = conn.prepareStatement(sqlQuery);
            ps.setString(1, apiIdentifier.getProviderName());
            ps.setString(2, apiIdentifier.getApiName());
            ps.setString(3, storeName);
            rs = ps.executeQuery();
            while (rs.next()) {
                version = rs.getString("API_VERSION");
            }
        } catch (SQLException e) {
            handleException("Error while getting External APIStore details from the database for  the API : " +
                    apiIdentifier.getApiName() + '-' + apiIdentifier.getVersion(), e);

        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
        return version;
    }

    @Override
    public boolean deleteExternalAPIStoresDetails(String uuid, Set<APIStore> apiStoreSet)
            throws APIManagementException {

        Connection conn = null;
        PreparedStatement ps = null;
        boolean state = false;
        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);

            String sqlQuery = SQLConstants.REMOVE_EXTERNAL_API_STORE_SQL;

            //Get API Id
            int apiIdentifier;
            apiIdentifier = getAPIID(uuid, conn);
            if (apiIdentifier == -1) {
                String msg = "Could not load API record for API with UUID: " + uuid;
                log.error(msg);
            }
            ps = conn.prepareStatement(sqlQuery);
            for (Object storeObject : apiStoreSet) {
                APIStore store = (APIStore) storeObject;
                ps.setInt(1, apiIdentifier);
                ps.setString(2, store.getName());
                ps.setString(3, store.getType());
                ps.addBatch();
            }
            ps.executeBatch();

            conn.commit();
            state = true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error("Failed to rollback deleting external apistore details ", e1);
                }
            }
            log.error("Failed to delete external apistore details", e);
            state = false;
        } catch (APIManagementException e) {
            log.error("Failed to delete external apistore details", e);
            state = false;
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, null);
        }
        return state;
    }

    @Override
    public void updateExternalAPIStoresDetails(String uuid, Set<APIStore> apiStoreSet)
            throws APIManagementException {

        Connection conn = null;
        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);
            updateExternalAPIStoresDetails(uuid, apiStoreSet, conn);
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error("Failed to rollback updating external apistore details ", e1);
                }
            }
            log.error("Failed to update external apistore details", e);
        } catch (APIManagementException e) {
            log.error("Failed to updating external apistore details", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(null, conn, null);
        }
    }

    /**
     * Update external APIStores details to which APIs published
     *
     * @param uuid API uuid
     * @throws APIManagementException if failed to add Application
     */
    private void updateExternalAPIStoresDetails(String uuid, Set<APIStore> apiStoreSet, Connection conn)
            throws APIManagementException, SQLException {

        PreparedStatement ps = null;

        try {
            conn.setAutoCommit(false);
            //This query to add external APIStores to database table
            String sqlQuery = SQLConstants.UPDATE_EXTERNAL_API_STORE_SQL;

            ps = conn.prepareStatement(sqlQuery);
            //Get API Id
            int apiId;
            apiId = getAPIID(uuid, conn);
            if (apiId == -1) {
                String msg = "Could not load API record for API with UUID: " + uuid;
                log.error(msg);
            }

            for (Object storeObject : apiStoreSet) {
                APIStore store = (APIStore) storeObject;
                ps.setString(1, store.getEndpoint());
                ps.setString(2, store.getType());
                ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
                ps.setInt(4, apiId);
                ps.setString(5, store.getName());
                ps.addBatch();
            }

            ps.executeBatch();
            ps.clearBatch();

            conn.commit();
        } catch (SQLException e) {
            log.error("Error while updating External APIStore details to the database for API : ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, null, null);
        }
    }

    @Override
    public boolean addExternalAPIStoresDetails(String uuid, Set<APIStore> apiStoreSet)
            throws APIManagementException {

        Connection conn = null;
        PreparedStatement ps = null;
        boolean state = false;
        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);

            //This query to add external APIStores to database table
            String sqlQuery = SQLConstants.ADD_EXTERNAL_API_STORE_SQL;

            //Get API Id
            int apiIdentifier;
            apiIdentifier = getAPIID(uuid, conn);
            if (apiIdentifier == -1) {
                String msg = "Could not load API record for API with uuid: " + uuid;
                log.error(msg);
            }
            ps = conn.prepareStatement(sqlQuery);
            for (Object storeObject : apiStoreSet) {
                APIStore store = (APIStore) storeObject;
                ps.setInt(1, apiIdentifier);
                ps.setString(2, store.getName());
                ps.setString(3, store.getDisplayName());
                ps.setString(4, store.getEndpoint());
                ps.setString(5, store.getType());
                ps.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
                ps.addBatch();
            }

            ps.executeBatch();
            conn.commit();
            state = true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error("Failed to rollback storing external apistore details ", e1);
                }
            }
            log.error("Failed to store external apistore details", e);
            state = false;
        } catch (APIManagementException e) {
            log.error("Failed to store external apistore details", e);
            state = false;
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, null);
        }
        return state;
    }

    @Override
    public Set<APIStore> getExternalAPIStoresDetails(String uuid) throws APIManagementException {

        Connection conn = null;
        Set<APIStore> storesSet = new HashSet<APIStore>();
        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);

            storesSet = getExternalAPIStoresDetails(uuid, conn);

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error("Failed to rollback getting external apistore details ", e1);
                }
            }
            log.error("Failed to get external apistore details", e);
        } catch (APIManagementException e) {
            log.error("Failed to get external apistore details", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(null, conn, null);
        }
        return storesSet;
    }

    /**
     * Get external APIStores details which are stored in database
     *
     * @param uuid API uuid
     * @throws APIManagementException if failed to get external APIStores
     */
    private Set<APIStore> getExternalAPIStoresDetails(String uuid, Connection conn)
            throws APIManagementException, SQLException {

        PreparedStatement ps = null;
        ResultSet rs = null;
        Set<APIStore> storesSet = new HashSet<APIStore>();
        try {
            conn = APIMgtDBUtil.getConnection();
            //This query to add external APIStores to database table
            String sqlQuery = SQLConstants.GET_EXTERNAL_API_STORE_DETAILS_SQL;

            ps = conn.prepareStatement(sqlQuery);
            int apiId;
            apiId = getAPIID(uuid, conn);
            if (apiId == -1) {
                String msg = "Could not load API record for API with UUID: " + uuid;
                log.error(msg);
                throw new APIManagementException(msg);
            }
            ps.setInt(1, apiId);
            rs = ps.executeQuery();
            while (rs.next()) {
                APIStore store = new APIStore();
                store.setName(rs.getString("STORE_ID"));
                store.setDisplayName(rs.getString("STORE_DISPLAY_NAME"));
                store.setEndpoint(rs.getString("STORE_ENDPOINT"));
                store.setType(rs.getString("STORE_TYPE"));
                store.setLastUpdated(rs.getTimestamp("LAST_UPDATED_TIME"));
                store.setPublished(true);
                storesSet.add(store);
            }
        } catch (SQLException e) {
            handleException(
                    "Error while getting External APIStore details from the database for the API with UUID: " + uuid,
                    e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
        return storesSet;
    }

    @Override
    public int getAPIProductId(APIProductIdentifier identifier) throws APIManagementException {

        Connection conn = null;
        String queryGetProductId = SQLConstants.GET_PRODUCT_ID;
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        int productId = -1;

        try {
            conn = APIMgtDBUtil.getConnection();
            preparedStatement = conn.prepareStatement(queryGetProductId);
            preparedStatement.setString(1, identifier.getName());
            preparedStatement.setString(2, APIUtil.replaceEmailDomainBack(identifier.getProviderName()));
            preparedStatement.setString(3, APIConstants.API_PRODUCT_VERSION); //versioning is not supported atm

            rs = preparedStatement.executeQuery();

            if (rs.next()) {
                productId = rs.getInt("API_ID");
            }

            if (productId == -1) {
                String msg = "Unable to find the API Product : " + productId + " in the database";
                log.error(msg);
                throw new APIManagementException(msg);
            }
        } catch (SQLException e) {
            handleExceptionWithCode("Error while retrieving api product id for product " + identifier.getName() + " by " +
                    APIUtil.replaceEmailDomainBack(identifier.getProviderName()), e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        } finally {
            APIMgtDBUtil.closeAllConnections(preparedStatement, conn, rs);
        }
        return productId;
    }

    @Override
    public String getGatewayVendorByAPIUUID(String apiId) throws APIManagementException {
        String gatewayVendor = null;
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(SQLConstants.GET_GATEWAY_VENDOR_BY_API_ID)) {
            ResultSet result = null;
            try {
                connection.setAutoCommit(false);
                ps.setString(1, apiId);
                result = ps.executeQuery();

                while (result.next()) {
                    gatewayVendor = result.getString("GATEWAY_VENDOR");
                }
                connection.commit();
            } catch (SQLException e) {
                APIMgtDBUtil.rollbackConnection(connection, "Failed to rollback while fetching gateway vendor" +
                        " of the API", e);
            }
        } catch (SQLException e) {
            handleExceptionWithCode("Error occurred while fetching gateway vendor of the API with ID " + apiId, e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        gatewayVendor = APIUtil.handleGatewayVendorRetrieval(gatewayVendor);
        return gatewayVendor;
    }

    @Override
    public List<API> getAllAPIVersions(String apiName, String apiProvider) throws APIManagementException {

        List<API> apiVersions = new ArrayList<API>();

        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQLConstants.GET_API_VERSIONS_UUID)) {
            statement.setString(1, APIUtil.replaceEmailDomainBack(apiProvider));
            statement.setString(2, apiName);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String version = resultSet.getString("API_VERSION");
                String status = resultSet.getString("STATUS");
                String versionTimestamp = resultSet.getString("VERSION_COMPARABLE");
                String context = resultSet.getString("CONTEXT");
                String contextTemplate = resultSet.getString("CONTEXT_TEMPLATE");

                String uuid = resultSet.getString("API_UUID");
                if (APIConstants.API_PRODUCT.equals(resultSet.getString("API_TYPE"))) {
                    // skip api products
                    continue;
                }
                API api = new API(new APIIdentifier(apiProvider, apiName,
                        version, uuid));
                api.setUuid(uuid);
                api.setStatus(status);
                api.setVersionTimestamp(versionTimestamp);
                api.setContext(context);
                api.setContextTemplate(contextTemplate);
                apiVersions.add(api);
            }
        } catch (SQLException e) {
            String errorMessage = "Error while retrieving versions for api " + apiName + " for the provider " + apiProvider;
            handleExceptionWithCode(errorMessage, e,
                    ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_MESSAGE, errorMessage));
        }
        return apiVersions;
    }

    @Override
    public Map<String, URITemplate> getURITemplatesForAPI(API api) throws APIManagementException {

        Map<String, URITemplate> templatesMap = new HashMap<String, URITemplate>();
        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        try {
            connection = APIMgtDBUtil.getConnection();
            String query = SQLConstants.GET_URL_TEMPLATES_FOR_API_WITH_UUID;

            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, api.getUuid());
            rs = prepStmt.executeQuery();
            while (rs.next()) {
                URITemplate template = new URITemplate();
                String urlPattern = rs.getString("URL_PATTERN");
                String httpMethod = rs.getString("HTTP_METHOD");

                template.setHTTPVerb(httpMethod);
                template.setResourceURI(urlPattern);
                template.setId(rs.getInt("URL_MAPPING_ID"));

                //TODO populate others if needed

                templatesMap.put(httpMethod + ":" + urlPattern, template);
            }

        } catch (SQLException e) {
            handleExceptionWithCode("Error while obtaining details of the URI Template for api " + api.getId(), e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
        }

        return templatesMap;
    }

    @Override
    public void addAPIProduct(APIProduct apiProduct, String organization) throws APIManagementException {

        Connection connection = null;
        PreparedStatement prepStmtAddAPIProduct = null;
        PreparedStatement prepStmtAddScopeEntry = null;

        if (log.isDebugEnabled()) {
            log.debug("addAPIProduct() : " + apiProduct.toString() + " for organization " + organization);
        }
        APIProductIdentifier identifier = apiProduct.getId();
        ResultSet rs = null;
        int productId = 0;
        int scopeId = 0;
        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            String queryAddAPIProduct = SQLConstants.ADD_API_PRODUCT;
            prepStmtAddAPIProduct = connection.prepareStatement(queryAddAPIProduct, new String[]{"api_id"});
            prepStmtAddAPIProduct.setString(1, APIUtil.replaceEmailDomainBack(identifier.getProviderName()));
            prepStmtAddAPIProduct.setString(2, identifier.getName());
            prepStmtAddAPIProduct.setString(3, identifier.getVersion());
            prepStmtAddAPIProduct.setString(4, apiProduct.getContext());
            prepStmtAddAPIProduct.setString(5, apiProduct.getProductLevelPolicy());
            prepStmtAddAPIProduct.setString(6, APIUtil.replaceEmailDomainBack(identifier.getProviderName()));
            prepStmtAddAPIProduct.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
            prepStmtAddAPIProduct.setString(8, APIConstants.API_PRODUCT);
            prepStmtAddAPIProduct.setString(9, apiProduct.getUuid());
            prepStmtAddAPIProduct.setString(10, apiProduct.getState());
            prepStmtAddAPIProduct.setString(11, organization);
            prepStmtAddAPIProduct.setString(12, apiProduct.getGatewayVendor());
            prepStmtAddAPIProduct.setString(13, apiProduct.getVersionTimestamp());
            prepStmtAddAPIProduct.execute();

            rs = prepStmtAddAPIProduct.getGeneratedKeys();

            if (rs.next()) {
                productId = rs.getInt(1);
            }
            //breaks the flow if product is not added to the db correctly
            if (productId == 0) {
                throw new APIManagementException("Error while adding API product " + apiProduct.getUuid());
            }

            addAPIProductResourceMappings(apiProduct.getProductResources(), apiProduct.getOrganization(), connection);
            String tenantUserName = MultitenantUtils
                    .getTenantAwareUsername(APIUtil.replaceEmailDomainBack(identifier.getProviderName()));
            int tenantId = APIUtil.getTenantId(APIUtil.replaceEmailDomainBack(identifier.getProviderName()));
            recordAPILifeCycleEvent(productId, null, APIStatus.CREATED.toString(), tenantUserName, tenantId,
                    connection);
            connection.commit();
        } catch (SQLException e) {
            handleException("Error while adding API product " + identifier.getName() + " of provider "
                    + APIUtil.replaceEmailDomainBack(identifier.getProviderName()), e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmtAddAPIProduct, null, null);
            APIMgtDBUtil.closeAllConnections(prepStmtAddScopeEntry, connection, null);
        }
    }

    /**
     * Add api product url mappings to DB
     * - url templeates to product mappings (resource bundling) - AM_API_PRODUCT_MAPPING
     *
     * @param productResources
     * @param organization
     * @param connection
     * @throws APIManagementException
     */
    public void addAPIProductResourceMappings(List<APIProductResource> productResources, String organization,
                                              Connection connection) throws APIManagementException {
        String addProductResourceMappingSql = SQLConstants.ADD_PRODUCT_RESOURCE_MAPPING_SQL;

        boolean isNewConnection = false;
        try {
            if (connection == null) {
                connection = APIMgtDBUtil.getConnection();
                isNewConnection = true;
            }

            Set<String> usedClonedPolicies = new HashSet<>();
            Map<String, String> clonedPoliciesMap = new HashMap<>();

            //add the duplicate resources in each API in the API product.
            for (APIProductResource apiProductResource : productResources) {
                APIProductIdentifier productIdentifier = apiProductResource.getProductIdentifier();
                String uuid;
                if (productIdentifier.getUUID() != null) {
                    uuid = productIdentifier.getUUID();
                } else {
                    uuid = getUUIDFromIdentifier(productIdentifier, organization, connection);
                }
                int productId = getAPIID(uuid, connection);
                int tenantId = APIUtil.getTenantId(APIUtil.replaceEmailDomainBack(productIdentifier.getProviderName()));
                String tenantDomain = APIUtil.getTenantDomainFromTenantId(tenantId);
                URITemplate uriTemplateOriginal = apiProductResource.getUriTemplate();
                int urlMappingId = uriTemplateOriginal.getId();
                // Adding to AM_API_URL_MAPPING table
                PreparedStatement getURLMappingsStatement = connection
                        .prepareStatement(SQLConstants.APIRevisionSqlConstants.
                                GET_URL_MAPPINGS_WITH_SCOPE_BY_URL_MAPPING_ID);
                getURLMappingsStatement.setInt(1, urlMappingId);
                List<URITemplate> urlMappingList = new ArrayList<>();
                try (ResultSet rs = getURLMappingsStatement.executeQuery()) {
                    while (rs.next()) {
                        URITemplate uriTemplate = new URITemplate();
                        uriTemplate.setHTTPVerb(rs.getString("HTTP_METHOD"));
                        uriTemplate.setAuthType(rs.getString("AUTH_SCHEME"));
                        uriTemplate.setUriTemplate(rs.getString("URL_PATTERN"));
                        uriTemplate.setThrottlingTier(rs.getString("THROTTLING_TIER"));
                        String script = null;
                        InputStream mediationScriptBlob = rs.getBinaryStream("MEDIATION_SCRIPT");
                        if (mediationScriptBlob != null) {
                            script = APIMgtDBUtil.getStringFromInputStream(mediationScriptBlob);
                        }
                        uriTemplate.setMediationScript(script);
                        if (!StringUtils.isEmpty(rs.getString("SCOPE_NAME"))) {
                            Scope scope = new Scope();
                            scope.setKey(rs.getString("SCOPE_NAME"));
                            uriTemplate.setScope(scope);
                        }
                        if (rs.getInt("API_ID") != 0) {
                            // Adding api id to uri template id just to store value
                            uriTemplate.setId(rs.getInt("API_ID"));
                        }
                        List<OperationPolicy> operationPolicies = getOperationPoliciesOfURITemplate(urlMappingId);
                        uriTemplate.setOperationPolicies(operationPolicies);
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

                PreparedStatement insertURLMappingsStatement = connection
                        .prepareStatement(SQLConstants.APIRevisionSqlConstants.INSERT_URL_MAPPINGS);
                for (URITemplate urlMapping : uriTemplateMap.values()) {
                    insertURLMappingsStatement.setInt(1, urlMapping.getId());
                    insertURLMappingsStatement.setString(2, urlMapping.getHTTPVerb());
                    insertURLMappingsStatement.setString(3, urlMapping.getAuthType());
                    insertURLMappingsStatement.setString(4, urlMapping.getUriTemplate());
                    insertURLMappingsStatement.setString(5, urlMapping.getThrottlingTier());
                    insertURLMappingsStatement.setString(6, String.valueOf(productId));
                    insertURLMappingsStatement.addBatch();
                }
                insertURLMappingsStatement.executeBatch();

                // Add to AM_API_RESOURCE_SCOPE_MAPPING table and to AM_API_PRODUCT_MAPPING
                PreparedStatement getRevisionedURLMappingsStatement = connection
                        .prepareStatement(SQLConstants.APIRevisionSqlConstants.GET_URL_MAPPINGS_ID);
                PreparedStatement insertScopeResourceMappingStatement = connection
                        .prepareStatement(SQLConstants.APIRevisionSqlConstants.INSERT_SCOPE_RESOURCE_MAPPING);
                PreparedStatement insertProductResourceMappingStatement = connection
                        .prepareStatement(addProductResourceMappingSql);
                String dbProductName = connection.getMetaData().getDatabaseProductName();
                PreparedStatement insertOperationPolicyMappingStatement = connection.prepareStatement(
                        SQLConstants.OperationPolicyConstants.ADD_API_OPERATION_POLICY_MAPPING,
                        new String[] { "OPERATION_POLICY_MAPPING_ID".toLowerCase() });
                for (URITemplate urlMapping : uriTemplateMap.values()) {
                    getRevisionedURLMappingsStatement.setInt(1, urlMapping.getId());
                    getRevisionedURLMappingsStatement.setString(2, urlMapping.getHTTPVerb());
                    getRevisionedURLMappingsStatement.setString(3, urlMapping.getAuthType());
                    getRevisionedURLMappingsStatement.setString(4, urlMapping.getUriTemplate());
                    getRevisionedURLMappingsStatement.setString(5, urlMapping.getThrottlingTier());
                    getRevisionedURLMappingsStatement.setString(6, String.valueOf(productId));
                    if (!urlMapping.getScopes().isEmpty()) {
                        try (ResultSet rs = getRevisionedURLMappingsStatement.executeQuery()) {
                            while (rs.next()) {
                                for (Scope scope : urlMapping.getScopes()) {
                                    insertScopeResourceMappingStatement.setString(1, scope.getKey());
                                    insertScopeResourceMappingStatement.setInt(2, rs.getInt(1));
                                    insertScopeResourceMappingStatement.setInt(3, tenantId);
                                    insertScopeResourceMappingStatement.addBatch();
                                }
                            }
                        }
                    }
                    try (ResultSet rs = getRevisionedURLMappingsStatement.executeQuery()) {
                        while (rs.next()) {
                            insertProductResourceMappingStatement.setInt(1, productId);
                            insertProductResourceMappingStatement.setInt(2, rs.getInt(1));
                            insertProductResourceMappingStatement.setString(3, "Current API");
                            insertProductResourceMappingStatement.addBatch();
                        }
                    }
                    try (ResultSet rs = getRevisionedURLMappingsStatement.executeQuery()) {
                        while (rs.next()) {
                            for (OperationPolicy policy : urlMapping.getOperationPolicies()) {
                                if (!clonedPoliciesMap.keySet().contains(policy.getPolicyId())) {
                                    OperationPolicyData existingPolicy =
                                            getAPISpecificOperationPolicyByPolicyID(policy.getPolicyId(), uuid,
                                                    tenantDomain, false);
                                    String clonedPolicyId = policy.getPolicyId();
                                    if (existingPolicy != null) {
                                        if (existingPolicy.isClonedPolicy()) {
                                            usedClonedPolicies.add(clonedPolicyId);
                                        }
                                    } else {
                                        // Even though the policy ID attached is not in the API specific policy list for the product uuid,
                                        // it can be from the dependent API and we need to verify that it has not been previously cloned
                                        // for the product before cloning again.
                                        clonedPolicyId = getClonedPolicyIdForCommonPolicyId(connection,
                                                policy.getPolicyId(), uuid);
                                        if (clonedPolicyId == null) {
                                            clonedPolicyId = cloneOperationPolicy(connection, policy.getPolicyId(),
                                                    uuid, null);
                                        }
                                        usedClonedPolicies.add(clonedPolicyId);
                                        //usedClonedPolicies set will not contain used API specific policies that are not cloned.
                                        //TODO: discuss whether we need to clone API specific policies as well
                                    }

                                    // Updated policies map will record the updated policy ID for the used policy ID.
                                    // If the policy has been cloned to the API specific policy list, we need to use the
                                    // updated policy Id.
                                    clonedPoliciesMap.put(policy.getPolicyId(), clonedPolicyId);
                                }

                                Gson gson = new Gson();
                                String paramJSON = gson.toJson(policy.getParameters());

                                insertOperationPolicyMappingStatement.setInt(1, rs.getInt(1));
                                insertOperationPolicyMappingStatement
                                        .setString(2, clonedPoliciesMap.get(policy.getPolicyId()));
                                insertOperationPolicyMappingStatement.setString(3, policy.getDirection());
                                insertOperationPolicyMappingStatement.setString(4, paramJSON);
                                insertOperationPolicyMappingStatement.setInt(5, policy.getOrder());
                                insertOperationPolicyMappingStatement.executeUpdate();
                            }
                        }
                    }
                }
                insertScopeResourceMappingStatement.executeBatch();
                insertProductResourceMappingStatement.executeBatch();
            }
        } catch (SQLException e) {
            handleExceptionWithCode("Error while adding API product Resources", e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        } finally {
            if (isNewConnection) {
                APIMgtDBUtil.closeAllConnections(null, connection, null);
            }
        }
    }

    /**
     * Get operation polycies attached to the resource identified by the url mapping ID
     *
     * @param urlMappingId URL Mapping ID of the resource
     * @return
     * @throws SQLException
     * @throws APIManagementException
     */
    private List<OperationPolicy> getOperationPoliciesOfURITemplate(int urlMappingId)
            throws SQLException, APIManagementException {

        List<OperationPolicy> operationPolicies = new ArrayList<>();
        try (Connection conn = APIMgtDBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     SQLConstants.OperationPolicyConstants.GET_OPERATION_POLICIES_BY_URI_TEMPLATE_ID)) {
            ps.setInt(1, urlMappingId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OperationPolicy policy = populateOperationPolicyWithRS(rs);
                    operationPolicies.add(policy);
                }
            }
        }
        return operationPolicies;
    }

    /**
     * Get API Product UUID by the API Product Identifier and organization.
     *
     * @param identifier API Product Identifier
     * @param organization
     * @param connection
     * @return String UUID
     * @throws APIManagementException if an error occurs
     */
    private String getUUIDFromIdentifier(APIProductIdentifier identifier, String organization, Connection connection)
            throws APIManagementException {
        boolean isNewConnection = false;
        String uuid = null;
        PreparedStatement prepStmt = null;
        String sql = SQLConstants.GET_UUID_BY_IDENTIFIER_AND_ORGANIZATION_SQL;
        try {
            if (connection == null) {
                connection = APIMgtDBUtil.getConnection();
                isNewConnection = true;
            }
            prepStmt = connection.prepareStatement(sql);
            prepStmt.setString(1, identifier.getName());
            prepStmt.setString(2, identifier.getVersion());
            prepStmt.setString(3, organization);
            try (ResultSet resultSet = prepStmt.executeQuery()) {
                while (resultSet.next()) {
                    uuid = resultSet.getString(1);
                }
            }
        } catch (SQLException e) {
            handleExceptionWithCode("Failed to retrieve the UUID for the API Product : " + identifier.getName() + '-'
                    + identifier.getVersion(), e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, null, null);
            if (isNewConnection) {
                APIMgtDBUtil.closeAllConnections(null, connection, null);
            }
        }
        return uuid;
    }

    @Override
    public void deleteAPIProduct(APIProductIdentifier productIdentifier) throws APIManagementException {

        String deleteQuery = SQLConstants.DELETE_API_PRODUCT_SQL;
        String deleteRatingsQuery = SQLConstants.REMOVE_FROM_API_RATING_SQL;
        String urlMappingQuery = SQLConstants.REMOVE_FROM_URI_TEMPLATES__FOR_PRODUCTS_SQL;
        PreparedStatement ps = null;
        Connection connection = null;
        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            //  delete product ratings
            int id = getAPIProductId(productIdentifier);
            ps = connection.prepareStatement(deleteRatingsQuery);
            ps.setInt(1, id);
            ps.execute();
            ps.close();//If exception occurs at execute, this statement will close in finally else here
            //delete product
            ps = connection.prepareStatement(deleteQuery);
            ps.setString(1, APIUtil.replaceEmailDomainBack(productIdentifier.getProviderName()));
            ps.setString(2, productIdentifier.getName());
            ps.setString(3, productIdentifier.getVersion());
            ps.executeUpdate();
            ps.close();

            ps = connection.prepareStatement(urlMappingQuery);
            ps.setString(1, Integer.toString(id));
            ps.execute();
            ps.close();

            deleteAllAPISpecificOperationPoliciesByAPIUUID(connection, productIdentifier.getUUID(), null);

            connection.commit();
        } catch (SQLException e) {
            handleException("Error while deleting api product " + productIdentifier, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, connection, null);
        }
    }

    @Override
    public String getUUIDFromIdentifier(APIProductIdentifier identifier, String organization)
            throws APIManagementException {
        return getUUIDFromIdentifier(identifier, organization, null);
    }

    @Override
    public void updateAPIProduct(APIProduct product, String username) throws APIManagementException {

        Connection conn = null;
        PreparedStatement ps = null;
        if (log.isDebugEnabled()) {
            log.debug("updateAPIProduct() : product- " + product.toString());
        }
        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);

            String query = SQLConstants.UPDATE_PRODUCT_SQL;

            ps = conn.prepareStatement(query);

            ps.setString(1, product.getProductLevelPolicy());
            ps.setString(2, username);
            ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            ps.setString(4, product.getGatewayVendor());
            APIProductIdentifier identifier = product.getId();
            ps.setString(5, identifier.getName());
            ps.setString(6, APIUtil.replaceEmailDomainBack(identifier.getProviderName()));
            ps.setString(7, identifier.getVersion());
            ps.executeUpdate();

            int productId = getAPIID(product.getUuid(), conn);
            updateAPIProductResourceMappings(product, productId, conn);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error("Error while rolling back the failed operation", e1);
                }
            }
            handleException("Error in updating API Product: " + e.getMessage(), e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, null);
        }
    }

    /**
     * Update Product scope and resource mappings
     *
     * @param apiProduct
     * @param productId
     * @param connection
     * @throws APIManagementException
     */
    private void updateAPIProductResourceMappings(APIProduct apiProduct, int productId, Connection connection)
            throws APIManagementException {

        PreparedStatement removeURLMappingsStatement = null;
        try {
            // Retrieve Product Resources
            PreparedStatement getProductMappingsStatement = connection.prepareStatement(SQLConstants.
                    APIRevisionSqlConstants.GET_CUURENT_API_PRODUCT_RESOURCES);
            getProductMappingsStatement.setInt(1, productId);
            List<Integer> urlMappingIds = new ArrayList<>();
            try (ResultSet rs = getProductMappingsStatement.executeQuery()) {
                while (rs.next()) {
                    urlMappingIds.add(rs.getInt(1));
                }
            }
            // Removing related revision entries from AM_API_URL_MAPPING table
            // This will cascade remove entries from AM_API_RESOURCE_SCOPE_MAPPING and AM_API_PRODUCT_MAPPING tables
            removeURLMappingsStatement = connection.prepareStatement(SQLConstants
                    .APIRevisionSqlConstants.REMOVE_PRODUCT_ENTRIES_IN_AM_API_URL_MAPPING_BY_URL_MAPPING_ID);
            for (int id : urlMappingIds) {
                removeURLMappingsStatement.setInt(1, id);
                removeURLMappingsStatement.addBatch();
            }
            removeURLMappingsStatement.executeBatch();
            //Add new resources
            addAPIProductResourceMappings(apiProduct.getProductResources(), apiProduct.getOrganization(), connection);
        } catch (SQLException e) {
            handleException("Error while updating API-Product Resources.", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(removeURLMappingsStatement, null, null);
        }
    }

    @Override
    public List<ResourcePath> getResourcePathsOfAPI(APIIdentifier apiId) throws APIManagementException {

        List<ResourcePath> resourcePathList = new ArrayList<ResourcePath>();

        try (Connection conn = APIMgtDBUtil.getConnection()) {
            String sql = SQLConstants.GET_URL_TEMPLATES_FOR_API;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, apiId.getApiName());
                ps.setString(2, apiId.getVersion());
                ps.setString(3, APIUtil.replaceEmailDomainBack(apiId.getProviderName()));

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ResourcePath resourcePath = new ResourcePath();
                        resourcePath.setId(rs.getInt("URL_MAPPING_ID"));
                        resourcePath.setResourcePath(rs.getString("URL_PATTERN"));
                        resourcePath.setHttpVerb(rs.getString("HTTP_METHOD"));
                        resourcePathList.add(resourcePath);
                    }
                }
            }
        } catch (SQLException e) {
            handleExceptionWithCode("Error while obtaining Resource Paths of api " + apiId, e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        return resourcePathList;
    }

    @Override
    public String getUUIDFromIdentifier(String provider, String apiName, String version, String organization)
            throws APIManagementException {

        String uuid = null;
        String sql = SQLConstants.GET_UUID_BY_IDENTIFIER_AND_ORGANIZATION_SQL;
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(sql)) {
            prepStmt.setString(1, apiName);
            prepStmt.setString(2, version);
            prepStmt.setString(3, organization);
            try (ResultSet resultSet = prepStmt.executeQuery()) {
                while (resultSet.next()) {
                    uuid = resultSet.getString(1);
                }
            }
        } catch (SQLException e) {
            handleExceptionWithCode("Failed to get the UUID for API : ", e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        return uuid;
    }

    @Override
    public String getAPILevelTier(String apiUUID, String revisionUUID) throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            return getAPILevelTier(connection, apiUUID, revisionUUID);
        } catch (SQLException e) {
            handleExceptionWithCode("Failed to retrieve Connection", e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        return null;
    }

    @Override
    public String getAPIStatusFromAPIUUID(String uuid) throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            try (PreparedStatement preparedStatement =
                         connection.prepareStatement(SQLConstants.RETRIEVE_API_STATUS_FROM_UUID)) {
                preparedStatement.setString(1, uuid);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getString("STATUS");
                    }
                }
            }
        } catch (SQLException e) {
            throw new APIManagementException("Error while retrieving apimgt connection", e,
                    ExceptionCodes.INTERNAL_ERROR);
        }
        return null;
    }

    @Override
    public Map<Integer, URITemplate> getURITemplatesOfAPIWithProductMapping(String uuid) throws APIManagementException {

        Map<Integer, URITemplate> uriTemplates = new LinkedHashMap<>();
        Map<Integer, Set<String>> scopeToURITemplateId = new HashMap<>();
        try (Connection conn = APIMgtDBUtil.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(SQLConstants.GET_URL_TEMPLATES_OF_API_WITH_PRODUCT_MAPPINGS_SQL)) {
            ps.setString(1, uuid);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Integer uriTemplateId = rs.getInt("URL_MAPPING_ID");
                    String scopeName = rs.getString("SCOPE_NAME");

                    if (scopeToURITemplateId.containsKey(uriTemplateId) && !StringUtils.isEmpty(scopeName)
                            && !scopeToURITemplateId.get(uriTemplateId).contains(scopeName)
                            && uriTemplates.containsKey(uriTemplateId)) {
                        Scope scope = new Scope();
                        scope.setKey(scopeName);
                        scopeToURITemplateId.get(uriTemplateId).add(scopeName);
                        uriTemplates.get(uriTemplateId).setScopes(scope);
                        continue;
                    }
                    String urlPattern = rs.getString("URL_PATTERN");
                    String verb = rs.getString("HTTP_METHOD");

                    URITemplate uriTemplate = new URITemplate();
                    uriTemplate.setUriTemplate(urlPattern);
                    uriTemplate.setHTTPVerb(verb);
                    uriTemplate.setHttpVerbs(verb);
                    String authType = rs.getString("AUTH_SCHEME");
                    String throttlingTier = rs.getString("THROTTLING_TIER");
                    if (StringUtils.isNotEmpty(scopeName)) {
                        Scope scope = new Scope();
                        scope.setKey(scopeName);
                        uriTemplate.setScope(scope);
                        uriTemplate.setScopes(scope);
                        Set<String> templateScopes = new HashSet<>();
                        templateScopes.add(scopeName);
                        scopeToURITemplateId.put(uriTemplateId, templateScopes);
                    }
                    uriTemplate.setAuthType(authType);
                    uriTemplate.setAuthTypes(authType);
                    uriTemplate.setThrottlingTier(throttlingTier);
                    uriTemplate.setThrottlingTiers(throttlingTier);

                    InputStream mediationScriptBlob = rs.getBinaryStream("MEDIATION_SCRIPT");
                    if (mediationScriptBlob != null) {
                        String script = APIMgtDBUtil.getStringFromInputStream(mediationScriptBlob);
                        uriTemplate.setMediationScript(script);
                        uriTemplate.setMediationScripts(verb, script);
                    }

                    uriTemplates.put(uriTemplateId, uriTemplate);
                }
            }

            setAssociatedAPIProductsURLMappings(uuid, uriTemplates);
        } catch (SQLException e) {
            handleExceptionWithCode("Failed to get URI Templates of API with UUID " + uuid, e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        return uriTemplates;
    }

    private void setAssociatedAPIProductsURLMappings(String uuid, Map<Integer, URITemplate> uriTemplates)
            throws SQLException {

        try (Connection conn = APIMgtDBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQLConstants.GET_ASSOCIATED_API_PRODUCT_URL_TEMPLATES_SQL)) {
            ps.setString(1, uuid);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String productName = rs.getString("API_NAME");
                    String productVersion = rs.getString("API_VERSION");
                    String productProvider = rs.getString("API_PROVIDER");
                    int uriTemplateId = rs.getInt("URL_MAPPING_ID");

                    URITemplate uriTemplate = uriTemplates.get(uriTemplateId);
                    if (uriTemplate != null) {
                        APIProductIdentifier productIdentifier = new APIProductIdentifier
                                (productProvider, productName, productVersion);
                        uriTemplate.addUsedByProduct(productIdentifier);
                    }
                }
            }
        }
    }

    @Override
    public void addAPIProductRevision(APIRevision apiRevision) throws APIManagementException {
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

                // Retrieve API Product ID
                APIProductIdentifier apiProductIdentifier =
                        APIUtil.getAPIProductIdentifierFromUUID(apiRevision.getApiUUID());
                int apiId = getAPIID(apiRevision.getApiUUID(), connection);
                int tenantId =
                        APIUtil.getTenantId(APIUtil.replaceEmailDomainBack(apiProductIdentifier.getProviderName()));
                String tenantDomain = APIUtil.getTenantDomainFromTenantId(tenantId);

                // Adding to AM_API_URL_MAPPING table
                PreparedStatement getURLMappingsStatement = connection
                        .prepareStatement(SQLConstants.APIRevisionSqlConstants.
                                GET_URL_MAPPINGS_WITH_SCOPE_AND_PRODUCT_ID_BY_PRODUCT_ID);
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
                            // Adding api id to uri template id just to store value
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

                setAPIProductOperationPoliciesToURITemplatesMap(new Integer(apiId).toString(), uriTemplateMap);

                PreparedStatement insertURLMappingsStatement = connection
                        .prepareStatement(SQLConstants.APIRevisionSqlConstants.INSERT_URL_MAPPINGS);
                for (URITemplate urlMapping : uriTemplateMap.values()) {
                    insertURLMappingsStatement.setInt(1, urlMapping.getId());
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
                        .prepareStatement(SQLConstants.APIRevisionSqlConstants.INSERT_PRODUCT_REVISION_RESOURCE_MAPPING);
                PreparedStatement insertOperationPolicyMappingStatement = connection.prepareStatement(
                        SQLConstants.OperationPolicyConstants.ADD_API_OPERATION_POLICY_MAPPING,
                        new String[] { "OPERATION_POLICY_MAPPING_ID".toLowerCase() });
                Map<String, String> clonedPoliciesMap = new HashMap<>();
                for (URITemplate urlMapping : uriTemplateMap.values()) {
                    getRevisionedURLMappingsStatement.setInt(1, urlMapping.getId());
                    getRevisionedURLMappingsStatement.setString(2, apiRevision.getRevisionUUID());
                    getRevisionedURLMappingsStatement.setString(3, urlMapping.getHTTPVerb());
                    getRevisionedURLMappingsStatement.setString(4, urlMapping.getAuthType());
                    getRevisionedURLMappingsStatement.setString(5, urlMapping.getUriTemplate());
                    getRevisionedURLMappingsStatement.setString(6, urlMapping.getThrottlingTier());
                    if (urlMapping.getScopes() != null) {
                        try (ResultSet rs = getRevisionedURLMappingsStatement.executeQuery()) {
                            while (rs.next()) {
                                for (Scope scope : urlMapping.getScopes()) {
                                    insertScopeResourceMappingStatement.setString(1, scope.getKey());
                                    insertScopeResourceMappingStatement.setInt(2, rs.getInt(1));
                                    insertScopeResourceMappingStatement.setInt(3, tenantId);
                                    insertScopeResourceMappingStatement.addBatch();
                                }
                            }
                        }
                    }
                    try (ResultSet rs = getRevisionedURLMappingsStatement.executeQuery()) {
                        while (rs.next()) {
                            insertProductResourceMappingStatement.setInt(1, apiId);
                            insertProductResourceMappingStatement.setInt(2, rs.getInt(1));
                            insertProductResourceMappingStatement.setString(3, apiRevision.getRevisionUUID());
                            insertProductResourceMappingStatement.addBatch();
                        }
                    }
                    try (ResultSet rs = getRevisionedURLMappingsStatement.executeQuery()) {
                        while (rs.next()) {
                            for (OperationPolicy policy : urlMapping.getOperationPolicies()) {
                                String clonedPolicyId = null;
                                if (!clonedPoliciesMap.keySet().contains(policy.getPolicyId())) {
                                    // Since we are creating a new revision, we need to clone all the policies from current status.
                                    // If the policy is not cloned from a previous policy, we have to clone.
                                    clonedPolicyId = revisionOperationPolicy(connection, policy.getPolicyId(),
                                            apiRevision.getApiUUID(), apiRevision.getRevisionUUID(), tenantDomain);
                                    clonedPoliciesMap.put(policy.getPolicyId(), clonedPolicyId);
                                }

                                Gson gson = new Gson();
                                String paramJSON = gson.toJson(policy.getParameters());

                                insertOperationPolicyMappingStatement.setInt(1, rs.getInt(1));
                                insertOperationPolicyMappingStatement.setString(2, clonedPoliciesMap.get(policy.getPolicyId()));
                                insertOperationPolicyMappingStatement.setString(3, policy.getDirection());
                                insertOperationPolicyMappingStatement.setString(4, paramJSON);
                                insertOperationPolicyMappingStatement.setInt(5, policy.getOrder());
                                insertOperationPolicyMappingStatement.executeUpdate();
                            }
                        }
                    }
                }
                insertScopeResourceMappingStatement.executeBatch();
                insertProductResourceMappingStatement.executeBatch();

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
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                handleException("Failed to add API Revision entry of API Product UUID "
                        + apiRevision.getApiUUID(), e);
            }
        } catch (SQLException e) {
            handleException("Failed to add API Revision entry of API Product UUID "
                    + apiRevision.getApiUUID(), e);
        }
    }

    /**
     * Populates operation policy mappings in the API Product URITemplate map
     *
     * @param productRevisionId Product Revision ID
     * @param uriTemplates      Map of URI Templates
     * @throws SQLException
     * @throws APIManagementException
     */
    private void setAPIProductOperationPoliciesToURITemplatesMap(String productRevisionId,
                                                                 Map<String, URITemplate> uriTemplates)
            throws SQLException, APIManagementException {

        try (Connection conn = APIMgtDBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     SQLConstants.OperationPolicyConstants.GET_OPERATION_POLICIES_PER_API_PRODUCT_SQL)) {
            ps.setString(1, productRevisionId);
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

    @Override
    public void restoreAPIProductRevision(APIRevision apiRevision) throws APIManagementException {
        try (Connection connection = APIMgtDBUtil.getConnection()) {
            try {
                connection.setAutoCommit(false);
                // Retrieve API ID
                APIProductIdentifier apiProductIdentifier =
                        APIUtil.getAPIProductIdentifierFromUUID(apiRevision.getApiUUID());
                int apiId = getAPIID(apiRevision.getApiUUID(), connection);
                int tenantId =
                        APIUtil.getTenantId(APIUtil.replaceEmailDomainBack(apiProductIdentifier.getProviderName()));
                String tenantDomain = APIUtil.getTenantDomainFromTenantId(tenantId);

                //Remove Current API Product entries from AM_API_URL_MAPPING table
                PreparedStatement removeURLMappingsFromCurrentAPIProduct = connection.prepareStatement(
                        SQLConstants.APIRevisionSqlConstants.REMOVE_CURRENT_API_PRODUCT_ENTRIES_IN_AM_API_URL_MAPPING);
                removeURLMappingsFromCurrentAPIProduct.setString(1, Integer.toString(apiId));
                removeURLMappingsFromCurrentAPIProduct.executeUpdate();

                //Copy Revision resources
                PreparedStatement getURLMappingsFromRevisionedAPIProduct = connection.prepareStatement(
                        SQLConstants.APIRevisionSqlConstants.GET_API_PRODUCT_REVISION_URL_MAPPINGS_BY_REVISION_UUID);
                getURLMappingsFromRevisionedAPIProduct.setString(1, apiRevision.getRevisionUUID());
                Map<String, URITemplate> urlMappingList = new HashMap<>();
                try (ResultSet rs = getURLMappingsFromRevisionedAPIProduct.executeQuery()) {
                    String key, httpMethod, urlPattern;
                    while (rs.next()) {
                        String script = null;
                        URITemplate uriTemplate = new URITemplate();
                        httpMethod = rs.getString("HTTP_METHOD");
                        urlPattern = rs.getString("URL_PATTERN");
                        uriTemplate.setHTTPVerb(httpMethod);
                        uriTemplate.setAuthType(rs.getString("AUTH_SCHEME"));
                        uriTemplate.setUriTemplate(rs.getString("URL_PATTERN"));
                        uriTemplate.setThrottlingTier(rs.getString("THROTTLING_TIER"));
                        InputStream mediationScriptBlob = rs.getBinaryStream("MEDIATION_SCRIPT");
                        if (mediationScriptBlob != null) {
                            script = APIMgtDBUtil.getStringFromInputStream(mediationScriptBlob);
                        }
                        uriTemplate.setMediationScript(script);
                        if (rs.getInt("API_ID") != 0) {
                            // Adding product id to uri template id just to store value
                            uriTemplate.setId(rs.getInt("API_ID"));
                        }
                        key = urlPattern + httpMethod;
                        urlMappingList.put(key, uriTemplate);
                    }
                }

                //Populate Scope Mappings
                PreparedStatement getScopeMappingsFromRevisionedAPIProduct = connection.prepareStatement(
                        SQLConstants.APIRevisionSqlConstants.GET_API_PRODUCT_REVISION_SCOPE_MAPPINGS_BY_REVISION_UUID);
                getScopeMappingsFromRevisionedAPIProduct.setString(1, apiRevision.getRevisionUUID());
                try (ResultSet rs = getScopeMappingsFromRevisionedAPIProduct.executeQuery()) {
                    while (rs.next()) {
                        String key = rs.getString("URL_PATTERN") + rs.getString("HTTP_METHOD");
                        if (urlMappingList.containsKey(key)) {
                            URITemplate uriTemplate = urlMappingList.get(key);

                            Scope scope = new Scope();
                            scope.setKey(rs.getString("SCOPE_NAME"));
                            uriTemplate.setScope(scope);

                            uriTemplate.setScopes(scope);
                        }
                    }
                }

                setAPIProductOperationPoliciesToURITemplatesMap(apiRevision.getRevisionUUID(), urlMappingList);

                PreparedStatement insertURLMappingsStatement = connection
                        .prepareStatement(SQLConstants.APIRevisionSqlConstants.INSERT_URL_MAPPINGS);
                for (URITemplate urlMapping : urlMappingList.values()) {
                    insertURLMappingsStatement.setInt(1, urlMapping.getId());
                    insertURLMappingsStatement.setString(2, urlMapping.getHTTPVerb());
                    insertURLMappingsStatement.setString(3, urlMapping.getAuthType());
                    insertURLMappingsStatement.setString(4, urlMapping.getUriTemplate());
                    insertURLMappingsStatement.setString(5, urlMapping.getThrottlingTier());
                    insertURLMappingsStatement.setString(6, Integer.toString(apiId));
                    insertURLMappingsStatement.addBatch();
                }
                insertURLMappingsStatement.executeBatch();

                //Insert Scope Mappings and operation policy mappings
                PreparedStatement getRevisionedURLMappingsStatement = connection
                        .prepareStatement(SQLConstants.APIRevisionSqlConstants.GET_REVISIONED_URL_MAPPINGS_ID);
                PreparedStatement addResourceScopeMapping = connection.prepareStatement(
                        SQLConstants.ADD_API_RESOURCE_SCOPE_MAPPING);
                PreparedStatement addOperationPolicyStatement = connection
                        .prepareStatement(SQLConstants.OperationPolicyConstants.ADD_API_OPERATION_POLICY_MAPPING);

                Map<String, String> clonedPoliciesMap = new HashMap<>();
                Set<String> usedClonedPolicies = new HashSet<String>();
                for (URITemplate urlMapping : urlMappingList.values()) {
                    getRevisionedURLMappingsStatement.setInt(1, urlMapping.getId());
                    getRevisionedURLMappingsStatement.setString(2, Integer.toString(apiId));
                    getRevisionedURLMappingsStatement.setString(3, urlMapping.getHTTPVerb());
                    getRevisionedURLMappingsStatement.setString(4, urlMapping.getAuthType());
                    getRevisionedURLMappingsStatement.setString(5, urlMapping.getUriTemplate());
                    getRevisionedURLMappingsStatement.setString(6, urlMapping.getThrottlingTier());
                    try (ResultSet rs = getRevisionedURLMappingsStatement.executeQuery()) {
                        if (rs.next()) {
                            int newURLMappingId = rs.getInt("URL_MAPPING_ID");
                            if (urlMapping.getScopes() != null && urlMapping.getScopes().size() > 0) {
                                for (Scope scope : urlMapping.getScopes()) {
                                    addResourceScopeMapping.setString(1, scope.getKey());
                                    addResourceScopeMapping.setInt(2, newURLMappingId);
                                    addResourceScopeMapping.setInt(3, tenantId);
                                    addResourceScopeMapping.addBatch();
                                }
                            }

                            if (urlMapping.getOperationPolicies().size() > 0) {
                                for (OperationPolicy policy : urlMapping.getOperationPolicies()) {
                                    if (!clonedPoliciesMap.keySet().contains(policy.getPolicyName())) {
                                        String policyId = restoreOperationPolicyRevision(connection,
                                                apiRevision.getApiUUID(), policy.getPolicyId(), apiRevision.getId(),
                                                tenantDomain);
                                        clonedPoliciesMap.put(policy.getPolicyName(), policyId);
                                        usedClonedPolicies.add(policyId);
                                    }

                                    Gson gson = new Gson();
                                    String paramJSON = gson.toJson(policy.getParameters());

                                    addOperationPolicyStatement.setInt(1, rs.getInt(1));
                                    addOperationPolicyStatement.setString(2, clonedPoliciesMap.get(policy.getPolicyName()));
                                    addOperationPolicyStatement.setString(3, policy.getDirection());
                                    addOperationPolicyStatement.setString(4, paramJSON);
                                    addOperationPolicyStatement.setInt(5, policy.getOrder());
                                    addOperationPolicyStatement.executeUpdate();
                                }
                            }
                        }
                    }
                }
                addResourceScopeMapping.executeBatch();
                cleanUnusedClonedOperationPolicies(connection, usedClonedPolicies, apiRevision.getApiUUID());

                //Get URL_MAPPING_IDs from table and add records to product mapping table
                PreparedStatement getURLMappingOfAPIProduct = connection.prepareStatement(
                        SQLConstants.GET_URL_MAPPING_IDS_OF_API_PRODUCT_SQL);
                PreparedStatement insertProductResourceMappingStatement = connection
                        .prepareStatement(SQLConstants.APIRevisionSqlConstants.INSERT_PRODUCT_REVISION_RESOURCE_MAPPING);
                getURLMappingOfAPIProduct.setString(1, Integer.toString(apiId));
                try (ResultSet rs = getURLMappingOfAPIProduct.executeQuery()) {
                    while (rs.next()) {
                        insertProductResourceMappingStatement.setInt(1, apiId);
                        insertProductResourceMappingStatement.setInt(2, rs.getInt("URL_MAPPING_ID"));
                        insertProductResourceMappingStatement.setString(3, "Current API");
                        insertProductResourceMappingStatement.addBatch();
                    }
                    insertProductResourceMappingStatement.executeBatch();
                }

                // Restoring AM_API_CLIENT_CERTIFICATE table entries
                PreparedStatement removeClientCertificatesStatement = connection.prepareStatement(SQLConstants
                        .APIRevisionSqlConstants.REMOVE_CURRENT_API_ENTRIES_IN_AM_API_CLIENT_CERTIFICATE_BY_API_ID);
                removeClientCertificatesStatement.setInt(1, apiId);
                removeClientCertificatesStatement.executeUpdate();

                PreparedStatement getClientCertificatesStatement = connection
                        .prepareStatement(SQLConstants.APIRevisionSqlConstants.GET_CLIENT_CERTIFICATES_BY_REVISION_UUID);
                getClientCertificatesStatement.setInt(1, apiId);
                getClientCertificatesStatement.setString(2, apiRevision.getRevisionUUID());
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
                        .prepareStatement(SQLConstants.APIRevisionSqlConstants.INSERT_CLIENT_CERTIFICATES_AS_CURRENT_API);
                for (ClientCertificateDTO clientCertificateDTO : clientCertificateDTOS) {
                    insertClientCertificateStatement.setInt(1, tenantId);
                    insertClientCertificateStatement.setString(2, clientCertificateDTO.getAlias());
                    insertClientCertificateStatement.setInt(3, apiId);
                    insertClientCertificateStatement.setBinaryStream(4,
                            getInputStream(clientCertificateDTO.getCertificate()));
                    insertClientCertificateStatement.setBoolean(5, false);
                    insertClientCertificateStatement.setString(6, clientCertificateDTO.getTierName());
                    insertClientCertificateStatement.setString(7, "Current API");
                    insertClientCertificateStatement.addBatch();
                }
                insertClientCertificateStatement.executeBatch();

                // Restoring AM_GRAPHQL_COMPLEXITY table
                PreparedStatement removeGraphQLComplexityStatement = connection.prepareStatement(SQLConstants
                        .APIRevisionSqlConstants.REMOVE_CURRENT_API_ENTRIES_IN_AM_GRAPHQL_COMPLEXITY_BY_API_ID);
                removeGraphQLComplexityStatement.setInt(1, apiId);
                removeGraphQLComplexityStatement.executeUpdate();

                PreparedStatement getGraphQLComplexityStatement = connection
                        .prepareStatement(SQLConstants.APIRevisionSqlConstants.GET_GRAPHQL_COMPLEXITY_BY_REVISION_UUID);
                List<CustomComplexityDetails> customComplexityDetailsList = new ArrayList<>();
                getGraphQLComplexityStatement.setInt(1, apiId);
                getGraphQLComplexityStatement.setString(2, apiRevision.getRevisionUUID());
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
                        .prepareStatement(SQLConstants.APIRevisionSqlConstants.INSERT_GRAPHQL_COMPLEXITY_AS_CURRENT_API);
                for (CustomComplexityDetails customComplexityDetails : customComplexityDetailsList) {
                    insertGraphQLComplexityStatement.setString(1, UUID.randomUUID().toString());
                    insertGraphQLComplexityStatement.setInt(2, apiId);
                    insertGraphQLComplexityStatement.setString(3, customComplexityDetails.getType());
                    insertGraphQLComplexityStatement.setString(4, customComplexityDetails.getField());
                    insertGraphQLComplexityStatement.setInt(5, customComplexityDetails.getComplexityValue());
                    insertGraphQLComplexityStatement.addBatch();
                }
                insertGraphQLComplexityStatement.executeBatch();

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                handleException("Failed to restore API Revision entry of API UUID "
                        + apiRevision.getApiUUID(), e);
            }
        } catch (SQLException e) {
            handleException("Failed to restore API Revision entry of API UUID "
                    + apiRevision.getApiUUID(), e);
        }
    }

    public void deleteAPIProductRevision(APIRevision apiRevision) throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            try {
                connection.setAutoCommit(false);
                // Retrieve API ID
                APIProductIdentifier apiProductIdentifier =
                        APIUtil.getAPIProductIdentifierFromUUID(apiRevision.getApiUUID());
                int apiId = getAPIID(apiRevision.getApiUUID(), connection);
                int tenantId =
                        APIUtil.getTenantId(APIUtil.replaceEmailDomainBack(apiProductIdentifier.getProviderName()));

                // Removing related revision entries from AM_REVISION table
                PreparedStatement removeAMRevisionStatement = connection.prepareStatement(SQLConstants
                        .APIRevisionSqlConstants.DELETE_API_REVISION);
                removeAMRevisionStatement.setString(1, apiRevision.getRevisionUUID());
                removeAMRevisionStatement.executeUpdate();

                // Removing related revision entries from AM_API_PRODUCT_MAPPING table
                PreparedStatement removeProductMappingsStatement = connection.prepareStatement(SQLConstants
                        .APIRevisionSqlConstants.REMOVE_REVISION_ENTRIES_IN_AM_API_PRODUCT_MAPPING_BY_REVISION_UUID);
                removeProductMappingsStatement.setInt(1, apiId);
                removeProductMappingsStatement.setString(2, apiRevision.getRevisionUUID());
                removeProductMappingsStatement.executeUpdate();

                // Removing related revision entries from AM_API_URL_MAPPING table
                PreparedStatement removeURLMappingsStatement = connection.prepareStatement(SQLConstants
                        .APIRevisionSqlConstants.REMOVE_PRODUCT_REVISION_ENTRIES_IN_AM_API_URL_MAPPING_BY_REVISION_UUID);
                removeURLMappingsStatement.setString(1, apiRevision.getRevisionUUID());
                removeURLMappingsStatement.executeUpdate();

                // Removing related revision entries from AM_API_CLIENT_CERTIFICATE table
                PreparedStatement removeClientCertificatesStatement = connection.prepareStatement(SQLConstants
                        .APIRevisionSqlConstants.REMOVE_REVISION_ENTRIES_IN_AM_API_CLIENT_CERTIFICATE_BY_REVISION_UUID);
                removeClientCertificatesStatement.setInt(1, apiId);
                removeClientCertificatesStatement.setString(2, apiRevision.getRevisionUUID());
                removeClientCertificatesStatement.executeUpdate();

                // Removing related revision entries from AM_GRAPHQL_COMPLEXITY table
                PreparedStatement removeGraphQLComplexityStatement = connection.prepareStatement(SQLConstants
                        .APIRevisionSqlConstants.REMOVE_REVISION_ENTRIES_IN_AM_GRAPHQL_COMPLEXITY_BY_REVISION_UUID);
                removeGraphQLComplexityStatement.setInt(1, apiId);
                removeGraphQLComplexityStatement.setString(2, apiRevision.getRevisionUUID());
                removeGraphQLComplexityStatement.executeUpdate();

                // Removing related revision entries for operation policies
                deleteAllAPISpecificOperationPoliciesByAPIUUID(connection, apiRevision.getApiUUID(), apiRevision.getRevisionUUID());

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                handleException("Failed to delete API Revision entry of API Product UUID "
                        + apiRevision.getApiUUID(), e);
            }
        } catch (SQLException e) {
            handleException("Failed to delete API Revision entry of API Product UUID "
                    + apiRevision.getApiUUID(), e);
        }
    }

}

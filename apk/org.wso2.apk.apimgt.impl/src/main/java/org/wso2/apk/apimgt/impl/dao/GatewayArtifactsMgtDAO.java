package org.wso2.apk.apimgt.impl.dao;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.apk.apimgt.api.APIManagementException;
import org.wso2.apk.apimgt.api.ErrorHandler;
import org.wso2.apk.apimgt.api.ExceptionCodes;
import org.wso2.apk.apimgt.api.model.APIRevisionDeployment;
import org.wso2.apk.apimgt.impl.dao.constants.SQLConstants;
import org.wso2.apk.apimgt.impl.dto.APIArtifactPropertyValues;
import org.wso2.apk.apimgt.impl.dto.APIRuntimeArtifactDto;
import org.wso2.apk.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.apk.apimgt.impl.utils.GatewayArtifactsMgtDBUtil;
import org.wso2.apk.apimgt.impl.utils.VHostUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GatewayArtifactsMgtDAO {

    private static final Log log = LogFactory.getLog(GatewayArtifactsMgtDAO.class);
    private static GatewayArtifactsMgtDAO gatewayArtifactsMgtDAO = null;

    /**
     * Private constructor
     */
    private GatewayArtifactsMgtDAO() {

    }

    /**
     * Method to get the instance of the GatewayArtifactsMgtDAO.
     *
     * @return {@link GatewayArtifactsMgtDAO} instance
     */
    public static GatewayArtifactsMgtDAO getInstance() {

        if (gatewayArtifactsMgtDAO == null) {
            gatewayArtifactsMgtDAO = new GatewayArtifactsMgtDAO();
        }
        return gatewayArtifactsMgtDAO;
    }

    /**
     * Add details of the APIs published in the Gateway
     *
     * @param apiId        - UUID of the API
     * @param name         - Name of the API
     * @param version      - Version of the API
     * @param organization - Identifier of the organization
     */
    private boolean addGatewayPublishedAPIDetails(Connection connection, String apiId, String name, String version,
                                                 String organization, String type)
            throws SQLException {

        boolean result = false;
        try (PreparedStatement preparedStatement = connection.prepareStatement(SQLConstants.CHECK_API_EXISTS)) {
            preparedStatement.setString(1, apiId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return true;
                }
            }
        }
        try (PreparedStatement statement = connection.prepareStatement(SQLConstants.ADD_GW_PUBLISHED_API_DETAILS)) {
            statement.setString(1, apiId);
            statement.setString(2, name);
            statement.setString(3, version);
            statement.setString(4, organization);
            statement.setString(5, type);
            result = statement.executeUpdate() == 1;
            connection.commit();
        } catch (SQLException e) {
            APIMgtDBUtil.rollbackConnection(connection,
                    "Failed to rollback add API details for " + name, e);
        }
        return result;
    }

    private boolean addGatewayPublishedAPIArtifacts(Connection connection, String apiId, String revision,
                                                   InputStream inputStream)
            throws APIManagementException, SQLException {

        String dbQuery = SQLConstants.ADD_GW_API_ARTIFACT;

        boolean result = false;
        if (isAPIArtifactExists(connection, apiId, revision)) {
            updateGatewayPublishedAPIArtifacts(connection, apiId, revision, inputStream);
        } else {
            try (PreparedStatement statement = connection.prepareStatement(dbQuery)) {
                statement.setBinaryStream(1, inputStream);
                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                statement.setTimestamp(2, timestamp);
                statement.setString(3, apiId);
                statement.setString(4, revision);
                result = statement.executeUpdate() == 1;
                connection.commit();
            }
        }
        return result;
    }

    /**
     * Check whether the API artifact for given label exists in the db
     *
     * @param APIId - UUID of the API
     */
    private boolean isAPIArtifactExists(Connection connection, String APIId, String revisionId)
            throws SQLException {

        try (PreparedStatement statement = connection.prepareStatement(SQLConstants.CHECK_ARTIFACT_EXISTS)) {
            connection.setAutoCommit(false);
            connection.commit();
            statement.setString(1, APIId);
            statement.setString(2, revisionId);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void handleException(String msg, Throwable t) throws APIManagementException {

        log.error(msg, t);
        throw new APIManagementException(msg, t);
    }

    private void handleExceptionWithCode(String msg, Throwable t, ErrorHandler errorHandler) throws APIManagementException {

        log.error(msg, t);
        throw new APIManagementException(msg, t, errorHandler);
    }

    /**
     * Retrieve the API ID of the API
     *
     * @param apiName      - Name of the API
     * @param version      - version of the API
     * @param tenantDomain - Tenant Domain of the API
     * @throws APIManagementException if an error occurs
     */
    public String getGatewayAPIId(String apiName, String version, String tenantDomain)
            throws APIManagementException {

        ResultSet rs = null;
        String apiID = null;
        try (Connection connection = GatewayArtifactsMgtDBUtil.getArtifactSynchronizerConnection();
             PreparedStatement statement = connection.prepareStatement(SQLConstants.GET_API_ID)) {
            connection.setAutoCommit(false);
            connection.commit();
            statement.setString(1, apiName);
            statement.setString(2, tenantDomain);
            statement.setString(3, version);
            rs = statement.executeQuery();
            while (rs.next()) {
                apiID = rs.getString("API_ID");
            }
        } catch (SQLException e) {
            handleException("Failed to get artifacts ", e);
        } finally {
            GatewayArtifactsMgtDBUtil.closeResultSet(rs);
        }
        return apiID;
    }

    /**
     * Retrieve the gateway labels which the API subscribed to
     *
     * @param apiID - API ID  of the API
     * @throws APIManagementException if an error occurs
     */
    public List<String> getGatewayAPILabels(String apiID)
            throws APIManagementException {

        ResultSet rs = null;
        List<String> labels = new ArrayList<>();
        try (Connection connection = GatewayArtifactsMgtDBUtil.getArtifactSynchronizerConnection();
             PreparedStatement statement = connection.prepareStatement(SQLConstants.GET_API_LABEL)) {
            connection.setAutoCommit(false);
            connection.commit();
            statement.setString(1, apiID);
            rs = statement.executeQuery();
            while (rs.next()) {
                labels.add(rs.getString("GATEWAY_LABEL"));
            }
        } catch (SQLException e) {
            handleException("Failed to get artifacts ", e);
        } finally {
            GatewayArtifactsMgtDBUtil.closeResultSet(rs);
        }
        return labels;
    }

    private void updateGatewayPublishedAPIArtifacts(Connection connection, String apiId, String revision,
                                                    InputStream fileInputStream)
            throws SQLException {

        String query = SQLConstants.UPDATE_API_ARTIFACT;
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setBinaryStream(1, fileInputStream);
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            statement.setTimestamp(2, timestamp);
            statement.setString(3, apiId);
            statement.setString(4, revision);
            statement.executeUpdate();
        }
    }

    /**
     * This method retrieves the organization and the deployed time of an API revision.
     *
     * @param apiUUID API UUID
     * @param envName Environment name
     * @param revisionUUId API revision UUID
     * @return Organization and Deployed Time
     * @throws APIManagementException If failed to retrieve organization and deployed time
     */
    public APIArtifactPropertyValues retrieveAPIArtifactPropertyValues(
            String apiUUID, String envName, String revisionUUId) throws APIManagementException {

        String query = SQLConstants.RETRIEVE_API_ARTIFACT_PROPERTY_VALUES;
        String organization = null;
        Timestamp deployedTime = null;
        APIArtifactPropertyValues propertyValues = new APIArtifactPropertyValues();
        try (Connection connection = GatewayArtifactsMgtDBUtil.getArtifactSynchronizerConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, apiUUID);
            preparedStatement.setString(2, envName);
            preparedStatement.setString(3, revisionUUId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    organization = resultSet.getString("ORGANIZATION");
                    deployedTime = resultSet.getTimestamp("DEPLOYED_TIME");
                }
            }
        } catch (SQLException e) {
            handleException("Failed to retrieve organization and API revision deployed time", e);
        }

        propertyValues.setOrganization(organization);
        propertyValues.setDeployedTime(deployedTime);
        return propertyValues;
    }

    public void addAndRemovePublishedGatewayLabels(String apiId, String revision, Set<String> gatewayLabelsToDeploy,
                                                   Map<String, String> gatewayVhosts,
                                                   Set<APIRevisionDeployment> gatewayLabelsToRemove)
            throws APIManagementException {

        String deleteQuery = SQLConstants.DELETE_GW_PUBLISHED_LABELS_BY_API_ID_REVISION_ID_DEPLOYMENT;
        if (gatewayLabelsToDeploy.size() > 0 || gatewayLabelsToRemove.size() > 0) {
            try (Connection connection = GatewayArtifactsMgtDBUtil.getArtifactSynchronizerConnection()) {
                connection.setAutoCommit(false);
                try (PreparedStatement statement = connection.prepareStatement(deleteQuery)) {
                    for (APIRevisionDeployment apiRevisionDeployment : gatewayLabelsToRemove) {
                        statement.setString(1, apiId);
                        statement.setString(2, apiRevisionDeployment.getRevisionUUID());
                        statement.setString(3, apiRevisionDeployment.getDeployment());
                        // no need to set vhost when deleting API Deployment, it is unique for revision + label
                        statement.addBatch();
                    }
                    statement.executeBatch();
                }
                try {
                    if (gatewayLabelsToDeploy.size() > 0) {
                        addPublishedGatewayLabels(connection, apiId, revision, gatewayLabelsToDeploy, gatewayVhosts);
                    }
                    connection.commit();
                } catch (SQLException | APIManagementException e) {
                    // APIManagementException if failed to revolve default vhost and set null to DB
                    connection.rollback();
                    throw new APIManagementException("Failed to attach labels", e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
                }
            } catch (SQLException e) {
                handleExceptionWithCode("Failed to attach labels" + apiId, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
            }
        }
    }

    private void addPublishedGatewayLabels(Connection connection, String apiUUID, String revisionId,
                                           Set<String> gateways, Map<String, String> gatewayVhosts)
            throws SQLException, APIManagementException {

        String addQuery = SQLConstants.ADD_GW_PUBLISHED_LABELS;
        try (PreparedStatement preparedStatement = connection.prepareStatement(addQuery)) {
            for (String gatewayLabel : gateways) {
                preparedStatement.setString(1, apiUUID);
                preparedStatement.setString(2, revisionId);
                preparedStatement.setString(3, gatewayLabel);
                String resolvedVhost = VHostUtils
                        .resolveIfDefaultVhostToNull(gatewayLabel, gatewayVhosts.get(gatewayLabel));
                preparedStatement.setString(4, resolvedVhost);
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
    }

    public void deleteGatewayArtifact(String apiId, String revision) throws APIManagementException {

        String deleteGWPublishedArtifacts = SQLConstants.DELETE_FROM_AM_GW_API_ARTIFACTS_WHERE_API_ID_AND_REVISION_ID;
        try (Connection connection = GatewayArtifactsMgtDBUtil.getArtifactSynchronizerConnection()) {
            connection.setAutoCommit(false);
            try {
                removePublishedGatewayLabels(connection, apiId, revision);
                try (PreparedStatement preparedStatement = connection.prepareStatement(deleteGWPublishedArtifacts)) {
                    preparedStatement.setString(1, apiId);
                    preparedStatement.setString(2, revision);
                    preparedStatement.executeUpdate();
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                handleExceptionWithCode("Failed to delete Gateway Artifact" + apiId, e,
                        ExceptionCodes.APIMGT_DAO_EXCEPTION);
            }
        } catch (SQLException e) {
            handleExceptionWithCode("Failed to delete Gateway Artifact" + apiId, e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
    }

    public void deleteGatewayArtifacts(String apiId) throws APIManagementException {

        String deleteGWArtifact = SQLConstants.DELETE_GW_PUBLISHED_API_DETAILS;
        try (Connection connection = GatewayArtifactsMgtDBUtil.getArtifactSynchronizerConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement preparedStatement = connection.prepareStatement(deleteGWArtifact)) {
                    preparedStatement.setString(1, apiId);
                    preparedStatement.executeUpdate();
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                handleException("Failed to delete Gateway Artifact" + apiId, e);
            }
        } catch (SQLException | APIManagementException e) {
            handleException("Failed to delete Gateway Artifact" + apiId, e);
        }
    }

    public List<APIRuntimeArtifactDto> retrieveGatewayArtifactsByAPIIDAndLabel(String apiId, String[] labels,
                                                                               String tenantDomain)
            throws APIManagementException {

        String query = SQLConstants.RETRIEVE_ARTIFACTS_BY_APIID_AND_LABEL;
        query = query.replaceAll(SQLConstants.GATEWAY_LABEL_REGEX, String.join(",",Collections.nCopies(labels.length, "?")));
        List<APIRuntimeArtifactDto> apiRuntimeArtifactDtoList = new ArrayList<>();
        try (Connection connection = GatewayArtifactsMgtDBUtil.getArtifactSynchronizerConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, apiId);
            int index = 2;
            for (String label : labels) {
                preparedStatement.setString(index, label);
                index++;
            }
            preparedStatement.setString(index, tenantDomain);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    APIRuntimeArtifactDto apiRuntimeArtifactDto = new APIRuntimeArtifactDto();
                    apiRuntimeArtifactDto.setTenantDomain(resultSet.getString("TENANT_DOMAIN"));
                    apiRuntimeArtifactDto.setApiId(apiId);
                    String label = resultSet.getString("LABEL");
                    // Do not handle the exception here since runtime artifacts are retrieved by API UUID
                    // throw the exception here.
                    String resolvedVhost = VHostUtils.resolveIfNullToDefaultVhost(label,
                            resultSet.getString("VHOST"));
                    apiRuntimeArtifactDto.setLabel(label);
                    apiRuntimeArtifactDto.setVhost(resolvedVhost);
                    apiRuntimeArtifactDto.setName(resultSet.getString("API_NAME"));
                    apiRuntimeArtifactDto.setVersion(resultSet.getString("API_VERSION"));
                    apiRuntimeArtifactDto.setProvider(resultSet.getString("API_PROVIDER"));
                    apiRuntimeArtifactDto.setRevision(resultSet.getString("REVISION_ID"));
                    apiRuntimeArtifactDto.setType(resultSet.getString("API_TYPE"));
                    apiRuntimeArtifactDto.setContext(resultSet.getString("CONTEXT"));
                    InputStream artifact = resultSet.getBinaryStream("ARTIFACT");
                    if (artifact != null) {
                        byte[] artifactByte = APIMgtDBUtil.getBytesFromInputStream(artifact);
                        try (InputStream newArtifact = new ByteArrayInputStream(artifactByte)) {
                            apiRuntimeArtifactDto.setArtifact(newArtifact);
                        } catch (IOException e) {
                            // Do not handle the exception here since runtime artifacts are retrieved by API UUID
                            // throw the exception here.
                            handleException("Error occurred retrieving input stream from byte array.", e);
                        }
                    }
                    apiRuntimeArtifactDto.setFile(true);
                    apiRuntimeArtifactDtoList.add(apiRuntimeArtifactDto);
                }
            }
        } catch (SQLException e) {
            handleException("Failed to retrieve Gateway Artifact for Api : " + apiId + " and labels: " + StringUtils.join(",", labels), e);
        }

        return apiRuntimeArtifactDtoList;
    }

    public List<APIRuntimeArtifactDto> retrieveAllGatewayArtifactsByAPIIDAndLabel(String apiId, String[] labels)
            throws APIManagementException {

        String query = SQLConstants.RETRIEVE_ALL_ARTIFACTS_BY_APIID_AND_LABEL;
        query = query.replaceAll(SQLConstants.GATEWAY_LABEL_REGEX, String.join(",",Collections.nCopies(labels.length, "?")));
        List<APIRuntimeArtifactDto> apiRuntimeArtifactDtoList = new ArrayList<>();
        try (Connection connection = GatewayArtifactsMgtDBUtil.getArtifactSynchronizerConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, apiId);
            int index = 2;
            for (String label : labels) {
                preparedStatement.setString(index, label);
                index++;
            }
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    APIRuntimeArtifactDto apiRuntimeArtifactDto = new APIRuntimeArtifactDto();
                    apiRuntimeArtifactDto.setApiId(apiId);
                    String label = resultSet.getString("LABEL");
                    String resolvedVhost = VHostUtils.resolveIfNullToDefaultVhost(label,
                            resultSet.getString("VHOST"));
                    apiRuntimeArtifactDto.setLabel(label);
                    apiRuntimeArtifactDto.setVhost(resolvedVhost);
                    apiRuntimeArtifactDto.setName(resultSet.getString("API_NAME"));
                    apiRuntimeArtifactDto.setVersion(resultSet.getString("API_VERSION"));
                    apiRuntimeArtifactDto.setProvider(resultSet.getString("API_PROVIDER"));
                    apiRuntimeArtifactDto.setRevision(resultSet.getString("REVISION_ID"));
                    apiRuntimeArtifactDto.setType(resultSet.getString("API_TYPE"));
                    apiRuntimeArtifactDto.setContext(resultSet.getString("CONTEXT"));
                    InputStream artifact = resultSet.getBinaryStream("ARTIFACT");
                    if (artifact != null) {
                        byte[] artifactByte = APIMgtDBUtil.getBytesFromInputStream(artifact);
                        try (InputStream newArtifact = new ByteArrayInputStream(artifactByte)) {
                            apiRuntimeArtifactDto.setArtifact(newArtifact);
                        } catch (IOException e) {
                            // Do not handle the exception here since runtime artifacts are retrieved by API UUID
                            // throw the exception here.
                            handleException("Error occurred retrieving input stream from byte array.", e);
                        }
                    }
                    apiRuntimeArtifactDto.setFile(true);
                    apiRuntimeArtifactDtoList.add(apiRuntimeArtifactDto);
                }
            }
        } catch (SQLException e) {
            handleException("Failed to retrieve Gateway Artifact for Api : " + apiId + " and labels: " + StringUtils.join(",", labels), e);
        }

        return apiRuntimeArtifactDtoList;
    }

    public List<APIRuntimeArtifactDto> retrieveGatewayArtifactsByAPIIDs(List<String> apiIds, String[] labels,
                                                                        String tenantDomain)
            throws APIManagementException {
        // Logging the API ID List
        if (log.isDebugEnabled()) {
            log.debug("Getting runtime artifacts for the API ID List: " + apiIds.toString());
        }
        // Split apiId list into smaller list of size 25
        int apiIdsChunkSize = SQLConstants.API_ID_CHUNK_SIZE;
        Collection<List<String>> apiIdsChunk = getSlicedCollection(apiIds, apiIdsChunkSize);
        List<APIRuntimeArtifactDto> apiRuntimeArtifactDtoList = new ArrayList<>();

        for (List<String> apiIdList: apiIdsChunk) {
            String query = SQLConstants.RETRIEVE_ARTIFACTS_BY_MULTIPLE_APIIDs_AND_LABEL;
            query = query.replaceAll(SQLConstants.GATEWAY_LABEL_REGEX, String.join(",",Collections.nCopies(labels.length, "?")));
            query = query.replaceAll(SQLConstants.API_ID_REGEX, String.join(",",Collections.nCopies(apiIdList.size(), "?")));

            try (Connection connection = GatewayArtifactsMgtDBUtil.getArtifactSynchronizerConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                int index = 1;
                for (String apiId: apiIdList) {
                    preparedStatement.setString(index, apiId);
                    index++;
                }
                for (String label : labels) {
                    preparedStatement.setString(index, label);
                    index++;
                }
                preparedStatement.setString(index, tenantDomain);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        APIRuntimeArtifactDto apiRuntimeArtifactDto = new APIRuntimeArtifactDto();
                        apiRuntimeArtifactDto.setTenantDomain(resultSet.getString("TENANT_DOMAIN"));
                        String apiId = resultSet.getString("API_ID");
                        apiRuntimeArtifactDto.setApiId(apiId);
                        String label = resultSet.getString("LABEL");
                        String resolvedVhost = VHostUtils.resolveIfNullToDefaultVhost(label,
                                resultSet.getString("VHOST"));
                        apiRuntimeArtifactDto.setLabel(label);
                        apiRuntimeArtifactDto.setVhost(resolvedVhost);
                        apiRuntimeArtifactDto.setName(resultSet.getString("API_NAME"));
                        apiRuntimeArtifactDto.setVersion(resultSet.getString("API_VERSION"));
                        apiRuntimeArtifactDto.setProvider(resultSet.getString("API_PROVIDER"));
                        apiRuntimeArtifactDto.setRevision(resultSet.getString("REVISION_ID"));
                        apiRuntimeArtifactDto.setType(resultSet.getString("API_TYPE"));
                        apiRuntimeArtifactDto.setContext(resultSet.getString("CONTEXT"));
                        InputStream artifact = resultSet.getBinaryStream("ARTIFACT");
                        if (artifact != null) {
                            byte[] artifactByte = APIMgtDBUtil.getBytesFromInputStream(artifact);
                            try (InputStream newArtifact = new ByteArrayInputStream(artifactByte)) {
                                apiRuntimeArtifactDto.setArtifact(newArtifact);
                            } catch (IOException e) {
                                // Do not handle the exception here since runtime artifacts are retrieved by API UUID
                                // throw the exception here.
                                handleException("Error occurred retrieving input stream from byte array.", e);
                            }
                        }
                        apiRuntimeArtifactDto.setFile(true);
                        log.debug("Adding runtime artifact dto for the API ID: " + apiRuntimeArtifactDto.getApiId()
                                + ", revision ID: " + apiRuntimeArtifactDto.getRevision() + ", label: "
                                + apiRuntimeArtifactDto.getLabel());
                        apiRuntimeArtifactDtoList.add(apiRuntimeArtifactDto);
                    }
                }
            } catch (SQLException e) {
                handleException("Failed to retrieve Gateway Artifact for Apis : " + apiIdList + " and labels: " + StringUtils.join(",", labels), e);
            }
        }
        return apiRuntimeArtifactDtoList;
    }

    public List<APIRuntimeArtifactDto> retrieveGatewayArtifactsOnlyByAPIIDs(List<String> apiIds, String tenantDomain)
            throws APIManagementException {
        // Logging the API ID List
        if (log.isDebugEnabled()) {
            log.debug("Getting runtime artifacts for the API ID List: " + apiIds.toString());
        }
        int apiIdsChunkSize = SQLConstants.API_ID_CHUNK_SIZE;
        Collection<List<String>> apiIdsChunk = getSlicedCollection(apiIds, apiIdsChunkSize);
        List<APIRuntimeArtifactDto> apiRuntimeArtifactDtoList = new ArrayList<>();

        for (List<String> apiIdList: apiIdsChunk) {
            String query = SQLConstants.RETRIEVE_ARTIFACTS_ONLY_BY_MULTIPLE_APIIDS;
            query = query.replaceAll(SQLConstants.API_ID_REGEX, String.join(",",Collections.nCopies(apiIdList.size(), "?")));

            try (Connection connection = GatewayArtifactsMgtDBUtil.getArtifactSynchronizerConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                int index = 1;
                for (String apiId: apiIdList) {
                    preparedStatement.setString(index, apiId);
                    index++;
                }
                preparedStatement.setString(index, tenantDomain);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        APIRuntimeArtifactDto apiRuntimeArtifactDto = new APIRuntimeArtifactDto();
                        apiRuntimeArtifactDto.setTenantDomain(resultSet.getString("TENANT_DOMAIN"));
                        String apiId = resultSet.getString("API_ID");
                        apiRuntimeArtifactDto.setApiId(apiId);
                        String label = resultSet.getString("LABEL");
                        String resolvedVhost = VHostUtils.resolveIfNullToDefaultVhost(label,
                                resultSet.getString("VHOST"));
                        apiRuntimeArtifactDto.setLabel(label);
                        apiRuntimeArtifactDto.setVhost(resolvedVhost);
                        apiRuntimeArtifactDto.setName(resultSet.getString("API_NAME"));
                        apiRuntimeArtifactDto.setVersion(resultSet.getString("API_VERSION"));
                        apiRuntimeArtifactDto.setProvider(resultSet.getString("API_PROVIDER"));
                        apiRuntimeArtifactDto.setRevision(resultSet.getString("REVISION_ID"));
                        apiRuntimeArtifactDto.setType(resultSet.getString("API_TYPE"));
                        apiRuntimeArtifactDto.setContext(resultSet.getString("CONTEXT"));
                        InputStream artifact = resultSet.getBinaryStream("ARTIFACT");
                        if (artifact != null) {
                            byte[] artifactByte = APIMgtDBUtil.getBytesFromInputStream(artifact);
                            try (InputStream newArtifact = new ByteArrayInputStream(artifactByte)) {
                                apiRuntimeArtifactDto.setArtifact(newArtifact);
                            } catch (IOException e) {
                                // Do not handle the exception here since runtime artifacts are retrieved by API UUID
                                // throw the exception here.
                                handleException("Error occurred retrieving input stream from byte array.", e);
                            }
                        }
                        apiRuntimeArtifactDto.setFile(true);
                        if (log.isDebugEnabled()){
                            log.debug("Adding runtime artifact dto for the API ID: " + apiRuntimeArtifactDto.getApiId()
                                    + ", revision ID: " + apiRuntimeArtifactDto.getRevision() + ", label: "
                                    + apiRuntimeArtifactDto.getLabel());
                        }
                        apiRuntimeArtifactDtoList.add(apiRuntimeArtifactDto);
                    }
                }
            } catch (SQLException e) {
                handleException("Failed to retrieve Gateway Artifact for Apis : " + apiIdList, e);
            }
        }
        return apiRuntimeArtifactDtoList;
    }

    private Collection<List<String>> getSlicedCollection(List<String> apiIds, int apiIdsChunkSize) {
        Collection<List<String>> apiIdsChunk = IntStream.range(0, apiIds.size())
                .boxed()
                .collect(Collectors.groupingBy(partition -> (partition / apiIdsChunkSize),
                        Collectors.mapping(elementIndex -> apiIds.get(elementIndex), Collectors.toList())))
                .values();
        return apiIdsChunk;
    }

    public List<APIRuntimeArtifactDto> retrieveAllGatewayArtifactsByAPIIDs(List<String> apiIds, String[] labels)
            throws APIManagementException {
        // Logging the API ID List
        log.debug("Getting runtime artifacts for the API ID List: " + apiIds.toString());
        // Split apiId list into smaller list of size 25
        List<List<String>> apiIdsChunk = new ArrayList<>();
        int apiIdListSize = apiIds.size();
        int apiIdArrayIndex = 0;
        int apiIdsChunkSize = SQLConstants.API_ID_CHUNK_SIZE;
        while (apiIdArrayIndex < apiIdListSize) {
            apiIdsChunk.add(apiIds.subList(apiIdArrayIndex, Math.min(apiIdArrayIndex + apiIdsChunkSize, apiIdListSize)));
            apiIdArrayIndex += apiIdsChunkSize;
        }
        List<APIRuntimeArtifactDto> apiRuntimeArtifactDtoList = new ArrayList<>();

        for (List<String> apiIdList: apiIdsChunk) {
            String query = SQLConstants.RETRIEVE_ALL_ARTIFACTS_BY_MULTIPLE_APIIDs_AND_LABEL;
            query = query.replaceAll(SQLConstants.GATEWAY_LABEL_REGEX, String.join(",",Collections.nCopies(labels.length, "?")));
            query = query.replaceAll(SQLConstants.API_ID_REGEX, String.join(",",Collections.nCopies(apiIdList.size(), "?")));

            try (Connection connection = GatewayArtifactsMgtDBUtil.getArtifactSynchronizerConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                int index = 1;
                for (String apiId: apiIdList) {
                    preparedStatement.setString(index, apiId);
                    index++;
                }
                for (String label : labels) {
                    preparedStatement.setString(index, label);
                    index++;
                }
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        APIRuntimeArtifactDto apiRuntimeArtifactDto = new APIRuntimeArtifactDto();
                        String apiId = resultSet.getString("API_ID");
                        apiRuntimeArtifactDto.setApiId(apiId);
                        String label = resultSet.getString("LABEL");
                        String resolvedVhost = VHostUtils.resolveIfNullToDefaultVhost(label,
                                resultSet.getString("VHOST"));
                        apiRuntimeArtifactDto.setLabel(label);
                        apiRuntimeArtifactDto.setVhost(resolvedVhost);
                        apiRuntimeArtifactDto.setName(resultSet.getString("API_NAME"));
                        apiRuntimeArtifactDto.setVersion(resultSet.getString("API_VERSION"));
                        apiRuntimeArtifactDto.setProvider(resultSet.getString("API_PROVIDER"));
                        apiRuntimeArtifactDto.setRevision(resultSet.getString("REVISION_ID"));
                        apiRuntimeArtifactDto.setType(resultSet.getString("API_TYPE"));
                        apiRuntimeArtifactDto.setContext(resultSet.getString("CONTEXT"));
                        InputStream artifact = resultSet.getBinaryStream("ARTIFACT");
                        if (artifact != null) {
                            byte[] artifactByte = APIMgtDBUtil.getBytesFromInputStream(artifact);
                            try (InputStream newArtifact = new ByteArrayInputStream(artifactByte)) {
                                apiRuntimeArtifactDto.setArtifact(newArtifact);
                            } catch (IOException e) {
                                // Do not handle the exception here since runtime artifacts are retrieved by API UUID
                                // throw the exception here.
                                handleException("Error occurred retrieving input stream from byte array.", e);
                            }
                        }
                        apiRuntimeArtifactDto.setFile(true);
                        log.debug("Adding runtime artifact dto for the API ID: " + apiRuntimeArtifactDto.getApiId()
                                + ", revision ID: " + apiRuntimeArtifactDto.getRevision() + ", label: "
                                + apiRuntimeArtifactDto.getLabel());
                        apiRuntimeArtifactDtoList.add(apiRuntimeArtifactDto);
                    }
                }
            } catch (SQLException e) {
                handleException("Failed to retrieve Gateway Artifact for Apis : " + apiIdList + " and labels: " + StringUtils.join(",", labels), e);
            }
        }
        return apiRuntimeArtifactDtoList;
    }

    public List<APIRuntimeArtifactDto> retrieveGatewayArtifactsByLabel(String[] labels, String tenantDomain)
            throws APIManagementException {

        String query = SQLConstants.RETRIEVE_ARTIFACTS_BY_LABEL;
        query = query.replaceAll(SQLConstants.GATEWAY_LABEL_REGEX, String.join(",",Collections.nCopies(labels.length, "?")));
        List<APIRuntimeArtifactDto> apiRuntimeArtifactDtoList = new ArrayList<>();
        try (Connection connection = GatewayArtifactsMgtDBUtil.getArtifactSynchronizerConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            int index = 1;
            for (String label : labels) {
                preparedStatement.setString(index, label);
                index++;
            }
            preparedStatement.setString(index, tenantDomain);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String apiId = resultSet.getString("API_ID");
                    String label = resultSet.getString("LABEL");
                    try {
                        APIRuntimeArtifactDto apiRuntimeArtifactDto = new APIRuntimeArtifactDto();
                        apiRuntimeArtifactDto.setTenantDomain(resultSet.getString("TENANT_DOMAIN"));
                        apiRuntimeArtifactDto.setApiId(apiId);
                        String resolvedVhost = VHostUtils.resolveIfNullToDefaultVhost(label,
                                resultSet.getString("VHOST"));
                        apiRuntimeArtifactDto.setLabel(label);
                        apiRuntimeArtifactDto.setVhost(resolvedVhost);
                        apiRuntimeArtifactDto.setName(resultSet.getString("API_NAME"));
                        apiRuntimeArtifactDto.setVersion(resultSet.getString("API_VERSION"));
                        apiRuntimeArtifactDto.setProvider(resultSet.getString("API_PROVIDER"));
                        apiRuntimeArtifactDto.setRevision(resultSet.getString("REVISION_ID"));
                        apiRuntimeArtifactDto.setType(resultSet.getString("API_TYPE"));
                        apiRuntimeArtifactDto.setContext(resultSet.getString("CONTEXT"));
                        InputStream artifact = resultSet.getBinaryStream("ARTIFACT");
                        if (artifact != null) {
                            byte[] artifactByte = APIMgtDBUtil.getBytesFromInputStream(artifact);
                            try (InputStream newArtifact = new ByteArrayInputStream(artifactByte)) {
                                apiRuntimeArtifactDto.setArtifact(newArtifact);
                            }
                        }
                        apiRuntimeArtifactDto.setFile(true);
                        apiRuntimeArtifactDtoList.add(apiRuntimeArtifactDto);
                    } catch (APIManagementException e) {
                        // handle exception inside the loop and continue with other API artifacts
                        log.error(String.format("Error resolving vhost while retrieving runtime artifact for API %s, "
                                + "gateway environment \"%s\", tenant: \"%s\"." +
                                "Skipping runtime artifact for the API.", apiId, label, tenantDomain), e);
                    } catch (IOException e) {
                        // handle exception inside the loop and continue with other API artifacts
                        log.error(String.format("Error occurred retrieving input stream from byte array of " +
                                "API: %s, gateway environment \"%s\", tenant: \"%s\".", apiId, label, tenantDomain), e);
                    } catch (SQLException e) {
                        // handle exception inside the loop and continue with other API artifacts
                        log.error(String.format("Failed to retrieve Gateway Artifact of API: %s, " +
                                "gateway environment \"%s\", tenant: \"%s\".", apiId, label, tenantDomain), e);
                    }
                }
            }
        } catch (SQLException e) {
            handleException("Failed to retrieve Gateway Artifact for labels : " + StringUtils.join(",", labels), e);
        }

        return apiRuntimeArtifactDtoList;
    }

    public List<APIRuntimeArtifactDto> retrieveAllGatewayArtifactsByLabel(String[] labels)
            throws APIManagementException {

        String query = SQLConstants.RETRIEVE_ALL_ARTIFACTS_BY_LABEL;
        query = query.replaceAll(SQLConstants.GATEWAY_LABEL_REGEX, String.join(",",Collections.nCopies(labels.length, "?")));
        List<APIRuntimeArtifactDto> apiRuntimeArtifactDtoList = new ArrayList<>();
        try (Connection connection = GatewayArtifactsMgtDBUtil.getArtifactSynchronizerConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            int index = 1;
            for (String label : labels) {
                preparedStatement.setString(index, label);
                index++;
            }
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String apiId = resultSet.getString("API_ID");
                    String label = resultSet.getString("LABEL");
                    try {
                        APIRuntimeArtifactDto apiRuntimeArtifactDto = new APIRuntimeArtifactDto();
                        apiRuntimeArtifactDto.setApiId(apiId);
                        String resolvedVhost = VHostUtils.resolveIfNullToDefaultVhost(label,
                                resultSet.getString("VHOST"));
                        apiRuntimeArtifactDto.setLabel(label);
                        apiRuntimeArtifactDto.setVhost(resolvedVhost);
                        apiRuntimeArtifactDto.setName(resultSet.getString("API_NAME"));
                        apiRuntimeArtifactDto.setVersion(resultSet.getString("API_VERSION"));
                        apiRuntimeArtifactDto.setProvider(resultSet.getString("API_PROVIDER"));
                        apiRuntimeArtifactDto.setRevision(resultSet.getString("REVISION_ID"));
                        apiRuntimeArtifactDto.setType(resultSet.getString("API_TYPE"));
                        apiRuntimeArtifactDto.setContext(resultSet.getString("CONTEXT"));
                        InputStream artifact = resultSet.getBinaryStream("ARTIFACT");
                        if (artifact != null) {
                            byte[] artifactByte = APIMgtDBUtil.getBytesFromInputStream(artifact);
                            try (InputStream newArtifact = new ByteArrayInputStream(artifactByte)) {
                                apiRuntimeArtifactDto.setArtifact(newArtifact);
                            }
                        }
                        apiRuntimeArtifactDto.setFile(true);
                        apiRuntimeArtifactDtoList.add(apiRuntimeArtifactDto);
                    } catch (APIManagementException e) {
                        // handle exception inside the loop and continue with other API artifacts
                        log.error(String.format("Error resolving vhost while retrieving runtime artifact for API %s, "
                                + "gateway environment \"%s\"." +
                                "Skipping runtime artifact for the API.", apiId, label), e);
                    } catch (IOException e) {
                        // handle exception inside the loop and continue with other API artifacts
                        log.error(String.format("Error occurred retrieving input stream from byte array of " +
                                "API: %s, gateway environment \"%s\".", apiId, label), e);
                    } catch (SQLException e) {
                        // handle exception inside the loop and continue with other API artifacts
                        log.error(String.format("Failed to retrieve Gateway Artifact of API: %s, " +
                                "gateway environment \"%s\".", apiId, label), e);
                    }
                }
            }
        } catch (SQLException e) {
            handleException("Failed to retrieve Gateway Artifact for labels : " + StringUtils.join(",", labels), e);
        }

        return apiRuntimeArtifactDtoList;
    }

    public List<APIRuntimeArtifactDto> retrieveGatewayArtifacts(String tenantDomain)
            throws APIManagementException {

        String query = SQLConstants.RETRIEVE_ARTIFACTS;
        List<APIRuntimeArtifactDto> apiRuntimeArtifactDtoList = new ArrayList<>();
        try (Connection connection = GatewayArtifactsMgtDBUtil.getArtifactSynchronizerConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, tenantDomain);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String apiId = resultSet.getString("API_ID");
                    String label = resultSet.getString("LABEL");
                    try {
                        APIRuntimeArtifactDto apiRuntimeArtifactDto = new APIRuntimeArtifactDto();
                        apiRuntimeArtifactDto.setTenantDomain(resultSet.getString("TENANT_DOMAIN"));
                        apiRuntimeArtifactDto.setApiId(apiId);
                        String resolvedVhost = VHostUtils.resolveIfNullToDefaultVhost(label,
                                resultSet.getString("VHOST"));
                        apiRuntimeArtifactDto.setLabel(label);
                        apiRuntimeArtifactDto.setVhost(resolvedVhost);
                        apiRuntimeArtifactDto.setName(resultSet.getString("API_NAME"));
                        apiRuntimeArtifactDto.setVersion(resultSet.getString("API_VERSION"));
                        apiRuntimeArtifactDto.setProvider(resultSet.getString("API_PROVIDER"));
                        apiRuntimeArtifactDto.setRevision(resultSet.getString("REVISION_ID"));
                        apiRuntimeArtifactDto.setType(resultSet.getString("API_TYPE"));
                        apiRuntimeArtifactDto.setContext(resultSet.getString("CONTEXT"));
                        InputStream artifact = resultSet.getBinaryStream("ARTIFACT");
                        if (artifact != null) {
                            byte[] artifactByte = APIMgtDBUtil.getBytesFromInputStream(artifact);
                            try (InputStream newArtifact = new ByteArrayInputStream(artifactByte)) {
                                apiRuntimeArtifactDto.setArtifact(newArtifact);
                            }
                        }
                        apiRuntimeArtifactDto.setFile(true);
                        apiRuntimeArtifactDtoList.add(apiRuntimeArtifactDto);
                    } catch (APIManagementException e) {
                        // handle exception inside the loop and continue with other API artifacts
                        log.error(String.format("Error resolving vhost while retrieving runtime artifact for API %s, "
                                + "gateway environment \"%s\", tenant: \"%s\"." +
                                "Skipping runtime artifact for the API.", apiId, label, tenantDomain), e);
                    } catch (IOException e) {
                        // handle exception inside the loop and continue with other API artifacts
                        log.error(String.format("Error occurred retrieving input stream from byte array of " +
                                "API: %s, gateway environment \"%s\", tenant: \"%s\".", apiId, label, tenantDomain), e);
                    } catch (SQLException e) {
                        // handle exception inside the loop and continue with other API artifacts
                        log.error(String.format("Failed to retrieve Gateway Artifact of API: %s, " +
                                "gateway environment \"%s\", tenant: \"%s\".", apiId, label, tenantDomain), e);
                    }
                }
            }
        } catch (SQLException e) {
            handleException("Failed to retrieve Gateway Artifacts.", e);
        }

        return apiRuntimeArtifactDtoList;
    }

    public List<APIRuntimeArtifactDto> retrieveAllGatewayArtifacts() throws APIManagementException {
        String query = SQLConstants.RETRIEVE_ALL_ARTIFACTS;
        List<APIRuntimeArtifactDto> apiRuntimeArtifactDtoList = new ArrayList<>();
        try (Connection connection = GatewayArtifactsMgtDBUtil.getArtifactSynchronizerConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String apiId = resultSet.getString("API_ID");
                    String label = resultSet.getString("LABEL");
                    try {
                        APIRuntimeArtifactDto apiRuntimeArtifactDto = new APIRuntimeArtifactDto();
                        apiRuntimeArtifactDto.setApiId(apiId);
                        String resolvedVhost = VHostUtils.resolveIfNullToDefaultVhost(label,
                                resultSet.getString("VHOST"));
                        apiRuntimeArtifactDto.setLabel(label);
                        apiRuntimeArtifactDto.setVhost(resolvedVhost);
                        apiRuntimeArtifactDto.setName(resultSet.getString("API_NAME"));
                        apiRuntimeArtifactDto.setVersion(resultSet.getString("API_VERSION"));
                        apiRuntimeArtifactDto.setProvider(resultSet.getString("API_PROVIDER"));
                        apiRuntimeArtifactDto.setRevision(resultSet.getString("REVISION_ID"));
                        apiRuntimeArtifactDto.setType(resultSet.getString("API_TYPE"));
                        apiRuntimeArtifactDto.setContext(resultSet.getString("CONTEXT"));
                        InputStream artifact = resultSet.getBinaryStream("ARTIFACT");
                        if (artifact != null) {
                            byte[] artifactByte = APIMgtDBUtil.getBytesFromInputStream(artifact);
                            try (InputStream newArtifact = new ByteArrayInputStream(artifactByte)) {
                                apiRuntimeArtifactDto.setArtifact(newArtifact);
                            }
                        }
                        apiRuntimeArtifactDto.setFile(true);
                        apiRuntimeArtifactDtoList.add(apiRuntimeArtifactDto);
                    } catch (APIManagementException e) {
                        // handle exception inside the loop and continue with other API artifacts
                        log.error(String.format("Error resolving vhost while retrieving runtime artifact for API %s, "
                                + "gateway environment \"%s\"." +
                                "Skipping runtime artifact for the API.", apiId, label), e);
                    } catch (IOException e) {
                        // handle exception inside the loop and continue with other API artifacts
                        log.error(String.format("Error occurred retrieving input stream from byte array of " +
                                "API: %s, gateway environment \"%s\".", apiId, label), e);
                    } catch (SQLException e) {
                        // handle exception inside the loop and continue with other API artifacts
                        log.error(String.format("Failed to retrieve Gateway Artifact of API: %s, " +
                                "gateway environment \"%s\".", apiId, label), e);
                    }
                }
            }
        } catch (SQLException e) {
            handleException("Failed to retrieve Gateway Artifacts.", e);
        }

        return apiRuntimeArtifactDtoList;
    }

    public void removePublishedGatewayLabels(String apiId, String apiRevisionId, Set<String> gatewayLabels) throws APIManagementException {

        try (Connection connection = GatewayArtifactsMgtDBUtil.getArtifactSynchronizerConnection()) {
            try {
                connection.setAutoCommit(false);
                removePublishedGatewayLabels(connection, apiId, apiRevisionId, gatewayLabels);
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            handleExceptionWithCode("Failed to delete Gateway Artifact", e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
    }

    private void removePublishedGatewayLabels(Connection connection, String apiId, String revision)
            throws SQLException {

        try (PreparedStatement preparedStatement = connection
                .prepareStatement(SQLConstants.DELETE_GW_PUBLISHED_LABELS)) {
            preparedStatement.setString(1, apiId);
            preparedStatement.setString(2, revision);
            preparedStatement.executeUpdate();
        }
    }

    private void removePublishedGatewayLabels(Connection connection, String apiId, String revision,
                                              Set<String> labels) throws SQLException {

        try (PreparedStatement preparedStatement = connection
                .prepareStatement(SQLConstants.DELETE_GW_PUBLISHED_LABELS_BY_API_ID_REVISION_ID_DEPLOYMENT)) {
            for (String label : labels) {
                preparedStatement.setString(1, apiId);
                preparedStatement.setString(2, revision);
                preparedStatement.setString(3, label);
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
    }

    public void addAndRemovePublishedGatewayLabels(String apiId, String revision, Set<String> gateways,
                                                   Map<String, String> gatewayVhosts)
            throws APIManagementException {

        try (Connection connection = GatewayArtifactsMgtDBUtil.getArtifactSynchronizerConnection()) {
            try {
                connection.setAutoCommit(false);
                removePublishedGatewayLabels(connection, apiId, revision);
                addPublishedGatewayLabels(connection, apiId, revision, gateways, gatewayVhosts);
                connection.commit();
            } catch (SQLException | APIManagementException e) {
                // APIManagementException if failed to revolve default vhost and set null to DB
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            handleException("Failed to delete and add  Gateway environments ", e);
        }
    }

    /**
     *
     * @param organization
     * @throws APIManagementException
     */
    public void removeOrganizationGatewayArtifacts(String organization) throws APIManagementException {

        try (Connection artifactSynchronizerConn = GatewayArtifactsMgtDBUtil.getArtifactSynchronizerConnection()) {
            artifactSynchronizerConn.setAutoCommit(false);
            // Delete gateway Artifacts from AM_GW_PUBLISHED_API_DETAILS, FK->AM_GW_API_ARTIFACTS,AM_GW_API_DEPLOYMENTS
            try (PreparedStatement preparedStatement = artifactSynchronizerConn.prepareStatement(
                    SQLConstants.DELETE_BULK_GW_PUBLISHED_API_DETAILS)) {
                preparedStatement.setString(1, organization);
                preparedStatement.executeUpdate();
                artifactSynchronizerConn.commit();
            } catch (SQLException e) {
                throw e;
            }
        } catch (SQLException e) {
            handleException("Failed to Delete API GW Artifact of organization " + organization + " from Database", e);
        }
    }

    public void addGatewayAPIArtifactAndMetaData(String apiUUID, String apiName, String version, String revisionUUID,
                                                 String organization, String apiType, File artifact)
            throws APIManagementException {
        try (Connection connection = GatewayArtifactsMgtDBUtil.getArtifactSynchronizerConnection()) {
            try {
                connection.setAutoCommit(false);
                addGatewayPublishedAPIDetails(connection, apiUUID, apiName, version, organization, apiType);
                try (FileInputStream fileInputStream = new FileInputStream(artifact)) {
                    addGatewayPublishedAPIArtifacts(connection, apiUUID, revisionUUID, fileInputStream);
                }
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException | IOException e) {
            handleExceptionWithCode("Failed to Add Artifact to Database", e, ExceptionCodes.INTERNAL_ERROR);
        }
    }

}

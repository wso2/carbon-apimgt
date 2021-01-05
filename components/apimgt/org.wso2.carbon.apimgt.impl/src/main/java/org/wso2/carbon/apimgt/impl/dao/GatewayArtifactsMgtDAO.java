package org.wso2.carbon.apimgt.impl.dao;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.constants.SQLConstants;
import org.wso2.carbon.apimgt.impl.dto.APIRuntimeArtifactDto;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.apimgt.impl.utils.GatewayArtifactsMgtDBUtil;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

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
     * @param name      - Name of the API
     * @param version      - Version of the API
     * @param tenantDomain - Tenant domain of the API
     * @throws APIManagementException if an error occurs
     */
    public boolean addGatewayPublishedAPIDetails(String apiId, String name, String version, String tenantDomain)
            throws APIManagementException {

        boolean result = false;
        try (Connection connection = GatewayArtifactsMgtDBUtil.getArtifactSynchronizerConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(SQLConstants.ADD_GW_PUBLISHED_API_DETAILS)) {
                statement.setString(1, apiId);
                statement.setString(2, name);
                statement.setString(3, version);
                statement.setString(4, tenantDomain);
                result = statement.executeUpdate() == 1;
                connection.commit();
            } catch (SQLException e) {
                APIMgtDBUtil.rollbackConnection(connection,
                        "Failed to rollback add API details for " + name, e);
            }
        } catch (SQLException e) {
            handleException("Failed to add API details for " + name, e);
        }
        return result;
    }

    /**
     * Add or update details of the APIs published in the Gateway
     *
     * @param apiId        - UUID of the API
     * @param revision - Published api revision
     * @param inputStream         - Byte array Input stream of the serializide gatewayAPIDTO
     * @throws APIManagementException if an error occurs
     */
    public boolean addGatewayPublishedAPIArtifacts(String apiId, String revision, InputStream inputStream)
            throws APIManagementException {

        String dbQuery = SQLConstants.ADD_GW_API_ARTIFACT;

        boolean result = false;
        try (Connection connection = GatewayArtifactsMgtDBUtil.getArtifactSynchronizerConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(dbQuery)) {
                statement.setBinaryStream(1, inputStream);
                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                statement.setTimestamp(2, timestamp);
                statement.setString(3, apiId);
                statement.setString(4, revision);
                result = statement.executeUpdate() == 1;
                connection.commit();
            } catch (SQLException e) {
                APIMgtDBUtil.rollbackConnection(connection,
                        "Failed to rollback add artifacts for " + apiId, e);
            }
        } catch (SQLException e) {
            handleException("Failed to add artifacts for " + apiId, e);
        }
        return result;
    }

    /**
     * Retrieve the blob of the API
     *
     * @param APIId        - UUID of the API
     * @param gatewayLabel - Gateway label of the API
     * @throws APIManagementException if an error occurs
     */
    public String getGatewayPublishedAPIArtifacts(String APIId, String gatewayLabel,
                                                  String gatewayInstruction)
            throws APIManagementException {

        ResultSet rs = null;
        String gatewayRuntimeArtifacts = null;
        String sqlQuery;
        if (APIConstants.GatewayArtifactSynchronizer.GATEWAY_INSTRUCTION_ANY.equals(gatewayInstruction)) {
            sqlQuery = SQLConstants.GET_API_ARTIFACT_ANY_INSTRUCTION;
        } else {
            sqlQuery = SQLConstants.GET_API_ARTIFACT;
        }
        try (Connection connection = GatewayArtifactsMgtDBUtil.getArtifactSynchronizerConnection();
             PreparedStatement statement = connection.prepareStatement(sqlQuery)) {
            connection.setAutoCommit(false);
            connection.commit();
            statement.setString(1, APIId);
            statement.setString(2, gatewayLabel);
            if (!APIConstants.GatewayArtifactSynchronizer.GATEWAY_INSTRUCTION_ANY.equals(gatewayInstruction)) {
                statement.setString(3, gatewayInstruction);
            }
            rs = statement.executeQuery();
            if (rs.next()) {
                try (InputStream inputStream = rs.getBinaryStream(1)) {
                    gatewayRuntimeArtifacts = IOUtils.toString(inputStream, APIConstants.DigestAuthConstants.CHARSET);
                } catch (IOException e) {
                    handleException("Error in generating gatewayRuntimeArtifacts " + APIId, e);
                }
            }
        } catch (SQLException e) {
            handleException("Failed to get artifacts of API with ID " + APIId, e);
        } finally {
            GatewayArtifactsMgtDBUtil.closeResultSet(rs);
        }
        return gatewayRuntimeArtifacts;
    }

    /**
     * Retrieve the list of blobs of the APIs for a given label
     *
     * @param label - Gateway label of the API
     * @throws APIManagementException if an error occurs
     */
    public List<String> getAllGatewayPublishedAPIArtifacts(String label)
            throws APIManagementException {

        ResultSet rs = null;
        List<String> gatewayRuntimeArtifactsArray = new ArrayList<>();
        try (Connection connection = GatewayArtifactsMgtDBUtil.getArtifactSynchronizerConnection();
             PreparedStatement statement = connection.prepareStatement(SQLConstants.GET_ALL_API_ARTIFACT)) {
            connection.setAutoCommit(false);
            connection.commit();
            statement.setString(1, label);
            statement.setString(2, APIConstants.GatewayArtifactSynchronizer.GATEWAY_INSTRUCTION_PUBLISH);
            rs = statement.executeQuery();
            while (rs.next()) {
                try (InputStream inputStream = rs.getBinaryStream(1)) {
                    String gatewayRuntimeArtifacts = IOUtils.toString(inputStream,
                            APIConstants.DigestAuthConstants.CHARSET);
                    gatewayRuntimeArtifactsArray.add(gatewayRuntimeArtifacts);
                } catch (IOException e) {
                    handleException("Error in generating gatewayRuntimeArtifacts ", e);
                }
            }
        } catch (SQLException e) {
            handleException("Failed to get artifacts ", e);
        } finally {
            GatewayArtifactsMgtDBUtil.closeResultSet(rs);
        }
        return gatewayRuntimeArtifactsArray;
    }

    /**
     * Check whether the API is published in any of the Gateways
     *
     * @param APIId - UUID of the API
     * @throws APIManagementException if an error occurs
     */
    public boolean isAPIPublishedInAnyGateway(String APIId) throws APIManagementException {

        int count = 0;
        ResultSet rs = null;
        try (Connection connection = GatewayArtifactsMgtDBUtil.getArtifactSynchronizerConnection();
             PreparedStatement statement = connection
                     .prepareStatement(SQLConstants.GET_PUBLISHED_GATEWAYS_FOR_API)) {
            connection.setAutoCommit(false);
            connection.commit();
            statement.setString(1, APIId);
            statement.setString(2, APIConstants.GatewayArtifactSynchronizer.GATEWAY_INSTRUCTION_PUBLISH);
            rs = statement.executeQuery();
            while (rs.next()) {
                count = rs.getInt("COUNT");
            }
        } catch (SQLException e) {
            handleException("Failed check whether API is published in any gateway " + APIId, e);
        } finally {
            GatewayArtifactsMgtDBUtil.closeResultSet(rs);
        }
        return count != 0;
    }

    /**
     * Check whether the API details exists in the db
     *
     * @param APIId - UUID of the API
     * @throws APIManagementException if an error occurs
     */
    public boolean isAPIDetailsExists(String APIId) throws APIManagementException {

        ResultSet rs = null;
        try (Connection connection = GatewayArtifactsMgtDBUtil.getArtifactSynchronizerConnection();
             PreparedStatement statement = connection
                     .prepareStatement(SQLConstants.CHECK_API_EXISTS)) {
            connection.setAutoCommit(false);
            connection.commit();
            statement.setString(1, APIId);
            rs = statement.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            handleException("Failed to check API details status of API with ID " + APIId, e);
        } finally {
            GatewayArtifactsMgtDBUtil.closeResultSet(rs);
        }
        return false;
    }

    /**
     * Check whether the API artifact for given label exists in the db
     *
     * @param APIId - UUID of the API
     * @throws APIManagementException if an error occurs
     */
    public boolean isAPIArtifactExists(String APIId, String revisionId) throws APIManagementException {

        ResultSet rs = null;
        try (Connection connection = GatewayArtifactsMgtDBUtil.getArtifactSynchronizerConnection();
             PreparedStatement statement = connection.prepareStatement(SQLConstants.CHECK_ARTIFACT_EXISTS)) {
            connection.setAutoCommit(false);
            connection.commit();
            statement.setString(1, APIId);
            statement.setString(2, revisionId);
            rs = statement.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            handleException("Failed to check API artifact status of API with ID " + APIId + " for revision "
                    + revisionId, e);
        } finally {
            GatewayArtifactsMgtDBUtil.closeResultSet(rs);
        }
        return false;
    }

    private void handleException(String msg, Throwable t) throws APIManagementException {

        log.error(msg, t);
        throw new APIManagementException(msg, t);
    }

    /**
     * Retrieve the API ID of the API
     *
     * @param apiName - Name of the API
     * @param version - version of the API
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

    public void updateGatewayPublishedAPIArtifacts(String apiId, String revision, FileInputStream fileInputStream)
            throws APIManagementException {

        String query = SQLConstants.UPDATE_API_ARTIFACT;
        try (Connection connection = GatewayArtifactsMgtDBUtil.getArtifactSynchronizerConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setBinaryStream(1, fileInputStream);
                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                statement.setTimestamp(2, timestamp);
                statement.setString(3, apiId);
                statement.setString(4, revision);
                statement.executeUpdate();
                connection.commit();
            } catch (SQLException e) {
                APIMgtDBUtil.rollbackConnection(connection,
                        "Failed to rollback update artifacts for " + apiId, e);
            }
        } catch (SQLException e) {
            handleException("Failed to update artifacts for " + apiId, e);
        }

    }

    public void addAndRemovePublishedGatewayLabels(String apiId, String revision, String[] gatewayLabels)
            throws APIManagementException {

        String addQuery = SQLConstants.ADD_GW_PUBLISHED_LABELS;
        String deleteQuery = SQLConstants.DELETE_GW_PUBLISHED_LABELS;
        try (Connection connection = GatewayArtifactsMgtDBUtil.getArtifactSynchronizerConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement statement = connection.prepareStatement(deleteQuery)) {
                    statement.setString(1, apiId);
                    statement.setString(2, revision);
                    statement.executeUpdate();
                }
                if (gatewayLabels.length > 0) {
                    try (PreparedStatement preparedStatement = connection.prepareStatement(addQuery)) {
                        for (String gatewayLabel : gatewayLabels) {
                            preparedStatement.setString(1, apiId);
                            preparedStatement.setString(2, revision);
                            preparedStatement.setString(3, gatewayLabel);
                            preparedStatement.addBatch();
                        }
                        preparedStatement.executeBatch();
                    }
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new APIManagementException("Failed to attach labels", e);
            }
        } catch (SQLException e) {
            handleException("Failed to attach labels" + apiId, e);
        }
    }

    public void deleteGatewayArtifact(String apiId, String revision) throws APIManagementException {

        String deleteGwPublishedLabels = SQLConstants.DELETE_GW_PUBLISHED_LABELS;
        String deleteGWPublishedArtifacts = SQLConstants.DELETE_FROM_AM_GW_API_ARTIFACTS_WHERE_API_ID_AND_REVISION_ID;
        try (Connection connection = GatewayArtifactsMgtDBUtil.getArtifactSynchronizerConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement statement = connection.prepareStatement(deleteGwPublishedLabels)) {
                    statement.setString(1, apiId);
                    statement.setString(2, revision);
                    statement.executeUpdate();
                }
                try (PreparedStatement preparedStatement = connection.prepareStatement(deleteGWPublishedArtifacts)) {
                    preparedStatement.setString(1, apiId);
                    preparedStatement.setString(2, revision);
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

    public void deleteGatewayArtifacts(String apiId) throws APIManagementException {

        String deleteGwPublishedLabels = SQLConstants.DELETE_GW_PUBLISHED_LABELS_BY_API_ID;
        String deleteGWPublishedArtifacts = SQLConstants.DELETE_FROM_AM_GW_API_ARTIFACTS_BY_API_ID;
        String deleteGWArtifact = SQLConstants.DELETE_GW_PUBLISHED_API_DETAILS;
        try (Connection connection = GatewayArtifactsMgtDBUtil.getArtifactSynchronizerConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement statement = connection.prepareStatement(deleteGwPublishedLabels)) {
                    statement.setString(1, apiId);
                    statement.executeUpdate();
                }
                try (PreparedStatement preparedStatement = connection.prepareStatement(deleteGWPublishedArtifacts)) {
                    preparedStatement.setString(1, apiId);
                    preparedStatement.executeUpdate();
                }
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

    public List<APIRuntimeArtifactDto> retrieveGatewayArtifactsByAPIIDAndLabel(String apiId, String label)
            throws APIManagementException {

        String query = SQLConstants.RETRIEVE_ARTIFACTS_BY_APIID_AND_LABEL;
        List<APIRuntimeArtifactDto> apiRuntimeArtifactDtoList = new ArrayList<>();
        try (Connection connection = GatewayArtifactsMgtDBUtil.getArtifactSynchronizerConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, apiId);
            preparedStatement.setString(2, label);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    APIRuntimeArtifactDto apiRuntimeArtifactDto = new APIRuntimeArtifactDto();
                    apiRuntimeArtifactDto.setTenantDomain(resultSet.getString("TENANT_DOMAIN"));
                    apiRuntimeArtifactDto.setApiId(apiId);
                    apiRuntimeArtifactDto.setLabel(label);
                    apiRuntimeArtifactDto.setName(resultSet.getString("API_NAME"));
                    apiRuntimeArtifactDto.setVersion(resultSet.getString("API_VERSION"));
                    apiRuntimeArtifactDto.setProvider(resultSet.getString("API_PROVIDER"));
                    apiRuntimeArtifactDto.setRevision(resultSet.getString("REVISION_ID"));
                    apiRuntimeArtifactDto.setType(resultSet.getString("API_TYPE"));
                    InputStream artifact = resultSet.getBinaryStream("ARTIFACT");
                    apiRuntimeArtifactDto.setArtifact(artifact);
                    apiRuntimeArtifactDto.setFile(true);
                    apiRuntimeArtifactDtoList.add(apiRuntimeArtifactDto);
                }
            }
        } catch (SQLException e) {
            handleException("Failed to delete Gateway Artifact for Api : " + apiId + " and label: " + label, e);
        }

        return apiRuntimeArtifactDtoList;
    }

    public List<APIRuntimeArtifactDto> retrieveGatewayArtifactsByLabel(String label)
            throws APIManagementException {

        String query = SQLConstants.RETRIEVE_ARTIFACTS_BY_LABEL;
        List<APIRuntimeArtifactDto> apiRuntimeArtifactDtoList = new ArrayList<>();
        try (Connection connection = GatewayArtifactsMgtDBUtil.getArtifactSynchronizerConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, label);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    APIRuntimeArtifactDto apiRuntimeArtifactDto = new APIRuntimeArtifactDto();
                    apiRuntimeArtifactDto.setTenantDomain(resultSet.getString("TENANT_DOMAIN"));
                    apiRuntimeArtifactDto.setApiId(resultSet.getString("API_ID"));
                    apiRuntimeArtifactDto.setLabel(label);
                    apiRuntimeArtifactDto.setName(resultSet.getString("API_NAME"));
                    apiRuntimeArtifactDto.setVersion(resultSet.getString("API_VERSION"));
                    apiRuntimeArtifactDto.setProvider(resultSet.getString("API_PROVIDER"));
                    apiRuntimeArtifactDto.setRevision(resultSet.getString("REVISION_ID"));
                    apiRuntimeArtifactDto.setType(resultSet.getString("API_TYPE"));
                    InputStream artifact = resultSet.getBinaryStream("ARTIFACT");
                    apiRuntimeArtifactDto.setArtifact(artifact);
                    apiRuntimeArtifactDto.setFile(true);
                    apiRuntimeArtifactDtoList.add(apiRuntimeArtifactDto);
                }
            }
        } catch (SQLException e) {
            handleException("Failed to retrieve Gateway Artifact for label : " + label, e);
        }

        return apiRuntimeArtifactDtoList;
    }

    public List<APIRuntimeArtifactDto> retrieveGatewayArtifacts()
            throws APIManagementException {

        String query = SQLConstants.RETRIEVE_ARTIFACTS;
        List<APIRuntimeArtifactDto> apiRuntimeArtifactDtoList = new ArrayList<>();
        try (Connection connection = GatewayArtifactsMgtDBUtil.getArtifactSynchronizerConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    APIRuntimeArtifactDto apiRuntimeArtifactDto = new APIRuntimeArtifactDto();
                    apiRuntimeArtifactDto.setTenantDomain(resultSet.getString("TENANT_DOMAIN"));
                    apiRuntimeArtifactDto.setApiId(resultSet.getString("API_ID"));
                    apiRuntimeArtifactDto.setLabel(resultSet.getString("LABEL"));
                    apiRuntimeArtifactDto.setName(resultSet.getString("API_NAME"));
                    apiRuntimeArtifactDto.setVersion(resultSet.getString("API_VERSION"));
                    apiRuntimeArtifactDto.setProvider(resultSet.getString("API_PROVIDER"));
                    apiRuntimeArtifactDto.setRevision(resultSet.getString("REVISION_ID"));
                    apiRuntimeArtifactDto.setType(resultSet.getString("API_TYPE"));
                    InputStream artifact = resultSet.getBinaryStream("ARTIFACT");
                    apiRuntimeArtifactDto.setArtifact(artifact);
                    apiRuntimeArtifactDto.setFile(true);
                    apiRuntimeArtifactDtoList.add(apiRuntimeArtifactDto);
                }
            }
        } catch (SQLException e) {
            handleException("Failed to retrieve Gateway Artifact", e);
        }

        return apiRuntimeArtifactDtoList;
    }
}

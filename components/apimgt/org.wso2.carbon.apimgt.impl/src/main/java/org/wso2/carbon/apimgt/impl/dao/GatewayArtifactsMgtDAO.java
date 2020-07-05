package org.wso2.carbon.apimgt.impl.dao;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.constants.SQLConstants;
import org.wso2.carbon.apimgt.impl.utils.GatewayArtifactsMgtDBUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GatewayArtifactsMgtDAO {
    private static final Log log = LogFactory.getLog(GatewayArtifactsMgtDAO.class);
    private static GatewayArtifactsMgtDAO gatewayArtifactsMgtDAO = null;

    /**
     * Private constructor
     */
    private GatewayArtifactsMgtDAO () {
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
     * @param APIId        - UUID of the API
     * @param APIName      - Name of the API
     * @param version      - Version of the API
     * @param tenantDomain - Tenant domain of the API
     * @throws APIManagementException if an error occurs
     */
    public void addGatewayPublishedAPIDetails(String APIId, String APIName, String version, String tenantDomain)
            throws APIManagementException {

        try (Connection connection = GatewayArtifactsMgtDBUtil.getArtifactSynchronizerConnection()) {
            connection.setAutoCommit(false);
                try (PreparedStatement statement = connection
                        .prepareStatement(SQLConstants.ADD_GW_PUBLISHED_API_DETAILS)) {
                    statement.setString(1, APIId);
                    statement.setString(2, APIName);
                    statement.setString(3, version);
                    statement.setString(4, tenantDomain);
                    statement.executeUpdate();
                }
            connection.commit();
        } catch (SQLException e) {
            handleException("Failed to add API details for " + APIName, e);
        }
    }

    /**
     * Add or update details of the APIs published in the Gateway
     *
     * @param APIId        - UUID of the API
     * @param gatewayLabel - Published gateway's label
     * @param bais         - Byte array Input stream of the serializide gatewayAPIDTO
     * @param streamLength - Length of the stream
     * @throws APIManagementException if an error occurs
     */
    public void addGatewayPublishedAPIArtifacts(String APIId, String gatewayLabel, ByteArrayInputStream bais,
            int streamLength, String gatewayInstruction, String query)
            throws APIManagementException {

        try (Connection connection = GatewayArtifactsMgtDBUtil.getArtifactSynchronizerConnection()) {
            connection.setAutoCommit(false);
                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.setBinaryStream(1, bais, streamLength);
                    statement.setString(2, gatewayInstruction);
                    statement.setString(3, APIId);
                    statement.setString(4, gatewayLabel);
                    statement.executeUpdate();
                }
            connection.commit();
        } catch (SQLException e) {
            handleException("Failed to add artifacts for " + APIId, e);
        }
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

        String gatewayRuntimeArtifacts = null;
        try (Connection connection = GatewayArtifactsMgtDBUtil.getArtifactSynchronizerConnection();
                PreparedStatement statement = connection.prepareStatement(SQLConstants.GET_API_ARTIFACT)) {
            connection.setAutoCommit(false);
            connection.commit();
            statement.setString(1, APIId);
            statement.setString(2, gatewayLabel);
            statement.setString(3, gatewayInstruction);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                try (InputStream inputStream = rs.getBinaryStream(1)) {
                    gatewayRuntimeArtifacts = IOUtils.toString(inputStream, APIConstants.DigestAuthConstants.CHARSET);
                } catch (IOException  e){
                    handleException("Error in generating gatewayRuntimeArtifacts " + APIId, e);
                }
            }
        } catch (SQLException e) {
            handleException("Failed to get artifacts of API with ID " + APIId, e);
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

        List<String> gatewayRuntimeArtifactsArray = new ArrayList<>();
        try (Connection connection = GatewayArtifactsMgtDBUtil.getArtifactSynchronizerConnection();
                PreparedStatement statement = connection.prepareStatement(SQLConstants.GET_ALL_API_ARTIFACT)) {
            connection.setAutoCommit(false);
            connection.commit();
            statement.setString(1, label);
            statement.setString(2, APIConstants.GatewayArtifactSynchronizer.GATEWAY_INSTRUCTION_PUBLISH);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                try (InputStream inputStream = rs.getBinaryStream(1)) {
                    String gatewayRuntimeArtifacts = IOUtils.toString(inputStream,
                            APIConstants.DigestAuthConstants.CHARSET);
                    gatewayRuntimeArtifactsArray.add(gatewayRuntimeArtifacts);
                } catch (IOException  e) {
                    handleException("Error in generating gatewayRuntimeArtifacts ", e);
                }
            }
        } catch (SQLException e) {
            handleException("Failed to get artifacts " , e);
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
        try (Connection connection = GatewayArtifactsMgtDBUtil.getArtifactSynchronizerConnection();
                PreparedStatement statement = connection
                        .prepareStatement(SQLConstants.GET_PUBLISHED_GATEWAYS_FOR_API)) {
            connection.setAutoCommit(false);
            connection.commit();
            statement.setString(1, APIId);
            statement.setString(2, APIConstants.GatewayArtifactSynchronizer.GATEWAY_INSTRUCTION_PUBLISH);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                count = rs.getInt("COUNT");
            }
        } catch (SQLException e) {
            handleException("Failed check whether API is published in any gateway " + APIId, e);
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

        try (Connection connection = GatewayArtifactsMgtDBUtil.getArtifactSynchronizerConnection();
                PreparedStatement statement = connection
                        .prepareStatement(SQLConstants.CHECK_API_EXISTS)) {
            connection.setAutoCommit(false);
            connection.commit();
            statement.setString(1, APIId);
            ResultSet rs = statement.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            handleException("Failed to check API details status of API with ID " + APIId, e);
        }
        return false;
    }

    /**
     * Check whether the API artifact for given label exists in the db
     *
     * @param APIId - UUID of the API
     * @throws APIManagementException if an error occurs
     */
    public boolean isAPIArtifactExists(String APIId, String gatewayLabel) throws APIManagementException {

        try (Connection connection = GatewayArtifactsMgtDBUtil.getArtifactSynchronizerConnection();
                PreparedStatement statement = connection.prepareStatement(SQLConstants.CHECK_ARTIFACT_EXISTS)) {
            connection.setAutoCommit(false);
            connection.commit();
            statement.setString(1, APIId);
            statement.setString(2, gatewayLabel);
            ResultSet rs = statement.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            handleException("Failed to check API artifact status of API with ID " + APIId + " for label "
                    + gatewayLabel, e);
        }
        return false;
    }

    private void handleException(String msg, Throwable t) throws APIManagementException {
        log.error(msg, t);
        throw new APIManagementException(msg, t);
    }

}

package org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.gateway.GatewayAPIDTO;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.exception.ArtifactSynchronizerException;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.exception.ConnectionUnavailableException;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.exception.TestConnectionNotSupportedException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Set;

public class DBPublisher implements ArtifactPublisher {

    private static final Log log = LogFactory.getLog(DBPublisher.class);
    protected ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();

    @Override
    public void init() throws ArtifactSynchronizerException {
        //not required
    }

    @Override
    public void testConnect() throws TestConnectionNotSupportedException, ConnectionUnavailableException {
        //not required
    }

    @Override
    public void connect() throws ConnectionUnavailableException {
        //not required
    }

    @Override
    public void publishArtifacts (GatewayAPIDTO gatewayAPIDTO)
            throws ArtifactSynchronizerException {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(gatewayAPIDTO);
            byte[] gatewayAPIDTOAsBytes = byteArrayOutputStream.toByteArray();
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(gatewayAPIDTOAsBytes);
            apiMgtDAO.addGatewayPublishedAPIDetails(gatewayAPIDTO);
            apiMgtDAO.addGatewayPublishedAPIArtifacts(gatewayAPIDTO, byteArrayInputStream, gatewayAPIDTOAsBytes.length);
            if (log.isDebugEnabled()){
                log.debug("Successfully published Artifacts of " + gatewayAPIDTO.getName());
            }
        } catch (IOException | APIManagementException e) {
            throw new ArtifactSynchronizerException("Error publishing Artifact of " + gatewayAPIDTO.getName() +
                    " API from DB", e);
        }

    }

    @Override
    public void updateArtifacts(GatewayAPIDTO gatewayAPIDTO, String artifactType)
            throws ArtifactSynchronizerException {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(gatewayAPIDTO);
            byte[] gatewayAPIDTOAsBytes = byteArrayOutputStream.toByteArray();
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(gatewayAPIDTOAsBytes);
            apiMgtDAO.updateGatewayPublishedAPIArtifacts(gatewayAPIDTO.getApiId(), gatewayAPIDTO.getGatewayLabel(),
                    byteArrayInputStream, gatewayAPIDTOAsBytes.length, artifactType);
            if (log.isDebugEnabled()) {
                log.debug("Successfully updated Artifacts of " + gatewayAPIDTO.getName());
            }
        } catch (APIManagementException | IOException e) {
            throw new ArtifactSynchronizerException ("Error updating Artifact of " + gatewayAPIDTO.getName() +
                    " API", e);
        }

    }

    @Override
    public Set<String> getExistingLabelsForAPI(String apiId) {
        Set<String> labelsSet = new HashSet<>();
        try {
            labelsSet = apiMgtDAO.getExistingLabelsForAPI(apiId);
        } catch (APIManagementException e) {
            log.error("Error getting labels for the API with ID " + apiId + " from DB", e);
        }

        return labelsSet;
    }

    @Override
    public void disconnect() {
        //not required
    }

    @Override
    public void destroy() {
        //not required
    }

    @Override
    public String getType() {

         return APIConstants.GatewayArtifactSynchronizer.DEFAULT_PUBLISHER_NAME;
    }
}

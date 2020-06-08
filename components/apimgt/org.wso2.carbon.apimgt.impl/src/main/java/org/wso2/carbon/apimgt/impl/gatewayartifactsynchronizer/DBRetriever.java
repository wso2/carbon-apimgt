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
import java.io.IOException;
import java.io.ObjectInputStream;

public class DBRetriever implements ArtifactRetriever {

    private static final Log log = LogFactory.getLog(DBRetriever.class);
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
    public GatewayAPIDTO retrieveArtifacts(String APIId, String APIName, String label)
            throws ArtifactSynchronizerException {

        GatewayAPIDTO gatewayAPIDTO = null;
        try {
            ByteArrayInputStream byteStream = apiMgtDAO.getGatewayPublishedAPIArtifacts(APIId, label);
            ObjectInputStream objectStream = new ObjectInputStream(byteStream);
            gatewayAPIDTO = (GatewayAPIDTO) objectStream.readObject();
            if (log.isDebugEnabled()) {
                log.debug("Successfully retrieved Artifacts of " + gatewayAPIDTO.getName());
            }
        } catch (APIManagementException | IOException | ClassNotFoundException e) {
            throw new ArtifactSynchronizerException("Error retrieving Artifact of " + APIName + " API from DB", e);
        }
        return gatewayAPIDTO;
    }

    @Override
    public void deleteArtifacts(GatewayAPIDTO gatewayAPIDTO) throws ArtifactSynchronizerException {

        try {
            apiMgtDAO.deleteGatewayPublishedAPIArtifacts(gatewayAPIDTO.getApiId(), gatewayAPIDTO.getGatewayLabel());
            apiMgtDAO.deleteGatewayPublishedAPIDetails(gatewayAPIDTO.getApiId());
            if (log.isDebugEnabled()) {
                log.debug("Successfully deleted Artifacts of " + gatewayAPIDTO.getName());
            }
        } catch (APIManagementException e) {
            throw new ArtifactSynchronizerException("Error deleting Artifacts of " + gatewayAPIDTO.getName()
                    + " API from DB", e);
        }

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

        return APIConstants.GatewayArtifactSynchronizer.DEFAULT_RETRIEVER_NAME;
    }
}

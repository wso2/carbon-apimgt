package org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.gateway.GatewayAPIDTO;
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
    public GatewayAPIDTO retrieveArtifacts(String APIId, String APIName, String label) throws ConnectionUnavailableException {

        GatewayAPIDTO gatewayAPIDTO = null;
        try {
            ByteArrayInputStream byteStream = apiMgtDAO.getAPIBlob(APIId, APIName, label);
            ObjectInputStream objectStream = new ObjectInputStream(byteStream);
            gatewayAPIDTO = (GatewayAPIDTO) objectStream.readObject();
        } catch (APIManagementException | IOException | ClassNotFoundException e) {
            throw new ConnectionUnavailableException("Error retrieving Artifact of " + APIName + " API from DB", e);
        }
        return gatewayAPIDTO;
    }

    @Override
    public void disconnect() {
        //not required
    }

    @Override
    public void destroy() {
        //not required
    }
}

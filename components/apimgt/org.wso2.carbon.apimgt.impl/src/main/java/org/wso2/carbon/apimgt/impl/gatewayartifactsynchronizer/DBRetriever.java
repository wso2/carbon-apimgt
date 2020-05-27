package org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.gateway.GatewayAPIDTO;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class DBDeployer implements ArtifactDeployer {

    private static final Log log = LogFactory.getLog(DBDeployer.class);
    protected ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();

    @Override
    public GatewayAPIDTO deployArtifact (String APIId, String APIName, String label) {

        GatewayAPIDTO gatewayAPIDTO = null;
        try {
            ByteArrayInputStream byteStream = apiMgtDAO.getAPIBlob(APIId, APIName, label);
            ObjectInputStream objectStream = new ObjectInputStream(byteStream);
            gatewayAPIDTO = (GatewayAPIDTO) objectStream.readObject();
        } catch (APIManagementException | IOException | ClassNotFoundException e) {
            log.error("Error deploying Artifact of " + APIName + " API", e);
        }
        return gatewayAPIDTO;
    }
}

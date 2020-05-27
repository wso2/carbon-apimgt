package org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.gateway.GatewayAPIDTO;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;


public class DBPublisher implements ArtifactPublisher {

    private static final Log log = LogFactory.getLog(DBPublisher.class);
    protected ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();

    @Override
    public void publishArtifacts (GatewayAPIDTO gatewayAPIDTO) {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(gatewayAPIDTO);
            byte[] gatewayAPIDTOAsBytes = byteArrayOutputStream.toByteArray();
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(gatewayAPIDTOAsBytes);
            apiMgtDAO.addAPIBlob(gatewayAPIDTO.getApiId(), gatewayAPIDTO.getName(), gatewayAPIDTO.getEnvironment(),
                    byteArrayInputStream, gatewayAPIDTOAsBytes.length);

        } catch (IOException | APIManagementException e) {
            log.error("Error publishing Artifact of " + gatewayAPIDTO.getName() + " API from DB", e);
        }

    }
}

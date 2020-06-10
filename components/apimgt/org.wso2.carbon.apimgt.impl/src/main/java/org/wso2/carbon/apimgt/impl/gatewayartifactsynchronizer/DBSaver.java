package org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer;

import com.google.gson.Gson;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.gateway.GatewayAPIDTO;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.exception.ArtifactSynchronizerException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Set;

public class DBSaver implements ArtifactSaver {

    private static final Log log = LogFactory.getLog(DBSaver.class);
    protected ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();

    @Override
    public void init() throws ArtifactSynchronizerException {
        //not required
    }

    @Override
    public void saveArtifact(GatewayAPIDTO gatewayAPIDTO)
            throws ArtifactSynchronizerException {

        try {
            byte[] gatewayAPIDTOAsBytes = Base64.encodeBase64(new Gson().toJson(gatewayAPIDTO).getBytes());
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(gatewayAPIDTOAsBytes);
            if (!apiMgtDAO.isAPIDetailsExists(gatewayAPIDTO.getApiId())) {
                apiMgtDAO.addGatewayPublishedAPIDetails(gatewayAPIDTO);
            }
            apiMgtDAO.addGatewayPublishedAPIArtifacts(gatewayAPIDTO, byteArrayInputStream, gatewayAPIDTOAsBytes.length);
            if (log.isDebugEnabled()) {
                log.debug("Successfully saved Artifacts of " + gatewayAPIDTO.getName());
            }
        } catch (APIManagementException e) {
            throw new ArtifactSynchronizerException("Error saving Artifact of " + gatewayAPIDTO.getName() +
                    " API to the DB", e);
        }

    }

    @Override
    public void updateArtifact(GatewayAPIDTO gatewayAPIDTO, String gatewayInstruction)
            throws ArtifactSynchronizerException {

        try {
            byte[] gatewayAPIDTOAsBytes = Base64.encodeBase64(new Gson().toJson(gatewayAPIDTO).getBytes());
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(gatewayAPIDTOAsBytes);
            apiMgtDAO.updateGatewayPublishedAPIArtifacts(gatewayAPIDTO.getApiId(), gatewayAPIDTO.getGatewayLabel(),
                    byteArrayInputStream, gatewayAPIDTOAsBytes.length, gatewayInstruction);
            if (log.isDebugEnabled()) {
                log.debug("Successfully updated Artifacts of " + gatewayAPIDTO.getName());
            }
        } catch (APIManagementException e) {
            throw new ArtifactSynchronizerException("Error updating Artifact of " + gatewayAPIDTO.getName() +
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
    public String getName() {

        return APIConstants.GatewayArtifactSynchronizer.DB_SAVER_NAME;
    }
}

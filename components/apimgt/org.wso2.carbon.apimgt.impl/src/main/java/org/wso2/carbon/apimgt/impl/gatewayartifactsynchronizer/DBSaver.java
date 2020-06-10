package org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dao.constants.SQLConstants;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.exception.ArtifactSynchronizerException;

import java.io.ByteArrayInputStream;

public class DBSaver implements ArtifactSaver {

    private static final Log log = LogFactory.getLog(DBSaver.class);
    protected ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();

    @Override
    public void init() throws ArtifactSynchronizerException {
        //not required
    }

    @Override
    public void saveArtifact(String gatewayRuntimeArtifacts, String gatewayInstruction)
            throws ArtifactSynchronizerException {

        try {
            JSONObject artifactObject = new JSONObject(gatewayRuntimeArtifacts);
            String apiId = (String) artifactObject.get("apiId");
            String apiName = (String) artifactObject.get("name");
            String version = (String) artifactObject.get("version");
            String tenantDomain = (String) artifactObject.get("tenantDomain");
            String gatewayLabel = (String) artifactObject.get("gatewayLabel");

            byte[] gatewayRuntimeArtifactsAsBytes = gatewayRuntimeArtifacts.getBytes();
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(gatewayRuntimeArtifactsAsBytes);
            if (!apiMgtDAO.isAPIDetailsExists(apiId)) {
                apiMgtDAO.addGatewayPublishedAPIDetails(apiId, apiName,
                        version, tenantDomain);
            }

            String dbQuery;
            if (apiMgtDAO.isAPIArtifactExists(apiId, gatewayLabel)) {
                dbQuery = SQLConstants.UPDATE_API_ARTIFACT;
            } else {
                dbQuery = SQLConstants.ADD_GW_API_ARTIFACT;
            }
            apiMgtDAO.addGatewayPublishedAPIArtifacts(apiId, gatewayLabel,
                    byteArrayInputStream, gatewayRuntimeArtifactsAsBytes.length, gatewayInstruction, dbQuery);

            if (log.isDebugEnabled()) {
                log.debug("Successfully saved Artifacts of " + apiName);
            }
        } catch (APIManagementException e) {
            throw new ArtifactSynchronizerException("Error saving Artifacts to the DB", e);
        }

    }

    @Override
    public boolean isAPIPublished(String apiId) {

        try {
            return apiMgtDAO.isAPIPublishedInAnyGateway(apiId);
        } catch (APIManagementException e) {
            log.error("Error checking API with ID " + apiId + " is published in any gateway", e);
        }
        return false;
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

package org.wso2.carbon.apimgt.rest.api.gateway.v1.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.gateway.InMemoryAPIDeployer;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.exception.ArtifactSynchronizerException;
import org.wso2.carbon.apimgt.rest.api.gateway.v1.RedeployApiApiService;
import org.wso2.carbon.apimgt.rest.api.gateway.v1.dto.DeployResponseDTO;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.util.Map;

import javax.ws.rs.core.Response;

public class RedeployApiApiServiceImpl implements RedeployApiApiService {

    private static final Log log = LogFactory.getLog(RedeployApiApiServiceImpl.class);
    private boolean debugEnabled = log.isDebugEnabled();

    public Response redeployApiPost(String apiName, String version, String tenantDomain,
                                    MessageContext messageContext) {

        InMemoryAPIDeployer inMemoryApiDeployer = new InMemoryAPIDeployer();
        if (tenantDomain == null) {
            tenantDomain = APIConstants.SUPER_TENANT_DOMAIN;
        }
        DeployResponseDTO deployResponseDTO = new DeployResponseDTO();
        boolean status = false;
        try {
            Map<String, String> apiAttributes =
                    inMemoryApiDeployer.getGatewayAPIAttributes(apiName, version, tenantDomain);
            String apiId = apiAttributes.get(APIConstants.GatewayArtifactSynchronizer.API_ID);
            String label = apiAttributes.get(APIConstants.GatewayArtifactSynchronizer.LABEL);

            if (label == null) {
                return Response.status(Response.Status.BAD_REQUEST).entity(apiName + " is not deployed in the Gateway")
                        .build();
            }
            status = inMemoryApiDeployer.deployAPI(apiId, label);
        } catch (ArtifactSynchronizerException e) {
            String errorMessage = "Error in fetching artifacts from storage";
            log.error(errorMessage, e);
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }

        if (status) {
            if (debugEnabled) {
                log.debug("Successfully deployed " + apiName + " in gateway");
            }
            deployResponseDTO.deployStatus(DeployResponseDTO.DeployStatusEnum.DEPLOYED);
            deployResponseDTO.setMessage(apiName + " redeployed from the gateway");
            return Response.ok().entity(deployResponseDTO).build();
        } else {
            return Response.serverError().entity("Unexpected error occurred").build();
        }
    }
}

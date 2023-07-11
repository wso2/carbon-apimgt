package org.wso2.carbon.apimgt.gateway;

import com.google.gson.Gson;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.gateway.GatewayAPIDTO;
import org.wso2.carbon.apimgt.api.gateway.GatewayPolicyDTO;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.gateway.service.APIGatewayAdmin;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.ArtifactRetriever;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.exception.ArtifactSynchronizerException;

import java.util.Set;

public class GatewayPolicyDeployer {
    private static final Log log = LogFactory.getLog(GatewayPolicyDeployer.class);
    ArtifactRetriever artifactRetriever;
    private String gatewayPolicyMappingUuid;

    public GatewayPolicyDeployer(String gatewayPolicyMappingUuid) {
        this.artifactRetriever = ServiceReferenceHolder.getInstance().getArtifactRetriever();
        this.gatewayPolicyMappingUuid = gatewayPolicyMappingUuid;
    }

    public void deployGatewayPolicyMapping() throws ArtifactSynchronizerException {
        try {
            GatewayPolicyDTO gatewayPolicyDTO = retrieveGatewayPolicyArtifact(gatewayPolicyMappingUuid);
            if (gatewayPolicyDTO != null) {
                APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdmin();
                MessageContext.setCurrentMessageContext(
                        org.wso2.carbon.apimgt.gateway.utils.GatewayUtils.createAxis2MessageContext());
                apiGatewayAdmin.deployGatewayPolicy(gatewayPolicyDTO);
            }
        } catch (AxisFault e) {
            throw new RuntimeException(e);
        }
    }

    public void undeployGatewayPolicyMapping() {

    }

    private GatewayPolicyDTO retrieveGatewayPolicyArtifact(String policyMappingUUUID)
            throws ArtifactSynchronizerException {

        GatewayPolicyDTO result;

        if (artifactRetriever != null) {
            try {
                String gatewayRuntimeArtifact = artifactRetriever.retrieveGatewayPolicyArtifact(policyMappingUUUID);
                if (StringUtils.isNotEmpty(gatewayRuntimeArtifact)) {
                    result = new Gson().fromJson(gatewayRuntimeArtifact, GatewayPolicyDTO.class);
                } else {
                    String msg = "Error retrieving artifacts for policy mapping UUID " + policyMappingUUUID +
                             ". Storage returned null";
                    log.error(msg);
                    throw new ArtifactSynchronizerException(msg);
                }
            } catch (ArtifactSynchronizerException e) {
                String msg = "Error deploying policy mapping " + policyMappingUUUID + " in Gateway";
                log.error(msg, e);
                throw new ArtifactSynchronizerException(msg, e);
            }
        } else {
            String msg = "Artifact retriever not found";
            log.error(msg);
            throw new ArtifactSynchronizerException(msg);
        }
        return result;
    }
}

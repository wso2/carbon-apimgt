package org.wso2.carbon.apimgt.rest.api.publisher.v1.common;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.dto.APIRuntimeArtifactDto;
import org.wso2.carbon.apimgt.impl.dto.RuntimeArtifactDto;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.GatewayArtifactGenerator;

import java.util.List;

public class SynapseArtifactGenerator implements GatewayArtifactGenerator {

    @Override
    public RuntimeArtifactDto generateGatewayArtifact(List<APIRuntimeArtifactDto> apiRuntimeArtifactDtoList)
            throws APIManagementException {

        return null;
    }

    @Override
    public String getType() {

        return "Synapse";
    }

}

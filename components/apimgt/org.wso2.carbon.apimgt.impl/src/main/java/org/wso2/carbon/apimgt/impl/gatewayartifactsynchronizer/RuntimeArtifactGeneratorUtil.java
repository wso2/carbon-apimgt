package org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer;

import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.impl.dao.GatewayArtifactsMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.APIRuntimeArtifactDto;
import org.wso2.carbon.apimgt.impl.dto.RuntimeArtifactDto;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

import java.util.ArrayList;
import java.util.List;

public class RuntimeArtifactGeneratorUtil {

    private static final GatewayArtifactsMgtDAO gatewayArtifactsMgtDAO = GatewayArtifactsMgtDAO.getInstance();

    public static RuntimeArtifactDto generateRuntimeArtifact(String apiId, String gatewayLabel, String type)
            throws APIManagementException {

        GatewayArtifactGenerator gatewayArtifactGenerator =
                ServiceReferenceHolder.getInstance().getGatewayArtifactGenerator(type);
        if (gatewayArtifactGenerator != null) {
            List<APIRuntimeArtifactDto> gatewayArtifacts;
            if (StringUtils.isNotEmpty(gatewayLabel)) {
                if (StringUtils.isNotEmpty(apiId)) {
                    gatewayArtifacts =
                            gatewayArtifactsMgtDAO.retrieveGatewayArtifactsByAPIIDAndLabel(apiId, gatewayLabel);
                } else {
                    gatewayArtifacts = gatewayArtifactsMgtDAO.retrieveGatewayArtifactsByLabel(gatewayLabel);
                }
            } else {
                gatewayArtifacts = gatewayArtifactsMgtDAO.retrieveGatewayArtifacts();
            }
            if (gatewayArtifacts != null){
                if (gatewayArtifacts.isEmpty()){
                    throw new APIManagementException("No API Artifacts", ExceptionCodes.NO_API_ARTIFACT_FOUND);
                }
            }
            return gatewayArtifactGenerator.generateGatewayArtifact(gatewayArtifacts);
        }
        return null;
    }

}

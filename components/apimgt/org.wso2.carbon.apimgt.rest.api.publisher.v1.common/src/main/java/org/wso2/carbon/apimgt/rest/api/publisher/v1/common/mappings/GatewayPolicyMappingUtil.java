package org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings;

import org.wso2.carbon.apimgt.api.model.GatewayPolicyDeployment;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.GatewayPolicyDeploymentDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is responsible for mapping Gateway Global Policy Objects into REST API Global Policy related DTOs
 * and vice versa.
 */
public class GatewayPolicyMappingUtil {
    public static Map<Boolean, List<GatewayPolicyDeployment>> fromDTOToGatewayPolicyDeploymentMap(
            List<GatewayPolicyDeploymentDTO> gatewayPolicyDeploymentDTO) {
        List<GatewayPolicyDeployment> gatewayPolicyDeploymentList = new ArrayList<>();
        List<GatewayPolicyDeployment> gatewayPolicyUndeploymentList = new ArrayList<>();
        for (GatewayPolicyDeploymentDTO gatewayPolicyDeploymentDTOEntry : gatewayPolicyDeploymentDTO) {
            GatewayPolicyDeployment gatewayPolicyDeployment = new GatewayPolicyDeployment();
            gatewayPolicyDeployment.setMappingUuid(gatewayPolicyDeploymentDTOEntry.getMappingUUID());
            gatewayPolicyDeployment.setGatewayLabel(gatewayPolicyDeploymentDTOEntry.getGatewayLabel());

            if (gatewayPolicyDeploymentDTOEntry.isGatewayDeployment()) {
                gatewayPolicyDeploymentList.add(gatewayPolicyDeployment);
            } else {
                gatewayPolicyUndeploymentList.add(gatewayPolicyDeployment);
            }
        }
        Map<Boolean, List<GatewayPolicyDeployment>> gatewayPolicyDeploymentMap = new HashMap<>();
        gatewayPolicyDeploymentMap.put(true, gatewayPolicyDeploymentList);
        gatewayPolicyDeploymentMap.put(false, gatewayPolicyUndeploymentList);
        return gatewayPolicyDeploymentMap;
    }
}

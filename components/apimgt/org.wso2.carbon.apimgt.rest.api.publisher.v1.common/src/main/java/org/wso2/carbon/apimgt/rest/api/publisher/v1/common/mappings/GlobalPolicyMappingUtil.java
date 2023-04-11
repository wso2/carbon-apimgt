package org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings;

import org.wso2.carbon.apimgt.api.model.OperationPolicy;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIOperationPoliciesDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GlobalPolicyMappingUtil {
    public static Map<String, List<OperationPolicy>> fromDTOToGlobalPoliciesList(
            Map<String, APIOperationPoliciesDTO> gatewayGlobalPolicyMap) {

        Map<String, List<OperationPolicy>> globalPoliciesToGatewayMap = new HashMap<>();

        for (Map.Entry<String, APIOperationPoliciesDTO> policies : gatewayGlobalPolicyMap.entrySet()) {
            globalPoliciesToGatewayMap.put(policies.getKey(),
                    OperationPolicyMappingUtil.fromDTOToAPIOperationPoliciesList(policies.getValue()));
        }

        return globalPoliciesToGatewayMap;
    }
}

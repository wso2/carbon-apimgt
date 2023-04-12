package org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings;

import org.wso2.carbon.apimgt.api.model.OperationPolicy;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIOperationPoliciesDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is responsible for mapping Gateway Global Policy Objects into REST API Global Policy related DTOs
 * and vice versa.
 */
public class GlobalPolicyMappingUtil {
    public static Map<String, List<OperationPolicy>> fromDTOToGlobalPoliciesToGatewayMap(
            Map<String, APIOperationPoliciesDTO> gatewayGlobalPolicyMap) {

        Map<String, List<OperationPolicy>> globalPoliciesToGatewayMap = new HashMap<>();

        for (Map.Entry<String, APIOperationPoliciesDTO> policiesOfDirections : gatewayGlobalPolicyMap.entrySet()) {
            globalPoliciesToGatewayMap.put(policiesOfDirections.getKey(),
                    OperationPolicyMappingUtil.fromDTOToAPIOperationPoliciesList(policiesOfDirections.getValue()));
        }

        return globalPoliciesToGatewayMap;
    }
}

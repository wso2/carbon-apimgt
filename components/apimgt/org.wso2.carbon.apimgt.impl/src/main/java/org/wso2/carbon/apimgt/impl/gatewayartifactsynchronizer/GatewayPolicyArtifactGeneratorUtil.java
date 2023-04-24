package org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.OperationPolicy;
import org.wso2.carbon.apimgt.api.model.OperationPolicyData;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.dto.RuntimeArtifactDto;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.context.CarbonContext;

import java.util.List;
import java.util.Set;

public class GatewayPolicyArtifactGeneratorUtil {

    public static RuntimeArtifactDto generateRuntimeArtifact(String policyMappingUuid, String type, String tenantDomain)
            throws APIManagementException {

        GatewayArtifactGenerator gatewayArtifactGenerator =
                ServiceReferenceHolder.getInstance().getGatewayArtifactGenerator(type);
        if (gatewayArtifactGenerator != null) {
            APIProvider apiProvider = APIManagerFactory.getInstance().getAPIProvider(CarbonContext.
                    getThreadLocalCarbonContext().getUsername());
            List<OperationPolicyData> gatewayPolicyDataList = apiProvider.getGatewayPolicyDataListByPolicyId(
                    policyMappingUuid, tenantDomain, true);
            List<OperationPolicy> gatewayPolicyList = apiProvider.getOperationPoliciesOfPolicyMapping(
                    policyMappingUuid);
            return gatewayArtifactGenerator.generateGatewayPolicyArtifact(gatewayPolicyDataList, gatewayPolicyList);
        } else {
            Set<String> gatewayArtifactGeneratorTypes =
                    ServiceReferenceHolder.getInstance().getGatewayArtifactGeneratorTypes();
            throw new APIManagementException("Couldn't find gateway Type",
                    ExceptionCodes.from(ExceptionCodes.GATEWAY_TYPE_NOT_FOUND, String.join(",",
                            gatewayArtifactGeneratorTypes)));
        }
    }
}

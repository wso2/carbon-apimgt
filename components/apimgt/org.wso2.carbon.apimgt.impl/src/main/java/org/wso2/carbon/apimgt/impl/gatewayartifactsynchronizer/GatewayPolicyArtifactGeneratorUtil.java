package org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.OperationPolicy;
import org.wso2.carbon.apimgt.api.model.OperationPolicyData;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.dto.GatewayPolicyArtifactDto;
import org.wso2.carbon.apimgt.impl.dto.RuntimeArtifactDto;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.context.CarbonContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class GatewayPolicyArtifactGeneratorUtil {

    public static RuntimeArtifactDto generateRuntimeArtifact(String policyMappingUuid, String type, String tenantDomain,
            String gatewayLabel) throws APIManagementException {

        GatewayArtifactGenerator gatewayArtifactGenerator =
                ServiceReferenceHolder.getInstance().getGatewayArtifactGenerator(type);
        List<GatewayPolicyArtifactDto> gatewayPolicyArtifactDtoList = new ArrayList<>();
        if (gatewayArtifactGenerator != null) {
            APIProvider apiProvider = APIManagerFactory.getInstance().getAPIProvider(CarbonContext.getThreadLocalCarbonContext()
                    .getUsername());
            List<String> policyMappingUuids;
            if (StringUtils.isNotEmpty(gatewayLabel)) {
                byte[] decodedValue = Base64.decodeBase64(gatewayLabel.getBytes());
                String[] gatewayLabels = new String(decodedValue).split("\\|");
                policyMappingUuids = apiProvider.getAllPolicyMappingUUIDsByGatewayLabels(gatewayLabels, tenantDomain);
            } else {
                policyMappingUuids = new ArrayList<>();
                policyMappingUuids.add(policyMappingUuid);
            }
            for (String mappingUuid : policyMappingUuids) {
                List<OperationPolicyData> gatewayPolicyDataList = apiProvider.getGatewayPolicyDataListByPolicyId(
                        mappingUuid, true);
                List<OperationPolicy> gatewayPolicyList = apiProvider.getOperationPoliciesOfPolicyMapping(
                        mappingUuid);
                GatewayPolicyArtifactDto gatewayPolicyArtifactDto = new GatewayPolicyArtifactDto();
                gatewayPolicyArtifactDto.setGatewayPolicyDataList(gatewayPolicyDataList);
                gatewayPolicyArtifactDto.setGatewayPolicyList(gatewayPolicyList);
                gatewayPolicyArtifactDto.setTenantDomain(tenantDomain);
                gatewayPolicyArtifactDtoList.add(gatewayPolicyArtifactDto);
            }
            return gatewayArtifactGenerator.generateGatewayPolicyArtifact(gatewayPolicyArtifactDtoList);
        } else {
            Set<String> gatewayArtifactGeneratorTypes =
                    ServiceReferenceHolder.getInstance().getGatewayArtifactGeneratorTypes();
            throw new APIManagementException("Couldn't find gateway Type",
                    ExceptionCodes.from(ExceptionCodes.GATEWAY_TYPE_NOT_FOUND, String.join(",",
                            gatewayArtifactGeneratorTypes)));
        }
    }
}

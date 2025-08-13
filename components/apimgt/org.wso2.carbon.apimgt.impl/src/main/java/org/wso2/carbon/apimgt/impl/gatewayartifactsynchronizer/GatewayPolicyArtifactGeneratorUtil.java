/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.OperationPolicy;
import org.wso2.carbon.apimgt.api.model.OperationPolicyData;
import org.wso2.carbon.apimgt.api.model.OperationPolicySpecAttribute;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.dto.GatewayPolicyArtifactDto;
import org.wso2.carbon.apimgt.impl.dto.RuntimeArtifactDto;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Util class for generating gateway policy artifacts.
 */
public class GatewayPolicyArtifactGeneratorUtil {

    /**
     * Generate runtime artifact for gateway policy mapping UUID.
     *
     * @param policyMappingUuid Policy mapping UUID
     * @param type              Type of the gateway policy
     * @param tenantDomain      Tenant domain
     * @param gatewayLabel      Gateway label
     * @return RuntimeArtifactDto Runtime artifact data object
     * @throws APIManagementException
     */
    public static RuntimeArtifactDto generateRuntimeArtifact(String policyMappingUuid, String type, String tenantDomain,
            String gatewayLabel) throws APIManagementException {

        GatewayArtifactGenerator gatewayArtifactGenerator =
                ServiceReferenceHolder.getInstance().getGatewayArtifactGenerator(type);
        List<GatewayPolicyArtifactDto> gatewayPolicyArtifactDtoList = new ArrayList<>();
        if (gatewayArtifactGenerator != null) {
            APIProvider apiProvider = APIManagerFactory.getInstance()
                    .getAPIProvider(CarbonContext.getThreadLocalCarbonContext().getUsername());
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

                // Create a map of policy data by policy ID for quick lookup
                Map<String, OperationPolicyData> policyDataMap = gatewayPolicyDataList.stream().collect(
                        Collectors.toMap(OperationPolicyData::getPolicyId, Function.identity(),
                                (existing, replacement) -> existing));

                // Iterate through the gateway policies and decrypt Secret type attributes
                CryptoUtil cryptoUtil = null;
                for (OperationPolicy gatewayPolicy : gatewayPolicyList) {
                    Map<String, Object> parameters = gatewayPolicy.getParameters();

                    // Check if parameters are not null and not empty
                    if (parameters != null && !parameters.isEmpty()) {
                        OperationPolicyData matchingPolicyData = policyDataMap.get(gatewayPolicy.getPolicyId());

                        // If policyData exists, check for Secret type attributes
                        if (matchingPolicyData != null && matchingPolicyData.getSpecification() != null) {
                            List<OperationPolicySpecAttribute> attributes = matchingPolicyData.getSpecification()
                                    .getPolicyAttributes();
                            if (attributes != null) {
                                for (OperationPolicySpecAttribute attribute : attributes) {
                                    // Check if the attribute type is Secret and if it exists in parameters
                                    if (attribute.getType() == OperationPolicySpecAttribute.AttributeType.Secret
                                            && parameters.containsKey(attribute.getName())) {
                                        // Decrypt the Secret type attributes
                                        Object encryptedValue = parameters.get(attribute.getName());
                                        if (encryptedValue != null && !encryptedValue.toString().isEmpty()) {
                                            if (cryptoUtil == null) {
                                                cryptoUtil = CryptoUtil.getDefaultCryptoUtil();
                                            }
                                            try {
                                                String decryptedValue = new String(
                                                        cryptoUtil.base64DecodeAndDecrypt(encryptedValue.toString()));
                                                // Replace the value with the decrypted value
                                                parameters.put(attribute.getName(), decryptedValue);
                                            } catch (CryptoException e) {
                                                throw new APIManagementException(
                                                        "Error decrypting the value of " + attribute.getName(), e);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

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

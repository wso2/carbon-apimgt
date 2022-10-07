/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.apk.apimgt.rest.api.admin.v1.common.utils.mappings.throttling;

import org.wso2.apk.apimgt.api.APIManagementException;
import org.wso2.apk.apimgt.api.UnsupportedThrottleConditionTypeException;
import org.wso2.apk.apimgt.api.UnsupportedThrottleLimitTypeException;
import org.wso2.apk.apimgt.api.model.policy.APIPolicy;
import org.wso2.apk.apimgt.api.model.policy.Pipeline;
import org.wso2.apk.apimgt.rest.api.admin.v1.dto.AdvancedThrottlePolicyDTO;
import org.wso2.apk.apimgt.rest.api.admin.v1.dto.AdvancedThrottlePolicyInfoDTO;
import org.wso2.apk.apimgt.rest.api.admin.v1.dto.AdvancedThrottlePolicyListDTO;
import org.wso2.apk.apimgt.rest.api.admin.v1.dto.ConditionalGroupDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for mapping Advanced Level Throttling model and its sub components into REST API DTOs
 * and vice-versa
 */
public class AdvancedThrottlePolicyMappingUtil {
    private static final String ADVANCED_THROTTLING_POLICY_TYPE = "AdvancedThrottlePolicy";
    private static final String ADVACNCED_THROTTLING_POLICY_INFO_TYPE = "AdvancedThrottlePolicyInfo";

    /**
     * Converts an array of Advanced Policy objects into a List DTO
     *
     * @param apiPolicies Array of Advanced Policies
     * @return A List DTO of converted Advanced Policies
     * @throws UnsupportedThrottleLimitTypeException
     * @throws UnsupportedThrottleConditionTypeException
     */
    public static AdvancedThrottlePolicyListDTO fromAPIPolicyArrayToListDTO(APIPolicy[] apiPolicies)
            throws APIManagementException {
        AdvancedThrottlePolicyListDTO listDTO = new AdvancedThrottlePolicyListDTO();
        List<AdvancedThrottlePolicyInfoDTO> advancedPolicyDTOs = new ArrayList<>();
        if (apiPolicies != null) {
            for (APIPolicy apiPolicy : apiPolicies) {
                advancedPolicyDTOs.add(fromAdvancedPolicyToInfoDTO(apiPolicy));
            }
        }
        listDTO.setList(advancedPolicyDTOs);
        listDTO.setCount(advancedPolicyDTOs.size());
        return listDTO;
    }

    /**
     * Converts a single Advanced Policy DTO into a model object
     *
     * @param dto Advanced policy DTO object
     * @return Converted Advanced policy model object
     * @throws APIManagementException
     */
    public static APIPolicy fromAdvancedPolicyDTOToPolicy(AdvancedThrottlePolicyDTO dto)
            throws APIManagementException {

        //update mandatory fields such as tenantDomain etc.
        dto = CommonThrottleMappingUtil.updateDefaultMandatoryFieldsOfThrottleDTO(dto);

        APIPolicy apiPolicy = new APIPolicy(dto.getPolicyName());
        apiPolicy = CommonThrottleMappingUtil.updateFieldsFromDTOToPolicy(dto, apiPolicy);

        List<Pipeline> pipelines = CommonThrottleMappingUtil.fromConditionalGroupDTOListToPipelineList(
                dto.getConditionalGroups());
        apiPolicy.setPipelines(pipelines);

        if (dto.getDefaultLimit() != null) {
            apiPolicy.setDefaultQuotaPolicy(CommonThrottleMappingUtil.fromDTOToQuotaPolicy(dto.getDefaultLimit()));
        }
        return apiPolicy;
    }

    /**
     * Converts a single Advanced Policy model into REST API DTO
     *
     * @param apiPolicy Advanced Policy model object
     * @return Converted Advanced policy REST API DTO object
     * @throws APIManagementException
     */
    public static AdvancedThrottlePolicyDTO fromAdvancedPolicyToDTO(APIPolicy apiPolicy)
            throws APIManagementException {
        AdvancedThrottlePolicyDTO policyDTO = new AdvancedThrottlePolicyDTO();
        policyDTO = CommonThrottleMappingUtil.updateFieldsFromToPolicyToDTO(apiPolicy, policyDTO);
        List<ConditionalGroupDTO> groupDTOs = CommonThrottleMappingUtil.fromPipelineListToConditionalGroupDTOList(
                apiPolicy.getPipelines());
        policyDTO.setConditionalGroups(groupDTOs);
        policyDTO.setType(ADVANCED_THROTTLING_POLICY_TYPE);

        if (apiPolicy.getDefaultQuotaPolicy() != null) {
            policyDTO.setDefaultLimit(CommonThrottleMappingUtil.fromQuotaPolicyToDTO(apiPolicy.getDefaultQuotaPolicy()));
        }
        return policyDTO;
    }

    /**
     * Converts a single Advanced Policy model into REST API DTO
     *
     * @param apiPolicy Advanced Policy model object
     * @return Converted Advanced policy REST API DTO object
     * @throws UnsupportedThrottleLimitTypeException
     */
    public static AdvancedThrottlePolicyInfoDTO fromAdvancedPolicyToInfoDTO(APIPolicy apiPolicy)
            throws APIManagementException {
        AdvancedThrottlePolicyInfoDTO policyDTO = new AdvancedThrottlePolicyInfoDTO();
        policyDTO = CommonThrottleMappingUtil.updateFieldsFromToPolicyToDTO(apiPolicy, policyDTO);
        if (apiPolicy.getDefaultQuotaPolicy() != null) {
            policyDTO.setDefaultLimit(
                    CommonThrottleMappingUtil.fromQuotaPolicyToDTO(apiPolicy.getDefaultQuotaPolicy()));
        }
        policyDTO.setType(ADVACNCED_THROTTLING_POLICY_INFO_TYPE);
        return policyDTO;
    }
}

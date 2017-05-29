/*
 *
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.wso2.carbon.apimgt.rest.api.admin.mappings;

import org.wso2.carbon.apimgt.core.models.policy.APIPolicy;
import org.wso2.carbon.apimgt.core.models.policy.Pipeline;
import org.wso2.carbon.apimgt.rest.api.admin.dto.AdvancedThrottlePolicyDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.AdvancedThrottlePolicyListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ConditionalGroupDTO;
import org.wso2.carbon.apimgt.rest.api.admin.exceptions.UnsupportedThrottleConditionTypeException;
import org.wso2.carbon.apimgt.rest.api.admin.exceptions.UnsupportedThrottleLimitTypeException;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for mapping Advanced Level Throttling model and its sub components into REST API DTOs
 * and vice-versa
 */
public class AdvancedThrottlePolicyMappingUtil {

    /**
     * Converts an array of Advanced Policy objects into a List DTO
     *
     * @param apiPolicies Array of Advanced Policies
     * @return A List DTO of converted Advanced Policies
     * @throws UnsupportedThrottleLimitTypeException
     * @throws UnsupportedThrottleConditionTypeException
     */
    public static AdvancedThrottlePolicyListDTO fromAPIPolicyArrayToListDTO(List<APIPolicy> apiPolicies)
            throws UnsupportedThrottleLimitTypeException, UnsupportedThrottleConditionTypeException {
        AdvancedThrottlePolicyListDTO listDTO = new AdvancedThrottlePolicyListDTO();
        List<AdvancedThrottlePolicyDTO> advancedPolicyDTOs = new ArrayList<>();
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
     * @throws UnsupportedThrottleLimitTypeException
     * @throws UnsupportedThrottleConditionTypeException
     */
    public static APIPolicy fromAdvancedPolicyDTOToPolicy(AdvancedThrottlePolicyDTO dto)
            throws UnsupportedThrottleLimitTypeException, UnsupportedThrottleConditionTypeException {

        APIPolicy apiPolicy = new APIPolicy(dto.getPolicyName());
        apiPolicy = CommonThrottleMappingUtil.updateFieldsFromDTOToPolicy(dto, apiPolicy);

        List<Pipeline> pipelines = CommonThrottleMappingUtil.fromConditionalGroupDTOListToPipelineList(
                dto.getConditionalGroups());
        apiPolicy.setPipelines(pipelines);

        if (dto.getQuotaPolicy() != null) {
            apiPolicy.setDefaultQuotaPolicy(CommonThrottleMappingUtil.fromDTOToQuotaPolicy(dto.getQuotaPolicy()));
        }
        return apiPolicy;
    }

    /**
     * Converts a single Advanced Policy model into REST API DTO
     *
     * @param apiPolicy Advanced Policy model object
     * @return Converted Advanced policy REST API DTO object
     * @throws UnsupportedThrottleLimitTypeException
     * @throws UnsupportedThrottleConditionTypeException
     */
    public static AdvancedThrottlePolicyDTO fromAdvancedPolicyToDTO(APIPolicy apiPolicy)
            throws UnsupportedThrottleLimitTypeException, UnsupportedThrottleConditionTypeException {
        AdvancedThrottlePolicyDTO policyDTO = new AdvancedThrottlePolicyDTO();
        policyDTO = CommonThrottleMappingUtil.updateFieldsFromToPolicyToDTO(apiPolicy, policyDTO);
        List<ConditionalGroupDTO> groupDTOs = CommonThrottleMappingUtil.fromPipelineListToConditionalGroupDTOList(
                apiPolicy.getPipelines());
        policyDTO.setConditionalGroups(groupDTOs);

        if (apiPolicy.getDefaultQuotaPolicy() != null) {
            policyDTO.setQuotaPolicy(CommonThrottleMappingUtil.fromQuotaPolicyToDTO(apiPolicy.getDefaultQuotaPolicy()));
        }
        return policyDTO;
    }

    /**
     * Converts a single Advanced Policy model into REST API DTO
     *
     * @param apiPolicy Advanced Policy model object
     * @return Converted Advanced policy REST API DTO object
     * @throws UnsupportedThrottleLimitTypeException
     * @throws UnsupportedThrottleConditionTypeException
     */
    public static AdvancedThrottlePolicyDTO fromAdvancedPolicyToInfoDTO(APIPolicy apiPolicy)
            throws UnsupportedThrottleLimitTypeException, UnsupportedThrottleConditionTypeException {
        AdvancedThrottlePolicyDTO policyDTO = new AdvancedThrottlePolicyDTO();
        policyDTO = CommonThrottleMappingUtil.updateFieldsFromToPolicyToDTO(apiPolicy, policyDTO);
        if (apiPolicy.getDefaultQuotaPolicy() != null) {
            policyDTO.setQuotaPolicy(CommonThrottleMappingUtil.fromQuotaPolicyToDTO(apiPolicy.getDefaultQuotaPolicy()));
        }
        return policyDTO;
    }
}

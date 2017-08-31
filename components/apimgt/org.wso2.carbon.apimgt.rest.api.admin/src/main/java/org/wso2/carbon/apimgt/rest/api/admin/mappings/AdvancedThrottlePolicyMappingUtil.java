/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */

package org.wso2.carbon.apimgt.rest.api.admin.mappings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(AdvancedThrottlePolicyMappingUtil.class);

    /**
     * Converts an array of Advanced Policy objects into a List DTO
     *
     * @param policies Array of Advanced Policies
     * @return A List DTO of converted Advanced Policies
     * @throws UnsupportedThrottleLimitTypeException - If error occurs
     * @throws UnsupportedThrottleConditionTypeException - If error occurs
     */
    public static AdvancedThrottlePolicyListDTO fromAPIPolicyArrayToListDTO(List<APIPolicy> policies)
            throws UnsupportedThrottleLimitTypeException, UnsupportedThrottleConditionTypeException {
        AdvancedThrottlePolicyListDTO listDTO = new AdvancedThrottlePolicyListDTO();
        List<AdvancedThrottlePolicyDTO> advancedPolicyDTOs = new ArrayList<>();
        if (policies != null) {
            for (APIPolicy policy : policies) {
                advancedPolicyDTOs.add(fromAdvancedPolicyToDTO(policy));
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
     * @throws UnsupportedThrottleLimitTypeException - If error occurs
     * @throws UnsupportedThrottleConditionTypeException - If error occurs
     */
    public static APIPolicy fromAdvancedPolicyDTOToPolicy(AdvancedThrottlePolicyDTO dto)
            throws UnsupportedThrottleLimitTypeException, UnsupportedThrottleConditionTypeException {

        APIPolicy apiPolicy = new APIPolicy(dto.getId(), dto.getPolicyName());
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
     * @param policy Advanced Policy model object
     * @return Converted Advanced policy REST API DTO object
     * @throws UnsupportedThrottleLimitTypeException - If error occurs
     * @throws UnsupportedThrottleConditionTypeException - If error occurs
     */
    public static AdvancedThrottlePolicyDTO fromAdvancedPolicyToDTO(APIPolicy policy)
            throws UnsupportedThrottleLimitTypeException, UnsupportedThrottleConditionTypeException {

        AdvancedThrottlePolicyDTO policyDTO = new AdvancedThrottlePolicyDTO();

        policyDTO = CommonThrottleMappingUtil.updateFieldsFromToPolicyToDTO(policy, policyDTO);

        List<ConditionalGroupDTO> groupDTOs = CommonThrottleMappingUtil
                .fromPipelineListToConditionalGroupDTOList(policy.getPipelines());
        policyDTO.setConditionalGroups(groupDTOs);

        if (policy.getDefaultQuotaPolicy() != null) {
            policyDTO.setDefaultLimit(CommonThrottleMappingUtil.fromQuotaPolicyToDTO(policy.getDefaultQuotaPolicy()));
        }
        return policyDTO;
    }

    /**
     * Converts a single Advanced Policy model into REST API DTO
     *
     * @param apiPolicy Advanced Policy model object
     * @return Converted Advanced policy REST API DTO object
     * @throws UnsupportedThrottleLimitTypeException - If error occurs
     * @throws UnsupportedThrottleConditionTypeException - If error occurs
     */
    public static AdvancedThrottlePolicyDTO fromAdvancedPolicyToInfoDTO(APIPolicy apiPolicy)
            throws UnsupportedThrottleLimitTypeException, UnsupportedThrottleConditionTypeException {

        AdvancedThrottlePolicyDTO policyDTO = new AdvancedThrottlePolicyDTO();
        policyDTO = CommonThrottleMappingUtil.updateFieldsFromToPolicyToDTO(apiPolicy, policyDTO);
        if (apiPolicy.getDefaultQuotaPolicy() != null) {
            policyDTO.setDefaultLimit(CommonThrottleMappingUtil.fromQuotaPolicyToDTO(apiPolicy.getDefaultQuotaPolicy()));
        }
        return policyDTO;
    }
}

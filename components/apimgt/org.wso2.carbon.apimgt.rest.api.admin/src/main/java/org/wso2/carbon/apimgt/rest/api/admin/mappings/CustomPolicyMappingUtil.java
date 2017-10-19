/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.rest.api.admin.mappings;

import org.wso2.carbon.apimgt.core.models.policy.CustomPolicy;
import org.wso2.carbon.apimgt.rest.api.admin.dto.CustomRuleDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.CustomRuleListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.exceptions.UnsupportedThrottleLimitTypeException;

import java.util.ArrayList;
import java.util.List;

/**
 * Mapping CustomPolicy and rest api CustomPolicyDTO object.
 */
public class CustomPolicyMappingUtil {

    /**
     * Converts an array of Custom Policy model objects into REST API DTO objects.
     *
     * @param customPolicies An array of custom policy model objects
     * @return A List DTO of Custom Policy DTOs derived from the array of model objects
     * @throws UnsupportedThrottleLimitTypeException - If error occurs
     */
    public static CustomRuleListDTO fromCustomPolicyArrayToListDTO(List<CustomPolicy> customPolicies)
            throws UnsupportedThrottleLimitTypeException {
        CustomRuleListDTO listDTO = new CustomRuleListDTO();
        List<CustomRuleDTO> customPolicyDTOList = new ArrayList<>();
        if (customPolicies != null) {
            for (CustomPolicy policy : customPolicies) {
                CustomRuleDTO dto = fromCustomPolicyToDTO(policy);
                customPolicyDTOList.add(dto);
            }
        }
        listDTO.setCount(customPolicyDTOList.size());
        listDTO.setList(customPolicyDTOList);
        return listDTO;
    }

    /**
     * Converts a single Custom Policy model object into DTO object.
     *
     * @param globalPolicy Custom Policy model object
     * @return DTO object derived from the Policy model object
     * @throws UnsupportedThrottleLimitTypeException - If error occurs
     */
    public static CustomRuleDTO fromCustomPolicyToDTO(CustomPolicy globalPolicy)
            throws UnsupportedThrottleLimitTypeException {
        CustomRuleDTO policyDTO = new CustomRuleDTO();
        policyDTO = CommonThrottleMappingUtil.updateFieldsFromToPolicyToDTO(globalPolicy, policyDTO);
        policyDTO.setKeyTemplate(globalPolicy.getKeyTemplate());
        policyDTO.setSiddhiQuery(globalPolicy.getSiddhiQuery());
        return policyDTO;
    }

    /**
     * Converts a single Custom Policy DTO object into model object.
     *
     * @param dto Custom Policy DTO object
     * @return Model object derived from DTO
     * @throws UnsupportedThrottleLimitTypeException - If error occurs
     */
    public static CustomPolicy fromCustomPolicyDTOToModel(CustomRuleDTO dto)
            throws UnsupportedThrottleLimitTypeException {
        CustomPolicy customPolicy = new CustomPolicy(dto.getPolicyName());
        customPolicy = CommonThrottleMappingUtil.updateFieldsFromDTOToPolicy(dto, customPolicy);
        customPolicy.setKeyTemplate(dto.getKeyTemplate());
        customPolicy.setSiddhiQuery(dto.getSiddhiQuery());
        return customPolicy;
    }

}

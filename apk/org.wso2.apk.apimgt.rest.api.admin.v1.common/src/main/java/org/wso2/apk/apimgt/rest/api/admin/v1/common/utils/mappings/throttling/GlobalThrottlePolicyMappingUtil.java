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

import org.wso2.apk.apimgt.api.UnsupportedThrottleLimitTypeException;
import org.wso2.apk.apimgt.api.model.policy.GlobalPolicy;
import org.wso2.apk.apimgt.rest.api.admin.v1.dto.CustomRuleDTO;
import org.wso2.apk.apimgt.rest.api.admin.v1.dto.CustomRuleListDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for mapping Global Level Throttling model and its sub components into REST API DTOs
 * and vice-versa
 */
public class GlobalThrottlePolicyMappingUtil {

    private static final String CUSTOM_RULE_THROTTLING_POLICY_TYPE = "CustomRule";
    /**
     * Converts an array of Global policy model objects into REST API DTO objects
     *
     * @param GlobalPolicies An array of Global Policy model objects
     * @return A List DTO of Global Policy DTOs derived from the array of model objects
     * @throws UnsupportedThrottleLimitTypeException
     */
    public static CustomRuleListDTO fromGlobalPolicyArrayToListDTO(
            GlobalPolicy[] GlobalPolicies) throws UnsupportedThrottleLimitTypeException {
        CustomRuleListDTO listDTO = new CustomRuleListDTO();
        List<CustomRuleDTO> globalPolicyDTOList = new ArrayList<>();
        if (GlobalPolicies != null) {
            for (GlobalPolicy policy : GlobalPolicies) {
                CustomRuleDTO dto = fromGlobalThrottlePolicyToDTO(policy);
                globalPolicyDTOList.add(dto);
            }
        }
        listDTO.setCount(globalPolicyDTOList.size());
        listDTO.setList(globalPolicyDTOList);
        return listDTO;
    }

    /**
     * Converts a single Global Policy model object into DTO object
     *
     * @param globalPolicy Global Policy model object
     * @return DTO object derived from the Policy model object
     */
    public static CustomRuleDTO fromGlobalThrottlePolicyToDTO(GlobalPolicy globalPolicy) {
        CustomRuleDTO policyDTO = new CustomRuleDTO();
        policyDTO = CommonThrottleMappingUtil.updateFieldsFromToPolicyToDTO(globalPolicy, policyDTO);
        policyDTO.setKeyTemplate(globalPolicy.getKeyTemplate());
        policyDTO.setSiddhiQuery(globalPolicy.getSiddhiQuery());
        policyDTO.setType(CUSTOM_RULE_THROTTLING_POLICY_TYPE);
        return policyDTO;
    }

    /**
     * Converts a single Global policy DTO object into model object
     *
     * @param dto Global policy DTO object
     * @return Model object derived from DTO
     */
    public static GlobalPolicy fromGlobalThrottlePolicyDTOToModel(CustomRuleDTO dto) {

        //update mandatory fields such as tenantDomain etc.
        dto = CommonThrottleMappingUtil.updateDefaultMandatoryFieldsOfThrottleDTO(dto);

        GlobalPolicy globalPolicy = new GlobalPolicy(dto.getPolicyName());
        globalPolicy = CommonThrottleMappingUtil.updateFieldsFromDTOToPolicy(dto, globalPolicy);
        globalPolicy.setKeyTemplate(dto.getKeyTemplate());
        globalPolicy.setSiddhiQuery(dto.getSiddhiQuery());
        return globalPolicy;
    }
}
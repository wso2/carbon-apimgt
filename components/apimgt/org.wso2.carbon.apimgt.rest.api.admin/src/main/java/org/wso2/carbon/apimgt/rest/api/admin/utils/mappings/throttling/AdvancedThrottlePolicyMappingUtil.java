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

package org.wso2.carbon.apimgt.rest.api.admin.utils.mappings.throttling;

import org.wso2.carbon.apimgt.api.UnsupportedThrottleConditionTypeException;
import org.wso2.carbon.apimgt.api.UnsupportedThrottleLimitTypeException;
import org.wso2.carbon.apimgt.api.model.policy.APIPolicy;
import org.wso2.carbon.apimgt.api.model.policy.Pipeline;
import org.wso2.carbon.apimgt.api.model.policy.PolicyConstants;
import org.wso2.carbon.apimgt.rest.api.admin.dto.AdvancedThrottlePolicyDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.AdvancedThrottlePolicyListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ConditionalGroupDTO;

import java.util.ArrayList;
import java.util.List;

public class AdvancedThrottlePolicyMappingUtil {

    public static AdvancedThrottlePolicyListDTO fromAPIPolicyArrayToListDTO(APIPolicy[] apiPolicies)
            throws UnsupportedThrottleLimitTypeException, UnsupportedThrottleConditionTypeException {
        AdvancedThrottlePolicyListDTO listDTO = new AdvancedThrottlePolicyListDTO();
        List <AdvancedThrottlePolicyDTO> advancedPolicyDTOs = new ArrayList<>();
        if (apiPolicies != null) {
            for (APIPolicy apiPolicy : apiPolicies) {
                advancedPolicyDTOs.add(fromAdvancedPolicyToDTO(apiPolicy));
            }
        }
        listDTO.setList(advancedPolicyDTOs);
        listDTO.setCount(advancedPolicyDTOs.size());

        //listDTO.setNext(); todo
        //listDTO.setPrevious(); todo
        
        return listDTO;
    }

    /////////////////  AdvancedPolicyDTO <---> APIPolicy ///////////////////////////////////////
    
    public static APIPolicy fromAdvancedPolicyDTOToPolicy (AdvancedThrottlePolicyDTO dto)
            throws UnsupportedThrottleLimitTypeException, UnsupportedThrottleConditionTypeException {

        //update mandatory fields such as tenantDomain etc.
        dto = CommonThrottleMappingUtil.updateDefaultMandatoryFieldsOfThrottleDTO(dto);

        APIPolicy apiPolicy = new APIPolicy(dto.getPolicyName());
        apiPolicy = CommonThrottleMappingUtil.updateFieldsFromDTOToPolicy(dto, apiPolicy);
        apiPolicy.setUserLevel(mapAdvancedPolicyUserLevelFromDTOToModel(dto.getUserLevel()));

        List<Pipeline> pipelines = CommonThrottleMappingUtil.fromConditionalGroupDTOListToPipelineList(
                dto.getConditionalGroups());
        apiPolicy.setPipelines(pipelines);
        return apiPolicy;
    }

    public static AdvancedThrottlePolicyDTO fromAdvancedPolicyToDTO (APIPolicy apiPolicy)
            throws UnsupportedThrottleLimitTypeException, UnsupportedThrottleConditionTypeException {
        AdvancedThrottlePolicyDTO policyDTO = new AdvancedThrottlePolicyDTO();
        policyDTO = CommonThrottleMappingUtil.updateFieldsFromToPolicyToDTO(apiPolicy, policyDTO);
        policyDTO.setUserLevel(mapAdvancedPolicyUserLevelFromModelToDTO(apiPolicy.getUserLevel()));
        List<ConditionalGroupDTO> groupDTOs = CommonThrottleMappingUtil.fromPipelineListToConditionalGroupDTOList(
                apiPolicy.getPipelines());
        policyDTO.setConditionalGroups(groupDTOs);
        return policyDTO;
    }

    private static String mapAdvancedPolicyUserLevelFromDTOToModel(AdvancedThrottlePolicyDTO.UserLevelEnum userLevelEnum ) {
        switch (userLevelEnum) {
        case apiLevel:
            return PolicyConstants.ACROSS_ALL;
        case userLevel:
            return PolicyConstants.PER_USER;
        default:
            return null;
        }
    }


    private static AdvancedThrottlePolicyDTO.UserLevelEnum mapAdvancedPolicyUserLevelFromModelToDTO(String userLevel) {
        switch (userLevel) {
        case PolicyConstants.ACROSS_ALL:
            return AdvancedThrottlePolicyDTO.UserLevelEnum.apiLevel;
        case PolicyConstants.PER_USER:
            return AdvancedThrottlePolicyDTO.UserLevelEnum.userLevel;
        default:
            return null;
        }
    }
}

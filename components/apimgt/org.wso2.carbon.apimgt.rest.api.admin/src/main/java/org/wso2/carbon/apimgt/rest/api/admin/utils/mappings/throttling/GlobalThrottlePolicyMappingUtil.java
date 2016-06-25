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

import org.wso2.carbon.apimgt.api.UnsupportedThrottleLimitTypeException;
import org.wso2.carbon.apimgt.api.model.policy.GlobalPolicy;
import org.wso2.carbon.apimgt.rest.api.admin.dto.GlobalThrottlePolicyDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.GlobalThrottlePolicyListDTO;

import java.util.ArrayList;
import java.util.List;

public class GlobalThrottlePolicyMappingUtil {

    public static GlobalThrottlePolicyListDTO fromGlobalPolicyArrayToListDTO(
            GlobalPolicy[] GlobalPolicies) throws UnsupportedThrottleLimitTypeException {
        GlobalThrottlePolicyListDTO listDTO = new GlobalThrottlePolicyListDTO();
        List<GlobalThrottlePolicyDTO> globalPolicyDTOList = new ArrayList<>();
        if (GlobalPolicies != null) {
            for (GlobalPolicy policy : GlobalPolicies) {
                GlobalThrottlePolicyDTO dto = fromGlobalThrottlePolicyToDTO(policy);
                globalPolicyDTOList.add(dto);
            }
        }
        listDTO.setCount(globalPolicyDTOList.size());
        listDTO.setList(globalPolicyDTOList);
        listDTO.setNext(""); //todo set next and previous
        listDTO.setPrevious("");
        return listDTO;
    }

    public static GlobalThrottlePolicyDTO fromGlobalThrottlePolicyToDTO(
            GlobalPolicy globalPolicy) throws UnsupportedThrottleLimitTypeException {
        GlobalThrottlePolicyDTO policyDTO = new GlobalThrottlePolicyDTO();
        policyDTO = CommonThrottleMappingUtil.updateFieldsFromToPolicyToDTO(globalPolicy, policyDTO);
        policyDTO.setKeyTemplate(globalPolicy.getKeyTemplate());
        policyDTO.setSiddhiQuery(globalPolicy.getSiddhiQuery());
        return policyDTO;
    }

    public static GlobalPolicy fromGlobalThrottlePolicyDTOToModel(GlobalThrottlePolicyDTO dto)
            throws UnsupportedThrottleLimitTypeException {

        //update mandatory fields such as tenantDomain etc.
        dto = CommonThrottleMappingUtil.updateDefaultMandatoryFieldsOfThrottleDTO(dto);

        GlobalPolicy globalPolicy = new GlobalPolicy(dto.getPolicyName());
        globalPolicy = CommonThrottleMappingUtil.updateFieldsFromDTOToPolicy(dto, globalPolicy);
        globalPolicy.setKeyTemplate(dto.getKeyTemplate());
        globalPolicy.setSiddhiQuery(dto.getSiddhiQuery());
        return globalPolicy;
    }
}

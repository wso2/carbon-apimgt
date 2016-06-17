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

package org.wso2.carbon.apimgt.rest.api.publisher.utils.mappings.throttling;

import org.wso2.carbon.apimgt.api.UnsupportedThrottleLimitTypeException;
import org.wso2.carbon.apimgt.api.model.policy.ApplicationPolicy;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.ApplicationThrottlePolicyDTO;

import org.apache.commons.codec.binary.Base64;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.ApplicationThrottlePolicyListDTO;

import java.util.ArrayList;
import java.util.List;

public class ApplicationThrottlePolicyMappingUtil {

    public static ApplicationThrottlePolicyListDTO fromApplicationPolicyArrayToListDTO(
            ApplicationPolicy[] appPolicies) throws UnsupportedThrottleLimitTypeException {
        ApplicationThrottlePolicyListDTO listDTO = new ApplicationThrottlePolicyListDTO();
        List<ApplicationThrottlePolicyDTO> appPolicyDTOList = new ArrayList<>();
        if (appPolicies != null) {
            for (ApplicationPolicy policy : appPolicies) {
                ApplicationThrottlePolicyDTO dto = fromApplicationThrottlePolicyToDTO(policy);
                appPolicyDTOList.add(dto);
            }
        }
        listDTO.setCount(appPolicyDTOList.size());
        listDTO.setNext(""); //todo set next and previous
        listDTO.setPrevious("");
        return listDTO;
    }
    
    public static ApplicationThrottlePolicyDTO fromApplicationThrottlePolicyToDTO (ApplicationPolicy appPolicy)
            throws UnsupportedThrottleLimitTypeException {
        Base64  base64 = new Base64(false);
        ApplicationThrottlePolicyDTO policyDTO = new ApplicationThrottlePolicyDTO();
        policyDTO = CommonThrottleMappingUtil.updateFieldsFromToPolicyToDTO(appPolicy, policyDTO);
        policyDTO.setApplicationId(appPolicy.getApplicationId() + ""); // todo change to uuid
        policyDTO.setCustomAttributes(base64.encodeToString(appPolicy.getCustomAttributes()));
        return policyDTO;
    }

    public static ApplicationPolicy fromApplicationThrottlePolicyDTOToModel (ApplicationThrottlePolicyDTO dto)
            throws UnsupportedThrottleLimitTypeException {
        Base64  base64 = new Base64(false);
        ApplicationPolicy appPolicy = new ApplicationPolicy(dto.getPolicyName());
        appPolicy = CommonThrottleMappingUtil.updateFieldsFromDTOToPolicy(dto, appPolicy);
        appPolicy.setApplicationId(Integer.parseInt(dto.getApplicationId())); // todo change to uuid
        appPolicy.setCustomAttributes(base64.decode(appPolicy.getCustomAttributes()));
        return appPolicy;
    }
}

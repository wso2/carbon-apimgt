/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.hybrid.gateway.throttling.synchronizer.util.mapping.throttling;

import org.wso2.carbon.apimgt.api.UnsupportedThrottleLimitTypeException;
import org.wso2.carbon.apimgt.api.model.policy.ApplicationPolicy;
import org.wso2.carbon.apimgt.hybrid.gateway.throttling.synchronizer.dto.ApplicationThrottlePolicyListDTO;
import org.wso2.carbon.apimgt.hybrid.gateway.throttling.synchronizer.dto.ApplicationThrottlePolicyDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for mapping Application Level Throttling model and its sub components into REST API DTOs
 * and vice-versa
 */
public class ApplicationThrottlePolicyMappingUtil {

    /**
     * Converts an array of Application Policy objects into a List DTO
     *
     * @param appPolicies Array of Application Policies
     * @return A List DTO of converted Application Policies
     * @throws UnsupportedThrottleLimitTypeException
     */
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
        listDTO.setList(appPolicyDTOList);
        return listDTO;
    }

    /**
     * Converts a single Application Policy model into REST API DTO
     *
     * @param appPolicy An Application Policy model object
     * @return Converted Application policy REST API DTO object
     * @throws UnsupportedThrottleLimitTypeException
     */
    public static ApplicationThrottlePolicyDTO fromApplicationThrottlePolicyToDTO(ApplicationPolicy appPolicy)
            throws UnsupportedThrottleLimitTypeException {
        ApplicationThrottlePolicyDTO policyDTO = new ApplicationThrottlePolicyDTO();
        policyDTO = CommonThrottleMappingUtil.updateFieldsFromToPolicyToDTO(appPolicy, policyDTO);
        if (appPolicy.getDefaultQuotaPolicy() != null) {
            policyDTO.setDefaultLimit(CommonThrottleMappingUtil.fromQuotaPolicyToDTO(appPolicy.getDefaultQuotaPolicy()));
        }
        return policyDTO;
    }

    /**
     * Converts a single Application Policy DTO into a model object
     *
     * @param dto Application Policy DTO Object
     * @return Converted Application Policy Model object
     * @throws UnsupportedThrottleLimitTypeException
     */
    public static ApplicationPolicy fromApplicationThrottlePolicyDTOToModel(ApplicationThrottlePolicyDTO dto)
            throws UnsupportedThrottleLimitTypeException {
        //update mandatory fields such as tenantDomain etc.
        dto = CommonThrottleMappingUtil.updateDefaultMandatoryFieldsOfThrottleDTO(dto);

        ApplicationPolicy appPolicy = new ApplicationPolicy(dto.getPolicyName());
        appPolicy = CommonThrottleMappingUtil.updateFieldsFromDTOToPolicy(dto, appPolicy);
        if (dto.getDefaultLimit() != null) {
            appPolicy.setDefaultQuotaPolicy(CommonThrottleMappingUtil.fromDTOToQuotaPolicy(dto.getDefaultLimit()));
        }
        return appPolicy;
    }
}

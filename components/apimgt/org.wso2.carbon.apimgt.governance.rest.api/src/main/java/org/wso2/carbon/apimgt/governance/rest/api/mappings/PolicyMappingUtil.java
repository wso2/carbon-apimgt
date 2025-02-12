/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.apimgt.governance.rest.api.mappings;

import org.wso2.carbon.apimgt.governance.api.model.PolicyInfo;
import org.wso2.carbon.apimgt.governance.rest.api.dto.PolicyInfoDTO;

/**
 * This class represents the Policy Mapping Utility
 */
public class PolicyMappingUtil {

    /**
     * Converts a PolicyInfo object to a PolicyInfoDTO object
     *
     * @param policyInfo PolicyInfo object
     * @return PolicyInfoDTO object
     */
    public static PolicyInfoDTO fromPolicyInfoToPolicyInfoDTO(PolicyInfo policyInfo) {
        PolicyInfoDTO policyInfoDTO = new PolicyInfoDTO();
        policyInfoDTO.setId(policyInfo.getId());
        policyInfoDTO.setName(policyInfo.getName());
        policyInfoDTO.setDescription(policyInfo.getDescription());
        policyInfoDTO.setPolicyCategory(PolicyInfoDTO.
                PolicyCategoryEnum.fromValue(String.valueOf(policyInfo.getPolicyCategory())));
        policyInfoDTO.setPolicyType(PolicyInfoDTO.
                PolicyTypeEnum.fromValue(String.valueOf(policyInfo.getPolicyType())));
        policyInfoDTO.setArtifactType(PolicyInfoDTO.
                ArtifactTypeEnum.fromValue(String.valueOf(policyInfo.getArtifactType())));
        policyInfoDTO.setDocumentationLink(policyInfo.getDocumentationLink());
        policyInfoDTO.setProvider(policyInfo.getProvider());
        policyInfoDTO.setCreatedBy(policyInfo.getCreatedBy());
        policyInfoDTO.setCreatedTime(policyInfo.getCreatedTime());
        policyInfoDTO.setUpdatedBy(policyInfo.getUpdatedBy());
        policyInfoDTO.setUpdatedTime(policyInfo.getUpdatedTime());
        return policyInfoDTO;
    }
}

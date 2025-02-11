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
 * This class represents the Ruleset Mapping Utility
 */
public class PolicyMappingUtil {

    /**
     * Converts a RulesetInfo object to a PolicyInfoDTO object
     *
     * @param policyInfo RulesetInfo object
     * @return PolicyInfoDTO object
     */
    public static PolicyInfoDTO fromPolicyInfoToPolicyInfoDTO(PolicyInfo policyInfo) {
        PolicyInfoDTO rulesetInfoDTO = new PolicyInfoDTO();
        rulesetInfoDTO.setId(policyInfo.getId());
        rulesetInfoDTO.setName(policyInfo.getName());
        rulesetInfoDTO.setDescription(policyInfo.getDescription());
        rulesetInfoDTO.setRuleCategory(PolicyInfoDTO.
                RuleCategoryEnum.fromValue(String.valueOf(policyInfo.getPolicyCategory())));
        rulesetInfoDTO.setRuleType(PolicyInfoDTO.
                RuleTypeEnum.fromValue(String.valueOf(policyInfo.getPolicyType())));
        rulesetInfoDTO.setArtifactType(PolicyInfoDTO.
                ArtifactTypeEnum.fromValue(String.valueOf(policyInfo.getArtifactType())));
        rulesetInfoDTO.setDocumentationLink(policyInfo.getDocumentationLink());
        rulesetInfoDTO.setProvider(policyInfo.getProvider());
        rulesetInfoDTO.setCreatedBy(policyInfo.getCreatedBy());
        rulesetInfoDTO.setCreatedTime(policyInfo.getCreatedTime());
        rulesetInfoDTO.setUpdatedBy(policyInfo.getUpdatedBy());
        rulesetInfoDTO.setUpdatedTime(policyInfo.getUpdatedTime());
        return rulesetInfoDTO;
    }
}

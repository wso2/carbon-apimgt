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

import org.wso2.carbon.apimgt.governance.api.model.GovernancePolicyInfo;
import org.wso2.carbon.apimgt.governance.api.model.GovernancePolicyInfoWithRulesetIds;
import org.wso2.carbon.apimgt.governance.api.model.GovernancePolicyList;
import org.wso2.carbon.apimgt.governance.api.model.RulesetId;
import org.wso2.carbon.apimgt.governance.api.model.RulesetInfo;
import org.wso2.carbon.apimgt.governance.rest.api.dto.GovernancePolicyInfoDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.GovernancePolicyInfoWithRulesetIdsDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.GovernancePolicyListDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.RulesetIdDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.RulesetInfoDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the Policy Mapping Utility
 */
public class PolicyMappingUtil {

    /**
     * Converts a GovernancePolicyInfoWithRulesetIdsDTO object to
     * a GovernancePolicyInfoWithRulesetIds object
     *
     * @param dto GovernancePolicyInfoWithRulesetIdsDTO object
     * @return GovernancePolicyInfoWithRulesetIds object
     */
    public static GovernancePolicyInfoWithRulesetIds fromDTOtoGovernancePolicyInfoWithRulesetIds
    (GovernancePolicyInfoWithRulesetIdsDTO dto) {
        GovernancePolicyInfoWithRulesetIds govPolicyInfo = new GovernancePolicyInfoWithRulesetIds();
        govPolicyInfo.setId(dto.getId());
        govPolicyInfo.setName(dto.getName());
        govPolicyInfo.setDescription(dto.getDescription());
        govPolicyInfo.setCreatedBy(dto.getCreatedBy());
        govPolicyInfo.setCreatedBy(dto.getCreatedBy());
        govPolicyInfo.setUpdatedBy(dto.getUpdatedBy());
        govPolicyInfo.setUpdatedTime(dto.getUpdatedTime());
        for (RulesetIdDTO rulesetIdDTO : dto.getRulesets()) {
            RulesetId rulesetId = new RulesetId();
            rulesetId.setId(rulesetIdDTO.getId());
            govPolicyInfo.addRuleset(rulesetId);
        }
        for (String label : dto.getLabels()) {
            govPolicyInfo.addLabel(label);
        }
        return govPolicyInfo;
    }

    /**
     * Converts a GovernancePolicyInfo object to a GovernancePolicyInfoDTO object
     *
     * @param governancePolicyInfo GovernancePolicyInfo object
     * @return GovernancePolicyInfoDTO object
     */
    public static GovernancePolicyInfoDTO fromGovernancePolicyInfoToGovernancePolicyInfoDTO
    (GovernancePolicyInfo governancePolicyInfo) {
        GovernancePolicyInfoDTO governancePolicyInfoDTO = new GovernancePolicyInfoDTO();
        governancePolicyInfoDTO.setId(governancePolicyInfo.getId());
        governancePolicyInfoDTO.setName(governancePolicyInfo.getName());
        governancePolicyInfoDTO.setDescription(governancePolicyInfo.getDescription());
        governancePolicyInfoDTO.setCreatedBy(governancePolicyInfo.getCreatedBy());
        governancePolicyInfoDTO.setCreatedTime(governancePolicyInfo.getCreatedTime());
        governancePolicyInfoDTO.setUpdatedBy(governancePolicyInfo.getUpdatedBy());
        governancePolicyInfoDTO.setUpdatedTime(governancePolicyInfo.getUpdatedTime());
        governancePolicyInfoDTO.setLabels(governancePolicyInfo.getLabels());
        List<RulesetInfoDTO> rulesetInfoDTOList = new ArrayList<>();
        for (RulesetInfo rulesetInfo : governancePolicyInfo.getRulesets()) {
            rulesetInfoDTOList.add(RulesetMappingUtil.fromRulesetInfoToRulesetInfoDTO(rulesetInfo));
        }
        governancePolicyInfoDTO.setRulesets(rulesetInfoDTOList);
        return governancePolicyInfoDTO;
    }

    /**
     * Converts a GovernancePolicyList object to a GovernancePolicyListDTO object
     *
     * @param policyList GovernancePolicyList object
     * @return GovernancePolicyListDTO object
     */
    public static GovernancePolicyListDTO fromGovernancePolicyListToGovernancePolicyListDTO(GovernancePolicyList policyList) {
        GovernancePolicyListDTO policyListDTO = new GovernancePolicyListDTO();
        policyListDTO.setCount(policyList.getCount());
        List<GovernancePolicyInfoDTO> policyInfoDTOList = new ArrayList<>();
        for (GovernancePolicyInfo policyInfo : policyList.getGovernancePolicyList()) {
            policyInfoDTOList.add(fromGovernancePolicyInfoToGovernancePolicyInfoDTO(policyInfo));
        }
        policyListDTO.setList(policyInfoDTOList);
        return policyListDTO;
    }
}

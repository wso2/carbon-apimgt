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

import org.wso2.carbon.apimgt.governance.api.model.RulesetInfo;
import org.wso2.carbon.apimgt.governance.api.model.RulesetList;
import org.wso2.carbon.apimgt.governance.rest.api.dto.RulesetInfoDTO;

/**
 * This class represents the Ruleset Mapping Utility
 */
public class RulesetMappingUtil {

    /**
     * Converts a RulesetInfo object to a RulesetInfoDTO object
     *
     * @param rulesetInfo RulesetInfo object
     * @return RulesetInfoDTO object
     */
    public static RulesetInfoDTO fromRulesetInfoToRulesetInfoDTO(RulesetInfo rulesetInfo) {
        RulesetInfoDTO rulesetInfoDTO = new RulesetInfoDTO();
        rulesetInfoDTO.setId(rulesetInfo.getId());
        rulesetInfoDTO.setName(rulesetInfo.getName());
        rulesetInfoDTO.setDescription(rulesetInfo.getDescription());
        rulesetInfoDTO.setRuleCategory(RulesetInfoDTO.
                RuleCategoryEnum.fromValue(String.valueOf(rulesetInfo.getRuleCategory())));
        rulesetInfoDTO.setRuleType(RulesetInfoDTO.
                RuleTypeEnum.fromValue(String.valueOf(rulesetInfo.getRuleType())));
        rulesetInfoDTO.setArtifactType(RulesetInfoDTO.
                ArtifactTypeEnum.fromValue(String.valueOf(rulesetInfo.getArtifactType())));
        rulesetInfoDTO.setDocumentationLink(rulesetInfo.getDocumentationLink());
        rulesetInfoDTO.setProvider(rulesetInfo.getProvider());
        rulesetInfoDTO.setCreatedBy(rulesetInfo.getCreatedBy());
        rulesetInfoDTO.setCreatedTime(rulesetInfo.getCreatedTime());
        rulesetInfoDTO.setUpdatedBy(rulesetInfo.getUpdatedBy());
        rulesetInfoDTO.setUpdatedTime(rulesetInfo.getUpdatedTime());
        return rulesetInfoDTO;
    }
}

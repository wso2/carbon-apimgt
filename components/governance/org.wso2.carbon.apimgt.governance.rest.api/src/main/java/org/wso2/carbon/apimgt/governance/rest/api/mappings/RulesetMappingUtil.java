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

import org.wso2.carbon.apimgt.governance.api.model.Ruleset;
import org.wso2.carbon.apimgt.governance.api.model.RulesetInfo;
import org.wso2.carbon.apimgt.governance.api.model.RulesetList;
import org.wso2.carbon.apimgt.governance.rest.api.dto.RulesetDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.RulesetInfoDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.RulesetListDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the Ruleset Mapping Utility
 */
public class RulesetMappingUtil {

    /**
     * Converts a RulesetDTO object to a Ruleset object
     *
     * @param rulesetDTO RulesetDTO object
     * @return Ruleset object
     */
    public static Ruleset fromDTOtoRuleset(RulesetDTO rulesetDTO) {
        Ruleset ruleset = new Ruleset();
        ruleset.setId(rulesetDTO.getId());
        ruleset.setName(rulesetDTO.getName());
        ruleset.setDescription(rulesetDTO.getDescription());
        ruleset.setRulesetContent(rulesetDTO.getRulesetContent());
        ruleset.setAppliesTo(String.valueOf(rulesetDTO.getAppliesTo()));
        ruleset.setDocumentationLink(rulesetDTO.getDocumentationLink());
        ruleset.setProvider(rulesetDTO.getProvider());
        ruleset.setCreatedBy(rulesetDTO.getCreatedBy());
        ruleset.setCreatedTime(rulesetDTO.getCreatedTime());
        ruleset.setUpdatedBy(rulesetDTO.getUpdatedBy());
        ruleset.setUpdatedTime(rulesetDTO.getUpdatedTime());
        return ruleset;
    }

    /**
     * Converts a Ruleset object to a RulesetDTO object
     *
     * @param createdRuleset Ruleset object
     * @return RulesetDTO object
     */
    public static RulesetDTO fromRulsetToDTO(Ruleset createdRuleset) {
        RulesetDTO rulesetDTO = new RulesetDTO();
        rulesetDTO.setId(createdRuleset.getId());
        rulesetDTO.setName(createdRuleset.getName());
        rulesetDTO.setDescription(createdRuleset.getDescription());
        rulesetDTO.setRulesetContent(createdRuleset.getRulesetContent());
        rulesetDTO.setAppliesTo(RulesetDTO.AppliesToEnum.
                fromValue(createdRuleset.getAppliesTo()));
        rulesetDTO.setDocumentationLink(createdRuleset.getDocumentationLink());
        rulesetDTO.setProvider(createdRuleset.getProvider());
        rulesetDTO.setCreatedBy(createdRuleset.getCreatedBy());
        rulesetDTO.setCreatedTime(createdRuleset.getCreatedTime());
        rulesetDTO.setUpdatedBy(createdRuleset.getUpdatedBy());
        rulesetDTO.setUpdatedTime(createdRuleset.getUpdatedTime());
        rulesetDTO.setIsDefault(createdRuleset.isDefault() == 1);
        return rulesetDTO;
    }

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
        rulesetInfoDTO.setAppliesTo(RulesetInfoDTO.
                AppliesToEnum.fromValue(rulesetInfo.getAppliesTo()));
        rulesetInfoDTO.setDocumentationLink(rulesetInfo.getDocumentationLink());
        rulesetInfoDTO.setProvider(rulesetInfo.getProvider());
        rulesetInfoDTO.setCreatedBy(rulesetInfo.getCreatedBy());
        rulesetInfoDTO.setCreatedTime(rulesetInfo.getCreatedTime());
        rulesetInfoDTO.setUpdatedBy(rulesetInfo.getUpdatedBy());
        rulesetInfoDTO.setUpdatedTime(rulesetInfo.getUpdatedTime());
        rulesetInfoDTO.setIsDefault(rulesetInfo.isDefault() == 1);
        return rulesetInfoDTO;
    }

    /**
     * Converts a RulesetList object to a RulesetListDTO object
     *
     * @param rulesetList RulesetList object
     * @return RulesetListDTO object
     */
    public static RulesetListDTO fromRulsetListToDTO(RulesetList rulesetList) {
        RulesetListDTO rulesetListDTO = new RulesetListDTO();
        rulesetListDTO.setCount(rulesetList.getCount());
        List<RulesetInfo> rulesets = rulesetList.getRulesetList();
        List<RulesetInfoDTO> rulesetInfoDTOs = new ArrayList();
        for (RulesetInfo ruleset : rulesets) {
            rulesetInfoDTOs.add(fromRulesetInfoToRulesetInfoDTO(ruleset));
        }
        rulesetListDTO.setList(rulesetInfoDTOs);
        return rulesetListDTO;
    }
}

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

import org.wso2.carbon.apimgt.governance.api.model.APIMGovernableState;
import org.wso2.carbon.apimgt.governance.api.model.APIMGovernanceAction;
import org.wso2.carbon.apimgt.governance.api.model.APIMGovernanceActionType;
import org.wso2.carbon.apimgt.governance.api.model.APIMGovernancePolicy;
import org.wso2.carbon.apimgt.governance.api.model.RuleSeverity;
import org.wso2.carbon.apimgt.governance.impl.APIMGovernanceConstants;
import org.wso2.carbon.apimgt.governance.rest.api.dto.APIMGovernancePolicyDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ActionDTO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class represents the Policy Mapping Utility
 */
public class PolicyMappingUtil {

    /**
     * Converts a GovernancePolicyDTO object to
     * a APIMGovernancePolicy object
     *
     * @param dto GovernancePolicyDTO object
     * @return APIMGovernancePolicy object
     */
    public static APIMGovernancePolicy fromDTOtoGovernancePolicy
    (APIMGovernancePolicyDTO dto) {
        APIMGovernancePolicy govPolicy = new APIMGovernancePolicy();
        govPolicy.setId(dto.getId());
        govPolicy.setName(dto.getName());
        govPolicy.setDescription(dto.getDescription());
        govPolicy.setCreatedBy(dto.getCreatedBy());
        govPolicy.setCreatedBy(dto.getCreatedBy());
        govPolicy.setUpdatedBy(dto.getUpdatedBy());
        govPolicy.setUpdatedTime(dto.getUpdatedTime());
        govPolicy.setRulesetIds(dto.getRulesets());
        govPolicy.setActions(fromActionDTOListtoActionList(dto.getActions()));
        govPolicy.setGovernableStates(dto.getGovernableStates().stream()
                .map(Enum::name)
                .map(APIMGovernableState::fromString)
                .collect(Collectors.toList()));

        List<String> labels = dto.getLabels();
        if (labels != null && labels.stream().anyMatch(label -> label
                .equalsIgnoreCase(APIMGovernanceConstants.GLOBAL_LABEL))) {
            govPolicy.setGlobal(true);
            govPolicy.setLabels(Collections.emptyList());
        } else {
            govPolicy.setLabels(labels);
        }

        return govPolicy;
    }

    /**
     * Converts a APIMGovernancePolicy object to a GovernancePolicyDTO object
     *
     * @param governancePolicy APIMGovernancePolicy object
     * @return GovernancePolicyDTO object
     */
    public static APIMGovernancePolicyDTO fromGovernancePolicyToGovernancePolicyDTO
    (APIMGovernancePolicy governancePolicy) {
        APIMGovernancePolicyDTO governancePolicyDTO = new APIMGovernancePolicyDTO();
        governancePolicyDTO.setId(governancePolicy.getId());
        governancePolicyDTO.setName(governancePolicy.getName());
        governancePolicyDTO.setDescription(governancePolicy.getDescription());
        governancePolicyDTO.setCreatedBy(governancePolicy.getCreatedBy());
        governancePolicyDTO.setCreatedTime(governancePolicy.getCreatedTime());
        governancePolicyDTO.setUpdatedBy(governancePolicy.getUpdatedBy());
        governancePolicyDTO.setUpdatedTime(governancePolicy.getUpdatedTime());
        governancePolicyDTO.setLabels(governancePolicy.getLabels());
        governancePolicyDTO.setRulesets(governancePolicy.getRulesetIds());
        governancePolicyDTO.setActions(
                fromActionListtoActionDTOList(governancePolicy.getActions()));
        governancePolicyDTO.setGovernableStates(governancePolicy.getGovernableStates().stream()
                .map(Enum::name)
                .map(APIMGovernancePolicyDTO.GovernableStatesEnum::valueOf)
                .collect(Collectors.toList()));
        if (governancePolicy.isGlobal()) {
            governancePolicyDTO.setLabels(new ArrayList<>(Collections
                    .singleton(APIMGovernanceConstants.GLOBAL_LABEL)));
        } else {
            governancePolicyDTO.setLabels(governancePolicy.getLabels());
        }
        return governancePolicyDTO;
    }

    /**
     * Converts a list of ActionDTO objects to a list of APIMGovernanceAction objects
     *
     * @param actions List of ActionDTO objects
     * @return List of APIMGovernanceAction objects
     */
    public static List<APIMGovernanceAction> fromActionDTOListtoActionList(List<ActionDTO> actions) {
        List<APIMGovernanceAction> governanceActions = new ArrayList<>();
        for (ActionDTO action : actions) {
            APIMGovernanceAction governanceAction = new APIMGovernanceAction();
            governanceAction.setGovernableState(APIMGovernableState.fromString(String.valueOf(
                    action.getState())));
            governanceAction.setRuleSeverity(RuleSeverity.fromString(String.valueOf(action.getRuleSeverity())));
            governanceAction.setType(APIMGovernanceActionType.fromString(String.valueOf(action.getType())));
            governanceActions.add(governanceAction);
        }
        return governanceActions;
    }

    /**
     * Converts a list of APIMGovernanceAction objects to a list of ActionDTO objects
     *
     * @param actions List of APIMGovernanceAction objects
     * @return List of ActionDTO objects
     */
    public static List<ActionDTO> fromActionListtoActionDTOList(List<APIMGovernanceAction> actions) {
        List<ActionDTO> actionDTOs = new ArrayList<>();
        for (APIMGovernanceAction action : actions) {
            ActionDTO actionDTO = new ActionDTO();
            String governableState = String.valueOf(action.getGovernableState());
            actionDTO.setState(ActionDTO.StateEnum.valueOf(governableState));
            String severity = String.valueOf(action.getRuleSeverity());
            actionDTO.setRuleSeverity(ActionDTO.RuleSeverityEnum.valueOf(severity));
            actionDTO.setType(ActionDTO.TypeEnum.valueOf(String.valueOf(action.getType())));
            actionDTOs.add(actionDTO);
        }
        return actionDTOs;
    }
}

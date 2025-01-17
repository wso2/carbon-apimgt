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

import org.wso2.carbon.apimgt.governance.api.model.GovernableState;
import org.wso2.carbon.apimgt.governance.api.model.GovernanceAction;
import org.wso2.carbon.apimgt.governance.api.model.GovernanceActionType;
import org.wso2.carbon.apimgt.governance.api.model.GovernancePolicy;
import org.wso2.carbon.apimgt.governance.api.model.GovernancePolicyList;
import org.wso2.carbon.apimgt.governance.api.model.Severity;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ActionDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.GovernancePolicyDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.GovernancePolicyListDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class represents the Policy Mapping Utility
 */
public class PolicyMappingUtil {

    /**
     * Converts a GovernancePolicyDTO object to
     * a GovernancePolicy object
     *
     * @param dto GovernancePolicyDTO object
     * @return GovernancePolicy object
     */
    public static GovernancePolicy fromDTOtoGovernancePolicy
    (GovernancePolicyDTO dto) {
        GovernancePolicy govPolicy = new GovernancePolicy();
        govPolicy.setId(dto.getId());
        govPolicy.setName(dto.getName());
        govPolicy.setDescription(dto.getDescription());
        govPolicy.setCreatedBy(dto.getCreatedBy());
        govPolicy.setCreatedBy(dto.getCreatedBy());
        govPolicy.setUpdatedBy(dto.getUpdatedBy());
        govPolicy.setUpdatedTime(dto.getUpdatedTime());
        govPolicy.setRulesetIds(dto.getRulesets());
        govPolicy.setLabels(dto.getLabels());
        govPolicy.setActions(fromActionDTOListtoActionList(dto.getActions()));
        govPolicy.setGovernableStates(dto.getGovernableStates().stream()
                .map(Enum::name)
                .map(GovernableState::fromString)
                .collect(Collectors.toList()));
        return govPolicy;
    }

    /**
     * Converts a GovernancePolicy object to a GovernancePolicyDTO object
     *
     * @param governancePolicy GovernancePolicy object
     * @return GovernancePolicyDTO object
     */
    public static GovernancePolicyDTO fromGovernancePolicyToGovernancePolicyDTO
    (GovernancePolicy governancePolicy) {
        GovernancePolicyDTO governancePolicyDTO = new GovernancePolicyDTO();
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
                .map(GovernancePolicyDTO.GovernableStatesEnum::valueOf)
                .collect(Collectors.toList()));
        return governancePolicyDTO;
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
        List<GovernancePolicyDTO> policyDTOList = new ArrayList<>();
        for (GovernancePolicy policy : policyList.getGovernancePolicyList()) {
            policyDTOList.add(fromGovernancePolicyToGovernancePolicyDTO(policy));
        }
        policyListDTO.setList(policyDTOList);
        return policyListDTO;
    }

    /**
     * Converts a list of ActionDTO objects to a list of GovernanceAction objects
     *
     * @param actions List of ActionDTO objects
     * @return List of GovernanceAction objects
     */
    public static List<GovernanceAction> fromActionDTOListtoActionList(List<ActionDTO> actions) {
        List<GovernanceAction> governanceActions = new ArrayList<>();
        for (ActionDTO action : actions) {
            GovernanceAction governanceAction = new GovernanceAction();
            governanceAction.setGovernableState(GovernableState.fromString(String.valueOf(
                    action.getState())));
            governanceAction.setRuleSeverity(Severity.fromString(String.valueOf(action.getRuleSeverity())));
            governanceAction.setType(GovernanceActionType.fromString(String.valueOf(action.getType())));
            governanceActions.add(governanceAction);
        }
        return governanceActions;
    }

    /**
     * Converts a list of GovernanceAction objects to a list of ActionDTO objects
     *
     * @param actions List of GovernanceAction objects
     * @return List of ActionDTO objects
     */
    public static List<ActionDTO> fromActionListtoActionDTOList(List<GovernanceAction> actions) {
        List<ActionDTO> actionDTOs = new ArrayList<>();
        for (GovernanceAction action : actions) {
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

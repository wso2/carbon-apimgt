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
import org.wso2.carbon.apimgt.governance.api.model.APIMGovernancePolicyAttachment;
import org.wso2.carbon.apimgt.governance.api.model.RuleSeverity;
import org.wso2.carbon.apimgt.governance.impl.APIMGovernanceConstants;
import org.wso2.carbon.apimgt.governance.rest.api.dto.APIMGovernancePolicyAttachmentDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ActionDTO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class represents the Policy Attachment Mapping Utility
 */
public class PolicyAttachmentMappingUtil {

    /**
     * Converts a GovernancePolicyAttachmentDTO object to a APIMGovernancePolicyAttachment object
     *
     * @param dto APIMGovernancePolicyAttachmentDTO object
     * @return APIMGovernancePolicyAttachment object
     */
    public static APIMGovernancePolicyAttachment fromDTOtoGovernancePolicyAttachment
    (APIMGovernancePolicyAttachmentDTO dto) {
        APIMGovernancePolicyAttachment attachment = new APIMGovernancePolicyAttachment();
        attachment.setId(dto.getId());
        attachment.setName(dto.getName());
        attachment.setDescription(dto.getDescription());
        attachment.setCreatedBy(dto.getCreatedBy());
        attachment.setCreatedBy(dto.getCreatedBy());
        attachment.setUpdatedBy(dto.getUpdatedBy());
        attachment.setUpdatedTime(dto.getUpdatedTime());
        attachment.setPolicyIds(dto.getPolicies());
        attachment.setActions(fromActionDTOListtoActionList(dto.getActions()));
        attachment.setGovernableStates(dto.getGovernableStates().stream()
                .map(Enum::name)
                .map(APIMGovernableState::fromString)
                .collect(Collectors.toList()));

        List<String> labels = dto.getLabels();
        if (labels != null && labels.stream().anyMatch(label -> label
                .equalsIgnoreCase(APIMGovernanceConstants.GLOBAL_LABEL))) {
            attachment.setGlobal(true);
            attachment.setLabels(Collections.emptyList());
        } else {
            attachment.setLabels(labels);
        }

        return attachment;
    }

    /**
     * Converts a APIMGovernancePolicyAttachment object to a GovernancePolicyAttachmentDTO object
     *
     * @param attachment APIMGovernancePolicyAttachment object
     * @return GovernancePolicyDTO object
     */
    public static APIMGovernancePolicyAttachmentDTO fromGovernancePolicyAttachmentToGovernancePolicyAttachmentDTO
    (APIMGovernancePolicyAttachment attachment) {
        APIMGovernancePolicyAttachmentDTO attachmentDTO = new APIMGovernancePolicyAttachmentDTO();
        attachmentDTO.setId(attachment.getId());
        attachmentDTO.setName(attachment.getName());
        attachmentDTO.setDescription(attachment.getDescription());
        attachmentDTO.setCreatedBy(attachment.getCreatedBy());
        attachmentDTO.setCreatedTime(attachment.getCreatedTime());
        attachmentDTO.setUpdatedBy(attachment.getUpdatedBy());
        attachmentDTO.setUpdatedTime(attachment.getUpdatedTime());
        attachmentDTO.setLabels(attachment.getLabels());
        attachmentDTO.setPolicies(attachment.getPolicyIds());
        attachmentDTO.setActions(
                fromActionListtoActionDTOList(attachment.getActions()));
        attachmentDTO.setGovernableStates(attachment.getGovernableStates().stream()
                .map(Enum::name)
                .map(APIMGovernancePolicyAttachmentDTO.GovernableStatesEnum::valueOf)
                .collect(Collectors.toList()));
        if (attachment.isGlobal()) {
            attachmentDTO.setLabels(new ArrayList<>(Collections
                    .singleton(APIMGovernanceConstants.GLOBAL_LABEL)));
        } else {
            attachmentDTO.setLabels(attachment.getLabels());
        }
        return attachmentDTO;
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

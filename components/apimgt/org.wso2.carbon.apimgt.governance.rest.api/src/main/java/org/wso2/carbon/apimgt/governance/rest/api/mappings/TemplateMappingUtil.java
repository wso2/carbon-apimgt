/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.governance.rest.api.mappings;

import org.wso2.carbon.apimgt.governance.api.model.DevportalGovernanceKeyManagerScope;
import org.wso2.carbon.apimgt.governance.api.model.DevportalGovernanceRulesetBinding;
import org.wso2.carbon.apimgt.governance.api.model.DevportalGovernanceTemplate;
import org.wso2.carbon.apimgt.governance.rest.api.dto.DevportalGovernanceKeyManagerScopeDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.DevportalGovernanceRulesetBindingDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.DevportalGovernanceTemplateDTO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class represents the Devportal Governance Template Mapping Utility.
 */
public class TemplateMappingUtil {

    /**
     * Converts a DevportalGovernanceTemplateDTO object to a DevportalGovernanceTemplate object.
     *
     * @param templateDTO Template DTO
     * @return Template model
     */
    public static DevportalGovernanceTemplate fromDTOToDevportalGovernanceTemplate(
            DevportalGovernanceTemplateDTO templateDTO) {

        DevportalGovernanceTemplate template = new DevportalGovernanceTemplate();
        if (templateDTO == null) {
            return template;
        }
        template.setId(templateDTO.getId());
        template.setName(templateDTO.getName());
        template.setDescription(templateDTO.getDescription());
        template.setTags(templateDTO.getTags());
        template.setIcon(templateDTO.getIcon());
        template.setFormConfig(templateDTO.getFormConfig() == null ? Collections.emptyMap() :
                templateDTO.getFormConfig());
        template.setFormConfigHash(templateDTO.getFormConfigHash());
        if (templateDTO.getStatus() != null) {
            template.setStatus(templateDTO.getStatus().toString());
        }
        Boolean isDefault = templateDTO.isIsDefault();
        template.setDefault(isDefault != null && isDefault);
        Boolean isGlobal = templateDTO.isIsGlobal();
        template.setGlobal(isGlobal != null && isGlobal);
        template.setCreatedBy(templateDTO.getCreatedBy());
        template.setCreatedTime(templateDTO.getCreatedTime());
        template.setUpdatedBy(templateDTO.getUpdatedBy());
        template.setUpdatedTime(templateDTO.getUpdatedTime());
        template.setRulesetBindings(fromDTOListToRulesetBindingList(templateDTO.getRulesetBindings()));
        return template;
    }

    /**
     * Converts a DevportalGovernanceTemplate object to a DevportalGovernanceTemplateDTO object.
     *
     * @param template Template model
     * @return Template DTO
     */
    public static DevportalGovernanceTemplateDTO fromDevportalGovernanceTemplateToDTO(
            DevportalGovernanceTemplate template) {

        DevportalGovernanceTemplateDTO templateDTO = new DevportalGovernanceTemplateDTO();
        if (template == null) {
            return templateDTO;
        }
        templateDTO.setId(template.getId());
        templateDTO.setName(template.getName());
        templateDTO.setDescription(template.getDescription());
        templateDTO.setTags(template.getTags());
        templateDTO.setIcon(template.getIcon());
        templateDTO.setFormConfig(template.getFormConfig());
        templateDTO.setFormConfigHash(template.getFormConfigHash());
        templateDTO.setStatus(DevportalGovernanceTemplateDTO.StatusEnum.fromValue(template.getStatus()));
        templateDTO.setIsDefault(template.isDefault());
        templateDTO.setIsGlobal(template.isGlobal());
        templateDTO.setCreatedBy(template.getCreatedBy());
        templateDTO.setCreatedTime(template.getCreatedTime());
        templateDTO.setUpdatedBy(template.getUpdatedBy());
        templateDTO.setUpdatedTime(template.getUpdatedTime());
        templateDTO.setRulesetBindings(fromRulesetBindingListToDTOList(template.getRulesetBindings()));
        return templateDTO;
    }

    private static List<DevportalGovernanceRulesetBinding> fromDTOListToRulesetBindingList(
            List<DevportalGovernanceRulesetBindingDTO> bindingDTOs) {

        List<DevportalGovernanceRulesetBinding> bindings = new ArrayList<>();
        if (bindingDTOs == null) {
            return bindings;
        }
        for (int i = 0; i < bindingDTOs.size(); i++) {
            DevportalGovernanceRulesetBindingDTO bindingDTO = bindingDTOs.get(i);
            DevportalGovernanceRulesetBinding binding = new DevportalGovernanceRulesetBinding();
            binding.setBindingId(bindingDTO.getBindingId());
            binding.setRulesetId(bindingDTO.getRulesetId());
            binding.setRulesetDescription(bindingDTO.getRulesetDescription());
            binding.setDocumentationLink(bindingDTO.getDocumentationLink());
            binding.setBindingOrder(bindingDTO.getBindingOrder() == null ? i : bindingDTO.getBindingOrder());
            binding.setKeyManagerScopes(fromDTOListToKeyManagerScopeList(bindingDTO.getKeyManagerScopes()));
            bindings.add(binding);
        }
        return bindings;
    }

    private static List<DevportalGovernanceRulesetBindingDTO> fromRulesetBindingListToDTOList(
            List<DevportalGovernanceRulesetBinding> bindings) {

        List<DevportalGovernanceRulesetBindingDTO> bindingDTOs = new ArrayList<>();
        if (bindings == null) {
            return bindingDTOs;
        }
        for (DevportalGovernanceRulesetBinding binding : bindings) {
            DevportalGovernanceRulesetBindingDTO bindingDTO = new DevportalGovernanceRulesetBindingDTO();
            bindingDTO.setBindingId(binding.getBindingId());
            bindingDTO.setRulesetId(binding.getRulesetId());
            bindingDTO.setRulesetName(binding.getRulesetName());
            bindingDTO.setRulesetDescription(binding.getRulesetDescription());
            bindingDTO.setDocumentationLink(binding.getDocumentationLink());
            bindingDTO.setRuleType(binding.getRuleType());
            bindingDTO.setArtifactType(binding.getArtifactType());
            bindingDTO.setBindingOrder(binding.getBindingOrder());
            bindingDTO.setKeyManagerScopes(fromKeyManagerScopeListToDTOList(binding.getKeyManagerScopes()));
            bindingDTOs.add(bindingDTO);
        }
        return bindingDTOs;
    }

    private static List<DevportalGovernanceKeyManagerScope> fromDTOListToKeyManagerScopeList(
            List<DevportalGovernanceKeyManagerScopeDTO> keyManagerScopeDTOs) {

        List<DevportalGovernanceKeyManagerScope> keyManagerScopes = new ArrayList<>();
        if (keyManagerScopeDTOs == null) {
            return keyManagerScopes;
        }
        for (DevportalGovernanceKeyManagerScopeDTO keyManagerScopeDTO : keyManagerScopeDTOs) {
            DevportalGovernanceKeyManagerScope keyManagerScope = new DevportalGovernanceKeyManagerScope();
            keyManagerScope.setKeyManagerUuid(keyManagerScopeDTO.getKeyManagerUuid());
            keyManagerScope.setOrganization(keyManagerScopeDTO.getOrganization());
            keyManagerScopes.add(keyManagerScope);
        }
        return keyManagerScopes;
    }

    private static List<DevportalGovernanceKeyManagerScopeDTO> fromKeyManagerScopeListToDTOList(
            List<DevportalGovernanceKeyManagerScope> keyManagerScopes) {

        List<DevportalGovernanceKeyManagerScopeDTO> keyManagerScopeDTOs = new ArrayList<>();
        if (keyManagerScopes == null) {
            return keyManagerScopeDTOs;
        }
        for (DevportalGovernanceKeyManagerScope keyManagerScope : keyManagerScopes) {
            DevportalGovernanceKeyManagerScopeDTO keyManagerScopeDTO = new DevportalGovernanceKeyManagerScopeDTO();
            keyManagerScopeDTO.setKeyManagerUuid(keyManagerScope.getKeyManagerUuid());
            keyManagerScopeDTO.setOrganization(keyManagerScope.getOrganization());
            keyManagerScopeDTOs.add(keyManagerScopeDTO);
        }
        return keyManagerScopeDTOs;
    }
}

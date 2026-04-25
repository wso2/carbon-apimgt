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

package org.wso2.carbon.apimgt.governance.impl;

import org.wso2.carbon.apimgt.governance.api.error.APIMGovExceptionCodes;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;
import org.wso2.carbon.apimgt.governance.api.model.DevportalGovernanceKeyManagerScope;
import org.wso2.carbon.apimgt.governance.api.model.DevportalGovernanceRulesetBinding;
import org.wso2.carbon.apimgt.governance.api.model.DevportalGovernanceTemplate;
import org.wso2.carbon.apimgt.governance.api.model.DevportalGovernanceTemplateList;
import org.wso2.carbon.apimgt.governance.impl.dao.DevportalGovernanceDAO;
import org.wso2.carbon.apimgt.governance.impl.util.APIMGovernanceUtil;
import org.wso2.carbon.apimgt.governance.impl.util.AuditLogger;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Manager for Admin Portal Devportal Governance templates.
 */
public class DevportalGovernanceManager {

    private final DevportalGovernanceDAO devportalGovernanceDAO;

    public DevportalGovernanceManager() {

        devportalGovernanceDAO = DevportalGovernanceDAO.getInstance();
    }

    /**
     * Create a Devportal Governance template.
     *
     * @param template     template
     * @param organization organization
     * @return created template
     * @throws APIMGovernanceException if an error occurs while creating the template
     */
    public DevportalGovernanceTemplate createTemplate(DevportalGovernanceTemplate template, String organization)
            throws APIMGovernanceException {

        validateTemplate(template);
        DevportalGovernanceTemplate existingTemplate =
                devportalGovernanceDAO.getTemplateByName(template.getName(), organization);
        if (existingTemplate != null) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.DEVPORTAL_TEMPLATE_ALREADY_EXISTS,
                    template.getName(), organization);
        }
        validateGlobalTemplatePrivilege(template, organization, template.getCreatedBy());
        template.setId(APIMGovernanceUtil.generateUUID());
        template.setOrganization(organization);
        if (isBlank(template.getStatus())) {
            template.setStatus(DevportalGovernanceTemplate.STATUS_DRAFT);
        }
        prepareRulesetBindings(template, organization);

        DevportalGovernanceTemplate createdTemplate =
                devportalGovernanceDAO.createTemplate(template, organization);
        AuditLogger.log("Devportal Governance Template",
                "Template %s with id %s created by user %s in organization %s",
                createdTemplate.getName(), createdTemplate.getId(), template.getCreatedBy(), organization);
        return createdTemplate;
    }

    /**
     * Update a Devportal Governance template.
     *
     * @param templateId   template ID
     * @param template     template
     * @param organization organization
     * @return updated template
     * @throws APIMGovernanceException if an error occurs while updating the template
     */
    public DevportalGovernanceTemplate updateTemplate(String templateId, DevportalGovernanceTemplate template,
                                                      String organization) throws APIMGovernanceException {

        DevportalGovernanceTemplate existingTemplate = getTemplateById(templateId, organization);
        validateGlobalTemplateMutation(existingTemplate, organization, template.getUpdatedBy());
        validateTemplate(template);
        validateGlobalTemplatePrivilege(template, organization, template.getUpdatedBy());
        DevportalGovernanceTemplate existingTemplateByName =
                devportalGovernanceDAO.getTemplateByName(template.getName(), organization);
        if (existingTemplateByName != null && !existingTemplateByName.getId().equals(templateId)) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.DEVPORTAL_TEMPLATE_ALREADY_EXISTS,
                    template.getName(), organization);
        }
        template.setId(templateId);
        template.setOrganization(organization);
        if (isBlank(template.getStatus())) {
            template.setStatus(existingTemplate.getStatus());
        }
        prepareRulesetBindings(template, organization);

        DevportalGovernanceTemplate updatedTemplate =
                devportalGovernanceDAO.updateTemplate(templateId, template, organization);
        AuditLogger.log("Devportal Governance Template",
                "Template %s with id %s updated by user %s in organization %s",
                updatedTemplate.getName(), updatedTemplate.getId(), template.getUpdatedBy(), organization);
        return updatedTemplate;
    }

    /**
     * Delete a Devportal Governance template.
     *
     * @param templateId   template ID
     * @param username     username
     * @param organization organization
     * @throws APIMGovernanceException if an error occurs while deleting the template
     */
    public void deleteTemplate(String templateId, String username, String organization) throws APIMGovernanceException {

        DevportalGovernanceTemplate existingTemplate = getTemplateById(templateId, organization);
        validateGlobalTemplateMutation(existingTemplate, organization, username);
        devportalGovernanceDAO.deleteTemplate(templateId, organization);
        AuditLogger.log("Devportal Governance Template",
                "Template %s with id %s deleted by user %s in organization %s",
                existingTemplate.getName(), existingTemplate.getId(), username, organization);
    }

    /**
     * Get a Devportal Governance template by ID.
     *
     * @param templateId   template ID
     * @param organization organization
     * @return template
     * @throws APIMGovernanceException if the template is not found or an error occurs while retrieving it
     */
    public DevportalGovernanceTemplate getTemplateById(String templateId, String organization)
            throws APIMGovernanceException {

        DevportalGovernanceTemplate template = devportalGovernanceDAO.getTemplateById(templateId, organization);
        if (template == null) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.DEVPORTAL_TEMPLATE_NOT_FOUND, templateId);
        }
        return template;
    }

    /**
     * Get the default Devportal Governance template.
     *
     * @param organization organization
     * @return default template
     * @throws APIMGovernanceException if the template is not found or an error occurs while retrieving it
     */
    public DevportalGovernanceTemplate getDefaultTemplate(String organization) throws APIMGovernanceException {

        DevportalGovernanceTemplate template = devportalGovernanceDAO.getDefaultTemplate(organization);
        if (template == null) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.DEVPORTAL_TEMPLATE_NOT_FOUND, "default");
        }
        return template;
    }

    /**
     * Get all Devportal Governance templates.
     *
     * @param organization organization
     * @return template list
     * @throws APIMGovernanceException if an error occurs while retrieving templates
     */
    public DevportalGovernanceTemplateList getTemplates(String organization) throws APIMGovernanceException {

        return devportalGovernanceDAO.getTemplates(organization);
    }

    private void validateTemplate(DevportalGovernanceTemplate template) throws APIMGovernanceException {

        if (template == null) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.BAD_REQUEST,
                    "Devportal Governance template payload is required");
        }
        if (isBlank(template.getName())) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.BAD_REQUEST,
                    "Devportal Governance template name is required");
        }
    }

    private void validateGlobalTemplatePrivilege(DevportalGovernanceTemplate template, String organization,
                                                 String username) throws APIMGovernanceException {

        if (template.isGlobal() && !canManageGlobalTemplates(organization, username)) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.BAD_REQUEST,
                    "Only super-tenant administrators can create or update global Devportal Governance templates");
        }
    }

    private void validateGlobalTemplateMutation(DevportalGovernanceTemplate template, String organization,
                                                String username) throws APIMGovernanceException {

        if (template.isGlobal() && !canManageGlobalTemplates(organization, username)) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.BAD_REQUEST,
                    "Global Devportal Governance templates are read-only for tenant administrators");
        }
    }

    private boolean canManageGlobalTemplates(String organization, String username) {

        if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(organization)) {
            return true;
        }
        return username != null &&
                MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(MultitenantUtils.getTenantDomain(username));
    }

    private void prepareRulesetBindings(DevportalGovernanceTemplate template, String organization)
            throws APIMGovernanceException {

        List<DevportalGovernanceRulesetBinding> preparedBindings = new ArrayList<>();
        Set<String> rulesetIds = new HashSet<>();
        Set<Integer> bindingOrders = new HashSet<>();
        int bindingOrder = 0;
        for (DevportalGovernanceRulesetBinding binding : template.getRulesetBindings()) {
            if (isBlank(binding.getRulesetId())) {
                throw new APIMGovernanceException(APIMGovExceptionCodes.BAD_REQUEST,
                        "Ruleset ID is required for each template binding");
            }
            if (!rulesetIds.add(binding.getRulesetId())) {
                throw new APIMGovernanceException(APIMGovExceptionCodes.BAD_REQUEST,
                        "Duplicate ruleset bindings are not allowed for a template");
            }
            if (!devportalGovernanceDAO.isRulesetInOrganization(binding.getRulesetId(), organization)) {
                throw new APIMGovernanceException(APIMGovExceptionCodes.RULESET_NOT_FOUND, binding.getRulesetId());
            }
            if (isBlank(binding.getBindingId())) {
                binding.setBindingId(APIMGovernanceUtil.generateUUID());
            }
            binding.setTemplateId(template.getId());
            binding.setBindingOrder(binding.getBindingOrder() >= 0 ? binding.getBindingOrder() : bindingOrder);
            if (!bindingOrders.add(binding.getBindingOrder())) {
                throw new APIMGovernanceException(APIMGovExceptionCodes.BAD_REQUEST,
                        "Duplicate binding orders are not allowed for a template");
            }
            binding.setKeyManagerScopes(prepareKeyManagerScopes(binding, organization));
            preparedBindings.add(binding);
            bindingOrder++;
        }
        template.setRulesetBindings(preparedBindings);
    }

    private List<DevportalGovernanceKeyManagerScope> prepareKeyManagerScopes(
            DevportalGovernanceRulesetBinding binding, String organization) throws APIMGovernanceException {

        List<DevportalGovernanceKeyManagerScope> preparedScopes = new ArrayList<>();
        Set<String> keyManagerUuids = new HashSet<>();
        for (DevportalGovernanceKeyManagerScope keyManagerScope : binding.getKeyManagerScopes()) {
            if (isBlank(keyManagerScope.getKeyManagerUuid())) {
                throw new APIMGovernanceException(APIMGovExceptionCodes.BAD_REQUEST,
                        "Key Manager UUID is required for each template ruleset key manager scope");
            }
            if (!keyManagerUuids.add(keyManagerScope.getKeyManagerUuid())) {
                throw new APIMGovernanceException(APIMGovExceptionCodes.BAD_REQUEST,
                        "Duplicate key manager scopes are not allowed for a template ruleset binding");
            }
            keyManagerScope.setBindingId(binding.getBindingId());
            keyManagerScope.setOrganization(organization);
            preparedScopes.add(keyManagerScope);
        }
        return preparedScopes;
    }

    private boolean isBlank(String value) {

        return value == null || value.trim().isEmpty();
    }
}

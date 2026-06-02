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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;
import org.wso2.carbon.apimgt.governance.api.model.DevportalGovernanceTemplate;
import org.wso2.carbon.apimgt.governance.impl.dao.DevportalGovernanceDAO;
import org.wso2.carbon.apimgt.governance.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Reconciles stored Devportal Governance template form configs with deterministic server-side settings.
 */
public final class DevportalGovernanceTemplateConfigSynchronizer {

    private static final Log log = LogFactory.getLog(DevportalGovernanceTemplateConfigSynchronizer.class);
    private static final String SYSTEM_USER = "system";

    private static final String SECTION_APPLICATION = "application";
    private static final String FIELD_GROUPS = "groups";
    private static final String FIELD_ATTRIBUTES = "attributes";
    private static final String KEY_ACTIVE = "active";
    private static final String KEY_HIDDEN = "hidden";
    private static final String KEY_REQUIRED = "required";
    private static final String KEY_DEFAULT_VALUE = "defaultValue";

    private DevportalGovernanceTemplateConfigSynchronizer() {

    }

    /**
     * Scan all stored templates once during server startup and mark server-backed fields active/inactive.
     */
    public static void synchronizeAllTemplates() {

        DevportalGovernanceDAO dao = DevportalGovernanceDAO.getInstance();
        boolean applicationSharingEnabled = APIUtil.isMultiGroupAppSharingEnabled();
        int updatedCount = 0;
        try {
            List<DevportalGovernanceTemplate> templates = dao.getAllTemplates();
            Map<String, JSONArray> attributesByOrganization = new HashMap<>();
            for (DevportalGovernanceTemplate template : templates) {
                JSONArray applicationAttributes = attributesByOrganization.computeIfAbsent(
                        template.getOrganization(),
                        DevportalGovernanceTemplateConfigSynchronizer::getApplicationAttributes);
                Map<String, Object> currentFormConfig = template.getFormConfig();
                Map<String, Object> reconciledFormConfig = reconcileFormConfig(currentFormConfig,
                        applicationSharingEnabled, applicationAttributes);
                if (!reconciledFormConfig.equals(currentFormConfig)) {
                    dao.updateTemplateFormConfig(template.getId(), reconciledFormConfig, template.getOrganization(),
                            SYSTEM_USER);
                    updatedCount++;
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("Synchronized Devportal Governance template server-backed fields. Updated templates: "
                        + updatedCount);
            }
        } catch (APIMGovernanceException e) {
            log.warn("Error while synchronizing Devportal Governance template server-backed fields", e);
        }
    }

    private static JSONArray getApplicationAttributes(String organization) {

        try {
            JSONObject applicationConfig = APIUtil.getAppAttributeKeysFromRegistry(organization);
            if (applicationConfig != null) {
                Object attributes = applicationConfig.get(APIConstants.ApplicationAttributes.ATTRIBUTES);
                return attributes instanceof JSONArray ? (JSONArray) attributes : new JSONArray();
            }
        } catch (APIManagementException e) {
            if (log.isDebugEnabled()) {
                log.debug("Unable to load tenant application attributes for organization " + organization, e);
            }
        }
        APIManagerConfiguration configuration = ServiceReferenceHolder.getInstance()
                .getAPIMConfigurationService().getAPIManagerConfiguration();
        JSONArray attributes = configuration != null ? configuration.getApplicationAttributes() : null;
        return attributes != null ? attributes : new JSONArray();
    }

    private static Map<String, Object> reconcileFormConfig(Map<String, Object> formConfig,
                                                           boolean applicationSharingEnabled,
                                                           JSONArray applicationAttributes) {

        Map<String, Object> reconciledFormConfig = copyMap(formConfig);
        Map<String, Object> application = copyMap(reconciledFormConfig.get(SECTION_APPLICATION));
        Map<String, Object> groups = copyMap(application.get(FIELD_GROUPS));
        if (groups.isEmpty()) {
            groups.put(KEY_HIDDEN, false);
            groups.put(KEY_REQUIRED, false);
            groups.put(KEY_DEFAULT_VALUE, "");
        }
        groups.put(KEY_ACTIVE, applicationSharingEnabled);
        application.put(FIELD_GROUPS, groups);
        application.put(FIELD_ATTRIBUTES,
                reconcileAttributes(application.get(FIELD_ATTRIBUTES), applicationAttributes));
        reconciledFormConfig.put(SECTION_APPLICATION, application);
        return reconciledFormConfig;
    }

    private static Map<String, Object> reconcileAttributes(Object attributesConfig, JSONArray applicationAttributes) {

        Set<String> currentAttributeNames = getAttributeNames(applicationAttributes);
        Map<String, Object> existingAttributes = copyMap(attributesConfig);
        Map<String, Object> reconciledAttributes = new LinkedHashMap<>();

        for (Map.Entry<String, Object> entry : existingAttributes.entrySet()) {
            Map<String, Object> attributeConfig = copyMap(entry.getValue());
            attributeConfig.put(KEY_ACTIVE, currentAttributeNames.contains(entry.getKey()));
            reconciledAttributes.put(entry.getKey(), attributeConfig);
        }
        for (String attributeName : currentAttributeNames) {
            Map<String, Object> attributeConfig = copyMap(reconciledAttributes.get(attributeName));
            if (attributeConfig.isEmpty()) {
                attributeConfig.put(KEY_HIDDEN, false);
                attributeConfig.put(KEY_REQUIRED, false);
                attributeConfig.put(KEY_DEFAULT_VALUE, "");
            }
            attributeConfig.put(KEY_ACTIVE, true);
            reconciledAttributes.put(attributeName, attributeConfig);
        }
        return reconciledAttributes;
    }

    private static Set<String> getAttributeNames(JSONArray applicationAttributes) {

        Set<String> attributeNames = new LinkedHashSet<>();
        for (Object object : applicationAttributes) {
            if (!(object instanceof JSONObject)) {
                continue;
            }
            Object attributeName = ((JSONObject) object).get(APIConstants.ApplicationAttributes.ATTRIBUTE);
            if (attributeName != null && !String.valueOf(attributeName).trim().isEmpty()) {
                attributeNames.add(String.valueOf(attributeName));
            }
        }
        return attributeNames;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> copyMap(Object value) {

        if (!(value instanceof Map)) {
            return new LinkedHashMap<>();
        }
        return new LinkedHashMap<>((Map<String, Object>) value);
    }
}

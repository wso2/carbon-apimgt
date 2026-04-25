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

package org.wso2.carbon.apimgt.rest.api.store.v1.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ErrorHandler;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;
import org.wso2.carbon.apimgt.governance.api.model.DevportalGovernanceApplicationSnapshot;
import org.wso2.carbon.apimgt.governance.api.model.DevportalGovernanceRulesetSnapshot;
import org.wso2.carbon.apimgt.governance.api.model.DevportalGovernanceRulesetSnapshotKeyManagerScope;
import org.wso2.carbon.apimgt.governance.api.model.RuleViolation;
import org.wso2.carbon.apimgt.governance.impl.DevportalGovernanceValidator;
import org.wso2.carbon.apimgt.governance.impl.dao.DevportalGovernanceDAO;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationKeyGenerateRequestDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.SubscriptionDTO;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Strict synchronous Devportal Governance validation helpers for REST API gates.
 */
public final class DevportalGovernanceValidationUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final long DEVPORTAL_GOVERNANCE_VALIDATION_ERROR_CODE = 990800L;
    private static final long DEVPORTAL_GOVERNANCE_VIOLATION_ERROR_CODE = 990801L;
    private static final String GOVERNANCE_VALIDATION_ERROR_MESSAGE = "Devportal Governance validation failed";

    private DevportalGovernanceValidationUtil() {

    }

    public static void validateApplicationCreate(ApplicationDTO applicationDTO, String organization, Log log)
            throws APIManagementException {

        try {
            List<DevportalGovernanceRulesetSnapshot> rulesets = DevportalGovernanceDAO.getInstance()
                    .getRulesetSnapshotsForTemplate(applicationDTO.getTemplateId(), organization);
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("operation", "POST /applications");
            payload.put("organization", organization);
            payload.put("templateId", applicationDTO.getTemplateId());
            payload.put("application", applicationDTO);
            validatePayload(payload, rulesets, null, organization, log);
        } catch (APIMGovernanceException | JsonProcessingException e) {
            RestApiUtil.handleInternalServerError("Error while validating Devportal Governance application creation",
                    e, log);
        }
    }

    public static void captureApplicationSnapshot(Application application, String templateId, String organization,
                                                  Log log) throws APIManagementException {

        if (application == null) {
            return;
        }
        try {
            DevportalGovernanceDAO.getInstance().captureApplicationSnapshot(application.getId(), application.getUUID(),
                    templateId, resolveOrganization(application, organization));
        } catch (APIMGovernanceException e) {
            RestApiUtil.handleInternalServerError("Error while capturing Devportal Governance application snapshot",
                    e, log);
        }
    }

    public static void validateApplicationUpdate(Application application, ApplicationDTO applicationDTO,
                                                 String organization, Log log) throws APIManagementException {

        try {
            DevportalGovernanceApplicationSnapshot snapshot = getOrCaptureSnapshot(application, organization, log);
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("operation", "PUT /applications/{id}");
            payload.put("organization", resolveOrganization(application, organization));
            payload.put("applicationId", application.getUUID());
            payload.put("application", applicationDTO);
            validatePayload(payload, snapshot, log);
        } catch (APIMGovernanceException | JsonProcessingException e) {
            RestApiUtil.handleInternalServerError("Error while validating Devportal Governance application update",
                    e, log);
        }
    }

    public static void validateSubscriptionCreate(Application application, SubscriptionDTO subscriptionDTO,
                                                  ApiTypeWrapper apiTypeWrapper, String organization, Log log)
            throws APIManagementException {

        try {
            DevportalGovernanceApplicationSnapshot snapshot = getOrCaptureSnapshot(application, organization, log);
            Map<String, Object> apiPayload = new LinkedHashMap<>();
            apiPayload.put("apiId", apiTypeWrapper.getUuid());
            apiPayload.put("name", apiTypeWrapper.getName());
            apiPayload.put("context", apiTypeWrapper.getContext());
            apiPayload.put("type", apiTypeWrapper.getType());
            apiPayload.put("lifecycleState", apiTypeWrapper.getLifecycleState());
            apiPayload.put("businessPlan", apiTypeWrapper.getTier());
            apiPayload.put("apiProduct", apiTypeWrapper.isAPIProduct());

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("operation", "POST /subscriptions");
            payload.put("organization", resolveOrganization(application, organization));
            payload.put("applicationId", application.getUUID());
            payload.put("subscription", subscriptionDTO);
            payload.put("api", apiPayload);
            validatePayload(payload, snapshot, log);
        } catch (APIMGovernanceException | JsonProcessingException e) {
            RestApiUtil.handleInternalServerError("Error while validating Devportal Governance subscription creation",
                    e, log);
        }
    }

    public static void validateKeyGeneration(Application application,
                                             ApplicationKeyGenerateRequestDTO keyGenerateRequestDTO,
                                             String organization, Log log) throws APIManagementException {

        if (keyGenerateRequestDTO == null) {
            RestApiUtil.handleBadRequest("Key generation request body is required", log);
            return;
        }
        try {
            DevportalGovernanceApplicationSnapshot snapshot = getOrCaptureSnapshot(application, organization, log);
            String keyManagerName = StringUtils.defaultIfBlank(keyGenerateRequestDTO.getKeyManager(),
                    APIConstants.KeyManager.DEFAULT_KEY_MANAGER);
            List<DevportalGovernanceRulesetSnapshot> keyManagerRulesets =
                    filterRulesetsByKeyManager(snapshot.getRulesetSnapshots(), keyManagerName);

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("operation", "POST /applications/{id}/generate-keys");
            payload.put("organization", resolveOrganization(application, organization));
            payload.put("applicationId", application.getUUID());
            payload.put("keyManager", keyManagerName);
            payload.put("keyType", keyGenerateRequestDTO.getKeyType() == null ? null :
                    keyGenerateRequestDTO.getKeyType().toString());
            payload.put("grantTypesToBeSupported", keyGenerateRequestDTO.getGrantTypesToBeSupported());
            payload.put("callbackUrl", keyGenerateRequestDTO.getCallbackUrl());
            payload.put("scopes", keyGenerateRequestDTO.getScopes());
            payload.put("validityTime", keyGenerateRequestDTO.getValidityTime());
            payload.put("clientId", keyGenerateRequestDTO.getClientId());
            payload.put("hasClientSecret", StringUtils.isNotBlank(keyGenerateRequestDTO.getClientSecret()));
            payload.put("additionalProperties", keyGenerateRequestDTO.getAdditionalProperties());
            validatePayload(payload, keyManagerRulesets, application.getUUID(),
                    resolveOrganization(application, organization), log);
        } catch (APIMGovernanceException | JsonProcessingException e) {
            RestApiUtil.handleInternalServerError("Error while validating Devportal Governance key generation",
                    e, log);
        }
    }

    private static DevportalGovernanceApplicationSnapshot getOrCaptureSnapshot(Application application,
                                                                               String organization, Log log)
            throws APIMGovernanceException, APIManagementException {

        DevportalGovernanceApplicationSnapshot snapshot = DevportalGovernanceDAO.getInstance()
                .getApplicationSnapshot(application.getId());
        if (snapshot != null) {
            return snapshot;
        }
        return DevportalGovernanceDAO.getInstance().captureApplicationSnapshot(application.getId(),
                application.getUUID(), null, resolveOrganization(application, organization));
    }

    private static void validatePayload(Map<String, Object> payload,
                                        DevportalGovernanceApplicationSnapshot snapshot, Log log)
            throws APIMGovernanceException, JsonProcessingException {

        if (snapshot == null) {
            RestApiUtil.handleInternalServerError("Devportal Governance application snapshot was not found", log);
        }
        List<RuleViolation> violations = new DevportalGovernanceValidator()
                .validate(OBJECT_MAPPER.writeValueAsString(payload), snapshot);
        handleViolations(violations, log);
    }

    private static void validatePayload(Map<String, Object> payload,
                                        List<DevportalGovernanceRulesetSnapshot> rulesets,
                                        String applicationUuid, String organization, Log log)
            throws APIMGovernanceException, JsonProcessingException {

        List<RuleViolation> violations = new DevportalGovernanceValidator()
                .validate(OBJECT_MAPPER.writeValueAsString(payload), rulesets, applicationUuid, organization);
        handleViolations(violations, log);
    }

    private static List<DevportalGovernanceRulesetSnapshot> filterRulesetsByKeyManager(
            List<DevportalGovernanceRulesetSnapshot> rulesetSnapshots, String keyManagerName) {

        List<DevportalGovernanceRulesetSnapshot> matchedRulesets = new ArrayList<>();
        for (DevportalGovernanceRulesetSnapshot rulesetSnapshot : rulesetSnapshots) {
            List<DevportalGovernanceRulesetSnapshotKeyManagerScope> keyManagerScopes =
                    rulesetSnapshot.getKeyManagerScopes();
            if (keyManagerScopes.isEmpty()) {
                matchedRulesets.add(rulesetSnapshot);
                continue;
            }
            for (DevportalGovernanceRulesetSnapshotKeyManagerScope keyManagerScope : keyManagerScopes) {
                if (StringUtils.equalsIgnoreCase(keyManagerName, keyManagerScope.getKeyManagerName()) ||
                        StringUtils.equalsIgnoreCase(keyManagerName, keyManagerScope.getKeyManagerUuid())) {
                    matchedRulesets.add(rulesetSnapshot);
                    break;
                }
            }
        }
        return matchedRulesets;
    }

    private static void handleViolations(List<RuleViolation> violations, Log log) {

        if (violations == null || violations.isEmpty()) {
            return;
        }
        List<ErrorHandler> errorHandlers = new ArrayList<>();
        errorHandlers.add(new DevportalGovernanceErrorHandler(DEVPORTAL_GOVERNANCE_VALIDATION_ERROR_CODE,
                GOVERNANCE_VALIDATION_ERROR_MESSAGE,
                "Request blocked by Devportal Governance. See error details for rule violations."));
        for (RuleViolation violation : violations) {
            errorHandlers.add(new DevportalGovernanceErrorHandler(DEVPORTAL_GOVERNANCE_VIOLATION_ERROR_CODE,
                    StringUtils.defaultIfBlank(violation.getRuleName(), GOVERNANCE_VALIDATION_ERROR_MESSAGE),
                    getViolationDescription(violation)));
        }
        RestApiUtil.handleBadRequest(errorHandlers, log);
    }

    private static String getViolationDescription(RuleViolation violation) {

        StringBuilder description = new StringBuilder();
        appendViolationPart(description, "message", violation.getRuleMessage());
        appendViolationPart(description, "path", violation.getViolatedPath());
        appendViolationPart(description, "severity",
                violation.getSeverity() == null ? null : violation.getSeverity().toString());
        appendViolationPart(description, "rulesetId", violation.getRulesetId());
        return description.toString();
    }

    private static void appendViolationPart(StringBuilder builder, String name, String value) {

        if (StringUtils.isBlank(value)) {
            return;
        }
        if (builder.length() > 0) {
            builder.append(", ");
        }
        builder.append(name).append(": ").append(value);
    }

    private static String resolveOrganization(Application application, String organization) {

        if (StringUtils.isNotBlank(organization)) {
            return organization;
        }
        return application == null ? null : application.getOrganization();
    }

    private static class DevportalGovernanceErrorHandler implements ErrorHandler {

        private final long errorCode;
        private final String errorMessage;
        private final String errorDescription;

        DevportalGovernanceErrorHandler(long errorCode, String errorMessage, String errorDescription) {

            this.errorCode = errorCode;
            this.errorMessage = errorMessage;
            this.errorDescription = errorDescription;
        }

        @Override
        public long getErrorCode() {

            return errorCode;
        }

        @Override
        public String getErrorMessage() {

            return errorMessage;
        }

        @Override
        public String getErrorDescription() {

            return errorDescription;
        }

        @Override
        public int getHttpStatusCode() {

            return 400;
        }

        @Override
        public boolean printStackTrace() {

            return false;
        }
    }
}

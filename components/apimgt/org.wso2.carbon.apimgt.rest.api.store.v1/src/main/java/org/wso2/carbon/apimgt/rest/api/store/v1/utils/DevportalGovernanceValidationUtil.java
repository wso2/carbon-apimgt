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
import org.wso2.carbon.apimgt.governance.api.error.APIMGovExceptionCodes;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;
import org.wso2.carbon.apimgt.governance.api.model.DevportalGovernanceApplicationSnapshot;
import org.wso2.carbon.apimgt.governance.api.model.DevportalGovernanceRulesetSnapshot;
import org.wso2.carbon.apimgt.governance.api.model.DevportalGovernanceRulesetSnapshotKeyManagerScope;
import org.wso2.carbon.apimgt.governance.api.model.KeyManagerGovernanceContext;
import org.wso2.carbon.apimgt.governance.api.model.RuleViolation;
import org.wso2.carbon.apimgt.governance.impl.DevportalGovernanceValidator;
import org.wso2.carbon.apimgt.governance.impl.dao.DevportalGovernanceDAO;
import org.wso2.carbon.apimgt.governance.impl.util.KeyManagerContextResolver;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationKeyDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationKeyGenerateRequestDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationKeyMappingRequestDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationTokenGenerateRequestDTO;
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

        // No templateId on the request → developer is creating an ungoverned application; skip.
        if (applicationDTO == null || StringUtils.isBlank(applicationDTO.getTemplateId())) {
            return;
        }
        try {
            List<DevportalGovernanceRulesetSnapshot> rulesets = DevportalGovernanceDAO.getInstance()
                    .getRulesetSnapshotsForTemplate(applicationDTO.getTemplateId(), organization);
            // Application-section rules only — see filterRulesetsByRuleType javadoc.
            rulesets = filterRulesetsByRuleType(rulesets, "APP_INFO");
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("operation", "POST /applications");
            payload.put("organization", organization);
            payload.put("templateId", applicationDTO.getTemplateId());
            payload.put("application", applicationDTO);
            validatePayload(payload, rulesets, null, organization, log);
        } catch (APIMGovernanceException e) {
            // Template-not-found is a USER-input issue (bad/deleted templateId), not a server
            // problem — return 400 instead of 500 so the developer gets a clear message.
            // Other governance failures still propagate as internal errors.
            if (e.getErrorHandler() != null
                    && e.getErrorHandler().getErrorCode() == APIMGovExceptionCodes.DEVPORTAL_TEMPLATE_NOT_FOUND
                            .getErrorCode()) {
                RestApiUtil.handleBadRequest("Referenced governance template '"
                        + applicationDTO.getTemplateId() + "' was not found.", log);
                return;
            }
            RestApiUtil.handleInternalServerError("Error while validating Devportal Governance application creation",
                    e, log);
        } catch (JsonProcessingException e) {
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
            // Application-section rules only.
            List<DevportalGovernanceRulesetSnapshot> appRulesets =
                    snapshot == null ? new ArrayList<>()
                            : filterRulesetsByRuleType(snapshot.getRulesetSnapshots(), "APP_INFO");
            validatePayload(payload, appRulesets, application.getUUID(),
                    resolveOrganization(application, organization), log);
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
            // Subscription-section rules only.
            List<DevportalGovernanceRulesetSnapshot> subRulesets =
                    snapshot == null ? new ArrayList<>()
                            : filterRulesetsByRuleType(snapshot.getRulesetSnapshots(), "APP_SUBSCRIPTION");
            validatePayload(payload, subRulesets, application.getUUID(),
                    resolveOrganization(application, organization), log);
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
            String resolvedOrg = resolveOrganization(application, organization);
            KeyManagerGovernanceContext kmContext = KeyManagerContextResolver.resolve(keyManagerName, resolvedOrg);
            // Capability gate: if the KM declares it cannot create OAuth clients, block at the API
            // layer before any KM-side DCR call. Spectral rules can still add policy on top.
            if (kmContext != null && !kmContext.isEnableOAuthAppCreation()) {
                RestApiUtil.handleBadRequest("Key Manager '" + kmContext.getDisplayName()
                        + "' does not allow OAuth application creation. "
                        + "Use map-keys to attach an existing OAuth client.", log);
                return;
            }

            // Allowed-list enforcement for grant types. The admin's selection in
            // formConfig.keyManagers[<km>].grantTypes.defaultValue is the constraint;
            // a developer cannot request grants outside that list. Empty list means
            // "no constraint — every grant the KM advertises is allowed".
            Map<String, Object> snapshotForm = snapshot == null ? null : snapshot.getFormConfig();
            List<String> allowedGrants = TemplateDefaultsApplier.readAllowedGrantTypes(
                    snapshotForm, keyManagerName);
            List<String> requestedGrants = keyGenerateRequestDTO.getGrantTypesToBeSupported();
            if (!allowedGrants.isEmpty() && requestedGrants != null) {
                for (String g : requestedGrants) {
                    if (g != null && !g.isEmpty() && !allowedGrants.contains(g)) {
                        RestApiUtil.handleBadRequest(
                                "Grant type '" + g + "' is not in the allowed list for this template "
                                + "and key manager. Allowed: " + allowedGrants, log);
                        return;
                    }
                }
            }
            List<DevportalGovernanceRulesetSnapshot> oauthRulesets =
                    filterRulesetsByRuleType(snapshot.getRulesetSnapshots(), "APP_OAUTH");
            List<DevportalGovernanceRulesetSnapshot> keyManagerRulesets =
                    filterRulesetsByKeyManager(oauthRulesets, keyManagerName);

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("operation", "POST /applications/{id}/generate-keys");
            payload.put("action", KeyManagerGovernanceContext.Action.OAUTH_APP_CREATE.name());
            payload.put("organization", resolvedOrg);
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
            payload.put("keyManagerContext", kmContext == null ? null : kmContext.toPayloadMap());
            validatePayload(payload, keyManagerRulesets, application.getUUID(), resolvedOrg, log);
        } catch (APIMGovernanceException | JsonProcessingException e) {
            RestApiUtil.handleInternalServerError("Error while validating Devportal Governance key generation",
                    e, log);
        }
    }

    /**
     * Govern the map-keys (out-of-band) flow. Blocks when:
     *   - the KM does not allow OAuth-app mapping ({@code enable_map_oauth_consumer_apps=false}),
     *   - or any KM-scoped ruleset bound to the application's snapshot fails Spectral evaluation.
     */
    public static void validateMapKeys(Application application, ApplicationKeyMappingRequestDTO mappingRequestDTO,
                                       String organization, Log log) throws APIManagementException {

        if (mappingRequestDTO == null) {
            RestApiUtil.handleBadRequest("Map keys request body is required", log);
            return;
        }
        try {
            DevportalGovernanceApplicationSnapshot snapshot = getOrCaptureSnapshot(application, organization, log);
            String keyManagerName = StringUtils.defaultIfBlank(mappingRequestDTO.getKeyManager(),
                    APIConstants.KeyManager.DEFAULT_KEY_MANAGER);
            String resolvedOrg = resolveOrganization(application, organization);
            KeyManagerGovernanceContext kmContext = KeyManagerContextResolver.resolve(keyManagerName, resolvedOrg);

            if (kmContext != null && !kmContext.isEnableMapOAuthConsumerApps()) {
                RestApiUtil.handleBadRequest("Key Manager '" + kmContext.getDisplayName()
                        + "' does not allow mapping out-of-band OAuth applications.", log);
                return;
            }

            List<DevportalGovernanceRulesetSnapshot> oauthRulesets =
                    filterRulesetsByRuleType(snapshot.getRulesetSnapshots(), "APP_OAUTH");
            List<DevportalGovernanceRulesetSnapshot> keyManagerRulesets =
                    filterRulesetsByKeyManager(oauthRulesets, keyManagerName);

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("operation", "POST /applications/{id}/map-keys");
            payload.put("action", KeyManagerGovernanceContext.Action.OAUTH_APP_MAP.name());
            payload.put("organization", resolvedOrg);
            payload.put("applicationId", application.getUUID());
            payload.put("keyManager", keyManagerName);
            payload.put("keyType", mappingRequestDTO.getKeyType() == null ? null
                    : mappingRequestDTO.getKeyType().toString());
            payload.put("consumerKey", mappingRequestDTO.getConsumerKey());
            payload.put("hasConsumerSecret", StringUtils.isNotBlank(mappingRequestDTO.getConsumerSecret()));
            payload.put("keyManagerContext", kmContext == null ? null : kmContext.toPayloadMap());
            validatePayload(payload, keyManagerRulesets, application.getUUID(), resolvedOrg, log);
        } catch (APIMGovernanceException | JsonProcessingException e) {
            RestApiUtil.handleInternalServerError("Error while validating Devportal Governance map keys",
                    e, log);
        }
    }

    /**
     * Govern update-oauth-app (PUT). Runs the OAUTH_APP_UPDATE Spectral payload through
     * KM-scoped rulesets — capability gating here is light because the KM-side update
     * call itself fails if the KM doesn't support modifications; rule authors decide
     * whether to clamp grant types, callback URL, expiry, etc.
     */
    public static void validateUpdateOAuthApp(Application application, String keyMappingId,
                                              ApplicationKeyDTO body, String organization, Log log)
            throws APIManagementException {

        if (body == null) {
            RestApiUtil.handleBadRequest("Update OAuth app request body is required", log);
            return;
        }
        Map<String, Object> extras = new LinkedHashMap<>();
        extras.put("keyMappingId", keyMappingId);
        extras.put("supportedGrantTypes", body.getSupportedGrantTypes());
        extras.put("callbackUrl", body.getCallbackUrl());
        extras.put("groupId", body.getGroupId());
        extras.put("additionalProperties", body.getAdditionalProperties());
        runOAuthLifecycleValidation(application, body.getKeyManager(),
                KeyManagerGovernanceContext.Action.OAUTH_APP_UPDATE,
                "PUT /applications/{id}/oauth-keys/{mappingId}",
                extras, organization, log, null);
    }

    /**
     * Govern access-token generation. Blocks at the API layer when the KM declares
     * {@code enable_token_generation=false}; otherwise feeds the request through KM-scoped
     * rulesets so policies can cap validity, restrict grants, etc.
     */
    public static void validateGenerateToken(Application application, String keyMappingId,
                                             String keyManagerName, ApplicationTokenGenerateRequestDTO body,
                                             String organization, Log log) throws APIManagementException {

        if (body == null) {
            RestApiUtil.handleBadRequest("Generate token request body is required", log);
            return;
        }
        Map<String, Object> extras = new LinkedHashMap<>();
        extras.put("keyMappingId", keyMappingId);
        extras.put("grantType", body.getGrantType() == null ? null : body.getGrantType().toString());
        extras.put("validityPeriod", body.getValidityPeriod());
        extras.put("scopes", body.getScopes());
        extras.put("additionalProperties", body.getAdditionalProperties());
        extras.put("hasRevokeToken", StringUtils.isNotBlank(body.getRevokeToken()));
        extras.put("hasConsumerSecret", StringUtils.isNotBlank(body.getConsumerSecret()));
        runOAuthLifecycleValidation(application, keyManagerName,
                KeyManagerGovernanceContext.Action.ACCESS_TOKEN_GENERATE,
                "POST /applications/{id}/oauth-keys/{mappingId}/generate-token",
                extras, organization, log,
                kmContext -> {
                    if (!kmContext.isEnableTokenGeneration()) {
                        return "Key Manager '" + kmContext.getDisplayName()
                                + "' does not allow APIM-side token generation.";
                    }
                    return null;
                });
    }

    /**
     * Govern consumer-secret rotation. The capability gate here only fires when the KM has
     * explicitly disabled token generation (rotation is meaningless without it). Per-policy
     * rotation rules (max age, max active secrets) belong in Spectral rulesets.
     */
    public static void validateRegenerateSecret(Application application, String keyMappingId,
                                                String keyManagerName, String organization, Log log)
            throws APIManagementException {

        Map<String, Object> extras = new LinkedHashMap<>();
        extras.put("keyMappingId", keyMappingId);
        runOAuthLifecycleValidation(application, keyManagerName,
                KeyManagerGovernanceContext.Action.CONSUMER_SECRET_REGEN,
                "POST /applications/{id}/oauth-keys/{mappingId}/regenerate-secret",
                extras, organization, log,
                kmContext -> {
                    if (!kmContext.isEnableTokenGeneration() && !kmContext.isEnableOAuthAppCreation()) {
                        return "Key Manager '" + kmContext.getDisplayName()
                                + "' does not allow consumer secret rotation.";
                    }
                    return null;
                });
    }

    /**
     * Govern cleanup or delete of an OAuth client. The action distinguishes the two so
     * rule authors can write different policies (e.g. allow cleanup of stale registrations,
     * but block deletion when the application has live subscriptions).
     */
    public static void validateOAuthAppRemoval(Application application, String keyMappingId,
                                                String keyManagerName,
                                                KeyManagerGovernanceContext.Action action,
                                                String organization, Log log)
            throws APIManagementException {

        Map<String, Object> extras = new LinkedHashMap<>();
        extras.put("keyMappingId", keyMappingId);
        runOAuthLifecycleValidation(application, keyManagerName, action,
                action == KeyManagerGovernanceContext.Action.OAUTH_APP_CLEANUP
                        ? "POST /applications/{id}/oauth-keys/{mappingId}/clean-up"
                        : "DELETE /applications/{id}/oauth-keys/{mappingId}",
                extras, organization, log, null);
    }

    /**
     * Common pipeline for all OAuth-lifecycle hooks: snapshot lookup → KM resolution →
     * capability gate → KM-scoped Spectral evaluation. The {@code capabilityGate} lambda
     * is action-specific and returns a non-null error message when the request must block.
     */
    private static void runOAuthLifecycleValidation(Application application, String keyManagerName,
                                                    KeyManagerGovernanceContext.Action action,
                                                    String operation, Map<String, Object> extras,
                                                    String organization, Log log,
                                                    java.util.function.Function<KeyManagerGovernanceContext, String>
                                                            capabilityGate) throws APIManagementException {

        try {
            DevportalGovernanceApplicationSnapshot snapshot = getOrCaptureSnapshot(application, organization, log);
            String resolvedKm = StringUtils.defaultIfBlank(keyManagerName,
                    APIConstants.KeyManager.DEFAULT_KEY_MANAGER);
            String resolvedOrg = resolveOrganization(application, organization);
            KeyManagerGovernanceContext kmContext = KeyManagerContextResolver.resolve(resolvedKm, resolvedOrg);
            if (kmContext != null && capabilityGate != null) {
                String reason = capabilityGate.apply(kmContext);
                if (reason != null) {
                    RestApiUtil.handleBadRequest(reason, log);
                    return;
                }
            }
            List<DevportalGovernanceRulesetSnapshot> oauthRulesets =
                    filterRulesetsByRuleType(snapshot.getRulesetSnapshots(), "APP_OAUTH");
            List<DevportalGovernanceRulesetSnapshot> keyManagerRulesets =
                    filterRulesetsByKeyManager(oauthRulesets, resolvedKm);

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("operation", operation);
            payload.put("action", action.name());
            payload.put("organization", resolvedOrg);
            payload.put("applicationId", application.getUUID());
            payload.put("keyManager", resolvedKm);
            if (extras != null) {
                payload.putAll(extras);
            }
            payload.put("keyManagerContext", kmContext == null ? null : kmContext.toPayloadMap());
            validatePayload(payload, keyManagerRulesets, application.getUUID(), resolvedOrg, log);
        } catch (APIMGovernanceException | JsonProcessingException e) {
            RestApiUtil.handleInternalServerError("Error while validating Devportal Governance " + action.name(),
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
            return;
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

    /**
     * Restrict the ruleset list to those whose ruleType matches the section being validated.
     * Without this, an APP_INFO rule with {@code given: "$"} and {@code then.field:
     * application.description} would fire on a gen-keys payload (which has no
     * {@code application.description}), producing spurious 400s. Each enforcement point
     * pre-filters so only rules authored for that section run.
     */
    private static List<DevportalGovernanceRulesetSnapshot> filterRulesetsByRuleType(
            List<DevportalGovernanceRulesetSnapshot> rulesets, String expectedRuleType) {

        if (rulesets == null || rulesets.isEmpty()) {
            return new ArrayList<>();
        }
        List<DevportalGovernanceRulesetSnapshot> matched = new ArrayList<>();
        for (DevportalGovernanceRulesetSnapshot rs : rulesets) {
            if (StringUtils.equalsIgnoreCase(rs.getRulesetType(), expectedRuleType)) {
                matched.add(rs);
            }
        }
        return matched;
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
                    StringUtils.defaultIfBlank(violation.getRuleMessage(), violation.getRuleName()),
                    getViolationDescription(violation)));
        }
        RestApiUtil.handleBadRequest(errorHandlers, log);
    }

    private static String getViolationDescription(RuleViolation violation) {

        StringBuilder description = new StringBuilder();
        appendViolationPart(description, "rule", violation.getRuleName());
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

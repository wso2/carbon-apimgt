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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.governance.generic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.governance.api.ValidationEngine;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;
import org.wso2.carbon.apimgt.governance.api.model.Rule;
import org.wso2.carbon.apimgt.governance.api.model.RuleSeverity;
import org.wso2.carbon.apimgt.governance.api.model.RuleViolation;
import org.wso2.carbon.apimgt.governance.api.model.Ruleset;
import org.wso2.carbon.apimgt.governance.api.model.RulesetContent;
import org.wso2.carbon.apimgt.governance.generic.model.ConflictReport;
import org.wso2.carbon.apimgt.governance.generic.model.DeduplicationResult;
import org.wso2.carbon.apimgt.governance.generic.service.DeduplicationConfigService;
import org.wso2.carbon.apimgt.governance.generic.service.GenericService;
import org.wso2.carbon.apimgt.governance.generic.service.SemanticSimilarityClient;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Generic Validation Engine for API deduplication.
 * <p>
 * This class provides validation logic for DEDUPLICATION rulesets and is used
 * internally by the Generic module. It is NOT registered as an OSGi service
 * because the SpectralValidationEngine already handles all ruleset types including
 * deduplication rulesets during startup.
 * <p>
 * This engine is used at runtime for API similarity detection using MinHash and LSH
 * algorithms to prevent API sprawl.
 * <p>
 * Configuration is done through the Governance Ruleset YAML with the following properties:
 * - similarity_threshold: Minimum Jaccard similarity for duplicate detection (default: 0.95)
 * - enabled: Whether deduplication is enabled (default: true)
 */
public class GenericValidationEngine implements ValidationEngine {

    private static final Log log = LogFactory.getLog(GenericValidationEngine.class);
    private static final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    private static final ObjectMapper jsonMapper = new ObjectMapper();

    private static final String DEDUPLICATION_RULE_NAME = "api-deduplication-check";
    private static final String DEDUPLICATION_RULE_DESCRIPTION =
            "Checks for duplicate APIs using MinHash and LSH similarity detection";

    // Lifecycle-specific rule name and descriptions (separate from dedup strings)
    private static final String LIFECYCLE_RULE_NAME = "api-deprecation-successor-check";
    private static final String LIFECYCLE_RULE_DESCRIPTION =
            "Checks for a valid successor when deprecating or retiring an API";
    private static final String LIFECYCLE_VIOLATION_REASON =
            "API deprecated without an identified successor.";
    private static final String LIFECYCLE_VIOLATION_MESSAGE =
            "This API was transitioned to a Deprecated/Retired state without a linked successor. "
            + "This creates a migration risk for consumers.";

    // Metadata prefix used by ComplianceEvaluationScheduler to pass context
    private static final String METADATA_PREFIX = "###GATEKEEPER_CONTEXT:";
    private static final String METADATA_SUFFIX = "###";

    /**
     * Validates the content of a deduplication ruleset.
     * Ensures the YAML configuration is valid and contains required fields.
     *
     * @param ruleset The ruleset to validate
     * @throws APIMGovernanceException If validation fails
     */
    @Override
    public void validateRulesetContent(Ruleset ruleset) throws APIMGovernanceException {
        // Lifecycle-only rulesets have their own YAML format — skip dedup-specific validation.
        // Combined rulesets (both dedup + lifecycle) still need dedup validation.
        if (isLifecycleRuleset(ruleset) && !isCombinedRuleset(ruleset)) {
            if (log.isDebugEnabled()) {
                log.debug("Validated lifecycle ruleset content: " + ruleset.getName());
            }
            return;
        }

        // Only process GENERIC/deduplication category rulesets (or combined rulesets)
        if (!isDeduplicationRuleset(ruleset) && !isCombinedRuleset(ruleset)) {
            if (log.isDebugEnabled()) {
                log.debug("Skipping non-deduplication ruleset: " + ruleset.getName());
            }
            return;
        }

        RulesetContent content = ruleset.getRulesetContent();
        if (content == null || content.getContent() == null) {
            throw new APIMGovernanceException("Ruleset content cannot be null for deduplication ruleset");
        }

        String contentString = new String(content.getContent(), StandardCharsets.UTF_8);

        try {
            Map<String, Object> config = yamlMapper.readValue(contentString, Map.class);

            // Support both root-level and nested 'deduplication:' section
            Map<String, Object> dedupConfig = config;
            if (config.containsKey("deduplication") && config.get("deduplication") instanceof Map) {
                dedupConfig = (Map<String, Object>) config.get("deduplication");
            }

            // Validate similarity_threshold if present
            validateThreshold(dedupConfig, GenericConstants.RulesetConfig.SIMILARITY_THRESHOLD);

            // Validate high_confidence_threshold if present
            if (dedupConfig.containsKey("high_confidence_threshold")) {
                Object val = dedupConfig.get("high_confidence_threshold");
                double hct = parseDouble(val);
                if (hct < 0.0 || hct > 1.0) {
                    throw new APIMGovernanceException(
                            "high_confidence_threshold must be between 0.0 and 1.0, got " + hct);
                }
            }

            // Validate mode if present
            if (dedupConfig.containsKey(GenericConstants.RulesetConfig.MODE)) {
                String mode = String.valueOf(dedupConfig.get(GenericConstants.RulesetConfig.MODE));
                if (!"audit".equalsIgnoreCase(mode) && !"warn".equalsIgnoreCase(mode)
                        && !"block".equalsIgnoreCase(mode)) {
                    throw new APIMGovernanceException(
                            "mode must be 'audit', 'warn', or 'block', got '" + mode + "'");
                }
            }

            if (log.isDebugEnabled()) {
                log.debug("Validated deduplication ruleset content: " + ruleset.getName());
            }

            // Invalidate config cache so new threshold/settings take effect immediately
            // This is called during create/update flows, so the new config will be
            // picked up on the next dedup check without waiting for cache expiry
            DeduplicationConfigService.getInstance().invalidateAllCaches();
            if (log.isDebugEnabled()) {
                log.debug("Invalidated deduplication config cache after ruleset validation");
            }

        } catch (APIMGovernanceException e) {
            throw e;
        } catch (Exception e) {
            throw new APIMGovernanceException("Failed to parse deduplication ruleset content: " + e.getMessage(), e);
        }
    }

    /**
     * Validates the threshold value within the config map.
     */
    private void validateThreshold(Map<String, Object> config, String key) throws APIMGovernanceException {
        if (config.containsKey(key)) {
            Object thresholdObj = config.get(key);
            double threshold = parseDouble(thresholdObj);

            if (threshold < GenericConstants.MIN_SIMILARITY_THRESHOLD ||
                    threshold > GenericConstants.MAX_SIMILARITY_THRESHOLD) {
                throw new APIMGovernanceException(
                        String.format("similarity_threshold must be between %.2f and %.2f, got %.2f",
                                GenericConstants.MIN_SIMILARITY_THRESHOLD,
                                GenericConstants.MAX_SIMILARITY_THRESHOLD,
                                threshold));
            }
        }
    }

    /**
     * Extracts rules from a deduplication ruleset.
     * For deduplication, there is typically one implicit rule for similarity checking.
     *
     * @param ruleset The ruleset
     * @return List of rules
     * @throws APIMGovernanceException If extraction fails
     */
    @Override
    public List<Rule> extractRulesFromRuleset(Ruleset ruleset) throws APIMGovernanceException {
        List<Rule> rules = new ArrayList<>();
        boolean combined = isCombinedRuleset(ruleset);

        // Handle lifecycle rules (lifecycle-only or combined rulesets)
        if (isLifecycleRuleset(ruleset)) {
            Rule lifecycleRule = new Rule();
            lifecycleRule.setId(java.util.UUID.randomUUID().toString());
            lifecycleRule.setName(LIFECYCLE_RULE_NAME);
            lifecycleRule.setDescription(LIFECYCLE_RULE_DESCRIPTION);
            lifecycleRule.setSeverity(RuleSeverity.ERROR);
            // Content is required by GOV_RULESET_RULE.RULE_CONTENT (NOT NULL blob).
            // Use the ruleset's YAML content so the rule row is properly persisted.
            RulesetContent rc = ruleset.getRulesetContent();
            if (rc != null && rc.getContent() != null) {
                lifecycleRule.setContent(new String(rc.getContent(), StandardCharsets.UTF_8));
            } else {
                lifecycleRule.setContent(LIFECYCLE_RULE_NAME);
            }
            rules.add(lifecycleRule);
            if (!combined) {
                return rules;
            }
        }

        // Handle dedup rules (dedup-only or combined rulesets)
        if (!isDeduplicationRuleset(ruleset) && !combined) {
            return rules;
        }

        RulesetContent content = ruleset.getRulesetContent();
        String contentString = new String(content.getContent(), StandardCharsets.UTF_8);

        try {
            Map<String, Object> config = yamlMapper.readValue(contentString, Map.class);

            // Support both root-level and nested 'deduplication:' section
            Map<String, Object> dedupConfig = config;
            if (config.containsKey("deduplication") && config.get("deduplication") instanceof Map) {
                dedupConfig = (Map<String, Object>) config.get("deduplication");
            }

            // Get threshold for the rule description
            double threshold = GenericConstants.DEFAULT_SIMILARITY_THRESHOLD;
            if (dedupConfig.containsKey(GenericConstants.RulesetConfig.SIMILARITY_THRESHOLD)) {
                threshold = parseDouble(dedupConfig.get(GenericConstants.RulesetConfig.SIMILARITY_THRESHOLD));
            }

            // Create the main deduplication rule
            Rule deduplicationRule = new Rule();
            deduplicationRule.setId(java.util.UUID.randomUUID().toString());
            deduplicationRule.setName(DEDUPLICATION_RULE_NAME);
            deduplicationRule.setDescription(String.format(
                    "%s (threshold: %.0f%%)", DEDUPLICATION_RULE_DESCRIPTION, threshold * 100));
            deduplicationRule.setSeverity(RuleSeverity.ERROR);
            deduplicationRule.setContent(contentString);

            rules.add(deduplicationRule);

            // Check for custom rules in 'rules' section (can be at root or inside deduplication)
            Map<String, Object> rulesMap = null;
            if (config.containsKey("rules") && config.get("rules") instanceof Map) {
                rulesMap = (Map<String, Object>) config.get("rules");
            } else if (dedupConfig.containsKey("rules") && dedupConfig.get("rules") instanceof Map) {
                rulesMap = (Map<String, Object>) dedupConfig.get("rules");
            }

            if (rulesMap != null) {
                boolean mainRuleUpdated = false;
                for (Map.Entry<String, Object> entry : rulesMap.entrySet()) {
                    // Update main dedup rule from the first rule entry in YAML
                    if (!mainRuleUpdated) {
                        if (entry.getValue() instanceof Map) {
                            Map<String, Object> ruleConfig = (Map<String, Object>) entry.getValue();
                            if (ruleConfig.containsKey("description")) {
                                deduplicationRule.setDescription((String) ruleConfig.get("description"));
                            }
                            if (ruleConfig.containsKey("severity")) {
                                deduplicationRule.setSeverity(
                                        RuleSeverity.fromString((String) ruleConfig.get("severity")));
                            }
                        }
                        mainRuleUpdated = true;
                        continue;
                    }

                    Rule customRule = new Rule();
                    customRule.setId(java.util.UUID.randomUUID().toString());
                    customRule.setName(entry.getKey());

                    if (entry.getValue() instanceof Map) {
                        Map<String, Object> ruleConfig = (Map<String, Object>) entry.getValue();
                        customRule.setDescription((String) ruleConfig.getOrDefault("description", ""));
                        String severityStr = (String) ruleConfig.getOrDefault("severity", "warn");
                        customRule.setSeverity(RuleSeverity.fromString(severityStr));
                        customRule.setContent(yamlMapper.writeValueAsString(ruleConfig));
                    }

                    rules.add(customRule);
                }
            }

        } catch (JsonProcessingException | RuntimeException e) {
            throw new APIMGovernanceException("Failed to extract rules from deduplication ruleset", e);
        }

        return rules;
    }

    /**
     * Validates an API definition against a deduplication ruleset.
     * Checks if the API is a duplicate of any existing API in the catalog.
     *
     * @param target  The API definition to validate
     * @param ruleset The deduplication ruleset
     * @return List of rule violations (duplicates found)
     * @throws APIMGovernanceException If validation fails
     */
    @Override
    public List<RuleViolation> validate(String target, Ruleset ruleset) throws APIMGovernanceException {
        List<RuleViolation> violations = new ArrayList<>();

        if (log.isDebugEnabled()) {
            log.debug("validate() called for ruleset: '" + ruleset.getName()
                    + "' (category=" + ruleset.getRuleCategory()
                    + ", id=" + ruleset.getId() + ")");
        }

        // ── Combined ruleset detection ──
        boolean combined = isCombinedRuleset(ruleset);

        // ── Lifecycle ruleset handling (deprecation/retirement successor check) ──
        boolean lifecycle = isLifecycleRuleset(ruleset);
        if (log.isDebugEnabled()) {
            log.debug("isLifecycleRuleset('" + ruleset.getName() + "') = " + lifecycle
                    + ", isCombined = " + combined);
        }
        if (lifecycle) {
            if (log.isDebugEnabled()) {
                log.debug("Routing to validateLifecycleRuleset for: " + ruleset.getName());
            }
            violations.addAll(validateLifecycleRuleset(target, ruleset));
            if (!combined) {
                return violations;
            }
        }

        // ── Deduplication ruleset handling ──
        if (!isDeduplicationRuleset(ruleset) && !combined) {
            if (log.isDebugEnabled()) {
                log.debug("Skipping validation for non-deduplication ruleset: " + ruleset.getName());
            }
            return violations;
        }

        // Check if enabled
        if (!isDeduplicationEnabled(ruleset)) {
            if (log.isDebugEnabled()) {
                log.debug("Deduplication is disabled for ruleset: " + ruleset.getName());
            }
            return violations;
        }

        // Read mode from config - determines enforcement behavior and severity
        // All modes (audit/warn/block) produce violations so they appear in the compliance dashboard.
        // The mode only affects severity and blocking behavior:
        //   audit → WARN severity, non-blocking (informational)
        //   warn  → WARN severity, non-blocking (alerts user)
        //   block → ERROR severity, blocks deployment at API_DEPLOY state
        String mode = getMode(ruleset);
        if (log.isDebugEnabled()) {
            log.debug("Deduplication mode for ruleset '" + ruleset.getName() + "': " + mode);
        }

        // Read custom message from YAML rules section
        String customMessage = getCustomRuleMessage(ruleset);

        // Read custom severity from YAML rules section
        RuleSeverity configuredSeverity = getConfiguredSeverity(ruleset);

        // Get threshold from ruleset config
        double threshold = getThreshold(ruleset);

        // Get GenericService instance
        GenericService genericService = GenericService.getInstance();

        if (!genericService.isInitialized()) {
            log.warn("GenericService not initialized. Skipping deduplication check.");
            return violations;
        }

        try {
            // Parse metadata context if present (set by ComplianceEvaluationScheduler)
            String apiDefinition = target;
            String apiUuid = null;
            String organization = null;

            if (target != null && target.startsWith(METADATA_PREFIX)) {
                int suffixStart = target.indexOf(METADATA_SUFFIX, METADATA_PREFIX.length());
                if (suffixStart > 0) {
                    String metadataJson = target.substring(METADATA_PREFIX.length(), suffixStart);
                    try {
                        Map<String, String> metadata = jsonMapper.readValue(metadataJson, Map.class);
                        apiUuid = metadata.get("apiUuid");
                        organization = metadata.get("organization");
                        log.debug("Parsed metadata context: apiUuid=" + apiUuid
                                + ", organization=" + organization);
                    } catch (JsonProcessingException | RuntimeException e) {
                        log.debug("Could not parse metadata from target", e);
                    }
                    // Strip metadata prefix from the API definition
                    int contentStart = suffixStart + METADATA_SUFFIX.length();
                    if (contentStart < target.length()) {
                        apiDefinition = target.substring(contentStart).trim();
                    }
                }
            }

            // Fallback to placeholder extraction if metadata not present
            if (apiUuid == null) {
                apiUuid = extractApiUuid(apiDefinition);
            }
            if (organization == null) {
                organization = extractOrganization(apiDefinition);
            }

            // Perform deduplication check
            DeduplicationResult result = genericService.checkForDuplicates(
                    apiDefinition, apiUuid, organization, threshold);

            if (result.isDuplicate()) {
                // Convert conflict reports to rule violations
                for (ConflictReport conflict : result.getConflictReports()) {
                    RuleViolation violation = new RuleViolation();
                    violation.setRulesetId(ruleset.getId());
                    violation.setRuleName(DEDUPLICATION_RULE_NAME);

                    // Set violatedPath to the system-generated details (similarity, matched API info)
                    String systemDetails = buildSystemDetails(conflict);
                    violation.setViolatedPath(systemDetails);

                    // Determine severity based on mode:
                    // - mode=block → ERROR severity (ensures policy BLOCK actions match)
                    // - mode=warn → use configured severity from YAML, or WARN as default
                    if ("block".equalsIgnoreCase(mode)) {
                        violation.setSeverity(RuleSeverity.ERROR);
                    } else {
                        // mode=warn: use YAML-configured severity or WARN default
                        violation.setSeverity(configuredSeverity != null
                                ? configuredSeverity : RuleSeverity.WARN);
                    }

                    // Set the message: use custom YAML message if available, else system message
                    if (customMessage != null && !customMessage.isEmpty()) {
                        violation.setRuleMessage(customMessage);
                    } else {
                        violation.setRuleMessage(buildViolationMessage(conflict));
                    }

                    violations.add(violation);
                }
            }

            // ── Phase 2: Semantic Similarity Check (fail-open) ──────────────────────
            // If the ruleset has 'semantic_check.enabled: true', perform an AI-plane
            // vector similarity search using the Marketplace Assistant infrastructure.
            try {
                if (isSemanticCheckEnabled(ruleset)) {
                    double semanticThreshold = getSemanticThreshold(ruleset);
                    int semanticLimit = getSemanticLimit(ruleset);
                    SemanticSimilarityClient semanticClient = new SemanticSimilarityClient();
                    SemanticSimilarityClient.SemanticMatchResult semanticResult =
                            semanticClient.findSemanticNeighbors(
                                    apiDefinition, organization, semanticLimit, semanticThreshold);

                    if (semanticResult != null && semanticResult.isMatchesFound()) {
                        for (SemanticSimilarityClient.SemanticCandidate candidate
                                : semanticResult.getTopCandidates()) {
                            RuleViolation violation = new RuleViolation();
                            violation.setRulesetId(ruleset.getId());
                            violation.setRuleName(DEDUPLICATION_RULE_NAME);
                            violation.setViolatedPath(String.format(
                                    "Semantic match: API '%s' (uuid=%s) — similarity=%.4f",
                                    candidate.getApiName(), candidate.getApiUuid(),
                                    candidate.getSimilarityScore()));
                            if ("block".equalsIgnoreCase(mode)) {
                                violation.setSeverity(RuleSeverity.ERROR);
                            } else {
                                violation.setSeverity(configuredSeverity != null
                                        ? configuredSeverity : RuleSeverity.WARN);
                            }
                            violation.setRuleMessage(String.format(
                                    "This API is semantically similar (%.0f%%) to an existing API '%s'. "
                                    + "Review whether this API duplicates existing functionality.",
                                    candidate.getSimilarityScore() * 100, candidate.getApiName()));
                            violations.add(violation);
                        }
                        log.info("Semantic similarity check found " + semanticResult.getCandidateCount()
                                + " match(es) for API in org=" + organization);
                    }
                }
            } catch (RuntimeException e) {
                // Fail-open: semantic check failure must never block the overall dedup result
                log.warn("Semantic similarity check failed — proceeding without it: " + e.getMessage());
            }

        } catch (APIMGovernanceException e) {
            log.error("Error during deduplication validation", e);
            throw e;
        } catch (RuntimeException e) {
            log.error("Error during deduplication validation", e);
            throw new APIMGovernanceException("Deduplication validation failed: " + e.getMessage(), e);
        }

        return violations;
    }

    /**
     * Validates an API against a lifecycle/retirement ruleset.
     * <p>
     * Checks whether the API has been deprecated/retired without a valid successor.
     * If no successor is found, produces a violation that persists in the GOV_RULE_VIOLATION table
     * and appears in the Compliance/Adherence Summary.
     * <p>
     * Enforcement modes:
     *   BLOCK → ERROR severity (transition blocked by ApisApiServiceImpl at lifecycle time)
     *   WARN  → WARN severity, non-blocking (violation recorded for compliance visibility)
     *   AUDIT → WARN severity, non-blocking (violation recorded for compliance visibility)
     *
     * @param target  API definition or metadata string
     * @param ruleset The lifecycle ruleset
     * @return List of violations (empty if API has a successor or is not deprecated/retired)
     */
    private List<RuleViolation> validateLifecycleRuleset(String target, Ruleset ruleset) {
        List<RuleViolation> violations = new ArrayList<>();

        GenericService genericService = GenericService.getInstance();
        if (!genericService.isInitialized()) {
            log.warn("GenericService not initialized. Skipping lifecycle ruleset check.");
            return violations;
        }

        // ── Policy Attachment Guard ──────────────────────────────────────
        // Skip lifecycle evaluation if the ruleset is not attached to any
        // active governance policy.  This prevents stale compliance results
        // when the admin toggles the lifecycle ruleset off in the policy UI.
        // For combined rulesets, the scheduler already ensures policy attachment
        // via getRulesetsWithContentByPolicyId(), so skip the name-based SQL check
        // (which wouldn't match combined ruleset names like "Strict Combined").
        if (!isCombinedRuleset(ruleset)) {
            try {
                if (!genericService.isLifecycleRulesetInActivePolicy(null)) {
                    if (log.isDebugEnabled()) {
                        log.debug("Lifecycle ruleset '" + ruleset.getName()
                                + "' is not attached to any active policy. "
                                + "Skipping compliance evaluation.");
                    }
                    return violations;
                }
            } catch (Exception ex) {
                log.warn("Could not verify lifecycle policy attachment for '"
                        + ruleset.getName() + "': " + ex.getMessage()
                        + ". Proceeding with evaluation (fail-open).");
            }
        }

        String mode = getLifecycleMode(ruleset);
        if (log.isDebugEnabled()) {
            log.debug("Lifecycle enforcement mode for ruleset '" + ruleset.getName() + "': " + mode);
        }

        try {
            // Parse metadata context if present
            String apiUuid = null;
            String organization = null;

            if (target != null && target.startsWith(METADATA_PREFIX)) {
                int suffixStart = target.indexOf(METADATA_SUFFIX, METADATA_PREFIX.length());
                if (suffixStart > 0) {
                    String metadataJson = target.substring(METADATA_PREFIX.length(), suffixStart);
                    try {
                        Map<String, String> metadata = jsonMapper.readValue(metadataJson, Map.class);
                        apiUuid = metadata.get("apiUuid");
                        organization = metadata.get("organization");
                    } catch (JsonProcessingException | RuntimeException e) {
                        log.debug("Could not parse metadata from target for lifecycle check", e);
                    }
                }
            }

            if (apiUuid == null) {
                apiUuid = extractApiUuid(target);
            }
            if (organization == null) {
                organization = extractOrganization(target);
            }

            // Get API status via DAO (works from scheduler threads, unlike APIProvider)
            if (log.isDebugEnabled()) {
                log.debug("Lifecycle check: looking up API status for " + apiUuid
                        + " in org=" + organization);
            }
            String status = null;
            try {
                status = org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO.getInstance()
                        .getAPIStatusFromAPIUUID(apiUuid);
            } catch (Throwable ex) {
                log.warn("Could not get API status via ApiMgtDAO for " + apiUuid
                        + ": " + ex.getClass().getName() + " - " + ex.getMessage());
            }

            if (status == null) {
                // Fallback: try via APIProvider
                try {
                    String adminUsername = genericService.getAdminUsername(organization);
                    org.wso2.carbon.apimgt.api.APIProvider apiProvider =
                            org.wso2.carbon.apimgt.impl.APIManagerFactory.getInstance()
                                    .getAPIProvider(adminUsername);
                    if (apiProvider != null) {
                        org.wso2.carbon.apimgt.api.model.API api =
                                apiProvider.getAPIbyUUID(apiUuid, organization);
                        if (api != null) {
                            status = api.getStatus();
                        }
                    }
                } catch (Throwable ex) {
                    log.warn("Fallback APIProvider also failed for " + apiUuid
                            + ": " + ex.getClass().getName() + " - " + ex.getMessage());
                }
            }

            if (status == null) {
                log.debug("Could not determine API status for " + apiUuid
                        + ". Skipping lifecycle check.");
                return violations;
            }

            if (log.isDebugEnabled()) {
                log.debug("Lifecycle check: API " + apiUuid + " status=" + status);
            }
            boolean isDeprecatedOrRetired = "DEPRECATED".equalsIgnoreCase(status)
                    || "RETIRED".equalsIgnoreCase(status);

            if (!isDeprecatedOrRetired) {
                // API is not deprecated/retired — no lifecycle violation
                if (log.isDebugEnabled()) {
                    log.debug("API " + apiUuid + " is " + status
                            + " — not deprecated/retired, no lifecycle violation.");
                }
                return violations;
            }

            // Check if a successor exists (via API properties or successor mapping table)
            boolean hasSuccessor = false;
            String successorUuid = null;
            try {
                String adminUsername = genericService.getAdminUsername(organization);
                org.wso2.carbon.apimgt.api.APIProvider apiProvider =
                        org.wso2.carbon.apimgt.impl.APIManagerFactory.getInstance()
                                .getAPIProvider(adminUsername);
                if (apiProvider != null) {
                    org.wso2.carbon.apimgt.api.model.API api =
                            apiProvider.getAPIbyUUID(apiUuid, organization);
                    if (api != null) {
                        successorUuid = api.getProperty("X-Deprecation-Successor-UUID");
                        if (successorUuid != null && !successorUuid.isEmpty()) {
                            hasSuccessor = true;
                        }
                    }
                }
            } catch (Throwable ex) {
                log.debug("Could not check successor property for " + apiUuid
                        + ": " + ex.getMessage());
                // If we can't check, assume no successor — safer to flag the violation
            }

            if (!hasSuccessor) {
                // No successor found — produce a violation
                RuleViolation violation = new RuleViolation();
                violation.setRulesetId(ruleset.getId());
                violation.setRuleName(LIFECYCLE_RULE_NAME);
                violation.setViolatedPath(LIFECYCLE_VIOLATION_REASON);

                // Determine severity based on mode
                if ("block".equalsIgnoreCase(mode)) {
                    violation.setSeverity(RuleSeverity.ERROR);
                } else {
                    violation.setSeverity(RuleSeverity.WARN);
                }

                violation.setRuleMessage(LIFECYCLE_VIOLATION_MESSAGE);
                violations.add(violation);

                if (log.isDebugEnabled()) {
                    log.debug("Lifecycle violation: API " + apiUuid + " (" + status
                            + ") has no successor. Mode: " + mode);
                }
            } else {
                log.debug("API " + apiUuid + " has successor " + successorUuid
                        + ". No lifecycle violation.");
            }

        } catch (RuntimeException e) {
            log.error("Error during lifecycle ruleset validation for "
                    + ruleset.getName() + ": " + e.getMessage(), e);
        }

        return violations;
    }

    /**
     * Checks if the ruleset is a deduplication ruleset (GENERIC but NOT lifecycle).
     *
     * @param ruleset The ruleset to check
     * @return True if it's a deduplication ruleset (not a lifecycle ruleset)
     */
    private boolean isDeduplicationRuleset(Ruleset ruleset) {
        // First check if it's GENERIC category
        boolean isGeneric = false;
        if (ruleset.getRuleCategory() != null) {
            isGeneric = GenericConstants.GENERIC_RULE_CATEGORY.equalsIgnoreCase(
                    ruleset.getRuleCategory().name());
        }

        if (!isGeneric) {
            // Also check the content for deduplication configuration
            try {
                RulesetContent content = ruleset.getRulesetContent();
                if (content != null && content.getContent() != null) {
                    String contentString = new String(content.getContent(), StandardCharsets.UTF_8);
                    Map<String, Object> config = yamlMapper.readValue(contentString, Map.class);
                    if (config.containsKey("type")
                            && GenericConstants.GENERIC_RULE_CATEGORY.equalsIgnoreCase(
                                    (String) config.get("type"))) {
                        isGeneric = true;
                    }
                }
            } catch (JsonProcessingException | RuntimeException e) {
                log.debug("Could not parse ruleset content for type detection", e);
            }
        }

        // Exclude lifecycle rulesets from dedup processing
        return isGeneric && !isLifecycleRuleset(ruleset);
    }

    /**
     * Checks if the ruleset is a lifecycle/retirement ruleset.
     * These handle deprecation/retirement successor enforcement, not deduplication.
     *
     * @param ruleset The ruleset to check
     * @return True if it's a lifecycle ruleset
     */
    private boolean isLifecycleRuleset(Ruleset ruleset) {
        String name = ruleset.getName() != null ? ruleset.getName().toLowerCase(Locale.ENGLISH) : "";
        if (name.contains("lifecycle") || name.contains("retirement") || name.contains("deprecation")) {
            return true;
        }
        // Also check content for lifecycle_retirement section or compliance_exclusion flag
        try {
            RulesetContent content = ruleset.getRulesetContent();
            if (content != null && content.getContent() != null) {
                String contentString = new String(content.getContent(), StandardCharsets.UTF_8);
                if (contentString.contains("lifecycle_retirement:")) {
                    return true;
                }
            }
        } catch (RuntimeException e) {
            log.debug("Could not parse ruleset content for lifecycle detection", e);
        }
        return false;
    }

    /**
     * Checks if the ruleset is a combined ruleset containing BOTH deduplication
     * AND lifecycle_retirement sections in its YAML content.
     *
     * @param ruleset The ruleset to check
     * @return True if it contains both deduplication and lifecycle sections
     */
    private boolean isCombinedRuleset(Ruleset ruleset) {
        try {
            RulesetContent content = ruleset.getRulesetContent();
            if (content != null && content.getContent() != null) {
                String contentString = new String(content.getContent(), StandardCharsets.UTF_8);
                return contentString.contains("deduplication:")
                        && contentString.contains("lifecycle_retirement:");
            }
        } catch (RuntimeException e) {
            log.debug("Could not check for combined ruleset", e);
        }
        return false;
    }

    /**
     * Checks if deduplication is enabled in the ruleset.
     */
    private boolean isDeduplicationEnabled(Ruleset ruleset) {
        try {
            RulesetContent content = ruleset.getRulesetContent();
            if (content != null && content.getContent() != null) {
                String contentString = new String(content.getContent(), StandardCharsets.UTF_8);
                Map<String, Object> config = yamlMapper.readValue(contentString, Map.class);
                if (config.containsKey(GenericConstants.RulesetConfig.ENABLED)) {
                    return Boolean.TRUE.equals(config.get(GenericConstants.RulesetConfig.ENABLED));
                }
            }
        } catch (JsonProcessingException | RuntimeException e) {
            log.debug("Could not parse ruleset content for enabled check", e);
        }
        return true; // Default to enabled
    }

    /**
     * Gets the similarity threshold from the ruleset.
     */
    private double getThreshold(Ruleset ruleset) {
        try {
            RulesetContent content = ruleset.getRulesetContent();
            if (content != null && content.getContent() != null) {
                String contentString = new String(content.getContent(), StandardCharsets.UTF_8);
                Map<String, Object> config = yamlMapper.readValue(contentString, Map.class);
                if (config.containsKey(GenericConstants.RulesetConfig.SIMILARITY_THRESHOLD)) {
                    return parseDouble(config.get(GenericConstants.RulesetConfig.SIMILARITY_THRESHOLD));
                }
            }
        } catch (JsonProcessingException | RuntimeException e) {
            log.debug("Could not parse threshold from ruleset", e);
        }
        return GenericConstants.DEFAULT_SIMILARITY_THRESHOLD;
    }

    /**
     * Parses a double value from an object.
     */
    private double parseDouble(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        } else if (value instanceof String) {
            return Double.parseDouble((String) value);
        }
        return GenericConstants.DEFAULT_SIMILARITY_THRESHOLD;
    }

    /**
     * Extracts API UUID from the target (placeholder for actual implementation).
     */
    private String extractApiUuid(String target) {
        // In practice, this would be passed through context or extracted from metadata
        // For now, generate a temporary UUID for the check
        return java.util.UUID.randomUUID().toString();
    }

    /**
     * Extracts organization from the target (placeholder for actual implementation).
     */
    private String extractOrganization(String target) {
        // In practice, this would be passed through context
        return "carbon.super";
    }

    /**
     * Builds a detailed violation message from a conflict report.
     */
    private String buildViolationMessage(ConflictReport conflict) {
        StringBuilder sb = new StringBuilder();
        sb.append("Potential duplicate API detected. ");
        sb.append(String.format("Similarity: %.1f%%. ", conflict.getSimilarityScore() * 100));

        if (conflict.getMatchedApiName() != null) {
            sb.append("Matched API: ").append(conflict.getMatchedApiName());
            if (conflict.getMatchedApiVersion() != null) {
                sb.append(" v").append(conflict.getMatchedApiVersion());
            }
            sb.append(". ");
        }

        if (conflict.getMatchedApiUuid() != null) {
            sb.append("UUID: ").append(conflict.getMatchedApiUuid()).append(". ");
        }

        if (conflict.getRecommendation() != null) {
            sb.append(conflict.getRecommendation());
        }

        return sb.toString();
    }

    /**
     * Builds system detail string for the violatedPath field.
     * Contains similarity score, matched API name/version, and UUID for frontend parsing.
     */
    private String buildSystemDetails(ConflictReport conflict) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Similarity: %.1f%%", conflict.getSimilarityScore() * 100));

        if (conflict.getMatchedApiName() != null) {
            sb.append(" | Matched API: ").append(conflict.getMatchedApiName());
            if (conflict.getMatchedApiVersion() != null) {
                sb.append(" v").append(conflict.getMatchedApiVersion());
            }
        }

        if (conflict.getMatchedApiUuid() != null) {
            sb.append(" | API_UUID:").append(conflict.getMatchedApiUuid());
        }

        if (conflict.getMatchedApiProvider() != null) {
            sb.append(" | API_CREATOR:").append(conflict.getMatchedApiProvider());
        }

        return sb.toString();
    }

    /**
     * Gets the mode (audit/warn/block) from the ruleset YAML configuration.
     */
    private String getMode(Ruleset ruleset) {
        try {
            RulesetContent content = ruleset.getRulesetContent();
            if (content != null && content.getContent() != null) {
                String contentString = new String(content.getContent(), StandardCharsets.UTF_8);
                Map<String, Object> config = yamlMapper.readValue(contentString, Map.class);

                // Check root level
                if (config.containsKey(GenericConstants.RulesetConfig.MODE)) {
                    return String.valueOf(config.get(GenericConstants.RulesetConfig.MODE));
                }

                // Check nested deduplication section
                if (config.containsKey("deduplication") && config.get("deduplication") instanceof Map) {
                    Map<String, Object> dedupConfig = (Map<String, Object>) config.get("deduplication");
                    if (dedupConfig.containsKey(GenericConstants.RulesetConfig.MODE)) {
                        return String.valueOf(dedupConfig.get(GenericConstants.RulesetConfig.MODE));
                    }
                }
            }
        } catch (JsonProcessingException | RuntimeException e) {
            log.debug("Could not parse mode from ruleset", e);
        }
        return "audit"; // Default to audit mode
    }

    /**
     * Gets the mode (audit/warn/block) from a lifecycle ruleset YAML configuration.
     * Looks inside 'lifecycle_retirement:' section or at root level.
     */
    @SuppressWarnings("unchecked")
    private String getLifecycleMode(Ruleset ruleset) {
        try {
            RulesetContent content = ruleset.getRulesetContent();
            if (content != null && content.getContent() != null) {
                String contentString = new String(content.getContent(), StandardCharsets.UTF_8);
                Map<String, Object> config = yamlMapper.readValue(contentString, Map.class);

                // Check nested lifecycle_retirement section first
                if (config.containsKey("lifecycle_retirement")
                        && config.get("lifecycle_retirement") instanceof Map) {
                    Map<String, Object> lcConfig = (Map<String, Object>) config.get("lifecycle_retirement");
                    if (lcConfig.containsKey(GenericConstants.RulesetConfig.MODE)) {
                        return String.valueOf(lcConfig.get(GenericConstants.RulesetConfig.MODE));
                    }
                }

                // Also support corrupted format where it's under 'deduplication:' section
                if (config.containsKey("deduplication") && config.get("deduplication") instanceof Map) {
                    Map<String, Object> dedupConfig = (Map<String, Object>) config.get("deduplication");
                    if (dedupConfig.containsKey(GenericConstants.RulesetConfig.MODE)) {
                        return String.valueOf(dedupConfig.get(GenericConstants.RulesetConfig.MODE));
                    }
                }

                // Check root level
                if (config.containsKey(GenericConstants.RulesetConfig.MODE)) {
                    return String.valueOf(config.get(GenericConstants.RulesetConfig.MODE));
                }
            }
        } catch (JsonProcessingException | RuntimeException e) {
            log.debug("Could not parse mode from lifecycle ruleset", e);
        }
        return "warn"; // Default to warn for lifecycle rulesets
    }

    /**
     * Gets the custom violation message from the ruleset YAML rules section.
     * Looks for rules.api-deduplication-check.message in the YAML.
     */
    @SuppressWarnings("unchecked")
    private String getCustomRuleMessage(Ruleset ruleset) {
        try {
            RulesetContent content = ruleset.getRulesetContent();
            if (content != null && content.getContent() != null) {
                String contentString = new String(content.getContent(), StandardCharsets.UTF_8);
                Map<String, Object> config = yamlMapper.readValue(contentString, Map.class);

                // Check for rules section at root or inside deduplication
                Map<String, Object> rulesMap = null;
                if (config.containsKey("rules") && config.get("rules") instanceof Map) {
                    rulesMap = (Map<String, Object>) config.get("rules");
                } else if (config.containsKey("deduplication") && config.get("deduplication") instanceof Map) {
                    Map<String, Object> dedupConfig = (Map<String, Object>) config.get("deduplication");
                    if (dedupConfig.containsKey("rules") && dedupConfig.get("rules") instanceof Map) {
                        rulesMap = (Map<String, Object>) dedupConfig.get("rules");
                    }
                }

                if (rulesMap != null) {
                    // Try specific rule name first, then fallback to first rule entry
                    Object ruleObj = rulesMap.get(DEDUPLICATION_RULE_NAME);
                    if (ruleObj == null && !rulesMap.isEmpty()) {
                        ruleObj = rulesMap.values().iterator().next();
                    }
                    if (ruleObj instanceof Map) {
                        Map<String, Object> ruleConfig = (Map<String, Object>) ruleObj;
                        if (ruleConfig.containsKey("message")) {
                            return String.valueOf(ruleConfig.get("message"));
                        }
                    }
                }
            }
        } catch (JsonProcessingException | RuntimeException e) {
            log.debug("Could not parse custom rule message from ruleset", e);
        }
        return null;
    }

    /**
     * Gets the configured severity from the ruleset YAML rules section.
     * Looks for rules.api-deduplication-check.severity in the YAML.
     */
    @SuppressWarnings("unchecked")
    private RuleSeverity getConfiguredSeverity(Ruleset ruleset) {
        try {
            RulesetContent content = ruleset.getRulesetContent();
            if (content != null && content.getContent() != null) {
                String contentString = new String(content.getContent(), StandardCharsets.UTF_8);
                Map<String, Object> config = yamlMapper.readValue(contentString, Map.class);

                // Check for rules section at root or inside deduplication
                Map<String, Object> rulesMap = null;
                if (config.containsKey("rules") && config.get("rules") instanceof Map) {
                    rulesMap = (Map<String, Object>) config.get("rules");
                } else if (config.containsKey("deduplication") && config.get("deduplication") instanceof Map) {
                    Map<String, Object> dedupConfig = (Map<String, Object>) config.get("deduplication");
                    if (dedupConfig.containsKey("rules") && dedupConfig.get("rules") instanceof Map) {
                        rulesMap = (Map<String, Object>) dedupConfig.get("rules");
                    }
                }

                if (rulesMap != null) {
                    // Try specific rule name first, then fallback to first rule entry
                    Object ruleObj = rulesMap.get(DEDUPLICATION_RULE_NAME);
                    if (ruleObj == null && !rulesMap.isEmpty()) {
                        ruleObj = rulesMap.values().iterator().next();
                    }
                    if (ruleObj instanceof Map) {
                        Map<String, Object> ruleConfig = (Map<String, Object>) ruleObj;
                        if (ruleConfig.containsKey("severity")) {
                            return RuleSeverity.fromString(
                                    String.valueOf(ruleConfig.get("severity")));
                        }
                    }
                }
            }
        } catch (JsonProcessingException | RuntimeException e) {
            log.debug("Could not parse configured severity from ruleset", e);
        }
        return null;
    }

    /**
     * Returns {@code true} if the ruleset YAML has {@code semantic_check.enabled: true}.
     * Defaults to {@code false} so the Phase 2 check is opt-in.
     */
    @SuppressWarnings("unchecked")
    private boolean isSemanticCheckEnabled(Ruleset ruleset) {
        try {
            RulesetContent content = ruleset.getRulesetContent();
            if (content == null || content.getContent() == null) {
                return false;
            }
            String contentString = new String(content.getContent(), StandardCharsets.UTF_8);
            Map<String, Object> config = yamlMapper.readValue(contentString, Map.class);
            if (config.containsKey("semantic_check") && config.get("semantic_check") instanceof Map) {
                Map<String, Object> sc = (Map<String, Object>) config.get("semantic_check");
                Object enabled = sc.get("enabled");
                if (enabled instanceof Boolean) {
                    return (Boolean) enabled;
                }
                if (enabled instanceof String) {
                    return Boolean.parseBoolean((String) enabled);
                }
            }
        } catch (JsonProcessingException | RuntimeException e) {
            log.debug("Could not read semantic_check.enabled from ruleset", e);
        }
        return false;
    }

    /**
     * Returns the {@code semantic_check.threshold} value from the ruleset YAML,
     * or {@code 0.85} as the default.
     */
    @SuppressWarnings("unchecked")
    private double getSemanticThreshold(Ruleset ruleset) {
        try {
            RulesetContent content = ruleset.getRulesetContent();
            if (content == null || content.getContent() == null) {
                return 0.85;
            }
            String contentString = new String(content.getContent(), StandardCharsets.UTF_8);
            Map<String, Object> config = yamlMapper.readValue(contentString, Map.class);
            if (config.containsKey("semantic_check") && config.get("semantic_check") instanceof Map) {
                Map<String, Object> sc = (Map<String, Object>) config.get("semantic_check");
                if (sc.containsKey("threshold")) {
                    return parseDouble(sc.get("threshold"));
                }
            }
        } catch (JsonProcessingException | RuntimeException e) {
            log.debug("Could not read semantic_check.threshold from ruleset", e);
        }
        return 0.85;
    }

    /**
     * Returns the {@code semantic_check.limit} value from the ruleset YAML,
     * or {@code 3} as the default.
     */
    @SuppressWarnings("unchecked")
    private int getSemanticLimit(Ruleset ruleset) {
        try {
            RulesetContent content = ruleset.getRulesetContent();
            if (content == null || content.getContent() == null) {
                return 3;
            }
            String contentString = new String(content.getContent(), StandardCharsets.UTF_8);
            Map<String, Object> config = yamlMapper.readValue(contentString, Map.class);
            if (config.containsKey("semantic_check") && config.get("semantic_check") instanceof Map) {
                Map<String, Object> sc = (Map<String, Object>) config.get("semantic_check");
                if (sc.containsKey("limit")) {
                    Object val = sc.get("limit");
                    if (val instanceof Integer) {
                        return (Integer) val;
                    }
                    return Integer.parseInt(String.valueOf(val));
                }
            }
        } catch (JsonProcessingException | RuntimeException e) {
            log.debug("Could not read semantic_check.limit from ruleset", e);
        }
        return 3;
    }
}

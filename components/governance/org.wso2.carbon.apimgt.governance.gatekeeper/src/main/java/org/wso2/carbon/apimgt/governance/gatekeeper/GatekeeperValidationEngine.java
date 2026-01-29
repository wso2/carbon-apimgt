/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.apimgt.governance.gatekeeper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.apimgt.governance.api.ValidationEngine;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;
import org.wso2.carbon.apimgt.governance.api.model.Rule;
import org.wso2.carbon.apimgt.governance.api.model.RuleSeverity;
import org.wso2.carbon.apimgt.governance.api.model.RuleViolation;
import org.wso2.carbon.apimgt.governance.api.model.Ruleset;
import org.wso2.carbon.apimgt.governance.api.model.RulesetContent;
import org.wso2.carbon.apimgt.governance.gatekeeper.model.ConflictReport;
import org.wso2.carbon.apimgt.governance.gatekeeper.model.DeduplicationResult;
import org.wso2.carbon.apimgt.governance.gatekeeper.service.GatekeeperService;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Gatekeeper Validation Engine for API deduplication.
 * <p>
 * This validation engine is registered as an OSGi service and processes rulesets
 * with the DEDUPLICATION category. It uses MinHash and LSH algorithms to detect
 * structurally similar APIs and prevent API sprawl.
 * <p>
 * Configuration is done through the Governance Ruleset YAML with the following properties:
 * - similarity_threshold: Minimum Jaccard similarity for duplicate detection (default: 0.95)
 * - enabled: Whether deduplication is enabled (default: true)
 */
@Component(
        name = "org.wso2.carbon.apimgt.governance.gatekeeper.GatekeeperValidationEngine",
        immediate = true,
        service = ValidationEngine.class
)
public class GatekeeperValidationEngine implements ValidationEngine {

    private static final Log log = LogFactory.getLog(GatekeeperValidationEngine.class);
    private static final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    private static final ObjectMapper jsonMapper = new ObjectMapper();

    private static final String DEDUPLICATION_RULE_NAME = "api-deduplication-check";
    private static final String DEDUPLICATION_RULE_DESCRIPTION =
            "Checks for duplicate APIs using MinHash and LSH similarity detection";

    /**
     * Validates the content of a deduplication ruleset.
     * Ensures the YAML configuration is valid and contains required fields.
     *
     * @param ruleset The ruleset to validate
     * @throws APIMGovernanceException If validation fails
     */
    @Override
    public void validateRulesetContent(Ruleset ruleset) throws APIMGovernanceException {
        // Only process DEDUPLICATION category rulesets
        if (!isDeduplicationRuleset(ruleset)) {
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

            // Validate similarity_threshold if present
            if (config.containsKey(GatekeeperConstants.RulesetConfig.SIMILARITY_THRESHOLD)) {
                Object thresholdObj = config.get(GatekeeperConstants.RulesetConfig.SIMILARITY_THRESHOLD);
                double threshold = parseDouble(thresholdObj);

                if (threshold < GatekeeperConstants.MIN_SIMILARITY_THRESHOLD ||
                        threshold > GatekeeperConstants.MAX_SIMILARITY_THRESHOLD) {
                    throw new APIMGovernanceException(
                            String.format("similarity_threshold must be between %.2f and %.2f, got %.2f",
                                    GatekeeperConstants.MIN_SIMILARITY_THRESHOLD,
                                    GatekeeperConstants.MAX_SIMILARITY_THRESHOLD,
                                    threshold));
                }
            }

            log.info("Validated deduplication ruleset: " + ruleset.getName());

        } catch (APIMGovernanceException e) {
            throw e;
        } catch (Exception e) {
            throw new APIMGovernanceException("Failed to parse deduplication ruleset content: " + e.getMessage(), e);
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
        // Only process DEDUPLICATION category rulesets
        if (!isDeduplicationRuleset(ruleset)) {
            return Collections.emptyList();
        }

        List<Rule> rules = new ArrayList<>();

        RulesetContent content = ruleset.getRulesetContent();
        String contentString = new String(content.getContent(), StandardCharsets.UTF_8);

        try {
            Map<String, Object> config = yamlMapper.readValue(contentString, Map.class);

            // Get threshold for the rule description
            double threshold = GatekeeperConstants.DEFAULT_SIMILARITY_THRESHOLD;
            if (config.containsKey(GatekeeperConstants.RulesetConfig.SIMILARITY_THRESHOLD)) {
                threshold = parseDouble(config.get(GatekeeperConstants.RulesetConfig.SIMILARITY_THRESHOLD));
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

            // If there are custom rules defined in the config, add those too
            if (config.containsKey("rules") && config.get("rules") instanceof Map) {
                Map<String, Object> customRules = (Map<String, Object>) config.get("rules");
                for (Map.Entry<String, Object> entry : customRules.entrySet()) {
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

        } catch (Exception e) {
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

        // Only process DEDUPLICATION category rulesets
        if (!isDeduplicationRuleset(ruleset)) {
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

        // Get threshold from ruleset config
        double threshold = getThreshold(ruleset);

        // Get GatekeeperService instance
        GatekeeperService gatekeeperService = GatekeeperService.getInstance();

        if (!gatekeeperService.isInitialized()) {
            log.warn("GatekeeperService not initialized. Skipping deduplication check.");
            return violations;
        }

        try {
            // Extract API UUID and organization from the target
            // The target format is expected to contain metadata or we use a placeholder
            // In practice, this would be passed through context
            String apiUuid = extractApiUuid(target);
            String organization = extractOrganization(target);

            // Perform deduplication check
            DeduplicationResult result = gatekeeperService.checkForDuplicates(
                    target, apiUuid, organization, threshold);

            if (result.isDuplicate()) {
                // Convert conflict reports to rule violations
                for (ConflictReport conflict : result.getConflictReports()) {
                    RuleViolation violation = new RuleViolation();
                    violation.setRulesetId(ruleset.getId());
                    violation.setRuleName(DEDUPLICATION_RULE_NAME);
                    violation.setViolatedPath("/");

                    // Set severity based on confidence
                    if (result.isHighConfidence()) {
                        violation.setSeverity(RuleSeverity.ERROR);
                    } else {
                        violation.setSeverity(RuleSeverity.WARN);
                    }

                    // Build detailed message
                    String message = buildViolationMessage(conflict);
                    violation.setRuleMessage(message);

                    violations.add(violation);
                }
            }

        } catch (Exception e) {
            log.error("Error during deduplication validation", e);
            throw new APIMGovernanceException("Deduplication validation failed: " + e.getMessage(), e);
        }

        return violations;
    }

    /**
     * Checks if the ruleset is a deduplication ruleset.
     *
     * @param ruleset The ruleset to check
     * @return True if it's a deduplication ruleset
     */
    private boolean isDeduplicationRuleset(Ruleset ruleset) {
        if (ruleset.getRuleCategory() != null) {
            return GatekeeperConstants.DEDUPLICATION_RULE_CATEGORY.equalsIgnoreCase(
                    ruleset.getRuleCategory().name());
        }

        // Also check the content for deduplication configuration
        try {
            RulesetContent content = ruleset.getRulesetContent();
            if (content != null && content.getContent() != null) {
                String contentString = new String(content.getContent(), StandardCharsets.UTF_8);
                Map<String, Object> config = yamlMapper.readValue(contentString, Map.class);
                if (config.containsKey("type") &&
                        GatekeeperConstants.DEDUPLICATION_RULE_CATEGORY.equalsIgnoreCase(
                                (String) config.get("type"))) {
                    return true;
                }
            }
        } catch (Exception e) {
            log.debug("Could not parse ruleset content for type detection", e);
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
                if (config.containsKey(GatekeeperConstants.RulesetConfig.ENABLED)) {
                    return Boolean.TRUE.equals(config.get(GatekeeperConstants.RulesetConfig.ENABLED));
                }
            }
        } catch (Exception e) {
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
                if (config.containsKey(GatekeeperConstants.RulesetConfig.SIMILARITY_THRESHOLD)) {
                    return parseDouble(config.get(GatekeeperConstants.RulesetConfig.SIMILARITY_THRESHOLD));
                }
            }
        } catch (Exception e) {
            log.debug("Could not parse threshold from ruleset", e);
        }
        return GatekeeperConstants.DEFAULT_SIMILARITY_THRESHOLD;
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
        return GatekeeperConstants.DEFAULT_SIMILARITY_THRESHOLD;
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
}

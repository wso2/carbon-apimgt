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

package org.wso2.carbon.apimgt.governance.engine;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.apimgt.governance.api.ValidationEngine;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovExceptionCodes;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;
import org.wso2.carbon.apimgt.governance.api.model.Rule;
import org.wso2.carbon.apimgt.governance.api.model.RuleCategory;
import org.wso2.carbon.apimgt.governance.api.model.RuleSeverity;
import org.wso2.carbon.apimgt.governance.api.model.RuleViolation;
import org.wso2.carbon.apimgt.governance.api.model.Ruleset;
import org.wso2.carbon.apimgt.governance.api.model.RulesetContent;
import org.wso2.carbon.apimgt.governance.impl.util.APIMGovernanceUtil;
import org.wso2.rule.validator.InvalidContentTypeException;
import org.wso2.rule.validator.InvalidRulesetException;
import org.wso2.rule.validator.validator.Validator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * This class represents a Validation Engine. This can be extended to implement a specific validation engine like
 * spectral
 */
@Component(
        name = "org.wso2.carbon.apimgt.governance.engine.SpectralValidationEngine",
        immediate = true,
        service = ValidationEngine.class
)
public class SpectralValidationEngine implements ValidationEngine {
    private static final Log log = LogFactory.getLog(SpectralValidationEngine.class);
    
    // Deduplication ruleset detection constants
    private static final String DEDUPLICATION_KEY = "deduplication";
    private static final String SIMILARITY_THRESHOLD_KEY = "similarity_threshold";

    /**
     * Check if a ruleset is valid
     *
     * @param ruleset Ruleset
     * @throws APIMGovernanceException If an error occurs while validating the ruleset
     */
    @Override
    public void validateRulesetContent(Ruleset ruleset) throws APIMGovernanceException {
        
        log.info("SpectralValidationEngine.validateRulesetContent called for: " + ruleset.getName());
        
        // Check if this is a deduplication ruleset - delegate to Gatekeeper validation
        if (isDeduplicationRuleset(ruleset)) {
            log.info("Deduplication ruleset detected, using custom validation for: " + ruleset.getName());
            validateDeduplicationRulesetContent(ruleset);
            return;
        }
        
        log.info("Using standard Spectral validation for: " + ruleset.getName());
        
        RulesetContent content = ruleset.getRulesetContent();
        String rulesetContentString = new String(content.getContent(),
                StandardCharsets.UTF_8);
        String jsonString;
        try {
            jsonString = Validator.validateRuleset(rulesetContentString);
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonString);
            boolean passed = rootNode.path("passed").asBoolean();
            if (passed) {
                return;
            }
            String message = rootNode.path("message").asText();
            throw new APIMGovernanceException(APIMGovExceptionCodes.INVALID_RULESET_CONTENT_DETAILED,
                    ruleset.getName(), message);
        } catch (InvalidContentTypeException | JsonProcessingException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.INVALID_RULESET_CONTENT, e, ruleset.getName());
        } catch (Throwable e) {
            throw new APIMGovernanceException("Unexpected error while validating ruleset content.", e);
        }
    }

    /**
     * Extract rules from a ruleset
     *
     * @param ruleset Ruleset
     * @return List of rules
     * @throws APIMGovernanceException If an error occurs while extracting rules
     */
    @Override
    public List<Rule> extractRulesFromRuleset(Ruleset ruleset) throws APIMGovernanceException {
        
        log.info("SpectralValidationEngine.extractRulesFromRuleset called for: " + ruleset.getName());
        
        // Check if this is a deduplication ruleset - use custom extraction
        if (isDeduplicationRuleset(ruleset)) {
            log.info("Deduplication ruleset detected, using custom extraction for: " + ruleset.getName());
            return extractDeduplicationRules(ruleset);
        }
        
        log.info("Using standard rule extraction for: " + ruleset.getName());
        
        String ruleContentString = new String(ruleset.getRulesetContent().getContent(),
                StandardCharsets.UTF_8);

        Map<String, Object> contentMap = APIMGovernanceUtil.getMapFromYAMLStringContent(ruleContentString);
        List<Rule> rulesList = new ArrayList<>();

        // Check if 'rules' is present and not null
        if (contentMap.containsKey("rules") && contentMap.get("rules") instanceof Map) {
            // Extract rules
            Map<String, Map<String, Object>> rules =
                    (Map<String, Map<String, Object>>) contentMap.get("rules");
            for (Map.Entry<String, Map<String, Object>> entry : rules.entrySet()) {

                Map<String, Object> ruleDetails = entry.getValue();

                String name = entry.getKey();
                if (name != null && name.length() > 256) {
                    throw new APIMGovernanceException(APIMGovExceptionCodes.BAD_REQUEST,
                            "Rule name `" + name + "` exceeds the maximum allowed length of 256 characters.");
                }

                String description = (String) ruleDetails.get("description");
                if (description != null && description.length() > 1024) {
                    log.warn("Rule description of rule `" + name + "` exceeds 1024 characters." +
                            " Truncating description.");
                    description = description.substring(0, 1024);
                }

                String severityString = (String) ruleDetails.get("severity");
                RuleSeverity severity = severityString == null ? RuleSeverity.WARN :
                        RuleSeverity.fromString(severityString);

                ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());

                Rule rule = new Rule();
                rule.setId(APIMGovernanceUtil.generateUUID());
                rule.setName(name);
                rule.setDescription(description);
                rule.setSeverity(severity);
                try {
                    String contentString = objectMapper.writerWithDefaultPrettyPrinter()
                            .writeValueAsString(ruleDetails);
                    rule.setContent(contentString);
                    rulesList.add(rule);
                } catch (JsonProcessingException e) {
                    throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_EXTRACTING_RULE_CONTENT, e);
                }

            }
        }

        return rulesList;
    }

    /**
     * Validate a target against a ruleset
     *
     * @param target  Target to be validated
     * @param ruleset Ruleset
     * @return List of rule violations
     * @throws APIMGovernanceException If an error occurs while validating the target
     */
    @Override
    public List<RuleViolation> validate(String target, Ruleset ruleset) throws APIMGovernanceException {

        // DEDUPLICATION rulesets are handled by the Gatekeeper module, not by Spectral.
        // Return empty list as deduplication checks are done during API creation via event listeners.
        if (isDeduplicationRuleset(ruleset)) {
            log.info("Skipping Spectral validation for DEDUPLICATION ruleset: " + ruleset.getName() +
                    ". Deduplication is handled by the Gatekeeper module.");
            return Collections.emptyList();
        }

        try {
            RulesetContent rulesetContent = ruleset.getRulesetContent();
            String rulesetContentString = new String(rulesetContent.getContent(),
                    StandardCharsets.UTF_8);

            String resultJson = Validator.validateDocument(target, rulesetContentString);
            if (log.isDebugEnabled()) {
                log.debug("Validation success for target: " + target);
            }
            return getRuleViolationsFromJsonResponse(resultJson, ruleset);
        } catch (InvalidRulesetException | InvalidContentTypeException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.INVALID_RULESET_CONTENT, ruleset.getName());
        } catch (Throwable e) {
            log.error("Error occurred while verifying governance compliance ", e);
            throw new APIMGovernanceException("Unexpected error occurred while verifying governance compliance ", e);
        }
    }


    /**
     * Get Rule Violations from a JSON response
     *
     * @param resultJson JSON response
     * @param ruleset    Ruleset
     * @return List of Rule Violations
     * @throws APIMGovernanceException If an error occurs while getting the rule violations
     */
    private List<RuleViolation> getRuleViolationsFromJsonResponse(String resultJson, Ruleset ruleset)
            throws APIMGovernanceException {
        ObjectMapper objectMapper = new ObjectMapper();
        List<RuleViolation> violations = new ArrayList<>();
        JsonNode jsonNode;

        // Parse JSON string to JsonNode
        try {
            jsonNode = objectMapper.readTree(resultJson);
            // Convert JsonNode to list of Result objects
            for (JsonNode node : jsonNode) {
                RuleViolation violation = new RuleViolation();
                violation.setRuleName(node.get("ruleName").asText());
                String path = node.get("path").asText();
                if (path != null && path.length() > 1024) {
                    throw new APIMGovernanceException("Violated path `" + path + "` in rule `"
                            + violation.getRuleName() +
                            "` exceeds the maximum allowed length of 1024 characters.");
                }
                violation.setViolatedPath(path);
                String message = node.get("message").asText();
                if (message != null && message.length() > 1024) {
                    log.warn("Rule message of rule `" + violation.getRuleName() + "` exceeds 1024 characters. " +
                            "Truncating message.");
                    message = message.substring(0, 1024);
                }
                violation.setRuleMessage(message);
                violation.setSeverity(RuleSeverity.fromString(node.get("severity").asText()));
                violation.setRulesetId(ruleset.getId());
                violations.add(violation);
            }
            return violations;
        } catch (JsonProcessingException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_READING_SPECTRAL_RESULTS, e);
        }

    }

    // ========================================================================================
    // DEDUPLICATION RULESET HANDLING
    // ========================================================================================

    /**
     * Checks if a ruleset is a deduplication ruleset based on its category, name, or content.
     * A deduplication ruleset has RuleCategory.DEDUPLICATION or contains a 'deduplication' section.
     *
     * @param ruleset The ruleset to check
     * @return true if this is a deduplication ruleset
     */
    private boolean isDeduplicationRuleset(Ruleset ruleset) {
        if (ruleset == null) {
            return false;
        }

        // FIRST check RuleCategory (most reliable check)
        if (ruleset.getRuleCategory() == RuleCategory.DEDUPLICATION) {
            log.info("Detected deduplication ruleset by RuleCategory: " + ruleset.getName());
            return true;
        }

        // SECOND check if ruleset name contains deduplication (fast check)
        if (ruleset.getName() != null && 
                (ruleset.getName().toLowerCase(Locale.ENGLISH).contains("deduplication") ||
                 ruleset.getName().toLowerCase(Locale.ENGLISH).contains("duplicate"))) {
            log.info("Detected deduplication ruleset by name: " + ruleset.getName());
            return true;
        }

        // THIRD check ruleset content
        if (ruleset.getRulesetContent() == null || ruleset.getRulesetContent().getContent() == null) {
            return false;
        }

        try {
            String contentString = new String(ruleset.getRulesetContent().getContent(), StandardCharsets.UTF_8);
            ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
            Map<String, Object> contentMap = yamlMapper.readValue(contentString, Map.class);

            // Check for 'deduplication' section
            if (contentMap.containsKey(DEDUPLICATION_KEY)) {
                log.info("Detected deduplication ruleset by 'deduplication' key in content");
                return true;
            }

            // Also check for similarity_threshold at root level
            if (contentMap.containsKey(SIMILARITY_THRESHOLD_KEY)) {
                log.info("Detected deduplication ruleset by 'similarity_threshold' key in content");
                return true;
            }

            return false;
        } catch (IOException e) {
            log.debug("Error parsing ruleset content: " + e.getMessage());
            return false;
        }
    }

    /**
     * Validates a deduplication ruleset content.
     * Checks for valid configuration values.
     *
     * @param ruleset The deduplication ruleset to validate
     * @throws APIMGovernanceException If validation fails
     */
    private void validateDeduplicationRulesetContent(Ruleset ruleset) throws APIMGovernanceException {
        RulesetContent content = ruleset.getRulesetContent();
        if (content == null || content.getContent() == null) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.INVALID_RULESET_CONTENT, ruleset.getName());
        }

        String contentString = new String(content.getContent(), StandardCharsets.UTF_8);

        try {
            ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
            Map<String, Object> config = yamlMapper.readValue(contentString, Map.class);

            // Extract deduplication config (could be at root or nested)
            Map<String, Object> dedupConfig = config;
            if (config.containsKey(DEDUPLICATION_KEY) && config.get(DEDUPLICATION_KEY) instanceof Map) {
                dedupConfig = (Map<String, Object>) config.get(DEDUPLICATION_KEY);
            }

            // Validate similarity_threshold if present
            Double threshold = extractThreshold(dedupConfig);
            if (threshold != null) {
                if (threshold < 0.5 || threshold > 1.0) {
                    throw new APIMGovernanceException(APIMGovExceptionCodes.INVALID_RULESET_CONTENT_DETAILED,
                            ruleset.getName(),
                            "similarity_threshold must be between 0.5 and 1.0, got " + threshold);
                }
            }

            // Check enabled flag if present
            if (dedupConfig.containsKey("enabled")) {
                Object enabledObj = dedupConfig.get("enabled");
                if (!(enabledObj instanceof Boolean)) {
                    throw new APIMGovernanceException(APIMGovExceptionCodes.INVALID_RULESET_CONTENT_DETAILED,
                            ruleset.getName(),
                            "'enabled' must be a boolean value (true/false)");
                }
            }

            log.info("Successfully validated deduplication ruleset: " + ruleset.getName());

        } catch (APIMGovernanceException e) {
            throw e;
        } catch (Exception e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.INVALID_RULESET_CONTENT_DETAILED,
                    ruleset.getName(), "Failed to parse YAML content: " + e.getMessage());
        }
    }

    /**
     * Extracts rules from a deduplication ruleset.
     *
     * @param ruleset The deduplication ruleset
     * @return List of rules
     * @throws APIMGovernanceException If extraction fails
     */
    private List<Rule> extractDeduplicationRules(Ruleset ruleset) throws APIMGovernanceException {
        List<Rule> rules = new ArrayList<>();

        try {
            String contentString = new String(ruleset.getRulesetContent().getContent(), StandardCharsets.UTF_8);
            ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
            Map<String, Object> config = yamlMapper.readValue(contentString, Map.class);

            // Extract deduplication config
            Map<String, Object> dedupConfig = config;
            if (config.containsKey(DEDUPLICATION_KEY) && config.get(DEDUPLICATION_KEY) instanceof Map) {
                dedupConfig = (Map<String, Object>) config.get(DEDUPLICATION_KEY);
            }

            // Get threshold for rule description
            Double threshold = extractThreshold(dedupConfig);
            if (threshold == null) {
                threshold = 0.95; // Default
            }

            // Create the main deduplication rule
            Rule deduplicationRule = new Rule();
            deduplicationRule.setId(APIMGovernanceUtil.generateUUID());
            deduplicationRule.setName("api-deduplication-check");
            deduplicationRule.setDescription(String.format(
                    "Detects duplicate APIs with similarity >= %.0f%% using MinHash/LSH algorithms",
                    threshold * 100));
            deduplicationRule.setSeverity(RuleSeverity.ERROR);

            // Store the full config as rule content for later use
            ObjectMapper jsonMapper = new ObjectMapper();
            String ruleContent = jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dedupConfig);
            deduplicationRule.setContent(ruleContent);

            rules.add(deduplicationRule);

            // Also extract any rules from 'rules' section if present (for compatibility)
            if (config.containsKey("rules") && config.get("rules") instanceof Map) {
                Map<String, Map<String, Object>> yamlRules = (Map<String, Map<String, Object>>) config.get("rules");
                for (Map.Entry<String, Map<String, Object>> entry : yamlRules.entrySet()) {
                    String ruleName = entry.getKey();
                    Map<String, Object> ruleDetails = entry.getValue();

                    // Skip if this is the api-deduplication-check rule we already added
                    if ("api-deduplication-check".equals(ruleName)) {
                        continue;
                    }

                    Rule rule = new Rule();
                    rule.setId(APIMGovernanceUtil.generateUUID());
                    rule.setName(ruleName);
                    rule.setDescription(ruleDetails.get("description") != null ? 
                            ruleDetails.get("description").toString() : "Deduplication rule");
                    
                    String severity = ruleDetails.get("severity") != null ? 
                            ruleDetails.get("severity").toString() : "error";
                    rule.setSeverity(RuleSeverity.fromString(severity));
                    
                    String content = jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(ruleDetails);
                    rule.setContent(content);
                    
                    rules.add(rule);
                }
            }

            log.info("Extracted " + rules.size() + " rule(s) from deduplication ruleset: " + ruleset.getName());
            return rules;

        } catch (IOException e) {
            log.error("Error extracting rules from deduplication ruleset: " + e.getMessage(), e);
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_EXTRACTING_RULE_CONTENT, e);
        }
    }

    /**
     * Extracts threshold value from config map.
     *
     * @param config The configuration map
     * @return The threshold value or null if not found
     */
    private Double extractThreshold(Map<String, Object> config) {
        Object thresholdObj = config.get(SIMILARITY_THRESHOLD_KEY);
        if (thresholdObj == null) {
            return null;
        }
        
        if (thresholdObj instanceof Number) {
            return ((Number) thresholdObj).doubleValue();
        } else if (thresholdObj instanceof String) {
            try {
                return Double.parseDouble((String) thresholdObj);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

}

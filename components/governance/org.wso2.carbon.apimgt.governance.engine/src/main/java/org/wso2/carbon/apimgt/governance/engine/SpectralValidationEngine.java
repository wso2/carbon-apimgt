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
import org.wso2.carbon.apimgt.governance.api.model.RuleSeverity;
import org.wso2.carbon.apimgt.governance.api.model.RuleViolation;
import org.wso2.carbon.apimgt.governance.api.model.Ruleset;
import org.wso2.carbon.apimgt.governance.api.model.RulesetContent;
import org.wso2.carbon.apimgt.governance.impl.util.APIMGovernanceUtil;
import org.wso2.rule.validator.InvalidContentTypeException;
import org.wso2.rule.validator.InvalidRulesetException;
import org.wso2.rule.validator.validator.Validator;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
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

    /**
     * Check if a ruleset is valid
     *
     * @param ruleset Ruleset
     * @throws APIMGovernanceException If an error occurs while validating the ruleset
     */
    @Override
    public void validateRulesetContent(Ruleset ruleset) throws APIMGovernanceException {
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
                String description = (String) ruleDetails.get("description");

                String severityString = (String) ruleDetails.get("severity");
                RuleSeverity severity = RuleSeverity.fromString(severityString);
                severity = severity == null ? RuleSeverity.WARN : severity;

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
                violation.setViolatedPath(node.get("path").asText());
                violation.setRuleMessage(node.get("message").asText());
                violation.setSeverity(RuleSeverity.fromString(node.get("severity").asText()));
                violation.setRulesetId(ruleset.getId());
                violations.add(violation);
            }
            return violations;
        } catch (JsonProcessingException e) {
            log.error("Error while parsing validation result JSON string", e);
            throw new APIMGovernanceException("Error while parsing validation result JSON string", e);
        }

    }


}

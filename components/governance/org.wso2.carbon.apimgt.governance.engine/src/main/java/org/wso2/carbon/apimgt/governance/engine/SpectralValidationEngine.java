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
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.apimgt.governance.api.ValidationEngine;
import org.wso2.carbon.apimgt.governance.api.error.GovernanceException;
import org.wso2.carbon.apimgt.governance.api.error.GovernanceExceptionCodes;
import org.wso2.carbon.apimgt.governance.api.model.Rule;
import org.wso2.carbon.apimgt.governance.api.model.RuleViolation;
import org.wso2.carbon.apimgt.governance.api.model.Ruleset;
import org.wso2.carbon.apimgt.governance.api.model.Severity;
import org.wso2.carbon.apimgt.governance.impl.util.GovernanceUtil;

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

    /**
     * Check if a ruleset is valid
     *
     * @param ruleset Ruleset
     * @return True if the ruleset is valid, False otherwise
     * @throws GovernanceException If an error occurs while validating the ruleset
     */
    @Override
    public boolean isRulesetValid(Ruleset ruleset) throws GovernanceException {
        // TODO: Call the Spectral engine to validate the ruleset
        // Validator.validateRuleset(ruleset.getRulesetContent());
        return true;
    }

    /**
     * Extract rules from a ruleset
     *
     * @param ruleset Ruleset
     * @return List of rules
     * @throws GovernanceException If an error occurs while extracting rules
     */
    @Override
    public List<Rule> extractRulesFromRuleset(Ruleset ruleset) throws GovernanceException {
        String ruleContentString = ruleset.getRulesetContent();
        Map<String, Object> contentMap = GovernanceUtil.getMapFromYAMLStringContent(ruleContentString);
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
                String messageOnValidationFailure = (String) ruleDetails.get("message");

                String severityString = (String) ruleDetails.get("severity");
                Severity severity = Severity.fromString(severityString);

                ObjectMapper objectMapper = new ObjectMapper();

                Rule rule = new Rule();
                rule.setId(GovernanceUtil.generateUUID());
                rule.setCode(name);
                rule.setDescription(description);
                rule.setSeverity(severity);
                rule.setMessageOnFailure(messageOnValidationFailure);
                try {
                    String contentString = objectMapper.writerWithDefaultPrettyPrinter()
                            .writeValueAsString(ruleDetails);
                    rule.setContent(contentString);
                    rulesList.add(rule);
                } catch (JsonProcessingException e) {
                    throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_EXTRACTING_RULE_CONTENT);
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
     * @throws GovernanceException If an error occurs while validating the target
     */
    @Override
    public List<RuleViolation> validate(String target, Ruleset ruleset) throws GovernanceException {

        ObjectMapper objectMapper = new ObjectMapper();
        List<RuleViolation> violations = new ArrayList<>();

        try {

//            String validationResultJsonString = Validator
//                    .validateDocument(target, ruleset.getRulesetContent());
            String validationResultJsonString = "";

            // Parse JSON string to JsonNode
            JsonNode jsonNode = objectMapper.readTree(validationResultJsonString);

            // Convert JsonNode to list of Result objects
            for (JsonNode node : jsonNode) {
                boolean isViolation = !node.get("passed").asBoolean();
                if (isViolation) {
                    RuleViolation violation = new RuleViolation();
                    violation.setRuleCode(node.get("ruleName").asText());
                    violation.setViolatedPath(node.get("path").asText());
                    violation.setRulesetId(ruleset.getId());
                    violations.add(violation);
                }
            }
            return violations;

        } catch (JsonProcessingException e) {
            throw new GovernanceException("Error while parsing validation result JSON string", e);
        }
    }


}

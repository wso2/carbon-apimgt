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

package org.wso2.carbon.apimgt.governance.external;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.governance.api.ValidationEngine;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovExceptionCodes;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;
import org.wso2.carbon.apimgt.governance.api.model.ExtendedArtifactType;
import org.wso2.carbon.apimgt.governance.api.model.Rule;
import org.wso2.carbon.apimgt.governance.api.model.RuleCategory;
import org.wso2.carbon.apimgt.governance.api.model.RuleSeverity;
import org.wso2.carbon.apimgt.governance.api.model.RuleType;
import org.wso2.carbon.apimgt.governance.api.model.RuleViolation;
import org.wso2.carbon.apimgt.governance.api.model.Ruleset;
import org.wso2.carbon.apimgt.governance.api.model.RulesetContent;
import org.wso2.carbon.apimgt.governance.impl.util.APIMGovernanceUtil;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Minimal validation engine for external governance rulesets.
 */
public class ExternalValidationEngine implements ValidationEngine {

    private static final Log log = LogFactory.getLog(ExternalValidationEngine.class);

    @Override
    public void validateRulesetContent(Ruleset ruleset) throws APIMGovernanceException {

        Map<String, Object> rulesetDefinition = parseExternalRuleset(ruleset);
        Map<String, Object> rules = getRules(rulesetDefinition);
        if (log.isDebugEnabled()) {
            log.debug("Accepted EXTERNAL ruleset content for ruleset: " + ruleset.getName()
                    + ". Extractable rules: " + rules.size());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Rule> extractRulesFromRuleset(Ruleset ruleset) throws APIMGovernanceException {

        Map<String, Object> rulesetDefinition = parseExternalRuleset(ruleset);
        applyRulesetMetadata(ruleset, rulesetDefinition);

        Map<String, Object> rules = getRules(rulesetDefinition);
        List<Rule> extractedRules = new ArrayList<>();
        ObjectMapper yamlWriter = new ObjectMapper(new YAMLFactory());

        for (Map.Entry<String, Object> ruleEntry : rules.entrySet()) {
            String ruleName = ruleEntry.getKey();
            if (ruleName != null && ruleName.length() > 256) {
                throw new APIMGovernanceException(APIMGovExceptionCodes.BAD_REQUEST,
                        "Rule name `" + ruleName + "` exceeds the maximum allowed length of 256 characters.");
            }

            Rule rule = new Rule();
            rule.setId(APIMGovernanceUtil.generateUUID());
            rule.setName(ruleName);

            if (ruleEntry.getValue() instanceof Map) {
                Map<String, Object> ruleDefinition = (Map<String, Object>) ruleEntry.getValue();
                String description = asString(ruleDefinition.get("description"));
                if (description != null && description.length() > 1024) {
                    log.warn("Rule description of external rule `" + ruleName + "` exceeds 1024 characters. "
                            + "Truncating description.");
                    description = description.substring(0, 1024);
                }
                rule.setDescription(description);

                RuleSeverity severity = RuleSeverity.fromString(asString(ruleDefinition.get("severity")));
                rule.setSeverity(severity != null ? severity : RuleSeverity.WARN);
            } else {
                rule.setSeverity(RuleSeverity.WARN);
            }

            try {
                rule.setContent(yamlWriter.writerWithDefaultPrettyPrinter().writeValueAsString(ruleEntry.getValue()));
            } catch (JsonProcessingException e) {
                throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_EXTRACTING_RULE_CONTENT, e);
            }
            extractedRules.add(rule);
        }

        if (log.isDebugEnabled()) {
            log.debug("Extracted " + extractedRules.size() + " rules from EXTERNAL ruleset: " + ruleset.getName());
        }
        return extractedRules;
    }

    @Override
    public List<RuleViolation> validate(String target, Ruleset ruleset) throws APIMGovernanceException {

        if (log.isDebugEnabled()) {
            log.debug("Skipping runtime validation for EXTERNAL ruleset: " + ruleset.getName()
                    + ". Target length: " + (target != null ? target.length() : 0));
        }
        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getRules(Map<String, Object> rulesetDefinition) {

        Object rulesetContent = rulesetDefinition.get("rulesetContent");
        if (rulesetContent instanceof Map) {
            Object rules = ((Map<String, Object>) rulesetContent).get("rules");
            if (rules instanceof Map) {
                return (Map<String, Object>) rules;
            }
        }

        Object rules = rulesetDefinition.get("rules");
        if (rules instanceof Map) {
            return (Map<String, Object>) rules;
        }
        return Collections.emptyMap();
    }

    private Map<String, Object> parseExternalRuleset(Ruleset ruleset) throws APIMGovernanceException {

        RulesetContent rulesetContent = ruleset.getRulesetContent();
        if (rulesetContent == null || rulesetContent.getContent().length == 0) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.INVALID_RULESET_CONTENT, ruleset.getName());
        }

        Map<String, Object> rulesetDefinition = APIMGovernanceUtil.getMapFromYAMLStringContent(
                new String(rulesetContent.getContent(), StandardCharsets.UTF_8));
        if (rulesetDefinition == null || rulesetDefinition.isEmpty()) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.INVALID_RULESET_CONTENT, ruleset.getName());
        }
        return rulesetDefinition;
    }

    private void applyRulesetMetadata(Ruleset ruleset, Map<String, Object> rulesetDefinition) {

        if (ruleset.getName() == null) {
            ruleset.setName(asString(rulesetDefinition.get("name")));
        }
        if (ruleset.getDescription() == null) {
            ruleset.setDescription(asString(rulesetDefinition.get("description")));
        }
        if (ruleset.getDocumentationLink() == null) {
            ruleset.setDocumentationLink(asString(rulesetDefinition.get("documentationLink")));
        }
        if (ruleset.getProvider() == null) {
            ruleset.setProvider(asString(rulesetDefinition.get("provider")));
        }
        if (ruleset.getRuleCategory() == null) {
            ruleset.setRuleCategory(RuleCategory.fromString(asString(rulesetDefinition.get("ruleCategory"))));
        }
        if (ruleset.getRuleType() == null) {
            ruleset.setRuleType(RuleType.fromString(asString(rulesetDefinition.get("ruleType"))));
        }
        if (ruleset.getArtifactType() == null) {
            ruleset.setArtifactType(ExtendedArtifactType.fromString(asString(rulesetDefinition.get("artifactType"))));
        }

        if (log.isDebugEnabled()) {
            log.debug("Applied EXTERNAL ruleset metadata for ruleset: " + ruleset.getName()
                    + ", category: " + ruleset.getRuleCategory()
                    + ", type: " + ruleset.getRuleType()
                    + ", artifactType: " + ruleset.getArtifactType());
        }
    }

    private String asString(Object value) {

        return value != null ? String.valueOf(value) : null;
    }
}

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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.governance.api.ValidationEngine;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovExceptionCodes;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;
import org.wso2.carbon.apimgt.governance.api.model.Rule;
import org.wso2.carbon.apimgt.governance.api.model.RuleSeverity;
import org.wso2.carbon.apimgt.governance.api.model.RuleViolation;
import org.wso2.carbon.apimgt.governance.api.model.Ruleset;
import org.wso2.carbon.apimgt.governance.external.model.ExternalEvaluationContext;
import org.wso2.carbon.apimgt.governance.external.model.ExternalPathMatch;
import org.wso2.carbon.apimgt.governance.external.model.ExternalRuleDefinition;
import org.wso2.carbon.apimgt.governance.external.model.ExternalRulesetDefinition;
import org.wso2.carbon.apimgt.governance.external.util.ExternalJsonPathUtils;
import org.wso2.carbon.apimgt.governance.external.util.ExternalRulesetUtils;
import org.wso2.carbon.apimgt.governance.external.util.ExternalServiceClient;
import org.wso2.carbon.apimgt.governance.external.util.ExternalTemplateUtils;
import org.wso2.carbon.apimgt.governance.impl.util.APIMGovernanceUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Validation engine for external governance rulesets.
 */
public class ExternalValidationEngine implements ValidationEngine {

    private static final Log log = LogFactory.getLog(ExternalValidationEngine.class);
    private static final ObjectMapper YAML_WRITER = new ObjectMapper(new YAMLFactory());

    private final ExternalServiceClient externalServiceClient;

    public ExternalValidationEngine() {

        this.externalServiceClient = new ExternalServiceClient();
    }

    @Override
    public void validateRulesetContent(Ruleset ruleset) throws APIMGovernanceException {

        ExternalRulesetDefinition rulesetDefinition = ExternalRulesetUtils.parseRuleset(ruleset);
        ExternalRulesetUtils.applyRulesetMetadata(ruleset, rulesetDefinition);
        ExternalRulesetUtils.validateRulesetDefinition(ruleset, rulesetDefinition);

        if (log.isDebugEnabled()) {
            log.debug("Validated EXTERNAL ruleset content for ruleset: " + ruleset.getName()
                    + ", artifactType=" + ruleset.getArtifactType()
                    + ", ruleType=" + ruleset.getRuleType()
                    + ", ruleCount=" + rulesetDefinition.getRules().size());
        }
    }

    @Override
    public List<Rule> extractRulesFromRuleset(Ruleset ruleset) throws APIMGovernanceException {

        ExternalRulesetDefinition rulesetDefinition = ExternalRulesetUtils.parseRuleset(ruleset);
        ExternalRulesetUtils.applyRulesetMetadata(ruleset, rulesetDefinition);
        ExternalRulesetUtils.validateRulesetDefinition(ruleset, rulesetDefinition);

        List<Rule> extractedRules = new ArrayList<>();
        for (Map.Entry<String, ExternalRuleDefinition> ruleEntry : rulesetDefinition.getRules().entrySet()) {
            String ruleName = ruleEntry.getKey();
            ExternalRuleDefinition ruleDefinition = ruleEntry.getValue();

            Rule rule = new Rule();
            rule.setId(APIMGovernanceUtil.generateUUID());
            rule.setName(ruleName);
            rule.setDescription(ExternalRulesetUtils.sanitizeRuleDescription(ruleName,
                    ruleDefinition != null ? ruleDefinition.getDescription() : null));
            rule.setSeverity(ExternalRulesetUtils.resolveSeverity(ruleDefinition != null
                    ? ruleDefinition.getSeverity() : null));

            try {
                rule.setContent(YAML_WRITER.writerWithDefaultPrettyPrinter().writeValueAsString(ruleDefinition));
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

        ExternalRulesetDefinition rulesetDefinition = ExternalRulesetUtils.parseRuleset(ruleset);
        ExternalRulesetUtils.applyRulesetMetadata(ruleset, rulesetDefinition);
        ExternalRulesetUtils.validateRulesetDefinition(ruleset, rulesetDefinition);

        JsonNode targetDocument = ExternalRulesetUtils.parseTargetDocument(target);
        List<RuleViolation> violations = new ArrayList<>();

        if (log.isDebugEnabled()) {
            log.debug("Validating EXTERNAL ruleset '" + ruleset.getName() + "' against artifact type "
                    + ruleset.getArtifactType() + " using " + rulesetDefinition.getRules().size() + " rules");
        }

        for (Map.Entry<String, ExternalRuleDefinition> ruleEntry : rulesetDefinition.getRules().entrySet()) {
            violations.addAll(validateRule(ruleEntry.getKey(), ruleEntry.getValue(), ruleset, targetDocument));
        }

        if (log.isDebugEnabled()) {
            log.debug("External validation completed for ruleset '" + ruleset.getName()
                    + "'. Total violations: " + violations.size());
        }
        return violations;
    }

    private List<RuleViolation> validateRule(String ruleName, ExternalRuleDefinition ruleDefinition, Ruleset ruleset,
                                             JsonNode targetDocument) throws APIMGovernanceException {

        List<RuleViolation> violations = new ArrayList<>();
        String targetPath = ExternalRulesetUtils.getTargetPath(ruleDefinition);
        List<ExternalPathMatch> targetMatches = ExternalJsonPathUtils.findMatches(targetDocument, targetPath);

        if (log.isDebugEnabled()) {
            log.debug("Rule '" + ruleName + "' selected " + targetMatches.size() + " target nodes using path "
                    + targetPath);
        }

        if (targetMatches.isEmpty()) {
            return violations;
        }

        for (ExternalPathMatch targetMatch : targetMatches) {
            String targetIdentifier = ExternalRulesetUtils.deriveTargetIdentifier(targetMatch.getValue(),
                    targetMatch.getPath());
            String contentPath = ExternalRulesetUtils.getContentPath(ruleDefinition);
            List<ExternalPathMatch> contentMatches = ExternalJsonPathUtils.findMatches(targetMatch.getValue(),
                    contentPath, targetMatch.getPath());

            if (log.isDebugEnabled()) {
                log.debug("Rule '" + ruleName + "' extracted " + contentMatches.size() + " validation values for "
                        + "target '" + targetIdentifier + "' using content path " + contentPath);
            }

            for (ExternalPathMatch contentMatch : contentMatches) {
                ExternalEvaluationContext evaluationContext = new ExternalEvaluationContext(
                        targetMatch.getValue(), targetMatch.getPath(), contentMatch.getValue(),
                        contentMatch.getPath(), targetIdentifier);
                violations.addAll(executeRuleForContent(ruleName, ruleDefinition, ruleset, evaluationContext));
            }
        }
        return violations;
    }

    private List<RuleViolation> executeRuleForContent(String ruleName, ExternalRuleDefinition ruleDefinition,
                                                      Ruleset ruleset, ExternalEvaluationContext evaluationContext)
            throws APIMGovernanceException {

        List<RuleViolation> violations = new ArrayList<>();
        Map<String, Object> templateContext = ExternalTemplateUtils.buildTemplateContext(evaluationContext);
        Object requestBody = ExternalTemplateUtils.renderTemplate(
                ruleDefinition.getPayload().getTemplate(), templateContext);
        Map<String, String> headers = ExternalTemplateUtils.renderHeaders(
                ExternalRulesetUtils.getHeaders(ruleDefinition), templateContext);

        JsonNode responseBody;
        try {
            responseBody = externalServiceClient.invoke(ruleDefinition, evaluationContext, requestBody, headers);
        } catch (ExternalServiceClient.ExternalServiceException e) {
            log.warn("External validation service call failed for rule '" + ruleName + "' at path "
                    + evaluationContext.getValuePath() + ": " + e.getMessage());
            violations.add(ExternalRulesetUtils.buildServiceFailureViolation(ruleset, ruleName,
                    evaluationContext, e.getMessage()));
            return violations;
        }

        List<ExternalPathMatch> resultMatches = ExternalJsonPathUtils.findMatches(responseBody,
                ruleDefinition.getResponse().getResultPath());
        if (resultMatches.isEmpty()) {
            String errorMessage = "Result path `" + ruleDefinition.getResponse().getResultPath()
                    + "` did not match the external service response";
            log.warn("External validation response handling failed for rule '" + ruleName + "' at path "
                    + evaluationContext.getValuePath() + ": " + errorMessage);
            violations.add(ExternalRulesetUtils.buildServiceFailureViolation(ruleset, ruleName,
                    evaluationContext, errorMessage));
            return violations;
        }

        JsonNode expectedResultNode = ExternalRulesetUtils.toJsonNode(ruleDefinition.getResponse().getExpectedValue());
        boolean violationDetected = false;
        JsonNode matchedResultNode = null;
        for (ExternalPathMatch resultMatch : resultMatches) {
            if (expectedResultNode.equals(resultMatch.getValue())) {
                violationDetected = true;
                matchedResultNode = resultMatch.getValue();
                break;
            }
        }

        if (!violationDetected) {
            if (log.isDebugEnabled()) {
                log.debug("Rule '" + ruleName + "' passed for target '" + evaluationContext.getTargetIdentifier()
                        + "' at path " + evaluationContext.getValuePath());
            }
            return violations;
        }

        String responseMessage = ExternalRulesetUtils.resolveResponseMessage(ruleDefinition, responseBody);
        Map<String, Object> messageContext = ExternalTemplateUtils.buildTemplateContext(evaluationContext);
        messageContext.put("responseMessage", responseMessage);
        messageContext.put("result", ExternalRulesetUtils.toJavaValue(matchedResultNode));

        String violationMessage = ExternalRulesetUtils.resolveViolationMessage(ruleName, ruleDefinition, messageContext,
                responseMessage);
        RuleSeverity severity = ExternalRulesetUtils.resolveSeverity(ruleDefinition.getSeverity());
        violations.add(ExternalRulesetUtils.buildRuleViolation(ruleset, ruleName, severity,
                evaluationContext, violationMessage));

        if (log.isDebugEnabled()) {
            log.debug("Rule '" + ruleName + "' produced a violation for target '"
                    + evaluationContext.getTargetIdentifier() + "' at path " + evaluationContext.getValuePath()
                    + " with severity " + severity);
        }
        return violations;
    }
}

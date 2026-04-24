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

package org.wso2.carbon.apimgt.governance.external.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovExceptionCodes;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;
import org.wso2.carbon.apimgt.governance.api.model.ExtendedArtifactType;
import org.wso2.carbon.apimgt.governance.api.model.RuleSeverity;
import org.wso2.carbon.apimgt.governance.api.model.RuleType;
import org.wso2.carbon.apimgt.governance.api.model.RuleViolation;
import org.wso2.carbon.apimgt.governance.api.model.Ruleset;
import org.wso2.carbon.apimgt.governance.api.model.RulesetContent;
import org.wso2.carbon.apimgt.governance.external.model.ExternalEvaluationContext;
import org.wso2.carbon.apimgt.governance.external.model.ExternalPathMatch;
import org.wso2.carbon.apimgt.governance.external.model.ExternalRequestPayload;
import org.wso2.carbon.apimgt.governance.external.model.ExternalResponseDefinition;
import org.wso2.carbon.apimgt.governance.external.model.ExternalRuleDefinition;
import org.wso2.carbon.apimgt.governance.external.model.ExternalRulesetDefinition;
import org.wso2.carbon.apimgt.governance.external.model.ExternalRulesetContentDefinition;

import java.net.MalformedURLException;
import java.net.URL;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Shared helper methods for the external governance engine.
 */
public final class ExternalRulesetUtils {

    private static final Log log = LogFactory.getLog(ExternalRulesetUtils.class);
    private static final ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory())
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    private static final String ROOT_PATH = "$";
    private static final int MAX_PATH_LENGTH = 1024;
    private static final int MAX_MESSAGE_LENGTH = 1024;

    private ExternalRulesetUtils() {
    }

    public static ExternalRulesetDefinition parseRuleset(Ruleset ruleset) throws APIMGovernanceException {

        RulesetContent rulesetContent = ruleset.getRulesetContent();
        if (rulesetContent == null || rulesetContent.getContent() == null || rulesetContent.getContent().length == 0) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.INVALID_RULESET_CONTENT, ruleset.getName());
        }
        try {
            ExternalRulesetDefinition definition = YAML_MAPPER.readValue(rulesetContent.getContent(),
                    ExternalRulesetDefinition.class);
            if (definition == null) {
                throw new APIMGovernanceException(APIMGovExceptionCodes.INVALID_RULESET_CONTENT, ruleset.getName());
            }
            if (definition.getRulesetContent() == null) {
                definition.setRulesetContent(new ExternalRulesetContentDefinition());
            }
            return definition;
        } catch (IOException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_FAILED_TO_PARSE_RULESET_CONTENT, e);
        }
    }

    public static JsonNode parseTargetDocument(String target) throws APIMGovernanceException {

        if (target == null || target.trim().isEmpty()) {
            throw new APIMGovernanceException("Target content is empty for external governance validation");
        }
        try {
            return YAML_MAPPER.readTree(target);
        } catch (JsonProcessingException e) {
            throw new APIMGovernanceException("Failed to parse target content for external governance validation", e);
        }
    }

    public static void applyRulesetMetadata(Ruleset ruleset, ExternalRulesetDefinition definition) {

        if (ruleset.getName() == null) {
            ruleset.setName(definition.getName());
        }
        if (ruleset.getDescription() == null) {
            ruleset.setDescription(definition.getDescription());
        }
        if (ruleset.getDocumentationLink() == null) {
            ruleset.setDocumentationLink(definition.getDocumentationLink());
        }
        if (ruleset.getProvider() == null) {
            ruleset.setProvider(definition.getProvider());
        }
        if (ruleset.getRuleType() == null) {
            ruleset.setRuleType(RuleType.fromString(definition.getRuleType()));
        }
        if (ruleset.getArtifactType() == null) {
            ruleset.setArtifactType(ExtendedArtifactType.fromString(definition.getArtifactType()));
        }

        if (log.isDebugEnabled()) {
            log.debug("Applied EXTERNAL ruleset metadata for ruleset: " + ruleset.getName()
                    + ", artifactType=" + ruleset.getArtifactType() + ", ruleType=" + ruleset.getRuleType());
        }
    }

    public static void validateRulesetDefinition(Ruleset ruleset, ExternalRulesetDefinition definition)
            throws APIMGovernanceException {

        if (!RuleType.API_DEFINITION.equals(ruleset.getRuleType())) {
            throw invalidRuleset(ruleset, "External governance rulesets only support ruleType `API_DEFINITION`");
        }

        if (!(ExtendedArtifactType.REST_API.equals(ruleset.getArtifactType())
                || ExtendedArtifactType.MCP.equals(ruleset.getArtifactType()))) {
            throw invalidRuleset(ruleset, "External governance rulesets only support artifact types `REST_API` "
                    + "and `MCP`");
        }

        if (definition.getRules().isEmpty()) {
            throw invalidRuleset(ruleset, "External governance rulesets must define at least one rule");
        }

        for (Map.Entry<String, ExternalRuleDefinition> entry : definition.getRules().entrySet()) {
            validateRuleDefinition(ruleset, entry.getKey(), entry.getValue());
        }
    }

    public static RuleSeverity resolveSeverity(String severityText) {

        if (severityText == null || severityText.trim().isEmpty()) {
            return RuleSeverity.WARN;
        }
        RuleSeverity parsedSeverity = RuleSeverity.fromString(severityText);
        return parsedSeverity != null ? parsedSeverity : RuleSeverity.WARN;
    }

    public static String sanitizeRuleDescription(String ruleName, String description) {

        if (description != null && description.length() > MAX_MESSAGE_LENGTH) {
            log.warn("Rule description of external rule `" + ruleName + "` exceeds "
                    + MAX_MESSAGE_LENGTH + " characters. Truncating description.");
            return description.substring(0, MAX_MESSAGE_LENGTH);
        }
        return description;
    }

    public static String getTargetPath(ExternalRuleDefinition ruleDefinition) {

        if (ruleDefinition == null || ruleDefinition.getTargetPath() == null
                || ruleDefinition.getTargetPath().trim().isEmpty()) {
            return ROOT_PATH;
        }
        return ruleDefinition.getTargetPath().trim();
    }

    public static String getContentPath(ExternalRuleDefinition ruleDefinition) {

        ExternalRequestPayload payload = ruleDefinition != null ? ruleDefinition.getPayload() : null;
        if (payload == null || payload.getContentPath() == null || payload.getContentPath().trim().isEmpty()) {
            return ROOT_PATH;
        }
        return payload.getContentPath().trim();
    }

    public static Map<String, Object> getHeaders(ExternalRuleDefinition ruleDefinition) {

        Map<String, Object> headers = new LinkedHashMap<>();
        if (ruleDefinition == null) {
            return headers;
        }
        ExternalRequestPayload payload = ruleDefinition.getPayload();
        if (payload != null && payload.getHeaders() != null) {
            headers.putAll(payload.getHeaders());
        }
        if (ruleDefinition.getHeaders() != null) {
            headers.putAll(ruleDefinition.getHeaders());
        }
        return headers;
    }

    public static String deriveTargetIdentifier(JsonNode targetNode, String fallbackPath) {

        if (targetNode != null && targetNode.isObject()) {
            String[] candidateFields = new String[] {"target", "name", "operationId", "displayName", "id", "title"};
            for (String fieldName : candidateFields) {
                JsonNode fieldValue = targetNode.get(fieldName);
                if (fieldValue != null && fieldValue.isValueNode()) {
                    String textValue = fieldValue.asText();
                    if (textValue != null && !textValue.trim().isEmpty()) {
                        return textValue;
                    }
                }
            }
        }
        return fallbackPath != null && !fallbackPath.trim().isEmpty() ? fallbackPath : ROOT_PATH;
    }

    public static RuleViolation buildRuleViolation(Ruleset ruleset, String ruleName, RuleSeverity severity,
                                                   ExternalEvaluationContext context, String message) {

        RuleViolation violation = new RuleViolation();
        violation.setRulesetId(ruleset.getId());
        violation.setRuleName(ruleName);
        violation.setSeverity(severity != null ? severity : RuleSeverity.WARN);
        violation.setViolatedPath(sanitizePath(context.getValuePath()));
        violation.setRuleMessage(sanitizeMessage(message));
        return violation;
    }

    public static RuleViolation buildServiceFailureViolation(Ruleset ruleset, String ruleName,
                                                             ExternalEvaluationContext context, String message) {

        String warningMessage = "External validation service call failed for target `"
                + context.getTargetIdentifier() + "`: " + message;
        return buildRuleViolation(ruleset, ruleName, RuleSeverity.WARN, context, warningMessage);
    }

    public static String resolveResponseMessage(ExternalRuleDefinition ruleDefinition, JsonNode responseBody)
            throws APIMGovernanceException {

        ExternalResponseDefinition responseDefinition = ruleDefinition.getResponse();
        if (responseDefinition == null || responseDefinition.getMessagePath() == null
                || responseDefinition.getMessagePath().trim().isEmpty()) {
            return null;
        }

        List<ExternalPathMatch> messageMatches = ExternalJsonPathUtils.findMatches(responseBody,
                responseDefinition.getMessagePath());
        if (messageMatches.isEmpty()) {
            return null;
        }
        Object messageValue = toJavaValue(messageMatches.get(0).getValue());
        return messageValue != null ? String.valueOf(messageValue) : null;
    }

    public static String resolveViolationMessage(String ruleName, ExternalRuleDefinition ruleDefinition,
                                                 Map<String, Object> templateContext, String responseMessage) {

        String messageTemplate = ruleDefinition.getMessage();
        if (messageTemplate != null && !messageTemplate.trim().isEmpty()) {
            return sanitizeMessage(ExternalTemplateUtils.renderMessage(messageTemplate, templateContext));
        }
        if (responseMessage != null && !responseMessage.trim().isEmpty()) {
            return sanitizeMessage(responseMessage);
        }
        return sanitizeMessage("External governance rule `" + ruleName + "` failed for target `"
                + templateContext.get("target") + "`");
    }

    public static JsonNode toJsonNode(Object value) {

        return JSON_MAPPER.valueToTree(value);
    }

    public static Object toJavaValue(JsonNode jsonNode) {

        if (jsonNode == null || jsonNode.isNull()) {
            return null;
        }
        return JSON_MAPPER.convertValue(jsonNode, Object.class);
    }

    private static void validateRuleDefinition(Ruleset ruleset, String ruleName, ExternalRuleDefinition ruleDefinition)
            throws APIMGovernanceException {

        if (ruleDefinition == null) {
            throw invalidRuleset(ruleset, "Rule `" + ruleName + "` must define a rule body");
        }
        if (ruleName == null || ruleName.length() > 256) {
            throw invalidRuleset(ruleset, "Rule name `" + ruleName + "` exceeds the maximum length of 256 "
                    + "characters");
        }
        if (ruleDefinition.getServiceUrl() == null || ruleDefinition.getServiceUrl().trim().isEmpty()) {
            throw invalidRuleset(ruleset, "Rule `" + ruleName + "` must define `serviceUrl`");
        }
        try {
            new URL(ruleDefinition.getServiceUrl());
        } catch (MalformedURLException e) {
            throw invalidRuleset(ruleset, "Rule `" + ruleName + "` contains an invalid `serviceUrl`");
        }

        ExternalRequestPayload payload = ruleDefinition.getPayload();
        if (payload == null) {
            throw invalidRuleset(ruleset, "Rule `" + ruleName + "` must define `payload`");
        }
        if (payload.getTemplate() == null) {
            throw invalidRuleset(ruleset, "Rule `" + ruleName + "` must define `payload.template`");
        }
        if (payload.getContentPath() != null && !payload.getContentPath().trim().isEmpty()
                && !payload.getContentPath().trim().startsWith(ROOT_PATH)) {
            throw invalidRuleset(ruleset, "Rule `" + ruleName + "` contains an invalid `payload.contentPath`");
        }
        if (ruleDefinition.getTargetPath() != null && !ruleDefinition.getTargetPath().trim().isEmpty()
                && !ruleDefinition.getTargetPath().trim().startsWith(ROOT_PATH)) {
            throw invalidRuleset(ruleset, "Rule `" + ruleName + "` contains an invalid `targetPath`");
        }

        ExternalResponseDefinition responseDefinition = ruleDefinition.getResponse();
        if (responseDefinition == null) {
            throw invalidRuleset(ruleset, "Rule `" + ruleName + "` must define `response`");
        }
        if (responseDefinition.getResultPath() == null || responseDefinition.getResultPath().trim().isEmpty()) {
            throw invalidRuleset(ruleset, "Rule `" + ruleName + "` must define `response.resultPath`");
        }
        if (!responseDefinition.getResultPath().trim().startsWith(ROOT_PATH)) {
            throw invalidRuleset(ruleset, "Rule `" + ruleName + "` contains an invalid `response.resultPath`");
        }
        if (responseDefinition.getExpectedValue() == null) {
            throw invalidRuleset(ruleset, "Rule `" + ruleName + "` must define `response.expectedValue`");
        }
        if (responseDefinition.getMessagePath() != null && !responseDefinition.getMessagePath().trim().isEmpty()
                && !responseDefinition.getMessagePath().trim().startsWith(ROOT_PATH)) {
            throw invalidRuleset(ruleset, "Rule `" + ruleName + "` contains an invalid `response.messagePath`");
        }
        if (ruleDefinition.getTimeout() != null && ruleDefinition.getTimeout() <= 0) {
            throw invalidRuleset(ruleset, "Rule `" + ruleName + "` contains an invalid `timeout`");
        }
        if (ruleDefinition.getRetry() != null && ruleDefinition.getRetry() <= 0) {
            throw invalidRuleset(ruleset, "Rule `" + ruleName + "` contains an invalid `retry`");
        }
    }

    private static APIMGovernanceException invalidRuleset(Ruleset ruleset, String message) {

        return new APIMGovernanceException(APIMGovExceptionCodes.INVALID_RULESET_CONTENT_DETAILED,
                ruleset.getName(), message);
    }

    private static String sanitizePath(String path) {

        if (path == null || path.trim().isEmpty()) {
            return ROOT_PATH;
        }
        if (path.length() > MAX_PATH_LENGTH) {
            log.warn("External governance violated path exceeds " + MAX_PATH_LENGTH
                    + " characters. Truncating path.");
            return path.substring(0, MAX_PATH_LENGTH);
        }
        return path;
    }

    private static String sanitizeMessage(String message) {

        if (message == null || message.trim().isEmpty()) {
            return "External governance validation failed.";
        }
        if (message.length() > MAX_MESSAGE_LENGTH) {
            log.warn("External governance violation message exceeds " + MAX_MESSAGE_LENGTH
                    + " characters. Truncating message.");
            return message.substring(0, MAX_MESSAGE_LENGTH);
        }
        return message;
    }
}

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
import com.fasterxml.jackson.databind.ObjectMapper;
import org.wso2.carbon.apimgt.governance.external.model.ExternalEvaluationContext;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility methods for external request/message templating.
 */
public final class ExternalTemplateUtils {

    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{\\s*([^{}\\s]+)\\s*}}");
    private static final Pattern EXACT_PLACEHOLDER_PATTERN = Pattern.compile("^\\{\\{\\s*([^{}\\s]+)\\s*}}$");

    private ExternalTemplateUtils() {
    }

    public static Map<String, Object> buildTemplateContext(ExternalEvaluationContext evaluationContext) {

        Map<String, Object> templateContext = new LinkedHashMap<>();
        templateContext.put("target", evaluationContext.getTargetIdentifier());
        templateContext.put("value", ExternalRulesetUtils.toJavaValue(evaluationContext.getValueNode()));
        templateContext.put("path", evaluationContext.getValuePath());
        return templateContext;
    }

    public static Object renderTemplate(Object template, Map<String, Object> templateContext) {

        if (template instanceof Map) {
            Map<String, Object> renderedMap = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) template).entrySet()) {
                renderedMap.put(String.valueOf(entry.getKey()), renderTemplate(entry.getValue(), templateContext));
            }
            return renderedMap;
        }
        if (template instanceof List) {
            List<Object> renderedList = new ArrayList<>();
            for (Object item : (List<?>) template) {
                renderedList.add(renderTemplate(item, templateContext));
            }
            return renderedList;
        }
        if (template instanceof String) {
            return renderStringTemplate((String) template, templateContext);
        }
        return template;
    }

    public static Map<String, String> renderHeaders(Map<String, Object> headers, Map<String, Object> templateContext) {

        Map<String, String> renderedHeaders = new LinkedHashMap<>();
        if (headers == null || headers.isEmpty()) {
            return renderedHeaders;
        }
        for (Map.Entry<String, Object> headerEntry : headers.entrySet()) {
            Object renderedValue = renderTemplate(headerEntry.getValue(), templateContext);
            renderedHeaders.put(headerEntry.getKey(), stringifyValue(renderedValue));
        }
        return renderedHeaders;
    }

    public static String renderMessage(String messageTemplate, Map<String, Object> templateContext) {

        Object rendered = renderTemplate(messageTemplate, templateContext);
        return rendered != null ? String.valueOf(rendered) : null;
    }

    private static Object renderStringTemplate(String template, Map<String, Object> templateContext) {

        Matcher exactMatcher = EXACT_PLACEHOLDER_PATTERN.matcher(template);
        if (exactMatcher.matches()) {
            String key = exactMatcher.group(1);
            return templateContext.get(key);
        }

        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
        StringBuffer renderedBuffer = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(1);
            String replacement = stringifyValue(templateContext.get(key));
            matcher.appendReplacement(renderedBuffer, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(renderedBuffer);
        return renderedBuffer.toString();
    }

    private static String stringifyValue(Object value) {

        if (value == null) {
            return "";
        }
        if (value instanceof String || value instanceof Number || value instanceof Boolean) {
            return String.valueOf(value);
        }
        try {
            return JSON_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return String.valueOf(value);
        }
    }
}

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

import com.fasterxml.jackson.databind.JsonNode;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;
import org.wso2.carbon.apimgt.governance.external.model.ExternalPathMatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Minimal JSON-path evaluator for the external governance engine.
 */
public final class ExternalJsonPathUtils {

    private static final String ROOT_PATH = "$";

    private ExternalJsonPathUtils() {
    }

    public static List<ExternalPathMatch> findMatches(JsonNode rootNode, String jsonPath)
            throws APIMGovernanceException {

        return findMatches(rootNode, jsonPath, ROOT_PATH);
    }

    public static List<ExternalPathMatch> findMatches(JsonNode rootNode, String jsonPath, String basePath)
            throws APIMGovernanceException {

        if (rootNode == null) {
            return Collections.emptyList();
        }

        String effectivePath = jsonPath == null || jsonPath.trim().isEmpty() ? ROOT_PATH : jsonPath.trim();
        String effectiveBasePath = basePath == null || basePath.trim().isEmpty() ? ROOT_PATH : basePath.trim();
        if (ROOT_PATH.equals(effectivePath)) {
            return Collections.singletonList(new ExternalPathMatch(rootNode, effectiveBasePath));
        }
        if (!effectivePath.startsWith(ROOT_PATH)) {
            throw new APIMGovernanceException("Invalid external JSON path `" + effectivePath
                    + "`. Paths must start with `$`.");
        }

        List<PathToken> tokens = tokenize(effectivePath);
        List<ExternalPathMatch> currentMatches = new ArrayList<>();
        currentMatches.add(new ExternalPathMatch(rootNode, effectiveBasePath));

        for (PathToken token : tokens) {
            List<ExternalPathMatch> nextMatches = new ArrayList<>();
            for (ExternalPathMatch currentMatch : currentMatches) {
                applyToken(currentMatch, token, nextMatches);
            }
            currentMatches = nextMatches;
            if (currentMatches.isEmpty()) {
                return currentMatches;
            }
        }
        return currentMatches;
    }

    private static void applyToken(ExternalPathMatch currentMatch, PathToken token,
                                   List<ExternalPathMatch> nextMatches) {

        JsonNode currentNode = currentMatch.getValue();
        switch (token.getType()) {
            case FIELD:
                if (currentNode != null && currentNode.isObject() && currentNode.has(token.getValue())) {
                    JsonNode childNode = currentNode.get(token.getValue());
                    nextMatches.add(new ExternalPathMatch(childNode,
                            currentMatch.getPath() + "." + token.getValue()));
                }
                break;
            case ARRAY_INDEX:
                if (currentNode != null && currentNode.isArray()) {
                    int index = Integer.parseInt(token.getValue());
                    if (index >= 0 && index < currentNode.size()) {
                        nextMatches.add(new ExternalPathMatch(currentNode.get(index),
                                currentMatch.getPath() + "[" + index + "]"));
                    }
                }
                break;
            case ARRAY_WILDCARD:
                if (currentNode != null && currentNode.isArray()) {
                    for (int index = 0; index < currentNode.size(); index++) {
                        nextMatches.add(new ExternalPathMatch(currentNode.get(index),
                                currentMatch.getPath() + "[" + index + "]"));
                    }
                }
                break;
            default:
                break;
        }
    }

    private static List<PathToken> tokenize(String jsonPath) throws APIMGovernanceException {

        List<PathToken> tokens = new ArrayList<>();
        int index = 1;
        while (index < jsonPath.length()) {
            char currentChar = jsonPath.charAt(index);
            if (currentChar == '.') {
                index++;
                int startIndex = index;
                while (index < jsonPath.length() && jsonPath.charAt(index) != '.'
                        && jsonPath.charAt(index) != '[') {
                    index++;
                }
                if (startIndex == index) {
                    throw new APIMGovernanceException("Invalid external JSON path `" + jsonPath + "`.");
                }
                tokens.add(PathToken.field(jsonPath.substring(startIndex, index)));
                continue;
            }
            if (currentChar == '[') {
                int closingIndex = jsonPath.indexOf(']', index);
                if (closingIndex < 0) {
                    throw new APIMGovernanceException("Invalid external JSON path `" + jsonPath + "`.");
                }
                String arrayToken = jsonPath.substring(index + 1, closingIndex).trim();
                if ("*".equals(arrayToken)) {
                    tokens.add(PathToken.arrayWildcard());
                } else {
                    try {
                        int parsedIndex = Integer.parseInt(arrayToken);
                        if (parsedIndex < 0) {
                            throw new NumberFormatException("Negative array index");
                        }
                    } catch (NumberFormatException e) {
                        throw new APIMGovernanceException("Invalid array token `" + arrayToken
                                + "` in external JSON path `" + jsonPath + "`.", e);
                    }
                    tokens.add(PathToken.arrayIndex(arrayToken));
                }
                index = closingIndex + 1;
                continue;
            }

            int startIndex = index;
            while (index < jsonPath.length() && jsonPath.charAt(index) != '.'
                    && jsonPath.charAt(index) != '[') {
                index++;
            }
            tokens.add(PathToken.field(jsonPath.substring(startIndex, index)));
        }
        return tokens;
    }

    /**
     * Token model for the minimal JSON-path evaluator.
     */
    private static final class PathToken {

        private final TokenType type;
        private final String value;

        private PathToken(TokenType type, String value) {

            this.type = type;
            this.value = value;
        }

        public static PathToken field(String value) {

            return new PathToken(TokenType.FIELD, value);
        }

        public static PathToken arrayIndex(String value) {

            return new PathToken(TokenType.ARRAY_INDEX, value);
        }

        public static PathToken arrayWildcard() {

            return new PathToken(TokenType.ARRAY_WILDCARD, null);
        }

        public TokenType getType() {

            return type;
        }

        public String getValue() {

            return value;
        }
    }

    /**
     * Token type for the minimal JSON-path evaluator.
     */
    private enum TokenType {
        FIELD,
        ARRAY_INDEX,
        ARRAY_WILDCARD
    }
}

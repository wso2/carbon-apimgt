/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com/).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.spec.parser.definitions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.model.Scope;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

public class APISpecParserUtil {

    private static final Log log = LogFactory.getLog(APISpecParserUtil.class);

    /**
     * Find scope object in a set based on the key
     *
     * @param scopes - Set of scopes
     * @param key    - Key to search with
     * @return Scope - scope object
     */
    public static Scope findScopeByKey(Set<Scope> scopes, String key) {

        for (Scope scope : scopes) {
            if (scope.getKey().equals(key)) {
                return scope;
            }
        }
        return null;
    }

    /**
     * Extracts the endpoint URL from an AsyncAPI or OpenAPI definition (JSON/YAML).
     * Supports AsyncAPI v2/v3 (servers as Map) and OpenAPI (servers as Array).
     *
     * @param apiDefinition The API definition string (JSON/YAML).
     * @return The extracted URL or null if not found.
     */
    public static String getEndpointUrlFromAsyncApiDefinition(String apiDefinition) {
        try {
            if (apiDefinition == null || apiDefinition.trim().isEmpty()) {
                return null;
            }
            JsonNode rootNode = new ObjectMapper().readTree(apiDefinition);
            if (rootNode.has("servers")) {
                JsonNode serversNode = rootNode.get("servers");
                if (serversNode.isArray() && serversNode.size() > 0) {
                    JsonNode server = serversNode.get(0);
                    if (server != null && server.has("url")) {
                        String resolvedUrl = server.get("url").asText();
                        if (server.has("variables") && server.get("variables").has("basePath")) {
                            JsonNode basePath = server.get("variables").get("basePath");
                            if (basePath.has("default")) {
                                String stageName = basePath.get("default").asText();
                                resolvedUrl = resolvedUrl
                                        .replace("/{basePath}", "/" + stageName)
                                        .replace("{basePath}", stageName);
                            }
                        }
                        return resolvedUrl;
                    }
                } else if (serversNode.isObject()) {
                    Iterator<JsonNode> elements = serversNode.elements();
                    if (elements.hasNext()) {
                        JsonNode server = elements.next();
                        if (server.has("url")) {
                            return server.get("url").asText();
                        }
                    }
                }
            }
        } catch (IOException e) {
            log.error("Error while parsing definition to extract URL", e);
        }
        return null;
    }
}

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

package org.wso2.carbon.apimgt.impl.definitions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIDefinitionProcessor;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.importexport.ImportExportConstants;
import org.wso2.carbon.apimgt.spec.parser.definitions.OASParserUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * DefinitionProcessor implementation for OpenAPI (Swagger) specifications.
 */
public class OASDefinitionProcessor implements APIDefinitionProcessor {

    private static final Log log = LogFactory.getLog(OASDefinitionProcessor.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public String getType(API api) {
        return api.getType();
    }

    @Override
    public boolean isAsync(API api) {
        return false;
    }

    @Override
    public String getDefinitionFromAPI(API api) {
        return api.getSwaggerDefinition();
    }

    @Override
    public void setDefinitionToAPI(API api, String definition) {
        api.setSwaggerDefinition(definition);
    }

    @Override
    public String extractEndpointUrl(String definition) {
        try {
            if (definition == null || definition.trim().isEmpty()) {
                return null;
            }
            JsonNode rootNode = OBJECT_MAPPER.readTree(definition);

            if (rootNode.has("servers") && rootNode.get("servers").isArray()) {
                JsonNode servers = rootNode.get("servers");
                if (servers.size() > 0) {
                    JsonNode server = servers.get(0);
                    if (server.has("url")) {
                        String resolvedUrl = server.get("url").asText();
                        
                        // Handle variables (e.g., {basePath})
                        if (server.has("variables")) {
                            JsonNode variables = server.get("variables");
                            if (variables.has("basePath") && variables.get("basePath").has("default")) {
                                String basePath = variables.get("basePath").get("default").asText();
                                resolvedUrl = resolvedUrl
                                        .replace("/{basePath}", "/" + basePath)
                                        .replace("{basePath}", basePath);
                            }
                        }
                        return resolvedUrl;
                    }
                }
            }
        } catch (IOException e) {
            log.error("Error extracting endpoint URL from OAS definition", e);
        }
        return null;
    }

    @Override
    public List<URITemplate> extractOperations(String definition) throws APIManagementException {
        if (definition == null || definition.isEmpty()) {
            return new ArrayList<>();
        }

        // 1. Get the correct parser (Auto-detects OAS2 vs OAS3)
        APIDefinition parser = OASParserUtil.getOASParser(definition);

        // 2. Extract the templates
        Set<URITemplate> templates = parser.getURITemplates(definition);

        // 3. Return as a List (Null safe)
        if (templates != null) {
            return new ArrayList<>(templates);
        }
        return new ArrayList<>();
    }

    @Override
    public String getDefinitionFileName() {
        return ImportExportConstants.SWAGGER_YAML_FILE_NAME;
    }
}

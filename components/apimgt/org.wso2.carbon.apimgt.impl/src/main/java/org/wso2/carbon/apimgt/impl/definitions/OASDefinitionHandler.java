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

package org.wso2.carbon.apimgt.impl.definitions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIDefinitionHandler;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.importexport.ImportExportConstants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.wso2.carbon.apimgt.impl.APIConstants.SWAGGER_DESCRIPTION;
import static org.wso2.carbon.apimgt.impl.APIConstants.SWAGGER_INFO;
import static org.wso2.carbon.apimgt.impl.APIConstants.SWAGGER_TITLE;
import static org.wso2.carbon.apimgt.impl.APIConstants.SWAGGER_VER;

/**
 * DefinitionHandler implementation for OpenAPI (Swagger) specifications.
 */
public class OASDefinitionHandler implements APIDefinitionHandler {

    private static final Log log = LogFactory.getLog(OASDefinitionHandler.class);

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
    public void updateAPIDefinitionWithVersion(API api) throws APIManagementException {
        String swaggerDefinition = api.getSwaggerDefinition();

        if (swaggerDefinition != null) {
            JsonObject apiSpec = JsonParser.parseString(swaggerDefinition).getAsJsonObject();
            JsonObject infoObject = apiSpec.has(SWAGGER_INFO) && apiSpec.get(SWAGGER_INFO).isJsonObject() ?
                    apiSpec.getAsJsonObject(SWAGGER_INFO) : null;
            if (infoObject != null) {
                infoObject.addProperty(SWAGGER_VER, api.getId().getVersion());
            } else {
                JsonObject newInfoObject = new JsonObject();
                newInfoObject.addProperty(SWAGGER_TITLE, api.getId().getApiName());
                newInfoObject.addProperty(SWAGGER_VER, api.getId().getVersion());
                newInfoObject.addProperty(SWAGGER_DESCRIPTION, api.getDescription());
                apiSpec.add(SWAGGER_INFO, newInfoObject);
            }
            api.setSwaggerDefinition(apiSpec.toString());
        }else {
            log.error("Swagger definition is null for API: " + api.getId().getApiName());
        }
    }

    @Override
    public String extractEndpointUrl(String definition) {
        try {
            if (definition == null || definition.trim().isEmpty()) {
                return null;
            }
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(definition);

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

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
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIDefinitionHandler;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.spec.parser.definitions.AbstractAsyncApiParser;
import org.wso2.carbon.apimgt.spec.parser.definitions.AsyncApiParserUtil;
import org.wso2.carbon.apimgt.spec.parser.definitions.asyncapi.AsyncApiParserFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.wso2.carbon.apimgt.impl.APIConstants.SWAGGER_DESCRIPTION;
import static org.wso2.carbon.apimgt.impl.APIConstants.SWAGGER_INFO;
import static org.wso2.carbon.apimgt.impl.APIConstants.SWAGGER_TITLE;
import static org.wso2.carbon.apimgt.impl.APIConstants.SWAGGER_VER;

/**
 * DefinitionHandler implementation for AsyncAPI specifications (WebSocket, WebSub, SSE).
 */
public class AsyncAPIDefinitionHandler implements APIDefinitionHandler {

    private static final Log log = LogFactory.getLog(AsyncAPIDefinitionHandler.class);

    @Override
    public String getType(API api) {
        return api.getType();
    }

    @Override
    public boolean isAsync(API api) {
        return true;
    }

    @Override
    public String getDefinitionFromAPI(API api) {
        return api.getAsyncApiDefinition();
    }

    @Override
    public void setDefinitionToAPI(API api, String definition) {
        api.setAsyncApiDefinition(definition);
    }

    @Override
    public void updateAPIDefinitionWithVersion(API api) throws APIManagementException {
        String asyncApiDefinition = api.getAsyncApiDefinition();

        if (asyncApiDefinition != null && !asyncApiDefinition.trim().isEmpty()) {
            JsonObject apiSpec = JsonParser.parseString(asyncApiDefinition).getAsJsonObject();
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
            api.setAsyncApiDefinition(apiSpec.toString());
        } else {
            if (log.isDebugEnabled()) {
                log.debug("AsyncAPI definition is null or empty for API: " + api.getId().getApiName() 
                        + ". Skipping version update.");
            }
        }
    }

    @Override
    public String extractEndpointUrl(String definition) {
        try {
            if (definition == null || definition.trim().isEmpty()) {
                return null;
            }
            JsonNode rootNode = new ObjectMapper().readTree(definition);
            if (rootNode.has("servers")) {
                JsonNode serversNode = rootNode.get("servers");
                Iterator<JsonNode> elements = serversNode.elements();
                if (elements.hasNext()) {
                // Get the first server defined (e.g., 'production')
                    JsonNode server = elements.next();
                
                // 4. Directly extract the URL
                    if (server.has("url")) {
                        return server.get("url").asText();
                    }
                }    
            }
        } catch (IOException e) {
            log.error("Error while parsing definition to extract URL", e);
        }
        return null;
    }

    @Override
    public List<URITemplate> extractOperations(String definition) throws APIManagementException {
        // AsyncAPI operations/channels are typically handled differently
        // Extract both publish and subscribe operations
        List<URITemplate> uriTemplates = new ArrayList<>();
        try {
            AbstractAsyncApiParser asyncApiParser = AsyncApiParserFactory.getAsyncApiParser(
                AsyncApiParserUtil.getAsyncApiVersion(definition), 
                null 
            );

            // Pass true to include publish operations along with subscribe operations
            Set<URITemplate> asyncTemplates = asyncApiParser
                .getURITemplates(definition, true);

            if (asyncTemplates != null) {
                uriTemplates.addAll(asyncTemplates);
            }
        } catch (Exception e) {
            log.error("Error extracting operations from AsyncAPI definition", e);
        }
        return uriTemplates;
    }

    @Override
    public String getDefinitionFileName() {
        return "Definitions/asyncapi.yaml";
    }
}

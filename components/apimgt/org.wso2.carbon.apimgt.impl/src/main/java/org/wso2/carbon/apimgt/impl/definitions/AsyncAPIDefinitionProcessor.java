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
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIDefinitionProcessor;
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

/**
 * DefinitionProcessor implementation for AsyncAPI specifications (WebSocket, WebSub, SSE).
 */
public class AsyncAPIDefinitionProcessor implements APIDefinitionProcessor {

    private static final Log log = LogFactory.getLog(AsyncAPIDefinitionProcessor.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final ObjectMapper YAML_OBJECT_MAPPER = new ObjectMapper(new YAMLFactory());

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
    public String extractEndpointUrl(String definition) {
        try {
            if (definition == null || definition.trim().isEmpty()) {
                return null;
            }
            JsonNode rootNode = readDefinition(definition);
            if (rootNode.has("servers")) {
                JsonNode serversNode = rootNode.get("servers");
                Iterator<JsonNode> elements = serversNode.elements();
                if (elements.hasNext()) {
                    JsonNode server = elements.next();

                    return resolveEndpointUrl(server);
                }
            }
        } catch (IOException e) {
            log.error("Error while parsing definition to extract URL", e);
        }
        return null;
    }

    private JsonNode readDefinition(String definition) throws IOException {
        try {
            return OBJECT_MAPPER.readTree(definition);
        } catch (IOException jsonException) {
            try {
                return YAML_OBJECT_MAPPER.readTree(definition);
            } catch (IOException yamlException) {
                yamlException.addSuppressed(jsonException);
                throw yamlException;
            }
        }
    }

    private String resolveEndpointUrl(JsonNode server) {
        if (server == null || !server.has("url")) {
            return null;
        }

        String url = server.get("url").asText();
        if (url == null || url.isEmpty()) {
            return null;
        }

        if (!server.has("protocol")) {
            return url;
        }

        String protocol = server.get("protocol").asText();
        if (protocol == null || protocol.isEmpty() || hasScheme(url, protocol)) {
            return url;
        }

        if (url.startsWith("//")) {
            return protocol + ":" + url;
        }

        return protocol + "://" + url;
    }

    private boolean hasScheme(String url, String protocol) {
        return url.contains("://") || url.startsWith(protocol + ":");
    }

    @Override
    public List<URITemplate> extractOperations(String definition) throws APIManagementException {
        // AsyncAPI operations/channels are typically handled differently
        // Extract both publish and subscribe operations
        try {
            AbstractAsyncApiParser asyncApiParser = AsyncApiParserFactory.getAsyncApiParser(
                AsyncApiParserUtil.getAsyncApiVersion(definition), 
                null 
            );

            // Pass true to include publish operations along with subscribe operations
            Set<URITemplate> asyncTemplates = asyncApiParser
                .getURITemplates(definition, true);

            if (asyncTemplates != null) {
                return new ArrayList<>(asyncTemplates);
            }
            return new ArrayList<>();
        } catch (APIManagementException e) {
            throw e;
        } catch (Exception e) {
            throw new APIManagementException("Error extracting operations from AsyncAPI definition", e);
        }
    }

    @Override
    public String getDefinitionFileName() {
        return "Definitions/asyncapi.yaml";
    }
}

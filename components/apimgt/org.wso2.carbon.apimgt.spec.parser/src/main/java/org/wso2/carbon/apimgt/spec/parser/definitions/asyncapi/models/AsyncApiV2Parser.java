/*
 *   Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com)
 *
 *   WSO2 LLC. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.apimgt.spec.parser.definitions.asyncapi.models;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.apicurio.datamodels.Library;
import io.apicurio.datamodels.models.MappedNode;
import io.apicurio.datamodels.models.asyncapi.AsyncApiChannelItem;
import io.apicurio.datamodels.models.asyncapi.AsyncApiChannels;
import io.apicurio.datamodels.models.asyncapi.AsyncApiComponents;
import io.apicurio.datamodels.models.asyncapi.AsyncApiDocument;
import io.apicurio.datamodels.models.asyncapi.AsyncApiExtensible;
import io.apicurio.datamodels.models.asyncapi.AsyncApiOAuthFlow;
import io.apicurio.datamodels.models.asyncapi.AsyncApiOperation;
import io.apicurio.datamodels.models.asyncapi.AsyncApiSecurityScheme;
import io.apicurio.datamodels.models.asyncapi.AsyncApiServer;
import io.apicurio.datamodels.models.asyncapi.AsyncApiServers;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIDefinitionValidationResponse;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.spec.parser.definitions.APISpecParserConstants;
import org.wso2.carbon.apimgt.spec.parser.definitions.APISpecParserUtil;
import org.wso2.carbon.apimgt.spec.parser.definitions.AsyncApiParser;
import org.wso2.carbon.apimgt.spec.parser.definitions.AsyncApiParserUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class is used to parse AsyncAPI 2.x.x specifications.
 * It extends the AsyncApiParser class to provide specific parsing capabilities for AsyncAPI 2.x.x.
 */

public class AsyncApiV2Parser extends AsyncApiParser {

    private static final Log log = LogFactory.getLog(AsyncApiV2Parser.class);

    @Override
    public Set<URITemplate> getURITemplates(String apiDefinition, boolean includePublish)
            throws APIManagementException {
        Set<URITemplate> uriTemplates = new HashSet<>();
        Set<Scope> scopes = getScopes(apiDefinition);
        AsyncApiDocument document = (AsyncApiDocument) Library.readDocumentFromJSONString(apiDefinition);
        AsyncApiChannels apiChannels = document.getChannels();
        MappedNode channels = (MappedNode) apiChannels;
        if (channels != null && !channels.getItems().isEmpty()) {
            for (Object entry : channels.getItemNames()) {
                AsyncApiChannelItem channel = (AsyncApiChannelItem) channels.getItem((String) entry);
                if (includePublish && channel.getPublish() != null) {
                    uriTemplates.add(buildURITemplate((String) entry, APISpecParserConstants.HTTP_VERB_PUBLISH,
                            channel.getPublish(), scopes, channel));
                }
                if (channel.getSubscribe() != null) {
                    uriTemplates.add(buildURITemplate( (String) entry, APISpecParserConstants.HTTP_VERB_SUBSCRIBE,
                            channel.getSubscribe(), scopes, channel));
                }
            }
        }
        return uriTemplates;
    }

    private URITemplate buildURITemplate(String target, String verb, AsyncApiOperation operation, Set<Scope> scopes,
                                         AsyncApiChannelItem channel) throws APIManagementException {
        URITemplate template = new URITemplate();
        template.setHTTPVerb(verb);
        template.setHttpVerbs(verb);
        template.setUriTemplate(target);

        AsyncApiExtensible asyncApiExtensibleChannel = (AsyncApiExtensible) channel;
        Map<String, JsonNode> extensions = asyncApiExtensibleChannel.getExtensions();
        if (extensions != null) {
            JsonNode authTypeExtension = extensions.get(APISpecParserConstants.SWAGGER_X_AUTH_TYPE);
            if (authTypeExtension != null && authTypeExtension.isTextual()) {
                template.setAuthType(authTypeExtension.asText());
            }
        }

        List<String> opScopes = getScopeOfOperations(operation);
        if (!opScopes.isEmpty()) {
            if (opScopes.size() == 1) {
                String firstScope = opScopes.get(0);
                Scope scope = APISpecParserUtil.findScopeByKey(scopes, firstScope);
                if (scope == null) {
                    throw new APIManagementException("Scope '" + firstScope + "' not found.",
                            ExceptionCodes.ERROR_READING_ASYNCAPI_SPECIFICATION);
                }
                template.setScope(scope);
                template.setScopes(scope);
            } else {
                for (String scopeName : opScopes) {
                    Scope scope = APISpecParserUtil.findScopeByKey(scopes, scopeName);
                    if (scope == null) {
                        throw new APIManagementException("Resource Scope '" + scopeName + "' not found.",
                                ExceptionCodes.ERROR_READING_ASYNCAPI_SPECIFICATION);
                    }
                    template.setScopes(scope);
                }
            }
        }
        return template;
    }

    private List<String> getScopeOfOperations(AsyncApiOperation operation) {
        return getScopeOfOperationsFromExtensions(operation);
    }

    private List<String> getScopeOfOperationsFromExtensions(AsyncApiOperation operation) {
        AsyncApiExtensible asyncApiExtensibleOperation = (AsyncApiExtensible) operation;
        Map<String, JsonNode> extensions = asyncApiExtensibleOperation.getExtensions();
        if (extensions != null) {
            JsonNode scopeBindings = extensions.get(APISpecParserConstants.SWAGGER_X_BASIC_AUTH_SCOPES);
            if (scopeBindings != null) {
                if (scopeBindings.isArray()) {
                    List<String> scopes = new ArrayList<>();
                    for (JsonNode node : scopeBindings) {
                        if (node.isTextual()) {
                            scopes.add(node.asText());
                        }
                    }
                    return scopes;
                }

                if (scopeBindings.isObject()) {
                    List<String> scopes = new ArrayList<>();
                    scopeBindings.elements().forEachRemaining(node -> {
                        if (node.isTextual()) {
                            scopes.add(node.asText());
                        }
                    });
                    return scopes;
                }
            }
        }
        return Collections.emptyList();
    }

    @Override
    public Set<Scope> getScopes(String resourceConfigsJSON) throws APIManagementException {
        Set<Scope> scopeSet = new LinkedHashSet<>();
        AsyncApiDocument document = (AsyncApiDocument) Library.readDocumentFromJSONString(resourceConfigsJSON);
        AsyncApiComponents components = document.getComponents();

        if (components != null && components.getSecuritySchemes() != null) {
            AsyncApiSecurityScheme oauth2 = (AsyncApiSecurityScheme) components.getSecuritySchemes().get(
                    APISpecParserConstants.DEFAULT_API_SECURITY_OAUTH2);

            if (oauth2 != null && oauth2.getFlows() != null && oauth2.getFlows().getImplicit() != null) {

                AsyncApiOAuthFlow implicitFlow = (AsyncApiOAuthFlow) oauth2.getFlows().getImplicit();
                Map<String, String> scopes = AsyncApiParserUtil.getAsyncApiOAuthFlowsScopes(implicitFlow);
                AsyncApiExtensible asyncApiExtImplicitFlow = (AsyncApiExtensible) implicitFlow;
                Map<String, JsonNode> extensions = implicitFlow != null ? asyncApiExtImplicitFlow.getExtensions():null;

                JsonNode xScopesBindings = null;
                if (extensions != null) {
                    xScopesBindings = extensions.get(APISpecParserConstants.SWAGGER_X_SCOPES_BINDINGS);
                }
                Map<String, String> scopeBindings = new HashMap<>();

                if (xScopesBindings != null && xScopesBindings.isObject()) {
                    Iterator<Map.Entry<String, JsonNode>> fields = xScopesBindings.fields();
                    while (fields.hasNext()) {
                        Map.Entry<String, JsonNode> entry = fields.next();
                        if (entry.getValue().isTextual()) {
                            scopeBindings.put(entry.getKey(), entry.getValue().asText());
                        }
                    }
                }
                if (scopes != null) {
                    for (Map.Entry<String, String> entry : scopes.entrySet()) {
                        Scope scope = new Scope();
                        scope.setKey(entry.getKey());
                        scope.setName(entry.getKey());
                        scope.setDescription(entry.getValue());
                        String scopeBinding = scopeBindings.get(scope.getKey());
                        if (scopeBinding != null) {
                            scope.setRoles(scopeBinding);
                        }
                        scopeSet.add(scope);
                    }
                }
            }
        }
        return scopeSet;
    }

    @Override
    public APIDefinitionValidationResponse validateAPIDefinition(String apiDefinition, boolean returnJsonContent)
            throws APIManagementException {

        APIDefinitionValidationResponse validationResponse = new APIDefinitionValidationResponse();
        String protocol = StringUtils.EMPTY;
        boolean validationSuccess = false;
        List<String> validationErrorMessages = new ArrayList<>();

        try {
            validationSuccess = AsyncApiParserUtil.validateAsyncApiContent(apiDefinition, validationErrorMessages);
        } catch (Exception e) {
            // unexpected problems during validation/parsing
            String msg = "Error occurred while validating AsyncAPI definition: " + e.getMessage();
            log.error(msg, e);
            throw new APIManagementException(msg, e, ExceptionCodes.ERROR_READING_ASYNCAPI_SPECIFICATION);
        }

        if (validationSuccess) {
            AsyncApiDocument asyncApiDocument = (AsyncApiDocument) Library.readDocumentFromJSONString(apiDefinition);
            ArrayList<String> endpoints = new ArrayList<>();
            AsyncApiServers servers = asyncApiDocument.getServers();
            if (servers != null && servers.getItems() != null && !servers.getItems().isEmpty() &&
                    servers.getItems().size() == 1)
            {
                protocol = ((AsyncApiServer) asyncApiDocument.getServers().getItems().get(0)).getProtocol();
            }

            AsyncApiParserUtil.updateValidationResponseAsSuccess(
                    validationResponse,
                    apiDefinition,
                    asyncApiDocument.getAsyncapi(),
                    asyncApiDocument.getInfo().getTitle(),
                    asyncApiDocument.getInfo().getVersion(),
                    null,
                    asyncApiDocument.getInfo().getDescription(),
                    null
            );

            validationResponse.setParser(this);
            if (returnJsonContent) {
                validationResponse.setJsonContent(apiDefinition);
            }
            if (StringUtils.isNotEmpty(protocol)) {
                validationResponse.setProtocol(protocol);
            }
        } else {
            if (validationErrorMessages != null){
                validationResponse.setValid(false);
                for (String errorMessage: validationErrorMessages){
                    AsyncApiParserUtil.addErrorToValidationResponse(validationResponse, errorMessage);
                }
            }
        }
        return validationResponse;
    }

    @Override
    public String generateAsyncAPIDefinition(API api) throws APIManagementException {
        AsyncApiDocument asyncApiDocument = AsyncApiParserUtil.createAsyncApiDocument(
                APISpecParserConstants.AsyncApi.ASYNC_API_V20);
        asyncApiDocument.setAsyncapi(APISpecParserConstants.AsyncApi.ASYNC_API_V2);
        asyncApiDocument.setInfo(asyncApiDocument.createInfo());
        asyncApiDocument.getInfo().setTitle(api.getId().getName());
        asyncApiDocument.getInfo().setVersion(api.getId().getVersion());
        if (!APISpecParserConstants.API_TYPE_WEBSUB.equals(api.getType())) {
            JsonObject endpointConfig = JsonParser.parseString(api.getEndpointConfig()).getAsJsonObject();

            AsyncApiServers servers = asyncApiDocument.createServers();
            if (endpointConfig.has(APISpecParserConstants.ENDPOINT_PRODUCTION_ENDPOINTS)) {
                AsyncApiServer prodServer = getAaiServer(api, endpointConfig,
                        APISpecParserConstants.ENDPOINT_PRODUCTION_ENDPOINTS, servers);
                servers.addItem(APISpecParserConstants.GATEWAY_ENV_TYPE_PRODUCTION, prodServer);
            }
            if (endpointConfig.has(APISpecParserConstants.ENDPOINT_SANDBOX_ENDPOINTS)) {
                AsyncApiServer sandboxServer = getAaiServer(api, endpointConfig,
                        APISpecParserConstants.ENDPOINT_SANDBOX_ENDPOINTS, servers);

                servers.addItem(APISpecParserConstants.GATEWAY_ENV_TYPE_SANDBOX, sandboxServer);
            }
            asyncApiDocument.setServers(servers);
        }

        AsyncApiChannels apiChannels = asyncApiDocument.createChannels();
        MappedNode channels = (MappedNode) apiChannels;
        for (URITemplate uriTemplate : api.getUriTemplates()) {
            AsyncApiChannelItem channelItem = AsyncApiParserUtil.createChannelItem(apiChannels);
            AsyncApiOperation subscribeOp = channelItem.createOperation();
            channelItem.setSubscribe(subscribeOp);
            if (APISpecParserConstants.API_TYPE_WS.equals(api.getType())) {
                AsyncApiOperation publishOp = channelItem.createOperation();
                channelItem.setPublish(publishOp);
            }
            channels.addItem(uriTemplate.getUriTemplate(), channelItem);
        }
        asyncApiDocument.setChannels((AsyncApiChannels) channels);
        return Library.writeDocumentToJSONString(asyncApiDocument);
    }

    /**
     * Configure Async API server from endpoint configurations
     *
     * @param api               API
     * @param endpointConfig    Endpoint configuration
     * @param endpoint          Endpoint to be configured
     * @return Configured AaiServer
     */
    private AsyncApiServer getAaiServer(API api, JsonObject endpointConfig, String endpoint, AsyncApiServers servers)
            throws APIManagementException {

        JsonObject endpointObj = endpointConfig.getAsJsonObject(endpoint);
        if (!endpointObj.has(APISpecParserConstants.API_DATA_URL)) {
            throw new APIManagementException(
                    "Missing or Invalid API_DATA_URL in endpoint config: " + endpoint
            );
        }

        String url = endpointObj.get(APISpecParserConstants.API_DATA_URL).getAsString();
        AsyncApiServer server = servers.createServer();
        AsyncApiParserUtil.setAsyncApiServer(url, server);
        server.setProtocol(api.getType().toLowerCase());
        return server;
    }


    /**
     * Update AsyncAPI definition for store
     *
     * @param api            API
     * @param asyncAPIDefinition  AsyncAPI definition
     * @param hostsWithSchemes host addresses with protocol mapping
     * @return AsyncAPI definition
     * @throws APIManagementException throws if an error occurred
     */
    @Override
    public String getAsyncApiDefinitionForStore(API api, String asyncAPIDefinition,
                                                Map<String, String> hostsWithSchemes) throws APIManagementException {
        AsyncApiDocument asyncApiDocument = (AsyncApiDocument) Library.readDocumentFromJSONString(asyncAPIDefinition);
        String channelName = api.getContext();
        String transports = api.getTransports();

        String url = StringUtils.EMPTY;
        String[] apiTransports = transports.split(",");
        if (ArrayUtils.contains(apiTransports, APISpecParserConstants.WSS_PROTOCOL)
                && hostsWithSchemes.get(APISpecParserConstants.WSS_PROTOCOL) != null) {
            url = hostsWithSchemes.get(APISpecParserConstants.WSS_PROTOCOL).trim()
                    .replace(APISpecParserConstants.WSS_PROTOCOL_URL_PREFIX, "");
        }
        if (ArrayUtils.contains(apiTransports, APISpecParserConstants.WS_PROTOCOL)
                && hostsWithSchemes.get(APISpecParserConstants.WS_PROTOCOL) != null) {
            if (StringUtils.isEmpty(url)) {
                url = hostsWithSchemes.get(APISpecParserConstants.WS_PROTOCOL).trim()
                        .replace(APISpecParserConstants.WS_PROTOCOL_URL_PREFIX, "");
            }
        }

        AsyncApiServer server = asyncApiDocument.getServers().getItems().get(0);
        AsyncApiParserUtil.setAsyncApiServer(url, server);
        AsyncApiChannels apiChannels = asyncApiDocument.getChannels();
        MappedNode channels = (MappedNode) apiChannels;
        AsyncApiChannelItem channelDetails = null;
        for (Object x : channels.getItemNames()) {
            channelDetails = (AsyncApiChannelItem) channels.getItem((String) x);
            channels.removeItem((String) x);
        }
        if (channelDetails == null) {
            throw new APIManagementException("No channel details found in AsyncAPI definition",
                    ExceptionCodes.ERROR_READING_ASYNCAPI_SPECIFICATION);
        }
        channels.addItem(channelName, channelDetails);
        asyncApiDocument.setChannels((AsyncApiChannels) channels);

        return Library.writeDocumentToJSONString(asyncApiDocument);
    }

    @Override
    public String updateAsyncAPIDefinition(String oldDefinition, API apiToUpdate) throws APIManagementException {
        AsyncApiDocument asyncApiDocument = (AsyncApiDocument) Library.readDocumentFromJSONString(oldDefinition);
        if (asyncApiDocument.getComponents() == null) {
            asyncApiDocument.setComponents(asyncApiDocument.createComponents());
        }

        AsyncApiSecurityScheme oauth2SecurityScheme = (AsyncApiSecurityScheme) asyncApiDocument.getComponents().
                createSecurityScheme();
        oauth2SecurityScheme.setType(APISpecParserConstants.DEFAULT_API_SECURITY_OAUTH2);

        if (oauth2SecurityScheme.getFlows() == null) {
            oauth2SecurityScheme.setFlows(oauth2SecurityScheme.createOAuthFlows());
        }
        if (oauth2SecurityScheme.getFlows().getImplicit() == null) {
            oauth2SecurityScheme.getFlows().setImplicit(oauth2SecurityScheme.getFlows().createOAuthFlow());
        }
        oauth2SecurityScheme.getFlows().getImplicit().setAuthorizationUrl("http://localhost:9999");

        Map<String, String> scopes = new HashMap<>();
        Map<String, String> scopeBindings = new HashMap<>();

        Iterator<Scope> iterator = apiToUpdate.getScopes().iterator();
        while (iterator.hasNext()) {
            Scope scope = iterator.next();
            scopes.put(scope.getName(), scope.getDescription());
            scopeBindings.put(scope.getName(), scope.getRoles());
        }
        AsyncApiParserUtil.setAsyncApiOAuthFlowsScopes(oauth2SecurityScheme, scopes, scopeBindings);
        asyncApiDocument.getComponents().addSecurityScheme(APISpecParserConstants.DEFAULT_API_SECURITY_OAUTH2,
                oauth2SecurityScheme);

        String endpointConfigString = apiToUpdate.getEndpointConfig();
        if (StringUtils.isNotEmpty(endpointConfigString)) {
            JsonObject endpointConfig = JsonParser.parseString(endpointConfigString).getAsJsonObject();

            AsyncApiServers servers = asyncApiDocument.createServers();
            if (endpointConfig.has(APISpecParserConstants.ENDPOINT_PRODUCTION_ENDPOINTS)) {
                AsyncApiServer prodServer = getAaiServer(apiToUpdate, endpointConfig,
                        APISpecParserConstants.ENDPOINT_PRODUCTION_ENDPOINTS, servers);
                servers.addItem(APISpecParserConstants.GATEWAY_ENV_TYPE_PRODUCTION, prodServer);
            }
            if (endpointConfig.has(APISpecParserConstants.ENDPOINT_SANDBOX_ENDPOINTS)) {
                AsyncApiServer sandboxServer = getAaiServer(apiToUpdate, endpointConfig,
                        APISpecParserConstants.ENDPOINT_SANDBOX_ENDPOINTS, servers);
                servers.addItem(APISpecParserConstants.GATEWAY_ENV_TYPE_SANDBOX, sandboxServer);
            }
            asyncApiDocument.setServers(servers);
        }
        return Library.writeDocumentToJSONString(asyncApiDocument);
    }

    @Override
    public Map<String,String> buildWSUriMapping(String apiDefinition) {
        Map<String,String> wsUriMapping = new HashMap<>();
        AsyncApiDocument asyncApiDocument = (AsyncApiDocument) Library.readDocumentFromJSONString(apiDefinition);
        AsyncApiChannels apiChannels = asyncApiDocument.getChannels();
        MappedNode channels = (MappedNode) apiChannels;
        if (channels != null && !channels.getItems().isEmpty()) {
            for (Object entry : channels.getItemNames()) {
                AsyncApiChannelItem channel = (AsyncApiChannelItem) channels.getItem((String) entry);
                AsyncApiOperation publishOperation = channel.getPublish();
                if (publishOperation != null) {
                    AsyncApiExtensible publishExtensibleOperation = (AsyncApiExtensible) publishOperation;
                    Map<String, JsonNode> publishExtensions = publishExtensibleOperation.getExtensions();
                    if (publishExtensions != null) {
                        JsonNode xUriMapping = publishExtensions.get(APISpecParserConstants.ASYNCAPI_URI_MAPPING);
                        if (xUriMapping != null) {
                            wsUriMapping.put(APISpecParserConstants.WS_URI_MAPPING_PUBLISH + entry, xUriMapping.asText());
                        }
                    }
                }
                AsyncApiOperation subscribeOperation = channel.getSubscribe();
                if (subscribeOperation != null) {
                    AsyncApiExtensible subscribeExtensibleOperation = (AsyncApiExtensible) subscribeOperation;
                    Map<String, JsonNode> subscribeExtensions = subscribeExtensibleOperation.getExtensions();
                    if (subscribeExtensions != null) {
                        JsonNode xUriMapping = subscribeExtensions.get(APISpecParserConstants.ASYNCAPI_URI_MAPPING);
                        if (xUriMapping != null) {
                            wsUriMapping.put(APISpecParserConstants.WS_URI_MAPPING_SUBSCRIBE + entry, xUriMapping.asText());
                        }
                    }
                }
            }
        }
        return wsUriMapping;
    }
}

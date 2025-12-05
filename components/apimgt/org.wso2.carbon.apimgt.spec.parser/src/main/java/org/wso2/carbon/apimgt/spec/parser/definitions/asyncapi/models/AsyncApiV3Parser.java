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
import io.apicurio.datamodels.models.asyncapi.AsyncApiDocument;
import io.apicurio.datamodels.models.asyncapi.AsyncApiExtensible;
import io.apicurio.datamodels.models.asyncapi.AsyncApiOperation;
import io.apicurio.datamodels.models.asyncapi.AsyncApiSecurityScheme;
import io.apicurio.datamodels.models.asyncapi.AsyncApiServer;
import io.apicurio.datamodels.models.asyncapi.AsyncApiServers;
import io.apicurio.datamodels.models.asyncapi.v30.AsyncApi30Channel;
import io.apicurio.datamodels.models.asyncapi.v30.AsyncApi30Channels;
import io.apicurio.datamodels.models.asyncapi.v30.AsyncApi30Components;
import io.apicurio.datamodels.models.asyncapi.v30.AsyncApi30Document;
import io.apicurio.datamodels.models.asyncapi.v30.AsyncApi30Operation;
import io.apicurio.datamodels.models.asyncapi.v30.AsyncApi30Operations;
import io.apicurio.datamodels.models.asyncapi.v30.AsyncApi30Reference;
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
import org.wso2.carbon.apimgt.spec.parser.definitions.AbstractAsyncApiParser;
import org.wso2.carbon.apimgt.spec.parser.definitions.AsyncApiParserUtil;
import org.wso2.carbon.apimgt.spec.parser.definitions.asyncapi.AsyncApiV3ParserUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * AsyncAPI v3 parser responsible for processing AsyncAPI 3.0.x specifications.
 * This class extends the AbstractAsyncApiParser class and
 * provides version-specific parsing capabilities for AsyncAPI 3.0 definitions.
 */
public class AsyncApiV3Parser extends AbstractAsyncApiParser {

    private static final Log log = LogFactory.getLog(AsyncApiV3Parser.class);

    /**
     * Extracts URI templates from the given AsyncAPI definition.
     *
     * @param apiDefinition   the AsyncAPI definition as a JSON string
     * @param includePublish  whether to include operations when generating URI templates
     * @return a set of generated URITemplate objects for the defined channels
     * @throws APIManagementException if an error occurs while parsing the AsyncAPI definition
     */
    @Override
    public Set<URITemplate> getURITemplates(String apiDefinition, boolean includePublish)
            throws APIManagementException {
        AsyncApi30Document document = (AsyncApi30Document) Library.readDocumentFromJSONString(apiDefinition);
        Set<Scope> scopes = getScopes(apiDefinition);
        Set<URITemplate> uriTemplates = new HashSet<>();

        AsyncApi30Channels channelsContainer = (AsyncApi30Channels) document.getChannels();
        AsyncApi30Operations operationsContainer = document.getOperations();

        Map<String, AsyncApi30Channel> channels = AsyncApiV3ParserUtil.toMap(channelsContainer);
        Map<String, AsyncApi30Operation> operations = AsyncApiV3ParserUtil.toMap(operationsContainer);

        for (Map.Entry<String, AsyncApi30Operation> entry : operations.entrySet()) {
            AsyncApi30Operation operation = entry.getValue();

            if (operation == null || operation.getChannel() == null) continue;

            String action = operation.getAction();
            AsyncApi30Reference channelRef = operation.getChannel();
            String channelName = AsyncApiV3ParserUtil.extractChannelNameFromRef(channelRef.get$ref());
            if (StringUtils.isEmpty(channelName)) continue;

            AsyncApi30Channel channel = channels.get(channelName);
            if (channel == null) continue;

            if (includePublish && APISpecParserConstants.ASYNCAPI_ACTION_SEND.equalsIgnoreCase(action)) {
                uriTemplates.add(buildURITemplate(
                        channelName,
                        APISpecParserConstants.HTTP_VERB_PUBLISH,
                        operation,
                        scopes,
                        channel
                ));
            } else if (APISpecParserConstants.ASYNCAPI_ACTION_RECEIVE.equalsIgnoreCase(action)) {
                uriTemplates.add(buildURITemplate(
                        channelName,
                        APISpecParserConstants.HTTP_VERB_SUBSCRIBE,
                        operation,
                        scopes,
                        channel
                ));
            }
        }

        return uriTemplates;
    }

    /**
     * Builds an URITemplate object for a given AsyncAPI channel operation.
     *
     * @param target   the URI template path for the operation
     * @param verb     the HTTP verb (e.g., publish or subscribe) for the operation
     * @param operation the AsyncAPI operation object
     * @param scopes    the set of available scopes to be applied to the template
     * @param channel   the AsyncAPI channel containing the operation and potential extensions
     * @return a fully constructed URITemplate
     * @throws APIManagementException if any operation scope cannot be resolved
     */
    private URITemplate buildURITemplate(String target, String verb, AsyncApiOperation operation, Set<Scope> scopes,
                                         AsyncApi30Channel channel) throws APIManagementException {
        URITemplate template = new URITemplate();
        template.setHTTPVerb(verb);
        template.setHttpVerbs(verb);
        template.setUriTemplate(target);

        AsyncApiExtensible asyncApiExtensibleChannel = channel;
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

    /**
     * Extracts the scopes defined in the extensions of an AsyncAPI operation.

     * @param operation the AsyncAPI operation containing possible scope extensions
     * @return a list of scope names defined in the operation extensions
     */
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

    /**
     * Extracts and returns the set of scopes defined in the given AsyncAPI configuration.
     *
     * @param resourceConfigsJSON the AsyncAPI configuration in JSON format
     * @return a set of Scope bjects defined in the configuration
     * @throws APIManagementException if an error occurs while extracting the scopes
     */
    @Override
    public Set<Scope> getScopes(String resourceConfigsJSON) throws APIManagementException {
        Set<Scope> scopeSet = AsyncApiParserUtil.getScopesFromAsyncAPIConfig(resourceConfigsJSON);
        return scopeSet;
    }

    /**
     * Validates the given AsyncAPI definition.
     *
     * @param apiDefinition      the AsyncAPI definition to be validated, in JSON format
     * @param returnJsonContent  whether the validated content should be returned in JSON format
     * @return an APIDefinitionValidationResponse containing validation results and messages
     * @throws APIManagementException if an error occurs during validation or schema processing
     */
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
            throw new APIManagementException(msg, e, ExceptionCodes.ERROR_READING_ASYNCAPI_SPECIFICATION);
        }

        /**
         * TODO Validation is currently not working since the Apicurio library does not yet include
         * the AsyncAPI v3 model in its ValidationRuleSet. As a result, the validation (problems)
         * returns null. Temporarily overriding the value as true until this is fixed.
         */
        validationSuccess = true;

        // Build the validation response
        if (validationSuccess) {
            AsyncApiDocument asyncApiDocument = (AsyncApiDocument) Library.readDocumentFromJSONString(apiDefinition);
            ArrayList<String> endpoints = new ArrayList<>();
            AsyncApiServers servers = asyncApiDocument.getServers();
            if (servers != null && servers.getItems() != null && !servers.getItems().isEmpty()) {
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
        } else if (validationErrorMessages != null) {
            validationResponse.setValid(false);
            for (String errorMessage : validationErrorMessages) {
                AsyncApiParserUtil.addErrorToValidationResponse(validationResponse, errorMessage);
            }
        }
        return validationResponse;
    }

    /**
     * Generates an AsyncAPI definition for the given API instance.
     *
     * @param api the API object from which the AsyncAPI definition will be generated
     * @return the generated AsyncAPI definition as a String
     * @throws APIManagementException if an error occurs while generating the AsyncAPI definition
     */
    @Override
    public String generateAsyncAPIDefinition(API api) throws APIManagementException {
        // Create an empty AsyncApiDocument v3
        AsyncApi30Document document = (AsyncApi30Document) AsyncApiParserUtil.createAsyncApiDocument(
                APISpecParserConstants.AsyncApi.ASYNC_API_V30);
        document.setAsyncapi(APISpecParserConstants.AsyncApi.ASYNC_API_V3);
        document.setInfo(document.createInfo());
        document.getInfo().setTitle(api.getId().getName());
        document.getInfo().setVersion(api.getId().getVersion());
        // Servers (top-level)
        if (!APISpecParserConstants.API_TYPE_WEBSUB.equals(api.getType())) {
            JsonObject endpointConfig = JsonParser.parseString(api.getEndpointConfig()).getAsJsonObject();
            AsyncApiServers servers = document.createServers();
            if (endpointConfig.has(APISpecParserConstants.ENDPOINT_PRODUCTION_ENDPOINTS)) {
                JsonObject prodObj = endpointConfig
                        .get(APISpecParserConstants.ENDPOINT_PRODUCTION_ENDPOINTS)
                        .getAsJsonObject();
                String url = prodObj.get(APISpecParserConstants.API_DATA_URL).getAsString();
                AsyncApiServer prodServer = servers.createServer();
                AsyncApiV3ParserUtil.setAsyncApiServerFromUrl(url, prodServer, api.getType());
                servers.addItem(APISpecParserConstants.GATEWAY_ENV_TYPE_PRODUCTION, prodServer);
            }
            if (endpointConfig.has(APISpecParserConstants.ENDPOINT_SANDBOX_ENDPOINTS)) {
                JsonObject sandboxObj = endpointConfig
                        .get(APISpecParserConstants.ENDPOINT_SANDBOX_ENDPOINTS)
                        .getAsJsonObject();
                String url = sandboxObj.get(APISpecParserConstants.API_DATA_URL).getAsString();
                AsyncApiServer sandboxServer = servers.createServer();
                AsyncApiV3ParserUtil.setAsyncApiServerFromUrl(url, sandboxServer, api.getType());
                servers.addItem(APISpecParserConstants.GATEWAY_ENV_TYPE_SANDBOX, sandboxServer);
            }
            document.setServers(servers);
        }
        // Components, Channels and Operations (top-level)
        if (document.getComponents() == null) {
            document.setComponents(document.createComponents());
        }
        AsyncApi30Components components = (AsyncApi30Components) document.getComponents();
        AsyncApi30Channels channels = (AsyncApi30Channels) document.createChannels();
        AsyncApi30Operations operations = document.createOperations();
        for (URITemplate uriTemplate : api.getUriTemplates()) {
            String channelName = uriTemplate.getUriTemplate();
            AsyncApi30Channel channel = components.createChannel();
            channel.setAddress(channelName);
            // The below normalizeChannelName will not be required if the publisher does not add "/" to every new topic
            channels.addItem(AsyncApiV3ParserUtil.normalizeChannelName(channelName), channel);
            AsyncApi30Operation receiveOp = operations.createOperation();
            receiveOp.setAction(APISpecParserConstants.ASYNCAPI_ACTION_RECEIVE);
            AsyncApi30Reference recvRef = receiveOp.createReference();
            recvRef.set$ref(APISpecParserConstants.ASYNCAPI_CHANNELS_PATH +
                    AsyncApiV3ParserUtil.normalizeChannelName(channelName));
            receiveOp.setChannel(recvRef);
            String recvOpName = APISpecParserConstants.ASYNCAPI_ACTION_RECEIVE_OPS + channelName;
            operations.addItem(recvOpName, receiveOp);
            if (APISpecParserConstants.API_TYPE_WS.equals(api.getType())) {
                AsyncApi30Operation sendOp = operations.createOperation();
                sendOp.setAction(APISpecParserConstants.ASYNCAPI_ACTION_SEND);

                AsyncApi30Reference sendRef = sendOp.createReference();
                sendRef.set$ref(APISpecParserConstants.ASYNCAPI_CHANNELS_PATH +
                        AsyncApiV3ParserUtil.normalizeChannelName(channelName));
                sendOp.setChannel(sendRef);
                String sendOpName = APISpecParserConstants.ASYNCAPI_ACTION_SEND_OPS + channelName;
                operations.addItem(sendOpName, sendOp);
            }
        }
        document.setChannels(channels);
        document.setOperations(operations);
        return Library.writeDocumentToJSONString(document);
    }

    /**
     * Configure Async API server from endpoint configurations.
     *
     * @param api              API
     * @param endpointConfig   Endpoint configuration
     * @param endpoint         Endpoint to be configured
     * @return Configured AaiServer
     */
    private AsyncApiServer getAaiServer(API api, JsonObject endpointConfig, String endpoint, AsyncApiServers servers)
            throws APIManagementException {

        AsyncApiServer server = AsyncApiParserUtil.getAsyncAPIServer(api, endpointConfig, endpoint, servers);
        return server;
    }


    /**
     * Update AsyncAPI definition for store.
     *
     * @param api                API
     * @param asyncAPIDefinition AsyncAPI definition
     * @param hostsWithSchemes   host addresses with protocol mapping
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

    /**
     * Updates an existing AsyncAPI definition with the latest API configuration details.
     *
     * @param oldDefinition the existing AsyncAPI definition as a JSON string
     * @param apiToUpdate    the API containing updated configuration details
     * @return the updated AsyncAPI definition as a String
     * @throws APIManagementException if an error occurs while reading or updating the definition
     */
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

    /**
     * Builds a WebSocket URI mapping from the given AsyncAPI definition.
     *
     * @param apiDefinition the AsyncAPI definition in JSON format
     * @return a map of channel names to WebSocket URIs
     */
    @Override
    public Map<String, String> buildWSUriMapping(String apiDefinition) {
        Map<String, String> wsUriMapping = new HashMap<>();
        AsyncApi30Document document = (AsyncApi30Document) Library.readDocumentFromJSONString(apiDefinition);

        AsyncApi30Channels channelsContainer = (AsyncApi30Channels) document.getChannels();
        Map<String, AsyncApi30Channel> channels = new HashMap<>();
        if (channelsContainer != null) {
            for (String name : channelsContainer.getItemNames()) {
                channels.put(name, channelsContainer.getItem(name));
            }
        }

        AsyncApi30Operations opsContainer = document.getOperations();
        if (opsContainer == null || opsContainer.getItems().isEmpty()) {
            return wsUriMapping;
        }

        Map<String, AsyncApi30Operation> operations = new HashMap<>();
        for (String name : opsContainer.getItemNames()) {
            operations.put(name, opsContainer.getItem(name));
        }

        for (Map.Entry<String, AsyncApi30Operation> entry : operations.entrySet()) {
            AsyncApi30Operation operation = entry.getValue();
            if (operation == null) continue;

            // action = send|receive
            String action = operation.getAction();
            if (action == null) continue;

            // Channel from $ref
            AsyncApi30Reference ref = operation.getChannel();
            if (ref == null || ref.get$ref() == null) continue;

            String channelName = AsyncApiV3ParserUtil.extractChannelNameFromRef(ref.get$ref());
            if (channelName == null) continue;

            Map<String, JsonNode> extensions = operation.getExtensions();
            if (extensions == null) continue;

            JsonNode mappingNode = extensions.get(APISpecParserConstants.ASYNCAPI_URI_MAPPING);
            if (mappingNode == null || !mappingNode.isTextual()) continue;

            String mappingValue = mappingNode.asText();

            if (APISpecParserConstants.ASYNCAPI_ACTION_SEND.equalsIgnoreCase(action)) {
                wsUriMapping.put(APISpecParserConstants.WS_URI_MAPPING_PUBLISH + channelName, mappingValue);
            } else if (APISpecParserConstants.ASYNCAPI_ACTION_RECEIVE.equalsIgnoreCase(action)) {
                wsUriMapping.put(APISpecParserConstants.WS_URI_MAPPING_SUBSCRIBE + channelName, mappingValue);
            }
        }
        return wsUriMapping;
    }

}

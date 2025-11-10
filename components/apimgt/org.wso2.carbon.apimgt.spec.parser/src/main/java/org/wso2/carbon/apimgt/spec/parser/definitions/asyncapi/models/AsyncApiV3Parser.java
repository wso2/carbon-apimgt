package org.wso2.carbon.apimgt.spec.parser.definitions.asyncapi.models;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.apicurio.datamodels.Library;
import io.apicurio.datamodels.models.Document;
import io.apicurio.datamodels.models.MappedNode;
import io.apicurio.datamodels.models.asyncapi.*;
import io.apicurio.datamodels.models.asyncapi.v30.*;
import io.apicurio.datamodels.models.util.JsonUtil;
import io.apicurio.datamodels.validation.*;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIDefinitionValidationResponse;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.spec.parser.definitions.APISpecParserConstants;
import org.wso2.carbon.apimgt.spec.parser.definitions.AsyncApiParser;
import org.wso2.carbon.apimgt.spec.parser.definitions.AsyncApiParserUtil;

import java.util.*;

/**
 * This class is used to parse AsyncAPI 3.0.x specifications.
 * It extends the AsyncApiParser class to provide specific parsing capabilities for AsyncAPI 3.0.0.
 */
@Component(
        name = "wso2.async.300.definition.parser.component",
        immediate = true,
        service = APIDefinition.class
)
public class AsyncApiV3Parser extends AsyncApiParser {

    private static final Log log = LogFactory.getLog(AsyncApiParserUtil.class);

    @Override
    public Set<URITemplate> getURITemplates(String apiDefinition, boolean includePublish)
            throws APIManagementException {
        // parse once and gather scopes
        JsonNode root = AsyncApiV3ParserUtil.parseToJsonNode(apiDefinition);
        Set<Scope> scopes = getScopes(apiDefinition);
        Set<URITemplate> uriTemplates = new LinkedHashSet<>();

        // get channels and operations
        Map<String, JsonNode> channels = AsyncApiV3ParserUtil.getMap(root, "components", "channels");
        if (channels.isEmpty()) {
            channels = AsyncApiV3ParserUtil.getMap(root, null, "channels");
        }
        Map<String, JsonNode> operations = AsyncApiV3ParserUtil.getMap(root, "components", "operations");
        if (operations.isEmpty()) {
            operations = AsyncApiV3ParserUtil.getMap(root, null, "operations");
        }

        if (!operations.isEmpty()) {
            // operations reference channels via $ref
            for (Map.Entry<String, JsonNode> opEntry : operations.entrySet()) {
                JsonNode opNode = opEntry.getValue();
                if (opNode == null || opNode.isNull()) {
                    continue;
                }
                String action = AsyncApiV3ParserUtil.textOrNull(opNode, "action");
                String channelRef = null;
                JsonNode channelNode = opNode.path("channel");
                if (channelNode.isTextual()) {
                    channelRef = channelNode.asText();
                } else if (channelNode.isObject()) {
                    channelRef = AsyncApiV3ParserUtil.textOrNull(channelNode, "$ref");
                }
                if (StringUtils.isBlank(channelRef)) {
                    continue;
                }
                String channelName = AsyncApiV3ParserUtil.extractChannelNameFromRef(channelRef);
                if (StringUtils.isBlank(channelName)) {
                    continue;
                }
                JsonNode channel = channels.get(channelName);
                if (channel == null) {
                    continue;
                }
                try {
                    if ("send".equalsIgnoreCase(action) && includePublish) {
                        uriTemplates.add(AsyncApiV3ParserUtil.buildURITemplate(channelName, APISpecParserConstants.HTTP_VERB_PUBLISH, opNode, channel, scopes));
                    } else if ("receive".equalsIgnoreCase(action)) {
                        uriTemplates.add(AsyncApiV3ParserUtil.buildURITemplate(channelName, APISpecParserConstants.HTTP_VERB_SUBSCRIBE, opNode, channel, scopes));
                    }
                } catch (APIManagementException e) {
                    log.warn("[AsyncApiV3Parser] skipped operation '" + opEntry.getKey() + "': " + e.getMessage());
                }
            }
        } else {
            // fallback to inline publish/subscribe inside channels
            for (Map.Entry<String, JsonNode> chEntry : channels.entrySet()) {
                String channelName = chEntry.getKey();
                JsonNode channelNode = chEntry.getValue();
                if (channelNode == null || channelNode.isNull()) {
                    continue;
                }
                try {
                    JsonNode publish = channelNode.path("publish");
                    if (includePublish && !publish.isMissingNode() && !publish.isNull()) {
                        uriTemplates.add(AsyncApiV3ParserUtil.buildURITemplate(channelName, APISpecParserConstants.HTTP_VERB_PUBLISH, publish, channelNode, scopes));
                    }
                } catch (APIManagementException e) {
                    log.warn("[AsyncApiV3Parser] skipped publish template for channel '" + channelName + "': " + e.getMessage());
                }
                try {
                    JsonNode subscribe = channelNode.path("subscribe");
                    if (!subscribe.isMissingNode() && !subscribe.isNull()) {
                        uriTemplates.add(AsyncApiV3ParserUtil.buildURITemplate(channelName, APISpecParserConstants.HTTP_VERB_SUBSCRIBE, subscribe, channelNode, scopes));
                    }
                } catch (APIManagementException e) {
                    log.warn("[AsyncApiV3Parser] skipped subscribe template for channel '" + channelName + "': " + e.getMessage());
                }
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("[AsyncApiV3Parser] getURITemplates produced " + uriTemplates.size() + " templates.");
        }
        return uriTemplates;
    }

    /**
     * This method might no long required - Need to confirm
     * This method was created to explicitly define the endpoints with topic/channels
     */
    public Map<String, Map<String, String>> getTopicMappings(String apiDefinition) throws APIManagementException {
        Map<String, Map<String, String>> topicMappings = new HashMap<>();

        if (apiDefinition == null || apiDefinition.trim().isEmpty()) {
            throw new APIManagementException("AsyncAPI definition is empty or null.");
        }

        AsyncApi30Document document = (AsyncApi30Document) Library.readDocumentFromJSONString(apiDefinition);
        if (document == null) {
            throw new APIManagementException("Failed to parse AsyncAPI document.");
        }

        AsyncApi30Components components = (AsyncApi30Components) document.getComponents();
        Map<String, AsyncApi30Channel> channels = null;
        Map<String, AsyncApi30Server> servers = null;

        if (components != null && components.getChannels() != null) {
            channels = components.getChannels();
        }

        if (components != null && components.getServers() != null) {
            servers = components.getServers();
        }

        if (channels != null) {
            for (Map.Entry<String, AsyncApi30Channel> channelEntry : channels.entrySet()) {
                String channelName = channelEntry.getKey();
                Map<String, String> endpoints = new HashMap<>();

                if (servers != null) {
                    for (Map.Entry<String, AsyncApi30Server> serverEntry : servers.entrySet()) {
                        String serverKey = serverEntry.getKey(); // e.g., "production" or "sandbox"
                        AsyncApi30Server server = serverEntry.getValue();

                        if (server != null) {
                            String protocol = server.getProtocol();
                            String host = server.getHost();
                            String pathname = server.getPathname();
                            String url = buildUrl(protocol, host, pathname);

                            if ("sandbox".equalsIgnoreCase(serverKey)) {
                                endpoints.put("sandbox", url);
                            } else {
                                endpoints.put("production", url);
                            }
                        }
                    }
                }

                if (endpoints.isEmpty()) {
                    endpoints.put("production", "");
                }

                topicMappings.put(channelName, endpoints);
            }
        }

        return topicMappings;
    }

    /**
     * Builds a full URL string from AsyncAPI v3 Server components.
     */
    private String buildUrl(String protocol, String host, String pathname) {
        StringBuilder sb = new StringBuilder();
        if (protocol != null && !protocol.isEmpty()) {
            sb.append(protocol).append("://");
        }
        if (host != null && !host.isEmpty()) {
            sb.append(host);
        }
        if (pathname != null && !pathname.isEmpty()) {
            if (!pathname.startsWith("/")) {
                sb.append("/");
            }
            sb.append(pathname);
        }
        return sb.toString();
    }


    private List<String> getScopeOfOperations(AsyncApiOperation operation) {
        return getScopeOfOperationsFromExtensions(operation);
    }

    private List<String> getScopeOfOperationsFromExtensions(AsyncApiOperation operation) {
        AsyncApiExtensible asyncApiExtensibleOperation = (AsyncApiExtensible) operation;
        Map<String, JsonNode> extensions = asyncApiExtensibleOperation.getExtensions();
        if (extensions != null) {
            JsonNode scopeBindings = extensions.get("x-scopes");
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
            AsyncApiSecurityScheme oauth2 = (AsyncApiSecurityScheme) components.getSecuritySchemes().get("oauth2");
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
    public APIDefinitionValidationResponse validateAPIDefinition(String apiDefinition, boolean returnJsonContent) throws APIManagementException {

        APIDefinitionValidationResponse validationResponse = new APIDefinitionValidationResponse();
        String protocol = StringUtils.EMPTY;
        boolean validationSuccess = false;
        List<String> validationErrorMessages = new ArrayList<>();

        try {
            // Parse the incoming JSON into a Jackson ObjectNode
            ObjectNode originalParsed;
            try {
                originalParsed = (ObjectNode) JsonUtil.parseJSON(apiDefinition);
            } catch (Exception e) {
                String msg = "Invalid AsyncAPI syntax: " + e.getMessage();
                log.error(msg, e);
                // treat parse failures as validation failures
                validationSuccess = false;
                validationErrorMessages.add(msg);
                // build response below
                originalParsed = null;
            }


            //If parsing succeeded, create Apicurio Document and run model validation
            if (originalParsed != null) {
                Document doc = Library.readDocument(originalParsed);
                if (doc == null || doc.root() == null) {
                    String msg = "Unable to parse AsyncAPI definition into Apicurio Document model.";
                    log.error(msg);
                    validationSuccess = false;
                    validationErrorMessages.add(msg);
                } else {
                    // Log model type (ModelType has a useful toString)
                    log.debug("[AsyncAPI][validateAPIDefinition] Parsed model type: " + doc.root().modelType());

                    // Validate using Apicurio validation rules
                    IValidationSeverityRegistry severityRegistry = new DefaultSeverityRegistry();
                    List<io.apicurio.datamodels.validation.ValidationProblem> problems =
                            Library.validate(doc, severityRegistry);

                    if (problems != null && !problems.isEmpty()) {
                        // Format problems into a readable string
                        String formatted = formatProblems(problems);
                        log.debug("[AsyncAPI][validateAPIDefinition] Validation Problems:\n" + formatted);

                        // Determine if there are any ERROR-level problems
                        // This can also include ValidationProblemSeverity low as well
                        boolean hasErrors = problems.stream()
                                .anyMatch(p -> p.severity == ValidationProblemSeverity.high
                                        || p.severity == ValidationProblemSeverity.medium);

                        if (hasErrors) {
                            validationSuccess = false;
                        }

                        // Collect all problem messages into validationErrorMessages
                        for (ValidationProblem problem : problems) {
                            validationErrorMessages.add(problem.message);
                        }
                    } else {
                        log.debug("[AsyncAPI][validateAPIDefinition] No validation problems found.");
                    }
                }
            }

        } catch (Exception e) {
            // unexpected problems during validation/parsing
            String msg = "Error occurred while validating AsyncAPI definition: " + e.getMessage();
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        }

        // TODO: Validation is currently not working since the Apicurio library does not yet include
        //  the AsyncAPI v3 model in its ValidationRuleSet. As a result, the validation (problems)
        //  returns null. Temporarily overriding the value as true until this is fixed.
        validationSuccess = true;

        // Build the validation response
        if (validationSuccess) {
            AsyncApiDocument asyncApiDocument = (AsyncApiDocument) Library.readDocumentFromJSONString(apiDefinition);
            ArrayList<String> endpoints = new ArrayList<>();
            AsyncApiServers servers = (AsyncApiServers) asyncApiDocument.getServers();
            if (servers != null && servers.getItems() != null && !servers.getItems().isEmpty() &&
                    servers.getItems().size() == 1) {
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
            if (validationErrorMessages != null) {
                validationResponse.setValid(false);
                for (String errorMessage : validationErrorMessages) {
                    AsyncApiParserUtil.addErrorToValidationResponse(validationResponse, errorMessage);
                }
            }
        }
        return validationResponse;
    }

    /**
     * Format the list of problems as a string.
     * @param problems Validation problems
     */
    protected String formatProblems(List<ValidationProblem> problems) {
        StringBuilder builder = new StringBuilder();
        problems.forEach(problem -> {
            builder.append("[");
            builder.append(problem.errorCode);
            builder.append("] |");
            builder.append(problem.severity);
            builder.append("| {");
            builder.append(problem.nodePath.toString(true));
            builder.append("->");
            builder.append(problem.property);
            builder.append("} :: ");
            builder.append(problem.message);
            builder.append("\n");
        });
        return builder.toString();
    }

    @Override
    public String generateAsyncAPIDefinition(API api) throws APIManagementException {
        // Create an empty AsyncApiDocument and populate basic info
        AsyncApiDocument asyncApiDocument = AsyncApiParserUtil.createAsyncApiDocument(
                APISpecParserConstants.AsyncApi.ASYNC_API_V30);
        asyncApiDocument.setAsyncapi("3.0.0");
        asyncApiDocument.setInfo(asyncApiDocument.createInfo());
        asyncApiDocument.getInfo().setTitle(api.getId().getName());
        asyncApiDocument.getInfo().setVersion(api.getId().getVersion());

        if (!APISpecParserConstants.API_TYPE_WEBSUB.equals(api.getType())) {
            JSONObject endpointConfig = new JSONObject(api.getEndpointConfig());

            AsyncApiServers servers = asyncApiDocument.createServers();
            if (endpointConfig.has(APISpecParserConstants.ENDPOINT_PRODUCTION_ENDPOINTS)) {
                AsyncApiServer prodServer = getAaiServer(api, asyncApiDocument, endpointConfig,
                        APISpecParserConstants.GATEWAY_ENV_TYPE_PRODUCTION,
                        APISpecParserConstants.ENDPOINT_PRODUCTION_ENDPOINTS, servers);
                servers.addItem(APISpecParserConstants.GATEWAY_ENV_TYPE_PRODUCTION, prodServer);
            }
            if (endpointConfig.has(APISpecParserConstants.ENDPOINT_SANDBOX_ENDPOINTS)) {
                AsyncApiServer sandboxServer = getAaiServer(api, asyncApiDocument, endpointConfig,
                        APISpecParserConstants.GATEWAY_ENV_TYPE_SANDBOX,
                        APISpecParserConstants.ENDPOINT_SANDBOX_ENDPOINTS, servers);

                servers.addItem(APISpecParserConstants.GATEWAY_ENV_TYPE_SANDBOX, sandboxServer);
            }
            asyncApiDocument.setServers(servers);
        }

        if (asyncApiDocument.getComponents() == null) {
            asyncApiDocument.setComponents(asyncApiDocument.createComponents());
        }
        AsyncApi30Components components = (AsyncApi30Components) asyncApiDocument.getComponents();

        // Create channels container on the document add channels/operations
        if (asyncApiDocument.getChannels() == null) {
            asyncApiDocument.setChannels(asyncApiDocument.createChannels());
        }
        for (URITemplate uriTemplate : api.getUriTemplates()) {
            AsyncApi30Channel channel = components.createChannel();
            channel.setAddress(uriTemplate.getUriTemplate());
            components.addChannel(uriTemplate.getUriTemplate(), channel);

            AsyncApi30Operation receiveOp = components.createOperation();
            receiveOp.setAction("receive");
            AsyncApi30Reference channelRef = receiveOp.createReference();
            channelRef.set$ref("#/channels/" + uriTemplate.getUriTemplate());
            receiveOp.setChannel(channelRef);
            String receiveOpName = uriTemplate.getUriTemplate() + "_receive";
            components.addOperation(receiveOpName, receiveOp);

            if (APISpecParserConstants.API_TYPE_WS.equals(api.getType())) {
                AsyncApi30Operation sendOp = components.createOperation();
                sendOp.setAction("send");
                AsyncApi30Reference sendRef = sendOp.createReference();
                sendRef.set$ref("#/channels/" + uriTemplate.getUriTemplate());
                sendOp.setChannel(sendRef);
                String sendOpName = uriTemplate.getUriTemplate() + "_send";
                components.addOperation(sendOpName, sendOp);
            }
        }

        Map<String, AsyncApi30Channel> compChannels = null;
        try {
            compChannels = components.getChannels();
        } catch (ClassCastException ignore) {
            compChannels = null;
        }

        if (compChannels != null && !compChannels.isEmpty()) {
            AsyncApi30Channels docChannels = (AsyncApi30Channels) asyncApiDocument.createChannels();

            for (Map.Entry<String, AsyncApi30Channel> entry : compChannels.entrySet()) {
                String name = entry.getKey();
                AsyncApi30Channel compChannel = entry.getValue();
                if (compChannel != null) {
                    docChannels.addItem(name, compChannel);
                }
            }

            asyncApiDocument.setChannels(docChannels);
        } else {
            // If there are no component channels, leave document.channels as-is (or empty)
        }
        return Library.writeDocumentToJSONString(asyncApiDocument);
    }

    /**
     * Configure Async API server from endpoint configurations
     *
     * @param api              API
     * @param asyncApiDocument Async Api Document
     * @param endpointConfig   Endpoint configuration
     * @param serverName       Name of the server
     * @param endpoint         Endpoint to be configured
     * @return Configured AaiServer
     */
    private AsyncApiServer getAaiServer(API api, AsyncApiDocument asyncApiDocument, JSONObject endpointConfig, String serverName,
                                        String endpoint, AsyncApiServers servers) {
        String url = endpointConfig.getJSONObject(endpoint).getString(APISpecParserConstants.API_DATA_URL);
        AsyncApiServer server = (AsyncApiServer) servers.createServer();
        AsyncApiParserUtil.setAsyncApiServer(url, server);
        server.setProtocol(api.getType().toLowerCase());
        return server;
    }


    /**
     * Update AsyncAPI definition for store
     *
     * @param api                API
     * @param asyncAPIDefinition AsyncAPI definition
     * @param hostsWithSchemes   host addresses with protocol mapping
     * @return AsyncAPI definition
     * @throws APIManagementException throws if an error occurred
     */
    @Override
    public String getAsyncApiDefinitionForStore(API api, String asyncAPIDefinition, Map<String, String> hostsWithSchemes)
            throws APIManagementException {
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
        if (ArrayUtils.contains(apiTransports, APISpecParserConstants.WSS_PROTOCOL)
                && hostsWithSchemes.get(APISpecParserConstants.WS_PROTOCOL) != null) {
            if (StringUtils.isEmpty(url)) {
                url = hostsWithSchemes.get(APISpecParserConstants.WS_PROTOCOL).trim()
                        .replace(APISpecParserConstants.WS_PROTOCOL_URL_PREFIX, "");
            }
        }

        AsyncApiServer server = asyncApiDocument.getServers().getItems().get(0);
        AsyncApiParserUtil.setAsyncApiServer(url, server);
        AsyncApiChannels apiChannels = (AsyncApiChannels) asyncApiDocument.getChannels();
        MappedNode channels = (MappedNode) apiChannels;
        AsyncApiChannelItem channelDetails = null;
        for (Object x : channels.getItemNames()) {
            channelDetails = (AsyncApiChannelItem) channels.getItem((String) x);
            channels.removeItem((String) x);
        }
        assert channelDetails != null;
        channels.addItem(channelName, channelDetails);
        asyncApiDocument.setChannels((AsyncApiChannels) channels);

        return Library.writeDocumentToJSONString(asyncApiDocument);
    }

    @Override
    public String updateAsyncAPIDefinition(String oldDefinition, API apiToUpdate) {
        AsyncApiDocument asyncApiDocument = (AsyncApiDocument) Library.readDocumentFromJSONString(oldDefinition);
        if (asyncApiDocument.getComponents() == null) {
            asyncApiDocument.setComponents(asyncApiDocument.createComponents());
        }

        AsyncApiSecurityScheme oauth2SecurityScheme = (AsyncApiSecurityScheme) asyncApiDocument.getComponents().createSecurityScheme();
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

        asyncApiDocument.getComponents().addSecurityScheme(APISpecParserConstants.DEFAULT_API_SECURITY_OAUTH2, oauth2SecurityScheme);
        String endpointConfigString = apiToUpdate.getEndpointConfig();
        if (StringUtils.isNotEmpty(endpointConfigString)) {
            JSONObject endpointConfig = new JSONObject(endpointConfigString);

            AsyncApiServers servers = asyncApiDocument.createServers();
            if (endpointConfig.has(APISpecParserConstants.ENDPOINT_PRODUCTION_ENDPOINTS)) {
                AsyncApiServer prodServer = getAaiServer(apiToUpdate, asyncApiDocument, endpointConfig,
                        APISpecParserConstants.GATEWAY_ENV_TYPE_PRODUCTION,
                        APISpecParserConstants.ENDPOINT_PRODUCTION_ENDPOINTS, servers);
                servers.addItem(APISpecParserConstants.GATEWAY_ENV_TYPE_PRODUCTION, prodServer);
            }
            if (endpointConfig.has(APISpecParserConstants.ENDPOINT_SANDBOX_ENDPOINTS)) {
                AsyncApiServer sandboxServer = getAaiServer(apiToUpdate, asyncApiDocument, endpointConfig,
                        APISpecParserConstants.GATEWAY_ENV_TYPE_SANDBOX,
                        APISpecParserConstants.ENDPOINT_SANDBOX_ENDPOINTS, servers);
                servers.addItem(APISpecParserConstants.GATEWAY_ENV_TYPE_SANDBOX, sandboxServer);
            }
            asyncApiDocument.setServers(servers);
        }
        return Library.writeDocumentToJSONString(asyncApiDocument);
    }

    @Override
    public Map<String, String> buildWSUriMapping(String apiDefinition) {
        Map<String, String> wsUriMapping = new HashMap<>();
        if (apiDefinition == null || apiDefinition.trim().isEmpty()) {
            return wsUriMapping;
        }

        try {
            // Parse once and delegate to helper
            JsonNode root = AsyncApiV3ParserUtil.parseToJsonNode(apiDefinition);
            return AsyncApiV3ParserUtil.getWSUriMapping(root);
        } catch (APIManagementException e) {
            // return empty map on parse failure
            log.debug("[AsyncApiV3Parser] buildWSUriMapping: failed to parse definition: " + e.getMessage());
            return wsUriMapping;
        }
    }


}

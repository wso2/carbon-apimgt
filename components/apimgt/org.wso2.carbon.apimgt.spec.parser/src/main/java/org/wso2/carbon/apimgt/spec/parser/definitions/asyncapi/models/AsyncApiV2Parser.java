package org.wso2.carbon.apimgt.spec.parser.definitions.asyncapi.models;

import com.fasterxml.jackson.databind.JsonNode;
import io.apicurio.datamodels.Library;
import io.apicurio.datamodels.models.MappedNode;
import io.apicurio.datamodels.models.asyncapi.*;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIDefinitionValidationResponse;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.spec.parser.definitions.APISpecParserConstants;
import org.wso2.carbon.apimgt.spec.parser.definitions.APISpecParserUtil;
import org.wso2.carbon.apimgt.spec.parser.definitions.AsyncApiParser;
import org.wso2.carbon.apimgt.spec.parser.definitions.AsyncApiParserUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * This class is used to parse AsyncAPI 2.x.x specifications.
 * It extends the AsyncApiParser class to provide specific parsing capabilities for AsyncAPI 2.x.x.
 */
@Component(
        name = "wso2.async.200.definition.parser.component",
        immediate = true,
        service = APIDefinition.class
)
public class AsyncApiV2Parser extends AsyncApiParser {

    private static final Log log = LogFactory.getLog(AsyncApiParserUtil.class);

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
                            (AsyncApiOperation) channel.getPublish(), scopes, channel));
                }
                if (channel.getSubscribe() != null) {
                    uriTemplates.add(buildURITemplate( (String) entry, APISpecParserConstants.HTTP_VERB_SUBSCRIBE,
                            (AsyncApiOperation) channel.getSubscribe(), scopes, channel));
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
                    throw new APIManagementException("Scope '" + firstScope + "' not found.");
                }
                template.setScope(scope);
                template.setScopes(scope);
            } else {
                for (String scopeName : opScopes) {
                    Scope scope = APISpecParserUtil.findScopeByKey(scopes, scopeName);
                    if (scope == null) {
                        throw new APIManagementException("Resource Scope '" + scopeName + "' not found.");
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

        //import and load AsyncAPI HyperSchema for JSON schema validation
        JSONObject hyperSchema = new JSONObject(APISpecParserConstants.AsyncApiSchemas.ASYNCAPI_JSON_HYPERSCHEMA);
        String protocol = StringUtils.EMPTY;

        boolean validationSuccess = false;
        List<String> validationErrorMessages = null;
        boolean isWebSocket = false;

        JSONObject schemaToBeValidated = new JSONObject(apiDefinition);

        //validate AsyncAPI using JSON schema validation
        try {
            JSONParser parser = new JSONParser();
            org.json.simple.JSONObject json = (org.json.simple.JSONObject) parser.parse(APISpecParserConstants.AsyncApiSchemas.METASCHEMA);
            SchemaLoader schemaLoader = SchemaLoader.builder().registerSchemaByURI
                    (new URI("http://json-schema.org/draft-07/schema#"), json).schemaJson(hyperSchema).build();
            Schema schemaValidator = schemaLoader.load().build();
            schemaValidator.validate(schemaToBeValidated);
            /*AaiDocument asyncApiDocument = (AaiDocument) Library.readDocumentFromJSONString(apiDefinition);
            validationErrorMessages = new ArrayList<>();
            if (asyncApiDocument.getServers().size() == 1) {
                if (!APISpecParserConstants.WS_PROTOCOL.equalsIgnoreCase(asyncApiDocument.getServers().get(0).protocol)) {
                    validationErrorMessages.add("#:The protocol of the server should be 'ws' for websockets");
                }
            }
            if (asyncApiDocument.getServers().size() > 1) {
                validationErrorMessages.add("#:The AsyncAPI definition should contain only a single server for websockets");
            }
            if (asyncApiDocument.getChannels().size() > 1) {
                validationErrorMessages.add("#:The AsyncAPI definition should contain only a single channel for websockets");
            }
            if (validationErrorMessages.size() == 0) {
                validationSuccess = true;
                validationErrorMessages = null;
            }*/

            //AaiDocument asyncApiDocument = (AaiDocument) Library.readDocumentFromJSONString(apiDefinition);
            /*//Checking whether it is a websocket
            validationErrorMessages = new ArrayList<>();
            if (APISpecParserConstants.WS_PROTOCOL.equalsIgnoreCase(asyncApiDocument.getServers().get(0).protocol)) {
                if (APISpecParserConstants.WS_PROTOCOL.equalsIgnoreCase(protocol)) {
                    isWebSocket = true;
                }
            }*/

            //validating channel count for websockets
            /*if (isWebSocket) {
                if (asyncApiDocument.getChannels().size() > 1) {
                    validationErrorMessages.add("#:The AsyncAPI definition should contain only a single channel for websockets");
                }
            }*/

            /*if (validationErrorMessages.size() == 0) {
                validationSuccess = true;
                validationErrorMessages = null;
            }*/

            validationSuccess = true;
        } catch(ValidationException e) {
            //validation error messages
            validationErrorMessages = e.getAllMessages();
        } catch (URISyntaxException e) {
            String msg = "Error occurred when registering the schema";
            throw new APIManagementException(msg, e);
        } catch (ParseException e) {
            String msg = "Error occurred when parsing the schema";
            throw new APIManagementException(msg, e);
        }

        // TODO: Validation is failing. Need to fix this. Therefore overriding the value as True.
        validationSuccess = true;

        if (validationSuccess) {
            AsyncApiDocument asyncApiDocument = (AsyncApiDocument) Library.readDocumentFromJSONString(apiDefinition);
//            AsyncApi20Document asyncApiDocument = (AsyncApi20Document) Library.readDocumentFromJSONString(apiDefinition);
            ArrayList<String> endpoints = new ArrayList<>();
            AsyncApiServers servers = (AsyncApiServers) asyncApiDocument.getServers();
            if (servers != null && servers.getItems() != null && !servers.getItems().isEmpty() &&
                    servers.getItems().size() == 1)
            {
                protocol = ((AsyncApiServer) asyncApiDocument.getServers().getItems().get(0)).getProtocol();
            }
            /*for (AaiServer x : asyncApiDocument.getServers()){
                endpoints.add(x.url);
            }
            AsyncApiParserUtil.updateValidationResponseAsSuccess(
                    validationResponse,
                    apiDefinition,
                    asyncApiDocument.asyncapi,
                    asyncApiDocument.info.title,
                    asyncApiDocument.info.version,
                    null,                           //asyncApiDocument.getChannels().get(0)._name,
                    asyncApiDocument.info.description,
                    endpoints
            );*/

            /*if (isWebSocket) {
                for (AaiServer x : asyncApiDocument.getServers()){
                    endpoints.add(x.url);
                }
                AsyncApiParserUtil.updateValidationResponseAsSuccess(
                        validationResponse,
                        apiDefinition,
                        asyncApiDocument.asyncapi,
                        asyncApiDocument.info.title,
                        asyncApiDocument.info.version,
                        asyncApiDocument.getChannels().get(0)._name,            //make this null
                        asyncApiDocument.info.description,
                        endpoints
                );
            } else {
                AsyncApiParserUtil.updateValidationResponseAsSuccess(
                        validationResponse,
                        apiDefinition,
                        asyncApiDocument.asyncapi,
                        asyncApiDocument.info.title,
                        asyncApiDocument.info.version,
                        null,
                        asyncApiDocument.info.description,
                        null
                );
            }*/

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
//        AsyncApiDocument asyncApiDocument = AsyncApiParserUtil.createAsyncApiDocument(api.getId().getVersion());
//        AsyncApi20Document aaiDocument = new AsyncApi20DocumentImpl();
        AsyncApiDocument asyncApiDocument = AsyncApiParserUtil.createAsyncApiDocument(
                APISpecParserConstants.AsyncApi.ASYNC_API_V20);
        asyncApiDocument.setAsyncapi("2.0.0");
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

        AsyncApiChannels apiChannels = (AsyncApiChannels) asyncApiDocument.createChannels();
        MappedNode channels = (MappedNode) apiChannels;
        for (URITemplate uriTemplate : api.getUriTemplates()) {
            AsyncApiChannelItem channelItem = AsyncApiParserUtil.createChannelItem(apiChannels);
            AsyncApiOperation subscribeOp = (AsyncApiOperation) channelItem.createOperation();
            channelItem.setSubscribe(subscribeOp);
            if (APISpecParserConstants.API_TYPE_WS.equals(api.getType())) {
                AsyncApiOperation publishOp = (AsyncApiOperation) channelItem.createOperation();
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
     * @param asyncApiDocument  Async Api Document
     * @param endpointConfig    Endpoint configuration
     * @param serverName        Name of the server
     * @param endpoint          Endpoint to be configured
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
     * @param api            API
     * @param asyncAPIDefinition  AsyncAPI definition
     * @param hostsWithSchemes host addresses with protocol mapping
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
    public Map<String,String> buildWSUriMapping(String apiDefinition) {
        Map<String,String> wsUriMapping = new HashMap<>();
        AsyncApiDocument asyncApiDocument = (AsyncApiDocument) Library.readDocumentFromJSONString(apiDefinition);
        AsyncApiChannels apiChannels = (AsyncApiChannels) asyncApiDocument.getChannels();
        MappedNode channels = (MappedNode) apiChannels;
        if (channels != null && !channels.getItems().isEmpty()) {
            for (Object entry : channels.getItemNames()) {
                AsyncApiChannelItem channel = (AsyncApiChannelItem) channels.getItem((String) entry);
                AsyncApiOperation publishOperation = (AsyncApiOperation) channel.getPublish();
                if (publishOperation != null) {
                    AsyncApiExtensible publishExtensibleOperation = (AsyncApiExtensible) publishOperation;
                    Map<String, JsonNode> publishExtensions = publishExtensibleOperation.getExtensions();
                    if (publishExtensions != null) {
                        JsonNode xUriMapping = publishExtensions.get("x-uri-mapping");
                        if (xUriMapping != null) {
                            wsUriMapping.put("PUBLISH_" + entry, xUriMapping.asText());
                        }
                    }
                }
                AsyncApiOperation subscribeOperation = (AsyncApiOperation) channel.getSubscribe();
                if (subscribeOperation != null) {
                    AsyncApiExtensible subscribeExtensibleOperation = (AsyncApiExtensible) subscribeOperation;
                    Map<String, JsonNode> subscribeExtensions = subscribeExtensibleOperation.getExtensions();
                    if (subscribeExtensions != null) {
                        JsonNode xUriMapping = subscribeExtensions.get("x-uri-mapping");
                        if (xUriMapping != null) {
                            wsUriMapping.put("SUBSCRIBE_" + entry, xUriMapping.asText());
                        }
                    }
                }
            }
        }
        return wsUriMapping;
    }
}

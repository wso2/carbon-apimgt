package org.wso2.carbon.apimgt.spec.parser.definitions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.apicurio.datamodels.Library;
import io.apicurio.datamodels.models.Document;
import io.apicurio.datamodels.models.MappedNode;
import io.apicurio.datamodels.models.asyncapi.*;
import io.apicurio.datamodels.models.asyncapi.v20.*;
import io.apicurio.datamodels.models.asyncapi.v21.*;
import io.apicurio.datamodels.models.asyncapi.v22.*;
import io.apicurio.datamodels.models.asyncapi.v23.*;
import io.apicurio.datamodels.models.asyncapi.v24.*;
import io.apicurio.datamodels.models.asyncapi.v25.*;
import io.apicurio.datamodels.models.asyncapi.v26.*;
import io.apicurio.datamodels.models.asyncapi.v30.*;
import io.apicurio.datamodels.models.util.JsonUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.wso2.carbon.apimgt.api.APIDefinitionValidationResponse;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ErrorHandler;
import org.wso2.carbon.apimgt.api.ErrorItem;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.UsedByMigrationClient;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.spec.parser.definitions.asyncapi.AsyncApiParserFactory;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class AsyncApiParserUtil {
    
    private static final Log log = LogFactory.getLog(AsyncApiParserUtil.class);
    private static final String PATH_SEPARATOR = "/";

    @UsedByMigrationClient
    public static APIDefinitionValidationResponse validateAsyncAPISpecification(
            String schemaToBeValidated, boolean returnJSONContent) throws APIManagementException {

        log.debug("[AsyncAPI][APIDefinitionValidationResponse] AsyncAPI definition validation has started");
        AsyncApiParser asyncApiParser = AsyncApiParserFactory.getAsyncApiParser(
                getAsyncApiVersion(schemaToBeValidated));
        APIDefinitionValidationResponse validationResponse = asyncApiParser.validateAPIDefinition(schemaToBeValidated,
                returnJSONContent);
        log.debug("[AsyncAPI][APIDefinitionValidationResponse] AsyncAPI definition validation is completed");
        final String asyncAPIKeyNotFound = "#: required key [asyncapi] not found";

        if (!validationResponse.isValid()) {
            for (ErrorHandler errorItem : validationResponse.getErrorItems()) {
                if (asyncAPIKeyNotFound.equals(errorItem.getErrorMessage())) {    //change it other way
                    addErrorToValidationResponse(validationResponse, "#: attribute [asyncapi] should be present");
                    return validationResponse;
                }
            }
        }

        return validationResponse;
    }

    public static APIDefinitionValidationResponse validateAsyncAPISpecificationByURL(
            String url, HttpClient httpClient, boolean returnJSONContent) throws APIManagementException{

        APIDefinitionValidationResponse validationResponse = new APIDefinitionValidationResponse();

        try {
            HttpGet httpGet = new HttpGet(url);
            HttpResponse response = httpClient.execute(httpGet);

            if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
                ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
                Object obj = yamlReader.readValue(new URL(url), Object.class);
                ObjectMapper jsonWriter = new ObjectMapper();
                String json = jsonWriter.writeValueAsString(obj);
                validationResponse = validateAsyncAPISpecification(json, returnJSONContent);
            } else {
                validationResponse.setValid(false);
                validationResponse.getErrorItems().add(ExceptionCodes.ASYNCAPI_URL_NO_200);
            }
        } catch (IOException e) {
            ErrorHandler errorHandler = ExceptionCodes.ASYNCAPI_URL_MALFORMED;
            //log the error and continue since this method is only intended to validate a definition
            log.error(errorHandler.getErrorDescription(), e);

            validationResponse.setValid(false);
            validationResponse.getErrorItems().add(errorHandler);
        }

        return validationResponse;
    }

    public static void updateValidationResponseAsSuccess(
            APIDefinitionValidationResponse validationResponse,
            String originalAPIDefinition,
            String asyncAPIVersion,
            String title,
            String version,
            String context,
            String description,
            List<String> endpoints)
    {
        validationResponse.setValid(true);
        validationResponse.setContent(originalAPIDefinition);
        APIDefinitionValidationResponse.Info info = new APIDefinitionValidationResponse.Info();
        info.setOpenAPIVersion(asyncAPIVersion);
        info.setName(title);
        info.setVersion(version);
        info.setContext(context);
        info.setDescription(description);
        info.setEndpoints(endpoints);
        validationResponse.setInfo(info);
    }

    public static void updateValidationResponseAsSuccess(
            APIDefinitionValidationResponse validationResponse,
            String originalAPIDefinition,
            String asyncAPIVersion,
            String title,
            String version,
            String context,
            String description,
            List<String> endpoints,
            List<URITemplate> uriTemplates)
    {
        validationResponse.setValid(true);
        validationResponse.setContent(originalAPIDefinition);
        APIDefinitionValidationResponse.Info info = new APIDefinitionValidationResponse.Info();
        info.setOpenAPIVersion(asyncAPIVersion);
        info.setName(title);
        info.setVersion(version);
        info.setContext(context);
        info.setDescription(description);
        info.setEndpoints(endpoints);
        info.setUriTemplates(uriTemplates);
        validationResponse.setInfo(info);
    }

    public static ErrorItem addErrorToValidationResponse(
            APIDefinitionValidationResponse validationResponse, String errMessage) {
        ErrorItem errorItem = new ErrorItem();
        errorItem.setMessage(errMessage);
        validationResponse.getErrorItems().add(errorItem);
        return errorItem;
    }

    /**
     * Get available transport protocols for the Async API
     *
     * @param definition Async API Definition
     * @return List<String> List of available transport protocols
     * @throws APIManagementException If the async env configuration if not provided properly
     */
    public static List<String> getTransportProtocolsForAsyncAPI(String definition) throws APIManagementException {

        HashSet<String> asyncTransportProtocols = new HashSet<>();
        AsyncApiDocument asyncApiDocument = (AsyncApiDocument) Library.readDocumentFromJSONString(definition);
        AsyncApiChannels channels = asyncApiDocument.getChannels();

        if (channels instanceof AsyncApi20Channels) {
            for (AsyncApiChannelItem channel : ((AsyncApi20Channels) channels).getItems()) {
                asyncTransportProtocols.addAll(getProtocols(channel));
            }
        } else if (channels instanceof AsyncApi21Channels) {
            for (AsyncApiChannelItem channel : ((AsyncApi21Channels) channels).getItems()) {
                asyncTransportProtocols.addAll(getProtocols(channel));
            }
        } else if (channels instanceof AsyncApi22Channels) {
            for (AsyncApiChannelItem channel : ((AsyncApi22Channels) channels).getItems()) {
                asyncTransportProtocols.addAll(getProtocols(channel));
            }
        } else if (channels instanceof AsyncApi23Channels) {
            for (AsyncApiChannelItem channel : ((AsyncApi23Channels) channels).getItems()) {
                asyncTransportProtocols.addAll(getProtocols(channel));
            }
        } else if (channels instanceof AsyncApi24Channels) {
            for (AsyncApiChannelItem channel : ((AsyncApi24Channels) channels).getItems()) {
                asyncTransportProtocols.addAll(getProtocols(channel));
            }
        } else if (channels instanceof AsyncApi25Channels) {
            for (AsyncApiChannelItem channel : ((AsyncApi25Channels) channels).getItems()) {
                asyncTransportProtocols.addAll(getProtocols(channel));
            }
        } else if (channels instanceof AsyncApi26Channels) {
            for (AsyncApiChannelItem channel : ((AsyncApi26Channels) channels).getItems()) {
                asyncTransportProtocols.addAll(getProtocols(channel));
            }
        } else if (channels instanceof AsyncApi30Channels) {
            AsyncApi30Operations operations = ((AsyncApi30Document) asyncApiDocument).getOperations();
            if (operations != null && operations.getItems() != null) {
                for (AsyncApi30Operation operation : operations.getItems()) {
                    if (operation.getBindings() != null) {
                        asyncTransportProtocols.addAll(getProtocolsFromBindings(operation.getBindings()));
                    }
                }
            }
        } else {
            throw new UnsupportedOperationException("No Channels available for AsyncAPI version: "
                    + asyncApiDocument.getAsyncapi() + " document");
        }
        ArrayList<String> asyncTransportProtocolsList = new ArrayList<>(asyncTransportProtocols);
        return asyncTransportProtocolsList;
    }

    /**
     * Get the transport protocols
     *
     * @param channel AsyncApiChannelItem to get protocol
     * @return HashSet<String> set of transport protocols
     */
    public static HashSet<String> getProtocols(AsyncApiChannelItem channel) {

        HashSet<String> protocols = new HashSet<>();

        if (channel.getSubscribe() != null) {
            if (channel.getSubscribe().getBindings() != null) {
                protocols.addAll(getProtocolsFromBindings(channel.getSubscribe().getBindings()));
            }
        }
        if (channel.getPublish() != null) {
            if (channel.getPublish().getBindings() != null) {
                protocols.addAll(getProtocolsFromBindings(channel.getPublish().getBindings()));
            }
        }

        return protocols;
    }

    /**
     * Get the transport protocols the bindings
     *
     * @param bindings AsyncApiOperationBindings to get protocols
     * @return HashSet<String> set of transport protocols
     */
    private static HashSet<String> getProtocolsFromBindings(AsyncApiOperationBindings bindings) {

        HashSet<String> protocolsFromBindings = new HashSet<>();

        if (bindings.getHttp() != null) {
            protocolsFromBindings.add(APISpecParserConstants.HTTP_TRANSPORT_PROTOCOL_NAME);
        }
        if (bindings.getWs() != null) {
            protocolsFromBindings.add(APISpecParserConstants.WS_TRANSPORT_PROTOCOL_NAME);
        }
        if (bindings.getKafka() != null) {
            protocolsFromBindings.add(APISpecParserConstants.KAFKA_TRANSPORT_PROTOCOL_NAME);
        }
        if (bindings.getAmqp() != null) {
            protocolsFromBindings.add(APISpecParserConstants.AMQP_TRANSPORT_PROTOCOL_NAME);
        }
        if (bindings.getAmqp1() != null) {
            protocolsFromBindings.add(APISpecParserConstants.AMQP1_TRANSPORT_PROTOCOL_NAME);
        }
        if (bindings.getMqtt() != null) {
            protocolsFromBindings.add(APISpecParserConstants.MQTT_TRANSPORT_PROTOCOL_NAME);
        }
        if (bindings.getMqtt5() != null) {
            protocolsFromBindings.add(APISpecParserConstants.MQTT5_TRANSPORT_PROTOCOL_NAME);
        }
        if (bindings.getNats() != null) {
            protocolsFromBindings.add(APISpecParserConstants.NATS_TRANSPORT_PROTOCOL_NAME);
        }
        if (bindings.getJms() != null) {
            protocolsFromBindings.add(APISpecParserConstants.JMS_TRANSPORT_PROTOCOL_NAME);
        }
        if (bindings.getSns() != null) {
            protocolsFromBindings.add(APISpecParserConstants.SNS_TRANSPORT_PROTOCOL_NAME);
        }
        if (bindings.getSqs() != null) {
            protocolsFromBindings.add(APISpecParserConstants.SQS_TRANSPORT_PROTOCOL_NAME);
        }
        if (bindings.getStomp() != null) {
            protocolsFromBindings.add(APISpecParserConstants.STOMP_TRANSPORT_PROTOCOL_NAME);
        }
        if (bindings.getRedis() != null) {
            protocolsFromBindings.add(APISpecParserConstants.REDIS_TRANSPORT_PROTOCOL_NAME);
        }

        if (bindings.hasExtraProperties()) {
            protocolsFromBindings.addAll(bindings.getExtraPropertyNames());
        }

        return protocolsFromBindings;
    }

    public static String getAsyncApiVersion(String apiDefinition) {
        ObjectNode json = (ObjectNode) JsonUtil.parseJSON(apiDefinition);
        log.debug("[AsyncAPI][getAsyncApiVersion] AsyncAPI definition version : " +
                JsonUtil.getStringProperty(json, APISpecParserConstants.AsyncApi.ASYNC_API));
        return JsonUtil.getStringProperty(json, APISpecParserConstants.AsyncApi.ASYNC_API);
    }

    /**
     * This method will provide the respective Async API Document with data from based on the version
     *
     * @param version String
     * @param definition String
     * @return AsyncApiDocument
     */
    public static AsyncApiDocument getFromAsyncApiDocument(String version, String definition){

        Document asyncApiDocument = Library.readDocumentFromJSONString(definition);
        if (version.startsWith(APISpecParserConstants.AsyncApi.ASYNC_API_V20)) {
            return (AsyncApi20Document) asyncApiDocument;
        } else if (version.startsWith(APISpecParserConstants.AsyncApi.ASYNC_API_V21)) {
            return (AsyncApi21Document) asyncApiDocument;
        } else if (version.startsWith(APISpecParserConstants.AsyncApi.ASYNC_API_V22)) {
            return (AsyncApi22Document) asyncApiDocument;
        } else if (version.startsWith(APISpecParserConstants.AsyncApi.ASYNC_API_V23)) {
            return (AsyncApi23Document) asyncApiDocument;
        } else if (version.startsWith(APISpecParserConstants.AsyncApi.ASYNC_API_V24)) {
            return (AsyncApi24Document) asyncApiDocument;
        } else if (version.startsWith(APISpecParserConstants.AsyncApi.ASYNC_API_V25)) {
            return (AsyncApi25Document) asyncApiDocument;
        } else if (version.startsWith(APISpecParserConstants.AsyncApi.ASYNC_API_V26)) {
            return (AsyncApi26Document) asyncApiDocument;
        } else if (version.startsWith(APISpecParserConstants.AsyncApi.ASYNC_API_V30)) {
            return (AsyncApi30Document) asyncApiDocument;
        } else {
            throw new UnsupportedOperationException("Unsupported AsyncAPI version: " + version);
        }
    }

    /**
     * This method will extract the extension data from the Async Api Document based on the version
     *
     * @param definition String
     * @return Map<String, JsonNode>
     */
    public static Map<String, JsonNode> getExtensionFromAsyncApiDoc(String definition) {

        AsyncApiDocument asyncApiDocument = getFromAsyncApiDocument(getAsyncApiVersion(definition), definition);
//        AsyncApiExtensible asyncApiExtensible = (AsyncApiExtensible) asyncApiDocument;
//        asyncApiExtensible.getExtensions();
        Map<String, JsonNode> extensions = null;
        if (asyncApiDocument instanceof AsyncApi20Document) {
            return extensions = ((AsyncApi20Document) asyncApiDocument).getExtensions();
        } else if (asyncApiDocument instanceof AsyncApi21Document) {
            return extensions = ((AsyncApi21Document) asyncApiDocument).getExtensions();
        } else if (asyncApiDocument instanceof AsyncApi22Document) {
            return extensions = ((AsyncApi22Document) asyncApiDocument).getExtensions();
        } else if (asyncApiDocument instanceof AsyncApi23Document) {
            return extensions = ((AsyncApi23Document) asyncApiDocument).getExtensions();
        } else if (asyncApiDocument instanceof AsyncApi24Document) {
            return extensions = ((AsyncApi24Document) asyncApiDocument).getExtensions();
        } else if (asyncApiDocument instanceof AsyncApi25Document) {
            return extensions = ((AsyncApi25Document) asyncApiDocument).getExtensions();
        } else if (asyncApiDocument instanceof AsyncApi26Document) {
            return extensions = ((AsyncApi26Document) asyncApiDocument).getExtensions();
        } else if (asyncApiDocument instanceof AsyncApi30Document) {
            return extensions = ((AsyncApi30Document) asyncApiDocument).getExtensions();
        } else {
            throw new UnsupportedOperationException("Unsupported AsyncAPI version: " + getAsyncApiVersion(definition));
        }
    }

    /**
     * This method will extract the OAuth Flow data from the Async Api Document based on the version
     *
     * @param flow AsyncApiOAuthFlows
     * @return Map<String, JsonNode>
     */
    public static Map<String, String> getAsyncApiOAuthFlowsScopes(AsyncApiOAuthFlow flow) {
        if (flow instanceof AsyncApi20OAuthFlow) {
            return ((AsyncApi20OAuthFlow) flow).getScopes();
        } else if (flow instanceof AsyncApi21OAuthFlow) {
            return ((AsyncApi21OAuthFlow) flow).getScopes();
        } else if (flow instanceof AsyncApi22OAuthFlow) {
            return ((AsyncApi22OAuthFlow) flow).getScopes();
        } else if (flow instanceof AsyncApi23OAuthFlow) {
            return ((AsyncApi23OAuthFlow) flow).getScopes();
        } else if (flow instanceof AsyncApi24OAuthFlow) {
            return ((AsyncApi24OAuthFlow) flow).getScopes();
        } else if (flow instanceof AsyncApi25OAuthFlow) {
            return ((AsyncApi25OAuthFlow) flow).getScopes();
        } else if (flow instanceof AsyncApi26OAuthFlow) {
            return ((AsyncApi26OAuthFlow) flow).getScopes();
        } else if (flow instanceof AsyncApi30OAuthFlow) {
            return ((AsyncApi30OAuthFlow) flow).getAvailableScopes();
        } else {
            return Collections.emptyMap();
        }
    }

    /**
     * This method will extract set the OAuth Flow scopes for the Async Api Document based on the version
     * @param securityScheme AsyncApiSecurityScheme
     * @param scopes  Map<String, String>
     * @param scopeBindings Map<String, String>
     */
    public static void setAsyncApiOAuthFlowsScopes(AsyncApiSecurityScheme securityScheme,
                                                   Map<String, String> scopes, Map<String, String> scopeBindings) {

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode scopeBindingsNode = objectMapper.valueToTree(scopeBindings);

        if (securityScheme instanceof AsyncApi20SecurityScheme) {
            ((AsyncApi20OAuthFlow) securityScheme.getFlows().getImplicit()).setScopes(scopes);
            ((AsyncApi20OAuthFlow) securityScheme.getFlows().getImplicit()).addExtension(
                    APISpecParserConstants.SWAGGER_X_SCOPES_BINDINGS, scopeBindingsNode);
        } else if (securityScheme instanceof AsyncApi21SecurityScheme) {
            ((AsyncApi21OAuthFlow) securityScheme.getFlows().getImplicit()).setScopes(scopes);
            ((AsyncApi21OAuthFlow) securityScheme.getFlows().getImplicit()).addExtension(
                    APISpecParserConstants.SWAGGER_X_SCOPES_BINDINGS, scopeBindingsNode);
        } else if (securityScheme instanceof AsyncApi22SecurityScheme) {
            ((AsyncApi22OAuthFlow) securityScheme.getFlows().getImplicit()).setScopes(scopes);
            ((AsyncApi22OAuthFlow) securityScheme.getFlows().getImplicit()).addExtension(
                    APISpecParserConstants.SWAGGER_X_SCOPES_BINDINGS, scopeBindingsNode);
        } else if (securityScheme instanceof AsyncApi23SecurityScheme) {
            ((AsyncApi23OAuthFlow) securityScheme.getFlows().getImplicit()).setScopes(scopes);
            ((AsyncApi23OAuthFlow) securityScheme.getFlows().getImplicit()).addExtension(
                    APISpecParserConstants.SWAGGER_X_SCOPES_BINDINGS, scopeBindingsNode);
        } else if (securityScheme instanceof AsyncApi24SecurityScheme) {
            ((AsyncApi24OAuthFlow) securityScheme.getFlows().getImplicit()).setScopes(scopes);
            ((AsyncApi24OAuthFlow) securityScheme.getFlows().getImplicit()).addExtension(
                    APISpecParserConstants.SWAGGER_X_SCOPES_BINDINGS, scopeBindingsNode);
        } else if (securityScheme instanceof AsyncApi25SecurityScheme) {
            ((AsyncApi25OAuthFlow) securityScheme.getFlows().getImplicit()).setScopes(scopes);
            ((AsyncApi25OAuthFlow) securityScheme.getFlows().getImplicit()).addExtension(
                    APISpecParserConstants.SWAGGER_X_SCOPES_BINDINGS, scopeBindingsNode);
        } else if (securityScheme instanceof AsyncApi26SecurityScheme) {
            ((AsyncApi26OAuthFlow) securityScheme.getFlows().getImplicit()).setScopes(scopes);
            ((AsyncApi26OAuthFlow) securityScheme.getFlows().getImplicit()).addExtension(
                    APISpecParserConstants.SWAGGER_X_SCOPES_BINDINGS, scopeBindingsNode);
        } else if (securityScheme instanceof AsyncApi30SecurityScheme) {
            ((AsyncApi30OAuthFlow) securityScheme.getFlows().getImplicit()).setAvailableScopes(scopes);
            ((AsyncApi30OAuthFlow) securityScheme.getFlows().getImplicit()).addExtension(
                    APISpecParserConstants.SWAGGER_X_SCOPES_BINDINGS, scopeBindingsNode);
        } else {
        throw new UnsupportedOperationException("Unsupported AsyncAPI OAuth Flow");
        }
    }

    /**
     * This method will create and provide the respective Async API Document with data from based on the version
     * @param version String
     * @return AsyncApiDocument
     */
    public static AsyncApiDocument createAsyncApiDocument(String version){
        if (version.startsWith(APISpecParserConstants.AsyncApi.ASYNC_API_V20)) {
            return new AsyncApi20DocumentImpl();
        } else if (version.startsWith(APISpecParserConstants.AsyncApi.ASYNC_API_V21)) {
            return new AsyncApi21DocumentImpl();
        } else if (version.startsWith(APISpecParserConstants.AsyncApi.ASYNC_API_V22)) {
            return new AsyncApi22DocumentImpl();
        } else if (version.startsWith(APISpecParserConstants.AsyncApi.ASYNC_API_V23)) {
            return new AsyncApi23DocumentImpl();
        } else if (version.startsWith(APISpecParserConstants.AsyncApi.ASYNC_API_V24)) {
            return new AsyncApi24DocumentImpl();
        } else if (version.startsWith(APISpecParserConstants.AsyncApi.ASYNC_API_V25)) {
            return new AsyncApi25DocumentImpl();
        } else if (version.startsWith(APISpecParserConstants.AsyncApi.ASYNC_API_V26)) {
            return new AsyncApi26DocumentImpl();
        } else if (version.startsWith(APISpecParserConstants.AsyncApi.ASYNC_API_V30)) {
            return new AsyncApi30DocumentImpl();
        } else {
            throw new UnsupportedOperationException("Unsupported AsyncAPI version: " + version);
        }
    }

    /**
     * This method will create and provide the respective Async API Channel Item with data from based on the version
     * @param channels String
     * @return AsyncApiDocument
     */
    public static AsyncApiChannelItem createChannelItem(AsyncApiChannels channels){
        if (channels instanceof AsyncApi20Channels) {
            return ((AsyncApi20Channels) channels).createChannelItem();
        } else if (channels instanceof AsyncApi21Channels) {
            return ((AsyncApi21Channels) channels).createChannelItem();
        } else if (channels instanceof AsyncApi22Channels) {
            return ((AsyncApi22Channels) channels).createChannelItem();
        } else if (channels instanceof AsyncApi23Channels) {
            return ((AsyncApi23Channels) channels).createChannelItem();
        } else if (channels instanceof AsyncApi24Channels) {
            return ((AsyncApi24Channels) channels).createChannelItem();
        } else if (channels instanceof AsyncApi25Channels) {
            return ((AsyncApi25Channels) channels).createChannelItem();
        } else if (channels instanceof AsyncApi26Channels) {
            return ((AsyncApi26Channels) channels).createChannelItem();
        } else {
            throw new UnsupportedOperationException("Unsupported AsyncAPI Channel");
        }
    }

    /**
     * This method will create and provide the respective Async API Channel with data from based on the version
     * @param channels String
     * @return AsyncApiDocument
     */
    public static AsyncApi30Channel createChannel(AsyncApiChannels channels){
        if (channels instanceof AsyncApi30Channels) {
            return ((AsyncApi30Channels) channels).createChannel();
        } else {
            throw new UnsupportedOperationException("Unsupported AsyncAPI Channel");
        }
    }

    /**
     * This method will return a list of channel items from the respective Async API document
     * @param asyncDocument AsyncApiDocument
     * @return List<AsyncApiChannelItem>
     */
    public static List<AsyncApiChannelItem> getAllChannels(AsyncApiDocument asyncDocument) {
        List<AsyncApiChannelItem> channels = new ArrayList<>();
        AsyncApiChannels apiChannels = asyncDocument.getChannels();

        if (apiChannels instanceof MappedNode) {
            MappedNode<?> mappedChannels = (MappedNode<?>) apiChannels;
            for (Object item : mappedChannels.getItems()) {
                if (item instanceof AsyncApiChannelItem) {
                    channels.add((AsyncApiChannelItem) item);
                }
            }
        }
        return channels;
    }

    /**
     * This method will return schema from the parameter object for the respective Async API based on the version
     *
     * @param parameterObj Object
     * @return Object
     */
    public static Object getSchemaFromParameter(Object parameterObj) {
        if (parameterObj instanceof AsyncApi20Parameter) {
            return ((AsyncApi20Parameter) parameterObj).getSchema();
        } else if (parameterObj instanceof AsyncApi21Parameter) {
            return ((AsyncApi21Parameter) parameterObj).getSchema();
        } else if (parameterObj instanceof AsyncApi22Parameter) {
            return ((AsyncApi22Parameter) parameterObj).getSchema();
        } else if (parameterObj instanceof AsyncApi23Parameter) {
            return ((AsyncApi23Parameter) parameterObj).getSchema();
        } else if (parameterObj instanceof AsyncApi24Parameter) {
            return ((AsyncApi24Parameter) parameterObj).getSchema();
        } else if (parameterObj instanceof AsyncApi25Parameter) {
            return ((AsyncApi25Parameter) parameterObj).getSchema();
        } else if (parameterObj instanceof AsyncApi26Parameter) {
            return ((AsyncApi26Parameter) parameterObj).getSchema();
//        } else if (parameterObj instanceof AsyncApi30Parameter) {
//            return ((AsyncApi30Parameter) parameterObj).getSchema();
        } else {
            throw new UnsupportedOperationException("Unsupported AsyncAPI Parameter");
        }
    }

    /**
     * This method will return reference from the parameter object for the respective Async API based on the version
     *
     * @param parameterObj Object
     * @return String
     */
    public static String getRefFromParameter(Object parameterObj) {
        if (parameterObj instanceof AsyncApi20Referenceable) {
            return ((AsyncApi20Referenceable) parameterObj).get$ref();
        } else if (parameterObj instanceof AsyncApi21Referenceable) {
            return ((AsyncApi21Referenceable) parameterObj).get$ref();
        } else if (parameterObj instanceof AsyncApi22Referenceable) {
            return ((AsyncApi22Referenceable) parameterObj).get$ref();
        } else if (parameterObj instanceof AsyncApi23Referenceable) {
            return ((AsyncApi23Referenceable) parameterObj).get$ref();
        } else if (parameterObj instanceof AsyncApi24Referenceable) {
            return ((AsyncApi24Referenceable) parameterObj).get$ref();
        } else if (parameterObj instanceof AsyncApi25Referenceable) {
            return ((AsyncApi25Referenceable) parameterObj).get$ref();
        } else if (parameterObj instanceof AsyncApi26Referenceable) {
            return ((AsyncApi26Referenceable) parameterObj).get$ref();
        } else if (parameterObj instanceof AsyncApi30Referenceable) {
            return ((AsyncApi30Referenceable) parameterObj).get$ref();
        } else {
            throw new UnsupportedOperationException("Unsupported AsyncAPI Parameter");
        }
    }

    /**
     * This method will set AsyncApiServer for the respective Async API based on the version
     *
     * @param url String
     * @param server AsyncApiServer
     * @return AsyncApiDocument
     */
    public static void setAsyncApiServer(String url, AsyncApiServer server){
        String host;
        String pathname = "/";
        if (server instanceof AsyncApi20Server) {
            ((AsyncApi20Server) server).setUrl(url);
        } else if (server instanceof AsyncApi21Server) {
            ((AsyncApi21Server) server).setUrl(url);
        } else if (server instanceof AsyncApi22Server) {
            ((AsyncApi22Server) server).setUrl(url);
        } else if (server instanceof AsyncApi23Server) {
            ((AsyncApi23Server) server).setUrl(url);
        } else if (server instanceof AsyncApi24Server) {
            ((AsyncApi24Server) server).setUrl(url);
        } else if (server instanceof AsyncApi25Server) {
            ((AsyncApi25Server) server).setUrl(url);
        } else if (server instanceof AsyncApi26Server) {
            ((AsyncApi26Server) server).setUrl(url);
        } else if (server instanceof AsyncApi30Server) {
            if (url.contains("/")) {
                int idx = url.indexOf("/");
                host = url.substring(0, idx);
                pathname = url.substring(idx);
            } else {
                host = url;
            }
            ((AsyncApi30Server) server).setHost(host);
            ((AsyncApi30Server) server).setPathname(pathname);
        } else {
            throw new UnsupportedOperationException("Unsupported AsyncAPI Channel");
        }
    }

}

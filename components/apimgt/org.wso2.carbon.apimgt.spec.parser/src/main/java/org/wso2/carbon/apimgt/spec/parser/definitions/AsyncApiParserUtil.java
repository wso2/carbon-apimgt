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
package org.wso2.carbon.apimgt.spec.parser.definitions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.apicurio.datamodels.Library;
import io.apicurio.datamodels.models.Document;
import io.apicurio.datamodels.models.MappedNode;
import io.apicurio.datamodels.models.asyncapi.AsyncApiChannelItem;
import io.apicurio.datamodels.models.asyncapi.AsyncApiChannels;
import io.apicurio.datamodels.models.asyncapi.AsyncApiDocument;
import io.apicurio.datamodels.models.asyncapi.AsyncApiExtensible;
import io.apicurio.datamodels.models.asyncapi.AsyncApiOAuthFlow;
import io.apicurio.datamodels.models.asyncapi.AsyncApiOperationBindings;
import io.apicurio.datamodels.models.asyncapi.AsyncApiReferenceable;
import io.apicurio.datamodels.models.asyncapi.AsyncApiSecurityScheme;
import io.apicurio.datamodels.models.asyncapi.AsyncApiServer;
import io.apicurio.datamodels.models.asyncapi.v20.AsyncApi20Channels;
import io.apicurio.datamodels.models.asyncapi.v20.AsyncApi20DocumentImpl;
import io.apicurio.datamodels.models.asyncapi.v20.AsyncApi20OAuthFlow;
import io.apicurio.datamodels.models.asyncapi.v20.AsyncApi20Parameter;
import io.apicurio.datamodels.models.asyncapi.v20.AsyncApi20SecurityScheme;
import io.apicurio.datamodels.models.asyncapi.v20.AsyncApi20Server;
import io.apicurio.datamodels.models.asyncapi.v21.AsyncApi21Channels;
import io.apicurio.datamodels.models.asyncapi.v21.AsyncApi21DocumentImpl;
import io.apicurio.datamodels.models.asyncapi.v21.AsyncApi21OAuthFlow;
import io.apicurio.datamodels.models.asyncapi.v21.AsyncApi21Parameter;
import io.apicurio.datamodels.models.asyncapi.v21.AsyncApi21SecurityScheme;
import io.apicurio.datamodels.models.asyncapi.v21.AsyncApi21Server;
import io.apicurio.datamodels.models.asyncapi.v22.AsyncApi22Channels;
import io.apicurio.datamodels.models.asyncapi.v22.AsyncApi22DocumentImpl;
import io.apicurio.datamodels.models.asyncapi.v22.AsyncApi22OAuthFlow;
import io.apicurio.datamodels.models.asyncapi.v22.AsyncApi22Parameter;
import io.apicurio.datamodels.models.asyncapi.v22.AsyncApi22SecurityScheme;
import io.apicurio.datamodels.models.asyncapi.v22.AsyncApi22Server;
import io.apicurio.datamodels.models.asyncapi.v23.AsyncApi23Channels;
import io.apicurio.datamodels.models.asyncapi.v23.AsyncApi23DocumentImpl;
import io.apicurio.datamodels.models.asyncapi.v23.AsyncApi23OAuthFlow;
import io.apicurio.datamodels.models.asyncapi.v23.AsyncApi23Parameter;
import io.apicurio.datamodels.models.asyncapi.v23.AsyncApi23SecurityScheme;
import io.apicurio.datamodels.models.asyncapi.v23.AsyncApi23Server;
import io.apicurio.datamodels.models.asyncapi.v24.AsyncApi24Channels;
import io.apicurio.datamodels.models.asyncapi.v24.AsyncApi24DocumentImpl;
import io.apicurio.datamodels.models.asyncapi.v24.AsyncApi24OAuthFlow;
import io.apicurio.datamodels.models.asyncapi.v24.AsyncApi24Parameter;
import io.apicurio.datamodels.models.asyncapi.v24.AsyncApi24SecurityScheme;
import io.apicurio.datamodels.models.asyncapi.v24.AsyncApi24Server;
import io.apicurio.datamodels.models.asyncapi.v25.AsyncApi25Channels;
import io.apicurio.datamodels.models.asyncapi.v25.AsyncApi25DocumentImpl;
import io.apicurio.datamodels.models.asyncapi.v25.AsyncApi25OAuthFlow;
import io.apicurio.datamodels.models.asyncapi.v25.AsyncApi25Parameter;
import io.apicurio.datamodels.models.asyncapi.v25.AsyncApi25SecurityScheme;
import io.apicurio.datamodels.models.asyncapi.v25.AsyncApi25Server;
import io.apicurio.datamodels.models.asyncapi.v26.AsyncApi26Channels;
import io.apicurio.datamodels.models.asyncapi.v26.AsyncApi26DocumentImpl;
import io.apicurio.datamodels.models.asyncapi.v26.AsyncApi26OAuthFlow;
import io.apicurio.datamodels.models.asyncapi.v26.AsyncApi26Parameter;
import io.apicurio.datamodels.models.asyncapi.v26.AsyncApi26SecurityScheme;
import io.apicurio.datamodels.models.asyncapi.v26.AsyncApi26Server;
import io.apicurio.datamodels.models.asyncapi.v30.AsyncApi30Channel;
import io.apicurio.datamodels.models.asyncapi.v30.AsyncApi30Channels;
import io.apicurio.datamodels.models.asyncapi.v30.AsyncApi30Document;
import io.apicurio.datamodels.models.asyncapi.v30.AsyncApi30DocumentImpl;
import io.apicurio.datamodels.models.asyncapi.v30.AsyncApi30OAuthFlow;
import io.apicurio.datamodels.models.asyncapi.v30.AsyncApi30Operation;
import io.apicurio.datamodels.models.asyncapi.v30.AsyncApi30Operations;
import io.apicurio.datamodels.models.asyncapi.v30.AsyncApi30SecurityScheme;
import io.apicurio.datamodels.models.asyncapi.v30.AsyncApi30Server;
import io.apicurio.datamodels.models.util.JsonUtil;
import io.apicurio.datamodels.validation.DefaultSeverityRegistry;
import io.apicurio.datamodels.validation.IValidationSeverityRegistry;
import io.apicurio.datamodels.validation.ValidationProblem;
import io.apicurio.datamodels.validation.ValidationProblemSeverity;
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
import org.wso2.carbon.apimgt.spec.parser.definitions.asyncapi.AsyncApiParseOptions;
import org.wso2.carbon.apimgt.spec.parser.definitions.asyncapi.AsyncApiParserFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class AsyncApiParserUtil {

    private static final Log log = LogFactory.getLog(AsyncApiParserUtil.class);
    private static final String PATH_SEPARATOR = "/";

    @UsedByMigrationClient
    public static APIDefinitionValidationResponse validateAsyncAPISpecification(
            String schemaToBeValidated, boolean returnJSONContent) throws APIManagementException {

        log.debug("AsyncAPI definition validation has started");
        AsyncApiParseOptions options = new AsyncApiParseOptions();
        options.setDefaultAsyncApiParserVersion(true);
        AsyncApiParser asyncApiParser = AsyncApiParserFactory.getAsyncApiParser(getAsyncApiVersion(
                schemaToBeValidated), options);
        APIDefinitionValidationResponse validationResponse = asyncApiParser.validateAPIDefinition(schemaToBeValidated,
                returnJSONContent);
        log.debug("AsyncAPI definition validation is completed");
        final String asyncAPIKeyNotFound = "#: required key [asyncapi] not found";

        if (!validationResponse.isValid()) {
            for (ErrorHandler errorItem : validationResponse.getErrorItems()) {
                if (asyncAPIKeyNotFound.equals(errorItem.getErrorMessage())) {    //change it other way
                    addErrorToValidationResponse(validationResponse, "#: attribute [asyncapi] " +
                            "should be present");
                    return validationResponse;
                }
            }
        }

        return validationResponse;
    }

    public static APIDefinitionValidationResponse validateAsyncAPISpecification(
            String schemaToBeValidated, boolean returnJSONContent, AsyncApiParseOptions options) throws APIManagementException {

        log.debug("AsyncAPI definition validation has started");
        AsyncApiParser asyncApiParser = AsyncApiParserFactory.getAsyncApiParser(getAsyncApiVersion(
                schemaToBeValidated), options);
        APIDefinitionValidationResponse validationResponse = asyncApiParser.validateAPIDefinition(schemaToBeValidated,
                returnJSONContent);
        log.debug("AsyncAPI definition validation is completed");
        final String asyncAPIKeyNotFound = "#: required key [asyncapi] not found";

        if (!validationResponse.isValid()) {
            for (ErrorHandler errorItem : validationResponse.getErrorItems()) {
                if (asyncAPIKeyNotFound.equals(errorItem.getErrorMessage())) {    //change it other way
                    addErrorToValidationResponse(validationResponse, "#: attribute [asyncapi] " +
                            "should be present");
                    return validationResponse;
                }
            }
        }

        return validationResponse;
    }

    @Deprecated
    public static APIDefinitionValidationResponse validateAsyncAPISpecificationByURL(
            String url, HttpClient httpClient, boolean returnJSONContent) throws APIManagementException {

        APIDefinitionValidationResponse validationResponse = new APIDefinitionValidationResponse();

        try {
            HttpGet httpGet = new HttpGet(url);
            HttpResponse response = httpClient.execute(httpGet);

            if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
                ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
                Object obj = yamlReader.readValue(new URL(url), Object.class);
                ObjectMapper jsonWriter = new ObjectMapper();
                String json = jsonWriter.writeValueAsString(obj);
                AsyncApiParseOptions options = new AsyncApiParseOptions();
                options.setDefaultAsyncApiParserVersion(true);
                validationResponse = validateAsyncAPISpecification(json, returnJSONContent, options);
            } else {
                validationResponse.setValid(false);
                validationResponse.getErrorItems().add(ExceptionCodes.ASYNCAPI_URL_NO_200);
            }
        } catch (IOException e) {
            ErrorHandler errorHandler = ExceptionCodes.ASYNCAPI_URL_MALFORMED;
            log.error(errorHandler.getErrorDescription(), e); // log the error and continue
            // since this method is only intended to validate a definition

            validationResponse.setValid(false);
            validationResponse.getErrorItems().add(errorHandler);
        }

        return validationResponse;
    }

    public static APIDefinitionValidationResponse validateAsyncAPISpecificationByURL(
            String url, HttpClient httpClient, boolean returnJSONContent, AsyncApiParseOptions options) throws APIManagementException {

        APIDefinitionValidationResponse validationResponse = new APIDefinitionValidationResponse();

        try {
            HttpGet httpGet = new HttpGet(url);
            HttpResponse response = httpClient.execute(httpGet);

            if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
                ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
                Object obj = yamlReader.readValue(new URL(url), Object.class);
                ObjectMapper jsonWriter = new ObjectMapper();
                String json = jsonWriter.writeValueAsString(obj);
                validationResponse = validateAsyncAPISpecification(json, returnJSONContent, options);
            } else {
                validationResponse.setValid(false);
                validationResponse.getErrorItems().add(ExceptionCodes.ASYNCAPI_URL_NO_200);
            }
        } catch (IOException e) {
            ErrorHandler errorHandler = ExceptionCodes.ASYNCAPI_URL_MALFORMED;
            log.error(errorHandler.getErrorDescription(), e); // log the error and continue
            // since this method is only intended to validate a definition

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
            List<String> endpoints) {
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
            List<URITemplate> uriTemplates) {
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
            throw new APIManagementException("No Channels available for AsyncAPI version: "
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
        log.debug("AsyncAPI definition version : " +
                JsonUtil.getStringProperty(json, APISpecParserConstants.AsyncApi.ASYNC_API));
        return JsonUtil.getStringProperty(json, APISpecParserConstants.AsyncApi.ASYNC_API);
    }

    /**
     * This method will provide the respective Async API Document with data from based on the version
     *
     * @param version    String
     * @param definition String
     * @return AsyncApiDocument
     */
    public static AsyncApiDocument getFromAsyncApiDocument(String version, String definition) throws APIManagementException {

        try {
            Document document = Library.readDocumentFromJSONString(definition);
            if (document instanceof AsyncApiDocument) {
                return (AsyncApiDocument) document;
            }

            throw new APIManagementException("Unsupported or invalid AsyncAPI definition version: " + version,
                    ExceptionCodes.ERROR_READING_ASYNCAPI_SPECIFICATION
            );

        } catch (Exception e) {
            throw new APIManagementException("Error while reading AsyncAPI definition", e,
                    ExceptionCodes.ERROR_READING_ASYNCAPI_SPECIFICATION
            );
        }
    }

    /**
     * This method will extract the extension data from the Async Api Document
     *
     * @param definition String
     * @return Map<String, JsonNode>
     */
    public static Map<String, JsonNode> getExtensionFromAsyncApiDoc(String definition) throws APIManagementException {

        AsyncApiDocument asyncApiDocument = getFromAsyncApiDocument(getAsyncApiVersion(definition), definition);
        AsyncApiExtensible extensibleDoc = (AsyncApiExtensible) asyncApiDocument;
        return extensibleDoc.getExtensions();
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
     * This method will extract and set the OAuth Flow scopes for the Async Api Document based on the version
     *
     * @param securityScheme AsyncApiSecurityScheme
     * @param scopes         Map<String, String>
     * @param scopeBindings  Map<String, String>
     */
    public static void setAsyncApiOAuthFlowsScopes(AsyncApiSecurityScheme securityScheme, Map<String, String> scopes,
                                                   Map<String, String> scopeBindings) throws APIManagementException {

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
            throw new APIManagementException("Unsupported AsyncAPI OAuth Flow",
                    ExceptionCodes.ERROR_READING_ASYNCAPI_SPECIFICATION);
        }
    }

    /**
     * This method will create and provide the respective Async API Document with data from based on the version
     *
     * @param version String
     * @return AsyncApiDocument
     */
    public static AsyncApiDocument createAsyncApiDocument(String version) throws APIManagementException {
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
            throw new APIManagementException("Unsupported AsyncAPI version: " + version,
                    ExceptionCodes.ERROR_READING_ASYNCAPI_SPECIFICATION);
        }
    }

    /**
     * This method will create and provide the respective Async API Channel Item with data from based on the version
     *
     * @param channels String
     * @return AsyncApiDocument
     */
    public static AsyncApiChannelItem createChannelItem(AsyncApiChannels channels) throws APIManagementException {
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
            throw new APIManagementException("Unsupported AsyncAPI Channel",
                    ExceptionCodes.ERROR_READING_ASYNCAPI_SPECIFICATION);
        }
    }

    /**
     * This method will create and provide the respective Async API Channel with data from based on the version
     *
     * @param channels String
     * @return AsyncApiDocument
     */
    public static AsyncApi30Channel createChannel(AsyncApiChannels channels) throws APIManagementException {
        if (channels instanceof AsyncApi30Channels) {
            return ((AsyncApi30Channels) channels).createChannel();
        } else {
            throw new APIManagementException("Unsupported AsyncAPI Channel",
                    ExceptionCodes.ERROR_READING_ASYNCAPI_SPECIFICATION);
        }
    }

    /**
     * This method will return a list of channel items from the respective Async API document
     *
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
        if (parameterObj instanceof AsyncApiReferenceable) {
            return ((AsyncApiReferenceable) parameterObj).get$ref();
        }
        throw new IllegalArgumentException("Unsupported AsyncAPI Parameter type: " +
                (parameterObj != null ? parameterObj.getClass().getName() : "null")
        );
    }

    /**
     * This method will set AsyncApiServer for the respective Async API based on the version
     *
     * @param url    String
     * @param server AsyncApiServer
     */
    public static void setAsyncApiServer(String url, AsyncApiServer server) throws APIManagementException {
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
            String host;
            String pathname;

            if (url != null && !url.isEmpty()) {
                int schemeIdx = url.indexOf("://");
                int start = schemeIdx >= 0 ? schemeIdx + 3 : 0;
                int slashIdx = url.indexOf('/', start);

                if (slashIdx >= 0) {
                    host = url.substring(start, slashIdx).trim();
                    pathname = url.substring(slashIdx).trim();
                    if (pathname.isEmpty()) {
                        pathname = "/";
                    }
                } else {
                    host = url.substring(start).trim();
                    pathname = "/";
                }

                // If host ends up empty (e.g. "file:///path" or malformed input), fall back to the raw url
                if (host == null || host.isEmpty()) {
                    host = url.trim();
                }
            } else {
                host = "";
                pathname = "/";
            }

            ((AsyncApi30Server) server).setHost(host);
            ((AsyncApi30Server) server).setPathname(pathname);
        } else {
            throw new APIManagementException("Unsupported AsyncAPI Channel",
                    ExceptionCodes.ERROR_READING_ASYNCAPI_SPECIFICATION);
        }
    }

    /**
     * Utility method to safely validate the Async API content
     */
    public static boolean validateAsyncApiContent(String apiDefinition, List<String> errorMessages) {
        // Parse the incoming JSON into a Jackson ObjectNode
        ObjectNode originalParsed;
        try {
            originalParsed = (ObjectNode) JsonUtil.parseJSON(apiDefinition);
        } catch (Exception e) {
            String msg = "Invalid AsyncAPI syntax: " + e.getMessage();
            log.error(msg, e);
            errorMessages.add(msg);
            return false;
        }

        //If parsing succeeded, create Apicurio Document and run model validation
        if (originalParsed != null) {
            Document doc = Library.readDocument(originalParsed);
            if (doc == null || doc.root() == null) {
                String msg = "Unable to parse AsyncAPI definition into Apicurio Document model.";
                log.error(msg);
                errorMessages.add(msg);
                return false;
            } else {
                // Log model type (ModelType has a useful toString)
                log.debug("Parsed model type: " + doc.root().modelType());

                // Validate using Apicurio validation rules
                IValidationSeverityRegistry severityRegistry = new DefaultSeverityRegistry();
                List<ValidationProblem> problems =
                        Library.validate(doc, severityRegistry);

                if (problems == null || problems.isEmpty()) {
                    log.debug("No validation problems found.");
                    return true;
                } else {
                    // Format problems into a readable string
                    String formatted = formatProblems(problems);
                    log.debug("Validation Problems:\n" + formatted);

                    // Determine if there are any high or medium severity problems
                    // This can also include ValidationProblemSeverity low as well
                    boolean hasErrors = problems.stream()
                            .anyMatch(p -> p.severity == ValidationProblemSeverity.high
                                    || p.severity == ValidationProblemSeverity.medium);

                    // Collect all problem messages into validationErrorMessages
                    for (ValidationProblem problem : problems) {
                        errorMessages.add(problem.message);
                    }

                    if (hasErrors) {
                        return false;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Format the list of problems as a string.
     *
     * @param problems Validation problems
     */
    private static String formatProblems(List<ValidationProblem> problems) {
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

}

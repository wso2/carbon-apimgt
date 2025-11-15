package org.wso2.carbon.apimgt.spec.parser.definitions;

import io.apicurio.datamodels.Library;
import io.apicurio.datamodels.models.asyncapi.*;
import io.apicurio.datamodels.models.asyncapi.v26.AsyncApi26Channels;
import io.apicurio.datamodels.models.asyncapi.v26.AsyncApi26Document;
import io.apicurio.datamodels.models.asyncapi.v26.AsyncApi26Server;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIDefinitionValidationResponse;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.UsedByMigrationClient;
import org.wso2.carbon.apimgt.api.dto.KeyManagerConfigurationDTO;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.SwaggerData;
import org.wso2.carbon.apimgt.api.model.URITemplate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component(
        name = "wso2.async.definition.parser.component",
        immediate = true,
        service = APIDefinition.class
)
public abstract class AsyncApiParser extends APIDefinition {

    private static final Log log = LogFactory.getLog(AsyncApiParser.class);
    private List<String> otherSchemes;
    public List<String> getOtherSchemes() {
        return otherSchemes;
    }
    public void setOtherSchemes(List<String> otherSchemes) {
        this.otherSchemes = otherSchemes;
    }

    @Override
    public Map<String, Object> generateExample(String apiDefinition) throws APIManagementException{
        return null;
    }

    @Override
    public Set<URITemplate> getURITemplates(String resourceConfigsJSON) throws APIManagementException {
        return getURITemplates(resourceConfigsJSON, true);
    }

    public abstract Set<URITemplate> getURITemplates(String apiDefinition, boolean includePublish)
            throws APIManagementException;

    @Override
    public abstract Set<Scope> getScopes(String resourceConfigsJSON) throws APIManagementException;

    public String generateAPIDefinitionForBackendAPI(SwaggerData swaggerData, String oasDefinition) {
        return null;
    }

    @Override
    public String generateAPIDefinition(SwaggerData swaggerData) throws APIManagementException {
        return null;
    }

    @Override
    public String generateAPIDefinition(SwaggerData swaggerData, String swagger) throws APIManagementException {
        return null;
    }

    @Override
    public APIDefinitionValidationResponse validateAPIDefinition(String apiDefinition, String url, boolean returnJsonContent) throws APIManagementException {
        return null;
    }

    @Override
    public abstract APIDefinitionValidationResponse validateAPIDefinition(String apiDefinition, boolean returnJsonContent) throws APIManagementException;

    @Override
    public String populateCustomManagementInfo(String oasDefinition, SwaggerData swaggerData) throws APIManagementException {
        return null;
    }

    @Override
    public String getOASDefinitionForStore(API api, String oasDefinition, Map<String, String> hostsWithSchemes,
                                           KeyManagerConfigurationDTO keyManagerConfigurationDTO)
            throws APIManagementException {
        return null;
    }

    @Override
    public String getOASDefinitionForStore(APIProduct product, String oasDefinition,
                                           Map<String, String> hostsWithSchemes,
                                           KeyManagerConfigurationDTO keyManagerConfigurationDTO)
            throws APIManagementException {
        return null;
    }

    @Override
    public String getOASDefinitionForPublisher(API api, String oasDefinition) throws APIManagementException {
        return null;
    }

    @Override
    public String getOASVersion(String oasDefinition) throws APIManagementException {
        return null;
    }

    @Override
    public String getOASDefinitionWithTierContentAwareProperty(String oasDefinition, List<String> contentAwareTiersList, String apiLevelTier) throws APIManagementException {
        return null;
    }

    @Override
    public String processOtherSchemeScopes(String resourceConfigsJSON) throws APIManagementException {
        return null;
    }

    @Override
    public API setExtensionsToAPI(String swaggerContent, API api) throws APIManagementException {
        return null;
    }

    @Override
    public String copyVendorExtensions(String existingOASContent, String updatedOASContent) throws APIManagementException {
        return null;
    }

    @Override
    public String processDisableSecurityExtension(String swaggerContent) throws APIManagementException{
        return null;
    }

    @Override
    public String getVendorFromExtension(String swaggerContent) {
        return APISpecParserConstants.WSO2_GATEWAY_ENVIRONMENT;
    }

    @Override
    public String injectMgwThrottlingExtensionsToDefault(String swaggerContent) throws APIManagementException{
        return null;
    }

    @UsedByMigrationClient
    public abstract String generateAsyncAPIDefinition(API api) throws APIManagementException;

    /**
     * Configure Aai server from endpoint configurations
     *
     * @param api               API
     * @param asyncDocument    Async API Document
     * @param endpointConfig   Endpoint configuration
     * @param serverName        Name of the server
     * @param endpoint          Endpoint to be configured
     * @return Configured AaiServer
     */
    private AsyncApiServer getAaiServer(API api, AsyncApi26Document asyncDocument, JSONObject endpointConfig, String serverName,
                                        String endpoint, AsyncApiServers servers) {

        AsyncApi26Server server = (AsyncApi26Server) servers.createServer();
        server.setUrl(endpointConfig.getJSONObject(endpoint).getString(APISpecParserConstants.API_DATA_URL));
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
    public abstract String getAsyncApiDefinitionForStore(API api, String asyncAPIDefinition, Map<String, String> hostsWithSchemes)
            throws APIManagementException;

    public abstract String updateAsyncAPIDefinition(String oldDefinition, API apiToUpdate);

    public abstract Map<String,String> buildWSUriMapping(String apiDefinition);

    /**
     * Get available transport protocols for the Async API
     *
     * @param definition Async API Definition
     * @return List<String> List of available transport protocols
     * @throws APIManagementException If the async env configuration if not provided properly
     */
    public static List<String> getTransportProtocolsForAsyncAPI(String definition) throws APIManagementException {
        AsyncApiDocument aaiDocument = (AsyncApiDocument) Library.readDocumentFromJSONString(definition);
        HashSet<String> asyncTransportProtocols = new HashSet<>();
        for (AsyncApiChannelItem channel : ((AsyncApi26Channels) aaiDocument.getChannels()).getItems()) {
            asyncTransportProtocols.addAll(getProtocols(channel));
        }
        ArrayList<String> asyncTransportProtocolsList = new ArrayList<>(asyncTransportProtocols);
        return asyncTransportProtocolsList;
    }

    /**
     * Get the transport protocols
     *
     * @param channel AaiChannelItem to get protocol
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
     * @param bindings AaiOperationBindings to get protocols
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

    @Override
    public String getType() {
        return APISpecParserConstants.WSO2_GATEWAY_ENVIRONMENT;
    }

    @Override
    public Set<URITemplate> generateMCPTools(String backendApiDefinition, APIIdentifier refApiId, String backendId,
                                             String mcpSubtype, Set<URITemplate> uriTemplates) {

        throw new UnsupportedOperationException("MCP tool generation is not supported for Async API definitions.");
    }

    @Override
    public Set<URITemplate> updateMCPTools(String backendApiDefinition, APIIdentifier refApiId, String backendId,
                                           String mcpSubtype, Set<URITemplate> uriTemplates) {

        throw new UnsupportedOperationException("MCP tool generation is not supported for Async API definitions.");
    }
}

/*
 * Copyright WSO2 Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.impl;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.gateway.GatewayContentDTO;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.APIProductIdentifier;
import org.wso2.carbon.apimgt.api.model.Label;
import org.wso2.carbon.apimgt.gateway.dto.stub.APIData;
import org.wso2.carbon.apimgt.gateway.dto.stub.ResourceData;
import org.wso2.carbon.apimgt.impl.certificatemgt.exceptions.CertificateManagementException;
import org.wso2.carbon.apimgt.impl.dao.CertificateMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.Environment;
import org.wso2.carbon.apimgt.impl.dto.GatewayArtifactSynchronizerProperties;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.ArtifactSaver;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.exception.ArtifactSynchronizerException;
import org.wso2.carbon.apimgt.impl.importexport.APIImportExportException;
import org.wso2.carbon.apimgt.impl.importexport.ExportFormat;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.notifier.events.APIEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.DeployAPIInGatewayEvent;
import org.wso2.carbon.apimgt.impl.recommendationmgt.RecommendationEnvironment;
import org.wso2.carbon.apimgt.impl.recommendationmgt.RecommenderDetailsExtractor;
import org.wso2.carbon.apimgt.impl.recommendationmgt.RecommenderEventPublisher;
import org.wso2.carbon.apimgt.impl.utils.APIGatewayAdminClient;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.utils.LocalEntryAdminClient;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class APIGatewayManager {

    private static final Log log = LogFactory.getLog(APIGatewayManager.class);
    private boolean debugEnabled = log.isDebugEnabled();
    private static APIGatewayManager instance;

    private Map<String, Environment> environments;
    private RecommendationEnvironment recommendationEnvironment;
    private GatewayArtifactSynchronizerProperties gatewayArtifactSynchronizerProperties;
    private ArtifactSaver artifactSaver;
    private boolean saveArtifactsToStorage = false;

    private static final String PRODUCT_PREFIX = "prod";

    private APIGatewayManager() {
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                .getAPIManagerConfiguration();
        environments = config.getApiGatewayEnvironments();
        this.recommendationEnvironment = config.getApiRecommendationEnvironment();
        this.gatewayArtifactSynchronizerProperties = config.getGatewayArtifactSynchronizerProperties();
        this.artifactSaver = ServiceReferenceHolder.getInstance().getArtifactSaver();
        if (artifactSaver != null && config.getGatewayArtifactSynchronizerProperties().isSaveArtifactsEnabled()){
            this.saveArtifactsToStorage = true;
        }
    }

    public synchronized static APIGatewayManager getInstance() {
        if (instance == null) {
            instance = new APIGatewayManager();
        }
        return instance;
    }

    private void sendDeploymentEvent(API api, String tenantDomain, Set<String> publishedGateways) {

        APIIdentifier apiIdentifier = api.getId();
        DeployAPIInGatewayEvent
                deployAPIInGatewayEvent = new DeployAPIInGatewayEvent(UUID.randomUUID().toString(),
                System.currentTimeMillis(), APIConstants.EventType.DEPLOY_API_IN_GATEWAY.name(), tenantDomain,
                api.getUUID(), publishedGateways,apiIdentifier.getName(),apiIdentifier.getVersion(),
                apiIdentifier.getProviderName(),api.getType());
        APIUtil.sendNotification(deployAPIInGatewayEvent, APIConstants.NotifierType.GATEWAY_PUBLISHED_API.name());
        if (debugEnabled) {
            log.debug("Event sent to Gateway with eventID " + deployAPIInGatewayEvent.getEventId() + " for api "
                    + "with apiID " + api + " at " + deployAPIInGatewayEvent.getTimeStamp());
        }
    }

    private void sendDeploymentEvent(APIProduct api, String tenantDomain, Set<String> publishedGateways) {

        APIProductIdentifier apiIdentifier = api.getId();
        DeployAPIInGatewayEvent
                deployAPIInGatewayEvent = new DeployAPIInGatewayEvent(UUID.randomUUID().toString(),
                System.currentTimeMillis(), APIConstants.EventType.DEPLOY_API_IN_GATEWAY.name(), tenantDomain,
                api.getUuid(), publishedGateways, apiIdentifier.getName(), apiIdentifier.getVersion(),
                PRODUCT_PREFIX, api.getType());
        APIUtil.sendNotification(deployAPIInGatewayEvent, APIConstants.NotifierType.GATEWAY_PUBLISHED_API.name());
        if (debugEnabled) {
            log.debug("Event sent to Gateway with eventID " + deployAPIInGatewayEvent.getEventId() + " for api "
                    + "with apiID " + api + " at " + deployAPIInGatewayEvent.getTimeStamp());
        }
    }

    /**
     * Publishes an API to a given environment
     *
     * @param environment               - Gateway environment
     * @param api                       - The API to be published
     * @param builder                   - The template builder
     * @param tenantDomain              - Tenant Domain of the publisher
     * @param isGatewayDefinedAsALabel  - Whether the environment is from a label or not. If it is from a label,
     *                                  directly publishing to gateway will not happen
     * @param failedGatewaysMap         - This map will be updated with the gateway if the publishing failed
     * @return failedEnvironmentsMap
     */
    private Map<String, String> publishAPIToGatewayEnvironment(Environment environment, API api,
            APITemplateBuilder builder,
            String tenantDomain, boolean isGatewayDefinedAsALabel,
            Set<String> publishedGateways,
            Map<String,String> failedGatewaysMap) {

        long startTime;
        long endTime;
        long startTimePublishToGateway = System.currentTimeMillis();

        GatewayAPIDTO gatewayAPIDTO;
        try {
            APIGatewayAdminClient client;
            startTime = System.currentTimeMillis();
            if (!APIConstants.APITransportType.WS.toString().equals(api.getType())) {
                gatewayAPIDTO = createAPIGatewayDTOtoPublishAPI(environment, api, builder, tenantDomain);
                if (gatewayAPIDTO == null) {
                    return failedGatewaysMap;
                } else {
                    if (gatewayArtifactSynchronizerProperties.isPublishDirectlyToGatewayEnabled()) {
                        if (!isGatewayDefinedAsALabel) {
                            client = new APIGatewayAdminClient(environment);
                            client.deployAPI(gatewayAPIDTO);
                        }
                    }

                    if (saveArtifactsToStorage) {
                        artifactSaver.saveArtifact(new Gson().toJson(gatewayAPIDTO), environment.getName(),
                                APIConstants.GatewayArtifactSynchronizer.GATEWAY_INSTRUCTION_PUBLISH);
                        publishedGateways.add(environment.getName());
                        if (debugEnabled) {
                            log.debug(gatewayAPIDTO.getName() + " details saved to the DB");
                        }
                    }
                }
            } else {
                client = new APIGatewayAdminClient(environment);
                deployWebsocketAPI(api, client, isGatewayDefinedAsALabel, publishedGateways, environment);
            }
            endTime = System.currentTimeMillis();
            if (debugEnabled) {
                log.debug("Publishing API (if the API does not exist in the Gateway) took " +
                        (endTime - startTime) / 1000 + "  seconds");
            }
        } catch (AxisFault axisFault) {
                /*
                didn't throw this exception to handle multiple gateway publishing
                if gateway is unreachable we collect that environments into map with issue and show on popup in ui
                therefore this didn't break the gateway publishing if one gateway unreachable
                 */
            failedGatewaysMap.put(environment.getName(), axisFault.getMessage());
            log.error("Error occurred when publish to gateway " + environment.getName(), axisFault);
        } catch (APIManagementException | JSONException ex) {
            log.error("Error occurred deploying sequences on " + environment.getName(), ex);
            failedGatewaysMap.put(environment.getName(), ex.getMessage());
        } catch (CertificateManagementException ex) {
            log.error("Error occurred while adding/updating client certificate in " + environment.getName(), ex);
            failedGatewaysMap.put(environment.getName(), ex.getMessage());
        } catch (APITemplateException | XMLStreamException e) {
            log.error("Error occurred while Publishing API directly to Gateway", e);
            failedGatewaysMap.put(environment.getName(), e.getMessage());
        } catch (ArtifactSynchronizerException e) {
            failedGatewaysMap.put(environment.getName(), e.getMessage());
            log.error("Error occurred while saving API artifacts to the Storage");
        }
        long endTimePublishToGateway = System.currentTimeMillis();
        if (debugEnabled) {
            log.debug("Publishing to gateway : " + environment.getName() + " total time taken : " +
                    (endTimePublishToGateway - startTimePublishToGateway) / 1000 + "  seconds");
        }
        return failedGatewaysMap;
    }

    /**
     * Create a DTO object required to publish API in to a given gateway environment
     *
     * @param environment           - Gateway environment
     * @param api                   - The API to be published
     * @param builder               - The template builder
     * @param tenantDomain          - Tenant Domain of the publisher
     * @return DTO object with API artifacts
     */
    private GatewayAPIDTO createAPIGatewayDTOtoPublishAPI(Environment environment, API api, APITemplateBuilder builder,
            String tenantDomain)
            throws APIManagementException, CertificateManagementException, APITemplateException, XMLStreamException {

        GatewayAPIDTO gatewayAPIDTO = new GatewayAPIDTO();
        gatewayAPIDTO.setName(api.getId().getName());
        gatewayAPIDTO.setVersion(api.getId().getVersion());
        gatewayAPIDTO.setProvider(api.getId().getProviderName());
        gatewayAPIDTO.setApiId(api.getUuid());
        gatewayAPIDTO.setTenantDomain(tenantDomain);
        gatewayAPIDTO.setOverride(true);

        String definition;

        if (api.getType() != null && APIConstants.APITransportType.GRAPHQL.toString().equals(api.getType())) {
            //Build schema with additional info
            GraphqlComplexityInfo graphqlComplexityInfo = APIUtil.getComplexityDetails(api);
            GraphQLSchemaDefinition schemaDefinition = new GraphQLSchemaDefinition();
            definition = schemaDefinition.buildSchemaWithAdditionalInfo(api, graphqlComplexityInfo);
            gatewayAPIDTO.setLocalEntriesToBeRemove(addStringToList(api.getUUID() + "_graphQL",
                    gatewayAPIDTO.getLocalEntriesToBeRemove()));
            GatewayContentDTO graphqlLocalEntry = new GatewayContentDTO();
            graphqlLocalEntry.setName(api.getUUID() + "_graphQL");
            graphqlLocalEntry.setContent("<localEntry key=\"" + api.getUUID() + "_graphQL" + "\">" +
                    definition + "</localEntry>");
            gatewayAPIDTO.setLocalEntriesToBeAdd(addGatewayContentToList(graphqlLocalEntry,
                    gatewayAPIDTO.getLocalEntriesToBeAdd()));
            Set<URITemplate> uriTemplates = new HashSet<>();
            URITemplate template = new URITemplate();
            template.setAuthType("Any");
            template.setHTTPVerb("POST");
            template.setHttpVerbs("POST");
            template.setUriTemplate("/*");
            uriTemplates.add(template);
            api.setUriTemplates(uriTemplates);
        } else if (api.getType() != null && (APIConstants.APITransportType.HTTP.toString().equals(api.getType())
                || APIConstants.API_TYPE_SOAP.equals(api.getType())
                || APIConstants.API_TYPE_SOAPTOREST.equals(api.getType()))) {
            definition = api.getSwaggerDefinition();
            gatewayAPIDTO.setLocalEntriesToBeRemove(addStringToList(api.getUUID(),
                    gatewayAPIDTO.getLocalEntriesToBeRemove()));

            GatewayContentDTO apiLocalEntry = new GatewayContentDTO();
            apiLocalEntry.setName(api.getUUID());
            apiLocalEntry.setContent("<localEntry key=\"" + api.getUUID() + "\">" +
                    definition.replaceAll("&(?!amp;)", "&amp;").
                            replaceAll("<", "&lt;").replaceAll(">", "&gt;")
                    + "</localEntry>");
            gatewayAPIDTO.setLocalEntriesToBeAdd(addGatewayContentToList(apiLocalEntry,
                    gatewayAPIDTO.getLocalEntriesToBeAdd()));
        }
        // Retrieve ga-config from the registry and publish to gateway as a local entry
        addGAConfigLocalEntry(gatewayAPIDTO, tenantDomain);

        // If the API exists in the Gateway and If the Gateway type is 'production' and a production url has not been
        // specified Or if the Gateway type is 'sandbox' and a sandbox url has not been specified

        if ((APIConstants.GATEWAY_ENV_TYPE_PRODUCTION.equals(environment.getType())
                && !APIUtil.isProductionEndpointsExists(api.getEndpointConfig()))
                || (APIConstants.GATEWAY_ENV_TYPE_SANDBOX.equals(environment.getType())
                && !APIUtil.isSandboxEndpointsExists(api.getEndpointConfig()))) {
            if (debugEnabled) {
                log.debug("Not adding API to environment " + environment.getName() + " since its endpoint URL "
                        + "cannot be found");
            }
            return null;
        }

        setCustomSequencesToBeRemoved(api, gatewayAPIDTO);
        setClientCertificatesToBeRemoved(api, tenantDomain, gatewayAPIDTO);
        setEndpointsToBeRemoved(api, gatewayAPIDTO);
        setAPIFaultSequencesToBeAdded(api, tenantDomain, gatewayAPIDTO);
        setCustomSequencesToBeAdded(api, tenantDomain, gatewayAPIDTO);
        setClientCertificatesToBeAdded(api, tenantDomain, gatewayAPIDTO);

        //Add the API
        if (APIConstants.IMPLEMENTATION_TYPE_INLINE.equalsIgnoreCase(api.getImplementation())) {
            String prototypeScriptAPI = builder.getConfigStringForPrototypeScriptAPI(environment);
            gatewayAPIDTO.setApiDefinition(prototypeScriptAPI);
        } else if (APIConstants.IMPLEMENTATION_TYPE_ENDPOINT.equalsIgnoreCase(api.getImplementation())) {
            String apiConfig = builder.getConfigStringForTemplate(environment);
            gatewayAPIDTO.setApiDefinition(apiConfig);
            JSONObject endpointConfig = new JSONObject(api.getEndpointConfig());
            if (!endpointConfig.get(APIConstants.API_ENDPOINT_CONFIG_PROTOCOL_TYPE)
                    .equals(APIConstants.ENDPOINT_TYPE_AWSLAMBDA)) {
                addEndpoints(api, builder, gatewayAPIDTO);
            }
        }

        if (api.isDefaultVersion()) {
            String defaultAPIConfig = builder.getConfigStringForDefaultAPITemplate(api.getId().getVersion());
            gatewayAPIDTO.setDefaultAPIDefinition(defaultAPIConfig);
        }
        setSecureVaultPropertyToBeAdded(api, gatewayAPIDTO);
        return gatewayAPIDTO;
    }


    /**
     * Create an environment object from a label
     *
     * @param label - Label object
     * @return environment
     */
    private Environment getEnvironmentFromLabel(Label label) {

        //Environment type is set as hybrid
        Environment environment = new Environment();
        environment.setName(label.getName());
        environment.setDefault(true);
        environment.setDescription(label.getDescription());
        if (!label.getAccessUrls().isEmpty()) {
            environment.setServerURL(label.getAccessUrls().get(0));
            if (label.getAccessUrls().size()>1) {
                environment.setApiGatewayEndpoint(label.getAccessUrls().get(1));
            }
            if (label.getAccessUrls().size()>2) {
                environment.setWebsocketGatewayEndpoint(label.getAccessUrls().get(2));
            }
        }
        return environment;
    }

    private void addGAConfigLocalEntry(GatewayAPIDTO gatewayAPIDTO, String tenantDomain)
            throws APIManagementException {

        String content = APIUtil.getGAConfigFromRegistry(tenantDomain);

        if (StringUtils.isNotEmpty(content)) {
            GatewayContentDTO apiLocalEntry = new GatewayContentDTO();
            apiLocalEntry.setName(APIConstants.GA_CONF_KEY);
            apiLocalEntry.setContent("<localEntry key=\"" + APIConstants.GA_CONF_KEY + "\">"
                    + content + "</localEntry>");
            gatewayAPIDTO.setLocalEntriesToBeAdd(addGatewayContentToList(apiLocalEntry,
                    gatewayAPIDTO.getLocalEntriesToBeAdd()));
        }
    }

    private GatewayContentDTO[] addGatewayContentToList(GatewayContentDTO gatewayContentDTO,
            GatewayContentDTO[] gatewayContents) {

        if (gatewayContents == null) {
            return new GatewayContentDTO[]{gatewayContentDTO};
        } else {
            Set<GatewayContentDTO> gatewayContentDTOList = new HashSet<>();
            Collections.addAll(gatewayContentDTOList, gatewayContents);
            gatewayContentDTOList.add(gatewayContentDTO);
            return gatewayContentDTOList.toArray(new GatewayContentDTO[gatewayContentDTOList.size()]);
        }
    }

    private String[] addStringToList(String key, String[] keys) {

        if (keys == null) {
            return new String[]{key};
        } else {
            Set<String> keyList = new HashSet<>();
            Collections.addAll(keyList, keys);
            keyList.add(key);
            return keyList.toArray(new String[keyList.size()]);
        }
    }

    /**
     * Returns the defined endpoint types of the in the publisher
     *
     * @param api API that the endpoint/s belong
     * @return ArrayList containing defined endpoint types
     */
    public ArrayList<String> getEndpointType(API api) {
        ArrayList<String> arrayList = new ArrayList<>();
        if (APIUtil.isProductionEndpointsExists(api.getEndpointConfig()) &&
                !APIUtil.isSandboxEndpointsExists(api.getEndpointConfig())) {
            arrayList.add(APIConstants.API_DATA_PRODUCTION_ENDPOINTS);
        } else if (APIUtil.isSandboxEndpointsExists(api.getEndpointConfig()) &&
                !APIUtil.isProductionEndpointsExists(api.getEndpointConfig())) {
            arrayList.add(APIConstants.API_DATA_SANDBOX_ENDPOINTS);
        } else {
            arrayList.add(APIConstants.API_DATA_PRODUCTION_ENDPOINTS);
            arrayList.add(APIConstants.API_DATA_SANDBOX_ENDPOINTS);
        }
        return arrayList;
    }

    public Map<String, String> updateLocalEntry(APIProduct apiProduct, String tenantDomain) {

        Map<String, String> failedEnvironmentsMap = new HashMap<>();

        for (String environmentName : apiProduct.getEnvironments()) {

            Environment environment = environments.get(environmentName);
            try {
                LocalEntryAdminClient localEntryAdminClient = new LocalEntryAdminClient(environment, tenantDomain);

                String definition = apiProduct.getDefinition();
                localEntryAdminClient.deleteEntry(apiProduct.getUuid());
                localEntryAdminClient.addLocalEntry("<localEntry key=\"" + apiProduct.getUuid() + "\">" +
                        definition.replaceAll("&(?!amp;)", "&amp;").
                                replaceAll("<", "&lt;").replaceAll(">", "&gt;")
                        + "</localEntry>");
            } catch (AxisFault e) {
                failedEnvironmentsMap.put(environmentName, e.getMessage());
                log.error("Error occurred when publish to gateway " + environmentName, e);
            }
        }

        return failedEnvironmentsMap;
    }

    private void sendUnDeploymentEvent(API api, String tenantDomain, Set<String> removedGateways) {
        APIIdentifier apiIdentifier = api.getId();

        DeployAPIInGatewayEvent
                deployAPIInGatewayEvent = new DeployAPIInGatewayEvent(UUID.randomUUID().toString(),
                System.currentTimeMillis(), APIConstants.EventType.REMOVE_API_FROM_GATEWAY.name(), tenantDomain,
                api.getUUID(), removedGateways,apiIdentifier.getName(),apiIdentifier.getVersion(),
                apiIdentifier.getProviderName(), api.getType());
        APIUtil.sendNotification(deployAPIInGatewayEvent,
                APIConstants.NotifierType.GATEWAY_PUBLISHED_API.name());

    }

    private void sendUnDeploymentEvent(APIProduct apiProduct, String tenantDomain, Set<String> removedGateways,
                                       Set<API> associatedAPIs) {

        APIProductIdentifier apiProductIdentifier = apiProduct.getId();
        Set<APIEvent> apiEvents = transformAPIToAPIEvent(associatedAPIs);
        DeployAPIInGatewayEvent
                deployAPIInGatewayEvent = new DeployAPIInGatewayEvent(UUID.randomUUID().toString(),
                System.currentTimeMillis(), APIConstants.EventType.REMOVE_API_FROM_GATEWAY.name(), tenantDomain,
                apiProduct.getUuid(), removedGateways, apiProductIdentifier.getName(),
                apiProductIdentifier.getVersion(), PRODUCT_PREFIX, APIConstants.API_PRODUCT, apiEvents);
        APIUtil.sendNotification(deployAPIInGatewayEvent, APIConstants.NotifierType.GATEWAY_PUBLISHED_API.name());
    }

    public Map<String, String> removeDefaultAPIFromGateway(API api, String tenantDomain) {

        Map<String, String> failedEnvironmentsMap = new HashMap<String, String>(0);
        LocalEntryAdminClient localEntryAdminClient;
        String localEntryUUId = api.getUUID();
        if (api.getEnvironments() != null) {
            for (String environmentName : api.getEnvironments()) {
                try {
                    Environment environment = environments.get(environmentName);
                    APIGatewayAdminClient client = new APIGatewayAdminClient(environment);
                    APIIdentifier id = api.getId();
                    if (client.getDefaultApi(tenantDomain, id) != null) {
                        if (debugEnabled) {
                            log.debug("Removing Default API " + api.getId().getApiName() + " From environment " +
                                    environment.getName());
                        }
                        client.deleteDefaultApi(tenantDomain, api.getId());
                    }
                    if (APIConstants.APITransportType.GRAPHQL.toString().equals(api.getType())) {
                        localEntryUUId = localEntryUUId + APIConstants.GRAPHQL_LOCAL_ENTRY_EXTENSION;
                    }
                    localEntryAdminClient = new LocalEntryAdminClient(environment, tenantDomain);
                    localEntryAdminClient.deleteEntry(localEntryUUId);
                } catch (AxisFault axisFault) {
                    /*
                didn't throw this exception to handle multiple gateway publishing
                if gateway is unreachable we collect that environments into map with issue and show on popup in ui
                therefore this didn't break the gateway unpublisihing if one gateway unreachable
                 */
                    log.error("Error occurred when removing default api from gateway " + environmentName, axisFault);
                    failedEnvironmentsMap.put(environmentName, axisFault.getMessage());
                }
            }
        }
        return failedEnvironmentsMap;
    }

    /**
     * Checks whether the API has been published.
     *
     * @param api          - The API to be checked.
     * @param tenantDomain - Tenant Domain of the publisher
     * @return True if the API is available in at least one Gateway. False if available in none.
     */
    public boolean isAPIPublished(API api, String tenantDomain) throws APIManagementException {

        if (gatewayArtifactSynchronizerProperties.isPublishDirectlyToGatewayEnabled()) {
            for (Environment environment : environments.values()) {
                try {
                    APIGatewayAdminClient client = new APIGatewayAdminClient(environment);
                    // If the API exists in at least one environment, consider as
                    // published and return true.
                    APIIdentifier id = api.getId();
                    if (client.getApi(tenantDomain, id) != null) {
                        return true;
                    }
                } catch (AxisFault axisFault) {
                    /*
                    didn't throw this exception to check api available in all the environments
                    therefore we didn't throw exception to avoid if gateway unreachable affect
                    */
                    if (!APIConstants.CREATED.equals(api.getStatus())) {
                        log.error("Error occurred when check api is published on gateway" + environment.getName(),
                                axisFault);
                    }
                }
            }
        } else {
            if (saveArtifactsToStorage) {
                try {
                    return artifactSaver.isAPIPublished(api.getUUID(), "CURRENT");
                } catch (ArtifactSynchronizerException e) {
                    throw new APIManagementException("Error while check existence of api in gateway", e);
                }
            }
        }
        return false;
    }

    /**
     * Get the endpoint Security type of the published API
     *
     * @param api          - The API to be checked.
     * @param tenantDomain - Tenant Domain of the publisher
     * @return Endpoint security type; Basic or Digest
     */
    public String getAPIEndpointSecurityType(API api, String tenantDomain) throws APIManagementException {

        for (Environment environment : environments.values()) {
            try {
                APIGatewayAdminClient client = new APIGatewayAdminClient(environment);
                APIIdentifier id = api.getId();
                APIData apiData = client.getApi(tenantDomain, id);
                if (apiData != null) {
                    ResourceData[] resourceData = apiData.getResources();
                    for (ResourceData resource : resourceData) {
                        if (resource != null && resource.getInSeqXml() != null) {
                            if(resource.getInSeqXml().contains("DigestAuthMediator")) {
                                return APIConstants.APIEndpointSecurityConstants.DIGEST_AUTH;
                            } else if(resource.getInSeqXml().contains("OAuthMediator")) {
                                return APIConstants.APIEndpointSecurityConstants.OAUTH;
                            }
                        }
                    }
                }
            } catch (AxisFault axisFault) {
                // didn't throw this exception to check api available in all the environments
                // therefore we didn't throw exception to avoid if gateway unreachable affect
                if (!APIConstants.CREATED.equals(api.getStatus())) {
                    log.error("Error occurred when check api endpoint security type on gateway"
                            + environment.getName(), axisFault);
                }
            }
        }
        return APIConstants.APIEndpointSecurityConstants.BASIC_AUTH;
    }

    /**
     * To update the database instance with the successfully removed client certificates from teh gateway.
     *
     * @param api          Relevant API related with teh removed certificate.
     * @param tenantDomain Tenant domain of the API.
     */
    private void updateRemovedClientCertificates(API api, String tenantDomain) {

        try {
            CertificateMgtDAO.getInstance().updateRemovedCertificatesFromGateways(api.getId(),
                    APIUtil.getTenantIdFromTenantDomain(tenantDomain));
            /* The flow does not need to be blocked, as this failure do not related with updating client certificates
             in gateway, rather updating in database. There is no harm in database having outdated certificate
             information.*/
        } catch (CertificateManagementException e) {
            log.error("Certificate Management Exception while trying to update the remove certificate from gateways "
                    + "for the api " + api.getId() + " for the tenant domain " + tenantDomain, e);
        }
    }

    /**
     * To undeploy the client certificates from the gateway environment.
     *
     * @param api          Relevant API particular certificate is related with.
     * @param tenantDomain Tenant domain of the API.
     * @throws CertificateManagementException Certificate Management Exception.
     */
    private void setClientCertificatesToBeRemoved(API api, String tenantDomain, GatewayAPIDTO gatewayAPIDTO)
            throws CertificateManagementException {

        if (!CertificateManagerImpl.getInstance().isClientCertificateBasedAuthenticationConfigured()) {
            return;
        }
        int tenantId = APIUtil.getTenantIdFromTenantDomain(tenantDomain);
        List<ClientCertificateDTO> clientCertificateDTOList = CertificateMgtDAO.getInstance()
                .getClientCertificates(tenantId, null, api.getId());
        if (clientCertificateDTOList != null) {
            for (ClientCertificateDTO clientCertificateDTO : clientCertificateDTOList) {
                gatewayAPIDTO.setClientCertificatesToBeRemove(addStringToList(clientCertificateDTO.getAlias() + "_" +
                        tenantId, gatewayAPIDTO.getLocalEntriesToBeRemove()));
            }
        }
        List<String> aliasList = CertificateMgtDAO.getInstance()
                .getDeletedClientCertificateAlias(api.getId(), tenantId);
        for (String alias : aliasList) {
            gatewayAPIDTO.setClientCertificatesToBeRemove(addStringToList(alias + "_" + tenantId,
                    gatewayAPIDTO.getClientCertificatesToBeRemove()));
        }
    }

    /**
     * To undeploy the client certificates from the gateway environment.
     *
     * @param identifier   Relevant API particular certificate is related with.
     * @param tenantDomain Tenant domain of the API.
     * @throws CertificateManagementException Certificate Management Exception.
     */
    private void setClientCertificatesToBeRemoved(APIIdentifier identifier, String tenantDomain, GatewayAPIDTO gatewayAPIDTO)
            throws CertificateManagementException {

        if (!CertificateManagerImpl.getInstance().isClientCertificateBasedAuthenticationConfigured()) {
            return;
        }
        int tenantId = APIUtil.getTenantIdFromTenantDomain(tenantDomain);
        List<ClientCertificateDTO> clientCertificateDTOList = CertificateMgtDAO.getInstance()
                .getClientCertificates(tenantId, null, identifier);
        if (clientCertificateDTOList != null) {
            for (ClientCertificateDTO clientCertificateDTO : clientCertificateDTOList) {
                gatewayAPIDTO.setClientCertificatesToBeRemove(addStringToList(clientCertificateDTO.getAlias() + "_" +
                        tenantId, gatewayAPIDTO.getLocalEntriesToBeRemove()));
            }
        }
        List<String> aliasList = CertificateMgtDAO.getInstance()
                .getDeletedClientCertificateAlias(identifier, tenantId);
        for (String alias : aliasList) {
            gatewayAPIDTO.setClientCertificatesToBeRemove(addStringToList(alias + "_" + tenantId,
                    gatewayAPIDTO.getClientCertificatesToBeRemove()));
        }
    }

    /**
     * Get the specified in/out sequences from api object
     *
     * @param api          -API object
     * @param tenantDomain
     * @throws APIManagementException
     */
    private void setCustomSequencesToBeAdded(API api, String tenantDomain, GatewayAPIDTO gatewayAPIDTO)
            throws APIManagementException {

        if (APIUtil.isSequenceDefined(api.getInSequence()) || APIUtil.isSequenceDefined(api.getOutSequence())) {
            try {
                if (APIUtil.isSequenceDefined(api.getInSequence())) {
                    if (api.getInSequenceMediation() != null) {
                        addSequenceFromConfig(api, gatewayAPIDTO, api.getInSequenceMediation().getConfig(),
                                APIConstants.API_CUSTOM_SEQ_IN_EXT, api.getInSequence());
                    } else {
                        addSequence(api, tenantDomain, gatewayAPIDTO, APIConstants.API_CUSTOM_SEQUENCE_TYPE_IN,
                                APIConstants.API_CUSTOM_SEQ_IN_EXT, api.getInSequence());
                    }
                }
                if (APIUtil.isSequenceDefined(api.getOutSequence())) {
                    if (api.getOutSequenceMediation() != null) {
                        addSequenceFromConfig(api, gatewayAPIDTO, api.getOutSequenceMediation().getConfig(),
                                APIConstants.API_CUSTOM_SEQ_OUT_EXT, api.getOutSequence());
                    } else {
                        addSequence(api, tenantDomain, gatewayAPIDTO, APIConstants.API_CUSTOM_SEQUENCE_TYPE_OUT,
                                APIConstants.API_CUSTOM_SEQ_OUT_EXT, api.getOutSequence());
                    }
                }
            } catch (Exception e) {
                String msg = "Error in deploying the sequence to gateway";
                log.error(msg, e);
                throw new APIManagementException(msg);
            }
        }

    }

    private void addSequenceFromConfig(API api, GatewayAPIDTO gatewayAPIDTO, String sequence, String sequenceExtension,
            String sequenceName) throws Exception {

        OMElement inSequence = APIUtil.buildOMElement(new ByteArrayInputStream(sequence.getBytes()));

        if (inSequence != null) {
            String inSeqExt = APIUtil.getSequenceExtensionName(api) + sequenceExtension;
            if (inSequence.getAttribute(new QName("name")) != null) {
                inSequence.getAttribute(new QName("name")).setAttributeValue(inSeqExt);
            }
            GatewayContentDTO sequenceDto = new GatewayContentDTO();
            sequenceDto.setName(inSeqExt);
            sequenceDto.setContent(APIUtil.convertOMtoString(inSequence));
            gatewayAPIDTO.setSequenceToBeAdd(addGatewayContentToList(sequenceDto, gatewayAPIDTO.getSequenceToBeAdd()));
        }

    }
    private void addSequence(API api, String tenantDomain, GatewayAPIDTO gatewayAPIDTO, String sequenceType,
            String sequenceExtension,String sequenceName) throws APIManagementException, XMLStreamException {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            if (tenantDomain != null && !"".equals(tenantDomain)) {
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            } else {
                PrivilegedCarbonContext.getThreadLocalCarbonContext()
                        .setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
            }
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            OMElement inSequence = APIUtil.getCustomSequence(sequenceName, tenantId, sequenceType, api.getId());
            if (inSequence != null) {
                String inSeqExt = APIUtil.getSequenceExtensionName(api) + sequenceExtension;
                if (inSequence.getAttribute(new QName("name")) != null) {
                    inSequence.getAttribute(new QName("name")).setAttributeValue(inSeqExt);
                }
                GatewayContentDTO sequenceDto = new GatewayContentDTO();
                sequenceDto.setName(inSeqExt);
                sequenceDto.setContent(APIUtil.convertOMtoString(inSequence));
                gatewayAPIDTO
                        .setSequenceToBeAdd(addGatewayContentToList(sequenceDto, gatewayAPIDTO.getSequenceToBeAdd()));
            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    /**
     * Undeploy the sequences deployed in synapse
     *
     * @param api
     * @throws APIManagementException
     */
    private void setCustomSequencesToBeRemoved(API api, GatewayAPIDTO gatewayAPIDTO) {

        String inSequence = APIUtil.getSequenceExtensionName(api) + APIConstants.API_CUSTOM_SEQ_IN_EXT;
        gatewayAPIDTO.setSequencesToBeRemove(addStringToList(inSequence, gatewayAPIDTO.getSequencesToBeRemove()));
        String outSequence = APIUtil.getSequenceExtensionName(api) + APIConstants.API_CUSTOM_SEQ_OUT_EXT;
        gatewayAPIDTO.setSequencesToBeRemove(addStringToList(outSequence, gatewayAPIDTO.getSequencesToBeRemove()));
        String faultSequence = APIUtil.getSequenceExtensionName(api) + APIConstants.API_CUSTOM_SEQ_FAULT_EXT;
        gatewayAPIDTO.setSequencesToBeRemove(addStringToList(faultSequence, gatewayAPIDTO.getSequencesToBeRemove()));
    }

    private void setAPIFaultSequencesToBeAdded(API api, String tenantDomain, GatewayAPIDTO gatewayAPIDTO)
            throws APIManagementException {

        String faultSequenceName = api.getFaultSequence();
        String faultSeqExt = APIUtil.getSequenceExtensionName(api) + APIConstants.API_CUSTOM_SEQ_FAULT_EXT;
        Mediation mediation = api.getFaultSequenceMediation();
        boolean isTenantFlowStarted = false;
        try {
            //If a fault sequence has be defined.
            if (APIUtil.isSequenceDefined(faultSequenceName)) {
                if (mediation == null) {
                    PrivilegedCarbonContext.startTenantFlow();
                    isTenantFlowStarted = true;
                    if (!StringUtils.isEmpty(tenantDomain)) {
                        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
                    } else {
                        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain
                                (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
                    }

                }
                gatewayAPIDTO
                        .setSequencesToBeRemove(addStringToList(faultSeqExt, gatewayAPIDTO.getSequencesToBeRemove()));

                //Get the fault sequence xml
                OMElement faultSequence = null;
                if (mediation != null) {
                    faultSequence = APIUtil.buildOMElement(new ByteArrayInputStream(mediation.getConfig().getBytes()));
                } else {
                    int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
                    faultSequence = APIUtil.getCustomSequence(faultSequenceName, tenantId,
                            APIConstants.API_CUSTOM_SEQUENCE_TYPE_FAULT, api.getId());
                }

                if (faultSequence != null) {
                    if (faultSequence.getAttribute(new QName("name")) != null) {
                        faultSequence.getAttribute(new QName("name")).setAttributeValue(faultSeqExt);
                    }
                    GatewayContentDTO faultSequenceContent = new GatewayContentDTO();
                    faultSequenceContent.setName(faultSeqExt);
                    faultSequenceContent.setContent(APIUtil.convertOMtoString(faultSequence));
                    gatewayAPIDTO.setSequenceToBeAdd(addGatewayContentToList(faultSequenceContent,
                            gatewayAPIDTO.getSequenceToBeAdd()));

                }
            }
            gatewayAPIDTO.setSequencesToBeRemove(addStringToList(faultSeqExt, gatewayAPIDTO.getSequencesToBeRemove()));
        } catch (Exception e) {
            throw new APIManagementException("Error while updating the fault sequence at the Gateway", e);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }

    /**
     * Store the secured endpoint username password to registry
     *
     * @param api
     * @param tenantDomain
     * @throws APIManagementException
     */
    private void setSecureVaultProperty(APIGatewayAdminClient securityAdminClient, API api, String tenantDomain)
            throws APIManagementException {

        boolean isSecureVaultEnabled =
                Boolean.parseBoolean(ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().
                        getAPIManagerConfiguration().getFirstProperty(APIConstants.API_SECUREVAULT_ENABLE));
        if (api.isEndpointSecured() && isSecureVaultEnabled) {
            try {
                securityAdminClient.setSecureVaultProperty(api, tenantDomain);
            } catch (Exception e) {
                String msg = "Error in setting secured password.";
                log.error(msg + ' ' + e.getLocalizedMessage(), e);
                throw new APIManagementException(msg);
            }
        }
    }

    /**
     * Construct the timeout, suspendOnFailure, markForSuspension to add suspend
     * configuration to the websocket endpoint (Simply assign config values according to the endpoint-template)
     *
     * @param api
     * @param urlType - Whether production or sandbox
     * @return timeout, suspendOnFailure, markForSuspension which will use to construct the endpoint configuration
     */
    private String[] websocketEndpointConfig(API api, String urlType) throws JSONException {

        JSONObject obj = new JSONObject(api.getEndpointConfig());
        JSONObject endpointObj = null;

        if (ENDPOINT_PRODUCTION.equalsIgnoreCase(urlType)) {
            JSONObject prodEP = obj.getJSONObject(APIConstants.API_DATA_PRODUCTION_ENDPOINTS);
            if (prodEP.has("config") && prodEP.get("config") instanceof JSONObject) {
                //if config is not a JSONObject(happens when save the api without changing enpoint config at very first time)
                endpointObj = prodEP.getJSONObject("config");
            } else {
                return new String[]{"", "", ""};
            }
        } else if (ENDPOINT_SANDBOX.equalsIgnoreCase(urlType)) {
            JSONObject sandEP = obj.getJSONObject(APIConstants.API_DATA_SANDBOX_ENDPOINTS);
            if (sandEP.has("config") && sandEP.get("config") instanceof JSONObject) {
                //if config is not a JSONObject(happens when save the api without changing enpoint config at very first time)
                endpointObj = sandEP.getJSONObject("config");
            } else {
                return new String[]{"", "", ""};
            }
        }
        String duration = validateJSONObjKey("actionDuration", endpointObj) ? "\t\t<duration>" +
                endpointObj.get("actionDuration") + "</duration>\n" : "";
        String responseAction = validateJSONObjKey("actionSelect", endpointObj) ? "\t\t<responseAction>" +
                endpointObj.get("actionSelect") + "</responseAction>\n" : "";
        String timeout = duration + "\n" + responseAction;
        String retryErrorCode;
        String suspendErrorCode;

        if (validateJSONObjKey("suspendDuration", endpointObj)) {
            //Avoid suspending the endpoint when suspend duration is zero
            if (Integer.parseInt(endpointObj.get("suspendDuration").toString()) == 0) {
                String suspendOnFailure = "\t\t<errorCodes>-1</errorCodes>\n" +
                        "\t\t<initialDuration>0</initialDuration>\n" +
                        "\t\t<progressionFactor>1.0</progressionFactor>\n" +
                        "\t\t<maximumDuration>0</maximumDuration>";
                String markForSuspension = "\t\t<errorCodes>-1</errorCodes>";
                return new String[]{timeout, suspendOnFailure, markForSuspension};
            }
        }
        suspendErrorCode = parseWsEndpointConfigErrorCodes(endpointObj, "suspendErrorCode");
        String suspendDuration = validateJSONObjKey("suspendDuration", endpointObj) ? "\t\t<initialDuration>" +
                endpointObj.get("suspendDuration").toString() + "</initialDuration>" : "";
        String suspendMaxDuration = validateJSONObjKey("suspendMaxDuration", endpointObj) ?
                "\t\t<maximumDuration>" + endpointObj.get("suspendMaxDuration") + "</maximumDuration>" : "";
        String factor = validateJSONObjKey("factor", endpointObj) ? "\t\t<progressionFactor>" +
                endpointObj.get("factor") + "</progressionFactor>" : "";
        String suspendOnFailure = suspendErrorCode + "\n" + suspendDuration + "\n" + suspendMaxDuration + "\n" + factor;

        retryErrorCode = parseWsEndpointConfigErrorCodes(endpointObj,
                "retryErroCode"); //todo: fix typo retryErroCode from client side
        String retryTimeOut = validateJSONObjKey("retryTimeOut", endpointObj) ? "\t\t<retriesBeforeSuspension>" +
                endpointObj.get("retryTimeOut") + "</retriesBeforeSuspension>" : "";
        String retryDelay = validateJSONObjKey("retryDelay", endpointObj) ? "\t\t<retryDelay>" +
                endpointObj.get("retryDelay") + "</retryDelay>" : "";
        String markForSuspension = retryErrorCode + "\n" + retryTimeOut + "\n" + retryDelay;
        return new String[]{timeout, suspendOnFailure, markForSuspension};
    }

    /**
     * Parse the error codes defined in the WebSocket endpoint config
     *
     * @param endpointObj   WebSocket endpoint config JSONObject
     * @param errorCodeType The error code type (retryErroCode/suspendErrorCode)
     * @return The parsed error codes
     */
    private String parseWsEndpointConfigErrorCodes(JSONObject endpointObj, String errorCodeType) {

        if (endpointObj.has(errorCodeType)) {
            //When there are/is multiple/single retry error codes
            if (endpointObj.get(errorCodeType) instanceof JSONArray &&
                    ((JSONArray) endpointObj.get(errorCodeType)).length() != 0) {
                StringBuilder codeListBuilder = new StringBuilder();
                for (int i = 0; i < endpointObj.getJSONArray(errorCodeType).length(); i++) {
                    codeListBuilder.append(endpointObj.getJSONArray(errorCodeType).get(i).toString()).append(",");
                }
                String codeList = codeListBuilder.toString();
                return "\t\t<errorCodes>" + codeList.substring(0, codeList.length() - 1) + "</errorCodes>";
            } else if (endpointObj.get(errorCodeType) instanceof String) {
                return "\t\t<errorCodes>" + endpointObj.get(errorCodeType) + "</errorCodes>";
            }
        }
        return "";
    }

    /**
     * Checks if a given key is available in the endpoint config and if it's value is a valid String
     *
     * @param key         Key that needs to be validated
     * @param endpointObj Endpoint config JSON object
     * @return True if the given key is available with a valid String value
     */
    private boolean validateJSONObjKey(String key, JSONObject endpointObj) {

        return endpointObj.has(key) && endpointObj.get(key) instanceof String &&
                StringUtils.isNotEmpty(endpointObj.getString(key));
    }

    private String getEndpointName(String endpointConfig) throws XMLStreamException {

        OMElement omElement = AXIOMUtil.stringToOM(endpointConfig);
        OMAttribute nameAttribute = omElement.getAttribute(new QName("name"));
        if (nameAttribute != null) {
            return nameAttribute.getAttributeValue();
        } else {
            return null;
        }

    }

    /**
     * Delete the endpoint file/s from the gateway
     *
     * @param api API that the endpoint/s belong
     * @throws AxisFault Thrown if an error occurred
     */
    public void setEndpointsToBeRemoved(API api, GatewayAPIDTO gatewayAPIDTO) {

        String endpointName = api.getId().getApiName() + "--v" + api.getId().getVersion();
        gatewayAPIDTO.setEndpointEntriesToBeRemove(addStringToList(
                endpointName + "_API" + APIConstants.API_DATA_SANDBOX_ENDPOINTS.replace("_endpoints", "") + "Endpoint"
                , gatewayAPIDTO.getEndpointEntriesToBeRemove()));
        gatewayAPIDTO.setEndpointEntriesToBeRemove(addStringToList(
                endpointName + "_API" + APIConstants.API_DATA_PRODUCTION_ENDPOINTS.replace("_endpoints", "") +
                        "Endpoint"
                , gatewayAPIDTO.getEndpointEntriesToBeRemove()));
    }


    /**
     * Publishes an API Revision to all configured Gateways.
     *
     * @param api          - The API to be published
     * @param builder      - The template builder
     * @param tenantDomain - Tenant Domain of the publisher
     * @return a map of environments that failed to publish the API
     */
    public Map<String, String> deployAPIRevisionToGateway(API api, APITemplateBuilder builder, String tenantDomain) {

        Map<String, String> failedGatewaysMap = new HashMap<String, String>(0);
        Set<String> publishedGateways = new HashSet<>();

        if (debugEnabled) {
            log.debug("API to be published: " + api.getId());
            if (api.getEnvironments() != null) {
                log.debug("Number of environments to be published to: " + (api.getEnvironments().size()));
            }
            if (api.getGatewayLabels() != null) {
                log.debug("Number of labeled gateways to be published to: " + (api.getGatewayLabels().size()));
            }
        }

        if (api.getEnvironments() != null) {
            for (String environmentName : api.getEnvironments()) {
                Environment environment = environments.get(environmentName);
                //If the environment is removed from the configuration, continue without publishing
                if (environment == null) {
                    continue;
                }
                if (debugEnabled) {
                    log.debug("API with " + api.getId() + " is publishing to the environment of "
                            + environment.getName());
                }
                failedGatewaysMap = deployAPIRevisionToGatewayEnvironment(environment, api, builder, tenantDomain, false,
                        publishedGateways, failedGatewaysMap);
            }
        }

        if (api.getGatewayLabels() != null) {
            for (Label label : api.getGatewayLabels()) {
                Environment environment = getEnvironmentFromLabel(label);
                if (debugEnabled) {
                    log.debug("API with " + api.getId() + " is publishing to the label " + label);
                }
                failedGatewaysMap = deployAPIRevisionToGatewayEnvironment(environment, api, builder, tenantDomain, true,
                        publishedGateways, failedGatewaysMap);
            }
        }

        DeployAPIInGatewayEvent
                deployAPIInGatewayEvent = new DeployAPIInGatewayEvent(UUID.randomUUID().toString(),
                System.currentTimeMillis(), APIConstants.EventType.DEPLOY_API_IN_GATEWAY.name(), tenantDomain,
                api.getUuid(), publishedGateways, api.getContext(), api.getId().getVersion());
        APIUtil.sendNotification(deployAPIInGatewayEvent, APIConstants.NotifierType.GATEWAY_PUBLISHED_API.name());
        if (debugEnabled) {
            log.debug("Event sent to Gateway with eventID " + deployAPIInGatewayEvent.getEventId() + " for api "
                    + "with apiID " +   api.getId() + " at " + deployAPIInGatewayEvent.getTimeStamp());
        }

        // Extracting API details for the recommendation system
        if (recommendationEnvironment != null) {
            RecommenderEventPublisher extractor = new RecommenderDetailsExtractor(api, tenantDomain);
            Thread recommendationThread = new Thread(extractor);
            recommendationThread.start();
        }

        return failedGatewaysMap;
    }

    /**
     * Publishes an API to a given environment
     *
     * @param environment               - Gateway environment
     * @param api                       - The API to be published
     * @param builder                   - The template builder
     * @param tenantDomain              - Tenant Domain of the publisher
     * @param isGatewayDefinedAsALabel  - Whether the environment is from a label or not. If it is from a label,
     *                                  directly publishing to gateway will not happen
     * @param failedGatewaysMap         - This map will be updated with the gateway if the publishing failed
     * @return failedEnvironmentsMap
     */
    private Map<String, String> deployAPIRevisionToGatewayEnvironment(Environment environment, API api,
                                                                      APITemplateBuilder builder, String tenantDomain,
                                                                      boolean isGatewayDefinedAsALabel,
                                                                      Set<String> publishedGateways,
                                                                      Map<String, String> failedGatewaysMap) {
        long startTime;
        long endTime;
        long startTimePublishToGateway = System.currentTimeMillis();

        GatewayAPIDTO gatewayAPIDTO;
        try {
            APIGatewayAdminClient client;
            startTime = System.currentTimeMillis();
            if (!APIConstants.APITransportType.WS.toString().equals(api.getType())) {
                gatewayAPIDTO = createAPIGatewayDTOtoPublishAPI(environment, api, builder, tenantDomain);
                if (gatewayAPIDTO == null) {
                    return failedGatewaysMap;
                } else {
                    if (gatewayArtifactSynchronizerProperties.isPublishDirectlyToGatewayEnabled()) {
                        if (!isGatewayDefinedAsALabel) {
                            client = new APIGatewayAdminClient(environment);
                            client.deployAPI(gatewayAPIDTO);
                        }
                    }

                    if (saveArtifactsToStorage) {
                        artifactSaver.saveArtifact(new Gson().toJson(gatewayAPIDTO), environment.getName(),
                                APIConstants.GatewayArtifactSynchronizer.GATEWAY_INSTRUCTION_PUBLISH);
                        publishedGateways.add(environment.getName());
                        if (debugEnabled) {
                            log.debug(gatewayAPIDTO.getName() + " details saved to the DB");
                        }
                    }
                }
            } else {
                client = new APIGatewayAdminClient(environment);
                deployWebsocketAPI(api, client, isGatewayDefinedAsALabel, publishedGateways, environment);
            }
            endTime = System.currentTimeMillis();
            if (debugEnabled) {
                log.debug("Publishing API (if the API does not exist in the Gateway) took " +
                        (endTime - startTime) / 1000 + "  seconds");
            }
        } catch (AxisFault axisFault) {
                /*
                didn't throw this exception to handle multiple gateway publishing
                if gateway is unreachable we collect that environments into map with issue and show on popup in ui
                therefore this didn't break the gateway publishing if one gateway unreachable
                 */
            failedGatewaysMap.put(environment.getName(), axisFault.getMessage());
            log.error("Error occurred when publish to gateway " + environment.getName(), axisFault);
        } catch (APIManagementException | JSONException ex) {
            log.error("Error occurred deploying sequences on " + environment.getName(), ex);
            failedGatewaysMap.put(environment.getName(), ex.getMessage());
        } catch (CertificateManagementException ex) {
            log.error("Error occurred while adding/updating client certificate in " + environment.getName(), ex);
            failedGatewaysMap.put(environment.getName(), ex.getMessage());
        } catch (APITemplateException | XMLStreamException e) {
            log.error("Error occurred while Publishing API directly to Gateway", e);
            failedGatewaysMap.put(environment.getName(), e.getMessage());
        } catch (ArtifactSynchronizerException e) {
            failedGatewaysMap.put(environment.getName(), e.getMessage());
            log.error("Error occurred while saving API artifacts to the Storage");
        }
        long endTimePublishToGateway = System.currentTimeMillis();
        if (debugEnabled) {
            log.debug("Publishing to gateway : " + environment.getName() + " total time taken : " +
                    (endTimePublishToGateway - startTimePublishToGateway) / 1000 + "  seconds");
        }
        return failedGatewaysMap;
    }

    /**
     * Remove an API from the configured Gateways
     *
     * @param api          - The API to be removed
     * @param tenantDomain - Tenant Domain of the publisher
     * @return a map of environments that failed to remove the API
     */
    public Map<String, String> removeAPIRevisionFromGateway(API api, String tenantDomain) {

        Map<String, String> failedEnvironmentsMap = new HashMap<String, String>(0);
        Set<String> removedGateways = new HashSet<>();

        if (debugEnabled) {
            log.debug("API to be published: " + api.getId());
            if (api.getEnvironments() != null) {
                log.debug("Number of environments to be published to: " + (api.getEnvironments().size()));
            }
            if (api.getGatewayLabels() != null) {
                log.debug("Number of labeled gateways to be published to: " + (api.getGatewayLabels().size()));
            }
        }

        if (api.getEnvironments() != null) {
            for (String environmentName : api.getEnvironments()) {
                Environment environment = environments.get(environmentName);
                //If the environment is removed from the configuration, continue without removing
                if (environment == null) {
                    continue;
                }
                if (debugEnabled) {
                    log.debug("API with " + api.getId() + " is removing from the environment of "
                            + environment.getName());
                }
                failedEnvironmentsMap = removeAPIRevisionFromGatewayEnvironment(api, tenantDomain, environment,
                        false, removedGateways, failedEnvironmentsMap);
            }
        }

        if (api.getGatewayLabels() != null) {
            for (Label label : api.getGatewayLabels()) {
                Environment environment = getEnvironmentFromLabel(label);
                if (debugEnabled) {
                    log.debug("API with " + api.getId() + " is removing from the label " + label);
                }
                failedEnvironmentsMap = removeAPIRevisionFromGatewayEnvironment(api, tenantDomain, environment,
                        true, removedGateways, failedEnvironmentsMap);
            }
        }

        DeployAPIInGatewayEvent deployAPIInGatewayEvent = new DeployAPIInGatewayEvent(UUID.randomUUID().toString(),
                System.currentTimeMillis(), APIConstants.EventType.REMOVE_API_FROM_GATEWAY.name(), tenantDomain,
                api.getUUID(), removedGateways, api.getContext(), api.getId().getVersion());
        APIUtil.sendNotification(deployAPIInGatewayEvent,
                APIConstants.NotifierType.GATEWAY_PUBLISHED_API.name());

        updateRemovedClientCertificates(api, tenantDomain);

        // Extracting API details for the recommendation system
        if (recommendationEnvironment != null) {
            RecommenderEventPublisher extractor = new RecommenderDetailsExtractor(api, tenantDomain);
            Thread recommendationThread = new Thread(extractor);
            recommendationThread.start();
        }
        return failedEnvironmentsMap;
    }

    /**
     * Remove an API from a given environment
     *
     * @param api                       - The API to be published
     * @param tenantDomain              - Tenant Domain of the publisher
     * @param environment               - Gateway environment
     * @param isGatewayDefinedAsALabel  - Whether the environment is from a label or not
     * @param failedEnvironmentsMap     - This map will be updated with the environment if the publishing failed
     * @return failedEnvironmentsMap
     */
    public Map<String, String> removeAPIRevisionFromGatewayEnvironment(API api, String tenantDomain, Environment environment,
                                                               boolean isGatewayDefinedAsALabel,
                                                               Set<String> removedGateways,
                                                               Map<String, String> failedEnvironmentsMap) {

        try {
            GatewayAPIDTO gatewayAPIDTO = createGatewayAPIDTOtoRemoveAPI(api, tenantDomain, environment);
            if (gatewayArtifactSynchronizerProperties.isPublishDirectlyToGatewayEnabled()
                    && !isGatewayDefinedAsALabel) {
                APIGatewayAdminClient client = new APIGatewayAdminClient(environment);
                client.unDeployAPI(gatewayAPIDTO);
            }

            if (saveArtifactsToStorage) {
                artifactSaver.saveArtifact(new Gson().toJson(gatewayAPIDTO), environment.getName(),
                        APIConstants.GatewayArtifactSynchronizer.GATEWAY_INSTRUCTION_REMOVE);
                removedGateways.add(environment.getName());
                if (debugEnabled) {
                    log.debug("Status of " + api.getId() + " has been updated to DB");
                }
            }
        } catch (AxisFault axisFault) {
            /*
            didn't throw this exception to handle multiple gateway publishing if gateway is unreachable we collect
            that environments into map with issue and show on popup in ui therefore this didn't break the gateway
            unpublisihing if one gateway unreachable
            */
            log.error("Error occurred when removing API directly from the gateway " + environment.getName(),
                    axisFault);
            failedEnvironmentsMap.put(environment.getName(), axisFault.getMessage());
        } catch (CertificateManagementException ex) {
            log.error("Error occurred when deleting certificate from gateway" + environment.getName(), ex);
            failedEnvironmentsMap.put(environment.getName(), ex.getMessage());
        } catch (ArtifactSynchronizerException e) {
            log.error("Error occurred when updating the remove instruction in the storage " + environment.getName(), e);
            failedEnvironmentsMap.put(environment.getName(), e.getMessage());
        }
        return failedEnvironmentsMap;
    }

    public void deployToGateway(API api, String tenantDomain) throws APIManagementException {

        String apiId = api.getUUID();
        APIIdentifier apiIdentifier = api.getId();
        Set<String> environments = api.getEnvironments();
        List<Label> gatewayLabels = api.getGatewayLabels();
        Set<String> gateways = new HashSet<>();
        if (gatewayLabels != null) {
            for (Label gatewayLabel : gatewayLabels) {
                gateways.add(gatewayLabel.getName());
            }
        }
        if (environments != null) {
            Map<String, Environment> apiGatewayEnvironments =
                    ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                            .getAPIManagerConfiguration().getApiGatewayEnvironments();
            for (String environment : environments) {
                if (apiGatewayEnvironments != null) {
                    if (apiGatewayEnvironments.containsKey(environment)) {
                        gateways.add(environment);
                    }
                }
            }
        }
            try {
                setThreadLocalContext(tenantDomain);
                File artifact = ServiceReferenceHolder.getInstance().getImportExportService()
                        .exportAPI(apiId, null, null, null, true,
                                ExportFormat.JSON, false, true);
                artifactSaver.saveArtifact(apiId, apiIdentifier.getApiName(), apiIdentifier.getVersion(), "Current",
                        tenantDomain, artifact, gateways.toArray(new String[0]), api.getType());

            } catch (APIManagementException | APIImportExportException | ArtifactSynchronizerException e) {
                throw new APIManagementException("API " + api.getId() + "couldn't get deployed", e);
            }
            if (debugEnabled) {
                log.debug("Status of " + api.getId() + " has been updated to DB");
            }
            sendDeploymentEvent(api, tenantDomain, gateways);
    }

    private void setThreadLocalContext(String tenantDomain) throws APIManagementException {

        String tenantAdminUserName = APIUtil.getTenantAdminUserName(tenantDomain);
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        carbonContext.setTenantDomain(tenantDomain,true);
        carbonContext.setUsername(tenantAdminUserName);
    }

    public void deployToGateway(APIProduct api, String tenantDomain) throws APIManagementException {

        String apiId = api.getUuid();
        APIProductIdentifier apiIdentifier = api.getId();
        Set<String> environments = api.getEnvironments();
        List<Label> gatewayLabels = api.getGatewayLabels();
        Set<String> gateways = new HashSet<>();
        if (gatewayLabels != null) {
            for (Label gatewayLabel : gatewayLabels) {
                gateways.add(gatewayLabel.getName());
            }
        }
        if (environments != null) {
            Map<String, Environment> apiGatewayEnvironments =
                    ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                            .getAPIManagerConfiguration().getApiGatewayEnvironments();
            for (String environment : environments) {
                if (apiGatewayEnvironments != null) {
                    if (apiGatewayEnvironments.containsKey(environment)) {
                        gateways.add(environment);
                    }
                }
            }
        }
            try {
                File artifact = ServiceReferenceHolder.getInstance().getImportExportService()
                        .exportAPIProduct(apiId, null, null, null, ExportFormat.JSON, true, false, true);
                artifactSaver.saveArtifact(apiId, apiIdentifier.getName(), apiIdentifier.getVersion(), "Current",
                        tenantDomain, artifact, gateways.toArray(new String[0]), APIConstants.ApiTypes.PRODUCT_API.name());

            } catch (APIManagementException | APIImportExportException | ArtifactSynchronizerException e) {
                throw new APIManagementException("API " + api.getId() + "couldn't get deployed", e);
            }
            if (debugEnabled) {
                log.debug("Status of " + api.getId() + " has been updated to DB");
            }
            sendDeploymentEvent(api, tenantDomain, gateways);
    }

    public void unDeployFromGateway(API api, String tenantDomain, Set<String> gatewaysToRemove)
            throws APIManagementException {

        updateRemovedClientCertificates(api, tenantDomain);
        // Extracting API details for the recommendation system
        if (recommendationEnvironment != null) {
            RecommenderEventPublisher extractor = new RecommenderDetailsExtractor(api, tenantDomain);
            Thread recommendationThread = new Thread(extractor);
            recommendationThread.start();
        }
        String apiId = api.getUUID();
        APIIdentifier apiIdentifier = api.getId();
        try {
            artifactSaver.removeArtifact(apiId, apiIdentifier.getName(), apiIdentifier.getVersion(), "Current",
                    tenantDomain);

        } catch (ArtifactSynchronizerException e) {
            throw new APIManagementException("API " + api.getId() + "couldn't get unDeployed", e);
        }
        if (debugEnabled) {
            log.debug("Status of " + api.getId() + " has been updated to DB");
        }
        sendUnDeploymentEvent(api, tenantDomain, gatewaysToRemove);
    }

    public void unDeployFromGateway(APIProduct apiProduct, String tenantDomain,Set<API> associatedAPIs) throws APIManagementException {

        String apiProductUuid = apiProduct.getUuid();
        APIProductIdentifier apiProductIdentifier = apiProduct.getId();
        try {
            artifactSaver.removeArtifact(apiProductUuid, apiProductIdentifier.getName(),
                    apiProductIdentifier.getVersion(), "Current", tenantDomain);

        } catch (ArtifactSynchronizerException e) {
            throw new APIManagementException("API " + apiProductIdentifier + "couldn't get unDeployed", e);
        }
        if (debugEnabled) {
            log.debug("Status of " + apiProductIdentifier + " has been updated to DB");
        }
        sendUnDeploymentEvent(apiProduct, tenantDomain, Collections.emptySet(), associatedAPIs);

    }

    private Set<APIEvent> transformAPIToAPIEvent(Set<API> apiSet) {

        Set<APIEvent> apiEvents = new HashSet<>();
        for (API api : apiSet) {
            APIIdentifier id = api.getId();
            APIEvent apiEvent = new APIEvent(id.getApiName(), id.getVersion(), id.getProviderName(), api.getType(),
                    api.getStatus());
            apiEvents.add(apiEvent);
        }
        return apiEvents;
    }
}

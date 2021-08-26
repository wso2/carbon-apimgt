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

import com.google.gson.Gson;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.dto.ClientCertificateDTO;
import org.wso2.carbon.apimgt.api.gateway.CredentialDto;
import org.wso2.carbon.apimgt.api.gateway.GatewayAPIDTO;
import org.wso2.carbon.apimgt.api.gateway.GatewayContentDTO;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.APIProductIdentifier;
import org.wso2.carbon.apimgt.api.model.APIProductResource;
import org.wso2.carbon.apimgt.api.model.APIStatus;
import org.wso2.carbon.apimgt.api.model.Label;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.api.model.graphql.queryanalysis.GraphqlComplexityInfo;
import org.wso2.carbon.apimgt.gateway.dto.stub.APIData;
import org.wso2.carbon.apimgt.gateway.dto.stub.ResourceData;
import org.wso2.carbon.apimgt.impl.certificatemgt.CertificateManagerImpl;
import org.wso2.carbon.apimgt.impl.certificatemgt.exceptions.CertificateManagementException;
import org.wso2.carbon.apimgt.impl.dao.CertificateMgtDAO;
import org.wso2.carbon.apimgt.impl.definitions.GraphQLSchemaDefinition;
import org.wso2.carbon.apimgt.impl.dto.Environment;
import org.wso2.carbon.apimgt.impl.dto.GatewayArtifactSynchronizerProperties;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.ArtifactSaver;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.exception.ArtifactSynchronizerException;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.notifier.events.DeployAPIInGatewayEvent;
import org.wso2.carbon.apimgt.impl.recommendationmgt.RecommendationEnvironment;
import org.wso2.carbon.apimgt.impl.recommendationmgt.RecommenderDetailsExtractor;
import org.wso2.carbon.apimgt.impl.recommendationmgt.RecommenderEventPublisher;
import org.wso2.carbon.apimgt.impl.template.APITemplateBuilder;
import org.wso2.carbon.apimgt.impl.template.APITemplateBuilderImpl;
import org.wso2.carbon.apimgt.impl.template.APITemplateException;
import org.wso2.carbon.apimgt.impl.utils.APIGatewayAdminClient;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.utils.LocalEntryAdminClient;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.util.*;

public class APIGatewayManager {

    private static final Log log = LogFactory.getLog(APIGatewayManager.class);
    private boolean debugEnabled = log.isDebugEnabled();
    private static APIGatewayManager instance;

    private Map<String, Environment> environments;
    private RecommendationEnvironment recommendationEnvironment;
    private GatewayArtifactSynchronizerProperties gatewayArtifactSynchronizerProperties;
    private ArtifactSaver artifactSaver;
    private boolean saveArtifactsToStorage = false;

    private final String ENDPOINT_PRODUCTION = "_PRODUCTION_";
    private final String ENDPOINT_SANDBOX = "_SANDBOX_";
    private static final String PRODUCT_PREFIX = "prod";
    private static final String PRODUCT_VERSION = "1.0.0";

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

    /**
     * Publishes an API to all configured Gateways.
     *
     * @param api          - The API to be published
     * @param builder      - The template builder
     * @param tenantDomain - Tenant Domain of the publisher
     * @return a map of environments that failed to publish the API
     */
    public Map<String, String> publishToGateway(API api, APITemplateBuilder builder, String tenantDomain) {

        Map<String, String> failedGatewaysMap = new HashMap<String, String>(0);
        Set<String> publishedGateways = new HashSet<>();
        Set<URITemplate> operationList = null;

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
            if (APIConstants.GRAPHQL_API.equals(api.getType())) {
                operationList = api.getUriTemplates();
            }
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
                failedGatewaysMap = publishAPIToGatewayEnvironment(environment, api, builder, tenantDomain, false,
                        publishedGateways, failedGatewaysMap);
                if (APIConstants.GRAPHQL_API.equals(api.getType())) {
                    api.setUriTemplates(operationList);
                }
            }
        }

        if (api.getGatewayLabels() != null) {
            for (Label label : api.getGatewayLabels()) {
                Environment environment = getEnvironmentFromLabel(label);
                if (debugEnabled) {
                    log.debug("API with " + api.getId() + " is publishing to the label " + label);
                }
                failedGatewaysMap = publishAPIToGatewayEnvironment(environment, api, builder, tenantDomain, true,
                        publishedGateways, failedGatewaysMap);
            }
        }

        DeployAPIInGatewayEvent
                deployAPIInGatewayEvent = new DeployAPIInGatewayEvent(UUID.randomUUID().toString(),
                System.currentTimeMillis(), APIConstants.EventType.DEPLOY_API_IN_GATEWAY.name(), tenantDomain,
                api.getUUID(), publishedGateways);
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
        gatewayAPIDTO.setApiId(api.getUUID());
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
            graphqlLocalEntry.setContent("<localEntry key=\"" + api.getUUID() + "_graphQL" + "\">" + "<![CDATA[" +
                    definition + "]]>" + "</localEntry>");
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

    private void setSecureVaultPropertyToBeAdded(API api, GatewayAPIDTO gatewayAPIDTO) {

        boolean isSecureVaultEnabled =
                Boolean.parseBoolean(ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().
                        getAPIManagerConfiguration().getFirstProperty(APIConstants.API_SECUREVAULT_ENABLE));

        if (isSecureVaultEnabled) {
            JSONObject endpointConfig = new JSONObject(api.getEndpointConfig());

            if (endpointConfig.has(APIConstants.ENDPOINT_SECURITY)) {
                JSONObject endpoints = (JSONObject) endpointConfig.get(APIConstants.ENDPOINT_SECURITY);
                JSONObject productionEndpointSecurity = (JSONObject)
                        endpoints.get(APIConstants.ENDPOINT_SECURITY_PRODUCTION);
                JSONObject sandboxEndpointSecurity = (JSONObject) endpoints.get(APIConstants.ENDPOINT_SECURITY_SANDBOX);

                boolean isProductionEndpointSecured = (boolean)
                        productionEndpointSecurity.get(APIConstants.ENDPOINT_SECURITY_ENABLED);
                boolean isSandboxEndpointSecured = (boolean)
                        sandboxEndpointSecurity.get(APIConstants.ENDPOINT_SECURITY_ENABLED);
                String secureVaultAlias = api.getId().getProviderName() + "--" + api.getId().getApiName() +
                        api.getId().getVersion();
                //for production endpoints
                if (isProductionEndpointSecured && !productionEndpointSecurity.isNull("type")
                        && "BASIC".equals(productionEndpointSecurity.getString("type"))) {
                    CredentialDto credentialDto = new CredentialDto();
                    credentialDto.setAlias(secureVaultAlias.concat("--").concat(APIConstants.
                            ENDPOINT_SECURITY_PRODUCTION));
                    credentialDto.setPassword((String)
                            productionEndpointSecurity.get(APIConstants.ENDPOINT_SECURITY_PASSWORD));
                    gatewayAPIDTO.setCredentialsToBeAdd(addCredentialsToList(credentialDto,
                            gatewayAPIDTO.getCredentialsToBeAdd()));
                    if (debugEnabled) {
                        log.debug("SecureVault alias " +  secureVaultAlias + "--production" + " is created for " +
                                api.getId().getApiName());
                    }
                }
                // for sandbox endpoints
                if (isSandboxEndpointSecured && !sandboxEndpointSecurity.isNull("type")
                        && "BASIC".equals(sandboxEndpointSecurity.getString("type"))) {
                    CredentialDto credentialDto = new CredentialDto();
                    credentialDto.setAlias(secureVaultAlias.concat("--").concat(APIConstants.
                            ENDPOINT_SECURITY_SANDBOX));
                    credentialDto.setPassword((String)
                            sandboxEndpointSecurity.get(APIConstants.ENDPOINT_SECURITY_PASSWORD));
                    gatewayAPIDTO.setCredentialsToBeAdd(addCredentialsToList(credentialDto,
                            gatewayAPIDTO.getCredentialsToBeAdd()));
                    if (debugEnabled) {
                        log.debug("SecureVault alias " +  secureVaultAlias + "--sandbox" + " is created for " +
                                api.getId().getApiName());
                    }
                }
            }
        }
    }

    private CredentialDto[] addCredentialsToList(CredentialDto credential, CredentialDto[] credentials) {

        if (credentials == null) {
            return new CredentialDto[]{credential};
        } else {
            Set<CredentialDto> credentialList = new HashSet<>();
            Collections.addAll(credentialList, credentials);
            credentialList.add(credential);
            return credentialList.toArray(new CredentialDto[credentialList.size()]);
        }
    }

    private void addEndpoints(API api, APITemplateBuilder builder, GatewayAPIDTO gatewayAPIDTO)
            throws APITemplateException, XMLStreamException {

        ArrayList<String> arrayListToAdd = getEndpointType(api);
        for (String type : arrayListToAdd) {
            String endpointConfigContext = builder.getConfigStringForEndpointTemplate(type);
            GatewayContentDTO endpoint = new GatewayContentDTO();
            endpoint.setName(getEndpointName(endpointConfigContext));
            endpoint.setContent(endpointConfigContext);
            gatewayAPIDTO.setEndpointEntriesToBeAdd(addGatewayContentToList(endpoint,
                    gatewayAPIDTO.getEndpointEntriesToBeAdd()));
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

    /**
     * Publishes an API Product to all configured Gateways.
     *
     * @param apiProduct     - The API Product to be published
     * @param builder        - The template builder
     * @param tenantDomain   - Tenant Domain of the publisher
     * @param associatedAPIs - APIs associated with the current API Product
     */
    public Map<String, String> publishToGateway(APIProduct apiProduct, APITemplateBuilder builder, String tenantDomain,
            Set<API> associatedAPIs) {

        Map<String, String> failedEnvironmentsMap = new HashMap<>(0);
        Set<String> publishedGateways = new HashSet<>();

        if (apiProduct.getEnvironments() == null) {
            return failedEnvironmentsMap;
        }
        long startTime = 0;
        long startTimePublishToGateway = 0;

        APIProductIdentifier apiProductId = apiProduct.getId();

        APIIdentifier id = new APIIdentifier(PRODUCT_PREFIX, apiProductId.getName(), PRODUCT_VERSION);

        if (debugEnabled) {
            log.debug("API to be published: " + id);
            log.debug("Number of environments to be published to: " + apiProduct.getEnvironments().size());
        }

        for (String environmentName : apiProduct.getEnvironments()) {
            if (debugEnabled) {
                startTimePublishToGateway = System.currentTimeMillis();
            }
            Environment environment = environments.get(environmentName);
            //If the environment is removed from the configuration, continue without publishing
            if (environment == null) {
                continue;
            }
            APIGatewayAdminClient client;
            try {
                client = new APIGatewayAdminClient(environment);
                GatewayAPIDTO productAPIDto = new GatewayAPIDTO();
                productAPIDto.setProvider(id.getProviderName());
                productAPIDto.setApiId(apiProduct.getUuid());
                productAPIDto.setName(id.getName());
                productAPIDto.setVersion(id.getVersion());
                productAPIDto.setTenantDomain(tenantDomain);
                productAPIDto.setOverride(false);
                String definition = apiProduct.getDefinition();
                productAPIDto.setLocalEntriesToBeRemove(addStringToList(apiProduct.getUuid(),
                        productAPIDto.getLocalEntriesToBeRemove()));
                GatewayContentDTO productLocalEntry = new GatewayContentDTO();
                productLocalEntry.setName(apiProduct.getUuid());
                productLocalEntry.setContent("<localEntry key=\"" + apiProduct.getUuid() + "\">" +
                        definition.replaceAll("&(?!amp;)", "&amp;").
                                replaceAll("<", "&lt;").replaceAll(">", "&gt;")
                        + "</localEntry>");
                productAPIDto.setLocalEntriesToBeAdd(addGatewayContentToList(productLocalEntry,
                        productAPIDto.getLocalEntriesToBeAdd()));

                // Retrieve ga-config from the registry and publish to gateway as a local entry
                addGAConfigLocalEntry(productAPIDto, tenantDomain);

                // If the Gateway type is 'production' and a production url has
                // not been specified
                // Or if the Gateway type is 'sandbox' and a sandbox url has not
                // been specified
                if (debugEnabled) {
                    startTime = System.currentTimeMillis();
                }

                //Add the API

                APIIdentifier apiId = new APIIdentifier(apiProductId.getProviderName(), apiProductId.getName(), PRODUCT_VERSION);
                setClientCertificatesToBeRemoved(apiId, tenantDomain, productAPIDto);
                setClientCertificatesToBeAdded(apiId, tenantDomain, productAPIDto);
                
                productAPIDto.setApiDefinition(builder.getConfigStringForTemplate(environment));

                for (API api : associatedAPIs) {
                    setCustomSequencesToBeRemoved(api, productAPIDto);
                    setClientCertificatesToBeRemoved(api, tenantDomain, productAPIDto);
                    APITemplateBuilder apiTemplateBuilder = new APITemplateBuilderImpl(api);
                    addEndpoints(api, apiTemplateBuilder, productAPIDto);
                    setCustomSequencesToBeAdded(api, tenantDomain, productAPIDto);
                    setAPIFaultSequencesToBeAdded(api, tenantDomain, productAPIDto);
                    setSecureVaultProperty(client, api, tenantDomain);
                    setClientCertificatesToBeAdded(api, tenantDomain, productAPIDto);
                }

                if (gatewayArtifactSynchronizerProperties.isPublishDirectlyToGatewayEnabled()) {
                    client.deployAPI(productAPIDto);
                }

                if (saveArtifactsToStorage) {
                    artifactSaver.saveArtifact(new Gson().toJson(productAPIDto), environmentName,
                            APIConstants.GatewayArtifactSynchronizer.GATEWAY_INSTRUCTION_PUBLISH);
                    publishedGateways.add(environment.getName());
                }

                if (debugEnabled) {
                    long endTime = System.currentTimeMillis();
                    log.debug("Publishing API (if the API does not exist in the Gateway) took " +
                            (endTime - startTime) / 1000 + "  seconds");
                }

            } catch (AxisFault | APIManagementException | APITemplateException
                    | XMLStreamException e) {
                /*
                didn't throw this exception to handle multiple gateway publishing
                if gateway is unreachable we collect that environments into map with issue and show on popup in ui
                therefore this didn't break the gateway publishing if one gateway unreachable
                 */
                failedEnvironmentsMap.put(environmentName, e.getMessage());
                log.error("Error occurred when publishing API directly to gateway" + environmentName, e);
            } catch (ArtifactSynchronizerException e) {
                failedEnvironmentsMap.put(environmentName, e.getMessage());
                log.error("Error occurred when saving API Product artifacts to storage" + environmentName, e);
            } catch (CertificateManagementException ex) {
                log.error("Error occurred while adding/updating client certificate in " + environmentName, ex);
                failedEnvironmentsMap.put(environmentName, ex.getMessage());
            }

            if (debugEnabled) {
                long endTimePublishToGateway = System.currentTimeMillis();
                log.debug("Publishing to gateway : " + environmentName + " total time taken : " +
                        (endTimePublishToGateway - startTimePublishToGateway) / 1000 + "  seconds");
            }
        }

        DeployAPIInGatewayEvent
                deployAPIInGatewayEvent = new DeployAPIInGatewayEvent(UUID.randomUUID().toString(),
                System.currentTimeMillis(), APIConstants.EventType.DEPLOY_API_IN_GATEWAY.name(), tenantDomain,
                apiProduct.getUuid(), publishedGateways);
        APIUtil.sendNotification(deployAPIInGatewayEvent, APIConstants.NotifierType.GATEWAY_PUBLISHED_API.name());

        return failedEnvironmentsMap;
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

    /**
     * Remove an API from the configured Gateways
     *
     * @param api          - The API to be removed
     * @param tenantDomain - Tenant Domain of the publisher
     * @return a map of environments that failed to remove the API
     */
    public Map<String, String> removeFromGateway(API api, String tenantDomain) {

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
                failedEnvironmentsMap = removeAPIFromGatewayEnvironment(api, tenantDomain, environment,
                        false, removedGateways, failedEnvironmentsMap);
            }
        }

        if (api.getGatewayLabels() != null) {
            for (Label label : api.getGatewayLabels()) {
                Environment environment = getEnvironmentFromLabel(label);
                if (debugEnabled) {
                    log.debug("API with " + api.getId() + " is removing from the label " + label);
                }
                failedEnvironmentsMap = removeAPIFromGatewayEnvironment(api, tenantDomain, environment,
                        true, removedGateways, failedEnvironmentsMap);
            }
        }

        DeployAPIInGatewayEvent
                deployAPIInGatewayEvent = new DeployAPIInGatewayEvent(UUID.randomUUID().toString(),
                System.currentTimeMillis(), APIConstants.EventType.REMOVE_API_FROM_GATEWAY.name(), tenantDomain,
                api.getUUID(), removedGateways);
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
    public Map<String, String> removeAPIFromGatewayEnvironment(API api, String tenantDomain, Environment environment,
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

    /**
     * Create a DTO object required to remove the API from a given gateway environment
     *
     * @param api                   - The API to be published
     * @param tenantDomain          - Tenant Domain of the publisher
     * @param environment           - Gateway environment
     * @return DTO object with API artifacts
     */
    public GatewayAPIDTO createGatewayAPIDTOtoRemoveAPI(API api, String tenantDomain, Environment environment)
            throws CertificateManagementException {

        GatewayAPIDTO gatewayAPIDTO = new GatewayAPIDTO();
        gatewayAPIDTO.setName(api.getId().getName());
        gatewayAPIDTO.setVersion(api.getId().getVersion());
        gatewayAPIDTO.setProvider(api.getId().getProviderName());
        gatewayAPIDTO.setTenantDomain(tenantDomain);
        gatewayAPIDTO.setOverride(true);
        gatewayAPIDTO.setApiId(api.getUUID());

        setClientCertificatesToBeRemoved(api, tenantDomain, gatewayAPIDTO);
        setEndpointsToBeRemoved(api, gatewayAPIDTO);
        if (!APIConstants.APITransportType.WS.toString().equals(api.getType())) {
            if (debugEnabled) {
                log.debug("Removing API " + api.getId().getApiName() + " From environment " +
                        environment.getName());
            }
            setCustomSequencesToBeRemoved(api, gatewayAPIDTO);
            setCustomSequencesToBeRemoved(api, gatewayAPIDTO);
        } else {
            String fileName = api.getContext().replace('/', '-');
            String[] fileNames = new String[2];
            fileNames[0] = ENDPOINT_PRODUCTION + fileName;
            fileNames[1] = ENDPOINT_SANDBOX + fileName;
            gatewayAPIDTO.setSequencesToBeRemove(addStringToList(fileNames[0],
                    gatewayAPIDTO.getSequencesToBeRemove()));
            gatewayAPIDTO.setSequencesToBeRemove(addStringToList(fileNames[1],
                    gatewayAPIDTO.getSequencesToBeRemove()));
        }

        String localEntryUUId = api.getUUID();
        if (localEntryUUId != null && !localEntryUUId.isEmpty()) {
            if (APIConstants.APITransportType.GRAPHQL.toString().equals(api.getType())) {
                localEntryUUId = localEntryUUId + APIConstants.GRAPHQL_LOCAL_ENTRY_EXTENSION;
            }
            gatewayAPIDTO.setLocalEntriesToBeRemove(addStringToList(localEntryUUId,
                    gatewayAPIDTO.getLocalEntriesToBeRemove()));
        }
        return gatewayAPIDTO;
    }

    /**
     * Removed an API Product from the configured Gateways
     *
     * @param apiProduct   - The API Product to be removed
     * @param tenantDomain - Tenant Domain of the publisher
     */
    public Map<String, String> removeFromGateway(APIProduct apiProduct, String tenantDomain, Set<API> associatedAPIs) {

        Map<String, String> failedEnvironmentsMap = new HashMap<>();
        Set<String> removedGateways = new HashSet<>();

        GatewayAPIDTO productAPIGatewayAPIDTO = new GatewayAPIDTO();
        productAPIGatewayAPIDTO.setApiId(apiProduct.getUuid());
        productAPIGatewayAPIDTO.setName(apiProduct.getId().getName());
        productAPIGatewayAPIDTO.setVersion(apiProduct.getId().getVersion());
        productAPIGatewayAPIDTO.setProvider(PRODUCT_PREFIX);
        productAPIGatewayAPIDTO.setTenantDomain(tenantDomain);
        productAPIGatewayAPIDTO.setOverride(true);
        if (apiProduct.getEnvironments() != null) {
            for (String environmentName : apiProduct.getEnvironments()) {
                try {
                    Environment environment = environments.get(environmentName);
                    //If the environment is removed from the configuration, continue without removing
                    if (environment == null) {
                        continue;
                    }

                    for (API api : associatedAPIs) {
                        if (!APIStatus.PUBLISHED.getStatus().equals(api.getStatus())) {
                            setEndpointsToBeRemoved(api, productAPIGatewayAPIDTO);
                            setCustomSequencesToBeRemoved(api, productAPIGatewayAPIDTO);
                            setClientCertificatesToBeRemoved(api, tenantDomain, productAPIGatewayAPIDTO);
                        }
                    }
                    productAPIGatewayAPIDTO.setLocalEntriesToBeRemove(addStringToList(apiProduct.getUuid(),
                            productAPIGatewayAPIDTO.getLocalEntriesToBeRemove()));
                    if (gatewayArtifactSynchronizerProperties.isPublishDirectlyToGatewayEnabled()) {
                        APIGatewayAdminClient client = new APIGatewayAdminClient(environment);
                        client.unDeployAPI(productAPIGatewayAPIDTO);
                    }

                    if (saveArtifactsToStorage) {
                        artifactSaver.saveArtifact(new Gson().toJson(productAPIGatewayAPIDTO), environmentName,
                                APIConstants.GatewayArtifactSynchronizer.GATEWAY_INSTRUCTION_REMOVE);
                        removedGateways.add(environmentName);
                    }

                } catch (AxisFault e) {
                    /*
                    didn't throw this exception to handle multiple gateway publishing
                    if gateway is unreachable we collect that environments into map with issue and show on popup in ui
                    therefore this didn't break the gateway unpublisihing if one gateway unreachable
                    */
                    log.error("Error occurred when removing API product directly from the gateway" + environmentName, e);
                    failedEnvironmentsMap.put(environmentName, e.getMessage());
                } catch (ArtifactSynchronizerException e) {
                    log.error("Error occurred when updating the remove instructions in storage " + environmentName, e);
                    failedEnvironmentsMap.put(environmentName, e.getMessage());
                } catch (CertificateManagementException ex) {
                    log.error("Error occurred while removing client certificate in " + environmentName, ex);
                    failedEnvironmentsMap.put(environmentName, ex.getMessage());
                }
            }
        }

        DeployAPIInGatewayEvent
                deployAPIInGatewayEvent = new DeployAPIInGatewayEvent(UUID.randomUUID().toString(),
                System.currentTimeMillis(),
                APIConstants.EventType.REMOVE_API_FROM_GATEWAY.name(), tenantDomain, apiProduct.getUuid(),
                removedGateways);
        APIUtil.sendNotification(deployAPIInGatewayEvent, APIConstants.NotifierType.GATEWAY_PUBLISHED_API.name());
        return failedEnvironmentsMap;
    }

    /**
     * add websoocket api to the gateway
     *
     * @param api
     * @param client
     * @throws APIManagementException
     */
    public void deployWebsocketAPI(API api, APIGatewayAdminClient client, boolean isGatewayDefinedAsALabel,
            Set<String> publishedGateways,Environment environment)
            throws APIManagementException, JSONException {

        GatewayAPIDTO gatewayAPIDTO = new GatewayAPIDTO();
        gatewayAPIDTO.setApiId(api.getUUID());
        gatewayAPIDTO.setName(api.getId().getName());
        gatewayAPIDTO.setVersion(api.getId().getVersion());
        gatewayAPIDTO.setProvider(api.getId().getProviderName());
        gatewayAPIDTO.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        gatewayAPIDTO.setOverride(true);
        try {
            String production_endpoint = null;
            String sandbox_endpoint = null;
            JSONObject obj = new JSONObject(api.getEndpointConfig());
            if (obj.has(APIConstants.API_DATA_PRODUCTION_ENDPOINTS)) {
                production_endpoint = obj.getJSONObject(APIConstants.API_DATA_PRODUCTION_ENDPOINTS).getString("url");
            }
            if (obj.has(APIConstants.API_DATA_SANDBOX_ENDPOINTS)) {
                sandbox_endpoint = obj.getJSONObject(APIConstants.API_DATA_SANDBOX_ENDPOINTS).getString("url");
            }
            OMElement element;
            try {
                if (production_endpoint != null) {
                    String content = createSeqString(api, production_endpoint, ENDPOINT_PRODUCTION);
                    element = AXIOMUtil.stringToOM(content);
                    String fileName = element.getAttributeValue(new QName("name"));
                    gatewayAPIDTO.setSequencesToBeRemove(addStringToList(fileName,
                            gatewayAPIDTO.getSequencesToBeRemove()));
                    GatewayContentDTO productionSequence = new GatewayContentDTO();
                    productionSequence.setContent(APIUtil.convertOMtoString(element));
                    productionSequence.setName(fileName);
                    gatewayAPIDTO.setSequenceToBeAdd(addGatewayContentToList(productionSequence,
                            gatewayAPIDTO.getSequenceToBeAdd()));
                }
                if (sandbox_endpoint != null) {
                    String content = createSeqString(api, sandbox_endpoint, ENDPOINT_SANDBOX);
                    element = AXIOMUtil.stringToOM(content);
                    String fileName = element.getAttributeValue(new QName("name"));
                    gatewayAPIDTO.setSequencesToBeRemove(addStringToList(fileName,
                            gatewayAPIDTO.getSequencesToBeRemove()));
                    GatewayContentDTO sandboxEndpointSequence = new GatewayContentDTO();
                    sandboxEndpointSequence.setContent(APIUtil.convertOMtoString(element));
                    sandboxEndpointSequence.setName(fileName);
                    gatewayAPIDTO.setSequenceToBeAdd(addGatewayContentToList(sandboxEndpointSequence,
                            gatewayAPIDTO.getSequenceToBeAdd()));
                }
                if (gatewayArtifactSynchronizerProperties.isPublishDirectlyToGatewayEnabled()) {
                    if (!isGatewayDefinedAsALabel) {
                        client.deployAPI(gatewayAPIDTO);
                    }
                }

                if (saveArtifactsToStorage) {
                    artifactSaver.saveArtifact(new Gson().toJson(gatewayAPIDTO), environment.getName(),
                            APIConstants.GatewayArtifactSynchronizer.GATEWAY_INSTRUCTION_PUBLISH);
                    publishedGateways.add(environment.getName());
                }

            } catch (AxisFault | ArtifactSynchronizerException e) {
                String msg = "Error while deploying WebsocketSequence";
                log.error(msg, e);
                throw new APIManagementException(msg);
            }
        } catch (XMLStreamException e) {
            String msg = "Error while parsing the Sequence";
            log.error(msg, e);
            throw new APIManagementException(msg);
        }
    }

    /**
     * add new api version at the API Gateway
     *
     * @param artifact
     * @param api
     */
    public void createNewWebsocketApiVersion(GenericArtifact artifact, API api) {

        try {
            APIGatewayManager gatewayManager = APIGatewayManager.getInstance();
            APIGatewayAdminClient client;
            Set<String> environments = APIUtil.extractEnvironmentsForAPI(
                    artifact.getAttribute(APIConstants.API_OVERVIEW_ENVIRONMENTS));
            api.setEndpointConfig(artifact.getAttribute(APIConstants.API_OVERVIEW_ENDPOINT_CONFIG));
            api.setContext(artifact.getAttribute(APIConstants.API_OVERVIEW_CONTEXT));
            for (String environmentName : environments) {
                Environment environment = this.environments.get(environmentName);
                client = new APIGatewayAdminClient(environment);
                boolean isGatewayDefinedAsALabel = api.getEnvironments() != null;
                Set<String> publishedGateways = new HashSet<>();
                try {
                    gatewayManager.deployWebsocketAPI(api, client, isGatewayDefinedAsALabel, publishedGateways, environment);
                } catch (JSONException ex) {
                    /*
                    didn't throw this exception to handle multiple gateway publishing
                    if gateway is unreachable we collect that environments into map with issue and show on popup in ui
                    therefore this didn't break the gateway publishing if one gateway unreachable
                    */
                    log.error("Error occurred deploying sequences on " + environmentName, ex);
                }
            }
        } catch (APIManagementException ex) {
            /*
            didn't throw this exception to handle multiple gateway publishing
            if gateway is unreachable we collect that environments into map with issue and show on popup in ui
            therefore this didn't break the gateway unpublisihing if one gateway unreachable
            */
            log.error("Error in deploying to gateway :" + ex.getMessage(), ex);
        } catch (AxisFault ex) {
            log.error("Error in deploying to gateway :" + ex.getMessage(), ex);
        } catch (GovernanceException ex) {
            log.error("Error in deploying to gateway :" + ex.getMessage(), ex);
        }
    }

    /**
     * create body of sequence
     *
     * @param api
     * @param url
     * @return
     */
    public String createSeqString(API api, String url, String urltype) throws JSONException  {

        String context = api.getContext();
        context = urltype + context;
        String[] endpointConfig = websocketEndpointConfig(api, urltype);
        String timeout = endpointConfig[0];
        String suspendOnFailure = endpointConfig[1];
        String markForSuspension = endpointConfig[2];
        String endpointConf = "<default>\n" +
                "\t<timeout>\n" +
                timeout +
                "\t</timeout>\n" +
                "\t<suspendOnFailure>\n" +
                suspendOnFailure + "\n" +
                "\t</suspendOnFailure>\n" +
                "\t<markForSuspension>\n" +
                markForSuspension +
                "\t</markForSuspension>\n" +
                "</default>";
        String seq = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<sequence xmlns=\"http://ws.apache.org/ns/synapse\" name=\"" +
                context.replace('/', '-') + "\">\n" +
                "   <property name=\"OUT_ONLY\" value=\"true\"/>\n" +
                "   <script language=\"js\">var sub_path = mc.getProperty(\"websocket.subscriber.path\");\t    \n" +
                "        \tvar queryParamString = sub_path.split(\"\\\\?\")[1];\n" +
                "                if(queryParamString != undefined) {\t    \n" +
                "\t\tmc.setProperty('queryparams', \"?\" + queryParamString);\n" +
                "\t\t}\t\t\n" +
                "   </script>\n" +
                "   <property xmlns:soapenv=\"http://www.w3.org/2003/05/soap-envelope\"\n" +
                "             xmlns:ns=\"http://org.apache.synapse/xsd\"\n" +
                "             xmlns:ns3=\"http://org.apache.synapse/xsd\"\n" +
                "             name=\"queryparams\"\n" +
                "             expression=\"$ctx:queryparams\"/>\n" +
                "   <property name=\"urlVal\" value=\""+ url + "\"/>\n" +
                "   <property xmlns:soapenv=\"http://www.w3.org/2003/05/soap-envelope\"\n" +
                "             xmlns:ns3=\"http://org.apache.synapse/xsd\"\n" +
                "             name=\"fullUrl\"\n" +
                "             expression=\"fn:concat(get-property('urlVal'), get-property('queryparams'))\"\n" +
                "             type=\"STRING\"/>\n" +
                "   <header xmlns:soapenv=\"http://www.w3.org/2003/05/soap-envelope\"\n" +
                "           xmlns:ns3=\"http://org.apache.synapse/xsd\"\n" +
                "           name=\"To\"\n" +
                "           expression=\"$ctx:fullUrl\"/>\n" +
                "   <send>\n" +
                "      <endpoint>\n" +
                endpointConf + "\n" +
                "      </endpoint>\n" +
                "   </send>\n" +
                "</sequence>";
        return seq;
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
            if (saveArtifactsToStorage){
                return artifactSaver.isAPIPublished(api.getUUID());
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

    public void setProductResourceSequences(APIProviderImpl apiProvider, APIProduct apiProduct)
            throws APIManagementException {

        for (APIProductResource resource : apiProduct.getProductResources()) {
            APIIdentifier apiIdentifier = resource.getApiIdentifier();
            API api = apiProvider.getAPI(apiIdentifier);

            String inSequenceKey = APIUtil.getSequenceExtensionName(api) + APIConstants.API_CUSTOM_SEQ_IN_EXT;
            if (APIUtil.isSequenceDefined(api.getInSequence())) {
                resource.setInSequenceName(inSequenceKey);
            }

            String outSequenceKey = APIUtil.getSequenceExtensionName(api) + APIConstants.API_CUSTOM_SEQ_OUT_EXT;
            if (APIUtil.isSequenceDefined(api.getOutSequence())) {
                resource.setOutSequenceName(outSequenceKey);
            }

            String faultSequenceKey = APIUtil.getFaultSequenceName(api);
            if (APIUtil.isSequenceDefined(api.getFaultSequence())) {
                resource.setFaultSequenceName(faultSequenceKey);
            }
        }
    }

    /**
     * To deploy client certificate in given API environment.
     *
     * @param api          Relevant API.
     * @param tenantDomain Tenant domain.
     * @throws CertificateManagementException Certificate Management Exception.
     */
    private void setClientCertificatesToBeAdded(API api, String tenantDomain, GatewayAPIDTO gatewayAPIDTO)
            throws CertificateManagementException {

        if (!CertificateManagerImpl.getInstance().isClientCertificateBasedAuthenticationConfigured()) {
            return;
        }
        int tenantId = APIUtil.getTenantIdFromTenantDomain(tenantDomain);
        List<ClientCertificateDTO> clientCertificateDTOList = CertificateMgtDAO.getInstance()
                .getClientCertificates(tenantId, null, api.getId());
        if (clientCertificateDTOList != null) {
            for (ClientCertificateDTO clientCertificateDTO : clientCertificateDTOList) {
                GatewayContentDTO clientCertificate = new GatewayContentDTO();
                clientCertificate.setName(clientCertificateDTO.getAlias() + "_" + tenantId);
                clientCertificate.setContent(clientCertificateDTO.getCertificate());
                gatewayAPIDTO.setClientCertificatesToBeAdd(addGatewayContentToList(clientCertificate,
                        gatewayAPIDTO.getClientCertificatesToBeAdd()));
            }
        }
    }

    
    /**
     * To deploy client certificate in given API environment.
     *
     * @param identifier Relevant API ID.
     * @param tenantDomain Tenant domain.
     * @throws CertificateManagementException Certificate Management Exception.
     */
    private void setClientCertificatesToBeAdded(APIIdentifier identifier, String tenantDomain,
            GatewayAPIDTO gatewayAPIDTO) throws CertificateManagementException {
        if (!CertificateManagerImpl.getInstance().isClientCertificateBasedAuthenticationConfigured()) {
            return;
        }
        int tenantId = APIUtil.getTenantIdFromTenantDomain(tenantDomain);
        List<ClientCertificateDTO> clientCertificateDTOList = CertificateMgtDAO.getInstance()
                .getClientCertificates(tenantId, null, identifier);
        if (clientCertificateDTOList != null) {
            for (ClientCertificateDTO clientCertificateDTO : clientCertificateDTOList) {
                GatewayContentDTO clientCertificate = new GatewayContentDTO();
                clientCertificate.setName(clientCertificateDTO.getAlias() + "_" + tenantId);
                clientCertificate.setContent(clientCertificateDTO.getCertificate());
                gatewayAPIDTO.setClientCertificatesToBeAdd(
                        addGatewayContentToList(clientCertificate, gatewayAPIDTO.getClientCertificatesToBeAdd()));
            }
        }
    }

    /**
     * To update the database instance with the successfully removed client certificates from teh gateway.
     *
     * @param api          Relevant API related with teh removed certificate.
     * @param tenantDomain Tenant domain of the API.
     */
    private void updateRemovedClientCertificates(API api, String tenantDomain) {

        if (!CertificateManagerImpl.getInstance().isClientCertificateBasedAuthenticationConfigured()) {
            return;
        }
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
    private void setClientCertificatesToBeRemoved(APIIdentifier identifier, String tenantDomain,
            GatewayAPIDTO gatewayAPIDTO) throws CertificateManagementException {

        if (!CertificateManagerImpl.getInstance().isClientCertificateBasedAuthenticationConfigured()) {
            return;
        }
        int tenantId = APIUtil.getTenantIdFromTenantDomain(tenantDomain);
        List<ClientCertificateDTO> clientCertificateDTOList = CertificateMgtDAO.getInstance()
                .getClientCertificates(tenantId, null, identifier);
        if (clientCertificateDTOList != null) {
            for (ClientCertificateDTO clientCertificateDTO : clientCertificateDTOList) {
                gatewayAPIDTO.setClientCertificatesToBeRemove(addStringToList(
                        clientCertificateDTO.getAlias() + "_" + tenantId, gatewayAPIDTO.getLocalEntriesToBeRemove()));
            }
        }
        List<String> aliasList = CertificateMgtDAO.getInstance().getDeletedClientCertificateAlias(identifier, tenantId);
        for (String alias : aliasList) {
            gatewayAPIDTO.setClientCertificatesToBeRemove(
                    addStringToList(alias + "_" + tenantId, gatewayAPIDTO.getClientCertificatesToBeRemove()));
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
                PrivilegedCarbonContext.startTenantFlow();
                if (tenantDomain != null && !"".equals(tenantDomain)) {
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
                } else {
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain
                            (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
                }
                int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

                if (APIUtil.isSequenceDefined(api.getInSequence())) {
                    addSequence(api, tenantId, gatewayAPIDTO, APIConstants.API_CUSTOM_SEQUENCE_TYPE_IN,
                            APIConstants.API_CUSTOM_SEQ_IN_EXT, api.getInSequence());
                }

                if (APIUtil.isSequenceDefined(api.getOutSequence())) {
                    addSequence(api, tenantId, gatewayAPIDTO, APIConstants.API_CUSTOM_SEQUENCE_TYPE_OUT,
                            APIConstants.API_CUSTOM_SEQ_OUT_EXT, api.getOutSequence());
                }

            } catch (Exception e) {
                String msg = "Error in deploying the sequence to gateway";
                log.error(msg, e);
                throw new APIManagementException(msg);
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }

    }

    private void addSequence(API api, int tenantId, GatewayAPIDTO gatewayAPIDTO, String sequenceType,
            String sequenceExtension,String sequenceName) throws APIManagementException, XMLStreamException {

        OMElement inSequence = APIUtil.getCustomSequence(sequenceName, tenantId, sequenceType, api.getId());

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
        boolean isTenantFlowStarted = false;
        try {
            PrivilegedCarbonContext.startTenantFlow();
            isTenantFlowStarted = true;
            if (!StringUtils.isEmpty(tenantDomain)) {
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            } else {
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain
                        (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
            }

            //If a fault sequence has be defined.
            if (APIUtil.isSequenceDefined(faultSequenceName)) {
                int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
                gatewayAPIDTO
                        .setSequencesToBeRemove(addStringToList(faultSeqExt, gatewayAPIDTO.getSequencesToBeRemove()));

                //Get the fault sequence xml
                OMElement faultSequence = APIUtil.getCustomSequence(faultSequenceName, tenantId,
                        APIConstants.API_CUSTOM_SEQUENCE_TYPE_FAULT, api.getId());

                if (faultSequence != null) {
                    if (APIUtil.isPerAPISequence(faultSequenceName, tenantId, api.getId(),
                            APIConstants.API_CUSTOM_SEQUENCE_TYPE_FAULT)) {
                        if (faultSequence.getAttribute(new QName("name")) != null) {
                            faultSequence.getAttribute(new QName("name")).setAttributeValue(faultSeqExt);
                        }
                    } else {
                        gatewayAPIDTO.setSequencesToBeRemove(addStringToList(faultSequenceName,
                                gatewayAPIDTO.getSequencesToBeRemove()));
                        faultSeqExt = faultSequenceName;
                    }
                    GatewayContentDTO faultSequenceContent = new GatewayContentDTO();
                    faultSequenceContent.setName(faultSeqExt);
                    faultSequenceContent.setContent(APIUtil.convertOMtoString(faultSequence));
                    gatewayAPIDTO.setSequenceToBeAdd(addGatewayContentToList(faultSequenceContent,
                            gatewayAPIDTO.getSequenceToBeAdd()));
                }
            } else {
                gatewayAPIDTO
                        .setSequencesToBeRemove(addStringToList(faultSeqExt, gatewayAPIDTO.getSequencesToBeRemove()));
            }
        } catch (XMLStreamException e) {
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

}

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

import io.swagger.annotations.Api;
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
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.dto.ClientCertificateDTO;
import org.wso2.carbon.apimgt.api.dto.CredentialDto;
import org.wso2.carbon.apimgt.api.dto.GatewayAPIDTO;
import org.wso2.carbon.apimgt.api.dto.GatewayContentDTO;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.APIProductIdentifier;
import org.wso2.carbon.apimgt.api.model.APIProductResource;
import org.wso2.carbon.apimgt.api.model.APIStatus;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.gateway.dto.stub.APIData;
import org.wso2.carbon.apimgt.gateway.dto.stub.ResourceData;
import org.wso2.carbon.apimgt.impl.certificatemgt.CertificateManagerImpl;
import org.wso2.carbon.apimgt.impl.certificatemgt.exceptions.CertificateManagementException;
import org.wso2.carbon.apimgt.impl.dao.CertificateMgtDAO;
import org.wso2.carbon.apimgt.impl.definitions.GraphQLSchemaDefinition;
import org.wso2.carbon.apimgt.impl.dto.Environment;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

public class APIGatewayManager {

	private static final Log log = LogFactory.getLog(APIGatewayManager.class);

	private static APIGatewayManager instance;

    private Map<String, Environment> environments;

	private boolean debugEnabled = log.isDebugEnabled();

    private final String ENDPOINT_PRODUCTION = "_PRODUCTION_";

    private final String ENDPOINT_SANDBOX = "_SANDBOX_";

    private static final String PRODUCT_PREFIX = "prod";
    private static final String PRODUCT_VERSION = "1.0.0";

	private APIGatewayManager() {
		APIManagerConfiguration config = ServiceReferenceHolder.getInstance()
		                                                       .getAPIManagerConfigurationService()
		                                                       .getAPIManagerConfiguration();
		environments = config.getApiGatewayEnvironments();
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
	 * @param api
	 *            - The API to be published
	 * @param builder
	 *            - The template builder
	 * @param tenantDomain
	 *            - Tenant Domain of the publisher
	 */
    public Map<String, String> publishToGateway(API api, APITemplateBuilder builder, String tenantDomain) {

        Map<String, String> failedEnvironmentsMap = new HashMap<String, String>(0);
        if (api.getEnvironments() == null) {
            return failedEnvironmentsMap;
        }
        long startTime;
        long endTime;
        if (debugEnabled) {
            log.debug("API to be published: " + api.getId());
            log.debug("Number of environments to be published to: " + api.getEnvironments().size());
        }
        for (String environmentName : api.getEnvironments()) {
            long startTimePublishToGateway = System.currentTimeMillis();
            Environment environment = environments.get(environmentName);
            //If the environment is removed from the configuration, continue without publishing
            if (environment == null) {
                continue;
            }
            APIGatewayAdminClient client;
            GatewayAPIDTO gatewayAPIDTO = new GatewayAPIDTO();
            gatewayAPIDTO.setName(api.getId().getName());
            gatewayAPIDTO.setVersion(api.getId().getVersion());
            gatewayAPIDTO.setProvider(api.getId().getProviderName());
            gatewayAPIDTO.setApiId(api.getUUID());
            gatewayAPIDTO.setTenantDomain(tenantDomain);
            gatewayAPIDTO.setOverride(true);

            try {
                String definition;
                String operation;
                client = new APIGatewayAdminClient(environment);
                if (api.getType() != null && APIConstants.APITransportType.GRAPHQL.toString().equals(api.getType())) {
                    //Build schema with scopes and roles
                    GraphQLSchemaDefinition schemaDefinition = new GraphQLSchemaDefinition();
                    definition = schemaDefinition.buildSchemaWithScopesAndRoles(api);
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
                // If the API exists in the Gateway
                // If the Gateway type is 'production' and a production url has
                // not been specified
                // Or if the Gateway type is 'sandbox' and a sandbox url has not
                // been specified
                startTime = System.currentTimeMillis();
                if ((APIConstants.GATEWAY_ENV_TYPE_PRODUCTION.equals(environment.getType()) &&
                        !APIUtil.isProductionEndpointsExists(api.getEndpointConfig())) ||
                        (APIConstants.GATEWAY_ENV_TYPE_SANDBOX.equals(environment.getType()) &&
                                !APIUtil.isSandboxEndpointsExists(api.getEndpointConfig()))) {
                    if (debugEnabled) {
                        log.debug("Not adding API to environment " + environment.getName() +
                                " since its endpoint URL " + "cannot be found");
                    }
                    return failedEnvironmentsMap;
                }
                setCustomSequencesToBeRemoved(api, gatewayAPIDTO);
                setClientCertificatesToBeRemoved(api, tenantDomain, gatewayAPIDTO);
                setEndpointsToBeRemoved(api, gatewayAPIDTO);
                setAPIFaultSequencesToBeAdded(api, tenantDomain, gatewayAPIDTO);
                setCustomSequencesToBeAdded(api, tenantDomain, gatewayAPIDTO);
                setClientCertificatesToBeAdded(api, tenantDomain, gatewayAPIDTO);
                if (!APIConstants.APITransportType.WS.toString().equals(api.getType())) {
                    //Add the API
                    if (APIConstants.IMPLEMENTATION_TYPE_INLINE.equalsIgnoreCase(api.getImplementation())) {
                        String prototypeScriptAPI = builder.getConfigStringForPrototypeScriptAPI(environment);
                        gatewayAPIDTO.setApiDefinition(prototypeScriptAPI);
                    } else if (APIConstants.IMPLEMENTATION_TYPE_ENDPOINT
                            .equalsIgnoreCase(api.getImplementation())) {
                        String apiConfig = builder.getConfigStringForTemplate(environment);
                        gatewayAPIDTO.setApiDefinition(apiConfig);
                        JSONObject endpointConfig = new JSONObject(api.getEndpointConfig());
                        if (!endpointConfig.get(APIConstants.API_ENDPOINT_CONFIG_PROTOCOL_TYPE)
                                .equals(APIConstants.ENDPOINT_TYPE_AWSLAMBDA)) {
                            addEndpoints(api, builder, gatewayAPIDTO);
                        }
                    }

                    if (api.isDefaultVersion()) {
                        String defaultAPIConfig =
                                builder.getConfigStringForDefaultAPITemplate(api.getId().getVersion());
                        gatewayAPIDTO.setDefaultAPIDefinition(defaultAPIConfig);
                    }
                    setSecureVaultPropertyToBeAdded(api, gatewayAPIDTO);

                    client.deployAPI(gatewayAPIDTO);
                } else {
                    deployWebsocketAPI(api, client);
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
                failedEnvironmentsMap.put(environmentName, axisFault.getMessage());
                log.error("Error occurred when publish to gateway " + environmentName, axisFault);
            } catch (APIManagementException | JSONException ex) {
                /*
                didn't throw this exception to handle multiple gateway publishing
                if gateway is unreachable we collect that environments into map with issue and show on popup in ui
                therefore this didn't break the gateway publishing if one gateway unreachable
                 */
                log.error("Error occurred deploying sequences on " + environmentName, ex);
                failedEnvironmentsMap.put(environmentName, ex.getMessage());
            } catch (CertificateManagementException ex) {
                log.error("Error occurred while adding/updating client certificate in " + environmentName, ex);
                failedEnvironmentsMap.put(environmentName, ex.getMessage());
            } catch (APITemplateException | XMLStreamException e1) {
                log.error("Error occurred while Publishing API",e1);
                failedEnvironmentsMap.put(environmentName, e1.getMessage());
            }
            long endTimePublishToGateway = System.currentTimeMillis();
            if (debugEnabled) {
                log.debug("Publishing to gateway : " + environmentName + " total time taken : " +
                        (endTimePublishToGateway - startTimePublishToGateway) / 1000 + "  seconds");
            }
        }
        updateRemovedClientCertificates(api, tenantDomain);
        return failedEnvironmentsMap;
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
        if (api.isEndpointSecured() && isSecureVaultEnabled) {
            String secureVaultAlias =
                    api.getId().getProviderName() + "--" + api.getId().getApiName() + api.getId().getVersion();

            CredentialDto credentialDto = new CredentialDto();
            credentialDto.setAlias(secureVaultAlias);
            credentialDto.setPassword(api.getEndpointUTPassword());
            gatewayAPIDTO.setCredentialsToBeAdd(addCredentialsToList(credentialDto,
                    gatewayAPIDTO.getCredentialsToBeAdd()));
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
     * @param apiProduct
     *            - The API Product to be published
     * @param builder
     *            - The template builder
     * @param tenantDomain
     *            - Tenant Domain of the publisher
     * @param associatedAPIs
     *            - APIs associated with the current API Product
     */
    public Map<String, String> publishToGateway(APIProduct apiProduct, APITemplateBuilder builder, String tenantDomain,
                                                Set<API> associatedAPIs) {
        Map<String, String> failedEnvironmentsMap = new HashMap<>(0);

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
                // If the Gateway type is 'production' and a production url has
                // not been specified
                // Or if the Gateway type is 'sandbox' and a sandbox url has not
                // been specified
                if (debugEnabled) {
                    startTime = System.currentTimeMillis();
                }

                //Add the API

                productAPIDto.setApiDefinition(builder.getConfigStringForTemplate(environment));

                for (API api : associatedAPIs) {
                    setCustomSequencesToBeRemoved(api, productAPIDto);
                    APITemplateBuilder apiTemplateBuilder = new APITemplateBuilderImpl(api);
                    addEndpoints(api, apiTemplateBuilder, productAPIDto);
                    setCustomSequencesToBeAdded(api, tenantDomain, productAPIDto);
                    setAPIFaultSequencesToBeAdded(api, tenantDomain, productAPIDto);
                    setSecureVaultProperty(client, api, tenantDomain);
                }
                client.deployAPI(productAPIDto);
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
                log.error("Error occurred when publish to gateway " + environmentName, e);
            }

            if (debugEnabled) {
                long endTimePublishToGateway = System.currentTimeMillis();
                log.debug("Publishing to gateway : " + environmentName + " total time taken : " +
                        (endTimePublishToGateway - startTimePublishToGateway) / 1000 + "  seconds");
            }
        }

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
	 * Removed an API from the configured Gateways
	 * 
	 * @param api
	 *            - The API to be removed
	 * @param tenantDomain
	 *            - Tenant Domain of the publisher
	 */
    public Map<String, String> removeFromGateway(API api, String tenantDomain) {
        Map<String, String> failedEnvironmentsMap = new HashMap<String, String>(0);
        String localEntryUUId = api.getUUID();
        if (api.getEnvironments() != null) {
            for (String environmentName : api.getEnvironments()) {
                try {
                    Environment environment = environments.get(environmentName);
                    //If the environment is removed from the configuration, continue without removing
                    if (environment == null) {
                        continue;
                    }
                    GatewayAPIDTO gatewayAPIDTO = new GatewayAPIDTO();
                    gatewayAPIDTO.setName(api.getId().getName());
                    gatewayAPIDTO.setVersion(api.getId().getVersion());
                    gatewayAPIDTO.setProvider(api.getId().getProviderName());
                    gatewayAPIDTO.setTenantDomain(tenantDomain);
                    gatewayAPIDTO.setOverride(true);
                    APIGatewayAdminClient client = new APIGatewayAdminClient(environment);
                    setClientCertificatesToBeRemoved(api, tenantDomain, gatewayAPIDTO);
                    setEndpointsToBeRemoved(api, gatewayAPIDTO);
                    if(!APIConstants.APITransportType.WS.toString().equals(api.getType())) {
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

                    if (localEntryUUId != null && !localEntryUUId.isEmpty()) {
                        if (APIConstants.APITransportType.GRAPHQL.toString().equals(api.getType())) {
                            localEntryUUId = localEntryUUId + APIConstants.GRAPHQL_LOCAL_ENTRY_EXTENSION;
                        }
                        gatewayAPIDTO.setLocalEntriesToBeRemove(addStringToList(localEntryUUId,
                                gatewayAPIDTO.getLocalEntriesToBeRemove()));
                    }
                    client.unDeployAPI(gatewayAPIDTO);
                } catch (AxisFault axisFault) {
                    /*
                    didn't throw this exception to handle multiple gateway publishing
                    if gateway is unreachable we collect that environments into map with issue and show on popup in ui
                    therefore this didn't break the gateway unpublisihing if one gateway unreachable
                    */
                    log.error("Error occurred when removing from gateway " + environmentName,
                              axisFault);
                    failedEnvironmentsMap.put(environmentName, axisFault.getMessage());
                } catch (CertificateManagementException ex) {
                    log.error("Error occurred when deleting certificate from gateway" + environmentName, ex);
                    failedEnvironmentsMap.put(environmentName, ex.getMessage());
                }
            }
            updateRemovedClientCertificates(api, tenantDomain);
        }
        return failedEnvironmentsMap;
    }

    /**
     * Removed an API Product from the configured Gateways
     *
     * @param apiProduct
     *            - The API Product to be removed
     * @param tenantDomain
     *            - Tenant Domain of the publisher
     */
    public Map<String, String> removeFromGateway(APIProduct apiProduct, String tenantDomain, Set<API> associatedAPIs) {
        Map<String, String> failedEnvironmentsMap = new HashMap<>();
        GatewayAPIDTO productAPIGatewayAPIDTO = new GatewayAPIDTO();
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

                    APIGatewayAdminClient client = new APIGatewayAdminClient(environment);

                    for (API api : associatedAPIs) {
                        if (!APIStatus.PUBLISHED.getStatus().equals(api.getStatus())) {
                            setEndpointsToBeRemoved(api, productAPIGatewayAPIDTO);
                            setCustomSequencesToBeRemoved(api, productAPIGatewayAPIDTO);
                        }
                    }
                    productAPIGatewayAPIDTO.setLocalEntriesToBeRemove(addStringToList(apiProduct.getUuid(),
                            productAPIGatewayAPIDTO.getLocalEntriesToBeRemove()));
                    client.unDeployAPI(productAPIGatewayAPIDTO);
                } catch (AxisFault e) {
                    /*
                    didn't throw this exception to handle multiple gateway publishing
                    if gateway is unreachable we collect that environments into map with issue and show on popup in ui
                    therefore this didn't break the gateway unpublisihing if one gateway unreachable
                    */
                    log.error("Error occurred when removing from gateway " + environmentName,
                            e);
                    failedEnvironmentsMap.put(environmentName, e.getMessage());
                }
            }
        }
        return failedEnvironmentsMap;
    }

    /**
     * add websoocket api to the gateway
     *
     * @param api
     * @param client
     * @throws APIManagementException
     */
    public void deployWebsocketAPI(API api, APIGatewayAdminClient client)
            throws APIManagementException, JSONException {

        GatewayAPIDTO gatewayAPIDTO = new GatewayAPIDTO();
        gatewayAPIDTO.setName(api.getId().getName());
        gatewayAPIDTO.setVersion(api.getId().getVersion());
        gatewayAPIDTO.setProvider(api.getId().getProviderName());
        gatewayAPIDTO.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
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
                client.deployAPI(gatewayAPIDTO);
            } catch (AxisFault e) {
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
                try {
                    gatewayManager.deployWebsocketAPI(api, client);
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
	 * @param api
	 *            - The API to be cheked.
	 * @param tenantDomain
	 *            - Tenant Domain of the publisher
	 * @return True if the API is available in at least one Gateway. False if
	 *         available in none.
	 */
    public boolean isAPIPublished(API api, String tenantDomain)throws APIManagementException {
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
                    log.error("Error occurred when check api is published on gateway" + environment.getName(), axisFault);
                }
            }
        }
        return false;
    }
    
    /**
     * Get the endpoint Security type of the published API
     * 
     * @param api - The API to be checked.
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
                        if (resource != null && resource.getInSeqXml() != null 
                                && resource.getInSeqXml().contains("DigestAuthMediator")) {
                            return APIConstants.APIEndpointSecurityConstants.DIGEST_AUTH;
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
	 * Get the specified in/out sequences from api object
	 * 
	 * @param api -API object
	 * @param tenantDomain
	 * @throws APIManagementException
     */
    private void setCustomSequencesToBeAdded(API api, String tenantDomain, GatewayAPIDTO gatewayAPIDTO)
            throws APIManagementException {

        if (APIUtil.isSequenceDefined(api.getInSequence()) || APIUtil.isSequenceDefined(api.getOutSequence())) {
            try {
                PrivilegedCarbonContext.startTenantFlow();
                if(tenantDomain != null && !"".equals(tenantDomain)){
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
                } else    {
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain
                            (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
                }
                int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

                if (APIUtil.isSequenceDefined(api.getInSequence())) {
                    addSequence(api, tenantId, gatewayAPIDTO, APIConstants.API_CUSTOM_SEQUENCE_TYPE_IN,
                            APIConstants.API_CUSTOM_SEQ_IN_EXT,api.getInSequence());
                }

                if (APIUtil.isSequenceDefined(api.getOutSequence())) {
                    addSequence(api, tenantId, gatewayAPIDTO, APIConstants.API_CUSTOM_SEQUENCE_TYPE_OUT,
                            APIConstants.API_CUSTOM_SEQ_OUT_EXT, api.getOutSequence());
                }

            } catch (Exception e) {
                String msg = "Error in deploying the sequence to gateway";
                log.error(msg, e);
                throw new APIManagementException(msg);
            }
            finally {
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
            gatewayAPIDTO.setSequenceToBeAdd(addGatewayContentToList(sequenceDto,gatewayAPIDTO.getSequenceToBeAdd()));
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
                gatewayAPIDTO.setSequencesToBeRemove(addStringToList(faultSeqExt, gatewayAPIDTO.getSequencesToBeRemove()));

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
                gatewayAPIDTO.setSequencesToBeRemove(addStringToList(faultSeqExt, gatewayAPIDTO.getSequencesToBeRemove()));
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
     * @param api
     * @param tenantDomain
     * @throws APIManagementException
     */
	private void setSecureVaultProperty(APIGatewayAdminClient securityAdminClient, API api, String tenantDomain)
            throws APIManagementException {
		boolean isSecureVaultEnabled = Boolean.parseBoolean(ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().
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
     *
     * @param urlType
     *            - Whether production or sandbox
     * @return timeout, suspendOnFailure, markForSuspension which will use to construct the endpoint configuration
     *
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
        String suspendErrorCode ;

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
     *
     */
    private String parseWsEndpointConfigErrorCodes(JSONObject endpointObj, String errorCodeType) {
        if (endpointObj.has(errorCodeType)) {
            //When there are/is multiple/single retry error codes
            if (endpointObj.get(errorCodeType) instanceof JSONArray &&
                    ((JSONArray)endpointObj.get(errorCodeType)).length() != 0) {
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
     * @param key Key that needs to be validated
     * @param endpointObj Endpoint config JSON object
     * @return True if the given key is available with a valid String value
     *
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
        gatewayAPIDTO.setEndpointEntriesToBeRemove(addStringToList(endpointName + "_API" + APIConstants.API_DATA_SANDBOX_ENDPOINTS.replace("_endpoints", "") + "Endpoint"
                , gatewayAPIDTO.getEndpointEntriesToBeRemove()));
        gatewayAPIDTO.setEndpointEntriesToBeRemove(addStringToList(endpointName + "_API" + APIConstants.API_DATA_PRODUCTION_ENDPOINTS.replace("_endpoints", "") + "Endpoint"
                , gatewayAPIDTO.getEndpointEntriesToBeRemove()));
    }


}

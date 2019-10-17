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
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.APIProductIdentifier;
import org.wso2.carbon.apimgt.api.model.APIProductResource;
import org.wso2.carbon.apimgt.gateway.dto.stub.APIData;
import org.wso2.carbon.apimgt.gateway.dto.stub.ResourceData;
import org.wso2.carbon.apimgt.impl.certificatemgt.CertificateManagerImpl;
import org.wso2.carbon.apimgt.impl.certificatemgt.exceptions.CertificateManagementException;
import org.wso2.carbon.apimgt.impl.dao.CertificateMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.Environment;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.template.APITemplateBuilder;
import org.wso2.carbon.apimgt.impl.template.APITemplateBuilderImpl;
import org.wso2.carbon.apimgt.impl.utils.APIGatewayAdminClient;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.utils.LocalEntryAdminClient;
import org.wso2.carbon.apimgt.impl.definitions.GraphQLSchemaDefinition;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
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
            LocalEntryAdminClient localEntryAdminClient;

            try {
                String definition;
                String operation;
                client = new APIGatewayAdminClient(environment);
                long apiGetStartTime = System.currentTimeMillis();
                APIData apiData = client.getApi(tenantDomain, api.getId());
                endTime = System.currentTimeMillis();
                if (debugEnabled) {
                    log.debug("Time taken to fetch API Data: " + (endTime - apiGetStartTime) / 1000 + "  seconds");
                }
                localEntryAdminClient = new LocalEntryAdminClient(environment, tenantDomain);
                if (api.getType() != null && APIConstants.APITransportType.GRAPHQL.toString().equals(api.getType())) {
                    //Build schema with scopes and roles
                    GraphQLSchemaDefinition schemaDefinition = new GraphQLSchemaDefinition();
                    definition = schemaDefinition.buildSchemaWithScopesAndRoles(api);
                    localEntryAdminClient.deleteEntry(api.getUUID() + "_graphQL");
                    localEntryAdminClient.addLocalEntry("<localEntry key=\"" + api.getUUID() + "_graphQL" + "\">" +
                            definition + "</localEntry>");
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
                    localEntryAdminClient.deleteEntry(api.getUUID());
                    localEntryAdminClient.addLocalEntry("<localEntry key=\"" + api.getUUID() + "\">" +
                            definition.replaceAll("&(?!amp;)", "&amp;").
                                    replaceAll("<", "&lt;").replaceAll(">", "&gt;")
                            + "</localEntry>");
                }
                // If the API exists in the Gateway
                if (apiData != null) {
                    startTime = System.currentTimeMillis();
                    // If the Gateway type is 'production' and the production url
                    // has been removed
                    // Or if the Gateway type is 'sandbox' and the sandbox url has
                    // been removed.
                    if ((APIConstants.GATEWAY_ENV_TYPE_PRODUCTION.equals(environment.getType()) &&
                            !APIUtil.isProductionEndpointsExists(api.getEndpointConfig())) ||
                            (APIConstants.GATEWAY_ENV_TYPE_SANDBOX.equals(environment.getType()) &&
                                    !APIUtil.isSandboxEndpointsExists(api.getEndpointConfig()))) {
                        if (debugEnabled) {
                            log.debug("Removing API " + api.getId().getApiName() +
                                    " from Environment " + environment.getName() +
                                    " since its relevant URL has been removed.");
                        }
                        client.deleteApi(tenantDomain, api.getId());
                        if (api.isPublishedDefaultVersion()) {
                            if (client.getDefaultApi(tenantDomain, api.getId()) != null) {
                                client.deleteDefaultApi(tenantDomain, api.getId());
                            }
                        }
                        setSecureVaultProperty(client, api, tenantDomain, environment);
                        undeployCustomSequences(client, api, tenantDomain, environment);
                        unDeployClientCertificates(client, api, tenantDomain);
                    } else {
                        if (debugEnabled) {
                            log.debug("API exists, updating existing API " + api.getId().getApiName() +
                                    " in environment " + environment.getName());
                        }
                        //Deploy the fault sequence first since it has to be available by the time the API is deployed.
                        deployAPIFaultSequence(client, api, tenantDomain, environment);

                        operation = "update";

                        //Update the API
                        if (api.getImplementation().equalsIgnoreCase(APIConstants.IMPLEMENTATION_TYPE_INLINE)) {
                            client.updateApiForInlineScript(builder, tenantDomain, api.getId());
                        } else if (api.getImplementation().equalsIgnoreCase(APIConstants.IMPLEMENTATION_TYPE_ENDPOINT)) {
                            client.updateApi(builder, tenantDomain, api.getId());
                            client.saveEndpoint(api, builder, tenantDomain);
                        }

                        if (api.isDefaultVersion() || api.isPublishedDefaultVersion()) {//api.isPublishedDefaultVersion() check is used to detect and update when context etc. is changed in the api which is not the default version but has a published default api
                            if (client.getDefaultApi(tenantDomain, api.getId()) != null) {
                                client.updateDefaultApi(builder, tenantDomain, api.getId().getVersion(), api.getId());
                            } else {
                                client.addDefaultAPI(builder, tenantDomain, api.getId().getVersion(), api.getId());
                            }
                        }
                        setSecureVaultProperty(client, api, tenantDomain, environment);

                        long customSeqStartTime = System.currentTimeMillis();
                        //Update the custom sequences of the API
                        updateCustomSequences(client, api, tenantDomain, environment);
                        endTime = System.currentTimeMillis();
                        if (debugEnabled) {
                            log.debug("Time taken to deploy custom Sequences: " +
                                    (endTime - customSeqStartTime) / 1000 + "  seconds");
                        }
                        updateClientCertificates(client, api, tenantDomain);
                    }
                    endTime = System.currentTimeMillis();
                    if (debugEnabled) {
                        log.debug("Publishing API (if the API exists in the Gateway) took " +
                                (endTime - startTime) / 1000 + "  seconds");
                    }
                } else {
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
                    } else {
                        if (debugEnabled) {
                            log.debug("API does not exist, adding new API " + api.getId().getApiName() +
                                    " in environment " + environment.getName());
                        }
                        //Deploy the fault sequence first since it has to be available by the time the API is deployed.
                        deployAPIFaultSequence(client, api, tenantDomain, environment);
                        deployClientCertificates(client, api, tenantDomain);
                        if (!APIConstants.APITransportType.WS.toString().equals(api.getType())) {
                            //Add the API
                            if (APIConstants.IMPLEMENTATION_TYPE_INLINE.equalsIgnoreCase(api.getImplementation())) {
                                client.addPrototypeApiScriptImpl(builder, tenantDomain, api.getId());
                            } else if (APIConstants.IMPLEMENTATION_TYPE_ENDPOINT
                                    .equalsIgnoreCase(api.getImplementation())) {
                                client.addApi(builder, tenantDomain, api.getId());
                                client.saveEndpoint(api, builder, tenantDomain);
                            }

                            if (api.isDefaultVersion()) {
                                if (client.getDefaultApi(tenantDomain, api.getId()) != null) {
                                    client.updateDefaultApi(builder, tenantDomain, api.getId().getVersion(), api.getId());
                                } else {
                                    client.addDefaultAPI(builder, tenantDomain, api.getId().getVersion(), api.getId());
                                }
                            }
                            setSecureVaultProperty(client, api, tenantDomain, environment);

                            //Deploy the custom sequences of the API.
                            deployCustomSequences(client, api, tenantDomain, environment);
                        } else {
                            deployWebsocketAPI(api, client);
                        }

                    }
                    endTime = System.currentTimeMillis();
                    if (debugEnabled) {
                        log.debug("Publishing API (if the API does not exist in the Gateway) took " +
                                (endTime - startTime) / 1000 + "  seconds");
                    }
                }
            } catch (AxisFault axisFault) {
                /*
                didn't throw this exception to handle multiple gateway publishing
                if gateway is unreachable we collect that environments into map with issue and show on popup in ui
                therefore this didn't break the gateway publishing if one gateway unreachable
                 */
                failedEnvironmentsMap.put(environmentName, axisFault.getMessage());
                log.error("Error occurred when publish to gateway " + environmentName, axisFault);
            } catch (APIManagementException ex) {
                /*
                didn't throw this exception to handle multiple gateway publishing
                if gateway is unreachable we collect that environments into map with issue and show on popup in ui
                therefore this didn't break the gateway publishing if one gateway unreachable
                 */
                log.error("Error occurred deploying sequences on " + environmentName, ex);
                failedEnvironmentsMap.put(environmentName, ex.getMessage());
            } catch (JSONException ex) {
                /*
                didn't throw this exception to handle multiple gateway publishing
                if gateway is unreachable we collect that environments into map with issue and show on popup in ui
                therefore this didn't break the gateway publishing if one gateway unreachable
                 */
                log.error("Error occurred deploying sequences on " + environmentName, ex);
                failedEnvironmentsMap.put(environmentName, ex.getMessage());
            } catch (EndpointAdminException ex) {
                log.error("Error occurred when endpoint add/update operation" + environmentName, ex);
                failedEnvironmentsMap.put(environmentName, ex.getMessage());
            } catch (CertificateManagementException ex) {
                log.error("Error occurred while adding/updating client certificate in " + environmentName, ex);
                failedEnvironmentsMap.put(environmentName, ex.getMessage());
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
        long apiGetStartTime = 0;

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
            LocalEntryAdminClient localEntryAdminClient;
            try {
                client = new APIGatewayAdminClient(environment);
                if (debugEnabled) {
                    apiGetStartTime = System.currentTimeMillis();
                }

                APIData apiData = client.getApi(tenantDomain, id);

                if (debugEnabled) {
                    long endTime = System.currentTimeMillis();
                    log.debug("Time taken to fetch API Data: " + (endTime - apiGetStartTime) / 1000 + "  seconds");
                }

                localEntryAdminClient = new LocalEntryAdminClient(environment, tenantDomain);

                String definition = apiProduct.getDefinition();
                localEntryAdminClient.deleteEntry(apiProduct.getUuid());
                localEntryAdminClient.addLocalEntry("<localEntry key=\"" + apiProduct.getUuid() + "\">" +
                        definition.replaceAll("&(?!amp;)", "&amp;").
                                replaceAll("<", "&lt;").replaceAll(">", "&gt;")
                        + "</localEntry>");

                // If the API exists in the Gateway
                if (apiData != null) {
                    if (debugEnabled) {
                        startTime = System.currentTimeMillis();
                    }

                    if (debugEnabled) {
                        log.debug("API exists, updating existing API " + id.getApiName() +
                                " in environment " + environment.getName());
                    }

                    //Update the API
                    client.updateApi(builder, tenantDomain, id);

                    for (API api : associatedAPIs) {
                        APITemplateBuilder apiTemplateBuilder = new APITemplateBuilderImpl(api);
                        client.saveEndpoint(api, apiTemplateBuilder, tenantDomain);
                        setSecureVaultProperty(client, api, tenantDomain, environment);
                        updateClientCertificates(client, api, tenantDomain);
                    }

                    if (debugEnabled) {
                        long endTime = System.currentTimeMillis();
                        log.debug("Publishing API (if the API exists in the Gateway) took " +
                                (endTime - startTime) / 1000 + "  seconds");
                    }
                } else {
                    // If the Gateway type is 'production' and a production url has
                    // not been specified
                    // Or if the Gateway type is 'sandbox' and a sandbox url has not
                    // been specified
                    if (debugEnabled) {
                        startTime = System.currentTimeMillis();
                    }

                    if (debugEnabled) {
                        log.debug("API does not exist, adding new API " + id.getApiName() +
                                " in environment " + environment.getName());
                    }

                    //Add the API
                    client.addApi(builder, tenantDomain, id);

                    for (API api : associatedAPIs) {
                        APITemplateBuilder apiTemplateBuilder = new APITemplateBuilderImpl(api);
                        client.saveEndpoint(api, apiTemplateBuilder, tenantDomain);
                        setSecureVaultProperty(client, api, tenantDomain, environment);
                        deployClientCertificates(client, api, tenantDomain);
                    }

                    if (debugEnabled) {
                        long endTime = System.currentTimeMillis();
                        log.debug("Publishing API (if the API does not exist in the Gateway) took " +
                                (endTime - startTime) / 1000 + "  seconds");
                    }
                }
            } catch (AxisFault | EndpointAdminException | APIManagementException | CertificateManagementException e) {
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
        LocalEntryAdminClient localEntryAdminClient;
        String localEntryUUId = api.getUUID();
        if (api.getEnvironments() != null) {
            for (String environmentName : api.getEnvironments()) {
                try {
                    Environment environment = environments.get(environmentName);
                    //If the environment is removed from the configuration, continue without removing
                    if (environment == null) {
                        continue;
                    }

                    APIGatewayAdminClient client = new APIGatewayAdminClient(environment);
                    unDeployClientCertificates(client, api, tenantDomain);
                    if(!APIConstants.APITransportType.WS.toString().equals(api.getType())) {
                        APIIdentifier id = api.getId();
                        if (client.getApi(tenantDomain, id) != null) {
                            if (debugEnabled) {
                                log.debug("Removing API " + api.getId().getApiName() + " From environment " +
                                        environment.getName());
                            }
                            if ("INLINE".equals(api.getImplementation()) || "MARKDOWN".equals(api.getImplementation())) {
                                client.deleteApi(tenantDomain, api.getId());
                                undeployCustomSequences(client, api, tenantDomain, environment);
                            } else {
                                client.deleteEndpoint(api, tenantDomain);
                                client.deleteApi(tenantDomain, api.getId());
                                undeployCustomSequences(client, api, tenantDomain, environment);
                            }
                        }
                    } else {
                        String fileName = api.getContext().replace('/', '-');
                        String[] fileNames = new String[2];
                        fileNames[0] = ENDPOINT_PRODUCTION + fileName;
                        fileNames[1] = ENDPOINT_SANDBOX + fileName;
                        if (client.isExistingSequence(fileNames[0], MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                            client.deleteSequence(fileNames[0], MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
                        }
                        if (client.isExistingSequence(fileNames[1], MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                            client.deleteSequence(fileNames[1], MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
                        }
                    }

                    if (api.isPublishedDefaultVersion()) {
                        APIIdentifier id = api.getId();
                        if (client.getDefaultApi(tenantDomain, id) != null) {
                            client.deleteDefaultApi(tenantDomain, api.getId());
                        }
                    }
                    if (localEntryUUId != null && !localEntryUUId.isEmpty()) {
                        if (APIConstants.APITransportType.GRAPHQL.toString().equals(api.getType())) {
                            localEntryUUId = localEntryUUId + APIConstants.GRAPHQL_LOCAL_ENTRY_EXTENSION;
                        }
                        localEntryAdminClient = new LocalEntryAdminClient(environment, tenantDomain == null ?
                                MultitenantConstants.SUPER_TENANT_DOMAIN_NAME : tenantDomain);
                        localEntryAdminClient.deleteEntry(localEntryUUId);
                    }
                } catch (AxisFault axisFault) {
                    /*
                    didn't throw this exception to handle multiple gateway publishing
                    if gateway is unreachable we collect that environments into map with issue and show on popup in ui
                    therefore this didn't break the gateway unpublisihing if one gateway unreachable
                    */
                    log.error("Error occurred when removing from gateway " + environmentName,
                              axisFault);
                    failedEnvironmentsMap.put(environmentName, axisFault.getMessage());
                } catch (EndpointAdminException ex) {
                    log.error("Error occurred when deleting endpoint from gateway" + environmentName, ex);
                    failedEnvironmentsMap.put(environmentName, ex.getMessage());
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
        if (apiProduct.getEnvironments() != null) {
            for (String environmentName : apiProduct.getEnvironments()) {
                try {
                    Environment environment = environments.get(environmentName);
                    //If the environment is removed from the configuration, continue without removing
                    if (environment == null) {
                        continue;
                    }

                    APIGatewayAdminClient client = new APIGatewayAdminClient(environment);

                    APIIdentifier id = new APIIdentifier(PRODUCT_PREFIX, apiProduct.getId().getName(), PRODUCT_VERSION);
                    client.deleteApi(tenantDomain, id);

                    for (API api : associatedAPIs) {
                        if (client.getApi(tenantDomain, api.getId()) == null) {
                            client.deleteEndpoint(api, tenantDomain);
                            unDeployClientCertificates(client, api, tenantDomain);
                            undeployCustomSequences(client, api, tenantDomain, environment);
                        }
                    }

                    String localEntryUUId = apiProduct.getUuid();

                    if (localEntryUUId != null && !localEntryUUId.isEmpty()) {
                        LocalEntryAdminClient localEntryAdminClient = new LocalEntryAdminClient(environment,
                                tenantDomain == null ?
                                MultitenantConstants.SUPER_TENANT_DOMAIN_NAME : tenantDomain);
                        localEntryAdminClient.deleteEntry(localEntryUUId);
                    }
                } catch (AxisFault | EndpointAdminException | CertificateManagementException e) {
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
                    if (client.isExistingSequence(fileName, MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                        client.deleteSequence(fileName, MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
                    }
                    client.addSequence(element, MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
                }
                if (sandbox_endpoint != null) {
                    String content = createSeqString(api, sandbox_endpoint, ENDPOINT_SANDBOX);
                    element = AXIOMUtil.stringToOM(content);
                    String fileName = element.getAttributeValue(new QName("name"));
                    if (client.isExistingSequence(fileName, MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                        client.deleteSequence(fileName, MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
                    }
                    client.addSequence(element, MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
                }
            } catch (AxisFault e) {
                String msg = "Error while parsing the policy to get the eligibility query: ";
                log.error(msg, e);
                throw new APIManagementException(msg);
            }
            } catch (XMLStreamException e) {
                String msg = "Error while parsing the policy to get the eligibility query: ";
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

    public void setProductResourceSequences(APIProviderImpl apiProvider, APIProduct apiProduct, String tenantDomain)
            throws APIManagementException {
        for (APIProductResource resource : apiProduct.getProductResources()) {
            APIIdentifier apiIdentifier = resource.getApiIdentifier();
            API api = apiProvider.getAPI(apiIdentifier);

            for (String environmentName : api.getEnvironments()) {
                Environment environment = environments.get(environmentName);
                try {
                    APIGatewayAdminClient client = new APIGatewayAdminClient(environment);

                    String inSequenceKey = APIUtil.getSequenceExtensionName(api) + APIConstants.API_CUSTOM_SEQ_IN_EXT;
                    if (client.isExistingSequence(inSequenceKey, tenantDomain)) {
                        resource.setInSequenceName(inSequenceKey);
                    }

                    String outSequenceKey = APIUtil.getSequenceExtensionName(api) + APIConstants.API_CUSTOM_SEQ_OUT_EXT;
                    if (client.isExistingSequence(outSequenceKey, tenantDomain)) {
                        resource.setOutSequenceName(outSequenceKey);
                    }

                    String faultSequenceKey = APIUtil.getSequenceExtensionName(api) + APIConstants.API_CUSTOM_SEQ_FAULT_EXT;
                    if (client.isExistingSequence(faultSequenceKey, tenantDomain)) {
                        resource.setFaultSequenceName(faultSequenceKey);
                    }
                } catch (AxisFault axisFault) {
                    throw new APIManagementException("Error occurred while checking if product resources " +
                            "have custom sequences", axisFault);
                }
            }
        }
    }

    /**
     * To deploy client certificate in given API environment.
     *
     * @param client       API GatewayAdminClient .
     * @param api          Relevant API.
     * @param tenantDomain Tenant domain.
     * @throws CertificateManagementException Certificate Management Exception.
     * @throws AxisFault                      AxisFault.
     */
    private void deployClientCertificates(APIGatewayAdminClient client, API api, String tenantDomain)
            throws CertificateManagementException, AxisFault {
        if (!CertificateManagerImpl.getInstance().isClientCertificateBasedAuthenticationConfigured()) {
            return;
        }
        int tenantId = APIUtil.getTenantIdFromTenantDomain(tenantDomain);
        List<ClientCertificateDTO> clientCertificateDTOList = CertificateMgtDAO.getInstance()
                .getClientCertificates(tenantId, null, api.getId());
        if (clientCertificateDTOList != null) {
            for (ClientCertificateDTO clientCertificateDTO : clientCertificateDTOList) {
                client.addClientCertificate(clientCertificateDTO.getCertificate(),
                        clientCertificateDTO.getAlias() + "_" + tenantId);
            }
        }
    }

    /**
     * To update client certificate in relevant API gateway environment.
     *
     * @param client       API Gateway admi client.
     * @param api          Relevant API.
     * @param tenantDomain Tenant domain.
     */
    private void updateClientCertificates(APIGatewayAdminClient client, API api, String tenantDomain)
            throws CertificateManagementException, AxisFault {
        if (!CertificateManagerImpl.getInstance().isClientCertificateBasedAuthenticationConfigured()) {
            return;
        }
        int tenantId = APIUtil.getTenantIdFromTenantDomain(tenantDomain);
        List<String> aliasList = CertificateMgtDAO.getInstance()
                .getDeletedClientCertificateAlias(api.getId(), tenantId);
        for (String alias : aliasList) {
            client.deleteClientCertificate(alias + "_" + tenantId);
        }
        List<ClientCertificateDTO> clientCertificateDTOList = CertificateMgtDAO.getInstance()
                .getClientCertificates(tenantId, null, api.getId());
        if (clientCertificateDTOList != null) {
            for (ClientCertificateDTO clientCertificateDTO : clientCertificateDTOList) {
                client.addClientCertificate(clientCertificateDTO.getCertificate(),
                        clientCertificateDTO.getAlias() + "_" + tenantId);
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
     * @param client       APIGatewayAdmin Client.
     * @param api          Relevant API particular certificate is related with.
     * @param tenantDomain Tenant domain of the API.
     * @throws CertificateManagementException Certificate Management Exception.
     * @throws AxisFault                      AxisFault.
     */
    private void unDeployClientCertificates(APIGatewayAdminClient client, API api, String tenantDomain)
            throws CertificateManagementException, AxisFault {
        if (!CertificateManagerImpl.getInstance().isClientCertificateBasedAuthenticationConfigured()) {
            return;
        }
        int tenantId = APIUtil.getTenantIdFromTenantDomain(tenantDomain);
        List<ClientCertificateDTO> clientCertificateDTOList = CertificateMgtDAO.getInstance()
                .getClientCertificates(tenantId, null, api.getId());
        if (clientCertificateDTOList != null) {
            for (ClientCertificateDTO clientCertificateDTO : clientCertificateDTOList) {
                client.deleteClientCertificate(clientCertificateDTO.getAlias() + "_" + tenantId);
            }
        }
        List<String> aliasList = CertificateMgtDAO.getInstance()
                .getDeletedClientCertificateAlias(api.getId(), tenantId);
        for (String alias : aliasList) {
            client.deleteClientCertificate(alias + "_" + tenantId);
        }
    }

	/**
	 * Get the specified in/out sequences from api object
	 * 
	 * @param api -API object
	 * @param tenantDomain
	 * @param environment
	 * @throws APIManagementException
	 * @throws AxisFault
	 */
    private void deployCustomSequences(APIGatewayAdminClient client, API api, String tenantDomain, Environment
            environment)
            throws APIManagementException, AxisFault {

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
                    deployInSequence(client, api, tenantId, tenantDomain, environment);
                }

                if (APIUtil.isSequenceDefined(api.getOutSequence())) {
                	deployOutSequence(client, api, tenantId, tenantDomain, environment);
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

    private void deployInSequence(APIGatewayAdminClient sequenceAdminServiceClient, API api, int tenantId, String
            tenantDomain, Environment
            environment)
            throws APIManagementException, AxisFault {

        String inSequenceName = api.getInSequence();
        OMElement inSequence = APIUtil.getCustomSequence(inSequenceName, tenantId, "in", api.getId());

        if (inSequence != null) {
            String inSeqExt = APIUtil.getSequenceExtensionName(api) + APIConstants.API_CUSTOM_SEQ_IN_EXT;
            if (inSequence.getAttribute(new QName("name")) != null) {
                inSequence.getAttribute(new QName("name")).setAttributeValue(inSeqExt);
            }
            sequenceAdminServiceClient.addSequence(inSequence, tenantDomain);
        }
    }

    private void deployOutSequence(APIGatewayAdminClient client, API api, int tenantId, String tenantDomain,
            Environment environment)
            throws APIManagementException, AxisFault {

        String outSequenceName = api.getOutSequence();
        OMElement outSequence = APIUtil.getCustomSequence(outSequenceName, tenantId, "out", api.getId());

        if (outSequence != null) {
            String outSeqExt  = APIUtil.getSequenceExtensionName(api) + APIConstants.API_CUSTOM_SEQ_OUT_EXT;
            if (outSequence.getAttribute(new QName("name")) != null)    {
                outSequence.getAttribute(new QName("name")).setAttributeValue(outSeqExt);
            }
            client.addSequence(outSequence, tenantDomain);
        }
    }

	/**
	 * Undeploy the sequences deployed in synapse
	 * 
	 * @param api
	 * @param tenantDomain
	 * @param environment
	 * @throws APIManagementException
	 */
    private void undeployCustomSequences(APIGatewayAdminClient client, API api, String tenantDomain, Environment
            environment) {

        if (APIUtil.isSequenceDefined(api.getInSequence()) || APIUtil.isSequenceDefined(api.getOutSequence())) {
            try {
                PrivilegedCarbonContext.startTenantFlow();
                if(tenantDomain != null && !"".equals(tenantDomain)){
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
                }
                else{
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain
                            (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
                }

                if (APIUtil.isSequenceDefined(api.getInSequence())) {
                    String inSequence = APIUtil.getSequenceExtensionName(api) + APIConstants.API_CUSTOM_SEQ_IN_EXT;
                    client.deleteSequence(inSequence, tenantDomain);
                }
                if (APIUtil.isSequenceDefined(api.getOutSequence())) {
                    String outSequence = APIUtil.getSequenceExtensionName(api) + APIConstants.API_CUSTOM_SEQ_OUT_EXT;
                    client.deleteSequence(outSequence, tenantDomain);
                }
                if (APIUtil.isSequenceDefined(api.getFaultSequence())) {
                    String faultSequence = APIUtil.getSequenceExtensionName(api) + APIConstants.API_CUSTOM_SEQ_FAULT_EXT;
                    if(client.isExistingSequence(faultSequence, tenantDomain)) {
                        client.deleteSequence(faultSequence, tenantDomain);
                    }                    
                }
            } catch (Exception e) {
                String msg = "Error in deleting the sequence from gateway";
                log.error(msg, e);
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }

    /**
	 * Update the custom sequences in gateway
	 * @param api
	 * @param tenantDomain
	 * @param environment
	 * @throws APIManagementException
	 */
	private void updateCustomSequences(APIGatewayAdminClient client, API api, String tenantDomain, Environment
            environment)
	                                                                                         throws APIManagementException {

        //If sequences have been added, updated or removed.
        if (APIUtil.isSequenceDefined(api.getInSequence()) || APIUtil.isSequenceDefined(api.getOutSequence()) ||
                APIUtil.isSequenceDefined(api.getOldInSequence()) || APIUtil.isSequenceDefined(api.getOldOutSequence())) {

            try {
                PrivilegedCarbonContext.startTenantFlow();
                if(tenantDomain != null && !"".equals(tenantDomain)){
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
                }
                else{
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain
                            (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
                }
                int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();


                //If an inSequence has been added, updated or removed.
                if (APIUtil.isSequenceDefined(api.getInSequence()) || APIUtil.isSequenceDefined(api.getOldInSequence())) {
                    String inSequenceKey = APIUtil.getSequenceExtensionName(api) + APIConstants.API_CUSTOM_SEQ_IN_EXT;
                    //If sequence already exists
                    if (client.isExistingSequence(inSequenceKey, tenantDomain)) {
                        //Delete existing sequence
                        client.deleteSequence(inSequenceKey, tenantDomain);
                    }
                    //If an inSequence has been added or updated.
                    if(APIUtil.isSequenceDefined(api.getInSequence())){
                        //Deploy the inSequence
                        deployInSequence(client, api, tenantId, tenantDomain, environment);
                    }
                }

                //If an outSequence has been added, updated or removed.
                if (APIUtil.isSequenceDefined(api.getOutSequence()) || APIUtil.isSequenceDefined(api.getOldOutSequence())) {
                    String outSequence = APIUtil.getSequenceExtensionName(api) + APIConstants.API_CUSTOM_SEQ_OUT_EXT;
                    //If the outSequence exists.
                    if (client.isExistingSequence(outSequence, tenantDomain)) {
                        //Delete existing outSequence
                        client.deleteSequence(outSequence, tenantDomain);
                    }

                    //If an outSequence has been added or updated.
                    if (APIUtil.isSequenceDefined(api.getOutSequence())){
                        //Deploy outSequence
                        deployOutSequence(client, api, tenantId, tenantDomain, environment);
                    }
                }
            } catch (Exception e) {
                String msg = "Error in updating the sequence at the Gateway";
                log.error(msg, e);
                throw new APIManagementException(msg, e);
            }
            finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }

    }

    private void deployAPIFaultSequence(APIGatewayAdminClient client, API api, String tenantDomain, Environment
            environment)
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
                tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
            }


            //If a fault sequence has be defined.
            if (APIUtil.isSequenceDefined(faultSequenceName)) {
                int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
                //If the sequence already exists
                if (client.isExistingSequence(faultSeqExt, tenantDomain)) {
                    //Delete the sequence. We need to redeploy afterwards since the sequence may have been updated.
                    client.deleteSequence(faultSeqExt, tenantDomain);
                }
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
                        //If the previous sequence was a per API fault sequence delete it
                        if (client.isExistingSequence(faultSequenceName, tenantDomain)) {
                            client.deleteSequence(faultSequenceName, tenantDomain);
                        }
                    }

                    //Deploy the fault sequence
                    client.addSequence(faultSequence, tenantDomain);
                }
            } else {
                if (client.isExistingSequence(faultSeqExt, tenantDomain)) {
                    client.deleteSequence(faultSeqExt, tenantDomain);
                }
            }
        } catch (AxisFault e) {
            String msg = "Error while updating the fault sequence at the Gateway";
            log.error(msg, e);
            throw new APIManagementException(msg, e);
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
     * @param environment
     * @throws APIManagementException
     */
	private void setSecureVaultProperty(APIGatewayAdminClient securityAdminClient, API api, String tenantDomain, Environment
            environment)
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
}

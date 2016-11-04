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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.llom.OMElementImpl;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIStatus;
import org.wso2.carbon.apimgt.gateway.dto.stub.APIData;
import org.wso2.carbon.apimgt.gateway.dto.stub.ResourceData;
import org.wso2.carbon.apimgt.impl.dto.Environment;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.template.APITemplateBuilder;
import org.wso2.carbon.apimgt.impl.utils.APIGatewayAdminClient;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

public class APIGatewayManager {

	private static final Log log = LogFactory.getLog(APIGatewayManager.class);

	private static APIGatewayManager instance;

    private Map<String, Environment> environments;

	private boolean debugEnabled = log.isDebugEnabled();

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
        for (String environmentName : api.getEnvironments()) {
            Environment environment = environments.get(environmentName);
            //If the environment is removed from the configuration, continue without publishing
            if (environment == null) {
                continue;
            }
            APIGatewayAdminClient client;
            try {
                client = new APIGatewayAdminClient(api.getId(), environment);
			String operation;
			// If the API exists in the Gateway
			if (client.getApi(tenantDomain, api.getId()) != null) {

				// If the Gateway type is 'production' and the production url
				// has been removed
				// Or if the Gateway type is 'sandbox' and the sandbox url has
				// been removed.
				if ((APIConstants.GATEWAY_ENV_TYPE_PRODUCTION.equals(environment.getType()) && !APIUtil.isProductionEndpointsExists(api)) ||
				    (APIConstants.GATEWAY_ENV_TYPE_SANDBOX.equals(environment.getType()) && !APIUtil.isSandboxEndpointsExists(api))) {
					if (debugEnabled) {
						log.debug("Removing API " + api.getId().getApiName() +
						          " from Environment " + environment.getName() +
						          " since its relevant URL has been removed.");
					}
					// We need to remove the api from the environment since its
					// relevant url has been removed.
					operation ="delete";
					client.deleteApi(tenantDomain, api.getId());
                    if(api.isPublishedDefaultVersion()){
                        if(client.getDefaultApi(tenantDomain, api.getId())!=null){
                            client.deleteDefaultApi(tenantDomain, api.getId());
                        }
                    }
					setSecureVaultProperty(api, tenantDomain, environment, operation);
					undeployCustomSequences(api,tenantDomain, environment);
				} else {
					if (debugEnabled) {
						log.debug("API exists, updating existing API " + api.getId().getApiName() +
						          " in environment " + environment.getName());
					}
                    //Deploy the fault sequence first since it has to be available by the time the API is deployed.
                    deployAPIFaultSequence(api, tenantDomain, environment);

                    operation ="update";

                    //Update the API
                    if(api.getImplementation().equalsIgnoreCase(APIConstants.IMPLEMENTATION_TYPE_INLINE)){
                        client.updateApiForInlineScript(builder, tenantDomain, api.getId());
                    }else if (api.getImplementation().equalsIgnoreCase(APIConstants.IMPLEMENTATION_TYPE_ENDPOINT)){
                        client.updateApi(builder, tenantDomain, api.getId());
                    }

                    if(api.isDefaultVersion() || api.isPublishedDefaultVersion()){//api.isPublishedDefaultVersion() check is used to detect and update when context etc. is changed in the api which is not the default version but has a published default api
                        if(client.getDefaultApi(tenantDomain, api.getId())!=null){
                            client.updateDefaultApi(builder, tenantDomain, api.getId().getVersion(), api.getId());
                        }else{
                            client.addDefaultAPI(builder, tenantDomain, api.getId().getVersion(), api.getId());
                        }
                    }
					setSecureVaultProperty(api, tenantDomain, environment, operation);

                    //Update the custom sequences of the API
					updateCustomSequences(api, tenantDomain, environment);
				}
			} else {
				// If the Gateway type is 'production' and a production url has
				// not been specified
				// Or if the Gateway type is 'sandbox' and a sandbox url has not
				// been specified
				if ((APIConstants.GATEWAY_ENV_TYPE_PRODUCTION.equals(environment.getType()) && !APIUtil.isProductionEndpointsExists(api)) ||
				    (APIConstants.GATEWAY_ENV_TYPE_SANDBOX.equals(environment.getType()) && !APIUtil.isSandboxEndpointsExists(api))) {

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
                    deployAPIFaultSequence(api, tenantDomain, environment);

                    operation ="add";
                    if(!api.isWS()){
                    //Add the API
                    if(APIConstants.IMPLEMENTATION_TYPE_INLINE.equalsIgnoreCase(api.getImplementation())){
                        client.addPrototypeApiScriptImpl(builder, tenantDomain, api.getId());
                    }else if (APIConstants.IMPLEMENTATION_TYPE_ENDPOINT.equalsIgnoreCase(api.getImplementation())){
                        client.addApi(builder, tenantDomain, api.getId());
                    }

                    if(api.isDefaultVersion()){
                        if(client.getDefaultApi(tenantDomain, api.getId())!=null){
                            client.updateDefaultApi(builder,tenantDomain,api.getId().getVersion(), api.getId());
                        }else{
                            client.addDefaultAPI(builder,tenantDomain,api.getId().getVersion(), api.getId());
                        }
                    }
					setSecureVaultProperty(api, tenantDomain, environment, operation);

                    //Deploy the custom sequences of the API.
					deployCustomSequences(api, tenantDomain, environment);
                    } else {
                        deployWebsocketAPI(api, client);
                    }

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
        if (api.getEnvironments() != null) {
            for (String environmentName : api.getEnvironments()) {
                try {
                    Environment environment = environments.get(environmentName);
                    //If the environment is removed from the configuration, continue without removing
                    if (environment == null) {
                        continue;
                    }
                    APIGatewayAdminClient client =
                            new APIGatewayAdminClient(api.getId(), environment);
                    if (!api.isWS()) {
                        if (client.getApi(tenantDomain, api.getId()) != null) {
                            if (debugEnabled) {
                                log.debug("Removing API " + api.getId().getApiName() +
                                          " From environment " +
                                          environment.getName());
                            }
                            String operation = "delete";

                            client.deleteApi(tenantDomain, api.getId());
                            undeployCustomSequences(api, tenantDomain, environment);

                            setSecureVaultProperty(api, tenantDomain, environment, operation);
                        }
                    } else {
                        String fileName = api.getContext().substring(1).replace('/', '-');
                        String[] fileNames = new String[2];
                        fileNames[0] = "_PRODUCTION_" + fileName;
                        fileNames[1] = "_SANDBOX_" + fileName;
                        client.undeployWSApi(new String[] {  });
                    }

                    if (api.isPublishedDefaultVersion()) {
                        if (client.getDefaultApi(tenantDomain, api.getId()) != null) {
                            client.deleteDefaultApi(tenantDomain, api.getId());
                        }
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
                } catch (APIManagementException ex) {
                    /*
                    didn't throw this exception to handle multiple gateway publishing
                    if gateway is unreachable we collect that environments into map with issue and show on popup in ui
                    therefore this didn't break the gateway unpublisihing if one gateway unreachable
                    */
                    log.error("Error occurred undeploy sequences on " + environmentName, ex);
                    failedEnvironmentsMap.put(environmentName, ex.getMessage());
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
            throws APIManagementException {
        try {
            JSONObject obj = new JSONObject(api.getEndpointConfig());
            String production_endpoint = obj.getJSONObject("production_endpoints").getString("url");
            String sandbox_endpoint = obj.getJSONObject("sandbox_endpoints").getString("url");
            OMElement element;
            String context;
            context = api.getContext();
            try {
                if (production_endpoint != null) {
                    api.setContext("_PRODUCTION_" + context);
                    String content = createSeqString(api, production_endpoint);
                    element = AXIOMUtil.stringToOM(content);
                    String fileName = element.getAttributeValue(new QName("name"));
                    client.deployWSApi(content, fileName);
                }
                if (sandbox_endpoint != null) {
                    api.setContext("_SANDBOX_" + context);
                    String content = createSeqString(api, sandbox_endpoint);
                    element = AXIOMUtil.stringToOM(content);
                    String fileName = element.getAttributeValue(new QName("name"));
                    client.deployWSApi(content, fileName);
                }

            } catch (XMLStreamException e) {
                String msg = "Error while parsing the policy to get the eligibility query: ";
                log.error(msg, e);
                throw new APIManagementException(msg);
            } catch (IOException e) {
                String msg = "Error while deploying the policy in gateway manager: ";
                log.error(msg, e);
                throw new APIManagementException(msg);
            }
        } catch (JSONException e) {
            log.error("Error in reading JSON object " + e.getMessage(), e);
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
                client = new APIGatewayAdminClient(api.getId(), environment);
                gatewayManager.deployWebsocketAPI(api, client);
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
    public String createSeqString(API api, String url) {

        String context = api.getContext();
        String seq = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                     "<sequence xmlns=\"http://ws.apache.org/ns/synapse\" name=\"" +
                     context.replace('/', '-') + "\">\n" +
                     "   <send>\n" +
                     "      <endpoint>\n" +
                     "         <http method=\"GET\"\n" +
                     "               uri-template=\"" + url + "\"/>\n" +
                     "      </endpoint>\n" +
                     "   </send>\n" +
                     "</sequence>";
        return seq;

    }



    public Map<String, String> removeDefaultAPIFromGateway(API api, String tenantDomain) {
        Map<String, String> failedEnvironmentsMap = new HashMap<String, String>(0);
        if (api.getEnvironments() != null) {
            for (String environmentName : api.getEnvironments()) {
                try {
                    Environment environment = environments.get(environmentName);
                    APIGatewayAdminClient client = new APIGatewayAdminClient(api.getId(), environment);
                    if (client.getDefaultApi(tenantDomain, api.getId()) != null) {
                        if (debugEnabled) {
                            log.debug("Removing Default API " + api.getId().getApiName() + " From environment " +
                                      environment.getName());
                        }
                        client.deleteDefaultApi(tenantDomain, api.getId());
                    }
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
                APIGatewayAdminClient client = new APIGatewayAdminClient(api.getId(), environment);
                // If the API exists in at least one environment, consider as
                // published and return true.
                if (client.getApi(tenantDomain, api.getId()) != null) {
                    return true;
                }
            } catch (AxisFault axisFault) {
                /*
                didn't throw this exception to check api available in all the environments
                therefore we didn't throw exception to avoid if gateway unreachable affect
                */
                if (api.getStatus() != APIStatus.CREATED) {
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
                APIGatewayAdminClient client = new APIGatewayAdminClient(api.getId(), environment);
                if (client.getApi(tenantDomain, api.getId()) != null) {
                    APIData apiData = client.getApi(tenantDomain, api.getId());
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
                if (api.getStatus() != APIStatus.CREATED) {
                    log.error("Error occurred when check api endpoint security type on gateway"
                                    + environment.getName(), axisFault);
                }
            }
        }
        return APIConstants.APIEndpointSecurityConstants.BASIC_AUTH;
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
    private void deployCustomSequences(API api, String tenantDomain, Environment environment)
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
                    deployInSequence(api, tenantId, tenantDomain, environment);
                }

                if (APIUtil.isSequenceDefined(api.getOutSequence())) {
                	deployOutSequence(api, tenantId, tenantDomain, environment);
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

    private void deployInSequence(API api, int tenantId, String tenantDomain, Environment environment)
            throws APIManagementException, AxisFault {

        String inSequenceName = api.getInSequence();
        OMElement inSequence = APIUtil.getCustomSequence(inSequenceName, tenantId, "in", api.getId());

        if (inSequence != null) {
            String inSeqExt = APIUtil.getSequenceExtensionName(api) + APIConstants.API_CUSTOM_SEQ_IN_EXT;
            if (inSequence.getAttribute(new QName("name")) != null) {
                inSequence.getAttribute(new QName("name")).setAttributeValue(inSeqExt);
            }
            APIGatewayAdminClient sequenceAdminServiceClient = new APIGatewayAdminClient(api.getId(), environment);
            sequenceAdminServiceClient.addSequence(inSequence, tenantDomain);
        }
    }

    private void deployOutSequence(API api, int tenantId, String tenantDomain, Environment environment)
            throws APIManagementException, AxisFault {

        String outSequenceName = api.getOutSequence();
        OMElement outSequence = APIUtil.getCustomSequence(outSequenceName, tenantId, "out", api.getId());

        if (outSequence != null) {
            String outSeqExt  = APIUtil.getSequenceExtensionName(api) + APIConstants.API_CUSTOM_SEQ_OUT_EXT;
            if (outSequence.getAttribute(new QName("name")) != null)    {
                outSequence.getAttribute(new QName("name")).setAttributeValue(outSeqExt);
            }
            APIGatewayAdminClient client = new APIGatewayAdminClient(api.getId(), environment);
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
    private void undeployCustomSequences(API api, String tenantDomain, Environment environment) {

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
                APIGatewayAdminClient client = new APIGatewayAdminClient(api.getId(), environment);

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
	private void updateCustomSequences(API api, String tenantDomain, Environment environment)
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

                APIGatewayAdminClient client = new APIGatewayAdminClient(api.getId(), environment);

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
                        deployInSequence(api, tenantId, tenantDomain, environment);
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
                        deployOutSequence(api, tenantId, tenantDomain, environment);
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

    private void deployAPIFaultSequence(API api, String tenantDomain, Environment environment)
            throws APIManagementException {

        String faultSequenceName = api.getFaultSequence();
        String faultSeqExt = APIUtil.getSequenceExtensionName(api) + APIConstants.API_CUSTOM_SEQ_FAULT_EXT;

        //If a fault sequence has be defined.
        if (APIUtil.isSequenceDefined(faultSequenceName)) {
            try {
                PrivilegedCarbonContext.startTenantFlow();
                if (tenantDomain != null && !"".equals(tenantDomain)) {
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
                } else {
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain
                            (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
                }
                int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

                APIGatewayAdminClient client = new APIGatewayAdminClient(api.getId(), environment);

                //If the sequence already exists
                if (client.isExistingSequence(faultSequenceName, tenantDomain)) {
                    //Delete the sequence. We need to redeploy afterwards since the sequence may have been updated.
                    client.deleteSequence(faultSequenceName, tenantDomain);
                }
                //Get the fault sequence xml
                OMElement faultSequence = APIUtil.getCustomSequence(faultSequenceName, tenantId, 
                                                            APIConstants.API_CUSTOM_SEQUENCE_TYPE_FAULT, api.getId());

                if (faultSequence != null) {
                    if (APIUtil.isPerAPISequence(faultSequenceName, tenantId, api.getId(), 
                                                 APIConstants.API_CUSTOM_SEQUENCE_TYPE_FAULT)) {
                        if (faultSequence.getAttribute(new QName("name")) != null)    {
                            faultSequence.getAttribute(new QName("name")).setAttributeValue(faultSeqExt);
                        }
                    } else {
                        //If the previous sequence was a per API fault sequence delete it
                        if (client.isExistingSequence(faultSeqExt, tenantDomain)) {
                            client.deleteSequence(faultSeqExt, tenantDomain);
                        }
                    }

                    //Deploy the fault sequence
                    client.addSequence(faultSequence, tenantDomain);
                }
            } catch (Exception e) {
                String msg = "Error in updating the fault sequence at the Gateway";
                log.error(msg, e);
                throw new APIManagementException(msg, e);
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }

       
    /**
     * Store the secured endpoint username password to registry
     * @param api
     * @param tenantDomain
     * @param environment
     * @param operation -add,delete,update operations for an API
     * @throws APIManagementException
     */
	private void setSecureVaultProperty(API api, String tenantDomain, Environment environment, String operation)
            throws APIManagementException {
		boolean isSecureVaultEnabled = Boolean.parseBoolean(ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().
		                                                    getAPIManagerConfiguration().getFirstProperty(APIConstants.API_SECUREVAULT_ENABLE));
		if (api.isEndpointSecured() && isSecureVaultEnabled) {
			try {							
				APIGatewayAdminClient securityAdminClient = new APIGatewayAdminClient(api.getId(), environment);
				if("add".equals(operation)){
				securityAdminClient.addSecureVaultProperty(api, tenantDomain);
				} else if("update".equals(operation)){
					securityAdminClient.updateSecureVaultProperty(api, tenantDomain);
				} else if("delete".equals(operation)){
					securityAdminClient.deleteSecureVaultProperty(api, tenantDomain);
				}

			} catch (Exception e) {
				String msg = "Error in setting secured password.";
                log.error(msg + ' ' + e.getLocalizedMessage(), e);
				throw new APIManagementException(msg);
			}
		}
	}
}

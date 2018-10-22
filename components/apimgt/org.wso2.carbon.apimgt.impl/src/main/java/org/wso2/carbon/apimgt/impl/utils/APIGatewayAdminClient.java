/*
* Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.apimgt.impl.utils;

import org.apache.axis2.AxisFault;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.gateway.stub.APIGatewayAdminStub;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.EndpointAdminException;
import org.wso2.carbon.apimgt.impl.dto.Environment;
import org.wso2.carbon.apimgt.impl.template.APITemplateBuilder;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.apimgt.gateway.dto.stub.APIData;
import org.apache.axiom.om.OMElement;

import java.io.StringWriter;
import java.rmi.RemoteException;
import java.util.ArrayList;

public class APIGatewayAdminClient extends AbstractAPIGatewayAdminClient {

    private APIGatewayAdminStub apiGatewayAdminStub;
    private Environment environment;
    private static Log log = LogFactory.getLog(APIGatewayAdminClient.class);

    public APIGatewayAdminClient(APIIdentifier apiId, Environment environment) throws AxisFault {
        //String qualifiedName = apiId.getProviderName() + "--" + apiId.getApiName() + ":v" + apiId.getVersion();
        //String qualifiedDefaultApiName = apiId.getProviderName() + "--" + apiId.getApiName();
        //String providerDomain = apiId.getProviderName();
        //providerDomain = APIUtil.replaceEmailDomainBack(providerDomain);
        apiGatewayAdminStub = new APIGatewayAdminStub(null, environment.getServerURL() + "APIGatewayAdmin");
        setup(apiGatewayAdminStub, environment);
        this.environment = environment;

        CarbonUtils.setBasicAccessSecurityHeaders(environment.getUserName(), environment.getPassword(),
                apiGatewayAdminStub._getServiceClient());
    }

    /**
     * Store the encrypted password into the registry with the unique property name.
     * Property name is constructed as "Provider+ ApiName +Version"
     *
     * @param api - The api
     * @param tenantDomain - The Tenant Domain
     * @throws APIManagementException
     */
    public void setSecureVaultProperty(API api, String tenantDomain) throws APIManagementException {
        try {
            String secureVaultAlias = api.getId().getProviderName() +
                                      "--" + api.getId().getApiName() + api.getId().getVersion();
            apiGatewayAdminStub.doEncryption(tenantDomain, secureVaultAlias, api.getEndpointUTPassword());
        } catch (Exception e) {
            String msg = "Failed to set secure vault property for the tenant : " + tenantDomain + e.getMessage();
            throw new APIManagementException(msg, e);
        }
    }


    /**
     * Add the API to the gateway
     *
     * @param builder - APITemplateBuilder instance
     * @param tenantDomain - The Tenant Domain
     * @throws AxisFault
     */
    public void addApi(APITemplateBuilder builder, String tenantDomain, APIIdentifier apiId) throws AxisFault {
        try {

            String apiConfig = builder.getConfigStringForTemplate(environment);
            if (tenantDomain != null && !("").equals(tenantDomain)
                && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                apiGatewayAdminStub.addApiForTenant(apiId.getProviderName(), apiId.getApiName(), apiId.getVersion(), apiConfig, tenantDomain);
            } else {
                apiGatewayAdminStub.addApi(apiId.getProviderName(), apiId.getApiName(), apiId.getVersion(), apiConfig);
            }
        } catch (Exception e) {
            throw new AxisFault("Error while publishing API to the Gateway. " + e.getMessage(), e);
        }
    }

    /**
     * Add the API to the gateway
     *
     * @param builder - APITemplateBuilder instance
     * @param tenantDomain - The Tenant Domain
     * @throws AxisFault
     */
    public void addPrototypeApiScriptImpl(APITemplateBuilder builder, String tenantDomain, APIIdentifier apiId)
            throws AxisFault {
        try {

            String apiConfig = builder.getConfigStringForPrototypeScriptAPI(environment);
            if (tenantDomain != null && !("").equals(tenantDomain)
                && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                apiGatewayAdminStub.addApiForTenant(apiId.getProviderName(), apiId.getApiName(), apiId.getVersion(), apiConfig, tenantDomain);
            } else {
                apiGatewayAdminStub.addApi(apiId.getProviderName(), apiId.getApiName(), apiId.getVersion(), apiConfig);
            }
        } catch (Exception e) {
            throw new AxisFault("Error while publishing prototype API to the Gateway. " + e.getMessage(), e);
        }
    }

    public void addDefaultAPI(APITemplateBuilder builder, String tenantDomain, String defaultVersion,
                              APIIdentifier apiId) throws AxisFault {

        try {
            String apiConfig = builder.getConfigStringForDefaultAPITemplate(defaultVersion);
            if (tenantDomain != null && !("").equals(tenantDomain)
                && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                apiGatewayAdminStub.addDefaultAPIForTenant(apiId.getProviderName(), apiId.getApiName(), apiId.getVersion(), apiConfig,
                                                           tenantDomain);
            } else {
                apiGatewayAdminStub.addDefaultAPI(apiId.getProviderName(), apiId.getApiName(), apiId.getVersion(), apiConfig);
            }
        } catch (Exception e) {
            throw new AxisFault("Error publishing default API to the Gateway. " + e.getMessage(), e);
        }
    }

    /**
     * Get API from the gateway
     *
     * @param tenantDomain - The Tenant Domain
     * @return - An APIData instance
     * @throws AxisFault
     */
    public APIData getApi(String tenantDomain, APIIdentifier apiId) throws AxisFault {
        try {
            APIData apiData;
            if (tenantDomain != null && !("").equals(tenantDomain)
                && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                apiData = apiGatewayAdminStub.getApiForTenant(apiId.getProviderName(), apiId.getApiName(), apiId.getVersion(), tenantDomain);
            } else {
                apiData = apiGatewayAdminStub.getApi(apiId.getProviderName(), apiId.getApiName(), apiId.getVersion());
            }
            return apiData;
        } catch (Exception e) {
            throw new AxisFault("Error while obtaining API information from gateway. " + e.getMessage(), e);
        }
    }

    public APIData getDefaultApi(String tenantDomain, APIIdentifier apiId) throws AxisFault {
        try {
            APIData apiData;
            if (tenantDomain != null && !("").equals(tenantDomain)
                && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                apiData = apiGatewayAdminStub.getDefaultApiForTenant(apiId.getProviderName(), apiId.getApiName(),
                                                                     apiId.getVersion(), tenantDomain);
            } else {
                apiData = apiGatewayAdminStub.getDefaultApi(apiId.getProviderName(), apiId.getApiName(), apiId.getVersion());
            }
            return apiData;
        } catch (Exception e) {
            throw new AxisFault("Error while obtaining default API information from gateway." + e.getMessage(), e);
        }
    }

    /**
     * Update the API in the Gateway
     *
     * @param builder - APITemplateBuilder instance
     * @param tenantDomain - The Tenant Domain
     * @throws AxisFault
     */
    public void updateApi(APITemplateBuilder builder, String tenantDomain, APIIdentifier apiId) throws AxisFault {
        try {
            String apiConfig = builder.getConfigStringForTemplate(environment);
            if (tenantDomain != null && !("").equals(tenantDomain) &&
                !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {

                apiGatewayAdminStub.updateApiForTenant(apiId.getProviderName(), apiId.getApiName(), apiId.getVersion(), apiConfig, tenantDomain);
            } else {
                apiGatewayAdminStub.updateApi(apiId.getProviderName(), apiId.getApiName(), apiId.getVersion(), apiConfig);
            }
        } catch (Exception e) {
            throw new AxisFault("Error while updating API in the gateway. " + e.getMessage(), e);
        }
    }

    /**
     * Update the API in the Gateway
     *
     * @param builder - APITemplateBuilder instance
     * @param tenantDomain - The Tenant Domain
     * @throws AxisFault
     */
    public void updateApiForInlineScript(APITemplateBuilder builder, String tenantDomain, APIIdentifier apiId)
            throws AxisFault {
        try {
            String apiConfig = builder.getConfigStringForPrototypeScriptAPI(environment);
            if (tenantDomain != null && !("").equals(tenantDomain) &&
                !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {

                apiGatewayAdminStub.updateApiForTenant(apiId.getProviderName(), apiId.getApiName(), apiId.getVersion(), apiConfig, tenantDomain);
            } else {
                apiGatewayAdminStub.updateApi(apiId.getProviderName(), apiId.getApiName(), apiId.getVersion(), apiConfig);
            }
        } catch (Exception e) {
            throw new AxisFault("Error while updating prototype API in the gateway. " + e.getMessage(), e);
        }
    }

    public void updateDefaultApi(APITemplateBuilder builder, String tenantDomain, String defaultVersion,
                                 APIIdentifier apiId) throws AxisFault {
        try {
            String apiConfig = builder.getConfigStringForDefaultAPITemplate(defaultVersion);
            if (tenantDomain != null && !("").equals(tenantDomain) &&
                !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {

                apiGatewayAdminStub.updateDefaultApiForTenant(apiId.getProviderName(), apiId.getApiName(), apiId.getVersion(),
                                                              apiConfig,
                                                              tenantDomain);
            } else {
                apiGatewayAdminStub.updateDefaultApi(apiId.getProviderName(), apiId.getApiName(), apiId.getVersion(), apiConfig);
            }
        } catch (Exception e) {
            throw new AxisFault("Error while updating default API in the gateway. " + e.getMessage(), e);
        }
    }


    /**
     * Delete the API from Gateway
     *
     * @param tenantDomain - The Tenant Domain
     * @throws AxisFault
     */
    public void deleteApi(String tenantDomain, APIIdentifier apiId) throws AxisFault {
        try {
            if (tenantDomain != null && !("").equals(tenantDomain) &&
                !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                apiGatewayAdminStub.deleteApiForTenant(apiId.getProviderName(), apiId.getApiName(), apiId.getVersion(), tenantDomain);
            } else {
                apiGatewayAdminStub.deleteApi(apiId.getProviderName(), apiId.getApiName(), apiId.getVersion());
            }

        } catch (Exception e) {
            throw new AxisFault("Error while deleting API from the gateway. " + e.getMessage(), e);
        }
    }

    public void deleteDefaultApi(String tenantDomain, APIIdentifier apiId) throws AxisFault {
        try {
            if (tenantDomain != null && !("").equals(tenantDomain) &&
                !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                apiGatewayAdminStub.deleteDefaultApiForTenant(apiId.getProviderName(), apiId.getApiName(), apiId.getVersion(), tenantDomain);
            } else {
                apiGatewayAdminStub.deleteDefaultApi(apiId.getProviderName(), apiId.getApiName(), apiId.getVersion());
            }

        } catch (Exception e) {
            throw new AxisFault("Error while deleting default API from the gateway. " + e.getMessage(), e);
        }
    }

    /**
     * Add the endpoint file/s to the gateway
     *
     * @param api API that the endpoint/s belong
     * @param builder The endpoint template builder instance
     * @param tenantDomain Domain of the logged tenant
     * @throws AxisFault Thrown if an error occurred
     */
    public void addEndpoint(API api, APITemplateBuilder builder, String tenantDomain) throws EndpointAdminException {
        try {
            ArrayList<String> arrayList = getEndpointType(api);
            log.debug("Adding endpoint to gateway");
            for (String type : arrayList) {
                String endpointConfigContext = builder.getConfigStringForEndpointTemplate(type);
                checkForTenantWhenAdding(apiGatewayAdminStub, endpointConfigContext, tenantDomain);
            }
        } catch (Exception e) {
            log.error("Error while adding endpoint to the gateway", e);
            throw new EndpointAdminException("Error while generating Endpoint file in Gateway " + e.getMessage(), e);
        }
    }

    /**
     * Delete the endpoint file/s from the gateway
     *
     * @param api API that the endpoint/s belong
     * @param tenantDomain Domain of the logged tenant
     * @throws AxisFault Thrown if an error occurred
     */
    public void deleteEndpoint(API api, String tenantDomain) throws EndpointAdminException {
        try {
            String endpointName = api.getId().getApiName() + "--v" + api.getId().getVersion();
            ArrayList<String> arrayList = getEndpointType(api);
            log.debug("Deleting endpoint from gateway");
            for (String type : arrayList) {
                String endpointType = type.replace("_endpoints", "");
                if (!StringUtils.isEmpty(tenantDomain)
                        && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                    apiGatewayAdminStub.deleteEndpointForTenant(endpointName + "_API" + endpointType +
                                    "Endpoint", tenantDomain);
                } else {
                    apiGatewayAdminStub.deleteEndpoint(endpointName + "_API" + endpointType +
                            "Endpoint");
                }
            }
        } catch (Exception e) {
            log.error("Error while deleting endpoint from the gateway", e);
            throw new EndpointAdminException("Error while deleting Endpoint from the gateway. " + e.getMessage(), e);
        }
    }

    /**
     * Update the endpoint file/s in the gateway
     *
     * @param api API that the endpoint/s belong
     * @param builder The endpoint template builder instance
     * @param tenantDomain Domain of the logged tenant
     * @throws AxisFault Thrown if an error occurred
     */
    public void saveEndpoint(API api, APITemplateBuilder builder, String tenantDomain) throws EndpointAdminException {
        try {
            log.debug("Removing endpoints from gateway to update");
            apiGatewayAdminStub.removeEndpointsToUpdate(api.getId().getApiName(), api.getId().getVersion(),
                    tenantDomain);

            log.debug("Adding updated endpoints to gateway");
            ArrayList<String> arrayListToAdd = getEndpointType(api);
            for (String type : arrayListToAdd) {
                String endpointConfigContext = builder.getConfigStringForEndpointTemplate(type);
                checkForTenantWhenAdding(apiGatewayAdminStub, endpointConfigContext, tenantDomain);
            }
        } catch (Exception e) {
            log.error("Error while updating endpoint file in the gateway", e);
            throw new EndpointAdminException("Error updating Endpoint file" + e.getMessage(), e);
        }
    }

    /**
     * Check if add the endpoint for tenant space or not
     *
     * @param stub Stub instance for adding the endpoint
     * @param epContext The content of the endpoint file
     * @param tenantDomain Domain of the logged tenant
     * @throws AxisFault Thrown if an error occurred
     */
    private void checkForTenantWhenAdding(APIGatewayAdminStub stub, String epContext, String tenantDomain)
            throws EndpointAdminException {
        try {
            if (!StringUtils.isEmpty(tenantDomain)
                    && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                stub.addEndpointForTenant(epContext, tenantDomain);
            } else {
                stub.addEndpoint(epContext);
            }
        } catch (Exception e) {
            log.error("Error while adding endpoint to the gateway", e);
            throw new EndpointAdminException("Error while generating endpoint file in gateway" + e.getMessage(), e);
        }
    }

    /**
     * Returns the defined endpoint types of the in the publisher
     *
     * @param api API that the endpoint/s belong
     * @return ArrayList containing defined endpoint types
     * @throws ParseException Thrown if an error occurred
     */
    public ArrayList<String> getEndpointType(API api) throws ParseException {
        ArrayList<String> arrayList = new ArrayList<>();
        if (APIUtil.isProductionEndpointsExists(api) && !APIUtil.isSandboxEndpointsExists(api)) {
            arrayList.add(APIConstants.API_DATA_PRODUCTION_ENDPOINTS);
        } else if (APIUtil.isSandboxEndpointsExists(api) && !APIUtil.isProductionEndpointsExists(api)) {
            arrayList.add(APIConstants.API_DATA_SANDBOX_ENDPOINTS);
        } else {
            arrayList.add(APIConstants.API_DATA_PRODUCTION_ENDPOINTS);
            arrayList.add(APIConstants.API_DATA_SANDBOX_ENDPOINTS);
        }
        return arrayList;
    }

    /**
     * Deploy the sequence to the gateway
     *
     * @param sequence     - The sequence element , which to be deployed in synapse
     * @param tenantDomain - The Tenant Domain
     * @throws AxisFault
     */
    public void addSequence(OMElement sequence, String tenantDomain) throws AxisFault {
        try {
            StringWriter writer = new StringWriter();
            sequence.serializeAndConsume(writer);
            if (tenantDomain != null && !("").equals(tenantDomain) &&
                !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                apiGatewayAdminStub.addSequenceForTenant(writer.toString(), tenantDomain);
            } else {
                apiGatewayAdminStub.addSequence(writer.toString());
            }

        } catch (Exception e) {
            throw new AxisFault("Error while adding new sequence", e);
        }
    }

    /**
     * Undeploy the sequence from gateway
     *
     * @param sequenceName -The sequence name, which need to be undeployed from synapse configuration
     * @param tenantDomain - The Tenant Domain
     * @throws AxisFault
     */
    public void deleteSequence(String sequenceName, String tenantDomain) throws AxisFault {
        try {
            if (tenantDomain != null && !("").equals(tenantDomain) &&
                !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                apiGatewayAdminStub.deleteSequenceForTenant(sequenceName, tenantDomain);
            } else {
                apiGatewayAdminStub.deleteSequence(sequenceName);
            }

        } catch (Exception e) {
            throw new AxisFault("Error while deleting sequence", e);
        }
    }

    /**
     * get the sequence from gateway
     *
     * @param sequenceName - The sequence name,
     * @param tenantDomain - The Tenant Domain
     * @throws AxisFault
     */
    public OMElement getSequence(String sequenceName, String tenantDomain) throws AxisFault {
        try {
            if (tenantDomain != null && !("").equals(tenantDomain) &&
                !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                return (OMElement) apiGatewayAdminStub.getSequenceForTenant(sequenceName, tenantDomain);
            } else {
                return (OMElement) apiGatewayAdminStub.getSequence(sequenceName);
            }

        } catch (Exception e) {
            throw new AxisFault("Error while retriving the sequence", e);
        }
    }

    public boolean isExistingSequence(String sequenceName, String tenantDomain) throws AxisFault {
        try {
            if (tenantDomain != null && !("").equals(tenantDomain) &&
                !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                return apiGatewayAdminStub.isExistingSequenceForTenant(sequenceName, tenantDomain);
            } else {
                return apiGatewayAdminStub.isExistingSequence(sequenceName);
            }
        } catch (Exception e) {
            throw new AxisFault("Error while checking for existence of sequence : " + sequenceName +
                                " in tenant " + tenantDomain, e);
        }
    }


   /**
     * deploying policy file
     *
     * @param content  content to be deployed
     * @param fileName name of the file
     * @throws AxisFault
     */
    public void deployPolicy(String content, String fileName) throws AxisFault {
        try {
            apiGatewayAdminStub.deployPolicy(content, fileName);
        } catch (RemoteException e) {
            throw new AxisFault("Error occured in deploying policy file " + fileName, e);
        }
    }

    /**
     * removing policy file
     *
     * @param fileNames name of the file to be removed
     * @throws AxisFault
     */
    public void undeployPolicy(String[] fileNames) throws AxisFault {
        try {
            apiGatewayAdminStub.undeployPolicy(fileNames);
        } catch (RemoteException e) {
            throw new AxisFault("Error occured in removing policy file ", e);
        }
    }

    /**
     * Add certificate to the gateway nodes.
     *
     * @param certificate : Base64 encoded certificate string.
     * @param alias       : The alias for the certificate.
     * @return : True if the certificate is added to trust store. False otherwise.
     * @throws AxisFault
     */
    public boolean addCertificate(String certificate, String alias) throws AxisFault {
        try {
            return apiGatewayAdminStub.addCertificate(certificate, alias);
        } catch (RemoteException e) {
            throw new AxisFault("Error adding certificate file", e);
        }
    }

    /**
     * Add client certificate to the gateway nodes.
     *
     * @param certificate : Base64 encoded certificate string.
     * @param alias       : The alias for the certificate.
     * @return : True if the certificate is added to trust store. False otherwise.
     * @throws AxisFault Axis Fault
     */
    public boolean addClientCertificate(String certificate, String alias) throws AxisFault {
        try {
            return apiGatewayAdminStub.addClientCertificate(certificate, alias);
        } catch (RemoteException e) {
            throw new AxisFault("Error while adding client certificate file with alias " + alias, e);
        }
    }

    /**
     * Delete client certificate from the gateway nodes.
     *
     * @param alias : The alias for the certificate.
     * @return : True if the certificate is deleted from trust store. False otherwise.
     * @throws AxisFault Axis Fault
     */
    public boolean deleteClientCertificate(String alias) throws AxisFault {
        try {
            return apiGatewayAdminStub.deleteClientCertificate(alias);
        } catch (RemoteException e) {
            throw new AxisFault("Error while adding deleting client certificate file " + alias, e);
        }
    }

    /**
     * Delete the certificate from gateway node.
     *
     * @param alias : The alias of the certificate which needs to be removed.
     * @return : True if the certificate is removed successfully. False otherwise.
     * @throws AxisFault
     */
    public boolean deleteCertificate(String alias) throws AxisFault {
        try {
            return apiGatewayAdminStub.deleteCertificate(alias);
        } catch (RemoteException e) {
            throw new AxisFault("Error deleting certificate", e);
        }
    }
}

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
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.dto.CredentialDto;
import org.wso2.carbon.apimgt.api.dto.GatewayAPIDTO;
import org.wso2.carbon.apimgt.api.dto.GatewayContentDTO;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.gateway.stub.APIGatewayAdminStub;
import org.wso2.carbon.apimgt.impl.dto.Environment;
import org.wso2.carbon.apimgt.keymgt.client.internal.ServiceReferenceHolder;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.apimgt.gateway.dto.stub.APIData;

import java.rmi.RemoteException;

public class APIGatewayAdminClient extends AbstractAPIGatewayAdminClient {

    private APIGatewayAdminStub apiGatewayAdminStub;
    private static Log log = LogFactory.getLog(APIGatewayAdminClient.class);

    public APIGatewayAdminClient(Environment environment) throws AxisFault {
        //String qualifiedName = apiId.getProviderName() + "--" + apiId.getApiName() + ":v" + apiId.getVersion();
        //String qualifiedDefaultApiName = apiId.getProviderName() + "--" + apiId.getApiName();
        //String providerDomain = apiId.getProviderName();
        //providerDomain = APIUtil.replaceEmailDomainBack(providerDomain);
        ConfigurationContext ctx = ServiceReferenceHolder.getInstance().getAxis2ConfigurationContext();
        apiGatewayAdminStub = new APIGatewayAdminStub(ctx, environment.getServerURL() + "APIGatewayAdmin");
        setup(apiGatewayAdminStub, environment);

        CarbonUtils.setBasicAccessSecurityHeaders(environment.getUserName(), environment.getPassword(),
                apiGatewayAdminStub._getServiceClient());
    }

    /**
     * Store the encrypted password into the registry with the unique property name.
     * Property name is constructed as "Provider+ ApiName +Version"
     *
     * @param api          - The api
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
                apiData = apiGatewayAdminStub.getApiForTenant(apiId.getProviderName(), apiId.getApiName(),
                        apiId.getVersion(), tenantDomain);
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
                apiData = apiGatewayAdminStub.getDefaultApi(apiId.getProviderName(), apiId.getApiName(),
                        apiId.getVersion());
            }
            return apiData;
        } catch (Exception e) {
            throw new AxisFault("Error while obtaining default API information from gateway." + e.getMessage(), e);
        }
    }

    public void deleteDefaultApi(String tenantDomain, APIIdentifier apiId) throws AxisFault {

        try {
            if (tenantDomain != null && !("").equals(tenantDomain) &&
                    !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                apiGatewayAdminStub.deleteDefaultApiForTenant(apiId.getProviderName(), apiId.getApiName(),
                        apiId.getVersion(), tenantDomain);
            } else {
                apiGatewayAdminStub.deleteDefaultApi(apiId.getProviderName(), apiId.getApiName(), apiId.getVersion());
            }

        } catch (Exception e) {
            throw new AxisFault("Error while deleting default API from the gateway. " + e.getMessage(), e);
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

    public boolean deployAPI(GatewayAPIDTO gatewayAPIDTO) throws AxisFault {

        try {
            return apiGatewayAdminStub.deployAPI(convertDto(gatewayAPIDTO));
        } catch (RemoteException e) {
            throw new AxisFault("Error while Deploying API ", e);
        }
    }

    public boolean unDeployAPI(GatewayAPIDTO gatewayAPIDTO) throws AxisFault {

        try {
            return apiGatewayAdminStub.unDeployAPI(convertDto(gatewayAPIDTO));
        } catch (RemoteException e) {
            throw new AxisFault("Error while Deploying API ", e);
        }
    }

    public org.wso2.carbon.apimgt.api.dto.xsd.GatewayAPIDTO convertDto(org.wso2.carbon.apimgt.api.dto.GatewayAPIDTO gatewayAPIDTO) {

        org.wso2.carbon.apimgt.api.dto.xsd.GatewayAPIDTO gatewayAPIDTOStub =
                new org.wso2.carbon.apimgt.api.dto.xsd.GatewayAPIDTO();
        gatewayAPIDTOStub.setName(gatewayAPIDTO.getName());
        gatewayAPIDTOStub.setVersion(gatewayAPIDTO.getVersion());
        gatewayAPIDTOStub.setProvider(gatewayAPIDTO.getProvider());
        gatewayAPIDTOStub.setTenantDomain(gatewayAPIDTO.getTenantDomain());
        gatewayAPIDTOStub.setApiDefinition(gatewayAPIDTO.getApiDefinition());
        gatewayAPIDTOStub.setDefaultAPIDefinition(gatewayAPIDTO.getDefaultAPIDefinition());
        gatewayAPIDTOStub.setOverride(gatewayAPIDTO.isOverride());
        if (gatewayAPIDTO.getClientCertificatesToBeAdd() != null) {
            for (GatewayContentDTO clientCertificate : gatewayAPIDTO.getClientCertificatesToBeAdd()) {
                org.wso2.carbon.apimgt.api.dto.xsd.GatewayContentDTO clientCertDto =
                        new org.wso2.carbon.apimgt.api.dto.xsd.GatewayContentDTO();
                clientCertDto.setName(clientCertificate.getName());
                clientCertDto.setContent(clientCertificate.getContent());
                gatewayAPIDTOStub.addClientCertificatesToBeAdd(clientCertDto);
            }
        }
        if (gatewayAPIDTO.getClientCertificatesToBeRemove() != null) {
            for (String alias : gatewayAPIDTO.getClientCertificatesToBeRemove()) {
                gatewayAPIDTOStub.addClientCertificatesToBeRemove(alias);
            }
        }
        if (gatewayAPIDTO.getEndpointEntriesToBeAdd() != null) {
            for (GatewayContentDTO endpointEntry : gatewayAPIDTO.getEndpointEntriesToBeAdd()) {
                org.wso2.carbon.apimgt.api.dto.xsd.GatewayContentDTO endpointEntryDto =
                        new org.wso2.carbon.apimgt.api.dto.xsd.GatewayContentDTO();
                endpointEntryDto.setName(endpointEntry.getName());
                endpointEntryDto.setContent(endpointEntry.getContent());
                gatewayAPIDTOStub.addEndpointEntriesToBeAdd(endpointEntryDto);
            }
        }
        if (gatewayAPIDTO.getEndpointEntriesToBeRemove() != null) {
            for (String alias : gatewayAPIDTO.getEndpointEntriesToBeRemove()) {
                gatewayAPIDTOStub.addEndpointEntriesToBeRemove(alias);
            }
        }
        if (gatewayAPIDTO.getSequenceToBeAdd() != null) {
            for (GatewayContentDTO sequence : gatewayAPIDTO.getSequenceToBeAdd()) {
                org.wso2.carbon.apimgt.api.dto.xsd.GatewayContentDTO sequenceDto =
                        new org.wso2.carbon.apimgt.api.dto.xsd.GatewayContentDTO();
                sequenceDto.setName(sequence.getName());
                sequenceDto.setContent(sequence.getContent());
                gatewayAPIDTOStub.addSequenceToBeAdd(sequenceDto);
            }
        }
        if (gatewayAPIDTO.getSequencesToBeRemove() != null) {
            for (String alias : gatewayAPIDTO.getSequencesToBeRemove()) {
                gatewayAPIDTOStub.addSequencesToBeRemove(alias);
            }
        }
        if (gatewayAPIDTO.getLocalEntriesToBeAdd() != null) {
            for (GatewayContentDTO localEntry : gatewayAPIDTO.getLocalEntriesToBeAdd()) {
                org.wso2.carbon.apimgt.api.dto.xsd.GatewayContentDTO localEntryDto =
                        new org.wso2.carbon.apimgt.api.dto.xsd.GatewayContentDTO();
                localEntryDto.setName(localEntry.getName());
                localEntryDto.setContent(localEntry.getContent());
                gatewayAPIDTOStub.addLocalEntriesToBeAdd(localEntryDto);
            }
        }
        if (gatewayAPIDTO.getLocalEntriesToBeRemove() != null) {
            for (String alias : gatewayAPIDTO.getLocalEntriesToBeRemove()) {
                gatewayAPIDTOStub.addLocalEntriesToBeRemove(alias);
            }
        }

        if (gatewayAPIDTO.getCredentialsToBeAdd() != null) {
            for (CredentialDto credentialDto : gatewayAPIDTO.getCredentialsToBeAdd()) {
                org.wso2.carbon.apimgt.api.dto.xsd.CredentialDto credential =
                        new org.wso2.carbon.apimgt.api.dto.xsd.CredentialDto();
                credential.setAlias(credentialDto.getAlias());
                credential.setPassword(credentialDto.getPassword());
                gatewayAPIDTOStub.addCredentialsToBeAdd(credential);
            }
        }
        if (gatewayAPIDTO.getCredentialsToBeRemove() != null) {
            for (String alias : gatewayAPIDTO.getCredentialsToBeRemove()) {
                gatewayAPIDTOStub.addCredentialsToBeRemove(alias);
            }
        }

        return gatewayAPIDTOStub;
    }
}

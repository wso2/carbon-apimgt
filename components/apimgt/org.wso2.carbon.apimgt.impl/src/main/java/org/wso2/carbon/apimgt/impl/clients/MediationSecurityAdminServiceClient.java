/*
 * Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.impl.clients;

import org.apache.axis2.AxisFault;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.Environment;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.AbstractAPIGatewayAdminClient;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.mediation.security.stub.MediationSecurityAdminServiceStub;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

/**
 * Mediation Security Admin Service Client to encode the passwords and store
 * into the registry .
 */

public class MediationSecurityAdminServiceClient extends AbstractAPIGatewayAdminClient {

	private MediationSecurityAdminServiceStub mediationSecurityAdminServiceStub;
	
	public MediationSecurityAdminServiceClient(Environment environment) throws AxisFault {
		mediationSecurityAdminServiceStub = new MediationSecurityAdminServiceStub(null, environment.getServerURL() +
		                                                                                  "MediationSecurityAdminService");	
		setup(mediationSecurityAdminServiceStub, environment);	
	}


	/**
	 * Store the encrypted password into the registry with the unique property name.
	 * Property name is constructed as "Provider+ ApiName +Version"
	 * 
	 * @param api
	 * @param tenantDomain
	 * @throws APIManagementException 
	 * 
	 */
	public void addSecureVaultProperty(API api, String tenantDomain) throws APIManagementException  {		

			UserRegistry registry;
            try {
            	String encryptedPassword = doEncryption(api.getEndpointUTPassword());
        		String secureVaultAlias = api.getId().getProviderName() +
                        "--" + api.getId().getApiName() + api.getId().getVersion();
	            registry = getRegistry(tenantDomain);
	            Resource resource = registry.get(APIConstants.API_SYSTEM_CONFIG_SECURE_VAULT_LOCATION);
	            //add the property to the resource then put the resource
	        	resource.addProperty(secureVaultAlias, encryptedPassword);
	        	registry.put(resource.getPath() ,resource);
	        	resource.discard();
	    		
            } catch (Exception e) {
            	String msg = "Failed to get registry secure vault property for the tenant : "+tenantDomain + e.getMessage();
    			throw new APIManagementException(msg, e);
            }			
	}
	
	/**
	 * Store the encrypted password into the registry with the unique property name.
	 * Property name is constructed as "Provider+ ApiName +Version"
	 * 
	 * @param api
	 * @param tenantDomain
	 * @throws APIManagementException
	 */
	public void deleteSecureVaultProperty(API api, String tenantDomain) throws APIManagementException  {
		
		UserRegistry registry;
        try {
        	
    		String secureVaultAlias = api.getId().getProviderName() +
                    "--" + api.getId().getApiName() + api.getId().getVersion();
            registry = getRegistry(tenantDomain);
            Resource resource = registry.get(APIConstants.API_SYSTEM_CONFIG_SECURE_VAULT_LOCATION);
        	resource.removeProperty(secureVaultAlias);
        	registry.put(resource.getPath(), resource);
            resource.discard();    		

		} catch (Exception e) {
			String msg = "Failed to delete the property. " + e.getMessage();
			throw new APIManagementException(msg, e);
		}
	}
	
	/**
	 * Update the encrypted password into the registry with the unique property
	 * name. Property name is constructed as "Provider+ ApiName +Version"
	 * 
	 * @param api
	 * @param tenantDomain
	 * @throws APIManagementException
	 */
	public void updateSecureVaultProperty(API api,String tenantDomain) throws APIManagementException {	
		UserRegistry registry;
		
		try {
			String encryptedPassword = doEncryption(api.getEndpointUTPassword());
			
    		String secureVaultAlias = api.getId().getProviderName() +
                    "--" + api.getId().getApiName() + api.getId().getVersion();
            registry = getRegistry(tenantDomain);
            Resource resource = registry.get(APIConstants.API_SYSTEM_CONFIG_SECURE_VAULT_LOCATION);
            resource.setProperty(secureVaultAlias,encryptedPassword);
            registry.put(resource.getPath(), resource);
            resource.discard();
		} catch (Exception e) {
			String msg = "Failed to update the property. " + e.getMessage();
			throw new APIManagementException(msg, e);
		}
	}
	
	/**
	 * encrypt the plain text password
	 * 
	 * @param cipher
	 *            init cipher
	 * @param plainTextPass
	 *            plain text password
	 * @return encrypted password
	 * @throws APIManagementException
	 */
	private String doEncryption(String plainTextPass) throws APIManagementException {
		
		String encodedValue = null;
		try {
		 encodedValue =	 mediationSecurityAdminServiceStub.doEncrypt(plainTextPass);
//			encodedValue = CryptoUtil.getDefaultCryptoUtil()
//			                         .encryptAndBase64Encode(plainTextPass.getBytes()); //why ESB can not use this?
		} catch (Exception e) {
			String msg = "Failed to encrypt the secured endpoint password, " + e.getMessage();
			throw new APIManagementException(msg, e);
		}
		return encodedValue;
	}

	
	/**
	 * Get the config system registry for tenants
	 * 
	 * @param tenantDomain
	 * @return
	 * @throws APIManagementException
	 */
	private UserRegistry getRegistry(String tenantDomain) throws APIManagementException {
		PrivilegedCarbonContext.startTenantFlow();
		if (tenantDomain != null && !tenantDomain.equals("")) {
			PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain,
			                                                                      true);
		} else {
			PrivilegedCarbonContext.getThreadLocalCarbonContext()
			                       .setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME,
			                                        true);
		}
		
		int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
		UserRegistry registry = null;
		try {
			registry = ServiceReferenceHolder.getInstance().getRegistryService()
			                                 .getConfigSystemRegistry(tenantId);
		} catch (RegistryException e) {
			String msg = "Failed to get registry instance for the tenant : " + tenantDomain +
			                     e.getMessage();
			throw new APIManagementException(msg, e);
		}
		return registry;
	}
}

package org.wso2.carbon.apimgt.impl.utils;

import org.apache.axis2.AxisFault;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.gateway.stub.APIGatewayAdminStub;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.Environment;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.template.APITemplateBuilder;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.apimgt.gateway.dto.stub.APIData;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.apache.axiom.om.OMElement;

public class APIGatewayAdminClient extends AbstractAPIGatewayAdminClient{
	
	private APIGatewayAdminStub apiGatewayAdminStub;
    private String qualifiedName;
    private String qualifiedDefaultApiName;
    private Environment environment;
    

	public APIGatewayAdminClient(APIIdentifier apiId, Environment environment) throws AxisFault {
		this.qualifiedName = apiId.getProviderName() + "--" + apiId.getApiName() + ":v" + apiId.getVersion();
        this.qualifiedDefaultApiName=apiId.getProviderName() + "--" + apiId.getApiName();
        String providerDomain = apiId.getProviderName();
        providerDomain=APIUtil.replaceEmailDomainBack(providerDomain);
        apiGatewayAdminStub = new APIGatewayAdminStub(null, environment.getServerURL() + "APIGatewayAdmin");
        setup(apiGatewayAdminStub, environment);
        this.environment = environment;
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
			String msg = "Failed to get registry instance for the tenant : " + tenantDomain + e.getMessage();
			throw new APIManagementException(msg, e);
		}
		return registry;
	}
	
	 /*** encrypt the plain text password
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
		 encodedValue =	 apiGatewayAdminStub.doEncryption(plainTextPass);

		} catch (Exception e) {
			String msg = "Failed to encrypt the secured endpoint password, " + e.getMessage();
			throw new APIManagementException(msg, e);
		}
		return encodedValue; 
	 }
	
	/**
	 * Add the API to the gateway
	 * @param builder
	 * @param tenantDomain
	 * @throws AxisFault
	 */
	public void addApi(APITemplateBuilder builder, String tenantDomain, APIIdentifier apiId ) throws AxisFault {
        try {

            String apiConfig = builder.getConfigStringForTemplate(environment);
            if (tenantDomain != null && !("").equals(tenantDomain)
                    && !tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            	apiGatewayAdminStub.addApiForTenant(apiId.getProviderName(),apiId.getApiName(), apiId.getVersion(),apiConfig, tenantDomain);
            }else {
            	apiGatewayAdminStub.addApi(apiId.getProviderName(),apiId.getApiName(), apiId.getVersion(),apiConfig);
            }
        } catch (Exception e) {
            throw new AxisFault("Error while publishing API to the Gateway. " + e.getMessage(), e);
        }
	}

    /**
     * Add the API to the gateway
     * @param builder
     * @param tenantDomain
     * @throws AxisFault
     */
    public void addPrototypeApiScriptImpl(APITemplateBuilder builder, String tenantDomain, APIIdentifier apiId) throws AxisFault {
        try {

            String apiConfig = builder.getConfigStringForPrototypeScriptAPI(environment);
            if (tenantDomain != null && !("").equals(tenantDomain)
                    && !tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                apiGatewayAdminStub.addApiForTenant(apiId.getProviderName(),apiId.getApiName(), apiId.getVersion(),apiConfig, tenantDomain);
            }else {
                apiGatewayAdminStub.addApi(apiId.getProviderName(),apiId.getApiName(), apiId.getVersion(),apiConfig);
            }
        } catch (Exception e) {
            throw new AxisFault("Error while publishing prototype API to the Gateway. " + e.getMessage(), e);
        }
    }

    public void addDefaultAPI(APITemplateBuilder builder, String tenantDomain, String defaultVersion, APIIdentifier apiId) throws AxisFault{

        try {
            String apiConfig = builder.getConfigStringForDefaultAPITemplate(defaultVersion);
            if (tenantDomain != null && !("").equals(tenantDomain)
                    && !tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                apiGatewayAdminStub.addApiForTenant(apiId.getProviderName(),apiId.getApiName(), apiId.getVersion(),apiConfig, tenantDomain);
            }else {
                apiGatewayAdminStub.addApi(apiId.getProviderName(),apiId.getApiName(), apiId.getVersion(),apiConfig);
            }
        } catch (Exception e) {
            throw new AxisFault("Error publishing default API to the Gateway. " + e.getMessage(), e);
        }
    }

	/**
	 * Get API from the gateway
	 * @param tenantDomain
	 * @return
	 * @throws AxisFault
	 */
    public APIData getApi(String tenantDomain, APIIdentifier apiId) throws AxisFault {
        try {
        	APIData apiData;
        	 if (tenantDomain != null && !("").equals(tenantDomain)
                     && !tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
        		 apiData = apiGatewayAdminStub.getApiForTenant(apiId.getProviderName(),apiId.getApiName(), apiId.getVersion(),tenantDomain);
             }else {
            	 apiData = apiGatewayAdminStub.getApi(apiId.getProviderName(),apiId.getApiName(), apiId.getVersion());	
             }
            return (APIData) apiData;
        } catch (Exception e) {
            throw new AxisFault("Error while obtaining API information from gateway. " + e.getMessage(), e);
        }
    }

    public APIData getDefaultApi(String tenantDomain, APIIdentifier apiId) throws AxisFault {
        try {
            APIData apiData;
            if (tenantDomain != null && !("").equals(tenantDomain)
                    && !tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                apiData = apiGatewayAdminStub.getApiForTenant(apiId.getProviderName(),apiId.getApiName(), apiId.getVersion(),tenantDomain);
            }else {
                apiData = apiGatewayAdminStub.getApi(apiId.getProviderName(),apiId.getApiName(), apiId.getVersion());
            }
            return (APIData) apiData;
        } catch (Exception e) {
            throw new AxisFault("Error while obtaining default API information from gateway." + e.getMessage(), e);
        }
    }
    /**
     * Update the API in the Gateway
     * @param builder
     * @param tenantDomain
     * @throws AxisFault
     */
	public void updateApi(APITemplateBuilder builder, String tenantDomain, APIIdentifier apiId) throws AxisFault {
		try {
			String apiConfig = builder.getConfigStringForTemplate(environment);
			if (tenantDomain != null && !("").equals(tenantDomain) &&
			    !tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {

				apiGatewayAdminStub.updateApiForTenant(apiId.getProviderName(),apiId.getApiName(), apiId.getVersion(), apiConfig, tenantDomain);
			} else {
				apiGatewayAdminStub.updateApi(apiId.getProviderName(),apiId.getApiName(), apiId.getVersion(), apiConfig);
			}
		} catch (Exception e) {
			throw new AxisFault("Error while updating API in the gateway. " + e.getMessage(), e);
		}
	}

    /**
     * Update the API in the Gateway
     * @param builder
     * @param tenantDomain
     * @throws AxisFault
     */
    public void updateApiForInlineScript(APITemplateBuilder builder, String tenantDomain, APIIdentifier apiId) throws AxisFault {
        try {
            String apiConfig = builder.getConfigStringForPrototypeScriptAPI(environment);
            if (tenantDomain != null && !("").equals(tenantDomain) &&
                    !tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {

                apiGatewayAdminStub.updateApiForTenant(apiId.getProviderName(),apiId.getApiName(), apiId.getVersion(), apiConfig, tenantDomain);
            } else {
                apiGatewayAdminStub.updateApi(apiId.getProviderName(),apiId.getApiName(), apiId.getVersion(), apiConfig);
            }
        } catch (Exception e) {
            throw new AxisFault("Error while updating prototype API in the gateway. " + e.getMessage(), e);
        }
    }

    public void updateDefaultApi(APITemplateBuilder builder, String tenantDomain,String defaultVersion, APIIdentifier apiId) throws AxisFault {
        try {
            String apiConfig = builder.getConfigStringForDefaultAPITemplate(defaultVersion);
            if (tenantDomain != null && !("").equals(tenantDomain) &&
                    !tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {

                apiGatewayAdminStub.updateApiForTenant(apiId.getProviderName(),apiId.getApiName(), apiId.getVersion(), apiConfig, tenantDomain);
            } else {
                apiGatewayAdminStub.updateApi(apiId.getProviderName(),apiId.getApiName(), apiId.getVersion(), apiConfig);
            }
        } catch (Exception e) {
            throw new AxisFault("Error while updating default API in the gateway. " + e.getMessage(), e);
        }
    }
    

	/**
	 * Delete the API from Gateway
	 * @param tenantDomain
	 * @throws AxisFault
	 */
	public void deleteApi(String tenantDomain, APIIdentifier apiId) throws AxisFault {
		try {
			if (tenantDomain != null && !("").equals(tenantDomain) &&
			    !tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
				apiGatewayAdminStub.deleteApiForTenant(apiId.getProviderName(),apiId.getApiName(), apiId.getVersion(), tenantDomain);
			} else {
				apiGatewayAdminStub.deleteApi(apiId.getProviderName(),apiId.getApiName(), apiId.getVersion());
			}
			
		} catch (Exception e) {
			throw new AxisFault("Error while deleting API from the gateway. " + e.getMessage(), e);
		}
	}

    public void deleteDefaultApi(String tenantDomain, APIIdentifier apiId) throws AxisFault {
        try {
            if (tenantDomain != null && !("").equals(tenantDomain) &&
                    !tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                apiGatewayAdminStub.deleteApiForTenant(apiId.getProviderName(),apiId.getApiName(), apiId.getVersion(), tenantDomain);
            } else {
                apiGatewayAdminStub.deleteApi(apiId.getProviderName(),apiId.getApiName(), apiId.getVersion());
            }

        } catch (Exception e) {
            throw new AxisFault("Error while deleting default API from the gateway. "+ e.getMessage(), e);
        }
    }
	
    /**
	 * Deploy the sequence to the gateway
	 * 
	 * @param sequence  - The sequence element , which to be deployed in synapse
	 * @param tenantDomain
	 * @throws AxisFault
	 */
	public void addSequence(OMElement sequence, String tenantDomain) throws AxisFault {
		try {
			if (tenantDomain != null && !("").equals(tenantDomain) &&
			    !tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
				apiGatewayAdminStub.addSequenceForTenant(sequence, tenantDomain);
			} else {
				apiGatewayAdminStub.addSequence(sequence);
			}

		} catch (Exception e) {
			throw new AxisFault("Error while adding new sequence", e);
		}
	}

	/**
	 * Undeploy the sequence from gateway
	 * @param sequenceName -The sequence name, which need to be undeployed from synapse configuration
	 * @param tenantDomain
	 * @throws AxisFault
	 */
	public void deleteSequence(String sequenceName, String tenantDomain) throws AxisFault {
		try {
			if (tenantDomain != null && !("").equals(tenantDomain) &&
			    !tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
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
	 * @param sequenceName
	 *            -The sequence name,
	 * @param tenantDomain
	 * @throws AxisFault
	 */
	public OMElement getSequence(String sequenceName, String tenantDomain) throws AxisFault {
		try {
			if (tenantDomain != null && !("").equals(tenantDomain) &&
			    !tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
				return (OMElement) apiGatewayAdminStub.getSequenceForTenant(sequenceName, tenantDomain);
			} else {
				return  (OMElement) apiGatewayAdminStub.getSequence(sequenceName);
			}
			
		} catch (Exception e) {
			throw new AxisFault("Error while retriving the sequence", e);
		}
	}

    public boolean isExistingSequence(String sequenceName, String tenantDomain) throws AxisFault{
        try{
            if (tenantDomain != null && !("").equals(tenantDomain) &&
                    !tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)){
                return apiGatewayAdminStub.isExistingSequenceForTenant(sequenceName, tenantDomain);
            }
            else{
                return apiGatewayAdminStub.isExistingSequence(sequenceName);
            }
        }catch (Exception e){
            throw new AxisFault("Error while checking for existence of sequence : " + sequenceName +
                    " in tenant " + tenantDomain, e);
        }
    }

}

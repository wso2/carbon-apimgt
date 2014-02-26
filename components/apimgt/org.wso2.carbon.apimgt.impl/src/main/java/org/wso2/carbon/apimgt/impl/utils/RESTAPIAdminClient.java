/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.impl.utils;

import org.apache.axis2.AxisFault;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.impl.dto.Environment;
import org.wso2.carbon.apimgt.impl.template.APITemplateBuilder;
import org.wso2.carbon.rest.api.stub.RestApiAdminStub;
import org.wso2.carbon.rest.api.stub.types.carbon.APIData;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

public class RESTAPIAdminClient extends AbstractAPIGatewayAdminClient {

    private RestApiAdminStub restApiAdminStub;
    private String qualifiedName;
    private Environment environment;
    
    public RESTAPIAdminClient(APIIdentifier apiId, Environment environment) throws AxisFault {
        this.qualifiedName = apiId.getProviderName() + "--" + apiId.getApiName() + ":v" + apiId.getVersion();
        String providerDomain = apiId.getProviderName();
        providerDomain=providerDomain.replace("-AT-", "@");
        restApiAdminStub = new RestApiAdminStub(null, environment.getServerURL() + "RestApiAdmin");
        setup(restApiAdminStub, environment);
        this.environment = environment;
    }
    
	/**
	 * Add the API to the gateway
	 * @param builder
	 * @param tenantDomain
	 * @throws AxisFault
	 */
	public void addApi(APITemplateBuilder builder, String tenantDomain ) throws AxisFault {
        try {
            String apiConfig = builder.getConfigStringForTemplate(environment);
            if (tenantDomain != null && !("").equals(tenantDomain)
                    && !tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            	restApiAdminStub.addApiForTenant(apiConfig, tenantDomain);
            }else {
            	 restApiAdminStub.addApiFromString(apiConfig);	
            }
        } catch (Exception e) {
            throw new AxisFault("Error while adding new API", e);
        }
	}

	/**
	 * Get API from the gateway
	 * @param tenantDomain
	 * @return
	 * @throws AxisFault
	 */
    public APIData getApi(String tenantDomain) throws AxisFault {
        try {
        	APIData apiData;
        	 if (tenantDomain != null && !("").equals(tenantDomain)
                     && !tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
        		 apiData = restApiAdminStub.getApiForTenant(qualifiedName,tenantDomain);
             }else {
            	 apiData = restApiAdminStub.getApiByName(qualifiedName);	
             }
            return (APIData) apiData;
        } catch (Exception e) {
            throw new AxisFault("Error while obtaining API information from gateway", e);
        }
    }

    /**
     * Update the API in the Gateway
     * @param builder
     * @param tenantDomain
     * @throws AxisFault
     */
	public void updateApi(APITemplateBuilder builder, String tenantDomain) throws AxisFault {
		try {
			String apiConfig = builder.getConfigStringForTemplate(environment);
			if (tenantDomain != null && !("").equals(tenantDomain) &&
			    !tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {

				restApiAdminStub.updateApiForTenant(qualifiedName, apiConfig, tenantDomain);
			} else {
				restApiAdminStub.updateApiFromString(qualifiedName, apiConfig);
			}
		} catch (Exception e) {
			throw new AxisFault("Error while updating API", e);
		}
	}

	/**
	 * Delete the API from Gateway
	 * @param tenantDomain
	 * @throws AxisFault
	 */
	public void deleteApi(String tenantDomain) throws AxisFault {
		try {
			if (tenantDomain != null && !("").equals(tenantDomain) &&
			    !tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
				restApiAdminStub.deleteApiForTenant(qualifiedName, tenantDomain);
			} else {
				restApiAdminStub.deleteApi(qualifiedName);
			}
			
		} catch (Exception e) {
			throw new AxisFault("Error while deleting API", e);
		}
	}
}

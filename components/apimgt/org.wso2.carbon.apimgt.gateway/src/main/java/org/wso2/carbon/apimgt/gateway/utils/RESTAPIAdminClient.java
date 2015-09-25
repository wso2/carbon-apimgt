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

package org.wso2.carbon.apimgt.gateway.utils;

import org.apache.axis2.AxisFault;
import org.wso2.carbon.rest.api.stub.RestApiAdminStub;
import org.wso2.carbon.rest.api.stub.types.carbon.APIData;

public class RESTAPIAdminClient {

    private RestApiAdminStub restApiAdminStub;
    private String qualifiedName;
    private String qualifiedDefaultApiName;

    
    static final String backendURLl = "local:///services/";
    
    public RESTAPIAdminClient(String apiProviderName, String apiName, String version) throws AxisFault {
        this.qualifiedName = apiProviderName + "--" + apiName + ":v" + version;
        this.qualifiedDefaultApiName=apiProviderName + "--" + apiName;
        restApiAdminStub = new RestApiAdminStub(null, backendURLl + "RestApiAdmin");

    }
    
	/**
	 * Add the API to the gateway
	 * @param builder
	 * @param tenantDomain
	 * @throws AxisFault
	 */
	public void addApi(String apiConfig, String tenantDomain ) throws AxisFault {
        try {
        	restApiAdminStub.addApiForTenant(apiConfig, tenantDomain);
        } catch (Exception e) {
            throw new AxisFault("Error while publishing API to the Gateway. " + e.getMessage(), e);
        }
	}
	
	public void addApi(String apiConfig ) throws AxisFault {
        try {
        	restApiAdminStub.addApiFromString(apiConfig);
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
    public void addPrototypeApiScriptImpl(String apiConfig, String tenantDomain) throws AxisFault {
        try {

        	 restApiAdminStub.addApiForTenant(apiConfig, tenantDomain);
        } catch (Exception e) {
            throw new AxisFault("Error while publishing prototype API to the Gateway. " + e.getMessage(), e);
        }
    }
    
    public void addPrototypeApiScriptImpl(String apiConfig) throws AxisFault {
        try {

        	restApiAdminStub.addApiFromString(apiConfig);
        } catch (Exception e) {
            throw new AxisFault("Error while publishing prototype API to the Gateway. " + e.getMessage(), e);
        }
    }

    public void addDefaultAPI(String apiConfig, String tenantDomain) throws AxisFault{

        try {
        	restApiAdminStub.addApiForTenant(apiConfig, tenantDomain);
        } catch (Exception e) {
            throw new AxisFault("Error publishing default API to the Gateway. " + e.getMessage(), e);
        }
    }
    
    public void addDefaultAPI(String apiConfig) throws AxisFault{

        try {
        	restApiAdminStub.addApiFromString(apiConfig);
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
    public APIData getApi(String tenantDomain) throws AxisFault {
        try {
        	APIData apiData = restApiAdminStub.getApiForTenant(qualifiedName,tenantDomain);
            return (APIData) apiData;
        } catch (Exception e) {
            throw new AxisFault("Error while obtaining API information from gateway. " + e.getMessage(), e);
        }
    }
    
    public APIData getApi() throws AxisFault {
        try {
        	APIData apiData = restApiAdminStub.getApiByName(qualifiedName);
            return (APIData) apiData;
        } catch (Exception e) {
            throw new AxisFault("Error while obtaining API information from gateway. " + e.getMessage(), e);
        }
    }

    public APIData getDefaultApi(String tenantDomain) throws AxisFault {
        try {
            APIData apiData = restApiAdminStub.getApiForTenant(qualifiedDefaultApiName,tenantDomain);
            return (APIData) apiData;
        } catch (Exception e) {
            throw new AxisFault("Error while obtaining default API information from gateway." + e.getMessage(), e);
        }
    }
    
    public APIData getDefaultApi() throws AxisFault {
        try {
            APIData apiData = restApiAdminStub.getApiByName(qualifiedDefaultApiName);
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
	public void updateApi(String apiConfig, String tenantDomain) throws AxisFault {
		try {
			restApiAdminStub.updateApiForTenant(qualifiedName, apiConfig, tenantDomain);
		} catch (Exception e) {
			throw new AxisFault("Error while updating API in the gateway. " + e.getMessage(), e);
		}
	}
	
	public void updateApi(String apiConfig) throws AxisFault {
		try {
			restApiAdminStub.updateApiFromString(qualifiedName, apiConfig);
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
    public void updateApiForInlineScript(String apiConfig, String tenantDomain) throws AxisFault {
        try {
        	restApiAdminStub.updateApiForTenant(qualifiedName, apiConfig, tenantDomain);
        } catch (Exception e) {
            throw new AxisFault("Error while updating prototype API in the gateway. " + e.getMessage(), e);
        }
    }
    
    public void updateApiForInlineScript(String apiConfig) throws AxisFault {
        try {
        	restApiAdminStub.updateApiFromString(qualifiedName, apiConfig);
        } catch (Exception e) {
            throw new AxisFault("Error while updating prototype API in the gateway. " + e.getMessage(), e);
        }
    }

    public void updateDefaultApi(String apiConfig, String tenantDomain) throws AxisFault {
        try {
        	restApiAdminStub.updateApiForTenant(qualifiedDefaultApiName, apiConfig, tenantDomain);
        } catch (Exception e) {
            throw new AxisFault("Error while updating default API in the gateway. " + e.getMessage(), e);
        }
    }
    
    public void updateDefaultApi(String apiConfig) throws AxisFault {
        try {
        	restApiAdminStub.updateApiFromString(qualifiedDefaultApiName, apiConfig);
        } catch (Exception e) {
            throw new AxisFault("Error while updating default API in the gateway. " + e.getMessage(), e);
        }
    }
    

	/**
	 * Delete the API from Gateway
	 * @param tenantDomain
	 * @throws AxisFault
	 */
	public void deleteApi(String tenantDomain) throws AxisFault {
		try {
			restApiAdminStub.deleteApiForTenant(qualifiedName, tenantDomain);
			
		} catch (Exception e) {
			throw new AxisFault("Error while deleting API from the gateway. " + e.getMessage(), e);
		}
	}

	public void deleteApi() throws AxisFault {
		try {
			restApiAdminStub.deleteApi(qualifiedName);
			
		} catch (Exception e) {
			throw new AxisFault("Error while deleting API from the gateway. " + e.getMessage(), e);
		}
	}

	
    public void deleteDefaultApi(String tenantDomain) throws AxisFault {
        try {
        	restApiAdminStub.deleteApiForTenant(qualifiedDefaultApiName, tenantDomain);

        } catch (Exception e) {
            throw new AxisFault("Error while deleting default API from the gateway. "+ e.getMessage(), e);
        }
    }
    
    public void deleteDefaultApi() throws AxisFault {
        try {
        	restApiAdminStub.deleteApi(qualifiedDefaultApiName);
        } catch (Exception e) {
            throw new AxisFault("Error while deleting default API from the gateway. "+ e.getMessage(), e);
        }
    }

}

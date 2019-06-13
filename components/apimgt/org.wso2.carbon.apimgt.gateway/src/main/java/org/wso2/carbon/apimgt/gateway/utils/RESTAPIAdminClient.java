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

    static final String backendURLl = "local:///services/";

    public RESTAPIAdminClient() throws AxisFault {
        restApiAdminStub = new RestApiAdminStub(null, backendURLl + "RestApiAdmin");

    }

    /**
     * Add the API to the gateway
     *
     * @param tenantDomain
     * @throws AxisFault
     */
    public boolean addApi(String apiConfig, String tenantDomain) throws AxisFault {
        try {
            return restApiAdminStub.addApiForTenant(apiConfig, tenantDomain);
        } catch (Exception e) {
            throw new AxisFault("Error while publishing API to the Gateway. " + e.getMessage(), e);
        }
    }

    public boolean addApi(String apiConfig) throws AxisFault {
        try {
            return restApiAdminStub.addApiFromString(apiConfig);
        } catch (Exception e) {
            throw new AxisFault("Error while publishing API to the Gateway. " + e.getMessage(), e);
        }
    }

    /**
     * Get API from the gateway
     *
     * @param tenantDomain
     * @return
     * @throws AxisFault
     */
    public APIData getApi(String apiName, String tenantDomain) throws AxisFault {
        try {
            return restApiAdminStub.getApiForTenant(apiName, tenantDomain);
        } catch (Exception e) {
            throw new AxisFault("Error while obtaining API information from gateway. " + e.getMessage(), e);
        }
    }

    public APIData getApi(String apiName) throws AxisFault {
        try {
            return restApiAdminStub.getApiByName(apiName);

        } catch (Exception e) {
            throw new AxisFault("Error while obtaining API information from gateway. " + e.getMessage(), e);
        }
    }

    /**
     * Update the API in the Gateway
     *
     * @param tenantDomain
     * @throws AxisFault
     */
    public boolean updateApi(String apiName, String apiConfig, String tenantDomain) throws AxisFault {
        try {
            return restApiAdminStub.updateApiForTenant(apiName, apiConfig, tenantDomain);
        } catch (Exception e) {
            throw new AxisFault("Error while updating API in the gateway. " + e.getMessage(), e);
        }
    }

    public boolean updateApi(String apiName, String apiConfig) throws AxisFault {
        try {
            return restApiAdminStub.updateApiFromString(apiName, apiConfig);
        } catch (Exception e) {
            throw new AxisFault("Error while updating API in the gateway. " + e.getMessage(), e);
        }
    }

    /**
     * Delete the API from Gateway
     *
     * @param tenantDomain
     * @throws AxisFault
     */
    public boolean deleteApi(String apiName, String tenantDomain) throws AxisFault {
        try {
            return restApiAdminStub.deleteApiForTenant(apiName, tenantDomain);
        } catch (Exception e) {
            throw new AxisFault("Error while deleting API from the gateway. " + e.getMessage(), e);
        }
    }

    public boolean deleteApi(String apiName) throws AxisFault {
        try {
            return restApiAdminStub.deleteApi(apiName);
        } catch (Exception e) {
            throw new AxisFault("Error while deleting API from the gateway. " + e.getMessage(), e);
        }
    }
}

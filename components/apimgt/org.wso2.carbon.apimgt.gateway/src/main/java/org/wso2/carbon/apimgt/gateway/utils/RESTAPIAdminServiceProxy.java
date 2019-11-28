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
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.rest.api.APIData;
import org.wso2.carbon.rest.api.service.RestApiAdmin;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

public class RESTAPIAdminServiceProxy {

    private RestApiAdmin restApiAdmin;
    private String tenantDomain;

    public RESTAPIAdminServiceProxy(String tenantDomain) {

        restApiAdmin = ServiceReferenceHolder.getInstance().getRestAPIAdmin();
        this.tenantDomain = tenantDomain;
    }

    public RESTAPIAdminServiceProxy() {
        restApiAdmin = ServiceReferenceHolder.getInstance().getRestAPIAdmin();
        this.tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;

    }

    public boolean addApi(String apiConfig) throws AxisFault {

        try {
            if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                return restApiAdmin.addApiFromString(apiConfig);
            } else {
                return restApiAdmin.addApiForTenant(apiConfig, tenantDomain);
            }
        } catch (Exception e) {
            throw new AxisFault("Error while publishing API to the Gateway. " + e.getMessage(), e);
        }
    }

    public APIData getApi(String apiName) throws AxisFault {

        try {
            if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                return restApiAdmin.getApiByName(apiName);
            } else {
                return restApiAdmin.getApiForTenant(apiName, tenantDomain);

            }

        } catch (Exception e) {
            throw new AxisFault("Error while obtaining API information from gateway. " + e.getMessage(), e);
        }
    }

    public boolean updateApi(String apiName, String apiConfig) throws AxisFault {

        try {
            if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                return restApiAdmin.updateApiFromString(apiName, apiConfig);
            } else {
                return restApiAdmin.updateApiForTenant(apiName, apiConfig, tenantDomain);
            }
        } catch (Exception e) {
            throw new AxisFault("Error while updating API in the gateway. " + e.getMessage(), e);
        }
    }

    public boolean deleteApi(String apiName) throws AxisFault {

        try {
            if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                return restApiAdmin.deleteApi(apiName);
            } else {
                return restApiAdmin.deleteApiForTenant(apiName, tenantDomain);
            }
        } catch (Exception e) {
            throw new AxisFault("Error while deleting API from the gateway. " + e.getMessage(), e);
        }
    }

    public void setTenantDomain(String tenantDomain) {

        this.tenantDomain = tenantDomain;
    }
}

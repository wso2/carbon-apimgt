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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.gateway.stub.APIGatewayAdminStub;

public class APIGatewayAdminClient extends AbstractAPIGatewayAdminClient {

    private APIGatewayAdminStub apiGatewayAdminStub;
    private static Log log = LogFactory.getLog(APIGatewayAdminClient.class);

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

}

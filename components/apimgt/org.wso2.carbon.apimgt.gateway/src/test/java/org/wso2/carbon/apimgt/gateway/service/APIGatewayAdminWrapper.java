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
package org.wso2.carbon.apimgt.gateway.service;


import org.apache.axis2.AxisFault;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.gateway.utils.EndpointAdminServiceProxy;
import org.wso2.carbon.apimgt.gateway.utils.MediationSecurityAdminServiceProxy;
import org.wso2.carbon.apimgt.gateway.utils.RESTAPIAdminServiceProxy;
import org.wso2.carbon.apimgt.gateway.utils.SequenceAdminServiceProxy;

public class APIGatewayAdminWrapper extends APIGatewayAdmin {
    private RESTAPIAdminServiceProxy restapiAdminServiceProxy;
    private EndpointAdminServiceProxy endpointAdminServiceProxy;
    private SequenceAdminServiceProxy sequenceAdminServiceProxy;

    public APIGatewayAdminWrapper(RESTAPIAdminServiceProxy restapiAdminServiceProxy, EndpointAdminServiceProxy
            endpointAdminServiceProxy, SequenceAdminServiceProxy sequenceAdminServiceProxy) {

        this.restapiAdminServiceProxy = restapiAdminServiceProxy;
        this.endpointAdminServiceProxy = endpointAdminServiceProxy;
        this.sequenceAdminServiceProxy = sequenceAdminServiceProxy;
    }

    public APIGatewayAdminWrapper(RESTAPIAdminServiceProxy restapiAdminServiceProxy, EndpointAdminServiceProxy
            endpointAdminServiceProxy, SequenceAdminServiceProxy sequenceAdminServiceProxy,
                                  MediationSecurityAdminServiceProxy mediationSecurityAdminServiceProxy) {
        this.restapiAdminServiceProxy = restapiAdminServiceProxy;
        this.sequenceAdminServiceProxy = sequenceAdminServiceProxy;
        this.endpointAdminServiceProxy = endpointAdminServiceProxy;
    }

    @Override
    protected RESTAPIAdminServiceProxy getRestapiAdminClient(String tenantDomain) throws
            AxisFault {
        return restapiAdminServiceProxy;
    }

    @Override
    protected EndpointAdminServiceProxy getEndpointAdminServiceClient(String tenantDomain) throws AxisFault {
        return endpointAdminServiceProxy;
    }

    @Override
    protected SequenceAdminServiceProxy getSequenceAdminServiceClient(String tenantDomain) throws AxisFault {
        return sequenceAdminServiceProxy;
    }

    @Override
    protected void deleteRegistryProperty(String apiProviderName, String apiName, String version, String
            tenantDomain) throws AxisFault {
    }

    @Override
    protected void setRegistryProperty(String tenantDomain, String secureVaultAlias, String encodedValue) throws
            APIManagementException {
    }

}

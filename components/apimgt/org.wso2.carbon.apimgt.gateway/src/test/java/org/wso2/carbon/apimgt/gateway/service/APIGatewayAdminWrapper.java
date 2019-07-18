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
import org.wso2.carbon.apimgt.gateway.utils.EndpointAdminServiceClient;
import org.wso2.carbon.apimgt.gateway.utils.MediationSecurityAdminServiceClient;
import org.wso2.carbon.apimgt.gateway.utils.RESTAPIAdminClient;
import org.wso2.carbon.apimgt.gateway.utils.SequenceAdminServiceClient;

public class APIGatewayAdminWrapper extends APIGatewayAdmin {
    private RESTAPIAdminClient restapiAdminClient;
    private EndpointAdminServiceClient endpointAdminServiceClient;
    private SequenceAdminServiceClient sequenceAdminServiceClient;
    private MediationSecurityAdminServiceClient mediationSecurityAdminServiceClient;
    public APIGatewayAdminWrapper(RESTAPIAdminClient restapiAdminClient, EndpointAdminServiceClient
            endpointAdminServiceClient, SequenceAdminServiceClient sequenceAdminServiceClient) {

        this.restapiAdminClient = restapiAdminClient;
        this.endpointAdminServiceClient = endpointAdminServiceClient;
        this.sequenceAdminServiceClient = sequenceAdminServiceClient;
    }

    public APIGatewayAdminWrapper(RESTAPIAdminClient restapiAdminClient, EndpointAdminServiceClient
            endpointAdminServiceClient, SequenceAdminServiceClient sequenceAdminServiceClient,
                                  MediationSecurityAdminServiceClient mediationSecurityAdminServiceClient) {
        this.restapiAdminClient = restapiAdminClient;
        this.sequenceAdminServiceClient = sequenceAdminServiceClient;
        this.endpointAdminServiceClient = endpointAdminServiceClient;
        this.mediationSecurityAdminServiceClient = mediationSecurityAdminServiceClient;
    }

    @Override
    protected RESTAPIAdminClient getRestapiAdminClient() throws
            AxisFault {
        return restapiAdminClient;
    }

    @Override
    protected EndpointAdminServiceClient getEndpointAdminServiceClient() throws AxisFault {
        return endpointAdminServiceClient;
    }

    @Override
    protected SequenceAdminServiceClient getSequenceAdminServiceClient() throws AxisFault {
        return sequenceAdminServiceClient;
    }

    @Override
    protected void deleteRegistryProperty(String apiProviderName, String apiName, String version, String
            tenantDomain) throws APIManagementException {
    }

    @Override
    protected void setRegistryProperty(String tenantDomain, String secureVaultAlias, String encodedValue) throws
            APIManagementException {
    }

    @Override
    protected MediationSecurityAdminServiceClient getMediationSecurityAdminServiceClient() throws AxisFault {
        return mediationSecurityAdminServiceClient;
    }
}

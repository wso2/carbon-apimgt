/*
* Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.wso2.carbon.apimgt.impl.throttling;

import org.apache.axis2.AxisFault;
import org.wso2.carbon.apimgt.impl.dto.ThrottleProperties;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.event.processor.stub.EventProcessorAdminServiceStub;

import java.net.MalformedURLException;

public class GlobalThrottleEngineClientWrapper extends GlobalThrottleEngineClient {

    private AuthenticationAdminStub authenticationAdminStub;
    private EventProcessorAdminServiceStub eventProcessorAdminServiceStub;
    private ThrottleProperties.PolicyDeployer policyDeployer;

    public GlobalThrottleEngineClientWrapper(AuthenticationAdminStub authenticationAdminStub,
            EventProcessorAdminServiceStub eventProcessorAdminServiceStub,
            ThrottleProperties.PolicyDeployer policyDeployer) {
        this.authenticationAdminStub = authenticationAdminStub;
        this.eventProcessorAdminServiceStub = eventProcessorAdminServiceStub;
        this.policyDeployer = policyDeployer;
    }

    @Override
    protected AuthenticationAdminStub getAuthenticationAdminStub() {
        return authenticationAdminStub;
    }

    @Override
    protected ThrottleProperties.PolicyDeployer getPolicyDeployer() {
        return policyDeployer;
    }

    @Override
    protected EventProcessorAdminServiceStub getEventProcessorAdminServiceStub() throws AxisFault {
        return eventProcessorAdminServiceStub;
    }
}

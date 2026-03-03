/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.internal.service.interceptors;

import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.wso2.carbon.context.PrivilegedCarbonContext;

/**
 * Runs after the JAX-RS resource (POST_INVOKE). When the request was authenticated
 * via platform gateway api-key, {@link PlatformGatewayApiKeyAuthInterceptor} sets
 * a message property. This interceptor calls {@link PrivilegedCarbonContext#endTenantFlow()}
 * so the tenant flow started there is properly closed and the thread-local is cleared.
 */
public class PlatformGatewayTenantFlowCleanupInterceptor extends AbstractPhaseInterceptor<Message> {

    public PlatformGatewayTenantFlowCleanupInterceptor() {
        super(Phase.POST_INVOKE);
    }

    @Override
    public void handleMessage(Message message) {
        if (!Boolean.TRUE.equals(message.get(PlatformGatewayApiKeyAuthInterceptor.MESSAGE_PROPERTY_TENANT_FLOW_STARTED))) {
            return;
        }
        try {
            PrivilegedCarbonContext.endTenantFlow();
        } finally {
            message.remove(PlatformGatewayApiKeyAuthInterceptor.MESSAGE_PROPERTY_TENANT_FLOW_STARTED);
        }
    }
}

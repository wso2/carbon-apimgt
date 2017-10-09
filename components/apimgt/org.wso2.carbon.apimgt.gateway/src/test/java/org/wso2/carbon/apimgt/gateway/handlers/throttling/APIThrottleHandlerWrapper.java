/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.gateway.handlers.throttling;

import org.apache.axis2.clustering.state.Replicator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.throttle.core.AccessRateController;
import org.apache.synapse.commons.throttle.core.RoleBasedAccessRateController;
import org.apache.synapse.commons.throttle.core.ThrottleConfiguration;
import org.apache.synapse.commons.throttle.core.ThrottleContext;
import org.apache.synapse.commons.throttle.core.ThrottleDataHolder;
import org.apache.synapse.commons.throttle.core.ThrottleException;
import org.wso2.carbon.metrics.manager.Timer;

public class APIThrottleHandlerWrapper extends APIThrottleHandler {
    private Timer timer;
    private ThrottleContext throttleContext;

    public APIThrottleHandlerWrapper(Timer timer, ThrottleContext throttleContext) {

        this.timer = timer;
        this.throttleContext = throttleContext;
    }

    @Override
    protected Timer getTimer() {
        return timer;
    }

    @Override
    protected boolean isClusteringEnabled() {
        return true;
    }


    @Override
    protected int resolveTenantId() {
        return -1234;
    }

    @Override
    protected void setFaultPayload(MessageContext messageContext, String errorMessage, String
            errorDescription) {
    }

    @Override
    protected void sendFault(MessageContext messageContext, int httpErrorCode) {
    }

    @Override
    protected ThrottleContext createThrottleContext(ThrottleConfiguration throttleConfiguration) throws ThrottleException {
        return throttleContext;
    }

    @Override
    protected ThrottleContext getApplicationThrottleContext(MessageContext synCtx, ThrottleDataHolder dataHolder, String applicationId) {
        return throttleContext;
    }

}

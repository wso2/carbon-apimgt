/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *
 */

package org.wso2.carbon.apimgt.gateway.handlers.throttling;

import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.throttle.core.AccessInformation;
import org.apache.synapse.commons.throttle.core.ThrottleContext;
import org.apache.synapse.commons.throttle.core.ThrottleException;
import org.apache.synapse.core.SynapseEnvironment;
import org.wso2.carbon.apimgt.gateway.throttling.ThrottleDataHolder;
import org.wso2.carbon.metrics.manager.Timer;

public class ThrottlingHandlerWrapper extends ThrottleHandler {

    private Timer timer;
    private ThrottleDataHolder throttleDataHolder;
    private ThrottleConditionEvaluator throttleConditionEvaluator;
    private AccessInformation accessInformation;

    public ThrottlingHandlerWrapper(Timer timer, ThrottleDataHolder throttleDataHolder, ThrottleConditionEvaluator
            throttleConditionEvaluator) {
        this.timer = timer;
        this.throttleDataHolder = throttleDataHolder;
        this.throttleConditionEvaluator = throttleConditionEvaluator;
    }

    public ThrottlingHandlerWrapper(Timer timer, ThrottleDataHolder throttleDataHolder, ThrottleConditionEvaluator throttleConditionEvaluator, AccessInformation accessInformation) {
        this.timer = timer;
        this.throttleDataHolder = throttleDataHolder;
        this.throttleConditionEvaluator = throttleConditionEvaluator;
        this.accessInformation = accessInformation;
    }

    @Override
    protected Timer getTimer(String name) {
        return timer;
    }

    @Override
    protected String getTenantDomain() {
        return "carbon.super";
    }

    @Override
    protected ThrottleDataHolder getThrottleDataHolder() {
        return throttleDataHolder;
    }

    @Override
    protected void setSOAPFault(MessageContext messageContext, String errorMessage, String errorDescription) {
        //Do Nothing
    }

    @Override
    protected void sendFault(MessageContext messageContext, int httpErrorCode) {
        //Do Nothing
    }

    @Override
    protected ThrottleConditionEvaluator getThrottleConditionEvaluator() {
        return throttleConditionEvaluator;
    }

    @Override
    protected boolean isClusteringEnabled() {
        return true;
    }

    @Override
    protected AccessInformation getAccessInformation(ThrottleContext hardThrottleContext, String throttleKey, String productionHardLimit) throws ThrottleException {
        return accessInformation;
    }
}

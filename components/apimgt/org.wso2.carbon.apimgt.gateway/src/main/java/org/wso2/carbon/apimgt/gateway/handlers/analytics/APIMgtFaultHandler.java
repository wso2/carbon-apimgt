/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.gateway.handlers.analytics;

import org.apache.synapse.MessageContext;
import org.wso2.carbon.apimgt.gateway.mediators.APIMgtCommonExecutionPublisher;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

public class APIMgtFaultHandler extends APIMgtCommonExecutionPublisher {

    public APIMgtFaultHandler() {
        super();
    }

    public boolean mediate(MessageContext messageContext) {
        //Skipping analytics
        return true; // Should never stop the message flow
    }

    protected String getTenantDomainFromRequestURL(String fullRequestPath) {
        return MultitenantUtils.getTenantDomainFromRequestURL(fullRequestPath);
    }

    protected void initDataPublisher() {
        this.initializeDataPublisher();
    }

    public boolean isContentAware() {
        return false;
    }
}

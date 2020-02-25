/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */


package org.wso2.carbon.apimgt.gateway.messageTrace;

import org.apache.synapse.aspects.flow.statistics.opentracing.management.OpenTracingManager;
import org.wso2.carbon.apimgt.gateway.messageTrace.APIMSpanHandler;

import org.apache.synapse.aspects.flow.statistics.opentracing.management.scoping.TracingScopeManager;


/**
 * Coordinates the APIM span handler with the tracer.
 */

public class APIMTracingManager implements OpenTracingManager{

    private APIMSpanHandler handler;

    public APIMTracingManager() {
        resolveHandler();
    }

    @Override
    public void resolveHandler() {
        this.handler = new APIMSpanHandler(new TracingScopeManager());
    }

    @Override
    public APIMSpanHandler getHandler() {
        return this.handler;
    }
}

/*
 * Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.gateway.handlers.ext.payloadhandler;

import org.apache.synapse.MessageContext;

/**
 * This is a singleton class acting as the factory class to build SynapsePayloadHandler objects for each message
 * context.
 */
public class SynapsePayloadHandlerFactory {

    private static final SynapsePayloadHandlerFactory instance = new SynapsePayloadHandlerFactory();

    public static SynapsePayloadHandlerFactory getInstance() {

        return instance;
    }

    private SynapsePayloadHandlerFactory() {

    }

    /**
     * Build synapse payload handler object.
     *
     * @param messageContext synapse message context
     * @return SynapsePayloadHandler
     */
    public SynapsePayloadHandler buildPayloadHandler(MessageContext messageContext) {

        return new SynapsePayloadHandler(messageContext);
    }

}


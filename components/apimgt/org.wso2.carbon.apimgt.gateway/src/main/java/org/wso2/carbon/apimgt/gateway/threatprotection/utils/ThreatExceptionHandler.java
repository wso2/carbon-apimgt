/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.gateway.threatprotection.utils;

import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.core.axis2.Axis2MessageContext;

/**
 * This class handles validator exceptions.
 */
public class ThreatExceptionHandler {

    /**
     * The method sets message context properties for error message.
     *
     * @param messageContext This message context contains the request message properties of the relevant API which was
     *                       enabled a Validator message mediation in flow.
     * @param errorMessage   specific error message for each validator.
     */
    public static void handleException(MessageContext messageContext, String errorMessage) {
        messageContext.setProperty(ThreatProtectorConstants.STATUS, true);
        messageContext.setProperty(ThreatProtectorConstants.ERROR_CODE,
                ThreatProtectorConstants.HTTP_HEADER_THREAT_CODE);
        messageContext.setProperty(ThreatProtectorConstants.ERROR_MESSAGE, errorMessage);
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().setProperty(
                ThreatProtectorConstants.HTTP_SC, ThreatProtectorConstants.HTTP_SC_CODE);
        throw new SynapseException(errorMessage);
    }
}

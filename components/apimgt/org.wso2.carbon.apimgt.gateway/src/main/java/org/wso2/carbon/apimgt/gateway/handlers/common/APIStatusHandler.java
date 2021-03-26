/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.gateway.handlers.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.api.ApiConstants;
import org.apache.synapse.core.axis2.Axis2Sender;
import org.apache.synapse.rest.AbstractHandler;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.keymgt.model.entity.API;

/**
 * This handler used to handle API Status.
 */
public class APIStatusHandler extends AbstractHandler {

    private static final Log log = LogFactory.getLog(APIStatusHandler.class);

    @Override
    public boolean handleRequest(MessageContext messageContext) {

        API api = GatewayUtils.getAPI(messageContext);
        if (api != null) {
            if (log.isDebugEnabled()) {
                log.debug("set the api.ut.status as " + api.getStatus());
            }
            messageContext.setProperty(APIMgtGatewayConstants.API_STATUS, api.getStatus());
            if (APIConstants.BLOCKED.equals(api.getStatus())) {
                handleBlockedAPIStatus(messageContext);
                return false;
            } else if (APIConstants.PROTOTYPED.equals(api.getStatus())) {
                messageContext.setProperty(APIConstants.API_KEY_TYPE, APIConstants.API_KEY_TYPE_PRODUCTION);
            }
        }
        return true;
    }

    @Override
    public boolean handleResponse(MessageContext messageContext) {

        return true;
    }

    private void handleBlockedAPIStatus(MessageContext messageContext) {
        if (log.isDebugEnabled()) {
            log.debug("Mediate from " + APISecurityConstants.API_BLOCKED_SEQUENCE);
        }

        Mediator sequence = messageContext.getSequence(APISecurityConstants.API_BLOCKED_SEQUENCE);
        if (sequence != null) {
            sequence.mediate(messageContext);
        } else {
            if (log.isDebugEnabled()) {
                log.debug(APISecurityConstants.API_BLOCKED_SEQUENCE + " sequence not available ");
            }
        }
        Axis2Sender.sendBack(messageContext);
    }
}

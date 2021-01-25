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
package org.wso2.carbon.apimgt.gateway.mediators.webhooks;

import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;

/**
 * This mediator would build the message payload and save in the property in messageContext.
 */
public class MessageBuilder extends AbstractMediator {

    @Override
    public boolean mediate(MessageContext messageContext) {
        org.apache.axis2.context.MessageContext axisMsgContext = ((Axis2MessageContext) messageContext).
                getAxis2MessageContext();
        try {
            RelayUtils.buildMessage(axisMsgContext);
            String payload;
            if (JsonUtil.hasAJsonPayload(axisMsgContext)) {
                payload = JsonUtil.jsonPayloadToString(axisMsgContext);
            } else {
                payload = messageContext.getEnvelope().getBody().getFirstElement().toString();
            }
            messageContext.setProperty(APIConstants.Webhooks.PAYLOAD_PROPERTY, payload);
        } catch (IOException | XMLStreamException e) {
            handleException("Error while building the message", e, messageContext);
        }
        return true;
    }
}

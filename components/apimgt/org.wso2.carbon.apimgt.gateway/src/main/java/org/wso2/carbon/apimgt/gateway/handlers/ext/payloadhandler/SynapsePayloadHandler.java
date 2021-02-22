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

import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.common.gateway.extensionlistener.PayloadHandler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.xml.stream.XMLStreamException;

/**
 * This class is the synapse implementation of PayloadHandler interface. This handle synapse specific logic to consume
 * the payload from the related message context.
 */
public class SynapsePayloadHandler implements PayloadHandler {

    private static final Log log = LogFactory.getLog(SynapsePayloadHandler.class);
    private MessageContext messageContext;

    public SynapsePayloadHandler(MessageContext messageContext) {

        this.messageContext = messageContext;
    }

    /**
     * Consume and return the payload as String.
     *
     * @return String payload
     * @throws APIManagementException if an error occurs
     */
    @Override
    public String consumeAsString() throws APIManagementException {

        return buildMessage();
    }

    /**
     * Consume and return payload as InputStream.
     *
     * @return InputStream payload
     * @throws APIManagementException if an error occurs
     */
    @Override
    public InputStream consumeAsStream() throws APIManagementException {

        String payload = buildMessage();
        if (payload != null) {
            //for now, a wrapped stream of the string payload is returned. will have to improve this later.
            return new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8));
        }
        return null;
    }

    /**
     * Build message in axis2 message context and return the SOAP envelope body as string the payload.
     *
     * @return String payload
     * @throws APIManagementException if an error occurs while building the message
     */
    private String buildMessage() throws APIManagementException {

        try {
            RelayUtils.buildMessage(((Axis2MessageContext) this.messageContext).getAxis2MessageContext());
        } catch (IOException | XMLStreamException e) {
            String errorMessage = "Error while consuming payload";
            log.error(errorMessage, e);
            throw new APIManagementException(errorMessage, e);
        }
        SOAPEnvelope env = ((Axis2MessageContext) this.messageContext).getAxis2MessageContext().getEnvelope();
        if (env != null) {
            SOAPBody body = env.getBody();
            if (body != null) {
                env.buildWithAttachments();
                return body.toString();
            }
        }
        return null;
    }
}


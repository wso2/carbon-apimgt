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
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.notification.handlers;

import org.apache.axiom.om.OMElement;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.keymgt.KeyManagerEventHandler;
import org.wso2.carbon.apimgt.notification.internal.ServiceReferenceHolder;

import java.io.IOException;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

/**
 * This class used to read and handle notify event.
 */
public class KeyManagerNotificationEventHandler extends AbstractHandler {

    private static final Log log = LogFactory.getLog(KeyManagerNotificationEventHandler.class);

    @Override
    public boolean handleRequest(MessageContext messageContext) {

        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) messageContext)
                .getAxis2MessageContext();
        Map<String, String> headers =
                (Map) axis2MessageContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        try {
            String keyManagerType = headers.get(APIConstants.KeyManager.KEY_MANAGER_TYPE_HEADER);
            if (StringUtils.isEmpty(keyManagerType)) {
                keyManagerType = APIConstants.KeyManager.DEFAULT_KEY_MANAGER_TYPE;
            }
            RelayUtils.buildMessage(axis2MessageContext);
            OMElement xmlResponse = messageContext.getEnvelope().getBody().getFirstElement();
            if (xmlResponse != null) {
                KeyManagerEventHandler keyManagerEventHandler =
                        ServiceReferenceHolder.getInstance().getKeyManagerEventHandlerByType(keyManagerType);
                if (keyManagerEventHandler != null) {
                    return keyManagerEventHandler.handleEvent(xmlResponse);
                }
            }
        } catch (IOException | XMLStreamException | APIManagementException e) {
            log.error("Error while handling notification Event", e);
            return false;
        }
        return true;
    }

    @Override
    public boolean handleResponse(MessageContext messageContext) {

        return true;
    }
}

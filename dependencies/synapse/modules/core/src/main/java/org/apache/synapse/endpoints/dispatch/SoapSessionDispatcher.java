/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.synapse.endpoints.dispatch;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.synapse.MessageContext;

import javax.xml.namespace.QName;

public class SoapSessionDispatcher extends AbstractDispatcher {


    private static final QName QNAME_SERVICE_GROUP_ID
            = new QName("http://ws.apache.org/namespaces/axis2", "ServiceGroupId", "axis2");

    /**
     * Gives the endpoint based on the service group context ID of the request message.
     *
     * @param synCtx Request MessageContext, possibly containing a service group context ID.
     * @return Endpoint associated with the soap session, if current message is a soap session
     *         message and if current message is not the first message of the session. Returns null,
     *         if an Endpoint could not be found for the session.
     */
    public SessionInformation getSession(MessageContext synCtx) {
        return SALSessions.getInstance().getSession(
                extractSessionID(synCtx.getEnvelope().getHeader(), QNAME_SERVICE_GROUP_ID));
    }

    /**
     * As this is a server initiated session, this method will only be called for response messages.
     * It extracts the service group context ID (if available) from the message and updates the
     * session (service group context ID) -> endpoint map.
     *
     * @param synCtx MessageContext of the response message.
     */
    public void updateSession(MessageContext synCtx) {

        // get the service group context id
        // check if service group context id is a key of any entry
        // if not, add an entry <service group context id, endpoint>

        SOAPHeader header = synCtx.getEnvelope().getHeader();

        if (header != null) {
            OMElement replyTo = header.getFirstChildWithName(
                    AddressingConstants.Final.QNAME_WSA_REPLY_TO);

            if (replyTo != null) {
                OMElement referenceParameters = replyTo.getFirstChildWithName(new QName(
                        "http://www.w3.org/2005/08/addressing", "ReferenceParameters", "wsa"));

                if (referenceParameters != null) {
                    SALSessions.getInstance().updateSession(synCtx,
                            extractSessionID(referenceParameters, QNAME_SERVICE_GROUP_ID));

                } else if (log.isDebugEnabled()) {
                    log.debug("Couldn't find the WSA ReferenceParameters in the Reply-To " +
                            "header to retrieve the ServiceQroupId");
                }
            } else if (log.isDebugEnabled()) {
                log.debug("Couldn't find the WSA Reply-To header to retrieve the ServiceQroupId");
            }
        }
    }

    public void unbind(MessageContext synCtx) {
        SALSessions.getInstance().removeSession(extractSessionID(synCtx.getEnvelope().getHeader(),
                QNAME_SERVICE_GROUP_ID));
    }

    /**
     * Soap session is initiated by the server. So this method always returns true.
     *
     * @return true
     */
    public boolean isServerInitiatedSession() {
        return true;
    }

    public void removeSessionID(MessageContext syCtx) {
        removeSessionID(syCtx.getEnvelope().getHeader(), QNAME_SERVICE_GROUP_ID);
    }
}

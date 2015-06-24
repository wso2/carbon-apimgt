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

import org.apache.synapse.MessageContext;

import javax.xml.namespace.QName;

/**
 * This dispatcher is implemented to demonstrate a sample client session. It will detect sessions
 * based on the <syn:ClientID xmlns:syn="http://ws.apache.org/ns/synapse"> soap header of the
 * request message. Therefore, above header has to be included in the request soap messages by the
 * client who wants to initiate and maintain a session.
 */
public class SimpleClientSessionDispatcher extends AbstractDispatcher {


    private static final QName CSID_QNAME = new QName("http://ws.apache.org/ns/synapse",
            "ClientID", "syn");

    public SessionInformation getSession(MessageContext synCtx) {

        return SALSessions.getInstance().getSession(
                extractSessionID(synCtx.getEnvelope().getHeader(), CSID_QNAME));
    }

    public void updateSession(MessageContext synCtx) {
        SALSessions.getInstance().updateSession(synCtx,
                extractSessionID(synCtx.getEnvelope().getHeader(), CSID_QNAME));
    }

    public void unbind(MessageContext synCtx) {
        SALSessions.getInstance().removeSession(
                extractSessionID(synCtx.getEnvelope().getHeader(), CSID_QNAME));
    }

    public boolean isServerInitiatedSession() {
        return false;
    }

    public void removeSessionID(MessageContext syCtx) {
        // no need to remove
    }
}

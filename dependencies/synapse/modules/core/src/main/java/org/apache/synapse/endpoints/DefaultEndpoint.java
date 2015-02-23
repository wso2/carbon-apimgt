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

package org.apache.synapse.endpoints;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.transport.passthru.PassThroughConstants;
import org.apache.synapse.transport.passthru.Pipe;
import org.apache.synapse.transport.passthru.util.RelayUtils;

/**
 * This class represents an endpoint with the EPR as the 'To' header of the message. It is
 * responsible for sending the message to this EPR, performing retries etc on failure and
 * using any QOS etc as specified
 */
public class DefaultEndpoint extends AbstractEndpoint {
    public void onFault(MessageContext synCtx) {

        // is this really a fault or a timeout/connection close etc?
        if (isTimeout(synCtx)) {
            getContext().onTimeout();
        } else if (isSuspendFault(synCtx)) {
            getContext().onFault();
        }

        // this should be an ignored error if we get here
        setErrorOnMessage(synCtx, null, null);
        super.onFault(synCtx);
    }

    public void onSuccess() {
        if (getContext() != null) {
            getContext().onSuccess();
        }
    }

    public void send(MessageContext synCtx) {
    	
    	org.apache.axis2.context.MessageContext messageContext =((Axis2MessageContext) synCtx).getAxis2MessageContext();
    	final Pipe pipe = (Pipe) messageContext.getProperty(PassThroughConstants.PASS_THROUGH_PIPE);
        if (pipe != null && !Boolean.TRUE.equals(messageContext.getProperty(PassThroughConstants.MESSAGE_BUILDER_INVOKED)) && messageContext.getProperty("To") == null) {
        	 try {
	            RelayUtils.buildMessage(((Axis2MessageContext) synCtx).getAxis2MessageContext(),false);
            } catch (Exception e) {
            	 handleException("Error while building message", e);
            } 
        }
        if (getParentEndpoint() == null && !readyToSend()) {
            // if the this leaf endpoint is too a root endpoint and is in inactive
            informFailure(synCtx, SynapseConstants.ENDPOINT_ADDRESS_NONE_READY,
                    "Currently , Default endpoint : " + getContext());
        } else {
            super.send(synCtx);
        }
    }
}

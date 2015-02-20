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
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.synapse.transport.nhttp;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.AddressingHelper;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.transport.RequestResponseTransport;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This interface is a point of control for Axis2 to control the behaviour of a Request-Response
 * transport such as HTTP/s
 *
 * For nhttp, this does not make much of a difference, as we are capable of keeping a socket open
 * and writing to it from a different thread, while letting the initial thread that read the
 * request go free.
 */
public class HttpCoreRequestResponseTransport implements RequestResponseTransport {

    private static final Log log = LogFactory.getLog(HttpCoreRequestResponseTransport.class);
    // TODO: the proper value of the status field has to be RequestResponseTransportStatus.INITIAL
    // TODO: as per the axis2 documentation, but this is a workaround to get Sandesha2 to work
    // TODO: synapse nhttp transport. see : https://issues.apache.org/jira/browse/SYNAPSE-493
    private RequestResponseTransportStatus status = RequestResponseTransportStatus.WAITING;
    private MessageContext msgContext = null;
    private boolean responseWritten = false;

    public HttpCoreRequestResponseTransport(MessageContext msgContext) {
        this.msgContext = msgContext;
    }

    public void acknowledgeMessage(MessageContext msgContext) throws AxisFault {
        if (log.isDebugEnabled()) {
            log.debug("Acking one-way request");
        }
        // need to skip the ACK till we get the ACK from the actual service for the out-only MEP
        if ((AddressingHelper.isReplyRedirected(msgContext) &&
                    !msgContext.getReplyTo().hasNoneAddress()) ||
                (msgContext.getOperationContext() != null &&
                WSDL2Constants.MEP_URI_IN_ONLY.equals(msgContext.getOperationContext()
                        .getAxisOperation().getMessageExchangePattern()))) {
            
            status = RequestResponseTransportStatus.ACKED;
            msgContext.getOperationContext().setProperty(
                    Constants.RESPONSE_WRITTEN, Constants.VALUE_FALSE);
        }
    }

    public void awaitResponse() throws InterruptedException, AxisFault {
        if (log.isDebugEnabled()) {
            log.debug("Returning thread but keeping socket open -- awaiting response");
        }
        status = RequestResponseTransportStatus.WAITING;
        msgContext.getOperationContext().setProperty(Constants.RESPONSE_WRITTEN, "SKIP");
    }

    public void signalResponseReady() {
        if (log.isDebugEnabled()) {
            log.debug("Signal response available");
        }
        status = RequestResponseTransportStatus.SIGNALLED;
    }

    public RequestResponseTransportStatus getStatus() {
        return status;
    }

    public void signalFaultReady(AxisFault fault) {
    }
    
    public boolean isResponseWritten() {
		return responseWritten;
	}

	public void setResponseWritten(boolean responseWritten) {
		this.responseWritten = responseWritten;
	}
}

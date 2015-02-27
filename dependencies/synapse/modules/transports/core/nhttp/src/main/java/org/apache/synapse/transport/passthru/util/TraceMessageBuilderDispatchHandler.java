/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.synapse.transport.passthru.util;


import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.engine.AbstractDispatcher;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.transport.passthru.PassThroughConstants;
import org.apache.synapse.transport.passthru.Pipe;


/**
 * This enables the persist full in come message for SOAP tracing facility provided
 */
public class TraceMessageBuilderDispatchHandler extends AbstractDispatcher{

    private static final Log log = LogFactory.getLog(RelaySecuirtyMessageBuilderDispatchandler.class);
    public static final String NAME = "TraceMessageBuilderDispatchHandler";


    @Override
    public AxisOperation findOperation(AxisService axisService, MessageContext messageContext) throws AxisFault {
        return null;
    }

    @Override
    public AxisService findService(MessageContext messageContext) throws AxisFault {
        return null;
    }

    @Override
    public void initDispatcher() {
        init(new HandlerDescription(NAME));
    }

    @Override
    public InvocationResponse invoke(MessageContext messageContext) throws AxisFault {

        InvocationResponse invocationResponse = super.invoke(messageContext);
        Pipe pipe = (Pipe) messageContext.getProperty(PassThroughConstants.PASS_THROUGH_PIPE);
        if (pipe != null) {
            if (!messageContext.isEngaged(PassThroughConstants.SECURITY_MODULE_NAME)) {
                if (messageContext.isEngaged(PassThroughConstants.TRACE_SOAP_MESSAGE)) {
                    build(messageContext);
                }
            }
        }
        return invocationResponse;
    }

    private void build(MessageContext messageContext) {
        try {
            RelayUtils.buildMessage(messageContext, false);
        } catch (Exception e) {
            log.error("Error while executing the message at relaySecurity handler", e);
        }
    }
}

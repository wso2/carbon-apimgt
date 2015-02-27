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

package org.apache.synapse.core.axis2;

import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.handlers.AbstractHandler;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * This is a handler for axis2 which will clear the mustUnderstand ness of the headers
 * if there are any after the Dispatch, which will allow Synapse to get the message
 * even with unprocessed mustUnderstand headers
 */
public class SynapseMustUnderstandHandler extends AbstractHandler {

    public static final String NAME = "SynapseMustUnderstandHandler";
    
    public InvocationResponse invoke(MessageContext messageContext) throws AxisFault {

        SOAPEnvelope envelope = messageContext.getEnvelope();

        if (envelope.getHeader() != null) {
            Iterator headerBlocks = envelope.getHeader().getHeadersToProcess(null);
            ArrayList<SOAPHeaderBlock> markedHeaderBlocks = new ArrayList<SOAPHeaderBlock>();

            while (headerBlocks.hasNext()) {
                SOAPHeaderBlock headerBlock = (SOAPHeaderBlock) headerBlocks.next();

                // if this header block mustUnderstand but has not been processed
                // then mark it as processed to get the message in to Synapse
                if (!headerBlock.isProcessed() && headerBlock.getMustUnderstand()) {
                    markedHeaderBlocks.add(headerBlock);
                    headerBlock.setProcessed();
                }
            }

            // incase we need to get them inside synapse
            messageContext.setProperty("headersMarkedAsProcessedBySynapse", markedHeaderBlocks);
        }

        return InvocationResponse.CONTINUE;
    }
}

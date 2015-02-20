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
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.RelatesTo;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.ClientUtils;
import org.apache.axis2.description.OutInAxisOperation;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.transport.TransportUtils;
import org.apache.axis2.util.TargetResolver;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.transport.nhttp.NhttpConstants;

import javax.xml.namespace.QName;

/**
 * DynamicAxisOperation which switch dynamically between MEPs
 */
public class DynamicAxisOperation extends OutInAxisOperation {

	public DynamicAxisOperation() {
		super();
	}

	public DynamicAxisOperation(QName name) {
		super(name);
	}

	public OperationClient createClient(ServiceContext sc, Options options) {
		return new DynamicOperationClient(this, sc, options);
	}

	static class DynamicOperationClient extends OperationClient {

		DynamicOperationClient(OutInAxisOperation axisOp, ServiceContext sc, Options options) {
            super(axisOp, sc, options);
		}

		/**
         * same as OutInAxisOperationClient
		 */
		public void addMessageContext(MessageContext mc) throws AxisFault {
			mc.setServiceContext(sc);
			if (mc.getMessageID() == null) {
				setMessageID(mc);
			}
			axisOp.registerOperationContext(mc, oc);
		}

		/**
		 * same as OutInAxisOperationClient
		 */
		public MessageContext getMessageContext(String messageLabel) throws AxisFault {
			return oc.getMessageContext(messageLabel);
		}

		public void executeImpl(boolean block) throws AxisFault {

            // if the MEP is completed, throw a fault
            if (completed) {
				throw new AxisFault(Messages.getMessage("mepiscomplted"));
			}

            // if the OUT message is not set on the operation context, throw a fault
            MessageContext outMsgCtx = oc.getMessageContext(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
			if (outMsgCtx == null) {
				throw new AxisFault(Messages.getMessage("outmsgctxnull"));
			}

            ConfigurationContext cfgCtx = sc.getConfigurationContext();

            // set ClientOptions to the current outgoing message
            outMsgCtx.setOptions(options);

			// do Target Resolution
			TargetResolver tr = cfgCtx.getAxisConfiguration().getTargetResolverChain();
            if (tr != null) {
                tr.resolveTarget(outMsgCtx);
            }

            // if the transport to use for sending is not specified, try to find it from the URL
			TransportOutDescription transportOut = options.getTransportOut();
			if (transportOut == null) {
				EndpointReference toEPR =
                    (options.getTo() != null) ? options.getTo() : outMsgCtx.getTo();
				transportOut =
                    ClientUtils.inferOutTransport(cfgCtx.getAxisConfiguration(), toEPR, outMsgCtx);
			}
			outMsgCtx.setTransportOut(transportOut);

			if (options.getTransportIn() == null && outMsgCtx.getTransportIn() == null) {
				outMsgCtx.setTransportIn(ClientUtils.inferInTransport(
                        cfgCtx.getAxisConfiguration(), options, outMsgCtx));
			} else if (outMsgCtx.getTransportIn() == null) {
				outMsgCtx.setTransportIn(options.getTransportIn());
			}

            // add reference parameters to To EPR
            addReferenceParameters(outMsgCtx);

            if (options.isUseSeparateListener()) {

				options.setTransportIn(outMsgCtx.getConfigurationContext()
						.getAxisConfiguration().getTransportIn(Constants.TRANSPORT_HTTP));

				SynapseCallbackReceiver callbackReceiver =
                        (SynapseCallbackReceiver) axisOp.getMessageReceiver();
                
                ((Axis2MessageContext)((AsyncCallback)
                        axisCallback).getSynapseOutMsgCtx()).getAxis2MessageContext().setProperty(
                        NhttpConstants.IGNORE_SC_ACCEPTED, Constants.VALUE_TRUE);
                callbackReceiver.addCallback(outMsgCtx.getMessageID(), axisCallback);
                
                EndpointReference replyToFromTransport = outMsgCtx.getConfigurationContext()
                        .getListenerManager().getEPRforService(sc.getAxisService().getName(),
                        axisOp.getName().getLocalPart(), outMsgCtx.getTransportOut().getName());

				if (outMsgCtx.getReplyTo() == null) {
					outMsgCtx.setReplyTo(replyToFromTransport);
				} else {
					outMsgCtx.getReplyTo().setAddress(replyToFromTransport.getAddress());
				}

				outMsgCtx.getConfigurationContext().registerOperationContext(
						outMsgCtx.getMessageID(), oc);

                AxisEngine.send(outMsgCtx);

				// Options object reused so soapAction needs to be removed so
				// that soapAction+wsa:Action on response don't conflict
				options.setAction("");

			} else {

                SynapseCallbackReceiver callbackReceiver =
                    (SynapseCallbackReceiver) axisOp.getMessageReceiver();
                callbackReceiver.addCallback(outMsgCtx.getMessageID(), axisCallback);
                send(outMsgCtx);
			}
		}

		private void send(MessageContext msgctx) throws AxisFault {

			// create the responseMessageContext and set that its related to the current outgoing
            // message, so that it could be tied back to the original request even if the response
            // envelope does not contain addressing headers
            MessageContext responseMessageContext = new MessageContext();
            responseMessageContext.setMessageID(msgctx.getMessageID());
            responseMessageContext.setProperty(
                    SynapseConstants.RELATES_TO_FOR_POX, msgctx.getMessageID());
            responseMessageContext.setOptions(options);
            responseMessageContext.setServerSide(true);
			addMessageContext(responseMessageContext);
            responseMessageContext.setProperty("synapse.send", "true");

            AxisEngine.send(msgctx);

            // did the engine receive a immediate synchronous response?
            // e.g. sometimes the transport sender may listen for a syncronous reply
			if (msgctx.getProperty(MessageContext.TRANSPORT_IN) != null) {

                responseMessageContext.setOperationContext(msgctx.getOperationContext());                
                responseMessageContext.setAxisMessage(
                    msgctx.getOperationContext().getAxisOperation().
                    getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE));
                responseMessageContext.setAxisService(msgctx.getAxisService());

                responseMessageContext.setProperty(MessageContext.TRANSPORT_OUT,
                    msgctx.getProperty(MessageContext.TRANSPORT_OUT));
                responseMessageContext.setProperty(org.apache.axis2.Constants.OUT_TRANSPORT_INFO,
                    msgctx.getProperty(org.apache.axis2.Constants.OUT_TRANSPORT_INFO));

                responseMessageContext.setProperty(
                    org.apache.synapse.SynapseConstants.ISRESPONSE_PROPERTY, Boolean.TRUE);
                responseMessageContext.setTransportIn(msgctx.getTransportIn());
                responseMessageContext.setTransportOut(msgctx.getTransportOut());

                // If request is REST assume that the responseMessageContext is REST too
                responseMessageContext.setDoingREST(msgctx.isDoingREST());

                responseMessageContext.setProperty(MessageContext.TRANSPORT_IN,
                    msgctx.getProperty(MessageContext.TRANSPORT_IN));
                responseMessageContext.setTransportIn(msgctx.getTransportIn());
                responseMessageContext.setTransportOut(msgctx.getTransportOut());

                // Options object reused above so soapAction needs to be removed so
                // that soapAction+wsa:Action on response don't conflict
                responseMessageContext.setSoapAction("");

                if (responseMessageContext.getEnvelope() == null) {
                    // If request is REST we assume the responseMessageContext is
                    // REST, so set the variable

                    Options options = responseMessageContext.getOptions();
                    if (options != null) {
                        RelatesTo relatesTo = options.getRelatesTo();
                        if (relatesTo != null) {
                            relatesTo.setValue(msgctx.getMessageID());
                        } else {
                            options.addRelatesTo(new RelatesTo(msgctx.getMessageID()));
                        }
                    }

                    SOAPEnvelope resenvelope =
                        TransportUtils.createSOAPMessage(responseMessageContext);

                    if (resenvelope != null) {
                        responseMessageContext.setEnvelope(resenvelope);
                        AxisEngine.receive(responseMessageContext);
                        if (responseMessageContext.getReplyTo() != null) {
                            sc.setTargetEPR(responseMessageContext.getReplyTo());
                        }

                        complete(msgctx);
                    } else {
                        throw new AxisFault(
                                Messages.getMessage("blockingInvocationExpectsResponse"));
                    }
                }
            }
        }
        
    }
}

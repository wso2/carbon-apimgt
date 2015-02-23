/**
 *  Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.synapse.transport.passthru;

import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.OutOnlyAxisOperation;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.util.MessageContextBuilder;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.transport.passthru.config.TargetConfiguration;

public class TargetErrorHandler {
    private Log log = LogFactory.getLog(TargetErrorHandler.class);

    private TargetConfiguration targetConfiguration = null;

    public TargetErrorHandler(TargetConfiguration targetConfiguration) {
        this.targetConfiguration = targetConfiguration;
    }

    /**
     * Mark request to send failed with error
     *
     * @param mc the failed message context
     * @param errorCode the error code to raise
     * @param errorMessage the text for an error message to be returned to the MR on failure
     * @param exceptionToRaise an Exception to be returned to the MR on failure
     * @param state state of the connection
     */
    protected void handleError(final MessageContext mc,
                               final int errorCode,
                               final String errorMessage,
                               final Exception exceptionToRaise,
                               final ProtocolState state) {

        if (errorCode == -1 && errorMessage == null && exceptionToRaise == null) {
            return;
        }

        if (mc.getAxisOperation() == null ||
                mc.getAxisOperation().getMessageReceiver() == null) {
            return;
        }

//        if (mc.getOperationContext().isComplete()) {
//            return;
//        } ? why we ignoring this..

        targetConfiguration.getWorkerPool().execute(new Runnable() {
            public void run() {
                MessageReceiver mr = mc.getAxisOperation().getMessageReceiver();
                try {
                    AxisFault axisFault = (exceptionToRaise != null ?
                            new AxisFault(errorMessage, exceptionToRaise) :
                            new AxisFault(errorMessage));

                    MessageContext faultMessageContext =
                            MessageContextBuilder.createFaultMessageContext(mc, axisFault);

                    SOAPEnvelope envelope = faultMessageContext.getEnvelope();

                    if (log.isDebugEnabled()) {
                        log.debug("Sending Fault for Request with Message ID : "
                                + mc.getMessageID());
                    }

                    faultMessageContext.setTo(null);
                    faultMessageContext.removeProperty(PassThroughConstants.PASS_THROUGH_PIPE);

                    // copy the important properties from the original message context
                    faultMessageContext.setProperty(PassThroughConstants.PASS_THROUGH_SOURCE_CONNECTION,
                            mc.getProperty(PassThroughConstants.PASS_THROUGH_SOURCE_CONNECTION));
                    faultMessageContext.setProperty(PassThroughConstants.PASS_THROUGH_SOURCE_CONFIGURATION,
                            mc.getProperty(PassThroughConstants.PASS_THROUGH_SOURCE_CONFIGURATION));

                    faultMessageContext.setServerSide(true);
                    faultMessageContext.setDoingREST(mc.isDoingREST());
                    faultMessageContext.setProperty(MessageContext.TRANSPORT_IN, mc
                            .getProperty(MessageContext.TRANSPORT_IN));
                    faultMessageContext.setTransportIn(mc.getTransportIn());
                    faultMessageContext.setTransportOut(mc.getTransportOut());


					if (!(mc.getOperationContext().getAxisOperation() instanceof OutOnlyAxisOperation)) {
						faultMessageContext.setAxisMessage(mc.getOperationContext().getAxisOperation()
						                                     .getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE));
					}
                    
                    faultMessageContext.setOperationContext(mc.getOperationContext());
                    faultMessageContext.setConfigurationContext(mc.getConfigurationContext());
                    faultMessageContext.setTo(null);

                    faultMessageContext.setProperty(
                            PassThroughConstants.SENDING_FAULT, Boolean.TRUE);
                    faultMessageContext.setProperty(
                            PassThroughConstants.ERROR_MESSAGE, errorMessage);
                    if (errorCode != -1) {
                        faultMessageContext.setProperty(
                                PassThroughConstants.ERROR_CODE, getErrorCode(errorCode, state));
                    }
                    if (exceptionToRaise != null) {
                        faultMessageContext.setProperty(
                                PassThroughConstants.ERROR_DETAIL, exceptionToRaise.toString());
                        faultMessageContext.setProperty(
                                PassThroughConstants.ERROR_EXCEPTION, exceptionToRaise);
                        envelope.getBody().getFault().getDetail().setText(
                                exceptionToRaise.toString());
                    } else {
                        faultMessageContext.setProperty(
                                PassThroughConstants.ERROR_DETAIL, errorMessage);
                        envelope.getBody().getFault().getDetail().setText(errorMessage);
                    }

                    faultMessageContext.setProperty(PassThroughConstants.NO_ENTITY_BODY, true);

                    mr.receive(faultMessageContext);

                } catch (AxisFault af) {
                    log.error("Unable to report back failure to the message receiver", af);
                }
            }
        });
    }

    private int getErrorCode(int errorCode, ProtocolState state) {
        return errorCode + state.ordinal();
    }

}

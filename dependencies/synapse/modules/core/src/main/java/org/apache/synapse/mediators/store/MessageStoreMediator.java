/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.apache.synapse.mediators.store;

import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.message.store.MessageStore;
import org.apache.synapse.transport.nhttp.NhttpConstants;

/**
 * <code>MessageStoreMediator</code> will store the incoming Messages in associated MessageStore
 */
public class MessageStoreMediator extends AbstractMediator{

    /**
     * Name of the Mediator
     */
    private String name;

    /**
     *MessageStore Name
     */
    private String messageStoreName;

    /**
     * Sequence name to be invoked when the message is stored
     */
    private String  onStoreSequence;


    public boolean mediate(MessageContext synCtx) {
        if(synCtx != null) {
            MessageStore messageStore = synCtx.getConfiguration().getMessageStore(messageStoreName);
            if(messageStore != null) {
                if(onStoreSequence != null) {
                    Mediator sequence = synCtx.getSequence(onStoreSequence);
                    if(sequence != null) {
                        sequence.mediate(synCtx);
                    }
                }

                if(log.isDebugEnabled()) {
                    log.debug("Message Store mediator storing the message : \n " + synCtx.getEnvelope());
                }

                // Here we set the server name in the message context before storing the message.
                //This can be used by the Processors in a clustering setup.
                if(synCtx instanceof Axis2MessageContext) {

                    String serverName =
                                        getAxis2ParameterValue(((Axis2MessageContext)synCtx).
                                        getAxis2MessageContext().
                                        getConfigurationContext().getAxisConfiguration(),
                                        SynapseConstants.Axis2Param.SYNAPSE_SERVER_NAME);
                    if(serverName != null) {
                        synCtx.setProperty(SynapseConstants.Axis2Param.SYNAPSE_SERVER_NAME,
                                serverName);
                    }

                }

                // Ensure that the message is fully read
                synCtx.getEnvelope().buildWithAttachments();
                boolean result = messageStore.getProducer().storeMessage(synCtx);
                if (!result) {
                    synCtx.setProperty(NhttpConstants.HTTP_SC, 500);
                    synCtx.setProperty(NhttpConstants.ERROR_DETAIL, "Failed to store message.");
                    synCtx.setProperty(NhttpConstants.ERROR_MESSAGE, "Failed to store message [" + synCtx.getMessageID()
                                                        + "] in store [" + messageStore.getName() + "].");
                    handleException("Failed to store message [" + synCtx.getMessageID()
                                    + "] in store [" + messageStore.getName() + "].", synCtx);
                }
                // with the nio transport, this causes the listener not to write a 202
                // Accepted response, as this implies that Synapse does not yet know if
                // a 202 or 200 response would be written back.
                ((Axis2MessageContext) synCtx).getAxis2MessageContext().getOperationContext().setProperty(
                        org.apache.axis2.Constants.RESPONSE_WRITTEN, "SKIP");

                return true;
            } else {
                handleException("Message Store does not exist.", synCtx);
            }
        }
        return false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessageStoreName() {
        return messageStoreName;
    }

    public void setMessageStoreName(String messageStoreName) {
        this.messageStoreName = messageStoreName;
    }

    public String  getOnStoreSequence() {
        return onStoreSequence;
    }

    public void setOnStoreSequence(String  onStoreSequence) {
        this.onStoreSequence = onStoreSequence;
    }

     /**
     * Helper method to get a value of a parameters in the AxisConfiguration
     *
     * @param axisConfiguration AxisConfiguration instance
     * @param paramKey The name / key of the parameter
     * @return The value of the parameter
     */
    private static String getAxis2ParameterValue(AxisConfiguration axisConfiguration,
                                                 String paramKey) {

        Parameter parameter = axisConfiguration.getParameter(paramKey);
        if (parameter == null) {
            return null;
        }
        Object value = parameter.getValue();
        if (value != null && value instanceof String) {
            return (String) parameter.getValue();
        } else {
            return null;
        }
    }
}

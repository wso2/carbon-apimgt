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
package org.apache.synapse.config.xml;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.SynapseException;
import org.apache.synapse.message.processor.MessageProcessor;

import javax.xml.namespace.QName;
import java.util.Iterator;


/**
 * Create an instance of the given Message processor, and sets properties on it.
 * <p/>
 * &lt;messageProcessor name="string" class="classname" messageStore = "string" &gt;
 * &lt;parameter name="string"&gt"string" &lt;parameter&gt;
 * &lt;parameter name="string"&gt"string" &lt;parameter&gt;
 * &lt;parameter name="string"&gt"string" &lt;parameter&gt;
 * .
 * .
 * &lt;/messageProcessor&gt;
 */
public class MessageProcessorSerializer {

    private static final Log log = LogFactory.getLog(MessageProcessorSerializer.class);

    public static final String FORWARDING_PROCESSOR =
            "org.apache.synapse.message.processor.impl.forwarder.ScheduledMessageForwardingProcessor";

    protected static final OMFactory fac = OMAbstractFactory.getOMFactory();
    protected static final OMNamespace synNS = SynapseConstants.SYNAPSE_OMNAMESPACE;
    protected static final OMNamespace nullNS = fac.createOMNamespace(
            XMLConfigConstants.NULL_NAMESPACE, "");


    /**
     * Serialize a give Message processor instance to XML configuration
     * @param parent parent configuration
     * @param processor message processor instance
     * @return  created XML configuration
     */
    public static OMElement serializeMessageProcessor(OMElement parent, MessageProcessor processor) {
        OMElement processorElem = fac.createOMElement("messageProcessor", synNS);
        if (processor != null) {
            processorElem.addAttribute(fac.createOMAttribute("class", nullNS,
                    processor.getClass().getName()));
        } else {
            handleException("Invalid processor. Provider is required");
        }

        if (processor.getName() != null) {
            processorElem.addAttribute(fac.createOMAttribute("name", nullNS, processor.getName()));
        } else {
            handleException("Message store Name not specified");
        }

        if (FORWARDING_PROCESSOR.equals(processor.getClass().getName())) {
            if (processor.getTargetEndpoint() != null) {
                processorElem.addAttribute(fac.createOMAttribute("targetEndpoint", nullNS, processor.getTargetEndpoint()));
            } else {
                // This validation is removed to support backward compatibility
                // handleException("Target Endpoint not specified");
            }
        }

        if(processor.getMessageStoreName() != null) {
            processorElem.addAttribute(fac.createOMAttribute(
                    "messageStore",nullNS,processor.getMessageStoreName()));
        }

        if (processor.getParameters() != null) {
            Iterator iterator = processor.getParameters().keySet().iterator();
            while (iterator.hasNext()) {
                String name = (String) iterator.next();
                String value = (String) processor.getParameters().get(name);
                OMElement property = fac.createOMElement("parameter", synNS);
                property.addAttribute(fac.createOMAttribute(
                        "name", nullNS, name));
                property.setText(value.trim());
                processorElem.addChild(property);
            }
        }


        if (getSerializedDescription(processor) != null) {
            processorElem.addChild(getSerializedDescription(processor));
        }

        if (parent != null) {
            parent.addChild(processorElem);
        }
        return processorElem;
    }

    private static void handleException(String msg) {
        log.error(msg);
        throw new SynapseException(msg);
    }

    private static OMElement getSerializedDescription(MessageProcessor processor) {
        OMElement descriptionElem = fac.createOMElement(
                new QName(SynapseConstants.SYNAPSE_NAMESPACE, "description"));

        if (processor.getDescription() != null) {
            descriptionElem.setText(processor.getDescription());
            return descriptionElem;
        } else {
            return null;
        }
    }

}

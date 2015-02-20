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

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.SynapseException;
import org.apache.synapse.message.processor.MessageProcessor;
import org.apache.synapse.message.processor.MessageProcessorConstants;
import org.apache.synapse.message.processor.impl.forwarder.ScheduledMessageForwardingProcessor;
import org.apache.synapse.message.processor.impl.sampler.SamplingProcessor;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


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
public class MessageProcessorFactory {
    private static final Log log = LogFactory.getLog(MessageProcessorFactory.class);

    public static final String FORWARDING_PROCESSOR =
            "org.apache.synapse.message.processor.impl.forwarder.ScheduledMessageForwardingProcessor";

    public static final QName CLASS_Q = new QName(XMLConfigConstants.NULL_NAMESPACE, "class");
    public static final QName TARGET_ENDPOINT_Q = new QName(XMLConfigConstants.NULL_NAMESPACE, "targetEndpoint");
    public static final QName SEQUENCE_Q = new QName(XMLConfigConstants.NULL_NAMESPACE, "sequence");
    public static final QName NAME_Q = new QName(XMLConfigConstants.NULL_NAMESPACE, "name");
    public static final QName PARAMETER_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE,
            "parameter");
    public static final QName MESSAGE_STORE_Q = new QName(XMLConfigConstants.NULL_NAMESPACE ,
            "messageStore");
    private static final QName DESCRIPTION_Q
            = new QName(SynapseConstants.SYNAPSE_NAMESPACE, "description");


    /**
     * Creates a Message processor instance from given xml configuration element
     * @param elem OMElement of that contain the Message processor configuration
     * @return  created message processor instance
     */
    public static MessageProcessor createMessageProcessor(OMElement elem) {
        MessageProcessor processor = null;
        OMAttribute clssAtt = elem.getAttribute(CLASS_Q);

        if(clssAtt != null) {
            String clssName = clssAtt.getAttributeValue();
            // Make synapse configuration backward compatible
            if (MessageProcessorConstants.DEPRECATED_FORWARDING_PROCESSOR_CLASS.equals(clssName)) {
                clssName = ScheduledMessageForwardingProcessor.class.getName();
            } else if (MessageProcessorConstants.DEPRECATED_SAMPLING_PROCESSOR_CLASS.equals(clssName)) {
                clssName = SamplingProcessor.class.getName();
            }

            try {
                Class cls = Class.forName(clssName);
                processor = (MessageProcessor) cls.newInstance();
            } catch (Exception e) {
                handleException("Error while creating Message processor " + e.getMessage());
            }
        } else {
            /**We throw Exception since there is not default processor*/
            handleException("Can't create Message processor without a provider class");
        }

        OMAttribute nameAtt = elem.getAttribute(NAME_Q);
        if (nameAtt != null) {
            assert processor != null;
            processor.setName(nameAtt.getAttributeValue());
        } else {
            handleException("Can't create Message processor without a name ");
        }

        if (FORWARDING_PROCESSOR.equals(clssAtt.getAttributeValue())) {
            OMAttribute targetSequenceAtt = elem.getAttribute(TARGET_ENDPOINT_Q);

            if (targetSequenceAtt != null) {
                assert processor != null;
                processor.setTargetEndpoint(targetSequenceAtt.getAttributeValue());
            } else {
                // This validation is commented due to backward compatibility
                // handleException("Can't create Message processor without a target endpoint ");
            }
        }

        OMAttribute storeAtt = elem.getAttribute(MESSAGE_STORE_Q);

        if(storeAtt != null) {
            assert processor != null;
            processor.setMessageStoreName(storeAtt.getAttributeValue());
        } else {
            handleException("Can't create a message processor with out a message Store");
        }

        OMElement descriptionElem = elem.getFirstChildWithName(DESCRIPTION_Q);
        if (descriptionElem != null) {
            assert processor != null;
            processor.setDescription(descriptionElem.getText());
        }

        assert processor != null;
        processor.setParameters(getParameters(elem));

        return processor;
    }

    private static Map<String, Object> getParameters(OMElement elem) {
        Iterator params = elem.getChildrenWithName(PARAMETER_Q);
        Map<String, Object> parameters = new HashMap<String, Object>();

        while (params.hasNext()) {
            Object o = params.next();
            if (o instanceof OMElement) {
                OMElement prop = (OMElement) o;
                OMAttribute paramName = prop.getAttribute(NAME_Q);
                String paramValue = prop.getText();
                if (paramName != null) {
                    if (paramValue != null) {
                        parameters.put(paramName.getAttributeValue(), paramValue);
                    }
                } else {
                    handleException("Invalid MessageStore parameter - Parameter must have a name ");
                }
            }
        }
        return parameters;
    }

    private static void handleException(String msg) {
        log.error(msg);
        throw new SynapseException(msg);
    }
}

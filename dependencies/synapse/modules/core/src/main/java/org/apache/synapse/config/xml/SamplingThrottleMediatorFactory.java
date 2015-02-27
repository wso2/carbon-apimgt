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
import org.apache.synapse.Mediator;
import org.apache.synapse.mediators.eip.Target;
import org.apache.synapse.mediators.eip.sample.MessageQueue;
import org.apache.synapse.mediators.eip.sample.SamplingThrottleMediator;

import javax.xml.namespace.QName;
import java.util.Properties;

/**
 * Builds the {@link org.apache.synapse.mediators.eip.sample.SamplingThrottleMediator} instance by looking at the
 * following configuration</p>
 *
 * <pre>&lt;sampler id="string" rate="int" unitTime="long"&gt;
 *   &lt;messageQueue class="string"/&gt;
 *   &lt;target .../&gt;
 * &lt;sampler/&gt;
 * </pre>
 *
 * @see org.apache.synapse.config.xml.AbstractMediatorFactory
 */
public class SamplingThrottleMediatorFactory extends AbstractMediatorFactory {

    private static final QName SAMPLER_Q
            = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "sampler");
    private static final QName ID_ATTR
            = new QName(XMLConfigConstants.NULL_NAMESPACE, "id");
    private static final QName RATE_ATTR
            = new QName(XMLConfigConstants.NULL_NAMESPACE, "rate");
    private static final QName UNIT_TIME_ATTR
            = new QName(XMLConfigConstants.NULL_NAMESPACE, "unitTime");
    private static final QName MESSAGE_QUEUE_Q
            = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "messageQueue");
    private static final QName CLASS_ATTR
            = new QName(XMLConfigConstants.NULL_NAMESPACE, "class");

    public Mediator createSpecificMediator(OMElement omElement, Properties properties) {

        SamplingThrottleMediator samplingThrottleMediator = new SamplingThrottleMediator();
        processAuditStatus(samplingThrottleMediator, omElement);

        OMAttribute idAttribute = omElement.getAttribute(ID_ATTR);
        if (idAttribute != null) {
            samplingThrottleMediator.setId(idAttribute.getAttributeValue());
        }

        OMAttribute rateAttribute = omElement.getAttribute(RATE_ATTR);
        if (rateAttribute != null) {
            try {
                samplingThrottleMediator.setSamplingRate(
                        Integer.parseInt(rateAttribute.getAttributeValue()));
            } catch (NumberFormatException nfe) {
                handleException("Sampling rate has to be an integer value, but found : "
                        + rateAttribute.getAttributeValue());
            }
        }

        OMAttribute unitTimeAttribute = omElement.getAttribute(UNIT_TIME_ATTR);
        if (unitTimeAttribute != null) {
            try {
                samplingThrottleMediator.setUnitTime(
                        Long.parseLong(unitTimeAttribute.getAttributeValue()));
            } catch (NumberFormatException nfe) {
                handleException("Sampling unitTime has to be a long value in milliseconds, " +
                        "but found : " + rateAttribute.getAttributeValue());
            }
        }

        OMElement targetElem = omElement.getFirstChildWithName(TARGET_Q);
        if (targetElem != null) {
            Target target = TargetFactory.createTarget(targetElem, properties);
            samplingThrottleMediator.setTarget(target);
        } else {
            handleException("Sampler requires a target for the sampling mediation");
        }

        OMElement messageQueueElem = omElement.getFirstChildWithName(MESSAGE_QUEUE_Q);
        if (messageQueueElem != null && messageQueueElem.getAttribute(CLASS_ATTR) != null) {
            String className = messageQueueElem.getAttributeValue(CLASS_ATTR);
            try {
                Class messageQueueImplClass = Class.forName(className);
                Object obj = messageQueueImplClass.newInstance();
                if (obj instanceof MessageQueue) {
                    samplingThrottleMediator.setMessageQueue((MessageQueue) obj);
                } else {
                    handleException("Provided message queue class : " + className
                            + " doesn't implement the org.apache.synapse.mediators." +
                            "eip.sample.MessageQueue interface");
                }
            } catch (ClassNotFoundException e) {
                handleException("Couldn't find the class specified for the message queue " +
                        "implementation : " + className);
            } catch (InstantiationException e) {
                handleException("Couldn't instantiate the message queue : " + className);
            } catch (IllegalAccessException e) {
                handleException("Couldn't instantiate the message queue : " + className);
            }
        }

        return samplingThrottleMediator;
    }

    public QName getTagQName() {
        return SAMPLER_Q;
    }
}

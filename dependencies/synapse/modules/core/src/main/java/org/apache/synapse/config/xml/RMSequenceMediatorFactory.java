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
import org.apache.synapse.SynapseException;
import org.apache.synapse.mediators.builtin.RMSequenceMediator;
import org.jaxen.JaxenException;

import javax.xml.namespace.QName;
import java.util.Properties;

/**
 * Factory for {@link RMSequenceMediator} instances.
 * <p>
 * Configuration syntax:
 * <pre>
 * &lt;RMSequence (correlation="xpath" [last-message="xpath"]) | single="true" [version="1.0|1.1"]/&gt;
 * </pre>
 */
public class RMSequenceMediatorFactory extends AbstractMediatorFactory {

    private static final QName SEQUENCE_Q
            = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "RMSequence");
    private static final QName ATT_CORR = new QName("correlation");
    private static final QName ATT_LASTMSG = new QName("last-message");
    private static final QName ATT_VERSION = new QName("version");
    private static final QName ATT_SINGLE = new QName("single");

    public Mediator createSpecificMediator(OMElement elem, Properties properties) {

        RMSequenceMediator sequenceMediator = new RMSequenceMediator();
        OMAttribute correlation = elem.getAttribute(ATT_CORR);
        OMAttribute lastMessage = elem.getAttribute(ATT_LASTMSG);
        OMAttribute single = elem.getAttribute(ATT_SINGLE);
        OMAttribute version = elem.getAttribute(ATT_VERSION);

        if (single == null && correlation == null) {
            String msg = "The 'single' attribute value of true or a 'correlation' attribute is " +
                "required for the configuration of a RMSequence mediator";
            log.error(msg);
            throw new SynapseException(msg);
        }

        if (correlation != null) {
            if (correlation.getAttributeValue() != null &&
                correlation.getAttributeValue().trim().length() == 0) {
                String msg = "Invalid attribute value specified for correlation";
                log.error(msg);
                throw new SynapseException(msg);

            } else {
                try {
                    sequenceMediator.setCorrelation(
                        SynapseXPathFactory.getSynapseXPath(elem, ATT_CORR));
                } catch (JaxenException e) {
                    String msg = "Invalid XPath expression for attribute correlation : "
                        + correlation.getAttributeValue();
                    log.error(msg);
                    throw new SynapseException(msg);
                }
            }
        }

        if (single != null) {
            sequenceMediator.setSingle(Boolean.valueOf(single.getAttributeValue()));
        }

        if (sequenceMediator.isSingle() && sequenceMediator.getCorrelation() != null) {
            String msg = "Invalid RMSequence mediator. A RMSequence can't have both a "
                + "single attribute value of true and a correlation attribute specified.";
            log.error(msg);
            throw new SynapseException(msg);

        } else if (!sequenceMediator.isSingle() && sequenceMediator.getCorrelation() == null) {
            String msg = "Invalid RMSequence mediator. A RMSequence must have a "
                + "single attribute value of true or a correlation attribute specified.";
            log.error(msg);
            throw new SynapseException(msg);
        }

        if (lastMessage != null) {
            if (lastMessage.getAttributeValue() != null &&
                lastMessage.getAttributeValue().trim().length() == 0) {
                String msg = "Invalid attribute value specified for last-message";
                log.error(msg);
                throw new SynapseException(msg);

            } else {
                try {
                    sequenceMediator.setLastMessage(
                        SynapseXPathFactory.getSynapseXPath(elem, ATT_LASTMSG));
                } catch (JaxenException e) {
                    String msg = "Invalid XPath expression for attribute last-message : "
                        + lastMessage.getAttributeValue();
                    log.error(msg);
                    throw new SynapseException(msg);
                }
            }
        }

        if (sequenceMediator.isSingle() && sequenceMediator.getLastMessage() != null) {
            String msg = "Invalid RMSequence mediator. A RMSequence can't have both a "
                + "single attribute value of true and a last-message attribute specified.";
            log.error(msg);
            throw new SynapseException(msg);
        }

        if (version != null) {
            if (!XMLConfigConstants.SEQUENCE_VERSION_1_0.equals(version.getAttributeValue()) &&
                !XMLConfigConstants.SEQUENCE_VERSION_1_1.equals(version.getAttributeValue())) {
                String msg = "Only '" + XMLConfigConstants.SEQUENCE_VERSION_1_0 + "' or '" +
                    XMLConfigConstants.SEQUENCE_VERSION_1_1
                    + "' values are allowed for attribute version for a RMSequence mediator"
                    + ", Unsupported version " + version.getAttributeValue();
                log.error(msg);
                throw new SynapseException(msg);
            }
            sequenceMediator.setVersion(version.getAttributeValue());
        }

        // after successfully creating the mediator
        // set its common attributes such as tracing etc
        processAuditStatus(sequenceMediator, elem);

        return sequenceMediator;
    }

    public QName getTagQName() {
        return SEQUENCE_Q;
    }
}

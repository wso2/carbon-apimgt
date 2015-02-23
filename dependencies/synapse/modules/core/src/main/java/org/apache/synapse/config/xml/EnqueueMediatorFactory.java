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

import org.apache.synapse.Mediator;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.mediators.builtin.EnqueueMediator;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMAttribute;

import javax.xml.namespace.QName;
import java.util.Properties;


public class EnqueueMediatorFactory extends AbstractMediatorFactory{

    public static final QName ENQUEUE_Q = new QName(SynapseConstants.SYNAPSE_NAMESPACE, "enqueue");
    public static final QName SEQUENCE_ATT = new QName("sequence");
    public static final QName PRIORITY_ATT = new QName("priority");
    public static final QName QUEUE_ATT = new QName("executor");

    public Mediator createSpecificMediator(OMElement elem, Properties properties) {
        EnqueueMediator mediator = new EnqueueMediator();

        OMAttribute seqAtt = elem.getAttribute(SEQUENCE_ATT);
        if (seqAtt != null && !"".equals(seqAtt.getAttributeValue())) {
            mediator.setSequenceName(seqAtt.getAttributeValue());
        } else {
            handleException("sequence is a required attribue");
        }

        OMAttribute priorityAtt = elem.getAttribute(PRIORITY_ATT);
        if (priorityAtt != null && !"".equals(priorityAtt.getAttributeValue())) {
            mediator.setPriority(Integer.parseInt(priorityAtt.getAttributeValue()));
        }

        OMAttribute queueAtt = elem.getAttribute(QUEUE_ATT);
        if (queueAtt != null && !"".equals(queueAtt.getAttributeValue())) {
            mediator.setExecutorName(queueAtt.getAttributeValue());
        } else {
            handleException("Queue is a required attribue");
        }

        return mediator;
    }

    public QName getTagQName() {
        return ENQUEUE_Q;
    }
}

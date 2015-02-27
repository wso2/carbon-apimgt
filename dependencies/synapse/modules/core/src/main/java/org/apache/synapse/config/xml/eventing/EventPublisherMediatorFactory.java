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

package org.apache.synapse.config.xml.eventing;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.Mediator;
import org.apache.synapse.config.xml.AbstractMediatorFactory;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.mediators.eventing.EventPublisherMediator;

import javax.xml.namespace.QName;
import java.util.Properties;

/**
 * Factory for {@link org.apache.synapse.mediators.eventing.EventPublisherMediator} instances.
 * <sequence>
 * <eventPublisher eventSourceName="name"/>
 * </sequence>
 */
public class EventPublisherMediatorFactory extends AbstractMediatorFactory {

    private static final QName TAG_NAME =
            new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "eventPublisher");
    private static final QName PROP_NAME = new QName("eventSourceName");

    public QName getTagQName() {
        return TAG_NAME;
    }

    public Mediator createSpecificMediator(OMElement elem, Properties properties) {
        EventPublisherMediator eventPublisherMediator = new EventPublisherMediator();
        OMAttribute attEventSource = elem.getAttribute(PROP_NAME);
        if (attEventSource != null) {
            eventPublisherMediator.setEventSourceName(attEventSource.getAttributeValue());
        } else {
            handleException(
                    "The 'eventSourceName' attribute is required for the EventPublisher mediator");
        }
        return eventPublisherMediator;
    }
}

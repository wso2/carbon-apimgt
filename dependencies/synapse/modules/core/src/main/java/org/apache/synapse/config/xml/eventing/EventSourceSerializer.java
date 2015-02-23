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

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.databinding.utils.ConverterUtil;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.eventing.SynapseEventSource;
import org.wso2.eventing.Subscription;

import java.util.Iterator;
import java.util.List;
import java.util.Collection;

/**
 * <eventSource name="blah">
 * <subscriptionManager class="org.apache.synapse.events.DefaultInMemorySubscriptionManager">
 * <property name="other" value="some text property"/>
 * </subscriptionManager>
 * <subscription id="static1">
 * <filter....>
 * <sequence...>
 * <endpoint..>
 * </subscription>*
 * <eventSource>
 */
public class EventSourceSerializer {

    public static OMElement serializeEventSource(OMElement elem, SynapseEventSource eventSource) {

        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace nullNS = fac.createOMNamespace(XMLConfigConstants.NULL_NAMESPACE, "");

        OMElement evenSourceElem =
                fac.createOMElement("eventSource", XMLConfigConstants.SYNAPSE_OMNAMESPACE);
        if (eventSource.getName() != null) {
            evenSourceElem
                    .addAttribute(fac.createOMAttribute("name", nullNS, eventSource.getName()));
        }

        if (eventSource.getSubscriptionManager() != null) {
            OMElement subManagerElem = fac.createOMElement("subscriptionManager",
                    XMLConfigConstants.SYNAPSE_OMNAMESPACE);
            subManagerElem.addAttribute(fac.createOMAttribute("class", nullNS,
                    eventSource.getSubscriptionManager().getClass().getName()));
            Collection<String> names =
                    (Collection<String>) eventSource.getSubscriptionManager().getPropertyNames();
            for (String name : names) {
                String value;
                if (eventSource.isContainsConfigurationProperty(name)) {
                    value = eventSource.getConfigurationProperty(name);
                } else {
                    value = eventSource.getSubscriptionManager().getPropertyValue(name);
                }
                OMElement propElem =
                        fac.createOMElement("property", XMLConfigConstants.SYNAPSE_OMNAMESPACE);
                propElem.addAttribute(fac.createOMAttribute("name", nullNS, name));
                propElem.addAttribute(fac.createOMAttribute("value", nullNS, value));
                subManagerElem.addChild(propElem);
            }
            evenSourceElem.addChild(subManagerElem);
            // Adding static subscriptions
            List<Subscription> staticSubscriptionList =
                    eventSource.getSubscriptionManager().getStaticSubscriptions();
            for (Iterator<Subscription> iterator = staticSubscriptionList.iterator();
                 iterator.hasNext();) {
                Subscription staticSubscription = iterator.next();
                OMElement staticSubElem =
                        fac.createOMElement("subscription", XMLConfigConstants.SYNAPSE_OMNAMESPACE);
                staticSubElem.addAttribute(
                        fac.createOMAttribute("id", nullNS, staticSubscription.getId()));
                OMElement filterElem =
                        fac.createOMElement("filter", XMLConfigConstants.SYNAPSE_OMNAMESPACE);
                filterElem.addAttribute(fac.createOMAttribute("source", nullNS,
                        (String) staticSubscription.getFilterValue()));
                filterElem.addAttribute(fac.createOMAttribute("dialect", nullNS,
                        (String) staticSubscription.getFilterDialect()));
                staticSubElem.addChild(filterElem);
                OMElement endpointElem =
                        fac.createOMElement("endpoint", XMLConfigConstants.SYNAPSE_OMNAMESPACE);
                OMElement addressElem =
                        fac.createOMElement("address", XMLConfigConstants.SYNAPSE_OMNAMESPACE);
                addressElem.addAttribute(
                        fac.createOMAttribute("uri", nullNS, staticSubscription.getEndpointUrl()));
                endpointElem.addChild(addressElem);
                staticSubElem.addChild(endpointElem);
                if (staticSubscription.getExpires() != null) {
                    OMElement expiresElem =
                            fac.createOMElement("expires", XMLConfigConstants.SYNAPSE_OMNAMESPACE);
                    fac.createOMText(expiresElem,
                            ConverterUtil.convertToString(staticSubscription.getExpires()));
                    staticSubElem.addChild(expiresElem);
                }
                evenSourceElem.addChild(staticSubElem);
            }

        }

        if (elem != null) {
            elem.addChild(evenSourceElem);
        }

        return evenSourceElem;
    }
}

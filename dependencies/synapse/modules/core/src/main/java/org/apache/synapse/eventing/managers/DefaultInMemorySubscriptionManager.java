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

package org.apache.synapse.eventing.managers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.eventing.SynapseEventingConstants;
import org.apache.synapse.eventing.filters.TopicBasedEventFilter;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.jaxen.JaxenException;
import org.wso2.eventing.*;
import org.wso2.eventing.exceptions.EventException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 */
public class DefaultInMemorySubscriptionManager implements SubscriptionManager<MessageContext> {

    private final Map<String, Subscription> store =
            new ConcurrentHashMap<String, Subscription>();
    private String topicHeaderName;
    private String topicHeaderNS;
    private SynapseXPath topicXPath;
    private final Map<String, String> properties = new HashMap<String, String>();
    private static final Log log = LogFactory.getLog(DefaultInMemorySubscriptionManager.class);

    public List<Subscription> getStaticSubscriptions() {
        LinkedList<Subscription> list = new LinkedList<Subscription>();
        for (Subscription storeSubscription : store.values()) {
            if (storeSubscription.isStaticEntry()) {
                list.add(storeSubscription);
            }
        }
        return list;
    }

    public String subscribe(Subscription subscription) throws EventException {
        if (subscription.getId() == null) {
            subscription.setId(org.apache.axiom.om.util.UUIDGenerator.getUUID());
        }
        store.put(subscription.getId(), subscription);
        return subscription.getId();

    }

  public boolean unsubscribe(String id) throws EventException {
        if (store.containsKey(id)) {
            store.remove(id);
            return true;
        } else {
            return false;
        }
    }


    public boolean renew(Subscription subscription) throws EventException {
        Subscription subscriptionOld = getSubscription(subscription.getId());
        if (subscriptionOld != null) {
            subscriptionOld.setExpires(subscription.getExpires());
            return true;
        } else {
            return false;
        }
    }

    public List<Subscription> getSubscriptions() throws EventException {
        LinkedList<Subscription> list = new LinkedList<Subscription>();
        for (Map.Entry<String, Subscription> stringSubscriptionEntry : store.entrySet()) {
            list.add(stringSubscriptionEntry.getValue());
        }
        return list;
    }

    public List<Subscription> getAllSubscriptions() throws EventException {
        LinkedList<Subscription> list = new LinkedList<Subscription>();
        for (Map.Entry<String, Subscription> stringSubscriptionEntry : store.entrySet()) {
            list.add(stringSubscriptionEntry.getValue());
        }
        return list;
    }

    public List<Subscription> getMatchingSubscriptions(Event<MessageContext> event)
            throws EventException {
        final LinkedList<Subscription> list = new LinkedList<Subscription>();
        for (Map.Entry<String, Subscription> stringSubscriptionEntry : store.entrySet()) {
            //TODO : pick the filter based on the dialect
            //XPathBasedEventFilter filter = new XPathBasedEventFilter();
            TopicBasedEventFilter filter = new TopicBasedEventFilter();
            if (filter != null) {
                filter.setResultValue(stringSubscriptionEntry.getValue().getFilterValue());
                filter.setSourceXpath(topicXPath);
                //evaluatedValue = topicXPath.stringValueOf(mc);
            }
            if (filter == null || filter.match(event)) {
                Subscription subscription = stringSubscriptionEntry.getValue();
                Calendar current = Calendar.getInstance(); //Get current date and time
                if (subscription.getExpires() != null) {
                    if (current.before(subscription.getExpires())) {
                        // add only valid subscriptions by checking the expiration
                        list.add(subscription);
                    }
                } else {
                    // If a expiration dosen't exisits treat it as a never expire subscription, valid till unsubscribe
                    list.add(subscription);
                }

            }
        }
        return list;
    }

    public Subscription getSubscription(String id) {
        return store.get(id);
    }

    public Subscription getStatus(String s) throws EventException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }


    public void init() {
        try {
            //TODO: pick values from the constants
            topicHeaderName = getPropertyValue("topicHeaderName");
            if(topicHeaderName==null){
                handleException("Unable to create topic header topic header name is null");
            }
            topicHeaderNS = getPropertyValue("topicHeaderNS");
            if(topicHeaderNS==null){
                handleException("Unable to create topic header topic header namespace is null");
            }
            topicXPath = new SynapseXPath(
                    "s11:Header/ns:" + topicHeaderName + " | s12:Header/ns:" + topicHeaderName);
            topicXPath.addNamespace("s11", "http://schemas.xmlsoap.org/soap/envelope/");
            topicXPath.addNamespace("s12", "http://www.w3.org/2003/05/soap-envelope");
            topicXPath.addNamespace("ns", topicHeaderNS);
        } catch (JaxenException e) {
            handleException("Unable to create the topic header XPath", e);
        }
    }

    public void addProperty(String name, String value) {
        properties.put(name, value);
    }

    public Collection<String> getPropertyNames() {
        return properties.keySet();
    }

    public String getPropertyValue(String name) {
        return properties.get(name);
    }

    private void handleException(String message) {
        log.error(message);
        throw new SynapseException(message);
    }

    private void handleException(String message, Exception e) {
        log.error(message, e);
        throw new SynapseException(message, e);
    }
}

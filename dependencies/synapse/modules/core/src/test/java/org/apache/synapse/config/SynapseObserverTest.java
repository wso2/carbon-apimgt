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

package org.apache.synapse.config;

import junit.framework.TestCase;
import org.apache.synapse.endpoints.Endpoint;
import org.apache.synapse.endpoints.AddressEndpoint;
import org.apache.synapse.eventing.SynapseEventSource;
import org.apache.synapse.core.axis2.ProxyService;
import org.apache.synapse.Mediator;
import org.apache.synapse.Startup;
import org.apache.synapse.mediators.template.TemplateMediator;
import org.apache.synapse.startup.quartz.SimpleQuartz;
import org.apache.synapse.commons.executors.PriorityExecutor;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.apache.axis2.engine.AxisConfiguration;

import java.util.*;

public class SynapseObserverTest extends TestCase {

    private static final int ENDPOINT   = 0;
    private static final int SEQUENCE   = 1;
    private static final int ENTRY      = 2;
    private static final int PROXY      = 3;
    private static final int STARTUP    = 4;
    private static final int EVENT_SRC  = 5;
    private static final int EXECUTOR   = 6;
    private static final int SEQUENCE_TEMPLATE   = 7;

    SimpleSynapseObserver observer = new SimpleSynapseObserver();

    public void testSimpleObserver() {
        SynapseConfiguration synapseConfig = new SynapseConfiguration();
        synapseConfig.setAxisConfiguration(new AxisConfiguration());
        synapseConfig.registerObserver(observer);

        Endpoint epr = new AddressEndpoint();
        epr.setName("endpoint1");
        synapseConfig.addEndpoint(epr.getName(), epr);
        assertItemAdded(epr.getName(), ENDPOINT);
        synapseConfig.removeEndpoint(epr.getName());
        assertItemRemoved(epr.getName(), ENDPOINT);

        SequenceMediator seq = new SequenceMediator();
        seq.setName("sequence1");
        synapseConfig.addSequence(seq.getName(), seq);
        assertItemAdded(seq.getName(), SEQUENCE);
        synapseConfig.removeSequence(seq.getName());
        assertItemRemoved(seq.getName(), SEQUENCE);

        TemplateMediator template = new TemplateMediator();
        template.setName("template1");
        synapseConfig.addSequenceTemplate(template.getName(), template);
        assertItemAdded(template.getName(), SEQUENCE_TEMPLATE);
        synapseConfig.removeSequenceTemplate(template.getName());
        assertItemRemoved(template.getName(), SEQUENCE_TEMPLATE);

        Entry entry = new Entry();
        entry.setKey("entry1");
        synapseConfig.addEntry(entry.getKey(), entry);
        assertItemAdded(entry.getKey(), ENTRY);
        synapseConfig.removeEntry(entry.getKey());
        assertItemRemoved(entry.getKey(), ENTRY);

        ProxyService proxy = new ProxyService("proxy1");
        synapseConfig.addProxyService(proxy.getName(), proxy);
        assertItemAdded(proxy.getName(), PROXY);
        synapseConfig.removeProxyService(proxy.getName());
        assertItemRemoved(proxy.getName(), PROXY);

        Startup startup = new SimpleQuartz();
        startup.setName("startup1");
        synapseConfig.addStartup(startup);
        assertItemAdded(startup.getName(), STARTUP);
        synapseConfig.removeStartup(startup.getName());
        assertItemRemoved(startup.getName(), STARTUP);

        SynapseEventSource eventSrc = new SynapseEventSource("eventSrc1");
        synapseConfig.addEventSource(eventSrc.getName(), eventSrc);
        assertItemAdded(eventSrc.getName(), EVENT_SRC);
        synapseConfig.removeEventSource(eventSrc.getName());
        assertItemRemoved(eventSrc.getName(), EVENT_SRC);

        PriorityExecutor exec = new PriorityExecutor();
        exec.setName("exec1");
        synapseConfig.addPriorityExecutor(exec.getName(), exec);
        assertItemAdded(exec.getName(), EXECUTOR);
        synapseConfig.removeExecutor(exec.getName());
        assertItemRemoved(exec.getName(), EXECUTOR);
    }

    private void assertItemAdded(String name, int type) {
        assertTrue(observer.tracker.get(type).contains(name));
    }

    private void assertItemRemoved(String name, int type) {
        assertTrue(!observer.tracker.get(type).contains(name));
    }

    private class SimpleSynapseObserver implements SynapseObserver {

        private Map<Integer, Set<String>> tracker = new HashMap<Integer, Set<String>>();

        public SimpleSynapseObserver() {
            tracker.put(ENDPOINT, new HashSet<String>());
            tracker.put(SEQUENCE, new HashSet<String>());
            tracker.put(ENTRY, new HashSet<String>());
            tracker.put(PROXY, new HashSet<String>());
            tracker.put(STARTUP, new HashSet<String>());
            tracker.put(EVENT_SRC, new HashSet<String>());
            tracker.put(EXECUTOR, new HashSet<String>());
            tracker.put(SEQUENCE_TEMPLATE, new HashSet<String>());
        }

        public void endpointAdded(Endpoint endpoint) {
            tracker.get(ENDPOINT).add(endpoint.getName());
        }

        public void endpointRemoved(Endpoint endpoint) {
            tracker.get(ENDPOINT).remove(endpoint.getName());
        }

        public void entryAdded(Entry entry) {
            tracker.get(ENTRY).add(entry.getKey());
        }

        public void entryRemoved(Entry entry) {
            tracker.get(ENTRY).remove(entry.getKey());
        }

        public void eventSourceAdded(SynapseEventSource eventSource) {
            tracker.get(EVENT_SRC).add(eventSource.getName());
        }

        public void eventSourceRemoved(SynapseEventSource eventSource) {
            tracker.get(EVENT_SRC).remove(eventSource.getName());
        }

        public void proxyServiceAdded(ProxyService proxy) {
            tracker.get(PROXY).add(proxy.getName());
        }

        public void proxyServiceRemoved(ProxyService proxy) {
            tracker.get(PROXY).remove(proxy.getName());
        }

        public void sequenceAdded(Mediator sequence) {
            tracker.get(SEQUENCE).add(((SequenceMediator) sequence).getName());
        }

        public void sequenceRemoved(Mediator sequence) {
            tracker.get(SEQUENCE).remove(((SequenceMediator) sequence).getName());
        }

        public void sequenceTemplateAdded(Mediator template) {
            tracker.get(SEQUENCE_TEMPLATE).add(((TemplateMediator) template).getName());
        }

        public void sequenceTemplateRemoved(Mediator template) {
            tracker.get(SEQUENCE_TEMPLATE).remove(((TemplateMediator) template).getName());
        }

        public void startupAdded(Startup startup) {
            tracker.get(STARTUP).add(startup.getName());
        }

        public void startupRemoved(Startup startup) {
            tracker.get(STARTUP).remove(startup.getName());
        }

        public void priorityExecutorAdded(PriorityExecutor exec) {
            tracker.get(EXECUTOR).add(exec.getName());
        }

        public void priorityExecutorRemoved(PriorityExecutor exec) {
            tracker.get(EXECUTOR).remove(exec.getName());
        }
    }
}

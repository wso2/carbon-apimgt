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

import org.apache.synapse.Mediator;
import org.apache.synapse.Startup;
import org.apache.synapse.commons.executors.PriorityExecutor;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.apache.synapse.eventing.SynapseEventSource;
import org.apache.synapse.core.axis2.ProxyService;
import org.apache.synapse.endpoints.Endpoint;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.mediators.template.TemplateMediator;

public abstract class AbstractSynapseObserver implements SynapseObserver {

    protected Log log;

    public AbstractSynapseObserver() {
        log = LogFactory.getLog(this.getClass()); 
    }

    public void sequenceAdded(Mediator sequence) {
        log.info("Sequence : " + ((SequenceMediator) sequence).getName() + " was added " +
                "to the Synapse configuration successfully" );
    }

    public void sequenceRemoved(Mediator sequence) {
        log.info("Sequence : " + ((SequenceMediator) sequence).getName() + " was  removed " +
                "from the Synapse configuration successfully");
    }

    public void sequenceTemplateAdded(Mediator template) {
        log.info("Template : " + ((TemplateMediator) template).getName() + " was added " +
                "to the Synapse configuration successfully" );
    }

    public void sequenceTemplateRemoved(Mediator template) {
        log.info("Template : " + ((TemplateMediator) template).getName() + " was removed " +
                "to the Synapse configuration successfully" );
    }

    public void entryAdded(Entry entry) {
        log.info("Local entry : " + entry.getKey() + " was added " +
                "to the Synapse configuration successfully");
    }

    public void entryRemoved(Entry entry) {
        log.info("Local entry : " + entry.getKey() + " was removed " +
                "from the Synapse configuration successfully");
    }

    public void endpointAdded(Endpoint endpoint) {
        log.info("Endpoint : " + endpoint.getName() + " was added " +
                "to the Synapse configuration successfully");
    }

    public void endpointRemoved(Endpoint endpoint) {
        log.info("Endpoint : " + endpoint.getName() + " was removed " +
                "from the Synapse configuration successfully");
    }

    public void proxyServiceAdded(ProxyService proxy) {
        log.info("Proxy service : " + proxy.getName() + " was added " +
                "to the Synapse configuration successfully");
    }

    public void proxyServiceRemoved(ProxyService proxy) {
        log.info("Proxy service : " + proxy.getName() + " was removed " +
                "from the Synapse configuration successfully");
    }

    public void startupAdded(Startup startup) {
        log.info("Startup : " + startup.getName() + " was added " +
                "to the Synapse configuration successfully");
    }

    public void startupRemoved(Startup startup) {
        log.info("Startup : " + startup.getName() + " was removed " +
                "from the Synapse configuration successfully");
    }

    public void eventSourceAdded(SynapseEventSource eventSource) {
        log.info("Event source : " + eventSource.getName() + " was added " +
                "to the Synapse configuration successfully");
    }

    public void eventSourceRemoved(SynapseEventSource eventSource) {
        log.info("Event source : " + eventSource.getName() + " was removed " +
                "from the Synapse configuration successfully");
    }

    public void priorityExecutorAdded(PriorityExecutor exec) {
        log.info("Priority executor : " + exec.getName() + " was added " +
                "to the Synapse configuration successfully");
    }

    public void priorityExecutorRemoved(PriorityExecutor exec) {
        log.info("Priority executor : " + exec.getName() + " was removed " +
                "from the Synapse configuration successfully");
    }
}

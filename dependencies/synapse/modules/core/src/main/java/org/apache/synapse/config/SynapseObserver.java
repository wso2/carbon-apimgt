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
import org.apache.synapse.eventing.SynapseEventSource;
import org.apache.synapse.core.axis2.ProxyService;
import org.apache.synapse.endpoints.Endpoint;

/**
 * An implementation of this interface can be registered with the SynapseConfiguration to receive
 * configuration update events. Whenever a new item is added to the configuration or an existing
 * item is removed the events defined in this interface will be fired.
 */
public interface SynapseObserver {

    /** Event fired when a new sequence is added to the configuration
     *
     * @param sequence the Sequence mediator added to the configuration
     */
    public void sequenceAdded(Mediator sequence);

    /** Event fired when an existing sequence is removed from the configuration
     *
     * @param sequence the Sequence removed from the configuration
     */
    public void sequenceRemoved(Mediator sequence);

    /** Event fired when a new sequence template is added to the configuration
     *
     * @param template the Sequence mediator added to the configuration
     */
    public void sequenceTemplateAdded(Mediator template);

    /** Event fired when an existing sequence template is removed from the configuration
     *
     * @param template the Sequence removed from the configuration
     */
    public void sequenceTemplateRemoved(Mediator template);

    /** Event fired when an entry is added to the configuration
     *
     * @param entry the Entry added to the configuration
     */
    public void entryAdded(Entry entry);

    /** Event fired when an entry is removed from the configuration
     *
     * @param entry the Entry removed from the configuration
     */
    public void entryRemoved(Entry entry);

    /** Event fired when an endpoint is added to the configuration
     *
     * @param endpoint the Endpoint added to the configuration
     */
    public void endpointAdded(Endpoint endpoint);

    /** Event fired when an endpoint is removed from the configuration
     *
     * @param endpoint the Endpoint removed from the configuration
     */
    public void endpointRemoved(Endpoint endpoint);

    /** Event fired when a proxy service is added to the configuration
     *
     * @param proxy the ProxyService added to the configuration
     */
    public void proxyServiceAdded(ProxyService proxy);

     /** Event fired when a proxy service is removed from the configuration
     *
     * @param proxy the ProxyService removed from the configuration
     */
    public void proxyServiceRemoved(ProxyService proxy);

    /** Event fired when a startup is added to the configuration
     *
     * @param startup the Startup added to the configuration
     */
    public void startupAdded(Startup startup);

     /** Event fired when a startup is removed from the configuration
     *
     * @param startup the Startup removed from the configuration
     */
    public void startupRemoved(Startup startup);

    /** Event fired when an event source is added to the configuration
     *
     * @param eventSource the SynapseEventSource added to the configuration
     */
    public void eventSourceAdded(SynapseEventSource eventSource);

     /** Event fired when an event source is removed from the configuration
     *
     * @param eventSource the SynapseEventSource removed from the configuration 
     */
    public void eventSourceRemoved(SynapseEventSource eventSource);

    /** Event fired when a priority executor is added to the configuration
     *
     * @param exec the PriorityExecutor added to the configuration 
     */
    public void priorityExecutorAdded(PriorityExecutor exec);

    /** Event fired when a priority executor is removed from the configuration
     *
     * @param exec the PriorityExecutor removed from the configuration
     */
    public void priorityExecutorRemoved(PriorityExecutor exec);

}

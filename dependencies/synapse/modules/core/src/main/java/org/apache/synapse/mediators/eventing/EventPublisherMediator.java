package org.apache.synapse.mediators.eventing;
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.eventing.SynapseEventSource;
import org.apache.synapse.mediators.AbstractMediator;

public class EventPublisherMediator extends AbstractMediator {
    private static final Log log = LogFactory.getLog(EventPublisherMediator.class);
    private String eventSourceName = null;

    public boolean mediate(MessageContext synCtx) {
        if (log.isDebugEnabled()) {
            log.debug("Mediation for Event Publisher started");
        }
        SynapseEventSource eventSource = synCtx.getConfiguration().getEventSource(eventSourceName);
        eventSource.dispatchEvents(synCtx);
        return true;
    }

    public String getEventSourceName() {
        return eventSourceName;
    }

    public void setEventSourceName(String eventSourceName) {
        this.eventSourceName = eventSourceName;
    }

}

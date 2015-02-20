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

package org.apache.synapse.mediators.ext;

import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.mediators.AbstractMediator;

import java.util.HashMap;
import java.util.Map;

/**
 * The class mediator delegates the mediation to a single instance of a specified
 * class. The specified class must implement the Mediator interface and optionally
 * may implement the ManagedLifecycle interface. At initialization time, a single
 * instance of the class is instantiated using a public no argument constructor, and
 * any one-time properties (parameter constants specified through the Synapse config)
 * are set on the instance. If each request needs synchronization, the user must
 * implement it within the specified class.
 * 
 * @see Mediator
 */
public class ClassMediator extends AbstractMediator implements ManagedLifecycle {

    /** The reference to the actual class that implments the Mediator interface */
    private Mediator mediator = null;
    /** A list of simple properties that would be set on the class before being used */
    private final Map<String, Object> properties = new HashMap<String, Object>();

    /**
	 * Don't use a new instance... do one instance of the object per instance of
	 * this mediator
	 * 
	 * @param synCtx
	 *            the message context
	 * @return as per standard semantics
	 */
	public boolean mediate(MessageContext synCtx) {

        SynapseLog synLog = getLog(synCtx);

        if (synLog.isTraceOrDebugEnabled()) {
            synLog.traceOrDebug("Start : Class mediator");

            if (synLog.isTraceTraceEnabled()) {
                synLog.traceTrace("Message : " + synCtx.getEnvelope());
            }
        }

        if (synLog.isTraceOrDebugEnabled()) {
			synLog.traceOrDebug("invoking : " + mediator.getClass() + ".mediate()");
		}

        boolean result;

        try {
			result = mediator.mediate(synCtx);
        } catch (Exception e) {
            // throw Synapse Exception for any exception in class meditor
            // so that the fault handler will be invoked
            throw new SynapseException("Error occured in the mediation of the class mediator", e);
        }

        synLog.traceOrDebug("End : Class mediator");
        
        return result;
    }

    public void destroy() {
        if (log.isDebugEnabled()) {
            log.debug("Destroying class mediator instance for : " + mediator.getClass());
        }
        if (mediator instanceof ManagedLifecycle) {
            ((ManagedLifecycle) mediator).destroy();
        }
    }

    public void init(SynapseEnvironment se) {
        if (log.isDebugEnabled()) {
            log.debug("Initializing class mediator instance for : " + mediator.getClass());
        }
        if (mediator == null) {
            log.warn("init() called before mediator reference set");
            return;
        }

        if (mediator instanceof ManagedLifecycle) {
            ((ManagedLifecycle) mediator).init(se);
        }
    }

    public void setMediator(Mediator mediator) {
		this.mediator = mediator;
	}

	public Mediator getMediator() {
		return mediator;
	}

    public void addProperty(String name, Object value) {
        properties.put(name, value);
    }

    public Map getProperties() {
        return this.properties;
    }

    @Override
    public boolean isContentAware() {
        return mediator.isContentAware();
    }
}

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

package org.apache.synapse.startup.tasks;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.SynapseException;
import org.apache.synapse.commons.jmx.MBeanRegistrar;
import org.apache.synapse.config.Entry;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.config.xml.MediatorFactoryFinder;
import org.apache.synapse.config.xml.endpoints.XMLToEndpointMapper;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.endpoints.Endpoint;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.apache.synapse.registry.AbstractRegistry;
import org.apache.synapse.registry.RegistryEntry;
import org.apache.synapse.task.Task;

import javax.xml.namespace.QName;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Items is a xml configuration
 * <resources>
 *     <resource type="sequence | endpoint | xml | text">registry path</resource>
 * </resources>
 */
public class RegistryResourceFetcher implements Task, ManagedLifecycle {
    private static Log log = LogFactory.getLog(RegistryResourceFetcher.class);

    public static final String SEQUENCE = "sequence";
    public static final String ENDPOINT = "endpoint";
    public static final String XML      = "xml";
    public static final String TEXT     = "text";

    private OMElement items;

    private List<RegistryResourceEntry> registryResources = new ArrayList<RegistryResourceEntry>();

    private SynapseConfiguration synapseConfiguration = null;

    private SynapseEnvironment synapseEnvironment = null;

    private AbstractRegistry registry = null;

    private int backOffFactor = 1;

    private int maxSuspendThreshold = 100;

    private int suspendThreshold = 4;

    private int currentFailedCount = 0;

    private int executionCount = 0;

    private int nextSuspendExecutionCount = 1;

    private long lastExecutionTime = 0;

    private State state = State.INIT;

    private ReadWriteLock lock = new ReentrantReadWriteLock();

    private RegistryResourceFetcherView view = null;

    public OMElement getItems() {
        return items;
    }

    public void setItems(OMElement items) {
        this.items = items;
    }

    public int getSuspendThreshold() {
        return suspendThreshold;
    }

    public int getMaxSuspendThreshold() {
        return maxSuspendThreshold;
    }

    public int getBackOffFactor() {
        return backOffFactor;
    }

    public void setBackOffFactor(int backOffFactor) {
        this.backOffFactor = backOffFactor;
    }

    public void setMaxSuspendThreshold(int maxSuspendThreshold) {
        this.maxSuspendThreshold = maxSuspendThreshold;
    }

    public void setSuspendThreshold(int suspendThreshold) {
        this.suspendThreshold = suspendThreshold;
    }

    public void init(SynapseEnvironment se) {
        if (items == null) {
            String msg = "resources configuration is required";
            log.error(msg);
            throw new SynapseException(msg);
        }

        Iterator it = items.getChildrenWithName(new QName("resource"));
        while (it.hasNext()) {
            OMElement resourceElement = (OMElement) it.next();

            String path = resourceElement.getText();
            String type = "xml";

            OMAttribute typeAttribute = resourceElement.getAttribute(new QName("type"));
            if (typeAttribute != null) {
                type = typeAttribute.getAttributeValue();
            }

            registryResources.add(new RegistryResourceEntry(path, type));
        }

        this.synapseConfiguration = se.getSynapseConfiguration();
        this.registry = (AbstractRegistry) se.getSynapseConfiguration().getRegistry();
        this.synapseEnvironment = se;

        this.view = new RegistryResourceFetcherView(this);
        MBeanRegistrar.getInstance().registerMBean(view, "ESB-Registry", "RegistryResourceFetcher");

        this.state = State.ACTIVE;
    }

    public void destroy() {
        MBeanRegistrar.getInstance().unRegisterMBean("ESB-Registry", "RegistryResourceFetcher");
    }

    public void execute() {
        Lock readerLock = lock.readLock();
        readerLock.lock();
        try {
            boolean execute = false;
            executionCount++;
            if (state == State.SUSPENDED) {
                if (executionCount >= maxSuspendThreshold) {
                    execute = true;
                }
            } else if (state == State.BACK_OFF) {
                if (nextSuspendExecutionCount == executionCount) {
                    nextSuspendExecutionCount = nextSuspendExecutionCount * backOffFactor;
                    execute = true;
                }
            } else if (state == State.SUSPECT || state == State.ACTIVE) {
                execute = true;
            }

            if (!execute) {
                if (log.isDebugEnabled()) {
                    log.debug("Skipping the execution because the Registry Fetching is at SUSPENDED state");
                }
                return;
            }

            for (RegistryResourceEntry key : registryResources) {
                if (state == State.ACTIVE) {
                    Entry entry = synapseConfiguration.getEntryDefinition(key.getPath());

                    if (entry == null) {
                        log.warn("A non remote entry has being specified: " + key.getPath());
                        return;
                    }

                    if (key.getType().equals(SEQUENCE)) {
                        entry.setMapper(MediatorFactoryFinder.getInstance());
                    } else if (key.getType().equals(ENDPOINT)) {
                        entry.setMapper(XMLToEndpointMapper.getInstance());
                    }

                    fetchEntry(key.getPath());
                }
            }

            lastExecutionTime = System.currentTimeMillis();
        } finally {
            readerLock.unlock();
        }
    }

    private class RegistryResourceEntry {
        private String path;

        private String type;

        private RegistryResourceEntry(String path, String type) {
            this.path = path;
            this.type = type;
        }

        public String getPath() {
            return path;
        }

        public String getType() {
            return type;
        }
    }

    /**
     * Get the resource with the given key
     *
     * @param key the key of the resource required
     */
    private void fetchEntry(String key) {
        Map localRegistry = synapseConfiguration.getLocalRegistry();

        Object o = localRegistry.get(key);
        if (o != null && o instanceof Entry) {
            Entry entry = (Entry) o;

            // This must be a dynamic entry whose cache has expired or which is not cached at all
            // A registry lookup is in order
            if (registry != null) {
                if (entry.isCached()) {
                    try {
                        Object resource = getResource(entry, synapseConfiguration.getProperties());
                        if (resource == null) {
                            log.warn("Failed to load the resource at the first time, " +
                                    "non-existing resource: " + key);
                        } else {
                            entry.setExpiryTime(Long.MAX_VALUE);
                        }
                        onSuccess();
                    } catch (Exception e) {
                        // Error occured while loading the resource from the registry
                        // Fall back to the cached value - Do not increase the expiry time
                        log.warn("Error while loading the resource " + key + " from the remote " +
                                "registry. Previously cached value will be used. Check the " +
                                "registry accessibility.");
                        onError();
                    }
                } else {
                    try {
                        // Resource not available in the cache - Must load from the registry
                        // No fall backs possible here!!
                        Object resource = getResource(entry, synapseConfiguration.getProperties());
                        if (resource == null) {
                            log.warn("Failed to load the resource at the first time, " +
                                    "non-existing resource: " + key);
                        } else {
                            entry.setExpiryTime(Long.MAX_VALUE);
                        }
                    } catch (Exception e) {
                        // failed to get the resource for the first time
                        log.warn("Failed to load the resource at the first time, " +
                                "non-existing resource: " + key);
                    }
                }
            } else {
                if (entry.isCached()) {
                    // Fall back to the cached value
                    log.warn("The registry is no longer available in the Synapse configuration. " +
                            "Using the previously cached value for the resource : " + key);
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Will not  evaluate the value of the remote entry with a key "
                            + key + ",  because the registry is not available");
                    }

                }
            }
        }
    }

    private Object getResource(Entry entry, Properties properties) {

        OMNode omNode;
        RegistryEntry re = registry.getRegistryEntry(entry.getKey());
        omNode = registry.lookup(entry.getKey());

        if (re == null) {
            return null;
        }

        if ((!entry.isCached() || (re.getVersion() == Long.MIN_VALUE ||
                re.getVersion() != entry.getVersion())) || re.getLastModified() >= lastExecutionTime) {
            entry.setEntryProperties(registry.getResourceProperties(entry.getKey()));
            entry.setVersion(re.getVersion());

            // if we get here, we have received the raw omNode from the
            // registry and our previous copy (if we had one) has expired or is not valid
            Object expiredValue = entry.getValue();

            // if we have a XMLToObjectMapper for this entry, use it to convert this
            // resource into the appropriate object - e.g. sequence or endpoint
            if (entry.getMapper() != null) {
                entry.setValue(entry.getMapper().getObjectFromOMNode(omNode, properties));

                if (entry.getValue() instanceof SequenceMediator) {
                    SequenceMediator seq = (SequenceMediator) entry.getValue();
                    seq.setDynamic(true);
                    seq.setRegistryKey(entry.getKey());
                    seq.init(synapseEnvironment);
                } else if (entry.getValue() instanceof  Endpoint) {
                    Endpoint ep = (Endpoint) entry.getValue();
                    ep.init(synapseEnvironment);
                }
            } else {
                // if the type of the object is known to have a mapper, create the
                // resultant Object using the known mapper, and cache this Object
                // else cache the raw OMNode
                entry.setValue(omNode);
            }

            if (expiredValue != null) {
                // Destroy the old resource so that everything is properly cleaned up
                if (expiredValue instanceof SequenceMediator) {
                    ((SequenceMediator) expiredValue).destroy();
                } else if (expiredValue instanceof Endpoint) {
                    ((Endpoint) expiredValue).destroy();
                }
            }

            entry.setVersion(re.getVersion());
        }

        // renew cache lease for another cachable duration (as returned by the
        // new getRegistryEntry() call
        if (re.getCachableDuration() > 0) {
            entry.setExpiryTime(
                    System.currentTimeMillis() + re.getCachableDuration());
        } else {
            entry.setExpiryTime(-1);
        }

        return entry.getValue();
    }

    private void onError() {
        currentFailedCount++;
        if (state == State.SUSPECT) {
            if (currentFailedCount == suspendThreshold) {
                log.info("Registry fetching state moved to :" + State.BACK_OFF +
                        " Registry is no longer available & Cached values will be used");
                state = State.BACK_OFF;
                executionCount = 0;
                nextSuspendExecutionCount = 1;
            }
        } else if (state == State.BACK_OFF) {
            if (executionCount >= maxSuspendThreshold) {
                log.info("Registry fetching state moved to :" + State.SUSPENDED +
                        " Will be retried in another " + maxSuspendThreshold);
                state = State.SUSPENDED;
                executionCount = 0;
                nextSuspendExecutionCount = 1;
            }
        } else if (state == State.SUSPENDED) {
            // we remain in the same stage
            state = State.SUSPENDED;
            executionCount = 0;
        } else if (state == State.ACTIVE) {
            // we move to the SUSPECT stage
            log.info("Registry fetching state moved to :" + State.SUSPECT +
                        " Registry seems to be no longer available & Cached values will be used");
            state = State.SUSPECT;
        }
    }

    private void onSuccess() {
        currentFailedCount = 0;
        if (state != State.ACTIVE) {
            log.info("Registry state changed from: " + state + " " + State.ACTIVE);
        }

        state = State.ACTIVE;
    }

    public void setState(State state) {
        Lock writeLock = lock.writeLock();
        writeLock.lock();
        try {
            if (state == State.ACTIVE) {
                currentFailedCount = 0;
                executionCount = 0;
                nextSuspendExecutionCount = 1;
                lastExecutionTime = 0;
            } else if (state == State.SUSPENDED) {
                currentFailedCount = 0;
                executionCount = 0;
                nextSuspendExecutionCount = 0;
            }
            this.state = state;
        } finally {
            writeLock.unlock();
        }
    }

    public State getState() {
        return state;
    }

    public void reset() {
        log.info("Reset the state to the initial values");
        state = State.ACTIVE;
        currentFailedCount = 0;
        executionCount = 0;
        nextSuspendExecutionCount = 1;
        lastExecutionTime = 0;
    }
}

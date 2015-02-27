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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axis2.AxisFault;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.Mediator;
import org.apache.synapse.Startup;
import org.apache.synapse.SynapseArtifact;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.SynapseException;
import org.apache.synapse.carbonext.TenantInfoConfigProvider;
import org.apache.synapse.carbonext.TenantInfoConfigurator;
import org.apache.synapse.commons.datasource.DataSourceRepositoryHolder;
import org.apache.synapse.commons.executors.PriorityExecutor;
import org.apache.synapse.config.xml.MediatorFactoryFinder;
import org.apache.synapse.config.xml.TemplateMediatorFactory;
import org.apache.synapse.config.xml.XMLToTemplateMapper;
import org.apache.synapse.config.xml.endpoints.TemplateFactory;
import org.apache.synapse.config.xml.endpoints.XMLToEndpointMapper;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2SynapseEnvironment;
import org.apache.synapse.core.axis2.ProxyService;
import org.apache.synapse.deployers.SynapseArtifactDeploymentStore;
import org.apache.synapse.endpoints.Endpoint;
import org.apache.synapse.endpoints.Template;
import org.apache.synapse.endpoints.dispatch.SALSessions;
import org.apache.synapse.eventing.SynapseEventSource;
import org.apache.synapse.libraries.imports.SynapseImport;
import org.apache.synapse.libraries.model.Library;
import org.apache.synapse.libraries.util.LibDeployerUtils;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.apache.synapse.mediators.template.TemplateMediator;
import org.apache.synapse.message.processor.MessageProcessor;
import org.apache.synapse.message.store.MessageStore;
import org.apache.synapse.registry.Registry;
import org.apache.synapse.rest.API;
import org.apache.synapse.util.xpath.ext.SynapseXpathFunctionContextProvider;
import org.apache.synapse.util.xpath.ext.SynapseXpathVariableResolver;
import org.apache.synapse.util.xpath.ext.XpathExtensionUtil;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The SynapseConfiguration holds the global configuration for a Synapse
 * instance.
 */
@SuppressWarnings({"UnusedDeclaration"})
public class SynapseConfiguration implements ManagedLifecycle, SynapseArtifact {

    private static final Log log = LogFactory.getLog(SynapseConfiguration.class);

    private static final String ENTRY = "entry";
    private static final String ENDPOINT = "endpoint";
    private static final String SEQUENCE = "sequence";
    private static final String TEMPLATE = "sequence-template";

    /**
     * The remote registry made available to the Synapse configuration. Only one
     * is supported
     */
    private Registry registry = null;

    /**
     * This holds the default QName of the configuration.
     */
    private QName defaultQName = null;

    /**
     * Mandatory sequence is treated specially since it is required for each and every message.
     * Keeps the reference to the mandatory sequence while it is available as a sequence in the
     * localRegistry map
     */
    private Mediator mandatorySequence = null;

    /**
     * Holds Proxy services defined through Synapse
     */
    private final Map<String, ProxyService> proxyServices = new ConcurrentHashMap<String, ProxyService>();

    /**
     * This holds a Map of ManagedLifeCycle objects
     */
    private final Map<String, Startup> startups = new ConcurrentHashMap<String, Startup>();

    /**
     * The local registry is a simple HashMap and provides the ability to
     * override definitions of a remote registry for entries defined locally
     * with the same key
     */
    private final Map<String, Object> localRegistry = new ConcurrentHashMap<String, Object>();

    /**
     * Holds the synapse properties
     */
    private Properties properties = new Properties();

    /**
     * This will provide the timer daemon object for the scheduled tasks.
     */
    private Timer synapseTimer = new Timer(true);

    /**
     * Hold reference to the Axis2 ConfigurationContext
     */
    private AxisConfiguration axisConfiguration = new AxisConfiguration();

    /**
     * Save the path to the configuration file loaded, to save it later if
     * required
     */
    private String pathToConfigFile = null;


    /**
     * Holds Event Sources defined through Synapse
     */
    private Map<String, SynapseEventSource> eventSources = new ConcurrentHashMap<String, SynapseEventSource>();

    /**
     * The list of registered configuration observers
     */
    private List<SynapseObserver> observers = new ArrayList<SynapseObserver>();

    /**
     * Executors for executing sequences with priorities
     */
    private Map<String, PriorityExecutor> executors = new ConcurrentHashMap<String, PriorityExecutor>();

    /**
     * Messages stores for the synapse configuration.
     */
    private Map<String, MessageStore> messageStores = new ConcurrentHashMap<String, MessageStore>();

    /**
     * Message processors in the synapse configuration
     */
    private Map<String, MessageProcessor> messageProcessors = new ConcurrentHashMap<String, MessageProcessor>();

    /**
     * Endpoint templates to create actual endpoints
     */
    private Map<String, Template> endpointTemplates = new ConcurrentHashMap<String, Template>();

    private Map<String, API> apiTable = new ConcurrentHashMap<String, API>();

    /**
     * Description/documentation of the configuration
     */
    private String description = null;

    /**
     * The artifact deployment store to keep track of the items deployed
     */
    private SynapseArtifactDeploymentStore artifactDeploymentStore = new SynapseArtifactDeploymentStore();

    /**
     * Holds synapse Libraries indexed by library qualified name
     */
    Map<String, Library> synapseLibraries = new ConcurrentHashMap<String, Library>();

    /**
     * Holds the library imports  currently being included into Synapse engine
     */
    Map<String, SynapseImport> synapseImports = new ConcurrentHashMap<String, SynapseImport>();
    
    
    /**
     * Cachable HasMap to hold the decrypted information in its synapse configuration space.
     * 
     */
    private Map<String, Object> decryptedCacheMap = new ConcurrentHashMap<String, Object>();
    
    private boolean allowHotUpdate = true;

    /**
     * Add a named sequence into the local registry. If a sequence already exists by the specified
     * key a runtime exception is thrown.
     *
     * @param key      the name for the sequence
     * @param mediator a Sequence mediator
     */
    public synchronized void addSequence(String key, Mediator mediator) {
        assertAlreadyExists(key, SEQUENCE);
        localRegistry.put(key, mediator);

        for (SynapseObserver o : observers) {
            o.sequenceAdded(mediator);
        }
    }

    /**
     * Add a sequence-template into the local registry. If a template already exists by the specified
     * key a runtime exception is thrown.
     *
     * @param key      the name for the sequence
     * @param mediator a Sequence mediator
     */
    public synchronized void addSequenceTemplate(String key, TemplateMediator mediator) {
        assertAlreadyExists(key, TEMPLATE);
        localRegistry.put(key, mediator);

        for (SynapseObserver o : observers) {
            o.sequenceTemplateAdded(mediator);
        }
    }

    /**
     * Update a sequence-template into the local registry. If a template already exists
     * by the specified key a runtime exception is thrown.
     *
     * @param key      the name for the sequence
     * @param mediator a Sequence mediator
     */
    public synchronized void updateSequenceTemplate(String key, TemplateMediator mediator) {
        localRegistry.put(key, mediator);

        for (SynapseObserver o : observers) {
            o.sequenceTemplateAdded(mediator);
        }
    }

    public synchronized void updateSequence(String key, Mediator mediator) {
        localRegistry.put(key, mediator);
        for (SynapseObserver o : observers) {
            o.sequenceAdded(mediator);
        }
    }

    /**
     * Allow a dynamic sequence to be cached and made available through the
     * local registry. If a sequence already exists by the specified
     * key a runtime exception is thrown.
     *
     * @param key   the key to lookup the sequence from the remote registry
     * @param entry the Entry object which holds meta information and the cached
     *              resource
     * @deprecated
     */
    public void addSequence(String key, Entry entry) {
        assertAlreadyExists(key, ENTRY);
        localRegistry.put(key, entry);
    }

    /**
     * Returns the map of defined sequences in the configuration excluding the
     * fetched sequences from remote registry.
     *
     * @return Map of SequenceMediators defined in the local configuration
     */
    public Map<String, SequenceMediator> getDefinedSequences() {

        Map<String, SequenceMediator> definedSequences = new HashMap<String, SequenceMediator>();

        synchronized (this) {
            for (Object o : localRegistry.values()) {
                if (o instanceof SequenceMediator) {
                    SequenceMediator seq = (SequenceMediator) o;
                    definedSequences.put(seq.getName(), seq);
                }
            }
        }
        return definedSequences;
    }

    /**
     * Returns the map of defined synapse templates in the configuration excluding the
     * fetched sequences from remote registry.
     *
     * @return Map of Templates defined in the local configuration
     */
    public Map<String, TemplateMediator> getSequenceTemplates() {

        Map<String, TemplateMediator> definedTemplates = new HashMap<String, TemplateMediator>();

        synchronized (this) {
            for (Object o : localRegistry.values()) {
                if (o instanceof TemplateMediator) {
                    TemplateMediator template = (TemplateMediator) o;
                    definedTemplates.put(template.getName(), template);
                }
            }
        }
        return definedTemplates;
    }

    /**
     * Returns the map of defined synapse endpoint templates in the configuration excluding the
     * fetched sequences from remote registry.
     *
     * @return Map of Templates defined in the local configuration
     */
    public Map<String, Template> getEndpointTemplates() {

        Map<String, Template> definedTemplates = new HashMap<String, Template>();

        synchronized (this) {
            for (Object o : localRegistry.values()) {
                if (o instanceof Template) {
                    Template template = (Template) o;
                    definedTemplates.put(template.getName(), template);
                }
            }
        }
        return definedTemplates;
    }

    public void addAPI(String name, API api) {
        if (!apiTable.containsKey(name)) {
            for (API existingAPI : apiTable.values()) {
                if (api.getVersion().equals(existingAPI.getVersion()) && existingAPI.getContext().equals(api.getContext())) {
                    handleException("URL context: " + api.getContext() + " is already registered" +
                                    " with the API: " + existingAPI.getName());
                }
            }
            apiTable.put(name, api);
        } else {
            handleException("Duplicate resource definition by the name: " + name);
        }
    }

    public void updateAPI(String name, API api) {
        if (!apiTable.containsKey(name)) {
            handleException("No API exists by the name: " + name);
        } else {
            for (API existingAPI : apiTable.values()) {
                if (!api.getName().equals(existingAPI.getName()) && api.getVersion().equals(existingAPI.getVersion()) && existingAPI.getContext().equals(api.getContext())) {
                    handleException("URL context: " + api.getContext() + " is already registered" +
                                    " with the API: " + existingAPI.getName());
                }
            }        	
            apiTable.put(name, api);
        }
    }

    public Collection<API> getAPIs() {
        return Collections.unmodifiableCollection(apiTable.values());
    }

    public API getAPI(String name) {
        return apiTable.get(name);
    }

    public void removeAPI(String name) {
        API api = apiTable.get(name);
        if (api != null) {
            apiTable.remove(name);
        } else {
            handleException("No API exists by the name: " + name);
        }
    }

    /**
     * Return the template specified with the given key
     *
     * @param key the key being referenced for the template
     * @return the template referenced by the key from local/remote registry
     */
    public TemplateMediator getSequenceTemplate(String key) {
        Object o = getEntry(key);
        if (o instanceof TemplateMediator) {
            return (TemplateMediator) o;
        }

        Entry entry = null;
        if (o == null) {
            entry = new Entry(key);
            entry.setType(Entry.REMOTE_ENTRY);
        } else {
            Object object = localRegistry.get(key);
            if (object instanceof Entry) {
                entry = (Entry) object;
            }
        }

        assertEntryNull(entry, key);

        //noinspection ConstantConditions
        if (entry.getMapper() == null) {
            entry.setMapper(new XMLToTemplateMapper());
        }

        if (entry.getType() == Entry.REMOTE_ENTRY) {
            if (registry != null) {
                o = registry.getResource(entry, getProperties());
                if (o != null && o instanceof TemplateMediator) {
                    localRegistry.put(key, entry);
                    return (TemplateMediator) o;
                } else if (o instanceof OMNode) {
                    TemplateMediator m = (TemplateMediator) new TemplateMediatorFactory().createMediator(
                            (OMElement) o, properties);
                    if (m != null) {
                        entry.setValue(m);
                        return m;
                    }
                }
            }
        } else {
            Object value = entry.getValue();
            if (value instanceof OMNode) {
                Object object = entry.getMapper().getObjectFromOMNode(
                        (OMNode) value, getProperties());
                if (object instanceof TemplateMediator) {
                    entry.setValue(object);
                    return (TemplateMediator) object;
                }
            }
        }

        //load from available libraries
        TemplateMediator templateFromLib = LibDeployerUtils.getLibArtifact(synapseLibraries, key, TemplateMediator.class);
        if (templateFromLib != null) {
            return templateFromLib;
        }
        return null;
    }


    /**
     * Gets the mandatory sequence, from the direct reference. This is also available in the
     * {@link SynapseConfiguration#getSequence(String)} but this method improves the
     * performance hence this will be required for all messages
     *
     * @return mandatory sequence direct reference in the local configuration
     */
    public Mediator getMandatorySequence() {
        return mandatorySequence;
    }

    /**
     * Sets the mandatory sequence direct reference
     *
     * @param mandatorySequence to be set as the direct reference
     */
    public void setMandatorySequence(Mediator mandatorySequence) {
        this.mandatorySequence = mandatorySequence;
    }

    /**
     * Return the sequence specified with the given key
     *
     * @param key the key being referenced
     * @return the sequence referenced by the key
     */
    public Mediator getSequence(String key) {

        Object o = getEntry(key);
        if (o instanceof Mediator) {
            return (Mediator) o;
        }

        Entry entry = null;
        if (o == null) {
            entry = new Entry(key);
            entry.setType(Entry.REMOTE_ENTRY);
        } else {
            Object object = localRegistry.get(key);
            if (object instanceof Entry) {
                entry = (Entry) object;
            }
        }

        assertEntryNull(entry, key);

        //noinspection ConstantConditions
        if (entry.getMapper() == null) {
            entry.setMapper(MediatorFactoryFinder.getInstance());
        }

        if (entry.getType() == Entry.REMOTE_ENTRY) {
            if (registry != null) {
                o = registry.getResource(entry, getProperties());
                if (o != null && o instanceof Mediator) {
                    localRegistry.put(key, entry);
                    return (Mediator) o;
                } else if (o instanceof OMNode) {
                    Mediator m = (Mediator) MediatorFactoryFinder.getInstance().
                            getObjectFromOMNode((OMNode) o, properties);
                    if (m != null) {
                        entry.setValue(m);
                        return m;
                    }
                }
            }
        } else {
            Object value = entry.getValue();
            if (value instanceof OMNode) {
                Object object = entry.getMapper().getObjectFromOMNode(
                        (OMNode) value, getProperties());
                if (object instanceof Mediator) {
                    entry.setValue(object);
                    return (Mediator) object;
                }
            }
        }

        return null;
    }


    /**
     * Return the format of the payload factory specified by the given key
     * @param key
     * @return OMElement
     */
    public OMElement getFormat(String key) {


        Object o = localRegistry.get(key);
        if (o != null && o instanceof OMElement) {
            return (OMElement) o;
        }
        Entry entry = new Entry(key);
        entry.setType(Entry.REMOTE_ENTRY);

        assertEntryNull(entry, key);

        //noinspection ConstantConditions
        if (entry.getMapper() == null) {
            entry.setMapper(MediatorFactoryFinder.getInstance());
        }
        if (registry != null) {
            o = registry.getFormat(entry);
            if (o instanceof OMElement) {
                localRegistry.put(key, o);
                return (OMElement) o;
            }
        }

        return null;

    }


    /**
     * Removes a sequence from the local registry
     *
     * @param key of the sequence to be removed
     */
    public synchronized void removeSequence(String key) {
        Object sequence = localRegistry.get(key);
        if (sequence instanceof Mediator) {
            localRegistry.remove(key);
            for (SynapseObserver o : observers) {
                o.sequenceRemoved((Mediator) sequence);
            }
        } else {
            handleException("No sequence exists by the key/name : " + key);
        }
    }

    /**
     * Removes a template from the local registry
     *
     * @param name of the template to be removed
     */
    public synchronized void removeSequenceTemplate(String name) {
        Object sequence = localRegistry.get(name);
        if (sequence instanceof TemplateMediator) {
            localRegistry.remove(name);
            for (SynapseObserver o : observers) {
                o.sequenceTemplateRemoved((Mediator) sequence);
            }
        } else {
            handleException("No template exists by the key/name : " + name);
        }
    }

    /**
     * Return the main/default sequence to be executed. This is the sequence
     * which will execute for all messages when message mediation takes place
     *
     * @return the main mediator sequence
     */
    public Mediator getMainSequence() {
        return getSequence(SynapseConstants.MAIN_SEQUENCE_KEY);
    }

    /**
     * Return the fault sequence to be executed when Synapse encounters a fault
     * scenario during processing
     *
     * @return the fault sequence
     */
    public Mediator getFaultSequence() {
        return getSequence(SynapseConstants.FAULT_SEQUENCE_KEY);
    }

    /**
     * Define a resource to the local registry. All static resources (e.g. URL
     * source) are loaded during this definition phase, and the inability to
     * load such a resource will not allow the definition of the resource to the
     * local registry. If an entry already exists by the specified key a runtime
     * exception is thrown.
     *
     * @param key   the key associated with the resource
     * @param entry the Entry that holds meta information about the resource and
     *              its contents (or cached contents if the Entry refers to a
     *              dynamic resource off a remote registry)
     */
    public synchronized void addEntry(String key, Entry entry) {

        assertAlreadyExists(key, ENTRY);

        if (entry.getType() == Entry.URL_SRC && entry.getValue() == null) {
            try {
                SynapseEnvironment synEnv = SynapseConfigUtils.getSynapseEnvironment(
                        axisConfiguration);
                entry.setValue(SynapseConfigUtils.getOMElementFromURL(entry.getSrc()
                                                                              .toString(), synEnv != null ? synEnv.getServerContextInformation()
                        .getServerConfigurationInformation().getSynapseHome() : ""));
                localRegistry.put(key, entry);
                for (SynapseObserver o : observers) {
                    o.entryAdded(entry);
                }
            } catch (IOException e) {
                handleException("Can not read from source URL : "
                                + entry.getSrc());
            }
        } else {
            localRegistry.put(key, entry);
            for (SynapseObserver o : observers) {
                o.entryAdded(entry);
            }
        }
    }

    public synchronized void updateEntry(String key, Entry entry) {
        if (entry.getType() == Entry.URL_SRC && entry.getValue() == null) {
            try {
                SynapseEnvironment synEnv = SynapseConfigUtils.getSynapseEnvironment(
                        axisConfiguration);
                entry.setValue(SynapseConfigUtils.getOMElementFromURL(entry.getSrc()
                                                                              .toString(), synEnv != null ? synEnv.getServerContextInformation()
                        .getServerConfigurationInformation().getSynapseHome() : ""));
                localRegistry.put(key, entry);
                for (SynapseObserver o : observers) {
                    o.entryAdded(entry);
                }
            } catch (IOException e) {
                handleException("Can not read from source URL : "
                                + entry.getSrc());
            }
        } else {
            localRegistry.put(key, entry);
            for (SynapseObserver o : observers) {
                o.entryAdded(entry);
            }
        }
    }

    /**
     * Gives the set of remote entries that are cached in localRegistry as mapping of entry key
     * to the Entry definition
     *
     * @return Map of locally cached entries
     */
    public Map<String, Entry> getCachedEntries() {

        Map<String, Entry> cachedEntries = new HashMap<String, Entry>();
        synchronized (this) {
            for (Object o : localRegistry.values()) {
                if (o != null && o instanceof Entry) {
                    Entry entry = (Entry) o;
                    if (entry.isDynamic() && entry.isCached()) {
                        cachedEntries.put(entry.getKey(), entry);
                    }
                }
            }
        }

        return cachedEntries;
    }

    /**
     * Returns the map of defined entries in the configuration excluding the
     * fetched entries from remote registry.
     *
     * @return Map of Entries defined in the local configuration
     */
    public Map<String, Entry> getDefinedEntries() {

        Map<String, Entry> definedEntries = new HashMap<String, Entry>();
        synchronized (this) {
            for (Object o : localRegistry.values()) {
                if (o instanceof Entry && ((Entry) o).getType() != Entry.REMOTE_ENTRY) {
                    Entry entry = (Entry) o;
                    definedEntries.put(entry.getKey(), entry);
                }
            }
        }
        return definedEntries;
    }

    /**
     * Get the resource with the given key
     *
     * @param key the key of the resource required
     * @return its value
     */
    public Object getEntry(String key) {
        Object o = localRegistry.get(key);
        if (o != null && o instanceof Entry) {
            Entry entry = (Entry) o;
            if (!entry.isDynamic() || (entry.isCached() && !entry.isExpired())) {
                // If the entry is not dynamic or if it is a cached dynamic entry with the
                // cache still not expired, return the existing value.
                return entry.getValue();
            }

            // This must be a dynamic entry whose cache has expired or which is not cached at all
            // A registry lookup is in order
            if (registry != null) {
                if (entry.isCached()) {
                    try {
                        o = registry.getResource(entry, getProperties());
                    } catch (Exception e) {
                        // Error occurred while loading the resource from the registry
                        // Fall back to the cached value - Do not increase the expiry time
                        log.warn("Error while loading the resource " + key + " from the remote " +
                                 "registry. Previously cached value will be used. Check the " +
                                 "registry accessibility.");
                        return entry.getValue();
                    }
                } else {
                    // Resource not available in the cache - Must load from the registry
                    // No fall backs possible here!!
                    o = registry.getResource(entry, getProperties());
                }
            } else {
                if (entry.isCached()) {
                    // Fall back to the cached value
                    log.warn("The registry is no longer available in the Synapse configuration. " +
                             "Using the previously cached value for the resource : " + key);
                    return entry.getValue();
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Will not  evaluate the value of the remote entry with a key "
                                  + key + ",  because the registry is not available");
                    }
                    return null; // otherwise will return an entry with a value null
                    // (method expects return  a value not an entry )
                }
            }
        }
        return o;
    }

    /**
     * Get the Entry object mapped to the given key
     *
     * @param key the key for which the Entry is required
     * @return its value
     */
    public Entry getEntryDefinition(String key) {
        Object o = localRegistry.get(key);
        if (o == null || o instanceof Entry) {
            if (o == null) {
                // this is not a local definition
                synchronized (this) {
                    o = localRegistry.get(key);
                    if (o == null) {
                        Entry entry = new Entry(key);
                        entry.setType(Entry.REMOTE_ENTRY);
                        addEntry(key, entry);
                        return entry;
                    }
                }
            }
            return (Entry) o;
        } else {
            if (log.isDebugEnabled()) {
                log.debug("There is no local registry entry for key : " + key);
            }
            return null;
        }
    }

    /**
     * Deletes any reference mapped to the given key from the local registry
     *
     * @param key the key of the reference to be removed
     */
    public synchronized void removeEntry(String key) {
        Object entry = localRegistry.get(key);
        if (entry instanceof Entry) {
            localRegistry.remove(key);
            for (SynapseObserver o : observers) {
                o.entryRemoved((Entry) entry);
            }
        } else {
            handleException("No entry exists by the key : " + key);
        }
    }

    /**
     * Clears the cache of the remote entry with the key specified
     *
     * @param key - String key of the entry
     */
    public void clearCachedEntry(String key) {
        Entry entry = getEntryDefinition(key);
        if (entry != null && entry.isDynamic() && entry.isCached()) {
            entry.clearCache();
        }
    }

    /**
     * Clears the cache of all the remote entries which has been
     * cached in the configuration
     */
    public synchronized void clearCache() {

        for (Object o : localRegistry.values()) {
            if (o != null && o instanceof Entry) {
                Entry entry = (Entry) o;
                if (entry.isDynamic() && entry.isCached()) {
                    entry.clearCache();
                }
            }
        }
    }

    /**
     * Define a named endpoint with the given key. If an endpoint already exists by the specified
     * name a runtime exception is thrown.
     *
     * @param key      the key for the endpoint
     * @param endpoint the endpoint definition
     */
    public synchronized void addEndpoint(String key, Endpoint endpoint) {
        assertAlreadyExists(key, ENDPOINT);
        localRegistry.put(key, endpoint);
        for (SynapseObserver o : observers) {
            o.endpointAdded(endpoint);
        }
    }

    public synchronized void updateEndpoint(String key, Endpoint endpoint) {
        localRegistry.put(key, endpoint);
        for (SynapseObserver o : observers) {
            o.endpointAdded(endpoint);
        }
    }

    /**
     * Add a dynamic endpoint definition to the local registry. If an endpoint already exists by
     * the specified name a runtime exception is thrown.
     *
     * @param key   the key for the endpoint definition
     * @param entry the actual endpoint definition to be added
     * @deprecated
     */
    public void addEndpoint(String key, Entry entry) {
        assertAlreadyExists(key, ENTRY);
        localRegistry.put(key, entry);
    }

    /**
     * Returns the map of defined endpoints in the configuration excluding the
     * fetched endpoints from remote registry
     *
     * @return Map of Endpoints defined in the local configuration
     */
    public Map<String, Endpoint> getDefinedEndpoints() {

        Map<String, Endpoint> definedEndpoints = new HashMap<String, Endpoint>();
        synchronized (this) {
            for (Object o : localRegistry.values()) {
                if (o instanceof Endpoint) {
                    Endpoint ep = (Endpoint) o;
                    definedEndpoints.put(ep.getName(), ep);
                }
            }
        }

        return definedEndpoints;
    }

    /**
     * Get the definition of the endpoint with the given key
     *
     * @param key the key of the endpoint
     * @return the endpoint definition
     */
    public Endpoint getEndpoint(String key) {

        Object o = getEntry(key);
        if (o != null && o instanceof Endpoint) {
            return (Endpoint) o;
        }

        Entry entry = null;
        if (o == null) {
            entry = new Entry(key);
            entry.setType(Entry.REMOTE_ENTRY);
        } else {
            Object object = localRegistry.get(key);
            if (object instanceof Entry) {
                entry = (Entry) object;
            }
        }

        assertEntryNull(entry, key);

        //noinspection ConstantConditions
        if (entry.getMapper() == null) {
            entry.setMapper(XMLToEndpointMapper.getInstance());
        }

        if (entry.getType() == Entry.REMOTE_ENTRY) {
            if (registry != null) {
                o = registry.getResource(entry, getProperties());
                if (o != null && o instanceof Endpoint) {
                    localRegistry.put(key, entry);
                    return (Endpoint) o;
                } else if (o instanceof OMNode) {
                    Endpoint e = (Endpoint) XMLToEndpointMapper.getInstance().
                            getObjectFromOMNode((OMNode) o, properties);
                    if (e != null) {
                        entry.setValue(e);
                        return e;
                    }
                }
            }
        } else {
            Object value = entry.getValue();
            if (value instanceof OMNode) {
                Object object = entry.getMapper().getObjectFromOMNode(
                        (OMNode) value, getProperties());
                if (object instanceof Endpoint) {
                    entry.setValue(object);
                    return (Endpoint) object;
                }
            }
        }
        return null;
    }

    /**
     * Deletes the endpoint with the given key. If an endpoint does not exist by the specified
     * key a runtime exception is thrown.
     *
     * @param key of the endpoint to be deleted
     */
    public synchronized void removeEndpoint(String key) {
        Object endpoint = localRegistry.get(key);
        if (endpoint instanceof Endpoint) {
            localRegistry.remove(key);
            for (SynapseObserver o : observers) {
                o.endpointRemoved((Endpoint) endpoint);
            }
        } else {
            handleException("No endpoint exists by the key/name : " + key);
        }
    }

    /**
     * Add a Proxy service to the configuration. If a proxy service already exists by the
     * specified name a runtime exception is thrown.
     *
     * @param name  the name of the Proxy service
     * @param proxy the Proxy service instance
     */
    public void addProxyService(String name, ProxyService proxy) {
        synchronized (this.axisConfiguration) {
            if (!proxyServices.containsKey(name)) {
                proxyServices.put(name, proxy);
                for (SynapseObserver o : observers) {
                    o.proxyServiceAdded(proxy);
                }
            } else {
                handleException("Duplicate proxy service by the name : " + name);
            }
        }
    }

    /**
     * Get the Proxy service with the given name
     *
     * @param name the name being looked up
     * @return the Proxy service
     */
    public ProxyService getProxyService(String name) {
        return proxyServices.get(name);
    }

    /**
     * Deletes the Proxy Service named with the given name. If a proxy service does not exist by
     * the specified name a runtime exception is thrown.
     *
     * @param name of the Proxy Service to be deleted
     */
    public void removeProxyService(String name) {
        synchronized (this.axisConfiguration) {
            ProxyService proxy = proxyServices.get(name);
            if (proxy == null) {
                handleException("Unknown proxy service for name : " + name);
            } else {
                try {
                    if (getAxisConfiguration().getServiceForActivation(name) != null) {
                        if (getAxisConfiguration().getServiceForActivation(name)
                                .isActive()) {
                            getAxisConfiguration().getService(name)
                                    .setActive(false);
                        }
                        getAxisConfiguration().removeService(name);
                    }
                    proxyServices.remove(name);
                    for (SynapseObserver o : observers) {
                        o.proxyServiceRemoved(proxy);
                    }
                } catch (AxisFault axisFault) {
                    handleException(axisFault.getMessage());
                }
            }
        }
    }

    /**
     * Return the list of defined proxy services
     *
     * @return the proxy services defined
     */
    public Collection<ProxyService> getProxyServices() {
        return Collections.unmodifiableCollection(proxyServices.values());
    }

    /**
     * Return an unmodifiable copy of the local registry
     *
     * @return an unmodifiable copy of the local registry
     */
    public Map getLocalRegistry() {
        return localRegistry;
    }

    /**
     * Get the remote registry defined (if any)
     *
     * @return the currently defined remote registry
     */
    public Registry getRegistry() {
        return registry;
    }

    /**
     * Set the remote registry for the configuration
     *
     * @param registry the remote registry for the configuration
     */
    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    /**
     * Set the Axis2 AxisConfiguration to the SynapseConfiguration
     *
     * @param axisConfig AxisConfiguration to be set
     */
    public void setAxisConfiguration(AxisConfiguration axisConfig) {
        this.axisConfiguration = axisConfig;
    }

    /**
     * Get the Axis2 AxisConfiguration for the SynapseConfiguration
     *
     * @return AxisConfiguration of the Axis2
     */
    public AxisConfiguration getAxisConfiguration() {
        return axisConfiguration;
    }

    /**
     * The path to the currently loaded configuration file
     *
     * @return file path to synapse.xml
     */
    public String getPathToConfigFile() {
        return pathToConfigFile;
    }

    /**
     * Set the path to the loaded synapse.xml
     *
     * @param pathToConfigFile path to the synapse.xml loaded
     */
    public void setPathToConfigFile(String pathToConfigFile) {
        this.pathToConfigFile = pathToConfigFile;
    }

    /**
     * Set the default QName of the Synapse Configuration
     *
     * @param defaultQName QName specifying the default QName of the configuration
     */
    public void setDefaultQName(QName defaultQName) {
        this.defaultQName = defaultQName;
    }

    /**
     * Get the default QName of the configuration.
     *
     * @return default QName of the configuration
     */
    public QName getDefaultQName() {
        return defaultQName;
    }

    /**
     * Get the timer object for the Synapse Configuration
     *
     * @return synapseTimer timer object of the configuration
     */
    public Timer getSynapseTimer() {
        return synapseTimer;
    }

    /**
     * Get the startup collection in the configuration
     *
     * @return collection of startup objects registered
     */
    public Collection<Startup> getStartups() {
        return startups.values();
    }

    /**
     * Get the Startup with the specified name
     *
     * @param id - String name of the startup to be retrieved
     * @return Startup object with the specified name or null
     */
    public Startup getStartup(String id) {
        return startups.get(id);
    }

    /**
     * Add a startup to the startups map in the configuration. If a startup already exists by the
     * specified name a runtime exception is thrown.
     *
     * @param startup - Startup object to be added
     */
    public synchronized void addStartup(Startup startup) {
        if (!startups.containsKey(startup.getName())) {
            startups.put(startup.getName(), startup);
            for (SynapseObserver o : observers) {
                o.startupAdded(startup);
            }
        } else {
            handleException("Duplicate startup by the name : " + startup.getName());
        }
    }

    public synchronized void updateStartup(Startup startup) {
        startups.put(startup.getName(), startup);
        for (SynapseObserver o : observers) {
            o.startupAdded(startup);
        }
    }

    /**
     * Removes the startup specified by the name. If no startup exists by the specified name a
     * runtime exception is thrown.
     *
     * @param name - name of the startup that needs to be removed
     */
    public synchronized void removeStartup(String name) {
        Startup startup = startups.get(name);
        if (startup != null) {
            startups.remove(name);
            for (SynapseObserver o : observers) {
                o.startupRemoved(startup);
            }
        } else {
            handleException("No startup exists by the name : " + name);
        }
    }

    /**
     * Gets the properties to configure the Synapse environment.
     *
     * @return set of properties as Properties
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * Sets the specified property to the Synapse configuration
     *
     * @param key   Name of the property
     * @param value Value of the property to be set
     */
    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }

    /**
     * Sets the properties to configure the Synapse environment.
     *
     * @param properties - Properties which needs to be set
     * @deprecated
     */
    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    /**
     * Gets the String representation of the property value if there is a property for the
     * given propKey or returns the default value passed
     *
     * @param propKey - key for the property lookup
     * @param def     - default value
     * @return String representation of the property value with the given key or the def value
     */
    public String getProperty(String propKey, String def) {
        String val = System.getProperty(propKey);
        if (val == null) {
            val = properties.getProperty(propKey);
        }

        if (val != null) {
            if (log.isDebugEnabled()) {
                log.debug("Using synapse tuning parameter : " + propKey + " = " + val);
            }
            return val;
        }
        return def;
    }

    /**
     * Gets the String representation of the property value if there is a property for the
     * given propKey or returns the default value passed
     *
     * @param propKey - key for the property lookup
     * @param def     - default value
     * @return String representation of the property value with the given key or the def value
     */
    public long getProperty(String propKey, long def) {
        String val = System.getProperty(propKey);
        if (val == null) {
            val = properties.getProperty(propKey);
        }

        if (val != null) {
            if (log.isDebugEnabled()) {
                log.debug("Using synapse tuning parameter : " + propKey + " = " + val);
            }
            return Long.valueOf(val);
        }
        return def;
    }

    /**
     * Gets the property value if the property specified by the propKey is there or null else
     *
     * @param propKey - key for the property lookup
     * @return String representation of the property value if found or null else
     */
    public String getProperty(String propKey) {
        String val = System.getProperty(propKey);
        if (val == null) {
            val = properties.getProperty(propKey);
        }

        if (val != null) {
            if (log.isDebugEnabled()) {
                log.debug("Using synapse tuning parameter : " + propKey + " = " + val);
            }
            return val;
        }
        return null;
    }

    /**
     * This method will be called on the soft shutdown or destroying the configuration
     * and will destroy all the stateful managed parts of the configuration.
     */
    public synchronized void destroy() {

        if (log.isDebugEnabled()) {
            log.debug("Destroying the Synapse Configuration");
        }

        // clear the timer tasks of Synapse
        synapseTimer.cancel();
        synapseTimer = null;

        // stop and shutdown all the proxy services
        for (ProxyService p : getProxyServices()) {

            if (p.getTargetInLineInSequence() != null) {
                p.getTargetInLineInSequence().destroy();
            }

            if (p.getTargetInLineOutSequence() != null) {
                p.getTargetInLineOutSequence().destroy();
            }
        }

        // destroy the managed mediators
        for (ManagedLifecycle seq : getDefinedSequences().values()) {
            seq.destroy();
        }

        //destroy sequence templates
        for (TemplateMediator seqTemplate : getSequenceTemplates().values()) {
            seqTemplate.destroy();
        }

        // destroy the managed endpoints
        for (Endpoint endpoint : getDefinedEndpoints().values()) {
            endpoint.destroy();
        }

        // destroy the startups
        for (ManagedLifecycle stp : startups.values()) {
            stp.destroy();
        }

        // clear session information used for SA load balancing
        try {
            SALSessions.getInstance().reset();
            DataSourceRepositoryHolder.getInstance().getDataSourceRepositoryManager().clear();
        } catch (Throwable ignored) {
        }

        // destroy the priority executors. 
        for (PriorityExecutor pe : executors.values()) {
            pe.destroy();
        }

        // destroy the Message Stores
        for (MessageStore ms : messageStores.values()) {
            ms.destroy();
        }

        // destroy the Message processors
        for (MessageProcessor mp : messageProcessors.values()) {
            mp.destroy();
        }

        for (API api : apiTable.values()) {
            api.destroy();
        }
    }

    /**
     * This method will be called in the startup of Synapse or in an initiation
     * and will initialize all the managed parts of the Synapse Configuration
     *
     * @param se SynapseEnvironment specifying the env to be initialized
     */
    public synchronized void init(SynapseEnvironment se) {

        if (log.isDebugEnabled()) {
            log.debug("Initializing the Synapse Configuration using the SynapseEnvironment");
        }

        // initialize registry
        if (registry != null && registry instanceof ManagedLifecycle) {
            ((ManagedLifecycle) registry).init(se);
        }

        //we initialize xpath extensions here since synapse environment is available
        initXpathExtensions(se);

        initCarbonTenantConfigurator(se);

        //initialize endpoints
        for (Endpoint endpoint : getDefinedEndpoints().values()) {
			try {
				endpoint.init(se);
			} catch (Exception e) {
				log.error(" Error in initializing endpoint ["
						+ endpoint.getName() + "] " + e.getMessage());
			}
        }

        //initialize sequence templates
        for (TemplateMediator seqTemplate : getSequenceTemplates().values()) {
			try {
				seqTemplate.init(se);
			} catch (Exception e) {
				log.error(" Error in initializing Sequence Template ["
						+ seqTemplate.getName() + "] " + e.getMessage());
			}
        }

        // initialize managed mediators
        for (ManagedLifecycle seq : getDefinedSequences().values()) {
            if (seq != null) {
				try {
					seq.init(se);
				} catch (Exception e) {
					log.error(" Error in initializing Sequence "
							+ e.getMessage());
				}
            }
        }

        // initialize all the proxy services
        for (ProxyService proxy : getProxyServices()) {
			try {
				if (proxy.getTargetInLineEndpoint() != null) {
					proxy.getTargetInLineEndpoint().init(se);
				}

				if (proxy.getTargetInLineInSequence() != null) {
					proxy.getTargetInLineInSequence().init(se);
				}

				if (proxy.getTargetInLineOutSequence() != null) {
					proxy.getTargetInLineOutSequence().init(se);
				}

				if (proxy.getTargetInLineFaultSequence() != null) {
					proxy.getTargetInLineFaultSequence().init(se);
				}
			} catch (Exception e) {
				log.error(" Error in initializing Proxy Service [ "
						+ proxy.getName() + "] " + e.getMessage());
			}
        }

        // initialize the startups
        for (ManagedLifecycle stp : getStartups()) {
            if (stp != null) {
				try {
					stp.init(se);
				} catch (Exception e) {
					log.error(" Error in initializing Stratups "
							+ e.getMessage());
				}
            }
        }

        // initialize sequence executors
        for (PriorityExecutor executor : getPriorityExecutors().values()) {
			try {
				executor.init();
			} catch (Exception e) {
				log.error(" Error in initializing Executor [ "
						+ executor.getName() + "] " + e.getMessage());
			}
        }

        //initialize message stores
        for(MessageStore messageStore : messageStores.values()) {
			try {
				messageStore.init(se);
			} catch (Exception e) {
				log.error(" Error in initializing Message Store [ "
						+ messageStore.getName() + "] " + e.getMessage());
			}
        }

        // initialize message processors
        for(MessageProcessor messageProcessor : messageProcessors.values()) {
			try {
				messageProcessor.init(se);
			} catch (Exception e) {
				log.error(" Error in initializing Message Processor [ "
						+ messageProcessor.getName() + "] " + e.getMessage());
			}
        }

        for (API api : apiTable.values()) {
			try {
				api.init(se);
			} catch (Exception e) {
				log.error(" Error in initializing API [ " + api.getName()
						+ "] " + e.getMessage());
			}
        }
        initImportedLibraries(se);
    }

    private void handleException(String msg) {
        log.error(msg);
        throw new SynapseException(msg);
    }

    /**
     * Add an event source to the configuration. If an event source already exists by the
     * specified name a runtime exception is thrown.
     *
     * @param name        name of the event source
     * @param eventSource the event source to be added
     */
    public synchronized void addEventSource(String name, SynapseEventSource eventSource) {
        if (!eventSources.containsKey(name)) {
            eventSources.put(name, eventSource);
            for (SynapseObserver o : observers) {
                o.eventSourceAdded(eventSource);
            }
        } else {
            handleException("Duplicate event source by the name : " + name);
        }
    }

    public SynapseEventSource getEventSource(String name) {
        return eventSources.get(name);
    }

    /**
     * Remove an event source from the configuration. If the specified event source does not
     * exist a runtime exception is thrown.
     *
     * @param name name of the event source to be removed
     */
    public synchronized void removeEventSource(String name) {
        SynapseEventSource eventSource = eventSources.get(name);
        if (eventSource == null) {
            handleException("No event source exists by the name : " + name);
        } else {
            try {
                if (getAxisConfiguration().getServiceForActivation(name) != null) {
                    if (getAxisConfiguration().getServiceForActivation(name)
                            .isActive()) {
                        getAxisConfiguration().getService(name)
                                .setActive(false);
                    }
                    getAxisConfiguration().removeService(name);
                }
                eventSources.remove(name);
                for (SynapseObserver o : observers) {
                    o.eventSourceRemoved(eventSource);
                }
            } catch (AxisFault axisFault) {
                handleException(axisFault.getMessage());
            }
        }
    }

    public Collection<SynapseEventSource> getEventSources() {
        return eventSources.values();
    }

    public void setEventSources(Map<String, SynapseEventSource> eventSources) {
        this.eventSources = eventSources;
    }

    public void registerObserver(SynapseObserver o) {
        if (!observers.contains(o)) {
            observers.add(o);
        }
    }

    public List<SynapseObserver> getObservers() {
        return Collections.unmodifiableList(observers);
    }

    /**
     * Add an executor
     *
     * @param name     name of the executor
     * @param executor executor
     */
    public synchronized void addPriorityExecutor(String name, PriorityExecutor executor) {
        if (!executors.containsKey(name)) {
            executors.put(name, executor);
            for (SynapseObserver o : observers) {
                o.priorityExecutorAdded(executor);
            }
        } else {
            handleException("Duplicate priority executor by the name: " + name);
        }
    }

    public synchronized void updatePriorityExecutor(String name, PriorityExecutor executor) {
        executors.put(name, executor);
        for (SynapseObserver o : observers) {
            o.priorityExecutorAdded(executor);
        }
    }

    /**
     * Get the executors map
     *
     * @return executors map, stored as name of executor and executor
     */
    public Map<String, PriorityExecutor> getPriorityExecutors() {
        return executors;
    }

    /**
     * Removes an executor from the configuration
     *
     * @param name name of the executor
     * @return removed executor
     */
    public synchronized PriorityExecutor removeExecutor(String name) {
        PriorityExecutor executor = executors.remove(name);
        if (executor != null) {
            for (SynapseObserver o : observers) {
                o.priorityExecutorRemoved(executor);
            }
        }
        return executor;
    }

    /**
     * Get the Message store for the configuration with a given name.
     *
     * @param name Name of the message store
     * @return a MessageStore instance or null
     */
    public MessageStore getMessageStore(String name) {
        return messageStores.get(name) ;
    }

    /**
     * Add MessageStore to the configuration with a given name.
     *
     * @param name Name of the message store
     * @param messageStore a MessageStore instance
     */
    public void addMessageStore(String name, MessageStore messageStore) {
        if (!messageStores.containsKey(name)) {
            messageStores.put(name, messageStore);
            Set<String> processors = messageProcessors.keySet();
            for (String processorName : processors) {
                if (messageProcessors.get(processorName).getMessageStoreName().equals(name)) {
                    (messageProcessors.get(processorName)).start();
                }
            }
        } else {
            handleException("Duplicate message store : " + name);
        }
    }

    /**
     * Get Message stores defined
     * @return  message store map stored as name of the message store and message store
     */
    public Map<String, MessageStore> getMessageStores() {
        return messageStores;
    }

    /**
     * Removes a Message store from the configuration
     *
     * @param name name of the message store
     * @return The message store with the specified name
     */
    public MessageStore removeMessageStore(String name) {
        return messageStores.remove(name);
    }

    /**
     * Add message processor to the synapse configuration with given name
     * @param name of the Message processor
     * @param processor instance
     */
    public void addMessageProcessor(String name , MessageProcessor processor) {
        if(!(messageProcessors.containsKey(name))) {
            messageProcessors.put(name , processor);
        } else {
            handleException("Duplicate Message Processor " + name);
        }
    }

    /**
     * Get all Message processors in the Synapse configuration
     * @return Return Map that contains all the message processors
     */
    public Map<String, MessageProcessor> getMessageProcessors() {
        return messageProcessors;
    }

    /**
     * remove the message processor from the synapse configuration
     * @param name  of the message
     * @return  Removed Message processor instance
     */
    public MessageProcessor removeMessageProcessor(String name) {
        return messageProcessors.remove(name);
    }

    /**
     * Add Synapse library to configuration with given name
     *
     * @param name      of synapse lib
     * @param library instance
     */
    public void addSynapseLibrary(String name, Library library) {
        if (!(synapseLibraries.containsKey(name))) {
            synapseLibraries.put(name, library);
        } else {
            handleException("Duplicate Synapse Library " + name);
        }
    }

    /**
     * Get all Synapse libraries in the Synapse configuration
     *
     * @return Return Map that contains all the Synapse libraries
     */
    public Map<String, Library> getSynapseLibraries() {
        return synapseLibraries;
    }

    /**
     * remove the Synapse library from the synapse configuration
     *
     * @param name of the lib
     * @return Removed Synapse library instance
     */
    public Library removeSynapseLibrary(String name) {
        return synapseLibraries.remove(name);
    }



    /**
     * Add Synapse Import to a configuration with given name
     *
     * @param name      of synapse lib
     * @param synImport instance
     */
    public void addSynapseImport(String name, SynapseImport synImport) {
    	 synapseImports.put(name, synImport);
    }

    /**
     * Get all Synapse libraries in the Synapse configuration
     *
     * @return Return Map that contains all the Synapse libraries
     */
    public Map<String, SynapseImport> getSynapseImports() {
        return synapseImports;
    }

    /**
     * remove the Synapse library from the synapse configuration
     *
     * @param name of the lib
     * @return Removed Synapse library instance
     */
    public SynapseImport removeSynapseImport(String name) {
        return synapseImports.remove(name);
    }


    /**
     * Sets the description of the configuration
     *
     * @param description tobe set to the artifact
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the configuration description
     *
     * @return description of the configuration
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get the SynapseArtifactDeploymentStore which is used to store the information about
     * the deployed artifacts
     *
     * @return the SynapseArtifactDeploymentStore for this configuration
     */
    public SynapseArtifactDeploymentStore getArtifactDeploymentStore() {
        return artifactDeploymentStore;
    }
    
    
    
	/**
	 * Returns the map which contains the Decrypted values read via the secure
	 * vault provider
	 * 
	 * @return
	 */
    public Map<String, Object> getDecryptedCacheMap() {
		return decryptedCacheMap;
	}
    

	private void assertAlreadyExists(String key, String type) {

        if (key == null || "".equals(key)) {
            handleException("Given entry key is empty or null.");
        }

        //noinspection ConstantConditions
        if (localRegistry.containsKey(key.trim())) {
            handleException("Duplicate " + type + " definition for key : " + key);
        }
    }

    private void assertEntryNull(Entry entry, String key) {
        if (entry == null) {
            handleException("Cannot locate an either local or remote entry for key : " + key);
        }
    }

    public void addEndpointTemplate(String name, Template template) {
        assertAlreadyExists(name, "template");
        localRegistry.put(name, template);
    }

    public void updateEndpointTemplate(String name, Template template) {
        localRegistry.put(name, template);
    }

    public void removeEndpointTemplate(String name) {
        Object sequence = localRegistry.get(name);
        if (sequence instanceof Template) {
            localRegistry.remove(name);
        } else {
            handleException("No template exists by the key/name : " + name);
        }
    }

    public Template getEndpointTemplate(String key) {
        Object o = getEntry(key);
        if (o instanceof Template) {
            return (Template) o;
        }

        Entry entry = null;
        if (o == null) {
            entry = new Entry(key);
            entry.setType(Entry.REMOTE_ENTRY);
        } else {
            Object object = localRegistry.get(key);
            if (object instanceof Entry) {
                entry = (Entry) object;
            }
        }

        assertEntryNull(entry, key);

        //noinspection ConstantConditions
        if (entry.getMapper() == null) {
            entry.setMapper(new XMLToTemplateMapper());
        }

        if (entry.getType() == Entry.REMOTE_ENTRY) {
            if (registry != null) {
                o = registry.getResource(entry, getProperties());
                if (o != null && o instanceof Template) {
                    localRegistry.put(key, entry);
                    return (Template) o;
                } else if (o instanceof OMNode) {
                    Template m = new TemplateFactory().createEndpointTemplate(
                            (OMElement) o, properties);
                    if (m != null) {
                        entry.setValue(m);
                        return m;
                    }
                }
            }
        } else {
            Object value = entry.getValue();
            if (value instanceof OMNode) {
                Object object = entry.getMapper().getObjectFromOMNode(
                        (OMNode) value, getProperties());
                if (object instanceof Template) {
                    entry.setValue(object);
                    return (Template) object;
                }
            }
        }
         //load from available libraries
        Template templateFromLib = LibDeployerUtils.getLibArtifact(synapseLibraries, key, Template.class);
        if (templateFromLib != null) {
            return templateFromLib;
        }

        return null;
    }
    
    public Mediator getDefaultConfiguration(String key) {
    	Object o = getEntry(key);
        if (o instanceof Mediator) {
            return (Mediator) o;
        }

        Entry entry = null;
        if (o == null) {
            entry = new Entry(key);
            entry.setType(Entry.REMOTE_ENTRY);
        } else {
            Object object = localRegistry.get(key);
            if (object instanceof Entry) {
                entry = (Entry) object;
            }
        }

        assertEntryNull(entry, key);

        //noinspection ConstantConditions
        if (entry.getMapper() == null) {
            entry.setMapper(MediatorFactoryFinder.getInstance());
        }

        if (entry.getType() == Entry.REMOTE_ENTRY) {
            if (registry != null) {
                o = registry.getResource(entry, getProperties());
                if (o != null && o instanceof Mediator) {
                    localRegistry.put(key, entry);
                    return (Mediator) o;
                } else if (o instanceof OMNode) {
                    Mediator m = (Mediator) MediatorFactoryFinder.getInstance().
                            getObjectFromOMNode((OMNode) o, properties);
                    if (m != null) {
                        entry.setValue(m);
                        return m;
                    }
                }
            }
        } else {
            Object value = entry.getValue();
            if (value instanceof OMNode) {
                Object object = entry.getMapper().getObjectFromOMNode(
                        (OMNode) value, getProperties());
                if (object instanceof Mediator) {
                    return (Mediator) object;
                }
            }
        }

        return null;
    }
    

    public boolean isAllowHotUpdate() {
        return allowHotUpdate;
    }

    public void setAllowHotUpdate(boolean allowHotUpdate) {
        this.allowHotUpdate = allowHotUpdate;
    }

    /**
     * This method initializes Xpath Extensions available through synapse.properties file
     * Xpath Extensions can be defined in Variable Context Extensions + Function Context Extensions
     * synapse.xpath.var.extensions --> Variable Extensions
     * synapse.xpath.func.extensions --> Function Extensions
     *
     * @param synapseEnvironment SynapseEnvironment
     */
    private void initXpathExtensions(SynapseEnvironment synapseEnvironment) {
        Axis2SynapseEnvironment axis2SynapseEnvironment = (Axis2SynapseEnvironment) synapseEnvironment;

        /*Initialize Function Context extensions for xpath
        */
        List<SynapseXpathFunctionContextProvider> functionExtensions =
                XpathExtensionUtil.getRegisteredFunctionExtensions();
        for (SynapseXpathFunctionContextProvider functionExtension : functionExtensions) {
            axis2SynapseEnvironment.setXpathFunctionExtensions(functionExtension);
        }

        /*Initialize Variable Context extensions for xpath
        */
        List<SynapseXpathVariableResolver> variableExtensions =
                XpathExtensionUtil.getRegisteredVariableExtensions();
        for (SynapseXpathVariableResolver variableExtension : variableExtensions) {
            axis2SynapseEnvironment.setXpathVariableExtensions(variableExtension);
        }

    }

    /**
     *
     * @param se
     */
    private void initCarbonTenantConfigurator(SynapseEnvironment se) {
        Axis2SynapseEnvironment axis2SynapseEnvironment = (Axis2SynapseEnvironment) se;
        TenantInfoConfigurator configurator = TenantInfoConfigProvider.getConfigurator();
        axis2SynapseEnvironment.setTenantInfoConfigurator(configurator);
    }


    private void initImportedLibraries(SynapseEnvironment synapseEnvironment) {
        for (String importKey : synapseImports.keySet()) {
            Library lib = synapseLibraries.get(importKey);
            if(lib==null){
                log.error("Unable to deploy synapse import:" + importKey +". Required library not found.");
                continue;
            }
            for (String artifactKey : lib.getArtifacts().keySet()) {
                if (lib.getArtifacts().get(artifactKey) instanceof TemplateMediator) {
                    ((TemplateMediator) lib.getArtifacts().get(artifactKey)).init(synapseEnvironment);
                }
            }

        }
    }




}

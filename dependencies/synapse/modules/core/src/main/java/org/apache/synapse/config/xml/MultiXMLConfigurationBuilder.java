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

package org.apache.synapse.config.xml;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.Mediator;
import org.apache.synapse.Startup;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.endpoints.Template;
import org.apache.synapse.libraries.imports.SynapseImport;
import org.apache.synapse.mediators.template.TemplateMediator;
import org.apache.synapse.SynapseException;
import org.apache.synapse.message.processor.MessageProcessor;
import org.apache.synapse.message.store.MessageStore;
import org.apache.synapse.commons.executors.PriorityExecutor;
import org.apache.synapse.config.Entry;
import org.apache.synapse.config.SynapseConfigUtils;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.axis2.ProxyService;
import org.apache.synapse.endpoints.Endpoint;
import org.apache.synapse.eventing.SynapseEventSource;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.apache.synapse.rest.API;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.util.Iterator;
import java.util.Properties;

/**
 * <p>
 * This optional configuration builder creates the Synapse configuration by processing
 * a specified file hierarchy. If the root of the specified file hierarchy is CONFIG_HOME,
 * then the following directories are expected to be in CONFIG_HOME.
 * <ul>
 *  <li>CONFIG_HOME/proxy-services</li>
 *  <li>CONFIG_HOME/sequences</li>
 *  <li>CONFIG_HOME/endpoints</li>
 *  <li>CONFIG_HOME/local-entries</li>
 *  <li>CONFIG_HOME/tasks</li>
 *  <li>CONFIG_HOME/event-sources</li>
 *  <li>CONFIG_HOME/priority-executors</li>
 * </ul>
 *
 * Each of these directories will house a set of XML files. Each file will define exactly
 * one configuration item (eg: a proxy service, an endpoint, a sequence).
 * </p>
 * <p>
 * In addition to the directories mentioned above one can have the following file in
 * CONFIG_HOME
 * <ul>
 *  <li>CONFIG_HOME/registry.xml</li>
 * </ul>
 * </p>
 *
 */
public class MultiXMLConfigurationBuilder {

    public static final String PROXY_SERVICES_DIR       = "proxy-services";
    public static final String SEQUENCES_DIR            = "sequences";
    public static final String TEMPLATES_DIR            = "templates";
    public static final String ENDPOINTS_DIR            = "endpoints";
    public static final String LOCAL_ENTRY_DIR          = "local-entries";
    public static final String TASKS_DIR                = "tasks";
    public static final String EVENTS_DIR               = "event-sources";
    public static final String EXECUTORS_DIR            = "priority-executors";
    public static final String MESSAGE_STORE_DIR        = "message-stores";
    public static final String MESSAGE_PROCESSOR_DIR    = "message-processors";
    public static final String REST_API_DIR             = "api";
    public static final String SYNAPSE_IMPORTS_DIR   = "imports";

    public static final String REGISTRY_FILE       = "registry.xml";

    public static final String SEPARATE_REGISTRY_DEFINITION = "__separateRegDef";

    private static final String[] extensions = { "xml" };

    private static Log log = LogFactory.getLog(MultiXMLConfigurationBuilder.class);

    public static SynapseConfiguration getConfiguration(String root, Properties properties) {

        log.info("Building synapse configuration from the synapse artifact repository at : " + root);

        // First try to load the configuration from synapse.xml
        SynapseConfiguration synapseConfig = createConfigurationFromSynapseXML(root, properties);
        if (synapseConfig == null) {
            synapseConfig = SynapseConfigUtils.newConfiguration();
            synapseConfig.setDefaultQName(XMLConfigConstants.DEFINITIONS_ELT);
        } else if (log.isDebugEnabled()) {
            log.debug("Found a synapse configuration in the " + SynapseConstants.SYNAPSE_XML
                    + " file at the artifact repository root, which gets precedence "
                    + "over other definitions");
        }

        if (synapseConfig.getRegistry() == null) {
            // If the synapse.xml does not define a registry look for a registry.xml
            createRegistry(synapseConfig, root, properties);
        } else if (log.isDebugEnabled()) {
            log.debug("Using the registry defined in the " + SynapseConstants.SYNAPSE_XML
                    + " as the registry, any definitions in the "+ REGISTRY_FILE +
                    " will be neglected");
        }

        createSynapseImports(synapseConfig, root, properties);

        createLocalEntries(synapseConfig, root, properties);
        createEndpoints(synapseConfig, root, properties);
        createSequences(synapseConfig, root, properties);
        createTemplates(synapseConfig, root, properties);
        createProxyServices(synapseConfig, root, properties);
        createTasks(synapseConfig, root, properties);
        createEventSources(synapseConfig, root, properties);
        createExecutors(synapseConfig, root, properties);
        createMessageStores(synapseConfig, root, properties);
        createMessageProcessors(synapseConfig, root, properties);

        createAPIs(synapseConfig, root, properties);

        return synapseConfig;
    }

    private static SynapseConfiguration createConfigurationFromSynapseXML(
            String rootDirPath, Properties properties) {

        File synapseXML = new File(rootDirPath, SynapseConstants.SYNAPSE_XML);
        if (!synapseXML.exists() || !synapseXML.isFile()) {
            return null;
        }

        FileInputStream is;
        SynapseConfiguration config = null;
        try {
            is = FileUtils.openInputStream(synapseXML);
        } catch (IOException e) {
            handleException("Error while opening the file: " + synapseXML.getName(), e);
            return null;
        }

        try {
            config = XMLConfigurationBuilder.getConfiguration(is, properties);
            is.close();
        } catch (XMLStreamException e) {
            handleException("Error while loading the Synapse configuration from the " +
                    synapseXML.getName() + " file", e);
        } catch (IOException e) {
            log.warn("Error while closing the input stream from file: " + synapseXML.getName(), e);
        }

        return config;
    }

    private static void createRegistry(SynapseConfiguration synapseConfig, String rootDirPath,
                                       Properties properties) {

        File registryDef = new File(rootDirPath, REGISTRY_FILE);
        try {
            if (registryDef.exists() && registryDef.isFile()) {
                if (log.isDebugEnabled()) {
                    log.debug("Initializing Synapse registry from the configuration at : " +
                            registryDef.getPath());
                }
                OMElement document = getOMElement(registryDef);
                SynapseXMLConfigurationFactory.defineRegistry(synapseConfig, document, properties);
                synapseConfig.setProperty(SEPARATE_REGISTRY_DEFINITION,
                        String.valueOf(Boolean.TRUE));
            }
        } catch (Exception e) {
            String msg = "Registry configuration cannot be built from : " + registryDef.getName();
            handleConfigurationError(SynapseConstants.FAIL_SAFE_MODE_REGISTRY, msg, e);
        }
    }

    private static void createLocalEntries(SynapseConfiguration synapseConfig, String rootDirPath,
                                           Properties properties) {

        File localEntriesDir = new File(rootDirPath, LOCAL_ENTRY_DIR);
        if (localEntriesDir.exists()) {
            if (log.isDebugEnabled()) {
                log.debug("Loading local entry definitions from : " + localEntriesDir.getPath());
            }

            Iterator entryDefinitions = FileUtils.iterateFiles(localEntriesDir, extensions, false);
            while (entryDefinitions.hasNext()) {
                File file = (File) entryDefinitions.next();
                try {
                    OMElement document = getOMElement(file);
                    Entry entry = SynapseXMLConfigurationFactory.defineEntry(synapseConfig, document,
                            properties);
                    if (entry != null) {
                        entry.setFileName(file.getName());
                        synapseConfig.getArtifactDeploymentStore().addArtifact(file.getAbsolutePath(),
                                entry.getKey());
                    }
                } catch (Exception e) {
                    String msg = "Local Entry configuration cannot be built from : " + file.getName();
                    handleConfigurationError(SynapseConstants.FAIL_SAFE_MODE_LOCALENTRIES, msg, e);
                }
             }
        }
    }

    private static void createProxyServices(SynapseConfiguration synapseConfig, String rootDirPath,
                                            Properties properties) {

        File proxyServicesDir = new File(rootDirPath, PROXY_SERVICES_DIR);
        if (proxyServicesDir.exists()) {
            if (log.isDebugEnabled()) {
                log.debug("Loading proxy services from : " + proxyServicesDir.getPath());
            }

            Iterator proxyDefinitions = FileUtils.iterateFiles(proxyServicesDir, extensions, false);

            while (proxyDefinitions.hasNext()) {
                File file = (File) proxyDefinitions.next();
                try {
                    OMElement document = getOMElement(file);
                    ProxyService proxy = SynapseXMLConfigurationFactory.defineProxy(synapseConfig,
                            document, properties);
                    if (proxy != null) {
                        proxy.setFileName(file.getName());
                        synapseConfig.getArtifactDeploymentStore().addArtifact(
                                file.getAbsolutePath(), proxy.getName());
                    }
                } catch (Exception e) {
                    String msg = "Proxy configuration cannot be built from : " + file.getName();
                    handleConfigurationError(SynapseConstants.FAIL_SAFE_MODE_PROXY_SERVICES, msg, e);
                }
            }
        }
    }

    private static void createTasks(SynapseConfiguration synapseConfig, String rootDirPath,
                                    Properties properties) {

        File tasksDir = new File(rootDirPath, TASKS_DIR);
        if (tasksDir.exists()) {
            if (log.isDebugEnabled()) {
                log.debug("Loading tasks from : " + tasksDir.getPath());
            }

            Iterator taskDefinitions = FileUtils.iterateFiles(tasksDir, extensions, false);
            while (taskDefinitions.hasNext()) {
                File file = (File) taskDefinitions.next();
                try {
                    OMElement document = getOMElement(file);
                    Startup startup = SynapseXMLConfigurationFactory.defineStartup(synapseConfig,
                            document, properties);
                    startup.setFileName(file.getName());
                    synapseConfig.getArtifactDeploymentStore().addArtifact(
                            file.getAbsolutePath(), startup.getName());
                } catch (Exception e) {
                    String msg = "Task configuration cannot be built from : " + file.getName();
                    handleConfigurationError(SynapseConstants.FAIL_SAFE_MODE_TASKS, msg, e);
                }
            }
        }
    }

    private static void createSequences(SynapseConfiguration synapseConfig, String rootDirPath,
                                        Properties properties) {

        File sequencesDir = new File(rootDirPath, SEQUENCES_DIR);
        if (sequencesDir.exists()) {
            if (log.isDebugEnabled()) {
                log.debug("Loading sequences from : " + sequencesDir.getPath());
            }

            Iterator sequences = FileUtils.iterateFiles(sequencesDir, extensions, false);
            while (sequences.hasNext()) {
                File file = (File) sequences.next();
                try{
                    OMElement document = getOMElement(file);
                    Mediator seq = SynapseXMLConfigurationFactory.defineSequence(synapseConfig,
                            document, properties);
                    if (seq != null && seq instanceof SequenceMediator) {
                        SequenceMediator sequence = (SequenceMediator) seq;
                        sequence.setFileName(file.getName());
                        synapseConfig.getArtifactDeploymentStore().addArtifact(
                                file.getAbsolutePath(), sequence.getName());
                    }
                } catch (Exception e) {
                    String msg = "Sequence configuration cannot be built from : " + file.getName();
                    handleConfigurationError(SynapseConstants.FAIL_SAFE_MODE_SEQUENCES, msg, e);
                }

             }
        }
    }

    private static void createTemplates(SynapseConfiguration synapseConfig, String rootDirPath,
                                        Properties properties) {

        File templatesDir = new File(rootDirPath, TEMPLATES_DIR);
        if (templatesDir.exists()) {
            if (log.isDebugEnabled()) {
                log.debug("Loading template from : " + templatesDir.getPath());
            }
            Iterator templates = FileUtils.iterateFiles(templatesDir, extensions, false);
            while (templates.hasNext()) {
                File file = (File) templates.next();
                try {
                    OMElement document = getOMElement(file);
                    OMElement element = document.getFirstChildWithName(
                            new QName(SynapseConstants.SYNAPSE_NAMESPACE, "sequence"));
                    if (element != null) {
                        TemplateMediator mediator =
                                (TemplateMediator) SynapseXMLConfigurationFactory.defineMediatorTemplate(
                                        synapseConfig, document, properties);
                        if (mediator != null) {
                            mediator.setFileName(file.getName());
                            synapseConfig.getArtifactDeploymentStore().addArtifact(
                                    file.getAbsolutePath(), mediator.getName());
                        }
                    } else {
                        element = document.getFirstChildWithName(
                                new QName(SynapseConstants.SYNAPSE_NAMESPACE, "endpoint"));
                        if (element != null) {
                            Template endpointTemplate =
                                    SynapseXMLConfigurationFactory.defineEndpointTemplate(
                                            synapseConfig, document, properties);
                            if (endpointTemplate != null) {
                                endpointTemplate.setFileName(file.getName());
                                synapseConfig.getArtifactDeploymentStore().addArtifact(
                                        file.getAbsolutePath(), endpointTemplate.getName());
                            }
                        }
                    }
                } catch (Exception e) {
                    String msg = "Template configuration cannot be built from : " + file.getName();
                    handleConfigurationError(SynapseConstants.FAIL_SAFE_MODE_TEMPLATES, msg, e);
                }
            }
        }
    }

    private static void createEndpoints(SynapseConfiguration synapseConfig, String rootDirPath,
                                        Properties properties) {

        File endpointsDir = new File(rootDirPath, ENDPOINTS_DIR);
        if (endpointsDir.exists()) {
            if (log.isDebugEnabled()) {
                log.debug("Loading endpoints from : " + endpointsDir.getPath());
            }

            Iterator endpoints = FileUtils.iterateFiles(endpointsDir, extensions, false);
            while (endpoints.hasNext()) {
                File file = (File) endpoints.next();
                try {
                    OMElement document = getOMElement(file);
                    Endpoint endpoint = SynapseXMLConfigurationFactory.defineEndpoint(
                            synapseConfig, document, properties);
                    if (endpoint != null) {
                        endpoint.setFileName(file.getName());
                        synapseConfig.getArtifactDeploymentStore().addArtifact(
                                file.getAbsolutePath(), endpoint.getName());
                    }
                } catch (Exception e) {
                    String msg = "Endpoint configuration cannot be built from : " + file.getName();
                    handleConfigurationError(SynapseConstants.FAIL_SAFE_MODE_EP, msg, e);
                }
            }
        }
    }

    private static void createEventSources(SynapseConfiguration synapseConfig, String rootDirPath,
                                           Properties properties) {

        File eventsDir = new File(rootDirPath, EVENTS_DIR);
        if (eventsDir.exists()) {
            if (log.isDebugEnabled()) {
                log.debug("Loading event sources from : " + eventsDir.getPath());
            }

            Iterator events = FileUtils.iterateFiles(eventsDir, extensions, false);
            while (events.hasNext()) {
                File file = (File) events.next();
                try {
                    OMElement document = getOMElement(file);
                    SynapseEventSource eventSource = SynapseXMLConfigurationFactory.
                            defineEventSource(synapseConfig, document, properties);
                    if (eventSource != null) {
                        eventSource.setFileName(file.getName());
                        synapseConfig.getArtifactDeploymentStore().addArtifact(
                                file.getAbsolutePath(), eventSource.getName());
                    }
                } catch (Exception e) {
                    String msg = "Event source configuration cannot be built from : " + file.getName();
                    handleConfigurationError(SynapseConstants.FAIL_SAFE_MODE_EVENT_SOURCE, msg, e);
                }
            }
        }
    }

    private static void createExecutors(SynapseConfiguration synapseConfig, String rootDirPath,
                                        Properties properties) {

        File executorsDir = new File(rootDirPath, EXECUTORS_DIR);
        if (executorsDir.exists()) {
            if (log.isDebugEnabled()) {
                log.debug("Loading event sources from : " + executorsDir.getPath());
            }

            Iterator executors = FileUtils.iterateFiles(executorsDir, extensions, false);
            while (executors.hasNext()) {
                File file = (File) executors.next();
                try {
                    OMElement document = getOMElement(file);
                    PriorityExecutor executor = SynapseXMLConfigurationFactory.
                            defineExecutor(synapseConfig, document, properties);
                    if (executor != null) {
                        executor.setFileName(file.getName());
                        synapseConfig.getArtifactDeploymentStore().addArtifact(
                                file.getAbsolutePath(), executor.getName());
                    }
                } catch (Exception e) {
                    String msg = "Executor configuration cannot be built from : " + file.getName();
                    handleConfigurationError(SynapseConstants.FAIL_SAFE_MODE_EXECUTORS, msg, e);
                }
            }
        }
    }

    private static void createMessageStores(SynapseConfiguration synapseConfig ,
                                            String rootDirPath, Properties properties) {

        File messageStoresDir = new File(rootDirPath, MESSAGE_STORE_DIR);
        if (messageStoresDir.exists() ) {
            if (log.isDebugEnabled()) {
                log.debug("Loading Message Stores from :" + messageStoresDir.getPath());
            }

            Iterator messageStores = FileUtils.iterateFiles(messageStoresDir, extensions, false);
            while (messageStores.hasNext()) {
                File file = (File) messageStores.next();
                try {
                    OMElement document = getOMElement(file);
                    MessageStore messageStore = SynapseXMLConfigurationFactory.defineMessageStore(
                            synapseConfig, document, properties);
                    if (messageStore != null) {
                        messageStore.setFileName(file.getName());
                        synapseConfig.getArtifactDeploymentStore().addArtifact(file.getAbsolutePath(),
                                messageStore.getName());
                    }
                } catch (Exception e) {
                    String msg = "Message store configuration cannot be built from : " + file.getName();
                    handleConfigurationError(SynapseConstants.FAIL_SAFE_MODE_MESSAGE_STORES, msg, e);
                }
            }
        }
    }


    private static void createMessageProcessors(SynapseConfiguration synapseConfig,
                                            String rootDirPath, Properties properties) {

        File messageProcessorDir = new File(rootDirPath, MESSAGE_PROCESSOR_DIR);
        if (messageProcessorDir.exists()) {
            if (log.isDebugEnabled()) {
                log.debug("Loading Message Processors from :" + messageProcessorDir.getPath());
            }

            Iterator messageProcessors = FileUtils.iterateFiles(messageProcessorDir, extensions, false);
            while (messageProcessors.hasNext()) {
                File file = (File) messageProcessors.next();
                try {
                    OMElement document = getOMElement(file);
                    MessageProcessor messageProcessor = SynapseXMLConfigurationFactory.defineMessageProcessor(
                            synapseConfig, document, properties);
                    if (messageProcessor != null) {
                        messageProcessor.setFileName(file.getName());
                        synapseConfig.getArtifactDeploymentStore().addArtifact(file.getAbsolutePath(),
                                messageProcessor.getName());
                    }
                } catch (Exception e) {
                    String msg = "Message processor configuration cannot be built from : " + file.getName();
                    handleConfigurationError(SynapseConstants.FAIL_SAFE_MODE_MESSAGE_PROCESSORS, msg, e);
                }
            }
        }
    }

    private static void createSynapseImports(SynapseConfiguration synapseConfig, String root, Properties properties) {
        File synImportsDir = new File(root, SYNAPSE_IMPORTS_DIR);
        if (synImportsDir.exists()) {
            if (log.isDebugEnabled()) {
                log.debug("Loading Synapse Imports from :" + synImportsDir.getPath());
            }
            Iterator synImports = FileUtils.iterateFiles(synImportsDir, extensions, false);
            while (synImports.hasNext()) {
                File file = (File) synImports.next();
                try {
                    OMElement document = getOMElement(file);
                    SynapseImport synImp = SynapseXMLConfigurationFactory.defineImport(
                            synapseConfig, document, properties);
                    if (synImp != null) {
                        synImp.setFileName(file.getName());
                        synapseConfig.getArtifactDeploymentStore().addArtifact(file.getAbsolutePath(),
                                synImp.getName());
                    }
                } catch (Exception e) {
                    String msg = "Import configuration cannot be built from : " + file.getName();
                    handleConfigurationError(SynapseConstants.FAIL_SAFE_MODE_IMPORTS, msg, e);
                }
            }
        }

    }


    private static void createAPIs(SynapseConfiguration synapseConfig,
                                   String rootDirPath, Properties properties) {

        File apiDir = new File(rootDirPath, REST_API_DIR);
        if (apiDir.exists()) {
            if (log.isDebugEnabled()) {
                log.debug("Loading APIs from :" + apiDir.getPath());
            }

            Iterator apiIterator = FileUtils.iterateFiles(apiDir, extensions, false);
            while (apiIterator.hasNext()) {
                File file = (File) apiIterator.next();
                try {
                    OMElement document = getOMElement(file);
                    API api = SynapseXMLConfigurationFactory.defineAPI(synapseConfig, document, properties);
                    if (api != null) {
                        api.setFileName(file.getName());
                        synapseConfig.getArtifactDeploymentStore().addArtifact(file.getAbsolutePath(),
                                api.getName());
                    }
                } catch (Exception e) {
                    String msg = "API configuration cannot be built from : " + file.getName();
                    handleConfigurationError(SynapseConstants.FAIL_SAFE_MODE_API, msg, e);
                }
            }
        }
    }

    private static OMElement getOMElement(File file) {
        FileInputStream is;
        OMElement document = null;

        try {
            is = FileUtils.openInputStream(file);
        } catch (IOException e) {
            handleException("Error while opening the file: " + file.getName() + " for reading", e);
            return null;
        }

        try {
            document = new StAXOMBuilder(is).getDocumentElement();
            document.build();
            is.close();
        } catch (XMLStreamException e) {
            handleException("Error while parsing the content of the file: " + file.getName(), e);
        } catch (IOException e) {
            log.warn("Error while closing the input stream from the file: " + file.getName(), e);
        }

        return document;
    }

    private static void handleConfigurationError(String componentType, String msg, Exception e) {
        if (SynapseConfigUtils.isFailSafeEnabled(componentType)) {
            log.warn(msg + " - Continue in fail-safe mode", e);
        } else {
            log.error(msg, e);
            throw new SynapseException(msg, e);
        }
    }

    private static void handleException(String msg, Exception e) {
        log.error(msg, e);
        throw new SynapseException(msg, e);
    }
}
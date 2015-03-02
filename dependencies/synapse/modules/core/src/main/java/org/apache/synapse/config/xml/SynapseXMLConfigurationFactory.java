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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.Mediator;
import org.apache.synapse.Startup;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.SynapseException;
import org.apache.synapse.config.xml.endpoints.TemplateFactory;
import org.apache.synapse.config.xml.rest.APIFactory;
import org.apache.synapse.endpoints.Template;
import org.apache.synapse.libraries.imports.SynapseImport;
import org.apache.synapse.libraries.model.Library;
import org.apache.synapse.libraries.util.LibDeployerUtils;
import org.apache.synapse.mediators.template.TemplateMediator;
import org.apache.synapse.message.processor.MessageProcessor;
import org.apache.synapse.message.store.MessageStore;
import org.apache.synapse.commons.executors.PriorityExecutor;
import org.apache.synapse.commons.executors.config.PriorityExecutorFactory;
import org.apache.synapse.config.Entry;
import org.apache.synapse.config.SynapseConfigUtils;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.config.xml.endpoints.EndpointFactory;
import org.apache.synapse.config.xml.eventing.EventSourceFactory;
import org.apache.synapse.core.axis2.ProxyService;
import org.apache.synapse.endpoints.Endpoint;
import org.apache.synapse.eventing.SynapseEventSource;
import org.apache.synapse.registry.Registry;
import org.apache.axis2.AxisFault;
import org.apache.synapse.rest.API;

import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.Properties;

public class SynapseXMLConfigurationFactory implements ConfigurationFactory {

    private static Log log = LogFactory.getLog(SynapseXMLConfigurationFactory.class);

    public SynapseConfiguration getConfiguration(OMElement definitions, Properties properties) {

        if (!definitions.getQName().equals(XMLConfigConstants.DEFINITIONS_ELT)) {
            throw new SynapseException(
                    "Wrong QName for this configuration factory " + definitions.getQName());
        }
        SynapseConfiguration config = SynapseConfigUtils.newConfiguration();               
        config.setDefaultQName(definitions.getQName());

        Iterator itr = definitions.getChildren();
        while (itr.hasNext()) {
            Object o = itr.next();
            if (o instanceof OMElement) {
                OMElement elt = (OMElement) o;
                if (XMLConfigConstants.SEQUENCE_ELT.equals(elt.getQName())) {
                    String key = elt.getAttributeValue(
                            new QName(XMLConfigConstants.NULL_NAMESPACE, "key"));
                    // this could be a sequence def or a referred sequence
                    if (key != null) {
                        handleException("Referred sequences are not allowed at the top level");
                    } else {
                        defineSequence(config, elt, properties);
                    }
                } else if (XMLConfigConstants.TEMPLATE_ELT.equals(elt.getQName())) {
                    defineTemplate(config, elt, properties);
                } else if (XMLConfigConstants.IMPORT_ELT.equals(elt.getQName())) {
                    defineImport(config, elt, properties);
                } else if (XMLConfigConstants.ENDPOINT_ELT.equals(elt.getQName())) {
                    defineEndpoint(config, elt, properties);
                } else if (XMLConfigConstants.ENTRY_ELT.equals(elt.getQName())) {
                    defineEntry(config, elt, properties);
                } else if (XMLConfigConstants.PROXY_ELT.equals(elt.getQName())) {
                    defineProxy(config, elt, properties);
                } else if (XMLConfigConstants.REGISTRY_ELT.equals(elt.getQName())) {
                    defineRegistry(config, elt, properties);
                } else if (XMLConfigConstants.EVENT_SOURCE_ELT.equals(elt.getQName())) {
                    defineEventSource(config, elt, properties);
                } else if (XMLConfigConstants.EXECUTOR_ELT.equals(elt.getQName())) {
                    defineExecutor(config, elt, properties);
                } else if(XMLConfigConstants.MESSAGE_STORE_ELT.equals(elt.getQName())) {
                    defineMessageStore(config, elt, properties);
                } else if (XMLConfigConstants.MESSAGE_PROCESSOR_ELT.equals(elt.getQName())){
                    defineMessageProcessor(config, elt, properties);
                } else if (StartupFinder.getInstance().isStartup(elt.getQName())) {
                    defineStartup(config, elt, properties);
                } else if (XMLConfigConstants.API_ELT.equals(elt.getQName())) {
                    defineAPI(config, elt, properties);
                } else if (XMLConfigConstants.DESCRIPTION_ELT.equals(elt.getQName())) {
                    config.setDescription(elt.getText());
                } else {
                    handleException("Invalid configuration element at the top level, one of \'sequence\', " +
                            "\'endpoint\', \'proxy\', \'eventSource\', \'localEntry\', \'priorityExecutor\' " +
                            "or \'registry\' is expected");
                }
            }
        }

        return config;
    }

    public static Registry defineRegistry(SynapseConfiguration config, OMElement elem,
                                          Properties properties) {
        if (config.getRegistry() != null) {
            handleException("Only one remote registry can be defined within a configuration");
        }
        Registry registry = RegistryFactory.createRegistry(elem, properties);
        config.setRegistry(registry);
        return registry;
    }

    public static Startup defineStartup(SynapseConfiguration config, OMElement elem,
                                        Properties properties) {
        Startup startup = StartupFinder.getInstance().getStartup(elem, properties);
        config.addStartup(startup);
        return startup;
    }

    public static ProxyService defineProxy(SynapseConfiguration config, OMElement elem,
                                           Properties properties) {
        ProxyService proxy = null;

        try {
            proxy = ProxyServiceFactory.createProxy(elem, properties);
            if (proxy != null) {
                config.addProxyService(proxy.getName(), proxy);
            }
        } catch (Exception e) {
            String msg = "Proxy Service configuration: " + elem.getAttributeValue((
                    new QName(XMLConfigConstants.NULL_NAMESPACE, "name"))) + " cannot be built";
            handleConfigurationError(SynapseConstants.FAIL_SAFE_MODE_PROXY_SERVICES, msg, e);
        }
        
        return proxy;
    }

    public static Entry defineEntry(SynapseConfiguration config, OMElement elem,
                                    Properties properties) {
        Entry entry = null;

        try {
            entry = EntryFactory.createEntry(elem, properties);
            if (entry != null) {
                config.addEntry(entry.getKey(), entry);
            }
        } catch (Exception e) {
            String msg = "Local entry configuration: " + elem.getAttributeValue((
                    new QName(XMLConfigConstants.NULL_NAMESPACE, "key"))) + " cannot be built";
            handleConfigurationError(SynapseConstants.FAIL_SAFE_MODE_LOCALENTRIES, msg, e);
        }
        return entry;
    }
    
	public static Entry defineEntry(SynapseConfiguration config, OMElement elem,
			Properties properties,Library library) {
		Entry entry = null;
		try {
			entry = EntryFactory.createEntry(elem, properties);
			String key = library.getQName().getLocalPart()+"."+entry.getKey();
			if(entry != null && config.getEntry(key) != null){
				//already existing thus need to update entry
				config.updateEntry(library.getQName().getLocalPart()+"."+entry.getKey(), entry);
			}else{
				config.addEntry(library.getQName().getLocalPart()+"."+entry.getKey(), entry);
				library.getLocalEntries().add(key);
			}
			
			
		} catch (Exception e) {
			String msg = "Local entry configuration: "
					+ elem.getAttributeValue((new QName(XMLConfigConstants.NULL_NAMESPACE, "key")))
					+ " cannot be built";
			handleConfigurationError(SynapseConstants.FAIL_SAFE_MODE_LOCALENTRIES, msg, e);
		}
		return entry;
	}

    public static Mediator defineSequence(SynapseConfiguration config, OMElement ele,
                                          Properties properties) {

        Mediator mediator = null;
        String name = ele.getAttributeValue(new QName(XMLConfigConstants.NULL_NAMESPACE, "name"));
        if (name != null) {
            try {
            	MediatorFactoryFinder.getInstance().setSynapseImportMap(config.getSynapseImports());
                mediator = MediatorFactoryFinder.getInstance().getMediator(ele, properties);
                if (mediator != null) {
                    config.addSequence(name, mediator);
                    // mandatory sequence is treated as a special sequence because it will be fetched for
                    // each and every message and keeps a direct reference to that from the configuration
                    // this also limits the ability of the mandatory sequence to be dynamic
                    if (SynapseConstants.MANDATORY_SEQUENCE_KEY.equals(name)) {
                        config.setMandatorySequence(mediator);
                    }
                }
            } catch (Exception e) {
                String msg = "Sequence configuration: " + name + " cannot be built";
                handleConfigurationError(SynapseConstants.FAIL_SAFE_MODE_SEQUENCES, msg, e);
            }
            return mediator;
        } else {
            String msg = "Invalid sequence definition without a name";
            handleConfigurationError(SynapseConstants.FAIL_SAFE_MODE_SEQUENCES, msg);
        }
        return null;
    }

    public static Mediator defineMediatorTemplate(SynapseConfiguration config, OMElement ele,
                                                  Properties properties) {

        Mediator mediator = null;
        String name = ele.getAttributeValue(new QName(XMLConfigConstants.NULL_NAMESPACE, "name"));
        if (name != null) {
            try {
                mediator = MediatorFactoryFinder.getInstance().getMediator(ele, properties);
                if (mediator != null) {
                    config.addSequenceTemplate(name, (TemplateMediator) mediator) ;
                }
            } catch (Exception e) {
                String msg = "Template configuration: " + name + " cannot be built";
                handleConfigurationError(SynapseConstants.FAIL_SAFE_MODE_TEMPLATES, msg, e);
            }
            return mediator;
        } else {
            String msg = "Invalid mediation template definition without a name";
            handleConfigurationError(SynapseConstants.FAIL_SAFE_MODE_SEQUENCES, msg);
        }
        return null;
    }

    public static Endpoint defineEndpoint(SynapseConfiguration config, OMElement ele,
                                          Properties properties) {

        String name = ele.getAttributeValue(new QName(XMLConfigConstants.NULL_NAMESPACE, "name"));
        Endpoint endpoint = null;
        if (name != null) {
            try {
                endpoint = EndpointFactory.getEndpointFromElement(ele, false, properties);
                if (endpoint != null) {
                    config.addEndpoint(name.trim(), endpoint);
                }
            } catch (Exception e) {
                String msg = "Endpoint configuration: " + name + " cannot be built";
                handleConfigurationError(SynapseConstants.FAIL_SAFE_MODE_EP, msg, e);
            }
            return endpoint;
        } else {
            String msg = "Invalid endpoint definition without a name";
            handleConfigurationError(SynapseConstants.FAIL_SAFE_MODE_SEQUENCES, msg);
        }
        return null;
    }

    public static SynapseEventSource defineEventSource(SynapseConfiguration config,
                                                       OMElement elem, Properties properties) {

        SynapseEventSource eventSource = null;

        try {
            eventSource = EventSourceFactory.createEventSource(elem, properties);
            if (eventSource != null) {
                config.addEventSource(eventSource.getName(), eventSource);
            }
        } catch (Exception e) {
            String msg = "Event Source configuration cannot be built";
            handleConfigurationError(SynapseConstants.FAIL_SAFE_MODE_EVENT_SOURCE, msg, e);
        }
        return eventSource;
    }

    public static PriorityExecutor defineExecutor(SynapseConfiguration config,
                                                       OMElement elem, Properties properties) {

        PriorityExecutor executor = null;
        try {
            executor = PriorityExecutorFactory.createExecutor(
                    XMLConfigConstants.SYNAPSE_NAMESPACE, elem, true, properties);
            assert executor != null;
            config.addPriorityExecutor(executor.getName(), executor);
        } catch (AxisFault axisFault) {
            String msg = "Executor configuration cannot be built";
            handleConfigurationError(SynapseConstants.FAIL_SAFE_MODE_EXECUTORS, msg, axisFault);
        }
        return executor;
    }

    public static MessageStore defineMessageStore(SynapseConfiguration config,
                                                  OMElement elem, Properties properties) {
        MessageStore messageStore = null;
        try {
            messageStore = MessageStoreFactory.createMessageStore(elem, properties);
            config.addMessageStore(messageStore.getName(), messageStore);
        } catch (Exception e) {
            String msg = "Message Store configuration cannot be built";
            handleConfigurationError(SynapseConstants.FAIL_SAFE_MODE_MESSAGE_STORES, msg, e);
        }
        return messageStore;
    }

    public static MessageProcessor defineMessageProcessor(SynapseConfiguration config,
                                                          OMElement elem, Properties properties) {
        MessageProcessor processor = null;
        try {
            processor = MessageProcessorFactory.createMessageProcessor(elem);
            config.addMessageProcessor(processor.getName(), processor);
        } catch (Exception e) {
            String msg = "Message Processor configuration cannot be built";
            handleConfigurationError(SynapseConstants.FAIL_SAFE_MODE_MESSAGE_PROCESSORS, msg, e);
        }
        return processor;
    }

    public static SynapseImport defineImport(SynapseConfiguration config, OMElement elt, Properties properties) {
        SynapseImport synImport = SynapseImportFactory.createImport(elt, properties);
        String libIndexString = LibDeployerUtils.getQualifiedName(synImport);
        config.addSynapseImport(libIndexString, synImport);

        //get corresponding library for loading imports if available
        Library synLib = config.getSynapseLibraries().get(libIndexString);
        if (synLib != null) {
            LibDeployerUtils.loadLibArtifacts(synImport, synLib);
        }
        return synImport;
    }

    public static Template defineEndpointTemplate(SynapseConfiguration config,
                                                    OMElement elem, Properties properties) {

        TemplateFactory templateFactory = new TemplateFactory();
        String name = elem.getAttributeValue(new QName(XMLConfigConstants.NULL_NAMESPACE, "name"));
        try {
            Template template = templateFactory.createEndpointTemplate(elem, properties);
            if (template != null) {
                config.addEndpointTemplate(template.getName(), template);
            }
            return template;
        } catch (Exception e) {
            String msg = "Endpoint Template: " + name + "configuration cannot be built";
            handleConfigurationError(SynapseConstants.FAIL_SAFE_MODE_TEMPLATES, msg, e);
        }
        return null;
    }

    public static void defineTemplate(SynapseConfiguration config,
                                      OMElement elem, Properties properties) {
        OMElement element = elem.getFirstChildWithName(
                new QName(SynapseConstants.SYNAPSE_NAMESPACE, "sequence"));
        if (element != null) {
            defineMediatorTemplate(config, elem, properties);
        }

        element = elem.getFirstChildWithName(
                new QName(SynapseConstants.SYNAPSE_NAMESPACE, "endpoint"));
        if (element != null) {
            defineEndpointTemplate(config, elem, properties);
        }
    }

    public static API defineAPI(SynapseConfiguration config, OMElement elem) {
        return defineAPI(config, elem, new Properties());
    }

    public static API defineAPI(SynapseConfiguration config, OMElement elem, Properties properties) {
        API api = null;
        try {
            api = APIFactory.createAPI(elem, properties);
            config.addAPI(api.getName(), api);
        } catch (Exception e) {
            String msg = "API configuration cannot be built";
            handleConfigurationError(SynapseConstants.FAIL_SAFE_MODE_API, msg, e);
        }
        return api;
    }

    private static void handleException(String msg) {
        log.error(msg);
        throw new SynapseException(msg);
    }

    public QName getTagQName() {

        return XMLConfigConstants.DEFINITIONS_ELT;
    }

    public Class getSerializerClass() {
        return SynapseXMLConfigurationSerializer.class;
    }

    private static void handleConfigurationError(String componentType, String msg) {
        if (SynapseConfigUtils.isFailSafeEnabled(componentType)) {
            log.warn(msg + " - Continue in fail-safe mode");
        } else {
            handleException(msg);
        }
    }

    private static void handleConfigurationError(String componentType, String msg, Exception e) {
        if (SynapseConfigUtils.isFailSafeEnabled(componentType)) {
            log.warn(msg + " - Continue in fail-safe mode", e);
        } else {
            log.error(msg, e);
            throw new SynapseException(msg, e);
        }
    }
}

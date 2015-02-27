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

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.Startup;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.SynapseException;
import org.apache.synapse.config.xml.endpoints.TemplateSerializer;
import org.apache.synapse.config.xml.rest.APISerializer;
import org.apache.synapse.endpoints.Template;
import org.apache.synapse.libraries.imports.SynapseImport;
import org.apache.synapse.mediators.template.TemplateMediator;
import org.apache.synapse.message.processor.MessageProcessor;
import org.apache.synapse.message.store.MessageStore;
import org.apache.synapse.commons.executors.PriorityExecutor;
import org.apache.synapse.commons.executors.config.PriorityExecutorSerializer;
import org.apache.synapse.config.Entry;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.config.xml.endpoints.EndpointSerializer;
import org.apache.synapse.config.xml.eventing.EventSourceSerializer;
import org.apache.synapse.core.axis2.ProxyService;
import org.apache.synapse.endpoints.Endpoint;
import org.apache.synapse.eventing.SynapseEventSource;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.apache.synapse.rest.API;

import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SynapseXMLConfigurationSerializer implements ConfigurationSerializer {

    private static final Log log = LogFactory
            .getLog(XMLConfigurationSerializer.class);

    private static final OMFactory fac = OMAbstractFactory.getOMFactory();

    private static final OMNamespace synNS = SynapseConstants.SYNAPSE_OMNAMESPACE;

    /**
     * Order of entries is irrelevant, however its nice to have some order.
     *
     * @param synCfg configuration to be serialized
     * @return serialized element of the configuration
     */

    public OMElement serializeConfiguration(SynapseConfiguration synCfg) {

        OMElement definitions = fac.createOMElement("definitions", synNS);

        // first add the description
        if (synCfg.getDescription() != null) {
            OMElement descElem = fac.createOMElement("description", synNS);
            descElem.setText(synCfg.getDescription());
            definitions.addChild(descElem);
        }

        // then process a remote registry if present
        if (synCfg.getRegistry() != null) {
            RegistrySerializer.serializeRegistry(definitions, synCfg
                    .getRegistry());
        }
        
        serializeImports(definitions, synCfg.getSynapseImports().values());

        // add proxy services
        Iterator itr = synCfg.getProxyServices().iterator();
        while (itr.hasNext()) {
            ProxyService service = (ProxyService) itr.next();
            ProxyServiceSerializer.serializeProxy(definitions, service);
        }

        // Add Event sources 
        for (SynapseEventSource eventSource : synCfg.getEventSources()) {
            EventSourceSerializer.serializeEventSource(definitions, eventSource);
        }

        Map<String, Entry> entries = new HashMap<String, Entry>();
        Map<String, Endpoint> endpoints = new HashMap<String, Endpoint>();
        Map<String, SequenceMediator> sequences = new HashMap<String, SequenceMediator>();
        Map<String, TemplateMediator> templates = new HashMap<String, TemplateMediator>();
        Map<String, Template> endpointTemplates = new HashMap<String, Template>();

        itr = synCfg.getLocalRegistry().keySet().iterator();
        while (itr.hasNext()) {
            Object key = itr.next();
            if (SynapseConstants.SERVER_IP.equals(key) || SynapseConstants.SERVER_HOST.equals(key)) {
                continue;
            }
            Object o = synCfg.getLocalRegistry().get(key);
            if (o instanceof TemplateMediator){
                templates.put(key.toString(), (TemplateMediator) o);
            } else if (o instanceof SequenceMediator) {
                sequences.put(key.toString(), (SequenceMediator) o);
            } else if (o instanceof Endpoint) {
                endpoints.put(key.toString(), (Endpoint) o);
            } else if (o instanceof Template) {
                endpointTemplates.put(key.toString(), (Template) o);
            } else if (o instanceof Entry) {
                entries.put(key.toString(), (Entry) o);
            } else {
                handleException("Unknown object : " + o.getClass()
                        + " for serialization into Synapse configuration");
            }
        }

        // process entries
        serializeEntries(definitions, entries);

        // process endpoints
        serializeEndpoints(definitions, endpoints);

        // process sequences
        serializeSequences(definitions, sequences);

        // process templates
        serializeMediatorTemplates(definitions, templates);

        // serialize the endpoint templates
        serializeEndpointTemplates(definitions, endpointTemplates);

        // handle startups
        serializeStartups(definitions, synCfg.getStartups());

        // Executors
        serializeExecutors(definitions, synCfg.getPriorityExecutors());

       // Message stores
        serializeMessageStores(definitions, synCfg.getMessageStores());        
       //Message Processors
        serializeMessageProcessors(definitions,synCfg.getMessageProcessors());

        serializeAPIs(definitions, synCfg.getAPIs());
     

        return definitions;
    }

    private static void serializeEntries(OMElement definitions, Map<String, Entry> entries) {
        for (Entry entry : entries.values()) {
            EntrySerializer.serializeEntry(entry, definitions);
        }
    }

    private static void serializeStartups(OMElement definitions, Collection startups) {
        for (Object o : startups) {
            if (o instanceof Startup) {
                Startup s = (Startup) o;
                StartupFinder.getInstance().serializeStartup(definitions, s);
            }
        }
    }

    private static void serializeEndpoints(OMElement definitions, Map<String, Endpoint> endpoints) {
        for (Endpoint endpoint: endpoints.values()) {
            definitions.addChild(EndpointSerializer.getElementFromEndpoint(endpoint));
        }
    }

    private static void serializeSequences(OMElement definitions,
                                           Map<String, SequenceMediator> sequences) {
        for (SequenceMediator seq : sequences.values()) {
            MediatorSerializerFinder.getInstance().getSerializer(seq)
                    .serializeMediator(definitions, seq);

        }
    }

    private static void serializeMediatorTemplates(OMElement definitions,
                                                   Map<String, TemplateMediator> eipSequences) {
        for (TemplateMediator template : eipSequences.values()) {
            MediatorSerializerFinder.getInstance().getSerializer(template)
                    .serializeMediator(definitions, template);

        }
    }

    private static void serializeExecutors(OMElement definitions,
                                           Map<String, PriorityExecutor> executors) {
        for (PriorityExecutor exec : executors.values()) {
            PriorityExecutorSerializer.serialize(definitions, exec,
                    XMLConfigConstants.SYNAPSE_NAMESPACE);
        }        
    }

    private static void serializeMessageStores(OMElement definitions,
                                               Map<String, MessageStore> messageStores ){

        for (MessageStore ms : messageStores.values()) {
            MessageStoreSerializer.serializeMessageStore(definitions, ms);
        }        
    }

    private static void serializeMessageProcessors(OMElement definitions,
                                               Map<String, MessageProcessor> processorMap ){
        for (MessageProcessor mp : processorMap.values()) {
            MessageProcessorSerializer.serializeMessageProcessor(definitions,mp);
        }
    }

    private static void serializeEndpointTemplates(OMElement definitions,
                                                   Map<String, Template> templateMap) {
        for (Template template : templateMap.values()) {
            TemplateSerializer serializer = new TemplateSerializer();
            serializer.serializeEndpointTemplate(template, definitions);
        }
    }

    private static void serializeAPIs(OMElement definitions,
                                      Collection<API> apiSet) {
        for (API api : apiSet) {
            OMElement apiElt = APISerializer.serializeAPI(api);
            definitions.addChild(apiElt);
        }
    }

    private static void serializeImports(OMElement definitions,
                                      Collection<SynapseImport> synImportSet) {
        for (SynapseImport synapseImport : synImportSet) {
            OMElement importElt = SynapseImportSerializer.serializeImport(synapseImport);
            definitions.addChild(importElt);
        }
    }

    private static void handleException(String msg) {
        log.error(msg);
        throw new SynapseException(msg);
    }

    public QName getTagQName() {
        return XMLConfigConstants.DEFINITIONS_ELT;
	}

}

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

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.Mediator;
import org.apache.synapse.SynapseException;
import org.apache.synapse.config.XMLToObjectMapper;
import org.apache.synapse.config.xml.eventing.EventPublisherMediatorFactory;
import org.apache.synapse.libraries.imports.SynapseImport;
import org.apache.synapse.libraries.model.Library;
import org.apache.synapse.mediators.Value;
import org.apache.synapse.mediators.template.InvokeMediator;
import sun.misc.Service;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 *
 *
 * This class is based on J2SE Service Provider model
 * http://java.sun.com/j2se/1.3/docs/guide/jar/jar.html#Service%20Provider
 */

public class MediatorFactoryFinder implements XMLToObjectMapper {

 	private Map<String, Library> synapseLibraryMap;
    private Map<String, SynapseImport> synapseImportMap;

	private static final Log log = LogFactory.getLog(MediatorFactoryFinder.class);

	private static final Class[] mediatorFactories = {
        SequenceMediatorFactory.class,
        LogMediatorFactory.class,
        SendMediatorFactory.class,
        FilterMediatorFactory.class,
        SynapseMediatorFactory.class,
        DropMediatorFactory.class,
        HeaderMediatorFactory.class,
        FaultMediatorFactory.class,
        PropertyMediatorFactory.class,
        SwitchMediatorFactory.class,
        InMediatorFactory.class,
        OutMediatorFactory.class,
        RMSequenceMediatorFactory.class,
        ClassMediatorFactory.class,
        ValidateMediatorFactory.class,
        XSLTMediatorFactory.class,
        AnnotatedCommandMediatorFactory.class,
        POJOCommandMediatorFactory.class,
        CloneMediatorFactory.class,
        IterateMediatorFactory.class,
        AggregateMediatorFactory.class,
        DBReportMediatorFactory.class,
        DBLookupMediatorFactory.class,
        CacheMediatorFactory.class,
        CalloutMediatorFactory.class,
        EventPublisherMediatorFactory.class,
        TransactionMediatorFactory.class,
        EnqueueMediatorFactory.class,
        ConditionalRouterMediatorFactory.class,
        SamplingThrottleMediatorFactory.class,
        URLRewriteMediatorFactory.class,
        EnrichMediatorFactory.class,
        MessageStoreMediatorFactory.class,
        TemplateMediatorFactory.class,
        InvokeMediatorFactory.class,
        PayloadFactoryMediatorFactory.class,
        BeanMediatorFactory.class,
        EJBMediatorFactory.class,
        CallMediatorFactory.class,
        LoopBackMediatorFactory.class,
        RespondMediatorFactory.class
    };

    private final static MediatorFactoryFinder instance  = new MediatorFactoryFinder();

    /**
     * A map of mediator QNames to implementation class
     */
    private static Map<QName, Class> factoryMap = new HashMap<QName, Class>();

    private static boolean initialized = false;

    public static synchronized MediatorFactoryFinder getInstance() {
        if (!initialized) {
            loadMediatorFactories();
        }
        return instance;
    }

    /**
     * Force re initialization next time
     */
    public static synchronized void reset() {
        factoryMap.clear();
        initialized = false;
    }

    private MediatorFactoryFinder() {
    }

    private static void loadMediatorFactories() {
        for (Class c : mediatorFactories) {
            try {
                MediatorFactory fac = (MediatorFactory) c.newInstance();
                factoryMap.put(fac.getTagQName(), c);
            } catch (Exception e) {
                throw new SynapseException("Error instantiating " + c.getName(), e);
            }
        }
        // now iterate through the available pluggable mediator factories
        registerExtensions();
        initialized = true;
    }

    /**
     * Register pluggable mediator factories from the classpath
     *
     * This looks for JAR files containing a META-INF/services that adheres to the following
     * http://java.sun.com/j2se/1.3/docs/guide/jar/jar.html#Service%20Provider
     */
    private static void registerExtensions() {

        // register MediatorFactory extensions
        Iterator it = Service.providers(MediatorFactory.class);
        while (it.hasNext()) {
            MediatorFactory mf = (MediatorFactory) it.next();
            QName tag = mf.getTagQName();
            factoryMap.put(tag, mf.getClass());
            if (log.isDebugEnabled()) {
                log.debug("Added MediatorFactory " + mf.getClass() + " to handle " + tag);
            }
        }
    }

    /**
	 * This method returns a Processor given an OMElement. This will be used
	 * recursively by the elements which contain processor elements themselves
	 * (e.g. rules)
	 *
	 * @param element XML representation of a mediator
     * @param properties bag of properties to pass in any information to the factory
     * @return Processor
	 */
	public Mediator getMediator(OMElement element, Properties properties) {

        String localName = element.getLocalName();
        QName qName;
        if (element.getNamespace() != null) {
            qName = new QName(element.getNamespace().getNamespaceURI(), localName);
        } else {
            qName = new QName(localName);
        }
        if (log.isDebugEnabled()) {
            log.debug("getMediator(" + qName + ")");
        }
        Class cls = factoryMap.get(qName);

        if (cls == null && localName.indexOf('.') > -1) {
            String newLocalName = localName.substring(0, localName.indexOf('.'));
            qName = new QName(element.getNamespace().getNamespaceURI(), newLocalName);
            if (log.isDebugEnabled()) {
                log.debug("getMediator.2(" + qName + ")");
            }
            cls = factoryMap.get(qName);
        }

        if (cls == null) {
            if (synapseLibraryMap != null
                    && !synapseLibraryMap.isEmpty()) {
                for (Map.Entry<String, Library> entry : synapseLibraryMap.entrySet()) {
                    if (entry.getValue().getLibArtifactDetails().containsKey(localName)) {
                        return getDynamicInvokeMediator(element, entry.getValue().getPackage());
                    }
                }
            }

            if (!synapseImportMap.isEmpty()) {
                for (Map.Entry<String, SynapseImport> entry : synapseImportMap.entrySet()) {
                    if (localName.startsWith(entry.getValue().getLibName())) {
                        return getDynamicInvokeMediator(element, entry.getValue().getLibPackage());
                    }
                }
            }


            String msg = "Unknown mediator referenced by configuration element : " + qName;
            log.error(msg);
            throw new SynapseException(msg);
        }

        try {
			MediatorFactory mf = (MediatorFactory) cls.newInstance();
			return mf.createMediator(element, properties);

        } catch (InstantiationException e) {
            String msg = "Error initializing mediator factory : " + cls;
            log.error(msg);
            throw new SynapseException(msg, e);

        } catch (IllegalAccessException e) {
            String msg = "Error initializing mediator factory : " + cls;
            log.error(msg);
            throw new SynapseException(msg, e);
		}
	}

    /**
     * This method exposes all the MediatorFactories and its Extensions
     * @return factoryMap
     */
    public Map<QName, Class> getFactoryMap() {
        return factoryMap;
    }

    /**
     * Allow the mediator factory finder to act as an XMLToObjectMapper for Mediators
     * (i.e. Sequence Mediator) loaded dynamically from a Registry
     * @param om node from which the object is expected
     * @return Object buit from the om node
     */
    public Object getObjectFromOMNode(OMNode om, Properties properties) {
        if (om instanceof OMElement) {
            return getMediator((OMElement) om, properties);
        } else {
            handleException("Invalid mediator configuration XML : " + om);
        }
        return null;
    }

    private void handleException(String msg) {
        log.error(msg);
        throw new SynapseException(msg);
    }

    public Map<String, Library> getSynapseLibraryMap() {
        return synapseLibraryMap;
    }

    public void setSynapseLibraryMap(Map<String, Library> synapseLibraryMap) {
        this.synapseLibraryMap = synapseLibraryMap;
    }

    public Map<String, SynapseImport> getSynapseImportMap() {
        return synapseImportMap;
    }

    public void setSynapseImportMap(Map<String, SynapseImport> synapseImportMap) {
        this.synapseImportMap = synapseImportMap;
    }

    public static void main(String[] args) throws Exception{
        String connectorStr = "<sfdc.getContact xmlns=\"http://ws.apache.org/ns/synapse\">\n" +
                "\t\t <parameter name=\"param1\" value=\"val1\"/>\n" +
                "\t\t <parameter name=\"param2\" value=\"val2\"/>\n" +
                "\t</sfdc.getContact>";

        OMElement inConnectorElem = AXIOMUtil.stringToOM(connectorStr);
        String libName = "synapse.lang.eip";

        InvokeMediator invokeMediator = MediatorFactoryFinder.getInstance().getDynamicInvokeMediator(inConnectorElem, libName);
        invokeMediator.getTargetTemplate();


    }

    public OMElement getCallTemplateFromConnector(OMElement connectorElem, String libraryName) {
        String callTemplateConfig = "<call-template target=\"synapse.lang.eip.sfdc.getContact\">\n" +
                "            <with-param name=\"p1\" value=\"abc\"/>\n" +
                "            <with-param name=\"p2\" value=\"efg\"/>\n" +
                "        </call-template>";
        OMElement callTemplateElem = null;

        try {
            callTemplateElem = AXIOMUtil.stringToOM(callTemplateConfig);


        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
        return callTemplateElem;
    }

    public InvokeMediator getDynamicInvokeMediator(OMElement connectorElem, String libraryName) {
        InvokeMediator invokeMediator = new InvokeMediator();
        if (connectorElem.getLocalName() != null
                && libraryName != null
                && !libraryName.equals("")) {
            invokeMediator.setTargetTemplate(libraryName + "." + connectorElem.getLocalName());
        }
        
		// load configuration based references for the given connector
		OMAttribute config_key = connectorElem.getAttribute(new QName(XMLConfigConstants.CONFIG_REF));
		if (config_key != null) {
			// ValueFactory for creating dynamic or static Value
			ValueFactory keyFac = new ValueFactory();
			// create dynamic or static key based on OMElement
			Value generatedKey = keyFac.createValue(XMLConfigConstants.CONFIG_REF, connectorElem);
			// setKey
			invokeMediator.setKey(generatedKey);
		}

        buildParamteres(connectorElem, invokeMediator);

        invokeMediator.setPackageName(libraryName);
        invokeMediator.setDynamicMediator(true);
        return invokeMediator;

    }

	private void buildParamteres(OMElement connectorElem, InvokeMediator invokeMediator) {
		Iterator parameters = connectorElem.getChildElements();
        while (parameters.hasNext()) {
            OMNode paramNode = (OMNode) parameters.next();
            if (paramNode instanceof OMElement) {
                String paramName = ((OMElement) paramNode).getLocalName(); //((OMElement) paramNode).getAttributeValue(new QName("name"));
                String paramValueStr = ((OMElement) paramNode).getText();//((OMElement) paramNode).getAttributeValue(new QName("value"));
                if (paramName != null && !paramName.equals("")
                        && paramValueStr != null
                        && !paramValueStr.equals("")) {
                    Value paramValue = new ValueFactory().createTextValue((OMElement) paramNode);
                    invokeMediator.addExpressionForParamName(paramName, paramValue);
                }
            }
        }
	}
}

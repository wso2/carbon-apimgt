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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.xml.namespace.QName;

import org.apache.axiom.om.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.apache.synapse.Startup;
import org.apache.synapse.config.XMLToObjectMapper;
import org.apache.synapse.startup.quartz.SimpleQuartzFactory;

import sun.misc.Service;

public class StartupFinder implements XMLToObjectMapper {

    private static final Log log = LogFactory
            .getLog(ConfigurationFactoryAndSerializerFinder.class);

    private final static StartupFinder instance = new StartupFinder();

    /**
     * A map of mediator QNames to implementation class
     */
    private static Map<QName,Class<? extends StartupFactory>> factoryMap
                        = new HashMap<QName,Class<? extends StartupFactory>>();
    
    private static Map<QName,Class<? extends StartupSerializer>> serializerMap
                        = new HashMap<QName,Class<? extends StartupSerializer>>();

    private static boolean initialized = false;

    public static synchronized StartupFinder getInstance() {
        if (!initialized) {
            loadStartups();
        }
        return instance;
    }

    /**
     * Force re initialization next time
     */
    public static synchronized void reset() {
        factoryMap.clear();
        serializerMap.clear();
        initialized = false;
    }

    private static final Class<?>[] builtins = {SimpleQuartzFactory.class};

    private StartupFinder() {
    }

    private static void loadStartups() {
        // preregister any built in
        for (Class<?> builtin : builtins) {
            if (builtin != null) {
                Class<? extends StartupFactory> b = builtin.asSubclass(StartupFactory.class);
                StartupFactory sf;
                try {
                    sf = b.newInstance();
                } catch (Exception e) {
                    throw new SynapseException("cannot instantiate " + b.getName(), e);

                }
                factoryMap.put(sf.getTagQName(), b);
                serializerMap.put(sf.getTagQName(), sf.getSerializerClass());
            }
        }
        registerExtensions();
        initialized = true;
    }
    private void handleException(String msg) {
        log.error(msg);
        throw new SynapseException(msg);
    }

    /**
     * Register pluggable mediator factories from the classpath
     * <p/>
     * This looks for JAR files containing a META-INF/services that adheres to
     * the following
     * http://java.sun.com/j2se/1.3/docs/guide/jar/jar.html#Service%20Provider
     */
    private static void registerExtensions() {

        // log.debug("Registering mediator extensions found in the classpath : "
        // + System.getResource("java.class.path"));

        // register MediatorFactory extensions
        Iterator<?> it = Service.providers(StartupFactory.class);
        while (it.hasNext()) {
            StartupFactory sf = (StartupFactory) it.next();
            QName tag = sf.getTagQName();
            factoryMap.put(tag, sf.getClass());
            serializerMap.put(tag, sf.getSerializerClass());
            if (log.isDebugEnabled()) {
                log.debug("Added StartupFactory " + sf.getClass()
                        + " to handle " + tag);
            }
        }
    }

    /**
     * Check whether an element with the given qualified name defines a startup.
     * 
     * @param name to be identified whether it is a startup or not
     * @return true if there is a startup registered with the factory map in the name, false if not
     */
    public boolean isStartup(QName name) {
        return factoryMap.containsKey(name);
    }

    /**
     * This method returns a Processor given an OMElement. This will be used
     * recursively by the elements which contain processor elements themselves
     * (e.g. rules)
     *
     * @param element configuration for creating the startup
     * @param properties bag of properties with additional information
     * @return Processor
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public Startup getStartup(OMElement element, Properties properties) {

        QName qName = element.getQName();
        if (log.isDebugEnabled()) {
            log.debug("Creating the Startup for : " + qName);
        }

        Class<? extends StartupFactory> cls = factoryMap.get(qName);
        if (cls == null) {
            String msg = "Unknown Startup type referenced by startup element : " + qName;
            log.error(msg);
            throw new SynapseException(msg);
        }

        try {
            StartupFactory sf = cls.newInstance();
            return sf.createStartup(element);

        } catch (InstantiationException e) {
            String msg = "Error initializing configuration factory : " + cls;
            log.error(msg);
            throw new SynapseException(msg, e);

        } catch (IllegalAccessException e) {
            String msg = "Error initializing configuration factory : " + cls;
            log.error(msg);
            throw new SynapseException(msg, e);
        }
    }

    /**
     * This method will serialize the config using the supplied QName (looking
     * up the right class to do it)
     *
     * @param parent  -
     *                Parent OMElement to which the created element will be added if
     *                not null
     * @param startup -
     *                Startup to be serialized
     * @return OMElement startup
     */
    public OMElement serializeStartup(OMElement parent, Startup startup) {

        Class<? extends StartupSerializer> cls = serializerMap.get(startup.getTagQName());
        if (cls == null) {
            String msg = "Unknown startup type referenced by startup element : "
                    + startup.getTagQName();
            log.error(msg);
            throw new SynapseException(msg);
        }

        try {
            StartupSerializer ss = cls.newInstance();
            return ss.serializeStartup(parent, startup);

        } catch (InstantiationException e) {
            String msg = "Error initializing startup serializer: " + cls;
            log.error(msg);
            throw new SynapseException(msg, e);

        } catch (IllegalAccessException e) {
            String msg = "Error initializing startup ser: " + cls;
            log.error(msg);
            throw new SynapseException(msg, e);
        }
    }

    /*
      * This method exposes all the StartupFactories and its Extensions
      */
    public Map<QName,Class<? extends StartupFactory>> getFactoryMap() {
        return factoryMap;
    }

    /*
	 * This method exposes all the StartupSerializers and its Extensions
	 */
    public Map<QName,Class<? extends StartupSerializer>> getSerializerMap() {
        return serializerMap;
    }

    /**
     * Allow the startup finder to act as an XMLToObjectMapper for
     * Startup (i.e. Startup) loaded dynamically from a Registry
     *
     * @param om to build the startup object
     * @param properties bag of properties with additional information
     * @return startup created
     */
    public Startup getObjectFromOMNode(OMNode om, Properties properties) {
        if (om instanceof OMElement) {
            return getStartup((OMElement) om, properties);
        } else {
			handleException("Invalid configuration XML : " + om);
		}
		return null;
	}

}

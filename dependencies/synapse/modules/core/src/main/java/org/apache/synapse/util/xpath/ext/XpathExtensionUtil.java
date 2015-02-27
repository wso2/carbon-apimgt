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
package org.apache.synapse.util.xpath.ext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.config.SynapsePropertiesLoader;
import org.apache.synapse.core.SynapseEnvironment;
import org.jaxen.Function;

import javax.xml.namespace.QName;
import java.util.*;

/**
 * Utility class to support custom xpath context extensions
 */
public class XpathExtensionUtil {
    /**
     * The Synapse extension property for variable context extensions
     */
    private static final String SYNAPSE_XPATH_VARIABLE_EXTENSIONS = "synapse.xpath.var.extensions";

    /**
     * The Synapse extension property for function context extensions
     */
    private static final String SYNAPSE_XPATH_FUNCTION_EXTENSIONS = "synapse.xpath.func.extensions";

    private static final Log log = LogFactory.getLog(XpathExtensionUtil.class);

    /**
     * Get all registered variable context extensions. Synapse will look for synapse.properties
     * property synapse.xpath.var.extensions
     *
     * @return List of Synapse Xpath Variable Context Providers
     */
    public static List<SynapseXpathVariableResolver> getRegisteredVariableExtensions() {
        Properties synapseProps = SynapsePropertiesLoader.loadSynapseProperties();
        String propValue = synapseProps.getProperty(SYNAPSE_XPATH_VARIABLE_EXTENSIONS);
        List<SynapseXpathVariableResolver> extProviders = new
                ArrayList<SynapseXpathVariableResolver>();
        extractProviders(propValue, extProviders);
        return extProviders;
    }

    /**
     * Get all registered function context extensions. Synapse will look for synapse.properties
     * property synapse.xpath.func.extensions
     *
     * @return List of Synapse Xpath Function Context Providers
     */
    public static List<SynapseXpathFunctionContextProvider> getRegisteredFunctionExtensions() {
        Properties synapseProps = SynapsePropertiesLoader.loadSynapseProperties();
        String propValue = synapseProps.getProperty(SYNAPSE_XPATH_FUNCTION_EXTENSIONS);
        List<SynapseXpathFunctionContextProvider> extProviders = new
                ArrayList<SynapseXpathFunctionContextProvider>();
        extractProviders(propValue, extProviders);
        return extProviders;
    }

    /**
     * Populate the set of registered  Extension proiders for a given property
     *
     * @param propValue    either variable/function provider property name
     * @param extProviders a List that will be populated with the registered extensions
     * @param <T>          Variable/Function Context Provider Type
     */
    private static <T> void extractProviders(String propValue, List<T> extProviders) {
        if (propValue != null) {
            String[] observerNames = propValue.split(",");
            for (String observer : observerNames) {
                try {
                    Class clazz = XpathExtensionUtil.class.getClassLoader().
                            loadClass(observer.trim());
                    T obj = (T) clazz.newInstance();
                    extProviders.add(obj);
                } catch (Exception e) {
                    handleException("Error while initializing Synapse Xpath extension providers", e);
                }
            }
        }
    }

    /**
     * Returns a Function Context extension registered for given QName/namespaceURI+prefix+localName
     * combination
     *
     * @param ctxt         Synapse Message Context
     * @param namespaceURI binding namespace in xpath expression
     * @param prefix       binding prefix string in xpath expression
     * @param localName    binding localname string in xpath expression
     * @return jaxen Function object for corresponding extension
     */
    public static Function getFunctionContext(MessageContext ctxt, String namespaceURI, String prefix,
                                              String localName) {
        SynapseEnvironment environment = ctxt.getEnvironment();
        if (environment != null) {
            Map<QName, SynapseXpathFunctionContextProvider> extensions =
                    environment.getXpathFunctionExtensions();
            SynapseXpathFunctionContextProvider functionContextProvider =
                    getMatchingExtensionProvider(extensions, namespaceURI, prefix, localName);
            if (functionContextProvider != null) {
                return initAndReturnXpathFunction(functionContextProvider, ctxt);
            }
        }
        return null;
    }

    /**
     * Returns an object resolved by Variable Context extension registered for given
     * QName/namespaceURI+prefix+localName combination
     *
     * @param ctxt         Synapse Message Context
     * @param namespaceURI binding namespace in xpath expression
     * @param prefix       binding prefix string in xpath expression
     * @param localName    binding localname string in xpath expression
     * @return Object variable resolved by corresponding extension
     */
    public static Object resolveVariableContext(MessageContext ctxt, String namespaceURI,
                                                String prefix, String localName) {
        SynapseEnvironment environment = ctxt.getEnvironment();
        if (environment != null) {
            Map<QName, SynapseXpathVariableResolver> extensions =
                    environment.getXpathVariableExtensions();
            SynapseXpathVariableResolver variableResolver =
                    getMatchingExtensionProvider(extensions, namespaceURI, prefix, localName);
            if (variableResolver != null) {
                return resolveXpathVariable(variableResolver, ctxt);
            }
        }
        return null;
    }

    /**
     * returns the matching Extension provider for a given QName/namespaceURI+prefix+localName
     * combination
     *
     * @param extensionMap registered extension Map for the corresponding extension provider
     * @param namespaceURI binding namespace in xpath expression
     * @param prefix       binding prefix string in xpath expression
     * @param localName    binding localname string in xpath expression
     * @param <T>          Variable/Function Context Provider Type
     * @return matching Extension provider. returns null if no extension is found for the given
     *         combination
     */
    private static <T> T getMatchingExtensionProvider(Map<QName, T> extensionMap,
                                                      String namespaceURI,
                                                      String prefix, String localName) {
        QName subject;
        if (localName != null && prefix != null) {
            subject = new QName(namespaceURI, localName, prefix);
        } else if (localName != null) {
            subject = new QName(namespaceURI, localName);
        } else {
            //can't resolve xpath extensions - invalid combination
            return null;
        }

        Set<QName> qNames = extensionMap.keySet();
        for (QName qName : qNames) {
            //check for a match for the given combination for QName registered
            if (subject.equals(qName)) {
                return extensionMap.get(qName);
            }
        }
        //no match found
        return null;

    }

    /**
     * try to resolve an xpath variable context using the extension given
     *
     * @param variableExt Extension provider for variable contexts
     * @param ctxt        Synapse Message Context
     * @return resolved property/object
     */
    private static Object resolveXpathVariable(SynapseXpathVariableResolver variableExt,
                                               MessageContext ctxt) {
        try {
            return variableExt.resolve(ctxt);
        } catch (Exception e) {
            handleExceptionWarning("Error Invoking Xpath Variable Provider " +
                                   variableExt.getClass().getName(), e);
        }
        return null;
    }

    /**
     * Initializes an returns new Xpath Function
     *
     * @param funcExtProvider extension provider for a Function Context
     * @param ctxt            Synapse Message Context
     * @return Xpath Function instance . returns null if error occurs
     */
    private static Function initAndReturnXpathFunction(
            SynapseXpathFunctionContextProvider funcExtProvider,
            MessageContext ctxt) {
        try {
            return funcExtProvider.getInitializedExtFunction(ctxt);
        } catch (Exception e) {
            handleExceptionWarning("Error Initializing Xpath Function Provider " +
                                   funcExtProvider.getClass().getName(), e);
        }
        return null;
    }

    private static void handleException(String msg, Exception e) {
        log.warn(msg, e);
        throw new SynapseException(msg, e);
    }

    private static void handleExceptionWarning(String msg, Exception e) {
        log.warn(msg, e);
    }
}

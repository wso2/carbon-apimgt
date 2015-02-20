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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.Mediator;
import org.apache.synapse.Nameable;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.SynapseException;
import org.apache.synapse.aspects.AspectConfigurable;
import org.apache.synapse.aspects.AspectConfiguration;

import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Parent class for all the {@link MediatorFactory} implementations
 */
@SuppressWarnings({"UnusedDeclaration"})
public abstract class AbstractMediatorFactory implements MediatorFactory {

    /** the standard log for mediators, will assign the logger for the actual subclass */
    static Log log;
    protected static final QName ATT_NAME    = new QName("name");
    protected static final QName ATT_VALUE   = new QName("value");
    protected static final QName ATT_DESCRIPTION   = new QName("description");
    protected static final QName ATT_XPATH   = new QName("xpath");
    protected static final QName ATT_REGEX   = new QName("regex");
    protected static final QName ATT_SEQUENCE = new QName("sequence");
    protected static final QName ATT_EXPRN   = new QName("expression");
    protected static final QName ATT_KEY     = new QName("key");
    protected static final QName ATT_SOURCE  = new QName("source");
    protected static final QName ATT_TARGET  = new QName("target");
    protected static final QName ATT_ONERROR = new QName("onError");
    protected static final QName ATT_EVAL   = new QName("evaluator");
    protected static final QName ATT_STATS
        = new QName(XMLConfigConstants.STATISTICS_ATTRIB_NAME);
    protected static final QName PROP_Q
        = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "property");
    protected static final QName FEATURE_Q
        = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "feature");
    protected static final QName TARGET_Q
            = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "target");
    protected static final QName DESCRIPTION_Q
            = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "description");

    /**
     * A constructor that makes subclasses pick up the correct logger
     */
    protected AbstractMediatorFactory() {
        log = LogFactory.getLog(this.getClass());
    }

    /**
     * Creates the mediator by looking at the given XML element. This method handles
     * extracting the common information from the respective element. It delegates the mediator
     * specific building to the {@link #createSpecificMediator(org.apache.axiom.om.OMElement,
     * java.util.Properties)} method, which has tobe implemented by the respective mediators</p>
     *
     * <p>This method has been marked as <code>final</code> to avoid mistakenly overwriting
     * this method instead of the {@link #createSpecificMediator(org.apache.axiom.om.OMElement,
     * java.util.Properties)} by the sub classes
     *
     * @param elem configuration element of the mediator to be built
     * @return built mediator using the above element
     */
    public final Mediator createMediator(OMElement elem, Properties properties) {
        Mediator mediator = createSpecificMediator(elem, properties);
        OMElement descElem = elem.getFirstChildWithName(DESCRIPTION_Q);
        if (descElem != null) {
            mediator.setDescription(descElem.getText());
        }
        OMAttribute attDescription = elem.getAttribute(ATT_DESCRIPTION);
        if (attDescription != null) {
            mediator.setShortDescription(attDescription.getAttributeValue());
        }
        return mediator;
    }

    /**
     * Specific mediator factory implementations should implement this method to build the
     * {@link org.apache.synapse.Mediator} by the given XML configuration
     *
     * @param elem configuration element describing the properties of the mediator
     * @param properties bag of properties to pass in any information to the factory
     * @return built mediator of that specific type
     */
    protected abstract Mediator createSpecificMediator(OMElement elem, Properties properties);

    /**
     * This is to Initialize the mediator with the default attributes.
     * 
     * @deprecated This method is deprecated. As of Synapse 1.3, please use
     *             {@link #processAuditStatus(Mediator, OMElement)}
     *
     * @param mediator of which trace state has to be set
     * @param mediatorOmElement from which the trace state is extracted
     */
    @Deprecated
    protected void processTraceState(Mediator mediator, OMElement mediatorOmElement) {
        processAuditStatus(mediator, mediatorOmElement);
    }
    
    /**
     * This is to Initialize the mediator regarding tracing and statistics.
     *
     * @param mediator of which trace state has to be set
     * @param mediatorOmElement from which the trace state is extracted
     * 
     * @since 2.0
     */
    protected void processAuditStatus(Mediator mediator, OMElement mediatorOmElement) {

        OMAttribute trace = mediatorOmElement.getAttribute(
            new QName(XMLConfigConstants.NULL_NAMESPACE, XMLConfigConstants.TRACE_ATTRIB_NAME));

        if (trace != null) {
            String traceValue = trace.getAttributeValue();
            if (traceValue != null) {
                if (traceValue.equals(XMLConfigConstants.TRACE_ENABLE)) {
                    mediator.setTraceState(org.apache.synapse.SynapseConstants.TRACING_ON);
                } else if (traceValue.equals(XMLConfigConstants.TRACE_DISABLE)) {
                    mediator.setTraceState(org.apache.synapse.SynapseConstants.TRACING_OFF);
                }
            }
        }

        String name = null;
        if (mediator instanceof Nameable) {
            name = ((Nameable) mediator).getName();
        }
        if (name == null || "".equals(name)) {
            name = SynapseConstants.ANONYMOUS_SEQUENCE;
        }

        if (mediator instanceof AspectConfigurable) {
            AspectConfiguration configuration = new AspectConfiguration(name);
            ((AspectConfigurable) mediator).configure(configuration);

            OMAttribute statistics = mediatorOmElement.getAttribute(ATT_STATS);
            if (statistics != null) {
                String statisticsValue = statistics.getAttributeValue();
                if (statisticsValue != null) {
                    if (XMLConfigConstants.STATISTICS_ENABLE.equals(statisticsValue)) {
                        configuration.enableStatistics();
                    }
                }
            }
        }
    }

    /**
     * Collect the <tt>name</tt> and <tt>value</tt> attributes from the children
     * with a given QName.
     *  
     * @param elem element to be traversed to find the specified <code>childElementName</code>
     * @param childElementName t be used to extract elements to collect the name value pairs
     * @return collected name value pairs
     */
    protected Map<String, String> collectNameValuePairs(OMElement elem, QName childElementName) {
        Map<String,String> result = new LinkedHashMap<String, String>();
        for (Iterator it = elem.getChildrenWithName(childElementName); it.hasNext(); ) {
            OMElement child = (OMElement)it.next();
            OMAttribute attName = child.getAttribute(ATT_NAME);
            OMAttribute attValue = child.getAttribute(ATT_VALUE);
            if (attName != null && attValue != null) {
                String name = attName.getAttributeValue().trim();
                String value = attValue.getAttributeValue().trim();
                if (result.containsKey(name)) {
                    handleException("Duplicate " + childElementName.getLocalPart()
                            + " with name " + name);
                } else {
                    result.put(name, value);
                }
            } else {
                handleException("Both of the name and value attributes are required for a "
                        + childElementName.getLocalPart());
            }
        }
        return result;
    }

    protected void handleException(String message, Exception e) {
        LogFactory.getLog(this.getClass()).error(message, e);
        throw new SynapseException(message, e);
    }

    protected void handleException(String message) {
        LogFactory.getLog(this.getClass()).error(message);
        throw new SynapseException(message);
    }
}

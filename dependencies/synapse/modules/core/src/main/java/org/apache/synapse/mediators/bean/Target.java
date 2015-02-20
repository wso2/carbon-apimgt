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

package org.apache.synapse.mediators.bean;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMText;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.config.xml.ValueFactory;
import org.apache.synapse.config.xml.ValueSerializer;
import org.apache.synapse.mediators.Value;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.jaxen.JaxenException;

/**
 * Represents a Target. Handles both static property names and dynamic(XPath) keys.
 */
public class Target {

    private static final Log log = LogFactory.getLog(Target.class);

    private Value value;

    /**
     * Creates a new Target from the OMElement
     * @param attributeName Name of the attribute where the property name/ XPath expression is
     * stored.
     * @param element OMElement where the the XPath expression and the namespaces are stored.
     */
    public Target(String attributeName, OMElement element) {
        this.value = new ValueFactory().createValue(attributeName, element);
    }

    /**
     * Inserts the given object into the target specified by the current Target object.
     * @param synCtx Message Context to be enriched with the object.
     * @param object Object to be inserted.
     */
    public void insert(MessageContext synCtx, Object object) {

        if (value.getExpression() != null) {

            SynapseXPath expression = value.getExpression();
            Object targetObj = null;

            try {
                targetObj = expression.selectSingleNode(synCtx);
            } catch (JaxenException e) {
                handleException("Failed to select the target.", e);
            }
            if (targetObj instanceof OMText) {
                Object targetParent = ((OMText) targetObj).getParent();
                if (targetParent != null && targetParent instanceof OMElement) {
                    ((OMElement) targetParent).setText(object == null ? "" : object.toString());
                } else {
                    handleException("Invalid target is specified by the expression: " + expression);
                }
            } else {
                handleException("Invalid target is specified by the expression: " + expression);
            }

        } else if (value.getKeyValue() != null) {

            synCtx.setProperty(value.getKeyValue(), object);

        } else {

            handleException("Invalid target description. " + value);
        }
    }

    /**
     * Serialized this Target object into the given element with the given attribute name.
     *
     * @param attributeName Name of the attribute.
     * @param element Element to serialize this target in to.
     * @return Element after serializing this target.
     */
    public OMElement serializeTarget(String attributeName, OMElement element) {
        return new ValueSerializer().serializeValue(value, attributeName, element);
    }

    private void handleException(String msg) {
        log.error(msg);
        throw new SynapseException(msg);
    }

    private void handleException(String msg, Throwable e) {
        log.error(msg);
        throw new SynapseException(msg, e);
    }
}

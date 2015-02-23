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
package org.apache.synapse.mediators.xquery;

import javax.xml.xquery.XQItemType;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMText;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.config.Entry;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.apache.synapse.util.xpath.SourceXPathSupport;
import org.jaxen.JaxenException;

import javax.xml.namespace.QName;
import java.util.List;

/**
 * The value of the custom variable will be evaluated dynamically.
 * The value is computed by extracting the data from the XML document which will lookup  through the
 * key or  the current SOAP message.
 */

public class MediatorCustomVariable extends MediatorVariable {

    private static final Log log = LogFactory.getLog(MediatorCustomVariable.class);

    /* The key to lookup the xml document from registry */
    private String regKey;
    /* The XPath expression*/
    private SynapseXPath expression;
    /*Lock used to ensure thread-safe lookup of the object from the registry */
    private final Object resourceLock = new Object();

    public MediatorCustomVariable(QName name) {
        super(name);
        // create the default XPath
        try {
            this.expression = new SynapseXPath(SourceXPathSupport.DEFAULT_XPATH);
            this.expression.addNamespace("s11", SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);
            this.expression.addNamespace("s12", SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
        } catch (JaxenException e) {
            handleException("Error creating source XPath expression", e);
        }

    }

    /**
     * To assign a value to variable dynamically
     *
     * @param synCtx The current message context
     */
    public boolean evaluateValue(MessageContext synCtx) {

        if (this.regKey == null) {   // get the node from the current message payload
            this.value = evaluate(synCtx);
            return true;
        } else {
            //Load the XML document from the registry
            boolean reLoad = false;
            boolean hasValueChanged = false;
            Entry dp = synCtx.getConfiguration().getEntryDefinition(this.regKey);
            // if the key refers to a dynamic resource
            if (dp != null && dp.isDynamic()) {
                if (!dp.isCached() || dp.isExpired()) {
                    reLoad = true;
                }
            }
            synchronized (resourceLock) {
                if (reLoad || this.value == null) {
                    hasValueChanged = true;
                    Object o = synCtx.getEntry(this.regKey);
                    if (o != null) {
                        if (!SourceXPathSupport.DEFAULT_XPATH.equals(expression.toString())) {
                            this.value = evaluate(o);
                        } else {
                            this.value = o;
                        }
                    }
                }
            }
            return hasValueChanged;
        }
    }

    /**
     * Return the object to be used for the variable value
     *
     * @param source The source on which will be evaluated the XPath expression
     * @return Return the OMNode to be used for the variable value
     */
    private Object evaluate(Object source) {
        try {
            Object result = expression.evaluate(source);
            if (result instanceof List && !((List) result).isEmpty()) {
                result = ((List) result).get(0);  // Always fetches *only* the first
            }
            if (result instanceof OMNode) {
                //if the type is not document-node(), then get the text value of the node
                if (this.getType() != XQItemType.XQITEMKIND_DOCUMENT
                        && this.getType() != XQItemType.XQITEMKIND_DOCUMENT_ELEMENT
                        && this.getType() != XQItemType.XQITEMKIND_ELEMENT) {

                    int nodeType = ((OMNode) result).getType();
                    if (nodeType == OMNode.TEXT_NODE) {
                        return ((OMText) result).getText();
                    } else if (nodeType == OMNode.ELEMENT_NODE) {
                        return ((OMElement) result).getText();
                    }
                }
                return result;
            } else {
                return result;
            }
        } catch (JaxenException e) {
            handleException("Error evaluating XPath " + expression + " on message" + source);
        }
        return null;
    }

    private void handleException(String msg, Exception e) {
        log.error(msg, e);
        throw new SynapseException(msg, e);
    }

    private void handleException(String msg) {
        log.error(msg);
        throw new SynapseException(msg);
    }

    public void setExpression(SynapseXPath expression) {
        this.expression = expression;
    }

    public void setRegKey(String regKey) {
        this.regKey = regKey;
    }

    public String getRegKey() {
        return regKey;
    }

    public SynapseXPath getExpression() {
        return expression;
    }
}

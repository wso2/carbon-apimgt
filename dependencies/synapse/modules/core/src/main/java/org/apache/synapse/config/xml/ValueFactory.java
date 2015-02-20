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
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.apache.synapse.config.SynapseConfigUtils;
import org.apache.synapse.mediators.Value;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.jaxen.JaxenException;

import javax.xml.namespace.QName;

/**
 * Factory for {@link org.apache.synapse.mediators.Value} instances.
 */
public class ValueFactory {

    private static final Log log = LogFactory.getLog(ValueFactory.class);

    private static final QName ATT_KEY = new QName("key");

    /**
     * Create a key instance
     *
     * @param elem OMElement
     * @return Key
     */
    public Value createValue(String name, OMElement elem) {

        Value key = null;

        OMAttribute attKey = elem.getAttribute(new QName(name));

        if (attKey != null) {
            String attributeValue = attKey.getAttributeValue();
            boolean hasEscape = isEscapedExpression(attributeValue);
            if (!hasEscape && isDynamicKey(attributeValue)) {
                /** dynamic key */
                SynapseXPath synXpath = createSynXpath(elem, attributeValue);
                key = new Value(synXpath);
            } else if (hasEscape) {
                /** escaped expr */
                key = new Value(getEscapedExpression(attributeValue));
                key.setNamespaces(elem);
            } else {
                /** static key */
                key = new Value(attributeValue);
            }
        } else {
            handleException("The 'key' attribute is required for the XSLT mediator");
        }
        return key;
    }
    
    /**
     * Create a key instance
     *
     * @param elem OMElement
     * @return Key
     */
    public Value createTextValue(OMElement elem) {

        Value key = null;

        //OMAttribute attKey = elem.getAttribute(new QName(name));
        String textValue = elem.getText();
        if (textValue != null) {
            boolean hasEscape = isEscapedExpression(textValue);
            if (!hasEscape && isDynamicKey(textValue)) {
                /** dynamic key */
                SynapseXPath synXpath = createSynXpath(elem, textValue);
                key = new Value(synXpath);
            } else if (hasEscape) {
                /** escaped expr */
                key = new Value(getEscapedExpression(textValue));
                key.setNamespaces(elem);
            } else {
                /** static key */
                key = new Value(textValue);
            }
        } else {
            handleException("The 'key' attribute is required for the XSLT mediator");
        }
        return key;
    }


    /**
     * Validate the given key to identify whether it is static or dynamic key
     * If the key is in the {} format then it is dynamic key(XPath)
     * Otherwise just a static key
     *
     * @param keyValue string to validate as a key
     * @return isDynamicKey representing key type
     */
    public boolean isDynamicKey(String keyValue) {
        boolean dynamicKey = false;

        final char startExpression = '{';
        final char endExpression = '}';

        char firstChar = keyValue.charAt(0);
        char lastChar = keyValue.charAt(keyValue.length() - 1);

        if (startExpression == firstChar && endExpression == lastChar) {
            dynamicKey = true;
        }
        return dynamicKey;
    }

    /**
     * we'll focus on the trivial escape ie:- ones that have escape characters at start and end
     */
    private boolean isEscapedExpression(String escapeExpr){
        final char startEscapeChar = '{';
        final char endEscapeChar = '}';
        String expr = escapeExpr.trim();
        if(expr.length() <= 3){
            return false;
        }
        //check if this is a escape expression ie:- {{//m0:getQuote}}
        char firstChar = expr.charAt(0);
        char nextTofirstChar = expr.charAt(1);
        char lastChar = expr.charAt(expr.length() - 1);
        char prevTolastChar = expr.charAt(expr.length() - 2);
        //if starting and ending chars have braces ;
        if (startEscapeChar == firstChar && endEscapeChar == lastChar) {
            return firstChar == nextTofirstChar && lastChar == prevTolastChar;
        }
        return false;
    }

    private String getEscapedExpression(String originalExpr){
        return originalExpr.trim().substring(1, originalExpr.length() - 1);
    }


    /**
     * Create synapse xpath expression
     * {} type user input is used to create real xpath expression
     *
     * @param elem the element
     * @param key xpath expression with {}
     * @return SynapseXpath
     */
    public SynapseXPath createSynXpath(OMElement elem, String key) {
        //derive XPath Expression from key
        String xpathExpr = key.trim().substring(1, key.length() - 1);

        SynapseXPath synapseXPath = null;

        try {
            synapseXPath = SynapseXPathFactory.getSynapseXPath(elem, xpathExpr);
        } catch (JaxenException e) {
            handleException("Can not create Synapse Xpath from given key");
        }

        return synapseXPath;
    }

    /**
     * Handle exceptions
     *
     * @param msg error message
     */
    private void handleException(String msg) {
        log.error(msg);
        throw new SynapseException(msg);
    }

}

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

package org.apache.synapse.util.xpath;

import org.apache.axiom.om.OMNode;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.config.SynapseConfigUtils;
import org.jaxen.JaxenException;

/**
 * Support class for mediators operating on message parts selected by an XPath
 * expression. It handles the default XPath expression
 * <code>s11:Body/child::*[position()=1] | s12:Body/child::*[position()=1]</code>
 * without actually parsing or evaluating it.
 */
public class SourceXPathSupport {
    public static final String DEFAULT_XPATH = "s11:Body/child::*[position()=1] | " +
            "s12:Body/child::*[position()=1]";

    private SynapseXPath xpath;
    private String xpathString;

    /**
     * Get the XPath expression.
     *
     * @return the XPath expression
     */
    public SynapseXPath getXPath() {
        return xpath;
    }

    /**
     * Set the XPath expression.
     *
     * @param xpath the XPath expression
     */
    public void setXPath(SynapseXPath xpath) {
        this.xpath = xpath;
    }

    /**
     * Set the string representation of the XPath expression.
     * Note that this information is not mandatory and only used for debugging
     * purposes. Setting the string representation explicitly may be useful to
     * make sure that debugging messages contain the XPath expression in the
     * exact form as specified by the user.
     *
     * @param xpathString a string representation of the XPath expression
     */
    public void setXPathString(String xpathString) {
        this.xpathString = xpathString;
    }

    /**
     * Get the first node selected by the configured XPath expression.
     * If no XPath expression is set, the first child element of the SOAP body
     * is returned, i.e. in this case the method behaves as if the XPath expression is
     * <code>s11:Body/child::*[position()=1] | s12:Body/child::*[position()=1]</code>.
     *
     * @param synCtx the message context
     * @param synLog
     * @return the first node selected by the XPath expression
     * @throws SynapseException if the evaluation of the XPath expression failed or didn't result in an
     *                          {@link OMNode}
     */
    public OMNode selectOMNode(MessageContext synCtx, SynapseLog synLog) {
        if (xpath == null) {
            return synCtx.getEnvelope().getBody().getFirstElement();
        } else {
            Object result;
            try {
                result = xpath.selectSingleNode(synCtx);
            } catch (JaxenException e) {
                throw new SynapseException("Error evaluating XPath expression : " + xpath, e, synLog);
            }
            if (result instanceof OMNode) {
                return (OMNode) result;
            } else if (result instanceof String) {
                return SynapseConfigUtils.stringToOM((String) result);
            } else {
                throw new SynapseException("The evaluation of the XPath expression "
                        + xpath + " did not result in an OMNode : " + result, synLog);
            }
        }
    }

    /**
     * Get a string representation of the XPath expression for debugging purposes.
     */
    @Override
    public String toString() {
        if (xpathString != null) {
            return xpathString;
        } else if (xpath == null) {
            return DEFAULT_XPATH;
        } else {
            return xpath.toString();
        }
    }
}

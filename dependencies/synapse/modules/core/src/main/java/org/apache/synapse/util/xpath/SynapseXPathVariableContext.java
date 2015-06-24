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

import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.description.AxisBindingOperation;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.description.WSDL20DefaultValueHolder;
import org.apache.axis2.Constants;
import org.apache.axis2.transport.http.util.URIEncoderDecoder;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import org.apache.synapse.mediators.template.TemplateContext;
import org.apache.synapse.util.xpath.ext.XpathExtensionUtil;
import org.jaxen.JaxenException;
import org.jaxen.UnresolvableException;
import org.jaxen.VariableContext;

import java.util.Map;
import java.io.UnsupportedEncodingException;
import java.util.Stack;

/**
 * Jaxen variable context for the XPath variables implicitly exposed by Synapse.
 * It exposes the following variables:
 * <dl>
 *   <dt><tt>body</tt></dt>
 *   <dd>The SOAP 1.1 or 1.2 body element.</dd>
 *   <dt><tt>header</tt></dt>
 *   <dd>The SOAP 1.1 or 1.2 header element.</dd>
 * </dl>
 */
public class SynapseXPathVariableContext implements VariableContext {

    private static final Log log = LogFactory.getLog(SynapseXPathVariableContext.class);

    /** Parent variable context */
    private final VariableContext parent;

    /** MessageContext to be used for the variable resolution */
    private final MessageContext synCtx;

    /** SOAPEnvelope to be used for the variable resolution */
    private final SOAPEnvelope env;

    /**
     * <p>Initializes the <code>SynapseVariableContext</code> with the specified context</p>
     *
     * @param parent the parent variable context
     * @param synCtx context to be initialized for the variable resolution
     */
    public SynapseXPathVariableContext(VariableContext parent, MessageContext synCtx) {
        this.parent = parent;
        this.synCtx = synCtx;
        this.env = synCtx.getEnvelope();
    }

    /**
     * <p>Initializes the <code>SynapseVariableContext</code> with the specified envelope</p>
     *
     * @param parent the parent variable context
     * @param env envelope to be initialized for the variable resolution
     */
    public SynapseXPathVariableContext(VariableContext parent, SOAPEnvelope env) {
        this.parent = parent;
        this.synCtx = null;
        this.env = env;
    }

    /**
     * <p>Initializes the <code>SynapseVariableContext</code> with the specified envelope</p>
     *
     * @param parent the parent variable context
     * @param synCtx Synapse Message context to be initialized for the variable resolution
     * @param env envelope to be initialized for the variable resolution
     */
    public SynapseXPathVariableContext(VariableContext parent, MessageContext synCtx, SOAPEnvelope env) {
        this.parent = parent;
        this.synCtx = synCtx;
        this.env = env;
    }

    /**
     * Gets the variable values resolved from the context. This includes the
     * <dl>
     *   <dt><tt>body</tt></dt>
     *   <dd>The SOAP 1.1 or 1.2 body element.</dd>
     *   <dt><tt>header</tt></dt>
     *   <dd>The SOAP 1.1 or 1.2 header element.</dd>
     * </dl>
     * and the following variable prefixes
     * <dl>
     *   <dt><tt>ctx</tt></dt>
     *   <dd>Prefix for Synapse MessageContext properties</dd>
     *   <dt><tt>axis2</tt></dt>
     *   <dd>Prefix for Axis2 MessageContext properties</dd>
     *   <dt><tt>trp</tt></dt>
     *   <dd>Prefix for the transport headers</dd>
     * </dl>
     * If the variable is unknown, this method attempts to resolve it using
     * the parent variable context.
     *
     * @param namespaceURI namespaces for the variable resolution
     * @param prefix string prefix for the variable resolution
     * @param localName string local name for the variable resolution
     * @return Resolved variable value
     * @throws UnresolvableException if the variable specified does not found
     */
    public Object getVariableValue(String namespaceURI, String prefix, String localName)
        throws UnresolvableException {

        if (namespaceURI == null) {

            if (env != null) {

                if (SynapseXPathConstants.SOAP_BODY_VARIABLE.equals(localName)) {
                    return env.getBody();
                } else if (SynapseXPathConstants.SOAP_HEADER_VARIABLE.equals(localName)) {
                    return env.getHeader();
                }
            }

            if (prefix != null && !"".equals(prefix) && synCtx != null) {

                if (SynapseXPathConstants.MESSAGE_CONTEXT_VARIABLE_PREFIX.equals(prefix)) {

                    return synCtx.getProperty(localName);

                } else if (SynapseXPathConstants.AXIS2_CONTEXT_VARIABLE_PREFIX.equals(prefix)) {

                    return ((Axis2MessageContext)
                        synCtx).getAxis2MessageContext().getProperty(localName);

                } else if (SynapseXPathConstants.FUNC_CONTEXT_VARIABLE_PREFIX.equals(prefix)) {
                    Stack<TemplateContext> functionStack = (Stack) synCtx.getProperty(SynapseConstants.SYNAPSE__FUNCTION__STACK);
                    TemplateContext topCtxt = functionStack.peek();
                    if (topCtxt != null) {
                        Object result = topCtxt.getParameterValue(localName);
                        if (result != null && result instanceof SynapseXPath && env != null) {
                            SynapseXPath expression = (SynapseXPath) topCtxt.getParameterValue(localName);
                            try {
                                return expression.evaluate(env);
                            } catch (JaxenException e) {
                                return null;
                            }
                        } else {
                            return result;
                        }
                    }
                } else if (SynapseXPathConstants.TRANSPORT_VARIABLE_PREFIX.equals(prefix)) {

                    org.apache.axis2.context.MessageContext axis2MessageContext =
                        ((Axis2MessageContext) synCtx).getAxis2MessageContext();
                    Object headers = axis2MessageContext.getProperty(
                        org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);

                    if (headers != null && headers instanceof Map) {
                        Map headersMap = (Map) headers;
                        return headersMap.get(localName);
                    } else {
                        return null;
                    }
                } else if (SynapseXPathConstants.URL_VARIABLE_PREFIX.equals(prefix)) {

                    EndpointReference toEPR = synCtx.getTo();
                    if (toEPR != null) {
                        String completeURL = toEPR.getAddress();
                        AxisBindingOperation axisBindingOperation = (AxisBindingOperation)
                                ((Axis2MessageContext) synCtx).getAxis2MessageContext().getProperty(
                                        Constants.AXIS_BINDING_OPERATION);
                        String queryParameterSeparator = null;
                        if (axisBindingOperation != null) {
                            queryParameterSeparator = (String) axisBindingOperation.getProperty(
                                    WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR);
                        }
                        if (queryParameterSeparator == null) {
                            queryParameterSeparator = WSDL20DefaultValueHolder.
                                    ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR_DEFAULT;
                        }

                        int i = completeURL.indexOf("?");
                        if (i > -1) {
                            String queryString = completeURL.substring(i + 1);

                            if (queryString != null && !queryString.equals("")) {
                                String params[] = queryString.split(queryParameterSeparator);

                                if (params == null || params.length == 0) {
                                    return "";
                                }

                                for (String param : params) {
                                    String temp[] = param.split("=");
                                    if (temp != null && temp.length >= 1) {
                                        if (temp[0].equalsIgnoreCase(localName)) {
                                            try {
                                                return temp.length > 1 ?
                                                        URIEncoderDecoder.decode(temp[1]) : "";
                                            } catch (UnsupportedEncodingException e) {
                                                String msg = "Couldn't decode the URL parameter " +
                                                        "value " + temp[1] +
                                                        " with name " + localName;
                                                log.error(msg, e);
                                                throw new UnresolvableException(
                                                        msg + e.getMessage());
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    return "";
                } else {
                    Object o = synCtx.getProperty(prefix);
                    if (o instanceof Map) {
                        Object valueObject = ((Map) o).get(localName);
                        if (valueObject != null) {
                            return valueObject.toString();
                        }
                    }
                }
            }
        }
        //try resolving using available custom extensions
        Object obj = XpathExtensionUtil.resolveVariableContext(
                synCtx,namespaceURI,prefix,localName);
        if (obj != null) {
            return obj;
        }
        return parent.getVariableValue(namespaceURI, prefix, localName);
    }
}

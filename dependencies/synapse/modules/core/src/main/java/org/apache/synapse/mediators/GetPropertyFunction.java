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

package org.apache.synapse.mediators;

import org.apache.axiom.om.OMText;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.RelatesTo;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.transport.base.BaseConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.config.Entry;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.template.TemplateContext;
import org.apache.synapse.registry.Registry;
import org.jaxen.*;
import org.jaxen.function.StringFunction;

import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;

/**
 * Implements the XPath extension function synapse:get-property(scope,prop-name)
 */
public class GetPropertyFunction implements Function , XPathFunction {

    private static final Log log = LogFactory.getLog(GetPropertyFunction.class);
    private static final Log trace = LogFactory.getLog(SynapseConstants.TRACE_LOGGER);

    public static final String NULL_STRING = "";

    /** Synapse Message context*/
    private final MessageContext synCtx;

    public GetPropertyFunction(MessageContext synCtx) {
        this.synCtx = synCtx;
    }

    public MessageContext getSynCtx() {
        return synCtx;
    }

    /**
     * Returns the string value of the property which is get from the corresponding context to the provided scope .
     * The default scope is used to get property from the synapse message context
     *
     * @param context the context at the point in the expression when the function is called
     * @param args  arguments of the functions 
     * @return The string value of a property
     * @throws FunctionCallException
     */
    public Object call(Context context, List args) throws FunctionCallException {

        if (synCtx == null) {
            if (log.isDebugEnabled()) {
                log.debug("Synapse message context has not been set for the " +
                        "XPath extension function 'synapse:get-property(prop-name)'");
            }
            return null;
        }
        
        boolean traceOn = synCtx.getTracingState() == SynapseConstants.TRACING_ON;
        boolean traceOrDebugOn = traceOn || log.isDebugEnabled();

        if (args == null || args.size() == 0) {
            if (traceOrDebugOn) {
                traceOrDebug(traceOn, "Property key value for lookup is not specified");
            }
            return NULL_STRING;

        } else {
            int size = args.size();
            if (size == 1) {
                return evaluate(
                    XMLConfigConstants.SCOPE_DEFAULT, args.get(0), null, context.getNavigator());

            } else if (size == 2) {
                String argOne = StringFunction.evaluate(args.get(0), context.getNavigator());
                if (argOne != null) {
                    if (!XMLConfigConstants.SCOPE_AXIS2.equals(argOne) && !XMLConfigConstants.SCOPE_OPERATION.equals(argOne) &&
                        !XMLConfigConstants.SCOPE_DEFAULT.equals(argOne) &&
                        !XMLConfigConstants.SCOPE_TRANSPORT.equals(argOne) &&
                            !XMLConfigConstants.SCOPE_REGISTRY.equals(argOne) &&
                            !XMLConfigConstants.SCOPE_FUNC.equals(argOne) &&
                            !XMLConfigConstants.SCOPE_SYSTEM.equals(argOne)) {
                        return evaluate(XMLConfigConstants.SCOPE_DEFAULT, args.get(0),
                            args.get(1), context.getNavigator());
                    } else {
                        return evaluate(args.get(0), args.get(1), null, context.getNavigator());
                    }
                }
            } else if (size == 3) {
                return evaluate(args.get(0), args.get(1), args.get(2), context.getNavigator());
            } else {

                String msg = "Invalid arguments for synapse:get-property(prop-name) 0r  " +
                    "synapse:get-property(scope, prop-name) XPath function ";
                if (traceOn) {
                    trace.error(msg);
                }
                log.error(msg);
                throw new FunctionCallException(msg);
            }
        }
        return NULL_STRING;
    }

    /**
     * Returns the string value of the property using arg one as key and arg two as scope
     *
     * @param scopeObject scope will decide from where property will be picked up from
     *        i.e. axis2, transport, default/synapse
     * @param keyObject the key of the property
     * @param navigator object model which can be used for navigation around
     * @param dateformat The dateformat that need to convert
     * @return The String value of property using arg one as key and arg two as scope
     */
    public Object evaluate(Object scopeObject, Object keyObject, Object dateformat,Navigator navigator) {

        boolean traceOn = synCtx.getTracingState() == SynapseConstants.TRACING_ON;
        boolean traceOrDebugOn = traceOn || log.isDebugEnabled();

        String scope = StringFunction.evaluate(scopeObject, navigator);
        String key = StringFunction.evaluate(keyObject, navigator);

        if (key == null || "".equals(key)) {
            if (traceOrDebugOn) {
                traceOrDebug(traceOn,
                    "property-name should be provided when executing synapse:get-property" +
                    "(scope,prop-name) or synapse:get-property(prop-name) Xpath function");
            }
            return NULL_STRING;
        }

        //Irrespective of the scope ,if the dateformat has provided ,
        // should return the Date according to the provided format

        if (SynapseConstants.SYSTEM_DATE.equals(key)) {
            if (dateformat != null) {
                Format formatter = new SimpleDateFormat(dateformat.toString());
                return formatter.format(new java.util.Date());
            } else {
                Format formatter = new SimpleDateFormat();
                return formatter.format(new java.util.Date());
            }
        }

        //return the current system time as a string , don't care scope
        if (SynapseConstants.SYSTEM_TIME.equals(key)) {

            return Long.toString(System.currentTimeMillis());
        }

        if (XMLConfigConstants.SCOPE_DEFAULT.equals(scope)) {

            if (SynapseConstants.HEADER_TO.equals(key)) {
                EndpointReference toEPR = synCtx.getTo();
                if (toEPR != null) {
                    return toEPR.getAddress();
                } else {
                    return NULL_STRING;
                }
            } else if (SynapseConstants.HEADER_FROM.equals(key)) {
                EndpointReference fromEPR = synCtx.getFrom();
                if (fromEPR != null) {
                    return fromEPR.getAddress();
                } else {
                    return NULL_STRING;
                }
            } else if (SynapseConstants.HEADER_ACTION.equals(key)) {
                String wsaAction = synCtx.getWSAAction();
                if (wsaAction != null) {
                    return wsaAction;
                } else {
                    return NULL_STRING;
                }
            } else if (SynapseConstants.HEADER_FAULT.equals(key)) {
                EndpointReference faultEPR = synCtx.getFaultTo();
                if (faultEPR != null) {
                    return faultEPR.getAddress();
                } else {
                    return NULL_STRING;
                }
            } else if (SynapseConstants.HEADER_REPLY_TO.equals(key)) {
                EndpointReference replyToEPR = synCtx.getReplyTo();
                if (replyToEPR != null) {
                    return replyToEPR.getAddress();
                } else {
                    return NULL_STRING;
                }
            } else if (SynapseConstants.HEADER_RELATES_TO.equals(key)) {
                RelatesTo relatesTo = synCtx.getRelatesTo();
                if (relatesTo != null) {
                    return relatesTo.getValue();
                } else {
                    return NULL_STRING;
                }
            } else if (SynapseConstants.HEADER_MESSAGE_ID.equals(key)) {
                String messageID = synCtx.getMessageID();
                if (messageID != null) {
                    return messageID;
                } else {
                    return NULL_STRING;
                }
            } else if (SynapseConstants.PROPERTY_FAULT.equals(key)) {
                if (synCtx.getEnvelope().hasFault()) {
                    return SynapseConstants.TRUE;
                } else if (synCtx instanceof Axis2MessageContext) {
                    org.apache.axis2.context.MessageContext axis2MessageContext
                            = ((Axis2MessageContext) synCtx).getAxis2MessageContext();
                    if (axis2MessageContext.getProperty(BaseConstants.FAULT_MESSAGE) != null
                            && SynapseConstants.TRUE.equals(
                            axis2MessageContext.getProperty(BaseConstants.FAULT_MESSAGE))) {
                        return SynapseConstants.TRUE;
                    }
                } else {
                    return NULL_STRING;
                }
            } else if (SynapseConstants.PROPERTY_MESSAGE_FORMAT.equals(key)) {
                if (synCtx.isDoingPOX())
                    return SynapseConstants.FORMAT_POX;
                else if (synCtx.isDoingGET())
                    return SynapseConstants.FORMAT_GET;
                else if (synCtx.isSOAP11())
                    return SynapseConstants.FORMAT_SOAP11;
                else
                    return SynapseConstants.FORMAT_SOAP12;
            } else if (SynapseConstants.PROPERTY_OPERATION_NAME.equals(key) ||
                    SynapseConstants.PROPERTY_OPERATION_NAMESPACE.equals(key)) {
                if (synCtx instanceof Axis2MessageContext) {
                    AxisOperation axisOperation
                            = ((Axis2MessageContext) synCtx).getAxis2MessageContext().getAxisOperation();
                    if (axisOperation != null) {
                        if (SynapseConstants.PROPERTY_OPERATION_NAMESPACE.equals(key)) {
                            return axisOperation.getName().getNamespaceURI();
                        } else {
                            return axisOperation.getName().getLocalPart();
                        }
                    }
                }
            } else {
                Object result = synCtx.getProperty(key);
                if (result != null) {
                    return result;
                } else {
                    return synCtx.getEntry(key);
                }
            }
        } else if (XMLConfigConstants.SCOPE_AXIS2.equals(scope)
                && synCtx instanceof Axis2MessageContext) {

            org.apache.axis2.context.MessageContext axis2MessageContext
                    = ((Axis2MessageContext) synCtx).getAxis2MessageContext();
            return axis2MessageContext.getProperty(key);

        } else if (XMLConfigConstants.SCOPE_FUNC.equals(scope)) {
            Stack<TemplateContext> functionStack = (Stack) synCtx.getProperty(SynapseConstants.SYNAPSE__FUNCTION__STACK);
            TemplateContext topCtxt = functionStack.peek();
            if (topCtxt!=null) {
                return topCtxt.getParameterValue(key);
            }
        } else if (XMLConfigConstants.SCOPE_TRANSPORT.equals(scope)
                && synCtx instanceof Axis2MessageContext) {

            org.apache.axis2.context.MessageContext axis2MessageContext
                    = ((Axis2MessageContext) synCtx).getAxis2MessageContext();
            Object headers = axis2MessageContext.getProperty(
                    org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);

            if (headers != null && headers instanceof Map) {
                Map headersMap = (Map) headers;
                return headersMap.get(key);
            }

        } else if(XMLConfigConstants.SCOPE_OPERATION.equals(scope)
                && synCtx instanceof Axis2MessageContext){
        	Axis2MessageContext axis2smc = (Axis2MessageContext) synCtx;
            org.apache.axis2.context.MessageContext axis2MessageCtx =
                    axis2smc.getAxis2MessageContext();
            return axis2smc.getAxis2MessageContext().getOperationContext().getProperty(key);
        }else if (XMLConfigConstants.SCOPE_REGISTRY.equals(scope)) {
            String[] regParam = key.split("@");
            String regPath = null;
            String propName = null;
            if (regParam.length == 2) {
                regPath = regParam[0];
                propName = regParam[1];
            } else if (regParam.length == 1) {
                regPath = regParam[0];
            }

            Entry propEntry = synCtx.getConfiguration().getEntryDefinition(regPath);
            if (propEntry == null) {
                propEntry = new Entry();
                propEntry.setType(Entry.REMOTE_ENTRY);
                propEntry.setKey(key);
            }
            Registry registry = synCtx.getConfiguration().getRegistry();
            if (registry != null) {
                registry.getResource(propEntry, new Properties());
                if (propName != null) {
                    Properties reqProperties = propEntry.getEntryProperties();
                    if (reqProperties != null) {
                        if (reqProperties.get(propName) != null) {
                            return reqProperties.getProperty(propName);
                        }
                    }
                } else if (propEntry.getValue() != null) {
                    if (propEntry.getValue() instanceof OMText) {
                        return ((OMText) propEntry.getValue()).getText();
                    }
                    return propEntry.getValue().toString();
                }
            }
        } else if (XMLConfigConstants.SCOPE_SYSTEM.equals(scope)) {
            String propVal = System.getProperty(key);
            if (propVal != null) {
                return propVal;
            } else {
                if (traceOrDebugOn) {
                    traceOrDebug(traceOn, "System property " + key + " not found");
                }
                return NULL_STRING;
            }

        } else {
            if (traceOrDebugOn) {
                traceOrDebug(traceOn, "Invalid scope : '" + scope + "' has been set for the " +
                        "synapse:get-property(scope,prop-name) XPath function");
            }
        }
        return NULL_STRING;
    }

    private void traceOrDebug(boolean traceOn, String msg) {
        if (traceOn) {
            trace.info(msg);
        }
        if (log.isDebugEnabled()) {
            log.debug(msg);
        }
    }

    /**
     * Wraps jaxon xpath function as a javax.xml.xpath.XPathFunction
     * @param args  List of argument for custom xpath function
     * @return result of xpath evaluation
     * @throws XPathFunctionException
     */
    public Object evaluate(List args) throws XPathFunctionException {
        try {
            return call(new Context(new ContextSupport()) ,args);
        } catch (FunctionCallException e) {
            throw new XPathFunctionException(e);
        }
    }
}


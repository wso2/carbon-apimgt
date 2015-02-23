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

package org.apache.synapse.mediators.builtin;

import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.SynapseException;
import org.apache.synapse.config.xml.SynapsePath;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.config.SynapseConfigUtils;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.util.JavaUtils;
import org.apache.http.protocol.HTTP;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * The property mediator would save(or remove) a named property as a local property of
 * the Synapse Message Context or as a property of the Axis2 Message Context or
 * as a Transport Header.
 * Properties set this way could be extracted through the XPath extension function
 * "synapse:get-property(scope,prop-name)"
 */

public class PropertyMediator extends AbstractMediator {

    /** The Name of the property  */
    private String name = null;
    /** The Value to be set  */
    private Object value = null;
    /** The data type of the value */
    private String type = null;
    /** The XML value to be set */
    private OMElement valueElement = null;
    /** The XPath expr. to get value  */
    private SynapsePath expression = null;
    /** The scope for which decide properties where to go*/
    private String scope = null;
    /** The Action - set or remove */
    public static final int ACTION_SET = 0;
    public static final int ACTION_REMOVE = 1;
    /** Set the property (ACTION_SET) or remove it (ACTION_REMOVE). Defaults to ACTION_SET */
    private int action = ACTION_SET;

    /** Regualar expresion pattern to be evaluated over the property value.
     * Resulting match string will be applied to the property */
    private Pattern pattern;

    /** A pattern can be matched to several parts of the value. Which one to choose.*/
    private int group = 0;

    /**
     * Sets a property into the current (local) Synapse Context or into the Axis Message Context
     * or into Transports Header and removes above properties from the corresponding locations.
     *
     * @param synCtx the message context
     * @return true always
     */
    public boolean mediate(MessageContext synCtx) {

        SynapseLog synLog = getLog(synCtx);

        if (synLog.isTraceOrDebugEnabled()) {
            synLog.traceOrDebug("Start : Property mediator");

            if (synLog.isTraceTraceEnabled()) {
                synLog.traceTrace("Message : " + synCtx.getEnvelope());
            }
        }

        if (action == ACTION_SET) {

            Object resultValue = getResultValue(synCtx);

            // if the result value is a String we can apply a reguar expression to
            // choose part of it
            if (resultValue instanceof String && pattern != null) {
                resultValue = getMatchedValue((String) resultValue, synLog);
            }

            if (synLog.isTraceOrDebugEnabled()) {
                synLog.traceOrDebug("Setting property : " + name + " at scope : " +
                    (scope == null ? "default" : scope) + " to : " + resultValue + " (i.e. " +
                    (value != null ? "constant : " + value :
                          "result of expression : " + expression) + ")");
            }

            if (scope == null || XMLConfigConstants.SCOPE_DEFAULT.equals(scope)) {
                //Setting property into the  Synapse Context
                synCtx.setProperty(name, resultValue);

            } else if (XMLConfigConstants.SCOPE_AXIS2.equals(scope)
                    && synCtx instanceof Axis2MessageContext) {
                //Setting property into the  Axis2 Message Context
                Axis2MessageContext axis2smc = (Axis2MessageContext) synCtx;
                org.apache.axis2.context.MessageContext axis2MessageCtx =
                        axis2smc.getAxis2MessageContext();
                axis2MessageCtx.setProperty(name, resultValue);
                handleSpecialProperties(resultValue, axis2MessageCtx);

            } else if (XMLConfigConstants.SCOPE_CLIENT.equals(scope)
                    && synCtx instanceof Axis2MessageContext) {
                //Setting property into the  Axis2 Message Context client options
                Axis2MessageContext axis2smc = (Axis2MessageContext) synCtx;
                org.apache.axis2.context.MessageContext axis2MessageCtx =
                        axis2smc.getAxis2MessageContext();
                axis2MessageCtx.getOptions().setProperty(name, resultValue);

            } else if (XMLConfigConstants.SCOPE_TRANSPORT.equals(scope)
                    && synCtx instanceof Axis2MessageContext) {
                //Setting Transport Headers
                Axis2MessageContext axis2smc = (Axis2MessageContext) synCtx;
                org.apache.axis2.context.MessageContext axis2MessageCtx =
                        axis2smc.getAxis2MessageContext();
                Object headers = axis2MessageCtx.getProperty(
                        org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);

                if (headers != null && headers instanceof Map) {
                    Map headersMap = (Map) headers;
                    headersMap.put(name, resultValue);
                }
                if (headers == null) {
                    Map headersMap = new HashMap();
                    headersMap.put(name, resultValue);
                    axis2MessageCtx.setProperty(
                            org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS,
                            headersMap);
                }
            }else if(XMLConfigConstants.SCOPE_OPERATION.equals(scope)
                    && synCtx instanceof Axis2MessageContext){
            	//Setting Transport Headers
                Axis2MessageContext axis2smc = (Axis2MessageContext) synCtx;
                org.apache.axis2.context.MessageContext axis2MessageCtx =
                        axis2smc.getAxis2MessageContext();
                axis2smc.getAxis2MessageContext().getOperationContext().setProperty(name, resultValue);
            }

        } else {
            if (synLog.isTraceOrDebugEnabled()) {
                synLog.traceOrDebug("Removing property : " + name +
                    " (scope:" + (scope == null ? "default" : scope) + ")");
            }

            if (scope == null || XMLConfigConstants.SCOPE_DEFAULT.equals(scope)) {
                //Removing property from the  Synapse Context
                Set pros = synCtx.getPropertyKeySet();
                if (pros != null) {
                    pros.remove(name);
                }

            } else if ((XMLConfigConstants.SCOPE_AXIS2.equals(scope) ||
                XMLConfigConstants.SCOPE_CLIENT.equals(scope))
                && synCtx instanceof Axis2MessageContext) {
                
                //Removing property from the Axis2 Message Context
                Axis2MessageContext axis2smc = (Axis2MessageContext) synCtx;
                org.apache.axis2.context.MessageContext axis2MessageCtx =
                        axis2smc.getAxis2MessageContext();
                axis2MessageCtx.removeProperty(name);

            } else if (XMLConfigConstants.SCOPE_TRANSPORT.equals(scope)
                    && synCtx instanceof Axis2MessageContext) {
                // Removing transport headers
                Axis2MessageContext axis2smc = (Axis2MessageContext) synCtx;
                org.apache.axis2.context.MessageContext axis2MessageCtx =
                        axis2smc.getAxis2MessageContext();
                Object headers = axis2MessageCtx.getProperty(
                        org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
                if (headers != null && headers instanceof Map) {
                    Map headersMap = (Map) headers;
                    headersMap.remove(name);
                } else {
                    synLog.traceOrDebug("No transport headers found for the message");
                }
            }
        }
        synLog.traceOrDebug("End : Property mediator");
        return true;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(String value) {
        setValue(value, null);
    }

    /**
     * Set the value to be set by this property mediator and the data type
     * to be used when setting the value. Accepted type names are defined in
     * XMLConfigConstants.DATA_TYPES enumeration. Passing null as the type
     * implies that 'STRING' type should be used.
     *
     * @param value the value to be set as a string
     * @param type the type name
     */
    public void setValue(String value, String type) {
        this.type = type;
        // Convert the value into specified type
        this.value = convertValue(value, type);
    }

    public String getType() {
        return type;
    }

    public OMElement getValueElement() {
        return valueElement;
    }

    public void setValueElement(OMElement valueElement) {
        this.valueElement = valueElement;
    }

    public SynapsePath getExpression() {
        return expression;
    }

    public void setExpression(SynapsePath expression) {
        setExpression(expression, null);
    }

    public void setExpression(SynapsePath expression, String type) {
        this.expression = expression;
        // Save the type information for now
        // We need to convert the result of the expression into this type during mediation
        // A null type would imply 'STRING' type
        this.type = type;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    public int getGroup() {
        return group;
    }

    public void setGroup(int group) {
        this.group = group;
    }

    private Object getResultValue(MessageContext synCtx) {
        if (value != null) {
            return value;
        } else if (valueElement != null) {
            return valueElement;
        } else {
            if(expression != null) {
                return convertValue(expression.stringValueOf(synCtx), type);
            }
        }

        return null;
    }

    private Object convertValue(String value, String type) {
        if (type == null) {
            // If no type is set we simply return the string value
            return value;
        }

        try {
            XMLConfigConstants.DATA_TYPES dataType = XMLConfigConstants.DATA_TYPES.valueOf(type);
            switch (dataType) {
                case BOOLEAN    : return JavaUtils.isTrueExplicitly(value);
                case DOUBLE     : return Double.parseDouble(value);
                case FLOAT      : return Float.parseFloat(value);
                case INTEGER    : return Integer.parseInt(value);
                case LONG       : return Long.parseLong(value);
                case OM         : return SynapseConfigUtils.stringToOM(value);
                case SHORT      : return Short.parseShort(value);
                default         : return value;
            }
        } catch (IllegalArgumentException e) {
            String msg = "Unknown type : " + type + " for the property mediator or the " +
                    "property value cannot be converted into the specified type.";
            log.error(msg, e);
            throw new SynapseException(msg, e);
        }
    }

    /**
     * Apply the Regular expression on to the String value and choose the matching group.
     * If a matching not found same string passed in to this method will be returned.
     * If a pattern is not specified same String passed to this method will be returned.
     *
     * @param value String value against the regular expression is matched
     * @param synLog log
     * @return the matched string group
     */
    private String getMatchedValue(String value, SynapseLog synLog) {

        String matchedValue = value;
        // if we cannot find a match set the value to empty string
        Matcher matcher = pattern.matcher(value);
        if (matcher.matches()) {
            
            if (matcher.groupCount() >= group) {
                matchedValue = matcher.group(group);
            } else {
                if (synLog.isTraceOrDebugEnabled()) {
                    String msg = "Failed to get a match for regx : " +
                            pattern.toString() + " with the property value :" +
                            value + " for group :" + group;
                    synLog.traceOrDebug(msg);
                }
            }
            
        } else {
            if (synLog.isTraceOrDebugEnabled()) {
                String msg = "Unable to find a match for regx : " +
                        pattern.toString() + " with the property value :" + value;
                synLog.traceOrDebug(msg);
            }
            return ""; //if not matched ideally should return empty string
        }
        
        return matchedValue;
    }

    @Override
    public boolean isContentAware() {
        boolean contentAware= false;
    	if (expression != null) {
            contentAware = expression.isContentAware();
        }
        
    	if (XMLConfigConstants.SCOPE_AXIS2.equals(scope) ) {
            //the logic will be determine the contentaware true
            if(org.apache.axis2.Constants.Configuration.MESSAGE_TYPE.equals(name)){
            	contentAware =true;
            }

        } else if(XMLConfigConstants.SCOPE_TRANSPORT.equals(scope)){
        	 //the logic will be determine the contentaware true
            if(HTTP.CONTENT_ENCODING.equals(name)){
            	contentAware =true;
            }
        }
        return contentAware;
    }

    private void handleSpecialProperties(Object resultValue,
                                         org.apache.axis2.context.MessageContext axis2MessageCtx) {
		if (org.apache.axis2.Constants.Configuration.MESSAGE_TYPE.equals(name)) {
			axis2MessageCtx.setProperty(org.apache.axis2.Constants.Configuration.CONTENT_TYPE, resultValue);
			Object o = axis2MessageCtx.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
			Map _headers = (Map) o;
			if (_headers != null) {
				_headers.remove(HTTP.CONTENT_TYPE);
				_headers.put(HTTP.CONTENT_TYPE, resultValue);
			}
		}
    }
}

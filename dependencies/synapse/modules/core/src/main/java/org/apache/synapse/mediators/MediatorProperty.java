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

import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.config.xml.SynapsePath;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.util.xpath.SynapseXPath;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;

/**
 * A mediator property is a name-value or name-expression pair which could be supplied
 * for certain mediators. If expressions are supplied they are evaluated at the runtime
 * against the current message into literal String values.
 */
public class MediatorProperty {
    // TODO: these constants are related to a specific configuration language
    //       and should be moved to a class in the related package
    public static final QName PROPERTY_Q  = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "property");
    public static final QName ATT_NAME_Q  = new QName(XMLConfigConstants.NULL_NAMESPACE, "name");
    public static final QName ATT_VALUE_Q = new QName(XMLConfigConstants.NULL_NAMESPACE, "value");
    public static final QName ATT_EXPR_Q  = new QName(XMLConfigConstants.NULL_NAMESPACE, "expression");
    public static final QName ATT_SCOPE_Q = new QName(XMLConfigConstants.NULL_NAMESPACE, "scope");

    private String name;
    private String value;
    private SynapsePath expression;

    private String scope;

    public MediatorProperty() {}

    /**
     *
     * @param synCtx
     */
    public void evaluate(MessageContext synCtx) {
        String result;
        if (value != null) {
            result = value;
        } else if (expression != null) {
            result = expression.stringValueOf(synCtx);
        } else {
            throw new SynapseException("A value or expression must be specified");
        }

        if (scope == null || XMLConfigConstants.SCOPE_DEFAULT.equals(scope)) {
            synCtx.setProperty(name, result);
        } else if (XMLConfigConstants.SCOPE_AXIS2.equals(scope)) {
            //Setting property into the  Axis2 Message Context
            Axis2MessageContext axis2smc = (Axis2MessageContext) synCtx;
            org.apache.axis2.context.MessageContext axis2MessageCtx =
                    axis2smc.getAxis2MessageContext();
            axis2MessageCtx.setProperty(name, result);
        } else if (XMLConfigConstants.SCOPE_CLIENT.equals(scope)) {
            //Setting property into the  Axis2 Message Context client options
            Axis2MessageContext axis2smc = (Axis2MessageContext) synCtx;
            org.apache.axis2.context.MessageContext axis2MessageCtx =
                    axis2smc.getAxis2MessageContext();
            axis2MessageCtx.getOptions().setProperty(name, result);
        } else if (XMLConfigConstants.SCOPE_TRANSPORT.equals(scope)) {
            //Setting Transport Headers
            Axis2MessageContext axis2smc = (Axis2MessageContext) synCtx;
            org.apache.axis2.context.MessageContext axis2MessageCtx =
                    axis2smc.getAxis2MessageContext();
            Object headers = axis2MessageCtx.getProperty(
                    org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);

            if (headers != null && headers instanceof Map) {
                Map headersMap = (Map) headers;
                headersMap.put(name, result);
            }
            if (headers == null) {
                Map headersMap = new HashMap();
                headersMap.put(name, result);
                axis2MessageCtx.setProperty(
                        org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS,
                        headersMap);
            }
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public SynapsePath getExpression() {
        return expression;
    }

    public void setExpression(SynapsePath expression) {
        this.expression = expression;
    }

    public String getEvaluatedExpression(MessageContext synCtx) {
        return expression.stringValueOf(synCtx);
    }

}

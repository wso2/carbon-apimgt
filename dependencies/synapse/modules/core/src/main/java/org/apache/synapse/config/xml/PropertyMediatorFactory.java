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
import org.apache.synapse.Mediator;
import org.apache.synapse.SynapseException;
import org.apache.synapse.mediators.builtin.PropertyMediator;
import org.jaxen.JaxenException;

import javax.xml.namespace.QName;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * Creates a property mediator through the supplied XML configuration
 * <p/>
 * <pre>
 * &lt;property name="string" [action=set/remove] (value="literal" | expression="xpath")
 * [scope=(axis2 | axis2-client | transport)]/&gt;
 * </pre>
 */
public class PropertyMediatorFactory extends AbstractMediatorFactory {

    private static final QName ATT_SCOPE = new QName("scope");
    private static final QName ATT_ACTION = new QName("action");
    private static final QName ATT_TYPE = new QName("type");
    private static final QName ATT_PATTERN = new QName("pattern");
    private static final QName ATT_GROUP = new QName("group");

    public Mediator createSpecificMediator(OMElement elem, Properties properties) {

        PropertyMediator propMediator = new PropertyMediator();
        OMAttribute name = elem.getAttribute(ATT_NAME);
        OMAttribute value = elem.getAttribute(ATT_VALUE);
        OMAttribute expression = elem.getAttribute(ATT_EXPRN);
        OMAttribute scope = elem.getAttribute(ATT_SCOPE);
        OMAttribute action = elem.getAttribute(ATT_ACTION);
        OMAttribute type = elem.getAttribute(ATT_TYPE);
        OMAttribute pattern = elem.getAttribute(ATT_PATTERN);
        OMAttribute group = elem.getAttribute(ATT_GROUP);

        OMElement valueElement = elem.getFirstElement();

        if (name == null) {
            String msg = "The 'name' attribute is required for the configuration of a property mediator";
            log.error(msg);
            throw new SynapseException(msg);
        } else if ((value == null && valueElement == null && expression == null) &&
                !(action != null && "remove".equals(action.getAttributeValue()))) {
            String msg = "Either a child element or one of 'value', 'expression' attributes is " +
                    "required for a property mediator when action is SET";
            log.error(msg);
            throw new SynapseException(msg);
        }
        
        propMediator.setName(name.getAttributeValue());
        String dataType = null;
        if (type != null) {
            dataType = type.getAttributeValue();
        }

        if (value != null) {
            propMediator.setValue(value.getAttributeValue(), dataType);
        } else if (valueElement != null) {
            propMediator.setValueElement(valueElement);
        } else if (expression != null) {
            try {
                propMediator.setExpression(SynapsePathFactory.getSynapsePath(elem, ATT_EXPRN),
                    dataType);
            } catch (JaxenException e) {
                String msg = "Invalid XPath expression for attribute 'expression' : " +
                        expression.getAttributeValue();
                log.error(msg);
                throw new SynapseException(msg);
            }
        }

        if (pattern != null) {
            propMediator.setPattern(Pattern.compile(pattern.getAttributeValue()));
            if (group != null) {
                int groupValue = Integer.parseInt(group.getAttributeValue());
                if (groupValue >= 0) {
                    propMediator.setGroup(groupValue);
                } else {                    
                    String msg = "group can have a positive value only";
                    log.error(msg);
                    throw new SynapseException(msg);
                }
            }
        } else if (group != null) {
            String msg = "group is only allowed when a pattern is specified";
            log.error(msg);
            throw new SynapseException(msg);
        }

        // The action attribute is optional, if provided and equals to 'remove' the
        // property mediator will act as a property remove mediator
        if (action != null && "remove".equals(action.getAttributeValue())) {
            propMediator.setAction(PropertyMediator.ACTION_REMOVE);
        }
        
        if (scope != null) {
            String valueStr = scope.getAttributeValue();
            if (!XMLConfigConstants.SCOPE_AXIS2.equals(valueStr) &&
                !XMLConfigConstants.SCOPE_TRANSPORT.equals(valueStr) &&
                !XMLConfigConstants.SCOPE_OPERATION.equals(valueStr) &&
                !XMLConfigConstants.SCOPE_DEFAULT.equals(valueStr) &&
                !XMLConfigConstants.SCOPE_CLIENT.equals(valueStr)) {

                String msg = "Only '" + XMLConfigConstants.SCOPE_AXIS2 +
                             "' or '" + XMLConfigConstants.SCOPE_TRANSPORT +
                             "' or '" + XMLConfigConstants.SCOPE_CLIENT +
                             "' or '" + XMLConfigConstants.SCOPE_DEFAULT +
                             "' or '" + XMLConfigConstants.SCOPE_OPERATION +
                             "' values are allowed for attribute scope for a property mediator" +
                             ", Unsupported scope " + valueStr;
                log.error(msg);
                throw new SynapseException(msg);
            }
            propMediator.setScope(valueStr);
        }

        // after successfully creating the mediator
        // set its common attributes such as tracing etc
        processAuditStatus(propMediator, elem);

        return propMediator;
    }

    public QName getTagQName() {
        return PROP_Q;
    }
}

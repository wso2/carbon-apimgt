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

import org.apache.axiom.om.OMElement;
import org.apache.synapse.Mediator;
import org.apache.synapse.mediators.bean.BeanConstants;
import org.apache.synapse.mediators.bean.BeanMediator;
import org.apache.synapse.mediators.bean.Target;

import javax.xml.namespace.QName;
import java.util.Properties;

public class BeanMediatorFactory extends AbstractMediatorFactory {

    private static final QName BEAN_Q
            = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "bean");

    public Mediator createSpecificMediator(OMElement elem, Properties properties) {

        BeanMediator mediator = new BeanMediator();

        String attributeValue;

        attributeValue = elem.getAttributeValue(new QName(BeanConstants.VAR));
        if (attributeValue != null) {
            mediator.setVarName(attributeValue);
        } else {
            handleException("'var' attribute of Bean mediator is required.");
        }

        attributeValue = elem.getAttributeValue(new QName(BeanConstants.ACTION));
        if (attributeValue != null) {
            try {
                switch (BeanMediator.Action.valueOf(attributeValue.toUpperCase())) {
                    case CREATE:
                        populateCreateBeanCase(mediator, elem);
                        break;
                    case REMOVE:
                        mediator.setAction(BeanMediator.Action.REMOVE);
                        break;
                    case SET_PROPERTY:
                        populateSetPropertyCase(mediator, elem);
                        break;
                    case GET_PROPERTY:
                        populateGetPropertyCase(mediator, elem);
                        break;
                    default:
                        assert false;
                }
            } catch (IllegalArgumentException e) {
                handleException("'action' attribute of bean mediator must be set to 'CREATE', " +
                        "'REMOVE', 'SET_PROPERTY' or 'GET_PROPERTY'.");
            }
        } else {
            handleException("'action' attribute of Bean mediator is required.");
        }

        return mediator;
    }

    private void populateCreateBeanCase(BeanMediator mediator, OMElement elem) {

        mediator.setAction(BeanMediator.Action.CREATE);

        String attributeValue;

        attributeValue = elem.getAttributeValue(new QName(BeanConstants.CLASS));
        if (attributeValue != null) {
            try {
                mediator.setClazz(Class.forName(attributeValue.trim()));
            } catch (Exception e) {
                handleException("Error while loading '" + attributeValue + "' class.", e);
            }
        } else {
            handleException("'class' attribute of Bean mediator is required when 'CREATE' action " +
                    "is set.");
        }

        attributeValue = elem.getAttributeValue(new QName(BeanConstants.REPLACE));
        if (attributeValue != null) {
            mediator.setReplace(Boolean.parseBoolean(attributeValue.trim()));
        }

    }

    private void populateSetPropertyCase(BeanMediator mediator, OMElement elem) {

        mediator.setAction(BeanMediator.Action.SET_PROPERTY);

        populatePropertyName(mediator, elem);

        if (elem.getAttributeValue(ATT_VALUE) != null) {
            mediator.setValue(new ValueFactory().createValue(BeanConstants.VALUE, elem));
        } else {
            handleException("'value' attribute of Bean mediator is required when 'SET_PROPERTY' " +
                    "action is set.");
        }
    }

    private void populateGetPropertyCase(BeanMediator mediator, OMElement elem) {

        mediator.setAction(BeanMediator.Action.GET_PROPERTY);

        populatePropertyName(mediator, elem);

        if (elem.getAttributeValue(new QName(BeanConstants.TARGET)) != null) {
            mediator.setTarget(new Target(BeanConstants.TARGET, elem));
        } else {
            handleException("'target' attribute of Bean mediator is required when 'GET_PROPERTY' " +
                    "action is set.");
        }
    }

    private void populatePropertyName(BeanMediator mediator, OMElement elem) {

        String attributeValue;

        attributeValue = elem.getAttributeValue(new QName(BeanConstants.PROPERTY));
        if (attributeValue != null) {
            mediator.setPropertyName(attributeValue);
        } else {
            handleException("'property' attribute of Bean mediator is required when " +
                    "SET/GET_PROPERTY action is set.");
        }
    }

    public QName getTagQName() {
        return BEAN_Q;
    }

}

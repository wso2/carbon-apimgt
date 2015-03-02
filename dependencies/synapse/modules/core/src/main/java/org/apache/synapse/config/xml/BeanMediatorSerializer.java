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

public class BeanMediatorSerializer extends AbstractMediatorSerializer {

    private static final String BEAN = "bean";

    public OMElement serializeSpecificMediator(Mediator m) {

        if (!(m instanceof BeanMediator)) {
            handleException("Unsupported mediator was passed in for serialization: " + m.getType());
            return null;
        }

        BeanMediator mediator = (BeanMediator) m;

        OMElement mediatorElem = fac.createOMElement(BEAN, synNS);
        saveTracingState(mediatorElem, mediator);

        if (mediator.getAction() != null) {
            mediatorElem.addAttribute(fac.createOMAttribute(
                    BeanConstants.ACTION, nullNS, mediator.getAction().toString()));
        } else {
            handleException();
        }

        if (mediator.getVarName() != null) {
            mediatorElem.addAttribute(fac.createOMAttribute(
                    BeanConstants.VAR, nullNS, mediator.getVarName()));
        } else {
            handleException();
        }

        switch (mediator.getAction()) {
            case CREATE:
                serializeCreateBeanCase(mediatorElem, mediator);
                break;
            case SET_PROPERTY:
                serializeSetPropertyCase(mediatorElem, mediator);
                break;
            case GET_PROPERTY:
                serializeGetPropertyCase(mediatorElem, mediator);
                break;
            default:
                assert false;
        }

        return mediatorElem;
    }

    private void serializeCreateBeanCase(OMElement mediatorElem, BeanMediator mediator) {

        if (mediator.getClazz() != null) {
            mediatorElem.addAttribute(fac.createOMAttribute(
                    BeanConstants.CLASS, nullNS, mediator.getClazz().getName()));
        } else {
            handleException();
        }

        if (!mediator.isReplace()) {
            mediatorElem.addAttribute(fac.createOMAttribute(
                    BeanConstants.REPLACE, nullNS, Boolean.toString(false)));
        }
    }

    private void serializeSetPropertyCase(OMElement mediatorElem, BeanMediator mediator) {

        serializePropertyName(mediatorElem, mediator);

        if (mediator.getValue() != null) {
            new ValueSerializer().serializeValue(
                    mediator.getValue(), BeanConstants.VALUE, mediatorElem);
        } else {
            handleException();
        }
    }

    private void serializeGetPropertyCase(OMElement mediatorElem, BeanMediator mediator) {

        serializePropertyName(mediatorElem, mediator);

        if (mediator.getTarget() != null) {
            mediator.getTarget().serializeTarget(BeanConstants.TARGET, mediatorElem);
        } else {
            handleException();
        }
    }

    private void serializePropertyName(OMElement mediatorElem, BeanMediator mediator) {

        if (mediator.getPropertyName() != null) {
            mediatorElem.addAttribute(fac.createOMAttribute(
                    BeanConstants.PROPERTY, nullNS, mediator.getPropertyName()));
        } else {
            handleException();
        }
    }

    public String getMediatorClassName() {
        return BeanMediator.class.getName();
    }

    private void handleException() {
        handleException("Invalid bean mediator was passed in for serialization");
    }
}

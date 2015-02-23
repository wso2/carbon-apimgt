/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.synapse.config.xml;

import org.apache.axiom.om.OMElement;
import org.apache.synapse.Mediator;
import org.apache.synapse.mediators.Value;
import org.apache.synapse.mediators.bean.BeanConstants;
import org.apache.synapse.mediators.bean.enterprise.EJBConstants;
import org.apache.synapse.mediators.bean.enterprise.EJBMediator;

import java.util.List;

public class EJBMediatorSerializer extends AbstractMediatorSerializer {

    private static final String EJB = "ejb";

    public OMElement serializeSpecificMediator(Mediator m) {

        if (!(m instanceof EJBMediator)) {
            handleException("An unsupported mediator was passed in for serialization : " +
                    m.getType());
            return null;
        }

        EJBMediator mediator = (EJBMediator) m;

        OMElement mediatorElem = fac.createOMElement(EJB, synNS);
        saveTracingState(mediatorElem, mediator);

        if (mediator.getBeanstalkName() != null) {
            mediatorElem.addAttribute(fac.createOMAttribute(
                    EJBConstants.BEANSTALK, nullNS, mediator.getBeanstalkName()));
        } else {
            handleException();
        }

        if (mediator.getClassName() != null) {
            mediatorElem.addAttribute(fac.createOMAttribute(
                    BeanConstants.CLASS, nullNS, mediator.getClassName()));
        } else {
            handleException();
        }

        if (mediator.getBeanId() != null) {
            mediatorElem.addAttribute(fac.createOMAttribute(
                    EJBConstants.STATEFUL, nullNS, Boolean.toString(true)));
            new ValueSerializer().serializeValue(
                    mediator.getBeanId(), EJBConstants.BEAN_ID, mediatorElem);
        }

        if (mediator.getMethod() != null) {
            mediatorElem.addAttribute(fac.createOMAttribute(
                    EJBConstants.METHOD, nullNS, mediator.getMethod().getName()));
        } else if (!mediator.isRemove()) {
            handleException();
        }

        if (mediator.getTarget() != null) {
            mediator.getTarget().serializeTarget(BeanConstants.TARGET, mediatorElem);
        }

        if (mediator.getJndiName() != null) {
            mediatorElem.addAttribute(fac.createOMAttribute(
                    EJBConstants.JNDI_NAME, nullNS, mediator.getJndiName()));
        }

        if (mediator.isRemove()) {
            mediatorElem.addAttribute(fac.createOMAttribute(
                    EJBConstants.REMOVE, nullNS, Boolean.toString(true)));
        }

        List<Value> argList = mediator.getArgumentList();

        if (argList != null && argList.size() > 0) {

            OMElement argumentsElem = fac.createOMElement(EJBConstants.ARGS, synNS);

            for (Value arg : argList) {
                OMElement argElem = fac.createOMElement(EJBConstants.ARG, synNS);
                new ValueSerializer().serializeValue(arg, BeanConstants.VALUE, argElem);
                argumentsElem.addChild(argElem);
            }

            mediatorElem.addChild(argumentsElem);
        }

        return mediatorElem;
    }

    public String getMediatorClassName() {
        return EJBMediator.class.getName();
    }

    private void handleException() {
        handleException("Invalid ejb mediator was passed in for serialization.");
    }
}

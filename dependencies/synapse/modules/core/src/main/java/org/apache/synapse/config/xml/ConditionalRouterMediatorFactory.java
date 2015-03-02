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
import org.apache.axis2.util.JavaUtils;
import org.apache.synapse.Mediator;
import org.apache.synapse.commons.evaluators.Evaluator;
import org.apache.synapse.commons.evaluators.EvaluatorException;
import org.apache.synapse.commons.evaluators.config.EvaluatorFactoryFinder;
import org.apache.synapse.mediators.eip.Target;
import org.apache.synapse.mediators.filters.router.ConditionalRouterMediator;
import org.apache.synapse.mediators.filters.router.ConditionalRoute;

import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.Properties;

/**
 * <pre>
 *  &lt;conditionalRouter continueAfter="(true|false)"&gt;
 *   &lt;route breakRoute="(true|false)"&gt;
 *     &lt;condition ../&gt;
 *     &lt;target ../&gt;
 *   &lt;/route&gt;
 *  &lt;/conditionalRouter&gt;
 * </pre>
 */
public class ConditionalRouterMediatorFactory extends AbstractMediatorFactory {

    private static final QName CONDITIONAL_ROUTER_Q
            = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "conditionalRouter");
    private static final QName ROUTE_Q
            = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "conditionalRoute");
    private static final QName CONDITION_Q
            = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "condition");
    private static final QName TARGET_Q
            = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "target");

    private static final QName CONTINUE_AFTER_ATTR
            = new QName(XMLConfigConstants.NULL_NAMESPACE, "continueAfter");
    private static final QName BREAK_ROUTE_ATTR
            = new QName(XMLConfigConstants.NULL_NAMESPACE, "breakRoute");
    private static final QName ASYNCHRONOUS_ATTR
            = new QName(XMLConfigConstants.NULL_NAMESPACE, "asynchronous");
    
    public Mediator createSpecificMediator(OMElement elem, Properties properties) {

        ConditionalRouterMediator conditionalRouterMediator = new ConditionalRouterMediator();
        processAuditStatus(conditionalRouterMediator, elem);

        if (elem.getAttribute(CONTINUE_AFTER_ATTR) != null) {
            if (JavaUtils.isTrueExplicitly(elem.getAttributeValue(CONTINUE_AFTER_ATTR).trim())) {
                conditionalRouterMediator.setContinueAfter(true);
            } else if (JavaUtils.isFalseExplicitly(
                    elem.getAttributeValue(CONTINUE_AFTER_ATTR).trim())) {
                conditionalRouterMediator.setContinueAfter(false);
            } else {
                handleException("continueAfter attribute value of the conditionalRouter must " +
                        "be either 'true' or 'false', the value found is : "
                        + elem.getAttributeValue(CONTINUE_AFTER_ATTR).trim());
            }
        }

        Iterator itr = elem.getChildrenWithName(ROUTE_Q);
        while (itr.hasNext()) {
            OMElement routeElem = (OMElement) itr.next();
            ConditionalRoute conditionalRoute = new ConditionalRoute();

            if (routeElem.getAttribute(BREAK_ROUTE_ATTR) != null) {
                if (JavaUtils.isTrueExplicitly(
                        routeElem.getAttributeValue(BREAK_ROUTE_ATTR).trim())) {

                    conditionalRoute.setBreakRoute(true);
                } else if (JavaUtils.isFalseExplicitly(
                        routeElem.getAttributeValue(BREAK_ROUTE_ATTR).trim())) {

                    conditionalRoute.setBreakRoute(false);
                } else {
                    handleException("breakRoute attribute value of the conditionalRoute element must " +
                            "be either 'true' or 'false', the value found is : "
                            + routeElem.getAttributeValue(BREAK_ROUTE_ATTR).trim());
                }
            }

            OMElement conditionElem = routeElem.getFirstChildWithName(CONDITION_Q);
            if (conditionElem == null) {
                handleException("Couldn't find the condition of the conditional router");
                return null;
            }

            try {
                Evaluator evaluator = EvaluatorFactoryFinder.getInstance().getEvaluator(
                        conditionElem.getFirstElement());
                conditionalRoute.setEvaluator(evaluator);
            } catch (EvaluatorException ee) {
                handleException("Couldn't build the condition of the conditional router", ee);
            }

            OMElement targetElem = routeElem.getFirstChildWithName(TARGET_Q);
            Target target = TargetFactory.createTarget(targetElem, properties);
            if (JavaUtils.isTrueExplicitly(routeElem.getAttributeValue(ASYNCHRONOUS_ATTR))) {
                target.setAsynchronous(true);
            } else {
                target.setAsynchronous(false);
            }
            conditionalRoute.setTarget(target);
            conditionalRouterMediator.addRoute(conditionalRoute);
        }
        return conditionalRouterMediator;
    }

    public QName getTagQName() {
        return CONDITIONAL_ROUTER_Q;
    }
}

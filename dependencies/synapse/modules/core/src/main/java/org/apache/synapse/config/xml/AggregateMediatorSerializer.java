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
import org.apache.synapse.mediators.eip.aggregator.AggregateMediator;

/**
 * Serializer for {@link AggregateMediator} instances.
 * 
 * @see AggregateMediatorFactory
 */
public class AggregateMediatorSerializer extends AbstractMediatorSerializer {

    public OMElement serializeSpecificMediator(Mediator m) {

        AggregateMediator mediator = null;
        if (!(m instanceof AggregateMediator)) {
            handleException("Unsupported mediator passed in for serialization : " + m.getType());
        } else {
            mediator = (AggregateMediator) m;
        }

        assert mediator != null;
        OMElement aggregator = fac.createOMElement("aggregate", synNS);
        saveTracingState(aggregator, mediator);

        if (mediator.getId() != null) {
            aggregator.addAttribute("id", mediator.getId(), nullNS);
        }

        if (mediator.getCorrelateExpression() != null) {
            OMElement corelateOn = fac.createOMElement("correlateOn", synNS);
            SynapseXPathSerializer.serializeXPath(
                mediator.getCorrelateExpression(), corelateOn, "expression");
            aggregator.addChild(corelateOn);
        }

        OMElement completeCond = fac.createOMElement("completeCondition", synNS);
        if (mediator.getCompletionTimeoutMillis() != 0) {
            completeCond.addAttribute("timeout",
                    Long.toString(mediator.getCompletionTimeoutMillis() / 1000), nullNS);
        }
        OMElement messageCount = fac.createOMElement("messageCount", synNS);
        if (mediator.getMinMessagesToComplete() != null) {
       	   new ValueSerializer().serializeValue(
                    mediator.getMinMessagesToComplete(), "min", messageCount);
        }
        if (mediator.getMaxMessagesToComplete() != null) {
        	new ValueSerializer().serializeValue(
        	        mediator.getMaxMessagesToComplete(), "max", messageCount);
        }
        completeCond.addChild(messageCount);
        aggregator.addChild(completeCond);

        OMElement onCompleteElem = fac.createOMElement("onComplete", synNS);
        if (mediator.getAggregationExpression() != null) {
            SynapseXPathSerializer.serializeXPath(
                mediator.getAggregationExpression(), onCompleteElem, "expression");
        }
        if (mediator.getOnCompleteSequenceRef() != null) {
            onCompleteElem.addAttribute("sequence", mediator.getOnCompleteSequenceRef(), nullNS);
        } else if (mediator.getOnCompleteSequence() != null) {
            new SequenceMediatorSerializer().serializeChildren(
                    onCompleteElem, mediator.getOnCompleteSequence().getList());
        }

        String enclosingElementPropertyName = mediator.getEnclosingElementPropertyName();
        if (enclosingElementPropertyName != null) {
            onCompleteElem.addAttribute("enclosingElementProperty", enclosingElementPropertyName,nullNS);
        }
        aggregator.addChild(onCompleteElem);

        return aggregator;
    }

    public String getMediatorClassName() {
        return AggregateMediator.class.getName();
    }
}

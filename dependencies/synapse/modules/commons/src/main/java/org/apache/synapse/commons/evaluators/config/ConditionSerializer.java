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

package org.apache.synapse.commons.evaluators.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.commons.evaluators.Condition;
import org.apache.synapse.commons.evaluators.EvaluatorConstants;
import org.apache.synapse.commons.evaluators.EvaluatorException;

import javax.xml.namespace.QName;

/**
 * Serialize the {@link Condition} to the XML configuration defined in
 * the {@link ConditionFactory}. 
 */
public class ConditionSerializer {
    private static Log log = LogFactory.getLog(ConditionSerializer.class);

    private static final OMFactory fac = OMAbstractFactory.getOMFactory();

    private static final OMNamespace nullNS = fac.createOMNamespace("", "");

    public OMElement serializer(OMElement parent, Condition condition) throws EvaluatorException {
        OMElement conditionElement = fac.createOMElement(new QName(EvaluatorConstants.CONDITION));

        conditionElement.addAttribute(
                fac.createOMAttribute(EvaluatorConstants.PRIORITY, nullNS,
                        condition.getPriority() + ""));

        EvaluatorSerializer serializer =
                EvaluatorSerializerFinder.getInstance().getSerializer(
                        condition.getEvaluator().getName());

        if (serializer != null) {
            serializer.serialize(conditionElement, condition.getEvaluator());
        } else {
            String msg = "Couldn't find the serializer for evaliator: " +
                    condition.getEvaluator().getName();
            log.error(msg);
            throw new EvaluatorException(msg);
        }

        if (parent != null) {
            parent.addChild(conditionElement);
        }

        return conditionElement;
    }
}

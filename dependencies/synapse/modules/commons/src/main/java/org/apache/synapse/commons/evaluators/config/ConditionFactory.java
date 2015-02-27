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

import org.apache.synapse.commons.evaluators.Condition;
import org.apache.synapse.commons.evaluators.EvaluatorException;
import org.apache.synapse.commons.evaluators.Evaluator;
import org.apache.synapse.commons.evaluators.EvaluatorConstants;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMAttribute;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import javax.xml.namespace.QName;

/**
 * This factory creates a {@link Condition} using the following XML configuration.</p>
 * <pre>
 * &lt;condition priority=&quot;priority value as an integer&quot;&gt;
 *           one evaluator, this evaluator can contain other evaluators
 * &lt;/condition&gt;</pre>
 */
public class ConditionFactory {
    private Log log = LogFactory.getLog(ConditionFactory.class);

    public Condition createCondition(OMElement ruleElement) throws EvaluatorException {
        Condition r = new Condition();

        OMAttribute priorityAtt = ruleElement.getAttribute(new QName(EvaluatorConstants.PRIORITY));

        if (priorityAtt != null) {
            int p = Integer.parseInt(priorityAtt.getAttributeValue());
            r.setPriority(p);
        }

        OMElement ce = ruleElement.getFirstElement();

        EvaluatorFactory ef = EvaluatorFactoryFinder.getInstance().
                findEvaluatorFactory(ce.getLocalName());

        if (ef == null) {
            handleException("Invalid configuration element: " + ce.getLocalName());
            return null;
        }

        Evaluator evaluator = ef.create(ce);

        r.setEvaluator(evaluator);
        return r;
    }

    private void handleException(String message) throws EvaluatorException {
        log.error(message);
        throw new EvaluatorException(message);
    }
}

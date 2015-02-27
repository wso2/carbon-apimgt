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

package org.apache.synapse.commons.evaluators;

import org.apache.axiom.om.OMElement;
import org.apache.synapse.commons.evaluators.config.ConditionFactory;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

/**
 * This class is used to parse a Given HTTP request against a set of rules.</p>
 * <p>
 * A Rule has a priority. If a HTTP request matches the Rule, parser returns
 * the priority corresponding to that rule.</p>
 * <p>
 * Here is the syntax of the configuration used to building the parser</p>
 * <pre>
 * &lt;conditions [defualtPriority = &quot;int&quot;]&gt;
 *     &lt;condition priority = &quot;&quot;&gt;
 *        &lt;and/&gt; | &lt;or&gt; | &lt;not&gt; | &lt;match&gt; | &lt;equal&gt;
 *     &lt;/condition&gt;
 * &lt;/conditions&gt;
 * </pre>
 */
@SuppressWarnings({"UnusedDeclaration"})
public class Parser {
    private Log log = LogFactory.getLog(Parser.class);

    /** set of conditions to be evaluated */
    private Condition[] conditions = null;

    /** Default priority to be used */
    private int defaultPriority = -1;

    /**
     * Create a parser with the defualt priority set to -1. If a HTTP message
     * doesn't obey any of the conditions parser will return -1.     
     */
    public Parser() {
    }

    /**
     * Create a parser with a default priority. If none of the rules matches the
     * given HTTP request, it returns the default priority.
     *
     * @param defaultPriority default priority
     */
    public Parser(int defaultPriority) {
        this.defaultPriority = defaultPriority;
    }

    /**
     * Parse the HTTP request against the condition set and return the matching priority.
     *
     * @param context context used for holding the HTTP information
     * @return priority as an integer
     */
    public int parse(EvaluatorContext context) {
        for (Condition condition : conditions) {
            try {
                if (condition.getEvaluator().evaluate(context)) {
                    return condition.getPriority();
                }
            } catch (EvaluatorException e) {
                String msg = "Error evaluating the "
                        + EvaluatorConstants.CONDITION + " with priority :"
                        + condition.getPriority();
                if (defaultPriority == -1) {
                    log.error(msg, e);
                }
                return defaultPriority;
            }
        }

        return defaultPriority;
    }

    /**
     * Build the parser from a given XML
     *
     * @param conditions set of conditions
     * @throws EvaluatorException if the configuration is invalid 
     */
    public void init(OMElement conditions) throws EvaluatorException {
        Iterator it = conditions.getChildElements();

        ConditionFactory rf = new ConditionFactory();
        List<Condition> conditionList = new ArrayList<Condition>();
        while (it.hasNext()) {
            OMElement conditionElement = (OMElement) it.next();

            if (!conditionElement.getLocalName().equals(EvaluatorConstants.CONDITION)) {
                handleException("Only " + EvaluatorConstants.CONDITION + " elements expected");
            }

            Condition r = null;
            try {
                r = rf.createCondition(conditionElement);
            } catch (EvaluatorException e) {
                handleException("Error creating " +
                        EvaluatorConstants.CONDITION + ": " + e.getMessage());
            }

            conditionList.add(r);
        }

        if (conditionList.size() > 1) {
            this.conditions = conditionList.toArray(new Condition[conditionList.size()]);
        } else if (conditionList.size() == 1 && defaultPriority == -1){
            handleException("No point in having one rule without a default priority");
        } else {
            handleException("One or more " +
                    EvaluatorConstants.CONDITION + "s should ve specified");
        }
    }

    private void handleException(String message) throws EvaluatorException {
        log.error(message);
        throw new EvaluatorException(message);
    }
}


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

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.commons.evaluators.Evaluator;
import org.apache.synapse.commons.evaluators.EvaluatorConstants;
import org.apache.synapse.commons.evaluators.EvaluatorException;

import java.util.Map;
import java.util.HashMap;

/**
 * Factories for creating Evaluators are found using the evaluator name. This class stores
 * information about Evaluator Factories and their corresponding names. This is a Singleton class.
 */
public class EvaluatorFactoryFinder {
    private static final Log log = LogFactory.getLog(EvaluatorFactoryFinder.class);

    private static final EvaluatorFactoryFinder finder = new EvaluatorFactoryFinder();

    private Map<String, EvaluatorFactory> factories = new HashMap<String, EvaluatorFactory>();

    private EvaluatorFactoryFinder() {
        factories.put(EvaluatorConstants.AND, new AndFactory());
        factories.put(EvaluatorConstants.OR, new OrFactory());
        factories.put(EvaluatorConstants.NOT, new NotFactory());
        factories.put(EvaluatorConstants.MATCH, new MatchFactory());
        factories.put(EvaluatorConstants.EQUAL, new EqualFactory());
    }

    /**
     * Return and instance of the <code>EvaluatorFactoryFinder</code>.
     * @return the EvaluatorFactoryFinder singleton
     */
    public static EvaluatorFactoryFinder getInstance() {
        return finder;
    }

    /**
     * Retun an <code>EvaluatorFactory</code> for a given Evaluator name.
     * @param name name of the Evaluator
     * @return an EvaluatorFactory
     */
    public EvaluatorFactory findEvaluatorFactory(String name) {
        return factories.get(name);
    }

    /**
     * Retun an <code>EvaluatorFactory</code> for a given Evaluator name.
     * @param elem A XML element containing the evaluator configuration
     * @return an Evaluator
     * @throws org.apache.synapse.commons.evaluators.EvaluatorException if it cannot find
     * a corresponding factory for creating an evaluator
     */
    public Evaluator getEvaluator(OMElement elem) throws EvaluatorException {
        
        EvaluatorFactory fac = findEvaluatorFactory(elem.getLocalName());
        if (fac == null) {
            handleException("Invalid configuration element: " + elem.getLocalName());
        } else {
            return fac.create(elem);
        }

        return null;
    }

    private void handleException(String message) throws EvaluatorException {
        log.error(message);
        throw new EvaluatorException(message);
    }
}

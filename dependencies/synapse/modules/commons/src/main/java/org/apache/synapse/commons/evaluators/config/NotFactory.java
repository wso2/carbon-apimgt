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

import org.apache.synapse.commons.evaluators.Evaluator;
import org.apache.synapse.commons.evaluators.EvaluatorException;
import org.apache.synapse.commons.evaluators.NotEvaluator;
import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This Factory creates a {@link NotEvaluator} from the following XML configuration.</p>   
 *
 * <pre>
 * &lt;not&gt;
 *     one evaluator
 * &lt;/not&gt;
 * </pre>
 */
public class NotFactory implements EvaluatorFactory {
    private Log log = LogFactory.getLog(NotFactory.class);

    public Evaluator create(OMElement e) throws EvaluatorException {
        NotEvaluator not = new NotEvaluator();

        OMElement ce = e.getFirstElement();

        EvaluatorFactory ef = EvaluatorFactoryFinder.getInstance().
                findEvaluatorFactory(ce.getLocalName());

        if (ef == null) {
            handleException("Invalid configuration element: " + ce.getLocalName());
            return null;
        }

        Evaluator evaluator = ef.create(ce);

        not.setEvaluator(evaluator);

        return not;
    }

    private void handleException(String message) throws EvaluatorException {
        log.error(message);
        throw new EvaluatorException(message);
    }
}

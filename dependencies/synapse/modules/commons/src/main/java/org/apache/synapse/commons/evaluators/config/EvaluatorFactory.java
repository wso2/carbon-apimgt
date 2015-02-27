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
import org.apache.axiom.om.OMElement;

/**
 * This should be implemented by the Factories creating evaluators.
 */
public interface EvaluatorFactory {

    /**
     * Create an evalautor from the given XML configuration
     *
     * @param e OMElement having the evaluator XML configuration
     * @return an evaluator
     * @throws EvaluatorException if a syntax error is there
     */
    Evaluator create(OMElement e) throws EvaluatorException;

}

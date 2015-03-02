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

/**
 * This is the base interface for evaluating boolean expressions. It executes a boolean
 * expression and return true or false based on the result of executing the boolean expression.
 */
public interface Evaluator {
    /**
     * Evaluate a boolean expression
     *
     * @param context hold the information about the HTTP request
     *
     * @return result of evaluating the boolean expression     
     * @throws EvaluatorException if an error occurs while evaluating
     * the HTTP request
     */
    boolean evaluate(EvaluatorContext context) throws EvaluatorException;

    /**
     * Name of the evaluator
     * @return name of the evaluator
     */
    String getName();
}

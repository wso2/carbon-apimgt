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
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.synapse.commons.evaluators.Evaluator;
import org.apache.synapse.commons.evaluators.EvaluatorException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Provide common methods for {@link EvaluatorSerializer} implementations
 */
public abstract class AbstractEvaluatorSerializer implements EvaluatorSerializer {

    protected Log log;

    protected static final OMFactory fac = OMAbstractFactory.getOMFactory();

    protected static final OMNamespace nullNS = fac.createOMNamespace("", "");
    
    /**
     * A constructor that makes subclasses pick up the correct logger
     */
    protected AbstractEvaluatorSerializer() {
        log = LogFactory.getLog(this.getClass());
    }

    protected void serializeChildren(OMElement parent, Evaluator[] childEvaluators)
            throws EvaluatorException {
        for (Evaluator evaluator : childEvaluators) {
            String name = evaluator.getName();

            EvaluatorSerializer serializer = EvaluatorSerializerFinder.
                    getInstance().getSerializer(name);

            if (serializer != null) {
                serializer.serialize(parent, evaluator);
            } else {
                String msg = "Couldn't find the serializer for evaliator: " + name;
                log.error(msg);
                throw new EvaluatorException(msg);
            }
        }
    }

    protected void serializeChild(OMElement parenet, Evaluator child) throws EvaluatorException {
        EvaluatorSerializer serializer =
                EvaluatorSerializerFinder.getInstance().getSerializer(child.getName());

        if (serializer != null) {
            serializer.serialize(parenet, child);
        } else {
            String msg = "Couldn't find the serializer for evaliator: " + child.getName();
            log.error(msg);
            throw new EvaluatorException(msg);
        }
    }
}

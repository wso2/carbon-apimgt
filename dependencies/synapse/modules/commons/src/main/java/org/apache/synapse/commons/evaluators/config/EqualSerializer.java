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
import org.apache.synapse.commons.evaluators.*;

/**
 * Serialize the {@link EqualEvaluator} to the XML configuration defined in
 * the {@link EqualFactory}. 
 */
public class EqualSerializer extends TextProcessingEvaluatorSerializer {

    public OMElement serialize(OMElement parent, Evaluator evaluator) throws EvaluatorException {

        if (!(evaluator instanceof EqualEvaluator)) {
            throw new IllegalArgumentException("Evalutor must be an EqualEvalutor");
        }

        EqualEvaluator equalEvaluator = (EqualEvaluator) evaluator;
        OMElement equalElement  = fac.createOMElement(EvaluatorConstants.EQUAL, EvaluatorConstants.SYNAPSE_NAMESPACE, EvaluatorConstants.EMPTY_PREFIX);
        serializeSourceTextRetriever(equalEvaluator.getTextRetriever(), equalElement);

        equalElement.addAttribute(fac.createOMAttribute(EvaluatorConstants.VALUE, nullNS,
                equalEvaluator.getValue()));

        if (parent != null) {
            parent.addChild(equalElement);
        }

        return equalElement;
    }
}

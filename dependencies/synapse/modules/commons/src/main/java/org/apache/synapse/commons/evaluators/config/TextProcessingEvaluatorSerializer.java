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

import org.apache.synapse.commons.evaluators.source.*;
import org.apache.synapse.commons.evaluators.EvaluatorConstants;
import org.apache.synapse.commons.evaluators.EvaluatorException;
import org.apache.axiom.om.OMElement;

public abstract class TextProcessingEvaluatorSerializer extends AbstractEvaluatorSerializer {

    protected void serializeSourceTextRetriever(SourceTextRetriever textRetriever,
                                                OMElement element) throws EvaluatorException {

        if (textRetriever instanceof HeaderTextRetriever) {
            element.addAttribute(fac.createOMAttribute(EvaluatorConstants.TYPE, nullNS,
                    EvaluatorConstants.HEADER));
            addSourceAttribute(textRetriever.getSource(), element);

        } else if (textRetriever instanceof ParameterTextRetriever) {
            element.addAttribute(fac.createOMAttribute(EvaluatorConstants.TYPE, nullNS,
                    EvaluatorConstants.PARAM));
            addSourceAttribute(textRetriever.getSource(), element);

        } else if (textRetriever instanceof PropertyTextRetriever) {
            element.addAttribute(fac.createOMAttribute(EvaluatorConstants.TYPE, nullNS,
                    EvaluatorConstants.PROPERTY));
            addSourceAttribute(textRetriever.getSource(), element);

        } else if (textRetriever instanceof SOAPEnvelopeTextRetriever) {
            element.addAttribute(fac.createOMAttribute(EvaluatorConstants.TYPE, nullNS,
                    EvaluatorConstants.SOAP));
            addSourceAttribute(textRetriever.getSource(), element);

        } else {
            element.addAttribute(fac.createOMAttribute(EvaluatorConstants.TYPE, nullNS,
                    EvaluatorConstants.URL));
            if (textRetriever.getSource() != null) {
                element.addAttribute(fac.createOMAttribute(EvaluatorConstants.SOURCE,
                        nullNS, textRetriever.getSource()));
            }
        }
    }

    private void addSourceAttribute(String source, OMElement element)
            throws EvaluatorException {

        if (source != null) {
            element.addAttribute(fac.createOMAttribute(EvaluatorConstants.SOURCE, nullNS,
                    source));
        } else {
            String msg = "If type is not URL a source value should be specified for " +
                            "the evaluator";
            log.error(msg);
            throw new EvaluatorException(msg);
        }
    }

}

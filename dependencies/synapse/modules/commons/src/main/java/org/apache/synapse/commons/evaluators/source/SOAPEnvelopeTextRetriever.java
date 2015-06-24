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

package org.apache.synapse.commons.evaluators.source;

import org.apache.synapse.commons.evaluators.EvaluatorContext;
import org.apache.synapse.commons.evaluators.EvaluatorException;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.axiom.om.impl.llom.OMTextImpl;
import org.apache.axiom.om.impl.llom.OMElementImpl;
import org.apache.axiom.om.impl.llom.OMDocumentImpl;
import org.apache.axiom.om.OMAttribute;
import org.jaxen.JaxenException;

import java.util.List;

public class SOAPEnvelopeTextRetriever implements SourceTextRetriever {

    private String source;
    private AXIOMXPath compiledXPath;

    public SOAPEnvelopeTextRetriever(String source) {
        this.source = source;
    }

    public String getSourceText(EvaluatorContext context) throws EvaluatorException {
        SOAPEnvelope envelope = context.getMessageContext().getEnvelope();
        Object result;
        
        try {
            if (compiledXPath == null) {
                compiledXPath = new AXIOMXPath(source);
            }
            result = compiledXPath.evaluate(envelope);
        } catch (JaxenException e) {
            throw new EvaluatorException("Error while parsing the XPath expression: " + source, e);
        }

        if (result instanceof List) {
            List list = (List) result;
            if (list.size() == 1 && list.get(0) == null) {
                return null;
            }

            StringBuffer textValue = new StringBuffer();
            for (Object o : list) {
                if (o instanceof OMTextImpl) {
                    textValue.append(((OMTextImpl) o).getText());

                } else if (o instanceof OMElementImpl) {
                    String s = ((OMElementImpl) o).getText();
                    if (s.trim().length() == 0) {
                        s = o.toString();
                    }
                    textValue.append(s);

                } else if (o instanceof OMDocumentImpl) {
                    textValue.append(((OMDocumentImpl) o).getOMDocumentElement().toString());
                } else if (o instanceof OMAttribute) {
                    textValue.append(((OMAttribute) o).getAttributeValue());
                }
            }

            return textValue.toString();

        } else {
            return result.toString();
        }
    }

    public String getSource() {
        return source;
    }
}

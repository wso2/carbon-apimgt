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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.commons.evaluators.source.SourceTextRetriever;


/**
 * Try to see weather a part of the HTTP request is equal to the value provided.
 * If the values are equal retun true.</p>
 * <pre>
 * &lt;equal type=&quot;header | param | url&quot; source=&quot;&quot; value=&quot;&quot;/&gt;
 * </pre>
 */
public class EqualEvaluator implements Evaluator {

    private Log log = LogFactory.getLog(EqualEvaluator.class);
    private String value = null;

    private SourceTextRetriever textRetriever;

    public boolean evaluate(EvaluatorContext context) throws EvaluatorException {
        String sourceText = textRetriever.getSourceText(context);
        return sourceText != null && sourceText.equalsIgnoreCase(value);
    }

    public String getName() {
        return EvaluatorConstants.EQUAL;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setTextRetriever(SourceTextRetriever textRetriever) {
        this.textRetriever = textRetriever;
    }

    public String getValue() {
        return value;
    }

    public SourceTextRetriever getTextRetriever() {
        return textRetriever;
    }
    
}

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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.UnsupportedEncodingException;

public class ParameterTextRetriever implements SourceTextRetriever {

    private static final Log log = LogFactory.getLog(ParameterTextRetriever.class);

    private String source;

    public ParameterTextRetriever(String source) {
        this.source = source;
    }

    public String getSourceText(EvaluatorContext context) throws EvaluatorException {
        try {
            return context.getParam(source);
        } catch (UnsupportedEncodingException e) {
            String message = "Error retrieving paramter: " + source;
            log.error(message);
            throw new EvaluatorException(message);
        }
    }

    public String getSource() {
        return source;
    }
}

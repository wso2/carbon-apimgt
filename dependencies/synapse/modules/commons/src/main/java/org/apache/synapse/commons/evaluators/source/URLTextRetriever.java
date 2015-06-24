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
import org.apache.synapse.commons.evaluators.EvaluatorConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.URI;
import java.net.URISyntaxException;

public class URLTextRetriever implements SourceTextRetriever {

    private static final Log log = LogFactory.getLog(URLTextRetriever.class);

    private EvaluatorConstants.URI_FRAGMENTS fragment = null;

    public String getSourceText(EvaluatorContext context) throws EvaluatorException {
        if (fragment == null) {
            return context.getUrl();
        }

        try {
            URI uri = new URI(context.getUrl());
            switch (fragment) {
                case protocol: return uri.getScheme();
                case user: return uri.getUserInfo();
                case host: return uri.getHost();
                case port: return String.valueOf(uri.getPort());
                case path: return uri.getPath();
                case query: return uri.getQuery();
                case ref: return uri.getFragment();
                default: return context.getUrl();
            }
        } catch (URISyntaxException e) {
            String message = "Error parsing URL: " + context.getUrl();
            log.error(message);
            throw new EvaluatorException(message);
        }
    }

    public String getSource() {
        if (fragment != null) {
            return fragment.name();
        }
        return null;
    }

    public void setSource(String fragment) {
        this.fragment = EvaluatorConstants.URI_FRAGMENTS.valueOf(fragment);
    }
}

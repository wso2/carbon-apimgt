/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com)
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.gateway.handlers.graphQL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.wso2.carbon.apimgt.common.gateway.dto.ExternalQueryAnalyzerResponseDTO;
import org.wso2.carbon.apimgt.common.gateway.graphql.ExternalQueryAnalyzer;
import org.wso2.carbon.apimgt.gateway.handlers.Utils;
import org.wso2.carbon.apimgt.impl.APIConstants;

/**
 * Handler to analyze incoming GraphQL queries using an external security analyzer.
 */
public class ExternalGraphQLQueryAnalysisHandler extends AbstractHandler
{
    private static final Log log = LogFactory.getLog(ExternalGraphQLQueryAnalysisHandler.class);

    @Override
    public boolean handleRequest(MessageContext messageContext)
    {
        if (Utils.isGraphQLSubscriptionRequest(messageContext)) {
            if (log.isDebugEnabled()) {
                log.debug("Skipping GraphQL subscription handshake request.");
            }
            return true;
        }

        Object queryObject = messageContext.getProperty(APIConstants.GRAPHQL_PAYLOAD);
        if (queryObject == null) {
            log.warn("GraphQL query is missing in the message context.");
            return true;
        }
        String query = queryObject.toString();

        ExternalQueryAnalyzer externalQueryAnalyzer = new ExternalQueryAnalyzer();
        ExternalQueryAnalyzerResponseDTO response = externalQueryAnalyzer.analyseQuery(query);

        if (response.isVulnerable()) {
            log.warn("Vulnerable GraphQL query detected by external analyzer.");
            log.warn("Identified vulnerabilities: " + response.getVulList().toString());
        }

        return true;
    }

    @Override
    public boolean handleResponse(MessageContext messageContext)
    {
        return true;
    }
}

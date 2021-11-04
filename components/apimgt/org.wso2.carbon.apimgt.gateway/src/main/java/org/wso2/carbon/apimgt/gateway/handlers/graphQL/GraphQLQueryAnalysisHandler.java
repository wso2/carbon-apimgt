/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.gateway.handlers.graphQL;

import graphql.schema.GraphQLSchema;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.rest.AbstractHandler;
import org.wso2.carbon.apimgt.gateway.handlers.Utils;
import org.wso2.carbon.apimgt.gateway.handlers.graphQL.analyzer.QueryMutationAnalyzer;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.apimgt.impl.APIConstants;

/**
 * This Handler can be used to analyse GraphQL Query. This implementation uses previously set
 * complexity and depth limitation to block the complex queries before it reaches the backend.
 */
public class GraphQLQueryAnalysisHandler extends AbstractHandler {

    private static final Log log = LogFactory.getLog(GraphQLQueryAnalysisHandler.class);
    private QueryMutationAnalyzer queryMutationAnalyzer;

    public boolean handleRequest(MessageContext messageContext) {
        if (Utils.isGraphQLSubscriptionRequest(messageContext)) {
            if (log.isDebugEnabled()) {
                log.debug("Skipping GraphQL subscription handshake request.");
            }
            return true;
        }
        GraphQLSchema schema = (GraphQLSchema) messageContext.getProperty(APIConstants.GRAPHQL_SCHEMA);
        if (queryMutationAnalyzer == null) {
            queryMutationAnalyzer = new QueryMutationAnalyzer(schema);
        }
        String payload = messageContext.getProperty(APIConstants.GRAPHQL_PAYLOAD).toString();
        if (!analyseQuery(messageContext, payload)) {
            if (log.isDebugEnabled()) {
                log.debug("Query was blocked by the static query analyser");
            }
            return false;
        }
        return true;
    }

    /**
     * This method analyses the query.
     *
     * @param messageContext message context of the request
     * @param payload        payload of the request
     * @return true, if the query is not blocked or false, if the query is blocked
     */
    private boolean analyseQuery(MessageContext messageContext, String payload) {

        try {
            return queryMutationAnalyzer.analyseQueryMutationDepth(messageContext, payload) &&
                    queryMutationAnalyzer.analyseQueryMutationComplexity(messageContext, payload);
        } catch (Exception e) {
            String errorMessage = "Policy definition parsing failed. ";
            log.error(errorMessage, e);
            handleFailure(messageContext);
            return false;
        }
    }

    /**
     * This method handle the failure.
     *
     * @param messageContext message context of the request
     */
    private void handleFailure(MessageContext messageContext) {
        messageContext.setProperty(SynapseConstants.ERROR_CODE, APISecurityConstants.API_AUTH_GENERAL_ERROR);
        messageContext.setProperty(SynapseConstants.ERROR_MESSAGE, APISecurityConstants.API_AUTH_GENERAL_ERROR_MESSAGE);
        messageContext.setProperty(SynapseConstants.ERROR_EXCEPTION,
                APISecurityConstants.API_AUTH_GENERAL_ERROR_MESSAGE);
        Mediator sequence = messageContext.getSequence(GraphQLConstants.GRAPHQL_API_FAILURE_HANDLER);
        if (sequence != null && !sequence.mediate(messageContext)) {
            return;
        }
        Utils.sendFault(messageContext, HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    @Override
    public boolean handleResponse(MessageContext messageContext) {
        return true;
    }
}
